package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.android.AdbTcpDeviceTransport
import bot.charlies.androiddesign.preview.app.android.DeviceManager
import bot.charlies.androiddesign.preview.app.android.DeviceSetupResult
import bot.charlies.androiddesign.preview.app.cli.Command
import bot.charlies.androiddesign.preview.app.cli.ExitCode
import bot.charlies.androiddesign.preview.app.cli.parseArgs
import bot.charlies.androiddesign.preview.app.client.ClientExitCode
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.model.PreviewJson
import java.io.PrintStream
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/** Internal subcommand launched by the CLI client to run the single shared HTTP/SSE host server. */
class ServerCommand : Command {
  override val name = "server"
  override val usage =
    "server --repo <dir> [--preferred-port <port>] [--view-token <token>] [--idle-timeout-ms <ms>] " +
      "[--backend android] [--runtime-hash <hash>] [--device-serial <id>] [--avd <name>] " +
      "[--headless true|false] [--force-reinstall-apk true|false] [--owned-emulator-serial <id>]"

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
          "--owned-emulator-serial",
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
    if (backendMode != RuntimeIdentity.BACKEND_ANDROID) {
      val msg =
        PreviewJson.compact.encodeToString(
          ErrorEnvelope.serializer(),
          ErrorEnvelope(
            error = "invalid_backend",
            message = "Unsupported backend '$backendMode'; expected 'android'",
          ),
        )
      out.println(msg)
      err.println(msg)
      return ExitCode.USAGE
    }
    val coldBudgetMs = System.getenv(COLD_BUDGET_ENV)?.toLongOrNull() ?: Watchdog.COLD_BUDGET_MS
    val warmBudgetMs = System.getenv(WARM_BUDGET_ENV)?.toLongOrNull() ?: Watchdog.WARM_BUDGET_MS
    val env = System.getenv().toMutableMap().apply { put(RuntimeIdentity.BACKEND_ENV, backendMode) }
    parsed.flags["--runtime-hash"]
      ?.takeIf { it.isNotBlank() }
      ?.let {
        env[RuntimeIdentity.RUNTIME_HASH_ENV] = it
      }
    val runtimeIdentity = RuntimeIdentity.compute(env)
    val cacheRoot = ServerPaths.cacheRoot()

    val worker: RenderWorker
    val deviceSerial: String?
    val forwardPort: Int?
    val apkSha256: String?
    val onLaunchLiveActivity: () -> Unit
    val recycleExecutor = Executors.newSingleThreadExecutor { r ->
      Thread(r, "android-design-sandbox-recycle").apply { isDaemon = true }
    }
    // An emulator this server launched (or inherited from a predecessor it replaced) goes down
    // with it, unless a newer version replaced this server and reuses the running device. The
    // single shutdown hook is registered before device setup so a SIGTERM mid-boot still stops it.
    // Devices the user started are never touched.
    val inheritedOwnedSerial =
      parsed.flags["--owned-emulator-serial"]?.trim()?.takeIf { it.isNotBlank() }
    val pendingInheritedSerial = AtomicReference<String?>(inheritedOwnedSerial)
    val emulatorLauncher = AtomicReference<DeviceManager?>(null)
    val runningServer = AtomicReference<PreviewHttpServer?>(null)
    val emulatorReleased = AtomicBoolean(false)
    val latch = CountDownLatch(1)
    val releaseLaunchedEmulator = {
      val manager = emulatorLauncher.get()
      if (
        manager != null &&
          runningServer.get()?.wasReplaced != true &&
          emulatorReleased.compareAndSet(false, true)
      ) {
        runCatching { manager.releaseLaunchedEmulator() }
        pendingInheritedSerial.getAndSet(null)?.let { serial ->
          runCatching { manager.releaseEmulatorSerial(serial) }
        }
      }
    }
    Runtime.getRuntime()
      .addShutdownHook(
        Thread {
          runningServer.get()?.stop(triggerExit = false)
          recycleExecutor.shutdownNow()
          releaseLaunchedEmulator()
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
                message = "Cannot find bootstrap-meta.properties for Android host server",
              ),
            )
          out.println(msg)
          err.println(msg)
          recycleExecutor.shutdownNow()
          return ClientExitCode.NOT_READY
        }
    val devMgr = DeviceManager(cacheRoot = cacheRoot)
    emulatorLauncher.set(devMgr)
    val headless = parsed.flags["--headless"].equals("true", ignoreCase = true)
    val forceReinstall = parsed.flags["--force-reinstall-apk"].equals("true", ignoreCase = true)
    out.println("startup: preparing Android device (boot, install, warm-up)")
    out.flush()
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
        pendingInheritedSerial.getAndSet(null)?.let { serial ->
          runCatching { devMgr.releaseEmulatorSerial(serial) }
        }
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
        pendingInheritedSerial.getAndSet(null)?.let { serial ->
          if (setup.serial == serial) {
            devMgr.adoptOwnedEmulator(serial)
          } else {
            runCatching { devMgr.releaseEmulatorSerial(serial) }
          }
        }
        deviceSerial = setup.serial
        forwardPort = setup.forwardPort
        apkSha256 = setup.apkSha256
        onLaunchLiveActivity = { devMgr.launchLiveActivity(setup.serial) }
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
        worker =
          AndroidRenderWorker(
            meta = meta,
            transport = transport,
            onRecycleRecommended = {
              recycleExecutor.execute { runCatching { transport.recycle() } }
            },
          )
      }
    }

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
        deviceSerial = deviceSerial,
        adbForwardPort = forwardPort,
        apkSha256 = apkSha256,
        ownedEmulatorSerial = devMgr.ownedEmulatorSerial(),
        onWatchdogHalt = watchdogHaltAction(releaseLaunchedEmulator),
        onProcessExit = { latch.countDown() },
        onLaunchLiveActivity = onLaunchLiveActivity,
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
      recycleExecutor.shutdownNow()
      releaseLaunchedEmulator()
    }
    return ExitCode.OK
  }

  companion object {
    const val IDLE_TIMEOUT_ENV = "ANDROID_DESIGN_IDLE_TIMEOUT_MS"
    const val MAX_COMPILES_ENV = "ANDROID_DESIGN_MAX_COMPILES"
    const val COLD_BUDGET_ENV = "ANDROID_DESIGN_WATCHDOG_COLD_MS"
    const val WARM_BUDGET_ENV = "ANDROID_DESIGN_WATCHDOG_WARM_MS"

    /** Releases any managed emulator before `Runtime.halt` skips JVM shutdown hooks. */
    internal fun watchdogHaltAction(
      releaseEmulator: () -> Unit,
      haltProcess: () -> Unit = { Runtime.getRuntime().halt(1) },
    ): () -> Unit = {
      runCatching { releaseEmulator() }
      haltProcess()
    }
  }
}
