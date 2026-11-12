package bot.charlies.androiddesign.preview.feature.preview

import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.Severity
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * An arrangement that spreads children needs free space on its main axis. A Row that wraps its
 * content has none, so `spaceBetween` leaves its children flush and the tree notes it.
 */
class ArrangementNoSpaceRenderTest {
  private fun LayoutTree.noSpace() =
    findings.filter { it.rule == FindingRule.ArrangementNoSpace }.map { it.id }

  private val header =
    """{"id":"head","component":"Row","arrangement":"spaceBetween","children":["l","r"]},""" +
      """{"id":"l","component":"Text","text":"Route"},""" +
      """{"id":"r","component":"Text","text":"Up to 5,364 m"}"""

  /** The trek shape: an unweighted Column in a Row wraps, and its header Row wraps with it. */
  private fun pane(weight: String): LayoutTree =
    render(
        singleScreen(
          """{"id":"root","component":"Scaffold","content":"split"},""" +
            """{"id":"split","component":"Row","children":["pane","other"]},""" +
            """{"id":"pane","component":"Column"$weight,"children":["head"]},""" +
            header +
            """,""" +
            """{"id":"other","component":"Column","weight":1,"children":["o"]},""" +
            """{"id":"o","component":"Text","text":"Map"}""",
        ),
        view = bot.charlies.androiddesign.preview.model.Views.Tablet,
      )
      .tree

  @Test
  fun `a spaceBetween header in an unweighted column in a row is noted`() {
    assertEquals(listOf("head"), pane(weight = "").noSpace())
  }

  @Test
  fun `the same header in a weighted column has free space`() {
    assertEquals(emptyList(), pane(weight = ""","weight":1""").noSpace())
  }

  @Test
  fun `a spaceBetween row that fills the screen is not noted`() {
    val tree =
      render(
          singleScreen(
            """{"id":"root","component":"Scaffold","content":"col"},""" +
              """{"id":"col","component":"Column","children":["head"]},""" +
              header,
          ),
        )
        .tree
    assertEquals(emptyList(), tree.noSpace())
  }

  @Test
  fun `a fill-width row whose children exactly fill it is not noted`() {
    val tree =
      render(
          singleScreen(
            """{"id":"root","component":"Scaffold","content":"col"},""" +
              """{"id":"col","component":"Column","children":["head"]},""" +
              """{"id":"head","component":"Row","arrangement":"spaceBetween","children":["l","r"]},""" +
              """{"id":"l","component":"Text","text":"Route","weight":1},""" +
              """{"id":"r","component":"Text","text":"Up to 5,364 m","weight":1}""",
          ),
        )
        .tree
    assertEquals(emptyList(), tree.noSpace())
  }

  /** [pane]'s trek shape with the header's children given [childProps]. */
  private fun paddedHeader(left: String, right: String): LayoutTree =
    render(
        singleScreen(
          """{"id":"root","component":"Scaffold","content":"split"},""" +
            """{"id":"split","component":"Row","children":["pane","other"]},""" +
            """{"id":"pane","component":"Column","children":["head"]},""" +
            """{"id":"head","component":"Row","arrangement":"spaceBetween","children":["l","r"]},""" +
            left +
            "," +
            right +
            """,""" +
            """{"id":"other","component":"Column","weight":1,"children":["o"]},""" +
            """{"id":"o","component":"Text","text":"Map"}""",
        ),
        view = bot.charlies.androiddesign.preview.model.Views.Tablet,
      )
      .tree

  @Test
  fun `children with padding count with it, so a padded header is still noted with no space free`() {
    val tree =
      paddedHeader(
        """{"id":"l","component":"Text","text":"Route","padding":"md"}""",
        """{"id":"r","component":"Text","text":"Up to 5,364 m","padding":"md"}""",
      )
    assertEquals(listOf("head"), tree.noSpace())
    val free = checkNotNull(tree.node("head").arrangement).free
    assertEquals(0f, free, 0.5f)
  }

  @Test
  fun `a child with a styled container and padding counts with them`() {
    val theme =
      """{"material":{"styles":{"pill":{"container":"tertiaryContainer","shape":"full","padding":"xs"}}}}"""
    val tree =
      render(
          proposalOf(
            "home" to
              screenJson(
                """{"id":"root","component":"Scaffold","content":"split"},""" +
                  """{"id":"split","component":"Row","children":["pane","other"]},""" +
                  """{"id":"pane","component":"Column","children":["head"]},""" +
                  """{"id":"head","component":"Row","arrangement":"spaceBetween","children":["l","r"]},""" +
                  """{"id":"l","component":"Column","style":"pill","children":["lt"]},""" +
                  """{"id":"lt","component":"Text","text":"Route"},""" +
                  """{"id":"r","component":"Text","text":"Up to 5,364 m"},""" +
                  """{"id":"other","component":"Column","weight":1,"children":["o"]},""" +
                  """{"id":"o","component":"Text","text":"Map"}""",
              ),
            theme = theme,
          ),
          view = bot.charlies.androiddesign.preview.model.Views.Tablet,
        )
        .tree
    assertEquals(listOf("head"), tree.noSpace())
    assertEquals(0f, checkNotNull(tree.node("head").arrangement).free, 0.5f)
  }

  @Test
  fun `the note on the trek header states the cause and the generic way out`() {
    val detail =
      pane(weight = "").findings.single { it.rule == FindingRule.ArrangementNoSpace }.detail
    assertEquals(
      "spaceBetween has no effect: the row is only as wide as its children, so there is no " +
        "space to spread. Give the row room on this axis, usually by weighting the pane or " +
        "column that wraps it in its parent row.",
      detail,
    )
  }

  @Test
  fun `weighting the pane, as the note says, clears it and adds no error`() {
    val before = pane(weight = "")
    val after = pane(weight = ""","weight":1""")
    assertEquals(listOf("head"), before.noSpace())
    assertEquals(emptyList(), after.noSpace())
    assertEquals(emptyList(), after.findings.filter { it.severity == Severity.Error })
  }

  private val spread =
    """{"id":"head","component":"Column","arrangement":"spaceBetween","children":["l","r"]},""" +
      """{"id":"l","component":"Text","text":"Route"},""" +
      """{"id":"r","component":"Text","text":"Up to 5,364 m"}"""

  /** A spaceBetween Column in a Column that fills the screen, with [headProps] on the Column. */
  private fun columnInColumn(headProps: String): LayoutTree =
    render(
        singleScreen(
          """{"id":"root","component":"Scaffold","content":"outer"},""" +
            """{"id":"outer","component":"Column","children":["head"]},""" +
            spread.replace("\"id\":\"head\",", "\"id\":\"head\"$headProps,"),
        ),
      )
      .tree

  /** The same Column held by a Row in a Column, with [rowProps] on the Row. */
  private fun columnInRow(rowProps: String): LayoutTree =
    render(
        singleScreen(
          """{"id":"root","component":"Scaffold","content":"outer"},""" +
            """{"id":"outer","component":"Column","children":["orow"]},""" +
            """{"id":"orow","component":"Row"$rowProps,"children":["head"]},""" +
            spread,
        ),
      )
      .tree

  @Test
  fun `a spaceBetween column in a column is noted, and weighting it clears the note`() {
    assertEquals(listOf("head"), columnInColumn("").noSpace())
    val fixed = columnInColumn(""","weight":1""")
    assertEquals(emptyList(), fixed.noSpace())
    assertEquals(emptyList(), fixed.findings.filter { it.severity == Severity.Error })
  }

  @Test
  fun `a spaceBetween column in a row is cleared by a row that has a height and stretches`() {
    assertEquals(listOf("head"), columnInRow("").noSpace())
    // A weight alone makes the row tall but not its children, so the note stays.
    assertEquals(listOf("head"), columnInRow(""","weight":1""").noSpace())
    val fixed = columnInRow(""","weight":1,"verticalAlignment":"stretch"""")
    assertEquals(emptyList(), fixed.noSpace())
    assertEquals(emptyList(), fixed.findings.filter { it.severity == Severity.Error })
  }
}
