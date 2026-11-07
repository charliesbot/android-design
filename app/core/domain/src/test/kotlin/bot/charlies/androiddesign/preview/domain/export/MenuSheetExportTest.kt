package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.has
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

  private val picks = """{"plates":[{"name":"Ramen","on":true},{"name":"Udon","on":false}]}"""

  private fun templatedRole(role: String) =
    export(
      """{"id":"o","component":"Menu","anchor":"open","items":{"componentId":"item","path":"/plates"}}""",
      """{"id":"item","component":"MenuItem","label":{"@path":"name"},"role":"$role","selected":{"@path":"on"},"action":{"functionCall":{"@call":"navigate","args":{"screen":"main"}}}}""",
      dataModel = picks,
    )

  @Test
  fun `a templated single item remembers the picked data row by index`() {
    val source = templatedRole("single")
    has(
      source,
      "var oPicked by remember(plates) { mutableIntStateOf(plates.indexOfFirst { plate -> plate.on }) }",
    )
    has(source, "SelectableDropdownMenuItem( selected = oPicked == index,")
    has(source, "onClick = { oPicked = index")
    has(source, "onNavigateToMain()")
  }

  @Test
  fun `a templated multi item remembers each data row by index and runs its action`() {
    val source = templatedRole("multi")
    has(
      source,
      "val itemChecked = remember(plates) { plates.map { plate -> plate.on }.toMutableStateList() }",
    )
    has(source, "CheckableDropdownMenuItem( checked = itemChecked[index], onCheckedChange = {")
    has(source, "itemChecked[index] = it")
    has(source, "import androidx.compose.runtime.toMutableStateList")
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

  private val sections =
    """{"sections":[{"title":"A","pin":true,"opts":[{"name":"a1","on":true},{"name":"a2","on":false}]},{"title":"B","pin":false,"opts":[{"name":"b1","on":false},{"name":"b2","on":true}]}]}"""

  private fun templatedGroups(vararg members: String) =
    export(
      """{"id":"o","component":"Menu","anchor":"open","items":{"componentId":"g","path":"/sections"}}""",
      *members,
      dataModel = sections,
    )

  @Test
  fun `a template of groups exports one group per data row with its items`() {
    val source =
      templatedGroups(
        """{"id":"g","component":"MenuGroup","items":["item"]}""",
        """{"id":"item","component":"MenuItem","label":{"@path":"title"}}""",
      )
    has(source, "sections.forEachIndexed { index, section ->")
    assertEquals(1, count(source, "DropdownMenuGroup("), source)
    has(source, "MenuDefaults.groupShape(index, sections.size)")
    has(source, "if (index > 0) { Spacer(")
    has(source, "Text(section.title)")
  }

  @Test
  fun `a template of groups with templated items loops both and keys state by both rows`() {
    val source =
      templatedGroups(
        """{"id":"g","component":"MenuGroup","items":{"componentId":"item","path":"opts"}}""",
        """{"id":"item","component":"MenuItem","label":{"@path":"name"},"role":"multi","selected":{"@path":"on"}}""",
      )
    has(source, "sections.forEachIndexed { index, section ->")
    has(source, "section.opts.forEachIndexed { index2, opt ->")
    has(
      source,
      "val itemChecked = remember(sections) { sections.map { section -> section.opts.map { opt -> opt.on }.toMutableStateList() } }",
    )
    has(source, "checked = itemChecked[index][index2]")
    has(source, "itemChecked[index][index2] = it")
  }

  @Test
  fun `single choice in a templated group is one picked index per group row`() {
    val source =
      templatedGroups(
        """{"id":"g","component":"MenuGroup","items":{"componentId":"item","path":"opts"}}""",
        """{"id":"item","component":"MenuItem","label":{"@path":"name"},"role":"single","selected":{"@path":"on"}}""",
      )
    has(
      source,
      "val gPicked = remember(sections) { sections.map { section -> section.opts.indexOfFirst { opt -> opt.on } }.toMutableStateList() }",
    )
    has(source, "selected = gPicked[index] == index2")
    has(source, "gPicked[index] = index2")
  }

  @Test
  fun `listed items of a templated group keep one choice and one flag per group row`() {
    val source =
      templatedGroups(
        """{"id":"g","component":"MenuGroup","items":["s1","s2","k"]}""",
        """{"id":"s1","component":"MenuItem","label":"One","role":"single","selected":true}""",
        """{"id":"s2","component":"MenuItem","label":"Two","role":"single"}""",
        """{"id":"k","component":"MenuItem","label":"Keep","role":"multi","selected":{"@path":"pin"}}""",
      )
    has(
      source,
      "val gSelected = remember(sections) { sections.map { _ -> \"s1\" }.toMutableStateList() }",
    )
    has(source, "selected = gSelected[index] == \"s1\"")
    has(source, "gSelected[index] = \"s2\"")
    has(source, "val kChecked = remember(sections) { sections.map { section ->")
    has(source, "checked = kChecked[index]")
  }
}
