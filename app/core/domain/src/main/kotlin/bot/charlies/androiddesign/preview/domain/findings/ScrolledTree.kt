package bot.charlies.androiddesign.preview.domain.findings

import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree

/**
 * The layout tree of a whole scrolling list. A lazy list composes only the items on screen, so one
 * tree covers one scroll position. A render that scrolls the list from top to end takes a tree at
 * each position and merges them here, so the findings cover every item.
 */
object ScrolledTree {
  /**
   * One tree from [positions], the trees taken at each scroll position from the top down. A node
   * seen at several positions is kept once, as it was where it lay fully inside the view and was
   * tallest (the earlier one on a tie), else as it first appeared. Nodes keep document order: a
   * node new at one position goes after the node before it at that position. Findings are evaluated
   * again over the merged nodes, whose bounds are the screen coordinates of the position each was
   * taken at.
   */
  fun merge(positions: List<LayoutTree>): LayoutTree {
    val first = positions.first()
    val width = first.view.width.toFloat()
    val height = first.view.height.toFloat()
    val merged = ArrayList<LayoutNode>()
    val index = HashMap<String, Int>()
    for (position in positions) {
      var after = -1
      for (node in position.nodes) {
        val at = index[node.id]
        if (at == null) {
          after += 1
          merged.add(after, node)
          // Every node at or after the slot moved down by one.
          index.replaceAll { _, i -> if (i >= after) i + 1 else i }
          index[node.id] = after
        } else {
          if (node.fullyInside(width, height) && node.betterThan(merged[at], width, height)) {
            merged[at] = node
          }
          after = at
        }
      }
    }
    val tree = first.copy(nodes = merged, findings = emptyList())
    return tree.copy(findings = FindingRules.evaluate(tree))
  }

  /**
   * Whether this appearance should replace [kept]: it lies fully inside the view and [kept] does
   * not, or both do and this one is taller. A list item at the display's edge is shrunk and an edge
   * button grows as the list reaches its end, so the tallest appearance is the node at full size.
   */
  private fun LayoutNode.betterThan(kept: LayoutNode, width: Float, height: Float): Boolean =
    !kept.fullyInside(width, height) || bounds.height > kept.bounds.height

  private fun LayoutNode.fullyInside(width: Float, height: Float): Boolean =
    isLaidOut &&
      bounds.x >= -SLACK_DP &&
      bounds.y >= -SLACK_DP &&
      bounds.x + bounds.width <= width + SLACK_DP &&
      bounds.y + bounds.height <= height + SLACK_DP

  private const val SLACK_DP = 0.5f
}
