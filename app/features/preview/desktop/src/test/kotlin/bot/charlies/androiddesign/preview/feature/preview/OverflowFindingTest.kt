package bot.charlies.androiddesign.preview.feature.preview

import bot.charlies.androiddesign.preview.model.FindingRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The tree's bounds come from semantics, which clip to the window, so a row laid out wider than the
 * window looks like it fits. The overflow finding reads the layout box the recorder keeps beside
 * them.
 */
class OverflowFindingTest {
  private fun icons(count: Int) = (1..count).joinToString(",") { "\"i$it\"" }

  private fun iconDefs(count: Int) =
    (1..count).joinToString(",") { """{"id":"i$it","component":"IconButton","icon":"home"}""" }

  private fun gridOfRow(parent: String, count: Int) =
    """{"id":"root","component":"Scaffold","content":"body"},""" +
      """{"id":"body","component":"Column","children":["g"]},""" +
      """{"id":"g","component":"Grid","columns":1,"padding":"sm","children":["r"]},""" +
      """{"id":"r","component":"$parent","children":[${icons(count)}]},""" +
      iconDefs(count)

  @Test
  fun `a row laid out wider than the window is reported with its overflow`() {
    val tree = render(singleScreen(gridOfRow("Row", 10))).tree
    val finding = tree.findings.single { it.rule == FindingRule.Overflow && it.id == "r" }
    assertTrue(finding.detail.orEmpty().contains("dp"), finding.detail)
    assertNotNull(tree.node("r").layout, "the unclipped layout box is recorded")
  }

  @Test
  fun `a row that fits is not reported`() {
    val tree = render(singleScreen(gridOfRow("Row", 3))).tree
    assertEquals(emptyList(), tree.findings.filter { it.rule == FindingRule.Overflow })
  }

  /** An open menu is wider than the icon button it is anchored to, and is meant to be. */
  @Test
  fun `an open menu anchored to an icon button is not reported against its anchor`() {
    val proposal =
      singleScreen(
        """{"id":"root","component":"Scaffold","content":"stage"},""" +
          """{"id":"stage","component":"Row","padding":"lg","children":["more"]},""" +
          """{"id":"more","component":"IconButton","icon":"home","contentDescription":"More","action":{"functionCall":{"@call":"open","args":{"overlay":"menu"}}}},""" +
          """{"id":"menu","component":"Menu","anchor":"more","items":["a","b"]},""" +
          """{"id":"a","component":"MenuItem","label":"Share this plate with a friend"},""" +
          """{"id":"b","component":"MenuItem","label":"Delete"}""",
      )
    val tree = render(proposal) { it.interaction.open("home", "menu") }.tree
    assertTrue(tree.has("menu"), "the menu is open: ${tree.nodes.map { it.id }}")
    assertEquals(emptyList(), tree.findings.filter { it.rule == FindingRule.Overflow })
  }
}
