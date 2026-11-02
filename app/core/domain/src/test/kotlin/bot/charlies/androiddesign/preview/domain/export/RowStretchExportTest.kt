package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.body
import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.has
import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.lacks
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `verticalAlignment = "stretch"` makes a full-height pane: the row writes no alignment (top is
 * Compose's default) and its Column and Box children get `Modifier.fillMaxHeight()`.
 */
class RowStretchExportTest {
  private fun panes(alignment: String?): String {
    val stated = if (alignment == null) "" else ""","verticalAlignment":"$alignment""""
    return ExportFixtures.export(
      """{"id":"root","component":"Scaffold","content":"row"}""",
      """{"id":"row","component":"Row"$stated,"children":["col","box","img","t"]}""",
      """{"id":"col","component":"Column","weight":1,"children":["t"]}""",
      """{"id":"box","component":"Box","children":["t"]}""",
      """{"id":"img","component":"Image","src":"missing.png"}""",
      """{"id":"t","component":"Text","text":"Hi"}""",
    )
  }

  private fun fillMaxHeights(source: String) =
    Regex("""fillMaxHeight\(\)""").findAll(body(ExportFixtures.flat(source))).count()

  @Test
  fun `stretch fills the height of a column and a box, and writes no alignment`() {
    val source = panes("stretch")
    has(source, "Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {")
    has(source, "Column(modifier = Modifier.weight(1f).fillMaxHeight()) {")
    has(source, "Box(modifier = Modifier.fillMaxHeight()) {")
    lacks(source, "verticalAlignment")
    lacks(source, "Alignment.Top")
    // Neither the image nor the text fills the height.
    assertEquals(2, fillMaxHeights(source))
  }

  @Test
  fun `a row without stretch writes no fill height`() {
    val source = panes(null)
    assertEquals(0, fillMaxHeights(source))
    assertEquals(0, fillMaxHeights(panes("top")))
    has(source, "Column(modifier = Modifier.weight(1f)) {")
  }

  /** The previewer fills a Row offered the height, as it does a Column or a Box. */
  @Test
  fun `a row pane inside a stretched row fills its height too`() {
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"Scaffold","content":"row"}""",
        """{"id":"row","component":"Row","verticalAlignment":"stretch","children":["inner"]}""",
        """{"id":"inner","component":"Row","weight":1,"children":["t"]}""",
        """{"id":"t","component":"Text","text":"Hi"}""",
      )
    has(source, "Row(modifier = Modifier.weight(1f).fillMaxHeight()) {")
  }

  @Test
  fun `the grandchildren of a stretched row do not inherit its fill height`() {
    val source = panes("stretch")
    // The column holds the text, and the box holds it too: still only the two panes fill.
    lacks(source, "Text(\"Hi\", modifier = Modifier.fillMaxHeight())")
  }
}
