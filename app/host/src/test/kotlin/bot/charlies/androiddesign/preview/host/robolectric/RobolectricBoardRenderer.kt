package bot.charlies.androiddesign.preview.host.robolectric

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.os.Looper
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inspector.WindowInspector
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.compositionContext
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.Density
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import bot.charlies.androiddesign.preview.data.DesignMdLocator
import bot.charlies.androiddesign.preview.data.DesignMdParse
import bot.charlies.androiddesign.preview.data.DesignMdParser
import bot.charlies.androiddesign.preview.domain.RenderEntry
import bot.charlies.androiddesign.preview.domain.RenderReport
import bot.charlies.androiddesign.preview.domain.ReportTheme
import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.domain.findings.ScrolledTree
import bot.charlies.androiddesign.preview.domain.image.FastPngEncoder
import bot.charlies.androiddesign.preview.domain.sandbox.SandboxClassLoader
import bot.charlies.androiddesign.preview.feature.preview.sandbox.AndroidSlotTreeExtractor
import bot.charlies.androiddesign.preview.feature.preview.sandbox.FrameStatus
import bot.charlies.androiddesign.preview.feature.preview.sandbox.InsetConsumingFrameLayout
import bot.charlies.androiddesign.preview.feature.preview.sandbox.OverlayWindowHelper
import bot.charlies.androiddesign.preview.feature.preview.sandbox.RawRgbaFrame
import bot.charlies.androiddesign.preview.feature.preview.sandbox.SettlePolicy
import bot.charlies.androiddesign.preview.feature.preview.sandbox.WearScrollDriver
import bot.charlies.androiddesign.preview.feature.preview.sandbox.WearScrollHelper
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalFonts
import bot.charlies.androiddesign.preview.host.sandbox.DeviceAssetCache
import bot.charlies.androiddesign.preview.host.sandbox.InspectedSubcomposition
import bot.charlies.androiddesign.preview.kit.BoardSurface
import bot.charlies.androiddesign.preview.kit.DirectionSpec
import bot.charlies.androiddesign.preview.kit.ExplorationBoard
import bot.charlies.androiddesign.preview.kit.ExploreBoardFrame
import bot.charlies.androiddesign.preview.model.DeviceBoardMeta
import bot.charlies.androiddesign.preview.model.DeviceDirectionMeta
import bot.charlies.androiddesign.preview.model.DeviceOverlayMeta
import bot.charlies.androiddesign.preview.model.DeviceRenderTarget
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.RobolectricRenderRequest
import bot.charlies.androiddesign.preview.model.RobolectricRenderResponse
import bot.charlies.androiddesign.preview.model.RobolectricTargetResult
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.ValidationError
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import java.lang.reflect.Modifier as JavaModifier
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.Collections
import java.util.IdentityHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf

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
  init {
    isDebugInspectorInfoEnabled = true
  }

  private const val NANOS_PER_MS = 1_000_000L
  private const val BASE_DPI = 160f

  private const val MAX_BOARD_CACHE = 8

  /** Boards loaded from a staged classes directory; each stage directory is immutable. */
  private val boardCache =
    object : LinkedHashMap<String, ExplorationBoard>(16, 0.75f, true) {
      override fun removeEldestEntry(
        eldest: MutableMap.MutableEntry<String, ExplorationBoard>?,
      ): Boolean = size > MAX_BOARD_CACHE
    }

  private fun writeReportFile(
    outDir: Path,
    sourcePath: Path,
    designPath: Path?,
    designTheme: ThemeOverride?,
    boardTheme: ThemeOverride?,
    board: ExplorationBoard?,
    errors: List<ValidationError>,
    renders: List<RenderEntry>,
  ): RenderReport {
    val system =
      if (board != null) {
        board.resolveDesignSystem(designMd = designTheme)
      } else {
        DesignSystemResolver.resolve(designTheme, boardTheme)
      }
    val valid = errors.isEmpty()
    val report =
      RenderReport(
        proposal = sourcePath.toString(),
        design = designPath?.toString(),
        theme = ReportTheme(system.seed, system.variant.wire, system.contrast),
        valid = valid,
        errors = errors,
        renders = renders,
        pass = valid && renders.all { it.pass },
      )
    Files.createDirectories(outDir)
    Files.writeString(
      outDir.resolve("report.json"),
      PreviewJson.pretty.encodeToString(RenderReport.serializer(), report) + "\n",
    )
    return report
  }

  private fun invalidResponse(
    request: RobolectricRenderRequest,
    srcPath: Path,
    outDir: Path?,
    writeReport: Boolean,
    designPath: Path?,
    designTheme: ThemeOverride?,
    errors: List<ValidationError>,
    compileMs: Long = 0L,
  ): RobolectricRenderResponse {
    if (writeReport && outDir != null) {
      writeReportFile(
        outDir = outDir,
        sourcePath = srcPath,
        designPath = designPath,
        designTheme = designTheme,
        boardTheme = null,
        board = null,
        errors = errors,
        renders = emptyList(),
      )
    }
    return RobolectricRenderResponse(
      id = request.id,
      ok = true,
      valid = false,
      pass = false,
      compileMs = compileMs,
      designTheme = designTheme,
      errors = errors,
    )
  }

  /** Loads the board from the classes the host backend staged; the runner never compiles. */
  private fun loadStagedBoard(
    request: RobolectricRenderRequest,
    sourceFile: String,
    loadImmediately: Boolean,
  ): StagedBoardOutcome {
    val classesDir =
      request.classesDir
        ?: return StagedBoardOutcome.Failed(
          errors = listOf(ValidationError(sourceFile, "Request has no staged classesDir")),
          ok = false,
          errorMessage = "Request has no staged classesDir",
        )
    synchronized(boardCache) { boardCache[classesDir] }
      ?.let {
        return StagedBoardOutcome.Ready(it)
      }
    val board =
      try {
        loadBoard(sourceFile, readStagedClasses(Paths.get(classesDir)))
      } catch (t: Throwable) {
        val msg = "${t::class.java.name}: ${t.message}"
        return StagedBoardOutcome.Failed(
          errors = listOf(ValidationError(sourceFile, msg)),
          ok = !loadImmediately,
          errorMessage = msg,
        )
      }
    synchronized(boardCache) { boardCache[classesDir] = board }
    return StagedBoardOutcome.Ready(board)
  }

  private fun readStagedClasses(classesDir: Path): Map<String, ByteArray> {
    val result = linkedMapOf<String, ByteArray>()
    Files.walk(classesDir).use { paths ->
      paths
        .filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".class") }
        .sorted()
        .forEach { file ->
          val name = classesDir.relativize(file).toString().removeSuffix(".class")
          result[name.replace(java.io.File.separatorChar, '.')] = Files.readAllBytes(file)
        }
    }
    return result
  }

  /** Serves the asset blobs the backend staged, matching what the device receives. */
  private fun stagedAssetResolver(request: RobolectricRenderRequest): ((String) -> ImageBitmap?)? {
    if (request.assetMap.isEmpty()) return null
    val blobsDir = Paths.get(checkNotNull(request.assetBlobsDir) { "Request has no assetBlobsDir" })
    val cache = DeviceAssetCache()
    for (sha in request.assetMap.values.distinct()) {
      val blob = blobsDir.resolve(sha)
      check(Files.isRegularFile(blob)) { "Staged asset blob $sha is missing" }
      cache.put(sha, Files.readAllBytes(blob))
    }
    return cache.resolverFor(request.assetMap)
  }

  private sealed interface StagedBoardOutcome {
    data class Ready(val board: ExplorationBoard) : StagedBoardOutcome

    data class Failed(
      val errors: List<ValidationError>,
      val ok: Boolean,
      val errorMessage: String? = null,
    ) : StagedBoardOutcome
  }

  private fun buildDeviceBoardMeta(board: ExplorationBoard): DeviceBoardMeta =
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

  fun handleRequest(request: RobolectricRenderRequest): RobolectricRenderResponse {
    if (request.mode == "ping") {
      return RobolectricRenderResponse(id = request.id, ok = true, valid = true, pass = true)
    }

    val writeReport = request.mode == "report" || request.writeReport
    val outDir = request.outDir?.let { Paths.get(it).toAbsolutePath().normalize() }
    if (writeReport && outDir == null) {
      return RobolectricRenderResponse(
        id = request.id,
        ok = false,
        valid = false,
        pass = false,
        error = "outDir is required for report mode",
      )
    }

    val srcPath = Paths.get(request.sourcePath).toAbsolutePath().normalize()
    if (!Files.isRegularFile(srcPath)) {
      return invalidResponse(
        request = request,
        srcPath = srcPath,
        outDir = outDir,
        writeReport = writeReport,
        designPath = null,
        designTheme = null,
        errors = listOf(ValidationError(request.sourcePath, "File not found: $srcPath")),
      )
    }

    val sourceFile = srcPath.fileName.toString()
    if ((request.mode == "compile" || writeReport) && !sourceFile.endsWith(".explore.kt")) {
      return invalidResponse(
        request = request,
        srcPath = srcPath,
        outDir = outDir,
        writeReport = writeReport,
        designPath = null,
        designTheme = null,
        errors =
          listOf(ValidationError(srcPath.toString(), "Expected an .explore.kt exploration file")),
      )
    }

    val explicitDesign = request.designPath?.let { Paths.get(it).toAbsolutePath().normalize() }
    val resolvedDesignPath = explicitDesign ?: DesignMdLocator.find(srcPath)
    val designOverride =
      resolveDesignOverride(resolvedDesignPath, explicit = explicitDesign != null)
    when (designOverride) {
      is DesignOutcome.Invalid ->
        return invalidResponse(
          request = request,
          srcPath = srcPath,
          outDir = outDir,
          writeReport = writeReport,
          designPath = designOverride.path,
          designTheme = null,
          errors = listOf(designOverride.error),
        )
      is DesignOutcome.Ok -> Unit
    }

    val sourceText = Files.readString(srcPath)
    val loadInSandboxError = request.mode == "render" && !writeReport && request.targets == null
    val staged = loadStagedBoard(request, sourceFile, loadImmediately = loadInSandboxError)
    if (staged is StagedBoardOutcome.Failed) {
      if (!staged.ok) {
        return RobolectricRenderResponse(
          id = request.id,
          ok = false,
          valid = false,
          pass = false,
          error = staged.errorMessage,
          errors = staged.errors,
        )
      }
      return invalidResponse(
        request = request,
        srcPath = srcPath,
        outDir = outDir,
        writeReport = writeReport,
        designPath = designOverride.path,
        designTheme = designOverride.theme,
        errors = staged.errors,
      )
    }

    val board = (staged as StagedBoardOutcome.Ready).board
    val assetResolver =
      try {
        stagedAssetResolver(request)
      } catch (t: Throwable) {
        val msg = "${t::class.java.name}: ${t.message}"
        return RobolectricRenderResponse(
          id = request.id,
          ok = false,
          valid = false,
          pass = false,
          error = msg,
          errors = listOf(ValidationError(sourceFile, msg)),
        )
      }
    val boardMeta = buildDeviceBoardMeta(board)
    val boardTheme = board.toThemeOverride()

    if (request.mode == "compile") {
      return RobolectricRenderResponse(
        id = request.id,
        ok = true,
        valid = true,
        pass = true,
        compileMs = 0L,
        surface = board.surfaceSpec.key,
        boardName = board.name,
        boardMeta = boardMeta,
        boardTheme = boardTheme,
        designTheme = designOverride.theme,
      )
    }

    if (outDir != null) {
      Files.createDirectories(outDir)
    }

    val requestedTargets = request.targets
    if (requestedTargets != null) {
      return handleTargetsRequest(
        request = request,
        board = board,
        boardMeta = boardMeta,
        boardTheme = boardTheme,
        designTheme = designOverride.theme,
        compileMs = 0L,
        sourceFile = sourceFile,
        sourceText = sourceText,
        assetResolver = assetResolver,
        outDir = outDir,
        targets = requestedTargets,
      )
    }

    if (writeReport) {
      return handleReportRequest(
        request = request,
        srcPath = srcPath,
        outDir = checkNotNull(outDir),
        designPath = designOverride.path,
        designTheme = designOverride.theme,
        boardTheme = boardTheme,
        board = board,
        boardMeta = boardMeta,
        compileMs = 0L,
        sourceFile = sourceFile,
        sourceText = sourceText,
        assetResolver = assetResolver,
      )
    }

    val renderStartNs = System.nanoTime()
    val outcomes =
      try {
        renderBoardTargets(
          board = board,
          sourceFile = sourceFile,
          sourceText = sourceText,
          assetResolver = assetResolver,
          designMd = designOverride.theme,
          directionFilter = request.direction,
          screenFilter = request.screens?.toSet(),
          viewFilter = request.views?.toSet(),
          overlayId = request.overlay,
          density = request.density,
          outDir = outDir,
          encodePng = request.encodePng && outDir != null,
        )
      } catch (t: Throwable) {
        val msg = "${t::class.java.name}: ${t.message}"
        return RobolectricRenderResponse(
          id = request.id,
          ok = false,
          valid = false,
          pass = false,
          compileMs = 0L,
          error = msg,
          errors = listOf(ValidationError(sourceFile, msg)),
        )
      }
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
      compileMs = 0L,
      renderMs = renderMs,
      surface = board.surfaceSpec.key,
      boardName = board.name,
      boardMeta = boardMeta,
      boardTheme = boardTheme,
      designTheme = designOverride.theme,
      trees = trees,
      endTrees = endTrees,
      writtenFiles = writtenFiles,
    )
  }

  private fun handleTargetsRequest(
    request: RobolectricRenderRequest,
    board: ExplorationBoard,
    boardMeta: DeviceBoardMeta,
    boardTheme: ThemeOverride?,
    designTheme: ThemeOverride?,
    compileMs: Long,
    sourceFile: String,
    sourceText: String,
    assetResolver: ((String) -> ImageBitmap?)?,
    outDir: Path?,
    targets: List<DeviceRenderTarget>,
  ): RobolectricRenderResponse {
    val startNs = System.nanoTime()
    val results = ArrayList<RobolectricTargetResult>(targets.size)
    val trees = mutableListOf<LayoutTree>()
    val endTrees = mutableListOf<LayoutTree>()
    val writtenFiles = mutableListOf<String>()
    targets.forEachIndexed { idx, target ->
      val targetStartNs = System.nanoTime()
      try {
        val dir =
          board.directions.firstOrNull { it.name == target.direction }
            ?: error("Unknown direction '${target.direction}'")
        val view = Views.byName(target.viewName) ?: error("Unknown view '${target.viewName}'")
        val wantPng = target.frameFormat != FrameFormat.NONE && outDir != null
        val wantEndPng = wantPng && target.includeEndFrame
        val stem =
          listOfNotNull(
              "t${idx}",
              sanitizeToken(dir.name),
              sanitizeToken(target.screen),
              view.name,
              target.overlayId?.let(::sanitizeToken),
            )
            .joinToString("-")
        val outcome =
          renderSingleView(
            board = board,
            direction = dir,
            screenId = target.screen,
            view = view,
            sourceFile = sourceFile,
            sourceText = sourceText,
            assetResolver = assetResolver,
            designMd = designTheme,
            overlayId = target.overlayId,
            density = target.densityScale,
            fontScale = target.fontScale,
            darkOverride = target.darkOverride,
            seedOverride = target.seedOverride,
            variantOverride = target.variantOverride,
            outDir = outDir,
            encodePng = wantPng,
            encodeEndPng = wantEndPng,
            fileStemOverride = stem,
          )
        val elapsedMs = (System.nanoTime() - targetStartNs) / NANOS_PER_MS
        val pngPath = outcome.writtenFiles.getOrNull(0)
        val endPngPath = outcome.writtenFiles.getOrNull(1)
        trees += outcome.tree
        outcome.endTree?.let { endTrees += it }
        writtenFiles += outcome.writtenFiles
        results +=
          RobolectricTargetResult(
            direction = target.direction,
            screen = target.screen,
            viewName = target.viewName,
            overlayId = target.overlayId,
            densityScale = target.densityScale,
            renderMs = elapsedMs,
            tree = outcome.tree,
            endTree = outcome.endTree,
            pngPath = pngPath,
            endPngPath = endPngPath,
          )
      } catch (t: Throwable) {
        val elapsedMs = (System.nanoTime() - targetStartNs) / NANOS_PER_MS
        results +=
          RobolectricTargetResult(
            direction = target.direction,
            screen = target.screen,
            viewName = target.viewName,
            overlayId = target.overlayId,
            densityScale = target.densityScale,
            renderMs = elapsedMs,
            error = "${t::class.java.name}: ${t.message}",
          )
      }
    }
    val totalMs = (System.nanoTime() - startNs) / NANOS_PER_MS
    val allPass = results.all {
      it.error == null && it.tree?.pass == true && (it.endTree?.pass ?: true)
    }
    return RobolectricRenderResponse(
      id = request.id,
      ok = true,
      valid = true,
      pass = allPass,
      compileMs = compileMs,
      renderMs = totalMs,
      surface = board.surfaceSpec.key,
      boardName = board.name,
      boardMeta = boardMeta,
      boardTheme = boardTheme,
      designTheme = designTheme,
      trees = trees,
      endTrees = endTrees,
      writtenFiles = writtenFiles,
      results = results,
    )
  }

  private fun handleReportRequest(
    request: RobolectricRenderRequest,
    srcPath: Path,
    outDir: Path,
    designPath: Path?,
    designTheme: ThemeOverride?,
    boardTheme: ThemeOverride?,
    board: ExplorationBoard,
    boardMeta: DeviceBoardMeta,
    compileMs: Long,
    sourceFile: String,
    sourceText: String,
    assetResolver: ((String) -> ImageBitmap?)?,
  ): RobolectricRenderResponse {
    val startNs = System.nanoTime()
    val surface = board.surfaceSpec
    val activeDirections =
      if (request.direction == null) board.directions
      else {
        val match = board.directions.firstOrNull { it.name == request.direction }
        if (match == null) {
          val known = board.directions.joinToString { it.name }
          return RobolectricRenderResponse(
            id = request.id,
            ok = false,
            valid = false,
            pass = false,
            compileMs = compileMs,
            error = "Unknown direction '${request.direction}' (available: $known)",
          )
        }
        listOf(match)
      }
    val selectedViews =
      if (request.views == null) surface.views
      else {
        val unknown =
          request.views!!.filter { reqName -> surface.views.none { it.name == reqName } }
        if (unknown.isNotEmpty()) {
          return RobolectricRenderResponse(
            id = request.id,
            ok = false,
            valid = false,
            pass = false,
            compileMs = compileMs,
            error = "Unknown view(s): ${unknown.joinToString()}",
          )
        }
        request.views!!.map { reqName -> surface.views.first { it.name == reqName } }
      }
    val screenFilter = request.screens?.toSet()
    val singleDirection = activeDirections.size == 1 && board.directions.size == 1

    val entries = mutableListOf<RenderEntry>()
    val trees = mutableListOf<LayoutTree>()
    val endTrees = mutableListOf<LayoutTree>()
    val writtenFiles = mutableListOf<String>()
    val results = mutableListOf<RobolectricTargetResult>()

    for (dir in activeDirections) {
      val screens =
        dir.screenIds.filter { sid ->
          val prefix = if (singleDirection) sid else "${sanitizeSegment(dir.name)}__$sid"
          val slashName = "${dir.name}/$sid"
          screenFilter == null ||
            sid in screenFilter ||
            prefix in screenFilter ||
            slashName in screenFilter
        }
      for (screenId in screens) {
        val prefix = if (singleDirection) screenId else "${sanitizeSegment(dir.name)}__$screenId"
        val baseChecks = HashMap<String, ViewCheck>()
        for (view in selectedViews) {
          val filesBase = "$prefix-${view.name}"
          val targetStartNs = System.nanoTime()
          try {
            val outcome =
              renderSingleView(
                board = board,
                direction = dir,
                screenId = screenId,
                view = view,
                sourceFile = sourceFile,
                sourceText = sourceText,
                assetResolver = assetResolver,
                designMd = designTheme,
                overlayId = null,
                density = request.density,
                outDir = outDir,
                encodePng = true,
                encodeEndPng = true,
                fileStemOverride = filesBase,
              )
            val elapsedMs = (System.nanoTime() - targetStartNs) / NANOS_PER_MS
            val fullCheck = ViewCheck.of(outcome.tree, surface)
            baseChecks[view.name] = fullCheck
            writeTreeJson(outDir, filesBase, outcome.tree)
            val endFiles =
              outcome.endTree?.let { et ->
                val endBase = "$filesBase-end"
                writeTreeJson(outDir, endBase, et)
                endTrees += et
                endBase
              }
            trees += outcome.tree
            writtenFiles += outcome.writtenFiles
            entries += RenderEntry.of(prefix, view.name, filesBase, fullCheck, endFiles)
            results +=
              RobolectricTargetResult(
                direction = dir.name,
                screen = screenId,
                viewName = view.name,
                densityScale = request.density,
                renderMs = elapsedMs,
                tree = outcome.tree,
                endTree = outcome.endTree,
                pngPath = outcome.writtenFiles.getOrNull(0),
                endPngPath = outcome.writtenFiles.getOrNull(1),
              )
          } catch (t: Throwable) {
            val elapsedMs = (System.nanoTime() - targetStartNs) / NANOS_PER_MS
            val failedCheck = ViewCheck.renderError(t)
            baseChecks[view.name] = failedCheck
            entries += RenderEntry.of(prefix, view.name, null, failedCheck)
            results +=
              RobolectricTargetResult(
                direction = dir.name,
                screen = screenId,
                viewName = view.name,
                densityScale = request.density,
                renderMs = elapsedMs,
                error = "${t::class.java.name}: ${t.message}",
              )
          }
        }
        if (request.includeOverlays || request.overlay == null) {
          val overlays =
            if (request.overlay != null) {
              dir.overlaysFor(screenId).filter { it.id == request.overlay }
            } else {
              dir.overlaysFor(screenId)
            }
          for (overlay in overlays) {
            val artifactName = ViewCheck.renderOverlayName(prefix, overlay.id)
            for (view in selectedViews) {
              val filesBase = "$artifactName-${view.name}"
              val targetStartNs = System.nanoTime()
              try {
                val outcome =
                  renderSingleView(
                    board = board,
                    direction = dir,
                    screenId = screenId,
                    view = view,
                    sourceFile = sourceFile,
                    sourceText = sourceText,
                    assetResolver = assetResolver,
                    designMd = designTheme,
                    overlayId = overlay.id,
                    density = request.density,
                    outDir = outDir,
                    encodePng = true,
                    encodeEndPng = true,
                    fileStemOverride = filesBase,
                  )
                val elapsedMs = (System.nanoTime() - targetStartNs) / NANOS_PER_MS
                val fullCheck = ViewCheck.of(outcome.tree, surface)
                val check = baseChecks[view.name]?.let { fullCheck.overlaying(it) } ?: fullCheck
                writeTreeJson(outDir, filesBase, outcome.tree)
                val endFiles =
                  outcome.endTree?.let { et ->
                    val endBase = "$filesBase-end"
                    writeTreeJson(outDir, endBase, et)
                    endTrees += et
                    endBase
                  }
                trees += outcome.tree
                writtenFiles += outcome.writtenFiles
                entries += RenderEntry.of(artifactName, view.name, filesBase, check, endFiles)
                results +=
                  RobolectricTargetResult(
                    direction = dir.name,
                    screen = screenId,
                    viewName = view.name,
                    overlayId = overlay.id,
                    densityScale = request.density,
                    renderMs = elapsedMs,
                    tree = outcome.tree,
                    endTree = outcome.endTree,
                    pngPath = outcome.writtenFiles.getOrNull(0),
                    endPngPath = outcome.writtenFiles.getOrNull(1),
                  )
              } catch (t: Throwable) {
                val elapsedMs = (System.nanoTime() - targetStartNs) / NANOS_PER_MS
                val failedCheck = ViewCheck.renderError(t)
                entries += RenderEntry.of(artifactName, view.name, null, failedCheck)
                results +=
                  RobolectricTargetResult(
                    direction = dir.name,
                    screen = screenId,
                    viewName = view.name,
                    overlayId = overlay.id,
                    densityScale = request.density,
                    renderMs = elapsedMs,
                    error = "${t::class.java.name}: ${t.message}",
                  )
              }
            }
          }
        }
      }
    }

    val report =
      writeReportFile(
        outDir = outDir,
        sourcePath = srcPath,
        designPath = designPath,
        designTheme = designTheme,
        boardTheme = boardTheme,
        board = board,
        errors = emptyList(),
        renders = entries,
      )
    val totalMs = (System.nanoTime() - startNs) / NANOS_PER_MS
    return RobolectricRenderResponse(
      id = request.id,
      ok = true,
      valid = true,
      pass = report.pass,
      compileMs = compileMs,
      renderMs = totalMs,
      surface = board.surfaceSpec.key,
      boardName = board.name,
      boardMeta = boardMeta,
      boardTheme = boardTheme,
      designTheme = designTheme,
      trees = trees,
      endTrees = endTrees,
      writtenFiles = writtenFiles,
      results = results,
    )
  }

  private fun writeTreeJson(outDir: Path, base: String, tree: LayoutTree) {
    Files.writeString(
      outDir.resolve("$base.tree.json"),
      PreviewJson.pretty.encodeToString(LayoutTree.serializer(), tree) + "\n",
    )
  }

  fun loadBoard(sourceFile: String, classes: Map<String, ByteArray>): ExplorationBoard {
    val loader = SandboxClassLoader(classes, RobolectricBoardRenderer::class.java.classLoader!!)
    val candidates = mutableListOf<java.lang.reflect.Method>()
    for (clsName in classes.keys) {
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
    assetResolver: ((String) -> ImageBitmap?)? = null,
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
                assetResolver = assetResolver,
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
                assetResolver = assetResolver,
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
    assetResolver: ((String) -> ImageBitmap?)? = null,
    designMd: ThemeOverride? = null,
    overlayId: String? = null,
    density: Float = 2f,
    fontScale: Float = 1f,
    darkOverride: Boolean? = null,
    seedOverride: String? = null,
    variantOverride: String? = null,
    outDir: Path? = null,
    encodePng: Boolean = false,
    encodeEndPng: Boolean = encodePng,
    fileStemOverride: String? = null,
  ): RobolectricRenderOutcome {
    val controller = Robolectric.buildActivity(ComponentActivity::class.java)
    var recomposer: Recomposer? = null
    var job: Job? = null
    var composeView: ComposeView? = null
    try {
      val activity = controller.get()
      activity.setTheme(android.R.style.Theme_Material_NoActionBar)
      WindowCompat.setDecorFitsSystemWindows(activity.window, false)
      controller.create().start().resume().visible()
      ProposalFonts.init(activity.assets)

      val effectiveView =
        if (darkOverride != null && board.surface == BoardSurface.Phone) {
          view.copy(dark = darkOverride)
        } else {
          view
        }
      val effectiveFontScale = fontScale.coerceAtLeast(0.5f)
      val widthPx = (effectiveView.width * density).roundToInt().coerceAtLeast(1)
      val heightPx = (effectiveView.height * density).roundToInt().coerceAtLeast(1)
      val dpi = (BASE_DPI * density).roundToInt().coerceAtLeast(120)
      val perimeterPx = (1f * density).roundToInt().coerceAtLeast(1)

      val cfg =
        Configuration(activity.resources.configuration).apply {
          densityDpi = dpi
          this.fontScale = effectiveFontScale
          screenWidthDp = effectiveView.width
          screenHeightDp = effectiveView.height
          smallestScreenWidthDp = minOf(effectiveView.width, effectiveView.height)
          uiMode =
            (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
              if (effectiveView.dark) Configuration.UI_MODE_NIGHT_YES
              else Configuration.UI_MODE_NIGHT_NO
          screenLayout =
            (screenLayout and Configuration.SCREENLAYOUT_ROUND_MASK.inv()) or
              if (effectiveView.round) Configuration.SCREENLAYOUT_ROUND_YES
              else Configuration.SCREENLAYOUT_ROUND_NO
        }
      val overrideCtx =
        object : ContextThemeWrapper(activity, activity.theme) {
          init {
            applyOverrideConfiguration(cfg)
          }
        }

      val clock = BroadcastFrameClock()
      val recomposerFailure = AtomicReference<Throwable?>(null)
      lateinit var activeRecomposer: Recomposer
      val handler = CoroutineExceptionHandler { _, t ->
        if (t !is CancellationException) {
          recomposerFailure.compareAndSet(null, t)
          activeRecomposer.cancel()
        }
      }
      activeRecomposer = Recomposer(AndroidUiDispatcher.Main + clock + handler)
      recomposer = activeRecomposer
      val tables = Collections.synchronizedSet(LinkedHashSet<CompositionData>())
      var rootCoords: LayoutCoordinates? = null
      var emptyDetailScreen: String? = null
      val variant = variantOverride?.let { PaletteVariant.fromWire(it) }
      val designSystem =
        board.resolveDesignSystem(
          designMd = designMd,
          seedOverride = seedOverride,
          variantOverride = variant,
        )

      val insetContainer = InsetConsumingFrameLayout(overrideCtx)
      val cv = ComposeView(overrideCtx)
      composeView = cv
      ViewCompat.setOnApplyWindowInsetsListener(activity.window.decorView) { _, _ ->
        WindowInsetsCompat.CONSUMED
      }
      ViewCompat.setOnApplyWindowInsetsListener(insetContainer) { _, _ ->
        WindowInsetsCompat.CONSUMED
      }
      ViewCompat.setOnApplyWindowInsetsListener(cv) { _, _ -> WindowInsetsCompat.CONSUMED }

      insetContainer.addView(cv, ViewGroup.LayoutParams(widthPx, heightPx))
      activity.window.decorView.compositionContext = activeRecomposer
      cv.setParentCompositionContext(activeRecomposer)
      activity.setContentView(insetContainer, ViewGroup.LayoutParams(widthPx, heightPx))

      job =
        CoroutineScope(AndroidUiDispatcher.Main + clock + handler).launch {
          try {
            activeRecomposer.runRecomposeAndApplyChanges()
          } catch (t: Throwable) {
            if (t is CancellationException) throw t
            recomposerFailure.compareAndSet(null, t)
          }
        }

      cv.setContent {
        CompositionLocalProvider(
          LocalContext provides overrideCtx,
          LocalConfiguration provides cfg,
          LocalDensity provides Density(density, fontScale = effectiveFontScale),
        ) {
          Box(Modifier.onGloballyPositioned { rootCoords = it }) {
            InspectedSubcomposition(tables) {
              ExploreBoardFrame(
                board = board,
                direction = direction,
                screenId = screenId,
                view = effectiveView,
                designSystem = designSystem,
                explorationDir = null,
                assetResolver = assetResolver,
                overlayId = overlayId,
                onEmptyDetail = { emptyDetailScreen = it },
              )
            }
          }
        }
      }

      shadowOf(Looper.getMainLooper()).idle()
      recomposerFailure.get()?.let { throw it }
      var virtualTimeNs = 0L
      val wSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
      val hSpec = View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY)
      val mainLoc = IntArray(2)

      fun layoutWindows(): FrameStatus {
        activity.window.decorView.measure(wSpec, hSpec)
        activity.window.decorView.layout(0, 0, widthPx, heightPx)
        insetContainer.measure(wSpec, hSpec)
        insetContainer.layout(0, 0, widthPx, heightPx)
        cv.measure(wSpec, hSpec)
        cv.layout(0, 0, widthPx, heightPx)
        activity.window.decorView.getLocationOnScreen(mainLoc)
        val mainDecor = activity.window.decorView
        val overlays =
          WindowInspector.getGlobalWindowViews().filter {
            it !== mainDecor && it.visibility == View.VISIBLE
          }
        var awaitingAttach = false
        for (win in overlays) {
          val m = OverlayWindowHelper.measureAndLayoutOverlayWindow(win, mainLoc, widthPx, heightPx)
          if (!win.isAttachedToWindow || m.width == 0) awaitingAttach = true
        }
        recomposerFailure.get()?.let { throw it }
        return FrameStatus(
          workPending = clock.hasAwaiters || activeRecomposer.hasPendingWork,
          overlayPresent = overlays.isNotEmpty(),
          awaitingOverlayAttach = awaitingAttach,
        )
      }

      val minScrollerWidthPx = widthPx * 0.6f
      val scrollStepPx = effectiveView.height * density / 2f

      fun collectUnmergedRoots(): List<SemanticsNode> {
        val windows = WindowInspector.getGlobalWindowViews().filter { it.isAttachedToWindow }
        return OverlayWindowHelper.collectComposeRoots(windows).map {
          it.semanticsOwner.unmergedRootSemanticsNode
        }
      }

      val driver =
        object : WearScrollDriver {
          override suspend fun advanceFrame(): FrameStatus {
            Snapshot.sendApplyNotifications()
            virtualTimeNs += SettlePolicy.FRAME_NS
            clock.sendFrame(virtualTimeNs)
            Snapshot.sendApplyNotifications()
            shadowOf(Looper.getMainLooper()).idle()
            recomposerFailure.get()?.let { throw it }
            return layoutWindows()
          }

          override suspend fun finishSettle() {
            shadowOf(Looper.getMainLooper()).idle()
            recomposerFailure.get()?.let { throw it }
          }

          override suspend fun scrollOnce(): Boolean =
            WearScrollHelper.scrollDown(collectUnmergedRoots(), minScrollerWidthPx, scrollStepPx)
        }

      fun settleFrames() = runBlocking { SettlePolicy.settle(driver) }

      fun captureSnapshot(): Pair<LayoutTree, RawRgbaFrame> {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val rgba = ByteArray(widthPx * heightPx * 4)
        val explicitWindowOffsets = IdentityHashMap<View, Offset>()
        val mainDecor = activity.window.decorView
        val mainLoc = IntArray(2)
        mainDecor.getLocationOnScreen(mainLoc)

        val attachedWindows =
          WindowInspector.getGlobalWindowViews().filter { it.isAttachedToWindow }
        try {
          val canvas = Canvas(bitmap)
          canvas.drawColor(AndroidColor.BLACK)

          for (win in attachedWindows) {
            if (win !== mainDecor) {
              val lp = win.layoutParams as? WindowManager.LayoutParams
              val m =
                OverlayWindowHelper.measureAndLayoutOverlayWindow(win, mainLoc, widthPx, heightPx)
              explicitWindowOffsets[win] = m.offset
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
              canvas.translate(m.offset.x, m.offset.y)
              if (win.width == 0 && m.width > 0 && win is ViewGroup && win.childCount > 0) {
                win.getChildAt(0).draw(canvas)
              } else {
                win.draw(canvas)
              }
              canvas.restore()
            } else {
              win.draw(canvas)
            }
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
        val composeRoots = OverlayWindowHelper.collectComposeRoots(attachedWindows)
        val mergedRoots = composeRoots.map { it.semanticsOwner.rootSemanticsNode }
        val unmergedRoots = composeRoots.map { it.semanticsOwner.unmergedRootSemanticsNode }
        val layoutWindowOffsets =
          OverlayWindowHelper.buildLayoutWindowOffsets(
            mainDecorView = mainDecor,
            composeRoots = composeRoots,
            tables = tables,
            explicitWindowOffsets = explicitWindowOffsets,
          )

        val extracted =
          AndroidSlotTreeExtractor.extract(
            tables = tables,
            mergedRoots = mergedRoots,
            unmergedRoots = unmergedRoots,
            rootCoordinates = rootCoords,
            frame = frame,
            screen = screenId,
            view = effectiveView,
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

      settleFrames()
      val (initialTree, initialFrame) = captureSnapshot()
      var finalTree = initialTree
      var endTree: LayoutTree? = null
      var endFrame: RawRgbaFrame? = null

      if (
        board.surface == BoardSurface.Wear &&
          overlayId == null &&
          !WearScrollHelper.hasWearPicker(tables, finalTree)
      ) {
        val positions = mutableListOf(finalTree)
        val scrolled = runBlocking { SettlePolicy.scrollThrough(driver) { captureSnapshot() } }
        for ((stepTree, stepFrame) in scrolled) {
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
          fileStemOverride
            ?: listOfNotNull(
                sanitizeToken(direction.name),
                sanitizeToken(screenId),
                effectiveView.name,
                overlayId?.let(::sanitizeToken),
              )
              .joinToString("-")
        val mainFile = outDir.resolve("$stem.png")
        Files.write(
          mainFile,
          FastPngEncoder.encodeFast(initialFrame.width, initialFrame.height, initialFrame.bytes),
        )
        writtenFiles += mainFile.toString()
        if (endFrame != null && encodeEndPng) {
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
      runCatching { composeView?.disposeComposition() }
      recomposer?.cancel()
      job?.cancel()
      runCatching { controller.pause().stop().destroy() }
      shadowOf(Looper.getMainLooper()).idle()
    }
  }

  private sealed interface DesignOutcome {
    data class Ok(val path: Path?, val theme: ThemeOverride?) : DesignOutcome

    data class Invalid(val path: Path?, val error: ValidationError) : DesignOutcome
  }

  private fun resolveDesignOverride(designPath: Path?, explicit: Boolean): DesignOutcome {
    if (designPath == null) return DesignOutcome.Ok(null, null)
    if (!Files.isRegularFile(designPath)) {
      return if (explicit) {
        DesignOutcome.Invalid(designPath, ValidationError(designPath.toString(), "no such file"))
      } else {
        DesignOutcome.Ok(null, null)
      }
    }
    return when (val parsed = DesignMdParser.parse(Files.readString(designPath))) {
      is DesignMdParse.Parsed -> DesignOutcome.Ok(designPath, parsed.theme)
      is DesignMdParse.Failed ->
        DesignOutcome.Invalid(designPath, ValidationError(designPath.toString(), parsed.message))
    }
  }

  private fun sanitizeToken(raw: String): String =
    raw.trim().lowercase().replace(Regex("[^a-z0-9._-]+"), "_")

  private fun sanitizeSegment(name: String): String =
    name.trim().replace(Regex("""[^A-Za-z0-9._-]+"""), "_").ifBlank { "direction" }

  private fun sha256Hex(text: String): String =
    MessageDigest.getInstance("SHA-256")
      .digest(text.toByteArray(StandardCharsets.UTF_8))
      .joinToString("") { "%02x".format(it) }
}
