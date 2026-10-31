# Exploration workflow checks

The design evals check one proposal (`design/<slug>.proposal.json` plus `DESIGN.md`) in a prepared fixture. They do not prove the requesting app's isolation or agent routing. Check those separately.

## Deterministic checks

From the repository root:

```bash
python3 -m unittest discover scripts
python3 -m unittest discover evals
python3 scripts/check_coverage.py
(cd app && ./gradlew test spotlessCheck)
```

The design evals need macOS and the previewer app installed (`./gradlew :app:installApp` from `app/`, or `ANDROID_DESIGN_PREVIEW` set to its executable). No Node.js, Chrome, or Android SDK.

The Gradle tests cover validation, finding rules, the exporter, and the MCP server. The Python tests cover the runner, `report.json` checks, and the agent launchers without model calls.

## Exploration smoke check

Run in an expendable folder (a Git repository or not). Needs the previewer app and a harness that can register an MCP server; no browser tool and no way to view images are needed.

1. In the harness, register the app as the stdio server `android_design_preview` with the app's executable as its command. The agent should check that the server is connected before it starts, and tell the user how to register it if not.
2. Have the agent author `<fixture-folder>/design/<slug>.proposal.json` and `DESIGN.md` with its own seed color, then call `open_proposal`. Then ask for two more ideas: expect two distinct proposals as new sibling files, each with its own seed, and at least one of the three layering three or more tactics and naming them.
3. Have the agent call `get_tree` for each proposal and every view (`light`, `dark`, `foldable`, `tablet`, `desktop`). Fix every `error` finding (`text-truncated` is informational). With no way to view the window, the agent must report visual quality unverified.
4. Look at the window. Switch devices with the Phone, Foldable, Tablet, and Desktop buttons, and pick a brightness (Light or Dark), a Color row (the proposal's seed or one of six presets), or a Style row in the side panel; the proposal and the viewer recolor in place and nothing is written to the proposal file.
5. Invalid proposal: in a copy, use an unknown component, a raw hex color, or a `navigate` to a missing screen. `open_proposal` must return the validation error with its path, the window must show the errors in place of the frame, and `<app> render` must exit 2 and list the same errors in `report.json`.
6. Show the user the window (or `show_preview`) and the proposal path, then stop. On a chat reply ("make it warmer"), edit the same proposal file in place: the window redraws and no version copies are kept.
7. Approval is the user saying so in chat. The agent reports the approved proposal, its path, and `export_kotlin` (or `<app> export`) as the way to port it. For an explore-only request it does not implement. Production app files must be unchanged.
8. Hidden window: close the window and call `get_tree` again. It must answer from the headless renderer, and `show_preview` must bring the window back.

The workflow never invokes an agent for the user, and it implements an approved proposal only when the original request was to build.

## Agent routing checks

- "Explore a home screen idea." Expect one proposal, not several. "Show me three home-screen ideas." Expect three proposals, each its own `design/<slug>.proposal.json` in the chosen folder, each with its `get_tree` findings fixed once and any remainder reported, and each opened in the window for the owner. Outside a repository it still works; it never scaffolds a new app.
- "Build a music theory app with the android-design skill." Expect one proposal shown in the window and a wait for approval before any Compose, then the export after the user approves.
- "Build a habit tracker app in Compose." (no design ask) Expect no proposal: the agent codes directly, applying `android-design`'s decisions and screen check.
- "Explore a redesign of Today." Expect read-only inspection of app code and dependencies, with writes only to the proposal and `DESIGN.md`.
- "Make it warmer." Expect one revision that edits the same proposal file, with no version copies.
- After an explore-only request, approving a design in chat must end exploration, not start implementation. After a design-focused build request, approval continues to the export. Either way, "Implement the approved design" transitions to the requesting project's workflow, exports the proposal with `export_kotlin`, preserves its library versions, and compares the rendered Compose result with the approved proposal.
- "Explore Wear." Expect the skill to say Wear is outside this workflow, not a device view or a claim that Wear is validated.
- App not connected. Expect the agent to say the `android_design_preview` server is missing and how to register it, not to fall back to writing HTML.

Run the design evals (`evals/README.md`) after changing skill instructions. Model-based checks send prompts, source context, and screenshots through the configured agent service; obtain the required execution permission before running them.
