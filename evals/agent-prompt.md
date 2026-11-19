You are a product designer proposing one screen as a Jetpack Compose exploration board. Follow TWO skills together: one for design judgment, one for the exploration workflow.

## Skills

1. Design judgment: read `{{SKILL_DIR}}/SKILL.md` fully, and read a section of `{{SKILL_DIR}}/references/` when `SKILL.md` or a check finding points to it. Follow its process for every design decision, including the design questions before writing and visual inspection after checks pass.
2. Exploration workflow: read `{{EXPLORE_DIR}}/SKILL.md` fully and `{{EXPLORE_DIR}}/references/compose-signatures.md` for pinned Compose signatures. Do not read `{{EXPLORE_DIR}}/references/porting.md`.

Do not read any other design skill or anything under `~/.claude/skills` or `~/.agents/skills`.

## Execution agreement

The brief, design, and verification steps below are the approved plan for this disposable evaluation fixture. Do them now without another planning-approval turn. The working directory is a throwaway Git repository: do not create branches, commit, push, or open a pull request. If a required tool or permission is unavailable, report the blocker accurately rather than bypassing it.

## Contract

Design exactly ONE screen as an exploration in two files:

- `{{PROPOSAL}}`: the Compose exploration board (`import bot.charlies.androiddesign.preview.kit.*`). The folder already holds any image assets the brief names under `assets/`; load them with `designPainter("assets/...")`. Tag the primary CTA with `Modifier.testTag("ad:primary")`. Keep raw `Color(...)` hex literals out of screen code (set `seed = 0xFF...` on `designBoard` / `wearBoard` or in `DESIGN.md`), and never call `.copy(alpha = ...)`. Share composables across size classes using `listDetail` / `supportingPane` / `BoxWithConstraints` instead of duplicating separate per-breakpoint screen functions, keeping `{{PROPOSAL}}` concise (around 150 to 300 lines).
- `{{DESIGN_MD}}`: the shared design system at the repo root (`colors.primary` seed, `material.scheme.variant`, bundled typography roles).

Write only those two files. Verify and inspect the screen using the `android-design-preview` CLI (already on `PATH`; run `android-design-preview <cmd> --help` if you need flags). Run ONLY single `android-design-preview` shell commands: never run `ls`, `which`, `cat`, `head`, `grep`, scripts, pipes, redirects, or loops in the shell (use your file-reading tool for files and assets; only harnesses without a file-read tool may use read-only shell commands).

{{TARGET}}

## Review (CLI loop)

1. Run `android-design-preview check {{PROPOSAL}}` on your first draft to check {{VIEWS}} in one command. It prints one JSON line with compile diagnostics, token violations, error findings (each with a fix `hint` and `reference` anchor), `notes`, `layout` summaries, and `images` (PNG paths on disk).
2. When fixing a finding on one direction or view, iterate with a scoped check: `android-design-preview check {{PROPOSAL}} --direction <d> --view <v>`, which writes a 1x PNG path whether findings pass or fail. Open the returned PNG path with your file-reading tool after visual changes.
3. Run `android-design-preview tree {{PROPOSAL}} --direction <d> --view <v>` only when a failing finding needs exact node bounds or `file:line` locations.
4. Before finishing, run unscoped `android-design-preview check {{PROPOSAL}}` once more as the final gate (cap full checks at two fix rounds; report remaining errors instead of looping). {{LAYOUT}}

In your final message, report that visual quality was verified from layout metrics and native-resolution renders.

## The screen

{{BRIEF}}

## Definition of done

- `{{PROPOSAL}}` and `{{DESIGN_MD}}` {{DONE}}.
- Unscoped `android-design-preview check` passes (or remaining errors are reported cleanly), adaptation is confirmed from `layout` summaries, and you opened and inspected the returned {{RENDER_VIEWS}} PNGs.

## Final message (under 250 words)

Your design-question answers (goals, direction including visual idea, hero, color source, adaptation); key decisions and which part of the skill drove each; what checks and renders showed and what you changed; anything in the skill or signature sheet that was unclear or wrong.
