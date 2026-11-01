package bot.charlies.androiddesign.preview.domain.navigation

import bot.charlies.androiddesign.preview.model.ActionFunction
import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.NavigationEdge
import bot.charlies.androiddesign.preview.model.PropKind
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.SizeClass
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * The navigation graph of a proposal. Its edges are the `navigate` calls found in components, so
 * the actions stay the only source of truth for how screens connect.
 */
class NavigationGraph(private val proposal: Proposal) {
  val edges: List<NavigationEdge> = extractEdges()

  /** For every screen, the shortest `navigate` path from the start screen to it, start first. */
  val backStacks: Map<String, List<String>> = computeBackStacks()

  /** The back stack to render [screen] with; a screen the start cannot reach shows alone. */
  fun backStack(screen: String): List<String> = backStacks[screen] ?: listOf(screen)

  private fun extractEdges(): List<NavigationEdge> {
    val found = mutableListOf<NavigationEdge>()
    for ((screenId, screen) in proposal.screens) {
      for (node in screen.components) {
        for (target in navigateTargets(node)) found += NavigationEdge(screenId, target, node.id)
      }
    }
    return found.distinct()
  }

  private fun computeBackStacks(): Map<String, List<String>> {
    val adjacency = edges.groupBy({ it.from }, { it.to })
    val start = proposal.navigation.start
    val parents = linkedMapOf<String, String?>()
    if (start in proposal.screens) {
      parents[start] = null
      val queue = ArrayDeque(listOf(start))
      while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        for (next in adjacency[current].orEmpty()) {
          if (next in proposal.screens && next !in parents) {
            parents[next] = current
            queue.addLast(next)
          }
        }
      }
    }
    return proposal.screens.keys.associateWith { screen ->
      if (screen !in parents) {
        listOf(screen)
      } else {
        generateSequence(screen) { parents[it] }.toList().reversed()
      }
    }
  }

  companion object {
    /** The screens a component navigates to, from its props and its size class overrides. */
    fun navigateTargets(node: ComponentNode): List<String> {
      val def = M3ExpressiveCatalog.find(node.component) ?: return emptyList()
      val actionProps = def.props.filter { it.kind == PropKind.Action }.map { it.name }
      val sources =
        buildList<JsonObject> {
          add(node.props)
          val overrides = node.props[M3ExpressiveCatalog.SIZE_CLASS] as? JsonObject
          SizeClass.entries.forEach { size ->
            (overrides?.get(size.wire) as? JsonObject)?.let(::add)
          }
        }
      return sources.flatMap { source ->
        actionProps.mapNotNull { name -> navigateTarget(source[name]) }
      }
    }

    private fun navigateTarget(action: JsonElement?): String? {
      val call = (action as? JsonObject)?.get("functionCall") as? JsonObject ?: return null
      if ((call["@call"] as? JsonPrimitive)?.content != ActionFunction.Navigate.wire) return null
      val args = call["args"] as? JsonObject ?: return null
      return (args[ActionFunction.Navigate.argument!!] as? JsonPrimitive)
        ?.takeIf { it.isString }
        ?.content
    }
  }
}
