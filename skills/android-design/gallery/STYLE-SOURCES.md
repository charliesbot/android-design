# Gallery design system

The gallery uses the pinned `@m3e/web` Material 3 Expressive components for buttons,
icon buttons, form fields, selects, switches, and dialogs. The shell uses semantic
color roles from its own theme, independently of preview-only proposal themes.
The native color picker and textarea sit within the themed control layout;
there is no duplicate custom select, button, or dialog implementation.

Google Sans Flex is shared with the proposal runtime at
`public/runtime/fonts/GoogleSansFlex.woff2`. It is a Latin subset preserving all
variable axes, generated from the Android starter's font by the runtime build.
License: `public/runtime/fonts/OFL.txt`. Other characters use the system fallback.

Device icons in `src/icons.mjs` and the proposal runtime sprite use Material Symbols
Rounded, weight 400. The runtime sprite is generated from `@material-symbols/svg-400`.
License: Apache 2.0, `public/runtime/icons-LICENSE.txt`.

The canvas-first composition is specific to reviewing proposals, not editing UI.
M3E Canvas informed the workspace direction, not its exact layout or controls:
https://lnkiai.github.io/m3e-canvas/
