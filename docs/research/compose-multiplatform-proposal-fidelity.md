# Compose Multiplatform proposal fidelity

2026-10-03

## Conclusion

Compose Multiplatform can render real, shared Compose UI in a browser, including Material 3 Expressive. It is not an HTML imitation of an Android design. That fits this project's definition of fidelity: confidence that a proposal can transfer into the requesting app, rather than pixel-identical rendering. However, the published versions inspected do not match the starter's full AndroidX Material3 API surface. These findings establish a plausible alternative, not complete compatibility. The visual exploration design selects Android plus Compose Preview CLI for v1 to avoid added template, browser-hosting, and capture infrastructure; it does not reject multiplatform on fidelity grounds.

This is dependency and documentation research, not a compiled or rendered comparison. It informed the [v1 exploration design](../archive/visual-design-exploration.md) and is the alternative recorded in [v2](../design/visual-design-exploration-v2.md); it does not change the skill, template, or evaluation framework. The decision remains one renderer for v1, not a hybrid fallback system.

## Verification 2026-10-10

Re-checked against primary sources one week after the original research. The 2026-10-03 table was accurate for its date. Since then JetBrains shipped `1.13.0-alpha02` and Google shipped AndroidX Material3 `1.5.0-beta01`, so the gap moved rather than closed. Sentences in the older text that this check corrected are listed at the end of this section.

### Version table

| Compose Multiplatform release | Date | JetBrains `org.jetbrains.compose.material3:material3` | AndroidX Material3 baseline | How the baseline was established |
| --- | --- | --- | --- | --- |
| `1.12.1` (current stable) | 2026-09-22 | `1.12.0-alpha03` | `1.5.0-alpha22` | [Release table](https://github.com/JetBrains/compose-multiplatform/releases/tag/v1.12.1); the [`material3-android` POM](https://repo1.maven.org/maven2/org/jetbrains/compose/material3/material3-android/1.12.0-alpha03/material3-android-1.12.0-alpha03.pom) depends on `androidx.compose.material3:material3-android:1.5.0-alpha22` |
| `1.13.0-alpha01` | 2026-09-10 | `1.13.0-alpha01` | `1.5.0-alpha27` | [Release table](https://github.com/JetBrains/compose-multiplatform/releases/tag/v1.13.0-alpha01); [`material3-android` POM](https://repo1.maven.org/maven2/org/jetbrains/compose/material3/material3-android/1.13.0-alpha01/material3-android-1.13.0-alpha01.pom) |
| `1.13.0-alpha02` (newest prerelease) | 2026-10-06 | `1.13.0-alpha02` | `1.5.0-alpha28` | [Release table](https://github.com/JetBrains/compose-multiplatform/releases/tag/v1.13.0-alpha02); [`material3-android` POM](https://repo1.maven.org/maven2/org/jetbrains/compose/material3/material3-android/1.13.0-alpha02/material3-android-1.13.0-alpha02.pom) |

The Maven path is the one to trust when the release table is ambiguous. The JetBrains `material3` Gradle module's Android variants redirect (`available-at`) to `org.jetbrains.compose.material3:material3-android`, and that artifact's POM and module depend on Google's `androidx.compose.material3:material3` at the baseline version. The `1.12.0-alpha03` module also carried an `androidx.compose.material3:material3-lint:1.5.0-alpha22` dependency in its common metadata; the `1.13.0` alphas dropped that, so only the `material3-android` POM names the AndroidX version there. [Maven metadata](https://repo1.maven.org/maven2/org/jetbrains/compose/material3/material3/maven-metadata.xml) listed `1.13.0-alpha02` as the newest version on 2026-10-06.

On the AndroidX side, `1.5.0-alpha29` (2026-09-23) is what the starter pins, and `1.5.0-beta01` (2026-10-07) is now the current beta; `1.4.0` remains the stable release. `beta01` restores `Slider`, `VerticalSlider`, and `RangeSlider` overloads with a nullable `onValueChange` as hidden deprecated APIs, renames `HorizontalCenteredHeroCarousel.maxItemWidth` to `preferredItemWidth`, and reworks `PolygonShape` and `CornerRounding`. No JetBrains artifact corresponds to `alpha29` or `beta01`. [AndroidX Material3 releases](https://developer.android.com/jetpack/androidx/releases/compose-material3#1.5.0-beta01)

Kotlin is a second axis of the gap. The wasm-js variants of `1.12.1` require `kotlin-stdlib-wasm-js:2.3.20`, which matches the starter's Kotlin `2.3.20`. `1.13.0-alpha02` raises the minimum to Kotlin `2.4.20` for Kotlin/JS and Kotlin/Wasm (`2.4.0` for Kotlin/Native), so taking the `alpha28` baseline on web also means taking a newer Kotlin than the starter. [1.13.0-alpha02 migration notes](https://github.com/JetBrains/compose-multiplatform/releases/tag/v1.13.0-alpha02), [`ui-wasm-js:1.12.1` module](https://repo1.maven.org/maven2/org/jetbrains/compose/ui/ui-wasm-js/1.12.1/ui-wasm-js-1.12.1.module), [`material3-wasm-js:1.13.0-alpha02` module](https://repo1.maven.org/maven2/org/jetbrains/compose/material3/material3-wasm-js/1.13.0-alpha02/material3-wasm-js-1.13.0-alpha02.module)

The `1.13.0-alpha01` page lists a known web issue, `RuntimeError: memory access out of bounds`, with the workaround `ComposeUiFlags.useSnapshotCache = false`. The `1.13.0-alpha02` page has no known-issues section, which is not evidence that the issue is fixed.

### API presence in the newest JetBrains artifact

Method: the published sources jars for `material3-wasm-js:1.13.0-alpha02` (bundles `commonMain`, `skikoMain`, `webMain`, `wasmJsMain`, `nonJvmMain`) and `material3:1.12.0-alpha03` were downloaded from Maven Central and searched for the declarations the starter and the skill's [Expressive API map](../../skills/android-design/references/components.md) rely on. The [kotlinlang API reference](https://kotlinlang.org/api/compose-multiplatform/material3/androidx.compose.material3/) still documents Material3 `1.10.0` in its header, so it was used only to confirm that a [`MaterialExpressiveTheme` page](https://kotlinlang.org/api/compose-multiplatform/material3/androidx.compose.material3/-material-expressive-theme.html) exists, not for current presence. Sources: [`material3-wasm-js-1.13.0-alpha02-sources.jar`](https://repo1.maven.org/maven2/org/jetbrains/compose/material3/material3-wasm-js/1.13.0-alpha02/material3-wasm-js-1.13.0-alpha02-sources.jar), [`material3-1.12.0-alpha03-sources.jar`](https://repo1.maven.org/maven2/org/jetbrains/compose/material3/material3/1.12.0-alpha03/material3-1.12.0-alpha03-sources.jar).

| API | `1.13.0-alpha02` (AndroidX `alpha28`) | `1.12.0-alpha03` (AndroidX `alpha22`, in stable `1.12.1`) | Declared in |
| --- | --- | --- | --- |
| `MaterialExpressiveTheme` | present | present | `commonMain/.../MaterialTheme.kt` |
| `MotionScheme.expressive()` | present | present | `commonMain/.../MotionScheme.kt` |
| `expressiveLightColorScheme()` | present | present | `commonMain/.../ColorScheme.kt` |
| Emphasized typography roles (`displayLargeEmphasized` through `labelSmallEmphasized`) | present | present | `commonMain/.../Typography.kt` |
| `MaterialShapes` (sealed class) and `RoundedPolygon.toShape()` | present | present | `commonMain/.../MaterialShapes.kt` |
| `ButtonDefaults.shapesFor(Dp)`, `contentPaddingFor`, `ExtraSmall`/`Medium`/`Large`/`ExtraLargeContainerHeight` | present | present | `commonMain/.../Button.kt` |
| `ToggleButton`, `ElevatedToggleButton`, `FilledTonalToggleButton`, `OutlinedToggleButton` | present | present | `commonMain/.../ToggleButton.kt` |
| `ToggleButtonSize`, `ToggleButton(buttonSize, icon)` overloads, `ToggleButtonDefaults.shapesFor(ToggleButtonSize)`, `contentPaddingFor`, `size` | present | absent (`1.12.0-alpha03` has only `ToggleButtonDefaults.shapesFor(buttonHeight: Dp)`) | `commonMain/.../ToggleButton.kt` |
| `ButtonGroup` and `ButtonGroupDefaults.connected*ButtonShapes()` | present | present | `commonMain/.../ButtonGroup.kt` |
| `HorizontalFloatingToolbar`, `FloatingToolbarDefaults` | present | present | `commonMain/.../FloatingToolbar.kt` |
| `ShortNavigationBar`, `ShortNavigationBarItem` | present | present | `commonMain/.../ShortNavigationBar.kt` |
| `WideNavigationRail`, `WideNavigationRailItem` | present (modal rail dialog has a `skikoMain` actual) | present | `commonMain/.../WideNavigationRail.kt`, `skikoMain/.../WideNavigationRail.skiko.kt` |
| `LargeFlexibleTopAppBar`, `MediumFlexibleTopAppBar` | present | present | `commonMain/.../AppBar.kt` |
| `MediumFloatingActionButton`, `FloatingActionButtonMenu` | present | present | `commonMain/.../FloatingActionButton.kt`, `FloatingActionButtonMenu.kt` |
| `LoadingIndicator`, `ContainedLoadingIndicator` | present | present | `commonMain/.../LoadingIndicator.kt` |
| `LinearWavyProgressIndicator`, `CircularWavyProgressIndicator` | present | present | `commonMain/.../WavyProgressIndicator.kt` |
| `SegmentedListItem`, `ListItemDefaults.segmentedShapes` | present | present | `commonMain/.../ListItem.kt`, `ListItemDefaults.kt` |
| `DropdownMenuPopup`, `DropdownMenuGroup`, `MenuDefaults.groupShape`/`itemShape` | present | present | `commonMain/.../Menu.kt`, `MenuDefaults.kt` |
| `SelectableDropdownMenuItem`, `CheckableDropdownMenuItem` | present | absent (added in AndroidX `alpha27`) | `commonMain/.../Menu.kt` |
| `SplitButtonLayout` | present | present | `commonMain/.../SplitButton.kt` |
| `AppBarWithSearch`, `rememberContainedSearchBarState` | present | present | `commonMain/.../SearchBar.kt` |
| `HorizontalCenteredHeroCarousel` | present | present | `commonMain/.../carousel/Carousel.kt` |
| `Modifier.carouselParallaxScrollEffect` | present | absent (added in AndroidX `alpha28`) | `commonMain/.../carousel/CarouselParallaxScrollEffect.kt` |
| `Slider(state, ...)` with `onValueChange` | present as `onValueChange: ((Float) -> Unit)? = null`; the stateless `Slider(value, onValueChange, ...)` overload is `@Deprecated` with a `rememberSliderState` replacement | stateless overload not deprecated | `commonMain/.../Slider.kt` |

Two observations follow. First, every component the skill names exists in the stable `1.12.1` line as well as the newest alpha; what differs is signatures and defaults, not component availability. Second, in `1.13.0-alpha02` the `MaterialExpressiveTheme` function carries no `@ExperimentalMaterial3ExpressiveApi` annotation, although the annotation class still exists and other declarations use it; the 1.9.0 note that the theme itself needs the opt-in no longer describes the current artifact. Inference, not verified by compiling: the skill's `Slider(state = s, onValueChange = { ... })` form uses named arguments, so it should compile against both the `alpha28` nullable parameter and the `alpha29` required one, while positional calls written against `alpha29` may not.

### Variable fonts and `composeResources` fonts on wasmJs

Variable fonts are documented as supported on all platforms since Compose Multiplatform 1.8.2; the what's-new page names weight, width, slant, italic, and custom axes but shows no API. [Variable fonts](https://kotlinlang.org/docs/multiplatform/whats-new-compose-180.html#variable-fonts)

The axis API exists in the resources library even though the usage docs only show the three-parameter form. `components-resources:1.13.0-alpha02` declares `expect fun Font(resource: FontResource, weight, style, variationSettings: FontVariation.Settings = FontVariation.Settings(weight, style))` in `commonMain`, with the `actual` in `skikoMain`, which the wasmJs target uses; `preloadFont(resource, weight, style, variationSettings)` lives in `webMain`. The four-parameter overload was added by [PR #5183](https://github.com/JetBrains/compose-multiplatform/pull/5183) (merged 2024-12-13, YouTrack CMP-7088). [`components-resources-wasmJs-1.13.0-alpha02-sources.jar`](https://repo1.maven.org/maven2/org/jetbrains/compose/components/components-resources-wasmJs/1.13.0-alpha02/components-resources-wasmJs-1.13.0-alpha02-sources.jar), [Font resources usage](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html#fonts)

Caveats that affect judging a screenshot: web resources load asynchronously through Fetch, and the docs warn this "can cause visual glitches, such as FOUT or displaying placeholders"; text may show in the default font, as empty boxes, or as question marks until the font is ready. Two mitigations are documented: a `<link rel="preload">` tag in `index.html`, and `preloadFont()`, which gates composition until the `Font` is non-null. The web resource cache is cleared on every launch and page refresh. `preloadFont`, `preloadImageBitmap`, and `preloadImageVector` are no longer experimental as of `1.13.0-alpha02`; the resources page still describes them as experimental because it was written for 1.8.0. Since 1.12.0, unresolved glyphs trigger an on-demand Noto subset download, with tofu visible until it arrives. [Web resource loading](https://kotlinlang.org/docs/multiplatform/compose-web-resources.html), [1.12 highlights](https://kotlinlang.org/docs/multiplatform/whats-new-compose-112.html)

Not verified: the GitHub issue about variable fonts rendering as tofu on Wasm ([#4635](https://github.com/JetBrains/compose-multiplatform/issues/4635)) was closed in September 2024 and moved to YouTrack without a named fixing version. Google Sans Flex specifically has not been rendered on wasmJs in this research.

### Text rendering and density on web versus Android

JetBrains states that Compose Multiplatform "doesn't guarantee pixel-perfect correspondence between different platforms" for text: each system assigns a different default font when none is set, and "even for the same font, letter aliasing mechanisms specific to each platform can lead to noticeable differences", which "may interfere with screenshot testing". [Platform-specific behavior](https://kotlinlang.org/docs/multiplatform/compose-platform-specifics.html)

Density on web is not documented on the viewport page, so it was read from the published source. In `ui-wasm-js:1.13.0-alpha02`, `ComposeWindowInternal.web.kt` builds `Density(density = window.devicePixelRatio, fontScale = 1f)` and, on pinch-to-zoom, multiplies the density by the viewport scale coerced into `1f..2f`. Consequences for judging a proposal: the browser's device pixel ratio and zoom level decide the dp scale (a 2x display renders at density 2.0, where the starter's phone previews model 420 dpi, density 2.625), `fontScale` is fixed at 1.0 so no accessibility font scaling is simulated, and the browser clamps zoom-driven density at 2.0. The viewport page adds that `ComposeViewport` injects no CSS, so the host page must size the canvas itself or it "may not resize correctly or fill the intended space". [`ui-wasm-js-1.13.0-alpha02-sources.jar`](https://repo1.maven.org/maven2/org/jetbrains/compose/ui/ui-wasm-js/1.13.0-alpha02/ui-wasm-js-1.13.0-alpha02-sources.jar), [Setting up the viewport](https://kotlinlang.org/docs/multiplatform/compose-css-styles.html)

### MaterialKolor

The README lists "JavaScript/wasm (Browser)" among supported targets, and Maven Central publishes `material-kolor-wasm-js` variants. The latest release is `5.0.1` (2026-08-27), whose Gradle module requires `kotlin-stdlib:2.4.10` and `org.jetbrains.compose.material3:material3:1.12.0-alpha03`, so it pairs with Compose Multiplatform `1.12.1` and the AndroidX `alpha22` baseline. Earlier pairings from the modules and release notes: `5.0.0` is built against Kotlin `2.4.0` and material3 `1.11.0-alpha07`; `5.0.0-alpha07` against Kotlin `2.3.10` and material3 `1.11.0-alpha03`; `4.1.1` against Kotlin `2.3.0` and material3 `1.10.0-alpha05`, and the `5.0.0-alpha05` notes record that Expressive was removed from the 1.10.0 line, so `4.x` is not an Expressive pairing. No release is built against Kotlin `2.3.20`. `6.0.0-beta01` (2026-10-01) exists on GitHub with the warning that "some colors change even when your code compiles without changes"; it had not reached Maven Central metadata as of 2026-08-27, the last update there. Not verified: whether a klib compiled against Kotlin `2.4.10` is consumable from a Kotlin `2.3.20` project. [README](https://github.com/jordond/MaterialKolor), [Releases](https://github.com/jordond/MaterialKolor/releases), [Maven metadata](https://repo1.maven.org/maven2/com/materialkolor/material-kolor/maven-metadata.xml), [`5.0.1` module](https://repo1.maven.org/maven2/com/materialkolor/material-kolor/5.0.1/material-kolor-5.0.1.module)

### Corrections made to the older text

- Version table: added the `1.13.0-alpha02` row (AndroidX `alpha28`) and the Maven `material3-android` POM as a second source for each baseline.
- The note that "the prerelease also documents a web memory-access error" now names `1.13.0-alpha01` and records that the `alpha02` page has no known-issues section.
- The sentence on `alpha28` and `alpha29` consequences now states that the newest JetBrains artifact reaches `alpha28`, leaving `alpha29` and `beta01` as the remaining gap.
- The claim that JetBrains' API reference exposes emphasized typography roles now notes that the reference is pinned at `1.10.0`; presence was verified from the sources jar instead.
- Variable fonts: added the `variationSettings` overload and the `preloadFont` status; the sentence on asynchronous loading now cites the non-experimental preload API and the automatic Noto fallback.
- Adaptive: `1.13.0-alpha02` ships `adaptive:1.3.0` rather than `1.3.0-rc01`.
- Density: the sentence that browser width does not simulate a device's density now states what the web density actually is.
- Kotlin: added the `2.4.20` minimum for the newest prerelease, which the older text did not consider.

## What is actually shared

Compose Multiplatform extends Jetpack Compose and shares its compiler, runtime, declarative UI model, layout APIs, state handling, and animation concepts. JetBrains ports libraries for non-Android targets. Android builds resolve Google's AndroidX artifacts through Gradle metadata; other targets resolve JetBrains' implementations. Different Maven coordinates therefore do not mean a different design system. A composable using supported common APIs can be shared rather than recreated for the browser. [JetBrains: relationship to Jetpack Compose](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-and-jetpack-compose.html)

The relevant browser path is Compose UI on Kotlin/Wasm, not a separately authored HTML/CSS screen. JetBrains documents Material3 components, adaptive resizing, animations, and browser interaction as supported capabilities. This establishes feasibility, not verification of this repository's proposals. [JetBrains: Compose for web Beta](https://blog.jetbrains.com/kotlin/2025/09/compose-multiplatform-1-9-0-compose-for-web-beta/)

## Version alignment is the concrete gap

The [starter version catalog](../../evals/compose-starter/gradle/libs.versions.toml) pins AndroidX Material3 `1.5.0-alpha29`, Compose BOM `2026.03.01`, and Kotlin `2.3.20`. The releases inspected advertise these mappings:

| Compose Multiplatform release | JetBrains Material3 artifact version | AndroidX Material3 baseline |
| --- | --- | --- |
| `1.12.1` | `1.12.0-alpha03` | `1.5.0-alpha22` |
| `1.13.0-alpha01` | `1.13.0-alpha01` | `1.5.0-alpha27` |
| `1.13.0-alpha02` (added 2026-10-10) | `1.13.0-alpha02` | `1.5.0-alpha28` |

Sources: [1.12.1 component table](https://github.com/JetBrains/compose-multiplatform/releases/tag/v1.12.1), [1.13.0-alpha01 component table](https://github.com/JetBrains/compose-multiplatform/releases/tag/v1.13.0-alpha01), [1.13.0-alpha02 component table](https://github.com/JetBrains/compose-multiplatform/releases/tag/v1.13.0-alpha02). The 2026-10-10 verification confirmed each row against the `material3-android` POM on Maven Central (see the verification section). No matching `alpha29` baseline exists in any published JetBrains artifact. The `1.13.0-alpha01` page documents a web memory-access error (`RuntimeError: memory access out of bounds`, workaround `ComposeUiFlags.useSnapshotCache = false`) and the `alpha02` page carries no known-issues section; choosing a prerelease solely to get closer API alignment is not automatically the safer route.

This lag has concrete consequences. AndroidX `alpha28` changes carousel parallax and toggle-button APIs and deprecates stateless slider overloads. `alpha29` makes `onValueChange` required and changes its parameter position in slider APIs. As of 2026-10-10 the newest JetBrains artifact (`1.13.0-alpha02`) reaches `alpha28`, so the stateless slider deprecation and the sized toggle-button APIs are already in it; the remaining gap to the starter is `alpha29`, plus `beta01` (2026-10-07) beyond that. A proposal written against those newer signatures cannot be assumed to compile unchanged against the inspected multiplatform baseline. The mismatch is more specific than an unsupported general claim that Expressive is unavailable. [AndroidX Material3 release notes](https://developer.android.com/jetpack/androidx/releases/compose-material3#1.5.0-alpha29)

The earlier `org.jetbrains.compose.material3:material3:1.9.0-alpha04` example was real Expressive support, not evidence of current version parity. JetBrains explicitly documents `MaterialExpressiveTheme`, with color, motion, shape, and typography configuration. Its API reference also exposes emphasized typography roles such as `displayLargeEmphasized`, although that reference is pinned at Material3 `1.10.0`; the 2026-10-10 check verified the roles from the `1.13.0-alpha02` sources jar instead. [Expressive introduction](https://kotlinlang.org/docs/multiplatform/whats-new-compose-190.html#material-3-expressive-theme), [Typography API](https://kotlinlang.org/api/compose-multiplatform/material3/androidx.compose.material3/-typography/)

## Fit with the current starter

The source inspection found platform glue, not evidence that the proposal layout itself must be rewritten:

| Current dependency | Meaning for browser proposals |
| --- | --- |
| Android application/library Gradle modules | A web target and common source set need configuration; this is not a launch flag on the current project. |
| `AppTheme` uses `Build`, `LocalContext`, and Android dynamic-color functions | Keep Android system integration platform-specific; provide explicit proposal colors to shared UI. Do not silently swap palettes between previews. |
| Typography loads `R.font.google_sans_flex` | Reuse the bundled font, but adapt resource loading for common/web code. |
| `ProposalScreen` uses Android `stringResource` and `R.string` | Use shared resources or caller-provided proposal content. |
| Android activity, application, and Koin Android setup | These are host wiring, not transferable browser entry points. A design preview need not port the entire app's data layer. |

Local evidence: [AppTheme](../../evals/compose-starter/app/src/main/kotlin/com/example/designsandbox/theme/AppTheme.kt), [Typography](../../evals/compose-starter/app/src/main/kotlin/com/example/designsandbox/theme/Typography.kt), [ProposalScreen](../../evals/compose-starter/app/src/main/kotlin/com/example/designsandbox/ProposalScreen.kt), and [app build](../../evals/compose-starter/app/build.gradle.kts).

JetBrains explicitly identifies `LocalContext`, Android resource access, activities, and other Android APIs as platform-specific. Camera, permissions, maps, and device services are not made portable simply by sharing Compose UI. Accordingly, transferable screen structure does not imply transferable host integration. [Android-only components](https://kotlinlang.org/docs/multiplatform/compose-android-only-components.html)

## Typography, resizing, and interaction

Variable fonts are supported on all platforms from Compose Multiplatform 1.8.2, including custom variation axes. Therefore Google Sans Flex is not a documented categorical blocker. The resources library exposes the axes through `Font(resource, weight, style, variationSettings = FontVariation.Settings(...))` and the matching `preloadFont` overload (verified in the `1.13.0-alpha02` sources on 2026-10-10). The specific bundled font, selected axes, and typography setup have not been exercised in a browser here. [Variable-font support](https://kotlinlang.org/docs/multiplatform/whats-new-compose-180.html#variable-fonts)

Web fonts load asynchronously. Preloading is needed to avoid judging a temporary fallback font or missing glyphs as the proposal; `preloadFont()` is no longer experimental as of `1.13.0-alpha02`, and since 1.12.0 missing glyphs trigger an on-demand Noto download with tofu visible until it lands. The common resource API loads fonts from `composeResources/font`; the existing Android `R.font` access is not interchangeable with it. [Web resource loading](https://kotlinlang.org/docs/multiplatform/compose-web-resources.html), [Common font resources](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html#fonts)

Adaptive layout logic can live in common code. JetBrains documents `org.jetbrains.compose.material3.adaptive:adaptive:1.3.0-rc01` (`1.3.0` in `1.13.0-alpha02`) and `currentWindowAdaptiveInfoV2()`. Its older Android-only-components page still lists adaptive libraries among unavailable APIs; the dedicated adaptive guide and current release table provide more specific, newer evidence. Do not interpret that older list as a blanket absence of adaptive support. [Adaptive layouts](https://kotlinlang.org/docs/multiplatform/compose-adaptive-layouts.html)

Inference: a live proposal can remeasure and recompose as its viewport changes, replacing repeated static captures for inspecting responsive layouts. This is different from stretching a screenshot. But browser width does not simulate Android system bars, keyboard insets, fold posture, or a device's density automatically. On web the density is the browser's `devicePixelRatio` (times pinch zoom, clamped at 2.0) with `fontScale` fixed at 1.0, not a modeled 420 dpi phone (verified from the `ui-wasm-js:1.13.0-alpha02` source on 2026-10-10). Those are separate environmental assumptions. Shared interaction code can demonstrate selection, scrolling, and motion; browser behavior is not proof of Android system gesture or IME behavior.

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
