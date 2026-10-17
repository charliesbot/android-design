---
name: android-design-explore
description: >
  Explore Android app ideas as HTML proposals before implementation, without editing the app. Use for
  brainstorming screens, redesign alternatives, visual directions, or proposals to compare and iterate on in
  chat: it renders phone, foldable, tablet, and desktop views plus a contact sheet and a self-contained view.html.
  Builds on the android-design skill, which owns every design decision.
---

# Visual design exploration

Read and follow the `android-design` skill for every design decision: its section 0 answers, component choices, theming, layout, and its screen check. This skill owns the workflow around them: brief, create, author, render, iterate in chat, approve, hand off and port, clean up.

Requirements: Node.js 22+ and Google Chrome (the `render` command drives it headlessly; the harness needs no browser). If either is missing, report it rather than installing it. Use image generation only for assets, not as a substitute for a proposal.

For multiple proposals or an exploration request, build HTML proposals on the bundled Material 3 Expressive runtime, render them headlessly, and iterate with the user in chat. The user's approval, said in chat, finishes exploration without implementing anything.

## Understand the brief

Read the requesting app's relevant screens, theme, assets, content, and dependency versions. Match the prompt's scope: a feature exploration preserves the surrounding visual language unless structural changes are needed; a redesign can go further. Answer section 0 of the `android-design` skill for the directions. Unless the user specifies otherwise, start with three distinct compositions of the same task and content, not three recolorings.

Give each direction its own seed: the app's brand color or a content color from the subject (a dish, an album, a trip), never the runtime's default palette. Make at least one direction a hero moment that layers three or more tactics (type voice, shape, color role, container form; see section 3 of the `android-design` skill) and name them in its rationale.

Keep app code and dependencies unchanged. Proposals must transfer to the app's existing libraries. Use representative data and non-sensitive assets, never credentials, databases, or personal data.

## Create or resume

Set `CHROME_PATH` when Chrome is not at its usual location.

Resolve this skill's installed directory and run:

```bash
node <skill-dir>/gallery/cli.mjs create --project <requesting-repo> --title "Today screen redesign"
```

The helper copies the HTML proposal starter to `design-explorations/<unique-id>/project/` in that Git repository. It records a local Git exclusion without editing the tracked `.gitignore`. Tell the user the returned absolute exploration path. Keep the installed template and production app untouched. A new app idea still needs a user-selected repository; do not silently write to the skill repository.

Resume the same exploration directory for revisions. Proposals live in `project/proposals/<id>/`; renders in `renders/` are regenerated output.

Renders cover phone, foldable, tablet, and desktop. Wear stays outside this workflow; use the Android starter's experimental Wear shell (`create --starter android`) only when explicitly requested, without claiming validated Wear quality or widening `android-design`'s validated scope.

## Author proposals

A proposal is `proposals/<id>/index.html` and `style.css` on the runtime, plus optional script files (see below). Copy `proposals/a` for each extra direction and change the design, not the rules. Details are in `project/README.md` and the runtime contract in `gallery/runtime-src/README.md`. `render` rejects violations before drawing anything.

- HTML on the runtime only, wrapped in `<m3e-theme color="#RRGGBB" variant="tonal-spot" scheme="light">`. Use only HTML that has a Compose counterpart.
- Text uses the `.t-*` role classes, or one `.voice-<name>` class declared once. No ad hoc sizes or weights. Plain text uses Google Sans Flex from the runtime, so a voice usually comes from `font-variation-settings`, weight, and width. The runtime turns optical sizing off, so an axis a voice does not name stays at the font's default, as in Compose; a voice that sets `"opsz"` (say `"opsz" 52` on a 52px Fraunces title) tells the port to pass it, because Compose never sets `opsz` itself and a port that drops it renders such a title about 4% wider. `font-family` is allowed only inside a single-class `.voice-<name>` rule and only names a bundled family: `"Google Sans Flex"`, one brand face (`Fraunces`, `"Bricolage Grotesque"`, `"Big Shoulders Display"`) with `font-size` of at least 24px in that rule, `Doto` with `font-size` of at least 36px, or `"Google Sans Code"` at any size, optionally followed by `serif`, `sans-serif`, or `monospace`. Use one brand face per proposal, on Display and Headline voices only; Body, Label, and Title stay Google Sans Flex. Any other family, a `font` shorthand, or a brand face below its size is rejected by the lint, and the checker flags rendered text in a brand face below its size.
- Color uses `--md-sys-color-*` roles. A non-role color (a dish or category color) goes in one `/* static colors */` block of `--static-*` properties. For different light and dark values, use `light-dark()` inside that block: `<m3e-theme>` sets `color-scheme` on the page, so it follows `scheme="light"`, `"dark"`, and `"auto"` (OS), including `view.html`'s light/dark control. The lint does not check values inside the block, so this passes:

  ```css
  /* static colors */
  :root {
    --static-tomato: light-dark(#D84315, #FFAB91);
  }
  ```

  Use it as `background: var(--static-tomato)`. There is one block per stylesheet and it holds only `--static-*` declarations, so do not put `[scheme="dark"]` or `prefers-color-scheme` overrides in it.
- Icons: any Material Symbols Rounded name from fonts.google.com/icons, through `<p-icon name="...">` (add `filled` for the filled glyph). Size a standalone icon (or one in a list item) with `size="N"`, never `font-size`: N is a Compose icon size (18, 20, 24 default, 28, 32, 36, 40, 48; the port is `Modifier.size(N.dp)`). Inside a component that forces its icon size (button, icon button, FAB, FAB menu item, chip, nav item, tab, menu item, and the others in `lint.mjs`) `size` is rejected, because the component sets the icon size: the port uses that component's default icon size, so for a bigger icon pick a bigger component size (a 40px icon is `m3e-icon-button size="extra-large"`; a 48dp icon in a play button has no such size, so build that button from a plain styled element with a standalone `size="48"` icon). The lint rejects names that are not Material Symbols and sizes that are not Compose icon sizes.
- Corners come from the corner scale, spacing from the 8dp scale.
- Components come only from `references/compose-mapping.md` rows marked `verified`, and a size step-down (see the button label rule in `android-design`) goes only to a size marked `verified` there. Anything else is built from plain HTML layout with role tokens when it has a plain Compose layout counterpart (`Box`, `Row`, `Column`, `Text`, `Surface`); otherwise leave it out.
- Text fields: `<m3e-form-field variant="filled" hide-subscript="always"><label slot="label" for="x">Label</label><input id="x" value="..."></m3e-form-field>` (variant `outlined` the same; hint or error text goes in `slot="hint"` or `slot="error"` with `hide-subscript="never"`, and an error field adds the `error` attribute; `p-icon slot="prefix"` or `"suffix"` for icons, `<span slot="prefix-text">$</span>` for an amount). A field draws static text only, in memory: it shows the value or placeholder you write, never persisted. Use `<textarea>` only in the filled variant. The rows marked `verified` in `references/compose-mapping.md` are the supported ones.
- Overlays: a bottom sheet, dialog, or menu comes only from its `verified` row in `references/compose-mapping.md` (`m3e-bottom-sheet modal handle`, `m3e-dialog`, `m3e-menu`), never from plain HTML, so the port has something to compare against. A hidden overlay is invisible to every default render and check, so declare each overlay, and any other important hidden state, as a named state: `<meta name="p-states" content="queue lyrics">` in the head of `index.html` (space-separated names of lowercase letters, digits, and hyphens), and in the proposal's own script `window.proposalState = name => { ... }`, which opens that state synchronously or returns a promise (`document.getElementById(name).show()` for a sheet or dialog, `menu.show(anchorElement)` for a menu; a state opens from the closed default, never on top of another). A brief that says an action opens something over the screen (lyrics, a queue, credits over a player) needs one state per overlay. Content behind an open modal is exempt from the overlap, contrast, and touch-target checks and the primary-action check is skipped, while the overlay and everything in it are checked in full. A side sheet has no verified row: leave it out.
- A large voice sets `line-height` to at least about 1.2 times its `font-size`, and about 1.25 for heavy or narrow voices (Google Sans Flex, Big Shoulders Display), which clip at 1.2 (measured in eval renders, not a Google rule).
- No inline styles or scripts. A proposal is one `index.html` and one `style.css`, and may add its own script files for in-memory interaction only: toggles, an add sheet, a selected tab, and `window.proposalState` for declared states. Load them with `<script type="module" src="app.js">`, a path inside the proposal folder. Scripts hold state in variables, never in `localStorage` or cookies, and make no network requests. Each script must be self-contained, because `view.html` inlines every file into one document: no relative `import`/`export ... from`, no dynamic `import()`, no `import.meta`, no CSS `@import`, and no file URLs built in JS (use a data URI, or put assets in the HTML or CSS). The lint rejects those with file and line, plus an inline `<script>` (one without `src`) and the style rules above; the no-storage and no-network rules are yours to keep.
- Mark the primary action `data-primary`. It must be the largest interactive element; navigation chrome (nav bar, rail, drawer) and slider or progress tracks and text fields are not compared.
- Truncate on purpose with `text-overflow: ellipsis` (horizontal) or a line clamp (vertical), like Compose's `Text(maxLines, overflow = Ellipsis)`; the overflow check passes that axis only, and still fails text clipped with neither. Button labels never truncate or wrap. Content may scroll under an opaque fixed or sticky bar only if scrolling can move every row fully clear of it, so pad the scroller by the bar height (Compose `contentPadding`); text that can never scroll clear is an overlap.
- Compact below 840px, expanded from 840px up.

## Render and check

Do not judge from code alone. `<exploration>` is the folder `create` printed (`design-explorations/<unique-id>`), not its parent. Render every proposal, or only the ids you name:

```bash
node <skill-dir>/gallery/cli.mjs render <exploration> [id ...] [--open]
```

`render` lints first. A lint error prints `file:line` and exits 2 with nothing rendered: fix and rerun. Otherwise it renders headlessly in Chrome, with no server and no port, and writes under `<exploration>/renders/`:

- `<id>/light.png`, `dark.png`, `wallpaper.png` (phone, 411x923), `foldable.png` (933x704), `tablet.png` (1280x800), `desktop.png` (1920x1080).
- `<id>/<state>-light.png` (phone) and `<id>/<state>-tablet.png` for each state the proposal declares in `p-states`, taken after `window.proposalState(<state>)` opens it. A missing `proposalState`, or one that throws, fails that state's views with the reason.
- `<id>/checks.json`: the runtime checks of every view, state views included (named `<state>-light`, `<state>-tablet`), with `pass` and `complete`.
- `<id>/view.html`: one self-contained file the user can double-click to switch devices, light and dark, seed colors, palette, and, for a proposal that declares states, the state (Default or any declared one).
- `contact-sheet.png`: one row per proposal across phone, foldable, tablet, and desktop, default state only.

1. Read `checks.json` for each proposal and fix every failure. Treat `unresolved` entries as unchecked, not passed. A result with `complete: false` means some view was not checked: treat it as not verified and report it.
2. Look at the screenshots yourself, every view of every proposal (state renders included), not just the phone. Judge hierarchy, character, and color balance using the `android-design` screen check, and confirm each larger view reveals what your `android-design` section 0 adaptation answer promised. Plan for the larger views to show more at once than the phone's first screen (a second pane of content side by side, more of a list, more detail). Google: "Additional space doesn't just mean making the same thing bigger." The eval bar is stricter than Google's five questions, which also count repositioning: the same single view only rearranged fails it.
3. Rerun `render` after each edit until the checks pass and the screenshots hold up.

If the harness cannot view images, report visual quality as unverified and do not claim the screen check passed.

## Show the user

Show `renders/contact-sheet.png` in chat: attach the image if your harness can, otherwise give its absolute path. Then open the proposals in the user's browser: when you run on the user's own machine, rerun the final render with `--open` (`node <skill-dir>/gallery/cli.mjs render <exploration> [id ...] --open`), which opens each rendered `<id>/view.html` in the default browser after the render finishes, so the user can try other devices, colors, and states. Open only the final render of a round, never while you are still iterating: `render` rewrites `renders/<id>/`. When you run remotely (a cloud session) or the user is not at that machine, give the absolute path of each `view.html` instead. When a direction's idea lives in an overlay, show its state renders too (attach `<state>-light.png`, or give the path). Say what each direction is in one line, name the tactics the hero layers, and report any unverified check. Then stop and wait for the user.

## Iterate in chat

The user replies in chat ("I like a and b, make b warmer"). Apply clear instructions and ask a focused question only when a meaningful decision remains. Produce one refinement by default, edit `project/proposals/<id>/`, and rerun `render` for the ids you changed. To keep an earlier version for comparison, copy the folder (`b` to `b2`) before editing, then edit the copy or the original. A combination of several ideas becomes one consolidated proposal. The user can ask to explore several alternatives again.

## Approve, hand off, or discard

Approval is the user saying so in chat. This skill's job ends there. Do not implement. Report the approved proposal, the path of its folder, and that `references/compose-mapping.md` is the porting table. Approval does not authorize implementation or library version changes.

A later explicit implementation request follows the requesting project's planning, branch, verification, and review rules. Port the approved proposal into the app with the mapping table, keeping the app's library versions. Then render the Compose result and compare it with the approved proposal's screenshots at the same viewports. Report unmapped or unverified elements instead of inventing a Compose equivalent. If the design cannot be preserved with the app's dependencies, ask rather than silently change the design or versions.

Keep proposals and renders until the user explicitly requests deletion, then run from the requesting repository:

```bash
node <skill-dir>/gallery/cli.mjs cleanup <exploration> --project <requesting-repo> --yes
```

Cleanup removes only a directly contained, identity-marked exploration and refuses symlinks or mismatched markers. It deletes every proposal and render. It never deletes the installed starter or the requesting app.
