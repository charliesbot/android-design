package bot.charlies.androiddesign.preview.domain.navigation

import bot.charlies.androiddesign.preview.model.Destination
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.SizeClass

/**
 * What the app-level navigation of `navigation.destinations` shows, decided in one place. The
 * renderer draws it and the exporter writes code that makes the same decisions, so the two cannot
 * disagree about which component shows, which destination is selected, or whether it shows at all.
 *
 * The navigation is drawn once around the pane scaffold, outside every pane. Material: "the
 * navigation rail should be placed outside any panes".
 */
object AppNavigation {
  /** What is drawn around the screens. */
  sealed interface Shown {
    /** Nothing: the screen covers the whole window and is not a destination. */
    data object Hidden : Shown

    /** `ShortNavigationBar` below the content, at compact widths. */
    data object Bar : Shown

    /**
     * `WideNavigationRail` on the leading edge, full height, from medium widths up. [expanded]
     * rails show their labels beside the icons.
     */
    data class Rail(val expanded: Boolean) : Shown
  }

  /** The declared destinations; empty when the proposal declares none. */
  fun destinations(proposal: Proposal): List<Destination> =
    proposal.navigation.destinations.orEmpty()

  /**
   * The index of the selected destination: walking [backStack] from the top, the first entry that
   * is a destination. Null when none is, or when the proposal declares none.
   */
  fun selectedIndex(proposal: Proposal, backStack: List<String>): Int? {
    val destinations = destinations(proposal)
    for (screen in backStack.asReversed()) {
      val index = destinations.indexOfFirst { it.screen == screen }
      if (index >= 0) return index
    }
    return null
  }

  /**
   * Whether the navigation shows with [backStack] on the stack. It shows when the top screen is a
   * destination, and when the top screen has a pane role and is drawn beside a destination: the
   * pane scaffold has room for two panes ([twoPanesFit]) and the selected destination's screen has
   * a pane role too. Otherwise a screen that covers the whole window, such as a compact detail or a
   * flow, shows none.
   */
  fun isVisible(proposal: Proposal, backStack: List<String>, twoPanesFit: Boolean): Boolean {
    val destinations = destinations(proposal)
    val top = backStack.lastOrNull() ?: return false
    if (destinations.isEmpty()) return false
    if (destinations.any { it.screen == top }) return true
    val panes = proposal.navigation.panes
    if (top !in panes || !twoPanesFit) return false
    val selected = selectedIndex(proposal, backStack)?.let { destinations[it].screen }
    return selected != null && selected in panes
  }

  /**
   * What shows in a window of size class [size], given [backStack] and whether the pane scaffold
   * has room for two panes. The bar is for compact widths, the rail for medium and expanded; the
   * rail is expanded only at expanded widths and only when `railExpanded` asks for it.
   */
  fun shown(
    proposal: Proposal,
    size: SizeClass,
    backStack: List<String>,
    twoPanesFit: Boolean,
  ): Shown {
    if (!isVisible(proposal, backStack, twoPanesFit)) return Shown.Hidden
    return when (size) {
      SizeClass.Compact -> Shown.Bar
      SizeClass.Medium -> Shown.Rail(expanded = false)
      SizeClass.Expanded -> Shown.Rail(expanded = proposal.navigation.railExpanded == true)
    }
  }

  /** The back stack after a tap on the destination of [screen]: just that screen. */
  fun stackAfterTap(screen: String): List<String> = listOf(screen)
}
