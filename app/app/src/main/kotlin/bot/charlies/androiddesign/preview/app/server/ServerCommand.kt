package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.cli.Command
import bot.charlies.androiddesign.preview.app.cli.ExitCode
import bot.charlies.androiddesign.preview.app.cli.parseArgs
import java.io.PrintStream
import java.nio.file.Path
import java.util.concurrent.CountDownLatch

/**
 * Internal subcommand launched by the CLI client to run one long-lived HTTP/SSE server per
 * repository.
 */
class ServerCommand : Command {
  override val name = "server"
  override val usage =
    "server --repo <dir> [--preferred-port <port>] [--view-token <token>] [--idle-timeout-ms <ms>]"

  override fun run(args: List<String>, out: PrintStream, err: PrintStream): Int {
    val parsed =
      parseArgs(
        args,
        setOf("--repo", "--preferred-port", "--view-token", "--idle-timeout-ms", "--max-compiles"),
        err,
      ) ?: return ExitCode.USAGE
    val repoPath =
      parsed.flags["--repo"]?.let(Path::of) ?: Path.of(".").toAbsolutePath().normalize()
    val repoRoot = ServerPaths.resolveRepoRoot(repoPath)
    val preferredPort =
      parsed.flags["--preferred-port"]?.toIntOrNull() ?: ServerPaths.preferredPort(repoRoot)
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

    val latch = CountDownLatch(1)
    val server =
      PreviewHttpServer(
        repoRoot = repoRoot,
        preferredPort = preferredPort,
        existingViewToken = viewToken,
        maxCompilesBeforeRecycle = maxCompiles,
        idleTimeoutMs = idleTimeoutMs,
        coldBudgetMs = coldBudgetMs,
        warmBudgetMs = warmBudgetMs,
        onProcessExit = { latch.countDown() },
      )
    val record = server.start()
    Runtime.getRuntime()
      .addShutdownHook(
        Thread {
          server.stop(triggerExit = false)
          latch.countDown()
        },
      )
    out.println(
      """{"ok":true,"pid":${record.pid},"port":${record.port},"boardUrl":"${record.boardUrl()}"}""",
    )
    out.flush()
    try {
      latch.await()
    } catch (_: InterruptedException) {
      server.stop(triggerExit = false)
    }
    return ExitCode.OK
  }

  companion object {
    const val IDLE_TIMEOUT_ENV = "ANDROID_DESIGN_IDLE_TIMEOUT_MS"
    const val MAX_COMPILES_ENV = "ANDROID_DESIGN_MAX_COMPILES"
    const val COLD_BUDGET_ENV = "ANDROID_DESIGN_WATCHDOG_COLD_MS"
    const val WARM_BUDGET_ENV = "ANDROID_DESIGN_WATCHDOG_WARM_MS"
  }
}
