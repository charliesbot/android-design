You are a product designer proposing one screen as an HTML proposal. Follow ONE design skill for every design decision.

## Design skill

Read {{SKILL_DIR}}/SKILL.md fully, and read its reference files when it tells you to ({{SKILL_DIR}}/references/, especially exploration.md for how to author, review, and check a proposal). Follow its process, including the steps before writing and the screen check at the end.

Do not read any other design skill or anything under ~/.claude/skills or ~/.agents/skills.

## Execution agreement

The brief, exploration, and verification steps below are the approved plan for this disposable evaluation fixture. Do them now without another planning-approval turn. The working directory is a throwaway Git repository: do not create branches, commit, push, or open a pull request. If a required tool or permission is unavailable, report the blocker accurately rather than bypassing it.

## Contract

Design exactly ONE screen as the proposal in `{{EXPLORATION}}/project/proposals/a/`. The folder already holds the starter (`index.html`, `style.css`) and any assets the brief names; replace the starter screen with your design and keep it a valid proposal for the gallery runtime. Write only inside that folder.

Publish your proposal as the skill's exploration workflow describes (`node {{SKILL_DIR}}/gallery/cli.mjs publish`), and fix anything its lint rejects until it publishes. Do not approve or finish the exploration, and do not touch anything outside the proposal folder except what publishing writes. The runner republishes the final files and renders them.

The screen must work and adapt across phone (411x923), foldable (933x704), tablet (1280x800), and desktop (1920x1080) windows, in light and dark.

## Review (the skill's screen check)

If your harness has a browser:

1. Serve the exploration on a free port: `node {{SKILL_DIR}}/gallery/cli.mjs serve {{EXPLORATION}} --port <free port>`.
2. Open your own new browser tab for `http://127.0.0.1:<port>/runtime/review/?src=/draft/a/` (other agents may share the browser, so never reuse or navigate an existing tab) and keep it in front while you run the checks, because a backgrounded tab can stall `reviewChecks()`. Run `window.reviewChecks()`, fix every failure, and look at the screenshots of each device.
3. Stop the server when you are done.

If your harness has no browser, say in your final message that visual review is unverified.

## The screen

{{BRIEF}}

## Definition of done

- The proposal in `{{EXPLORATION}}/project/proposals/a/` implements the brief and adapts across all four window sizes.
- You reviewed it through the gallery review page and looked at the screenshots, or you reported visual review as unverified.

## Final message (under 250 words)

Your section 0 answers (goals, direction including the visual idea, hero, color source); key decisions and which part of the skill drove each; what the review found and what you changed; whether visual review was verified; anything in the skill that was unclear or wrong.
