# Compose Multiplatform proposal fidelity

2026-10-03

## Conclusion

Compose Multiplatform can render real, shared Compose UI in a browser, including Material 3 Expressive. It is not an HTML imitation of an Android design. That fits this project's definition of fidelity: confidence that a proposal can transfer into the requesting app, rather than pixel-identical rendering. However, the published versions inspected do not match the starter's full AndroidX Material3 API surface. These findings establish a plausible alternative, not complete compatibility. The visual exploration design selects Android plus Compose Preview CLI for v1 to avoid added template, browser-hosting, and capture infrastructure; it does not reject multiplatform on fidelity grounds.

This is dependency and documentation research, not a compiled or rendered comparison. It informs the [visual exploration design](../design/visual-design-exploration.md); it does not change the skill, template, or evaluation framework. The decision remains one renderer for v1, not a hybrid fallback system.

## What is actually shared

Compose Multiplatform extends Jetpack Compose and shares its compiler, runtime, declarative UI model, layout APIs, state handling, and animation concepts. JetBrains ports libraries for non-Android targets. Android builds resolve Google's AndroidX artifacts through Gradle metadata; other targets resolve JetBrains' implementations. Different Maven coordinates therefore do not mean a different design system. A composable using supported common APIs can be shared rather than recreated for the browser. [JetBrains: relationship to Jetpack Compose](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-and-jetpack-compose.html)

The relevant browser path is Compose UI on Kotlin/Wasm, not a separately authored HTML/CSS screen. JetBrains documents Material3 components, adaptive resizing, animations, and browser interaction as supported capabilities. This establishes feasibility, not verification of this repository's proposals. [JetBrains: Compose for web Beta](https://blog.jetbrains.com/kotlin/2025/09/compose-multiplatform-1-9-0-compose-for-web-beta/)

## Version alignment is the concrete gap

The [starter version catalog](../../skills/android-design/assets/exploration-starter/gradle/libs.versions.toml) pins AndroidX Material3 `1.5.0-alpha29`, Compose BOM `2026.03.01`, and Kotlin `2.3.20`. The releases inspected advertise these mappings:

| Compose Multiplatform release | JetBrains Material3 artifact version | AndroidX Material3 baseline |
| --- | --- | --- |
| `1.12.1` | `1.12.0-alpha03` | `1.5.0-alpha22` |
| `1.13.0-alpha01` | `1.13.0-alpha01` | `1.5.0-alpha27` |

Sources: [1.12.1 component table](https://github.com/JetBrains/compose-multiplatform/releases/tag/v1.12.1), [1.13.0-alpha01 component table](https://github.com/JetBrains/compose-multiplatform/releases/tag/v1.13.0-alpha01). These are advertised release mappings, not a complete Maven artifact audit. No matching `alpha29` baseline was established in this research. The prerelease also documents a web memory-access error; choosing it solely to get closer API alignment is not automatically the safer route.

This lag has concrete consequences. AndroidX `alpha28` changes carousel parallax and toggle-button APIs and deprecates stateless slider overloads. `alpha29` makes `onValueChange` required and changes its parameter position in slider APIs. A proposal written against those newer signatures cannot be assumed to compile unchanged against the inspected multiplatform baseline. The mismatch is more specific than an unsupported general claim that Expressive is unavailable. [AndroidX Material3 release notes](https://developer.android.com/jetpack/androidx/releases/compose-material3#1.5.0-alpha29)

The earlier `org.jetbrains.compose.material3:material3:1.9.0-alpha04` example was real Expressive support, not evidence of current version parity. JetBrains explicitly documents `MaterialExpressiveTheme`, with color, motion, shape, and typography configuration. Its API reference also exposes emphasized typography roles such as `displayLargeEmphasized`. [Expressive introduction](https://kotlinlang.org/docs/multiplatform/whats-new-compose-190.html#material-3-expressive-theme), [Typography API](https://kotlinlang.org/api/compose-multiplatform/material3/androidx.compose.material3/-typography/)

## Fit with the current starter

The source inspection found platform glue, not evidence that the proposal layout itself must be rewritten:

| Current dependency | Meaning for browser proposals |
| --- | --- |
| Android application/library Gradle modules | A web target and common source set need configuration; this is not a launch flag on the current project. |
| `AppTheme` uses `Build`, `LocalContext`, and Android dynamic-color functions | Keep Android system integration platform-specific; provide explicit proposal colors to shared UI. Do not silently swap palettes between previews. |
| Typography loads `R.font.google_sans_flex` | Reuse the bundled font, but adapt resource loading for common/web code. |
| `ProposalScreen` uses Android `stringResource` and `R.string` | Use shared resources or caller-provided proposal content. |
| Android activity, application, and Koin Android setup | These are host wiring, not transferable browser entry points. A design preview need not port the entire app's data layer. |

Local evidence: [AppTheme](../../skills/android-design/assets/exploration-starter/app/src/main/kotlin/com/example/designsandbox/theme/AppTheme.kt), [Typography](../../skills/android-design/assets/exploration-starter/app/src/main/kotlin/com/example/designsandbox/theme/Typography.kt), [ProposalScreen](../../skills/android-design/assets/exploration-starter/app/src/main/kotlin/com/example/designsandbox/ProposalScreen.kt), and [app build](../../skills/android-design/assets/exploration-starter/app/build.gradle.kts).

JetBrains explicitly identifies `LocalContext`, Android resource access, activities, and other Android APIs as platform-specific. Camera, permissions, maps, and device services are not made portable simply by sharing Compose UI. Accordingly, transferable screen structure does not imply transferable host integration. [Android-only components](https://kotlinlang.org/docs/multiplatform/compose-android-only-components.html)

## Typography, resizing, and interaction

Variable fonts are supported on all platforms from Compose Multiplatform 1.8.2, including custom variation axes. Therefore Google Sans Flex is not a documented categorical blocker. The specific bundled font, selected axes, and typography setup have not been exercised in a browser here. [Variable-font support](https://kotlinlang.org/docs/multiplatform/whats-new-compose-180.html#variable-fonts)

Web fonts load asynchronously. Preloading is needed to avoid judging a temporary fallback font or missing glyphs as the proposal. The common resource API loads fonts from `composeResources/font`; the existing Android `R.font` access is not interchangeable with it. [Web resource loading](https://kotlinlang.org/docs/multiplatform/compose-web-resources.html), [Common font resources](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html#fonts)

Adaptive layout logic can live in common code. JetBrains documents `org.jetbrains.compose.material3.adaptive:adaptive:1.3.0-rc01` and `currentWindowAdaptiveInfoV2()`. Its older Android-only-components page still lists adaptive libraries among unavailable APIs; the dedicated adaptive guide and current release table provide more specific, newer evidence. Do not interpret that older list as a blanket absence of adaptive support. [Adaptive layouts](https://kotlinlang.org/docs/multiplatform/compose-adaptive-layouts.html)

Inference: a live proposal can remeasure and recompose as its viewport changes, replacing repeated static captures for inspecting responsive layouts. This is different from stretching a screenshot. But browser width does not simulate Android system bars, keyboard insets, fold posture, or a device's density automatically. Those are separate environmental assumptions. Shared interaction code can demonstrate selection, scrolling, and motion; browser behavior is not proof of Android system gesture or IME behavior.

JetBrains explicitly warns that text rendering and native behaviors can differ by platform. It also documents Compose Hot Reload as JVM-only, so a web preview must not be described as having the same hot-reload loop. No build-time or edit-to-preview speed claim is established here. [Platform-specific behavior](https://kotlinlang.org/docs/multiplatform/compose-platform-specifics.html)

## Decision boundary

The evidence supports real shared components, Expressive theming, variable fonts, and adaptive browser interaction. It does not establish an unchanged build of the current starter, complete parity with AndroidX `alpha29`, or faster iteration.

If web rendering is revisited, the unresolved technical question is whether an aligned dependency baseline and limited host/resource adaptations preserve the designs the skill should propose. Selecting web must not require replacing real components with imitations, suppressing useful Expressive APIs, or maintaining a second hand-authored proposal. These are transferability concerns, not demands for identical pixels. The current v1 decision is recorded in the design document rather than inferred from these research findings.

## Reusing `compose-preview browse`

Source inspection pinned `compose-ai-tools` at `15b8080eb5435ca6ceaba93230bc28a0bd41b9c6` and `compose-preview-server` at `7c958637b9b94f290d244cd44b6ba383d36103d5`. This was not a running browser evaluation. Local CLI `2.33.0` help was inspected; the separate server distribution could not be resolved in the local network-restricted check, so no server was installed or launched. That is an environment limitation, not evidence of UI incompatibility.

**Reuse opportunity:** `browse` already supplies a local component catalog, preview discovery across modules, source inspection, and an optional interactive Wasm lane. It delegates to the server with `--discover`, `--component-browser`, and `--no-history`. It deliberately omits the full viewer's revision history. [Pinned BrowseCommand](https://github.com/yschimke/compose-ai-tools/blob/15b8080eb5435ca6ceaba93230bc28a0bd41b9c6/cli/src/main/kotlin/ee/schimke/composeai/cli/BrowseCommand.kt)

**Interactive prerequisites:** automatic discovery finds an existing executable `wasmJs` project, including a browser app depending on a shared preview module. Its usable distribution must contain `index.html` and `compose-preview-components.json`. The server can build a missing distribution with `wasmJsBrowserDistribution`, but it does not convert the Android starter into shared code or supply the app's component dispatch protocol. The source's project-browser path first starts native preview daemon sessions; failed Wasm preparation leaves snapshots available. [Pinned discovery](https://github.com/yschimke/compose-preview-server/blob/7c958637b9b94f290d244cd44b6ba383d36103d5/server/src/main/kotlin/ee/schimke/composeai/cli/serve/ServeWasmDiscovery.kt), [Pinned server preparation](https://github.com/yschimke/compose-preview-server/blob/7c958637b9b94f290d244cd44b6ba383d36103d5/server/src/main/kotlin/ee/schimke/composeai/cli/serve/ServeRunner.kt)

**Embedding and resizing:** the viewer embeds Wasm in a sandboxed iframe. Theme, font scale, locale, and declared knobs update through URL parameters and message patches. The sample signals `cp-wasm-ready` after its first frame. However, the viewer positions the iframe over the snapshot's footprint and treats device/orientation controls as server-render-only. Browser-window fitting is therefore not evidence of arbitrary device-size reflow. A responsive proposal host still needs a deliberate viewport contract. [Pinned viewer](https://github.com/yschimke/compose-preview-server/blob/7c958637b9b94f290d244cd44b6ba383d36103d5/serve-web/src/viewer.ts), [Pinned sample protocol](https://github.com/yschimke/compose-ai-tools/blob/15b8080eb5435ca6ceaba93230bc28a0bd41b9c6/samples/cmp-wasm-catalog/src/wasmJsMain/kotlin/com/example/cmpwasmcatalog/Main.kt)

**One-renderer constraint:** the component browser intentionally opens in snapshot mode, with Wasm as an optional switch and the snapshot retained during startup. Its tests enforce this behavior. No supported snapshot-free `browse` switch was established. A browser capture for agent inspection would still be evidence from the web renderer, not a second design renderer; the inspected Wasm handshake itself did not establish a dedicated agent image-capture API. Do not assume native `render_preview` output depicts the currently interactive Wasm state. [Pinned component-browser tests](https://github.com/yschimke/compose-preview-server/blob/7c958637b9b94f290d244cd44b6ba383d36103d5/server/src/test/kotlin/ee/schimke/composeai/cli/serve/ServeComponentBrowserTest.kt)

**Workflow gap:** upstream comparison tools concern rendered components against references or alternate rendering lanes, including reporting selected mismatches. They are not evidence of this design's shortlist, one-textbox-per-proposal, refinement-round, and final-approval workflow. No complete version of that workflow was established in the inspected surfaces. [Pinned comparison selection logic](https://github.com/yschimke/compose-preview-server/blob/7c958637b9b94f290d244cd44b6ba383d36103d5/serve-web/src/compare/picks.ts)

If live web proposals are revisited, evaluate reuse before creating new preview infrastructure, but do not equate `browse` with the proposed gallery. Reusing its browser host would still require resolving the snapshot-first behavior, responsive sizing, and exploration state. Hosting the shared Compose proposal directly remains a distinct possibility if adapting the component browser costs more than the small exploration workflow needs. Neither path is part of the selected Android screenshot workflow for v1.
