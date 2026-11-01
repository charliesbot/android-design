package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.has
import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.lacks
import kotlin.test.Test
import kotlin.test.assertTrue

/** `showIn` and `sizeClass` become conditions on the window width class. */
class ResponsiveExportTest {
  private fun flat(text: String) =
    text.replace(Regex("\\s+"), " ").replace(", )", ")").replace("( ", "(").replace(" .", ".")

  private fun export(vararg children: String): String =
    flat(
      ExportFixtures.export(
        """{"id":"root","component":"Column","children":[${(1..children.size).joinToString(",") { "\"c$it\"" }}]}""",
        *children.mapIndexed { i, json -> json.replace("ID", "c${i + 1}") }.toTypedArray(),
      ),
    )

  @Test
  fun `showIn on every size class adds no condition`() {
    val source =
      export(
        """{"id":"ID","component":"Text","text":"A","showIn":["compact","medium","expanded"]}""",
      )
    lacks(source, "atLeastMedium")
    lacks(source, "windowSizeClass")
  }

  @Test
  fun `the window width class is read once and only when used`() {
    val source =
      export(
        """{"id":"ID","component":"Text","text":"A","showIn":["compact"]}""",
        """{"id":"ID","component":"Text","text":"B","showIn":["expanded"]}""",
      )
    has(source, "val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass")
    has(
      source,
      "val atLeastMedium = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)",
    )
    has(
      source,
      "val atLeastExpanded = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)",
    )
    has(source, "ExperimentalMaterial3AdaptiveApi::class")
    assertTrue(source.indexOf("val windowSizeClass") < source.indexOf("Column("))
    assertTrue(source.windowCount() == 1)
  }

  private fun String.windowCount() = Regex("val windowSizeClass").findAll(this).count()

  @Test
  fun `each combination of size classes has its own condition`() {
    val cases =
      mapOf(
        """["compact"]""" to "if (!atLeastMedium) {",
        """["medium"]""" to "if (atLeastMedium && !atLeastExpanded) {",
        """["expanded"]""" to "if (atLeastExpanded) {",
        """["compact","medium"]""" to "if (!atLeastExpanded) {",
        """["medium","expanded"]""" to "if (atLeastMedium) {",
        """["compact","expanded"]""" to "if (!atLeastMedium || atLeastExpanded) {",
      )
    cases.forEach { (showIn, condition) ->
      val source = export("""{"id":"ID","component":"Text","text":"A","showIn":$showIn}""")
      assertTrue(source.contains(condition), "$showIn should give $condition in $source")
    }
  }

  @Test
  fun `a component shown nowhere is left out`() {
    val source = export("""{"id":"ID","component":"Text","text":"A","showIn":[]}""")
    lacks(source, "Text(\"A\")")
  }

  @Test
  fun `size class overrides become a chain of conditions`() {
    val source =
      export(
        """{"id":"ID","component":"Text","text":"A","type":"bodyLarge","sizeClass":{"expanded":{"type":"headlineSmall","text":"Wide"}}}""",
      )
    assertTrue(
      source.contains(
        "if (!atLeastExpanded) { Text(\"A\", style = MaterialTheme.typography.bodyLarge) } " +
          "else if (atLeastExpanded) { Text(\"Wide\", style = MaterialTheme.typography.headlineSmall) }",
      ) ||
        source.contains("if (!atLeastExpanded) {") &&
          source.contains("} else if (atLeastExpanded) {"),
      source,
    )
    has(source, "Text(\"Wide\", style = MaterialTheme.typography.headlineSmall)")
  }

  @Test
  fun `a size class that does not change the component shares its branch`() {
    val source =
      export(
        """{"id":"ID","component":"Text","text":"A","sizeClass":{"medium":{"text":"M"},"expanded":{"text":"A"}}}""",
      )
    assertTrue(source.contains("if (!atLeastMedium || atLeastExpanded) { Text(\"A\") }"), source)
    assertTrue(
      source.contains("else if (atLeastMedium && !atLeastExpanded) { Text(\"M\") }"),
      source,
    )
  }

  @Test
  fun `a medium override carries on to expanded like it does in the renderer`() {
    val source =
      export(
        """{"id":"ID","component":"Text","text":"A","sizeClass":{"medium":{"text":"M"}}}""",
      )
    assertTrue(source.contains("if (!atLeastMedium) { Text(\"A\") }"), source)
    assertTrue(source.contains("else if (atLeastMedium) { Text(\"M\") }"), source)
    lacks(source, "!atLeastExpanded")
  }

  @Test
  fun `an expanded override wins over a medium one at expanded`() {
    val source =
      export(
        """{"id":"ID","component":"Text","text":"A","sizeClass":{"medium":{"text":"M"},"expanded":{"text":"E"}}}""",
      )
    assertTrue(source.contains("if (!atLeastMedium) { Text(\"A\") }"), source)
    assertTrue(
      source.contains("else if (atLeastMedium && !atLeastExpanded) { Text(\"M\") }"),
      source,
    )
    assertTrue(source.contains("else if (atLeastExpanded) { Text(\"E\") }"), source)
  }
}
