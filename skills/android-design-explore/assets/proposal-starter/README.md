# Proposal starter

Each proposal is a folder `project/proposals/<id>/` with an `index.html` and the files it uses. `proposals/a` is a working skeleton: copy it for each extra proposal and change the design, not the rules.

Page contract: load `/runtime/runtime.css`, your `style.css`, and `/runtime/m3e.js` (module); wrap the page in `<m3e-theme color="#RRGGBB" variant="tonal-spot" scheme="light">`; mark the primary action with `data-primary`.

Before writing markup, read `gallery/runtime-src/README.md` (relative to the skill directory): it holds every rule the lint enforces, the components, the open states, and `window.runtimeChecks()`.

Check a draft with `node <skill-dir>/gallery/cli.mjs render <exploration>`; it needs no server.
