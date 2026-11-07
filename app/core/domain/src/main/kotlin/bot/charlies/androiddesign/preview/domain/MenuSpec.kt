package bot.charlies.androiddesign.preview.domain

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

  const val COLORS_VIBRANT = "vibrant"
}
