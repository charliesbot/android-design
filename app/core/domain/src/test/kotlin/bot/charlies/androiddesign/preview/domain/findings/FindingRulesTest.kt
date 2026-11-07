package bot.charlies.androiddesign.preview.domain.findings

import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.RingInset
import bot.charlies.androiddesign.preview.model.Severity
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import bot.charlies.androiddesign.preview.model.WearCatalog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FindingRulesTest {
  // region touch-target

  @Test
  fun `touch-target passes for a 48 dp touch area around a 40 dp button`() {
    assertEquals(emptyList(), evaluate(tree(nodes = arrayOf(button("cook")))))
  }

  @Test
  fun `touch-target fails for an interactive node under 48 dp`() {
    val small = node("close", "IconButton", w = 32f, h = 32f)
    val findings = evaluate(tree(nodes = arrayOf(small))).of(FindingRule.TouchTarget)
    assertEquals(1, findings.size)
    assertEquals("close", findings.single().id)
    assertEquals(Severity.Error, findings.single().severity)
    assertEquals("32 x 32 dp, needs 48 x 48", findings.single().detail)
  }

  @Test
  fun `touch-target fails when only one side is short`() {
    val wide = node("chip", "AssistChip", w = 120f, h = 32f, touch = Bounds(0f, 0f, 120f, 40f))
    assertEquals(1, evaluate(tree(nodes = arrayOf(wide))).of(FindingRule.TouchTarget).size)
  }

  @Test
  fun `touch-target uses the bounds when no touch area is given`() {
    val field = node("amount", "TextField", w = 280f, h = 56f)
    assertEquals(emptyList(), evaluate(tree(nodes = arrayOf(field))))
  }

  @Test
  fun `touch-target ignores non-interactive components`() {
    val icon = node("star", "Icon", w = 24f, h = 24f)
    val label = node("title", "Text", w = 60f, h = 20f, text = text())
    assertEquals(emptyList(), evaluate(tree(nodes = arrayOf(icon, label))))
  }

  @Test
  fun `touch-target tolerates a rounding sliver under 48 dp`() {
    val almost = node("go", "Button", w = 100f, h = 47.96f)
    assertEquals(emptyList(), evaluate(tree(nodes = arrayOf(almost))))
  }

  @Test
  fun `touch-target skips nodes that are not laid out`() {
    val hidden = node("ghost", "IconButton", w = 0f, h = 0f)
    assertEquals(emptyList(), evaluate(tree(nodes = arrayOf(hidden))))
  }

  // endregion

  // region font

  private fun fontFindings(font: String?, role: String?) =
    evaluate(
        tree(
          nodes =
            arrayOf(
              node("t", "Text", h = 24f, text = text(font = font), typeRole = role),
            ),
        ),
      )
      .of(FindingRule.Font)

  @Test
  fun `font passes for Google Sans Flex in every group`() {
    listOf("displayLarge", "headlineSmall", "titleMedium", "bodyLarge", "labelSmall").forEach {
      assertEquals(emptyList(), fontFindings(FLEX, it), it)
    }
  }

  @Test
  fun `font fails for a family that is not bundled`() {
    val finding = fontFindings("Georgia", "bodyLarge").single()
    assertEquals("t", finding.id)
    assertEquals(Severity.Error, finding.severity)
    assertEquals("Georgia is not a bundled font", finding.detail)
  }

  @Test
  fun `font allows brand faces on display and headline roles`() {
    listOf("Fraunces", "Bricolage Grotesque", "Big Shoulders Display").forEach { family ->
      listOf("displayLarge", "displaySmallEmphasized", "headlineMedium").forEach { role ->
        assertEquals(emptyList(), fontFindings(family, role), "$family $role")
      }
    }
  }

  @Test
  fun `font rejects brand faces on title body and label roles`() {
    listOf("titleLarge", "bodyMedium", "labelLarge").forEach { role ->
      val finding = fontFindings("Fraunces", role).single()
      assertEquals("Fraunces is for display and headline text, not $role", finding.detail)
    }
  }

  @Test
  fun `font allows Doto on display roles only`() {
    assertEquals(emptyList(), fontFindings("Doto", "displayMedium"))
    assertEquals(
      "Doto is for display text, not headlineLarge",
      fontFindings("Doto", "headlineLarge").single().detail,
    )
  }

  @Test
  fun `font allows Google Sans Code at any size`() {
    assertEquals(emptyList(), fontFindings("Google Sans Code", "labelSmall"))
    assertEquals(emptyList(), fontFindings("Google Sans Code", "displayLarge"))
  }

  @Test
  fun `font reads quoted and listed family names`() {
    assertEquals(emptyList(), fontFindings("\"Google Sans Flex\", sans-serif", "bodyLarge"))
    assertEquals(emptyList(), fontFindings("google sans flex", "bodyLarge"))
  }

  @Test
  fun `font treats an unknown type role as plain text`() {
    assertEquals(1, fontFindings("Fraunces", null).size)
    assertEquals(emptyList(), fontFindings(FLEX, null))
  }

  @Test
  fun `font skips text nodes that report no family`() {
    assertEquals(emptyList(), fontFindings(null, "bodyLarge"))
  }

  // endregion

  // region text-clipped and text-truncated

  private fun textNodeFindings(info: bot.charlies.androiddesign.preview.model.TextInfo) =
    evaluate(tree(nodes = arrayOf(node("desc", "Text", h = 60f, text = info))))

  @Test
  fun `text-clipped fails for overflow without an ellipsis`() {
    val finding =
      textNodeFindings(text(lines = 1, overflow = true)).of(FindingRule.TextClipped).single()
    assertEquals("desc", finding.id)
    assertEquals(Severity.Error, finding.severity)
    assertEquals("text is cut off with no ellipsis", finding.detail)
  }

  @Test
  fun `text-clipped passes when the text fits`() {
    assertEquals(emptyList(), textNodeFindings(text(lines = 2)))
  }

  @Test
  fun `an ellipsis excuses the overflow it causes`() {
    val findings = textNodeFindings(text(lines = 3, overflow = true, truncated = true))
    assertEquals(emptyList(), findings.of(FindingRule.TextClipped))
  }

  @Test
  fun `text-truncated is informational and does not fail the view`() {
    val truncatedTree =
      tree(nodes = arrayOf(node("desc", "Text", text = text(lines = 3, truncated = true))))
    val findings = evaluate(truncatedTree)
    val finding = findings.of(FindingRule.TextTruncated).single()
    assertEquals("desc", finding.id)
    assertEquals(Severity.Info, finding.severity)
    assertEquals("3 lines, ellipsized", finding.detail)
    assertTrue(truncatedTree.copy(findings = findings).pass)
  }

  @Test
  fun `text-truncated passes for text that is not truncated`() {
    assertEquals(emptyList(), textNodeFindings(text(lines = 3)).of(FindingRule.TextTruncated))
  }

  // endregion

  // region label-truncated

  private fun labelFindings(type: String, info: bot.charlies.androiddesign.preview.model.TextInfo) =
    evaluate(
      tree(
        nodes =
          arrayOf(
            node("c", type, w = 160f, h = 48f, text = info, touch = Bounds(0f, 0f, 160f, 48f)),
          ),
      ),
    )

  @Test
  fun `label-truncated fails for an ellipsized button label`() {
    val findings = labelFindings("Button", text(truncated = true, overflow = true))
    val finding = findings.of(FindingRule.LabelTruncated).single()
    assertEquals(Severity.Error, finding.severity)
    assertEquals("label is cut off with an ellipsis", finding.detail)
    assertEquals(emptyList(), findings.of(FindingRule.TextTruncated))
  }

  @Test
  fun `label-truncated fails for a wrapped label`() {
    val finding =
      labelFindings("FilterChip", text(lines = 2)).of(FindingRule.LabelTruncated).single()
    assertEquals("label wraps to 2 lines", finding.detail)
  }

  @Test
  fun `label-truncated covers chips navigation items fab and menu items`() {
    listOf(
        "AssistChip",
        "NavigationBarItem",
        "NavigationRailItem",
        "FloatingActionButton",
        "MenuItem",
        "FabMenuItem",
        "ToggleButton",
        "SplitButton",
      )
      .forEach { type ->
        assertEquals(
          1,
          labelFindings(type, text(truncated = true)).of(FindingRule.LabelTruncated).size,
          type,
        )
      }
  }

  @Test
  fun `label-truncated passes for a single line label`() {
    assertEquals(emptyList(), labelFindings("Button", text(lines = 1)))
  }

  @Test
  fun `label-truncated leaves body text and text fields alone`() {
    assertEquals(
      emptyList(),
      labelFindings("Text", text(lines = 2)).of(FindingRule.LabelTruncated),
    )
    assertEquals(
      emptyList(),
      labelFindings("TextField", text(lines = 2)).of(FindingRule.LabelTruncated),
    )
  }

  @Test
  fun `a clipped button label is a text-clipped failure`() {
    val findings = labelFindings("Button", text(overflow = true))
    assertEquals(1, findings.of(FindingRule.TextClipped).size)
    assertEquals(emptyList(), findings.of(FindingRule.LabelTruncated))
  }

  // endregion

  // region contrast

  private fun contrastFindings(
    content: String?,
    container: String?,
    role: String = "bodyLarge",
  ) =
    evaluate(
        tree(
          nodes =
            arrayOf(
              node(
                "body",
                "Text",
                text = text(),
                typeRole = role,
                content = content,
                container = container,
              ),
            ),
        ),
      )
      .of(FindingRule.Contrast)

  @Test
  fun `contrast passes for black on white`() {
    assertEquals(emptyList(), contrastFindings("#000000", "#FFFFFF"))
  }

  @Test
  fun `contrast fails below 4_5 to 1 for body text`() {
    val finding = contrastFindings("#777777", "#FFFFFF").single()
    assertEquals("body", finding.id)
    assertEquals(Severity.Error, finding.severity)
    assertEquals("4.48:1, needs 4.5:1", finding.detail)
  }

  @Test
  fun `contrast passes just above 4_5 to 1`() {
    assertEquals(emptyList(), contrastFindings("#767676", "#FFFFFF"))
  }

  @Test
  fun `contrast needs only 3 to 1 for display and headline text`() {
    assertEquals(emptyList(), contrastFindings("#949494", "#FFFFFF", "displayLarge"))
    assertEquals(emptyList(), contrastFindings("#949494", "#FFFFFF", "headlineSmall"))
    assertEquals(1, contrastFindings("#949494", "#FFFFFF", "bodyLarge").size)
  }

  @Test
  fun `contrast still fails large text under 3 to 1`() {
    assertEquals(
      "1.92:1, needs 3:1",
      contrastFindings("#BBBBBB", "#FFFFFF", "displayMedium").single().detail,
    )
  }

  @Test
  fun `contrast finds the backdrop on an ancestor`() {
    val card = node("card", "Card", container = "#FFFFFF", w = 300f, h = 100f)
    val body =
      node(
        "body",
        "Text",
        parent = "card",
        text = text(),
        typeRole = "bodyLarge",
        content = "#CCCCCC",
      )
    val findings = evaluate(tree(nodes = arrayOf(card, body))).of(FindingRule.Contrast)
    assertEquals("body", findings.single().id)
  }

  @Test
  fun `contrast is not measured without a backdrop or a content color`() {
    assertEquals(emptyList(), contrastFindings("#CCCCCC", null))
    assertEquals(emptyList(), contrastFindings(null, "#FFFFFF"))
  }

  @Test
  fun `contrast ignores malformed colors`() {
    assertEquals(emptyList(), contrastFindings("red", "#FFFFFF"))
  }

  @Test
  fun `contrast ignores nodes without text`() {
    val icon = node("star", "Icon", w = 24f, h = 24f, content = "#EEEEEE", container = "#FFFFFF")
    assertEquals(emptyList(), evaluate(tree(nodes = arrayOf(icon))))
  }

  // endregion

  // region primary-largest

  @Test
  fun `primary-largest passes when the primary is the tallest action`() {
    val primary = button("cook", primary = true, h = 56f)
    val other = button("skip", y = 200f, h = 40f)
    assertEquals(emptyList(), evaluate(tree(nodes = arrayOf(primary, other))))
  }

  @Test
  fun `primary-largest fails when another action is taller`() {
    val primary = button("cook", primary = true, h = 40f)
    val other = button("archive", y = 200f, h = 96f)
    val finding =
      evaluate(tree(nodes = arrayOf(primary, other))).of(FindingRule.PrimaryLargest).single()
    assertEquals("cook", finding.id)
    assertEquals(Severity.Error, finding.severity)
    assertEquals("40 dp tall, shorter than archive (96 dp)", finding.detail)
  }

  @Test
  fun `primary-largest tolerates 1 dp`() {
    val primary = button("cook", primary = true, h = 40f)
    val other = button("skip", y = 200f, h = 41f)
    assertEquals(emptyList(), evaluate(tree(nodes = arrayOf(primary, other))))
  }

  @Test
  fun `primary-largest passes when there is no primary`() {
    val a = button("a", h = 40f)
    val b = button("b", y = 200f, h = 96f)
    assertEquals(emptyList(), evaluate(tree(nodes = arrayOf(a, b))))
  }

  @Test
  fun `primary-largest ignores navigation chrome sliders and text fields`() {
    val primary = button("cook", primary = true, h = 40f)
    val bar = node("bar", "NavigationBar", y = 800f, w = 411f, h = 80f)
    val items = destinations("bar", 3)
    val slider = node("volume", "Slider", y = 300f, w = 280f, h = 56f)
    val field = node("amount", "TextField", y = 400f, w = 280f, h = 56f)
    assertEquals(
      emptyList(),
      evaluate(tree(nodes = arrayOf(primary, bar, *items, slider, field))),
    )
  }

  @Test
  fun `primary-largest ignores buttons inside a docked toolbar`() {
    val primary = button("cook", primary = true, h = 40f)
    val toolbar = node("toolbar", "DockedToolbar", y = 800f, w = 411f, h = 80f)
    val inside = button("more", parent = "toolbar", y = 806f, h = 96f)
    assertEquals(emptyList(), evaluate(tree(nodes = arrayOf(primary, toolbar, inside))))
  }

  @Test
  fun `primary-largest ignores toggle buttons inside a floating toolbar`() {
    val primary = button("cook", primary = true, h = 40f)
    val toolbar = node("toolbar", "FloatingToolbar", y = 800f, w = 280f, h = 64f)
    val toggle =
      node(
        "saved",
        "ToggleButton",
        parent = "toolbar",
        y = 806f,
        w = 80f,
        h = 52f,
        touch = Bounds(0f, 800f, 80f, 64f),
      )
    assertEquals(emptyList(), evaluate(tree(nodes = arrayOf(primary, toolbar, toggle))))
  }

  @Test
  fun `primary-largest ignores tappable rows and cards`() {
    val primary = button("cook", primary = true, h = 40f)
    val row = node("row", "ListItem", y = 200f, w = 411f, h = 72f)
    val card = node("card", "Card", y = 300f, w = 380f, h = 160f)
    assertEquals(emptyList(), evaluate(tree(nodes = arrayOf(primary, row, card))))
  }

  @Test
  fun `primary-largest compares a standard icon button by its visual size`() {
    val primary = button("cook", primary = true, h = 40f)
    val icon =
      node("more", "IconButton", y = 200f, w = 40f, h = 40f, touch = Bounds(0f, 196f, 48f, 48f))
    assertEquals(emptyList(), evaluate(tree(nodes = arrayOf(primary, icon))))
  }

  @Test
  fun `primary-largest counts a fab as an action`() {
    val primary = button("cook", primary = true, h = 40f)
    val fab =
      node(
        "add",
        "FloatingActionButton",
        y = 700f,
        w = 80f,
        h = 80f,
        touch = Bounds(0f, 700f, 80f, 80f),
      )
    assertEquals(
      1,
      evaluate(tree(nodes = arrayOf(primary, fab))).of(FindingRule.PrimaryLargest).size,
    )
  }

  @Test
  fun `primary-largest checks every marked primary`() {
    val a = button("a", primary = true, h = 40f)
    val b = button("b", y = 200f, primary = true, h = 56f)
    val findings = evaluate(tree(nodes = arrayOf(a, b))).of(FindingRule.PrimaryLargest)
    assertEquals(listOf("a"), findings.map { it.id })
  }

  // endregion

  // region carousel-items

  private fun carousel(vararg itemWidths: Pair<Float, Float>, withText: Boolean = true) =
    arrayOf(node("gallery", "Carousel", y = 100f, w = 411f, h = 200f)) +
      itemWidths.flatMapIndexed { i, (x, w) ->
        val item = node("item$i", "Card", parent = "gallery", x = x, y = 100f, w = w, h = 200f)
        if (withText) {
          listOf(
            item,
            node("label$i", "Text", parent = "item$i", x = x, y = 250f, text = text()),
          )
        } else {
          listOf(item)
        }
      }

  private fun carouselFindings(view: ViewSpec, vararg nodes: LayoutNode) =
    evaluate(tree(view, *nodes)).of(FindingRule.CarouselItems)

  @Test
  fun `carousel-items passes for the items of a multi-browse carousel`() {
    // Two large items and a small one; the items past it are laid out inside it.
    val nodes =
      carousel(
        13.33f to 177.5f,
        193.5f to 177.5f,
        304.92f to 106.08f,
        332.58f to 78.42f,
        352.46f to 58.54f,
      )
    assertEquals(emptyList(), carouselFindings(Views.Light, *nodes))
  }

  @Test
  fun `carousel-items flags four items with text at a compact view`() {
    val nodes = carousel(16f to 80f, 104f to 80f, 192f to 80f, 280f to 131f, 330f to 81f)
    val finding = carouselFindings(Views.Light, *nodes).single()
    assertEquals("gallery", finding.id)
    assertEquals(Severity.Info, finding.severity)
    assertEquals(
      "4 items with text show at 411 dp; Material shows at most 3 at compact widths unless " +
        "they are easy to recognize",
      finding.detail,
    )
  }

  @Test
  fun `carousel-items does not count items without text`() {
    val nodes = carousel(16f to 80f, 104f to 80f, 192f to 80f, 280f to 131f, withText = false)
    assertEquals(emptyList(), carouselFindings(Views.Light, *nodes))
  }

  @Test
  fun `carousel-items does not count items outside the view`() {
    val nodes = carousel(16f to 140f, 164f to 140f, 312f to 99f, 420f to 140f, 568f to 140f)
    assertEquals(emptyList(), carouselFindings(Views.Light, *nodes))
  }

  @Test
  fun `carousel-items leaves wider views alone`() {
    val nodes = carousel(16f to 80f, 104f to 80f, 192f to 80f, 280f to 131f)
    assertEquals(emptyList(), carouselFindings(Views.Tablet, *nodes))
  }

  // endregion

  // region nav-destinations

  private fun destinations(bar: String, count: Int) =
    Array(count) { i ->
      node(
        "$bar-item$i",
        "NavigationBarItem",
        parent = bar,
        x = i * 100f,
        y = 800f,
        w = 80f,
        h = 80f,
        touch = Bounds(i * 100f, 800f, 80f, 80f),
      )
    }

  @Test
  fun `nav-destinations passes for three destinations`() {
    val bar = node("bar", "NavigationBar", y = 800f, w = 411f, h = 80f)
    val findings = evaluate(tree(nodes = arrayOf(bar, *destinations("bar", 3))))
    assertEquals(emptyList(), findings.of(FindingRule.NavDestinations))
  }

  @Test
  fun `nav-destinations fails for fewer than three and points at tabs`() {
    val bar = node("bar", "NavigationBar", y = 800f, w = 411f, h = 80f)
    val finding =
      evaluate(tree(nodes = arrayOf(bar, *destinations("bar", 2))))
        .of(FindingRule.NavDestinations)
        .single()
    assertEquals("bar", finding.id)
    assertEquals(Severity.Error, finding.severity)
    assertEquals(
      "2 destinations; a navigation bar needs at least 3, use tabs for fewer",
      finding.detail,
    )
  }

  @Test
  fun `nav-destinations ignores a bar that is not drawn`() {
    val bar = node("bar", "NavigationBar", w = 0f, h = 0f)
    assertEquals(emptyList(), evaluate(tree(nodes = arrayOf(bar))).of(FindingRule.NavDestinations))
  }

  // endregion

  // region docked-toolbar-nav-bar

  @Test
  fun `docked-toolbar-nav-bar fails when a navigation bar is drawn beside a docked toolbar`() {
    val toolbar = node("toolbar", "DockedToolbar", y = 800f, w = 411f, h = 80f)
    val bar = node("bar", "NavigationBar", y = 700f, w = 411f, h = 80f)
    val finding =
      evaluate(tree(nodes = arrayOf(toolbar, bar))).of(FindingRule.DockedToolbarNavBar).single()
    assertEquals("toolbar", finding.id)
    assertEquals(Severity.Error, finding.severity)
  }

  @Test
  fun `docked-toolbar-nav-bar passes for either alone and for a bar that is not drawn`() {
    val toolbar = node("toolbar", "DockedToolbar", y = 800f, w = 411f, h = 80f)
    val bar = node("bar", "NavigationBar", y = 800f, w = 411f, h = 80f)
    val hidden = node("hidden", "NavigationBar", w = 0f, h = 0f)
    assertEquals(
      emptyList(),
      evaluate(tree(nodes = arrayOf(toolbar))).of(FindingRule.DockedToolbarNavBar),
    )
    assertEquals(
      emptyList(),
      evaluate(tree(nodes = arrayOf(bar, *destinations("bar", 3))))
        .of(FindingRule.DockedToolbarNavBar),
    )
    assertEquals(
      emptyList(),
      evaluate(tree(nodes = arrayOf(toolbar, hidden))).of(FindingRule.DockedToolbarNavBar),
    )
  }

  // endregion

  // region one-fab

  private fun fab(id: String, type: String = "FloatingActionButton") =
    node(id, type, y = 800f, w = 80f, h = 80f, touch = Bounds(0f, 800f, 80f, 80f))

  @Test
  fun `one-fab passes for a single fab`() {
    assertEquals(emptyList(), evaluate(tree(nodes = arrayOf(fab("add")))))
  }

  @Test
  fun `one-fab fails for each fab after the first`() {
    val findings =
      evaluate(tree(nodes = arrayOf(fab("add"), fab("edit"), fab("share")))).of(FindingRule.OneFab)
    assertEquals(listOf("edit", "share"), findings.map { it.id })
    assertEquals(Severity.Error, findings.first().severity)
    assertEquals("3 floating action buttons (add, edit, share), keep one", findings.first().detail)
  }

  @Test
  fun `one-fab counts a fab menu but not its items`() {
    val menu = fab("actions", "FabMenu")
    val item = fab("compose", "FabMenuItem")
    assertEquals(emptyList(), evaluate(tree(nodes = arrayOf(menu, item))).of(FindingRule.OneFab))
    assertEquals(
      1,
      evaluate(tree(nodes = arrayOf(menu, fab("add")))).of(FindingRule.OneFab).size,
    )
  }

  // endregion

  // region offscreen

  private fun offscreen(
    view: bot.charlies.androiddesign.preview.model.ViewSpec,
    vararg nodes: bot.charlies.androiddesign.preview.model.LayoutNode,
  ) = evaluate(tree(view, *nodes)).of(FindingRule.Offscreen)

  @Test
  fun `offscreen passes for content inside the view`() {
    assertEquals(
      emptyList(),
      offscreen(Views.Light, node("box", "Box", x = 0f, w = 411f, h = 100f)),
    )
  }

  @Test
  fun `offscreen fails for content wider than the view`() {
    val finding = offscreen(Views.Light, node("hero", "Box", x = 0f, w = 600f, h = 100f)).single()
    assertEquals("hero", finding.id)
    assertEquals(Severity.Error, finding.severity)
    assertEquals("ends at 600 dp, the view is 411 dp wide", finding.detail)
  }

  @Test
  fun `offscreen fails for content crossing the left edge`() {
    val finding = offscreen(Views.Light, node("pin", "Box", x = -40f, w = 160f, h = 40f)).single()
    assertEquals("starts at -40 dp, before the view", finding.detail)
  }

  @Test
  fun `offscreen tolerates 1 dp`() {
    assertEquals(
      emptyList(),
      offscreen(Views.Light, node("edge", "Box", x = -1f, w = 412f, h = 40f)),
    )
  }

  @Test
  fun `offscreen allows content fully off the left edge`() {
    assertEquals(
      emptyList(),
      offscreen(Views.Light, node("drawer", "Box", x = -9999f, w = 280f, h = 900f)),
    )
  }

  @Test
  fun `offscreen names the widest parent before its children`() {
    val parent = node("row", "Row", w = 600f, h = 48f)
    val child = node("last", "Button", parent = "row", x = 500f, w = 100f, h = 40f)
    assertEquals(listOf("row"), offscreen(Views.Light, parent, child).map { it.id })
  }

  @Test
  fun `offscreen reports at most five offenders`() {
    val boxes = (1..8).map { node("b$it", "Box", w = 500f + it, h = 10f) }
    assertEquals(5, offscreen(Views.Light, *boxes.toTypedArray()).size)
  }

  @Test
  fun `offscreen follows the width of the view`() {
    val wide = node("hero", "Box", w = 1500f, h = 100f)
    assertEquals(1, offscreen(Views.Tablet, wide).size)
    assertEquals(emptyList(), offscreen(Views.Desktop, wide))
  }

  // endregion

  // region overflow

  private fun overflow(vararg nodes: bot.charlies.androiddesign.preview.model.LayoutNode) =
    evaluate(tree(Views.Light, *nodes)).of(FindingRule.Overflow)

  @Test
  fun `overflow names a node whose layout box runs past the view though its bounds fit`() {
    val grid = node("grid", "Grid", x = 32f, w = 379f, h = 100f)
    val row =
      node(
        "row",
        "Row",
        x = 32f,
        w = 379f,
        h = 48f,
        parent = "grid",
        layout = Bounds(32f, 0f, 395f, 48f),
      )
    val finding = overflow(grid, row).single()
    assertEquals("row", finding.id)
    assertEquals(Severity.Error, finding.severity)
    assertEquals("ends 16 dp past grid", finding.detail)
  }

  @Test
  fun `overflow names a child wider than its parent inside the view`() {
    val card = node("card", "Card", w = 150f, h = 100f)
    val row = node("row", "Row", w = 200f, h = 48f, parent = "card")
    assertEquals("ends 50 dp past card", overflow(card, row).single().detail)
  }

  @Test
  fun `overflow tolerates half a dp and leaves fitting children alone`() {
    val card = node("card", "Card", w = 150f, h = 100f)
    assertEquals(
      emptyList(),
      overflow(card, node("row", "Row", w = 150.5f, h = 48f, parent = "card")),
    )
  }

  @Test
  fun `overflow leaves what offscreen already reports to it`() {
    assertEquals(emptyList(), overflow(node("hero", "Box", w = 600f, h = 100f)))
  }

  @Test
  fun `overflow exempts the children of a carousel`() {
    val carousel = node("carousel", "Carousel", w = 411f, h = 200f)
    val item = node("item", "Card", x = 300f, w = 300f, h = 200f, parent = "carousel")
    assertEquals(emptyList(), overflow(carousel, item))
  }

  @Test
  fun `overflow measures an anchored menu against the view, not its anchor`() {
    val anchor = node("more", "IconButton", x = 300f, w = 48f, h = 48f)
    val menu = node("menu", "Menu", x = 200f, w = 200f, h = 120f, parent = "more")
    assertEquals(emptyList(), overflow(anchor, menu))
    val past =
      node(
        "menu",
        "Menu",
        x = 200f,
        w = 211f,
        h = 120f,
        parent = "more",
        layout = Bounds(200f, 0f, 300f, 120f),
      )
    assertEquals("ends 89 dp past the view", overflow(anchor, past).single().detail)
  }

  @Test
  fun `overflow names the parent before its children`() {
    val card = node("card", "Card", w = 150f, h = 100f)
    val row = node("row", "Row", w = 200f, h = 48f, parent = "card")
    val last = node("last", "Button", x = 150f, w = 50f, h = 48f, parent = "row")
    assertEquals(listOf("row"), overflow(card, row, last).map { it.id })
  }

  // endregion

  // region panes

  /**
   * A list pane (the requested screen's own ids are plain) beside a detail pane prefixed
   * `plate_detail/`.
   */
  @Test
  fun `primary-largest compares actions within one pane`() {
    val listFab =
      node(
        "new_plate",
        "FloatingActionButton",
        x = 300f,
        y = 800f,
        w = 80f,
        h = 80f,
        primary = true,
        touch = Bounds(300f, 800f, 80f, 80f),
      )
    val detailCook = button("plate_detail/cook", primary = true, h = 40f)
    val found = evaluate(tree(Views.Tablet, listFab, detailCook))
    assertEquals(emptyList(), found.of(FindingRule.PrimaryLargest))
  }

  @Test
  fun `primary-largest still fails inside a pane`() {
    val detailCook = button("plate_detail/cook", primary = true, h = 40f)
    val detailBig = button("plate_detail/archive", y = 200f, h = 96f)
    val found = evaluate(tree(Views.Tablet, detailCook, detailBig)).of(FindingRule.PrimaryLargest)
    assertEquals(listOf("plate_detail/cook"), found.map { it.id })
  }

  @Test
  fun `one-fab counts each pane on its own`() {
    val listFab = fab("new_plate")
    val detailFab = fab("plate_detail/edit")
    assertEquals(
      emptyList(),
      evaluate(tree(Views.Tablet, listFab, detailFab)).of(FindingRule.OneFab),
    )
    val twice =
      evaluate(tree(Views.Tablet, listFab, fab("share"), detailFab)).of(FindingRule.OneFab)
    assertEquals(listOf("share"), twice.map { it.id })
  }

  // endregion

  // region modal overlays

  @Test
  fun `content behind an open sheet is exempt from touch target and contrast`() {
    val sheet = node("sheet", "BottomSheet", y = 500f, w = 411f, h = 400f, container = "#FFFFFF")
    val sheetText =
      node(
        "sheet_title",
        "Text",
        parent = "sheet",
        y = 520f,
        text = text(),
        typeRole = "bodyLarge",
        content = "#000000",
      )
    val behindTiny = node("tiny", "IconButton", w = 32f, h = 32f)
    val behindText =
      node(
        "faint",
        "Text",
        text = text(),
        typeRole = "bodyLarge",
        content = "#EEEEEE",
        container = "#FFFFFF",
      )
    assertEquals(
      emptyList(),
      evaluate(tree(nodes = arrayOf(behindTiny, behindText, sheet, sheetText))),
    )
  }

  @Test
  fun `the open modal and its content are still checked`() {
    val dialog = node("dialog", "Dialog", y = 300f, w = 300f, h = 200f, container = "#FFFFFF")
    val tiny = node("confirm", "Button", parent = "dialog", y = 400f, w = 60f, h = 32f)
    val faint =
      node(
        "body",
        "Text",
        parent = "dialog",
        y = 320f,
        text = text(),
        typeRole = "bodyLarge",
        content = "#DDDDDD",
      )
    val rules = evaluate(tree(nodes = arrayOf(dialog, tiny, faint))).rules()
    assertTrue(FindingRule.TouchTarget in rules)
    assertTrue(FindingRule.Contrast in rules)
  }

  @Test
  fun `the primary action check is skipped while a modal is open`() {
    val primary = button("cook", primary = true, h = 40f)
    val other = button("archive", y = 200f, h = 96f)
    val sheet = node("sheet", "BottomSheet", y = 500f, w = 411f, h = 400f)
    assertEquals(emptyList(), evaluate(tree(nodes = arrayOf(primary, other, sheet))))
  }

  @Test
  fun `a menu does not exempt the content behind it`() {
    val menu = node("menu", "Menu", y = 200f, w = 200f, h = 150f)
    val tiny = node("tiny", "IconButton", w = 32f, h = 32f)
    assertEquals(1, evaluate(tree(nodes = arrayOf(menu, tiny))).of(FindingRule.TouchTarget).size)
  }

  @Test
  fun `a docked date picker does not exempt the content behind it, a modal one does`() {
    val tiny = node("tiny", "IconButton", w = 32f, h = 32f)
    fun picker(variant: String) =
      node("pick", "DatePicker", y = 200f, w = 360f, h = 392f, variant = variant)
    for (variant in listOf("modal", "modal-input", "full-screen")) {
      assertEquals(
        emptyList(),
        evaluate(tree(nodes = arrayOf(tiny, picker(variant)))).of(FindingRule.TouchTarget),
        variant,
      )
    }
    assertEquals(
      1,
      evaluate(tree(nodes = arrayOf(tiny, picker("docked")))).of(FindingRule.TouchTarget).size,
    )
  }

  @Test
  fun `a full-screen date picker is flagged from medium widths up, as a note`() {
    val full = node("pick", "DatePicker", w = 1280f, h = 800f, variant = "full-screen")
    val finding = evaluate(tree(Views.Tablet, full)).of(FindingRule.FullScreenDatePicker).single()
    assertEquals("pick", finding.id)
    assertEquals(Severity.Info, finding.severity)
    assertEquals(
      emptyList(),
      evaluate(tree(Views.Light, full)).of(FindingRule.FullScreenDatePicker),
    )
    val modal = node("pick", "DatePicker", w = 360f, h = 400f, variant = "modal")
    assertEquals(
      emptyList(),
      evaluate(tree(Views.Tablet, modal)).of(FindingRule.FullScreenDatePicker),
    )
    assertTrue(evaluate(tree(Views.Tablet, full)).none { it.severity == Severity.Error })
  }

  // endregion

  // region round-edge-clipped

  private fun iconButton(id: String, x: Float, y: Float = 76f, parent: String? = null) =
    node(id, "IconButton", x, y, 52f, 52f, parent = parent, touch = Bounds(x, y, 52f, 52f))

  @Test
  fun `round-edge-clipped fails for a button row wider than the circle`() {
    val row = tree(Views.Watch, iconButton("minus", 0f), iconButton("plus", 152f))
    val findings = evaluate(row).of(FindingRule.RoundEdgeClipped)
    assertEquals(listOf("minus", "plus"), findings.map { it.id })
    assertEquals(Severity.Error, findings.first().severity)
    assertEquals("reaches 3.3 dp outside the round edge", findings.first().detail)
  }

  @Test
  fun `round-edge-clipped passes for buttons under a scrolling AlertDialog`() {
    val dialog =
      tree(
        Views.Watch,
        node("sure", "AlertDialog", 0f, 0f, 204f, 204f, scrolls = true),
        iconButton("minus", 0f, parent = "sure"),
        iconButton("plus", 152f, parent = "sure"),
      )
    assertEquals(emptyList(), evaluate(dialog).of(FindingRule.RoundEdgeClipped))
  }

  @Test
  fun `round-edge-clipped passes for a scrolling AlertDialog that carries its title`() {
    val dialog =
      tree(
        Views.Watch,
        node("sure", "AlertDialog", 0f, 0f, 204f, 204f, scrolls = true, text = text("End run?")),
      )
    assertEquals(emptyList(), evaluate(dialog).of(FindingRule.RoundEdgeClipped))
  }

  @Test
  fun `round-edge-clipped passes for the same row inside the circle`() {
    val row = tree(Views.Watch, iconButton("minus", 30f), iconButton("plus", 122f))
    assertEquals(emptyList(), evaluate(row).of(FindingRule.RoundEdgeClipped))
  }

  @Test
  fun `round-edge-clipped checks text that is not a control`() {
    val title =
      node("title", "Text", x = 0f, y = 4f, w = 204f, h = 24f, text = text("A long title"))
    assertEquals(
      listOf("title"),
      evaluate(tree(Views.Watch, title)).of(FindingRule.RoundEdgeClipped).map { it.id },
    )
  }

  @Test
  fun `round-edge-clipped judges a text by its drawn lines, not its box`() {
    val short =
      node("title", "Text", 0f, 4f, 204f, 24f, text = text("Hi", ink = Bounds(80f, 4f, 44f, 24f)))
    assertEquals(emptyList(), evaluate(tree(Views.Watch, short)).of(FindingRule.RoundEdgeClipped))
    val long =
      node("long", "Text", 0f, 4f, 204f, 24f, text = text("Wide", ink = Bounds(0f, 4f, 204f, 24f)))
    assertEquals(
      listOf("long"),
      evaluate(tree(Views.Watch, long)).of(FindingRule.RoundEdgeClipped).map { it.id },
    )
  }

  @Test
  fun `round-edge-clipped skips an item in a scrolling list`() {
    val list = node("list", "Screen", w = 204f, h = 204f, scrolls = true)
    val item = node("item", "Button", 0f, 0f, 204f, 52f, parent = "list", text = text())
    val tree = tree(Views.Watch, list, item)
    assertEquals(emptyList(), evaluate(tree).of(FindingRule.RoundEdgeClipped))
    // The same item on a screen that does not scroll is clipped.
    val fixed = node("list", "Screen", w = 204f, h = 204f)
    assertEquals(
      listOf("item"),
      evaluate(tree(Views.Watch, fixed, item)).of(FindingRule.RoundEdgeClipped).map { it.id },
    )
  }

  @Test
  fun `round-edge-clipped skips a node nested in a scrolling list`() {
    val list = node("list", "Screen", w = 204f, h = 204f, scrolls = true)
    val row = node("row", "Row", 0f, 0f, 204f, 52f, parent = "list")
    val button = node("b", "IconButton", 0f, 0f, 52f, 52f, parent = "row")
    assertEquals(
      emptyList(),
      evaluate(tree(Views.Watch, list, row, button)).of(FindingRule.RoundEdgeClipped),
    )
  }

  @Test
  fun `round-edge-clipped skips components made for the round edge`() {
    val edge = node("edge", "EdgeButton", 40f, 150f, 124f, 54f, text = text("Done"))
    val ring = node("ring", "CircularProgressIndicator", w = 204f, h = 204f)
    val clock = node("clock", "TimeText", 60f, 0f, 84f, 24f, text = text("10:09"))
    assertEquals(
      emptyList(),
      evaluate(tree(Views.Watch, edge, ring, clock)).of(FindingRule.RoundEdgeClipped),
    )
    WearCatalog.roundEdgeNames
      .filter { it != "TimeText" }
      .forEach {
        assertTrue(WearCatalog.find(it) != null, it)
      }
  }

  // A full-screen ring takes a band at the edge: its padding and stroke, read from the tree.
  private val ringBand = RingInset(padding = 4f, stroke = 8f)

  private fun ring(inset: RingInset? = ringBand, size: Float = 204f) =
    node("ring", "CircularProgressIndicator", w = size, h = size, ring = inset)

  private fun line(y: Float, width: Float = 84f) =
    node(
      "stats",
      "Text",
      x = 102f - width / 2,
      y = y,
      w = width,
      h = 16f,
      text = text("3.1 / 5 km", ink = Bounds(102f - width / 2, y, width, 16f)),
    )

  @Test
  fun `round-edge-clipped fails for a text inside the circle but inside the ring band`() {
    // The corners are 92.1 dp from the center: inside the 102 dp circle, past the 90 dp ring band.
    val findings =
      evaluate(tree(Views.Watch, ring(), line(y = 20f))).of(FindingRule.RoundEdgeClipped)
    assertEquals(listOf("stats"), findings.map { it.id })
    assertEquals(Severity.Error, findings.single().severity)
    assertEquals("reaches 2.1 dp into the progress ring", findings.single().detail)
  }

  @Test
  fun `round-edge-clipped passes for the same text further in from the ring`() {
    assertEquals(
      emptyList(),
      evaluate(tree(Views.Watch, ring(), line(y = 30f))).of(FindingRule.RoundEdgeClipped),
    )
  }

  @Test
  fun `round-edge-clipped ignores a ring that did not record its band`() {
    assertEquals(
      emptyList(),
      evaluate(tree(Views.Watch, ring(inset = null), line(y = 20f)))
        .of(FindingRule.RoundEdgeClipped),
    )
  }

  @Test
  fun `round-edge-clipped ignores a text outside a small ring`() {
    // A small spinner in the corner: the text is not drawn inside it.
    val spinner = ring(size = 48f)
    assertEquals(
      emptyList(),
      evaluate(tree(Views.Watch, spinner, line(y = 20f))).of(FindingRule.RoundEdgeClipped),
    )
  }

  // A ring above an edge button: 140 dp across at the top, centered at (102, 70), its band leaving
  // a 58 dp circle inside.
  private fun bodyRing() = ring(size = 140f).copy(bounds = Bounds(32f, 0f, 140f, 140f))

  @Test
  fun `round-edge-clipped fails for a text inside a smaller ring but inside its band`() {
    // The corners are 65.3 dp from the ring's center, past its 58 dp inner circle.
    val findings =
      evaluate(tree(Views.Watch, bodyRing(), line(y = 20f))).of(FindingRule.RoundEdgeClipped)
    assertEquals(listOf("stats"), findings.map { it.id })
    assertEquals("reaches 7.3 dp into the progress ring", findings.single().detail)
  }

  @Test
  fun `round-edge-clipped passes for a control outside a smaller ring`() {
    // Centered 101 dp from the ring's center, outside the ring, and inside the display.
    val below = iconButton("below", 76f, y = 145f)
    assertEquals(
      emptyList(),
      evaluate(tree(Views.Watch, bodyRing(), below)).of(FindingRule.RoundEdgeClipped),
    )
  }

  @Test
  fun `round-edge-clipped reports the round edge first for a text outside the circle`() {
    val findings =
      evaluate(tree(Views.Watch, ring(), line(y = 4f, width = 204f)))
        .of(FindingRule.RoundEdgeClipped)
    assertEquals("reaches 39.4 dp outside the round edge", findings.single().detail)
  }

  @Test
  fun `round-edge-clipped uses the widest band when two rings fill the view`() {
    val second = ring(RingInset(4f, 12f)).copy(id = "inner")
    val findings =
      evaluate(tree(Views.Watch, ring(), second, line(y = 25f))).of(FindingRule.RoundEdgeClipped)
    assertEquals(listOf("stats"), findings.map { it.id })
    assertEquals("reaches 1.7 dp into the progress ring", findings.single().detail)
  }

  @Test
  fun `round-edge-clipped skips nodes behind an open dialog`() {
    val dialog = node("sure", "AlertDialog", w = 204f, h = 204f)
    val behind = iconButton("behind", 0f)
    val inside = iconButton("inside", 0f, parent = "sure")
    val findings =
      evaluate(tree(Views.Watch, behind, dialog, inside)).of(FindingRule.RoundEdgeClipped)
    assertEquals(listOf("inside"), findings.map { it.id })
  }

  @Test
  fun `round-edge-clipped does not apply to a phone view`() {
    val wide = tree(Views.Light, iconButton("a", 0f, y = 0f), iconButton("b", 359f, y = 871f))
    assertEquals(emptyList(), evaluate(wide).of(FindingRule.RoundEdgeClipped))
  }

  // endregion

  // region primary-largest on Wear

  @Test
  fun `primary-largest runs on a Wear tree from the Wear catalog`() {
    val small = node("go", "IconButton", 76f, 20f, 52f, 52f, primary = true)
    val big = node("more", "Button", 20f, 90f, 164f, 60f, text = text())
    val findings = evaluate(tree(Views.Watch, small, big)).of(FindingRule.PrimaryLargest)
    assertEquals(listOf("go"), findings.map { it.id })
    assertEquals(
      emptyList(),
      evaluate(tree(Views.Watch, big.copy(primary = true), small.copy(primary = false)))
        .of(FindingRule.PrimaryLargest),
    )
  }

  // endregion

  // region output

  @Test
  fun `findings follow rule order and then document order`() {
    val tiny = node("tiny", "IconButton", w = 32f, h = 32f, touch = null)
    val georgia = node("t", "Text", text = text(font = "Georgia"), typeRole = "bodyLarge")
    val rules = evaluate(tree(nodes = arrayOf(georgia, tiny))).map { it.rule }
    assertEquals(listOf(FindingRule.TouchTarget, FindingRule.Font), rules)
  }

  @Test
  fun `a clean tree has no findings and passes`() {
    val clean = tree(nodes = arrayOf(button("cook", primary = true)))
    assertEquals(emptyList(), evaluate(clean))
    assertTrue(clean.pass)
  }

  @Test
  fun `every rule that needs the catalog names a real component`() {
    FindingRules.fabTypes.forEach { assertTrue(M3ExpressiveCatalog.find(it) != null, it) }
    FindingRules.modalTypes.forEach {
      assertTrue(M3ExpressiveCatalog.find(it)?.overlay == true, it)
    }
    assertNull(M3ExpressiveCatalog.find("NotAComponent"))
  }

  // endregion
}
