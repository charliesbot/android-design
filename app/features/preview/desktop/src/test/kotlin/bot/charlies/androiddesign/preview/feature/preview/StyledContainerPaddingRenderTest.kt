package bot.charlies.androiddesign.preview.feature.preview

import bot.charlies.androiddesign.preview.feature.preview.theme.ThemeResolver
import bot.charlies.androiddesign.preview.feature.preview.theme.toHex
import bot.charlies.androiddesign.preview.model.ColorRole
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * A style's padding sits inside its container: the background and clip surround the padding, as the
 * exporter writes them (`clip`, `background`, then `padding`). The tree reports the inner box.
 */
class StyledContainerPaddingRenderTest {
  private val scale = 2 // the png is two pixels per dp
  private val padding = 24f // the `xl` token

  private val theme =
    """{"material":{"styles":{"pane":{"container":"tertiaryContainer","shape":"extraLarge","padding":"xl"}}}}"""

  private fun containerRgb(): Int =
    ThemeResolver.resolve(
        designOf(proposalOf("home" to screenJson("{}"), theme = theme).theme),
        dark = false,
      )
      .color(ColorRole.TertiaryContainer)
      .toHex()
      .removePrefix("#")
      .toInt(16)

  private fun pixel(image: BufferedImage, xDp: Float, yDp: Float) =
    image.getRGB((xDp * scale).toInt(), (yDp * scale).toInt()) and 0xFFFFFF

  private fun distance(a: Int, b: Int) =
    listOf(16, 8, 0).sumOf { abs(((a shr it) and 0xFF) - ((b shr it) and 0xFF)) }

  @Test fun `a column's container surrounds its padding`() = assertSurrounds("Column")

  @Test fun `a row's container surrounds its padding`() = assertSurrounds("Row")

  private fun assertSurrounds(layout: String) {
    val proposal =
      proposalOf(
        "home" to
          screenJson(
            """{"id":"root","component":"Scaffold","content":"l"},""" +
              """{"id":"l","component":"$layout","style":"pane","children":["t"]},""" +
              """{"id":"t","component":"Text","text":"Hello"}""",
          ),
        theme = theme,
      )
    val result = render(proposal)
    val l = result.tree.node("l")
    val t = result.tree.node("t")
    val image = ImageIO.read(ByteArrayInputStream(result.png))
    val color = containerRgb()
    val midY = l.bounds.y + l.bounds.height / 2
    val left = l.bounds.x - padding

    assertEquals(l.bounds.x, t.bounds.x, "$layout: the child starts at the inner box")
    assertTrue(left >= 0f, "$layout: the container starts on screen")
    // Just inside the container's left edge, in the padding: the container color, not the page.
    assertTrue(
      distance(color, pixel(image, left + 2f, midY)) <= 12,
      "$layout: padding is drawn on the container, got #%06X".format(pixel(image, left + 2f, midY)),
    )
    // Just left of the child: still the container, so the content is inset from its edge.
    assertTrue(
      distance(color, pixel(image, t.bounds.x - 2f, t.bounds.y + t.bounds.height / 2)) <= 12,
      "$layout: the child is inset from the container edge",
    )
  }
}
