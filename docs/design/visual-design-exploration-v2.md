# Visual design exploration v2

**Date:** 2026-10-10

## TL;DR

Design exploration proposals become live HTML built with the Material 3 Expressive web components in `@m3e/web` and themed by Google's own color utilities, instead of Compose code rendered to PNG. The agent renders each proposal headlessly with `cli.mjs render`, fixes the rule checks, looks at one review image, and shows it to the user in chat; the user answers in chat. Each proposal also gets a self-contained `view.html` the user can double-click to try theme colors and device sizes with no server. Compose is written once, at implementation time, from the approved design and a verified mapping to Compose components. The point is speed: first results in minutes rather than ten, sharp previews, and instant device and theme switching, without giving up proposals that map one-to-one onto real Compose components. The v1 gallery, rounds, and Refine are gone; see the history line at the end.

## Design

### What stays from v1

Requests for multiple designs or for exploration start an exploration; ordinary implementation requests do not. The prompt sets the scope: a feature exploration preserves the app's visual language, a redesign can go further, and alternatives differ in design approach rather than color. Image-generated UI mockups are not proposals.

Each run of the skill is one shot: a brief in, one proposal written, rendered, and shown, then the agent stops. Iteration happens in chat. The agent shows `review.png` and gives the `view.html` path, the user replies ("make it warmer"), and the agent runs again on the same folder with the current HTML plus the feedback, overwriting the proposal; it keeps no version copies. The agent makes several proposals only when the user asks, each in its own folder. Approval is the user saying so in chat.

One folder is one proposal. The agent copies the skill's `assets/proposal-starter/` to `design-explorations/<slug>/` in the folder it is given (the app's, or any folder for a quick idea; it need not be a Git repository). The starter's own `.gitignore` of `*` keeps the proposal out of commits, and the agent never writes inside the skill's repository. There are no exploration or proposal ids and no `create` or `cleanup` command: the only command is `render`, and deleting a proposal is deleting its folder. Proposals use representative data and no credentials or personal data. Approval ends an explore-only request without changing the app; a build request continues to the port, which follows the app's own workflow and adapts the design to the app's existing library versions rather than the reverse.

### A proposal is HTML on a Material 3 Expressive runtime

A proposal is a small static web page: `index.html` and its stylesheet, built from the components in [`@m3e/web`](https://www.npmjs.com/package/@m3e/web) and wrapped in its theme element. The skill ships one prebuilt runtime bundle with the library, Google's [`material-color-utilities`](https://github.com/material-foundation/material-color-utilities), Google Sans Flex, Material Symbols, and the rule checker, committed inside the skill like the gallery's own build, so an installed skill needs no package install and proposals work offline.

The choice rests on two facts checked on 2026-10-10:

- **Component coverage.** `@m3e/web` implements nearly everything the skill's component guidance names: sized buttons with shape morph, standard and connected button groups, split button, FAB menu, toolbar, loading and wavy progress indicators, navigation bar and rail in compact and expanded modes, segmented lists, and the 35-shape library. No official Google library does: `@material/web` is in maintenance mode without Expressive components, and Angular Material and Flutter have not shipped them.
- **Color parity.** Its theme calls Google's color utilities at the 2025 color spec, the same generator Android's dynamic and seed schemes use. Compared against MaterialKolor, the Kotlin port the Compose side would use, 717 of 720 role values matched exactly across three seeds, four variants, both spec versions, and light and dark. The three differences are one divider role in one variant, and trace to a rule MaterialKolor has not yet updated from upstream. All 53 color roles reach CSS variables.

Proposals are authored to the skill's rules exactly as Compose proposals were: Google Sans Flex voices through font variation axes, color roles from the theme rather than hand-picked hex, theme-generated surfaces, the component choices of the skill's component table, and the 840dp breakpoint for adaptive layouts. A proposal may only use HTML that has a Compose counterpart in the mapping described below; layout that Compose cannot express is not a proposal.

Styling is strict, so the port is a lookup rather than a translation. Text uses only the runtime's type role classes (one per Material type role and emphasized role) or a custom voice the proposal defines once as a named class, never ad hoc sizes and weights. Color uses only theme role variables, or a named static color the proposal declares once (a dish color, a category color), never inline hex. Icons come from the Material Symbols subset that also exists in Compose. Shapes come from the corner scale and the shape library, spacing from the 8dp scale. Each of these names maps to exactly one Compose token, so nothing in an approved proposal leaves the implementer guessing.

The runtime fills the library's confirmed gaps with small shims rather than leaving each proposal to improvise: a floating toolbar wrapper, the emphasized type role classes, and inset variables that stand in for system bars. The mapping table covers shims like any other component. A library component that is listed but not yet checked, such as the large flexible app bar's collapse or press shape morph, is allowed in proposals only after a test page shows it behaves like its Compose counterpart.

### `render` and `view.html` show proposals

`render` loads each proposal at its real device viewport: Pixel 11 at 411 by 923, Galaxy Z Fold8 inner screen at 933 by 704, Pixel Tablet at 1280 by 800, and a 1080p desktop at 1920 by 1080, in dp, which equal CSS pixels. Each lands in its own window size class (the Fold8 inner screen turned to landscape is expanded, the desktop is extra large); medium has no device. It also renders phone dark and a red wallpaper-seed view. `review.png` puts every checked view on one image, a phone row over a large row, so the agent reviews one image instead of one per view; `light-full.png` shows the phone light view at full document height for a screen that scrolls.

`view.html` is one file with the runtime, fonts, and the proposal inlined, opened from `file://` or by `render --open`. Its Phone, Foldable, Tablet, and Desktop buttons resize the frame, and its light or dark, seed color, and palette variant controls re-theme it instantly and save nothing. To keep a change, the user says so in chat.

Only HTML proposals are rendered. Explorations recorded as PNG rounds stay on disk but are not displayed.

### The agent checks every render

Before showing anything, the agent runs `render`, which lints the proposal and runs the in-page rule checker on every view, then looks at `review.png`. It fixes check failures once; anything still failing, or incomplete, is reported rather than looped on. The checker covers what the rules make measurable: touch targets of at least 48dp, Google Sans Flex on every text node, text that clips or overflows, contrast of each text and background pair, the largest interactive element being the primary action, and at most one FAB. Checks it cannot resolve are reported as unresolved, never as passing.

The screenshot stays because the rules cannot see everything. A music screen in this project's evals passed every code check and still failed the distinctive-design criterion, and in the first recipes exploration a button used `primary` exactly as the rules prescribe and still competed with the hero's color. Hierarchy, character, and color balance are judged by looking.

When an agent cannot view images, it reports visual quality as unverified instead of showing the proposal on code checks alone.

### Compose is written at implementation, from a verified mapping

For an explore-only request the skill's job ends at an approved design; a build request continues to the port. Either way the exploration hands the parent agent the approved proposal's source, its renders, and a mapping table from every `m3e-*` element and pattern the runtime allows to its Compose API, with each Compose name verified against the pinned Material 3 artifact. The parent agent writes the Compose in the app under the app's own approval and branch workflow, renders it, and compares it with the approved renders.

A blind port tested this on 2026-10-10: the approved Compose screen from the first recipes exploration was rebuilt in HTML, then ported back to Compose by an agent that could not see the original. The port compiled in under two minutes and rendered nearly identical to the original, with seed-tinted surfaces the original lacked. Its differences were small style choices the HTML did not determine (a headline weight, a selected-pill color, card padding); strict role-based styling removes them. The one wrong API name was the same mistake every Compose round had made, and the verified table removes it.

### Why not the alternatives

- **Compose rendered to PNG (v1).** Real code at every round, but each round paid a cold Gradle and Robolectric render and a compile-fix loop against a moving alpha API: about ten minutes to the first round and two per refinement in the recipes test, with soft scaled images and no live switching. It remains the path for implementing in the app. The evals moved to HTML proposals as well, because the Compose eval harness was slow enough that it went unrun; Compose fidelity is checked by a manual port instead (see `evals/README.md`).
- **Compose Multiplatform on the web.** The JetBrains Material 3 artifact has every Expressive API the skill uses, and the MaterialKolor builder site shows live Compose previews in a browser. It costs a Kotlin 2.4 toolchain, a host that sets density, and a Wasm compile per round, which is far more machinery than static pages for the same comparison. See the [Compose Multiplatform research](../research/compose-multiplatform-proposal-fidelity.md).
- **A sketching tool such as m3e-canvas.** Fast, but it assembles a fixed catalog of parts on one phone frame, which produces the generic look exploration exists to avoid and cannot express type axes, image masks, or content color.

### Fidelity, dependency, and motion

UI fidelity is the priority: layout, hierarchy, type, color, shape, and component choice must transfer to Compose. Motion is not a goal of proposals. Live pages show the library's built-in transitions as a side effect, but those are not Compose's spring scheme and are not judged or transferred; motion is designed during implementation.

`@m3e/web` is maintained by one person and is not affiliated with Google. The runtime pins a version. If the library stalls or diverges from the spec, the project forks it or moves proposals to Compose Multiplatform on the web, whichever is easier at that point.

### Scope

Phone, tablet, desktop, and foldable layouts are in scope. Wear OS stays outside this workflow, without claiming validated Wear quality. Its experimental Compose shell remains in `evals/compose-starter/`.


## Links

- [Visual design exploration v1](../archive/visual-design-exploration.md): the Compose PNG design this replaces, including the original reasoning for the workflow kept here.
- [Agent-driven Refine loop](../archive/agent-refine-loop.md): the archived watch session that answered Refine without returning to chat.
- [Compose Multiplatform proposal fidelity](../research/compose-multiplatform-proposal-fidelity.md): the verified alternative renderer.
- [Android design skill](../../skills/android-design/SKILL.md): the design rules proposals follow.
- [`@m3e/web`](https://github.com/matraic/m3e): the Material 3 Expressive web components.
- [`material-color-utilities`](https://github.com/material-foundation/material-color-utilities): Google's color generator, including the 2025 spec.
- [MaterialKolor](https://github.com/jordond/MaterialKolor): the Kotlin port used on the Compose side.

## History

2026-10-15: the gallery, its server, the feedback store, rounds, publish, and the watch session were replaced by `cli.mjs render`, a self-contained `view.html` per proposal, and iteration in chat. `render` lints, renders headlessly with no port, and writes per-proposal screenshots, `checks.json`, `view.html`, and a contact sheet. The agent fixes the checks, looks at the screenshots, and shows the user the contact sheet; the user replies in chat, and approval is the user saying so. Descriptions above of the gallery, rounds, Refine, Finish, and `status` or `wait` are history. The proposal format, runtime, rule checker, and Compose port flow are unchanged. See [references/exploration.md](../../skills/android-design-explore/SKILL.md).

2026-10-17: the workflow was flattened to one folder per proposal. `create`, `cleanup`, exploration and proposal ids, named states (`p-states` and `proposalState`), the contact sheet, and the workspace helper were removed; the Compose sandbox moved from the skill to `evals/compose-starter/` and is used only by the manual port check. Overlays still exist, opened by the page's own script. The only command is `cli.mjs render <proposal-folder> [--open]`. Descriptions above of the contact sheet or of `create` in the 2026-10-15 entry are history. Runtime maintainer notes moved to [proposal-runtime.md](../proposal-runtime.md).
