package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.model.BoardMeta
import bot.charlies.androiddesign.preview.model.BoardSurface
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.ValidationError
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.WorkerRenderTarget
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

/** Compiled exploration artifacts cached per `(repoRoot, sourcePath, contentHash)`. */
data class CompiledBoardData(
  val sourceFile: String,
  val sourceText: String,
  val entryClass: String,
  val classBytes: Map<String, ByteArray>,
  val classBundleBytes: ByteArray,
  val classHash: String,
  val explorationDir: Path? = null,
  val assetMap: Map<String, String> = emptyMap(),
  val assetBlobs: Map<String, ByteArray> = emptyMap(),
  val boardTheme: ThemeOverride? = null,
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
  val surface: BoardSurface,
  val boardName: String?,
  val boardMeta: BoardMeta?,
  val compiledData: CompiledBoardData?,
  val boardTheme: ThemeOverride? = null,
)

class SandboxWatchdogException(message: String) : RuntimeException(message)

/** Outcome returned by a [RenderWorker] after compiling and preloading an exploration. */
sealed interface WorkerCompileResult {
  data class Ready(
    val sourcePath: Path,
    val designPath: Path?,
    val designTheme: ThemeOverride?,
    val compileMs: Long,
    val boardMeta: BoardMeta,
    val boardTheme: ThemeOverride? = null,
    val compiledData: CompiledBoardData? = null,
  ) : WorkerCompileResult

  data class Failed(
    val sourcePath: Path,
    val designPath: Path?,
    val designTheme: ThemeOverride?,
    val compileMs: Long,
    val errors: List<ValidationError>,
    val boardTheme: ThemeOverride? = null,
  ) : WorkerCompileResult
}

/** Encoded PNG frame returned from a [RenderWorker] target render. */
data class WorkerFrame(val bytes: ByteArray) {
  override fun equals(other: Any?): Boolean =
    this === other || (other is WorkerFrame && bytes.contentEquals(other.bytes))

  override fun hashCode(): Int = bytes.contentHashCode()
}

/** Per-target tree and optional PNG frame bytes returned from a [RenderWorker]. */
data class WorkerTargetOutput(
  val direction: String,
  val screen: String,
  val viewName: String,
  val overlayId: String? = null,
  val renderMs: Long = 0L,
  val tree: LayoutTree? = null,
  val endTree: LayoutTree? = null,
  val frame: WorkerFrame? = null,
  val endFrame: WorkerFrame? = null,
  val error: String? = null,
)

/** Batch render result returned from [RenderWorker.renderTargets]. */
data class WorkerBatchResult(
  val ok: Boolean,
  val renderTotalMs: Long = 0L,
  val error: String? = null,
  val targets: List<WorkerTargetOutput> = emptyList(),
)

/** Live-preview focus/theme update pushed to an active interactive viewer. */
data class LivePreviewUpdate(
  val direction: String? = null,
  val screenId: String? = null,
  val overlayId: String? = null,
  val darkOverride: Boolean? = null,
  val seedOverride: String? = null,
  val variantOverride: String? = null,
  val fontScale: Float = 1f,
)

/** Parameters for a single-view render executed through [CheckExecutor.renderSingle]. */
data class SingleRenderRequest(
  val direction: String,
  val screenId: String,
  val prefix: String,
  val overlayId: String? = null,
  val view: ViewSpec,
  val densityScale: Int = 2,
  val baseCheck: ViewCheck? = null,
  val writeFiles: Boolean = false,
  val rendersRoot: Path,
  val includeFrame: Boolean = true,
  val fontScale: Float = 1f,
  val seedOverride: String? = null,
  val variantOverride: String? = null,
  val darkOverride: Boolean? = null,
)

/**
 * Narrow boundary between [CheckExecutor] and the underlying compilation + frame-capture runtime
 * ([LayoutlibRenderWorker] or in-memory test doubles).
 */
interface RenderWorker : AutoCloseable {
  val name: String

  /**
   * Brings the worker to a warm state before a job starts. [RenderQueue] calls this outside the job
   * watchdog so a cold start (or restart after watchdog kill) is not charged to a job budget.
   */
  fun prepare() {}

  fun compile(snapshot: ContentHasher.Snapshot): WorkerCompileResult

  fun renderTargets(
    entry: CompileEntry,
    targets: List<WorkerRenderTarget>,
    stageRoot: Path,
  ): WorkerBatchResult

  override fun close() {}
}
