package bot.charlies.androiddesign.preview.domain

/**
 * What `render_view` returns: one screen at one view as PNG bytes, [widthPx] by [heightPx] pixels
 * (the view's dp size at [PIXELS_PER_DP]). A scrolling Wear list also has [end], the same view with
 * the list scrolled to its end. Each picture is saved to a file for harnesses that do not show MCP
 * images, and [png] and [end] say where.
 */
class ViewRender(
  val screen: String,
  val view: String,
  val widthPx: Int,
  val heightPx: Int,
  val png: RenderedPng,
  val end: RenderedPng? = null,
) {
  companion object {
    /** `render_view` draws at this density: a desktop view is 1920x1080, small enough to read. */
    const val PIXELS_PER_DP = 1f
  }
}

/** A PNG's bytes and the absolute path they were saved to. */
class RenderedPng(val bytes: ByteArray, val path: String)
