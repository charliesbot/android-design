package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.android.AndroidCompileOutcome
import bot.charlies.androiddesign.preview.app.android.AndroidExplorationCompiler
import bot.charlies.androiddesign.preview.app.android.DeviceBatchExecution
import bot.charlies.androiddesign.preview.app.android.DeviceTransport
import bot.charlies.androiddesign.preview.app.android.FrameKey
import bot.charlies.androiddesign.preview.app.cli.RenderEntry
import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.domain.layout.LayoutSummary
import bot.charlies.androiddesign.preview.model.BoardSurface
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.DeviceBatchRequest
import bot.charlies.androiddesign.preview.model.DeviceLiveStateRequest
import bot.charlies.androiddesign.preview.model.DeviceRenderTarget
import bot.charlies.androiddesign.preview.model.DeviceTargetResult
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.PhoneSurface
import bot.charlies.androiddesign.preview.model.SurfaceSpec
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.ValidationError
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.WearSurface
import java.nio.file.Files
import java.nio.file.Path

/** Exploration compile inputs, resolved design system, and any validation/compiler errors. */
data class LoadedExploration(
  val sourcePath: Path,
  val designPath: Path?,
  val designTheme: ThemeOverride?,
  val designSystem: DesignSystem,
  val compileMs: Long,
  val errors: List<ValidationError>,
)

/**
 * Android-compiled artifacts cached per `(repoRoot, sourcePath, contentHash)` on the host server.
 */
data class AndroidCompiledData(
  val sourceFile: String,
  val sourceText: String,
  val entryClass: String,
  val dexBytes: ByteArray,
  val dexHash: String,
  val assetMap: Map<String, String>,
  val assetBlobs: Map<String, ByteArray>,
  val boardTheme: ThemeOverride?,
)

/** One target screen/overlay/view tuple planned for a check or board render. */
data class CheckTargetSpec(
  val direction: String,
  val screenId: String,
  val prefix: String,
  val overlayId: String?,
  val view: ViewSpec,
)

data class BackendCompileOutput(
  val loaded: LoadedExploration,
  val dexHash: String?,
  val surface: BoardSurface,
  val boardName: String?,
  val boardMeta: BoardMeta?,
  val androidData: AndroidCompiledData?,
  val boardTheme: ThemeOverride? = null,
)

class SandboxWatchdogException(message: String) : RuntimeException(message)

interface PreviewBackend : AutoCloseable {
  val name: String
  val supportsBatchCheck: Boolean
  val supportsHeadlessSpeculativePrecompile: Boolean

  fun compile(snapshot: ContentHasher.Snapshot): BackendCompileOutput

  fun renderBatchCheck(
    entry: CompileEntry,
    targets: List<CheckTargetSpec>,
    scoped: Boolean,
    rendersRoot: Path,
  ): List<RenderArtifact>

  fun renderSingle(
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
    fontScale: Float = 1f,
    seedOverride: String? = null,
    variantOverride: String? = null,
    darkOverride: Boolean? = null,
  ): RenderArtifact

  fun updateLive(
    entry: CompileEntry,
    direction: String?,
    screenId: String?,
    overlayId: String?,
    darkOverride: Boolean? = null,
    seedOverride: String? = null,
    variantOverride: String? = null,
    fontScale: Float = 1f,
  ) {}

  override fun close() {}
}

/**
 * Android Host APK backend (`kotlinc + D8` in RAM, persistent `adb forward` `:sandbox` bridge,
 * host-side `Deflater.BEST_SPEED` PNG encoding for selected frames, and `WEBP_LOSSLESS` browser
 * cards).
 */
class AndroidPreviewBackend(
  private val meta: BootstrapMeta,
  private val transport: DeviceTransport,
  private val onRecycleRecommended: (String?) -> Unit = {},
  private val batchTimeoutMs: Long = DEFAULT_BATCH_TIMEOUT_MS,
) : PreviewBackend {
  override val name: String = RuntimeIdentity.BACKEND_ANDROID
  override val supportsBatchCheck: Boolean = true
  override val supportsHeadlessSpeculativePrecompile: Boolean = true

  override fun compile(snapshot: ContentHasher.Snapshot): BackendCompileOutput {
    return when (
      val outcome =
        AndroidExplorationCompiler.compile(snapshot.sourcePath, snapshot.designPath, meta)
    ) {
      is AndroidCompileOutcome.Failed -> {
        val fallbackSystem = DesignSystemResolver.resolve(outcome.designTheme, null)
        BackendCompileOutput(
          loaded =
            LoadedExploration(
              sourcePath = snapshot.sourcePath,
              designPath = outcome.designPath,
              designTheme = outcome.designTheme,
              designSystem = fallbackSystem,
              compileMs = outcome.compileMs,
              errors = outcome.errors,
            ),
          dexHash = null,
          surface = BoardSurface.Phone,
          boardName = null,
          boardMeta = null,
          androidData = null,
        )
      }
      is AndroidCompileOutcome.Success -> {
        val compiled = outcome.compiled
        val preload = transport.preloadDex(compiled.dexHash, compiled.entryClass, compiled.dexBytes)
        if (!preload.ok || preload.boardMeta == null) {
          val errMsg = preload.error ?: "Host APK PRELOAD_DEX returned no board metadata"
          val fallbackSystem = DesignSystemResolver.resolve(compiled.designTheme, null)
          return BackendCompileOutput(
            loaded =
              LoadedExploration(
                sourcePath = compiled.sourcePath,
                designPath = compiled.designPath,
                designTheme = compiled.designTheme,
                designSystem = fallbackSystem,
                compileMs = compiled.compileMs,
                errors = listOf(ValidationError(compiled.sourceFile, errMsg)),
              ),
            dexHash = compiled.dexHash,
            surface = BoardSurface.Phone,
            boardName = null,
            boardMeta = null,
            androidData = null,
          )
        }
        transport.syncAssets(compiled.assetBlobs)

        val devMeta = preload.boardMeta!!
        val boardSurface = if (devMeta.surface == "wear") BoardSurface.Wear else BoardSurface.Phone
        val surfaceSpec = if (boardSurface == BoardSurface.Wear) WearSurface else PhoneSurface
        val designSystem =
          DesignSystemResolver.resolve(compiled.designTheme, preload.boardTheme, surfaceSpec)
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
        val androidData =
          AndroidCompiledData(
            sourceFile = compiled.sourceFile,
            sourceText = compiled.sourceText,
            entryClass = compiled.entryClass,
            dexBytes = compiled.dexBytes,
            dexHash = compiled.dexHash,
            assetMap = compiled.assetMap,
            assetBlobs = compiled.assetBlobs,
            boardTheme = preload.boardTheme,
          )
        BackendCompileOutput(
          loaded =
            LoadedExploration(
              sourcePath = compiled.sourcePath,
              designPath = compiled.designPath,
              designTheme = compiled.designTheme,
              designSystem = designSystem,
              compileMs = compiled.compileMs,
              errors = emptyList(),
            ),
          dexHash = compiled.dexHash,
          surface = boardSurface,
          boardName = devMeta.boardName,
          boardMeta = boardMeta,
          androidData = androidData,
          boardTheme = preload.boardTheme,
        )
      }
    }
  }

  override fun renderBatchCheck(
    entry: CompileEntry,
    targets: List<CheckTargetSpec>,
    scoped: Boolean,
    rendersRoot: Path,
  ): List<RenderArtifact> {
    val data =
      checkNotNull(entry.androidData) { "Missing AndroidCompiledData for ${entry.sourcePath}" }
    val surface = entry.surface
    val surfaceSpec: SurfaceSpec = if (surface == BoardSurface.Wear) WearSurface else PhoneSurface
    val primaryViews =
      if (surface == BoardSurface.Wear) setOf("watch", "watch-large") else setOf("light", "desktop")

    val deviceTargets = targets.map { t ->
      val wantFrame = scoped || (t.overlayId == null && t.view.name in primaryViews)
      DeviceRenderTarget(
        direction = t.direction,
        screen = t.screenId,
        viewName = t.view.name,
        overlayId = t.overlayId,
        densityScale = 2f,
        fontScale = 1f,
        frameFormat = if (wantFrame) FrameFormat.LZ4_RGBA else FrameFormat.NONE,
        includeEndFrame = wantFrame,
      )
    }

    val exec = executeBatchWithAssetSync(data, entry.loaded.designTheme, deviceTargets)
    val resp = exec.response
    if (resp.recycleReason == "watchdog_timeout") {
      throw SandboxWatchdogException(resp.error ?: "Batch render timed out on :sandbox")
    }
    if (!resp.ok && resp.results.isEmpty()) {
      val failedCheck = ViewCheck.renderError(RuntimeException(resp.error ?: "Device batch failed"))
      return targets.map { t ->
        val artName = t.overlayId?.let { ViewCheck.renderOverlayName(t.prefix, it) } ?: t.prefix
        RenderArtifact(
          direction = t.direction,
          screen = t.screenId,
          overlay = t.overlayId,
          view = t.view,
          densityScale = 2,
          artifactName = artName,
          pass = false,
          renderMs = resp.renderTotalMs.toLong(),
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

    val resultsByTargetKey =
      exec.response.results.associateBy { r ->
        TargetKey(r.direction, r.screen, r.overlayId, r.viewName)
      }
    val frames = LinkedHashMap(exec.frames)

    // Evaluate all FindingRules and LayoutSummaries in memory across every target.
    val baseChecks = HashMap<BaseKey, ViewCheck>()
    val evaluated = ArrayList<EvaluatedTarget>(targets.size)
    for (t in targets) {
      val res = resultsByTargetKey[TargetKey(t.direction, t.screenId, t.overlayId, t.view.name)]
      val check: ViewCheck
      val layout: bot.charlies.androiddesign.preview.domain.layout.ViewLayout?
      if (res == null || res.error != null) {
        check =
          ViewCheck.renderError(
            RuntimeException(
              res?.error ?: "Missing result for ${t.direction}/${t.screenId}/${t.view.name}",
            ),
          )
        layout = null
      } else {
        val full = ViewCheck.of(res.tree, surfaceSpec)
        if (t.overlayId == null) {
          check = full
          baseChecks[BaseKey(t.direction, t.screenId, t.view.name)] = full
          layout = LayoutSummary.of(res.tree, surfaceSpec)
        } else {
          val base = baseChecks[BaseKey(t.direction, t.screenId, t.view.name)]
          check = if (base == null) full else full.overlaying(base)
          layout = null
        }
      }
      evaluated += EvaluatedTarget(t, res, check, layout)
    }

    val allPass = evaluated.all { it.check.pass }
    // For unscoped failures where failing targets had FrameFormat.NONE, fetch up to 3 failing LZ4
    // frames so we still return up to 2 deduplicated failing host PNGs.
    if (!scoped && !allPass) {
      val existingFailingHashes = LinkedHashSet<String>()
      for (ev in evaluated) {
        if (ev.check.pass) continue
        val f =
          frames[
            FrameKey(
              ev.spec.direction,
              ev.spec.screenId,
              ev.spec.overlayId,
              ev.spec.view.name,
              false,
            )]
        val bytes = f?.pngBytes
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
                frames[
                  FrameKey(
                    ev.spec.direction,
                    ev.spec.screenId,
                    ev.spec.overlayId,
                    ev.spec.view.name,
                    false,
                  )] == null
            }
            .take(3)
        if (missingFailing.isNotEmpty()) {
          val followUpTargets = missingFailing.map { ev ->
            DeviceRenderTarget(
              direction = ev.spec.direction,
              screen = ev.spec.screenId,
              viewName = ev.spec.view.name,
              overlayId = ev.spec.overlayId,
              densityScale = 2f,
              fontScale = 1f,
              frameFormat = FrameFormat.LZ4_RGBA,
              includeEndFrame = true,
            )
          }
          val followUp = executeBatchWithAssetSync(data, entry.loaded.designTheme, followUpTargets)
          frames.putAll(followUp.frames)
        }
      }
    }

    // Decide which targets actually get written as host PNG files on disk.
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
        val f =
          frames[
            FrameKey(
              ev.spec.direction,
              ev.spec.screenId,
              ev.spec.overlayId,
              ev.spec.view.name,
              false,
            )]
        val png = f?.pngBytes?.takeIf { it.isNotEmpty() } ?: continue
        val sha = ContentHasher.sha256Hex(png)
        if (!seenWrittenSha.add(sha)) continue
        writeSet +=
          TargetKey(ev.spec.direction, ev.spec.screenId, ev.spec.overlayId, ev.spec.view.name)
        if (writeSet.size >= 2) break
      }
    } else {
      for (ev in evaluated) {
        if (ev.spec.overlayId != null || ev.spec.view.name !in primaryViews) continue
        val f =
          frames[
            FrameKey(
              ev.spec.direction,
              ev.spec.screenId,
              ev.spec.overlayId,
              ev.spec.view.name,
              false,
            )]
        val png = f?.pngBytes?.takeIf { it.isNotEmpty() } ?: continue
        val sha = ContentHasher.sha256Hex(png)
        if (!seenWrittenSha.add(sha)) continue
        writeSet +=
          TargetKey(ev.spec.direction, ev.spec.screenId, ev.spec.overlayId, ev.spec.view.name)
      }
    }

    val artifacts = evaluated.map { ev ->
      val t = ev.spec
      val artName = t.overlayId?.let { ViewCheck.renderOverlayName(t.prefix, it) } ?: t.prefix
      val k = TargetKey(t.direction, t.screenId, t.overlayId, t.view.name)
      val mainFrame = frames[FrameKey(t.direction, t.screenId, t.overlayId, t.view.name, false)]
      val endFrame = frames[FrameKey(t.direction, t.screenId, t.overlayId, t.view.name, true)]
      val pngBytes = mainFrame?.pngBytes
      val endPngBytes = endFrame?.pngBytes
      var pngPath: Path? = null
      var endPngPath: Path? = null
      if (k in writeSet && pngBytes != null && pngBytes.isNotEmpty()) {
        val hashDir = rendersRoot.resolve(entry.contentHash.take(12))
        Files.createDirectories(hashDir)
        val baseFile = "${sanitizeFileName(artName)}-${t.view.name}-2x"
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
        densityScale = 2,
        artifactName = artName,
        pass = ev.check.pass,
        renderMs = ev.res?.renderMs?.toLong() ?: 0L,
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
      )
    }

    if (resp.recycleRecommended) {
      onRecycleRecommended(resp.recycleReason)
    }
    return artifacts
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
    val data =
      checkNotNull(entry.androidData) { "Missing AndroidCompiledData for ${entry.sourcePath}" }
    val surfaceSpec: SurfaceSpec =
      if (entry.surface == BoardSurface.Wear) WearSurface else PhoneSurface
    val effectiveFormat =
      if (writeFiles && frameFormat == FrameFormat.WEBP_LOSSLESS) FrameFormat.LZ4_RGBA
      else frameFormat
    val target =
      DeviceRenderTarget(
        direction = direction,
        screen = screenId,
        viewName = view.name,
        overlayId = overlayId,
        densityScale = densityScale.toFloat(),
        fontScale = fontScale,
        darkOverride = darkOverride,
        seedOverride = seedOverride,
        variantOverride = variantOverride,
        frameFormat = effectiveFormat,
        includeEndFrame = effectiveFormat != FrameFormat.NONE,
      )
    val exec = executeBatchWithAssetSync(data, entry.loaded.designTheme, listOf(target))
    val resp = exec.response
    if (resp.recycleReason == "watchdog_timeout") {
      throw SandboxWatchdogException(resp.error ?: "Render timed out on :sandbox")
    }
    val artName = overlayId?.let { ViewCheck.renderOverlayName(prefix, it) } ?: prefix
    val res = resp.results.firstOrNull()
    if (!resp.ok && res == null) {
      val failed = ViewCheck.renderError(RuntimeException(resp.error ?: "Device render failed"))
      return RenderArtifact(
        direction = direction,
        screen = screenId,
        overlay = overlayId,
        view = view,
        densityScale = densityScale,
        artifactName = artName,
        pass = false,
        renderMs = resp.renderTotalMs.toLong(),
        check = failed,
        layout = null,
        tree = null,
        pngPath = null,
        endPngPath = null,
        pngBytes = null,
        endPngBytes = null,
        pngSha256 = null,
        entry = RenderEntry.of(artName, view.name, null, failed),
        imageMime = if (effectiveFormat == FrameFormat.WEBP_LOSSLESS) "image/webp" else "image/png",
      )
    }

    val targetRes = checkNotNull(res)
    val check =
      if (targetRes.error != null) {
        ViewCheck.renderError(RuntimeException(targetRes.error))
      } else {
        val full = ViewCheck.of(targetRes.tree, surfaceSpec)
        if (baseCheck == null) full else full.overlaying(baseCheck)
      }
    val layout =
      if (overlayId == null && targetRes.error == null)
        LayoutSummary.of(targetRes.tree, surfaceSpec)
      else null
    val mainFrame = exec.frames[FrameKey(direction, screenId, overlayId, view.name, false)]
    val endFrame = exec.frames[FrameKey(direction, screenId, overlayId, view.name, true)]
    val mainBytes = mainFrame?.imageBytes?.takeIf { it.isNotEmpty() }
    val endBytes = endFrame?.imageBytes?.takeIf { it.isNotEmpty() }

    var pngPath: Path? = null
    var endPngPath: Path? = null
    if (writeFiles && mainFrame?.pngBytes != null && mainFrame.pngBytes.isNotEmpty()) {
      val hashDir = rendersRoot.resolve(entry.contentHash.take(12))
      Files.createDirectories(hashDir)
      val baseFile = "${sanitizeFileName(artName)}-${view.name}-${densityScale}x"
      pngPath = hashDir.resolve("$baseFile.png").toAbsolutePath().normalize()
      Files.write(pngPath, mainFrame.pngBytes)
      endFrame
        ?.pngBytes
        ?.takeIf { it.isNotEmpty() }
        ?.let { endPng ->
          endPngPath = hashDir.resolve("$baseFile-end.png").toAbsolutePath().normalize()
          Files.write(endPngPath, endPng)
        }
    }

    val sha = mainBytes?.let(ContentHasher::sha256Hex)
    val baseName = if (pngPath != null) "${artName}-${view.name}" else null
    val endName = if (endPngPath != null) "${artName}-${view.name}-end" else null
    if (resp.recycleRecommended) {
      onRecycleRecommended(resp.recycleReason)
    }
    return RenderArtifact(
      direction = direction,
      screen = screenId,
      overlay = overlayId,
      view = view,
      densityScale = densityScale,
      artifactName = artName,
      pass = check.pass,
      renderMs = targetRes.renderMs.toLong(),
      check = check,
      layout = layout,
      tree = targetRes.tree,
      pngPath = pngPath,
      endPngPath = endPngPath,
      pngBytes = mainBytes,
      endPngBytes = endBytes,
      pngSha256 = sha,
      entry = RenderEntry.of(artName, view.name, baseName, check, endName),
      imageMime =
        mainFrame?.imageMime
          ?: if (effectiveFormat == FrameFormat.WEBP_LOSSLESS) "image/webp" else "image/png",
    )
  }

  override fun updateLive(
    entry: CompileEntry,
    direction: String?,
    screenId: String?,
    overlayId: String?,
    darkOverride: Boolean?,
    seedOverride: String?,
    variantOverride: String?,
    fontScale: Float,
  ) {
    val data = entry.androidData ?: return
    transport.syncAssets(data.assetBlobs)
    transport.setLive(
      dexBytes = data.dexBytes,
      request =
        DeviceLiveStateRequest(
          dexHash = data.dexHash,
          entryClass = data.entryClass,
          sourceFile = data.sourceFile,
          designTheme = entry.loaded.designTheme,
          assetMap = data.assetMap,
          direction = direction,
          screen = screenId,
          overlayId = overlayId,
          darkOverride = darkOverride,
          seedOverride = seedOverride,
          variantOverride = variantOverride,
          fontScale = fontScale,
        ),
    )
  }

  override fun close() {
    transport.close()
  }

  private fun executeBatchWithAssetSync(
    data: AndroidCompiledData,
    designTheme: ThemeOverride?,
    targets: List<DeviceRenderTarget>,
  ): DeviceBatchExecution {
    transport.syncAssets(data.assetBlobs)
    return transport.renderBatch(
      dexHash = data.dexHash,
      entryClass = data.entryClass,
      dexBytes = data.dexBytes,
      request =
        DeviceBatchRequest(
          dexHash = data.dexHash,
          entryClass = data.entryClass,
          sourceFile = data.sourceFile,
          sourceText = data.sourceText,
          designTheme = designTheme,
          assetMap = data.assetMap,
          targets = targets,
          timeoutMs = batchTimeoutMs,
        ),
    )
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
    val res: DeviceTargetResult?,
    val check: ViewCheck,
    val layout: bot.charlies.androiddesign.preview.domain.layout.ViewLayout?,
  )

  private fun sanitizeFileName(raw: String): String =
    raw.replace(Regex("""[^A-Za-z0-9._+-]+"""), "_").ifBlank { "render" }

  companion object {
    private const val DEFAULT_BATCH_TIMEOUT_MS = 15_000L
  }
}

internal fun DesignSystemResolver.resolveWithOverrides(
  entry: CompileEntry,
  seedOverride: String? = null,
  variantOverride: PaletteVariant? = null,
): DesignSystem {
  val android = entry.androidData
  val baseTheme = entry.boardTheme ?: android?.boardTheme
  val surfaceSpec = if (entry.surface == BoardSurface.Wear) WearSurface else PhoneSurface
  val overriddenBoard =
    if (seedOverride == null && variantOverride == null) {
      baseTheme
    } else {
      val currentScheme = baseTheme?.material?.scheme
      val nextScheme =
        bot.charlies.androiddesign.preview.model.SchemeOverride(
          seed = seedOverride ?: currentScheme?.seed,
          variant = variantOverride?.wire ?: currentScheme?.variant,
          contrast = currentScheme?.contrast,
        )
      val nextColors =
        if (seedOverride != null) (baseTheme?.colors.orEmpty() + ("primary" to seedOverride))
        else baseTheme?.colors.orEmpty()
      (baseTheme ?: ThemeOverride()).copy(
        colors = nextColors,
        material =
          (baseTheme?.material ?: bot.charlies.androiddesign.preview.model.MaterialOverride()).copy(
            scheme = nextScheme,
          ),
      )
    }
  return resolve(entry.loaded.designTheme, overriddenBoard, surfaceSpec)
}
