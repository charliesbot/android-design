package bot.charlies.androiddesign.preview.app.client

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.app.server.CheckScope
import bot.charlies.androiddesign.preview.app.server.ContentHasher
import bot.charlies.androiddesign.preview.app.server.LiveFocusRequest
import bot.charlies.androiddesign.preview.app.server.RuntimeIdentity
import bot.charlies.androiddesign.preview.app.server.ServerPaths
import bot.charlies.androiddesign.preview.app.server.ServerRecord
import bot.charlies.androiddesign.preview.app.server.StatusResponse
import bot.charlies.androiddesign.preview.app.server.TimeoutEntry
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.model.PreviewJson
import com.sun.net.httpserver.HttpServer
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.junit.jupiter.api.io.TempDir

class PreviewClientSessionTest {

  private fun setupEnv(
    tmp: Path,
    extraEnv: Map<String, String> = emptyMap(),
  ): Triple<Path, Path, Map<String, String>> {
    val cache = tmp.resolve("cache")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val bdir = tmp.resolve("bootstrap").also { Files.createDirectories(it) }
    Files.writeString(bdir.resolve("server.cp"), "-cp\nfoo.jar\n")
    Files.writeString(
      bdir.resolve("bootstrap-meta.properties"),
      """
      androidJar=/tmp/android.jar
      d8Jar=/tmp/d8.jar
      bundleJar=/tmp/bundle.jar
      apkPath=/tmp/host.apk
      composeCompilerPluginJar=/tmp/compose.jar
      """
        .trimIndent(),
    )
    val env =
      mapOf(
        BootstrapMeta.BOOTSTRAP_DIR_ENV to bdir.toString(),
        RuntimeIdentity.SERVER_CP_ENV to bdir.resolve("server.cp").toString(),
      ) + extraEnv
    return Triple(cache, repo, env)
  }

  private fun writeRecord(
    cache: Path,
    repo: Path,
    port: Int,
    identity: RuntimeIdentity,
  ): ServerRecord =
    ServerRecord(
        pid = 999_999_123L,
        port = port,
        jarVersion = AppInfo.VERSION,
        startedAt = 1_000L,
        repoRoot = repo.toString(),
        clientSecret = "sec",
        viewToken = "view",
        backend = identity.backend,
        runtimeHash = identity.runtimeHash,
      )
      .also { ServerPaths.writeRecord(ServerPaths.sharedRecordPath(cache), it) }

  private fun readyLookup(
    rec: ServerRecord,
    repo: Path,
    identity: RuntimeIdentity,
    activeFile: String? = null,
  ): PreviewClient.ServerLookup.Ready =
    PreviewClient.ServerLookup.Ready(
      record = rec,
      status =
        StatusResponse(
          pid = rec.pid,
          port = rec.port,
          jarVersion = AppInfo.VERSION,
          startedAt = 1_000L,
          compileCount = 1,
          repoRoot = repo.toString(),
          boardUrl = rec.boardUrl(),
          activeFile = activeFile,
          backend = identity.backend,
          runtimeHash = identity.runtimeHash,
        ),
    )

  @Test
  fun `CheckScope formats keys and matches recorded timeouts`() {
    assertFalse(CheckScope().isScoped)
    assertEquals("full", CheckScope().key)
    val scoped = CheckScope(direction = "Main", screen = "home", view = "light")
    assertTrue(scoped.isScoped)
    assertEquals("dir=Main,screen=home,view=light", scoped.key)

    val rec =
      ServerRecord(
        pid = 1L,
        port = 4000,
        jarVersion = "1",
        startedAt = 1L,
        repoRoot = "/repo",
        clientSecret = "s",
        viewToken = "v",
        timeouts =
          listOf(
            TimeoutEntry(contentHash = "h1", scope = "Main/home+dialog/light@2x", recordedAt = 1L),
            TimeoutEntry(contentHash = "h2", scope = "compile", recordedAt = 1L),
          ),
      )

    assertTrue(
      rec.hasTimeout("h1", CheckScope(direction = "Main", screen = "home", view = "light")),
    )
    assertTrue(rec.hasTimeout("h1", CheckScope(direction = "Main", view = "light")))
    assertTrue(rec.hasTimeout("h1", CheckScope()))
    assertFalse(
      rec.hasTimeout("h1", CheckScope(direction = "Main", screen = "detail", view = "light")),
    )
    assertFalse(
      rec.hasTimeout("h1", CheckScope(direction = "Main", screen = "home", view = "dark")),
    )
    assertTrue(rec.hasTimeout("h2", CheckScope(direction = "Other", view = "desktop")))
    assertFalse(rec.hasTimeout("h3", CheckScope()))
  }

  @Test
  fun `postScoped classifies HTTP statuses, replacement, deadline recovery, and watchdog halt`(
    @TempDir tmp: Path,
  ) {
    val (cache, repo, env) = setupEnv(tmp, mapOf(PreviewClient.CLIENT_TIMEOUT_ENV to "200"))
    val source = repo.resolve("Demo.explore.kt").also { Files.writeString(it, "val x = 1") }
    val snapshot = ContentHasher.snapshot(source)
    val identity = RuntimeIdentity.compute(env)
    val client = PreviewClient(cacheRoot = cache, env = env, progress = {})
    val scope = CheckScope(direction = "Dir", view = "light")

    val cfg =
      object {
        @Volatile var statusToReturn = 200
        @Volatile var bodyToReturn = """{"ok":true}"""
        @Volatile var sleepMs = 0L
        @Volatile var closeOnCheck = false
        @Volatile var recordTimeoutOnCheck = false
        @Volatile var writeReplacedOnCheck = false
      }

    val httpExecutor = Executors.newCachedThreadPool()
    val http = HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0)
    http.executor = httpExecutor
    var rec = writeRecord(cache, repo, http.address.port, identity)
    http.createContext("/api/status") { ex ->
      val body =
        PreviewJson.compact.encodeToString(
          StatusResponse.serializer(),
          readyLookup(rec, repo, identity).status,
        )
      val bytes = body.toByteArray(StandardCharsets.UTF_8)
      ex.sendResponseHeaders(200, bytes.size.toLong())
      ex.responseBody.use { it.write(bytes) }
    }
    http.createContext("/api/check") { ex ->
      val delayMs = cfg.sleepMs
      val markReplaced = cfg.writeReplacedOnCheck
      val shouldClose = cfg.closeOnCheck
      val recordTimeout = cfg.recordTimeoutOnCheck
      val status = cfg.statusToReturn
      val bodyText = cfg.bodyToReturn
      if (delayMs > 0L) {
        if (markReplaced) {
          ServerPaths.updateRecord(ServerPaths.sharedRecordPath(cache)) {
            it.copy(replacedByVersion = "9.9.9")
          }
        }
        Thread.sleep(delayMs)
      }
      if (shouldClose) {
        if (recordTimeout) {
          ServerPaths.updateRecord(ServerPaths.sharedRecordPath(cache)) {
            it.copy(timeouts = it.timeouts + TimeoutEntry(snapshot.contentHash, scope.key, 1L))
          }
        }
        ex.close()
        return@createContext
      }
      val bytes = bodyText.toByteArray(StandardCharsets.UTF_8)
      ex.sendResponseHeaders(status, bytes.size.toLong())
      ex.responseBody.use { it.write(bytes) }
    }
    http.start()
    try {
      val ready = readyLookup(rec, repo, identity)

      // 200 -> Ok
      val ok = client.postScoped(ready, repo, "/api/check", "{}", snapshot, scope)
      assertEquals(PreviewClient.PostOutcome.Ok("""{"ok":true}"""), ok)

      // 400 -> ClientError
      cfg.statusToReturn = 400
      cfg.bodyToReturn = """{"error":"bad"}"""
      val cliErr = client.postScoped(ready, repo, "/api/check", "{}", snapshot, scope)
      assertEquals(PreviewClient.PostOutcome.ClientError(400, """{"error":"bad"}"""), cliErr)

      // 422 / 500 -> ServerError
      cfg.statusToReturn = 422
      cfg.bodyToReturn = """{"error":"compile"}"""
      val srvErr = client.postScoped(ready, repo, "/api/check", "{}", snapshot, scope)
      assertEquals(PreviewClient.PostOutcome.ServerError(422, """{"error":"compile"}"""), srvErr)

      // 409 -> Replaced
      cfg.statusToReturn = 409
      cfg.bodyToReturn = """{"error":"server_replaced"}"""
      val replaced409 = client.postScoped(ready, repo, "/api/check", "{}", snapshot, scope)
      assertIs<PreviewClient.PostOutcome.Replaced>(replaced409)
      assertEquals("""{"error":"server_replaced"}""", replaced409.body)

      // SocketTimeoutException on responsive server -> DeadlineExceeded(restarted = false)
      cfg.statusToReturn = 200
      cfg.sleepMs = 450L
      val deadline = client.postScoped(ready, repo, "/api/check", "{}", snapshot, scope)
      assertEquals(PreviewClient.PostOutcome.DeadlineExceeded(200, restarted = false), deadline)

      // SocketTimeoutException after replacement recorded -> Replaced
      cfg.writeReplacedOnCheck = true
      val timeoutReplaced = client.postScoped(ready, repo, "/api/check", "{}", snapshot, scope)
      assertEquals(PreviewClient.PostOutcome.Replaced("9.9.9"), timeoutReplaced)
      cfg.writeReplacedOnCheck = false
      cfg.sleepMs = 0L
      rec = writeRecord(cache, repo, http.address.port, identity)

      // Connection drop after watchdog recorded timeout -> WatchdogHalted
      cfg.closeOnCheck = true
      cfg.recordTimeoutOnCheck = true
      val halted = client.postScoped(ready, repo, "/api/check", "{}", snapshot, scope)
      assertEquals(PreviewClient.PostOutcome.WatchdogHalted(scope), halted)

      // Connection drop to dead port -> Unreachable
      val deadPort = ServerSocket(0, 0, InetAddress.getLoopbackAddress()).use { it.localPort }
      val deadRec = writeRecord(cache, repo, deadPort, identity)
      val unreachable =
        client.postScoped(
          readyLookup(deadRec, repo, identity),
          repo,
          "/api/check",
          "{}",
          snapshot,
          scope,
        )
      assertIs<PreviewClient.PostOutcome.Unreachable>(unreachable)
    } finally {
      http.stop(0)
      httpExecutor.shutdownNow()
    }
  }

  @Test
  fun `OpenCommand opens browser only by default and starts live session only with --live`(
    @TempDir tmp: Path,
  ) {
    val (cache, repo, baseEnv) = setupEnv(tmp)
    val source =
      repo.resolve("OpenMe.explore.kt").also { Files.writeString(it, "val preview = Unit") }
    val identity = RuntimeIdentity.compute(baseEnv)
    val liveRequests = CopyOnWriteArrayList<LiveFocusRequest>()
    val checkCalls = CopyOnWriteArrayList<String>()
    var activeFileOnStatus: String? = null

    val http = HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0)
    val rec = writeRecord(cache, repo, http.address.port, identity)
    http.createContext("/api/status") { ex ->
      val body =
        PreviewJson.compact.encodeToString(
          StatusResponse.serializer(),
          readyLookup(rec, repo, identity, activeFile = activeFileOnStatus).status,
        )
      val bytes = body.toByteArray(StandardCharsets.UTF_8)
      ex.sendResponseHeaders(200, bytes.size.toLong())
      ex.responseBody.use { it.write(bytes) }
    }
    http.createContext("/api/check") { ex ->
      checkCalls += ex.requestBody.bufferedReader(StandardCharsets.UTF_8).readText()
      val bytes =
        """{"ok":true,"valid":true,"pass":true,"scoped":false}"""
          .toByteArray(StandardCharsets.UTF_8)
      ex.sendResponseHeaders(200, bytes.size.toLong())
      ex.responseBody.use { it.write(bytes) }
    }
    http.createContext("/api/live") { ex ->
      val body = ex.requestBody.bufferedReader(StandardCharsets.UTF_8).readText()
      liveRequests += PreviewJson.compact.decodeFromString(LiveFocusRequest.serializer(), body)
      val bytes =
        """{"ok":true,"repoId":"repo","boardUrl":"http://127.0.0.1:${http.address.port}","deviceSerial":"emulator-5554","adbForwardPort":38420}"""
          .toByteArray(StandardCharsets.UTF_8)
      ex.sendResponseHeaders(200, bytes.size.toLong())
      ex.responseBody.use { it.write(bytes) }
    }
    http.start()
    try {
      // Case 1: cold server (activeFile == null) without --live -> /api/check + browser open,
      // no /api/live call.
      val openedUrls = mutableListOf<String>()
      val openClient = PreviewClient(cacheRoot = cache, env = baseEnv, progress = {})
      val code1 =
        OpenCommand(
            client = openClient,
            openBrowser = { url ->
              openedUrls += url
              true
            },
          )
          .run(
            listOf(source.toString()),
            PrintStream(ByteArrayOutputStream()),
            PrintStream(ByteArrayOutputStream()),
          )
      assertEquals(ClientExitCode.OK, code1)
      assertEquals(1, checkCalls.size)
      assertTrue(liveRequests.isEmpty(), "open without --live must not call /api/live")
      assertEquals(1, openedUrls.size)

      // Case 2: warm server with NO_OPEN_ENV=1 and --live --avd Pixel_10 -> calls /api/live
      // with startLive=true, avd="Pixel_10", skips browser, and returns deviceSerial.
      activeFileOnStatus = source.toString()
      openedUrls.clear()
      val quietClient =
        PreviewClient(
          cacheRoot = cache,
          env = baseEnv + (PreviewClient.NO_OPEN_ENV to "1"),
          progress = {},
        )
      val liveOut = ByteArrayOutputStream()
      val code2 =
        OpenCommand(
            client = quietClient,
            openBrowser = { url ->
              openedUrls += url
              true
            },
          )
          .run(
            listOf("--live", "--avd", "Pixel_10", source.toString()),
            PrintStream(liveOut),
            PrintStream(ByteArrayOutputStream()),
          )
      assertEquals(ClientExitCode.OK, code2)
      assertEquals(1, liveRequests.size)
      val req = liveRequests.single()
      assertTrue(req.startLive)
      assertEquals("Pixel_10", req.avd)
      assertTrue(openedUrls.isEmpty())
      val parsed =
        PreviewJson.compact.decodeFromString(
          OpenCommand.OpenResponse.serializer(),
          liveOut.toString(StandardCharsets.UTF_8).trim(),
        )
      assertEquals("emulator-5554", parsed.deviceSerial)

      // Case 3: --avd without --live -> USAGE error.
      val badCode =
        OpenCommand(client = quietClient, openBrowser = { false })
          .run(
            listOf("--avd", "Pixel_10"),
            PrintStream(ByteArrayOutputStream()),
            PrintStream(ByteArrayOutputStream()),
          )
      assertEquals(ClientExitCode.USAGE, badCode)
    } finally {
      http.stop(0)
    }
  }
}
