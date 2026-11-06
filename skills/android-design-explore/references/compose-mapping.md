# Compose mapping for proposals

The lookup for porting an approved proposal into an Android app. `export_kotlin` gives the first draft; this table is the Compose call each catalog component and variant should land on, and what is known about it. Every Compose name here exists in `scripts/material3-inventory.txt` in the repository, the public names of the pinned material3 artifact, except a few `foundation`, `ui`, Navigation 3, and MaterialKolor names that the test in `scripts/test_material3_inventory.py` lists on purpose.

`verified` records that the Compose call was rendered on Android and measured. That evidence predates v3: it was compared with the HTML `m3e-*` components proposals used before the previewer app, not with the app's own Compose, so read it as a measured call, not as a check against the window. `unverified` means the call is the right one but nobody has rendered and measured it: report it in the port. To verify a row, export `evals/verify/verify.proposal.json` (it uses every catalog component), render it in `evals/compose-starter/` as the export check in `evals/README.md` describes, compare it with the app's render of the same component, then change the row's Status and name the evidence in the commit. A small difference (a few dp, a weight or tracking nuance) is acceptable when it cannot break an approved design; record it in Notes. A difference that can break a design (noticeably larger, text that would no longer fit) blocks verification.

Measured on a Pixel 10 Pro XL emulator, not Google rules: with Google Sans Flex, headline-size text and the large and extra-large buttons render a few dp taller than their type tokens, because the font sets a minimum line height, and display-size text by more (`displayLarge`, a 64sp token line height, rendered about 71dp tall). Leave vertical room around headline and display text. Fraunces, Bricolage Grotesque, and Big Shoulders Display at 36/44 and 32/40, and Google Sans Code at 16/24 and 32/40, match the token line height within 1dp; Doto at 45/52 renders 54dp tall.

Compose is written at implementation, in the app, under the app's own conventions. Use the patterns below as the API call to reach for, not as finished code.

## Components

The first cell is the catalog component and the prop values that select the pattern. A prop not listed has no Compose parameter to set beyond the obvious one (a `label` is the text slot, an `icon` is an `Icon`).

| Catalog | Compose | Status | Notes |
| --- | --- | --- | --- |
| `Column` | `Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally)` | unverified | `spacing` is the `spacedBy` gap, at the step's dp value; `scroll` is `Modifier.verticalScroll(rememberScrollState())`. |
| `Row` | `Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically)` | unverified | `arrangement` values other than `spacing` map to `Arrangement.SpaceBetween` and its siblings. `verticalAlignment="stretch"` writes no alignment and adds `Modifier.fillMaxHeight()` to its `Column`, `Row`, and `Box` children, the full-height panes; it fills the height the row is given (a Scaffold's content, or `weight` inside a `Column`). `stretch` alone was rendered on Android against the app's tablet view: three weighted panes filled the height in both. |
| `Box` | `Box(contentAlignment = Alignment.TopStart)` | unverified | |
| `Grid` | `Grid` | unverified | Compose's experimental grid layout. `columns` is a fixed count, `minColumnWidth` an adaptive column size. An app whose foundation version lacks it uses `LazyVerticalGrid(columns = GridCells.Fixed(n))` or `GridCells.Adaptive(minSize)`. |
| `FlexBox` | `FlexBox` | unverified | Compose's experimental flex layout. An app whose foundation version lacks it uses `FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp))`. |
| `Spacer` | `Spacer(Modifier.width(16.dp))` or `Spacer(Modifier.height(16.dp))` | unverified | The step's dp value. |
| `Divider` | `HorizontalDivider` | unverified | `vertical` is `VerticalDivider`. |
| `Shape` | `Box(Modifier.clip(MaterialShapes.Sunny.toShape()))` | unverified | The `shape` name with its first letter upper-cased is the `MaterialShapes` member (`cookie9Sided` is `MaterialShapes.Cookie9Sided`). `color` is the box's `Modifier.background`. |
| `Text` | `Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)` | unverified | `type` and `color` are the roles; see Tokens. `maxLines` with `overflow` `ellipsis` is `overflow = TextOverflow.Ellipsis`; `align` is `textAlign = TextAlign.Center` and its siblings. |
| `Icon` | `Icon(painterResource(R.drawable.home), contentDescription = null)` | unverified | See Icons. `size` is `Modifier.size(32.dp)` (with its value) on an icon that stands alone or sits in a list item slot. |
| `Image` | `Image(painterResource(R.drawable.plate), contentDescription, contentScale = ContentScale.Crop, modifier = Modifier.clip(MaterialTheme.shapes.large).aspectRatio(1.5f))` | unverified | `fit` names the `ContentScale` (`crop` is `ContentScale.Crop`); `shape` is the clip. Copy the image into the app's `res/drawable/`. |
| `Scaffold` | `Scaffold(topBar, bottomBar, floatingActionButton) { innerPadding -> }` | unverified | `navigationRail` sits beside the scaffold: `Row { WideNavigationRail(...); Scaffold(...) }`. Pad the content by `innerPadding`. |
| `TopAppBar size="small"` | `TopAppBar` | unverified | Slots: `navigationIcon` is `navigationIcon`, `title` is `title`, `actions` is `actions`, `subtitle` is the bar's `subtitle` slot. |
| `TopAppBar size="center"` | `CenterAlignedTopAppBar` | unverified | |
| `TopAppBar size="medium"` | `MediumFlexibleTopAppBar` | unverified | |
| `TopAppBar size="large"` | `LargeFlexibleTopAppBar` | unverified | |
| `NavigationBar` | `ShortNavigationBar(arrangement = ShortNavigationBarArrangement.EqualWeight)` | verified | 3 to 5 items, icons on top. Verified with three items at 345dp: container color (surface container), 56x32dp indicator in secondary container, 24dp icons, labels (label medium, weight 500), and the 64dp bar height. Set `ShortNavigationBarArrangement.EqualWeight`: the default `ShortNavigationBarArrangement.Centered` packs the items together instead of spreading them. Give the bar its width with a parent `Box`, not `Modifier.width` on the bar. |
| `NavigationBarItem` | `ShortNavigationBarItem(selected, onClick, icon, label)` | verified | Selected icon on secondary container, others on surface variant. `selectedIcon` is the icon drawn while selected (usually the filled symbol). The icon sits on top (`NavigationItemIconPosition.Top`, the default in a bar). |
| `Tabs` | `PrimaryTabRow(selectedTabIndex)` | unverified | Sits under the app bar and fills the width. `selectedTabIndex` is the index of the tab whose `selected` is set. `variant="secondary"` is `SecondaryTabRow`. Tabs hold related content at the same level of hierarchy, scrollable when they do not fit; fewer than 3 navigation destinations are tabs, not a navigation bar. |
| `Tabs scrollable=true` | `PrimaryScrollableTabRow` or `SecondaryScrollableTabRow` | unverified | The first tab sits 52dp from the leading edge by default (`edgePadding`). |
| `Tab` | `Tab(selected, onClick, text, icon, unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant)` | unverified | The inactive color is Material's on surface variant; the selected one is the row's own (primary, or on surface in a secondary row). `label` is `text`, `icon` is `icon` (icon above the label, 64dp tall; text only is 48dp). A tab's content is another screen: `action` is the `navigate` call. Selection moves on tap as it does for navigation items. |
| `NavigationRail` | `WideNavigationRail(state = rememberWideNavigationRailState())` | verified | Collapsed rail. |
| `NavigationRail expanded=true` | `WideNavigationRail` with `rememberWideNavigationRailState` starting at `WideNavigationRailValue.Expanded` | unverified | Item height 56dp. |
| `NavigationRailItem` | `WideNavigationRailItem(railExpanded = false, selected, onClick, icon, label)` | verified | In an expanded rail, `railExpanded = true` (unverified). |
| `NavigationBarItem` or `NavigationRailItem` with `badge` | `icon = { BadgedBox(badge = { Badge() }) { Icon(...) } }` | unverified | `badge: true` is the small dot, 6dp: `Badge()`. A number or a text of up to four characters is the large badge: `Badge { Text("12") }`. Default colors, error and on error. |
| `Tab` or `IconButton` with `badge` | `BadgedBox(badge = { Badge() }) { Icon(...) }` | unverified | As on a navigation item. A `Tab` without an `icon` wraps its label in the `BadgedBox`. |
| `FloatingToolbar` | `HorizontalFloatingToolbar(expanded = true, modifier = Modifier.align(Alignment.BottomCenter).offset(y = -FloatingToolbarDefaults.ScreenOffset), colors = FloatingToolbarDefaults.standardFloatingToolbarColors())` | verified | Floats at bottom center, 16dp above the edge (`FloatingToolbarDefaults.ScreenOffset`), inside a `Box` over the content. Add `WindowInsets.navigationBars` padding in an edge-to-edge app. |
| `FloatingToolbar orientation="vertical"` | `VerticalFloatingToolbar` | unverified | |
| `FloatingToolbar colors="vibrant"` | `colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()` | unverified | |
| `ToggleButton` inside `FloatingToolbar` | `ToggleButton(checked, onCheckedChange, colors = ToggleButtonDefaults.colors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurfaceVariant, checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer, checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer))` | verified | Single-select: the checked one is tonal, the others text. 48dp minimum height with `Modifier.heightIn(min = 48.dp)`; icon at `ToggleButtonDefaults.IconSize`. |
| `Carousel variant="multi-browse"` | `HorizontalMultiBrowseCarousel(state = rememberCarouselState { count }, preferredItemWidth = itemWidth, itemSpacing = 8.dp, flingBehavior = CarouselDefaults.multiBrowseFlingBehavior(state), 16dp horizontal contentPadding)` | unverified | Each child is the item content, clipped with `Modifier.maskClip(MaterialTheme.shapes.extraLarge)` (not `clip`, which drops the parallax). `itemWidth` is `preferredItemWidth`. Small items are narrower than the text and cut it, and the previewer reports that as `text-clipped` unless the text sets `maxLines` with an ellipsis, so prefer `maxLines` 1 with an ellipsis in a proposal. |
| `Carousel variant="hero"` | `HorizontalMultiBrowseCarousel` with `preferredItemWidth` = available width - 96.dp | unverified | No composable of its own: one large and one small item. 96dp is the 16dp padding twice, the 56dp small item and the 8dp gap; read the width from `BoxWithConstraints`. `itemWidth` is ignored. |
| `Carousel variant="centered-hero"` | `HorizontalCenteredHeroCarousel(itemSpacing = 8.dp, 16dp horizontal contentPadding)` | unverified | |
| `Carousel variant="uncontained"` | `HorizontalUncontainedCarousel(itemWidth, itemSpacing = 8.dp, flingBehavior = CarouselDefaults.noSnapFlingBehavior(), 16dp horizontal contentPadding)` | unverified | |
| `Carousel variant="full-screen"` | `HorizontalUncontainedCarousel` with `itemWidth` = available width, `itemSpacing = 16.dp`, `no contentPadding`, `flingBehavior = CarouselDefaults.singleAdvanceFlingBehavior(state)` | unverified | No composable of its own: one edge-to-edge item with snapping. Read the width from `BoxWithConstraints`. `itemWidth` is ignored. |
| `Card variant="filled"` with `action` | `Card(onClick = ..., shape = MaterialTheme.shapes.large)` | verified | The corner is the proposal's `shape` token. The card's `content` is a `Column` inside it. |
| `Card variant="filled"` | `Card` | unverified | No `action`: no `onClick`. |
| `Card variant="elevated"` | `ElevatedCard` | unverified | |
| `Card variant="outlined"` | `OutlinedCard` | unverified | |
| `List variant="standard"` | `ListItem` per item | verified | Slots: `headline` is `headlineContent`, `overline` is `overlineContent`, `supporting` is `supportingContent`, `leading` is `leadingContent`, `trailing` is `trailingContent`. One line 56dp, two lines 72dp, overline plus two lines 88dp; headline 16/24, supporting 14/20, overline and trailing text 11/16. A longer supporting text wraps and grows the item. |
| `List variant="segmented"` | `SegmentedListItem` per item with `ListItemDefaults.segmentedShapes(index, count)` in a `Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap))` | verified | Three two-line items: 72dp each, 2dp gap, 16dp outer and 4dp inner corners. Give the items `ListItemDefaults.segmentedColors`: the default container is the surface color, so on a surface background the items need a container color to show. |
| `ListItem` with `action` | `ListItem` or `SegmentedListItem` with `onClick` | unverified | |
| `ListItem selected=true` | `SegmentedListItem` with `selected` | unverified | |
| `Button variant="filled" size="extra-large"` | `Button(shapes = ButtonDefaults.shapesFor(ButtonDefaults.ExtraLargeContainerHeight), contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.ExtraLargeContainerHeight), modifier = Modifier.heightIn(min = ButtonDefaults.ExtraLargeContainerHeight))` with label `ButtonDefaults.textStyleFor(ButtonDefaults.ExtraLargeContainerHeight)` | verified | Container and content colors come from the theme roles, not overrides. With Google Sans Flex it renders 142dp tall against the 136dp token (see the line-height note above). |
| `Button size="extra-small"` | same pattern with height `ButtonDefaults.ExtraSmallContainerHeight` | unverified | |
| `Button size="small"` | same pattern with height `ButtonDefaults.MinHeight` | verified | 16dp of horizontal padding a side. |
| `Button size="medium"` | same pattern with height `ButtonDefaults.MediumContainerHeight` | verified | The label uses title medium (weight 500, tracking 0.2). |
| `Button size="large"` | same pattern with height `ButtonDefaults.LargeContainerHeight` | verified | With Google Sans Flex it renders about 99dp tall against the 96dp token. |
| `Button variant="tonal"` | `FilledTonalButton` | verified | Size pattern as above. Verified at small, medium, large, and extra-large, with the same notes as filled. |
| `Button variant="elevated"` | `ElevatedButton` | unverified | Size pattern as above |
| `Button variant="outlined"` | `OutlinedButton` | unverified | Size pattern as above |
| `Button variant="text"` | `TextButton` | unverified | Size pattern as above |
| `Button shape="square"` | `ButtonDefaults.squareShape` as the `shape` of `ButtonDefaults.shapes` | unverified | `round` is the default and is what `shapesFor` gives |
| `Button` with `icon` | `Icon(modifier = Modifier.size(ButtonDefaults.IconSize))` then `Spacer(Modifier.width(ButtonDefaults.IconSpacing))` | verified | Use `ButtonDefaults.iconSizeFor` and `ButtonDefaults.iconSpacingFor` with the button height at other sizes: icons 20, 24, 32, 40 and gaps 8, 8, 12, 16 at small to extra-large. |
| `ToggleButton variant="filled"` | `ToggleButton(checked, onCheckedChange, shapes = ToggleButtonDefaults.shapesFor(ToggleButtonDefaults.MinHeight))` | verified | Checked shows the checked shape (12dp corners, unchecked fully round). 16dp of horizontal padding a side. |
| `ToggleButton variant="tonal"` | `FilledTonalToggleButton` | verified | Same notes as filled. Unchecked is the secondary container, checked the secondary color. |
| `ToggleButton variant="elevated"` | `ElevatedToggleButton` | unverified | |
| `ToggleButton variant="outlined"` | `OutlinedToggleButton` | unverified | |
| `ButtonGroup variant="standard"` | `ButtonGroup` | unverified | Set the size on each button. |
| `ButtonGroup variant="connected"` | `Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween))` of toggle buttons with `ButtonGroupDefaults.connectedLeadingButtonShapes`, `ButtonGroupDefaults.connectedMiddleButtonShapes`, `ButtonGroupDefaults.connectedTrailingButtonShapes` | verified | One color style for the whole group. Verified tonal, three options, size medium (give each button `ButtonDefaults.contentPaddingFor`, `ButtonDefaults.textStyleFor`, and `heightIn(min = ButtonDefaults.MediumContainerHeight)` of that size), content-sized and with `weight(1f)` across a fixed width. 2dp gaps, 28dp outer and 8dp inner corners. Labels use title medium (weight 500, tracking 0.15). |
| `IconButton variant="standard"` | `IconButton(onClick, modifier = Modifier.size(48.dp), colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant))` with `Icon` inside | verified | 48dp keeps the touch target. Pass the content color: Compose defaults to the surrounding content color (on surface, darker). |
| `IconButton variant="filled"` | `FilledIconButton` | verified | 40dp container, 24dp icon, primary and on primary. |
| `IconButton variant="tonal"` | `FilledTonalIconButton` | verified | 40dp, secondary container and on secondary container. |
| `IconButton variant="outlined"` | `OutlinedIconButton(onClick, colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant))` | verified | 40dp. Pass the color and the border: by default Compose draws the ring and the icon in the surrounding content color (near black). |
| `IconButton toggle=true` | `IconToggleButton(checked, onCheckedChange, shapes = IconButtonDefaults.toggleableShapes())`, or `FilledIconToggleButton`, `FilledTonalIconToggleButton`, `OutlinedIconToggleButton` for the matching variant, with the same `shapes` | verified | Outlined icon when unchecked, filled icon when checked. Verified for standard, filled, tonal, and outlined, unchecked and checked: 40dp container, 24dp icon, unchecked fully round and checked squarer. Standard checked is just the primary icon, no container. |
| `IconButton size="extra-small"` | `FilledTonalIconButton(onClick, modifier = Modifier.size(IconButtonDefaults.extraSmallContainerSize()), shapes = IconButtonDefaults.shapes(IconButtonDefaults.extraSmallRoundShape))` with `Icon(modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize))` | verified | Also `small`, `medium`, `large`, `extra-large` with the matching `small`, `medium`, `large`, `extraLarge` names. Verified tonal round at all five sizes: containers 32, 40, 56, 96, 136dp and icons 20, 24, 24, 32, 40dp. Compose lays the 32 and 40dp buttons out at their visual size, so wrap them in `Box(Modifier.minimumInteractiveComponentSize())` or leave 8 to 16dp of room to keep a 48dp touch target. Round only here; `shape="square"` is the next row. |
| `IconButton shape="square"` | `IconButtonDefaults.shapes(IconButtonDefaults.smallSquareShape, IconButtonDefaults.smallPressedShape)` as the `shapes` of the button; a toggle uses `IconButtonDefaults.toggleableShapes(IconButtonDefaults.mediumSquareShape, IconButtonDefaults.mediumPressedShape, IconButtonDefaults.mediumSelectedSquareShape)` at medium | verified | Rendered on Android against the app's window: filled large, tonal small, filled small toggle unchecked and checked, and outlined extra-small toggle. Corners, sizes, and the checked toggle's round shape matched. `round` is the default and keeps `shapes()` and `toggleableShapes()`. Take the `small`, `medium`, `large`, `extraLarge` names from the button's `size`, with `extra-small` as `extraSmall`; the square, pressed, and selected shapes must all match the size. The square corner is 12dp at extra-small and small, 16dp at medium, and 28dp at large and extra-large; a checked square toggle turns fully round. |
| `IconButton width="narrow"` | `IconButtonDefaults.IconButtonWidthOption.Narrow` | unverified | `uniform` is `IconButtonDefaults.IconButtonWidthOption.Uniform`, `wide` is `IconButtonDefaults.IconButtonWidthOption.Wide` |
| `SplitButton variant="filled"` | `SplitButtonLayout(leadingButton = { SplitButtonDefaults.LeadingButton(...) }, trailingButton = { SplitButtonDefaults.TrailingButton(...) })` | unverified | `menu` is the popup of the Menu row, opened from the trailing button. |
| `SplitButton variant="tonal"` | `SplitButtonDefaults.TonalLeadingButton` and `SplitButtonDefaults.TonalTrailingButton` | unverified | |
| `SplitButton variant="elevated"` | `SplitButtonDefaults.ElevatedLeadingButton` and `SplitButtonDefaults.ElevatedTrailingButton` | unverified | |
| `SplitButton variant="outlined"` | `SplitButtonDefaults.OutlinedLeadingButton` and `SplitButtonDefaults.OutlinedTrailingButton` | unverified | |
| `FloatingActionButton size="small"` | `FloatingActionButton` | verified | 56dp, 16dp corners, 24dp icon. The catalog has no 40dp FAB: `SmallFloatingActionButton` is never a port target. |
| `FloatingActionButton size="medium"` | `MediumFloatingActionButton` | verified | 80dp, 20dp corners, 28dp icon (`FloatingActionButtonDefaults.MediumIconSize`). Not `FloatingActionButton`, which is 56dp. |
| `FloatingActionButton size="large"` | `LargeFloatingActionButton` | verified | 96dp, 28dp corners, 36dp icon (`FloatingActionButtonDefaults.LargeIconSize`). |
| `FloatingActionButton extended=true` | `MediumExtendedFloatingActionButton(onClick, icon, text)` | verified | Medium: 80dp tall, 20dp corners, 28dp icon. `size="small"` is `ExtendedFloatingActionButton` (56dp) and `size="large"` is `LargeExtendedFloatingActionButton` (96dp). Icon sizes as the plain FABs (24, 28, 36). |
| `FloatingActionButton color="primary-container"` | `containerColor = MaterialTheme.colorScheme.primaryContainer`, `contentColor = MaterialTheme.colorScheme.onPrimaryContainer` | verified | Pass the colors on the FAB of the proposal's size. |
| `FloatingActionButton color="primary"` | `containerColor = MaterialTheme.colorScheme.primary`, `contentColor = MaterialTheme.colorScheme.onPrimary` | verified | Pass the colors on the FAB of the proposal's size. |
| `FloatingActionButton color="secondary-container"` | `containerColor = MaterialTheme.colorScheme.secondaryContainer`, `contentColor = MaterialTheme.colorScheme.onSecondaryContainer` | verified | Pass the colors on the FAB of the proposal's size. |
| `FloatingActionButton color="secondary"` | `containerColor = MaterialTheme.colorScheme.secondary`, `contentColor = MaterialTheme.colorScheme.onSecondary` | verified | Pass the colors on the FAB of the proposal's size. |
| `FloatingActionButton color="tertiary-container"` | `containerColor = MaterialTheme.colorScheme.tertiaryContainer`, `contentColor = MaterialTheme.colorScheme.onTertiaryContainer` | verified | Pass the colors on the FAB of the proposal's size. |
| `FloatingActionButton color="tertiary"` | `containerColor = MaterialTheme.colorScheme.tertiary`, `contentColor = MaterialTheme.colorScheme.onTertiary` | verified | Pass the colors on the FAB of the proposal's size. |
| `FloatingActionButton color="surface"` | `containerColor = MaterialTheme.colorScheme.surfaceContainerHigh`, `contentColor = MaterialTheme.colorScheme.primary` | verified | Pass the colors on the FAB of the proposal's size. |
| `FabMenu` | `FloatingActionButtonMenu(expanded, button = { ToggleFloatingActionButton(...) })` | unverified | Only from a regular FAB. `icon` is the toggle's icon. |
| `FabMenuItem` | `FloatingActionButtonMenuItem(onClick, text, icon)` | unverified | |
| `AssistChip variant="outlined"` | `AssistChip` | unverified | |
| `AssistChip variant="elevated"` | `ElevatedAssistChip` | unverified | |
| `FilterChip variant="outlined"` | `FilterChip(selected, onClick, label, leadingIcon = { Icon(Modifier.size(FilterChipDefaults.IconSize)) })` | verified | 32dp tall, 8dp corners, 18dp icon, selected color secondary container. Give a selected chip a check `leadingIcon`; a chip with its own `icon` shows that icon when unselected. Compose reserves 48dp of height for the touch target around the 32dp chip; leave that room. |
| `FilterChip variant="elevated"` | `ElevatedFilterChip` | verified | Same notes as outlined. |
| `InputChip` | `InputChip` | unverified | A remove control is the `trailingIcon`. |
| `SuggestionChip variant="outlined"` | `SuggestionChip` | unverified | |
| `SuggestionChip variant="elevated"` | `ElevatedSuggestionChip` | unverified | |
| `ChipGroup` | `Row(horizontalArrangement = FilterChipDefaults.horizontalArrangement)` | unverified | One row that scrolls or wraps. `multi` lets more than one chip be selected; it is state, not a parameter. |
| `Switch` | `Switch` | verified | Compose reserves a 48dp-tall touch box around the 32dp switch; leave that room. |
| `Checkbox` | `Checkbox(checked, onCheckedChange)` in a `Row` with the `label` as `Text` | unverified | Make the whole row the touch target with `Modifier.toggleable` on the `Row`. |
| `Slider` | `Slider(value, onValueChange)` | verified | Track and thumb through `SliderDefaults.Track` and `SliderDefaults.Thumb`. Verified continuous at a mid value: 16dp track, 4x44dp handle, 6dp gaps, and the `SliderState` overload renders the same. The 48dp layout box is the touch target. `range` is `valueRange`, `steps` is `steps`. |
| `RangeSlider` | `RangeSlider` | verified | Same notes as the single slider. |
| `TextField variant="filled"` | `TextField(value, onValueChange, modifier = Modifier.width(280.dp), label = { Text("Label") }, singleLine = true)` | verified | 56dp container, 4dp top corners, 1dp indicator in on surface variant, container surface container highest, value 16/24 in on surface, text 16dp from the edge. Compose's minimum width is 280dp (`TextFieldDefaults.MinWidth`); a full-width field is `Modifier.fillMaxWidth()`. Focus, hover, disabled, and read-only are not verified. |
| `TextField variant="outlined"` | `OutlinedTextField(value, onValueChange, modifier = Modifier.width(280.dp), label = { Text("Label") }, singleLine = true)` | verified | Same measurements as filled: 56dp, 4dp corners, 1dp outline in the outline color. |
| `TextField` with `supporting` | `supportingText = { Text("Supporting text") }` | verified | Both variants: the field is 76dp tall, the supporting text 12/16 in on surface variant, 16dp from the edge. |
| `TextField error=true` | `isError = true`, `supportingText = { Text("Error text") }` | verified | Both variants: the indicator or outline, the label, and the supporting text take the error color. Put the message in `supporting`. |
| `TextField` with `leadingIcon` | `leadingIcon = { Icon(painterResource(R.drawable.search), null) }` | verified | A 24dp icon whose ink starts 15dp from the edge; the label and value follow on the same lines. |
| `TextField` with `trailingIcon` | `trailingIcon = { Icon(painterResource(R.drawable.close), null) }` | verified | A 24dp icon at the right edge. An icon that acts (clear, show password) is an `IconButton` in the app; the proposal's trailing icon is decoration. |
| `TextField` with `prefix` | `prefix = { Text("$") }` | verified | The amount pattern. The prefix appears once the label floats (focused or filled). |
| `TextField variant="filled"` with `placeholder` and no `label` | `TextField(value, onValueChange, placeholder = { Text("Placeholder") })` | verified | Same 56dp container. With a label, Compose shows the placeholder only when focused, so a static proposal uses the label or the placeholder, not both. |
| `TextField variant="outlined"` with `placeholder` and no `label` | `OutlinedTextField(value, onValueChange, placeholder = { Text("Placeholder") })` | verified | |
| `TextField variant="filled" lines=3` | `TextField(value, onValueChange, singleLine = false, minLines = 3)` | verified | Container 104dp, lines at a 24dp pitch. |
| `TextField variant="outlined" lines=3` | `OutlinedTextField(value, onValueChange, singleLine = false, minLines = 3)` | unverified | About 101dp tall, more padding above and below the lines than filled. Use the filled variant for multi-line input. |
| `TopAppBar size="search"` | `AppBarWithSearch(state, inputField, navigationIcon, actions, colors = SearchBarDefaults.appBarWithSearchColors(searchBarColors = colors))` | unverified | The `title` is the field's hint, `navigationIcon` the leading icon button and `actions` the trailing icon buttons, outside the search field, with the `avatar` last. The field and its `expanded` view are written as for `SearchBar`. |
| `LoadingIndicator` | `LoadingIndicator` | unverified | |
| `LoadingIndicator contained=true` | `ContainedLoadingIndicator` | unverified | |
| `ProgressIndicator shape="linear"` | `LinearProgressIndicator` | unverified | `progress` is `progress = { p }`; without it, the indeterminate overload. |
| `ProgressIndicator shape="linear" wavy=true` | `LinearWavyProgressIndicator` | unverified | |
| `ProgressIndicator shape="circular"` | `CircularProgressIndicator` | unverified | |
| `ProgressIndicator shape="circular" wavy=true` | `CircularWavyProgressIndicator` | unverified | |
| `BottomSheet` | `ModalBottomSheet(onDismissRequest, sheetState = rememberModalBottomSheetState(), dragHandle = { BottomSheetDefaults.DragHandle() })` | verified | Container surface container low, 28dp top corners, 32x4dp drag handle in on surface variant, black scrim at 32%. On a wide window it is 640dp wide and centered (`BottomSheetDefaults.SheetMaxWidth`). Content starts 48dp below the sheet top. Only the modal sheet with a handle is verified. |
| `Dialog` | `AlertDialog(onDismissRequest, confirmButton = { TextButton(...) }, dismissButton = { TextButton(...) }, title, text, modifier = Modifier.padding(horizontal = 24.dp).widthIn(max = 560.dp), properties = DialogProperties(usePlatformDefaultWidth = false))` | verified | `confirm` and `dismiss` are the two text buttons, right aligned. The modifier and `usePlatformDefaultWidth = false` pin the width to the window minus 24dp a side, up to 560dp (363dp on a 411dp screen). Container surface container high, 28dp corners, 24dp padding, title 24sp on 32, text 14sp on 20. The text buttons keep a 48dp touch target around the 40dp button, so the dialog is about 6dp taller than its visual content; leave that room. Long scrolling content is not verified. |
| `Menu` with text-only `MenuItem` children | `DropdownMenuPopup(expanded, onDismissRequest) { DropdownMenuGroup(shapes = MenuDefaults.groupShape(0, 1), containerColor = MaterialTheme.colorScheme.surfaceContainer) { items.forEachIndexed { i, text -> DropdownMenuItem(text = { Text(text) }, onClick, shape = MenuDefaults.itemShape(i, items.size).shape) } } }` | verified | `anchor` is the component the popup attaches to. Use this expressive menu, not the older `DropdownMenu`, whose items are 48dp tall with 4dp corners. Three text items: 121 x 148dp, 16dp corners, label large text 16dp from the edge, items at a 48dp pitch. Pass `containerColor`: the group defaults to surface container low. The elevation shadow is not compared. |
| `MenuItem` with `icon` | `DropdownMenuItem` with `leadingIcon` | unverified | |
| `SideSheet` | `Dialog(onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false))` holding a `Box` that closes on a tap outside and a `Surface` aligned `Alignment.CenterEnd` | unverified | No side sheet composable, so it is composed, and the previewer draws the same composition. `Surface`: `widthIn(max = 400.dp)`, `shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)` (the edge that faces the content; the start corners mirror in right-to-left), `color = MaterialTheme.colorScheme.surfaceContainerLow`, 24dp padding (16dp at the start when the header has a back icon button). The header is a `Row` of an optional back `IconButton` (`back` is its action), the `title` in `MaterialTheme.typography.titleLarge` and a close `IconButton`, 12dp apart, all in on surface variant; the content scrolls below it. The close icon button is always drawn, because Material calls it highly recommended. The `Dialog` window provides the scrim. A standard side sheet is not this component: it is a screen with pane role `supporting`. |
| `SearchBar` | `SearchBar(state, inputField, colors)` with `SearchBarDefaults.InputField(textFieldState, searchBarState, onSearch, placeholder, leadingIcon, trailingIcon)` | unverified | The state is `rememberContainedSearchBarState()` (`rememberSearchBarWithGapState()` for a docked view), made at the top of the screen so `open` reaches it; the colors are `SearchBarDefaults.containedColors(state)`. The `leadingIcon` slot shows `navigationIcon` or the `leadingIcon` symbol, and a back icon button once the state is expanded. `trailing` and a 30dp circular `avatar` fill the `trailingIcon` slot. The bar and its view sit in one `Box`, and the input field is a composable lambda both draw. |
| `SearchView layout="full-screen"` | `ExpandedFullScreenContainedSearchBar(state, inputField, colors) { results }` | unverified | The contained style, with no divider. Compose's divided style (`ExpandedFullScreenSearchBar`) is not offered, as Material marks it "not recommended". Opening is `coroutineScope.launch { state.animateToExpanded() }`. |
| `SearchView layout="docked"` | `ExpandedDockedSearchBarWithGap(state, inputField) { results }` | unverified | A dropdown below the bar, with a gap. |
| `Tooltip variant="plain"` | `TooltipBox(positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above), tooltip = { PlainTooltip { Text(text) } }, state = rememberTooltipState()) { anchor }` | unverified | The anchor is the box's content. The tooltip opens below an anchor inside a `TopAppBar` (`TooltipAnchorPosition.Below`). The previewer shows it open, as a persistent tooltip, so a static render does not time out. |
| `Tooltip variant="rich"` | `RichTooltip(title = { Text(title) }, action = { Row { TextButton ... } }) { Text(text) }` in a `TooltipBox(..., state = rememberTooltipState(isPersistent = true), hasAction = true)` | unverified | Opens below its anchor. Up to two text buttons, side by side. `open` and `dismiss` are `coroutineScope.launch { state.show() }` and `state.dismiss()`. |
| `Snackbar` | `Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) })`, with `LaunchedEffect(shown) { snackbarHostState.showSnackbar(message, actionLabel, withDismissAction) }` | unverified | Draws in the Scaffold's snackbar slot, above the FAB and the bottom bar, in the default inverse surface colors. The preview shows it open, like the other overlays; the code posts it when the state it opens by turns true, and clears the state when `showSnackbar` returns. `action` runs when the result is `SnackbarResult.ActionPerformed`. A snackbar with an action label stays until acted on, which is `showSnackbar`'s default; one without times out. `dismiss` is `withDismissAction = true`. |

### Common props and actions

| Catalog | Compose | Notes |
| --- | --- | --- |
| `padding` | `Modifier.padding(16.dp)` | The spacing step's dp value from the design system. |
| `weight` | `Modifier.weight(w)` | Only inside a `Row` or `Column`. |
| `style` | The style's tokens as the component's parameters and modifiers | `type`, `color`, `container`, `shape`, and `padding` each map as in Tokens. Not the experimental Styles API, which material3 components do not accept. |
| `showIn`, `sizeClass` | `BoxWithConstraints` with `maxWidth >= 600.dp` and `maxWidth >= 840.dp` | Or the app's own window size class source. Compact below 600dp, medium to 839dp, expanded from 840dp. |
| `primary` | The one prominent action of the screen | Not a Compose API: carry the emphasis, not an attribute. |
| `contentDescription` | The `contentDescription` parameter, or `Modifier.semantics` | |
| `navigate`, `back` | Add or remove the destination key on the Navigation 3 back stack shown by `NavDisplay` | |
| `navigation.panes` | `ListDetailSceneStrategy` or `SupportingPaneSceneStrategy` with each entry's pane metadata | `list`, `detail`, `extra` belong to the list-detail scene; `supporting` to the supporting-pane scene. |
| `open`, `dismiss` | The `onClick` that sets the sheet, dialog, or menu visible, and the `onDismissRequest` that hides it | The overlay itself is its Components row. |

## Wear OS components

The rows for the `wear` catalog (`get_catalog` with `catalog: "wear"`). Each names the Wear Compose API from `androidx.wear.compose.material3` or `androidx.wear.compose.foundation.lazy`, written in full because the Material 3 inventory above does not list them. All rows are unverified: nobody has rendered them against the app yet. Interactive Wear components need a 48dp touch target.

| Catalog | Compose | Status | Notes |
| --- | --- | --- | --- |
| `Screen` | `androidx.wear.compose.material3.AppScaffold` around `androidx.wear.compose.material3.ScreenScaffold(scrollState, timeText, edgeButton)` | unverified | `scroll=true` (the default) fills a `androidx.wear.compose.foundation.lazy.TransformingLazyColumn` with the `children` as items, using `androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState`, and passes its `contentPadding` through; `scroll=false` puts the `children` in a `Box`. `timeText` is `androidx.wear.compose.material3.TimeText`, `edgeButton` is the `edgeButton` slot of the scaffold. |
| `Column / Row / Box / Spacer` | The foundation layouts of the Components table | unverified | Same mapping as on phone. On a round display keep a fixed layout inside the circle. |
| `ListHeader` | `androidx.wear.compose.material3.ListHeader` | unverified | `text` is the header's `Text`. |
| `ListSubHeader` | `androidx.wear.compose.material3.ListSubHeader` | unverified | `text` is the label slot, `icon` the icon slot. |
| `Button variant="filled"` | `androidx.wear.compose.material3.Button` | unverified | `label`, `secondaryLabel`, `icon` are the `label`, `secondaryLabel` and `icon` slots. Full width in a list. |
| `Button variant="filledTonal"` | `androidx.wear.compose.material3.FilledTonalButton` | unverified |  |
| `Button variant="filledVariant"` | `androidx.wear.compose.material3.Button` with `androidx.wear.compose.material3.ButtonDefaults.filledVariantButtonColors()` | unverified |  |
| `Button variant="outlined"` | `androidx.wear.compose.material3.OutlinedButton` | unverified |  |
| `Button variant="child"` | `androidx.wear.compose.material3.ChildButton` | unverified | The low-emphasis button for a child action of a card or list. |
| `CompactButton` | `androidx.wear.compose.material3.CompactButton` | unverified | `variant` selects the `colors` as for Button. `label` and `icon` are its slots. |
| `IconButton variant="standard"` | `androidx.wear.compose.material3.IconButton` | unverified | `filled` is `FilledIconButton`, `filledTonal` is `FilledTonalIconButton`, `outlined` is `OutlinedIconButton`. `size` is the matching `IconButtonDefaults` button size (extra small, small, default, large). |
| `TextButton` | `androidx.wear.compose.material3.TextButton` | unverified | `variant` other than `standard` uses the matching colors of the button defaults; `size` is its small, default or large button size. |
| `EdgeButton` | `androidx.wear.compose.material3.EdgeButton(buttonSize = androidx.wear.compose.material3.EdgeButtonSize.Small)` | unverified | `size` is the edge button size of the same name (extra small to large). Pass it as the edge button slot of the scaffold, not inside the list. |
| `ButtonGroup` | `androidx.wear.compose.material3.ButtonGroup` | unverified | Children share the width. |
| `IconToggleButton` | `androidx.wear.compose.material3.IconToggleButton(checked, onCheckedChange)` | unverified | `action` is the state change. |
| `TextToggleButton` | `androidx.wear.compose.material3.TextToggleButton(checked, onCheckedChange)` | unverified |  |
| `SwitchButton` | `androidx.wear.compose.material3.SwitchButton(checked, onCheckedChange)` | unverified | `label`, `secondaryLabel` and `icon` are its slots. |
| `CheckboxButton` | `androidx.wear.compose.material3.CheckboxButton(checked, onCheckedChange)` | unverified |  |
| `RadioButton` | `androidx.wear.compose.material3.RadioButton(selected, onSelect)` | unverified |  |
| `Text` | `androidx.wear.compose.material3.Text(text, style = androidx.wear.compose.material3.MaterialTheme.typography.numeralLarge, color = androidx.wear.compose.material3.MaterialTheme.colorScheme.onSurface)` | unverified | `type` and `color` are Wear roles; see the Wear tokens below. |
| `Icon` | `androidx.wear.compose.material3.Icon` | unverified | Same icon handling as the Icons section. |
| `Image` | `androidx.compose.foundation.Image` | unverified | As the Components row. |
| `Card` | `androidx.wear.compose.material3.Card(onClick)` | unverified | The `children` are the card's content. |
| `TitleCard` | `androidx.wear.compose.material3.TitleCard(onClick, title, time, subtitle, content)` | unverified | `body` is `content`. |
| `AppCard` | `androidx.wear.compose.material3.AppCard(onClick, appName, appImage, title, time, content)` | unverified | `appIcon` is `appImage`, `body` is `content`. |
| `Slider` | `androidx.wear.compose.material3.Slider(value, onValueChange, steps)` | unverified | `range` is `valueRange`. |
| `Stepper` | `androidx.wear.compose.material3.Stepper(value, onValueChange, steps)` | unverified | `content` is the `content` slot. |
| `CircularProgressIndicator` | `androidx.wear.compose.material3.CircularProgressIndicator(progress = { p })` | unverified | Without `progress`, the indeterminate overload. |
| `LinearProgressIndicator` | `androidx.wear.compose.material3.LinearProgressIndicator(progress = { p })` | unverified |  |
| `SegmentedCircularProgressIndicator` | `androidx.wear.compose.material3.SegmentedCircularProgressIndicator(segmentCount, progress = { p })` | unverified |  |
| `AlertDialog` | `androidx.wear.compose.material3.AlertDialog(visible, onDismissRequest, confirmButton, dismissButton, title, icon, text)` | unverified | Opened by `open`, closed by `dismiss`; `visible` is the state. |
| `ConfirmationDialog` | `androidx.wear.compose.material3.ConfirmationDialog(visible, onDismissRequest, curvedText)` | unverified | `text` is the `curvedText`, `icon` the content. |

### Wear tokens

| Catalog | Compose | Notes |
| --- | --- | --- |
| Color role `primaryDim` | `androidx.wear.compose.material3.MaterialTheme.colorScheme.primaryDim` | Every Wear color role is the `androidx.wear.compose.material3.ColorScheme` property of the same name. Wear has no `surface`. |
| Type role `numeralLarge` | `androidx.wear.compose.material3.MaterialTheme.typography.numeralLarge` | Display, title, label, body and numeral roles are the `androidx.wear.compose.material3.Typography` property of the same name. |
| `sizeClass` key `large` | A check that the screen width is at least 225dp | Wear's large-display breakpoint. |
| `navigation.panes` | Not used | A watch shows one screen at a time. |

## Tokens

| Catalog | Compose | Notes |
| --- | --- | --- |
| Color role `primaryContainer` | `MaterialTheme.colorScheme.primaryContainer` | Every catalog color role is the `ColorScheme` property of the same name. |
| Type role `bodyLarge` | `MaterialTheme.typography.bodyLarge` | The baseline roles are the `Typography` property of the same name. Emphasized roles: see the table below. |
| Corner `extraSmall` to `extraExtraLarge` | `MaterialTheme.shapes.extraSmall` to `MaterialTheme.shapes.extraExtraLarge` | 4, 8, 12, 16, 20, 28, 32, 48dp by default; a DESIGN.md `rounded` value overrides the matching `Shapes` entry. |
| Corner `none` | `RectangleShape` | |
| Corner `full` | `CircleShape` | |
| Shape polygon `sunny` | `MaterialShapes.Sunny.toShape()` | First letter upper-cased. |
| Spacing name `lg` | `16.dp` | The step's value from DESIGN.md, or the default scale: none 0, xxs 2, xs 4, sm 8, md 12, lg 16, xl 24, xxl 32, xxxl 48. |
| Theme (`colors.primary`, `material.scheme`) | `MaterialExpressiveTheme(colorScheme = rememberDynamicColorScheme(seedColor = Color(0xFFB5523B), isDark, style = PaletteStyle.TonalSpot, contrastLevel = 0.0, specVersion = ColorSpec.SpecVersion.SPEC_2025), motionScheme = MotionScheme.expressive())` | `variant` names the `PaletteStyle` (`tonal-spot` is `PaletteStyle.TonalSpot`, `fruit-salad` is `PaletteStyle.FruitSalad`). A `colors` role override replaces that role with `.copy(...)` on the generated scheme. |

An emphasized type role does not map to `MaterialTheme.typography.<role>Emphasized`: with the pinned material3 (`1.5.0-alpha29`), `Typography()` gives every `*Emphasized` style the baseline role's weight and tracking. Copy the baseline role with Google's emphasized token weight and tracking:

| Catalog | Compose |
| --- | --- |
| `displayLargeEmphasized`, `displayMediumEmphasized`, `displaySmallEmphasized`, `headlineLargeEmphasized`, `headlineMediumEmphasized`, `headlineSmallEmphasized`, `titleLargeEmphasized` | the baseline role `.copy(fontWeight = FontWeight(500), letterSpacing = 0.sp)` |
| `titleMediumEmphasized` | `MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight(700), letterSpacing = 0.15.sp)` |
| `titleSmallEmphasized` | `MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight(700), letterSpacing = 0.1.sp)` |
| `bodyLargeEmphasized` | `MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight(500), letterSpacing = 0.15.sp)` |
| `bodyMediumEmphasized` | `MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight(500), letterSpacing = 0.25.sp)` |
| `bodySmallEmphasized` | `MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight(500), letterSpacing = 0.4.sp)` |
| `labelLargeEmphasized`, `labelMediumEmphasized`, `labelSmallEmphasized` | the baseline role `.copy(fontWeight = FontWeight(700))` |

### Typography from DESIGN.md

A `typography` entry in DESIGN.md changes the face and axes of one role. Build it once as a `TextStyle` that copies the role's size and line height, and pass every axis the entry names: Compose sets only the axes it is given.

| DESIGN.md | Compose | Notes |
| --- | --- | --- |
| `fontVariation` on Google Sans Flex | `MaterialTheme.typography.displayLarge.copy(fontFamily = FontFamily(Font(R.font.google_sans_flex, FontWeight(w), variationSettings = FontVariation.Settings(FontVariation.weight(w), FontVariation.width(wd), FontVariation.opticalSizing(os.sp)))))` | `wght` is `FontVariation.weight`, `wdth` is `FontVariation.width`, `opsz` is `FontVariation.opticalSizing`, and any other axis (ROND) is `FontVariation.Setting("ROND", v)`. Compose never sets `opsz` by itself, so an entry that names `opsz` needs it passed. |
| `fontFamily` naming a bundled face | The same `TextStyle` with `FontFamily(Font(R.font.fraunces, FontWeight(600), variationSettings = FontVariation.Settings(FontVariation.weight(600), FontVariation.opticalSizing(52.sp), FontVariation.Setting("SOFT", 100f), FontVariation.Setting("WONK", 1f))))` | Files and their axes: `fraunces` (opsz, SOFT, WONK), `bricolage_grotesque` (opsz, wdth), `big_shoulders_display` (wght), `doto` (ROND), `google_sans_code` (wght). Copy the faces the design uses from the previewer app's fonts folder (`app/features/preview/desktop/src/main/resources/fonts/` in the repository) into the app's `res/font/`. Measured on Fraunces 600 at 52sp: dropping `opsz` 52 renders 4% wider. |
| `fontWeight`, `letterSpacing` | `fontWeight = FontWeight(w)`, `letterSpacing = 0.5.sp` on the same `copy` | |

## Icons

Both sides use Material Symbols, so no per-icon table exists. Every icon `name` ports as the same Material Symbol: Rounded style, weight 400, grade 0, with Fill 1 when the `Icon` sets `filled` and Fill 0 otherwise (so a toggle's unchecked and checked states are two drawables). Download it from [fonts.google.com/icons](https://fonts.google.com/icons) in the Android format (or add the SVG with Android Studio's Vector Asset) and import it as a vector drawable named after the symbol. Delete the `android:tint="?attr/colorControlNormal"` attribute from the downloaded `<vector>` tag (keep the tag's closing `>`): an app without AppCompat or MDC cannot resolve that attribute, so it fails resource linking, and `Icon` applies the color. Draw it with `Icon(painterResource(R.drawable.name), contentDescription = label)`; the component's `contentDescription` is the `contentDescription`, and without it pass `null`. Leave `tint` at its default. The legacy `Icons.Rounded` set from `material-icons-extended` is frozen and lacks the Fill axis: do not use it.

Inside a component that sizes its own icon (buttons, icon buttons, FABs and FAB menu items, chips, navigation items, menu items, text field icons), the port uses that component's default icon size, which for icon buttons and FABs follows the container size (icon buttons 20, 24, 24, 32, 40; FABs 24, 28, 36). A standalone `Icon`'s `size` is `Modifier.size(N.dp)`; the Compose icon sizes are 18 (`FilterChipDefaults.IconSize`), 20 (`ButtonDefaults.ExtraSmallIconSize`), 24, 28 (`FloatingActionButtonDefaults.MediumIconSize`), 32 (`ButtonDefaults.LargeIconSize`), 36 (`FloatingActionButtonDefaults.LargeIconSize`), 40 (`ButtonDefaults.ExtraLargeIconSize`), and 48.

Directional icons (`arrow_back`, `arrow_forward`, `sort`, `help`, `chat`, `list`, `queue_music`, and any other glyph whose meaning has a direction) must mirror in right-to-left layouts: set `android:autoMirrored="true"` on the drawable's `<vector>` tag.
