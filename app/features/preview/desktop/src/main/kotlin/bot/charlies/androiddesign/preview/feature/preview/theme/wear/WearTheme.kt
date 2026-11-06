package bot.charlies.androiddesign.preview.feature.preview.theme.wear

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.wear.compose.SystemColorNames
import androidx.wear.compose.dynamicWearColorScheme
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Typography
import bot.charlies.androiddesign.preview.feature.preview.theme.FontFace
import bot.charlies.androiddesign.preview.feature.preview.theme.PreviewTheme
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalFonts
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalTypography
import bot.charlies.androiddesign.preview.feature.preview.theme.parseHexColor
import bot.charlies.androiddesign.preview.feature.preview.theme.toHex
import bot.charlies.androiddesign.preview.feature.preview.theme.toPaletteStyle
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.WearCatalog
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec

/**
 * The Wear Material 3 values a [DesignSystem] resolves to. Wear is always dark.
 *
 * @property fontFamilies The family name each type role is set in, by role wire name.
 */
class WearTheme(
  override val design: DesignSystem,
  val colorScheme: ColorScheme,
  val typography: Typography,
  val fontFamilies: Map<String, String>,
) : PreviewTheme {
  override val dark: Boolean
    get() = true

  fun color(role: String): Color =
    colorScheme.color(role) ?: error("'$role' is not a Wear color role")

  fun style(role: String): TextStyle =
    typography.style(role) ?: error("'$role' is not a Wear type role")
}

/** Turns a [DesignSystem] into Wear theme values. Pure, so it needs no composition. */
object WearThemeResolver {
  /** The family the upstream Wear typography is set in, which is not one of the bundled faces. */
  const val DEFAULT_FAMILY = "Roboto Flex"

  fun resolve(design: DesignSystem): WearTheme {
    val (typography, families) = typography(design)
    return WearTheme(design, colorScheme(design), typography, families)
  }

  /**
   * The color scheme [design] resolves to as `#RRGGBB` per role of [WearCatalog.colorRoles]: the
   * values the previewer draws, which the Kotlin export writes into the exported theme.
   */
  fun roleColors(design: DesignSystem): Map<String, String> {
    val scheme = colorScheme(design)
    return WearCatalog.colorRoles.associateWith { role ->
      checkNotNull(scheme.color(role)) { "'$role' is not a Wear color role" }.toHex()
    }
  }

  /**
   * Without a seed, the upstream default scheme. With one, the dynamic scheme of a watch face
   * themed from that seed: the tonal palettes come from MaterialKolor and go to Wear's own
   * `dynamicColorScheme` as the Android system colors, so the role mapping is Wear's. Explicit role
   * colors are applied on top.
   */
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
    val colors =
      mapOf(
        "system_background_dark" to Color.Black,
        "system_on_background_dark" to tones.onBackground,
        "system_primary_fixed" to tones.primaryFixed,
        "system_primary_fixed_dim" to tones.primaryFixedDim,
        "system_on_primary_fixed" to tones.onPrimaryFixed,
        "system_primary_container_dark" to tones.primaryContainer,
        "system_on_primary_container_dark" to tones.onPrimaryContainer,
        "system_secondary_fixed" to tones.secondaryFixed,
        "system_secondary_fixed_dim" to tones.secondaryFixedDim,
        "system_on_secondary_fixed" to tones.onSecondaryFixed,
        "system_secondary_container_dark" to tones.secondaryContainer,
        "system_on_secondary_container_dark" to tones.onSecondaryContainer,
        "system_tertiary_fixed" to tones.tertiaryFixed,
        "system_tertiary_fixed_dim" to tones.tertiaryFixedDim,
        "system_on_tertiary_fixed" to tones.onTertiaryFixed,
        "system_tertiary_container_dark" to tones.tertiaryContainer,
        "system_on_tertiary_container_dark" to tones.onTertiaryContainer,
        "system_error_dark" to tones.error,
        "system_error_container_dark" to tones.errorContainer,
        "system_on_error_dark" to tones.onError,
        "system_on_error_container_dark" to tones.onErrorContainer,
        "system_on_surface_dark" to tones.onSurface,
        "system_on_surface_variant_dark" to tones.onSurfaceVariant,
        "system_outline_dark" to tones.outline,
        "system_outline_variant_dark" to tones.outlineVariant,
        "system_surface_container_dark" to tones.surfaceContainer,
        "system_surface_container_high_dark" to tones.surfaceContainerHigh,
        "system_surface_container_low_dark" to tones.surfaceContainerLow,
      )
    check(colors.keys == SystemColorNames.toSet()) { "the system colors Wear reads changed" }
    return dynamicWearColorScheme { name -> colors.getValue(name) }
  }

  /** The upstream type scale with the design's per-role overrides on top. */
  private fun typography(design: DesignSystem): Pair<Typography, Map<String, String>> {
    val base = Typography()
    val families = WearCatalog.typeRoles.keys.associateWith { DEFAULT_FAMILY }.toMutableMap()
    val styles = linkedMapOf<String, TextStyle>()
    for (role in WearCatalog.typeRoles.keys) {
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
                  ?: style.fontFamily,
              fontWeight = FontWeight(axes.weight.coerceIn(ProposalFonts.WEIGHTS)),
            )
        }
        override.letterSpacing?.let(ProposalTypography::parseTracking)?.let {
          style = style.copy(letterSpacing = it)
        }
        face?.let { families[role] = it.family }
      }
      styles[role] = style
    }
    return base.withStyles(styles) to families
  }
}

/** The resolved Wear theme of the nearest [ProvideWearTheme]. */
val LocalWearTheme =
  compositionLocalOf<WearTheme> { error("ProvideWearTheme is missing from the composition") }

/** Makes the Wear Material theme from [design] and [LocalWearTheme] available to [content]. */
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
