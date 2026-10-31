package bot.charlies.androiddesign.preview.feature.preview.viewer

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.awt.ComposePanel
import bot.charlies.androiddesign.preview.data.FileProposalRepository
import java.awt.GraphicsEnvironment
import java.nio.file.Path
import javax.accessibility.AccessibleContext
import javax.swing.JFrame
import javax.swing.SwingUtilities
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assumptions.assumeFalse

/**
 * A throw inside the preview frame must not leave the window dead: the viewer reports it and, once
 * the proposal is fixed, draws the frame again. It needs a real AWT window, so it is skipped when
 * there is no display.
 */
class FrameFailureTest {
  private val plates = Path.of(System.getProperty("fixtures.dir")).resolve("plates.proposal.json")

  private fun names(context: AccessibleContext?, out: MutableList<String>) {
    if (context == null) return
    context.accessibleName?.takeIf { it.isNotBlank() }?.let(out::add)
    for (i in 0 until context.accessibleChildrenCount) {
      names(context.getAccessibleChild(i)?.accessibleContext, out)
    }
  }

  private fun await(timeoutMs: Long = 8000, condition: () -> Boolean): Boolean {
    val end = System.currentTimeMillis() + timeoutMs
    while (System.currentTimeMillis() < end) {
      if (condition()) return true
      Thread.sleep(50)
    }
    return condition()
  }

  @Test
  fun `a throw in the frame is reported and the frame is rebuilt once the proposal is fixed`() {
    assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display")
    val viewModel = ViewerViewModel()
    val snapshot = runBlocking { FileProposalRepository().load(plates) }
    viewModel.show(snapshot)
    val explode = mutableStateOf(false)
    lateinit var window: JFrame
    SwingUtilities.invokeAndWait {
      val outer = ComposePanel()
      outer.setContent {
        ViewerScreen(
          viewModel,
          frame = { host, design, view, scale ->
            ConfinedFrame(
              host,
              design,
              view,
              scale,
              onFailure = viewModel::frameFailed,
              content = { _, _, _ ->
                check(!explode.value) { "boom in the frame" }
                Text("frame is alive")
              },
            )
          },
        )
      }
      window = JFrame("frame failure")
      window.contentPane.add(outer)
      window.setSize(1100, 1000)
      window.isVisible = true
    }
    try {
      fun seen(): List<String> {
        var result = emptyList<String>()
        SwingUtilities.invokeAndWait {
          result = buildList { names(window.accessibleContext, this) }
        }
        return result
      }

      assertTrue(await { "frame is alive" in seen() }, "the frame is drawn")

      explode.value = true
      assertTrue(await { !viewModel.state.value.valid }, "the failure reaches the viewer state")
      val error = viewModel.state.value.errors.single()
      assertEquals("render", error.path)
      assertTrue("boom in the frame" in error.message, error.message)
      assertTrue(await { "frame is alive" !in seen() }, "the dead frame is taken down")

      explode.value = false
      viewModel.show(snapshot)
      assertTrue(await { "frame is alive" in seen() }, "the frame is drawn again after the fix")
      assertFalse(viewModel.state.value.errors.isNotEmpty())
    } finally {
      SwingUtilities.invokeAndWait { window.dispose() }
    }
  }
}
