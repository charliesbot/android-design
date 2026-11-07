package bot.charlies.androiddesign.preview.domain.findings

import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.Views
import kotlin.test.Test
import kotlin.test.assertEquals

class ScrolledTreeTest {
  private val view = Views.Watch

  private fun ids(tree: LayoutTree) = tree.nodes.map { it.id }

  @Test
  fun `a node seen at two scroll positions is kept once, where it is fully on screen`() {
    val top =
      tree(
        view,
        node("root", "Screen", w = 204f, h = 204f, scrolls = true),
        node("a", "Button", y = 20f, w = 100f, h = 40f, parent = "root"),
        // Cut by the bottom edge at the top of the list.
        node("b", "Button", y = 180f, w = 100f, h = 40f, parent = "root"),
      )
    val scrolled =
      tree(
        view,
        node("root", "Screen", w = 204f, h = 204f, scrolls = true),
        node("b", "Button", y = 100f, w = 100f, h = 40f, parent = "root"),
        node("c", "Button", y = 150f, w = 100f, h = 40f, parent = "root"),
      )

    val merged = ScrolledTree.merge(listOf(top, scrolled))

    assertEquals(listOf("root", "a", "b", "c"), ids(merged))
    assertEquals(100f, merged.nodes.single { it.id == "b" }.bounds.y)
    assertEquals(20f, merged.nodes.single { it.id == "a" }.bounds.y)
  }

  @Test
  fun `a node never fully on screen keeps its first appearance`() {
    val first = tree(view, node("tall", "Card", y = 50f, w = 100f, h = 300f))
    val second = tree(view, node("tall", "Card", y = -100f, w = 100f, h = 300f))

    val merged = ScrolledTree.merge(listOf(first, second))

    assertEquals(50f, merged.nodes.single().bounds.y)
  }

  @Test
  fun `a node seen fully on screen twice keeps the first when both are as tall`() {
    val first = tree(view, node("a", "Button", y = 10f, w = 100f, h = 40f))
    val second = tree(view, node("a", "Button", y = 30f, w = 100f, h = 40f))

    assertEquals(10f, ScrolledTree.merge(listOf(first, second)).nodes.single().bounds.y)
  }

  @Test
  fun `a node that grows or is shrunk at an edge keeps its full size`() {
    val growing =
      listOf(40f, 49f, 56f, 52f).mapIndexed { i, h ->
        tree(view, node("start", "EdgeButton", y = 204f - h - i, w = 100f, h = h))
      }

    val merged = ScrolledTree.merge(growing)

    assertEquals(56f, merged.nodes.single().bounds.height)
  }

  @Test
  fun `the edge button that only appears at the end comes after the items`() {
    val top =
      tree(
        view,
        node("root", "Screen", w = 204f, h = 204f, scrolls = true),
        node("row0", "Button", y = 20f, parent = "root"),
        node("row1", "Button", y = 70f, parent = "root"),
      )
    val end =
      tree(
        view,
        node("root", "Screen", w = 204f, h = 204f, scrolls = true),
        node("row1", "Button", y = 20f, parent = "root"),
        node("row2", "Button", y = 70f, parent = "root"),
        node("done", "EdgeButton", y = 160f, w = 100f, h = 44f, parent = "root"),
      )

    assertEquals(
      listOf("root", "row0", "row1", "row2", "done"),
      ids(ScrolledTree.merge(listOf(top, end))),
    )
  }

  @Test
  fun `findings are computed again over the merged nodes`() {
    val top =
      tree(
        view,
        node("a", "Button", y = 20f, w = 100f, h = 40f, touch = Bounds(0f, 16f, 100f, 48f)),
      )
    val end = tree(view, node("z", "Button", y = 20f, w = 100f, h = 20f))

    val merged = ScrolledTree.merge(listOf(top, end))

    // The 20 dp tall button is below the 48 dp touch target only once it is part of the tree.
    assertEquals(
      listOf("z"),
      merged.findings.filter { it.rule.wire == "touch-target" }.mapNotNull { it.id },
    )
  }

  @Test
  fun `one tree merges to itself`() {
    val only = tree(view, node("a", "Button"))
    assertEquals(listOf("a"), ids(ScrolledTree.merge(listOf(only))))
  }
}
