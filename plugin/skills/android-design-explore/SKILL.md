---
name: android-design-explore
description: >
  Explore Android app ideas as live Compose proposals in the previewer app before implementation, without
  changing app code. Use to brainstorm phone, tablet, desktop, or Wear OS app screens, mock up redesign
  alternatives, or compare and iterate on visual directions in chat. Not for Wear tiles, watch faces, or widgets.
  Builds on the android-design skill, which owns every design decision.
---

# Visual design exploration

Follow the `android-design` skill for every design decision: its design questions, components, theming, layout, and visual criteria. This skill owns the exploration workflow. Each run writes a live Jetpack Compose exploration board, verifies it headlessly through `android_design_preview`, shows renders and the interactive browser board link, then waits for feedback. Phone, foldable, tablet, desktop, and Wear OS app screens are in scope; Wear tiles, watch faces, and widgets are not. Where Wear differs, the **Wear OS** section below says how.

## Requirements

Requires the `android_design_preview` MCP server (`check_design` and `get_tree`), which runs on macOS, Linux, and Windows with Node.js 18+ and Java 17+. Call `check_design` when your first draft is ready. If its tools are unavailable or report missing Java/Node prerequisites, stop, give the user [references/setup.md](references/setup.md), and wait.

## Brief

When there is an app, read its relevant screens, theme, assets, and any existing `DESIGN.md` and `design/*.explore.kt` files first. Answer `android-design`'s design questions in your reply before writing code:

- Pick one seed: the brand color, or a content color from the subject (a dish, an album, a trip), never the default `#6750A4`.
- When the screen is a hero moment, layer three or more tactics and name them.
- Keep app code and dependencies unchanged.
- Use representative, non-sensitive data and assets. Use image generation only for assets inside `design/assets/`, never as a substitute for a Compose board.

When the user asks for alternatives, declare multiple `direction("...")` blocks inside the same `<slug>.explore.kt` file so they compare side by side on the browser board, each a genuinely distinct composition with clear visual differentiation rather than a recoloring.

## Files

Work in the app's repository root, or the folder the user names. Never write into this skill's own repository.

- `DESIGN.md` at the repo root holds the shared design system (`colors.primary` seed, `material.scheme.variant`, bundled typography roles). Create it when missing; update it only when every exploration should share a change.
- `design/<slug>.explore.kt` holds the Compose exploration board (`design/<slug>-wear.explore.kt` for Wear OS).

Report the file's absolute path and the `boardUrl` returned by `check_design`.

## Write

1. Read [references/compose-signatures.md](references/compose-signatures.md) for the exact pinned Material 3 Expressive, Wear M3, and Exploration Kit signatures. Do **not** read `references/porting.md` until the user approves a direction and requests implementation.
2. Write `DESIGN.md` (if needed) and `design/<slug>.explore.kt` using `designBoard(...)` or `wearBoard(...)`.

Follow these exploration rules:

- Mark the screen's single primary CTA with `Modifier.testTag("ad:primary")` (`AD_PRIMARY_TAG`).
- Never place raw `Color(...)` hex literals in screen code (pass `seed = 0xFF...` on `designBoard` / `wearBoard` instead) and never call `.copy(alpha = ...)`. Use `MaterialTheme.colorScheme` and `MaterialTheme.typography` roles in intended pairs (`onSurfaceVariant` for subdued text).
- Draw bundled Material Symbols with `SymbolIcon(...)` and local images beneath the exploration folder with `designPainter("assets/...")`.
- Adapt at 600 and 840 dp: switch `ShortNavigationBar` to `WideNavigationRail` (or use `listDetail` / `supportingPane` for dual-pane layouts) so wider views reveal more structure rather than stretching phone content.
- Keep labels concise so button, chip, and navigation items fit on one line without truncation.

## Check

`check_design` compiles in memory inside a sandboxed worker, enforces token rules, checks every direction, screen, overlay, and view (`light`, `dark`, `foldable`, `tablet`, `desktop`, or `watch` and `watch-large` on Wear), and when error findings reach zero, returns `[TextContent(checkJson), ImageContent(lightPng), ImageContent(desktopPng)]` (`watch` and `watch-large` on Wear) plus `boardUrl`.

1. Call `check_design` with the absolute `path` of your `.explore.kt` file (leave `views` unset so every view checks in one call).
2. Fix every compile diagnostic, token rule violation, or error finding (`overflow`, `corner-clipped`, `touch-target`, contrast, typography, `round-edge-clipped`). Read `notes` (info findings) and `colors` (judge strong fills from rendered hex roles, keeping one strong fill on the hero or coherent action group). A view equal to an earlier one on the same screen collapses to `{"same": "<view>"}`.
3. Confirm adaptation from the `layout` summaries: each larger view (`tablet`, `desktop`) must show what your adaptation answer promised.
4. Call `get_tree` only when a failing finding needs exact node bounds or `<file>:<line>` locations.
5. Once checks pass and `check_design` returns images (`light` + `desktop`, or `watch` + `watch-large`), visually inspect the PNGs for hierarchy, icon fit, asset legibility, and spacing balance. Fix any visual flaw, re-run `check_design` once to confirm, and never exceed two fix rounds without reporting unresolved findings.

## Show

State in one line what the exploration covers, name the tactics layered for hero moments, note any remaining unresolved findings, share the absolute `.explore.kt` path and clickable `boardUrl`, and report `"visual quality checked from layout metrics and native-resolution renders"`. Stop and wait for the user's reply.

## Revise

Edit the same `design/<slug>.explore.kt` file (the browser board watches saves and pushes SSE updates automatically). Re-run `check_design` on changed directions/screens, keeping a single clean file with no stale copies.

## Approve and port

Approval is the user saying so in chat. An explore-only request ends here: do not touch app source code. A build request (sent here by `android-design`'s Route step) continues to wiring: now read [references/porting.md](references/porting.md) and follow the host project's planning, branch, verification, and review workflow.

## Wear OS

Wear explorations follow the same loop using `wearBoard(...)` and `design/<slug>-wear.explore.kt`:

- **Views:** `watch` (204 dp round, dark) and `watch-large` (227 dp round, dark).
- **Scrolling and scaffold:** wrap in `AppScaffold` and `ScreenScaffold`; scrolling screens use `TransformingLazyColumn` and optional `EdgeButton`. The previewer scrolls from top to end and merges bounds automatically.
- **Findings:** fix `round-edge-clipped` with percentage margins (`PaddingDefaults`) or tighter content.

## Delete

Remove `design/<slug>.explore.kt` (or `-wear.explore.kt`) when asked. Keep `DESIGN.md` unless the user asks to remove it too.
