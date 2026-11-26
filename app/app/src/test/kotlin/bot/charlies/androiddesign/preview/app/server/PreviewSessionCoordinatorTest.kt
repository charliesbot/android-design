package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.model.DirectionMeta
import bot.charlies.androiddesign.preview.model.PhoneSurface
import java.nio.file.Files
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PreviewSessionCoordinatorTest {
  private fun coordinatorFixture(
    tmp: java.nio.file.Path,
    worker: RenderWorker =
      InMemoryRenderWorker(
        directions = listOf(DirectionMeta("Editorial", "phone", "home", listOf("home"))),
        views = PhoneSurface.views,
      ),
    coldBudgetMs: Long = Watchdog.COLD_BUDGET_MS,
    warmBudgetMs: Long = Watchdog.WARM_BUDGET_MS,
    onWatchdogHalt: () -> Unit = {},
    sseEvents: CopyOnWriteArrayList<Pair<String, String>> = CopyOnWriteArrayList(),
  ): PreviewSessionCoordinator {
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    return PreviewSessionCoordinator(
      repoRoot = repo,
      cacheRoot = tmp.resolve("cache"),
      coldBudgetMs = coldBudgetMs,
      warmBudgetMs = warmBudgetMs,
      worker = worker,
      onWatchdogHalt = onWatchdogHalt,
      enableFileWatcher = false,
      boardUrlForRepoId = { rid -> "http://127.0.0.1:0/?repo=${rid.orEmpty()}" },
      broadcastSse = { _, ev, json -> sseEvents += ev to json },
      hasBoardListeners = { sseEvents.isNotEmpty() },
    )
  }

  @Test
  fun `coordinator executes check, tree, boardFocus, and serialized override render without HTTP`() {
    val tmp = createTempDirectory("coord-unit-")
    val renderThreads = CopyOnWriteArrayList<String>()
    val worker =
      InMemoryRenderWorker(
        directions = listOf(DirectionMeta("Editorial", "phone", "home", listOf("home"))),
        views = PhoneSurface.views,
        onRenderTarget = { _ -> renderThreads += Thread.currentThread().name },
      )
    val coord = coordinatorFixture(tmp, worker = worker)
    coord.start()
    try {
      val repo = coord.defaultRepoRoot
      val source =
        repo.resolve("design/board.explore.kt").also {
          Files.createDirectories(it.parent)
          Files.writeString(it, "val preview = Unit\n")
        }

      val checkOutcome =
        assertIs<CheckOutcome.Reply>(
          coord.check(
            CheckRequest(
              path = source.toString(),
              direction = "Editorial",
              view = "light",
              repoRoot = repo.toString(),
            ),
          ),
        )
      assertEquals(200, checkOutcome.status)
      assertTrue(checkOutcome.response.ok)

      val treeOutcome =
        assertIs<CoordinatorOutcome.Success<TreeResponse>>(
          coord.tree(TreeRequest(path = source.toString(), view = "light", maxDepth = 1)),
        )
      assertEquals("Editorial", treeOutcome.payload.direction)
      assertNotNull(treeOutcome.payload.tree)

      coord.boardFocus(null, BoardFocusRequest(direction = "Editorial", view = "light"))
      val callerThread = Thread.currentThread().name
      val overrideOutcome =
        assertIs<CoordinatorOutcome.Success<BoardRenderResponse>>(
          coord.boardRender(
            null,
            BoardRenderRequest(direction = "Editorial", view = "light", seed = "#123456"),
          ),
        )
      assertEquals(200, overrideOutcome.status)
      assertEquals("light", overrideOutcome.payload.view)
      assertTrue(renderThreads.isNotEmpty())
      assertTrue(
        renderThreads.all { it == "android-design-render-queue" },
        "All renders including renderOverride must execute on the queue thread, got $renderThreads (caller=$callerThread)",
      )
      assertNotEquals(callerThread, renderThreads.last())
    } finally {
      coord.stop()
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `queued renderOverride is protected by Watchdog`() {
    val tmp = createTempDirectory("coord-override-watchdog-")
    val halted = AtomicBoolean(false)
    val entered = CountDownLatch(1)
    val release = CountDownLatch(1)
    val worker =
      InMemoryRenderWorker(
        directions = listOf(DirectionMeta("Editorial", "phone", "home", listOf("home"))),
        views = PhoneSurface.views,
        onRenderTarget = { target ->
          if (target.seedOverride != null) {
            entered.countDown()
            release.await(5, TimeUnit.SECONDS)
          }
        },
      )
    val coord =
      coordinatorFixture(
        tmp,
        worker = worker,
        coldBudgetMs = 2_000L,
        warmBudgetMs = 150L,
        onWatchdogHalt = { halted.set(true) },
      )
    coord.start()
    try {
      val repo = coord.defaultRepoRoot
      val source =
        repo.resolve("design/board.explore.kt").also {
          Files.createDirectories(it.parent)
          Files.writeString(it, "val preview = Unit\n")
        }
      assertIs<CheckOutcome.Reply>(
        coord.check(
          CheckRequest(path = source.toString(), direction = "Editorial", view = "light"),
        ),
      )
      val errRef = AtomicReference<Throwable?>()
      val t = Thread {
        runCatching {
          coord.boardRender(
            null,
            BoardRenderRequest(direction = "Editorial", view = "light", seed = "#ABCDEF"),
          )
        }
          .onFailure { errRef.set(it) }
      }
      t.start()
      assertTrue(entered.await(2, TimeUnit.SECONDS))
      val deadline = System.currentTimeMillis() + 2_000L
      while (!halted.get() && System.currentTimeMillis() < deadline) {
        Thread.sleep(20)
      }
      assertTrue(halted.get(), "Watchdog must trip when renderOverride hangs past warmBudgetMs")
      release.countDown()
      t.join(2_000)
    } finally {
      release.countDown()
      coord.stop()
      tmp.toFile().deleteRecursively()
    }
  }
}
