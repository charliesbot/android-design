package bot.charlies.androiddesign.preview.domain

import bot.charlies.androiddesign.preview.model.Catalogs
import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.Screen
import kotlinx.serialization.json.JsonPrimitive

/**
 * Where each overlay of a screen draws. A menu opens beside a component: the one named by its
 * `anchor`, or the SplitButton that names it as its `menu`. A snackbar draws in the snackbar slot
 * of the screen's [snackbarScaffold], above its FAB and navigation bar. Every other overlay (a
 * sheet, a dialog, a menu nobody holds) draws at screen level. The renderer and the exporter both
 * follow this, so the preview and the exported code place overlays the same way.
 */
class OverlayPlacement(screen: Screen) {
  private val overlayNames: Set<String> = Catalogs.of(screen).overlayNames

  private val anchored: Map<String, String> = buildMap {
    for (node in screen.components) {
      if (node.component != MENU) continue
      val anchorId = node.stringProp("anchor") ?: continue
      putIfAbsent(anchorId, node.id)
    }
  }

  private val splitButtonMenus: Set<String> =
    screen.components
      .filter { it.component == SPLIT_BUTTON }
      .mapNotNull { it.stringProp("menu") }
      .toSet()

  /**
   * The id of the Scaffold whose snackbar slot holds the screen's snackbars: the root when it is a
   * Scaffold, else the first Scaffold in document order. Null when the screen has none.
   */
  val snackbarScaffold: String? =
    screen.components.firstOrNull { it.id == ComponentNode.ROOT_ID && it.component == SCAFFOLD }?.id
      ?: screen.components.firstOrNull { it.component == SCAFFOLD }?.id

  /** The snackbars of the screen, in document order. They draw in [snackbarScaffold]. */
  val snackbars: List<ComponentNode> =
    if (snackbarScaffold == null) emptyList()
    else screen.components.filter { it.component == SNACKBAR }

  /** The overlays that draw at screen level, in document order. */
  val screenLevel: List<ComponentNode> =
    screen.components.filter { node ->
      node.component in overlayNames && !isBesideComponent(node) && !isSnackbarInScaffold(node)
    }

  /** The id of the menu anchored to [componentId] by `Menu.anchor`, or null. */
  fun menuAt(componentId: String): String? = anchored[componentId]

  /** The string prop [key], or null when it is absent or not a string. */
  private fun ComponentNode.stringProp(key: String): String? =
    (props[key] as? JsonPrimitive)?.takeIf { it.isString }?.content

  private fun isSnackbarInScaffold(node: ComponentNode): Boolean =
    node.component == SNACKBAR && snackbarScaffold != null

  private fun isBesideComponent(node: ComponentNode): Boolean =
    node.component == MENU && (node.id in anchored.values || node.id in splitButtonMenus)

  private companion object {
    const val MENU = "Menu"
    const val SPLIT_BUTTON = "SplitButton"
    const val SCAFFOLD = "Scaffold"
    const val SNACKBAR = "Snackbar"
  }
}
