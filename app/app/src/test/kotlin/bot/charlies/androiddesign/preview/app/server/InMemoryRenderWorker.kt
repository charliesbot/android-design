package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.model.DeviceBoardMeta
import bot.charlies.androiddesign.preview.model.DeviceDirectionMeta
import bot.charlies.androiddesign.preview.model.DeviceRenderTarget
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Configurable in-memory [RenderWorker] for JVM unit tests covering [CheckExecutor], [RenderQueue],
 * [CheckService], and [PreviewHttpServer].
 */
class InMemoryRenderWorker(
  override val name: String = "android",
  override val supportsHeadlessSpeculativePrecompile: Boolean = false,
  override val scopedCheckDensity: Int = 2,
  private val surface: String = "phone",
  private val boardName: String? = "Board",
  private val directions: List<DeviceDirectionMeta> =
    listOf(
      DeviceDirectionMeta(
        name = "A",
        surface = surface,
        startScreen = "home",
        screens = listOf("home"),
      ),
    ),
  private val views: List<ViewSpec> = Views.all,
  private val dexHashFor: ((ContentHasher.Snapshot) -> String?)? = null,
  private val onPrepare: () -> Unit = {},
  private val compileOverride: ((ContentHasher.Snapshot) -> WorkerCompileResult)? = null,
  private val onCompile: (ContentHasher.Snapshot) -> Unit = {},
  private val onRenderBatch: (List<DeviceRenderTarget>) -> Unit = {},
  private val aroundRenderBatch:
    ((List<DeviceRenderTarget>, () -> WorkerBatchResult) -> WorkerBatchResult)? =
    null,
  private val onRenderTarget: (DeviceRenderTarget) -> Unit = {},
  private val batchOverride:
    ((CompileEntry, List<DeviceRenderTarget>, Path) -> WorkerBatchResult)? =
    null,
  private val targetBuilder: ((DeviceRenderTarget) -> WorkerTargetOutput)? = null,
  private val onUpdateLive: (CompileEntry, LivePreviewUpdate) -> Unit = { _, _ -> },
  private val onClose: () -> Unit = {},
) : RenderWorker {
  val prepareCount = AtomicInteger(0)
  val compileCount = AtomicInteger(0)
  val renderBatchCount = AtomicInteger(0)
  val recordedBatches = CopyOnWriteArrayList<List<DeviceRenderTarget>>()
  val recordedUpdates = CopyOnWriteArrayList<LivePreviewUpdate>()
  val closed = AtomicBoolean(false)

  override fun prepare() {
    prepareCount.incrementAndGet()
    onPrepare()
  }

  override fun compile(snapshot: ContentHasher.Snapshot): WorkerCompileResult {
    compileCount.incrementAndGet()
    onCompile(snapshot)
    compileOverride?.let {
      return it(snapshot)
    }
    return WorkerCompileResult.Ready(
      sourcePath = snapshot.sourcePath,
      designPath = snapshot.designPath,
      designTheme = null,
      compileMs = 5L,
      boardMeta =
        DeviceBoardMeta(
          surface = surface,
          boardName = boardName,
          directions = directions,
          views = views,
        ),
      dexHash = dexHashFor?.invoke(snapshot),
    )
  }

  override fun renderTargets(
    entry: CompileEntry,
    targets: List<DeviceRenderTarget>,
    stageRoot: Path,
  ): WorkerBatchResult {
    renderBatchCount.incrementAndGet()
    recordedBatches += targets.toList()
    onRenderBatch(targets)
    val proceed = {
      batchOverride?.invoke(entry, targets, stageRoot)
        ?: targets
          .map { target ->
            onRenderTarget(target)
            targetBuilder?.invoke(target) ?: defaultTargetOutput(target)
          }
          .let { outputs ->
            WorkerBatchResult(ok = true, renderTotalMs = outputs.size.toLong(), targets = outputs)
          }
    }
    return aroundRenderBatch?.invoke(targets, proceed) ?: proceed()
  }

  override fun updateLive(entry: CompileEntry, update: LivePreviewUpdate) {
    recordedUpdates += update
    onUpdateLive(entry, update)
  }

  override fun close() {
    if (closed.compareAndSet(false, true)) {
      onClose()
    }
  }

  private fun defaultTargetOutput(target: DeviceRenderTarget): WorkerTargetOutput {
    val view =
      Views.byName(target.viewName)
        ?: views.firstOrNull { it.name == target.viewName }
        ?: Views.Light
    val tree =
      LayoutTree(
        screen = target.screen,
        view = view,
        nodes = emptyList(),
        findings = emptyList(),
      )
    val frame =
      when (target.frameFormat) {
        FrameFormat.NONE -> null
        FrameFormat.WEBP_LOSSLESS ->
          WorkerFrame(
            bytes =
              ("RIFF\u0000\u0000\u0000\u0000WEBP" +
                  "${target.direction}/${target.screen}/${target.overlayId}/${target.viewName}@${target.densityScale}")
                .toByteArray(),
            mime = "image/webp",
            isPng = false,
          )
        FrameFormat.LZ4_RGBA ->
          WorkerFrame(
            bytes =
              PNG_SIGNATURE +
                "${target.direction}/${target.screen}/${target.overlayId}/${target.viewName}@${target.densityScale}"
                  .toByteArray(),
            mime = "image/png",
            isPng = true,
          )
      }
    return WorkerTargetOutput(
      direction = target.direction,
      screen = target.screen,
      viewName = target.viewName,
      overlayId = target.overlayId,
      renderMs = 1L,
      tree = tree,
      endTree = null,
      frame = frame,
      endFrame = null,
    )
  }

  companion object {
    val PNG_SIGNATURE: ByteArray =
      byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
  }
}
