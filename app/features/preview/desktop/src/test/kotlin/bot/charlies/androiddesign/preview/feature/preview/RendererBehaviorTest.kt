package bot.charlies.androiddesign.preview.feature.preview

import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import bot.charlies.androiddesign.preview.feature.preview.theme.ThemeResolver
import bot.charlies.androiddesign.preview.feature.preview.theme.toHex
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.Severity
import bot.charlies.androiddesign.preview.model.Views
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RendererBehaviorTest {
  private val platesDetail =
    """{"id":"root","component":"Scaffold","topBar":"bar","content":"body"},""" +
      """{"id":"bar","component":"TopAppBar","title":{"@path":"/plate/name"},"navigationIcon":"back"},""" +
      """{"id":"back","component":"IconButton","icon":"arrow_back","contentDescription":"Back","action":{"functionCall":{"@call":"back"}}},""" +
      """{"id":"body","component":"Text","text":{"@path":"/plate/name"}}"""

  private val platesList =
    """{"id":"root","component":"Scaffold","content":"rows"},""" +
      """{"id":"rows","component":"Column","children":{"componentId":"row","path":"/plates"}},""" +
      """{"id":"row","component":"ListItem","headline":{"@path":"name"},"supporting":{"@path":"/owner"},"action":{"functionCall":{"@call":"navigate","args":{"screen":"detail"}}}}"""

  private fun twoScreens() =
    proposalOf(
      "list" to
        screenJson(platesList, """{"plates":[{"name":"Burrata"},{"name":"Miso"}],"owner":"Ada"}"""),
      "detail" to screenJson(platesDetail, """{"plate":{"name":"Burrata"}}"""),
      panes = """{"list":"list","detail":"detail"}""",
    )

  @Test
  fun `templates repeat their component per item with relative and absolute bindings`() {
    val tree = render(twoScreens(), "list").tree
    assertEquals("Burrata", tree.node("row[0]").text?.value)
    assertEquals("Miso", tree.node("row[1]").text?.value)
    assertEquals("row[0]", tree.nodes.first { it.id == "row[0]" }.id)
    assertEquals("rows", tree.node("row[1]").parent)
  }

  @Test
  fun `index calls resolve to the template position`() {
    val proposal =
      singleScreen(
        """{"id":"root","component":"Scaffold","content":"rows"},{"id":"rows","component":"Column","children":{"componentId":"t","path":"/items"}},{"id":"t","component":"Text","text":{"@call":"@index"}}""",
        """{"items":["a","b","c"]}""",
      )
    val tree = render(proposal).tree
    assertEquals(listOf("0", "1", "2"), (0..2).map { tree.node("t[$it]").text?.value })
  }

  @Test
  fun `nodes are listed in document order with parents`() {
    val tree = render(twoScreens(), "detail", Views.Light).tree
    assertEquals(listOf("root", "bar", "back", "body"), tree.nodes.map { it.id })
    assertEquals("root", tree.node("bar").parent)
    assertNull(tree.node("root").parent)
    assertEquals("bar", tree.node("back").parent)
  }

  @Test
  fun `showIn hides a component outside its size classes`() {
    val proposal =
      singleScreen(
        """{"id":"root","component":"Scaffold","content":"col"},{"id":"col","component":"Column","children":["wide","narrow"]},""" +
          """{"id":"wide","component":"Text","text":"wide","showIn":["expanded"]},{"id":"narrow","component":"Text","text":"narrow","showIn":["compact","medium"]}""",
      )
    val phone = render(proposal, view = Views.Light).tree
    assertFalse(phone.has("wide"))
    assertTrue(phone.has("narrow"))
    val tablet = render(proposal, view = Views.Tablet).tree
    assertTrue(tablet.has("wide"))
    assertFalse(tablet.has("narrow"))
  }

  @Test
  fun `a max width caps a stretched card and the parent's alignment places it`() {
    val proposal =
      singleScreen(
        """{"id":"root","component":"Column","horizontalAlignment":"center","children":["card"]},""" +
          """{"id":"card","component":"Card","maxWidth":480,"content":["t"]},""" +
          """{"id":"t","component":"Text","text":"Auto sync"}""",
      )
    val foldable = render(proposal, view = Views.Foldable).tree.node("card").bounds
    assertEquals(480f, foldable.width, "$foldable")
    assertEquals((704f - 480f) / 2, foldable.x, "$foldable")
    val phone = render(proposal, view = Views.Light).tree.node("card").bounds
    assertEquals(411f, phone.width, "below its max width the card still fills: $phone")
  }

  @Test
  fun `sizeClass overrides apply from medium up using the frame width`() {
    val proposal =
      singleScreen(
        """{"id":"root","component":"Scaffold","content":"t"},""" +
          """{"id":"t","component":"Text","text":"compact","sizeClass":{"medium":{"text":"medium"},"expanded":{"text":"expanded"}}}""",
      )
    assertEquals("compact", render(proposal, view = Views.Light).tree.node("t").text?.value)
    assertEquals("medium", render(proposal, view = Views.Foldable).tree.node("t").text?.value)
    assertEquals("expanded", render(proposal, view = Views.Tablet).tree.node("t").text?.value)
    assertEquals("expanded", render(proposal, view = Views.Desktop).tree.node("t").text?.value)
  }

  @Test
  fun `a detail screen at tablet shows both panes and at phone only the detail`() {
    val tablet = render(twoScreens(), "detail", Views.Tablet).tree
    assertTrue(tablet.has("root"), "the detail's own nodes keep plain ids")
    assertTrue(tablet.has("list/row[0]"), "the list pane's nodes carry the screen prefix")
    assertTrue(tablet.has("list/root"))
    val phone = render(twoScreens(), "detail", Views.Light).tree
    assertTrue(phone.has("body"))
    assertFalse(phone.nodes.any { it.id.startsWith("list/") })
  }

  private fun withPlaceholder(declared: Boolean) =
    proposalOf(
      "list" to
        screenJson(platesList, """{"plates":[{"name":"Burrata"},{"name":"Miso"}],"owner":"Ada"}"""),
      "detail" to screenJson(platesDetail, """{"plate":{"name":"Burrata"}}"""),
      "pick" to
        screenJson(
          """{"id":"root","component":"Scaffold","content":"hint"},""" +
            """{"id":"hint","component":"Text","text":"Pick a plate"}""",
        ),
      panes = """{"list":"list","detail":"detail"}""",
      placeholders = if (declared) """{"list":"pick"}""" else "{}",
    )

  @Test
  fun `a two-pane list shows its placeholder in the detail pane`() {
    val tree = render(withPlaceholder(declared = true), "list", Views.Tablet).tree
    assertTrue(tree.has("row[0]"), "the list pane keeps plain ids")
    assertEquals("Pick a plate", tree.node("pick/hint").text?.value)
    assertEquals(emptyList(), tree.findings.filter { it.rule == FindingRule.EmptyDetailPane })
  }

  @Test
  fun `a one-pane list does not draw its placeholder`() {
    val tree = render(withPlaceholder(declared = true), "list", Views.Light).tree
    assertFalse(tree.nodes.any { it.id.startsWith("pick/") })
    assertEquals(emptyList(), tree.findings.filter { it.rule == FindingRule.EmptyDetailPane })
  }

  @Test
  fun `a two-pane list without a placeholder is noted`() {
    val tree = render(withPlaceholder(declared = false), "list", Views.Tablet).tree
    val note = tree.findings.single { it.rule == FindingRule.EmptyDetailPane }
    assertEquals(Severity.Info, note.severity)
    assertTrue("navigation.placeholders" in note.detail.orEmpty(), note.detail)
    val phone = render(withPlaceholder(declared = false), "list", Views.Light).tree
    assertEquals(emptyList(), phone.findings.filter { it.rule == FindingRule.EmptyDetailPane })
  }

  @Test
  fun `a selected detail leaves no empty pane`() {
    val tree = render(withPlaceholder(declared = false), "detail", Views.Tablet).tree
    assertEquals(emptyList(), tree.findings.filter { it.rule == FindingRule.EmptyDetailPane })
  }

  @Test
  fun `navigate and back actions move through the stack`() {
    var onDetailAfterClick = false
    var backOnList = false
    render(twoScreens(), "list", Views.Light) { host ->
      assertEquals(listOf("list"), host.backStack.toList())
      onNodeWithTag("row[0]", useUnmergedTree = true).performClick()
      onDetailAfterClick = host.backStack.toList() == listOf("list", "detail")
      settleForTest()
      onNodeWithTag("back", useUnmergedTree = true).performClick()
      backOnList = host.backStack.toList() == listOf("list")
    }
    assertTrue(onDetailAfterClick)
    assertTrue(backOnList)
  }

  private val withSheet =
    """{"id":"root","component":"Scaffold","content":"stage","floatingActionButton":"fab"},""" +
      """{"id":"stage","component":"Text","text":"Page"},""" +
      """{"id":"fab","component":"FloatingActionButton","icon":"add","contentDescription":"New","action":{"functionCall":{"@call":"open","args":{"overlay":"sheet"}}}},""" +
      """{"id":"sheet","component":"BottomSheet","content":["sheet_text","sheet_close"]},""" +
      """{"id":"sheet_text","component":"Text","text":"In sheet"},""" +
      """{"id":"sheet_close","component":"Button","variant":"text","label":"Close","action":{"functionCall":{"@call":"dismiss"}}}"""

  @Test
  fun `overlays stay closed until opened and close on dismiss`() {
    val closed = render(singleScreen(withSheet)).tree
    assertFalse(closed.has("sheet"))
    assertFalse(closed.has("sheet_text"))

    var openedInside: Boolean? = null
    var closedAgain: Boolean? = null
    val opened =
      render(singleScreen(withSheet)) { host ->
        onNodeWithTag("fab", useUnmergedTree = true).performClick()
        openedInside = host.interaction.openOverlay("home") == "sheet"
        settleForTest()
        onNodeWithTag("sheet_close", useUnmergedTree = true).performClick()
        closedAgain = host.interaction.openOverlay("home") == null
      }
    assertEquals(true, openedInside)
    assertEquals(true, closedAgain)
    assertFalse(opened.tree.has("sheet_text"), "dismissed sheets leave the tree")

    val stillOpen = render(singleScreen(withSheet)) { it.interaction.open("home", "sheet") }.tree
    assertTrue(stillOpen.has("sheet_text"))
    assertEquals("In sheet", stillOpen.node("sheet_text").text?.value)
  }

  @Test
  fun `toggles keep local state`() {
    val proposal =
      singleScreen(
        """{"id":"root","component":"Scaffold","content":"sw"},{"id":"sw","component":"Switch","checked":false,"contentDescription":"Alerts"}""",
      )
    var before = false
    var after = false
    render(proposal) {
      onNodeWithTag("sw", useUnmergedTree = true).assertIsOff()
      before = true
      onNodeWithTag("sw", useUnmergedTree = true).performClick()
      settleForTest()
      onNodeWithTag("sw", useUnmergedTree = true).assertIsOn()
      after = true
    }
    assertTrue(before && after)
  }

  @Test
  fun `tapping a tab moves the selection to it`() {
    val proposal =
      singleScreen(
        """{"id":"root","component":"Scaffold","content":"tabs"},{"id":"tabs","component":"Tabs","children":["a","b"]},""" +
          """{"id":"a","component":"Tab","label":"Plates","selected":true},{"id":"b","component":"Tab","label":"Menus"}""",
      )
    var checked = false
    render(proposal) {
      onNodeWithTag("a", useUnmergedTree = true).assertIsSelected()
      onNodeWithTag("b", useUnmergedTree = true).assertIsNotSelected()
      onNodeWithTag("b", useUnmergedTree = true).performClick()
      settleForTest()
      onNodeWithTag("a", useUnmergedTree = true).assertIsNotSelected()
      onNodeWithTag("b", useUnmergedTree = true).assertIsSelected()
      checked = true
    }
    assertTrue(checked)
  }

  @Test
  fun `long text with an ellipsis reports truncation`() {
    val long =
      "Slow-roasted tomatoes, burrata, basil oil, and grilled sourdough for two people tonight."
    val proposal =
      singleScreen(
        """{"id":"root","component":"Scaffold","content":"col"},{"id":"col","component":"Column","padding":"lg","children":["ellipsized","clipped","fits"]},""" +
          """{"id":"ellipsized","component":"Text","text":"$long","maxLines":1,"overflow":"ellipsis"},""" +
          """{"id":"clipped","component":"Text","text":"$long","maxLines":1},""" +
          """{"id":"fits","component":"Text","text":"Short","maxLines":1,"overflow":"ellipsis"}""",
      )
    val tree = render(proposal).tree
    val ellipsized = tree.node("ellipsized").text!!
    assertEquals(1, ellipsized.lines)
    assertTrue(ellipsized.overflow)
    assertTrue(ellipsized.truncated)
    val clipped = tree.node("clipped").text!!
    assertTrue(clipped.overflow)
    assertFalse(clipped.truncated)
    val fits = tree.node("fits").text!!
    assertFalse(fits.overflow)
    assertFalse(fits.truncated)
    assertEquals("Google Sans Flex", fits.font)
  }

  @Test
  fun `a wrapped button label reports more than one line`() {
    val proposal =
      singleScreen(
        """{"id":"root","component":"Scaffold","content":"col"},{"id":"col","component":"Column","padding":"lg","children":["b"]},""" +
          """{"id":"b","component":"Button","label":"A label that is far too long to fit on one line of a phone"}""",
      )
    assertTrue(render(proposal).tree.node("b").text!!.lines > 1)
  }

  @Test
  fun `a square icon button reports its square corner and a round one stays full`() {
    fun shapeOf(props: String): String? =
      render(
          singleScreen(
            """{"id":"root","component":"Scaffold","content":"col"},{"id":"col","component":"Column","children":["b"]},""" +
              """{"id":"b","component":"IconButton","icon":"play_arrow",$props}""",
          ),
        )
        .tree
        .node("b")
        .roles
        ?.shape
    assertEquals("full", shapeOf(""""variant":"filled""""))
    assertEquals("full", shapeOf(""""variant":"filled","shape":"round""""))
    assertEquals("medium", shapeOf(""""variant":"filled","shape":"square""""))
    assertEquals("medium", shapeOf(""""variant":"filled","shape":"square","size":"extra-small""""))
    assertEquals("large", shapeOf(""""variant":"filled","shape":"square","size":"medium""""))
    assertEquals("extraLarge", shapeOf(""""variant":"filled","shape":"square","size":"large""""))
    assertEquals(
      "extraLarge",
      shapeOf(""""variant":"filled","shape":"square","size":"extra-large""""),
    )
    assertEquals(
      "full",
      shapeOf(""""variant":"filled","shape":"square","toggle":true,"checked":true"""),
    )
    assertEquals("medium", shapeOf(""""variant":"filled","shape":"square","toggle":true"""))
  }

  @Test
  fun `a 40dp button has a 48dp touch target and accessibility facts`() {
    val proposal =
      singleScreen(
        """{"id":"root","component":"Scaffold","content":"col"},{"id":"col","component":"Column","padding":"lg","children":["cook"]},""" +
          """{"id":"cook","component":"Button","label":"Cook","primary":true}""",
      )
    val cook = render(proposal).tree.node("cook")
    assertEquals(40f, cook.bounds.height)
    assertEquals(48f, cook.touch!!.height)
    assertEquals(cook.bounds.y - 4f, cook.touch!!.y)
    assertEquals(true, cook.primary)
    assertEquals("Button", cook.a11y?.role)
    assertEquals("Cook", cook.a11y?.label)
    assertTrue("click" in cook.a11y!!.actions)
    assertEquals("primary", cook.roles?.container)
    assertEquals("onPrimary", cook.roles?.content)
    assertEquals("labelLarge", cook.roles?.type)
    assertEquals(
      setOf(true),
      setOf(cook.colors?.container?.matches(Regex("#[0-9A-F]{6}"))),
    )
  }

  /**
   * Compose wraps both sliders in `minimumInteractiveComponentSize()`, so their layout box is 48dp
   * tall around a 44dp handle (the M3 slider spec's handle height). The wrapper is the touch
   * target; the slider's own semantics node sits inside it and reports only the 44dp handle.
   */
  @Test
  fun `a slider and a range slider keep Compose's 48dp touch target around the 44dp handle`() {
    val proposal =
      singleScreen(
        """{"id":"root","component":"Scaffold","content":"col"},{"id":"col","component":"Column","padding":"lg","children":["seek","span"]},""" +
          """{"id":"seek","component":"Slider","value":0.4},""" +
          """{"id":"span","component":"RangeSlider","range":[0,100],"value":[20,80]}""",
      )
    val tree = render(proposal).tree
    for (id in listOf("seek", "span")) {
      val node = tree.node(id)
      assertEquals(48f, node.touch!!.height, "$id touch height")
      assertTrue(node.touch!!.width >= node.bounds.width, "$id touch width")
    }
    assertEquals(
      emptyList(),
      tree.findings.filter { it.rule == FindingRule.TouchTarget },
      "no slider fails the touch target rule",
    )
  }

  /**
   * A slider's semantics node is 10dp wider than its layout box on each side (the handle's
   * overhang). Bounds describe the layout box, so a full-width slider 8dp from the edges is not
   * flagged as off screen.
   */
  @Test
  fun `a full-width slider and range slider stay inside the view`() {
    val proposal =
      singleScreen(
        """{"id":"root","component":"Scaffold","content":"col"},{"id":"col","component":"Column","padding":"sm","children":["seek","span"]},""" +
          """{"id":"seek","component":"Slider","value":0.4},""" +
          """{"id":"span","component":"RangeSlider","range":[0,100],"value":[20,80]}""",
      )
    val tree = render(proposal).tree
    val width = tree.view.width.toFloat()
    for (id in listOf("seek", "span")) {
      val bounds = tree.node(id).bounds
      assertEquals(8f, bounds.x, "$id x")
      assertEquals(width - 16f, bounds.width, "$id width")
    }
    assertEquals(
      emptyList(),
      tree.findings.filter { it.rule == FindingRule.Offscreen },
      "no slider is flagged off screen",
    )
  }

  @Test
  fun `named styles and token props resolve`() {
    val proposal =
      proposalOf(
        "home" to
          screenJson(
            """{"id":"root","component":"Scaffold","content":"t"},{"id":"t","component":"Text","text":"Styled","style":"hero","padding":"lg"}""",
          ),
        theme =
          """{"material":{"styles":{"hero":{"type":"displaySmallEmphasized","color":"onPrimaryContainer"}}}}""",
      )
    val t = render(proposal).tree.node("t")
    assertEquals("displaySmallEmphasized", t.roles?.type)
    assertEquals("onPrimaryContainer", t.roles?.content)
    assertEquals(16f, t.bounds.x, "padding lg is 16dp")
  }

  private val styledTheme =
    """{"material":{"styles":{"cta":{"container":"tertiaryContainer","color":"onTertiaryContainer"}}}}"""

  private fun hexOf(role: ColorRole) =
    ThemeResolver.resolve(
        designOf(proposalOf("home" to screenJson("{}"), theme = styledTheme).theme),
        dark = false,
      )
      .color(role)
      .toHex()

  @Test
  fun `a styled button reports its style's container and color`() {
    val proposal =
      proposalOf(
        "home" to
          screenJson(
            """{"id":"root","component":"Scaffold","content":"b"},{"id":"b","component":"Button","label":"Go","style":"cta"}""",
          ),
        theme = styledTheme,
      )
    val b = render(proposal).tree.node("b")
    assertEquals("tertiaryContainer", b.roles?.container)
    assertEquals("onTertiaryContainer", b.roles?.content)
    assertEquals(hexOf(ColorRole.TertiaryContainer), b.colors?.container)
    assertEquals(hexOf(ColorRole.OnTertiaryContainer), b.colors?.content)
  }

  @Test
  fun `a styled text reports its style's container as its backdrop`() {
    val proposal =
      proposalOf(
        "home" to
          screenJson(
            """{"id":"root","component":"Scaffold","content":"t"},{"id":"t","component":"Text","text":"Hi","style":"cta"}""",
          ),
        theme = styledTheme,
      )
    val t = render(proposal).tree.node("t")
    assertEquals("tertiaryContainer", t.roles?.container)
    assertEquals("onTertiaryContainer", t.roles?.content)
    assertEquals(hexOf(ColorRole.TertiaryContainer), t.colors?.container)
  }

  @Test
  fun `a styled layout reports its style's container and color`() {
    for (layout in listOf("Column", "Row", "Box", "Grid", "FlexBox")) {
      val proposal =
        proposalOf(
          "home" to
            screenJson(
              """{"id":"root","component":"Scaffold","content":"l"},{"id":"l","component":"$layout","style":"cta","children":["t"]},{"id":"t","component":"Text","text":"Hi"}""",
            ),
          theme = styledTheme,
        )
      val l = render(proposal).tree.node("l")
      assertEquals("tertiaryContainer", l.roles?.container, layout)
      assertEquals("onTertiaryContainer", l.roles?.content, layout)
      assertEquals(hexOf(ColorRole.TertiaryContainer), l.colors?.container, layout)
    }
  }

  @Test
  fun `a shape reports the color it paints as its container`() {
    val proposal =
      singleScreen(
        """{"id":"root","component":"Scaffold","content":"s"},{"id":"s","component":"Shape","shape":"cookie9Sided","color":"tertiaryContainer"}""",
      )
    val s = render(proposal).tree.node("s")
    assertEquals("tertiaryContainer", s.roles?.container)
    assertEquals(hexOf(ColorRole.TertiaryContainer), s.colors?.container)
  }

  @Test
  fun `a selected list item reports the secondary container it is drawn on`() {
    val proposal =
      singleScreen(
        """{"id":"root","component":"Scaffold","content":"plain"},""" +
          """{"id":"plain","component":"List","children":["on","off"]},""" +
          """{"id":"on","component":"ListItem","headline":"On","selected":true},""" +
          """{"id":"off","component":"ListItem","headline":"Off"}""",
      )
    val tree = render(proposal).tree
    assertEquals("secondaryContainer", tree.node("on").roles?.container)
    assertEquals(hexOf(ColorRole.SecondaryContainer), tree.node("on").colors?.container)
    assertEquals("surface", tree.node("off").roles?.container)
  }

  @Test
  fun `a selected segmented list item reports the secondary container and its content color`() {
    val proposal =
      singleScreen(
        """{"id":"root","component":"Scaffold","content":"seg"},""" +
          """{"id":"seg","component":"List","variant":"segmented","children":["on","off"]},""" +
          """{"id":"on","component":"ListItem","headline":"On","selected":true},""" +
          """{"id":"off","component":"ListItem","headline":"Off"}""",
      )
    val tree = render(proposal).tree
    assertEquals("secondaryContainer", tree.node("on").roles?.container)
    assertEquals("onSecondaryContainer", tree.node("on").roles?.content)
    assertEquals("surfaceContainer", tree.node("off").roles?.container)
    assertEquals("onSurface", tree.node("off").roles?.content)
  }

  @Test
  fun `the png is two pixels per dp`() {
    val png =
      render(
          singleScreen(
            """{"id":"root","component":"Scaffold","content":"t"},{"id":"t","component":"Text","text":"x"}""",
          ),
        )
        .png
    val image = org.jetbrains.skia.Image.makeFromEncoded(png)
    assertEquals(822, image.width)
    assertEquals(1846, image.height)
  }

  @Test
  fun `a fab at the bottom of a tall frame keeps its shadow against its edge`() {
    val proposal =
      singleScreen(
        """{"id":"root","component":"Scaffold","floatingActionButton":"fab","content":"t"},{"id":"t","component":"Text","text":"x"},""" +
          """{"id":"fab","component":"FloatingActionButton","size":"medium","extended":true,"icon":"add","label":"New plate"}""",
      )
    val result = render(proposal, view = Views.Light)
    val fab = result.tree.node("fab").bounds
    val bitmap =
      org.jetbrains.skia.Bitmap.makeFromImage(org.jetbrains.skia.Image.makeFromEncoded(result.png))
    val x = ((fab.x + fab.width / 2) * 2).toInt()
    val edge = ((fab.y + fab.height) * 2).toInt()
    fun luminance(y: Int): Int =
      bitmap.getColor(x, y).let { ((it shr 16) and 255) + ((it shr 8) and 255) + (it and 255) }
    val background = luminance(bitmap.height - 2)
    // Compose Desktop's window light used to push the shadow down and leave background showing
    // here.
    for (below in 2..4) {
      assertTrue(
        luminance(edge + below) < background - 30,
        "pixel ${below}px under the fab is background, not shadow",
      )
    }
  }

  /** A Scaffold whose content is a Row of two short Columns, aligned as [alignment] says. */
  private fun panes(alignment: String?): Proposal {
    val stated = if (alignment == null) "" else ""","verticalAlignment":"$alignment""""
    return singleScreen(
      """{"id":"root","component":"Scaffold","content":"row"},""" +
        """{"id":"row","component":"Row"$stated,"children":["left","right"]},""" +
        """{"id":"left","component":"Column","children":["a"]},""" +
        """{"id":"right","component":"Column","children":["b"]},""" +
        """{"id":"a","component":"Text","text":"Left"},{"id":"b","component":"Text","text":"Right"}""",
    )
  }

  @Test
  fun `a row as the content of a scaffold fills the content height`() {
    val tree = render(panes(null), view = Views.Tablet).tree
    assertEquals(tree.node("root").bounds.height, tree.node("row").bounds.height)
  }

  @Test
  fun `stretch gives the columns of a row the height of the row`() {
    val tree = render(panes("stretch"), view = Views.Tablet).tree
    val height = tree.node("root").bounds.height
    assertEquals(height, tree.node("row").bounds.height, "row")
    assertEquals(height, tree.node("left").bounds.height, "left")
    assertEquals(height, tree.node("right").bounds.height, "right")
    assertEquals(0f, tree.node("a").bounds.y, "other children stay at the top")
  }

  /** A divider between the panes must not shrink them: the export fills the height either way. */
  @Test
  fun `a stretched row with a vertical divider still fills the content height`() {
    val tree =
      render(
          singleScreen(
            """{"id":"root","component":"Scaffold","content":"row"},""" +
              """{"id":"row","component":"Row","verticalAlignment":"stretch","children":["left","line","right"]},""" +
              """{"id":"left","component":"Column","children":["a"]},""" +
              """{"id":"line","component":"Divider","vertical":true},""" +
              """{"id":"right","component":"Column","children":["b"]},""" +
              """{"id":"a","component":"Text","text":"Left"},{"id":"b","component":"Text","text":"Right"}""",
          ),
          view = Views.Tablet,
        )
        .tree
    val height = tree.node("root").bounds.height
    assertEquals(height, tree.node("row").bounds.height, "row")
    assertEquals(height, tree.node("left").bounds.height, "left")
  }

  @Test
  fun `a row without stretch keeps its columns as tall as their content`() {
    val tree = render(panes(null), view = Views.Tablet).tree
    assertTrue(tree.node("left").bounds.height < tree.node("root").bounds.height / 2)
  }

  private fun collapsingScreen(size: String, extra: String = "") =
    singleScreen(
      """{"id":"root","component":"Scaffold","topBar":"bar","content":"col"},""" +
        """{"id":"bar","component":"TopAppBar","size":"$size","title":"Title"$extra},""" +
        """{"id":"col","component":"Column","scroll":true,"children":[${(0 until 60).joinToString(",") { "\"t$it\"" }}]},""" +
        (0 until 60).joinToString(",") { """{"id":"t$it","component":"Text","text":"Row $it"}""" },
    )

  @Test
  fun `medium and large bars draw expanded at rest and collapse as the content scrolls`() {
    for ((size, expanded) in listOf("medium" to 112f, "large" to 120f)) {
      val proposal = collapsingScreen(size)
      val atRest = render(proposal).tree.node("bar").bounds.height
      assertEquals(expanded, atRest, "$size at rest")
      val scrolled =
        render(proposal) {
            settleForTest()
            onNodeWithTag("col", useUnmergedTree = true).performTouchInput { swipeUp() }
            settleForTest()
          }
          .tree
          .node("bar")
          .bounds
          .height
      assertEquals(64f, scrolled, "$size after scrolling")
    }
  }

  @Test
  fun `a collapsing bar hidden at the window size does not hold back the content scroll`() {
    val proposal = collapsingScreen("large", ""","showIn":["compact"]""")
    val before = render(proposal, view = Views.Tablet).tree.node("t10").bounds.y
    val after =
      render(proposal, view = Views.Tablet) {
          settleForTest()
          onNodeWithTag("col", useUnmergedTree = true).performTouchInput { swipeUp() }
          settleForTest()
        }
        .tree
        .node("t10")
        .bounds
        .y
    assertTrue(after < before, "scrolled: $before to $after")
  }

  private fun androidx.compose.ui.test.ComposeUiTest.settleForTest() {
    with(bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessRenderer) { settle() }
  }
}
