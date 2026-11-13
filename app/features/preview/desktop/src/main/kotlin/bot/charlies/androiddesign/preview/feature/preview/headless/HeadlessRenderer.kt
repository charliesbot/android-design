package bot.charlies.androiddesign.preview.feature.preview.headless

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import bot.charlies.androiddesign.preview.domain.findings.ScrolledTree
import bot.charlies.androiddesign.preview.feature.preview.nav.ProposalPreview
import bot.charlies.androiddesign.preview.feature.preview.record.SemanticsIndex
import bot.charlies.androiddesign.preview.feature.preview.record.TreeRecorder
import bot.charlies.androiddesign.preview.feature.preview.render.ProposalHostState
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.ViewSpec
import java.nio.file.Path
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

/**
 * One headless render: the layout tree in dp, and a PNG at the density it was asked for. A
 * scrolling Wear screen is drawn at the top of its list and its [tree] covers the whole list (the
 * render scrolls it); [end] is the list scrolled to its end, and null for every other screen.
 */
class HeadlessResult(val tree: LayoutTree, val png: ByteArray, val end: HeadlessEnd? = null) {
  override fun toString() = "HeadlessResult(${tree.screen}/${tree.view.name}, ${png.size} bytes)"
}

/** A scrolling list drawn at its end: the tree of what is on screen there, and its picture. */
class HeadlessEnd(val tree: LayoutTree, val png: ByteArray)

/** What a draw produced; the picture and the end are there only when an image was wanted. */
private class Drawn(val tree: LayoutTree, val png: ByteArray?, val end: HeadlessEnd?)

/**
 * What a render draws from: the [proposal], the [design] system it is styled with, and [dir], the
 * folder its relative asset paths resolve against (null when it has none).
 */
data class RenderSource(val proposal: Proposal, val design: DesignSystem, val dir: Path?)

/**
 * Draws a proposal screen without a window, with the same composables the window uses, and reads
 * the result back. It runs on Compose's own UI test harness (`runDesktopComposeUiTest`'s engine),
 * which is public API and needs no display.
 *
 * The engine owns global state, so renders never overlap: a call made while another is drawing
 * waits for it, whichever thread it comes from. Callers need no lock of their own.
 */
object HeadlessRenderer {
  /**
   * Default pixels per dp in the PNG. Bounds in the tree stay in dp, so the image is the density
   * times the view's dp size in each direction.
   */
  const val DENSITY = 2f

  /**
   * One font resolver for every render. Each test scene would otherwise build its own, and a new
   * resolver copies every font it draws with into native memory again: the 15 MB symbols font and
   * the 4 MB Flex font, on every render. Renders run one at a time ([renderLock]), so sharing is
   * safe.
   */
  private val fontResolver by lazy { createFontFamilyResolver() }

  private val renderLock = Any()

  /**
   * Renders [screen] of [source] at [view] with a PNG, for the CLI. The back stack is the shortest
   * `navigate` path from the start screen, so a detail screen at an expanded width shows beside its
   * list.
   *
   * @param replay A running host whose back stack and local state (open overlays, toggles, typed
   *   text) are taken over before the tree is read, so the result shows what that host shows.
   * @param overlay The id of an overlay of [screen] to draw open, on top of the replayed state.
   * @param density Pixels per dp of the PNG.
   */
  @OptIn(ExperimentalTestApi::class)
  fun image(
    source: RenderSource,
    screen: String,
    view: ViewSpec,
    afterLaunch: (ComposeUiTest.(ProposalHostState) -> Unit)? = null,
    replay: ProposalHostState? = null,
    overlay: String? = null,
    density: Float = DENSITY,
  ): HeadlessResult {
    val drawn =
      draw(source, screen, view, afterLaunch, replay, overlay, imageWanted = true, density)
    return HeadlessResult(drawn.tree, checkNotNull(drawn.png), drawn.end)
  }

  /**
   * Renders only the layout tree of [screen] at [view], for `get_tree` and for checking that a
   * proposal draws. No image is captured or encoded, and the clock advances only as far as the tree
   * needs: not at all for a fresh screen that does not scroll, for a scrolling Wear list the time
   * each scroll position takes to settle, and for a [replay] or an [overlay] the time its open
   * overlays and its scene change take to come to rest. See [image] for the parameters.
   */
  @OptIn(ExperimentalTestApi::class)
  fun tree(
    source: RenderSource,
    screen: String,
    view: ViewSpec,
    replay: ProposalHostState? = null,
    overlay: String? = null,
  ): LayoutTree =
    draw(source, screen, view, null, replay, overlay, imageWanted = false, DENSITY).tree

  /** Draws, and captures the image as well when [imageWanted]. */
  private fun draw(
    source: RenderSource,
    screen: String,
    view: ViewSpec,
    afterLaunch: (ComposeUiTest.(ProposalHostState) -> Unit)?,
    replay: ProposalHostState?,
    overlay: String?,
    imageWanted: Boolean,
    pixelsPerDp: Float,
  ): Drawn =
    synchronized(renderLock) {
      drawLocked(source, screen, view, afterLaunch, replay, overlay, imageWanted, pixelsPerDp)
    }

  /** The work of [draw], done while holding [renderLock]. */
  @OptIn(ExperimentalTestApi::class)
  private fun drawLocked(
    source: RenderSource,
    screen: String,
    view: ViewSpec,
    afterLaunch: (ComposeUiTest.(ProposalHostState) -> Unit)?,
    replay: ProposalHostState?,
    overlay: String?,
    imageWanted: Boolean,
    pixelsPerDp: Float,
  ): Drawn {
    val (proposal, design, proposalDir) = source
    require(screen in proposal.screens) { "no screen '$screen' in this proposal" }
    val density = Density(pixelsPerDp)
    val test =
      DesktopComposeUiTest(
        width = (view.width * pixelsPerDp).toInt(),
        height = (view.height * pixelsPerDp).toInt(),
        density = density,
        useStandardTestDispatcherForComposition = false,
      )
    val host = ProposalHostState(proposal, proposalDir, screen)
    val recorder = TreeRecorder()
    lateinit var tree: LayoutTree
    var png: ByteArray? = null
    var end: HeadlessEnd? = null
    test.runTest {
      setContent {
        CompositionLocalProvider(LocalFontFamilyResolver provides fontResolver) {
          ProposalPreview(host, design, view, recorder = recorder)
        }
      }
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
      overlay?.let {
        host.interaction.open(host.current, it)
        settle(if (imageWanted) SETTLE_MS else OVERLAY_SETTLE_MS)
      }
      fun snapshot(): LayoutTree {
        val semantics =
          SemanticsIndex.of(
            mergedRoots = onAllNodes(isRoot(), useUnmergedTree = false).fetchSemanticsNodes(),
            unmergedRoots = onAllNodes(isRoot(), useUnmergedTree = true).fetchSemanticsNodes(),
          )
        return recorder.snapshot(host.current, view, density.density, semantics)
      }
      fun capture(): ByteArray = captureToImage().asSkiaBitmap().toPng()
      tree = snapshot()
      if (imageWanted) png = capture()
      // A lazy list composes only the items on screen, so the top of a long list is all that one
      // tree holds. Scroll it to its end and merge what each position showed. Nothing scrolls
      // behind an open overlay, which covers the list.
      val list = recorder.scrollable
      if (list != null && host.interaction.openOverlay(host.current) == null) {
        val positions = mutableListOf(tree)
        scrollToEnd(list, view.height * density.density / 2f) { positions += snapshot() }
        // A list that fits the screen does not move and has no other end.
        if (positions.size > 1) {
          tree = ScrolledTree.merge(positions)
          if (imageWanted) end = HeadlessEnd(positions.last(), capture())
        }
      }
    }
    return Drawn(tree, png, end)
  }

  /**
   * Scrolls [list] forward by [stepPx] at a time until it cannot go further, calling [record] at
   * each position after the list has come to rest, and once more at the end, where an edge button
   * has had time to take its full size. A list that cannot move records nothing. Half a viewport
   * per step shows every item whole at some position. It runs in the list's public scroll API, as a
   * wheel or a finger would drive it.
   */
  @OptIn(ExperimentalTestApi::class)
  private fun ComposeUiTest.scrollToEnd(list: ScrollableState, stepPx: Float, record: () -> Unit) {
    var steps = 0
    var moved = false
    while (runOnIdle { list.canScrollForward } && steps++ < MAX_SCROLL_STEPS) {
      val consumed = runOnIdle { runBlocking { list.scrollBy(stepPx) } }
      if (consumed == 0f) break
      moved = true
      settle(SCROLL_SETTLE_MS)
      record()
    }
    if (moved) {
      settle(SETTLE_MS)
      record()
    }
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
  private const val SCROLL_SETTLE_MS = 500L

  /** Bounds a list that never reports its end, such as one that grows as it scrolls. */
  private const val MAX_SCROLL_STEPS = 60

  private fun org.jetbrains.skia.Bitmap.toPng(): ByteArray =
    Image.makeFromBitmap(this).use { image ->
      checkNotNull(image.encodeToData(EncodedImageFormat.PNG)) { "PNG encoding failed" }.bytes
    }
}
