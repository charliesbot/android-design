# Compose mapping for proposals

The lookup an implementing agent uses to port an approved HTML proposal to Compose. Every Compose name here exists in `material3-inventory.txt`, the public names of the pinned material3 artifact, except a few `foundation`, `ui`, and MaterialKolor names that the test in `scripts/test_material3_inventory.py` lists on purpose.

Proposals may only use components marked `verified` in the first table. A component becomes `verified` when its pattern has been ported to Compose, rendered, and compared against the same component on `/runtime/verify/`, and the two match in layout, color, shape, and text closely enough that the approved design survives the port. Proposals aim to land extremely close to the shipped screen, not pixel-perfect. A small difference between the two libraries is acceptable when it cannot break an approved design (a few dp, a weight or tracking nuance); record it in the row's Notes. A difference that can break a design (Compose noticeably larger, text that would no longer fit) blocks verification. With Google Sans Flex, headline-size text and the large and extra-large buttons render a few dp taller in Compose than in the proposal: the font sets a minimum line height above the type tokens (measured on device). Display-size text does the same, by more: on a Pixel 10 Pro XL emulator `displayLarge` (64sp token line height) rendered about 71dp tall, the font's natural line height of about 1.25em (a measurement, not a Google rule). Leave vertical room around headline and display text, and do not pack a display line tightly against its neighbors. The bundled brand faces were measured the same way (Compose `TextStyle` from the M3 `bodyLarge` with each token's size and line height, weight set through `FontVariation`, Pixel_10_Pro_XL emulator at 3.0 density, plus preview renders at 160 dpi; measurements, not Google rules): Fraunces, Bricolage Grotesque, and Big Shoulders Display at 36/44 and 32/40 match the token line height to within 1dp, and Google Sans Code at 16/24 and 32/40 does too. Doto at 45/52 renders 54dp tall (2dp taller than the token) on device and in previews. Weights 400 and 700 render for every face. Then change its Status here and name the evidence in the commit. The verify page alone never marks a component verified. Exempt from the rule because every page needs them: `m3e-theme`, `p-icon`, and the token tables (type, color, icons, shapes, spacing).

Compose is written at implementation, in the app, under the app's own conventions. Use the patterns below as the API call to reach for, not as finished code.

## Components

The HTML cell is the element and the attribute values that select the pattern. An attribute not listed keeps the library default and has no Compose parameter to set.

| HTML | Compose | Status | Notes |
| --- | --- | --- | --- |
| `m3e-button variant="filled" size="extra-large"` | `Button(shapes = ButtonDefaults.shapesFor(ButtonDefaults.ExtraLargeContainerHeight), contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.ExtraLargeContainerHeight), modifier = Modifier.heightIn(min = ButtonDefaults.ExtraLargeContainerHeight))` with label `ButtonDefaults.textStyleFor(ButtonDefaults.ExtraLargeContainerHeight)` | verified | Container and content colors come from the theme roles, not overrides. With Google Sans Flex, Compose renders 142dp tall against 136 (see the line-height note above); width and color match. |
| `m3e-button size="extra-small"` | same pattern with height `ButtonDefaults.ExtraSmallContainerHeight` | unverified | |
| `m3e-button size="small"` | same pattern with height `ButtonDefaults.MinHeight` | verified | The default size, filled as the starter ships it. Compose pads 16dp a side (Google's recommended value), `@m3e/web` 20px, so Compose is about 9dp narrower; harmless. |
| `m3e-button size="medium"` | same pattern with height `ButtonDefaults.MediumContainerHeight` | verified | Compose labels use title medium (weight 500, tracking 0.2), the proposal 400 and 0.5; harmless. |
| `m3e-button size="large"` | same pattern with height `ButtonDefaults.LargeContainerHeight` | verified | With Google Sans Flex, Compose renders about 99dp tall against 96. |
| `m3e-button variant="elevated"` | `ElevatedButton` | unverified | Size pattern as above |
| `m3e-button variant="tonal"` | `FilledTonalButton` | verified | Size pattern as above. Verified at small, medium, large, and extra-large, with the same notes as filled. |
| `m3e-button variant="outlined"` | `OutlinedButton` | unverified | Size pattern as above |
| `m3e-button variant="text"` | `TextButton` | unverified | Size pattern as above |
| `m3e-button shape="square"` | `ButtonDefaults.squareShape` as the `shape` of `ButtonDefaults.shapes` | unverified | `rounded` is the default and is what `shapesFor` gives |
| `m3e-button` with a leading `p-icon` | `Icon(modifier = Modifier.size(ButtonDefaults.IconSize))` then `Spacer(Modifier.width(ButtonDefaults.IconSpacing))` | verified | Use `ButtonDefaults.iconSizeFor` and `ButtonDefaults.iconSpacingFor` with the button height at other sizes. Icon sizes 20, 24, 32, 40 and gaps 8, 8, 12, 16 match at small to extra-large. |
| `m3e-button toggle variant="filled"` | `ToggleButton(checked, onCheckedChange, shapes = ToggleButtonDefaults.shapesFor(ToggleButtonDefaults.MinHeight))` | verified | Selected state shows the checked shape (12dp corners, unselected fully round). Colors, height, and label size match. Compose pads 16dp a side, `@m3e/web` 20px, so Compose is about 9dp narrower; harmless. |
| `m3e-button toggle variant="tonal"` | `FilledTonalToggleButton` | verified | Same notes as filled. Unselected is the secondary container, selected the secondary color; both libraries agree. |
| `m3e-button toggle variant="elevated"` | `ElevatedToggleButton` | unverified | |
| `m3e-button toggle variant="outlined"` | `OutlinedToggleButton` | unverified | |
| `m3e-button-group variant="standard"` | `ButtonGroup` | unverified | `multi` and `size` are not parameters: set the size on each button |
| `m3e-button-group variant="connected"` | `Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween))` of toggle buttons with `ButtonGroupDefaults.connectedLeadingButtonShapes`, `ButtonGroupDefaults.connectedMiddleButtonShapes`, `ButtonGroupDefaults.connectedTrailingButtonShapes` | verified | One color style for the whole group. Verified tonal, three options, size medium (`size="medium"` on the group and each button; in Compose give each button `ButtonDefaults.contentPaddingFor`, `ButtonDefaults.textStyleFor`, and `heightIn(min = ButtonDefaults.MediumContainerHeight)` of that size), content-sized and with `weight(1f)` across a fixed width. Heights, 2dp gaps, corners, and colors match; widths within 1dp. Compose labels use title medium (weight 500, tracking 0.15), the proposal 400 and 0.5; harmless. Leave the buttons' `shape` at its default: `shape="square"` gives 16dp outer corners that Compose's connected shapes do not have (28dp outer, 8dp inner). |
| `m3e-icon-button variant="standard"` | `IconButton(onClick, modifier = Modifier.size(48.dp), colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant))` with `Icon` inside | verified | The hero search button of the blind port; 48dp keeps the touch target. Pass the content color: Compose defaults to the surrounding content color (on surface, darker), the proposal draws on surface variant (97, 93, 104 against 52, 49, 58 measured). With it the colors match. |
| `m3e-icon-button variant="filled"` | `FilledIconButton` | verified | 40dp container, 24dp icon, primary and on primary: sizes and colors match exactly in a render. |
| `m3e-icon-button variant="tonal"` | `FilledTonalIconButton` | verified | 40dp, secondary container and on secondary container; matches exactly. |
| `m3e-icon-button variant="outlined"` | `OutlinedIconButton(onClick, colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant))` | verified | 40dp. Pass the color and the border: Compose draws the ring and the icon in the surrounding content color (near black here) where the proposal uses outline variant and on surface variant. With them the ring (181, 176, 187) and icon match. |
| `m3e-icon-button variant="elevated"` | none | unverified | No Compose counterpart: do not use |
| `m3e-icon-button toggle` | `IconToggleButton(checked, onCheckedChange, shapes = IconButtonDefaults.toggleableShapes())`, or `FilledIconToggleButton`, `FilledTonalIconToggleButton`, `OutlinedIconToggleButton` for the matching variant, with the same `shapes` | verified | Outlined icon when off, filled icon when on. Verified for variants standard, filled, tonal, and outlined, off and on: 40dp container, 24dp icon, unchecked fully round and checked squarer (12dp in the proposal, a slightly larger corner in Compose), and every container and icon color matches exactly (filled off 242, 236, 245 and on 101, 87, 137; tonal off 232, 222, 248 and on 98, 92, 113; outlined on 15, 13, 18). Standard on is just the primary icon, no container. |
| `m3e-icon-button size="extra-small"` | `FilledTonalIconButton(onClick, modifier = Modifier.size(IconButtonDefaults.extraSmallContainerSize()), shapes = IconButtonDefaults.shapes(IconButtonDefaults.extraSmallRoundShape))` with `Icon(modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize))` | verified | Also `small`, `medium`, `large`, `extraLarge` with the same suffixes. Verified tonal round at all five sizes: containers 32, 40, 56, 96, 136dp and icons 20, 24, 24, 32, 40dp match in a render, colors too. The proposal reserves a 48px box around the 32 and 40 buttons (the touch target) while Compose lays them out at 32dp and 40dp, so leave 8 to 16dp more room around those two or wrap them in `Box(Modifier.minimumInteractiveComponentSize())`. Round only: `shape="square"` and the pressed shapes are not verified. |
| `m3e-icon-button width="narrow"` | `IconButtonDefaults.IconButtonWidthOption.Narrow` | unverified | `default` is `IconButtonDefaults.IconButtonWidthOption.Uniform`, `wide` is `IconButtonDefaults.IconButtonWidthOption.Wide` |
| `m3e-split-button variant="filled"` | `SplitButtonLayout(leadingButton = { SplitButtonDefaults.LeadingButton(...) }, trailingButton = { SplitButtonDefaults.TrailingButton(...) })` | unverified | |
| `m3e-split-button variant="tonal"` | `SplitButtonDefaults.TonalLeadingButton` and `SplitButtonDefaults.TonalTrailingButton` | unverified | |
| `m3e-split-button variant="elevated"` | `SplitButtonDefaults.ElevatedLeadingButton` and `SplitButtonDefaults.ElevatedTrailingButton` | unverified | |
| `m3e-split-button variant="outlined"` | `SplitButtonDefaults.OutlinedLeadingButton` and `SplitButtonDefaults.OutlinedTrailingButton` | unverified | |
| `m3e-fab size="small"` | `FloatingActionButton` | verified | 56dp, 16dp corners, 24dp icon; the smallest `m3e-fab`. `SmallFloatingActionButton` (40dp) has no `m3e-fab` size: do not use it in a port of a proposal. |
| `m3e-fab size="medium"` | `MediumFloatingActionButton` | verified | The default size: 80dp, 20dp corners, 28dp icon (`FloatingActionButtonDefaults.MediumIconSize`). Not `FloatingActionButton`, which is 56dp. |
| `m3e-fab size="large"` | `LargeFloatingActionButton` | verified | 96dp, 28dp corners, 36dp icon (`FloatingActionButtonDefaults.LargeIconSize`). `@m3e/web` draws it 92dp wide against 96 tall, so Compose is 4dp wider; harmless. |
| `m3e-fab extended` | `MediumExtendedFloatingActionButton(onClick, icon, text)` | verified | Default size: 80dp tall, 20dp corners, 28dp icon; "Create" measured 160.0dp wide in the proposal and 159.7 in Compose. `m3e-fab extended size="small"` is `ExtendedFloatingActionButton` (56dp, 115.5 against 117.0 wide) and `size="large"` is `LargeExtendedFloatingActionButton` (96dp, 182.2 against 182.9). Icon sizes as the plain FABs (24, 28, 36). The small label is weight 500 in the proposal and 400 in Compose; harmless. |
| `m3e-fab variant="primary-container"` | `containerColor = MaterialTheme.colorScheme.primaryContainer`, `contentColor = MaterialTheme.colorScheme.onPrimaryContainer` | verified | Colors measured equal (212, 195, 253); the default. Pass the colors on a `MediumFloatingActionButton` (or the size you use). |
| `m3e-fab variant="primary"` | `containerColor = MaterialTheme.colorScheme.primary`, `contentColor = MaterialTheme.colorScheme.onPrimary` | verified | Container color measured equal (101, 87, 137). Pass the colors on a `MediumFloatingActionButton` (or the size you use). |
| `m3e-fab variant="secondary-container"` | `containerColor = MaterialTheme.colorScheme.secondaryContainer`, `contentColor = MaterialTheme.colorScheme.onSecondaryContainer` | verified | Container color measured equal (232, 222, 248). Pass the colors on a `MediumFloatingActionButton` (or the size you use). |
| `m3e-fab variant="secondary"` | `containerColor = MaterialTheme.colorScheme.secondary`, `contentColor = MaterialTheme.colorScheme.onSecondary` | verified | Container color measured equal (98, 92, 113). Pass the colors on a `MediumFloatingActionButton` (or the size you use). |
| `m3e-fab variant="tertiary-container"` | `containerColor = MaterialTheme.colorScheme.tertiaryContainer`, `contentColor = MaterialTheme.colorScheme.onTertiaryContainer` | verified | Container color measured equal (244, 191, 227). Pass the colors on a `MediumFloatingActionButton` (or the size you use). |
| `m3e-fab variant="tertiary"` | `containerColor = MaterialTheme.colorScheme.tertiary`, `contentColor = MaterialTheme.colorScheme.onTertiary` | verified | Container color measured equal (123, 82, 112). Pass the colors on a `MediumFloatingActionButton` (or the size you use). |
| `m3e-fab variant="surface"` | `containerColor = MaterialTheme.colorScheme.surfaceContainerHigh`, `contentColor = MaterialTheme.colorScheme.primary` | verified | Container color measured equal (236, 230, 240). Pass the colors on a `MediumFloatingActionButton` (or the size you use). |
| `m3e-fab-menu` | `FloatingActionButtonMenu(expanded, button = { ToggleFloatingActionButton(...) })` | unverified | Only from a regular FAB |
| `m3e-fab-menu-item` | `FloatingActionButtonMenuItem(onClick, text, icon)` | unverified | |
| `m3e-fab-menu-trigger` | `ToggleFloatingActionButton` | unverified | The button that opens the menu |
| `m3e-card variant="filled" actionable` | `Card(onClick = ..., shape = MaterialTheme.shapes.large)` | verified | Corner is the proposal's declared token; the blind port used `large` |
| `m3e-card variant="filled"` | `Card` | unverified | Not actionable: no `onClick` |
| `m3e-card variant="elevated"` | `ElevatedCard` | unverified | |
| `m3e-card variant="outlined"` | `OutlinedCard` | unverified | |
| `m3e-card orientation="horizontal"` | `Row` inside the card content | unverified | `vertical` is a `Column` |
| `m3e-list variant="standard"` and `m3e-list-item` | `ListItem` per item | verified | Slots: default is `headlineContent`, `overline` is `overlineContent`, `supporting-text` is `supportingContent`, `leading` is `leadingContent`, `trailing` is `trailingContent`. One line 56dp, two lines 72dp, overline plus two lines 88dp in both libraries; text positions and sizes match (headline 16/24, supporting 14/20, overline and trailing text 11/16) and so does the surface color. The leading icon is on surface in the proposal and on surface variant in Compose (a shade lighter); harmless. A longer supporting text wraps and grows both. |
| `m3e-list variant="segmented"` | `SegmentedListItem` per item with `ListItemDefaults.segmentedShapes(index, count)` in a `Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap))` | verified | Three two-line items: 72dp each, 2dp gap, 16dp outer and 4dp inner corners, surface container color and text positions match. Give the items `ListItemDefaults.segmentedColors`; the default container is the surface color, so on a surface background the items need a container behind them to show, as in the proposal card. |
| `m3e-list-item` | see `m3e-list` | verified | |
| `m3e-action-list` and `m3e-list-action` | `ListItem` or `SegmentedListItem` with `onClick` | unverified | |
| `m3e-selection-list` and `m3e-list-option` | `SegmentedListItem` with `selected` | unverified | One selection mode at a time |
| `m3e-expandable-list-item` | none | unverified | No stock composable: do not use |
| `m3e-chip` | none | unverified | Non-interactive chip has no stock composable: do not use |
| `m3e-chip-set` | `Row(horizontalArrangement = FilterChipDefaults.horizontalArrangement)` | unverified | Scrolls or wraps in one row |
| `m3e-assist-chip variant="outlined"` | `AssistChip` | unverified | |
| `m3e-assist-chip variant="elevated"` | `ElevatedAssistChip` | unverified | |
| `m3e-filter-chip variant="outlined"` | `FilterChip(selected, onClick, label, leadingIcon = { Icon(Modifier.size(FilterChipDefaults.IconSize)) })` | verified | 32dp tall, 8dp corners, 18dp icon, selected color secondary container: widths match within 1dp ("Quick" 70.8 and 70.9, "Dessert" 83.1 and 82.7). `@m3e/web` draws a check on a selected chip, so give a selected `FilterChip` a check `leadingIcon` (a selected "Vegan" is 91dp with it, 73 without), and an icon chip shows its own icon when off. Compose reserves 48dp of height for the touch target around the 32dp chip; leave that room (16dp more than the proposal). |
| `m3e-filter-chip variant="elevated"` | `ElevatedFilterChip` | verified | Same notes as outlined. Unselected container color (248, 241, 250) and widths match; the elevation shadow is softer in Compose. |
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
| `m3e-nav-rail mode="expanded"` | `WideNavigationRail` with `rememberWideNavigationRailState` starting at `WideNavigationRailValue.Expanded` | unverified | Close but not within the bar: item height 56dp and positions match, but the label is 14sp in Compose and 12px in `@m3e/web`, so the selected pill is 113dp wide against 105. |
| `m3e-nav-item` inside `m3e-nav-rail mode="expanded"` | `WideNavigationRailItem(railExpanded = true, ...)` | unverified | |
| `m3e-nav-rail-toggle` | `IconButton` that calls `WideNavigationRailState.toggle` | unverified | |
| `m3e-nav-bar` | `ShortNavigationBar(arrangement = ShortNavigationBarArrangement.EqualWeight)` | verified | 3 to 5 items, icons on top. Verified with three items at 345dp: container color (surface container), 56x32dp indicator in secondary container, 24dp icons, and labels (label medium, weight 500) match. The proposal bar is 68dp tall, Compose 64dp (the spec value); Compose is 4dp shorter, harmless. Set `ShortNavigationBarArrangement.EqualWeight`: the default `ShortNavigationBarArrangement.Centered` packs the items together instead of spreading them as the proposal does, and give the bar its width with a parent `Box`, not `Modifier.width` on the bar. |
| `m3e-nav-item` inside `m3e-nav-bar` | `ShortNavigationBarItem(selected, onClick, icon, label)` | verified | Selected icon colors match (on secondary container selected, on surface variant otherwise). Use the filled icon for the selected item if the proposal does; `@m3e/web` does not swap it. |
| `m3e-nav-item orientation="vertical"` | `iconPosition = NavigationItemIconPosition.Top` | verified | The default in a bar. `horizontal` is `NavigationItemIconPosition.Start` and is unverified: Compose spreads the items on equal slots while `@m3e/web` left-aligns content-sized items (a 105x40dp indicator matches, the item positions do not). |
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
| `m3e-switch` | `Switch` | verified | `icons` is the `thumbContent` slot: `Icon(painterResource(R.drawable.check), null, Modifier.size(SwitchDefaults.IconSize))` when on, `R.drawable.close` when off (see Icons). Track, thumb sizes, and colors match, off and on, with and without icons. Compose reserves a 48dp-tall touch box around the 32dp switch; leave that room. The on-state icon is slightly darker in Compose; harmless. |
| `m3e-slider` | `Slider(value, onValueChange)` | verified | Track and thumb through `SliderDefaults.Track` and `SliderDefaults.Thumb`. Verified continuous at a mid value: 16dp track, 4x44dp handle, 6dp gaps, colors match, and the `SliderState` overload renders the same. Compose's track is 2dp shorter at each end, its end dot is the primary color (proposal: on-secondary-container), inner track corners are 2dp, and the layout box is 48dp against 44; harmless. In a proposal, give the slider `min-height: 48px` to match Compose's 48dp slider, whose whole box is the touch target. Do not resize the thumb: @m3e/web derives the handle gaps from it. |
| `m3e-slider` with two `m3e-slider-thumb` | `RangeSlider` | verified | Same notes as the single slider. Compose places each handle up to 4dp nearer the track ends; harmless. |
| `m3e-slider-thumb` | see `m3e-slider` | verified | Covered by the slider rows. |
| `m3e-form-field variant="filled" hide-subscript="always"` with a slotted `label` and `<input>` | `TextField(value, onValueChange, modifier = Modifier.width(280.dp), label = { Text("Label") }, singleLine = true)` | verified | Always set `hide-subscript="always"` unless the field has hint or error text: the default reserves a 20px row under the field that Compose does not have. Measured at 280 wide, unfocused, with a value and empty: 56dp container in both, top corners 4dp, indicator 1dp in on surface variant, container surface container highest (231, 224, 236), value 16/24 in on surface, label and value on the same lines (an empty label sits 1.5dp lower in Compose), text 16dp from the edge. The label is on surface in the proposal and on surface variant in Compose; harmless. The proposal is 270px wide by default and Compose's minimum is 280dp (`TextFieldDefaults.MinWidth`): set `--m3e-form-field-width` in `style.css` (`100%` for `Modifier.fillMaxWidth()`). Focus, hover, disabled, and read-only are not verified. The proposal holds static text only: a field's value is whatever the HTML says. |
| `m3e-form-field variant="outlined" hide-subscript="always"` with a slotted `label` and `<input>` | `OutlinedTextField(value, onValueChange, modifier = Modifier.width(280.dp), label = { Text("Label") }, singleLine = true)` | verified | Same measurements as filled: 56dp, 4dp corners, 1dp outline in outline color (125, 121, 131), text 16dp from the edge, value on the same line within 0.6dp. Same label color note. |
| `m3e-form-field` with `<span slot="hint">` and `hide-subscript="never"` | `supportingText = { Text("Supporting text") }` | verified | Both variants: the field is 76dp tall in both libraries, the supporting text is 12/16 in on surface variant (97, 93, 104), 16dp from the edge, on the same line within 0.5dp. |
| `m3e-form-field error hide-subscript="never"` with `<span slot="error">` | `isError = true`, `supportingText = { Text("Error text") }` | verified | Both variants: the indicator or outline, the label, and the supporting text are all (168, 54, 75), an exact match. Put the message in the `error` slot, not `hint`: `hint` is hidden when the field is invalid. The `error` attribute keeps the static view in the error state. |
| `m3e-form-field` with `<p-icon slot="prefix">` | `leadingIcon = { Icon(painterResource(R.drawable.search), null) }` | verified | A 24dp icon; its ink starts 15dp from the edge in both libraries and the label and value follow on the same lines. Do not set `size` on the icon. The icon is on surface in the proposal and on surface variant in Compose; harmless. |
| `m3e-form-field` with `<p-icon slot="suffix">` | `trailingIcon = { Icon(painterResource(R.drawable.close), null) }` | verified | A 24dp icon at the right edge; rows and text match, icon color note as the leading icon. An icon that acts (clear, show password) is an `IconButton` in Compose; the proposal's suffix icon is decoration. |
| `m3e-form-field` with `<span slot="prefix-text">$</span>` | `prefix = { Text("$") }` | verified | The amount pattern. Both variants, with a value and empty: "$" and the value start 16dp from the edge on the same lines within 0.6dp. An empty field shows only its label in both libraries (the prefix appears once the label floats, in Compose when focused or filled). `suffix-text` is `suffix` and is not verified. |
| `m3e-form-field variant="filled"` with `<input placeholder="...">` and no label | `TextField(value, onValueChange, placeholder = { Text("Placeholder") })` | verified | Same 56dp container. The proposal draws the placeholder 7dp lower than Compose (it keeps the label's top space) and in on surface where Compose uses on surface variant; harmless. With a label, both libraries show the placeholder only when focused, so a static proposal uses the label or the placeholder, not both. |
| `m3e-form-field variant="outlined"` with `<input placeholder="...">` and no label | `OutlinedTextField(value, onValueChange, placeholder = { Text("Placeholder") })` | verified | Placeholder on the same line within 0.6dp; on surface in the proposal, on surface variant in Compose; harmless. |
| `m3e-form-field variant="filled"` with `<textarea rows="3">` | `TextField(value, onValueChange, singleLine = false, minLines = 3)` | verified | Container 104 in both, lines at the same 24dp pitch within 0.7dp. Write line breaks as `&#10;`, not source newlines (the source indentation would show). Browsers draw a resize handle in the corner that Compose lacks; harmless. |
| `m3e-form-field variant="outlined"` with `<textarea rows="3">` | `OutlinedTextField(value, onValueChange, singleLine = false, minLines = 3)` | unverified | The proposal is 88px tall and Compose about 101dp (more padding above and below the lines): Compose is taller and the design below it would shift. Use the filled variant for multi-line input. |
| `m3e-bottom-sheet modal handle` | `ModalBottomSheet(onDismissRequest, sheetState = rememberModalBottomSheetState(), dragHandle = { BottomSheetDefaults.DragHandle() })` | verified | Opened over the page by a state (see Overlays). Compared against an expanded `ModalBottomSheet` at 411 and 1280 wide: container color is surface container low (248, 241, 250) in both, top corners 28dp, drag handle 32x4dp in on surface variant (97, 93, 104), scrim black at 32% in both (172, 168, 172 over the surface), and on a wide window both are 640dp wide, centered, at the same height (`BottomSheetDefaults.SheetMaxWidth`). Content starts 48dp below the sheet top in both. The handle sits 4dp higher in the proposal (18dp from the top, Compose 22dp); harmless. The proposal sheet ends 8dp under its content, Compose ends where the content does: give the Compose content `padding(bottom = 8.dp)` to land on the same height. Only a modal sheet with a handle and no `detents` is verified. |
| `m3e-bottom-sheet` without `modal` | `BottomSheetScaffold` | unverified | A standard sheet has no scrim and is never opened over the page by a state: do not use |
| `m3e-bottom-sheet detents="..."` | `SheetState` with `skipPartiallyExpanded` and `SheetValue` | unverified | Heights between `fit`, `half`, and `full` are not compared |
| `m3e-dialog` with a `header`, text, and `actions` of two `m3e-button variant="text"` | `AlertDialog(onDismissRequest, confirmButton = { TextButton(...) }, dismissButton = { TextButton(...) }, title, text, modifier = Modifier.padding(horizontal = 24.dp).widthIn(max = 560.dp), properties = DialogProperties(usePlatformDefaultWidth = false))` | verified | Opened over the page by a state (see Overlays). Compared at the same width, 363dp on a 411dp screen (a Compose dialog sizes itself to its window, so the modifier and `usePlatformDefaultWidth = false` pin the proposal's width: the window minus 24dp of margin per side, up to 560dp). Container is surface container high (236, 230, 240), corners 28dp, 24dp padding, title 24sp on 32, text 14sp on 20, text-button actions right aligned: all match. Compose is about 6dp taller (208 against 202) because its text buttons keep a 48dp touch target around the 40dp button; leave that room. Compose's text wraps a word later than the proposal's (about 3% narrower). The scrim is black at 32% in the proposal (Material's value); a Compose dialog gets its dim from its window, so it is not compared (the sandbox render dims 60%). Only this pattern is verified: `dismissible`, `alert`, a `close-icon`, and long scrolling content are not. |
| `m3e-dialog-action` | the `onClick` of the `TextButton` that dismisses the dialog | verified | Part of the dialog row. |
| `m3e-menu` with `m3e-menu-item` children | `DropdownMenuPopup(expanded, onDismissRequest) { DropdownMenuGroup(shapes = MenuDefaults.groupShape(0, 1), containerColor = MaterialTheme.colorScheme.surfaceContainer) { items.forEachIndexed { i, text -> DropdownMenuItem(text = { Text(text) }, onClick, shape = MenuDefaults.itemShape(i, items.size).shape) } } }` | verified | Opened by a state or a trigger (see Overlays). Use this expressive menu, not the older `DropdownMenu`, whose items are 48dp tall with 4dp corners (a 3-item menu is 160dp, the proposal 144dp). Compared with three text items: container 120 x 144dp in the proposal and 121 x 148 in Compose, 16dp corners, label large text 16dp from the edge. Pass `containerColor`: Compose's group defaults to surface container low (248, 241, 250) where `@m3e/web` draws surface container (242, 236, 245); with it the colors match exactly. Compose items pitch 48dp, the proposal 46: three items end 4dp lower; harmless. `@m3e/web` focuses the first item when the menu opens and draws its focus layer on it (a render shows "Edit" shaded); Compose shades nothing until an item is focused; harmless. The elevation shadow is not compared. Only text-only items in the standard variant are verified. |
| `m3e-bottom-sheet-trigger`, `m3e-dialog-trigger`, `m3e-menu-trigger` | the `onClick` that sets the sheet, dialog, or menu visible (`expanded = true`) | verified | Wiring only, draws nothing: put it inside the `m3e-button` that opens the overlay. The overlay itself is in the rows above. |
| `m3e-bottom-sheet-action` | the `onClick` that hides the sheet | verified | Wiring only, draws nothing. |
| `m3e-menu-item-group` | `DropdownMenuGroup` | unverified | Groups radio items; not compared |
| `m3e-menu variant="vibrant"` | `MenuDefaults.groupVibrantContainerColor` and `MenuDefaults.itemVibrantColors` | unverified | |
| `m3e-menu-item` with an icon, `m3e-menu-item-checkbox`, `m3e-menu-item-radio`, a submenu, or a divider | `DropdownMenuItem` with icons, `checked` or `selected` | unverified | Not compared |
| `m3e-drawer-container` | `ModalNavigationDrawer`, `DismissibleNavigationDrawer` | unverified | This is the navigation drawer, not a side sheet. Not compared |
| side sheet | none | unverified | No `@m3e/web` element draws a side sheet (`m3e-drawer-container` is a navigation drawer) and Material3 has no stock side sheet composable: do not use. A side panel that is part of the layout is plain `Row`, `Box`, and `Surface` layout |
| `m3e-shape name="sunny"` | `Modifier.clip(MaterialShapes.Sunny.toShape())` | unverified | Name table below |
| `p-floating-toolbar` | see the shims table | verified | The `m3e-toolbar` wrapper inside it is part of this row. The toggle buttons inside are the blind port's `HorizontalFloatingToolbar` with `ToggleButton` |
| `m3e-button toggle` inside `p-floating-toolbar` | `ToggleButton(checked, onCheckedChange, colors = ToggleButtonDefaults.colors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurfaceVariant, checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer, checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer))` | verified | Selected is tonal, others text; 48dp minimum height with `Modifier.heightIn(min = 48.dp)`; icon at `ToggleButtonDefaults.IconSize` |
| `m3e-theme color variant scheme` | `MaterialExpressiveTheme(colorScheme = rememberDynamicColorScheme(seedColor, isDark, style = PaletteStyle.TonalSpot, specVersion = ColorSpec.SpecVersion.SPEC_2025), motionScheme = MotionScheme.expressive())` | unverified | Required by the page contract. See the color table for `variant` and `scheme` |
| `p-icon` | `Icon(painterResource(R.drawable.home), contentDescription = ...)` | unverified | `label` is the `contentDescription`; without it pass `null`. See Icons |
| `p-icon size="N"` | `Modifier.size(24.dp)` on the `Icon`, with N for 24 | unverified | Only for an icon that stands alone or sits in an `m3e-list-item` slot (confirmed by render: a 40 request draws 40px there). Inside a component that forces its icon size (button, icon button, FAB, FAB menu item, chip, nav item, tab, menu item, and the others in `lint.mjs`) the lint rejects `size`; port that icon with the component's default icon size, which for an icon button or FAB follows its container size row (icon buttons 20, 24, 24, 32, 40; FABs 24, 28, 36). N is 18, 20, 24 (default), 28, 32, 36, 40, or 48, each a Compose icon size measured in a render: 18 `AssistChipDefaults.IconSize` and `FilterChipDefaults.IconSize`; 20 `ButtonDefaults.ExtraSmallIconSize` and `IconButtonDefaults.extraSmallIconSize`; 24 `ButtonDefaults.MediumIconSize` and `IconButtonDefaults.smallIconSize` and `mediumIconSize`; 28 `FloatingActionButtonDefaults.MediumIconSize`; 32 `ButtonDefaults.LargeIconSize` and `IconButtonDefaults.largeIconSize`; 36 `FloatingActionButtonDefaults.LargeIconSize`; 40 `ButtonDefaults.ExtraLargeIconSize` and `IconButtonDefaults.extraLargeIconSize`; 48 the largest optical size on m3.material.io/styles/icons. A larger icon in a button comes from a larger size (a 40px icon is `m3e-icon-button size="extra-large"`), not from `size`; a 48dp icon in a custom play button is a standalone icon in a plain styled element. |

### Overlays and open states

A sheet, dialog, or menu is hidden until opened, so the default views never show it. The proposal declares each one as a state, `<meta name="p-states" content="queue lyrics">` in the head of `index.html`, and its own script defines `window.proposalState = name => { ... }`, which opens that state (for example `document.getElementById(name).show()`). `render` calls it for each name, shoots `<state>-light.png` (phone) and `<state>-tablet.png`, and runs the checks on each. While a modal sheet or dialog is open, content behind it is exempt from the overlap, contrast, and touch-target checks and the primary-action check is skipped (the default views cover them). The Compose port opens the same overlay from the same trigger, with `ModalBottomSheet`, `AlertDialog`, or the menu above. A hand-built overlay has no verified row; build it from a verified one or leave it out.

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

One class per Material role, plus the emphasized role. Sizes are the generated values in `runtime-src/type-scale.json`, so each baseline class equals its `MaterialTheme.typography` role. An emphasized class does not map to `MaterialTheme.typography.<role>Emphasized`: with the pinned material3 (`1.5.0-alpha29`), `Typography()` gives every `*Emphasized` style the baseline role's weight and tracking (a render of `displayLargeEmphasized` showed weight 400 and tracking -0.2sp, `titleSmallEmphasized` weight 500), so the library's emphasized styles are not emphasized. The class values are Google's emphasized tokens (`TypeScaleTokens`), so the Compose column copies the baseline role with the token weight and tracking. Measured as widths of an 18-character line in each emphasized role, the `.copy` renders within 5% of the class (ink width against advance width, the same offset the baseline roles show), where the library's `*Emphasized` rendered up to 9% narrower than the class where the weights differ (title and label roles).

| HTML | Compose | Values |
| --- | --- | --- |
| `.t-display-large` | `MaterialTheme.typography.displayLarge` | 57sp / 64sp, weight 400, tracking -0.2sp |
| `.t-display-large-em` | `MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight(500), letterSpacing = 0.sp)` | 57sp / 64sp, weight 500, tracking 0sp |
| `.t-display-medium` | `MaterialTheme.typography.displayMedium` | 45sp / 52sp, weight 400, tracking 0sp |
| `.t-display-medium-em` | `MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight(500), letterSpacing = 0.sp)` | 45sp / 52sp, weight 500, tracking 0sp |
| `.t-display-small` | `MaterialTheme.typography.displaySmall` | 36sp / 44sp, weight 400, tracking 0sp |
| `.t-display-small-em` | `MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight(500), letterSpacing = 0.sp)` | 36sp / 44sp, weight 500, tracking 0sp |
| `.t-headline-large` | `MaterialTheme.typography.headlineLarge` | 32sp / 40sp, weight 400, tracking 0sp |
| `.t-headline-large-em` | `MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight(500), letterSpacing = 0.sp)` | 32sp / 40sp, weight 500, tracking 0sp |
| `.t-headline-medium` | `MaterialTheme.typography.headlineMedium` | 28sp / 36sp, weight 400, tracking 0sp |
| `.t-headline-medium-em` | `MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight(500), letterSpacing = 0.sp)` | 28sp / 36sp, weight 500, tracking 0sp |
| `.t-headline-small` | `MaterialTheme.typography.headlineSmall` | 24sp / 32sp, weight 400, tracking 0sp |
| `.t-headline-small-em` | `MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight(500), letterSpacing = 0.sp)` | 24sp / 32sp, weight 500, tracking 0sp |
| `.t-title-large` | `MaterialTheme.typography.titleLarge` | 22sp / 28sp, weight 400, tracking 0sp |
| `.t-title-large-em` | `MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight(500), letterSpacing = 0.sp)` | 22sp / 28sp, weight 500, tracking 0sp |
| `.t-title-medium` | `MaterialTheme.typography.titleMedium` | 16sp / 24sp, weight 500, tracking 0.2sp |
| `.t-title-medium-em` | `MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight(700), letterSpacing = 0.15.sp)` | 16sp / 24sp, weight 700, tracking 0.15sp |
| `.t-title-small` | `MaterialTheme.typography.titleSmall` | 14sp / 20sp, weight 500, tracking 0.1sp |
| `.t-title-small-em` | `MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight(700), letterSpacing = 0.1.sp)` | 14sp / 20sp, weight 700, tracking 0.1sp |
| `.t-body-large` | `MaterialTheme.typography.bodyLarge` | 16sp / 24sp, weight 400, tracking 0.5sp |
| `.t-body-large-em` | `MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight(500), letterSpacing = 0.15.sp)` | 16sp / 24sp, weight 500, tracking 0.15sp |
| `.t-body-medium` | `MaterialTheme.typography.bodyMedium` | 14sp / 20sp, weight 400, tracking 0.2sp |
| `.t-body-medium-em` | `MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight(500), letterSpacing = 0.25.sp)` | 14sp / 20sp, weight 500, tracking 0.25sp |
| `.t-body-small` | `MaterialTheme.typography.bodySmall` | 12sp / 16sp, weight 400, tracking 0.4sp |
| `.t-body-small-em` | `MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight(500), letterSpacing = 0.4.sp)` | 12sp / 16sp, weight 500, tracking 0.4sp |
| `.t-label-large` | `MaterialTheme.typography.labelLarge` | 14sp / 20sp, weight 500, tracking 0.1sp |
| `.t-label-large-em` | `MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight(700), letterSpacing = 0.1.sp)` | 14sp / 20sp, weight 700, tracking 0.1sp |
| `.t-label-medium` | `MaterialTheme.typography.labelMedium` | 12sp / 16sp, weight 500, tracking 0.5sp |
| `.t-label-medium-em` | `MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight(700), letterSpacing = 0.5.sp)` | 12sp / 16sp, weight 700, tracking 0.5sp |
| `.t-label-small` | `MaterialTheme.typography.labelSmall` | 11sp / 16sp, weight 500, tracking 0.5sp |
| `.t-label-small-em` | `MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight(700), letterSpacing = 0.5.sp)` | 11sp / 16sp, weight 700, tracking 0.5sp |
| `.voice-<name>` | `TextStyle` with `fontFamily = FontFamily(voiceFont(R.font.google_sans_flex, w, FontVariation.width(wd), FontVariation.opticalSizing(os.sp)))` (the starter's theme/Typography.kt helper; plain `Font(..., variationSettings = FontVariation.Settings(FontVariation.weight(w), ...))` is the same call) | Build it once per declared voice from `font-variation-settings` axes (`wght`, `wdth`, `opsz`); start from the role the proposal extends with `.copy` and take its size and line height. Every axis the rule names must be passed: Compose sets only the axes it is given |
| `.voice-<name>` with `font-family` naming a bundled face | The same `TextStyle`, with `FontFamily(Font(R.font.<file>, FontWeight(w), variationSettings = FontVariation.Settings(FontVariation.weight(w), <the face's own axes>)))`. Files in `res/font/`: `fraunces` (`opsz` via `FontVariation.opticalSizing`, plus `FontVariation.Setting("SOFT", v)` and `FontVariation.Setting("WONK", v)`), `bricolage_grotesque` (`opticalSizing`, `FontVariation.width`), `big_shoulders_display` (`weight`), `doto` (`FontVariation.Setting("ROND", v)`), `google_sans_code` (`weight`) | Map each `font-variation-settings` axis to the matching setting, with `voiceFont` from the starter's theme/Typography.kt (`voiceFont(R.font.fraunces, 600, FontVariation.opticalSizing(52.sp), FontVariation.Setting("SOFT", 100f), FontVariation.Setting("WONK", 1f))`). Optical size: the runtime sets `font-optical-sizing: none` and Compose never sets `opsz` by itself, so a voice that does not name `opsz` is the font's default on both sides (18 for Google Sans Flex, 9 for Fraunces), and a voice that names it (`"opsz" 52`) must pass `FontVariation.opticalSizing(52.sp)` in the port: the port must set it, the proposal never relies on automatic sizing. Measured on Fraunces 600 "Golden Hour" at 52px, ink widths in HTML and Compose (dp = px at the preview scale): `opsz` 52 312 and 309, `opsz` 144 273 and 269, `opsz` 9 326 and 322, `opsz` 52 with SOFT 100 and WONK 1 314 and 310; Compose with no `opsz` is the `opsz` 9 width (322), 4% wider than `opsz` 52, which is the gap a port that drops the axis creates. The two sides agree within 2% when the axes match. Brand faces only on Display and Headline styles (Doto Display only); `google_sans_code` anywhere. Google Sans Flex stays the default family, so a voice without `font-family` keeps the row above |

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
| `--md-sys-color-primary-fixed-dim` | `MaterialTheme.colorScheme.primaryFixedDim` |  |
| `--md-sys-color-on-primary-fixed` | `MaterialTheme.colorScheme.onPrimaryFixed` |  |
| `--md-sys-color-on-primary-fixed-variant` | `MaterialTheme.colorScheme.onPrimaryFixedVariant` |  |
| `--md-sys-color-inverse-primary` | `MaterialTheme.colorScheme.inversePrimary` |  |
| `--md-sys-color-secondary` | `MaterialTheme.colorScheme.secondary` |  |
| `--md-sys-color-secondary-dim` | none | No `ColorScheme` member in this material3 version: do not use |
| `--md-sys-color-on-secondary` | `MaterialTheme.colorScheme.onSecondary` |  |
| `--md-sys-color-secondary-container` | `MaterialTheme.colorScheme.secondaryContainer` |  |
| `--md-sys-color-on-secondary-container` | `MaterialTheme.colorScheme.onSecondaryContainer` |  |
| `--md-sys-color-secondary-fixed` | `MaterialTheme.colorScheme.secondaryFixed` |  |
| `--md-sys-color-secondary-fixed-dim` | `MaterialTheme.colorScheme.secondaryFixedDim` |  |
| `--md-sys-color-on-secondary-fixed` | `MaterialTheme.colorScheme.onSecondaryFixed` |  |
| `--md-sys-color-on-secondary-fixed-variant` | `MaterialTheme.colorScheme.onSecondaryFixedVariant` |  |
| `--md-sys-color-tertiary` | `MaterialTheme.colorScheme.tertiary` |  |
| `--md-sys-color-tertiary-dim` | none | No `ColorScheme` member in this material3 version: do not use |
| `--md-sys-color-on-tertiary` | `MaterialTheme.colorScheme.onTertiary` |  |
| `--md-sys-color-tertiary-container` | `MaterialTheme.colorScheme.tertiaryContainer` |  |
| `--md-sys-color-on-tertiary-container` | `MaterialTheme.colorScheme.onTertiaryContainer` |  |
| `--md-sys-color-tertiary-fixed` | `MaterialTheme.colorScheme.tertiaryFixed` |  |
| `--md-sys-color-tertiary-fixed-dim` | `MaterialTheme.colorScheme.tertiaryFixedDim` |  |
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

Both sides use Material Symbols, so no per-icon table exists. Every `p-icon name="x"` ports as the same Material Symbol: Rounded style, weight 400, grade 0, with Fill 1 when the element has `filled` and Fill 0 otherwise (so a toggle's off and on states are two drawables). Download it from [fonts.google.com/icons](https://fonts.google.com/icons) in the Android format (or add the SVG with Android Studio's Vector Asset) and import it as a vector drawable named after the symbol. Delete the `android:tint="?attr/colorControlNormal"` attribute from the downloaded `<vector>` tag (keep the tag's closing `>`): the starter has no AppCompat or MDC, so the attribute fails resource linking, and `Icon` applies the color. Draw it with `Icon(painterResource(R.drawable.name), contentDescription = label)`; the `label` attribute is the `contentDescription`, and without it pass `null`. Leave `tint` at its default. The legacy `Icons.Rounded` set from `material-icons-extended` is frozen and lacks the Fill axis: do not use it.

Directional icons (`arrow_back`, `arrow_forward`, `sort`, `help`, `chat`, `list`, `queue_music`, and any other glyph whose meaning has a direction) must mirror in right-to-left layouts: set `android:autoMirrored="true"` on the drawable's `<vector>` tag.

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
