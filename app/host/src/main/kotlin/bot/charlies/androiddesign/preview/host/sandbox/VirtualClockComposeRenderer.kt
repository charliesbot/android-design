package bot.charlies.androiddesign.preview.host.sandbox

import android.content.Context
import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import bot.charlies.androiddesign.preview.feature.preview.sandbox.ComposeBoardRenderer
import bot.charlies.androiddesign.preview.feature.preview.sandbox.RenderTargetRequest
import bot.charlies.androiddesign.preview.kit.ExplorationBoard
import bot.charlies.androiddesign.preview.model.DeviceBatchRequest
import bot.charlies.androiddesign.preview.model.DeviceBatchResponse
import bot.charlies.androiddesign.preview.model.DeviceBoardMeta
import bot.charlies.androiddesign.preview.model.DeviceLiveStateRequest
import bot.charlies.androiddesign.preview.model.DeviceMemoryStats
import bot.charlies.androiddesign.preview.model.DeviceRenderTarget
import bot.charlies.androiddesign.preview.model.DeviceTargetResult
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.Views
import kotlinx.coroutines.runBlocking

internal class VirtualClockComposeRenderer(
  serviceContext: Context,
  internal val assetCache: DeviceAssetCache = DeviceAssetCache(),
) {
  private val frameCapture = FrameCapture()
  private val boardRenderer = ComposeBoardRenderer(Display0RenderHost(serviceContext, frameCapture))
  private val mainHandler = Handler(Looper.getMainLooper())

  private data class LoadedDex(val loader: PolicyDexClassLoader, val board: ExplorationBoard)

  private val dexCache = LruCache<String, LoadedDex>(MAX_CACHED_DEX)
  @Volatile private var reloadCount: Int = 0

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
      for (target in request.targets) {
        val (targetResult, targetFrames) = renderSingleTarget(board, request, target, assetResolver)
        results += targetResult
        encodedFrames += targetFrames
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

  private suspend fun renderSingleTarget(
    board: ExplorationBoard,
    request: DeviceBatchRequest,
    target: DeviceRenderTarget,
    assetResolver: ((String) -> ImageBitmap?)?,
  ): Pair<DeviceTargetResult, List<EncodedFramePayload>> {
    val direction =
      board.directions.firstOrNull { it.name == target.direction }
        ?: error("No direction '${target.direction}'")
    val baseView = Views.byName(target.viewName) ?: error("Unknown view '${target.viewName}'")
    val rendered =
      boardRenderer.render(
        RenderTargetRequest(
          board = board,
          direction = direction,
          screenId = target.screen,
          view = baseView,
          sourceFile = request.sourceFile,
          sourceText = request.sourceText,
          designMd = request.designTheme,
          overlayId = target.overlayId,
          density = target.densityScale,
          fontScale = target.fontScale,
          darkOverride = target.darkOverride,
          seedOverride = target.seedOverride,
          variantOverride = target.variantOverride,
          assetResolver = assetResolver,
        ),
      )

    val viewName = rendered.target.effectiveView.name
    val payloads = mutableListOf<EncodedFramePayload>()
    val endFrame = rendered.endFrame
    if (target.frameFormat != FrameFormat.NONE) {
      payloads +=
        frameCapture.encodeFrame(
          target = target,
          viewName = viewName,
          isEndFrame = false,
          frame = rendered.frame,
        )
      if (target.includeEndFrame && endFrame != null) {
        payloads +=
          frameCapture.encodeFrame(
            target = target,
            viewName = viewName,
            isEndFrame = true,
            frame = endFrame,
          )
      }
    }

    return DeviceTargetResult(
      direction = target.direction,
      screen = target.screen,
      viewName = target.viewName,
      overlayId = target.overlayId,
      densityScale = target.densityScale,
      widthPx = rendered.target.widthPx,
      heightPx = rendered.target.heightPx,
      renderMs = rendered.renderMs,
      pass = rendered.tree.pass,
      tree = rendered.tree,
      endTree = rendered.endTree,
      hasFrame = target.frameFormat != FrameFormat.NONE,
      hasEndFrame =
        target.frameFormat != FrameFormat.NONE && target.includeEndFrame && endFrame != null,
      frameFormat = target.frameFormat,
    ) to payloads
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
    ComposeBoardRenderer.buildBoardMeta(board)

  private fun evaluateRecycleReason(reloads: Int, mem: DeviceMemoryStats): String? =
    when {
      reloads >= RECYCLE_RELOAD_LIMIT -> "reload_limit:$reloads"
      mem.nativeHeapAllocatedBytes > RECYCLE_NATIVE_HEAP_BYTES ->
        "native_heap_mb:${mem.nativeHeapAllocatedBytes / (1024 * 1024)}"
      mem.totalPssKb > RECYCLE_TOTAL_PSS_KB -> "total_pss_mb:${mem.totalPssKb / 1024}"
      else -> null
    }

  companion object {
    private const val NANOS_PER_MS = 1_000_000.0
    private const val MAX_CACHED_DEX = 4
    private const val RECYCLE_RELOAD_LIMIT = 200
    private const val RECYCLE_NATIVE_HEAP_BYTES = 256L * 1024L * 1024L
    private const val RECYCLE_TOTAL_PSS_KB = 512 * 1024
  }
}
