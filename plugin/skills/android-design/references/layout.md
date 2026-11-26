# Layout

Adaptive layout for phones, foldables, tablets, and desktop windows, from Material's layout guidance. For Compose adaptive APIs in depth (MediaQuery, Grid, FlexBox, Navigation 3 scenes), also load `adaptive` and `navigation-3`.

## Breakpoints

Design for window size, not devices; orientation, folding, split screen, and free-form windows change it at runtime. Apply **reveal, not stretch**: larger windows show more panes, navigation, or detail rather than wider copies of phone UI.

**Height counts too.** A window under 480dp tall (a phone or open flippable in landscape) is compact height and keeps one pane even at medium width.

| Breakpoint | Width | Panes | Navigation pattern | Margins |
| --- | --- | --- | --- | --- |
| Compact | under 600dp | 1 | Navigation bar (or modal expanded rail) | 16dp |
| Medium | 600 to 839dp | 1 recommended; 2 only for low-density content, 50/50 | Collapsed rail with 1 pane; horizontal nav bar with 2 panes | 24dp, 24dp spacer |
| Expanded | 840 to 1199dp | 2 recommended | Collapsed or expanded rail | 24dp, 24dp spacer |
| Large | 1200 to 1599dp | 2 recommended | Collapsed or expanded rail | 24dp, 24dp spacer |
| Extra large | 1600dp+ | 2 (3 with a side sheet) | Expanded rail | 24dp, 24dp spacer |

Moving up a breakpoint, ask five questions (reflowing controls and revealing content on control-based layouts like media players):

1. **Reveal:** what hidden UI can appear (expanded rail, second pane, more list detail)?
2. **Divide:** one pane or two?
3. **Resize:** cards, feeds, lists, keeping text at 40 to 60 characters per line.
4. **Reposition:** move actions from the bottom to the leading edge, add negative space, and keep controls reachable (avoid the top 25% of a tablet held in landscape).
5. **Swap:** nav bar to rail, bottom sheet to menu or side sheet, full-screen dialog to basic dialog, FAB to extended FAB. Only swap functionally equivalent components.

**Use the height of a tall window** (medium portrait, such as an open foldable or upright tablet):

- Keep one pane by default (two only for low-density content) and grow the compact vertical order into the height: enlarge hero art or cards, show more list rows, and place supporting content (up next, details) below the primary content.
- Keep the primary action visible with the strongest emphasis; scale imagery down rather than pushing the primary action off screen.
- Constrain aspect-ratio imagery and balance empty space evenly around it instead of leaving a single empty block below, and give every block a max width so nothing stretches wider or taller than its content needs.

## Scaffold

- **Bars** frame the page (top app bar, bottom navigation bar) next to system safety regions (handle insets; see `edge-to-edge`).
- **Rails** surround panes: on compact screens the bottom rail region above the nav bar holds toolbars, chat inputs, and FABs; on large screens the leading rail holds navigation and the trailing rail holds page controls (a vertical floating toolbar).
- **Panes fill the window:** 1 to 3 panes, at least one flexible; fixed panes are 360dp (expanded) or 412dp (large and up), with the split spacer visually centered even when a rail is present. In Compose, lay panes in `Row(Modifier.fillMaxSize())` with `Modifier.weight(...)` or fixed width and `Modifier.fillMaxHeight()`, scrolling lists inside their own pane (`LazyColumn`/`LazyRow`) and scaling hero art with the pane.
- **Reveal, not stretch inside panes:** panes fill height, while cards, text fields, buttons, tabs, and single-pane containers keep content height and a max width (`Modifier.widthIn(max = ...)`). On expanded widths and up, give a list pane a fixed width beside a flexible detail pane; an unselected detail pane shows an empty state.
- Panes display **co-planar** (side by side), **floating** (dialog), or **docked** (bottom sheet), and adapt by showing/hiding, levitating, or reflowing. Blend panes into the background or distinguish the primary pane with surface roles (`surfaceBright` active pane beside a `surfaceDim` rail).

## Canonical layouts

- **Feed:** card grid; one column on compact, more columns as width grows, with lead items spanning columns for hierarchy.
- **List-detail:** one pane on compact, two from expanded. Show selection state only in two-pane mode and back button only in single-pane mode; show a detail placeholder when nothing is selected. Navigation 3: `ListDetailSceneStrategy` with `ListDetailScene.ListPane` and `ListDetailScene.DetailPane`.
- **Supporting pane:** primary content about two-thirds; supporting content sits below on compact/medium (or in a compact bottom sheet) and beside it at 360dp on expanded (412dp on large+). Use `SupportingPaneSceneStrategy` instead of hand-splitting percentages, and reserve it for secondary content tied to the primary view (use list-detail for parent-child).
- **Pane-count transitions:** returning to one pane shows the view last active ("If a layout showed the list view previously, it should return to that view"). Preserve read/unread state, entered text, playback, and scroll position across rotation, fold, and resize; measure window size classes at runtime rather than caching or hardcoding dimensions.

## Navigation by size

- **Compact:** `ShortNavigationBar` with `NavigationItemIconPosition.Top` (or `ModalWideNavigationRail` when more than five destinations are needed).
- **Medium:** `ShortNavigationBar` with `NavigationItemIconPosition.Start` for two-pane layouts, or a collapsed `WideNavigationRail` for one pane.
- **Expanded and up:** `WideNavigationRail` (collapsed or expanded, replacing the navigation drawer).
- **Adaptive switcher:** `NavigationSuiteScaffold` switches automatically (bar on compact width/height or tabletop posture, rail otherwise). Always keep one navigation component per screen.
- **Window measurement:** read `currentWindowAdaptiveInfoV2()` (the older `currentWindowAdaptiveInfo()` requires `supportLargeAndXLargeWidth = true` for large/extra large).

## Density and text

- Density is a user choice, never an automatic breakpoint change; keep menus, dialogs, and snackbars at standard density.

## Grids and max widths

- Compact uses a 4-column grid. Pick hierarchical (editorial/detail), modular (equal items), or column (one-direction flow) grids by content.
- **Compose 1.13 layout primitives:** use `Grid` for 2D structure (give lazy-list tracks `GridTrackSize.MinMax(0.dp, 1.fr)`), and `FlexBox(config = { wrap(FlexWrap.Wrap); gap(8.dp) })` for small wrapping sets of chips or badges (`FlexBox` defaults to `NoWrap`).
- 8dp grid for layout and components; 4dp for icons, type, and small elements.
- Image ratios: 16:9, 3:2, 4:3, 1:1, 3:4, 2:3; decide per breakpoint whether images keep ratio, switch ratio, or hold fixed height.
- **Keep context and pinned controls:** small menus and edit modals stay compact rather than filling large windows, with context menus anchored near the selected item. Pin critical actions and inputs (FAB, message field) instead of letting them scroll away; non-scrolling portrait content stays non-scrolling in landscape.

## Postures and landscape

- Landscape phones (medium width, compact height): one pane with a rail or horizontal navigation bar items.
- Tabletop (half-folded): content on top half, large controls on bottom (video, calls). Keep text, controls, dialogs, and pop-up menus clear of the hinge and widen the hinge gutter.
- Book posture (vertical fold): split reading content left and right.
- Cover screens: hero art background, one big anchor control, optional rail.

## Desktop windows and input

"More screen, more done": denser, not bigger. Treat touch, mouse, and keyboard as equal inputs.

- **Scale:** step type up one or two sizes for viewing distance; keep copy around 60 characters; reveal more with progressive disclosure. Use faster motion for small nearby changes and simpler/slower motion for large movements.
- **Single pane and sheets:** cap and center single-pane desktop content width. Replace bottom sheets on desktop with right-click context menus, scrimless side sheets (tools/config), or dialogs (modal decisions); detach any remaining bottom sheet from the bottom edge.
- **Pointer:** every primary journey works with left click alone; right-click opens context menus. Show hover state, tooltips, selection checkboxes, and carousel prev/next buttons (plus persistent "Show all") on hover without relying on hover alone. Show appropriate cursor icons (including prohibitive over invalid drop targets), translucent drag feedback, and a single-click upload alternative. Tighter secondary hover targets are acceptable for precise pointers.
- **Keyboard:** Tab follows reading order, arrows move inside components, Escape closes temporary UI (clearing search, deselecting, or canceling edits), initial focus lands on the key element, and focus remains visible. Standard shortcuts: Enter sends, Space toggles media playback, Ctrl/Cmd + click, Shift + click, Ctrl/Cmd + A, Ctrl/Cmd + R (with a refresh button), Ctrl/Cmd + scroll (pan-first zoom; plain scroll on zoom-first maps) plus `+`/`-` buttons; list shortcuts in the Keyboard Shortcuts Helper.
- **States and windows:** custom interactive elements need visible hover and focus states with accessible contrast; show scrollbars during mouse/trackpad scroll. Support multi-window instances, cross-app drag and drop, PiP for playback, and custom header-bar nav/search where helpful.

| App type | Large-window pattern |
| --- | --- |
| Media | Details and related items beside the player; browse while playing; tabletop viewing; PiP |
| Reading | Two-page spread on foldables; comfortable line length; collapsible notes pane |
| Shopping | Filters in a supporting pane; product detail beside the list |
| Social and chat | List-detail conversations; comments in a supporting pane; Enter sends |
| Creative tools | Movable palettes; context menus; stylus hover, tilt, and pressure |
