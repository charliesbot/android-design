# Compose mapping for proposals

The lookup an implementing agent uses to port an approved HTML proposal to Compose. Every Compose name here exists in `material3-inventory.txt`, the public names of the pinned material3 artifact, except a few `foundation`, `ui`, and MaterialKolor names that the test in `scripts/test_material3_inventory.py` lists on purpose.

Proposals may only use components marked `verified` in the first table. A component becomes `verified` when its pattern has been ported to Compose, rendered, and compared against the same component on `/runtime/verify/` with matching layout, color, shape, and text; then change its Status here and name the evidence in the commit. The verify page alone never marks a component verified. Exempt from the rule because every page needs them: `m3e-theme`, `p-icon`, and the token tables (type, color, icons, shapes, spacing).

Compose is written at implementation, in the app, under the app's own conventions. Use the patterns below as the API call to reach for, not as finished code.

## Components

The HTML cell is the element and the attribute values that select the pattern. An attribute not listed keeps the library default and has no Compose parameter to set.

| HTML | Compose | Status | Notes |
| --- | --- | --- | --- |
| `m3e-button variant="filled" size="extra-large"` | `Button(shapes = ButtonDefaults.shapesFor(ButtonDefaults.ExtraLargeContainerHeight), contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.ExtraLargeContainerHeight), modifier = Modifier.heightIn(min = ButtonDefaults.ExtraLargeContainerHeight))` with label `ButtonDefaults.textStyleFor(ButtonDefaults.ExtraLargeContainerHeight)` | verified | Container and content colors come from the theme roles, not overrides |
| `m3e-button size="extra-small"` | same pattern with height `ButtonDefaults.ExtraSmallContainerHeight` | unverified | |
| `m3e-button size="small"` | same pattern with height `ButtonDefaults.MinHeight` | unverified | The default size |
| `m3e-button size="medium"` | same pattern with height `ButtonDefaults.MediumContainerHeight` | unverified | |
| `m3e-button size="large"` | same pattern with height `ButtonDefaults.LargeContainerHeight` | unverified | |
| `m3e-button variant="elevated"` | `ElevatedButton` | unverified | Size pattern as above |
| `m3e-button variant="tonal"` | `FilledTonalButton` | unverified | Size pattern as above |
| `m3e-button variant="outlined"` | `OutlinedButton` | unverified | Size pattern as above |
| `m3e-button variant="text"` | `TextButton` | unverified | Size pattern as above |
| `m3e-button shape="square"` | `ButtonDefaults.squareShape` as the `shape` of `ButtonDefaults.shapes` | unverified | `rounded` is the default and is what `shapesFor` gives |
| `m3e-button` with a leading `p-icon` | `Icon(modifier = Modifier.size(ButtonDefaults.IconSize))` then `Spacer(Modifier.width(ButtonDefaults.IconSpacing))` | unverified | Use `ButtonDefaults.iconSizeFor` and `ButtonDefaults.iconSpacingFor` with the button height at other sizes |
| `m3e-button toggle variant="filled"` | `ToggleButton(checked, onCheckedChange, shapes = ToggleButtonDefaults.shapesFor(ToggleButtonDefaults.MinHeight))` | unverified | Selected state shows the checked shape |
| `m3e-button toggle variant="tonal"` | `FilledTonalToggleButton` | unverified | |
| `m3e-button toggle variant="elevated"` | `ElevatedToggleButton` | unverified | |
| `m3e-button toggle variant="outlined"` | `OutlinedToggleButton` | unverified | |
| `m3e-button-group variant="standard"` | `ButtonGroup` | unverified | `multi` and `size` are not parameters: set the size on each button |
| `m3e-button-group variant="connected"` | `Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween))` of toggle buttons with `ButtonGroupDefaults.connectedLeadingButtonShapes`, `ButtonGroupDefaults.connectedMiddleButtonShapes`, `ButtonGroupDefaults.connectedTrailingButtonShapes` | unverified | One color style for the whole group |
| `m3e-icon-button variant="standard"` | `IconButton(onClick, modifier = Modifier.size(48.dp))` with `Icon` inside | verified | The hero search button of the blind port; 48dp keeps the touch target |
| `m3e-icon-button variant="filled"` | `FilledIconButton` | unverified | |
| `m3e-icon-button variant="tonal"` | `FilledTonalIconButton` | unverified | |
| `m3e-icon-button variant="outlined"` | `OutlinedIconButton` | unverified | |
| `m3e-icon-button variant="elevated"` | none | unverified | No Compose counterpart: do not use |
| `m3e-icon-button toggle` | `IconToggleButton`, or `FilledIconToggleButton`, `FilledTonalIconToggleButton`, `OutlinedIconToggleButton` for the matching variant | unverified | Outlined icon when off, filled icon when on |
| `m3e-icon-button size="extra-small"` | `IconButtonDefaults.extraSmallContainerSize` and `IconButtonDefaults.extraSmallIconSize` | unverified | Also `small`, `medium`, `large`, `extraLarge` with the same suffixes |
| `m3e-icon-button width="narrow"` | `IconButtonDefaults.IconButtonWidthOption.Narrow` | unverified | `default` is `IconButtonDefaults.IconButtonWidthOption.Uniform`, `wide` is `IconButtonDefaults.IconButtonWidthOption.Wide` |
| `m3e-split-button variant="filled"` | `SplitButtonLayout(leadingButton = { SplitButtonDefaults.LeadingButton(...) }, trailingButton = { SplitButtonDefaults.TrailingButton(...) })` | unverified | |
| `m3e-split-button variant="tonal"` | `SplitButtonDefaults.TonalLeadingButton` and `SplitButtonDefaults.TonalTrailingButton` | unverified | |
| `m3e-split-button variant="elevated"` | `SplitButtonDefaults.ElevatedLeadingButton` and `SplitButtonDefaults.ElevatedTrailingButton` | unverified | |
| `m3e-split-button variant="outlined"` | `SplitButtonDefaults.OutlinedLeadingButton` and `SplitButtonDefaults.OutlinedTrailingButton` | unverified | |
| `m3e-fab size="medium"` | `FloatingActionButton` | unverified | |
| `m3e-fab size="small"` | `SmallFloatingActionButton` | unverified | |
| `m3e-fab size="large"` | `LargeFloatingActionButton` | unverified | |
| `m3e-fab extended` | `ExtendedFloatingActionButton` | unverified | `MediumExtendedFloatingActionButton` and `LargeExtendedFloatingActionButton` at the medium and large sizes |
| `m3e-fab variant="primary-container"` | `containerColor = MaterialTheme.colorScheme.primaryContainer`, `contentColor = MaterialTheme.colorScheme.onPrimaryContainer` | unverified | The default |
| `m3e-fab variant="primary"` | `containerColor = MaterialTheme.colorScheme.primary`, `contentColor = MaterialTheme.colorScheme.onPrimary` | unverified | |
| `m3e-fab variant="secondary-container"` | `containerColor = MaterialTheme.colorScheme.secondaryContainer`, `contentColor = MaterialTheme.colorScheme.onSecondaryContainer` | unverified | |
| `m3e-fab variant="secondary"` | `containerColor = MaterialTheme.colorScheme.secondary`, `contentColor = MaterialTheme.colorScheme.onSecondary` | unverified | |
| `m3e-fab variant="tertiary-container"` | `containerColor = MaterialTheme.colorScheme.tertiaryContainer`, `contentColor = MaterialTheme.colorScheme.onTertiaryContainer` | unverified | |
| `m3e-fab variant="tertiary"` | `containerColor = MaterialTheme.colorScheme.tertiary`, `contentColor = MaterialTheme.colorScheme.onTertiary` | unverified | |
| `m3e-fab variant="surface"` | `containerColor = MaterialTheme.colorScheme.surfaceContainerHigh`, `contentColor = MaterialTheme.colorScheme.primary` | unverified | |
| `m3e-fab-menu` | `FloatingActionButtonMenu(expanded, button = { ToggleFloatingActionButton(...) })` | unverified | Only from a regular FAB |
| `m3e-fab-menu-item` | `FloatingActionButtonMenuItem(onClick, text, icon)` | unverified | |
| `m3e-fab-menu-trigger` | `ToggleFloatingActionButton` | unverified | The button that opens the menu |
| `m3e-card variant="filled" actionable` | `Card(onClick = ..., shape = MaterialTheme.shapes.large)` | verified | Corner is the proposal's declared token; the blind port used `large` |
| `m3e-card variant="filled"` | `Card` | unverified | Not actionable: no `onClick` |
| `m3e-card variant="elevated"` | `ElevatedCard` | unverified | |
| `m3e-card variant="outlined"` | `OutlinedCard` | unverified | |
| `m3e-card orientation="horizontal"` | `Row` inside the card content | unverified | `vertical` is a `Column` |
| `m3e-list variant="standard"` and `m3e-list-item` | `ListItem` per item | unverified | Slots: default is `headlineContent`, `overline` is `overlineContent`, `supporting-text` is `supportingContent`, `leading` is `leadingContent`, `trailing` is `trailingContent` |
| `m3e-list variant="segmented"` | `SegmentedListItem` per item with `ListItemDefaults.segmentedShapes` and `ListItemDefaults.segmentedColors` | unverified | Gap `ListItemDefaults.SegmentedGap` |
| `m3e-list-item` | see `m3e-list` | unverified | |
| `m3e-action-list` and `m3e-list-action` | `ListItem` or `SegmentedListItem` with `onClick` | unverified | |
| `m3e-selection-list` and `m3e-list-option` | `SegmentedListItem` with `selected` | unverified | One selection mode at a time |
| `m3e-expandable-list-item` | none | unverified | No stock composable: do not use |
| `m3e-chip` | none | unverified | Non-interactive chip has no stock composable: do not use |
| `m3e-chip-set` | `Row(horizontalArrangement = FilterChipDefaults.horizontalArrangement)` | unverified | Scrolls or wraps in one row |
| `m3e-assist-chip variant="outlined"` | `AssistChip` | unverified | |
| `m3e-assist-chip variant="elevated"` | `ElevatedAssistChip` | unverified | |
| `m3e-filter-chip variant="outlined"` | `FilterChip(selected, onClick, label)` | unverified | |
| `m3e-filter-chip variant="elevated"` | `ElevatedFilterChip` | unverified | |
| `m3e-filter-chip-set` | `Row(horizontalArrangement = FilterChipDefaults.horizontalArrangement)` of `FilterChip` | unverified | `multi` selects more than one |
| `m3e-input-chip` | `InputChip` | unverified | `removable` is the `trailingIcon` |
| `m3e-input-chip-set` | `Row(horizontalArrangement = InputChipDefaults.horizontalArrangement)` of `InputChip` | unverified | |
| `m3e-suggestion-chip variant="outlined"` | `SuggestionChip` | unverified | |
| `m3e-suggestion-chip variant="elevated"` | `ElevatedSuggestionChip` | unverified | |
| `m3e-toolbar` | `HorizontalFloatingToolbar(expanded = true)` | unverified | Only inside `p-floating-toolbar`; see the shims table |
| `m3e-toolbar vertical` | `VerticalFloatingToolbar` | unverified | |
| `m3e-toolbar variant="standard"` | `colors = FloatingToolbarDefaults.standardFloatingToolbarColors()` | unverified | |
| `m3e-toolbar variant="vibrant"` | `colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()` | unverified | |
| `m3e-nav-rail mode="compact"` | `WideNavigationRail(state = rememberWideNavigationRailState())` | verified | |
| `m3e-nav-item` inside `m3e-nav-rail mode="compact"` | `WideNavigationRailItem(railExpanded = false, selected, onClick, icon, label)` | verified | |
| `m3e-nav-rail mode="expanded"` | `WideNavigationRail` with `rememberWideNavigationRailState` starting at `WideNavigationRailValue.Expanded` | unverified | |
| `m3e-nav-item` inside `m3e-nav-rail mode="expanded"` | `WideNavigationRailItem(railExpanded = true, ...)` | unverified | |
| `m3e-nav-rail-toggle` | `IconButton` that calls `WideNavigationRailState.toggle` | unverified | |
| `m3e-nav-bar` | `ShortNavigationBar` | unverified | 3 to 5 items |
| `m3e-nav-item` inside `m3e-nav-bar` | `ShortNavigationBarItem(selected, onClick, icon, label)` | unverified | |
| `m3e-nav-item orientation="vertical"` | `iconPosition = NavigationItemIconPosition.Top` | unverified | `horizontal` is `NavigationItemIconPosition.Start` |
| `m3e-app-bar size="small"` | `TopAppBar` | unverified | Slots: `leading` is `navigationIcon`, `title` is `title`, `trailing` is `actions` |
| `m3e-app-bar size="small" centered` | `CenterAlignedTopAppBar` | unverified | |
| `m3e-app-bar size="medium"` | `MediumFlexibleTopAppBar` | unverified | |
| `m3e-app-bar size="large"` | `LargeFlexibleTopAppBar` | unverified | |
| `m3e-loading-indicator variant="uncontained"` | `LoadingIndicator` | unverified | |
| `m3e-loading-indicator variant="contained"` | `ContainedLoadingIndicator` | unverified | |
| `m3e-circular-progress-indicator variant="flat"` | `CircularProgressIndicator` | unverified | |
| `m3e-circular-progress-indicator variant="wavy"` | `CircularWavyProgressIndicator` | unverified | |
| `m3e-linear-progress-indicator variant="flat"` | `LinearProgressIndicator` | unverified | |
| `m3e-linear-progress-indicator variant="wavy"` | `LinearWavyProgressIndicator` | unverified | |
| `m3e-switch` | `Switch` | unverified | `icons` is the `thumbContent` slot |
| `m3e-slider` | `Slider(value, onValueChange)` | unverified | Track and thumb through `SliderDefaults.Track` and `SliderDefaults.Thumb` |
| `m3e-slider` with two `m3e-slider-thumb` | `RangeSlider` | unverified | |
| `m3e-slider-thumb` | see `m3e-slider` | unverified | |
| `m3e-shape name="sunny"` | `Modifier.clip(MaterialShapes.Sunny.toShape())` | unverified | Name table below |
| `p-floating-toolbar` | see the shims table | verified | The toggle buttons inside are the blind port's `HorizontalFloatingToolbar` with `ToggleButton` |
| `m3e-button toggle` inside `p-floating-toolbar` | `ToggleButton(checked, onCheckedChange, colors = ToggleButtonDefaults.colors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurfaceVariant, checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer, checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer))` | verified | Selected is tonal, others text; 48dp minimum height with `Modifier.heightIn(min = 48.dp)`; icon at `ToggleButtonDefaults.IconSize` |
| `m3e-theme color variant scheme` | `MaterialExpressiveTheme(colorScheme = rememberDynamicColorScheme(seedColor, isDark, style = PaletteStyle.TonalSpot, specVersion = ColorSpec.SpecVersion.SPEC_2025), motionScheme = MotionScheme.expressive())` | unverified | Required by the page contract. See the color table for `variant` and `scheme` |
| `p-icon` | `Icon(Icons.Rounded.Home, contentDescription = ...)` | unverified | `label` is the `contentDescription`; without it pass `null`. See the icons table |

### Shape names for `m3e-shape`

| HTML `name` | Compose | Notes |
| --- | --- | --- |
| `4-leaf-clover` | `MaterialShapes.Clover4Leaf` | |
| `8-leaf-clover` | `MaterialShapes.Clover8Leaf` | |
| `4-sided-cookie` | `MaterialShapes.Cookie4Sided` | |
| `6-sided-cookie` | `MaterialShapes.Cookie6Sided` | |
| `7-sided-cookie` | `MaterialShapes.Cookie7Sided` | |
| `9-sided-cookie` | `MaterialShapes.Cookie9Sided` | |
| `12-sided-cookie` | `MaterialShapes.Cookie12Sided` | |
| `arch` | `MaterialShapes.Arch` | |
| `arrow` | `MaterialShapes.Arrow` | |
| `boom` | `MaterialShapes.Boom` | |
| `bun` | `MaterialShapes.Bun` | |
| `burst` | `MaterialShapes.Burst` | |
| `circle` | `MaterialShapes.Circle` | |
| `diamond` | `MaterialShapes.Diamond` | |
| `fan` | `MaterialShapes.Fan` | |
| `flower` | `MaterialShapes.Flower` | |
| `gem` | `MaterialShapes.Gem` | |
| `ghost-ish` | `MaterialShapes.Ghostish` | |
| `heart` | `MaterialShapes.Heart` | |
| `hexagon` | none | No Compose counterpart: do not use |
| `oval` | `MaterialShapes.Oval` | |
| `pentagon` | `MaterialShapes.Pentagon` | |
| `pill` | `MaterialShapes.Pill` | |
| `pixel-circle` | `MaterialShapes.PixelCircle` | |
| `pixel-triangle` | `MaterialShapes.PixelTriangle` | |
| `puffy` | `MaterialShapes.Puffy` | |
| `puffy-diamond` | `MaterialShapes.PuffyDiamond` | |
| `semicircle` | `MaterialShapes.SemiCircle` | |
| `slanted` | `MaterialShapes.Slanted` | |
| `soft-boom` | `MaterialShapes.SoftBoom` | |
| `soft-burst` | `MaterialShapes.SoftBurst` | |
| `square` | `MaterialShapes.Square` | |
| `sunny` | `MaterialShapes.Sunny` | |
| `triangle` | `MaterialShapes.Triangle` | |
| `very-sunny` | `MaterialShapes.VerySunny` | |

`MaterialShapes.ClamShell` has no `m3e-shape` name.

## Type roles

One class per Material role, plus the emphasized role. Sizes are the generated values in `runtime-src/type-scale.json`, so each class equals its `MaterialTheme.typography` role.

| HTML | Compose | Values |
| --- | --- | --- |
| `.t-display-large` | `MaterialTheme.typography.displayLarge` | 57sp / 64sp, weight 400, tracking -0.2sp |
| `.t-display-large-em` | `MaterialTheme.typography.displayLargeEmphasized` | 57sp / 64sp, weight 500, tracking 0sp |
| `.t-display-medium` | `MaterialTheme.typography.displayMedium` | 45sp / 52sp, weight 400, tracking 0sp |
| `.t-display-medium-em` | `MaterialTheme.typography.displayMediumEmphasized` | 45sp / 52sp, weight 500, tracking 0sp |
| `.t-display-small` | `MaterialTheme.typography.displaySmall` | 36sp / 44sp, weight 400, tracking 0sp |
| `.t-display-small-em` | `MaterialTheme.typography.displaySmallEmphasized` | 36sp / 44sp, weight 500, tracking 0sp |
| `.t-headline-large` | `MaterialTheme.typography.headlineLarge` | 32sp / 40sp, weight 400, tracking 0sp |
| `.t-headline-large-em` | `MaterialTheme.typography.headlineLargeEmphasized` | 32sp / 40sp, weight 500, tracking 0sp |
| `.t-headline-medium` | `MaterialTheme.typography.headlineMedium` | 28sp / 36sp, weight 400, tracking 0sp |
| `.t-headline-medium-em` | `MaterialTheme.typography.headlineMediumEmphasized` | 28sp / 36sp, weight 500, tracking 0sp |
| `.t-headline-small` | `MaterialTheme.typography.headlineSmall` | 24sp / 32sp, weight 400, tracking 0sp |
| `.t-headline-small-em` | `MaterialTheme.typography.headlineSmallEmphasized` | 24sp / 32sp, weight 500, tracking 0sp |
| `.t-title-large` | `MaterialTheme.typography.titleLarge` | 22sp / 28sp, weight 400, tracking 0sp |
| `.t-title-large-em` | `MaterialTheme.typography.titleLargeEmphasized` | 22sp / 28sp, weight 500, tracking 0sp |
| `.t-title-medium` | `MaterialTheme.typography.titleMedium` | 16sp / 24sp, weight 500, tracking 0.2sp |
| `.t-title-medium-em` | `MaterialTheme.typography.titleMediumEmphasized` | 16sp / 24sp, weight 700, tracking 0.15sp |
| `.t-title-small` | `MaterialTheme.typography.titleSmall` | 14sp / 20sp, weight 500, tracking 0.1sp |
| `.t-title-small-em` | `MaterialTheme.typography.titleSmallEmphasized` | 14sp / 20sp, weight 700, tracking 0.1sp |
| `.t-body-large` | `MaterialTheme.typography.bodyLarge` | 16sp / 24sp, weight 400, tracking 0.5sp |
| `.t-body-large-em` | `MaterialTheme.typography.bodyLargeEmphasized` | 16sp / 24sp, weight 500, tracking 0.15sp |
| `.t-body-medium` | `MaterialTheme.typography.bodyMedium` | 14sp / 20sp, weight 400, tracking 0.2sp |
| `.t-body-medium-em` | `MaterialTheme.typography.bodyMediumEmphasized` | 14sp / 20sp, weight 500, tracking 0.25sp |
| `.t-body-small` | `MaterialTheme.typography.bodySmall` | 12sp / 16sp, weight 400, tracking 0.4sp |
| `.t-body-small-em` | `MaterialTheme.typography.bodySmallEmphasized` | 12sp / 16sp, weight 500, tracking 0.4sp |
| `.t-label-large` | `MaterialTheme.typography.labelLarge` | 14sp / 20sp, weight 500, tracking 0.1sp |
| `.t-label-large-em` | `MaterialTheme.typography.labelLargeEmphasized` | 14sp / 20sp, weight 700, tracking 0.1sp |
| `.t-label-medium` | `MaterialTheme.typography.labelMedium` | 12sp / 16sp, weight 500, tracking 0.5sp |
| `.t-label-medium-em` | `MaterialTheme.typography.labelMediumEmphasized` | 12sp / 16sp, weight 700, tracking 0.5sp |
| `.t-label-small` | `MaterialTheme.typography.labelSmall` | 11sp / 16sp, weight 500, tracking 0.5sp |
| `.t-label-small-em` | `MaterialTheme.typography.labelSmallEmphasized` | 11sp / 16sp, weight 700, tracking 0.5sp |
| `.voice-<name>` | `TextStyle` with `fontFamily = FontFamily(Font(R.font.google_sans_flex, variationSettings = FontVariation.Settings(FontVariation.weight(w), FontVariation.width(wd), FontVariation.opticalSizing(os.sp))))` | Build it once per declared voice from `font-variation-settings` axes (`wght`, `wdth`, `opsz`); start from the role the proposal extends with `.copy` and take its size and line height |

## Color

Every role the runtime theme emits. `--md-sys-color-<role>` is the kebab-case of the Compose property.

| HTML | Compose | Notes |
| --- | --- | --- |
| `--md-sys-color-background` | `MaterialTheme.colorScheme.background` |  |
| `--md-sys-color-on-background` | `MaterialTheme.colorScheme.onBackground` |  |
| `--md-sys-color-surface` | `MaterialTheme.colorScheme.surface` |  |
| `--md-sys-color-surface-dim` | `MaterialTheme.colorScheme.surfaceDim` |  |
| `--md-sys-color-surface-bright` | `MaterialTheme.colorScheme.surfaceBright` |  |
| `--md-sys-color-surface-container-lowest` | `MaterialTheme.colorScheme.surfaceContainerLowest` |  |
| `--md-sys-color-surface-container-low` | `MaterialTheme.colorScheme.surfaceContainerLow` |  |
| `--md-sys-color-surface-container` | `MaterialTheme.colorScheme.surfaceContainer` |  |
| `--md-sys-color-surface-container-high` | `MaterialTheme.colorScheme.surfaceContainerHigh` |  |
| `--md-sys-color-surface-container-highest` | `MaterialTheme.colorScheme.surfaceContainerHighest` |  |
| `--md-sys-color-on-surface` | `MaterialTheme.colorScheme.onSurface` |  |
| `--md-sys-color-surface-variant` | `MaterialTheme.colorScheme.surfaceVariant` |  |
| `--md-sys-color-on-surface-variant` | `MaterialTheme.colorScheme.onSurfaceVariant` |  |
| `--md-sys-color-outline` | `MaterialTheme.colorScheme.outline` |  |
| `--md-sys-color-outline-variant` | `MaterialTheme.colorScheme.outlineVariant` |  |
| `--md-sys-color-inverse-surface` | `MaterialTheme.colorScheme.inverseSurface` |  |
| `--md-sys-color-inverse-on-surface` | `MaterialTheme.colorScheme.inverseOnSurface` |  |
| `--md-sys-color-primary` | `MaterialTheme.colorScheme.primary` |  |
| `--md-sys-color-primary-dim` | none | No `ColorScheme` member in this material3 version: do not use |
| `--md-sys-color-on-primary` | `MaterialTheme.colorScheme.onPrimary` |  |
| `--md-sys-color-primary-container` | `MaterialTheme.colorScheme.primaryContainer` |  |
| `--md-sys-color-on-primary-container` | `MaterialTheme.colorScheme.onPrimaryContainer` |  |
| `--md-sys-color-primary-fixed` | `MaterialTheme.colorScheme.primaryFixed` |  |
| `--md-sys-color-primary-fixed-dim` | none | No `ColorScheme` member in this material3 version: do not use |
| `--md-sys-color-on-primary-fixed` | `MaterialTheme.colorScheme.onPrimaryFixed` |  |
| `--md-sys-color-on-primary-fixed-variant` | `MaterialTheme.colorScheme.onPrimaryFixedVariant` |  |
| `--md-sys-color-inverse-primary` | `MaterialTheme.colorScheme.inversePrimary` |  |
| `--md-sys-color-secondary` | `MaterialTheme.colorScheme.secondary` |  |
| `--md-sys-color-secondary-dim` | none | No `ColorScheme` member in this material3 version: do not use |
| `--md-sys-color-on-secondary` | `MaterialTheme.colorScheme.onSecondary` |  |
| `--md-sys-color-secondary-container` | `MaterialTheme.colorScheme.secondaryContainer` |  |
| `--md-sys-color-on-secondary-container` | `MaterialTheme.colorScheme.onSecondaryContainer` |  |
| `--md-sys-color-secondary-fixed` | `MaterialTheme.colorScheme.secondaryFixed` |  |
| `--md-sys-color-secondary-fixed-dim` | none | No `ColorScheme` member in this material3 version: do not use |
| `--md-sys-color-on-secondary-fixed` | `MaterialTheme.colorScheme.onSecondaryFixed` |  |
| `--md-sys-color-on-secondary-fixed-variant` | `MaterialTheme.colorScheme.onSecondaryFixedVariant` |  |
| `--md-sys-color-tertiary` | `MaterialTheme.colorScheme.tertiary` |  |
| `--md-sys-color-tertiary-dim` | none | No `ColorScheme` member in this material3 version: do not use |
| `--md-sys-color-on-tertiary` | `MaterialTheme.colorScheme.onTertiary` |  |
| `--md-sys-color-tertiary-container` | `MaterialTheme.colorScheme.tertiaryContainer` |  |
| `--md-sys-color-on-tertiary-container` | `MaterialTheme.colorScheme.onTertiaryContainer` |  |
| `--md-sys-color-tertiary-fixed` | `MaterialTheme.colorScheme.tertiaryFixed` |  |
| `--md-sys-color-tertiary-fixed-dim` | none | No `ColorScheme` member in this material3 version: do not use |
| `--md-sys-color-on-tertiary-fixed` | `MaterialTheme.colorScheme.onTertiaryFixed` |  |
| `--md-sys-color-on-tertiary-fixed-variant` | `MaterialTheme.colorScheme.onTertiaryFixedVariant` |  |
| `--md-sys-color-error` | `MaterialTheme.colorScheme.error` |  |
| `--md-sys-color-error-dim` | none | No `ColorScheme` member in this material3 version: do not use |
| `--md-sys-color-on-error` | `MaterialTheme.colorScheme.onError` |  |
| `--md-sys-color-error-container` | `MaterialTheme.colorScheme.errorContainer` |  |
| `--md-sys-color-on-error-container` | `MaterialTheme.colorScheme.onErrorContainer` |  |
| `--md-sys-color-shadow` | `Color.Black` | Compose has no shadow role; elevation shadows are black |
| `--md-sys-color-scrim` | `MaterialTheme.colorScheme.scrim` |  |
| `--md-sys-color-surface-tint` | `MaterialTheme.colorScheme.surfaceTint` |  |
| `--static-<name>` | A named `Color(0xFF......)` constant in a `private val`, declared once | A color that is not a role (dish or category color); the hex is the proposal's `/* static colors */` block |
| `m3e-theme variant="tonal-spot"` | `PaletteStyle.TonalSpot` | |
| `m3e-theme variant="neutral"` | `PaletteStyle.Neutral` | |
| `m3e-theme variant="vibrant"` | `PaletteStyle.Vibrant` | |
| `m3e-theme variant="expressive"` | `PaletteStyle.Expressive` | |
| `m3e-theme variant="monochrome"` | `PaletteStyle.Monochrome` | |
| `m3e-theme variant="fidelity"` | `PaletteStyle.Fidelity` | |
| `m3e-theme variant="content"` | `PaletteStyle.Content` | |
| `m3e-theme variant="rainbow"` | `PaletteStyle.Rainbow` | |
| `m3e-theme variant="fruit-salad"` | `PaletteStyle.FruitSalad` | |
| `m3e-theme scheme="light"` | `isDark = false` | `dark` is `true`; `auto` is `isSystemInDarkTheme()` |
| `m3e-theme color="#RRGGBB"` | `seedColor = Color(0xFFRRGGBB)` | |

The theme element is the single color source: `rememberDynamicColorScheme(seedColor, isDark, style, specVersion = ColorSpec.SpecVersion.SPEC_2025)` inside `MaterialExpressiveTheme`, as in the first table. The 2025 spec matches the library's generator.

## Icons

Every name in `runtime-src/icons.json`. Icons marked `extended` need the `material-icons-extended` dependency; `core` ones ship with `material-icons-core`. The five directional icons use `Icons.AutoMirrored.Rounded`, which mirrors in right-to-left layouts. Compose's icon set has no unfilled and filled pair: `p-icon` with or without `filled` maps to the same `Icons.Rounded` vector, so the unfilled look needs a Material Symbols vector drawable at implementation.

| HTML `name` | Compose | Artifact |
| --- | --- | --- |
| `home` | `Icons.Rounded.Home` | core |
| `search` | `Icons.Rounded.Search` | core |
| `favorite` | `Icons.Rounded.Favorite` | core |
| `settings` | `Icons.Rounded.Settings` | core |
| `add` | `Icons.Rounded.Add` | core |
| `close` | `Icons.Rounded.Close` | core |
| `arrow_back` | `Icons.AutoMirrored.Rounded.ArrowBack` | core |
| `more_vert` | `Icons.Rounded.MoreVert` | core |
| `menu` | `Icons.Rounded.Menu` | core |
| `edit` | `Icons.Rounded.Edit` | core |
| `delete` | `Icons.Rounded.Delete` | core |
| `share` | `Icons.Rounded.Share` | core |
| `check` | `Icons.Rounded.Check` | core |
| `calendar_month` | `Icons.Rounded.CalendarMonth` | extended |
| `schedule` | `Icons.Rounded.Schedule` | extended |
| `person` | `Icons.Rounded.Person` | core |
| `notifications` | `Icons.Rounded.Notifications` | core |
| `star` | `Icons.Rounded.Star` | core |
| `bookmark` | `Icons.Rounded.Bookmark` | extended |
| `play_arrow` | `Icons.Rounded.PlayArrow` | core |
| `pause` | `Icons.Rounded.Pause` | extended |
| `skip_next` | `Icons.Rounded.SkipNext` | extended |
| `skip_previous` | `Icons.Rounded.SkipPrevious` | extended |
| `shuffle` | `Icons.Rounded.Shuffle` | extended |
| `repeat` | `Icons.Rounded.Repeat` | extended |
| `filter_list` | `Icons.Rounded.FilterList` | extended |
| `sort` | `Icons.AutoMirrored.Rounded.Sort` | extended |
| `info` | `Icons.Rounded.Info` | core |
| `help` | `Icons.AutoMirrored.Rounded.Help` | extended |
| `map` | `Icons.Rounded.Map` | extended |
| `photo_camera` | `Icons.Rounded.PhotoCamera` | extended |
| `image` | `Icons.Rounded.Image` | extended |
| `chat` | `Icons.AutoMirrored.Rounded.Chat` | extended |
| `mail` | `Icons.Rounded.Mail` | extended |
| `call` | `Icons.Rounded.Call` | core |
| `shopping_cart` | `Icons.Rounded.ShoppingCart` | core |
| `restaurant` | `Icons.Rounded.Restaurant` | extended |
| `local_dining` | `Icons.Rounded.LocalDining` | extended |
| `timer` | `Icons.Rounded.Timer` | extended |
| `list` | `Icons.AutoMirrored.Rounded.List` | core |
| `grid_view` | `Icons.Rounded.GridView` | extended |
| `tune` | `Icons.Rounded.Tune` | extended |
| `refresh` | `Icons.Rounded.Refresh` | core |
| `download` | `Icons.Rounded.Download` | extended |
| `upload` | `Icons.Rounded.Upload` | extended |
| `lock` | `Icons.Rounded.Lock` | core |
| `visibility` | `Icons.Rounded.Visibility` | extended |
| `location_on` | `Icons.Rounded.LocationOn` | core |
| `explore` | `Icons.Rounded.Explore` | extended |
| `music_note` | `Icons.Rounded.MusicNote` | extended |

## Shapes and spacing

The corner scale in px (which equal dp). The runtime does not define `--md-sys-shape-corner-*` variables; write the px value, which `@m3e/web` uses as its default for the same token.

| HTML | Compose | Notes |
| --- | --- | --- |
| `0` | `RectangleShape` | none |
| `4px` | `MaterialTheme.shapes.extraSmall` | |
| `8px` | `MaterialTheme.shapes.small` | |
| `12px` | `MaterialTheme.shapes.medium` | |
| `16px` | `MaterialTheme.shapes.large` | |
| `20px` | `MaterialTheme.shapes.largeIncreased` | |
| `28px` | `MaterialTheme.shapes.extraLarge` | |
| `32px` | `MaterialTheme.shapes.extraLargeIncreased` | |
| `48px` | `MaterialTheme.shapes.extraExtraLarge` | |
| `9999px` | `CircleShape` | Fully rounded |
| Only some corners round, as in `border-radius: 0 0 28px 28px` | `MaterialTheme.shapes.extraLarge.copy(topStart = ZeroCornerSize, topEnd = ZeroCornerSize)` | Zero the corners that are 0 |
| Expressive shape from the library | `MaterialShapes.Circle.toShape()` | `m3e-shape` table above |
| `4px` or `8px` gap | `Arrangement.spacedBy(8.dp)` or `Modifier.padding(8.dp)` | The 8dp scale: 4, 8, 16, 24, 32, 48, 64 map to the same dp values; 4dp is for tight pairs only |
| `max-width: 1200px` content column | `Modifier.widthIn(max = 1200.dp)` | |

## Shims and layout

| HTML | Compose | Notes |
| --- | --- | --- |
| `@media (min-width: 840px)` | `BoxWithConstraints` with `maxWidth >= 840.dp` | Compact below, expanded at 840dp and up |
| `p-floating-toolbar` | `HorizontalFloatingToolbar(expanded = true, modifier = Modifier.align(Alignment.BottomCenter).offset(y = -FloatingToolbarDefaults.ScreenOffset), colors = FloatingToolbarDefaults.standardFloatingToolbarColors())` | The 16px lift is `FloatingToolbarDefaults.ScreenOffset`; the navigation bar inset comes from `--p-inset-bottom` below |
| `--p-inset-top` | `WindowInsets.statusBars` | Or `Modifier.statusBarsPadding()` |
| `--p-inset-bottom` | `WindowInsets.navigationBars` | Or `Modifier.navigationBarsPadding()` |
| `data-primary` | the one prominent action of the screen | Not a Compose API: carry the emphasis, not an attribute |
