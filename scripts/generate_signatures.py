#!/usr/bin/env python3
"""Generates plugin/skills/android-design-explore/references/compose-signatures.md from pinned sources.

Reads versions from app/gradle/libs.versions.toml, evals/compose-starter/gradle/libs.versions.toml,
and app/wear/compose/upstream/VERSION, validates Material 3 names against scripts/material3-inventory.txt,
extracts exact Kit DSL signatures from app/features/preview/desktop/.../kit/, and checks Wear M3 upstream
signatures from app/wear/compose/upstream/.
"""

import argparse
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
APP_TOML = ROOT / "app/gradle/libs.versions.toml"
STARTER_TOML = ROOT / "evals/compose-starter/gradle/libs.versions.toml"
WEAR_VERSION = ROOT / "app/wear/compose/upstream/VERSION"
INVENTORY_PATH = ROOT / "scripts/material3-inventory.txt"
KIT_DIR = ROOT / "app/features/preview/desktop/src/main/kotlin/bot/charlies/androiddesign/preview/kit"
TOKENS_KT = ROOT / "app/core/model/src/main/kotlin/bot/charlies/androiddesign/preview/model/Tokens.kt"
WEAR_M3_DIR = ROOT / "app/wear/compose/upstream/material3/androidx/wear/compose/material3"
WEAR_FOUNDATION_DIR = ROOT / "app/wear/compose/upstream/foundation/androidx/wear/compose/foundation"
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
    "ButtonGroupDefaults.connectedLeadingButtonShapes",
    "ButtonGroupDefaults.connectedMiddleButtonShapes",
    "ButtonGroupDefaults.connectedTrailingButtonShapes",
    "MediumFloatingActionButton",
    "LargeFloatingActionButton",
    "FloatingActionButtonMenu",
    "FloatingActionButtonMenuItem",
    "ToggleFloatingActionButton",
    "SegmentedListItem",
    "ListItemDefaults.segmentedShapes",
    "ListItemDefaults.segmentedColors",
    "ListItemDefaults.SegmentedGap",
    "LoadingIndicator",
    "ContainedLoadingIndicator",
    "LinearWavyProgressIndicator",
    "CircularWavyProgressIndicator",
    "DropdownMenuPopup",
    "DropdownMenuGroup",
    "DropdownMenuItem",
    "MenuDefaults.groupShape",
    "MenuDefaults.itemShape",
    "MenuDefaults.groupVibrantContainerColor",
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
    variants = re.findall(r"^\s*([A-Z]\w+)\(", variants_block.group(1), re.M)

    fonts_block = re.search(r"enum class BundledFont[^{]*\{(.*?);", tokens, re.S)
    if not fonts_block:
        raise RuntimeError(f"Could not find BundledFont enum in {TOKENS_KT}")
    fonts = re.findall(r"^\s*([A-Z]\w+)\(", fonts_block.group(1), re.M)
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


def verify_wear_upstream() -> None:
    expected = {
        WEAR_M3_DIR / "AppScaffold.kt": "fun AppScaffold(",
        WEAR_M3_DIR / "ScreenScaffold.kt": "fun ScreenScaffold(",
        WEAR_M3_DIR / "EdgeButton.kt": "fun EdgeButton(",
        WEAR_M3_DIR / "Button.kt": "fun FilledTonalButton(",
        WEAR_M3_DIR / "Card.kt": "fun TitleCard(",
        WEAR_M3_DIR / "SwitchButton.kt": "fun SwitchButton(",
        WEAR_M3_DIR / "AlertDialog.kt": "fun AlertDialog(",
        WEAR_M3_DIR / "ConfirmationDialog.kt": "fun ConfirmationDialog(",
        WEAR_M3_DIR / "TimeText.kt": "fun TimeText(",
        WEAR_M3_DIR / "ListHeader.kt": "fun ListHeader(",
        WEAR_FOUNDATION_DIR / "lazy/TransformingLazyColumn.kt": "fun TransformingLazyColumn(",
    }
    for path, marker in expected.items():
        if not path.is_file():
            raise RuntimeError(f"Missing Wear upstream source: {path}")
        if marker not in path.read_text(encoding="utf-8"):
            raise RuntimeError(f"Missing '{marker}' in {path}")


def verify_inventory(inventory: set[str]) -> list[str]:
    missing = [sym for sym in REQUIRED_M3_INVENTORY_SYMBOLS if sym not in inventory]
    return missing


def render_markdown() -> str:
    inventory = load_inventory()
    missing = verify_inventory(inventory)
    if missing:
        raise RuntimeError(
            f"scripts/material3-inventory.txt is missing required symbols: {', '.join(missing)}"
        )

    verify_kit_declarations()
    verify_wear_upstream()
    variants, fonts = extract_kit_enums()

    starter_m3 = read_toml_version(STARTER_TOML, "material3")
    desktop_m3 = read_toml_version(APP_TOML, "material3")
    wear_m3 = WEAR_VERSION.read_text(encoding="utf-8").strip()

    shape_samples = ", ".join(
        name.removeprefix("MaterialShapes.")
        for name in sorted(inventory)
        if name.startswith("MaterialShapes.")
    )

    lines = [
        "# Compose Expressive Signature Reference",
        "",
        f"Generated by `scripts/generate_signatures.py` from pinned M3 `{starter_m3}` (runtime `{desktop_m3}`) and Wear M3 `{wear_m3}`.",
        "Use exact parameter names below rather than guessing beta signatures from older Material versions.",
        "",
        "## Exploration Kit (`import bot.charlies.androiddesign.preview.kit.*`)",
        "",
        "```kotlin",
        "fun designBoard(name: String = \"Exploration\", seed: Long? = null, variant: PaletteVariant? = null, contrast: Double? = null, brandFont: BundledFont? = null, block: DesignBoardScope.() -> Unit): ExplorationBoard",
        "fun wearBoard(name: String = \"Wear exploration\", seed: Long? = null, block: WearBoardScope.() -> Unit): ExplorationBoard",
        "fun direction(name: String, block: DirectionScope.() -> Unit)",
        "fun screen(id: String = \"main\", content: @Composable () -> Unit) // first screen declared is start",
        "fun listDetail(id: String = \"list\", detailId: String = \"detail\", list: @Composable (onOpenDetail: () -> Unit) -> Unit, detail: @Composable () -> Unit, placeholder: (@Composable () -> Unit)? = null)",
        "fun supportingPane(id: String = \"main\", supportingId: String = \"supporting\", main: @Composable (onOpenSupporting: () -> Unit) -> Unit, supporting: @Composable () -> Unit)",
        "fun overlay(id: String, screen: String? = null, content: @Composable () -> Unit) // opens on active overlay inspection",
        "@Composable fun SymbolIcon(name: String, contentDescription: String? = null, modifier: Modifier = Modifier, filled: Boolean = false, size: Dp = 24.dp, tint: Color = LocalContentColor.current)",
        "@Composable fun designPainter(relativePath: String): Painter // confined beneath .explore.kt dir, e.g. \"assets/cover.jpg\"",
        "fun bundledFontFamily(font: BundledFont, weight: Int = 400, variation: String? = null): FontFamily",
        "```",
        "",
        f"- `PaletteVariant`: `{'`, `'.join(variants)}`",
        f"- `BundledFont`: `{'`, `'.join(fonts)}` (`Doto` allowed at Display sizes only)",
        "- Primary CTA tag: `Modifier.testTag(\"ad:primary\")` (alias `AD_PRIMARY_TAG`).",
        "",
        "## Material 3 Expressive (`@OptIn(ExperimentalMaterial3ExpressiveApi::class)`)",
        "",
        "```kotlin",
        "// Theme & Motion",
        "MaterialExpressiveTheme(colorScheme = ..., motionScheme = MotionScheme.expressive(), shapes = ..., typography = ...) { ... }",
        "MaterialTheme.motionScheme.fastSpatialSpec() // defaultSpatialSpec(), slowSpatialSpec(), fastEffectsSpec()",
        "",
        "// App Bars & Toolbars (subtitle only on Flexible bars)",
        "MediumFlexibleTopAppBar(title = { Text(...) }, modifier = Modifier, subtitle = { Text(...) }, navigationIcon = {}, actions = {}, scrollBehavior = ...)",
        "LargeFlexibleTopAppBar(title = { Text(...) }, modifier = Modifier, subtitle = { Text(...) }, navigationIcon = {}, actions = {}, scrollBehavior = ...)",
        "FlexibleBottomAppBar(modifier = Modifier, horizontalArrangement = ..., content = { ... })",
        "HorizontalFloatingToolbar(expanded = true, floatingActionButton = { FloatingToolbarDefaults.StandardFloatingActionButton(...) }, colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()) { ... }",
        "VerticalFloatingToolbar(expanded = true, colors = FloatingToolbarDefaults.standardFloatingToolbarColors()) { ... }",
        "",
        "// Navigation (phone bar -> foldable/tablet rail; ModalWideNavigationRail on expandable rails)",
        "ShortNavigationBar { ShortNavigationBarItem(selected = ..., onClick = {}, icon = { ... }, label = { Text(...) }, iconPosition = NavigationItemIconPosition.Top) }",
        "WideNavigationRail(state = rememberWideNavigationRailState(), header = { ... }) { WideNavigationRailItem(railExpanded = ..., icon = { ... }, label = { Text(...) }, selected = ..., onClick = {}) }",
        "ModalWideNavigationRail(state = rememberWideNavigationRailState(), hideOnCollapse = false, header = { ... }) { ... }",
        "",
        "// Sized Buttons, Connected Toggle Groups & FABs (XS 32.dp, S 40.dp, M 56.dp, L 96.dp, XL 136.dp)",
        "Button(onClick = {}, shapes = ButtonDefaults.shapesFor(56.dp), contentPadding = ButtonDefaults.contentPaddingFor(56.dp)) { ... }",
        "FilledTonalButton(onClick = {}, shapes = ButtonDefaults.shapesFor(40.dp)) { ... }",
        "Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {",
        "  ToggleButton(checked = ..., onCheckedChange = {}, shapes = ButtonGroupDefaults.connectedLeadingButtonShapes()) { ... }",
        "  ToggleButton(checked = ..., onCheckedChange = {}, shapes = ButtonGroupDefaults.connectedMiddleButtonShapes()) { ... }",
        "  FilledTonalToggleButton(checked = ..., onCheckedChange = {}, shapes = ButtonGroupDefaults.connectedTrailingButtonShapes()) { ... }",
        "}",
        "MediumFloatingActionButton(onClick = {}) { ... } // or LargeFloatingActionButton",
        "FloatingActionButtonMenu(expanded = ..., button = { ToggleFloatingActionButton(checked = ..., onCheckedChange = {}) { ... } }) { FloatingActionButtonMenuItem(onClick = {}, text = {}, icon = {}) }",
        "",
        "// Segmented Lists, Indicators, Grouped Menus & Shapes",
        "Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {",
        "  SegmentedListItem(onClick = {}, shapes = ListItemDefaults.segmentedShapes(index = i, count = n), leadingContent = {}, supportingContent = {}, trailingContent = {}) { Text(\"...\") }",
        "}",
        "LoadingIndicator(modifier = Modifier.size(48.dp)) // or ContainedLoadingIndicator()",
        "LinearWavyProgressIndicator(progress = { 0.65f }) // or CircularWavyProgressIndicator(progress = { 0.65f })",
        "DropdownMenuPopup(expanded = ..., onDismissRequest = {}) {",
        "  DropdownMenuGroup(shapes = MenuDefaults.groupShape(index = 0, count = 2)) {",
        "    DropdownMenuItem(onClick = {}, text = { Text(...) }, shape = MenuDefaults.itemShape(0, 2).shape)",
        "  }",
        "}",
        "Modifier.clip(MaterialShapes.Cookie9Sided.toShape()) // shapes below",
        "```",
        "",
        f"Available `MaterialShapes`: `{shape_samples}`.",
        "",
        "## Wear OS Material 3 (`import androidx.wear.compose.material3.*` + `androidx.wear.compose.foundation.lazy.*`)",
        "",
        "```kotlin",
        "AppScaffold(timeText = { TimeText { timeTextCurvedText(\"10:08\") } }) { ... } // wrap once at direction/root",
        "val listState = rememberTransformingLazyColumnState()",
        "ScreenScaffold(scrollState = listState, edgeButton = { EdgeButton(onClick = {}, buttonSize = EdgeButtonSize.Small) { Text(...) } }) { contentPadding ->",
        "  TransformingLazyColumn(state = listState, contentPadding = contentPadding) { ... }",
        "}",
        "ScreenScaffold { contentPadding -> /* non-scrolling fixed watch screen */ }",
        "Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text(...) } // FilledTonalButton, OutlinedButton",
        "IconButton(onClick = {}) { ... } // or FilledIconButton(onClick = {})",
        "TitleCard(onClick = {}, title = { Text(...) }, time = { Text(...) }) { Text(...) } // or Card(onClick = {})",
        "SwitchButton(checked = ..., onCheckedChange = {}) { Text(...) }",
        "ListHeader { Text(...) }",
        "AlertDialog(visible = ..., onDismissRequest = {}, confirmButton = { AlertDialogDefaults.ConfirmButton(onClick = {}) }, dismissButton = { AlertDialogDefaults.DismissButton(onClick = {}) }, title = { Text(...) }, text = { Text(...) })",
        "ConfirmationDialog(visible = ..., onDismissRequest = {}, curvedText = { confirmationDialogCurvedText(\"Done\", ConfirmationDialogDefaults.curvedTextStyle()) }) { ... }",
        "TimeText { timeTextCurvedText(\"10:08\") } // note: AlertDialog uses `visible`, never `show`",
        "```",
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
    markdown = render_markdown()
    args.out.parent.mkdir(parents=True, exist_ok=True)
    args.out.write_text(markdown, encoding="utf-8")
    print(f"Wrote {args.out.relative_to(ROOT)} ({len(markdown.splitlines())} lines)")


if __name__ == "__main__":
    main()
