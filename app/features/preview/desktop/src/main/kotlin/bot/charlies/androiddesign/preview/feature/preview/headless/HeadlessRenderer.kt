package bot.charlies.androiddesign.preview.feature.preview.headless

import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.unit.Density
import bot.charlies.androiddesign.preview.feature.preview.nav.ProposalPreview
import bot.charlies.androiddesign.preview.feature.preview.record.SemanticsIndex
import bot.charlies.androiddesign.preview.feature.preview.record.TreeRecorder
import bot.charlies.androiddesign.preview.feature.preview.render.ProposalHostState
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.ViewSpec
import java.nio.file.Path
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

/** One headless render: the layout tree in dp, and a PNG at [HeadlessRenderer.DENSITY]. */
class HeadlessResult(val tree: LayoutTree, val png: ByteArray) {
  override fun toString() = "HeadlessResult(${tree.screen}/${tree.view.name}, ${png.size} bytes)"
}

/**
 * Draws a proposal screen without a window, with the same composables the window uses, and reads
 * the result back. It runs on Compose's own UI test harness (`runDesktopComposeUiTest`'s engine),
 * which is public API and needs no display.
 */
object HeadlessRenderer {
  /**
   * Pixels per dp in the PNG. Bounds in the tree stay in dp, so the image is [DENSITY] times the
   * view's dp size in each direction.
   */
  const val DENSITY = 2f

  /**
   * Renders [screen] at [view] with a PNG, for the CLI. The back stack is the shortest `navigate`
   * path from the start screen, so a detail screen at an expanded width shows beside its list.
   *
   * @param replay A running host whose back stack and local state (open overlays, toggles, typed
   *   text) are taken over before the tree is read, so the result shows what that host shows.
   */
  @OptIn(ExperimentalTestApi::class)
  fun render(
    proposal: Proposal,
    design: DesignSystem,
    proposalDir: Path?,
    screen: String,
    view: ViewSpec,
    afterLaunch: (ComposeUiTest.(ProposalHostState) -> Unit)? = null,
    replay: ProposalHostState? = null,
  ): HeadlessResult {
    var png: ByteArray? = null
    val tree =
      draw(proposal, design, proposalDir, screen, view, afterLaunch, replay) { test ->
        png = test.captureToImage().asSkiaBitmap().toPng()
      }
    return HeadlessResult(tree, checkNotNull(png))
  }

  /**
   * Renders only the layout tree of [screen] at [view], for `get_tree` and for checking that a
   * proposal draws. No image is captured or encoded, and the clock advances only as far as the tree
   * needs: not at all for a fresh screen, and for a [replay] only the time its open overlays and
   * its scene change take to come to rest. See [render] for the parameters.
   */
  @OptIn(ExperimentalTestApi::class)
  fun renderTree(
    proposal: Proposal,
    design: DesignSystem,
    proposalDir: Path?,
    screen: String,
    view: ViewSpec,
    replay: ProposalHostState? = null,
  ): LayoutTree = draw(proposal, design, proposalDir, screen, view, null, replay, capture = null)

  /** Draws, and calls [capture] with the test when the caller wants the image as well. */
  @OptIn(ExperimentalTestApi::class)
  private fun draw(
    proposal: Proposal,
    design: DesignSystem,
    proposalDir: Path?,
    screen: String,
    view: ViewSpec,
    afterLaunch: (ComposeUiTest.(ProposalHostState) -> Unit)?,
    replay: ProposalHostState?,
    capture: ((SkikoComposeUiTest) -> Unit)?,
  ): LayoutTree {
    require(screen in proposal.screens) { "no screen '$screen' in this proposal" }
    val density = Density(DENSITY)
    val test =
      DesktopComposeUiTest(
        width = (view.width * DENSITY).toInt(),
        height = (view.height * DENSITY).toInt(),
        density = density,
        useStandardTestDispatcherForComposition = false,
      )
    val host = ProposalHostState(proposal, proposalDir, screen)
    val recorder = TreeRecorder()
    val imageWanted = capture != null
    lateinit var tree: LayoutTree
    test.runTest {
      setContent { ProposalPreview(host, design, view, recorder = recorder) }
      settle(if (imageWanted) SETTLE_MS else 0)
      replay?.let {
        val time = if (imageWanted) SETTLE_MS else host.timeToAdopt(it)
        host.adoptStateOf(it)
        settle(time)
      }
      afterLaunch?.let { hook ->
        hook(this, host)
        settle(SETTLE_MS)
      }
      val semantics =
        SemanticsIndex.of(
          mergedRoots = onAllNodes(isRoot(), useUnmergedTree = false).fetchSemanticsNodes(),
          unmergedRoots = onAllNodes(isRoot(), useUnmergedTree = true).fetchSemanticsNodes(),
        )
      tree = recorder.snapshot(host.current, view, density.density, semantics)
      capture?.invoke(this)
    }
    return tree
  }

  /**
   * How long the clock must run after this host takes over the state of [other] for the tree to be
   * at rest: an open overlay slides or fades in, a changed back stack plays a scene transition, and
   * a local value (a toggle, a selection) drives animations that only move with the clock, such as
   * the items of an expanded FAB menu. Only state that is all in place after composition (nothing
   * held, nothing moved) needs no time. The times are the shortest measured ones with a margin, and
   * tests compare them with a long settle.
   */
  private fun ProposalHostState.timeToAdopt(other: ProposalHostState): Long =
    when {
      other.backStack.any { other.interaction.openOverlay(it) != null } -> OVERLAY_SETTLE_MS
      other.interaction.hasValues() -> VALUE_SETTLE_MS
      other.backStack.toList() != backStack.toList() -> SCENE_SETTLE_MS
      else -> 0L
    }

  /**
   * Lets composition, layout and the entrance animations (sheets, scene changes) finish. Time is
   * driven by hand so that endless animations such as progress indicators cannot keep it busy.
   */
  fun ComposeUiTest.settle() = settle(SETTLE_MS)

  @OptIn(ExperimentalTestApi::class)
  private fun ComposeUiTest.settle(ms: Long) {
    mainClock.autoAdvance = false
    waitForIdle()
    if (ms > 0) {
      mainClock.advanceTimeBy(ms)
      waitForIdle()
    }
  }

  private const val SETTLE_MS = 1500L
  private const val OVERLAY_SETTLE_MS = 600L
  private const val VALUE_SETTLE_MS = 600L
  private const val SCENE_SETTLE_MS = 200L

  private fun org.jetbrains.skia.Bitmap.toPng(): ByteArray =
    Image.makeFromBitmap(this).use { image ->
      checkNotNull(image.encodeToData(EncodedImageFormat.PNG)) { "PNG encoding failed" }.bytes
    }
}
