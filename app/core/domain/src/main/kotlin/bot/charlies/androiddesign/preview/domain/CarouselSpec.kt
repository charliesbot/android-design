package bot.charlies.androiddesign.preview.domain

/**
 * Material's carousel measurements, in dp. The renderer and the exporter both read them, so the
 * preview and the exported code size a carousel the same way.
 */
object CarouselSpec {
  /** The leading and trailing content padding of every layout but full-screen, which has none. */
  const val PADDING_DP = 16f

  /** The gap between items. */
  const val GAP_DP = 8f

  /** The gap between full-screen items. */
  const val FULL_SCREEN_GAP_DP = 16f

  /** The width of the small item a hero carousel keeps beside its large one. */
  const val SMALL_ITEM_DP = 56f

  /** What a hero reserves of the available width: both paddings, the small item and one gap. */
  const val HERO_RESERVED_DP = PADDING_DP * 2 + SMALL_ITEM_DP + GAP_DP
}
