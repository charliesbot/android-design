package bot.charlies.androiddesign.preview.domain.export

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Data model bindings become Kotlin values, and templates become loops. */
class BindingExportTest {
  private fun flat(text: String) =
    text.replace(Regex("\\s+"), " ").replace(", )", ")").replace("( ", "(").replace(" .", ".")

  private fun assertHas(source: String, vararg expected: String) {
    val folded = flat(source)
    expected.forEach { assertTrue(folded.contains(flat(it)), "missing:\n$it\n\nin:\n$source") }
  }

  private val data =
    """{"title":"Plates","count":3,"ratio":1.5,"open":true,"chef":{"name":"Ada","city":"Oslo"}}"""

  private fun column(vararg children: String) =
    arrayOf(
      """{"id":"root","component":"Column","children":[${children.joinToString(",") { "\"$it\"" }}]}""",
    )

  @Test
  fun `a bound string is the sample value's property`() {
    val source =
      ExportFixtures.export(
        *column("a", "b"),
        """{"id":"a","component":"Text","text":{"@path":"/title"}}""",
        """{"id":"b","component":"Text","text":{"@path":"/chef/name"}}""",
        dataModel = data,
      )
    assertHas(source, "Text(title)", "Text(chef.name)")
  }

  @Test
  fun `the data model becomes parameters with sample defaults`() {
    val source =
      ExportFixtures.export(
        *column("a"),
        """{"id":"a","component":"Text","text":"x"}""",
        dataModel = data,
      )
    assertHas(
      source,
      "title: String = MainSample.title",
      "count: Int = MainSample.count",
      "ratio: Double = MainSample.ratio",
      "open: Boolean = MainSample.open",
      "chef: Chef = MainSample.chef",
      "data class Chef(val name: String, val city: String)",
      "val chef = Chef(name = \"Ada\", city = \"Oslo\")",
      "val title = \"Plates\"",
      "val count = 3",
      "val ratio = 1.5",
      "val open = true",
    )
  }

  @Test
  fun `a bound number shown as text is converted and a bound flag is passed through`() {
    val source =
      ExportFixtures.export(
        *column("a", "b"),
        """{"id":"a","component":"Text","text":{"@path":"/count"}}""",
        """{"id":"b","component":"Switch","checked":{"@path":"/open"}}""",
        dataModel = data,
      )
    assertHas(source, "Text(count.toString())", "var bChecked by remember { mutableStateOf(open) }")
  }

  @Test
  fun `a binding with no sample value is an empty string`() {
    val source =
      ExportFixtures.export(
        *column("a"),
        """{"id":"a","component":"Text","text":{"@path":"/missing/path"}}""",
        dataModel = data,
      )
    assertHas(source, "Text(\"\")")
  }

  @Test
  fun `keys that are not identifiers are made into properties`() {
    val source =
      ExportFixtures.export(
        *column("a"),
        """{"id":"a","component":"Text","text":{"@path":"/first_name"}}""",
        dataModel = """{"first_name":"Ada"}""",
      )
    assertHas(source, "Text(firstName)", "firstName: String = MainSample.firstName")
  }

  @Test
  fun `a template over a list becomes a forEach loop`() {
    val source =
      ExportFixtures.export(
        *column("list"),
        """{"id":"list","component":"Column","children":{"componentId":"row","path":"/items"}}""",
        """{"id":"row","component":"Text","text":{"@path":"name"}}""",
        dataModel = """{"items":[{"name":"A"},{"name":"B"}]}""",
      )
    assertHas(
      source,
      "items: List<Item> = MainSample.items",
      "items.forEach { item -> Text(item.name) }",
      "data class Item(val name: String)",
      "val items = listOf(Item(name = \"A\"), Item(name = \"B\"))",
    )
  }

  @Test
  fun `a template that shows the index loops with forEachIndexed`() {
    val source =
      ExportFixtures.export(
        *column("list"),
        """{"id":"list","component":"Column","children":{"componentId":"row","path":"/items"}}""",
        """{"id":"row","component":"Text","text":{"@call":"@index"}}""",
        dataModel = """{"items":[{"name":"A"}]}""",
      )
    assertHas(source, "items.forEachIndexed { index, item -> Text(index.toString()) }")
  }

  @Test
  fun `nested templates read the innermost item and number their index`() {
    val source =
      ExportFixtures.export(
        *column("outer"),
        """{"id":"outer","component":"Column","children":{"componentId":"group","path":"/groups"}}""",
        """{"id":"group","component":"Column","children":{"componentId":"leaf","path":"subs"}}""",
        """{"id":"leaf","component":"Text","text":{"@path":"label"}}""",
        dataModel = """{"groups":[{"title":"G","subs":[{"label":"s1"}]}]}""",
      )
    assertHas(
      source,
      "groups.forEach { group -> Column(modifier = Modifier.fillMaxWidth()) { group.subs.forEach { sub -> Text(sub.label) } } }",
    )
  }

  @Test
  fun `a loop variable that would clash falls back to item`() {
    val source =
      ExportFixtures.export(
        *column("list"),
        """{"id":"list","component":"Column","children":{"componentId":"row","path":"/item"}}""",
        """{"id":"row","component":"Text","text":{"@path":"name"}}""",
        dataModel = """{"item":[{"name":"A"}]}""",
      )
    assertHas(source, "item.forEach { item2 -> Text(item2.name) }")
  }

  @Test
  fun `two screens with different shapes under one name get distinct classes`() {
    val first = """{"id":"root","component":"Text","text":{"@path":"/plate/name"}}"""
    val text =
      """{"format":1,"navigation":{"start":"a"},"screens":{
        "a":{"catalogId":"x","dataModel":{"plate":{"name":"A"}},"components":[$first]},
        "b":{"catalogId":"x","dataModel":{"plate":{"name":"B","note":"n"}},"components":[
          {"id":"root","component":"Text","text":{"@path":"/plate/note"}}]}}}"""
    val proposal =
      kotlinx.serialization.json.Json.decodeFromString(
        bot.charlies.androiddesign.preview.model.Proposal.serializer(),
        text,
      )
    val source = KotlinExporter.export(proposal, ExportFixtures.defaultDesign())
    assertHas(
      source,
      "data class Plate(val name: String)",
      "data class BPlate(val name: String, val note: String)",
      "plate: BPlate = BSample.plate",
    )
  }

  @Test
  fun `a screen without a data model has no sample object`() {
    val source = ExportFixtures.export(*column("a"), """{"id":"a","component":"Text","text":"x"}""")
    assertEquals(false, source.contains("MainSample"))
  }
}
