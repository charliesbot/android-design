package bot.charlies.androiddesign.preview.feature.preview.sandbox

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import bot.charlies.androiddesign.preview.domain.findings.Contrast
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt
import org.jetbrains.skia.Bitmap

/**
 * Measured foreground color and 2 px perimeter worst-percentile background color for one text node.
 */
internal data class SampledContrast(val fgHex: String, val bgHex: String, val ratio: Double)

/**
 * Samples pixels in a 2 px outer perimeter band around each drawn line's ink box (zero glyph
 * anti-aliasing contamination) and evaluates WCAG contrast at the 10th-worst luminance percentile
 * (`p90` when `L_fg > 0.179`, `p10` otherwise).
 */
internal object PixelContrastSampler {
  private const val LIGHT_FOREGROUND_LUMINANCE = 0.179
  private const val LIGHT_BG_PERCENTILE = 0.90
  private const val DARK_BG_PERCENTILE = 0.10
  private const val PERIMETER_PX = 2

  fun sample(
    bitmap: Bitmap,
    fgColor: Color,
    lineRectsPx: List<Rect>,
    excludeRectsPx: List<Rect> = lineRectsPx,
  ): SampledContrast? {
    val effectiveFg = if (fgColor == Color.Unspecified) Color.Black else fgColor
    if (effectiveFg.alpha <= 0f) return null
    val intRects = toIntRects(bitmap, lineRectsPx)
    if (intRects.isEmpty()) return null
    val excludeIntRects =
      if (excludeRectsPx === lineRectsPx) intRects else toIntRects(bitmap, excludeRectsPx)

    val opaqueFgLum = Contrast.relativeLuminance(colorToHex(effectiveFg)) ?: return null
    var worst: SampledContrast? = null
    for (line in intRects) {
      val samples = collectPerimeterSamples(bitmap, line, excludeIntRects)
      if (samples.isEmpty()) continue
      samples.sortBy { it.luminance }
      val pct =
        if (opaqueFgLum > LIGHT_FOREGROUND_LUMINANCE) LIGHT_BG_PERCENTILE else DARK_BG_PERCENTILE
      val idx = floor((samples.size - 1) * pct).toInt().coerceIn(0, samples.lastIndex)
      val picked = samples[idx]
      val compositedHex = compositeOver(effectiveFg, picked.argb)
      val ratio = Contrast.ratio(compositedHex, picked.hex) ?: continue
      if (worst == null || ratio < worst.ratio) {
        worst = SampledContrast(fgHex = compositedHex, bgHex = picked.hex, ratio = ratio)
      }
    }
    return worst
  }

  private fun toIntRects(bitmap: Bitmap, rects: List<Rect>): List<IntRect> =
    rects.mapNotNull { rect ->
      val left = floor(rect.left).toInt().coerceIn(0, bitmap.width)
      val top = floor(rect.top).toInt().coerceIn(0, bitmap.height)
      val right = ceil(rect.right).toInt().coerceIn(0, bitmap.width)
      val bottom = ceil(rect.bottom).toInt().coerceIn(0, bitmap.height)
      if (right > left && bottom > top) IntRect(left, top, right, bottom) else null
    }

  private fun compositeOver(fg: Color, bgArgb: Int): String {
    val srgb = fg.convert(ColorSpaces.Srgb)
    val alpha = srgb.alpha.coerceIn(0f, 1f)
    if (alpha >= 0.999f) return colorToHex(srgb)
    val bgR = ((bgArgb ushr 16) and 0xFF) / 255f
    val bgG = ((bgArgb ushr 8) and 0xFF) / 255f
    val bgB = (bgArgb and 0xFF) / 255f
    val r = ((srgb.red * alpha + bgR * (1f - alpha)) * 255f).roundToInt().coerceIn(0, 255)
    val g = ((srgb.green * alpha + bgG * (1f - alpha)) * 255f).roundToInt().coerceIn(0, 255)
    val b = ((srgb.blue * alpha + bgB * (1f - alpha)) * 255f).roundToInt().coerceIn(0, 255)
    return "#%02X%02X%02X".format(r, g, b)
  }

  private data class PixelSample(val luminance: Double, val hex: String, val argb: Int)

  private fun collectPerimeterSamples(
    bitmap: Bitmap,
    line: IntRect,
    allLines: List<IntRect>,
  ): MutableList<PixelSample> {
    val minX = (line.left - PERIMETER_PX).coerceAtLeast(0)
    val maxX = (line.right + PERIMETER_PX).coerceAtMost(bitmap.width)
    val minY = (line.top - PERIMETER_PX).coerceAtLeast(0)
    val maxY = (line.bottom + PERIMETER_PX).coerceAtMost(bitmap.height)
    val samples = ArrayList<PixelSample>()
    for (y in minY until maxY) {
      for (x in minX until maxX) {
        if (allLines.any { it.contains(x, y) }) continue
        val argb = bitmap.getColor(x, y)
        val r = (argb ushr 16) and 0xFF
        val g = (argb ushr 8) and 0xFF
        val b = argb and 0xFF
        val hex = "#%02X%02X%02X".format(r, g, b)
        val lum = Contrast.relativeLuminance(hex) ?: continue
        samples += PixelSample(lum, hex, argb)
      }
    }
    return samples
  }

  internal fun colorToHex(color: Color): String {
    val srgb = color.convert(ColorSpaces.Srgb)
    val r = (srgb.red * 255f).roundToInt().coerceIn(0, 255)
    val g = (srgb.green * 255f).roundToInt().coerceIn(0, 255)
    val b = (srgb.blue * 255f).roundToInt().coerceIn(0, 255)
    return "#%02X%02X%02X".format(r, g, b)
  }

  private data class IntRect(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    fun contains(x: Int, y: Int): Boolean = x in left until right && y in top until bottom
  }
}
