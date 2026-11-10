package bot.charlies.androiddesign.preview.domain.layout

import bot.charlies.androiddesign.preview.domain.node.ChildGraph
import bot.charlies.androiddesign.preview.model.Catalog
import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.M3Component
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Whether [node] or anything under it grows its semantics bounds vertically, which a `FlexBox` item
 * must not do with the largest height `Constraints` can hold (the previewer and the exporter both
 * restore an unbounded height for such an item). In material3 that is a linear progress indicator
 * (wavy or not) and a vertical slider, which use `IncreaseVerticalSemanticsBounds`; a circular
 * indicator does not.
 *
 * [propsOf] gives the props to test for a node: one object for a renderer that draws a single size
 * class, one per size class for the exporter. A shape that is not a literal could be linear, so it
 * counts.
 */
fun growsSemanticsBounds(
  node: ComponentNode,
  byId: Map<String, ComponentNode>,
  catalog: Catalog,
  propsOf: (ComponentNode) -> List<JsonObject>,
): Boolean {
  val seen = mutableSetOf<String>()

  fun visit(current: ComponentNode): Boolean {
    if (!seen.add(current.id)) return false
    val props = propsOf(current)
    val own =
      when (current.component) {
        M3Component.ProgressIndicator.wire -> props.any { it.literal("shape") != "circular" }
        M3Component.Slider.wire -> props.any { it.literal("orientation") == "vertical" }
        else -> false
      }
    return own || ChildGraph.childIds(current, catalog).mapNotNull(byId::get).any(::visit)
  }
  return visit(node)
}

private fun JsonObject.literal(key: String): String? =
  (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content
