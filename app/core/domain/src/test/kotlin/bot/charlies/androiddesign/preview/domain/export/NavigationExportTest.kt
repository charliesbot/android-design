package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.PreviewException
import bot.charlies.androiddesign.preview.domain.TestProposal
import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.has
import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.lacks
import bot.charlies.androiddesign.preview.model.Proposal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** A full export is a Navigation 3 host; one screen is just that screen. */
class NavigationExportTest {
  private fun flat(text: String) =
    text.replace(Regex("\\s+"), " ").replace(", )", ")").replace("( ", "(").replace(" .", ".")

  private fun export(proposal: Proposal, screen: String? = null) =
    flat(KotlinExporter.export(proposal, ExportFixtures.defaultDesign(), screen))

  private fun withNavigation(json: String) = TestProposal.valid().withNavigation(json).build()

  @Test
  fun `a full export has a route per screen and a NavDisplay with a back stack`() {
    val source = export(TestProposal.valid().build())
    has(source, "@Serializable internal data object PlatesRoute : NavKey")
    has(source, "@Serializable internal data object PlateDetailRoute : NavKey")
    lacks(source, "private data object")
    has(source, "fun TestApp() { TestTheme {")
    has(source, "val backStack = rememberNavBackStack(PlatesRoute)")
    assertTrue(
      source.contains(
        "NavDisplay( backStack = backStack, onBack = { backStack.removeLastOrNull() },"
          .replace("( ", "("),
      ),
    )
    has(source, "entryProvider = entryProvider {")
  }

  @Test
  fun `navigate and back actions become callbacks wired to the back stack`() {
    val source = export(TestProposal.valid().build())
    has(source, "onNavigateToPlateDetail: () -> Unit = {}")
    has(source, "PlatesScreen(onNavigateToPlateDetail = { backStack.add(PlateDetailRoute) })")
  }

  @Test
  fun `list and detail panes use the list detail strategy`() {
    val source = export(TestProposal.valid().build())
    has(source, "val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()")
    has(source, "sceneStrategies = listOf(listDetailStrategy)")
    has(source, "entry<PlatesRoute>(metadata = ListDetailSceneStrategy.listPane())")
    has(source, "entry<PlateDetailRoute>(metadata = ListDetailSceneStrategy.detailPane())")
  }

  private fun withPlaceholder() =
    TestProposal.valid()
      .withPickScreen()
      .withNavigation(
        """{"start":"plates","panes":{"plates":"list","plate_detail":"detail"},"placeholders":{"plates":"pick"}}""",
      )
      .build()

  @Test
  fun `a list pane passes its placeholder screen as the detail placeholder`() {
    val source = export(withPlaceholder())
    has(
      source,
      "entry<PlatesRoute>(metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { PickScreen() }))",
    )
    has(source, "fun PickScreen(")
  }

  @Test
  fun `a placeholder screen has no route or entry`() {
    val source = export(withPlaceholder())
    lacks(source, "PickRoute")
    has(source, "entry<PlateDetailRoute>(metadata = ListDetailSceneStrategy.detailPane())")
  }

  @Test
  fun `a list pane without a placeholder keeps the plain list pane`() {
    has(export(TestProposal.valid().build()), "ListDetailSceneStrategy.listPane())")
  }

  @Test
  fun `an extra pane uses the list detail strategy`() {
    val source =
      export(
        withNavigation("""{"start":"plates","panes":{"plates":"list","plate_detail":"extra"}}"""),
      )
    has(source, "ListDetailSceneStrategy.extraPane()")
  }

  @Test
  fun `supporting panes use the supporting pane strategy`() {
    val source =
      export(
        withNavigation(
          """{"start":"plates","panes":{"plates":"detail","plate_detail":"supporting"}}""",
        ),
      )
    has(source, "val supportingPaneStrategy = rememberSupportingPaneSceneStrategy<NavKey>()")
    has(source, "sceneStrategies = listOf(supportingPaneStrategy)")
    has(source, "entry<PlatesRoute>(metadata = SupportingPaneSceneStrategy.mainPane())")
    has(source, "entry<PlateDetailRoute>(metadata = SupportingPaneSceneStrategy.supportingPane())")
    lacks(source, "ListDetailSceneStrategy")
  }

  @Test
  fun `screens without pane roles use no scene strategy`() {
    val source = export(withNavigation("""{"start":"plates"}"""))
    lacks(source, "sceneStrategies")
    has(source, "entry<PlatesRoute> { PlatesScreen(")
  }

  @Test
  fun `one screen is exported without routes or a host`() {
    val source = export(TestProposal.valid().build(), "plate_detail")
    lacks(source, "NavDisplay")
    lacks(source, "Route")
    has(source, "fun PlateDetailScreen(")
    lacks(source, "fun PlatesScreen(")
    has(source, "fun TestTheme(")
  }

  @Test
  fun `an unknown screen is an error that names the screens`() {
    val error =
      assertFailsWith<PreviewException> {
        KotlinExporter.export(TestProposal.valid().build(), ExportFixtures.defaultDesign(), "nope")
      }
    assertTrue(error.message!!.contains("plates"), error.message)
  }

  // region navigation.destinations

  private val extraScreen =
    """{"id":"root","component":"Scaffold","content":"t"},{"id":"t","component":"Text","text":"x"}"""

  private fun withDestinations(
    destinations: String =
      """[{"screen":"plates","label":"Plates","icon":"restaurant"},""" +
        """{"screen":"saved","label":"Saved","icon":"favorite"},""" +
        """{"screen":"profile","label":"Profile","icon":"person"}]""",
    panes: String = """{"plates":"list","plate_detail":"detail"}""",
    extra: String = "",
  ) =
    TestProposal.valid()
      .withScreen("saved", extraScreen)
      .withScreen("profile", extraScreen)
      .withNavigation(
        """{"start":"plates","panes":$panes,"destinations":$destinations$extra}""",
      )
      .build()

  @Test
  fun `destinations wrap the NavDisplay in one NavigationSuiteScaffold`() {
    val source = export(withDestinations())
    has(source, "NavigationSuiteScaffold( navigationItems = {")
    has(source, "navigationSuiteType = navigationSuiteType, ) { NavDisplay(")
    assertEquals(1, Regex("NavigationSuiteScaffold\\(").findAll(source).count())
    assertEquals(1, Regex("NavDisplay\\(").findAll(source).count())
    has(source, "val navigationSuiteType = navigationSuiteTypeFor(showNavigation,")
    has(
      source,
      "import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold",
    )
  }

  @Test
  fun `each destination is one item that selects, taps and draws as the previewer does`() {
    val source = export(withDestinations())
    has(source, "val destinations = listOf<NavKey>(PlatesRoute, SavedRoute, ProfileRoute)")
    has(source, "val selectedRoute = backStack.lastOrNull { it in destinations }")
    has(
      source,
      "NavigationSuiteItem( selected = selectedRoute == SavedRoute, " +
        "onClick = { backStack.clear() backStack.add(SavedRoute) }, " +
        "icon = { Icon(painterResource(R.drawable.favorite), contentDescription = null) }, " +
        "label = { Text(\"Saved\") }, navigationSuiteType = navigationSuiteType, )",
    )
    lacks(source, "ShortNavigationBar(")
    lacks(source, "WideNavigationRail(")
  }

  @Test
  fun `the suite type is a compact bar, a collapsed rail from medium and none when hidden`() {
    val source = export(withDestinations())
    has(
      source,
      "private fun navigationSuiteTypeFor( showNavigation: Boolean, windowSizeClass: WindowSizeClass, )",
    )
    has(source, "!showNavigation -> NavigationSuiteType.None")
    has(source, "WIDTH_DP_MEDIUM_LOWER_BOUND) -> NavigationSuiteType.ShortNavigationBarCompact")
    has(source, "else -> NavigationSuiteType.WideNavigationRailCollapsed")
    lacks(source, "WideNavigationRailExpanded")
  }

  @Test
  fun `railExpanded adds the expanded rail from the expanded width`() {
    val source = export(withDestinations(extra = ""","railExpanded":true"""))
    has(
      source,
      "WIDTH_DP_EXPANDED_LOWER_BOUND) -> NavigationSuiteType.WideNavigationRailExpanded",
    )
  }

  @Test
  fun `the navigation shows on a destination and beside one where two panes fit`() {
    val source = export(withDestinations())
    has(source, "val paneRoutes = listOf<NavKey>(PlatesRoute, PlateDetailRoute)")
    has(
      source,
      "calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2()).maxHorizontalPartitions >= 2",
    )
    has(
      source,
      "val showNavigation = topRoute in destinations || " +
        "(topRoute in paneRoutes && twoPanes && selectedRoute in paneRoutes)",
    )
  }

  @Test
  fun `without pane roles the navigation shows on a destination only`() {
    val source = export(withDestinations(panes = "{}"))
    has(source, "val showNavigation = topRoute in destinations")
    lacks(source, "paneRoutes")
    lacks(source, "calculatePaneScaffoldDirective")
  }

  @Test
  fun `a selected icon and a badge reach the item`() {
    val source =
      export(
        withDestinations(
          """[{"screen":"plates","label":"Plates","icon":"restaurant","selectedIcon":"restaurant_menu","badge":3},""" +
            """{"screen":"saved","label":"Saved","icon":"favorite","badge":true},""" +
            """{"screen":"profile","label":"Profile","icon":"person","badge":"99+"}]""",
        ),
      )
    has(
      source,
      "painterResource(if (selectedRoute == PlatesRoute) R.drawable.restaurant_menu else R.drawable.restaurant)",
    )
    has(source, "badge = { Badge { Text(\"3\") } }")
    has(source, "badge = { Badge() }")
    has(source, "badge = { Badge { Text(\"99+\") } }")
  }

  @Test
  fun `a proposal without destinations exports no navigation suite`() {
    val source = export(TestProposal.valid().build())
    lacks(source, "NavigationSuite")
    lacks(source, "navigationSuiteTypeFor")
    lacks(source, "selectedRoute")
    has(source, "NavDisplay(")
  }

  @Test
  fun `screens draw no bar or rail of their own`() {
    val source = export(withDestinations())
    lacks(source, "bottomBar")
    lacks(source, "WideNavigationRailItem")
    lacks(source, "ShortNavigationBarItem")
  }

  // endregion
}
