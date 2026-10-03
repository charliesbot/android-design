# Exploration workflow checks

The main design evals check implementation in a prepared project. They do not test whether an agent chooses an isolated workspace for an exploratory request. Keep that distinction when interpreting results.

## Deterministic checks

```bash
python3 -m unittest discover scripts
python3 scripts/check_coverage.py
python3 skills/android-design/scripts/explore.py create
```

In the returned directory, set the SDK path if necessary and run:

```bash
./gradlew projects spotlessCheck :app:assembleDebug :wear:assembleDebug
python3 render.py app
python3 render.py wear
```

Confirm all five core modules exist. Inspect the light/dark phone PNGs and the Wear PNG. Check fonts, readable foreground/background pairs, and complete screenshots. Wear is a scaffold check, not design-quality approval. The starter must remain free of generated files after these checks.

## Exploration and refinement smoke check

Use the installed `compose-preview` skill and CLI for this agent-facing workflow. The deterministic checks above exercise the starter maintenance harness, not compatibility with compose-ai-tools. A successful maintenance render does not establish that the external renderer works.

1. Start from a separate read-only app fixture and record its file hashes plus those of the installed starter. Create a workspace with the helper from the fixture's working directory.
2. In the copy, create three distinct sample-state compositions and three named main-source `@Preview` functions with the same viewport/content. Render and inspect all three; confirm their images differ.
3. Revise only the first composition and render again. Confirm only its PNG hash changes, while the source fixture and installed starter hashes are unchanged.
4. Run the cleanup helper only on an expendable, explicitly selected test workspace. Confirm the source fixture and starter remain intact. Unit tests also cover missing markers, symlinks, mismatched markers, and stale output removal on render failure.

## Agent routing checks

Test these prompts in an authorized agent session with the installed skill. They are not covered by the deterministic smoke check:

- "I'm thinking of a reading app. Show me three phone home-screen ideas." Expect temporary Compose proposals, no app scaffold in the current working directory.
- "Help me explore redesign ideas for this app's Today screen." Expect read-only app inspection and proposals in a temporary copy, with no branch or edits in the target app.
- "Adjust option B." Expect reuse of the same workspace, not a new starter or edits in the app.
- "Implement option B in this app." Expect an explicit transition to the target project's approval and branch workflow.
- "Explore a Wear version." Expect the watch shell and references, an experimental label, and no claim that parked Wear quality evals pass.

Run the existing design evals after changing skill instructions as well. Model-based checks send prompts, source context, and screenshots through the configured agent service; obtain the required execution permission before running them.
