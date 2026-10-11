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

Gallery tests exercise round snapshots, proposal lint, source integrity, atomic feedback, stale writes, approval, path safety, and HTTP origin/host boundaries. `tests/gallery/wait.test.mjs` covers the `wait` command, its session status file, and signal handling. Node HTTP tests need loopback listening permission. The gallery runtime needs no third-party Node packages; development uses `npm install` for Vite and Lit.

## Exploration and gallery smoke check

Run in an expendable Git repository. Needs Node.js 22+ and a browser in the harness; no Gradle or Android SDK.

1. Create an exploration with `node skills/android-design/gallery/cli.mjs create --project <fixture-repo>`. Record the production app and installed starter hashes.
2. Author three distinct proposals in `project/proposals/`, each with its own seed color and a rationale; at least one layers three or more tactics and names them.
3. Review each draft at `/runtime/review/?src=/draft/<id>/` in a browser, which frames all four devices. Call `window.reviewChecks()`, fix every failure, treat `complete: false` as unverified, and look at one screenshot per device. With no browser, the agent must report visual quality unverified.
4. Publish with `gallery/cli.mjs publish`. Separately, add a raw hex color, an inline `style=""`, or a type declaration outside a `.voice-*` class to a copy and confirm publish rejects it with file and line. Publishing never touches the original round.
5. Gallery smoke check: serve, open the URL, and switch across the four devices; the frames resize in place and cross the 840px breakpoint. Change seed color, variant, and light or dark, and confirm nothing is saved after reload.
6. Select one option, switch to another, and enter shared notes. Only one proposal may be selected. Submit a refinement and verify its saved snapshot and chat handoff. Test None of these with required notes. Verify save confirmation, restart the server, and reload. Notes and selections must survive. With two tabs, verify an outdated write is rejected without silently overwriting saved state. Exercise visible retry after a disconnected server.
7. Refine round: refine one direction in `project/`, review it in the browser, and publish another round. Check the original round's file hashes are unchanged. Verify the new round appears automatically without losing edits or interrupting history inspection.
8. Finish one selected revision and confirm the approved identity survives restart. Further writes and publication must be refused. Production app and installed starter hashes must still match.
9. Handoff report: the agent reports the approved round and proposal, the path of its published files, and `references/compose-mapping.md` as the porting table. It does not implement.
10. Stop the server and exercise cleanup only on an expendable, explicitly selected fixture exploration. It must refuse the repository root, unmarked folders, symlinks, and copied markers. Published files and notes are deleted together only on explicit confirmation.
11. Watch session: start a session (`gallery/cli.mjs wait`), press Refine, and observe the next round arrive without touching chat while the status line moves through waiting and working. Press Finish and confirm the session reports the approved revision and stops. Separately, let a session idle out and confirm the chat message and the gallery's "return to chat" fallback text.

The gallery never invokes an agent or implements a selected proposal. Test its website with the available browser tooling; Playwright is not a runtime dependency.

## Agent routing checks

- "Show me three home-screen ideas." Expect three HTML proposals in the requesting repository's ignored exploration folder, browser-reviewed with `reviewChecks()`, and a local gallery. Ask for a repository if none was selected, not a new app scaffold in the current directory.
- "Explore a redesign of Today." Expect read-only inspection of app code and dependencies, with writes only to exploration artifacts and the local Git exclusion.
- "Adjust option B." Expect saved feedback to be read, one refinement by default, and a new preserved round in the same exploration.
- "Keep refining while I review." Expect a watch session to start after the first publish. "Adjust option B" without a session still produces one preserved round. A failed browser review stops the session with visual quality reported unverified.
- Approving a design in the gallery must end exploration, not start implementation. "Implement the approved design" explicitly transitions to the requesting project's workflow, ports the proposal with `references/compose-mapping.md`, preserves its library versions, and compares the rendered Compose result with the approved proposal.
- "Explore Wear." Expect `create --starter android` and its experimental Wear shell and preview skill, not the four-device gallery or a claim that parked Wear evals pass.

Run the existing design evals after changing skill instructions. Model-based checks send prompts, source context, and screenshots through the configured agent service; obtain the required execution permission before running them.
