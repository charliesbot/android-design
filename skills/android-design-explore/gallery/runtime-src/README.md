# Proposal runtime

Every exploration proposal is an `index.html` and a `style.css` that load this runtime. It is built from this folder into `../public/runtime/` and committed, so the installed skill never runs npm. `cli.mjs render` serves it to headless Chrome at `/runtime/`, and `view.html` inlines it.

A proposal page starts like this:

```html
<link rel="stylesheet" href="/runtime/runtime.css">
<link rel="stylesheet" href="style.css">
<script type="module" src="/runtime/m3e.js"></script>
```

Proposals have no inline `<style>`, no `style=""`, and no inline scripts (the lint enforces this); they keep styles in `style.css`. `view.html` inlines the runtime, fonts, and proposal files as data URIs, so a proposal may reference its own files by relative path. See `sample/` for a working page.

## What it contains

| File | Purpose |
| --- | --- |
| `runtime/m3e.js` | ES module: registers the `@m3e/web` 2.9.2 elements below, the `p-` pieces, the theme message listener, `window.runtimeChecks`, and `window.runtimeSettle` (resolves once no finite animation or transition is running; call it before `runtimeChecks()`) |
| `runtime/runtime.css` | Google Sans Flex `@font-face`, page defaults, type role classes, shim styles |
| `runtime/fonts/` | Latin-subset text woff2 with all variable axes and each OFL license: `GoogleSansFlex` (plain text, always), `Fraunces`, `BricolageGrotesque`, `BigShouldersDisplay`, `Doto` (optional brand faces for Display and Headline voices), `GoogleSansCode` (numbers and data). The family list and minimum sizes are in `fonts.json`, read by the lint and the fonts check |
| `runtime/fonts/MaterialSymbolsRounded.woff2` | The full Material Symbols Rounded variable font (axes FILL, GRAD, opsz, wght), unmodified from the pinned `material-symbols` npm package; provenance in `MaterialSymbolsRounded-SOURCE.txt`, Apache 2.0 license in `MaterialSymbolsRounded-LICENSE.txt` |
| `runtime/symbols.json` | Every Material Symbols name (from the package's type list). The lint rejects a `p-icon` name that is not in it |
| `runtime/sample/` | A small page that uses only runtime pieces |
| `runtime/checks-fixture/` | Known-bad and known-good component text (low-contrast and clipped button labels, clipped plain text) that `runtimeChecks()` must flag, plus opacity 0 text it must skip instead of leaving unresolved, and bundled-face text (Fraunces at 40px and Google Sans Code at 12px pass, Doto at 24px must be listed in `fonts.offenders`) |
| `runtime/verify/` | One section per component in `references/compose-mapping.md`, with its variants, for comparing against Compose; it marks nothing verified |

## Registered elements

The runtime registers these `@m3e/web` elements: `m3e-theme`, `m3e-app-bar`, `m3e-button`, `m3e-button-group`, `m3e-icon-button`, `m3e-split-button`, `m3e-fab`, `m3e-fab-menu` (with `-item` and `-trigger`), `m3e-card`, `m3e-list` and the list item elements, the chip elements (`m3e-chip`, `m3e-assist-chip`, `m3e-filter-chip`, `m3e-input-chip`, `m3e-suggestion-chip` and their sets), `m3e-toolbar`, `m3e-nav-bar`, `m3e-nav-item`, `m3e-nav-rail`, `m3e-loading-indicator`, `m3e-circular-progress-indicator`, `m3e-linear-progress-indicator`, `m3e-shape`, `m3e-switch`, `m3e-slider`, `m3e-form-field`, `m3e-bottom-sheet` (with `-action` and `-trigger`), `m3e-dialog` (with `-action` and `-trigger`), `m3e-menu` (with the `m3e-menu-item` family, `-trigger`, and `-item-group`).

Registered does not mean usable. A proposal may use only the components `references/compose-mapping.md` marks verified. Anything else it builds from plain HTML layout (`div`, `span`, headings, lists) styled with role tokens, as long as the result has a plain Compose layout counterpart (`Box`, `Row`, `Column`, `Text`, `Surface`).

Wrap the page in `<m3e-theme color="#RRGGBB" variant="tonal-spot" scheme="light">`. `m3e-icon` is not registered: use `p-icon`.

## Page defaults

`runtime.css` sets `box-sizing: border-box` on every light-DOM element and pseudo-element, so padding and borders sit inside a declared size, as in Compose (`Modifier.height(56.dp).padding(12.dp)` is 56dp). Shadow DOM inside `@m3e/web` components is untouched. Only the page's own `m3e-theme` fills the viewport height (`min-height: 100vh`); a nested `m3e-theme`, such as a preview card in another scheme, takes its content height.

## Type roles

One class per Material 3 role: `.t-display-large`, `.t-display-medium`, `.t-display-small`, `.t-headline-large|medium|small`, `.t-title-large|medium|small`, `.t-body-large|medium|small`, `.t-label-large|medium|small` (15), and the same names with an `-em` suffix for the emphasized roles (15). Sizes, line heights, tracking, and weights are generated from `type-scale.json`, extracted from the pinned AndroidX Material 3 artifact, so each baseline class equals its Compose `MaterialTheme.typography` role exactly. An emphasized class carries Google's emphasized token values, which the pinned material3 does not apply to `typography.<role>Emphasized` (it equals the baseline), so the port copies the baseline role with the token weight and tracking (`references/compose-mapping.md`, type roles).

Optical sizing is off (`font-optical-sizing: none` on every element): browsers otherwise set a variable font's `opsz` axis to the font size, Compose never does, and with it on 14px Google Sans Flex measured 5% wider than in Compose. Text and icons draw at each font's default `opsz` (18 for Google Sans Flex, 9 for Fraunces); a voice that wants another sets it in `font-variation-settings` (`"opsz" 52`) and the port passes the same value with `FontVariation.opticalSizing`.

Fonts: every element is Google Sans Flex. A `.voice-*` rule may set `font-family` to one bundled face (`Fraunces`, `"Bricolage Grotesque"`, `"Big Shoulders Display"` at `font-size` 24px and up in that rule; `Doto` at 36px and up; `"Google Sans Code"` at any size), optionally followed by `serif`, `sans-serif`, or `monospace`. Publishing rejects any other family, a `font` shorthand, and a brand face below its minimum, with the file and line. The fonts check lists rendered text whose first family is not bundled or is a brand face below its minimum size.

Large voices: Google Sans Flex at heavy weights and large sizes needs a line height of at least about 1.2x the font size (the `android-design` skill's `SKILL.md` section 5: "Line height about 1.2x on large styles"); heavy or narrow voices need about 1.25x (measured). The checker flags a tighter line box as overflow, because the glyph box then exceeds the box that holds it.

## Icons

`<p-icon name="search"></p-icon>` draws a 24px `currentColor` Material Symbols Rounded glyph, by ligature from the bundled variable font. `name` is any symbol name from fonts.google.com/icons (`directions_car`, `hiking`, `cloud`); add `filled` for FILL 1, `label="..."` to expose it to assistive tech (otherwise it is `aria-hidden`). Size an icon that stands alone (or in a list item slot) with the `size` attribute, one of the Compose icon sizes `18 20 24 28 32 36 40 48` (`icon-sizes.json`; 24 by default): `<p-icon name="add" size="40">`. Inside a component that forces its icon size (an `@m3e/web` 2.9.2 element whose every slotted icon has an `!important` size, found in its source: buttons, icon buttons, FABs and FAB menu items, the chips, nav items, tabs, menu items, breadcrumb items, and the others listed in `lint.mjs`; the common ones confirmed by render. Components that force only a named slot, such as a dialog close icon, are not linted) `size` has no effect and the lint rejects it; in `m3e-theme`, `m3e-card`, `m3e-list-item`, and plain layout it works; the port uses that component's default icon size (an icon button or FAB follows its container size row in `references/compose-mapping.md`). A larger icon in a button comes from a larger component size whose default icon is that size, for example a 40px icon from `m3e-icon-button size="extra-large"`, not from `size`. A `font-size` on an icon is rejected like any `font-size` outside a voice, and so is any other size. Weight is 400, grade 0, and the optical size is the font's default (24), which is what a 24dp drawable scaled to the size draws in Compose. Publishing rejects a name that is not in `symbols.json`, with the file and line. An unknown name would otherwise render its text as a word, so the lint is the guard.

## Text fields

`m3e-form-field` hosts a native `<input>` or `<textarea>`. Write it like this:

```html
<m3e-form-field variant="filled" hide-subscript="always">
  <label slot="label" for="amount">Amount</label>
  <span slot="prefix-text">$</span>
  <input id="amount" type="text" inputmode="decimal" value="120.00">
</m3e-form-field>
```

Slots: `label`, `prefix` and `suffix` (a `p-icon`), `prefix-text` and `suffix-text`, `hint`, `error`. Variants are `filled` and `outlined` (the default). Always set `hide-subscript="always"`, or `"never"` when the field shows hint or error text: the default `auto` reserves a 20px row under the field that Compose does not have. An invalid field shows the `error` slot and hides `hint`; add the `error` attribute to draw the static error state. Give the field a width with `--m3e-form-field-width` in `style.css` (270px by default, 280dp is Compose's minimum). A value field holds static text: the proposal shows the `value` (or `placeholder`) you write, and typing is not saved. The page keeps state in memory if its script needs it. `runtimeChecks()` measures a field as the whole `m3e-form-field` for the 48px touch target, not the native input inside it. Which patterns are verified is in `references/compose-mapping.md`; `runtime/verify/` has each case.

## Shims

- `<p-floating-toolbar>`: wrap one `<m3e-toolbar>`. Floats it at bottom center, 16px above `--p-inset-bottom`, rounded and elevated. Its `m3e-button[toggle]` children are single-select: the selected one is `tonal`, the others `text`, 48px tall.
- Type roles: the emphasized classes above, because `@m3e/web` has no type scale component.
- `--p-inset-top`, `--p-inset-bottom`: default `0px`. Stand-ins for the status and navigation bars, which do not exist on the web. A proposal sets them to show system bars.

## Component notes

`m3e-list variant="segmented"` items draw no container of their own: the default is the surface color, as in Compose, where `ListItemDefaults.segmentedColors` also defaults to the surface color (`references/compose-mapping.md`). When the list sits on a surface, set the container to a role on the list or an ancestor: `--m3e-segmented-list-item-container-color: var(--md-sys-color-surface-container)`. Setting `--m3e-list-item-container-color` on an ancestor has no effect, because the segmented list resets it from the segmented token; the segmented token is the one to use.

An open modal `m3e-bottom-sheet` moves focus to its drag handle. The browser's default `:focus-visible` outline (blue) would draw around it, so `p-sheet-handle.js` turns that outline off inside the sheet's shadow root. Every other focusable `@m3e/web` part already sets `outline: none` and relies on `m3e-focus-ring`, which `m3e-theme` hides unless its `strong-focus` attribute is set; the sheet's `.handle` is the one part that omits the rule, so the patch makes it behave like the rest of the library. Drop `p-sheet-handle.js` once `@m3e/web` sets `outline: none` on the handle itself.

## Charts

Draw a line or area chart as inline SVG in `index.html`. Its colors come only from classes in `style.css` (role variables or static color variables, never hex in the HTML), and the Compose port draws the same points with `Canvas` and `drawPath`, colors from `MaterialTheme.colorScheme`. Use solid roles (`primary`, `secondary`), not containers: graphics need 3:1 against the surface.

```html
<svg class="chart" viewBox="0 0 100 40" preserveAspectRatio="none" role="img" aria-label="Steps this week, rising from 4,200 to 9,800">
  <line class="chart-base" x1="0" y1="39" x2="100" y2="39"/>
  <path class="chart-line" d="M0 30 L25 24 L50 26 L75 12 L100 6"/>
  <circle class="chart-dot" cx="100" cy="6" r="2"/>
</svg>
<p class="t-label-medium">Mon 4,200, Fri 9,800</p>
```

```css
.chart { width: 100%; height: 160px; display: block; }
.chart-line { fill: none; stroke: var(--md-sys-color-primary); stroke-width: 3; stroke-linecap: round; stroke-linejoin: round; vector-effect: non-scaling-stroke; }
.chart-base { stroke: var(--md-sys-color-outline-variant); stroke-width: 1; vector-effect: non-scaling-stroke; }
.chart-dot { fill: var(--md-sys-color-primary); }
```

Responsive: `preserveAspectRatio="none"` stretches the path to the box, and `vector-effect: non-scaling-stroke` keeps the line width in px (a dot stretches too, so draw markers in HTML or a fixed aspect). For a fixed aspect, drop `preserveAspectRatio` and set `height: auto`. Give the `svg` `role="img"` and an `aria-label`, and repeat the key numbers in text.

## Open states

A sheet, dialog, or menu is hidden until opened, so a default render never shows it. A proposal names its hidden states in the head of `index.html`, `<meta name="p-states" content="queue lyrics">`, and defines `window.proposalState = name => { ... }` in its own script (the lint wants a valid, unique list and at least one script file). For example, with `<m3e-bottom-sheet id="queue" modal handle>` in the page:

```js
window.proposalState = name => { document.getElementById(name).show(); };
```

`render` loads the page, applies the theme, waits for it to settle (a bottom sheet runs a 250ms close animation when it first renders and would hide itself again if opened sooner), awaits `proposalState(name)`, settles again, shoots `<name>-light.png` (411x923) and `<name>-tablet.png` (1280x800, light), and stores `runtimeChecks()` under `queue-light` and `queue-tablet` in `checks.json`. A page with no `proposalState`, or one that throws, fails those views with that reason. `view.html` shows a State picker (Default plus each name) that sends the same `proposalState` call through a `{type: "state", name}` message; Default reloads the page.

## Theme messages

`view.html`'s theme controls post `{type: "theme", color, variant, scheme}` to the proposal frame, and its state picker posts `{type: "state", name}`. `m3e.js` applies valid theme values to the page's `m3e-theme`, waits for `runtimeSettle()` and then calls the page's `window.proposalState(name)` for a state message, and ignores messages from other origins. `render` does not use messages: it sets the `scheme` and `color` attributes on `m3e-theme` directly in the page and calls `proposalState` itself.

## Rule checker

`window.runtimeChecks()` returns one JSON object:

```
{ pass, complete, viewport, touchTargets: {checked, under48}, fonts: {offenders}, overflow, midWordBreaks, overlaps, pageOverflow: {fails, scrollWidth, offenders},
  primary: {status, marked, largest, reason?}, fabs: {count, ok}, contrast: {fails, passed}, unresolved }
```

`pass` is false when any check failed. `complete` is false when anything is in `unresolved`. Treat `complete: false` as not verified, whatever `pass` says: an unresolved case is never a pass.

Checks: the page wider than its viewport (`pageOverflow`, a failure; names up to 5 elements past the right edge, widest first, except inside a deliberate horizontal scroller narrower than the viewport; a `position: fixed` or `sticky` element that is partly visible but crosses the left or right edge also fails, while one fully off screen is treated as hidden on purpose); interactive elements under 48x48 (a component is measured as a whole; its target is the larger of its host box and any `.touch` element in its shadow root, and the reported size is that union; for `m3e-slider` the host is measured and `m3e-slider-thumb` is skipped, because Compose's whole 48dp Slider is the touch target); text not in Google Sans Flex (`p-icon` excluded); text whose box clips it (not horizontally when the box or its clipping ancestor truncates with `text-overflow: ellipsis`, and not vertically when it has a line clamp; the other axis is still checked, and nothing inside a button, icon button, FAB, chip or button group is exempt); a word split across two lines with no hyphen (`midWordBreaks`: it lists the element and the words, and measures segments between spaces, hyphens, soft hyphens, zero-width spaces, and slashes, skipping `hyphens: auto` and CJK text; the usual cause is `overflow-wrap: anywhere` or `word-break` on a box too narrow for its word); text covered by another element painted above it (`overlaps`: the text is sampled across each line, and it fails when at least a fifth of the samples are hit through a nearly opaque element, such as a floating toolbar or an opaque sheet; pointer-events: none and fully transparent elements, translucent scrims, gradients, the text's own ancestors and descendants, and an opaque fixed or sticky surface over content that scrolling can move fully clear of it, in the document or an inner scroller, are not counted: that is occlusion, normal scrolling UI. Scrolling down clears a bar in the lower half of the scroller, scrolling back up clears one in the upper half, each limited to the scroll range left or used, so a scroller needs padding at least the bar height or its first or last row is an overlap. A pinned ancestor above the scroller, such as a fixed app shell, does not pin the text, and a pinned surface as tall as the scroller's viewport still counts. Text covered where nothing scrolls, or text that is itself pinned, still fails); the element marked `data-primary` must be at least as tall as every other action (ties pass, within 1px). Height is the visual container (the component's `.base`, else its box), which is the button size step: width follows the label, and a standard icon button's 48px touch box is not its 40px visual. Actions are `m3e-button` (including toggle), `m3e-icon-button`, `m3e-fab`, `m3e-fab-menu-trigger`, `m3e-split-button`, the `m3e-*chip` elements, and a native `button` or `[role=button]` that is not part of a row. Content is not compared: `m3e-list-action`, `m3e-list-option`, `m3e-card[actionable]`, and a native button inside `m3e-list-item`, `m3e-card`, or `li` (or with a `link`, `listitem`, `row`, or `option` role) may be larger than the primary. Elements inside navigation chrome, `m3e-nav-bar`, `m3e-nav-rail`, the labeled `m3e-button[toggle]` items of `p-floating-toolbar` (the compact navigation; its icon buttons and non-toggle buttons are still compared), the `start` and `end` slotted drawers of `m3e-drawer-container` (not its main content), `<nav>` or `role=navigation`, and sliders, progress tracks, and text fields, `m3e-slider`, `input[type=range]`, `role=slider`, `role=progressbar`, `m3e-form-field`, are also left out; at most one FAB; text contrast of 4.5:1 (3:1 at 24px and up, or bold at 18.66px and up).

Text is measured where it renders. Slotted text, such as a button label, is attributed to the element its slot places it under inside the component's shadow root (`m3e-button#cta > div.label`), not to the light-DOM host. Color and font come from that element, the fill from the first opaque background up the flat tree (slot, shadow ancestors, host, light ancestors), so a filled button is measured against its container, not the page. Clipping is tested on the shadow element that clips. Cases it cannot resolve, such as a gradient or translucent background, a page with no `data-primary`, or an ancestor with opacity below 1, are listed in `unresolved`. Decorative text (`aria-hidden`) is exempt from overflow and contrast. Run it at each device size you review; it reads the live layout.

Open overlays are found through the library's own open state: a modal `m3e-bottom-sheet` that is `:popover-open` and an `m3e-dialog` whose inner `<dialog>` is open are modal surfaces, and an open `m3e-menu` is a floating one. Behind an open modal the page is inert under a scrim, so its interactive elements are not measured for the 48px target, its text is not measured for overlap or contrast, and the `data-primary` check is reported as `skipped` (the default views check it). The modal itself and everything inside it are checked in full. Text under an open menu is not an overlap (a menu floats over content on purpose), though the menu's own text is checked. A bottom sheet that is not open sits below the viewport and is not measured at all. A dialog's text is measured against the dialog's container, which the library paints with a `::before` layer: the first opaque `::before` that covers its box counts as a fill. The drag handle's 48px target is its `.handle-touch` element.

`m3e-icon-button` draws a 40px visual. `runtime.css` gives it the 48px minimum box Compose `IconButton` has, so a proposal needs no per-page CSS for the touch target.

## Render output

`cli.mjs render` loads each proposal at exactly 411x923 (light, dark, and a red wallpaper color), 933x704, 1280x800, and 1920x1080, awaits `runtimeSettle()`, and stores `runtimeChecks()` for every view in `renders/<id>/checks.json`:

```
{ url, renders: [{ name, viewport: [width, height], result }], pass, complete }
```

`pass` and `complete` fold every view's `result`. `complete: false` means not verified. A proposal that declares states adds two more views per state after these four device views, `<state>-light` and `<state>-tablet`; they count in `pass` and `complete` like the others.

`render` also writes `renders/<id>/light-full.png`: the default-state phone light view (411 wide) captured at the full height of the document, so an agent can review content below the first screen. It is for your own review only: it is not in the contact sheet, not in `checks.json`, and not a judged view, and the checks run on the viewport views only. The capture shows what the document shows: a page whose own inner scroller (`overflow: auto` on an element) holds the content shows only that scroller's visible part, and fixed elements such as the floating toolbar stay at the viewport position.

## Rebuild

```
npm run build
```

builds this runtime (`vite.runtime.config.mjs`, `assets.mjs`). Output is deterministic. After changing anything here, commit `../public/runtime/` with the source.
