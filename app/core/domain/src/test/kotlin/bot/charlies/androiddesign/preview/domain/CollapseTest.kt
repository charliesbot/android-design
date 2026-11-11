package bot.charlies.androiddesign.preview.domain

import bot.charlies.androiddesign.preview.domain.layout.ViewLayout
import bot.charlies.androiddesign.preview.model.PreviewJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CollapseTest {
  private val pass = ViewCheck(pass = true)

  private fun json(views: Map<String, ViewCheck>) =
    PreviewJson.compact.encodeToString(
      ProposalCheck.serializer(),
      ProposalCheck(pass = true, ms = 1, screens = mapOf("home" to views)),
    )

  @Test
  fun `dark equal to light collapses to same light`() {
    val views = ViewCheck.collapse(linkedMapOf("light" to pass, "dark" to pass))

    assertEquals(pass, views.getValue("light"))
    assertEquals(ViewCheck.same("light"), views.getValue("dark"))
    assertEquals(
      """{"pass":true,"ms":1,"screens":{"home":{"light":{"pass":true},"dark":{"same":"light"}}}}""",
      json(views),
    )
  }

  @Test
  fun `a view that differs in pass, errors, notes or layout does not collapse`() {
    val note = ViewCheck(pass = true, notes = listOf(CheckNote("contrast", listOf("a"))))
    val failing = ViewCheck(pass = false, errors = listOf(CheckError("one-fab", listOf("f"))))
    val otherError = ViewCheck(pass = false, errors = listOf(CheckError("one-fab", listOf("g"))))
    val laidOut =
      ViewCheck(pass = true, layout = ViewLayout(nav = "NavigationBar", panes = emptyMap()))
    val views =
      linkedMapOf(
        "light" to pass,
        "a" to note,
        "b" to failing,
        "c" to otherError,
        "d" to laidOut,
        "e" to ViewCheck(pass = false),
      )

    assertEquals(views, ViewCheck.collapse(views))
  }

  @Test
  fun `three equal views all point at the first, never at another same entry`() {
    val views =
      ViewCheck.collapse(
        linkedMapOf("light" to pass, "dark" to pass, "tablet" to ViewCheck(false), "wide" to pass),
      )

    assertEquals("light", views.getValue("dark").same)
    assertEquals("light", views.getValue("wide").same)
    assertEquals(ViewCheck(false), views.getValue("tablet"))
  }

  @Test
  fun `a failing view equal to an earlier failing view collapses and the proposal still fails`() {
    val failing = ViewCheck(pass = false, errors = listOf(CheckError("one-fab", listOf("f"))))
    val views = ViewCheck.collapse(linkedMapOf("light" to failing, "dark" to failing))

    assertEquals("light", views.getValue("dark").same)
    val check = ProposalCheck(pass = false, ms = 1, screens = mapOf("home" to views))
    assertTrue(
      """"dark":{"same":"light"}""" in
        PreviewJson.compact.encodeToString(ProposalCheck.serializer(), check),
    )
    assertEquals(false, check.pass)
  }

  @Test
  fun `a collapsed entry has no verdict of its own to read`() {
    assertFailsWith<IllegalStateException> { ViewCheck.same("light").pass }
  }
}
