package bot.charlies.androiddesign.preview.host.sandbox

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.HardwareRenderer
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RenderNode
import android.hardware.HardwareBuffer
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inspector.WindowInspector
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.runtime.tooling.LocalInspectionTables
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.compositionContext
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.Density
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import bot.charlies.androiddesign.preview.domain.findings.ScrolledTree
import bot.charlies.androiddesign.preview.feature.preview.sandbox.AndroidSlotTreeExtractor
import bot.charlies.androiddesign.preview.feature.preview.sandbox.InsetConsumingFrameLayout
import bot.charlies.androiddesign.preview.feature.preview.sandbox.OverlayWindowHelper
import bot.charlies.androiddesign.preview.feature.preview.sandbox.RawRgbaFrame
import bot.charlies.androiddesign.preview.feature.preview.sandbox.WearScrollHelper
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalFonts
import bot.charlies.androiddesign.preview.host.LivePreviewActivity
import bot.charlies.androiddesign.preview.kit.BoardSurface
import bot.charlies.androiddesign.preview.kit.DirectionSpec
import bot.charlies.androiddesign.preview.kit.ExplorationBoard
import bot.charlies.androiddesign.preview.kit.ExploreBoardFrame
import bot.charlies.androiddesign.preview.model.DesignSystem
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
import java.io.ByteArrayOutputStream
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.util.Collections
import java.util.IdentityHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import net.jpountz.lz4.LZ4Factory

internal data class EncodedFramePayload(
  val direction: String,
  val screen: String,
  val viewName: String,
  val overlayId: String?,
  val isEndFrame: Boolean,
  val format: FrameFormat,
  val widthPx: Int,
  val heightPx: Int,
  val rawLength: Int,
  val payload: ByteArray,
)

internal class ActiveRenderSession(
  val sessionKey: String,
  val sourceFile: String,
  val sourceText: String,
  val board: ExplorationBoard,
  val direction: DirectionSpec,
  val screenId: String,
  val view: ViewSpec,
  val designSystem: DesignSystem,
  val overlayId: String?,
  val renderDensity: Float,
  val fontScale: Float,
  val overrideContext: Context,
  val overrideConfig: Configuration,
  val inspectionTables: MutableSet<CompositionData>,
  val assetResolver: ((String) -> ImageBitmap?)?,
  val onRootCoordinates: (LayoutCoordinates) -> Unit,
  val onEmptyDetail: (String?) -> Unit,
)

class HostRenderActivity : ComponentActivity() {
  internal var composeView: ComposeView? = null
  internal var container: FrameLayout? = null
  internal val activeSession: MutableState<ActiveRenderSession?> = mutableStateOf(null)
  internal var virtualClock: BroadcastFrameClock? = null
  internal var recomposer: Recomposer? = null
  internal var virtualTimeNanos: Long = 0L
  internal val recomposerFailure = AtomicReference<Throwable?>(null)
  private var recomposerJob: Job? = null
  private val alwaysResumedOwners = ServiceViewTreeOwners()
  private var movedToBack = false
  private var activeViewKey: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ProposalFonts.init(assets)
    startForegroundService(Intent(this, RenderForegroundService::class.java))
    WindowCompat.setDecorFitsSystemWindows(window, false)
    val root = InsetConsumingFrameLayout(this)
    setContentView(root)
    alwaysResumedOwners.moveToResumed()
    window.decorView.setPadding(0, 0, 0, 0)
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, _ ->
      WindowInsetsCompat.CONSUMED
    }
    ViewCompat.setOnApplyWindowInsetsListener(root) { _, _ -> WindowInsetsCompat.CONSUMED }
    listOf(window.decorView, root).forEach { v ->
      v.setViewTreeLifecycleOwner(alwaysResumedOwners)
      v.setViewTreeSavedStateRegistryOwner(alwaysResumedOwners)
      v.setViewTreeViewModelStoreOwner(alwaysResumedOwners)
      v.setViewTreeOnBackPressedDispatcherOwner(alwaysResumedOwners)
    }
    container = root
    instanceRef = WeakReference(this)
    val decor = window.decorView
    if (decor.isAttachedToWindow) {
      readyLatch.countDown()
    } else {
      decor.addOnAttachStateChangeListener(
        object : View.OnAttachStateChangeListener {
          override fun onViewAttachedToWindow(v: View) {
            v.removeOnAttachStateChangeListener(this)
            readyLatch.countDown()
          }

          override fun onViewDetachedFromWindow(v: View) = Unit
        },
      )
    }
  }

  override fun onResume() {
    super.onResume()
    window.decorView.post {
      if (!movedToBack && intent?.getBooleanExtra("move_to_back", true) != false) {
        movedToBack = true
        moveTaskToBack(true)
      }
    }
  }

  private fun ensureRecomposerPipeline() {
    if (recomposer != null) return
    recomposerFailure.set(null)
    val clock = BroadcastFrameClock()
    lateinit var recomp: Recomposer
    val handler = CoroutineExceptionHandler { _, t ->
      if (t !is CancellationException) {
        recomposerFailure.compareAndSet(null, t)
        recomp.cancel()
      }
    }
    recomp = Recomposer(AndroidUiDispatcher.Main + clock + handler)
    window.decorView.compositionContext = recomp
    recomposerJob =
      CoroutineScope(AndroidUiDispatcher.Main + clock + handler).launch {
        try {
          recomp.runRecomposeAndApplyChanges()
        } catch (t: Throwable) {
          if (t is CancellationException) throw t
          recomposerFailure.compareAndSet(null, t)
        }
      }
    virtualClock = clock
    recomposer = recomp
  }

  internal fun ensureComposeViewForTarget(
    overrideCtx: Context,
    viewKey: String,
    widthPx: Int,
    heightPx: Int,
  ) {
    ensureRecomposerPipeline()
    val root = container ?: return
    val recomp = recomposer ?: return
    if (composeView == null || activeViewKey != viewKey) {
      activeSession.value = null
      composeView?.disposeComposition()
      root.removeAllViews()
      isDebugInspectorInfoEnabled = true
      val cv = ComposeView(overrideCtx)
      ViewCompat.setOnApplyWindowInsetsListener(cv) { _, _ -> WindowInsetsCompat.CONSUMED }
      cv.setViewTreeLifecycleOwner(alwaysResumedOwners)
      cv.setViewTreeSavedStateRegistryOwner(alwaysResumedOwners)
      cv.setViewTreeViewModelStoreOwner(alwaysResumedOwners)
      cv.setViewTreeOnBackPressedDispatcherOwner(alwaysResumedOwners)
      cv.setParentCompositionContext(recomp)
      root.layoutParams = FrameLayout.LayoutParams(widthPx, heightPx)
      root.addView(cv, FrameLayout.LayoutParams(widthPx, heightPx))
      cv.setContent {
        val session = activeSession.value
        if (session != null) {
          key(session.sessionKey) {
            CompositionLocalProvider(
              LocalContext provides session.overrideContext,
              LocalConfiguration provides session.overrideConfig,
              LocalDensity provides
                Density(session.renderDensity, fontScale = session.fontScale.coerceAtLeast(0.5f)),
              LocalNavigationEventDispatcherOwner provides alwaysResumedOwners,
            ) {
              Box(Modifier.onGloballyPositioned { session.onRootCoordinates(it) }) {
                InspectedSubcomposition(session.inspectionTables) {
                  ExploreBoardFrame(
                    board = session.board,
                    direction = session.direction,
                    screenId = session.screenId,
                    view = session.view,
                    designSystem = session.designSystem,
                    explorationDir = null,
                    overlayId = session.overlayId,
                    assetResolver = session.assetResolver,
                    onEmptyDetail = session.onEmptyDetail,
                  )
                }
              }
            }
          }
        }
      }
      composeView = cv
      activeViewKey = viewKey
    } else {
      root.layoutParams = FrameLayout.LayoutParams(widthPx, heightPx)
      composeView?.layoutParams = FrameLayout.LayoutParams(widthPx, heightPx)
    }
  }

  internal fun resetComposeHost() {
    activeSession.value = null
    recomposerFailure.set(null)
    runCatching { composeView?.disposeComposition() }
    composeView = null
    activeViewKey = null
    container?.removeAllViews()
    recomposer?.cancel()
    recomposerJob?.cancel()
    recomposer = null
    virtualClock = null
  }

  override fun onDestroy() {
    resetComposeHost()
    alwaysResumedOwners.destroy()
    if (instanceRef?.get() === this) {
      instanceRef = null
    }
    super.onDestroy()
  }

  companion object {
    @Volatile private var instanceRef: WeakReference<HostRenderActivity>? = null
    @Volatile var readyLatch = CountDownLatch(1)

    fun current(): HostRenderActivity? = instanceRef?.get()

    fun resetLatch() {
      readyLatch = CountDownLatch(1)
    }
  }
}

@OptIn(ComposeToolingApi::class)
@Composable
internal fun InspectedSubcomposition(
  tables: MutableSet<CompositionData>,
  content: @Composable () -> Unit,
) {
  // Enable source/parameter markers only on the outer composer if root didn't enable them.
  // Calling collectParameterInformation() inside subcompose(Unit) resets an already-populated
  // SlotTable.sourceInformationMap mid-composition and drops child source info.
  if (LocalInspectionTables.current == null) {
    currentComposer.collectParameterInformation()
  }
  CompositionLocalProvider(LocalInspectionTables provides tables) {
    SubcomposeLayout { constraints ->
      val placeables = subcompose(Unit) { content() }.map { it.measure(constraints) }
      val width = placeables.maxOfOrNull { it.width } ?: constraints.minWidth
      val height = placeables.maxOfOrNull { it.height } ?: constraints.minHeight
      layout(width, height) { placeables.forEach { it.placeRelative(0, 0) } }
    }
  }
}

internal class VirtualClockComposeRenderer(
  private val serviceContext: Context,
  internal val assetCache: DeviceAssetCache = DeviceAssetCache(),
) {
  private val lz4Compressor = LZ4Factory.fastestInstance().fastCompressor()
  private val mainHandler = Handler(Looper.getMainLooper())

  private data class LoadedDex(val loader: PolicyDexClassLoader, val board: ExplorationBoard)

  private val dexCache = LruCache<String, LoadedDex>(MAX_CACHED_DEX)
  @Volatile private var reloadCount: Int = 0
  private var sessionSequence: Long = 0L

  private var hardwareRenderer: HardwareRenderer? = null
  private var renderNode: RenderNode? = null
  private var hwImageReader: ImageReader? = null
  private var hwWidth: Int = 0
  private var hwHeight: Int = 0

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
    val (rawFrame, overlayOffsets) =
      captureMultiWindowViaHardwareRenderer(activity, widthPx, heightPx)

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
          val (stepFrame, stepOffsets) =
            captureMultiWindowViaHardwareRenderer(activity, widthPx, heightPx)
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
      payloads += encodeFrame(target, effectiveView.name, isEndFrame = false, frame = rawFrame)
      if (target.includeEndFrame && endRawFrame != null) {
        payloads += encodeFrame(target, effectiveView.name, isEndFrame = true, frame = endRawFrame)
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

  private fun encodeFrame(
    target: DeviceRenderTarget,
    viewName: String,
    isEndFrame: Boolean,
    frame: RawRgbaFrame,
  ): EncodedFramePayload {
    val encodedBytes =
      when (target.frameFormat) {
        FrameFormat.LZ4_RGBA -> compressLz4(frame.bytes)
        FrameFormat.WEBP_LOSSLESS -> compressWebpLossless(frame)
        FrameFormat.NONE -> ByteArray(0)
      }
    return EncodedFramePayload(
      direction = target.direction,
      screen = target.screen,
      viewName = viewName,
      overlayId = target.overlayId,
      isEndFrame = isEndFrame,
      format = target.frameFormat,
      widthPx = frame.width,
      heightPx = frame.height,
      rawLength = frame.bytes.size,
      payload = encodedBytes,
    )
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

  private fun measureMainActivityTarget(activity: HostRenderActivity, widthPx: Int, heightPx: Int) {
    val wSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
    val hSpec = View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY)
    val decor = activity.window.decorView
    if (decor.isLayoutRequested || decor.width != widthPx || decor.height != heightPx) {
      decor.measure(wSpec, hSpec)
      decor.layout(0, 0, widthPx, heightPx)
    }
    activity.container?.let { c ->
      if (c.isLayoutRequested || c.width != widthPx || c.height != heightPx) {
        c.measure(wSpec, hSpec)
        c.layout(0, 0, widthPx, heightPx)
      }
    }
    activity.composeView?.let { cv ->
      if (cv.isLayoutRequested || cv.width != widthPx || cv.height != heightPx) {
        cv.measure(wSpec, hSpec)
        cv.layout(0, 0, widthPx, heightPx)
      }
    }
  }

  private fun collectOverlayWindows(activity: HostRenderActivity): List<View> {
    val mainDecor = activity.window.decorView
    return WindowInspector.getGlobalWindowViews().filter { win ->
      win !== mainDecor && win.visibility == View.VISIBLE && belongsToOffscreenTree(activity, win)
    }
  }

  private fun belongsToOffscreenTree(activity: HostRenderActivity, win: View): Boolean {
    if (win === activity.window.decorView) return true
    if (win === LivePreviewActivity.currentDecorView()) return false
    val lp = win.layoutParams as? WindowManager.LayoutParams
    if (lp?.type == WindowManager.LayoutParams.TYPE_BASE_APPLICATION) return false
    val liveToken = LivePreviewActivity.currentWindowToken()
    if (lp?.token != null && lp.token == liveToken) return false
    val hostToken = activity.window.decorView.windowToken
    if (lp?.token != null && lp.token == hostToken) return true
    return checkSubtreeContext(win) ?: false
  }

  private fun checkSubtreeContext(view: View): Boolean? {
    var cur: Context? = view.context
    while (cur is ContextWrapper) {
      if (cur is HostRenderActivity) return true
      if (cur is LivePreviewActivity) return false
      cur = cur.baseContext
    }
    if (view is ViewGroup) {
      for (i in 0 until view.childCount) {
        checkSubtreeContext(view.getChildAt(i))?.let {
          return it
        }
      }
    }
    return null
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

  private suspend fun captureMultiWindowViaHardwareRenderer(
    activity: HostRenderActivity,
    widthPx: Int,
    heightPx: Int,
  ): Pair<RawRgbaFrame, Map<View, Offset>> {
    if (hardwareRenderer == null) {
      hardwareRenderer = HardwareRenderer()
      renderNode = RenderNode("adp-frame")
    }
    if (hwImageReader == null || hwWidth != widthPx || hwHeight != heightPx) {
      hwImageReader?.close()
      hwImageReader =
        ImageReader.newInstance(
          widthPx,
          heightPx,
          PixelFormat.RGBA_8888,
          2,
          HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE or
            HardwareBuffer.USAGE_GPU_COLOR_OUTPUT or
            HardwareBuffer.USAGE_CPU_READ_OFTEN,
        )
      hwWidth = widthPx
      hwHeight = heightPx
      hardwareRenderer!!.setSurface(hwImageReader!!.surface)
    }
    val rn = renderNode!!
    val hr = hardwareRenderer!!
    val reader = hwImageReader!!

    while (true) {
      val stale = reader.acquireLatestImage() ?: break
      stale.close()
    }

    val overlayOffsets = IdentityHashMap<View, Offset>()
    withContext(Dispatchers.Main) {
      measureMainActivityTarget(activity, widthPx, heightPx)
      val view = checkNotNull(activity.composeView)
      val overlays = collectOverlayWindows(activity)
      val mainLoc = IntArray(2)
      activity.window.decorView.getLocationOnScreen(mainLoc)

      val dimPaint = Paint()
      rn.setPosition(0, 0, widthPx, heightPx)
      val canvas = rn.beginRecording(widthPx, heightPx)
      try {
        view.draw(canvas)
        for (win in overlays) {
          val lp = win.layoutParams as? WindowManager.LayoutParams
          if (
            lp != null &&
              (lp.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND) != 0 &&
              lp.dimAmount > 0f
          ) {
            val alpha = (lp.dimAmount * 255f).roundToInt().coerceIn(0, 255)
            dimPaint.color = Color.argb(alpha, 0, 0, 0)
            canvas.drawRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), dimPaint)
          }
          val m = OverlayWindowHelper.measureAndLayoutOverlayWindow(win, mainLoc, widthPx, heightPx)
          val offset = m.offset
          overlayOffsets[win] = offset
          canvas.save()
          canvas.translate(offset.x, offset.y)
          if (win.width == 0 && m.width > 0 && win is ViewGroup && win.childCount > 0) {
            win.getChildAt(0).draw(canvas)
          } else {
            win.draw(canvas)
          }
          canvas.restore()
        }
      } finally {
        rn.endRecording()
      }
      hr.setContentRoot(rn)
    }

    val syncStatus = hr.createRenderRequest().setWaitForPresent(true).syncAndDraw()
    check(
      syncStatus == HardwareRenderer.SYNC_OK || syncStatus == HardwareRenderer.SYNC_FRAME_DROPPED,
    ) {
      "HardwareRenderer syncAndDraw status=$syncStatus"
    }
    val image = acquireLatestOrWaitImage(reader)
    return copyImageToRgba(image, widthPx, heightPx) to overlayOffsets
  }

  private suspend fun acquireLatestOrWaitImage(
    reader: ImageReader,
    timeoutMs: Long = 1000L,
  ): Image {
    reader.acquireLatestImage()?.let {
      return it
    }
    return suspendCancellableCoroutine { cont ->
      val done = AtomicBoolean(false)
      val timeoutRunnable = Runnable {
        if (done.compareAndSet(false, true)) {
          reader.setOnImageAvailableListener(null, null)
          val fallback = reader.acquireLatestImage()
          if (fallback != null) {
            cont.resume(fallback)
          } else {
            cont.cancel(
              IllegalStateException("Timed out waiting ${timeoutMs}ms for ImageReader frame"),
            )
          }
        }
      }
      mainHandler.postDelayed(timeoutRunnable, timeoutMs)
      reader.setOnImageAvailableListener(
        { r ->
          val img = r.acquireLatestImage()
          if (img != null && done.compareAndSet(false, true)) {
            mainHandler.removeCallbacks(timeoutRunnable)
            r.setOnImageAvailableListener(null, null)
            cont.resume(img)
          } else {
            img?.close()
          }
        },
        mainHandler,
      )
      cont.invokeOnCancellation {
        mainHandler.removeCallbacks(timeoutRunnable)
        reader.setOnImageAvailableListener(null, null)
      }
    }
  }

  private fun copyImageToRgba(image: Image, expectedW: Int, expectedH: Int): RawRgbaFrame {
    try {
      val plane = image.planes[0]
      val buf = plane.buffer
      val rowStride = plane.rowStride
      val pixelStride = plane.pixelStride
      val w = minOf(image.width, expectedW)
      val h = minOf(image.height, expectedH)
      val out = ByteArray(w * h * 4)
      val expectedRowBytes = w * 4
      if (pixelStride == 4 && rowStride == expectedRowBytes) {
        buf.position(0)
        buf.get(out, 0, out.size)
      } else {
        val rowTmp = ByteArray(rowStride)
        for (y in 0 until h) {
          buf.position(y * rowStride)
          val toRead = minOf(rowStride, buf.remaining())
          buf.get(rowTmp, 0, toRead)
          if (pixelStride == 4) {
            System.arraycopy(rowTmp, 0, out, y * expectedRowBytes, expectedRowBytes)
          } else {
            var dst = y * expectedRowBytes
            var src = 0
            for (x in 0 until w) {
              out[dst++] = rowTmp[src]
              out[dst++] = rowTmp[src + 1]
              out[dst++] = rowTmp[src + 2]
              out[dst++] = rowTmp[src + 3]
              src += pixelStride
            }
          }
        }
      }
      return RawRgbaFrame(w, h, out)
    } finally {
      image.close()
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

  private fun compressLz4(raw: ByteArray): ByteArray {
    val maxLen = lz4Compressor.maxCompressedLength(raw.size)
    val dst = ByteArray(maxLen)
    val len = lz4Compressor.compress(raw, 0, raw.size, dst, 0, maxLen)
    return dst.copyOf(len)
  }

  private fun compressWebpLossless(frame: RawRgbaFrame): ByteArray {
    val bmp = Bitmap.createBitmap(frame.width, frame.height, Bitmap.Config.ARGB_8888)
    try {
      bmp.copyPixelsFromBuffer(ByteBuffer.wrap(frame.bytes))
      val out = ByteArrayOutputStream(frame.width * frame.height / 2)
      check(bmp.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 0, out)) {
        "Bitmap.compress(WEBP_LOSSLESS) returned false"
      }
      return out.toByteArray()
    } finally {
      bmp.recycle()
    }
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

private class ServiceViewTreeOwners :
  LifecycleOwner,
  SavedStateRegistryOwner,
  ViewModelStoreOwner,
  OnBackPressedDispatcherOwner,
  NavigationEventDispatcherOwner {
  private val lifecycleRegistry = LifecycleRegistry(this)
  private val savedStateController = SavedStateRegistryController.create(this)
  override val viewModelStore: ViewModelStore = ViewModelStore()
  override val onBackPressedDispatcher: OnBackPressedDispatcher = OnBackPressedDispatcher()
  override val navigationEventDispatcher: NavigationEventDispatcher = NavigationEventDispatcher()

  override val lifecycle: Lifecycle
    get() = lifecycleRegistry

  override val savedStateRegistry: SavedStateRegistry
    get() = savedStateController.savedStateRegistry

  fun moveToResumed() {
    savedStateController.performRestore(null)
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
  }

  fun destroy() {
    if (lifecycleRegistry.currentState != Lifecycle.State.DESTROYED) {
      lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
  }
}
