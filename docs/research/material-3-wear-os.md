# Material 3 Expressive for Wear OS

Design research for Material 3 Expressive on watches. It extends
[material-3.md](material-3.md): the phone guidance on emphasis, hero moments,
color roles, and springs still applies, and this document records what changes
on a round, glanceable, battery-bound screen.

Implementation (Compose for Wear OS APIs, `AppScaffold`, `ScreenScaffold`,
`TransformingLazyColumn`, migration) is covered by the installed
`wear-compose-m3` skill. This document is about design judgment.

## Sources and status

- **developer.android.com Wear OS design guide** (`/design/ui/wear/guides/`),
  41 current pages, fetched September 2026: get started, foundations (adaptive
  design, quality tiers, common layouts), styles (color, typography), surfaces
  (apps, tiles), patterns (gestures, media).
- **m3.material.io Design for watches** (Overview, Foundations, Styles,
  Layout tabs), content version `2026-09-16_06-10-03`.
- **Gaps:** the M3 design guide has no component or motion pages yet (APIs and spring values below come from androidx sources); component URLs
  redirect to the older Material 2.5 guide. Its behavior and surface pages
  (navigation, physical buttons, launch, sign-in, watch faces) were read later
  and still describe platform behavior; its component styling was skipped. Shape details
  (edge-hugging button specs, corner values) and Wear spring values come from
  the Compose APIs, not the design guide.

## What Expressive means on a watch

> "Material 3 Expressive is the latest design system for our smallest screen:
> the watch... emphasizes premium and a greater level of expression."

Five principles:

1. **Embrace round.** A shape framework for round screens that uses the whole
   canvas. **Edge-hugging buttons** and containers are "an ownable and iconic
   design pattern for round devices."
2. **Premium motion and springs.** Motion spatially connects surfaces and
   responds to gestures.
3. **Shape morphing.** Lists and selectable buttons morph their corners;
   containers round and sharpen to show state.
4. **Rich color.** Deeper tonal palettes, a third accent color, and more
   specific roles ("dim" variants, containers inside accent groups).
5. **Variable fonts.** Roboto Flex replaces Roboto everywhere, with a type
   scale tailored to the round screen.

### Levels of expression

Google grades Wear apps on three tiers and asks for **excellent or
transformative**:

| | Foundational | Excellent (required) | Transformative (recommended) |
| --- | --- | --- | --- |
| Components | Baseline migration | Multiple expressive components | Multiple expressive components and customization |
| Color | Baseline palette | Dynamic palette themes | Dynamic themes and/or unexpected color combinations |
| Typography | Roboto Flex | Roboto Flex | Roboto Flex with morphing |
| Shape | None | Some shape library and containment | Shape library and expressive containment |
| Motion | Motion tokens | Tokens plus some expressive motion (shape morphing, springs) | Same |
| Hero moments | None | Product-specific expressive moments | Expressive moments plus dramatic hierarchy and customization |
| Adaptive | Percentage margins | Plus added value after the 225dp breakpoint | Plus large-screen-specific designs |

This table is a useful self-check for generated Wear screens: "unremarkable"
is an explicit failure ("Don't let your app or tile be unremarkable").

## Watch design principles

- **Critical tasks only:** focus on one or two tasks, not a full app. Help
  people finish within seconds (arm fatigue); tiles get about 7 seconds of
  attention.
- **Glanceable:** test while moving and distracted. No spreadsheets, calendar
  grids, or dense data.
- **Shallow and linear:** no hierarchy deeper than two levels; content and
  navigation inline.
- **Vertical only:** scroll in one direction.
- **Always relevant:** adapt to time, place, activity. **Works offline.**
- Round screens have 22% less space than square ones and need larger margins.

- **Body-connected input:** the watch offers "Input enabled by a physical body connection, through sensors and motion detection", plus quick glanceable access through complications, notifications, and tiles. Limits: smaller screen, lower information density, limited battery.
- **Better together (watch vs phone):** "Watches work well for quick, frequent tasks, while mobile devices are better for prolonged and complex interactions." A watch and phone can split different parts of the same task. "Consider which actions are appropriate for each device." (m3 `/foundations/watches`: "Watches are often dependent on connected phones for functionality or complex interactions... Consider how experiences can be consistent and complement the strengths of each device"; "Navigation on a watch complements the experience on a phone.")

## Color

- **Build from black.** Watch UIs sit on a black background (battery, and it
  makes the bezel disappear). Tiles must never use a full-bleed image or color
  background.
- Dynamic theming derives from **two seed colors** (primary and tertiary) set by
  the watch face, and is WCAG AAA. A brand color can seed a custom theme
  instead. Media apps take their seed from the artwork, falling back to the
  watch face theme or a monochrome palette.
- Roles: three accent groups (primary, secondary, tertiary) each with
  **base, dim, and container** variants, plus error (error, errorDim,
  errorContainer) and surfaces (surfaceContainerLow, surfaceContainer,
  surfaceContainerHigh).
  - **Primary:** the most important actions, edge-hugging buttons, active
    states. **PrimaryDim:** visually distinct but not demanding attention.
    **PrimaryContainer:** cards or selected states that highlight content.
  - **Secondary:** supporting actions in dense UI. **Tertiary:** "drawing
    attention to key elements," standout feedback, a goal being reached, tap
    responses.
  - **Error:** delete and dismiss actions (like swipe to reveal).
    **ErrorDim:** high-priority emergencies and stop buttons.
  - Semantic color matters more on a watch: red for errors, green for success.
- Only pair `X` with `onX` (7:1 on watch).

Recommended pairings:

| Pairing | Use |
| --- | --- |
| Primary + PrimaryDim | Main action plus complementary items |
| PrimaryDim + Tertiary | Highlight important elements, tertiary for standout feedback (tap responses) |
| Primary + SecondaryContainer | Key elements stand out, less prominent content recedes |
| Primary + PrimaryContainer | Main action plus complementary items with depth |
| PrimaryDim + TertiaryDim | Highlight plus feedback such as a goal met |
| Tertiary + Primary + SecondaryContainer | When there's no single main action |
| Secondary + PrimaryContainer | Two equally important options that still contrast |
| Primary + TertiaryDim | Main action plus a complementary accent |
| Tertiary + Primary + PrimaryContainer | No clear main action: "use a combination of Tertiary and Primary for a main actions and Primary-Container for a complementary actions" |

Tiles: primary colors on primary actions, secondary or tertiary on the rest;
never all filled primary buttons.

### Dark theme only and designing across seeds

- "Wear OS uses only the dark theme because the wearable interface is built on a black background." The palette is also smaller, because a touch platform "doesn't require as many hover and focus states." Never generate a light Wear theme. (The m3.material.io watches page mentions light themes for daylight. developer.android.com is the newer and more specific source.)
- Design first with the **baseline scheme** so the right roles map to the right components, then use Material Theme Builder to check the mocks "across a range of source colors", since the watch face can supply any seed.
- Contrast comes from HCT **tone difference**: "Colors with a greater difference in tone create higher contrast."

### Full role set (roles-tokens)

- **Container** roles are fills for foreground elements like buttons; "They shouldn't be used for text or icons." Text and icons use the matching `on` role.
- Custom components "need to be properly mapped to this set of color roles" (baseline or dynamic).
- **PrimaryContainer:** cards or modals that highlight sections or selected states, and "ongoing activities."
- **SecondaryDim** (content: Secondary): "muted contrast for passive elements in dense areas."
- **SecondaryContainer:** "organizing secondary elements in dense layouts"; structure and separation without dominance.
- **Tertiary:** also badges, stickers, special action elements, and heightened attention on an input field.
- **TertiaryDim** (content: Tertiary): buttons or actions related to tertiary actions "yet don't require immediate focus."
- **TertiaryContainer:** backgrounds that group tertiary content, "like collections of badges or stickers."
- **Error vs ErrorDim vs ErrorContainer:**
  - **Error** is "slightly less alarming and urgent" (remove, delete, close, dismiss, as in swipe to reveal).
  - **ErrorDim** is for high-priority errors and emergencies (safety alerts, failed dialog overlays, stop buttons).
  - **ErrorContainer** is "an active error state which feels less interactive than a filled state" (an active emergency-sharing button or card, a failed overlay dialog).
- **Surfaces** (content: onSurface / onSurfaceVariant):
  - **SurfaceContainerLow** is an expanded container that sits below SurfaceContainer (an expanded notification card), or a non-interactive card that still benefits from containment.
  - **SurfaceContainer** is "The default container color for most elements."
  - **SurfaceContainerHigh** is for high-emphasis components on top of, or combined with, SurfaceContainer.

### Pairings to avoid

- **Don't** put primaryContainer content on Primary, or PrimaryDim content on PrimaryContainer. These "become illegible as contrast levels shift" and miss the 7:1 minimum. The same applies to the secondary and tertiary roles. **Do** pair onPrimary on Primary and onPrimaryContainer on PrimaryContainer (AAA, 7:1 or more).

## Typography

- **Roboto Flex** with two axes that matter most: **weight** (no very light
  weights for small text; no excessive weight at small sizes) and **width**
  (narrow fits long names and numbers; no wide type in page headers).
- **Variable axes in motion:** animate weight, width, or both as expressive
  feedback.
- 21 styles in six roles:
  - **Display** (L/M/S): short, highly glanceable hero information, key
    metrics, brand moments. Doesn't scale with the user font setting.
  - **Title** (L/M/S): wayfinding (page and section titles), not interactive
    components.
  - **Label** (L/M/S): text inside components; LabelMedium is the most common.
  - **Body** (L/M/S/ExtraSmall): content text, timestamps, metadata.
  - **Numeral** (ExtraLarge to ExtraSmall): a few digits that need no
    localization (charging screen, timer, step count, pickers, workout
    metrics); tabular by default and can take expressive width.
  - **Arc** (L/M/S): curved text at the top or bottom edge (time text, page
    titles, confirmation overlays, calls to action), with spacing tuned for the
    curve.
- Line height about **1.1x** on watch (vs 1.2x on phone); Compose adds extra
  line height to the last line.
- User font scaling moves in 6% steps, and nothing scales past 20sp. Per role:
  Display, Numeral, and LabelLarge never scale; Title, LabelMedium/LabelSmall,
  Body, and Arc scale with the user setting up to that cap. Test font scaling
  on those roles (titles, labels inside components, body, curved text), and
  design for the largest and smallest settings.
- Tabular numbers for anything that animates or changes.

- **Arc** text also takes tabular and mono spacing. Numerals take it "especially when the numbers scroll or change using motion" (for example, Picker).

## Shape

- Edge-hugging buttons at the bottom of scrolling lists and non-scrolling
  screens use the curve instead of fighting it; bezel-hugging elements
  (progress rings, scroll indicators, time text) grow automatically with the
  screen.
- Flexible containers round and sharpen corners to show state; lists and
  selectable buttons morph.
- Grouped containers distribute space evenly for symmetry, or unevenly to
  establish hierarchy and emphasize the important item.
- Loading animations, button groups, and layouts use the shape library.

## Layout

- **Structure:** time text (recommended on app screens, but not on dialogs,
  confirmation overlays, or pickers) and optional title at the top; content in
  the middle; action buttons at the bottom (an end-of-list **edge-hugging
  button** is recommended); scroll indicator only on scrolling screens.
- **Non-scrolling** screens (media players, pickers, steppers, dialogs,
  confirmations, fitness trackers) split into top, middle, and bottom; the
  middle stretches, top and bottom get inner margins against the curve.
  Prefer icon buttons over wide pill buttons; show key data graphically
  (progress indicators, large numbers). Paginate instead of scrolling when
  focus matters.
- **Scrolling** screens: `TransformingLazyColumn`; components fill the width;
  elevate unambiguous primary actions to the top of long pages; use labels to
  orient long mixed lists.
- Use icons **and** labels for actions when possible.

### Adaptive sizes

- Design for the **smallest round screen first** (204 to 216dp; check dense
  layouts at 192dp with large fonts).
- **Margins in percentages** on the outer edges; fixed dp between elements.
- **Breakpoint at 225dp:** below it, 192 to 224dp; above, 225 to 240dp+.
  After the breakpoint, add value: more content, more buttons, larger
  components, bolder graphs, the title slot on tiles. "Be expressive and bold."
- A larger screen must never show less than a smaller one. Don't just scale up;
  don't stretch components to fill space; don't enlarge fonts unless they're
  graphic.
- Quality tiers: ready for all screens (no broken layouts), responsive and
  optimized (fill the available space), adaptive and differentiated (new
  layouts past 225dp).

- Concrete value to add past the breakpoint on non-scrolling screens: "Media players can gain additional buttons or larger controls. Confirmation dialogs can gain an illustration or more information. Fitness screens can gain additional metrics." Non-scrolling layouts gain the most from extra space.

### App layout sections (surfaces/apps/layouts)

- These sections don't apply to preset component layouts (dialogs, confirmation overlays, pickers, switchers), which keep their own layouts, margins, and components.
- **Top:** time text, a **compact button**, and a title, all optional. Use the compact button "in special cases where the page is very long", such as search or an action, so users don't have to scroll to the bottom.
- **Middle:** any non-fullscreen Compose or custom components in a list, optional group headings, and the scrollbar. Components fill the width up to the percentage margins.
- **Bottom:** primary and secondary actions, or empty "if this is the end of a journey." An end-of-list edge-hugging button is recommended. For more than one action, use "a button stack, or two-icon button group."
- The system places time text and the scrollbar. Include them in layouts to show they're on. Time text "can include other relative information in your app."
- Wear minimum tap target: **48 x 48dp**, with icons centered in the target.

### Non-scrolling details

- **Rotary input:** "Consider the use of the rotary scroll button to control elements of the screen when its size is limited, as tapping interactions alone may not provide the best experience" (steppers, pickers, players, fitness).
- **Pagination:** when an experience needs more content but should stay non-scrolling, "consider a multi-page layout with either vertical or horizontal pagination." M2.5 adds that "Users find vertical layouts much easier to navigate than paginated UI's". Paginated pages suit gross-gesture contexts "such as when working out or on the go", mainly workout and media apps.
- Use non-scrollable layouts "only when the content is known or controlled ahead of time." Test them with combined languages, font scaling, devices, and variable content. Non-scrolling screens must be constrained vertically **and** horizontally, which makes them the most at risk of breaking on larger screens.
- Time text, if used, must not overlap the top section. For full-screen progress indicators, use a top gap and percentage margins and padding in the central area.
- Past 225dp, changes must be additive only: the small-screen experience must not break, and added content "should never come at the cost of the glanceability."
- **Preset non-scrolling components** (use these instead of custom builds):
  - **Dialog:** a full-screen transient overlay for a single action. It appears "in response to a user task or an action."
  - **Confirmation overlay:** brief, after an action has executed.
  - **Open on phone:** an overlay with a progress indicator that tells the user when to check the phone.
  - **Stepper:** full-screen range selection, controlled "using the buttons or crown", with a curved level indicator.
  - **Time picker:** up to 3 columns (seconds, 12h/24h). One column at a time; the value in focus is the selection.
  - **Date picker:** up to 3 columns in an order that varies by use case. Only one column is in view at a time, with a hint of its neighbors.
- Custom non-scrolling examples include maps, an emergency overlay, and an emergency alert.

### Scrolling details

- Past 225dp the layout "can alter slightly... so that the content above the fold in the default view is optimized, however all of the same content below the fold should still be available regardless of screen size."
- Text boxes fill the width, so they gain characters as width grows. Cards may gain text rows and icon buttons stretch.
- "The top and bottom margins can change depending on which components sit at the top and bottom", to avoid clipping on the curve.
- Don't add a section label when the app contains a single content type. Settings and preference entry points stay inline, with icons and labels.

## Surfaces

### Tiles

- Immediate, predictable, relevant. Pick **one primary use case**; a few options
  plus "more" beats many options.
- At least one container, every container tappable and functional; no
  decorative containers; fewer containers when they lead to the same place.
- The system shows the app icon; don't draw it. Hide the title on small screens
  when two rows need 48dp targets; bring it back at 225dp.
- Show glanceable graphs, not detailed numbers.
- Design empty, sign-in, error, and no-data states with a clear call to action.
- Customize within the template so the tile carousel stays consistent.

- **One layout, three sections (current guide):** "One fully customizable layout, with 60 or more permutations built into it," built with ProtoLayout Material3 `primaryLayout`: a title slot at the top, a main slot, and a bottom slot. Any slot can hold custom content such as an image or graph. (The older M2.5 guide described two templates, PrimaryLayout and EdgeContentLayout with a progress ring in place of the bottom button; follow the current single layout.)
- **Slots:**
  - **Title slot:** the system draws the app icon. The title gains characters on wider screens.
  - **Main slot:** components set width and height to "expand".
  - **Bottom slot:** a button, text (for example a fitness goal), or nothing ("a default margin is added automatically").
- **Customization:** any slot can take "any content or component" in any variant and color combination, but "these customizations should be limited, and shouldn't deviate from the tile template," so the tile carousel stays consistent. Brand styling is allowed within the template: "primary color, app icon, font, icons."
- **Past 225dp:**
  - Restore a hidden title.
  - Add component slots.
  - Or "add action buttons or content in the bottom section."
  - "Don't just scale up the design": use a larger component style or graphic content with more detail instead. Never show less on the larger screen.
- **Choose the layout by goal (M2.5 guide, still useful as a content decision):**
  - **Text-centric:** lots of text plus a clear CTA. Also used for empty, sign-in, error, and setup states.
  - **Button-centric:** "up to 5 related primary actions", as buttons or a round-button grid.
  - **Info-centric:** high-level metrics and goal progress. With a progress ring, show "progress and one key metric".
  - **Data-centric:** graphs and graphics.
  - Keep text short in info-centric layouts. (Small screens: hide the title slot when two rows need 48dp targets, as above.)
- **Bottom CTA wording (M2.5 guide):** "a word that's short but specific to a particular action or destination", with "More" as the fallback. About 6 characters recommended (max 8) below 225dp, 7 (max 9) above. One line, no truncation, and translations must fit.
- **Monochrome icon:** "Ensure the app icon provided is monochrome if you are having dynamic theming on your tile." The best-practice example uses filled primary and secondary buttons with a tertiary tonal-fill bottom button.
- **States:**
  - Every state has a clear call to action.
  - **No-data** states "Describe what is causing the lack of data and how a user can rectify the issue."
  - **Content-empty** states can be the most common state (a clear diary, for example). Make them feel "complete as a populated state" with an image, or default to text on a tonal background.
  - **Progress** components must handle both empty and overflow values.
- **Ongoing activities:**
  - A long-running activity (workout, music) shows its progress in one or more tiles, with required primary data and a status label, plus an optional icon or graphic and a bottom CTA.
  - A start tile must "Indicate that an ongoing activity is already in progress." Tapping it opens the in-progress activity: "Don't start a new instance."
  - Users typically can't stop the activity from the tile.
- **Motion:** "Emphasize if you're updating information on a tile, such as progress toward a step count goal." "Don't: Unexpectedly toggle between values."
- **Preview image:**
  - 400 x 400px, circular, solid black background, PNG or JPEG, localized for popular languages.
  - Don't show an empty state, don't include the tile icon, and don't add a stroke.
- For platform data such as heart rate and steps, Wear OS controls the refresh rate.

### Media

- Media controls use a **5-button layout**: main play/pause 64dp (80dp at
  225dp+) with a progress ring; side controls; 2 bottom buttons (3 at 225dp+);
  overflow for more.
- Volume through the rotating side button or bezel; show the indicator only
  while rotating. Show the output device.
- Prioritize downloaded media (streaming drains the battery). Tiles show
  selectable media, not play/pause (tile updates lag up to 20 seconds).

- **IA:** a flat hierarchy of **Browse** (prioritizes downloaded items, shows thumbnails) and **Entity page** (context plus key actions: manual download, play, shuffle).
- **Controls:**
  - Adapt the 5-button layout to the content type (music vs podcast or audiobook).
  - Put more than 5 actions behind a visible three-dot **overflow** button, never hidden gestures.
  - The extra bottom slot past 225dp is a shortcut to an important action such as the **playback queue**.
  - The screen is fixed height: top (media details), middle (controls), bottom (configurable secondary buttons).
  - Custom icons and fonts are allowed.
- **Sizes:**
  - Below 225dp: ring 64dp with a 3dp stroke, inner button 54dp with a 26dp icon; side controls 64 x 64dp; 2 bottom buttons at 68 x 60dp.
  - 225dp and up: ring 80dp with a 4dp stroke, button 70dp with a 32dp icon; side controls 72.5 x 80dp; 3 bottom buttons at 58 x 72.5dp.
- **Title marquee:** the artist truncates; long song titles scroll with 8dp edge gradients and an 8dp gap from a fixed (non-scrolling) icon.
- **Principles:**
  - Use the same control patterns across media surfaces.
  - Volume works with the hardware crown or bezel.
  - The output-device icon shows where sound plays and where volume is controlled.
  - Avoid duplicate screens between system and app UI.
  - Reflect dynamic status (volume, connected output).
- **Playback queue:** use the standard pattern, in versions with and without previous songs.
- **Loading:** placeholders that "follow the same structure of the layout and components that are loading."
- **Use cases:**
  - Downloads show location, progress, time, and size (a size dialog).
  - Browse shows recently downloaded media first, plus a Downloads button leading to the full list.
  - A downloaded item offers "remove download", showing the space it uses.
  - When the watch is the source, prompt for audio output (the system output switcher) before playback. Then play and show the output icon (headset or buds) on the controls.

### Always on and haptics

- Always-on layouts limit illuminated pixels and drop frequently updating
  progress indicators.
- Haptics: stronger for key moments (payment confirmation), subtler for
  precision (scrolling). Sync haptics with motion and sound.

### Surface priorities, watch faces, and notifications

From developer.android.com/design/ui/wear (M2.5 surfaces guides, still
current as platform behavior):

- **Split content by priority across surfaces.** Google's weather example: the
  complication answers P1 ("weather right now"), the tile adds P2 ("today"),
  the app adds P3 (hourly breakdown, preferences). Notifications carry only
  P1 alerts.
- **Tiles:** one task per tile (a fitness app ships a goals tile and a workout
  tile); show how fresh the data is ("45 min ago") when it's cached; update at
  most about once a minute for ongoing activities.
- **Watch faces:** time first (checked about 150 times a day), complications
  for glanceable data, customization, black as the primary color, stay within
  the bezel. Always-on mode lights **15% or less** of the pixels.
- **Notifications:** only when worth buzzing the wrist (valuable, glanceable,
  timely); standard, big text, big picture, and messaging templates.

- **Complications:**
  - Glanceable, content forward (visible "by simply raising their wrist"), and context-relevant.
  - A tap opens a specific part of the app, or does a self-contained action (tapping a water-count complication increments it).
  - "WearOS automatically includes an app shortcut complication, so you don't need to create your own."
  - Choose the type by its required field: SHORT_TEXT, ICON, RANGED_VALUE (value, min, max), LONG_TEXT, SMALL_IMAGE, LARGE_IMAGE. Optional fields include an icon, a burn-in protection icon, and a title.

## Behaviors (developer.android.com/design/ui/wear)

- **Navigation:** swipe right to close replaces back buttons. Keep
  navigation vertical and "don't use both vertical and horizontal scrolling"
  (media playback is the exception). The current M3 guide allows multi-page
  non-scrolling layouts "with either vertical or horizontal pagination"; the
  older M2.5 advice against horizontal carousels applies to scrolling
  content. Pannable views (maps) limit the dismiss swipe to a left-edge
  threshold.
- **Physical buttons:** map a multifunction button only to obvious binary
  single-press actions (start/stop, play/pause) in apps used without looking.
  Every mapped action also exists on screen; never map a destructive or
  multi-step action (deleting, stopping navigation, replying).
- **Launch:** black window background with the 48dp circular app icon
  centered (matching the launcher icon). Build the screen gradually: static
  text, buttons, and placeholders first; avoid indeterminate spinners; give
  visual feedback before the work completes.
- **Ongoing activities** (timers, workouts, media): the Recents entry states
  type and status (track name, workout duration, ETA); the tile shows a
  glanceable summary, not detail and actions.
- **Clipping:** test with other languages, larger text, and **Bold text**.
  Calls to action use text that fits the smallest screen; use compact chips
  instead of cards for dense layouts; pad lists so first and last items scroll
  fully into view.
- **Offline:** an offline indicator at the top when features are unavailable
  (gray them out or hide them), at the bottom of a list when no more content
  can load.
- **Sign-in:** Credential Manager with passkeys first, plus passwords and Sign
  in with Google; at least two distinct methods ("sign in on phone" alone fails
  without the phone). Sign-in-only apps show it immediately; others delay it
  until needed and explain the benefit in context. Never name "Credential
  Manager" in UI.
- **Dialogs:** alerts are full screen and interrupt, so use them sparingly;
  left-align alert text longer than three lines. Confirmations only
  acknowledge a finished action; they never ask for a decision.

- **Single prominent action:** "Design each screen to contain a single prominent chip for the primary action." Other actions use lower-emphasis (secondary or outlined) styles.
- **Confirmation not needed when the UI visibly changes:** "In most cases, an explicit confirmation is not needed. A visible change in the UI is enough to show that an action succeeded." Show an overlay only when the change isn't visible (for example, a sent message appears in history instead). Overlay text: max 3 lines, 36 characters (30 for non-Latin scripts).
- **Swipe to reveal** (cards and chips in lists):
  - A partial **left** swipe reveals a primary and an optional secondary action, on the same side in every locale.
  - Tapping the primary action or continuing the swipe commits it: the button extends full width with a label.
  - "For destructive actions, add an undo component": an undo chip replaces the item, then fades and the action completes.
  - Thresholds: under 50% of the width snaps back; 50 to 75% stays revealed; over 75% commits the primary action.
  - Delete and dismiss use the Error color.
- **Swipe to dismiss:**
  - When content also swipes horizontally (pagers, pannable views), "reserve 20% of the edge of the screen" for dismiss.
  - Past 50% of the width, finish the back animation; otherwise snap back. "If the gesture is quick, ignore the 50% threshold."
  - The app view "should never leave the edge of the screen", with a squeeze-like resistance effect.
- **Time text:**
  - Fades and scrolls away with list scroll.
  - Optional leading content (for example a maps ETA), but "the full length of the arc should not be larger than a quarter of the watch face."
  - Curved on round screens, straight on rectangular ones.
- **Pickers:**
  - Items "loop infinitely in both directions" by default. "Consider disabling this behaviour if order in the list is important, or to allow users to reach the first and last element with a quick swipe."
  - Use PickerGroup for multi-part values, such as date and time.
- **Expandable items:**
  - Keep dense content compact with a centered "Show more" (lists) or "More" (text) chip.
  - Recommended collapsed state: **3 list items** or **8 lines** of text. "The tap target consists of the entire text area, not just the button."
  - Expansion animates in one smooth motion.
- **Progress indicators:**
  - Full-screen rings leave a gap "to leave space for important information such as the time."
  - A ring can wrap a component such as a play button.
  - Use indeterminate spinners sparingly.
- **Sign-in edge cases:**
  - Automatic data-layer auth is "the only secondary option which is acceptable to precede Credential Manager". It must be fully automatic with no UI beforehand. On failure, "Don't alert the user", and go straight to Credential Manager. Always offer at least one other method.
  - Apps usable without sign-in: "If sign-in fails, offer the option to skip authentication."
  - "Display all secondary options together." Keep users signed in as long as privacy and security allow.
  - For non-Credential Manager methods, tell the user they're being signed in on first open, then show a confirmation on success.
- **Permission messages:**
  - Request at the moment of need (runtime permissions). Some permissions must also be accepted on the phone.
  - Three patterns:
    1. A full-screen message that opens the permission dialog.
    2. An inline message that opens the permission setting.
    3. A message that opens the permission setting on the phone.

## Gestures (Wear OS 7)

- **Double pinch** triggers the screen's primary action (answer a call, play or
  pause, snooze an alarm); **wrist turn** dismisses (back by default). Pixel
  Watch 3 and newer.
- Use for one-and-done interactions; never hide a gesture-only action (a
  visible button must do the same thing); no dead ends; scroll by gesture only
  when content is glanceable and reached hands-free.
- Floating hints use a tertiary container with onTertiary content; button hints
  match the content color. Gesture success plays a short, crisp haptic.

- On a scrollable page, "your app can map only one UI element at a time to the primary action."
- Double pinch fits only elements reached by gesture, voice, or auto-open (notifications, media controls). If the element is reachable only by touch or buttons, "the action isn't a good use case."
- Wrist-turn overrides must stay within "dismiss, silence, or minimize" (silence a call, close notifications) and "should never be mapped to arbitrary actions." Disable it on risky screens (an active workout, an emergency call) with an empty subscription.
- Gestures must fire the same sound and visual feedback as touch (for example, the camera shutter sound).
- Don't build an in-app gesture tutorial. Rely on the system tutorial and in-context hints.
- Hint cadence is global and system-controlled; floating hints appear at most once per day per experience.

## Wear OS for kids

From `/design/ui/wear/guides/m2-5/foundations/wear-os-for-kids`:

- The audience is ages 6 to 18. Target a narrower band (6 to 8, 9 to 12) or make the app work across the whole band.
- **Short, active, fun sessions:** kids tire holding up their wrists, so "Limit interactions to seconds" and encourage them to come back later.
- **Do:**
  - Use age-appropriate vocabulary and tone, with audio or voiceover where it helps.
  - Use common icons and visual cues.
  - Use gestures the age group can perform, and support tricky interactions during onboarding.
  - Spark off-screen activity; frame fitness positively, with meaningful rewards.
- **Don't:**
  - Port phone or tablet experiences directly.
  - Rely on "large amounts of text or excessive scrolling", or use touch targets that are unresponsive or badly placed.
  - Require motor skills beyond the age group.
  - Use "manipulative in-game reward strategies" (a character harmed or upset if the child doesn't act).

## Compose API reference (verified September 2026)

Verified against androidx-main (`bf95ca5`) and the released
`androidx.wear.compose:compose-material3:1.6.2` sources (1.7.0-rc01 adds the
`OneHandedGesture*` APIs). Everything below is stable and non-experimental.
Source root: `wear/compose/compose-material3/src/main/java/androidx/wear/compose/material3/`.

- **Theme:** `MaterialTheme(colorScheme, typography, shapes, motionScheme, content)`.
  **The default motion scheme is `MotionScheme.standard()`**; pass
  `MotionScheme.expressive()` to get bounce.
- **Springs** (damping / stiffness): expressive spatial fast 0.7 / 800, default
  0.75 / 350, slow 0.8 / 200; standard spatial 1.0 with 1400 / 500 / 260;
  effects no bounce with 1400 / 500 / 260. These differ from the phone values.
- **Dynamic color:** `dynamicColorScheme(context): ColorScheme?` returns null
  when unavailable, so fall back to your own scheme.
- **Shapes:** 4, 8, 18, 26, 36dp (differs from phone). No `MaterialShapes` on
  Wear.
- **Typography:** 21 styles: `arcLarge/Medium/Small` (`CurvedTextStyle`),
  `display*`, `title*`, `label*`, `bodyLarge` to `bodyExtraSmall`,
  `numeralExtraLarge` to `numeralExtraSmall`. `Typography(defaultFontFamily)`.
  Curved text: `curvedText(text, style = MaterialTheme.typography.arcMedium)`
  inside a `CurvedScope`.
- **Edge-hugging button:** `EdgeButton(onClick, buttonSize = EdgeButtonSize.Small, ...)`
  with sizes ExtraSmall 46, Small 56, Medium 70, Large 96dp; place it through
  `ScreenScaffold(edgeButton = ...)`.
- **Button group:** `ButtonGroup(...)`; in its scope, give each button its own
  `MutableInteractionSource`, apply `Modifier.animateWidth(interactionSource)`,
  and pass the same source to the button. Pressed buttons widen and squeeze
  their neighbors.
- **Shape morphing buttons:** `shapes = IconButtonDefaults.animatedShapes()`
  (also `TextButtonDefaults.animatedShapes()`, and toggle `animatedShapes()` /
  `variantAnimatedShapes()`).
- **Morphing lists:** `TransformingLazyColumn` with `rememberTransformationSpec()`;
  per item `Modifier.transformedHeight(this, spec)` and
  `transformation = SurfaceTransformation(spec)`. Items shrink as they near the
  top and bottom edges. A custom spec can override
  `applyContainerTransformation` (for example rotating items at the bottom);
  treat that as a hero-moment option, not a default.

## Open questions

1. Should the design skill cover Wear in its own reference file, or should
   `wear-compose-m3` point to this research?
