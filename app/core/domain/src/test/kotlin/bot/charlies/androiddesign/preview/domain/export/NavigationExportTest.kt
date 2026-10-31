package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.PreviewException
import bot.charlies.androiddesign.preview.domain.TestProposal
import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.has
import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.lacks
import bot.charlies.androiddesign.preview.model.Proposal
import kotlin.test.Test
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
    has(source, "@Serializable private data object PlatesRoute : NavKey")
    has(source, "@Serializable private data object PlateDetailRoute : NavKey")
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
}
