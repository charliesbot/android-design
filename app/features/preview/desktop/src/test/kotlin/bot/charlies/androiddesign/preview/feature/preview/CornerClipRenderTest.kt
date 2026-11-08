package bot.charlies.androiddesign.preview.feature.preview

import bot.charlies.androiddesign.preview.model.CornerClip
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.LayoutTree
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * A Card and a styled container record the rounded shape they clip their content to, so the
 * corner-clipped finding can tell when that shape cuts a text or a control.
 */
class CornerClipRenderTest {
  private fun LayoutTree.cornerClipped() =
    findings.filter { it.rule == FindingRule.CornerClipped }.map { it.id }

  private fun cardWith(padding: String?): LayoutTree {
    val pad = padding?.let { ""","padding":"$it"""" }.orEmpty()
    return render(
        singleScreen(
          """{"id":"root","component":"Scaffold","content":"c"},""" +
            """{"id":"c","component":"Card"$pad,"content":["t"]},""" +
            """{"id":"t","component":"Text","text":"Notifications"}""",
        ),
      )
      .tree
  }

  @Test
  fun `a card with no padding cuts the text flush against its corner`() {
    val tree = cardWith(padding = null)
    assertEquals(CornerClip(12f), tree.node("c").clip)
    assertEquals(listOf("t"), tree.cornerClipped())
  }

  @Test
  fun `a padded card keeps its text clear of the corner`() {
    assertEquals(emptyList(), cardWith(padding = "lg").cornerClipped())
  }

  @Test
  fun `a styled container records its shape around its padding`() {
    val theme =
      """{"material":{"styles":{"pill":{"container":"tertiaryContainer","shape":"full","padding":"xxs"}}}}"""
    val tree =
      render(
          proposalOf(
            "home" to
              screenJson(
                """{"id":"root","component":"Scaffold","content":"l"},""" +
                  """{"id":"l","component":"Column","style":"pill","children":["t"]},""" +
                  """{"id":"t","component":"Text","text":"Hello"}""",
              ),
            theme = theme,
          ),
        )
        .tree
    val l = tree.node("l")
    val clip = checkNotNull(l.clip)
    assertEquals(2f, clip.outset)
    // A full shape is capped at half its shorter side.
    assertEquals(minOf(l.bounds.width, l.bounds.height) / 2 + clip.outset, clip.radius)
    assertEquals(listOf("t"), tree.cornerClipped())
  }

  @Test
  fun `a card the window cuts off records no clip`() {
    // The card runs past the bottom of the window, so its bounds end at the window's edge, where
    // no corner is drawn. Its padded content there must not be measured against that edge.
    val rows = (0 until 40).map { "r$it" }
    val tree =
      render(
          singleScreen(
            """{"id":"root","component":"Scaffold","content":"col"},""" +
              """{"id":"col","component":"Column","scroll":true,"children":["c"]},""" +
              """{"id":"c","component":"Card","shape":"extraLarge","padding":"lg",""" +
              """"content":[${rows.joinToString(",") { "\"$it\"" }}]},""" +
              rows.joinToString(",") { """{"id":"$it","component":"Text","text":"Row $it"}""" },
          ),
        )
        .tree
    assertNull(tree.node("c").clip)
    assertEquals(emptyList(), tree.cornerClipped())
  }

  @Test
  fun `a card that scrolls in a bottom sheet records no clip`() {
    // The sheet's scrolling column cuts the card, and an overlay's position is clipped as well, so
    // only the card's measured size tells that it runs past the viewport.
    val rows = (0 until 40).map { "r$it" }
    val tree =
      render(
          singleScreen(
            """{"id":"root","component":"Scaffold","content":"open"},""" +
              """{"id":"open","component":"Button","label":"Open",""" +
              """"action":{"functionCall":{"@call":"open","args":{"overlay":"sheet"}}}},""" +
              """{"id":"sheet","component":"BottomSheet","content":["col"]},""" +
              """{"id":"col","component":"Column","scroll":true,"children":["c"]},""" +
              """{"id":"c","component":"Card","shape":"extraLarge","padding":"lg",""" +
              """"content":[${rows.joinToString(",") { "\"$it\"" }}]},""" +
              rows.joinToString(",") { """{"id":"$it","component":"Text","text":"Row $it"}""" },
          ),
        ) {
          it.interaction.open("home", "sheet")
        }
        .tree
    assertNull(tree.node("c").clip)
    assertEquals(emptyList(), tree.cornerClipped())
  }

  @Test
  fun `an unstyled column clips nothing`() {
    val tree =
      render(
          singleScreen(
            """{"id":"root","component":"Scaffold","content":"l"},""" +
              """{"id":"l","component":"Column","children":["t"]},""" +
              """{"id":"t","component":"Text","text":"Hello"}""",
          ),
        )
        .tree
    assertNull(tree.node("l").clip)
  }
}
