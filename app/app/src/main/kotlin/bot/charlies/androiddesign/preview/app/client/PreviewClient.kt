package bot.charlies.androiddesign.preview.app.client

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.app.server.ContentHasher
import bot.charlies.androiddesign.preview.app.server.ServerPaths
import bot.charlies.androiddesign.preview.app.server.ServerRecord
import bot.charlies.androiddesign.preview.app.server.StatusResponse
import bot.charlies.androiddesign.preview.model.PreviewJson
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

/**
 * Short-lived CLI HTTP client using [HttpURLConnection] (~0.05 s startup; never uses
 * `java.net.http.HttpClient`). Discovers the per-repository server, spawns or replaces it on
 * demand, checks recorded watchdog timeouts before overwriting dead records, and enforces external
 * deadlines.
 */
class PreviewClient(
  private val cacheRoot: Path = ServerPaths.cacheRoot(),
  private val env: Map<String, String> = System.getenv(),
) {
  data class HttpResult(val status: Int, val body: String)

  sealed class ServerLookup {
    data class Ready(val record: ServerRecord, val status: StatusResponse) : ServerLookup()

    data class NotReady(val message: String) : ServerLookup()
  }

  /** Probes liveness of the repository server via `/api/status` without starting one. */
  fun probe(repoRoot: Path): ServerLookup {
    val recordPath = ServerPaths.recordPath(repoRoot, cacheRoot)
    val record =
      ServerPaths.readRecord(recordPath)
        ?: return ServerLookup.NotReady("No server record found for $repoRoot")
    val status =
      probeRecord(record)
        ?: return ServerLookup.NotReady("Server is not answering on 127.0.0.1:${record.port}")
    return ServerLookup.Ready(record, status)
  }

  /**
   * Checks whether [recordPath] holds a recorded watchdog timeout for [snapshot] and scope BEFORE
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
    val recordPath = ServerPaths.recordPath(repoRoot, cacheRoot)
    val record = ServerPaths.readRecord(recordPath) ?: return false
    return record.hasTimeout(snapshot.contentHash, scope, direction, screen, view)
  }

  /**
   * Finds a live server for [repoRoot] running this client's JAR, or spawns a fresh one, reusing
   * the old record's port and viewToken so open browser `EventSource` connections reconnect
   * cleanly. An outdated server (another version, or a rebuilt JAR) and one that is alive but not
   * answering are stopped first, so a repository never runs two servers.
   */
  fun ensureServer(repoRoot: Path): ServerLookup {
    val recordPath = ServerPaths.recordPath(repoRoot, cacheRoot)
    val existing = ServerPaths.readRecord(recordPath)
    if (existing != null) {
      val live = probeRecord(existing)
      if (live != null && runsCurrentJar(existing, live)) {
        return ServerLookup.Ready(existing, live)
      }
      stopServer(existing, graceful = live != null)
    }
    return spawnServer(
      repoRoot = repoRoot,
      reusePort = existing?.port,
      reuseViewToken = existing?.viewToken,
    )
  }

  /** Stops [repoRoot]'s server, answering or not. Returns its record, or null when none ran. */
  fun stop(repoRoot: Path): ServerRecord? {
    val record = ServerPaths.readRecord(ServerPaths.recordPath(repoRoot, cacheRoot)) ?: return null
    val stopped = stopServer(record, graceful = probeRecord(record) != null)
    return if (stopped) record else null
  }

  fun killAndRespawn(repoRoot: Path, deadRecord: ServerRecord): ServerLookup {
    stopServer(deadRecord, graceful = false)
    return spawnServer(
      repoRoot = repoRoot,
      reusePort = deadRecord.port,
      reuseViewToken = deadRecord.viewToken,
    )
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

  /**
   * True when [record]'s server runs the JAR this client would start: the same version and, when
   * the client runs from a JAR, the same path and modification time, so a rebuild at the same
   * version still replaces the server.
   */
  private fun runsCurrentJar(record: ServerRecord, status: StatusResponse): Boolean {
    if (status.jarVersion != AppInfo.VERSION) return false
    val jar = resolveJarPath() ?: return true
    val real =
      try {
        jar.toRealPath()
      } catch (_: Exception) {
        return false
      }
    return record.jarPath == real.toString() && record.jarModified == ServerPaths.jarModified(real)
  }

  /**
   * Stops the server [record] describes and waits for it to go, asking it to stop first when
   * [graceful]. The authenticated stop request needs no pid check, since only the recorded server
   * holds the secret, but forcing a process does: only one [serverProcess] vouches for is killed.
   * Returns whether the server stopped.
   */
  private fun stopServer(record: ServerRecord, graceful: Boolean): Boolean {
    val handle = serverProcess(record)
    if (graceful) {
      val asked =
        try {
          postJson(record, "/api/stop", "{}", readTimeoutMs = 1_500)
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

  /**
   * The live process [record] describes, or null. A server that exited long ago may have left its
   * pid to an unrelated process, so the process must have started just before the server recorded
   * `startedAt` (or, where the OS hides start times, run this JAR's `server` command).
   */
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

  /**
   * Waits for [record]'s server, whose process [serverProcess] could not vouch for, to stop
   * answering and then for its recorded pid to exit. Waiting harms nothing even if the pid now
   * belongs to another process; it only frees the port before a replacement binds it.
   */
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

  private fun probeRecord(record: ServerRecord): StatusResponse? {
    return try {
      val res = request(record, "GET", "/api/status", body = null, readTimeoutMs = 700)
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
  ): ServerLookup {
    val jarPath =
      resolveJarPath()
        ?: return ServerLookup.NotReady(
          "Cannot locate android-design-preview.jar; set ANDROID_DESIGN_PREVIEW_JAR or run via plugin/bin/android-design-preview.",
        )
    val javaBin = resolveJavaBinary()
    val cmd = buildList {
      add(javaBin)
      add("-Djava.awt.headless=true")
      add("-jar")
      add(jarPath.toString())
      add("server")
      add("--repo")
      add(repoRoot.toAbsolutePath().normalize().toString())
      if (reusePort != null && reusePort > 0) {
        add("--preferred-port")
        add(reusePort.toString())
      }
      if (!reuseViewToken.isNullOrBlank()) {
        add("--view-token")
        add(reuseViewToken)
      }
    }
    val logDir = ServerPaths.serversDir(cacheRoot)
    Files.createDirectories(logDir)
    val logFile = logDir.resolve("${ServerPaths.repoHash(repoRoot)}.log").toFile()

    val pb =
      ProcessBuilder(cmd)
        .directory(repoRoot.toFile())
        .redirectOutput(ProcessBuilder.Redirect.appendTo(logFile))
        .redirectErrorStream(true)
    pb.environment()[ServerPaths.CACHE_DIR_ENV] = cacheRoot.toString()
    val proc =
      try {
        pb.start()
      } catch (e: Exception) {
        return ServerLookup.NotReady("Failed to launch preview server: ${e.message}")
      }

    val recordPath = ServerPaths.recordPath(repoRoot, cacheRoot)
    val deadline = System.currentTimeMillis() + STARTUP_WAIT_MS
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
    return ServerLookup.NotReady("Timed out waiting for preview server to start on $repoRoot")
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

  companion object {
    const val JAR_ENV = "ANDROID_DESIGN_PREVIEW_JAR"
    const val NO_OPEN_ENV = "ANDROID_DESIGN_NO_OPEN"
    const val SCRIPT_ENV = "ANDROID_DESIGN_PREVIEW_SCRIPT"
    const val CLIENT_TIMEOUT_ENV = "ANDROID_DESIGN_CLIENT_TIMEOUT_MS"
    private const val STARTUP_WAIT_MS = 15_000L
    private const val COLD_CLIENT_DEADLINE_MS = 35_000
    private const val WARM_SCOPED_CLIENT_DEADLINE_MS = 8_000
    private const val WARM_FULL_CLIENT_DEADLINE_MS = 35_000
    private const val STOP_WAIT_MS = 3_000L
    /** How far the process clock may run ahead of the server's recorded `startedAt`. */
    private const val START_CLOCK_SLACK_MS = 2_000L
    /** The longest a server takes from process start to recording `startedAt`. */
    private const val MAX_SERVER_BOOT_MS = 120_000L
  }
}
