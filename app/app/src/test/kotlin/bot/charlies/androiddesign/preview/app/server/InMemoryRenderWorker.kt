package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.model.BoardMeta
import bot.charlies.androiddesign.preview.model.DirectionMeta
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import bot.charlies.androiddesign.preview.model.WorkerRenderTarget
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Configurable in-memory [RenderWorker] for JVM unit tests covering [CheckExecutor], [RenderQueue],
 * [PreviewSessionCoordinator], and [PreviewHttpServer].
 */
class InMemoryRenderWorker(
  override val name: String = RuntimeIdentity.BACKEND_LAYOUTLIB,
  private val surface: String = "phone",
  private val boardName: String? = "Board",
  private val directions: List<DirectionMeta> =
    listOf(
      DirectionMeta(
        name = "A",
        surface = surface,
        startScreen = "home",
        screens = listOf("home"),
      ),
    ),
  private val views: List<ViewSpec> = Views.all,
  private val onPrepare: () -> Unit = {},
  private val compileOverride: ((ContentHasher.Snapshot) -> WorkerCompileResult)? = null,
  private val onCompile: (ContentHasher.Snapshot) -> Unit = {},
  private val onRenderBatch: (List<WorkerRenderTarget>) -> Unit = {},
  private val aroundRenderBatch:
    ((List<WorkerRenderTarget>, () -> WorkerBatchResult) -> WorkerBatchResult)? =
    null,
  private val onRenderTarget: (WorkerRenderTarget) -> Unit = {},
  private val batchOverride:
    ((CompileEntry, List<WorkerRenderTarget>, Path) -> WorkerBatchResult)? =
    null,
  private val targetBuilder: ((WorkerRenderTarget) -> WorkerTargetOutput)? = null,
  private val onClose: () -> Unit = {},
) : RenderWorker {
  val prepareCount = AtomicInteger(0)
  val compileCount = AtomicInteger(0)
  val renderBatchCount = AtomicInteger(0)
  val recordedBatches = CopyOnWriteArrayList<List<WorkerRenderTarget>>()
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
        BoardMeta(
          surface = surface,
          boardName = boardName,
          directions = directions,
          views = views,
        ),
    )
  }

  override fun renderTargets(
    entry: CompileEntry,
    targets: List<WorkerRenderTarget>,
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

  override fun close() {
    if (closed.compareAndSet(false, true)) {
      onClose()
    }
  }

  private fun defaultTargetOutput(target: WorkerRenderTarget): WorkerTargetOutput {
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
      if (!target.includeFrame) {
        null
      } else {
        WorkerFrame(
          bytes =
            PNG_SIGNATURE +
              "${target.direction}/${target.screen}/${target.overlayId}/${target.viewName}@${target.densityScale}"
                .toByteArray(),
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
