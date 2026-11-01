package bot.charlies.androiddesign.preview.domain.layout

import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.Proposal
import kotlin.math.roundToInt
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

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

  fun of(proposal: Proposal, tree: LayoutTree): ViewLayout {
    val children = tree.nodes.groupBy { it.parent }
    val panes =
      proposal.screens
        .mapNotNull { (screen, body) ->
          // The renderer prefixes the ids of every pane but the one that was asked for.
          val prefix = if (screen == tree.screen) "" else "$screen/"
          val root = tree.nodes.firstOrNull { it.id == prefix + ComponentNode.ROOT_ID }
          val content = contentRootOf(body.components)?.let { prefix + it }
          root?.let {
            Pane(
              screen,
              prefix,
              it.bounds.x,
              content?.let(children::get).orEmpty().filter { it.isLaidOut },
            )
          }
        }
        .sortedBy { it.x }
    return ViewLayout(
      nav = navigation(tree),
      panes = panes.associate { it.screen to regions(it) },
    )
  }

  private class Pane(
    val screen: String,
    val prefix: String,
    /** Where the pane starts, which orders the panes left to right. */
    val x: Float,
    /** The children of the screen's content root. */
    val children: List<LayoutNode>,
  )

  /** The id of a scaffold's content, or of the root itself for any other root. */
  private fun contentRootOf(components: List<ComponentNode>): String? {
    val root = components.firstOrNull { it.id == ComponentNode.ROOT_ID } ?: return null
    if (root.component != "Scaffold") return root.id
    return (root.props["content"] as? JsonPrimitive)?.takeIf { it.isString }?.content
  }

  private fun navigation(tree: LayoutTree): String {
    val shown = tree.nodes.map { it.type }.toSet()
    return M3ExpressiveCatalog.navigationNames
      .filter { it in shown }
      .joinToString("+")
      .ifEmpty { NO_NAV }
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
