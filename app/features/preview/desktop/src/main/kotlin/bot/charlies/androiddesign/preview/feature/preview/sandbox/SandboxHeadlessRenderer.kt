package bot.charlies.androiddesign.preview.feature.preview.sandbox

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.runtime.tooling.LocalInspectionTables
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import bot.charlies.androiddesign.preview.domain.findings.ScrolledTree
import bot.charlies.androiddesign.preview.kit.BoardSurface
import bot.charlies.androiddesign.preview.kit.DirectionSpec
import bot.charlies.androiddesign.preview.kit.ExploreBoardFrame
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.ViewSpec
import java.util.Collections
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

/**
 * One headless render: the layout tree in dp, and a PNG at the density requested. A scrolling Wear
 * screen is drawn at the top of its list and its [tree] covers the whole list; [end] is the list
 * scrolled to its end, and null for every other screen.
 */
class HeadlessResult(val tree: LayoutTree, val png: ByteArray, val end: HeadlessEnd? = null) {
  override fun toString() = "HeadlessResult(${tree.screen}/${tree.view.name}, ${png.size} bytes)"
}

/** A scrolling list drawn at its end: the tree of what is on screen there, and its PNG. */
class HeadlessEnd(val tree: LayoutTree, val png: ByteArray)

/**
 * Renders one compiled `.explore.kt` screen headlessly using `DesktopComposeUiTest`, captures the
 * Skia bitmap (for 2 px perimeter contrast sampling and optional PNG encoding), and extracts a
 * checked [LayoutTree] from [LocalInspectionTables] and semantics trees.
 */
object SandboxHeadlessRenderer {
  const val DENSITY = 2f
  private const val SETTLE_MS = 1500L
  private const val SCROLL_SETTLE_MS = 500L
  private const val MAX_SCROLL_STEPS = 60

  private val fontResolver by lazy { createFontFamilyResolver() }
  private val renderLock = Any()

  fun render(
    compiled: CompileResult.Compiled,
    direction: DirectionSpec,
    screenId: String,
    view: ViewSpec,
    designSystem: DesignSystem,
    overlayId: String? = null,
    density: Float = DENSITY,
    encodePng: Boolean = true,
  ): HeadlessResult =
    synchronized(renderLock) {
      drawLocked(compiled, direction, screenId, view, designSystem, overlayId, density, encodePng)
    }

  @OptIn(ExperimentalTestApi::class, ComposeToolingApi::class)
  private fun drawLocked(
    compiled: CompileResult.Compiled,
    direction: DirectionSpec,
    screenId: String,
    view: ViewSpec,
    designSystem: DesignSystem,
    overlayId: String?,
    pixelsPerDp: Float,
    encodePng: Boolean,
  ): HeadlessResult {
    val density = Density(pixelsPerDp)
    val test =
      DesktopComposeUiTest(
        width = (view.width * pixelsPerDp).toInt(),
        height = (view.height * pixelsPerDp).toInt(),
        density = density,
        useStandardTestDispatcherForComposition = false,
      )
    val inspectionTables = Collections.synchronizedSet(LinkedHashSet<CompositionData>())
    var rootCoordinates: LayoutCoordinates? = null
    var emptyDetailScreen: String? = null
    lateinit var tree: LayoutTree
    var pngBytes = ByteArray(0)
    var endResult: HeadlessEnd? = null

    test.runTest {
      setContent {
        CompositionLocalProvider(LocalFontFamilyResolver provides fontResolver) {
          Box(Modifier.onGloballyPositioned { rootCoordinates = it }) {
            InspectedSubcomposition(inspectionTables) {
              ExploreBoardFrame(
                board = compiled.board,
                direction = direction,
                screenId = screenId,
                view = view,
                designSystem = designSystem,
                explorationDir = compiled.explorationDir,
                overlayId = overlayId,
                onEmptyDetail = { emptyDetailScreen = it },
              )
            }
          }
        }
      }
      settle(SETTLE_MS)

      fun takeSnapshot(): Pair<LayoutTree, Bitmap> {
        val mergedRoots = onAllNodes(isRoot(), useUnmergedTree = false).fetchSemanticsNodes()
        val unmergedRoots = onAllNodes(isRoot(), useUnmergedTree = true).fetchSemanticsNodes()
        val bitmap = captureToImage().asSkiaBitmap()
        val emptyLists = listOfNotNull(emptyDetailScreen)
        val extracted =
          SlotTreeExtractor.extract(
            tables = inspectionTables,
            mergedRoots = mergedRoots,
            unmergedRoots = unmergedRoots,
            rootCoordinates = rootCoordinates,
            bitmap = bitmap,
            screen = screenId,
            view = view,
            surface = compiled.board.surface,
            explorationFile = compiled.sourceFile,
            sourceText = compiled.sourceText,
            density = pixelsPerDp,
            emptyDetailLists = emptyLists,
          )
        return extracted to bitmap
      }

      val (firstTree, firstBitmap) = takeSnapshot()
      tree = firstTree
      if (encodePng) pngBytes = firstBitmap.toPng()

      if (compiled.board.surface == BoardSurface.Wear && overlayId == null) {
        val scrollable =
          findVerticalScroller(onAllNodes(isRoot(), useUnmergedTree = true).fetchSemanticsNodes())
        if (scrollable != null) {
          val positions = mutableListOf(firstTree)
          var lastBitmap: Bitmap? = null
          val stepPx = view.height * pixelsPerDp / 2f
          scrollWearToEnd(stepPx) {
            val (snapTree, snapBitmap) = takeSnapshot()
            positions += snapTree
            lastBitmap = snapBitmap
          }
          if (positions.size > 1) {
            tree = ScrolledTree.merge(positions)
            val endPng = if (encodePng) lastBitmap?.toPng() ?: ByteArray(0) else ByteArray(0)
            endResult = HeadlessEnd(positions.last(), endPng)
          }
        }
      }
    }
    return HeadlessResult(tree, pngBytes, endResult)
  }

  @OptIn(ComposeToolingApi::class)
  @Composable
  private fun InspectedSubcomposition(
    tables: MutableSet<CompositionData>,
    content: @Composable () -> Unit,
  ) {
    CompositionLocalProvider(LocalInspectionTables provides tables) {
      SubcomposeLayout { constraints ->
        val placeables =
          subcompose(Unit) {
              currentComposer.collectParameterInformation()
              content()
            }
            .map { it.measure(constraints) }
        val width = placeables.maxOfOrNull { it.width } ?: constraints.minWidth
        val height = placeables.maxOfOrNull { it.height } ?: constraints.minHeight
        layout(width, height) { placeables.forEach { it.placeRelative(0, 0) } }
      }
    }
  }

  private fun findVerticalScroller(roots: List<SemanticsNode>): SemanticsNode? {
    for (root in roots) {
      val range = root.config.getOrNull(SemanticsProperties.VerticalScrollAxisRange)
      if (range != null && root.config.contains(SemanticsActions.ScrollBy)) {
        return root
      }
      findVerticalScroller(root.children)?.let {
        return it
      }
    }
    return null
  }

  @OptIn(ExperimentalTestApi::class)
  private fun ComposeUiTest.scrollWearToEnd(stepPx: Float, onStep: () -> Unit) {
    var steps = 0
    var moved = false
    while (steps++ < MAX_SCROLL_STEPS) {
      val scroller =
        findVerticalScroller(onAllNodes(isRoot(), useUnmergedTree = true).fetchSemanticsNodes())
          ?: break
      val range = scroller.config.getOrNull(SemanticsProperties.VerticalScrollAxisRange) ?: break
      val currentValue = range.value()
      val maxValue = range.maxValue()
      if (currentValue >= maxValue - 0.5f) break
      val action = scroller.config.getOrNull(SemanticsActions.ScrollBy)?.action ?: break
      val scrolled = runOnIdle { action(0f, stepPx) }
      if (!scrolled) break
      moved = true
      settle(SCROLL_SETTLE_MS)
      onStep()
    }
    if (moved) {
      settle(SETTLE_MS)
      onStep()
    }
  }

  @OptIn(ExperimentalTestApi::class)
  private fun ComposeUiTest.settle(ms: Long) {
    mainClock.autoAdvance = false
    waitForIdle()
    if (ms > 0) {
      mainClock.advanceTimeBy(ms)
      waitForIdle()
    }
  }

  private fun Bitmap.toPng(): ByteArray =
    Image.makeFromBitmap(this).use { image ->
      checkNotNull(image.encodeToData(EncodedImageFormat.PNG)) { "PNG encoding failed" }.bytes
    }
}
