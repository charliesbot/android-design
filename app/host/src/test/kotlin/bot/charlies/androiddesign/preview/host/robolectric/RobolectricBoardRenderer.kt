package bot.charlies.androiddesign.preview.host.robolectric

import androidx.compose.ui.graphics.ImageBitmap
import bot.charlies.androiddesign.preview.data.DesignMdLocator
import bot.charlies.androiddesign.preview.data.DesignMdParse
import bot.charlies.androiddesign.preview.data.DesignMdParser
import bot.charlies.androiddesign.preview.domain.sandbox.SandboxClassLoader
import bot.charlies.androiddesign.preview.feature.preview.sandbox.ComposeBoardRenderer
import bot.charlies.androiddesign.preview.feature.preview.sandbox.RawRgbaFrame
import bot.charlies.androiddesign.preview.feature.preview.sandbox.RenderTargetRequest
import bot.charlies.androiddesign.preview.host.sandbox.DeviceAssetCache
import bot.charlies.androiddesign.preview.kit.DirectionSpec
import bot.charlies.androiddesign.preview.kit.ExplorationBoard
import bot.charlies.androiddesign.preview.model.DeviceBoardMeta
import bot.charlies.androiddesign.preview.model.DeviceRenderTarget
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.RobolectricRenderRequest
import bot.charlies.androiddesign.preview.model.RobolectricRenderResponse
import bot.charlies.androiddesign.preview.model.RobolectricTargetResult
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.ValidationError
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import java.lang.reflect.Modifier as JavaModifier
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlinx.coroutines.runBlocking

internal data class RobolectricRenderOutcome(
  val tree: LayoutTree,
  val endTree: LayoutTree? = null,
  val frame: RawRgbaFrame,
  val endFrame: RawRgbaFrame? = null,
  val writtenFiles: List<String> = emptyList(),
)

/**
 * Headless Robolectric runner dispatch and board loader, delegating Compose settling, frame
 * capture, tree extraction, and Wear scrolling to [ComposeBoardRenderer] above
 * [RobolectricRenderHost].
 */
internal object RobolectricBoardRenderer {
  private const val NANOS_PER_MS = 1_000_000L
  private const val MAX_BOARD_CACHE = 8

  private val boardRenderer = ComposeBoardRenderer(RobolectricRenderHost())

  /** Boards loaded from a staged classes directory; each stage directory is immutable. */
  private val boardCache =
    object : LinkedHashMap<String, ExplorationBoard>(16, 0.75f, true) {
      override fun removeEldestEntry(
        eldest: MutableMap.MutableEntry<String, ExplorationBoard>?,
      ): Boolean = size > MAX_BOARD_CACHE
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
      return RobolectricReportWriter.writeInvalidResponse(
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
      return RobolectricReportWriter.writeInvalidResponse(
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
        return RobolectricReportWriter.writeInvalidResponse(
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
      return RobolectricReportWriter.writeInvalidResponse(
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
    val boardMeta = ComposeBoardRenderer.buildBoardMeta(board)
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
      val reportOutDir = checkNotNull(outDir)
      return RobolectricReportWriter.handleReportRequest(
        request = request,
        srcPath = srcPath,
        outDir = reportOutDir,
        designPath = designOverride.path,
        designTheme = designOverride.theme,
        boardTheme = boardTheme,
        board = board,
        boardMeta = boardMeta,
        compileMs = 0L,
        renderTarget = { dir, screenId, view, overlayId, fileStem ->
          renderSingleView(
            board = board,
            direction = dir,
            screenId = screenId,
            view = view,
            sourceFile = sourceFile,
            sourceText = sourceText,
            assetResolver = assetResolver,
            designMd = designOverride.theme,
            overlayId = overlayId,
            density = request.density,
            outDir = reportOutDir,
            encodePng = true,
            encodeEndPng = true,
            fileStemOverride = fileStem,
          )
        },
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
              "t$idx",
              RobolectricReportWriter.sanitizeToken(dir.name),
              RobolectricReportWriter.sanitizeToken(target.screen),
              view.name,
              target.overlayId?.let(RobolectricReportWriter::sanitizeToken),
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
        val resolvedOverlay =
          if (overlayId != null) {
            dir.overlaysFor(screenId).firstOrNull { it.id == overlayId }?.id ?: continue
          } else {
            null
          }
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
              overlayId = resolvedOverlay,
              density = density,
              outDir = outDir,
              encodePng = encodePng,
            )
        }
      }
    }
    return outcomes
  }

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
    val rendered = runBlocking {
      boardRenderer.render(
        RenderTargetRequest(
          board = board,
          direction = direction,
          screenId = screenId,
          view = view,
          sourceFile = sourceFile,
          sourceText = sourceText,
          designMd = designMd,
          overlayId = overlayId,
          density = density,
          fontScale = fontScale,
          darkOverride = darkOverride,
          seedOverride = seedOverride,
          variantOverride = variantOverride,
          assetResolver = assetResolver,
        ),
      )
    }
    val writtenFiles =
      if (encodePng && outDir != null) {
        RobolectricReportWriter.writePngFiles(
          outDir = outDir,
          directionName = direction.name,
          screenId = screenId,
          viewName = rendered.target.effectiveView.name,
          overlayId = overlayId,
          frame = rendered.frame,
          endFrame = rendered.endFrame,
          encodeEndPng = encodeEndPng,
          fileStemOverride = fileStemOverride,
        )
      } else {
        emptyList()
      }
    return RobolectricRenderOutcome(
      tree = rendered.tree,
      endTree = rendered.endTree,
      frame = rendered.frame,
      endFrame = rendered.endFrame,
      writtenFiles = writtenFiles,
    )
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
}
