# Color variant provenance

Research date: 2026-09-28. This note records newly checked official prose and implementation evidence online, not additions to the generated `sources/` corpus. It does not change skill guidance or establish that the repository's saved-source requirement has been fulfilled.

## Official prose explains the variants

These concepts are not documented only in code. [Android Developers: How color variants display on a user's device](https://developer.android.com/develop/ui/views/theming/dynamic-colors) explicitly identifies Tonal Spot with Android 12, and Neutral, Vibrant Tonal, and Expressive with Android 13. It explains that variants transform wallpaper seeds using different vibrancy and hue-rotation recipes while apps retain the same color tokens. Its Pixel Wallpaper & style example makes the user-facing connection explicit.

[AOSP: Theme styles for dynamic color](https://source.android.com/docs/core/display/dynamic-color) describes six Android 13 styles: `TONAL_SPOT`, `VIBRANT`, `EXPRESSIVE`, `SPRITZ`, `RAINBOW`, and `FRUIT_SALAD`. It characterizes Tonal Spot as mid-vibrancy, Vibrant and Expressive as high-vibrancy treatments, and Spritz as low-vibrancy. Rainbow and Fruit Salad are recommended for static themes rather than wallpaper extraction. Tonal Spot is the default; manufacturers need not expose every style. The FAQ specifies wallpaper-derived seeds from `ColorScheme#getSeedColors`, which can provide several candidates.

Do not silently equate AOSP's `SPRITZ` with MCU's `NEUTRAL`: these pages use different names, and an exact implementation mapping was not verified here. Likewise, a phone screenshot with palette swatches establishes the personalization concept, not the exact style or seed behind each swatch. Both prose pages were checked live on the research date; they are not immutable historical snapshots.

## Findings

| Question | Verified evidence | Conclusion |
|---|---|---|
| Did MaterialKolor invent these names? | Its [pinned README](https://github.com/jordond/MaterialKolor/blob/ddebf5c8279f96bf418acea17589be3b432d43df/README.md#inspiration) credits Google's Material Color Utilities as the core Java-to-Kotlin Multiplatform port, with Compose ideas from `m3color`. The [mapping implementation](https://github.com/jordond/MaterialKolor/blob/ddebf5c8279f96bf418acea17589be3b432d43df/material-kolor/src/commonMain/kotlin/com/materialkolor/internal/Util.kt#L6-L17) explicitly maps `PaletteStyle` to the port's `Variant` constants. | MaterialKolor exposes existing variant concepts through a wrapper enum. Its documentation is primary evidence for its own wrapper, not a Google design mandate. |
| Are variants linked to the 2025 color update? | Google's [2025-03-25 MCU commit](https://dart.googlesource.com/external/github.com/material-foundation/material-color-utilities/+/e98f6b83f3a4f4afe4c2c0021a85566ee4a36fca) says: “Added 2025 color specs for Neutral, Tonal spot, Expressive, and Vibrant across multiple color roles.” It adds `ColorSpec2021` and `ColorSpec2025`. | Four named variants explicitly received 2025 color-spec work. The variants and spec version are separate concepts. This is not evidence that the names originated in 2025. |
| Is there pre-2025 evidence? | The [AOSP May 2022 merge diff](https://android.googlesource.com/platform/frameworks/base/+/f587a703ec5f27073b811e9e1633bfeb2f626174%5E2..f587a703ec5f27073b811e9e1633bfeb2f626174/) includes `Style.TONAL_SPOT`, `Style.VIBRANT`, and `Style.EXPRESSIVE` in Monet-related code and tests. | These names existed before the M3 Expressive update. This bounds their history; it does not identify the first invention date, nor prove historical/current implementations identical. |
| Does MaterialKolor always choose the newest color spec? | [Public color-scheme functions](https://github.com/jordond/MaterialKolor/blob/ddebf5c8279f96bf418acea17589be3b432d43df/material-kolor/src/commonMain/kotlin/com/materialkolor/DynamicColorScheme.kt#L42-L44) default to `TonalSpot` and `SpecVersion.Default`; [ColorSpec](https://github.com/jordond/MaterialKolor/blob/ddebf5c8279f96bf418acea17589be3b432d43df/material-color-utilities/src/commonMain/kotlin/com/materialkolor/dynamiccolor/ColorSpec.kt#L15-L22) defines that default as `SPEC_2021`. | Ordinary generation must explicitly request `SPEC_2025` when that behavior is intended. Do not infer spec version from a recent dependency version. |
| Does its Expressive wrapper use different defaults? | [DynamicMaterialExpressiveTheme](https://github.com/jordond/MaterialKolor/blob/ddebf5c8279f96bf418acea17589be3b432d43df/material-kolor/src/commonMain/kotlin/com/materialkolor/DynamicMaterialExpressiveTheme.kt#L47-L62) defaults to `PaletteStyle.Expressive` and `SPEC_2025`. Its README recommends that combination. | The wrapper deliberately couples two separately configurable options. Its defaults do not prove that every M3 Expressive interface must use the Expressive palette variant. |

## Exact mapping and parameter flow

At MaterialKolor commit `ddebf5c8279f96bf418acea17589be3b432d43df`, `material-kolor/src/commonMain/kotlin/com/materialkolor/internal/Util.kt` maps:

| Wrapper name | Port enum constant |
|---|---|
| `TonalSpot` | `TONAL_SPOT` |
| `Neutral` | `NEUTRAL` |
| `Vibrant` | `VIBRANT` |
| `Expressive` | `EXPRESSIVE` |

The same function also maps Rainbow, FruitSalad, Monochrome, Fidelity, and Content. Consequently, the four variants under discussion are not the complete enum.

[Color.toDynamicScheme](https://github.com/jordond/MaterialKolor/blob/ddebf5c8279f96bf418acea17589be3b432d43df/material-kolor/src/commonMain/kotlin/com/materialkolor/ktx/DynamicScheme.kt) dispatches each style to its corresponding scheme class, forwarding dark mode, contrast, spec version, and platform. [SchemeTonalSpot](https://github.com/jordond/MaterialKolor/blob/ddebf5c8279f96bf418acea17589be3b432d43df/material-color-utilities/src/commonMain/kotlin/com/materialkolor/scheme/SchemeTonalSpot.kt) passes `Variant.TONAL_SPOT` through the selected `ColorSpecs` implementation. This demonstrates that scheme style and generation spec are independently passed choices, not synonyms.

## Current versus historical evidence

The MaterialKolor files above are pinned to the then-current `main` commit, not assumed identical to a released artifact. GitHub's latest release endpoint returned [5.0.1](https://github.com/jordond/MaterialKolor/releases/tag/5.0.1), published 2026-08-27. No release-by-release behavior history was established.

The March 2025 Google commit is historical implementation evidence. It is stronger than interpreting the generic phrase “vibrant color schemes” in design prose as an enum reference. Historical presence of a variant name does not guarantee stable formulas, hue rotation, or chroma across spec versions.

## Missing saved evidence and recommendation

The existing committed design-page corpus establishes source colors, palettes, role mapping, contrast, and expressive design tactics, but does not preserve the newly checked prose pages or implementation files above. Official prose can support the general variant explanation; pinned code is additionally needed for exact API mapping and spec-version behavior. Before turning this note into skill rules, agree how to capture these official sources with licensing and provenance, without hand-editing the generated `sources/` directory. MaterialKolor evidence can establish wrapper API behavior only. Attribute design guidance to Google and wrapper defaults to MaterialKolor separately.

Bounded proposed wording: Material 3 Expressive is the broader design evolution. Named color variants choose palette behavior; the color-spec version chooses generation rules. MaterialKolor exposes both and has different defaults for ordinary generation and its Expressive theme wrapper. Choosing the Expressive variant alone does not establish Expressive typography, components, shape, or motion.
