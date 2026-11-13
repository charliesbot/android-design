package bot.charlies.androiddesign.preview.domain

/**
 * What `render_view` returns: one screen at one view as PNG bytes, [widthPx] by [heightPx] pixels
 * (the view's dp size at [pixelsPerDp]). A scrolling Wear list also has [end], the same view with
 * the list scrolled to its end. Each picture is saved to a file for harnesses that do not show MCP
 * images, and [png] and [end] say where.
 */
class ViewRender(
  val screen: String,
  val view: String,
  val pixelsPerDp: Float,
  val widthPx: Int,
  val heightPx: Int,
  val png: RenderedPng,
  val end: RenderedPng? = null,
) {
  companion object {
    /** `render_view` draws at this density: a desktop view is 1920x1080, small enough to read. */
    const val PIXELS_PER_DP = 1f

    /** [PIXELS_PER_DP] as text, so a whole number prints as "1", not "1.0". */
    val pixelsPerDpText: String = formatDensity(PIXELS_PER_DP)

    /** Formats a density so a whole number has no decimal part. */
    fun formatDensity(value: Float): String =
      if (value % 1f == 0f) value.toInt().toString() else value.toString()
  }
}

/** A PNG's bytes and the absolute path they were saved to. */
class RenderedPng(val bytes: ByteArray, val path: String)
