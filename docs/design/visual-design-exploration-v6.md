# Visual design exploration v6

**Date:** 2026-11-22

## TL;DR

v6 keeps v5's host server (`PreviewHttpServer`, `RenderQueue`, `SelectionStore`, `FindingRules`), browser control board, and 0.05 s Kotlin CLI contract, while replacing the Compose Multiplatform Skiko desktop renderer, the ~77.5k-line Wear Compose desktop port, and the released fat JAR with a zero-chrome Android Host APK running real AndroidX Material 3 Expressive and Wear Compose on an emulator or physical device. A small two-module Gradle project inside the skill runs once at bootstrap (`--no-daemon`) to build the Host APK, bundle AndroidX compile jars, and emit `@server.cp`, `@client.cp`, and `@robolectric.cp` classpath argfiles; after bootstrap, Gradle exits and plain `java` runs the warm compiler server (`kotlinc`, Compose compiler, in-process `D8`) and CLI with zero Gradle daemon tax. Headless evals run against the same Gradle-locked AndroidX classpath on Robolectric, and `android-design-preview start` delegates AVD lifecycle and APK installation to the `android` CLI.

## Design

### Why v6

v5 ([Visual design exploration v5](visual-design-exploration-v5.md)) made Compose exploration fast and deterministic on desktop Skiko, but three structural ceilings remain:

1. **Static PNGs only:** Explorations cannot respond to taps, scrolling, dragged bottom sheets, Navigation 3 transitions, IME insets, predictive back, Material 3 Expressive springs, or live testing on a physical phone or watch.
2. **Upstream Compose Multiplatform lag and the Wear desktop port:** Desktop `material3` tracks AndroidX releases on JetBrains' schedule (such as `#153`), and Wear support depends on maintaining ~77.5k lines in `app/wear/` (49 shim, host, and generated source files under `app/wear/compose/src/`, excluded upstream components, and curved-text and rotary gaps).
3. **Platform-specific Skiko release matrix and Android-only ceilings:** v5 packages four OS and CPU Skiko `.tar.xz` archives via `package_release.py` and GitHub Releases, yet desktop Skiko still cannot render Android-only surfaces such as Glance widgets or Wear tiles.

This repository optimizes for an Android developer workflow (`AGENTS.md`), so having the Android SDK, the `android` CLI, and an emulator (or USB device) available is an acceptable baseline prerequisite for v6.

### Architecture overview

```text
agent shell / person
  │  android-design-preview <command> <file> ...       (sh entrypoint -> java @client.cp, ~0.05 s)
  ▼
Host Kotlin server  (java @server.cp, one warm JVM per repo, exits when idle, 0 MB Gradle daemon)
  ├── PreviewHttpServer, RenderQueue, SelectionStore, Watchdog, browser control board (HTML/SVG/SSE)
  ├── FileWatcher (speculative pre-compile on save) + SHA-256 asset server (design/assets/*)
  ├── SourceTokenLinter -> warm K2JVMCompiler + Compose compiler -> BytecodeGuard -> in-memory D8
  └── Host PNG encoder (decodes selected LZ4 raw frames from device into local PNG files)
  │  ▲
  │  │  persistent TCP keep-alive over `adb forward`
  ▼  │
Zero-chrome Android Host APK  (:sandbox worker + ForegroundService(specialUse) + live Activity)
  ├── InMemoryDexClassLoader(ByteBuffer) + key(dexHash) composition isolation
  ├── Real AndroidX M3 Expressive + native androidx.wear.compose AARs + bundled fonts + DesignBoard kit
  ├── Virtual BroadcastFrameClock Recomposer + offscreen capture (HardwareRenderer / VirtualDisplay)
  └── SlotTreeExtractor (LocalInspectionTables, RootForTest.semanticsOwner) + LUT PixelContrastSampler + 21 FindingRules
```

### Gradle as bootstrap, not compiler

One self-contained two-module Gradle project (`:host` Android application module and `:server` plain JVM module, plus the Robolectric eval classpath) lives inside the skill. Because everything resolves from one Gradle graph, the Host APK runtime, the `kotlinc` compile classpath, and the Robolectric eval runner share structurally identical dependency versions.

- **On cache miss (first run or runtime source hash change):** Bootstrap runs Gradle once with `--no-daemon` to build `host-debug.apk`, resolve `:host`'s `debugCompileClasspath` through AGP's `android-classes-jar` artifact view, merge the ~95 AndroidX `classes.jar` files into one uncompressed (`ZIP_STORED`) `androidx-compile-bundle.jar` (preserving `META-INF/*.kotlin_module` and `kotlin/**/*.kotlin_builtins` so Kotlin top-level and extension lookup continues to work, while avoiding the regression of opening ~95 compressed jars per edit), record `android.jar` and `$ANDROID_HOME/build-tools/*/lib/d8.jar`, and write `@server.cp`, `@client.cp`, and `@robolectric.cp` argfiles pointing into `~/.gradle/caches/`. Gradle then exits completely.
- **On cache hit:** `android-design-preview` launches `java @client.cp` and `java @server.cp` directly with zero Gradle invocation and zero Gradle daemon RAM. If Gradle's periodic cache cleanup evicts any artifact referenced in an argfile, the launcher detects missing paths and re-runs bootstrap automatically.
- **No binary release pipeline:** Dropping prebuilt release JARs and APKs eliminates `package_release.py`, `release-previewer.yml`, GitHub Release asset uploads, and manifest version synchronization. Keeping `:host` and `:server` cleanly separated leaves a straightforward escape hatch to package a single OS-independent Slim Universal Bundle later without architectural rework.

### Host server, CLI, browser board, and edit loop

The host control plane carries over from v5 with minimal surface change: `PreviewHttpServer`, `RenderQueue`, `SelectionStore`, `FindingRules`, the browser board (multi-direction comparisons, controls for direction, screen, overlay, light or dark brightness, seed color, palette variant, and font scale, plus clickable SVG box selection over returned screenshots), and the 0.05 s Kotlin CLI (`java @client.cp` using `HttpURLConnection`, avoiding a Python rewrite).

- **Device lifecycle via `android` CLI:** `android-design-preview start` (and auto-setup on `check` when no active forward exists) calls the `android` CLI (`android emulator`, `android install`) to boot an emulator if needed, install `host-debug.apk`, run one-time AOT compilation (`adb shell cmd package compile -m speed -f <pkg>`), apply background idle exemptions, and establish `adb forward`.
- **Why the server stays on the host computer:**
  1. Speculative pre-compile needs a host process watching `.explore.kt` and `DESIGN.md` directly.
  2. `check` returns local host PNG file paths so the agent's file-reading tool opens them without cross-device pull logic in the CLI.
  3. Host Apple Silicon CPU encodes 2x and 4K desktop PNGs faster than an emulator.
  4. The browser board stays alive if Android recycles or kills `:sandbox`.
- **Speculative pre-compile on save:** v5 (`PreviewHttpServer.kt`) skipped `FileWatcher` compiles whenever `sseClients.isEmpty()` specifically to avoid exhausting `MAX_COMPILES_BEFORE_RECYCLE = 25` caused by Skiko and ClassLoader retention. Removing Skiko from the host JVM lets `FileWatcher` debounce-compile and pre-push `.dex` bytecode on save during the agent's thinking gap so `check` usually hits an already-loaded DEX.
- **Device asset boundary:** Rendered PNG outputs stay on the host, while `designPainter("assets/...")` files under `design/assets/` are served by the host server over the `adb forward` channel keyed by SHA-256 and cached on-device.

### Zero-chrome Host APK and offscreen rendering

- **Zero controls on device:** Controls remain on the browser board so the device shows unobstructed, full-bleed UI with authentic window size classes, system bar insets, and 204 or 227 dp Wear frames.
- **One unified APK:** `androidx.compose.material3` and `androidx.wear.compose.material3` live in disjoint packages, so one Host APK serves phone, foldable, tablet, desktop, and Wear (keeping `android.hardware.type.watch` `required="false"` in the manifest).
- **Dynamic DEX loading and state:** Compiled `.dex` bytes stream over keep-alive TCP and load via `InMemoryDexClassLoader(ByteBuffer)` (no writable DEX files on disk, avoiding Android 14+ dynamic code loading restrictions). Wrapping the loaded exploration root in `key(dexHash)` disposes old SlotTables and classloader references cleanly on every edit, while hoisting shell state (direction, screen, brightness, seed, variant, font scale) outside `key(dexHash)` preserves controls across edits without risking cross-classloader `ClassCastException` or `rememberSaveable` slot-key collisions when source lines shift.
- **AOT host runtime and `sourceInformation`:** The Host APK builds with `isMinifyEnabled = false` so R8 does not strip Compose `sourceInformation` from upstream Material 3 and Wear Compose AARs, and runs `cmd package compile -m speed` once on install so framework code executes natively while only the tiny exploration `.dex` starts in ART `nterp`. `D8` preserves `sourceInformation` string literals.
- **Virtual frame clock settling:** v5 (`SandboxHeadlessRenderer.kt`) settles animations via a virtual test clock (`settle(1500L)`). On Android, stock `Choreographer` ticks in real time and would stall 1.5 s per view. Injecting a custom `Recomposer` backed by a virtual `BroadcastFrameClock` (`setParentCompositionContext` and window decor `compositionContext`) settles animations across views in milliseconds.
- **Offscreen capture, contrast LUT, and lazy PNGs:** Catalog views render offscreen without resizing the visible emulator window, using pipelined `HardwareRenderer` + `ImageReader(USAGE_CPU_READ_OFTEN)` for standard views and falling back to a private `VirtualDisplay` when a view spawns `Dialog`, `ModalBottomSheet`, or `Popup` windows. `PixelContrastSampler.kt`'s inner loop replaces per-pixel `"#%02X%02X%02X".format(r, g, b)` and hex parsing with a 256-entry sRGB LUT directly over raw pixel bytes. Contrast and all 21 `FindingRules` run in memory on every view; unselected frames are discarded immediately, and only the 1 to 6 PNGs `check` returns cross `adb forward` as LZ4-compressed raw pixels for host-side PNG encoding (while browser cards request on-demand `WEBP_LOSSLESS` at quality 0).
- **Slot inspection and Wear OS:** `SlotTreeExtractor` reads semantics through `(composeView.getChildAt(0) as RootForTest).semanticsOwner` and extends Skiko layer matching (`Dialog.skiko.kt`, `Popup.skiko.kt`) with Android counterparts (`AndroidDialog.android.kt`, `AndroidPopup.android.kt`, `ModalBottomSheet.android.kt`). Native Wear views apply `ContextThemeWrapper.applyOverrideConfiguration` (`SCREENLAYOUT_ROUND_YES`, 204/227 dp, dark mode), bundled `roboto_flex.ttf`, MaterialKolor Wear color role mapping (bypassing phone `Settings.Global` dynamic-color suppression), and `ScrolledTree.merge` driven by `SemanticsActions.ScrollBy`.
- **Process isolation:** Rendering runs inside an isolated `:sandbox` process backed by a `ForegroundService(specialUse)` and ADB idle exemptions. Watchdog timeouts (infinite composition loops) and reload or native-heap thresholds recycle `:sandbox` without interrupting the host server or browser board.

### Evals on Robolectric and sandbox boundary

Evals (`evals/run.py`, `evals/multiturn.py`) run headless against the same Gradle-locked AndroidX classpath through `@robolectric.cp` inside a persistent Robolectric Native Graphics JVM per eval run, loading compiled `.class` bytes directly into a sandboxed child classloader without `D8` or an emulator.

Because Robolectric executes `.explore.kt` on the host JVM rather than inside Android's application sandbox, `BytecodeGuard` and `SourceTokenLinter` stay active in the host compiler pipeline both to isolate eval runs and to enforce deterministic authoring rules (rejecting raw `Color(...)`, `.copy(alpha = ...)`, and unbundled `FontFamily` calls).

### Rejected alternatives

- **Gradle incremental tasks on every edit (`:explore:dex`, Tooling API, or `--continuous`):** Pays irreducible launcher, configuration-cache, and input-snapshotting tax on every edit while pinning 2 to 3.3 GB of resident Gradle and Kotlin daemons; strictly slower and heavier than Gradle-as-bootstrap.
- **Reinstalling the APK per edit (`android run --use-delta-install`):** Takes multiple seconds per cycle, kills the active process, and resets device state on every edit.
- **Hosting the HTTP/SSE server inside the APK:** Breaks host `FileWatcher` speculative pre-compiles, complicates host PNG output paths, forces slower on-device 4K PNG encoding, and tears down the browser board when Android recycles or kills `:sandbox`.
- **Declarative JSON / A2UI data catalog:** Brings back v3's closed-catalog maintenance ceiling and gives up arbitrary Compose, custom `Canvas`, and direct copy-to-app porting.
- **Controls inside the APK:** Breaks authentic window size classes, crowds edge-to-edge bars, and does not fit 204 dp Wear screens.
- **Live Literals (`liveLiteralsV2`) and Android Studio Live Edit:** Live Literals carries deprecation risk after Live Edit superseded it, and Live Edit requires Android Studio GUI and a JVMTI proxy while `InMemoryDexClassLoader` already reloads entire `.explore.kt` leaf files including structural edits.
- **Existing preview CLIs (`android preview render`, `compose-preview` simulator):** Require project builds, rely on Layoutlib or multi-second reloads, and do not expose the `TextLayoutResult`, unclipped `LayoutInfo`, `GraphicsLayer` clip radius, and 2 px perimeter pixel data required by the 21 `FindingRules`.
- **Shipping a Slim Universal Compiler JAR right now:** Matches Gradle-as-bootstrap's warm speed and offers faster first-run installs and simpler sandbox permissions, at the cost of keeping a release workflow. Deferred unless external distribution or sandbox friction later justifies packaging `:host` and `:server` into one OS-independent archive.

## Open questions

1. **Offscreen capture for overlays:** Confirm the cleanest capture path for `Dialog`, `ModalBottomSheet`, and `Popup` (`HardwareRenderer` + `ImageReader` primary vs `VirtualDisplay` + `Presentation` vs persistent `HostRenderActivity` without `BadTokenException`).
2. **Measured Apple Silicon warm edit latency:** Benchmark merged `androidx-compile-bundle.jar` (`android-classes-jar` transform) + in-process `D8` + `InMemoryDexClassLoader` + virtual `BroadcastFrameClock` + LZ4-to-host PNG transfer on a multi-direction `.explore.kt`.
3. **Native Wear M3 on Phone AVD and `sourceInformation` after `D8`:** Verify `androidx.wear.compose` rendering under `SCREENLAYOUT_ROUND_YES` on a phone emulator and exact `<file>:<line>` parity from `SlotTreeExtractor` after `D8`.
4. **Robolectric vs Emulator parity:** Diff `LayoutTree`, Google Sans Flex variable axes, and 2 px perimeter contrast between Robolectric Native Graphics and the emulator, and check any AGSL shader gaps (such as Wear `curveToEdge`).
5. **Optional Slim Universal Bundle packaging:** Revisit bundling `:host` and `:server` into one OS-independent release archive only if Codex sandboxes or external installs cannot tolerate the first-run Gradle bootstrap.

## Links

- [Visual design exploration v5](visual-design-exploration-v5.md)
- [Wear OS in the previewer](wear-os-previewer.md)
- [Visual design exploration v4](../archive/visual-design-exploration-v4.md)
- [Visual design exploration v3](../archive/visual-design-exploration-v3.md)
- [Script ban eval baseline](script-ban-eval.md)
