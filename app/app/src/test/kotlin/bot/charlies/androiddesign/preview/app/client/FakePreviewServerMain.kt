package bot.charlies.androiddesign.preview.app.client

import bot.charlies.androiddesign.preview.app.server.InMemoryRenderWorker
import bot.charlies.androiddesign.preview.app.server.PreviewHttpServer
import bot.charlies.androiddesign.preview.app.server.RuntimeIdentity
import bot.charlies.androiddesign.preview.app.server.ServerPaths
import bot.charlies.androiddesign.preview.app.server.WorkerFrame
import bot.charlies.androiddesign.preview.app.server.WorkerTargetOutput
import bot.charlies.androiddesign.preview.domain.image.FastPngEncoder
import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.DirectionMeta
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PhoneSurface
import bot.charlies.androiddesign.preview.model.Views
import java.nio.file.Path
import java.util.concurrent.CountDownLatch

/** Test-only subprocess server using [InMemoryRenderWorker] so client tests need no emulator. */
object FakePreviewServerMain {
  @JvmStatic
  fun main(args: Array<String>) {
    var repo = Path.of(".")
    var preferredPort = 0
    var viewToken: String? = null
    var runtimeHash: String? = null
    var idx = 0
    while (idx < args.size) {
      when (args[idx]) {
        "--repo" -> repo = Path.of(args.getOrElse(++idx) { "." })
        "--preferred-port" -> preferredPort = args.getOrElse(++idx) { "0" }.toIntOrNull() ?: 0
        "--view-token" -> viewToken = args.getOrNull(++idx)
        "--runtime-hash" -> runtimeHash = args.getOrNull(++idx)
        else -> {
          if (
            args[idx].startsWith("--") && idx + 1 < args.size && !args[idx + 1].startsWith("--")
          ) {
            idx++
          }
        }
      }
      idx++
    }

    val rgba =
      ByteArray(64 * 64 * 4) { i ->
        if (i % 4 == 3) 0xFF.toByte() else ((i * 37) xor (i shr 3)).toByte()
      }
    val png = FastPngEncoder.encodeFast(64, 64, rgba)
    val worker =
      InMemoryRenderWorker(
        name = RuntimeIdentity.BACKEND_LAYOUTLIB,
        boardName = "Plates",
        directions =
          listOf(DirectionMeta("Editorial", "phone", "home", listOf("home"), emptyList())),
        views = PhoneSurface.views,
        targetBuilder = { target ->
          val view = Views.byName(target.viewName) ?: Views.Light
          val node =
            LayoutNode(
              id = "btn-1",
              type = "Button",
              bounds = Bounds(16f, 40f, 100f, 56f),
              sourceFile = "plates.explore.kt",
              sourceLine = 12,
            )
          WorkerTargetOutput(
            direction = target.direction,
            screen = target.screen,
            viewName = target.viewName,
            overlayId = target.overlayId,
            renderMs = 2L,
            tree =
              LayoutTree(
                screen = target.screen,
                view = view,
                nodes = listOf(node),
                findings = emptyList(),
              ),
            frame = WorkerFrame(bytes = png),
          )
        },
      )

    val cacheRoot = ServerPaths.cacheRoot()
    val computed = RuntimeIdentity.compute(System.getenv())
    val identity =
      if (!runtimeHash.isNullOrBlank()) computed.copy(runtimeHash = runtimeHash) else computed
    val latch = CountDownLatch(1)
    val server =
      PreviewHttpServer(
        repoRoot = ServerPaths.resolveRepoRoot(repo),
        cacheRoot = cacheRoot,
        preferredPort = preferredPort,
        existingViewToken = viewToken,
        idleTimeoutMs = 0L,
        worker = worker,
        runtimeIdentity = identity,
        enableFileWatcher = false,
        onProcessExit = { latch.countDown() },
      )
    server.start()
    Runtime.getRuntime()
      .addShutdownHook(
        Thread {
          server.stop(triggerExit = false)
          latch.countDown()
        },
      )
    latch.await()
  }
}
