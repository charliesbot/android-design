# Visual design exploration v7

**Date:** 2026-11-25

## TL;DR

v7 keeps v6's host server, CLI, browser board, compiler pipeline, board kit, tree extractor, and finding rules. It replaces the emulator as the renderer: boards render through layoutlib (the engine behind Android Studio previews), driven directly from a JVM the server owns. Phone, foldable, tablet, desktop, and Wear OS views all render this way, with no device, no `adb`, and no APK install in the default path.

The person sees one surface, the browser board. The emulator and the Host APK remain only as an explicit opt-in for live interaction, and nothing boots them implicitly.

In spikes on Apple Silicon, layoutlib 17.0.4 (API 37) with the repo's real kit matched the emulator host node for node on phone and Wear boards. Findings were identical on phone, and on Wear apart from a node-ordering instability that predates v7. The renderer drops from an emulator holding 1.8 to 5.1 GB to a worker JVM under 1 GB.

## Design

### Why v7

v6 ([Visual design exploration v6](../archive/visual-design-exploration-v6.md)) reached real AndroidX fidelity by rendering on an emulator. That fixed v5's Compose Multiplatform lag, but it left three problems:

1. **Two tools wired together.** The result experience is a browser board plus an emulator that is sometimes visible and sometimes not. In practice agents run `check` and share `boardUrl`, so the person only ever sees images, while a multi-gigabyte emulator runs out of sight.
2. **A device to keep alive.** The server must boot, own, and shut down an emulator. Several paths can leave one running with no owner: a crash mid-boot, `stop` against a dead server, an adopted emulator with no process handle, and a closed board tab that does not shorten the idle timer.
3. **Cost.** A cold start waits on an emulator boot (13 to 19 s measured), and the emulator plus server held 3.1 to 5.95 GB resident in this session's measurements.

The requirement that does not change is fidelity. Android libraries ship features well before other renderers support them, so the renderer has to run real Android framework drawing code at the current API level.

### One surface

The browser board is the product surface. It already holds every setting a person adjusts (direction, screen, overlay, device, light or dark, seed color, style variant, font scale, inspector) and it is how results are shared and viewed remotely. v7 makes it the only surface by default.

Interaction and motion are not part of the default path: layoutlib produces settled frames, not a running app. Judging springs, drags, and transitions stays an explicit live mode on the emulator Host APK (see "The emulator after v7").

### Architecture overview

```text
agent shell / person
  │  android-design-preview <command> <file> ...       (unchanged for check, tree, render, open)
  ▼
Host Kotlin server  (unchanged: PreviewHttpServer, RenderQueue, SelectionStore, FileWatcher, browser board)
  ├── SourceTokenLinter -> warm K2JVMCompiler + Compose compiler -> BytecodeGuard      (JVM classes, no D8)
  └── Render worker JVM  (owned by the server, exits when the server's pipe closes)
        ├── layoutlib 17.x from Google Maven (layoutlib, layoutlib-runtime, layoutlib-resources)
        ├── Vendored driver (from Paparazzi's driver sources, Apache-2.0)
        ├── Real AndroidX M3 Expressive + Wear Compose classes + bundled fonts + DesignBoard kit
        ├── Per-view DeviceConfig (size, density, round, night, font scale) + SettlePolicy frame stepping
        └── AndroidSlotTreeExtractor + LutPixelContrastSampler + all FindingRules  (same source as the Host APK)
```

The board kit, theme, fonts, `AndroidSlotTreeExtractor`, `ScrolledTree.merge`, `ViewCheck`, and `FindingRules` compiled and ran on layoutlib with zero source changes. They take plain Compose types (slot tables from `LocalInspectionTables`, semantics roots, layout coordinates, a pixel frame), none of which are device-specific. Existing layoutlib-based preview tools do not expose this data, which is why none of them can replace the renderer as is. Calling layoutlib in-process removes that limit, because the extractor runs inside the same composition.

What leaves the default path: `adb forward`, the TCP frame protocol, LZ4 frame transport, `InMemoryDexClassLoader`, D8, APK install and AOT compilation, emulator boot and ownership, and the `:sandbox` process.

### The render worker

Rendering runs in a JVM separate from the server, for the same reasons v6 isolated `:sandbox`: a hung composition, a leaked classloader, or a native crash must not take down the board or the compiler. The worker is a child of the server connected by a pipe and exits when the pipe closes, so it cannot outlive its owner. There is nothing else to orphan: no device, no `adb` server, no emulator.

layoutlib holds a global `Bridge` lock, so renders are serial within one worker. That matches the existing `RenderQueue`.

### The driver

layoutlib is published as plain jars but has no supported outside API. A driver has to initialize the `Bridge`, supply resources and assets, describe the device, set up the render thread and clock, host a `ComposeView` with lifecycle owners, and read the image back. Paparazzi's driver does this and is Apache-2.0, but its releases trail layoutlib (its current alpha targets layoutlib 16.2.1, API 36), which would reintroduce the upstream lag v6 was built to remove.

v7 vendors the driver sources into `app/` and owns them. Only the driver is taken, not Paparazzi's Gradle plugin, snapshot comparison, or test integration. The spikes needed these changes for layoutlib 17:

- **Version class initialization.** The driver must force `android.os.Build$VERSION` to initialize before writing `SDK_INT`. Otherwise layoutlib's static initializer resets it to 0, and Compose silently takes pre-API-29 code paths: `ViewLayer` instead of `GraphicsLayer`, which drops elevation shadows, every nested `graphicsLayer` `RenderEffect` (including `Modifier.blur` and Wear `curveToEdge()`), and variable font axes. This one line was the difference between failing and passing the fidelity bar, and it fails without any error.
- **layoutlib 17 API moves.** Thread setup and cleanup, the frame-time hooks, and image readback (`getRecyclableImage`, which must be closed) all changed. `layoutlib-api` and its sibling tool libraries must be 32.5.3 or newer.
- **AndroidX moves.** The view-tree owner setters changed packages.
- **Classpath.** The SDK's `android.jar` goes last on the classpath for `android.os.Build`, and `Build` strings such as `MANUFACTURER` must be non-null (Wear haptics dereferences them).
- **Assets.** The host's `assets/` directory is passed as an asset directory, so the existing `Font(path, assetManager, variationSettings)` loading works unchanged.

### Settling

A single frame at time zero is not a valid capture. The driver reproduces the host's `SettlePolicy` with a virtual frame clock: step 16.67 ms frames with a minimum of 4, stop after 3 idle frames, cap at 120, redraw at the same virtual time, then capture. Wear list views then scroll and merge exactly as today.

Four details were each shown necessary by ablation:

1. Drain pending Handler callbacks before reading `Recomposer.hasPendingWork`, or lists are captured one frame early.
2. Create a fresh render session per target. Reusing one leaks a previous overlay window's state into the next view.
3. Run `delay()` in real time, as the host does (it virtualizes only the frame clock), or timed content appears that the host never shows.
4. Treat Wear dialogs as same-window content, not as overlay windows with the longer overlay minimum.

### Overlay windows

`AlertDialog`, `ModalBottomSheet`, `DropdownMenu`, and other `Popup` content open their own windows. layoutlib composites secondary windows itself, in hardware, with dim and shadows, but only when the main window's root node is re-recorded. On a static base screen that never happens after the first frame, so a sheet's entrance animation and a popup's final position are never drawn. Before each render pass the driver therefore does two things for every window:

- **Invalidate the main renderer's root**, so secondary windows are re-recorded.
- **Put the window in touch mode.** layoutlib reports "not in touch mode", so new windows assign initial focus, which paints a focus highlight on the sheet's drag handle and the first menu item.

Tree extraction collects Compose roots from every window and passes each window's frame offset, as the host's `OverlayWindowHelper` does.

Inline overlays (the kit's `Scrim` followed by `BottomSheet`) are ordinary same-window Compose and need none of this.

### Ground truth is a device at the view's size

Parity was measured against the emulator host, and the comparison exposed three places where the host's output is an artifact of rendering a small view on a larger AVD. v7 renders what the view's own window would show and does not reproduce these:

- **No elevation shadows.** The host's `HardwareRenderer` capture draws none, on any component. layoutlib draws them.
- **Overlay insets from the wrong display.** The host's main window consumes all insets, so base screens lay out with none. Its secondary windows still receive the physical AVD's navigation bar inset, read at the overridden density, which adds 36 dp to the bottom of a modal sheet on a phone view. In v7 every window of a view gets the same insets as the main window, which stay at none, so base screens do not change and a modal sheet is 36 dp shorter than today.
- **Menu placement.** Compose positions a dropdown against the AVD's real display, and the host then clamps it into the view, which puts the menu flush against the edge and raises a false `overflow` finding. layoutlib positions it against the view's window.

These three change rendered output and findings relative to v6's emulator eval baselines: shadows appear, modal sheets get shorter, and dropdown menus move and stop raising that `overflow` error. They follow from how Compose and the framework behave in a window of the view's size; none was checked against a physical device.

### The emulator after v7

The Host APK, `LivePreviewActivity`, and the device code stay in the repository for one purpose: live interaction and motion. Live mode is an explicit flag on `open`, and it is the only thing that boots or contacts a device. `check`, `tree`, `render`, plain `open`, and evals never do. `start` starts only the server and render worker; its device options (`--serial`, `--avd`, `--headless`) move to live mode. A person who wants to feel a spring or drag a sheet asks for live mode and gets a visible emulator window that they own and close.

The kit, extractor, and finding rules are one source tree used by both the render worker and the Host APK, so the two cannot drift in design-system behavior.

### Evidence

All measurements: Apple Silicon, JBR 21, layoutlib 17.0.4 (API 37), Compose UI 1.13.0, Material 3 1.5.0, Wear Compose 1.8.0-alpha03, compared against the v6 emulator host on API 37 AVDs. Timings are single runs.

| What was compared | Result |
| --- | --- |
| Phone board (`evals/multiturn_fixture`), 15 views, 519 nodes | 0 nodes differ in bounds, text ink, line count, touch, or colors. Findings and full check output identical in 15 of 15. Pixels over 48/255 different: 0.00% in every view. |
| Two deliberately bad boards (`evals/calibration`), 10 views | Findings and check output identical, including `contrast` and `line-length` error text. |
| Bundled fonts, every variable axis in use | Same line widths and wrap points as the emulator; remaining difference is anti-aliasing. |
| Uniform and progressive blur | Match the emulator. |
| Wear boards (repo fixture plus one new board), 16 views, 182 nodes | 0 nodes differ in bounds, ink, lines, or touch, including scrolled trees. `curveToEdge()`, curved `TimeText`, `EdgeButton`, scroll indicator, and `DatePicker` draw. Pixels over 48/255: 0.03% mean, 0.17% worst. Findings identical in 12 of 16 and full check output in 11 of 16. The misses differ in node ids and in which text a node reports, both of which also vary between two emulator runs; in one view that ordering flips a `contrast` error and with it pass or fail. |
| `AlertDialog`, `ModalBottomSheet`, `DropdownMenu` | Measured with the host's 36 dp secondary-window inset declared, to isolate rendering from the inset difference above. Dialog and sheet match (0.00% over 48/255), with identical trees and findings. Menu matches after accounting for the placement difference above. |

| Cost | Emulator host | layoutlib spike harness |
| --- | --- | --- |
| Time to first image from nothing running | 13 to 19 s (server start, emulator boot, install, compile, render) | about 2 s (precompiled board, harness JVM start and render only) |
| 15 phone views, cold JVM to exit | not comparable | about 5 s, of which 1.1 s is rendering |
| 16 Wear views, cold JVM to exit | not comparable | 4.2 s |
| Renderer resident memory | 1.8 to 5.1 GB (emulator process) | 0.8 to 0.95 GB peak (harness JVM) |

Both paths also run the server and compiler JVM, measured at 0.8 to 1.3 GB, which v7 keeps. The harness numbers exclude it and exclude compilation, so they bound the render worker, not the whole tool.

Limits of this evidence, which bound what the table proves:

- One machine class, one API level, 2x density. Linux and JDK 17 were not run.
- Warm render time inside the real server pipeline was not measured; v6's 766 ms median for a full 15-view check is the number to beat.
- Flat fills under fractional layer alpha differ by one level in 255, because layoutlib rasterizes in software and rounds differently from the GPU.
- Image assets (`designPainter`), overlays in dark and on large views, and animating base content under an overlay were not exercised.
- Overlays with no secondary-window inset, the configuration v7 specifies, were rendered but had no reference to compare against. No physical device was measured for any comparison.
- Tree node ids and the text a node reports depend on identity-hash iteration order in the extractor, on both renderers. That instability predates v7.

### Rejected alternatives

- **Hardening the hidden emulator (gRPC idle timeout, pipe watchdog, lease file, board Stop button):** Fixes orphans but keeps the boot time, the memory, and a device as a hidden second tool.
- **Board plus a visible emulator as the standard experience:** The two-tools shape itself. Kept only as explicit live mode.
- **Streaming a live stage into the board from a hidden emulator:** One surface with real interaction, but it keeps every emulator cost and adds a frame stream and input path.
- **A full previewer inside the APK on a tablet or desktop emulator:** Coherent and interactive, but it rebuilds the board in Compose, requires a large visible AVD, and loses the shareable URL, remote viewing, and sandbox use. Images are still needed for agents and evals.
- **Prevue as the renderer:** Same engine, but its input is `@Preview` functions (one per variant), it broke on the pinned Compose until `ui-tooling` was downgraded, and it exposes no layout tree for findings. Its loop (one browser page, redraw on save, click to point) remains the reference for the board experience.
- **Depending on Paparazzi:** Trails layoutlib releases, and its driver source needed the `SDK_INT` initialization fix to render correctly on layoutlib 17.
- **Robolectric, Compose Multiplatform desktop:** v6 retired both as renderers. Robolectric draws Wear `curveToEdge()` flat, and Compose Multiplatform 1.13 still treats progressive blur as a no-op.
- **Mac.apk, docker-android:** A partial framework reimplementation with no dynamic class loading, and a Linux-only KVM container, respectively.

## Links

- [Visual design exploration v6](../archive/visual-design-exploration-v6.md)
- [Visual design exploration v5](../archive/visual-design-exploration-v5.md)
- [layoutlib on Google Maven](https://dl.google.com/android/maven2/com/android/tools/layoutlib/layoutlib/maven-metadata.xml)
- [Paparazzi](https://github.com/cashapp/paparazzi) (driver source, Apache-2.0)
- [Prevue](https://github.com/kakao/prevue)
