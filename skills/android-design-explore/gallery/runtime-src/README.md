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

The runtime registers these `@m3e/web` elements: `m3e-theme`, `m3e-app-bar`, `m3e-button`, `m3e-button-group`, `m3e-icon-button`, `m3e-split-button`, `m3e-fab`, `m3e-fab-menu` (with `-item` and `-trigger`), `m3e-card`, `m3e-list` and the list item elements, the chip elements (`m3e-chip`, `m3e-assist-chip`, `m3e-filter-chip`, `m3e-input-chip`, `m3e-suggestion-chip` and their sets), `m3e-toolbar`, `m3e-nav-bar`, `m3e-nav-item`, `m3e-nav-rail`, `m3e-loading-indicator`, `m3e-circular-progress-indicator`, `m3e-linear-progress-indicator`, `m3e-shape`, `m3e-switch`, `m3e-slider`.

Registered does not mean usable. A proposal may use only the components `references/compose-mapping.md` marks verified. Anything else it builds from plain HTML layout (`div`, `span`, headings, lists) styled with role tokens, as long as the result has a plain Compose layout counterpart (`Box`, `Row`, `Column`, `Text`, `Surface`).

Wrap the page in `<m3e-theme color="#RRGGBB" variant="tonal-spot" scheme="light">`. `m3e-icon` is not registered: use `p-icon`.

## Type roles

One class per Material 3 role: `.t-display-large`, `.t-display-medium`, `.t-display-small`, `.t-headline-large|medium|small`, `.t-title-large|medium|small`, `.t-body-large|medium|small`, `.t-label-large|medium|small` (15), and the same names with an `-em` suffix for the emphasized roles (15). Sizes, line heights, tracking, and weights are generated from `type-scale.json`, extracted from the pinned AndroidX Material 3 artifact, so each class equals its Compose `MaterialTheme.typography` role exactly.

Fonts: every element is Google Sans Flex. A `.voice-*` rule may set `font-family` to one bundled face (`Fraunces`, `"Bricolage Grotesque"`, `"Big Shoulders Display"` at `font-size` 24px and up in that rule; `Doto` at 36px and up; `"Google Sans Code"` at any size), optionally followed by `serif`, `sans-serif`, or `monospace`. Publishing rejects any other family, a `font` shorthand, and a brand face below its minimum, with the file and line. The fonts check lists rendered text whose first family is not bundled or is a brand face below its minimum size.

Large voices: Google Sans Flex at heavy weights and large sizes needs a line height of at least about 1.2x the font size (the `android-design` skill's `SKILL.md` section 5: "Line height about 1.2x on large styles"); heavy or narrow voices need about 1.25x (measured). The checker flags a tighter line box as overflow, because the glyph box then exceeds the box that holds it.

## Icons

`<p-icon name="search"></p-icon>` draws a 24px `currentColor` Material Symbols Rounded glyph, by ligature from the bundled variable font. `name` is any symbol name from fonts.google.com/icons (`directions_car`, `hiking`, `cloud`); add `filled` for FILL 1, `label="..."` to expose it to assistive tech (otherwise it is `aria-hidden`). Size it with `font-size`; weight is 400, grade 0, and the optical size follows the size. Publishing rejects a name that is not in `symbols.json`, with the file and line. An unknown name would otherwise render its text as a word, so the lint is the guard.

## Shims

- `<p-floating-toolbar>`: wrap one `<m3e-toolbar>`. Floats it at bottom center, 16px above `--p-inset-bottom`, rounded and elevated. Its `m3e-button[toggle]` children are single-select: the selected one is `tonal`, the others `text`, 48px tall.
- Type roles: the emphasized classes above, because `@m3e/web` has no type scale component.
- `--p-inset-top`, `--p-inset-bottom`: default `0px`. Stand-ins for the status and navigation bars, which do not exist on the web. A proposal sets them to show system bars.

## Theme messages

`view.html`'s theme controls post `{type: "theme", color, variant, scheme}` to the proposal frame. `m3e.js` applies valid values to the page's `m3e-theme` and ignores messages from other origins. `render` does not use messages: it sets the `scheme` and `color` attributes on `m3e-theme` directly in the page.

## Rule checker

`window.runtimeChecks()` returns one JSON object:

```
{ pass, complete, viewport, touchTargets: {checked, under48}, fonts: {offenders}, overflow, midWordBreaks, overlaps, pageOverflow: {fails, scrollWidth, offenders},
  primary: {status, marked, largest, reason?}, fabs: {count, ok}, contrast: {fails, passed}, unresolved }
```

`pass` is false when any check failed. `complete` is false when anything is in `unresolved`. Treat `complete: false` as not verified, whatever `pass` says: an unresolved case is never a pass.

Checks: the page wider than its viewport (`pageOverflow`, a failure; names up to 5 elements past the right edge, widest first, except inside a deliberate horizontal scroller narrower than the viewport; a `position: fixed` or `sticky` element that is partly visible but crosses the left or right edge also fails, while one fully off screen is treated as hidden on purpose); interactive elements under 48x48 (a component is measured as a whole; its target is the larger of its host box and any `.touch` element in its shadow root, and the reported size is that union; for `m3e-slider` the host is measured and `m3e-slider-thumb` is skipped, because Compose's whole 48dp Slider is the touch target); text not in Google Sans Flex (`p-icon` excluded); text whose box clips it; a word split across two lines with no hyphen (`midWordBreaks`: it lists the element and the words, and measures segments between spaces, hyphens, soft hyphens, zero-width spaces, and slashes, skipping `hyphens: auto` and CJK text; the usual cause is `overflow-wrap: anywhere` or `word-break` on a box too narrow for its word); text covered by another element painted above it (`overlaps`: the text is sampled across each line, and it fails when at least a fifth of the samples are hit through a nearly opaque element, such as a floating toolbar or an opaque sheet; pointer-events: none and fully transparent elements, translucent scrims, gradients, the text's own ancestors and descendants, and a fixed or sticky element over content the document can scroll clear of are not counted); the element marked `data-primary` must be the largest interactive element; at most one FAB; text contrast of 4.5:1 (3:1 at 24px and up, or bold at 18.66px and up).

Text is measured where it renders. Slotted text, such as a button label, is attributed to the element its slot places it under inside the component's shadow root (`m3e-button#cta > div.label`), not to the light-DOM host. Color and font come from that element, the fill from the first opaque background up the flat tree (slot, shadow ancestors, host, light ancestors), so a filled button is measured against its container, not the page. Clipping is tested on the shadow element that clips. Cases it cannot resolve, such as a gradient or translucent background, a page with no `data-primary`, or an ancestor with opacity below 1, are listed in `unresolved`. Decorative text (`aria-hidden`) is exempt from overflow and contrast. Run it at each device size you review; it reads the live layout.

`m3e-icon-button` draws a 40px visual. `runtime.css` gives it the 48px minimum box Compose `IconButton` has, so a proposal needs no per-page CSS for the touch target.

## Render output

`cli.mjs render` loads each proposal at exactly 411x923, 933x704, 1280x800, and 1920x1080, awaits `runtimeSettle()`, and stores `runtimeChecks()` for every view in `renders/<id>/checks.json`:

```
{ url, renders: [{ name, viewport: [width, height], result }], pass, complete }
```

`pass` and `complete` fold every view's `result`. `complete: false` means not verified.

## Rebuild

```
npm run build
```

builds this runtime (`vite.runtime.config.mjs`, `assets.mjs`). Output is deterministic. After changing anything here, commit `../public/runtime/` with the source.
