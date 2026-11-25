package bot.charlies.androiddesign.preview.app.client

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.app.android.DeviceManager
import bot.charlies.androiddesign.preview.app.server.CheckScope
import bot.charlies.androiddesign.preview.app.server.ContentHasher
import bot.charlies.androiddesign.preview.app.server.LiveFocusRequest
import bot.charlies.androiddesign.preview.app.server.RuntimeIdentity
import bot.charlies.androiddesign.preview.app.server.ServerCommand
import bot.charlies.androiddesign.preview.app.server.ServerPaths
import bot.charlies.androiddesign.preview.app.server.ServerRecord
import bot.charlies.androiddesign.preview.app.server.StatusResponse
import bot.charlies.androiddesign.preview.app.server.StopRequest
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.model.PreviewJson
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URI
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.TimeUnit

/**
 * Short-lived CLI HTTP client using [HttpURLConnection] (~0.05 s startup; never uses
 * `java.net.http.HttpClient`). Discovers the single shared host server (`servers/active.json`),
 * serializes startup/replacement via `servers/active.lock`, checks recorded watchdog timeouts, and
 * enforces external client deadlines.
 */
class PreviewClient(
  internal val cacheRoot: Path = ServerPaths.cacheRoot(),
  private val env: Map<String, String> = System.getenv(),
  private val progress: (String) -> Unit = { System.err.println(it) },
  internal val serverMainClass: String = DEFAULT_SERVER_MAIN,
) {
  data class HttpResult(val status: Int, val body: String)

  /** True when automatic browser / device live-activity launching is suppressed via env. */
  val noOpen: Boolean
    get() = env[NO_OPEN_ENV] == "1"

  sealed class ServerLookup {
    data class Ready(val record: ServerRecord, val status: StatusResponse) : ServerLookup()

    data class NotReady(val message: String) : ServerLookup()
  }

  /** Outcome of a scoped CLI POST to `/api/check` or `/api/tree` with timeout/409 recovery. */
  sealed interface PostOutcome {
    data class Ok(val body: String) : PostOutcome

    data class ClientError(val status: Int, val body: String) : PostOutcome

    data class ServerError(val status: Int, val body: String) : PostOutcome

    data class Replaced(val replacedByVersion: String?, val body: String? = null) : PostOutcome

    data class DeadlineExceeded(val timeoutMs: Int, val restarted: Boolean) : PostOutcome

    data class WatchdogHalted(val scope: CheckScope) : PostOutcome

    data class Unreachable(val message: String) : PostOutcome
  }

  /** Probes liveness of the shared host server via `/api/status` without starting one. */
  fun probe(repoRoot: Path): ServerLookup {
    val recordPath = ServerPaths.sharedRecordPath(cacheRoot)
    val record =
      ServerPaths.readRecord(recordPath) ?: return ServerLookup.NotReady("No server record found")
    val repoId = ServerPaths.repoHash(repoRoot.toAbsolutePath().normalize())
    val status =
      probeRecord(record, repoId)
        ?: return ServerLookup.NotReady("Server is not answering on 127.0.0.1:${record.port}")
    return ServerLookup.Ready(record, status)
  }

  /** Returns replaced version if a newer client just superseded [priorRecord]. */
  fun checkReplacedByVersion(priorRecord: ServerRecord): String? {
    val current = ServerPaths.readRecord(ServerPaths.sharedRecordPath(cacheRoot)) ?: return null
    current.replacedByVersion
      ?.takeIf { it.isNotBlank() }
      ?.let {
        return it
      }
    if (current.pid != priorRecord.pid && current.startedAt >= priorRecord.startedAt) {
      return current.jarVersion
    }
    return null
  }

  /**
   * Checks whether `active.json` holds a recorded watchdog timeout for [snapshot] and [scope]
   * BEFORE overwriting a dead server record on respawn.
   */
  fun checkRecordedTimeout(
    snapshot: ContentHasher.Snapshot,
    scope: CheckScope,
  ): Boolean {
    val record = ServerPaths.readRecord(ServerPaths.sharedRecordPath(cacheRoot)) ?: return false
    return record.hasTimeout(snapshot.contentHash, scope)
  }

  /**
   * Finds the single live shared server running this client's runtime/version, or spawns a fresh
   * one, reusing the prior port and viewToken so open browser `EventSource` connections reconnect
   * cleanly. Any mismatched or unresponsive server is stopped first (`replacedByVersion` is pushed
   * to fail displaced in-flight checks immediately).
   */
  fun ensureServer(
    repoRoot: Path,
    requestedSerial: String? = null,
    requestedAvd: String? = null,
    headless: Boolean = false,
    forceReinstallApk: Boolean = false,
  ): ServerLookup = withServerLock {
    ensureServerLocked(
      repoRoot,
      requestedSerial,
      requestedAvd,
      headless,
      forceReinstallApk,
    )
  }

  /** [ensureServer] body; the caller must hold the server lock. */
  private fun ensureServerLocked(
    repoRoot: Path,
    requestedSerial: String? = null,
    requestedAvd: String? = null,
    headless: Boolean = false,
    forceReinstallApk: Boolean = false,
  ): ServerLookup {
    val normRepo = repoRoot.toAbsolutePath().normalize()
    val repoId = ServerPaths.repoHash(normRepo)
    val recordPath = ServerPaths.sharedRecordPath(cacheRoot)
    val identity = RuntimeIdentity.compute(env)
    val existing = ServerPaths.readRecord(recordPath)
    var apkChanged = forceReinstallApk
    var inheritedOwnedSerial: String? = null
    if (existing != null) {
      val live = probeRecord(existing, repoId)
      val serialMatches = requestedSerial.isNullOrBlank() || live?.deviceSerial == requestedSerial
      if (
        live != null &&
          !forceReinstallApk &&
          serialMatches &&
          runsCurrentRuntime(existing, live, identity)
      ) {
        return ServerLookup.Ready(existing, live)
      }
      if (
        existing.apkSha256 != null &&
          identity.apkSha256 != null &&
          existing.apkSha256 != identity.apkSha256
      ) {
        apkChanged = true
      }
      inheritedOwnedSerial = existing.ownedEmulatorSerial?.takeIf { it.isNotBlank() }
      stopServer(existing, graceful = live != null, replacedByVersion = AppInfo.VERSION)
    }
    val spawned =
      spawnServer(
        repoRoot = normRepo,
        reusePort = existing?.port ?: ServerPaths.SHARED_PREFERRED_PORT,
        reuseViewToken = existing?.viewToken,
        identity = identity,
        forceReinstallApk = apkChanged,
        deviceSerial = requestedSerial,
        avdName = requestedAvd,
        headless = headless,
        ownedEmulatorSerial = inheritedOwnedSerial,
      )
    if (spawned is ServerLookup.NotReady && inheritedOwnedSerial != null) {
      runCatching {
        DeviceManager(cacheRoot = cacheRoot, env = env).releaseEmulatorSerial(inheritedOwnedSerial)
      }
    }
    return spawned
  }

  /** Stops the shared server, answering or not. Returns its record, or null when none ran. */
  fun stop(repoRoot: Path): ServerRecord? = withServerLock {
    val record =
      ServerPaths.readRecord(ServerPaths.sharedRecordPath(cacheRoot)) ?: return@withServerLock null
    val stopped =
      stopServer(record, graceful = probeRecord(record) != null, replacedByVersion = null)
    if (stopped) record else null
  }

  /**
   * Replaces [deadRecord]'s server. Re-reads `active.json` under the lock: if another CLI already
   * replaced the server (different pid or start time), that server is used through the normal
   * readiness path instead of spawning a second one and orphaning it.
   */
  fun killAndRespawn(repoRoot: Path, deadRecord: ServerRecord): ServerLookup = withServerLock {
    val current = ServerPaths.readRecord(ServerPaths.sharedRecordPath(cacheRoot))
    if (
      current != null &&
        (current.pid != deadRecord.pid || current.startedAt != deadRecord.startedAt)
    ) {
      return@withServerLock ensureServerLocked(repoRoot)
    }
    val normRepo = repoRoot.toAbsolutePath().normalize()
    val identity = RuntimeIdentity.compute(env)
    val inheritedOwnedSerial = deadRecord.ownedEmulatorSerial?.takeIf { it.isNotBlank() }
    stopServer(deadRecord, graceful = false, replacedByVersion = null)
    val spawned =
      spawnServer(
        repoRoot = normRepo,
        reusePort = deadRecord.port,
        reuseViewToken = deadRecord.viewToken,
        identity = identity,
        forceReinstallApk = false,
        deviceSerial = deadRecord.deviceSerial,
        avdName = null,
        headless = false,
        ownedEmulatorSerial = inheritedOwnedSerial,
      )
    if (spawned is ServerLookup.NotReady && inheritedOwnedSerial != null) {
      runCatching {
        DeviceManager(cacheRoot = cacheRoot, env = env).releaseEmulatorSerial(inheritedOwnedSerial)
      }
    }
    spawned
  }

  /** Outcome of [recoverFromDeadline]. */
  enum class DeadlineRecovery {
    /** The server still answered `/api/status`; it was left running (the watchdog owns hangs). */
    SERVER_RESPONSIVE,
    /** The server did not answer and was replaced. */
    RESTARTED,
  }

  /**
   * Handles a client-side deadline. The deadline includes queue wait in the shared single worker,
   * so a timeout alone does not prove the server is hung: probe `/api/status` first and only
   * replace an unresponsive server.
   */
  fun recoverFromDeadline(repoRoot: Path, record: ServerRecord): DeadlineRecovery {
    if (probeRecord(record, timeoutMs = DEADLINE_PROBE_TIMEOUT_MS) != null) {
      return DeadlineRecovery.SERVER_RESPONSIVE
    }
    killAndRespawn(repoRoot, record)
    return DeadlineRecovery.RESTARTED
  }

  /**
   * Sends a JSON POST request (`check` or `tree`) to the active server under [scope]'s client
   * deadline and classifies HTTP errors, server replacement (409 or record supersession), watchdog
   * halts, and deadline recovery into [PostOutcome].
   */
  fun postScoped(
    ready: ServerLookup.Ready,
    repoRoot: Path,
    path: String,
    jsonBody: String,
    snapshot: ContentHasher.Snapshot,
    scope: CheckScope,
  ): PostOutcome {
    val timeoutMs = deadlineMs(isCold = ready.status.compileCount == 0, scoped = scope.isScoped)
    val http =
      try {
        postJson(ready.record, path, jsonBody, readTimeoutMs = timeoutMs)
      } catch (e: Exception) {
        val repVer = checkReplacedByVersion(ready.record)
        if (repVer != null) {
          return PostOutcome.Replaced(replacedByVersion = repVer)
        }
        return when {
          e is SocketTimeoutException -> {
            val restarted =
              recoverFromDeadline(repoRoot, ready.record) == DeadlineRecovery.RESTARTED
            PostOutcome.DeadlineExceeded(timeoutMs = timeoutMs, restarted = restarted)
          }
          checkRecordedTimeout(snapshot, scope) -> PostOutcome.WatchdogHalted(scope)
          else -> PostOutcome.Unreachable(e.message ?: "Failed contacting preview server")
        }
      }

    return when (http.status) {
      400,
      404 -> PostOutcome.ClientError(http.status, http.body)
      409 ->
        PostOutcome.Replaced(
          replacedByVersion = checkReplacedByVersion(ready.record),
          body = http.body,
        )
      in 200..299 -> PostOutcome.Ok(http.body)
      else -> PostOutcome.ServerError(http.status, http.body)
    }
  }

  /**
   * Updates live focus on the active server and, when [launchActivity] is true, asks the server to
   * bring `LivePreviewActivity` to the foreground (allowing enough read timeout for `am start -W`).
   */
  fun notifyLive(
    record: ServerRecord,
    sourcePath: Path?,
    repoRoot: Path,
    launchActivity: Boolean = false,
  ): Boolean =
    try {
      val body =
        PreviewJson.compact.encodeToString(
          LiveFocusRequest.serializer(),
          LiveFocusRequest(
            path = sourcePath?.toAbsolutePath()?.normalize()?.toString(),
            repoRoot = repoRoot.toAbsolutePath().normalize().toString(),
            launchActivity = launchActivity,
          ),
        )
      val timeoutMs = if (launchActivity) LIVE_LAUNCH_TIMEOUT_MS else LIVE_TIMEOUT_MS
      postJson(record, "/api/live", body, readTimeoutMs = timeoutMs).status == 200
    } catch (_: Exception) {
      false
    }

  fun get(record: ServerRecord, pathAndQuery: String, readTimeoutMs: Int = 5_000): HttpResult =
    request(record, "GET", pathAndQuery, body = null, readTimeoutMs = readTimeoutMs)

  fun postJson(
    record: ServerRecord,
    path: String,
    jsonBody: String,
    readTimeoutMs: Int,
  ): HttpResult = request(record, "POST", path, body = jsonBody, readTimeoutMs = readTimeoutMs)

  fun deadlineMs(isCold: Boolean, scoped: Boolean): Int {
    val override = env[CLIENT_TIMEOUT_ENV]?.toIntOrNull()
    if (override != null && override > 0) return override
    return when {
      isCold -> COLD_CLIENT_DEADLINE_MS
      scoped -> WARM_SCOPED_CLIENT_DEADLINE_MS
      else -> WARM_FULL_CLIENT_DEADLINE_MS
    }
  }

  private fun runsCurrentRuntime(
    record: ServerRecord,
    status: StatusResponse,
    identity: RuntimeIdentity,
  ): Boolean {
    if (status.jarVersion != AppInfo.VERSION) return false
    if (status.backend != identity.backend) return false
    if (record.runtimeHash != identity.runtimeHash) return false
    if (
      identity.apkSha256 != null &&
        record.apkSha256 != null &&
        record.apkSha256 != identity.apkSha256
    ) {
      return false
    }
    return true
  }

  private fun stopServer(
    record: ServerRecord,
    graceful: Boolean,
    replacedByVersion: String?,
  ): Boolean {
    val handle = serverProcess(record)
    if (graceful) {
      val stopBody =
        if (replacedByVersion != null) {
          PreviewJson.compact.encodeToString(
            StopRequest.serializer(),
            StopRequest(replacedByVersion = replacedByVersion, reason = "runtime_mismatch"),
          )
        } else {
          "{}"
        }
      val asked =
        try {
          postJson(record, "/api/stop", stopBody, readTimeoutMs = 2_000)
          true
        } catch (_: Exception) {
          false
        }
      if (handle != null && waitForExit(handle, SERVER_TERM_GRACE_MS)) return true
      if (handle == null && asked && waitUntilSilent(record)) return true
    }
    if (handle == null) return false
    // SIGTERM first so the server's shutdown hook can cleanly stop any emulator it launched.
    handle.destroy()
    if (waitForExit(handle, SERVER_TERM_GRACE_MS)) return true
    handle.destroyForcibly()
    return waitForExit(handle, STOP_WAIT_MS)
  }

  internal fun serverProcess(record: ServerRecord): ProcessHandle? {
    val handle = ProcessHandle.of(record.pid).orElse(null)?.takeIf { it.isAlive } ?: return null
    val info = handle.info()
    val started = info.startInstant().orElse(null)?.toEpochMilli()
    val isServer =
      if (started != null) {
        record.startedAt - started in -START_CLOCK_SLACK_MS..MAX_SERVER_BOOT_MS
      } else {
        info.commandLine().orElse("").let { it.contains(" server ") && it.contains("--repo") }
      }
    return if (isServer) handle else null
  }

  private fun waitUntilSilent(record: ServerRecord): Boolean {
    val deadline = System.currentTimeMillis() + STOP_WAIT_MS
    while (probeRecord(record) != null) {
      if (System.currentTimeMillis() >= deadline) return false
      Thread.sleep(50)
    }
    ProcessHandle.of(record.pid).ifPresent { waitForExit(it, SERVER_TERM_GRACE_MS) }
    return true
  }

  private fun waitForExit(handle: ProcessHandle, timeoutMs: Long = STOP_WAIT_MS): Boolean =
    try {
      handle.onExit().get(timeoutMs, TimeUnit.MILLISECONDS)
      true
    } catch (_: Exception) {
      !handle.isAlive
    }

  private fun probeRecord(
    record: ServerRecord,
    repoId: String? = null,
    timeoutMs: Int = 700,
  ): StatusResponse? {
    return try {
      val path = if (repoId.isNullOrBlank()) "/api/status" else "/api/status?repo=$repoId"
      val res = request(record, "GET", path, body = null, readTimeoutMs = timeoutMs)
      if (res.status != 200) return null
      PreviewJson.compact.decodeFromString(StatusResponse.serializer(), res.body)
    } catch (_: Exception) {
      null
    }
  }

  private fun spawnServer(
    repoRoot: Path,
    reusePort: Int?,
    reuseViewToken: String?,
    identity: RuntimeIdentity,
    forceReinstallApk: Boolean,
    deviceSerial: String?,
    avdName: String?,
    headless: Boolean,
    ownedEmulatorSerial: String? = null,
  ): ServerLookup {
    val bootstrapCp =
      identity.serverCpFile
        ?: return ServerLookup.NotReady(
          "Cannot locate bootstrap server.cp; run `android-design-preview start` or `./gradlew :host:exportBootstrap` from app/.",
        )

    val javaBin = resolveJavaBinary()
    val cmd = buildList {
      add(javaBin)
      add("-Djava.awt.headless=true")
      add("@${bootstrapCp.toAbsolutePath().normalize()}")
      add(serverMainClass)
      add("server")
      add("--repo")
      add(repoRoot.toString())
      add("--backend")
      add(identity.backend)
      add("--runtime-hash")
      add(identity.runtimeHash)
      if (reusePort != null && reusePort > 0) {
        add("--preferred-port")
        add(reusePort.toString())
      }
      if (!reuseViewToken.isNullOrBlank()) {
        add("--view-token")
        add(reuseViewToken)
      }
      if (!deviceSerial.isNullOrBlank()) {
        add("--device-serial")
        add(deviceSerial)
      }
      if (!avdName.isNullOrBlank()) {
        add("--avd")
        add(avdName)
      }
      if (headless) {
        add("--headless")
        add("true")
      }
      if (forceReinstallApk) {
        add("--force-reinstall-apk")
        add("true")
      }
      if (!ownedEmulatorSerial.isNullOrBlank()) {
        add("--owned-emulator-serial")
        add(ownedEmulatorSerial)
      }
    }

    val logDir = ServerPaths.serversDir(cacheRoot)
    Files.createDirectories(logDir)
    val logFile = ServerPaths.sharedLogPath(cacheRoot).toFile()

    val pb =
      ProcessBuilder(cmd)
        .directory(repoRoot.toFile())
        .redirectOutput(ProcessBuilder.Redirect.appendTo(logFile))
        .redirectErrorStream(true)
    val devNull = java.io.File("/dev/null")
    if (devNull.exists()) {
      pb.redirectInput(ProcessBuilder.Redirect.from(devNull))
    }
    val childEnv = pb.environment()
    childEnv[ServerPaths.CACHE_DIR_ENV] = cacheRoot.toString()
    childEnv[RuntimeIdentity.BACKEND_ENV] = identity.backend
    identity.bootstrapDir?.let { childEnv[BootstrapMeta.BOOTSTRAP_DIR_ENV] = it.toString() }
    childEnv[RuntimeIdentity.SERVER_CP_ENV] = bootstrapCp.toString()
    identity.sourceDir?.let { childEnv[RuntimeIdentity.SRC_ENV] = it }
    env["ANDROID_HOME"]?.let { childEnv["ANDROID_HOME"] = it }
    env["ANDROID_SDK_ROOT"]?.let { childEnv["ANDROID_SDK_ROOT"] = it }
    env["ANDROID_DESIGN_ADB_BIN"]?.let { childEnv["ANDROID_DESIGN_ADB_BIN"] = it }
    env["ANDROID_DESIGN_ANDROID_CLI"]?.let { childEnv["ANDROID_DESIGN_ANDROID_CLI"] = it }
    env["ANDROID_DESIGN_PREVIEW_JAVA"]?.let { childEnv["ANDROID_DESIGN_PREVIEW_JAVA"] = it }
    env[RuntimeIdentity.RUNTIME_HASH_ENV]?.let { childEnv[RuntimeIdentity.RUNTIME_HASH_ENV] = it }
    env[ServerCommand.IDLE_TIMEOUT_ENV]?.let { childEnv[ServerCommand.IDLE_TIMEOUT_ENV] = it }
    env[ServerCommand.MAX_COMPILES_ENV]?.let { childEnv[ServerCommand.MAX_COMPILES_ENV] = it }
    env[ServerCommand.COLD_BUDGET_ENV]?.let { childEnv[ServerCommand.COLD_BUDGET_ENV] = it }
    env[ServerCommand.WARM_BUDGET_ENV]?.let { childEnv[ServerCommand.WARM_BUDGET_ENV] = it }

    val logStart = logFile.takeIf { it.exists() }?.length() ?: 0L
    val proc =
      try {
        pb.start()
      } catch (e: Exception) {
        return ServerLookup.NotReady("Failed to launch preview server: ${e.message}")
      }

    return awaitServerStart(proc, logFile, logStart, ANDROID_STARTUP_WAIT_MS)
  }

  /**
   * Waits for the spawned [proc] to publish a live record. Device setup (AVD boot, install, AOT)
   * runs before the record exists, so the wait lasts as long as the child is alive, up to [waitMs].
   * Fails fast when the child exits; reports progress from the child's log to [progress].
   */
  internal fun awaitServerStart(
    proc: Process,
    logFile: java.io.File,
    logStart: Long,
    waitMs: Long,
    progressIntervalMs: Long = PROGRESS_INTERVAL_MS,
  ): ServerLookup {
    val recordPath = ServerPaths.sharedRecordPath(cacheRoot)
    val started = System.currentTimeMillis()
    val deadline = started + waitMs
    val targetPid = proc.pid()
    var nextProgressAt = started + progressIntervalMs
    while (System.currentTimeMillis() < deadline) {
      if (!proc.isAlive) {
        return ServerLookup.NotReady(
          "Preview server exited immediately (code ${proc.exitValue()}): ${logTail(logFile, logStart)}",
        )
      }
      val rec = ServerPaths.readRecord(recordPath)
      if (rec != null && rec.pid == targetPid) {
        val live = probeRecord(rec)
        if (live != null) return ServerLookup.Ready(rec, live)
      }
      val now = System.currentTimeMillis()
      if (now >= nextProgressAt) {
        nextProgressAt = now + progressIntervalMs
        val last = logTail(logFile, logStart).lineSequence().lastOrNull { it.isNotBlank() }
        progress(
          "Starting preview server (${(now - started) / 1000}s)" +
            (last?.let { ": ${it.take(160)}" } ?: "..."),
        )
      }
      try {
        Thread.sleep(50)
      } catch (_: InterruptedException) {
        break
      }
    }
    // SIGTERM first: the server's shutdown hook stops an emulator it launched mid-boot.
    proc.destroy()
    try {
      if (!proc.waitFor(SERVER_TERM_GRACE_MS, TimeUnit.MILLISECONDS)) proc.destroyForcibly()
    } catch (_: InterruptedException) {
      proc.destroyForcibly()
    }
    return ServerLookup.NotReady("Timed out waiting for preview server to start")
  }

  private fun logTail(logFile: java.io.File, logStart: Long): String =
    try {
      val text = logFile.readText()
      text.substring(minOf(logStart, text.length.toLong()).toInt()).takeLast(600).trim()
    } catch (_: Exception) {
      ""
    }

  private fun request(
    record: ServerRecord,
    method: String,
    pathAndQuery: String,
    body: String?,
    readTimeoutMs: Int,
  ): HttpResult {
    val url = URI("http://127.0.0.1:${record.port}$pathAndQuery").toURL()
    val conn = (url.openConnection() as HttpURLConnection)
    try {
      conn.requestMethod = method
      conn.connectTimeout = 1_500
      conn.readTimeout = readTimeoutMs
      conn.useCaches = false
      conn.setRequestProperty("Host", "127.0.0.1:${record.port}")
      conn.setRequestProperty("X-Client-Secret", record.clientSecret)
      if (body != null) {
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        val bytes = body.toByteArray(Charsets.UTF_8)
        conn.setFixedLengthStreamingMode(bytes.size)
        conn.outputStream.use { it.write(bytes) }
      }
      val status = conn.responseCode
      val stream =
        if (status in 200..399) conn.inputStream else (conn.errorStream ?: conn.inputStream)
      val responseText = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
      return HttpResult(status, responseText)
    } catch (e: SocketTimeoutException) {
      throw e
    } catch (e: IOException) {
      throw e
    } finally {
      conn.disconnect()
    }
  }

  private fun resolveJavaBinary(): String {
    val home = System.getProperty("java.home")
    if (!home.isNullOrBlank()) {
      val unix = Path.of(home, "bin", "java")
      if (Files.isExecutable(unix)) return unix.toString()
      val win = Path.of(home, "bin", "java.exe")
      if (Files.isRegularFile(win)) return win.toString()
    }
    return "java"
  }

  private inline fun <T> withServerLock(block: () -> T): T {
    synchronized(inProcessLock) {
      val lockPath = ServerPaths.sharedLockPath(cacheRoot)
      lockPath.parent?.let(Files::createDirectories)
      return FileChannel.open(
          lockPath,
          StandardOpenOption.CREATE,
          StandardOpenOption.WRITE,
        )
        .use { ch ->
          ch.lock().use { block() }
        }
    }
  }

  companion object {
    const val DEFAULT_SERVER_MAIN = "bot.charlies.androiddesign.preview.app.MainKt"
    const val NO_OPEN_ENV = "ANDROID_DESIGN_NO_OPEN"
    const val SCRIPT_ENV = "ANDROID_DESIGN_PREVIEW_SCRIPT"
    const val CLIENT_TIMEOUT_ENV = "ANDROID_DESIGN_CLIENT_TIMEOUT_MS"
    /**
     * Overall startup cap: the device setup budget in `DeviceManager` (AVD online 120 s + boot 60 s
     * + install 120 s + AOT 120 s), which runs before the server writes its record.
     */
    internal const val ANDROID_STARTUP_WAIT_MS = 420_000L
    private const val PROGRESS_INTERVAL_MS = 10_000L
    private const val DEADLINE_PROBE_TIMEOUT_MS = 2_000
    private const val LIVE_TIMEOUT_MS = 3_000
    /** Exceeds `DeviceManager`'s 15 s `adb shell am start -W` timeout plus live focus update. */
    private const val LIVE_LAUNCH_TIMEOUT_MS = 20_000
    private const val COLD_CLIENT_DEADLINE_MS = 35_000
    private const val WARM_SCOPED_CLIENT_DEADLINE_MS = 10_000
    private const val WARM_FULL_CLIENT_DEADLINE_MS = 35_000
    private const val STOP_WAIT_MS = 3_000L
    private const val SERVER_TERM_GRACE_MS = 15_000L
    private const val START_CLOCK_SLACK_MS = 2_000L
    private const val MAX_SERVER_BOOT_MS = ANDROID_STARTUP_WAIT_MS + 30_000L
    private val inProcessLock = Any()
  }
}
