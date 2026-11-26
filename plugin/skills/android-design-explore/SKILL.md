---
name: android-design-explore
description: >
  Explore Android app ideas as live Compose proposals before implementation, without changing app
  code. Use to brainstorm phone, foldable, tablet, desktop, or Wear OS app screens, mock up
  redesign alternatives, or compare and iterate on visual directions. Not for Wear tiles, watch
  faces, or widgets. Builds on android-design.
---

# Visual design exploration

Follow `android-design` for every design decision: design questions, **emphasis budget**, **role pairs**, **reveal, not stretch**, components, theming, layout, and visual criteria. This skill owns the exploration loop and chat workflow. Phone, foldable, tablet, desktop, and Wear OS app screens are in scope; Wear tiles, watch faces, and widgets are out of scope.

## Tooling

Run `android-design-preview` from the shell (`android-design-preview <command> --help` documents flags, exit codes, and JSON output). When it is not on `PATH`, run `sh <skill-dir>/scripts/android-design-preview`, where `<skill-dir>` is the folder containing this `SKILL.md`.

- Default previews (`start`, `check`, `tree`, `render`, `open`) run device-free in layoutlib without a device, `adb`, or emulator. Pass `open --live [--serial <id>] [--avd <name>]` only for opt-in live motion and touch on a visible device or emulator.
- If the bundled script is missing or `android-design-preview` exits `3`, stop, point the user to [references/setup.md](references/setup.md), and wait. Never edit user sandbox config unasked.

## 1. Brief

When an app exists, read its relevant screens, theme, assets, and any `DESIGN.md` and `design/*.explore.kt` first. Answer `android-design`'s seven design questions in your reply before writing code:

- Pick one seed color from the brand or subject (a dish, an album, a trip), never the default `#6750A4`.
- Name the three or more layered tactics for any hero moment.
- Keep app code and dependencies unchanged.
- Use representative, non-sensitive data and assets. Use image generation only for files inside `design/assets/`, never as a substitute for a Compose board.
- For alternatives, place multiple `direction("...")` blocks inside one `.explore.kt` file so they compare side by side, each a distinct composition rather than a recoloring.

## 2. Write

Work in the app's repository root or the folder the user names; write outside this skill's own repository.

- Keep board tokens on `designBoard(seed = ..., variant = ..., brandFont = ...)` (or `wearBoard(seed = ...)`). Read any existing `DESIGN.md` as a constraint without editing it until the user approves a direction (step 6).
- Author `design/<slug>.explore.kt` (`design/<slug>-wear.explore.kt` for Wear OS using `wearBoard(...)`).
- Read [references/compose-signatures.md](references/compose-signatures.md) for pinned Material 3 Expressive, Wear M3, and Exploration Kit signatures.
- Follow the kit rules: tag the single primary CTA with `Modifier.testTag("ad:primary")`; use `MaterialTheme.colorScheme.*` **role pairs** (no raw `Color(...)` hex literals or `.copy(alpha = ...)` in screen code); use `SymbolIcon(...)` and `designPainter("assets/...")`; author one adaptive screen (`listDetail`, `supportingPane`, or `BoxWithConstraints`) instead of duplicating per-breakpoint screens (aim for ~150 to 300 lines per direction).

Done when `design/<slug>.explore.kt` compiles cleanly with all requested `direction(...)` blocks and kit rules applied.

## 3. Iterate and check

Every `check` writes PNGs on disk and returns their paths in `images`, plus `boardUrl`, layout summaries, and findings (each with a fix `hint` and `reference` anchor in `android-design/references/`). Read a reference section when a finding points to it instead of pre-reading every reference file.

1. **First draft (full check):** run `android-design-preview check <file>` (unscoped) to check every direction, screen, overlay, and view (`light`, `dark`, `foldable`, `tablet`, `desktop`, or `watch` and `watch-large` on Wear).
2. **Targeted fixes (scoped loop):** edit the file, then run one bare `android-design-preview check <file> --direction <d> --view <v>` (add `--screen <s>` when a direction has multiple screens) for the single direction and view you changed. Keep each shell call to a single bare command (never pipe, chain, or loop across views during iteration). Scoped checks always return a 1x PNG path whether findings pass or fail. Call `android-design-preview tree <file> --direction <d> --view <v>` only when a finding needs exact bounds or `file:line`.
3. **Inspect the PNG before claiming a fix:** after any visual change, open the returned 1x PNG path for the changed view with your file-reading tool to verify hierarchy, icon fit, asset legibility, and spacing balance. Renders draw blur (uniform, gradient, and `BlurRadiusSpec.shader` masks) as Android 13+ does, while older devices draw content unblurred, so keep text over blurred content backed by a scrim or surface.
4. **Final gate (full check):** before showing the person, run unscoped `android-design-preview check <file>` once more, verify adaptation in `layout` summaries (`tablet`/`desktop` reveal what your adaptation answer promised), check `colors` for one strong fill, and open the returned `light` and `desktop` (`watch` and `watch-large` on Wear) PNGs.
5. **Fix cap:** cap full checks at two fix rounds (scoped iterations between them are uncapped). Report any unresolved findings instead of looping.

## 4. Show

Run `android-design-preview open <file>` on interactive runs (or share `boardUrl` from `check`). In your reply:

- State in one line what the exploration covers and name the tactics layered for hero moments.
- Report any remaining unresolved findings.
- Share the absolute `.explore.kt` path and clickable `boardUrl`, and state `"visual quality checked from layout metrics and native-resolution renders"`.

**Stop here for this turn:** wait for the user's reply before revising, writing `DESIGN.md`, or editing app code.

## 5. Revise

- Whenever the user refers to `"this"`, `"here"`, or a clicked element, check `selection` first (from the latest `check` response or `android-design-preview selection <file>`). If no selection exists, ask the person to click the part they mean on the browser board rather than guessing from source. If `selection.stale` is true, confirm the match using `callName` and `callChain`.
- Edit the same `design/<slug>.explore.kt` file (the server watches saves and pushes focus-first SSE updates). Iterate with scoped `check`, open the returned PNG after visual changes, and run the final unscoped `check` gate before presenting.

## 6. Approve, port, or delete

- **Record the approved direction:** when the user approves a direction, write its seed, scheme variant, and brand font into `DESIGN.md` at the repo root (create it when missing; update only those shared defaults).
- **Explore-only:** after recording `DESIGN.md`, approval ends the task; leave app source code untouched.
- **Build / port:** once the user approves a direction for implementation, read [references/porting.md](references/porting.md) and follow the host project's planning, branch, verification, and review workflow.
- **Delete:** remove `design/<slug>.explore.kt` (or `-wear.explore.kt`) when asked; keep `DESIGN.md` unless asked to remove it too.
