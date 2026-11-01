package bot.charlies.androiddesign.preview.feature.preview

import bot.charlies.androiddesign.preview.data.ProposalCodec
import bot.charlies.androiddesign.preview.data.ProposalParse
import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessRenderer
import bot.charlies.androiddesign.preview.feature.preview.render.ProposalHostState
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * The tree-only render skips the image and most of the clock. It must still read the same tree as
 * the render that waits a long time and takes a picture, or `get_tree` and the pre-render check
 * would describe something the window does not show.
 */
class TreeOnlyRenderTest {
  private val verify = Path.of(System.getProperty("repo.dir"), "evals/verify/verify.proposal.json")
  private val proposal: Proposal =
    assertIs<ProposalParse.Parsed>(ProposalCodec.parse(Files.readString(verify))).proposal
  private val design = designOf(proposal.theme)
  private val views = listOf(Views.Light, Views.Foldable, Views.Desktop)
  private val overlayKinds = setOf("BottomSheet", "Dialog", "Menu")

  private fun json(tree: LayoutTree) =
    PreviewJson.pretty.encodeToString(LayoutTree.serializer(), tree)

  private fun compare(
    screen: String,
    view: ViewSpec,
    replay: ProposalHostState? = null,
  ): String? {
    val quick = HeadlessRenderer.renderTree(proposal, design, null, screen, view, replay)
    val slow = HeadlessRenderer.render(proposal, design, null, screen, view, replay = replay).tree
    return if (json(quick) == json(slow)) null else "$screen/${view.name}"
  }

  @Test
  fun `a fresh screen reads the same tree without waiting`() {
    val different =
      proposal.screens.keys.flatMap { screen -> views.mapNotNull { compare(screen, it) } }
    assertEquals(emptyList(), different)
  }

  @Test
  fun `a replayed back stack reads the same tree after its short wait`() {
    val different =
      proposal.screens.keys.flatMap { screen ->
        views.mapNotNull { view ->
          val live = ProposalHostState(proposal, null).also { it.showScreen(screen) }
          compare(proposal.navigation.start, view, live)?.let { "stack $screen: $it" }
        }
      }
    assertEquals(emptyList(), different)
  }

  @Test
  fun `a replayed open overlay reads the same tree after its short wait`() {
    val overlays =
      proposal.screens.flatMap { (screen, body) ->
        body.components.filter { it.component in overlayKinds }.map { screen to it.id }
      }
    assertTrue(overlays.isNotEmpty(), "the verify proposal has overlays")
    val different = overlays.flatMap { (screen, overlay) ->
      listOf(Views.Light, Views.Desktop).mapNotNull { view ->
        val live =
          ProposalHostState(proposal, null, screen).also { it.interaction.open(screen, overlay) }
        compare(screen, view, live)?.let { "overlay $overlay: $it" }
      }
    }
    assertEquals(emptyList(), different)
  }

  /** A host on [screen] with local [values] set the way a tap would set them. */
  private fun liveWith(screen: String, vararg values: Pair<String, Any>): ProposalHostState =
    ProposalHostState(proposal, null, screen).also { host ->
      values.forEach { (key, value) -> host.interaction["$screen::$key"] = value }
    }

  @Test
  fun `a replayed expanded fab menu reads the same tree after its short wait`() {
    val live = liveWith("overlays", "fab_menu:expanded" to true)
    val tree = HeadlessRenderer.renderTree(proposal, design, null, "overlays", Views.Light, live)
    val closed = HeadlessRenderer.renderTree(proposal, design, null, "overlays", Views.Light)
    assertTrue(
      tree.nodes.first { it.id == "fab_item_note" }.bounds.height >
        closed.nodes.first { it.id == "fab_item_note" }.bounds.height,
      "the expanded menu shows its items",
    )
    assertEquals(
      emptyList(),
      listOfNotNull(
        compare("overlays", Views.Light, live),
        compare("overlays", Views.Desktop, live),
      ),
    )
  }

  @Test
  fun `replayed selections and switches read the same tree after their short wait`() {
    val lives =
      listOf(
        "inputs" to liveWith("inputs", "filter_elevated:selected" to true),
        "inputs" to liveWith("inputs", "selection:chip_group" to 2),
        "inputs" to liveWith("inputs", "switch_off:checked" to true, "switch_on:checked" to false),
        "inputs" to liveWith("inputs", "tb_erase:checked" to true, "tb_draw:checked" to false),
        "actions" to liveWith("actions", "con_week:checked" to true, "con_day:checked" to false),
        "actions" to liveWith("actions", "toggle_outlined:checked" to true),
      )
    val different = lives.flatMapIndexed { index, (screen, live) ->
      views.mapNotNull { view -> compare(screen, view, live)?.let { "case $index: $it" } }
    }
    assertEquals(emptyList(), different)
  }
}
