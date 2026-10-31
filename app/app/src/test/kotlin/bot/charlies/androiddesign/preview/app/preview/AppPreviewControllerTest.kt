package bot.charlies.androiddesign.preview.app.preview

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.data.FileProposalRepository
import bot.charlies.androiddesign.preview.domain.PreviewException
import bot.charlies.androiddesign.preview.domain.ViewChange
import bot.charlies.androiddesign.preview.feature.preview.viewer.ViewerViewModel
import bot.charlies.androiddesign.preview.model.Device
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.PaletteVariant
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

class AppPreviewControllerTest {
  private val fixtures = Path.of(System.getProperty("fixtures.dir"))
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private val viewer = ViewerViewModel()
  private val window = WindowVisibility()
  private val controller =
    AppPreviewController(
      repository = FileProposalRepository(),
      viewer = viewer,
      window = window,
      renderer = HeadlessTreeRenderer(),
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
    assertTrue("\"protocolVersion\"" in controller.getCatalog())
    controller.openProposal(workspace().toString())
    val screen = controller.exportKotlin("plates")
    assertTrue("fun PlatesScreen" in screen)
    assertFalse("fun PlateDetailScreen" in screen)
    assertTrue("NavDisplay" in controller.exportKotlin())
    assertFailsWith<PreviewException> { controller.exportKotlin("nope") }
    Unit
  }

  @Test
  fun `a bad seed is refused`() = runBlocking {
    assertFailsWith<PreviewException> { controller.setView(ViewChange(seed = "blue")) }
    Unit
  }

  private fun ViewerViewModel.host() = assertNotNull(state.value.host)
}
