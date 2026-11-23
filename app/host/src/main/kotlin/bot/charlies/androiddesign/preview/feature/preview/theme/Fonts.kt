package bot.charlies.androiddesign.preview.feature.preview.theme

import android.content.res.AssetManager
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import bot.charlies.androiddesign.preview.model.BundledFont
import java.util.concurrent.ConcurrentHashMap

enum class FontFace(val font: BundledFont, val assetPath: String) {
  GoogleSansFlex(BundledFont.GoogleSansFlex, "fonts/google_sans_flex.ttf"),
  Fraunces(BundledFont.Fraunces, "fonts/fraunces.ttf"),
  BricolageGrotesque(BundledFont.BricolageGrotesque, "fonts/bricolage_grotesque.ttf"),
  BigShouldersDisplay(BundledFont.BigShouldersDisplay, "fonts/big_shoulders_display.ttf"),
  Doto(BundledFont.Doto, "fonts/doto.ttf"),
  GoogleSansCode(BundledFont.GoogleSansCode, "fonts/google_sans_code.ttf");

  val family: String
    get() = font.family

  companion object {
    fun byFamily(family: String?): FontFace? {
      val font = family?.let { BundledFont.byFamily(it.trim(), ignoreCase = true) }
      return entries.firstOrNull { it.font == font }
    }
  }
}

data class FontAxis(val tag: String, val value: Float)

object ProposalFonts {
  val WEIGHTS = 1..1000
  const val SYMBOLS_ASSET = "fonts/material_symbols_rounded.ttf"
  const val WEAR_ROBOTO_FLEX_ASSET = "fonts/roboto_flex.ttf"

  @Volatile private var assetManager: AssetManager? = null

  private val cache = ConcurrentHashMap<Key, FontFamily>()
  private val wearCache = ConcurrentHashMap<Pair<Int, List<FontAxis>>, FontFamily>()
  private val symbolCache = ConcurrentHashMap<Pair<Boolean, Int>, FontFamily>()
  private val reverseFaces = ConcurrentHashMap<FontFamily, FontFace>()
  private val wearFamilies = ConcurrentHashMap.newKeySet<FontFamily>()

  private data class Key(val face: FontFace, val weight: Int, val axes: List<FontAxis>)

  fun init(assets: AssetManager) {
    if (assetManager == null) {
      assetManager = assets
    }
  }

  private fun requireAssets(): AssetManager =
    checkNotNull(assetManager) { "ProposalFonts.init(assets) must be called before creating fonts" }

  fun faceOf(family: FontFamily?): FontFace? = family?.let(reverseFaces::get)

  fun isWearDefaultFont(family: FontFamily?): Boolean = family != null && family in wearFamilies

  fun isSymbolFont(family: FontFamily?): Boolean = family != null && family in symbolCache.values

  fun family(face: FontFace, weight: Int, axes: List<FontAxis> = emptyList()): FontFamily {
    val clamped = weight.coerceIn(WEIGHTS)
    val usable = axes.filter { it.tag != "wght" && it.value.isFinite() }
    return cache.getOrPut(Key(face, clamped, usable)) {
      val settings = listOf("wght" to clamped.toFloat()) + usable.map { it.tag to it.value }
      variableFont(face.assetPath, clamped, settings).also { reverseFaces[it] = face }
    }
  }

  fun wearRobotoFlex(weight: Int, axes: List<FontAxis> = emptyList()): FontFamily {
    val clamped = weight.coerceIn(WEIGHTS)
    val usable = axes.filter { it.tag != "wght" && it.value.isFinite() }
    return wearCache.getOrPut(clamped to usable) {
      val settings = listOf("wght" to clamped.toFloat()) + usable.map { it.tag to it.value }
      variableFont(WEAR_ROBOTO_FLEX_ASSET, clamped, settings).also { wearFamilies += it }
    }
  }

  fun symbols(filled: Boolean, sizeDp: Int): FontFamily {
    val opticalSize = sizeDp.coerceIn(20, 48)
    return symbolCache.getOrPut(filled to opticalSize) {
      variableFont(
        SYMBOLS_ASSET,
        400,
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

  private fun variableFont(
    assetPath: String,
    weight: Int,
    settings: List<Pair<String, Float>>,
  ): FontFamily {
    val variationSettings =
      FontVariation.Settings(
        *settings.map { (tag, value) -> FontVariation.Setting(tag, value) }.toTypedArray(),
      )
    return FontFamily(
      Font(
        path = assetPath,
        assetManager = requireAssets(),
        weight = FontWeight(weight),
        style = FontStyle.Normal,
        variationSettings = variationSettings,
      ),
    )
  }

  private val AXIS = Regex("""['"]([A-Za-z0-9]{4})['"]\s*(-?\d+(?:\.\d+)?)""")

  fun parseVariation(value: String?): List<FontAxis> =
    value
      ?.let { text ->
        AXIS.findAll(text).map { FontAxis(it.groupValues[1], it.groupValues[2].toFloat()) }.toList()
      }
      .orEmpty()
}
