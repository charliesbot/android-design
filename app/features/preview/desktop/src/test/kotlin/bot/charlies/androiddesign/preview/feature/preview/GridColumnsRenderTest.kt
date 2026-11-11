package bot.charlies.androiddesign.preview.feature.preview

import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.LayoutNode
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.Base64
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * A Grid's equal columns must not grow to an image's bitmap width: a bare `1.fr` track has a
 * min-content minimum, which for an image is its pixel width.
 */
class GridColumnsRenderTest {
  /** A 1600 x 900 bitmap, wider in pixels than any phone view. */
  private val wide: String by lazy {
    val out = ByteArrayOutputStream()
    ImageIO.write(BufferedImage(1600, 900, BufferedImage.TYPE_INT_RGB), "png", out)
    "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray())
  }

  private fun gridWith(first: String, vararg extra: String, grid: String = """"columns":2""") =
    """{"id":"root","component":"Scaffold","content":"body"},""" +
      """{"id":"body","component":"Column","children":["grid"]},""" +
      """{"id":"grid","component":"Grid",$grid,"children":["cellA","cellB"]},""" +
      first +
      """,{"id":"cellB","component":"Text","text":"Second cell"}""" +
      extra.joinToString("") { ",$it" }

  private fun assertEqualColumnsInsideGrid(components: String, firstCell: String) {
    val tree = render(singleScreen(components)).tree
    val grid = tree.node("grid").bounds
    val a: LayoutNode = tree.node(firstCell)
    val b = tree.node("cellB")
    assertEquals(emptyList(), tree.findings.filter { it.rule == FindingRule.Overflow })
    assertTrue(
      a.bounds.x >= grid.x - 0.5f && a.bounds.x + a.bounds.width <= grid.x + grid.width + 0.5f,
      "first cell ${a.bounds} sits inside the grid $grid",
    )
    assertTrue(
      b.bounds.width > 0f && b.bounds.x + b.bounds.width <= grid.x + grid.width + 0.5f,
      "second cell ${b.bounds} sits inside the grid $grid",
    )
    // The second cell is a Text that wraps to its own width, so the columns show in where the
    // second one starts: at the grid's midpoint, with the first cell filling its half.
    assertEquals(grid.x + grid.width / 2, b.bounds.x, 1f, "the second column starts mid-grid")
    assertEquals(grid.width / 2, a.bounds.width, 1f, "the first cell fills half the grid")
  }

  @Test
  fun `an image wider than the view does not widen its grid column`() {
    assertEqualColumnsInsideGrid(
      gridWith("""{"id":"cellA","component":"Image","src":"$wide","aspectRatio":1.7778}"""),
      "cellA",
    )
  }

  @Test
  fun `a row holding a weighted image does not widen its grid column`() {
    val components =
      gridWith(
        """{"id":"cellA","component":"Row","children":["pic","label"]}""",
        """{"id":"pic","component":"Image","src":"$wide","aspectRatio":1.7778,"weight":1}""",
        """{"id":"label","component":"Text","text":"Caption","weight":1}""",
      )
    assertEqualColumnsInsideGrid(components, "cellA")
  }

  @Test
  fun `a grid sized by minColumnWidth keeps its columns equal around a wide image`() {
    val components =
      gridWith(
        """{"id":"cellA","component":"Image","src":"$wide","aspectRatio":1.7778}""",
        grid = """"minColumnWidth":160""",
      )
    assertEqualColumnsInsideGrid(components, "cellA")
  }
}
