# Theming

Theme setup for Material 3 Expressive in Compose: versions, color schemes, typography, shapes, motion, blur, shadows, gradients, and spacing (`androidx.compose.material3` 1.5.0 stable).

## Versions

- `androidx.compose.material3:material3`: **1.5.0+** (Compose BOM `2026.11.01`). (1.4.0 includes only `ShortNavigationBar`, `WideNavigationRail`, and `ModalWideNavigationRail`.)
- Toolchain: Compose `1.13` / Material 3 `1.5.0` require **Kotlin 2.2+**, **compileSdk 37.1**, and **AGP 9.1+** (AGP 9.4 needs Gradle 9.6). Check [Maven metadata](https://dl.google.com/dl/android/maven2/androidx/compose/material3/material3/maven-metadata.xml) when upgrading beyond `1.5.0`; `androidx.compose.material` (M2) is deprecated.
- `@ExperimentalMaterial3ExpressiveApi`: `MaterialShapes`, `RoundedPolygon.toShape()` / `toPath()`, `Morph.toPath()`, `LoadingIndicator`, `ContainedLoadingIndicator`, `LoadingIndicatorDefaults`, pull-to-refresh loading indicator. `@ExperimentalMaterial3Api`: `AppBarWithSearch`, `carouselParallaxScrollEffect`, `BottomSheet`, `ModalBottomSheet`, `BottomSheetScaffold`, tooltips. Shape morphing uses `androidx.graphics:graphics-shapes` (exposed by `material3`).

## Theme

```kotlin
@Composable
fun AppTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colorScheme = when {
        Build.VERSION.SDK_INT >= 31 -> if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> darkColorScheme()
        else -> expressiveLightColorScheme()
    }
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        typography = AppTypography, // Google Sans Flex voices plus an optional brand face
        content = content,
    )
}
```

- Top-level nulls default to `expressiveLightColorScheme()` (no dark counterpart; use `darkColorScheme()` or a generated scheme), `MotionScheme.expressive()`, `Shapes()`, and `Typography()`. Nested themes inherit outer values for nulls; plain `MaterialTheme(...)` defaults to `MotionScheme.standard()`.
- Material 3 1.5.0 adds `LocalRippleThemeConfiguration` (`androidx.compose.material3:material3-ripple`, inset focus rings), `Typography(fontFamily = ...)` for single-family propagation, and `Text(..., color = { ... })` (`ColorProducer`) for draw-phase color updates.

## Color schemes

Pick one source per app, plus at most one content-based scheme per screen.

### Dynamic (wallpaper)

`dynamicLightColorScheme(context)` / `dynamicDarkColorScheme(context)` on API 31+ with a fallback scheme for older devices.

### Brand (seed color)

Compose has no built-in seed API. Either export static schemes from Material Theme Builder (enable "match color" for literal brand colors) or generate at runtime with [MaterialKolor](https://github.com/jordond/MaterialKolor) (`com.materialkolor:material-kolor`):

```kotlin
val colorScheme = rememberDynamicColorScheme(
    seedColor = BrandColor,
    isDark = dark,
    specVersion = ColorSpec.SpecVersion.SPEC_2025,
    style = PaletteStyle.TonalSpot, // Material's default; Fidelity when brand color must appear as-is
    contrastLevel = rememberSystemContrast(),
)
```

Keep `TonalSpot` (or `Fidelity` for literal primary/secondary tones, noting `Fidelity` and `Content` move tertiary away from the seed's hue). Avoid `PaletteStyle.Expressive` (unrelated to M3 Expressive; shifts hues and tints surfaces) and `Vibrant` (maxes primary chroma). A common pattern is brand scheme by default with dynamic color as a user setting.

### Content-based

Color a contained area from a visible on-screen image (limit a screen to two scheme sources; never recolor semantic colors; decode a small thumbnail for extraction):

```kotlin
val seed = remember(art) { art.themeColor(fallback = BrandColor) } // com.materialkolor.ktx, on an ImageBitmap
val scheme = rememberDynamicColorScheme(seedColor = seed, isDark = isSystemInDarkTheme(), style = PaletteStyle.Content, contrastLevel = rememberSystemContrast())
MaterialExpressiveTheme(colorScheme = scheme) {
    Surface(color = MaterialTheme.colorScheme.surface) { NowPlaying(...) }
}
```

Wrap the nested theme in `Surface(color = MaterialTheme.colorScheme.surface)` so `LocalContentColor` updates alongside `MaterialTheme.colorScheme`. MaterialKolor 5.x targets Kotlin 2.4; use 4.x if compiling on older Kotlin.

### Roles

| Role | Use |
| --- | --- |
| `primary` / `onPrimary` | Most important action and active states |
| `primaryContainer` / `onPrimaryContainer` | Standout fills (FAB, selected hero element) |
| `secondary`, `secondaryContainer` | Supporting controls, selection, tonal buttons, filter chips |
| `tertiary`, `tertiaryContainer` | Contrasting accents: status, badges, progress, delight; vibrant menus (vibrant toolbars use `primaryContainer`) |
| `error`, `errorContainer` | Errors (static in every scheme) |
| `surface`, `surfaceContainerLowest` to `surfaceContainerHighest` | Body background, nested containers by emphasis; nav areas use `surfaceContainer` |
| `onSurface`, `onSurfaceVariant` | Default text; lower-emphasis text and icons |
| `outline`, `outlineVariant` | Text field borders; dividers and decorative lines only |
| `inverseSurface`, `inverseOnSurface`, `inversePrimary` | Snackbars and reversed standout elements |
| `primaryFixed`, `primaryFixedDim` (and secondary, tertiary) | Same tone in light and dark themes |

Use **role pairs** only (`SKILL.md`, Color). Opacity is reserved for built-in component state layers, disabled content (38%), and modal `scrim`.

### Extra colors (semantic and categories)

- **Categories:** two categories can use `primaryContainer` and `tertiaryContainer` (skip `secondary`, which shares `primary`'s hue); three or more need unharmonized static colors (`harmonize = false` with well-spaced hue seeds). Color only the badge or icon rather than the whole card, keeping one consistent role per category (`color` + `onColor` when also plotted in charts; `container` + `onContainer` for badges only).
- **Charts and data graphics:** use solid roles (`primary`, `secondary`, `tertiary`, or static `color`) for 3:1 contrast against the background rather than pale containers.
- **Contrast for extra colors:** HCT tone difference >= 50 against background for small elements (< 40dp) and >= 40 for larger elements.

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

### Contrast levels

Dynamic schemes follow Android 14+ system contrast automatically; static exports (export medium/high contrast variants too) and MaterialKolor schemes pass `rememberSystemContrast()`:

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

## Typography

- 15 baseline styles plus 15 emphasized ones (`displayLargeEmphasized` ... `labelSmallEmphasized`, 1.5.0+). Emphasized raises weight (Display, Headline, Body, Title Large: Regular to Medium; Title Medium/Small, Labels: Medium to Bold).
- Adjust letter spacing and line height for custom faces while preserving scale sizes (`numeralStyle` below handles oversized editorial display numerals outside the scale).
- **Editorial techniques:** contrast two voices on one line for meaning (light condensed origin vs heavy wide destination); repeat decorative cropped/tinted background text readably and hide the graphic with `Modifier.clearAndSetSemantics {}`; align huge numeral + small unit in a `Row` with `Modifier.alignByBaseline()`.

### Google Sans Flex

Open source variable font ([Google Fonts](https://fonts.google.com/specimen/Google+Sans+Flex)); bundle the TTF in `res/font/` (downloadable fonts lack variation settings). Axes: `wght` 1 to 1000, `wdth` 25 to 151, `opsz` 6 to 144, `slnt` -10 to 0, `GRAD` 0 to 100, `ROND` 0 to 100.

Voice starting points (tune to direction rather than copying literally):

| Voice | Axes (approx.) | Use for |
| --- | --- | --- |
| Friendly hero | weight 800 to 900, roundness 100 | The one hero title or number in a warm, playful product |
| Tall numerals | width 25 to 50; weight 300 off, 800 on | Numbers filling a card (times, totals, calculators), state via weight |
| Premium display | width 75 to 90, weight 400 to 600 | Elegant headers and editorial titles |
| Airy display | weight 100 to 300, large sizes only | Calm headers, inactive states |
| Wide header | width 125 to 151, weight 500 to 700, roundness 0 | Confident section or dialog titles; keep out of app bars |
| Loud expressive | width 151, weight 900, slant -10 | Celebrations and user expression; rare |
| Text | default axes, weight 400 to 500 | Body and labels; keep axes at default |

### Brand faces (Display, Headline, and Title Large only)

Optional single face on Display, Headline, and `titleLarge` (small app bars and side sheets use `titleLarge`); keep Title Medium, Title Small, Body, and Label on Google Sans Flex. All faces are OFL and bundled in `res/font/` (and in the previewer).

| Face | File | Axes | Use for | Starting point |
| --- | --- | --- | --- | --- |
| Fraunces | `fraunces` | `opsz` 9-144, `wght` 100-900, `SOFT` 0-100, `WONK` 0-1 | Warm editorial serif: food, reading, journaling, heritage | `wght` 600, `SOFT` 100, `WONK` 1 |
| Bricolage Grotesque | `bricolage_grotesque` | `opsz` 12-96, `wdth` 75-100, `wght` 200-800 | Playful, quirky grotesque: indie, creative tools | `wght` 800, `wdth` 80 to 100 |
| Big Shoulders Display | `big_shoulders_display` | `wght` 100-900 | Tall condensed poster type: sport, fitness, transit, loud energy | `wght` 800 to 900, tight tracking, line height 1.2x |
| Doto | `doto` | `ROND` 0-100, `wght` 100-900 | Dot-matrix: timers, music, retro-tech. Display sizes only (36sp+) | `wght` 700 to 900, `ROND` 100 for round dots |

Doto at 45/52 renders 54dp tall; leave 2dp extra vertical room around Doto lines (the other bundled brand faces match token line heights).

Google Sans Code (`google_sans_code`, `wght` 300-800) serves numbers and data (timers, tabular values, codes, metadata) at any size.

### Building the type scale

Use `voice` so every variable font gets explicit `FontVariation.weight` and `opticalSizing` (API 26+):

```kotlin
/**
 * Builds a fixed-axis TextStyle from a bundled variable font. Sets weight and optical size here
 * because Compose omits optical size by default; pass only extra axes (width, SOFT, ROND) in `axes`.
 */
fun TextStyle.voice(@FontRes font: Int, weight: Int, vararg axes: FontVariation.Setting) = copy(
    fontFamily = FontFamily(
        Font(
            resId = font,
            weight = FontWeight(weight),
            variationSettings = FontVariation.Settings(
                FontVariation.weight(weight),
                FontVariation.opticalSizing(fontSize),
                *axes,
            ),
        ),
    ),
    fontWeight = FontWeight(weight),
)

private val base = Typography()
private val soft = arrayOf(FontVariation.Setting("SOFT", 100f), FontVariation.Setting("WONK", 1f))

val AppTypography = base.copy(
    // Set all Display, Headline, and Title Large styles plus their *Emphasized counterparts
    // so internal component slots (such as large app bars using displaySmall) never fall back.
    displayLarge = base.displayLarge.voice(R.font.fraunces, 600, *soft),
    displayMedium = base.displayMedium.voice(R.font.fraunces, 600, *soft),
    displaySmall = base.displaySmall.voice(R.font.fraunces, 600, *soft),
    headlineLarge = base.headlineLarge.voice(R.font.fraunces, 600, *soft),
    headlineMedium = base.headlineMedium.voice(R.font.fraunces, 600, *soft),
    headlineSmall = base.headlineSmall.voice(R.font.fraunces, 600, *soft),
    titleLarge = base.titleLarge.voice(R.font.fraunces, 600, *soft),
    // Title Medium/Small, Body, and Label: always Google Sans Flex at default axes.
    titleMedium = base.titleMedium.voice(R.font.google_sans_flex, 500),
    bodyLarge = base.bodyLarge.voice(R.font.google_sans_flex, 400),
    bodyMedium = base.bodyMedium.voice(R.font.google_sans_flex, 400),
    labelLarge = base.labelLarge.voice(R.font.google_sans_flex, 500),
    // ...remaining Title, Body, Label, and *Emphasized styles (one weight step heavier)
)

@Composable
fun numeralStyle(selected: Boolean) = MaterialTheme.typography.displayLarge
    .voice(R.font.google_sans_flex, if (selected) 800 else 300, FontVariation.width(35f))
    .copy(fontFeatureSettings = "tnum")

@Composable
fun dataStyle() = MaterialTheme.typography.bodyMedium
    .voice(R.font.google_sans_code, 500)
    .copy(fontFeatureSettings = "tnum")
```

- Reuse 2 to 3 voices app-wide; express state through weight (`700` to `900` selected vs `200` to `300` unselected at equal size) or `FontVariation.grade(...)`; step animated axes across a small fixed set in hero moments only.

## Shapes

`Shapes(extraSmall, small, medium, large, extraLarge, largeIncreased, extraLargeIncreased, extraExtraLarge)`, read via `MaterialTheme.shapes.*`:

| Token | Default | Typical use |
| --- | --- | --- |
| `extraSmall` | 4dp | Chips, snackbars |
| `small` | 8dp | Text fields, menus |
| `medium` | 12dp | Cards |
| `large` | 16dp | FABs, large-screen sheets |
| `largeIncreased` | 20dp | Expressive containers |
| `extraLarge` | 28dp | Dialogs, bottom sheets |
| `extraLargeIncreased` | 32dp | Expressive hero containers |
| `extraExtraLarge` | 48dp | Large hero surfaces |

### Shape library

`MaterialShapes` (experimental) provides 35 `RoundedPolygon`s: `Circle`, `Square`, `Slanted`, `Arch`, `Fan`, `Arrow`, `SemiCircle`, `Oval`, `Pill`, `Triangle`, `Diamond`, `ClamShell`, `Pentagon`, `Gem`, `Sunny`, `VerySunny`, `Cookie4Sided`, `Cookie6Sided`, `Cookie7Sided`, `Cookie9Sided`, `Cookie12Sided`, `Ghostish`, `Clover4Leaf`, `Clover8Leaf`, `Burst`, `SoftBurst`, `Boom`, `SoftBoom`, `Flower`, `Puffy`, `PuffyDiamond`, `PixelCircle`, `PixelTriangle`, `Bun`, `Heart`.

Use freely as masks on avatars, photos, and album art (`Modifier.clip(MaterialShapes.Cookie9Sided.toShape())`); standalone decorative graphics must depict something from the subject (sun, cloud, record label, rather than arbitrary blobs) in recognizable static colors.

### Shape morphing

Prefer components that morph natively (buttons, icon buttons, toggles, button groups, list items, loading indicators). For custom morphs:

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

`MotionScheme` defines `defaultSpatialSpec`, `fastSpatialSpec`, `slowSpatialSpec`, `defaultEffectsSpec`, `fastEffectsSpec`, and `slowEffectsSpec`:

| Spec | Expressive (damping / stiffness) | Standard |
| --- | --- | --- |
| Fast spatial | 0.6 / 800 | 0.9 / 1400 |
| Default spatial | 0.8 / 380 | 0.9 / 700 |
| Slow spatial | 0.8 / 200 | 0.9 / 300 |
| Effects fast / default / slow | 1.0 / 3800, 1600, 800 | same |

- Override a subtree with `MaterialTheme(motionScheme = MotionScheme.standard()) { ... }` (`LocalMotionScheme` was removed). Keep springs interruptible and read `MaterialTheme.motionScheme.*` instead of `tween()`:

```kotlin
val scale by animateFloatAsState(if (pressed) 0.94f else 1f, MaterialTheme.motionScheme.fastSpatialSpec())
val tint by animateColorAsState(
    if (selected) colors.primary else colors.surfaceContainerHigh,
    MaterialTheme.motionScheme.defaultEffectsSpec(),
)
```

- **Reduced motion** (drop decorative morphs/parallax; keep meaningful fades):

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

- Custom brand schemes override the same six specs with `spring(dampingRatio, stiffness)` (`Spring.DampingRatioNoBouncy` on effects).
- **API selection:** `AnimatedVisibility` to show/hide (keeps hidden elements out of layout and accessibility, unlike alpha animations), `AnimatedContent` to swap content, `Modifier.animateContentSize()` before size modifiers, `Modifier.animateItem()` for lists, `updateTransition` for coupled properties, `Animatable` for gestures. Animate in the draw phase via lambda modifiers (`offset { }`, `graphicsLayer { shadowElevation }`, `drawBehind`).
- **Shared elements:** `Modifier.sharedBounds()` for container transforms, `Modifier.sharedElement()` for hero images, `sharedBounds` + `scaleToBounds()` for text; return the theme's spatial spec from `boundsTransform` and keep bars/FABs above with `renderInSharedTransitionScopeOverlay()` (shared elements cannot cross into `Dialog` or `ModalBottomSheet`).

## Blur, shadows, and gradients

- **Blur:** blurs the composable's own surface content (not a backdrop/frosted-glass blur behind it). Requires Android 12 (`Modifier.blur`), Android 13 (`ui` 1.13) for progressive `BlurRadiusSpec`; older devices render unblurred, so keep an active-item cue (color, weight, indicator) and back text over blurred photos with a scrim, gradient, or solid/tonal surface.
  - **Progressive API:** import `BlurRadiusSpec` and `BlurStop` from `androidx.compose.ui.graphics.blur` (`blur` and `BlurredEdgeTreatment` from `androidx.compose.ui.draw`). Pass `verticalGradient`, `horizontalGradient`, `linearGradient(start, end)`, or `radialGradient(center, fallOffRadius)` with 2 to 16 `BlurStop(fraction, radius)` (`0.dp` across sharp bands; radii clamp at 150px). Use `Modifier.blur { }` when geometry depends on layer `size`. `BlurredEdgeTreatment(shape)` matches card/image corners; `BlurredEdgeTreatment.Unbounded` dissolves scrolling/canvas edges.

```kotlin
LazyColumn(
    modifier = Modifier.blur(
        blurRadiusSpec = BlurRadiusSpec.verticalGradient(
            stops = listOf(
                BlurStop(fraction = 0f, radius = 16.dp),
                BlurStop(fraction = 0.3f, radius = 0.dp),
                BlurStop(fraction = 0.7f, radius = 0.dp),
                BlurStop(fraction = 1f, radius = 16.dp),
            ),
        ),
        edgeTreatment = BlurredEdgeTreatment.Unbounded,
    ),
) { /* items */ }
```

- **Shadows:** rely primarily on tonal surface elevation; add `Modifier.shadow(elevation, shape)` only where a spec defines it. `dropShadow()` (before `background`) and `innerShadow()` (after `background`) follow any `MaterialShapes` path; reserve neumorphic/neobrutalist shadows for directions that specifically call for them.
- **Gradients:** use `MeshGradientPainter` for multi-point organic backgrounds (`RuntimeShader` needs Android 13 + fallback); end sweep gradients on their start color.

## Spacing

Define the 8dp scale once in the theme package (16dp side margins compact, 24dp medium+; gaps on parents via `Arrangement.spacedBy`):

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
