package bot.charlies.androiddesign.preview.domain

import bot.charlies.androiddesign.preview.model.CarouselItemSize

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

  /**
   * The width of the small item a hero carousel keeps beside its large one, and the widest a small
   * item gets (`CarouselDefaults.MaxSmallItemSize`).
   */
  const val SMALL_ITEM_DP = 56f

  /** What a hero reserves of the available width: both paddings, the small item and one gap. */
  const val HERO_RESERVED_DP = PADDING_DP * 2 + SMALL_ITEM_DP + GAP_DP
}

/**
 * Whether a node with an `itemSize` shows on a carousel item, from the item's current [size] and
 * the [maxSize] its carousel gives (what `CarouselItemScope.carouselItemDrawInfo` reports, in
 * pixels; [density] converts the small item's dp). The renderer calls [isShown]; the exporter
 * writes [expression], so both use the same thresholds.
 *
 * `carouselItemDrawInfo.minSize` is not used: it is the size of the anchor items that scroll off
 * the edge (a few dp), not of the small item on screen. `maxSize` is a few dp above the large item
 * on screen, so "full size" is a fraction of it rather than an exact match.
 */
object CarouselItemGate {
  /**
   * An item at this fraction of `maxSize` or more counts as large. Material's own strategy treats a
   * medium item within 0.85 of the large one as large (`CarouselDefaults.
   * MediumLargeItemDiffThreshold`), and large items on screen measure 96 percent or more.
   */
  const val LARGE_FRACTION = 0.85f

  fun isShown(minimum: CarouselItemSize, size: Float, maxSize: Float, density: Float): Boolean {
    val full = size >= maxSize * LARGE_FRACTION
    return when (minimum) {
      CarouselItemSize.Large -> full
      // A carousel whose items are all small has no smaller item to hide on.
      CarouselItemSize.Medium -> full || size > CarouselSpec.SMALL_ITEM_DP * density
    }
  }

  /**
   * [isShown] as a Kotlin condition over [info], the name of a `CarouselItemDrawInfo`;
   * [smallItemPx] is the expression for the small item's width in pixels.
   */
  fun expression(minimum: CarouselItemSize, info: String, smallItemPx: String): String {
    val full = "$info.size >= $info.maxSize * ${LARGE_FRACTION}f"
    return when (minimum) {
      CarouselItemSize.Large -> full
      CarouselItemSize.Medium -> "$full || $info.size > $smallItemPx"
    }
  }
}
