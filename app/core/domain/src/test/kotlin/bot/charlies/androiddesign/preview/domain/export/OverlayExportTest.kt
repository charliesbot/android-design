package bot.charlies.androiddesign.preview.domain.export

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Overlays are state that starts closed; actions open and close them. */
class OverlayExportTest {
  private fun flat(text: String) =
    text.replace(Regex("\\s+"), " ").replace(", )", ")").replace("( ", "(").replace(" .", ".")

  private val open = { id: String ->
    """{"functionCall":{"@call":"open","args":{"overlay":"$id"}}}"""
  }
  private val dismiss = """{"functionCall":{"@call":"dismiss"}}"""

  @Test
  fun `every overlay starts closed`() {
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"Column","children":["b"]}""",
        """{"id":"b","component":"Button","label":"Open","action":${open("sheet")}}""",
        """{"id":"sheet","component":"BottomSheet","content":[]}""",
        """{"id":"dlg","component":"Dialog","title":"T","confirm":"ok"}""",
        """{"id":"ok","component":"Button","label":"OK"}""",
        """{"id":"menu","component":"Menu","items":[]}""",
      )
    val body = flat(source)
    assertTrue(body.contains("var showSheet by remember { mutableStateOf(false) }"))
    assertTrue(body.contains("var showDlg by remember { mutableStateOf(false) }"))
    assertTrue(body.contains("var showMenu by remember { mutableStateOf(false) }"))
  }

  @Test
  fun `open sets the flag and an overlay is drawn only while it is set`() {
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"Column","children":["b"]}""",
        """{"id":"b","component":"Button","label":"Open","action":${open("sheet")}}""",
        """{"id":"sheet","component":"BottomSheet","content":[]}""",
      )
    val body = flat(source)
    assertTrue(body.contains("onClick = { showSheet = true }"))
    assertTrue(body.contains("if (showSheet) { ModalBottomSheet("))
    assertTrue(
      source.indexOf("Column(") < source.indexOf("if (showSheet)"),
      "overlays follow the content",
    )
  }

  @Test
  fun `dismiss inside an overlay closes that overlay`() {
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"Column","children":["a","b"]}""",
        """{"id":"a","component":"Button","label":"A","action":${open("s1")}}""",
        """{"id":"b","component":"Button","label":"B","action":${open("s2")}}""",
        """{"id":"s1","component":"BottomSheet","content":["c1"]}""",
        """{"id":"c1","component":"Button","label":"Close","action":$dismiss}""",
        """{"id":"s2","component":"BottomSheet","content":[]}""",
      )
    val body = flat(source)
    assertTrue(body.contains("Button(onClick = { showS1 = false }) { Text(\"Close\") }"))
    assertTrue(
      !Regex("onClick = \\{[^}]*showS2 = false").containsMatchIn(body),
      "the other sheet stays open",
    )
  }

  @Test
  fun `dismiss outside any overlay closes them all`() {
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"Column","children":["x"]}""",
        """{"id":"x","component":"Button","label":"Reset","action":$dismiss}""",
        """{"id":"s1","component":"BottomSheet","content":[]}""",
        """{"id":"s2","component":"BottomSheet","content":[]}""",
      )
    assertTrue(flat(source).contains("onClick = { showS1 = false showS2 = false }"))
  }

  @Test
  fun `a menu beside its anchor shares one box with it`() {
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"Column","children":["b"]}""",
        """{"id":"b","component":"Button","label":"More","action":${open("m")}}""",
        """{"id":"m","component":"Menu","anchor":"b","items":["i"]}""",
        """{"id":"i","component":"MenuItem","label":"Edit"}""",
      )
    val body = flat(source)
    assertTrue(
      body.contains(
        "Box { Button(onClick = { showM = true }) { Text(\"More\") } DropdownMenuPopup(",
      ),
    )
    assertEquals(1, Regex("DropdownMenuPopup\\(").findAll(source).count())
  }
}
