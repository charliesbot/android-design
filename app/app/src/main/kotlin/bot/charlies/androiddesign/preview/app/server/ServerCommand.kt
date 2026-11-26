package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.cli.Command
import bot.charlies.androiddesign.preview.app.cli.ExitCode
import bot.charlies.androiddesign.preview.app.cli.parseArgs
import bot.charlies.androiddesign.preview.app.client.ClientExitCode
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.model.PreviewJson
import java.io.PrintStream
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

/** Internal subcommand launched by the CLI client to run the single shared HTTP/SSE host server. */
class ServerCommand : Command {
  override val name = "server"
  override val usage =
    "server --repo <dir> [--preferred-port <port>] [--view-token <token>] " +
      "[--idle-timeout-ms <ms>] [--max-compiles <n>] [--runtime-hash <hash>]"

  override fun run(args: List<String>, out: PrintStream, err: PrintStream): Int {
    val parsed =
      parseArgs(
        args,
        setOf(
          "--repo",
          "--preferred-port",
          "--view-token",
          "--idle-timeout-ms",
          "--max-compiles",
          "--runtime-hash",
        ),
        err,
      ) ?: return ExitCode.USAGE

    val repoPath =
      parsed.flags["--repo"]?.let(Path::of) ?: Path.of(".").toAbsolutePath().normalize()
    val repoRoot = ServerPaths.resolveRepoRoot(repoPath)
    val preferredPort =
      parsed.flags["--preferred-port"]?.toIntOrNull() ?: ServerPaths.SHARED_PREFERRED_PORT
    val viewToken = parsed.flags["--view-token"]?.takeIf { it.isNotBlank() }
    val idleTimeoutMs =
      parsed.flags["--idle-timeout-ms"]?.toLongOrNull()
        ?: System.getenv(IDLE_TIMEOUT_ENV)?.toLongOrNull()
        ?: PreviewHttpServer.DEFAULT_IDLE_TIMEOUT_MS
    val maxCompiles =
      parsed.flags["--max-compiles"]?.toIntOrNull()
        ?: System.getenv(MAX_COMPILES_ENV)?.toIntOrNull()
        ?: PreviewHttpServer.MAX_COMPILES_BEFORE_RECYCLE
    val coldBudgetMs = System.getenv(COLD_BUDGET_ENV)?.toLongOrNull() ?: Watchdog.COLD_BUDGET_MS
    val warmBudgetMs = System.getenv(WARM_BUDGET_ENV)?.toLongOrNull() ?: Watchdog.WARM_BUDGET_MS
    val env = System.getenv().toMutableMap()
    parsed.flags["--runtime-hash"]
      ?.takeIf { it.isNotBlank() }
      ?.let {
        env[RuntimeIdentity.RUNTIME_HASH_ENV] = it
      }
    val runtimeIdentity = RuntimeIdentity.compute(env)
    val cacheRoot = ServerPaths.cacheRoot()

    val runningServer = AtomicReference<PreviewHttpServer?>(null)
    val runningWorker = AtomicReference<RenderWorker?>(null)
    val runningLiveHost = AtomicReference<LiveSessionHost?>(null)
    val latch = CountDownLatch(1)
    Runtime.getRuntime()
      .addShutdownHook(
        Thread {
          runningServer.get()?.stop(triggerExit = false)
          runCatching { runningWorker.get()?.close() }
          runCatching { runningLiveHost.get()?.close() }
          latch.countDown()
        },
      )

    val meta =
      BootstrapMeta.locateOrNull()
        ?: run {
          val msg =
            PreviewJson.compact.encodeToString(
              ErrorEnvelope.serializer(),
              ErrorEnvelope(
                error = "bootstrap_missing",
                message = "Cannot find bootstrap-meta.properties for preview host server",
              ),
            )
          out.println(msg)
          err.println(msg)
          return ClientExitCode.NOT_READY
        }

    out.println("startup: starting layoutlib render worker")
    out.flush()
    val worker = LayoutlibRenderWorker(meta = meta, env = env)
    runningWorker.set(worker)
    try {
      worker.prepare()
    } catch (t: Throwable) {
      val msg =
        PreviewJson.compact.encodeToString(
          ErrorEnvelope.serializer(),
          ErrorEnvelope(
            error = "worker_start_failed",
            message = t.message ?: "Failed starting layoutlib render worker",
          ),
        )
      out.println(msg)
      err.println(msg)
      runCatching { worker.close() }
      return ClientExitCode.NOT_READY
    }

    val liveSessionHost = AndroidLiveSessionHost(meta = meta, cacheRoot = cacheRoot, env = env)
    runningLiveHost.set(liveSessionHost)

    val server =
      PreviewHttpServer(
        repoRoot = repoRoot,
        cacheRoot = cacheRoot,
        preferredPort = preferredPort,
        existingViewToken = viewToken,
        maxCompilesBeforeRecycle = maxCompiles,
        idleTimeoutMs = idleTimeoutMs,
        coldBudgetMs = coldBudgetMs,
        warmBudgetMs = warmBudgetMs,
        worker = worker,
        runtimeIdentity = runtimeIdentity,
        liveSessionHost = liveSessionHost,
        onWatchdogHalt =
          watchdogHaltAction(
            releaseResources = {
              runCatching { worker.close() }
              runCatching { liveSessionHost.close() }
            },
          ),
        onProcessExit = { latch.countDown() },
      )
    runningServer.set(server)
    val record = server.start()
    out.println(
      """{"ok":true,"pid":${record.pid},"port":${record.port},"backend":"${record.backend}","boardUrl":"${record.boardUrl(ServerPaths.repoHash(repoRoot))}"}""",
    )
    out.flush()
    try {
      latch.await()
    } catch (_: InterruptedException) {
      server.stop(triggerExit = false)
    } finally {
      server.stop(triggerExit = false)
      runCatching { worker.close() }
      runCatching { liveSessionHost.close() }
    }
    return ExitCode.OK
  }

  companion object {
    const val IDLE_TIMEOUT_ENV = "ANDROID_DESIGN_IDLE_TIMEOUT_MS"
    const val MAX_COMPILES_ENV = "ANDROID_DESIGN_MAX_COMPILES"
    const val COLD_BUDGET_ENV = "ANDROID_DESIGN_WATCHDOG_COLD_MS"
    const val WARM_BUDGET_ENV = "ANDROID_DESIGN_WATCHDOG_WARM_MS"

    /** Releases active worker and live emulator before `Runtime.halt` skips JVM shutdown hooks. */
    internal fun watchdogHaltAction(
      releaseResources: () -> Unit,
      haltProcess: () -> Unit = { Runtime.getRuntime().halt(1) },
    ): () -> Unit = {
      runCatching { releaseResources() }
      haltProcess()
    }
  }
}
