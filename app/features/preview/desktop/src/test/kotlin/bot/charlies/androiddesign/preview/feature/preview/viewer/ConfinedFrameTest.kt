package bot.charlies.androiddesign.preview.feature.preview.viewer

import androidx.compose.ui.awt.ComposePanel
import bot.charlies.androiddesign.preview.feature.preview.designOf
import bot.charlies.androiddesign.preview.feature.preview.render.ProposalHostState
import bot.charlies.androiddesign.preview.feature.preview.singleScreen
import bot.charlies.androiddesign.preview.model.Device
import java.awt.Component
import java.awt.Container
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import javax.accessibility.Accessible
import javax.accessibility.AccessibleContext
import javax.swing.JFrame
import javax.swing.SwingUtilities
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assumptions.assumeFalse

/**
 * Measures that overlays opened inside the preview stay inside the device frame and out of the
 * viewer's own panel. It needs a real AWT window (the overlays are popups of a real scene), so it
 * is skipped when there is no display.
 */
class ConfinedFrameTest {
  private class Seen(val name: String, val screen: Rectangle)

  private val proposal =
    singleScreen(
      """{"id":"root","component":"Scaffold","content":"stage","floatingActionButton":"fab"},""" +
        """{"id":"stage","component":"Text","text":"Page"},""" +
        """{"id":"fab","component":"FloatingActionButton","icon":"add","contentDescription":"New"},""" +
        """{"id":"sheet","component":"BottomSheet","content":["sheet_text"]},""" +
        """{"id":"sheet_text","component":"Text","text":"In the sheet"},""" +
        """{"id":"dialog","component":"Dialog","title":"Delete plate","text":"This cannot be undone","confirm":"yes"},""" +
        """{"id":"yes","component":"Button","label":"Delete"}""",
    )

  private fun walk(context: AccessibleContext?, out: MutableList<Seen>) {
    if (context == null) return
    val component = context.accessibleComponent
    val name = context.accessibleName
    if (component != null && component.isShowing && !name.isNullOrBlank()) {
      out += Seen(name, Rectangle(component.locationOnScreen, component.size))
    }
    for (i in 0 until context.accessibleChildrenCount) {
      walk((context.getAccessibleChild(i) as? Accessible)?.accessibleContext, out)
    }
  }

  private fun panels(root: Component): List<ComposePanel> = buildList {
    if (root is ComposePanel) add(root)
    if (root is Container) root.components.forEach { addAll(panels(it)) }
  }

  private fun <T> onEdt(block: () -> T): T {
    var result: Result<T>? = null
    SwingUtilities.invokeAndWait { result = runCatching(block) }
    return result!!.getOrThrow()
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
  fun `overlays opened in the frame stay inside the frame and out of the viewer`() {
    assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display")
    val host = ProposalHostState(proposal)
    val state =
      ViewerState(
        name = "Overlays",
        screens = listOf("home"),
        design = designOf(),
        host = host,
        device = Device.Phone,
      )
    lateinit var window: JFrame
    lateinit var outer: ComposePanel
    SwingUtilities.invokeAndWait {
      outer = ComposePanel()
      outer.setContent {
        ViewerContent(state, NoActions, { h, d, v, f, s -> ConfinedFrame(h, d, v, f, s) })
      }
      window = JFrame("confinement")
      window.contentPane.add(outer)
      window.setSize(1100, 1000)
      window.isVisible = true
    }
    try {
      fun seen(): List<Seen> = onEdt { buildList { walk(window.accessibleContext, this) } }
      fun inner(): ComposePanel? = onEdt { panels(window).firstOrNull { it !== outer } }

      assertTrue(
        await { seen().any { it.name == "Brightness" } && inner() != null },
        "viewer drawn",
      )
      val frame = assertNotNull(inner())
      val frameBounds = onEdt { Rectangle(frame.locationOnScreen, frame.size) }
      val sidePanel = seen().first { it.name == "Brightness" }.screen
      assertTrue(!frameBounds.intersects(sidePanel), "the side panel is not under the frame")

      fun inFrame(r: Rectangle) = frameBounds.grow(2).contains(r)

      for ((overlay, expected) in
        listOf(
          "sheet" to listOf("In the sheet", "Drag handle"),
          "dialog" to listOf("Delete plate", "Delete"),
        )) {
        host.interaction.open("home", overlay)
        assertTrue(await { seen().any { it.name == expected.first() } }, "$overlay is drawn")
        Thread.sleep(1500) // the entrance animation
        val all = seen()
        for (name in expected) {
          val node = all.first { it.name == name }
          assertTrue(
            inFrame(node.screen),
            "$overlay: '$name' ${node.screen} is outside the frame $frameBounds",
          )
        }
        // The scrim covers the frame and nothing beyond it.
        val scrims = all.filter {
          it.screen.width >= frameBounds.width - 2 && it.name != "confinement"
        }
        scrims.forEach {
          assertTrue(
            inFrame(it.screen),
            "$overlay: ${it.name} ${it.screen} spills out of $frameBounds",
          )
        }
        assertEquals(sidePanel, seen().first { it.name == "Brightness" }.screen)
        host.interaction.dismiss("home")
        assertTrue(await { seen().none { it.name == expected.first() } }, "$overlay closes")
      }
    } finally {
      SwingUtilities.invokeAndWait { window.dispose() }
    }
  }

  private fun Rectangle.grow(by: Int) = Rectangle(x - by, y - by, width + 2 * by, height + 2 * by)
}
