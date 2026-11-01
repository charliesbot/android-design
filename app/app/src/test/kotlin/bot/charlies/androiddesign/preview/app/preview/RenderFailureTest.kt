package bot.charlies.androiddesign.preview.app.preview

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.data.FileProposalRepository
import bot.charlies.androiddesign.preview.domain.PreviewException
import bot.charlies.androiddesign.preview.domain.ViewChange
import bot.charlies.androiddesign.preview.feature.preview.render.ProposalHostState
import bot.charlies.androiddesign.preview.feature.preview.viewer.ViewerViewModel
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.Device
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.ViewSpec
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/** A proposal that throws while composing must not kill the app or break the window. */
class RenderFailureTest {
  /**
   * Renders for real, except that a proposal named [BROKEN] throws, as a composition crash does.
   */
  private class CrashingRenderer : TreeRenderer {
    private val real = HeadlessTreeRenderer()

    override suspend fun render(
      proposal: Proposal,
      design: DesignSystem,
      proposalDir: Path?,
      screen: String,
      view: ViewSpec,
      live: ProposalHostState?,
    ): LayoutTree {
      check(proposal.name != BROKEN) { "cannot measure $screen" }
      if (proposal.name == BLANK) throw IllegalStateException("")
      val tree = real.render(proposal, design, proposalDir, screen, view, live)
      // A bad value that only some states or sizes draw, as a slider with a reversed range does.
      check(tree.nodes.none { it.text?.value == BAD_TEXT }) {
        "cannot draw '$BAD_TEXT' in $screen at ${view.name}"
      }
      return tree
    }
  }

  private val fixtures = Path.of(System.getProperty("fixtures.dir"))
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private val viewer = ViewerViewModel()
  private val controller =
    AppPreviewController(
      repository = FileProposalRepository(),
      viewer = viewer,
      window = WindowVisibility(),
      renderer = CrashingRenderer(),
      scope = scope,
      version = AppInfo.VERSION,
    )

  @AfterTest
  fun stop() {
    scope.cancel()
  }

  private fun workspace(): Path {
    val dir = Files.createTempDirectory("crash")
    Files.copy(fixtures.resolve("DESIGN.md"), dir.resolve("DESIGN.md"))
    return dir.resolve("plates.proposal.json").also {
      Files.copy(fixtures.resolve("plates.proposal.json"), it)
    }
  }

  private fun breakIt(file: Path) =
    Files.writeString(
      file,
      Files.readString(file).replace("\"name\": \"Tonight's plate\"", "\"name\": \"$BROKEN\""),
    )

  private suspend fun until(condition: () -> Boolean) {
    withTimeout(10_000) {
      while (!condition()) delay(20)
    }
  }

  @Test
  fun `a proposal that throws keeps the last good window and reports the error`() = runBlocking {
    val file = workspace()
    controller.openProposal(file.toString())
    val good = viewer.state.value.host

    breakIt(file)
    until { !viewer.state.value.valid }

    assertTrue(viewer.state.value.host === good, "the window keeps the last good proposal")
    assertEquals("Tonight's plate", viewer.state.value.name)
    val error = viewer.state.value.errors.single()
    assertEquals("render", error.path)
    assertTrue("cannot measure" in error.message, error.message)

    val status = controller.status()
    assertEquals(listOf(error), status.errors)
    val failure = assertFailsWith<PreviewException> { controller.getTree() }
    assertTrue("cannot measure" in failure.message.orEmpty(), failure.message)

    Files.copy(fixtures.resolve("plates.proposal.json"), file, StandardCopyOption.REPLACE_EXISTING)
    until { viewer.state.value.valid }
    assertEquals(emptyList(), controller.status().errors)
  }

  @Test
  fun `opening a proposal that throws reports the error and shows nothing`() = runBlocking {
    val file = workspace()
    breakIt(file)

    val opened = controller.openProposal(file.toString())

    assertFalse(opened.valid)
    assertTrue(opened.errors.single().message.contains("cannot measure"), opened.errors.toString())
    assertFalse(viewer.state.value.hasProposal, "nothing was adopted into the window")
  }

  @Test
  fun `a failure with a blank message is reported by its class`() = runBlocking {
    val file = workspace()
    Files.writeString(
      file,
      Files.readString(file).replace("\"name\": \"Tonight's plate\"", "\"name\": \"$BLANK\""),
    )

    val opened = controller.openProposal(file.toString())

    assertFalse(opened.valid)
    assertTrue("IllegalStateException" in opened.errors.single().message, opened.errors.toString())
  }

  @Test
  fun `a bad value inside an overlay the window has open is caught before it is shown`() =
    runBlocking {
      val file = workspace()
      controller.openProposal(file.toString())
      val good = viewer.state.value.host
      viewer.state.value.host!!.interaction.open("plates", "new_sheet")

      Files.writeString(file, Files.readString(file).replace("Add a plate", BAD_TEXT))
      until { !viewer.state.value.valid }

      assertTrue(viewer.state.value.host === good, "the window keeps the last good proposal")
      val error = viewer.state.value.errors.single()
      assertEquals("render", error.path)
      assertTrue(BAD_TEXT in error.message, error.message)
      assertEquals(listOf(error), controller.status().errors)

      Files.copy(
        fixtures.resolve("plates.proposal.json"),
        file,
        StandardCopyOption.REPLACE_EXISTING,
      )
      until { viewer.state.value.valid }
    }

  @Test
  fun `a bad value that only another size class draws is shown, then reported by the window`() =
    runBlocking {
      val file = workspace()
      controller.openProposal(file.toString())

      Files.writeString(
        file,
        Files.readString(file)
          .replace("\"name\": \"Tonight's plate\"", "\"name\": \"Edited\"")
          .replace(
            "\"title\": \"Tonight's plate\"",
            "\"title\": \"Tonight's plate\", \"sizeClass\": {\"expanded\": {\"title\": \"$BAD_TEXT\"}}",
          ),
      )
      until { viewer.state.value.name == "Edited" }

      // The save is not held up drawing sizes the window is not on.
      assertTrue(viewer.state.value.valid, viewer.state.value.errors.toString())

      // Switching device draws the bad value in the window's frame, which contains the throw.
      controller.setView(ViewChange(device = Device.Desktop))
      viewer.frameFailed(IllegalStateException("cannot draw '$BAD_TEXT' in plates at desktop"))

      val error = controller.status().errors.single()
      assertEquals("render", error.path)
      assertTrue(BAD_TEXT in error.message, error.message)

      Files.copy(
        fixtures.resolve("plates.proposal.json"),
        file,
        StandardCopyOption.REPLACE_EXISTING,
      )
      until { viewer.state.value.valid }
      assertEquals(emptyList(), controller.status().errors)
    }

  @Test
  fun `a frame failure shows in the status until the proposal is fixed`() = runBlocking {
    val file = workspace()
    controller.openProposal(file.toString())

    viewer.frameFailed(IllegalStateException("boom"))

    val error = controller.status().errors.single()
    assertEquals("render", error.path)
    assertTrue("boom" in error.message, error.message)

    Files.writeString(file, Files.readString(file).replace("Tonight's plate", "Dinner"))
    until { viewer.state.value.valid }
    assertEquals(emptyList(), controller.status().errors)
  }

  private companion object {
    const val BROKEN = "Crash test"
    const val BLANK = "Blank failure"
    const val BAD_TEXT = "BOOM"
  }
}
