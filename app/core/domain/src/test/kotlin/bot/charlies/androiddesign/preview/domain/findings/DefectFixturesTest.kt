package bot.charlies.androiddesign.preview.domain.findings

import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The v2 seeded-defect fixtures (`tests/evals/defects/`) as synthetic layout trees. Each test is
 * named after its fixture. The starter is v2's proposal starter: a headline, supporting text, a
 * small primary button, and either a floating toolbar (compact) or a navigation rail (expanded).
 * Every defect must fail exactly the rule it seeds, and every control must pass.
 *
 * Fixtures that cannot happen in v3 are listed in the report, not here: lint-only defects (raw hex,
 * inline style, font size outside a voice, unknown icon, expressive palette), HTML overlap and
 * scroller geometry, mid-word breaks, CSS line-height clipping, a modal open on load, a native
 * button, and the HTML navigation drawer.
 */
class DefectFixturesTest {
  private val surface = "#FEF7FF"
  private val onSurface = "#1D1B20"
  private val onSurfaceVariant = "#49454F"

  /** The starter at [view]: a compact view gets the floating toolbar, wider ones the rail. */
  private fun starter(view: ViewSpec = Views.Light): List<LayoutNode> {
    val width = view.width.toFloat()
    val compact = view.width < 840
    val nodes = mutableListOf<LayoutNode>()
    nodes += node("screen", "Column", w = width, h = view.height.toFloat(), container = surface)
    nodes +=
      node(
        "headline",
        "Text",
        x = 16f,
        y = 80f,
        w = 200f,
        h = 40f,
        parent = "screen",
        text = text("Headline"),
        typeRole = "headlineLargeEmphasized",
        content = onSurface,
      )
    nodes +=
      node(
        "supporting",
        "Text",
        x = 16f,
        y = 130f,
        w = 300f,
        h = 24f,
        parent = "screen",
        text = text("Replace this screen with the proposal."),
        typeRole = "bodyLarge",
        content = onSurfaceVariant,
      )
    nodes += button("add", y = 170f, primary = true, parent = "screen", label = "Add")
    if (compact) {
      nodes += node("toolbar", "FloatingToolbar", x = 60f, y = 840f, w = 290f, h = 64f)
      listOf("Home", "Saved", "Plan").forEachIndexed { i, label ->
        nodes +=
          node(
            label.lowercase(),
            "ToggleButton",
            x = 70f + i * 90f,
            y = 848f,
            w = 80f,
            h = 48f,
            parent = "toolbar",
            touch = Bounds(70f + i * 90f, 848f, 80f, 48f),
            text = text(label),
            typeRole = "labelLarge",
            content = onSurfaceVariant,
            container = surface,
          )
      }
    } else {
      nodes += node("rail", "NavigationRail", w = 96f, h = view.height.toFloat())
      listOf("Home", "Saved", "Plan").forEachIndexed { i, label ->
        nodes +=
          node(
            label.lowercase(),
            "NavigationRailItem",
            y = 40f + i * 64f,
            w = 96f,
            h = 64f,
            parent = "rail",
            touch = Bounds(0f, 40f + i * 64f, 96f, 64f),
            text = text(label),
            typeRole = "labelMedium",
            content = onSurfaceVariant,
            container = surface,
          )
      }
    }
    return nodes
  }

  private fun findings(
    view: ViewSpec,
    vararg extra: LayoutNode,
    replace: Map<String, LayoutNode> = emptyMap(),
  ) = evaluate(tree(view, *(starter(view).map { replace[it.id] ?: it } + extra).toTypedArray()))

  private fun assertOnly(
    rule: FindingRule,
    found: List<bot.charlies.androiddesign.preview.model.Finding>,
    detail: Regex? = null,
  ) {
    assertEquals(listOf(rule), found.rules(), found.toString())
    if (detail != null)
      assertTrue(detail.containsMatchIn(found.first().detail.orEmpty()), found.toString())
  }

  // region controls on the starter

  @Test
  fun control() {
    Views.all.forEach { view -> assertEquals(emptyList(), findings(view), view.name) }
  }

  @Test
  fun `control-offscreen`() {
    val skip = node("skip", "Box", x = -9999f, y = 0f, w = 100f, h = 48f)
    val drawer = node("drawer", "Box", x = -280f, y = 0f, w = 280f, h = 923f)
    assertEquals(emptyList(), findings(Views.Light, skip, drawer))
  }

  // endregion

  // region touch targets

  @Test
  fun `tiny-target`() {
    val tiny = node("tiny", "IconButton", x = 16f, y = 230f, w = 32f, h = 32f, parent = "screen")
    assertOnly(FindingRule.TouchTarget, findings(Views.Light, tiny), Regex("32 x 32"))
  }

  @Test
  fun `control-slider`() {
    // Compose's whole 48 dp Slider is the touch target.
    val slider = node("volume", "Slider", x = 16f, y = 230f, w = 280f, h = 48f, parent = "screen")
    assertEquals(emptyList(), findings(Views.Light, slider))
  }

  @Test
  fun `slider-short`() {
    val slider = node("volume", "Slider", x = 16f, y = 230f, w = 280f, h = 44f, parent = "screen")
    assertOnly(FindingRule.TouchTarget, findings(Views.Light, slider), Regex("280 x 44"))
  }

  @Test
  fun `control-text-field`() {
    val amount =
      node(
        "amount",
        "TextField",
        x = 16f,
        y = 230f,
        w = 280f,
        h = 56f,
        parent = "screen",
        text = text("", lines = 1),
      )
    val note =
      node(
        "note",
        "TextField",
        x = 16f,
        y = 300f,
        w = 280f,
        h = 56f,
        parent = "screen",
        text = text("Short note"),
      )
    assertEquals(emptyList(), findings(Views.Light, amount, note))
  }

  // endregion

  // region fonts

  @Test
  fun `non-flex-font`() {
    val georgia = starter().first { it.id == "supporting" }.copy(text = text(font = "Georgia"))
    assertOnly(
      FindingRule.Font,
      findings(Views.Light, replace = mapOf("supporting" to georgia)),
      Regex("Georgia is not a bundled font"),
    )
  }

  @Test
  fun `brand-font-small`() {
    val small = starter().first { it.id == "supporting" }.copy(text = text(font = "Fraunces"))
    assertOnly(
      FindingRule.Font,
      findings(Views.Light, replace = mapOf("supporting" to small)),
      Regex("Fraunces is for display and headline text"),
    )
  }

  @Test
  fun `control-brand-font`() {
    val display =
      node(
        "hero",
        "Text",
        x = 16f,
        y = 230f,
        w = 300f,
        h = 64f,
        parent = "screen",
        text = text("Golden Hour", font = "Fraunces"),
        typeRole = "displayLarge",
        content = onSurface,
      )
    assertEquals(emptyList(), findings(Views.Light, display))
  }

  // endregion

  // region contrast

  @Test
  fun `low-contrast`() {
    // The supporting text drawn in surface container high on the surface.
    val faint =
      starter()
        .first { it.id == "supporting" }
        .let { it.copy(colors = it.colors?.copy(content = "#ECE6F0")) }
    assertOnly(
      FindingRule.Contrast,
      findings(Views.Light, replace = mapOf("supporting" to faint)),
      Regex("needs 4.5:1"),
    )
  }

  @Test
  fun `sheet-low-contrast`() {
    // The sheet's own title matches its container; the page behind it is not measured.
    val sheet = node("queue", "BottomSheet", y = 500f, w = 411f, h = 400f, container = "#F3EDF7")
    val title =
      node(
        "queue_title",
        "Text",
        parent = "queue",
        y = 540f,
        w = 200f,
        h = 28f,
        text = text("Queue"),
        typeRole = "headlineSmall",
        content = "#F3EDF7",
      )
    val found = findings(Views.Light, sheet, title)
    assertOnly(FindingRule.Contrast, found)
    assertEquals(listOf("queue_title"), found.map { it.id })
  }

  @Test
  fun `dialog-low-contrast`() {
    // Measured against the dialog's container, not the page behind it.
    val dialog =
      node("credits", "Dialog", x = 40f, y = 300f, w = 331f, h = 220f, container = "#ECE6F0")
    val body =
      node(
        "credits_body",
        "Text",
        parent = "credits",
        x = 64f,
        y = 380f,
        w = 280f,
        h = 40f,
        text = text("Credits"),
        typeRole = "bodyMedium",
        content = "#E0DAE4",
      )
    val found = findings(Views.Light, dialog, body)
    assertOnly(FindingRule.Contrast, found)
    assertEquals(listOf("credits_body"), found.map { it.id })
  }

  // endregion

  // region overflow and clipping

  @Test
  fun `desktop-only`() {
    val wide =
      starter(Views.Desktop)
        .first { it.id == "add" }
        .let { it.copy(bounds = it.bounds.copy(width = 2400f)) }
    assertOnly(
      FindingRule.Offscreen,
      findings(Views.Desktop, replace = mapOf("add" to wide)),
      Regex("ends at 2416 dp"),
    )
    // The same proposal fits every smaller view.
    listOf(Views.Light, Views.Dark, Views.Foldable, Views.Tablet).forEach {
      assertEquals(emptyList(), findings(it), it.name)
    }
  }

  @Test
  fun `foldable-only`() {
    val wide =
      starter(Views.Foldable)
        .first { it.id == "add" }
        .let { it.copy(bounds = it.bounds.copy(width = 2000f)) }
    assertOnly(
      FindingRule.Offscreen,
      findings(Views.Foldable, replace = mapOf("add" to wide)),
      Regex("ends at 2016 dp"),
    )
    assertEquals(emptyList(), findings(Views.Tablet))
    assertEquals(emptyList(), findings(Views.Desktop))
  }

  @Test
  fun `fixed-right`() {
    val pin = node("pin", "Box", x = 411f - 160f + 40f, y = 880f, w = 160f, h = 40f)
    assertOnly(FindingRule.Offscreen, findings(Views.Light, pin), Regex("ends at 451 dp"))
  }

  @Test
  fun `fixed-left`() {
    val pin = node("pin", "Box", x = -40f, y = 880f, w = 160f, h = 40f)
    assertOnly(FindingRule.Offscreen, findings(Views.Light, pin), Regex("starts at -40 dp"))
  }

  @Test
  fun `text-clipped-no-ellipsis`() {
    val clipped =
      node(
        "subject",
        "Text",
        x = 16f,
        y = 230f,
        w = 379f,
        h = 24f,
        parent = "screen",
        text = text("Quarterly planning notes from the whole design team", overflow = true),
        typeRole = "bodyLarge",
        content = onSurface,
      )
    assertOnly(FindingRule.TextClipped, findings(Views.Light, clipped))
  }

  @Test
  fun `control-truncate-ellipsis`() {
    // A single-line ellipsis and a two-line clamp are intentional truncation.
    val oneLine =
      node(
        "one",
        "Text",
        x = 16f,
        y = 230f,
        w = 379f,
        h = 24f,
        parent = "screen",
        text = text("Long", overflow = true, truncated = true),
        typeRole = "bodyLarge",
        content = onSurface,
      )
    val clamp =
      node(
        "two",
        "Text",
        x = 16f,
        y = 270f,
        w = 379f,
        h = 48f,
        parent = "screen",
        text = text("Longer", lines = 2, overflow = true, truncated = true),
        typeRole = "bodyLarge",
        content = onSurface,
      )
    val found = findings(Views.Light, oneLine, clamp)
    assertEquals(listOf(FindingRule.TextTruncated), found.rules())
    assertTrue(tree(nodes = emptyArray()).copy(findings = found).pass)
  }

  @Test
  fun `button-label-ellipsis`() {
    val label =
      button(
        "open",
        y = 230f,
        w = 160f,
        parent = "screen",
        label = "Open packing list for the whole trip",
      )
    val truncated =
      label.copy(
        text = text("Open packing list for the whole trip", overflow = true, truncated = true),
      )
    assertOnly(FindingRule.LabelTruncated, findings(Views.Light, truncated))
  }

  @Test
  fun `ellipsized text excuses overflow on a chip but not its label`() {
    val chip =
      node(
        "chip",
        "FilterChip",
        x = 16f,
        y = 230f,
        w = 100f,
        h = 32f,
        parent = "screen",
        touch = Bounds(16f, 222f, 100f, 48f),
        text = text("Everything", overflow = true, truncated = true),
      )
    assertOnly(FindingRule.LabelTruncated, findings(Views.Light, chip))
  }

  // endregion

  // region primary action

  @Test
  fun `primary-outsized`() {
    // A regular extra-large button outsizes the primary FAB.
    val screen = starter().filterNot { it.id == "add" }
    val fab =
      node(
        "compose",
        "FloatingActionButton",
        x = 315f,
        y = 827f,
        w = 56f,
        h = 56f,
        primary = true,
        touch = Bounds(315f, 827f, 56f, 56f),
      )
    val archive =
      node(
        "archive",
        "Button",
        x = 16f,
        y = 230f,
        w = 379f,
        h = 96f,
        parent = "screen",
        touch = Bounds(16f, 230f, 379f, 96f),
        text = text("Archive"),
        typeRole = "labelLarge",
        content = "#FFFFFF",
        container = "#6750A4",
      )
    val found = evaluate(tree(nodes = (screen + fab + archive).toTypedArray()))
    assertOnly(FindingRule.PrimaryLargest, found, Regex("shorter than archive"))
  }

  @Test
  fun `primary-outsized-fab`() {
    // A FAB (80 dp) is taller than the small (40 dp) primary button.
    val fab =
      node(
        "compose",
        "FloatingActionButton",
        x = 315f,
        y = 827f,
        w = 80f,
        h = 80f,
        touch = Bounds(315f, 827f, 80f, 80f),
      )
    assertOnly(
      FindingRule.PrimaryLargest,
      findings(Views.Light, fab),
      Regex("shorter than compose"),
    )
  }

  @Test
  fun `control-primary-with-slider`() {
    val slider = node("volume", "Slider", x = 16f, y = 230f, w = 379f, h = 96f, parent = "screen")
    assertEquals(emptyList(), findings(Views.Light, slider))
  }

  @Test
  fun `control-primary-with-nav-rail`() {
    val rail = node("rail2", "NavigationRail", w = 96f, h = 923f)
    val item =
      node(
        "big",
        "NavigationRailItem",
        parent = "rail2",
        w = 96f,
        h = 120f,
        touch = Bounds(0f, 0f, 96f, 120f),
      )
    assertEquals(emptyList(), findings(Views.Light, rail, item))
  }

  @Test
  fun `control-primary-with-content`() {
    val row = node("row", "ListItem", x = 0f, y = 230f, w = 411f, h = 88f, parent = "screen")
    val card = node("card", "Card", x = 16f, y = 330f, w = 379f, h = 200f, parent = "screen")
    assertEquals(emptyList(), findings(Views.Light, row, card))
  }

  @Test
  fun `control-primary-equal-buttons`() {
    val peer = button("cancel", x = 130f, y = 170f, h = 40f, parent = "screen", label = "Cancel")
    assertEquals(emptyList(), findings(Views.Light, peer))
  }

  @Test
  fun `control-primary-default-with-toolbar`() {
    // The starter itself: a 40 dp primary beside 48 dp toolbar toggles.
    assertEquals(emptyList(), findings(Views.Light))
  }

  @Test
  fun `control-primary-small-peers`() {
    val peer =
      button(
        "cancel",
        x = 130f,
        y = 170f,
        w = 240f,
        h = 40f,
        parent = "screen",
        label = "Cancel the whole itinerary",
      )
    assertEquals(emptyList(), findings(Views.Light, peer))
  }

  @Test
  fun `control-primary-with-icon-buttons`() {
    // A standard icon button is a 48 dp touch box around a 40 dp visual.
    val icon =
      node(
        "more",
        "IconButton",
        x = 300f,
        y = 170f,
        w = 40f,
        h = 40f,
        parent = "screen",
        touch = Bounds(296f, 166f, 48f, 48f),
      )
    assertEquals(emptyList(), findings(Views.Light, icon))
  }

  // endregion

  // region open overlays

  @Test
  fun `control-sheet-open`() {
    // A modal sheet covers the starter's text and floating toolbar.
    val sheet = node("queue", "BottomSheet", y = 500f, w = 411f, h = 423f, container = "#F3EDF7")
    val title =
      node(
        "queue_title",
        "Text",
        parent = "queue",
        x = 24f,
        y = 540f,
        w = 200f,
        h = 28f,
        text = text("Queue"),
        typeRole = "headlineSmall",
        content = onSurface,
      )
    val close = button("queue_close", y = 600f, parent = "queue", label = "Close")
    assertEquals(emptyList(), findings(Views.Light, sheet, title, close))
  }

  @Test
  fun `control-dialog-open`() {
    val dialog =
      node("credits", "Dialog", x = 40f, y = 300f, w = 331f, h = 220f, container = "#ECE6F0")
    val body =
      node(
        "credits_body",
        "Text",
        parent = "credits",
        x = 64f,
        y = 380f,
        w = 280f,
        h = 40f,
        text = text("Credits"),
        typeRole = "bodyMedium",
        content = onSurface,
      )
    val ok = button("credits_ok", x = 300f, y = 460f, w = 64f, parent = "credits", label = "OK")
    assertEquals(emptyList(), findings(Views.Light, dialog, body, ok))
  }

  @Test
  fun `control-menu-open`() {
    // A menu opened over the primary button covers it and exempts nothing.
    val menu = node("more", "Menu", x = 16f, y = 170f, w = 200f, h = 144f, container = "#F3EDF7")
    val edit =
      node(
        "edit",
        "MenuItem",
        parent = "more",
        x = 16f,
        y = 170f,
        w = 200f,
        h = 48f,
        touch = Bounds(16f, 170f, 200f, 48f),
        text = text("Edit"),
        typeRole = "labelLarge",
        content = onSurface,
      )
    assertEquals(emptyList(), findings(Views.Light, menu, edit))
  }

  @Test
  fun `sheet-text-overflow`() {
    val sheet = node("queue", "BottomSheet", y = 500f, w = 411f, h = 423f, container = "#F3EDF7")
    val long =
      node(
        "queue_note",
        "Text",
        parent = "queue",
        x = 24f,
        y = 540f,
        w = 363f,
        h = 24f,
        text = text("A queue note that is cut off", overflow = true),
        typeRole = "bodyLarge",
        content = onSurface,
      )
    val found = findings(Views.Light, sheet, long)
    assertOnly(FindingRule.TextClipped, found)
    assertEquals(listOf("queue_note"), found.map { it.id })
  }

  // endregion
}
