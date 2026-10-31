package bot.charlies.androiddesign.preview.feature.preview.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.ShapeToken
import bot.charlies.androiddesign.preview.model.TypeRole
import bot.charlies.androiddesign.preview.model.TypographyOverride
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec

/** The Compose values a [DesignSystem] resolves to for one brightness. */
class ResolvedTheme(
  val design: DesignSystem,
  val dark: Boolean,
  val colorScheme: ColorScheme,
  val typography: Typography,
  /** The style of every type role, including the emphasized ones. */
  val textStyles: Map<TypeRole, TextStyle>,
  /** The family name each role is set in, such as `Google Sans Flex`. */
  val fontFamilies: Map<TypeRole, String>,
  val shapes: Shapes,
) {
  fun style(role: TypeRole): TextStyle = textStyles.getValue(role)

  fun color(role: ColorRole) = colorScheme.color(role)
}

/** Turns a [DesignSystem] into Compose theme values. Pure, so it needs no composition. */
object ThemeResolver {
  fun resolve(design: DesignSystem, dark: Boolean): ResolvedTheme {
    val (typography, styles, families) = ProposalTypography.build(design)
    return ResolvedTheme(
      design = design,
      dark = dark,
      colorScheme = colorScheme(design, dark),
      typography = typography,
      textStyles = styles,
      fontFamilies = families,
      shapes = shapes(design),
    )
  }

  fun colorScheme(design: DesignSystem, dark: Boolean): ColorScheme {
    val generated =
      dynamicColorScheme(
        seedColor = parseHexColor(design.seed),
        isDark = dark,
        isAmoled = false,
        style = design.variant.toPaletteStyle(),
        contrastLevel = design.contrast,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
      )
    return design.colorOverrides.entries.fold(generated) { scheme, (role, hex) ->
      scheme.withColor(role, parseHexColor(hex))
    }
  }

  fun shapes(design: DesignSystem): Shapes {
    fun corner(token: ShapeToken): CornerBasedShape {
      val dp = design.rounded[token] ?: DesignSystemResolver.DEFAULT_ROUNDED.getValue(token)
      return if (dp >= FULL_DP) CircleShape else RoundedCornerShape(dp.dp)
    }
    return Shapes(
      extraSmall = corner(ShapeToken.ExtraSmall),
      small = corner(ShapeToken.Small),
      medium = corner(ShapeToken.Medium),
      large = corner(ShapeToken.Large),
      extraLarge = corner(ShapeToken.ExtraLarge),
      largeIncreased = corner(ShapeToken.LargeIncreased),
      extraLargeIncreased = corner(ShapeToken.ExtraLargeIncreased),
      extraExtraLarge = corner(ShapeToken.ExtraExtraLarge),
    )
  }

  private const val FULL_DP = 1000f
}

fun PaletteVariant.toPaletteStyle(): PaletteStyle =
  when (this) {
    PaletteVariant.TonalSpot -> PaletteStyle.TonalSpot
    PaletteVariant.Neutral -> PaletteStyle.Neutral
    PaletteVariant.Vibrant -> PaletteStyle.Vibrant
    PaletteVariant.Expressive -> PaletteStyle.Expressive
    PaletteVariant.Monochrome -> PaletteStyle.Monochrome
    PaletteVariant.Fidelity -> PaletteStyle.Fidelity
    PaletteVariant.Content -> PaletteStyle.Content
    PaletteVariant.Rainbow -> PaletteStyle.Rainbow
    PaletteVariant.FruitSalad -> PaletteStyle.FruitSalad
  }

/**
 * Builds the type scale: Google Sans Flex by default, with the design system's per-role overrides.
 */
object ProposalTypography {
  data class Built(
    val typography: Typography,
    val styles: Map<TypeRole, TextStyle>,
    val families: Map<TypeRole, String>,
  )

  /** Emphasized roles are the baseline role with Google's emphasized weight and tracking. */
  private data class Emphasis(val weight: Int, val trackingSp: Float)

  private fun emphasis(role: TypeRole): Emphasis? =
    when (role) {
      TypeRole.DisplayLargeEmphasized,
      TypeRole.DisplayMediumEmphasized,
      TypeRole.DisplaySmallEmphasized,
      TypeRole.HeadlineLargeEmphasized,
      TypeRole.HeadlineMediumEmphasized,
      TypeRole.HeadlineSmallEmphasized,
      TypeRole.TitleLargeEmphasized -> Emphasis(500, 0f)
      TypeRole.TitleMediumEmphasized -> Emphasis(700, 0.15f)
      TypeRole.TitleSmallEmphasized -> Emphasis(700, 0.1f)
      TypeRole.BodyLargeEmphasized -> Emphasis(500, 0.15f)
      TypeRole.BodyMediumEmphasized -> Emphasis(500, 0.25f)
      TypeRole.BodySmallEmphasized -> Emphasis(500, 0.4f)
      TypeRole.LabelLargeEmphasized -> Emphasis(700, 0.1f)
      TypeRole.LabelMediumEmphasized,
      TypeRole.LabelSmallEmphasized -> Emphasis(700, 0.5f)
      else -> null
    }

  private fun TypeRole.baseline(): TypeRole =
    if (emphasis(this) == null) this else TypeRole.valueOf(name.removeSuffix("Emphasized"))

  fun build(design: DesignSystem): Built {
    val base = Typography()
    val styles = linkedMapOf<TypeRole, TextStyle>()
    val families = linkedMapOf<TypeRole, String>()
    for (role in TypeRole.entries) {
      val override = design.typography[role]
      val emphasis = emphasis(role)
      var style = base.style(role.baseline())
      if (emphasis != null) {
        style =
          style.copy(
            fontWeight = FontWeight(emphasis.weight),
            letterSpacing = emphasis.trackingSp.sp,
          )
      }
      val axes = FontAxisSet(override, style.fontWeight?.weight ?: 400)
      val face = FontFace.byFamily(override?.fontFamily) ?: FontFace.GoogleSansFlex
      style =
        style.copy(
          fontFamily = ProposalFonts.family(face, axes.weight, axes.others),
          fontWeight = FontWeight(axes.weight.coerceIn(ProposalFonts.WEIGHTS)),
          letterSpacing = override?.letterSpacing?.let(::parseTracking) ?: style.letterSpacing,
        )
      styles[role] = style
      families[role] = face.family
    }
    return Built(typography(styles), styles, families)
  }

  private class FontAxisSet(override: TypographyOverride?, styleWeight: Int) {
    private val declared = ProposalFonts.parseVariation(override?.fontVariation)
    val weight: Int =
      declared.firstOrNull { it.tag == "wght" }?.value?.toInt()
        ?: override?.fontWeight
        ?: styleWeight
    val others = declared.filter { it.tag != "wght" }
  }

  private fun parseTracking(value: String) =
    Regex("""^(-?\d+(?:\.\d+)?)""").find(value.trim())?.groupValues?.get(1)?.toFloatOrNull()?.sp

  private fun typography(styles: Map<TypeRole, TextStyle>): Typography =
    Typography(
      displayLarge = styles.getValue(TypeRole.DisplayLarge),
      displayMedium = styles.getValue(TypeRole.DisplayMedium),
      displaySmall = styles.getValue(TypeRole.DisplaySmall),
      headlineLarge = styles.getValue(TypeRole.HeadlineLarge),
      headlineMedium = styles.getValue(TypeRole.HeadlineMedium),
      headlineSmall = styles.getValue(TypeRole.HeadlineSmall),
      titleLarge = styles.getValue(TypeRole.TitleLarge),
      titleMedium = styles.getValue(TypeRole.TitleMedium),
      titleSmall = styles.getValue(TypeRole.TitleSmall),
      bodyLarge = styles.getValue(TypeRole.BodyLarge),
      bodyMedium = styles.getValue(TypeRole.BodyMedium),
      bodySmall = styles.getValue(TypeRole.BodySmall),
      labelLarge = styles.getValue(TypeRole.LabelLarge),
      labelMedium = styles.getValue(TypeRole.LabelMedium),
      labelSmall = styles.getValue(TypeRole.LabelSmall),
      displayLargeEmphasized = styles.getValue(TypeRole.DisplayLargeEmphasized),
      displayMediumEmphasized = styles.getValue(TypeRole.DisplayMediumEmphasized),
      displaySmallEmphasized = styles.getValue(TypeRole.DisplaySmallEmphasized),
      headlineLargeEmphasized = styles.getValue(TypeRole.HeadlineLargeEmphasized),
      headlineMediumEmphasized = styles.getValue(TypeRole.HeadlineMediumEmphasized),
      headlineSmallEmphasized = styles.getValue(TypeRole.HeadlineSmallEmphasized),
      titleLargeEmphasized = styles.getValue(TypeRole.TitleLargeEmphasized),
      titleMediumEmphasized = styles.getValue(TypeRole.TitleMediumEmphasized),
      titleSmallEmphasized = styles.getValue(TypeRole.TitleSmallEmphasized),
      bodyLargeEmphasized = styles.getValue(TypeRole.BodyLargeEmphasized),
      bodyMediumEmphasized = styles.getValue(TypeRole.BodyMediumEmphasized),
      bodySmallEmphasized = styles.getValue(TypeRole.BodySmallEmphasized),
      labelLargeEmphasized = styles.getValue(TypeRole.LabelLargeEmphasized),
      labelMediumEmphasized = styles.getValue(TypeRole.LabelMediumEmphasized),
      labelSmallEmphasized = styles.getValue(TypeRole.LabelSmallEmphasized),
    )
}

/** The text style of [role] in the standard Material typography. */
fun Typography.style(role: TypeRole): TextStyle =
  when (role) {
    TypeRole.DisplayLarge -> displayLarge
    TypeRole.DisplayMedium -> displayMedium
    TypeRole.DisplaySmall -> displaySmall
    TypeRole.HeadlineLarge -> headlineLarge
    TypeRole.HeadlineMedium -> headlineMedium
    TypeRole.HeadlineSmall -> headlineSmall
    TypeRole.TitleLarge -> titleLarge
    TypeRole.TitleMedium -> titleMedium
    TypeRole.TitleSmall -> titleSmall
    TypeRole.BodyLarge -> bodyLarge
    TypeRole.BodyMedium -> bodyMedium
    TypeRole.BodySmall -> bodySmall
    TypeRole.LabelLarge -> labelLarge
    TypeRole.LabelMedium -> labelMedium
    TypeRole.LabelSmall -> labelSmall
    TypeRole.DisplayLargeEmphasized -> displayLargeEmphasized
    TypeRole.DisplayMediumEmphasized -> displayMediumEmphasized
    TypeRole.DisplaySmallEmphasized -> displaySmallEmphasized
    TypeRole.HeadlineLargeEmphasized -> headlineLargeEmphasized
    TypeRole.HeadlineMediumEmphasized -> headlineMediumEmphasized
    TypeRole.HeadlineSmallEmphasized -> headlineSmallEmphasized
    TypeRole.TitleLargeEmphasized -> titleLargeEmphasized
    TypeRole.TitleMediumEmphasized -> titleMediumEmphasized
    TypeRole.TitleSmallEmphasized -> titleSmallEmphasized
    TypeRole.BodyLargeEmphasized -> bodyLargeEmphasized
    TypeRole.BodyMediumEmphasized -> bodyMediumEmphasized
    TypeRole.BodySmallEmphasized -> bodySmallEmphasized
    TypeRole.LabelLargeEmphasized -> labelLargeEmphasized
    TypeRole.LabelMediumEmphasized -> labelMediumEmphasized
    TypeRole.LabelSmallEmphasized -> labelSmallEmphasized
  }

/** The resolved theme of the nearest [ProposalTheme]. */
val LocalResolvedTheme =
  compositionLocalOf<ResolvedTheme> { error("ProposalTheme is missing from the composition") }

/**
 * Real Material 3 Expressive theming from a design system: MaterialKolor's dynamic scheme (seed,
 * variant, contrast, 2025 spec) with explicit role colors on top, the type scale, the corner scale
 * and the expressive motion scheme.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProposalTheme(design: DesignSystem, dark: Boolean, content: @Composable () -> Unit) {
  val resolved = remember(design, dark) { ThemeResolver.resolve(design, dark) }
  CompositionLocalProvider(LocalResolvedTheme provides resolved) {
    MaterialExpressiveTheme(
      colorScheme = resolved.colorScheme,
      motionScheme = MotionScheme.expressive(),
      shapes = resolved.shapes,
      typography = resolved.typography,
      content = content,
    )
  }
}

@Preview
@Composable
private fun ProposalThemePreview() {
  val design = DesignSystemResolver.resolve(null, null)
  ProposalTheme(design, dark = false) {
    Surface {
      Column(Modifier.padding(16.dp)) {
        Text("Display small", style = MaterialTheme.typography.displaySmall)
        Text("Body large in Google Sans Flex", style = MaterialTheme.typography.bodyLarge)
        Box(Modifier.padding(top = 8.dp)) {
          Text("Label large", style = MaterialTheme.typography.labelLarge)
        }
      }
    }
  }
}
