# Proposal starter

Each proposal is a folder `project/proposals/<id>/` with an `index.html` and the files it uses. `proposals/a` is a working skeleton: copy it for each extra proposal and change the design, not the rules.

Page contract: load `/runtime/runtime.css`, your `style.css`, and `/runtime/m3e.js` (module); wrap the page in `<m3e-theme color="#RRGGBB" variant="tonal-spot" scheme="light">`; lay out compact below 840px and expanded (nav rail instead of the floating toolbar) from 840px up; mark the primary action with `data-primary`. The runtime's pieces, components, and `window.runtimeChecks()` are documented in `gallery/runtime-src/README.md`.

Check a draft with `node <skill-dir>/gallery/cli.mjs render <exploration>`; it needs no server.

## Styling rules

Publishing fails on a violation and names the file and line. Each rule keeps a proposal a lookup into Compose tokens.

- Text uses only the `.t-*` role classes. A custom voice is a class named `.voice-<name>`, declared once, and is the only place `font-size`, `font-weight`, `line-height`, `letter-spacing`, or `font-variation-settings` may appear. The selector must be that single class.
- Layout must fit every device width. A flex or grid child that holds content needs `min-width: 0`, and grid tracks use `minmax(0, 1fr)` rather than `1fr`, or long content pushes the page wider than the phone. The review checks fail a page wider than its viewport.
- Color uses only `var(--md-sys-color-*)` role variables. A color that is not a role (a dish or category color) is declared once, in a single block that starts with the comment `/* static colors */` and declares only `--static-*` custom properties. Raw hex, `rgb(`, `hsl(`, and named colors are rejected everywhere else; `transparent`, `currentColor`, and `inherit` are fine. The `primary-dim`, `secondary-dim`, `tertiary-dim`, and `error-dim` roles are rejected because Compose has no matching role.
- Icons are any Material Symbols Rounded name from fonts.google.com/icons, through `<p-icon name="...">` (`filled` for the filled glyph). Publishing rejects unknown names.
- Corners come from the Material corner scale (or `m3e-shape`), spacing from the 8dp scale (4dp for tight pairs).
- No `style=""` attributes, inline `<style>`, or inline `<script>`: the CSP forbids them and the linter rejects them. Put styles in a `.css` file and scripts behind `src`. A proposal may add its own script files for in-memory interaction only (no network, no storage); the lint checks that scripts have `src`, not what they do.

Static color block example (`light-dark()` gives a light and a dark value; `<m3e-theme>` sets `color-scheme` from its `scheme`):

```css
/* static colors */
:root {
  --static-tomato: light-dark(#D84315, #FFAB91);
}
```
