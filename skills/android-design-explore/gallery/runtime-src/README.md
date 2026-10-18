# Proposal authoring guide

A proposal is an `index.html` and a `style.css` that load this runtime, in one folder. Read this before writing markup. The maintainer notes (what the runtime contains, the rule checker's internals, theme messages, rebuild) are in [`docs/proposal-runtime.md`](../../../../docs/proposal-runtime.md).

## Page contract

A proposal page starts like this:

```html
<link rel="stylesheet" href="/runtime/runtime.css">
<link rel="stylesheet" href="style.css">
<script type="module" src="/runtime/m3e.js"></script>
```

Wrap the page in `<m3e-theme color="#RRGGBB" variant="tonal-spot" scheme="light">`. The folder's own files are referenced by relative path, because `view.html` inlines the runtime, fonts, and proposal files as data URIs. `sample/` has a working page.

## Authoring rules

This is the syntax the lint enforces. `render` runs it first and, on a violation, prints the file and line and draws nothing.

- **Files.** A proposal is one `index.html` and one `style.css`, plus optional script files. No `style=""`, inline `<style>`, or inline `<script>` (one without `src`). Load a script with `<script type="module" src="app.js">`, a path inside the proposal folder.
- **Scripts.** Each script is self-contained, because `view.html` inlines every file into one document: no relative `import` or `export ... from`, no dynamic `import()`, no `import.meta`, no CSS `@import`, and no file URLs built in JS. Keep state in memory only (no `localStorage`, cookies, or network requests); the lint cannot check that.
- **Text.** Use the `.t-*` role classes, or a custom voice: a class named `.voice-<name>`, declared once, whose selector is that single class. A voice is the only place `font-size`, `font-weight`, `line-height`, `letter-spacing`, `font-variation-settings`, or `font-family` may appear; a `font` shorthand is rejected everywhere. Plain text is Google Sans Flex, so a voice usually comes from `font-variation-settings`, weight, and width.
- **Color.** Use only `var(--md-sys-color-*)` role variables. Raw hex, `rgb(`, `hsl(`, and named colors are rejected; `transparent`, `currentColor`, and `inherit` are fine. The `primary-dim`, `secondary-dim`, `tertiary-dim`, and `error-dim` roles are rejected because Compose has no matching role. A color that is not a role (a dish or category color) goes in one `/* static colors */` block per stylesheet, which holds only `--static-*` declarations (no `[scheme="dark"]` or `prefers-color-scheme` overrides) and which the lint does not check inside. For different light and dark values use `light-dark()`: `<m3e-theme>` sets `color-scheme`, so it follows `scheme="light"`, `"dark"`, and `"auto"` (OS), including `view.html`'s light/dark control.

  ```css
  /* static colors */
  :root {
    --static-tomato: light-dark(#D84315, #FFAB91);
  }
  ```

  Use it as `background: var(--static-tomato)`.
- **Layout.** A flex or grid child that holds content needs `min-width: 0`, and grid tracks use `minmax(0, 1fr)` rather than `1fr`, or long content pushes the page wider than the phone. `pageOverflow` fails a page wider than its viewport.

## Registered elements

The runtime registers these `@m3e/web` elements: `m3e-theme`, `m3e-app-bar`, `m3e-button`, `m3e-button-group`, `m3e-icon-button`, `m3e-split-button`, `m3e-fab`, `m3e-fab-menu` (with `-item` and `-trigger`), `m3e-card`, `m3e-list` and the list item elements, the chip elements (`m3e-chip`, `m3e-assist-chip`, `m3e-filter-chip`, `m3e-input-chip`, `m3e-suggestion-chip` and their sets), `m3e-toolbar`, `m3e-nav-bar`, `m3e-nav-item`, `m3e-nav-rail`, `m3e-loading-indicator`, `m3e-circular-progress-indicator`, `m3e-linear-progress-indicator`, `m3e-shape`, `m3e-switch`, `m3e-slider`, `m3e-form-field`, `m3e-bottom-sheet` (with `-action` and `-trigger`), `m3e-dialog` (with `-action` and `-trigger`), `m3e-menu` (with the `m3e-menu-item` family, `-trigger`, and `-item-group`).

Any registered element may be used. `references/compose-mapping.md` records which patterns were ported to Compose, rendered, and matched (`verified`); the port reports anything not verified or not mapped. Build anything that is not registered from plain HTML layout (`div`, `span`, headings, lists) styled with role tokens, as long as the result has a plain Compose layout counterpart (`Box`, `Row`, `Column`, `Text`, `Surface`).

`m3e-icon` is not registered: use `p-icon`. Two runtime pieces fill gaps in the library: `<p-floating-toolbar>` wraps one `<m3e-toolbar>` and floats it at bottom center, rounded and elevated, with its `m3e-button[toggle]` children single-select (the selected one is `tonal`, the others `text`); and `--p-inset-top` and `--p-inset-bottom` (default `0px`) stand in for the status and navigation bars, which a proposal sets to show system bars.

## Page defaults

`runtime.css` sets `box-sizing: border-box` on every light-DOM element and pseudo-element, so padding and borders sit inside a declared size, as in Compose (`Modifier.height(56.dp).padding(12.dp)` is 56dp). Shadow DOM inside `@m3e/web` components is untouched. Only the page's own `m3e-theme` fills the viewport height (`min-height: 100vh`); a nested `m3e-theme`, such as a preview card in another scheme, takes its content height.

## Type roles

One class per Material 3 role: `.t-display-large`, `.t-display-medium`, `.t-display-small`, `.t-headline-large|medium|small`, `.t-title-large|medium|small`, `.t-body-large|medium|small`, `.t-label-large|medium|small`, and the same names with an `-em` suffix for the emphasized roles. Each baseline class equals its Compose `MaterialTheme.typography` role exactly. An emphasized class carries Google's emphasized token values, which the pinned material3 does not apply to `typography.<role>Emphasized`, so the port copies the baseline role with the token weight and tracking (`references/compose-mapping.md`, type roles).

Optical sizing is off (`font-optical-sizing: none`), because Compose never sets a variable font's `opsz` axis and with it on 14px Google Sans Flex measured 5% wider than in Compose. Text draws at each font's default `opsz`; a voice that wants another sets it in `font-variation-settings` (`"opsz" 52` on a 52px Fraunces title), and the port passes the same value with `FontVariation.opticalSizing`.

Fonts: every element is Google Sans Flex. A `.voice-*` rule may set `font-family` to one bundled face (`Fraunces`, `"Bricolage Grotesque"`, `"Big Shoulders Display"` at `font-size` 24px and up in that rule; `Doto` at 36px and up; `"Google Sans Code"` at any size), optionally followed by `serif`, `sans-serif`, or `monospace`. The lint rejects any other family and a brand face below its minimum, with the file and line.

Large voices: Google Sans Flex at heavy weights and large sizes needs a line height of at least about 1.2x the font size; heavy or narrow voices need about 1.25x (measured). The checker flags a tighter line box as overflow.

## Icons

`<p-icon name="search"></p-icon>` draws a 24px `currentColor` Material Symbols Rounded glyph. `name` is any symbol name from fonts.google.com/icons (`directions_car`, `hiking`); the lint rejects a name that is not one, because an unknown name would render as a word. Add `filled` for FILL 1 and `label="..."` to expose the icon to assistive tech (otherwise it is `aria-hidden`).

Size an icon that stands alone, or sits in a list item slot, with `size`, one of the Compose icon sizes 18, 20, 24 (the default), 28, 32, 36, 40, 48; the port is `Modifier.size(N.dp)`. Inside a component that forces its icon size (buttons, icon buttons, FABs and FAB menu items, chips, nav items, tabs, menu items, and the others listed in `lint.mjs`), `size` has no effect and the lint rejects it; the port uses that component's default icon size. A larger icon in a button comes from a larger component size, such as a 40px icon from `m3e-icon-button size="extra-large"`. A 48dp icon in a play button has no such size: build that button from a plain styled element with a standalone `size="48"` icon. A `font-size` on an icon is rejected like any `font-size` outside a voice.

## Text fields

`m3e-form-field` hosts a native `<input>` or `<textarea>`. Write it like this:

```html
<m3e-form-field variant="filled" hide-subscript="always">
  <label slot="label" for="amount">Amount</label>
  <span slot="prefix-text">$</span>
  <input id="amount" type="text" inputmode="decimal" value="120.00">
</m3e-form-field>
```

Slots: `label`, `prefix` and `suffix` (a `p-icon`), `prefix-text` and `suffix-text`, `hint`, `error`. Variants are `filled` and `outlined` (the default); `<textarea>` only in the filled variant. Always set `hide-subscript="always"`, or `"never"` when the field shows hint or error text: the default `auto` reserves a 20px row under the field that Compose does not have. An invalid field shows the `error` slot and hides `hint`; add the `error` attribute to draw the static error state. Give the field a width with `--m3e-form-field-width` in `style.css` (270px by default, 280dp is Compose's minimum). A value field holds static text: the proposal shows the `value` (or `placeholder`) you write.

## Component notes

`m3e-list variant="segmented"` items draw no container of their own: the default is the surface color, as in Compose, where `ListItemDefaults.segmentedColors` also defaults to the surface color (`references/compose-mapping.md`). When the list sits on a surface, set the container to a role on the list or an ancestor: `--m3e-segmented-list-item-container-color: var(--md-sys-color-surface-container)`. Setting `--m3e-list-item-container-color` on an ancestor has no effect, because the segmented list resets it from the segmented token; the segmented token is the one to use.

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

`preserveAspectRatio="none"` stretches the path to the box (a dot stretches too, so draw markers in HTML or use a fixed aspect: drop `preserveAspectRatio` and set `height: auto`). Give the `svg` `role="img"` and an `aria-label`, and repeat the key numbers in text.

## Render output

`cli.mjs render <proposal-folder> [--open]` lints the proposal, then loads it at exactly 411x923 (light, dark, and a red wallpaper color), 933x704, 1280x800, and 1920x1080 and runs the rule checks on every view. It writes `<proposal-folder>/renders/`, removed and rewritten each run:

- `light.png`, `dark.png`, `wallpaper.png`, `foldable.png`, `tablet.png`, `desktop.png`: the checked views.
- `light-full.png`: the phone light view at the full height of the document, for review below the first screen. It is not checked. A page whose inner scroller (`overflow: auto` on an element) holds the content shows only that scroller's visible part.
- `review.png`: every checked view on one captioned image, a phone row over a large row, at most 1600px wide. It is not checked.
- `checks.json`: `{ url, renders: [{ name, viewport: [width, height], result }], pass, complete }`.
- `view.html`: one self-contained file to switch devices, light and dark, seed color, and palette. `--open` opens it in the default browser.

`pass` is false when any view fails a check (touch target under 48x48, text outside Google Sans Flex, clipped or overlapped text, a page wider than its viewport, text contrast below 4.5:1, a `data-primary` shorter than another action, more than one FAB). `complete` is false when a check could not be resolved, such as text over a gradient. Treat `complete: false` as not verified, whatever `pass` says. The checker's details are in `docs/proposal-runtime.md`.
