package bot.charlies.androiddesign.preview.domain.navigation

import bot.charlies.androiddesign.preview.domain.TestProposal
import bot.charlies.androiddesign.preview.model.NavigationEdge
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.Screen
import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationGraphTest {
  private fun navigate(to: String) =
    """{"functionCall":{"@call":"navigate","args":{"screen":"$to"}}}"""

  private fun Proposal.withScreen(id: String): Proposal =
    copy(
      screens = screens + (id to Screen(screens.getValue("plates").catalogId, null, emptyList())),
    )

  @Test
  fun `edges are the navigate calls found in components`() {
    val graph = NavigationGraph(TestProposal.valid().build())
    assertEquals(listOf(NavigationEdge("plates", "plate_detail", "open_plate")), graph.edges)
  }

  @Test
  fun `back and open actions are not edges`() {
    val p =
      TestProposal.valid()
        .withDetail(
          "back",
          """{"id":"back","component":"IconButton","icon":"arrow_back","action":{"functionCall":{"@call":"back"}}}""",
        )
        .build()
    assertEquals(1, NavigationGraph(p).edges.size)
  }

  @Test
  fun `navigate calls inside size class overrides count`() {
    val p =
      TestProposal.valid()
        .withDetail(
          "go",
          """{"id":"go","component":"Button","label":"Plates","sizeClass":{"expanded":{"action":${navigate("plates")}}}}""",
        )
        .build()
    assertEquals(
      listOf(
        NavigationEdge("plates", "plate_detail", "open_plate"),
        NavigationEdge("plate_detail", "plates", "go"),
      ),
      NavigationGraph(p).edges,
    )
  }

  @Test
  fun `the back stack is the shortest navigate path from start`() {
    val p =
      TestProposal.valid()
        .with(
          "shortcut",
          """{"id":"shortcut","component":"Button","label":"Direct","action":${navigate("deep")}}""",
        )
        .withDetail(
          "next",
          """{"id":"next","component":"Button","label":"Deep","action":${navigate("deep")}}""",
        )
        .build()
        .withScreen("deep")
    val graph = NavigationGraph(p)
    assertEquals(listOf("plates"), graph.backStack("plates"))
    assertEquals(listOf("plates", "plate_detail"), graph.backStack("plate_detail"))
    assertEquals(listOf("plates", "deep"), graph.backStack("deep"))
  }

  @Test
  fun `a screen two hops away has both hops in its back stack`() {
    val p =
      TestProposal.valid()
        .withDetail(
          "next",
          """{"id":"next","component":"Button","label":"More","action":${navigate("extra")}}""",
        )
        .build()
        .withScreen("extra")
    assertEquals(listOf("plates", "plate_detail", "extra"), NavigationGraph(p).backStack("extra"))
  }

  @Test
  fun `an unreachable screen shows alone`() {
    val p = TestProposal.valid().withNavigation("""{"start":"plate_detail"}""").build()
    assertEquals(listOf("plates"), NavigationGraph(p).backStack("plates"))
    assertEquals(listOf("plate_detail"), NavigationGraph(p).backStack("plate_detail"))
  }

  @Test
  fun `back stacks cover every screen`() {
    val graph = NavigationGraph(TestProposal.valid().build())
    assertEquals(setOf("plates", "plate_detail"), graph.backStacks.keys)
  }
}
