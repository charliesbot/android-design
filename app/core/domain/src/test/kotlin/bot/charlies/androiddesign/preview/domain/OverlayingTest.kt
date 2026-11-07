package bot.charlies.androiddesign.preview.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OverlayingTest {
  private fun check(info: Int = 0, vararg errors: CheckError) =
    ViewCheck(pass = errors.isEmpty(), info = info, errors = errors.toList())

  @Test
  fun `an error the base screen already reports is dropped, matched by rule and id`() {
    val base = check(0, CheckError("text-clipped", listOf("a", "b"), "cut"))
    val overlay = check(0, CheckError("text-clipped", listOf("a", "b"), "cut differently"))

    val result = overlay.overlaying(base)

    assertTrue(result.pass)
    assertEquals(emptyList(), result.errors)
  }

  @Test
  fun `an error the overlay causes stays, with only its new ids`() {
    val base = check(0, CheckError("text-clipped", listOf("a"), "cut"))
    val overlay =
      check(
        0,
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
  fun `a whole-view error is matched by rule, and the layout and repeated info are left out`() {
    val base = check(2, CheckError("view-rule", detail = "x"))
    val overlay =
      ViewCheck(
        pass = false,
        info = 3,
        errors = listOf(CheckError("view-rule", detail = "x"), CheckError("other", detail = "y")),
        layout = null,
      )

    val result = overlay.overlaying(base)

    assertEquals(listOf("other"), result.errors.map { it.rule })
    assertEquals(1, result.info)
    assertNull(result.layout)
  }
}
