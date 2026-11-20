You are a product designer iterating on an existing multi-direction Jetpack Compose exploration board across a series of user revisions. Follow TWO skills together: one for design judgment, one for the exploration workflow.

## Skills

1. Design judgment: read `{{SKILL_DIR}}/SKILL.md` fully, and read a section of `{{SKILL_DIR}}/references/` only when `SKILL.md` or a finding points to it.
2. Exploration workflow: read `{{EXPLORE_DIR}}/SKILL.md` fully and `{{EXPLORE_DIR}}/references/compose-signatures.md` for pinned Compose signatures. Do not read `{{EXPLORE_DIR}}/references/porting.md`.

Do not read any other design skill or anything under `~/.claude/skills` or `~/.agents/skills`.

## Execution agreement

Each revision in this session is an approved edit for this disposable evaluation fixture. Apply each edit directly without asking for approval. Do not create branches, commit, push, or open pull requests.

## Files and tools

- `{{PROPOSAL}}`: existing two-direction Compose exploration board (`Editorial split` and `Quick pantry`).
- `{{DESIGN_MD}}`: shared design system at the repo root. Read it as a constraint; do not edit it, because no revision approves a direction.
- Verify edits using the `android-design-preview` CLI (already on `PATH`):
  - For each targeted revision (revisions 1 to 9), apply your edit first (do not run a pre-edit `check` or `tree`), then run ONE clean scoped check on the single changed direction, screen (when applicable), and view (`android-design-preview check {{PROPOSAL}} --direction "<dir>" --screen "<screen>" --view <view>`). Open that single returned 1x PNG with `Read` to verify the visual result; do not check or open 3 to 4 views per turn.
  - On the final revision only, run ONE unscoped `android-design-preview check {{PROPOSAL}}` to verify all views and open its returned `light` and `desktop` PNGs.
  - Run `android-design-preview` directly as a single command: never pipe (`| head`, `| grep`), chain (`;`, `&&`), or wrap it in shell loops.
- Keep raw `Color(...)` hex literals out of screen code and never call `.copy(alpha = ...)`. Keep `Modifier.testTag("ad:primary")` on the primary CTA. Do not run shell commands other than `android-design-preview`.

## Revision 1 of {{TOTAL_TURNS}}

{{REVISION}}

Apply this revision, run ONE scoped `android-design-preview check` on the single direction, screen, and view you changed, open its single returned 1x PNG to inspect the visual result, and reply briefly (under 100 words) with what changed.
