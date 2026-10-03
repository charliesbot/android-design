# Compose exploration starter

Copy this project with the installed skill's `scripts/explore.py create`. Develop in that temporary copy, not here or in the user's app. This is an executable design sandbox, not a production app template or a claim of integration compatibility.

## Entry points

- `app/src/main/kotlin/com/example/designsandbox/ProposalScreen.kt`: phone/tablet proposal with sample state.
- `wear/src/main/kotlin/com/example/designsandbox/wear/ProposalScreen.kt`: experimental watch proposal.
- Each shell's `theme/`: independent phone Expressive and Wear Material 3 themes. Phone uses bundled Google Sans Flex; Wear uses bundled Roboto Flex.
- For exploration, add named `@Preview` functions alongside the proposal code in the requested shell's main source set and render through the installed `compose-preview` skill.
- Each shell's `src/screenshotTest/kotlin/` contains fixed `@PreviewTest` entry points for starter maintenance, separate from the exploration workflow.
- `core/strings` holds shared text; `core/designsystem/common` holds fonts and other non-string resources. The remaining core modules preserve `android-dev` boundaries without sample business logic.

Both shells start Koin and have a single Navigation 3 destination. Previews call the screen with sample state directly, without starting navigation, DI, databases, or network requests. Separate composables can hold alternatives inside the same shell. The `android-dev` skill is needed only if you choose to generate further modules, not to use this starter.

## Exploration rendering

Assume the `compose-preview` CLI and skill are installed, as documented in the repository README. Follow that skill to render the named previews in this temporary copy and inspect its returned image paths. Add matched light/dark, smaller-window, and large-font previews. Report rendering failures instead of switching to the maintenance harness.

## Starter maintenance checks

Requires Python 3, JDK 17+, Android SDK platform 37, and initial network access to resolve pinned dependencies. Set `ANDROID_HOME` or `ANDROID_SDK_ROOT` if the SDK is not in the standard macOS location. Dependencies and Gradle caches are machine-level tooling, not bundled in the skill.

```bash
./gradlew projects
./gradlew :app:assembleDebug :wear:assembleDebug
python3 render.py app
python3 render.py wear
./gradlew spotlessApply spotlessCheck
```

The maintenance renderer clears that surface's previous generated screenshots before running Gradle, then prints absolute PNG paths under `renders/<surface>/`. Open them to inspect the result. Add matched light/dark, smaller-window, and large-font previews when evaluating designs. Device behavior and integration still need testing in the real project.

Wear builds and screenshots are scaffold checks only. Its design guidance remains experimental; including this shell does not unpark the design evals. Widgets and complications are intentionally absent.

## Provenance and maintenance

Generated once using `android create empty-activity` and `android-dev`'s `bootstrap.sh`, `generate.sh app`, and `generate.sh wear`. Catalog aliases were merged, themes/fonts and navigation completed, and the existing eval screenshot approach adapted for exploration. Consumers copy the result without invoking those generators.

Keep this starter's dependencies explicitly pinned. When updating it, build both shells, render both screenshot entry points, inspect the PNGs, and run Spotless in a disposable copy. Keep build outputs, local SDK paths, caches, and exploration results out of the installed template.

Unmodified fonts come from the official [Google Fonts repository](https://github.com/google/fonts): `ofl/googlesansflex` and `ofl/robotoflex`. Their redistribution licenses are in `licenses/`.
