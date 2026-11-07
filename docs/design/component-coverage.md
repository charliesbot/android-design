# Component coverage

**Date:** 2026-11-04

## TL;DR

The previewer draws 24 of the 36 components Material 3 lists at m3.material.io/components. Ten are missing, six covered ones lack variants the skill recommends, and two are left out on purpose because Material retired them. Every gap maps to real material3 composables, present in both the Android build the evals port to and the desktop build the app renders with, so every gap closes the same way the catalog was built: one registry entry, rendered and exported from that entry. Where Material has no single composable (modal side sheet, full-screen dialog, full-screen date picker), the component is composed from real ones, and the exporter emits the same composition. Coverage stays current through the registry itself: each component names the Material page it implements, each omission records why, and a test fails when Material publishes a component page that neither claims.

## The audit

The audit compares the 36 component pages in `sources/m3.material.io/pages/components/` with the registry in `app/core/model/.../Catalog.kt`. A refetch on 2026-11-04 (`fetch.py --only m3`) found no component page changes since the September fetch. Compose availability was checked in both material3 builds the project uses: the Android inventory (`scripts/material3-inventory.txt`, material3 1.5.0-alpha29) and the desktop jar (`org.jetbrains.compose.material3:material3-desktop:1.13.0-beta01`).

| Status | Components |
|---|---|
| Covered | Buttons, button groups (standard and connected), split button, icon buttons, FAB, extended FAB, FAB menu, app bars (except the search app bar), bottom sheets, cards, chips, divider, lists, loading indicator, navigation bar, navigation rail, progress indicators, radio buttons, text fields, toolbars (floating and docked), sliders (including centered and vertical), checkbox (including indeterminate), switch (including thumb icons), menus (groups, vibrant colors, selectable items) |
| Covered, variants missing | Dialogs (full-screen) |
| Missing | Tabs, search, carousel, snackbar, badges, tooltips, date pickers, time pickers, side sheets |
| Left out on purpose | Navigation drawer, segmented buttons |

Both omissions are Material's own retirements:

- **Navigation drawer:** the M3 Expressive update says it "is no longer recommended" and points to the expanded navigation rail, which the catalog has (`NavigationRail` with `expanded`).
- **Segmented buttons:** replaced by the connected button group, which the catalog has (`ButtonGroup` with `variant: connected`).

## Rules every addition follows

- **Real composables only.** A component renders with the material3 composables its `compose` field names, never a lookalike, per the fidelity bar in `visual-design-exploration-v3.md`. When Compose has no single composable for a Material component, the component is composed from real ones, as the connected button group already is (a `Row` of `ToggleButton`s with connected shapes). The renderer and the exporter use the same composition, so the port matches the preview exactly. Three new components are composed: the modal side sheet, the full-screen dialog and the full-screen date picker.
- **One entry, every path.** A component exists once, as a `ComponentDef`. The renderer, the exporter, the schemas in `app/schemas/`, `check_proposal` and `get_tree` all read that entry, and `compose-mapping.md` gets its row. No path special-cases a component the registry does not describe.
- **Variants are props, families are components.** When Material's variants share one Compose family and placement rules, they are a `variant` (or similar) prop, as `Button` and `Card` do. A variant becomes its own component only when its composable, slot or placement rules differ, which is why the docked toolbar is not a prop of `FloatingToolbar`.
- **State starts from a prop.** A control's state (selected, checked, expanded, picked date) starts at a proposal prop and then lives in the window's interaction state, like `checked` on `Switch` today. A static render always shows the prop's value.
- **Transient surfaces are overlays.** Anything that appears on top of the screen and closes again (snackbar, tooltip, expanded search, pickers, modal side sheet) is `overlay = true`: closed until an `open(id)` action, and openable for review through the view state, like `BottomSheet`, `Dialog` and `Menu`. Anchored overlays reuse the `Menu.anchor` mechanism in `OverlayPlacement`, so the renderer and the exporter place them the same way.

## Missing components

### Tabs

`Tabs` holds `Tab` children, with `variant: primary | secondary` and `scrollable`. They export to `PrimaryTabRow`, `SecondaryTabRow` and their scrollable forms, and each `Tab` to `Tab`. `Tab` takes `label`, `icon`, `selected`, `badge` and an `action`, mirroring `NavigationBarItem`. A tab's content is another screen reached with `navigate`, not a child of the row, because that is how the catalog already models destinations. Tabs swapping content in place would need a second content model for one component.

### Search

Search has two parts, matching Compose's split between the collapsed bar and its expanded view:

- `SearchBar` is the inline bar: `placeholder`, leading and trailing icons, and an optional `expanded` child naming a `SearchView` overlay, the same way `SplitButton.menu` names its `Menu`. Tapping the bar opens it.
- `SearchView` is the overlay: `layout: docked | full-screen` and result children. It exports to `ExpandedDockedSearchBarWithGap` or `ExpandedFullScreenContainedSearchBar`, which are the contained style; the plain `ExpandedDockedSearchBar` and `ExpandedFullScreenSearchBar` are the divided style, which Material marks "Not recommended", so it is not offered.

The search app bar is a new `TopAppBar` size, `search`, exporting to `AppBarWithSearch`, because it occupies the app bar slot and follows app bar rules (up to two trailing icons plus an avatar on compact widths, up to four from medium up).

### Carousel

`Carousel` takes `variant: multi-browse | hero | centered-hero | uncontained | full-screen`, an item size in dp where the variant needs one, and children, usually a template bound to the data model. Each child is clipped with `Modifier.maskClip` to the extra-large shape (28dp, per the spec), so items get the carousel's masking and parallax.

| Variant | Export |
|---|---|
| multi-browse | `HorizontalMultiBrowseCarousel`, `CarouselDefaults.multiBrowseFlingBehavior` |
| hero | `HorizontalMultiBrowseCarousel` with a large `preferredItemWidth` (one large item and one small), snapping |
| centered-hero | `HorizontalCenteredHeroCarousel` |
| uncontained | `HorizontalUncontainedCarousel`, `CarouselDefaults.noSnapFlingBehavior` |
| full-screen | `HorizontalUncontainedCarousel` with `itemWidth` equal to the window width, 0dp padding, 16dp `itemSpacing` and `CarouselDefaults.singleAdvanceFlingBehavior` (the spec's "one edge-to-edge large item" with mandatory snapping) |

Two parts of Material's carousel guidance have no Compose API, so the catalog does not offer them:

- **Uncontained multi-aspect ratio:** `HorizontalUncontainedCarousel` takes one `itemWidth` for every item, and the layout exists to give items different widths.
- **Vertical full-screen feeds:** Material describes full-screen as suited to "vertically-scrolling video or image feeds", and every Compose carousel is horizontal. A vertical feed would be a `VerticalPager`, which has no carousel masking, so it is not a carousel and stays out.

Both are recorded as `no-compose-api` omissions (see Staying current), so a material3 release that adds them is noticed.

### Snackbar

`Snackbar` is an overlay with `message`, an optional `action` label and action, and `dismiss`. It draws in the `Scaffold`'s snackbar slot, not at screen level, so it sits above the FAB and the navigation bar as Material requires. That is the one change to `OverlayPlacement`. It exports to a `SnackbarHost` in the `Scaffold` plus a `showSnackbar` call. Two-line and long-action layouts follow from the text, as they do in Compose.

### Radio button

`RadioGroup` holds `RadioButton` children (`label`, `selected`), mirroring `ChipGroup`. The group owns single selection through the existing group-selection state, so a tap moves the selection the way chips already do. It exports to a `Column` with `Modifier.selectableGroup()` and `RadioButton` rows whose whole row is the touch target.

### Badges

A badge is a `badge` prop, not a component: `true` draws a small badge, and a number or short string draws a large one. The prop is on `NavigationBarItem`, `NavigationRailItem`, `Tab` and `IconButton`, and exports to `BadgedBox` with `Badge`. Material says badges anchor to an icon's upper trailing corner and live "within other components", so a free-standing `Badge` component would only add a way to place one wrongly.

### Tooltips

`Tooltip` is an anchored overlay with `anchor`, `variant: plain | rich`, `text`, and, for rich, an optional `title` and up to two action buttons. It reuses `Menu.anchor` placement and exports to `TooltipBox` with `PlainTooltip` or `RichTooltip`. Plain tooltips are overlays too, not a prop on icon buttons, so a reviewer can open one in a static render.

### Date and time pickers

- `DatePicker` is an overlay with `variant: modal | modal-input | docked | full-screen`, `range` for date ranges, and selected dates as ISO strings.
  - Modal and modal input export to `DatePickerDialog` holding `DatePicker` or `DateRangePicker` with the matching display mode.
  - Docked is anchored to a `TextField` through `anchor`, Material's text field dropdown.
  - Full-screen is composed: the full-screen dialog below holding `DatePicker` or `DateRangePicker` without their own action row. Material's anatomy for it is a full-screen dialog's parts (headline, close icon button, Save text button, divider) around the calendar, and `DateRangePicker`'s vertically scrolling months are that calendar. Material recommends it on compact windows, so a finding flags it at medium and larger views.
- `TimePicker` is an overlay with `variant: dial | input`, `time` as `HH:mm` and `is24Hour`. It exports to `TimePickerDialog` holding `TimePicker` or `TimeInput`.

Pickers otherwise read the clock: a date picker opens on today's month and marks today. Renders must not change from one day to the next, so the selected date is required and the displayed month comes from it. The renderer passes a fixed "today" so evals stay reproducible, and the exporter emits the proposal's dates as sample data.

### Side sheets

Material has two side sheets, and Compose has a composable for neither, so each maps to what the catalog can already express or compose:

- **Standard side sheet:** a screen with pane role `supporting`. Material describes it as staying visible beside the main content at medium and expanded widths, which is the supporting pane the previewer already draws with `SupportingPaneSceneStrategy` and exports to Navigation 3. The sheet's header (title, close icon button) is ordinary components in that screen. No new component.
- **Modal side sheet:** a `SideSheet` overlay with `title`, an optional back icon, and children. It is composed from a scrim and a `Surface` on the trailing edge (leading in RTL) with the spec's values: surface container low, 16dp corners on the inner edge, at most 400dp wide, 24dp padding, and a close icon button in the header, which Material calls "highly recommended". It exports to the same composition inside a full-window `Dialog`.

The skill's guidance ("bottom sheet on phones, side sheet on medium and up") stays as it is, because both forms are now drawable.

## Missing variants

- **Docked toolbar:** a new `DockedToolbar` component for the `Scaffold` bottom bar slot, exporting to `FlexibleBottomAppBar`. It is separate from `FloatingToolbar` because its composable, slot and rules differ: bottom of the window only, never with a navigation bar.
- **Slider:** `variant: standard | centered` (centered draws `SliderDefaults.CenteredTrack`) and `orientation: horizontal | vertical` (`VerticalSlider`). `RangeSlider` stays horizontal, as Material requires. Size presets stay out: Compose has none, and the skill says so.
- **Checkbox:** an `indeterminate` prop, exporting to `TriStateCheckbox`.
- **Switch:** `icons: none | selected | both`, exporting to `thumbContent`.
- **Menus:** `Menu` gains `colors: standard | vibrant` and `MenuGroup` children for grouped layouts (`DropdownMenuGroup`). `MenuItem` gains `role: action | single | multi` with `selected`, exporting to `DropdownMenuItem`, `SelectableDropdownMenuItem` and `CheckableDropdownMenuItem`.
- **Full-screen dialog:** `Dialog` gains `variant: basic | full-screen`. Full-screen takes children and a confirm label, and is composed from a `Dialog` with full-screen `DialogProperties` holding a `Scaffold` whose `TopAppBar` has the close icon and the confirm text button. Material keeps it to compact windows, so a finding flags it at medium and larger views rather than the renderer swapping it.

## Findings that come with the new components

Several skill rules become checkable once the components exist, and the checks belong in the finding rules beside the components: at most one snackbar open, at most three carousel items with text at compact widths, no docked toolbar together with a navigation bar, full-screen dialogs and date pickers only at compact widths, and no navigation bar with fewer than three destinations (tabs instead). Each traces to the component's source page in `docs/research/material-3-components.md`.

Selection counts are validator rules instead: exactly one selected radio button per group, and at most one selected single-select menu item per group. A selection is authored in props (a `selected` flag per button or item), and a finding sees only the rendered tree, where the group's own selection state has already picked one. The validator reads the authored props at each size class, so it reports the proposal that is wrong, not the render.

## Staying current

Material and Compose change on separate schedules, so coverage can drift in three ways, and each has its own signal.

### Material adds or retires a component

`fetch.py` builds the M3 page list from the site's own content manifest, so a new component page appears in `sources/m3.material.io/pages/components/` on the next monthly fetch without anyone listing it. Today nothing connects that file to the catalog: `check_coverage.py` only demands a research ledger row.

The registry closes that link:

- Each `ComponentDef` names the M3 page it implements (`m3Page`, a slug such as `carousel`). Several entries may share a page (`Tabs` and `Tab`), and a page may be claimed by a variant of another entry (`extended-fab` by `FloatingActionButton`).
- The catalog holds an omission list: each M3 page the catalog deliberately does not draw, with a reason of `retired` (navigation drawer, segmented buttons) or `no-compose-api` (carousel multi-aspect and vertical feeds), and a sentence of why.
- Both are written into `app/schemas/m3-expressive.catalog.json` by `writeSchemas`, which `scripts/test_material3_inventory.py` already reads.
- A test in `scripts/` fails when a component page in `sources/` (other than the `all-buttons` chooser) is neither claimed nor omitted, and when a claim or omission names a page that no longer exists.

So the monthly fetch turns "Material shipped a new component" into a failing `python3 -m unittest discover scripts`, the check `AGENTS.md` already requires, and the fix is a decision recorded in the registry: implement it or omit it with a reason.

### Material changes a covered component

A new variant or configuration on an existing page shows up as that page's diff in `git diff sources/`. The upstream update loop in `AGENTS.md` already sends every changed page to the research docs and then the skill. The loop gains one more stop: the catalog entry that claims the page, so a variant the skill now recommends becomes a variant the previewer can draw. This stays a review step, not a test, because no script can tell a new variant from reworded guidance.

### Compose adds an API

A component omitted as `no-compose-api` can become possible on a material3 upgrade. Upgrades already regenerate `scripts/material3-inventory.txt` with `material3_inventory.py`, so the upgrade's inventory diff lists every new public name. On each upgrade, read the added top-level names against the `no-compose-api` omissions and against Material components composed from parts (modal side sheet, full-screen dialog, full-screen date picker): a dedicated composable replaces the composition.

The desktop build lags Android. The app renders with JetBrains' material3 (1.13.0-beta01, mapped to AndroidX 1.5.0-beta01) while the export sandbox pins AndroidX 1.5.0-alpha29, so an API in the Android inventory may not exist on desktop yet. A component can join the catalog only when both builds have its composables; until then it stays omitted as `no-compose-api` with the desktop version it is waiting for.

## Links

- [Visual design exploration v3](visual-design-exploration-v3.md): why the previewer exists, its fidelity bar, and its architecture
- [Material 3 components research](../research/material-3-components.md): the distilled usage rules per component
- [compose-mapping.md](../../skills/android-design-explore/references/compose-mapping.md): proposal component to Compose mapping
- [Material 3 components](https://m3.material.io/components)
