package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.android.AndroidCompileOutcome
import bot.charlies.androiddesign.preview.app.android.AndroidExplorationCompiler
import bot.charlies.androiddesign.preview.app.android.DecodedDeviceFrame
import bot.charlies.androiddesign.preview.app.android.DeviceBatchExecution
import bot.charlies.androiddesign.preview.app.android.DeviceTransport
import bot.charlies.androiddesign.preview.app.android.FrameKey
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.model.DeviceBatchRequest
import bot.charlies.androiddesign.preview.model.DeviceLiveStateRequest
import bot.charlies.androiddesign.preview.model.DeviceRenderTarget
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.ValidationError
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference

/**
 * Android Host APK worker (`kotlinc + D8` in RAM, persistent `adb forward` `:sandbox` bridge,
 * host-side `Deflater.BEST_SPEED` PNG decoding for selected frames, and `WEBP_LOSSLESS` browser
 * cards).
 */
class AndroidRenderWorker(
  private val meta: BootstrapMeta,
  private val transport: DeviceTransport,
  private val onRecycleRecommended: (String?) -> Unit = {},
  private val batchTimeoutMs: Long = DEFAULT_BATCH_TIMEOUT_MS,
) : RenderWorker {
  override val name: String = RuntimeIdentity.BACKEND_ANDROID
  override val supportsHeadlessSpeculativePrecompile: Boolean = true
  override val scopedCheckDensity: Int = 2

  private data class PendingRecycle(val reason: String?)

  private val pendingRecycle = AtomicReference<PendingRecycle?>(null)

  override fun compile(snapshot: ContentHasher.Snapshot): WorkerCompileResult =
    when (
      val outcome =
        AndroidExplorationCompiler.compile(
          sourcePath = snapshot.sourcePath,
          explicitDesign = snapshot.designPath,
          meta = meta,
          snapshot = snapshot,
        )
    ) {
      is AndroidCompileOutcome.Failed ->
        WorkerCompileResult.Failed(
          sourcePath = snapshot.sourcePath,
          designPath = outcome.designPath,
          designTheme = outcome.designTheme,
          compileMs = outcome.compileMs,
          errors = outcome.errors,
        )
      is AndroidCompileOutcome.Success -> {
        val compiled = outcome.compiled
        val preload = transport.preloadDex(compiled.dexHash, compiled.entryClass, compiled.dexBytes)
        val boardMeta = preload.boardMeta
        if (!preload.ok || boardMeta == null) {
          val errMsg = preload.error ?: "Host APK PRELOAD_DEX returned no board metadata"
          WorkerCompileResult.Failed(
            sourcePath = compiled.sourcePath,
            designPath = compiled.designPath,
            designTheme = compiled.designTheme,
            compileMs = compiled.compileMs,
            errors = listOf(ValidationError(compiled.sourceFile, errMsg)),
            dexHash = compiled.dexHash,
          )
        } else {
          if (compiled.assetBlobs.isNotEmpty()) {
            transport.syncAssets(compiled.assetBlobs)
          }
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
          WorkerCompileResult.Ready(
            sourcePath = compiled.sourcePath,
            designPath = compiled.designPath,
            designTheme = compiled.designTheme,
            compileMs = compiled.compileMs,
            boardMeta = boardMeta,
            boardTheme = preload.boardTheme,
            dexHash = compiled.dexHash,
            androidData = androidData,
          )
        }
      }
    }

  override fun renderTargets(
    entry: CompileEntry,
    targets: List<DeviceRenderTarget>,
    stageRoot: Path,
  ): WorkerBatchResult {
    val data =
      checkNotNull(entry.androidData) { "Missing AndroidCompiledData for ${entry.sourcePath}" }
    val exec = executeBatchWithAssetSync(data, entry.loaded.designTheme, targets)
    val resp = exec.response
    if (resp.recycleReason == "watchdog_timeout") {
      pendingRecycle.set(null)
      throw SandboxWatchdogException(resp.error ?: "Render timed out on :sandbox")
    }
    if (resp.recycleRecommended) {
      pendingRecycle.set(PendingRecycle(resp.recycleReason))
    }
    val outputs =
      resp.results.map { res ->
        val mainFrame =
          exec.frames[FrameKey(res.direction, res.screen, res.overlayId, res.viewName, false)]
            ?.toWorkerFrame()
        val endFrame =
          exec.frames[FrameKey(res.direction, res.screen, res.overlayId, res.viewName, true)]
            ?.toWorkerFrame()
        WorkerTargetOutput(
          direction = res.direction,
          screen = res.screen,
          viewName = res.viewName,
          overlayId = res.overlayId,
          renderMs = res.renderMs.toLong(),
          tree = res.tree,
          endTree = res.endTree,
          frame = mainFrame,
          endFrame = endFrame,
          error = res.error,
        )
      }
    return WorkerBatchResult(
      ok = resp.ok,
      renderTotalMs = resp.renderTotalMs.toLong(),
      error = resp.error,
      targets = outputs,
    )
  }

  override fun updateLive(entry: CompileEntry, update: LivePreviewUpdate) {
    val data = entry.androidData ?: return
    if (data.assetBlobs.isNotEmpty()) {
      transport.syncAssets(data.assetBlobs)
    }
    transport.setLive(
      dexBytes = data.dexBytes,
      request =
        DeviceLiveStateRequest(
          dexHash = data.dexHash,
          entryClass = data.entryClass,
          sourceFile = data.sourceFile,
          designTheme = entry.loaded.designTheme,
          assetMap = data.assetMap,
          direction = update.direction,
          screen = update.screenId,
          overlayId = update.overlayId,
          darkOverride = update.darkOverride,
          seedOverride = update.seedOverride,
          variantOverride = update.variantOverride,
          fontScale = update.fontScale,
        ),
      assetBlobs = data.assetBlobs,
    )
  }

  override fun finishCheck() {
    val pending = pendingRecycle.getAndSet(null) ?: return
    onRecycleRecommended(pending.reason)
  }

  override fun close() {
    pendingRecycle.set(null)
    transport.close()
  }

  private fun executeBatchWithAssetSync(
    data: AndroidCompiledData,
    designTheme: ThemeOverride?,
    targets: List<DeviceRenderTarget>,
  ): DeviceBatchExecution {
    if (data.assetBlobs.isNotEmpty()) {
      transport.syncAssets(data.assetBlobs)
    }
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
      assetBlobs = data.assetBlobs,
    )
  }

  private fun DecodedDeviceFrame.toWorkerFrame(): WorkerFrame? {
    val bytes = imageBytes.takeIf { it.isNotEmpty() } ?: return null
    return WorkerFrame(bytes = bytes, mime = imageMime, isPng = imageMime == "image/png")
  }

  companion object {
    private const val DEFAULT_BATCH_TIMEOUT_MS = 15_000L
  }
}
