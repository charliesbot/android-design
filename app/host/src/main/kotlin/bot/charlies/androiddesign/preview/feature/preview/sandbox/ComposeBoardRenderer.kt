package bot.charlies.androiddesign.preview.feature.preview.sandbox

import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import bot.charlies.androiddesign.preview.domain.findings.ScrolledTree
import bot.charlies.androiddesign.preview.kit.BoardSurface
import bot.charlies.androiddesign.preview.kit.ExplorationBoard
import bot.charlies.androiddesign.preview.model.DeviceBoardMeta
import bot.charlies.androiddesign.preview.model.DeviceDirectionMeta
import bot.charlies.androiddesign.preview.model.DeviceOverlayMeta
import bot.charlies.androiddesign.preview.model.LayoutTree
import java.util.Collections

/**
 * Shared Compose board rendering engine used by both `:sandbox` and the Robolectric runner. Owns
 * target preparation, Compose settling, frame + slot-tree capture, and Wear list auto-scroll on top
 * of a pluggable [BoardRenderHost].
 */
@OptIn(ComposeToolingApi::class)
internal class ComposeBoardRenderer(private val host: BoardRenderHost) {

  suspend fun render(request: RenderTargetRequest): BoardRenderResult {
    val startNs = System.nanoTime()
    isDebugInspectorInfoEnabled = true
    val prepared = PreparedRenderTarget.from(request)
    val inspectionTables = Collections.synchronizedSet(LinkedHashSet<CompositionData>())
    var rootCoords: LayoutCoordinates? = null
    var emptyDetailScreen: String? = null

    return host.withMountedTarget(
      target = prepared,
      inspectionTables = inspectionTables,
      onRootCoordinates = { rootCoords = it },
      onEmptyDetail = { emptyDetailScreen = it },
    ) { session ->
      SettlePolicy.settle(session.driver, SettlePolicy.MAX_SETTLE_FRAMES)
      val initialCapture = session.captureFrame()
      var finalTree =
        extractTree(
          session = session,
          target = prepared,
          inspectionTables = inspectionTables,
          rootCoords = rootCoords,
          captured = initialCapture,
          emptyDetailScreen = emptyDetailScreen,
        )
      var endTree: LayoutTree? = null
      var endFrame: RawRgbaFrame? = null

      if (shouldAutoScrollWear(session, prepared, inspectionTables, finalTree)) {
        val positions = mutableListOf(finalTree)
        val scrolled =
          SettlePolicy.scrollThrough(session.driver) {
            val stepCapture = session.captureFrame()
            val stepTree =
              extractTree(
                session = session,
                target = prepared,
                inspectionTables = inspectionTables,
                rootCoords = rootCoords,
                captured = stepCapture,
                emptyDetailScreen = emptyDetailScreen,
              )
            stepCapture.frame to stepTree
          }
        for ((stepFrame, stepTree) in scrolled) {
          positions += stepTree
          endFrame = stepFrame
        }
        if (scrolled.isNotEmpty() && positions.size > 1) {
          finalTree = ScrolledTree.merge(positions)
          endTree = positions.last()
        }
      }

      val elapsedMs = (System.nanoTime() - startNs) / NANOS_PER_MS
      BoardRenderResult(
        target = prepared,
        tree = finalTree,
        endTree = endTree,
        frame = initialCapture.frame,
        endFrame = endFrame,
        renderMs = elapsedMs,
      )
    }
  }

  private suspend fun shouldAutoScrollWear(
    session: MountedRenderSession,
    target: PreparedRenderTarget,
    inspectionTables: Set<CompositionData>,
    initialTree: LayoutTree,
  ): Boolean {
    if (target.request.board.surface != BoardSurface.Wear || target.request.overlayId != null) {
      return false
    }
    return session.onUiThread {
      if (WearScrollHelper.hasWearPicker(inspectionTables, initialTree)) {
        return@onUiThread false
      }
      val composeRoots =
        OverlayWindowHelper.collectComposeRoots(session.currentWindows().activeWindows)
      val unmergedRoots = composeRoots.map { it.semanticsOwner.unmergedRootSemanticsNode }
      WearScrollHelper.findVerticalScroller(unmergedRoots, target.minScrollerWidthPx) != null
    }
  }

  private suspend fun extractTree(
    session: MountedRenderSession,
    target: PreparedRenderTarget,
    inspectionTables: Set<CompositionData>,
    rootCoords: LayoutCoordinates?,
    captured: CapturedFrame,
    emptyDetailScreen: String?,
  ): LayoutTree = session.onUiThread {
    val windows = session.currentWindows()
    val composeRoots = OverlayWindowHelper.collectComposeRoots(windows.activeWindows)
    val mergedRoots = composeRoots.map { it.semanticsOwner.rootSemanticsNode }
    val unmergedRoots = composeRoots.map { it.semanticsOwner.unmergedRootSemanticsNode }
    val layoutWindowOffsets =
      OverlayWindowHelper.buildLayoutWindowOffsets(
        mainDecorView = windows.mainDecorView,
        composeRoots = composeRoots,
        tables = inspectionTables,
        explicitWindowOffsets = captured.explicitWindowOffsets,
      )
    AndroidSlotTreeExtractor.extract(
        tables = inspectionTables,
        mergedRoots = mergedRoots,
        unmergedRoots = unmergedRoots,
        rootCoordinates = rootCoords,
        frame = captured.frame,
        screen = target.request.screenId,
        view = target.effectiveView,
        surface = target.request.board.surface,
        explorationFile = target.request.sourceFile,
        sourceText = target.request.sourceText,
        density = target.request.density,
        perimeterPx = target.perimeterPx,
        emptyDetailLists = listOfNotNull(emptyDetailScreen),
        layoutWindowOffsets = layoutWindowOffsets,
      )
      .tree
  }

  companion object {
    private const val NANOS_PER_MS = 1_000_000.0

    fun buildBoardMeta(board: ExplorationBoard): DeviceBoardMeta =
      DeviceBoardMeta(
        surface = board.surfaceSpec.key,
        boardName = board.name,
        directions =
          board.directions.map { d ->
            DeviceDirectionMeta(
              name = d.name,
              surface = board.surfaceSpec.key,
              startScreen = d.startScreen,
              screens = d.screenIds,
              overlays = d.overlays.map { o -> DeviceOverlayMeta(o.id, o.screen) },
            )
          },
        views = board.surfaceSpec.views,
      )
  }
}
