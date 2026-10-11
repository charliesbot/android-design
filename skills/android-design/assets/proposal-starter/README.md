# Proposal starter

Each proposal is a folder `project/proposals/<id>/` with an `index.html` and the files it uses. `proposals/a` is a working skeleton: copy it for each extra proposal and change the design, not the rules.

Page contract: load `/runtime/runtime.css`, your `style.css`, and `/runtime/m3e.js` (module); wrap the page in `<m3e-theme color="#RRGGBB" variant="tonal-spot" scheme="light">`; lay out compact below 840px and expanded (nav rail instead of the floating toolbar) from 840px up; mark the primary action with `data-primary`. The runtime's pieces, components, and `window.runtimeChecks()` are documented in `gallery/runtime-src/README.md`.

Review a draft before publishing at `/draft/<id>/` on the running gallery server.

## Styling rules

Publishing fails on a violation and names the file and line. Each rule keeps a proposal a lookup into Compose tokens.

- Text uses only the `.t-*` role classes. A custom voice is a class named `.voice-<name>`, declared once, and is the only place `font-size`, `font-weight`, `line-height`, `letter-spacing`, or `font-variation-settings` may appear. The selector must be that single class.
- Color uses only `var(--md-sys-color-*)` role variables. A color that is not a role (a dish or category color) is declared once, in a single block that starts with the comment `/* static colors */` and declares only `--static-*` custom properties. Raw hex, `rgb(`, `hsl(`, and named colors are rejected everywhere else; `transparent`, `currentColor`, and `inherit` are fine. The `primary-dim`, `secondary-dim`, `tertiary-dim`, and `error-dim` roles are rejected because Compose has no matching role.
- Icons come only from `gallery/runtime-src/icons.json`, through `<p-icon name="...">`.
- Corners come from the Material corner scale (or `m3e-shape`), spacing from the 8dp scale (4dp for tight pairs).
- No `style=""` attributes, inline `<style>`, or inline `<script>`: the CSP forbids them and the linter rejects them. Put styles in a `.css` file and scripts behind `src`.

Static color block example:

```css
/* static colors */
:root {
  --static-tomato: #D84315;
}
```
