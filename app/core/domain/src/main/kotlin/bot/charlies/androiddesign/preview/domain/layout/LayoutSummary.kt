package bot.charlies.androiddesign.preview.domain.layout

import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.M3Component
import bot.charlies.androiddesign.preview.model.NavigationNodes
import bot.charlies.androiddesign.preview.model.SurfaceSpec
import bot.charlies.androiddesign.preview.model.SurfaceSpecs
import kotlin.math.roundToInt
import kotlinx.serialization.Serializable

/**
 * What an agent needs to see that a view adapts, without the tree: which screens are on show, which
 * navigation component, and the main regions of each screen.
 *
 * Panes sit side by side, so a wider view shows more of them, and its navigation moves from a bar
 * to a rail. Compare the summaries of two views to see what the larger one reveals.
 */
@Serializable
data class ViewLayout(
  /**
   * The navigation components shown: `NavigationBar`, `NavigationRail` or `FloatingToolbar`, joined
   * with `+` if more than one is, or `none`.
   */
  val nav: String,
  /**
   * The screens shown, left to right, each with its regions: the direct children of the screen's
   * content root that are laid out, as `Type id width` with the width in whole dp. A screen with
   * more than [LayoutSummary.MAX_REGIONS] regions ends with `+N more`.
   */
  val panes: Map<String, List<String>>,
)

/** Reads a [ViewLayout] off a rendered [LayoutTree]. */
object LayoutSummary {
  const val NO_NAV = "none"

  /**
   * The most regions listed for one screen, which keeps a view's summary to a few hundred bytes.
   */
  const val MAX_REGIONS = 8

  const val PANE_PREFIX = "@pane/"

  /** Summarizes a rendered [tree] from its hierarchy under [surface]. */
  fun of(tree: LayoutTree, surface: SurfaceSpec = SurfaceSpecs.forView(tree.view)): ViewLayout {
    val children = tree.nodes.groupBy { it.parent }
    val roots =
      tree.nodes
        .filter { it.parent == null && it.isLaidOut && it.id != NavigationNodes.CONTAINER_ID }
        .sortedBy { it.bounds.x }
    val paneEntries =
      if (roots.any { it.id.startsWith(PANE_PREFIX) }) {
        val byPane = linkedMapOf<String, MutableList<LayoutNode>>()
        for (root in roots) {
          if (!root.id.startsWith(PANE_PREFIX)) continue
          val paneName = root.id.removePrefix(PANE_PREFIX).substringBefore('/')
          byPane.getOrPut(paneName) { mutableListOf() } += root
        }
        byPane.map { (paneName, groupRoots) ->
          val paneChildren = groupRoots.flatMap {
            directContentChildren(it, children).ifEmpty { listOf(it) }
          }
          Pane(paneName, "$PANE_PREFIX$paneName/", paneChildren)
        }
      } else {
        val contentNodes = roots.flatMap {
          directContentChildren(it, children).ifEmpty { listOf(it) }
        }
        listOf(Pane(tree.screen, "", contentNodes))
      }
    return ViewLayout(
      nav = navigation(tree, surface),
      panes = paneEntries.associate { it.screen to regions(it) },
    )
  }

  private fun directContentChildren(
    root: LayoutNode,
    children: Map<String?, List<LayoutNode>>,
  ): List<LayoutNode> {
    val kids = children[root.id].orEmpty().filter { it.isLaidOut }
    if (root.type != M3Component.Scaffold.wire) return kids
    val inner = kids.firstOrNull {
      it.type != M3Component.TopAppBar.wire && it.type != M3Component.FloatingActionButton.wire
    }
    return inner?.let {
      children[it.id].orEmpty().filter { child -> child.isLaidOut }.ifEmpty { listOf(it) }
    } ?: kids
  }

  private class Pane(
    val screen: String,
    val prefix: String,
    val children: List<LayoutNode>,
  )

  private fun navigation(tree: LayoutTree, surface: SurfaceSpec): String {
    val shown = tree.nodes.map { it.type }.toSet()
    return surface.navigationNames.filter { it in shown }.joinToString("+").ifEmpty { NO_NAV }
  }

  private fun regions(pane: Pane): List<String> {
    val listed =
      pane.children.take(MAX_REGIONS).map { child ->
        "${child.type} ${child.id.removePrefix(pane.prefix)} ${child.bounds.width.roundToInt()}"
      }
    val more = pane.children.size - listed.size
    return if (more > 0) listed + "+$more more" else listed
  }
}
