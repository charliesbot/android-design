# Wear starter

This is the Wear OS sandbox for the manual export check in `evals/README.md`. Copy it to a scratch folder and drop the previewer app's exported Kotlin for an approved Wear proposal into it. Develop in the copy, not here. It is an executable sandbox, not a production app template.

## Where exported code goes

1. Replace `app/src/main/kotlin/com/example/wearsandbox/ProposalScreen.kt` with the exported Kotlin (it declares its own `WearAppTheme`), after changing its `package` to `com.example.wearsandbox`. That is the only edit the export needs; anything else it needs is an exporter bug.
2. Point `MainActivity.kt` at the export's top-level composable (the exporter names it after the proposal).
3. In `app/src/screenshotTest/kotlin/.../ProposalPreviews.kt`, add one `@PreviewTest` per exported screen function, like the placeholder's. It carries the previewer's two views, both round on black:

| Preview | Device spec |
| --- | --- |
| watch | `spec:width=204dp,height=204dp,dpi=320,isRound=true` |
| watch-large | `spec:width=227dp,height=227dp,dpi=320,isRound=true` |

A screen with the time text off needs `Frame(clock = false)`: its `timeText = {}` takes effect only inside the navigation host, so an isolated preview would still show the app clock.

The starter ships no fonts or icons. Text uses the layoutlib system font, so label weights look lighter than the previewer's bundled Roboto Flex. Import each Material Symbol the export names (its header lists them; a `_filled` suffix is Fill 1) as a vector drawable into `app/src/main/res/drawable/`, as the Icons section of `skills/android-design-explore/references/compose-mapping.md` describes. The exporter lists an `Image` as a drawable too: add a bitmap or vector for it.

## Render

Requires JDK 17+, Android SDK platform 37.1, and initial network access to resolve pinned dependencies. Gradle finds the SDK through `ANDROID_HOME` or `sdk.dir` in `local.properties`; on macOS it is usually `~/Library/Android/sdk`.

```bash
rm -rf app/src/screenshotTestDebug/reference && ./gradlew --console=plain :app:updateDebugScreenshotTest
```

This writes one PNG per preview under `app/src/screenshotTestDebug/reference/` (408 and 454 px square at 320 dpi), without an emulator. The clock is live, so the time text differs between renders.

## Versions

The toolchain matches `evals/compose-starter/` (Gradle 9.6, AGP 9.4.1, Kotlin 2.3.20, Compose Preview screenshot plugin 0.0.1-alpha16). Wear Compose (`compose-material3`, `compose-foundation`, `compose-navigation3`) is 1.8.0-alpha03, with Navigation 3 and kotlinx.serialization for the export's back stack, the version the previewer's port compiles (`app/wear/compose/upstream/VERSION`). That release pulls Compose 1.13 beta, so `compileSdk` is 37.1; `minSdk` is 30. Keep these pinned, and keep build outputs, `local.properties`, and renders out of committed files.
