package bot.charlies.androiddesign.preview.feature.preview

import bot.charlies.androiddesign.preview.model.Axis
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.LayoutTree
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A `weight` inside a scrolling Column is given no height to share, so the weighted child draws at
 * zero and the tree reports `weight-collapsed`. In a bounded Column the same weight fills it.
 */
class WeightCollapsedRenderTest {
  private fun LayoutTree.collapsed() =
    findings.filter { it.rule == FindingRule.WeightCollapsed }.map { it.id }

  private fun columnWith(scroll: Boolean): LayoutTree =
    render(
        singleScreen(
          """{"id":"root","component":"Scaffold","content":"outer"},""" +
            """{"id":"outer","component":"Column","children":["col"]},""" +
            """{"id":"col","component":"Column","scroll":$scroll,"children":["t","list"]},""" +
            """{"id":"t","component":"Text","text":"Title"},""" +
            """{"id":"list","component":"Column","weight":1,"children":["item"]},""" +
            """{"id":"item","component":"Text","text":"Row"}""",
        ),
      )
      .tree

  @Test
  fun `a weighted child of a scrolling column collapses and is reported`() {
    val tree = columnWith(scroll = true)
    assertEquals(Axis.Height, tree.node("list").weight?.axis)
    assertEquals(listOf("list"), tree.collapsed())
  }

  @Test
  fun `a weighted child of a bounded column takes the space left`() {
    val tree = columnWith(scroll = false)
    assertEquals(Axis.Height, tree.node("list").weight?.axis)
    assertEquals(emptyList(), tree.collapsed())
  }

  @Test
  fun `a child of a Row records the width axis`() {
    val tree =
      render(
          singleScreen(
            """{"id":"root","component":"Scaffold","content":"row"},""" +
              """{"id":"row","component":"Row","children":["a","b"]},""" +
              """{"id":"a","component":"Text","text":"A","weight":1},""" +
              """{"id":"b","component":"Text","text":"B"}""",
          ),
        )
        .tree
    assertEquals(Axis.Width, tree.node("a").weight?.axis)
    assertEquals(null, tree.node("b").weight)
    assertEquals(emptyList(), tree.collapsed())
  }

  private fun spacerColumn(lines: Int): LayoutTree {
    val texts = (0 until lines).map { "t$it" }
    val children = (texts + listOf("sp", "btn")).joinToString(",") { "\"$it\"" }
    return render(
        singleScreen(
          """{"id":"root","component":"Scaffold","content":"col"},""" +
            """{"id":"col","component":"Column","scroll":true,"children":[$children]},""" +
            texts.joinToString(",") { """{"id":"$it","component":"Text","text":"Line $it"}""" } +
            """,""" +
            """{"id":"sp","component":"Spacer","weight":1},""" +
            """{"id":"btn","component":"Button","label":"Continue"}""",
        ),
      )
      .tree
  }

  @Test
  fun `a weighted Spacer is not reported with short content`() {
    assertEquals(emptyList(), spacerColumn(lines = 3).collapsed())
  }

  @Test
  fun `a weighted Spacer is not reported with content taller than the window`() {
    assertEquals(emptyList(), spacerColumn(lines = 60).collapsed())
  }

  /** A scroll Column of [lines] Texts, then a Row of two buttons that share its width. */
  private fun scrollWithRow(lines: Int): LayoutTree {
    val texts = (0 until lines).map { "t$it" }
    val children = (texts + "row").joinToString(",") { "\"$it\"" }
    return render(
        singleScreen(
          """{"id":"root","component":"Scaffold","content":"col"},""" +
            """{"id":"col","component":"Column","scroll":true,"children":[$children]},""" +
            texts.joinToString(",") { """{"id":"$it","component":"Text","text":"Line $it"}""" } +
            """,""" +
            """{"id":"row","component":"Row","children":["a","b"]},""" +
            """{"id":"a","component":"Button","label":"Cancel","weight":1},""" +
            """{"id":"b","component":"Button","label":"Save","weight":1}""",
        ),
      )
      .tree
  }

  @Test
  fun `weighted buttons below the fold of a scrolling column are not reported`() {
    val tree = scrollWithRow(lines = 60)
    assertEquals(Axis.Width, tree.node("a").weight?.axis)
    assertEquals(emptyList(), tree.collapsed())
  }

  @Test
  fun `weighted buttons above the fold are not reported`() {
    assertEquals(emptyList(), scrollWithRow(lines = 3).collapsed())
  }

  @Test
  fun `a weighted Column collapses once the scrolling content is taller than the window`() {
    val texts = (0 until 60).map { "t$it" }
    val children = (texts + "list").joinToString(",") { "\"$it\"" }
    val tree =
      render(
          singleScreen(
            """{"id":"root","component":"Scaffold","content":"col"},""" +
              """{"id":"col","component":"Column","scroll":true,"children":[$children]},""" +
              texts.joinToString(",") { """{"id":"$it","component":"Text","text":"Line $it"}""" } +
              """,""" +
              """{"id":"list","component":"Column","weight":1,"children":["item"]},""" +
              """{"id":"item","component":"Text","text":"Row"}""",
          ),
        )
        .tree
    assertEquals(listOf("list"), tree.collapsed())
  }
}
