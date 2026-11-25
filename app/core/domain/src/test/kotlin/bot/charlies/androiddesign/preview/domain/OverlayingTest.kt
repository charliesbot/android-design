package bot.charlies.androiddesign.preview.domain

import bot.charlies.androiddesign.preview.domain.findings.node
import bot.charlies.androiddesign.preview.domain.findings.text
import bot.charlies.androiddesign.preview.domain.findings.tree
import bot.charlies.androiddesign.preview.model.Finding
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.Severity
import bot.charlies.androiddesign.preview.model.Views
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OverlayingTest {
  private fun check(notes: List<CheckNote> = emptyList(), vararg errors: CheckError) =
    ViewCheck(pass = errors.isEmpty(), notes = notes, errors = errors.toList())

  @Test
  fun `an error the base screen already reports is dropped, matched by rule and id`() {
    val base = check(emptyList(), CheckError("text-clipped", listOf("a", "b"), "cut"))
    val overlay =
      check(emptyList(), CheckError("text-clipped", listOf("a", "b"), "cut differently"))

    val result = overlay.overlaying(base)

    assertTrue(result.pass)
    assertEquals(emptyList(), result.errors)
  }

  @Test
  fun `an error the overlay causes stays, with only its new ids`() {
    val base = check(emptyList(), CheckError("text-clipped", listOf("a"), "cut"))
    val overlay =
      check(
        emptyList(),
        CheckError("text-clipped", listOf("a", "snack"), "cut"),
        CheckError("one-fab", listOf("fab"), "two"),
      )

    val result = overlay.overlaying(base)

    assertEquals(
      listOf(
        CheckError("text-clipped", listOf("snack"), "cut"),
        CheckError("one-fab", listOf("fab"), "two"),
      ),
      result.errors,
    )
    assertEquals(false, result.pass)
  }

  @Test
  fun `a whole-view error is matched by rule, and the layout is left out`() {
    val base = check(emptyList(), CheckError("view-rule", detail = "x"))
    val overlay =
      ViewCheck(
        pass = false,
        errors = listOf(CheckError("view-rule", detail = "x"), CheckError("other", detail = "y")),
        layout = null,
      )

    val result = overlay.overlaying(base)

    assertEquals(listOf("other"), result.errors.map { it.rule })
    assertNull(result.layout)
  }

  @Test
  fun `a note the base screen already reports is dropped by rule and id, and a new one stays`() {
    val base = check(listOf(CheckNote("text-truncated", listOf("a")), CheckNote("offscreen")))
    val overlay =
      check(
        listOf(
          CheckNote("text-truncated", listOf("a", "snack")),
          CheckNote("offscreen"),
          CheckNote("contrast", listOf("sheet")),
        ),
      )

    val result = overlay.overlaying(base)

    assertEquals(
      listOf(CheckNote("text-truncated", listOf("snack")), CheckNote("contrast", listOf("sheet"))),
      result.notes,
    )
    assertTrue(result.pass, "notes never fail a view")
  }

  @Test
  fun `contrast on base text drawn again in place is dropped, because the overlay may cover it`() {
    val artist = text("Late Summer")
    val base =
      ViewCheck.of(
        tree(
          Views.Light,
          node("screen", "Column", w = 411f, h = 800f),
          node("artist", "Text", y = 120f, parent = "screen", text = artist),
        ),
      )
    // The overlay composable draws the screen again, so its copy of the base text gets a new id;
    // the same call in the sheet, at other bounds, is the overlay's own text.
    val overlayTree =
      tree(
        Views.Light,
        node("screen", "Column", w = 411f, h = 800f),
        node("artist", "Text", y = 120f, parent = "screen", text = artist),
        node("artist_2", "Text", y = 120f, parent = "screen", text = artist),
        node("sheet-artist", "Text", y = 600f, parent = "screen", text = artist),
      )
    val contrast = { id: String ->
      Finding(id, FindingRule.Contrast, Severity.Error, detail = "1:1, needs 4.5:1")
    }
    val overlay =
      ViewCheck.of(
        overlayTree.copy(findings = listOf("artist", "artist_2", "sheet-artist").map(contrast)),
      )

    val result = overlay.overlaying(base)

    assertEquals(listOf(listOf("sheet-artist")), result.errors.map { it.ids })
  }
}
