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

Requirements: Node.js 22+ and Google Chrome (`render` drives it headlessly). If either is missing, report it rather than installing it. Use image generation only for assets, not as a substitute for a proposal.

Build HTML proposals on the bundled Material 3 Expressive runtime, render them headlessly, and iterate with the user in chat. The user's approval, said in chat, finishes exploration without implementing anything.

## Understand the brief

Read the requesting app's relevant screens, theme, assets, content, and dependency versions. A feature exploration preserves the surrounding visual language unless structural changes are needed; a redesign can go further. Unless the user specifies otherwise, start with three distinct compositions of the same task and content, not three recolorings.

Give each direction its own seed: the app's brand color or a content color from the subject (a dish, an album, a trip), never the runtime's default palette. Make at least one direction a hero moment that layers three or more tactics (type voice, shape, color role, container form; see section 3 of the `android-design` skill) and name them in its rationale.

Keep app code and dependencies unchanged, so proposals transfer to the app's existing libraries. Use representative data and non-sensitive assets, never credentials, databases, or personal data.

Done when the reply answers section 0 of the `android-design` skill for each direction and each direction has its own seed.

## Create or resume

Set `CHROME_PATH` when Chrome is not at its usual location.

Resolve this skill's installed directory and run:

```bash
node <skill-dir>/gallery/cli.mjs create --project <requesting-repo> --title "Today screen redesign"
```

The helper copies the HTML proposal starter to `design-explorations/<unique-id>/project/` in that Git repository and records a local Git exclusion without editing the tracked `.gitignore`. Tell the user the returned absolute exploration path. A new app idea still needs a user-selected repository; do not silently write to the skill repository.

Resume the same exploration directory for revisions; `renders/` is regenerated output. Wear stays outside this workflow; use the Android starter's experimental Wear shell (`create --starter android`) only when explicitly requested, without claiming validated Wear quality.

## Author proposals

Before writing markup, read `gallery/runtime-src/README.md`: it is the syntax the lint enforces (type voices, color roles and static colors, icons, text fields, scripts). `project/README.md` is the page contract. A proposal is `proposals/<id>/index.html`, `style.css`, and optional script files. Copy `proposals/a` for each direction and change the design, not the rules. Done when every direction has its own proposal folder and `render` gets past the lint (a violation prints `file:line` and draws nothing).

- HTML on the runtime only, and only HTML that has a Compose counterpart.
- Components come only from `references/compose-mapping.md` rows marked `verified`, and a size step-down (see the button label rule in `android-design`) goes only to a size marked `verified` there. Anything else is built from plain HTML layout with role tokens when it has a plain Compose layout counterpart (`Box`, `Row`, `Column`, `Text`, `Surface`); otherwise leave it out. Text fields have verified rows too.
- Overlays: a bottom sheet, dialog, or menu comes only from its `verified` row (`m3e-bottom-sheet modal handle`, `m3e-dialog`, `m3e-menu`), never plain HTML. A hidden overlay is invisible to default renders and checks, so declare each one, and any other important hidden state, as a named state: `<meta name="p-states" content="queue lyrics">` in the head and `window.proposalState = name => { ... }` in the proposal's script (`gallery/runtime-src/README.md`, Open states: a menu opens from its anchor, and each state opens from the closed default). A brief where an action opens something over the screen (lyrics, a queue, credits) needs one state per overlay. A modal bottom sheet opens at about half the window height and scrolls inside, so put its key action and content near the top. Leave out a side sheet: it has no verified row.
- Mark the primary action `data-primary`. It leads its peers as `android-design` section 2 describes (normal size with the strongest fill by default). The check compares its visual height (the button size step) with the other actions on the screen; content (list rows, and `m3e-card actionable` cards or tiles), navigation chrome, sliders, progress, and text fields are not compared. Build a tappable tile as `m3e-card actionable`: a bare native button is compared as an action.
- Type: plain text (Title, Body, Label) is Google Sans Flex. One bundled brand face per proposal may take Display and Headline voices only, and Google Sans Code serves numbers and data.
- Corners come from the corner scale, spacing from the 8dp scale. Compact below 840px, expanded (nav rail instead of the floating toolbar) from 840px up.
- Truncate on purpose (ellipsis or a line clamp, like Compose's `Text(maxLines, overflow = Ellipsis)`); button labels never truncate or wrap. Pad a scroller by the height of any opaque pinned bar over it (Compose `contentPadding`).
- Scripts hold in-memory state only (toggles, an add sheet, a selected tab): no `localStorage`, cookies, or network requests; the lint cannot check these.

## Render and check

Do not judge from code alone. `<exploration>` is the folder `create` printed (`design-explorations/<unique-id>`). Render every proposal, or only the ids you name:

```bash
node <skill-dir>/gallery/cli.mjs render <exploration> [id ...] [--open]
```

`render` lints first, then renders headlessly in Chrome (no server, no port) into `<exploration>/renders/`: per proposal `<id>/` holds PNGs for light, dark, wallpaper, foldable, tablet, and desktop views, `light-full.png` (the phone light view at full page height, for your own review below the first screen; not checked or judged), `<state>-light.png` and `<state>-tablet.png` for each declared state (a missing or throwing `proposalState` fails that state with the reason), `checks.json` (the runtime checks of every view, with `pass` and `complete`), and `view.html`, one self-contained file (opened in the default browser by `--open`, or from disk) to switch devices, light and dark, seed colors, palette, and declared states. `contact-sheet.png` has one row per proposal across the four devices, default state only. Dimensions are in `gallery/runtime-src/README.md`, Render output.

1. Read `checks.json` for each proposal and fix every failure. Treat `unresolved` entries as unchecked, not passed, and `complete: false` as not verified: report it.
2. Look at every screenshot yourself, state renders included, not just the phone (`light-full.png` shows the phone below the first screen). Judge hierarchy, character, and color balance with the `android-design` screen check, and confirm each larger view reveals what your section 0 adaptation answer promised: a second pane, more of a list, more detail. Google: "Additional space doesn't just mean making the same thing bigger." The same single view only rearranged fails.
3. Rerun `render` after each edit.

Done when every proposal's `checks.json` has `pass` and `complete` true and every view has been looked at. If the harness cannot view images, report visual quality as unverified and do not claim the screen check passed.

## Show the user

Show `renders/contact-sheet.png` in chat (attach the image, or give its absolute path). On the user's own machine, rerun the final render of the round with `--open` (never while still iterating: `render` rewrites `renders/<id>/`); when running remotely, give the absolute path of each `view.html` instead. When a direction's idea lives in an overlay, show its state renders too. Say what each direction is in one line, name the tactics the hero layers, and report any unverified check. Then stop and wait for the user.

## Iterate in chat

The user replies in chat ("I like a and b, make b warmer"). Apply clear instructions and ask a focused question only when a meaningful decision remains. Produce one refinement by default, edit `project/proposals/<id>/`, and rerun `render` for the ids you changed. To keep an earlier version for comparison, copy the folder (`b` to `b2`) before editing. A combination of several ideas becomes one consolidated proposal.

## Approve, hand off, or discard

Approval is the user saying so in chat. This skill's job ends there: do not implement. Report the approved proposal, the path of its folder, and that `references/compose-mapping.md` is the porting table. Approval does not authorize implementation or library version changes.

A later explicit implementation request follows the requesting project's planning, branch, verification, and review rules. Port the approved proposal with the mapping table, keeping the app's library versions, then render the Compose result and compare it with the approved proposal's screenshots at the same viewports. Report unmapped or unverified elements instead of inventing a Compose equivalent. If the design cannot be preserved with the app's dependencies, ask rather than silently change the design or versions.

Keep proposals and renders until the user explicitly requests deletion, then run from the requesting repository:

```bash
node <skill-dir>/gallery/cli.mjs cleanup <exploration> --project <requesting-repo> --yes
```

Cleanup removes only a directly contained, identity-marked exploration and refuses symlinks or mismatched markers. It never deletes the installed starter or the requesting app.
