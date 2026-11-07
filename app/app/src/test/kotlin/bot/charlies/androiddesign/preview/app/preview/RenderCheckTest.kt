package bot.charlies.androiddesign.preview.app.preview

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.data.FileProposalRepository
import bot.charlies.androiddesign.preview.domain.ViewChange
import bot.charlies.androiddesign.preview.feature.preview.headless.RenderSource
import bot.charlies.androiddesign.preview.feature.preview.render.ProposalHostState
import bot.charlies.androiddesign.preview.feature.preview.viewer.ViewerViewModel
import bot.charlies.androiddesign.preview.model.Device
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.ViewSpec
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/**
 * A save is checked by drawing what the window is about to show, once. Drawing every screen at
 * every size class made each save cost seconds on a large proposal.
 */
class RenderCheckTest {
  private class Call(val screen: String, val view: ViewSpec, val live: ProposalHostState?)

  private class RecordingRenderer : TreeRenderer {
    private val real = HeadlessTreeRenderer()
    val calls = CopyOnWriteArrayList<Call>()

    override suspend fun render(
      source: RenderSource,
      screen: String,
      view: ViewSpec,
      live: ProposalHostState?,
      overlay: String?,
    ): LayoutTree {
      calls += Call(screen, view, live)
      return real.render(source, screen, view, live, overlay)
    }
  }

  private val fixtures = Path.of(System.getProperty("fixtures.dir"))
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private val viewer = ViewerViewModel()
  private val renderer = RecordingRenderer()
  private val controller =
    AppPreviewController(
      repository = FileProposalRepository(),
      viewer = viewer,
      window = PreviewWindow(),
      renderer = renderer,
      scope = scope,
      version = AppInfo.VERSION,
    )

  @AfterTest
  fun stop() {
    scope.cancel()
  }

  private fun workspace(): Path {
    val dir = Files.createTempDirectory("render-check")
    Files.copy(fixtures.resolve("DESIGN.md"), dir.resolve("DESIGN.md"))
    return dir.resolve("plates.proposal.json").also {
      Files.copy(fixtures.resolve("plates.proposal.json"), it)
    }
  }

  /** A change to the proposal that the proposal reacts to the width for, and a new name. */
  private fun edit(file: Path, name: String) =
    Files.writeString(
      file,
      Files.readString(file)
        .replace("\"name\": \"Tonight's plate\"", "\"name\": \"$name\"")
        .replace(
          "\"title\": \"Tonight's plate\"",
          "\"title\": \"Tonight's plate\", \"sizeClass\": {\"expanded\": {\"title\": \"Wide\"}}",
        ),
    )

  private suspend fun until(condition: () -> Boolean) {
    withTimeout(10_000) {
      while (!condition()) delay(20)
    }
  }

  @Test
  fun `a save draws the window's screen once, whatever the proposal reacts to`() = runBlocking {
    val file = workspace()
    controller.openProposal(file.toString())
    renderer.calls.clear()

    edit(file, "Dinner")
    until { viewer.state.value.name == "Dinner" }

    val call = renderer.calls.single()
    assertEquals("plates", call.screen)
    assertEquals(viewer.state.value.view, call.view)
  }

  @Test
  fun `a save draws the screen and the view the window is on, with its state`() = runBlocking {
    val file = workspace()
    controller.openProposal(file.toString())
    controller.setView(ViewChange(device = Device.Tablet))
    viewer.selectScreen("plate_detail")
    renderer.calls.clear()

    edit(file, "Dinner")
    until { viewer.state.value.name == "Dinner" }

    val call = renderer.calls.single()
    assertEquals("plate_detail", call.screen)
    assertEquals(Device.Tablet, call.view.device)
    assertSame(viewer.state.value.host, call.live)
  }
}
