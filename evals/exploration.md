# Exploration workflow checks

The design evals check one HTML proposal in a prepared fixture exploration. They do not prove the requesting app's isolation or agent routing. Check those separately.

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

`npm test` (`node --test tests/gallery/*.test.mjs`) covers proposal lint, the render command, `view.html`, and workspace path safety. The render tests that drive Chrome need Google Chrome.

## Exploration smoke check

Run in an expendable Git repository. Needs Node.js 22+ and Google Chrome; no Gradle or Android SDK, and no browser tool in the harness.

1. Create an exploration with `node skills/android-design-explore/gallery/cli.mjs create --project <fixture-repo>`. Record the production app and installed starter hashes.
2. Author two to three distinct proposals in `project/proposals/`, each with its own seed color; at least one layers three or more tactics and names them.
3. Run `cli.mjs render <exploration>`. Every proposal gets six PNGs, a `checks.json`, and a `view.html`, and `renders/contact-sheet.png` has one row per proposal across phone, foldable, tablet, and desktop. A proposal that declares `p-states` also gets `<state>-light.png` and `<state>-tablet.png` per state. Fix every failing or incomplete check (`complete: false` is unverified) and look at every PNG. With no way to view images, the agent must report visual quality unverified.
4. Open a `view.html` from `file://`. Double-click to switch devices with the Phone, Foldable, Tablet, and Desktop buttons, light and dark, seed color, and palette variant; the proposal updates in place and nothing is saved after reload. For a proposal with states, the State buttons (Default and each declared state) open each overlay.
5. Lint failure: in a copy, add a raw hex color, an inline `style=""`, or a type declaration outside a `.voice-*` class. `render` must exit 2 with file and line and write no new renders.
6. Show the user the contact sheet in chat (attached image, or its path) plus each `view.html` path, then iterate on a chat reply ("keep a, make b warmer"). To keep a version, copy the folder (`b` to `b2`) before editing, and render only the changed ids.
7. Approval is the user saying so in chat. The agent reports the approved proposal, the path of its folder, and `references/compose-mapping.md` as the porting table. It does not implement. Production app and installed starter hashes must still match.
8. No port is left open after rendering (no listening process remains).
9. Exercise cleanup only on an expendable, explicitly selected fixture exploration. It must refuse the repository root, unmarked folders, symlinks, and copied markers, and delete proposals and renders only on explicit confirmation.

The workflow never invokes an agent for the user or implements a selected proposal.

## Agent routing checks

- "Show me three home-screen ideas." Expect three HTML proposals in the requesting repository's ignored exploration folder, rendered with checks fixed, and the contact sheet plus `view.html` paths shown in chat. Ask for a repository if none was selected, not a new app scaffold in the current directory.
- "Explore a redesign of Today." Expect read-only inspection of app code and dependencies, with writes only to exploration artifacts and the local Git exclusion.
- "I like a and b, make b warmer." Expect one refinement by default, edited in the same exploration and rendered again, with an earlier version kept only if the user wants to compare.
- Approving a design in chat must end exploration, not start implementation. "Implement the approved design" explicitly transitions to the requesting project's workflow, ports the proposal with `references/compose-mapping.md`, preserves its library versions, and compares the rendered Compose result with the approved proposal.
- "Explore Wear." Expect `create --starter android` and its experimental Wear shell and preview skill, not the four-device renders or a claim that parked Wear evals pass.

Run the design evals (`evals/README.md`) after changing skill instructions. Model-based checks send prompts, source context, and screenshots through the configured agent service; obtain the required execution permission before running them.
