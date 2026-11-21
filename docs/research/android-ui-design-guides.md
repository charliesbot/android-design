# Android UI design guides

Research from Android's own design guidance at
[developer.android.com/design/ui](https://developer.android.com/design/ui), the
platform layer that sits beside Material ([material-3.md](material-3.md)).
Material says how components and styles work; these guides say how an Android
app behaves on the platform: system bars, edge-to-edge, adaptive layouts,
settings, onboarding, notifications, widgets, and desktop input. Wear OS
findings live in [material-3-wear-os.md](material-3-wear-os.md).

## Sources and status

Fetched September 2026: Mobile guides (foundations, styles, layout and content,
patterns, home screen, widgets), the widget hub and the widget quality
guidelines (`/develop/adaptive-apps/quality-guidelines/widget-quality`), Desktop guides, the
large screens hub, and the 41-page Gallery of large-screen case studies. Cars,
TV, XR, and AI glasses were skipped as out of scope. Fetched November 2026: the
developer adaptive apps guides, cookbook, and quality guidelines
(`/develop/adaptive-apps`), distilled in
[Adaptive apps (developer guides)](#adaptive-apps-developer-guides).

## What this adds to the Material research

Material covers what a screen looks like; these guides cover platform
behavior Material leaves out. The most useful additions for a design skill:

1. **Android conventions that generated code often gets wrong**, especially
   iOS habits (see [Translating from iOS](#translating-from-ios)).
2. **Platform surfaces with their own rules:** widgets, notifications, live
   updates, picture-in-picture.
3. **Concrete pattern guidance:** settings, onboarding, sign-in, help,
   predictive back.
4. **Desktop and input:** hover, right-click, keyboard, cursors, density.

## Color and theming (platform view)

- "An oversaturated look can result in using only the base color roles of
  primary, secondary, or tertiary. To help with your color hierarchy, apply
  color schemes to include less vibrant container colors and outline roles."
- Use a vibrant primary for the most prominent action; a FAB in a muted tone
  matching the navigation blends in.
- "Apps can be vibrant and expressive, but stick to a palette of colors. Too
  many semantic colors can be confusing, too many decorative colors
  overwhelming."
- Keep semantic colors consistent: if purple means membership, it always means
  membership.
- Surfaces: "Don't be shy to use lots of surface space; the human eye needs
  space to relax."
- Content color: "avoid pulling colors from multiple content pieces"; works best
  with one primary content source.
- HCT hue limits: yellow has little chroma at dark tones; blue peaks at dark
  tones; light blue and bright light red are physically hard to get.
- A primary color can be the brand color, or a separate interactive color so
  brand colors are used more sparingly.
- Always ship a static fallback scheme; without one the system falls back to
  the baseline purple.
- Themes: respect system light and dark, dynamic color, contrast, and font
  size; offering an in-app override is fine; don't lock the app to light theme.
  Use one primary theme source for most of the UI.
- Design systems can extend Material with their own systems (gradients,
  spacing) as additional theme values.
- Use one icon style (outlined, rounded, sharp) across the app.

## Accessibility (platform minimums)

- Body text never below 12sp; always `sp` for text.
- 4.5:1 text contrast; 3:1 between surfaces and non-text elements such as icons.
- Never make color the only affordance; links and actions need a second cue.
- 48dp touch targets even when the visual is smaller.
- Don't rely on gestures alone: every swipe action needs a visible alternative
  (a trash icon beside swipe-to-delete) and an accessibility action.
- Decorative images get a null description; group related elements so screen
  readers can skip between blocks.

- "Consider haptic feedback to help inform the user with additional, real-time sensory input."

## System bars and edge-to-edge

- Draw backgrounds and scrolling content behind the status and navigation bars
  (enforced from Android 15; call `enableEdgeToEdge()`).
- Keep the gesture navigation bar transparent; never add a background to it.
  Three-button navigation gets a translucent scrim when content scrolls under
  it.
- Status bar: transparent when nothing scrolls under it or an image sits there;
  translucent (gradient protection) when content scrolls beneath. Material's
  `TopAppBar` already protects; don't stack protections. In multi-pane layouts,
  each pane gets protection matching its own background.
- Small top app bars collapse to status bar height or scroll off; flexible
  bars compress.
- No tap or drag targets inside system gesture insets.
- Backgrounds, dividers, and carousels draw edge-to-edge (carousels even into
  display cutouts); text and buttons are inset with display-cutout insets.
- Immersive mode (video, reading, games): hide bars only when the benefit
  exceeds "extra space," let a tap reveal controls and bars, and put a scrim
  under overlaid text.
- Sync UI with the keyboard using inset animations; pin text inputs to the
  keyboard rather than letting it hide them.

- **Three-button nav, transparent case:** "Use transparent three-button navigation bars when there is a bottom app bar or bottom app navigation bar, or when the UI doesn't scroll underneath the three-button navigation bar." Set `Window.setNavigationBarContrastEnforced(false)` to remove the system's default translucent scrim, and pad bottom app bars so they draw underneath the system navigation bar. Keep the translucent three-button bar for scrolling content.
- **Status bar icon contrast:** after making the status bar transparent or translucent, "set the style of your system bar icons so that the icons have proper contrast."
- **Insets in every window mode:** "Use WindowInsets to ensure your UI is not obscured by the system bars" on large screens, foldables, and multi-window and desktop windowing modes. Verify that no control sits under a system bar, because "if, for example, the navigation bar covers a button, the user might not be able to click the button." Style the system bars per context too (compact versus expanded, foldable postures), since the adaptive navigation components keep the system bars from interfering with the rail or bar.
- **Keyboard:** use `WindowInsetsAnimationCompat` so the app's transition stays in sync with the IME sliding in and out.
- **Inset types:** system bar insets cover tappable UI that must not be hidden. System gesture insets are OS gesture areas that "take priority over your app". Display cutout insets cover hardware intrusions.
- **Small top app bar:** if the bar is sticky, collapse it to status bar height. If it is not sticky, "add a matching background color gradient" behind the status bar. Medium and large bars collapse to a smaller app bar.
- **Scrolling protection:** use the built-in protection of Material 3 `TopAppBar`, or a custom gradient composable (`GradientProtection` in Views).
- **Navigation drawer:** "navigation drawers should also have a separate protection from the rest of the app." The drawer's status bar area gets its own translucent protection.
- **Bottom app bars:** `BottomAppBar` should collapse while scrolling through its `scrollBehavior` parameter. When the bar animates away, add a system bar scrim for three-button navigation. Gesture navigation stays transparent with no added scrim. `NavigationBar` has no `scrollBehavior` parameter, so keep it visible while scrolling, including the navigation bar `NavigationSuiteScaffold` shows while a foldable is folded.
- **Display cutouts:** solid app bar backgrounds draw into the cutout, while important UI is inset. Never place critical UI at the very edge of the screen.

## Layout

- **Adaptive is the default**, not an afterthought. Never lock to portrait
  (letterboxing when resized).
- **Set max widths:** don't stretch content, buttons, inputs, or tabs full
  width on large screens; change presentation instead (bottom sheet becomes a
  side sheet, list becomes cards or a carousel, extended FAB appears).
- **Think in panes and containment**, not screens: compact one pane, medium one
  or two, large multiple.
- Adapt at the component or pane level with the media query API rather than
  designing whole screens per size, input, and posture.
- Compact margins 16dp; mobile uses a 4-column grid. Choose the grid by content:
  **hierarchical** (editorial, detail screens), **modular** (equal items like a
  gallery), **column** (flexible one-direction flow); manuscript and masonry
  exist too. Break the grid only when content needs it.
- Baseline grid: 8dp for layout and components, 4dp for icons, type, and small
  elements.
- Recommended aspect ratios: 16:9, 3:2, 4:3, 1:1, 3:4, 2:3. Notate how images
  scale and crop per breakpoint (fixed ratio, changing ratio, or fixed height).
- Consistent spacing between like elements; inconsistent spacing "appears
  haphazard." Don't show too many actions per view.
- Pin critical actions (FAB) and inputs (message field) rather than letting
  them scroll away.
- **Navigation pairings:** navigation bar (3 to 5 destinations) or modal drawer
  for primary; tabs or a bottom toolbar for secondary. Don't stretch a bottom
  nav bar on large screens; switch to a rail. A rail can be more ergonomic even
  on a compact cover screen.
- **Control-based layouts reflow and reveal.** Adapt-layout: "For control based layouts, like media players, allow the controls to reflow and content to reveal." On a cover screen, "use an anchor element, like a large play button, to adapt the content around and the hero art becomes the background on the cover screen." Reflow and reveal come as a pair here: the controls move, and the extra space shows content the compact layout hid.
- **Actions:** one FAB for the most important action; secondary actions in the
  top bar or next to their content; rare actions in an overflow menu.
- **Landscape and postures:** landscape phones are medium width with compact
  height (horizontal nav bar items or a rail); tabletop posture suits large
  controls and landscape video; keep controls and text out of the hinge (wider
  gutter); cover screens stay focused, with hero art as the background and a
  big anchor control like play.
- If content didn't scroll in portrait, don't make it scroll in landscape.
- **Scrolling regions.** Content-structure: "You can present content in
  scrolling rows or columns with lazy rows and lazy columns." In a pane, the
  list is the part that scrolls.
- **Scale content to the window.** Content-structure lists "Set the same scale
  on larger screens" as a don't, with the advice "take advantage of the space
  and consider distance from the screen." Content that must keep its aspect ratio should "constrain
  aspect and balance empty space," so leftover space is shared around it rather
  than left as one empty block.

- **Units:** give every spec in dp, and every font size in sp ("Always specify font sizes in sp units"). sp follows the user's font size setting. Never declare measurements in px, because the same pixel size looks larger on low-density screens and smaller on high-density ones. dp = (width in px × 160) / screen density, where 1dp ≈ 1px at 160dpi.
- **Density buckets:** mdpi x1, hdpi x1.5, xhdpi x2, xxhdpi x3, xxxhdpi x4. nodpi and anydpi do not scale and are typically used for vector drawables. Export raster assets for every bucket. Vectors need only one version.
- **Images and graphics:**
  - "Avoid including immutable text in assets."
  - "Use vector formats first whenever possible." Use SVG or VectorDrawable for icons and illustrations, and WebP, PNG or JPG for photos and complex gradients.
  - Use Animated Vector Drawables for small UI animations. Animate graphics in code rather than shipping motion files.
  - "Provide sufficient scrim between background images and text."
  - Icons and small assets carry intrinsic padding (for example, 24dp icons with the padding built in) for touch space and consistent sizing.
  - Tint one asset with blend modes or fills instead of producing many colored copies.
  - Asset names are lowercase with no resolution suffix. Prefix icons with `ic_`.
  - Adaptive app icons are vector drawables, and legacy icons are PNG. Monochrome icons appear in notifications, the status bar and widgets.
  - Decide each image's scaling mode: Fit, Crop, FillHeight, FillWidth, FillBounds, Inside or None. Images can also be clipped to a shape.
  - Draw gradients with Compose `Brush` (color stops, tiling), cropped by containers or shapes.
  - Use `Modifier.blur()` "with caution because they can affect performance and are only available on devices running Android 12 and higher."

### Windows, orientation, and continuity

- Never assume orientation or size: "The natural orientation of the device isn't always portrait, and it might even vary based on user preference." The inner and outer displays of a foldable often differ in pixel density, natural orientation, and resolution.
- Measure the space the app window occupies (Jetpack WindowManager or Material 3 Adaptive window size classes), not the device.
- "It's important not to cache or hardcode any values about display size, window size or orientation since they will change at runtime." Fold and unfold trigger screenLayout, screenSize, and smallestScreenSize changes.
- Keep state across configuration changes (rotation, fold and unfold, resize): scroll position, text entered in fields, component state such as video playback position, and other interactive state, retained with ViewModel or similar. For games, "Support configuration changes gracefully" across orientation, posture, and window size so players "pick up where they left off."
- Camera previews: use CameraX and its preview view so the library handles sensor orientation and scaling.
- A hardware keyboard means the soft keyboard no longer hides part of the layout when a text field is focused.
- Grid and FlexBox (Pawparazzi sample):
  - "Designing for specific pixel-perfect lockups is not only ineffective, it can also negatively impact user experience." Think of content in flexible containers, not only breakpoints.
  - Derive adaptation points (which window size classes, hinge placement, orientations) from the app's primary goal and its content grouping.
  - Use subgrids inside the layout grid, for example a 2-across gallery on compact.
  - Grid is two-directional, and cells can span rows and columns for hierarchy: "The grid may be 2x4, but the top spot spans 2 columns and rows."
  - FlexBox is for one-directional content that responds to its content, for example "filter chips can respond to their labels and the filter area can expand depending on the amount of filters." Reveal more filters on larger screens.
  - MediaQuery handles context (a connected display, a mouse): smaller targets and denser content with precise pointers.
- Tabletop posture for media: "Place playback media above the fold, controls and supplementary content below the fold, for a hands-free viewing or listening experience." Productivity apps can use tabletop for presentations and calls with controls within reach.
- Supporting content on compact is a bottom sheet or dialog. "On larger screens, supporting sheets can open as a pane."
- WebView: "In most cases, we recommend using a standard web browser, like Chrome, to deliver content to the user" (invoke the browser with an intent).

## Translating from iOS

The "10 Steps to Android" guide lists the iOS habits to remove. Useful as an
anti-pattern list for generated UI:

| iOS habit | Android |
| --- | --- |
| Tab bar | Navigation bar (3 to 5), secondary items move to the top app bar or a FAB |
| Centered nav bar title | Title left-aligned by default; large titles become large flexible app bars that collapse on scroll |
| Back chevron | Up arrow for hierarchy; system back and predictive back handle "back" |
| "Cancel" / "Done" text buttons | Icons (close) for dismissal; full-screen dialog app bar for modal tasks |
| Action sheets, share sheets | Bottom sheets |
| Segmented control for sibling views | Tabs (swipeable); for option selection, Material's connected button group |
| Table cells with dividers and disclosure chevrons | Lists with dividers used sparingly and no disclosure indicators |
| iOS toggles and pickers | Material switches, checkboxes, radio buttons, text fields |
| SF Symbols, San Francisco | Material Symbols; Roboto or a brand font |
| iOS motion | Material motion: ripple, container transform, shared axis, fade through |

Design frames at 412dp wide and test at 360dp.

- **When the nav bar shows:** "Your primary navigation should always be present on parent views" (the top level of a section). Child views may keep primary navigation if they sit higher in the hierarchy and are not modal.
- **Leading app bar icon:**
  - With a drawer, every parent view shows the drawer icon.
  - "If your app does not have a rail, or drawer, then parent views don't show a primary navigation icon."
  - Child views show the up arrow.
  - Full-screen modals (video player, image viewer) show close, which dismisses the modal.
- **Large titles:** large titles are "typically reserved for parent views". Collapsing app bars work on parent and child views, but "use your best judgment here as collapsing Top App Bars can take up a lot of space."
- **Alerts:** iOS alerts that need acknowledgment become system dialogs. iOS sheet modals become a full-screen dialog app bar, or a bottom sheet where that fits.
- Tabs are typically attached to the app bar and can be swiped.

## Patterns

### Settings

- Respect system settings; the app may not need its own. Never replicate or
  override device settings (they can be accessibility needs); extend them
  instead (for example more granular theming).
- Include infrequent preferences only; frequent actions live next to their
  feature (captions settings on the video player).
- **Don't put** app info (version, licenses) or account management on the
  settings screen; give them their own destinations.
- Defaults: what most users would pick, low risk, low battery and data, only
  interrupt when important.
- Placement: usually secondary navigation (top bar icon or overflow, after
  everything except Help & Feedback); call it "Settings," never "Options" or
  "Preferences"; accessible when signed out.
- Layout: an overview list of the most important settings with values shown;
  grouped with containment and headings; 15 or more settings means subscreens;
  subscreen title matches the label that opened it; add search for deep
  hierarchies. On large screens, list-detail with the overview as the list;
  never stretch single-pane items full width.
- Controls: switch or checkbox for on/off (never a single radio button); radio
  buttons in a dialog or child screen for one-of-many; slider for ranges or
  imprecise values; dependent settings sit under their parent with a reason
  when disabled.
- Labels: most important words first; neutral terms ("Block," not "Don't");
  impersonal ("Notifications," not "Notify me"); no generic verbs (Set, Change,
  Edit, Manage, Use, Select, Choose); don't repeat the section title. Supporting
  text shows status, not a description of the setting.

- **Quick Settings tile:** for recurring tasks the user completes often, the app can provide a Quick Settings tile in the notification shade panel. It extends device settings and does not replace them.
- **Settings versus filters:** both can save preferences, but "filters are contextual to the current content the user is trying to augment." Filters belong with that content, not on the settings screen. (The scraped page ends at this sentence.)

### Onboarding and sign-in

- Separate what must happen before using the app from what can happen in
  context. Show value before asking for permissions or an account; prime
  permissions at the moment of need.
- Prefer previews of real content over interstitial slides; always offer skip,
  and let people resume later.
- Show progress with steppers or progress indicators, never with decoration
  that could be mistaken for progress.
- Collect minimum data; group related fields; avoid long scrolling forms and
  one-input-per-screen overkill; state password rules; recovery ("Forgot
  password") is always easy to find.
- Passkeys through Credential Manager as the default; don't list every sign-in
  method as separate buttons. Button label "Create a passkey"; "Sign in," not
  "Log in."
- Snackbars for minor confirmations; error copy that focuses on the fix, never
  mocks.
- On expanded layouts, cap form width; never stretch buttons and inputs.

- **Walkthroughs:** "Before implementing a full walkthrough, critically evaluate if your application truly requires one." Often "complex features can be introduced more naturally through subtle motion cues or in-context tooltips."
- **Feature education:** use rich tooltips and dialogs for feature discovery, and sheets for an interstitial onboarding state.
- **Where onboarding goes:** put it up front (welcome) only when registration gates all content, previews are impossible, or in-context learning doesn't fit. Otherwise go contextual (just-in-time).
- **Sign-in:** offer "biometric prompts and auto-fill capabilities". If the app only needs auth for accounts, consider combining registration and sign-in behind one SSO method.
- **Sensitive input:** "sensitive information like passwords should never be pre-filled during a retrieval or reset process. Always default to masking sensitive input." Pre-filling an email is fine. Help users remember or reset a required username.
- **Saved progress:** tell users clearly what happens to their progress when they skip or pause.
- **Passkeys, when to offer creation:**
  - For new users, at account creation, as the default sign-up. Alternatives stay reachable, and the passkey option stays visible on the "Other options" page.
  - For existing users, during password reset or account recovery (prompt at the end of the reset), "immediately after signing in with a password or other method", and in account settings. Avoid creating duplicate passkeys for the same username in the same password manager.
- **Passkey prompt copy:**
  - Show the benefits before starting creation, focused on speed, simplicity and security, and explained as unlocking with your screen lock.
  - "Lead with the benefits of passkeys in the header"; don't emphasize the word "passkey" there.
  - Keep the prompt concise, with a learn-more link.
  - "passkey" is lowercase with an article. Label storage as "Save a passkey to [password_manager_name]."
  - Always show a confirmation after a passkey is created.
  - Sign-in labels: "Sign in", "Sign in with a passkey" or "Sign in with a password". Use "Sign-in" (hyphen) only as a noun.
- **Passkey management in settings:**
  - Each row shows the syncing password manager's name and icon, the created and last-used timestamps, and a "delete" option (no other term).
  - Don't highlight the device where the passkey was created, because users might think it is stored only on that device.
  - Re-offer creation if the user deletes all their passkeys. Link troubleshooting when a passkey fails.
  - Use only the filled version of the Google passkey icon.

### Help and feedback

Secondary navigation (overflow, bottom of a drawer, settings). Standard labels
"Help" and "Send feedback." Most common scenarios first, legal pages last.

### Predictive back

- Full-screen surfaces: exit scales 100% to 90%, enter 110% to 100%, fade
  through at 35% progress, interpolator (0.1, 0.1, 0, 1).
- Shared-element back preview: surface detaches, x shift ((screen width / 20) -
  8)dp, y shift ((height / 20) - 8)dp, scale down to 90%, 8dp margin; feed
  gesture progress through a standard decelerate curve; a fling commits after
  briefly reaching the max preview; cancel springs back.
- Don't suggest the item is being dismissed in the gesture's direction.

## Home screen surfaces

### Widgets

- One primary use case per widget; separate widgets for distinct jobs
  (a steps widget and a streak widget), not one widget per color variant.
- **Canonical layouts** (Jetpack Glance samples exist for each): text only,
  text and image, search toolbar, toolbar, expressive toolbar (sample list
  only), text and image list, checklist, action list, full-bleed image (snap
  scroll, Android 17+), image grid, image and text grid. Snap scrolling needs
  `verticalScrollMode = VerticalScrollMode.SnapScrollMatchHeight(size.height)`
  on the `LazyColumn`, `SizeMode.Exact`, Glance 1.3.0-alpha02 or higher,
  compile SDK 37, and AGP 9.1.0 or higher.
- **Fill the bounds:** misalignment is a main reason users remove widgets.
  Rectangular widgets touch all four grid edges; custom shapes touch at least
  two opposing edges (all four for differentiated quality). No custom padding.
- **Style:** Material color roles and dynamic themes; light and dark; the
  system corner radius for rectangular widgets. Minimal-content widgets
  (photo, weather, now playing) can take a whole expressive shape; data-heavy
  widgets use expressive shapes for hierarchy or the call to action. "Get
  bolder with headlines, labels, and data" through dramatic type scales.
- **Sizes:** "Because cell sizes vary by device and screen orientation, design adaptive widgets instead of fixed-size widgets": optimize for multiple size classes or breakpoints. Use breakpoints to add or remove content as the widget resizes; add 48dp buttons when room allows.
  - Cell estimate on a typical 5x4 handset (Pixel 4, from `/develop/ui/views/appwidgets/layouts`, dp, width by height, portrait then landscape): 1x1 57x102, 127x51; 2x1 130x102, 269x51; 3x1 203x102, 412x51; 4x1 276x102, 554x51; 5x1 349x102, 697x51; 5x2 349x220, 697x117; 5x3 349x337, 697x184; 5x4 349x455, 697x250. For n x m cells, portrait is (73n - 16) x (118m - 16) and landscape is (142n - 15) x (66m - 15).
  - Portrait cell widths inform `minWidth`, `minResizeWidth`, and `maxResizeWidth`; landscape cell heights inform `minHeight`, `minResizeHeight`, and `maxResizeHeight`. For a widget resizable to one Pixel 4 cell, `minResizeWidth` is at most 56dp and `minResizeHeight` at most 50dp. A widget can still be placed larger than `maxResizeWidth` by `maxResizeHeight`.
- **Configuration:** open it on placement only if the widget is empty without
  it; otherwise ship a good default and configure later. One or two screens,
  preview the result, progressive disclosure, no dead ends.
- **Picker:** accurate previews at the real size (and in dynamic colors), a
  clear description, 6 to 8 variations at most. Promote pinning in-app at
  relevant moments, subtly, never blocking the main task.
- **Quality tiers** (`/develop/adaptive-apps/quality-guidelines/widget-quality`):
  - Low quality: doesn't fill bounds, poor contrast, no name in the design, no
    preview, stale content, doesn't update after actions, cropped content.
  - Standard: aligned, sensible min and max sizes (min shows a clear use case
    with 48dp targets), layouts that rearrange instead of scaling text and
    icons, accurate previews, empty and signed-out states that scale to larger
    bounds, manual refresh when data outpaces the UI.
  - Differentiated: fills all four edges, resizes to 2x1 and at least two of
    1x1, 2x2, 4x2, 4x4, a consistent header (icon always, title when space
    allows), scrolling containers for collection and feed widgets, device or
    app color theming, light and dark, previews with the target form factor's
    proportions plus user content or a system theme, unique name and
    description, system corner radius, loading state spec with errors shown on
    the widget, system configuration entry, system launch transition.

- **Unfinished configuration:** "If configuration is not completed, don't cancel adding the widget. Provide a state to allow for restoring or configuring within the widget."
  - Tapping a choice should finish configuration and add the widget. Don't treat this step like in-app settings, and don't make it unclear that closing adds the widget.
  - "Include an empty state if there is no other preset available", showing an onboarding or authentication reminder.
  - For widgets with limited customization, show popular variants directly in the picker instead of a configuration screen. Offer extra variants through a confirmation screen, not more picker entries.
- **Default size:**
  - Set `targetCellWidth` and `targetCellHeight` to match the widget's default grid span, with minimums that meet that target cell on most devices in portrait and landscape.
  - Set them for handheld and tablet separately. Don't use one default size for all form factors.
  - "Don't change the widget's shape or size when the user initially drops it onto the home screen."
  - With no preview, the picker shows only the app icon. Use `android:previewLayout` for accurate previews.
  - Don't publish several same-purpose widgets that differ only in color or shape. Use a configuration activity instead.
- **Shape:** "Don't use fixed square shapes. Instead, use responsive rectangular containers that adapt to various grid dimensions." A non-rectangular shape must touch the grid on one axis.
- **Quality details:**
  - Header (WL-3): recommended for scrolling content, or when it adds useful context such as a list name. Optional for full-bleed widgets (photos), tight space, or redundant content. When used, the icon is always present, the title appears when space allows, and actions come from the widget's context.
  - Resizing (WL-4): a resizable widget defines minimum and maximum sizes, rearranges the layout to show more content as it grows, and does not scale text and icons linearly.
  - Max size (WL-4.1): set a maximum width or height when a larger size only adds blank space. When growth shows more content, such as a taller scrolling list, allow unrestricted resizing (WL-4.2).
  - Minimum size: large enough to show a clear use case and meet the 48x48 dp touch target. The system can scale touch targets for desktop or car screens.
  - Empty states (WT-1): zero and empty states scale to larger bounds without sparse areas. Before login, show a distinct use case or a call to action.
  - Collections (WL-5, differentiated): collection and feed widgets use vertical scrolling containers, and at 4x2 and 4x4 show more items and more detail per item.
  - States (WS-3): use the loading state specification and show errors directly on the widget UI.
  - Search bar exception (WL-1.1, differentiated): a 4x1 widget containing a search bar may touch only 2 edges.
  - Updates (WT-3.2): the widget must update after related actions the user completes inside the app, not only after actions taken on the widget (WT-3.1).
- **Other form factors** (`/design/ui/mobile/guides/widgets/adapt`): tablets and desktops share phone widget principles with different input expectations. Cars start with Android Auto, then cars with Google built-in; high-quality mobile widgets mostly carry over.
  - Build with Jetpack Glance, make widgets responsive (car grids vary), include light and dark themes, use dynamic theming, and fill the grid bounds (WL-1).
  - Car widgets don't support manual scrolling, so design for glanceability. If the widget uses a scrollable `LazyColumn`, offer a version that puts all necessary information on one surface, because the car shows only the first few items. In a scrollable segmented list for auto, put the most valuable, personalized content first.

### Notifications

- Only for timely, relevant value: never ads, "we miss you," rating requests,
  holiday greetings, silent syncing, or self-recovering errors.
- Title under 30 characters without the app name; body under 40.
- Large icon only when it reinforces content (sender's photo is circular;
  other images square), never for branding.
- Up to three actions; don't duplicate the tap-on-body action; offer inline
  replies for short text.
- Pick the template (standard, big text, big picture, progress with cancel,
  media, messaging, call), set channels and importance honestly, group bursts,
  mark sensitivity for the lock screen, and dismiss stale ones.
- Ask for notification permission after explaining the benefit in context.

- **Tap target:** "When the user taps a notification, your app must display UI that relates directly to that notification and lets the user take immediate action" (a turn notification opens that game).
- **Channel importance:** "When an unimportant notification is disguised as urgent, it can produce unnecessary alarm."
  - HIGH: sound and on-screen, for "time-critical information that the user must know, or act on, immediately" (messages, alarms, calls).
  - DEFAULT: sound, for things seen "at the user's earliest convenience" without interrupting (traffic alerts, task reminders).
  - LOW: no sound (subscribed content, social invitations).
  - MIN: "no sound or visual interruption" (nearby places, weather, promotional content).
  - Also assign each notification the best predefined `CATEGORY_*`, since the system uses it for ranking and filtering.
- **Foreground services:** they require a non-dismissible notification, and "you must provide an action for the user to stop the service."
- **Groups:** a parent summarizes its children. "Child notifications must be understandable if they appear solo," because the system may show them outside the group.
- **Colorized:** reserve `setColorized` background color for "high priority notifications such as navigation, ongoing call, or other similar high-priority events." From Android 12 the icon color comes from `setColor`, or the system theme if unset.
- **Messaging:** keep the notification present after an inline reply, and auto-dismiss it only once the conversation pauses. Longer typing opens the app.
- **Media template:** up to 3 actions collapsed, and 5 with an image or 6 without when expanded. A MediaStyle notification with a valid MediaSession token puts the player in Quick Settings.
- **Progress template:** it needs a cancel action; "Non-cancelable activities don't warrant notifications."
- **Never notify:**
  - users who have never opened the app;
  - as the primary way of communicating with users;
  - to cross-promote (prohibited by Play).
- **Permission:**
  - Before the system dialog, show dismissible contextual UI (card, bottom sheet or onboarding screen) that explains the benefit and the cost of declining.
  - "Don't show the notification permission dialog, if the user has dismissed the UI."
  - Provide notification preferences in the app's settings.
  - Media sessions and call apps are exempt from the permission (Android 13+).
- **Status bar icon:** when the app sends many notification types, use a symbol that captures each type's purpose instead of the app icon.

### Live updates and picture-in-picture

- Live updates: only for finite, user-initiated, trackable activities (ride,
  delivery); alert only on critical changes; progress that matches reality;
  the same timestamp format in the status chip and the card.
- PiP: video (and maps) only, nothing but the content in the window, auto-enter
  on home, smooth transitions with a source rect hint; a new selection plays in
  the existing player.

- **Avoid live updates** when the information is bundled from multiple apps, when the notification offers recommendations, when there is no clear end time, or when it needs "bespoke visuals, animations, or unique data structures."
- **Alerts:** "If you do alert, the UI should provide immediate visual evidence of why." Alert on driver arrived, not on an ETA shift.
- **Progress:** discrete steps get labeled phases, and a bar's fill matches the remaining time or distance.
- **Same-vertical templates:** for rideshare, delivery and maps, "apps within the same vertical should use similar fields for similar data points". The content title holds the most critical information.

## Desktop and large screens

- **Principles:** adaptive from the start; "more screen, more done" (denser,
  not bigger); faster motion for small nearby elements, slower or simpler for
  large movements; multitasking inside the app; balance efficiency with
  simplicity; touch, pointer, and keyboard are equally important.
- **Scale:** type a step or two up for viewing distance; max widths on content
  and components; about 60 characters for short copy; progressive disclosure
  as windows grow.
- **Navigation:** rail instead of a stretched bottom bar; tabs at a max width;
  app bars can attach to their pane.
- **Density:** denser data is fine with precise input; tighter click targets
  (pointer targets can go below 48dp, for example secondary hover actions; no
  oversized intrinsic targets).
- **Pointer:** every primary journey works with left click alone; right-click
  opens context menus (not long press); hover shows state, tooltips, and
  secondary actions (checkboxes on hover for selection); instant
  click-and-drag; cursor icons communicate what's possible.
- **Keyboard:** Tab moves in reading order, arrows move within components,
  Escape dismisses temporary UI, initial focus on the key element (search, the
  primary action), visible focus styles, standard shortcuts (Enter sends, Space
  plays) and the Keyboard Shortcuts Helper.
- **Windows:** multi-instance, drag and drop between apps, PiP for playback, a
  customizable window header bar (navigation or search in its middle).

- **Multi-window:** plan for the app running side by side with another app, or as two instances ("research while writing, search while chatting, schedule while video calling"; reading in one window while taking notes in another; reference material beside an editor). On desktop, apps become windowed and resizable. Support drag and drop "within and between apps," including "between widespread views in the same app."
- **Content within panes:**
  - Ask whether turning a list row into a card loses "interaction efficiency and scannability."
  - Set maximum paddings and margins within panes, not only max widths.
  - Use progressive disclosure as the window grows (a hidden description appears in expanded), but only where it adds productivity, not confusion.
  - Consider "letting the user adjust the layout to their preference."
- **Text scaling:** "Consider any content density optional and always allow for text scaling within the layout, don't hard set type sizes."
- **Peripherals (games):** support external game controllers, plus mouse, trackpad, and keyboard on large screens, ChromeOS, and Google Play Games on PC. Some large screens are driven only by a gamepad or a D-pad remote.
- **Stylus:**
  - Low latency input.
  - Hover for tooltips, UI highlighting, and messaging.
  - "Hover the stylus over the canvas to preview the selected brush size and shape."
  - Hover to display media playback and file previews.
  - Palm rejection. Tilt and pressure for stroke width, color saturation, or intensity.

- **Cursor icons:** "Use and set the system cursors to communicate interaction." Common ones:
  - hand pointer for clickable elements;
  - grab or grabbing for draggable elements and during a drag;
  - text (and text vertical) for editable or copyable text;
  - zoom in or zoom out over zoomable content ("use a magnifying glass when hovering over zoomable content").
  - Others: no drop, copy, all scroll, context menu, crosshair, resize arrows and no-resize variants, process spinner, and scribe hover for stylus-writable fields.
  - "Create a custom cursor icon for more specialized actions that are not provided by Android." In Compose this is `Modifier.pointerHoverIcon`.
- **Keyboard focus:**
  - Tab order "automatically adapting to right-to-left for RTL languages".
  - Arrow keys move focus in 2D: "the Right arrow moves focus to the next item in a row and the Down arrow moves focus to the next row".
  - In modal dialogs, "keyboard navigation should stay within the dialog, preventing focus from moving to the underlying page."
  - Follow component key patterns: "when keyboard focus lands on a slider, users expect the Left and Right arrow keys to adjust the value rather than moving focus" (per the ARIA Authoring Practices patterns).
  - Escape acts "strictly as a local 'cancel' command" for dialogs, menus and bottom sheets.
- **Multitasking:**
  - "Media playback should continue when users minimize the app or move it to the background."
  - PiP windows can show play, pause, previous and next.
  - Users switch between multiple instances of the same app with Alt+Tab.
- **Taskbar:** long-press or right-click on the app's taskbar icon shows pin, new window, close, "and App Shortcuts defined by your app". Define App Shortcuts so this menu is useful.

### Gallery patterns by app category

| Category | Large-screen pattern |
| --- | --- |
| Media | Details, reviews, and related titles beside the player; browse while playing; tabletop posture for lean-back viewing; PiP |
| Reading | Two-page spread on foldables; adapted line length; collapsible pane for notes and comments; full-screen reading |
| Shopping | Filters in a supporting pane; detail beside the product list; drag-and-drop cart and wish list |
| Social | Conversations list-detail; comments in a supporting pane; drag and drop to share; Enter sends |
| Productivity | Tabletop layouts for calls; tools and comments without covering the document; file thumbnails with right-click menus |
| Creativity | Movable, resizable palettes; contextual menus; stylus hover, tilt, pressure, palm rejection |

Case studies report real gains (Concepts: 70% more time in app on tablets;
eBay and Google Duo rating improvements), which argues for treating large
screens as a first-class target.

- Hierarchy in large-screen feeds:
  - Showcase best sellers or bargains "with prominent size and position" in a multi-column grid.
  - Categorize in rows or columns and add side navigation.
  - Include synopses or excerpts for select titles.
  - "Promote products by using outsized dimensions and prominent positioning."
- Reading and annotation: "A heads-up control pane puts palettes and tools within easy reach for comments, annotations, notes, and highlighting." Keep comments, notes, and bookmarks in a collapsible supporting pane.
- Media detail: embed a supporting panel "while maintaining an immersive viewing experience."
- Foldable dual-screen: content can show on both screens at once (rear-camera selfies, a dual-screen interpreter). This is hardware-specific.
- Kids (ages 2 to 8, ABCmouse): "Small hands need bigger touch targets and forgiving gestures. Pre-readers need visual and audio cues over text." Build clear progress markers into the flow, and avoid too many choices at once.

## Adaptive apps (developer guides)

From `/develop/adaptive-apps` (guides, cookbook, and quality guidelines), fetched November 2026. These pages are written for developers, so they name the Compose API behind each layout decision. The design rules above still lead; this section adds the thresholds, API choices, and checkable quality criteria the design guides leave out.

### Window size classes in code

- **Height classes:** compact under 480dp, medium 480 to 899dp, expanded 900dp and up. "Most apps can build an adaptive UI by considering only the width window size class," but a phone or open flippable in landscape has medium width and compact height, "in which case two pane layouts are not practical." Check height with `windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)` (the page's example hides the top app bar below it).
- "Window size classes are not intended for _isTablet_‑type logic." "**Avoid using physical hardware values for making layout decisions**": no device allowlists, no `Build.MODEL` checks, no screen aspect ratio.
- **Read the window with `currentWindowAdaptiveInfoV2()`.** The dos-and-donts page names it as the replacement for "the deprecated `currentWindowAdaptiveInfo()` function." The use-window-size-classes page still calls `currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true)`, because without that flag the old function reports only compact, medium, and expanded. Both pages mean the same call: in material3-adaptive 1.3.0, `currentWindowAdaptiveInfoV2()` is defined as `currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true)`, and the old function is deprecated with "Please use V2 version of this function to support L and XL width size classes." Use V2, so the large (1200dp) and extra-large (1600dp) breakpoints are reported.
- **Order of work:** "first optimize your layout for the expanded width size class," then decide whether medium needs its own layout.
- **Layered adaptation:** compute the window size class once near the top of the app and pass it down as state. "A layered approach confines display size logic to a single location." Reusable nested composables never read the global window size: they adapt to the width they are given, with modifiers or a custom layout to change how content is placed, and `BoxWithConstraints` to change what is shown (the page's card drops its description below `maxWidth < 400.dp`). "`BoxWithConstraints` defers composition until the layout phase," so use it only for that.
- **Pass all the data:** "Enough data should be provided to the composable so that the composable always has enough content for any display size." Hoist UI state such as an expanded "show more" so it survives the switch between layouts.
- **No stretched defaults:** "If you use `Modifier.fillMaxWidth`, don't assume that's the right behavior for all display sizes." "Long lines of text are difficult to read. Stretched buttons look poorly designed." "Reposition dialogs to be reachable on all displays."
- **Make layouts scroll:** "Enable app layouts to scroll to verify all content is reachable regardless of the height of the display."
- **Orientation locks don't hold:** on Android 16 (API 36), "The overrides apply to devices with smallest width >= 600dp": the system ignores orientation, aspect ratio, and resizability restrictions (games, by `android:appCategory`, are exempt). In desktop windowing, "apps with locked orientation are freely resizable," and multi-window ignores `screenOrientation`. The cookbook's portrait lock (only when width or height is under 600dp) is "a temporary measure", not a target. "Letterboxed apps are subject to decreased discoverability on Google Play for tablets, foldables, and ChromeOS devices."

### Navigation and canonical layouts in code

- **Scaffolds per decision:** `NavigationSuiteScaffold` (bar or rail), `ListDetailPaneScaffold`, and `SupportingPaneScaffold`. "Don't reinvent the wheel."
- **Bar or rail:** by default `NavigationSuiteScaffold` shows a "**Navigation bar** if the width or height is compact or if the device is in tabletop posture," and a rail otherwise, because the rail's "navigation controls are easier to reach while holding the left and right sides of the device." For an app that "shows a single large pane of content for a feed," the page overrides to a permanent drawer at expanded and keeps the default below. Define each destination's icon, label, and click once; "The destination UI is used across navigation bars, rails, and drawers." Override scaffold colors only with theme values "so your app supports dark and light display modes."
- **List-detail:** `NavigableListDetailPaneScaffold` adds navigation and predictive back. "A list-detail scaffold supports up to three panes" (list, detail, and an optional extra pane). Back behavior: "`PopUntilScaffoldValueChange` (Default and recommended in most cases)." `PopUntilContentChange` returns through previously viewed items. With two panes, picking another item only updates the detail pane, so back "might exit the app or the current navigation flow." Keep the selected item in a `Parcelable` so it survives configuration changes, and wrap panes in `AnimatedPane` for the default pane motion. List-detail also suits settings: "a list of categories with the preferences for each category in the detail pane."
- **Supporting pane:** `NavigableSupportingPaneScaffold` has a main pane, a supporting pane, and an optional extra pane. "In small windows, only one pane is visible at a time," so a button in the main pane opens the supporting pane. On large screens the supporting pane shows a close (X) icon button that navigates back to dismiss it.
- **Supporting pane width:** follow Material's spec, not the canonical-layouts guide's split. The guide says "For medium width, split the display space equally between the main and supporting content. For expanded width, give 70% of the space to the main content, 30% to the supporting content." Material's canonical layout table puts the supporting pane below the focus pane, flexible, at compact and medium, and beside it at a "Fixed (360 dp)" width at expanded ([material-3.md](material-3.md), Canonical layouts: behavior rules). The Compose scaffold agrees with Material: in material3-adaptive 1.3.0 (layout sources read from 1.3.0-rc01), `calculatePaneScaffoldDirective` allows one horizontal pane at compact and medium, two at expanded with a 360dp preferred width for the non-main pane and a 24dp spacer, and three at large and up with 412dp. The navigators and Navigation 3 scene strategies build their default directive from `currentWindowAdaptiveInfoV2()`, so the supporting pane gets Material's layout by default, and the 50/50 and 70/30 split appears nowhere else. Two panes at medium are an opt-in (`calculatePaneScaffoldDirectiveWithTwoPanesOnMediumWidth`), matching Material's rule that medium two-pane layouts suit only low-density content, split 50/50.
- **Feed:** a `LazyVerticalGrid` with "`GridCells.Adaptive(minSize = 180.dp)` creates a grid where each column is at least `180.dp` wide," showing as many columns as fit. Headers and dividers "use `maxLineSpan` to take up the full width of the layout." "Content groups are created by making elements the same size and positioning them together."

### Grid, FlexBox, and lazy grids

Grid and FlexBox are stable from Compose 1.13 (BOM 2026.11.01). Pick by structure:

| Need | Use |
| --- | --- |
| 2D structure of a screen or complex component (tables, dashboards, header plus content plus footer) | `Grid` |
| A small set of items in one direction that wraps (chips, tags, action rows) | `FlexBox` |
| A large, homogeneous, scrolling data set | `LazyVerticalGrid`, `LazyHorizontalGrid`, `LazyVerticalStaggeredGrid`, lazy lists |

- Lazy grids "are NOT designed for the structural layout of a screen or complex component." Nested `Row` and `Column` for a 2D layout give "deep hierarchies and difficulties in adaptability." A table in `Grid` keeps columns aligned "without manually synchronizing widths."
- Neither `Grid` nor `FlexBox` is lazy. "`Grid` does not recycle items and has a limit of 1000 tracks," so put a lazy list inside a grid cell for long content rather than looping items into the grid.
- "Placing a lazy list inside a standard flexible track causes an `IllegalStateException` crash." Give that track `GridTrackSize.MinMax(0.dp, 1.fr)`, and header and footer rows `MaxContent`. A plain flexible track "can shrink to `0.dp`," so use `MinMax` wherever content must not be crushed.
- Named areas (`area(...)` plus `Modifier.gridItem(areaId)`) let a breakpoint reshape the grid "without needing to modify the order or parameters of the child composables." Coordinates suit simple grids; "For larger or more complex layouts, define named areas instead." Spans build hierarchy (a hero cell spanning two columns and rows). "You can't place a UI element into a gap, but you can span a UI element across it."
- Overlays (badges, status marks, layered cards) can share a grid cell: "Items declared later in the `Grid` content block are rendered on top of items declared earlier."
- **FlexBox:** "If you need to wrap items, use `FlexBox` instead of `FlowRow` and `FlowColumn`." "For an overall screen layout, `Grid` is usually a better choice." Wrapping is off by default ("`NoWrap` (default)"), so a chip row must opt in, or items overflow and get clipped. Use the container's `gap` instead of spacing modifiers on children. "By default, items won't grow": `Modifier.flex { grow(1f) }` fills a row, and grow values split extra space relative to siblings. Default shrink is `1f`, so give an item that must not truncate `shrink(0f)`. `order` moves items visually without changing declaration order. Row direction follows layout direction, so RTL flips automatically.

### Foldables and postures

- **Inner display shapes:** square (1:1) inner displays "Generally fall into the medium or expanded window size class in both portrait and landscape orientations." 4:3 inner displays "Reach the expanded window size class in landscape orientation" and pair with a 16:10 outer display.
- **Folded and unfolded:** optimize each separately ("a single column layout with a bottom navigation bar is straightforward but effective" when folded). On unfold, keep the same hero content and add complementary content: "the unfolded screen should maintain continuity by showing the same image and description."
- **Half opened:** Split content into two areas when half opened: "top and bottom in tabletop posture, left and right in book posture." "Position dialog boxes and pop‑up menus so they don't overlay the fold." Text over the fold "can be hard to read (or unreadable if `occlusionType` is `FULL`)." "Don't place UI controls too close to a fold or hinge when `isSeparating` is true." A `HALF_OPENED` device always reports `isSeparating`. Dual-screen hinge devices use tabletop and book layouts "even if the `FoldingFeature.State` is `FLAT`." The fold angle is not exposed. Trifolds "don't support tabletop posture and cannot be used `HALF_OPENED`."
- **Posture use cases:** book posture is for "Reading lengthy text content"; tabletop posture is for "Video calling and video or audio playback."
- **Tabletop recipe:** detect it with `mediaQuery { windowPosture == Posture.Tabletop }` (reactive, no activity restart) and lay out a `Grid` with two equal rows: player or game canvas on top, controls on the bottom. In other postures the controls overlay the player by sharing its cell. "Ensure state and scroll positions are preserved when transitioning between standard full-screen layouts and tabletop posture."
- **Orientation:** "Determine the app's orientation based on the actual screen area the app occupies, not the device's physical orientation." Size classes on foldables and trifolds "can shift between compact, medium, and expanded window size classes instantly."

### Context-aware UI with MediaQuery

- `mediaQuery { }` reads `windowWidth`, `windowHeight`, `windowPosture` (tabletop, book, flat), `pointerPrecision` (fine, coarse, blunt, none), `keyboardKind` (physical, virtual, none), `hasCamera`, `hasMicrophone`, and `viewingDistance` (near, medium, far). It is experimental and needs `ComposeUiFlags.isMediaQueryIntegrationEnabled = true`.
- Use `derivedMediaQuery` (not `mediaQuery`) for `windowWidth` and `windowHeight`, which change often, comparing against the `WindowSizeClass` lower-bound constants.
- **Targets by pointer:** size controls by `pointerPrecision` (larger for blunt). "If multiple pointing devices are available and their precisions are different, the parameter is resolved with the highest one."
- **Keyboard:** a hardware keyboard reports physical even while the on-screen keyboard shows. Attaching or detaching one "can cause a loss of UI state," so save state. Change the layout on attach only "In special cases," such as making room for tools or editing windows.
- **Hardware:** "Some devices don't support cameras or microphones." Show camera and microphone buttons only when `hasCamera` and `hasMicrophone` say the device supports them.
- **Viewing distance:** "If the user is using the app from a distance, they would expect the text and UI elements to be bigger" (the page's example steps body text 16sp near, 18sp medium, 20sp far).

### Desktop windowing and connected displays

- **Header bar:** "All apps running in desktop windowing have a header bar, even in immersive mode." Draw into it by making the caption bar transparent and padding with `WindowInsets.captionBar`. Keep content clear of the system window buttons (`WindowInsets.getBoundingRects()`), and request `setSystemGestureExclusionRects()` for interactive content there, such as tabs (as in Chrome), "search bars, or profile avatars."
- **Desktop UI mode:** switch to "a desktop UI, which typically has higher information density, different navigation patterns, and optimized mouse interactions" when the window is engaged with a precise pointer, not by device type.
- **Multi-instance:** offer "New Window" and let users drag a tab or document out into a new instance; "Users can also move elements between different instances of the same app." "Don't confuse multi-instance with a multi‑pane layout, such as a list‑detail presentation, which runs in a single window."
- **Window size:** "Not all apps, even if resizable, need a large window to offer user value." Set default and minimum sizes in the manifest's `<layout>`. Below the minimum in split screen, "the system crops the activity to the size the user requests," so design down to that minimum.
- **Visible but not focused:** "activities that play video _not_ pause video playback in response to the `ON_PAUSE` lifecycle event." Keep updating media, messages, and progress while another window has focus. Apps "cannot hide the status bar if they are running in multi-window mode."
- **Connected displays:** "If you hardcode values based on the phone's screen, for example, your layouts will likely break on an external display." Density and size can change when a window moves between displays, and the activity may be recreated on disconnect, so keep state. A phone on an external display runs a separate desktop session; a tablet in desktop windowing extends one session across both.
- **Handoff to a larger window:** a phone's full-screen detail maps to a selected item in list-detail: "Populate both the primary list selection and the detail pane so the desktop window has complete context."

### Input

- Compose 1.7 and higher give "Keyboard tab navigation and mouse or trackpad click, select, and scroll" by default. "All apps should support keyboard navigation and mouse input for accessibility and usability."
- **Spacebar:** "Whenever your app plays a media file, users should be able to pause and resume playback by pressing the Spacebar on a physical keyboard." For full-screen video, handle it at the screen level on key up.
- **Stylus:** "Use the Jetpack Material 3 library to enable users to write into any `TextField` component using a stylus." Drawing apps cancel and roll back a stroke on a canceled touch, and "must keep a history of user actions so that unintended inputs such as palm touches can be undone."

### Quality criteria

The adaptive app quality tiers (tier 3 basic, tier 2 optimized, tier 1 differentiated by experience) and the core app quality checklist add these UI criteria:

- **Line length (core):** "limiting line length to 45-75 characters (including spaces) for each of the app's supported languages."
- **Parity (core):** "Display orientations and fold states expose essentially the same features and actions and preserve functional parity." "Minor letterboxing to compensate for small variations in screen geometry is acceptable."
- **Back (core):** "App supports standard back button navigation and does not make use of any custom, onscreen back button prompts," and supports gesture navigation. The app bar's up arrow is navigation, not a back prompt.
- **Themes (core):** "The app's content, and all web content accessed by the app, support both light and dark themes." "Verify that all text is readable in light and dark themes."
- **Graphics (core):** "No aliasing at the edges of menus, buttons, and other UI elements," and no "noticeable distortion, blurring, or pixelation."
- **Feedback (core):** show a progress indicator "if the app takes longer than two seconds to load." Audio starts within a second, or the app shows that it is preparing. Share through the Android Sharesheet. The app "gracefully degrades when users deny or revoke a permission."
- **Configuration changes (tier 3):** "App is not letterboxed; it does not run in compatibility mode." "Content does not overflow the available display area." "Text typed into text fields is retained and the keyboard state is restored." Combinations hold too ("window resizing followed by device rotation, or rotation followed by device folding or unfolding"), and on desktop, minimize, restore, and maximize keep state.
- **Mouse basics (tier 3):** click, select, and scroll every scrollable element "both vertically and horizontally."
- **Secondary elements (tier 2):** "Bottom sheets are not full width on large screens." "Buttons are not full width on large screens." "Text fields and boxes don't stretch to full width on large screens." "Small edit menus or modals don't cover the entire screen and maintain context for the user as much as possible." "Context menus appear next to the item the user selected." "Navigation drawers are updated to expanded navigation rails." "Images are displayed at a proper resolution and are not stretched or cropped."
- **Responsive layouts (tier 2):** "Trailing‑edge panels that are open by default on desktop screen sizes; closed, on smaller screens," "Grid layouts that scale the number of columns to accommodate window size changes," and "Columns of text on large screens."
- **Keyboard and mouse (tier 2):** a custom interactive element "must be focusable when the device is not in Touch Mode, and a visual indication of the focused state must be apparent." Enter sends in communication apps; Space plays and pauses media. Esc exits: it "Clears search text or removes search focus," "Deselects all selected items," "Aborts renaming/editing without saving," and "Cancels Up Next / autoplay timers." Content zooms with Ctrl plus the scroll wheel and with trackpad pinch. "Actionable UI elements have hover states (where appropriate)." Standard shortcuts cover select, cut, copy, paste, undo, and redo.
- **Desktop (tier 1):** "App displays a scrollbar while content is being scrolled by a mouse or trackpad." Hover reveals "previews, fly‑out menus, and informative tooltips." Hover and focus states are "distinct and consistent" with contrast "that supports accessibility standards." Prefer "desktop-style menus, context menus, and small modals" over navigating away from the task. "Adaptive layouts enable users to switch between list, grid, and column presentations"; floating toolbars can be docked or moved; panels are "collapsible and reconfigurable using a mouse or trackpad" (not navigation bars, rails, and drawers). Initial focus lands on the first field of the task ("focus is initially on the To text field"). Ctrl and Shift click select multiple or ranges; triple click selects a line or paragraph. Cursor "target sizes that match visual target sizes." Multiple dropped items are processed as a group.
- **Foldables (tier 1):** media controls move to the horizontal half in tabletop; camera previews adjust between folded and unfolded and between front and back screens.
- **Stylus (tier 1):** erase with the stylus, drag and drop with it, "More pressure should produce thicker strokes," and "The more the stylus is tilted, the wider and lighter the shading strokes should be." Low latency with motion prediction.

## Compose UI (developer guides)

From `/develop/ui/compose`, fetched November 2026. Like the adaptive guides, these pages are written for developers: they name the Compose API that carries each accessibility, theming, motion, graphics, and text decision. Material's design rules ([material-3.md](material-3.md)) still lead; these pages say how to express them in Compose. Several pages show baseline Material 3 snippets (Roboto, `NavigationBar`, a 24dp `extraLarge`); the skill's Expressive choices override those.

### Accessibility semantics

- **Touch targets come with the callback:** Material controls pad to 48dp only when interactive: "when a `Checkbox` has its `onCheckedChange` parameter set to a non-null value, the checkbox includes padding to have a width and height of at least 48 dp." For a whole row that toggles, "lift the clickable behavior to a parent container by setting the click callback on the composable to `null`, and adding a `toggleable` or `selectable` modifier to the parent composable," with a `role`, so the row is one target and one node.
- **Small visuals:** `Modifier.minimumInteractiveComponentSize()` reserves a 48dp target around an element that must look small. "Put it before any size modifier, because a size modifier before it limits the space that it can reserve." "To prevent possible overlap between touch areas of different composables, always use a large enough minimum size for the composable."
- **Content descriptions:** "`contentDescription` is mainly meant to be used for graphic elements, such as images." Decorative images pass `null`; `Text` and labeled buttons already carry their text.
- **Action labels:** set `onClickLabel` (and `onLongClickLabel`) so TalkBack says what a tap does, "Double tap to open this article", rather than "Double tap to activate".
- **Custom components:** "make heavy use of Foundation modifiers, as these include built-in accessibility considerations" (`toggleable`, `selectable`, `triStateToggleable`) rather than a bare `clickable`. "**when you're adding custom low-level composables, you have to manually provide its semantics**": a `Canvas` or custom `Layout` exposes nothing on its own.
- **Merging:** "`clickable` and `toggleable` merge their children's semantics by default," and so do Material buttons and "the `ListItem` composable will merge its descendants." Don't add `mergeDescendants` to them. Group a non-clickable composite (avatar, name, metadata) with `Modifier.semantics(mergeDescendants = true) {}`. "A parent cannot merge its children when the children are also merging," so a bookmark button inside a clickable row stays its own node, which is how a list item keeps a second action.
- **Hide or clear:** "Use `hideFromAccessibility` when content is possibly decorative or redundant, but still must be tested" (a watermark, a separator dot). Use `clearAndSetSemantics` only when merging is not enough, such as a custom toggle that must state its role and state. "Semantics information can potentially be used by AI agents and similar services to understand the screen, and thus should only be cleared when necessary." "Use `clearAndSetSemantics{}` with an empty lambda when parent and children semantics need to be cleared for all services."
- **Headings:** mark section titles with `Modifier.semantics { heading() }`, whatever their text style, so screen reader users can jump between sections.
- **Live regions:** a snackbar-like status message takes `liveRegion = LiveRegionMode.Polite`; assertive is for crucial, time-sensitive content. "Live regions shouldn't be used on content that updates frequently, such as countdown timers, to avoid overwhelming users with constant feedback."
- **Custom windows, errors, progress, collections:** "Window-like custom components, similar to `ModalBottomSheet`, need additional signals to differentiate them from surrounding content": give them a `paneTitle`. Custom error states set `error(...)` semantics with the fuller message, custom progress sets `progressBarRangeInfo`, and custom lists and grids set `collectionInfo` and `collectionItemInfo`. `stateDescription` overrides the spoken state of a toggle (for example "Subscribed").
- **Custom actions:** gestures such as swipe to dismiss need a labeled `customActions` entry. When a row's several actions move into the container's custom actions, "manually clear the original children's semantics with the `clearAndSetSemantics` modifier," so Switch Access and Voice Access traversal stays short.
- **Traversal order:** the default order runs by position, so side-by-side cards can read across rather than down. "Setting `isTraversalGroup = true` on a node means that all children of that node are visited before moving to other elements." "`isTraversalGroup` is `true` by default on scroll containers (like `LazyColumn`) and Material surfaces." `traversalIndex` goes on a focusable child: "Setting only `traversalIndex` on, for example, a `Column` would have no effect, unless the column has `isTraversalGroup` set on it as well."
- **In-app zoom (optional):** if an app offers pinch to scale content, use density scaling for visually structured layouts (feeds, carousels, grids) and density or font scaling for reading layouts, both of which reflow: "The reflowing nature avoids horizontal panning, which would conflict with nested scrolling elements." Also "consider providing an alternative way to adjust or reset the scale that doesn't rely on gestures."

### Theming in code

- **Extend Material:** the custom design systems page offers three paths: extend `MaterialTheme`, replace some of its systems, or build a fully custom system. For a Material 3 Expressive app, "Extend `MaterialTheme` with additional theming values."
- **How to extend:** extension properties (`ColorScheme.snackbarAction`) suit a few values that are the same in every theme: "This approach is only recommended for straightforward theming value additions, or for values that are the same in different themes. If you have multiple themes, it's better to define a class with new properties instead." New color roles (a `caution` and `onCaution` pair) go in an `@Immutable` class provided through `staticCompositionLocalOf` by a wrapper theme that nests `MaterialTheme`.
- **Wrap, don't scatter:** for components that use the extended values, "wrap them in your own composable functions, directly setting the values you want to alter, and exposing others as parameters to the containing composable."
- **Replaced systems:** if typography or shapes are replaced, Material components still draw Material's defaults, so wrap them and "you may need to wrap `content` lambdas in provider functions (like `ProvideTextStyle`)." A fully custom system also loses text selection colors and the ripple indication: "If you want to continue using Material components, you must replace some of these systems in your custom themes or handle the systems in your components."
- **Ripple:** customize with `LocalRippleConfiguration`. "Starting with `material3` 1.5.0-beta01, you can also provide `LocalRippleThemeConfiguration` to configure the ripple theme for all Material components."
- **Dark-dependent values:** "the M3 `ColorScheme` class doesn't include an `isLight` parameter. In general, you should try and model whatever needs this information at the theme level," for example a theme-provided value.
- **Elevation:** "Material 3 represents elevation mainly using tonal color overlays." `Surface` takes `tonalElevation` and `shadowElevation` separately. "The default color used by `tonalElevation` in M3 is `primary`. You can override this by using the `surfaceTint` parameter in the `ColorScheme` class." "Tonal elevation in M3 replaces the concept of elevation overlays in M2 dark themes." The Expressive research prefers the `surfaceContainer` roles over tint ([material-3.md](material-3.md), Migrating elevation-tinted surfaces).
- **Emphasis without alpha:** high emphasis is `onSurface`, medium is `onSurfaceVariant`, and text emphasis can also come from weight ("`FontWeight.Medium` - `FontWeight.Black` for text" for high emphasis). Disabled is the one alpha case: "For disabled states in M3, it's still acceptable to use “on-x” (where x can be primary, secondary, surface etc.) colors with alpha values" (`onSurface.copy(alpha = 0.38f)`).
- **Role pairs:** "Use on-primary on top of primary, and on-primary-container on top of primary-container, and the same for other accent and neutral colors." Neutral pairs give emphasis without accent: "surface can be used with on-surface-variant and surface-variant can be used with on-surface to provide different levels of emphasis." The tertiary role is for "contrasting accents that can be used to balance primary and secondary colors or bring enhanced attention to an element."
- **Fixed shapes:** `none` and `full` are not slots in `Shapes`: "Use `RectangleShape` and `CircleShape` respectively for these."
- **Dynamic color fallback:** "you almost always need a fallback custom color scheme given the Android 12 and higher restriction."
- **Type scale:** "Your product will likely not need all 15 default styles from the Material Design type scale." (The same page's note that `Typography` lacks a default font family is out of date; see [material-3.md](material-3.md), Compose API reference.)
- **Styles API:** the experimental Compose Styles API stays experimental in Compose 1.13 and is being redesigned, so generated code should not use it.

### Motion in code

- **Springs are the default for a reason:** "`spring` can handle interruptions more smoothly than duration-based `AnimationSpec` types because it guarantees the continuity of velocity when target value changes amid animations." "`spring` is used as the default AnimationSpec by many animation APIs, such as `animate*AsState` and `updateTransition`." Keyframes suit preset animations; "if you're working with user-driven points, it's preferable to use springs to achieve a similar smoothness between points because those are interruptible." The pages' samples use `tween(...)` durations as API demos, not as rules; the skill's `MotionScheme` springs still apply ([material-3.md](material-3.md), Motion).
- **Touch wins:** "you might need to interrupt an ongoing animation when touch events begin, because user interaction should have the highest priority." Gesture-driven values use `Animatable`: stop on touch down, `snapTo` while dragging, decay on fling, and animate back to rest. `animate*AsState` retargets on its own: "If there's already an animation in flight, the animation starts from its current value (and velocity) and animates toward the target value."
- **Which API:**

| UI change | API |
| --- | --- |
| Switch screens with navigation | The navigation library's enter and exit transitions (Navigation 3: `NavDisplay` `transitionSpec`, `popTransitionSpec`, `predictivePopTransitionSpec`, overridable per entry with metadata) |
| Switch content in place | `AnimatedContent` (default "fade through"), `Crossfade`, or a pager |
| Show or hide | `AnimatedVisibility` (default fade plus expand in, fade plus shrink out) |
| Size change | `Modifier.animateContentSize()` |
| List items appear, move, or leave | `Modifier.animateItem()` |
| Several properties that start together | `updateTransition` |
| One independent property | `animate*AsState` |
| Sequenced or gesture-driven values | `Animatable` |
| Ambient looping motion | `rememberInfiniteTransition` |
| Text that scales, moves, or rotates | `TextStyle(textMotion = TextMotion.Animated)` with `graphicsLayer` |

- **Hide for real:** animating alpha leaves a node in place: "the composable **remains in the composition** and continues to occupy the space it's laid out in," so screen readers still find it. Use `AnimatedVisibility` when content should go away. Custom effects inside `AnimatedVisibility` must run on its transition: otherwise it "might remove the content composable before they finish."
- **Animate in the cheapest phase:** "choose the lambda version of a `Modifier` where possible. This skips recomposition and performs the animation outside of the composition phase, otherwise use `Modifier.graphicsLayer{ }`, as this modifier always runs in the draw phase." Animate a color with `drawBehind`, not `Modifier.background()`. "If you are animating the shadow, using the `Modifier.graphicsLayer{ }` modifier is the more performant option." `Modifier.offset {}` moves an element without moving its siblings; use a layout modifier when siblings should respond.
- **`animateContentSize` order:** "place it _before_ any size modifiers such as `size` or `defaultMinSize`."
- **Shared elements:** "The most common use case for `sharedBounds()` is the container transform pattern, whereas for `sharedElement()` the example use case is a hero transition." "`sharedBounds()` is for content that is visually different but should share the same area between states, whereas `sharedElement()` expects the content to be the same." "When using `Text` composables, `sharedBounds()` is preferred to support font changes," and "For `Text` composables, `scaleToBounds()` is recommended, as it avoids relayout and reflowing of text onto different lines."
  - Use them inside `AnimatedContent`, `AnimatedVisibility`, or the navigation host, which manage the transition. Wrap `NavDisplay` in `SharedTransitionLayout` and read `LocalNavAnimatedContentScope`.
  - "Be consistent with the order of modifiers on the matching items." Keys must be unique per list item; prefer data-class keys over strings.
  - "No interoperability between Views and Compose is supported. This includes any composable that wraps `AndroidView`, such as a `Dialog` or `ModalBottomSheet`."
  - `boundsTransform` takes any spec ("You can use any `AnimationSpec`"), so pass the theme's spatial spring. Keep a bottom bar or FAB above the transition with `Modifier.renderInSharedTransitionScopeOverlay()`, and let siblings move with `PlaceholderSize.AnimatedSize`.
  - Shared elements work with predictive back: "All versions of Navigation 3 support predictive back." "Predictive back animations are enabled by default on devices running Android 15 (API level 35) or higher."
- **Lazy lists:** "Be careful when using `LaunchedEffect` inside lazy layouts. It relaunches when the items re-enter the composition."

### Graphics

- **Blur limits:** "Uniform blurs require Android 12 (API level 31) and higher, and progressive blurs require Android 13 (API level 33) and higher. On lower versions, Compose renders the content unblurred." Progressive blur is "available in `androidx.compose.ui:ui` 1.13.0-beta01 and higher." Nothing may depend on blur for legibility or hierarchy, since older devices show the unblurred content.
- **Blur is not frosted glass:** "`Modifier.blur` blurs the content that a composable draws itself," which the docs call "a surface or layer blur, as distinct from a backdrop blur, which blurs whatever is drawn behind a composable." So it cannot blur what lies behind a card or sheet. "Applying `Modifier.blur` renders the composable into a separate graphics layer," which is its cost.
- **When to blur:** the examples use blur as a focus device: softening items near the edges of a scrolling list or picker "while keeping the active center region in sharp focus," a drag-to-move reading spotlight over text, or a hero image "progressively blurring the lower-right region behind the overlaid text" while the subject stays sharp.
- **Uniform and directional blur:** `Modifier.blur(radius)` blurs both axes equally; `radiusX` and `radiusY` give "a directional motion-blur effect," and "both `radiusX` and `radiusY` must be greater than `0.dp` for the blur effect to apply."
- **Progressive blur API:** pass a `BlurRadiusSpec` to `Modifier.blur`. Factories: `verticalGradient` (top `0f` to bottom `1f`), `horizontalGradient` (left to right), `linearGradient` (between two `DpOffset` points), `radialGradient` (outward from `center` to `fallOffRadius`; unspecified `center` is the composable's center and unspecified `fallOffRadius` is "half of the layer's smaller dimension"), `uniform`, and `shader` (a custom `Shader`'s alpha scales `maxRadius`). Gradients interpolate linearly; to keep "a middle section completely sharp" or blur several edges, pass "2 to 16 `BlurStop` entries," each a `fraction` (`0f..1f`, sorted automatically) and a `radius` in `Dp`. "Spatially varying blurs cap at 150 pixels once converted, whereas `BlurRadiusSpec.uniform` has no cap."
- **`Modifier.blur { }`:** the block runs in `BlurScope` with `size: DpSize`, `blurRadiusSpec`, `edgeTreatment`, and `alpha`. "Because the `BlurScope` block runs during the graphics layer phase, after measurement and layout, you can compute gradient geometry from `size` without triggering recomposition." The diagonal hero example uses it for `end = DpOffset(size.width, size.height)`, and the spotlight example reads pointer state there.
- **Edge treatment:** "the blurred output extends beyond the original content bounds by the blur radius." `BlurredEdgeTreatment.Rectangle` (default) clips to the bounds; `BlurredEdgeTreatment(shape)` clips to a shape and "keeps the blurred result sharp at the shape boundary"; `BlurredEdgeTreatment.Unbounded` does not clip, so outer edges "appear fuzzy and blurred beyond the original bounds." On images, "it is recommended to use `BlurredEdgeTreatment(Shape)`, instead of `BlurredEdgeTreatment.Unbounded`, as the latter is used for blurring of arbitrary renderings that are expected to render outside the bounds of the original content" (images/customize). The scrolling-list and ruler examples use `Unbounded` on the `LazyColumn` and `Canvas`. Below the required Android version, "`BlurredEdgeTreatment` shape clipping and alpha settings still apply."
- **Shader masks:** `BlurRadiusSpec.shader` takes a custom `Shader` such as an AGSL `RuntimeShader` (Android 13) for "repeating patterns, arbitrary shapes, or procedural noise"; create the shader and spec once with `remember` and set size-dependent uniforms in its configuration block.
- **Shadows:** "You can only use elevation with `Modifier.shadow()`, and cannot customize properties like shadow spread or radius." `dropShadow()` and `innerShadow()` take radius, spread, color, offset, or a brush, and any shape: "It can use any geometry from the `androidx.compose.foundation.shape` package, as well as the Material Expressive shapes." An inner shadow "creates the illusion that an element is recessed or pressed into the underlying surface." Order matters: "If the `innerShadow()` is placed before the background, the background is drawn over the shadow, hiding it completely." The page also shows neumorphic, neobrutalist, and realistic shadow styles; those are styles outside Material, and Material's rule (tonal surfaces first, shadows only where needed) still decides when a shadow appears ([material-3.md](material-3.md), Elevation).
- **Gradients:** "use mesh gradients instead of stacking multiple `radialGradient` or `linearGradient` brushes" for "fluid, organic, or multi-point backgrounds" (`MeshGradientPainter`). "`TileMode.Decal` is only available on API 31+." "`RuntimeShaders` only works on Android 13+. Wrap your Composable in an API if-else check and provide a suitable fallback." "To get a smooth transition between colors - set the last color to the start color" of a sweep gradient. Brushes also color text and image fills.
- **Masking:** "Many of the `BlendModes` that involve alpha won't work as expected without an offscreen buffer," so a cutout (an avatar badge notch) needs `CompositingStrategy.Offscreen`.
- **Shapes:** "The `graphics-shapes` library is not intended for use for arbitrary shapes"; use a path or vector for those. `RoundedPolygon` takes `CornerRounding(radius, smoothing)` and `perVertexRounding`. A custom shape set on `graphicsLayer` with `clip = true` lets the shadow follow the clipped outline ("this technique can be used to clip an image and apply a shadow around the clipped region").
- **Image scaling:** `ContentScale.Fit` is the default and scales small images up. `Crop` center-crops (avatars, album art, hero images). `FillBounds` "distorts images if you place them in containers that don't match the exact ratio of the image." `Inside` and `None` never scale up. Fix crops with `Modifier.aspectRatio`, and combine `clip` with `border` for framed avatars.
- **Tinting:** a tint color filter recolors vectors and icons, "useful for icons and vectors that need to be themed differently," instead of shipping colored copies.
- **Icons:** "By default, the `Icon` composable is tinted with `LocalContentColor.current` and is `24.dp` in size." The `material-icons` artifact "is no longer maintained or recommended for use in your apps"; use Material Symbols. "Good examples of `ImageVector`s are the Material Symbols."
- **Remote images:** "Avoid using `wrapContentSize` or leaving dimensions unconstrained on composables that load remote images," so the loader can downsample. "If your visual element on screen is purely for visual decoration, set your `contentDescription` to `null` for the screen reader to ignore it."

### Text and input

- **Fonts:** "Variable fonts are only supported on Android O and above," so keep a static fallback family below API 26. "Variable fonts are not currently supported via downloadable fonts": bundle the variable `.ttf` in `res/font`. Extract each style's axis settings "into a set of constants for easier reuse," and guard custom axes: "Define guardrails for the values they can accept." Downloadable Google Fonts need a fallback chain, and "Google Fonts takes several months to make new fonts available on Android."
- **Display size:** "you should use `displayLarge` for short, critical text, as it's the largest text on the screen."
- **Line height:** "Starting from Compose BOM version `2024.01.01`, `includeFontPadding` is set to `false` by default, which brings the default text layout more in line with common design tools." Set line height in `em` rather than `sp` "for improved precision," so a 1.2x ratio stays 1.2x at any text scale. `LineHeightStyle` trim needs font padding off.
- **Line breaking:** `LineBreak.Heading` ("Recommended for short text, such as titles"), `LineBreak.Paragraph` ("Recommended for larger amounts of text, such as paragraphs"), and `LineBreak.Simple` for text input. The balanced strategy is "Recommended for small screens, like watches, to maximize the amount of text displayed." "By default, hyphenation is not enabled"; `Hyphens.Auto` turns it on.
- **Alignment:** "prefer using `TextAlign.Start` and `TextAlign.End` instead of `TextAlign.Left` and `TextAlign.Right`," so RTL resolves.
- **Links and selection:** tappable text uses `LinkAnnotation`, because "you can’t add a modifier to a portion of a `Text` composable." "By default, composables aren’t selectable": wrap copyable text (codes, addresses, IDs) in `SelectionContainer`.
- **Overflow:** "The marquee effect occurs if the content is too wide to fit in the available constraints" (`Modifier.basicMarquee`).
- **Text fields:** "We recommend `TextField` over `BasicTextField` when your design calls for a Material `TextField` or `OutlinedTextField`." "Starting in Compose 1.13, value-based `BasicTextField`, Material 2 `TextField`, and `VisualTransformation` are deprecated in favor of `TextFieldState`-based text fields with `InputTransformation` and `OutputTransformation`."
- **Passwords:** "We recommend using `SecureTextField` for creating password text fields, as it hides character input by default and disables cut and copy actions."
- **Field height:** a single-line field keeps its height and scrolls sideways; a multi-line field grows: "If the text requires more lines, the field grows until it is `maxHeightInLines` tall, and scrolls vertically."
- **Formatting:** `InputTransformation` filters what is stored (max length, digits only); `OutputTransformation` formats what is shown, such as a phone number: "Unlike `InputTransformation`, the formatting done through the `OutputTransformation` is not saved in the `TextFieldState`."
- **Keyboard:** set capitalization, autocorrect, keyboard type, and IME action per field. "selecting `ImeAction.Next` or `ImeAction.Previous` as the action type will, by default, shift focus to the subsequent or preceding input field," so non-final fields take Next and the last field Done.
- **Expressive fields:** filled fields use `TextFieldDefaults.roundedShape` with `tonalColors()`; outlined fields add `labelPosition = TextFieldLabelPosition.Inside()`: "In the expressive outlined variant, the label is displayed inside the container rather than in a cutout on the upper border."
- **Autofill:** "To indicate that a `TextField` is Autofill-enabled, set the `ContentType` semantics with the types that the field can accept," combining types where a field accepts either ("contentType = ContentType.Username + ContentType.EmailAddress"). Sign-up and reset forms use the new-credential types, so "users may see a button in the keyboard to _Suggest strong password_."
- **Emoji:** Compose text gets current emoji automatically, but "Android versions 11 (API level 30) and lower can't update the emoji font."
