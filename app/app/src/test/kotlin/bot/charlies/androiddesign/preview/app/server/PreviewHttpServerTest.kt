package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.cli.RenderEntry
import bot.charlies.androiddesign.preview.app.cli.RenderReport
import bot.charlies.androiddesign.preview.model.BoardSurface
import bot.charlies.androiddesign.preview.model.DeviceDirectionMeta
import bot.charlies.androiddesign.preview.model.DeviceOverlayMeta
import bot.charlies.androiddesign.preview.model.PhoneSurface
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PreviewHttpServerTest {
  private fun simpleBoard(title: String = "Plates", failWide: Boolean = false): String =
    """
    import androidx.compose.foundation.layout.*
    import androidx.compose.material3.*
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.platform.testTag
    import androidx.compose.ui.unit.dp
    import bot.charlies.androiddesign.preview.kit.*

    val preview = designBoard("$title", seed = 0xFFB5523BL, variant = PaletteVariant.TonalSpot) {
      direction("Editorial") {
        screen("home") {
          Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            ${if (failWide) """Text("${"Very long paragraph text ".repeat(12)}")""" else """Text("$title")"""}
            Button(
              onClick = {},
              modifier = Modifier.testTag(AD_PRIMARY_TAG).height(56.dp),
            ) {
              Text("Open")
            }
          }
        }
      }
      direction("Compact") {
        screen("home") {
          Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Compact view")
            Button(
              onClick = {},
              modifier = Modifier.testTag(AD_PRIMARY_TAG).height(56.dp),
            ) {
              Text("Go")
            }
          }
        }
      }
    }
    """
      .trimIndent()

  private fun fakeWorker(
    compileCalls: AtomicInteger = AtomicInteger(0),
    onCompile: ((ContentHasher.Snapshot) -> Unit)? = null,
    onRenderSingle: ((String, ViewSpec, Int) -> Unit)? = null,
  ): RenderWorker =
    InMemoryRenderWorker(
      name = RuntimeIdentity.BACKEND_LAYOUTLIB,
      scopedCheckDensity = 1,
      boardName = "Plates",
      directions =
        listOf(
          DeviceDirectionMeta("Editorial", "phone", "home", listOf("home"), emptyList()),
          DeviceDirectionMeta("Compact", "phone", "home", listOf("home"), emptyList()),
        ),
      views = PhoneSurface.views,
      onCompile = { snapshot ->
        compileCalls.incrementAndGet()
        onCompile?.invoke(snapshot)
      },
      onRenderTarget = { target ->
        if (onRenderSingle != null) {
          val view = Views.byName(target.viewName) ?: Views.Light
          onRenderSingle.invoke(target.direction, view, target.densityScale.toInt())
        }
      },
    )

  private fun httpRequest(
    port: Int,
    method: String,
    path: String,
    headers: Map<String, String> = emptyMap(),
    body: String? = null,
  ): Pair<Int, String> {
    val conn = URI("http://127.0.0.1:$port$path").toURL().openConnection() as HttpURLConnection
    return try {
      conn.requestMethod = method
      conn.connectTimeout = 2_000
      conn.readTimeout = 30_000
      conn.setRequestProperty("Host", "127.0.0.1:$port")
      headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
      if (body != null) {
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
      }
      val status = conn.responseCode
      val stream =
        if (status in 200..399) conn.inputStream else (conn.errorStream ?: conn.inputStream)
      status to stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
    } finally {
      conn.disconnect()
    }
  }

  private fun rawHostRequest(port: Int, hostHeader: String, path: String): String {
    Socket("127.0.0.1", port).use { socket ->
      socket.soTimeout = 2_000
      val writer = OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8)
      writer.write("GET $path HTTP/1.1\r\nHost: $hostHeader\r\nConnection: close\r\n\r\n")
      writer.flush()
      return socket.getInputStream().bufferedReader(Charsets.UTF_8).readText()
    }
  }

  @Test
  fun `content-hash cache hits reuse compile and scoped vs full image policy works`() {
    val tmp = createTempDirectory("v6-server-cache-")
    val cache = tmp.resolve("cache")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it) }
    val source = repo.resolve("board.explore.kt").also { Files.writeString(it, simpleBoard()) }
    val compileCalls = AtomicInteger(0)

    val server =
      PreviewHttpServer(
        repoRoot = repo,
        cacheRoot = cache,
        preferredPort = 0,
        worker = fakeWorker(compileCalls = compileCalls),
        enableFileWatcher = false,
      )
    try {
      val rec = server.start()

      // First scoped check compiles once and renders 1x PNG.
      val scopedBody =
        PreviewJson.compact.encodeToString(
          CheckRequest.serializer(),
          CheckRequest(path = source.toString(), direction = "Editorial", view = "light"),
        )
      val (s1, b1) =
        httpRequest(
          rec.port,
          "POST",
          "/api/check",
          headers = mapOf("X-Client-Secret" to rec.clientSecret),
          body = scopedBody,
        )
      assertEquals(200, s1, b1)
      val r1 = PreviewJson.compact.decodeFromString(CheckResponse.serializer(), b1)
      assertTrue(r1.ok && r1.valid && r1.pass && r1.scoped)
      assertFalse(r1.cached)
      assertEquals(1, compileCalls.get())
      assertEquals(1, r1.images.size)
      assertEquals(1, r1.images.single().density)
      assertTrue(Files.isRegularFile(Path.of(r1.images.single().path)))

      // Repeat identical scoped check hits content-hash cache (no recompile).
      val (s2, b2) =
        httpRequest(
          rec.port,
          "POST",
          "/api/check",
          headers = mapOf("X-Client-Secret" to rec.clientSecret),
          body = scopedBody,
        )
      assertEquals(200, s2, b2)
      val r2 = PreviewJson.compact.decodeFromString(CheckResponse.serializer(), b2)
      assertTrue(r2.cached)
      assertEquals(1, compileCalls.get(), "unchanged hash must not recompile")
      assertEquals(r1.images.single().path, r2.images.single().path)

      // Full check on same hash still reuses compiled classes and returns 2x light + desktop PNGs.
      val fullBody =
        PreviewJson.compact.encodeToString(
          CheckRequest.serializer(),
          CheckRequest(path = source.toString()),
        )
      val (s3, b3) =
        httpRequest(
          rec.port,
          "POST",
          "/api/check",
          headers = mapOf("X-Client-Secret" to rec.clientSecret),
          body = fullBody,
        )
      assertEquals(200, s3, b3)
      val r3 = PreviewJson.compact.decodeFromString(CheckResponse.serializer(), b3)
      assertTrue(r3.ok && r3.pass && !r3.scoped)
      assertEquals(1, compileCalls.get(), "full check on same hash must reuse compile")
      assertTrue(r3.images.all { it.density == 2 })
      assertEquals(setOf("light", "desktop"), r3.images.map { it.view }.toSet())
    } finally {
      server.stop()
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `failing full check caps returned PNGs at 2 and deduplicates identical bytes`() {
    val identicalPng = byteArrayOf(1, 2, 3, 4)
    val distinctPngA = byteArrayOf(10, 20, 30, 40)
    val distinctPngB = byteArrayOf(50, 60, 70, 80)

    val dummyCheck =
      bot.charlies.androiddesign.preview.domain.ViewCheck(
        pass = false,
        errors = listOf(bot.charlies.androiddesign.preview.domain.CheckError("line-length")),
      )
    fun fakeArt(viewName: String, png: ByteArray): RenderArtifact {
      val viewSpec =
        bot.charlies.androiddesign.preview.model.Views.all.first { it.name == viewName }
      return RenderArtifact(
        direction = "Editorial",
        screen = "home",
        overlay = null,
        view = viewSpec,
        densityScale = 2,
        artifactName = "home",
        pass = false,
        renderMs = 10L,
        check = dummyCheck,
        layout = null,
        tree = null,
        pngPath = Path.of("/tmp/$viewName.png"),
        endPngPath = null,
        pngBytes = png,
        endPngBytes = null,
        pngSha256 = ContentHasher.sha256Hex(png),
        entry = RenderEntry.of("home", viewName, null, dummyCheck),
      )
    }

    val selected =
      CheckService.selectImagesForResponse(
        artifacts =
          listOf(
            fakeArt("foldable", identicalPng),
            fakeArt("tablet", identicalPng), // duplicate bytes: skipped!
            fakeArt("desktop", distinctPngA),
            fakeArt("dark", distinctPngB), // 3rd distinct failure: capped at 2!
          ),
        scoped = false,
        allPass = false,
        surface = BoardSurface.Phone,
      )
    assertEquals(listOf("foldable", "desktop"), selected.map { it.view })
  }

  @Test
  fun `Host, Origin, clientSecret, and viewToken are enforced with 403`() {
    val tmp = createTempDirectory("v6-server-sec-")
    val server =
      PreviewHttpServer(
        repoRoot = tmp,
        cacheRoot = tmp.resolve("cache"),
        preferredPort = 0,
        worker = fakeWorker(),
        enableFileWatcher = false,
      )
    try {
      val rec = server.start()

      // DNS rebinding Host rejection
      val badHostResp = rawHostRequest(rec.port, "evil.example:${rec.port}", "/api/status")
      assertTrue(badHostResp.startsWith("HTTP/1.1 403"), badHostResp)

      // Client endpoints reject missing/wrong clientSecret and reject viewToken
      val (noSecret, _) = httpRequest(rec.port, "GET", "/api/status")
      assertEquals(403, noSecret)
      val (viewTokenOnClient, _) =
        httpRequest(
          rec.port,
          "POST",
          "/api/check",
          headers = mapOf("X-Client-Secret" to rec.viewToken),
          body = "{}",
        )
      assertEquals(403, viewTokenOnClient)

      // Board endpoints reject missing/wrong viewToken and clientSecret
      val (noBoardTok, _) = httpRequest(rec.port, "GET", "/api/board")
      assertEquals(403, noBoardTok)
      val (clientSecretOnBoard, _) =
        httpRequest(
          rec.port,
          "GET",
          "/api/board",
          headers = mapOf("X-View-Token" to rec.clientSecret),
        )
      assertEquals(403, clientSecretOnBoard)

      // State-changing board POSTs require exact Origin header
      val (missingOrigin, _) =
        httpRequest(
          rec.port,
          "POST",
          "/api/board/focus",
          headers = mapOf("X-View-Token" to rec.viewToken),
          body = """{"direction":"A","view":"light"}""",
        )
      assertEquals(403, missingOrigin)
      val (badOrigin, _) =
        httpRequest(
          rec.port,
          "POST",
          "/api/board/focus",
          headers = mapOf("X-View-Token" to rec.viewToken, "Origin" to "http://evil.example"),
          body = """{"direction":"A","view":"light"}""",
        )
      assertEquals(403, badOrigin)
      val (goodOrigin, _) =
        httpRequest(
          rec.port,
          "POST",
          "/api/board/focus",
          headers =
            mapOf("X-View-Token" to rec.viewToken, "Origin" to "http://127.0.0.1:${rec.port}"),
          body = """{"direction":"A","view":"light"}""",
        )
      assertEquals(200, goodOrigin)
    } finally {
      server.stop()
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `OS-assigned port fallback binds a free port when preferred port is busy`() {
    val tmp = createTempDirectory("v6-server-port-")
    ServerSocket(0, 0, InetAddress.getByName("127.0.0.1")).use { occupied ->
      val busyPort = occupied.localPort
      val server =
        PreviewHttpServer(
          repoRoot = tmp,
          cacheRoot = tmp.resolve("cache"),
          preferredPort = busyPort,
          worker = fakeWorker(),
          enableFileWatcher = false,
        )
      try {
        val rec = server.start()
        assertNotEquals(busyPort, rec.port)
        assertTrue(rec.port > 0)
        val (status, _) =
          httpRequest(
            rec.port,
            "GET",
            "/api/status",
            headers = mapOf("X-Client-Secret" to rec.clientSecret),
          )
        assertEquals(200, status)
      } finally {
        server.stop()
        tmp.toFile().deleteRecursively()
      }
    }
  }

  @Test
  fun `1-view queue preempts board remainder for agent requests and cancels stale jobs on new save`() {
    val tmp = createTempDirectory("v6-queue-")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it) }
    val source = repo.resolve("board.explore.kt").also { Files.writeString(it, simpleBoard("V1")) }
    val snap1 = ContentHasher.snapshot(source)

    val firstRenderStarted = CountDownLatch(1)
    val releaseFirstRender = CountDownLatch(1)
    val order = CopyOnWriteArrayList<String>()

    val worker =
      fakeWorker(
        onRenderSingle = { direction, view, density ->
          order += "$direction/${view.name}@${density}x"
          if (order.size == 1) {
            firstRenderStarted.countDown()
            releaseFirstRender.await(5, TimeUnit.SECONDS)
          }
        },
      )

    val queue = RenderQueue(rendersRoot = tmp.resolve("renders"), executor = CheckExecutor(worker))
    queue.start()
    try {
      val entry1 = queue.submitCompile(snap1, QueuePriority.AGENT).get(5, TimeUnit.SECONDS)
      val dir = assertNotNull(entry1.boardMeta).directions.first().name
      val views = assertNotNull(entry1.boardMeta).views

      // Enqueue 4 low-priority board remainder jobs; first starts and blocks on releaseFirstRender.
      val rem0 =
        queue.submitSingleRender(
          entry1,
          dir,
          "home",
          "home",
          null,
          views[0],
          2,
          null,
          QueuePriority.BOARD_REMAINDER,
        )
      assertTrue(firstRenderStarted.await(5, TimeUnit.SECONDS))
      val rem1 =
        queue.submitSingleRender(
          entry1,
          dir,
          "home",
          "home",
          null,
          views[1],
          2,
          null,
          QueuePriority.BOARD_REMAINDER,
        )
      val rem2 =
        queue.submitSingleRender(
          entry1,
          dir,
          "home",
          "home",
          null,
          views[2],
          2,
          null,
          QueuePriority.BOARD_REMAINDER,
        )

      // Agent request arrives while rem0 is running and rem1/rem2 are queued -> must run BEFORE
      // rem1/rem2!
      val agentJob =
        queue.submitSingleRender(
          entry1,
          dir,
          "home",
          "home",
          null,
          views[3],
          1,
          null,
          QueuePriority.AGENT,
        )

      // Cancel stale remainder jobs for a newer hash before releasing rem0.
      queue.cancelStaleJobs(source, "newer-hash-2")
      releaseFirstRender.countDown()

      assertNotNull(rem0.get(5, TimeUnit.SECONDS))
      assertNotNull(agentJob.get(5, TimeUnit.SECONDS))
      assertTrue(
        rem1.isCompletedExceptionally,
        "queued remainder job should be cancelled on newer save",
      )
      assertTrue(
        rem2.isCompletedExceptionally,
        "queued remainder job should be cancelled on newer save",
      )
      assertEquals(listOf("Editorial/light@2x", "Editorial/tablet@1x"), order)
    } finally {
      queue.stop()
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `watchdog records timeout to server record and subsequent check fast-fails until file changes`() {
    val tmp = createTempDirectory("v6-watchdog-")
    val cache = tmp.resolve("cache")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it) }
    val source = repo.resolve("slow.explore.kt").also { Files.writeString(it, simpleBoard("Slow")) }
    val haltedLatch = CountDownLatch(1)
    val releaseSlow = CountDownLatch(1)

    val server =
      PreviewHttpServer(
        repoRoot = repo,
        cacheRoot = cache,
        preferredPort = 0,
        coldBudgetMs = 200L,
        warmBudgetMs = 200L,
        worker =
          fakeWorker(
            onCompile = { releaseSlow.await(3, TimeUnit.SECONDS) },
          ),
        onWatchdogHalt = {
          haltedLatch.countDown()
          releaseSlow.countDown()
        },
        enableFileWatcher = false,
      )
    try {
      val rec = server.start()
      val reqBody =
        PreviewJson.compact.encodeToString(
          CheckRequest.serializer(),
          CheckRequest(path = source.toString(), direction = "Editorial", view = "light"),
        )
      httpRequest(
        rec.port,
        "POST",
        "/api/check",
        headers = mapOf("X-Client-Secret" to rec.clientSecret),
        body = reqBody,
      )
      assertTrue(haltedLatch.await(5, TimeUnit.SECONDS), "watchdog must fire on slow compile")

      val updatedRecord = assertNotNull(ServerPaths.readRecord(server.recordPath))
      assertTrue(updatedRecord.timeouts.isNotEmpty(), "timeout must be recorded in server record")
      assertNotNull(updatedRecord.haltedAt)

      // Next check for the exact same content hash fast-fails with error="timeout".
      val (s2, b2) =
        httpRequest(
          rec.port,
          "POST",
          "/api/check",
          headers = mapOf("X-Client-Secret" to rec.clientSecret),
          body = reqBody,
        )
      assertEquals(200, s2)
      val r2 = PreviewJson.compact.decodeFromString(CheckResponse.serializer(), b2)
      assertFalse(r2.ok)
      assertEquals("timeout", r2.error)
    } finally {
      server.stop()
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `superseded submitSingleRender is rejected and does not overwrite newer FileHashCache`() {
    val tmp = createTempDirectory("v6-queue-race-")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it) }
    val source = repo.resolve("board.explore.kt").also { Files.writeString(it, simpleBoard("V1")) }
    val snap1 = ContentHasher.snapshot(source)
    val queue =
      RenderQueue(rendersRoot = tmp.resolve("renders"), executor = CheckExecutor(fakeWorker()))
    queue.start()
    try {
      val entry1 = queue.submitCompile(snap1, QueuePriority.AGENT).get(5, TimeUnit.SECONDS)
      Files.writeString(source, simpleBoard("V2"))
      val snap2 = ContentHasher.snapshot(source)
      queue.submitCompile(snap2, QueuePriority.AGENT).get(5, TimeUnit.SECONDS)

      assertTrue(queue.isCurrentHash(source, snap2.contentHash))
      assertFalse(queue.isCurrentHash(source, snap1.contentHash))

      val meta = assertNotNull(entry1.boardMeta)
      val staleRender =
        queue.submitSingleRender(
          entry = entry1,
          direction = meta.directions.first().name,
          screenId = "home",
          prefix = "home",
          overlayId = null,
          view = meta.views.first(),
          densityScale = 2,
          baseCheck = null,
          priority = QueuePriority.BOARD_REMAINDER,
        )
      assertTrue(staleRender.isCompletedExceptionally, "superseded hash render must fail/cancel")
      assertEquals(snap2.contentHash, queue.latestCompiled(source)?.contentHash)
    } finally {
      queue.stop()
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `api render writes PNGs even after board render cached WebP for the same view`() {
    val tmp = createTempDirectory("v6-api-render-")
    val cacheDir = tmp.resolve("cache")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val source =
      repo.resolve("board.explore.kt").also { Files.writeString(it, simpleBoard("WebP")) }
    val outDir = tmp.resolve("renders-out")
    val worker = fakeWorker()
    val server =
      PreviewHttpServer(
        repoRoot = repo,
        cacheRoot = cacheDir,
        preferredPort = 0,
        worker = worker,
        enableFileWatcher = false,
      )
    val rec = server.start()
    val repoId = ServerPaths.repoHash(repo)
    try {
      // Populate cache with a check first.
      val (chkStatus, _) =
        httpRequest(
          rec.port,
          "POST",
          "/api/check",
          headers = mapOf("X-Client-Secret" to rec.clientSecret),
          body = """{"path":"$source","direction":"Editorial","view":"dark","repoRoot":"$repo"}""",
        )
      assertEquals(200, chkStatus)

      // Ask /api/board/render for dark -> caches PNG in RenderQueue.
      val (boardStatus, boardBody) =
        httpRequest(
          rec.port,
          "POST",
          "/api/board/render?view=${rec.viewToken}&repo=$repoId",
          headers = mapOf("Origin" to "http://127.0.0.1:${rec.port}"),
          body = """{"repo":"$repoId","direction":"Editorial","screen":"home","view":"dark"}""",
        )
      assertEquals(200, boardStatus, boardBody)
      assertTrue(boardBody.contains("image/png"), boardBody)

      // Now call /api/render for dark -> writes PNG bytes.
      val (renderStatus, renderBody) =
        httpRequest(
          rec.port,
          "POST",
          "/api/render",
          headers = mapOf("X-Client-Secret" to rec.clientSecret),
          body = """{"path":"$source","outDir":"$outDir","views":["dark"],"repoRoot":"$repo"}""",
        )
      assertEquals(200, renderStatus, renderBody)
      val parsed = PreviewJson.compact.decodeFromString(RenderResponse.serializer(), renderBody)
      assertTrue(parsed.valid, renderBody)
      assertTrue(Files.isRegularFile(outDir.resolve("report.json")))
      val pngBytes = Files.readAllBytes(outDir.resolve("Editorial__home-dark.png"))
      assertEquals(
        listOf(0x89.toByte(), 0x50, 0x4E, 0x47),
        pngBytes.take(4),
        "Expected PNG signature",
      )
    } finally {
      server.stop()
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `api render executes all directions, views, and overlays in one view-grouped batch`() {
    val tmp = createTempDirectory("v6-api-render-batch-")
    val cacheDir = tmp.resolve("cache")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val source =
      repo.resolve("board.explore.kt").also { Files.writeString(it, simpleBoard("Batch")) }
    val outDir = tmp.resolve("renders-out")
    val worker =
      InMemoryRenderWorker(
        name = RuntimeIdentity.BACKEND_LAYOUTLIB,
        directions =
          listOf(
            DeviceDirectionMeta(
              name = "Editorial",
              surface = "phone",
              startScreen = "home",
              screens = listOf("home"),
              overlays = listOf(DeviceOverlayMeta("sheet", "home")),
            ),
            DeviceDirectionMeta(
              name = "Compact",
              surface = "phone",
              startScreen = "home",
              screens = listOf("home"),
              overlays = emptyList(),
            ),
          ),
        views = PhoneSurface.views,
      )
    val server =
      PreviewHttpServer(
        repoRoot = repo,
        cacheRoot = cacheDir,
        preferredPort = 0,
        worker = worker,
        enableFileWatcher = false,
      )
    val rec = server.start()
    try {
      val (status, body) =
        httpRequest(
          rec.port,
          "POST",
          "/api/render",
          headers = mapOf("X-Client-Secret" to rec.clientSecret),
          body =
            """{"path":"$source","outDir":"$outDir","views":["light","desktop"],"repoRoot":"$repo"}""",
        )
      assertEquals(200, status, body)
      val parsed = PreviewJson.compact.decodeFromString(RenderResponse.serializer(), body)
      assertTrue(parsed.ok && parsed.valid && parsed.pass, body)

      // All 6 targets (2 directions x 2 views + 1 overlay x 2 views) run in 1 batch.
      assertEquals(1, worker.renderBatchCount.get())
      val sent = worker.recordedBatches.single()
      assertEquals(
        listOf(
          "Editorial/base/light",
          "Compact/base/light",
          "Editorial/sheet/light",
          "Editorial/base/desktop",
          "Compact/base/desktop",
          "Editorial/sheet/desktop",
        ),
        sent.map { "${it.direction}/${it.overlayId ?: "base"}/${it.viewName}" },
      )

      // report.json keeps deterministic canonical order: Editorial base, Editorial overlay,
      // Compact.
      val report =
        PreviewJson.pretty.decodeFromString(
          RenderReport.serializer(),
          Files.readString(outDir.resolve("report.json")),
        )
      assertEquals(
        listOf(
          "Editorial__home/light",
          "Editorial__home/desktop",
          "Editorial__home+sheet/light",
          "Editorial__home+sheet/desktop",
          "Compact__home/light",
          "Compact__home/desktop",
        ),
        report.renders.map { "${it.screen}/${it.view}" },
      )
    } finally {
      server.stop()
      tmp.toFile().deleteRecursively()
    }
  }

  private class FakeLiveSessionHost(initialActive: Boolean = false) : LiveSessionHost {
    @Volatile override var isActive: Boolean = initialActive
    override val deviceSerial: String?
      get() = if (isActive) "emulator-5554" else null

    override val adbForwardPort: Int?
      get() = if (isActive) 38_420 else null

    val recordedUpdates = CopyOnWriteArrayList<LivePreviewUpdate>()
    val startRequests = CopyOnWriteArrayList<Triple<String?, String?, LivePreviewUpdate?>>()
    val closed = AtomicBoolean(false)
    var onUpdate: (CompileEntry, LivePreviewUpdate) -> Unit = { _, _ -> }
    var startResultProvider:
      (String?, String?, CompileEntry?, LivePreviewUpdate?) -> LiveSessionStartResult =
      { serial, _, _, _ ->
        isActive = true
        LiveSessionStartResult.Ready(serial ?: "emulator-5554", 38_420)
      }

    override fun startSession(
      serial: String?,
      avd: String?,
      entry: CompileEntry?,
      update: LivePreviewUpdate?,
    ): LiveSessionStartResult {
      startRequests += Triple(serial, avd, update)
      return startResultProvider(serial, avd, entry, update)
    }

    override fun updateIfActive(entry: CompileEntry, update: LivePreviewUpdate) {
      if (!isActive) return
      recordedUpdates += update
      onUpdate(entry, update)
    }

    override fun close() {
      closed.set(true)
      isActive = false
    }
  }

  @Test
  fun `api check replies without blocking on slow active live host update`() {
    val tmp = createTempDirectory("v6-async-live-")
    val cacheDir = tmp.resolve("cache")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val source =
      repo.resolve("board.explore.kt").also { Files.writeString(it, simpleBoard("AsyncLive")) }
    val liveStarted = CountDownLatch(1)
    val releaseLive = CountDownLatch(1)
    val liveFinished = CountDownLatch(1)
    val liveHost =
      FakeLiveSessionHost(initialActive = true).apply {
        onUpdate = { _, _ ->
          liveStarted.countDown()
          releaseLive.await(5, TimeUnit.SECONDS)
          liveFinished.countDown()
        }
      }
    val worker =
      InMemoryRenderWorker(
        name = RuntimeIdentity.BACKEND_LAYOUTLIB,
        directions =
          listOf(
            DeviceDirectionMeta("Editorial", "phone", "home", listOf("home"), emptyList()),
          ),
        views = PhoneSurface.views,
      )
    val server =
      PreviewHttpServer(
        repoRoot = repo,
        cacheRoot = cacheDir,
        preferredPort = 0,
        worker = worker,
        liveSessionHost = liveHost,
        enableFileWatcher = false,
      )
    val rec = server.start()
    try {
      val (status, body) =
        httpRequest(
          rec.port,
          "POST",
          "/api/check",
          headers = mapOf("X-Client-Secret" to rec.clientSecret),
          body = """{"path":"$source","direction":"Editorial","view":"light","repoRoot":"$repo"}""",
        )
      assertEquals(200, status, body)
      // HTTP reply returned even though updateIfActive is still blocked on releaseLive.
      assertTrue(liveStarted.await(2, TimeUnit.SECONDS), "updateIfActive must be dispatched async")
      assertEquals(1L, liveFinished.count, "HTTP reply must not wait for updateIfActive")
      releaseLive.countDown()
      assertTrue(
        liveFinished.await(2, TimeUnit.SECONDS),
        "updateIfActive must complete asynchronously",
      )
      assertEquals("Editorial", liveHost.recordedUpdates.single().direction)
    } finally {
      releaseLive.countDown()
      server.stop()
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `explicit api live serializes after in-flight async scheduleLiveUpdate without being overwritten`() {
    val tmp = createTempDirectory("v6-live-serialize-")
    val cacheDir = tmp.resolve("cache")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val source =
      repo.resolve("board.explore.kt").also { Files.writeString(it, simpleBoard("LiveOrder")) }
    val asyncStarted = CountDownLatch(1)
    val releaseAsync = CountDownLatch(1)
    val appliedOrder = CopyOnWriteArrayList<String>()
    val liveHost =
      FakeLiveSessionHost(initialActive = true).apply {
        onUpdate = { _, update ->
          if (update.direction == "Editorial") {
            asyncStarted.countDown()
            releaseAsync.await(5, TimeUnit.SECONDS)
          }
          appliedOrder += update.direction.orEmpty()
        }
      }
    val worker =
      InMemoryRenderWorker(
        name = RuntimeIdentity.BACKEND_LAYOUTLIB,
        directions =
          listOf(
            DeviceDirectionMeta("Editorial", "phone", "home", listOf("home"), emptyList()),
            DeviceDirectionMeta("Compact", "phone", "home", listOf("home"), emptyList()),
          ),
        views = PhoneSurface.views,
      )
    val server =
      PreviewHttpServer(
        repoRoot = repo,
        cacheRoot = cacheDir,
        preferredPort = 0,
        worker = worker,
        liveSessionHost = liveHost,
        enableFileWatcher = false,
      )
    val rec = server.start()
    val clientPool = Executors.newSingleThreadExecutor()
    try {
      // Trigger /api/check, which dispatches an async scheduleLiveUpdate("Editorial") on
      // liveUpdateExecutor and blocks inside updateIfActive.
      val (checkStatus, checkBody) =
        httpRequest(
          rec.port,
          "POST",
          "/api/check",
          headers = mapOf("X-Client-Secret" to rec.clientSecret),
          body = """{"path":"$source","direction":"Editorial","view":"light","repoRoot":"$repo"}""",
        )
      assertEquals(200, checkStatus, checkBody)
      assertTrue(
        asyncStarted.await(5, TimeUnit.SECONDS),
        "async updateIfActive should be in flight",
      )

      // While async updateIfActive("Editorial") is still in flight, send an explicit /api/live
      // focusing "Compact". It must serialize behind the in-flight async call.
      val liveFuture =
        clientPool.submit<Pair<Int, String>> {
          httpRequest(
            rec.port,
            "POST",
            "/api/live",
            headers = mapOf("X-Client-Secret" to rec.clientSecret),
            body =
              """{"path":"$source","direction":"Compact","screen":"home","repoRoot":"$repo"}""",
          )
        }
      Thread.sleep(100)
      assertFalse(
        liveFuture.isDone,
        "explicit /api/live must wait for in-flight async updateIfActive on liveUpdateExecutor",
      )

      releaseAsync.countDown()
      val (liveStatus, liveBody) = liveFuture.get(5, TimeUnit.SECONDS)
      assertEquals(200, liveStatus, liveBody)
      assertEquals(
        listOf("Editorial", "Compact"),
        appliedOrder,
        "explicit /api/live must run strictly after in-flight async update and not be overwritten",
      )
    } finally {
      releaseAsync.countDown()
      clientPool.shutdownNow()
      server.stop()
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `live host remains inactive during check and focus until api live startLive activates it`() {
    val tmp = createTempDirectory("v7-lazy-live-")
    val cacheDir = tmp.resolve("cache")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val source =
      repo.resolve("board.explore.kt").also { Files.writeString(it, simpleBoard("LazyLive")) }
    val liveHost = FakeLiveSessionHost(initialActive = false)
    val worker =
      InMemoryRenderWorker(
        name = RuntimeIdentity.BACKEND_LAYOUTLIB,
        directions =
          listOf(
            DeviceDirectionMeta("Editorial", "phone", "home", listOf("home"), emptyList()),
          ),
        views = PhoneSurface.views,
      )
    val server =
      PreviewHttpServer(
        repoRoot = repo,
        cacheRoot = cacheDir,
        preferredPort = 0,
        worker = worker,
        liveSessionHost = liveHost,
        enableFileWatcher = false,
      )
    val rec = server.start()
    try {
      val (chkStatus, _) =
        httpRequest(
          rec.port,
          "POST",
          "/api/check",
          headers = mapOf("X-Client-Secret" to rec.clientSecret),
          body = """{"path":"$source","direction":"Editorial","view":"light","repoRoot":"$repo"}""",
        )
      assertEquals(200, chkStatus)
      assertFalse(liveHost.isActive)
      assertTrue(liveHost.startRequests.isEmpty())
      assertTrue(liveHost.recordedUpdates.isEmpty())

      val (_, statusBefore) =
        httpRequest(
          rec.port,
          "GET",
          "/api/status",
          headers = mapOf("X-Client-Secret" to rec.clientSecret),
        )
      val parsedBefore =
        PreviewJson.compact.decodeFromString(StatusResponse.serializer(), statusBefore)
      assertNull(parsedBefore.deviceSerial)
      assertNull(parsedBefore.adbForwardPort)

      val (startStatus, startBody) =
        httpRequest(
          rec.port,
          "POST",
          "/api/live",
          headers = mapOf("X-Client-Secret" to rec.clientSecret),
          body =
            """{"path":"$source","direction":"Editorial","screen":"home","repoRoot":"$repo","avd":"Pixel_10_Pro_XL","startLive":true}""",
        )
      assertEquals(200, startStatus, startBody)
      assertTrue(liveHost.isActive)
      assertEquals(1, liveHost.startRequests.size)
      assertEquals("Pixel_10_Pro_XL", liveHost.startRequests.single().second)

      val (_, statusAfter) =
        httpRequest(
          rec.port,
          "GET",
          "/api/status",
          headers = mapOf("X-Client-Secret" to rec.clientSecret),
        )
      val parsedAfter =
        PreviewJson.compact.decodeFromString(StatusResponse.serializer(), statusAfter)
      assertEquals("emulator-5554", parsedAfter.deviceSerial)
      assertEquals(38_420, parsedAfter.adbForwardPort)
    } finally {
      server.stop()
      assertTrue(liveHost.closed.get(), "server.stop() must close LiveSessionHost")
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `board render, focus, and api live stay HTTP 200 when active LiveSessionHost throws on update`() {
    val tmp = createTempDirectory("v7-live-disconnect-http-")
    val cacheDir = tmp.resolve("cache")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val repoId = ServerPaths.repoHash(repo.toAbsolutePath().normalize())
    val source =
      repo.resolve("board.explore.kt").also { Files.writeString(it, simpleBoard("LiveDisconnect")) }
    val updateAttempted = CountDownLatch(2)
    val liveHost =
      FakeLiveSessionHost(initialActive = true).apply {
        onUpdate = { _, _ ->
          updateAttempted.countDown()
          throw IOException("emulator closed")
        }
      }
    val worker =
      InMemoryRenderWorker(
        name = RuntimeIdentity.BACKEND_LAYOUTLIB,
        directions =
          listOf(
            DeviceDirectionMeta("Editorial", "phone", "home", listOf("home"), emptyList()),
          ),
        views = PhoneSurface.views,
      )
    val server =
      PreviewHttpServer(
        repoRoot = repo,
        cacheRoot = cacheDir,
        preferredPort = 0,
        worker = worker,
        liveSessionHost = liveHost,
        enableFileWatcher = false,
      )
    val rec = server.start()
    try {
      val (chkStatus, _) =
        httpRequest(
          rec.port,
          "POST",
          "/api/check",
          headers = mapOf("X-Client-Secret" to rec.clientSecret),
          body = """{"path":"$source","direction":"Editorial","view":"light","repoRoot":"$repo"}""",
        )
      assertEquals(200, chkStatus)

      val (boardStatus, boardBody) =
        httpRequest(
          rec.port,
          "POST",
          "/api/board/render?view=${rec.viewToken}&repo=$repoId",
          headers = mapOf("Origin" to "http://127.0.0.1:${rec.port}"),
          body = """{"repo":"$repoId","direction":"Editorial","screen":"home","view":"dark"}""",
        )
      assertEquals(200, boardStatus, boardBody)
      assertTrue(boardBody.contains("image/png"), boardBody)

      val (focusStatus, focusBody) =
        httpRequest(
          rec.port,
          "POST",
          "/api/board/focus?view=${rec.viewToken}&repo=$repoId",
          headers = mapOf("Origin" to "http://127.0.0.1:${rec.port}"),
          body = """{"repo":"$repoId","direction":"Editorial","screen":"home","view":"dark"}""",
        )
      assertEquals(200, focusStatus, focusBody)

      val (liveStatus, liveBody) =
        httpRequest(
          rec.port,
          "POST",
          "/api/live",
          headers = mapOf("X-Client-Secret" to rec.clientSecret),
          body =
            """{"path":"$source","direction":"Editorial","screen":"home","repoRoot":"$repo"}""",
        )
      assertEquals(200, liveStatus, liveBody)
      assertTrue(updateAttempted.await(2, TimeUnit.SECONDS))
    } finally {
      server.stop()
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `shared resolveMetaTargets and sanitizeSegment handle single, multi, and filtered targets`() {
    val single =
      listOf(DirectionMeta("Main Flow!", "phone", "home", listOf("home", "detail"), emptyList()))
    assertEquals(
      listOf("home", "detail"),
      resolveMetaTargets(single, screenFilter = null).map { it.prefix },
    )
    val multi =
      listOf(
        DirectionMeta("Main Flow!", "phone", "home", listOf("home"), emptyList()),
        DirectionMeta("   ", "phone", "home", listOf("home"), emptyList()),
      )
    assertEquals("Main_Flow_", sanitizeSegment("Main Flow!"))
    assertEquals("direction", sanitizeSegment("   "))
    assertEquals(
      listOf("Main_Flow___home", "direction__home"),
      resolveMetaTargets(multi, screenFilter = null).map { it.prefix },
    )
    assertEquals(
      listOf("Main_Flow___home"),
      resolveMetaTargets(multi, screenFilter = listOf("Main Flow!/home")).map { it.prefix },
    )
    assertEquals(
      listOf("Main_Flow___home"),
      resolveMetaTargets(multi, screenFilter = null, directionFilter = setOf("Main Flow!")).map {
        it.prefix
      },
    )
  }

  @Test
  fun `idle timeout stops server even with an open SSE client and broadcasts stopped event`() {
    val tmp = createTempDirectory("v6-idle-sse-")
    val cache = tmp.resolve("cache")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val exited = CountDownLatch(1)
    val server =
      PreviewHttpServer(
        repoRoot = repo,
        cacheRoot = cache,
        preferredPort = 0,
        idleTimeoutMs = 300L,
        worker = fakeWorker(),
        onProcessExit = { exited.countDown() },
        enableFileWatcher = false,
      )
    val sseExec = Executors.newSingleThreadExecutor()
    try {
      val rec = server.start()
      val sseEvents = CopyOnWriteArrayList<String>()
      val sseConnected = CountDownLatch(1)
      val sseConn =
        URI("http://127.0.0.1:${rec.port}/api/events?view=${rec.viewToken}")
          .toURL()
          .openConnection() as HttpURLConnection
      sseConn.connectTimeout = 2_000
      sseConn.readTimeout = 3_000
      sseExec.submit {
        try {
          sseConn.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
            var line = reader.readLine()
            while (line != null) {
              if (line.startsWith(": connected")) sseConnected.countDown()
              sseEvents += line
              line = reader.readLine()
            }
          }
        } catch (_: Exception) {
          // Stream closed on server shutdown.
        }
      }
      assertTrue(sseConnected.await(2, TimeUnit.SECONDS), "SSE client must connect")
      assertTrue(
        exited.await(3, TimeUnit.SECONDS),
        "idle timeout must stop server even while an SSE tab is open",
      )
      val joined = sseEvents.joinToString("\n")
      assertTrue("event: stopped" in joined, "expected stopped SSE event before shutdown: $joined")
      assertTrue("idle_timeout" in joined, "expected idle_timeout reason in SSE event: $joined")
      sseConn.disconnect()
    } finally {
      sseExec.shutdownNow()
      server.stop()
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `idle timeout does not stop server while a check is in flight`() {
    val tmp = createTempDirectory("v6-idle-inflight-")
    val cache = tmp.resolve("cache")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val source = repo.resolve("board.explore.kt").also { Files.writeString(it, simpleBoard()) }
    val compileStarted = CountDownLatch(1)
    val releaseCompile = CountDownLatch(1)
    val exited = CountDownLatch(1)
    val server =
      PreviewHttpServer(
        repoRoot = repo,
        cacheRoot = cache,
        preferredPort = 0,
        idleTimeoutMs = 250L,
        worker =
          fakeWorker(
            onCompile = {
              compileStarted.countDown()
              releaseCompile.await(5, TimeUnit.SECONDS)
            },
          ),
        onProcessExit = { exited.countDown() },
        enableFileWatcher = false,
      )
    val clientExec = Executors.newSingleThreadExecutor()
    try {
      val rec = server.start()
      val checkFuture =
        clientExec.submit<Pair<Int, String>> {
          httpRequest(
            rec.port,
            "POST",
            "/api/check",
            headers = mapOf("X-Client-Secret" to rec.clientSecret),
            body =
              """{"path":"$source","direction":"Editorial","view":"light","repoRoot":"$repo"}""",
          )
        }
      assertTrue(compileStarted.await(2, TimeUnit.SECONDS), "check should be in flight")
      // Wait longer than idleTimeoutMs while check is still running.
      assertFalse(
        exited.await(600, TimeUnit.MILLISECONDS),
        "idle timeout must not fire while a check is in flight",
      )
      releaseCompile.countDown()
      val (status, body) = checkFuture.get(5, TimeUnit.SECONDS)
      assertEquals(200, status, body)
      assertTrue(
        exited.await(3, TimeUnit.SECONDS),
        "idle timeout must fire once the check completes and server stays idle",
      )
    } finally {
      releaseCompile.countDown()
      clientExec.shutdownNow()
      server.stop()
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `ServerCommand watchdogHaltAction releases managed emulator before halting`() {
    val events = CopyOnWriteArrayList<String>()
    val action =
      ServerCommand.watchdogHaltAction(
        releaseResources = { events += "release" },
        haltProcess = { events += "halt" },
      )
    action()
    assertEquals(listOf("release", "halt"), events)
  }
}
