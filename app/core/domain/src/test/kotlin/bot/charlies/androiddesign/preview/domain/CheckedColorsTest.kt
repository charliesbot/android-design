package bot.charlies.androiddesign.preview.domain

import bot.charlies.androiddesign.preview.domain.findings.node
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.NodeColors
import bot.charlies.androiddesign.preview.model.NodeRoles
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CheckedColorsTest {
  private fun drawn(id: String, container: Pair<String, String>?, content: Pair<String, String>?) =
    node(id, "Card")
      .copy(
        roles = NodeRoles(container = container?.first, content = content?.first),
        colors = NodeColors(container = container?.second, content = content?.second),
      )

  private fun tree(view: ViewSpec, vararg nodes: LayoutNode) =
    LayoutTree(screen = "home", view = view, nodes = nodes.toList(), findings = emptyList())

  @Test
  fun `only the roles nodes draw with, container and content, sorted by role name`() {
    val colors = CheckedColors()
    colors.add(
      tree(
        Views.Light,
        drawn("a", "surface" to "#F5FBF2", "onSurface" to "#171D18"),
        drawn("b", "primaryContainer" to "#8FF8A3", "onPrimaryContainer" to "#00210A"),
        drawn("c", "surface" to "#F5FBF2", null),
        node("plain", "Text"),
      ),
    )

    val result = colors.toMap()

    assertEquals(listOf("light"), result.keys.toList())
    assertEquals(
      listOf("onPrimaryContainer", "onSurface", "primaryContainer", "surface"),
      result.getValue("light").keys.toList(),
    )
    assertEquals("#8FF8A3", result.getValue("light").getValue("primaryContainer"))
  }

  @Test
  fun `each brightness checked gets its own colors, light first`() {
    val colors = CheckedColors()
    colors.add(tree(Views.Dark, drawn("a", "surface" to "#0F1510", null)))
    colors.add(tree(Views.Light, drawn("a", "surface" to "#F5FBF2", null)))
    colors.add(tree(Views.Tablet, drawn("a", "surface" to "#F5FBF2", null)))

    assertEquals(
      mapOf(
        "light" to mapOf("surface" to "#F5FBF2"),
        "dark" to mapOf("surface" to "#0F1510"),
      ),
      colors.toMap(),
    )
    assertEquals(listOf("light", "dark"), colors.toMap().keys.toList())
  }

  @Test
  fun `only the brightness rendered appears, and a Wear view is dark`() {
    val light =
      CheckedColors().apply { add(tree(Views.Foldable, drawn("a", "surface" to "#111111", null))) }
    assertEquals(listOf("light"), light.toMap().keys.toList())

    val wear =
      CheckedColors().apply { add(tree(Views.Watch, drawn("a", "background" to "#000000", null))) }
    assertEquals(mapOf("dark" to mapOf("background" to "#000000")), wear.toMap())
  }

  @Test
  fun `nothing drawn means no colors`() {
    assertEquals(emptyMap(), CheckedColors().toMap())
    assertEquals(
      emptyMap(),
      CheckedColors().apply { add(tree(Views.Light, node("plain", "Text"))) }.toMap(),
    )
  }

  @Test
  fun `a role recorded as two colors at one brightness is an error, not a silent pick`() {
    val colors = CheckedColors()
    colors.add(tree(Views.Light, drawn("a", "surface" to "#F5FBF2", null)))

    val error =
      assertFailsWith<IllegalStateException> {
        colors.add(tree(Views.Tablet, drawn("b", "surface" to "#FFFFFF", null)))
      }
    assertEquals(true, "surface" in error.message.orEmpty())
  }
}
