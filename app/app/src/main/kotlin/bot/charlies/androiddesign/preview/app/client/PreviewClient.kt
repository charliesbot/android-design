package bot.charlies.androiddesign.preview.app.client

import bot.charlies.androiddesign.preview.app.AppInfo
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
) {
  data class HttpResult(val status: Int, val body: String)

  sealed class ServerLookup {
    data class Ready(val record: ServerRecord, val status: StatusResponse) : ServerLookup()

    data class NotReady(val message: String) : ServerLookup()
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
   * Checks whether `active.json` holds a recorded watchdog timeout for [snapshot] and scope BEFORE
   * overwriting a dead server record on respawn.
   */
  fun checkRecordedTimeout(
    repoRoot: Path,
    snapshot: ContentHasher.Snapshot,
    scope: String,
    direction: String?,
    screen: String?,
    view: String?,
  ): Boolean {
    val record = ServerPaths.readRecord(ServerPaths.sharedRecordPath(cacheRoot)) ?: return false
    return record.hasTimeout(snapshot.contentHash, scope, direction, screen, view)
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
    val normRepo = repoRoot.toAbsolutePath().normalize()
    val repoId = ServerPaths.repoHash(normRepo)
    val recordPath = ServerPaths.sharedRecordPath(cacheRoot)
    val identity = RuntimeIdentity.compute(env, jar = resolveJarPath())
    val existing = ServerPaths.readRecord(recordPath)
    var apkChanged = forceReinstallApk
    if (existing != null) {
      val live = probeRecord(existing, repoId)
      val serialMatches = requestedSerial.isNullOrBlank() || live?.deviceSerial == requestedSerial
      if (
        live != null &&
          !forceReinstallApk &&
          serialMatches &&
          runsCurrentRuntime(existing, live, identity)
      ) {
        return@withServerLock ServerLookup.Ready(existing, live)
      }
      if (
        identity.backend == RuntimeIdentity.BACKEND_ANDROID &&
          existing.apkSha256 != null &&
          identity.apkSha256 != null &&
          existing.apkSha256 != identity.apkSha256
      ) {
        apkChanged = true
      }
      stopServer(existing, graceful = live != null, replacedByVersion = AppInfo.VERSION)
    }
    spawnServer(
      repoRoot = normRepo,
      reusePort = existing?.port ?: ServerPaths.SHARED_PREFERRED_PORT,
      reuseViewToken = existing?.viewToken,
      identity = identity,
      forceReinstallApk = apkChanged,
      deviceSerial = requestedSerial,
      avdName = requestedAvd,
      headless = headless,
    )
  }

  /** Stops the shared server, answering or not. Returns its record, or null when none ran. */
  fun stop(repoRoot: Path): ServerRecord? = withServerLock {
    val record =
      ServerPaths.readRecord(ServerPaths.sharedRecordPath(cacheRoot)) ?: return@withServerLock null
    val stopped =
      stopServer(record, graceful = probeRecord(record) != null, replacedByVersion = null)
    if (stopped) record else null
  }

  fun killAndRespawn(repoRoot: Path, deadRecord: ServerRecord): ServerLookup = withServerLock {
    val normRepo = repoRoot.toAbsolutePath().normalize()
    val identity = RuntimeIdentity.compute(env, jar = resolveJarPath())
    stopServer(deadRecord, graceful = false, replacedByVersion = null)
    spawnServer(
      repoRoot = normRepo,
      reusePort = deadRecord.port,
      reuseViewToken = deadRecord.viewToken,
      identity = identity,
      forceReinstallApk = false,
      deviceSerial = deadRecord.deviceSerial,
      avdName = null,
      headless = false,
    )
  }

  fun notifyLive(record: ServerRecord, sourcePath: Path?, repoRoot: Path): Boolean =
    try {
      val body =
        PreviewJson.compact.encodeToString(
          LiveFocusRequest.serializer(),
          LiveFocusRequest(
            path = sourcePath?.toAbsolutePath()?.normalize()?.toString(),
            repoRoot = repoRoot.toAbsolutePath().normalize().toString(),
          ),
        )
      postJson(record, "/api/live", body, readTimeoutMs = 3_000).status == 200
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
    val usesBootstrapOrOverride =
      identity.backend == RuntimeIdentity.BACKEND_ANDROID ||
        identity.backend == RuntimeIdentity.BACKEND_ROBOLECTRIC ||
        identity.serverCpFile != null ||
        identity.sourceDir != null ||
        !env[RuntimeIdentity.RUNTIME_HASH_ENV].isNullOrBlank()
    if (usesBootstrapOrOverride) {
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
    val jar = identity.jarPath ?: return true
    return record.jarPath == jar.toString() && record.jarModified == identity.jarModified
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
      if (handle != null && waitForExit(handle)) return true
      if (handle == null && asked && waitUntilSilent(record)) return true
    }
    if (handle == null) return false
    handle.destroyForcibly()
    return waitForExit(handle)
  }

  private fun serverProcess(record: ServerRecord): ProcessHandle? {
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
    ProcessHandle.of(record.pid).ifPresent { waitForExit(it) }
    return true
  }

  private fun waitForExit(handle: ProcessHandle): Boolean =
    try {
      handle.onExit().get(STOP_WAIT_MS, TimeUnit.MILLISECONDS)
      true
    } catch (_: Exception) {
      !handle.isAlive
    }

  private fun probeRecord(record: ServerRecord, repoId: String? = null): StatusResponse? {
    return try {
      val path = if (repoId.isNullOrBlank()) "/api/status" else "/api/status?repo=$repoId"
      val res = request(record, "GET", path, body = null, readTimeoutMs = 700)
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
  ): ServerLookup {
    val bootstrapCp =
      identity.serverCpFile?.takeIf {
        identity.backend == RuntimeIdentity.BACKEND_ANDROID ||
          identity.backend == RuntimeIdentity.BACKEND_ROBOLECTRIC ||
          identity.jarPath == null
      }

    val javaBin = resolveJavaBinary()
    val cmd =
      if (bootstrapCp != null) {
        buildList {
          add(javaBin)
          add("-Djava.awt.headless=true")
          add("@${bootstrapCp.toAbsolutePath().normalize()}")
          add("bot.charlies.androiddesign.preview.app.MainKt")
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
        }
      } else {
        val jarPath =
          identity.jarPath
            ?: return ServerLookup.NotReady(
              "Cannot locate android-design-preview.jar or bootstrap server.cp; run `android-design-preview start` or set ANDROID_DESIGN_PREVIEW_JAR.",
            )
        buildList {
          add(javaBin)
          add("-Djava.awt.headless=true")
          add("-jar")
          add(jarPath.toString())
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
    val childEnv = pb.environment()
    childEnv[ServerPaths.CACHE_DIR_ENV] = cacheRoot.toString()
    childEnv[RuntimeIdentity.BACKEND_ENV] = identity.backend
    if (bootstrapCp != null) {
      identity.bootstrapDir?.let { childEnv[BootstrapMeta.BOOTSTRAP_DIR_ENV] = it.toString() }
      childEnv[RuntimeIdentity.SERVER_CP_ENV] = bootstrapCp.toString()
      identity.robolectricCpFile?.let {
        childEnv[RuntimeIdentity.ROBOLECTRIC_CP_ENV] = it.toString()
      }
      identity.sourceDir?.let { childEnv[RuntimeIdentity.SRC_ENV] = it }
      env["ANDROID_HOME"]?.let { childEnv["ANDROID_HOME"] = it }
      env["ANDROID_SDK_ROOT"]?.let { childEnv["ANDROID_SDK_ROOT"] = it }
      env["ANDROID_DESIGN_ADB_BIN"]?.let { childEnv["ANDROID_DESIGN_ADB_BIN"] = it }
      env["ANDROID_DESIGN_ANDROID_CLI"]?.let { childEnv["ANDROID_DESIGN_ANDROID_CLI"] = it }
      env["ANDROID_DESIGN_PREVIEW_JAVA"]?.let { childEnv["ANDROID_DESIGN_PREVIEW_JAVA"] = it }
    } else {
      childEnv.remove(BootstrapMeta.BOOTSTRAP_DIR_ENV)
      childEnv.remove(RuntimeIdentity.SERVER_CP_ENV)
      childEnv.remove(RuntimeIdentity.ROBOLECTRIC_CP_ENV)
    }
    env[RuntimeIdentity.RUNTIME_HASH_ENV]?.let { childEnv[RuntimeIdentity.RUNTIME_HASH_ENV] = it }
    env[ServerCommand.COLD_BUDGET_ENV]?.let { childEnv[ServerCommand.COLD_BUDGET_ENV] = it }
    env[ServerCommand.WARM_BUDGET_ENV]?.let { childEnv[ServerCommand.WARM_BUDGET_ENV] = it }

    val proc =
      try {
        pb.start()
      } catch (e: Exception) {
        return ServerLookup.NotReady("Failed to launch preview server: ${e.message}")
      }

    val recordPath = ServerPaths.sharedRecordPath(cacheRoot)
    val waitMs =
      if (
        identity.backend == RuntimeIdentity.BACKEND_ANDROID ||
          identity.backend == RuntimeIdentity.BACKEND_ROBOLECTRIC
      ) {
        ANDROID_STARTUP_WAIT_MS
      } else {
        STARTUP_WAIT_MS
      }
    val deadline = System.currentTimeMillis() + waitMs
    val targetPid = proc.pid()
    while (System.currentTimeMillis() < deadline) {
      if (!proc.isAlive) {
        val tail =
          try {
            logFile.readText().takeLast(600).trim()
          } catch (_: Exception) {
            ""
          }
        return ServerLookup.NotReady(
          "Preview server exited immediately (code ${proc.exitValue()}): $tail",
        )
      }
      val rec = ServerPaths.readRecord(recordPath)
      if (rec != null && rec.pid == targetPid) {
        val live = probeRecord(rec)
        if (live != null) return ServerLookup.Ready(rec, live)
      }
      try {
        Thread.sleep(50)
      } catch (_: InterruptedException) {
        break
      }
    }
    proc.destroyForcibly()
    return ServerLookup.NotReady("Timed out waiting for preview server to start")
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

  private fun resolveJarPath(): Path? {
    val fromEnv = env[JAR_ENV]?.takeIf { it.isNotBlank() }?.let(Path::of)
    if (fromEnv != null && Files.isRegularFile(fromEnv)) return fromEnv.toAbsolutePath().normalize()
    val codeSource =
      try {
        PreviewClient::class.java.protectionDomain?.codeSource?.location?.toURI()?.let(Path::of)
      } catch (_: Exception) {
        null
      }
    if (
      codeSource != null &&
        Files.isRegularFile(codeSource) &&
        codeSource.toString().endsWith(".jar")
    ) {
      return codeSource.toAbsolutePath().normalize()
    }
    return null
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
    const val JAR_ENV = "ANDROID_DESIGN_PREVIEW_JAR"
    const val NO_OPEN_ENV = "ANDROID_DESIGN_NO_OPEN"
    const val SCRIPT_ENV = "ANDROID_DESIGN_PREVIEW_SCRIPT"
    const val CLIENT_TIMEOUT_ENV = "ANDROID_DESIGN_CLIENT_TIMEOUT_MS"
    private const val STARTUP_WAIT_MS = 15_000L
    private const val ANDROID_STARTUP_WAIT_MS = 90_000L
    private const val COLD_CLIENT_DEADLINE_MS = 35_000
    private const val WARM_SCOPED_CLIENT_DEADLINE_MS = 10_000
    private const val WARM_FULL_CLIENT_DEADLINE_MS = 35_000
    private const val STOP_WAIT_MS = 3_000L
    private const val START_CLOCK_SLACK_MS = 2_000L
    private const val MAX_SERVER_BOOT_MS = 120_000L
    private val inProcessLock = Any()
  }
}
