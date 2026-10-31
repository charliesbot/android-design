package bot.charlies.androiddesign.preview.domain.export

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Properties every export has: deterministic, 2-space, 100 columns, no em-dashes. */
class ExportShapeTest {
  private fun exports(): List<Pair<String, String>> {
    val (proposal, design) = ExportFixtures.plates()
    return listOf("plates" to KotlinExporter.export(proposal, design)) +
      componentCases.mapIndexed { i, case ->
        "case $i ${case.covers}" to
          KotlinExporter.export(
            ExportFixtures.proposal(*case.components.toTypedArray()),
            ExportFixtures.defaultDesign(),
          )
      }
  }

  @Test
  fun `the same input gives the same output`() {
    val (proposal, design) = ExportFixtures.plates()
    assertEquals(KotlinExporter.export(proposal, design), KotlinExporter.export(proposal, design))
    assertEquals(
      KotlinExporter.export(proposal, design, "plates"),
      KotlinExporter.export(proposal, design, "plates"),
    )
  }

  @Test
  fun `output is indented by two spaces and fits 100 columns`() {
    exports().forEach { (name, source) ->
      source.lines().forEachIndexed { i, line ->
        val code = !line.trimStart().startsWith("//")
        assertTrue(!line.contains('\t'), "$name line ${i + 1} has a tab")
        if (code) {
          val indent = line.length - line.trimStart().length
          assertTrue(indent % 2 == 0, "$name line ${i + 1} is indented by $indent: $line")
          assertTrue(
            line.length <= 100 || line.contains('"'),
            "$name line ${i + 1} is ${line.length} wide: $line",
          )
        }
      }
    }
  }

  @Test
  fun `output has no em-dashes and ends with one newline`() {
    exports().forEach { (name, source) ->
      assertTrue(!source.contains('\u2014'), "$name has an em-dash")
      assertTrue(source.endsWith("}\n") && !source.endsWith("\n\n"), name)
    }
  }

  @Test
  fun `braces and parentheses balance`() {
    exports().forEach { (name, source) ->
      val code = source.lines().filter { !it.trimStart().startsWith("//") }.joinToString("\n")
      val stripped = code.replace(Regex("\"(\\\\.|[^\"\\\\])*\""), "\"\"")
      listOf('{' to '}', '(' to ')').forEach { (open, close) ->
        assertEquals(
          stripped.count { it == open },
          stripped.count { it == close },
          "$name $open$close",
        )
      }
    }
  }

  @Test
  fun `a file has one package and imports in order`() {
    exports().forEach { (name, source) ->
      val imports = source.lines().filter { it.startsWith("import ") }
      assertEquals(imports.sorted(), imports, name)
      assertEquals(imports.distinct(), imports, name)
      assertEquals(1, source.lines().count { it.startsWith("package ") }, name)
    }
  }
}
