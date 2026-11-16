package bot.charlies.androiddesign.preview.model

/** Node names and container ids recorded when Compose navigation chrome renders around a screen. */
object NavigationNodes {
  /** The bottom bar drawn at compact widths (`ShortNavigationBar`). */
  const val BAR = "NavigationBar"
  const val BAR_ITEM = "NavigationBarItem"

  /** The rail drawn from medium widths up (`WideNavigationRail`). */
  const val RAIL = "NavigationRail"
  const val RAIL_ITEM = "NavigationRailItem"

  /** Synthetic container id excluded from pane region summaries. */
  const val CONTAINER_ID = "@navigation"

  /** Interactive item types that carry a navigation label. */
  val itemTypes: Set<String> = setOf(BAR_ITEM, RAIL_ITEM)
}
