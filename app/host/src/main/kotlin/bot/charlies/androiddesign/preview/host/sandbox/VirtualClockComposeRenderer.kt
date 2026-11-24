package bot.charlies.androiddesign.preview.host.sandbox

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.view.ContextThemeWrapper
import android.view.View
import android.view.inspector.WindowInspector
import androidx.compose.runtime.key
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import bot.charlies.androiddesign.preview.domain.findings.ScrolledTree
import bot.charlies.androiddesign.preview.feature.preview.sandbox.AndroidSlotTreeExtractor
import bot.charlies.androiddesign.preview.feature.preview.sandbox.OverlayWindowHelper
import bot.charlies.androiddesign.preview.feature.preview.sandbox.RawRgbaFrame
import bot.charlies.androiddesign.preview.feature.preview.sandbox.WearScrollHelper
import bot.charlies.androiddesign.preview.kit.BoardSurface
import bot.charlies.androiddesign.preview.kit.ExplorationBoard
import bot.charlies.androiddesign.preview.model.DeviceBatchRequest
import bot.charlies.androiddesign.preview.model.DeviceBatchResponse
import bot.charlies.androiddesign.preview.model.DeviceBoardMeta
import bot.charlies.androiddesign.preview.model.DeviceDirectionMeta
import bot.charlies.androiddesign.preview.model.DeviceLiveStateRequest
import bot.charlies.androiddesign.preview.model.DeviceMemoryStats
import bot.charlies.androiddesign.preview.model.DeviceOverlayMeta
import bot.charlies.androiddesign.preview.model.DeviceRenderTarget
import bot.charlies.androiddesign.preview.model.DeviceTargetResult
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import java.util.Collections
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

internal class VirtualClockComposeRenderer(
  private val serviceContext: Context,
  internal val assetCache: DeviceAssetCache = DeviceAssetCache(),
) {
  private val frameCapture = FrameCapture()
  private val mainHandler = Handler(Looper.getMainLooper())

  private data class LoadedDex(val loader: PolicyDexClassLoader, val board: ExplorationBoard)

  private val dexCache = LruCache<String, LoadedDex>(MAX_CACHED_DEX)
  @Volatile private var reloadCount: Int = 0
  private var sessionSequence: Long = 0L

  @Synchronized
  fun preloadDex(dexHash: String, entryClass: String, dexBytes: ByteArray): ExplorationBoard {
    dexCache.get(dexHash)?.let {
      return it.board
    }
    return loadDex(dexHash, entryClass, dexBytes, cacheResult = true).board
  }

  fun cachedDexHashes(): List<String> = dexCache.snapshot().keys.toList()

  fun currentReloadCount(): Int = reloadCount

  fun collectMemoryStats(): DeviceMemoryStats {
    val rt = Runtime.getRuntime()
    val usedJava = rt.totalMemory() - rt.freeMemory()
    val dbgMem = Debug.MemoryInfo()
    Debug.getMemoryInfo(dbgMem)
    return DeviceMemoryStats(
      javaHeapUsedBytes = usedJava,
      javaHeapMaxBytes = rt.maxMemory(),
      nativeHeapAllocatedBytes = Debug.getNativeHeapAllocatedSize(),
      nativeHeapSizeBytes = Debug.getNativeHeapSize(),
      totalPssKb = dbgMem.totalPss,
      nativePssKb = dbgMem.nativePss,
      dalvikPssKb = dbgMem.dalvikPss,
    )
  }

  fun setLive(
    dexBytes: ByteArray?,
    request: DeviceLiveStateRequest,
  ) {
    val loaded = resolveBoard(request.dexHash, request.entryClass, dexBytes, forceNew = false)
    val board = loaded.board
    val direction =
      request.direction?.let { name -> board.directions.firstOrNull { it.name == name } }
        ?: board.directions.first()
    val screenId = request.screen?.takeIf { it in direction.screens } ?: direction.startScreen
    val variant = request.variantOverride?.let { PaletteVariant.fromWire(it) }
    val designSystem =
      board.resolveDesignSystem(
        designMd = request.designTheme,
        seedOverride = request.seedOverride,
        variantOverride = variant,
      )
    val dark = request.darkOverride ?: board.surfaceSpec.views.first().dark
    val resolver = assetCache.resolverFor(request.assetMap)
    val nextSession =
      LiveSession(
        dexHash = request.dexHash,
        board = board,
        direction = direction,
        screenId = screenId,
        overlayId = request.overlayId,
        designSystem = designSystem,
        dark = dark,
        fontScale = request.fontScale,
        assetResolver = resolver,
      )
    mainHandler.post { LivePreviewState.session.value = nextSession }
  }

  @Synchronized
  fun executeBatch(
    dexBytes: ByteArray?,
    entryClass: String,
    request: DeviceBatchRequest,
  ): Pair<DeviceBatchResponse, List<EncodedFramePayload>> {
    val totalStartNs = System.nanoTime()
    val dexStartNs = System.nanoTime()
    val loaded =
      resolveBoard(
        dexHash = request.dexHash,
        entryClass = entryClass,
        dexBytes = dexBytes,
        forceNew = request.forceNewClassLoader,
      )
    val dexLoadMs = (System.nanoTime() - dexStartNs) / NANOS_PER_MS
    val board = loaded.board

    val results = mutableListOf<DeviceTargetResult>()
    val encodedFrames = mutableListOf<EncodedFramePayload>()
    val assetResolver = assetCache.resolverFor(request.assetMap)

    runBlocking {
      try {
        for (target in request.targets) {
          val (targetResult, targetFrames) =
            renderSingleTarget(board, request, target, assetResolver)
          results += targetResult
          encodedFrames += targetFrames
        }
      } catch (t: Throwable) {
        withContext(Dispatchers.Main) { HostRenderActivity.current()?.resetComposeHost() }
        throw t
      }
    }

    val memStats = collectMemoryStats()
    val recycleReason = evaluateRecycleReason(reloadCount, memStats)
    val totalMs = (System.nanoTime() - totalStartNs) / NANOS_PER_MS
    return DeviceBatchResponse(
      ok = true,
      dexLoadMs = dexLoadMs,
      renderTotalMs = totalMs,
      reloadCount = reloadCount,
      recycleRecommended = recycleReason != null,
      recycleReason = recycleReason,
      boardMeta = buildBoardMeta(board),
      results = results,
      memoryAfter = memStats,
    ) to encodedFrames
  }

  private fun resolveBoard(
    dexHash: String,
    entryClass: String,
    dexBytes: ByteArray?,
    forceNew: Boolean,
  ): LoadedDex {
    if (!forceNew) {
      dexCache.get(dexHash)?.let {
        return it
      }
    }
    val bytes =
      dexBytes?.takeIf { it.isNotEmpty() }
        ?: throw IllegalArgumentException("DEX '$dexHash' is not preloaded and no bytes supplied")
    return loadDex(dexHash, entryClass, bytes, cacheResult = true)
  }

  private fun loadDex(
    dexHash: String,
    entryClass: String,
    dexBytes: ByteArray,
    cacheResult: Boolean,
  ): LoadedDex {
    val loader =
      PolicyDexClassLoader(dexBytes, VirtualClockComposeRenderer::class.java.classLoader!!)
    val clazz = loader.loadClass(entryClass)
    val getter =
      clazz.declaredMethods.firstOrNull {
        it.name == "getPreview" &&
          java.lang.reflect.Modifier.isStatic(it.modifiers) &&
          it.parameterCount == 0
      }
        ?: clazz.declaredMethods.firstOrNull {
          it.name == "preview" &&
            java.lang.reflect.Modifier.isStatic(it.modifiers) &&
            it.parameterCount == 0
        }
        ?: clazz.declaredMethods.firstOrNull {
          java.lang.reflect.Modifier.isStatic(it.modifiers) &&
            it.parameterCount == 0 &&
            ExplorationBoard::class.java.isAssignableFrom(it.returnType)
        }
        ?: error("No getPreview() method in $entryClass")
    val board = getter.invoke(null) as ExplorationBoard
    reloadCount++
    val loaded = LoadedDex(loader, board)
    if (cacheResult) {
      dexCache.put(dexHash, loaded)
    }
    return loaded
  }

  internal fun buildBoardMeta(board: ExplorationBoard): DeviceBoardMeta =
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

  private fun evaluateRecycleReason(reloads: Int, mem: DeviceMemoryStats): String? =
    when {
      reloads >= RECYCLE_RELOAD_LIMIT -> "reload_limit:$reloads"
      mem.nativeHeapAllocatedBytes > RECYCLE_NATIVE_HEAP_BYTES ->
        "native_heap_mb:${mem.nativeHeapAllocatedBytes / (1024 * 1024)}"
      mem.totalPssKb > RECYCLE_TOTAL_PSS_KB -> "total_pss_mb:${mem.totalPssKb / 1024}"
      else -> null
    }

  private suspend fun renderSingleTarget(
    board: ExplorationBoard,
    request: DeviceBatchRequest,
    target: DeviceRenderTarget,
    assetResolver: ((String) -> ImageBitmap?)?,
  ): Pair<DeviceTargetResult, List<EncodedFramePayload>> {
    val viewStartNs = System.nanoTime()
    val direction =
      board.directions.firstOrNull { it.name == target.direction }
        ?: error("No direction '${target.direction}'")
    val baseView = Views.byName(target.viewName) ?: error("Unknown view '${target.viewName}'")
    val effectiveView =
      if (target.darkOverride != null) baseView.copy(dark = target.darkOverride!!) else baseView
    val variantOverride = target.variantOverride?.let { PaletteVariant.fromWire(it) }
    val designSystem =
      board.resolveDesignSystem(
        designMd = request.designTheme,
        seedOverride = target.seedOverride,
        variantOverride = variantOverride,
      )
    val density = target.densityScale
    val fontScale = target.fontScale.coerceAtLeast(0.5f)
    val widthPx = (effectiveView.width * density).roundToInt().coerceAtLeast(1)
    val heightPx = (effectiveView.height * density).roundToInt().coerceAtLeast(1)
    val dpi = (BASE_DPI * density).roundToInt().coerceAtLeast(120)
    val perimeterPx = (1f * density).roundToInt().coerceAtLeast(1)

    ensureDisplay0ActivityHost()
    val activity = checkNotNull(HostRenderActivity.current()) { "HostRenderActivity did not start" }
    val inspectionTables = Collections.synchronizedSet(LinkedHashSet<CompositionData>())
    var rootCoords: LayoutCoordinates? = null
    var emptyDetailScreen: String? = null

    val (overrideCtx, overrideCfg) =
      buildOverrideContext(activity, effectiveView, density, dpi, fontScale)
    val session =
      ActiveRenderSession(
        sessionKey =
          "${request.dexHash}:${target.direction}:${target.screen}:${effectiveView.name}:${target.overlayId}:$density:$fontScale:${effectiveView.dark}:${target.seedOverride}:${target.variantOverride}:${sessionSequence++}",
        sourceFile = request.sourceFile,
        sourceText = request.sourceText,
        board = board,
        direction = direction,
        screenId = target.screen,
        view = effectiveView,
        designSystem = designSystem,
        overlayId = target.overlayId,
        renderDensity = density,
        fontScale = fontScale,
        overrideContext = overrideCtx,
        overrideConfig = overrideCfg,
        inspectionTables = inspectionTables,
        assetResolver = assetResolver,
        onRootCoordinates = { rootCoords = it },
        onEmptyDetail = { emptyDetailScreen = it },
      )

    withContext(Dispatchers.Main) {
      activity.ensureComposeViewForTarget(
        overrideCtx,
        "${effectiveView.name}@$density@$fontScale",
        widthPx,
        heightPx,
      )
      activity.activeSession.value = session
    }

    settleVirtualClock(activity, MAX_SETTLE_FRAMES, widthPx, heightPx)
    val (rawFrame, overlayOffsets) = frameCapture.capture(activity, widthPx, heightPx)

    val initialOutput =
      withContext(Dispatchers.Main) {
        extractTreeSnapshot(
          activity = activity,
          inspectionTables = inspectionTables,
          rootCoords = rootCoords,
          rawFrame = rawFrame,
          screenId = target.screen,
          view = effectiveView,
          surface = board.surface,
          sourceFile = request.sourceFile,
          sourceText = request.sourceText,
          density = density,
          perimeterPx = perimeterPx,
          emptyDetailScreen = emptyDetailScreen,
          explicitWindowOffsets = overlayOffsets,
        )
      }
    var finalTree = initialOutput.tree
    var endTree: LayoutTree? = null
    var endRawFrame: RawRgbaFrame? = null

    if (board.surface == BoardSurface.Wear && target.overlayId == null) {
      val minScrollerWidthPx = widthPx * 0.6f
      val scroller =
        withContext(Dispatchers.Main) {
          if (WearScrollHelper.hasWearPicker(inspectionTables, finalTree)) {
            null
          } else {
            WearScrollHelper.findVerticalScroller(
              collectUnmergedRoots(activity),
              minScrollerWidthPx,
            )
          }
        }
      if (scroller != null) {
        val positions = mutableListOf(finalTree)
        val stepPx = effectiveView.height * density / 2f
        var steps = 0
        var moved = false
        while (steps++ < MAX_SCROLL_STEPS) {
          val didScroll =
            withContext(Dispatchers.Main) {
              val curScroller =
                WearScrollHelper.findVerticalScroller(
                  collectUnmergedRoots(activity),
                  minScrollerWidthPx,
                ) ?: return@withContext false
              val range =
                curScroller.config.getOrNull(SemanticsProperties.VerticalScrollAxisRange)
                  ?: return@withContext false
              if (range.value() >= range.maxValue() - 0.5f) return@withContext false
              val action =
                curScroller.config.getOrNull(SemanticsActions.ScrollBy)?.action
                  ?: return@withContext false
              action(0f, stepPx)
            }
          if (!didScroll) break
          moved = true
          settleVirtualClock(activity, SCROLL_SETTLE_FRAMES, widthPx, heightPx)
          val (stepFrame, stepOffsets) = frameCapture.capture(activity, widthPx, heightPx)
          val stepTree =
            withContext(Dispatchers.Main) {
              extractTreeSnapshot(
                  activity = activity,
                  inspectionTables = inspectionTables,
                  rootCoords = rootCoords,
                  rawFrame = stepFrame,
                  screenId = target.screen,
                  view = effectiveView,
                  surface = board.surface,
                  sourceFile = request.sourceFile,
                  sourceText = request.sourceText,
                  density = density,
                  perimeterPx = perimeterPx,
                  emptyDetailScreen = emptyDetailScreen,
                  explicitWindowOffsets = stepOffsets,
                )
                .tree
            }
          positions += stepTree
          endRawFrame = stepFrame
        }
        if (moved && positions.size > 1) {
          finalTree = ScrolledTree.merge(positions)
          endTree = positions.last()
        }
      }
    }

    val payloads = mutableListOf<EncodedFramePayload>()
    if (target.frameFormat != FrameFormat.NONE) {
      payloads +=
        frameCapture.encodeFrame(target, effectiveView.name, isEndFrame = false, frame = rawFrame)
      if (target.includeEndFrame && endRawFrame != null) {
        payloads +=
          frameCapture.encodeFrame(
            target,
            effectiveView.name,
            isEndFrame = true,
            frame = endRawFrame,
          )
      }
    }
    val renderMs = (System.nanoTime() - viewStartNs) / NANOS_PER_MS
    return DeviceTargetResult(
      direction = target.direction,
      screen = target.screen,
      viewName = target.viewName,
      overlayId = target.overlayId,
      densityScale = density,
      widthPx = widthPx,
      heightPx = heightPx,
      renderMs = renderMs,
      pass = finalTree.pass,
      tree = finalTree,
      endTree = endTree,
      hasFrame = target.frameFormat != FrameFormat.NONE,
      hasEndFrame =
        target.frameFormat != FrameFormat.NONE && target.includeEndFrame && endRawFrame != null,
      frameFormat = target.frameFormat,
    ) to payloads
  }

  private fun ensureDisplay0ActivityHost() {
    if (HostRenderActivity.current() != null) return
    HostRenderActivity.resetLatch()
    val intent =
      Intent(serviceContext, HostRenderActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
        putExtra("move_to_back", true)
      }
    serviceContext.startActivity(intent)
    check(HostRenderActivity.readyLatch.await(3, TimeUnit.SECONDS)) {
      "Timed out waiting for HostRenderActivity on Display 0"
    }
  }

  private suspend fun settleVirtualClock(
    activity: HostRenderActivity,
    maxFrames: Int,
    widthPx: Int,
    heightPx: Int,
  ) {
    val clock = activity.virtualClock ?: return
    val recomp = activity.recomposer ?: return
    val mainLoc = IntArray(2)
    var attachYields = 0
    var hadOverlay = false
    var idleStreak = 0
    for (step in 0 until maxFrames) {
      withContext(Dispatchers.Main) {
        Snapshot.sendApplyNotifications()
        activity.virtualTimeNanos += SETTLE_FRAME_NS
        clock.sendFrame(activity.virtualTimeNanos)
        Snapshot.sendApplyNotifications()
        activity.recomposerFailure.get()?.let { throw it }
      }
      var needsAttachWait = false
      val hasMore =
        withContext(Dispatchers.Main) {
          activity.recomposerFailure.get()?.let { throw it }
          measureMainActivityTarget(activity, widthPx, heightPx)
          activity.window.decorView.getLocationOnScreen(mainLoc)
          val overlays = collectOverlayWindows(activity)
          if (overlays.isNotEmpty()) hadOverlay = true
          for (win in overlays) {
            val m =
              OverlayWindowHelper.measureAndLayoutOverlayWindow(win, mainLoc, widthPx, heightPx)
            if (!win.isAttachedToWindow || m.width == 0) {
              needsAttachWait = true
            }
          }
          activity.recomposerFailure.get()?.let { throw it }
          clock.hasAwaiters || recomp.hasPendingWork
        }
      if (step >= 1 && needsAttachWait && attachYields < 6) {
        attachYields++
        delay(16)
      }
      val minSteps = if (hadOverlay) 45 else 4
      if (!hasMore && !needsAttachWait && step >= minSteps) {
        idleStreak++
        if (idleStreak >= 3) break
      } else {
        idleStreak = 0
      }
    }
    withContext(Dispatchers.Main) {
      activity.recomposerFailure.get()?.let { throw it }
      measureMainActivityTarget(activity, widthPx, heightPx)
      activity.window.decorView.getLocationOnScreen(mainLoc)
      for (win in collectOverlayWindows(activity)) {
        OverlayWindowHelper.measureAndLayoutOverlayWindow(win, mainLoc, widthPx, heightPx)
      }
      activity.recomposerFailure.get()?.let { throw it }
    }
  }

  private fun extractTreeSnapshot(
    activity: HostRenderActivity,
    inspectionTables: Set<CompositionData>,
    rootCoords: LayoutCoordinates?,
    rawFrame: RawRgbaFrame,
    screenId: String,
    view: ViewSpec,
    surface: BoardSurface,
    sourceFile: String,
    sourceText: String,
    density: Float,
    perimeterPx: Int,
    emptyDetailScreen: String?,
    explicitWindowOffsets: Map<View, Offset>,
  ): AndroidSlotTreeExtractor.ExtractionOutput {
    val globalViews =
      WindowInspector.getGlobalWindowViews().filter { belongsToOffscreenTree(activity, it) }
    val composeRoots = OverlayWindowHelper.collectComposeRoots(globalViews)
    val mergedRoots = composeRoots.map { it.semanticsOwner.rootSemanticsNode }
    val unmergedRoots = composeRoots.map { it.semanticsOwner.unmergedRootSemanticsNode }
    val layoutWindowOffsets =
      OverlayWindowHelper.buildLayoutWindowOffsets(
        mainDecorView = activity.window.decorView,
        composeRoots = composeRoots,
        tables = inspectionTables,
        explicitWindowOffsets = explicitWindowOffsets,
      )

    return AndroidSlotTreeExtractor.extract(
      tables = inspectionTables,
      mergedRoots = mergedRoots,
      unmergedRoots = unmergedRoots,
      rootCoordinates = rootCoords,
      frame = rawFrame,
      screen = screenId,
      view = view,
      surface = surface,
      explorationFile = sourceFile,
      sourceText = sourceText,
      density = density,
      perimeterPx = perimeterPx,
      emptyDetailLists = listOfNotNull(emptyDetailScreen),
      layoutWindowOffsets = layoutWindowOffsets,
    )
  }

  private fun collectUnmergedRoots(activity: HostRenderActivity): List<SemanticsNode> {
    val globalViews =
      WindowInspector.getGlobalWindowViews().filter { belongsToOffscreenTree(activity, it) }
    return OverlayWindowHelper.collectComposeRoots(globalViews).map {
      it.semanticsOwner.unmergedRootSemanticsNode
    }
  }

  private fun buildOverrideContext(
    base: Context,
    view: ViewSpec,
    density: Float,
    dpi: Int,
    fontScale: Float,
  ): Pair<Context, Configuration> {
    val cfg = Configuration(base.resources.configuration)
    cfg.densityDpi = dpi
    cfg.fontScale = fontScale
    cfg.screenWidthDp = view.width
    cfg.screenHeightDp = view.height
    cfg.smallestScreenWidthDp = minOf(view.width, view.height)
    cfg.uiMode =
      (cfg.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
        if (view.dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
    cfg.screenLayout =
      (cfg.screenLayout and Configuration.SCREENLAYOUT_ROUND_MASK.inv()) or
        if (view.round) Configuration.SCREENLAYOUT_ROUND_YES
        else Configuration.SCREENLAYOUT_ROUND_NO
    val wrapped =
      object : ContextThemeWrapper(base, base.theme) {
        init {
          applyOverrideConfiguration(cfg)
        }
      }
    return wrapped to cfg
  }

  companion object {
    private const val NANOS_PER_MS = 1_000_000.0
    private const val BASE_DPI = 160f
    private const val SETTLE_FRAME_NS = 16_666_667L
    private const val MAX_SETTLE_FRAMES = 120
    private const val SCROLL_SETTLE_FRAMES = 32
    private const val MAX_SCROLL_STEPS = 60
    private const val MAX_CACHED_DEX = 4
    private const val RECYCLE_RELOAD_LIMIT = 200
    private const val RECYCLE_NATIVE_HEAP_BYTES = 256L * 1024L * 1024L
    private const val RECYCLE_TOTAL_PSS_KB = 512 * 1024
  }
}
