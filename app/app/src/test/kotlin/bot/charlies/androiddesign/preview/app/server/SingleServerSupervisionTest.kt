package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.client.ClientExitCode
import bot.charlies.androiddesign.preview.model.DirectionMeta
import bot.charlies.androiddesign.preview.model.PhoneSurface
import bot.charlies.androiddesign.preview.model.PreviewJson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class SingleServerSupervisionTest {

  private fun writeSampleExplore(
    repoRoot: Path,
    name: String = "Demo.explore.kt",
    label: String = "Hello",
  ): Path {
    val file = repoRoot.resolve(name)
    Files.writeString(
      file,
      """
      import androidx.compose.material3.Text
      import bot.charlies.androiddesign.preview.kit.designBoard

      val preview = designBoard(name = "Board") {
        direction("Dir") {
          screen("home") {
            Text("$label")
          }
        }
      }
      """
        .trimIndent(),
    )
    return file
  }

  private val fakeDirections =
    listOf(DirectionMeta("Dir", "phone", "home", listOf("home"), emptyList()))

  private fun httpCall(
    url: String,
    method: String = "GET",
    secret: String? = null,
    body: String? = null,
  ): Pair<Int, String> {
    val conn = (URI(url).toURL().openConnection() as HttpURLConnection)
    conn.requestMethod = method
    conn.connectTimeout = 2_000
    conn.readTimeout = 5_000
    if (secret != null) conn.setRequestProperty("X-Client-Secret", secret)
    if (body != null) {
      conn.doOutput = true
      conn.setRequestProperty("Content-Type", "application/json")
      conn.outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) }
    }
    val code = conn.responseCode
    val stream = if (code in 200..399) conn.inputStream else (conn.errorStream ?: conn.inputStream)
    val text = stream.bufferedReader(StandardCharsets.UTF_8).readText()
    conn.disconnect()
    return code to text
  }

  @Test
  fun `isolates renders and SSE streams per repo, enforces auth 403s, and transfers live ownership`(
    @TempDir tmp: Path,
  ) {
    val cache = tmp.resolve("cache")
    val repoA = tmp.resolve("repo-a").also { Files.createDirectories(it.resolve(".git")) }
    val repoB = tmp.resolve("repo-b").also { Files.createDirectories(it.resolve(".git")) }
    val fileA = writeSampleExplore(repoA, "A.explore.kt", "From A")
    val fileB = writeSampleExplore(repoB, "B.explore.kt", "From B")

    val liveOwners = CopyOnWriteArrayList<String>()
    val concurrentRenders = AtomicInteger(0)
    val maxConcurrent = AtomicInteger(0)

    val worker =
      InMemoryRenderWorker(
        name = RuntimeIdentity.BACKEND_LAYOUTLIB,
        directions = fakeDirections,
        views = PhoneSurface.views,
        aroundRenderBatch = { _, proceed ->
          val cur = concurrentRenders.incrementAndGet()
          maxConcurrent.updateAndGet { maxOf(it, cur) }
          try {
            Thread.sleep(25)
            proceed()
          } finally {
            concurrentRenders.decrementAndGet()
          }
        },
      )
    val liveHost =
      object : LiveSessionHost {
        override val isActive: Boolean = true
        override val deviceSerial: String? = null
        override val adbForwardPort: Int? = null

        override fun startSession(
          serial: String?,
          avd: String?,
          entry: CompileEntry?,
          update: LivePreviewUpdate?,
        ): LiveSessionStartResult = LiveSessionStartResult.Ready("emulator-5554", 38_420)

        override fun updateIfActive(entry: CompileEntry, update: LivePreviewUpdate) {
          liveOwners += entry.sourcePath.toAbsolutePath().normalize().toString()
        }
      }

    val server =
      PreviewHttpServer(
        repoRoot = repoA,
        cacheRoot = cache,
        preferredPort = 0,
        worker = worker,
        liveSessionHost = liveHost,
        enableFileWatcher = false,
      )
    val record = server.start()
    try {
      assertEquals(
        ServerPaths.sharedRecordPath(cache).toAbsolutePath().normalize(),
        server.recordPath,
      )
      val base = "http://127.0.0.1:${record.port}"
      val repoIdA = ServerPaths.repoHash(repoA)
      val repoIdB = ServerPaths.repoHash(repoB)

      // 1. Secret & viewToken 403 enforcement.
      val (noSecCode, _) =
        httpCall(
          "$base/api/check",
          method = "POST",
          secret = "wrong-secret",
          body = """{"path":"$fileA"}""",
        )
      assertEquals(403, noSecCode)
      val (badViewCode, _) = httpCall("$base/?view=bad-token&repo=$repoIdA")
      assertEquals(403, badViewCode)
      val (badLiveCode, _) =
        httpCall(
          "$base/api/live",
          method = "POST",
          secret = "wrong",
          body = """{"path":"$fileA"}""",
        )
      assertEquals(403, badLiveCode)

      // 2. Open SSE listener strictly for repoB.
      val repoBEvents = CopyOnWriteArrayList<String>()
      val sseConn =
        (URI("$base/api/events?view=${record.viewToken}&repo=$repoIdB").toURL().openConnection()
          as HttpURLConnection)
      sseConn.connectTimeout = 2_000
      sseConn.readTimeout = 3_000
      val sseReady = CountDownLatch(1)
      val sseExec = Executors.newSingleThreadExecutor()
      sseExec.submit {
        BufferedReader(InputStreamReader(sseConn.inputStream, StandardCharsets.UTF_8)).use { reader
          ->
          sseReady.countDown()
          while (true) {
            val line =
              try {
                reader.readLine()
              } catch (_: Exception) {
                break
              } ?: break
            if (line.startsWith("data:") || line.startsWith("event:")) repoBEvents += line
          }
        }
      }
      assertTrue(sseReady.await(2, TimeUnit.SECONDS))

      // 3. Run concurrent checks for repoA and repoB -> serialized queue + isolated render dirs.
      val pool = Executors.newFixedThreadPool(2)
      val futA =
        pool.submit<Pair<Int, String>> {
          httpCall(
            "$base/api/check",
            method = "POST",
            secret = record.clientSecret,
            body =
              PreviewJson.compact.encodeToString(
                CheckRequest.serializer(),
                CheckRequest(path = fileA.toString(), repoRoot = repoA.toString()),
              ),
          )
        }
      val futB =
        pool.submit<Pair<Int, String>> {
          httpCall(
            "$base/api/check",
            method = "POST",
            secret = record.clientSecret,
            body =
              PreviewJson.compact.encodeToString(
                CheckRequest.serializer(),
                CheckRequest(path = fileB.toString(), repoRoot = repoB.toString()),
              ),
          )
        }
      val (codeA, bodyA) = futA.get(5, TimeUnit.SECONDS)
      val (codeB, bodyB) = futB.get(5, TimeUnit.SECONDS)
      pool.shutdown()
      assertEquals(200, codeA, bodyA)
      assertEquals(200, codeB, bodyB)
      assertEquals(1, maxConcurrent.get(), "RenderQueue must serialize across repos")

      val respA = PreviewJson.compact.decodeFromString(CheckResponse.serializer(), bodyA)
      val respB = PreviewJson.compact.decodeFromString(CheckResponse.serializer(), bodyB)
      assertTrue(respA.images.any { ServerPaths.repoHash(repoA) in it.path })
      assertTrue(respB.images.any { ServerPaths.repoHash(repoB) in it.path })

      // Status for an uncompiled repo C must not leak repo A or repo B activeFile/repoRoot.
      val repoC = tmp.resolve("repo-c").also { Files.createDirectories(it.resolve(".git")) }
      val repoIdC = ServerPaths.repoHash(repoC)
      val (stCCode, stCBody) =
        httpCall("$base/api/status?repo=$repoIdC", secret = record.clientSecret)
      assertEquals(200, stCCode, stCBody)
      val statusC = PreviewJson.compact.decodeFromString(StatusResponse.serializer(), stCBody)
      assertEquals("", statusC.repoRoot)
      assertEquals(null, statusC.activeFile)
      assertTrue("?repo=$repoIdC&" in statusC.boardUrl, statusC.boardUrl)

      // Board payloads are isolated per ?repo=<id>.
      val (bACode, bABody) = httpCall("$base/api/board?view=${record.viewToken}&repo=$repoIdA")
      val (bBCode, bBBody) = httpCall("$base/api/board?view=${record.viewToken}&repo=$repoIdB")
      assertEquals(200, bACode)
      assertEquals(200, bBCode)
      assertTrue(fileA.toAbsolutePath().normalize().toString() in bABody)
      assertFalse(fileB.toAbsolutePath().normalize().toString() in bABody)
      assertTrue(fileB.toAbsolutePath().normalize().toString() in bBBody)

      Thread.sleep(100)
      val joinedB = repoBEvents.joinToString("\n")
      assertFalse(
        fileA.toAbsolutePath().normalize().toString() in joinedB,
        "repoB SSE stream must not receive repoA board events",
      )

      // 4. Transfer live ownership cleanly via POST /api/live.
      val (liveCode, _) =
        httpCall(
          "$base/api/live",
          method = "POST",
          secret = record.clientSecret,
          body =
            PreviewJson.compact.encodeToString(
              LiveFocusRequest.serializer(),
              LiveFocusRequest(path = fileA.toString(), repoRoot = repoA.toString()),
            ),
        )
      assertEquals(200, liveCode)
      assertEquals(fileA.toAbsolutePath().normalize().toString(), liveOwners.last())

      sseConn.disconnect()
      sseExec.shutdownNow()
    } finally {
      server.stop()
    }
  }

  @Test
  fun `headless FileWatcher speculatively precompiles and subsequent check hits cache`(
    @TempDir tmp: Path,
  ) {
    val cache = tmp.resolve("cache")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val source = writeSampleExplore(repo, "Watch.explore.kt", "Initial")

    val compileCount = AtomicInteger(0)
    val compiledLatches = AtomicReference(CountDownLatch(1))
    val worker =
      InMemoryRenderWorker(
        name = "fake-watch",
        directions = fakeDirections,
        views = PhoneSurface.views,
        onCompile = {
          compileCount.incrementAndGet()
          compiledLatches.get().countDown()
        },
      )

    val server =
      PreviewHttpServer(
        repoRoot = repo,
        cacheRoot = cache,
        preferredPort = 0,
        worker = worker,
        enableFileWatcher = true,
      )
    val record = server.start()
    try {
      val base = "http://127.0.0.1:${record.port}"
      val (c1, b1) =
        httpCall(
          "$base/api/check",
          method = "POST",
          secret = record.clientSecret,
          body =
            PreviewJson.compact.encodeToString(
              CheckRequest.serializer(),
              CheckRequest(path = source.toString(), repoRoot = repo.toString()),
            ),
        )
      assertEquals(200, c1, b1)
      val countAfterFirst = compileCount.get()

      // Edit file while NO SSE clients are open -> FileWatcher triggers headless SPECULATIVE
      // compile.
      compiledLatches.set(CountDownLatch(1))
      writeSampleExplore(repo, "Watch.explore.kt", "Edited copy")
      assertTrue(
        compiledLatches.get().await(5, TimeUnit.SECONDS),
        "Headless FileWatcher must speculatively recompile on file edit",
      )
      val countAfterSpeculative = compileCount.get()
      assertEquals(countAfterFirst + 1, countAfterSpeculative)

      // Subsequent agent check hits cache without recompiling.
      val (c2, b2) =
        httpCall(
          "$base/api/check",
          method = "POST",
          secret = record.clientSecret,
          body =
            PreviewJson.compact.encodeToString(
              CheckRequest.serializer(),
              CheckRequest(path = source.toString(), repoRoot = repo.toString()),
            ),
        )
      assertEquals(200, c2, b2)
      assertEquals(
        countAfterSpeculative,
        compileCount.get(),
        "Agent check after speculative compile must hit compile cache",
      )
    } finally {
      server.stop()
    }
  }

  @Test
  fun `stop replacement fails in-flight check immediately with 409 and replacedByVersion`(
    @TempDir tmp: Path,
  ) {
    val cache = tmp.resolve("cache")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val source = writeSampleExplore(repo, "Slow.explore.kt", "Slow")

    val renderStarted = CountDownLatch(1)
    val worker =
      InMemoryRenderWorker(
        name = "slow",
        directions = fakeDirections,
        views = PhoneSurface.views,
        onRenderBatch = {
          renderStarted.countDown()
          Thread.sleep(10_000)
        },
      )

    val server =
      PreviewHttpServer(
        repoRoot = repo,
        cacheRoot = cache,
        preferredPort = 0,
        worker = worker,
        enableFileWatcher = false,
      )
    val record = server.start()
    val exec = Executors.newSingleThreadExecutor()
    try {
      val base = "http://127.0.0.1:${record.port}"
      val t0 = System.nanoTime()
      val checkFuture =
        exec.submit<Pair<Int, String>> {
          httpCall(
            "$base/api/check",
            method = "POST",
            secret = record.clientSecret,
            body =
              PreviewJson.compact.encodeToString(
                CheckRequest.serializer(),
                CheckRequest(path = source.toString(), repoRoot = repo.toString()),
              ),
          )
        }
      assertTrue(renderStarted.await(3, TimeUnit.SECONDS))
      val tStop = System.nanoTime()
      val (stopCode, _) =
        httpCall(
          "$base/api/stop",
          method = "POST",
          secret = record.clientSecret,
          body =
            PreviewJson.compact.encodeToString(
              StopRequest.serializer(),
              StopRequest(replacedByVersion = "9.9.9"),
            ),
        )
      assertEquals(200, stopCode)
      val (checkCode, checkBody) = checkFuture.get(1500, TimeUnit.MILLISECONDS)
      val elapsedAfterStopMs = (System.nanoTime() - tStop) / 1_000_000L
      assertEquals(409, checkCode, "Expected 409 on displaced check, got $checkCode ($checkBody)")
      assertTrue("replaced by v9.9.9, rerun" in checkBody, checkBody)
      assertTrue(
        elapsedAfterStopMs < 1_000L,
        "Expected <1s failure after stop, took ${elapsedAfterStopMs}ms (total ${(System.nanoTime() - t0)/1_000_000L}ms)",
      )
    } finally {
      exec.shutdownNow()
      server.stop()
    }
  }

  @Test
  fun `POST api live starts LiveSessionHost only when startLive is true and maps failures cleanly`(
    @TempDir tmp: Path,
  ) {
    val cache = tmp.resolve("cache")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val startCount = AtomicInteger(0)
    var nextStartResult: LiveSessionStartResult =
      LiveSessionStartResult.Ready("emulator-5554", 38_420)
    val liveHost =
      object : LiveSessionHost {
        override var isActive: Boolean = false
        override val deviceSerial: String?
          get() = if (isActive) "emulator-5554" else null

        override val adbForwardPort: Int?
          get() = if (isActive) 38_420 else null

        override fun startSession(
          serial: String?,
          avd: String?,
          entry: CompileEntry?,
          update: LivePreviewUpdate?,
        ): LiveSessionStartResult {
          startCount.incrementAndGet()
          if (nextStartResult is LiveSessionStartResult.Ready) isActive = true
          return nextStartResult
        }

        override fun updateIfActive(entry: CompileEntry, update: LivePreviewUpdate) {}
      }
    val worker =
      InMemoryRenderWorker(
        name = RuntimeIdentity.BACKEND_LAYOUTLIB,
        directions = fakeDirections,
        views = PhoneSurface.views,
      )
    val server =
      PreviewHttpServer(
        repoRoot = repo,
        cacheRoot = cache,
        preferredPort = 0,
        worker = worker,
        liveSessionHost = liveHost,
        enableFileWatcher = false,
      )
    val record = server.start()
    try {
      val base = "http://127.0.0.1:${record.port}"
      val (cFalse, _) =
        httpCall(
          "$base/api/live",
          method = "POST",
          secret = record.clientSecret,
          body =
            PreviewJson.compact.encodeToString(
              LiveFocusRequest.serializer(),
              LiveFocusRequest(repoRoot = repo.toString(), startLive = false),
            ),
        )
      assertEquals(200, cFalse)
      assertEquals(0, startCount.get())

      nextStartResult = LiveSessionStartResult.Failed(ClientExitCode.USAGE, "usage", "Bad avd")
      val (cUsage, bUsage) =
        httpCall(
          "$base/api/live",
          method = "POST",
          secret = record.clientSecret,
          body =
            PreviewJson.compact.encodeToString(
              LiveFocusRequest.serializer(),
              LiveFocusRequest(repoRoot = repo.toString(), avd = "missing", startLive = true),
            ),
        )
      assertEquals(400, cUsage, bUsage)

      nextStartResult =
        LiveSessionStartResult.Failed(ClientExitCode.NOT_READY, "device_not_ready", "No device")
      val (cNotReady, bNotReady) =
        httpCall(
          "$base/api/live",
          method = "POST",
          secret = record.clientSecret,
          body =
            PreviewJson.compact.encodeToString(
              LiveFocusRequest.serializer(),
              LiveFocusRequest(repoRoot = repo.toString(), startLive = true),
            ),
        )
      assertEquals(503, cNotReady, bNotReady)

      nextStartResult = LiveSessionStartResult.Ready("emulator-5554", 38_420)
      val (cTrue, bTrue) =
        httpCall(
          "$base/api/live",
          method = "POST",
          secret = record.clientSecret,
          body =
            PreviewJson.compact.encodeToString(
              LiveFocusRequest.serializer(),
              LiveFocusRequest(repoRoot = repo.toString(), startLive = true),
            ),
        )
      assertEquals(200, cTrue, bTrue)
      val resp = PreviewJson.compact.decodeFromString(LiveFocusResponse.serializer(), bTrue)
      assertEquals("emulator-5554", resp.deviceSerial)
      assertEquals(38_420, resp.adbForwardPort)
      assertEquals(3, startCount.get())
    } finally {
      server.stop()
    }
  }
}
