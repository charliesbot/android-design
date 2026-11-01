package bot.charlies.androiddesign.preview.domain

import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.Screen
import kotlinx.serialization.json.JsonPrimitive

/**
 * Where each overlay of a screen draws. A menu opens beside a component: the one named by its
 * `anchor`, or the SplitButton that names it as its `menu`. Every other overlay (a sheet, a dialog,
 * a menu nobody holds) draws at screen level. The renderer and the exporter both follow this, so
 * the preview and the exported code place overlays the same way.
 */
class OverlayPlacement(screen: Screen) {
  private val anchored: Map<String, String> = buildMap {
    for (node in screen.components) {
      if (node.component != MENU) continue
      val anchor = node.props["anchor"] as? JsonPrimitive
      val anchorId = anchor?.takeIf { it.isString }?.content ?: continue
      putIfAbsent(anchorId, node.id)
    }
  }

  private val splitButtonMenus: Set<String> =
    screen.components
      .filter { it.component == SPLIT_BUTTON }
      .mapNotNull {
        (it.props["menu"] as? JsonPrimitive)?.takeIf { menu -> menu.isString }?.content
      }
      .toSet()

  /** The overlays that draw at screen level, in document order. */
  val screenLevel: List<ComponentNode> =
    screen.components.filter { node ->
      node.component in M3ExpressiveCatalog.overlayNames && !isBesideComponent(node)
    }

  /** The id of the menu anchored to [componentId] by `Menu.anchor`, or null. */
  fun menuAt(componentId: String): String? = anchored[componentId]

  private fun isBesideComponent(node: ComponentNode): Boolean =
    node.component == MENU && (node.id in anchored.values || node.id in splitButtonMenus)

  private companion object {
    const val MENU = "Menu"
    const val SPLIT_BUTTON = "SplitButton"
  }
}
