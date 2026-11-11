package bot.charlies.androiddesign.preview.domain

import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.ViewSpec

/**
 * The colors `check_proposal` lists once for the whole proposal: for each brightness checked, the
 * `#RRGGBB` the theme gives each color role (by its proposal spelling) that a node of a checked
 * render draws with, as its container or its content. Nodes record the resolved color of each role
 * they draw with, straight from the theme's scheme, so a role has one color per brightness; a
 * second one means a node drew a role with something other than the scheme's value, and is an error
 * rather than a pick.
 */
class CheckedColors {
  private val byBrightness = mapOf(LIGHT to sortedMapOf<String, String>(), DARK to sortedMapOf())

  /** Adds the roles the nodes of [tree], a render at its view, draw with. */
  fun add(tree: LayoutTree) {
    val view = tree.view
    val roles = byBrightness.getValue(if (view.dark) DARK else LIGHT)
    for (node in tree.nodes) {
      record(roles, view, node.id, node.roles?.container, node.colors?.container)
      record(roles, view, node.id, node.roles?.content, node.colors?.content)
    }
  }

  /**
   * Light then dark, each with its roles sorted by name; a brightness that was not checked is out.
   */
  fun toMap(): Map<String, Map<String, String>> =
    byBrightness.filterValues { it.isNotEmpty() }.mapValues { it.value.toMap() }

  private fun record(
    roles: MutableMap<String, String>,
    view: ViewSpec,
    id: String,
    role: String?,
    hex: String?,
  ) {
    if (role == null || hex == null) return
    val known = roles.putIfAbsent(role, hex)
    check(known == null || known == hex) {
      "the role '$role' is $known and $hex in the ${view.name} view ($id); the theme gives it one"
    }
  }

  private companion object {
    const val LIGHT = "light"
    const val DARK = "dark"
  }
}
