package bot.charlies.androiddesign.preview.feature.preview.sandbox

import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt

/** Raw RGBA_8888 pixel frame in row-major `[R, G, B, A]` byte order. */
class RawRgbaFrame(val width: Int, val height: Int, val bytes: ByteArray)

internal data class SampledContrast(val fgHex: String, val bgHex: String, val ratio: Double)

/**
 * 256-entry sRGB LUT contrast sampler operating directly on raw RGBA_8888 pixel bytes without
 * per-pixel `String.format` or hex parsing.
 */
internal object LutPixelContrastSampler {
  private const val LIGHT_FOREGROUND_LUMINANCE = 0.179
  private const val LIGHT_BG_PERCENTILE = 0.90
  private const val DARK_BG_PERCENTILE = 0.10

  private val SRGB_LUT =
    DoubleArray(256) { i ->
      val c = i / 255.0
      if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }

  fun sample(
    frame: RawRgbaFrame,
    fgColor: androidx.compose.ui.graphics.Color,
    lineRectsPx: List<androidx.compose.ui.geometry.Rect>,
    excludeRectsPx: List<androidx.compose.ui.geometry.Rect> = lineRectsPx,
    perimeterPx: Int = 2,
  ): SampledContrast? {
    val effectiveFg =
      if (fgColor == androidx.compose.ui.graphics.Color.Unspecified) {
        androidx.compose.ui.graphics.Color.Black
      } else {
        fgColor
      }
    if (effectiveFg.alpha <= 0f) return null
    val intRects = toIntRects(frame, lineRectsPx)
    if (intRects.isEmpty()) return null
    val excludeIntRects =
      if (excludeRectsPx === lineRectsPx) intRects else toIntRects(frame, excludeRectsPx)

    val srgbFg = effectiveFg.convert(androidx.compose.ui.graphics.colorspace.ColorSpaces.Srgb)
    val fgR = (srgbFg.red * 255f).roundToInt().coerceIn(0, 255)
    val fgG = (srgbFg.green * 255f).roundToInt().coerceIn(0, 255)
    val fgB = (srgbFg.blue * 255f).roundToInt().coerceIn(0, 255)
    val opaqueFgLum = 0.2126 * SRGB_LUT[fgR] + 0.7152 * SRGB_LUT[fgG] + 0.0722 * SRGB_LUT[fgB]
    val fgAlpha = srgbFg.alpha.coerceIn(0f, 1f)

    var worst: SampledContrast? = null
    val band = perimeterPx.coerceAtLeast(1)

    var lumBuf = DoubleArray(512)
    var rgbBuf = IntArray(512)

    for (line in intRects) {
      val minX = (line.left - band).coerceAtLeast(0)
      val maxX = (line.right + band).coerceAtMost(frame.width)
      val minY = (line.top - band).coerceAtLeast(0)
      val maxY = (line.bottom + band).coerceAtMost(frame.height)
      val maxCap = (maxX - minX) * (maxY - minY)
      if (maxCap <= 0) continue
      if (lumBuf.size < maxCap) {
        lumBuf = DoubleArray(maxCap)
        rgbBuf = IntArray(maxCap)
      }
      var count = 0
      val bytes = frame.bytes
      val stride = frame.width * 4
      for (y in minY until maxY) {
        val rowOff = y * stride
        for (x in minX until maxX) {
          var excluded = false
          for (i in excludeIntRects.indices) {
            if (excludeIntRects[i].contains(x, y)) {
              excluded = true
              break
            }
          }
          if (excluded) continue
          val off = rowOff + x * 4
          val r = bytes[off].toInt() and 0xFF
          val g = bytes[off + 1].toInt() and 0xFF
          val b = bytes[off + 2].toInt() and 0xFF
          lumBuf[count] = 0.2126 * SRGB_LUT[r] + 0.7152 * SRGB_LUT[g] + 0.0722 * SRGB_LUT[b]
          rgbBuf[count] = (r shl 16) or (g shl 8) or b
          count++
        }
      }
      if (count == 0) continue

      // IEEE754 non-negative luminance quantized into upper bits sorts identically with RGB
      // payload.
      val packed = LongArray(count)
      for (i in 0 until count) {
        val qLum = (lumBuf[i] * 1_000_000_000.0).toLong()
        packed[i] = (qLum shl 24) or (rgbBuf[i].toLong() and 0xFFFFFFL)
      }
      packed.sort()

      val pct =
        if (opaqueFgLum > LIGHT_FOREGROUND_LUMINANCE) LIGHT_BG_PERCENTILE else DARK_BG_PERCENTILE
      val idx = floor((count - 1) * pct).toInt().coerceIn(0, count - 1)
      val pickedRgb = (packed[idx] and 0xFFFFFFL).toInt()
      val bgR = (pickedRgb ushr 16) and 0xFF
      val bgG = (pickedRgb ushr 8) and 0xFF
      val bgB = pickedRgb and 0xFF

      val effR: Int
      val effG: Int
      val effB: Int
      if (fgAlpha >= 0.999f) {
        effR = fgR
        effG = fgG
        effB = fgB
      } else {
        effR =
          ((srgbFg.red * fgAlpha + (bgR / 255f) * (1f - fgAlpha)) * 255f)
            .roundToInt()
            .coerceIn(0, 255)
        effG =
          ((srgbFg.green * fgAlpha + (bgG / 255f) * (1f - fgAlpha)) * 255f)
            .roundToInt()
            .coerceIn(0, 255)
        effB =
          ((srgbFg.blue * fgAlpha + (bgB / 255f) * (1f - fgAlpha)) * 255f)
            .roundToInt()
            .coerceIn(0, 255)
      }
      val fgLum = 0.2126 * SRGB_LUT[effR] + 0.7152 * SRGB_LUT[effG] + 0.0722 * SRGB_LUT[effB]
      val bgLum = 0.2126 * SRGB_LUT[bgR] + 0.7152 * SRGB_LUT[bgG] + 0.0722 * SRGB_LUT[bgB]
      val ratio = (maxOf(fgLum, bgLum) + 0.05) / (minOf(fgLum, bgLum) + 0.05)

      if (worst == null || ratio < worst.ratio) {
        worst =
          SampledContrast(
            fgHex = "#%02X%02X%02X".format(Locale.ROOT, effR, effG, effB),
            bgHex = "#%02X%02X%02X".format(Locale.ROOT, bgR, bgG, bgB),
            ratio = ratio,
          )
      }
    }
    return worst
  }

  private fun toIntRects(
    frame: RawRgbaFrame,
    rects: List<androidx.compose.ui.geometry.Rect>,
  ): List<IntRect> = rects.mapNotNull { rect ->
    val left = floor(rect.left).toInt().coerceIn(0, frame.width)
    val top = floor(rect.top).toInt().coerceIn(0, frame.height)
    val right = ceil(rect.right).toInt().coerceIn(0, frame.width)
    val bottom = ceil(rect.bottom).toInt().coerceIn(0, frame.height)
    if (right > left && bottom > top) IntRect(left, top, right, bottom) else null
  }

  private class IntRect(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    fun contains(x: Int, y: Int): Boolean = x in left until right && y in top until bottom
  }
}
