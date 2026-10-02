# Material 3 and Material 3 Expressive

Research for a Material 3 Expressive design skill for Jetpack Compose. The goal
is the equivalent of `apple-design` for Android: design judgment distilled from
Google's own sources, with concrete values and Compose APIs, so generated
screens embrace Expressive instead of looking like the stock M3 catalog.

Expressive is not a separate design language. Google calls it an evolution of
M3 ("not M4") and an "expansion pack" of opt-in APIs, so this document covers
the M3 foundation first ([Part 1](#part-1-material-3-foundation)) and the
Expressive layer on top of it ([Part 2](#part-2-the-expressive-layer)). Wear OS
lives in [material-3-wear-os.md](material-3-wear-os.md).

## Sources and status

- **m3.material.io**, full text of content version `2026-09-16_06-10-03`: every
  page under Get started, Foundations, Styles, Components, and Develop, plus the
  four Expressive articles. Pages come from the site's content API at
  `https://m3.material.io/_dsm/content/m3/<version>/<page-id>.json`; the page
  manifest is embedded in the site's Angular bundle. The 110 pre-Expressive
  blog posts (2020 to 2024) were skipped.
- **developer.android.com Wear OS design guide**, "Benefits of expressive
  design": reproduces Google's research write-up on Expressive and is used here
  for the research findings.
- **Six official Android Developers videos** (manual transcripts):
  "Build next-level UX with Material 3 Expressive" (I/O 2025, `6IsFP3gD28E`),
  "More customization in Material 3: the path to expressive apps" (Nov 2025,
  `t9rrsqfB2tM`), "Build beautiful, premium, adaptive apps with Material"
  (I/O 2026, `zRBi6oBtpoo`), "Make Material your own" (I/O 2026,
  `HbAFGivZ158`), "The future unfolds! How to optimize Android apps for
  adaptive layouts" (Jul 2026, `pLNJ-fNYTKU`), and "WearOS Material 3 shape
  morphing" (Jul 2025, `qEEo6AwgBjU`).
- **androidx sources** at commit `bf95ca5` (Sep 22, 2026), the released
  `material3-android-1.5.0-alpha28` sources jar, and Maven metadata. See
  [Compose API reference](#compose-api-reference-verified-september-2026).

The most valuable pages for the skill:

| Page | Why it matters |
| --- | --- |
| [Start building with M3 Expressive](https://m3.material.io/building-with-m3-expressive) | The seven expressive tactics and hero moments |
| [Usability, Applying M3 Expressive](https://m3.material.io/foundations/usability) | Worked example (Aura app) of emphasis decisions, with before/after |
| [Shape](https://m3.material.io/styles/shape) | Shape principles, tension, morph, optical roundness |
| [Typography](https://m3.material.io/styles/typography) | Emphasized styles, brand/plain typefaces, editorial treatments |
| [Motion physics](https://m3.material.io/styles/motion/overview) and [with Compose](https://m3.material.io/m3-expressive-motion-theming) | Spring schemes, spatial vs effects, `MotionScheme` |
| [Color roles](https://m3.material.io/styles/color/roles) and [advanced](https://m3.material.io/styles/color/advanced) | Role semantics, combining schemes, fidelity |
| [Breakpoints](https://m3.material.io/foundations/layout/breakpoints) and [scaffold](https://m3.material.io/foundations/layout/scaffold) | Adaptive layout rules |
| Component pages | Usage rules, and what Expressive replaced |

## Why the stock output looks generic

Google's own research team started from the same complaint. In 2022 a research
intern studying sentiment toward Material in Google apps triggered a team-wide
debate: "Why did all these apps look so similar? So boring? Wasn't there room
to dial up the feeling?" Expressive is their answer. Three causes show up when
comparing the guidelines with typical generated code:

1. **Stock M3 is the baseline, not the target.** Expressive is opt-in:
   `MaterialExpressiveTheme`, emphasized type styles, `MotionScheme.expressive()`,
   the shape library. Code using `MaterialTheme`, default type and shapes, and
   baseline components produces pre-2025 Material.
2. **Many default components are no longer recommended.** Medium and large top
   app bars, the bottom app bar, segmented buttons, the navigation drawer,
   baseline lists and menus, the small FAB, and the circular spinner for short
   waits were all replaced (see [the table](#components-what-expressive-replaced)).
3. **Expressive is about emphasis decisions, which tokens cannot encode.** The
   guidance is about choosing one primary goal per screen and spending emphasis
   (size, color contrast, shape, type, containment, motion) on it. Without that
   decision, every element gets equal weight.

---

# Part 1: Material 3 foundation

## Tokens and theming model

- Tokens replace hardcoded values. Three classes: **reference** (`md.ref.*`,
  every available value, like `palette.secondary90`), **system** (`md.sys.*`,
  the decisions that make a theme, such as `color.secondary-container`; this
  is where theming happens), and **component** (`md.comp.*`, like
  `fab.primary.container.color`).
- Tokens resolve per **context**: light or dark theme, contrast level, density,
  form factor, RTL. Hardcoded values break every one of these.
- In Compose, system tokens surface as `MaterialTheme.colorScheme`,
  `.typography`, `.shapes`, and `.motionScheme`.

## Color

### How the system works

- "Paint by number": UI elements are assigned **roles**; roles get colors.
- A source color (wallpaper, in-app content, or hand-picked) runs through
  Material Color Utilities, which derives five key colors (primary, secondary,
  tertiary, neutral, neutral variant), builds a tonal palette for each (tones
  0 to 100), and assigns tones to 26+ roles for light and dark themes.
- Color math uses **HCT** (hue, chroma, tone). Tone determines contrast; hue and
  chroma can change without affecting it. Don't reason in HSL.
- **Three contrast levels** (May 2025): standard, medium (at least 3:1), high
  (7:1). Using roles correctly gives these for free, including in custom
  components.
- Aug 2024: `on*Container` roles became more colorful in light theme.

### Roles

| Group | Roles | Use |
| --- | --- | --- |
| Primary | primary, onPrimary, primaryContainer, onPrimaryContainer | Most important actions and elements: FAB, high-emphasis buttons, active states |
| Secondary | secondary, onSecondary, secondaryContainer, onSecondaryContainer | Less prominent: filter chips, selected nav icon, tonal buttons, dismissive actions |
| Tertiary | tertiary, onTertiary, tertiaryContainer, onTertiaryContainer | Contrasting accents that balance primary and secondary, or heightened attention on smaller elements (badges, input fields); "at the designer's discretion... to support broader color expression" |
| Error | error, onError, errorContainer, onErrorContainer | Static in every scheme (still adapts to light and dark) |
| Surface | surface, onSurface, onSurfaceVariant, surfaceContainerLowest to surfaceContainerHighest, surfaceDim, surfaceBright | Backgrounds and large low-emphasis areas; containers for cards, sheets, dialogs |
| Outline | outline, outlineVariant | outline for important boundaries (text fields); outlineVariant for dividers and decorative lines |
| Inverse | inverseSurface, inverseOnSurface, inversePrimary | Snackbars and other reversed elements |
| Fixed (add-on) | primaryFixed, primaryFixedDim, onPrimaryFixed, onPrimaryFixedVariant (and secondary, tertiary) | Same tone in light and dark; avoid where contrast against surface is needed |

Rules:

- **Containers are fills**, never text or icon colors. **Only pair `X` with
  `onX`** (or `onSurface` / `onSurfaceVariant` on any surface). Other pairings
  break under dynamic color and contrast levels.
- Default text is `onSurface`; `onSurfaceVariant` for lower emphasis. Links use
  `primary` (or `tertiary` for less prominent), always underlined.
- `surface` for the body and `surfaceContainer` for navigation regions, the
  same mapping at every breakpoint. The five container levels create nesting
  and hierarchy on larger screens. Default mappings: low (elevated button and
  card), default (top and bottom bars), high (FAB, dialog), highest (text field
  fill, switch track).
- Don't use `outline` for dividers or multi-element containers like cards; don't
  use `outlineVariant` alone to define a tap target's boundary.
- `surfaceBright` / `surfaceDim` keep relative brightness in both themes (for
  example a dim rail beside a bright chat pane).

### Choosing a scheme

| Scheme | Choose when | Result |
| --- | --- | --- |
| Static baseline | Not ready for dynamic, enterprise, iOS | The stock purple Material look |
| Static custom brand | Product should "look like its brand" | Hand-picked sources for primary, secondary, tertiary, neutral; consistent everywhere |
| Dynamic, user-generated | Showcase personalization | Colors from the wallpaper; the app looks like every other app on that phone |
| Dynamic, content-based | Content is front and center (media, photos) | Colors from album art or images, applied near the source |

- Dynamic color can be applied selectively, for example only on a profile
  screen.
- **Combine schemes:** a base scheme (baseline, brand, or wallpaper) plus one
  content-based scheme mapped to contained areas (a media player colored by its
  album art; each activity card colored by its image). Limit a screen to **two
  scheme source types**. Keep the source image visible where its color is used.
  Don't replace semantic colors (a red error, a green success) with content
  color.
- **Static colors** (formerly custom colors): add semantic colors like Success;
  Material returns four roles (color, on, container, onContainer). Optionally
  **harmonize** them toward primary's hue; skip harmonizing for literal brand
  colors.
- **Custom baseline:** by convention, primary and tertiary are the most
  prominent, tertiary shifting hue from primary; secondary, neutral variant,
  and neutral share primary's hue with progressively less chroma.
- **Color fidelity** keeps generated tones close to the input color (otherwise a
  dark purple can come back pale). It's on by default in Theme Builder for
  custom and static colors.
- **Custom dynamic scheme:** to match the wallpaper but look more vibrant,
  define your own hue and chroma rules and generate through Material Color
  Utilities.

- A static scheme gives up personalized colors and "User-controlled contrast settings." Choose it for enterprise users "who wouldn't benefit from personalized color or user-controlled contrast settings."
- With dynamic color, "initially design it using the baseline color scheme" so the right roles are mapped to the right components. Then check the mocks across a range of source colors in Material Theme Builder. The actual colors change but "the color role mappings remain the same."
- Harmonizing has a limit, so a static color keeps its meaning: "a red color... can become cooler or warmer in hue, but will not appear purple or orange." The result varies with primary, so "check the results under a variety of schemes." In code, harmonize with the MCU `Blend` function.

### Applying content color and remapping

- Put content-based color in contained areas on top of a baseline or wallpaper foundation. Your existing app structure suggests where those areas are.
- **Build hierarchy:** when many kinds of information share a screen, use content color to draw attention to the content. Example: photo editing controls colored from the photo.
- **Link and associate:** "In lists and collections of repeated items that benefit from differentiation, content-based color can help associate related elements," such as a list item and its action. Example: "each card is colored with a scheme sourced from its main image."
- **Immerse:** "Full-screen content-based color moments can orient users within a content-driven experience, such as a media control or a purchase flow." Example: a whole media screen colored from its album art.
- "Avoid applying content-based color in spaces where the content itself isn't visible."
- **Remapping** a component, or coloring a custom component: pick the role by how the color is used (background to `surface`, text and icons to `onSurface`).
  - Only `on-` pairs are guaranteed to contrast. Other pairs may miss 4.5:1 (small text) and 3:1 (large text).
  - Under dynamic color, test the result in light and dark and under red, yellow, green and blue schemes.
- "Always apply color roles rather than static values or tonal palette values." Those break light and dark, contrast control and other features. If no role fits, define new colors or adjust existing ones.

### Custom color roles

- Use one only when the standard roles and static colors don't meet your needs. "Defining custom color roles should be considered only if you cannot achieve your desired colors with other Material color solutions."
- Define it the way Material does:
  - **Palette and reference tone:** a palette (primary, secondary, tertiary, neutral, neutralVariant, error) and a tone (for example primary70) for both light and dark.
  - **Pairings:** which colors are used together as foreground and background, or must keep a tone delta.
  - **Contrast:** confirm each pairing meets Material's minimums.
- Then add it to your own dynamic color object and generate its value with Material Color Utilities. It then follows user theming and contrast level.
- Google's example: "primary graphic" is primary palette tone 50, used as a large weather icon on `primaryContainer` at 3:1.

### Contrast for custom colors (HCT tone rule)

- HCT turns contrast ratios into a tone difference. For WCAG, "smaller elements (less than ¼" or 40 dp) require a tone difference of 50 with their background, larger elements require a tone difference of 40." "This principle works consistently for any pair of colors."
- Use it for colors outside the scheme roles: brand accents, chart series, widget art, category colors.
- Android guides: "avoid pairing colors with similar tones," and red and green are common patterns but "not accessible to users with certain kinds of color blindness."

### Data visualization (accessible charts)

From the M3 blog "Top Tips for Data Accessibility":
- **Compare:** "Keep comparable data sets on the same scales and measures where possible." Pick a chart type that separates data sets (line charts show small changes over continuous time). Offer filtering to find outliers.
- **Familiar types:** "Use common charts, such as area charts, bar charts, donut charts, line charts."
- **Color:** use colors with required contrast against adjacent elements (background, metrics, interaction states), "along with an additional element or encoding" to carry meaning. Encodings must represent the data accurately.
- **Summary:** give a summary of what the chart conveys and refresh it after interaction such as filtering. Don't just narrate the visual elements.
- **Orientation:** show data recency and collection method, and keep patterns consistent across charts.
- **Labels:** "Provide labels for legends, data points, axes, and marks."
- **Interactive charts:** describe the data organization for assistive technology, support keyboard navigation with few tab stops, and allow sorting.
- **Underlying data:** link to it, for example a downloadable CSV or an accessible table.
- **Options:** consider screen sizes and data types. Where appropriate, let people choose the chart's theme, contrast and density.

## Typography

- **Roles:** Display, Headline, Title, Body, Label, each Large / Medium /
  Small. Display: short, important text or numerals, best on large screens.
  Headline: short high-emphasis text on small screens. Title: medium emphasis,
  secondary regions. Body: long passages. Label: text inside components (buttons
  use Label Large), captions.
- Default scale: Major Second (1.125) anchored at 14. Aim for "impactful
  contrast between sizes by avoiding small differences." Products rarely need all
  15 styles; a reduced set of about 5 is normal.
- **Brand and plain typefaces:** brand for large styles (Display, Headline),
  plain for small ones (Body, Label). Roboto is the default for both. "Consider
  replacing Roboto with different typefaces to boost brand expression." Display
  can take an expressive face (Google's examples: Bagel Fat One, Anton);
  Headline can with adjusted line height and tracking; be cautious at Title;
  never on Body or Label.
- Line height about **1.2x** for Title, Headline, Display and about **1.5x** for
  Body and Label. Heavier fonts may need wider tracking.
- **Tabular numbers** wherever values change (clocks, timers, tables).
- Keep text at **40 to 60 characters per line** across breakpoints.
- Variable fonts: Roboto Flex (width, weight, grade, slant, optical size, plus
  parametric axes), Roboto Serif, Roboto Mono; Google Sans Flex has six axes.
  Fallback order: Roboto Flex, Roboto, Noto Sans.
- Language height (Aug 2026): line heights adapt to script height (small,
  medium about 7% taller, large about 30%, extra large about 100%). Default to
  medium.
- Fonts are `sp` on Android. Support **200% text scaling**: text and line
  height scale, padding and spacing stay fixed, layouts reflow (stack
  side-by-side buttons, allow vertical scrolling).

### Customizing type styles

- Change the brand and plain typeface tokens, then tune line height and letter spacing. "Avoid changing the type size; this can affect how components render and reflow."
- Repeat for the emphasized set. "Try to keep emphasized styles visually consistent, like all wider than baseline."
- Heavier fonts need wider letter spacing. Fonts with long ascenders or descenders need different line heights. Customizing the scale opts you out of Material's typography token updates.
- Android letter spacing is in em: tracking (px) / font size (sp). For example, 0.2 / 16sp = 0.0125em.
- Language height:
  - Components that use vertical padding adapt to it automatically.
  - "Components with fixed heights are built for small values and may not adapt by default."
  - Ignoring language height causes overlapping text and broken UI.
- Typesetting: on Android, specify vertical distances to the text baseline, not to bounding boxes ("Android screens rely on distance to baselines for spacing"). Line height is measured baseline to baseline. Bounding boxes are the web and iOS method.
- Grade: at the default grade 0, light and dark modes read equally well (Material readability study), so grade compensation between modes is optional. The minimum grade measurably slowed reading in light mode. Use it only where brand goals justify the cost.

### Text resizing and truncation

- "When designing for text resizing, don't resize components without text." Icons, checkboxes, radio buttons, and progress indicators stay 1x. Only text and line height scale. Padding and gaps stay 1x (the spacing guide says the same: "the same spacing should be preserved by default").
- If the OS doesn't control text size, offer in-app multipliers such as 1.5x or 2x (default size × scale).
- Fixes, in order:
  1. Grow the container.
  2. Reflow (stack side-by-side items).
  3. Scroll. "Users should only be asked to scroll in one direction," and vertical is preferred.
  4. Tooltip. In the top app bar, nav bar, nav rail, and tabs fixed to the top, keep the label at 1x and show the enlarged label in a touch-and-hold tooltip.
- Truncation: "Information should always be available to readers, even if text is truncated or wrapped." Content must survive larger text, wider spacing, and longer translations (non-Latin scripts may be exceptions).
  - Wrap text that is critical, or when the component has room. If it still doesn't fit, "provide a way for users to see more."
  - Use flexible containers that grow to fit content. Don't impose text limits that leave space unused.
  - An ellipsis is acceptable only when the full text is available through a tooltip, or through a link or expanded view that shows it. "If there's an ellipsis, but no way to show the truncated text, it is not accessible." Never truncate a checkbox label in a multi-select list without another way to read it.

## Shape

Corner radius scale (10 steps; the three marked Expressive were added May 2025):

| Token | Value | Typical use |
| --- | --- | --- |
| None | 0dp | |
| Extra small | 4dp | Chips, snackbars |
| Small | 8dp | Text fields, menus |
| Medium | 12dp | Cards |
| Large | 16dp | FABs, drawers |
| Large increased | 20dp (Expressive) | |
| Extra large | 28dp | Dialogs, bottom sheets |
| Extra large increased | 32dp (Expressive) | |
| Extra extra large | 48dp (Expressive) | |
| Full | fully rounded | Buttons, badges |

- Customize at the **style** level (change what `medium` means everywhere) or the
  **component** level (remap buttons from `full` to `medium`). Families can be
  rounded or cut.
- **Optical roundness:** nested radius = outer radius minus padding (48dp minus
  14dp = 34dp). Never reuse the outer radius inside.
- **Inner corners:** closely grouped items (menus, split buttons, connected
  button groups, segmented lists) use small inner and large outer corners.
- Don't put large or full corners on information-dense components like cards.
- Expressive shape guidance is in [Part 2](#shape-expressive).

## Elevation

- Six levels: 0 (0dp), 1 (1dp), 2 (3dp), 3 (6dp), 4 (8dp), 5 (12dp). Resting
  states use 0 to 3; 4 and 5 are for hover and drag. Hover raises by one level.
- M3 shows elevation with **tonal surface differences** by default. Use visible
  **shadows only** to protect elements over busy backgrounds or to invite
  interaction (lift on drag). "The fewer levels in your UI, the more power they
  have." Don't change components' default elevation.
- Overlapping containers must use different surface roles.
- **Scrims** use the `scrim` role at 32% opacity behind modals and expanded
  navigation.
- Resting levels: 3 for FAB, extended FAB, dialogs, pickers, search; 2 for
  scrolled app bar, menus, nav bar, toolbars, rich tooltips; 1 for modal sheets,
  elevated buttons and cards, banners; 0 for most everything else (filled,
  tonal, outlined buttons; filled and outlined cards; lists; rails; tabs).
- Surface tint is deprecated.

## Icons

- **Material Symbols** (variable font) in outlined, rounded, or sharp. Pick the
  style that echoes the brand: rounded with heavy type and curved shapes, sharp
  with rectangular details, outlined for dense UI.
- Axes: **weight** (minimum 200 at 24dp; apply consistently, never mix weights
  in one set), **fill** (0 to 1; use filled for selected states), **grade**
  (-25 for light icons on dark backgrounds; positive grade for active states),
  **optical size** (20 to 48dp; 40 to 48dp to highlight primary actions).
- Match icon size and optical weight to adjacent text; shift the icon baseline
  down about 11.5% of the text size.
- 24dp icons get 48dp touch targets. Icons below 20dp that are complex or key
  actions need a text label.
- Toggle icon buttons: outlined when unselected, filled when selected (semibold
  if no filled version exists).

### Applying icons (additions)

- Some symbols "should remain filled, such as full body human icons or proprietary icons," even in an outlined set.
- Match icon grade to text grade when the text font has a grade axis ("if the text font has a -25 grade value, the symbols can match it").
- Size:
  - Standard icons are 24dp. Extra optical sizes: 20dp for desktop and dense layouts, 40 and 48dp next to display or headline type and on larger screens.
  - "When a mouse and keyboard are the primary input methods... A 20dp size symbol can use a target size of 40dp."
- Labels:
  - "Navigation items must have labels for clarity and accessibility."
  - Label abstract icons.
  - Leave labels off only where reduced visual impact is truly needed, and only if the meaning is unambiguous.
- Localize icons: checkout may be a cart, bag, or basket depending on locale. Color and symbol meanings vary by culture (white means mourning in some eastern cultures; red or green can signal warning).

### Custom icons (only when drawing new vectors)

- 24dp grid. Content stays in the 20×20dp live area with 2dp of padding. Content can extend into the padding for weight, but never outside the 24dp trim area.
- Keylines: square 18dp, circle 20dp diameter, rectangles 20×16dp (vertical or horizontal). Place shapes on whole-pixel coordinates.
- Corners:
  - Outlined: 2dp exterior radii, square interior corners, no rounding on strokes 2dp wide or less.
  - Rounded: exterior and interior corners both rounded.
  - Sharp: 0dp corners.
- Stroke: 2dp (weight 400), consistent throughout, with squared stroke terminals. Complex icons can drop to 1.5dp as an optical correction.
- "Make icons face forward." No tilt, isometric view, or 3D. Simplify, "Don't be overly literal." Use geometric shapes, not loose organic ones. Keep one style per set.

## Layout

### Breakpoints

| Breakpoint | Width | Panes | Navigation | Margins |
| --- | --- | --- | --- | --- |
| Compact | under 600dp | 1 | Navigation bar or modal expanded rail | 16dp |
| Medium | 600 to 839dp | 1 (recommended) or 2 | Navigation bar (2 panes) or collapsed rail (1 pane) | 24dp, 24dp spacer |
| Expanded | 840 to 1199dp | 2 (recommended) | Collapsed or expanded rail | 24dp, 24dp spacer |
| Large | 1200 to 1599dp | 2 (recommended) | Rail, collapsed or expanded | 24dp, 24dp spacer |
| Extra large | 1600dp+ | 1 to 3 | Expanded rail | 24dp, 24dp spacer |

- Design for breakpoints, not devices. Height breakpoints exist but are rarely
  needed.
- Moving up a breakpoint, ask: **reveal** (show the expanded rail, a second
  pane), **divide** (panes), **resize** (cards, feeds, lists, keep 40 to 60
  characters per line), **reposition** (move actions from bottom to leading
  edge, add negative space), **swap** (nav bar to rail, bottom sheet to menu,
  full-screen dialog to basic dialog). Only swap functionally equivalent
  components; "Additional space doesn't just mean making the same thing
  bigger."
- Medium two-pane layouts: only for low-density content, 50/50 widths.
- Fixed-and-flexible layouts: fixed pane 360dp (expanded) or 412dp (large and
  up). Split-pane keeps the spacer visually centered even with a rail.
- Reachability on tablets in landscape: avoid interactions in the top 25%.

### Scaffold

- **Bars** (app bar at top, navigation bar at bottom) frame the page beside the
  safety regions (system UI). **Rails** surround panes: on compact, the rail
  region above the nav bar holds toolbars, chat inputs, FABs; on large screens
  the leading rail holds the navigation rail and the trailing rail holds
  supporting controls. **Panes** hold all content, 1 to 3 of them, at least one
  flexible.
- Panes display as **co-planar** (side by side), **floating** (dialog-like), or
  **docked** (bottom sheet). They adapt by **show and hide**, **levitate**
  (become floating or docked), or **reflow** (stack vertically).
- **Canonical layouts:** **feed** (card grid, 1 column compact, more columns up),
  **list-detail** (1 pane compact, 2 panes expanded; selection state only in
  two-pane, back button only in single-pane; keep scroll position), and
  **supporting pane** (primary about two-thirds; below the focus pane on compact
  and medium, beside it at 360dp on expanded).
- Containment: panes can blend with the background (implicit grouping) or use
  color and outlines (explicit). In multi-pane layouts, use color to show
  emphasis.

### Spacing

- 8dp system (`space100` = 8dp; Compose only); nested units like 2, 4, 6, 10dp
  exist. Prefer **padding and gaps** on the parent over margins on children.
- Spacing groups (explicit via containers and outlines, implicit via
  proximity), directs attention (rhythm, similarity, proximity, continuity),
  and sets personality: "A denser layout can feel more serious and focused,
  while a more spacious layout can feel calm and open."
- Density is a user choice, not an automatic breakpoint change. Never raise
  density on focused tasks (menus) or alerts (snackbars, dialogs). Component
  density drops 4dp per step.

- Apply system spacing tokens to custom components and layouts instead of hardcoded values. Spacing logic lives in each component, not in a global theme switch.
- Extending the system:
  - New tokens follow the multiplier: "space225 = 18dp (8dp x 2.25)."
  - Tokenize product-wide adaptive patterns. For example, if cards and sheets adapt horizontal padding the same way, create one "surface content horizontal padding" token.
  - To change a component everywhere, remap its token (for example, button top padding from space125 to space200).
- Adaptive spacing:
  - Adaptive layout: "Map the spacing to different system tokens for each device type, such as mobile or desktop."
  - Density: "Adapt vertical padding to different spacing values for each setting."
- At 200% text, keep spacing unchanged.

### Panes in depth

- "All content must be in a pane." A pane is "a single destination in the product" (for example, the message list is one pane and a conversation thread is another). In Compose, Navigation 3 shows several destinations on screen at once.
- **Pane totals:** compact 1; medium 1 (2 allowed); expanded and large 2 (1 allowed); extra-large 2 (1 or 3 allowed). Every layout needs at least one flexible pane.
- **Permanent vs temporary:** permanent panes sit side by side. Temporary panes appear and are dismissed as needed, and that changes the size of the other panes. When a temporary pane closes, the remaining pane fills the space.
- **Split-pane:** best for foldables and dynamic layouts. A rail or drawer shrinks only the leading pane, so the other pane stays at 50% of the window width. With a navigation bar or no navigation, each pane gets 50%.
- **Fixed-and-flexible:** panes can go in whichever order suits the content. The fixed pane is often temporary, used for side sheets or lists with light information density.
- **Three panes (extra-large only):** use a standard side sheet as the third pane (default max width 400dp, while fixed panes are 412dp). While the sheet is open, the rail can stay, collapse, or hide. "Don't use more than three panes."
- **Drag handles:** in a split-pane, both flexible panes resize freely or snap to widths. In fixed-and-flexible, the handle fully collapses and expands the fixed pane, which switches between one and two panes. The handle "should also toggle between layout sizes when selected" (tap, double tap, or long press). At expanded and larger breakpoints, snap targets are "360dp, 412dp, Split-pane with spacer centered visually".
- **Persistent resizing:** "remembers a person's pane width preference. Use this for most resizable layouts." The width survives app restarts and breakpoint changes. A two-pane layout collapsed to one pane stays collapsed after rotation or a breakpoint change.
- **Temporary resizing:** mainly for supporting-pane layouts, where resizing is rare. Panes "should always return to the default layout after the pane or product is closed and reopened."
- **Placement:**
  - Co-planar: "persistent utilities like tool panels should be co-planar with primary content."
  - Floating: "Temporary tasks should remain floating regardless of breakpoint, such as a dialog." Floating panes can be made draggable or resizable, and then need accessible controls for moving and resizing. On large screens floating is the default, and the scrim is optional.
  - Docked: usually at the bottom, like a bottom sheet. At medium and expanded it can become floating or co-planar. "On large screens, consider changing docked panes into co-planar panes."
- **Reflow:** in vertical orientation, or when horizontal space runs out, a supporting pane moves below the primary pane.
- **Focus and accessibility:**
  - Co-planar and docked panes: "The focus order should match the visual arrangement of the panes on screen."
  - Modal floating pane: elements behind it can't be interacted with. Focus "moves automatically to the first element in the pane, and when the pane is closed, focus moves back to the element that triggered it, like a dialog." If the pane opened automatically, focus still moves into it, and on close it goes "to the next most logical element on screen."
  - Non-modal floating pane: the rest of the product stays interactive, focus can move into and out of the pane, and the pane sits "in a logical reading order of the screen."
  - Docked panes follow the same rules as modal and non-modal panes.

### Breakpoint-specific rules

- **Compact to larger transitions:** a layout must re-adapt dynamically on fold and unfold, rotation, entering or exiting split-screen, multi-window resizing, and free-form window resizing.
- **Single-pane immersive:** at any breakpoint, one pane can focus attention on "Playing a game, Watching a movie, Video calls, Creative applications." At expanded and larger, use a single pane only for "visually- or information-dense content, such as videos."
- **Medium:**
  - "Avoid setting custom widths" for two panes: they are 50/50, and a drag handle can take one pane to 100%.
  - Use a navigation bar with two panes (so the panes get the full width) and a rail with one pane.
  - Don't use two panes for high-density content.
  - For list-detail, use a single pane for dense content or deep focus, and two panes to browse and switch items quickly. With two panes, use a bottom nav bar or a modal rail to maximize width.
- **Rail visibility:** at medium and expanded, "The navigation rail can be hidden in secondary destinations as long as the primary destination can still be accessed using a back button." At large and extra-large, "Consider collapsing the navigation rail when space is needed, or when on pages deeper in the page hierarchy." The expanded rail suits extra-large best.
- **Secondary navigation:** at expanded and up, "For sorting, filtering, or secondary navigation, use tabs or other components directly in the pane" (not in the rail).
- **Ergonomics (tablets, unfolded foldables):** limit interactions in the top 25%. "Avoid placing essential interactive elements too close to the bottom edge of the screen." There are three regions: top inconvenient (reached by extending fingers), middle comfortable, bottom challenging.
- **Large and extra-large:** "Some products may not need large and extra-large breakpoints." Watch line length for readability.
- **Swaps:** "Don't swap a button for a chip. Be careful when changing between list items and cards." Don't swap a button for a menu. The common swaps are: nav bar to collapsed rail; modal expanded rail to standard expanded rail; basic or full-screen dialog to basic dialog; bottom sheet to menu for supplemental selection.
- **Anchoring:** as a container scales, internal elements anchor left, right, or center, or keep a fixed position (a FAB in a rail). A button's icon and label stay anchored together and centered as the container widens.

### Canonical layouts: behavior rules

- **Supporting pane vs list-detail:**
  - "Use the supporting pane layout when the secondary content is only meaningful in relation to the primary content. For content with a parent-child relationship, use a list-detail layout instead."
  - Supporting pane use cases: productivity, document editing and commenting, content and media browsing.
  - On compact, "A bottom sheet can be useful for keeping focus on the primary pane while providing access to supporting information."
  - The supporting pane sits below the focus pane (flexible width) on compact and medium, and on the leading or trailing side (fixed 360dp) on expanded.
- **List-detail transitions:**
  - No selection: a single pane shows the list, and two panes show "placeholder content in the detail pane" (an empty state).
  - Multi-select and similar cases: "the most recently used pane should stay visible" when going to one pane.
  - Single to two panes with a selection: show both panes with the selected item's details.
  - Two panes to one: typically show the detail with an app bar. With selection that has no deep navigation (multi-select), show the list with the item selected.
  - "Consistency is key: If a layout showed the list view previously, it should return to that view when returning to a single pane."
  - Save state between detail views, "This includes read and unread content", and keep scroll position.
  - Two-pane focus: use explicit and implicit grouping to direct visual focus.
- **Feed:**
  - "Use size and position to establish relationships among content elements" (mix small and large cards).
  - Items reflow on rotate, unfold, and entering multi-window. "The order of items is determined by their position."
  - On compact, cards stack vertically, each filling the pane width. Column count usually increases at expanded.
- **Custom layouts:** build on a canonical layout, or layer panes using the levitate strategy for focused tasks ("Reviewing a shopping basket, Responding to comments, Creating a calendar event").

### Adapting components

Components adapt by one of three strategies:

- **Resizing:** "buttons may scale along with their parent container, or hug their contents and maintain a left or right alignment."
- **Showing and hiding:** "list items may reveal descriptions or other additional information as their parent container scales." Components can also collapse or expand.
- **Presentation changes:** orientation, color, type, and shape can change, and so can the configuration: "a FAB can change to an extended FAB, and navigation rails can be automatically expanded" as the window grows (and back to a FAB when it shrinks).

Also:

- Size constraints: most components have min and max dimensions. On small screens a snackbar grows upward, and on large screens it grows horizontally to stay on one line. Dialogs widen to a readable max width, then grow vertically up to a max height (source: the 2021 blog).
- Window modes on mobile: full-screen (default), split-screen, and bubbles. Desktop uses free-form windows that "should adapt to various screen sizes."
- A tablet becomes a desktop experience with a keyboard and mouse, and a phone does when it connects to an external monitor. Design every product for touch, pointer, and keyboard.

### Grids and rulers

- Column count, width, and spacing grow with the breakpoint (fewer columns on compact for focus, more when unfolded).
- Order of placement: first the scaffold regions nearest the usable edges (bars, nav bar and rail, toolbars, app bars), then panes.
- Rulers are global alignment lines (Compose Rulers API) that keep margins and placement consistent across screens:
  - Bar and safety rulers reserve space for the status bar and gesture navigation, so app bars aren't covered.
  - The title ruler aligns the app bar title, icons, and components.
  - The first content ruler anchors hero images, headlines, and primary components. Secondary rulers set where supplementary text or actions begin.
  - Margin rulers can shift narrower or wider to add or remove negative space. "A photo grid can take the full width of the screen, while components like search use wider margins."

### Spacing and density details

- "Leading elements like thumbnails, avatars, or icons should always be aligned."
- Similar items use identical sizes: basket thumbnails match "even if the original photos have different aspect ratios."
- Cards "should maintain consistent horizontal spacing" when their heights vary.
- Buttons go "close to the content they're affecting." A row of chips reads as one control.
- "Desktop layouts can use more generous spacing than mobile layouts."
- Density is contextual: someone may want dense on desktop but not on mobile. It "shouldn't automatically change across breakpoints or orientation unless a person changes it." High density suits data-rich products ("News, financial portals, dashboards"). Lower density suits aesthetic or focused products.
- Component scaling:
  - People "opt in to dense layouts and components."
  - The density setting itself must use 48x48 targets so it can be reverted. "Don't scale layouts below 48x48dp by default."
  - Center the grouped element in the container.
  - "Text size shouldn't change as the container size scales."
  - An icon can be smaller than its target, but the target stays at least 48x48dp.

### Bidirectionality (RTL)

- Canonical layouts (list-detail, feed, supporting pane) mirror, and so do navigation.
- Nav rails and expanded rails sit on the leading edge (right in RTL).
- Mirror directional icons ("back and forward", send, arrows in app bars). Help icons mirror in some languages (Urdu, Persian).
- Do not mirror:
  - "Clock icons, circular refresh icons, and progress indicators with arrows pointing clockwise." Clocks still turn clockwise. In 12h clocks, AM/PM sits on the left.
  - "Media controls for video or audio players are always LTR."
  - Graphs and charts stay LTR for Persian and Urdu.
  - Hebrew keeps timelines, media controls, and linear progress LTR (linear progress runs right to left in most other RTL languages). Circular progress never mirrors.
- Text needs both RTL alignment and RTL directionality, or word order scrambles. Emails keep username before domain ("The domain should always be to the right of the username"). Watch cursor position, punctuation, phone numbers, and URLs.
- Swipe actions mirror (a delete revealed from the right in LTR is revealed from the left in RTL), and so does predictive back.

## Interaction

- **States:** enabled, disabled, hover, focused, pressed, dragged. State layers
  use the content (`on*`) color at hover 8%, focus 10%, press 10%, drag 16%.
  State layer 40dp inside a 48dp target. Only one hover, focus, press, or drag
  at a time. Disabled needs no contrast; a FAB whose action is unavailable
  should disappear rather than disable.
- **Selection:** check marks, checkboxes, or a surface color change; nav bar,
  rail, and tabs use an active indicator. Enter selection mode with long press
  (or tapping an avatar); long press plus drag for batch selection.
- **Gestures:** tap, double tap, long press, scroll, swipe (peer views or
  actions), drag, pick up and move, pinch. **Predictive back** is supported on
  bottom sheets, nav bar, nav rail, search, and side sheets.
- **Inputs:** support mouse, trackpad, and keyboard on every form factor: hover
  states, context menus on secondary click, Enter to send, Space to play,
  Escape to dismiss, logical Tab order.

### Applying states

- "States have two visual indicators to ensure accessibility." States combine: selected plus hover, focus, or pressed (for example, a selected filter chip that is also hovered). "Apply states consistently across components."
- A state layer can cover the whole component, one element inside it, or a circular shape over part of it.
- **Containers never take interaction states. Only their actionable children do.** "The individual components that are actionable within the app bar inherit hover states, not the whole app bar." The same rule covers focus and pressed.
  - No hover: app bars, badges, dialogs, menus, nav bar, drawer, and rail, sheets, tabs.
  - No pressed: app bars, badges, bottom navigation, dialogs, menus, sheets, tabs.
  - No dragged: app bars, badges, buttons, dialogs, menus, nav bar, drawer, and rail. "Components like an app bar that require consistent placement should not inherit dragged states."
- **Disabled** is shown by color changes and reduced elevation. "Disabled components can't be focused, dragged, or pressed, and they don't change state when tapped or hovered over." A layout can have any number of disabled components.
  - Takes disabled: buttons, cards, checkboxes, chips, list items, radio buttons, switches, text fields.
  - Never disabled: app bars, badges, dialogs, FABs, menus, nav bar, drawer, and rail, sheets, tabs, tooltips. Hide an unavailable FAB instead.
- **Hover** uses a lower-emphasis overlay. It "appear[s] and disappear[s] using a low-emphasis animated fade." Hover should also open tooltips where they apply.
- **Focused** uses a higher-emphasis overlay. A keyboard-focused element also shows a ring-like keyboard focus indicator.
- **Pressed** is high emphasis and shown with a ripple. "Some components, such as buttons or cards, can inherit elevation to signify a pressed state."
- **Dragged** uses a low-emphasis overlay "to avoid distracting users from their task." It applies only to cards, chips, list items, and sliders. List items, chips, and cards can also take elevation while dragged.

### Selection mode

- Enter by long press, or by a shortcut such as tapping the item's avatar. Add items by tapping them. Exit by deselecting every item "or tap an action on the toolbar."
- Long press plus drag selects in batches. "Don't use this gesture combination if it is already in use to pick up and move items, like cards."
- Nav bar, drawer, rail, and tabs show the current item with an active indicator. Only one item is selected at a time.
- Desktop (click): "checkboxes are always visible when selection is the primary activity." When selection is secondary, show a checkbox on the hovered item, and show checkboxes on all items once one is selected.

### Pointer, trackpad, stylus, and keyboard

- Show a cursor whenever a mouse is connected, on any device type. A primary click or stylus tap gives the same feedback as touch (the pressed ripple). A secondary click opens a context menu.
- Cursor shapes:
  - Pointer by default.
  - Hand on links and clickable images.
  - Resize arrows on the edges of resizable elements.
  - I-beam over text: single click places the cursor, double click selects a word, triple click selects a paragraph.
- Text selection by mouse, trackpad, or stylus:
  - "Highlight the selected area using a single color."
  - "Don't show touch controls next to the highlighted area." Show the I-beam and a right-click context menu instead, even on a touch device.
  - Touch selection always shows touch handles, "even if other inputs are connected."
  - In a text area, touch drag scrolls and mouse drag selects.
- A stylus needs no cursor unless the cursor shows tool properties such as brush size or shape.
- The wheel and two-finger trackpad scroll only the pane under the cursor. Horizontally scrolling content (carousels) must scroll with the wheel and with a two-finger horizontal gesture. Trackpad pinch zooms.
- Physical keyboard:
  - Users must be able to do everything the virtual keyboard allows, "and more."
  - "When a physical keyboard is attached, hide the virtual keyboard." Show it again when the keyboard is removed.
  - Tab focus follows a logical order, usually left to right, top to bottom.
  - Escape dismisses menus, dialogs, and bottom sheets, and removes visible focus indicators. Escape "should remove the text cursor when typing, but should not remove already-typed text."

## Motion foundation: transitions

Transitions still use the legacy easing and duration system (the springs in
[Part 2](#motion) drive components).

- **Six patterns:** container transform (strongest relationship, "perceived to
  be the most expressive"; cards, list items, FABs, search), forward and
  backward (Android: fade while sliding), lateral (peers slide in unison, no
  fade), top level (fade out then in, no shared elements), enter and exit
  (Android components expand along x or y, away from the nearest edge, not
  scale), skeleton loaders.
- Good transitions: respect reduced motion (fades, no parallax or morphing),
  stay consistent, keep layouts stable, avoid jump cuts (unless efficiency
  matters most), build a coherent spatial model, move in a **unified direction**
  (only hero elements persist), use **clean fades** (fade out fully before
  fading in; never fade a bottom sheet), and keep a **simple style** ("not
  receptive to highly stylized motion").
- Easing: Emphasized for most transitions (Standard for small utility ones);
  Emphasized decelerate to enter, Emphasized accelerate to exit permanently.
  Duration scales with the area covered; exits are shorter than enters.

| Easing | Cubic bezier |
| --- | --- |
| Emphasized | 0.2, 0, 0, 1 (Compose `EasingEmphasizedCubicBezier`, a single-curve approximation; the Android spec is a two-segment path: `M 0,0 C 0.05, 0, 0.133333, 0.06, 0.166666, 0.4 C 0.208333, 0.82, 0.25, 1, 1, 1`) |
| Emphasized decelerate | 0.05, 0.7, 0.1, 1 |
| Emphasized accelerate | 0.3, 0, 0.8, 0.15 |
| Standard | 0.2, 0, 0, 1 |
| Standard decelerate | 0, 0, 0, 1 |
| Standard accelerate | 0.3, 0, 1, 1 |

### Choosing a transition pattern

- **Container transform:** "the most dramatic pattern in terms of style and should be reserved for the right context." Use it for "Hero moments that should be expressive", for "Shallow hierarchies where you expand an element for more detail then collapse it", and for "Creating a seamless connection between elements".
  - "Don't use container transform in apps with deep hierarchies, the motion becomes excessive. The expressive style also doesn't fit this utility focused navigation" (for example, a settings list item opening a detail screen).
  - Use it for hero moments instead of forward and backward: "Don't use forward and backward transitions on hero moments like opening a photo memory."
  - Research: in a study of a grid of menu cards, a clear majority of participants preferred container transform over seven other transitions and a jump cut. It was the only one felt as warm and "high end". Participants who preferred a jump cut or fade through called them "efficient" and "quick".
- **Forward and backward:** "Both Android and iOS should use platform defaults for forward and backward navigation. It's easy to implement and stays current as platforms update." Caution: container transform "require[s] custom implementations and the motion may feel excessive when used frequently", so don't use it for everyday hierarchical navigation.
- **Lateral:** for browsing peer content in the same set (for example, tabs in a media library). Sliding hints that the content area can be swiped.
  - Caution: "Fading content as it slides makes the peer relationship and swipe gesture less obvious", and it can be confused with forward and backward.
  - "Don't use a Lateral transition for navigating hierarchical screens." A full-width slide is excessive for a high-frequency transition and implies a peer relationship the screens don't have.
- **Top level:** tapping a navigation bar, rail, or drawer uses a quick fade. "Don't use a lateral transition to move between top level destinations. The gesture conflicts with carousel and list item gestures."
- **Enter and exit:** introduces a component in the context of the screen's main UI. It can be modal (a dialog) or let both regions be used at once (a standard bottom sheet over a map). "Don't use an enter and exit pattern for navigating hierarchical screens": a full-height slide is excessive and leaves the relationship between screens unclear (for example, a card sliding up into a full screen).

### Pattern details

- **Skeleton loaders:** they hint where content will appear and are combined with other transitions to "reduce perceived latency and stabilize layouts."
  - They pulse subtly to show indeterminate progress. The pulse "starts at the top left of the screen and moves down to the bottom right."
  - "Once content is loaded, it quickly fades in on top of the skeleton loader."
  - Don't let content pop in or shift position as it loads.
- **Enter and exit direction:** a component expands away from the nearest device edge. "A menu at the top of the screen expands downwards, and a snackbar at the bottom of the screen expands upwards."
  - Beyond the screen bounds, Android components (app bars, banners, nav bar, rail, drawer, sheets) expand or collapse along x or y as they slide. This "emphasizes their shape."
- **Entry location builds the spatial model:** a notification enters from the top because the shade pulls down from there. A nav drawer enters from the left, so users know where it lives off screen. "A bottom sheet and the keyboard enters from the bottom of the screen", which is "a sensible default location for sheets to enter since the bottom of the screen is easiest to reach."
- **Scroll-driven:** a top app bar and a navigation bar can slide off and back on screen during a scroll, which leaves more room for browsing.
- **Coplanar side sheet:** a side sheet can enter and exit "at the same elevation as the main content". "Coplanar sheets shrink the available area for content" instead of overlaying it.
- **Cross fades:** if one is unavoidable, "keep it quick and hide it during the fastest part of the transition." A component fading in over content (for example, a centered dialog) uses a short fade duration.

### Easing and duration defaults

Google calls the easing and duration system "no longer maintained", but transitions still use it. Components use springs.

| Easing | Duration | Transition type |
| --- | --- | --- |
| Emphasized | 500ms | Begin and end on screen |
| Emphasized decelerate | 400ms | Enter the screen |
| Emphasized accelerate | 200ms | Exit the screen |
| Standard | 300ms | Begin and end on screen |
| Standard decelerate | 250ms | Enter the screen |
| Standard accelerate | 200ms | Exit the screen |

- **Exit the screen temporarily** (for example, a drawer that can be reopened) uses **Emphasized**, not accelerate: "By ending at rest just off screen, it gives the impression the exiting component can be retrieved." Emphasized accelerate is only for permanent exits, where ending at peak velocity suggests the component "cannot be retrieved."
- Standard is for "small utility focused transitions that need to be quick" (for example, a text field). It is also the fallback where Emphasized isn't supported (web, iOS).
- Avoid durations so short they become jarring. Examples: small-area selection controls use 200ms with Standard. An enter at 500ms pairs with an exit at 200ms (dialog, bottom sheet).
- Duration tokens, by use:

| Group | Values | Use |
| --- | --- | --- |
| Short 1 to 4 | 50, 100, 150, 200ms | Small utility transitions (selection controls: 200ms, Standard) |
| Medium 1 to 4 | 250, 300, 350, 400ms | Transitions across a medium area (FAB into a sheet: 400ms, Emphasized) |
| Long 1 to 4 | 450, 500, 550, 600ms | Large expressive transitions, often with Emphasized (card to full screen: 500ms) |
| Extra long 1 to 4 | 700, 800, 900, 1000ms | Rare; ambient transitions without user input (carousel auto-advance: 1000ms, Emphasized) |

## Accessibility

- Contrast: 4.5:1 small text, 3:1 large text and graphics. Clustered elements
  (a row of buttons) need 3:1 against the background; standalone prominent ones
  (a FAB) don't.
- Targets 48x48dp (pointer 44dp), 8dp between targets.
- Labels describe purpose, not appearance ("Voice search," not "Microphone"),
  and never include the role ("button"). Mark decorative images as hidden.
- Define initial focus and focus return (dialogs return focus to their
  trigger). Keyboard shortcuts use two or more keys.
- Use native components before custom ones; custom dialogs need extra testing.

### Labels and roles

- Needs a label (`contentDescription`):
  - Icon-only buttons, or buttons whose text lacks context (a pencil edit button).
  - Interactive images.
  - Visual cues such as progress bars and error handling.
  - Meaningful icons (status icons) and meaningful images (diagrams, substantive photos, illustrations).
- Visible text that still needs an extra label: "Generic links (for example, 'Learn more')" and "Buttons with generic text (for example, 'Save' when there are multiple such buttons on a page)."
- No label needed: non-interactive UI text (read automatically) and "Buttons with sufficient text (for example, 'Download image')."
- Labels "concisely describe an element's content, purpose and behavior."
- Assign a role to every interactive element. "For non-web, assign roles based on your design system components (button, slider, menu, etc.)." This matters most when "some visual elements may look the same, but are intended to behave differently." The role is announced for you, so a label like "Got it button" is read as "Got it button button."

### Structure and reading order

- "Place important actions at the top or bottom of the screen (reachable with shortcuts)." "Place related items of a similar hierarchy next to each other."
- Screen readers follow the code's top-down structure, not the visual layout. Make the traversal order match the intended content hierarchy.
- Headings:
  - "Identify headings based on content hierarchy, rather than visual styling."
  - Don't skip heading levels.
  - Headings should be meaningful titles. If a title isn't meaningful, rewrite it or add a label for assistive tech.
  - Visual prominence doesn't have to match heading level.
- 48dp is about 9mm physically. The recommended touch target is 7 to 10mm, and larger targets serve a wider range of users.

### Focus flow and shortcuts

- Keep the default traversal order (left to right, top to bottom) "unless you have a UX pattern or custom component that breaks from the default pattern."
- Primary and secondary user journeys must be completable with Tab, arrow keys, and shortcuts:
  - Tab moves between interactive elements (Shift+Tab reverses).
  - Arrow keys move within a component.
  - Enter activates.
- Set initial focus for each screen and for complex components (a multi-action card, a dialog). Put it on the element that serves the most common goal.
- For unique layouts, "group a collection of interactive elements as one tab stop, and use arrow keys to traverse sub-elements."
- Shortcuts:
  - Include "a tutorial, list, or help center page of all custom keyboard shortcuts."
  - A single-key shortcut must be one of these, in order of preference: remappable to include a non-printable key (most preferred); active "only when a relevant component is focused" (preferred); or able to be turned off (temporary only).

### Assistive technology

- Design for three kinds of assistive technology besides touch:
  - Keyboard, D-pad, and trackball users "jump from selection to selection in a linear fashion."
  - Screen readers (TalkBack) read visible text plus hidden alt text and headings.
  - Switch Access: "Switches scan the items on your screen, highlighting each item in turn, until you make a selection."
- Focus order and grouping serve all three.

### Images, illustrations, and alt text

- Illustrations: essential text and graphics must meet 4.5:1 (small text) or 3:1 (large text). Decorative parts "do not have to meet Material's contrast requirements."
- Text embedded in an image is invisible to screen readers. "If there is essential information embedded as text in the image, include the essential information in the alt text."
- Captions explain how an image relates to the content, and they serve both sighted and screen reader users.
- Decorative test: "If you remove the image from the page and no information is lost, then the image is decorative." Hide decorative images (null alt, which is `contentDescription = null` in Compose).
- Alt text rules:
  - Length: "The recommended length for alt text is 140 characters." The writing best-practices page says "up to 125 characters," so aim for 125 or less.
  - "Don't start alt text with 'image of'." The screen reader already announces "image."
  - Name the image type (chart, map, screenshot, headshot, diagram) only when that helps.
  - Describe meaning in context, not detail. In a shopping app, describe the item for sale, not its surroundings.
  - The same image gets different alt text in different contexts.
  - "Don't repeat the caption in alt text." Keep wording consistent with the caption (not "antique" in one and "vintage" in the other).
  - Never leave a generated file name as alt text.
- Charts:
  - Formula: "Summary of [data type] + [reason for showing the chart]." Give key takeaways, don't copy data points.
  - Editorial charts: state the main takeaway and its metrics ("Your step count was 55% higher this week...").
  - Analysis charts: describe the structure and point to the data, don't summarize. Link the source data when it's available.
  - Prefer interactive charts with per-point tooltips for complex visualizations.
- Motion assets (GIFs, animations): if the information isn't elsewhere on screen, write alt text "like a heading or title you might give it," not a frame-by-frame account. Long-form video uses video description instead of alt text.

## Content design

- Sentence case everywhere, including buttons and app bars. No all caps.
- Second person ("you"); don't mix with "my"; avoid "we."
- No periods on single sentences in labels, tooltips, lists, dialog body text.
  Contractions yes. Serial comma. Exclamation points only for real
  celebrations. No ellipses on buttons or menu items. Avoid em dashes; use an
  en dash without spaces for ranges.
- Explain consequences neutrally and how to undo.
- Global writing: short sentences, no idioms, no abbreviations, avoid
  "please," "sorry," "thank you" in errors.
- Notifications: lead with what matters to the user, title under 29
  characters, collapsed body under 40, 1 or 2 buttons, days of the week instead
  of "today," don't repeat the app name, "don't overdo delight."

### Style rules (additions)

- "Follow Associated Press (AP) Style unless noted otherwise."
- Use specific, scannable headings and subheads to group information.
- Spell out abbreviations when there's room. No Latin abbreviations: "for example," not "e.g."; "and more," not "etc." Time abbreviations (AM, PM) are fine.
- Contractions by default, but "do not" can add emphasis "when caution is needed."
- First person ("I agree to the terms of service") is allowed for legal agreements or acknowledgments where ownership matters.
- Numbers:
  - Commas from 1,000 to 1 million.
  - No commas in addresses, radio frequencies, or years.
  - Round and abbreviate for volume ("23M" views).
- Punctuation:
  - Colons: none in headings on lists of items; use one to introduce a list within body text.
  - Parentheses: only to define terms, acronyms, or jargon, or to cite a source. Never for side notes.
  - "&": only in headlines, column and table headers, navigation labels, and buttons, and only when space is tight. Spell out "and" in sentences.
  - Ellipses: allowed for an action in progress or truncated text, with no space before them.
- Emphasis: bold, not italics. Italics only for a single word or phrase such as a name, never a whole sentence. No all-caps blocks ("they're not accessible").

### Writing for translation (additions)

- "Other languages average at 1.5 times longer than English." Localization research says text "can expand by 30% or might even double," and some scripts get shorter but taller. Size buttons, tabs, and chips for that:
  - "Leave open space around condensed UI components, such as buttons and tabs."
  - For longer text, "establish a component's maximum width that allows lengthier passages to wrap."
- Use global examples ("the holidays," not "Christmas"). Where a local reference is needed (locations, names, currencies, temperatures, date formats, providers), explain it in the string's translator description.
- Pronouns and ambiguity:
  - Repeat the noun instead of an unclear pronoun ("Couldn't move photo").
  - Don't start a sentence with "this" or "that" unless the noun follows immediately.
  - Don't use both meanings of a word ("filter," "change," "traffic") in one string.
  - Reduce technical jargon.
- "Please" is acceptable only when asking the user to do something inconvenient.
- Icons and emoji don't read the same everywhere. Pair culturally loaded icons with a word (a flame plus "Trending"). Don't replace words with emoji.
- Fonts and line breaks: use Noto for CJK and other scripts so Latin and CJK text match in weight and missing-glyph boxes never appear. Don't break CJK words across lines.
- Account for regional differences in color meaning, preferred information density, RTL mirroring, and data formats (addresses, names, currencies, measurements).

### Notification copy (additions)

- Expanded body under 80 characters: "start with collapsed body and add to it." Buttons are 1 to 2 words each. These limits still apply on newer Android versions because they prevent truncation on small devices.
- Dynamic text: "Place dynamic text in the notification body." "Text that gets truncated in the headline will not expand, even in expandable notifications." If a title must be dynamic, pair it with at most one other word, and keep a fallback notification that fits.
- Emoji: "Use emoji only to enhance a message." "Don't add negative emotions to a message. Don't replace words with emoji." Face and hand-gesture emoji perform better than generic ones.
- Offer opt-out in context, and say what the user gains or loses. Don't fire onboarding notifications during onboarding. Never send unsolicited ads.
- Put critical information at the front of sentences (people skim in an F shape). Make the call to action concise and specific.

## Component selection guide

Condensed "which component and when" rules from the component usage sections.

| Need | Use | Key rules |
| --- | --- | --- |
| Page title and 1 or 2 actions | App bar (search, small, medium flexible, large flexible) | 1 action, 2 max; boost the key action with a filled or tonal (optionally wide) icon button, never two filled; container fills on scroll, or stays transparent with filled icon buttons; flexible bars compress to small on scroll |
| Many page actions | Toolbar (docked for global, floating for contextual); "Where app bar supports navigation, toolbar provides critical actions for the current page" | Standard or vibrant color; never with a navigation bar on screen; floating can collapse to a FAB on scroll |
| The single most important action | FAB (the site calls medium "most recommended"; the talks position it for compact and medium windows and foldables) | One per screen; not every screen needs one; stays put on scroll; disappears and reappears when switching tabs |
| Labeled primary action on long scroll | Extended FAB (small, medium, large) | One per screen; bottom half only; not in a set of actions; not with a floating toolbar |
| 2 to 6 related actions from a FAB | FAB menu | Color set matches the FAB; not from an extended FAB; not with a toolbar or rail |
| Discrete actions | Buttons (elevated, filled, tonal, outlined, text; XS to XL; round or square) | Don't overuse; no more than 3 in an arrangement; the primary action gets more size, color, or shape |
| Related buttons that react together | Standard button group | Same size and shape by default; mixed sizes only for hero moments; shape differences only for selection or meaning |
| Pick options or switch views | Connected button group | Must be toggleable; don't mix color styles |
| Main action plus hidden alternatives | Split button | Menu aligned to the trailing half, 4dp gap; menu button rotates with the standard scheme |
| Common icon actions | Icon button (filled, tonal, outlined, standard; XS 32 to XL 136dp; narrow, default, wide) | Filled sparingly; same size when equally important; tooltip on hover |
| Contextual choices, filters, tags | Chips (assist, filter, input, suggestion) | Never for Save or Cancel; always in a set; scroll horizontally |
| Single-topic content | Cards (elevated, filled, outlined; same function) | Don't force content into cards when spacing or headings would do; expand with container transform only for hero moments; no internal scrolling on mobile |
| Visual collection | Carousel (multi-browse, uncontained, hero, center hero, full-screen) | Snap-scroll except uncontained; max 3 items with text on compact; provide Show all |
| Scannable items | List (expressive) | Align leading visuals and text; gaps for contained lists, dividers only for uncontained; one selection mode at a time |
| Temporary actions | Menu (vertical) | Vibrant sparingly; group with gaps or dividers |
| 3 to 5 top destinations | Navigation bar (flexible) | Labels always; not with fewer than 3 (use tabs); no swiping between destinations; reselect scrolls to top |
| Destinations on larger screens | Navigation rail (collapsed 3 to 7 items; expanded replaces the drawer) | Leading edge, outside panes; one navigation component per screen |
| Related content at one level | Tabs (primary, secondary) | Not for sequential content; avoid swipeable content inside |
| Supplementary content | Bottom sheet (mobile), side sheet (medium+) | Drag handle cycles heights; scrim tap closes |
| Blocking decision | Dialog | Sparingly; low priority goes to a snackbar |
| Brief feedback | Snackbar | One at a time; one action; above the FAB and navigation |
| Wait 200ms to 5s | Loading indicator | Under 200ms nothing; over 5s a progress indicator; one indicator per group |
| Settings on or off | Switch | Immediate effect; not for opposing options (use a connected button group) |
| One or many from a list | Radio buttons (5 or fewer options) or checkboxes | Vertical; one radio always selected |
| Value from a range | Slider (standard, centered, range; XS to XL) | Immediate effect; no vertical range sliders |
| Text entry | Text field (filled or outlined) | Don't mix variants in one form |
| Search | Search bar, search app bar (global), or search icon button (secondary) | Gaps group results; docked on tablets, full-screen on phones |
| Icon-only label | Plain tooltip | One at a time; never hide critical info in a tooltip |

---

# Part 2: The Expressive layer

## What it is and why

> "Expressive interfaces have an emotional impact, fostering connection by
> evoking a feeling or mood through visual design and interaction."

- Launched May 2025 as "a set of new features, updated components, and design
  tactics for creating emotionally impactful UX." Not M4, not a deprecation.
- "Most researched update since 2014": 46 studies, 18,000+ participants, using
  eye tracking, surveys and focus groups, experiments, and usability tests.
  Research started at the component level (which progress indicators make
  waits feel faster, how big a button can be before it overwhelms, how to make
  the floating toolbar noticeable).
- Findings:
  - Preferred by all ages, **especially 18 to 24**.
  - Rated higher on "energetic," "emotive," "positive vibe," "creative,"
    "playful," "friendly," and seen as more modern.
  - About **100% increase in preference** and **up to 170% in aesthetics** over
    baseline variants.
  - Participants spotted key UI elements **up to 4x faster**.
  - Users are more likely to switch to products using Expressive.
  - Up to **87% preference** among 18 to 24 year olds.
  - Accessibility: older adults usually take longer to find key elements, but
    "with M3 Expressive, we saw a dramatic erasure of the age gap." Larger
    buttons and clearer hierarchy help across movement and visual abilities.
- Expressive sharpens screens that already work: Google's Fitbit before and
  after started from a screen that was "very functional and readable" and used
  the shape library and a toolbar "to strengthen the hierarchy and guide your
  attention." Google ships these in Meet (button groups), the Pixel media
  player (wavy progress), Pixel volume (sliders), Workspace (split button),
  Gmail and Fitbit (shapes), and the Phone dialer (a hero moment).
- The research framing matters for the skill: expressive is justified by
  **usability**, not decoration. Emphasis makes the primary action findable.
- Google's own caveats: "a strong minority of users preferred calmer, less
  intense versions"; start from user needs; "don't compromise your product's
  core functionality for visual flourishes. No amount of emotion can compensate
  for a lack of clarity."
- From the original design.google article ("Expressive Design: Google's UX
  Research"):
  - Eye tracking across 10 apps in Expressive and current M3 versions. In an
    email app, a larger Send button placed just above the keyboard in the
    secondary color was spotted 4x faster than a small Send in the top app bar.
  - Desirability: +32% "subculture" (feels in-the-know), +34% modernity, +30%
    "rebelliousness" (bold, breaks convention).
  - "Context still matters": what works in a media player may not suit
    banking. Failures: a playlist rebuilt as helter-skelter album art looked
    exciting but wasn't recognized as a playlist; removing text labels from
    email actions reduced usability. "When basic interaction paradigms are
    broken, expressive design can lead to poor usability."
  - Unfamiliarity lowered some scores, expected to fade as adoption grows.

## The seven expressive tactics

Each tactic is one axis along which a screen can become more expressive.

1. **Use a variety of shapes.** Mix classic and abstract shapes, round and
   square corners, for tension and contrast. Break from the surrounding shape
   style to draw attention to one element. Caution: smaller shapes can make
   essential actions look less important.
2. **Apply rich and nuanced colors.** Use contrast between primary, secondary,
   and tertiary roles and surface tones to prioritize. Caution: without
   contrast, elements blend together.
3. **Guide attention with typography.** Emphasized styles for headlines and
   actions; heavier weight, larger size, color, and spacing create
   "editorial-like moments."
4. **Contain content for emphasis.** Group content into containers. Give the
   most important content "ample space and the brightest surface mapping."
   Caution: ungrouped information blends together.
5. **Add fluid and natural motion.** Shape morph, surface effects, expressive
   springs, custom micro-animations.
6. **Leverage component flexibility.** Shift controls to the context, adapt to
   foldables and large screens.
7. **Combine tactics to create hero moments.** See below.

### Hero moments

> "Hero moments use multiple expressive tactics to break from predictable or
> uniformly applied design ideas."

- They make a stand-alone statement or frame essential information "in a
  fresh, editorial way," and act as a focusing mechanism: "Invest your time in
  making the most critical interactions sing; these moments are the heart of
  your product."
- They are "brief, delightful, surprising, and unexpected."
- **Stick to one or two per product.** Too many overwhelm or distract.
- To find the hero moment, ask: *Is this interaction emotionally impactful?*
  and *Is this a key interaction in the product?*

## Emphasis and hierarchy (usability)

The usability page is the most actionable source:

- Emphasize key actions to create visual hierarchy; don't overwhelm with too
  much visual information; test and iterate.
- Order of work: first build a strong hierarchy (color, size, spacing,
  placement, containment), **then** add unique emphasis to celebrate success or
  progress (illustration, scale, shapes, shape morph).
- **Size:** the most important action should be the largest element. Larger
  key actions measurably improve efficiency, errors, learnability, and
  satisfaction.
- **Color:** use contrasting hues (their example: purple and green) rather than
  similar ones.
- **Primary goals get the strongest emphasis.** Rank primary, secondary, and
  tertiary goals per screen. One primary task per page, empty space to focus
  attention, core actions large and reachable.
- **Don't use too many expressive tactics at the same time.**

### Worked example: Aura breathing app

Google's conceptual app, built from the eye tracking research. It is the best
template for how the skill should reason: each screen spends emphasis according
to its goal rank.

| Screen | Goal rank | How emphasis is spent |
| --- | --- | --- |
| Home | Primary: start a session | Extra large **Start breathing** button, dark `primary` on soft light purple, placed low for reach, last in the vertical flow. Settings in `secondary`, less emphasized. Daily message least emphasized, but in a soft blue container with medium text. Components: XL button, button groups, switch, navigation bar. |
| Session | Hero moment | A huge flower shape (Material shapes "flower" and "sunny") expands and contracts with the breath, driven by spring tokens. Vibrant yellow on inhale against the purple background. Countdown numbers very large versus small inhale/hold/exhale labels. Pause and stop small, at the bottom. Navigation bar hides. |
| Report | Secondary | Fewer tactics "to reduce cognitive load." Metrics inside uniform flower shapes, emphasized type for key numbers (18, 3min), medium (not XL) **Finish** button. |
| Progress | Tertiary | Subtle tactics. Key data in `primary`, completed days as `secondary` yellow flower shapes in a calendar. Custom-scaled numbers "large enough to scan, but they don't dominate." |

Lessons they call out explicitly:

- **Version 1 vs 2:** v1 had no containment, similar sizes, inconsistent colors,
  every setting competing. v2 grouped settings as list items above one extra
  large button with consistent secondary color. "From cluttered to calm."
- **Clear scale and placement.** Caution: all elements large and competing. Do:
  one strong focal point, supporting controls clear but smaller.
- **Consistent color roles.** Caution: `primary` and `primaryContainer` for
  everything. Do: `primary` for the main action, `secondary` for selected
  settings, `secondaryContainer` for dates.
- **Calm, balanced layouts.** Caution: shapes of different forms and sizes,
  overlapping. Do: uniform shapes, even spacing.
- **One moving thing.** Caution: text width changing while arrows and the
  flower animate at once. Do: keep text stable so attention stays on the one
  moving shape.

## Shape (Expressive)

- 35 shapes in the Material shape library, with built-in morphing between any
  two. Compose: shapes API (`MaterialShapes`); not available on web.
- Shapes echo Google Sans Flex's roundness: "Use shape and type together."

Principles:

- **Morph to connect function and feeling.** Morph for interaction states
  (selected, pressed), actions in progress (typing, loading), and environmental
  change (sound, temperature, time). Consider tap, swipe, scroll, release, long
  press.
- **Be bold and embrace tension.** "Material historically focused on rounded
  shapes. However, using sharp shapes, thereby adding tension, creates more
  dynamic design, one that's more memorable and expressive." Mix square and
  round.
- **Shape is versatile, not semantic.** Don't assign a fixed meaning to one
  shape (a wave is not "the progress shape").
- **Use abstract shapes sparingly.** "Shapes without clear meaning behind why
  they're different can add more visual clutter than delight." Not on
  text-heavy containers. "Ensure that shapes resonate with the product's
  narrative. Consider the 'why' behind their inclusion": a shape earns its
  place by connecting to the subject, not by filling space.
- **Aesthetic moments are the most flexible use:** image crops, avatar masks,
  decorative graphics.
- **Shape can be 2.5D:** layered shapes with different motion suggest depth.
- Customizing shapes "is sometimes necessary, and even encouraged, for hero
  moments or custom components." Carousels can switch between square and fully
  rounded items for "unexpected moments."

## Color (Expressive)

- "Vibrant color schemes": an expanded range of colors "to sharpen hierarchy
  and clarify key actions."
- Hierarchy comes from **contrast between roles**, not `primary` everywhere.
  Use contrasting hues for different kinds of things (action vs status vs
  data).
- The most important content gets the brightest surface.
- **Vibrant** component styles are high emphasis: vibrant menus map to
  tertiary ("Vibrant menus are more prominent, and should be used
  sparingly"); vibrant toolbars use primary container.

## Typography (Expressive)

- 30 styles: 15 **baseline** and 15 **emphasized** (higher weight, minor
  adjustments), same scale. **Components don't use emphasized styles by
  default;** opt in per usage.
- Use emphasized styles on selection, actions, headlines, editorial moments,
  badges, primary buttons, extended FABs, selected list and menu items, unread
  messages.
- Two ways to apply: **weight** (emphasize text that is already bold) and
  **context** (emphasize selectively to show hierarchy or state).

### Google Sans Flex

- Open source on Google Fonts since 2025-11-12 (Google Fonts metadata, checked
  Sep 2026). Axes: `wght` 1 to 1000, `wdth` 25 to 151, `opsz` 6 to 144,
  `slnt` -10 to 0, `GRAD` 0 to 100, `ROND` (roundness) 0 to 100. The shape
  page notes M3 shapes and Google Sans Flex "share roundness visual attributes."
- "Making Google Sans Flex" (design.google): Google Sans Flex is "a power tool
  for expression." Weight takes text from "calm as a whisper" to "loud and
  rugged"; roundness evokes a "personal, playful" tone and was the axis
  designers found most influential. Research with 3,000+ readers found taller,
  more elegant styles more premium and engaging. Optical size keeps expressive
  settings legible "from a smartwatch to a billboard," so it must track text
  size. Google Sans Code (open source, 2025) is the code face; Google Sans Mono
  was for medium and large editorial text and failed for code. Open-sourcing
  aims to close the visual gap between Google and third-party apps.
- It is the face most shipped Expressive apps use (observed in the reference
  screenshots below). Compose applies axes through
  `Font(resId, weight, variationSettings = FontVariation.Settings(...))` on
  API 26+; the resource overload is not experimental.

- The small-size companion is Google Sans Text (2020, first on Pixel 3). Google Sans was not legible enough at small sizes, which had forced a split: Google Sans for display, Roboto for small text. Compared with Google Sans, Google Sans Text is "taller, more condensed, and less circular," with more letter spacing, less geometric numerals, and softer terminal cuts. It was "designed to match the proportions of Roboto" so swapping from Roboto is smooth.
- Google Sans supports "more than 20 writing systems," including Arabic, Cyrillic, Chinese, Devanagari, Greek, Hebrew, Japanese, Korean, and Thai.

### Reference screenshots (patterns observed)

Sixteen screenshots of Google's Expressive showcases and third-party
Expressive apps (focus timer, music players, expense tracker, weather) show
recurring moves:

- Variable axes carry state: selected alarm times heavy in a filled container,
  unselected ones thin (same size).
- Tall condensed numerals (narrow width) filling cards; huge Display numbers
  for temperatures and totals.
- Mixed voices: mono for metadata and timecodes, serif for long reading, a
  bold display face for the hero.
- Metric cards: small label over a huge number, containers color-coded per
  category; an inverse (dark) card for the most important total.
- Shape-masked media and avatars (cookie, flower, clover), clustered avatars
  for groups, shapes as decorative badges.
- Pill hero controls (full-width Play or Pause with smaller round secondary
  buttons), connected button groups for options, wavy progress for playback
  and countdowns.
- Floating pill toolbars at the bottom with the active item as a filled pill.
- Segmented settings lists with icons in tonal circles and switches with
  check/close thumb icons.
- Color that follows content or state (weather sky), with seed color, palette
  style, and pure black dark mode offered as user settings.
- From the "Making Google Sans Flex" article images: two voices inside one
  line to encode meaning (origin light and condensed, destination heavy, wide,
  and slanted: "PDX › SFO"); type as graphic (oversized numbers and words
  cropped by the edge or tinted near their background); huge numerals with a
  small unit on the same baseline ("15 min", "0%"); big split action pairs
  (Snooze and Stop as half-width pills); arc text around circular progress.

### Editorial treatments

> "Standalone, showcase moments driven by type... type can freely dominate the
> screen."

- Three uses: **celebrate content** (a photo album cover), **voice of the
  user** (an enthusiastic message rendered huge), **bespoke functionality** (a
  light slider whose label widens and bolds as brightness rises).
- Match the emotion: narrow and thin for serenity, bold and italic for
  liveliness.
- Guard rails: tokenize treatments for consistency, match the emotional tone,
  don't mix clashing styles in one layout, don't mimic personalization
  theming, never on labels or purely informational text.
- Variable axes: weight (no very light weights at small sizes, no excessive
  weight at small sizes), grade (emphasis without reflow; negative grade in
  dark mode), width (narrow for tight labels; no wide type in app bars),
  optical size (match to type size).

## Motion

- The **motion physics system** (springs) replaces easing and duration for
  components. 21 Compose components use it by default; all component motion is
  driven by two tokens, expressive fast spatial and expressive fast effects.
- **Schemes:** **Expressive** ("Material's opinionated scheme... for most
  situations, particularly hero moments and key interactions," overshoots) and
  **Standard** ("more functional with minimal bounce... for utilitarian
  products"). Custom schemes are allowed.
- **Spec kinds:** **spatial** (position, rotation, size, corner radius;
  overshoots) and **effects** (color, opacity; no overshoot).
- **Speeds:** **fast** (small components like switches and buttons),
  **default** (partial-screen, like sheets and expanded rails), **slow** (full
  screen). Values resolve per device (watch vs phone vs tablet).
- Why springs: interruptible and retargetable with velocity carried through;
  they adapt across screen sizes because they're defined by damping and
  stiffness, not time.
- Three customization levels: preset scheme; custom `MotionScheme`; override
  the scheme per element via CompositionLocal (for example Standard for the
  app, Expressive for the hero). Shape morphing uses the Expressive scheme by
  default. The split button's menu rotation deliberately uses Standard.

```kotlin
MaterialExpressiveTheme(
    motionScheme = MotionScheme.expressive(), // or MotionScheme.standard()
) { /* ... */ }

// Custom components should use the theme's specs, not tween():
val scale by animateFloatAsState(
    targetValue = if (pressed) 1.1f else 1f,
    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
)
val color by animateColorAsState(
    targetValue = if (pressed) activeColor else restColor,
    animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
)
```

Spring values from the androidx tokens (`ExpressiveMotionTokens.kt`,
`StandardMotionTokens.kt`), damping ratio / stiffness:

| Spec | Expressive | Standard |
| --- | --- | --- |
| Fast spatial | 0.6 / 800 | 0.9 / 1400 |
| Default spatial | 0.8 / 380 | 0.9 / 700 |
| Slow spatial | 0.8 / 200 | 0.9 / 300 |
| Fast effects | 1.0 / 3800 | 1.0 / 3800 |
| Default effects | 1.0 / 1600 | 1.0 / 1600 |
| Slow effects | 1.0 / 800 | 1.0 / 800 |

- **Plain `MaterialTheme` defaults to `MotionScheme.standard()`**; only
  `MaterialExpressiveTheme` defaults to expressive. This alone explains flat
  motion in stock code.
- `LocalMotionScheme` was removed (1.5.0-alpha27): override a subtree by
  nesting `MaterialTheme(motionScheme = ...)`.
- A custom scheme that snaps every spec immediately is Google's own pattern for
  a reduced-motion scheme ("mySnappyMotionScheme" in the I/O 2025 talk).
- The blog's "playful" custom scheme (spatial damping 0.6 with stiffness
  700 / 1400 / 300) is an example of customization, not the Expressive values.

Web approximations of the springs (a sanity check of the feel):

| Spring | Cubic bezier | Duration |
| --- | --- | --- |
| Expressive fast spatial | 0.42, 1.67, 0.21, 0.90 | 350ms |
| Expressive default spatial | 0.38, 1.21, 0.22, 1.00 | 500ms |
| Expressive slow spatial | 0.39, 1.29, 0.35, 0.98 | 650ms |
| Expressive fast / default / slow effects | 0.31, 0.94, 0.34, 1.00 / 0.34, 0.80, 0.34, 1.00 / 0.34, 0.88, 0.34, 1.00 | 150 / 200 / 300ms |
| Standard spatial (all speeds) | 0.27, 1.06, 0.18, 1.00 | 350 / 500 / 750ms |

Bounce belongs to components and hero moments; screen transitions stay simple
(see [Motion foundation](#motion-foundation-transitions)).

## Containment and spacing (Expressive)

- Give the most important content "visual prominence with generous spacing and
  the brightest surfaces." Negative space frames and emphasizes.
- Consistent placement of key actions builds recognizable focal points across
  screens.
- Group with containers; in lists and search results, **gaps** between filled
  items replace dividers.

## Components: what Expressive replaced

The most mechanical, highest-leverage part for the skill: stock code still
defaults to the left column.

| No longer recommended | Use instead |
| --- | --- |
| Medium and large top app bars | Medium flexible and large flexible app bars (shorter, larger title, subtitle, wrapping, left or center aligned) |
| Bottom app bar | Docked toolbar, or floating toolbar (horizontal or vertical, standard or vibrant, can pair with a FAB) |
| Segmented buttons | Connected button group |
| Navigation drawer | Expanded navigation rail (modal or standard) |
| Baseline navigation rail | Collapsed navigation rail |
| Baseline navigation bar | Flexible navigation bar (shorter; horizontal items in medium windows; active label uses `secondary`) |
| Small FAB, surface FABs | FAB, medium FAB, large FAB in primary / secondary / tertiary or their containers |
| Baseline extended FAB, surface extended FAB | Small (56dp), medium (80dp), large (96dp) extended FAB with larger type |
| Speed dial, stacked small FABs | FAB menu |
| Indeterminate circular progress for short waits | Loading indicator (shape morph, 200ms to 5s, contained or not; also pull-to-refresh) |
| Baseline list | Expressive list (standard or segmented, highlighted selection, slots) |
| Baseline menu | Vertical menu (standard or vibrant, gaps, submenus) |
| Search bar plus search view | Search (contained style recommended, grows wider when focused) |

New or expanded:

- **Buttons:** five sizes (XS, S, M, L, XL), round or square, shape morphs when
  pressed and when selected, toggle behavior. Small button padding 16dp.
- **Icon buttons:** five sizes (32, 40, 56, 96, 136dp), three widths, round or
  square, morph on press and select.
- **Button groups:** standard (pressed button changes shape and width, neighbors
  temporarily change width; selected toggles also change color) and connected.
  Selected buttons change round to square or square to round.
- **Split button:** menu half spins 180 degrees and changes shape when opened.
- **Progress indicators:** configurable track height and a **wavy** active
  track "for use cases that would benefit from increased expressiveness" (flat
  in very small buttons).
- **Sliders:** five sizes, vertical orientation, optional inset icon (Compose
  has no size presets; see the API reference).
- **Expressive lists:** the segmented style uses gaps between filled items;
  unselected items have 4dp inner and 16dp outer corners, selected items morph
  to 16dp all around. Swipe reveals mixed-style buttons, with the primary
  action last.
- **Expressive lists, more guidance:** people get circular or expressive-shape
  avatars, content gets square images; items with images can take a
  content-based container color; selection needs two cues, not color alone;
  lists can become cards or carousels at larger breakpoints.
- **Vertical menus:** vibrant style is tertiary-based. Group with gaps (not if
  the menu scrolls) or dividers. At most one or two gaps; disabled items stay
  visible; slots only for simple content; menus can become bottom sheets on
  compact screens.
- **Search:** container `surfaceContainerHigh`, never on `surfaceContainer`
  ("use surface container roles that are more than one step apart"); at most
  two trailing icons; hint text names what's searchable; 24dp margins
  unfocused, 12dp focused.
- **Toolbars:** "use the vibrant color style for greater emphasis" or to signal
  a temporary mode such as editing.
- **App bars:** can stay transparent on scroll with filled icon buttons
  floating over content; narrow icon buttons for Back.

## Restraint: collected cautions

Expressive is not "maximum everything." Every source pairs a push with a limit:

- One or two hero moments per product.
- Don't combine too many tactics at once; secondary screens use fewer.
- One focal point per screen; not every element large.
- Don't use the same color role for all actions and data.
- Abstract shapes sparingly and only with a reason; not on text-heavy
  containers.
- Uniform shapes for grouped data; no overlapping shapes.
- One moving thing at a time in a hero moment.
- Vibrant components sparingly; filled buttons sparingly.
- Mixed button sizes in a group only for hero moments.
- Transitions stay simple.
- Editorial type never on labels or purely informational text.
- Respect reduced motion (fades, no morphing or parallax).
- A strong minority prefers calmer designs; function beats flourish.

### Customizing without forking

From "Make Material your own" (I/O 2026): "Material is no longer an 'all or
nothing' choice." Brand identity sits on top of Material's research-driven,
accessible UX layer.

- Order of preference: **use as-is** (easiest), **wrap** (more control, but you
  own the API and forward new features), **Styles API** for visual changes
  (coming to Material components; can set a pressed background, even a
  gradient, without tracking interaction state), **fork** only as a last
  resort. "When you fork, you break your connection to the Material
  foundation": accessibility, behavior, and updates are lost "all for the sake
  of a stylistic change."
- Google's brand examples keep Material behavior and add one subtle touch:
  Shrine adds a custom shadow and animated background to a single promo button
  (keeping its shape morph); Achieve replaces the ripple with a custom reward
  animation on task completion, because rewarding the user is core to the
  brand; Clip uses "oversized graphic components" for a bold, distinct look.

## Compose API reference (verified September 2026)

Verified against androidx-main (`bf95ca5`, Sep 22, 2026) and the released
`material3-android-1.5.0-alpha28` sources, whose public API matches main
except `Slider`. Source root:
`compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/`.

### Versions and stability

- `androidx.compose.material3:material3`: stable **1.4.0** (Sep 2025) has
  **no Expressive APIs** except `WideNavigationRail`, `ModalWideNavigationRail`,
  and `ShortNavigationBar`. Expressive requires **1.5.0-alpha28** (Sep 9, 2026);
  1.5.0 has no beta yet.
- Within 1.5.0 alphas nearly everything was promoted out of experimental:
  motion (alpha15), typography (alpha16), `MaterialExpressiveTheme` and
  wavy progress (alpha18), buttons, toggles, menus, FABs (alpha19), split
  button (alpha20), floating toolbar and button groups (alpha22), flexible app
  bars, expressive list items, contained search (alpha23).
- **Still `@ExperimentalMaterial3ExpressiveApi`:** `MaterialShapes`,
  `RoundedPolygon.toShape()` / `toPath()`, `Morph.toPath()`, `LoadingIndicator`,
  `ContainedLoadingIndicator`, and the pull-to-refresh loading indicator.
  **Still `@ExperimentalMaterial3Api`:** `AppBarWithSearch`,
  `carouselParallaxScrollEffect`, sheets, tooltips.
- Other versions: adaptive 1.3.0 stable (1.4.0-alpha02); Wear compose-material3
  1.6.2 stable (1.7.0-rc01); graphics-shapes 1.1.0; MDC Views 1.14.0.
- **Replaced components are not deprecated.** Lint won't flag medium/large top
  app bars, the bottom app bar, or segmented buttons, so the skill has to steer.
  Exception: `ListItem(headlineContent = ...)` is deprecated.

- **Views is in maintenance mode.** "Material Views 1.14.0 (MDC-Android) will be our final stable release for the Views library." The library "won't get any new features, but will receive critical bug fixes." Views 1.14.0 did ship Expressive themes, an expressive list, the emphasized type scale and expressive styles for 11 components.
- **Compose is the only target.** Material Android is "all-in" on Compose and moves all feature work to Material Compose. Material Compose 1.5.0, due later in 2026 (H2), promotes the M3 Expressive experimental APIs to stable. The Figma kit and guidelines follow the Compose library. Generate Compose only. Google offers a migration skill for moving Views screens to Compose screen by screen.
- The Styles API integration will skip "the composition phase during style updates," which improves performance.

### Theme

```kotlin
@Composable fun MaterialExpressiveTheme(
    colorScheme: ColorScheme? = null, motionScheme: MotionScheme? = null,
    shapes: Shapes? = null, typography: Typography? = null, content: @Composable () -> Unit,
)
```

- Top-level nulls default to `expressiveLightColorScheme()`,
  `MotionScheme.expressive()`, `Shapes()`, `Typography()`; nested nulls inherit.
- `expressiveLightColorScheme()` is `lightColorScheme` with on*Container roles
  at tone 30. **There is no `expressiveDarkColorScheme`**; use
  `darkColorScheme()`.
- `MotionScheme` specs: `default/fast/slowSpatialSpec<T>()` and
  `default/fast/slowEffectsSpec<T>()`, read from `MaterialTheme.motionScheme`.

```kotlin
@Composable fun AppTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val context = LocalContext.current
    val scheme = when {
        Build.VERSION.SDK_INT >= 31 -> if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> darkColorScheme()
        else -> expressiveLightColorScheme()
    }
    MaterialExpressiveTheme(colorScheme = scheme, motionScheme = MotionScheme.expressive(), content = content)
}
```

### Color schemes from a seed

- Compose has **no seed or variant API**: only `dynamicLight/DarkColorScheme(context)`
  (API 31+), with no style parameter.
- Variants (TonalSpot, Neutral, Vibrant, Expressive, Rainbow, FruitSalad,
  Monochrome, Fidelity, Content) live in material-color-utilities, which has no
  Maven artifact; MDC's copy is `@RestrictTo`.
- Practical options: export a static brand scheme from Material Theme Builder;
  or generate at runtime with the third-party **MaterialKolor**
  (`com.materialkolor:material-kolor:5.0.1`):
  `rememberDynamicColorScheme(seedColor, isDark, specVersion = ColorSpec.SpecVersion.SPEC_2025, style = PaletteStyle.Expressive)`.
- The videos add: dynamic color on recent Android versions has "more variation
  and higher chroma across all hues," and Google "strongly encourage[s]" it.

### Migrating elevation-tinted surfaces

- Tone-based surface roles replace "surfaces at +1 to +5 elevation." They are not tied to elevation and support user-controlled contrast. Material components switch automatically. Custom mappings should be remapped.
- Replace `ColorScheme.surfaceColorAtElevation()`:
  - +1 becomes `surfaceContainerLow`
  - +2 becomes `surfaceContainer`
  - +3 becomes `surfaceContainerHigh`
  - +4 and +5 are deprecated: use `surfaceContainerHighest` by default, or `surfaceContainerHigh` / `surfaceDim` by use case
  - `surfaceVariant` becomes `surfaceContainerHighest`
  - `surfaceContainerLowest` is new
- `surfaceContainer` is "the recommended default color role for a contained area against the surface color role."

### Shapes

- `Shapes(extraSmall, small, medium, large, extraLarge, largeIncreased,
  extraLargeIncreased, extraExtraLarge)`: 4, 8, 12, 16, 28, 20, 32, 48dp; read
  as `MaterialTheme.shapes.largeIncreased` and so on (1.5.0 only).
- `MaterialShapes` (experimental), 35 `RoundedPolygon`s: Circle, Square,
  Slanted, Arch, Fan, Arrow, SemiCircle, Oval, Pill, Triangle, Diamond,
  ClamShell, Pentagon, Gem, Sunny, VerySunny, Cookie4Sided, Cookie6Sided,
  Cookie7Sided, Cookie9Sided, Cookie12Sided, Ghostish, Clover4Leaf,
  Clover8Leaf, Burst, SoftBurst, Boom, SoftBoom, Flower, Puffy, PuffyDiamond,
  PixelCircle, PixelTriangle, Bun, Heart.
- `RoundedPolygon.toShape(startAngle = 0): Shape` (composable) for clipping;
  `Morph(start, end)` from `androidx.graphics.shapes` plus
  `Morph.toPath(progress)` (unit space) for morphing. Prefer components'
  built-in `shapes` / animated shapes over hand-built morphs, which "usually
  require many lines of code."

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private class MorphShape(val morph: Morph, val progress: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = morph.toPath(progress = progress)
        path.transform(Matrix().apply { scale(size.width, size.height) })
        return Outline.Generic(path)
    }
}
// val morph = remember { Morph(MaterialShapes.Circle, MaterialShapes.Cookie9Sided) }
// val t by animateFloatAsState(if (selected) 1f else 0f, MaterialTheme.motionScheme.defaultSpatialSpec())
// Modifier.clip(MorphShape(morph, t))
```

### Typography

- 15 `*Emphasized` properties on `Typography` (`displayLargeEmphasized` to
  `labelSmallEmphasized`), 1.5.0 only. Emphasized weights: Display, Headline,
  Body, and Title Large go Regular to Medium; Title Medium/Small and Labels go
  Medium to Bold.
- New `Typography(fontFamily = ...)` constructor sets one family for all styles.

### Spacing

- **No spacing tokens in Compose** (no `space100`). Define an app spacing object
  on the 8dp scale.

### Components

| Guideline name | Compose API | Notes |
| --- | --- | --- |
| Medium / large flexible app bar | `MediumFlexibleTopAppBar`, `LargeFlexibleTopAppBar` | `subtitle`, `titleHorizontalAlignment`; pair with `TopAppBarDefaults.exitUntilCollapsedScrollBehavior()` |
| Search app bar | `AppBarWithSearch` | Experimental; nav and action slots outside the field |
| Contained search | `rememberContainedSearchBarState()`, `SearchBarDefaults.containedColors(state)`, `ExpandedFullScreenContainedSearchBar` | `SearchBar(state, inputField)` is stable |
| Floating toolbar | `HorizontalFloatingToolbar`, `VerticalFloatingToolbar` | FAB overload; `FloatingToolbarDefaults.vibrantFloatingToolbarColors()`, `VibrantFloatingActionButton`, `exitAlwaysScrollBehavior`, `floatingToolbarVerticalNestedScroll`, `ScreenOffset` |
| Docked toolbar | `FlexibleBottomAppBar` | No "DockedToolbar" composable |
| FAB menu | `FloatingActionButtonMenu`, `FloatingActionButtonMenuItem`, `ToggleFloatingActionButton` | `Modifier.animateIcon`, `animateFloatingActionButton(visible, alignment)` |
| FAB sizes | `MediumFloatingActionButton`, `LargeFloatingActionButton`, `Small/Medium/LargeExtendedFloatingActionButton` | |
| Buttons XS to XL | `Button(onClick, shapes = ButtonDefaults.shapesFor(h))` | Heights `ExtraSmallContainerHeight` 32, `MinHeight` 40, `MediumContainerHeight` 56, `LargeContainerHeight` 96, `ExtraLargeContainerHeight` 136; `contentPaddingFor`, `iconSizeFor`, `textStyleFor` |
| Toggle button | `ToggleButton` (+ Elevated, FilledTonal, Outlined) | `ToggleButtonSize.ExtraSmall..ExtraLarge` |
| Standard button group | `ButtonGroup(overflowIndicator, ...)` | Scope: `clickableItem`, `toggleableItem`, `customItem`, `Modifier.animateWidth(interactionSource)`; overflows automatically |
| Connected button group | `Row` of `ToggleButton`s | `ButtonGroupDefaults.ConnectedSpaceBetween`, `connectedLeading/Middle/TrailingButtonShapes()` |
| Split button | `SplitButtonLayout` | `SplitButtonDefaults.LeadingButton`, `TrailingButton(checked, onCheckedChange)` |
| Icon button sizes | `IconButton(shapes = ...)` + `Modifier.size(IconButtonDefaults.mediumContainerSize(IconButtonWidthOption.Wide))` | 32/40/56/96/136dp; Narrow, Uniform, Wide; round, square, pressed shapes |
| Loading indicator | `LoadingIndicator`, `ContainedLoadingIndicator` | Experimental |
| Wavy progress | `LinearWavyProgressIndicator`, `CircularWavyProgressIndicator` | `amplitude`, `wavelength`, `waveSpeed` |
| Expressive list | `ListItem(onClick, ...) { headline }`, `SegmentedListItem(shapes = ListItemDefaults.segmentedShapes(i, n))` | Space with `ListItemDefaults.SegmentedGap` (2dp) |
| Vertical menu | `DropdownMenuPopup`, `DropdownMenuGroup(shapes = MenuDefaults.groupShape(i, n))`, `SelectableDropdownMenuItem`, `CheckableDropdownMenuItem` | Vibrant: `MenuDefaults.itemVibrantColors()`; submenus by nesting `DropdownMenuPopup` (the site says submenus aren't in Compose; the source disagrees) |
| Flexible navigation bar | `ShortNavigationBar`, `ShortNavigationBarItem(iconPosition = Top or Start)` | Stable since 1.4.0 |
| Collapsed / expanded rail | `WideNavigationRail(state = rememberWideNavigationRailState())`, `ModalWideNavigationRail` | Stable since 1.4.0; `NavigationSuiteScaffold` adapts bar and rail |
| Slider | `Slider(state = s, onValueChange = { ... })`, `VerticalSlider` | **No XS to XL size presets**; inset icons are custom-drawn |
| Carousel | `HorizontalMultiBrowseCarousel`, `HorizontalUncontainedCarousel`, `HorizontalCenteredHeroCarousel` | Stable |

```kotlin
// Flexible app bar
val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
Scaffold(
    Modifier.nestedScroll(scroll.nestedScrollConnection),
    topBar = { LargeFlexibleTopAppBar(title = { Text("Inbox") }, subtitle = { Text("3 unread") }, scrollBehavior = scroll) },
) { }

// Sized morphing button
val h = ButtonDefaults.MediumContainerHeight
Button(onClick = {}, shapes = ButtonDefaults.shapesFor(h), modifier = Modifier.heightIn(h),
    contentPadding = ButtonDefaults.contentPaddingFor(h)) { Text("Start", style = ButtonDefaults.textStyleFor(h)) }

// Connected button group
Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
    options.forEachIndexed { i, label ->
        ToggleButton(
            checked = selected == i, onCheckedChange = { selected = i },
            shapes = when (i) {
                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
            },
            modifier = Modifier.weight(1f).semantics { role = Role.RadioButton },
        ) { Text(label) }
    }
}

// Segmented list
Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
    items.forEachIndexed { i, item ->
        SegmentedListItem(onClick = {}, shapes = ListItemDefaults.segmentedShapes(i, items.size)) { Text(item) }
    }
}
```

### Other APIs mentioned in the talks

- Navigation 3 list-detail: `ListDetailSceneStrategy` with entries tagged
  `ListDetailScene.ListPane` / `DetailPane`; predictive back comes free.
- Adaptive: `ExperimentalMediaQueryApi` for posture, Grid and FlexBox APIs.
- Styles API integration for Material components is announced but in progress.

- **Checkable menu items:** "use the check parameter and check leading icon. This swaps the icon from outlined to filled when an option is toggled."
- **Scroll-based number input:** a new "scroll-based input component" powers a tactile time picker variant. It is "a standalone primitive" usable for any number-based input, and is experimental in the 1.5 alphas.
- **Expanded search bars:** "expanded docked search bar with gap" animates together with `AppBarWithSearch`. The exact API name is unverified in source.

## Open questions

1. `Material3ExpressiveApi` (a no-opt-in marker mentioned in alpha18 notes)
   doesn't exist on main; whether it shipped and was removed is unverified.
2. Material Theme Builder's current Compose export format (static brand scheme
   path) is unverified.
