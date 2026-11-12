package bot.charlies.androiddesign.preview.domain.export.wear

import bot.charlies.androiddesign.preview.domain.export.wear.WearExportFixtures.has
import bot.charlies.androiddesign.preview.domain.export.wear.WearExportFixtures.lacks
import bot.charlies.androiddesign.preview.domain.export.wear.WearExportFixtures.root
import kotlin.test.Test

/** A full Wear export is a Navigation 3 host with Wear's swipe-to-dismiss scene. */
class WearNavigationExportTest {
  private val proposal =
    WearExportFixtures.screens(
      "home",
      "home" to
        listOf(
          root("go", "back_", props = """"timeText":true"""),
          """{"id":"go","component":"Button","label":"Settings","action":{"functionCall":{"@call":"navigate","args":{"screen":"settings"}}}}""",
          """{"id":"back_","component":"CompactButton","label":"Quit","action":{"functionCall":{"@call":"back"}}}""",
        ),
      "settings" to
        listOf(
          root("t", props = """"timeText":false"""),
          """{"id":"t","component":"Text","text":"Settings"}""",
        ),
    )
  private val source = WearExportFixtures.export(proposal)

  @Test
  fun `a route per screen and a NavDisplay with the swipe dismiss scene`() {
    has(source, "@Serializable internal data object HomeRoute : NavKey")
    has(source, "@Serializable internal data object SettingsRoute : NavKey")
    has(source, "val backStack = rememberNavBackStack(HomeRoute)")
    has(
      source,
      "NavDisplay(backStack = backStack, onBack = { backStack.removeLastOrNull() }, sceneStrategies = listOf(rememberSwipeDismissableSceneStrategy<NavKey>()),",
    )
    has(source, "import androidx.wear.compose.navigation3.rememberSwipeDismissableSceneStrategy")
    has(source, "entryProvider = entryProvider {")
  }

  @Test
  fun `navigate and back become callbacks wired to the back stack`() {
    has(source, "onNavigateToSettings: () -> Unit = {}")
    has(source, "onBack: () -> Unit = {}")
    has(
      source,
      "entry<HomeRoute> { HomeScreen(onBack = { backStack.removeLastOrNull() }, onNavigateToSettings = { backStack.add(SettingsRoute) }) }",
    )
    has(source, "entry<SettingsRoute> { SettingsScreen() }")
  }

  @Test
  fun `the scaffold is at the app root and a screen can turn the time text off`() {
    has(source, "fun WearTestApp() { WearAppTheme { AppScaffold(timeText = { TimeText() }) {")
    has(source, "fun SettingsScreen(modifier: Modifier = Modifier) {")
    has(source, "timeText = {}")
    lacks(
      source,
      "fun HomeScreen(modifier: Modifier = Modifier, onBack: () -> Unit = {}, onNavigateToSettings: () -> Unit = {}) { AppScaffold",
    )
  }

  @Test
  fun `a single screen has no navigation host`() {
    val one = WearExportFixtures.export(proposal, "settings")
    lacks(one, "NavDisplay")
    lacks(one, "Route")
  }

  @Test
  fun `a dialog's dismiss and open actions stay inside the screen`() {
    val dialogs =
      WearExportFixtures.export(
        WearExportFixtures.proposal(
          root("o"),
          """{"id":"o","component":"Button","label":"Reset","action":{"functionCall":{"@call":"open","args":{"overlay":"d"}}}}""",
          """{"id":"d","component":"AlertDialog","title":"Sure?","confirm":"y"}""",
          """{"id":"y","component":"IconButton","icon":"check","action":{"functionCall":{"@call":"dismiss"}}}""",
        ),
        "main",
      )
    has(dialogs, "var showD by remember { mutableStateOf(false) }")
    has(dialogs, "onClick = { showD = true }")
    has(dialogs, "AlertDialog(visible = showD, onDismissRequest = { showD = false },")
    lacks(dialogs, "NavDisplay") // opening a dialog is not navigation
  }
}
