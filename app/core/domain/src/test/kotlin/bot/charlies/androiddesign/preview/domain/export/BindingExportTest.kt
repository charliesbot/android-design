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

  /**
   * A prop that picks the Compose call, the scroll or the grid kind exports the same code when it
   * is bound to a sample value as when it is written out: the previewer draws the sample.
   */
  @Test
  fun `a bound prop that picks the call exports as its sample value`() {
    class Case(val sample: String, val template: String)
    val cases =
      listOf(
        Case("true", """{"id":"root","component":"Divider","vertical":%s}"""),
        Case("true", """{"id":"root","component":"Column","scroll":%s,"children":["t"]}"""),
        Case("3", """{"id":"root","component":"Grid","columns":%s,"children":["t"]}"""),
        Case("true", """{"id":"root","component":"LoadingIndicator","contained":%s}"""),
        Case("true", """{"id":"root","component":"ProgressIndicator","wavy":%s}"""),
        Case(
          "true",
          """{"id":"root","component":"IconButton","icon":"star","toggle":%s,"checked":true}""",
        ),
        Case(
          "true",
          """{"id":"root","component":"FloatingActionButton","icon":"add","label":"New","extended":%s}""",
        ),
        Case("true", """{"id":"root","component":"ChipGroup","multi":%s,"children":["a","b"]}"""),
        Case("2", """{"id":"root","component":"Text","text":"t","maxLines":%s}"""),
        Case("32", """{"id":"root","component":"Icon","name":"star","size":%s}"""),
        Case("4", """{"id":"root","component":"Slider","steps":%s}"""),
        Case("4", """{"id":"root","component":"RangeSlider","steps":%s}"""),
        Case("3", """{"id":"root","component":"TextField","lines":%s}"""),
        Case(
          "true",
          """{"id":"root","component":"NavigationRail","expanded":%s,"children":["rail"]}""",
        ),
      )
    val rest =
      arrayOf(
        """{"id":"t","component":"Text","text":"t"}""",
        """{"id":"a","component":"FilterChip","label":"A"}""",
        """{"id":"b","component":"FilterChip","label":"B"}""",
        """{"id":"rail","component":"NavigationRailItem","label":"Home","icon":"home"}""",
      )
    cases.forEach { case ->
      fun body(value: String, data: String? = null) =
        ExportFixtures.body(
          ExportFixtures.export(case.template.format(value), *rest, dataModel = data),
        )
      val written = body(case.sample)
      val bound = body("""{"@path":"/v"}""", """{"v":${case.sample}}""")
      assertTrue(written != body("false"), "${case.template} should change the export")
      assertEquals(written, bound, case.template)
    }
  }

  /**
   * A bound number skips validation, so the export moves it into the catalog's bounds as the
   * previewer does.
   */
  @Test
  fun `a bound grid size is moved into its bounds like the previewer does`() {
    fun body(prop: String, value: String, data: String? = null) =
      ExportFixtures.body(
        ExportFixtures.export(
          """{"id":"root","component":"Grid","$prop":$value,"children":["t"]}""",
          """{"id":"t","component":"Text","text":"t"}""",
          dataModel = data,
        ),
      )
    listOf(
        Triple("columns", "0", "1"),
        Triple("columns", "-1", "1"),
        Triple("columns", "2.6", "3"),
        Triple("columns", "30", "24"),
        Triple("minColumnWidth", "0", "1"),
        Triple("minColumnWidth", "5000", "1000"),
      )
      .forEach { (prop, sample, drawn) ->
        val bound = body(prop, """{"@path":"/v"}""", """{"v":$sample}""")
        assertEquals(body(prop, drawn), bound, "$prop bound to $sample")
      }
  }

  private val templateColumn =
    arrayOf(
      """{"id":"root","component":"Column","children":{"componentId":"mark","path":"/plates"}}""",
    )

  private fun plates(vararg fields: String) =
    """{"plates":[${fields.joinToString(",") { "{$it}" }}]}"""

  @Test
  fun `a bound icon name calls a helper that lists every name across the items`() {
    val source =
      ExportFixtures.export(
        *templateColumn,
        """{"id":"mark","component":"Icon","name":{"@path":"icon"}}""",
        dataModel = plates(""""icon":"skillet"""", """"icon":"restaurant""""),
      )
    assertHas(source, "Icon(painterResource(symbolDrawable(plate.icon)), contentDescription = null")
    assertTrue(
      source.contains(
        """
        |private fun symbolDrawable(name: String): Int =
        |  when (name) {
        |    "restaurant" -> R.drawable.restaurant
        |    "skillet" -> R.drawable.skillet
        |    else -> R.drawable.help
        |  }
        """
          .trimMargin()
          .trim(),
      ),
      source,
    )
    assertHas(
      source,
      "//   R.drawable.help",
      "//   R.drawable.restaurant",
      "//   R.drawable.skillet",
    )
    assertEquals(false, source.contains("filledSymbolDrawable"))
    assertEquals(false, source.contains("R.drawable.help_filled"))
  }

  @Test
  fun `a bound icon in a list item slot and in a nested template reads every parent's items`() {
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"Column","children":{"componentId":"group","path":"/groups"}}""",
        """{"id":"group","component":"List","children":{"componentId":"row","path":"subs"}}""",
        """{"id":"row","component":"ListItem","headline":"h","leading":"mark"}""",
        """{"id":"mark","component":"Icon","name":{"@path":"icon"}}""",
        dataModel = """{"groups":[{"subs":[{"icon":"b"}]},{"subs":[{"icon":"a"},{"icon":"c"}]}]}""",
      )
    assertHas(
      source,
      "leadingContent = { Icon(painterResource(symbolDrawable(sub.icon)), contentDescription = null) }",
      "\"a\" -> R.drawable.a \"b\" -> R.drawable.b \"c\" -> R.drawable.c else -> R.drawable.help",
    )
  }

  @Test
  fun `a bound icon name on a button, chip, field, menu item and nav item uses the helper`() {
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"Column","children":["b","c","t","m","n"]}""",
        """{"id":"b","component":"Button","label":"x","icon":{"@path":"/i"}}""",
        """{"id":"c","component":"SuggestionChip","label":"x","icon":{"@path":"/i"}}""",
        """{"id":"t","component":"TextField","leadingIcon":{"@path":"/i"}}""",
        """{"id":"m","component":"MenuItem","label":"x","icon":{"@path":"/i"}}""",
        """{"id":"n","component":"NavigationBarItem","label":"x","icon":{"@path":"/i"},"selectedIcon":{"@path":"/j"},"selected":{"@path":"/on"}}""",
        dataModel = """{"i":"home","j":"star","on":true}""",
      )
    assertEquals(4, Regex("painterResource\\(symbolDrawable\\(i\\)\\)").findAll(source).count())
    assertHas(
      source,
      "leadingIcon = { Icon(painterResource(symbolDrawable(i)), contentDescription = null) }",
      "painterResource(if (on) symbolDrawable(j) else symbolDrawable(i))",
    )
  }

  @Test
  fun `a bound icon that is filled calls the filled helper`() {
    val named =
      ExportFixtures.export(
        *templateColumn,
        """{"id":"mark","component":"Icon","name":{"@path":"icon"},"filled":true}""",
        dataModel = plates(""""icon":"star"""", """"icon":"home""""),
      )
    assertHas(named, "painterResource(filledSymbolDrawable(plate.icon))")
    assertTrue(
      named.contains(
        """
        |private fun filledSymbolDrawable(name: String): Int =
        |  when (name) {
        |    "home" -> R.drawable.home_filled
        |    "star" -> R.drawable.star_filled
        |    else -> R.drawable.help_filled
        |  }
        """
          .trimMargin()
          .trim(),
      ),
      named,
    )
    assertEquals(false, named.contains("private fun symbolDrawable"))

    val toggled =
      ExportFixtures.export(
        """{"id":"root","component":"IconButton","icon":{"@path":"/icon"},"toggle":true,"checked":true}""",
        dataModel = """{"icon":"star"}""",
      )
    assertHas(
      toggled,
      "painterResource(if (rootChecked) filledSymbolDrawable(icon) else symbolDrawable(icon))",
    )
  }

  @Test
  fun `a bound filled flag picks between the filled and plain drawable`() {
    val literalName =
      ExportFixtures.export(
        """{"id":"root","component":"Icon","name":"star","filled":{"@path":"/on"}}""",
        dataModel = """{"on":true}""",
      )
    assertHas(
      literalName,
      "painterResource(if (on) R.drawable.star_filled else R.drawable.star)",
    )
    val boundName =
      ExportFixtures.export(
        """{"id":"root","component":"Icon","name":{"@path":"/icon"},"filled":{"@path":"/on"}}""",
        dataModel = """{"icon":"star","on":true}""",
      )
    assertHas(
      boundName,
      "painterResource(if (on) filledSymbolDrawable(icon) else symbolDrawable(icon))",
    )
  }

  /** Each row keeps its own fill, as in the previewer, not the first row's. */
  @Test
  fun `a bound name and filled flag in a template are both read per item`() {
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"Column","children":{"componentId":"mark","path":"/tasks"}}""",
        """{"id":"mark","component":"Icon","name":{"@path":"icon"},"filled":{"@path":"done"}}""",
        dataModel = """{"tasks":[{"icon":"eco","done":true},{"icon":"call","done":false}]}""",
      )
    assertHas(
      source,
      "painterResource(if (task.done) filledSymbolDrawable(task.icon) else symbolDrawable(task.icon))",
    )
  }

  @Test
  fun `a bound image source calls a helper that lists every file across the items`() {
    val source =
      ExportFixtures.export(
        *templateColumn,
        """{"id":"mark","component":"Image","src":{"@path":"photo"},"contentDescription":"d"}""",
        dataModel = plates(""""photo":"images/ramen.jpg"""", """"photo":"images/dumpling.png""""),
      )
    assertHas(source, "Image(painter = painterResource(imageDrawable(plate.photo))")
    assertTrue(
      source.contains(
        """
        |private fun imageDrawable(src: String): Int =
        |  when (src) {
        |    "images/dumpling.png" -> R.drawable.dumpling
        |    "images/ramen.jpg" -> R.drawable.ramen
        |    else -> error("No drawable for image ${'$'}src")
        |  }
        """
          .trimMargin()
          .trim(),
      ),
      source,
    )
    assertHas(source, "//   R.drawable.dumpling", "//   R.drawable.ramen")
  }

  @Test
  fun `bound data URIs are named after the node and numbered when there are several`() {
    val one =
      ExportFixtures.export(
        *templateColumn,
        """{"id":"mark","component":"Image","src":{"@path":"photo"},"contentDescription":"d"}""",
        dataModel =
          plates(
            """"photo":"data:image/png;base64,AAA="""",
            """"photo":"data:image/png;base64,AAA="""",
          ),
      )
    assertHas(one, """"data:image/png;base64,AAA=" -> R.drawable.mark""")
    val several =
      ExportFixtures.export(
        *templateColumn,
        """{"id":"mark","component":"Image","src":{"@path":"photo"},"contentDescription":"d"}""",
        dataModel =
          plates(
            """"photo":"data:image/png;base64,BBB="""",
            """"photo":"data:image/png;base64,AAA="""",
            """"photo":"data:image/png;base64,BBB="""",
          ),
      )
    assertHas(
      several,
      """"data:image/png;base64,AAA=" -> R.drawable.mark_2""",
      """"data:image/png;base64,BBB=" -> R.drawable.mark_1""",
    )
  }

  @Test
  fun `a bound error, enabled and selected flag is passed through`() {
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"List","variant":"segmented","children":["row","field","sw"]}""",
        """{"id":"row","component":"ListItem","headline":"h","selected":{"@path":"/on"}}""",
        """{"id":"field","component":"TextField","error":{"@path":"/bad"},"enabled":{"@path":"/on"}}""",
        """{"id":"sw","component":"Switch","enabled":{"@path":"/on"}}""",
        dataModel = """{"on":true,"bad":false}""",
      )
    assertHas(source, "selected = on,", "isError = bad,", "enabled = on,")
    assertEquals(2, Regex("enabled = on").findAll(source).count())
  }

  @Test
  fun `a literal error, enabled and selected flag keeps its shape`() {
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"List","variant":"segmented","children":["row","field","sw"]}""",
        """{"id":"row","component":"ListItem","headline":"h","selected":true}""",
        """{"id":"field","component":"TextField","error":true,"enabled":false}""",
        """{"id":"sw","component":"Switch","enabled":true}""",
      )
    assertHas(source, "selected = true,", "isError = true,", "enabled = false,")
    assertEquals(false, source.contains("enabled = true"))
  }

  @Test
  fun `a chip group starts on the chip whose bound selected flag is true`() {
    fun body(selected: String, data: String? = null) =
      ExportFixtures.body(
        ExportFixtures.export(
          """{"id":"root","component":"ChipGroup","children":["a","b"]}""",
          """{"id":"a","component":"FilterChip","label":"A"}""",
          """{"id":"b","component":"FilterChip","label":"B","selected":$selected}""",
          dataModel = data,
        ),
      )
    val bound = body("""{"@path":"/on"}""", """{"on":true}""")
    assertHas(bound, """rootSelected by remember { mutableStateOf("b") }""")
    assertEquals(body("true"), bound)
  }

  @Test
  fun `a grid with a minimum column width counts the gap like the previewer does`() {
    fun source(vararg extra: String) =
      ExportFixtures.export(
        """{"id":"root","component":"Grid","minColumnWidth":160,${extra.joinToString("")}"children":["t"]}""",
        """{"id":"t","component":"Text","text":"t"}""",
      )
    assertHas(
      source(""""spacing":"sm","""),
      "val columns = ((constraints.maxWidth.toDp() + 8.dp) / (160.dp + 8.dp)).toInt().coerceIn(1, 24)",
      "gap(8.dp)",
    )
    assertHas(
      source(),
      "val columns = (constraints.maxWidth.toDp() / 160.dp).toInt().coerceIn(1, 24)",
    )
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
  fun `a template under a carousel reads its item by index`() {
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"Carousel","children":{"componentId":"item","path":"/plates"}}""",
        """{"id":"item","component":"Text","text":{"@path":"name"}}""",
        dataModel = """{"plates":[{"name":"Soup"},{"name":"Salad"}]}""",
      )
    assertHas(
      source,
      "val rootState = rememberCarouselState { plates.size }",
      ") { index ->",
      "val plate = plates[index]",
      "Text(plate.name, modifier = Modifier.maskClip(MaterialTheme.shapes.extraLarge))",
    )
  }

  @Test
  fun `the selected tab of a template follows the bound selected flag`() {
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"Tabs","children":{"componentId":"tab","path":"/tabs"}}""",
        """{"id":"tab","component":"Tab","label":{"@path":"name"},"selected":{"@path":"on"}}""",
        dataModel =
          """{"tabs":[{"name":"A","on":false},{"name":"B","on":true},{"name":"C","on":false}]}""",
      )
    assertHas(
      source,
      "PrimaryTabRow(selectedTabIndex = tabs.indexOfFirst { tab -> tab.on }.coerceAtLeast(0)",
      "tabs.forEach { tab ->",
      "selected = tab.on",
    )
  }

  @Test
  fun `a screen without a data model has no sample object`() {
    val source = ExportFixtures.export(*column("a"), """{"id":"a","component":"Text","text":"x"}""")
    assertEquals(false, source.contains("MainSample"))
  }
}
