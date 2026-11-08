package bot.charlies.androiddesign.preview.domain.findings

import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.Severity
import bot.charlies.androiddesign.preview.model.Views
import kotlin.test.Test
import kotlin.test.assertEquals

/** Material: "keep text between 40–60 characters per line" at every breakpoint. */
class LineLengthTest {
  // 27 characters drawn 184 dp wide: about 6.8 dp a character, Material's body text.
  private fun subtitle(id: String, x: Float, parent: String) =
    node(
      id,
      "Text",
      x = x,
      y = 120f,
      w = 184f,
      h = 20f,
      parent = parent,
      text = text("Over Wi-Fi, after each edit", ink = Bounds(x, 120f, 184f, 20f)),
    )

  private fun lineLength(vararg nodes: bot.charlies.androiddesign.preview.model.LayoutNode) =
    evaluate(tree(Views.Foldable, *nodes)).filter { it.rule == FindingRule.LineLength }

  @Test
  fun `a card wide enough for lines past 60 characters fails`() {
    val findings =
      lineLength(node("card", "Card", x = 24f, w = 656f, h = 120f), subtitle("sub", 48f, "card"))
    assertEquals(listOf("card"), findings.map { it.id })
    assertEquals(Severity.Error, findings.single().severity)
  }

  @Test
  fun `the same card at phone width passes`() {
    assertEquals(
      emptyList(),
      lineLength(node("card", "Card", x = 16f, w = 379f, h = 120f), subtitle("sub", 32f, "card")),
    )
  }

  @Test
  fun `a wide card without text passes`() {
    assertEquals(
      emptyList(),
      lineLength(
        node("card", "Card", x = 24f, w = 656f, h = 300f),
        node("art", "Image", x = 24f, w = 656f, h = 300f, parent = "card"),
      ),
    )
  }

  @Test
  fun `a wide paragraph fails on its own`() {
    val paragraph =
      node(
        "body",
        "Text",
        x = 24f,
        w = 900f,
        h = 60f,
        text =
          text(
            "Oat milk, lemons, the good sourdough. Ask about the honey stand that was gone.",
            lines = 2,
            ink = Bounds(24f, 0f, 540f, 60f),
          ),
      )
    assertEquals(listOf("body"), lineLength(paragraph).map { it.id })
  }

  @Test
  fun `a wide list item takes its character width from the view's other text`() {
    val findings =
      lineLength(
        node("row", "ListItem", x = 24f, y = 300f, w = 656f, h = 72f, text = text("Dynamic color")),
        node("card", "Card", x = 16f, w = 300f, h = 120f),
        subtitle("sub", 32f, "card"),
      )
    assertEquals(listOf("row"), findings.map { it.id })
  }

  @Test
  fun `only the outermost wide text container is reported`() {
    val findings =
      lineLength(
        node("card", "Card", x = 24f, w = 656f, h = 200f),
        node("row", "ListItem", x = 24f, w = 656f, h = 72f, parent = "card", text = text("Sync")),
        subtitle("sub", 48f, "card"),
      )
    assertEquals(listOf("card"), findings.map { it.id })
  }

  @Test
  fun `a screen of list items with no measured text still fails when stretched`() {
    val findings =
      evaluate(
          tree(
            Views.Desktop,
            node("list", "Column", x = 96f, w = 1800f, h = 400f),
            node(
              "a",
              "ListItem",
              x = 96f,
              w = 1800f,
              h = 72f,
              parent = "list",
              text = text("Sync"),
            ),
            node(
              "b",
              "ListItem",
              x = 96f,
              y = 72f,
              w = 1800f,
              h = 72f,
              parent = "list",
              text = text("Theme"),
            ),
          ),
        )
        .filter { it.rule == FindingRule.LineLength }
    assertEquals(listOf("a", "b"), findings.map { it.id })
  }

  @Test
  fun `a wrapped paragraph with a short last line does not inflate the character width`() {
    // 106 characters wrapped to two lines; the widest line spans 702.6 dp.
    val value =
      "Your notes back up over Wi-Fi after each edit, and the last sync finished two minutes " +
        "ago without a problem."
    val findings =
      lineLength(
        node("card", "Card", x = 24f, w = 704f, h = 120f),
        node(
          "body",
          "Text",
          x = 24f,
          w = 704f,
          h = 48f,
          parent = "card",
          text = text(value, lines = 2, ink = Bounds(24f, 0f, 702.6f, 48f)),
        ),
      )
    assertEquals(listOf("card"), findings.map { it.id })
  }

  @Test
  fun `a truncated line is not measured, so an ellipsized phone card passes`() {
    // 137 characters cut to one 398 dp line: the ink shows only part of the text.
    val preview =
      "Hey, are we still on for Saturday? I was thinking we could grab coffee first, then walk " +
        "to the market before the honey stand sells out."
    val findings =
      evaluate(
          tree(
            Views.Light,
            node("card", "Card", x = 0f, w = 411f, h = 96f),
            node(
              "preview",
              "Text",
              x = 6f,
              w = 398f,
              h = 20f,
              parent = "card",
              text =
                text(preview, overflow = true, truncated = true, ink = Bounds(6f, 0f, 398f, 20f)),
            ),
          ),
        )
        .filter { it.rule == FindingRule.LineLength }
    assertEquals(emptyList(), findings.map { it.id })
  }
}
