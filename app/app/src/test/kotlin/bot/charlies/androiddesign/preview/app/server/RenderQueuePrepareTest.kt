package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.model.BoardSurface
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.PhoneSurface
import bot.charlies.androiddesign.preview.model.ViewSpec
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * A backend that must warm a fresh worker (Robolectric after a watchdog kill) pays that cost in
 * [PreviewBackend.prepare], outside the server watchdog's job budget, so a cold start never halts
 * the whole server.
 */
class RenderQueuePrepareTest {
  @Test
  fun `backend warmup runs outside the job watchdog budget`() {
    val root = createTempDirectory("queue-prepare-")
    val repo = root.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val source = repo.resolve("design/proposal.explore.kt")
    Files.createDirectories(source.parent)
    Files.writeString(source, "val x = 1\n")

    val halted = AtomicBoolean(false)
    val prepared = AtomicBoolean(false)
    val backend = SlowWarmupBackend(warmupMs = 600L, prepared = prepared)
    val watchdog =
      Watchdog(
        coldBudgetMs = 200L,
        warmBudgetMs = 200L,
        pollIntervalMs = 10L,
        onTimeout = {},
        onHalt = { halted.set(true) },
      )
    val queue =
      RenderQueue(rendersRoot = root.resolve("renders"), backend = backend, watchdog = watchdog)
    watchdog.start()
    queue.start()
    try {
      val entry =
        queue
          .submitCompile(repo, ContentHasher.snapshot(source), QueuePriority.AGENT)
          .get(10, TimeUnit.SECONDS)
      assertTrue(prepared.get(), "prepare() must run before the compile job")
      assertFalse(halted.get(), "warmup inside the job budget would halt the server")
      assertTrue(entry.contentHash.isNotEmpty())
    } finally {
      queue.stop()
      watchdog.stop()
      root.toFile().deleteRecursively()
    }
  }

  private class SlowWarmupBackend(
    private val warmupMs: Long,
    private val prepared: AtomicBoolean,
  ) : PreviewBackend {
    override val name = RuntimeIdentity.BACKEND_ROBOLECTRIC
    override val supportsBatchCheck = true
    override val supportsHeadlessSpeculativePrecompile = false

    override fun prepare() {
      if (prepared.compareAndSet(false, true)) Thread.sleep(warmupMs)
    }

    override fun compile(snapshot: ContentHasher.Snapshot): BackendCompileOutput {
      // Without prepare(), the first job pays the warmup inside its watchdog budget.
      if (prepared.compareAndSet(false, true)) Thread.sleep(warmupMs)
      return BackendCompileOutput(
        loaded =
          LoadedExploration(
            sourcePath = snapshot.sourcePath,
            designPath = snapshot.designPath,
            designTheme = null,
            designSystem = DesignSystemResolver.resolve(null, null, PhoneSurface),
            compileMs = 1L,
            errors = emptyList(),
          ),
        dexHash = null,
        surface = BoardSurface.Phone,
        boardName = "Prepare",
        boardMeta = null,
        androidData = null,
      )
    }

    override fun renderBatchCheck(
      entry: CompileEntry,
      targets: List<CheckTargetSpec>,
      scoped: Boolean,
      rendersRoot: java.nio.file.Path,
    ): List<RenderArtifact> = emptyList()

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
      rendersRoot: java.nio.file.Path,
      frameFormat: FrameFormat,
      fontScale: Float,
      seedOverride: String?,
      variantOverride: String?,
      darkOverride: Boolean?,
    ): RenderArtifact = error("unused")

    override fun close() {}
  }
}
