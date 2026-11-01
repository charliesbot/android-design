package bot.charlies.androiddesign.preview.domain.design

import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.ShapeToken
import bot.charlies.androiddesign.preview.model.StyleDef
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.TypeRole
import bot.charlies.androiddesign.preview.model.TypographyOverride

/**
 * Merges DESIGN.md with a proposal's `theme` (the proposal wins per key) and fills defaults. Values
 * it cannot read are dropped; the validator is what reports them.
 */
object DesignSystemResolver {
  const val DEFAULT_SEED = "#6750A4"

  val DEFAULT_SPACING: Map<String, Float> =
    linkedMapOf(
      "none" to 0f,
      "xxs" to 2f,
      "xs" to 4f,
      "sm" to 8f,
      "md" to 12f,
      "lg" to 16f,
      "xl" to 24f,
      "xxl" to 32f,
      "xxxl" to 48f,
    )

  val DEFAULT_ROUNDED: Map<ShapeToken, Float> =
    linkedMapOf(
      ShapeToken.None to 0f,
      ShapeToken.ExtraSmall to 4f,
      ShapeToken.Small to 8f,
      ShapeToken.Medium to 12f,
      ShapeToken.Large to 16f,
      ShapeToken.LargeIncreased to 20f,
      ShapeToken.ExtraLarge to 28f,
      ShapeToken.ExtraLargeIncreased to 32f,
      ShapeToken.ExtraExtraLarge to 48f,
      ShapeToken.Full to 9999f,
    )

  /** A `#RRGGBB` color. */
  val HEX = Regex("^#[0-9a-fA-F]{6}$")
  private val DIMENSION = Regex("^(-?\\d+(?:\\.\\d+)?)\\s*(?:px|dp)?$")
  private val COLOR_REFERENCE = Regex("^\\{colors\\.([A-Za-z0-9-]+)}$")

  /** `#RRGGBB` upper-cased, or null when [value] is not that form. */
  fun parseHex(value: String): String? = value.takeIf { HEX.matches(it) }?.uppercase()

  /** True for a reference to a design system color, such as `{colors.primary}`. */
  fun isColorReference(value: String): Boolean = COLOR_REFERENCE.matches(value)

  /** The largest spacing a design system holds, in dp; the validator rejects more. */
  const val MAX_SPACING_DP = 1000f

  /** Reads `28px`, `28dp` or `28` as dp. */
  fun parseDimension(value: String): Float? =
    DIMENSION.matchEntire(value.trim())?.groupValues?.get(1)?.toFloatOrNull()

  fun resolve(designMd: ThemeOverride?, proposalTheme: ThemeOverride?): DesignSystem {
    val layers = listOfNotNull(designMd, proposalTheme)
    val colors = layers.fold(emptyMap<String, String>()) { acc, layer -> acc + layer.colors }

    val seed =
      listOfNotNull(proposalTheme, designMd).firstNotNullOfOrNull { layerSeed(it, colors) }
        ?: DEFAULT_SEED
    val scheme = layers.mapNotNull { it.material?.scheme }
    val variant =
      scheme.mapNotNull { it.variant }.lastOrNull()?.let(PaletteVariant::fromWire)
        ?: PaletteVariant.TonalSpot
    val contrast = scheme.mapNotNull { it.contrast }.lastOrNull()?.coerceIn(-1.0, 1.0) ?: 0.0

    val colorOverrides = linkedMapOf<ColorRole, String>()
    for ((key, value) in colors) {
      if (key == "primary") continue // the seed generates the primary role
      val role = ColorRole.fromKebab(key) ?: continue
      parseHex(value)?.let { colorOverrides[role] = it }
    }

    val typography = linkedMapOf<TypeRole, TypographyOverride>()
    for (layer in layers) {
      for ((key, value) in layer.typography) {
        val role = TypeRole.fromKebab(key) ?: continue
        typography[role] = typography[role]?.mergedWith(value) ?: value
      }
    }

    val rounded = LinkedHashMap(DEFAULT_ROUNDED)
    for (layer in layers) {
      for ((key, value) in layer.rounded) {
        val token = ShapeToken.fromKebab(key) ?: continue
        parseDimension(value)?.let { rounded[token] = it.coerceAtLeast(0f) }
      }
    }

    val declaredSpacing = linkedMapOf<String, Float>()
    for (layer in layers) {
      for ((key, value) in layer.spacing) {
        parseDimension(value)?.let { declaredSpacing[key] = it.coerceIn(0f, MAX_SPACING_DP) }
      }
    }
    val spacing = if (layers.any { it.spacing.isNotEmpty() }) declaredSpacing else DEFAULT_SPACING

    val styles = linkedMapOf<String, StyleDef>()
    for (layer in layers) {
      for ((name, style) in layer.material?.styles.orEmpty()) {
        styles[name] = styles[name]?.mergedWith(style) ?: style
      }
    }

    return DesignSystem(
      name = layers.mapNotNull { it.name }.lastOrNull(),
      seed = seed,
      variant = variant,
      contrast = contrast,
      colorOverrides = colorOverrides,
      typography = typography,
      rounded = rounded,
      spacing = spacing,
      styles = styles,
    )
  }

  /**
   * A layer's own seed: its scheme seed (a hex or a `{colors.x}` reference) or its
   * `colors.primary`.
   */
  private fun layerSeed(layer: ThemeOverride, mergedColors: Map<String, String>): String? {
    val declared = layer.material?.scheme?.seed
    if (declared != null) {
      val reference = COLOR_REFERENCE.matchEntire(declared)
      val value = if (reference != null) mergedColors[reference.groupValues[1]] else declared
      value?.let(::parseHex)?.let {
        return it
      }
    }
    return layer.colors["primary"]?.let(::parseHex)
  }

  private fun TypographyOverride.mergedWith(other: TypographyOverride) =
    TypographyOverride(
      fontFamily = other.fontFamily ?: fontFamily,
      fontVariation = other.fontVariation ?: fontVariation,
      fontWeight = other.fontWeight ?: fontWeight,
      letterSpacing = other.letterSpacing ?: letterSpacing,
    )

  private fun StyleDef.mergedWith(other: StyleDef) =
    StyleDef(
      type = other.type ?: type,
      color = other.color ?: color,
      container = other.container ?: container,
      shape = other.shape ?: shape,
      padding = other.padding ?: padding,
    )
}
