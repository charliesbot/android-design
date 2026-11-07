package bot.charlies.androiddesign.preview.feature.preview

import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.Views
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** What the tree says about tabs and carousels matches what is drawn. */
class TabsAndCarouselRenderTest {
  private fun tabs(variant: String) =
    singleScreen(
      """{"id":"root","component":"Scaffold","content":"row"},""" +
        """{"id":"row","component":"Tabs","variant":"$variant","children":["a","b"]},""" +
        """{"id":"a","component":"Tab","label":"Plates","selected":true},""" +
        """{"id":"b","component":"Tab","label":"Menus"}""",
    )

  /**
   * The pixel of [node]'s label band farthest from the [backdrop], the text's fully covered color.
   */
  private fun drawnColor(png: ByteArray, node: LayoutNode, backdrop: String): Int {
    val image = ImageIO.read(ByteArrayInputStream(png))
    val scale = 2 // the png is two pixels per dp
    val bg = backdrop.removePrefix("#").toInt(16)
    fun distance(rgb: Int) =
      listOf(16, 8, 0).sumOf { abs(((rgb shr it) and 0xFF) - ((bg shr it) and 0xFF)) }
    var best = bg
    // The tab's middle band holds only its label, not the indicator or the divider at its foot.
    val left = (node.bounds.x * scale).toInt()
    val top = ((node.bounds.y + 12) * scale).toInt()
    val right = ((node.bounds.x + node.bounds.width) * scale).toInt().coerceAtMost(image.width)
    val bottom = ((node.bounds.y + 36) * scale).toInt().coerceAtMost(image.height)
    for (x in left until right) {
      for (y in top until bottom) {
        val rgb = image.getRGB(x, y) and 0xFFFFFF
        if (distance(rgb) > distance(best)) best = rgb
      }
    }
    return best
  }

  private fun assertDrawnAsDescribed(png: ByteArray, node: LayoutNode) {
    val expected = checkNotNull(node.colors?.content).removePrefix("#").toInt(16)
    val drawn = drawnColor(png, node, checkNotNull(node.colors?.container))
    val off =
      listOf(16, 8, 0).maxOf { abs(((drawn shr it) and 0xFF) - ((expected shr it) and 0xFF)) }
    assertTrue(off <= 16, "${node.id}: tree says #%06X, drawn #%06X".format(expected, drawn))
  }

  @Test
  fun `a tab's described color is the color it is drawn in, in both variants`() {
    for (variant in listOf("primary", "secondary")) {
      val result = render(tabs(variant))
      assertDrawnAsDescribed(result.png, result.tree.node("a"))
      assertDrawnAsDescribed(result.png, result.tree.node("b"))
    }
  }

  @Test
  fun `a tab's described color follows the tab last tapped`() {
    for (variant in listOf("primary", "secondary")) {
      val result =
        render(tabs(variant)) {
          onNodeWithTag("b", useUnmergedTree = true).performClick()
          settleForTest()
        }
      val tree = result.tree
      val selected = if (variant == "primary") "primary" else "onSurface"
      assertEquals(tree.node("b").roles?.content, selected)
      assertEquals(tree.node("a").roles?.content, "onSurfaceVariant")
      assertDrawnAsDescribed(result.png, tree.node("a"))
      assertDrawnAsDescribed(result.png, tree.node("b"))
    }
  }

  private fun carousel(items: Int, itemWidth: Int): LayoutNodes {
    val ids = (1..items).joinToString(",") { "\"c$it\"" }
    val cards =
      (1..items).joinToString(",") {
        """{"id":"c$it","component":"Card","content":["x$it"]},""" +
          """{"id":"x$it","component":"Text","text":"Item $it","maxLines":1,"overflow":"ellipsis"}"""
      }
    val proposal =
      singleScreen(
        """{"id":"root","component":"Scaffold","content":"stage"},""" +
          """{"id":"stage","component":"Column","children":["gallery"]},""" +
          """{"id":"gallery","component":"Carousel","itemWidth":$itemWidth,"children":[$ids]},$cards""",
      )
    return LayoutNodes(render(proposal, view = Views.Light).tree.findings)
  }

  private class LayoutNodes(val findings: List<bot.charlies.androiddesign.preview.model.Finding>) {
    val items = findings.filter { it.rule == FindingRule.CarouselItems }
  }

  @Test
  fun `a multi-browse carousel of three text items shows no carousel-items finding at 411dp`() {
    assertEquals(emptyList(), carousel(items = 3, itemWidth = 186).items)
  }

  @Test
  fun `items a carousel keeps past its last visible one are not counted`() {
    // Six items at the default width still show two large and one small.
    assertEquals(emptyList(), carousel(items = 6, itemWidth = 186).items)
  }

  @Test
  fun `a carousel that really shows more than three text items is flagged`() {
    // Four 90dp items and a peeking one show; the sixth is laid out inside the fifth.
    val finding = carousel(items = 6, itemWidth = 90).items.single()
    assertEquals("gallery", finding.id)
    assertTrue(finding.detail.orEmpty().startsWith("5 items with text show at 411 dp"))
  }

  /** Three cards in a multi-browse carousel: each has a `large`-gated title and a plain label. */
  private fun gatedCarousel(gate: String): LayoutTree {
    val cards =
      (1..3).joinToString(",") {
        """{"id":"c$it","component":"Card","content":["col$it"]},""" +
          """{"id":"col$it","component":"Column","children":["title$it","label$it"]},""" +
          """{"id":"title$it","component":"Text","text":"Title $it","maxLines":1,"itemSize":"$gate"},""" +
          """{"id":"label$it","component":"Text","text":"Label $it","maxLines":1}"""
      }
    val proposal =
      singleScreen(
        """{"id":"root","component":"Scaffold","content":"stage"},""" +
          """{"id":"stage","component":"Column","children":["gallery"]},""" +
          """{"id":"gallery","component":"Carousel","itemWidth":186,"children":["c1","c2","c3"]},$cards""",
      )
    return render(proposal, view = Views.Light).tree
  }

  @Test
  fun `a large-gated node shows on the large item and not on the small one`() {
    val tree = gatedCarousel("large")
    assertTrue(tree.has("title1"), "the first item is large: ${tree.nodes.map { it.id }}")
    assertTrue(tree.has("label3"), "the small item still draws its ungated label")
    assertFalse(tree.has("title3"), "the small item hides the title: ${tree.nodes.map { it.id }}")
  }

  @Test
  fun `a medium-gated node shows on the large item and not on the small one`() {
    val tree = gatedCarousel("medium")
    assertTrue(tree.has("title1"))
    assertFalse(tree.has("title3"), "the small item hides the title: ${tree.nodes.map { it.id }}")
  }

  /** A multi-browse carousel whose items are a Column holding one Text with [textProps]. */
  private fun croppedCarousel(textProps: String, itemProps: String = ""): LayoutTree {
    val items =
      (1..3).joinToString(",") {
        """{"id":"col$it","component":"Column","children":["name$it"]$itemProps},""" +
          """{"id":"name$it","component":"Text","text":"Shakshuka $it","maxLines":1$textProps}"""
      }
    val proposal =
      singleScreen(
        """{"id":"root","component":"Scaffold","content":"stage"},""" +
          """{"id":"stage","component":"Column","children":["gallery"]},""" +
          """{"id":"gallery","component":"Carousel","itemWidth":186,"children":["col1","col2","col3"]},$items""",
      )
    return render(proposal, view = Views.Light).tree
  }

  private fun LayoutTree.cropped() = findings.filter {
    it.rule == FindingRule.TextClipped && it.id == "name1"
  }

  @Test
  fun `the tree records a carousel item as its mask shows it, with the layout box kept`() {
    val item = croppedCarousel("").node("col1")
    val layout = checkNotNull(item.layout) { "the item is laid out wider than its mask" }
    assertTrue(layout.width > item.bounds.width + 1, "mask ${item.bounds}, layout $layout")
  }

  @Test
  fun `text at the start edge of a large carousel item is reported as cropped by its mask`() {
    val finding = croppedCarousel("").cropped().single()
    assertTrue("carousel item" in finding.detail.orEmpty(), finding.detail)
    assertTrue("mask" in finding.detail.orEmpty(), finding.detail)
  }

  @Test
  fun `text padded in from the edge of a carousel item is not reported`() {
    assertEquals(emptyList(), croppedCarousel(""","padding":"md"""").cropped())
  }

  @Test
  fun `an item with its own padding is placed by its outer box, not its padded content`() {
    val tree = croppedCarousel("", itemProps = ""","padding":"md"""")
    val item = tree.node("col1")
    val gallery = tree.node("gallery")
    assertTrue(
      kotlin.math.abs(item.bounds.y - gallery.bounds.y) < 1f,
      "item ${item.bounds} should start at the carousel's top ${gallery.bounds}",
    )
    // The text sits inside the padding, so the mask does not crop it.
    assertEquals(emptyList(), tree.cropped())
  }

  @Test
  fun `a centered text whose glyphs are inside the item is not reported`() {
    assertEquals(emptyList(), croppedCarousel(""","align":"center"""").cropped())
  }

  private fun androidx.compose.ui.test.ComposeUiTest.settleForTest() {
    with(bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessRenderer) { settle() }
  }
}
