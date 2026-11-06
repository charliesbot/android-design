package bot.charlies.androiddesign.preview.domain.export.wear

import bot.charlies.androiddesign.preview.domain.export.wear.WearExportFixtures.has
import bot.charlies.androiddesign.preview.domain.export.wear.WearExportFixtures.root
import kotlin.test.Test

/** Data model bindings become Kotlin values, and templates become loops, as in the phone export. */
class WearBindingExportTest {
  private val data = """{"title":"Plates","count":3,"ratio":0.4,"open":true}"""

  private val plates = """{"plates":[{"name":"Soup","done":true},{"name":"Salad","done":false}]}"""

  private val templateRoot =
    """{"id":"root","component":"Screen","children":{"componentId":"row","path":"/plates"}}"""

  private fun template(itemComponent: String) =
    WearExportFixtures.export(templateRoot, itemComponent, dataModel = plates)

  @Test
  fun `a bound string is the sample value and the data model becomes parameters`() {
    val source =
      WearExportFixtures.export(
        root("t"),
        """{"id":"t","component":"Text","text":{"@path":"/title"}}""",
        dataModel = data,
      )
    has(source, "fun MainScreen(modifier: Modifier = Modifier, title: String = MainSample.title,")
    has(source, "count: Int = MainSample.count")
    has(source, "private object MainSample {")
    has(source, "val title = \"Plates\"")
    has(source, "Text(title, modifier = Modifier.transformedHeight(this, transformationSpec))")
  }

  @Test
  fun `a bound number shown as text is converted and a bound flag is passed through`() {
    val source =
      WearExportFixtures.export(
        root("a", "b", "c", props = """"scroll":false"""),
        """{"id":"a","component":"Text","text":{"@path":"/count"}}""",
        """{"id":"b","component":"SwitchButton","label":"Open","checked":{"@path":"/open"},"enabled":{"@path":"/open"}}""",
        """{"id":"c","component":"CircularProgressIndicator","progress":{"@path":"/ratio"}}""",
        dataModel = data,
      )
    has(source, "Text(count.toString())")
    has(source, "var bChecked by remember { mutableStateOf(open) }")
    has(source, "enabled = open")
    has(source, "progress = { ratio.toFloat() }")
  }

  @Test
  fun `a template in a scrolling screen is one items call`() {
    val source =
      template(
        """{"id":"row","component":"Button","label":{"@path":"name"}}""",
      )
    has(
      source,
      "items(plates) { plate -> Button(onClick = {}, modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec), transformation = SurfaceTransformation(transformationSpec)) { Text(plate.name) } }",
    )
    has(source, "import androidx.wear.compose.foundation.lazy.items")
    has(source, "data class Plate(val name: String, val done: Boolean)")
    has(source, "plates: List<Plate> = MainSample.plates")
  }

  @Test
  fun `a template that shows its index in a scrolling screen is itemsIndexed`() {
    val source =
      template(
        """{"id":"row","component":"Button","label":{"@call":"@index"}}""",
      )
    has(source, "itemsIndexed(plates) { index, plate ->")
    has(source, "Text(index.toString())")
    has(source, "import androidx.wear.compose.foundation.lazy.itemsIndexed")
  }

  @Test
  fun `a template in a column is a forEach inside it`() {
    val source =
      WearExportFixtures.export(
        root("col"),
        """{"id":"col","component":"Column","children":{"componentId":"row","path":"/plates"}}""",
        """{"id":"row","component":"Text","text":{"@path":"name"}}""",
        dataModel = plates,
      )
    has(
      source,
      "item { Column(modifier = Modifier.fillMaxWidth()) { plates.forEach { plate -> Text(plate.name) } } }",
    )
  }

  @Test
  fun `a bound image source goes through the image lookup`() {
    val source =
      WearExportFixtures.export(
        root("i", props = """"scroll":false"""),
        """{"id":"i","component":"Image","src":{"@path":"/pic"}}""",
        dataModel = """{"pic":"art/pic.png"}""",
      )
    has(source, "painterResource(imageDrawable(pic))")
    has(source, "private fun imageDrawable(src: String): Int =")
    has(source, "\"art/pic.png\" -> R.drawable.pic")
    has(source.substringBefore("\npackage "), "// R.drawable.pic")
  }

  @Test
  fun `a radio template shares one group state outside the loop`() {
    val source =
      template(
        """{"id":"row","component":"RadioButton","label":{"@path":"name"},"selected":{"@path":"done"}}""",
      )
    has(
      source,
      "var rootSelected by remember { mutableIntStateOf(plates.indexOfFirst { plate -> plate.done }) }",
    )
    has(source, "itemsIndexed(plates) { index, plate ->")
    has(source, "selected = rootSelected == index")
    has(source, "onSelect = { rootSelected = index }")
  }
}
