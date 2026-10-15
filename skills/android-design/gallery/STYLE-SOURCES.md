# Gallery design system

The gallery uses the pinned `@m3e/web` Material 3 Expressive components for buttons,
icon buttons, form fields, selects, switches, and dialogs. The shell and active proposal share semantic
color roles, seed, palette, and light/dark mode. Labeled seed swatches use
Material buttons. The native textarea sits within a Material form field;
there is no duplicate custom select, button, or dialog implementation.

Google Sans Flex is shared with the proposal runtime at
`public/runtime/fonts/GoogleSansFlex.woff2`. It is a Latin subset preserving all
variable axes, generated from the Android starter's font by the runtime build.
License: `public/runtime/fonts/OFL.txt`. Other characters use the system fallback. The runtime also bundles the
brand and data faces (Fraunces, Bricolage Grotesque, Big Shoulders Display, Doto, Google Sans Code) the same way, each
with its own `<Name>-OFL.txt` beside it; the gallery shell itself uses only Google Sans Flex.

Device icons in `src/icons.mjs` use Material Symbols Rounded, weight 400, copied from `@material-symbols/svg-400`.
The proposal runtime bundles the Material Symbols Rounded variable font (`material-symbols` npm package, pinned) as
`public/runtime/fonts/MaterialSymbolsRounded.woff2`, with provenance and the Apache 2.0 license beside it
(`MaterialSymbolsRounded-SOURCE.txt`, `MaterialSymbolsRounded-LICENSE.txt`).

The canvas-first composition is specific to reviewing proposals, not editing UI.
M3E Canvas informed the workspace direction, not its exact layout or controls:
https://lnkiai.github.io/m3e-canvas/
