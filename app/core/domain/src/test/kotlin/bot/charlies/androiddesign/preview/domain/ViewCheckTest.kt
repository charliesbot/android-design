package bot.charlies.androiddesign.preview.domain

import bot.charlies.androiddesign.preview.domain.findings.node
import bot.charlies.androiddesign.preview.model.Finding
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.Severity
import bot.charlies.androiddesign.preview.model.Views
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ViewCheckTest {
  private val proposal = TestProposal.valid().build()

  private fun treeOf(vararg findings: Finding) =
    LayoutTree(
      screen = "plates",
      view = Views.Light,
      nodes = listOf(node("root", "Scaffold", w = 411f)),
      findings = findings.toList(),
    )

  private fun error(rule: FindingRule, id: String?, detail: String? = null) =
    Finding(id, rule, Severity.Error, detail)

  @Test
  fun `a tree without findings passes and carries its layout`() {
    val check = ViewCheck.of(proposal, treeOf())
    assertTrue(check.pass)
    assertEquals(0, check.info)
    assertEquals(emptyList(), check.errors)
    assertNotNull(check.layout)
  }

  @Test
  fun `info findings are counted and do not fail the view`() {
    val check =
      ViewCheck.of(
        proposal,
        treeOf(
          Finding("a", FindingRule.Contrast, Severity.Info, "low"),
          Finding("b", FindingRule.Contrast, Severity.Info, "low"),
        ),
      )
    assertTrue(check.pass)
    assertEquals(2, check.info)
    assertEquals(emptyList(), check.errors)
  }

  @Test
  fun `error findings of one rule and detail share an entry with their ids`() {
    val check =
      ViewCheck.of(
        proposal,
        treeOf(
          error(FindingRule.OneFab, "fab_a", "2 floating action buttons, keep one"),
          error(FindingRule.TouchTarget, "chip", "chip 40x40"),
          error(FindingRule.OneFab, "fab_b", "2 floating action buttons, keep one"),
        ),
      )
    assertEquals(false, check.pass)
    assertEquals(
      listOf(
        CheckError("one-fab", listOf("fab_a", "fab_b"), "2 floating action buttons, keep one"),
        CheckError("touch-target", listOf("chip"), "chip 40x40"),
      ),
      check.errors,
    )
  }

  @Test
  fun `a finding about the whole view has no ids`() {
    val check =
      ViewCheck.of(proposal, treeOf(error(FindingRule.Offscreen, null, "content cut off")))
    assertEquals(listOf(CheckError("offscreen", emptyList(), "content cut off")), check.errors)
  }

  @Test
  fun `a render that threw fails with its first line and no layout`() {
    val check = ViewCheck.renderError(IllegalStateException("boom\nat Foo.kt:1"))
    assertEquals(false, check.pass)
    assertEquals(listOf(CheckError("render-error", detail = "boom")), check.errors)
    assertNull(check.layout)
  }
}
