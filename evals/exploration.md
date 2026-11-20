# Exploration workflow checks

The design evals check one exploration (`design/<slug>.explore.kt`) in a prepared fixture. They do not prove the requesting app's isolation or agent routing. Check those separately.

## Deterministic checks

From the repository root:

```bash
python3 -m unittest discover scripts
python3 -m unittest discover evals
python3 scripts/check_coverage.py
(cd app && ./gradlew build spotlessCheck)
```

Requires Java 17+ (`./gradlew :app:workerJar` builds `app/app/build/libs/android-design-preview.jar`). No Node.js, Android SDK, or Chrome binary is required.

The Gradle tests cover source linting, bytecode/classloader sandbox policy, Compose tooling slot extraction, pixel contrast sampling, finding rules with reference anchors, the Kotlin HTTP/SSE server, per-view priority queue, browser board security and selection re-resolution, `HttpURLConnection` CLI commands, and headless `render`. The Python tests cover manifests, source coverage, eval runner, `report.json` checks, multi-turn aggregation, and per-harness transcript audits without model calls.

## Exploration smoke check

Run in an expendable folder (a Git repository or not). Needs Java 17+ and `android-design-preview` on `PATH` (via Claude Code plugin `bin/` or `plugin/bin/android-design-preview install`).

1. Start an interactive session: verify `android-design-preview status` works. When missing or exiting `3`, the agent reports the missing prerequisite and links `plugin/skills/android-design-explore/references/setup.md` rather than falling back to HTML or editing user sandbox config unasked.
2. Have the agent author `<fixture-folder>/design/<slug>.explore.kt` with its own seed color in `designBoard(...)` using verified signatures from `plugin/skills/android-design-explore/references/compose-signatures.md`. Ask for three ideas in one exploration: expect three `direction(...)` blocks inside `<slug>.explore.kt` or cleanly distinct directions, each compiled and verified with `android-design-preview check`.
3. Confirm unscoped `android-design-preview check` compiles in memory, checks all five phone views (`light`, `dark`, `foldable`, `tablet`, `desktop`) in one call, writes 2x PNGs (`light` and `desktop` on pass; up to two deduplicated failing views on fail), and returns `boardUrl` (`http://127.0.0.1:<port>/?view=<token>`). Confirm scoped `check --direction <d> --view <v>` returns a 1x PNG path, and `android-design-preview tree` returns geometry and `<file>:<line>` source locations when needed.
4. Open the browser board (`android-design-preview open <file>`): switch devices, brightness, seed color, palette variant, screen, and overlays (re-renders warmly in ~40 ms without recompiling), click a node on the SVG overlay to record a board selection (`android-design-preview selection <file>`), and edit the `.explore.kt` file directly to verify focus-first SSE live reload.
5. Invalid exploration: introduce a compile error, raw `Color(...)` hex literal, or disallowed `java.io.File` reference. `android-design-preview check` must exit `1` with validation/compiler/security errors without writing production files, and `android-design-preview render <file.explore.kt> --out <dir>` must exit `2` with the same errors in `report.json`.
6. No `DESIGN.md` is created or edited while exploring. Approving a direction records its seed, scheme variant, and brand font in `DESIGN.md` at the repo root, then reports the chosen direction name, `.explore.kt` file path, and next steps from `plugin/skills/android-design-explore/references/porting.md`. For an explore-only request it does not modify production app files.

## Agent routing checks

- "Explore a home screen idea." Expect one `.explore.kt` file with one `direction(...)`. "Show me three home-screen ideas." Expect one `.explore.kt` file holding multiple `direction(...)` alternatives checked together and shown side by side on the browser board. Outside a repository it still works; it never scaffolds a new app.
- "Build a music theory app with the android-design skill." Expect one exploration shown on the browser board and a wait for approval before copying/wiring composables into app modules per `porting.md`.
- "Build a habit tracker app in Compose." (no design ask) Expect no `.explore.kt` file: the agent codes directly, applying `android-design`'s decisions and screen check.
- "Explore a redesign of Today." Expect read-only inspection of app code and dependencies, with writes only to `design/<slug>.explore.kt`; an existing `DESIGN.md` is read, not edited.
- "Make it warmer." Expect one in-place edit to the active `.explore.kt`, with no duplicate version files (`-v2.explore.kt`).
- After an explore-only request, approving a design in chat must end exploration, not start implementation. Either way, "Implement the approved design" follows `porting.md`: copies approved `@Composable` screens into the app, replaces `SymbolIcon`, `designPainter`, `bundledFontFamily`, and kit helpers with real app resources/Navigation 3, and verifies live previews via `kakao/prevue` or `compose-preview`.
- "Explore a Wear timer app." Expect a `wearBoard` file (`design/<slug>-wear.explore.kt`) checked at `watch` and `watch-large` with one CTA tagged `Modifier.testTag("ad:primary")`, skipping phone views and size classes. A Wear tile or widget request gets the skill saying those are outside this workflow.
- Tool missing or Java unavailable (`exit 3`). Expect the agent to stop, report the prerequisite from `references/setup.md`, and wait rather than falling back to writing HTML.

Run the design evals (`evals/README.md`) after changing skill instructions. Model-based checks send prompts, source context, and screenshots through the configured agent service; obtain the required execution permission before running them.
