package bot.charlies.androiddesign.preview.domain

/**
 * Material's modal side sheet measurements, in dp. Compose has no side sheet composable, so the
 * renderer and the exporter both compose one from a `Dialog` and a `Surface` with these numbers.
 */
object SideSheetSpec {
  /** The sheet's widest size. */
  const val MAX_WIDTH_DP = 400f

  /** The padding at the sheet's start, end, top and bottom. */
  const val PADDING_DP = 24f

  /** The start padding when the header has a back icon button. */
  const val START_PADDING_WITH_ICON_DP = 16f

  /** The gap between the header's elements, and between the header and the content. */
  const val HEADER_GAP_DP = 12f

  /** The radius of the corners on the edge that faces the content. */
  const val CORNER_DP = 16f
}
