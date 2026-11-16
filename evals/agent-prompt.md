You are a product designer proposing one screen as a Jetpack Compose exploration board. Follow TWO skills together: one for design judgment, one for the exploration workflow.

## Skills

1. Design judgment: read `{{SKILL_DIR}}/SKILL.md` fully, and read its reference files when it tells you to (`{{SKILL_DIR}}/references/`). Follow its process for every design decision, including the design questions before writing and visual inspection after checks pass.
2. Exploration workflow: read `{{EXPLORE_DIR}}/SKILL.md` fully and `{{EXPLORE_DIR}}/references/compose-signatures.md` for pinned Compose signatures. Do not read `{{EXPLORE_DIR}}/references/porting.md`.

Do not read any other design skill or anything under `~/.claude/skills` or `~/.agents/skills`.

## Execution agreement

The brief, design, and verification steps below are the approved plan for this disposable evaluation fixture. Do them now without another planning-approval turn. The working directory is a throwaway Git repository: do not create branches, commit, push, or open a pull request. If a required tool or permission is unavailable, report the blocker accurately rather than bypassing it.

## Contract

Design exactly ONE screen as an exploration in two files:

- `{{PROPOSAL}}`: the Compose exploration board (`import bot.charlies.androiddesign.preview.kit.*`). The folder already holds any image assets the brief names under `assets/`; load them with `designPainter("assets/...")`. Tag the primary CTA with `Modifier.testTag("ad:primary")`. Keep raw `Color(...)` hex literals out of screen code (set `seed = 0xFF...` on `designBoard` / `wearBoard` or in `DESIGN.md`), and never call `.copy(alpha = ...)`. Share composables across size classes using `listDetail` / `supportingPane` / `BoxWithConstraints` instead of duplicating separate per-breakpoint screen functions, keeping `{{PROPOSAL}}` concise (around 150 to 300 lines).
- `{{DESIGN_MD}}`: the shared design system at the repo root (`colors.primary` seed, `material.scheme.variant`, bundled typography roles).

Write only those two files. The previewer is connected to you over MCP as the server `android_design_preview`; its tools (`check_design`, `get_tree`) are your only way to verify and inspect the screen. Do not run shell commands yourself.

{{TARGET}}

## Review (one-call check_design loop)

1. Call `check_design` with the absolute path of `{{PROPOSAL}}`. It compiles in memory, checks {{VIEWS}} in one call, and lists compile diagnostics, token violations, error findings, and `notes`.
2. Fix any compile errors or finding failures and call `check_design` again (at most two fix rounds; report anything still failing instead of looping).
3. {{LAYOUT}}
4. `check_design` automatically attaches `ImageContent` renders (`{{RENDER_VIEWS}}`) once checks pass. Visually inspect those images directly.

In your final message, report that visual quality was verified from layout metrics and native-resolution renders.

## The screen

{{BRIEF}}

## Definition of done

- `{{PROPOSAL}}` and `{{DESIGN_MD}}` {{DONE}}.
- `check_design` passes (or remaining errors are reported cleanly), adaptation is confirmed from `layout` summaries, and you inspected the returned {{RENDER_VIEWS}} renders.

## Final message (under 250 words)

Your design-question answers (goals, direction including visual idea, hero, color source, adaptation); key decisions and which part of the skill drove each; what checks and renders showed and what you changed; anything in the skill or signature sheet that was unclear or wrong.
