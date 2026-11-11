package bot.charlies.androiddesign.preview.domain.export

import kotlin.test.Test

/**
 * Regressions found by compiling the goldens in a real Android project (AndroidX material3
 * 1.5.0-alpha29, foundation-layout 1.13.0-alpha01): each test pins a shape that did not compile.
 */
class CompileFidelityTest {
  private val column = """{"id":"root","component":"Column","children":["x"]}"""

  @Test
  fun `grid fr is a scope member, not an import`() {
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"Grid","columns":2,"children":["t"]}""",
        """{"id":"t","component":"Text","text":"Hi"}""",
      )
    ExportFixtures.has(source, "repeat(2) { column(minmax(0.dp, 1.fr)) }")
    ExportFixtures.lacks(source, "import androidx.compose.foundation.layout.fr")
  }

  @Test
  fun `flexbox is stable and needs no opt-in`() {
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"FlexBox","wrap":true,"children":["t"]}""",
        """{"id":"t","component":"Text","text":"Hi"}""",
      )
    ExportFixtures.has(source, "FlexBox(")
    ExportFixtures.lacks(source, "ExperimentalFlexBoxApi")
  }

  @Test
  fun `list item takes its headline as the trailing content lambda`() {
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"List","children":["a"]}""",
        """{"id":"a","component":"ListItem","headline":"One","supporting":"Sup"}""",
      )
    ExportFixtures.has(source, ") { Text(\"One\") }")
    ExportFixtures.lacks(source, "headlineContent")
  }

  @Test
  fun `icon button width goes through the container size, not a widthOption parameter`() {
    val source =
      ExportFixtures.export(
        column,
        """{"id":"x","component":"IconButton","variant":"filled","icon":"mic","width":"narrow"}""",
      )
    ExportFixtures.has(
      source,
      "Modifier.size(IconButtonDefaults.smallContainerSize(IconButtonWidthOption.Narrow))",
    )
    ExportFixtures.has(
      source,
      "import androidx.compose.material3.IconButtonDefaults.IconButtonWidthOption",
    )
    ExportFixtures.lacks(source, "widthOption =")
  }

  @Test
  fun `chip group arrangement is a function call`() {
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"ChipGroup","children":["a"]}""",
        """{"id":"a","component":"FilterChip","label":"A"}""",
      )
    ExportFixtures.has(source, "horizontalArrangement = FilterChipDefaults.horizontalArrangement()")
  }

  @Test
  fun `sample classes that a screen takes are public so the screen can expose them`() {
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"Text","text":{"@path":"/plate/name"}}""",
        dataModel = """{"plate":{"name":"Soup"}}""",
      )
    ExportFixtures.has(source, "data class Plate(val name: String)")
    ExportFixtures.lacks(source, "private data class")
    ExportFixtures.has(source, "private object MainSample")
  }

  @Test
  fun `a sample class never takes the name of an imported Compose symbol`() {
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"Row","children":{"componentId":"cell","path":"/rows"}}""",
        """{"id":"cell","component":"Text","text":{"@path":"name"}}""",
        dataModel = """{"rows":[{"name":"A"}]}""",
      )
    ExportFixtures.lacks(source, "data class Row(")
    ExportFixtures.has(source, "data class MainRow(val name: String)")
  }

  @Test
  fun `a call too long for its line breaks one argument per line like ktfmt`() {
    val id = "iconToggleOutlinedOffWithAVeryLongNameThatForcesTheWrap"
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"Column","children":["$id"]}""",
        """{"id":"$id","component":"IconButton","variant":"outlined","icon":"star","toggle":true,"checked":true}""",
      )
    val long = source.lines().filter { it.length > 100 && !it.trimStart().startsWith("//") }
    kotlin.test.assertEquals(emptyList(), long)
    kotlin.test.assertTrue(
      source.lines().any { it.trimStart() == "else R.drawable.star," },
      "the else branch should start its own line:\n$source",
    )
  }
}
