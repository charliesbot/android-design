package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.cli.DesignMdLoader
import bot.charlies.androiddesign.preview.app.cli.RenderEntry
import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.domain.layout.LayoutSummary
import bot.charlies.androiddesign.preview.domain.layout.ViewLayout
import bot.charlies.androiddesign.preview.model.BoardSurface
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.PhoneSurface
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.RobolectricRenderRequest
import bot.charlies.androiddesign.preview.model.RobolectricRenderResponse
import bot.charlies.androiddesign.preview.model.RobolectricTargetResult
import bot.charlies.androiddesign.preview.model.SurfaceSpec
import bot.charlies.androiddesign.preview.model.ValidationError
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.WearSurface
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * Headless AndroidX + Robolectric Native Graphics backend backed by one persistent child JVM
 * (`@robolectric.cp` -> `RobolectricRunnerMain`) communicating via single-line JSON over stdio.
 */
class RobolectricPreviewBackend
private constructor(
  private val meta: BootstrapMeta?,
  private val robolectricCp: Path?,
  private val bootstrapDir: Path?,
  private val requestExecutor: ((RobolectricRenderRequest) -> RobolectricRenderResponse)?,
  commandOverride: List<String>?,
) : PreviewBackend {
  constructor(
    meta: BootstrapMeta? = null,
    robolectricCp: Path? = null,
    bootstrapDir: Path? = meta?.bootstrapDir,
    requestExecutor: ((RobolectricRenderRequest) -> RobolectricRenderResponse)? = null,
  ) : this(meta, robolectricCp, bootstrapDir, requestExecutor, null)

  internal constructor(
    workerCommand: List<String>,
  ) : this(null, null, null, null, workerCommand)

  override val name: String = RuntimeIdentity.BACKEND_ROBOLECTRIC
  override val supportsBatchCheck: Boolean = true
  override val supportsHeadlessSpeculativePrecompile: Boolean = false

  private val requestIds = AtomicLong(0L)
  private val processClient =
    when {
      commandOverride != null -> RobolectricProcessClient(commandOverride = commandOverride)
      requestExecutor == null ->
        RobolectricProcessClient(
          robolectricCp =
            robolectricCp
              ?: meta?.bootstrapDir?.resolve("robolectric.cp")
              ?: error("Missing robolectric.cp path for RobolectricPreviewBackend"),
          bootstrapDir =
            bootstrapDir
              ?: meta?.bootstrapDir
              ?: robolectricCp?.toAbsolutePath()?.normalize()?.parent
              ?: error("Missing bootstrap directory for RobolectricPreviewBackend"),
        )
      else -> null
    }

  /** Eagerly starts the child JVM and initializes Robolectric SDK 35 before HTTP checks arrive. */
  fun warmUp() {
    val resp = execRequest(RobolectricRenderRequest(id = nextId(), mode = "ping"))
    check(resp.ok) { resp.error ?: "Robolectric warmup ping failed" }
  }

  override fun compile(snapshot: ContentHasher.Snapshot): BackendCompileOutput {
    val normSource = snapshot.sourcePath.toAbsolutePath().normalize()
    val designRes = DesignMdLoader.load(normSource, snapshot.designPath)
    if (designRes.errors.isNotEmpty()) {
      return failedCompileOutput(
        sourcePath = normSource,
        designPath = designRes.path,
        designTheme = designRes.theme,
        compileMs = 0L,
        errors = designRes.errors,
      )
    }
    val resp =
      execRequest(
        RobolectricRenderRequest(
          id = nextId(),
          sourcePath = normSource.toString(),
          designPath = designRes.path?.toString(),
          mode = "compile",
        ),
      )
    if (!resp.ok) {
      val msg = resp.error ?: "Robolectric compilation failed"
      return failedCompileOutput(
        sourcePath = normSource,
        designPath = designRes.path,
        designTheme = designRes.theme,
        compileMs = resp.compileMs,
        errors =
          resp.errors.ifEmpty { listOf(ValidationError(normSource.fileName.toString(), msg)) },
      )
    }
    val devMeta = resp.boardMeta
    if (!resp.valid || devMeta == null) {
      return failedCompileOutput(
        sourcePath = normSource,
        designPath = designRes.path,
        designTheme = designRes.theme,
        compileMs = resp.compileMs,
        errors = resp.errors,
        boardTheme = resp.boardTheme,
      )
    }

    val boardSurface = if (devMeta.surface == "wear") BoardSurface.Wear else BoardSurface.Phone
    val surfaceSpec = if (boardSurface == BoardSurface.Wear) WearSurface else PhoneSurface
    val designSystem = DesignSystemResolver.resolve(designRes.theme, resp.boardTheme, surfaceSpec)
    val boardMeta =
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
    return BackendCompileOutput(
      loaded =
        LoadedExploration(
          sourcePath = normSource,
          designPath = designRes.path,
          designTheme = designRes.theme,
          designSystem = designSystem,
          compileMs = resp.compileMs,
          errors = emptyList(),
        ),
      dexHash = null,
      surface = boardSurface,
      boardName = devMeta.boardName,
      boardMeta = boardMeta,
      androidData = null,
      boardTheme = resp.boardTheme,
    )
  }

  override fun renderBatchCheck(
    entry: CompileEntry,
    targets: List<CheckTargetSpec>,
    scoped: Boolean,
    rendersRoot: Path,
  ): List<RenderArtifact> {
    val surfaceSpec: SurfaceSpec =
      if (entry.surface == BoardSurface.Wear) WearSurface else PhoneSurface
    val densityScale = if (scoped) 1 else 2
    val primaryViews =
      if (entry.surface == BoardSurface.Wear) setOf("watch", "watch-large")
      else setOf("light", "desktop")

    val deviceTargets = targets.map { t ->
      val wantFrame = scoped || (t.overlayId == null && t.view.name in primaryViews)
      bot.charlies.androiddesign.preview.model.DeviceRenderTarget(
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

    val pngBytesByKey = LinkedHashMap<TargetKey, ByteArray>()
    val endPngBytesByKey = LinkedHashMap<TargetKey, ByteArray>()

    val pass1Resp =
      withTempStageDir(rendersRoot) { stageDir ->
        val r =
          execRequest(
            RobolectricRenderRequest(
              id = nextId(),
              sourcePath = entry.sourcePath.toString(),
              designPath = entry.loaded.designPath?.toString(),
              outDir = stageDir.toString(),
              targets = deviceTargets,
            ),
          )
        collectStagedFrames(r.results, pngBytesByKey, endPngBytesByKey)
        r
      }

    if (!pass1Resp.ok && pass1Resp.results.isEmpty()) {
      val failedCheck =
        ViewCheck.renderError(RuntimeException(pass1Resp.error ?: "Robolectric batch failed"))
      return targets.map { t ->
        val artName = t.overlayId?.let { ViewCheck.renderOverlayName(t.prefix, it) } ?: t.prefix
        RenderArtifact(
          direction = t.direction,
          screen = t.screenId,
          overlay = t.overlayId,
          view = t.view,
          densityScale = densityScale,
          artifactName = artName,
          pass = false,
          renderMs = pass1Resp.renderMs,
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

    val resultsByKey =
      pass1Resp.results.associateBy { r ->
        TargetKey(r.direction, r.screen, r.overlayId, r.viewName)
      }

    val baseChecks = HashMap<BaseKey, ViewCheck>()
    val evaluated = ArrayList<EvaluatedTarget>(targets.size)
    for (t in targets) {
      val res = resultsByKey[TargetKey(t.direction, t.screenId, t.overlayId, t.view.name)]
      val tree = res?.tree
      val check: ViewCheck
      val layout: ViewLayout?
      if (res == null || res.error != null || tree == null) {
        check =
          ViewCheck.renderError(
            RuntimeException(
              res?.error
                ?: "Missing Robolectric result for ${t.direction}/${t.screenId}/${t.view.name}",
            ),
          )
        layout = null
      } else {
        val full = ViewCheck.of(tree, surfaceSpec)
        if (t.overlayId == null) {
          check = full
          baseChecks[BaseKey(t.direction, t.screenId, t.view.name)] = full
          layout = LayoutSummary.of(tree, surfaceSpec)
        } else {
          val base = baseChecks[BaseKey(t.direction, t.screenId, t.view.name)]
          check = if (base == null) full else full.overlaying(base)
          layout = null
        }
      }
      evaluated += EvaluatedTarget(t, res, check, layout)
    }

    val allPass = evaluated.all { it.check.pass }
    if (!scoped && !allPass) {
      val existingFailingHashes = LinkedHashSet<String>()
      for (ev in evaluated) {
        if (ev.check.pass) continue
        val bytes =
          pngBytesByKey[
            TargetKey(ev.spec.direction, ev.spec.screenId, ev.spec.overlayId, ev.spec.view.name)]
        if (bytes != null && bytes.isNotEmpty()) {
          existingFailingHashes += ContentHasher.sha256Hex(bytes)
        }
      }
      if (existingFailingHashes.size < 2) {
        val missingFailing =
          evaluated
            .filter { ev ->
              !ev.check.pass &&
                ev.res?.error == null &&
                pngBytesByKey[
                  TargetKey(
                    ev.spec.direction,
                    ev.spec.screenId,
                    ev.spec.overlayId,
                    ev.spec.view.name,
                  )] == null
            }
            .take(3)
        if (missingFailing.isNotEmpty()) {
          val followUpTargets = missingFailing.map { ev ->
            bot.charlies.androiddesign.preview.model.DeviceRenderTarget(
              direction = ev.spec.direction,
              screen = ev.spec.screenId,
              viewName = ev.spec.view.name,
              overlayId = ev.spec.overlayId,
              densityScale = densityScale.toFloat(),
              fontScale = 1f,
              frameFormat = FrameFormat.LZ4_RGBA,
              includeEndFrame = true,
            )
          }
          withTempStageDir(rendersRoot) { stageDir ->
            val followUpResp =
              execRequest(
                RobolectricRenderRequest(
                  id = nextId(),
                  sourcePath = entry.sourcePath.toString(),
                  designPath = entry.loaded.designPath?.toString(),
                  outDir = stageDir.toString(),
                  targets = followUpTargets,
                ),
              )
            collectStagedFrames(followUpResp.results, pngBytesByKey, endPngBytesByKey)
          }
        }
      }
    }

    val writeSet = LinkedHashSet<TargetKey>()
    val seenWrittenSha = LinkedHashSet<String>()
    if (scoped) {
      for (ev in evaluated) {
        writeSet +=
          TargetKey(ev.spec.direction, ev.spec.screenId, ev.spec.overlayId, ev.spec.view.name)
      }
    } else if (!allPass) {
      for (ev in evaluated) {
        if (ev.check.pass) continue
        val key =
          TargetKey(ev.spec.direction, ev.spec.screenId, ev.spec.overlayId, ev.spec.view.name)
        val png = pngBytesByKey[key]?.takeIf { it.isNotEmpty() } ?: continue
        val sha = ContentHasher.sha256Hex(png)
        if (!seenWrittenSha.add(sha)) continue
        writeSet += key
        if (writeSet.size >= 2) break
      }
    } else {
      for (ev in evaluated) {
        if (ev.spec.overlayId != null || ev.spec.view.name !in primaryViews) continue
        val key =
          TargetKey(ev.spec.direction, ev.spec.screenId, ev.spec.overlayId, ev.spec.view.name)
        val png = pngBytesByKey[key]?.takeIf { it.isNotEmpty() } ?: continue
        val sha = ContentHasher.sha256Hex(png)
        if (!seenWrittenSha.add(sha)) continue
        writeSet += key
      }
    }

    return evaluated.map { ev ->
      val t = ev.spec
      val artName = t.overlayId?.let { ViewCheck.renderOverlayName(t.prefix, it) } ?: t.prefix
      val key = TargetKey(t.direction, t.screenId, t.overlayId, t.view.name)
      val pngBytes = pngBytesByKey[key]
      val endPngBytes = endPngBytesByKey[key]
      var pngPath: Path? = null
      var endPngPath: Path? = null
      if (key in writeSet && pngBytes != null && pngBytes.isNotEmpty()) {
        val hashDir = rendersRoot.resolve(entry.contentHash.take(12))
        Files.createDirectories(hashDir)
        val baseFile = "${sanitizeFileName(artName)}-${t.view.name}-${densityScale}x"
        pngPath = hashDir.resolve("$baseFile.png").toAbsolutePath().normalize()
        Files.write(pngPath, pngBytes)
        if (endPngBytes != null && endPngBytes.isNotEmpty()) {
          endPngPath = hashDir.resolve("$baseFile-end.png").toAbsolutePath().normalize()
          Files.write(endPngPath, endPngBytes)
        }
      }
      val sha = pngBytes?.takeIf { it.isNotEmpty() }?.let(ContentHasher::sha256Hex)
      val baseName = if (pngPath != null) "${artName}-${t.view.name}" else null
      val endName = if (endPngPath != null) "${artName}-${t.view.name}-end" else null
      RenderArtifact(
        direction = t.direction,
        screen = t.screenId,
        overlay = t.overlayId,
        view = t.view,
        densityScale = densityScale,
        artifactName = artName,
        pass = ev.check.pass,
        renderMs = ev.res?.renderMs ?: 0L,
        check = ev.check,
        layout = ev.layout,
        tree = ev.res?.tree,
        pngPath = pngPath,
        endPngPath = endPngPath,
        pngBytes = pngBytes,
        endPngBytes = endPngBytes,
        pngSha256 = sha,
        entry = RenderEntry.of(artName, t.view.name, baseName, ev.check, endName),
        imageMime = "image/png",
        endTree = ev.res?.endTree,
      )
    }
  }

  override fun renderSingle(
    entry: CompileEntry,
    direction: String,
    screenId: String,
    prefix: String,
    overlayId: String?,
    view: ViewSpec,
    densityScale: Int,
    designSystem: DesignSystem,
    baseCheck: ViewCheck?,
    writeFiles: Boolean,
    rendersRoot: Path,
    frameFormat: FrameFormat,
    fontScale: Float,
    seedOverride: String?,
    variantOverride: String?,
    darkOverride: Boolean?,
  ): RenderArtifact {
    val surfaceSpec: SurfaceSpec =
      if (entry.surface == BoardSurface.Wear) WearSurface else PhoneSurface
    val artName = overlayId?.let { ViewCheck.renderOverlayName(prefix, it) } ?: prefix
    val wantFrame = frameFormat != FrameFormat.NONE
    val target =
      bot.charlies.androiddesign.preview.model.DeviceRenderTarget(
        direction = direction,
        screen = screenId,
        viewName = view.name,
        overlayId = overlayId,
        densityScale = densityScale.toFloat(),
        fontScale = fontScale,
        darkOverride = darkOverride,
        seedOverride = seedOverride,
        variantOverride = variantOverride,
        frameFormat = if (wantFrame) FrameFormat.LZ4_RGBA else FrameFormat.NONE,
        includeEndFrame = wantFrame,
      )

    var mainBytes: ByteArray? = null
    var endBytes: ByteArray? = null
    val resp =
      if (wantFrame) {
        withTempStageDir(rendersRoot) { stageDir ->
          val r =
            execRequest(
              RobolectricRenderRequest(
                id = nextId(),
                sourcePath = entry.sourcePath.toString(),
                designPath = entry.loaded.designPath?.toString(),
                outDir = stageDir.toString(),
                targets = listOf(target),
              ),
            )
          val res = r.results.firstOrNull()
          mainBytes = readStagedBytes(res?.pngPath)
          endBytes = readStagedBytes(res?.endPngPath)
          r
        }
      } else {
        execRequest(
          RobolectricRenderRequest(
            id = nextId(),
            sourcePath = entry.sourcePath.toString(),
            designPath = entry.loaded.designPath?.toString(),
            targets = listOf(target),
            encodePng = false,
          ),
        )
      }

    val res = resp.results.firstOrNull()
    val tree = res?.tree
    if (!resp.ok || res == null || res.error != null || tree == null) {
      val failed =
        ViewCheck.renderError(
          RuntimeException(res?.error ?: resp.error ?: "Robolectric render failed"),
        )
      return RenderArtifact(
        direction = direction,
        screen = screenId,
        overlay = overlayId,
        view = view,
        densityScale = densityScale,
        artifactName = artName,
        pass = false,
        renderMs = res?.renderMs ?: resp.renderMs,
        check = failed,
        layout = null,
        tree = null,
        pngPath = null,
        endPngPath = null,
        pngBytes = null,
        endPngBytes = null,
        pngSha256 = null,
        entry = RenderEntry.of(artName, view.name, null, failed),
        imageMime = "image/png",
      )
    }

    val full = ViewCheck.of(tree, surfaceSpec)
    val check = if (baseCheck == null) full else full.overlaying(baseCheck)
    val layout = if (overlayId == null) LayoutSummary.of(tree, surfaceSpec) else null

    var pngPath: Path? = null
    var endPngPath: Path? = null
    val resolvedMain = mainBytes
    val resolvedEnd = endBytes
    if (writeFiles && resolvedMain != null && resolvedMain.isNotEmpty()) {
      val hashDir = rendersRoot.resolve(entry.contentHash.take(12))
      Files.createDirectories(hashDir)
      val baseFile = "${sanitizeFileName(artName)}-${view.name}-${densityScale}x"
      pngPath = hashDir.resolve("$baseFile.png").toAbsolutePath().normalize()
      Files.write(pngPath, resolvedMain)
      if (resolvedEnd != null && resolvedEnd.isNotEmpty()) {
        endPngPath = hashDir.resolve("$baseFile-end.png").toAbsolutePath().normalize()
        Files.write(endPngPath, resolvedEnd)
      }
    }

    val sha = resolvedMain?.takeIf { it.isNotEmpty() }?.let(ContentHasher::sha256Hex)
    val baseName = if (pngPath != null) "${artName}-${view.name}" else null
    val endName = if (endPngPath != null) "${artName}-${view.name}-end" else null
    return RenderArtifact(
      direction = direction,
      screen = screenId,
      overlay = overlayId,
      view = view,
      densityScale = densityScale,
      artifactName = artName,
      pass = check.pass,
      renderMs = res.renderMs,
      check = check,
      layout = layout,
      tree = tree,
      pngPath = pngPath,
      endPngPath = endPngPath,
      pngBytes = mainBytes,
      endPngBytes = endBytes,
      pngSha256 = sha,
      entry = RenderEntry.of(artName, view.name, baseName, check, endName),
      imageMime = "image/png",
      endTree = res.endTree,
    )
  }

  override fun close() {
    processClient?.close()
  }

  private fun execRequest(req: RobolectricRenderRequest): RobolectricRenderResponse =
    requestExecutor?.invoke(req) ?: processClient!!.request(req)

  private fun nextId(): String = "rb-${requestIds.incrementAndGet()}"

  private fun failedCompileOutput(
    sourcePath: Path,
    designPath: Path?,
    designTheme: bot.charlies.androiddesign.preview.model.ThemeOverride?,
    compileMs: Long,
    errors: List<ValidationError>,
    boardTheme: bot.charlies.androiddesign.preview.model.ThemeOverride? = null,
  ): BackendCompileOutput =
    BackendCompileOutput(
      loaded =
        LoadedExploration(
          sourcePath = sourcePath,
          designPath = designPath,
          designTheme = designTheme,
          designSystem = DesignSystemResolver.resolve(designTheme, boardTheme),
          compileMs = compileMs,
          errors = errors,
        ),
      dexHash = null,
      surface = BoardSurface.Phone,
      boardName = null,
      boardMeta = null,
      androidData = null,
      boardTheme = boardTheme,
    )

  private inline fun <T> withTempStageDir(rendersRoot: Path, block: (Path) -> T): T {
    Files.createDirectories(rendersRoot)
    val dir = Files.createTempDirectory(rendersRoot, ".robo-stage-")
    return try {
      block(dir)
    } finally {
      dir.toFile().deleteRecursively()
    }
  }

  private fun collectStagedFrames(
    results: List<RobolectricTargetResult>,
    pngBytes: MutableMap<TargetKey, ByteArray>,
    endPngBytes: MutableMap<TargetKey, ByteArray>,
  ) {
    for (r in results) {
      val key = TargetKey(r.direction, r.screen, r.overlayId, r.viewName)
      readStagedBytes(r.pngPath)?.let { pngBytes[key] = it }
      readStagedBytes(r.endPngPath)?.let { endPngBytes[key] = it }
    }
  }

  private fun readStagedBytes(path: String?): ByteArray? {
    if (path.isNullOrBlank()) return null
    val p = Path.of(path)
    if (!Files.isRegularFile(p)) return null
    return Files.readAllBytes(p)
  }

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

  private data class EvaluatedTarget(
    val spec: CheckTargetSpec,
    val res: RobolectricTargetResult?,
    val check: ViewCheck,
    val layout: ViewLayout?,
  )

  private fun sanitizeFileName(raw: String): String =
    raw.replace(Regex("""[^A-Za-z0-9._+-]+"""), "_").ifBlank { "render" }

  companion object {
    const val DEFAULT_COLD_BUDGET_MS = 60_000L
    const val DEFAULT_WARM_BUDGET_MS = 30_000L
    private const val ROBOLECTRIC_MAIN =
      "bot.charlies.androiddesign.preview.host.robolectric.RobolectricRunnerMain"
  }
}

private class RobolectricProcessClient(
  private val robolectricCp: Path? = null,
  private val bootstrapDir: Path? = null,
  private val commandOverride: List<String>? = null,
) : AutoCloseable {
  private val requestLock = Any()
  private val stateLock = Any()
  private var process: Process? = null
  private var writer: BufferedWriter? = null
  private var reader: BufferedReader? = null
  private var closed = false
  private val stderrTail = ArrayDeque<String>(64)
  private val stderrLock = Any()

  private data class ProcessHandles(
    val process: Process?,
    val writer: BufferedWriter?,
    val reader: BufferedReader?,
  )

  fun request(req: RobolectricRenderRequest): RobolectricRenderResponse {
    synchronized(requestLock) {
      val (proc, w, r) = ensureStarted()
      val payload = PreviewJson.compact.encodeToString(RobolectricRenderRequest.serializer(), req)
      try {
        w.write(payload)
        w.newLine()
        w.flush()
        while (true) {
          val line = r.readLine()
          if (line == null) {
            val wasClosed = synchronized(stateLock) { closed }
            val detached = synchronized(stateLock) { detachIfCurrentLocked(proc) }
            terminateDetached(detached, forceFirst = wasClosed)
            runCatching { w.close() }
            runCatching { r.close() }
            if (wasClosed) {
              throw IllegalStateException("RobolectricProcessClient is closed")
            }
            val exit = runCatching { proc.exitValue() }.getOrNull()
            val tail = recentStderr()
            throw IllegalStateException(
              "Robolectric worker exited unexpectedly (code=${exit ?: "?"}): $tail",
            )
          }
          val trimmed = line.trim()
          if (trimmed.isEmpty()) continue
          return PreviewJson.compact.decodeFromString(
            RobolectricRenderResponse.serializer(),
            trimmed,
          )
        }
      } catch (e: Exception) {
        val wasClosed = synchronized(stateLock) { closed }
        val detached = synchronized(stateLock) { detachIfCurrentLocked(proc) }
        terminateDetached(detached, forceFirst = wasClosed)
        runCatching { w.close() }
        runCatching { r.close() }
        if (wasClosed) {
          throw IllegalStateException("RobolectricProcessClient is closed", e)
        }
        if (e is IllegalStateException && detached.process == null) throw e
        val tail = recentStderr()
        throw IllegalStateException(
          "Robolectric worker request failed: ${e.message}${if (tail.isNotBlank()) "\nstderr:\n$tail" else ""}",
          e,
        )
      }
    }
  }

  override fun close() {
    val detached =
      synchronized(stateLock) {
        if (closed) return
        closed = true
        detachAllLocked()
      }
    terminateDetached(detached, forceFirst = true)
  }

  private fun ensureStarted(): Triple<Process, BufferedWriter, BufferedReader> {
    synchronized(stateLock) {
      check(!closed) { "RobolectricProcessClient is closed" }
      val p = process
      val w = writer
      val r = reader
      if (p != null && p.isAlive && w != null && r != null) {
        return Triple(p, w, r)
      }
    }
    val stale = synchronized(stateLock) { detachAllLocked() }
    terminateDetached(stale, forceFirst = false)

    val cmd =
      commandOverride
        ?: run {
          val rcp = checkNotNull(robolectricCp)
          val bdir = checkNotNull(bootstrapDir)
          require(Files.isRegularFile(rcp)) {
            "Missing robolectric.cp at $rcp; run `./gradlew :host:exportBootstrap` from app/"
          }
          listOf(
            resolveJavaBinary(),
            "-Xmx3072m",
            "-Djava.awt.headless=true",
            "-Dadp.bootstrap.dir=${bdir.toAbsolutePath().normalize()}",
            "@${rcp.toAbsolutePath().normalize()}",
            "bot.charlies.androiddesign.preview.host.robolectric.RobolectricRunnerMain",
          )
        }
    synchronized(stderrLock) { stderrTail.clear() }
    val proc = ProcessBuilder(cmd).start()
    val w = BufferedWriter(OutputStreamWriter(proc.outputStream, StandardCharsets.UTF_8))
    val r = BufferedReader(InputStreamReader(proc.inputStream, StandardCharsets.UTF_8))
    val errStream = proc.errorStream

    val installed =
      synchronized(stateLock) {
        if (closed) {
          false
        } else {
          process = proc
          writer = w
          reader = r
          true
        }
      }
    if (!installed) {
      terminateDetached(ProcessHandles(proc, w, r), forceFirst = true)
      runCatching { errStream.close() }
      throw IllegalStateException("RobolectricProcessClient is closed")
    }

    Thread(
        {
          try {
            BufferedReader(InputStreamReader(errStream, StandardCharsets.UTF_8)).use { errReader ->
              while (true) {
                val line = errReader.readLine() ?: break
                synchronized(stderrLock) {
                  if (stderrTail.size >= 64) stderrTail.removeFirst()
                  stderrTail.addLast(line)
                }
              }
            }
          } catch (_: Exception) {
            // Stream closes when worker exits.
          }
        },
        "robolectric-worker-stderr",
      )
      .apply {
        isDaemon = true
        start()
      }
    return Triple(proc, w, r)
  }

  private fun recentStderr(): String = synchronized(stderrLock) { stderrTail.joinToString("\n") }

  private fun detachAllLocked(): ProcessHandles {
    val res = ProcessHandles(process, writer, reader)
    process = null
    writer = null
    reader = null
    return res
  }

  private fun detachIfCurrentLocked(expected: Process): ProcessHandles {
    if (process !== expected) return ProcessHandles(null, null, null)
    return detachAllLocked()
  }

  private fun terminateDetached(handles: ProcessHandles, forceFirst: Boolean) {
    val p = handles.process
    if (forceFirst) {
      // Never call BufferedReader.close()/BufferedWriter.close() from an external close() thread:
      // Java's BufferedReader synchronizes close() on the same internal lock held by readLine().
      if (p != null) {
        runCatching { p.descendants().forEach { it.destroyForcibly() } }
        runCatching { p.destroyForcibly() }
        runCatching { p.outputStream.close() }
        runCatching { p.inputStream.close() }
        runCatching { p.errorStream.close() }
        runCatching { p.waitFor(2, TimeUnit.SECONDS) }
      }
      return
    }
    if (p != null) {
      runCatching { p.outputStream.close() }
      runCatching {
        if (!p.waitFor(2, TimeUnit.SECONDS)) {
          p.descendants().forEach { it.destroyForcibly() }
          p.destroyForcibly()
          p.waitFor(2, TimeUnit.SECONDS)
        }
      }
      if (p.isAlive) {
        runCatching { p.descendants().forEach { it.destroyForcibly() } }
        runCatching { p.destroyForcibly() }
      }
      runCatching { p.inputStream.close() }
      runCatching { p.errorStream.close() }
    }
    runCatching { handles.writer?.close() }
    runCatching { handles.reader?.close() }
  }

  private fun resolveJavaBinary(): String {
    val override = System.getenv("ANDROID_DESIGN_PREVIEW_JAVA")?.takeIf { it.isNotBlank() }
    if (override != null && Files.isExecutable(Path.of(override))) return override
    val home = System.getProperty("java.home")
    if (!home.isNullOrBlank()) {
      val unix = Path.of(home, "bin", "java")
      if (Files.isExecutable(unix)) return unix.toString()
      val win = Path.of(home, "bin", "java.exe")
      if (Files.isRegularFile(win)) return win.toString()
    }
    return "java"
  }
}
