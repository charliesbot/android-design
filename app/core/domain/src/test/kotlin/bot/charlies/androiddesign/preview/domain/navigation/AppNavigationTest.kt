package bot.charlies.androiddesign.preview.domain.navigation

import bot.charlies.androiddesign.preview.domain.navigation.AppNavigation.Shown
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.SizeClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json

/**
 * The decisions the renderer and the exporter's code both follow: which destination is selected,
 * whether the navigation shows, and which component draws.
 */
class AppNavigationTest {
  private val catalogId = "https://github.com/charliesbot/android-design/catalogs/m3-expressive/v1"

  /** A portfolio app: list and detail panes, plus screens that cover the window. */
  private fun proposal(
    panes: String = """{"portfolio":"list","stock":"detail","markets":"list"}""",
    railExpanded: Boolean? = null,
  ): Proposal {
    fun screen() =
      """{"catalogId":"$catalogId","components":[{"id":"root","component":"Text","text":"x"}]}"""
    val expanded = railExpanded?.let { ""","railExpanded":$it""" }.orEmpty()
    return Json.decodeFromString(
      Proposal.serializer(),
      """{"format":1,"navigation":{"start":"portfolio","panes":$panes,
        "destinations":[
          {"screen":"portfolio","label":"Portfolio","icon":"pie_chart"},
          {"screen":"markets","label":"Markets","icon":"show_chart"},
          {"screen":"account","label":"Account","icon":"person"}]$expanded},
        "screens":{"portfolio":${screen()},"stock":${screen()},"markets":${screen()},"account":${screen()},"flow":${screen()}}}""",
    )
  }

  @Test
  fun `the selected destination is the first one from the top of the back stack`() {
    val p = proposal()
    assertEquals(0, AppNavigation.selectedIndex(p, listOf("portfolio")))
    assertEquals(0, AppNavigation.selectedIndex(p, listOf("portfolio", "stock")))
    assertEquals(1, AppNavigation.selectedIndex(p, listOf("portfolio", "markets")))
    assertEquals(
      2,
      AppNavigation.selectedIndex(p, listOf("portfolio", "markets", "account", "flow")),
    )
    assertEquals(null, AppNavigation.selectedIndex(p, listOf("flow")))
  }

  @Test
  fun `a tap leaves just the destination on the back stack`() {
    assertEquals(listOf("markets"), AppNavigation.stackAfterTap("markets"))
  }

  @Test
  fun `a destination on top shows the navigation at any width`() {
    val p = proposal()
    for (twoPanes in listOf(false, true)) {
      assertEquals(true, AppNavigation.isVisible(p, listOf("account"), twoPanes))
      assertEquals(
        true,
        AppNavigation.isVisible(p, listOf("portfolio", "stock", "markets"), twoPanes),
      )
    }
  }

  @Test
  fun `a pane beside a destination shows it only where two panes fit`() {
    val p = proposal()
    val stack = listOf("portfolio", "stock")
    assertEquals(true, AppNavigation.isVisible(p, stack, twoPanesFit = true))
    // A compact detail covers the window.
    assertEquals(false, AppNavigation.isVisible(p, stack, twoPanesFit = false))
  }

  @Test
  fun `a pane is not beside a destination that has no pane role`() {
    val p = proposal(panes = """{"portfolio":"list","stock":"detail"}""")
    // markets is a destination with no pane role, so a detail reached from it has no list beside.
    assertEquals(false, AppNavigation.isVisible(p, listOf("markets", "stock"), twoPanesFit = true))
    assertEquals(true, AppNavigation.isVisible(p, listOf("portfolio", "stock"), twoPanesFit = true))
  }

  @Test
  fun `a screen that is no destination and has no pane role shows none`() {
    val p = proposal()
    assertEquals(false, AppNavigation.isVisible(p, listOf("portfolio", "flow"), twoPanesFit = true))
    assertEquals(false, AppNavigation.isVisible(p, listOf("flow"), twoPanesFit = true))
  }

  @Test
  fun `a pane with no selected destination shows none`() {
    assertEquals(false, AppNavigation.isVisible(proposal(), listOf("stock"), twoPanesFit = true))
  }

  @Test
  fun `a proposal without destinations shows none and selects none`() {
    val p =
      Json.decodeFromString(
        Proposal.serializer(),
        """{"format":1,"navigation":{"start":"a"},"screens":{"a":{"catalogId":"$catalogId","components":[{"id":"root","component":"Text","text":"x"}]}}}""",
      )
    assertEquals(emptyList(), AppNavigation.destinations(p))
    assertEquals(null, AppNavigation.selectedIndex(p, listOf("a")))
    assertEquals(Shown.Hidden, AppNavigation.shown(p, SizeClass.Expanded, listOf("a"), true))
  }

  @Test
  fun `a bar is for compact and a rail for medium and expanded`() {
    val p = proposal()
    val stack = listOf("portfolio")
    assertEquals(Shown.Bar, AppNavigation.shown(p, SizeClass.Compact, stack, false))
    assertEquals(Shown.Rail(false), AppNavigation.shown(p, SizeClass.Medium, stack, false))
    assertEquals(Shown.Rail(false), AppNavigation.shown(p, SizeClass.Expanded, stack, true))
  }

  @Test
  fun `railExpanded expands the rail at expanded widths only`() {
    val p = proposal(railExpanded = true)
    val stack = listOf("portfolio")
    assertEquals(Shown.Bar, AppNavigation.shown(p, SizeClass.Compact, stack, false))
    assertEquals(Shown.Rail(false), AppNavigation.shown(p, SizeClass.Medium, stack, false))
    assertEquals(Shown.Rail(true), AppNavigation.shown(p, SizeClass.Expanded, stack, true))
    assertEquals(
      Shown.Rail(false),
      AppNavigation.shown(proposal(railExpanded = false), SizeClass.Expanded, stack, true),
    )
  }

  @Test
  fun `a hidden navigation stays hidden at every size`() {
    val p = proposal()
    for (size in SizeClass.entries) {
      assertEquals(Shown.Hidden, AppNavigation.shown(p, size, listOf("flow"), false), size.name)
    }
  }
}
