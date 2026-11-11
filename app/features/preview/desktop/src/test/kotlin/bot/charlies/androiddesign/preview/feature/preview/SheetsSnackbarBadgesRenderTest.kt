package bot.charlies.androiddesign.preview.feature.preview

import androidx.compose.ui.test.click
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import bot.charlies.androiddesign.preview.domain.SideSheetSpec
import bot.charlies.androiddesign.preview.feature.preview.theme.ThemeResolver
import bot.charlies.androiddesign.preview.feature.preview.theme.toHex
import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.Views
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The modal side sheet, the snackbar and badges draw where and in what the tree says. */
class SheetsSnackbarBadgesRenderTest {
  private val scale = 2 // the png is two pixels per dp

  private fun image(png: ByteArray): BufferedImage = ImageIO.read(ByteArrayInputStream(png))

  private fun rgb(hex: String) = hex.removePrefix("#").toInt(16)

  private fun distance(a: Int, b: Int) =
    listOf(16, 8, 0).sumOf { abs(((a shr it) and 0xFF) - ((b shr it) and 0xFF)) }

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

  private fun pixel(png: ByteArray, xDp: Float, yDp: Float) =
    image(png).getRGB((xDp * scale).toInt(), (yDp * scale).toInt()) and 0xFFFFFF

  private fun assertSame(expected: Int, drawn: Int, label: String) =
    assertTrue(
      distance(expected, drawn) <= 16,
      "$label: tree says #%06X, drawn #%06X".format(expected, drawn),
    )

  private val sheet =
    """{"id":"root","component":"Scaffold","content":"stage"},""" +
      """{"id":"stage","component":"Column","children":["open","note"]},""" +
      """{"id":"open","component":"Button","label":"Filters","action":{"functionCall":{"@call":"open","args":{"overlay":"filters"}}}},""" +
      """{"id":"note","component":"Text","text":"Behind the scrim"},""" +
      """{"id":"filters","component":"SideSheet","title":"Filters","content":["sort","done"]},""" +
      """{"id":"sort","component":"Text","text":"Sort by"},""" +
      """{"id":"done","component":"Button","label":"Done","action":{"functionCall":{"@call":"dismiss"}}}"""

  @Test
  fun `a side sheet sits on the trailing edge, at most 400dp wide, and closes on dismiss`() {
    assertFalse(render(singleScreen(sheet)).tree.has("filters"))
    for (view in listOf(Views.Light, Views.Tablet)) {
      val tree =
        render(singleScreen(sheet), view = view) { it.interaction.open("home", "filters") }.tree
      val node = tree.node("filters")
      val expected = minOf(view.width.toFloat(), SideSheetSpec.MAX_WIDTH_DP)
      assertEquals(expected, node.bounds.width, 1f, view.name)
      assertEquals(view.width.toFloat(), node.bounds.x + node.bounds.width, 1f, view.name)
      assertTrue(tree.has("sort"), "the sheet's content is in the tree")
    }
    var closed: Boolean? = null
    val after =
      render(singleScreen(sheet)) { host ->
          host.interaction.open("home", "filters")
          settleForTest()
          onNodeWithTag("done", useUnmergedTree = true).performClick()
          closed = host.interaction.openOverlay("home") == null
        }
        .tree
    assertEquals(true, closed)
    assertFalse(after.has("filters"))
  }

  @Test
  fun `a tap inside the side sheet does not close it but a tap on the scrim does`() {
    var afterTitle: String? = null
    var afterSort: String? = null
    var afterScrim: String? = "unset"
    render(singleScreen(sheet), view = Views.Tablet) { host ->
      host.interaction.open("home", "filters")
      settleForTest()
      onNodeWithTag("sort", useUnmergedTree = true).performClick()
      afterSort = host.interaction.openOverlay("home")
      onNodeWithTag("filters", useUnmergedTree = true).performTouchInput {
        click(androidx.compose.ui.geometry.Offset(width / 2f, height - 20f))
      }
      afterTitle = host.interaction.openOverlay("home")
      onAllNodes(isRoot(), useUnmergedTree = true)[1].performTouchInput {
        click(androidx.compose.ui.geometry.Offset(20f, 400f))
      }
      afterScrim = host.interaction.openOverlay("home")
    }
    assertEquals("filters", afterSort, "tap on content")
    assertEquals("filters", afterTitle, "tap on empty sheet space")
    assertEquals(null, afterScrim, "tap on the scrim")
  }

  @Test
  fun `a side sheet's described colors are the ones drawn`() {
    val result =
      render(singleScreen(sheet), view = Views.Tablet) {
        it.interaction.open("home", "filters")
      }
    val node = result.tree.node("filters")
    val container = rgb(checkNotNull(node.colors?.container))
    // An empty spot of the sheet, low in its body.
    assertSame(
      container,
      pixel(result.png, node.bounds.x + 100, node.bounds.y + node.bounds.height - 40),
      "container",
    )
    val title =
      Bounds(
        node.bounds.x + SideSheetSpec.PADDING_DP,
        node.bounds.y + SideSheetSpec.PADDING_DP,
        120f,
        48f,
      )
    assertSame(
      rgb(checkNotNull(node.colors?.content)),
      farthest(result.png, title, container),
      "title",
    )
  }

  private val snackbar =
    """{"id":"root","component":"Scaffold","content":"stage","bottomBar":"bar","floatingActionButton":"fab"},""" +
      """{"id":"stage","component":"Text","text":"Page"},""" +
      """{"id":"bar","component":"DockedToolbar","children":["a","b","c"]},""" +
      """{"id":"a","component":"IconButton","icon":"home","contentDescription":"Home"},""" +
      """{"id":"b","component":"IconButton","icon":"favorite","contentDescription":"Saved"},""" +
      """{"id":"c","component":"IconButton","icon":"person","contentDescription":"Profile"},""" +
      """{"id":"fab","component":"FloatingActionButton","icon":"add","contentDescription":"New"},""" +
      """{"id":"saved","component":"Snackbar","message":"Plate saved","actionLabel":"Undo","dismiss":true,"action":{"functionCall":{"@call":"open","args":{"overlay":"other"}}}},""" +
      """{"id":"other","component":"Snackbar","message":"Restored"}"""

  @Test
  fun `a snackbar draws in the scaffold's slot above the fab and the bottom bar`() {
    for (view in listOf(Views.Light, Views.Tablet)) {
      val tree =
        render(singleScreen(snackbar), view = view) { it.interaction.open("home", "saved") }.tree
      val bar = tree.node("bar").bounds
      val fab = tree.node("fab").bounds
      val node = tree.node("saved").bounds
      assertTrue(node.y + node.height <= bar.y + 1f, "${view.name}: above the bar")
      assertTrue(node.y + node.height <= fab.y + 1f, "${view.name}: above the fab")
      assertTrue(node.y > 0f, view.name)
    }
  }

  @Test
  fun `a snackbar's described colors are the ones drawn`() {
    val result =
      render(singleScreen(snackbar.replace("\"actionLabel\":\"Undo\",", ""))) {
        it.interaction.open("home", "saved")
      }
    val node = result.tree.node("saved")
    val container = rgb(checkNotNull(node.colors?.container))
    // The tree's bounds include the 12dp the snackbar keeps clear of the edge, so sample inside it.
    assertSame(
      container,
      pixel(result.png, node.bounds.x + 16, node.bounds.y + node.bounds.height / 2),
      "container",
    )
    val message = Bounds(node.bounds.x + 24, node.bounds.y + 20, 100f, node.bounds.height - 40)
    assertSame(
      rgb(checkNotNull(node.colors?.content)),
      farthest(result.png, message, container),
      "message",
    )
  }

  @Test
  fun `only one snackbar is open at a time and a tap on the action or close closes it`() {
    var openedOther: String? = null
    var afterClose: String? = "unset"
    render(singleScreen(snackbar)) { host ->
      host.interaction.open("home", "saved")
      settleForTest()
      // The action opens the other snackbar, which replaces this one.
      onNodeWithTag("saved", useUnmergedTree = true).performClick()
      host.interaction.open("home", "other")
      openedOther = host.interaction.openOverlay("home")
      host.interaction.dismiss("home")
      afterClose = host.interaction.openOverlay("home")
    }
    assertEquals("other", openedOther)
    assertEquals(null, afterClose)
    val tree = render(singleScreen(snackbar)) { it.interaction.open("home", "other") }.tree
    assertTrue(tree.has("other"))
    assertFalse(tree.has("saved"), "the first snackbar is gone when another opens")
  }

  /** Three destinations on a compact window; the first carries [badges]. */
  private fun navProposal(badges: String): Proposal {
    val screen = screenJson("""{"id":"root","component":"Text","text":"Page"}""")
    return proposalOf(
      "home" to screen,
      "saved" to screen,
      "profile" to screen,
      destinations =
        """[{"screen":"home","label":"Home","icon":"home"$badges},""" +
          """{"screen":"saved","label":"Chat","icon":"chat"},""" +
          """{"screen":"profile","label":"Profile","icon":"person"}]""",
    )
  }

  /** How many pixels in [region] (dp) are close to [color]. */
  private fun count(png: ByteArray, region: Bounds, color: Int): Int {
    val image = image(png)
    var n = 0
    for (x in (region.x * scale).toInt() until ((region.x + region.width) * scale).toInt()) {
      for (y in (region.y * scale).toInt() until ((region.y + region.height) * scale).toInt()) {
        if (distance(image.getRGB(x, y) and 0xFFFFFF, color) <= 12) n++
      }
    }
    return n
  }

  private fun error() =
    ThemeResolver.resolve(designOf(null), dark = false).color(ColorRole.Error).toHex().let(::rgb)

  private fun itemRegion(node: LayoutNode) =
    Bounds(node.bounds.x, node.bounds.y, node.bounds.width, node.bounds.height)

  @Test
  fun `a badge draws in the error color on the icon, small for true and large for a text`() {
    val none = render(navProposal(""))
    val small = render(navProposal(""","badge":true"""))
    val large = render(navProposal(""","badge":"99+""""))
    val color = error()
    val region = itemRegion(none.tree.node("@navigation[0]"))
    val dot = count(small.png, region, color)
    assertEquals(0, count(none.png, region, color), "no badge, no error color")
    assertTrue(dot > 0, "a small badge draws in the error color")
    assertTrue(count(large.png, region, color) > dot, "a large badge is bigger than the dot")
    val chat = itemRegion(small.tree.node("@navigation[1]"))
    assertEquals(0, count(small.png, chat, color), "the other item has none")
  }

  private fun androidx.compose.ui.test.ComposeUiTest.settleForTest() {
    with(bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessRenderer) { settle() }
  }
}
