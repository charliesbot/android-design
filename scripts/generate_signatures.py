#!/usr/bin/env python3
"""Generates plugin/skills/android-design-explore/references/compose-signatures.md from pinned sources.

Reads versions from app/gradle/libs.versions.toml, validates Material 3 names against
scripts/material3-inventory.txt, verifies Kit DSL declarations in app/host/.../kit/, and checks
Wear M3 and Foundation symbols from cached AndroidX Wear AARs.
"""

import argparse
import io
import re
import zipfile
from pathlib import Path

from material3_inventory import find_aar

ROOT = Path(__file__).resolve().parent.parent
APP_TOML = ROOT / "app/gradle/libs.versions.toml"
INVENTORY_PATH = ROOT / "scripts/material3-inventory.txt"
KIT_DIR = ROOT / "app/host/src/main/kotlin/bot/charlies/androiddesign/preview/kit"
TOKENS_KT = ROOT / "app/core/model/src/main/kotlin/bot/charlies/androiddesign/preview/model/Tokens.kt"
GRADLE_FILES = Path.home() / ".gradle/caches/modules-2/files-2.1"
OUT_PATH = ROOT / "plugin/skills/android-design-explore/references/compose-signatures.md"

REQUIRED_M3_INVENTORY_SYMBOLS = (
    "MaterialExpressiveTheme",
    "MotionScheme.expressive",
    "MotionScheme.standard",
    "MediumFlexibleTopAppBar",
    "LargeFlexibleTopAppBar",
    "FlexibleBottomAppBar",
    "HorizontalFloatingToolbar",
    "VerticalFloatingToolbar",
    "TopAppBarDefaults.pinnedScrollBehavior",
    "TopAppBarDefaults.enterAlwaysScrollBehavior",
    "FloatingToolbarDefaults.standardFloatingToolbarColors",
    "FloatingToolbarDefaults.vibrantFloatingToolbarColors",
    "ShortNavigationBar",
    "ShortNavigationBarItem",
    "WideNavigationRail",
    "WideNavigationRailItem",
    "ModalWideNavigationRail",
    "rememberWideNavigationRailState",
    "Button",
    "FilledTonalButton",
    "OutlinedButton",
    "ButtonDefaults.shapesFor",
    "ToggleButton",
    "FilledTonalToggleButton",
    "ButtonGroup",
    "ButtonGroupDefaults.OverflowIndicator",
    "ButtonGroupDefaults.connectedLeadingButtonShapes",
    "ButtonGroupDefaults.connectedMiddleButtonShapes",
    "ButtonGroupDefaults.connectedTrailingButtonShapes",
    "SplitButtonLayout",
    "SplitButtonDefaults.LeadingButton",
    "SplitButtonDefaults.TrailingButton",
    "FilterChip",
    "ElevatedFilterChip",
    "InputChip",
    "FilterChipDefaults.shapes",
    "InputChipDefaults.shapes",
    "MediumFloatingActionButton",
    "LargeFloatingActionButton",
    "FloatingActionButtonMenu",
    "FloatingActionButtonMenuItem",
    "ToggleFloatingActionButton",
    "ListItem",
    "SegmentedListItem",
    "ListItemDefaults.segmentedShapes",
    "ListItemDefaults.segmentedColors",
    "ListItemDefaults.SegmentedGap",
    "ListItemDefaults.verticalAlignment",
    "LoadingIndicator",
    "ContainedLoadingIndicator",
    "LinearWavyProgressIndicator",
    "CircularWavyProgressIndicator",
    "DropdownMenuPopup",
    "DropdownMenuGroup",
    "DropdownMenuItem",
    "SelectableDropdownMenuItem",
    "CheckableDropdownMenuItem",
    "MenuDefaults.groupShape",
    "MenuDefaults.itemShape",
    "MenuDefaults.groupVibrantContainerColor",
    "MenuDefaults.rememberDropdownMenuPopupPositionProvider",
    "MenuAnchorPosition.End",
    "Slider",
    "VerticalSlider",
    "rememberSliderState",
    "RangeSlider",
    "rememberRangeSliderState",
    "RangeSliderState.startValue",
    "RangeSliderState.endValue",
    "WideNavigationRailItemDefaults.colors",
    "TextField",
    "OutlinedTextField",
    "SecureTextField",
    "OutlinedSecureTextField",
    "TextFieldDefaults.roundedShape",
    "TextFieldDefaults.tonalColors",
    "OutlinedTextFieldDefaults.roundedShape",
    "OutlinedTextFieldDefaults.tonalColors",
    "SearchBar",
    "rememberSearchBarState",
    "SearchBarDefaults.InputField",
    "AppBarWithSearch",
    "ExpandedFullScreenContainedSearchBar",
    "ExpandedDockedSearchBarWithGap",
    "BottomSheet",
    "rememberBottomSheetState",
    "Scrim",
    "HorizontalCenteredHeroCarousel",
    "HorizontalMultiBrowseCarousel",
    "HorizontalUncontainedCarousel",
    "rememberCarouselState",
    "CarouselDefaults.ItemSpacing",
    "CarouselDefaults.ContentPadding",
    "carouselParallaxScrollEffect",
    "TooltipBox",
    "PlainTooltip",
    "RichTooltip",
    "TooltipDefaults.rememberTooltipPositionProvider",
    "TooltipDefaults.caretShape",
    "TooltipAnchorPosition.Above",
    "rememberTooltipState",
    "LocalRippleThemeConfiguration",
    "Typography",
    "ScrollField",
    "rememberScrollFieldState",
    "TimeScroll",
    "TimePickerDefaults.vibrantColors",
    "TimeInputDefaults.vibrantColors",
    "TimePickerDefaults.shapes",
    "VibrantTimePickerDialog",
    "rememberTimePickerState",
    "TimePickerDialogDefaults.DisplayModeToggle",
    "TimePickerDialogDefaults.ScrollDisplayModeToggle",
    "MaterialShapes.Cookie9Sided",
    "MaterialShapes.Flower",
    "MaterialShapes.Sunny",
    "toShape",
)


def read_toml_version(path: Path, key: str) -> str:
    text = path.read_text(encoding="utf-8")
    match = re.search(rf'^{re.escape(key)}\s*=\s*"([^"]+)"', text, re.M)
    if not match:
        raise RuntimeError(f"Missing version key '{key}' in {path}")
    return match.group(1)


def load_inventory() -> set[str]:
    return {
        line.strip()
        for line in INVENTORY_PATH.read_text(encoding="utf-8").splitlines()
        if line.strip() and not line.startswith("#")
    }


def extract_kit_enums() -> tuple[list[str], list[str]]:
    tokens = TOKENS_KT.read_text(encoding="utf-8")
    variants_block = re.search(r"enum class PaletteVariant[^{]*\{(.*?);", tokens, re.S)
    if not variants_block:
        raise RuntimeError(f"Could not find PaletteVariant enum in {TOKENS_KT}")
    variants = re.findall(r"^  ([A-Z]\w+)\(", variants_block.group(1), re.M)

    fonts_block = re.search(r"enum class BundledFont[^{]*\{(.*?);", tokens, re.S)
    if not fonts_block:
        raise RuntimeError(f"Could not find BundledFont enum in {TOKENS_KT}")
    fonts = re.findall(r"^  ([A-Z]\w+)\(", fonts_block.group(1), re.M)
    return variants, fonts


def verify_kit_declarations() -> None:
    board_src = (KIT_DIR / "DesignBoard.kt").read_text(encoding="utf-8")
    kit_src = (KIT_DIR / "ExploreKit.kt").read_text(encoding="utf-8")
    assets_src = (KIT_DIR / "DesignAssets.kt").read_text(encoding="utf-8")

    expected_board = (
        "fun designBoard(",
        "fun wearBoard(",
        "fun direction(",
        "fun screen(",
        "fun listDetail(",
        "fun supportingPane(",
        "fun overlay(",
    )
    for marker in expected_board:
        if marker not in board_src:
            raise RuntimeError(f"Missing '{marker}' in DesignBoard.kt")
    for marker in ("fun SymbolIcon(", "fun bundledFontFamily(", 'const val AD_PRIMARY_TAG = "ad:primary"'):
        if marker not in kit_src:
            raise RuntimeError(f"Missing '{marker}' in ExploreKit.kt")
    if "fun designPainter(" not in assets_src:
        raise RuntimeError("Missing 'fun designPainter(' in DesignAssets.kt")


def verify_wear_aars(
    wear_version: str,
    gradle_files: Path = GRADLE_FILES,
    require_cache: bool = True,
) -> None:
    _, m3_aar = find_aar(
        "androidx.wear.compose", "compose-material3", wear_version, gradle_files=gradle_files
    )
    _, fn_aar = find_aar(
        "androidx.wear.compose", "compose-foundation", wear_version, gradle_files=gradle_files
    )
    if m3_aar is None or fn_aar is None:
        if require_cache:
            raise RuntimeError(
                f"Missing androidx.wear.compose {wear_version} AARs in {gradle_files}; run `cd app && ./gradlew :host:exportBootstrap`"
            )
        return

    m3_checks = (
        ("androidx/wear/compose/material3/AppScaffoldKt.class", b"AppScaffold"),
        ("androidx/wear/compose/material3/ScreenScaffoldKt.class", b"ScreenScaffold"),
        ("androidx/wear/compose/material3/EdgeButtonKt.class", b"EdgeButton"),
        ("androidx/wear/compose/material3/ButtonKt.class", b"FilledTonalButton"),
        ("androidx/wear/compose/material3/CardKt.class", b"TitleCard"),
        ("androidx/wear/compose/material3/SwitchButtonKt.class", b"SwitchButton"),
        ("androidx/wear/compose/material3/AlertDialogKt.class", b"AlertDialog"),
        ("androidx/wear/compose/material3/ConfirmationDialogKt.class", b"ConfirmationDialog"),
        ("androidx/wear/compose/material3/TimeTextKt.class", b"TimeText"),
        ("androidx/wear/compose/material3/ListHeaderKt.class", b"ListHeader"),
        ("androidx/wear/compose/material3/CircularProgressIndicatorKt.class", b"CircularProgressIndicator"),
        ("androidx/wear/compose/material3/CheckboxButtonKt.class", b"CheckboxButton"),
    )
    fn_checks = (
        ("androidx/wear/compose/foundation/lazy/TransformingLazyColumnKt.class", b"TransformingLazyColumn"),
    )
    for aar_path, checks in ((m3_aar, m3_checks), (fn_aar, fn_checks)):
        with zipfile.ZipFile(aar_path) as aar:
            classes_bytes = aar.read("classes.jar")
        with zipfile.ZipFile(io.BytesIO(classes_bytes)) as jar:
            names = set(jar.namelist())
            for cls_name, sym in checks:
                if cls_name not in names:
                    raise RuntimeError(f"Missing Wear class {cls_name} in {aar_path}")
                if sym not in jar.read(cls_name):
                    raise RuntimeError(f"Missing symbol {sym.decode()} in {cls_name} ({aar_path})")


def verify_inventory(inventory: set[str]) -> list[str]:
    missing = [sym for sym in REQUIRED_M3_INVENTORY_SYMBOLS if sym not in inventory]
    return missing


def render_markdown(require_wear_cache: bool = False) -> str:
    inventory = load_inventory()
    missing = verify_inventory(inventory)
    if missing:
        raise RuntimeError(
            f"scripts/material3-inventory.txt is missing required symbols: {', '.join(missing)}"
        )

    verify_kit_declarations()
    variants, fonts = extract_kit_enums()

    androidx_m3 = read_toml_version(APP_TOML, "androidxMaterial3")
    wear_m3 = read_toml_version(APP_TOML, "androidxWearCompose")
    verify_wear_aars(wear_m3, require_cache=require_wear_cache)

    shape_samples = ", ".join(
        name.removeprefix("MaterialShapes.")
        for name in sorted(inventory)
        if name.startswith("MaterialShapes.")
    )

    lines = [
        "# Compose Expressive Signature Reference",
        "",
        f"Generated by `scripts/generate_signatures.py` from pinned AndroidX M3 `{androidx_m3}` and Wear M3 `{wear_m3}`.",
        "Use exact signatures below rather than guessing parameter names from older Material versions.",
        "",
        "## `DESIGN.md` Front Matter",
        "",
        "```yaml",
        "---",
        "name: App name",
        "colors:",
        "  primary: \"#B5523B\"",
        "material:",
        "  scheme: { seed: \"{colors.primary}\", variant: tonal-spot, contrast: 0 }",
        "---",
        "```",
        "",
        "- `material.scheme.variant` is lowercase kebab-case (`tonal-spot`, `neutral`, `content`, `fidelity`, `monochrome`, `rainbow`, `fruit-salad`); avoid `expressive` and `vibrant` on phone because they flood surfaces with chroma.",
        "- Set `brandFont = BundledFont.Fraunces` (or `BricolageGrotesque`, `BigShouldersDisplay`, or `Doto` at Display only) directly on `designBoard(...)`. The board draws the face at its starting voice from the theming reference (Fraunces `wght` 600 with `SOFT` 100 and `WONK` 1, Bricolage Grotesque and Big Shoulders Display `wght` 800, Doto `wght` 700); give a hero `Text` its own `fontWeight` or axes when the direction wants a different voice. For custom axes in `DESIGN.md`, keys under `typography:` must be kebab-case Material type roles (`display-large: { fontFamily: Fraunces, fontVariation: \"'opsz' 72\" }`), never `typography.brand`.",
        "",
        "## Exploration Kit & Responsive Sizing (`import bot.charlies.androiddesign.preview.kit.*`)",
        "",
        "```kotlin",
        "val preview = designBoard(name = \"Exploration\", seed = 0xFFB5523BL, variant = PaletteVariant.TonalSpot, brandFont = BundledFont.Fraunces) { ... }",
        "val preview = wearBoard(name = \"Wear\", seed = 0xFF3E8E7EL) { ... } // top-level property returning ExplorationBoard",
        "fun direction(name: String, block: DirectionScope.() -> Unit)",
        "fun screen(id: String = \"main\", content: @Composable () -> Unit) // first screen declared is start",
        "fun listDetail(id: String = \"list\", detailId: String = \"detail\", list: @Composable (onOpenDetail: () -> Unit) -> Unit, detail: @Composable () -> Unit, placeholder: (@Composable () -> Unit)? = null)",
        "fun supportingPane(id: String = \"main\", supportingId: String = \"supporting\", main: @Composable (onOpenSupporting: () -> Unit) -> Unit, supporting: @Composable () -> Unit)",
        "fun overlay(id: String, screen: String? = null, content: @Composable () -> Unit) // opens on active overlay inspection",
        "@Composable fun SymbolIcon(name: String, contentDescription: String? = null, modifier: Modifier = Modifier, filled: Boolean = false, size: Dp = 24.dp, tint: Color = LocalContentColor.current)",
        "@Composable fun designPainter(relativePath: String): Painter // confined beneath .explore.kt dir, e.g. \"assets/cover.jpg\"",
        "fun bundledFontFamily(font: BundledFont, weight: Int = 400, variation: String? = null): FontFamily",
        "// Responsive breakpoints (LocalConfiguration/android.content.* are blocked in preview sandbox):",
        "BoxWithConstraints { val compact = maxWidth < 600.dp; val expanded = maxWidth >= 840.dp; ... }",
        "FlexBox(config = { wrap(FlexWrap.Wrap); gap(8.dp) }) { ... } // wrapping chips/badges (NoWrap default; replaces FlowRow)",
        "```",
        "",
        f"- `PaletteVariant`: `{'`, `'.join(variants)}`",
        f"- `BundledFont`: `{'`, `'.join(fonts)}` (`Doto` at Display only; brand faces only on Display/Headline/`titleLarge`; `GoogleSansCode` on any role)",
        "- Primary CTA tag: `Modifier.testTag(\"ad:primary\")` (alias `AD_PRIMARY_TAG`).",
        "",
        "## Material 3 Expressive (`LoadingIndicator` and `MaterialShapes` need `@OptIn(ExperimentalMaterial3ExpressiveApi::class)`; `AppBarWithSearch`, `BottomSheet`, `ModalBottomSheet`, `TooltipBox`, and `carouselParallaxScrollEffect` need `@OptIn(ExperimentalMaterial3Api::class)`)",
        "",
        "```kotlin",
        "// Theme & Motion",
        "MaterialExpressiveTheme(colorScheme = ..., motionScheme = MotionScheme.expressive(), shapes = ..., typography = ...) { ... }",
        "MaterialTheme.motionScheme.fastSpatialSpec() // defaultSpatialSpec(), slowSpatialSpec(), fastEffectsSpec()",
        "Typography(fontFamily = ...) // propagates family to unset roles; Text(\"...\", color = { animatedColor }) // ColorProducer; LocalRippleThemeConfiguration",
        "",
        "// App Bars & Toolbars (subtitle only on Flexible bars)",
        "MediumFlexibleTopAppBar(title = { Text(...) }, modifier = Modifier, subtitle = { Text(...) }, navigationIcon = {}, actions = {}, scrollBehavior = ...)",
        "LargeFlexibleTopAppBar(title = { Text(...) }, modifier = Modifier, subtitle = { Text(...) }, navigationIcon = {}, actions = {}, scrollBehavior = ...)",
        "val topBarBehavior = TopAppBarDefaults.pinnedScrollBehavior(listState) // or enterAlwaysScrollBehavior(listState) (takes ScrollableState; custom lambda uses isScrollingContentAtStart)",
        "FlexibleBottomAppBar(modifier = Modifier, horizontalArrangement = ..., content = { ... })",
        "HorizontalFloatingToolbar(expanded = true, floatingActionButton = { FloatingToolbarDefaults.StandardFloatingActionButton(...) }, colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()) { ... }",
        "VerticalFloatingToolbar(expanded = true, colors = FloatingToolbarDefaults.standardFloatingToolbarColors()) { ... }",
        "",
        "// Navigation (phone bar -> foldable/tablet rail; railExpanded = false fits default 96.dp rail width)",
        "ShortNavigationBar { ShortNavigationBarItem(selected = ..., onClick = {}, icon = { ... }, label = { Text(...) }, iconPosition = NavigationItemIconPosition.Top) }",
        "WideNavigationRail(state = rememberWideNavigationRailState(), header = { ... }) { WideNavigationRailItem(railExpanded = false, icon = { ... }, label = { Text(...) }, selected = ..., onClick = {}, colors = WideNavigationRailItemDefaults.colors()) }",
        "ModalWideNavigationRail(state = rememberWideNavigationRailState(), hideOnCollapse = false, header = { ... }) { ... }",
        "",
        "// Buttons, Toggle/Split/Button Groups, Chips, Sliders & FABs (XS 32.dp, S 40.dp, M 56.dp, L 96.dp, XL 136.dp)",
        "Button(onClick = {}, shapes = ButtonDefaults.shapesFor(56.dp), contentPadding = ButtonDefaults.contentPaddingFor(56.dp)) { ... }",
        "FilledTonalButton(onClick = {}, shapes = ButtonDefaults.shapesFor(40.dp)) { ... }",
        "ButtonGroup(overflowIndicator = { ButtonGroupDefaults.OverflowIndicator(it) }) { clickableItem(onClick = {}, label = \"...\") }",
        "Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {",
        "  ToggleButton(checked = ..., onCheckedChange = {}, shapes = ButtonGroupDefaults.connectedLeadingButtonShapes()) { ... }",
        "  ToggleButton(checked = ..., onCheckedChange = {}, shapes = ButtonGroupDefaults.connectedMiddleButtonShapes()) { ... }",
        "  FilledTonalToggleButton(checked = ..., onCheckedChange = {}, shapes = ButtonGroupDefaults.connectedTrailingButtonShapes()) { ... }",
        "}",
        "SplitButtonLayout(leadingButton = { SplitButtonDefaults.LeadingButton(onClick = {}) { Text(\"...\") } }, trailingButton = { SplitButtonDefaults.TrailingButton(checked = ..., onCheckedChange = {}) { ... } })",
        "FilterChip(selected = ..., onClick = {}, label = { Text(\"...\") }, shapes = FilterChipDefaults.shapes()) // also ElevatedFilterChip, InputChip(shapes = InputChipDefaults.shapes())",
        "val sliderState = rememberSliderState(value = 0.5f); Slider(state = sliderState, onValueChange = { sliderState.value = it }) // also VerticalSlider",
        "val rangeState = rememberRangeSliderState(startValue = 0.2f, endValue = 0.8f); RangeSlider(state = rangeState, onValueChange = { rangeState.startValue = it.start; rangeState.endValue = it.endInclusive }) // startValue/endValue replace activeRangeStart/End",
        "MediumFloatingActionButton(onClick = {}) { ... } // or LargeFloatingActionButton",
        "FloatingActionButtonMenu(expanded = ..., button = { ToggleFloatingActionButton(checked = ..., onCheckedChange = {}) { ... } }) { FloatingActionButtonMenuItem(onClick = {}, text = {}, icon = {}) }",
        "",
        "// Lists (main body uses trailing `content` lambda, never headlineContent), Indicators & Menus",
        "ListItem(onClick = {}, verticalAlignment = ListItemDefaults.verticalAlignment()) { Text(\"...\") }",
        "Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {",
        "  SegmentedListItem(onClick = {}, shapes = ListItemDefaults.segmentedShapes(index = i, count = n), leadingContent = {}, supportingContent = {}, trailingContent = {}) { Text(\"...\") }",
        "}",
        "LoadingIndicator(modifier = Modifier.size(48.dp)) // or ContainedLoadingIndicator()",
        "LinearWavyProgressIndicator(progress = { 0.65f }) // or CircularWavyProgressIndicator(progress = { 0.65f })",
        "DropdownMenuPopup(expanded = ..., onDismissRequest = {}) { // submenus: popupPositionProvider = MenuDefaults.rememberDropdownMenuPopupPositionProvider(MenuAnchorPosition.End)",
        "  DropdownMenuGroup(shapes = MenuDefaults.groupShape(index = 0, count = 2)) {",
        "    DropdownMenuItem(onClick = {}, text = { Text(...) }, shape = MenuDefaults.itemShape(0, 3).shape)",
        "    SelectableDropdownMenuItem(selected = ..., onClick = {}, text = { Text(...) }, shapes = MenuDefaults.itemShape(1, 3))",
        "    CheckableDropdownMenuItem(checked = ..., onCheckedChange = {}, text = { Text(...) }, shapes = MenuDefaults.itemShape(2, 3))",
        "  }",
        "}",
        "Modifier.clip(MaterialShapes.Cookie9Sided.toShape()) // shapes below",
        "// Surface blur (androidx.compose.ui.draw.{blur, BlurredEdgeTreatment}; use uniform Modifier.blur in explorations, reserve progressive BlurRadiusSpec in theming.md for app builds)",
        "Modifier.blur(radius = 2.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded) // or BlurredEdgeTreatment(shape) on clipped cards/images",
        "",
        "// Text Fields & Search (state-based TextFieldState; SecureTextField / OutlinedSecureTextField for passwords)",
        "TextField(state = rememberTextFieldState(), label = { Text(...) }, shape = TextFieldDefaults.roundedShape, colors = TextFieldDefaults.tonalColors(), lineLimits = TextFieldLineLimits.SingleLine)",
        "OutlinedTextField(state = rememberTextFieldState(), label = { Text(...) }, shape = OutlinedTextFieldDefaults.roundedShape, colors = OutlinedTextFieldDefaults.tonalColors(), labelPosition = TextFieldLabelPosition.Inside())",
        "OutlinedSecureTextField(state = rememberTextFieldState(), label = { Text(\"Password\") }) // or SecureTextField(...)",
        "val searchState = rememberSearchBarState(); SearchBar(state = searchState, inputField = { SearchBarDefaults.InputField(textFieldState = rememberTextFieldState(), searchBarState = searchState, onSearch = {}) })",
        "ExpandedDockedSearchBarWithGap(state = searchState, inputField = { ... }) { ... } // tablet; phone: ExpandedFullScreenContainedSearchBar(...)",
        "AppBarWithSearch(state = searchState, inputField = { ... })",
        "",
        "// Sheets, Scrim, Carousels & Tooltips",
        "BottomSheet(state = rememberBottomSheetState(initialValue = SheetValue.Expanded), onDismissRequest = {}) { ... } // static non-modal; place it after Scrim as a sibling filling the screen, never Modifier.align(Alignment.BottomCenter) (it offsets itself to the bottom, so aligning pushes it offscreen); modal: ModalBottomSheet(...)",
        "Scrim(contentDescription = \"Dismiss\", onClick = {}, alpha = { 0.32f })",
        "HorizontalCenteredHeroCarousel(state = rememberCarouselState { n }, preferredItemWidth = 220.dp, itemSpacing = CarouselDefaults.ItemSpacing, contentPadding = CarouselDefaults.ContentPadding) { i -> ... } // preferredItemWidth replaces maxItemWidth; also HorizontalMultiBrowseCarousel, HorizontalUncontainedCarousel",
        "TooltipBox(positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above), tooltip = { PlainTooltip(caretShape = TooltipDefaults.caretShape()) { Text(\"...\") } }, state = rememberTooltipState()) { ... } // or RichTooltip",
        "",
        "// Scroll Fields & Expressive Time Pickers (inline ScrollField / TimeScroll preferred; vibrantColors for Expressive style)",
        "val scrollState = rememberScrollFieldState(itemCount = 60, index = 0); ScrollField(state = scrollState, contentDescription = \"Minutes\") { index, _, _ -> Text(\"$index\") }",
        "val timeState = rememberTimePickerState(); TimeScroll(state = timeState, shapes = TimePickerDefaults.shapes(), toggle = { TimePickerDialogDefaults.ScrollDisplayModeToggle(...) }, colors = TimePickerDefaults.vibrantColors()) // modal: VibrantTimePickerDialog(...)",
        "```",
        "",
        f"Available `MaterialShapes`: `{shape_samples}`.",
        "",
        "## Wear OS Material 3 (`import androidx.wear.compose.material3.*` + `androidx.wear.compose.foundation.lazy.*`)",
        "",
        "```kotlin",
        "AppScaffold(timeText = { TimeText { timeTextCurvedText(\"10:08\") } }) { ... } // wrap once at direction/root",
        "val listState = rememberTransformingLazyColumnState()",
        "ScreenScaffold(scrollState = listState, edgeButton = { EdgeButton(onClick = {}, buttonSize = EdgeButtonSize.Medium) { Text(...) } }) { contentPadding ->",
        "  TransformingLazyColumn(state = listState, contentPadding = contentPadding) { ... }",
        "}",
        "ScreenScaffold { contentPadding -> /* non-scrolling fixed watch screen */ }",
        "CircularProgressIndicator(progress = { 0.62f }, modifier = Modifier.fillMaxSize().padding(1.dp), startAngle = 295f, endAngle = 245f, strokeWidth = CircularProgressIndicatorDefaults.largeStrokeWidth)",
        "Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text(...) } // FilledTonalButton, OutlinedButton",
        "IconButton(onClick = {}) { ... } // or FilledIconButton(onClick = {})",
        "TitleCard(onClick = {}, title = { Text(...) }, time = { Text(...) }) { Text(...) } // or Card(onClick = {})",
        "SwitchButton(checked = ..., onCheckedChange = {}) { Text(...) }",
        "CheckboxButton(checked = ..., onCheckedChange = {}) { Text(...) }",
        "ListHeader { Text(...) }",
        "AlertDialog(visible = ..., onDismissRequest = {}, confirmButton = { AlertDialogDefaults.ConfirmButton(onClick = {}) }, dismissButton = { AlertDialogDefaults.DismissButton(onClick = {}) }, title = { Text(...) }, text = { Text(...) })",
        "val style = ConfirmationDialogDefaults.curvedTextStyle // read property in @Composable scope before CurvedScope lambda",
        "ConfirmationDialog(visible = ..., onDismissRequest = {}, curvedText = { confirmationDialogCurvedText(\"Done\", style) }) { ... }",
        "```",
        "",
        "## First-Pass Check Rules",
        "",
        "1. `line-length`: cap reading text, `Card`, and `ListItem` panes to `Modifier.widthIn(max = 420.dp)` at tablet/desktop widths (`<= 60` chars/line).",
        "2. `contrast`: use `onSurface` or `onSurfaceVariant` for text on surfaces, never `outline` or `outlineVariant` (`>= 4.5:1` body, `>= 3:1` large).",
        "3. `primary-largest`: tag one CTA with `Modifier.testTag(\"ad:primary\")` and size its height `>=` peer buttons/chips (`EdgeButtonSize.Medium` or `Large` on Wear when paired with chips).",
        "4. `fonts`: brand fonts apply only on Display, Headline, and `titleLarge` (`Doto` Display only); use `bundledFontFamily(BundledFont.GoogleSansCode)` for inline numerals on Body/Label.",
        "5. `overflow` & `label-truncated`: keep button/chip/nav labels short (1 to 2 words, single line) and avoid unwrapped multi-badge rows on foldable panes (`FlexBox(config = { wrap(FlexWrap.Wrap); gap(8.dp) })` or compact counts).",
        "6. `semantics`: tag section headings with `Modifier.semantics { heading() }`; on toggling/selecting rows lift interaction to the row (`Modifier.toggleable`/`selectable` with `role`, or `SegmentedListItem(onClick = ...)` with `Modifier.semantics { role = Role.Switch; toggleableState = ToggleableState(checked) }`) and pass `null` to the child `Switch`/`Checkbox`/`RadioButton`.",
        "7. `fields`: build text fields and `SearchBarDefaults.InputField` on `TextFieldState` (`rememberTextFieldState()`); use `SecureTextField` or `OutlinedSecureTextField` for passwords (never value-based `onValueChange` or `PasswordVisualTransformation`).",
        "8. `visibility` & `blur`: show/hide with `AnimatedVisibility`, `AnimatedContent`, or `if` (never `alpha = 0f` alone); back text over blurred photos with a scrim, gradient, or solid/tonal surface.",
        "",
    ]

    output = "\n".join(lines)
    if "\u2014" in output:
        raise RuntimeError("Generated compose-signatures.md contains an em-dash")
    if len(lines) > 220:
        raise RuntimeError(f"Generated compose-signatures.md is {len(lines)} lines (> 220)")
    return output


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--out", type=Path, default=OUT_PATH)
    args = parser.parse_args()
    markdown = render_markdown(require_wear_cache=True)
    args.out.parent.mkdir(parents=True, exist_ok=True)
    args.out.write_text(markdown, encoding="utf-8")
    print(f"Wrote {args.out.relative_to(ROOT)} ({len(markdown.splitlines())} lines)")


if __name__ == "__main__":
    main()
