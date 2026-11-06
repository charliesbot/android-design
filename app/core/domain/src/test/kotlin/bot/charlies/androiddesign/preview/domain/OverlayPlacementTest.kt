package bot.charlies.androiddesign.preview.domain

import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.Screen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class OverlayPlacementTest {
  private fun node(id: String, component: String, vararg props: Pair<String, String>) =
    ComponentNode(
      id,
      component,
      buildJsonObject { props.forEach { (key, value) -> put(key, JsonPrimitive(value)) } },
    )

  private fun placement(vararg nodes: ComponentNode) =
    OverlayPlacement(Screen(M3ExpressiveCatalog.ID, components = nodes.toList()))

  @Test
  fun `an anchored menu opens beside its anchor and not at screen level`() {
    val placement =
      placement(node("more", "IconButton"), node("more_menu", "Menu", "anchor" to "more"))

    assertEquals("more_menu", placement.menuAt("more"))
    assertNull(placement.menuAt("other"))
    assertEquals(emptyList(), placement.screenLevel.map { it.id })
  }

  @Test
  fun `a split button menu draws beside its button and not at screen level`() {
    val placement =
      placement(node("split", "SplitButton", "menu" to "split_menu"), node("split_menu", "Menu"))

    assertNull(placement.menuAt("split"))
    assertEquals(emptyList(), placement.screenLevel.map { it.id })
  }

  @Test
  fun `sheets, dialogs and unattached menus draw at screen level in document order`() {
    val placement =
      placement(
        node("dialog", "Dialog"),
        node("loose_menu", "Menu"),
        node("sheet", "BottomSheet"),
        node("button", "Button"),
      )

    assertEquals(listOf("dialog", "loose_menu", "sheet"), placement.screenLevel.map { it.id })
  }

  @Test
  fun `a second menu on the same anchor goes to screen level`() {
    val placement =
      placement(
        node("first", "Menu", "anchor" to "more"),
        node("second", "Menu", "anchor" to "more"),
      )

    assertEquals("first", placement.menuAt("more"))
    assertEquals(listOf("second"), placement.screenLevel.map { it.id })
  }

  @Test
  fun `snackbars draw in the root scaffold's snackbar slot and not at screen level`() {
    val placement =
      placement(
        node("root", "Scaffold"),
        node("saved", "Snackbar"),
        node("undo", "Snackbar"),
        node("sheet", "SideSheet"),
      )

    assertEquals("root", placement.snackbarScaffold)
    assertEquals(listOf("saved", "undo"), placement.snackbars.map { it.id })
    assertEquals(listOf("sheet"), placement.screenLevel.map { it.id })
  }

  @Test
  fun `without a root scaffold the first scaffold holds the snackbars`() {
    val placement =
      placement(node("root", "Box"), node("inner", "Scaffold"), node("saved", "Snackbar"))

    assertEquals("inner", placement.snackbarScaffold)
    assertEquals(listOf("saved"), placement.snackbars.map { it.id })
  }

  @Test
  fun `a screen with no scaffold has nowhere to draw a snackbar`() {
    val placement = placement(node("root", "Column"), node("saved", "Snackbar"))

    assertNull(placement.snackbarScaffold)
    assertEquals(emptyList(), placement.snackbars.map { it.id })
    assertEquals(listOf("saved"), placement.screenLevel.map { it.id })
  }

  @Test
  fun `a tooltip hangs off its anchor and not at screen level`() {
    val placement =
      placement(node("info", "IconButton"), node("tip", "Tooltip", "anchor" to "info"))

    assertEquals("tip", placement.tooltipAt("info"))
    assertNull(placement.tooltipAt("other"))
    assertEquals(emptyList(), placement.screenLevel.map { it.id })
  }

  @Test
  fun `plain tooltips open above and below an app bar, rich ones open below`() {
    val placement =
      placement(
        node("bar", "TopAppBar", "navigationIcon" to "save"),
        node("save", "IconButton"),
        node("plain_in_bar", "Tooltip", "anchor" to "save"),
        node("fab", "FloatingActionButton"),
        node("plain", "Tooltip", "anchor" to "fab"),
        node("rich", "Tooltip", "anchor" to "fab", "variant" to "rich"),
      )

    assertEquals(TooltipSpec.Side.Below, placement.tooltipSide("plain_in_bar", rich = false))
    assertEquals(TooltipSpec.Side.Above, placement.tooltipSide("plain", rich = false))
    assertEquals(TooltipSpec.Side.Below, placement.tooltipSide("rich", rich = true))
    // The second tooltip on one anchor has no place beside it.
    assertEquals(listOf("rich"), placement.screenLevel.map { it.id })
  }

  @Test
  fun `a search view opens beside the bar that names it`() {
    val placement =
      placement(
        node("bar", "SearchBar", "expanded" to "results"),
        node("results", "SearchView"),
        node("app", "TopAppBar", "size" to "search", "expanded" to "app_results"),
        node("app_results", "SearchView"),
        node("loose", "SearchView"),
      )

    assertEquals("results", placement.expandedViewOf("bar"))
    assertEquals("app_results", placement.expandedViewOf("app"))
    assertEquals("bar", placement.barOf("results"))
    assertNull(placement.barOf("loose"))
    assertEquals(listOf("loose"), placement.screenLevel.map { it.id })
  }
}
