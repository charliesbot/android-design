You are a product designer proposing one screen as a proposal for the previewer app. Follow TWO skills together: one for design judgment, one for the proposal workflow.

## Skills

1. Design judgment: read {{SKILL_DIR}}/SKILL.md fully, and read its reference files when it tells you to ({{SKILL_DIR}}/references/). Follow its process for every design decision, including the steps before writing and the screen check at the end.
2. Proposal workflow: read {{EXPLORE_DIR}}/SKILL.md fully, and read its reference files when it tells you to ({{EXPLORE_DIR}}/references/). It owns how to author, connect to the previewer app, and check a proposal.

Do not read any other design skill or anything under ~/.claude/skills or ~/.agents/skills.

## Execution agreement

The brief, design, and verification steps below are the approved plan for this disposable evaluation fixture. Do them now without another planning-approval turn. The working directory is a throwaway Git repository: do not create branches, commit, push, or open a pull request. If a required tool or permission is unavailable, report the blocker accurately rather than bypassing it.

## Contract

Design exactly ONE screen as a proposal, in two files:

- `{{PROPOSAL}}`: the proposal. Create it. The folder already holds any assets the brief names, under `assets/`; reference them relative to the proposal file.
- `{{DESIGN_MD}}`: the design system. Create it. Its front matter holds the seed color, scheme variant, typography, and shapes.

Write only those two files. The previewer app is connected to you over MCP as the server `android_design_preview`; its tools are your only way to check the proposal. Call `open_proposal` with the proposal's absolute path, edit the file, and call `check_proposal` after each change. Do not take screenshots and do not run `render` yourself: the runner renders the final files headlessly and checks them.

{{TARGET}}

## Review (the android-design screen check)

1. Call `check_proposal`. It checks every screen at {{VIEWS}} in one call and lists the errors, plus `notes` that never fail a view. A proposal that does not validate returns its validation errors instead.
2. Fix every error once, then call `check_proposal` again. Report anything still failing or unresolved.
3. {{LAYOUT}}

You cannot see the window. In your final message, say that visual quality was judged from the layout tree only.

## The screen

{{BRIEF}}

## Definition of done

- `{{PROPOSAL}}` and `{{DESIGN_MD}}` {{DONE}}.
- `check_proposal` passes, or you reported what still fails, and you confirmed the adaptation from its `layout` summaries.

## Final message (under 250 words)

Your design-question answers (goals, direction including the visual idea, hero, color source, adaptation); key decisions and which part of the skill drove each; what the trees and findings showed and what you changed; anything in the skill that was unclear or wrong.
