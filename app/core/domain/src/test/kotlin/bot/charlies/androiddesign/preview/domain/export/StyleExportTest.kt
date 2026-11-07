package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.has
import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.lacks
import bot.charlies.androiddesign.preview.model.MaterialOverride
import bot.charlies.androiddesign.preview.model.StyleDef
import bot.charlies.androiddesign.preview.model.ThemeOverride
import kotlin.test.Test
import kotlin.test.assertTrue

/** Named styles and tokens are plain parameters and modifiers, never the Styles API. */
class StyleExportTest {
  private fun flat(text: String) =
    text.replace(Regex("\\s+"), " ").replace(", )", ")").replace("( ", "(").replace(" .", ".")

  private val design =
    DesignSystemResolver.resolve(
      ThemeOverride(
        spacing = mapOf("tiny" to "3px", "huge" to "72px"),
        material =
          MaterialOverride(
            styles =
              mapOf(
                "hero" to
                  StyleDef(
                    type = "displaySmall",
                    color = "primary",
                    container = "surfaceContainerHigh",
                    shape = "large",
                    padding = "huge",
                  ),
              ),
          ),
      ),
      null,
    )

  private fun export(vararg components: String) =
    flat(ExportFixtures.export(*components, design = design))

  @Test
  fun `a style fills in type and color`() {
    val source =
      export(
        """{"id":"root","component":"Column","children":["t"]}""",
        """{"id":"t","component":"Text","style":"hero","text":"Hi"}""",
      )
    assertTrue(
      source.contains(
        "Text(\"Hi\", modifier = Modifier.clip(MaterialTheme.shapes.large).background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(72.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.displaySmall)",
      ),
      source,
    )
  }

  @Test
  fun `explicit props win over the style`() {
    val source =
      export(
        """{"id":"root","component":"Column","children":["t"]}""",
        """{"id":"t","component":"Text","style":"hero","text":"Hi","type":"titleSmall","color":"error","padding":"tiny"}""",
      )
    has(source, "color = MaterialTheme.colorScheme.error")
    has(source, "style = MaterialTheme.typography.titleSmall")
    has(source, ".padding(3.dp)")
    lacks(source, "style = MaterialTheme.typography.displaySmall")
  }

  @Test
  fun `a style container is the background of a layout and the colors of a card`() {
    val source =
      export(
        """{"id":"root","component":"Column","style":"hero","children":["card"]}""",
        """{"id":"card","component":"Card","style":"hero","content":[]}""",
      )
    // The layout is clipped to the style's shape, as the previewer draws it.
    assertTrue(
      source.contains(
        "modifier = modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(72.dp)",
      ),
      source,
    )
    assertTrue(
      source.contains(
        "colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)",
      ),
      source,
    )
    has(source, "shape = MaterialTheme.shapes.large")
    has(source, "import androidx.compose.ui.draw.clip")
    has(source, "import androidx.compose.foundation.background")
  }

  @Test
  fun `a styled text carries its container behind its padding`() {
    val source =
      export(
        """{"id":"root","component":"Column","children":["t"]}""",
        """{"id":"t","component":"Text","style":"hero","text":"Hi"}""",
      )
    has(
      source,
      "Modifier.clip(MaterialTheme.shapes.large).background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(72.dp)",
    )
  }

  @Test
  fun `shape tokens map to the scale and to MaterialShapes`() {
    val source =
      export(
        """{"id":"root","component":"Column","children":["a","b","c","d"]}""",
        """{"id":"a","component":"Image","src":"a.png","shape":"none"}""",
        """{"id":"b","component":"Image","src":"b.png","shape":"full"}""",
        """{"id":"c","component":"Image","src":"c.png","shape":"extraLargeIncreased"}""",
        """{"id":"d","component":"Image","src":"d.png","shape":"sunny"}""",
      )
    has(source, "clip(RectangleShape)")
    has(source, "clip(CircleShape)")
    has(source, "clip(MaterialTheme.shapes.extraLargeIncreased)")
    has(source, "clip(MaterialShapes.Sunny.toShape())")
  }

  @Test
  fun `the Styles API is never used`() {
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"Text","style":"hero","text":"Hi"}""",
        design = design,
      )
    lacks(source, "foundation.style")
    lacks(source, "Style {")
    lacks(source, "MutableStyleState")
  }
}
