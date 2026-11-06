package bot.charlies.androiddesign.preview.domain

import bot.charlies.androiddesign.preview.domain.export.ChildItems
import bot.charlies.androiddesign.preview.domain.export.childItems
import bot.charlies.androiddesign.preview.model.Catalogs
import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.PropKind
import bot.charlies.androiddesign.preview.model.Screen
import kotlinx.serialization.json.JsonPrimitive

/**
 * Where each overlay of a screen draws. A menu opens beside a component: the one named by its
 * `anchor`, or the SplitButton that names it as its `menu`. A tooltip hangs off the component named
 * by its `anchor`. A search view opens from the SearchBar or search app bar that names it as its
 * `expanded`. A snackbar draws in the snackbar slot of the screen's [snackbarScaffold], above its
 * FAB and navigation bar. Every other overlay (a sheet, a dialog, a menu nobody holds) draws at
 * screen level. The renderer and the exporter both follow this, so the preview and the exported
 * code place overlays the same way.
 */
class OverlayPlacement(private val screen: Screen) {
  private val overlayNames: Set<String> = Catalogs.of(screen).overlayNames

  private val anchored: Map<String, String> = buildMap {
    for (node in screen.components) {
      if (node.component != MENU) continue
      val anchorId = node.stringProp("anchor") ?: continue
      putIfAbsent(anchorId, node.id)
    }
  }

  private val tooltips: Map<String, String> = buildMap {
    for (node in screen.components) {
      if (node.component != TOOLTIP) continue
      val anchorId = node.stringProp("anchor") ?: continue
      putIfAbsent(anchorId, node.id)
    }
  }

  /** The search view each bar names as its `expanded`, by the bar's id. */
  private val expandedViews: Map<String, String> = buildMap {
    for (node in screen.components) {
      if (node.component != SEARCH_BAR && !SearchBars.isSearchAppBar(node)) continue
      val viewId = node.stringProp("expanded") ?: continue
      if (screen.components.none { it.id == viewId && it.component == SEARCH_VIEW }) continue
      if (viewId !in values) put(node.id, viewId)
    }
  }

  /** The tooltip's anchor, by the tooltip's id; a component without a node is not an anchor. */
  private val tooltipAnchors: Map<String, String> =
    tooltips.entries.associate { (anchorId, tooltipId) -> tooltipId to anchorId }

  private val parents: Map<String, String> = buildMap {
    for (node in screen.components) {
      val def = Catalogs.of(screen).find(node.component) ?: continue
      for (prop in def.props) {
        val ids =
          when (prop.kind) {
            PropKind.Child -> listOfNotNull(node.stringProp(prop.name))
            PropKind.ChildList ->
              when (val items = node.props[prop.name].childItems()) {
                is ChildItems.Listed -> items.ids
                is ChildItems.Template -> listOf(items.componentId)
                null -> emptyList()
              }
            else -> emptyList()
          }
        // An anchor names the component a tooltip or menu hangs off; it is not a child.
        if (prop.name == ANCHOR) continue
        ids.forEach { putIfAbsent(it, node.id) }
      }
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

  /** The id of the tooltip anchored to [componentId] by `Tooltip.anchor`, or null. */
  fun tooltipAt(componentId: String): String? = tooltips[componentId]

  /** The id of the search view that [componentId], a bar, names as its `expanded`, or null. */
  fun expandedViewOf(componentId: String): String? = expandedViews[componentId]

  /** The id of the bar that holds search view [viewId], or null when nothing holds it. */
  fun barOf(viewId: String): String? = expandedViews.entries.firstOrNull { it.value == viewId }?.key

  /**
   * The side of its anchor that tooltip [tooltipId] opens on, or null for an unknown tooltip.
   * [rich] is the tooltip's variant at the size being drawn, which the caller reads with the size
   * class overrides laid over it.
   */
  fun tooltipSide(tooltipId: String, rich: Boolean): TooltipSpec.Side? {
    if (screen.components.none { it.id == tooltipId && it.component == TOOLTIP }) return null
    return TooltipSpec.side(
      rich,
      anchorInAppBar = tooltipAnchors[tooltipId]?.let(::inAppBar) == true,
    )
  }

  /** Whether [componentId] is, or sits inside, a TopAppBar. */
  private fun inAppBar(componentId: String): Boolean {
    val seen = mutableSetOf<String>()
    var current: String? = componentId
    while (current != null && seen.add(current)) {
      if (screen.components.any { it.id == current && it.component == TOP_APP_BAR }) return true
      current = parents[current]
    }
    return false
  }

  /** The string prop [key], or null when it is absent or not a string. */
  private fun ComponentNode.stringProp(key: String): String? =
    (props[key] as? JsonPrimitive)?.takeIf { it.isString }?.content

  private fun isSnackbarInScaffold(node: ComponentNode): Boolean =
    node.component == SNACKBAR && snackbarScaffold != null

  private fun isBesideComponent(node: ComponentNode): Boolean =
    when (node.component) {
      MENU -> node.id in anchored.values || node.id in splitButtonMenus
      TOOLTIP -> node.id in tooltips.values
      SEARCH_VIEW -> node.id in expandedViews.values
      else -> false
    }

  private companion object {
    const val MENU = "Menu"
    const val SPLIT_BUTTON = "SplitButton"
    const val SCAFFOLD = "Scaffold"
    const val SNACKBAR = "Snackbar"
    const val TOOLTIP = "Tooltip"
    const val SEARCH_BAR = "SearchBar"
    const val SEARCH_VIEW = "SearchView"
    const val TOP_APP_BAR = "TopAppBar"
    const val ANCHOR = "anchor"
  }
}
