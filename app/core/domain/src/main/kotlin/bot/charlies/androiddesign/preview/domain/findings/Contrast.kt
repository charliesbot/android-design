package bot.charlies.androiddesign.preview.domain.findings

import kotlin.math.pow

/** WCAG 2 contrast math on `#RRGGBB` colors. */
object Contrast {
  /** Text at or above this size, in dp, needs only [LARGE_RATIO]. */
  const val LARGE_TEXT_DP = 24f

  /** Bold text at or above this size, in dp (14 pt), is also large. */
  const val LARGE_BOLD_TEXT_DP = 18.66f

  const val NORMAL_RATIO = 4.5
  const val LARGE_RATIO = 3.0

  private val HEX = Regex("^#([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})$")

  /** The WCAG relative luminance of [hex], or null when it is not `#RRGGBB`. */
  fun relativeLuminance(hex: String): Double? {
    val match = HEX.matchEntire(hex) ?: return null
    val (r, g, b) = match.groupValues.drop(1).map { linear(it.toInt(16) / 255.0) }
    return 0.2126 * r + 0.7152 * g + 0.0722 * b
  }

  /** The contrast ratio from 1 to 21 between two colors, or null when either is malformed. */
  fun ratio(first: String, second: String): Double? {
    val a = relativeLuminance(first) ?: return null
    val b = relativeLuminance(second) ?: return null
    return (maxOf(a, b) + 0.05) / (minOf(a, b) + 0.05)
  }

  /** The ratio text of this size and weight must reach: 3 for large text, 4.5 otherwise. */
  fun requiredRatio(sizeDp: Float, bold: Boolean): Double =
    if (sizeDp >= LARGE_TEXT_DP || (bold && sizeDp >= LARGE_BOLD_TEXT_DP)) LARGE_RATIO
    else NORMAL_RATIO

  private fun linear(channel: Double): Double =
    if (channel <= 0.04045) channel / 12.92 else ((channel + 0.055) / 1.055).pow(2.4)
}
