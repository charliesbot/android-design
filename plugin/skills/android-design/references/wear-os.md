# Wear OS

Material 3 Expressive for Wear OS app screens, from the Wear OS design guide and androidx Wear Compose Material 3. Phone guidance on emphasis, hero moments, and restraint still applies; this file covers what changes on a round, glanceable, battery-bound screen. Tiles, watch faces, and complications belong to `wear-widgets`; use `wear-compose-m3` for scaffolding (`AppScaffold`, `ScreenScaffold`), navigation, and migration.

## Principles

- **One or two critical tasks**, finished in seconds. Keep dense data, grids, and spreadsheets off the watch.
- **Glanceable:** legible while moving.
- **Shallow and vertical:** at most two levels deep; scroll in one direction only.
- **Embrace round:** edge-hugging buttons and bezel-hugging elements (progress rings, scroll indicators, curved time text) follow the curve.

## Theme

```kotlin
@Composable
fun WearAppTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    MaterialTheme(
        colorScheme = dynamicColorScheme(context) ?: BrandWearColorScheme,
        motionScheme = MotionScheme.expressive(), // Wear defaults to standard
        content = content,
    )
}
```

(`androidx.wear.compose.material3.MaterialTheme`, `MotionScheme`, and `dynamicColorScheme`.)

- `dynamicColorScheme(context)` returns null when the watch face provides no theme; always fall back.
- Wear springs (damping / stiffness): expressive spatial fast 0.7 / 800, default 0.75 / 350, slow 0.8 / 200; standard spatial 1.0 with 1400 / 500 / 260; effects never bounce.
- Wear corner scale: 4, 8, 18, 26, 36dp. There is no `MaterialShapes` on Wear.

## Color

- **Build from black, dark theme only:** Wear OS uses only dark theme. Keep the `background` role pure black; color lives inside components, never as a tinted page. Container roles are fills, never text or icon colors.
- Dynamic themes derive from two watch-face seed colors (primary and tertiary) and meet WCAG AAA. Pair only `X` with `onX`.
- Roles: `Primary` (main action, edge button), `PrimaryDim` (distinct, lower demand), `Secondary` (supporting dense UI), `Tertiary` (standout feedback, goal reached), `ErrorDim` (emergencies, stop).
- Fill only the primary action ([Primary action](#primary-action)); keep supporting actions and cards in neutral or container roles (`SecondaryContainer`, `PrimaryDim`).
- Color carries meaning: red for errors, green for success.

## Typography

- **Roboto Flex** everywhere. Weight and width are the useful axes: narrow width fits long names and numbers; keep headers away from extreme widths and avoid ultra-light weights at small sizes.
- Roles: `display*` (hero info/metrics), `title*` (wayfinding), `label*` (inside components), `bodyLarge` to `bodyExtraSmall`, `numeralExtraLarge` to `numeralExtraSmall` (tabular by default, accepts expressive width), `arcLarge/Medium/Small` (curved edge text via `curvedText(text, style = MaterialTheme.typography.arcMedium)` in `CurvedScope`).
- Minimum sizes: 12sp essential text, 10sp non-essential text. Line height ~1.1x.
- Design for Bold text and long languages so text and controls stay inside the round edge without overlapping.
- Use `numeral*` for a few unlocalized digits (timer, step count, picker) with tabular spacing whenever digits change.

## Layout

- **Three sections:** top (optional time text, optional title, optional compact button on long pages), middle (content), bottom (actions, ideally an **edge button**, or empty at journey end). Omit time text on dialogs, confirmations, and pickers; show the scroll indicator only on scrolling screens.
- **Round safe area:** outer margins (top, bottom, sides) are **percentages**, never fixed dp, and grow where the curve cuts in (top and bottom differ by content). Spacing between elements is fixed dp.
- **Touch targets:** at least 48 x 48dp with icons centered; pair icons with labels inline where they fit.
- **Small first (204 to 216dp, test dense layouts at 192dp), add value past 225dp:** past the 225dp breakpoint, reveal more content, extra buttons, or larger targets/text for glanceability without dropping anything the smaller watch showed or pushing content off screen. Fill component width appropriately without scaling fonts up unless graphic.
- **Scrolling screens:** `TransformingLazyColumn` scales and fades items at top/bottom edges. Group long mixed lists with labels (skip labels on single-type lists). Elevate a clear primary action to the top of long pages or close with an edge button. Scroll in one direction only.
- **Non-scrolling screens** (media, pickers, steppers, timers, dialogs, progress): stretch the middle section to fill height between top and bottom; prefer icon buttons over wide pills and show data graphically (progress ring, large numeral). Use preset dialog, confirmation, stepper, and picker layouts, keeping all content unclipped without scrolling.
- **Rotary input:** bind the crown to the main value on steppers, pickers, and players.

## Primary action

One prominent action per screen, filled `primary`:

- **Scrolling screens:** usually the **edge button** at the bottom of the list, taking either a short glanceable label or a single clear icon (never both).
- **Non-scrolling screens:** an edge button when space allows, otherwise one filled icon button among lower-emphasis neighbors (`tonal`, `outlined`, or child buttons; stack multiple bottom actions or use a two-icon button group).
- **Recommended pairings:** Primary + PrimaryDim (main plus complementary), PrimaryDim + Tertiary (highlight plus feedback), Primary + SecondaryContainer (main leads, rest recedes), or Tertiary + Primary + SecondaryContainer when there is no single main action.

## Expressive components

| Pattern | API |
| --- | --- |
| Edge-hugging button | `ScreenScaffold(scrollState, edgeButton = { EdgeButton(onClick, buttonSize = EdgeButtonSize.Medium) { ... } })`; sizes ExtraSmall 46, Small 56, Medium 70, Large 96dp |
| Morph on press | `FilledIconButton(onClick, shapes = IconButtonDefaults.animatedShapes()) { ... }` (also `TextButtonDefaults.animatedShapes()`, toggle `animatedShapes()`) |
| Button group | `ButtonGroup { ... }`: one `MutableInteractionSource` per button, `Modifier.animateWidth(interactionSource)`, and the same source passed to the button; the pressed button widens and squeezes its neighbors |
| Morphing list | `TransformingLazyColumn` with `val spec = rememberTransformationSpec()`; per item `Modifier.transformedHeight(this, spec)` and `transformation = SurfaceTransformation(spec)` |

Reserve custom `TransformationSpec` edge effects (rotating or morphing items) for hero moments.

## Media and ongoing activities

- **Media:** five-button controls with play/pause at 64dp (80dp past 225dp) inside a progress ring; volume via the rotating side button; prioritize downloaded media.
- **Always-on:** few lit pixels; drop frequently updating progress.
- **Haptics:** strong for key moments (payment confirmed), subtle for precision (scrolling), synced with motion.
- **Gestures (Wear OS 7):** double pinch triggers the screen's one primary action; wrist turn dismisses. Every gesture action also needs a visible button.

## Behaviors

- **Navigation:** swipe right closes the screen (no back buttons). Avoid mixing vertical and horizontal scrolling (media playback excepted). Non-scrolling multi-page layouts can paginate vertically or horizontally.
- **Physical buttons:** map a multifunction button only to a single-press, binary, reversible action (start/stop, play/pause) also present on screen; keep destructive and multi-step actions off hardware buttons.
- **Launch:** black background with a centered 48dp circular app icon; build the screen from static text and placeholders instead of an indeterminate spinner.
- **Ongoing activities:** the Recents entry states type and status (track, workout duration, ETA).
- **Dense chips:** compact chips beat cards in dense layouts.
- **Offline:** show an offline indicator at the top when features are unavailable (gray out or hide unavailable items), or at the list end when no more items can load.
- **Sign-in:** passkeys through Credential Manager first, at least two methods total; sign-in-only apps ask immediately, others wait until needed and explain the benefit. Keep username/password typing off the watch.
- **Dialogs:** alerts are full-screen interruptions used sparingly (left-align text past three lines). Confirmations only acknowledge finished actions (never ask questions) and are usually unnecessary when a visible UI change confirms success.
- **Swipe to reveal** (delete, archive in lists): partial left swipe reveals a primary and optional secondary action; swipe past 75% commits the primary action; destructive actions show an undo chip.
