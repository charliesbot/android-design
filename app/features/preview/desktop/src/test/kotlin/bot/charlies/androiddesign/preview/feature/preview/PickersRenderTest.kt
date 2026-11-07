package bot.charlies.androiddesign.preview.feature.preview

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import bot.charlies.androiddesign.preview.feature.preview.theme.ThemeResolver
import bot.charlies.androiddesign.preview.feature.preview.theme.toHex
import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.Views
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Dates, times and full-screen dialogs draw where, and in what, the tree says. */
class PickersRenderTest {
  private val scale = 2 // the png is two pixels per dp

  private fun image(png: ByteArray): BufferedImage = ImageIO.read(ByteArrayInputStream(png))

  private fun rgb(hex: String) = hex.removePrefix("#").toInt(16)

  private fun role(role: ColorRole) =
    ThemeResolver.resolve(designOf(null), dark = false).color(role).toHex().let(::rgb)

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
      "$label: expected #%06X, drawn #%06X".format(expected, drawn),
    )

  private fun page(picker: String, extra: String = "") =
    singleScreen(
      """{"id":"root","component":"Scaffold","content":"stage"},""" +
        """{"id":"stage","component":"Column","padding":"lg","spacing":"md","children":["field","open"]},""" +
        """{"id":"field","component":"TextField","label":"Date","value":"Nov 6, 2026"},""" +
        """{"id":"open","component":"Button","label":"Open","action":{"functionCall":{"@call":"open","args":{"overlay":"pick"}}}},""" +
        picker +
        extra,
    )

  private val date = """{"id":"pick","component":"DatePicker","selected":"2026-11-06"}"""

  private fun ComposeUiTest.settleForTest() {
    with(bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessRenderer) { settle() }
  }

  @Test
  fun `a picker is closed until opened and closes on dismiss`() {
    val pickers =
      listOf(
        date,
        """{"id":"pick","component":"DatePicker","variant":"modal-input","selected":"2026-11-06"}""",
        """{"id":"pick","component":"DatePicker","range":true,"start":"2026-11-06"}""",
        """{"id":"pick","component":"TimePicker","time":"09:30"}""",
        """{"id":"pick","component":"TimePicker","variant":"input","time":"09:30"}""",
      )
    for (picker in pickers) {
      assertFalse(render(page(picker)).tree.has("pick"), picker)
      for (view in listOf(Views.Light, Views.Tablet)) {
        val tree = render(page(picker), view = view) { it.interaction.open("home", "pick") }.tree
        assertTrue(tree.has("pick"), "$picker at ${view.name}")
        assertTrue(tree.node("pick").bounds.height > 0f)
      }
      var after: String? = "unset"
      render(page(picker)) { host ->
        host.interaction.open("home", "pick")
        settleForTest()
        onNodeWithText("Cancel", useUnmergedTree = true).performClick()
        after = host.interaction.openOverlay("home")
      }
      assertEquals(null, after, picker)
    }
  }

  @Test
  fun `a date picker opens on the month of its date, not the clock's`() {
    for ((selected, month) in
      listOf("2027-03-15" to "March 2027", "1999-12-31" to "December 1999")) {
      val picker = """{"id":"pick","component":"DatePicker","selected":"$selected"}"""
      render(page(picker)) { host ->
        host.interaction.open("home", "pick")
        settleForTest()
        onNodeWithText(month, useUnmergedTree = true).assertExists()
      }
    }
  }

  @Test
  fun `picking a date updates the state, and a reopened picker keeps it`() {
    var picked: String? = null
    var reopened: String? = null
    render(page(date)) { host ->
      host.interaction.open("home", "pick")
      settleForTest()
      onNodeWithText("November 20, 2026", substring = true, useUnmergedTree = true).performClick()
      settleForTest()
      picked = host.interaction.get<String>("home::pick:selected")
      onNodeWithText("OK", useUnmergedTree = true).performClick()
      host.interaction.open("home", "pick")
      settleForTest()
      reopened = host.interaction.get<String>("home::pick:selected")
    }
    assertEquals("2026-11-20", picked)
    assertEquals("2026-11-20", reopened)
  }

  @Test
  fun `picking a range updates start and end`() {
    var start: String? = null
    var end: String? = null
    val range =
      """{"id":"pick","component":"DatePicker","range":true,"start":"2026-11-06","end":"2026-11-08"}"""
    render(page(range)) { host ->
      host.interaction.open("home", "pick")
      settleForTest()
      assertEquals("2026-11-06", host.interaction.get<String>("home::pick:start"))
      onNodeWithText("November 12, 2026", substring = true, useUnmergedTree = true).performClick()
      settleForTest()
      start = host.interaction.get<String>("home::pick:start")
      end = host.interaction.get<String>("home::pick:end")
    }
    // A tap after a complete range starts a new one.
    assertEquals("2026-11-12", start)
    assertEquals("", end)
  }

  @Test
  fun `picking a time and switching to input updates the state`() {
    var time: String? = null
    var mode: String? = null
    render(page("""{"id":"pick","component":"TimePicker","time":"09:30"}""")) { host ->
      host.interaction.open("home", "pick")
      settleForTest()
      assertEquals("09:30", host.interaction.get<String>("home::pick:time") ?: "09:30")
      onNodeWithText("PM", useUnmergedTree = true).performClick()
      settleForTest()
      time = host.interaction.get<String>("home::pick:time")
      onNodeWithContentDescription("Switch to text input mode", useUnmergedTree = true)
        .performClick()
      settleForTest()
      mode = host.interaction.get<String>("home::pick:mode")
    }
    assertEquals("21:30", time)
    assertEquals("input", mode)
  }

  @Test
  fun `a docked picker opens directly below its field and not at screen level`() {
    val docked =
      """{"id":"pick","component":"DatePicker","variant":"docked","selected":"2026-11-06","anchor":"field"}"""
    for (view in listOf(Views.Light, Views.Tablet)) {
      val tree = render(page(docked), view = view) { it.interaction.open("home", "pick") }.tree
      val field = tree.node("field").bounds
      val picker = tree.node("pick").bounds
      assertEquals(field.y + field.height, picker.y, 0.5f, view.name)
      assertEquals(field.x, picker.x, 0.5f, view.name)
      assertEquals(360f, picker.width, 1f, view.name)
    }
  }

  @Test
  fun `a full-screen picker fills the window and its close and save dismiss it`() {
    val full =
      """{"id":"pick","component":"DatePicker","variant":"full-screen","selected":"2026-11-06"}"""
    val tree = render(page(full)) { it.interaction.open("home", "pick") }.tree
    val bounds = tree.node("pick").bounds
    assertEquals(Views.Light.width.toFloat(), bounds.width, 1f)
    assertEquals(Views.Light.height.toFloat(), bounds.height, 1f)
    for (action in listOf("Save", "Close")) {
      var after: String? = "unset"
      render(page(full)) { host ->
        host.interaction.open("home", "pick")
        settleForTest()
        if (action == "Save") onNodeWithText("Save", useUnmergedTree = true).performClick()
        else onNodeWithContentDescription("Close").performClick()
        after = host.interaction.openOverlay("home")
      }
      assertEquals(null, after, action)
    }
  }

  private val fullDialog =
    """{"id":"pick","component":"Dialog","variant":"full-screen","title":"New event","confirm":"save","content":["f1"],"showIn":["compact"]},""" +
      """{"id":"save","component":"Button","variant":"text","label":"Save","action":{"functionCall":{"@call":"dismiss"}}},""" +
      """{"id":"f1","component":"TextField","label":"Title"}"""

  @Test
  fun `a full-screen dialog fills the window, shows its content, and closes on close or save`() {
    val tree = render(page(fullDialog)) { it.interaction.open("home", "pick") }.tree
    val bounds = tree.node("pick").bounds
    assertEquals(Views.Light.width.toFloat(), bounds.width, 1f)
    assertEquals(Views.Light.height.toFloat(), bounds.height, 1f)
    assertTrue(tree.has("f1") && tree.has("save"))
    for (action in listOf("Save", "Close")) {
      var after: String? = "unset"
      render(page(fullDialog)) { host ->
        host.interaction.open("home", "pick")
        settleForTest()
        if (action == "Save") onNodeWithTag("save", useUnmergedTree = true).performClick()
        else onNodeWithContentDescription("Close").performClick()
        after = host.interaction.openOverlay("home")
      }
      assertEquals(null, after, action)
    }
  }

  @Test
  fun `a basic dialog is unchanged`() {
    val basic =
      """{"id":"pick","component":"Dialog","title":"Discard?","text":"Lost.","confirm":"save"},""" +
        """{"id":"save","component":"Button","variant":"text","label":"Discard"}"""
    val tree = render(page(basic)) { it.interaction.open("home", "pick") }.tree
    assertTrue(tree.node("pick").bounds.width < Views.Light.width)
  }

  @Test
  fun `the described colors of the date picker, time picker and full-screen surfaces are drawn`() {
    val surface = role(ColorRole.SurfaceContainerHigh)
    for (picker in
      listOf(
        date,
        """{"id":"pick","component":"TimePicker","time":"09:30"}""",
        """{"id":"pick","component":"DatePicker","variant":"docked","selected":"2026-11-06","anchor":"field"}""",
      )) {
      val result = render(page(picker)) { it.interaction.open("home", "pick") }
      val node = result.tree.node("pick")
      assertEquals(surface, rgb(checkNotNull(node.colors?.container)))
      assertEquals(role(ColorRole.OnSurface), rgb(checkNotNull(node.colors?.content)))
      assertSame(
        surface,
        pixel(
          result.png,
          node.bounds.x + node.bounds.width / 3,
          node.bounds.y + node.bounds.height - 6,
        ),
        "container of $picker",
      )
    }
    for (full in
      listOf(
        """{"id":"pick","component":"DatePicker","variant":"full-screen","selected":"2026-11-06"}""",
        fullDialog,
      )) {
      val result = render(page(full)) { it.interaction.open("home", "pick") }
      val node = result.tree.node("pick")
      assertSame(surface, rgb(checkNotNull(node.colors?.container)), "described")
      assertSame(
        surface,
        pixel(result.png, node.bounds.width / 2, node.bounds.height - 20),
        "container of $full",
      )
    }
  }

  @Test
  fun `a date and a time are drawn in the content color the tree reports`() {
    val surface = role(ColorRole.SurfaceContainerHigh)
    val onSurface = role(ColorRole.OnSurface)
    val result = render(page(date)) { it.interaction.open("home", "pick") }
    val node = result.tree.node("pick")
    // A day number is on surface.
    val day = Bounds(node.bounds.x + 20, node.bounds.y + 150, node.bounds.width - 40, 220f)
    assertSame(onSurface, farthest(result.png, day, surface), "day numbers")
    val title =
      render(page(fullDialog)) { it.interaction.open("home", "pick") }
        .let { farthest(it.png, Bounds(48f, 8f, 160f, 40f), surface) }
    assertSame(onSurface, title, "full-screen headline")
  }
}
