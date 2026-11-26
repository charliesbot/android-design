# Components

Which component to use, usage rules from Google's Material 3 guidelines, and the Compose Expressive API (`androidx.compose.material3` 1.5.0 stable). For production Kotlin code examples, read [references/component-snippets.md](component-snippets.md); explorations use `compose-signatures.md`.

## Selection guide

| Need | Component | Compose API |
| --- | --- | --- |
| Page title and 1 or 2 actions | App bar: search, small, medium/large flexible | `MediumFlexibleTopAppBar`, `LargeFlexibleTopAppBar`, `AppBarWithSearch` |
| Page actions or local sub-navigation | Floating or docked toolbar | `HorizontalFloatingToolbar`, `VerticalFloatingToolbar`, `FlexibleBottomAppBar` |
| Single most important action | FAB (small/default, medium, large) | `FloatingActionButton`, `MediumFloatingActionButton`, `LargeFloatingActionButton` |
| Labeled primary action on long scroll | Extended FAB (small, medium, large) | `SmallExtendedFloatingActionButton`, `MediumExtendedFloatingActionButton`, `LargeExtendedFloatingActionButton` |
| 2 to 6 related actions behind FAB | FAB menu | `FloatingActionButtonMenu`, `FloatingActionButtonMenuItem`, `ToggleFloatingActionButton` |
| Discrete actions | Buttons (XS 32 to XL 136dp) | `Button`, `ElevatedButton`, `FilledTonalButton`, `OutlinedButton`, `TextButton` |
| Related buttons reacting together | Standard button group | `ButtonGroup`, `ToggleButton`, `FilledTonalToggleButton`, `ElevatedToggleButton`, `OutlinedToggleButton` |
| View mode or option toggle within screen | Connected button group | `Row` of `ToggleButton` with `ButtonGroupDefaults.connected*ButtonShapes()` |
| Main action plus alternatives | Split button | `SplitButtonLayout` |
| Common icon actions | Icon buttons (XS to XL; narrow/uniform/wide) | `IconButton`, `FilledIconButton`, `FilledTonalIconButton`, `OutlinedIconButton`, `IconToggleButton` |
| Forking filters or entered items | Chips: assist, filter, input, suggestion | `FilterChip`, `ElevatedFilterChip`, `InputChip` |
| Content and actions on one topic | Cards: elevated, filled, outlined | `Card`, `ElevatedCard`, `OutlinedCard` |
| Visual collection | Carousel: multi-browse, uncontained, centered hero | `HorizontalMultiBrowseCarousel`, `HorizontalUncontainedCarousel`, `HorizontalCenteredHeroCarousel` |
| Scannable rows | Expressive list (standard or segmented) | `ListItem(onClick, ...) { headline }`, `SegmentedListItem` |
| Temporary action or choice menu | Vertical menu | `DropdownMenuPopup`, `DropdownMenuGroup`, `DropdownMenuItem`, `SelectableDropdownMenuItem`, `CheckableDropdownMenuItem` |
| 3 to 5 top destinations (phone) | Flexible navigation bar | `ShortNavigationBar`, `ShortNavigationBarItem` |
| Destinations on larger windows | Navigation rail (collapsed or expanded) | `WideNavigationRail`, `WideNavigationRailItem`, `ModalWideNavigationRail`, `NavigationSuiteScaffold` |
| Related content on one hierarchy level | Tabs (primary or secondary) | `PrimaryTabRow`, `SecondaryTabRow`, `PrimaryScrollableTabRow` |
| Supplementary content | Bottom sheet (phone), side sheet (medium+) | `BottomSheet`, `ModalBottomSheet`, `BottomSheetScaffold`, `Scrim` |
| Blocking decision | Dialog | `AlertDialog` |
| Brief feedback | Snackbar | `SnackbarHost`, `Snackbar` |
| Wait of 200ms to 5s / over 5s | Loading indicator / Wavy progress | `LoadingIndicator`, `ContainedLoadingIndicator`, `LinearWavyProgressIndicator`, `CircularWavyProgressIndicator` |
| On/off setting / pick one / pick several | Switch / radio buttons / checkboxes | `Switch`, `RadioButton`, `Checkbox` |
| Value in a range | Slider: standard, vertical, range | `Slider`, `VerticalSlider`, `RangeSlider` |
| Text entry | Text field (filled or outlined) | `TextField`, `OutlinedTextField`, `SecureTextField`, `OutlinedSecureTextField` |
| Search | Search bar or search app bar | `SearchBar`, `ExpandedFullScreenContainedSearchBar`, `ExpandedDockedSearchBarWithGap`, `AppBarWithSearch` |
| Numeric or measurement value | Scroll field | `ScrollField` |
| Time selection | Time picker: scroll, input, dial (vibrant) | `TimeScroll`, `TimeInput`, `TimePicker`, `VibrantTimePickerDialog` |

## Component details

Read the matching subsection below for every component you place.

### States on containers

- Containers stay non-interactive so actionable children own hover and press states: keep `clickable` off app bars, dialogs, menus, sheets, navigation bars, and tabs as a whole.
- Disabled state applies to buttons, cards, checkboxes, chips, list items, radios, switches, and text fields. App bars, dialogs, menus, sheets, navigation, tabs, and FABs are never disabled (hide an unavailable FAB).

### Choosing emphasis across buttons

- Keep one prominent primary button per screen, limiting any single arrangement to three buttons at most (move secondary actions to an overflow menu or icon buttons). Emphasis order: FAB, filled, tonal, elevated, outlined, text; icon buttons: filled, tonal, outlined, standard.
- **Variants:** filled for final/unblocking actions (Save, Confirm; one per screen); tonal for supporting actions that need weight beyond an outline (Next); outlined for medium emphasis or backing out; text for lowest-priority actions inside cards, dialogs, and snackbars; elevated only to separate from a patterned background. Step pairs down (filled beside text or outlined; outlined beside text) and place side by side when space allows.
- **Labels and sizing:** 1 to 3 words, sentence case, icon leading, single line without truncation. Give the container enough room or step down across breakpoints (`ButtonDefaults.ExtraSmallContainerHeight` 32dp, `MinHeight` 40dp, `MediumContainerHeight` 56dp, `LargeContainerHeight` 96dp, `ExtraLargeContainerHeight` 136dp); pair `Button(shapes = ButtonDefaults.shapesFor(h), contentPadding = ButtonDefaults.contentPaddingFor(h))` with `iconSizeFor(h)` and `textStyleFor(h)`. Keep buttons compact rather than stretching into long flat bars on large screens.
- **Toggle buttons:** for binary selections (Save, Favorite) via `ToggleButton`, `ElevatedToggleButton`, `FilledTonalToggleButton`, or `OutlinedToggleButton` (`ToggleButtonSize`, `ToggleButtonDefaults.shapesFor(...)`, and each variant's `*ToggleButtonDefaults.colors(...)`). Outlined icon when off, filled when on, plus shape morphing.

### Icon buttons

- Use for common actions with clear system icons; group dense sets inside a toolbar or button group. Keep equally important buttons the same size, using size (extraSmall to extraLarge) and width (`IconButtonDefaults.IconButtonWidthOption.Narrow`, `Uniform`, `Wide`, via `Modifier.size(IconButtonDefaults.smallContainerSize(...))`) for hierarchy.
- `IconButton(shapes = IconButtonDefaults.shapes())` and filled/tonal/outlined variants: filled sparingly for a key action, tonal beside a filled action, outlined for medium emphasis, standard for low emphasis or colorful surfaces. Use small on app bars; reserve medium and up for hero controls.
- Use `IconToggleButton(checked, onCheckedChange, shapes = IconButtonDefaults.toggleableShapes())` (and filled/tonal/outlined variants with `iconToggleButtonColors(...)` / `filledIconToggleButtonColors(...)`) only for binary states, never for overflow, swapping to a filled icon when checked.

### Button groups and split button

- **Standard group:** `ButtonGroup(overflowIndicator = { ButtonGroupDefaults.OverflowIndicator(it) })` with `clickableItem`, `toggleableItem`, `customItem`, and `Modifier.animateWidth(interactionSource)`. Keep same size and shape by default (mixed sizes only in hero moments; different shape only for selection/meaning). Use filled, tonal, outlined, or elevated buttons so items keep container treatment.
- **Connected group:** `Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween))` of `ToggleButton`s using `ButtonGroupDefaults.connectedLeadingButtonShapes()`, `connectedMiddleButtonShapes()`, and `connectedTrailingButtonShapes()` to select options, switch views, or sort. Require every item to be toggleable, use one color style per group, and keep on a single line without wrapping.
- **Split button:** `SplitButtonLayout` with `SplitButtonDefaults.LeadingButton` and `TrailingButton(checked, onCheckedChange)` (plus tonal, outlined, elevated variants). Keep labels to 1 to 2 words, menu aligned 4dp below trailing half with the menu icon.

### FAB, extended FAB, FAB menu

- One per screen for the top constructive action (create, share, start); hide when unavailable. Stay put on scroll; bottom half on compact/medium (above any nav bar), in the rail on expanded. Medium is default on phones, large on tablets.
- **Extended FAB:** `SmallExtendedFloatingActionButton`, `MediumExtendedFloatingActionButton`, `LargeExtendedFloatingActionButton` for long scrolling screens where a label helps (keep off the top half of phones, cards, and toolbars); collapse to a FAB on scroll down.
- **FAB menu:** `FloatingActionButtonMenu`, `FloatingActionButtonMenuItem`, and `ToggleFloatingActionButton` with `Modifier.animateIcon({ checkedProgress })` for 2 to 6 closely related actions from a regular FAB (never with a floating toolbar or navigation rail).

### App bars

- `MediumFlexibleTopAppBar` and `LargeFlexibleTopAppBar` (`subtitle`, `titleHorizontalAlignment`; `TopAppBarDefaults.exitUntilCollapsedScrollBehavior()`, `pinnedScrollBehavior(scrollableState)`, `enterAlwaysScrollBehavior(scrollableState)` with `isScrollingContentAtStart` for custom scrollables) emphasize the headline and compress to small on scroll; use small for dense layouts or scrolled pages.
- Title (start-aligned or centered, wrapping up to two lines only in flexible bars) plus 1 essential action (at most 2) that alters or exits the page (Send, Save, Edit). Boost the key action with one filled or tonal (optionally wide) icon button, keeping filled icons and moving extra actions to a toolbar. Keep corners straight and height at or above default; fill with contrasting color on scroll or float filled icon buttons over content.

### Toolbars

Decide the toolbar's job by what its controls do:

- **Actions for the current page** (open something or act on the page: lyrics, queue, share, edit): fill with icon buttons for an even hierarchy, naming each with a plain tooltip and content description. Controls that show/hide a panel use toggle icon buttons so the open one shows selected state.
- **Local navigation** (swapping body content in place between related subsequent pages such as Favorites, Saved, Library): use labeled items with exactly one selected tonal pill, never alongside a navigation bar.

Rules for both (`HorizontalFloatingToolbar`, `VerticalFloatingToolbar`, `FlexibleBottomAppBar`):

- Slots hold icon buttons, buttons, text fields, images, or custom controls, keeping one control design per toolbar. Ensure each labeled button reads as its own control rather than merging with the toolbar's fill.
- Emphasize one action at most (filled icon button, contrasting color role, wide icon button, or paired `FloatingToolbarDefaults.VibrantFloatingActionButton` / `StandardFloatingActionButton`; never a bold button plus a FAB).
- Standard colors (`standardFloatingToolbarColors()`) focus attention on content; vibrant (`vibrantFloatingToolbarColors()`, `primaryContainer`) emphasizes controls or signals editing mode. Selected toggles use toggle tonal style (`secondaryContainer` in standard, `surfaceContainer` in vibrant).
- **Floating:** horizontal on phones sized to its content with an overflow menu for extras, at least `FloatingToolbarDefaults.ScreenOffset` (16dp compact, 24dp vertical) from edges, avoiding square filled icon buttons. Vertical mainly on larger windows opposite the navigation rail (on compact only for simple screens with few controls). One toolbar per compact window; may pair with a regular FAB (never extended FAB or FAB menu). On scroll (`exitAlwaysScrollBehavior(FloatingToolbarExitDirection.Bottom)` + `Modifier.floatingToolbarVerticalNestedScroll(...)`), stay, slide off, or collapse to one key action, never both.
- **Docked:** `FlexibleBottomAppBar`, full width with straight corners at the bottom for persistent global actions across pages (replaces bottom app bar).

### Navigation bar and rail

- **Bar:** `ShortNavigationBar` + `ShortNavigationBarItem(iconPosition = NavigationItemIconPosition.Top)` (`Start` on medium windows) for 3 to 5 top destinations on compact/small tablets (use tabs for fewer than 3; omit on desktop). Always show 1 to 2 word single-line labels, fixed positions, filled icon for active; keep FAB above, never over, the bar; skip swiping between destinations and scroll to top on reselect.
- **Rail:** `WideNavigationRail(state = rememberWideNavigationRailState())`, `WideNavigationRailItem(railExpanded = ...)`, `ModalWideNavigationRail` for 3 to 7 destinations on medium+ windows, leading edge outside panes (expanded rail replaces the deprecated navigation drawer). Use `NavigationSuiteScaffold` to swap bar and rail automatically; keep one navigation component per screen.

### Tabs

- `PrimaryTabRow` (under app bar) and `SecondaryTabRow` (inside content) organize peer content at the same hierarchy level (never sequential steps). Keep at most 4 fixed tabs, switching to scrollable tabs (52dp first-tab offset) for long or numerous labels; place icons on all tabs or none. Attached tabs move with their app bar; avoid swipeable content inside tabbed pages.

### Cards and carousel

- Elevated, filled, and outlined cards differ in style only and hold content + actions on a single topic. Skip cards when spacing, headings, or dividers create a simpler hierarchy; consider lists on compact screens. Make a card either actionable itself or container for child actions (never both), with no internal scroll or swipeable content.
- **Carousel:** `HorizontalMultiBrowseCarousel`, `HorizontalUncontainedCarousel`, `HorizontalCenteredHeroCarousel` (`rememberCarouselState`, `preferredItemWidth` replacing `maxItemWidth`, `CarouselDefaults.ItemSpacing`, `CarouselDefaults.ContentPadding`, `Modifier.carouselParallaxScrollEffect` with `@ExperimentalMaterial3Api`). Snap scrolling except uncontained; at most 3 items with text on compact; buttons above or below (never on or beside); offer "Show all." Adapt text by item size (large: full title + label; medium: label only; small: brief label or none).

### Dialogs and sheets

- **Dialogs:** rare, for critical tasks or decisions (use snackbars for low-priority messages). At most two actions, confirm at trailing edge, action-naming verbs ("Send", "Create", "Delete" rather than "OK", "Done", "Close"), never apologize or ask "Are you sure?". Full-screen dialogs only on compact for input-heavy flows: close icon + enabled "Save" action (show all errors together; prompt "Discard?" in a basic dialog on dirty close).
- **Sheets & scrim:** `BottomSheet` (`@ExperimentalMaterial3Api` static non-modal; place after `Scrim` as a sibling filling the screen, omitting `Modifier.align(Alignment.BottomCenter)` which pushes it offscreen), `ModalBottomSheet`, `BottomSheetScaffold`, and `Scrim` with `rememberBottomSheetState(...)`. Drag handle cycles heights; scrim tap closes. Side sheets hold optional content on medium+ with a close button.

### Chips

- `FilterChip(shapes = FilterChipDefaults.shapes())`, `ElevatedFilterChip`, `InputChip(shapes = InputChipDefaults.shapes())` (`contentPadding`, `horizontalArrangement`) represent forking paths (never Save/Cancel or lone single chips). Assist starts with a verb, filter names inclusions, input holds user entries, suggestion holds recommendations. Keep all chip sets on a page consistently single- or multi-select, non-elevated, with labels <= 20 chars wrapping or scrolling in one row.

### Selection controls, sliders, text fields

- **Selection:** checkboxes for multi-select lists; vertical radio buttons for 1 of 5 or fewer (one always selected); switches for immediate standalone settings (use a connected button group for opposing view modes, never a switch as a button).
- **Sliders:** `Slider(state = remember { SliderState(value, steps, trackRange = 0f..1f) }, onValueChange = { ... })`, `VerticalSlider`, and `RangeSlider(state = rememberRangeSliderState(startValue, endValue), onValueChange = { ... })` take effect immediately and show the full range (stateless `value =` overloads are deprecated in `1.5.0`; `trackRange` replaces `valueRange`, `startValue`/`endValue` replace `activeRangeStart`/`activeRangeEnd`). Put custom tracks (wavy seek bar, inset icons) in the `track = { ... }` slot; no XS to XL presets; reserve huge prominent sliders for hero moments.
- **Text fields:** build on `TextFieldState` (`rememberTextFieldState()`) with `InputTransformation` / `OutputTransformation` (`BasicTextField` value overloads and `VisualTransformation` are deprecated from Compose 1.13). Expressive styling: `TextFieldDefaults.roundedShape` + `TextFieldDefaults.tonalColors()`; outlined adds `labelPosition = TextFieldLabelPosition.Inside()` (`OutlinedTextFieldDefaults`). Passwords use `SecureTextField` / `OutlinedSecureTextField`. Keep one field variant (filled or outlined) per form, visible non-truncating labels, error text replacing supporting text, `keyboardOptions` (`ImeAction.Next` then `Done`), autofill `contentType`, and bounded widths on large screens.

### Feedback and status

- **Loading & progress:** `LoadingIndicator` / `ContainedLoadingIndicator` (`@ExperimentalMaterial3ExpressiveApi`, also pull-to-refresh) for 200ms to 5s waits (nothing under 200ms, one indicator per group; use contained over content); `LinearWavyProgressIndicator` / `CircularWavyProgressIndicator` (`amplitude`, `wavelength`, `waveSpeed`) over 5s. Keep default wavy strokes (thick custom strokes on small circles deform the wave; scale indicator size instead).
- **Snackbar:** one at a time, at most one action ("Undo"), no icons/links, above FAB and clear of navigation.
- **Tooltips:** `TooltipBox`, `PlainTooltip`, `RichTooltip` (`@ExperimentalMaterial3Api`, `caretShape = TooltipDefaults.caretShape()`, `TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.*)`) label icon-only buttons without hiding critical info.
- **Badges:** upper trailing corner of icons, error colors, <= 4 chars ("999+").
- **Dividers:** group sections only when open space cannot; avoid separating individual items.

### Lists

- Use `ListItem(onClick, ...) { headline }` (`headlineContent =` is deprecated; `verticalAlignment = ListItemDefaults.verticalAlignment()`) or `SegmentedListItem(shapes = ListItemDefaults.segmentedShapes(i, n))` spaced by `ListItemDefaults.SegmentedGap`.
- Align leading visuals (at leading edge, never mid-row) and primary text consistently. Use circular or `MaterialShapes` avatars for people, square/rectangular images for products/videos (which may supply content-based container color).
- Supporting text: 1 to 3 lines; trailing text for price/count/date. Primary action takes most of the row; secondary actions (bookmark, overflow) sit trailing. Show selection with two cues (checkmark + fill).
- **One selection mode per list:** single-action rows have no nested actions or persistent selection; single-select uses radios (or `Modifier.selectable(role = Role.RadioButton)`) with no nested actions; multi-select uses `SegmentedListItem`'s `checked` overload, `Modifier.toggleable(role = Role.Checkbox)`, or switch semantics. Switch settings rows use `SegmentedListItem(onClick = ...)` with trailing `Switch(checked, onCheckedChange = null)` and `Modifier.semantics { role = Role.Switch; toggleableState = ToggleableState(checked) }`.
- Full-width row controls (connected button group, slider, preview) go below the label inside the same segmented item (not in `trailingContent`). Set `colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)` on `surface` pages so items stand off their background.
- Swipe reveals mixed-style buttons with primary last (full swipe triggers it) alongside an overflow alternative. Adapt edge-to-edge phone lists to side-by-side list-detail (or cards/carousel) on medium+.

### Menus

- `DropdownMenuPopup` + `DropdownMenuGroup(shapes = MenuDefaults.groupShape(i, n))` with `DropdownMenuItem(onClick, text, shape)` (actions), `SelectableDropdownMenuItem` (single-choice), and `CheckableDropdownMenuItem` (multi-choice), using `MenuDefaults.itemShape(i, n)` and nesting `DropdownMenuPopup` with `MenuDefaults.rememberDropdownMenuPopupPositionProvider(...)` for submenus.
- Keep persistent actions in a toolbar; show unavailable items disabled (focusable, not selectable) rather than hiding them. Group with 1 to 2 fixed gaps (or dividers in scrolling menus); use vibrant colors (`itemVibrantColors()`, `selectableItemVibrantColors()`, `groupVibrantContainerColor`) sparingly. Slots hold simple content only (never buttons or switches). Keep trigger pressed while open; close on single choice/action, stay open on multi-select; swap long compact menus to bottom sheets while keeping menus in context on large screens.

### Search

- Entry point by prominence: `AppBarWithSearch` (`@ExperimentalMaterial3Api`) when primary, `SearchBar` (`rememberContainedSearchBarState()`, `SearchBarDefaults.containedColors(state)`, `SearchBarDefaults.InputField(textFieldState = rememberTextFieldState(), searchBarState = state)`) below title for one view, search icon button when secondary.
- Contained style uses `surfaceContainerHigh` on `surface` (never `surfaceContainer`) with 24dp unfocused / 12dp focused margins and <= 720dp max width. Leading nav icon or non-functional search icon; at most two trailing icons (voice, avatar, overflow); domain-specific hint ("Search your messages").
- Group results with leading icons, category labels, avatars, filter chips, and gaps. Use `ExpandedFullScreenContainedSearchBar` on compact and `ExpandedDockedSearchBarWithGap` on medium+, keeping query visible, labeling "Results" with a screen-reader live region, and resetting on Back.

### Time pickers and scroll fields

- **Scroll fields:** `ScrollField(state = rememberScrollFieldState(itemCount, index), contentDescription = ...)` (`ScrollFieldDefaults.colors()`, `.shape`, `.ScrollFieldHeight`) for numbers/measurements up to 4 digits (small 100dp, medium 144dp, large 186dp min width). Place inline when possible, slightly cropping inactive values (`>= 48dp` targets, `>= 16dp` corner radius), with a nearby keyboard input toggle.
- **Time pickers (Expressive vibrant style):** prefer inline `TimeScroll(state, shapes, toggle, colors = TimePickerDefaults.vibrantColors())` or `TimeInput` over modal `VibrantTimePickerDialog` / `TimePicker` (use button groups or menus for preset slots). Always supply the keyboard input toggle (`TimePickerDialogDefaults.ScrollDisplayModeToggle(...)` or `DisplayModeToggle(...)`); default 24-hour locales to digital input with dial available; swap vertical/horizontal dial across breakpoints and fall back to scroll/input on tight heights so dials never scroll or clip.

## Patterns from shipped Expressive apps

Use these only after choosing your composition when a pattern serves the screen's direction:

- **Metric card:** small label over a huge emphasized number in a tinted container.
- **Editorial header:** Display or custom-width title dominating the top of a screen or sitting over a full-bleed photo.
- **Shape-masked media:** album art, avatars, and badges clipped to `MaterialShapes` (`Cookie`, `Flower`, `Sunny`, `Clover`).
- **Pill hero control:** full-width pill primary action beside smaller round secondary buttons (Play with skip; Pause with Stop and Restart).
- **Split action pair:** two large half-width contrasting pills (Snooze and Stop) or wide pill beside round button.
- **Floating pill toolbar:** bottom page actions or local navigation (tonal pill on selected nav item).
- **Segmented settings:** grouped segmented list items with leading icons in tonal circles and check/close `Switch(thumbContent = ...)`.
- **Wavy progress:** linear wavy for playback/goals, circular wavy for countdowns.

### Worked example: Aura breathing app

Google's case study allocates **emphasis budget** by goal rank across four screens:

| Screen | Rank | Emphasis |
| --- | --- | --- |
| Home | Primary: start a session | Extra-large "Start breathing" button in dark `primary` on soft surface, low on screen. Settings grouped as list items in `secondary`. Daily message in a soft container, least emphasis. |
| Session | Hero moment | Screen-filling `MaterialShapes.Flower` and `Sunny` grow/shrink on spring motion; vibrant yellow on inhale; huge countdown numerals against small labels; small pause/stop at bottom; navigation bar hidden. |
| Report | Secondary | Fewer tactics to reduce load: metrics in uniform flower shapes, emphasized numbers, medium Finish button. |
| Progress | Tertiary | Subtle: key data in `primary`, completed days as small secondary shapes, scannable numbers that do not dominate. |

## Customize without forking

Keep Material's accessible behavior and layer brand identity in order: (1) theme tokens, (2) wrapper composables with preset parameters, (3) `colors`/`shapes`/Styles API, (4) fork only as a last resort. Brand examples add one touch (custom button shadow/gradient, reward completion animation, or oversized graphic) while keeping behavior intact.

## Icons

Use Material Symbols in one style (outlined, rounded, or sharp) app-wide:

1. Download Android vector drawable XMLs from [fonts.google.com/icons](https://fonts.google.com/icons) (Fill 0 default, Fill 1 selected).
2. Remove `android:tint="?attr/colorControlNormal"` from `<vector>` (keeping `>`) so Compose `Icon` applies color.
3. Draw with `Icon(painterResource(R.drawable.ic_favorite), contentDescription = "Like", tint = ...)`, leaving `tint` at `LocalContentColor` inside components so **role pairs** stay intact.

Treat `material-icons-extended` (frozen at 1.7.8) as prototype-only; production code uses Material Symbols drawables. Read [references/component-snippets.md](component-snippets.md) when writing production Compose code.
