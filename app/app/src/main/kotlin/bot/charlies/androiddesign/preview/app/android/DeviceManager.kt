package bot.charlies.androiddesign.preview.app.android

import bot.charlies.androiddesign.preview.app.client.ClientExitCode
import bot.charlies.androiddesign.preview.app.server.ContentHasher
import bot.charlies.androiddesign.preview.app.server.ServerPaths
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.model.DevicePingResponse
import bot.charlies.androiddesign.preview.model.DeviceTransportSpec
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
    val errThread = Thread {
      stderr = proc.errorStream.bufferedReader(StandardCharsets.UTF_8).readText()
    }
    outThread.start()
    errThread.start()
    val finished = proc.waitFor(timeoutMs, TimeUnit.MILLISECONDS)
    if (!finished) {
      proc.destroyForcibly()
      outThread.join(1_000)
      errThread.join(1_000)
      return CommandOutput(
        exitCode = -1,
        stdout = stdout,
        stderr = "Command timed out: ${command.joinToString(" ")}",
      )
    }
    outThread.join(5_000)
    errThread.join(5_000)
    return CommandOutput(proc.exitValue(), stdout, stderr)
  }
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
 * background idle exemptions, `adb forward`, and `:sandbox` `HostRenderActivity` startup.
 */
class DeviceManager(
  private val cacheRoot: Path = ServerPaths.cacheRoot(),
  private val env: Map<String, String> = System.getenv(),
  private val runner: ExternalCommandRunner = ProcessCommandRunner,
  private val portAllocator: () -> Int = { findPreferredOrFreePort(DEFAULT_DEVICE_PORT) },
  private val pingFn: (Int) -> DevicePingResponse? = ::pingLocalForward,
  private val sleepFn: (Long) -> Unit = Thread::sleep,
  private val nowMs: () -> Long = System::currentTimeMillis,
) {
  data class DeviceSlot(
    val serial: String,
    val state: String,
    val avdName: String? = null,
  )

  fun ensureDeviceReady(
    meta: BootstrapMeta,
    requestedSerial: String? = null,
    requestedAvd: String? = null,
    headless: Boolean = false,
    forceReinstall: Boolean = false,
  ): DeviceSetupResult {
    val sdkDir = resolveSdkDir()
    val adbBin =
      resolveAdbBinary(sdkDir)
        ?: return DeviceSetupResult.Failed(
          exitCode = ClientExitCode.NOT_READY,
          error = "adb_not_found",
          message = "Cannot find `adb`. Ensure Android SDK platform-tools are installed.",
        )
    val androidCli = resolveAndroidCli()
    val cmdEnv = buildMap {
      sdkDir?.let {
        put("ANDROID_HOME", it.toString())
        put("ANDROID_SDK_ROOT", it.toString())
      }
    }

    val onlineDevices = listOnlineDevices(adbBin, cmdEnv)
    val chosenSerial: String
    var chosenAvd: String? = requestedAvd

    if (!requestedSerial.isNullOrBlank()) {
      val wanted = requestedSerial.trim()
      val match = onlineDevices.firstOrNull { it.serial == wanted }
      if (match == null) {
        return DeviceSetupResult.Failed(
          exitCode = ClientExitCode.USAGE,
          error = "unknown_serial",
          message =
            "Device serial '$wanted' is not online (online: ${onlineDevices.joinToString { it.serial }.ifBlank { "none" }})",
        )
      }
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
        if (wanted !in avds) {
          return DeviceSetupResult.Failed(
            exitCode = ClientExitCode.USAGE,
            error = "unknown_avd",
            message =
              "AVD '$wanted' does not exist (available: ${avds.joinToString().ifBlank { "none" }})",
          )
        }
        chosenAvd = wanted
        chosenSerial =
          startAvdAndWaitOnline(androidCli, adbBin, sdkDir, wanted, headless, cmdEnv)
            ?: return DeviceSetupResult.Failed(
              exitCode = ClientExitCode.FAILED,
              error = "emulator_start_failed",
              message = "Failed starting AVD '$wanted'.",
            )
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
        startAvdAndWaitOnline(androidCli, adbBin, sdkDir, targetAvd, headless, cmdEnv)
          ?: return DeviceSetupResult.Failed(
            exitCode = ClientExitCode.FAILED,
            error = "emulator_start_failed",
            message = "Failed starting AVD '$targetAvd'.",
          )
      chosenSerial = started
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

    // Apply background idle exemptions every start so :sandbox stays reachable while offscreen.
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
          message = "Failed starting $HOST_RENDER_ACTIVITY on $chosenSerial.",
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
    )
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
    headless: Boolean,
    cmdEnv: Map<String, String>,
  ): String? {
    val before = listOnlineDevices(adbBin, cmdEnv).map { it.serial }.toSet()
    if (androidCli != null) {
      val cmd = buildList {
        add(androidCli)
        add("emulator")
        add("start")
        if (headless) add("--headless")
        add(avdName)
      }
      val startOut = runner.run(cmd, BOOT_TIMEOUT_MS, cmdEnv)
      if (startOut.exitCode != 0) return null
    } else {
      val emulatorBin =
        sdkDir?.resolve("emulator/emulator")?.takeIf { Files.isExecutable(it) }?.toString()
          ?: return null
      val cmd = buildList {
        add(emulatorBin)
        add("-avd")
        add(avdName)
        if (headless) add("-no-window")
      }
      if (runner === ProcessCommandRunner) {
        val pb =
          ProcessBuilder(cmd)
            .redirectInput(ProcessBuilder.Redirect.from(File("/dev/null")))
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
        pb.environment().putAll(cmdEnv)
        pb.start()
      } else {
        runner.run(cmd, BOOT_TIMEOUT_MS, cmdEnv)
      }
    }

    val deadline = System.currentTimeMillis() + BOOT_WAIT_MS
    while (System.currentTimeMillis() < deadline) {
      val after = listOnlineDevices(adbBin, cmdEnv)
      val added = after.firstOrNull { it.serial !in before }
      if (added != null) return added.serial
      if (before.isEmpty() && after.isNotEmpty()) return after.first().serial
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
        listOf(
          adbBin,
          "-s",
          serial,
          "shell",
          "am",
          "start",
          "-W",
          "-n",
          HOST_RENDER_ACTIVITY,
          "--ez",
          "move_to_back",
          "true",
        ),
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
    const val HOST_RENDER_ACTIVITY = "$HOST_PACKAGE/.sandbox.HostRenderActivity"
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
          out.writeByte(1) // DeviceOp.PING
          out.flush()
          val len = inp.readInt()
          if (len <= 0 || len > 1024 * 1024) return null
          val bytes = ByteArray(len)
          inp.readFully(bytes)
          PreviewJson.compact.decodeFromString(
            DevicePingResponse.serializer(),
            String(bytes, StandardCharsets.UTF_8),
          )
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
