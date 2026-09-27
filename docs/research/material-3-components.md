# Material 3 components: usage guidelines

Per-component usage guidance from the Guidelines tab of each component on
[m3.material.io/components](https://m3.material.io/components), plus the "M3
Expressive update" notes from each Overview and key Accessibility points. It
complements [material-3.md](material-3.md), which covers the foundations, the
Expressive layer, the verified Compose API, and short notes on lists, menus, and
search; their full guideline distillation is in the "Lists, menus, and
search" section below.

## Sources and status

Scraped September 2026 (content version `2026-09-16_06-10-03`), one page per
component with the Overview, Specs, Guidelines, and Accessibility tabs. Specs
measurements are included only where they change a design decision. Quotes are
Google's wording; everything else is a close paraphrase of the page.

## Actions

### All buttons (choosing a button)
- **Use when / avoid when:** M3 has 10 button types: button, toggle button, icon button, toggle icon button, split button, standard button group, connected button group, FAB, extended FAB, FAB menu. Choose a type by the level of emphasis the action needs.
- **Variants and how to choose:**
  - High emphasis ("the primary, most important, or most common action on a screen"): the FAB, extended FAB and FAB menu are "the largest and most visually prominent", and "the extended FAB is best on large screens". The filled button is "the most prominent button after the FAB", for "final or unblocking actions in a flow" (Save, Confirm). The split button is for "key actions with multiple options" (Send, Add). The standard button group is for "multiple key actions" (Back, Pause, Next).
  - Medium emphasis: the tonal button (secondary palette) is for final, unblocking or supporting actions. Use the elevated button "only... when a button requires visual separation from a patterned background". The outlined button is for actions "that need attention but aren't the primary action", and is "the button to use for giving someone the opportunity to change their mind or escape a flow".
  - Low emphasis: the connected button group is for "changing the content visible on a page" (Walk, Bike, Drive). The text button is for "actions not essential to the user journey". The icon button is for "optional supplementary actions" (Bookmark, Star).
- **Emphasis and color:** "Each screen should contain a single prominent button for the primary action." Use different color styles to show a hierarchy among the other buttons.
- **Layout and placement:** combine button styles so attention goes to one primary action while alternatives stay available (for example a filled button, a text button and an extended FAB on one screen).
- **Do / Don't:**
  - DO: "For multiple actions, choose a higher-emphasis button for the more important action, such as a filled button next to a text button"
  - DO: "you can place an outlined button (medium emphasis) next to a filled button (high emphasis)"
  - DO: "you can place a text button (low emphasis) next to an outlined button (medium emphasis)"
  - DO: "Use a filled button on its own for a single important action"
  - DON'T: "Avoid placing a button below another button if there's space to place them side-by-side"

### Buttons (common buttons)
- **Use when / avoid when:** use buttons for discrete actions in dialogs, modals, forms, cards, toolbars and standard button groups. They "shouldn't be overused". For extra actions, "consider placing additional actions in a navigation rail, set of chips, text links, or icon buttons."
- **Variants and how to choose:**
  - Two variants: default and toggle. Use toggle buttons "for binary selections, such as **Save** or **Favorite**". Toggle buttons have no text style.
  - Five color styles, from most to least emphasis: elevated, filled, tonal, outlined, text.
  - Five sizes: XS, S (default), M, L, XL. Two shapes: round (default) and square.
  - Labels: "ideally 1–3 words", in sentence case. "Don't truncate or wrap label text."
- **Emphasis and color:**
  - Filled has "the most visual impact after the FAB", for "important, final actions that complete a flow". Use it "ideally for only one action on a page". A filled button can use tertiary colors.
  - Tonal is for a "lower-priority button [that] requires slightly more emphasis than an outline would give", such as Next in onboarding. It uses the secondary color mapping.
  - Elevated is tonal plus a shadow. Use it "only when absolutely necessary", for separation from a prominent background. "For high emphasis, consider the filled style instead."
  - Outlined is medium emphasis and "pair[s] well with filled buttons". Use it on simple backgrounds, not on images or video.
  - Text is for "the lowest priority actions". It is used in cards, dialogs and snackbars. "Don't underline the text button."
  - Other color roles are allowed if the container and text keep a 3:1 contrast ratio.
- **Layout and placement:**
  - Width fits the label, or it can be responsive and stretch to the layout grid.
  - Put the icon on the leading side.
  - Align dialog text buttons to the trailing edge.
- **Behavior (scroll, adaptive, motion):**
  - Buttons morph toward a square when pressed.
  - Toggle buttons change from round to square when selected (a square button changes to round).
  - A toggle button uses an outlined icon when unselected and a filled icon when selected, or a heavier weight if no filled icon exists.
  - Adaptive: "constrain button width or place buttons beside other elements" in large windows. Keep the icon and label grouped and centered. Keep the same element order on every screen size.
- **Do / Don't:**
  - DO: "Use buttons for discrete actions"
  - DON'T: "Don't clutter your UI with too many buttons. Consider presenting low-priority actions in overflow menus or as icon buttons."
  - DON'T: "A button container's width shouldn't be narrower than its label text"
  - DO: "When using toggleable buttons, keep the label character count a similar length for both states"
  - DON'T: "Don't wrap text."
  - CAUTION: "The outlined button style is very similar to chips. Consider using a filled or tonal button instead."
  - DON'T: "Don't vertically align an icon and text in the center of a button"
  - DON'T: "Don't use two icons in the same button"
  - CAUTION: "Higher elevation increases the emphasis of a button"
  - CAUTION: "Use caution when placing outlined buttons on top of images... Or, use a filled button instead."
  - DON'T: "Don't allow the button to stretch in a way that creates long, flat buttons with very little content inside"
- **Accessibility:**
  - Enabled buttons need 3:1 contrast with the background. Measure the container for elevated, filled and tonal, and the label for outlined and text.
  - XS and S buttons need a target of at least 48x48dp.
  - On Android, a label must fit within two lines at 200% text size.
  - The accessibility label matches the visible label.

- **Specs (shape and toggle color):**
  - Square button corner radius by size: XS 12dp, S 12dp, M 16dp, L 28dp, XL 28dp. Pressed radius: XS 8dp, S 8dp, M 12dp, L 16dp, XL 16dp. "Both round and square buttons should have the same pressed shape."
  - Toggle color roles (unselected → selected):
    - Elevated: surface container low with primary → primary with on primary.
    - Filled: surface container with on surface variant → primary with on primary.
    - Tonal: secondary container with on secondary container → secondary with on secondary.
    - Outlined: outline variant outline with on surface variant → inverse surface with inverse on surface.
  - Toggle buttons have no text style.
  - "The elevated button style has an elevation of 1 by default and 0 when disabled."
- **Adaptive placement example:** "Filled buttons are end-aligned below flight information in a compact window" and "start-aligned beside flight information in a large window."

- **M3 Expressive changes:** added the toggle variant, the square shape, press and select shape morphs, and the XS, M, L and XL sizes. Color styles are now configurations. Small buttons use 16dp padding (24dp is "no longer recommended").

### Button groups
- **Use when / avoid when:**
  - Standard groups hold related buttons that "respond to one another".
  - Connected groups "help people select options, switch views, or sort elements". Use them only for single-select or multi-select toggle patterns: "Avoid using a connected group when none of the buttons can be toggled."
  - Connected groups replace the segmented button, which "is no longer recommended".
- **Variants and how to choose:**
  - Standard: the selected button changes shape and width, a selected toggle button also changes color, and the adjacent buttons move and change width for a moment.
  - Connected: only the selected button's own shape changes.
  - Both support all sizes (XS to XL), round or square shapes, and single-select, multi-select or selection-required.
- **Emphasis and color:**
  - The group has no color of its own. Use the filled, tonal, outlined or elevated button styles. "Avoid using standard icon buttons or text buttons, as they have no container treatment."
  - Give primary actions more emphasis through size, color or shape.
  - Connected groups: "Avoid mixing color styles".
- **Layout and placement:**
  - In a standard group, all buttons share one size and one shape by default. "Only use multiple sizes in a group for hero moments", and use a different shape only for a selected button or "to add meaning or contrast".
  - The standard group hugs its buttons.
  - The connected group spans the width of its surface. Consider a maximum width in large windows.
  - Padding between buttons: standard groups use 18dp (XS), 12dp (S) and 8dp (M to XL). Connected groups use 2dp.
- **Behavior (scroll, adaptive, motion):**
  - Groups stay on a single line and never wrap to a second line. Groups can be stacked, but they "don't interact vertically".
  - Resizing is fixed or flexible. Don't stretch icon buttons beyond the wide width.
  - Use smaller, narrower buttons in compact windows and larger, wider buttons in large windows.
  - The primary action must stay the most prominent button at every breakpoint.
  - Trailing buttons can collapse into an overflow menu placed at the trailing end of the group.
- **Do / Don't:**
  - DO: "Use the same shapes for buttons in a group, but change other properties like width and color"
  - CAUTION: "Reserve shape differences in button groups for key interactions"
  - DON'T: "Don't mix color styles in connected button groups"
- **Accessibility:**
  - Each button needs at least a 48x48dp target. Don't reduce the XS and S padding.
  - The container isn't focusable and needs no label. Focus goes to the first button.
- **M3 Expressive changes:** new component (May 2025). Adds the standard and connected groups, with the connected group replacing the segmented button. Supports all sizes and applies a default shape to every button.

### Split button
- **Use when / avoid when:** use it to show "an action with a menu of related actions". It "reduces visual complexity by hiding extra options". It can stand alone or sit next to common buttons, icon buttons and button groups.
- **Variants and how to choose:**
  - One variant, in 5 sizes (XS, S default, M, L, XL). It can be a different size from other buttons on the page.
  - Color styles: elevated, filled, tonal, outlined. There is no text style.
  - The leading button has an icon, a label or both, with a label of "just one or two words". The trailing button always shows the menu icon.
- **Emphasis and color:** uses the button color schemes. Selection doesn't change the color, "only a state layer is applied". "Scale up the split button in large breakpoints, or to create more emphasis in smaller windows" (for example in hero moments).
- **Layout and placement:**
  - Align the menu with the trailing button. If there isn't room, align it to one edge of the button. Place the menu 4dp from the split button.
  - The gap between the two halves is always 2dp.
  - Mirror the layout in RTL languages.
- **Behavior (scroll, adaptive, motion):** the menu icon rotates 180° inward and the button morphs shape. It uses the standard motion scheme, not the expressive one. It usually opens a menu, but it can open other components such as cards.
- **Do / Don't:**
  - DO: "Open a menu from a split button"
  - DON'T: "Avoid modifying the menu in unusual ways"
  - DON'T: "Avoid using very long labels or changing the trailing icon"
- **Accessibility:**
  - Each half needs a 48x48dp target. The XS and S sizes need taller target areas.
  - Focus goes to the leading button first, then the trailing button.
  - The trailing button's label describes the expanded or collapsed state and says more options exist (for example "More watch options").
- **M3 Expressive changes:** new component (May 2025), in 5 sizes and 4 color styles, with a menu button that spins and changes shape.

### Icon buttons
- **Use when / avoid when:** use icon buttons for common actions with "a system icon with a clear meaning". Default icon buttons can open other elements such as menus or search. Toggle icon buttons are for binary on/off states (favorite, bookmark). "Only use a few icon buttons at once." In dense layouts, group them in a toolbar or button group.
- **Variants and how to choose:**
  - Default and toggle.
  - Sizes: XS 32dp, S 40dp (default), M 56dp, L 96dp, XL 136dp.
  - Widths: narrow, default, wide. Shapes: round and square.
  - Use size and width for hierarchy. "When buttons have a similar importance, they should be the same size."
- **Emphasis and color:**
  - Order of emphasis: filled, tonal, outlined, standard.
  - Filled is for "key actions that require high emphasis". Use it sparingly.
  - Tonal is the middle ground, for secondary actions next to a high-emphasis action (for example Raise hand next to a filled End call).
  - Outlined is medium emphasis, for buttons that aren't "the main focus", such as browsing cards.
  - Standard is low emphasis, or for buttons on a colorful surface.
  - Use a styled container when the button needs separation from the background.
- **Layout and placement:** place icon buttons directly on the background or inside cards, app bars and toolbars.
- **Behavior (scroll, adaptive, motion):**
  - Buttons morph when pressed. A toggle button changes from round to square when selected (a square button changes to round).
  - Default icon buttons use filled icons. Toggle icon buttons go from outlined to filled, or to semibold (then bold) weight if no filled icon exists, so "selection is communicated through at least two properties, rather than just color."
  - The hover tooltip describes the action, not the name of the icon.
- **Do / Don't:**
  - DO: "Use icons with a background to make them easy to see on any surface"
  - DO: "When mixing button variants, use color styles to make the primary action clear"
  - DO: "Use toggle icon buttons when the icon can be selected"
  - DON'T: "Don't use toggle icon buttons for actions that don't have a selected state, such as an icon button for an overflow menu"
  - DO / DON'T: keep a 3:1 contrast ratio. "Avoid using colors with contrast below 3:1"
- **Accessibility:**
  - Targets must be at least 48dp, even when nested.
  - "Don't apply density to icon buttons by default". Offer density only as an opt-in.
  - The label describes the action (for example "Add to favorites").
  - Show a tooltip on web.

- **Specs (shape and toggle color):**
  - Square corner radius by size: XS 12dp, S 12dp, M 16dp, L 28dp, XL 28dp. Pressed radius: XS 8dp, S 8dp, M 12dp, L 16dp, XL 16dp.
  - Toggle color roles (unselected → selected):
    - Filled: surface container with on surface variant → primary with on primary.
    - Tonal: secondary container with on secondary container → secondary with on secondary.
    - Outlined: outline variant outline with on surface variant → inverse surface with inverse on surface.
    - Standard: on surface variant icon → primary icon.
  - Other roles are allowed if the container and icon keep 3:1 contrast (for example tertiary and on tertiary).

- **M3 Expressive changes:** added the XS, M, L and XL sizes, the narrow and wide widths, the square shape and the shape morphs. Color styles are now configurations. Icon buttons inside button groups now interact with each other.

### FAB
- **Use when / avoid when:**
  - Use a FAB for "the most important action on a screen". It should be "constructive": Create, Favorite, Share, Start a process.
  - Don't use it for minor or destructive actions: archive or trash, alerts, limited tasks such as cut, or controls better suited to a toolbar.
  - "FABs are not needed on every screen."
- **Variants and how to choose:**
  - FAB is the smallest, best for compact windows that show other actions.
  - Medium FAB is the "most recommended", for compact and medium windows.
  - Large FAB is for a prominent action, best in expanded and larger windows.
  - "Use a medium FAB for mobile layouts, and large FAB for tablets and large screens."
  - Use the extended FAB when a label is needed, and the FAB menu when many related actions apply.
- **Emphasis and color:**
  - Six color styles: primary, secondary or tertiary container (primary container is the default), and plain primary, secondary or tertiary.
  - Surface FABs are no longer recommended.
  - The container must stand out from its background.
- **Layout and placement:**
  - Align it left, center or right, either above the navigation bar or nested in it.
  - Compact and medium windows: the lower-right corner. Expanded windows: the upper-left, in the navigation rail.
  - Don't cover the container with badges.
  - Use a filled icon. Don't show notifications or actions that appear elsewhere on the screen.
- **Behavior (scroll, adaptive, motion):**
  - The FAB expands from its center when it appears.
  - It stays in place when content scrolls.
  - It disappears and reappears during screen and tab transitions, reappearing only if relevant and in the same spot.
  - It can container-transform into any surface, or change into a FAB menu.
- **Do / Don't:**
  - DON'T: "Don't display multiple FABs on a single screen"
  - DO: "Use FABs for primary, positive actions"
  - DON'T: "Don't use FABs for minor, overflow, unclear, or destructive actions"
  - DO: "Use clear and simple icons such as add, message, or edit"
  - DON'T: "Don't use confusing or open-ended icons"
  - DO: "A FAB can be used within a navigation component, such as a navigation rail"
  - DON'T: "Individual components, such as cards, shouldn't have their own FAB"
  - DO: "The FAB should disappear and reappear when switching pages"
  - DON'T: "Don't keep the FAB on screen when switching pages"
- **Accessibility:**
  - "Don't disable the FAB". Hide it if the action is unavailable.
  - The icon needs 3:1 contrast with the container.
  - Prioritize the FAB in focus order.
  - Don't fully hide another element's focus indicator.
  - The label describes the action ("Compose a new message").

- **State layer:** "When using a non-default color mapping for FABs, make sure the state layer color is the same as the icon color." (The extended FAB spec says the same.)

- **M3 Expressive changes:** added the medium FAB. The small FAB and surface FABs are no longer recommended. Variants are now based on size, not color. Added the primary, secondary and tertiary styles, and renamed the old styles to "container".

### Extended FAB
- **Use when / avoid when:**
  - Use it on long, scrolling screens that need persistent access to the primary action (for example checkout). Use it when a label helps people understand the action or adds emphasis.
  - "Only one extended FAB should be used per screen."
  - "The extended FAB shouldn't be used as an option in a set of actions". Use filled buttons there instead.
- **Variants and how to choose:**
  - Sizes: small 56dp, medium 80dp, large 96dp.
  - Large can suit compact windows with one prominent action. Use medium or large at larger breakpoints.
  - The label is at most 1–2 words. The icon is optional, but it can't have an icon without a label.
- **Emphasis and color:** it is "more prominent than regular FABs". It uses the same six color styles as the FAB. Surface styles are not recommended.
- **Layout and placement:**
  - The container hugs its contents. Margins are 16dp.
  - Compact and medium windows: at the bottom, centered or at the trailing edge.
  - Expanded windows: at the bottom right (in both LTR and RTL) or inside the navigation rail.
  - Don't show other floating components with it: "Floating toolbars can be paired with FABs, but not extended FABs".
  - Mirror the layout in RTL languages.
- **Behavior (scroll, adaptive, motion):**
  - Expands when it appears. Can container-transform into any surface.
  - Collapses to a FAB when scrolling down and expands when scrolling up.
  - Changes to a FAB when a navigation rail collapses, and back when the rail expands.
- **Do / Don't:**
  - DO: "Only show one prominent action at a time with the extended FAB"
  - DON'T: "Don't use multiple extended FABs in one screen as it disrupts visual hierarchy"
  - DO: "Use a button with appropriate styling to emphasize it in a group of buttons"
  - DON'T: "Don't use the extended FAB to convey an option in a set of actions"
  - DO: "Unlike standard FABs, extended FABs don't require an icon"
  - DON'T: "An extended FAB can't have an icon without a text label"
  - DON'T: "Avoid wrapping or truncating text"
  - DON'T: "Don't place the extended FAB on top of toolbars"
  - DON'T: "Don't place the extended FAB in the upper half of a mobile screen"
  - DON'T: "Don't place extended FABs on cards or inside other containers"
  - DON'T: "Don't place extended FABs over another actionable element"
- **Accessibility:**
  - The icon and label form one focus stop. No tooltip is needed.
  - Prioritize it in focus order.
  - The accessibility label must start with the visible label's first word.
- **M3 Expressive changes:** added the small, medium and large sizes with larger type (small uses title medium). The baseline 56dp extended FAB and the surface style are no longer recommended.

### FAB menu
- **Use when / avoid when:**
  - Opens from a FAB to show "2–6 related actions" that are "closely related under a single action, like **Share**".
  - Replaces the speed dial and stacked small FABs.
  - "Don't open a FAB menu from an extended FAB or any other component."
  - Don't use it when the FAB is paired with a floating toolbar or navigation rail.
- **Variants and how to choose:** one menu size that works with any FAB. Pair it with a FAB size that suits the window size class.
- **Emphasis and color:** primary, secondary and tertiary color sets. Match the FAB: a primary or primary container FAB uses the primary set, and so on. The close button and items use contrasting colors.
- **Layout and placement:**
  - Align it to the trailing edge (the left in RTL, mirrored).
  - The menu appears where the FAB was, anchored to the FAB's top trailing corner.
  - Margins are 16dp, or 24dp in large and extra large windows.
  - The close button is 56dp. Items use the medium button specs.
  - On web, it uses a menu component with a 4dp gap.
- **Behavior (scroll, adaptive, motion):**
  - The FAB changes into the close button, and the items use the enter and exit transition from the top trailing corner.
  - In short windows, items scroll behind the close button.
  - An item can container-transform into any surface.
- **Do / Don't:**
  - DO: "FAB menus can have 2-6 items"
  - DON'T: "Don't use a FAB menu with one item"
  - DO: "FABs can be placed next to toolbars and other components"
  - DON'T: "Don't use a FAB menu with a toolbar or navigation rail"
  - CAUTION: "Only remove the icon if necessary."
  - DON'T: "Don't remove the label"
  - DON'T: "Don't expand container sizes"
  - DON'T: "Don't change FAB menu shapes"
  - DON'T: "Don't obstruct the close button in short screens"
- **Accessibility:**
  - Initial focus stays on the close button, then moves from the top item to the bottom.
  - Android close button: label "Toggle menu", role Button, state expanded or collapsed.
  - Item labels match their UI text.
- **M3 Expressive changes:** new component (May 2025) that replaces the speed dial and stacked small FABs. It supports dynamic color and works with any FAB color style.

## Bars and navigation

### App bars
- **Use when / avoid when:** Use to show "content and actions related to the current page, such as page navigation actions, headlines, images, and 1–2 essential actions." It can also hold global controls such as search or notifications. It "should only have one action, two if necessary"; the primary action "should alter or exit the entire page, like Send, Save, or Edit." If there are many actions, "place those in a toolbar. Avoid placing an overflow menu in the app bar when possible."
- **Variants and how to choose:** Search app bar: "Use on home pages when search is key to the product." Small: "Use in dense layouts or when a page is scrolled." Medium flexible: shows a larger headline and "can collapse into a small app bar on scroll." Large flexible: "Use to emphasize the headline of the page." Baseline medium and large are no longer recommended; use the flexible versions instead.
- **Emphasis and color:** Container is surface, then surface container on scroll. To boost the primary action, make it a filled or tonal icon button, optionally wide, and use only one. Prefer filled icons. The search container defaults to surface container; on darker backgrounds use a lighter role such as surface bright, keeping at least 3:1 contrast between search text and container.
- **Layout and placement:** Full window width, default height, straight corners. Up to 2 trailing icon buttons, with the most used placed closest to the leading edge. The leading button is a menu (opens a modal expanded nav rail) or back. Headlines are leading-aligned or centered and never truncated. Only the medium and large flexible bars wrap, to 2 lines at most. Headline type: search Body large, small Title large, medium flexible Headline medium, large flexible Display small. On the search app bar, placeholder text always includes "Search". It takes up to 2 trailing icons plus an avatar on mobile and up to 4 on large screens. The leading slot can hold a logo, but "Avoid using a logo to open an expanded navigation rail."
- **Behavior (scroll, adaptive, motion):** The bar starts in the background color and fills on scroll. It can stay fixed or hide and reappear. It can also go transparent on scroll so buttons float, in which case icon buttons need a container fill. Medium and large flexible bars compress to small and stay small until scrolled back to the top. "Don't transform app bars into a search app bar." Selecting the search bar opens search view. Trailing actions collapse into overflow at small sizes. The search field fills 100% of the free space up to 312dp, then 50% of it. Layout mirrors in RTL.
- **Do / Don't:**
  - DO: "Use a filled or tonal button for important actions"
  - DON'T: "Don't put multiple filled or tonal buttons in the app bar"
  - DON'T: "Don't use three icons and an avatar in a search app bar"
  - DO: "Use straight corners for app bars"
  - DON'T: "Don't use curved shapes. This implies that the container can expand upon interaction."
  - DON'T: "Don't make an app bar shorter than its default height"
  - DO: "If headline text is long, use a medium flexible or large flexible app bar and wrap the headline to two lines maximum"
  - DON'T: "Don't wrap text in a small app bar"
  - DO: "Use filled icons for clear, visible actions"
  - CAUTION: "Outlined icons can be used as needed, or when using toggle buttons"
- **Accessibility:** Initial focus lands on the leading button. Tab moves between items; Space or Enter activates. The title's label matches its text, with context added if needed. Actions stay accessible when content scrolls. Search bar and label keep at least 3:1 contrast.

- **Subtitle and chip row:**
  - Subtitle type: small Label medium, medium flexible Label large, large flexible Title medium.
  - Subtitles are leading-aligned or center-aligned with the headline. Medium and large flexible bars hug their text, so they get taller when a subtitle shows.
  - "Avoid customizing the size of the heading and subtitle, or adding too many actions."
  - App bars "Can animate on and off screen with another bar of controls, like a row of chips."

- **M3 Expressive changes:** Renamed from "top app bar" to "app bar". Adds the search app bar. Medium flexible and large flexible replace medium and large, with reduced height, larger titles, a subtitle, left or center alignment, text wrapping, and room for images and filled buttons. Small gains a subtitle, centered text (center-aligned is merged into small), and images or filled buttons.

### Toolbars
- **Use when / avoid when:** Use to show "actions related to the current page"; it "can scale to show more actions in larger windows." When actions don't fit, add a menu. Cross-component rule: toolbar and navigation bar sit at the bottom "so should not be shown at the same time. Show the navigation bar on primary pages, and toolbars on subsequent pages with actions." Floating toolbars can act as tabs between related subsequent pages (local navigation).
- **Variants and how to choose:** The docked toolbar spans full width and is "best used for global actions that remain the same across multiple pages." The floating toolbar floats above content and is "best used for contextual actions relevant to the body content or the specific page." The bottom app bar is not recommended; use the docked toolbar.
- **Emphasis and color:** Standard (surface container) is "a low-emphasis color scheme best used for focusing attention on the body content." Vibrant (primary container) is "a high-emphasis color scheme that draws attention to the controls," and can also signal a temporary mode such as edit mode. Emphasize one action only, using filled, tonal, or standard icon button styles, custom roles, wide or narrow buttons, or a paired FAB. Floating toolbars have elevation by default, which can be removed over visually distinct content.
- **Layout and placement:** Default height is 64dp. Keep at least 16dp outside padding; 32dp between items is only the default. Targets are at least 48x48dp. Docked toolbars go only at the bottom of the window, never alongside other bottom-aligned elements such as a nav bar. Horizontal floating toolbars keep a 16dp margin; vertical ones (larger breakpoints, either side) keep 24dp and use narrow or default icon buttons, not wide. Place a vertical toolbar opposite the nav rail and use the rail's centered configuration. Floating toolbars must stay fully on screen, with extras going into overflow. Avoid square icon buttons in floating toolbars (fine in docked). A FAB can sit next to a floating toolbar to carry the highest-priority action.
- **Behavior (scroll, adaptive, motion):** Docked toolbars stay on screen or animate off. Floating toolbars can also collapse to a single high-emphasis action (on Compose, a FAB or key action), but "Don't collapse actions and scroll at the same time." Docked items are evenly spaced in compact windows. In medium and up, center them, or center a key action and push the rest to the edges. On web and large screens the docked toolbar may be rounded and use dividers. A floating toolbar hugs its items up to the 16dp margin, with trailing overflow. Vertical toolbars aren't recommended in compact windows. Multiple toolbars are fine in large windows but not compact ones. In RTL, flip the order when order matters.
- **Do / Don't:**
  - DO: "Keep navigation distinct, and use a toolbar to display local navigation on a specific page"
  - DON'T: "Don't show a navigation bar and a toolbar with navigation controls at the same time"
  - DO: "Use straight corners for docked toolbars"
  - DON'T: "Avoid modifying the container shape"
  - DON'T: "Don't overwhelm people with too many controls"
  - DO: "Choose the most essential actions to show on screen by default"
  - DON'T: "Floating toolbars shouldn't exceed the edge of the window or pane"
  - DON'T: "Don't emphasize multiple buttons with bold, primary colors, such as a button and FAB together. Emphasize one action at a time."
  - DON'T: "Avoid mixing too many different controls in the same toolbar. A consistent control design keeps things clear."
  - DON'T: "Don't use square filled icon buttons in floating toolbars"
  - DON'T: "Using wide buttons with vertical toolbars can unnecessarily widen toolbar containers and hide other UI elements"
  - DON'T: "Don't add extra space to a toolbar beyond its necessary items"
  - CAUTION: "Vertical toolbars can cover important content in compact windows"
  - DON'T: "Avoid using multiple toolbars in smaller windows"
  - DON'T: "Toolbars shouldn't both collapse and transition off page"
- **Accessibility:** Focus starts on the first interactive element. Tab or arrow keys move between items; Space or Enter activates. Controls stay reachable when content is scrolled or the toolbar is collapsed. Use the web "toolbar" role; on mobile a generic container is fine.
- **M3 Expressive changes:** Adds the docked toolbar (shorter, standard or vibrant, more flexible) to replace the bottom app bar. Adds the floating toolbar: horizontal or vertical, standard or vibrant, holds many elements, and can pair with a FAB.

### Navigation bar
- **Use when / avoid when:** For "three to five main pages," "mobile or tablet only," in compact and medium windows. It "shouldn't be used for accessing single tasks." For more than 5 destinations, use tabs or a modal expanded nav rail behind a menu icon. For fewer than 3, use tabs. Not for desktop: use a nav rail or tabs there.
- **Variants and how to choose:** Flexible navigation bar, which replaces the baseline bar. Use vertical items (icon above label) in compact windows and horizontal items (icon beside label, inside the indicator) in medium windows.
- **Emphasis and color:** Container is surface container. Active indicator is secondary container, active icon on secondary container, active label secondary, inactive items on surface variant. Active items get a filled icon (or semibold if no filled version exists); inactive items are outlined. Icons need at least 3:1 contrast with the container.
- **Layout and placement:** Always at the bottom and full window width. Vertical items stretch equally; horizontal items have a fixed width, stay centered, and get outer margins. Labels are required and 1 to 2 words. Destinations have fixed positions. Badges sit at the icon's upper right. The FAB goes above the bar, right-aligned, and never covers it. Dialogs, sheets, drawers, or the keyboard may cover the bar temporarily but never permanently. Cross-component rules: compact uses a nav bar or modal nav rail; medium uses a nav bar or nav rail; expanded and up uses a nav rail. Never pair with a docked toolbar.
- **Behavior (scroll, adaptive, motion):** Switching uses a top-level transition, and each app chooses whether to preserve or reset state. Re-selecting the active item scrolls to the top. No swiping between destinations. The bar can hide on scroll, "Don't hide the navigation bar on scroll when a screen reader is active." The indicator expands from the icon center on one axis only.
- **Do / Don't:**
  - DON'T: "Avoid putting more than five navigation items in a navigation bar"
  - DON'T: "Don't remove the labels from navigation items"
  - DON'T: "Don't use a navigation bar for fewer than three destinations. Instead, use tabs."
  - DON'T: "Navigation bar destinations have fixed positions. Don't scroll them or modify their positions."
  - DO: "Use filled icons when the navigation item is active"
  - CAUTION: "If a filled version of an icon is unavailable, the icon's weight must increase"
  - DON'T: "Don't use multiple or low-contrast colors in a navigation bar..."
  - DO: "Use the active indicator only for the active destination"
  - DON'T: "Don't use the active indicator for more than one destination at a time"
  - DON'T: "Don't wrap or truncate text as it can make the label hard to understand"
  - DON'T: "Don't shrink longer text to fit on a single line"
  - DO: "The FAB should be right-aligned above the navigation bar"
  - DON'T: "Don't cover the navigation bar with a FAB"
  - DON'T: "Don't use navigation bars for desktop layouts. Instead, use a navigation rail or tabs."
- **Accessibility:** The bar grows vertically for large text. Full labels stay visible up to 2x text size and may wrap. Selected items use a bold label, unselected a medium label. Focus starts on the first item; Tab moves, Space or Enter selects. Use a more descriptive accessibility label when the visible one is ambiguous ("Library" becomes "Music library").
- **M3 Expressive changes:** Baseline bar no longer recommended. The flexible bar is shorter and supports horizontal items in medium windows. The active label changed from on surface variant to secondary.

### Navigation rail
- **Use when / avoid when:** Medium through extra-large windows, with 3 to 7 destinations plus an optional FAB. "Compact windows should always use a navigation bar"; in medium windows with few destinations, consider a nav bar. "Never use the navigation rail and navigation bar simultaneously." It "should be the only visible navigation element." Tabs can be added alongside it for an extra layer of navigation.
- **Variants and how to choose:** The collapsed rail replaces the baseline rail. It runs along the leading edge and "should not be hidden." The expanded rail replaces the navigation drawer and always opens from a menu icon. It can show secondary destinations. Standard expanded sits beside content, for large windows with space. Modal expanded overlaps content, for dense layouts or many items. The expanded rail can be hidden entirely in immersive experiences. Choose standard or modal based on horizontal space and the number of destinations; with more than 5 destinations, consider modal expanded.
- **Emphasis and color:** Container fill (surface container) is optional. If it's removed, keep at least 3:1 contrast. Active items use secondary container, a filled icon, and a more prominent color. Use no more than 2 colors for destinations.
- **Layout and placement:** Always vertical, always on the leading edge, outside any panes. Items align to the top or center (center on tablets). The menu and FAB are always top-aligned. The nested FAB has elevation 0. A logo is allowed but must not act as the expand button. Labels are one word; break or hyphenate rather than wrap. In expanded rails, the indicator hugs its content (it can be overridden to fill), the target spans the full width, and badges sit next to the label. An optional divider goes on the edge next to content.
- **Behavior (scroll, adaptive, motion):** Destinations stay fixed during vertical scroll. With horizontal scroll, the rail can stay or leave; use a divider or level 1 elevation to separate it. On expand, content reflows and the FAB becomes an extended FAB. The menu icon changes to show the collapse action. Rail and bar swap between large and small screens. Selection uses a top-level transition with the indicator expanding from the icon center. Predictive back applies only to the modal expanded rail.
- **Do / Don't:**
  - DON'T: "Don't use the navigation rail horizontally. Use a navigation bar instead."
  - DO: "A top-aligned FAB in the navigation rail"
  - DON'T: "Avoid placing the FAB below navigation items"
  - CAUTION: "Use caution when placing logos in the rail where they might be confused with an action or destination"
  - DO: "Use the active indicator only for the current open page"
  - DON'T: "Don't use the active indicator for more than one navigation item at a time"
  - DO: "Write clear and concise labels that describe the destination page"
  - CAUTION: "Break up longer phrases into two text lines if necessary"
  - DON'T: "Don't truncate or display an ellipsis in place of label text"
  - DON'T: "Don't reduce the type size to fit more characters into a destination label"
  - DON'T: "Don't use more than two colors for destinations or low-contrast colors in the navigation rail..."
- **Accessibility:** Full labels stay visible up to 2x text size, and items grow vertically to fit. Focus starts on the first item (menu, FAB, or first destination), then Tab or arrow keys move and Space or Enter selects. The active item uses a filled icon (or semibold). Use a descriptive accessibility label when the visible one is ambiguous.
- **M3 Expressive changes:** Baseline rail no longer recommended. Adds the collapsed and expanded rails, which match visually and transition into each other. Expanded supports modal and non-modal modes and can transition to collapsed or hide. The active label on vertical items changes from on surface variant to secondary.

### Navigation drawer
- **Use when / avoid when:** "The navigation drawer is no longer recommended in the Material 3 Expressive update... use an expanded navigation rail, which has mostly the same functionality of the navigation drawer and adapts better across breakpoints." Legacy guidance: use it for 5 or more top-level destinations, 2 or more hierarchy levels, or quick switching between unrelated destinations. Don't combine it with other primary navigation such as a nav bar.
- **Variants and how to choose:** The standard drawer is for expanded, large, and extra-large windows. It can be permanently visible (for frequent switching) or dismissible via a menu icon (content-first). The modal drawer uses a scrim, can appear at any breakpoint, is mainly for compact and medium, and always opens from an outside action.
- **Emphasis and color:** Container is surface container low. Active indicator is secondary container. Modal drawers add a scrim.
- **Layout and placement:** A list inside a side sheet, placed on the start edge. Width is 360dp. Icons are optional, but use them on all items or none. Use dividers between groups, not between individual items. Short section labels can group related destinations. Put the most frequent destinations at the top. When paired with a rail, a modal drawer may repeat the rail's destinations if the hierarchy levels are clearly separated. On web below 320 CSS px, swap to a nav bar.
- **Behavior (scroll, adaptive, motion):** The drawer scrolls independently while body content stays still. Modal drawers dismiss on item select, scrim tap, or a swipe toward the anchor edge. Uses the enter and exit transition. Transition between components when swapping (rail to drawer).
- **Do / Don't:**
  - DO: "Use a navigation drawer for 5 or more primary destinations, or more than 1 level of navigation hierarchy"
  - CAUTION: "Avoid using two navigation components on the same screen"
  - DO: "Use full-width dividers (1) to separate groups of destinations"
  - DON'T: "Don't use dividers to separate individual destinations"
  - DO: "Keep text labels concise, but truncate them if they extend beyond the container width"
  - DON'T: "Don't wrap label text"
  - DON'T: "Don't shrink text size in order to fit a text label on a single line"
  - DO: "Use recognizable icons when conventions exist"
  - DON'T: "Don't apply icons to some destinations and not others. Icons should be used for all destinations, or none."
- **Accessibility:** Focus starts on the first item; arrow keys move and Space or Enter selects. The scrim closes a modal drawer. Selected items use a filled icon.
- **M3 Expressive changes:** No longer recommended; use the expanded navigation rail.

### Tabs
- **Use when / avoid when:** "Tabs organize groups of related content that are at the same level of hierarchy." Use tabs "to group related content, not sequential content." Also use them instead of a nav bar when there are fewer than 3 destinations, and alongside a nav rail.
- **Variants and how to choose:** Primary tabs sit "at the top of the content pane under an app bar" and show the main content destinations; use them when there is a single set of tabs. Secondary tabs sit within a content area, are "necessary when a screen requires more than one level of tabs," always sit below primary tabs, and use a simpler indicator. Fixed tabs show all tabs at once, for quick switching. Scrollable tabs are for sets that don't fit or have long labels.
- **Emphasis and color:** Active tabs get an underline plus a color change on text and icon (primary on primary tabs). Inactive tabs are on surface variant. Divider is outline variant.
- **Layout and placement:** The container spans full width in equal sections, with a bottom divider. Height is 48dp for text only and 64dp with icons. Use icons on all tabs or none, and use icons alone only if they are globally recognized. Labels stay short; a second line is allowed with truncation. For scrollable tabs, offset the first tab 52dp from the leading edge and keep padding consistent. Avoid more than 4 fixed tabs. Size fixed tabs to the widest one, with fluid margins, aligned center or leading. Badges max out at 4 characters (including "+") and clear once viewed.
- **Behavior (scroll, adaptive, motion):** Fixed tabs support tap or swipe in the content area; avoid swipeable content inside. On scroll, tabs stay fixed or scroll off and return when scrolling up. When attached to an app bar, they move with it as one unit.
- **Do / Don't:**
  - DO: "Utilize tabs to categorize related groups of content into clearly defined sets"
  - DON'T: "Don't use tabs to move through sequential content that needs to be read in a particular order..."
  - DO: "Use icons that are globally recognized when using icons alone"
  - DON'T: "Don't use tabs with both icons and text labels on only some tabs, but not others"
  - DO: "Offset the first scrollable tab 52dp from the leading edge so it's clear that more content is available"
  - DON'T: "Don't truncate labels unless required, as truncated text can impede comprehension"
  - DO: "Use different gesture directions when using tabs"
  - DON'T: "Avoid placing swipeable items in the content area of a UI that has tabs..."
  - DO: "Tabs can scroll offscreen on scroll, and reappear when the page is scrolled up"
  - DON'T: "Don't scroll tabs behind an app bar. When tabs are attached to a component, they should appear and move as a single unit."
  - DO: "Use Arrow/Tab to navigate through items"
  - DON'T: "Don't use Space/Enter for navigating tabs. Space/Enter is only used for completing actions."
- **Accessibility:** Never loop tabs infinitely, since it traps screen reader users. Don't apply density by default; keep targets at 48x48 CSS px. Use descriptive labels for icon-only tabs.
- **M3 Expressive changes:** None stated in the source.

## Containment

None of the six source files has an "M3 Expressive update" section. Their Overview tabs contain only "Differences from M2" and, for carousel, an update log. The Expressive heading is omitted throughout for that reason.

### Cards
- **Use when / avoid when:** "Use a card to display content and actions on a single topic." Cards can be "entry points into deeper levels of detail or navigation." They should be easy to scan, with a clear hierarchy.
- **Variants and how to choose:** There are three variants: elevated, filled and outlined. "Each provides the same legibility and functionality, so the variant you use depends on style alone."
- **Emphasis and color:**
  - Filled (surface container highest) gives "subtle separation... less emphasis than elevated or outlined."
  - Elevated (surface container low, with shadow) gives "more separation from the background than filled cards, but less than outlined cards."
  - Outlined (surface plus outline variant) "can provide greater emphasis than the other variants."
  - M3 uses "lower elevation and no shadow by default."
- **Layout and placement:**
  - Specs: 12dp corners, 16dp side padding, 8dp max between cards. The container is the only required element.
  - Full-width dividers mark expandable content. Inset dividers separate related content. Overflow menus go upper-right or lower-right.
  - Collections come as a grid (staggered or mosaic), a vertical list or a carousel. They are coplanar at rest.
  - Filters "must apply to each card" and are "placed outside of the card collection."
- **Behavior (scroll, adaptive, motion):**
  - Use container transform for expansion, reserved "for hero moments." Forward/backward transitions are for common navigation.
  - Allow one swipe action per card.
  - On mobile, content taller than the maximum card height is truncated and cards never scroll internally. On desktop they can.
  - Adaptive: on expanded screens, go from a horizontal card to a larger vertical one and use multiple columns. "Avoid extending UI elements across the screen." At compact sizes, "consider swapping cards for lists."
- **Do / Don't:**
  - DO "Cards can be shown together."
  - DON'T "Don't force content into cards when spacing, headlines, or dividers would create a simpler visual hierarchy."
  - CAUTION "Ensure that text on images meets accessible contrast standards." CAUTION "consider using a bounding shape."
  - DO "Expand a card to reveal information." DON'T "Don't scroll within a card to reveal information."
  - DON'T "Cards shouldn't contain content that can be swiped, such as an image carousel or pagination."
  - DO "When moving a card, increase its elevation." DON'T "Don't let cards bump other elements out of the way."
- **Accessibility:**
  - A card is either a non-actionable container holding actions or directly actionable with none: "An action shouldn't be placed on an actionable surface."
  - Non-actionable cards have no ripple, no hover and no tab stop.
  - Drag and swipe need a single-pointer alternative, such as a menu.
  - Hide decorative images. Actionable cards take the button or link role.

### Carousel
- **Use when / avoid when:** Carousels show a scrollable list of visual items with brief text. For lots of text, "consider using the uncontained layout... or use a series of cards instead." Research found users expect "around 10 items" in a carousel that scrolls several items at once.
- **Variants and how to choose:**
  - Multi-browse: "browsing many visual items at once." Avoid it for heavy text or complex imagery.
  - Uncontained: "highly-customized or text-heavy... traditional carousel behavior."
  - Uncontained multi-aspect ratio: items range from 9:16 to 16:9. "Only use... if the items have various widths."
  - Hero: "spotlighting very large visual items."
  - Center-aligned hero: centered, large items.
  - Full-screen: "vertically-scrolling video or image feeds, immersive experiences." Portrait only, compact and medium breakpoints, never landscape.
- **Layout and placement:**
  - Specs: 16dp leading/trailing padding, 8dp gaps, 28dp item corners. Small items are 40 to 56dp wide. Full-screen uses 0dp padding and 16dp gaps.
  - Items must be fully visible, except in uncontained.
  - At compact sizes, show at most three items if they have text. A hero shows one large and one small item.
- **Behavior (scroll, adaptive, motion):**
  - Items scroll with parallax.
  - Default scrolling is for uncontained only. Snap-scrolling is for multi-browse, hero and full-screen, and full-screen "must use snap-scrolling."
  - Larger containers show more items. Full-screen always shows one.
- **Do / Don't:**
  - DO "Set the large carousel item size to ensure the images and text are easy to read and recognize." DON'T "Avoid setting carousel items so small that the image isn't recognizable."
  - CAUTION "At compact breakpoints, only show more than three items if the items are easy to understand and recognize."
  - CAUTION "Avoid exceeding two lines of text in carousel items at compact breakpoints unless the background is simple."
  - DON'T "Avoid scrolling freely on full-screen carousels."
  - DON'T "Avoid adding buttons into the carousel container or beside it. Place any buttons above or below the carousel." DON'T "Don't cover the carousel with buttons or other UI."
  - DO "Set initial focus on the first carousel item." DON'T "Avoid focusing on the carousel container."
- **Accessibility:**
  - On vertical pages, provide a "Show all" button below the carousel, or a 48dp arrow next to its header, that opens a vertical page of all items.
  - Labels announce the current item and the total.
  - Reduced motion removes parallax and makes all items the same size.

- **Item text and sizing:**
  - "Text should always be understandable at each size. Consider adapting the text to use brief labels on smaller carousel items." The large item shows the full title and label, the medium item hides the title, and the small item abbreviates the label.
  - A center-aligned hero "adds an additional previewed item on the leading edge, making the large carousel item centered" (two small items around a centered large item).
  - The large item's max width is adjustable. "When the large item's max width is narrow enough, more items can be shown on screen at once. At compact breakpoints, this is only recommended for carousels with simple imagery."

- **Choosing by task (research):** two factors decided which carousel users wanted:
  - "a deep browse versus grabbing a quick item" (news headlines vs recipe images)
  - "the weight of the commitment" (a movie means two hours of watching)
  - The "multi-swipe option with previews" helped with "grabbing an item and making a quick, non-critical decision." For a big decision, participants "wouldn't want to swipe through too much content at once, or to do so too quickly."
- Users were less certain how many items a vertical carousel or a hero (single very large item) carousel held. Horizontal single-advance and multi-advance variants did not have this problem.
- **Compose:** shape and outline items with `maskClip` and `maskBorder` (on `CarouselItemScope`), not `clip` and `border`. Plain `clip` clips the item statically and drops the parallax effect.

### Dialogs
- **Use when / avoid when:**
  - Use for "critical information that requires a specific user task, decision, or acknowledgement." Use them "sparingly."
  - DON'T use for low or medium priority; use a snackbar instead (low importance, optional action). A dropdown menu is the less disruptive alternative.
- **Variants and how to choose:**
  - Basic: alerts, quick selection, confirmation, and date or time pickers.
  - Full-screen: tasks with a series of steps, keyboard input, changes not saved instantly, or nested dialogs. "For compact breakpoints only."
- **Layout and placement:**
  - Basic dialogs are 280 to 560dp wide with 28dp corners and 24dp padding.
  - Keep "a maximum of two actions," aligned to the trailing edge with confirm closest to the edge. A single action must be an acknowledgement.
  - Headlines avoid apologies, alarm and "Are you sure?"
  - Full-screen dialogs have "Save" and use the close X as the only app bar navigation.
- **Behavior (scroll, adaptive, motion):**
  - Enter/exit transition. Use container transform from a FAB into a full-screen dialog.
  - When content scrolls, the title and buttons stay pinned.
  - At larger breakpoints, full-screen swaps to basic. Basic dialogs sit centered by default and can be custom-positioned within a 56dp edge margin.
  - Only full-screen dialogs can have other dialogs over them.
  - Show field errors inline. Show general errors in a basic dialog.
- **Do / Don't:**
  - DO "Disable confirming actions until a choice is made. Dismissive actions are never disabled."
  - DON'T "Don't place dismissive actions to the right of confirming actions."
  - CAUTION "Stacked buttons... Confirming actions appear above dismissive actions."
  - CAUTION on "Learn more": it "navigates away from this dialog."
  - DO "place longer headlines into the content area."
  - DON'T "Don't trigger a basic dialog when the confirming action is selected." DON'T "Don't use the confirming action to dismiss the full-screen dialog."
- **Accessibility:**
  - Initial focus goes to the first interactive element. Tab cycles through elements and Escape closes.
  - Headlines must fit 4 lines at 200% text size.
  - On web, basic dialogs use the "alert dialog" role.

- **Full-screen confirm, dismiss and errors:**
  - Label the confirmation by its outcome: "The confirmation action should be clear about what happens next, like **Send** or **Create**. Avoid using vague terms like **Done**, **OK**, or **Close**."
  - "Don't disable the confirmation button" in a full-screen dialog. This differs from basic dialogs, where confirming is disabled until a choice is made. "Only trigger an additional basic dialog if the action fails."
  - "When someone dismisses a full-screen dialog, a basic dialog should appear to confirm that they want to discard the unsaved changes" (keep editing or discard).
  - Close with the close icon or a dismissive action (Cancel, Back), and save with **Save**.
  - Errors say the source and how to fix it. "Show all errors on the page at once so people can fix everything before trying again." Field errors go inline. Add errors next to checkboxes and radio buttons, which have no built-in error text.
  - Instead of a third "Learn more" action, "an inline expansion can display more information. If more extensive information is needed, provide it prior to entering the dialog."
  - A full-screen dialog resets perceived elevation, so menus or dialogs over it cover the screen rather than floating.

### Bottom sheets
- **Use when / avoid when:** Use for supplementary (not main) content and actions at compact and medium breakpoints.
- **Variants and how to choose:**
  - Standard: coexists with the main UI and complements it (an audio player, info over a map).
  - Modal: blocks the app "like dialogs." Use it as an alternative to menus or simple dialogs for long action lists or items that need descriptions. "Used in mobile apps only."
- **Emphasis and color:** Surface container low, 28dp top corners. On Android, the system handles the scrim.
- **Layout and placement:**
  - Full width up to 640dp. Above 640dp, 56dp top and side margins.
  - Top margin 72dp at 640dp and below (the sheet never covers the top of the screen); drag handle centered with 22dp padding above and below.
  - A modal sheet's initial height is capped at 50% of the screen.
  - Show a close affordance when full-screen. A full-height standard sheet shows a collapse icon.
- **Behavior (scroll, adaptive, motion):**
  - Preset heights. Selecting the drag handle cycles heights or closes; tapping the scrim always closes.
  - Dismiss by tapping an item, tapping the scrim, swiping down or using close.
  - Scrolls independently of the page.
  - Predictive back detaches the sheet from the side edges.
  - On larger screens, the max width can be overridden. For complex flows, consider a floating sheet. On desktop, swap to a side sheet.
- **Accessibility:**
  - Top 48dp is the touch target.
  - The drag handle is focusable and toggles height on Space/Enter. Label only the drag handle, with role "button."
  - Provide a single-pointer alternative to dragging.

### Side sheets
- **Use when / avoid when:** Use for "optional content and actions without interrupting the main content," such as filters or supplemental info.
- **Variants and how to choose:**
  - Standard: medium and expanded breakpoints. Stays visible alongside the main content.
  - Modal: "preferred in compact breakpoints." It must be dismissed before the underlying content can be used.
  - A modal sheet can transition to a standard sheet on larger screens.
- **Emphasis and color:** Standard uses surface. Modal uses surface container low with 16dp corners. "Use elevation, fill, and tone to call attention to specific actions."
- **Layout and placement:**
  - Max width 400dp, 24dp padding, 16dp margin when detached.
  - Right edge by default, left edge in RTL with elements reversed.
  - Dividers separate actions from content, and user-generated from system content.
  - When a standard sheet opens, the body area shrinks to fit it.
- **Behavior (scroll, adaptive, motion):** Scrolls vertically and independently, never horizontally. Predictive back detaches the sheet from the top and bottom edges.
- **Do / Don't:**
  - DO "Place side sheets along the edge of the screen, usually on the right side... They can be slightly inset by 16dp."
  - DON'T "Don't inset a side sheet from the screen edges far beyond the recommended margin."
  - DO "Side sheets can vertically scroll internally." DON'T "Don't allow horizontal scrolling or lay out the side sheet in a way that suggests horizontal scrolling."
  - DO "A close icon button makes the side sheet easy to dismiss." DON'T omit it: "people can't predict the opening and closing flow."
- **Accessibility:** "Material requires that a close affordance... is always present." Role: Dialog.

- **Back and close affordances:**
  - Side sheets "can contain a back icon for navigation" (optional, at the upper leading corner) to exit or move to a different experience within the sheet. Because the main content stays visible, give people affordances for leaving the sheet.
  - The close icon button (upper trailing corner) "is highly recommended, increases accessibility, and makes focused side sheets easier to close."
  - Predictive back also scales the sheet and its content in the direction of the gesture and previews the previous screen.

### Divider
- **Use when / avoid when:**
  - "Make dividers visible but not bold."
  - "Only use dividers if items can't be grouped with open space."
  - "Use dividers to group things, not separate individual items."
  - Repetitive list items may need only margins.
- **Variants and how to choose:**
  - Full-width separates larger sections of unrelated content, or interactive from non-interactive areas.
  - Inset (16dp) separates related content within a section, anchored to icons or avatars.
  - Middle-inset (16dp both sides).
  - Vertical, for large-screen layouts.
- **Emphasis and color:** Outline variant.
- **Layout and placement:** When both kinds appear on one screen, use full-width for a different kind of content and inset for nested items, so they "reinforce the hierarchy."
- **Do / Don't:**
  - DO "Use full-width divider lines to separate interactive and non-interactive areas of a container such as a card."
  - CAUTION "Use full-width dividers sparingly. Too many divider lines will make an interface look cluttered."
  - DO "Use a combination of inset and full-width dividers to reflect the hierarchy of information." DO "Content may not require a divider line."
- **Accessibility:** "Dividers are decorative elements, which have no contrast minimums."

## Selection and input

### Chips
- **Use when / avoid when:** Chips "help people enter information, make selections, filter content, or trigger actions." Chips are not buttons: "Use chips to enhance a person's current journey and encourage action. Use buttons to progress them through the product and for significant actions." "Chips represent forking paths for a current task, while buttons represent linear steps." Chips appear in a set; buttons should number "no more than 3" in one arrangement.
- **Variants and how to choose:** Choose by purpose and author. Action: **assist** ("smart or automated actions that can span multiple apps", e.g. Add to calendar). Filter a collection: **filter**. User-authored information: **input** (e.g. Gmail contact in To). Product-authored suggestions: **suggestion** (e.g. suggested chat reply). Assist labels start with a verb and can update ("Save" to "Saved"); filter labels are nouns for what to **include** (avoid "Exclude images"); suggestion labels are nouns or short phrases. Filter chips are an alternative to segmented buttons, checkboxes, radio buttons or single-select menus; "avoid mixing chip set behaviors. All chip sets on a page should be either single-select or multi-select." Input chips are "a more flexible way to filter search results, compared to filter chips."
- **Emphasis and color:** Elevation defaults to 0; elevate only on images or dynamic backgrounds. Leading icon default is **primary**; **on surface variant** when less emphasis is wanted. Stroke softened from outline to **outline variant** (Aug 2024) "to improve visual hierarchy between chips and buttons."
- **Layout and placement:** 32dp tall, 8dp corners. Labels 20 characters or fewer, button typography, skip articles. Place inline in rows, not vertically; wrap, or scroll horizontally for one-row fields; if more than two rows, consider horizontal scrolling. 8dp minimum between chips; 48dp minimum target. A trailing action needs 48x48dp targets: chip min width 88dp (or 42dp label). Assist chips go after primary content (below a card or at the screen bottom). Filter chips pair with search fields; use a side sheet for many.
- **Behavior (scroll, adaptive, motion):** Selecting a filter chip adds a leading checkmark. Trailing remove/menu icons on filter chips suit medium and expanded windows; in compact, the whole chip should perform the action. Assist chips can transform into modals or full-screen views, and show progress and confirmation. Input chips are editable (revert to text), reorderable, movable between fields, and expand via container transform. Backspace before a chip selects it, second press deletes.
- **Do / Don't:** DO "Use chips to present contextual, supplemental options". DON'T "Avoid replacing major actions with chips. Actions that progress people to the next or previous step should always be displayed as buttons." DON'T "Avoid using chips to finish or progress a task". DON'T "Don't display a single chip by itself. Chips should appear in a set." DON'T "Filter chips should not present only a single option". DON'T "Chips shouldn't be elevated when placed directly on the page". DON'T "Avoid using elevation to indicate a chip's pressed state. Instead, use the visual ripple effect." DON'T "Avoid chip labels longer than 20 characters". CAUTION "In compact windows, make sure the whole chip opens the menu."
- **Accessibility:** Label needs 3:1 contrast. Action chips expose button semantics. Chips require a secondary interactivity cue: a group label ("Select type"), page context ("Filter results"), **outline** instead of outline variant, or an action label/leading icon. For overflow, offer a "Show all" reflow chip or menu (no menu method with remove icons). Don't apply density by default. Remove actions are labeled "Remove {chip content}".

### Checkbox
- **Use when / avoid when:** Select one or more options from a list, present sub-selections, "Turn an item on or off in a desktop environment", group similar options. Use "instead of switches if multiple, related options can be selected from a list": they group related items and "take up less space than switches."
- **Variants and how to choose:** Cross-control rule: checkboxes for "multiple related options in a list", radio buttons for "a single option in a list", switches for "standalone or more verbose options in a list, like settings." States: selected, unselected, indeterminate, plus error states for each.
- **Emphasis and color:** "Selected items are more prominent than unselected items." Adjacent labels use **on surface**, unchanged by selection or interaction.
- **Layout and placement:** 18dp box, 48dp target. In expanded breakpoints, group checkboxes in a contained region such as a side sheet.
- **Behavior (scroll, adaptive, motion):** Parent-child: checking the parent checks all children, unchecking unchecks all; partial selection makes the parent indeterminate, and checking it checks all. When used to turn something on or off, "the action should be immediately executed."
- **Do / Don't:** DO "Checkboxes let users select one or more options from a list. A parent checkbox allows for easy selection or deselection of all items." DON'T "If a list consists of multiple options, don't use switches. Instead, use checkboxes. Checkboxes imply the items are related, and take up less visual space."
- **Accessibility:** Selecting the label or the box toggles it. Accessibility label usually equals the adjacent text. Don't apply density by default (48x48 target).

### Radio button
- **Use when / avoid when:** "the recommended way to allow users to make a single selection from a list of options"; use to select one option and "Expose all available options." Use radio buttons (not switches) for single selection from a list. Use when there are five or fewer options; consider a drop-down menu when space is constrained, though menus "require additional steps for a person, both in the number of clicks and cognitive effort."
- **Variants and how to choose:** Single-select, unlike multi-select checkboxes. Switches and checkboxes are the alternatives for settings or preferences.
- **Emphasis and color:** Selected uses **primary**, unselected **on surface variant**. Labels use **on surface**, unchanged by state.
- **Layout and placement:** 20dp icon, 48dp target. "Radio buttons should be vertically listed and have one option always selected." Each option needs its own adjacent label.
- **Behavior (scroll, adaptive, motion):** Tapping the icon or label selects. "Radio buttons should take effect immediately, unless they're in a dialog or page that needs to be saved."
- **Do / Don't:** DO "Use radio buttons when only one option can be selected from a list". DO "Use checkboxes when multiple options can be selected from a list". DON'T "Don't nest radio buttons". DON'T "Don't allow radio buttons to select multiple options". DO "Use radio buttons when there are five or fewer options". DO "Consider using a drop-down menu instead of radio buttons when space is constrained". DO "Radio buttons should always have one option pre-selected". CAUTION "Avoid using horizontal radio button lists".
- **Accessibility:** A group can't be deselected once chosen; offer "Not applicable"/"No option" or "Clear selection." Tab enters at the selected (or first) radio; arrows move and select, wrapping. Group role **Radio group**, labeled by its title. No default density.

### Switch
- **Use when / avoid when:** "best used to adjust settings and other standalone options"; binary on/off, true/false. "The effects of a switch should start immediately, without needing to save." Switches control binary, not opposing, options; for opposing options (list vs map view) "Use a connected button group instead." Use switches (not radio buttons) when list items are independently controlled.
- **Variants and how to choose:** Configurations: no icon, icon on selected only, icon on both states. Cross-control rule as for checkbox: switches for "standalone or more verbose options in a list, like settings."
- **Emphasis and color:** "Make sure the switch's selection (on or off) is visible at a glance." Label **on surface**; supporting text may use **on surface variant**.
- **Layout and placement:** Track 52x32dp, 48dp target. Often stacked; settings screens are the common home. Always pair with an inline label describing what the switch controls when on.
- **Behavior (scroll, adaptive, motion):** The handle slides to the opposite end; handle grows when on (16dp to 24dp) and when pressed (28dp); hover area grows on cursor hover.
- **Do / Don't:** DO "Use a connected button group to choose between opposing options". DON'T "Avoid using switches to toggle between opposing options". DO "Use checkboxes (not switches) to let people select one or more options from a list". DON'T "A switch can't replace a button. People expect a call to action to be a button, not a switch." DO "Use radio buttons (not switches) when only one item can be selected from a list". DON'T "Avoid using a switch to select multiple options that require people to save. Switches should be immediate. Use checkboxes instead." DO "Use icons that clearly communicate whether the switch is on or off, such as an X and a checkmark". DON'T "Avoid using more ambiguous or non-binary icons, such as a moon or edit icon". DO "Keep labels short and direct. A label should describe what the control does when the switch is on." DON'T "Don't add label text into the switch; the font size would be too small to be accessible. Use an appropriate icon instead."
- **Accessibility:** Focus lands on the handle; Space or Enter toggles. Make ambiguous labels more descriptive (visible "Photo album", a11y "Photo album access"). No default density.

### Sliders
- **Use when / avoid when:** Select values along a track: volume, brightness, filter intensity. "Sliders should present the full range of available values." Changes "must take effect immediately."
- **Variants and how to choose:** **Standard**: one value, starting from zero or the start of a sequence. **Centered**: positive and negative range, zero or default in the middle. **Range**: two handles for min and max; keep horizontal. **Stops** configuration (formerly discrete) snaps to predetermined values; avoid too many stops.
- **Emphasis and color:** Sizes XS, S, M, L, XL (track 16, 24, 40, 56, 96dp); "Use larger sizes to increase the targets and provide a larger visual emphasis." "XL sliders should be reserved for hero moments, where the slider itself is the most important element on the page." Active and inactive tracks stay the same size.
- **Layout and placement:** Horizontal or vertical "depending on what is best for your use case." Inset icon only on standard M, L, XL; it moves to the inactive track at low values; consider swapping at zero (volume to mute). Icons or text at the ends can replace stop indicators. An external text field can show the value, synced both ways, tabbable right after the slider. RTL reverses direction.
- **Behavior (scroll, adaptive, motion):** Drag, tap track to jump, or keyboard. Handle narrows when pressed and the value appears (one value at a time on range sliders); tracks change shape at edges.
- **Do / Don't:** DO "Horizontal range slider". DON'T "Because of the additional cognitive load of a range slider, avoid using it in vertical orientation." DO "Inset icons change placement based on the handle". DON'T "Don't use an inset icon with sliders that have track thicknesses under 40dp". DON'T "Don't use an inset icon on a centered slider". DON'T "Don't use an inset icon on a range slider".
- **Accessibility:** End of inactive track needs 3:1 contrast (end stop indicators or icons provide it). Focus lands on the handle; arrows step, Space and arrows jump intervals, Home/End go to extremes. Role **slider**; outside stepper icons use button role.
- **M3 Expressive changes:** (May 2025) Continuous renamed **standard**; discrete became **stops** configuration; added vertical orientation, optional inset icon (standard only), sizes S to XL (XS is the existing default; S to XL are presets on MDC-Android, tokens elsewhere). Earlier (Dec 2023): centered and range added, new track/handle shapes, handle narrows on press.

### Text fields
- **Use when / avoid when:** "when someone needs to enter text into a UI"; common in forms and dialogs. Single-line fields "are not suitable for collecting long responses"; use multi-line or text area.
- **Variants and how to choose:** **Filled** and **outlined** have identical function; choice "can depend on style alone." Pick the one that fits the visual style, UI goals, and "Is most distinct from other components (like buttons) and surrounding content." Outlined has less emphasis, which "helps simplify the layout" in dense forms. If both are used, separate them by region, never within one form. Input modes: single-line (scrolls sideways), multi-line (grows, pushes content), text area (fixed height, scrolls; preferred on web).
- **Emphasis and color:** Stroke color and thickness change to show active state. Filled container **surface container highest**; focus uses **primary**.
- **Layout and placement:** 56dp height. Every field needs a label (or an adjacent label aligned to the leading edge); label always visible, never truncated or multi-line; it floats to the top on focus. Required fields: asterisk plus an explanation in supporting text or a form note. Supporting text ideally one line; add a counter for limits. Icons: signifier, valid/error, clear (only with input), voice, dropdown; images 24dp tall. Prefix/suffix for currency, units, domain. Read-only fields keep regular styling and are labeled read-only.
- **Behavior (scroll, adaptive, motion):** Full width in compact; in medium and expanded, bind with flexible margins or containers. Error text replaces supporting text to avoid layout shift; describe how to avoid the (most likely) error. Error icon "strongly recommended."
- **Do / Don't:** DO "When using both variants of text fields in a UI, separate them by region". DON'T "When using both variants of text fields, don't use both next to each other or within the same form". DON'T "Don't truncate label text. Keep it short, clear, and fully visible." DON'T "Label text shouldn't take up multiple lines". DO "Swap supporting text with error text". DON'T "Don't add error text in addition to supporting text, as their appearance will shift content". CAUTION "Long errors can wrap to multiple lines ... ensure padding between text fields is sufficient to prevent multi-lined errors from bumping layout content." DON'T "Don't use fixed text field margins on large devices. Text fields shouldn't span the full width of a large screen." DO "Make sure the container outline has a minimum contrast of 3:1 to the background". DON'T "Don't choose colors that won't pass Material's minimum contrast of 3:1".
- **Accessibility:** Errors get the "alert" role; trailing icon buttons get functional labels ("Show password"/"Hide password"); required labels include the asterisk; prefix/suffix need spoken names ("Euro"). No default density.

### Segmented buttons
- **Use when / avoid when:** Select options, switch views, or sort; "simple choices between two to five items (for more items or complex choices, use chips)." **No longer recommended in M3 Expressive; use the connected button group instead.**
- **Variants and how to choose:** **Single-select**: one option from up to five, switch views, sort (e.g. beverage size). **Multi-select**: selection optional, none to all (e.g. price range filter).
- **Emphasis and color:** Selected uses **secondary container**/**on secondary container** plus a checkmark; outline **outline**.
- **Layout and placement:** 2 to 5 segments; 40dp height, 48dp target, fully rounded. Keep adequate viewport margins; on large screens cap segment padding so it doesn't fill the width. Can sit on bottom sheets or full-screen dialogs. Density reduces height by 4dp per step.
- **Behavior (scroll, adaptive, motion):** With icon plus text, the icon becomes a checkmark when selected.
- **Do / Don't:** DO "Segmented buttons are best used for selecting between 2 and 5 choices". DON'T "Don't use more than five segments ... If you have more than five choices, consider using another component, such as chips." DO "Keep labels short and consistent in length". DON'T "Don't allow segments to wrap onto a new line". DO "Use consistent label types". CAUTION "Icons can be used in place of labels, but they must clearly communicate their meaning". DON'T "Avoid mixing icon-only labels with text labels." DO "Allow adequate space for margins. The button container shouldn't reach the edge of the viewport." DO "Set a maximum padding within the segments to ensure usability on larger screens". DON'T "Don't allow segmented buttons to span the full width of larger screens or panes." DO/DON'T outline contrast of at least 3:1.
- **Accessibility:** Selection shown by checkmark and color, never color alone. Single-select role **Radiogroup**, multi-select **Checkbox**. Icon-only segments need descriptive labels ("Inexpensive").
- **M3 Expressive changes:** (May 2025) "The segmented button is no longer recommended. Use the connected button group instead," which "has mostly the same functionality but with an updated visual design."

Notes: chips, checkbox, radio button, switch and text fields have no M3 Expressive section in the source. The checkbox page's keyboard table is a copy of the chips table (source error), so it was omitted.

## Lists, menus, and search

Source: m3.material.io scrapes of lists, menus and search (Overview, Specs, Guidelines, Accessibility). All three have an M3 Expressive update: expressive lists (December 2025), vertical menus (November 2025) and contained search (February 2025). In each case the baseline version is still available but not recommended for new designs.

### Lists
- **Use when / avoid when:**
  - "Use lists to help people find a specific item and act on it". Lists are "vertical groups of text, icons, images, and other elements, optimized for reading comprehension". Use them "for communicating or selecting discrete items, such as choosing from a set of colors."
  - Order items logically (alphabetical, numerical), keep items short and easy to scan, and show icons, text and actions in a consistent format.
  - Non-interactive lists organize information for scanning. They "don't perform any actions and can't be selected."
- **Variants and how to choose:**
  - Expressive list: "Recommended for new designs". It adds the standard or segmented style, highlighted selection states and flexible slots. The baseline list is "Not recommended. Use expressive lists instead." It has square corners and lacks the new selection treatment and slots.
  - Styles: standard and segmented "are a visual choice, and don't affect a list's behavior."
  - Selection modes: single-action, multi-action, single-select, multi-select. "A list can have only one selection mode at a time". For example, a single-action list can change to a multi-select list, "but can't be both at once."
    - Single-action: "the entire list item performs one action, such as navigating to a new page". These items "Can't have secondary nested actions" and "Can't be toggled into a persistent selected state".
    - Multi-action: "The primary action should take up the majority of the space in the leading and content positions. Place supplementary actions, like a bookmark or menu, in the trailing position."
    - Single-select (for example with radio buttons): items "Don't support multi-actions; Can't have secondary nested actions; Shouldn't use checkboxes".
    - Multi-select: items "Pair well with checkboxes and switches; Can't have secondary nested actions; Shouldn't use radio buttons".
  - Selection controls go at the leading or trailing end: checkboxes to select multiple items, switches to toggle settings, radio buttons to select a single item.
  - Interactions: expand and collapse, and swipe to reveal (swipe is "only available on Android Views").
- **Emphasis and color:**
  - Color roles: surface; on surface for label text; on surface variant for the other text and icons; outline variant for dividers; primary container and on primary container.
  - "The selected state applies to the entire list item": when an item with a checkbox is selected, both the item and the checkbox show a selected state.
  - With a leading image, "consider customizing the container color to use a content-based color scheme", applied to the enabled state or to an interaction.
- **Layout and placement:**
  - Anatomy: container and label text are required. Optional elements are overline, supporting text, trailing text, trailing icon, trailing selection control, divider, leading avatar, leading icon, and leading image or video.
  - Height: "The tallest element within a list item determines the list item's height: either 56dp, 72dp, or 88dp." Elements are middle-aligned in most cases. They are top-aligned when the item is 88dp or taller, or has three or more lines of text.
  - Baseline measurements: 16dp leading padding, 16dp between the content and the trailing element, 24dp trailing padding, 48dp targets. Inset dividers use 16dp left and 24dp right padding.
  - Text: "Keep label text brief." "Limit supporting text to one to three lines" and truncate it depending on screen size. Label and supporting text can wrap or truncate.
  - Keep visuals and primary text in the same position in every item: "Don't vary the position of elements within a list." Anchor media to the leading edge.
  - Use circular or expressive-shaped avatars for a person or entity, and square or rectangular images for products or videos.
  - A leading icon relates to the label. A trailing icon shows status or an action such as **Show more**. Trailing text holds meta-information such as a price, count or date.
  - Slots: an item is a leading slot, a content slot and a trailing slot. "The leading and trailing slot positions must be a smaller width than the content section", and the content slot is the largest, in the middle.
    - Leading slots hold a visual (avatar, icon, image, video thumbnail), a selection control, or a badge or larger image.
    - Trailing slots hold an icon, an icon button, trailing text or a selection control.
    - Slots "are not accessible by default": follow list rules and structure, "Use standard list item padding", use targets of at least 48x48dp, and don't add interactive elements that make the item hard to navigate.
    - "For selection lists, use only one selection interaction per list item" (for example, not a checkmark plus a trailing bookmark).
  - Gaps and dividers: "Use **gaps** for contained lists." "Limit **dividers** to uncontained or complex lists, only when a stronger visual separation is necessary."
- **Behavior (scroll, adaptive, motion):**
  - Compact: "Lists should extend edge-to-edge in compact windows. Selecting a list item should open a page with the details."
  - Medium and expanded: show the list and its detail side by side (list-detail).
  - At larger breakpoints, adjust margins, spacing or density. Reveal more content such as supporting text and larger imagery, or split into a multi-column layout.
  - Consider swapping the list for cards, or transforming it into a carousel, when there is space.
  - Line length: ideally 40 to 60 characters, up to 120 on large screens. Near 120, increase line height. Adjust margins instead of letting lines stretch.
  - Expand and collapse: a parent item expands "in a folder-like manner" using a container transform.
  - Swipe (Android): mix button styles. "The primary action must be the final end-aligned option. A full swipe triggers this action, clearing the list item and all other actions off-screen." Swipeable items need another way to reach the hidden actions, such as a more icon.
  - Shape morph (expressive): unselected items have a 4dp inner and 16dp outer corner radius, and a selected item has 16dp all around.
- **Do / Don't:**
  - DO: "Place supporting visuals, like thumbnails, at the leading edge of a row to improve scannability"
  - CAUTION: "Avoid placing visuals in the center of a row because it makes the list difficult to scan"
  - DO: "Use only one selection interaction per list item"
  - DON'T: "Don't use multiple selection interactions in one item"
  - DO: "Use **segmented gaps** and filled list items to define a list group"
  - CAUTION: "Limit the use of **dividers** to uncontained lists"
  - DO: "Adjust margins to create a more comfortable line length for reading"
  - DON'T: "Don't scale components without adjusting other affected areas of the screen, such as text length."
  - CAUTION: "Reserve the use of slots for use cases that maintain the list's accessibility and functionality"
- **Accessibility:**
  - Don't rely on color alone for selection. Add a radio button or checkbox, a leading or trailing icon, or a non-color style. The example uses "a leading checkmark and filled color".
  - Focus: "The first element in a list should always receive focus, unless the list has a selected element", in which case focus goes to the selected item. Arrow keys move through the list and wrap at both ends. Space or Enter activates.
  - In multi-action lists, "The list item as a whole isn't selectable; only the individual actions are." Tab focuses the first element, all four arrow keys move between focusable elements across items, and Space or Enter activates.
  - The label is "typically the same as the **label text** and **supporting text**."
  - Roles: single-select items are Radio button and multi-select items are Checkbox, with the state Checked or Not-checked. "On Jetpack Compose, the role applies to the list item as a whole." On Android Views the label and role go on the contained checkbox or radio button. On web the container is a List box, its label describes the selection type, and items are Option. "If a list isn't selectable, the label text is read out without a role."
  - Swipe alternatives: single tap, double tap, long press or another single-pointer interaction (for example a more button).
- **M3 Expressive changes:** added the expressive list (December 2025, Android Views and Compose) with the segmented style, highlighted selection states, selection shape morph and flexible slots. The baseline list is not recommended.

### Menus
- **Use when / avoid when:**
  - "Use a menu to show a temporary set of actions. To show actions on screen at all times, use a toolbar instead."
  - "A menu takes up less space than a set of radio buttons or chips."
  - Uses: overflow menus, text field dropdown menus, select menus and context menus.
  - Menus open from icon buttons, buttons, split buttons, text fields, filter chips, and selected text or images. They also open on a specific action such as right-click or press-and-hold.
  - Context menus provide actions for a specific item and open with a secondary click (right-click, or a two-finger tap on a trackpad).
- **Variants and how to choose:**
  - Vertical menus (November 2025) are "recommended for new designs", with rounded corners, standard and vibrant color styles, more selection states and submenu motion. The baseline menu is still available.
  - Layout is standard or grouped. Selection is single-select (choosing a new item unselects the previous one) or multi-select ("They stay open until the person dismisses the menu").
- **Emphasis and color:**
  - Standard is surface-based and lower emphasis. Vibrant is tertiary-based and higher emphasis: "Vibrant menus are more prominent so should be used sparingly."
  - Standard roles: surface container low container, on surface text, on surface variant for icons and supporting text. The selected item is tertiary container with on tertiary container.
  - Vibrant roles: tertiary container container, on tertiary container for content and the state layer. The selected item is tertiary with on tertiary.
  - Baseline menu: surface container, 4dp corners, 112dp min to 280dp max width, 48dp items, 24dp icons.
- **Layout and placement:**
  - Anatomy: menu item, menu item text, container, and optional leading icon, trailing icon, badge, trailing text, supporting text, label text, gap and divider.
  - "When a menu item can only be used under specific conditions, it should appear disabled rather than be removed."
  - Placement: below, next to or in front of the element that opens it. "If a menu is in a position to be cut off, it should automatically reposition to appear to the left, right, or above the element that generates it." A menu opened at the top of the screen expands downward.
  - Submenus "should open next to the parent menu item without overlapping it" and are "best used on large screens where there's space". The site says submenus "are not currently available on Jetpack Compose"; the Compose source supports them by nesting `DropdownMenuPopup` (see material-3.md).
  - Gaps group items and are more expressive than dividers. "Avoid changing the size of the gap", "Limit the number of gaps in a menu to one or two", and "Don't use gaps in scrollable menus". Gaps aren't available on web.
  - Use dividers for scrollable menus, for text field dropdowns where grouping isn't appropriate, and on web.
  - Slots suit simple content such as images, progress indicators and color swatches. Keep the same item padding and 48x48dp targets. "Don't add buttons, switches, or other direct actions into the menu item. Nested elements should only perform one action."
- **Behavior (scroll, adaptive, motion):**
  - Compact: "Consider adapting menus into bottom sheets on small screens", which fit more items and longer labels.
  - Medium and expanded: menus work best in context with the content, and can show more items and use submenus.
  - Motion: an enter and exit transition ties the menu to its trigger. "In dense products, such as on desktop, menus can open instantly to reduce motion."
  - The trigger: "the corresponding button or icon button should remain the same visually, with the addition of a pressed state", even when the menu opens from a keyboard shortcut.
  - Filtering (autocomplete): a text field filters the options as the person types, and items ease into their new positions.
  - Scrolling: "Menus can scroll when all menu items can't display at once. In this state, menus show a persistent scrollbar."
  - Submenu focus: the focused submenu's corners become more rounded and the unfocused one becomes less rounded.
  - Density levels apply on web only.
- **Do / Don't:**
  - CAUTION: "Reserve the use of slots for use cases that maintain the menu's accessibility and functionality"
  - DO: "Disabled menu items can receive focus"
  - DON'T: "A divider or gap can't receive focus"
- **Accessibility:**
  - Selected items change shape and color by default, with 3:1 contrast between selected and unselected. "It's recommended to include another visual cue, like a checkmark".
  - "When a menu opens, focus should be placed on the first menu item."
  - Exit by selecting an option, pressing Escape, tapping outside the menu, or using system back. Where focus goes after closing depends on the app.
  - Keys:
    - Space or Enter opens a closed menu or submenu, and selects an item in an open one.
    - Up and Down open a closed menu and move between items in an open one.
    - Left and Right open or close a submenu.
    - Letters jump to the next item starting with that letter. Escape closes.
  - "Disabled menu items can receive focus but aren't selectable. Dividers and gaps can't receive focus."
  - The label equals the item text. The role is "Generic actionable element" on Android Views and Compose, and "Menu item" on web. An icon next to text is marked decorative.
- **M3 Expressive changes:** added vertical menus (November 2025) with new shapes, the standard and vibrant color styles, selection states, refined submenu motion, and gaps on Android. The baseline menu is still available.

### Search
- **Use when / avoid when:**
  - Use search "for products with many items to manage, such as files or messages."
  - Pick the entry point by importance:
    - Search bar: "to search contents in a specific view, like **Search your messages**", placed below a title.
    - Search app bar: "when search is the primary, global function".
    - Search icon button: "when search is a secondary action or not the main focus".
  - "If search is the primary action, focused search can be a standalone destination reached from a navigation bar."
- **Variants and how to choose:**
  - One component, "search", covers the former search bar and search view.
  - Styles: contained ("recommended"), where a filled container separates the bar from its suggestions or results; and divided (baseline), which uses a divider and is "Not recommended. Use contained."
  - Focused layouts: "**Docked** opens a list below the search bar, with a scrim covering main content"; "**Full-screen** expands to fill the screen".
- **Emphasis and color:**
  - "Search bars use the **surface container high** color role", for contrast on white or tonal surface backgrounds.
  - Avoid a surface container high bar on a surface container background. "Use surface container roles that are more than one step apart."
  - The full-screen layout background is surface container low with a surface container high bar. The docked layout uses surface container high. Input text is on surface, and icons and hint text are on surface variant.
- **Layout and placement:**
  - Usually at the top of the screen or content. The bar is 56dp tall, 360dp min to 720dp max wide, with start-aligned text and a 30dp avatar.
  - Margins are 24dp unfocused and 12dp focused. The contained container "remains the same shape in both the unfocused and focused states. Avoid changing the container behavior."
  - The docked results container is 360 to 720dp wide and 240dp min to "2/3 of screen height" max. Full-screen fills the screen.
  - Leading slot: "either: A navigational icon button, such as a menu or arrow; A non-functional search icon".
  - Trailing slot: "one or two trailing icons or icon buttons", such as voice search, a separate high-level action (current location, profile), an overflow menu or a decorative search icon. "Combine an avatar with up to one other trailing icon button". Focused search can show an optional clear icon.
  - Hint text is a short description of what can be searched ("Search replies", "Search your messages"). Typing replaces it.
  - Suggestions and results use the list component. "Consider adding variety and context": leading icons, category labels (**Recent**, **Contacts**, **Suggestions**), avatars or other high-priority items, and filter chips. Use gaps to separate groups.
- **Behavior (scroll, adaptive, motion):**
  - Adaptive: the bar should "Stay in its pane and scale in width accordingly", keep its internal elements anchored to both sides, and stay close to the content it searches.
  - Focused layouts: "Full-screen layout: Default for compact breakpoints". Docked is "Best for medium and expanded windows". Swap from full-screen to docked as the window grows.
  - Focused search can show history before typing, show suggestions or results while typing, or wait until a query is submitted.
  - "The **back** icon releases focus, dismisses any suggestions or results, and returns the search bar to its original state."
  - Search runs on Enter, or when the person selects a suggestion or result. Results scroll beneath the bar. "When search results are queried, the input text should remain visible, but not in focus."
  - "Focused search needs a clear status indicator that it's searching content, like a search icon or **Results** label" (for example "Quick results").
  - Scroll: the bar can scroll away and reappear when scrolling back, or stay fixed at the top.
  - Predictive back (Android): search "detaches from the screen edge to signal the full-screen layout will minimize" and previews the previous screen.
  - Expressive motion: the bar grows wider when focused.
- **Accessibility:**
  - "When search suggestions and results appear, the screen reader must announce the change."
  - Initial focus lands on the first interactive element, usually the leading icon button, or on the text field if there's no leading icon.
  - Tab or Shift+Tab moves between elements, Space or Enter activates the field, and arrow keys move between results.
  - "The hinted search text should be used as the accessibility label". The input role is **Text field** on Android. Label icon buttons by their own guidance.
  - Results use list semantics, and screen readers announce them as a list.
- **M3 Expressive changes:** (February 2025, Jetpack Compose) search bar and search view renamed to "search". Adds the contained style (recommended) and gaps to group results, and the bar grows wider when focused. The divided style is no longer recommended.

## Communication and pickers

Source: m3.material.io scrapes (Overview, Specs, Guidelines, Accessibility). Only loading indicator and progress indicators have an "M3 Expressive update" section. The other five list only "Differences from M2", so they have no M3 Expressive heading below.

### Badges
- **Use when / avoid when:** Badges "indicate a notification, item count, or other information relating to a navigation destination." They are "most commonly used within other components, such as navigation bar, navigation rail, app bars, and tabs."
- **Variants and how to choose:**
  - A small badge is "a simple circle, used to indicate an unread notification" (6dp).
  - A large badge "contains label text communicating item count information" (16dp high, up to 16x34dp at maximum length).
  - Use large when "visual collisions aren't an issue," for example on a navigation rail. Use small "when spaces are tightly constrained, such as app bars."
- **Emphasis and color:**
  - Container uses Error and label uses On error.
  - "Use the default color mapping to avoid color conflict issues."
  - If you use custom roles, they need at least 3:1 contrast.
- **Layout and placement:**
  - Anchor "inside the icon bounding box, at the upper trailing edge of the icon."
  - As the count grows, width expands but placement stays the same.
  - Maximum of "four characters, including a + to indicate more" (for example 999+).
- **Behavior:** "In navigation bars, hide the badge once the destination has been selected." An unread badge "gets hidden once it's selected."
- **Do / Don't:**
  - DO: "Change the position of the badge for right-to-left languages"
  - DON'T: "Badges have fixed positions. Don't change the position of the badge arbitrarily or place the badge over the icon."
  - DO: "Use the default badge color"
  - DON'T: "Avoid using custom color roles for the badge container and label text. If custom roles are necessary, make sure they have contrast of at least 3:1."
  - DO: "Truncate badge labels as needed"
  - DON'T: "Don't let the badge get cut off or collide with another element"
  - DO: "Use a large badge to show count information when visual collisions aren't an issue, such as in a navigation rail"
  - CAUTION: "Use a small badge when spaces are tightly constrained, such as app bars. Small badges won't run into the edge of the screen."
  - DO: "When an icon with a badge is followed by text or another element, place a large badge at the trailing edge"
  - DON'T: "Avoid using a large badge when it might overlap with a trailing element. Either place it at the trailing edge or use a small badge instead."
- **Accessibility:**
  - The badge is read after its navigation destination.
  - Numeric badges read their number. Non-counting badges announce "New notification".
  - Minimum 3:1 contrast.

### Loading indicator
- **Use when / avoid when:**
  - Use it for short waits "between 200ms and 5s," "when progress isn't detectable, or when it's not necessary to indicate how long an activity will take."
  - It is "Recommended as a replacement for indeterminate circular progress indicators."
  - It is "never simply decorative."
  - It is "Not used for processes that transition from indeterminate to determinate."
- **Wait-time rule (shared with progress indicators):**
  - Under 200ms: no indicator.
  - 200ms to 5s: loading indicator.
  - Over 5s: progress indicator.
  - "If the wait is very long, consider allowing users to navigate away."
- **Variants and how to choose:**
  - Default (uncontained) or contained.
  - Use the container "when the loading indicator is placed over other content," and always with pull-to-refresh.
  - The container is "not needed when the loading indicator is placed directly on a surface."
- **Emphasis and color:**
  - Default: Primary indicator.
  - Contained: On primary container indicator on a Primary container.
- **Layout and placement:**
  - Center it on the page or container that is loading.
  - When loading more items, place it "in the empty space where the new content will appear. Avoid overlapping existing content."
  - It can go inside buttons or tabs.
  - Size is 48dp by default, flexible from 24dp to 240dp. Scale up in larger windows and never exceed 240dp. The container-to-indicator ratio stays fixed.
- **Behavior:**
  - The active indicator is a looping morph through seven M3 shapes.
  - Pull-to-refresh uses it "on Jetpack Compose only," for "dynamic content that can have frequent updates."
  - The gesture must pass a threshold before refreshing, and reversing past the threshold cancels.
  - The indicator stays visible until new content shows or the user navigates away.
- **Do / Don't:**
  - DO: "Transition from an indeterminate progress indicator to a determinate progress indicator"
  - DON'T: "Avoid transitioning from a loading indicator to a determinate progress indicator"
  - DO: "Keep the loading indicator in view until the activity is completed to provide status of the refresh activity"
  - DON'T: "Don't scroll the loading indicator off-screen, as it hides the status of the refresh activity..."
  - DO: "Ensure at least 3:1 contrast between the indicator and the surface it's on"
  - DON'T: "Avoid using when the contrast is under 3:1"
- **Accessibility:**
  - The indicator needs 3:1 contrast. The container does not.
  - Pull-to-refresh needs a single-pointer alternative, for example a refresh action in the app bar or a menu.
  - Use the "progress bar" role with a label like "refreshing page".

- **Pull-to-refresh placement:** use it "at the beginning of lists, grid lists, and card collections where the most recent content appears." The indicator "can appear on top of the content or adjacent to it." Offer the single-pointer refresh "in a menu or directly alongside the content", for example in the app bar.

- **M3 Expressive changes:** New component (May 2025) for waits "under five seconds." It "should replace most uses of the indeterminate circular progress indicator." It can be contained or uncontained, "use shape and motion to capture attention," and can scale in size.

### Progress indicators
- **Use when / avoid when:**
  - Use them for "the status of ongoing processes, such as loading an app, submitting a form, or saving updates," and for waits over 5s (see the wait-time rule above).
  - For multiple items, use one indicator for the group.
- **Variants and how to choose:**
  - Linear is "best when placed on the edge of a container." Circular is "best when centered in an element."
  - Determinate means known progress and must be accurate. Indeterminate means unknown progress.
  - Switch from indeterminate to determinate as information arrives.
  - "A process should be represented by the same variant of progress indicator throughout the product."
- **Emphasis and color:** The active and stop indicators use Primary. The track uses Secondary container.
- **Layout and placement:**
  - Place linear on the container's edge, or on the edge that animates if the container changes shape. It can also go in the middle.
  - One linear indicator at the top of a page shows that the whole page is loading.
  - Center circular on the loading container. For more items, put it in the empty space, "not overlapping existing content." If loading is quick, "consider using a loading indicator instead."
  - Circular inside a button: set the active indicator to the button's label or icon color and remove the track. Use the flat shape in very small buttons.
  - Circular size ranges from 24dp to 240dp.
  - Linear spans its element and should not be used in elements under 40dp. It has 4dp minimum end padding.
  - Mirror linear for RTL. Circular does not need mirroring.
- **Behavior:**
  - Linear animates from the leading edge. Circular animates clockwise from the top.
  - At low percentages the active indicator appears as a dot.
  - The stop indicator is a 4dp circle for linear determinate only. It is "required if the track has a contrast below 3:1 with its container or the surface behind."
- **Do / Don't:**
  - DO: "Indicate overall progress of a group of items"
  - DON'T: "Don't show the progress of each activity in a group"
  - DO: "Use a stop indicator when placing the progress indicator inside a container with low contrast"
  - CAUTION: "Only remove the end stop indicator if there's a visual contrast of at least 3:1 with surrounding surfaces"
  - DO: "Use circular indicators for short, indeterminate activities under 5 seconds"
  - DON'T: "Avoid applying progress indicators to every button in a list"
  - DO: "Ensure the indicator's color provides at least 3:1 contrast against the surface it's on"
  - DON'T: "Avoid using a color below 3:1 contrast"
- **Accessibility:** Use the "progress bar" role. The label names the process and the content, for example "Loading news article".
- **M3 Expressive changes (Aug 2024):**
  - Configurable track thickness (the default is 4dp).
  - Wavy shape option. "The wavy shape can make longer processes feel less static," and suits cases "when a more expressive style is appropriate."
  - Wavy increases the component's height and "may not be as visible" at small sizes.
  - Earlier (Dec 2023) changes: stop indicator, higher track contrast, rounded corners.

### Snackbar
- **Use when / avoid when:**
  - Snackbars "inform users of a process that an app has performed or will perform". They are low priority and "shouldn't interrupt."
  - Snackbar vs dialog: a snackbar is low priority and action is optional. A dialog is high priority and "block[s] app usage until the user takes a dialog action."
  - Snackbars "shouldn't be the only way to access a core use case."
- **Variants and how to choose:**
  - Single line or two lines, with or without an action.
  - A long action can go on a third line.
  - Only one action, and a "Dismiss" or "cancel" action is optional.
- **Emphasis and color:**
  - Inverse surface container, Inverse on surface text, and Inverse primary action.
  - Must be opaque, though slight transparency is allowed if text stays legible.
  - Use the default mapping.
- **Layout and placement:**
  - Place it at the bottom, in front of content. Nudge it up to clear FABs or docked toolbars.
  - Place it above FABs and never in front of navigation.
  - Full width only when there are no persistent navigation components.
  - Compact windows: 48dp to 64dp tall, up to two lines, with fixed edge distances.
  - Medium and expanded windows: grow the width (ideal line length is 40 to 60 characters), aim for a single line, and align left or center consistently.
- **Behavior:**
  - Show one at a time.
  - Without an action, it auto-dismisses after 4 to 10s depending on platform.
  - With an action, it stays until the user acts or dismisses it.
  - An updated snackbar may replace an outdated one immediately.
- **Do / Don't:**
  - DO: "Keep the snackbar text label to one line long when possible"
  - DO: "On mobile, the text label can be up to two lines long"
  - CAUTION: "Avoid adding icons to snackbars. If your message needs an icon, consider using a different component such as a dialog."
  - DON'T: "Avoid using stylized text or inline links in snackbars... If your message needs a link, add a button instead, or use a different component."
  - DON'T: "The text label shouldn't share the same color as the text button"
  - DON'T: "Don't use a filled or elevated button in a snackbar, as it draws too much attention"
  - DO: "In wide layouts, extend the container width to accommodate longer text labels"
  - DON'T: "Avoid significantly altering the shape of a snackbar container"
  - DO: "To allow users to amend choices, display an 'Undo' action"
  - CAUTION: "A dismiss action is unnecessary, as snackbar disappears on their own by default"
  - DON'T: "Avoid placing snackbars in front of navigation components"
  - DON'T: "Don't place a snackbar in front of a FAB" / "behind a FAB"
  - DON'T: "Don't place snackbars flush to one edge of the layout"
  - DON'T: "Don't place consecutive snackbars side by side"
  - DON'T: "Don't stack snackbars on top of one another"
  - DON'T: "Don't animate other components along with snackbar animations, such as the floating action button"
- **Accessibility:**
  - Snackbars with actions should not auto-dismiss.
  - On web, an auto-dismissing snackbar also needs inline feedback (for example "Save" changing to "Saved"), or it must be actionable.
  - Announce it politely. Don't move or trap focus.
  - Esc dismisses the snackbar when it has focus.

- **Focus exit:**
  - Focus should "return to the element that triggered the snackbar, or go to the next most logical element on the page." "On Android Compose, focus may move to the nearest visible element, or to the first actionable item on the page."
  - On web, provide a documented shortcut (such as Alt+G) to reach an actionable snackbar.
  - A snackbar shown at app launch is announced after the page title and doesn't take focus.

### Tooltips
- **Use when / avoid when:**
  - A tooltip adds context to a UI element.
  - Never put critical information in a tooltip. Use a dialog instead.
- **Variants and how to choose:**
  - Plain tooltips label elements that have no text, "like icon-only buttons and fields."
  - Rich tooltips carry "longer text like definitions or explanations." They can have an optional subhead, a link, and up to two text buttons.
  - A persistent rich tooltip opens on click or tap, or on page load to explain a new feature.
- **Emphasis and color:**
  - Plain: Inverse surface with Inverse on surface text.
  - Rich: Surface container with On surface variant text and a Primary button.
- **Layout and placement:**
  - Plain: above the element, 4dp away if the element has a visual boundary and 8dp if not. In app bars it goes below the element.
  - Rich: at the bottom right of the element. It repositions in 8dp steps to stay on screen and should not cover its parent.
  - On desktop, a tooltip may be centered below the element.
- **Behavior:**
  - Hover shows a tooltip on desktop. Long press shows it on mobile.
  - Transient tooltips disappear 1.5s after the pointer leaves the element.
  - A new tooltip closes the current one.
  - Persistent tooltips stay until the user interacts elsewhere, and hovering does not trigger them.
- **Do / Don't:**
  - DO: "Use plain tooltips to label icon-only buttons"
  - DON'T: "Plain tooltips aren't needed when the UI element already has label text"
  - DO: "Use rich tooltips to provide extra information and actions about a UI element or new feature"
  - DON'T: "Don't hide critical information within tooltips as it's easy to miss. Use an interruptive dialog instead."
  - DO: "Briefly describe a UI element"
  - CAUTION: "Avoid wrapping text to multiple lines or including many pieces of information"
  - DO: "Summarize the message in a few words"
  - DON'T: "Avoid wrapping to more than one line" (subhead)
  - CAUTION: "Avoid stacking buttons"
  - DON'T: "Only display one tooltip at a time"
  - DON'T: "Don't use a persistent rich tooltip on icon buttons"
- **Accessibility:**
  - Tooltips appear on hover or focus.
  - Include a subhead when a rich tooltip appears automatically.
  - Don't trap focus. Focus moves top to bottom inside a rich tooltip.
  - Use the "Tooltip" role.

### Date pickers
- **Use when / avoid when:**
  - Use for selecting a date or a range, for example flights or hotels.
  - Compact windows: embed the picker in dialogs. Medium and expanded windows: embed it in text field dropdowns.
  - "Don't use a modal date picker to prompt for dates in the distant past or future, such as a date of birth". Use modal input or docked instead.
- **Variants and how to choose:**
  - Docked: a text field plus a dropdown calendar. It is best for medium and expanded windows and handles both near and distant dates.
  - Modal: a calendar in a dialog.
  - Modal input: keyboard entry, and the default "for dates that don't require a calendar view."
  - Full-screen modal is recommended on compact windows.
- **Emphasis and color:**
  - Selection uses color. Selected dates are Primary with On primary.
  - The in-range highlight is Secondary container.
  - The container is Surface container high, with no shadow.
- **Layout and placement:**
  - Docked appears directly below its field.
  - Neither docked nor modal scales responsively.
- **Behavior:**
  - Modal: swipe horizontally for months, scroll vertically for years, and tap the year to open the year picker.
  - Range picker: scrolls vertically.
  - The edit icon and calendar icon toggle between picker and input.
  - Modal exits via OK, Cancel, or tapping outside. Full-screen adds a close (x) button and Save.
  - Uses dialog enter and exit transitions.
- **Do / Don't:**
  - DO: "For dates that don't require a calendar view, the modal date input can be the default view"
  - DO: "Alternatively, a text field with appropriate hint text can prompt for dates, such as in a form"
  - DON'T: "Don't scale the date picker responsively to a larger size"
- **Accessibility:**
  - Always offer text entry.
  - 48x48dp targets. Don't increase density.
  - Format the date after Enter or blur, with no input masks, and accept varied separators.
  - Remove Clear if it isn't needed.
  - Show keyboard shortcuts in tooltips.
  - Dates need 4.5:1 contrast.
  - Screen readers announce the full date.

- **Accessibility (text entry and labeling):**
  - The text field label states the purpose ("event date", "reservation date") and "should match the placeholder text when the field is empty."
  - Helper text states the date format, for example MM/DD/YYYY or YYYY/MM/DD (default "MM/DD/YYYY"), and acts as the field's description.
  - "The calendar icon is the exclusive entry point for the date picker", and "Each input is a separate tab stop". This keeps the picker optional for keyboard and screen reader users.
  - The modal picker offers date input through the edit icon. The docked picker uses its text field.
  - Keys:
    - Page up/down moves to the same date in the next or previous month, and Shift+Page up/down to the same date in the next or previous year.
    - Home/End go to the first day of the month (as the page states).
    - Shift+M opens the month dropdown and Shift+Y the year dropdown. Enter closes the calendar and saves.
  - Include the shortcut in the tooltip and the hint description.
  - Weekday headers aren't focusable, and their full-name tooltip shows only on hover.
  - Roles: previous and next month or year are Button, the month and year dropdowns are Button, weekdays are column headers, and the month grid is Grid.

### Time pickers
- **Use when / avoid when:**
  - Time pickers are modal and "cover the main content." Use them for alarms and meetings.
  - They are "not ideal for nuanced or granular time selection, such as milliseconds."
- **Variants and how to choose:**
  - Dial or input. Input "should be accessible from any other mobile time picker interface by tapping the keyboard icon."
  - Make input the default when a dial isn't needed.
  - 12h vs 24h is set outside the component, usually in system settings. A 24h dial puts even numbers on the inner ring. There is no AM/PM selector in 24h.
- **Emphasis and color:**
  - The selected field is Primary container.
  - The period selector is Tertiary container.
  - The dial is Surface container highest with a Primary handle.
- **Layout and placement:**
  - Shown above a scrim and never obscured or cropped.
  - The dial is 256dp with 48dp handle targets.
  - Landscape places the parts side by side.
- **Behavior (adaptive):**
  - Switch orientation or variant to avoid scrolling. Fall back to input when there isn't enough height.
  - Doesn't scroll with the background.
  - Exits via OK, Cancel, or tapping outside, with dialog transitions.
  - Dial and fields update each other.
- **Do / Don't:**
  - DO: "Hour selection in a mobile calendar picker"
  - DON'T: "Don't apply density to the time picker dial when the viewport is constrained. Instead, use an input picker."
- **Accessibility:**
  - Always allow manual text entry.
  - 48x48dp dial targets.
  - The dial reads "Hour 7 of 12".
  - AM/PM uses the radio button role.

- **24-hour default (research):** "Even participants who currently use 24 hour clocks found all 24 hour analog designs unfamiliar and confusing." "An overwhelming majority of participants perceived a simple digital input as the least confusing and indeed made fewer errors."
  - Material's resulting direction: "digital input by default for 24hr users" with the dual-ring analog dial as an option.
  - Remember the user's choice: switching to the analog clock makes it the default next time.
  - With digital input, still make clear whether it is a 12h or 24h clock.

