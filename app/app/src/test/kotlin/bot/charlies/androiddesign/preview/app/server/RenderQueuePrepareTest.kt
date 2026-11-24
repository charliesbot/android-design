package bot.charlies.androiddesign.preview.app.server

import java.nio.file.Files
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * A worker that must warm a fresh process (Robolectric after a watchdog kill) pays that cost in
 * [RenderWorker.prepare], outside the server watchdog's job budget, so a cold start never halts the
 * whole server.
 */
class RenderQueuePrepareTest {
  @Test
  fun `worker warmup runs outside the job watchdog budget`() {
    val root = createTempDirectory("queue-prepare-")
    val repo = root.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val source = repo.resolve("design/proposal.explore.kt")
    Files.createDirectories(source.parent)
    Files.writeString(source, "val x = 1\n")

    val halted = AtomicBoolean(false)
    val prepared = AtomicBoolean(false)
    val executor =
      CheckExecutor(
        InMemoryRenderWorker(
          name = RuntimeIdentity.BACKEND_ROBOLECTRIC,
          onPrepare = { if (prepared.compareAndSet(false, true)) Thread.sleep(600L) },
          onCompile = {
            // Without prepare(), the first job pays the warmup inside its watchdog budget.
            if (prepared.compareAndSet(false, true)) Thread.sleep(600L)
          },
        ),
      )
    val watchdog =
      Watchdog(
        coldBudgetMs = 200L,
        warmBudgetMs = 200L,
        pollIntervalMs = 10L,
        onTimeout = {},
        onHalt = { halted.set(true) },
      )
    val queue =
      RenderQueue(rendersRoot = root.resolve("renders"), executor = executor, watchdog = watchdog)
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
}
