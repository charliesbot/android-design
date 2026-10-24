# Theming

Theme setup for Material 3 Expressive in Compose: versions, color schemes, typography, shapes, motion, and spacing. API names were checked against androidx sources (material3 1.5.0-alpha28) in September 2026.

## Versions

- `androidx.compose.material3:material3`: Expressive requires **1.5.0-alpha** or later. Stable 1.4.0 only has `ShortNavigationBar`, `WideNavigationRail`, and `ModalWideNavigationRail` from this skill.
- Toolchain: 1.5.0-alpha29 (and Wear Compose 1.7) require **compileSdk 37 and AGP 9.1+**; AGP 9.4 in turn needs Gradle 9.6. The `android create` template ships AGP 9.0.1, so raise these first or the build fails on dependency metadata checks.
- Check the newest version in [Maven metadata](https://dl.google.com/dl/android/maven2/androidx/compose/material3/material3/maven-metadata.xml) before adding it; alpha APIs still change (for example `Slider`'s `onValueChange` became required on main after alpha28, so pass it by name).
- Still `@ExperimentalMaterial3ExpressiveApi`: `MaterialShapes`, `RoundedPolygon.toShape()` / `toPath()`, `Morph.toPath()`, `LoadingIndicator`, `ContainedLoadingIndicator`, the pull-to-refresh loading indicator. Still `@ExperimentalMaterial3Api`: `AppBarWithSearch`, `carouselParallaxScrollEffect`, sheets, tooltips.
- Shape morphing uses `androidx.graphics:graphics-shapes`, which material3 already exposes.

## Theme

```kotlin
@Composable fun MaterialExpressiveTheme(
    colorScheme: ColorScheme? = null,
    motionScheme: MotionScheme? = null,
    shapes: Shapes? = null,
    typography: Typography? = null,
    content: @Composable () -> Unit,
)
```

- At the top level, nulls become `expressiveLightColorScheme()`, `MotionScheme.expressive()`, `Shapes()`, and `Typography()`. Nested, nulls inherit from the outer theme.
- `expressiveLightColorScheme()` is `lightColorScheme()` with more colorful `on*Container` roles. There is no dark counterpart; use `darkColorScheme()` or a generated scheme.
- Plain `MaterialTheme(...)` still works but defaults to `MotionScheme.standard()`.

## Color schemes

Pick one source per app, plus at most one content-based scheme per screen.

### Dynamic (wallpaper)

`dynamicLightColorScheme(context)` / `dynamicDarkColorScheme(context)` on API 31+. Google recommends it. Always provide a fallback scheme for older devices.

### Brand (seed color)

Compose has no seed or variant API. Two options:

1. **Static export:** generate light and dark schemes in Material Theme Builder from the brand color (or several: primary, secondary, tertiary, neutral) and paste the `lightColorScheme(...)` / `darkColorScheme(...)` values into the theme. Turn on "match color" (color fidelity) when the brand color must appear as-is.
2. **Runtime generation** with the third-party [MaterialKolor](https://github.com/jordond/MaterialKolor) library (`com.materialkolor:material-kolor`; check its latest version):

```kotlin
val colorScheme = rememberDynamicColorScheme(
    seedColor = BrandColor,
    isDark = dark,
    specVersion = ColorSpec.SpecVersion.SPEC_2025,
    style = PaletteStyle.TonalSpot, // Material's default; Fidelity when the brand color must appear as-is
    contrastLevel = rememberSystemContrast(), // see Contrast levels below
)
```

Keep the generator's default style. `TonalSpot` is what Material and Theme Builder generate; `Fidelity` keeps tones close to the seed, like Theme Builder's "match color." The other styles are scheme variants, not M3 Expressive: `Vibrant` pushes primary chroma to the maximum, and `Expressive` shifts hues away from the seed (in testing, a teal brand gained brown and orange roles) and tints surfaces. Palette variants are color-generation choices, not levels of M3 Expressive. For more color on a screen, use content-based color or give accent roles to more elements that have a job.

A common pattern: brand scheme by default, dynamic color as a user setting.

### Content-based

Color a contained area from an image on screen (a media player from its album art, cards from their photos). Keep the source image visible where its color is used, limit a screen to two scheme sources, and never recolor semantic colors.

With MaterialKolor, extract a seed from the image and theme just that subtree:

```kotlin
val seed = remember(art) { art.themeColor(fallback = BrandColor) } // com.materialkolor.ktx, on an ImageBitmap
val scheme = rememberDynamicColorScheme(seedColor = seed, isDark = isSystemInDarkTheme(), style = PaletteStyle.Content, contrastLevel = rememberSystemContrast())
MaterialExpressiveTheme(colorScheme = scheme) { NowPlaying(...) }
```

A nested theme changes `MaterialTheme.colorScheme` but not `LocalContentColor`, so plain `Icon` and `Text` keep the outer scheme's color (black icons on a dark player). Draw the themed subtree inside a `Surface(color = MaterialTheme.colorScheme.surface)`, which sets the matching content color.

Decode a small thumbnail for extraction, not the full image. MaterialKolor 5.x is built with Kotlin 2.4; if it fails to resolve against your Kotlin or Compose versions, pin the latest version that does (4.x for older Kotlin).

### Roles

| Role | Use |
| --- | --- |
| `primary` / `onPrimary` | The most important action and active states |
| `primaryContainer` / `onPrimaryContainer` | Standout fills (FAB, selected hero element) |
| `secondary`, `secondaryContainer` | Supporting controls, selection, tonal buttons, filter chips |
| `tertiary`, `tertiaryContainer` | Contrasting accents: status, badges, progress, delight; vibrant menus (vibrant toolbars use `primaryContainer`) |
| `error`, `errorContainer` | Errors (static in every scheme) |
| `surface`, `surfaceContainerLowest` to `surfaceContainerHighest` | Body background, then nested containers by emphasis; nav areas use `surfaceContainer` |
| `onSurface`, `onSurfaceVariant` | Default text; lower-emphasis text and icons |
| `outline`, `outlineVariant` | Text field borders; dividers and decorative lines |
| `inverseSurface`, `inverseOnSurface`, `inversePrimary` | Snackbars and reversed elements |
| `primaryFixed`, `primaryFixedDim` (and secondary, tertiary) | Same tone in light and dark themes |

Pairing and no-alpha rules live in [SKILL.md section 4](../SKILL.md#4-color). Material itself uses opacity only for state layers (built into components), disabled content (38%), and the `scrim` behind modals. `outlineVariant` is for dividers and decorative lines only, never text or icons.

### Extra colors (semantic and categories)

For a Success green, or categories beyond the two category-safe accent containers, Material's answer is a **static color**: one seed that produces four roles (color, on-color, container, on-container) following the same pairing rules. Generate them at runtime so they follow light, dark, and contrast:

```kotlin
@Immutable
data class ExtraColor(val color: Color, val onColor: Color, val container: Color, val onContainer: Color)

@Composable
fun rememberExtraColor(seed: Color, harmonize: Boolean = true): ExtraColor {
    val scheme = MaterialTheme.colorScheme
    val source = if (harmonize) scheme.harmonizeWithPrimary(seed) else seed // com.materialkolor.ktx
    val generated = rememberDynamicColorScheme(
        seedColor = source,
        isDark = scheme.surface.luminance() < 0.5f,
        style = PaletteStyle.TonalSpot,
        contrastLevel = rememberSystemContrast(),
    )
    return ExtraColor(generated.primary, generated.onPrimary, generated.primaryContainer, generated.onPrimaryContainer)
}
```

- Harmonizing shifts the hue slightly toward the scheme's primary while keeping its meaning (a red stays red). Skip it when the color is literal (a brand color, transit line colors) or must stay distinguishable: pass `harmonize = false` for categories, and pick seeds far apart in hue.
- Contrast for colors outside the scheme roles (category seeds, chart series, widget art): HCT tone difference is the contrast rule. "Smaller elements (less than ¼" or 40 dp) require a tone difference of 50 with their background, larger elements require a tone difference of 40." (Google)
- Use one role per category across a screen: `color` with an `onColor` glyph when the category also appears in a chart, or `container` with `onContainer` when it only marks badges. Category rules: SKILL.md section 4.

### Contrast levels

Users pick standard, medium, or high contrast in system settings (Android 14+). Dynamic schemes follow it automatically; hand-made and generated schemes don't. For generated schemes, pass the system value to MaterialKolor:

```kotlin
@Composable
fun rememberSystemContrast(): Double {
    val context = LocalContext.current
    return remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            context.getSystemService(UiModeManager::class.java).contrast.toDouble() // 0 standard, 0.5 medium, 1 high
        } else 0.0
    }
}
```

The brand and content-based snippets above already pass it. For a static Theme Builder export, also export the medium and high contrast schemes and pick one from this value.

## Typography

- 15 baseline styles plus 15 emphasized ones (`displayLargeEmphasized` ... `labelSmallEmphasized`, 1.5.0 only). Emphasized raises weight: Display, Headline, Body, and Title Large go Regular to Medium; Title Medium/Small and Labels go Medium to Bold.
- Change letter spacing and line height to fit a new face, not the scale's sizes: "Avoid changing the type size; this can affect how components render and reflow." (Google) For editorial sizes (a total that fills the top of the screen), define a separate style outside the scale, like `numeralStyle` below.

### Bundled typefaces and voices

All six variable TTFs are OFL and bundled in the `android-design-explore` skill's `assets/fonts/` folder (copy the ones the app uses into `res/font/`, so neither explorations nor Compose apps fetch fonts). Material splits typography into the plain typeface (smaller styles, "to focus on readability") and the brand typeface (larger styles, "to focus on expression"). The face set, one Display/Headline face per app or proposal, Doto's display-only limit, and keeping Title on Google Sans Flex are this skill's choices, not Google's:

- **Plain text (`Title`, `Body`, `Label`):** always `Google Sans Flex` (`google_sans_flex`).
- **Display and Headline (`24sp+`, or `36sp+` for `Doto`):** pick **one** face from the table below (`Fraunces`, `Bricolage Grotesque`, `Big Shoulders Display`, `Doto`, or `Google Sans Flex` on expressive axes). Do not use a brand face on Title, Body, or Label.
- **Numbers and data (any size):** `Google Sans Code` (`google_sans_code`) for timers, tabular values, codes, timecodes, and metadata.

| Face | File (`res/font/`) | Roles & min size | Variable axes | Use for & starting point |
| --- | --- | --- | --- | --- |
| Google Sans Flex | `google_sans_flex` | Plain text always; Display & Headline (`24sp+`) | `wght` 1-1000, `wdth` 25-151, `opsz` 6-144, `slnt` -10-0, `GRAD` 0-100, `ROND` 0-100 | Plain text at default axes (`wght` 400-700). Display/Headline voices: see table below |
| Fraunces | `fraunces` | Display & Headline (`24sp+`) | `opsz` 9-144, `wght` 100-900, `SOFT` 0-100, `WONK` 0-1 | Warm editorial serif: food, reading, journaling, heritage (`wght` 600, `opsz` = size, `SOFT` 100, `WONK` 1) |
| Bricolage Grotesque | `bricolage_grotesque` | Display & Headline (`24sp+`) | `opsz` 12-96, `wdth` 75-100, `wght` 200-800 | Playful, quirky grotesque: indie, creative tools (`wght` 800, `wdth` 80 to 100, `opsz` = size up to 96) |
| Big Shoulders Display | `big_shoulders_display` | Display & Headline (`24sp+`) | `wght` 100-900 | Tall condensed poster type: sport, fitness, transit, loud energy (`wght` 800 to 900, tight tracking, line height 1.2x) |
| Doto | `doto` | Display only (`36sp+`) | `ROND` 0-100, `wght` 100-900 | Dot-matrix: timers, music, retro-tech (`wght` 700 to 900, `ROND` 100 for round dots) |
| Google Sans Code | `google_sans_code` | Numbers & data (any size) | `wght` 300-800 | Timers, tabular values, codes, metadata (`wght` 400 to 700, `fontFeatureSettings = "tnum"`) |

When `Google Sans Flex` is chosen for Display and Headline, tune its axes to the screen's direction rather than leaving it at regular weight and width:

| Google Sans Flex voice | Axes (approx.) | Use for |
| --- | --- | --- |
| Friendly hero | weight 800 to 900, roundness 100 | The one hero title or number in a warm, playful product |
| Tall numerals | width 25 to 50; weight 300 off, 800 on | Numbers that fill a card (times, totals, calculators), state through weight |
| Premium display | width 75 to 90, weight 400 to 600 | Elegant headers and editorial titles |
| Airy display | weight 100 to 300, large sizes only | Calm headers, inactive states |
| Wide header | width 125 to 151, weight 500 to 700, roundness 0 | Confident section or dialog titles; never in app bars |
| Loud expressive | width 151, weight 900, slant -10 | Celebrations and the user's own voice; rare |
| Text | default axes, weight 400 to 700 | Title, Body, and Label; keep default axes |

### Compose font utilities (`googleSansFlex` and `voiceFont`)

A plain `FontFamily(Font(R.font.google_sans_flex))` draws every weight at 400 because variable TTFs require a `Font` instance with `FontVariation.weight` per weight. Use two helpers across every app:

1. `googleSansFlex`: the 4-weight plain `FontFamily` (`400, 500, 600, 700`) applied to the baseline type scale.
2. `voiceFont(resId, weight, vararg axes)`: builds a `FontFamily` for any custom Display, Headline, or `Google Sans Code` voice. Variation settings apply on API 26+.

```kotlin
// Plain text family: one wght instance per weight so baseline and emphasized styles render their weights.
private val googleSansFlex = FontFamily(
    listOf(400, 500, 600, 700).map { weight ->
        Font(
            resId = R.font.google_sans_flex,
            weight = FontWeight(weight),
            variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
        )
    },
)

/**
 * One font family for a custom voice (Display, Headline, or Google Sans Code): its weight plus every
 * other axis the voice uses (`FontVariation.opticalSizing(size.sp)` for `opsz`, `FontVariation.width`
 * for `wdth`, `FontVariation.Setting("SOFT", 100f)`, `FontVariation.Setting("ROND", 100f)`, and so on).
 */
fun voiceFont(resId: Int, weight: Int, vararg axes: FontVariation.Setting) = FontFamily(
    Font(
        resId = resId,
        weight = FontWeight(weight),
        variationSettings = FontVariation.Settings(FontVariation.weight(weight), *axes),
    ),
)

private val base = Typography()

// Example 1: a brand face (Fraunces) on Display/Headline, Google Sans Flex on Title/Body/Label.
private fun heroVoice(sizeSp: Int) = voiceFont(
    R.font.fraunces,
    weight = 600,
    FontVariation.opticalSizing(sizeSp.sp),
    FontVariation.Setting("SOFT", 100f),
    FontVariation.Setting("WONK", 1f),
)

// Example 2: or an expressive Google Sans Flex voice on Display/Headline:
// private fun heroVoice(sizeSp: Int) = voiceFont(
//     R.font.google_sans_flex,
//     weight = 850,
//     FontVariation.opticalSizing(sizeSp.sp),
//     FontVariation.Setting("ROND", 100f),
// )

val AppTypography = base.copy(
    displayLarge = base.displayLarge.copy(fontFamily = heroVoice(57), fontWeight = FontWeight(600)),
    displayMedium = base.displayMedium.copy(fontFamily = heroVoice(45), fontWeight = FontWeight(600)),
    headlineLarge = base.headlineLarge.copy(fontFamily = heroVoice(32), fontWeight = FontWeight(600)),
    headlineMedium = base.headlineMedium.copy(fontFamily = heroVoice(28), fontWeight = FontWeight(600)),
    titleLarge = base.titleLarge.copy(fontFamily = googleSansFlex),
    titleMedium = base.titleMedium.copy(fontFamily = googleSansFlex),
    titleSmall = base.titleSmall.copy(fontFamily = googleSansFlex),
    bodyLarge = base.bodyLarge.copy(fontFamily = googleSansFlex),
    bodyMedium = base.bodyMedium.copy(fontFamily = googleSansFlex),
    bodySmall = base.bodySmall.copy(fontFamily = googleSansFlex),
    labelLarge = base.labelLarge.copy(fontFamily = googleSansFlex),
    labelMedium = base.labelMedium.copy(fontFamily = googleSansFlex),
    labelSmall = base.labelSmall.copy(fontFamily = googleSansFlex),
    // ...plus the *Emphasized styles you use
)

// Numbers and data voice with Google Sans Code:
val dataVoice = voiceFont(R.font.google_sans_code, weight = 500)

// A voice outside the type scale, such as tall numerals with state:
@Composable
fun numeralStyle(selected: Boolean) = MaterialTheme.typography.displayLarge.copy(
    fontFamily = voiceFont(
        R.font.google_sans_flex,
        weight = if (selected) 800 else 300,
        FontVariation.width(35f),
        FontVariation.opticalSizing(57.sp),
    ),
    fontWeight = FontWeight(if (selected) 800 else 300),
    fontFeatureSettings = "tnum",
)
```

- Define two or three voices once per app and apply them consistently; don't invent new axis values per screen.
- State through axes: selected items heavy (700 to 900), unselected light (200 to 300); keep sizes equal so nothing reflows. `FontVariation.grade(...)` changes emphasis without changing width.
- Each distinct axis combination is a separate font instance; for animated axes (text that widens as a slider moves), step through a small set of values and keep it to a hero moment.
- Tabular numbers: `style.copy(fontFeatureSettings = "tnum")`.
- Measured line boxes (Pixel_10_Pro_XL emulator, M3 `bodyLarge` base style with the token size and line height): Fraunces, Bricolage Grotesque, and Big Shoulders Display match the token line height at 36/44 and 32/40 (Fraunces 44.7dp at 36/44 from pixel rounding), so no extra vertical room is needed. Doto at 45/52 renders 54dp tall, so leave about 2dp of room around a Doto line. Google Sans Flex, by comparison, renders 52.7dp at 36/44 and 46.7dp at 32/40. Widths in Compose differ from the HTML runtime unless you copy its tracking (the runtime adds 0.5px letter spacing) and set `FontVariation.opticalSizing` to the style's size for Fraunces and Bricolage Grotesque; without it, Bricolage Grotesque rendered about 10% narrower than the proposal in the preview.

## Shapes

`Shapes(extraSmall, small, medium, large, extraLarge, largeIncreased, extraLargeIncreased, extraExtraLarge)`, read as `MaterialTheme.shapes.*`:

| Token | Default | Typical use |
| --- | --- | --- |
| extraSmall | 4dp | Chips, snackbars |
| small | 8dp | Text fields, menus |
| medium | 12dp | Cards |
| large | 16dp | FABs, sheets on large screens |
| largeIncreased | 20dp | Expressive containers |
| extraLarge | 28dp | Dialogs, bottom sheets |
| extraLargeIncreased | 32dp | Expressive hero containers |
| extraExtraLarge | 48dp | Large hero surfaces |

Change a token to change every component mapped to it; pass `shape` to change one component.

### Shape library

`MaterialShapes` (experimental) has 35 `RoundedPolygon`s: Circle, Square, Slanted, Arch, Fan, Arrow, SemiCircle, Oval, Pill, Triangle, Diamond, ClamShell, Pentagon, Gem, Sunny, VerySunny, Cookie4Sided, Cookie6Sided, Cookie7Sided, Cookie9Sided, Cookie12Sided, Ghostish, Clover4Leaf, Clover8Leaf, Burst, SoftBurst, Boom, SoftBoom, Flower, Puffy, PuffyDiamond, PixelCircle, PixelTriangle, Bun, Heart.

Clip with `Modifier.clip(MaterialShapes.Sunny.toShape())` or use as a `Surface` shape.

### Shape morphing

Prefer components that morph on their own (buttons, icon buttons, toggles, button groups, list items, loading indicators). For a custom morph between two library shapes:

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private class MorphShape(private val morph: Morph, private val progress: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = morph.toPath(progress = progress) // unit-square coordinates
        path.transform(Matrix().apply { scale(size.width, size.height) })
        return Outline.Generic(path)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MorphingBadge(done: Boolean, modifier: Modifier = Modifier) {
    val morph = remember { Morph(MaterialShapes.Circle, MaterialShapes.Cookie9Sided) }
    val progress by animateFloatAsState(if (done) 1f else 0f, MaterialTheme.motionScheme.defaultSpatialSpec())
    Box(modifier.size(64.dp).clip(MorphShape(morph, progress)).background(MaterialTheme.colorScheme.tertiaryContainer))
}
```

## Motion

`MotionScheme` has six specs: `defaultSpatialSpec`, `fastSpatialSpec`, `slowSpatialSpec`, `defaultEffectsSpec`, `fastEffectsSpec`, `slowEffectsSpec`. `MotionScheme.expressive()` and `MotionScheme.standard()` are the presets:

| Spec | Expressive (damping / stiffness) | Standard |
| --- | --- | --- |
| Fast spatial | 0.6 / 800 | 0.9 / 1400 |
| Default spatial | 0.8 / 380 | 0.9 / 700 |
| Slow spatial | 0.8 / 200 | 0.9 / 300 |
| Effects fast / default / slow | 1.0 / 3800, 1600, 800 | same |

- Override one subtree by nesting `MaterialTheme(motionScheme = MotionScheme.standard()) { ... }` (`LocalMotionScheme` was removed).
- Reduced motion, Google's pattern:

```kotlin
object ReducedMotionScheme : MotionScheme {
    override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> slowSpatialSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> slowEffectsSpec(): FiniteAnimationSpec<T> = snap()
}
```

- A custom brand scheme overrides the same six functions with `spring(dampingRatio, stiffness)`; keep effects specs at `Spring.DampingRatioNoBouncy`.

## Spacing

Compose has no spacing tokens. Define the 8dp scale once in the theme package and use it everywhere:

```kotlin
object Spacing {
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp
}
```

Screen margins: 16dp on compact, 24dp on medium and larger. Put padding and gaps on parent layouts (`Arrangement.spacedBy`) instead of margins on children.
