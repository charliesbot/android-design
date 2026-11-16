# Visual design exploration v4

**Date:** 2026-11-15

## TL;DR

Visual design exploration replaces the macOS Swing app and `.dmg` installer with a cross-platform plugin under `plugin/`, a ~25 KB Node `npx` MCP supervisor (zero idle JVM memory), a supervised headless Skiko JVM worker (`skiko-awt-runtime-all`), and a localhost HTML and SVG browser board. Inside that runtime, v4 introduces a **Code-First Compose Kotlin Sandbox** alongside v3's JSON pipeline: agents write real `@Composable` screens in `design/<slug>.explore.kt`, compiled in memory by `K2JVMCompiler` behind a bytecode/classloader allowlist and checked across five views in **328 to 460 ms warm**. Once the `.kt` sandbox passes a side-by-side 8-case agent A/B benchmark against v3 (matching or beating 6/8 rubric quality at >= 30% lower median wall time), v3's ~15,000 lines of A2UI JSON catalog, interpreter, schemas, and Kotlin exporter are removed; if compile retries eat the speed gain, the new outer runtime ships around the existing JSON interpreter instead.

## Design

### Why v4

v3 ([visual-design-exploration-v3.md](visual-design-exploration-v3.md)) proved that previewing with real Compose Material 3 and upstream Wear Compose hits the fidelity bar. Three structural problems remained:

1. **A closed JSON catalog limits brainstorming and taxes maintenance.** A2UI JSON restricts designs to 40 pre-wired component slots, blocking custom `Canvas` graphics, editorial overlaps, and bespoke `RoundedPolygon` compositions. Maintaining that intermediate language requires `Catalog.kt` and `WearCatalog.kt` (2,280 lines), 25+ interpreter files, JSON schemas, `export/` (8,071 lines), and a 71 KB `compose-mapping.md`, all updated whenever Material 3 changes.
2. **Generation is slow (~160 s median) because of dual-language model overhead.** In v3's timed trace, MCP tools took 2.5 s while 157.5 s was the model reading ~120 KB of Kotlin plus JSON documentation, reasoning in an unfamiliar flat ID-pointer JSON dialect across 17 to 36 turns ([script-ban-eval.md](script-ban-eval.md)), and making sequential `status`, `get_catalog`, `open_proposal`, `check_proposal`, and `render_view` calls.
3. **Native desktop packaging and split installs add friction.** `jpackage` cannot cross-compile `.dmg`/`.deb`/`.msi` bundles, `compose.desktop.currentOs` binds builds to one host OS, macOS users hit unsigned Gatekeeper quarantine (`xattr`) and `AppNap`, and installing skills separately from the MCP binary requires manual setup.

### Why Code-First works without Gradle

v1 took ~2 minutes per edit because every round booted Gradle, AGP, Android resource merging, and Robolectric, with no signature reference for beta Material 3 APIs.

Measured on Apple Silicon inside an already-booted headless JDK process (excluding ~0.5 s of initial JVM process startup) with `kotlin-compiler-embeddable:2.4.21`, `kotlin-compose-compiler-plugin-embeddable:2.4.21` (`sourceInformation=true`), and `:app`'s Compose Multiplatform `1.13.0-beta01` classpath:
- Compiling a 53-line Material 3 Expressive screen (`LargeFlexibleTopAppBar`, `MediumFloatingActionButton`, `Card`, `displaySmallEmphasized`, `LinearWavyProgressIndicator`, `LazyColumn`, `ListItem`) across 8 rounds took `1,909 ms` on round 1 (compiler JIT warmup) and **`267 to 429 ms`** on rounds 2 through 8.
- End-to-end compile plus 5-view `runDesktopComposeUiTest` rendering and Skia bitmap capture on a 29-line M3 Expressive screen across 5 rounds took **`2,071 ms`** on round 1 (`1,639 ms` compile + `432 ms` 5-view render) and **`328 to 460 ms`** on rounds 2 through 5 (`244 to 375 ms` compile + `79 to 85 ms` across all five views).

### Rejected alternatives

- **JetBrains `compose-hot-reload` (`1.2.0`) in a scratch Gradle module:** Avoids writing a ~180-line `K2JVMCompiler` host, at the cost of reopening a native desktop Swing window, requiring Gradle and Kotlin daemons plus a ~200 MB JBR 25 download (~1.5 to 2.5 GB RAM), and checking one live window per tool call rather than batch-checking all five views in one turn.
- **Pure Compose Multiplatform Wasm (`wasmJs`) or WebMCP:** `skiko.wasm` requires a browser WebGL canvas, so Node.js cannot run `HeadlessRenderer` without ~170 MB per-OS headless Chromium binaries or stalling on closed/backgrounded tabs. Coding harnesses also do not speak `navigator.modelContext` directly and still require a local stdio server and file watcher.
- **Building exploration directly on `kakao/prevue`:** Adopted on macOS for iterating inside target Android repositories after exploration (0.2 to 0.9 s `@Preview` hot-swap), while unsuitable as the exploration engine itself (macOS-only, requires `ANDROID_HOME` and an Android Gradle module, ~1.4 GB RAM per daemon, and no deterministic M3 layout or contrast rules).
- **CLI commands instead of MCP:** `claude -p` blocks granting `Bash` to spawned eval generators ([script-ban-eval.md](script-ban-eval.md)), interactive harnesses prompt on shell commands, and CLI stdout cannot return inline `ImageContent`, adding extra `Read` turns per PNG.

### Runtime: Node `npx` supervisor and disposable JVM worker

```text
Agent (Claude Code / Codex / Antigravity)
  │ stdio MCP (initialize + tools/list answered in Node: ~20 ms, 0 MB JVM heap when idle)
  ▼
npx @charliesbot/android-design-preview@<version>   (~25 KB Node supervisor)
  ├── Localhost HTTP + SSE server (127.0.0.1:<port>, Host + Origin + 256-bit session token)
  ├── Warms ~/.cache/ GitHub Release JAR (~132 MB, SHA-256 verified) in background on cache miss
  └── Spawns JVM only on first tool call & supervises over private stdio pipes (no JVM port)
        ▼
      Headless JVM Worker (java -Djava.awt.headless=true -jar android-design-preview.jar)
        ├── Skiko (skiko-awt-runtime-all: macos-arm64/x64, linux-arm64/x64, windows-arm64/x64)
        ├── K2JVMCompiler + Compose Compiler + constant-pool guard + SandboxClassLoader
        └── HeadlessRenderer + Wear Compose + SubcomposeLayout slot inspector + finding rules
```

**Background JAR warm-up and lazy JVM spawn.** npm tarballs past 100 MB hit registry timeouts and slow down cold `npx` installs. Publishing a ~25 KB pure-JS supervisor lets enabled plugin sessions answer MCP `initialize` and `tools/list` in ~20 ms with zero JVM memory. On supervisor startup, if `~/.cache/android-design-preview/<version>/android-design-preview.jar` is missing (first session after install or upgrade), Node starts a background download and SHA-256 verification immediately while the agent reads the brief; cached sessions only run a `fs.existsSync` check. The JVM worker itself is spawned only on the first `check_design` or `get_tree` call, locating `java` 17+ (`PATH`, `JAVA_HOME`, or Android Studio's bundled JBR) and honoring `ANDROID_DESIGN_PREVIEW_JAR` for local builds and `evals/run.py`.

**Split deadlines and worker recycling.** In-process JVM thread interruption cannot stop a tight loop (`while (true)`) inside composition, and repeated `ClassLoader` loads can retain entries in Skia or Compose caches. Running one JVM as a supervised child of Node solves both without running two JVMs. Deadlines are split by phase so slower Intel or Windows machines do not trip false timeouts on cold boot: JAR download reports progress separately, worker process boot plus round-1 JIT warmup gets a **30 s** readiness budget, and each in-worker `compile + render` request after readiness gets a strict **5 s** deadline before Node sends `SIGKILL` and respawns a clean worker. Node also recycles the worker every 25 compiles and after idle timeout.

**Minimal surfaces.** Agents see two MCP tools (`check_design` and `get_tree`). Humans and `evals/run.py` use two CLI commands (`serve` and `render`).

### Security and sandbox boundary

Compiling `.kt` files executes JVM bytecode, which changes v3's data-only trust model and interacts with [script-ban-eval.md](script-ban-eval.md). Three controls bound that surface:

1. **No network-reachable compile surface.** The JVM worker opens zero network ports. Node's HTTP server serves the browser board only: it requires `Host: 127.0.0.1:<port>` (blocking DNS rebinding), validates `Origin` plus a 256-bit per-session token (`http://127.0.0.1:<port>/?token=<secret>`) on state-changing requests, and exposes no endpoint that compiles arbitrary paths or source strings. Only the agent's stdio MCP pipe can call `check_design(path)`.
2. **Constant-pool bytecode verification, `SandboxClassLoader`, and inert UI locals.** Before loading compiled `.class` files, the worker inspects each classfile's constant pool and loads classes through an allowlisting `SandboxClassLoader` permitting only:
   - `androidx.compose.*`, `androidx.navigation3.*`, `androidx.wear.compose.*`, `androidx.graphics.shapes.*`, `com.materialkolor.*`, `kotlinx.collections.immutable.*`, and `bot.charlies.androiddesign.preview.kit.*`.
   - `kotlinx.coroutines.*` with `newSingleThreadContext` and `newFixedThreadPoolContext` (`ThreadPoolDispatcherKt`) explicitly denied (`Dispatchers.IO` is harmless with all I/O blocked).
   - Safe `kotlin.*` packages (`kotlin.*` with `kotlin.io.*`, `kotlin.system.*`, `kotlin.reflect.*`, and `kotlin.concurrent.*` explicitly denied so `kotlin.concurrent.thread { }` cannot spawn threads without referencing `Thread` directly).
   - Explicit `java.lang` value types (`Object`, `String`, `CharSequence`, `Number` subclasses, `Boolean`, `Enum`, `Math`, `Throwable`, standard runtime exceptions), explicit `java.util` collection and value types (`List`, `Map`, `Set`, `Collection`, `Iterable`, `Iterator`, `ArrayList`, `HashMap`, `LinkedHashMap`, `HashSet`, `LinkedHashSet`, `Collections`, `Arrays`, `Objects`, `Optional`, `Comparator`, `Locale`, `UUID`), and `java.util.concurrent.atomic.*`. Broad `java.util.*` is not allowed, blocking `java.util.prefs.*` (disk writes), `java.util.logging.*` (`FileHandler`), `java.util.concurrent.*` executors, `java.util.Timer`, `java.util.ServiceLoader`, and `java.util.zip.*` / `java.util.jar.*`.
   - In `java.lang.invoke.*`, only `LambdaMetafactory` and `StringConcatFactory` bootstrap entries required by Kotlin lambdas and string templates; direct `MethodHandles`, `Class.forName`, `ClassLoader`, `Runtime`, `ProcessBuilder`, `System`, `Thread`, `java.lang.reflect.*`, `java.io.*`, `java.nio.*`, and `java.net.*` are rejected before execution.
   In addition, `designBoard` and `wearBoard` override `LocalUriHandler`, `LocalClipboard`, and `LocalClipboardManager` with inert no-op implementations so explorations cannot open browser URLs or touch the host clipboard through allowed Compose UI APIs.
3. **Explicit trust boundary and eval parity.** Bytecode, classloader, and CompositionLocal sandboxing keeps LLM-authored explorations deterministic and side-effect-free (preserving `script-ban-eval.md`'s ban on file-generating scripts while keeping `claude -p` free of `Bash` permissions); it is not a multi-tenant VM boundary for untrusted third-party code.

### Exploration files and the `DesignBoard` kit

Explorations are tracked files at `design/<slug>.explore.kt`, optionally reading shared defaults from `DESIGN.md` front matter at the repository root.

Screen composables use standard AndroidX imports, marking the primary CTA with a namespaced `Modifier.testTag("ad:primary")` so copied screens never collide with an app's own `"primary"` test tags. The top-level `designBoard` or `wearBoard` block in `bot.charlies.androiddesign.preview.kit` wires bundled variable fonts (`ProposalFonts`), seed schemes, Navigation 3 adaptive panes (`ListDetailSceneStrategy`, `SupportingPaneSceneStrategy`), overlays, inert `LocalUriHandler`/`LocalClipboard` stubs, and safe relative image loading (`designPainter("assets/cover.jpg")`, confined beneath the exploration directory with `..` traversal blocked):

```kotlin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import bot.charlies.androiddesign.preview.kit.*

val preview = designBoard(
  name = "Tonight's plate",
  seed = 0xFFB5523B,
  variant = PaletteVariant.TonalSpot,
  brandFont = BundledFont.Fraunces,
) {
  direction("Editorial split") {
    listDetail(
      list = { onOpenDetail -> PlatesListScreen(onSelect = onOpenDetail) },
      detail = { PlateDetailScreen() },
      placeholder = { PickPlatePlaceholder() },
    )
  }
}
```

Multiple `direction(...)` blocks in one file render side by side on the browser board when comparing alternatives.

### One-call agent loop, slot inspection, and finding rules

Both `android-design` and `android-design-explore` speak Jetpack Compose Kotlin, backed by a compact ~2-page Material 3 Expressive `1.5.0` and Wear M3 signature sheet generated from the verified dependency jars so agents do not guess beta parameter names.

One MCP call, `check_design(path, directions?, views?)`, runs source token lint (rejecting raw `Color(...)` hex literals outside `designBoard` and `.copy(alpha = ...)`), compiles in memory, checks every registered screen and overlay across all catalog views (`light`, `dark`, `foldable`, `tablet`, `desktop`, or `watch` and `watch-large` on Wear), and when error findings hit zero, returns `[TextContent(checkJson), ImageContent(lightPng), ImageContent(desktopPng)]` at native resolution in one response.

**Slot-table and semantics extraction.** `androidx.compose.runtime:runtime-desktop:1.13.0-beta01` includes `LocalInspectionTables` and `parseSourceInformation` (without adding `ui-tooling-data`), annotated `@ComposeToolingApi` alongside `Composer.collectParameterInformation()`. Because `LocalInspectionTables` is bound when a subcomposition starts, `HeadlessRenderer` wraps the target composable inside a 6-line `SubcomposeLayout` under `CompositionLocalProvider(LocalInspectionTables provides tables)`, pairing `parseSourceInformation` (`C<line>@<offset>L<length>`) with placed `LayoutInfo` boxes and `SemanticsNode` trees to recover `<file>:<line>`, the Composable call identifier at `source[offset]`, unclipped `LayoutInfo` dimensions, and `ModifierInfo` entries. Because `@ComposeToolingApi` carries no cross-release stability guarantee, Compose versions remain pinned in `app/gradle/libs.versions.toml` and a dedicated canary unit test asserts that call names, line numbers, and bounds resolve on a fixture screen so upgrades fail loudly if the format changes. On Wear views, `HeadlessRenderer` scrolls the root vertical scroller via `SemanticsProperties.VerticalScrollAxisRange` and `SemanticsActions.ScrollBy` half a viewport at a time and merges nodes through `ScrolledTree.merge`.

Mapping of all 21 check functions in `FindingRules.kt`:

| Mechanism in v4 | Checks preserved |
|---|---|
| **`SemanticsNode` + `GetTextLayoutResult` + slot call names (15 checks)** | `touchTargets` (48 dp `touchBoundsInRoot`), `clippedText`, `undrawnText`, `truncatedText` (info), `truncatedLabels` (buttons, chips, tabs, nav items), `primaryLargest` (`testTag("ad:primary")` vs action call names per pane), `oneFab`, `offscreen`, `overflow` (unclipped `LayoutInfo.width` vs parent/view, exempting `HorizontalScrollAxisRange`), `lineLength` (`Card`, `ListItem`, and multi-line `Text` > 60 measured chars), `carouselItems` (info), `dockedToolbarWithNavBar`, `fullScreenDatePicker` (info), and `emptyDetailPane` (info, from `listDetail` when `placeholder` is omitted) |
| **`LayoutInfo.getModifierInfo()` + Wear/carousel geometry (3 checks)** | `weightCollapsed` (weighted non-`Spacer` node measuring `<= 0.5 dp`), `cornerClipped` (rounded `Shape` clip radius from `GraphicsLayer` modifiers vs text ink and control bounds), `roundEdgeClipped`, and `croppedByCarouselItem` |
| **`designBoard` + `TextLayoutResult` + bytecode/source guard (2 checks)** | `fonts` (reads resolved `ProposalFonts` family and `fontSize` on `TextLayoutResult.layoutInput.style`; unbundled `FontFamily` constructors and raw `Color(...)` / `.copy(alpha = ...)` rejected) |
| **2 px perimeter Skia bitmap sampling (1 check, upgraded)** | `contrast`: reads exact sRGB foreground luminance $L_{\text{fg}}$ from `TextLayoutResult.layoutInput.style.color`, samples the 2 px outer perimeter band around each drawn line's ink box (zero glyph anti-aliasing pollution, including white-on-white), and checks WCAG ratio at the 10th-worst background luminance percentile (`p90` when $L_{\text{fg}} > 0.179$, `p10` otherwise), catching low contrast over photos and gradients |
| **Dropped (1 info note)** | `arrangementNoSpace`: omitted because inline `Row` and `Column` calls do not retain their `Arrangement` instance on `LayoutInfo` |

### Browser board

Node serves an HTML, CSS, and SVG board at `http://127.0.0.1:<port>/?token=<secret>` and opens it in the default browser on interactive runs:

- **Multi-direction board and ~40 ms switches.** Shows multiple `direction(...)` blocks side by side or focuses one direction with Brightness, Seed Color, Style Variant, Screen, Overlay, and Device controls. Because `<slug>.explore.kt` stays compiled in the worker, changing theme or device settings re-renders in ~40 ms without recompiling.
- **Live file watching.** Node watches `DESIGN.md` and `<slug>.explore.kt`, recompiles on save (~330 ms warm), and pushes fresh frames over Server-Sent Events.
- **Click-to-source SVG overlay.** Maps `CompositionGroup` source locations and `SemanticsNode` bounds onto each 2x PNG: hovering shows the Composable name, `<file>:<line>`, and measured dp dimensions; clicking copies `<slug>.explore.kt:<line>` to the clipboard, and clicking navigation or overlay triggers switches the board state.

### Plugin bundle and moving to production

Distributable files live under `plugin/` (`plugin/skills/`, Antigravity `plugin.json` and `mcp_config.json`, Claude Code `.claude-plugin/`, Codex `.codex-plugin/`, and `.mcp.json` pinned to `@charliesbot/android-design-preview@<version>`) so installs transfer ~150 KB rather than `sources/`, `evals/`, and `app/`. Root marketplace manifests point to `./plugin` via git-subdir, while `npx skills add` and Chai (`~/chai.toml`) read `plugin/skills/` directly.

Approving a direction needs no code generator (`export/` is gone), though porting still wires the screen into the app: the agent copies the approved `@Composable` screen functions into the target project's module layout, removes `testTag("ad:primary")` if undesired, connects them to the app's theme, ViewModel state, and Navigation 3 routes, and verifies the live screens with `kakao/prevue` on macOS or `compose-preview` elsewhere.

## Open questions

- **Agent-run 8-case A/B gate (gates deleting v3's JSON pipeline):** Warm compile plus 5-view render latency (`328 to 460 ms`), `@ComposeToolingApi` slot extraction, and 2 px perimeter contrast sampling are verified; end-to-end LLM authoring behavior across the 8 eval briefs (`music`, `finance`, `settings`, `trek`, `recipes`, `workout`, `player`, `reminder`) is not yet measured. Removing v3's ~15,000 lines of JSON catalog, interpreter, schemas, and exporter requires: (1) rubric pass rate >= 6/8 ([script-ban-eval.md](script-ban-eval.md)), (2) median generation wall time >= 30% faster than the v3 JSON baseline, and (3) median compile-fix retries <= 1 per case. If beta API compile retries offset the speed gain, the outer `plugin/`, Node supervisor, headless worker, and browser board ship around v3's JSON interpreter instead.

## Links

- [Visual design exploration v3](visual-design-exploration-v3.md): the JSON-interpreted macOS previewer architecture kept alongside v4 until the 8-case agent A/B gate passes
- [Wear OS in the previewer](wear-os-previewer.md): the upstream `androidx.wear.compose` desktop port reused unchanged on the worker classpath
- [Script ban eval baseline](script-ban-eval.md): the 8-case v3 baseline (17 to 36 turns, 6/8 rubric) and why `claude -p` generators use MCP rather than `Bash`
- [`kakao/prevue`](https://github.com/kakao/prevue): browser-based `@Preview` hot-swap tool used on macOS when wiring approved screens into an Android app
