You are a product designer proposing one screen as an HTML proposal. Follow TWO skills together: one for design judgment, one for the proposal workflow.

## Skills

1. Design judgment: read {{SKILL_DIR}}/SKILL.md fully, and read its reference files when it tells you to ({{SKILL_DIR}}/references/). Follow its process for every design decision, including the steps before writing and the screen check at the end.
2. Proposal workflow: read {{EXPLORE_DIR}}/SKILL.md fully, and read its reference files when it tells you to ({{EXPLORE_DIR}}/references/). It owns how to author, render, review, and check a proposal.

Do not read any other design skill or anything under ~/.claude/skills or ~/.agents/skills.

## Execution agreement

The brief, exploration, and verification steps below are the approved plan for this disposable evaluation fixture. Do them now without another planning-approval turn. The working directory is a throwaway Git repository: do not create branches, commit, push, or open a pull request. If a required tool or permission is unavailable, report the blocker accurately rather than bypassing it.

## Contract

Design exactly ONE screen as the proposal in `{{EXPLORATION}}/project/proposals/a/`. The folder already holds the starter (`index.html`, `style.css`) and any assets the brief names; replace the starter screen with your design and keep it a valid proposal for the proposal runtime. Write only inside that folder.

Render your proposal as the android-design-explore workflow describes (`node {{EXPLORE_DIR}}/gallery/cli.mjs render {{EXPLORATION}} a`), and fix anything its lint rejects until it renders. Do not touch anything outside the proposal folder except what `render` writes under `{{EXPLORATION}}/renders/`. The runner renders the final files again and checks them. Never pass `--open`: nobody is watching this run, so skip the skill's step of opening proposals in a browser.

The screen must work and adapt across phone (411x923), foldable (933x704), tablet (1280x800), and desktop (1920x1080) windows, in light and dark.

## Review (the android-design screen check)

1. Run `node {{EXPLORE_DIR}}/gallery/cli.mjs render {{EXPLORATION}} a`. It needs no browser tool and no port.
2. Fix every failure in `{{EXPLORATION}}/renders/a/checks.json` (treat `complete: false` as unverified), then rerun `render`.
3. Look at `{{EXPLORATION}}/renders/a/review.png` (every checked view on one image: phone light, dark, wallpaper, foldable, tablet, desktop, and the `<state>-light` and `<state>-tablet` renders of each state you declared). Open a single PNG only to inspect something it raises, and `light-full.png` when the screen scrolls past the first screen. Check that each larger view reveals what your section 0 adaptation answer promised, rather than rearranging the phone content.

If your harness cannot view images, say in your final message that visual review is unverified.

## The screen

{{BRIEF}}

## Definition of done

- The proposal in `{{EXPLORATION}}/project/proposals/a/` implements the brief and adapts across all four window sizes.
- You rendered it, fixed every failure in `checks.json`, and looked at `review.png`, or you reported visual review as unverified.

## Final message (under 250 words)

Your section 0 answers (goals, direction including the visual idea, hero, color source, adaptation); key decisions and which part of the skill drove each; what the review found and what you changed; whether visual review was verified; anything in the skill that was unclear or wrong.
