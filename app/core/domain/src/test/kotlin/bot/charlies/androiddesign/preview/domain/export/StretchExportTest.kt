package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.body
import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.has
import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.lacks
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `Modifier.fillMaxWidth()` is written where the previewer stretches a component: the catalog says
 * which components stretch and which parents let them (`ComponentDef.width`, `stretchesChildren`).
 */
class StretchExportTest {
  private fun text(id: String, extra: String = "") =
    """{"id":"$id","component":"Text","text":"$id"$extra}"""

  private fun column(vararg children: String) =
    """{"id":"root","component":"Column","children":[${children.joinToString { "\"$it\"" }}]}"""

  private fun stretches(source: String) =
    Regex("""fillMaxWidth\(\)""").findAll(body(ExportFixtures.flat(source))).count()

  @Test
  fun `a column stretches a progress bar and a row`() {
    val source =
      ExportFixtures.export(
        column("bar", "times"),
        """{"id":"bar","component":"ProgressIndicator","wavy":true,"progress":0.4}""",
        """{"id":"times","component":"Row","arrangement":"spaceBetween","children":["a","b"]}""",
        text("a"),
        text("b"),
      )
    has(
      source,
      "LinearWavyProgressIndicator(progress = { 0.4f }, modifier = Modifier.fillMaxWidth())",
    )
    has(
      source,
      "Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween)",
    )
    // The root column, the bar and the row; the texts inside the row stay as wide as their text.
    assertEquals(3, stretches(source))
  }

  @Test
  fun `a row does not stretch its children`() {
    val source =
      ExportFixtures.export(
        column("row"),
        """{"id":"row","component":"Row","children":["slider","field","t"]}""",
        """{"id":"slider","component":"Slider","value":0.5}""",
        """{"id":"field","component":"TextField","label":"Name"}""",
        text("t", ""","align":"center""""),
      )
    // The root column and the row; neither the slider, the field nor the aligned text.
    assertEquals(2, stretches(source))
  }

  @Test
  fun `a max width caps the stretch before it fills`() {
    val source =
      ExportFixtures.export(
        column("card"),
        """{"id":"card","component":"Card","maxWidth":480,"content":["t"]}""",
        text("t"),
      )
    has(source, "Modifier.widthIn(max = 480.dp).fillMaxWidth()")
  }

  @Test
  fun `weight comes first and padding last`() {
    val source =
      ExportFixtures.export(
        column("slider"),
        """{"id":"slider","component":"Slider","value":0.5,"weight":1,"padding":"md"}""",
      )
    has(source, "Modifier.weight(1f).fillMaxWidth().padding(")
  }

  @Test
  fun `a prop decides for text, image, divider and progress`() {
    fun count(vararg components: String) = stretches(ExportFixtures.export(*components))
    // The root column alone is 1.
    assertEquals(1, count(column("t"), text("t")))
    assertEquals(2, count(column("t"), text("t", ""","align":"end"""")))
    assertEquals(
      1,
      count(column("i"), """{"id":"i","component":"Image","src":"missing.png"}"""),
    )
    assertEquals(
      2,
      count(
        column("i"),
        """{"id":"i","component":"Image","src":"missing.png","aspectRatio":1.5}""",
      ),
    )
    assertEquals(2, count(column("d"), """{"id":"d","component":"Divider"}"""))
    assertEquals(1, count(column("d"), """{"id":"d","component":"Divider","vertical":true}"""))
    assertEquals(
      1,
      count(column("p"), """{"id":"p","component":"ProgressIndicator","shape":"circular"}"""),
    )
  }

  @Test
  fun `a bound prop decides by its sample value, as the previewer resolves it`() {
    fun count(data: String, vararg components: String) =
      stretches(ExportFixtures.export(*components, dataModel = data))
    val divider = """{"id":"d","component":"Divider","vertical":{"@path":"/vertical"}}"""
    // The root column alone is 1; a horizontal divider adds its own.
    assertEquals(1, count("""{"vertical":true}""", column("d"), divider))
    assertEquals(2, count("""{"vertical":false}""", column("d"), divider))
    // A binding that resolves to nothing falls back to the catalog default, a horizontal divider.
    assertEquals(2, count("""{}""", column("d"), divider))
    val aligned = """{"id":"t","component":"Text","text":"t","align":{"@path":"/align"}}"""
    assertEquals(2, count("""{"align":"center"}""", column("t"), aligned))
    assertEquals(1, count("""{"align":null}""", column("t"), aligned))
  }

  @Test
  fun `a scaffold stretches its content, but a layout there already fills the screen`() {
    val card =
      ExportFixtures.export(
        """{"id":"root","component":"Scaffold","content":"card"}""",
        """{"id":"card","component":"Card","content":["t"]}""",
        text("t"),
      )
    has(card, "Card(modifier = Modifier.fillMaxWidth())")
    val layout =
      ExportFixtures.export(
        """{"id":"root","component":"Scaffold","content":"col"}""",
        """{"id":"col","component":"Column","children":["t"]}""",
        text("t"),
      )
    has(layout, "Modifier.fillMaxSize().padding(innerPadding)")
    lacks(layout, "fillMaxWidth()")
  }

  @Test
  fun `a column in a list item's trailing slot wraps its row`() {
    val source =
      ExportFixtures.export(
        column("list"),
        """{"id":"list","component":"List","children":["item"]}""",
        """{"id":"item","component":"ListItem","headline":"Tacos","trailing":"slot"}""",
        """{"id":"slot","component":"Column","horizontalAlignment":"end","children":["time","rating"]}""",
        text("time"),
        """{"id":"rating","component":"Row","children":["score"]}""",
        text("score"),
      )
    // The root column, the list and the list item; neither the slot column nor its row.
    assertEquals(3, stretches(source))
  }

  @Test
  fun `a column in a row wraps its children unless it has a weight`() {
    fun source(weight: String) =
      ExportFixtures.export(
        column("line"),
        """{"id":"line","component":"Row","children":["pane"]}""",
        """{"id":"pane","component":"Column"$weight,"children":["label","bar"]}""",
        text("label"),
        """{"id":"bar","component":"Row","children":["inner"]}""",
        text("inner"),
      )
    // The root column and the line row stretch; the pane's own row does not.
    assertEquals(2, stretches(source("")))
    // A weighted pane fills its share, so its row stretches like any child of a filled column.
    assertEquals(3, stretches(source(""","weight":1""")))
  }
}
