# Exploration workflow checks

The design evals check one HTML proposal folder in a prepared fixture. They do not prove the requesting app's isolation or agent routing. Check those separately.

## Deterministic checks

From the repository root:

```bash
python3 -m unittest discover scripts
python3 -m unittest discover evals
npm test
python3 scripts/check_coverage.py
node --test tests/evals/*.test.mjs
```

The design evals need Node.js 22+ and Google Chrome or `CHROME_PATH`; no Android SDK.

`npm test` (`node --test tests/gallery/*.test.mjs`) covers proposal lint, the render command, and `view.html`. The render tests that drive Chrome need Google Chrome.

## Exploration smoke check

Run in an expendable folder (a Git repository or not). Needs Node.js 22+ and Google Chrome; no Gradle or Android SDK, and no browser tool in the harness.

1. Copy `skills/android-design-explore/assets/proposal-starter/` to `<fixture-folder>/design-explorations/<slug>/`. Record the production app and installed starter hashes.
2. Author the proposal in that folder with its own seed color. Then ask for two more ideas: expect two distinct proposals in new sibling folders, each with its own seed, and at least one of the three layering three or more tactics and naming them.
3. Run `cli.mjs render <proposal-folder>`. The folder gets `renders/` with five PNGs (`light`, `dark`, `foldable`, `tablet`, `desktop`), `light-full.png`, `review.png`, a `checks.json`, and a `view.html`. Fix every failing or incomplete check (`complete: false` is unverified) and look at `review.png`. With no way to view images, the agent must report visual quality unverified.
4. Open `renders/view.html` from `file://`. Double-click to switch devices with the Phone, Foldable, Tablet, and Desktop buttons, light and dark, seed color, and palette variant; the proposal updates in place and nothing is saved after reload.
5. Lint failure: in a copy, add a raw hex color, an inline `style=""`, or a type declaration outside a `.voice-*` class. `render` must exit 2 with file and line and write no new renders.
6. Show the user `review.png` in chat (attached image, or its path) plus the `view.html` path, then stop. On a chat reply ("make it warmer"), run again on the same folder with the current HTML plus the feedback: the revision overwrites the proposal and no version copies are kept.
7. Approval is the user saying so in chat. The agent reports the approved proposal, the path of its folder, and `references/compose-mapping.md` as the porting table. For an explore-only request it does not implement. Production app and installed starter hashes must still match.

The workflow never invokes an agent for the user, and it implements an approved proposal only when the original request was to build.

## Agent routing checks

- "Explore a home screen idea." Expect one HTML proposal, not several. "Show me three home-screen ideas." Expect three HTML proposals, each its own self-ignoring folder under `design-explorations/` in the chosen folder, each rendered with checks fixed once and any remainder reported, and each `review.png` plus `view.html` path shown in chat. Outside a repository it still works; it never scaffolds a new app.
- "Build a music theory app with the android-design skill." Expect one HTML proposal shown in chat and a wait for approval before any Compose, then the port after the user approves.
- "Build a habit tracker app in Compose." (no design ask) Expect no proposal: the agent codes directly, applying `android-design`'s decisions and screen check.
- "Explore a redesign of Today." Expect read-only inspection of app code and dependencies, with writes only inside the proposal folder under `design-explorations/`.
- "Make it warmer." Expect one revision that overwrites the same proposal folder and is rendered again, with no version copies.
- After an explore-only request, approving a design in chat must end exploration, not start implementation. After a design-focused build request, approval continues to the port. Either way, "Implement the approved design" transitions to the requesting project's workflow, ports the proposal with `references/compose-mapping.md`, preserves its library versions, and compares the rendered Compose result with the approved proposal.
- "Explore Wear." Expect the skill to say Wear is outside this workflow, not the device renders or a claim that Wear is validated.

Run the design evals (`evals/README.md`) after changing skill instructions. Model-based checks send prompts, source context, and screenshots through the configured agent service; obtain the required execution permission before running them.
