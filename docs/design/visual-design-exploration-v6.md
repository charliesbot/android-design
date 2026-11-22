# Visual design exploration v6

**Date:** 2026-11-22

## TL;DR

v6 keeps v5's host server (`PreviewHttpServer`, `RenderQueue`, `SelectionStore`, `FindingRules`), browser control board, and 0.05 s Kotlin CLI contract, while replacing the Compose Multiplatform Skiko desktop renderer, the ~77.5k-line Wear Compose desktop port, and the released fat JAR with a zero-chrome Android Host APK running real AndroidX Material 3 Expressive and Wear Compose on an emulator or physical device. A two-module Gradle project under `app/` runs once at bootstrap (`--no-daemon`) to build a non-debuggable, unminified Host APK (`host-preview.apk`), bundle AndroidX compile jars, and emit `@server.cp`, `@client.cp`, and `@robolectric.cp` classpath argfiles; after bootstrap, Gradle exits and plain `java` runs one shared warm compiler server (`kotlinc`, Compose compiler, in-process `D8`) and the CLI with zero Gradle daemon tax. Headless evals run against the same Gradle-locked AndroidX classpath on Robolectric, and `android-design-preview start` delegates AVD lifecycle and APK installation to the `android` CLI.

## Design

### Why v6

v5 ([Visual design exploration v5](visual-design-exploration-v5.md)) made Compose exploration fast and deterministic on desktop Skiko, but three structural ceilings remain:

1. **Static PNGs only:** Explorations cannot respond to taps, scrolling, dragged bottom sheets, Navigation 3 transitions, IME insets, predictive back, Material 3 Expressive springs, or live testing on a physical phone or watch.
2. **Upstream Compose Multiplatform lag and the Wear desktop port:** Desktop `material3` tracks AndroidX releases on JetBrains' schedule (such as [#153](https://github.com/charliesbot/android-design/issues/153)), and Wear support depends on maintaining ~77.5k lines in `app/wear/` (the Wear Compose desktop shims and host glue under `app/wear/compose/`, excluded upstream components, and curved-text and rotary gaps).
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
Zero-chrome Android Host APK  (host-preview.apk: :sandbox worker + ForegroundService(specialUse) + live Activity)
  ├── InMemoryDexClassLoader(ByteBuffer) + key(dexHash) composition isolation
  ├── Real AndroidX M3 Expressive + native androidx.wear.compose AARs + bundled fonts + DesignBoard kit
  ├── Virtual BroadcastFrameClock Recomposer + offscreen capture (HardwareRenderer / VirtualDisplay)
  └── SlotTreeExtractor (LocalInspectionTables, RootForTest.semanticsOwner) + LUT PixelContrastSampler + all FindingRules
```

### Gradle as bootstrap, not compiler

One self-contained two-module Gradle project (`:host` Android application module and `:server` plain JVM module, plus the Robolectric eval classpath) stays under `app/` at the repository root. Because everything resolves from one Gradle graph, the Host APK runtime, the `kotlinc` compile classpath, and the Robolectric eval runner share structurally identical dependency versions.

- **Repo layout compliance (`AGENTS.md`):** Keeping `:host` and `:server` under `app/` keeps `plugin/skills/` restricted to skill content and the thin `scripts/android-design-preview` bootstrap script. Chai and `npx skills` installs stay free of build source trees.
- **Versioning and publishing:** Bumping `plugin/plugin.json` (and matching `VERSION` in the manifests and bootstrap script) on `main` creates a lightweight `v<version>` git tag without running `package_release.py` or uploading binary release assets.
- **On cache miss (first run or version change):**
  1. `scripts/android-design-preview` fetches `app/` via a shallow sparse checkout at tag `v<version>` into `~/.cache/android-design-preview/<version>/src/` (or reads an `ANDROID_DESIGN_PREVIEW_SRC` local checkout override during development).
  2. Bootstrap runs Gradle once (`--no-daemon`) to build `host-preview.apk` from the `previewRelease` build type (`isDebuggable = false`, `isMinifyEnabled = false`, signed with the local debug keystore).
  3. Gradle resolves `:host`'s `previewReleaseCompileClasspath` via AGP's `android-classes-jar` artifact view and merges the AndroidX `classes.jar` files into one uncompressed (`ZIP_STORED`) `androidx-compile-bundle.jar`. Merging preserves `META-INF/*.kotlin_module` and `kotlin/**/*.kotlin_builtins` for Kotlin top-level and extension lookup while avoiding opening ~95 compressed jars per edit.
  4. Gradle records `android.jar` and `$ANDROID_HOME/build-tools/*/lib/d8.jar`, writes `@server.cp`, `@client.cp`, and `@robolectric.cp` argfiles pointing into `~/.gradle/caches/`, and exits completely.
- **On cache hit:** `android-design-preview` launches `java @client.cp` and `java @server.cp` directly with zero Gradle invocation and zero Gradle daemon RAM. If Gradle's periodic cache cleanup evicts any referenced artifact, the launcher detects missing paths and re-runs bootstrap automatically.
- **Escape hatch:** Keeping `:host` and `:server` cleanly separated under `app/` lets us package a single OS-independent Slim Universal Bundle later without architectural rework.

### Host server, CLI, browser board, and edit loop

The host control plane carries over from v5 with minimal surface change: `PreviewHttpServer`, `RenderQueue`, `SelectionStore`, `FindingRules`, the browser board (multi-direction comparisons, controls for direction, screen, overlay, light or dark brightness, seed color, palette variant, and font scale, plus clickable SVG box selection over returned screenshots), and the 0.05 s Kotlin CLI (`java @client.cp` using `HttpURLConnection`, avoiding a Python rewrite).

- **One shared host server per machine:** One warm server instance manages the single `adb forward` tunnel per active device serial and keys internal records and state by `repoRoot`. Sharing one server prevents duplicate server instances, port collisions, and competing device connections when multiple repositories are open.
- **Cross-repo queue ordering and device ownership:** Offscreen `RenderQueue` jobs from different repositories serialize cleanly through the shared server tagged by `(repoRoot, dexHash)`, so background checks never overwrite another repository's results. Live visible Activity ownership belongs to whichever repository last ran `check` or `open`, or whichever repository is selected on the browser board.
- **Device lifecycle via `android` CLI:** `android-design-preview start` (and auto-setup on `check` when no active forward exists) calls the `android` CLI (`android emulator`, `android install`) to boot an emulator if needed, install `host-preview.apk`, run one-time AOT compilation (`adb shell cmd package compile -m speed -f <pkg>`), apply background idle exemptions, and establish `adb forward`.
- **Why the server stays on the host computer:**
  1. Speculative pre-compile needs a host process watching `.explore.kt` and `DESIGN.md` directly.
  2. `check` returns local host PNG file paths so the agent's file-reading tool opens them without cross-device pull logic in the CLI.
  3. Host Apple Silicon CPU encodes 2x and 4K desktop PNGs faster than an emulator.
  4. The browser board stays alive if Android recycles or kills `:sandbox`.
- **Speculative pre-compile on save:** v5 (`PreviewHttpServer.kt`) skipped `FileWatcher` compiles whenever `sseClients.isEmpty()` to avoid exhausting `MAX_COMPILES_BEFORE_RECYCLE = 25` caused by Skiko and ClassLoader retention. Removing Skiko from the host JVM lets `FileWatcher` debounce-compile and pre-push `.dex` bytecode on save during the agent's thinking gap so `check` usually hits an already-loaded DEX.
- **Device asset boundary:** Rendered PNG outputs stay on the host. Files referenced by `designPainter("assets/...")` under `design/assets/` are served by the host server over `adb forward` keyed by SHA-256 and cached on-device.

### Zero-chrome Host APK and offscreen rendering

- **Zero controls on device:** Controls remain on the browser board. The device displays unobstructed, full-bleed UI with authentic window size classes, system bar insets, and 204 or 227 dp Wear frames.
- **One unified APK and live Wear scope:** `androidx.compose.material3` and `androidx.wear.compose.material3` live in disjoint packages, so one Host APK (`host-preview.apk`) serves phone, foldable, tablet, desktop, and Wear (keeping `android.hardware.type.watch` `required="false"` in the manifest). A Phone AVD handles offscreen Wear `check` renders through `ContextThemeWrapper.applyOverrideConfiguration` (`SCREENLAYOUT_ROUND_YES`), while live interactive Wear use (real round display and rotary crown input) requires a Wear OS emulator or physical watch.
- **Dynamic DEX loading and state:** Compiled `.dex` bytes stream over keep-alive TCP and load via `InMemoryDexClassLoader(ByteBuffer)` (no writable DEX files on disk, safe under Android 14+ dynamic code loading restrictions). Wrapping the loaded root in `key(dexHash)` disposes old SlotTables and classloaders cleanly on every edit, while hoisting shell state outside `key(dexHash)` preserves controls across edits without `rememberSaveable` slot-key collisions or cross-classloader `ClassCastException`s.
- **Non-debuggable AOT host runtime and `sourceInformation`:** Build `host-preview.apk` from `previewRelease` (`isDebuggable = false`, `isMinifyEnabled = false`) and run `cmd package compile -m speed` once on install. Keeping `isDebuggable = false` avoids debuggable Compose overhead and preserves ART background verification of in-memory DEX, while `isMinifyEnabled = false` keeps upstream `sourceInformation` intact alongside `D8`-preserved exploration literals.
- **Virtual frame clock settling:** v5 (`SandboxHeadlessRenderer.kt`) settles animations via a virtual test clock (`settle(1500L)`), whereas stock Android `Choreographer` ticks in real time and would stall 1.5 s per view. Injecting a custom `Recomposer` backed by a virtual `BroadcastFrameClock` (`setParentCompositionContext` and window decor `compositionContext`) settles animations across views in milliseconds.
- **Offscreen capture, contrast LUT, and lazy PNGs:** Catalog views render offscreen without resizing the visible emulator window, using pipelined `HardwareRenderer` + `ImageReader(USAGE_CPU_READ_OFTEN)` for standard views and a private `VirtualDisplay` fallback for `Dialog`, `ModalBottomSheet`, or `Popup` windows. `PixelContrastSampler.kt` replaces per-pixel `"#%02X%02X%02X".format(r, g, b)` and hex parsing with a 256-entry sRGB LUT over raw pixel bytes. Contrast and all `FindingRules` run in memory on every view; unselected frames are discarded immediately, and only the 1 to 6 PNGs `check` returns cross `adb forward` as LZ4-compressed raw pixels for host-side PNG encoding (while browser cards use on-demand `WEBP_LOSSLESS` at quality 0).
- **Slot inspection and Wear OS:** `SlotTreeExtractor` reads semantics through `(composeView.getChildAt(0) as RootForTest).semanticsOwner` and extends Skiko layer matching (`Dialog.skiko.kt`, `Popup.skiko.kt`) with Android counterparts (`AndroidDialog.android.kt`, `AndroidPopup.android.kt`, `ModalBottomSheet.android.kt`). Native Wear views apply `ContextThemeWrapper.applyOverrideConfiguration` (`SCREENLAYOUT_ROUND_YES`, 204/227 dp, dark mode), bundled `roboto_flex.ttf`, MaterialKolor Wear color role mapping (bypassing phone `Settings.Global` dynamic-color suppression), and `ScrolledTree.merge` driven by `SemanticsActions.ScrollBy`.
- **Process isolation:** Rendering runs inside an isolated `:sandbox` process backed by a `ForegroundService(specialUse)` and ADB idle exemptions. Watchdog timeouts (infinite composition loops) and reload or native-heap thresholds recycle `:sandbox` without interrupting the host server or browser board.

### Evals on Robolectric and sandbox boundary

Evals (`evals/run.py`, `evals/multiturn.py`) run headless against the same Gradle-locked AndroidX classpath through `@robolectric.cp` inside a persistent Robolectric Native Graphics JVM per eval run, loading compiled `.class` bytes directly into a sandboxed child classloader without `D8` or an emulator.

Because Robolectric executes `.explore.kt` on the host JVM rather than inside Android's application sandbox, `BytecodeGuard` and `SourceTokenLinter` stay active in the host compiler pipeline both to isolate eval runs and to enforce deterministic authoring rules (rejecting raw `Color(...)`, `.copy(alpha = ...)`, and unbundled `FontFamily` calls).

### Rollout gate

v6 replaces v5 only after passing both speed and eval parity gates on Apple Silicon:

- **Speed gate:** Warm 1-view scoped `check` `<= 0.5 s` and full 15-view unscoped `check` `<= 2.8 s`.
- **Eval parity:** Match v5's 8-case A/B bar (`evals/run.py`) with no regression in multi-turn tokens or wall time (`evals/multiturn.py`).
- **Cutover scope:** Once both gates pass, one cutover retires Skiko, `app/wear/`, binary release packaging, and desktop-only sandbox plumbing, moves `visual-design-exploration-v5.md` and `wear-os-previewer.md` to `docs/archive/`, updates `plugin/skills/android-design-explore/` prerequisites (Android SDK, `android` CLI, and an emulator or connected device), and updates `AGENTS.md` layout notes.

### Rejected alternatives

- **Gradle incremental tasks on every edit (`:explore:dex`, Tooling API, or `--continuous`):** Pays irreducible launcher, configuration-cache, and input-snapshotting tax on every edit while pinning 2 to 3.3 GB of resident Gradle and Kotlin daemons; strictly slower and heavier than Gradle-as-bootstrap.
- **Reinstalling the APK per edit (`android run --use-delta-install`):** Takes multiple seconds per cycle, kills the active process, and resets device state on every edit.
- **Hosting the HTTP/SSE server inside the APK:** Breaks host `FileWatcher` speculative pre-compiles, complicates host PNG output paths, forces slower on-device 4K PNG encoding, and tears down the browser board when Android recycles or kills `:sandbox`.
- **Declarative JSON / A2UI data catalog:** Brings back v3's closed-catalog maintenance ceiling and gives up arbitrary Compose, custom `Canvas`, and direct copy-to-app porting.
- **Controls inside the APK:** Breaks authentic window size classes, crowds edge-to-edge bars, and does not fit 204 dp Wear screens.
- **Live Literals (`liveLiteralsV2`) and Android Studio Live Edit:** Live Literals carries deprecation risk after Live Edit superseded it, and Live Edit requires Android Studio GUI and a JVMTI proxy while `InMemoryDexClassLoader` already reloads entire `.explore.kt` leaf files including structural edits.
- **Existing preview CLIs (`android preview render`, `compose-preview` simulator):** Require project builds, rely on Layoutlib or multi-second reloads, and do not expose the `TextLayoutResult`, unclipped `LayoutInfo`, `GraphicsLayer` clip radius, and 2 px perimeter pixel data required by all `FindingRules`.
- **Shipping a Slim Universal Compiler JAR right now:** Matches Gradle-as-bootstrap's warm speed and offers faster first-run installs and simpler sandbox permissions, at the cost of keeping a release workflow. Deferred unless external distribution or sandbox friction later justifies packaging `:host` and `:server` into one OS-independent archive.

## Open questions

1. **Offscreen capture for overlays:** Confirm the cleanest capture path for `Dialog`, `ModalBottomSheet`, and `Popup` (`HardwareRenderer` + `ImageReader` primary vs `VirtualDisplay` + `Presentation` vs persistent `HostRenderActivity` without `BadTokenException`).
2. **Measured Apple Silicon warm edit latency:** Benchmark merged `androidx-compile-bundle.jar` (`android-classes-jar` transform) + in-process `D8` + `InMemoryDexClassLoader` + virtual `BroadcastFrameClock` + LZ4-to-host PNG transfer on a multi-direction `.explore.kt` (pass marks: `<= 0.5 s` warm 1-view scoped check and `<= 2.8 s` full 15-view unscoped check).
3. **Native Wear M3 on Phone AVD and `sourceInformation` after `D8`:** Verify `androidx.wear.compose` rendering under `SCREENLAYOUT_ROUND_YES` on a phone emulator and exact `<file>:<line>` parity from `SlotTreeExtractor` after `D8`.
4. **Robolectric vs Emulator parity:** Diff `LayoutTree`, Google Sans Flex variable axes, and 2 px perimeter contrast between Robolectric Native Graphics and the emulator, and check any AGSL shader gaps (such as Wear `curveToEdge`).
5. **Optional Slim Universal Bundle packaging:** Revisit bundling `:host` and `:server` into one OS-independent release archive only if Codex sandboxes or external installs cannot tolerate the first-run Gradle bootstrap.

## Links

- [Visual design exploration v5](visual-design-exploration-v5.md)
- [Wear OS in the previewer](wear-os-previewer.md)
- [Visual design exploration v4](../archive/visual-design-exploration-v4.md)
- [Visual design exploration v3](../archive/visual-design-exploration-v3.md)
- [Script ban eval baseline](script-ban-eval.md)
