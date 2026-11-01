package bot.charlies.androiddesign.preview.feature.preview.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import bot.charlies.androiddesign.preview.model.BundledFont
import java.util.concurrent.ConcurrentHashMap

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
  private val files = ConcurrentHashMap<String, ByteArray>()

  private data class Key(val face: FontFace, val weight: Int, val axes: List<FontAxis>)

  /**
   * The bytes of a bundled font, read from the classpath once. A font given to Compose by resource
   * name is read and inflated again by every scene that uses it, which for the 15 MB symbols font
   * dominates a headless render; bytes in memory only need copying.
   */
  private fun bytesOf(resource: String): ByteArray =
    files.getOrPut(resource) {
      checkNotNull(ProposalFonts::class.java.classLoader.getResourceAsStream(resource)) {
          "missing font resource $resource"
        }
        .use { it.readAllBytes() }
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
      FontFamily(variableFont(face.resource, clamped, settings))
    }
  }

  /** Material Symbols Rounded at weight 400, grade 0, with the optical size matching [sizeDp]. */
  fun symbols(filled: Boolean, sizeDp: Int): FontFamily {
    val opticalSize = sizeDp.coerceIn(20, 48)
    return symbolCache.getOrPut(filled to opticalSize) {
      FontFamily(
        variableFont(
          SYMBOLS_RESOURCE,
          weight = 400,
          settings =
            listOf(
              "FILL" to if (filled) 1f else 0f,
              "GRAD" to 0f,
              "opsz" to opticalSize.toFloat(),
              "wght" to 400f,
            ),
        ),
      )
    }
  }

  /**
   * A font from [resource] with the axes in [settings]. Its identity names those axes: Compose keys
   * its typeface cache by identity, weight and style and not by variation settings, so two fonts
   * that differ only in their axes would otherwise share whichever typeface a resolver loaded
   * first, and which one that is would depend on the order things were drawn in.
   */
  private fun variableFont(
    resource: String,
    weight: Int,
    settings: List<Pair<String, Float>>,
  ): Font =
    Font(
      identity = "$resource?" + settings.joinToString("&") { (tag, value) -> "$tag=$value" },
      data = bytesOf(resource),
      weight = FontWeight(weight),
      style = FontStyle.Normal,
      variationSettings =
        FontVariation.Settings(
          *settings.map { (tag, value) -> FontVariation.Setting(tag, value) }.toTypedArray(),
        ),
    )

  private val AXIS = Regex("""['"]([A-Za-z0-9]{4})['"]\s*(-?\d+(?:\.\d+)?)""")

  /** Reads a CSS `font-variation-settings` string such as `'opsz' 72, 'SOFT' 50`. */
  fun parseVariation(value: String?): List<FontAxis> =
    value
      ?.let { text ->
        AXIS.findAll(text).map { FontAxis(it.groupValues[1], it.groupValues[2].toFloat()) }.toList()
      }
      .orEmpty()
}
