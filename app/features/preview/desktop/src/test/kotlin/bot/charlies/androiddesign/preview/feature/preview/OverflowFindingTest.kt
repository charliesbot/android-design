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
}
