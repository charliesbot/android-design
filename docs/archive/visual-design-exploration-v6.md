# Visual design exploration v6

> **Archived:** Superseded by [Visual design exploration v7](../design/visual-design-exploration-v7.md). v7 keeps v6's host server, compiler pipeline, browser board, and CLI while replacing offscreen emulator rendering with a device-free layoutlib worker and keeping `LivePreviewActivity` as opt-in `open --live`.

**Date:** 2026-11-22

## TL;DR

v6 keeps v5's host server (`PreviewHttpServer`, `RenderQueue`, `SelectionStore`, `FindingRules`), browser control board, and 0.05 s Kotlin CLI contract. It replaces the Compose Multiplatform Skiko renderer, the Wear Compose desktop port, and the released fat JAR with a zero-chrome Android Host APK running real AndroidX Material 3 Expressive and Wear Compose on an emulator or physical device.

A two-module Gradle project under `app/` runs once at bootstrap (`--no-daemon`) to build a non-debuggable, unminified Host APK (`host-preview.apk`), bundle AndroidX compile jars, and emit `@server.cp` and `@client.cp` argfiles. After bootstrap, Gradle exits and plain `java` runs one shared warm compiler server (`kotlinc`, Compose compiler, in-process `D8`) and the CLI with zero Gradle daemon tax. Evals and `android-design-preview render` route through the same Android Host APK server (`POST /api/render`), while `android-design-preview start` delegates AVD lifecycle and APK installation to the `android` CLI. (The Robolectric preview backend was retired after v0.6.0; Robolectric remains only for `:host` JUnit tests.)

## Design

### Why v6

v5 ([Visual design exploration v5](visual-design-exploration-v5.md)) made Compose exploration fast and deterministic on desktop Skiko, but three structural ceilings remain:

1. **Static PNGs only:** Explorations cannot respond to taps, scrolling, dragged bottom sheets, Navigation 3 transitions, IME insets, predictive back, Material 3 Expressive springs, or live testing on a physical phone or watch.
2. **Upstream Compose Multiplatform lag and the Wear desktop port:** Desktop `material3` tracks AndroidX releases on JetBrains' schedule (such as [#153](https://github.com/charliesbot/android-design/issues/153)). Wear support also depends on ~77.5k lines in `app/wear/` (~75k byte-identical upstream plus ~2.1k local shims, host glue, and tests), while still carrying excluded upstream components and curved-text and rotary gaps.
3. **Four-target Skiko release matrix:** v5 packages four OS and CPU Skiko `.tar.xz` archives via `package_release.py` and GitHub Releases to distribute desktop native binaries across macOS and Linux.

This repository optimizes for an Android developer workflow (`AGENTS.md`), so having the Android SDK, the `android` CLI, and an emulator (or USB device) available is an acceptable baseline prerequisite for v6.

### Architecture overview

```text
agent shell / person
  │  android-design-preview <command> <file> ...       (sh entrypoint -> java @client.cp, ~0.05 s)
  ▼
Host Kotlin server  (java @server.cp, one shared warm JVM per machine, exits when idle, 0 MB Gradle daemon)
  ├── PreviewHttpServer, RenderQueue (repo-tagged), SelectionStore, Watchdog, browser control board (HTML/SVG/SSE)
  ├── FileWatcher (speculative pre-compile on save) + SHA-256 asset server (design/assets/*)
  ├── SourceTokenLinter -> warm K2JVMCompiler + Compose compiler -> BytecodeGuard -> in-memory D8
  └── Host PNG encoder (decodes selected LZ4 raw frames from device into local PNG files)
  │  ▲
  │  │  persistent TCP keep-alive over `adb forward`
  ▼  │
Zero-chrome Android Host APK  (host-preview.apk: :sandbox worker + ForegroundService(specialUse) + back-stacked HostRenderActivity)
  ├── InMemoryDexClassLoader(ByteBuffer) + key(dexHash) composition isolation
  ├── Real AndroidX M3 Expressive + native androidx.wear.compose AARs + bundled fonts + DesignBoard kit
  ├── Virtual BroadcastFrameClock Recomposer + Display 0 back-stacked HardwareRenderer compositor
  └── SlotTreeExtractor (LocalInspectionTables, RootForTest.semanticsOwner) + LUT PixelContrastSampler + all FindingRules
```

### Gradle as bootstrap, not compiler

One self-contained Gradle project (`:host` Android application module, `:app` plain JVM server/CLI module, and `:core:*`) stays under `app/` at the repository root. Because everything resolves from one Gradle graph, the Host APK runtime and the `kotlinc` compile classpath share structurally identical dependency versions.

- **Repo layout compliance (`AGENTS.md`):** Keeping `:host` and `:server` under `app/` keeps `plugin/skills/` restricted to skill content and the thin `plugin/skills/android-design-explore/scripts/android-design-preview` bootstrap script. Chai and `npx skills` installs stay free of build source trees.
- **Versioning and publishing:** Bumping `plugin/plugin.json` (and matching `VERSION` in the manifests and bootstrap script) on `main` triggers a trimmed `.github/workflows/release-previewer.yml` that creates the lightweight `v<version>` git tag, while `package_release.py` and binary release uploads go away.
- **SDK and Android regex compatibility:** Compose UI `1.13.0` and Wear Compose `1.8.0-alpha03` require `compileSdk = 37` with `minorApiLevel = 1` (`android-37.1`) in `:host`. Shared domain regexes running inside ART's ICU engine must escape literal closing braces (for example `DesignSystemResolver.kt:50`, where `}$` throws `PatternSyntaxException` unless written `\\}$`).
- **On cache miss (first run or version change):**
  1. `plugin/skills/android-design-explore/scripts/android-design-preview` fetches `app/` via a shallow sparse checkout at tag `v<version>` into `~/.cache/android-design-preview/<version>/src/` (or reads an `ANDROID_DESIGN_PREVIEW_SRC` local checkout override during development).
  2. Bootstrap runs Gradle once (`--no-daemon`) to build `host-preview.apk` from the `previewRelease` build type (`isDebuggable = false`, `isMinifyEnabled = false`, signed with the local debug keystore).
  3. Gradle resolves `:host`'s `previewReleaseCompileClasspath` via AGP's `android-classes-jar` artifact view and merges the AndroidX and kit `classes.jar` files into one uncompressed (`ZIP_STORED`) `androidx-compile-bundle.jar` (`63` jars, `14,103` class entries, `83.0 MiB` in spike). Merging preserves `META-INF/*.kotlin_module` (`59` entries) and `kotlin/**/*.kotlin_builtins` (`8` entries) for Kotlin top-level and extension lookup while avoiding opening dozens of compressed jars per edit.
  4. Gradle records `android.jar` and `$ANDROID_HOME/build-tools/*/lib/d8.jar`, writes `@server.cp` and `@client.cp` argfiles pointing into `~/.gradle/caches/`, and exits completely.
- **On cache hit:** `android-design-preview` launches `java @client.cp` and `java @server.cp` directly with zero Gradle invocation and zero Gradle daemon RAM. If Gradle's periodic cache cleanup evicts any referenced artifact, the launcher detects missing paths and re-runs bootstrap automatically.
- **Escape hatch:** Keeping `:host` and `:server` cleanly separated under `app/` lets us package a single OS-independent Slim Universal Bundle later without architectural rework.

### Host server, CLI, browser board, and edit loop

The host control plane carries over from v5 with minimal surface change: `PreviewHttpServer`, `RenderQueue`, `SelectionStore`, `FindingRules`, the browser board (multi-direction comparisons, controls for direction, screen, overlay, light or dark brightness, seed color, palette variant, and font scale, plus clickable SVG box selection over returned screenshots), and the 0.05 s Kotlin CLI (`java @client.cp` using `HttpURLConnection`, avoiding a Python rewrite).

- **One active host server and APK per machine:** One warm server manages the single `adb forward` tunnel per active device serial and pairs with one installed `host-preview.apk` (no per-version servers and no separate dev `applicationId`). Sharing one server prevents duplicate server instances, port collisions, and competing device connections.
- **Version mismatch replacement and displaced checks:** When a client connects with a different version or local dev checkout (`ANDROID_DESIGN_PREVIEW_SRC`), it stops the running server, starts its own version, and reinstalls the matching `host-preview.apk`. Any in-flight `check` interrupted by that replacement fails immediately with `replaced by v<x>, rerun` instead of hanging or timing out.
- **Auth and board tokens:** Keep one shared `clientSecret` (`0600`) in the server record for local CLI calls and one shared `viewToken` for the browser board, matching v5. `?repo=<id>` on `/?repo=<id>&view=<token>` selects which repository is active on the board rather than acting as a per-repo security boundary.
- **Cross-repo queue ordering and device ownership:** Offscreen `RenderQueue` jobs from different repositories serialize cleanly through the shared server tagged by `(repoRoot, dexHash)`, so background checks never overwrite another repository's results. Live visible Activity ownership belongs to whichever repository last ran `check` or `open`, or whichever repository is selected on the browser board.
- **Device lifecycle, owned by the server:** the preview server does the only device setup, whether `android-design-preview start` or `check` started it (the CLI just validates `--serial` and `--avd` first). It boots an emulator if needed through the SDK `emulator` binary with `-gpu host -no-snapshot-save` (`android emulator start --headless` forces software rendering, which recent system images never finish booting on, so the `android` CLI is only the fallback when the binary is missing). It owns an emulator only when the serial that comes online runs the AVD it launched while its launched process is still alive. It shuts that emulator down when setup fails, when it stops or idles out, and when the CLI times out waiting for it (the CLI sends SIGTERM before forcing a kill). Devices that were already running, or that won a boot race, are left alone. Ownership does not pass to a replacement server: after a version replacement or a deadline respawn, an emulator the old server booted keeps running until the user stops it. It also installs `host-preview.apk`, runs one-time AOT compilation (`adb shell cmd package compile -m speed -f <pkg>`), applies background idle exemptions, and establishes `adb forward`. Because `android emulator create --list-profiles` offers no `resizable` profile (only phone, foldable, tablet, and desktop profiles), `android-design-preview start` reuses an existing compatible AVD (preferring a `resizable` AVD created in Android Studio or with `avdmanager` if present) and otherwise creates `medium_phone` with `android emulator create`. The resizable emulator toolbar (API 34+) is optional for sizing the live interactive view and is never resized or required for offscreen checks.
- **Why the server stays on the host computer:**
  1. Speculative pre-compile needs a host process watching `.explore.kt` and `DESIGN.md` directly.
  2. `check` returns local host PNG file paths so the agent's file-reading tool opens them without cross-device pull logic in the CLI.
  3. Host Apple Silicon CPU encodes 2x and 4K desktop PNGs faster than an emulator (`29.8 ms` at 4K with `Deflater.BEST_SPEED` vs `163.6 ms` with `ImageIO`).
  4. The browser board stays alive if Android recycles or kills `:sandbox`.
- **Speculative pre-compile on save:** v5 (`PreviewHttpServer.kt`) skipped `FileWatcher` compiles whenever `sseClients.isEmpty()` to avoid exhausting `MAX_COMPILES_BEFORE_RECYCLE = 25` caused by Skiko and ClassLoader retention. Removing Skiko from the host JVM lets `FileWatcher` debounce-compile and pre-push `.dex` bytecode on save during the agent's thinking gap so `check` usually hits an already-loaded DEX.
- **Device asset boundary:** Rendered PNG outputs stay on the host. Files referenced by `designPainter("assets/...")` under `design/assets/` are served by the host server over `adb forward` keyed by SHA-256 and cached on-device.

### Zero-chrome Host APK and offscreen rendering

- **Zero controls on device:** Controls remain on the browser board. The device displays unobstructed, full-bleed UI with authentic window size classes, system bar insets, and 204 or 227 dp Wear frames.
- **One unified APK and live Wear scope:** `androidx.compose.material3` and `androidx.wear.compose.material3` live in disjoint packages, so one Host APK (`host-preview.apk`) serves phone, foldable, tablet, desktop, and Wear (keeping `android.hardware.type.watch` `required="false"` in the manifest). A Phone AVD handles offscreen Wear `check` renders through `ContextThemeWrapper.applyOverrideConfiguration` (`SCREENLAYOUT_ROUND_YES`), while live interactive Wear use (real round display and rotary crown input) requires a Wear OS emulator or physical watch.
- **Dynamic DEX loading and state:** Compiled `.dex` bytes stream over keep-alive TCP and load via `InMemoryDexClassLoader(ByteBuffer)` (no writable DEX files on disk, safe under Android 14+ dynamic code loading restrictions). Wrapping the loaded root in `key(dexHash)` disposes old SlotTables and classloaders cleanly on every edit, while hoisting shell state outside `key(dexHash)` preserves controls across edits without `rememberSaveable` slot-key collisions or cross-classloader `ClassCastException`s.
- **Non-debuggable AOT host runtime and `sourceInformation`:** Build `host-preview.apk` from `previewRelease` (`isDebuggable = false`, `isMinifyEnabled = false`) and run `cmd package compile -m speed` once on install. Keeping `isDebuggable = false` avoids debuggable Compose overhead and preserves ART background verification of in-memory DEX, while `isMinifyEnabled = false` keeps upstream `sourceInformation` intact alongside `D8`-preserved exploration literals.
- **Virtual frame clock settling:** v5 (`SandboxHeadlessRenderer.kt`) settles animations via a virtual test clock (`settle(1500L)`), whereas stock Android `Choreographer` ticks in real time and would stall 1.5 s per view. Injecting a custom `Recomposer` backed by `BroadcastFrameClock` (`AndroidUiDispatcher.Main + clock` on both the `Recomposer` and its launch `CoroutineScope`), advancing `virtualTimeNanos` monotonically across targets, and running a two-turn `settleVirtualClock` (`minSteps = 45` for overlays) settles animations and modal transitions in milliseconds.
- **Unified offscreen capture, contrast LUT, and lazy PNGs:** All views, including `AlertDialog`, `ModalBottomSheet`, and `DropdownMenu` popups, render offscreen through one persistent, opaque, back-stacked `HostRenderActivity` (`moveTaskToBack(true)` posted after `decorView` attachment, without `windowIsTranslucent`, `noHistory`, or `excludeFromRecents`) combined with a multi-window `HardwareRenderer` + `ImageReader(USAGE_CPU_READ_OFTEN)` compositor over `WindowInspector.getGlobalWindowViews()` (no `VirtualDisplay` fallback required). Each view uses a per-`(view.name, density)` `ComposeView(overrideCtx)` whose root consumes Display 0 physical system bar insets (`WindowInsetsCompat.CONSUMED`) so host AVD chrome never shifts offscreen layouts across views. Because desktop 4K 2x GPU readback takes only `20.04 ms` and checking all 15 views at 2x is faster than running 15 checks at 1x plus 6 separate 2x re-renders, checks render directly at 2x (`PERIMETER_PX = 2`, 1 dp at 2x). `PixelContrastSampler.kt` replaces per-pixel `"#%02X%02X%02X".format(r, g, b)` and hex parsing with a 256-entry sRGB LUT over raw pixel bytes. Contrast and all `FindingRules` run in memory on every view; unselected frames are discarded immediately, and only the 1 to 6 PNGs `check` returns cross `adb forward` as LZ4-compressed raw pixels for host-side `Deflater.BEST_SPEED` PNG encoding (while browser cards use on-demand `WEBP_LOSSLESS` at quality 0).
- **Slot inspection and Wear OS:** `SlotTreeExtractor` runs on `Dispatchers.Main` (preventing `SlotTable` concurrent modification), reads semantics through `RootForTest.semanticsOwner`, and extends Skiko layer matching (`Dialog.skiko.kt`, `Popup.skiko.kt`) with Android counterparts (`AndroidAlertDialog.android.kt`, `AndroidDialog.android.kt`, `AndroidPopup.android.kt`, `ModalBottomSheet.android.kt`). Native Wear views apply `ContextThemeWrapper.applyOverrideConfiguration` (`SCREENLAYOUT_ROUND_YES`, 204/227 dp, dark mode), bundled `roboto_flex.ttf`, MaterialKolor Wear color role mapping (bypassing phone `Settings.Global` dynamic-color suppression), and `ScrolledTree.merge` driven by `SemanticsActions.ScrollBy` while skipping infinite circular `DatePicker` pickers during auto-scroll.
- **Process isolation:** Rendering runs inside an isolated `:sandbox` process backed by a `ForegroundService(specialUse)` and ADB idle exemptions. Across 50 consecutive `InMemoryDexClassLoader` reloads in the spike, `:sandbox` native heap grew only `+0.1 MB` (`20.2 -> 20.3 MB`) and total PSS stayed flat (`138.1 -> 137.6 MB`), so a generous safety-net recycle threshold of `200` reloads or `> 256 MB` native heap / `> 512 MB` PSS (plus watchdog timeouts on composition loops) recycles `:sandbox` without churn and without interrupting the host server or browser board.

### Evals and `render` on the Android Host APK (Robolectric backend retired)

Evals (`evals/run.py`, `evals/multiturn.py`) and `android-design-preview render` run through the same shared Android Host APK server (`start --headless` + `POST /api/render`) rather than a separate Robolectric preview backend. On Apple Silicon, warm emulator rendering with `-gpu host` matches or beats Robolectric Native Graphics CPU rasterization (781 ms vs 913 ms median on full 15-view checks) while running real Android 37 shaders such as Wear `Modifier.curveToEdge()` and removing ~3,500 lines of duplicate backend code. `FrameCapture` (`HardwareFrameRenderer`) renders through `HardwareRenderer` and `ImageReader` so `RenderEffect`s such as `Modifier.blur` and AGSL `BlurRadiusSpec.shader` masks draw. Robolectric (`4.18.1`, `@Config(sdk = [35])`) remains only as a Gradle `testImplementation` for `:host` JVM unit tests (`ComposeBoardRendererTest`, `DesignSystemResolverIcuTest`).

Because `.explore.kt` executes inside `:sandbox` on Android, `BytecodeGuard` and `SourceTokenLinter` stay active in the host compiler pipeline to enforce deterministic authoring rules (rejecting raw `Color(...)`, `.copy(alpha = ...)`, and unbundled `FontFamily` calls).

### Rollout gate (historical v0.6.0 baseline)

v6 replaces v5 only after passing both speed and eval parity gates on Apple Silicon:

- **Speed gate:** Warm 1-view scoped `check` `<= 0.5 s` and full 15-view unscoped `check` `<= 2.8 s`.
- **Eval parity:** Match v5's 8-case A/B bar (`evals/run.py`) with no regression in multi-turn tokens or wall time (`evals/multiturn.py`).
- **Cutover scope:** Once both gates pass, one cutover retires Skiko, `app/wear/`, binary release packaging, and desktop-only sandbox plumbing, and trims `.github/workflows/release-previewer.yml` to lightweight tag creation. It also moves `visual-design-exploration-v5.md` and `wear-os-previewer.md` to `docs/archive/`, updates `plugin/skills/android-design-explore/` prerequisites (Android SDK, `android` CLI, emulator or connected device, plus Codex setup guidance for outbound access to GitHub and Maven hosts and a writable `~/.gradle` alongside `~/.cache/android-design-preview`), and updates `AGENTS.md` layout notes.

### Rollout gate results

Rerun on the v0.6.0 fixes (watchdogs and liveness on the device worker, server supervision, Robolectric parity through the shared compiler and `SettlePolicy`, server-owned device lifecycle). Raw outputs are committed under `evals/results/v6/` (`speed.json`, `ab/`, `multiturn/`). Every eval ran in Claude Code; the earlier v5 and v6 A/B numbers came from `agy` (Gemini) and are not comparable, so this section judges the A/B run against the bar itself.

**Speed gate: passes.** Apple Silicon host, `Pixel_10_Pro_XL` headless emulator, `android` backend, uncached edits, every sample checked for the expected render count and a valid result (`evals/bench_speed.py`, commit `5de0b7e`):

| Benchmark | Target | Median | Min / Max |
| --- | --- | --- | --- |
| Warm 1-view scoped `check` (`n=7`) | `<= 500 ms` | **`423.9 ms`** | `411.3 ms` / `471.5 ms` |
| Full 15-view unscoped `check` (`n=7`, 3 directions x 5 views) | `<= 2800 ms` | **`765.9 ms`** | `712.9 ms` / `790.4 ms` |

**Eight-case A/B suite: fails the bar.** `evals/run.py`, `claude-opus-5-5` generator and judge (one judge run), Robolectric backend, one run of all 8 cases:

| Metric | Bar | Result |
| --- | --- | --- |
| Cases passing every rubric item | `6/8` | **`3/8`** (`trek`, `player`, `workout`); `93/102` items |
| Deterministic code checks | `100%` | **`18/21`** (`86%`): `music`, `workout`, `reminder` each fail `C-review` |
| Median compile retries | `0` | **`0.5`** |
| Median wall time / turns | | `174.6 s` / `25.5` |
| Median output tokens / check calls | | `16,748` / `6.5` |
| Median `skill_bytes_read` | | `63,625 B` |

Failures, by cause:

- **Previewer bug (`music` `C-review`).** The lyrics and queue sheets render inside the view, but the layout tree reports a padded `Column` (`fillMaxWidth().padding(start = 24.dp, ...)`) at its padded x (`24 dp`) with its unpadded width (`411 dp`), so it appears to end at `435 dp` in a `411 dp` view and trips `offscreen` and `overflow`. The child `Row` bounds are consistent. This is a tree-bounds bug, not an agent layout error.
- **Not yet triaged (`workout`, `reminder` `C-review`).** `workout` flags `contrast` on the watch end-confirm overlay; `reminder` flags `primary-largest` on its list on both watch sizes. The judge independently saw the `reminder` confirmation check mark as dark on dark, so at least that one looks like a real agent miss.
- **Rubric misses (agent output).** `R-font` on `music`, `settings`, and `recipes` (Fraunces at its default weight with no axis choice), `R-controls` and `R-music-actions` on `music` (three actions inside one tonal pill), `R-idea` on `music`, `R-finance-categories` on `finance` (one hue for every category), `R-semantics` on `settings`, and `R-wear-hero` on `reminder`.

**Ten-turn multi-turn revision case:** `evals/multiturn.py`, `claude-fable-5-2`, Robolectric backend, commit `2e679a8`, compared with v5 (`3b1294c`, `claude-fable-5-2`) from [Visual design exploration v5](../archive/visual-design-exploration-v5.md):

| Metric | v5 (`3b1294c`) | v6 | Change |
| --- | --- | --- | --- |
| Final deterministic checks | `3/3` | **`3/3`** | |
| Total wall time | `337.20 s` | `350.32 s` | `+3.9%` |
| Mean tokens / turn | `275,745.3` | `371,003.0` | `+34.5%` |
| Check calls / compile retries | `10` / `0` | `12` / `0` | |
| `skill_bytes_read` | `42,315 B` (`3` files) | `49,806 B` (`3` files) | `+17.7%` |

Both tokens and wall time regressed against v5, so the multi-turn case fails "no regression in tokens or wall time". Tokens regressed most, by about a third per turn.

### Rejected alternatives

- **Gradle incremental tasks on every edit (`:explore:dex`, Tooling API, or `--continuous`):** Pays irreducible launcher, configuration-cache, and input-snapshotting tax on every edit while pinning 2 to 3.3 GB of resident Gradle and Kotlin daemons; strictly slower and heavier than Gradle-as-bootstrap.
- **Reinstalling the APK per edit (`android run --use-delta-install`):** Takes multiple seconds per cycle, kills the active process, and resets device state on every edit.
- **Hosting the HTTP/SSE server inside the APK:** Breaks host `FileWatcher` speculative pre-compiles, complicates host PNG output paths, forces slower on-device 4K PNG encoding, and tears down the browser board when Android recycles or kills `:sandbox`.
- **Declarative JSON / A2UI data catalog:** Brings back v3's closed-catalog maintenance ceiling and gives up arbitrary Compose, custom `Canvas`, and direct copy-to-app porting.
- **Controls inside the APK:** Breaks authentic window size classes, crowds edge-to-edge bars, and does not fit 204 dp Wear screens.
- **Live Literals (`liveLiteralsV2`) and Android Studio Live Edit:** Live Literals carries deprecation risk after Live Edit superseded it, and Live Edit requires Android Studio GUI and a JVMTI proxy while `InMemoryDexClassLoader` already reloads entire `.explore.kt` leaf files including structural edits.
- **Existing preview CLIs (`android preview render`, `compose-preview` simulator):** Require project builds, rely on Layoutlib or multi-second reloads, and do not expose the `TextLayoutResult`, unclipped `LayoutInfo`, `GraphicsLayer` clip radius, and 2 px perimeter pixel data required by all `FindingRules`.
- **Shipping a Slim Universal Compiler JAR right now:** Matches Gradle-as-bootstrap's warm speed and offers faster first-run installs and simpler sandbox permissions, at the cost of keeping a release workflow. Deferred unless external distribution or sandbox friction later justifies packaging `:host` and `:server` into one OS-independent archive.

## Validation spike findings and open questions

Validation spike ([#159](https://github.com/charliesbot/android-design/issues/159)) ran against `Pixel_10_Pro_XL` (ARM64 API 37, `-gpu host`) on Apple Silicon (M4 Max, 36 GB RAM, OpenJDK 17.0.20, AGP 9.5.x, Kotlin 2.4.21, Compose BOM 2026.11.01 / UI 1.13.0, Material 3 1.5.0, Wear Compose 1.8.0-alpha03, Robolectric 4.18.1, `d8.jar` from build-tools 36.1.0), resolving Open Questions 1 through 4:

1. **Offscreen capture for overlays (Resolved):** One unified Display 0 path handles plain views and all overlays (`AlertDialog`, `ModalBottomSheet`, `DropdownMenu` / `Popup`). A persistent, opaque, back-stacked `HostRenderActivity` (without `windowIsTranslucent`, `noHistory`, or `excludeFromRecents`, posting `moveTaskToBack(true)` after `decorView` attachment) hosts per-`(view.name, density)` `ComposeView(overrideCtx)` windows and draws `WindowInspector.getGlobalWindowViews()` layers plus `FLAG_DIM_BEHIND` scrims into a reused `HardwareRenderer` + `ImageReader(USAGE_CPU_READ_OFTEN)`. Both rejected alternatives failed on real Android checks: `VirtualDisplay` + `HostRenderActivity` (`setLaunchDisplayId`) threw `ActivityTaskManager: Permission Denial` on untrusted virtual displays, and `VirtualDisplay` + `Service Presentation` threw `IllegalArgumentException: Window type mismatch` (`2030` vs `TYPE_APPLICATION` `2`) on Compose modals. Sequential plain, `AlertDialog` (`97.5 ms`, `10 nodes`), `ModalBottomSheet` (`176.9 ms`, `9 nodes`), `DropdownMenu` (`224.1 ms`, `9 nodes`), and plain-after (`74.1 ms`, `4 nodes`, `globalWindowCount=1` with zero leaked windows) all passed. Offscreen capture also succeeded undisturbed while `com.android.settings` stayed top-resumed on Display 0 (`379,353` non-black px across plain/dialog/sheet, `105.7 ms` plain) and while the screen was off (`mWakefulness=Asleep`, `379,353` non-black px, `111.6 ms` plain). Note: offscreen `ComposeView` roots must consume Display 0 physical `WindowInsets` (`WindowInsetsCompat.CONSUMED`) so host bar insets do not shift offscreen layouts across densities.
2. **Measured Apple Silicon warm edit latency, desktop readback, and memory (Resolved):**
   - **Warm latency (3-direction fixture):** Warm 1-view scoped `check` hit **`295.9 ms` median** (`n=7`, min `271.9`, max `362.7`) at `1x` (`compile 153.6`, `d8 37.7`, `dexLoad 4.6`, `composeSettle 36.0`, `gpuReadback 7.8`, `slotExtract 27.4`, `lz4 5.6`, `tcp+dev 116.2`, `lz4Dec 0.4`, `hostPng 2.7` ms) and **`312.2 ms` median** (`n=7`, min `304.1`, max `372.8`) at `2x` (`<= 500 ms` pass mark). Full 15-view unscoped `check` (3 directions x 5 views rendered at `2x` with 6 returned `2x` PNGs) hit **`2428.4 ms` median** (`n=5`, min `2277.8`, max `2559.8`; `compile 111.7`, `d8 25.9`, `dexLoad 2.9`, `deviceRender 2089.9`, `tcp+dev 2119.2`, `lz4Dec 38.1`, `hostPng 116.8` ms, `<= 2800 ms` pass mark), beating 15 checks at `1x` plus 6 separate `2x` re-renders (`2648.2 ms` median, 21 targets vs 15).
   - **Desktop GPU-to-CPU readback and PNG encode:** `HardwareRenderer` + `ImageReader` median `sync + readback` was `12.33 ms` at 1x (`1920x1080`: `3.69 ms` sync, `8.64 ms` readback) and `20.04 ms` at 2x (`3840x2160`: `4.21 ms` sync, `16.00 ms` readback), well below `50 ms`, so checks run directly at `2x` (`PERIMETER_PX = 2`, `1 dp` at 2x). Host `Deflater.BEST_SPEED` encoded a 1x desktop PNG in `9.03 ms` and a 2x 4K PNG in `29.78 ms` (vs `163.60 ms` via `ImageIO`).
   - **50-edit `:sandbox` memory and host RAM:** Across 50 consecutive forced `InMemoryDexClassLoader` reloads, `:sandbox` `nativeAllocMB` stayed flat (`20.2 -> 20.3 MB`, `+0.1 MB`), `totalPssMB` stayed in `137.3` to `144.4 MB` (`138.1 -> 137.6 MB`), and `javaUsedMB` stayed at `60.6 -> 60.1 MB` (`dumpsys meminfo`: `Views=8`, `ViewRootImpl=1`, `Activities=1`), while JIT warmed `compileMs` down to `93.3 ms` and `d8Ms` to `15.1 ms`. Combined host RSS during a warm harness run was `~1.48 GB` (`qemu-system-aarch64` `879.6 MiB`, `netsimd` + `crashpad_handler` `24.0 MiB`, warm compiler JVM `581.6 MiB`). Settled `:sandbox` recycle threshold: `200` reloads or `> 256 MB` native heap / `> 512 MB` PSS, plus watchdog timeouts.
3. **Native Wear M3 on Phone AVD and `sourceInformation` after `D8` (Resolved):** All six Wear M3 components (`TimeText`, `TransformingLazyColumn` + `SemanticsActions.ScrollBy`, `EdgeButton`, Wear `AlertDialog`, `DatePicker`, `Modifier.curveToEdge()`) rendered under `SCREENLAYOUT_ROUND_YES` at `204 dp` and `227 dp` (`2x`) on the Phone AVD, and `SlotTreeExtractor` recovered exact `<file>:<line>` sources (`wear.explore.kt:21..71`) after `D8`. Finding for `#160`: Wear `DatePicker` exposes an endless circular `Picker` `VerticalScrollAxisRange` that drove auto-scroll through all 60 iterations (`6114.8 ms`); `#160` skips `DatePicker` infinite pickers during Wear `ScrolledTree.merge`.
4. **Robolectric vs Emulator parity (Resolved with documented caveats):** Robolectric `4.18.1` runs on `@Config(sdk = [35])` under JDK 17 (SDK 36/37 require JDK 21). Unpacking frames via `bitmap.getPixels(...)` into `[R, G, B, A]` avoids host Skia `copyPixelsToBuffer` BGRA byte order. On matched nodes, Robolectric matched the emulator on 100% of 2 px perimeter hex colors (`23/23` Phone 1x, `22/22` Phone 2x, `13/13` Wear main) and width (`maxWDelta = 0.00 dp`); height deltas (`115.50 dp` at 2x, `231.00 dp` at 1x) came from Display 0 physical `WindowInsets` on the emulator, confirming the need to consume Display 0 insets offscreen. Google Sans Flex variable axes (`wght` `300/700`, `opsz` `14/72`, `ROND` `0/100`) matched emulator string widths (`229.0 / 243.0 / 213.0 / 229.0 px`, `diffPx = 4309 / 3462 / 326`). Confirmed AGSL shader caveat: `Modifier.curveToEdge()` renders curved text along the bezel on Android 37 emulator (`2` nodes) whereas Robolectric API 35 Native Graphics drops the AGSL radial curve transform and draws flat left-clipped text (`990` non-black px at `(73,30)..(177,54)`).
5. **Optional Slim Universal Bundle packaging (Open):** Revisit bundling `:host` and `:server` into one OS-independent release archive only if Codex sandboxes or external installs cannot tolerate the first-run Gradle bootstrap.

## Links

- [Visual design exploration v5](../archive/visual-design-exploration-v5.md)
- [Wear OS in the previewer](../archive/wear-os-previewer.md)
- [Visual design exploration v4](../archive/visual-design-exploration-v4.md)
- [Visual design exploration v3](../archive/visual-design-exploration-v3.md)
- [Script ban eval baseline](../design/script-ban-eval.md)
