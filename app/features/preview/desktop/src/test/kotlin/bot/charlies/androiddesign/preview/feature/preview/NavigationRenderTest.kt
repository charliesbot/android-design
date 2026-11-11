package bot.charlies.androiddesign.preview.feature.preview

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import bot.charlies.androiddesign.preview.domain.layout.LayoutSummary
import bot.charlies.androiddesign.preview.domain.navigation.AppNavigation
import bot.charlies.androiddesign.preview.feature.preview.render.AppNavigationFrame
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalTheme
import bot.charlies.androiddesign.preview.feature.preview.theme.ThemeResolver
import bot.charlies.androiddesign.preview.feature.preview.theme.toHex
import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.Destination
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The navigation of `navigation.destinations` is drawn once around the pane scaffold, outside every
 * pane. A rail used to be a slot of one screen, so it sat inside the 360 dp list pane and left 264
 * dp for the list; these tests keep it on the frame's leading edge, at full height, with the panes
 * beside it at their own widths.
 */
class NavigationRenderTest {
  private fun rows(prefix: String) =
    (0 until 3).joinToString(",") { i ->
      """{"id":"${prefix}_$i","component":"ListItem","headline":"Holding $i",""" +
        """"action":{"functionCall":{"@call":"navigate","args":{"screen":"stock"}}}}"""
    }

  private val portfolio =
    screenJson(
      """{"id":"root","component":"Scaffold","content":"list"},""" +
        """{"id":"list","component":"Column","children":["row_0","row_1","row_2"]},""" +
        rows("row"),
    )

  private val stock =
    screenJson(
      """{"id":"root","component":"Scaffold","content":"stock_text"},""" +
        """{"id":"stock_text","component":"Text","text":"Stock detail"}""",
    )

  private fun plain(name: String) =
    screenJson(
      """{"id":"root","component":"Scaffold","content":"${name}_text"},""" +
        """{"id":"${name}_text","component":"Text","text":"$name"}""",
    )

  private val destinations =
    """[{"screen":"portfolio","label":"Portfolio","icon":"pie_chart","selectedIcon":"pie_chart","badge":3},""" +
      """{"screen":"markets","label":"Markets","icon":"show_chart"},""" +
      """{"screen":"account","label":"Account","icon":"person"}]"""

  private fun app(
    railExpanded: Boolean? = null,
    destinations: String = this.destinations,
    portfolio: String = this.portfolio,
  ): Proposal =
    proposalOf(
      "portfolio" to portfolio,
      "stock" to stock,
      "markets" to plain("markets"),
      "account" to plain("account"),
      panes = """{"portfolio":"list","stock":"detail"}""",
      destinations = destinations,
      railExpanded = railExpanded,
    )

  private fun LayoutTree.bounds(id: String): Bounds = node(id).bounds

  private fun ViewSpec.assertRailBesideTwoPanes() {
    // The stock screen, reached from the portfolio list: both panes are on show.
    val tree = render(app(), "stock", this).tree
    val rail = tree.node("@navigation")
    assertEquals("NavigationRail", rail.type)
    assertEquals(0f, rail.bounds.x, 0.5f, "$name: the rail starts at the frame's left edge")
    assertEquals(0f, rail.bounds.y, 0.5f, "$name: the rail starts at the top")
    assertEquals(height.toFloat(), rail.bounds.height, 0.5f, "$name: the rail spans the height")
    assertTrue(rail.bounds.width in 56f..120f, "$name: collapsed rail ${rail.bounds.width} dp wide")

    val row = tree.bounds("portfolio/row_0")
    assertEquals(360f, row.width, 2f, "$name: the list pane's content is 360 dp, not 264")
    assertTrue(row.x >= rail.bounds.width - 0.5f, "$name: the list pane starts after the rail")
    val detail = tree.bounds("stock_text")
    assertTrue(
      detail.x >= row.x + row.width - 0.5f,
      "$name: the detail pane ($detail) sits beside the list ($row)",
    )
    assertTrue(detail.x + detail.width <= width + 0.5f, "$name: the detail stays in the frame")
  }

  @Test
  fun `a rail is outside the panes at tablet width and the list keeps its 360 dp`() {
    Views.Tablet.assertRailBesideTwoPanes()
  }

  @Test
  fun `a rail is outside the panes at desktop width and the list keeps its 360 dp`() {
    Views.Desktop.assertRailBesideTwoPanes()
  }

  @Test
  fun `a bar sits at the bottom of a compact window across its whole width`() {
    val tree = render(app(), "portfolio", Views.Light).tree
    val bar = tree.node("@navigation")
    assertEquals("NavigationBar", bar.type)
    assertEquals(0f, bar.bounds.x, 0.5f)
    assertEquals(Views.Light.width.toFloat(), bar.bounds.width, 0.5f)
    assertEquals(Views.Light.height.toFloat(), bar.bounds.y + bar.bounds.height, 0.5f)
    val items = tree.nodes.filter { it.type == "NavigationBarItem" }
    assertEquals(listOf("@navigation[0]", "@navigation[1]", "@navigation[2]"), items.map { it.id })
    // The screen is above the bar, not under it.
    assertTrue(tree.bounds("row_0").y + tree.bounds("row_0").height <= bar.bounds.y + 0.5f)
  }

  @Test
  fun `the foldable gets a collapsed rail and no list detail`() {
    val tree = render(app(railExpanded = true), "portfolio", Views.Foldable).tree
    val rail = tree.node("@navigation")
    assertEquals("NavigationRail", rail.type)
    assertTrue(rail.bounds.width in 56f..120f, "railExpanded applies from 840 dp: ${rail.bounds}")
    assertEquals(0f, rail.bounds.x, 0.5f)
  }

  @Test
  fun `railExpanded widens the rail at expanded widths and leaves the default collapsed`() {
    val collapsed = render(app(), "portfolio", Views.Desktop).tree.bounds("@navigation").width
    val stated = render(app(railExpanded = false), "portfolio", Views.Desktop).tree
    val expanded = render(app(railExpanded = true), "portfolio", Views.Desktop).tree
    assertEquals(collapsed, stated.bounds("@navigation").width, 0.5f)
    assertTrue(
      expanded.bounds("@navigation").width > collapsed + 80f,
      "expanded ${expanded.bounds("@navigation").width} dp, collapsed $collapsed dp",
    )
  }

  @Test
  fun `a compact detail covers the window and shows no navigation`() {
    val tree = render(app(), "stock", Views.Light).tree
    assertFalse(tree.has("@navigation"), tree.nodes.map { it.id }.toString())
    assertTrue(tree.nodes.none { it.type == "NavigationBar" || it.type == "NavigationBarItem" })
    assertEquals("none", LayoutSummary.of(app(), tree).nav)
    // The detail is a pane beside a destination on a wider view, so it keeps the rail there.
    assertTrue(render(app(), "stock", Views.Tablet).tree.has("@navigation"))
  }

  @Test
  fun `a screen that covers the window and is no destination shows no navigation at any width`() {
    val flow = screenJson("""{"id":"root","component":"Text","text":"Checkout"}""")
    val proposal =
      proposalOf(
        "portfolio" to portfolio,
        "stock" to stock,
        "markets" to plain("markets"),
        "account" to plain("account"),
        "flow" to flow,
        panes = """{"portfolio":"list","stock":"detail"}""",
        destinations = destinations,
      )
    for (view in listOf(Views.Light, Views.Foldable, Views.Tablet)) {
      assertFalse(render(proposal, "flow", view).tree.has("@navigation"), view.name)
    }
  }

  @Test
  fun `the nav summary names the bar at compact and the rail from medium up`() {
    val proposal = app()
    fun nav(view: ViewSpec) = LayoutSummary.of(proposal, render(proposal, "markets", view).tree).nav
    assertEquals("NavigationBar", nav(Views.Light))
    assertEquals("NavigationRail", nav(Views.Foldable))
    assertEquals("NavigationRail", nav(Views.Tablet))
  }

  private val scale = 2 // the png is two pixels per dp

  /** How many pixels in [region] (dp) are close to [color]. */
  private fun count(png: ByteArray, region: Bounds, color: Int): Int {
    val image = ImageIO.read(ByteArrayInputStream(png))
    var n = 0
    for (x in (region.x * scale).toInt() until ((region.x + region.width) * scale).toInt()) {
      for (y in (region.y * scale).toInt() until ((region.y + region.height) * scale).toInt()) {
        val rgb = image.getRGB(x, y) and 0xFFFFFF
        val close =
          abs((rgb shr 16) - (color shr 16)) +
            abs(((rgb shr 8) and 0xFF) - ((color shr 8) and 0xFF)) +
            abs((rgb and 0xFF) - (color and 0xFF))
        if (close <= 12) n++
      }
    }
    return n
  }

  private fun indicator() =
    ThemeResolver.resolve(designOf(null), dark = false)
      .color(ColorRole.SecondaryContainer)
      .toHex()
      .removePrefix("#")
      .toInt(16)

  @Test
  fun `the selected destination follows the back stack`() {
    val proposal = app()
    fun selected(screen: String, view: ViewSpec): List<Int> {
      val result = render(proposal, screen, view)
      return (0 until 3).filter { index ->
        count(result.png, result.tree.bounds("@navigation[$index]"), indicator()) > 0
      }
    }
    assertEquals(listOf(0), selected("portfolio", Views.Light))
    // The detail's back stack is [portfolio, stock]: the portfolio is the first destination below.
    assertEquals(listOf(0), selected("stock", Views.Tablet))
    assertEquals(listOf(1), selected("markets", Views.Light))
    assertEquals(listOf(2), selected("account", Views.Tablet))
  }

  @Test
  fun `a tap on a destination makes it the whole back stack`() {
    var afterTap: List<String> = emptyList()
    val result =
      render(app(), "stock", Views.Tablet) { host ->
        assertEquals(listOf("portfolio", "stock"), host.backStack.toList())
        onNodeWithTag("@navigation[1]", useUnmergedTree = true).performClick()
        afterTap = host.backStack.toList()
      }
    assertEquals(listOf("markets"), afterTap)
    // The markets screen is on show, with the rail still up and no list detail pane left.
    assertTrue(result.tree.has("markets_text"), result.tree.nodes.map { it.id }.toString())
    assertEquals("markets", result.tree.screen)
    assertFalse(result.tree.has("portfolio/row_0"))
    assertTrue(result.tree.has("@navigation"))
  }

  @Test
  fun `the bar and rail are in the tree with bounds, touch bounds, roles and a Tab role`() {
    for ((view, barType, itemType) in
      listOf(
        Triple(Views.Light, "NavigationBar", "NavigationBarItem"),
        Triple(Views.Tablet, "NavigationRail", "NavigationRailItem"),
      )) {
      val tree = render(app(), "portfolio", view).tree
      val container = tree.node("@navigation")
      assertEquals(barType, container.type)
      assertEquals(null, container.parent)
      assertTrue(container.roles?.container != null && container.colors?.container != null)
      for (index in 0 until 3) {
        val item = tree.node("@navigation[$index]")
        assertEquals(itemType, item.type)
        assertEquals("@navigation", item.parent)
        assertEquals("Tab", item.a11y?.role, "${view.name} item $index")
        assertEquals("labelMedium", item.roles?.type)
        assertEquals(listOf("Portfolio", "Markets", "Account")[index], item.text?.value)
        assertEquals(1, item.text?.lines)
        val touch = checkNotNull(item.touch)
        assertTrue(touch.width >= 48f && touch.height >= 48f, "${view.name}: touch $touch")
        assertTrue(item.colors?.content != null && item.colors?.container != null)
      }
      assertEquals(emptyList(), tree.findings.filter { it.rule == FindingRule.TouchTarget })
    }
  }

  @Test
  fun `a destination label that does not fit is a label-truncated finding`() {
    val long =
      """[{"screen":"portfolio","label":"Portfolio holdings and watchlists","icon":"pie_chart"},""" +
        """{"screen":"markets","label":"Markets","icon":"show_chart"},""" +
        """{"screen":"account","label":"Account","icon":"person"}]"""
    val tree = render(app(destinations = long), "portfolio", Views.Light).tree
    val finding = tree.findings.single { it.rule == FindingRule.LabelTruncated }
    assertEquals("@navigation[0]", finding.id)
  }

  @Test
  fun `a docked toolbar where the navigation bar shows is an error, and not beside a rail`() {
    val docked =
      screenJson(
        """{"id":"root","component":"Scaffold","content":"list","bottomBar":"toolbar"},""" +
          """{"id":"list","component":"Column","children":["row_0"]},""" +
          """{"id":"row_0","component":"ListItem","headline":"Holding 0"},""" +
          """{"id":"toolbar","component":"DockedToolbar","children":["tool"]},""" +
          """{"id":"tool","component":"IconButton","icon":"edit","contentDescription":"Edit"}""",
      )
    val proposal = app(portfolio = docked)
    val compact = render(proposal, "portfolio", Views.Light).tree
    assertEquals(
      listOf("toolbar"),
      compact.findings.filter { it.rule == FindingRule.DockedToolbarNavBar }.map { it.id },
    )
    val wide = render(proposal, "portfolio", Views.Tablet).tree
    assertEquals(emptyList(), wide.findings.filter { it.rule == FindingRule.DockedToolbarNavBar })
  }

  @Test
  fun `a snackbar draws above the bar`() {
    val snackbar =
      screenJson(
        """{"id":"root","component":"Scaffold","content":"markets_text"},""" +
          """{"id":"markets_text","component":"Text","text":"markets"},""" +
          """{"id":"saved","component":"Snackbar","message":"Saved"}""",
      )
    val proposal =
      proposalOf(
        "portfolio" to portfolio,
        "stock" to stock,
        "markets" to snackbar,
        "account" to plain("account"),
        panes = """{"portfolio":"list","stock":"detail"}""",
        destinations = destinations,
      )
    val tree =
      render(proposal, "markets", Views.Light) { it.interaction.open("markets", "saved") }.tree
    val bar = tree.bounds("@navigation")
    val node = tree.bounds("saved")
    assertTrue(node.y + node.height <= bar.y + 1f, "the snackbar $node is above the bar $bar")
  }

  @Test
  fun `a proposal without destinations draws no navigation`() {
    val proposal =
      proposalOf(
        "portfolio" to portfolio,
        "stock" to stock,
        panes = """{"portfolio":"list","stock":"detail"}""",
      )
    for (view in Views.all) {
      val tree = render(proposal, "portfolio", view).tree
      assertTrue(tree.nodes.none { it.id.startsWith("@navigation") }, view.name)
    }
  }

  @Test
  fun `a component id of navigation does not collide with the navigation nodes`() {
    val clash =
      screenJson(
        """{"id":"root","component":"Scaffold","content":"navigation"},""" +
          """{"id":"navigation","component":"Text","text":"My own navigation"}""",
      )
    val tree = render(app(portfolio = clash), "portfolio", Views.Light).tree
    assertEquals("Text", tree.node("navigation").type)
    assertEquals("NavigationBar", tree.node("@navigation").type)
  }

  /**
   * The rail shows on a destination and hides on a full-window screen that is no destination, and
   * the screens are in the same window both times: a screen keeps what it remembers.
   */
  @OptIn(ExperimentalTestApi::class)
  @Test
  fun `the content is not rebuilt when the rail appears or disappears beside it`() =
    runComposeUiTest {
      var shown: AppNavigation.Shown by mutableStateOf(AppNavigation.Shown.Rail(expanded = false))
      var created = 0
      var disposed = 0
      setContent {
        ProposalTheme(designOf(null), dark = false) {
          AppNavigationFrame(
            shown = shown,
            destinations = listOf(Destination("home", "Home", "home")),
            selected = 0,
            onSelect = {},
            recorder = null,
          ) {
            remember { created++ }
            DisposableEffect(Unit) { onDispose { disposed++ } }
          }
        }
      }
      waitForIdle()
      shown = AppNavigation.Shown.Hidden
      waitForIdle()
      assertEquals(1 to 0, created to disposed, "the rail hid")
      shown = AppNavigation.Shown.Rail(expanded = false)
      waitForIdle()
      assertEquals(1 to 0, created to disposed, "the rail came back")
      shown = AppNavigation.Shown.Rail(expanded = true)
      waitForIdle()
      assertEquals(1 to 0, created to disposed, "the rail widened")
    }
}
