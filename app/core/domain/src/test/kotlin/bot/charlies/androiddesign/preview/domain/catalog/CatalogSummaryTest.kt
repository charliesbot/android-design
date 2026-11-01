package bot.charlies.androiddesign.preview.domain.catalog

import bot.charlies.androiddesign.preview.domain.PreviewException
import bot.charlies.androiddesign.preview.model.ActionFunction
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.ShapeToken
import bot.charlies.androiddesign.preview.model.TypeRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.serialization.json.jsonObject

class CatalogSummaryTest {
  private val summary = CatalogSummary.render()

  private fun line(component: String) =
    summary.lines().first { it.startsWith("$component: ") || it.startsWith("$component [") }

  @Test
  fun `the summary stays well under the MCP result limit`() {
    assertTrue(summary.length < 20_000, "summary is ${summary.length} characters")
  }

  @Test
  fun `every component has a line with its purpose and every prop`() {
    for (def in M3ExpressiveCatalog.components) {
      val line = line(def.name)
      assertTrue(def.description.trim().trimEnd('.') in line, "${def.name} purpose")
      for (prop in def.props) {
        assertTrue(
          Regex("(^|[ ,])${prop.name}\\*?: ").containsMatchIn(line),
          "${def.name}.${prop.name}",
        )
      }
    }
  }

  @Test
  fun `a prop is name and kind, with enum values inline, slots marked and required starred`() {
    val button = line("Button")
    assertTrue("variant: filled|tonal|elevated|outlined|text" in button, button)
    assertTrue("label*: string" in button, button)
    assertTrue("action: action" in button, button)
    val scaffold = line("Scaffold")
    assertTrue("topBar: child" in scaffold, scaffold)
    assertTrue("content*: child" in scaffold || "content: child" in scaffold, scaffold)
    assertTrue("children*: childList" in line("Column") || "children: childList" in line("Column"))
    assertTrue("[overlay]" in line("Dialog"))
  }

  @Test
  fun `a prop with a catalog default shows it after the kind`() {
    assertTrue("variant: filled|tonal|elevated|outlined|text = filled" in line("Button"))
    assertTrue("size: extra-small|small|medium|large|extra-large = small" in line("Button"))
    assertTrue("enabled: boolean = true" in line("Button"))
    assertTrue("size: number = 24" in line("Icon"))
    assertTrue("label*: string," in line("Button"))
    for (def in M3ExpressiveCatalog.components) {
      for (prop in def.props.filter { it.default != null }) {
        val shown =
          Regex("(^|[ ,])${prop.name}: [^,]* = ${Regex.escape(prop.default!!.content)}(,|$)")
        assertTrue(shown.containsMatchIn(line(def.name)), "${def.name}.${prop.name}")
      }
    }
  }

  @Test
  fun `a restricted slot lists the components it accepts`() {
    M3ExpressiveCatalog.components.forEach { def ->
      def.props.forEach { prop ->
        prop.allowedChildren?.let { children ->
          val allowed = children.sorted().joinToString("|")
          val line = line(def.name)
          assertTrue(
            "${prop.name}" in line && "($allowed)" in line,
            "${def.name}.${prop.name}: $line",
          )
        }
      }
    }
  }

  @Test
  fun `token vocabularies appear once`() {
    for ((label, names) in
      mapOf(
        "colorRole" to ColorRole.entries.map { it.wire },
        "typeRole" to TypeRole.entries.map { it.wire },
        "shape" to ShapeToken.entries.map { it.wire },
      )) {
      val vocabulary = summary.lines().filter { it.startsWith("$label: ") }
      assertEquals(1, vocabulary.size, label)
      assertTrue(names.all { it in vocabulary.single().split(" ", ",", "(", ")") }, label)
    }
  }

  @Test
  fun `the action functions are listed`() {
    for (function in ActionFunction.entries) {
      assertTrue(summary.lines().any { it.startsWith("${function.wire}(") }, function.wire)
    }
  }

  @Test
  fun `components returns the generated catalog entries in the order asked`() {
    val entries = CatalogJson.components(listOf("Slider", "Button"))
    assertEquals(listOf("Slider", "Button"), entries.keys.toList())
    val catalog = CatalogJson.catalog().getValue("components").jsonObject
    assertEquals(catalog.getValue("Slider"), entries.getValue("Slider"))
    assertEquals(catalog.getValue("Button"), entries.getValue("Button"))
  }

  @Test
  fun `an unknown component names the known ones`() {
    val error = assertFailsWith<PreviewException> { CatalogJson.components(listOf("Buton")) }
    assertTrue("'Buton'" in error.message.orEmpty() && "Button" in error.message.orEmpty())
  }
}
