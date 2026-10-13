# Compose starter

Copy this project with the installed skill's `gallery/cli.mjs create --starter android`. Use it to implement an approved HTML proposal as a Compose screen, and for the Wear OS shell. Exploration itself starts from `assets/proposal-starter`, not here. Develop in the returned copy's `project/` directory, not here or in production app code. This is an executable sandbox, not a production app template or a claim of integration compatibility.

## Theme

`AppTheme` in `app/.../theme/AppTheme.kt` takes the same inputs as the HTML runtime's `<m3e-theme>`, so porting a proposal's theme is a lookup:

| `AppTheme` parameter | `<m3e-theme>` attribute | Notes |
| --- | --- | --- |
| `seed: Color` | `color` | Default `0xFF6750A4`. |
| `style: PaletteStyle` | `variant` | Default `PaletteStyle.TonalSpot`. |
| `darkTheme: Boolean` | `scheme` | Default follows the system. |
| `dynamic: Boolean` | none | Default `false`. When true on API 31+, uses wallpaper colors instead of the seed. |

The scheme comes from MaterialKolor (`rememberDynamicColorScheme`, spec 2025) with Material 3 Expressive motion and the bundled Google Sans Flex typography. `material-icons-extended` is included so every runtime icon has an `Icons.Rounded.*` counterpart.
## Entry points

- `app/src/main/kotlin/com/example/designsandbox/ProposalScreen.kt`: phone/tablet proposal with sample state.
- `wear/src/main/kotlin/com/example/designsandbox/wear/ProposalScreen.kt`: experimental watch proposal.
- Each shell's `theme/`: independent phone Expressive and Wear Material 3 themes. Phone uses bundled Google Sans Flex; Wear uses bundled Roboto Flex.
- For exploration, add named `@Preview` functions alongside the proposal code in the requested shell's main source set and render through the installed `compose-preview` skill.
- Each shell's `src/screenshotTest/kotlin/` contains fixed `@PreviewTest` entry points for starter maintenance, separate from the exploration workflow.
- `core/strings` holds shared text; `core/designsystem/common` holds fonts and other non-string resources. The remaining core modules preserve `android-dev` boundaries without sample business logic.

Both shells start Koin and have a single Navigation 3 destination. Previews call the screen with sample state directly, without starting navigation, DI, databases, or network requests. Separate composables can hold alternatives inside the same shell. The `android-dev` skill is needed only if you choose to generate further modules, not to use this starter.

## Exploration rendering

Assume the `compose-preview` CLI and skill are installed, as documented in the repository README. Follow that skill to render the named previews in the exploration copy and inspect its returned image paths. Add matched light/dark, smaller-window, and large-font previews. Report rendering failures instead of switching to the maintenance harness.

## Device previews

`ProposalScreen.kt` contains one adaptive sample and four named preview wrappers. Keep the same data and theme across a proposal's wrappers. The explicit logical canvases are:

| Preview | Logical canvas | Density | Provenance |
| --- | --- | --- | --- |
| Pixel 11 | 411 x 923 dp | 420 dpi | Modeled density from Pixel 10, with Pixel 11's 1080 x 2424 physical panel. Not a verified factory density. |
| Pixel Tablet | 1280 x 800 dp | 320 dpi | Android Studio `pixel_tablet` profile, landscape, xhdpi. |
| Desktop 1080p | 1920 x 1080 dp | 160 dpi | Representative 1080p desktop display at 1x, chosen so the desktop canvas reaches the extra large window size class (1600dp and up) instead of sharing the tablet's large class. |
| Z Fold8 non-Ultra | 933 x 704 dp | 420 dpi | Modeled density, landscape main-display orientation, based on the 2448 x 1848 retail specification. Landscape reads as an open foldable and reaches the expanded window size class. |

These are full content-only preview canvases, with no simulated system UI. Do not subtract guessed status-bar or taskbar heights. Insets, folds, input, and motion need separate device validation. Rounding to whole dp can shift output pixels slightly from panel resolution. Use explicit `dp` device specs: the verified Compose Preview CLI 2.33.0 render treated pixel-unit dimensions as the default 400 x 800 dp canvas instead of the requested dimensions.

Device evidence: [Pixel 11 specs](https://store.google.com/product/pixel_11_specs?hl=en-US), [Pixel Tablet specs](https://support.google.com/googlepixeltablet/answer/13555146?hl=en-CA), and [Fold8 retail specs](https://www.samsung.com/uk/business/smartphones/galaxy-z/galaxy-z-fold8-lavender-256gb-sm-f971blvbeub/). Samsung's developer skin listing differs from the retail width; these previews use the retail dimensions and do not claim OEM emulator fidelity.

## Starter maintenance checks

Requires Node.js 22+, JDK 17+, Android SDK platform 37, and initial network access to resolve pinned dependencies. Set `ANDROID_HOME` or `ANDROID_SDK_ROOT` if the SDK is not in the standard macOS location. Dependencies and Gradle caches are machine-level tooling, not bundled in the skill.

```bash
./gradlew projects
./gradlew :app:assembleDebug :wear:assembleDebug
node render.mjs app
node render.mjs wear
./gradlew spotlessApply spotlessCheck
```

The maintenance renderer clears that surface's previous generated screenshots before running Gradle, then prints absolute PNG paths under `renders/<surface>/`. Open them to inspect the result. Add matched light/dark, smaller-window, and large-font previews when evaluating designs. Device behavior and integration still need testing in the real project.

Wear builds and screenshots are scaffold checks only. Its design guidance remains experimental; including this shell does not unpark the design evals. Widgets and complications are intentionally absent.

## Provenance and maintenance

Generated once using `android create empty-activity` and `android-dev`'s `bootstrap.sh`, `generate.sh app`, and `generate.sh wear`. Catalog aliases were merged, themes/fonts and navigation completed, and the existing eval screenshot approach adapted for exploration. Consumers copy the result without invoking those generators.

Keep this starter's dependencies explicitly pinned. When updating it, build both shells, render both screenshot entry points, inspect the PNGs, and run Spotless in a disposable copy. Keep build outputs, local SDK paths, caches, and exploration results out of the installed template.

Unmodified fonts come from the official [Google Fonts repository](https://github.com/google/fonts): `ofl/googlesansflex` and `ofl/robotoflex`. Their redistribution licenses are in `licenses/`.
