package bot.charlies.androiddesign.preview.feature.preview

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.ScrollWheel
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performMouseInput
import androidx.wear.compose.material3.CircularProgressIndicatorDefaults
import androidx.wear.compose.material3.Typography
import bot.charlies.androiddesign.preview.data.ProposalCodec
import bot.charlies.androiddesign.preview.data.ProposalParse
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.domain.findings.FindingRules
import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessRenderer
import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessResult
import bot.charlies.androiddesign.preview.feature.preview.headless.RenderSource
import bot.charlies.androiddesign.preview.feature.preview.render.ProposalHostState
import bot.charlies.androiddesign.preview.feature.preview.theme.toHex
import bot.charlies.androiddesign.preview.feature.preview.theme.wear.WearThemeResolver
import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.NodeColors
import bot.charlies.androiddesign.preview.model.NodeRoles
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.Severity
import bot.charlies.androiddesign.preview.model.TextInfo
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import bot.charlies.androiddesign.preview.model.WearCatalog
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image

/**
 * The Wear fixture drawn with the real Wear Compose components in a round device: the picture, the
 * layout tree, overlays, navigation, the `large` size override and the theme.
 */
@OptIn(ExperimentalTestApi::class)
class WearRenderTest {
  private val proposal: Proposal =
    assertIs<ProposalParse.Parsed>(
        ProposalCodec.parse(
          Files.readString(Path.of(System.getProperty("fixtures.dir"), "wear.proposal.json")),
        ),
      )
      .proposal

  private fun design(theme: ThemeOverride? = proposal.theme): DesignSystem =
    DesignSystemResolver.resolve(null, theme, WearCatalog)

  private val source = RenderSource(proposal, design(), null)

  private fun draw(
    screen: String,
    view: ViewSpec = Views.Watch,
    source: RenderSource = this.source,
    afterLaunch: (androidx.compose.ui.test.ComposeUiTest.(ProposalHostState) -> Unit)? = null,
  ): HeadlessResult = HeadlessRenderer.image(source, screen, view, afterLaunch)

  private fun LayoutTree.has(id: String) = nodes.any { it.id == id }

  private fun LayoutTree.node(id: String): LayoutNode =
    nodes.firstOrNull { it.id == id } ?: error("no node '$id' in ${nodes.map { it.id }}")

  private fun decode(png: ByteArray): Bitmap =
    Bitmap().also { bitmap ->
      Image.makeFromEncoded(png).use { image ->
        bitmap.allocPixels(image.imageInfo.withColorType(ColorType.RGBA_8888))
        check(image.readPixels(bitmap))
      }
    }

  private fun Bitmap.argb(x: Int, y: Int): Int = getColor(x, y)

  @Test
  fun `the fixture draws round, in both watch views, with its components in the tree`() {
    for (view in Views.wear) {
      val result = draw("home", view)
      val bitmap = decode(result.png)
      val px = (view.width * HeadlessRenderer.DENSITY).toInt()
      assertEquals(px, bitmap.width)
      // The corners are outside the circle: nothing is drawn there.
      for ((x, y) in listOf(0 to 0, px - 1 to 0, 0 to px - 1, px - 1 to px - 1)) {
        assertEquals(0, bitmap.argb(x, y) ushr 24, "corner ($x, $y) of ${view.name} is drawn")
      }
      val colors = HashSet<Int>()
      for (x in 0 until px step 3) for (y in 0 until px step 3) colors += bitmap.argb(x, y)
      assertTrue(colors.size > 40, "${view.name} is mostly empty: ${colors.size} colors")
      assertEquals(view.name, result.tree.view.name)
      for (id in listOf("root", "header", "ring")) {
        assertTrue(result.tree.has(id), "no '$id' in ${result.tree.nodes.map { it.id }}")
      }
    }
  }

  @Test
  fun `the whole fixture renders with no error finding`() {
    val failures = buildList {
      for (screen in proposal.screens.keys) {
        for (view in Views.wear) {
          val errors = draw(screen, view).tree.findings.filter { it.severity == Severity.Error }
          if (errors.isNotEmpty()) add("$screen/${view.name}: $errors")
        }
      }
    }
    assertEquals(emptyList(), failures)
  }

  @Test
  fun `a button row wider than the round display reports round-edge-clipped`() {
    val clipped =
      assertIs<ProposalParse.Parsed>(
          ProposalCodec.parse(
            Files.readString(
              Path.of(System.getProperty("fixtures.dir"), "wear-clipped.proposal.json"),
            ),
          ),
        )
        .proposal
    val result = draw("home", Views.Watch, RenderSource(clipped, design(clipped.theme), null))
    val hits = result.tree.findings.filter { it.rule == FindingRule.RoundEdgeClipped }
    assertTrue(
      hits.any { it.id == "stop" || it.id == "resume" },
      "no clip finding: ${result.tree.findings}",
    )
    assertTrue(hits.none { it.id == "ring" }, "the ring is made for the edge: $hits")
    assertEquals(
      listOf("stop", "resume"),
      hits.map { it.id },
      "only the buttons are clipped, the centered texts fit: $hits",
    )
    val shipped =
      Views.wear.flatMap { view ->
        proposal.screens.keys.flatMap { draw(it, view).tree.findings }
      }
    assertEquals(emptyList(), shipped.filter { it.rule == FindingRule.RoundEdgeClipped })
  }

  /** A fixed run screen: a full-screen ring and a Column of text lines inside it. */
  private fun runScreen(topPadding: String): Proposal {
    val json =
      """
      {"format":1,"navigation":{"start":"home"},
       "screens":{"home":{"catalogId":"${WearCatalog.ID}","components":[
        {"id":"root","component":"Screen","scroll":false,"timeText":false,"children":["ring","col"]},
        {"id":"ring","component":"CircularProgressIndicator","progress":0.62},
        {"id":"col","component":"Column","horizontalAlignment":"center","children":["gap","dist","time","stats"]},
        {"id":"gap","component":"Spacer","height":"$topPadding"},
        {"id":"dist","component":"Text","text":"3.1 / 5 km \u00b7 1.9 left","type":"labelMedium","align":"center"},
        {"id":"time","component":"Text","text":"24:18","type":"numeralLarge","align":"center"},
        {"id":"stats","component":"Text","text":"5:12 /km \u00b7 148 bpm","type":"bodySmall","align":"center"}]}}}
      """
        .trimIndent()
    return assertIs<ProposalParse.Parsed>(ProposalCodec.parse(json)).proposal
  }

  @Test
  fun `a full-screen ring records the band it takes from Wear's defaults`() {
    val parsed = runScreen("none")
    val tree = draw("home", Views.WatchLarge, RenderSource(parsed, design(parsed.theme), null)).tree
    val ring = assertNotNull(tree.node("ring").ring)
    assertEquals(CircularProgressIndicatorDefaults.FullScreenPadding.value, ring.padding)
    assertTrue(ring.stroke > 0f, "a stroke width: $ring")
    assertEquals(null, tree.node("col").ring, "only a ring records a band")
  }

  @Test
  fun `a line inside a full-screen ring's band reports round-edge-clipped`() {
    // The distance line sits inside the circle but reaches into the ring; the numeral and the
    // stats below it are clear.
    val parsed = runScreen("xl")
    val tree = draw("home", Views.WatchLarge, RenderSource(parsed, design(parsed.theme), null)).tree
    val hits = tree.findings.filter { it.rule == FindingRule.RoundEdgeClipped }
    assertEquals(listOf("dist"), hits.map { it.id }, "${tree.findings}")
    assertTrue("into the progress ring" in hits.single().detail.orEmpty(), "${hits.single()}")
    // Without the recorded band the round edge alone lets the line through.
    val unaware = tree.copy(nodes = tree.nodes.map { it.copy(ring = null) }, findings = emptyList())
    assertEquals(
      emptyList(),
      FindingRules.evaluate(unaware).filter { it.rule == FindingRule.RoundEdgeClipped },
    )
  }

  @Test
  fun `the same line further in from the ring passes`() {
    val parsed = runScreen("xxxl")
    val tree = draw("home", Views.WatchLarge, RenderSource(parsed, design(parsed.theme), null)).tree
    assertEquals(
      emptyList(),
      tree.findings.filter { it.rule == FindingRule.RoundEdgeClipped },
      "${tree.nodes.map { it.id + it.text?.ink }}",
    )
  }

  @Test
  fun `a stepper's content is drawn in onSurface, Wear's default for it`() {
    val tree = draw("adjust", Views.Watch).tree
    assertEquals(
      design().let { WearThemeResolver.resolve(it).color("onSurface").toHex() },
      tree.node("minutes").colors?.content,
      "${tree.node("minutes").roles}",
    )
    assertEquals("onSurface", tree.node("minutes").roles?.content)
  }

  @Test
  fun `a primary icon button smaller than a rival button reports primary-largest`() {
    val json =
      """
      {"format":1,"navigation":{"start":"home"},
       "screens":{"home":{"catalogId":"${WearCatalog.ID}","components":[
        {"id":"root","component":"Screen","scroll":true,"timeText":false,"children":["go","more"]},
        {"id":"go","component":"IconButton","icon":"play_arrow","size":"small","primary":true},
        {"id":"more","component":"Button","label":"More"}]}}}
      """
        .trimIndent()
    val parsed = assertIs<ProposalParse.Parsed>(ProposalCodec.parse(json)).proposal
    val result = draw("home", source = RenderSource(parsed, design(parsed.theme), null))
    assertTrue(result.tree.node("go").primary)
    assertEquals(
      listOf("go"),
      result.tree.findings.filter { it.rule == FindingRule.PrimaryLargest }.map { it.id },
    )
  }

  @Test
  fun `a fixed screen shows its edge button in full at the bottom`() {
    val json =
      """
      {"format":1,"navigation":{"start":"home"},
       "screens":{"home":{"catalogId":"${WearCatalog.ID}","components":[
        {"id":"root","component":"Screen","scroll":false,"edgeButton":"pause","children":["col"]},
        {"id":"col","component":"Column","horizontalAlignment":"center","arrangement":"center","children":["t"]},
        {"id":"t","component":"Text","text":"Running","align":"center"},
        {"id":"pause","component":"EdgeButton","label":"Pause"}]}}}
      """
        .trimIndent()
    val parsed = assertIs<ProposalParse.Parsed>(ProposalCodec.parse(json)).proposal
    for (view in Views.wear) {
      val tree = draw("home", view, RenderSource(parsed, design(parsed.theme), null)).tree
      val edge = tree.node("pause").bounds
      assertTrue(edge.width > 0f && edge.height > 0f, "${view.name}: edge button is $edge")
      assertTrue(
        edge.y + edge.height > view.height - 6f,
        "${view.name}: edge button ends at ${edge.y + edge.height} of ${view.height}",
      )
      // The body stops above the button.
      val col = tree.node("col").bounds
      assertTrue(col.y + col.height <= edge.y + 1f, "${view.name}: body $col over button $edge")
    }
  }

  @Test
  fun `a fixed screen without an edge button has a full size body`() {
    val json =
      """
      {"format":1,"navigation":{"start":"home"},
       "screens":{"home":{"catalogId":"${WearCatalog.ID}","components":[
        {"id":"root","component":"Screen","scroll":false,"timeText":false,"children":["col"]},
        {"id":"col","component":"Column","horizontalAlignment":"center","arrangement":"center","children":["t"]},
        {"id":"t","component":"Text","text":"Running","align":"center"}]}}}
      """
        .trimIndent()
    val parsed = assertIs<ProposalParse.Parsed>(ProposalCodec.parse(json)).proposal
    for (view in Views.wear) {
      val tree = draw("home", view, RenderSource(parsed, design(parsed.theme), null)).tree
      assertEquals(
        Bounds(0f, 0f, view.width.toFloat(), view.height.toFloat()),
        tree.node("col").bounds,
      )
    }
  }

  @Test
  fun `scrolling a list to its end brings up the edge button and the items below`() {
    val settings =
      draw("settings") { _ ->
        onRoot().performMouseInput {
          moveTo(center)
          scroll(60f, ScrollWheel.Vertical)
        }
        waitForIdle()
        mainClock.advanceTimeBy(1500)
        waitForIdle()
      }
    assertTrue(settings.tree.has("goal"), "the title card is below the first screenful")
    assertTrue(settings.tree.has("done"), "the edge button is at the end of the list")
    val edge = settings.tree.node("done").bounds
    assertTrue(edge.y + edge.height > 180, "the edge button sits at the bottom: $edge")
  }

  @Test
  fun `the tree of a scrolling screen covers the whole list, not the top of it`() {
    val tree = HeadlessRenderer.tree(source, "settings", Views.Watch)
    assertTrue(tree.has("goal"), "an item below the first screenful: ${tree.nodes.map { it.id }}")
    assertTrue(tree.has("done"), "the edge button at the end of the list")
    val edge = tree.node("done").bounds
    assertTrue(
      edge.y + edge.height > 180,
      "the edge button is kept where it is shown in full: $edge",
    )
    val ids = tree.nodes.map { it.id }
    assertEquals(ids.distinct(), ids, "a node seen at several positions is kept once")
  }

  @Test
  fun `a scrolling screen's picture stays at the top and an end picture shows the last row`() {
    val result = draw("settings")
    val end = assertNotNull(result.end, "a scrolling screen has an end render")
    assertTrue(end.tree.has("done"), "the end shows the edge button")
    assertTrue(end.tree.has("goal"), "and the last rows")
    assertTrue(
      !end.tree.has("mode"),
      "the top of the list is scrolled away: ${end.tree.nodes.map { it.id }}",
    )
    assertTrue(!result.png.contentEquals(end.png), "the end picture differs from the top one")
    assertNull(draw("adjust").end, "a screen that does not scroll has none")
  }

  @Test
  fun `an overlay render opens that overlay and does not scroll the list behind it`() {
    val opened = HeadlessRenderer.image(source, "settings", Views.Watch, overlay = "sure")
    assertTrue(opened.tree.has("sure") && opened.tree.has("yes"), "the dialog is drawn")
    assertNull(opened.end, "nothing scrolls behind a dialog")
    val tree = HeadlessRenderer.tree(source, "settings", Views.Watch, overlay = "sure")
    assertTrue(tree.has("sure"))
    assertTrue(!tree.has("goal"), "the list behind it stays at the top")
  }

  @Test
  fun `an overlay opened by open shows in the tree`() {
    val live =
      ProposalHostState(proposal, null, "settings").also {
        it.interaction.open("settings", "sure")
      }
    val tree = HeadlessRenderer.tree(source, "settings", Views.Watch, live)
    assertTrue(tree.has("sure"), "the AlertDialog is in ${tree.nodes.map { it.id }}")
    assertTrue(tree.has("yes") && tree.has("no"), "its buttons are drawn")
    val closed = HeadlessRenderer.tree(source, "settings", Views.Watch)
    assertTrue(!closed.has("sure"), "closed, the dialog is not drawn")
  }

  @Test
  fun `an AlertDialog whose content fits is not recorded as scrolling`() {
    // Upstream measures the content and uses a fixed layout when it fits, a list only when not.
    val tree = HeadlessRenderer.tree(source, "settings", Views.Watch, overlay = "sure")
    assertTrue(!tree.node("sure").scrolls, "the dialog's content fits, so it is fixed")
    assertTrue(!tree.node("done").scrolls, "the screen behind it is unchanged")
  }

  @Test
  fun `an AlertDialog whose content does not fit is recorded as scrolling`() {
    val long =
      assertIs<ProposalParse.Parsed>(
          ProposalCodec.parse(
            Files.readString(
              Path.of(System.getProperty("fixtures.dir"), "wear-long-dialog.proposal.json"),
            ),
          ),
        )
        .proposal
    val tree =
      HeadlessRenderer.tree(
        RenderSource(long, design(long.theme), null),
        "home",
        Views.Watch,
        overlay = "sure",
      )
    assertTrue(tree.node("sure").scrolls, "${tree.nodes.map { it.id to it.bounds }}")
  }

  @Test
  fun `an AlertDialog that only just overflows is recorded as scrolling`() {
    // Its buttons are partly on screen, so their clipped box ends at the dialog's bottom edge.
    val borderline =
      assertIs<ProposalParse.Parsed>(
          ProposalCodec.parse(
            Files.readString(
              Path.of(System.getProperty("fixtures.dir"), "wear-borderline-dialog.proposal.json"),
            ),
          ),
        )
        .proposal
    val tree =
      HeadlessRenderer.tree(
        RenderSource(borderline, design(borderline.theme), null),
        "home",
        Views.Watch,
        overlay = "sure",
      )
    val shown = tree.nodes.map { it.id to it.bounds }
    assertTrue(tree.node("sure").scrolls, "$shown")
    assertEquals(
      emptyList(),
      tree.findings.filter { it.rule == FindingRule.RoundEdgeClipped },
      "$shown",
    )
  }

  @Test
  fun `an AlertDialog's own buttons keep their size and stay inside the circle`() {
    // Upstream places the buttons at their own size in a centered row; they never share it.
    val tree = HeadlessRenderer.tree(source, "settings", Views.Watch, overlay = "sure")
    for (id in listOf("yes", "no")) {
      assertTrue(tree.node(id).bounds.width <= 60f, "$id is ${tree.node(id).bounds}")
    }
    assertEquals(
      emptyList(),
      tree.findings.filter { it.rule == FindingRule.RoundEdgeClipped },
      "${tree.nodes.filter { it.id in listOf("yes", "no") }.map { it.bounds }}",
    )
  }

  @Test
  fun `navigate and back move between screens`() {
    val forward = draw("home") { host -> host.navigate("settings") }
    assertEquals("settings", forward.tree.screen)
    assertTrue(forward.tree.has("mode"))
    val back =
      draw("home") { host ->
        host.navigate("settings")
        waitForIdle()
        host.back()
      }
    assertEquals("home", back.tree.screen)
    assertTrue(back.tree.has("ring"))
  }

  @Test
  fun `a large size override changes the large watch and not the small one`() {
    val small = draw("home", Views.Watch).tree.node("ring")
    val large = draw("home", Views.WatchLarge).tree.node("ring")
    assertEquals("numeralLarge", small.roles?.type)
    assertEquals("numeralExtraLarge", large.roles?.type)
    assertTrue(large.bounds.height > small.bounds.height, "${large.bounds} vs ${small.bounds}")
  }

  @Test
  fun `the default scheme is the upstream one and a seed themes it`() {
    val upstream = androidx.wear.compose.material3.ColorScheme()
    assertEquals(upstream.primary, WearThemeResolver.colorScheme(design()).primary)
    val red = design(ThemeOverride(colors = mapOf("primary" to "#D32F2F")))
    val seeded = WearThemeResolver.colorScheme(red)
    assertNotEquals(upstream.primary, seeded.primary)
    assertNotEquals(upstream.primaryContainer, seeded.primaryContainer)
    // Wear's own mapping gives the same role from the system's fixed colors, dim included.
    assertNotEquals(seeded.primary, seeded.primaryDim)
  }

  @Test
  fun `a seed changes the color a render draws with`() {
    val plain = draw("settings").tree.node("mode").colors?.container
    val red = design(ThemeOverride(colors = mapOf("primary" to "#D32F2F")))
    val themed = draw("settings", source = RenderSource(proposal, red, null)).tree.node("mode")
    assertNotEquals(plain, themed.colors?.container)
    assertEquals(
      WearThemeResolver.colorScheme(red).primaryContainer.toHex(),
      themed.colors?.container,
    )
  }

  @Test
  fun `a primaryDim override is honored`() {
    val themed = design(ThemeOverride(colors = mapOf("primary-dim" to "#123456")))
    assertEquals("#123456", themed.colorOverrides["primaryDim"])
    val ring = draw("home", source = RenderSource(proposal, themed, null)).tree.node("ring")
    assertEquals("#123456", ring.colors?.content)
  }

  @Test
  fun `the finding rules read the wear type sizes from the upstream scale`() {
    val typography = Typography()
    val styles =
      mapOf(
        "displayLarge" to typography.displayLarge,
        "displayMedium" to typography.displayMedium,
        "displaySmall" to typography.displaySmall,
        "titleLarge" to typography.titleLarge,
        "titleMedium" to typography.titleMedium,
        "titleSmall" to typography.titleSmall,
        "labelLarge" to typography.labelLarge,
        "labelMedium" to typography.labelMedium,
        "labelSmall" to typography.labelSmall,
        "bodyLarge" to typography.bodyLarge,
        "bodyMedium" to typography.bodyMedium,
        "bodySmall" to typography.bodySmall,
        "bodyExtraSmall" to typography.bodyExtraSmall,
        "numeralExtraLarge" to typography.numeralExtraLarge,
        "numeralLarge" to typography.numeralLarge,
        "numeralMedium" to typography.numeralMedium,
        "numeralSmall" to typography.numeralSmall,
        "numeralExtraSmall" to typography.numeralExtraSmall,
      )
    assertEquals(WearCatalog.typeRoles.keys, styles.keys)
    // 3.5:1 is enough for large text (24 sp and up) and short of the 4.5:1 other text needs.
    val node = { role: String ->
      LayoutNode(
        id = role,
        type = "Text",
        bounds = Bounds(0f, 0f, 50f, 20f),
        text = TextInfo("Aa", 1, overflow = false, truncated = false, font = "Roboto Flex"),
        roles = NodeRoles("background", "onBackground", role, null),
        colors = NodeColors("#FFFFFF", "#8A8A8A"),
      )
    }
    for ((role, style) in styles) {
      val tree = LayoutTree("home", Views.Watch, listOf(node(role)), emptyList())
      val failsContrast = FindingRules.evaluate(tree).any { it.rule == FindingRule.Contrast }
      assertEquals(
        style.fontSize.value < LARGE_TEXT_SP,
        failsContrast,
        "$role at ${style.fontSize.value} sp",
      )
    }
  }

  @Test
  fun `a wear proposal next to a phone DESIGN_md validates, renders and takes its seed`() {
    val dir = Files.createTempDirectory("wear-next-to-phone")
    Files.copy(Path.of(System.getProperty("fixtures.dir"), "DESIGN.md"), dir.resolve("DESIGN.md"))
    val file = dir.resolve("wear.proposal.json")
    Files.copy(Path.of(System.getProperty("fixtures.dir"), "wear.proposal.json"), file)
    val snapshot =
      kotlinx.coroutines.runBlocking {
        bot.charlies.androiddesign.preview.data.FileProposalRepository().load(file)
      }
    assertEquals(emptyList(), snapshot.errors)
    val themed = snapshot.designSystem
    assertEquals(emptyMap(), themed.typography)
    val mode = draw("settings", source = RenderSource(proposal, themed, null)).tree.node("mode")
    assertEquals(
      WearThemeResolver.colorScheme(themed).primaryContainer.toHex(),
      mode.colors?.container,
    )
    assertNotEquals(draw("settings").tree.node("mode").colors?.container, mode.colors?.container)
  }

  @Test
  fun `a style on a layout paints its container and color with wear roles`() {
    val json =
      """
      {"format":1,"navigation":{"start":"home"},
       "theme":{"material":{"styles":{"panel":{"container":"surfaceContainerHigh","color":"primary"}}}},
       "screens":{"home":{"catalogId":"${WearCatalog.ID}","components":[
        {"id":"root","component":"Screen","scroll":false,"timeText":false,"children":["col"]},
        {"id":"col","component":"Column","style":"panel","children":["t"]},
        {"id":"t","component":"Text","text":"Hello"}]}}}
      """
        .trimIndent()
    val styled = assertIs<ProposalParse.Parsed>(ProposalCodec.parse(json)).proposal
    val styledDesign = design(styled.theme)
    val result = draw("home", source = RenderSource(styled, styledDesign, null))
    val scheme = WearThemeResolver.colorScheme(styledDesign)
    val col = result.tree.node("col")
    assertEquals(scheme.surfaceContainerHigh.toHex(), col.colors?.container)
    assertEquals(scheme.primary.toHex(), col.colors?.content)
    val text = result.tree.node("t")
    assertEquals(scheme.surfaceContainerHigh.toHex(), text.colors?.container)
    assertEquals(scheme.primary.toHex(), text.colors?.content)
    val pixels = decode(result.png)
    val p = col.bounds
    val x = ((p.x + p.width / 2) * HeadlessRenderer.DENSITY).toInt()
    val y = ((p.y + p.height - 2) * HeadlessRenderer.DENSITY).toInt()
    assertEquals(
      scheme.surfaceContainerHigh.toHex(),
      "#%06X".format(pixels.argb(x, y) and 0xFFFFFF),
    )
  }

  private companion object {
    const val LARGE_TEXT_SP = 24f
  }
}
