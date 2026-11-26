package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.model.Finding
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.Severity
import bot.charlies.androiddesign.preview.model.Views
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * A render error can come from the device rather than the board (a host activity that died, a
 * timeout), so the queue never serves one from its content-hash cache: the next request renders
 * again instead of repeating a stale failure until the server restarts.
 */
class RenderQueueErrorCacheTest {
  @Test
  fun `a target that failed on the device renders again on the next batch check`() {
    val root = createTempDirectory("queue-error-cache-")
    val repo = root.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val source = repo.resolve("design/proposal.explore.kt")
    Files.createDirectories(source.parent)
    Files.writeString(source, "val x = 1\n")

    // First batch: Light renders with a design error (so the batch writes its PNG), and Dark
    // fails on the device. Every later batch renders both cleanly.
    val calls = AtomicInteger(0)
    val worker =
      InMemoryRenderWorker(
        batchOverride = { _, deviceTargets, _ ->
          val firstBatch = calls.incrementAndGet() == 1
          WorkerBatchResult(
            ok = true,
            targets =
              deviceTargets.map { t ->
                val view = Views.byName(t.viewName) ?: Views.Light
                val deviceFailure = firstBatch && view == Views.Dark
                val findings =
                  if (firstBatch && view == Views.Light) {
                    listOf(Finding("cta", FindingRule.TouchTarget, Severity.Error))
                  } else {
                    emptyList()
                  }
                WorkerTargetOutput(
                  direction = t.direction,
                  screen = t.screen,
                  viewName = t.viewName,
                  overlayId = t.overlayId,
                  tree =
                    if (deviceFailure) null
                    else LayoutTree(t.screen, view, nodes = emptyList(), findings = findings),
                  frame =
                    if (deviceFailure) null
                    else
                      WorkerFrame(
                        bytes = InMemoryRenderWorker.PNG_SIGNATURE + t.viewName.toByteArray(),
                        mime = "image/png",
                        isPng = true,
                      ),
                  error =
                    if (deviceFailure) "Timed out waiting for layoutlib worker render" else null,
                )
              },
          )
        },
      )
    val targets =
      listOf(
        CheckTargetSpec("A", "home", "A-home", null, Views.Light),
        CheckTargetSpec("A", "home", "A-home", null, Views.Dark),
      )
    val queue = RenderQueue(rendersRoot = root.resolve("renders"), executor = CheckExecutor(worker))
    queue.start()
    try {
      val entry =
        queue
          .submitCompile(repo, ContentHasher.snapshot(source), QueuePriority.AGENT)
          .get(10, TimeUnit.SECONDS)
      val first = queue.submitBatchCheck(entry, targets, scoped = false).get(10, TimeUnit.SECONDS)
      assertTrue(
        first
          .single { it.view == Views.Dark }
          .check
          .errors
          .any {
            it.rule == ViewCheck.RENDER_ERROR
          },
      )

      val rendersAfterFirst = worker.renderBatchCount.get()

      val second = queue.submitBatchCheck(entry, targets, scoped = false).get(10, TimeUnit.SECONDS)
      assertEquals(
        rendersAfterFirst + 1,
        worker.renderBatchCount.get(),
        "a render error must not be served from cache",
      )
      assertTrue(second.all { it.check.errors.isEmpty() }, "the rerun must replace the failure")
    } finally {
      queue.stop()
      root.toFile().deleteRecursively()
    }
  }

  @Test
  fun `a single render that failed on the device renders again on the next request`() {
    val root = createTempDirectory("queue-single-error-")
    val repo = root.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val source = repo.resolve("design/proposal.explore.kt")
    Files.createDirectories(source.parent)
    Files.writeString(source, "val x = 1\n")

    val calls = AtomicInteger(0)
    val worker =
      InMemoryRenderWorker(
        batchOverride = { _, deviceTargets, _ ->
          val t = deviceTargets.single()
          val failing = calls.incrementAndGet() == 1
          WorkerBatchResult(
            ok = true,
            targets =
              listOf(
                WorkerTargetOutput(
                  direction = t.direction,
                  screen = t.screen,
                  viewName = t.viewName,
                  overlayId = t.overlayId,
                  tree =
                    if (failing) null
                    else
                      LayoutTree(
                        t.screen,
                        Views.Light,
                        nodes = emptyList(),
                        findings = emptyList(),
                      ),
                  frame =
                    if (failing) null
                    else
                      WorkerFrame(
                        bytes = InMemoryRenderWorker.PNG_SIGNATURE + t.viewName.toByteArray(),
                        mime = "image/png",
                        isPng = true,
                      ),
                  error = if (failing) "Timed out waiting for layoutlib worker render" else null,
                ),
              ),
          )
        },
      )
    val queue = RenderQueue(rendersRoot = root.resolve("renders"), executor = CheckExecutor(worker))
    queue.start()
    try {
      val entry =
        queue
          .submitCompile(repo, ContentHasher.snapshot(source), QueuePriority.AGENT)
          .get(10, TimeUnit.SECONDS)
      fun render() =
        queue
          .submitSingleRender(
            entry = entry,
            direction = "A",
            screenId = "home",
            prefix = "A-home",
            overlayId = null,
            view = Views.Light,
            densityScale = 2,
            baseCheck = null,
            priority = QueuePriority.AGENT,
          )
          .get(10, TimeUnit.SECONDS)

      assertTrue(render().check.isRenderError)
      val second = render()
      assertEquals(2, calls.get(), "a render error must not be shared with the next request")
      assertTrue(second.check.errors.isEmpty(), "the rerun must replace the failure")
    } finally {
      queue.stop()
      root.toFile().deleteRecursively()
    }
  }
}
