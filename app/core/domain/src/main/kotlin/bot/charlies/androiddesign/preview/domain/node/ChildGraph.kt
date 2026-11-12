package bot.charlies.androiddesign.preview.domain.node

import bot.charlies.androiddesign.preview.model.Catalog
import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.PropKind
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** The value of a `ChildList` prop: listed component ids, or a template over a data array. */
sealed interface ChildItems {
  class Listed(val ids: List<String>) : ChildItems

  class Template(val componentId: String, val path: String) : ChildItems
}

/** This prop value as [ChildItems], or null when it is neither a list nor a complete template. */
fun JsonElement?.childItems(): ChildItems? =
  when (this) {
    is JsonArray -> ChildItems.Listed(mapNotNull { it.childId() })
    is JsonObject -> {
      val componentId = (this["componentId"] as? JsonPrimitive)?.content
      val path = (this["path"] as? JsonPrimitive)?.content
      if (componentId != null && path != null) ChildItems.Template(componentId, path) else null
    }
    else -> null
  }

/** This prop value as the component id a `Child` or `Anchor` prop names, or null. */
fun JsonElement?.childId(): String? = (this as? JsonPrimitive)?.takeIf { it.isString }?.content

/**
 * The tree a screen's components form through their catalog's child props. It is the one answer
 * every layer uses to "what is inside this node": the validator, the exporter, the renderer and the
 * overlay placement. An [PropKind.Anchor] is a reference to a component drawn elsewhere, never a
 * child.
 */
object ChildGraph {
  /** A child [id] that a node holds through its child prop [prop]. */
  class ChildRef(val prop: String, val id: String)

  /**
   * The ids [node] holds through its child props, a template counting once by its component id.
   * With [withSizeOverrides], ids named only in a `sizeClass` override count too, for questions
   * about every size at once (does any size form a cycle?).
   */
  fun childIds(
    node: ComponentNode,
    catalog: Catalog,
    withSizeOverrides: Boolean = false,
  ): List<String> {
    val sources = buildList {
      add(node.props)
      if (withSizeOverrides) {
        (node.props[Catalog.SIZE_CLASS] as? JsonObject)?.values?.forEach {
          (it as? JsonObject)?.let(::add)
        }
      }
    }
    return sources.flatMap { props -> childRefs(node.component, props, catalog).map { it.id } }
  }

  /**
   * The children a component named [component] holds through the child props in [props], which may
   * be its base props or the props in effect at one size. A template counts once by its component
   * id.
   */
  fun childRefs(
    component: String,
    props: Map<String, JsonElement>,
    catalog: Catalog,
  ): List<ChildRef> {
    val def = catalog.find(component) ?: return emptyList()
    return def.props.flatMap { prop ->
      val value = props[prop.name]
      val ids =
        when (prop.kind) {
          PropKind.Child -> listOfNotNull(value.childId())
          PropKind.ChildList ->
            when (val items = value.childItems()) {
              is ChildItems.Listed -> items.ids
              is ChildItems.Template -> listOf(items.componentId)
              null -> emptyList()
            }
          else -> emptyList()
        }
      ids.map { ChildRef(prop.name, it) }
    }
  }
}
