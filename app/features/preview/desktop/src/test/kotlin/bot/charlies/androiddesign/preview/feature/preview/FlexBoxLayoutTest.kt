package bot.charlies.androiddesign.preview.feature.preview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FlexBoxLayoutTest {
  private fun inScrollingFlexBox(vararg items: String): String {
    val ids = items.indices.joinToString(",") { "\"i$it\"" }
    val defs = items.mapIndexed { index, item -> item.replace("ID", "i$index") }.joinToString(",")
    return """{"id":"root","component":"Scaffold","content":"body"},""" +
      """{"id":"body","component":"Column","scroll":true,"children":["flex"]},""" +
      """{"id":"flex","component":"FlexBox","wrap":true,"gap":"sm","children":[$ids]},$defs"""
  }

  private val linear =
    """{"id":"ID","component":"ProgressIndicator","shape":"linear","progress":0.4}"""

  /**
   * Compose's FlexBox measures a wrapped item with a finite stand-in for an unbounded height, and
   * Material components that grow their semantics bounds then overflow Constraints. The items must
   * still lay out as they would with an unbounded height.
   */
  @Test
  fun `wrapped items that grow their semantics bounds render in a scrolling column`() {
    val tree = render(singleScreen(inScrollingFlexBox(linear, linear))).tree
    val first = tree.node("i0").bounds
    val second = tree.node("i1").bounds
    assertEquals(first.width, second.width)
    assertEquals(first.x, second.x)
    assertTrue(second.y > first.y, "the second indicator wraps below the first: $first $second")
  }
}
