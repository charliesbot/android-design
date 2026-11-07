package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.has
import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.lacks
import kotlin.test.Test
import kotlin.test.assertEquals

/** Menus and sheets export their templated items and their size class overrides. */
class MenuSheetExportTest {
  private val data = """{"plates":[{"name":"Ramen"},{"name":"Udon"}]}"""

  private fun export(vararg components: String, dataModel: String? = null) =
    ExportFixtures.export(
      """{"id":"root","component":"Column","children":["open"]}""",
      """{"id":"open","component":"Button","label":"Open","action":{"functionCall":{"@call":"open","args":{"overlay":"o"}}}}""",
      *components,
      dataModel = dataModel,
    )

  private fun count(source: String, needle: String) =
    Regex(Regex.escape(needle)).findAll(source).count()

  @Test
  fun `templated menu items export as a loop with their position among the data rows`() {
    val source =
      export(
        """{"id":"o","component":"Menu","anchor":"open","items":{"componentId":"item","path":"/plates"}}""",
        """{"id":"item","component":"MenuItem","label":{"@path":"name"}}""",
        dataModel = data,
      )
    has(source, "DropdownMenuGroup(")
    has(
      source,
      "plates.forEachIndexed { index, plate -> DropdownMenuItem( text = { Text(plate.name) }, onClick = { showO = false }, shape = MenuDefaults.itemShape(index, plates.size).shape",
    )
  }

  @Test
  fun `templated items in a menu group export inside that group`() {
    val source =
      export(
        """{"id":"o","component":"Menu","anchor":"open","items":["g"]}""",
        """{"id":"g","component":"MenuGroup","items":{"componentId":"item","path":"/plates"}}""",
        """{"id":"item","component":"MenuItem","label":{"@path":"name"}}""",
        dataModel = data,
      )
    has(source, "plates.forEachIndexed { index, plate -> DropdownMenuItem(")
    has(source, "MenuDefaults.itemShape(index, plates.size)")
  }

  @Test
  fun `a bottom sheet whose content changes with the width exports both bodies by width`() {
    val source =
      export(
        """{"id":"o","component":"BottomSheet","content":["a"],"sizeClass":{"medium":{"content":["b"]}}}""",
        """{"id":"a","component":"Text","text":"Compact body"}""",
        """{"id":"b","component":"Text","text":"Wide body"}""",
      )
    has(source, "if (!atLeastMedium) {")
    has(source, "Text(\"Compact body\")")
    has(source, "Text(\"Wide body\")")
    assertEquals(1, count(source, "var showO by"))
  }

  @Test
  fun `a side sheet whose title changes with the width exports both titles by width`() {
    val source =
      export(
        """{"id":"o","component":"SideSheet","title":"Filters","content":[],"sizeClass":{"expanded":{"title":"All filters"}}}""",
      )
    has(source, "if (!atLeastExpanded) {")
    has(source, "Text(\"Filters\"")
    has(source, "Text(\"All filters\"")
    assertEquals(1, count(source, "var showO by"))
  }

  @Test
  fun `a menu whose colors change with the width exports both by width and declares its state once`() {
    val source =
      export(
        """{"id":"o","component":"Menu","anchor":"open","items":["i"],"sizeClass":{"medium":{"colors":"vibrant"}}}""",
        """{"id":"i","component":"MenuItem","label":"Edit","role":"multi","selected":true}""",
      )
    has(source, "if (!atLeastMedium) {")
    has(source, "colors = MenuDefaults.selectableItemVibrantColors()")
    assertEquals(2, count(source, "DropdownMenuPopup("))
    assertEquals(1, count(source, "var showO by"))
    assertEquals(1, count(source, "var iChecked by"))
  }

  @Test
  fun `a template of groups still exports the one empty group`() {
    val source =
      export(
        """{"id":"o","component":"Menu","anchor":"open","items":{"componentId":"g","path":"/plates"}}""",
        """{"id":"g","component":"MenuGroup","items":["item"]}""",
        """{"id":"item","component":"MenuItem","label":"Edit"}""",
        dataModel = data,
      )
    assertEquals(1, count(source, "DropdownMenuGroup("), source)
    lacks(source, "forEachIndexed")
  }
}
