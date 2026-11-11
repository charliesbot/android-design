package bot.charlies.androiddesign.preview.model

/**
 * The navigation a proposal declares in `navigation.destinations`. It is drawn once, around the
 * screens and outside every pane, so it is not a component of any screen, yet the layout tree
 * records it so the touch target, contrast, font and label rules cover it.
 *
 * The node types are the names of the Material components it stands for. The ids start with `@`,
 * which a component id cannot (they match `[A-Za-z_][A-Za-z0-9_]*`), so they never collide with the
 * id of a screen's component.
 */
object NavigationNodes {
  /** The proposal field that declares the navigation. */
  const val DECLARATION = "navigation.destinations"

  /** The bottom bar drawn at compact widths (`ShortNavigationBar`). */
  const val BAR = "NavigationBar"
  const val BAR_ITEM = "NavigationBarItem"

  /** The rail drawn from medium widths up (`WideNavigationRail`). */
  const val RAIL = "NavigationRail"
  const val RAIL_ITEM = "NavigationRailItem"

  /** The tree id of the bar or the rail. */
  const val CONTAINER_ID = "@navigation"

  /** The tree id of the item at [index], in declaration order. */
  fun itemId(index: Int): String = "$CONTAINER_ID[$index]"

  /** The types of the items, which are interactive and carry a label. */
  val itemTypes: Set<String> = setOf(BAR_ITEM, RAIL_ITEM)

  /**
   * The catalog components that this declaration replaced. A proposal that still uses one gets an
   * error that points at [DECLARATION].
   */
  val replacedComponents: Set<String> = setOf(BAR, BAR_ITEM, RAIL, RAIL_ITEM)

  /** The Scaffold slot that held the rail. */
  const val REPLACED_RAIL_SLOT = "navigationRail"
}
