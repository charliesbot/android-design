package bot.charlies.androiddesign.preview.feature.preview

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessRenderer
import bot.charlies.androiddesign.preview.feature.preview.theme.ThemeResolver
import bot.charlies.androiddesign.preview.feature.preview.theme.toHex
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

/** Radio groups, menu roles and the new variants answer taps and draw the colors the tree says. */
class RadioAndVariantsRenderTest {
  private val scale = 2 // the png is two pixels per dp

  private fun image(png: ByteArray): BufferedImage = ImageIO.read(ByteArrayInputStream(png))

  private fun rgb(hex: String) = hex.removePrefix("#").toInt(16)

  private fun roleRgb(role: ColorRole) =
    rgb(
      ThemeResolver.resolve(designOf(null), dark = false).color(role).toHex(),
    )

  private fun distance(a: Int, b: Int) =
    listOf(16, 8, 0).sumOf { abs(((a shr it) and 0xFF) - ((b shr it) and 0xFF)) }

  private fun pixel(png: ByteArray, xDp: Float, yDp: Float) =
    image(png).getRGB((xDp * scale).toInt(), (yDp * scale).toInt()) and 0xFFFFFF

  private fun assertSame(expected: Int, drawn: Int, label: String) =
    assertTrue(
      distance(expected, drawn) <= 24,
      "$label: expected #%06X, drawn #%06X".format(expected, drawn),
    )

  private fun ComposeUiTest.settleForTest() {
    with(HeadlessRenderer) { settle() }
  }

  private val stage =
    """{"id":"root","component":"Scaffold","content":"stage"},""" +
      """{"id":"stage","component":"Column","padding":"lg","spacing":"md","children":["subject"]}"""

  private val radios =
    stage.replace("\"subject\"", "\"group\"") +
      """,{"id":"group","component":"RadioGroup","children":["a","b"]}""" +
      """,{"id":"a","component":"RadioButton","label":"Small","selected":true}""" +
      """,{"id":"b","component":"RadioButton","label":"Large"}"""

  @Test
  fun `a tap on a radio row moves the group's selection and the whole row is the target`() {
    var checked = false
    render(singleScreen(radios)) {
      settleForTest()
      onNodeWithTag("a", useUnmergedTree = true).assertIsSelected()
      onNodeWithTag("b", useUnmergedTree = true).assertIsNotSelected()
      onNodeWithTag("b", useUnmergedTree = true).performClick()
      settleForTest()
      onNodeWithTag("a", useUnmergedTree = true).assertIsNotSelected()
      onNodeWithTag("b", useUnmergedTree = true).assertIsSelected()
      // Tapping the selected row keeps it selected: one option is always selected.
      onNodeWithTag("b", useUnmergedTree = true).performClick()
      settleForTest()
      onNodeWithTag("b", useUnmergedTree = true).assertIsSelected()
      checked = true
    }
    assertTrue(checked)
  }

  @Test
  fun `a radio row is at least 48dp tall and as wide as its group`() {
    val tree = render(singleScreen(radios)).tree
    val row = tree.node("a")
    assertTrue(row.bounds.height >= 48f, "touch target ${row.bounds.height}dp")
    assertEquals(tree.node("group").bounds.width, row.bounds.width, "row fills the group")
  }

  private fun menuScreen(items: String, colors: String = "standard") =
    """{"id":"root","component":"Scaffold","content":"stage"},""" +
      """{"id":"stage","component":"Column","padding":"lg","children":["anchor"]},""" +
      """{"id":"anchor","component":"Button","label":"More","action":{"functionCall":{"@call":"open","args":{"overlay":"menu"}}}},""" +
      """{"id":"menu","component":"Menu","anchor":"anchor","colors":"$colors","items":["g1","g2"]},""" +
      """{"id":"g1","component":"MenuGroup","items":["s1","s2"]},""" +
      """{"id":"g2","component":"MenuGroup","items":["k1","a1"]},""" +
      items

  private val roles =
    """{"id":"s1","component":"MenuItem","label":"Newest","role":"single","selected":true},""" +
      """{"id":"s2","component":"MenuItem","label":"Oldest","role":"single"},""" +
      """{"id":"k1","component":"MenuItem","label":"Unread","role":"multi"},""" +
      """{"id":"a1","component":"MenuItem","label":"Settings"}"""

  @Test
  fun `a single-select item moves its group's selection and closes the menu`() {
    var open: String? = "unset"
    render(singleScreen(menuScreen(roles))) { host ->
      host.interaction.open("home", "menu")
      settleForTest()
      onNodeWithTag("s1", useUnmergedTree = true).assertIsSelected()
      onNodeWithTag("s2", useUnmergedTree = true).assertIsNotSelected()
      onNodeWithTag("s2", useUnmergedTree = true).performClick()
      settleForTest()
      open = host.interaction.openOverlay("home")
    }
    assertEquals(null, open, "a chosen option closes the menu")
    // Reopened, the choice is kept.
    var selected = false
    render(singleScreen(menuScreen(roles))) { host ->
      host.interaction.open("home", "menu")
      settleForTest()
      onNodeWithTag("s2", useUnmergedTree = true).performClick()
      settleForTest()
      host.interaction.open("home", "menu")
      settleForTest()
      onNodeWithTag("s1", useUnmergedTree = true).assertIsNotSelected()
      onNodeWithTag("s2", useUnmergedTree = true).assertIsSelected()
      selected = true
    }
    assertTrue(selected)
  }

  @Test
  fun `a multi-select item toggles and the menu stays open`() {
    var open: String? = null
    var toggled = false
    render(singleScreen(menuScreen(roles))) { host ->
      host.interaction.open("home", "menu")
      settleForTest()
      onNodeWithTag("k1", useUnmergedTree = true).assertIsNotSelected()
      onNodeWithTag("k1", useUnmergedTree = true).performClick()
      settleForTest()
      onNodeWithTag("k1", useUnmergedTree = true).assertIsSelected()
      onNodeWithTag("k1", useUnmergedTree = true).performClick()
      settleForTest()
      onNodeWithTag("k1", useUnmergedTree = true).assertIsNotSelected()
      open = host.interaction.openOverlay("home")
      toggled = true
    }
    assertTrue(toggled)
    assertEquals("menu", open, "a multi-select menu stays open until dismissed")
  }

  private val plates = """{"plates":[{"name":"Ramen","on":false},{"name":"Udon","on":true}]}"""

  private fun templatedMenu(role: String) =
    """{"id":"root","component":"Scaffold","content":"stage"},""" +
      """{"id":"stage","component":"Column","padding":"lg","children":["anchor"]},""" +
      """{"id":"anchor","component":"Button","label":"More","action":{"functionCall":{"@call":"open","args":{"overlay":"menu"}}}},""" +
      """{"id":"menu","component":"Menu","anchor":"anchor","items":{"componentId":"item","path":"/plates"}},""" +
      """{"id":"item","component":"MenuItem","label":{"@path":"name"},"role":"$role","selected":{"@path":"on"}}"""

  @Test
  fun `a templated multi item toggles from its bound selection`() {
    var toggled = false
    render(singleScreen(templatedMenu("multi"), plates)) { host ->
      host.interaction.open("home", "menu")
      settleForTest()
      onNodeWithTag("item[0]", useUnmergedTree = true).assertIsNotSelected()
      onNodeWithTag("item[1]", useUnmergedTree = true).assertIsSelected()
      onNodeWithTag("item[0]", useUnmergedTree = true).performClick()
      settleForTest()
      onNodeWithTag("item[0]", useUnmergedTree = true).assertIsSelected()
      onNodeWithTag("item[1]", useUnmergedTree = true).assertIsSelected()
      toggled = true
    }
    assertTrue(toggled)
  }

  @Test
  fun `a templated single item moves the pick among the data rows`() {
    var moved = false
    render(singleScreen(templatedMenu("single"), plates)) { host ->
      host.interaction.open("home", "menu")
      settleForTest()
      onNodeWithTag("item[0]", useUnmergedTree = true).assertIsNotSelected()
      onNodeWithTag("item[1]", useUnmergedTree = true).assertIsSelected()
      onNodeWithTag("item[0]", useUnmergedTree = true).performClick()
      settleForTest()
      host.interaction.open("home", "menu")
      settleForTest()
      onNodeWithTag("item[0]", useUnmergedTree = true).assertIsSelected()
      onNodeWithTag("item[1]", useUnmergedTree = true).assertIsNotSelected()
      moved = true
    }
    assertTrue(moved)
  }

  @Test
  fun `a templated group keeps one pick per data row`() {
    val sections =
      """{"sections":[{"opts":[{"name":"A","on":true},{"name":"B","on":false}]},""" +
        """{"opts":[{"name":"C","on":true},{"name":"D","on":false}]}]}"""
    val components =
      """{"id":"root","component":"Scaffold","content":"stage"},""" +
        """{"id":"stage","component":"Column","padding":"lg","children":["anchor"]},""" +
        """{"id":"anchor","component":"Button","label":"More","action":{"functionCall":{"@call":"open","args":{"overlay":"menu"}}}},""" +
        """{"id":"menu","component":"Menu","anchor":"anchor","items":{"componentId":"grp","path":"/sections"}},""" +
        """{"id":"grp","component":"MenuGroup","items":{"componentId":"item","path":"opts"}},""" +
        """{"id":"item","component":"MenuItem","label":{"@path":"name"},"role":"single","selected":{"@path":"on"}}"""
    var done = false
    render(singleScreen(components, sections)) { host ->
      host.interaction.open("home", "menu")
      settleForTest()
      onNodeWithTag("item[1][1]", useUnmergedTree = true).assertIsNotSelected()
      onNodeWithTag("item[1][1]", useUnmergedTree = true).performClick()
      settleForTest()
      host.interaction.open("home", "menu")
      settleForTest()
      onNodeWithTag("item[1][1]", useUnmergedTree = true).assertIsSelected()
      onNodeWithTag("item[1][0]", useUnmergedTree = true).assertIsNotSelected()
      // The other group keeps its own pick.
      onNodeWithTag("item[0][0]", useUnmergedTree = true).assertIsSelected()
      done = true
    }
    assertTrue(done)
  }

  @Test
  fun `a vibrant menu draws the container and the selected item colors its tree reports`() {
    for (colors in listOf("standard", "vibrant")) {
      val result =
        render(singleScreen(menuScreen(roles, colors))) { it.interaction.open("home", "menu") }
      val tree = result.tree
      val group = tree.node("g1")
      val expectedGroup =
        if (colors == "vibrant") roleRgb(ColorRole.TertiaryContainer)
        else roleRgb(ColorRole.SurfaceContainer)
      assertEquals(
        expectedGroup,
        rgb(checkNotNull(group.colors?.container)),
        "$colors group container in the tree",
      )
      // The unselected item below the selected one sits on the group's container.
      val unselected = tree.node("s2").bounds
      assertSame(
        expectedGroup,
        pixel(
          result.png,
          unselected.x + unselected.width - 6,
          unselected.y + unselected.height / 2,
        ),
        "$colors menu container drawn",
      )
      val selected = tree.node("s1")
      val expectedSelected =
        if (colors == "vibrant") roleRgb(ColorRole.Tertiary)
        else roleRgb(ColorRole.TertiaryContainer)
      assertEquals(
        expectedSelected,
        rgb(checkNotNull(selected.colors?.container)),
        "$colors selected item container in the tree",
      )
      assertSame(
        expectedSelected,
        pixel(
          result.png,
          selected.bounds.x + selected.bounds.width - 6,
          selected.bounds.y + selected.bounds.height / 2,
        ),
        "$colors selected item drawn",
      )
    }
  }

  @Test
  fun `a docked toolbar fills the bottom and draws the container its tree reports`() {
    for (colors in listOf("standard", "vibrant")) {
      val proposal =
        singleScreen(
          """{"id":"root","component":"Scaffold","bottomBar":"bar","content":"t"},""" +
            """{"id":"bar","component":"DockedToolbar","colors":"$colors","children":["i"]},""" +
            """{"id":"i","component":"IconButton","icon":"edit","contentDescription":"Edit"},""" +
            """{"id":"t","component":"Text","text":"Body"}""",
        )
      val result = render(proposal, view = Views.Light)
      val bar = result.tree.node("bar")
      val view = result.tree.node("root").bounds
      assertEquals(view.width, bar.bounds.width, "$colors: full width")
      assertEquals(
        view.y + view.height,
        bar.bounds.y + bar.bounds.height,
        1f,
        "$colors: at the bottom",
      )
      val expected = rgb(checkNotNull(bar.colors?.container))
      assertSame(
        expected,
        pixel(result.png, bar.bounds.x + bar.bounds.width / 2, bar.bounds.y + 4),
        "$colors docked toolbar container",
      )
      if (colors == "vibrant") {
        assertSame(roleRgb(ColorRole.PrimaryContainer), expected, "vibrant is primary container")
      }
    }
  }

  @Test
  fun `an indeterminate checkbox draws a filled box and a tap checks it`() {
    val proposal =
      singleScreen(
        stage.replace("\"subject\"", "\"box\"") +
          """,{"id":"box","component":"Checkbox","indeterminate":true,"label":"All"}""",
      )
    var after = false
    val result =
      render(proposal) {
        settleForTest()
        onNodeWithTag("box", useUnmergedTree = true).performClick()
        settleForTest()
        onNodeWithTag("box", useUnmergedTree = true).assertIsOn()
        after = true
      }
    assertTrue(after)
    // The unticked dash box is filled with primary; a clear box is not.
    val indeterminate = render(proposal)
    val clear =
      render(
        singleScreen(
          stage.replace("\"subject\"", "\"box\"") +
            """,{"id":"box","component":"Checkbox","label":"All"}""",
        ),
      )
    val bounds = indeterminate.tree.node("box").bounds
    // The 18dp box sits in a 40dp target, so its corner area is 11dp in; sample just inside it.
    val x = bounds.x + 11f
    val y = bounds.y + bounds.height / 2 - 6f
    val primary = roleRgb(ColorRole.Primary)
    assertSame(primary, pixel(indeterminate.png, x, y), "indeterminate box fill")
    assertTrue(distance(primary, pixel(clear.png, x, y)) > 60, "a clear box is not filled")
    assertTrue(result.png.isNotEmpty())
  }

  @Test
  fun `switch icons draw in the thumb only as far as icons says`() {
    fun switchScreen(icons: String, checked: Boolean) =
      singleScreen(
        stage.replace("\"subject\"", "\"sw\"") +
          """,{"id":"sw","component":"Switch","checked":$checked,"icons":"$icons"}""",
      )
    fun thumbDiffers(icons: String, checked: Boolean): Boolean {
      val plain = render(switchScreen("none", checked))
      val iconic = render(switchScreen(icons, checked))
      val bounds = plain.tree.node("sw").bounds
      // The thumb's icon is in the middle of the thumb: the right end when on, the left when off.
      val cx = if (checked) bounds.x + bounds.width - 20f else bounds.x + 20f
      val cy = bounds.y + bounds.height / 2
      var differs = false
      for (dx in -8..8) for (dy in -8..8) {
        val a = pixel(plain.png, cx + dx, cy + dy)
        val b = pixel(iconic.png, cx + dx, cy + dy)
        if (distance(a, b) > 60) differs = true
      }
      return differs
    }
    assertTrue(thumbDiffers("selected", true), "selected: a check when on")
    assertFalse(thumbDiffers("selected", false), "selected: no icon when off")
    assertTrue(thumbDiffers("both", true), "both: a check when on")
    assertTrue(thumbDiffers("both", false), "both: a close when off")
    val on = render(switchScreen("both", true))
    val bounds = on.tree.node("sw").bounds
    assertSame(
      rgb(checkNotNull(on.tree.node("sw").colors?.container)),
      pixel(on.png, bounds.x + 4, bounds.y + bounds.height / 2),
      "the track is still the tree's container",
    )
  }

  @Test
  fun `a vertical slider is taller than wide and a centered one fills from the middle`() {
    fun sliderScreen(props: String) =
      singleScreen(
        stage.replace("\"subject\"", "\"s\"") +
          """,{"id":"s","component":"Slider","range":[-1,1],"value":0.5,$props}""",
      )
    val vertical = render(sliderScreen(""""orientation":"vertical""""), view = Views.Light).tree
    val bounds = vertical.node("s").bounds
    assertTrue(bounds.height >= 190f, "vertical height ${bounds.height}")
    assertTrue(bounds.width < bounds.height / 2, "vertical width ${bounds.width}")
    val horizontal = render(sliderScreen(""""variant":"centered"""")).tree.node("s").bounds
    assertTrue(horizontal.width > horizontal.height * 4, "centered stays horizontal")
  }

  /** The pixel in [region] (dp) farthest from [backdrop]: the fully covered color of an icon. */
  private fun farthest(
    png: ByteArray,
    region: bot.charlies.androiddesign.preview.model.Bounds,
    backdrop: Int,
  ): Int {
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

  @Test
  fun `an icon button in a toolbar takes the toolbar's content color`() {
    for (toolbar in listOf("DockedToolbar", "FloatingToolbar")) {
      for (colors in listOf("standard", "vibrant")) {
        val proposal =
          singleScreen(
            """{"id":"root","component":"Scaffold","bottomBar":"bar","content":"t"},""" +
              """{"id":"bar","component":"$toolbar","colors":"$colors","children":["i"]},""" +
              """{"id":"i","component":"IconButton","icon":"edit","contentDescription":"Edit"},""" +
              """{"id":"t","component":"Text","text":"Body"}""",
          )
        val result = render(proposal, view = Views.Light)
        val container = rgb(checkNotNull(result.tree.node("bar").colors?.container))
        val expected =
          roleRgb(if (colors == "vibrant") ColorRole.OnPrimaryContainer else ColorRole.OnSurface)
        val icon = result.tree.node("i")
        assertEquals(expected, rgb(checkNotNull(icon.colors?.content)), "$toolbar $colors tree")
        assertSame(expected, farthest(result.png, icon.bounds, container), "$toolbar $colors drawn")
      }
    }
  }

  @Test
  fun `a vertical slider has zero at the bottom`() {
    val proposal =
      singleScreen(
        stage.replace("\"subject\"", "\"s\"") +
          """,{"id":"s","component":"Slider","orientation":"vertical","range":[0,100],"value":25}""",
      )
    val result = render(proposal)
    val b = result.tree.node("s").bounds
    val x = b.x + b.width / 2
    val active = roleRgb(ColorRole.Primary)
    assertSame(active, pixel(result.png, x, b.y + b.height - 12f), "the bottom is filled")
    assertTrue(distance(active, pixel(result.png, x, b.y + 12f)) > 60, "the top is not")
  }

  @Test
  fun `a menu's groups and items are shaped by the siblings shown at this size`() {
    val proposal =
      singleScreen(
        """{"id":"root","component":"Scaffold","content":"stage"},""" +
          """{"id":"stage","component":"Column","padding":"lg","children":["anchor"]},""" +
          """{"id":"anchor","component":"Button","label":"More","action":{"functionCall":{"@call":"open","args":{"overlay":"menu"}}}},""" +
          """{"id":"menu","component":"Menu","anchor":"anchor","items":["a1","a2","a3"]},""" +
          """{"id":"a1","component":"MenuGroup","items":["i1"],"showIn":["medium","expanded"]},""" +
          """{"id":"a2","component":"MenuGroup","items":["i2","i2b"]},""" +
          """{"id":"a3","component":"MenuGroup","items":["i3"]},""" +
          """{"id":"i1","component":"MenuItem","label":"One"},""" +
          """{"id":"i2","component":"MenuItem","label":"Two"},""" +
          """{"id":"i2b","component":"MenuItem","label":"Two b"},""" +
          """{"id":"i3","component":"MenuItem","label":"Three"}""",
      )
    val compact = render(proposal) { it.interaction.open("home", "menu") }
    assertFalse(compact.tree.has("a1"), "hidden at compact")
    val first = compact.tree.node("a2").bounds
    val second = compact.tree.node("a3").bounds
    assertTrue(second.y > first.y + first.height, "a gap between the groups shown")
    // groupShape(0, 2): the first group's top corners are the large outer radius, so the pixel just
    // inside its corner is outside the rounded shape.
    val corner = pixel(compact.png, first.x + 4f, first.y + 4f)
    val container = roleRgb(ColorRole.SurfaceContainer)
    assertTrue(distance(container, corner) > 24, "the first group's top corner is rounded")
    // With the hidden group shown, a2 is the second of three and its top corners are square.
    val medium = render(proposal, view = Views.Tablet) { it.interaction.open("home", "menu") }
    val middle = medium.tree.node("a2").bounds
    assertSame(
      container,
      pixel(medium.png, middle.x + 4f, middle.y + 4f),
      "a middle group's corner",
    )
  }

  private fun menuWith(items: String, members: String) =
    """{"id":"root","component":"Scaffold","content":"stage"},""" +
      """{"id":"stage","component":"Column","padding":"lg","children":["anchor"]},""" +
      """{"id":"anchor","component":"Button","label":"More","action":{"functionCall":{"@call":"open","args":{"overlay":"menu"}}}},""" +
      """{"id":"menu","component":"Menu","anchor":"anchor","items":[$items]},""" +
      members

  @Test
  fun `a menu whose only group is hidden draws no group box`() {
    val result =
      render(
        singleScreen(
          menuWith(
            "\"g\"",
            """{"id":"g","component":"MenuGroup","items":["i"],"showIn":[]},""" +
              """{"id":"i","component":"MenuItem","label":"Never"}""",
          ),
        ),
      ) {
        it.interaction.open("home", "menu")
      }
    assertFalse(result.tree.has("i"))
    val below = result.tree.node("anchor").bounds
    val background = roleRgb(ColorRole.Surface)
    var drawn = 0
    for (x in 0 until 200) for (y in 0 until 120) {
      val px = pixel(result.png, below.x + x * 0.5f, below.y + below.height + 8f + y * 0.5f)
      if (distance(px, background) > 12) drawn++
    }
    assertEquals(0, drawn, "nothing is drawn below the anchor")
  }

  @Test
  fun `a menu of items stays a menu of items when a group is hidden at this size`() {
    val result =
      render(
        singleScreen(
          menuWith(
            "\"g\",\"x\"",
            """{"id":"g","component":"MenuGroup","items":["i"],"showIn":[]},""" +
              """{"id":"i","component":"MenuItem","label":"Never"},""" +
              """{"id":"x","component":"MenuItem","label":"Kept"}""",
          ),
        ),
      ) {
        it.interaction.open("home", "menu")
      }
    val item = result.tree.node("x").bounds
    assertSame(
      roleRgb(ColorRole.SurfaceContainer),
      pixel(result.png, item.x + item.width - 6f, item.y + item.height / 2),
      "the item sits in its group's container",
    )
  }

  @Test
  fun `a menu of templated items draws a row for each data item`() {
    val result =
      render(
        singleScreen(
          """{"id":"root","component":"Scaffold","content":"stage"},""" +
            """{"id":"stage","component":"Column","padding":"lg","children":["anchor"]},""" +
            """{"id":"anchor","component":"Button","label":"More","action":{"functionCall":{"@call":"open","args":{"overlay":"menu"}}}},""" +
            """{"id":"menu","component":"Menu","anchor":"anchor","items":{"componentId":"row","path":"/recent"}},""" +
            """{"id":"row","component":"MenuItem","label":{"@path":"name"}}""",
          """{"recent":[{"name":"Alpha"},{"name":"Beta"}]}""",
        ),
      ) {
        it.interaction.open("home", "menu")
      }
    assertEquals("Alpha", result.tree.node("row[0]").text?.value)
    assertEquals("Beta", result.tree.node("row[1]").text?.value)
  }
}
