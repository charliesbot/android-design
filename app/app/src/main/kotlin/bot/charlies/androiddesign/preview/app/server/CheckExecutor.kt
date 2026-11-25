package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.cli.RenderEntry
import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.domain.layout.ViewLayout
import bot.charlies.androiddesign.preview.model.BoardSurface
import bot.charlies.androiddesign.preview.model.DeviceBoardMeta
import bot.charlies.androiddesign.preview.model.DeviceRenderTarget
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.PhoneSurface
import bot.charlies.androiddesign.preview.model.SurfaceSpec
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.WearSurface
import java.nio.file.Files
import java.nio.file.Path

/**
 * Owns check orchestration, `ViewCheck` evaluation, overlay diffing, failing-frame follow-up
 * fetches, SHA-256 PNG deduplication, primary-view policy, and host PNG persistence above a
 * [RenderWorker] seam.
 */
class CheckExecutor(val worker: RenderWorker) : AutoCloseable {
  val name: String
    get() = worker.name

  val supportsHeadlessSpeculativePrecompile: Boolean
    get() = worker.supportsHeadlessSpeculativePrecompile

  val scopedCheckDensity: Int
    get() = worker.scopedCheckDensity

  fun prepare() = worker.prepare()

  fun updateLive(entry: CompileEntry, update: LivePreviewUpdate) = worker.updateLive(entry, update)

  override fun close() = worker.close()

  fun compile(snapshot: ContentHasher.Snapshot): BackendCompileOutput =
    when (val res = worker.compile(snapshot)) {
      is WorkerCompileResult.Failed ->
        BackendCompileOutput(
          loaded =
            LoadedExploration(
              sourcePath = res.sourcePath,
              designPath = res.designPath,
              designTheme = res.designTheme,
              designSystem = DesignSystemResolver.resolve(res.designTheme, res.boardTheme),
              compileMs = res.compileMs,
              errors = res.errors,
            ),
          dexHash = res.dexHash,
          surface = BoardSurface.Phone,
          boardName = null,
          boardMeta = null,
          androidData = null,
          boardTheme = res.boardTheme,
        )
      is WorkerCompileResult.Ready -> {
        val boardSurface =
          if (res.boardMeta.surface == "wear") BoardSurface.Wear else BoardSurface.Phone
        val surfaceSpec = surfaceSpecFor(boardSurface)
        BackendCompileOutput(
          loaded =
            LoadedExploration(
              sourcePath = res.sourcePath,
              designPath = res.designPath,
              designTheme = res.designTheme,
              designSystem =
                DesignSystemResolver.resolve(res.designTheme, res.boardTheme, surfaceSpec),
              compileMs = res.compileMs,
              errors = emptyList(),
            ),
          dexHash = res.dexHash,
          surface = boardSurface,
          boardName = res.boardMeta.boardName,
          boardMeta = toBoardMeta(res.boardMeta),
          androidData = res.androidData,
          boardTheme = res.boardTheme,
        )
      }
    }

  fun renderBatchCheck(
    entry: CompileEntry,
    targets: List<CheckTargetSpec>,
    scoped: Boolean,
    rendersRoot: Path,
  ): List<RenderArtifact> = withCheckLifecycle {
    val surfaceSpec = surfaceSpecFor(entry.surface)
    val densityScale = if (scoped) worker.scopedCheckDensity else 2
    val primaryViews = primaryCheckViews(entry.surface)

    val deviceTargets =
      orderTargetsForBatchRender(targets).map { t ->
        val wantFrame = scoped || (t.overlayId == null && t.view.name in primaryViews)
        DeviceRenderTarget(
          direction = t.direction,
          screen = t.screenId,
          viewName = t.view.name,
          overlayId = t.overlayId,
          densityScale = densityScale.toFloat(),
          fontScale = 1f,
          frameFormat = if (wantFrame) FrameFormat.LZ4_RGBA else FrameFormat.NONE,
          includeEndFrame = wantFrame,
        )
      }

    val batch = worker.renderTargets(entry, deviceTargets, rendersRoot)
    if (!batch.ok && batch.targets.isEmpty()) {
      return@withCheckLifecycle buildBatchFailureArtifacts(
        targets,
        densityScale,
        batch.renderTotalMs,
        batch.error,
      )
    }

    val resultsByKey = LinkedHashMap<TargetKey, WorkerTargetOutput>(batch.targets.size)
    val framesByKey = LinkedHashMap<FrameKey, WorkerFrame>(batch.targets.size * 2)
    collectBatchOutputs(batch.targets, resultsByKey, framesByKey)

    val evaluated = evaluateTargets(targets, resultsByKey, surfaceSpec)
    val allPass = evaluated.all { it.check.pass }
    if (!scoped && !allPass) {
      fetchMissingFailingFrames(entry, evaluated, densityScale, rendersRoot, framesByKey)
    }

    val writeSet = selectTargetsToWrite(evaluated, framesByKey, scoped, allPass, primaryViews)
    evaluated.map { ev ->
      val t = ev.spec
      val key = targetKey(t)
      val mainFrame =
        framesByKey[FrameKey(t.direction, t.screenId, t.overlayId, t.view.name, false)]
      val endFrame = framesByKey[FrameKey(t.direction, t.screenId, t.overlayId, t.view.name, true)]
      val artName = artifactName(t.prefix, t.overlayId)
      buildRenderArtifact(
        contentHash = entry.contentHash,
        direction = t.direction,
        screenId = t.screenId,
        overlayId = t.overlayId,
        view = t.view,
        densityScale = densityScale,
        artifactName = artName,
        renderMs = ev.output?.renderMs ?: 0L,
        check = ev.check,
        layout = ev.layout,
        output = ev.output,
        mainFrame = mainFrame,
        endFrame = endFrame,
        writeFiles = key in writeSet,
        rendersRoot = rendersRoot,
        fallbackMime = "image/png",
      )
    }
  }

  /**
   * Renders every target in [targets] as a single view-grouped LZ4 PNG batch for [ReportRenderer],
   * evaluating overlays against their base screens and returning artifacts in [targets] order
   * without writing cache files.
   */
  fun renderBatchReport(
    entry: CompileEntry,
    targets: List<CheckTargetSpec>,
    rendersRoot: Path,
    densityScale: Int = 2,
  ): List<RenderArtifact> = withCheckLifecycle {
    if (targets.isEmpty()) return@withCheckLifecycle emptyList()
    val surfaceSpec = surfaceSpecFor(entry.surface)
    val deviceTargets =
      orderTargetsForBatchRender(targets).map { t ->
        DeviceRenderTarget(
          direction = t.direction,
          screen = t.screenId,
          viewName = t.view.name,
          overlayId = t.overlayId,
          densityScale = densityScale.toFloat(),
          fontScale = 1f,
          frameFormat = FrameFormat.LZ4_RGBA,
          includeEndFrame = true,
        )
      }

    val resultsByKey = LinkedHashMap<TargetKey, WorkerTargetOutput>(targets.size)
    val framesByKey = LinkedHashMap<FrameKey, WorkerFrame>(targets.size * 2)
    val batch =
      try {
        worker.renderTargets(entry, deviceTargets, rendersRoot)
      } catch (e: ServerReplacedException) {
        throw e
      } catch (e: SandboxWatchdogException) {
        throw e
      } catch (e: Exception) {
        if (deviceTargets.size <= 1) throw e
        // Isolate per-target crashes so healthy screens are still written to the report.
        for (singleTarget in deviceTargets) {
          val singleKey =
            TargetKey(
              singleTarget.direction,
              singleTarget.screen,
              singleTarget.overlayId,
              singleTarget.viewName,
            )
          try {
            val singleBatch = worker.renderTargets(entry, listOf(singleTarget), rendersRoot)
            collectBatchOutputs(singleBatch.targets, resultsByKey, framesByKey)
          } catch (singleErr: ServerReplacedException) {
            throw singleErr
          } catch (singleErr: SandboxWatchdogException) {
            throw singleErr
          } catch (singleErr: Exception) {
            resultsByKey[singleKey] =
              WorkerTargetOutput(
                direction = singleTarget.direction,
                screen = singleTarget.screen,
                viewName = singleTarget.viewName,
                overlayId = singleTarget.overlayId,
                error = singleErr.message ?: singleErr::class.simpleName ?: "Render failed",
              )
          }
        }
        null
      }

    if (batch != null) {
      if (!batch.ok && batch.targets.isEmpty()) {
        return@withCheckLifecycle buildBatchFailureArtifacts(
          targets,
          densityScale,
          batch.renderTotalMs,
          batch.error,
        )
      }
      collectBatchOutputs(batch.targets, resultsByKey, framesByKey)
    }

    val evaluated = evaluateTargets(targets, resultsByKey, surfaceSpec)
    evaluated.map { ev ->
      val t = ev.spec
      val mainFrame =
        framesByKey[FrameKey(t.direction, t.screenId, t.overlayId, t.view.name, false)]
      val endFrame = framesByKey[FrameKey(t.direction, t.screenId, t.overlayId, t.view.name, true)]
      val artName = artifactName(t.prefix, t.overlayId)
      buildRenderArtifact(
        contentHash = entry.contentHash,
        direction = t.direction,
        screenId = t.screenId,
        overlayId = t.overlayId,
        view = t.view,
        densityScale = densityScale,
        artifactName = artName,
        renderMs = ev.output?.renderMs ?: 0L,
        check = ev.check,
        layout = ev.layout,
        output = ev.output,
        mainFrame = mainFrame,
        endFrame = endFrame,
        writeFiles = false,
        rendersRoot = rendersRoot,
        fallbackMime = "image/png",
      )
    }
  }

  fun renderSingle(entry: CompileEntry, request: SingleRenderRequest): RenderArtifact =
    withCheckLifecycle {
      val surfaceSpec = surfaceSpecFor(entry.surface)
      val effectiveFormat =
        if (request.writeFiles && request.frameFormat == FrameFormat.WEBP_LOSSLESS) {
          FrameFormat.LZ4_RGBA
        } else {
          request.frameFormat
        }
      val fallbackMime =
        if (effectiveFormat == FrameFormat.WEBP_LOSSLESS) "image/webp" else "image/png"
      val target =
        DeviceRenderTarget(
          direction = request.direction,
          screen = request.screenId,
          viewName = request.view.name,
          overlayId = request.overlayId,
          densityScale = request.densityScale.toFloat(),
          fontScale = request.fontScale,
          darkOverride = request.darkOverride,
          seedOverride = request.seedOverride,
          variantOverride = request.variantOverride,
          frameFormat = effectiveFormat,
          includeEndFrame = effectiveFormat != FrameFormat.NONE,
        )

      val batch = worker.renderTargets(entry, listOf(target), request.rendersRoot)
      val artName = artifactName(request.prefix, request.overlayId)
      val output = batch.targets.firstOrNull()
      val tree = output?.tree
      if ((!batch.ok && output == null) || output == null || output.error != null || tree == null) {
        val msg =
          output?.error
            ?: batch.error
            ?: "Render failed for ${request.direction}/${request.screenId}"
        val failed = ViewCheck.renderError(RuntimeException(msg))
        return@withCheckLifecycle RenderArtifact(
          direction = request.direction,
          screen = request.screenId,
          overlay = request.overlayId,
          view = request.view,
          densityScale = request.densityScale,
          artifactName = artName,
          pass = false,
          renderMs = output?.renderMs ?: batch.renderTotalMs,
          check = failed,
          layout = null,
          tree = null,
          pngPath = null,
          endPngPath = null,
          pngBytes = null,
          endPngBytes = null,
          pngSha256 = null,
          entry = RenderEntry.of(artName, request.view.name, null, failed),
          imageMime = fallbackMime,
        )
      }

      val full = ViewCheck.of(tree, surfaceSpec)
      val check = if (request.baseCheck == null) full else full.overlaying(request.baseCheck)
      val layout = if (request.overlayId == null) full.layout else null
      buildRenderArtifact(
        contentHash = entry.contentHash,
        direction = request.direction,
        screenId = request.screenId,
        overlayId = request.overlayId,
        view = request.view,
        densityScale = request.densityScale,
        artifactName = artName,
        renderMs = output.renderMs,
        check = check,
        layout = layout,
        output = output,
        mainFrame = output.frame,
        endFrame = output.endFrame,
        writeFiles = request.writeFiles,
        rendersRoot = request.rendersRoot,
        fallbackMime = fallbackMime,
      )
    }

  private inline fun <T> withCheckLifecycle(block: () -> T): T =
    try {
      block()
    } finally {
      worker.finishCheck()
    }

  private fun evaluateTargets(
    targets: List<CheckTargetSpec>,
    resultsByKey: Map<TargetKey, WorkerTargetOutput>,
    surfaceSpec: SurfaceSpec,
  ): List<EvaluatedTarget> {
    val baseChecks = HashMap<BaseKey, ViewCheck>()
    // Pre-compute base screen checks so overlays always diff against their base regardless of
    // order.
    for (t in targets) {
      if (t.overlayId != null) continue
      val res = resultsByKey[targetKey(t)]
      val tree = res?.tree
      if (res != null && res.error == null && tree != null) {
        baseChecks[BaseKey(t.direction, t.screenId, t.view.name)] = ViewCheck.of(tree, surfaceSpec)
      }
    }
    val evaluated = ArrayList<EvaluatedTarget>(targets.size)
    for (t in targets) {
      val res = resultsByKey[targetKey(t)]
      val tree = res?.tree
      val check: ViewCheck
      val layout: ViewLayout?
      if (res == null || res.error != null || tree == null) {
        val msg = res?.error ?: "Missing result for ${t.direction}/${t.screenId}/${t.view.name}"
        check = ViewCheck.renderError(RuntimeException(msg))
        layout = null
      } else if (t.overlayId == null) {
        val full =
          baseChecks[BaseKey(t.direction, t.screenId, t.view.name)]
            ?: ViewCheck.of(tree, surfaceSpec)
        check = full
        layout = full.layout
      } else {
        val full = ViewCheck.of(tree, surfaceSpec)
        val base = baseChecks[BaseKey(t.direction, t.screenId, t.view.name)]
        check = if (base == null) full else full.overlaying(base)
        layout = null
      }
      evaluated += EvaluatedTarget(t, res, check, layout)
    }
    return evaluated
  }

  /**
   * Unscoped checks skip frames on non-primary views during the first pass; if any non-primary view
   * fails, fetch up to 3 failing frames so the response can return up to 2 deduplicated failing
   * PNGs.
   */
  private fun fetchMissingFailingFrames(
    entry: CompileEntry,
    evaluated: List<EvaluatedTarget>,
    densityScale: Int,
    rendersRoot: Path,
    framesByKey: MutableMap<FrameKey, WorkerFrame>,
  ) {
    val existingFailingHashes = LinkedHashSet<String>()
    for (ev in evaluated) {
      if (ev.check.pass) continue
      val bytes = framesByKey[frameKey(ev.spec, isEndFrame = false)]?.bytes
      if (bytes != null && bytes.isNotEmpty()) {
        existingFailingHashes += ContentHasher.sha256Hex(bytes)
      }
    }
    if (existingFailingHashes.size >= 2) return

    val missingFailing =
      evaluated
        .filter { ev ->
          !ev.check.pass &&
            ev.output?.error == null &&
            framesByKey[frameKey(ev.spec, isEndFrame = false)] == null
        }
        .take(3)
    if (missingFailing.isEmpty()) return

    val followUpTargets =
      orderTargetsForBatchRender(missingFailing.map { it.spec }).map { spec ->
        DeviceRenderTarget(
          direction = spec.direction,
          screen = spec.screenId,
          viewName = spec.view.name,
          overlayId = spec.overlayId,
          densityScale = densityScale.toFloat(),
          fontScale = 1f,
          frameFormat = FrameFormat.LZ4_RGBA,
          includeEndFrame = true,
        )
      }
    val followUp = worker.renderTargets(entry, followUpTargets, rendersRoot)
    for (r in followUp.targets) {
      storeFrames(r, framesByKey)
    }
  }

  private fun selectTargetsToWrite(
    evaluated: List<EvaluatedTarget>,
    framesByKey: Map<FrameKey, WorkerFrame>,
    scoped: Boolean,
    allPass: Boolean,
    primaryViews: Set<String>,
  ): Set<TargetKey> {
    val writeSet = LinkedHashSet<TargetKey>()
    if (scoped) {
      for (ev in evaluated) writeSet += targetKey(ev.spec)
      return writeSet
    }
    val seenSha = LinkedHashSet<String>()
    val candidates =
      if (!allPass) {
        evaluated.asSequence().filter { !it.check.pass }
      } else {
        evaluated.asSequence().filter {
          it.spec.overlayId == null && it.spec.view.name in primaryViews
        }
      }
    for (ev in candidates) {
      val frame = framesByKey[frameKey(ev.spec, isEndFrame = false)]
      val png = frame?.bytes?.takeIf { frame.isPng && it.isNotEmpty() } ?: continue
      if (!seenSha.add(ContentHasher.sha256Hex(png))) continue
      writeSet += targetKey(ev.spec)
      if (!allPass && writeSet.size >= 2) break
    }
    return writeSet
  }

  private fun buildBatchFailureArtifacts(
    targets: List<CheckTargetSpec>,
    densityScale: Int,
    renderTotalMs: Long,
    error: String?,
  ): List<RenderArtifact> {
    val failedCheck = ViewCheck.renderError(RuntimeException(error ?: "Batch render failed"))
    return targets.map { t ->
      val artName = artifactName(t.prefix, t.overlayId)
      RenderArtifact(
        direction = t.direction,
        screen = t.screenId,
        overlay = t.overlayId,
        view = t.view,
        densityScale = densityScale,
        artifactName = artName,
        pass = false,
        renderMs = renderTotalMs,
        check = failedCheck,
        layout = null,
        tree = null,
        pngPath = null,
        endPngPath = null,
        pngBytes = null,
        endPngBytes = null,
        pngSha256 = null,
        entry = RenderEntry.of(artName, t.view.name, null, failedCheck),
        imageMime = "image/png",
      )
    }
  }

  private fun buildRenderArtifact(
    contentHash: String,
    direction: String,
    screenId: String,
    overlayId: String?,
    view: ViewSpec,
    densityScale: Int,
    artifactName: String,
    renderMs: Long,
    check: ViewCheck,
    layout: ViewLayout?,
    output: WorkerTargetOutput?,
    mainFrame: WorkerFrame?,
    endFrame: WorkerFrame?,
    writeFiles: Boolean,
    rendersRoot: Path,
    fallbackMime: String,
  ): RenderArtifact {
    val mainBytes = mainFrame?.bytes?.takeIf { it.isNotEmpty() }
    val endBytes = endFrame?.bytes?.takeIf { it.isNotEmpty() }
    var pngPath: Path? = null
    var endPngPath: Path? = null
    if (writeFiles && mainFrame != null && mainFrame.isPng && mainBytes != null) {
      val hashDir = rendersRoot.resolve(contentHash.take(12))
      Files.createDirectories(hashDir)
      val baseFile = "${sanitizeFileName(artifactName)}-${view.name}-${densityScale}x"
      pngPath = hashDir.resolve("$baseFile.png").toAbsolutePath().normalize()
      Files.write(pngPath, mainBytes)
      if (endFrame != null && endFrame.isPng && endBytes != null) {
        endPngPath = hashDir.resolve("$baseFile-end.png").toAbsolutePath().normalize()
        Files.write(endPngPath, endBytes)
      }
    }
    val sha = mainBytes?.let(ContentHasher::sha256Hex)
    val baseName = if (pngPath != null) "$artifactName-${view.name}" else null
    val endName = if (endPngPath != null) "$artifactName-${view.name}-end" else null
    return RenderArtifact(
      direction = direction,
      screen = screenId,
      overlay = overlayId,
      view = view,
      densityScale = densityScale,
      artifactName = artifactName,
      pass = check.pass,
      renderMs = renderMs,
      check = check,
      layout = layout,
      tree = output?.tree,
      pngPath = pngPath,
      endPngPath = endPngPath,
      pngBytes = mainBytes,
      endPngBytes = endBytes,
      pngSha256 = sha,
      entry = RenderEntry.of(artifactName, view.name, baseName, check, endName),
      imageMime = mainFrame?.mime ?: fallbackMime,
      endTree = output?.endTree,
    )
  }

  private fun collectBatchOutputs(
    targets: List<WorkerTargetOutput>,
    resultsByKey: MutableMap<TargetKey, WorkerTargetOutput>,
    framesByKey: MutableMap<FrameKey, WorkerFrame>,
  ) {
    for (r in targets) {
      resultsByKey[TargetKey(r.direction, r.screen, r.overlayId, r.viewName)] = r
      storeFrames(r, framesByKey)
    }
  }

  private fun storeFrames(
    output: WorkerTargetOutput,
    framesByKey: MutableMap<FrameKey, WorkerFrame>,
  ) {
    output.frame?.let { f ->
      framesByKey[
        FrameKey(output.direction, output.screen, output.overlayId, output.viewName, false)] = f
    }
    output.endFrame?.let { ef ->
      framesByKey[
        FrameKey(output.direction, output.screen, output.overlayId, output.viewName, true)] = ef
    }
  }

  private fun surfaceSpecFor(surface: BoardSurface): SurfaceSpec =
    if (surface == BoardSurface.Wear) WearSurface else PhoneSurface

  private fun toBoardMeta(devMeta: DeviceBoardMeta): BoardMeta =
    BoardMeta(
      directions =
        devMeta.directions.map { d ->
          DirectionMeta(
            name = d.name,
            surface = d.surface,
            startScreen = d.startScreen,
            screens = d.screens,
            overlays = d.overlays.map { ov -> OverlayMeta(ov.id, ov.screen) },
          )
        },
      views = devMeta.views,
    )

  private fun artifactName(prefix: String, overlayId: String?): String =
    overlayId?.let { ViewCheck.renderOverlayName(prefix, it) } ?: prefix

  private fun targetKey(t: CheckTargetSpec): TargetKey =
    TargetKey(t.direction, t.screenId, t.overlayId, t.view.name)

  private fun frameKey(t: CheckTargetSpec, isEndFrame: Boolean): FrameKey =
    FrameKey(t.direction, t.screenId, t.overlayId, t.view.name, isEndFrame)

  private fun sanitizeFileName(raw: String): String =
    raw.replace(Regex("""[^A-Za-z0-9._+-]+"""), "_").ifBlank { "render" }

  private data class TargetKey(
    val direction: String,
    val screen: String,
    val overlayId: String?,
    val viewName: String,
  )

  private data class BaseKey(
    val direction: String,
    val screen: String,
    val viewName: String,
  )

  private data class FrameKey(
    val direction: String,
    val screen: String,
    val overlayId: String?,
    val viewName: String,
    val isEndFrame: Boolean,
  )

  private data class EvaluatedTarget(
    val spec: CheckTargetSpec,
    val output: WorkerTargetOutput?,
    val check: ViewCheck,
    val layout: ViewLayout?,
  )

  companion object {
    fun primaryCheckViews(surface: BoardSurface): Set<String> =
      if (surface == BoardSurface.Wear) setOf("watch", "watch-large") else setOf("light", "desktop")
  }
}
