package bot.charlies.androiddesign.preview.domain

/**
 * What Material fixes about tooltips. The renderer, the exporter and the validator all read it, so
 * the preview and the exported code place a tooltip on the same side of its anchor.
 */
object TooltipSpec {
  /** A rich tooltip has "up to two text buttons". */
  const val MAX_ACTIONS = 2

  /** The gap between a rich tooltip's text buttons, which sit side by side. */
  const val ACTION_GAP_DP = 8f

  /** The side of its anchor a tooltip opens on; [compose] names the `TooltipAnchorPosition`. */
  enum class Side(val compose: String) {
    Above("Above"),
    Below("Below"),
  }

  /**
   * Plain tooltips sit directly above their anchor, "if the element is in an app bar, the plain
   * tooltip appears below the element". A rich tooltip opens below its anchor, to the bottom right
   * by default, so it does not cover the element.
   */
  fun side(rich: Boolean, anchorInAppBar: Boolean): Side =
    if (rich || anchorInAppBar) Side.Below else Side.Above
}
