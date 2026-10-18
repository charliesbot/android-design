---
name: android-design-explore
description: >
  Explore Android app ideas as HTML proposals before implementation, without editing the app. Use for
  brainstorming screens, redesign alternatives, visual directions, or proposals to compare and iterate on in
  chat: it renders phone, foldable, tablet, and desktop views and a self-contained view.html.
  Builds on the android-design skill, which owns every design decision.
---

# Visual design exploration

Follow the `android-design` skill for every design decision: its section 0 answers, components, theming, layout, and screen check. This skill owns the workflow. Each run is one shot: brief in, one proposal written, rendered, shown, then stop. Phone, foldable, tablet, and desktop are in scope; Wear is not.

Requirements: Node.js 22+ and Google Chrome (set `CHROME_PATH` when Chrome is not at its usual location). If either is missing, report it rather than installing it. Use image generation only for assets, not as a substitute for a proposal.

## Brief

When there is an app, read its relevant screens, theme, and assets first. Answer section 0 of `android-design` in the reply.

- Pick one seed: the brand color, or a content color from the subject (a dish, an album, a trip), never the runtime's default.
- When the screen is a hero moment (question 3 of section 0), layer three or more tactics (section 3) and name them.
- Keep app code and dependencies unchanged.
- Use representative, non-sensitive data and assets.

Make several proposals only when the user asks. Each is its own folder, a distinct composition (not a recoloring) with its own seed, and at least one is a hero.

## Start the folder

Copy `<skill-dir>/assets/proposal-starter/` (including its `.gitignore`) to `<folder>/design-explorations/<slug>/`. `<folder>` is the app's folder or any folder; Git is not needed. The `.gitignore` of `*` keeps the proposal out of commits. Never write into this skill's own repository. Tell the user the absolute path.

## Write

Read `gallery/runtime-src/README.md` first: it is the syntax the lint enforces. Edit `index.html`, `style.css`, and optional scripts in the folder.

- Use only HTML that has a Compose counterpart. Any registered element may be used; the port reports what `references/compose-mapping.md` has not verified or mapped. Build anything else from plain layout with role tokens when `Box`, `Row`, `Column`, `Text`, or `Surface` can express it; otherwise leave it out.
- Overlays come only from `m3e-bottom-sheet modal handle`, `m3e-dialog`, or `m3e-menu`, opened from a control by the page's own script. A render fails if a modal is open on load. A modal bottom sheet opens at about half the window height and scrolls inside, so put its key action near the top. Leave out a side sheet.
- Mark the primary action `data-primary`. It leads its peers as `android-design` section 2 describes. Build a tappable tile as `m3e-card actionable`: a bare native button is compared as an action.
- Plain text is Google Sans Flex. One bundled brand face may take Display and Headline only; Google Sans Code serves numbers and data.
- Corners come from the corner scale, spacing from the 8dp scale. Compact below 840px; nav rail instead of the floating toolbar from 840px.
- Truncate on purpose (ellipsis or a line clamp); button labels never truncate or wrap. Pad a scroller by the height of any opaque pinned bar over it.
- Scripts hold in-memory state only: no `localStorage`, cookies, or network requests.

## Render and check

```bash
node <skill-dir>/gallery/cli.mjs render <proposal-folder> [--open]
```

It lints first: errors print as `file:line`, exit 2, and nothing is drawn. Fix them and rerun. Then it renders headlessly into `<proposal-folder>/renders/` (rewritten each run): `light`, `dark`, `wallpaper`, `foldable`, `tablet`, and `desktop` PNGs, `light-full.png` (phone light at full document height), `review.png` (every checked view on one image), `checks.json` (`{url, renders, pass, complete}`), and `view.html` (self-contained viewer).

1. Fix every `checks.json` failure once, then rerun. Report anything still failing, or `complete: false` (unchecked, not passed), instead of looping.
2. Review `review.png`, not each PNG; add `light-full.png` when the screen scrolls. Judge hierarchy, character, and color balance with the `android-design` screen check. Confirm each larger view reveals what your section 0 adaptation answer promised. Google: "Additional space doesn't just mean making the same thing bigger." The same view only rearranged fails.

If the harness cannot view images, report visual quality as unverified.

## Show

On the user's machine, rerun the final render with `--open`; otherwise give the absolute path of `renders/view.html`. Show `renders/review.png` in chat. Say in one line what the proposal is, name the tactics a hero layers, and report any unresolved check. Then stop and wait.

## Revise

Rerun on the same folder with the current HTML plus the user's feedback. The revision overwrites the proposal; keep no copies.

## Approve and port

Approval is the user saying so in chat. Report the approved proposal, its folder path, and that `references/compose-mapping.md` is the porting table. An explore-only request ends there: do not implement. A build request (the `android-design` skill sends new apps, screens, and redesigns here first) continues to the port. Approval never authorizes library version changes.

The port follows the requesting project's planning, branch, verification, and review rules. Read `references/compose-mapping.md`, port the approved proposal with it, keeping the app's library versions, then render the Compose result and compare it with the proposal's screenshots at the same viewports. Report unmapped or unverified elements instead of inventing a Compose equivalent. If the design cannot be preserved with the app's dependencies, ask.

## Delete

When the user asks, remove `<folder>/design-explorations/<slug>/`.
