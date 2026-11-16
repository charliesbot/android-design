# Exploration workflow checks

The design evals check one exploration (`design/<slug>.explore.kt` plus `DESIGN.md`) in a prepared fixture. They do not prove the requesting app's isolation or agent routing. Check those separately.

## Deterministic checks

From the repository root:

```bash
python3 -m unittest discover scripts
python3 -m unittest discover evals
python3 scripts/check_coverage.py
(cd app && ./gradlew build spotlessCheck)
(cd supervisor && npm test && npm pack --dry-run)
```

Requires Node.js 18+ and Java 17+ (`./gradlew :app:workerJar` builds `app/app/build/libs/android-design-preview.jar`). No Android SDK or Chrome binary is required.

The Gradle tests cover source linting, bytecode/classloader sandbox policy, Compose tooling slot extraction, pixel contrast sampling, finding rules, CLI `render`, and the stdio JSON-RPC `worker`. The Node tests cover the MCP supervisor, browser board HTTP origin/token checks, and end-to-end real-JAR MCP execution. The Python tests cover the runner, `report.json` checks, and the agent launchers without model calls.

## Exploration smoke check

Run in an expendable folder (a Git repository or not). Needs Node 18+, Java 17+, and a harness with the `plugin/` installed (or `android_design_preview` registered in MCP config).

1. Start an interactive session: check that `android_design_preview` (`check_design` and `get_tree`) is available in MCP tools. When unavailable, the agent reports the missing server and registration instructions from `plugin/mcp_config.json` rather than falling back to HTML or raw CLI loops.
2. Have the agent author `<fixture-folder>/design/<slug>.explore.kt` and `DESIGN.md` with its own seed color using verified signatures from `plugin/skills/android-design-explore/references/compose-signatures.md`. Ask for three ideas in one exploration: expect three `direction(...)` blocks inside `<slug>.explore.kt` or cleanly distinct directions, each compiled and verified with `check_design`.
3. Confirm `check_design` compiles in memory, checks all five phone views (`light`, `dark`, `foldable`, `tablet`, `desktop`) in one call, returns primary screenshots once error findings reach zero, and starts the localhost browser board (`http://127.0.0.1:<port>/?token=<secret>`). Verify `get_tree` returns geometry and `<file>:<line>` source locations where inspection is needed.
4. Open the browser board link: switch devices, brightness, seed color, palette variant, screen, and overlays (re-renders warmly in ~40 ms without recompiling), hover/click the click-to-source SVG overlay to copy `<file>:<line>`, and edit the `.explore.kt` file directly to verify SSE live reload.
5. Invalid exploration: introduce a compile error, raw `Color(...)` hex literal, or disallowed `java.io.File` reference. `check_design` must return validation/compiler/security errors without writing production files, and `<supervisor> render <file.explore.kt> --out <dir>` must exit 2 with the same errors in `report.json`.
6. Approving a direction reports the chosen direction name, `.explore.kt` file path, and next steps from `plugin/skills/android-design-explore/references/porting.md`. For an explore-only request it does not modify production app files.

## Agent routing checks

- "Explore a home screen idea." Expect one `.explore.kt` file with one `direction(...)`. "Show me three home-screen ideas." Expect one `.explore.kt` file holding multiple `direction(...)` alternatives checked together and shown side by side on the browser board. Outside a repository it still works; it never scaffolds a new app.
- "Build a music theory app with the android-design skill." Expect one exploration shown on the browser board and a wait for approval before copying/wiring composables into app modules per `porting.md`.
- "Build a habit tracker app in Compose." (no design ask) Expect no `.explore.kt` file: the agent codes directly, applying `android-design`'s decisions and screen check.
- "Explore a redesign of Today." Expect read-only inspection of app code and dependencies, with writes only to `design/<slug>.explore.kt` and `DESIGN.md`.
- "Make it warmer." Expect one in-place edit to the active `.explore.kt` (or `DESIGN.md`), with no duplicate version files (`-v2.explore.kt`).
- After an explore-only request, approving a design in chat must end exploration, not start implementation. Either way, "Implement the approved design" follows `porting.md`: copies approved `@Composable` screens into the app, replaces `SymbolIcon`, `designPainter`, `bundledFontFamily`, and kit helpers with real app resources/Navigation 3, and verifies live previews via `kakao/prevue` or `compose-preview`.
- "Explore a Wear timer app." Expect a `wearBoard` file (`design/<slug>-wear.explore.kt`) checked at `watch` and `watch-large` with one CTA tagged `Modifier.testTag("ad:primary")`, skipping phone views and size classes. A Wear tile or widget request gets the skill saying those are outside this workflow.
- Server not connected. Expect the agent to say the `android_design_preview` server is missing and how to register it, not to fall back to writing HTML.

Run the design evals (`evals/README.md`) after changing skill instructions. Model-based checks send prompts, source context, and screenshots through the configured agent service; obtain the required execution permission before running them.
