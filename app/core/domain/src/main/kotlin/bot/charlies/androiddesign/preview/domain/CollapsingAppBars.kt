package bot.charlies.androiddesign.preview.domain

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * The one answer to "does this top app bar collapse as the content scrolls", read by the renderer
 * and the exporter so the preview and the exported Scaffold cannot disagree. Medium and large
 * flexible app bars collapse to the small bar; the other sizes do not.
 */
object CollapsingAppBars {
  private const val TOP_APP_BAR = "TopAppBar"
  private val COLLAPSING_SIZES = setOf("medium", "large")

  /** Whether [component] with [props] (already laid over by the size class overrides) collapses. */
  fun isCollapsing(component: String, props: JsonObject): Boolean =
    component == TOP_APP_BAR && (props["size"] as? JsonPrimitive)?.content in COLLAPSING_SIZES
}
