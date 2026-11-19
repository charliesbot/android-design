package bot.charlies.androiddesign.preview.domain.findings

import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.TextInfo
import bot.charlies.androiddesign.preview.model.Views
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FindingGuidanceTest {
  @Test
  fun `every FindingRule has a non-blank reference anchor and actionable hint`() {
    val anchorPattern = Regex("""^[a-z0-9-]+\.md#[a-z0-9-]+$""")
    for (rule in FindingRule.entries) {
      assertTrue(
        anchorPattern.matches(rule.reference),
        "${rule.wire} reference '${rule.reference}' must match <file>.md#<anchor>",
      )
      assertTrue(rule.hint.isNotBlank(), "${rule.wire} hint must not be blank")
      assertTrue('\u2014' !in rule.hint, "${rule.wire} hint must not contain an em-dash")
    }
    assertEquals("layout.md#grids-and-max-widths", FindingRule.LineLength.reference)
  }

  @Test
  fun `evaluate and ViewCheck attach reference pointers and hints to findings`() {
    val wideLine =
      node(
        id = "body",
        type = "Text",
        w = 700f,
        h = 48f,
        text =
          TextInfo(
            value = "A".repeat(140),
            lines = 2,
            overflow = false,
            truncated = false,
          ),
      )
    val tree = LayoutTree("home", Views.Desktop, listOf(wideLine), emptyList())
    val findings = FindingRules.evaluate(tree)
    val lineFinding = findings.firstOrNull { it.rule == FindingRule.LineLength }
    assertNotNull(lineFinding, "expected line-length finding, got $findings")
    assertEquals("layout.md#grids-and-max-widths", lineFinding.reference)
    assertNotNull(lineFinding.hint)

    val viewCheck = ViewCheck.of(tree.copy(findings = findings))
    val err = viewCheck.errors.firstOrNull { it.rule == "line-length" }
    assertNotNull(err)
    assertEquals("layout.md#grids-and-max-widths", err.reference)
    assertNotNull(err.hint)
  }
}
