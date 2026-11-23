package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.android.AdbTcpDeviceTransport
import bot.charlies.androiddesign.preview.app.android.DeviceManager
import bot.charlies.androiddesign.preview.app.android.DeviceSetupResult
import bot.charlies.androiddesign.preview.app.cli.Command
import bot.charlies.androiddesign.preview.app.cli.ExitCode
import bot.charlies.androiddesign.preview.app.cli.parseArgs
import bot.charlies.androiddesign.preview.app.client.V5ExitCode
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.model.PreviewJson
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/** Internal subcommand launched by the CLI client to run the single shared HTTP/SSE host server. */
class ServerCommand : Command {
  override val name = "server"
  override val usage =
    "server --repo <dir> [--preferred-port <port>] [--view-token <token>] [--idle-timeout-ms <ms>] " +
      "[--backend android|robolectric] [--runtime-hash <hash>] [--device-serial <id>] [--avd <name>] " +
      "[--headless true|false] [--force-reinstall-apk true|false]"

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
          "--backend",
          "--runtime-hash",
          "--device-serial",
          "--avd",
          "--headless",
          "--force-reinstall-apk",
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
    val backendMode =
      parsed.flags["--backend"]?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
        ?: RuntimeIdentity.resolveBackend()
    if (
      backendMode != RuntimeIdentity.BACKEND_ANDROID &&
        backendMode != RuntimeIdentity.BACKEND_ROBOLECTRIC
    ) {
      val msg =
        PreviewJson.compact.encodeToString(
          ErrorEnvelope.serializer(),
          ErrorEnvelope(
            error = "invalid_backend",
            message = "Unsupported backend '$backendMode'; expected 'android' or 'robolectric'",
          ),
        )
      out.println(msg)
      err.println(msg)
      return ExitCode.USAGE
    }
    val coldBudgetMs =
      System.getenv(COLD_BUDGET_ENV)?.toLongOrNull()
        ?: if (backendMode == RuntimeIdentity.BACKEND_ROBOLECTRIC) {
          RobolectricPreviewBackend.DEFAULT_COLD_BUDGET_MS
        } else {
          Watchdog.COLD_BUDGET_MS
        }
    val warmBudgetMs =
      System.getenv(WARM_BUDGET_ENV)?.toLongOrNull()
        ?: if (backendMode == RuntimeIdentity.BACKEND_ROBOLECTRIC) {
          RobolectricPreviewBackend.DEFAULT_WARM_BUDGET_MS
        } else {
          Watchdog.WARM_BUDGET_MS
        }
    val env = System.getenv().toMutableMap().apply { put(RuntimeIdentity.BACKEND_ENV, backendMode) }
    parsed.flags["--runtime-hash"]
      ?.takeIf { it.isNotBlank() }
      ?.let {
        env[RuntimeIdentity.RUNTIME_HASH_ENV] = it
      }
    val runtimeIdentity = RuntimeIdentity.compute(env)
    val cacheRoot = ServerPaths.cacheRoot()

    val backend: PreviewBackend
    var deviceSerial: String? = null
    var forwardPort: Int? = null
    var apkSha256: String? = null
    val recycleExecutor = Executors.newSingleThreadExecutor { r ->
      Thread(r, "android-design-sandbox-recycle").apply { isDaemon = true }
    }

    if (backendMode == RuntimeIdentity.BACKEND_ROBOLECTRIC) {
      val meta =
        BootstrapMeta.locateOrNull()
          ?: run {
            val msg =
              PreviewJson.compact.encodeToString(
                ErrorEnvelope.serializer(),
                ErrorEnvelope(
                  error = "bootstrap_missing",
                  message = "Cannot find bootstrap-meta.properties for Robolectric host server",
                ),
              )
            out.println(msg)
            err.println(msg)
            recycleExecutor.shutdownNow()
            return V5ExitCode.NOT_READY
          }
      val cpOverride =
        System.getenv(RuntimeIdentity.ROBOLECTRIC_CP_ENV)?.takeIf { it.isNotBlank() }?.let(Path::of)
      val robolectricCp = cpOverride ?: meta.bootstrapDir.resolve("robolectric.cp")
      if (!Files.isRegularFile(robolectricCp)) {
        val msg =
          PreviewJson.compact.encodeToString(
            ErrorEnvelope.serializer(),
            ErrorEnvelope(
              error = "bootstrap_missing",
              message =
                "Missing robolectric.cp at $robolectricCp; run `./gradlew :host:exportBootstrap` from app/",
            ),
          )
        out.println(msg)
        err.println(msg)
        recycleExecutor.shutdownNow()
        return V5ExitCode.NOT_READY
      }
      val roboBackend = RobolectricPreviewBackend(meta = meta, robolectricCp = robolectricCp)
      try {
        roboBackend.warmUp()
      } catch (e: Exception) {
        roboBackend.close()
        val msg =
          PreviewJson.compact.encodeToString(
            ErrorEnvelope.serializer(),
            ErrorEnvelope(
              error = "robolectric_init_failed",
              message = e.message ?: "Failed warming Robolectric worker",
            ),
          )
        out.println(msg)
        err.println(msg)
        recycleExecutor.shutdownNow()
        return V5ExitCode.NOT_READY
      }
      backend = roboBackend
    } else {
      val meta =
        BootstrapMeta.locateOrNull()
          ?: run {
            val msg =
              PreviewJson.compact.encodeToString(
                ErrorEnvelope.serializer(),
                ErrorEnvelope(
                  error = "bootstrap_missing",
                  message = "Cannot find bootstrap-meta.properties for Android host server",
                ),
              )
            out.println(msg)
            err.println(msg)
            recycleExecutor.shutdownNow()
            return V5ExitCode.NOT_READY
          }
      val devMgr = DeviceManager(cacheRoot = cacheRoot)
      val headless = parsed.flags["--headless"].equals("true", ignoreCase = true)
      val forceReinstall = parsed.flags["--force-reinstall-apk"].equals("true", ignoreCase = true)
      val setup =
        devMgr.ensureDeviceReady(
          meta = meta,
          requestedSerial = parsed.flags["--device-serial"],
          requestedAvd = parsed.flags["--avd"],
          headless = headless,
          forceReinstall = forceReinstall,
        )
      when (setup) {
        is DeviceSetupResult.Failed -> {
          val msg =
            PreviewJson.compact.encodeToString(
              ErrorEnvelope.serializer(),
              ErrorEnvelope(error = setup.error, message = setup.message),
            )
          out.println(msg)
          err.println(msg)
          recycleExecutor.shutdownNow()
          return setup.exitCode
        }
        is DeviceSetupResult.Ready -> {
          deviceSerial = setup.serial
          forwardPort = setup.forwardPort
          apkSha256 = setup.apkSha256
          val currentPort = AtomicInteger(setup.forwardPort)
          lateinit var transport: AdbTcpDeviceTransport
          transport =
            AdbTcpDeviceTransport(
              connectAndEnsurePort = {
                val ping = DeviceManager.pingLocalForward(currentPort.get())
                if (ping != null && ping.ok) {
                  currentPort.get()
                } else {
                  val refreshed =
                    devMgr.ensureDeviceReady(meta = meta, requestedSerial = setup.serial)
                  if (refreshed is DeviceSetupResult.Ready) {
                    currentPort.set(refreshed.forwardPort)
                    refreshed.forwardPort
                  } else {
                    currentPort.get()
                  }
                }
              },
              restartSandboxActivity = { devMgr.restartSandboxOnSerial(setup.serial) },
            )
          backend =
            AndroidPreviewBackend(
              meta = meta,
              transport = transport,
              onRecycleRecommended = {
                recycleExecutor.execute { runCatching { transport.recycle() } }
              },
            )
        }
      }
    }

    val latch = CountDownLatch(1)
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
        backend = backend,
        runtimeIdentity = runtimeIdentity,
        deviceSerial = deviceSerial,
        adbForwardPort = forwardPort,
        apkSha256 = apkSha256,
        onProcessExit = { latch.countDown() },
      )
    val record = server.start()
    Runtime.getRuntime()
      .addShutdownHook(
        Thread {
          server.stop(triggerExit = false)
          recycleExecutor.shutdownNow()
          latch.countDown()
        },
      )
    out.println(
      """{"ok":true,"pid":${record.pid},"port":${record.port},"backend":"${record.backend}","boardUrl":"${record.boardUrl(ServerPaths.repoHash(repoRoot))}"}""",
    )
    out.flush()
    try {
      latch.await()
    } catch (_: InterruptedException) {
      server.stop(triggerExit = false)
    } finally {
      recycleExecutor.shutdownNow()
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
