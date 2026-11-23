package bot.charlies.androiddesign.preview.feature.preview.theme.wear

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Typography
import bot.charlies.androiddesign.preview.feature.preview.theme.FontFace
import bot.charlies.androiddesign.preview.feature.preview.theme.PreviewTheme
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalFonts
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalTypography
import bot.charlies.androiddesign.preview.feature.preview.theme.parseHexColor
import bot.charlies.androiddesign.preview.feature.preview.theme.toPaletteStyle
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.WearSurface
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.hct.Hct

class WearTheme(
  override val design: DesignSystem,
  val colorScheme: ColorScheme,
  val typography: Typography,
  val fontFamilies: Map<String, String>,
) : PreviewTheme {
  override val dark: Boolean
    get() = true
}

object WearThemeResolver {
  const val DEFAULT_FAMILY = "Roboto Flex"

  fun resolve(design: DesignSystem): WearTheme {
    val (typography, families) = typography(design)
    return WearTheme(design, colorScheme(design), typography, families)
  }

  fun colorScheme(design: DesignSystem): ColorScheme {
    val base = if (design.seeded) seeded(design) else ColorScheme()
    return design.colorOverrides.entries.fold(base) { scheme, (role, hex) ->
      scheme.withColor(role, parseHexColor(hex))
    }
  }

  private fun seeded(design: DesignSystem): ColorScheme {
    val tones =
      dynamicColorScheme(
        seedColor = parseHexColor(design.seed),
        isDark = true,
        isAmoled = false,
        style = design.variant.toPaletteStyle(),
        contrastLevel = design.contrast,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
      )
    // Bypass phone Settings.Global dynamic_color_theme_enabled suppression by mapping Wear roles
    // directly from MaterialKolor tonal palettes matching androidx.wear.compose.material3.
    return ColorScheme(
      primary = tones.primaryFixed,
      primaryDim = tones.primaryFixedDim,
      primaryContainer = tones.primaryContainer,
      onPrimary = tones.onPrimaryFixed,
      onPrimaryContainer = tones.onPrimaryContainer,
      secondary = tones.secondaryFixed,
      secondaryDim = tones.secondaryFixedDim,
      secondaryContainer = tones.secondaryContainer,
      onSecondary = tones.onSecondaryFixed,
      onSecondaryContainer = tones.onSecondaryContainer,
      tertiary = tones.tertiaryFixed,
      tertiaryDim = tones.tertiaryFixedDim,
      tertiaryContainer = tones.tertiaryContainer,
      onTertiary = tones.onTertiaryFixed,
      onTertiaryContainer = tones.onTertiaryContainer,
      surfaceContainerLow = tones.surfaceContainerLow,
      surfaceContainer = tones.surfaceContainer,
      surfaceContainerHigh = tones.surfaceContainerHigh,
      onSurface = tones.onSurface,
      onSurfaceVariant = tones.onSurfaceVariant,
      outline = tones.outline,
      outlineVariant = tones.outlineVariant,
      background = Color.Black,
      onBackground = tones.onBackground,
      error = tones.error,
      errorDim =
        Hct.fromInt(tones.errorContainer.toArgb()).let { h ->
          Color(Hct.from(h.hue, h.chroma, 68.0).toInt())
        },
      errorContainer = tones.errorContainer,
      onError = tones.onError,
      onErrorContainer = tones.onErrorContainer,
    )
  }

  private fun typography(design: DesignSystem): Pair<Typography, Map<String, String>> {
    val base = Typography()
    val families = WearSurface.typeRoles.keys.associateWith { DEFAULT_FAMILY }.toMutableMap()
    val styles = linkedMapOf<String, TextStyle>()
    for (role in WearSurface.typeRoles.keys) {
      var style = checkNotNull(base.style(role))
      val override = design.typography[role]
      if (override != null) {
        val axes = ProposalTypography.FontAxisSet(override, style.fontWeight?.weight ?: 400)
        val face = FontFace.byFamily(override.fontFamily)
        if (face != null || override.fontWeight != null || override.fontVariation != null) {
          style =
            style.copy(
              fontFamily =
                face?.let { ProposalFonts.family(it, axes.weight, axes.others) }
                  ?: ProposalFonts.wearRobotoFlex(axes.weight, axes.others),
              fontWeight = FontWeight(axes.weight.coerceIn(ProposalFonts.WEIGHTS)),
            )
        }
        override.letterSpacing?.let(ProposalTypography::parseTracking)?.let {
          style = style.copy(letterSpacing = it)
        }
        face?.let { families[role] = it.family }
      } else {
        val weight = style.fontWeight?.weight ?: 400
        style = style.copy(fontFamily = ProposalFonts.wearRobotoFlex(weight))
      }
      styles[role] = style
    }
    return base.withStyles(styles) to families
  }
}

val LocalWearTheme =
  compositionLocalOf<WearTheme> { error("ProvideWearTheme is missing from the composition") }

@Composable
fun ProvideWearTheme(design: DesignSystem, content: @Composable () -> Unit) {
  val resolved = remember(design) { WearThemeResolver.resolve(design) }
  CompositionLocalProvider(LocalWearTheme provides resolved) {
    MaterialTheme(
      colorScheme = resolved.colorScheme,
      typography = resolved.typography,
      content = content,
    )
  }
}
