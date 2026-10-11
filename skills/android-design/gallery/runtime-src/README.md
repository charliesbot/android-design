# Proposal runtime

Every exploration proposal is an `index.html` and a `style.css` that load this runtime. It is built from this folder into `../public/runtime/` and committed, so the installed skill never runs npm. The gallery serves it at `/runtime/`.

A proposal page starts like this:

```html
<link rel="stylesheet" href="/runtime/runtime.css">
<link rel="stylesheet" href="style.css">
<script type="module" src="/runtime/m3e.js"></script>
```

The gallery CSP is `default-src 'self'`: no inline `<style>`, no `style=""`, no inline scripts, no data URIs. Proposals keep styles in `style.css`. See `sample/` for a working page.

## What it contains

| File | Purpose |
| --- | --- |
| `runtime/m3e.js` | ES module: registers the `@m3e/web` 2.9.2 elements below, the `p-` pieces, the theme message listener, and `window.runtimeChecks` |
| `runtime/runtime.css` | Google Sans Flex `@font-face`, page defaults, type role classes, shim styles |
| `runtime/fonts/` | `GoogleSansFlex.woff2` (Latin subset, all variable axes) and its OFL license |
| `runtime/icons.svg` | Material Symbols Rounded sprite (weight 400, unfilled and filled), license in `icons-LICENSE.txt` |
| `runtime/sample/` | A small page that uses only runtime pieces |

## Allowed components

`m3e-theme`, `m3e-app-bar`, `m3e-button`, `m3e-button-group`, `m3e-icon-button`, `m3e-split-button`, `m3e-fab`, `m3e-fab-menu` (with `-item` and `-trigger`), `m3e-card`, `m3e-list` and the list item elements, the chip elements (`m3e-chip`, `m3e-assist-chip`, `m3e-filter-chip`, `m3e-input-chip`, `m3e-suggestion-chip` and their sets), `m3e-toolbar`, `m3e-nav-bar`, `m3e-nav-item`, `m3e-nav-rail`, `m3e-loading-indicator`, `m3e-circular-progress-indicator`, `m3e-linear-progress-indicator`, `m3e-shape`, `m3e-switch`, `m3e-slider`.

Wrap the page in `<m3e-theme color="#RRGGBB" variant="tonal-spot" scheme="light">`. `m3e-icon` is not registered: use `p-icon`.

## Type roles

One class per Material 3 role: `.t-display-large`, `.t-display-medium`, `.t-display-small`, `.t-headline-large|medium|small`, `.t-title-large|medium|small`, `.t-body-large|medium|small`, `.t-label-large|medium|small` (15), and the same names with an `-em` suffix for the emphasized roles (15). Sizes, line heights, tracking, and weights are generated from `type-scale.json`, extracted from the pinned AndroidX Material 3 artifact, so each class equals its Compose `MaterialTheme.typography` role exactly.

## Icons

`<p-icon name="search"></p-icon>` draws a 24px `currentColor` glyph; add `filled` for the filled variant, `label="..."` to expose it to assistive tech (otherwise it is `aria-hidden`). Size it with `font-size`. The allowed names are the single list in `icons.json`; they all exist as `Icons.Rounded.*` in Compose. To add one, add it to `icons.json` and rebuild.

## Shims

- `<p-floating-toolbar>`: wrap one `<m3e-toolbar>`. Floats it at bottom center, 16px above `--p-inset-bottom`, rounded and elevated. Its `m3e-button[toggle]` children are single-select: the selected one is `tonal`, the others `text`, 48px tall.
- Type roles: the emphasized classes above, because `@m3e/web` has no type scale component.
- `--p-inset-top`, `--p-inset-bottom`: default `0px`. Stand-ins for the status and navigation bars, which do not exist on the web. A proposal sets them to show system bars.

## Theme messages

The gallery's preview-only theme controls post `{type: "theme", color, variant, scheme}` to the proposal frame. `m3e.js` applies valid values to the page's `m3e-theme` and ignores messages from other origins.

## Rule checker

`window.runtimeChecks()` returns one JSON object:

```
{ pass, viewport, touchTargets: {checked, under48}, fonts: {offenders}, overflow,
  primary: {status, marked, largest, reason?}, fabs: {count, ok}, contrast: {fails, passed}, unresolved }
```

`pass` is false when any check failed. Checks: interactive elements under 48x48 (a component is measured as a whole, other shadow roots are searched); text not in Google Sans Flex (`p-icon` excluded); text whose box clips it; the element marked `data-primary` must be the largest interactive element; at most one FAB; text contrast of 4.5:1 (3:1 at 24px and up, or bold at 18.66px and up). Cases it cannot resolve, such as a gradient or translucent background, a page with no `data-primary`, or an ancestor with opacity below 1, are listed in `unresolved` and are never counted as passes. Decorative text (`aria-hidden`) is exempt from overflow and contrast. Run it at each device size you review; it reads the live layout.

## Rebuild

```
npm run build
```

builds the gallery, then this runtime (`vite.runtime.config.mjs`, `assets.mjs`). Output is deterministic. After changing anything here, commit `../public/runtime/` with the source.
