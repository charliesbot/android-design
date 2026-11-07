package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.has
import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.lacks
import kotlin.test.Test

/** Dialogs and pickers follow their `showIn` and `sizeClass` in the exported code. */
class PickerExportTest {
  private fun flat(text: String) =
    text.replace(Regex("\\s+"), " ").replace(", )", ")").replace("( ", "(").replace(" .", ".")

  private fun export(vararg components: String) =
    flat(
      ExportFixtures.export(
        """{"id":"root","component":"Column","children":["open"]}""",
        """{"id":"open","component":"Button","label":"Open","action":{"functionCall":{"@call":"open","args":{"overlay":"pick"}}}}""",
        *components,
      ),
    )

  @Test
  fun `a date picker full-screen at compact and modal from medium up exports both by width`() {
    val source =
      export(
        """{"id":"pick","component":"DatePicker","variant":"full-screen","selected":"2026-11-06","sizeClass":{"medium":{"variant":"modal"}}}""",
      )
    has(source, "if (!atLeastMedium) {")
    has(source, "} else if (atLeastMedium) {")
    has(source, "Scaffold(modifier = Modifier.fillMaxSize()")
    has(source, "DatePickerDialog(")
  }

  @Test
  fun `a picker has its own state in each branch and the open flag outside them`() {
    val source =
      export(
        """{"id":"pick","component":"DatePicker","selected":"2026-11-06","sizeClass":{"medium":{"range":true,"start":"2026-11-06"}}}""",
      )
    has(source, "var showPick by remember { mutableStateOf(false) }")
    has(source, "rememberDatePickerState(initialSelectedDateMillis = 1793923200000L)")
    has(source, "rememberDateRangePickerState(initialSelectedStartDateMillis = 1793923200000L)")
  }

  @Test
  fun `a full-screen dialog shown at compact only draws nothing at the other sizes`() {
    val source =
      export(
        """{"id":"pick","component":"Dialog","variant":"full-screen","title":"New","content":[],"showIn":["compact"]}""",
      )
    has(source, "if (!atLeastMedium) {")
    lacks(source, "} else")
    lacks(source, "AlertDialog(")
  }

  @Test
  fun `a dialog that is full-screen at compact and basic from medium up exports both`() {
    val source =
      export(
        """{"id":"pick","component":"Dialog","variant":"full-screen","title":"New","content":[],"sizeClass":{"medium":{"variant":"basic","content":[]}}}""",
      )
    has(source, "Scaffold(")
    has(source, "AlertDialog(")
  }

  @Test
  fun `a picker hidden at every size is not written`() {
    val source = export("""{"id":"pick","component":"TimePicker","time":"09:30","showIn":[]}""")
    lacks(source, "TimePickerDialog(")
    lacks(source, "rememberTimePickerState")
  }

  @Test
  fun `a docked picker is written beside its anchor, a dialog picker with an anchor too`() {
    val source =
      flat(
        ExportFixtures.export(
          """{"id":"root","component":"Column","children":["field"]}""",
          """{"id":"field","component":"TextField","label":"Date"}""",
          """{"id":"pick","component":"DatePicker","variant":"docked","selected":"2026-11-06","anchor":"field","sizeClass":{"medium":{"variant":"modal"}}}""",
        ),
      )
    has(source, "Box {")
    has(source, "Popup(")
    has(source, "DatePickerDialog(")
  }

  private fun count(source: String, part: String) =
    source.windowed(part.length).count { it == part }

  @Test
  fun `a picker anchored to a field that changes with the width is written beside every variant`() {
    val source =
      flat(
        ExportFixtures.export(
          """{"id":"root","component":"Column","children":["field"]}""",
          """{"id":"field","component":"TextField","label":"Date","sizeClass":{"medium":{"label":"When"}}}""",
          """{"id":"pick","component":"DatePicker","variant":"docked","selected":"2026-11-06","anchor":"field"}""",
        ),
      )
    has(source, "label = { Text(\"Date\") }")
    has(source, "label = { Text(\"When\") }")
    check(count(source, "Popup(") == 2) { "expected a popup beside each field, got $source" }
    check(count(source, "var showPick by") == 1) { "the open flag is declared once" }
  }

  @Test
  fun `a menu anchored to a button that changes with the width is written beside every variant`() {
    val source =
      flat(
        ExportFixtures.export(
          """{"id":"root","component":"Column","children":["more"]}""",
          """{"id":"more","component":"Button","label":"More","sizeClass":{"medium":{"label":"Menu"}}}""",
          """{"id":"menu","component":"Menu","anchor":"more","items":["edit"]}""",
          """{"id":"edit","component":"MenuItem","label":"Edit"}""",
        ),
      )
    check(count(source, "DropdownMenuPopup(") == 2) { "expected a menu beside each button" }
    check(count(source, "var showMenu by") == 1) { "the open flag is declared once" }
  }

  @Test
  fun `the exported time picker leaves the clock format to the device`() {
    val source = export("""{"id":"pick","component":"TimePicker","time":"14:05","is24Hour":true}""")
    has(source, "rememberTimePickerState(initialHour = 14, initialMinute = 5)")
    lacks(source, "is24Hour")
  }
}
