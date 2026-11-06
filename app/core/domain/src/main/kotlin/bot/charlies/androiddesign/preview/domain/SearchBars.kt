package bot.charlies.androiddesign.preview.domain

import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.SizeClass
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * The one answer to "is this top app bar a search app bar", read by the placement, the validator,
 * the renderer and the exporter so they cannot disagree. The size is a prop, so a `sizeClass`
 * override can make a bar a search bar at some widths only.
 */
object SearchBars {
  private const val TOP_APP_BAR = "TopAppBar"
  private const val SEARCH = "search"

  /** Whether [component] with [props] (already laid over by the size class overrides) is one. */
  fun isSearchAppBar(component: String, props: JsonObject): Boolean =
    component == TOP_APP_BAR && (props["size"] as? JsonPrimitive)?.content == SEARCH

  /** Whether [node] is a search app bar at any size class. */
  fun isSearchAppBar(node: ComponentNode): Boolean =
    node.component == TOP_APP_BAR &&
      SizeClass.entries.any { size ->
        isSearchAppBar(node.component, JsonObject(node.props + node.sizeOverridesAt(size)))
      }
}
