package bot.charlies.androiddesign.preview.feature.preview

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessRenderer
import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.Views
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Search and tooltips draw where and in what the tree says, and answer taps like the real ones. */
class SearchAndTooltipRenderTest {
  private val scale = 2 // the png is two pixels per dp

  private fun image(png: ByteArray): BufferedImage = ImageIO.read(ByteArrayInputStream(png))

  private fun rgb(hex: String) = hex.removePrefix("#").toInt(16)

  private fun distance(a: Int, b: Int) =
    listOf(16, 8, 0).sumOf { abs(((a shr it) and 0xFF) - ((b shr it) and 0xFF)) }

  private fun pixel(png: ByteArray, xDp: Float, yDp: Float) =
    image(png).getRGB((xDp * scale).toInt(), (yDp * scale).toInt()) and 0xFFFFFF

  /** The pixel in [region] (dp) farthest from [backdrop]: the fully covered color of its text. */
  private fun farthest(png: ByteArray, region: Bounds, backdrop: Int): Int {
    val image = image(png)
    var best = backdrop
    for (x in (region.x * scale).toInt() until ((region.x + region.width) * scale).toInt()) {
      for (y in (region.y * scale).toInt() until ((region.y + region.height) * scale).toInt()) {
        if (x >= image.width || y >= image.height) continue
        val pixel = image.getRGB(x, y) and 0xFFFFFF
        if (distance(pixel, backdrop) > distance(best, backdrop)) best = pixel
      }
    }
    return best
  }

  private fun assertSame(expected: Int, drawn: Int, label: String) =
    assertTrue(
      distance(expected, drawn) <= 24,
      "$label: tree says #%06X, drawn #%06X".format(expected, drawn),
    )

  private fun ComposeUiTest.settleForTest() {
    with(HeadlessRenderer) { settle() }
  }

  private fun search(layout: String) =
    """{"id":"root","component":"Scaffold","content":"stage"},""" +
      """{"id":"stage","component":"Column","padding":"lg","spacing":"md","children":["field","note"]},""" +
      """{"id":"field","component":"SearchBar","placeholder":"Search mail","trailing":["mic"],"expanded":"view"},""" +
      """{"id":"mic","component":"IconButton","icon":"mic","contentDescription":"Voice search"},""" +
      """{"id":"note","component":"Text","text":"Behind the view"},""" +
      """{"id":"view","component":"SearchView","layout":"$layout","content":["first","second"]},""" +
      """{"id":"first","component":"Text","text":"Recent: invoices"},""" +
      """{"id":"second","component":"Text","text":"Recent: tickets"}"""

  @Test
  fun `a search view draws only while open, full-screen over the window and docked as a dropdown`() {
    assertFalse(render(singleScreen(search("full-screen"))).tree.has("view"))
    for (view in listOf(Views.Light, Views.Tablet)) {
      val full =
        render(singleScreen(search("full-screen")), view = view) {
            it.interaction.open("home", "view")
          }
          .tree
      assertEquals(view.width.toFloat(), full.node("view").bounds.width, 1f, view.name)
      assertEquals(view.height.toFloat(), full.node("view").bounds.height, 1f, view.name)
      assertTrue(full.has("first"), "the results are in the tree")
      val docked =
        render(singleScreen(search("docked")), view = view) { it.interaction.open("home", "view") }
          .tree
      val bounds = docked.node("view").bounds
      assertTrue(bounds.width < view.width, "${view.name}: a dropdown, not the window")
      assertTrue(bounds.height >= 240f, "${view.name}: at least 240dp")
      assertTrue(bounds.y >= docked.node("field").bounds.y, "${view.name}: below the bar")
    }
  }

  @Test
  fun `a tap on the bar opens its view, a tap on a result keeps it open and back closes it`() {
    var afterTap: String? = null
    var afterResult: String? = "unset"
    var afterBack: String? = "unset"
    render(singleScreen(search("full-screen"))) { host ->
      settleForTest()
      onNodeWithTag("field", useUnmergedTree = true).performClick()
      settleForTest()
      afterTap = host.interaction.openOverlay("home")
      onNodeWithTag("first", useUnmergedTree = true).performClick()
      settleForTest()
      afterResult = host.interaction.openOverlay("home")
      onAllNodesWithContentDescription("Back", useUnmergedTree = true).onLast().performClick()
      settleForTest()
      afterBack = host.interaction.openOverlay("home")
    }
    assertEquals("view", afterTap, "tap on the bar")
    assertEquals("view", afterResult, "tap inside the view")
    assertEquals(null, afterBack, "back")
  }

  @Test
  fun `the bar and the view's described colors are the ones drawn`() {
    val closed = render(singleScreen(search("full-screen")))
    val bar = closed.tree.node("field")
    val container = rgb(checkNotNull(bar.colors?.container))
    assertSame(container, pixel(closed.png, bar.bounds.x + 90, bar.bounds.y + 4), "bar container")
    val hint = Bounds(bar.bounds.x + 56, bar.bounds.y + 16, 100f, bar.bounds.height - 32)
    assertSame(
      rgb(checkNotNull(bar.colors?.content)),
      farthest(closed.png, hint, container),
      "bar hint",
    )
    for (layout in listOf("full-screen", "docked")) {
      val open =
        render(singleScreen(search(layout)), view = Views.Tablet) {
          it.interaction.open("home", "view")
        }
      val view = open.tree.node("view")
      // Full-screen: beside the first result. Docked: in the input field's pill, above its hint,
      // since the dropdown only wraps the results.
      val x =
        if (layout == "docked") view.bounds.x + 200 else view.bounds.x + view.bounds.width - 16
      val y =
        if (layout == "docked") view.bounds.y + 6
        else open.tree.node("first").bounds.let { it.y + it.height / 2 }
      assertSame(rgb(checkNotNull(view.colors?.container)), pixel(open.png, x, y), layout)
    }
  }

  private fun tooltip(extra: String, anchorIn: String = "stage") =
    """{"id":"root","component":"Scaffold","topBar":"top","content":"stage"},""" +
      """{"id":"top","component":"TopAppBar","title":"Page","actions":["save"]},""" +
      """{"id":"save","component":"IconButton","icon":"save","contentDescription":"Save"},""" +
      """{"id":"stage","component":"Column","padding":"lg","spacing":"md","children":["info","edit"]},""" +
      """{"id":"info","component":"IconButton","icon":"info","contentDescription":"Info"},""" +
      """{"id":"edit","component":"IconButton","icon":"edit","contentDescription":"Edit"},""" +
      extra

  @Test
  fun `a tooltip draws only while open, on its anchor's side, and closes on dismiss`() {
    val tips =
      tooltip(
        """{"id":"plain","component":"Tooltip","anchor":"info","text":"More about this"},""" +
          """{"id":"in_bar","component":"Tooltip","anchor":"save","text":"Save"},""" +
          """{"id":"rich","component":"Tooltip","anchor":"edit","variant":"rich","title":"Edit","text":"Change it.","actions":["ok","later"]},""" +
          """{"id":"ok","component":"Button","variant":"text","label":"Got it","action":{"functionCall":{"@call":"dismiss"}}},""" +
          """{"id":"later","component":"Button","variant":"text","label":"Later"}""",
      )
    assertFalse(render(singleScreen(tips)).tree.has("plain"))
    fun at(id: String) =
      render(singleScreen(tips)) { it.interaction.open("home", id) }
        .tree
        .let { tree ->
          tree.node(id).bounds to tree
        }
    val (plain, plainTree) = at("plain")
    val info = plainTree.node("info").bounds
    assertTrue(plain.y + plain.height <= info.y + 1f, "plain: above its anchor")
    assertTrue(plain.y > 0f, "plain: on screen")
    val (inBar, barTree) = at("in_bar")
    val save = barTree.node("save").bounds
    assertTrue(inBar.y >= save.y + save.height - 1f, "plain in an app bar: below its anchor")
    val (rich, richTree) = at("rich")
    val edit = richTree.node("edit").bounds
    assertTrue(rich.y >= edit.y + edit.height - 1f, "rich: below its anchor")
    assertTrue(richTree.has("ok") && richTree.has("later"), "the buttons are in the tree")
    var after: String? = "unset"
    render(singleScreen(tips)) { host ->
      host.interaction.open("home", "rich")
      settleForTest()
      onNodeWithTag("ok", useUnmergedTree = true).performClick()
      settleForTest()
      after = host.interaction.openOverlay("home")
    }
    assertEquals(null, after, "the button's dismiss closes the tooltip")
  }

  @Test
  fun `a tap inside an open rich tooltip, on its text or an inert button, keeps it open`() {
    val tips =
      tooltip(
        """{"id":"rich","component":"Tooltip","anchor":"edit","variant":"rich","title":"Edit","text":"Change it.","actions":["ok","later"]},""" +
          """{"id":"ok","component":"Button","variant":"text","label":"Got it","action":{"functionCall":{"@call":"dismiss"}}},""" +
          """{"id":"later","component":"Button","variant":"text","label":"Later"}""",
      )
    var afterBody: String? = "unset"
    var afterButton: String? = "unset"
    render(singleScreen(tips)) { host ->
      host.interaction.open("home", "rich")
      settleForTest()
      onNodeWithTag("rich", useUnmergedTree = true).performClick()
      settleForTest()
      afterBody = host.interaction.openOverlay("home")
      onNodeWithTag("later", useUnmergedTree = true).performClick()
      settleForTest()
      afterButton = host.interaction.openOverlay("home")
    }
    assertEquals("rich", afterBody, "tap on the tooltip's body")
    assertEquals("rich", afterButton, "tap on a button with no action")
  }

  @Test
  fun `a tooltip's described colors are the ones drawn`() {
    val tips =
      tooltip(
        """{"id":"plain","component":"Tooltip","anchor":"info","text":"More about this"},""" +
          """{"id":"rich","component":"Tooltip","anchor":"edit","variant":"rich","text":"Change it."}""",
      )
    for (id in listOf("plain", "rich")) {
      val result = render(singleScreen(tips)) { it.interaction.open("home", id) }
      val node = result.tree.node(id)
      val container = rgb(checkNotNull(node.colors?.container))
      assertSame(
        container,
        pixel(result.png, node.bounds.x + node.bounds.width - 3, node.bounds.y + 3),
        "$id container",
      )
      val text =
        Bounds(node.bounds.x + 4, node.bounds.y + 4, node.bounds.width - 8, node.bounds.height - 8)
      assertSame(
        rgb(checkNotNull(node.colors?.content)),
        farthest(result.png, text, container),
        "$id text",
      )
    }
  }
}
