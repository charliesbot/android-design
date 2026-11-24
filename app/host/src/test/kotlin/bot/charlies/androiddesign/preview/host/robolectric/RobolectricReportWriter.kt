package bot.charlies.androiddesign.preview.host.robolectric

import bot.charlies.androiddesign.preview.domain.RenderEntry
import bot.charlies.androiddesign.preview.domain.RenderReport
import bot.charlies.androiddesign.preview.domain.ReportTheme
import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.domain.image.FastPngEncoder
import bot.charlies.androiddesign.preview.feature.preview.sandbox.RawRgbaFrame
import bot.charlies.androiddesign.preview.kit.DirectionSpec
import bot.charlies.androiddesign.preview.kit.ExplorationBoard
import bot.charlies.androiddesign.preview.model.DeviceBoardMeta
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.RobolectricRenderRequest
import bot.charlies.androiddesign.preview.model.RobolectricRenderResponse
import bot.charlies.androiddesign.preview.model.RobolectricTargetResult
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.ValidationError
import bot.charlies.androiddesign.preview.model.ViewSpec
import java.nio.file.Files
import java.nio.file.Path

/** Handles CLI/eval `report.json`, tree JSON, and PNG file persistence for Robolectric runs. */
internal object RobolectricReportWriter {
  private const val NANOS_PER_MS = 1_000_000L

  fun writeInvalidResponse(
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

  fun writePngFiles(
    outDir: Path,
    directionName: String,
    screenId: String,
    viewName: String,
    overlayId: String?,
    frame: RawRgbaFrame,
    endFrame: RawRgbaFrame?,
    encodeEndPng: Boolean,
    fileStemOverride: String?,
  ): List<String> {
    val stem =
      fileStemOverride
        ?: listOfNotNull(
            sanitizeToken(directionName),
            sanitizeToken(screenId),
            viewName,
            overlayId?.let(::sanitizeToken),
          )
          .joinToString("-")
    val written = mutableListOf<String>()
    val mainFile = outDir.resolve("$stem.png")
    Files.write(mainFile, FastPngEncoder.encodeFast(frame.width, frame.height, frame.bytes))
    written += mainFile.toString()
    if (endFrame != null && encodeEndPng) {
      val endFile = outDir.resolve("$stem-end.png")
      Files.write(
        endFile,
        FastPngEncoder.encodeFast(endFrame.width, endFrame.height, endFrame.bytes),
      )
      written += endFile.toString()
    }
    return written
  }

  fun handleReportRequest(
    request: RobolectricRenderRequest,
    srcPath: Path,
    outDir: Path,
    designPath: Path?,
    designTheme: ThemeOverride?,
    boardTheme: ThemeOverride?,
    board: ExplorationBoard,
    boardMeta: DeviceBoardMeta,
    compileMs: Long,
    renderTarget:
      (
        dir: DirectionSpec,
        screenId: String,
        view: ViewSpec,
        overlayId: String?,
        fileStem: String,
      ) -> RobolectricRenderOutcome,
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
          renderReportTarget(
            board = board,
            dir = dir,
            screenId = screenId,
            prefix = prefix,
            overlayId = null,
            view = view,
            density = request.density,
            outDir = outDir,
            baseChecks = baseChecks,
            entries = entries,
            trees = trees,
            endTrees = endTrees,
            writtenFiles = writtenFiles,
            results = results,
            renderTarget = renderTarget,
          )
        }
        if (request.includeOverlays || request.overlay == null) {
          val overlays =
            if (request.overlay != null) {
              dir.overlaysFor(screenId).filter { it.id == request.overlay }
            } else {
              dir.overlaysFor(screenId)
            }
          for (overlay in overlays) {
            for (view in selectedViews) {
              renderReportTarget(
                board = board,
                dir = dir,
                screenId = screenId,
                prefix = prefix,
                overlayId = overlay.id,
                view = view,
                density = request.density,
                outDir = outDir,
                baseChecks = baseChecks,
                entries = entries,
                trees = trees,
                endTrees = endTrees,
                writtenFiles = writtenFiles,
                results = results,
                renderTarget = renderTarget,
              )
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

  private fun renderReportTarget(
    board: ExplorationBoard,
    dir: DirectionSpec,
    screenId: String,
    prefix: String,
    overlayId: String?,
    view: ViewSpec,
    density: Float,
    outDir: Path,
    baseChecks: MutableMap<String, ViewCheck>,
    entries: MutableList<RenderEntry>,
    trees: MutableList<LayoutTree>,
    endTrees: MutableList<LayoutTree>,
    writtenFiles: MutableList<String>,
    results: MutableList<RobolectricTargetResult>,
    renderTarget:
      (
        dir: DirectionSpec,
        screenId: String,
        view: ViewSpec,
        overlayId: String?,
        fileStem: String,
      ) -> RobolectricRenderOutcome,
  ) {
    val artifactName = overlayId?.let { ViewCheck.renderOverlayName(prefix, it) } ?: prefix
    val filesBase = "$artifactName-${view.name}"
    val targetStartNs = System.nanoTime()
    try {
      val outcome = renderTarget(dir, screenId, view, overlayId, filesBase)
      val elapsedMs = (System.nanoTime() - targetStartNs) / NANOS_PER_MS
      val fullCheck = ViewCheck.of(outcome.tree, board.surfaceSpec)
      val check =
        if (overlayId == null) {
          fullCheck.also { baseChecks[view.name] = it }
        } else {
          baseChecks[view.name]?.let { fullCheck.overlaying(it) } ?: fullCheck
        }
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
          overlayId = overlayId,
          densityScale = density,
          renderMs = elapsedMs,
          tree = outcome.tree,
          endTree = outcome.endTree,
          pngPath = outcome.writtenFiles.getOrNull(0),
          endPngPath = outcome.writtenFiles.getOrNull(1),
        )
    } catch (t: Throwable) {
      val elapsedMs = (System.nanoTime() - targetStartNs) / NANOS_PER_MS
      val failedCheck = ViewCheck.renderError(t)
      if (overlayId == null) {
        baseChecks[view.name] = failedCheck
      }
      entries += RenderEntry.of(artifactName, view.name, null, failedCheck)
      results +=
        RobolectricTargetResult(
          direction = dir.name,
          screen = screenId,
          viewName = view.name,
          overlayId = overlayId,
          densityScale = density,
          renderMs = elapsedMs,
          error = "${t::class.java.name}: ${t.message}",
        )
    }
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

  private fun writeTreeJson(outDir: Path, base: String, tree: LayoutTree) {
    Files.writeString(
      outDir.resolve("$base.tree.json"),
      PreviewJson.pretty.encodeToString(LayoutTree.serializer(), tree) + "\n",
    )
  }

  fun sanitizeToken(raw: String): String =
    raw.trim().lowercase().replace(Regex("[^a-z0-9._-]+"), "_")

  private fun sanitizeSegment(name: String): String =
    name.trim().replace(Regex("""[^A-Za-z0-9._-]+"""), "_").ifBlank { "direction" }
}
