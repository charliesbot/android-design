package bot.charlies.androiddesign.preview.feature.preview

import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.FindingRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * A `Column` that does not fill its own width (a list item's trailing slot, an unweighted child of
 * a `Row`) wraps its children instead of stretching them. A stretched `Row` inside it would take
 * every pixel the slot offers and squeeze its neighbors to nothing.
 */
class WrapHostedStretchRenderTest {
  private fun Bounds.right() = x + width

  private fun trailingColumn(supporting: String = ""): String =
    """{"id":"root","component":"Scaffold","content":"list"},""" +
      """{"id":"list","component":"List","children":["item"]},""" +
      """{"id":"item","component":"ListItem","headline":"Crispy fish tacos"$supporting,"trailing":"slot"},""" +
      """{"id":"slot","component":"Column","horizontalAlignment":"end","children":["time","rating"]},""" +
      """{"id":"time","component":"Text","text":"35 min"},""" +
      """{"id":"rating","component":"Row","children":["star","score"]},""" +
      """{"id":"star","component":"Icon","name":"star","size":16,"contentDescription":"Rating"},""" +
      """{"id":"score","component":"Text","text":"4.6"}"""

  @Test
  fun `a list item's trailing column wraps its row and keeps the headline readable`() {
    val tree = render(singleScreen(trailingColumn())).tree
    val slot = tree.node("slot").bounds
    val time = tree.node("time").bounds
    val rating = tree.node("rating").bounds
    val widest = maxOf(time.width, rating.width)
    assertTrue(slot.width <= widest + 1f, "the column ($slot) is no wider than its widest child")
    assertEquals(slot.right(), rating.right(), 0.5f, "the row is end-aligned in the column")
    assertEquals(emptyList(), tree.findings.filter { it.rule == FindingRule.TextClipped })
    val headline = tree.node("item").text
    val ink = headline?.ink
    assertTrue(ink != null && ink.width > 1f, "the headline drew glyphs: $headline")
    assertTrue(ink.right() <= slot.x + 0.5f, "the headline ($ink) sits left of the column ($slot)")
  }

  @Test
  fun `a list item with supporting text keeps its headline whole beside a trailing column`() {
    val tree = render(singleScreen(trailingColumn(""","supporting":"Lime crema""""))).tree
    val item = tree.node("item").text
    assertTrue(item != null && !item.truncated, "the headline is not ellipsized: $item")
    assertEquals(emptyList(), tree.findings.filter { it.rule == FindingRule.TextTruncated })
  }

  private fun inRow(weight: String): String =
    """{"id":"root","component":"Scaffold","content":"line"},""" +
      """{"id":"line","component":"Row","children":["pane","filler"]},""" +
      """{"id":"pane","component":"Column","horizontalAlignment":"end"$weight,"children":["label","bar"]},""" +
      """{"id":"label","component":"Text","text":"Label"},""" +
      """{"id":"bar","component":"Row","children":["a","b"]},""" +
      """{"id":"a","component":"Text","text":"A"},""" +
      """{"id":"b","component":"Text","text":"B"},""" +
      """{"id":"filler","component":"Text","text":"Beside","weight":1}"""

  @Test
  fun `an unweighted column in a row wraps its children`() {
    val tree = render(singleScreen(inRow(""))).tree
    val pane = tree.node("pane").bounds
    val bar = tree.node("bar").bounds
    assertTrue(pane.width < 100f, "the column ($pane) wraps")
    assertTrue(bar.width < pane.width, "the row ($bar) wraps instead of stretching to the column")
    assertEquals(pane.right(), bar.right(), 0.5f, "the column's alignment puts the row at the end")
  }

  @Test
  fun `a weighted column in a row fills its share and stretches its children`() {
    val tree = render(singleScreen(inRow(""","weight":1"""))).tree
    val pane = tree.node("pane").bounds
    val bar = tree.node("bar").bounds
    assertTrue(pane.width > 100f, "the column ($pane) fills its share")
    assertEquals(pane.width, bar.width, 0.5f, "the row stretches to the column")
  }
}
