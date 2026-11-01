package bot.charlies.androiddesign.preview.feature.preview.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Typeface
import bot.charlies.androiddesign.preview.model.BundledFont
import java.util.concurrent.ConcurrentHashMap
import org.jetbrains.skia.Data
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontVariation as SkiaFontVariation
import org.jetbrains.skia.Typeface as SkiaTypeface

/** The faces the app bundles: each [BundledFont] with the classpath path of its (variable) TTF. */
enum class FontFace(val font: BundledFont, val resource: String) {
  GoogleSansFlex(BundledFont.GoogleSansFlex, "fonts/google_sans_flex.ttf"),
  Fraunces(BundledFont.Fraunces, "fonts/fraunces.ttf"),
  BricolageGrotesque(BundledFont.BricolageGrotesque, "fonts/bricolage_grotesque.ttf"),
  BigShouldersDisplay(BundledFont.BigShouldersDisplay, "fonts/big_shoulders_display.ttf"),
  Doto(BundledFont.Doto, "fonts/doto.ttf"),
  GoogleSansCode(BundledFont.GoogleSansCode, "fonts/google_sans_code.ttf");

  val family: String
    get() = font.family

  companion object {
    /** The face named [family], ignoring case; null for names the app does not bundle. */
    fun byFamily(family: String?): FontFace? {
      val font = family?.let { BundledFont.byFamily(it.trim(), ignoreCase = true) }
      return entries.firstOrNull { it.font == font }
    }
  }
}

/** One axis of a variable font, such as `wght` 500. */
data class FontAxis(val tag: String, val value: Float)

/** Builds and caches the Compose font families for the bundled variable fonts. */
object ProposalFonts {
  /** The weights a variable font accepts. */
  val WEIGHTS = 1..1000

  const val SYMBOLS_RESOURCE = "fonts/material_symbols_rounded.ttf"

  private val cache = ConcurrentHashMap<Key, FontFamily>()
  private val symbolCache = ConcurrentHashMap<Pair<Boolean, Int>, FontFamily>()
  private val bases = ConcurrentHashMap<String, SkiaTypeface>()

  private data class Key(val face: FontFace, val weight: Int, val axes: List<FontAxis>)

  /**
   * How many fonts have been loaded from the classpath: one per resource, however many variants.
   */
  internal val baseTypefaceCount: Int
    get() = bases.size

  /**
   * The typeface of a bundled font with its default axes, loaded once per resource. Every variant
   * is a clone of it. Compose would otherwise copy the whole file into native memory for each
   * variant and keep each copy in its font cache for the life of the process, which for the 15 MB
   * symbols font at up to 58 variants is close to a gigabyte. A clone shares the font data.
   */
  private fun baseOf(resource: String): SkiaTypeface =
    bases.getOrPut(resource) {
      val bytes =
        checkNotNull(ProposalFonts::class.java.classLoader.getResourceAsStream(resource)) {
            "missing font resource $resource"
          }
          .use { it.readAllBytes() }
      checkNotNull(FontMgr.default.makeFromData(Data.makeFromBytes(bytes))) {
        "cannot read font resource $resource"
      }
    }

  /**
   * A family holding [face] at [weight] with the extra variation [axes]. A variable font needs one
   * entry per weight, so each text style gets the family built for its own weight. A weight outside
   * [WEIGHTS] is brought to the nearest one and an axis value that is not finite is left out,
   * because Compose throws on them and the validator is not the only way a value gets here.
   */
  fun family(face: FontFace, weight: Int, axes: List<FontAxis> = emptyList()): FontFamily {
    val clamped = weight.coerceIn(WEIGHTS)
    val usable = axes.filter { it.tag != "wght" && it.value.isFinite() }
    return cache.getOrPut(Key(face, clamped, usable)) {
      val settings = listOf("wght" to clamped.toFloat()) + usable.map { it.tag to it.value }
      variableFont(face.resource, settings)
    }
  }

  /** Material Symbols Rounded at weight 400, grade 0, with the optical size matching [sizeDp]. */
  fun symbols(filled: Boolean, sizeDp: Int): FontFamily {
    val opticalSize = sizeDp.coerceIn(20, 48)
    return symbolCache.getOrPut(filled to opticalSize) {
      variableFont(
        SYMBOLS_RESOURCE,
        settings =
          listOf(
            "FILL" to if (filled) 1f else 0f,
            "GRAD" to 0f,
            "opsz" to opticalSize.toFloat(),
            "wght" to 400f,
          ),
      )
    }
  }

  /**
   * A family of [resource] cloned with the axes in [settings]. Its alias names those axes: Compose
   * keys its typeface cache by identity, weight and style and not by the variation, so two
   * typefaces that differ only in their axes would otherwise share whichever one a resolver loaded
   * first, and which one that is would depend on the order things were drawn in. Weight comes from
   * the `wght` setting, so the family answers every requested weight with the same face.
   */
  private fun variableFont(resource: String, settings: List<Pair<String, Float>>): FontFamily {
    val variations = settings.map { (tag, value) -> SkiaFontVariation(tag, value) }
    val typeface = baseOf(resource).makeClone(variations.toTypedArray(), 0)
    val alias = "$resource?" + settings.joinToString("&") { (tag, value) -> "$tag=$value" }
    return FontFamily(Typeface(typeface, alias))
  }

  private val AXIS = Regex("""['"]([A-Za-z0-9]{4})['"]\s*(-?\d+(?:\.\d+)?)""")

  /** Reads a CSS `font-variation-settings` string such as `'opsz' 72, 'SOFT' 50`. */
  fun parseVariation(value: String?): List<FontAxis> =
    value
      ?.let { text ->
        AXIS.findAll(text).map { FontAxis(it.groupValues[1], it.groupValues[2].toFloat()) }.toList()
      }
      .orEmpty()
}
