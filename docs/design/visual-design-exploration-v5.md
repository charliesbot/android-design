# Visual design exploration v5

**Date:** 2026-11-18

## TL;DR

v5 adopts Prevue's architecture and loop: a command-line tool, `android-design-preview`, that talks over localhost to one long-running Kotlin server, which compiles, renders, checks, and serves the browser board. Both the command and the server are Kotlin, replacing v4's Node MCP supervisor. Each save produces one shared result, iteration checks only the direction and view being changed and always writes its image, the board updates the screen the person is looking at first, and a board click reaches the agent as `file:line`. The explore skill shrinks to the order of steps, with command details in `--help` and guidance attached to findings, since model reading time outweighs tool time. v4's bytecode sandbox, `designBoard` kit, and 21 finding rules carry over unchanged, and a full check of every view stays the gate before showing the person.

## Design

### Why v5

v4 ([visual-design-exploration-v4.md](../archive/visual-design-exploration-v4.md)) made explorations real Compose code with deterministic checks and passed its rollout gate. Two problems remained.

**The loop was slow, though the engine was not.** A Prevue 0.22.1 fit test built the same three-direction board (editorial, map led, itinerary) for both tools and ran the same edit script on Apple Silicon:

| Structural edit, save to result | Time |
|---|---|
| Prevue `wait`, focused card | 1.16 s |
| v4 `check_design`, every direction and view | 6.9 to 7.7 s |
| v4 compile plus render, one direction and one view | 0.45 to 0.55 s |
| v4 save to board update | 3.3 to 3.5 s |

v4's board watcher ran a full `compileAndCheck` 120 ms after each save, and the agent's `check_design` then queued behind it and repeated the same work. Every call checked all 15 views (about 2.8 s) however small the edit, and an unchanged file was compiled again. v4's "328 to 460 ms warm" figure was measured on a 29-line, single-direction screen and holds per view, not per realistic board. The agent also iterated without seeing: images returned only once every error finding cleared, and then always `light` and `desktop`, never the view that failed.

**The architecture spread across two languages.** About 1,900 lines of Node (MCP server, worker supervisor, board server, CLI, JAR cache, Java locator) wrapped the Kotlin worker, plus an MCP configuration to keep correct in each of three harnesses. Prevue showed a simpler shape: a CLI that every harness can run through its shell, in front of one long-running server that owns compiling, rendering, and the viewer. Prevue's own CLI is Bash and Python and runs only on macOS; v5 takes its architecture with a Kotlin client so the tool stays one language and keeps Linux and Windows.

Prevue itself is not adopted as the engine: it needs an Android Gradle module, runs only on macOS, and carries no design checks. In the same test v4 flagged a foldable line-length problem that Prevue rendered without comment.

### Why Code-First works without Gradle

v1 took ~2 minutes per edit because every round booted Gradle, AGP, Android resource merging, and Robolectric, with no signature reference for beta Material 3 APIs.

Measured on Apple Silicon inside an already-booted headless JDK process (excluding ~0.5 s of initial JVM process startup) with `kotlin-compiler-embeddable:2.4.21`, `kotlin-compose-compiler-plugin-embeddable:2.4.21` (`sourceInformation=true`), and `:app`'s Compose Multiplatform `1.13.0-beta01` classpath:
- Compiling a 53-line Material 3 Expressive screen (`LargeFlexibleTopAppBar`, `MediumFloatingActionButton`, `Card`, `displaySmallEmphasized`, `LinearWavyProgressIndicator`, `LazyColumn`, `ListItem`) across 8 rounds took `1,909 ms` on round 1 (compiler JIT warmup) and **`267 to 429 ms`** on rounds 2 through 8.
- End-to-end compile plus 5-view `runDesktopComposeUiTest` rendering and Skia bitmap capture on a 29-line M3 Expressive screen across 5 rounds took **`2,071 ms`** on round 1 (`1,639 ms` compile + `432 ms` 5-view render) and **`328 to 460 ms`** on rounds 2 through 5 (`244 to 375 ms` compile + `79 to 85 ms` across all five views).

### Rejected alternatives

- **v4's Node MCP supervisor:** It kept idle JVM memory at zero and returned PNGs inline, but cost a second language, about 1,900 lines of glue, and three harness MCP configurations. v5 keeps idle memory at zero by exiting the server when idle, and the agent reads PNGs by path as it does with Prevue.
- **A Bash and Python client, as in Prevue:** Least code, but three languages and macOS only. Windows would need a second PowerShell client.
- **A Go client:** A single static binary that starts in milliseconds and needs no Java, but the server needs Java regardless. It would add a second language, a request and response contract defined twice, and six OS and architecture binaries in the plugin, to save about 0.05 s per call (measured below).
- **A one-shot CLI with no server:** Every call would pay JVM start and compiler warmup (about 2 s for the first compile), losing the 0.5 s loop.
- **Migrating exploration to `kakao/prevue`:** Measured above. Its loop and architecture are adopted; its engine is not a fit (macOS only, needs `ANDROID_HOME`, a Gradle module, and a classpath dump, about 1.4 GB RAM per daemon, 1.2 to 1.9 s save to screen, and no Material 3 checks). Prevue stays the tool for iterating on real `@Preview`s inside an Android app after a direction is approved.
- **Kit placeholders such as `MockMap` and `MockChart`:** They would save the agent from hand-drawing stand-ins (the fit test's illustrative map took about 35 lines of `Canvas`), but risk a generic look shared by every exploration, against the skill's rule that graphics come from the subject. Not in v5; reconsider only if eval telemetry shows hand-drawn maps and charts are a real output cost.
- **JetBrains `compose-hot-reload` (`1.2.0`) in a scratch Gradle module:** Requires Gradle and Kotlin daemons plus a ~200 MB JBR 25 download (~1.5 to 2.5 GB RAM) and reopens a native desktop window per check.
- **Pure Compose Multiplatform Wasm (`wasmJs`) or WebMCP:** `skiko.wasm` requires a browser WebGL canvas, so headless checks would need ~170 MB per-OS Chromium binaries or stall on closed or backgrounded tabs.

### Runtime: Kotlin CLI and one Kotlin server

```text
agent shell / person
  │  android-design-preview <command> <file> ...      (one JSON line out, fixed exit codes)
  ▼
bin/android-design-preview  (sh, and .cmd on Windows: find Java 17+, fetch the JAR once, exec)
  ▼
Kotlin CLI client  (same JAR, short-lived)
  │  finds the server for this repository, starts it if absent, enforces the request deadline
  ▼  HTTP on 127.0.0.1:<port>, client secret
Kotlin server  (one JVM per repository, started on first use, exits when idle)
  ├── K2JVMCompiler + Compose Compiler + constant-pool guard + SandboxClassLoader
  ├── HeadlessRenderer (Skiko) + Wear Compose + slot inspector + finding rules
  ├── file watcher + result store (one result per save)
  └── browser board: HTML, SVG, Server-Sent Events
```

**Bootstrap.** The plugin ships `bin/android-design-preview` (POSIX `sh`) and `bin/android-design-preview.cmd`, each a few dozen lines. They locate Java 17+ (`PATH`, `JAVA_HOME`, or Android Studio's bundled JBR), download `~/.cache/android-design-preview/<version>/android-design-preview.jar` from the GitHub Release on first use with SHA-256 verification, and run its client. `ANDROID_DESIGN_PREVIEW_JAR` still points local builds and `evals/run.py` at a development JAR. These scripts and the board's browser JavaScript are the only non-Kotlin code.

**Client start cost.** The client runs from the same 158 MB JAR as the server; the JAR's size adds nothing measurable, since the JVM reads only the classes it loads. On Apple Silicon with JDK 17, a JVM that boots and makes one request to a local server takes about 0.05 s with `HttpURLConnection` and about 0.5 s with `java.net.http.HttpClient`, whose setup alone costs about 0.47 s. The client therefore uses `HttpURLConnection`, keeping a warm scoped `check` near the 0.5 s of the server's own work.

**Finding the server.** As in Prevue, no command takes a port. The client derives the repository from the file path, computes a preferred port from a hash of the repository root within a reserved range, and keeps a record per server (pid, port, JAR version, start time, client secret, board view token) under `~/.cache/android-design-preview/servers/`. When another program already holds the preferred port, the server binds an OS-assigned port instead; clients always read the port from the record. Liveness is a `/status` request, not a pid check, since pids are reused after a reboot. A record whose server does not answer, or whose JAR version differs from the client's, is replaced by starting a fresh server.

**Lifecycle and deadlines.** The server starts on the first command for its repository and exits after an idle timeout, so idle memory stays at zero between sessions. A JVM cannot stop a tight loop (`while (true)`) inside composition, and repeated `ClassLoader` loads can retain entries in Skia or Compose caches. Two supervisors cover this, because watcher jobs run when no client is waiting:

- **A watchdog thread in the server** times every job against its deadline (30 s for the first compile after start, 5 s for each warm compile and one-view render). When a job passes it, the watchdog appends the job's content hash and scope to a `timeouts` list in the server record, then calls `Runtime.halt`. The next `check` for that hash answers `timeout` from the record at once instead of running the same hang again, and a new save clears it. A person using only the board sees it marked stopped, with the command that restarts it.
- **The client** enforces the same deadlines from outside, for a server too stuck to run its watchdog (GC thrashing, a deadlock): it kills the process, starts a fresh server, and returns a `timeout` failure.

The server also recycles itself every 25 compiles: it answers the 25th compile's request, then exits as it does when idle, and the next command starts a fresh server. A restarted server reuses the record's port and view token, so an open board's `EventSource` reconnects on its own; if the port had to change, the next `check` response carries the new `boardUrl`.

### CLI contract

Every command answers with one JSON line and an exit code: `0` success, `1` compile, render, or check failure, `2` usage or a choice to make, `3` server or prerequisites not ready. A response may carry `next` (the exact command to run), `ask` (a question the agent must put to the person before continuing), or `tell` (a message to pass on before stopping). Each command's `--help` documents its arguments and response fields, so the skill holds only the order of steps and the rules for talking to the person, and the CLI explains itself to any agent.

| Command | Purpose |
|---|---|
| `check <file> [--direction d] [--screen s] [--view v]` | Compile and check. Unscoped, it checks every direction, screen, and view. Returns findings, layout summaries, PNG paths, `boardUrl`, and any new `selection`. |
| `tree <file> --direction d --view v` | Layout bounds, call names, and `file:line` for one render, when a finding needs exact locations. |
| `selection <file>` | The node the person last clicked on the board, or exit `1` when there is none. |
| `open <file>` | Print and open the board URL. |
| `status`, `stop` | Server state for the current repository, and shutting it down. |
| `render <file> --out <dir>` | Write PNGs and trees for humans and `evals/run.py`. |
| `install` | Link the command into `~/.local/bin` for harnesses that do not put the plugin's `bin/` on `PATH`. |

### What the agent reads

Tool time is the smaller part of an exploration. In v3's timed trace, 157.5 of 160 s was the model reading and reasoning, and v4's median rollout run took 317 s of wall time while each tool call took seconds. v5 therefore treats context as part of the loop.

Today an exploration always reads `android-design/SKILL.md` (40.7 KB), `android-design-explore/SKILL.md` (7.7 KB), and `compose-signatures.md` (9.9 KB), about 58 KB, and steps can pull in `components.md` (36.2 KB), `theming.md` (22.2 KB), `layout.md` (11.0 KB), and `wear-os.md` (10.6 KB), up to about 144 KB in all. v5 changes how that knowledge reaches the agent:

- **The explore skill holds only the order of steps and the rules for talking to the person,** as Prevue's does in about 110 lines. Command arguments, response fields, and failure meanings live in each command's `--help`, read only when the agent needs them.
- **Findings carry their own guidance.** Each finding includes its fix hint and the reference section that explains it (for example, `line-length` points to `layout.md#grids-and-max-widths`), so references are read when a finding calls for them rather than up front.
- **Always-read files hold rules; quotes, examples, and recipes live in references.** `android-design/SKILL.md` keeps the design questions and rules every exploration needs. Supporting material moves to the reference that a step or finding points to. Every such move is judged by the rubric evals, since this content is the skill's design judgment.
- **Evals report reading cost.** Generator telemetry records the bytes of skill files each case reads, so a skill change is judged by its cost as well as its rubric score.

### Security and sandbox boundary

Compiling `.kt` files executes JVM bytecode. Three controls bound that surface:

1. **Only the local user can request a compile.** The server binds `127.0.0.1` only and requires `Host: 127.0.0.1:<port>` to block DNS rebinding. Compile, check, tree, and selection endpoints require the client secret, a 256-bit value stored in the server record with owner-only permissions (`0600`), so only processes running as the user can reach them. The browser board uses a separate view token in its URL that can read renders and record selections but cannot request a compile, and its state-changing requests also validate `Origin`.
2. **Constant-pool bytecode verification, `SandboxClassLoader`, and inert UI locals.** Before loading compiled `.class` files, the server inspects each classfile's constant pool and loads classes through an allowlisting `SandboxClassLoader` permitting only:
   - `androidx.compose.*`, `androidx.navigation3.*`, `androidx.wear.compose.*`, `androidx.graphics.shapes.*`, `com.materialkolor.*`, `kotlinx.collections.immutable.*`, and `bot.charlies.androiddesign.preview.kit.*`.
   - `kotlinx.coroutines.*` with `newSingleThreadContext` and `newFixedThreadPoolContext` (`ThreadPoolDispatcherKt`) explicitly denied (`Dispatchers.IO` is harmless with all I/O blocked).
   - Safe `kotlin.*` packages (`kotlin.*` with `kotlin.io.*`, `kotlin.system.*`, `kotlin.reflect.*`, and `kotlin.concurrent.*` explicitly denied so `kotlin.concurrent.thread { }` cannot spawn threads without referencing `Thread` directly).
   - Explicit `java.lang` value types (`Object`, `String`, `CharSequence`, `Number` subclasses, `Boolean`, `Enum`, `Math`, `Throwable`, standard runtime exceptions), explicit `java.util` collection and value types (`List`, `Map`, `Set`, `Collection`, `Iterable`, `Iterator`, `ArrayList`, `HashMap`, `LinkedHashMap`, `HashSet`, `LinkedHashSet`, `Collections`, `Arrays`, `Objects`, `Optional`, `Comparator`, `Locale`, `UUID`), and `java.util.concurrent.atomic.*`. Broad `java.util.*` is not allowed, blocking `java.util.prefs.*` (disk writes), `java.util.logging.*` (`FileHandler`), `java.util.concurrent.*` executors, `java.util.Timer`, `java.util.ServiceLoader`, and `java.util.zip.*` / `java.util.jar.*`.
   - In `java.lang.invoke.*`, only `LambdaMetafactory` and `StringConcatFactory` bootstrap entries required by Kotlin lambdas and string templates; direct `MethodHandles`, `Class.forName`, `ClassLoader`, `Runtime`, `ProcessBuilder`, `System`, `Thread`, `java.lang.reflect.*`, `java.io.*`, `java.nio.*`, and `java.net.*` are rejected before execution.
   In addition, `designBoard` and `wearBoard` override `LocalUriHandler`, `LocalClipboard`, and `LocalClipboardManager` with inert no-op implementations so explorations cannot open browser URLs or touch the host clipboard through allowed Compose UI APIs.
3. **Explicit trust boundary and eval parity.** Bytecode, classloader, and CompositionLocal sandboxing keeps LLM-authored explorations deterministic and side-effect-free; it is not a multi-tenant VM boundary for untrusted third-party code. Exploration code now shares a JVM with the HTTP server, so the allowlist, not process separation as in v4, is what keeps it away from the server's sockets and client secret. Eval generators keep `script-ban-eval.md`'s ban on file-generating scripts, enforced per harness:
   - **Claude Code** allows only `Bash(android-design-preview:*)` and edits inside the case folder. Verified with `claude -p --allowedTools 'Bash(android-design-preview:*)'`: the command runs, `python3` is denied, and a chained `android-design-preview status && python3 ...` is blocked before either part runs.
   - **Antigravity** cannot allow one command alone. Verified with `agy -p`: a `deny` of `command(*)` overrides an `allow` of `command(android-design-preview *)`, and without the deny every command runs, chains included.
   - **Codex** generators already have a shell in v4, since `workspace-write` runs commands.

   For Antigravity and Codex, `evals/backends.py` audits the generator's transcript, which it already parses for tool calls, with a per-harness rule: Antigravity reads files through `read_file`, so its audit fails the case when any shell command other than a single `android-design-preview` invocation ran (rejecting chains, pipes, subshells, and other binaries); Codex reads files through the shell and edits through `apply_patch`, so its audit allows single `android-design-preview` invocations plus read-only commands (`cat`, `head`, `tail`, `nl`, `ls`, `rg`, `grep`, `wc`, and `sed` without `-i`), alone or piped into each other, and fails the case on interpreters, output redirection, `tee`, `sed -i`, command substitution or subshells, or chains containing a non-allowed command. The ban is enforced after the fact there rather than prevented, which is enough for an eval.

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

### The loop

```text
person saves or agent edits  ──►  file watcher (server)
                                        │ content hash = source + DESIGN.md + JAR version
                                        ▼
                         ┌──────── result store (server) ──────┐
agent: check ──────────► │ hit: same hash and scope already    │──► JSON line + PNG paths
  (scoped or full)       │      done or in flight, reuse it    │
                         │ miss: enqueue one render job        │
                         └──────────────┬──────────────────────┘
                                        ▼
                     render queue, one view per job, by priority: agent request,
                     board focus, board remainder (cancelled when a newer save lands)
```

**One result per save.** The server keys every result by content hash and scope (direction, screen, view). When `check` asks for a scope that a finished or in-flight job already covers for the same hash, the server waits for that job and answers from it instead of running another. Compiled classes for the last hash stay loaded, so an unchanged file is never recompiled. Every render job covers exactly one view (one direction, screen, and view, about 0.1 to 0.35 s). A Skiko render cannot be interrupted once it starts, so this granularity is what lets a newer save or an agent request take over within one view's render instead of waiting behind a whole board. A newer save cancels the queued remainder jobs for the older hash.

**Scoped iteration, full check as the gate.** While iterating, the skill runs `check` with the one direction and view it just changed: about one compile plus one render (0.45 to 0.55 s in the fit test) instead of a full board. The unscoped `check` runs on the first draft and once more before showing the person. The cap of two fix rounds applies to full checks; scoped iterations between them are not capped.

**Images during iteration.** Every check writes PNGs and returns their paths; the agent opens them by path, as with Prevue, so an image costs context only when the agent chooses to look. A scoped check always returns its one image path, whether findings pass or fail, rendered at 1x density: about a quarter of the pixels, and so roughly a quarter of the image tokens, of the 2x renders the board and the gate use. A failing full check returns the paths for at most two failing views, skipping renders identical to one already returned, so the agent sees the foldable or desktop problem rather than a passing light view without a five-image response. A passing full check returns `light` and `desktop` (`watch` and `watch-large` on Wear) for the final visual inspection. The skill tells the agent to open the image after any visual change and before saying something is fixed, which also catches pixel-only render bugs that `tree` cannot show.

### Browser board

The server serves an HTML, CSS, and SVG board at `http://127.0.0.1:<port>/?view=<token>` and opens it in the default browser on interactive runs:

- **Focus first, then the rest.** The board reports which direction and view the person has open. On save, the server renders that one first and pushes it over Server-Sent Events, then renders the remaining views at low priority and pushes them as they finish. Views not yet redrawn are marked stale instead of showing an old render as current. With no board open, the agent's most recent scope counts as the focus.
- **Multi-direction board and ~40 ms switches.** Shows multiple `direction(...)` blocks side by side or focuses one direction with Brightness, Seed Color, Style Variant, Screen, Overlay, and Device controls. Because `<slug>.explore.kt` stays compiled, changing theme or device settings re-renders in ~40 ms without recompiling.
- **Live file watching.** The server watches `DESIGN.md` and `<slug>.explore.kt`. Watcher runs feed the shared result store, so a save the board already rendered is free for the agent's next scoped check.
- **Fixed views, no free resize or live input.** The board keeps v4's catalog views; `foldable`, `tablet`, and `desktop` already show what each breakpoint reveals, so v5 adds no width slider. Renders stay static images, with no taps or scrolling on a live composition.
- **Click-to-source SVG overlay.** Maps `CompositionGroup` source locations and `SemanticsNode` bounds onto each 2x PNG: hovering shows the Composable name, `<file>:<line>`, and measured dp dimensions. Clicking selects that node, copies `<slug>.explore.kt:<line>` to the clipboard, and records it as the person's selection. Clicking navigation or overlay triggers still switches the board state.

### Selection reaches the agent

The server stores the last clicked node per file: `<file>:<line>`, Composable call name, ancestor call chain, direction, screen, view, and dp bounds. `selection <file>` returns it, and every `check` response carries a `selection` field when the person has clicked since the agent's previous command. Each selection is tagged with the content hash of the file it was clicked on. When the file changes, the server re-resolves the selection on the next render of that view by its call chain and source text: a unique match updates `file:line`, and anything else marks the selection `stale: true` while keeping the call name and chain, so an edit that shifts lines never points the agent at the wrong composable. The skill tells the agent to read the selection first whenever the person says "this", "here", or similar, and to ask the person to click the part they mean when nothing is selected, without guessing from the source.

### Slot inspection and finding rules

Both `android-design` and `android-design-explore` speak Jetpack Compose Kotlin, backed by a compact ~2-page Material 3 Expressive `1.5.0` and Wear M3 signature sheet generated from the verified dependency jars so agents do not guess beta parameter names.

`check` runs source token lint (rejecting raw `Color(...)` hex literals outside `designBoard` and `.copy(alpha = ...)`), compiles in memory, and checks every requested screen and overlay across the requested catalog views (`light`, `dark`, `foldable`, `tablet`, `desktop`, or `watch` and `watch-large` on Wear; all of them when unscoped).

**Slot-table and semantics extraction.** `androidx.compose.runtime:runtime-desktop:1.13.0-beta01` includes `LocalInspectionTables` and `parseSourceInformation` (without adding `ui-tooling-data`), annotated `@ComposeToolingApi` alongside `Composer.collectParameterInformation()`. Because `LocalInspectionTables` is bound when a subcomposition starts, `HeadlessRenderer` wraps the target composable inside a 6-line `SubcomposeLayout` under `CompositionLocalProvider(LocalInspectionTables provides tables)`, pairing `parseSourceInformation` (`C<line>@<offset>L<length>`) with placed `LayoutInfo` boxes and `SemanticsNode` trees to recover `<file>:<line>`, the Composable call identifier at `source[offset]`, unclipped `LayoutInfo` dimensions, and `ModifierInfo` entries. Because `@ComposeToolingApi` carries no cross-release stability guarantee, Compose versions remain pinned in `app/gradle/libs.versions.toml` and a dedicated canary unit test asserts that call names, line numbers, and bounds resolve on a fixture screen so upgrades fail loudly if the format changes. On Wear views, `HeadlessRenderer` scrolls the root vertical scroller via `SemanticsProperties.VerticalScrollAxisRange` and `SemanticsActions.ScrollBy` half a viewport at a time and merges nodes through `ScrolledTree.merge`.

Mapping of all 21 check functions in `FindingRules.kt`:

| Mechanism | Checks |
|---|---|
| **`SemanticsNode` + `GetTextLayoutResult` + slot call names (15 checks)** | `touchTargets` (48 dp `touchBoundsInRoot`), `clippedText`, `undrawnText`, `truncatedText` (info), `truncatedLabels` (buttons, chips, tabs, nav items), `primaryLargest` (`testTag("ad:primary")` vs action call names per pane), `oneFab`, `offscreen`, `overflow` (unclipped `LayoutInfo.width` vs parent/view, exempting `HorizontalScrollAxisRange`), `lineLength` (`Card`, `ListItem`, and single- or multi-line `Text` > 60 measured chars), `carouselItems` (info), `dockedToolbarWithNavBar`, `fullScreenDatePicker` (info), and `emptyDetailPane` (info, from `listDetail` when `placeholder` is omitted) |
| **`LayoutInfo.getModifierInfo()` + Wear/carousel geometry (3 checks)** | `weightCollapsed` (weighted non-`Spacer` node measuring `<= 0.5 dp`), `cornerClipped` (rounded `Shape` clip radius from `GraphicsLayer` modifiers vs text ink and control bounds), `roundEdgeClipped`, and `croppedByCarouselItem` |
| **`designBoard` + `TextLayoutResult` + bytecode/source guard (2 checks)** | `fonts` (reads resolved `ProposalFonts` family and `fontSize` on `TextLayoutResult.layoutInput.style`; unbundled `FontFamily` constructors and raw `Color(...)` / `.copy(alpha = ...)` rejected) |
| **2 px perimeter Skia bitmap sampling (1 check, upgraded)** | `contrast`: reads exact sRGB foreground luminance $L_{\text{fg}}$ from `TextLayoutResult.layoutInput.style.color`, samples the 2 px outer perimeter band around each drawn line's ink box (zero glyph anti-aliasing pollution, including white-on-white), and checks WCAG ratio at the 10th-worst background luminance percentile (`p90` when $L_{\text{fg}} > 0.179$, `p10` otherwise), catching low contrast over photos and gradients |
| **Dropped (1 info note)** | `arrangementNoSpace`: omitted because inline `Row` and `Column` calls do not retain their `Arrangement` instance on `LayoutInfo` |

### Plugin bundle and moving to production

Distributable files live under `plugin/`: `plugin/skills/`, `plugin/bin/` (the bootstrap scripts), and each harness's manifest (Antigravity `plugin.json`, Claude Code `.claude-plugin/`, Codex `.codex-plugin/`), with no MCP configuration. The JAR stays out of the plugin and is fetched on first use, so installs stay small. Root marketplace manifests point to `./plugin` via git-subdir, while `npx skills add` and Chai (`~/chai.toml`) read `plugin/skills/` directly. The skills call `android-design-preview` by name, so the one setup requirement is that it is on `PATH`. Claude Code puts a plugin's `bin/` on `PATH` (Prevue relies on the same). Antigravity, Codex, `npx skills`, and Chai installs run `android-design-preview install` once from the plugin or repository checkout, which links the bootstrap script into `~/.local/bin`; when the command is missing, the skill gives the person that one step. Codex runs shell commands in its workspace-write sandbox, so it also needs `sandbox_workspace_write.network_access = true` (localhost and the first JAR download) and `~/.cache/android-design-preview` in `writable_roots`, the same requirement Prevue has for `~/.prevue`. That setting opens outbound network to every shell command Codex runs, not only this one, which is a real cost over v4's MCP server that needed no Codex configuration; the skill states it when the command fails in Codex rather than changing it unasked. Antigravity runs terminal commands unsandboxed by default.

Approving a direction needs no code generator, though porting still wires the screen into the app: the agent copies the approved `@Composable` screen functions into the target project's module layout, removes `testTag("ad:primary")` if undesired, connects them to the app's theme, ViewModel state, and Navigation 3 routes, and verifies the live screens with `kakao/prevue` on macOS or `compose-preview` elsewhere.

## Rollout gate

v5 replaces v4 only after the eight-case A/B in `evals/run.py` meets v4's bar (at least 6/8 rubric passes, every deterministic check passing, and 0 median compile retries) and a multi-turn revision case shows v5 using no more tokens and no more wall time per turn than v4. The multi-turn case matters because a brainstorm runs ten or more revisions: every image the agent opens stays in its context for later turns, and the tool time v5 saves (about 6.5 s per edit) is small next to model time across a long session. Wall time matters too because v4 returned images inline, while v5's image paths cost the agent a separate read, one more model round trip each time it looks.

### Rollout gate results

Both gates passed on `#126`'s branch with the trimmed `android-design/SKILL.md` (`24,584` bytes vs v4's `40,738` bytes, `-39.7%`) and shrunk `android-design-explore/SKILL.md` (`69` lines).

**Eight-case A/B (`evals/run.py`, Antigravity `Gemini 3.8 Flash (High)` generator and judge):**

| Metric | v4 baseline (Run 2, tuned) | v5 (`96658ea`) | Gate |
| --- | --- | --- | --- |
| Cases passed (rubric) | `6/8` (`80/82` items) | **`7/8`** (`81/82` items) | Pass (`>= 6/8`) |
| Deterministic code checks | `21/21` (100%) | **`21/21`** (100%) | Pass (100%) |
| Median compile retries | `0` | **`0`** (`0, 0, 1, 0, 1, 0, 2, 0`) | Pass (`0`) |
| Median agent turns | `35` | `35` | |
| Median input tokens | `257,128.5` | **`150,382`** (`-41.5%`) | |
| Median output tokens | `67,142` | **`44,664.5`** (`-33.5%`) | |
| Median check calls | `5.5` | `8.5` | |
| Median wall time | `317.2 s` | `448.9 s` | |
| Median `skill_bytes_read` | n/a | `112,236 B` (`51,659 B` to `89,030 B` on Wear) | |

All three Wear cases (`workout`, `player`, `reminder`) passed `9/9`, and four of five phone cases (`music`, `settings`, `trek`, `recipes`) passed `11/11`; `finance` passed `10/11` (`R-finance-categories`).

**Ten-turn multi-turn revision case (`evals/multiturn.py`, Claude `claude-fable-5-2`):**

| Metric | v4 baseline (`304ffe4`) | v5 (`3b1294c`) | Delta / Gate |
| --- | --- | --- | --- |
| Final deterministic checks | `3/3` pass | **`3/3`** pass | Pass |
| Mean total tokens / turn | `385,918.5` | **`275,745.3`** | `-28.5%` (Pass) |
| Median total tokens / turn | `387,757.5` | **`268,044.0`** | `-30.9%` (Pass) |
| Mean wall time / turn | `33.75 s` (`337.46 s` total) | **`33.72 s`** (`337.20 s` total) | `-0.1%` (Pass) |
| Median wall time / turn | `29.45 s` | **`26.57 s`** | `-9.8%` (Pass) |
| Total cost (mean / median per turn) | `$5.9603` (`$0.5960` / `$0.4213`) | **`$3.6292`** (`$0.3629` / `$0.2113`) | `-39.1%` mean / `-49.8%` median |
| Total check calls | `13` (`1.3` / turn) | **`10`** (`1.0` / turn) | `-23.1%` |
| Compile retries | `0` | **`0`** | Pass |
| `skill_bytes_read` | `71,245 B` (`4` files) | **`42,315 B`** (`3` files) | `-40.6%` |

Diagnostic fixes that closed the multi-turn gate during `#126`: granting Claude generators `--add-dir ~/.cache/android-design-preview` and `Read` so path-based PNG reads succeed, removing the duplicate top-level `layout` field from `CheckRenderSummary` (keeping unscoped `check` JSON under Claude's 10 KB truncation threshold), skipping `FileWatcher` background compiles when zero browser SSE clients are connected (preventing intermediate `Edit` saves from exhausting the 25-compile recycle budget mid-session), deduplicating pixel-identical passing images by SHA-256, and instructing scoped iteration to run one check and open one 1x PNG per revision.

## Links

- [Visual design exploration v4](../archive/visual-design-exploration-v4.md): archived; the sandbox, kit, and checks this design keeps, its rollout gate results, and the Node MCP loop it replaces
- [Visual design exploration v3](../archive/visual-design-exploration-v3.md): archived JSON-interpreted macOS previewer design superseded by v4
- [Wear OS in the previewer](wear-os-previewer.md): the upstream `androidx.wear.compose` desktop port reused on the server classpath
- [Script ban eval baseline](script-ban-eval.md): the 8-case v3 baseline and the eval generators' tool restrictions
- [`kakao/prevue`](https://github.com/kakao/prevue): browser-based `@Preview` hot-swap tool whose CLI, server, and loop this design adopts, used on macOS when wiring approved screens into an Android app
