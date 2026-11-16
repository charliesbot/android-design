package bot.charlies.androiddesign.preview.domain.layout

import bot.charlies.androiddesign.preview.domain.findings.node
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PhoneSurface
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.Views
import kotlin.test.Test
import kotlin.test.assertEquals

class LayoutSummaryTest {
  private fun tree(screen: String, view: String, vararg nodes: LayoutNode) =
    LayoutTree(screen, Views.byName(view)!!, nodes.toList(), emptyList())

  private val phoneDetail =
    tree(
      "plate_detail",
      "light",
      node("root", "Scaffold", w = 411f),
      node("bar", "TopAppBar", w = 411f, parent = "root"),
      node("body", "Column", w = 411f, parent = "root"),
      node("title", "Text", w = 411f, parent = "body"),
      node("cook", "Button", w = 160f, parent = "body"),
    )

  private val tabletDetail =
    tree(
      "plate_detail",
      "tablet",
      node("@pane/plates/root", "Scaffold", x = 0f, w = 412f),
      node("@pane/plates/body", "List", x = 0f, w = 412f, parent = "@pane/plates/root"),
      node("@pane/plates/row[0]", "ListItem", x = 0f, w = 412f, parent = "@pane/plates/body"),
      node("@pane/plates/row[1]", "ListItem", x = 0f, w = 412f, parent = "@pane/plates/body"),
      node("@pane/plate_detail/root", "Scaffold", x = 412f, w = 868f),
      node(
        "@pane/plate_detail/bar",
        "TopAppBar",
        x = 412f,
        w = 868f,
        parent = "@pane/plate_detail/root",
      ),
      node(
        "@pane/plate_detail/body",
        "Column",
        x = 412f,
        w = 867.6f,
        parent = "@pane/plate_detail/root",
      ),
      node(
        "@pane/plate_detail/title",
        "Text",
        x = 412f,
        w = 867.6f,
        parent = "@pane/plate_detail/body",
      ),
      node(
        "@pane/plate_detail/cook",
        "Button",
        x = 412f,
        w = 160f,
        parent = "@pane/plate_detail/body",
      ),
    )

  @Test
  fun `a phone shows the one screen and a tablet shows the list beside it`() {
    val phone = LayoutSummary.of(phoneDetail, PhoneSurface)
    val tablet = LayoutSummary.of(tabletDetail, PhoneSurface)

    assertEquals(listOf("plate_detail"), phone.panes.keys.toList())
    assertEquals(listOf("Text title 411", "Button cook 160"), phone.panes.getValue("plate_detail"))
    assertEquals(listOf("plates", "plate_detail"), tablet.panes.keys.toList(), "left to right")
    assertEquals(
      listOf("ListItem row[0] 412", "ListItem row[1] 412"),
      tablet.panes.getValue("plates"),
      "the pane prefix of another pane's ids is dropped",
    )
    assertEquals(
      listOf("Text title 868", "Button cook 160"),
      tablet.panes.getValue("plate_detail"),
      "widths are whole dp",
    )
  }

  @Test
  fun `a bar becomes a rail and no navigation is none`() {
    val bar = phoneDetail.copy(nodes = phoneDetail.nodes + node("nav", "NavigationBar"))
    val rail = tabletDetail.copy(nodes = tabletDetail.nodes + node("nav", "NavigationRail"))
    val both = rail.copy(nodes = rail.nodes + node("tools", "FloatingToolbar"))

    assertEquals("NavigationBar", LayoutSummary.of(bar, PhoneSurface).nav)
    assertEquals("NavigationRail", LayoutSummary.of(rail, PhoneSurface).nav)
    assertEquals("NavigationRail+FloatingToolbar", LayoutSummary.of(both, PhoneSurface).nav)
    assertEquals("none", LayoutSummary.of(phoneDetail, PhoneSurface).nav)
  }

  @Test
  fun `a long list of regions is cut with a count`() {
    val items = (0 until 11).map { node("c$it", "Card", w = 100f, parent = "body") }
    val crowded =
      tree(
        "plate_detail",
        "light",
        node("root", "Scaffold"),
        node("body", "Column", parent = "root"),
      )
    val summary = LayoutSummary.of(crowded.copy(nodes = crowded.nodes + items), PhoneSurface)

    val regions = summary.panes.getValue("plate_detail")
    assertEquals(LayoutSummary.MAX_REGIONS + 1, regions.size)
    assertEquals("Card c0 100", regions.first())
    assertEquals("+3 more", regions.last())
  }

  @Test
  fun `a child that was not laid out is not a region`() {
    val unseen = node("below", "Grid", w = 0f, h = 0f, parent = "body")
    val summary =
      LayoutSummary.of(phoneDetail.copy(nodes = phoneDetail.nodes + unseen), PhoneSurface)

    assertEquals(
      listOf("Text title 411", "Button cook 160"),
      summary.panes.getValue("plate_detail"),
    )
  }

  @Test
  fun `a root that is not a scaffold is its own content root`() {
    val tree =
      tree(
        "plate_detail",
        "light",
        node("root", "Column", w = 411f),
        node("title", "Text", w = 300f, parent = "root"),
      )

    assertEquals(
      listOf("Text title 300"),
      LayoutSummary.of(tree, PhoneSurface).panes.getValue("plate_detail"),
    )
  }

  @Test
  fun `a view stays small as json`() {
    val json =
      PreviewJson.compact.encodeToString(
        ViewLayout.serializer(),
        LayoutSummary.of(tabletDetail, PhoneSurface),
      )
    assertEquals(
      """{"nav":"none","panes":{"plates":["ListItem row[0] 412","ListItem row[1] 412"],"plate_detail":["Text title 868","Button cook 160"]}}""",
      json,
    )
  }
}
