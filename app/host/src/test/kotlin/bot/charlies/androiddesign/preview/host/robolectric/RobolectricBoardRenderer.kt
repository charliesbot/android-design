package bot.charlies.androiddesign.preview.host.robolectric

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Rect
import android.os.Looper
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inspector.WindowInspector
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.LayoutInfo
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.node.RootForTest
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.compositionContext
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.Density
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import bot.charlies.androiddesign.preview.data.DesignMdLocator
import bot.charlies.androiddesign.preview.data.DesignMdParse
import bot.charlies.androiddesign.preview.data.DesignMdParser
import bot.charlies.androiddesign.preview.domain.findings.ScrolledTree
import bot.charlies.androiddesign.preview.domain.image.FastPngEncoder
import bot.charlies.androiddesign.preview.domain.sandbox.SandboxClassLoader
import bot.charlies.androiddesign.preview.feature.preview.sandbox.AndroidSlotTreeExtractor
import bot.charlies.androiddesign.preview.feature.preview.sandbox.RawRgbaFrame
import bot.charlies.androiddesign.preview.feature.preview.sandbox.WearScrollHelper
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalFonts
import bot.charlies.androiddesign.preview.host.compiler.HostClassCompiler
import bot.charlies.androiddesign.preview.host.compiler.HostCompileOutcome
import bot.charlies.androiddesign.preview.host.sandbox.InspectedSubcomposition
import bot.charlies.androiddesign.preview.kit.BoardSurface
import bot.charlies.androiddesign.preview.kit.DirectionSpec
import bot.charlies.androiddesign.preview.kit.ExplorationBoard
import bot.charlies.androiddesign.preview.kit.ExploreBoardFrame
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.RobolectricRenderRequest
import bot.charlies.androiddesign.preview.model.RobolectricRenderResponse
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.ValidationError
import bot.charlies.androiddesign.preview.model.ViewSpec
import java.lang.reflect.Modifier as JavaModifier
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Collections
import java.util.IdentityHashMap
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf

private class RobolectricInsetConsumingLayout(context: Context) : FrameLayout(context) {
  override fun dispatchApplyWindowInsets(insets: WindowInsets): WindowInsets = WindowInsets.CONSUMED

  override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets = WindowInsets.CONSUMED
}

internal data class RobolectricRenderOutcome(
  val tree: LayoutTree,
  val endTree: LayoutTree? = null,
  val frame: RawRgbaFrame,
  val endFrame: RawRgbaFrame? = null,
  val writtenFiles: List<String> = emptyList(),
)

/**
 * Headless Compose + Native Graphics renderer executing inside a Robolectric SDK 35 sandbox. Shares
 * [AndroidSlotTreeExtractor], [LutPixelContrastSampler], [ScrolledTree], [FindingRules], and the
 * `DesignBoard` kit directly with `:host`.
 */
internal object RobolectricBoardRenderer {
  private const val NANOS_PER_MS = 1_000_000L
  private const val BASE_DPI = 160f
  private const val FRAME_NS = 16_000_000L
  private const val MAX_SCROLL_STEPS = 30

  fun handleRequest(request: RobolectricRenderRequest): RobolectricRenderResponse {
    val srcPath = Paths.get(request.sourcePath).toAbsolutePath().normalize()
    if (!Files.isRegularFile(srcPath)) {
      return RobolectricRenderResponse(
        id = request.id,
        ok = true,
        valid = false,
        pass = false,
        errors = listOf(ValidationError(request.sourcePath, "File not found: $srcPath")),
      )
    }

    val designOverride =
      resolveDesignOverride(
        request.designPath?.let { Paths.get(it) } ?: DesignMdLocator.find(srcPath),
      )
    when (designOverride) {
      is DesignOutcome.Invalid ->
        return RobolectricRenderResponse(
          id = request.id,
          ok = true,
          valid = false,
          pass = false,
          errors = listOf(designOverride.error),
        )
      is DesignOutcome.Ok -> Unit
    }

    val sourceFile = srcPath.fileName.toString()
    val sourceText = Files.readString(srcPath)
    val compileResult = HostClassCompiler.compileToClasses(sourceFile, sourceText)
    if (compileResult is HostCompileOutcome.Failed) {
      return RobolectricRenderResponse(
        id = request.id,
        ok = true,
        valid = false,
        pass = false,
        compileMs = compileResult.compileMs,
        errors = compileResult.errors,
      )
    }
    val classes = (compileResult as HostCompileOutcome.Classes)
    val board = loadBoard(sourceFile, classes)

    val outDir = request.outDir?.let { Paths.get(it).toAbsolutePath().normalize() }
    if (outDir != null) {
      Files.createDirectories(outDir)
    }

    val renderStartNs = System.nanoTime()
    val outcomes =
      renderBoardTargets(
        board = board,
        sourceFile = sourceFile,
        sourceText = sourceText,
        explorationDir = srcPath.parent,
        designMd = designOverride.theme,
        directionFilter = request.direction,
        screenFilter = request.screens?.toSet(),
        viewFilter = request.views?.toSet(),
        overlayId = request.overlay,
        density = request.density,
        outDir = outDir,
        encodePng = request.encodePng && outDir != null,
      )
    val renderMs = (System.nanoTime() - renderStartNs) / NANOS_PER_MS

    val trees = outcomes.map { it.tree }
    val endTrees = outcomes.mapNotNull { it.endTree }
    val writtenFiles = outcomes.flatMap { it.writtenFiles }
    val allPass = trees.all { it.pass } && endTrees.all { it.pass }
    return RobolectricRenderResponse(
      id = request.id,
      ok = true,
      valid = true,
      pass = allPass,
      compileMs = classes.compileMs,
      renderMs = renderMs,
      surface = board.surfaceSpec.key,
      boardName = board.name,
      trees = trees,
      endTrees = endTrees,
      writtenFiles = writtenFiles,
    )
  }

  fun loadBoard(sourceFile: String, compiled: HostCompileOutcome.Classes): ExplorationBoard {
    val loader =
      SandboxClassLoader(compiled.classes, RobolectricBoardRenderer::class.java.classLoader!!)
    val candidates = mutableListOf<java.lang.reflect.Method>()
    for (clsName in compiled.classes.keys) {
      val cls = loader.loadClass(clsName)
      for (m in cls.declaredMethods) {
        if (
          JavaModifier.isStatic(m.modifiers) &&
            m.parameterCount == 0 &&
            ExplorationBoard::class.java.isAssignableFrom(m.returnType)
        ) {
          candidates += m
        }
      }
    }
    val getter =
      candidates.firstOrNull { it.name == "getPreview" }
        ?: candidates.firstOrNull()
        ?: error("No top-level `val preview = designBoard(...)` found in $sourceFile")
    return getter.invoke(null) as ExplorationBoard
  }

  fun renderBoardTargets(
    board: ExplorationBoard,
    sourceFile: String,
    sourceText: String,
    explorationDir: Path? = null,
    designMd: ThemeOverride? = null,
    directionFilter: String? = null,
    screenFilter: Set<String>? = null,
    viewFilter: Set<String>? = null,
    overlayId: String? = null,
    density: Float = 2f,
    outDir: Path? = null,
    encodePng: Boolean = false,
  ): List<RobolectricRenderOutcome> {
    val directions =
      if (directionFilter == null) board.directions
      else {
        listOf(
          board.directions.firstOrNull { it.name == directionFilter }
            ?: error("Unknown direction '$directionFilter'"),
        )
      }
    val allViews = board.surfaceSpec.views
    val selectedViews =
      if (viewFilter == null) allViews
      else
        allViews
          .filter { it.name in viewFilter }
          .also {
            require(it.isNotEmpty()) { "No matching views for filter: $viewFilter" }
          }

    val outcomes = mutableListOf<RobolectricRenderOutcome>()
    for (dir in directions) {
      val screens =
        if (screenFilter == null) dir.screenIds else dir.screenIds.filter { it in screenFilter }
      for (screenId in screens) {
        if (overlayId != null) {
          val matching = dir.overlaysFor(screenId).firstOrNull { it.id == overlayId } ?: continue
          for (view in selectedViews) {
            outcomes +=
              renderSingleView(
                board = board,
                direction = dir,
                screenId = screenId,
                view = view,
                sourceFile = sourceFile,
                sourceText = sourceText,
                explorationDir = explorationDir,
                designMd = designMd,
                overlayId = matching.id,
                density = density,
                outDir = outDir,
                encodePng = encodePng,
              )
          }
        } else {
          for (view in selectedViews) {
            outcomes +=
              renderSingleView(
                board = board,
                direction = dir,
                screenId = screenId,
                view = view,
                sourceFile = sourceFile,
                sourceText = sourceText,
                explorationDir = explorationDir,
                designMd = designMd,
                overlayId = null,
                density = density,
                outDir = outDir,
                encodePng = encodePng,
              )
          }
        }
      }
    }
    return outcomes
  }

  @OptIn(ComposeToolingApi::class)
  fun renderSingleView(
    board: ExplorationBoard,
    direction: DirectionSpec,
    screenId: String,
    view: ViewSpec,
    sourceFile: String,
    sourceText: String,
    explorationDir: Path? = null,
    designMd: ThemeOverride? = null,
    overlayId: String? = null,
    density: Float = 2f,
    outDir: Path? = null,
    encodePng: Boolean = false,
  ): RobolectricRenderOutcome {
    val controller = Robolectric.buildActivity(ComponentActivity::class.java)
    val activity = controller.get()
    activity.setTheme(android.R.style.Theme_Material_NoActionBar)
    WindowCompat.setDecorFitsSystemWindows(activity.window, false)
    controller.create().start().resume().visible()
    ProposalFonts.init(activity.assets)

    val widthPx = (view.width * density).roundToInt().coerceAtLeast(1)
    val heightPx = (view.height * density).roundToInt().coerceAtLeast(1)
    val dpi = (BASE_DPI * density).roundToInt().coerceAtLeast(120)
    val perimeterPx = (1f * density).roundToInt().coerceAtLeast(1)

    val cfg =
      Configuration(activity.resources.configuration).apply {
        densityDpi = dpi
        screenWidthDp = view.width
        screenHeightDp = view.height
        smallestScreenWidthDp = minOf(view.width, view.height)
        uiMode =
          (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
            if (view.dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
        screenLayout =
          (screenLayout and Configuration.SCREENLAYOUT_ROUND_MASK.inv()) or
            if (view.round) Configuration.SCREENLAYOUT_ROUND_YES
            else Configuration.SCREENLAYOUT_ROUND_NO
      }
    val overrideCtx =
      object : ContextThemeWrapper(activity, activity.theme) {
        init {
          applyOverrideConfiguration(cfg)
        }
      }

    val clock = BroadcastFrameClock()
    val recomposer = Recomposer(AndroidUiDispatcher.Main + clock)
    val tables = Collections.synchronizedSet(LinkedHashSet<CompositionData>())
    var rootCoords: LayoutCoordinates? = null
    var emptyDetailScreen: String? = null
    val designSystem = board.resolveDesignSystem(designMd = designMd)

    val insetContainer = RobolectricInsetConsumingLayout(overrideCtx)
    val cv = ComposeView(overrideCtx)
    ViewCompat.setOnApplyWindowInsetsListener(activity.window.decorView) { _, _ ->
      WindowInsetsCompat.CONSUMED
    }
    ViewCompat.setOnApplyWindowInsetsListener(insetContainer) { _, _ ->
      WindowInsetsCompat.CONSUMED
    }
    ViewCompat.setOnApplyWindowInsetsListener(cv) { _, _ -> WindowInsetsCompat.CONSUMED }

    insetContainer.addView(cv, ViewGroup.LayoutParams(widthPx, heightPx))
    activity.window.decorView.compositionContext = recomposer
    cv.setParentCompositionContext(recomposer)
    activity.setContentView(insetContainer, ViewGroup.LayoutParams(widthPx, heightPx))

    val job =
      CoroutineScope(AndroidUiDispatcher.Main + clock).launch {
        recomposer.runRecomposeAndApplyChanges()
      }

    cv.setContent {
      CompositionLocalProvider(
        LocalContext provides overrideCtx,
        LocalConfiguration provides cfg,
        LocalDensity provides Density(density, fontScale = 1f),
      ) {
        Box(Modifier.onGloballyPositioned { rootCoords = it }) {
          InspectedSubcomposition(tables) {
            ExploreBoardFrame(
              board = board,
              direction = direction,
              screenId = screenId,
              view = view,
              designSystem = designSystem,
              explorationDir = explorationDir,
              overlayId = overlayId,
              onEmptyDetail = { emptyDetailScreen = it },
            )
          }
        }
      }
    }

    shadowOf(Looper.getMainLooper()).idle()
    var virtualTimeNs = 0L
    fun settleFrames(minSteps: Int) {
      val wSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
      val hSpec = View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY)
      for (step in 0 until 80) {
        Snapshot.sendApplyNotifications()
        virtualTimeNs += FRAME_NS
        clock.sendFrame(virtualTimeNs)
        Snapshot.sendApplyNotifications()
        shadowOf(Looper.getMainLooper()).idle()
        activity.window.decorView.measure(wSpec, hSpec)
        activity.window.decorView.layout(0, 0, widthPx, heightPx)
        insetContainer.measure(wSpec, hSpec)
        insetContainer.layout(0, 0, widthPx, heightPx)
        cv.measure(wSpec, hSpec)
        cv.layout(0, 0, widthPx, heightPx)
        if (step >= minSteps && !clock.hasAwaiters && !recomposer.hasPendingWork) break
      }
      shadowOf(Looper.getMainLooper()).idle()
    }

    fun captureSnapshot(): Pair<LayoutTree, RawRgbaFrame> {
      val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
      val rgba = ByteArray(widthPx * heightPx * 4)
      val explicitWindowOffsets = IdentityHashMap<View, Offset>()
      val composeRoots = mutableListOf<RootForTest>()
      val mainDecor = activity.window.decorView
      val mainLoc = IntArray(2)
      mainDecor.getLocationOnScreen(mainLoc)

      try {
        val canvas = Canvas(bitmap)
        canvas.drawColor(AndroidColor.BLACK)

        fun collectRoots(v: View) {
          if (v is RootForTest) composeRoots += v
          if (v is ViewGroup) {
            for (i in 0 until v.childCount) collectRoots(v.getChildAt(i))
          }
        }

        val windows = WindowInspector.getGlobalWindowViews().filter { it.isAttachedToWindow }
        for (win in windows) {
          if (win !== mainDecor) {
            val lp = win.layoutParams as? WindowManager.LayoutParams
            val (offset, measuredSize) = measureAndLayoutOverlay(win, mainLoc, widthPx, heightPx)
            explicitWindowOffsets[win] = offset
            val dimAlpha =
              if ((lp?.flags ?: 0) and WindowManager.LayoutParams.FLAG_DIM_BEHIND != 0) {
                lp?.dimAmount ?: 0.32f
              } else {
                0f
              }
            if (dimAlpha > 0f) {
              canvas.drawColor(AndroidColor.argb((dimAlpha * 255).toInt(), 0, 0, 0))
            }
            canvas.save()
            canvas.translate(offset.x, offset.y)
            if (
              win.width == 0 && measuredSize.first > 0 && win is ViewGroup && win.childCount > 0
            ) {
              win.getChildAt(0).draw(canvas)
            } else {
              win.draw(canvas)
            }
            canvas.restore()
          } else {
            win.draw(canvas)
          }
          collectRoots(win)
        }

        // Unpack via getPixels so RGBA byte order matches ImageReader RGBA_8888 across host Skia.
        val ints = IntArray(widthPx * heightPx)
        bitmap.getPixels(ints, 0, widthPx, 0, 0, widthPx, heightPx)
        var d = 0
        for (px in ints) {
          rgba[d++] = ((px ushr 16) and 0xFF).toByte()
          rgba[d++] = ((px ushr 8) and 0xFF).toByte()
          rgba[d++] = (px and 0xFF).toByte()
          rgba[d++] = ((px ushr 24) and 0xFF).toByte()
        }
      } finally {
        bitmap.recycle()
      }

      val frame = RawRgbaFrame(widthPx, heightPx, rgba)
      val mergedRoots = composeRoots.map { it.semanticsOwner.rootSemanticsNode }
      val unmergedRoots = composeRoots.map { it.semanticsOwner.unmergedRootSemanticsNode }

      val rootLayoutOffsets = IdentityHashMap<LayoutInfo, Offset>()
      for (r in composeRoots) {
        val rv = r as? View ?: continue
        var winRoot: View = rv
        while (winRoot.parent is View) {
          winRoot = winRoot.parent as View
        }
        if (winRoot !== mainDecor) {
          val offset = explicitWindowOffsets[winRoot] ?: Offset.Zero
          rootLayoutOffsets[r.semanticsOwner.unmergedRootSemanticsNode.layoutInfo] = offset
          rootLayoutOffsets[r.semanticsOwner.rootSemanticsNode.layoutInfo] = offset
        }
      }
      val layoutWindowOffsets = IdentityHashMap<LayoutInfo, Offset>()
      fun resolveOffset(li: LayoutInfo): Offset? {
        var cur: LayoutInfo? = li
        while (cur != null) {
          rootLayoutOffsets[cur]?.let {
            return it
          }
          cur = cur.parentInfo
        }
        return null
      }
      fun indexSemanticsOffsets(node: SemanticsNode) {
        resolveOffset(node.layoutInfo)?.let { layoutWindowOffsets[node.layoutInfo] = it }
        node.children.forEach(::indexSemanticsOffsets)
      }
      for (r in composeRoots) {
        indexSemanticsOffsets(r.semanticsOwner.unmergedRootSemanticsNode)
        indexSemanticsOffsets(r.semanticsOwner.rootSemanticsNode)
      }
      fun indexGroupOffsets(g: androidx.compose.runtime.tooling.CompositionGroup) {
        (g.node as? LayoutInfo)?.let { li ->
          resolveOffset(li)?.let { layoutWindowOffsets[li] = it }
        }
        g.compositionGroups.forEach(::indexGroupOffsets)
      }
      for (table in tables) {
        table.compositionGroups.forEach(::indexGroupOffsets)
      }

      val extracted =
        AndroidSlotTreeExtractor.extract(
          tables = tables,
          mergedRoots = mergedRoots,
          unmergedRoots = unmergedRoots,
          rootCoordinates = rootCoords,
          frame = frame,
          screen = screenId,
          view = view,
          surface = board.surface,
          explorationFile = sourceFile,
          sourceText = sourceText,
          density = density,
          perimeterPx = perimeterPx,
          emptyDetailLists = listOfNotNull(emptyDetailScreen),
          layoutWindowOffsets = layoutWindowOffsets,
        )
      return extracted.tree to frame
    }

    try {
      settleFrames(if (overlayId != null) 50 else 4)
      val (initialTree, initialFrame) = captureSnapshot()
      var finalTree = initialTree
      var endTree: LayoutTree? = null
      var endFrame: RawRgbaFrame? = null

      if (
        board.surface == BoardSurface.Wear &&
          overlayId == null &&
          !WearScrollHelper.hasWearPicker(tables, finalTree)
      ) {
        fun collectUnmergedRoots(): List<SemanticsNode> {
          val roots = mutableListOf<SemanticsNode>()
          fun walk(v: View) {
            if (v is RootForTest) roots += v.semanticsOwner.unmergedRootSemanticsNode
            if (v is ViewGroup) {
              for (i in 0 until v.childCount) walk(v.getChildAt(i))
            }
          }
          for (win in WindowInspector.getGlobalWindowViews().filter { it.isAttachedToWindow }) {
            walk(win)
          }
          return roots
        }

        val minScrollerWidthPx = widthPx * 0.6f
        val positions = mutableListOf(finalTree)
        val stepPx = view.height * density / 2f
        var steps = 0
        while (steps++ < MAX_SCROLL_STEPS) {
          val scroller =
            WearScrollHelper.findVerticalScroller(collectUnmergedRoots(), minScrollerWidthPx)
              ?: break
          val range =
            scroller.config.getOrNull(SemanticsProperties.VerticalScrollAxisRange) ?: break
          if (range.value() >= range.maxValue() - 0.5f) break
          val action = scroller.config.getOrNull(SemanticsActions.ScrollBy)?.action ?: break
          if (!action(0f, stepPx)) break
          settleFrames(4)
          val (stepTree, stepFrame) = captureSnapshot()
          positions += stepTree
          endFrame = stepFrame
        }
        if (positions.size > 1) {
          finalTree = ScrolledTree.merge(positions)
          endTree = positions.last()
        }
      }

      val writtenFiles = mutableListOf<String>()
      if (encodePng && outDir != null) {
        val stem =
          listOfNotNull(
              sanitizeToken(direction.name),
              sanitizeToken(screenId),
              view.name,
              overlayId?.let(::sanitizeToken),
            )
            .joinToString("-")
        val mainFile = outDir.resolve("$stem.png")
        Files.write(
          mainFile,
          FastPngEncoder.encodeFast(initialFrame.width, initialFrame.height, initialFrame.bytes),
        )
        writtenFiles += mainFile.toString()
        if (endFrame != null) {
          val endFile = outDir.resolve("$stem-end.png")
          Files.write(
            endFile,
            FastPngEncoder.encodeFast(endFrame.width, endFrame.height, endFrame.bytes),
          )
          writtenFiles += endFile.toString()
        }
      }

      return RobolectricRenderOutcome(
        tree = finalTree,
        endTree = endTree,
        frame = initialFrame,
        endFrame = endFrame,
        writtenFiles = writtenFiles,
      )
    } finally {
      recomposer.cancel()
      job.cancel()
      controller.pause().stop().destroy()
      shadowOf(Looper.getMainLooper()).idle()
    }
  }

  private sealed interface DesignOutcome {
    data class Ok(val theme: ThemeOverride?) : DesignOutcome

    data class Invalid(val error: ValidationError) : DesignOutcome
  }

  private fun resolveDesignOverride(designPath: Path?): DesignOutcome {
    if (designPath == null || !Files.isRegularFile(designPath)) return DesignOutcome.Ok(null)
    return when (val parsed = DesignMdParser.parse(Files.readString(designPath))) {
      is DesignMdParse.Parsed -> DesignOutcome.Ok(parsed.theme)
      is DesignMdParse.Failed ->
        DesignOutcome.Invalid(ValidationError(designPath.toString(), parsed.message))
    }
  }

  private fun sanitizeToken(raw: String): String =
    raw.trim().lowercase().replace(Regex("[^a-z0-9._-]+"), "_")

  private fun forceLayoutTree(v: View) {
    v.forceLayout()
    if (v is ViewGroup) {
      for (i in 0 until v.childCount) {
        forceLayoutTree(v.getChildAt(i))
      }
    }
  }

  private fun measureAndLayoutOverlay(
    win: View,
    mainLoc: IntArray,
    widthPx: Int,
    heightPx: Int,
  ): Pair<Offset, Pair<Int, Int>> {
    val lp = win.layoutParams as? WindowManager.LayoutParams
    fun specFor(lpDim: Int, maxSize: Int): Int =
      when {
        lpDim == ViewGroup.LayoutParams.MATCH_PARENT ->
          View.MeasureSpec.makeMeasureSpec(maxSize, View.MeasureSpec.EXACTLY)
        lpDim == ViewGroup.LayoutParams.WRAP_CONTENT ->
          View.MeasureSpec.makeMeasureSpec(maxSize, View.MeasureSpec.AT_MOST)
        lpDim > 0 ->
          View.MeasureSpec.makeMeasureSpec(lpDim.coerceAtMost(maxSize), View.MeasureSpec.EXACTLY)
        else -> View.MeasureSpec.makeMeasureSpec(maxSize, View.MeasureSpec.AT_MOST)
      }
    forceLayoutTree(win)
    val wSpec = specFor(lp?.width ?: ViewGroup.LayoutParams.WRAP_CONTENT, widthPx)
    val hSpec = specFor(lp?.height ?: ViewGroup.LayoutParams.WRAP_CONTENT, heightPx)
    win.measure(wSpec, hSpec)
    var w = win.measuredWidth.coerceIn(0, widthPx)
    var h = win.measuredHeight.coerceIn(0, heightPx)
    if ((w == 0 || h == 0) && win is ViewGroup && win.childCount > 0) {
      val child = win.getChildAt(0)
      forceLayoutTree(child)
      child.measure(
        View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.AT_MOST),
        View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.AT_MOST),
      )
      val cw = child.measuredWidth.coerceIn(0, widthPx)
      val ch = child.measuredHeight.coerceIn(0, heightPx)
      if (cw > 0 && ch > 0) {
        w = cw
        h = ch
        win.layout(0, 0, w, h)
        child.layout(0, 0, w, h)
      } else {
        win.layout(0, 0, w, h)
      }
    } else {
      win.layout(0, 0, w, h)
    }

    if (
      lp != null &&
        lp.type in
          WindowManager.LayoutParams.FIRST_SUB_WINDOW..WindowManager.LayoutParams.LAST_SUB_WINDOW
    ) {
      val left = (lp.x - mainLoc[0]).coerceIn(0, (widthPx - w).coerceAtLeast(0))
      val top = (lp.y - mainLoc[1]).coerceIn(0, (heightPx - h).coerceAtLeast(0))
      return Offset(left.toFloat(), top.toFloat()) to (w to h)
    }

    val containerRect = Rect(0, 0, widthPx, heightPx)
    val outRect = Rect()
    Gravity.apply(lp?.gravity ?: Gravity.CENTER, w, h, containerRect, outRect)
    val left = (outRect.left + (lp?.x ?: 0)).coerceIn(0, (widthPx - w).coerceAtLeast(0))
    val top = (outRect.top + (lp?.y ?: 0)).coerceIn(0, (heightPx - h).coerceAtLeast(0))
    return Offset(left.toFloat(), top.toFloat()) to (w to h)
  }
}
