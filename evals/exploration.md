# Exploration workflow checks

The design evals check implementation in a prepared project. They do not prove the gallery's persistence, the requesting app's isolation, or agent routing. Check those separately.

## Deterministic checks

From the repository root:

```bash
python3 -m unittest discover scripts
python3 -m unittest discover evals
node --test tests/gallery/*.test.mjs
python3 scripts/check_coverage.py
```

Gallery tests exercise round snapshots, image validation, source integrity, atomic feedback, stale writes, approval, path safety, and HTTP origin/host boundaries. Node HTTP tests need loopback listening permission. No third-party Node packages are required.

Create an exploration in an expendable Git repository with `scripts/explore.py create --project <fixture-repo>`. In the returned `project/`, configure the SDK if necessary and run:

```bash
./gradlew projects spotlessCheck :app:assembleDebug :wear:assembleDebug
python3 render.py app
python3 render.py wear
```

These are starter maintenance checks, not evidence that Compose Preview CLI works. Wear remains an experimental scaffold check, not design-quality approval. Never build inside the installed starter.

## Render and gallery smoke check

1. Record the source fixture and installed starter hashes. Create an exploration under the fixture, then render the four named main-source previews using the installed `compose-preview` skill and CLI. Verify actual PNG dimensions and inspect every device layout. The three modeled device profiles must not be described as factory-default emulation.
2. In the copy, create three distinct sample-state compositions, each with four matching previews. Render, inspect, and publish all three with `gallery/cli.mjs publish`. Start `serve`, open the printed localhost URL, switch devices, and inspect an expanded preview.
3. Select one option, switch to another, and enter shared notes. Only one proposal may be selected. Submit a refinement and verify its saved snapshot and chat handoff. Test None of these with required notes. Verify save confirmation, restart the server, and reload. Notes and selections must survive. With two tabs, verify an outdated write is rejected without silently overwriting the saved state. Exercise visible retry after a disconnected server.
4. Refine one direction in the working `project/`, rerender, inspect, and publish another round. Check the original round's source and image hashes are unchanged. Verify the new round appears automatically without losing edits or interrupting history inspection. Finish one selected revision and confirm the approved identity survives restart. Further writes/publication must be refused. Production app hashes and installed starter hashes must still match.
5. Stop the server and exercise cleanup only on an expendable, explicitly selected fixture exploration. It must refuse the repository root, unmarked folders, symlinks, and copied markers. Published source, PNGs, and notes are deleted together only on explicit confirmation.

The gallery never invokes an agent, runs Gradle, or implements a selected proposal. Test its website with the available browser tooling; Playwright is not a runtime dependency or proposal capture layer.

## Agent routing checks

- "Show me three home-screen ideas." Expect three real Compose proposals in the requesting repository's ignored exploration folder, four PNGs per proposal, and a local gallery. Ask for a repository if none was selected, not a new app scaffold in the current directory.
- "Explore a redesign of Today." Expect read-only inspection of app code and dependencies, with writes only to exploration artifacts and the local Git exclusion.
- "Adjust option B." Expect saved feedback to be read, one refinement by default, and a new preserved round in the same exploration.
- Approving a design in the gallery must end exploration, not start implementation. "Implement the approved design" explicitly transitions to the requesting project's workflow and preserves its library versions.
- "Explore Wear." Expect the experimental watch shell and preview skill, not the four-device gallery or a claim that parked Wear evals pass.

Run the existing design evals after changing skill instructions. Model-based checks send prompts, source context, and screenshots through the configured agent service; obtain the required execution permission before running them.
