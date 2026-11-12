# Compose starter

This is the Compose sandbox for the manual export check in `evals/README.md`. Copy it to a scratch folder and drop the previewer app's exported Kotlin for an approved proposal into it. Proposals live in `design/*.proposal.json` with a `DESIGN.md`, not here. Develop in the copy, not here or in production app code. This is an executable sandbox, not a production app template or a claim of integration compatibility.

## Theme

`AppTheme` in `app/.../theme/AppTheme.kt` takes the same inputs as a proposal's `DESIGN.md` scheme, so porting a theme is a lookup:

| `AppTheme` parameter | `DESIGN.md` key | Notes |
| --- | --- | --- |
| `seed: Color` | `colors.primary` | Default `0xFF6750A4`. |
| `style: PaletteStyle` | `material.scheme.variant` | Default `PaletteStyle.TonalSpot`. |
| `darkTheme: Boolean` | none (the `dark` view) | Default follows the system. |
| `dynamic: Boolean` | none | Default `false`. When true on API 31+, uses wallpaper colors instead of the seed. |

`theme/Typography.kt` also has `voiceFont(R.font.<file>, weight, axes...)` for a `DESIGN.md` typography entry's font: pass every axis its `fontVariation` names (`opsz`, `SOFT`, `WONK`, `wdth`), because Compose sets only the axes it is given. Its `*Emphasized` styles equal the baseline roles in this material3 version, so copy a baseline role with the emphasized token weight instead (see `skills/android-design-explore/references/compose-mapping.md`, type roles).

The scheme comes from MaterialKolor (`rememberDynamicColorScheme`, spec 2025) with Material 3 Expressive motion and the bundled Google Sans Flex typography. The starter ships no icons: a port imports each Material Symbol it uses as a vector drawable into `app/src/main/res/drawable/` and draws it with `Icon(painterResource(...))`, as the Icons section of `skills/android-design-explore/references/compose-mapping.md` describes.
## Entry points

- `app/src/main/kotlin/com/example/designsandbox/ProposalScreen.kt`: phone/tablet proposal with sample state.
- `app/.../theme/`: the Expressive theme, with bundled Google Sans Flex.
- `app/src/screenshotTest/kotlin/` contains fixed `@PreviewTest` entry points for starter maintenance.
- `core/strings` holds shared text; `core/designsystem/common` holds fonts and other non-string resources. The remaining core modules preserve `android-dev` boundaries without sample business logic.

The app starts Koin and has a single Navigation 3 destination. It also depends on `material3-adaptive-navigation-suite`, because an export of a proposal with `navigation.destinations` wraps its `NavDisplay` in a `NavigationSuiteScaffold`. It depends on `adaptive-layout` and `adaptive-navigation3` too, which an export with list-detail or supporting panes imports (`calculatePaneScaffoldDirective`, `rememberListDetailSceneStrategy`). Previews call the screen with sample state directly, without starting navigation, DI, databases, or network requests. Separate composables can hold alternatives in the same module. The `android-dev` skill is needed only if you choose to generate further modules, not to use this starter.

## Device previews

`ProposalScreen.kt` contains one adaptive sample and four named preview wrappers. Keep the same data and theme across a proposal's wrappers. The explicit logical canvases are:

| Preview | Logical canvas | Density | Provenance |
| --- | --- | --- | --- |
| Pixel 11 | 411 x 923 dp | 420 dpi | Modeled density from Pixel 10, with Pixel 11's 1080 x 2424 physical panel. Not a verified factory density. |
| Pixel Tablet | 1280 x 800 dp | 320 dpi | Android Studio `pixel_tablet` profile, landscape, xhdpi. |
| Desktop 1080p | 1920 x 1080 dp | 160 dpi | Representative 1080p desktop display at 1x, chosen so the desktop canvas reaches the extra large window size class (1600dp and up) instead of sharing the tablet's large class. |
| Z Fold8 non-Ultra | 704 x 933 dp | 420 dpi | Modeled density, main display held upright, based on the 2448 x 1848 retail specification. Portrait is the medium window size class, the only canvas here between the phone and the expanded tablet. |

These are full content-only preview canvases, with no simulated system UI. Do not subtract guessed status-bar or taskbar heights. Insets, folds, input, and motion need separate device validation. Rounding to whole dp can shift output pixels slightly from panel resolution. Use explicit `dp` device specs: the verified Compose Preview CLI 2.33.0 render treated pixel-unit dimensions as the default 400 x 800 dp canvas instead of the requested dimensions.

Device evidence: [Pixel 11 specs](https://store.google.com/product/pixel_11_specs?hl=en-US), [Pixel Tablet specs](https://support.google.com/googlepixeltablet/answer/13555146?hl=en-CA), and [Fold8 retail specs](https://www.samsung.com/uk/business/smartphones/galaxy-z/galaxy-z-fold8-lavender-256gb-sm-f971blvbeub/). Samsung's developer skin listing differs from the retail width; these previews use the retail dimensions and do not claim OEM emulator fidelity.

## Starter maintenance checks

Requires JDK 17+, Android SDK platform 37, and initial network access to resolve pinned dependencies. Gradle finds the SDK through `ANDROID_HOME` or `sdk.dir` in `local.properties`; on macOS it is usually `~/Library/Android/sdk`. Dependencies and Gradle caches are machine-level tooling, not bundled in the skill.

```bash
./gradlew projects
./gradlew :app:assembleDebug
rm -rf app/src/screenshotTestDebug/reference
./gradlew --console=plain :app:updateDebugScreenshotTest
./gradlew spotlessApply spotlessCheck
```

`updateDebugScreenshotTest` renders the `@PreviewTest` entry points without an emulator and writes one PNG per preview under `app/src/screenshotTestDebug/reference/`. Delete that folder first, as above, so a failed run cannot leave the previous render in place. Open the PNGs to inspect the result. Add matched light/dark, smaller-window, and large-font previews when evaluating designs. Device behavior and integration still need testing in the real project.

## Provenance and maintenance

Generated once using `android create empty-activity` and `android-dev`'s `bootstrap.sh` and `generate.sh app`. Catalog aliases were merged, themes/fonts and navigation completed, and the existing eval screenshot approach adapted. The export check copies the result without invoking those generators.

Keep this starter's dependencies explicitly pinned. When updating it, build the app, render its screenshot entry point, inspect the PNGs, and run Spotless in a disposable copy. Keep build outputs, local SDK paths, caches, and renders out of this sandbox's committed files.

Unmodified fonts come from the official [Google Fonts repository](https://github.com/google/fonts) (`main` branch). Their redistribution licenses are in `licenses/`. Google Sans Flex is the default typography; the other faces are a curated set an exploration may use for a direction (brand faces on Display and Headline only, Doto at Display sizes only, Google Sans Code for numbers and data), and are not referenced by the starter's `Typography`.

| File in `res/font/` | Source (`https://raw.githubusercontent.com/google/fonts/main/ofl/...`) | License in `licenses/` |
| --- | --- | --- |
| `google_sans_flex.ttf` | `googlesansflex/` (variable TTF) | `google-sans-flex-OFL.txt` |
| `fraunces.ttf` | `fraunces/Fraunces[SOFT,WONK,opsz,wght].ttf` | `fraunces-OFL.txt` (`fraunces/OFL.txt`) |
| `bricolage_grotesque.ttf` | `bricolagegrotesque/BricolageGrotesque[opsz,wdth,wght].ttf` | `bricolage-grotesque-OFL.txt` (`bricolagegrotesque/OFL.txt`) |
| `big_shoulders_display.ttf` | `bigshouldersdisplay/BigShouldersDisplay[wght].ttf` | `big-shoulders-display-OFL.txt` (`bigshouldersdisplay/OFL.txt`) |
| `doto.ttf` | `doto/Doto[ROND,wght].ttf` | `doto-OFL.txt` (`doto/OFL.txt`) |
| `google_sans_code.ttf` | `googlesanscode/GoogleSansCode[wght].ttf` | `google-sans-code-OFL.txt` (`googlesanscode/OFL.txt`) |

The previewer app bundles identical copies of these TTFs in `app/features/preview/desktop/src/main/resources/fonts/`. `scripts/test_font_copies.py` fails when the two copies differ, so update both together.
