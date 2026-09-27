# Source coverage

A page-by-page ledger of every official source behind the android-design skill and whether its guidance reached the research docs ([material-3.md](material-3.md), [material-3-components.md](material-3-components.md), [material-3-wear-os.md](material-3-wear-os.md), [android-ui-design-guides.md](android-ui-design-guides.md)). Built September 2026 from the full route lists of m3.material.io (site manifest) and developer.android.com/design/ui (site navigation), plus the six official videos and two design.google articles. Each batch was audited by reading the scraped page and checking the research docs for its substance, not just its topic.

Legend: `[x]` distilled, `[~]` partly distilled (missing points listed), `[ ]` not distilled, `[-]` skipped with a reason. Each batch ends with its "Gaps worth distilling," ranked by impact on agent-built UI. Update a row when a gap is filled.

History: the first audit found 266 distilled, 133 partial, and 13 missing routes. A gap-fill pass in September 2026 closed all of them. Each batch's intro paragraph and "Gaps worth distilling" list describe the audit before that pass; the rows and the summary table show the current status.

## Summary

| Batch | Distilled | Partial | Missing | Skipped |
| --- | --- | --- | --- | --- |
| m3.material.io: Foundations (excluding Layout) | 31 | 0 | 0 | 16 |
| m3.material.io: Foundations > Layout | 19 | 0 | 0 | 2 |
| m3.material.io: Styles, Develop, and articles | 39 | 0 | 0 | 11 |
| m3.material.io: Components (A to L) | 69 | 0 | 0 | 0 |
| m3.material.io: Components (L to T) | 76 | 0 | 0 | 0 |
| m3.material.io blog, videos, and articles | 22 | 0 | 0 | 98 |
| developer.android.com/design/ui: Mobile, desktop, large screens, widgets | 39 | 0 | 0 | 9 |
| developer.android.com/design/ui: Gallery | 33 | 0 | 0 | 8 |
| developer.android.com/design/ui: Wear OS (current guides) | 46 | 0 | 0 | 4 |
| developer.android.com/design/ui: Wear OS (components and M2.5 guides) | 38 | 0 | 0 | 16 |
| Out of scope: developer.android.com/design/ui platforms | 0 | 0 | 0 | 144 |
| **Total** | **412** | **0** | **0** | **308** |

## m3.material.io: Foundations (excluding Layout)

### Foundations
- [-] /foundations; landing page of section links, no guidance

### Building for all
- [-] /foundations/building-for-all/co-design; research-process advice (involve overlooked communities, ML bias questions), no UI decision
- [-] /foundations/building-for-all/user-needs; list of experience dimensions for research and testing, no UI decision

### Content design
- [x] /foundations/content-design/overview → material-3.md (Content design: Style rules)
- [x] /foundations/content-design/alt-text → material-3.md (Accessibility: Images, illustrations, and alt text)
- [x] /foundations/content-design/global-writing/overview → material-3.md (Content design: global writing)
- [x] /foundations/content-design/global-writing/word-choice → material-3.md (Content design: Writing for translation)
- [x] /foundations/content-design/notifications → material-3.md (Content design: Notification copy); SMS limits skipped (not UI)
- [x] /foundations/content-design/style-guide/grammar-and-punctuation → material-3.md (Content design: Style rules); hyphenation table skipped (defers to AP)
- [x] /foundations/content-design/style-guide/ux-writing-best-practices → material-3.md (Content design: Style rules)
- [x] /foundations/content-design/style-guide/word-choice → material-3.md (Content design: Style rules)

### Customization
- [x] /foundations/customization → material-3.md (Color: Choosing a scheme; Color schemes from a seed), android-ui-design-guides.md (Color and theming: static fallback)

### Design tokens
- [x] /foundations/design-tokens/overview → material-3.md (Tokens and theming model)
- [-] /foundations/design-tokens/how-to-use-tokens; Figma plugin and DSP download steps, tooling procedure only

### Designing (accessibility)
- [x] /foundations/designing/overview → material-3.md (Accessibility: native components; custom dialogs need extra testing)
- [x] /foundations/designing/color-contrast → material-3.md (Accessibility: 4.5:1, 3:1, clustered vs standalone, disabled exempt)
- [x] /foundations/designing/elements → material-3.md (Accessibility: Labels and roles)
- [x] /foundations/designing/flow → material-3.md (Accessibility: Focus flow and shortcuts)
- [x] /foundations/designing/structure → material-3.md (Accessibility: Structure and reading order); web landmarks skipped (web only)

### Glossary
- [-] /foundations/glossary; A to Z definitions, no guidance

### Interaction
- [x] /foundations/interaction/gestures → material-3.md (Interaction: Gestures, predictive back components)
- [x] /foundations/interaction/inputs → material-3.md (Interaction: Pointer, trackpad, stylus, and keyboard)
- [x] /foundations/interaction/selection → material-3.md (Interaction: Selection mode)
- [x] /foundations/interaction/states/overview → material-3.md (Interaction: Applying states)
- [x] /foundations/interaction/states/state-layers → material-3.md (Interaction: States)
- [x] /foundations/interaction/states/applying-states → material-3.md (Interaction: Applying states)

### Accessibility overview
- [x] /foundations/overview/assistive-technology → material-3.md (Accessibility: Assistive technology)
- [-] /foundations/overview/principles; process principles (honor individuals, learn before, requirements as a starting point); the concrete corollary (respect system settings) is in android-ui-design-guides.md

### Usability
- [x] /foundations/usability/overview → material-3.md (Emphasis and hierarchy)
- [x] /foundations/usability/applying-m3-expressive → material-3.md (Worked example: Aura breathing app)

### Watches
- [x] /foundations/watches/foundations → material-3-wear-os.md (Watch design principles: Better together)
- [x] /foundations/watches/layout → material-3-wear-os.md (Layout; Adaptive sizes)
- [x] /foundations/watches/overview → material-3-wear-os.md (What Expressive means on a watch)
- [x] /foundations/watches/styles → material-3-wear-os.md (Color; Typography; Always on and haptics)

### Writing and text
- [x] /foundations/writing/best-practices → material-3.md (Accessibility: Images, illustrations, and alt text)
- [x] /foundations/writing/text-resizing → material-3.md (Typography: Text resizing and truncation)
- [x] /foundations/writing/text-truncation → material-3.md (Typography: Text resizing and truncation)

### XR (out of scope by owner decision)
- [-] /foundations/xr/components/app-bars; XR out of scope
- [-] /foundations/xr/components/dialogs; XR out of scope
- [-] /foundations/xr/components/nav-bar; XR out of scope
- [-] /foundations/xr/components/nav-rail; XR out of scope
- [-] /foundations/xr/components/overview; XR out of scope
- [-] /foundations/xr/components/toolbars; XR out of scope
- [-] /foundations/xr/design/accessibility; XR out of scope
- [-] /foundations/xr/design/interaction; XR out of scope
- [-] /foundations/xr/design/layout; XR out of scope
- [-] /foundations/xr/design/overview; XR out of scope

### Gaps worth distilling
1. /foundations/interaction/states/applying-states: "The individual components that are actionable within the app bar inherit hover states, not the whole app bar"; the same holds for focus, pressed, and dragged on dialogs, sheets, menus, and nav bars. "Disabled components can't be focused, dragged, or pressed." Pressed and dragged can "inherit elevation." Why: stops agents putting `clickable` or hover on whole containers and settles state behavior for custom Compose components.
2. /foundations/writing/text-truncation: "Information should always be available to readers, even if text is truncated or wrapped." "If there's an ellipsis, but no way to show the truncated text, it is not accessible." Why: agents reach for `maxLines = 1, overflow = Ellipsis` everywhere; this sets when to wrap and when an ellipsis needs a way to see the full text.
3. /foundations/writing/text-resizing: "When designing for text resizing, don't resize components without text"; scroll vertically only; in top app bar, nav bar, rail, and fixed tabs, keep text at 1x and "display enlarged content" in a touch-and-hold tooltip. Why: covers what happens at 200% font scale in constrained bars on phone and tablet.
4. /foundations/interaction/inputs: hand cursor on links, "I-beam when hovering on text", resize arrows on resizable edges; mouse text selection uses "a single color" and no touch handles; "When a physical keyboard is attached, hide the virtual keyboard"; Escape "should remove the text cursor... but should not remove already-typed text"; horizontal wheel scroll on carousels. Why: sets desktop and tablet pointer behavior (`pointerHoverIcon`, selection) that the desktop notes only hint at.
5. /foundations/designing/elements: labels are needed for "Generic links (for example, 'Learn more')" and "Buttons with generic text (for example, 'Save' when there are multiple such buttons)", status icons, progress and error cues; not needed for "Buttons with sufficient text"; "assign roles based on your design system components." Why: tells an agent exactly where `contentDescription` and `semantics { role }` go and where they are noise.
6. /foundations/designing/flow: "group a collection of interactive elements as one tab stop, and use arrow keys to traverse sub-elements"; single-key shortcuts must be remappable or "Activate the shortcut only when a relevant component is focused"; list all custom shortcuts. Why: settles keyboard focus structure for tablet, desktop, and ChromeOS Compose layouts.
7. /foundations/designing/structure: "Place important actions at the top or bottom of the screen"; "Place related items of a similar hierarchy next to each other"; screen reader order must follow the visual hierarchy; headings are set by content hierarchy, not styling. Why: sets Compose traversal order and `heading()` semantics, and shapes where primary actions sit.
8. /foundations/interaction/selection: exit selection mode by deselecting or "tap an action on the toolbar"; don't reuse long press plus drag for batch select where it already means pick up and move; on desktop "checkboxes are always visible when selection is the primary activity," otherwise on hover and for all items once one is selected. Why: multi-select lists are common, and agents get the entry, exit, and gesture conflicts wrong.
9. /foundations/content-design/alt-text: 140-char limit; "Don't start alt text with 'image of'"; "Don't repeat the caption in alt text"; describe meaning in context, not detail; chart formula "Summary of [data type] + [reason for showing the chart]". Why: agents write poor `contentDescription` strings, especially for charts and product images.
10. /foundations/writing/best-practices: decorative parts of an illustration "do not have to meet Material's contrast requirements" while essential text and graphics do; "If there is essential information embedded as text in the image, include the essential information in the alt text." Why: clarifies which contrast rules apply to illustrations and empty-state art.
11. /foundations/content-design/global-writing/word-choice: "Other languages average at 1.5 times longer than English"; repeat nouns instead of "it"; don't start with "this"/"that"; use global examples. Why: sets how much room to leave for translated strings in buttons and chips, and makes UI copy easier to localize.
12. /foundations/content-design/notifications: expanded body under 80 chars; "Place dynamic text in the notification body"; "Don't add negative emotions to a message. Don't replace words with emoji."; offer opt-out in context. Why: fills in the notification copy rules agents write for Android notifications.
13. /foundations/content-design/style-guide (grammar, UX writing): no Latin abbreviations ("for example," not "e.g."); "23M" for approximate volume and commas from 1,000 to 1M; no colons in list headings; bold, not italics, for emphasis; "&" only where space is tight. Why: small, mechanical copy rules that show up in every generated screen.
14. /foundations/interaction/states/overview: "States have two visual indicators to ensure accessibility"; states combine (selected plus hover or focus). Why: custom selectable components need a second cue besides color.
15. /foundations/overview/assistive-technology: design for Switch Access ("Switches scan the items on your screen, highlighting each item in turn") and D-pad linear traversal alongside TalkBack. Why: a reminder that focus order and grouping serve more than screen readers. Low impact on its own.

## m3.material.io: Foundations > Layout

Status is judged against the four research docs only. Most layout coverage is in `material-3.md` (Layout: Breakpoints, Scaffold, Spacing; Containment and spacing (Expressive)), with a little in `android-ui-design-guides.md` (Layout) and some RTL rules for individual components in `material-3-components.md`.

On the skill side, `skills/android-design/references/layout.md` covers roughly the same ground as `material-3.md` Layout. It holds three points the research docs lack, so they reached the skill without a research source:
- an empty state in the detail pane when nothing is selected;
- a bottom sheet for the supporting pane on compact;
- "2 (3 with a side sheet)" for extra-large.

Neither the skill nor the research has rulers, pane drag handles or resize persistence, pane focus rules, RTL mirroring exceptions, or the component adaptation strategies.

### Bidirectionality & RTL
- [x] /foundations/layout/bidirectionality-rtl → material-3.md (Layout > Bidirectionality (RTL))

### Breakpoints
- [x] /foundations/layout/breakpoints/overview → material-3.md (Layout > Breakpoint-specific rules)
- [x] /foundations/layout/breakpoints/compact → material-3.md (Layout > Breakpoints table)
- [x] /foundations/layout/breakpoints/medium → material-3.md (Layout > Breakpoint-specific rules)
- [x] /foundations/layout/breakpoints/expanded → material-3.md (Layout > Breakpoint-specific rules)
- [x] /foundations/layout/breakpoints/large-extra-large → material-3.md (Layout > Breakpoint-specific rules; Panes in depth)

### Canonical layout examples
- [x] /foundations/layout/canonical-examples/overview → material-3.md (Layout > Canonical layouts: behavior rules)
- [x] /foundations/layout/canonical-examples/feed → material-3.md (Layout > Canonical layouts: behavior rules)
- [x] /foundations/layout/canonical-examples/list-detail → material-3.md (Layout > Canonical layouts: behavior rules; Breakpoint-specific rules)
- [x] /foundations/layout/canonical-examples/supporting-pane → material-3.md (Layout > Canonical layouts: behavior rules)

### Grids & spacing
- [-] /foundations/layout/grids-spacing/overview; summary bullets and availability table only, and its substance is on the subpages below
- [x] /foundations/layout/grids-spacing/grids → material-3.md (Layout > Grids and rulers)
- [x] /foundations/layout/grids-spacing/spacing → material-3.md (Layout > Spacing and density details)
- [x] /foundations/layout/grids-spacing/density → material-3.md (Layout > Spacing and density details); pixel density and dp formulas left out (low value)

### Layout overview
- [-] /foundations/layout/layout-overview/overview; glossary and changelog, and the terms that matter are covered where they are used (Scaffold, Breakpoints)
- [x] /foundations/layout/layout-overview/parts-of-layout → material-3.md (Layout > Panes in depth; Grids and rulers; Adapting components)
- [x] /foundations/layout/layout-overview/adaptive-design → material-3.md (Layout > Adapting components; Panes in depth)

### Scaffold
- [x] /foundations/layout/scaffold/overview → material-3.md (Layout > Scaffold)
- [x] /foundations/layout/scaffold/bars → material-3.md (Layout > Scaffold); android-ui-design-guides.md (app bars attach to their pane)
- [x] /foundations/layout/scaffold/rails → material-3.md (Layout > Scaffold) (the XR orbiter bit is out of scope)
- [x] /foundations/layout/scaffold/panes → material-3.md (Layout > Panes in depth); XR spatial panels left out (out of scope)
  - permanent vs temporary panes;
  - drag handles: collapse or expand the fixed pane, toggle on tap, double tap or long press, snap to 360dp, 412dp or a centered split;
  - persistent resizing remembers width across restarts and breakpoint changes; temporary resizing (supporting pane) resets on reopen;
  - placement: "persistent utilities like tool panels should be co-planar"; "Temporary tasks should remain floating regardless of breakpoint";
  - floating panes are the default on large screens with an optional scrim; docked panes become floating or co-planar at medium and expanded;
  - pane accessibility: focus order follows the visual pane order; a modal floating pane moves focus in and back to its trigger; a non-modal pane sits in reading order

### Gaps worth distilling
1. /foundations/layout/scaffold/panes: pane accessibility. "The focus order should match the visual arrangement of the panes on screen". A modal floating pane moves focus "to the first element in the pane, and when the pane is closed, focus moves back to the element that triggered it". A non-modal pane stays in logical reading order. Why: this directly shapes focus handling in Compose two-pane and floating-pane code on tablet, desktop and keyboard.
2. /foundations/layout/canonical-examples/list-detail: transition rules. Show placeholder content in the detail pane when nothing is selected. Going two-pane to one pane shows the detail with an app bar, unless multi-select. "Consistency is key: If a layout showed the list view previously, it should return to that view". Save read and unread state. Why: these are the fold, unfold and rotate state bugs agents commonly ship in list-detail scenes.
3. /foundations/layout/scaffold/panes: placement. "Temporary tasks should remain floating regardless of breakpoint"; "persistent utilities like tool panels should be co-planar"; floating panes are the default on large screens with an optional scrim; docked panes (bottom sheets) become floating or co-planar at medium and expanded. Why: this decides whether a sheet stays a sheet, floats, or becomes a side pane on large windows.
4. /foundations/layout/layout-overview/adaptive-design: component adaptation. Buttons "scale along with their parent container, or hug their contents"; list items "reveal descriptions or other additional information as their parent container scales"; FAB becomes extended FAB and rails expand automatically. Why: it gives agents a rule for adapting components themselves, not just panes.
5. /foundations/layout/bidirectionality-rtl: mirroring exceptions. Mirror directional icons (back, forward, send), but clocks, clockwise refresh icons and circular progress don't mirror; "Media controls for video or audio players are always LTR"; Hebrew keeps timelines, media controls and linear progress LTR; swipe actions and predictive back mirror. Why: Compose auto-mirrors layout, so the errors come from icons and custom drawing, and those are what this page covers.
6. /foundations/layout/scaffold/panes: resizing. Drag handles snap to "360dp, 412dp, Split-pane with spacer centered visually". Persistent resizing "remembers a person's pane width preference. Use this for most resizable layouts" and survives breakpoint changes. Temporary resizing (supporting pane) resets. Why: this sets the expected behavior when building resizable panes on tablet and desktop.
7. /foundations/layout/breakpoints/expanded: "For sorting, filtering, or secondary navigation, use tabs or other components directly in the pane"; "The navigation rail can be hidden in secondary destinations as long as the primary destination can still be accessed using a back button". Why: it stops agents putting secondary navigation in the rail and tells them when the rail may disappear.
8. /foundations/layout/canonical-examples/supporting-pane: "Use the supporting pane layout when the secondary content is only meaningful in relation to the primary content. For content with a parent-child relationship, use a list-detail layout instead." Also, a bottom sheet holds supporting content on compact. Why: this is the rule for choosing between the two canonical layouts.
9. /foundations/layout/grids-spacing/grids: rulers. Bar and safety rulers, a title ruler aligning app bar content, first and secondary content rulers, adjustable margin rulers ("a photo grid can take the full width of the screen, while components like search use wider margins"). Compose Rulers API. Why: consistent alignment across screens and deliberate full-bleed media are currently unaddressed.
10. /foundations/layout/grids-spacing/spacing: "Leading elements like thumbnails, avatars, or icons should always be aligned"; thumbnails use identical sizes whatever the source ratio; buttons "close to the content they're affecting"; cards keep consistent horizontal spacing when heights vary. Why: these are concrete list and card polish rules an agent can apply directly.
11. /foundations/layout/grids-spacing/density: people "opt in to dense layouts"; settings for density must keep 48x48 targets so the change is reversible; "Text size shouldn't change as the container size scales"; "Don't scale layouts below 48x48dp by default". Why: it keeps agents from shrinking text or targets when asked for a "compact" UI.
12. /foundations/layout/breakpoints/large-extra-large: an expanded rail suits extra-large; "Consider collapsing the navigation rail when space is needed, or when on pages deeper in the page hierarchy"; a side sheet as third pane (max 400dp); "Don't use more than three panes"; some products don't need large or extra-large. Why: it governs desktop window layouts.
13. /foundations/layout/breakpoints/overview: use single-pane immersive layouts for "Playing a game, Watching a movie, Video calls, Creative applications", even at expanded. "Don't swap a button for a chip. Be careful when changing between list items and cards." Why: it stops agents forcing two panes onto media and focus apps.
14. /foundations/layout/canonical-examples/feed: feed items reflow on rotate, unfold and multi-window; "Use size and position to establish relationships among content elements"; "The order of items is determined by their position". Why: it guides adaptive grid hierarchy instead of uniform columns.
15. /foundations/layout/breakpoints/medium: "Avoid placing essential interactive elements too close to the bottom edge of the screen", plus the three ergonomic regions (inconvenient top, comfortable middle, challenging bottom) for tablets and unfolded foldables. Why: it refines control placement beyond the top-25% rule already captured.

## m3.material.io: Styles, Develop, and articles

The Styles pages are mostly distilled. The biggest gap is the transition-pattern selection rules on `/styles/motion/transitions/applying-transitions`. After that come the easing and duration defaults and a possible error in the doc's easing table (see "Correctness flag" at the end).

### Landing and index pages
- [-] /; not scraped (homepage, no guidance)
- [-] /components; index page of one-line component descriptions, no guidance
- [-] /foundations; index page of one-line section descriptions
- [-] /styles; index page of one-line section descriptions
- [-] /whats-new-at-io26; announcement index. Its substance (layout scaffold, 8dp spacing system, expressive lists and menus, contained search, Compose-first) lives on linked pages audited in other batches or below

### Expressive articles
- [x] /building-with-m3-expressive → material-3.md (What it is and why; The seven expressive tactics; Hero moments)
- [x] /m3-expressive-motion-theming → material-3.md (Motion: schemes, spatial vs effects, speeds, springs vs tween, custom `MotionScheme`, custom components using `MaterialTheme.motionScheme`)
- [x] /material-is-compose-first → material-3.md (Versions and stability)

### Develop
- [-] /develop; platform landing page. Only guidance: Compose is recommended, Views and Web are in maintenance mode
- [-] /develop/android/jetpack-compose; link hub (announcements, docs, samples)
- [-] /develop/android/mdc-android; Views link hub, outside Compose scope. Its maintenance-mode status is not recorded anywhere (see gap 10)
- [-] /develop/flutter; out-of-scope platform (no Expressive on Flutter)
- [-] /develop/web; out-of-scope platform (maintenance mode, no Expressive)

### Styles: Color
- [x] /styles/color/system/overview → material-3.md (Color: How the system works; three contrast levels; Aug 2024 on*Container change)
- [x] /styles/color/system/how-the-system-works → material-3.md (Color: How the system works; HCT; contrast levels; custom components via roles)
- [x] /styles/color/roles → material-3.md (Color: Roles table and Rules)
- [x] /styles/color/choosing-a-scheme → material-3.md (Color: Choosing a scheme)
- [x] /styles/color/dynamic/choosing-a-source → material-3.md (Choosing a scheme table; combine schemes)
- [x] /styles/color/dynamic/user-generated-source → material-3.md (Choosing a scheme); the rest of the page is Figma plugin procedure
- [x] /styles/color/dynamic/content-based-source → material-3.md (Choosing a scheme; combine schemes); the rest of the page is Figma procedure
- [x] /styles/color/static/baseline → material-3.md (Choosing a scheme); the rest of the page is Figma procedure
- [x] /styles/color/static/custom-brand → material-3.md (Choosing a scheme; Color schemes from a seed: Theme Builder export)
- [x] /styles/color/advanced/overview → material-3.md (Choosing a scheme bullets: combine, static colors, custom baseline, fidelity, custom dynamic)
- [x] /styles/color/advanced/apply-colors → material-3.md (Color: Applying content color and remapping)
- [x] /styles/color/advanced/define-new-colors → material-3.md (Color: Custom color roles)
- [x] /styles/color/advanced/adjust-existing-colors → material-3.md (Color: Choosing a scheme, harmonize limit)
- [-] /styles/color/resources; links to Theme Builder and codelabs, no guidance

### Styles: Elevation
- [x] /styles/elevation/overview → material-3.md (Elevation)
- [x] /styles/elevation/applying-elevation → material-3.md (Elevation: tonal difference, shadows only to protect or invite interaction, scrim 32%, overlapping containers use different roles)
- [x] /styles/elevation/tokens → material-3.md (Elevation: resting levels per component, surface tint deprecated)

### Styles: Icons
- [x] /styles/icons/overview → material-3.md (Icons)
- [x] /styles/icons/designing-icons → material-3.md (Icons: Custom icons)
- [x] /styles/icons/applying-icons → material-3.md (Icons: Applying icons)

### Styles: Motion
- [x] /styles/motion/overview/how-it-works → material-3.md (Motion: schemes, springs, spatial and effects, speeds, three customization levels)
- [x] /styles/motion/overview/specs → material-3.md (Motion: web approximation table; spring token values)
- [x] /styles/motion/easing-and-duration/applying-easing-and-duration → material-3.md (Motion foundation: transitions; Easing and duration defaults)
- [x] /styles/motion/easing-and-duration/tokens-specs → material-3.md (Motion foundation: transitions; easing table and Easing and duration defaults)
- [x] /styles/motion/transitions/transition-patterns → material-3.md (Motion foundation: transitions; Pattern details)
- [x] /styles/motion/transitions/applying-transitions → material-3.md (Motion foundation: transitions; Choosing a transition pattern)

### Styles: Shape
- [x] /styles/shape/overview-principles → material-3.md (Shape (Expressive) principles)
- [x] /styles/shape/corner-radius-scale → material-3.md (Shape: scale, style vs component customization, cut family, optical roundness, inner corners, no large corners on dense cards)
- [x] /styles/shape/shape-morph → material-3.md (Shape (Expressive); Compose API Shapes; morph uses the Expressive scheme by default)

### Styles: Spacing
- [x] /styles/spacing/overview → material-3.md (Layout: Spacing: 8dp, padding and gaps over margins)
- [x] /styles/spacing/applying-spacing → material-3.md (Layout: Spacing)
- [x] /styles/spacing/tokens → material-3.md (Spacing: space100 = 8dp, nested 2/4/6/10dp; Compose API: no tokens exposed)

### Styles: Typography
- [x] /styles/typography/overview → material-3.md (Typography; Typography (Expressive); language height)
- [x] /styles/typography/fonts → material-3.md (Typography: variable fonts and fallback order)
- [x] /styles/typography/type-scale-tokens → material-3.md (Typography: Customizing type styles)
- [x] /styles/typography/applying-type → material-3.md (Typography: Customizing type styles, baseline typesetting)
- [x] /styles/typography/editorial-treatments → material-3.md (Editorial treatments)

### Gaps worth distilling
1. **/styles/motion/transitions/applying-transitions**, the "Choosing a transition pattern" rules. Container transform is for "Hero moments," "Shallow hierarchies," and "Don't use container transform in apps with deep hierarchies, the motion becomes excessive." "Both Android and iOS should use platform defaults for forward and backward navigation." "Don't use a lateral transition to move between top level destinations. The gesture conflicts with carousel and list item gestures." "Don't use an enter and exit pattern for navigating hierarchical screens." "Don't use forward and backward transitions on hero moments." Why it matters: these rules decide what an agent puts in Navigation 3 or `AnimatedContent` transitions on every screen change.
2. **/styles/motion/easing-and-duration/applying-easing-and-duration and tokens-specs**: the default pairs (Emphasized 500ms on-screen, Emphasized decelerate 400ms enter, Emphasized accelerate 200ms exit, Standard 300/250/200ms), exit-temporarily uses Emphasized, and the duration token ranges. Why it matters: transitions still use this system, and without values agents pick arbitrary `tween()` durations.
3. **/styles/motion/transitions/transition-patterns**, skeleton loaders: "a subtle pulsing animation... starts at the top left of the screen and moves down to the bottom right. Once content is loaded, it quickly fades in." Also bars slide off on scroll, and entry direction builds the spatial model. Why it matters: loading states and scroll behavior show up on almost every data screen.
4. **/styles/color/advanced/apply-colors**, content-color best practices: "each card is colored with a scheme sourced from its main image" to link related items; full-screen content-color moments for media controls; "Avoid applying content-based color in spaces where the content itself isn't visible." Why it matters: this is the concrete recipe behind Expressive media and feed screens.
5. **/styles/typography/type-scale-tokens**, customizing styles: "Avoid changing the type size; this can affect how components render and reflow"; tune line height and letter spacing instead; "Try to keep emphasized styles visually consistent." Why it matters: it stops agents from breaking component layouts when they add a brand font.
6. **/styles/spacing/applying-spacing**: new tokens follow the multiplier ("space225 = 18dp"); tokenize shared adaptive patterns ("surface content horizontal padding"); "Density: Adapt vertical padding"; map spacing to different tokens per device type. Why it matters: it gives the app spacing object from the doc a shape that adapts across phone, tablet and desktop.
7. **/styles/icons/applying-icons**: "A 20dp size symbol can use a target size of 40dp" when pointer and keyboard are primary; "navigation items must have labels"; some symbols "should remain filled, such as full body human icons." Why it matters: dense desktop layouts and nav labeling.
8. **/styles/color/advanced/define-new-colors**, custom color roles: assign a palette and reference tones for light and dark, specify pairings and tone deltas, confirm contrast, generate via MCU; "considered only if you cannot achieve your desired colors with other Material color solutions." Why it matters: it tells agents how to add a chart or graphic color without breaking contrast levels, and when not to.
9. **/styles/color/choosing-a-scheme**: static schemes give up "User-controlled contrast settings"; with dynamic color, "design it using the baseline color scheme" first, then test across sources. Why it matters: it frames the trade-off when an agent hardcodes a brand scheme.
10. **/material-is-compose-first**: "Material Views 1.14.0 (MDC-Android) will be our final stable release"; the Views library is in maintenance mode. Why it matters: it justifies steering any Views code to Compose.
11. **/styles/icons/designing-icons**: 24dp grid with a "20dp x 20dp live area, with 2dp of padding," 2dp corner radii, regular 400 stroke, "Make icons face forward," "Don't be overly literal." Why it matters: this applies only when an agent draws custom vector icons, which is rare.
12. **/styles/typography/applying-type**: "Android screens rely on distance to baselines for spacing." Why it matters: low. It affects only precise text alignment such as `paddingFromBaseline`.

**Correctness flag:** material-3.md's easing table lists Emphasized as `0.2, 0, 0, 1`, the same as Standard. The site defines Android Emphasized as `pathInterpolator(M 0,0 C 0.05, 0, 0.133333, 0.06, 0.166666, 0.4 C 0.208333, 0.82, 0.25, 1, 1, 1)`, and says web and iOS fall back to Standard. The doc's value may come from Compose's `EasingEmphasizedCubicBezier`, which I did not check. If so, the doc should say so.

## m3.material.io: Components (A to L)

Checked 69 URLs. 57 are fully distilled, 12 are partly distilled, and none are skipped or missing. Lists has the weakest coverage: `material-3.md` only has a short summary, not the full distillation that `material-3-components.md` says it contains. Doc names below are files in `docs/research/`.

### Actions: all buttons
- [x] /components/all-buttons → material-3-components.md (All buttons)

### Actions: buttons
- [x] /components/buttons/overview → material-3-components.md (Buttons; M3 Expressive changes)
- [x] /components/buttons/specs → material-3-components.md (Buttons: square and pressed radii, toggle color roles, elevated elevation 1/0)
- [x] /components/buttons/guidelines → material-3-components.md (Buttons: compact vs large filled-button alignment added)
- [x] /components/buttons/accessibility → material-3-components.md (Buttons, Accessibility)

### Actions: button groups
- [x] /components/button-groups/overview → material-3-components.md (Button groups)
- [x] /components/button-groups/specs → material-3-components.md (Button groups: padding 18/12/8dp and 2dp connected; only the inner corner sizes are left out)
- [x] /components/button-groups/guidelines → material-3-components.md (Button groups)
- [x] /components/button-groups/accessibility → material-3-components.md (Button groups, Accessibility)

### Actions: icon buttons
- [x] /components/icon-buttons/overview → material-3-components.md (Icon buttons)
- [x] /components/icon-buttons/specs → material-3-components.md (Icon buttons: square and pressed radii, toggle color roles)
- [x] /components/icon-buttons/guidelines → material-3-components.md (Icon buttons)
- [x] /components/icon-buttons/accessibility → material-3-components.md (Icon buttons, Accessibility)

### Actions: FAB, extended FAB, FAB menu
- [x] /components/floating-action-button/overview → material-3-components.md (FAB)
- [x] /components/floating-action-button/specs → material-3-components.md (FAB: state layer matches icon color)
- [x] /components/floating-action-button/guidelines → material-3-components.md (FAB)
- [x] /components/floating-action-button/accessibility → material-3-components.md (FAB, Accessibility)
- [x] /components/extended-fab/overview → material-3-components.md (Extended FAB)
- [x] /components/extended-fab/specs → material-3-components.md (FAB: same state layer rule)
- [x] /components/extended-fab/guidelines → material-3-components.md (Extended FAB)
- [x] /components/extended-fab/accessibility → material-3-components.md (Extended FAB, Accessibility)
- [x] /components/fab-menu/overview → material-3-components.md (FAB menu)
- [x] /components/fab-menu/specs → material-3-components.md (FAB menu: 56dp close button, medium button items, 16/24dp margins, 4dp web gap)
- [x] /components/fab-menu/guidelines → material-3-components.md (FAB menu)
- [x] /components/fab-menu/accessibility → material-3-components.md (FAB menu, Accessibility)

### Bars: app bars
- [x] /components/app-bars/overview → material-3-components.md (App bars: animates with a chip row)
- [x] /components/app-bars/specs → material-3-components.md (App bars: surface to surface container on scroll, anatomy customizations; measurements are images only)
- [x] /components/app-bars/guidelines → material-3-components.md (App bars: subtitle type, avoid resizing heading and subtitle)
- [x] /components/app-bars/accessibility → material-3-components.md (App bars, Accessibility)

### Communication: badges
- [x] /components/badges/overview → material-3-components.md (Badges)
- [x] /components/badges/specs → material-3-components.md (Badges: 6dp, 16dp, 16x34dp, error and on error)
- [x] /components/badges/guidelines → material-3-components.md (Badges)
- [x] /components/badges/accessibility → material-3-components.md (Badges, Accessibility)

### Containment: bottom sheets, cards, carousel, dialogs, divider
- [x] /components/bottom-sheets/overview → material-3-components.md (Bottom sheets)
- [x] /components/bottom-sheets/specs → material-3-components.md (Bottom sheets: 640dp, 72dp top margin, 22dp drag handle padding, 56dp margins above 640dp)
- [x] /components/bottom-sheets/guidelines → material-3-components.md (Bottom sheets)
- [x] /components/bottom-sheets/accessibility → material-3-components.md (Bottom sheets, Accessibility)
- [x] /components/cards/overview → material-3-components.md (Cards)
- [x] /components/cards/specs → material-3-components.md (Cards: 12dp corners, 16dp padding, 8dp max gap, color roles)
- [x] /components/cards/guidelines → material-3-components.md (Cards)
- [x] /components/cards/accessibility → material-3-components.md (Cards, Accessibility)
- [x] /components/carousel/overview → material-3-components.md (Carousel, including the research finding of about 10 items)
- [x] /components/carousel/specs → material-3-components.md (Carousel: 16dp padding, 8dp gaps, 28dp corners, 40 to 56dp small items)
- [x] /components/carousel/guidelines → material-3-components.md (Carousel: brief labels on smaller items, center-aligned hero preview, narrow max width)
- [x] /components/carousel/accessibility → material-3-components.md (Carousel, Accessibility)
- [x] /components/dialogs/overview → material-3-components.md (Dialogs)
- [x] /components/dialogs/specs → material-3-components.md (Dialogs: 280 to 560dp, 28dp corners, 24dp padding)
- [x] /components/dialogs/guidelines → material-3-components.md (Dialogs: discard confirmation, specific confirm labels, don't disable full-screen confirm, show all errors, inline expansion)
- [x] /components/dialogs/accessibility → material-3-components.md (Dialogs, Accessibility)
- [x] /components/divider/overview → material-3-components.md (Divider)
- [x] /components/divider/specs → material-3-components.md (Divider: outline variant, 16dp inset)
- [x] /components/divider/guidelines → material-3-components.md (Divider)
- [x] /components/divider/accessibility → material-3-components.md (Divider, Accessibility)

### Selection: checkbox, chips
- [x] /components/checkbox/overview → material-3-components.md (Checkbox)
- [x] /components/checkbox/specs → material-3-components.md (Checkbox: 18dp box, 48dp target, on surface label)
- [x] /components/checkbox/guidelines → material-3-components.md (Checkbox)
- [x] /components/checkbox/accessibility → material-3-components.md (Checkbox, Accessibility)
- [x] /components/chips/overview → material-3-components.md (Chips)
- [x] /components/chips/specs → material-3-components.md (Chips: 32dp, 8dp corners, 88dp min width for a trailing action)
- [x] /components/chips/guidelines → material-3-components.md (Chips)
- [x] /components/chips/accessibility → material-3-components.md (Chips, Accessibility)

### Pickers: date pickers
- [x] /components/date-pickers/overview → material-3-components.md (Date pickers)
- [x] /components/date-pickers/specs → material-3-components.md (Date pickers: color roles; measurements are images only)
- [x] /components/date-pickers/guidelines → material-3-components.md (Date pickers)
- [x] /components/date-pickers/accessibility → material-3-components.md (Date pickers, Accessibility)

### Lists
- [x] /components/lists/overview → material-3-components.md (Lists, M3 Expressive changes)
- [x] /components/lists/specs → material-3-components.md (Lists: 56/72/88dp heights, top alignment, slot rules, one selection interaction, shape morph); still omitted: expressive measurement diagram (images only)
- [x] /components/lists/guidelines → material-3-components.md (Lists: selection modes, 1 to 3 line supporting text, compact edge-to-edge, list-detail, center-visual caution, full swipe)
- [x] /components/lists/accessibility → material-3-components.md (Lists, Accessibility)

### Gaps worth distilling
1. /components/lists/guidelines: the selection-mode rules. "Single-select list items: Don't support multi-actions; Can't have secondary nested actions; Shouldn't use checkboxes"; "Single-action list items: Can't have secondary nested actions; Can't be toggled into a persistent selected state"; multi-action: "The primary action should take up the majority of the space... Place supplementary actions... in the trailing position." Why: `ListItem` is one of the most generated composables, and mixing modes is the most common structural mistake.
2. /components/lists/accessibility: "On Jetpack Compose, the role applies to the list item as a whole" (Radio button or Checkbox, Checked or Not-checked). Focus goes to the selected item if there is one. Why: this decides whether `Modifier.selectable` or `toggleable` goes on the row or on the control.
3. /components/lists/guidelines (adaptive): "Lists should extend edge-to-edge in compact windows. Selecting a list item should open a page with the details." Medium and expanded show list and detail side by side. "Limit supporting text to one to three lines." Why: this sets phone and tablet list layout and truncation.
4. /components/dialogs/guidelines: "When someone dismisses a full-screen dialog, a basic dialog should appear to confirm that they want to discard the unsaved changes." The confirmation action "should be clear about what happens next, like Send or Create. Avoid using vague terms like Done, OK, or Close." Also "Don't disable the confirmation button." Why: this shapes every edit or create flow and its button labels.
5. /components/lists/specs: "The tallest element within a list item determines the list item's height: either 56dp, 72dp, or 88dp." Elements are top-aligned when an item is 88dp+ or has 3+ lines of text. Slots: "Use only one selection interaction per list item." Why: custom rows and slot content drift without these rules.
6. /components/buttons/specs and /components/icon-buttons/specs: square corner radii (XS 12, S 12, M 16, L 28, XL 28dp) and pressed radii (8, 8, 12, 16, 16dp). Toggle color roles: unselected filled is surface container with on surface variant, and selected outlined is inverse surface. Why: needed when styling custom toggles or shapes outside the Compose defaults.
7. /components/dialogs/guidelines: "Show all errors on the page at once so people can fix everything before trying again." Instead of a third "Learn more" action, "an inline expansion can display more information." Why: this affects form validation inside dialogs and the action count.
8. /components/carousel/guidelines: "Consider adapting the text to use brief labels on smaller carousel items" (the medium item hides the title, the small item abbreviates the label). Why: without it, generated carousels show clipped text on shrinking items.
9. /components/date-pickers/accessibility: "The helper text... should specify the date format (for example, MM/DD/YYYY)". The label states the purpose ("event date") and matches the placeholder. Why: this affects every date text field an agent builds.
10. /components/buttons/guidelines (adaptive): filled buttons are "end-aligned below flight information in a compact window" and "start-aligned beside flight information in a large window." Why: this gives a concrete rule for moving buttons across breakpoints.
11. /components/floating-action-button/specs and /components/extended-fab/specs: "When using a non-default color mapping... make sure the state layer color is the same as the icon color." Why: this prevents wrong ripple colors on custom-colored FABs.
12. /components/app-bars/guidelines: subtitle type is small Label medium, medium flexible Label large, large flexible Title medium, and "Avoid customizing the size of the heading and subtitle." Why: agents often restyle app bar titles by hand.
13. /components/app-bars/overview: app bars "Can animate on and off screen with another bar of controls, like a row of chips." Why: this is the pattern for a chip filter row attached to the app bar.
14. /components/lists/guidelines: "Avoid placing visuals in the center of a row because it makes the list difficult to scan." Full swipe "trigger[s] this action, clearing the list item". Why: this sets row composition and swipe-to-dismiss behavior.
15. /components/bottom-sheets/specs: 72dp top margin in compact windows, and 22dp drag handle padding. Why: minor, but it fixes the sheet's maximum height on phones.

## m3.material.io: Components (L to T)

76 URLs audited: 68 fully distilled, 4 partly distilled, 2 not distilled. All the gaps are in menus and search, which `material-3-components.md` defers to `material-3.md`. There they get only a short bullet under "Components: what Expressive replaced" plus one row in the component selection table, not a full distillation. The line in the header of `material-3-components.md` claiming "the full distillation of lists, menus, and search" is inaccurate for menus and search.

### Loading indicator
- [x] /components/loading-indicator/overview → material-3-components.md (Loading indicator, M3 Expressive changes)
- [x] /components/loading-indicator/specs → material-3-components.md (Loading indicator: default and contained colors, 48dp default size)
- [x] /components/loading-indicator/guidelines → material-3-components.md (Loading indicator: pull-to-refresh placement)
- [x] /components/loading-indicator/accessibility → material-3-components.md (Loading indicator, Accessibility)

### Menus
- [x] /components/menus/overview → material-3-components.md (Menus)
- [x] /components/menus/specs → material-3-components.md (Menus: standard and vibrant color roles, submenu shape morph, baseline 112 to 280dp)
- [x] /components/menus/guidelines → material-3-components.md (Menus)
- [x] /components/menus/accessibility → material-3-components.md (Menus, Accessibility)

### Navigation bar
- [x] /components/navigation-bar/overview → material-3-components.md (Navigation bar)
- [x] /components/navigation-bar/specs → material-3-components.md (Navigation bar: color roles, vertical items stretch and horizontal items have fixed width)
- [x] /components/navigation-bar/guidelines → material-3-components.md (Navigation bar)
- [x] /components/navigation-bar/accessibility → material-3-components.md (Navigation bar, Accessibility)

### Navigation drawer
- [x] /components/navigation-drawer/overview → material-3-components.md (Navigation drawer: no longer recommended)
- [x] /components/navigation-drawer/specs → material-3-components.md (Navigation drawer: surface container low, 360dp width)
- [x] /components/navigation-drawer/guidelines → material-3-components.md (Navigation drawer)
- [x] /components/navigation-drawer/accessibility → material-3-components.md (Navigation drawer, Accessibility)

### Navigation rail
- [x] /components/navigation-rail/overview → material-3-components.md (Navigation rail)
- [x] /components/navigation-rail/specs → material-3-components.md (Navigation rail: colors, target spans the full width)
- [x] /components/navigation-rail/guidelines → material-3-components.md (Navigation rail)
- [x] /components/navigation-rail/accessibility → material-3-components.md (Navigation rail, Accessibility)

### Progress indicators
- [x] /components/progress-indicators/overview → material-3-components.md (Progress indicators, M3 Expressive changes)
- [x] /components/progress-indicators/specs → material-3-components.md (Progress indicators: 4dp track, 4dp stop indicator, 4dp end padding, colors)
- [x] /components/progress-indicators/guidelines → material-3-components.md (Progress indicators)
- [x] /components/progress-indicators/accessibility → material-3-components.md (Progress indicators, Accessibility)

### Radio button
- [x] /components/radio-button/overview → material-3-components.md (Radio button)
- [x] /components/radio-button/specs → material-3-components.md (Radio button: 20dp icon, 48dp target, colors)
- [x] /components/radio-button/guidelines → material-3-components.md (Radio button)
- [x] /components/radio-button/accessibility → material-3-components.md (Radio button, Accessibility)

### Search
- [x] /components/search/overview → material-3-components.md (Search)
- [x] /components/search/specs → material-3-components.md (Search: 360 to 720dp, 56dp, docked 240dp to 2/3 height, color roles, 24/12dp margins)
- [x] /components/search/guidelines → material-3-components.md (Search)
- [x] /components/search/accessibility → material-3-components.md (Search, Accessibility)

### Segmented buttons
- [x] /components/segmented-buttons/overview → material-3-components.md (Segmented buttons: replaced by the connected button group)
- [x] /components/segmented-buttons/specs → material-3-components.md (Segmented buttons: 40dp, 48dp target, density minus 4dp per step)
- [x] /components/segmented-buttons/guidelines → material-3-components.md (Segmented buttons)
- [x] /components/segmented-buttons/accessibility → material-3-components.md (Segmented buttons, Accessibility)

### Side sheets
- [x] /components/side-sheets/overview → material-3-components.md (Side sheets)
- [x] /components/side-sheets/specs → material-3-components.md (Side sheets: 400dp max, 24dp padding, 16dp detached margin, 16dp modal corners)
- [x] /components/side-sheets/guidelines → material-3-components.md (Side sheets: optional back icon, close icon highly recommended)
- [x] /components/side-sheets/accessibility → material-3-components.md (Side sheets: close affordance required, Dialog role)

### Sliders
- [x] /components/sliders/overview → material-3-components.md (Sliders, M3 Expressive changes)
- [x] /components/sliders/specs → material-3-components.md (Sliders: track heights 16 to 96dp per size)
- [x] /components/sliders/guidelines → material-3-components.md (Sliders)
- [x] /components/sliders/accessibility → material-3-components.md (Sliders, Accessibility)

### Snackbar
- [x] /components/snackbar/overview → material-3-components.md (Snackbar)
- [x] /components/snackbar/specs → material-3-components.md (Snackbar: inverse color roles, line configurations)
- [x] /components/snackbar/guidelines → material-3-components.md (Snackbar)
- [x] /components/snackbar/accessibility → material-3-components.md (Snackbar: focus exit, Compose behavior, launch announcement)

### Split button
- [x] /components/split-button/overview → material-3-components.md (Split button)
- [x] /components/split-button/specs → material-3-components.md (Split button: 2dp gap, state layer only when selected, color styles; per-size inner corner radii 4/8/12dp omitted as measurements)
- [x] /components/split-button/guidelines → material-3-components.md (Split button)
- [x] /components/split-button/accessibility → material-3-components.md (Split button, Accessibility)

### Switch
- [x] /components/switch/overview → material-3-components.md (Switch)
- [x] /components/switch/specs → material-3-components.md (Switch: 52x32dp track, handle 16/24/28dp, 48dp target)
- [x] /components/switch/guidelines → material-3-components.md (Switch)
- [x] /components/switch/accessibility → material-3-components.md (Switch, Accessibility)

### Tabs
- [x] /components/tabs/overview → material-3-components.md (Tabs)
- [x] /components/tabs/specs → material-3-components.md (Tabs: 48dp and 64dp heights, colors)
- [x] /components/tabs/guidelines → material-3-components.md (Tabs)
- [x] /components/tabs/accessibility → material-3-components.md (Tabs, Accessibility)

### Text fields
- [x] /components/text-fields/overview → material-3-components.md (Text fields)
- [x] /components/text-fields/specs → material-3-components.md (Text fields: 56dp, surface container highest fill)
- [x] /components/text-fields/guidelines → material-3-components.md (Text fields)
- [x] /components/text-fields/accessibility → material-3-components.md (Text fields, Accessibility)

### Time pickers
- [x] /components/time-pickers/overview → material-3-components.md (Time pickers)
- [x] /components/time-pickers/specs → material-3-components.md (Time pickers: 256dp dial, 48dp handle, color roles)
- [x] /components/time-pickers/guidelines → material-3-components.md (Time pickers)
- [x] /components/time-pickers/accessibility → material-3-components.md (Time pickers, Accessibility)

### Toolbars
- [x] /components/toolbars/overview → material-3-components.md (Toolbars)
- [x] /components/toolbars/specs → material-3-components.md (Toolbars: 64dp, 16dp minimum padding, standard and vibrant containers)
- [x] /components/toolbars/guidelines → material-3-components.md (Toolbars)
- [x] /components/toolbars/accessibility → material-3-components.md (Toolbars, Accessibility)

### Tooltips
- [x] /components/tooltips/overview → material-3-components.md (Tooltips)
- [x] /components/tooltips/specs → material-3-components.md (Tooltips: plain and rich colors, up to two buttons; 24dp height and padding omitted as measurements)
- [x] /components/tooltips/guidelines → material-3-components.md (Tooltips)
- [x] /components/tooltips/accessibility → material-3-components.md (Tooltips, Accessibility)

### Gaps worth distilling
1. **/components/menus/guidelines, slots.** "Don't add buttons, switches, or other direct actions into the menu item. Nested elements should only perform one action. Adding multiple actions can break keyboard navigation and screen reader functionality." Why it matters: agents often put a Switch or IconButton inside a `DropdownMenuItem`.
2. **/components/menus/accessibility, selection cues.** "By default, menu items change shape and color when selected... It's recommended to include another visual cue, like a checkmark." Also: "Disabled menu items can receive focus but aren't selectable." Why it matters: it decides whether `SelectableDropdownMenuItem` gets a checkmark and whether unavailable items are hidden or disabled.
3. **/components/menus/guidelines, behavior.** "Multi-select menus can have many selected items. They stay open until the person dismisses the menu." And "When a menu is opened, the corresponding button or icon button should remain the same visually, with the addition of a pressed state." Why it matters: this is dismiss-on-click logic and anchor styling in Compose.
4. **/components/menus/guidelines, submenus and adaptive.** "Submenus should open next to the parent menu item without overlapping it. Submenus are best used on large screens where there's space." And "In dense products, such as on desktop, menus can open instantly to reduce motion." Why it matters: it decides whether nested menus are used on phone versus desktop, and the motion choice on desktop.
5. **/components/search/guidelines, focused-search behavior.** "The back icon releases focus, dismisses any suggestions or results, and returns the search bar to its original state." And "When search results are queried, the input text should remain visible, but not in focus." Why it matters: this is the core state machine of a Compose `SearchBar`.
6. **/components/search/accessibility.** "When search suggestions and results appear, the screen reader must announce the change." And "The hinted search text should be used as the accessibility label." Android role: Text field. Why it matters: it needs a live region or semantics on the results list, and the label wiring.
7. **/components/search/guidelines, leading icon and status.** The leading slot holds "A navigational icon button, such as a menu or arrow" or "A non-functional search icon". Also, "focused search needs a clear status indicator that it's searching content, like a search icon or **Results** label." Why it matters: agents otherwise put arbitrary actions in the leading slot and show results with no label.
8. **/components/search/specs, widths.** The search bar and docked container are "Min: 360dp, max: 720dp" wide, and docked results are "Min: 240dp, max: 2/3 of screen height". Why it matters: it caps the search width on tablet and desktop instead of letting it stretch full width.
9. **/components/search/guidelines, suggestions and destination.** "If search is the primary action, focused search can be a standalone destination reached from a navigation bar." Add variety with "Category labels, like **Recent**, **Contacts**, or **Suggestions**", avatars, and filter chips. Why it matters: it shapes how the suggestion list is built and where search sits in the nav graph.
10. **/components/menus/guidelines, placement and scrolling.** "If a menu is in a position to be cut off, it should automatically reposition to appear to the left, right, or above." And "Menus can scroll when all menu items can't display at once. In this state, menus show a persistent scrollbar." Why it matters: custom popups and long menus.
11. **/components/menus/specs, standard color mapping.** The standard vertical menu uses Surface container low, with Tertiary container / On tertiary container for selected items. The vibrant menu uses Tertiary container, with Tertiary / On tertiary for selected items. Why it matters: the docs only say "vibrant is tertiary-based", so the selected color in a standard menu is undocumented.
12. **/components/menus/guidelines, filtering.** "A menu can include a text field to filter options. This pattern is also known as autocomplete... Menu items ease into their new position as the menu is filtered." Why it matters: this is the exposed-dropdown or autocomplete pattern for long option lists in forms.
13. **/components/snackbar/accessibility, focus exit.** "Ideally, focus should either return to the element that triggered the snackbar, or go to the next most logical element... On Android Compose, focus may move to the nearest visible element." Why it matters: this is small, since the rest of snackbar accessibility is captured.
14. **/components/side-sheets/guidelines, back icon.** "Because the primary content behind or beside a side sheet is always visible, it's important to provide affordances for leaving a side sheet." The optional back icon button is for multi-level sheet content. Why it matters: this is minor, for navigation inside a sheet.
15. **/components/loading-indicator/guidelines, where pull-to-refresh applies.** Use it "at the beginning of lists, grid lists, and card collections where the most recent content appears". Why it matters: this is minor, but it stops agents adding pull-to-refresh to static screens.

## m3.material.io blog, videos, and articles

Method: /blog/<slug> only serves an index, so I pulled each post's full text from the site's legacy post endpoint (`https://m3.material.io/page-data/Posts/<document_id>.json`; the ids are in the main.js manifest). All 110 posts are now in scratchpad/blogjson and scratchpad/blogtxt. I read every post whose title suggested current guidance. The remaining posts I judged from their titles and first lines. The video transcripts and both article HTML copies were read in full. No files were edited.

### Blog
- [-] /blog; index page with no guidance. Its 2025 and 2026 posts (Expressive launch, motion theming, "Start migrating to Compose") have other slugs and are not in this batch.
- [-] /blog/search.html; search page.
- [-] /blog/2023-google-fonts-redesign; Google Fonts website news, no UI guidance.
- [x] /blog/24-hour-clock-design-research → material-3-components.md (Time pickers)
- [x] /blog/5-steps-large-screen-apps → android-ui-design-guides.md (Layout > Windows, orientation, and continuity) + material-3.md (Layout > Adapting components: size constraints); drawer and 256dp region guidance left out (superseded)
- [-] /blog/accessibility-awareness-day-2022; announcement.
- [-] /blog/android-dark-theme-tutorial; superseded by current docs (M2 MDC Views dark theme).
- [-] /blog/android-material-motion; superseded by current docs (MDC Views transition patterns, already in Motion foundation).
- [-] /blog/android-material-theme-color; superseded by current docs (M2 MDC Views theming).
- [-] /blog/android-material-theme-shape; superseded by current docs (M2 MDC Views theming).
- [-] /blog/android-material-theme-type; superseded by current docs (M2 MDC Views theming).
- [-] /blog/android-stable-release-1-10-0; MDC Views release notes, superseded.
- [-] /blog/android-stable-release-1-12-0; MDC Views release notes, superseded.
- [-] /blog/android-stable-release-1-2; MDC Views release notes, superseded.
- [-] /blog/android-stable-release-1-3-0; MDC Views release notes, superseded.
- [-] /blog/android-stable-release-1-4; MDC Views release notes, superseded.
- [-] /blog/android-stable-release-1-5; MDC Views release notes, superseded.
- [-] /blog/android-stable-release-1-6-1; MDC Views release notes, superseded.
- [-] /blog/android-stable-release-1-7-0; MDC Views release notes, superseded.
- [-] /blog/android-stable-release-1-8-0; MDC Views release notes, superseded.
- [-] /blog/android-stable-release-1-9-0; MDC Views release notes, superseded.
- [-] /blog/announcing-material-you; 2021 launch announcement, superseded by current docs.
- [-] /blog/asset-people-1; imagery and placeholder-avatar editorial, no UI guidance.
- [-] /blog/asset-people-2; imagery editorial.
- [-] /blog/asset-people-3; imagery editorial.
- [-] /blog/atkinson-hyperlegible-design; typeface story.
- [-] /blog/color-fonts-are-here; Google Fonts news.
- [-] /blog/dark-theme-design-tutorial-video; video link, M2.
- [x] /blog/data-visualization-accessibility → material-3.md (Color: Data visualization (accessible charts))
- [-] /blog/derek-brahney-interview; artist interview.
- [-] /blog/design-material-theme-color; superseded by current docs (M2 Figma theming).
- [-] /blog/design-material-theme-shape; superseded by current docs.
- [-] /blog/design-material-theme-type; superseded by current docs.
- [-] /blog/designer-toolbox-figma-android-studio-relay; tooling (Relay).
- [-] /blog/designing-text-visual-acuity-research; methods for scaling text with viewing distance, no UI rule (and Google says these are "not... ideal minimum text sizes").
- [-] /blog/designtocode; tooling.
- [-] /blog/device-metrics; dp calculation, superseded by breakpoints.
- [-] /blog/digital-wellbeing-design-systems; ethics essay, no component or style guidance.
- [-] /blog/digital-wellbeing-face-retouching; camera-filter ethics, out of scope.
- [-] /blog/digital-wellbeing-ux-principles; ethics principles, no UI styling guidance.
- [x] /blog/dynamic-color-harmony → material-3.md (Color: Choosing a scheme, static colors and harmonize limit)
- [-] /blog/finding-ethical-design; essay.
- [-] /blog/fonts-are-software-video; video link.
- [-] /blog/get-google-fonts-update; Google Fonts API news.
- [-] /blog/google-design-tutorial-video; video link.
- [-] /blog/google-fonts-dark-theme; Angular web case study, out of scope.
- [-] /blog/google-fonts-knowledge; announcement.
- [-] /blog/google-fonts-material-icons; superseded by Material Symbols.
- [-] /blog/google-fonts-pairing-figma; Figma resource.
- [-] /blog/google-io-2024; event roundup.
- [-] /blog/google-material-custom-theme; 2018 M2 case study, superseded by current docs.
- [-] /blog/how-to-gemini-app-compose-material-design-3; LLM prompting tips. The quoted outputs recommend a drawer, which is superseded.
- [-] /blog/how-to-make-text-more-accessible; list of accessible typefaces. Its optical-size point is already in Typography.
- [-] /blog/inclusive-imagery-at-google; imagery editorial.
- [-] /blog/interview-oddfellows-m3-art-style; illustration interview.
- [x] /blog/introducing-symbols → material-3.md (Icons: Applying icons, grade matching)
- [-] /blog/jamie-chung-photography-interview; interview.
- [-] /blog/jetpack-compose-beta; superseded.
- [-] /blog/jetpack-compose-catalog; superseded.
- [x] /blog/localization-principles-techniques → material-3.md (Content design: Writing for translation)
- [-] /blog/m3-a11y; I/O 2022 roundup.
- [x] /blog/material-3-carousel-research-design → material-3-components.md (Carousel)
- [-] /blog/material-3-compose-1-1; API release notes, superseded by the verified API reference.
- [-] /blog/material-3-compose-1-2; API release notes, superseded.
- [x] /blog/material-3-compose-1-3 → material-3.md (Compose API reference: Migrating elevation-tinted surfaces), material-3-components.md (Carousel: maskClip / maskBorder)
- [-] /blog/material-3-compose-stable; superseded.
- [-] /blog/material-3-figma-design-kit; Figma.
- [-] /blog/material-3-slot-components-figma; Figma.
- [-] /blog/material-ads-2022; event roundup.
- [-] /blog/material-density-web; web implementation, out of scope (density is in Spacing).
- [-] /blog/material-design-2022-roundup; roundup.
- [-] /blog/material-design-awards-2021; awards.
- [-] /blog/material-design-blog-welcome; editor's letter.
- [-] /blog/material-design-for-large-screens; 2021 grid and drawer guidance, superseded by current docs.
- [-] /blog/material-design-wordpress-plugin; out of scope.
- [-] /blog/material-design-wordpress-plugin-030; out of scope.
- [-] /blog/material-design-xr-dev-preview; XR, out of scope.
- [-] /blog/material-design-youtube-channel; announcement.
- [-] /blog/material-google-io21; event roundup.
- [-] /blog/material-google-io22; event roundup.
- [-] /blog/material-google-io23; event roundup.
- [-] /blog/material-icons-sehee-lee-interview; interview.
- [-] /blog/material-io-redesign; site redesign story.
- [-] /blog/material-partner-studies; 2018 M2 case studies.
- [-] /blog/material-theme-builder; tool launch, superseded by MTB 2.
- [x] /blog/material-theme-builder-2-color-match → material-3.md (Color: color fidelity)
- [-] /blog/material-you-large-screens; 2022 size classes and drawer, superseded (hinge and cutout are in android-ui-design-guides.md).
- [-] /blog/mda-2020-winners; awards.
- [-] /blog/mda-2021-winners; awards.
- [-] /blog/migrate-android-material-components; MDC Views migration.
- [-] /blog/migrating-material-3; 2021 M2 to M3 migration, superseded.
- [x] /blog/motion-research-container-transform → material-3.md (Motion foundation: transitions; Choosing a transition pattern, research bullet)
- [-] /blog/noto-announcement; Google Fonts news.
- [-] /blog/readability-consortium; announcement.
- [x] /blog/readability-research → material-3.md (Typography: Customizing type styles, grade note)
- [-] /blog/readex-pro-legibility-arabic-type-design; typeface story.
- [-] /blog/reduce-reflow-with-web-fonts; web, out of scope.
- [-] /blog/relay-in-alpha; discontinued tooling.
- [-] /blog/relay-ravn-case-study; discontinued tooling.
- [-] /blog/research-state-of-design-systems-2020; industry survey.
- [-] /blog/roboto-flex; typeface launch (axes already in Typography).
- [-] /blog/roboto-serif; typeface launch.
- [x] /blog/science-of-color-design → material-3.md (Color: Contrast for custom colors (HCT tone rule))
- [-] /blog/shantell-martin-variable-font; typeface story.
- [-] /blog/sil-typefaces; typeface news.
- [-] /blog/start-building-with-material-you; 2021 dynamic color how-to, superseded by the API reference.
- [x] /blog/ten-steps-ios-android-design → android-ui-design-guides.md (Translating from iOS)
- [-] /blog/testing-material-3; M2 vs M3 perception study, superseded by the Expressive research in material-3.md.
- [x] /blog/tone-based-surface-color-m3 → material-3.md (Compose API reference: Migrating elevation-tinted surfaces); tone 98 and neutral chroma 6 skipped as values the scheme applies automatically
- [-] /blog/whats-new-design-kit; Figma.
- [-] /blog/why-we-recommend-material-design-components-android; Views.
- [-] /blog/year-in-the-life-material-design-advocate; profile.

### Videos
- [x] youtube.com/watch?v=6IsFP3gD28E → material-3.md (What it is and why; Components: what Expressive replaced; Motion; Compose API reference)
- [x] youtube.com/watch?v=t9rrsqfB2tM → material-3.md (What it is and why; Components: what Expressive replaced; Customizing without forking)
- [x] youtube.com/watch?v=zRBi6oBtpoo → material-3.md (Versions and stability; Other APIs mentioned in the talks)
- [x] youtube.com/watch?v=HbAFGivZ158 → material-3.md (Customizing without forking)
- [x] youtube.com/watch?v=pLNJ-fNYTKU → android-ui-design-guides.md (Layout > Windows, orientation, and continuity)
- [x] youtube.com/watch?v=qEEo6AwgBjU → material-3-wear-os.md (animatedShapes, ButtonGroup animateWidth, TransformingLazyColumn transformation specs)

### Articles
- [x] design.google/library/google-sans-flex-font → material-3.md (Google Sans Flex: Google Sans Text, writing systems)
- [x] design.google/library/expressive-material-design-google-research → material-3.md (What it is and why: design.google findings, context still matters, caveats)

### Gaps worth distilling
1. /blog/science-of-color-design: add the HCT contrast rule, "smaller elements (less than ¼" or 40 dp) require a tone difference of 50 with their background, larger elements require a tone difference of 40." Why it matters: this is the one tool an agent has for custom colors outside the scheme roles (brand accents, charts, widget art, category colors). Without it, contrast is guessed.
2. /blog/localization-principles-techniques: text "can expand by 30% or might even double"; "Leave open space around condensed UI components, such as buttons and tabs"; "establish a component's maximum width that allows lengthier passages to wrap"; Noto for CJK. Why it matters: generated Compose often sizes buttons, tabs and chips to fit English, and they truncate once translated.
3. youtube.com/watch?v=pLNJ-fNYTKU plus /blog/5-steps-large-screen-apps: "The natural orientation of the device isn't always portrait"; "not to cache or hardcode any values about display size, window size or orientation"; keep scroll position, text input and playback state across fold and unfold. Why it matters: foldable, tablet and desktop layouts break when generated code keys off device or orientation instead of window size.
4. /blog/material-3-compose-1-3 and /blog/tone-based-surface-color-m3: the `surfaceColorAtElevation()` to surfaceContainer* mapping (+1 Low, +2 Container, +3 High, +4/+5 Highest, surfaceVariant to Highest), and `maskClip` / `maskBorder` for carousel items. Why it matters: agents editing older Compose code keep elevation tints and plain `clip`, which render wrong or break the carousel mask.
5. /blog/data-visualization-accessibility: use common chart types, keep compared data on the same scales, give a text summary, label axes and marks, pair color with a second encoding at the required contrast, offer the underlying data. Why it matters: dashboards, fitness stats and widgets with charts have no chart guidance in the research docs today.
6. youtube.com/watch?v=zRBi6oBtpoo: "Material Views 1.14 will be our final stable release for the Views Library"; checkable menu items swap the icon "from outlined to filled when an option is toggled"; the new scroll-based time and number input. Why it matters: it confirms Compose-only output and gives a state cue for menus.
7. /blog/24-hour-clock-design-research: 24h users should get digital input by default ("An overwhelming majority of participants perceived a simple digital input as the least confusing"), with the analog dial optional and the user's choice remembered. Why it matters: it changes which TimePicker mode an agent defaults to for 24h locales.
8. /blog/material-3-carousel-research-design: pick the carousel by task, using multi-browse with previews for quick, low-commitment choices and slower advance for high-commitment ones. Why it matters: it is a concrete rule for choosing between carousel variants.
9. design.google/library/google-sans-flex-font: Google Sans Text is the small-size face ("taller, more condensed, and less circular", more spacing, Roboto proportions). Why it matters: an agent pushing Google Sans Flex should know the small-text concerns that shaped the family.
10. /blog/readability-research: at grade 0, light and dark modes are equally readable; very low grade hurts light-mode reading. Why it matters: it tempers the "negative grade in dark mode" advice so agents do not add grade changes that aren't needed.
11. /blog/introducing-symbols: match icon grade to text grade. Why it matters: a small consistency detail for icon and text pairs.

## developer.android.com/design/ui: Mobile, desktop, large screens, widgets

Result: 48 routes checked. 25 are fully distilled, 17 are partly distilled, 6 are skipped, and none are completely missing. Most of the coverage is in android-ui-design-guides.md. The canonical layouts and breakpoints come from material-3.md.

### Desktop
- [-] /design/ui/desktop; hub page with links only, no guidance
- [x] /design/ui/desktop/guides/foundations/design-principles → android-ui-design-guides.md (Desktop and large screens: Principles)
- [x] /design/ui/desktop/guides/foundations/get-started → android-ui-design-guides.md (Desktop and large screens: Content within panes, Text scaling)
- [-] /design/ui/desktop/guides/foundations/across-form-factors; describes the devices only (phone with connected display, tablet folio, Chromebook, XR), no design decisions
- [x] /design/ui/desktop/guides/interaction/pointer-interactions → android-ui-design-guides.md (Pointer, Density)
- [x] /design/ui/desktop/guides/interaction/cursors → android-ui-design-guides.md (Desktop and large screens)
- [x] /design/ui/desktop/guides/interaction/keyboard → android-ui-design-guides.md (Desktop and large screens)
- [x] /design/ui/desktop/guides/system/multi-task → android-ui-design-guides.md (Desktop and large screens)
- [x] /design/ui/desktop/guides/system/system-bars → android-ui-design-guides.md (Desktop and large screens)

### Large screens
- [-] /design/ui/large-screens; returns the same hub content as /design/ui/desktop, no guidance of its own

### Mobile: hub, components, samples
- [-] /design/ui/mobile; hub page with links only
- [-] /design/ui/mobile/guides/components/material-overview; lists Material's component categories and defers to Material (already in material-3-components.md)
- [-] /design/ui/mobile/samples; link list (Now in Android, Jetchat, Jetcaster, Reply)

### Mobile: foundations
- [x] /design/ui/mobile/guides/foundations/accessibility → android-ui-design-guides.md (Accessibility (platform minimums))
- [-] /design/ui/mobile/guides/foundations/glossary; definitions of terms only
- [x] /design/ui/mobile/guides/foundations/system-bars → android-ui-design-guides.md (System bars and edge-to-edge)
- [x] /design/ui/mobile/guides/foundations/translate-designs → android-ui-design-guides.md (Translating from iOS)

### Mobile: styles
- [x] /design/ui/mobile/guides/styles/color → android-ui-design-guides.md (Color and theming) plus material-3.md (Color: Contrast for custom colors, red and green for colorblind users)
- [x] /design/ui/mobile/guides/styles/themes → android-ui-design-guides.md (Color and theming)

### Mobile: layout and content
- [x] /design/ui/mobile/guides/layout-and-content/adapt-layout → android-ui-design-guides.md (Layout)
- [x] /design/ui/mobile/guides/layout-and-content/app-anatomy → android-ui-design-guides.md (System bars and edge-to-edge)
- [-] /design/ui/mobile/guides/layout-and-content/canonical-layouts; dead link (HTTP 404), not scraped
- [x] /design/ui/mobile/guides/layout-and-content/common-layouts → android-ui-design-guides.md (Layout > Windows, orientation, and continuity: supporting sheets, WebView)
- [x] /design/ui/mobile/guides/layout-and-content/content-structure → android-ui-design-guides.md (Layout)
- [x] /design/ui/mobile/guides/layout-and-content/edge-to-edge → android-ui-design-guides.md (System bars and edge-to-edge)
- [x] /design/ui/mobile/guides/layout-and-content/grids-and-units → android-ui-design-guides.md (Layout)
- [x] /design/ui/mobile/guides/layout-and-content/images-graphics → android-ui-design-guides.md (Layout)
- [x] /design/ui/mobile/guides/layout-and-content/immersive-content → android-ui-design-guides.md (System bars and edge-to-edge: Immersive mode)
- [x] /design/ui/mobile/guides/layout-and-content/layout-and-nav-patterns → android-ui-design-guides.md (Layout: navigation pairings, actions)
- [x] /design/ui/mobile/guides/layout-and-content/layout-basics → android-ui-design-guides.md (Layout, System bars and edge-to-edge)
- [x] /design/ui/mobile/guides/layout-and-content/postures-and-orientation → android-ui-design-guides.md (Layout: landscape and postures)

### Mobile: patterns
- [x] /design/ui/mobile/guides/patterns/help-content → android-ui-design-guides.md (Help and feedback)
- [x] /design/ui/mobile/guides/patterns/onboarding → android-ui-design-guides.md (Onboarding and sign-in)
- [x] /design/ui/mobile/guides/patterns/passkeys → android-ui-design-guides.md (Onboarding and sign-in)
- [x] /design/ui/mobile/guides/patterns/predictive-back → android-ui-design-guides.md (Predictive back)
- [x] /design/ui/mobile/guides/patterns/settings → android-ui-design-guides.md (Settings); the scraped settings-versus-filters section is one sentence long, so it is captured in full

### Mobile: home screen
- [x] /design/ui/mobile/guides/home-screen/live-updates → android-ui-design-guides.md (Live updates and picture-in-picture)
- [x] /design/ui/mobile/guides/home-screen/notifications → android-ui-design-guides.md (Notifications)
- [x] /design/ui/mobile/guides/home-screen/picture-in-picture → android-ui-design-guides.md (Live updates and picture-in-picture)
- [x] /design/ui/mobile/guides/home-screen/widgets → android-ui-design-guides.md (Widgets)

### Mobile: widgets
- [x] /design/ui/mobile/guides/widgets → android-ui-design-guides.md (Widgets)
- [x] /design/ui/mobile/guides/widgets/configuration → android-ui-design-guides.md (Widgets)
- [x] /design/ui/mobile/guides/widgets/discovery-promotion → android-ui-design-guides.md (Widgets)
- [x] /design/ui/mobile/guides/widgets/layouts → android-ui-design-guides.md (Widgets: Canonical layouts); small miss: Auto widget lists don't scroll
- [x] /design/ui/mobile/guides/widgets/sizing → android-ui-design-guides.md (Widgets)
- [x] /design/ui/mobile/guides/widgets/style → android-ui-design-guides.md (Widgets: Style)

### Widget hub and quality
- [-] /design/ui/widget; hub page with links only
- [x] /docs/quality-guidelines/widget-quality → android-ui-design-guides.md (Widgets)

### Gaps worth distilling
1. /design/ui/desktop/guides/foundations/get-started: "Consider any content density optional and always allow for text scaling within the layout, don't hard set type sizes." Why: agents building dense desktop UI tend to hard-code small fixed text sizes.
2. /design/ui/mobile/guides/home-screen/notifications: the importance table (HIGH makes a sound and appears on screen for "time-critical information"; DEFAULT makes a sound; LOW no sound; MIN no visual interruption), plus "When the user taps a notification, your app must display UI that relates directly to that notification." Why: without it, agents pick channel importance and tap targets arbitrarily.
3. /design/ui/mobile/guides/foundations/system-bars: "Use transparent three-button navigation bars when there is a bottom app bar or bottom app navigation bar," via setNavigationBarContrastEnforced(false) with bottom bars padded to draw underneath. Why: this is the one case where the default scrim is wrong, and a Compose NavigationBar sits right in it.
4. /design/ui/desktop/guides/interaction/keyboard: keep focus inside modal dialogs; "when keyboard focus lands on a slider, users expect the Left and Right arrow keys to adjust the value"; arrow keys move focus as a 2D grid. Why: these are the keyboard behaviors most often broken in Compose on tablets, ChromeOS and desktop.
5. /design/ui/mobile/guides/layout-and-content/images-graphics: "Avoid including immutable text in assets," "Use vector formats first," "Provide sufficient scrim between background images and text," and blur "can affect performance and [is] only available on devices running Android 12 and higher." Why: this governs image choice, text-over-image legibility and Modifier.blur use.
6. /docs/quality-guidelines/widget-quality (WL-3): header rules (recommended for scrolling content or useful context; optional for full-bleed or tight space; icon always, title when space allows; actions from widget context) and WL-4.1 "Max size should be set if resizing the widget only adds blank space." Why: these are differentiated-tier criteria that directly change Glance layout code.
7. /design/ui/mobile/guides/foundations/translate-designs: "Your primary navigation should always be present on parent views"; child views keep it unless modal; parent views without a drawer or rail show no navigation icon. Why: it settles when a Compose screen shows the nav bar and the leading app bar icon.
8. /design/ui/mobile/guides/patterns/passkeys: offer passkey creation at account creation, after a password reset, after a password sign-in and in settings; lead with benefits; always confirm creation; the management list shows the password manager name and icon, created and last-used times, and "delete." Why: agents building sign-in or account screens otherwise invent their own flow.
9. /design/ui/mobile/guides/widgets/configuration: "If configuration is not completed, don't cancel adding the widget. Provide a state to allow for restoring or configuring within the widget," plus an empty state with an onboarding or sign-in reminder. Why: it prevents widgets that silently vanish or stay blank.
10. /design/ui/mobile/guides/layout-and-content/edge-to-edge: "navigation drawers should also have a separate protection from the rest of the app"; "Bottom app bars should collapse while scrolling," with a three-button scrim when the bar animates away. Why: agents wiring ModalNavigationDrawer or BottomAppBar edge-to-edge get these wrong.
11. /design/ui/desktop/guides/interaction/cursors: set system cursor icons to match the interaction (hand for clickable, grab or grabbing for drag, zoom in for zoomable content, text); custom icons only for actions Android does not provide. Why: in Compose this maps to Modifier.pointerHoverIcon, which agents rarely set.
12. /design/ui/mobile/guides/layout-and-content/common-layouts: "In most cases, we recommend using a standard web browser, like Chrome, to deliver content." Why: it keeps agents from embedding WebViews for links or help content.
13. /design/ui/mobile/guides/widgets/sizing: the tablet table (2x1 180-304 by 64-120; 2x2 180-304 by 184-304; 2x3 180-304 by 304-488; 3x1 328-488 by 64-120; 3x2 298-488 by 184-304; 3x3 298-488 by 304-488; 3x4 298-488 by 424-672) and "Don't use fixed square shapes." Why: needed to set widget breakpoints and targetCell values on tablets.
14. /design/ui/desktop/guides/system/multi-task: "Media playback should continue when users minimize the app or move it to the background," with PiP controls for play, pause, previous and next. Why: it changes lifecycle and playback choices for media apps in desktop windows.
15. /design/ui/mobile/guides/patterns/onboarding: first check whether a walkthrough is needed at all, since "complex features can be introduced more naturally through subtle motion cues or in-context tooltips"; offer biometrics and autofill; never pre-fill passwords, mask sensitive input. Why: it stops agents from generating onboarding carousels by default.

## developer.android.com/design/ui: Gallery

The docs cover the Gallery mainly through the "Gallery patterns by app category" table in android-ui-design-guides.md (Desktop and large screens) and the canonical layouts in material-3.md (Scaffold). Most of the 41 pages are short one-liners, and that table captures them. Four areas are missing. Pawparazzi's Grid and FlexBox guidance and the tabletop above-and-below-the-fold rule are the biggest. Multi-window is never named in any research doc, although six pages recommend it. Stylus hover details are also missing.

### Creativity
- [-] /design/ui/gallery/creativity/concepts; results-only case study (70% more tablet time) plus boilerplate links to list-detail and the Figma kit
- [x] /design/ui/gallery/creativity/configurable → android-ui-design-guides.md (Gallery patterns by app category: movable, resizable palettes; contextual menus)
- [-] /design/ui/gallery/creativity/luminar; showcase of one app's custom photo sliders, film styling and sound design, with no transferable Android layout rule beyond the supporting-pane boilerplate
- [x] /design/ui/gallery/creativity/multi-task → android-ui-design-guides.md (Desktop and large screens: Multi-window)
- [x] /design/ui/gallery/creativity/stylus → android-ui-design-guides.md (Desktop and large screens: Stylus)
- [x] /design/ui/gallery/creativity/utility → android-ui-design-guides.md (Gallery table: palettes, contextual menus) + material-3.md (Scaffold: supporting pane)

### Games
- [x] /design/ui/gallery/games/continuity → android-ui-design-guides.md (Layout > Windows, orientation, and continuity)
- [x] /design/ui/gallery/games/peripherals → android-ui-design-guides.md (Desktop and large screens: Peripherals)

### Media
- [x] /design/ui/gallery/media/detail → android-ui-design-guides.md (Layout > Windows, orientation, and continuity: tabletop; Gallery patterns: media detail)
- [x] /design/ui/gallery/media/discovery → material-3.md (Scaffold: canonical feed) + android-ui-design-guides.md (Gallery table: related titles and reviews)
- [x] /design/ui/gallery/media/multitask → android-ui-design-guides.md (Desktop and large screens: Multi-window)
- [-] /design/ui/gallery/media/spotify; company story about form-factor reach, no design guidance

### Productivity
- [x] /design/ui/gallery/productivity/file-browsing → android-ui-design-guides.md (Gallery table: thumbnails, right-click menus) + material-3.md (Scaffold: list-detail)
- [x] /design/ui/gallery/productivity/multitask → android-ui-design-guides.md (Desktop and large screens: Multi-window; Layout: tabletop)
- [x] /design/ui/gallery/productivity/tools → android-ui-design-guides.md (Gallery table: tools and comments without covering the document)
- [-] /design/ui/gallery/productivity/wps; link-out case study on foldables, no guidance

### Reading
- [x] /design/ui/gallery/reading/abcmouse → android-ui-design-guides.md (Gallery patterns by app category: Kids)
- [x] /design/ui/gallery/reading/browsing → android-ui-design-guides.md (Gallery patterns by app category: hierarchy in feeds)
- [x] /design/ui/gallery/reading/complementary → android-ui-design-guides.md (Gallery table: collapsible pane for notes and comments)
- [x] /design/ui/gallery/reading/formatting → android-ui-design-guides.md (Gallery patterns by app category: reading and annotation)
- [x] /design/ui/gallery/reading/full-screen → android-ui-design-guides.md (Gallery table: full-screen reading, adapted line length) + material-3.md (Breakpoints: 40 to 60 characters)
- [x] /design/ui/gallery/reading/multi-task → android-ui-design-guides.md (Desktop and large screens: Multi-window)
- [x] /design/ui/gallery/reading/news → material-3.md (Scaffold: canonical list-detail; headlines beside the full article)
- [-] /design/ui/gallery/reading/readera; testimonial quote only

### Shopping
- [x] /design/ui/gallery/shopping/catalogs → android-ui-design-guides.md (Gallery patterns by app category: hierarchy in feeds)
- [-] /design/ui/gallery/shopping/ebay; results-only case study (4.7 rating)
- [x] /design/ui/gallery/shopping/multi-task → android-ui-design-guides.md (Gallery table: drag-and-drop cart and wish list; Desktop: drag and drop between apps)
- [x] /design/ui/gallery/shopping/product-details → android-ui-design-guides.md (Gallery table: detail beside the product list)
- [x] /design/ui/gallery/shopping/product-filter → android-ui-design-guides.md (Gallery table: filters in a supporting pane)

### Social
- [x] /design/ui/gallery/social/comments → android-ui-design-guides.md (Gallery table: comments in a supporting pane)
- [x] /design/ui/gallery/social/conversations → android-ui-design-guides.md (Gallery table: conversations list-detail)
- [x] /design/ui/gallery/social/drag-and-drop → android-ui-design-guides.md (Gallery table, Desktop: drag and drop, click-and-drag)
- [x] /design/ui/gallery/social/dual-screen → android-ui-design-guides.md (Gallery patterns by app category: dual-screen)
- [-] /design/ui/gallery/social/duo; results-only case study; its tabletop video-call idea is already in the Productivity row
- [x] /design/ui/gallery/social/laptop-input → material-3.md (Interaction: Inputs, Enter sends, Space plays) + android-ui-design-guides.md (Desktop: hover, shortcuts)
- [-] /design/ui/gallery/social/meta; testimonial quote only
- [x] /design/ui/gallery/social/multi-tasking → android-ui-design-guides.md (Desktop and large screens: Multi-window)
- [x] /design/ui/gallery/social/pawparazzi → android-ui-design-guides.md (Layout > Windows, orientation, and continuity: Grid and FlexBox)
- [x] /design/ui/gallery/social/posts → android-ui-design-guides.md (Gallery table: comments in a supporting pane; same copy as comments)
- [x] /design/ui/gallery/social/tools → android-ui-design-guides.md (Gallery table: Creativity palettes) + material-3.md (Scaffold: supporting pane)
- [x] /design/ui/gallery/social/video-browsing → android-ui-design-guides.md (Gallery table: browse while playing)

### Gaps worth distilling
1. /design/ui/gallery/social/pawparazzi: Grid spans for hierarchy ("The grid may be 2x4, but the top spot spans 2 columns and rows"), subgrids (2-across on compact), and FlexBox for filter chips that "respond to their labels", revealing more filters on larger screens. Why: this is the only Google guidance on when to use the new Compose Grid and FlexBox APIs, which the docs name only as APIs.
2. Multi-window across media/multitask, productivity/multitask, reading/multi-task, creativity/multi-task and social/multi-tasking: "Double device usability with side-by-side apps in multi-window or multi-instance scenarios", plus desktop windows that "can also be resized". Why: the term never appears in the research docs, so an agent will not plan for half-screen windows or two instances side by side.
3. /design/ui/gallery/media/detail: in tabletop posture, "Place playback media above the fold, controls and supplementary content below the fold". Why: this concrete placement rule turns the vague "tabletop suits large controls" into a layout an agent can build.
4. /design/ui/gallery/pawparazzi: "designing for specific pixel-perfect lockups is not only ineffective, it can also negatively impact user experience"; think of content in flexible containers and derive adaptation points from the app's primary goal. Why: this principle stops agents from hard-coding per-device mockups.
5. /design/ui/gallery/creativity/stylus: low latency input; stylus hover for tooltips and highlighting; "preview the selected brush size and shape"; hover to show media and file previews. Why: the docs list stylus features but give no hover behavior to implement.
6. /design/ui/gallery/reading/formatting: "A heads-up control pane puts palettes and tools within easy reach for comments, annotations, notes, and highlighting." Why: this names the tool-pane pattern for document and annotation screens on large screens.
7. /design/ui/gallery/reading/browsing + /shopping/catalogs: feature key items "with prominent size and position" or "outsized dimensions"; add synopses or excerpts to listings; side navigation. Why: without this, an agent renders large-screen feeds as uniform grids with no hierarchy.
8. /design/ui/gallery/games/continuity: "Support configuration changes gracefully" across orientation, posture and window size, and let users "pick up where they left off". Why: state preservation across resizing is the adaptive failure users notice most.
9. /design/ui/gallery/games/peripherals: support external game controllers, plus mouse, trackpad and keyboard on ChromeOS and Play Games on PC. Why: this matters only for game or controller-driven projects.
10. /design/ui/gallery/reading/abcmouse: for young children, "bigger touch targets and forgiving gestures", "visual and audio cues over text", clear progress markers and fewer choices at once. Why: this matters only if a kids app comes up.
11. /design/ui/gallery/social/dual-screen: show content on both foldable screens at once (rear-camera selfies, interpreter). Why: hardware-specific, lowest priority.

## developer.android.com/design/ui: Wear OS (current guides)

Of the 50 routes: 24 are distilled [x], 22 are partial [~], none are missing outright, and 4 are skipped [-]. The research doc `docs/research/material-3-wear-os.md` covers the principles, levels of expression, adaptive sizing and the 225dp breakpoint, core color pairings, typography roles, and the tile basics well. Most of the partial pages lost operational detail: the full color role set, font-scaling rules per type role, tile slot structure, the preset non-scrolling components, the finer gesture rules, and media IA.

Each line ends with how far that page's guidance reached `skills/android-design/references/wear-os.md`, as "skill: yes/partial/no/n/a". Status itself is judged by the research docs only.

### Get started
- [x] /design/ui/wear/guides/get-started → material-3-wear-os.md (What Expressive means on a watch); skill: partial (only "embrace round" is stated as a principle)
- [x] /design/ui/wear/guides/get-started/apply → material-3-wear-os.md (What Expressive means; Typography; Shape); skill: partial (grouped containers even or uneven, shape for loading animations absent)
- [x] /design/ui/wear/guides/get-started/benefits → material-3.md (Part 2 caveats: calmer minority, function over flourish); skill: n/a
- [x] /design/ui/wear/guides/get-started/design-for-wearables → material-3-wear-os.md (Watch design principles)
- [x] /design/ui/wear/guides/get-started/design-for-wearables/principles → material-3-wear-os.md (Watch design principles); skill: partial ("works offline" appears only as offline behavior)
- [-] /design/ui/wear/guides/get-started/design-kits; Figma kit links, no guidance
- [x] /design/ui/wear/guides/get-started/levels-expression → material-3-wear-os.md (Levels of expression); skill: yes

### Foundations: adaptive design
- [x] /design/ui/wear/guides/foundations/adaptive-design → material-3-wear-os.md (Layout, Adaptive sizes); skill: yes (except "element height changes non-linearly with font scale and bold text")
- [x] /design/ui/wear/guides/foundations/adaptive-design/principles-adding-value → material-3-wear-os.md (Adaptive sizes: more content, larger components, bolder graphs); skill: partial ("thicker rings", "padding between content items" absent)
- [-] /design/ui/wear/guides/foundations/adaptive-design/wear/guides/common-design-layouts; malformed link, not scraped
- [x] /design/ui/wear/guides/foundations/adaptive-layouts → material-3-wear-os.md (Adaptive sizes); same content as adaptive-design and principles-adding-value; skill: yes
- [x] /design/ui/wear/guides/foundations/canonical-adaptive-layouts → material-3-wear-os.md (Layout: Non-scrolling details, Scrolling details; Adaptive sizes; Tiles)
- [x] /design/ui/wear/guides/foundations/screen-sizes → material-3-wear-os.md (Adaptive sizes); skill: yes
- [-] /design/ui/wear/guides/foundations/download; duplicate of design-kits Figma links

### Foundations: common layouts
- [x] /design/ui/wear/guides/foundations/common-layouts → material-3-wear-os.md (Layout); overview of the three layout families; skill: yes
- [x] /design/ui/wear/guides/foundations/common-layouts/apps-non-scrolling → material-3-wear-os.md (Layout: Non-scrolling details)
- [x] /design/ui/wear/guides/foundations/common-layouts/apps-scrolling → material-3-wear-os.md (Layout: Scrolling details)
- [x] /design/ui/wear/guides/foundations/common-layouts/tiles → material-3-wear-os.md (Tiles)

### Foundations: quality tiers
- [x] /design/ui/wear/guides/foundations/quality-tiers → material-3-wear-os.md (Adaptive sizes: quality tiers); skill: partial (tiers are not named)
- [x] /design/ui/wear/guides/foundations/quality-tiers/ready-all-screens → material-3-wear-os.md (Adaptive sizes); skill: yes
- [x] /design/ui/wear/guides/foundations/quality-tiers/responsive-optimized → material-3-wear-os.md (Adaptive sizes: don't stretch, don't enlarge fonts unless graphic); skill: yes
- [x] /design/ui/wear/guides/foundations/quality-tiers/adaptive-differentiated → material-3-wear-os.md (Adaptive sizes; Levels of expression "unremarkable"); skill: partial (the don't "Only rely on responsive behavior" and per-surface examples absent)
- [x] /design/ui/wear/guides/foundations/larger-screens-ready → material-3-wear-os.md (Adaptive sizes); skill: yes
- [x] /design/ui/wear/guides/foundations/larger-screens-optimized → material-3-wear-os.md (Adaptive sizes); skill: yes
- [x] /design/ui/wear/guides/foundations/larger-screens-differentiated → material-3-wear-os.md (Adaptive sizes); skill: yes

### Styles: color
- [x] /design/ui/wear/guides/styles/color → material-3-wear-os.md (Color: build from black, semantic red and green); skill: partial (semantic color absent)
- [x] /design/ui/wear/guides/styles/color/apply → material-3-wear-os.md (Color: Additional pairing and pairings to avoid)
- [x] /design/ui/wear/guides/styles/color/roles-tokens → material-3-wear-os.md (Color: Full role set)
- [x] /design/ui/wear/guides/styles/color/system → material-3-wear-os.md (Color: Dark theme only and designing across seeds)

### Styles: typography
- [x] /design/ui/wear/guides/styles/typography → material-3-wear-os.md (Typography); skill: yes
- [x] /design/ui/wear/guides/styles/typography/accessibility → material-3-wear-os.md (Typography: 6% steps, design for largest and smallest); skill: partial (6% steps absent)
- [x] /design/ui/wear/guides/styles/typography/apply → material-3-wear-os.md (Typography: roles, 1.1x line height, last-line extra height, tabular numbers); skill: partial (LabelLarge on title buttons and LabelSmall for secondary labels and compact buttons absent everywhere)
- [x] /design/ui/wear/guides/styles/typography/fonts → material-3-wear-os.md (Typography: weight and width); skill: yes
- [x] /design/ui/wear/guides/styles/typography/type-scale-tokens → material-3-wear-os.md (Typography)

### Patterns
- [x] /design/ui/wear/guides/patterns/gestures → material-3-wear-os.md (Gestures)
- [x] /design/ui/wear/guides/patterns/media → material-3-wear-os.md (Media)
- [x] /design/ui/wear/guides/patterns/media/color → material-3-wear-os.md (Color: artwork seed, watch-face fallback, monochrome); skill: no
- [x] /design/ui/wear/guides/patterns/media/controls → material-3-wear-os.md (Media; Layout: 48dp tap target)
- [x] /design/ui/wear/guides/patterns/media/principles → material-3-wear-os.md (Media)
- [x] /design/ui/wear/guides/patterns/media/use-cases → material-3-wear-os.md (Media)

### Surfaces: apps
- [x] /design/ui/wear/guides/surfaces/apps → material-3-wear-os.md (Watch design principles: focused, shallow, vertical); container types are image captions only; skill: yes
- [x] /design/ui/wear/guides/surfaces/apps/best-practices → material-3-wear-os.md (Layout: time text, icons and labels, elevate primary actions, scroll indicator, percentage margins); skill: partial ("icons and labels" and "no label on single-content-type dialogs" absent)
- [x] /design/ui/wear/guides/surfaces/apps/layouts → material-3-wear-os.md (Layout: App layout sections)
- [x] /design/ui/wear/guides/surfaces/apps/layouts/non-scrolling → material-3-wear-os.md (Layout: Non-scrolling details)
- [x] /design/ui/wear/guides/surfaces/apps/layouts/scrolling → material-3-wear-os.md (Layout; Adaptive sizes: percentage outer margins, fixed dp between); rest is image captions; skill: yes

### Surfaces: tiles
- [x] /design/ui/wear/guides/surfaces/tiles → material-3-wear-os.md (Tiles: immediate, predictable, relevant); skill: partial
- [x] /design/ui/wear/guides/surfaces/tiles/bestpractices → material-3-wear-os.md (Tiles)
- [-] /design/ui/wear/guides/surfaces/tiles/reference/kotlin/androidx/wear/protolayout/material3/package-summary; API reference, not design guidance, not scraped
- [x] /design/ui/wear/guides/surfaces/tiles/responsive-adaptive-design → material-3-wear-os.md (Adaptive sizes; Tiles); skill: yes
- [x] /design/ui/wear/guides/surfaces/tiles/states → material-3-wear-os.md (Tiles)

### Gaps worth distilling
1. **/surfaces/apps/layouts/non-scrolling, /foundations/common-layouts/apps-non-scrolling:** "consider a multi-page layout with either vertical or horizontal pagination". Both the research doc and the skill say "no horizontal carousels", so an agent will wrongly rule out horizontal pagers.
2. **/styles/color/roles-tokens:** the Surface-Container-Low/default/High roles, the Dim and Container roles for secondary, tertiary and error, and the rule that container roles "shouldn't be used for text or icons". Without these an agent picks surface and emphasis roles for cards and buttons by guesswork.
3. **/styles/typography/type-scale-tokens:** "None of the Display type styles can scale"; Numerals and LabelLarge don't scale either; Title, LabelMedium/Small, Body and Arc do. This decides which text needs font-scale testing and where clipping will happen.
4. **/surfaces/apps/layouts:** the bottom section holds an edge button, "a button stack, or two-icon button group", or stays empty at the end of a journey; the top section may hold a compact button on very long pages. This gives concrete compositions for multi-action screens.
5. **/patterns/gestures:** only one element owns the primary action at a time; wrist-turn overrides stay within "dismiss, silence, or minimize"; disable wrist turn on an active workout or emergency call; gestures fire the same sound and visual feedback as touch. This prevents wrong gesture wiring in Compose.
6. **/foundations/canonical-adaptive-layouts, /apps-non-scrolling:** "Consider the use of the rotary scroll button to control elements of the screen when its size is limited." Rotary input is absent from both files, yet it is the main input for pickers and steppers.
7. **/foundations/common-layouts/apps-scrolling:** past 225dp, "all of the same content below the fold should still be available regardless of screen size"; top and bottom margins depend on the first and last component. This guards against breakpoint layouts that drop content.
8. **/foundations/common-layouts/tiles:** title, main ("expand") and bottom slots; past 225dp add slots or bottom content; "Don't just scale up the design." This sets the structure for any generated tile.
9. **/surfaces/apps/layouts/non-scrolling:** stepper (buttons or crown plus a curved level indicator), time picker (up to 3 columns), date picker (one column in view), open-on-phone overlay. An agent should use these presets instead of building custom ones.
10. **/surfaces/tiles/states:** "Indicate that an ongoing activity is already in progress. Don't start a new instance"; give content-empty states an image so they feel complete. This affects tile state design and a quality-guideline requirement.
11. **/patterns/media, /patterns/media/use-cases:** browse plus entity page in a flat IA; "more than 5 actions" go to an overflow page; loading placeholders mirror the layout; show download size and progress; prompt for output before playback. These define a media app's screen set.
12. **/foundations/canonical-adaptive-layouts:** past the breakpoint "Confirmation dialogs can gain an illustration or more information. Fitness screens can gain additional metrics." These are concrete add-value moves for non-scrolling screens.
13. **/patterns/media/controls:** "The minimum tap target size is 48 x 48 dp on Wear OS devices"; ring and button sizes (54/70dp, 26/32dp icons); bottom button sizes. The 48dp Wear minimum appears only in the tile rule.
14. **/styles/color/system:** "Wear OS uses only the dark theme"; design with the baseline scheme first and preview across seeds in Material Theme Builder. This stops agents from generating a light Wear theme.
15. **/surfaces/tiles/bestpractices:** "Ensure the app icon provided is monochrome if you are having dynamic theming on your tile." It's a small asset rule that is easy to miss.

## developer.android.com/design/ui: Wear OS (components and M2.5 guides)

Summary: 54 URLs checked. 20 fully distilled, 14 partly distilled, 3 not distilled, 17 skipped (mostly M2.5 component styling that Wear M3 supersedes, plus kit, landing and pointer pages). Wear behaviors and surface priorities are well covered. The biggest remaining gaps are swipe-to-reveal behavior, the ongoing-activity rules for tiles, complications, and sign-in edge cases.

### Landing and samples
- [-] /design/ui/wear; landing page of links to guides and kits, no guidance
- [-] /design/ui/wear/samples; links to GitHub samples (Compose starter, Golden tile, Horologist), no guidance

### Current components
- [x] /design/ui/wear/guides/components/dialogs → material-3-wear-os.md (Behaviors: Dialogs)

### M2.5 behaviors and patterns
- [x] /design/ui/wear/guides/m2-5/behaviors-and-patterns/clipping → material-3-wear-os.md (Behaviors: Clipping; Adaptive sizes)
- [x] /design/ui/wear/guides/m2-5/behaviors-and-patterns/disconnect → material-3-wear-os.md (Behaviors: Offline)
- [x] /design/ui/wear/guides/m2-5/behaviors-and-patterns/launch → material-3-wear-os.md (Behaviors: Launch)
- [x] /design/ui/wear/guides/m2-5/behaviors-and-patterns/navigation → material-3-wear-os.md (Behaviors: Navigation)
- [x] /design/ui/wear/guides/m2-5/behaviors-and-patterns/ongoing-activities → material-3-wear-os.md (Behaviors: Ongoing activities; Surface priorities: tiles update about once a minute)
- [x] /design/ui/wear/guides/m2-5/behaviors-and-patterns/permission → android-ui-design-guides.md (Onboarding and sign-in: "prime permissions at the moment of need"); the Wear note that multiple requests appear one after another is not recorded (minor)
- [x] /design/ui/wear/guides/m2-5/behaviors-and-patterns/permission-message → material-3-wear-os.md (Behaviors)
- [x] /design/ui/wear/guides/m2-5/behaviors-and-patterns/physical-buttons → material-3-wear-os.md (Behaviors: Physical buttons); press-and-hold at 500ms or longer is omitted (minor)
- [x] /design/ui/wear/guides/m2-5/behaviors-and-patterns/sign-in → material-3-wear-os.md (Behaviors)

### M2.5 components
- [-] /design/ui/wear/guides/m2-5/components/buttons; M2.5 button styling (sizes, emphasis fills, outlined 60% stroke) is replaced by Wear M3 buttons
- [-] /design/ui/wear/guides/m2-5/components/cards; M2.5 card styling and gradients are replaced by Wear M3; the "max 60% of screen height or it clips" note is styling
- [x] /design/ui/wear/guides/m2-5/components/chips → material-3-wear-os.md (Behaviors: single prominent action)
- [x] /design/ui/wear/guides/m2-5/components/confirmation-overlay → material-3-wear-os.md (Behaviors)
- [x] /design/ui/wear/guides/m2-5/components/dialogs → material-3-wear-os.md (Behaviors: Dialogs)
- [x] /design/ui/wear/guides/m2-5/components/expandable-item → material-3-wear-os.md (Behaviors)
- [-] /design/ui/wear/guides/m2-5/components/lists; ScalingLazyColumn padding and margin specs are replaced by TransformingLazyColumn (the wear-compose-m3 skill); snapping guidance ("use snapping when items are tall but not taller than the screen") is implementation detail
- [-] /design/ui/wear/guides/m2-5/components/page-indicators; M2.5 HorizontalPageIndicator styling (curved, max 6 dots), replaced by Wear M3
- [x] /design/ui/wear/guides/m2-5/components/pickers → material-3-wear-os.md (Behaviors)
- [-] /design/ui/wear/guides/m2-5/components/position-indicator; arc size specs are superseded; "scroll indicator only on scrolling screens" is already in Layout
- [x] /design/ui/wear/guides/m2-5/components/progress-indicator → material-3-wear-os.md (Behaviors)
- [-] /design/ui/wear/guides/m2-5/components/sliders; styling is superseded; the immediacy rule is covered in material-3-components.md (Slider: "must take effect immediately"); the Wear-only segmented slider for 3 to 9 values is minor
- [-] /design/ui/wear/guides/m2-5/components/steppers; M2.5 anatomy only; steppers as a full-screen non-scrolling control are listed in material-3-wear-os.md Layout
- [x] /design/ui/wear/guides/m2-5/components/swipe-to-dismiss → material-3-wear-os.md (Behaviors)
- [x] /design/ui/wear/guides/m2-5/components/swipe-to-reveal → material-3-wear-os.md (Behaviors)
- [x] /design/ui/wear/guides/m2-5/components/time-text → material-3-wear-os.md (Behaviors)
- [-] /design/ui/wear/guides/m2-5/components/toggle-chips; styling is superseded by Wear M3 switch buttons; the switch, radio, and checkbox semantics are covered in material-3-components.md (Checkbox, Radio button, Switch)
- [-] /design/ui/wear/guides/m2-5/components/vignette; M2.5-only visual component with no Wear M3 counterpart, scaling spec only

### M2.5 foundations
- [x] /design/ui/wear/guides/m2-5/foundations/adaptive-layouts → material-3-wear-os.md (Adaptive sizes; Layout)
- [x] /design/ui/wear/guides/m2-5/foundations/canonical-adaptive-layouts → material-3-wear-os.md (Layout: Non-scrolling details; Adaptive sizes)
- [x] /design/ui/wear/guides/m2-5/foundations/design-principles → material-3-wear-os.md (Watch design principles)
- [-] /design/ui/wear/guides/m2-5/foundations/download; Figma kit and Roboto download links only
- [x] /design/ui/wear/guides/m2-5/foundations/getting-started → material-3-wear-os.md (Watch design principles: 22% less space, no spreadsheets, round first, test in motion)
- [x] /design/ui/wear/guides/m2-5/foundations/larger-screens-differentiated → material-3-wear-os.md (Levels of expression; Adaptive sizes)
- [x] /design/ui/wear/guides/m2-5/foundations/larger-screens-optimized → material-3-wear-os.md (Adaptive sizes: don't stretch, don't enlarge fonts)
- [x] /design/ui/wear/guides/m2-5/foundations/larger-screens-ready → material-3-wear-os.md (Adaptive sizes: quality tiers, don't just scale up)
- [x] /design/ui/wear/guides/m2-5/foundations/screen-sizes → material-3-wear-os.md (Adaptive sizes: small first, percentage margins, 225dp breakpoint)
- [x] /design/ui/wear/guides/m2-5/foundations/wear-os-for-kids → material-3-wear-os.md (Wear OS for kids)

### M2.5 styles
- [-] /design/ui/wear/guides/m2-5/styles/color; the M2.5 baseline palette (200 tones, AA 4.5:1) is replaced by Wear M3 color (material-3-wear-os.md Color); the black background is already covered
- [-] /design/ui/wear/guides/m2-5/styles/icons; pointer to Material icon principles, no Wear-specific guidance
- [-] /design/ui/wear/guides/m2-5/styles/theme; the 13-color and 11-type-style M2.5 theming model is replaced by the Wear M3 theme
- [-] /design/ui/wear/guides/m2-5/styles/typography; Roboto medium and regular are replaced by Roboto Flex and the M3 Wear type scale

### M2.5 surfaces
- [x] /design/ui/wear/guides/m2-5/surfaces/apps-layouts → material-3-wear-os.md (Layout: Non-scrolling details)
- [x] /design/ui/wear/guides/m2-5/surfaces/apps-principles → material-3-wear-os.md (Layout: Non-scrolling details, Scrolling details)
- [x] /design/ui/wear/guides/m2-5/surfaces/complications → material-3-wear-os.md (Surface priorities, watch faces, and notifications)
- [x] /design/ui/wear/guides/m2-5/surfaces/interaction-types → material-3-wear-os.md (Surface priorities: weather P1/P2/P3 example)
- [x] /design/ui/wear/guides/m2-5/surfaces/media-apps → material-3-wear-os.md (Media)
- [x] /design/ui/wear/guides/m2-5/surfaces/notifications → material-3-wear-os.md (Surface priorities: Notifications)
- [x] /design/ui/wear/guides/m2-5/surfaces/tiles-design-system → material-3-wear-os.md (Tiles: bottom CTA wording)
- [x] /design/ui/wear/guides/m2-5/surfaces/tiles-layouts → material-3-wear-os.md (Tiles)
- [x] /design/ui/wear/guides/m2-5/surfaces/tiles-principles → material-3-wear-os.md (Tiles)
- [x] /design/ui/wear/guides/m2-5/surfaces/watch-faces → material-3-wear-os.md (Surface priorities: Watch faces)

### Gaps worth distilling
1. /wear/guides/m2-5/components/swipe-to-reveal: a partial left swipe reveals a primary and an optional secondary action; "fully swipe... to quickly commit to the primary action"; "For destructive actions, add an undo component"; thresholds are under 50% snap back, 50 to 75% reveal, over 75% commit. Why: this is the standard Wear pattern for delete and archive in lists, and without it agents add trailing icon buttons or a dialog.
2. /wear/guides/m2-5/components/confirmation-overlay: "In most cases, an explicit confirmation is not needed. A visible change in the UI is enough to show that an action succeeded." Why: agents tend to show a confirmation after every action, which is noisy on a watch.
3. /wear/guides/m2-5/surfaces/tiles-principles: for ongoing activities, "Indicate that an ongoing activity is already in progress" and on tap "show the in-progress activity. Don't start a new instance"; motion: "Emphasize if you're updating information... Don't: Unexpectedly toggle between values." Why: it prevents duplicate workouts and timers and fixes tile state design.
4. /wear/guides/m2-5/surfaces/tiles-layouts: choose text-centric, button-centric (up to 5 related actions), info-centric (one key metric with a progress ring), or data-centric (graphs) by the tile's goal; drop the secondary label and chip below 225dp. Why: it gives agents a decision rule for tile composition, which the doc currently lacks.
5. /wear/guides/m2-5/surfaces/complications: glanceable and context-relevant; a tap opens a specific part of the app or does a self-contained action; "WearOS automatically includes an app shortcut complication, so you don't need to create your own"; choose a type by its required field. Why: complications are in android-dev scope, and the doc only mentions them as the P1 surface.
6. /wear/guides/m2-5/behaviors-and-patterns/sign-in: data-layer auth may come first only if it is "fully automatic" with no UI, and on failure go straight to Credential Manager "Don't alert the user"; "If sign-in fails, offer the option to skip authentication"; show secondary options together; secondary methods say you're being signed in, then confirm. Why: it shapes the whole sign-in flow of a Wear app with a phone companion.
7. /wear/guides/m2-5/surfaces/apps-principles: "Don't use both vertical and horizontal scrolling" except media playback; paginated screens only for gross-gesture use ("generally used in workout and media app UIs"). Why: it settles pager versus list on Wear screens.
8. /wear/guides/m2-5/components/time-text: time text scrolls away with list scroll; leading content (for example ETA) must keep the arc under "a quarter of the watch face". Why: it affects every scrolling app screen and the use of curved labels.
9. /wear/guides/m2-5/components/pickers: looping is the default; "Consider disabling this behaviour if order in the list is important, or to allow users to reach the first and last element with a quick swipe." Why: it is a direct parameter choice for Wear Picker.
10. /wear/guides/m2-5/foundations/design-principles: "Better together... Watches work well for quick, frequent tasks, while mobile devices are better for prolonged and complex interactions... Consider which actions are appropriate for each device." Why: it decides which features belong in :wear versus :app.
11. /wear/guides/m2-5/surfaces/media-apps: Browse and Entity pages with a flat hierarchy; downloads show destination, progress, time, and size; "remove download" shows the space used; prompt to connect a headset before playback when the watch is the source. Why: this is the core flow of any Wear media app beyond the control screen.
12. /wear/guides/m2-5/foundations/canonical-adaptive-layouts: on size-limited non-scrolling screens, "Consider the use of the rotary scroll button to control elements of the screen... as tapping interactions alone may not provide the best experience." Why: it prompts rotary input support on steppers, players, and fitness screens.
13. /wear/guides/m2-5/components/chips: "Design each screen to contain a single prominent chip for the primary action." Why: it gives a hierarchy rule for app screens (the doc only states it for tiles).
14. /wear/guides/m2-5/components/expandable-item: use a centered "Show more" chip for dense lists or text; collapse to 3 items or 8 lines. Why: it gives a concrete default for long content on a watch.
15. /wear/guides/m2-5/surfaces/tiles-design-system: the bottom CTA is "a word that's short but specific to a particular action or destination", with "More" as the fallback, about 6 characters below 225dp and no truncation. Why: it keeps tile CTAs from truncating on small screens.

## Out of scope: developer.android.com/design/ui platforms

Skipped by the owner's decision: the skill targets phones, tablets, foldables, desktop windows, widgets, and Wear OS.

### AI glasses (31 routes)

- [-] /design/ui/ai-glasses; out of scope
- [-] /design/ui/ai-glasses/guides/components/button-group; out of scope
- [-] /design/ui/ai-glasses/guides/components/buttons; out of scope
- [-] /design/ui/ai-glasses/guides/components/cards; out of scope
- [-] /design/ui/ai-glasses/guides/components/lists; out of scope
- [-] /design/ui/ai-glasses/guides/components/overview; out of scope
- [-] /design/ui/ai-glasses/guides/components/pager; out of scope
- [-] /design/ui/ai-glasses/guides/components/progress; out of scope
- [-] /design/ui/ai-glasses/guides/components/stack; out of scope
- [-] /design/ui/ai-glasses/guides/components/title-chip; out of scope
- [-] /design/ui/ai-glasses/guides/components/voice-indicator; out of scope
- [-] /design/ui/ai-glasses/guides/foundations/design-principles; out of scope
- [-] /design/ui/ai-glasses/guides/foundations/get-started; out of scope
- [-] /design/ui/ai-glasses/guides/foundations/made-for-glasses; out of scope
- [-] /design/ui/ai-glasses/guides/interaction/Glasses_userEdu_motion.aep; out of scope
- [-] /design/ui/ai-glasses/guides/interaction/ai-patterns; out of scope
- [-] /design/ui/ai-glasses/guides/interaction/audio-input; out of scope
- [-] /design/ui/ai-glasses/guides/interaction/earcons; out of scope
- [-] /design/ui/ai-glasses/guides/interaction/inputs; out of scope
- [-] /design/ui/ai-glasses/guides/interaction/navigation; out of scope
- [-] /design/ui/ai-glasses/guides/interaction/onboarding; out of scope
- [-] /design/ui/ai-glasses/guides/interaction/permissions; out of scope
- [-] /design/ui/ai-glasses/guides/styles/color; out of scope
- [-] /design/ui/ai-glasses/guides/styles/icons; out of scope
- [-] /design/ui/ai-glasses/guides/styles/overview; out of scope
- [-] /design/ui/ai-glasses/guides/styles/surfaces; out of scope
- [-] /design/ui/ai-glasses/guides/styles/type; out of scope
- [-] /design/ui/ai-glasses/guides/surfaces/ai-in-your-app; out of scope
- [-] /design/ui/ai-glasses/guides/surfaces/app; out of scope
- [-] /design/ui/ai-glasses/guides/surfaces/overview; out of scope
- [-] /design/ui/ai-glasses/guides/surfaces/sysui; out of scope

### Cars (82 routes)

- [-] /design/ui/cars; out of scope
- [-] /design/ui/cars/design-kit; out of scope
- [-] /design/ui/cars/guides/app-cuj/parked-passenger-apps; out of scope
- [-] /design/ui/cars/guides/app-types/access-location-details; out of scope
- [-] /design/ui/cars/guides/app-types/add-stop; out of scope
- [-] /design/ui/cars/guides/app-types/arrive-at-destination; out of scope
- [-] /design/ui/cars/guides/app-types/branding-elements; out of scope
- [-] /design/ui/cars/guides/app-types/browse-locations; out of scope
- [-] /design/ui/cars/guides/app-types/communications; out of scope
- [-] /design/ui/cars/guides/app-types/create-media-apps; out of scope
- [-] /design/ui/cars/guides/app-types/customize-playback-controls; out of scope
- [-] /design/ui/cars/guides/app-types/media-apps; out of scope
- [-] /design/ui/cars/guides/app-types/navigate-to-a-saved-location; out of scope
- [-] /design/ui/cars/guides/app-types/navigation-alerts; out of scope
- [-] /design/ui/cars/guides/app-types/navigation-apps; out of scope
- [-] /design/ui/cars/guides/app-types/other-apps; out of scope
- [-] /design/ui/cars/guides/app-types/parked-passenger-apps; out of scope
- [-] /design/ui/cars/guides/app-types/plan-browsing-views; out of scope
- [-] /design/ui/cars/guides/app-types/plan-navigation-tabs; out of scope
- [-] /design/ui/cars/guides/app-types/provide-recommendations; out of scope
- [-] /design/ui/cars/guides/app-types/report-incident; out of scope
- [-] /design/ui/cars/guides/app-types/resume-navigation; out of scope
- [-] /design/ui/cars/guides/app-types/search-using-past-results; out of scope
- [-] /design/ui/cars/guides/app-types/timed-alert; out of scope
- [-] /design/ui/cars/guides/app-types/view-map-in-cluster; out of scope
- [-] /design/ui/cars/guides/app-types/weather-apps; out of scope
- [-] /design/ui/cars/guides/components/action-strip; out of scope
- [-] /design/ui/cars/guides/components/banners/banners; out of scope
- [-] /design/ui/cars/guides/components/button; out of scope
- [-] /design/ui/cars/guides/components/chips/chips; out of scope
- [-] /design/ui/cars/guides/components/condensed-items/condensed-items; out of scope
- [-] /design/ui/cars/guides/components/fab; out of scope
- [-] /design/ui/cars/guides/components/header; out of scope
- [-] /design/ui/cars/guides/components/hun; out of scope
- [-] /design/ui/cars/guides/components/long-message-template; out of scope
- [-] /design/ui/cars/guides/components/map-action-strip; out of scope
- [-] /design/ui/cars/guides/components/message-template; out of scope
- [-] /design/ui/cars/guides/components/minimized-control-panel/minimized-control-panel; out of scope
- [-] /design/ui/cars/guides/components/nav-alerts; out of scope
- [-] /design/ui/cars/guides/components/overview; out of scope
- [-] /design/ui/cars/guides/components/plan-communications; out of scope
- [-] /design/ui/cars/guides/components/row; out of scope
- [-] /design/ui/cars/guides/components/section-header/section-header; out of scope
- [-] /design/ui/cars/guides/components/spotlight-section/spotlight-section; out of scope
- [-] /design/ui/cars/guides/components/tabs-switch-views; out of scope
- [-] /design/ui/cars/guides/components/toast; out of scope
- [-] /design/ui/cars/guides/components/voice-input; out of scope
- [-] /design/ui/cars/guides/flows/create-settings; out of scope
- [-] /design/ui/cars/guides/flows/create-sign-in-flow; out of scope
- [-] /design/ui/cars/guides/flows/grant-permissions-in-car; out of scope
- [-] /design/ui/cars/guides/flows/grant-permissions-on-phone; out of scope
- [-] /design/ui/cars/guides/flows/overview; out of scope
- [-] /design/ui/cars/guides/flows/purchase; out of scope
- [-] /design/ui/cars/guides/flows/sign-in-while-parked; out of scope
- [-] /design/ui/cars/guides/flows/widgets; out of scope
- [-] /design/ui/cars/guides/foundations/cal; out of scope
- [-] /design/ui/cars/guides/foundations/customize-app; out of scope
- [-] /design/ui/cars/guides/foundations/define-user-tasks; out of scope
- [-] /design/ui/cars/guides/foundations/design-principles; out of scope
- [-] /design/ui/cars/guides/foundations/design-process; out of scope
- [-] /design/ui/cars/guides/foundations/overview; out of scope
- [-] /design/ui/cars/guides/foundations/writing-guidelines; out of scope
- [-] /design/ui/cars/guides/templates/grid-template; out of scope
- [-] /design/ui/cars/guides/templates/list-template; out of scope
- [-] /design/ui/cars/guides/templates/long-message-template; out of scope
- [-] /design/ui/cars/guides/templates/map-content-template; out of scope
- [-] /design/ui/cars/guides/templates/media-playback-template; out of scope
- [-] /design/ui/cars/guides/templates/message-template; out of scope
- [-] /design/ui/cars/guides/templates/navigation-template; out of scope
- [-] /design/ui/cars/guides/templates/overview; out of scope
- [-] /design/ui/cars/guides/templates/pane-template; out of scope
- [-] /design/ui/cars/guides/templates/place-list-map-template; out of scope
- [-] /design/ui/cars/guides/templates/search-template; out of scope
- [-] /design/ui/cars/guides/templates/sectioned-item-template; out of scope
- [-] /design/ui/cars/guides/templates/sign-in-template; out of scope
- [-] /design/ui/cars/guides/templates/tab-template; out of scope
- [-] /design/ui/cars/guides/ux-requirements/adapt-parked-apps; out of scope
- [-] /design/ui/cars/guides/ux-requirements/communicate-app-by-voice; out of scope
- [-] /design/ui/cars/guides/ux-requirements/driving-state; out of scope
- [-] /design/ui/cars/guides/ux-requirements/overview; out of scope
- [-] /design/ui/cars/guides/ux-requirements/plan-task-flows; out of scope
- [-] /design/ui/cars/guides/ux-requirements/voice-actions; out of scope

### TV (20 routes)

- [-] /design/ui/tv; out of scope
- [-] /design/ui/tv/guides/components; out of scope
- [-] /design/ui/tv/guides/components/buttons; out of scope
- [-] /design/ui/tv/guides/components/cards; out of scope
- [-] /design/ui/tv/guides/components/featured-carousel; out of scope
- [-] /design/ui/tv/guides/components/immersive-list; out of scope
- [-] /design/ui/tv/guides/components/lists; out of scope
- [-] /design/ui/tv/guides/components/navigation-drawer; out of scope
- [-] /design/ui/tv/guides/components/tabs; out of scope
- [-] /design/ui/tv/guides/foundations/color-on-tv; out of scope
- [-] /design/ui/tv/guides/foundations/design-for-tv; out of scope
- [-] /design/ui/tv/guides/foundations/navigation-on-tv; out of scope
- [-] /design/ui/tv/guides/styles/color-system; out of scope
- [-] /design/ui/tv/guides/styles/focus-system; out of scope
- [-] /design/ui/tv/guides/styles/layouts; out of scope
- [-] /design/ui/tv/guides/styles/typography; out of scope
- [-] /design/ui/tv/guides/system/tv-app-icon-guidelines; out of scope
- [-] /design/ui/tv/samples/jet-fit; out of scope
- [-] /design/ui/tv/samples/jet-stream; out of scope
- [-] /design/ui/tv/samples/overview; out of scope

### XR (11 routes)

- [-] /design/ui/xr; out of scope
- [-] /design/ui/xr/guides; out of scope
- [-] /design/ui/xr/guides/3d-content; out of scope
- [-] /design/ui/xr/guides/considerations; out of scope
- [-] /design/ui/xr/guides/environments; out of scope
- [-] /design/ui/xr/guides/foundations; out of scope
- [-] /design/ui/xr/guides/get-started; out of scope
- [-] /design/ui/xr/guides/motion; out of scope
- [-] /design/ui/xr/guides/openxr; out of scope
- [-] /design/ui/xr/guides/spatial-ui; out of scope
- [-] /design/ui/xr/guides/visual-design; out of scope
