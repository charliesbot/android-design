package bot.charlies.androiddesign.preview.domain

import bot.charlies.androiddesign.preview.domain.node.ChildItems
import bot.charlies.androiddesign.preview.domain.node.childItems
import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.M3Component
import bot.charlies.androiddesign.preview.model.SizeClass

/**
 * How a menu item behaves by its `role`, for the renderer and the exporter alike. Material:
 * single-select menus have "one item selected at a time" and multi-select menus "stay open until
 * the person dismisses the menu". Choosing an action or a single-select item is choosing an option,
 * which exits the menu.
 */
object MenuSpec {
  enum class Role(val wire: String, val closesMenu: Boolean) {
    Action("action", closesMenu = true),
    Single("single", closesMenu = true),
    Multi("multi", closesMenu = false);

    val selectable: Boolean
      get() = this != Action
  }

  /** The role named [wire], else [Role.Action], the catalog default. */
  fun role(wire: String?): Role = Role.entries.firstOrNull { it.wire == wire } ?: Role.Action

  /**
   * What a menu holds, whatever the size class: nothing drawn, items, groups, or both (invalid).
   */
  enum class Layout {
    Empty,
    Items,
    Groups,
    Mixed,
  }

  /**
   * The members of [menu] drawn at any size class: the union over the size classes of the `items`
   * in effect there, each kept where it is shown. A menu is laid out by this union, not by one size
   * class, so it is a menu of items or a menu of groups at every size.
   */
  fun drawnMembers(menu: ComponentNode, nodes: Map<String, ComponentNode>): List<ComponentNode> =
    SizeClass.entries
      .flatMap { size ->
        val ids =
          when (val items = (menu.props + menu.sizeOverridesAt(size))["items"].childItems()) {
            is ChildItems.Listed -> items.ids
            // A template repeats one component per data item: its members are that component.
            is ChildItems.Template -> listOf(items.componentId)
            null -> emptyList()
          }
        ids.mapNotNull(nodes::get).filter { it.isShownAt(size) }
      }
      .distinct()

  /** How [menu] is laid out; the renderer, the exporter and the validator all ask this. */
  fun layout(menu: ComponentNode, nodes: Map<String, ComponentNode>): Layout {
    val members = drawnMembers(menu, nodes)
    val groups = members.any { it.component == GROUP }
    val items = members.any { it.component != GROUP }
    return when {
      groups && items -> Layout.Mixed
      groups -> Layout.Groups
      items -> Layout.Items
      else -> Layout.Empty
    }
  }

  val GROUP = M3Component.MenuGroup.wire
}
