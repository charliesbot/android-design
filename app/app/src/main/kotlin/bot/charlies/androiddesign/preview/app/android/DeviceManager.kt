package bot.charlies.androiddesign.preview.app.android

import bot.charlies.androiddesign.preview.app.client.ClientExitCode
import bot.charlies.androiddesign.preview.app.server.ContentHasher
import bot.charlies.androiddesign.preview.app.server.ServerPaths
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.model.DevicePingResponse
import bot.charlies.androiddesign.preview.model.DeviceTransportSpec
import bot.charlies.androiddesign.preview.model.DeviceWireCodec
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.isWorkerStuck
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlinx.serialization.Serializable

data class CommandOutput(
  val exitCode: Int,
  val stdout: String,
  val stderr: String = "",
)

fun interface ExternalCommandRunner {
  fun run(command: List<String>, timeoutMs: Long, envOverrides: Map<String, String>): CommandOutput
}

object ProcessCommandRunner : ExternalCommandRunner {
  override fun run(
    command: List<String>,
    timeoutMs: Long,
    envOverrides: Map<String, String>,
  ): CommandOutput {
    val pb = ProcessBuilder(command)
    pb.environment().putAll(envOverrides)
    val proc = pb.start()
    proc.outputStream.close()
    var stdout = ""
    var stderr = ""
    val outThread = Thread {
      stdout = proc.inputStream.bufferedReader(StandardCharsets.UTF_8).readText()
    }
      .apply { isDaemon = true }
    val errThread = Thread {
      stderr = proc.errorStream.bufferedReader(StandardCharsets.UTF_8).readText()
    }
      .apply { isDaemon = true }
    outThread.start()
    errThread.start()
    val finished =
      try {
        proc.waitFor(timeoutMs, TimeUnit.MILLISECONDS)
      } catch (_: InterruptedException) {
        proc.destroyForcibly()
        Thread.currentThread().interrupt()
        return CommandOutput(
          exitCode = -1,
          stdout = stdout,
          stderr = "Command interrupted: ${command.joinToString(" ")}",
        )
      }
    if (!finished) {
      proc.destroyForcibly()
      runCatching { outThread.join(1_000) }
      runCatching { errThread.join(1_000) }
      return CommandOutput(
        exitCode = -1,
        stdout = stdout,
        stderr = "Command timed out: ${command.joinToString(" ")}",
      )
    }
    try {
      outThread.join(5_000)
      errThread.join(5_000)
    } catch (_: InterruptedException) {
      Thread.currentThread().interrupt()
    }
    return CommandOutput(proc.exitValue(), stdout, stderr)
  }
}

/**
 * Starts [command] detached from this process's stdio, for long-lived children like the emulator.
 */
private fun startDetached(command: List<String>, env: Map<String, String>): Process {
  val builder =
    ProcessBuilder(command)
      .redirectInput(ProcessBuilder.Redirect.from(File("/dev/null")))
      .redirectOutput(ProcessBuilder.Redirect.DISCARD)
      .redirectError(ProcessBuilder.Redirect.DISCARD)
  builder.environment().putAll(env)
  return builder.start()
}

sealed interface DeviceSetupResult {
  data class Ready(
    val serial: String,
    val avdName: String?,
    val forwardPort: Int,
    val apkInstalled: Boolean,
    val aotCompiled: Boolean,
    val apkSha256: String,
    val sandboxPid: Int,
    /** True only when this call started the AVD, so the server may shut it down on exit. */
    val bootedEmulator: Boolean = false,
  ) : DeviceSetupResult

  data class Failed(
    val exitCode: Int,
    val error: String,
    val message: String,
  ) : DeviceSetupResult
}

@Serializable
private data class InstalledApkStamp(
  val apkSha256: String,
  val packagePath: String,
  val installedAt: Long,
)

/**
 * Supervises AVD lifecycle via `android emulator` / `android install` plus `adb` AOT compilation,
 * background idle exemptions, `adb forward`, and `:sandbox` `LivePreviewActivity` startup for
 * opt-in live mode.
 */
class DeviceManager(
  private val cacheRoot: Path = ServerPaths.cacheRoot(),
  private val env: Map<String, String> = System.getenv(),
  private val runner: ExternalCommandRunner = ProcessCommandRunner,
  private val portAllocator: () -> Int = { findPreferredOrFreePort(DEFAULT_DEVICE_PORT) },
  private val pingFn: (Int) -> DevicePingResponse? = ::pingLocalForward,
  private val sleepFn: (Long) -> Unit = Thread::sleep,
  private val nowMs: () -> Long = System::currentTimeMillis,
  /**
   * Starts the SDK emulator detached. Its launcher execs into qemu, so the process is the emulator.
   */
  private val launchEmulator: (List<String>, Map<String, String>) -> Process = ::startDetached,
) {
  /**
   * The emulator this manager launched, kept so it can be shut down again. [owned] is set only when
   * the serial that came online runs the launched AVD while the launched process is still alive.
   */
  private class LaunchedEmulator(val avdName: String, val process: Process?) {
    @Volatile var serial: String? = null
    @Volatile var owned: Boolean = false
  }

  @Volatile private var launched: LaunchedEmulator? = null
  @Volatile private var released: Boolean = false

  /** Returns the serial of the emulator owned by this manager, if any. */
  fun ownedEmulatorSerial(): String? = synchronized(this) { launched?.takeIf { it.owned }?.serial }

  /**
   * Adopts ownership of an already-running emulator booted by a predecessor server so this manager
   * shuts it down on exit.
   */
  fun adoptOwnedEmulator(serial: String) {
    val trimmed = serial.trim()
    if (trimmed.isEmpty()) return
    val shutDownNow =
      synchronized(this) {
        if (released) {
          true
        } else {
          if (launched == null) {
            launched =
              LaunchedEmulator(avdName = "", process = null).apply {
                this.serial = trimmed
                this.owned = true
              }
          }
          false
        }
      }
    if (shutDownNow) shutdownEmulator(trimmed)
  }

  /** Shuts down a specific emulator serial that was owned by a predecessor server. */
  fun releaseEmulatorSerial(serial: String): Boolean = shutdownEmulator(serial)

  data class DeviceSlot(
    val serial: String,
    val state: String,
    val avdName: String? = null,
  )

  /**
   * Checks a device request without booting or installing anything: adb is present, a requested
   * serial is online, and a requested AVD is online or exists. `start` calls it so bad input fails
   * fast in the CLI before the server, which does the actual setup, is started.
   */
  fun validateRequest(requestedSerial: String?, requestedAvd: String?): DeviceSetupResult.Failed? {
    val sdkDir = resolveSdkDir()
    val adbBin = resolveAdbBinary(sdkDir) ?: return adbNotFound()
    val cmdEnv = sdkEnv(sdkDir)
    if (!requestedSerial.isNullOrBlank()) {
      val wanted = requestedSerial.trim()
      val online = listOnlineDevices(adbBin, cmdEnv)
      return if (online.any { it.serial == wanted }) null else unknownSerial(wanted, online)
    }
    if (!requestedAvd.isNullOrBlank()) {
      val wanted = requestedAvd.trim()
      val onlineMatch =
        listOnlineDevices(adbBin, cmdEnv).any {
          (it.avdName ?: queryAvdName(adbBin, it.serial, cmdEnv)) == wanted
        }
      if (onlineMatch) return null
      val avds = listAvds(resolveAndroidCli(), sdkDir, cmdEnv)
      return if (wanted in avds) null else unknownAvd(wanted, avds)
    }
    return null
  }

  fun ensureDeviceReady(
    meta: BootstrapMeta,
    requestedSerial: String? = null,
    requestedAvd: String? = null,
    forceReinstall: Boolean = false,
  ): DeviceSetupResult {
    val launchedBefore = launched
    val result = prepareDevice(meta, requestedSerial, requestedAvd, forceReinstall)
    // An emulator this call launched must not outlive its failed setup.
    if (result is DeviceSetupResult.Failed && launched !== launchedBefore) {
      releaseLaunchedEmulator(markShuttingDown = false)
    }
    return result
  }

  /**
   * Shuts down the emulator this manager launched: `emu kill` on its serial when it owns that
   * device, and the launched process tree itself if it is still running. Devices it did not launch
   * are never touched. Returns whether anything was stopped.
   */
  fun releaseLaunchedEmulator(markShuttingDown: Boolean = true): Boolean {
    val emulator =
      synchronized(this) {
        if (markShuttingDown) released = true
        launched.also { launched = null }
      } ?: return false
    var stopped = false
    val serial = emulator.serial
    if (emulator.owned && serial != null) stopped = shutdownEmulator(serial)
    val process = emulator.process
    if (process != null && terminateProcessTree(process)) {
      stopped = true
    }
    return stopped
  }

  private fun terminateProcessTree(process: Process): Boolean {
    val descendants = runCatching { process.descendants().toList() }.getOrDefault(emptyList())
    val wasAlive = process.isAlive || descendants.any { it.isAlive }
    if (!wasAlive) return false
    descendants.forEach { runCatching { it.destroy() } }
    if (process.isAlive) runCatching { process.destroy() }
    val exited = runCatching {
      process.waitFor(EMULATOR_EXIT_WAIT_MS, TimeUnit.MILLISECONDS)
    }
      .getOrDefault(false)
    if (!exited) {
      descendants.forEach { runCatching { if (it.isAlive) it.destroyForcibly() } }
      runCatching { if (process.isAlive) process.destroyForcibly() }
    }
    return true
  }

  private fun prepareDevice(
    meta: BootstrapMeta,
    requestedSerial: String?,
    requestedAvd: String?,
    forceReinstall: Boolean,
  ): DeviceSetupResult {
    val sdkDir = resolveSdkDir()
    val adbBin = resolveAdbBinary(sdkDir) ?: return adbNotFound()
    val androidCli = resolveAndroidCli()
    val cmdEnv = sdkEnv(sdkDir)

    val onlineDevices = listOnlineDevices(adbBin, cmdEnv)
    val chosenSerial: String
    var bootedEmulator = false
    var chosenAvd: String? = requestedAvd

    if (!requestedSerial.isNullOrBlank()) {
      val wanted = requestedSerial.trim()
      val match = onlineDevices.firstOrNull { it.serial == wanted }
      if (match == null) return unknownSerial(wanted, onlineDevices)
      chosenSerial = match.serial
      chosenAvd = match.avdName ?: queryAvdName(adbBin, chosenSerial, cmdEnv)
    } else if (!requestedAvd.isNullOrBlank()) {
      val wanted = requestedAvd.trim()
      val annotated = onlineDevices.map { slot ->
        val avd = slot.avdName ?: queryAvdName(adbBin, slot.serial, cmdEnv)
        slot.copy(avdName = avd)
      }
      val onlineMatch = annotated.firstOrNull { it.avdName == wanted }
      if (onlineMatch != null) {
        chosenSerial = onlineMatch.serial
        chosenAvd = onlineMatch.avdName
      } else {
        val avds = listAvds(androidCli, sdkDir, cmdEnv)
        if (wanted !in avds) return unknownAvd(wanted, avds)
        chosenAvd = wanted
        chosenSerial =
          startAvdAndWaitOnline(androidCli, adbBin, sdkDir, wanted, cmdEnv)
            ?: return DeviceSetupResult.Failed(
              exitCode = ClientExitCode.FAILED,
              error = "emulator_start_failed",
              message = "Failed starting AVD '$wanted'.",
            )
        bootedEmulator = launched?.owned == true
      }
    } else if (onlineDevices.isNotEmpty()) {
      val annotated = onlineDevices.map { slot ->
        val avd = slot.avdName ?: queryAvdName(adbBin, slot.serial, cmdEnv)
        slot.copy(avdName = avd)
      }
      val best = pickPreferredOnlineDevice(annotated)
      chosenSerial = best.serial
      chosenAvd = best.avdName
    } else {
      val avds = listAvds(androidCli, sdkDir, cmdEnv)
      val targetAvd =
        selectPreferredAvd(avds)
          ?: createDefaultAvd(androidCli, cmdEnv).let { created ->
            if (created == null) {
              return DeviceSetupResult.Failed(
                exitCode =
                  if (androidCli == null) ClientExitCode.NOT_READY else ClientExitCode.FAILED,
                error = if (androidCli == null) "android_cli_not_found" else "avd_create_failed",
                message =
                  if (androidCli == null) {
                    "No AVDs exist and `android` CLI was not found on PATH to create `medium_phone`."
                  } else {
                    "Failed creating `medium_phone` AVD via `android emulator create $DEFAULT_AVD_PROFILE`."
                  },
              )
            }
            created
          }
      chosenAvd = targetAvd
      val started =
        startAvdAndWaitOnline(androidCli, adbBin, sdkDir, targetAvd, cmdEnv)
          ?: return DeviceSetupResult.Failed(
            exitCode = ClientExitCode.FAILED,
            error = "emulator_start_failed",
            message = "Failed starting AVD '$targetAvd'.",
          )
      chosenSerial = started
      bootedEmulator = launched?.owned == true
    }

    if (!waitUntilBootCompleted(adbBin, chosenSerial, cmdEnv)) {
      return DeviceSetupResult.Failed(
        exitCode = ClientExitCode.FAILED,
        error = "boot_timeout",
        message =
          "Device $chosenSerial did not report sys.boot_completed=1 within " +
            "${BOOT_COMPLETED_TIMEOUT_MS / 1000}s.",
      )
    }

    val apkSha256 = meta.apkSha256OrCompute()
    val pmPath = queryInstalledPackagePath(adbBin, chosenSerial, cmdEnv)
    val stampFile = stampPath(chosenSerial)
    val existingStamp = readStamp(stampFile)
    val needsInstall =
      forceReinstall ||
        pmPath == null ||
        existingStamp == null ||
        existingStamp.apkSha256 != apkSha256 ||
        existingStamp.packagePath != pmPath

    var installedNow = false
    var aotNow = false
    if (needsInstall) {
      val installOk = installApk(androidCli, adbBin, chosenSerial, meta.apkPath, cmdEnv)
      if (!installOk) {
        return DeviceSetupResult.Failed(
          exitCode = ClientExitCode.FAILED,
          error = "apk_install_failed",
          message = "Failed installing ${meta.apkPath} on device $chosenSerial.",
        )
      }
      installedNow = true
      val aotOut =
        runner.run(
          listOf(
            adbBin,
            "-s",
            chosenSerial,
            "shell",
            "cmd",
            "package",
            "compile",
            "-m",
            "speed",
            "-f",
            HOST_PACKAGE,
          ),
          AOT_TIMEOUT_MS,
          cmdEnv,
        )
      aotNow = aotOut.exitCode == 0
      if (!aotNow) {
        return DeviceSetupResult.Failed(
          exitCode = ClientExitCode.FAILED,
          error = "aot_compile_failed",
          message =
            "AOT compile failed on $chosenSerial: ${(aotOut.stdout + aotOut.stderr).trim()}",
        )
      }
      val newPmPath = queryInstalledPackagePath(adbBin, chosenSerial, cmdEnv) ?: pmPath.orEmpty()
      writeStamp(stampFile, InstalledApkStamp(apkSha256, newPmPath, System.currentTimeMillis()))
    }

    // Apply background idle exemptions every start so :sandbox stays reachable while open.
    runner.run(
      listOf(
        adbBin,
        "-s",
        chosenSerial,
        "shell",
        "dumpsys",
        "deviceidle",
        "whitelist",
        "+$HOST_PACKAGE",
      ),
      ADB_SHORT_TIMEOUT_MS,
      cmdEnv,
    )
    runner.run(
      listOf(
        adbBin,
        "-s",
        chosenSerial,
        "shell",
        "cmd",
        "appops",
        "set",
        HOST_PACKAGE,
        "RUN_ANY_IN_BACKGROUND",
        "allow",
      ),
      ADB_SHORT_TIMEOUT_MS,
      cmdEnv,
    )

    val forwardPort =
      ensureAdbForward(adbBin, chosenSerial, cmdEnv)
        ?: return DeviceSetupResult.Failed(
          exitCode = ClientExitCode.FAILED,
          error = "adb_forward_failed",
          message = "Failed forwarding host TCP port to device $chosenSerial:$DEVICE_REMOTE.",
        )

    var ping = pingFn(forwardPort)
    if (ping != null && ping.isWorkerStuck()) {
      killWedgedSandbox(adbBin, chosenSerial, cmdEnv, ping)
    }
    if (ping == null || !ping.ok || installedNow || ping.isWorkerStuck()) {
      if (!launchSandboxActivity(adbBin, chosenSerial, cmdEnv)) {
        return DeviceSetupResult.Failed(
          exitCode = ClientExitCode.FAILED,
          error = "sandbox_start_failed",
          message = "Failed starting $LIVE_ACTIVITY on $chosenSerial.",
        )
      }
      ping = awaitPing(forwardPort)
      if (ping == null || !ping.ok) {
        return DeviceSetupResult.Failed(
          exitCode = ClientExitCode.FAILED,
          error = "sandbox_ping_timeout",
          message =
            "Timed out waiting for RenderForegroundService ping on $chosenSerial (port $forwardPort).",
        )
      }
    }

    return DeviceSetupResult.Ready(
      serial = chosenSerial,
      avdName = chosenAvd,
      forwardPort = forwardPort,
      apkInstalled = installedNow,
      aotCompiled = aotNow,
      apkSha256 = apkSha256,
      sandboxPid = ping.pid,
      bootedEmulator = bootedEmulator,
    )
  }

  /**
   * Shuts down the emulator at [serial] with `adb emu kill`. Callers pass only a serial this server
   * booted ([DeviceSetupResult.Ready.bootedEmulator]); devices the user started stay up.
   */
  private fun shutdownEmulator(serial: String): Boolean {
    val sdkDir = resolveSdkDir()
    val adbBin = resolveAdbBinary(sdkDir) ?: return false
    val cmdEnv = buildMap {
      sdkDir?.let {
        put("ANDROID_HOME", it.toString())
        put("ANDROID_SDK_ROOT", it.toString())
      }
    }
    return runner
      .run(listOf(adbBin, "-s", serial, "emu", "kill"), ADB_SHORT_TIMEOUT_MS, cmdEnv)
      .exitCode == 0
  }

  fun launchLiveActivity(serial: String? = null): Boolean {
    val sdkDir = resolveSdkDir()
    val adbBin = resolveAdbBinary(sdkDir) ?: return false
    val cmdEnv = buildMap {
      sdkDir?.let {
        put("ANDROID_HOME", it.toString())
        put("ANDROID_SDK_ROOT", it.toString())
      }
    }
    val targetSerial =
      serial ?: listOnlineDevices(adbBin, cmdEnv).firstOrNull()?.serial ?: return false
    val res =
      runner.run(
        listOf(adbBin, "-s", targetSerial, "shell", "am", "start", "-W", "-n", LIVE_ACTIVITY),
        ADB_SHORT_TIMEOUT_MS,
        cmdEnv,
      )
    return res.exitCode == 0
  }

  fun restartSandboxOnSerial(serial: String): Boolean {
    val sdkDir = resolveSdkDir()
    val adbBin = resolveAdbBinary(sdkDir) ?: return false
    val cmdEnv = buildMap {
      sdkDir?.let {
        put("ANDROID_HOME", it.toString())
        put("ANDROID_SDK_ROOT", it.toString())
      }
    }
    val port = ensureAdbForward(adbBin, serial, cmdEnv) ?: return false
    pingFn(port)
      ?.takeIf { it.isWorkerStuck() }
      ?.let { killWedgedSandbox(adbBin, serial, cmdEnv, it) }
    if (!launchSandboxActivity(adbBin, serial, cmdEnv)) return false
    return awaitPing(port)?.ok == true
  }

  /**
   * A ping is answered on a client thread, so it stays healthy while the single worker is stuck in
   * user code. When the in-flight op outlived its budget the in-process watchdog failed, so the
   * host kills the whole package process before relaunching.
   */
  private fun killWedgedSandbox(
    adbBin: String,
    serial: String,
    cmdEnv: Map<String, String>,
    ping: DevicePingResponse,
  ) {
    System.err.println(
      "adp: sandbox worker stuck in ${ping.inFlightOp} for ${ping.inFlightAgeMs}ms " +
        "(budget ${ping.inFlightBudgetMs}ms); force-stopping $HOST_PACKAGE",
    )
    runner.run(
      listOf(adbBin, "-s", serial, "shell", "am", "force-stop", HOST_PACKAGE),
      ADB_SHORT_TIMEOUT_MS,
      cmdEnv,
    )
  }

  internal fun listOnlineDevices(adbBin: String, cmdEnv: Map<String, String>): List<DeviceSlot> {
    val out = runner.run(listOf(adbBin, "devices", "-l"), ADB_SHORT_TIMEOUT_MS, cmdEnv)
    if (out.exitCode != 0) return emptyList()
    return parseAdbDevices(out.stdout)
  }

  internal fun parseAdbDevices(stdout: String): List<DeviceSlot> =
    stdout
      .lineSequence()
      .map { it.trim() }
      .filter { it.isNotEmpty() && !it.startsWith("List of devices") && !it.startsWith("*") }
      .mapNotNull { line ->
        val parts = line.split(Regex("""\s+"""))
        if (parts.size < 2) return@mapNotNull null
        val serial = parts[0]
        val state = parts[1]
        if (state != "device") return@mapNotNull null
        DeviceSlot(serial = serial, state = state)
      }
      .toList()

  internal fun selectPreferredAvd(avds: List<String>): String? {
    val cleaned = avds.map { it.trim() }.filter { it.isNotEmpty() }
    if (cleaned.isEmpty()) return null
    return cleaned.firstOrNull { "resizable" in it.lowercase() }
      ?: cleaned.firstOrNull { "wear" !in it.lowercase() }
      ?: cleaned.first()
  }

  private fun pickPreferredOnlineDevice(devices: List<DeviceSlot>): DeviceSlot =
    devices.firstOrNull { d -> d.avdName?.lowercase()?.contains("resizable") == true }
      ?: devices.firstOrNull { d -> d.avdName?.lowercase()?.contains("wear") != true }
      ?: devices.first()

  private fun queryAvdName(adbBin: String, serial: String, cmdEnv: Map<String, String>): String? {
    if (!serial.startsWith("emulator-")) return null
    val res =
      runner.run(
        listOf(adbBin, "-s", serial, "emu", "avd", "name"),
        ADB_SHORT_TIMEOUT_MS,
        cmdEnv,
      )
    if (res.exitCode != 0) return null
    return res.stdout
      .lineSequence()
      .map { it.trim() }
      .firstOrNull { it.isNotEmpty() && !it.equals("OK", ignoreCase = true) }
  }

  private fun listAvds(
    androidCli: String?,
    sdkDir: Path?,
    cmdEnv: Map<String, String>,
  ): List<String> {
    if (androidCli != null) {
      val res = runner.run(listOf(androidCli, "emulator", "list"), ADB_SHORT_TIMEOUT_MS, cmdEnv)
      if (res.exitCode == 0) {
        return res.stdout.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
      }
    }
    val emulatorBin = sdkDir?.resolve("emulator/emulator")?.takeIf { Files.isExecutable(it) }
    if (emulatorBin != null) {
      val res =
        runner.run(listOf(emulatorBin.toString(), "-list-avds"), ADB_SHORT_TIMEOUT_MS, cmdEnv)
      if (res.exitCode == 0) {
        return res.stdout.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
      }
    }
    return emptyList()
  }

  private fun createDefaultAvd(androidCli: String?, cmdEnv: Map<String, String>): String? {
    if (androidCli == null) return null
    val res =
      runner.run(
        listOf(androidCli, "emulator", "create", DEFAULT_AVD_PROFILE),
        BOOT_TIMEOUT_MS,
        cmdEnv,
      )
    if (res.exitCode != 0) return null
    val avds = listAvds(androidCli, resolveSdkDir(), cmdEnv)
    return avds.firstOrNull { DEFAULT_AVD_PROFILE in it.lowercase() }
      ?: avds.lastOrNull()
      ?: DEFAULT_AVD_PROFILE
  }

  private fun startAvdAndWaitOnline(
    androidCli: String?,
    adbBin: String,
    sdkDir: Path?,
    avdName: String,
    cmdEnv: Map<String, String>,
  ): String? {
    if (synchronized(this) { released }) return null
    val before = listOnlineDevices(adbBin, cmdEnv).map { it.serial }.toSet()
    // Prefer the SDK emulator with the host GPU; `-no-snapshot-save` keeps shutdown from writing a
    // quickboot snapshot into the user's AVD. Live mode always opens a visible window.
    val emulatorBin =
      sdkDir?.resolve("emulator/emulator")?.takeIf { Files.isExecutable(it) }?.toString()
    if (emulatorBin != null) {
      val cmd = buildList {
        add(emulatorBin)
        add("-avd")
        add(avdName)
        add("-gpu")
        add("host")
        add("-no-snapshot-save")
      }
      if (synchronized(this) { released }) return null
      val proc = launchEmulator(cmd, cmdEnv)
      val aborted =
        synchronized(this) {
          if (released) {
            true
          } else {
            launched = LaunchedEmulator(avdName, proc)
            false
          }
        }
      if (aborted) {
        terminateProcessTree(proc)
        return null
      }
    } else if (androidCli != null) {
      val cmd = listOf(androidCli, "emulator", "start", avdName)
      val aborted =
        synchronized(this) {
          if (released) {
            true
          } else {
            launched = LaunchedEmulator(avdName, process = null)
            false
          }
        }
      if (aborted) return null
      val startOut = runner.run(cmd, BOOT_TIMEOUT_MS, cmdEnv)
      if (startOut.exitCode != 0) return null
    } else {
      return null
    }

    val emulator = launched ?: return null
    val deadline = nowMs() + BOOT_WAIT_MS
    while (nowMs() < deadline) {
      if (released) return null
      val added = listOnlineDevices(adbBin, cmdEnv).filter { it.serial !in before }
      val ours = added.firstOrNull {
        (it.avdName ?: queryAvdName(adbBin, it.serial, cmdEnv)) == avdName
      }
      if (ours != null) {
        // A launch that already exited (the AVD was starting elsewhere) does not own the device.
        emulator.serial = ours.serial
        emulator.owned = emulator.process?.isAlive ?: true
        return ours.serial
      }
      if (emulator.process?.isAlive == false && added.isNotEmpty()) return added.first().serial
      sleepFn(250L)
    }
    return null
  }

  /** Returns false when the device never reports `sys.boot_completed=1` within the budget. */
  private fun waitUntilBootCompleted(
    adbBin: String,
    serial: String,
    cmdEnv: Map<String, String>,
  ): Boolean {
    runner.run(listOf(adbBin, "-s", serial, "wait-for-device"), ADB_SHORT_TIMEOUT_MS, cmdEnv)
    val deadline = nowMs() + BOOT_COMPLETED_TIMEOUT_MS
    while (nowMs() < deadline) {
      val prop =
        runner.run(
          listOf(adbBin, "-s", serial, "shell", "getprop", "sys.boot_completed"),
          ADB_SHORT_TIMEOUT_MS,
          cmdEnv,
        )
      if (prop.exitCode == 0 && prop.stdout.trim() == "1") return true
      sleepFn(250L)
    }
    return false
  }

  private fun queryInstalledPackagePath(
    adbBin: String,
    serial: String,
    cmdEnv: Map<String, String>,
  ): String? {
    val res =
      runner.run(
        listOf(adbBin, "-s", serial, "shell", "pm", "path", HOST_PACKAGE),
        ADB_SHORT_TIMEOUT_MS,
        cmdEnv,
      )
    if (res.exitCode != 0) return null
    return res.stdout
      .lineSequence()
      .map { it.trim() }
      .firstOrNull { it.startsWith("package:") }
      ?.removePrefix("package:")
  }

  private fun installApk(
    androidCli: String?,
    adbBin: String,
    serial: String,
    apkPath: Path,
    cmdEnv: Map<String, String>,
  ): Boolean {
    if (androidCli != null) {
      val res =
        runner.run(
          listOf(androidCli, "install", "--device=$serial", "--apks=$apkPath"),
          INSTALL_TIMEOUT_MS,
          cmdEnv,
        )
      if (res.exitCode == 0) return true
    }
    val adbRes =
      runner.run(
        listOf(adbBin, "-s", serial, "install", "-r", apkPath.toString()),
        INSTALL_TIMEOUT_MS,
        cmdEnv,
      )
    return adbRes.exitCode == 0
  }

  private fun ensureAdbForward(
    adbBin: String,
    serial: String,
    cmdEnv: Map<String, String>,
  ): Int? {
    val listOut =
      runner.run(listOf(adbBin, "-s", serial, "forward", "--list"), ADB_SHORT_TIMEOUT_MS, cmdEnv)
    if (listOut.exitCode == 0) {
      val existing = parseForwardPort(listOut.stdout, serial)
      if (existing != null) return existing
    }
    val localPort = portAllocator()
    val fwdOut =
      runner.run(
        listOf(adbBin, "-s", serial, "forward", "tcp:$localPort", DEVICE_REMOTE),
        ADB_SHORT_TIMEOUT_MS,
        cmdEnv,
      )
    return if (fwdOut.exitCode == 0) localPort else null
  }

  internal fun parseForwardPort(stdout: String, serial: String): Int? {
    val targetRemote = DEVICE_REMOTE
    for (raw in stdout.lineSequence()) {
      val parts = raw.trim().split(Regex("""\s+"""))
      if (parts.size >= 3 && parts[0] == serial && parts[2] == targetRemote) {
        val local = parts[1]
        if (local.startsWith("tcp:")) {
          return local.removePrefix("tcp:").toIntOrNull()
        }
      }
    }
    return null
  }

  private fun launchSandboxActivity(
    adbBin: String,
    serial: String,
    cmdEnv: Map<String, String>,
  ): Boolean {
    val res =
      runner.run(
        listOf(adbBin, "-s", serial, "shell", "am", "start", "-W", "-n", LIVE_ACTIVITY),
        ADB_SHORT_TIMEOUT_MS,
        cmdEnv,
      )
    return res.exitCode == 0
  }

  private fun awaitPing(port: Int): DevicePingResponse? {
    val deadline = System.currentTimeMillis() + SANDBOX_START_TIMEOUT_MS
    while (System.currentTimeMillis() < deadline) {
      val ping = pingFn(port)
      if (ping != null && ping.ok) return ping
      sleepFn(150L)
    }
    return null
  }

  private fun sdkEnv(sdkDir: Path?): Map<String, String> = buildMap {
    sdkDir?.let {
      put("ANDROID_HOME", it.toString())
      put("ANDROID_SDK_ROOT", it.toString())
    }
  }

  private fun adbNotFound() =
    DeviceSetupResult.Failed(
      exitCode = ClientExitCode.NOT_READY,
      error = "adb_not_found",
      message = "Cannot find `adb`. Ensure Android SDK platform-tools are installed.",
    )

  private fun unknownSerial(wanted: String, online: List<DeviceSlot>) =
    DeviceSetupResult.Failed(
      exitCode = ClientExitCode.USAGE,
      error = "unknown_serial",
      message =
        "Device serial '$wanted' is not online (online: ${online.joinToString { it.serial }.ifBlank { "none" }})",
    )

  private fun unknownAvd(wanted: String, avds: List<String>) =
    DeviceSetupResult.Failed(
      exitCode = ClientExitCode.USAGE,
      error = "unknown_avd",
      message =
        "AVD '$wanted' does not exist (available: ${avds.joinToString().ifBlank { "none" }})",
    )

  private fun resolveSdkDir(): Path? {
    val candidates =
      listOfNotNull(
        env["ANDROID_HOME"]?.takeIf { it.isNotBlank() }?.let(Path::of),
        env["ANDROID_SDK_ROOT"]?.takeIf { it.isNotBlank() }?.let(Path::of),
        Path.of(System.getProperty("user.home"), "Library", "Android", "sdk"),
        Path.of(System.getProperty("user.home"), "Android", "Sdk"),
      )
    return candidates.firstOrNull { Files.isDirectory(it) }
  }

  private fun resolveAdbBinary(sdkDir: Path?): String? {
    env[ADB_ENV]
      ?.takeIf { it.isNotBlank() && Files.isExecutable(Path.of(it)) }
      ?.let {
        return it
      }
    sdkDir
      ?.resolve("platform-tools/adb")
      ?.takeIf { Files.isExecutable(it) }
      ?.let {
        return it.toString()
      }
    return findOnPath("adb", env)
  }

  private fun resolveAndroidCli(): String? {
    env[ANDROID_CLI_ENV]
      ?.takeIf { it.isNotBlank() && Files.isExecutable(Path.of(it)) }
      ?.let {
        return it
      }
    return findOnPath("android", env)
  }

  private fun stampPath(serial: String): Path {
    val hash = ContentHasher.sha256Hex(serial.toByteArray(Charsets.UTF_8)).take(16)
    return cacheRoot.resolve("device-apk").resolve("$hash.json")
  }

  private fun readStamp(path: Path): InstalledApkStamp? =
    try {
      if (!Files.isRegularFile(path)) null
      else
        PreviewJson.compact.decodeFromString(InstalledApkStamp.serializer(), Files.readString(path))
    } catch (_: Exception) {
      null
    }

  private fun writeStamp(path: Path, stamp: InstalledApkStamp) {
    runCatching {
      Files.createDirectories(path.parent)
      Files.writeString(
        path,
        PreviewJson.compact.encodeToString(InstalledApkStamp.serializer(), stamp),
      )
    }
  }

  companion object {
    const val HOST_PACKAGE = "bot.charlies.androiddesign.preview.host"
    const val LIVE_ACTIVITY = "$HOST_PACKAGE/.LivePreviewActivity"
    const val DEVICE_REMOTE = DeviceTransportSpec.ADB_REMOTE
    const val DEFAULT_DEVICE_PORT = 18765
    const val DEFAULT_AVD_PROFILE = "medium_phone"
    const val ADB_ENV = "ANDROID_DESIGN_ADB_BIN"
    const val ANDROID_CLI_ENV = "ANDROID_DESIGN_ANDROID_CLI"

    private const val ADB_SHORT_TIMEOUT_MS = 15_000L
    private const val BOOT_COMPLETED_TIMEOUT_MS = 60_000L
    private const val INSTALL_TIMEOUT_MS = 120_000L
    private const val AOT_TIMEOUT_MS = 120_000L
    private const val BOOT_TIMEOUT_MS = 180_000L
    private const val BOOT_WAIT_MS = 120_000L
    private const val EMULATOR_EXIT_WAIT_MS = 10_000L
    private const val SANDBOX_START_TIMEOUT_MS = 15_000L

    fun findPreferredOrFreePort(preferred: Int = DEFAULT_DEVICE_PORT): Int {
      try {
        ServerSocket(preferred, 0, InetAddress.getByName("127.0.0.1")).use {
          return it.localPort
        }
      } catch (_: Exception) {
        ServerSocket(0, 0, InetAddress.getByName("127.0.0.1")).use {
          return it.localPort
        }
      }
    }

    fun pingLocalForward(port: Int): DevicePingResponse? =
      try {
        Socket("127.0.0.1", port).use { sock ->
          sock.tcpNoDelay = true
          sock.soTimeout = 2_000
          val out = DataOutputStream(BufferedOutputStream(sock.getOutputStream()))
          val inp = DataInputStream(BufferedInputStream(sock.getInputStream()))
          DeviceWireCodec.writePingRequest(out)
          DeviceWireCodec.readPingResponse(inp)
        }
      } catch (_: Exception) {
        null
      }

    private fun findOnPath(name: String, env: Map<String, String>): String? {
      val pathEnv = env["PATH"] ?: System.getenv("PATH").orEmpty()
      for (entry in pathEnv.split(File.pathSeparator)) {
        if (entry.isBlank()) continue
        val candidate = Path.of(entry, name)
        if (Files.isRegularFile(candidate) && Files.isExecutable(candidate)) {
          return candidate.toAbsolutePath().normalize().toString()
        }
      }
      return null
    }
  }
}
