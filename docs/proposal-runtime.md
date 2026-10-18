# Proposal runtime

Maintainer notes for the proposal runtime in `skills/android-design-explore/gallery/runtime-src/`. Proposal authors read that folder's `README.md` instead; this document covers what is inside the runtime, how the rule checker works, how `view.html` talks to a proposal, and how to rebuild it.

The runtime is built from `runtime-src/` into `skills/android-design-explore/gallery/public/runtime/` and committed, so the installed skill never runs npm. `cli.mjs render` serves it to headless Chrome at `/runtime/`, and `view.html` inlines it.

## What it contains

Paths are in the built `public/runtime/`; the sources sit directly in `runtime-src/`.

| File | Purpose |
| --- | --- |
| `runtime/m3e.js` | ES module: registers the `@m3e/web` 2.9.2 elements listed in the runtime README, the `p-` pieces, the theme message listener, `window.runtimeChecks`, and `window.runtimeSettle` (resolves once no finite animation or transition is running; call it before `runtimeChecks()`) |
| `runtime/runtime.css` | Google Sans Flex `@font-face`, page defaults, type role classes, shim styles |
| `runtime/fonts/` | Latin-subset text woff2 with all variable axes and each OFL license: `GoogleSansFlex` (plain text, always), `Fraunces`, `BricolageGrotesque`, `BigShouldersDisplay`, `Doto` (optional brand faces for Display and Headline voices), `GoogleSansCode` (numbers and data). The family list and minimum sizes are in `fonts.json`, read by the lint and the fonts check |
| `runtime/fonts/MaterialSymbolsRounded.woff2` | The full Material Symbols Rounded variable font (axes FILL, GRAD, opsz, wght), unmodified from the pinned `material-symbols` npm package; provenance in `MaterialSymbolsRounded-SOURCE.txt`, Apache 2.0 license in `MaterialSymbolsRounded-LICENSE.txt` |
| `runtime/symbols.json` | Every Material Symbols name (from the package's type list). The lint rejects a `p-icon` name that is not in it |
| `runtime/sample/` | A small page that uses only runtime pieces |
| `runtime/checks-fixture/` | Known-bad and known-good component text (low-contrast and clipped button labels, clipped plain text) that `runtimeChecks()` must flag, plus opacity 0 text it must skip instead of leaving unresolved, and bundled-face text (Fraunces at 40px and Google Sans Code at 12px pass, Doto at 24px must be listed in `fonts.offenders`) |
| `runtime/verify/` | One section per component in `references/compose-mapping.md`, with its variants, for comparing against Compose; it marks nothing verified |

## Shims

- `<p-floating-toolbar>`: wrap one `<m3e-toolbar>`. Floats it at bottom center, 16px above `--p-inset-bottom`, rounded and elevated. Its `m3e-button[toggle]` children are single-select: the selected one is `tonal`, the others `text`, 48px tall.
- Type roles: the emphasized classes above, because `@m3e/web` has no type scale component.
- `--p-inset-top`, `--p-inset-bottom`: default `0px`. Stand-ins for the status and navigation bars, which do not exist on the web. A proposal sets them to show system bars.

## Sheet handle patch

An open modal `m3e-bottom-sheet` moves focus to its drag handle. The browser's default `:focus-visible` outline (blue) would draw around it, so `p-sheet-handle.js` turns that outline off inside the sheet's shadow root. Every other focusable `@m3e/web` part already sets `outline: none` and relies on `m3e-focus-ring`, which `m3e-theme` hides unless its `strong-focus` attribute is set; the sheet's `.handle` is the one part that omits the rule, so the patch makes it behave like the rest of the library. Drop `p-sheet-handle.js` once `@m3e/web` sets `outline: none` on the handle itself.

## Theme messages

`view.html`'s theme controls post `{type: "theme", color, variant, scheme}` to the proposal frame. `m3e.js` applies valid theme values to the page's `m3e-theme`, waits for `runtimeSettle()`, and ignores messages from other origins. `render` does not use messages: it sets the `scheme` and `color` attributes on `m3e-theme` directly in the page.

A page's own script opens overlays (a sheet, dialog, or menu) from its controls, so a person can open them in `view.html`. `render` fails a view when a modal is open on load, because an open modal makes the page behind it inert and hides its failures, and it never opens an overlay itself. The checker below still measures an overlay while it is open.

## Rule checker

`window.runtimeChecks()` returns one JSON object:

```
{ pass, complete, viewport, touchTargets: {checked, under48}, fonts: {offenders}, overflow, midWordBreaks, overlaps, pageOverflow: {fails, scrollWidth, offenders},
  primary: {status, marked, largest, reason?}, fabs: {count, ok}, contrast: {fails, passed}, unresolved }
```

`pass` is false when any check failed. `complete` is false when anything is in `unresolved`. Treat `complete: false` as not verified, whatever `pass` says: an unresolved case is never a pass.

Checks: the page wider than its viewport (`pageOverflow`, a failure; names up to 5 elements past the right edge, widest first, except inside a deliberate horizontal scroller narrower than the viewport; a `position: fixed` or `sticky` element that is partly visible but crosses the left or right edge also fails, while one fully off screen is treated as hidden on purpose); interactive elements under 48x48 (a component is measured as a whole; its target is the larger of its host box and any `.touch` element in its shadow root, and the reported size is that union; for `m3e-slider` the host is measured and `m3e-slider-thumb` is skipped, because Compose's whole 48dp Slider is the touch target); text not in Google Sans Flex (`p-icon` excluded); text whose box clips it (not horizontally when the box or its clipping ancestor truncates with `text-overflow: ellipsis`, and not vertically when it has a line clamp; the other axis is still checked, and nothing inside a button, icon button, FAB, chip or button group is exempt); a word split across two lines with no hyphen (`midWordBreaks`: it lists the element and the words, and measures segments between spaces, hyphens, soft hyphens, zero-width spaces, and slashes, skipping `hyphens: auto` and CJK text; the usual cause is `overflow-wrap: anywhere` or `word-break` on a box too narrow for its word); text covered by another element painted above it (`overlaps`: the text is sampled across each line, and it fails when at least a fifth of the samples are hit through a nearly opaque element, such as a floating toolbar or an opaque sheet; pointer-events: none and fully transparent elements, translucent scrims, gradients, the text's own ancestors and descendants, and an opaque fixed or sticky surface over content that scrolling can move fully clear of it, in the document or an inner scroller, are not counted: that is occlusion, normal scrolling UI. Scrolling down clears a bar in the lower half of the scroller, scrolling back up clears one in the upper half, each limited to the scroll range left or used, so a scroller needs padding at least the bar height or its first or last row is an overlap. A pinned ancestor above the scroller, such as a fixed app shell, does not pin the text, and a pinned surface as tall as the scroller's viewport still counts. Text covered where nothing scrolls, or text that is itself pinned, still fails); the element marked `data-primary` must be at least as tall as every other action (ties pass, within 1px). Height is the visual container (the component's `.base`, else its box), which is the button size step: width follows the label, and a standard icon button's 48px touch box is not its 40px visual. Actions are `m3e-button` (including toggle), `m3e-icon-button`, `m3e-fab`, `m3e-fab-menu-trigger`, `m3e-split-button`, the `m3e-*chip` elements, and a native `button` or `[role=button]` that is not part of a row. Content is not compared: `m3e-list-action`, `m3e-list-option`, `m3e-card[actionable]`, and a native button inside `m3e-list-item`, `m3e-card`, or `li` (or with a `link`, `listitem`, `row`, or `option` role) may be larger than the primary. A tile built as a bare native button is an action, so build a tappable tile as `m3e-card actionable`. Elements inside navigation chrome, `m3e-nav-bar`, `m3e-nav-rail`, the labeled `m3e-button[toggle]` items of `p-floating-toolbar` (the compact navigation; its icon buttons and non-toggle buttons are still compared), the `start` and `end` slotted drawers of `m3e-drawer-container` (not its main content), `<nav>` or `role=navigation`, and sliders, progress tracks, and text fields, `m3e-slider`, `input[type=range]`, `role=slider`, `role=progressbar`, `m3e-form-field`, are also left out; at most one FAB; text contrast of 4.5:1 (3:1 at 24px and up, or bold at 18.66px and up).

Text is measured where it renders. Slotted text, such as a button label, is attributed to the element its slot places it under inside the component's shadow root (`m3e-button#cta > div.label`), not to the light-DOM host. Color and font come from that element, the fill from the first opaque background up the flat tree (slot, shadow ancestors, host, light ancestors), so a filled button is measured against its container, not the page. Clipping is tested on the shadow element that clips. Cases it cannot resolve, such as a gradient or translucent background, a page with no `data-primary`, or an ancestor with opacity below 1, are listed in `unresolved`. Decorative text (`aria-hidden`) is exempt from overflow and contrast. Run it at each device size you review; it reads the live layout.

Open overlays are found through the library's own open state: a modal `m3e-bottom-sheet` that is `:popover-open` and an `m3e-dialog` whose inner `<dialog>` is open are modal surfaces, and an open `m3e-menu` is a floating one. Behind an open modal the page is inert under a scrim, so its interactive elements are not measured for the 48px target, its text is not measured for overlap or contrast, and the `data-primary` check is reported as `skipped` (the default views check it). The modal itself and everything inside it are checked in full. Text under an open menu is not an overlap (a menu floats over content on purpose), though the menu's own text is checked. A bottom sheet that is not open sits below the viewport and is not measured at all. A dialog's text is measured against the dialog's container, which the library paints with a `::before` layer: the first opaque `::before` that covers its box counts as a fill. The drag handle's 48px target is its `.handle-touch` element.

`m3e-icon-button` draws a 40px visual. `runtime.css` gives it the 48px minimum box Compose `IconButton` has, so a proposal needs no per-page CSS for the touch target.

## Rebuild

```
npm run build
```

builds the runtime (`vite.runtime.config.mjs`, `assets.mjs`). Output is deterministic. After changing anything in `runtime-src/`, commit `../public/runtime/` with the source, and run `npm test` (`node --test tests/gallery/*.test.mjs`) and, after changing the runtime checks, `node --test tests/evals/*.test.mjs`.
