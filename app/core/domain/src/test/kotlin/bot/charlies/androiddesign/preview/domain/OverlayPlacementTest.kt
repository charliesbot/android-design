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
}
