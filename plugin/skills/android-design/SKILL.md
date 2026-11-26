---
name: android-design
description: >
  Material 3 Expressive design judgment for Android apps in Jetpack Compose. Load first for any task
  deciding how an Android screen looks, even plain Compose builds: creating or polishing screens,
  reviewing UI code or screenshots, theming (color, typography, shape), choosing components, adding
  motion, and adapting phone, foldable, tablet, desktop (list-detail, supporting pane), or Wear OS
  app screens. Wear tiles, watch faces, and widgets belong to other skills.
---

# Android Design

Material 3 Expressive design decisions for Jetpack Compose.

Three ideas govern every screen. **Emphasis budget**: rank what the screen is for, then spend scale, fill, color contrast, shape, type, containment, and motion on the primary goal, less on secondary goals, and almost none on the rest (uniform emphasis creates a **template**). **Role pairs**: color strictly through `X` / `onX` pairs (and `onSurfaceVariant` for lower emphasis), keeping raw hex literals and `.copy(alpha = ...)` out of screen code. **Reveal, not stretch**: larger windows reveal a second pane, expanded rail, or more detail instead of stretching phone UI wider.

Defaults can bend when the direction justifies it; all other rules hold.

**Surfaces.** Phone, tablet, desktop, and Wear OS app screens. For Wear OS, read [references/wear-os.md](references/wear-os.md) first (it overrides this file where they differ); use `wear-compose-m3` for scaffolding and migration. Wear tiles, watch faces, and home screen widgets belong to `wear-widgets` and Glance skills.

## Workflow

1. **Route** the request: proposal first, or straight to code ([Route](#route)).
2. **Answer the [design questions](#design-questions)** in your reply before any code.
3. **[Compose the screen](#compose-the-screen)** from those answers.
4. **Build** with the [reference](#reference) below until the screen compiles and renders.
5. **Run the [screen check](#screen-check)** on the rendered result.

### Route

Start with an exploration when the request asks for the design of a new app, new screen, or redesign (polish, visual direction, Material 3 Expressive, or naming this skill): follow `android-design-explore`, verify and show its live board via `android-design-preview`, and wait for approval before wiring Compose into the app. Skip exploration only if the user explicitly asks to skip it or an approved exploration already exists (port it via `android-design-explore`).

Plain builds without design questions, and changes inside an existing design (fixes, tweaks, reviews, new states of an approved screen), go straight to code through the design questions and screen check. UI Builder requests follow `compose-ui-builder` while this skill owns design decisions.

### Design questions

Answer all seven in your reply before writing code (1 to 2 sentences each):

1. **Rank the goals.** Name the single primary task, plus secondary and tertiary ones.
2. **Choose a direction.** State the emotional character, defining content or interaction, and one concrete visual idea uniting type, imagery, color, and shape ("the total set in tall numerals fills the top third," rather than generic adjectives).
3. **Decide the hero moment.** Is this one of the product's one or two hero moments? If yes, plan how tactics combine ([Hero moments](#hero-moments-and-restraint)); otherwise keep it calm.
4. **Choose the color source.** Dynamic (wallpaper), brand (generated scheme), or content-based (from an on-screen image) ([Color](#color)).
5. **Spend the emphasis budget.** Primary gets the most, usually size first; secondary gets fewer tactics; tertiary gets containment and type. Content can dominate when it is the product, keeping the primary action unmistakable.
6. **Pick components** from the [Components](#components) table instead of older Material APIs.
7. **Plan window adaptation (reveal, not stretch).** For each size class you render (compact; medium or expanded; large or extra large), name what extra space reveals (second pane, expanded rail, more detail) using the five questions in [references/layout.md](references/layout.md). Keep navigation consistent across breakpoints (rail on larger windows pairs with a navigation bar on compact), and reflow control screens to reveal content.

### Compose the screen

Components come last; compose from direction and content first:

1. **One element leads.** Pick what fills the most space (content, primary control, or key summary) and surround it with empty space to focus attention.
2. **Space follows rank.** Give the lead scale, ample space, and the brightest surface mapping; pack supporting data compactly; put tertiary details at edges or behind a tap.
3. **Type can be the structure.** Anchor editorial moments with a card-free display title or numeral shaped by weight, size, color, and spacing.
4. **Group deliberately by spacing or containers.** Proximity and alignment keep flowing lists open; containers and surface steps group units that read or tap together. On action-list screens (habits, inbox), gather actionable items under one heading and container group, and put completed items as plain rows under their own heading so contained and flat rows stay grouped rather than alternating.
5. **Vary form by information type.** Match grid to content: hierarchical for editorial/detail, modular for equal items, column for flowing lists ([references/layout.md](references/layout.md)). Vary container forms across groups to avoid the template look.
6. **Let content set color.** When an image or subject leads, derive screen color from it ([Color](#color)) instead of adding unrelated accents.
7. **Focus with surface blur (optional).** On scrolling lyrics or pickers (softening edges around a sharp center) or photo-led hero headers (softening the image region behind scrimmed text), apply surface blur ([blur](references/theming.md#blur-shadows-and-gradients)).

For shipped examples (metric card, editorial header, pill hero control), read [references/components.md](references/components.md#patterns-from-shipped-expressive-apps) after choosing the composition.

### Screen check

For an exploration, run `android-design-explore`'s Check step instead of rendering here.

**Visual gate:** judge the rendered screen, not the code. Render via `compose-preview` (or `compose-ui-builder` for UI Builder outputs). If rendering is unavailable or fails, report `"visual quality unverified"` and point to this repo's README setup instead of guessing from code. Verify every item:

- Does the eye land on the primary goal immediately?
- Does every expressive choice serve the direction?
- Does color reinforce hierarchy and atmosphere, with accents only on elements that have a job?
- Are supporting actions easy to find and clearly tappable (with text-only buttons kept inside a clear action group such as a dialog, card, or snackbar)?
- Does it hold up with real content lengths and a smaller window?
- Would it remain clear with expressive flourishes removed?
- Is it still a template? A screen resembling the Material component catalog is incomplete.

Fix the largest mismatch and re-capture; for stateful screens, also capture the alternate state that changes emphasis or layout. Stop after two fix rounds and report what remains.

**Code gate** (every item checked):
- [ ] `MaterialExpressiveTheme` with chosen color source, Google Sans Flex for plain text, and at most one bundled brand face on Display, Headline, and Title Large.
- [ ] Components from the right column of [Components](#components).
- [ ] **Role pairs** only: zero raw hex colors, zero `.copy(alpha = ...)`.
- [ ] Custom animations use `MaterialTheme.motionScheme` instead of `tween()`.
- [ ] Text fields use `TextFieldState` (`rememberTextFieldState()`) with `SecureTextField` / `OutlinedSecureTextField` for passwords, and search bars back `SearchBarDefaults.InputField` with `TextFieldState`.
- [ ] Show/hide uses `AnimatedVisibility`, `AnimatedContent`, or conditional composition (never `alpha = 0f` alone); text over blurred images sits on a scrim, gradient, or solid/tonal surface.
- [ ] Platform semantics match [Android conventions](#android-conventions); content draws edge-to-edge.

## Reference

### Theme setup

Wrap the app in `MaterialExpressiveTheme` with `MotionScheme.expressive()`, the chosen color scheme, and the type scale from [Typography](#typography). Requires `androidx.compose.material3:material3` **1.5.0** or later (Compose BOM `2026.11.01`). Read [references/theming.md](references/theming.md) for theme code, toolchain requirements, opt-in APIs, seed schemes, type voices, shape morphing, motion specs, blur, shadows, and spacing.

### Emphasis and hierarchy

Worked example across four screens: [references/components.md](references/components.md#worked-example-aura-breathing-app).

- **Size is relative:** start the primary CTA at default size with the strongest fill (small button; medium FAB by default) and grow only enough to lead peer actions (content like a summary card or list row can be larger). Grouped actions share one size by default, with the primary leading by fill, width, or shape. Reserve XL for control-led hero screens (player, breathing session). Keep button labels to 1 to 3 words on a single line; give more room or step down a size across breakpoints so labels stay unclipped.
- **Placement:** put the key action where hands and eyes already are (low in vertical flow or above the keyboard) based on the interaction and content space (default).
- **Color contrast across roles:** keep the strongest fill on the primary action only; secondary actions use tonal or standard, and toggles show state via filled icon or tonal container. Use `primary` for the main action, `secondary` / `secondaryContainer` for supporting controls, and `tertiary` for status or accents.
- **Summaries support the task:** on action-list screens, keep actionable rows in clear containers, let completed items recede (quieter color, surface container or flat row), and reserve selection highlights for user choice rather than completion.
- **Order of work:** establish hierarchy first (size, color, spacing, placement, containment), then layer unique emphasis (shape, scale, morph).

### Hero moments and restraint

Hero moments layer expressive tactics (type, shape, color, motion, flexible components) on the product's one or two most emotional, central interactions (player, dialer, completed goal, breathing session). Keep secondary screens calmer.

- **One custom thing moves:** keep text stable while a hero shape animates (built-in component press morphs excluded).
- **Let content lead:** when album art or photos are the product, give them scale and derive screen color from them. Library-shape masks, full bleed, or layered titles are optional.
- **Keep familiar patterns:** style recognizable patterns (playlists, clear labels, domain tone) rather than replacing them; clarity beats emotion.

### Color

Read [references/theming.md](references/theming.md#color-schemes) for scheme generation and code; read [extra colors](references/theming.md#extra-colors-semantic-and-categories) when using 3+ categories or charts/data graphics.

- **Color source:** Dynamic (wallpaper) for personalization; Brand via Material Theme Builder or MaterialKolor at default `TonalSpot` (keep `PaletteStyle.Expressive` out, since it shifts hues away from the brand); Content-based from a visible on-screen image (cap a screen at two color sources); `expressiveLightColorScheme()` only as the light fallback.
- **Surfaces cover most of the screen:** build hierarchy with `surface`, `surfaceContainer` (navigation), and `surfaceContainerLowest` to `Highest` (brightest on the most important container). Reserve accent containers for elements with a job.
- **Role pairs only:** pair `onX` on `X`; `onSurface`, `onSurfaceVariant`, and `primary` on surfaces; `inverseOnSurface` and `inversePrimary` on `inverseSurface` (one standout element such as a snackbar or key total). Use `outlineVariant` for dividers, `outline` for text field borders, and `onSurfaceVariant` for quieter emphasis. Keep semantic error/success colors consistent app-wide.
- **One strong fill (default):** give saturated fill to the hero or one coherent group. Hero screens may mix two or three distinct accent container roles (`primaryContainer`, `secondaryContainer`, `tertiaryContainer`, or inverse) for key elements with distinct jobs while everything else stays on surfaces. Inspect rendered hex via `android-design-preview check` (`colors`) or `tree`.

### Typography

Read [references/theming.md](references/theming.md#typography) for Google Sans Flex axes, bundled brand faces, the `voice` builder, and editorial techniques.

- **Scale and voices:** use about five clearly contrasted styles per screen from Display, Headline, Title, Body, and Label, plus emphasized styles (`displayLargeEmphasized` through `labelSmallEmphasized`) for primary button labels, selected/unread items, key numbers, and headlines. Give Display/Headline a real voice (matching `opsz` to text size) and reuse two or three named voices per app; keep Body and Label on default axes. Preserve scale sizes when swapping faces.
- **Typeface standard:** Google Sans Flex is default and mandatory for plain text (Title Medium/Small, Body, Label) on phone, tablet, and desktop. Optionally pick at most one bundled brand face (`Fraunces`, `Bricolage Grotesque`, `Big Shoulders Display`, or `Doto` at Display only) for Display, Headline, and `titleLarge` (small app bars and side sheets use `titleLarge`). Use Google Sans Code for numbers and data at any size.
- **Formatting:** ~1.2x line height on large styles (~1.25x for heavy/narrow custom voices), 1.5x on body; `fontFeatureSettings = "tnum"` for changing numbers; sentence case everywhere.

### Shape

Read [references/theming.md](references/theming.md#shapes) and [shape library](references/theming.md#shape-library) for tokens, `MaterialShapes`, and morphing code.

- **Tension creates focus:** mix round and square to highlight the focal point (default: rounded square primary control among circles, or vice versa).
- **Shape signals state:** use Expressive components' `shapes` parameters so buttons, toggles, and list items morph on press and selection.
- **Uniform peers and nesting:** comparable data (metrics, calendar days) shares shape, size, and spacing without overlap. Nested corner radius equals outer radius minus padding; segmented lists use 4dp inner, 16dp outer corners (selected morphs to 16dp all around).
- **Shape library (`MaterialShapes`):** 35 shapes for avatars, image masks, decorative graphics, achievements, and hero buttons; keep text-heavy containers on standard rounded corners.

### Motion

Read [references/theming.md](references/theming.md#motion) for spring values, API selection, and `ReducedMotionScheme`.

- **Spring physics via `MotionScheme`:** use `MaterialTheme.motionScheme` instead of `tween()`. Spatial specs (position, size, rotation, corners) overshoot and settle; Effects specs (color, opacity) never overshoot. Speeds: fast (small controls), default (sheets, rails, partial screen), slow (full screen). Default to `MotionScheme.expressive()`; use `MotionScheme.standard()` for utilitarian products.
- **Simple screen transitions:** container transform only for hero moments in shallow hierarchies; platform forward/backward for everyday hierarchy; quick fade-out then fade-in between top-level destinations (keep transitions free of lateral slides, and slide bottom sheets without fading).

### Spacing and layout

Read [references/layout.md](references/layout.md) for breakpoints, tall medium windows, canonical layouts, posture handling, and desktop mouse/keyboard rules.

- **8dp scale:** `4, 8, 12, 16, 24, 32, 48`dp. Put padding and gaps on parents; use 16dp side margins on compact and 24dp on medium and up. Negative space around the lead adds emphasis.
- **Group with gaps and containers:** use dividers only in uncontained lists. Keep reading text to 40 to 60 characters per line.
- **Breakpoints (600 and 840dp):** apply **reveal, not stretch**; compact uses one pane plus navigation bar, while expanded and up use two full-height panes plus navigation rail.

### Components

Read [references/components.md](references/components.md) when choosing, writing, or customizing components.

| Instead of | Use | Compose |
| --- | --- | --- |
| Medium/large top app bar | Flexible app bar (shorter, bigger title, subtitle) | `MediumFlexibleTopAppBar`, `LargeFlexibleTopAppBar` |
| Bottom app bar | Floating or docked toolbar | `HorizontalFloatingToolbar` (FAB overload), `FlexibleBottomAppBar` |
| Segmented buttons | Connected button group | `Row` of `ToggleButton` with `ButtonGroupDefaults.connected*ButtonShapes()` |
| Navigation drawer | Expanded navigation rail | `WideNavigationRail`, `ModalWideNavigationRail` |
| `NavigationBar` | Flexible navigation bar | `ShortNavigationBar` + `ShortNavigationBarItem` |
| Small FAB, speed dial | Medium/large FAB, FAB menu | `MediumFloatingActionButton`, `FloatingActionButtonMenu` |
| Spinner for short waits | Loading indicator (200ms to 5s) | `LoadingIndicator`, `ContainedLoadingIndicator` |
| Plain progress bar | Wavy progress where expressiveness fits (default strokes) | `LinearWavyProgressIndicator`, `CircularWavyProgressIndicator` |
| `ListItem(headlineContent=)` with dividers | Segmented expressive list | `SegmentedListItem` spaced by `ListItemDefaults.SegmentedGap` |
| Flat dropdown menu | Grouped vertical menu | `DropdownMenuPopup` + `DropdownMenuGroup` |
| One-size buttons | Sized, morphing buttons (XS 32 to XL 136dp) | `Button(shapes = ButtonDefaults.shapesFor(height))` |

- **App bar vs toolbar:** app bars hold navigation plus 1 (at most 2) essential actions, boosted with one filled or tonal icon button. Page controls go in a floating toolbar of icon buttons; local navigation uses a labeled floating toolbar with one selected pill, never alongside a navigation bar ([toolbars](references/components.md#toolbars)).
- **One FAB per screen** for the top screen-wide action only, never on cards or minor actions.
- **Button groups:** same size and shape by default; mix sizes only in hero moments.

### Accessibility and writing

- **Contrast and targets:** 4.5:1 small text, 3:1 large text, icons, and controls; honor system contrast ([contrast levels](references/theming.md#contrast-levels)); pair color with icon, label, or underline. Keep touch targets at least 48dp with 8dp separation.
- **Wrapping:** wrap text by default; ellipsize only when full text is reachable (tooltip, link, expanded view).
- **Compose semantics:** section titles take `Modifier.semantics { heading() }`. Toggle/select rows carry row-level `toggleable` or `selectable` with a `role` (or `SegmentedListItem(onClick = ...)` with `Modifier.semantics { role = Role.Switch; toggleableState = ToggleableState(checked) }` for switch rows, or its `checked` / `selected` overloads) and pass `null` as the child `Checkbox`/`Switch`/`RadioButton` callback. Rely on built-in merging on clickables, buttons, and `ListItem`. Hide decorative or repeated text via `hideFromAccessibility()`; use `clearAndSetSemantics` only when merging cannot describe the control; add semantics manually to custom `Canvas` controls.
- **Writing:** content descriptions state purpose ("Voice search"), not appearance or role. Sentence case, second person, contractions, no periods on single-sentence labels, exclamation points only for real celebrations.

### Android conventions

| Avoid (iOS habit) | Use (Android) |
| --- | --- |
| Centered navigation title by habit | Start-aligned title by default (center only when composition calls for it); flexible app bars collapse on scroll |
| Back chevron; "Cancel" and "Done" text in top bar | Up arrow for hierarchy (system back handles back); close icon for full-screen modals with confirm action in app bar; dialogs keep labeled text buttons |
| Action sheets | Bottom sheets |
| Segmented control | Tabs for peer destinations; connected button group for options/view modes within one screen |
| Table rows with disclosure chevrons and hairline dividers | List items without chevrons, grouped with gaps ([Spacing and layout](#spacing-and-layout)) |
| SF Symbols | Material Symbols, one style (outlined, rounded, or sharp) app-wide |

- **Edge-to-edge:** draw backgrounds, images, and scrolling content behind system bars; keep text, controls, and tap targets inset clear of gesture insets; keep the gesture bar transparent and status bar transparent unless content scrolls under it; pin inputs above the keyboard (`edge-to-edge` skill).
- **Adaptation:** support landscape, window resizing, and light/dark themes on every screen without locking orientation or theme.
- **Specific screens:** when building settings, onboarding/sign-in, or notifications, read [references/android-conventions.md](references/android-conventions.md).
