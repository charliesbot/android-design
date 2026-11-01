---
name: android-design-explore
description: >
  Explore Android app ideas as live Compose proposals in the previewer app before implementation, without
  changing app code. Use to brainstorm screens, mock up redesign alternatives, or compare and iterate on
  visual directions in chat. Builds on the android-design skill, which owns every design decision.
---

# Visual design exploration

Follow the `android-design` skill for every design decision: its section 0 answers, components, theming, layout, and the screen check's questions. This skill owns the workflow, and its Check step replaces the screen check's rendering. Each run is one shot: brief in, one proposal written, checked, shown, then stop. Phone, foldable, tablet, and desktop are in scope; Wear is not.

A proposal is a data file that the previewer app, a native macOS window, draws with real Compose Material 3 components and redraws on every save. You check it through the app's layout tree; the user judges taste in the window.

## Requirements

macOS, with the previewer app installed and registered as an MCP server named `android_design_preview`. Call its `status` tool first. When its tools are unavailable, stop and tell the user the two steps, then wait:

1. Build and install the app from a checkout of the android-design repository: `cd app && ./gradlew :app:installApp`, which installs `~/Applications/AndroidDesignPreview.app`.
2. Register it for their harness, then restart the session (`<binary>` is `~/Applications/AndroidDesignPreview.app/Contents/MacOS/AndroidDesignPreview`, written as an absolute path where the harness does not expand `~`):
   - Claude Code: `claude mcp add --scope user android_design_preview -- <binary>`
   - Codex: a `[mcp_servers.android_design_preview]` table in `~/.codex/config.toml` with `command = "<binary>"` and `default_tools_approval_mode = "approve"`
   - Antigravity: `agy mcp add android_design_preview <binary>`

## Brief

When there is an app, read its relevant screens, theme, assets, and any existing `DESIGN.md` and `design/` proposals first. Answer section 0 of `android-design` in the reply.

- Pick one seed: the brand color, or a content color from the subject (a dish, an album, a trip), never the app's default `#6750A4`.
- When the screen is a hero moment (question 3 of section 0), layer three or more tactics (section 3) and name them.
- Keep app code and dependencies unchanged.
- Use representative, non-sensitive data and assets. Use image generation only for assets inside a proposal, never as a substitute for one.

Make several proposals only when the user asks. Each is its own file, a distinct composition (not a recoloring) with its own seed in its `theme`, and at least one is a hero.

## Files

Work in the app's repository root, or the folder the user names. Never write into this skill's own repository.

- `DESIGN.md` at the root holds the shared design system. Create it when missing, with the chosen seed, faces, and named styles. Change it only when the direction changes what every proposal shares; a direction's own look goes in its proposal's `theme`.
- `design/<slug>.proposal.json` holds one proposal.

Both are ordinary tracked files; committing them follows the project's own workflow. Tell the user the proposal's absolute path.

## Write

1. Call `get_catalog` (its tool description says what it returns). It is the only component vocabulary: a component it lacks is out of reach, so build the idea from `Box`, `Row`, `Column`, `Text`, and `Shape`, or leave it out.
2. Read `references/proposal-format.md`, then write `DESIGN.md` and the proposal, compactly as its "Writing it compactly" section says.

The proposal carries these design rules; `proposal-format.md` gives the syntax for each:

- The screen has one primary action, marked as primary, and it leads its peers as `android-design` section 2 describes.
- It adapts at 600 and 840 dp: a navigation bar in compact becomes a rail from medium up, and a list's detail screen opens beside the list at expanded widths.
- Overlays (`BottomSheet`, `Dialog`, `Menu`) open from a control. A modal bottom sheet opens at about half the window height and scrolls inside, so its key action sits near the top.
- Text truncates on purpose, with an ellipsis. Button, chip, and navigation labels fit on one line.

## Check

This is `android-design`'s screen check for a proposal. Answer its questions from the `check_proposal` layout summaries and errors (and `get_tree` when they are not enough), never from a render or pixels. Screenshots stay out of this loop: never take one or ask for one.

1. Call `open_proposal` with the proposal's absolute path. It loads the file, watches it, shows the window, and returns validation errors. Fix them and call `status` until it reports none.
2. Call `check_proposal`. One call checks every screen at every view and returns each view's errors and `layout` summary; its tool description gives the format.
3. Fix every error once, then call `check_proposal` again. Report anything still failing instead of looping.
4. Confirm the adaptation from the `layout` summaries: each larger view reveals what your section 0 adaptation answer promised, such as a second pane, a rail, or more regions. Google: "Additional space doesn't just mean making the same thing bigger." A larger view with the phone's regions only spread wider fails.
5. Call `get_tree` only when a summary or an error is not enough; its tool description says what it returns.

The step is done when `check_proposal` passes or each remaining error is on the list you report, and the adaptation is confirmed.

## Show

Call `show_preview`. Say in one line what the proposal is, name the tactics a hero layers, list any unresolved findings, and report "visual quality judged from the layout tree only". Then stop and wait for the user's reply.

## Revise

Edit the same proposal with the user's feedback; the window redraws on save. Repeat the check on the screens you changed. The revision replaces the old version; keep no copies.

## Approve and port

Approval is the user saying so in chat. Report the approved proposal's path and that `references/compose-mapping.md` is the porting table. An explore-only request ends there: do not implement. A build request (the `android-design` skill sends design-focused builds of new apps, screens, and redesigns here first) continues to the port. Approval never authorizes library version changes.

The port follows the requesting project's planning, branch, verification, and review rules:

1. Call `export_kotlin` for the screens to port. Its Kotlin is the first draft, not finished code.
2. Fit it to the app's conventions and library versions with `references/compose-mapping.md`.
3. Render the result with the `compose-preview` skill and compare it with the proposal's trees at the same views.

Report components the mapping marks unverified, and anything it lacks, instead of inventing a Compose equivalent. If the design cannot be preserved with the app's dependencies, ask.

## Delete

When the user asks, remove `design/<slug>.proposal.json`. Keep `DESIGN.md` unless the user asks to remove it too.
