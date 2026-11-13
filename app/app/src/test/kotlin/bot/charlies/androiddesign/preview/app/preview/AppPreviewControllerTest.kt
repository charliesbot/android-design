package bot.charlies.androiddesign.preview.app.preview

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.data.FileProposalRepository
import bot.charlies.androiddesign.preview.domain.PreviewException
import bot.charlies.androiddesign.preview.domain.ProposalCheck
import bot.charlies.androiddesign.preview.domain.ViewChange
import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.domain.catalog.CatalogJson
import bot.charlies.androiddesign.preview.domain.layout.ViewLayout
import bot.charlies.androiddesign.preview.feature.preview.viewer.ViewerViewModel
import bot.charlies.androiddesign.preview.model.Device
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.Views
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AppPreviewControllerTest {
  private val fixtures = Path.of(System.getProperty("fixtures.dir"))
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private val viewer = ViewerViewModel()
  private val window = PreviewWindow()
  private val controller =
    AppPreviewController(
      repository = FileProposalRepository(),
      viewer = viewer,
      window = window,
      renderer = HeadlessProposalRenderer(),
      scope = scope,
      version = AppInfo.VERSION,
    )

  @AfterTest
  fun stop() {
    scope.cancel()
  }

  /** The fixture and its DESIGN.md in a temp folder, so a test can edit the proposal. */
  private fun workspace(): Path {
    val dir = Files.createTempDirectory("controller")
    Files.copy(fixtures.resolve("DESIGN.md"), dir.resolve("DESIGN.md"))
    return dir.resolve("plates.proposal.json").also {
      Files.copy(fixtures.resolve("plates.proposal.json"), it)
    }
  }

  private fun LayoutTree.text(id: String) = nodes.first { it.id == id }.text?.value

  private suspend fun until(timeoutMs: Long = 5000, condition: () -> Boolean) {
    withTimeout(timeoutMs) {
      while (!condition()) delay(20)
    }
  }

  @Test
  fun `open loads the proposal, shows the window and reports the screens`() = runBlocking {
    val file = workspace()
    assertFalse(window.visible.value)

    val opened = controller.openProposal(file.toString())

    assertTrue(opened.valid, opened.errors.toString())
    assertEquals("Tonight's plate", opened.name)
    assertEquals("plates", opened.start)
    assertEquals(listOf("plates", "plate_detail"), opened.screens)
    assertEquals(file.parent.resolve("DESIGN.md").toString(), opened.designMd)
    assertTrue(window.visible.value, "open_proposal shows the window")
    assertEquals("plates", viewer.currentScreen)
  }

  @Test
  fun `open reports validation errors without failing`() = runBlocking {
    val file = workspace()
    Files.writeString(file, Files.readString(file).replace("\"Scaffold\"", "\"Scaffoldd\""))

    val opened = controller.openProposal(file.toString())

    assertFalse(opened.valid)
    assertTrue(opened.errors.any { "Scaffoldd" in it.message }, opened.errors.toString())
    val error = assertFailsWith<PreviewException> { controller.getTree() }
    assertTrue("Scaffoldd" in error.message.orEmpty(), "the tree call says what is wrong")
  }

  @Test
  fun `open fails for a file that does not exist`() {
    val error =
      assertFailsWith<PreviewException> {
        runBlocking { controller.openProposal("/no/such/plates.proposal.json") }
      }
    assertTrue("/no/such/plates.proposal.json" in error.message.orEmpty())
  }

  @Test
  fun `tools that need a proposal say to open one`() {
    for (call in
      listOf<suspend () -> Any>({ controller.getTree() }, { controller.exportKotlin() })) {
      val error = assertFailsWith<PreviewException> { runBlocking { call() } }
      assertTrue("open_proposal" in error.message.orEmpty(), error.message)
    }
  }

  @Test
  fun `a change to the file reloads the window on its own`() = runBlocking {
    val file = workspace()
    controller.openProposal(file.toString())
    assertEquals("Tonight's plate", viewer.state.value.name)

    Files.writeString(
      file,
      Files.readString(file)
        .replace("\"name\": \"Tonight's plate\"", "\"name\": \"Tomorrow's plate\""),
    )

    until { viewer.state.value.name == "Tomorrow's plate" }
  }

  @Test
  fun `a tree read right after an edit already shows the edit`() = runBlocking {
    val file = workspace()
    controller.openProposal(file.toString())
    assertEquals("Tonight's plate", controller.getTree().text("bar"))

    Files.writeString(
      file,
      Files.readString(file).replace("\"title\": \"Tonight's plate\"", "\"title\": \"Dinner\""),
    )

    assertEquals("Dinner", controller.getTree().text("bar"))
  }

  @Test
  fun `an invalid edit keeps the last good render and the tree call reports the errors`() =
    runBlocking {
      val file = workspace()
      controller.openProposal(file.toString())
      val good = viewer.state.value.host
      Files.writeString(file, Files.readString(file).replace("\"Scaffold\"", "\"Scaffoldd\""))
      until { !viewer.state.value.valid }

      assertTrue(viewer.state.value.host === good)
      assertFailsWith<PreviewException> { controller.getTree() }
      assertFalse(controller.status().errors.isEmpty())

      Files.writeString(file, Files.readString(fixtures.resolve("plates.proposal.json")))
      until { viewer.state.value.valid }
      assertEquals("Tonight's plate", controller.getTree().text("bar"))
    }

  @Test
  fun `get_tree with no arguments describes the window's current screen and view`() = runBlocking {
    controller.openProposal(workspace().toString())
    controller.setView(ViewChange(device = Device.Tablet, dark = true))
    viewer.selectScreen("plate_detail")

    val tree = controller.getTree()

    assertEquals("plate_detail", tree.screen)
    assertEquals(Device.Tablet, tree.view.device)
    assertTrue(tree.view.dark)
    assertEquals(1280, tree.view.width)
    // At tablet width the detail shows beside its list.
    assertTrue(tree.nodes.any { it.id.startsWith("plates/") }, "the list pane is drawn too")
  }

  @Test
  fun `render_view with no arguments draws what the window shows, live state replayed`() =
    runBlocking {
      controller.openProposal(workspace().toString())
      controller.setView(ViewChange(device = Device.Tablet))

      val plain = controller.renderView()

      assertEquals("plates", plain.screen)
      assertEquals("tablet", plain.view)
      assertEquals(1280, plain.widthPx)
      assertTrue(Files.isRegularFile(Path.of(plain.png.path)))
      assertNull(plain.end, "only a scrolling Wear list has an end picture")

      viewer.host().interaction.open("plates", "new_sheet")
      val replayed = controller.renderView()
      assertFalse(
        plain.png.bytes.contentEquals(replayed.png.bytes),
        "the window's open overlay is in the picture",
      )

      val fresh = controller.renderView(screen = "plates")
      assertTrue(fresh.png.bytes.contentEquals(plain.png.bytes), "a named screen drops the overlay")
      assertFailsWith<PreviewException> { controller.renderView(screen = "nope") }
      Unit
    }

  @Test
  fun `get_tree with arguments is a fresh render of the named screen and view`() = runBlocking {
    controller.openProposal(workspace().toString())
    controller.setView(ViewChange(dark = true))
    viewer.host().interaction.open("plates", "new_sheet")

    val tree = controller.getTree(screen = "plate_detail", view = "desktop")

    assertEquals("plate_detail", tree.screen)
    assertEquals("desktop", tree.view.name)
    assertFalse(tree.view.dark, "the named view decides, not the window")
    assertFalse(tree.nodes.any { it.id == "sheet_title" })

    val error = assertFailsWith<PreviewException> { controller.getTree(screen = "nope") }
    assertTrue("plates" in error.message.orEmpty(), "lists the screens that exist")
    assertFailsWith<PreviewException> { controller.getTree(view = "huge") }
    Unit
  }

  @Test
  fun `the tree of the window includes an opened overlay`() = runBlocking {
    controller.openProposal(workspace().toString())
    assertFalse(controller.getTree().nodes.any { it.id == "sheet_title" })

    viewer.host().interaction.open("plates", "new_sheet")
    val tree = controller.getTree()

    assertEquals("Add a plate", tree.text("sheet_title"))
    assertEquals("Not now", tree.text("sheet_close"))
    val button = tree.nodes.first { it.id == "sheet_close" }
    assertEquals("Button", button.a11y?.role)
    assertNotNull(button.touch)

    viewer.host().interaction.dismiss("plates")
    assertFalse(controller.getTree().nodes.any { it.id == "sheet_title" })
  }

  @Test
  fun `a hidden window still answers from a headless render`() = runBlocking {
    controller.openProposal(workspace().toString())
    controller.showPreview()
    assertTrue(window.visible.value)
    window.hide()

    val tree = controller.getTree()
    assertEquals("plates", tree.screen)
    assertEquals("Tonight's plate", tree.text("bar"))
    assertFalse(controller.status().windowVisible)

    controller.showPreview()
    assertTrue(controller.status().windowVisible)
  }

  @Test
  fun `set_view changes the window and the tree`() = runBlocking {
    controller.openProposal(workspace().toString())
    val before = controller.getTree().nodes.first { it.id == "new_plate" }.colors?.container

    val state =
      controller.setView(
        ViewChange(device = Device.Foldable, seed = "#0061A4", variant = PaletteVariant.Vibrant),
      )

    assertEquals(Device.Foldable, state.device)
    assertEquals("#0061A4", state.seed)
    assertEquals("vibrant", state.variant)
    assertFalse(state.dark)
    val tree = controller.getTree()
    assertEquals("foldable", tree.view.name)
    assertNotEquals(before, tree.nodes.first { it.id == "new_plate" }.colors?.container)

    // Omitted values keep what they were.
    assertEquals(Device.Foldable, controller.setView(ViewChange(dark = true)).device)
    assertEquals("#0061A4", controller.status().view?.seed)
  }

  @Test
  fun `set_view moves the window to a screen`() = runBlocking {
    controller.openProposal(workspace().toString())

    controller.setView(ViewChange(screen = "plate_detail", device = Device.Tablet))

    assertEquals("plate_detail", controller.getTree().screen)
    assertEquals(Device.Tablet, controller.status().view?.device)
    val error =
      assertFailsWith<PreviewException> { controller.setView(ViewChange(screen = "nope")) }
    assertTrue("plate_detail" in error.message.orEmpty(), error.message)
    assertEquals("plate_detail", controller.getTree().screen, "a bad screen changes nothing")
  }

  @Test
  fun `status reports the app, the proposal and the window`() = runBlocking {
    val before = controller.status()
    assertEquals(AppInfo.VERSION, before.version)
    assertEquals(M3ExpressiveCatalog.ID, before.catalogId)
    assertNull(before.proposal)
    assertFalse(before.windowVisible)

    val file = workspace()
    controller.openProposal(file.toString())
    controller.setView(ViewChange(device = Device.Desktop))
    val after = controller.status()
    assertEquals(file.toString(), after.proposal)
    assertTrue(after.windowVisible)
    assertEquals("plates", after.screen)
    assertEquals(Device.Desktop, after.view?.device)
    assertTrue(after.errors.isEmpty())
  }

  @Test
  fun `catalog and export come from the domain`() = runBlocking {
    assertTrue("Button: " in controller.getCatalog())
    assertTrue("\"protocolVersion\"" in CatalogJson.render(CatalogJson.catalog()))
    controller.openProposal(workspace().toString())
    val screen = controller.exportKotlin("plates")
    assertTrue("fun PlatesScreen" in screen)
    assertFalse("fun PlateDetailScreen" in screen)
    assertTrue("NavDisplay" in controller.exportKotlin())
    assertFailsWith<PreviewException> { controller.exportKotlin("nope") }
    Unit
  }

  @Test
  fun `export_kotlin writes Wear Compose for a Wear proposal`() = runBlocking {
    controller.openProposal(fixtures.resolve("wear.proposal.json").toString())
    val screen = controller.exportKotlin("home")
    assertTrue("fun HomeScreen" in screen)
    assertTrue("androidx.wear.compose.material3.ScreenScaffold" in screen)
    assertTrue("fun WearAppTheme" in screen)
    assertTrue("rememberSwipeDismissableSceneStrategy" in controller.exportKotlin())
    Unit
  }

  @Test
  fun `check_proposal renders every screen at every view by default`() = runBlocking {
    controller.openProposal(workspace().toString())

    val check = controller.checkProposal()

    assertEquals(listOf("plates", "plates+new_sheet", "plate_detail"), check.screens.keys.toList())
    for ((screen, views) in check.screens) {
      assertEquals(Views.all.map { it.name }, views.keys.toList(), screen)
    }
    assertTrue(check.pass, check.toString())
    assertTrue(check.ms > 0)
    val json = PreviewJson.compact.encodeToString(ProposalCheck.serializer(), check)
    assertTrue(json.length < 2_500, "a passing check stays small: ${json.length} characters")
  }

  @Test
  fun `check_proposal writes a view that repeats an earlier one as same, and still fails on it`() =
    runBlocking {
      controller.openProposal(workspace().toString())

      val check = controller.checkProposal(screens = listOf("plates"))

      val views = check.screens.getValue("plates")
      assertNull(views.getValue("light").same)
      assertEquals("light", views.getValue("dark").same, "dark looks like light")
      assertNull(views.getValue("tablet").same, "a wider view shows more and stays whole")
      val failing = workspace()
      Files.writeString(
        failing,
        Files.readString(failing)
          .replace(
            "\"label\": \"Cook\"",
            "\"label\": \"A label that is far too long to fit on one line of a phone\"",
          ),
      )
      controller.openProposal(failing.toString())
      val broken =
        controller.checkProposal(screens = listOf("plate_detail"), views = listOf("light", "dark"))
      assertFalse(broken.pass, "the overall verdict comes from the views, not the collapsed map")
      assertEquals("light", broken.screens.getValue("plate_detail").getValue("dark").same)
    }

  @Test
  fun `check_proposal lists the colors of the roles the renders draw with, once per brightness`() =
    runBlocking {
      controller.openProposal(workspace().toString())

      val both = controller.checkProposal(screens = listOf("plate_detail"))
      assertEquals(listOf("light", "dark"), both.colors.keys.toList())
      for (colors in both.colors.values) {
        assertEquals(colors.keys.sorted(), colors.keys.toList(), "sorted by role name")
        assertTrue(colors.values.all { Regex("#[0-9A-F]{6}").matches(it) }, colors.toString())
      }
      assertNotEquals(both.colors.getValue("light"), both.colors.getValue("dark"))

      val light =
        controller.checkProposal(screens = listOf("plate_detail"), views = listOf("light"))
      assertEquals(listOf("light"), light.colors.keys.toList())
      // The light brightness also covers the foldable, tablet and desktop views, which draw more.
      assertTrue(
        both.colors.getValue("light").entries.containsAll(light.colors.getValue("light").entries),
      )

      val tree = controller.getTree("plate_detail", "light")
      val drawn =
        tree.nodes.flatMap { listOfNotNull(it.roles?.container, it.roles?.content) }.toSortedSet()
      assertEquals(drawn.toList(), light.colors.getValue("light").keys.toList())
    }

  @Test
  fun `check_proposal lists a Wear proposal's colors for dark alone`() = runBlocking {
    controller.openProposal(
      Path.of(System.getProperty("repo.dir"))
        .resolve("evals/verify/wear.verify.proposal.json")
        .toString(),
    )

    val check = controller.checkProposal(screens = listOf("home"))

    assertEquals(listOf("dark"), check.colors.keys.toList())
    assertTrue(check.colors.getValue("dark").isNotEmpty())
  }

  @Test
  fun `check_proposal renders only the screens and views asked for`() = runBlocking {
    controller.openProposal(workspace().toString())

    val check =
      controller.checkProposal(screens = listOf("plate_detail"), views = listOf("dark", "tablet"))

    assertEquals(listOf("plate_detail"), check.screens.keys.toList())
    assertEquals(listOf("dark", "tablet"), check.screens.getValue("plate_detail").keys.toList())
  }

  @Test
  fun `check_proposal names the rule and the node of a failure and keeps info out of the errors`() =
    runBlocking {
      val file = workspace()
      Files.writeString(
        file,
        Files.readString(file)
          .replace(
            "\"label\": \"Cook\"",
            "\"label\": \"A label that is far too long to fit on one line of a phone\"",
          ),
      )
      controller.openProposal(file.toString())

      val check =
        controller.checkProposal(screens = listOf("plate_detail"), views = listOf("light"))

      assertFalse(check.pass)
      val light = check.screens.getValue("plate_detail").getValue("light")
      assertFalse(light.pass)
      val error = light.errors.first { it.rule == "label-truncated" }
      assertEquals(listOf("cook"), error.ids)
      assertTrue(error.detail.orEmpty().isNotBlank())
      assertTrue(light.errors.all { it.rule != "text-truncated" }, "info findings are not errors")
    }

  @Test
  fun `check_proposal stays compact on a proposal full of failures`() = runBlocking {
    val verify =
      Path.of(System.getProperty("repo.dir")).resolve("evals/verify/verify.proposal.json")
    controller.openProposal(verify.toString())

    val check = controller.checkProposal(screens = listOf("fabs"))

    assertFalse(check.pass)
    val light = check.screens.getValue("fabs").getValue("light")
    val oneFab = light.errors.single { it.rule == "one-fab" }
    assertTrue(oneFab.ids.size > 10, "one entry names every offender: ${oneFab.ids}")
    val json = PreviewJson.compact.encodeToString(ProposalCheck.serializer(), check)
    assertTrue(json.length < 4_500, "five views of 12 failures: ${json.length} characters")
  }

  /** The verdict on [view] of [screen], following `same` to the view it repeats. */
  private fun ProposalCheck.view(screen: String, view: String): ViewCheck {
    val views = screens.getValue(screen)
    val entry = views.getValue(view)
    return entry.same?.let { views.getValue(it) } ?: entry
  }

  private val verifyProposal: Path
    get() = Path.of(System.getProperty("repo.dir")).resolve("evals/verify/verify.proposal.json")

  @Test
  fun `check_proposal says what each view shows, so adaptation can be compared`() = runBlocking {
    controller.openProposal(verifyProposal.toString())

    val check = controller.checkProposal()

    fun layout(screen: String, view: String) =
      assertNotNull(check.view(screen, view).layout, "$screen/$view")
    assertEquals("NavigationBar", layout("home", "light").nav)
    assertEquals("NavigationBar", layout("home", "dark").nav)
    assertEquals("NavigationRail", layout("home", "tablet").nav)
    assertEquals("NavigationRail", layout("home", "desktop").nav)
    assertEquals("none", layout("fabs", "light").nav)
    // The navigation is drawn once, outside the panes: it shows beside a destination's detail pane
    // where two panes fit, and a compact detail that covers the window shows none.
    assertEquals("NavigationRail", layout("detail", "tablet").nav)
    assertEquals("none", layout("detail", "light").nav)
    assertEquals("none", layout("detail", "foldable").nav)
    // The detail opens over the list up to medium (the foldable) and beside it from expanded up.
    assertEquals(listOf("detail"), layout("detail", "light").panes.keys.toList())
    assertEquals(listOf("detail"), layout("detail", "foldable").panes.keys.toList())
    assertEquals(listOf("home", "detail"), layout("detail", "tablet").panes.keys.toList())
    val region = Regex("""[A-Z]\w+ \S+ \d+|\+\d+ more""")
    // An overlay render is the screen's with the overlay open, and has no summary of its own.
    for ((screen, views) in check.screens.filterKeys { '+' !in it }) {
      for (view in views.keys) {
        val summary = assertNotNull(check.view(screen, view).layout, "$screen/$view")
        val json = PreviewJson.compact.encodeToString(ViewLayout.serializer(), summary)
        assertTrue(json.length <= 300, "$screen/$view is ${json.length} bytes: $json")
        assertTrue(summary.panes.values.flatten().all(region::matches), json)
      }
    }
  }

  @Test
  fun `check_proposal adds a render per overlay, named screen+overlay, at every view`() =
    runBlocking {
      controller.openProposal(verifyProposal.toString())

      val check =
        controller.checkProposal(screens = listOf("overlays"), views = listOf("light", "tablet"))

      assertEquals("overlays", check.screens.keys.first())
      for (overlay in listOf("sheet", "dialog", "menu", "time_dial", "dialog_full")) {
        val views = assertNotNull(check.screens["overlays+$overlay"], "overlays+$overlay")
        assertEquals(listOf("light", "tablet"), views.keys.toList())
        assertNull(
          views.getValue("light").layout,
          "an overlay render leaves out the layout summary",
        )
      }
      assertNotNull(check.screens.getValue("overlays").getValue("light").layout)
      val baseRules = check.screens.getValue("overlays").getValue("light").errors.map { it.rule }
      assertTrue(baseRules.isNotEmpty(), "the base screen has errors for the overlays to repeat")
      for (overlay in listOf("sheet", "dialog", "menu")) {
        val light = check.screens.getValue("overlays+$overlay").getValue("light")
        assertTrue(
          light.pass,
          "overlays+$overlay repeats the base screen's errors: ${light.errors}",
        )
      }
      val json = PreviewJson.compact.encodeToString(ProposalCheck.serializer(), check)
      assertTrue(json.length < 4_000, "twelve overlays by two views: ${json.length} characters")
    }

  @Test
  fun `check_proposal covers a whole Wear list and the Wear dialogs`() = runBlocking {
    controller.openProposal(
      Path.of(System.getProperty("repo.dir"))
        .resolve("evals/verify/wear.verify.proposal.json")
        .toString(),
    )

    val check = controller.checkProposal(screens = listOf("home"), views = listOf("watch"))

    assertEquals(listOf("home", "home+sure", "home+done"), check.screens.keys.toList())
  }

  @Test
  fun `check_proposal reports a bad proposal, screen or view like the other tools`() {
    for (call in listOf<suspend () -> Any>({ controller.checkProposal() })) {
      val error = assertFailsWith<PreviewException> { runBlocking { call() } }
      assertTrue("open_proposal" in error.message.orEmpty())
    }
    runBlocking {
      val file = workspace()
      controller.openProposal(file.toString())
      val screen =
        assertFailsWith<PreviewException> { controller.checkProposal(screens = listOf("nope")) }
      assertTrue("plates" in screen.message.orEmpty())
      val view =
        assertFailsWith<PreviewException> { controller.checkProposal(views = listOf("huge")) }
      assertTrue("tablet" in view.message.orEmpty())
      Files.writeString(file, Files.readString(file).replace("\"Scaffold\"", "\"Scaffoldd\""))
      val invalid = assertFailsWith<PreviewException> { controller.checkProposal() }
      assertTrue("Scaffoldd" in invalid.message.orEmpty())
    }
  }

  @Test
  fun `get_catalog is a compact summary, or the full entries of the named components`() =
    runBlocking {
      val summary = controller.getCatalog()
      assertTrue(summary.length < 20_000, "the summary is ${summary.length} characters")
      assertTrue("Button: " in summary && "variant: filled|tonal" in summary)

      val entries =
        Json.parseToJsonElement(controller.getCatalog(listOf("Slider", "Button"))).jsonObject
      assertEquals(listOf("Slider", "Button"), entries.keys.toList())
      assertEquals(
        "Slider",
        entries
          .getValue("Slider")
          .jsonObject
          .getValue("properties")
          .jsonObject
          .getValue("component")
          .jsonObject
          .getValue("const")
          .jsonPrimitive
          .content,
      )
      assertFailsWith<PreviewException> { controller.getCatalog(listOf("Nope")) }
      Unit
    }

  @Test
  fun `a bad seed is refused`() = runBlocking {
    assertFailsWith<PreviewException> { controller.setView(ViewChange(seed = "blue")) }
    Unit
  }

  private fun ViewerViewModel.host() = assertNotNull(state.value.host)
}
