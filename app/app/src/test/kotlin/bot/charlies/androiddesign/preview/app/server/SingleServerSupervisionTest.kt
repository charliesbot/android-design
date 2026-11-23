package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.app.cli.ExploreRenderRunner
import bot.charlies.androiddesign.preview.app.cli.RenderEntry
import bot.charlies.androiddesign.preview.app.client.PreviewClient
import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.feature.preview.sandbox.CompileResult
import bot.charlies.androiddesign.preview.kit.BoardSurface
import bot.charlies.androiddesign.preview.kit.designBoard
import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PhoneSurface
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.ViewSpec
import com.sun.net.httpserver.HttpServer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.InetSocketAddress
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

  private fun fakeBoardMeta(): BoardMeta =
    BoardMeta(
      directions = listOf(DirectionMeta("Dir", "phone", "home", listOf("home"), emptyList())),
      views = PhoneSurface.views,
    )

  private fun fakeLoaded(sourcePath: Path): ExploreRenderRunner.LoadedExploration {
    val board = designBoard(name = "Board") { direction("Dir") { screen("home") {} } }
    val compiled =
      CompileResult.Compiled(
        board = board,
        sourceFile = sourcePath.fileName.toString(),
        sourceText = Files.readString(sourcePath),
        compileMs = 1L,
        explorationDir = sourcePath.parent,
      )
    return ExploreRenderRunner.LoadedExploration(
      sourcePath = sourcePath,
      designPath = null,
      designTheme = null,
      designSystem =
        bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver.resolve(
          null,
          null,
          PhoneSurface,
        ),
      compiled = compiled,
      compileMs = 1L,
      errors = emptyList(),
    )
  }

  private fun fakeArtifact(
    dir: String,
    screen: String,
    view: ViewSpec,
    rendersRoot: Path,
  ): RenderArtifact {
    val artName = "$dir-$screen"
    val pngPath = rendersRoot.resolve("$artName-${view.name}.png")
    Files.createDirectories(pngPath.parent)
    val bytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    Files.write(pngPath, bytes)
    val check = ViewCheck(pass = true)
    val tree =
      LayoutTree(
        screen = screen,
        view = view,
        nodes = listOf(LayoutNode("n1", "Text", bounds = Bounds(0f, 0f, 50f, 20f))),
        findings = emptyList(),
      )
    return RenderArtifact(
      direction = dir,
      screen = screen,
      overlay = null,
      view = view,
      densityScale = 2,
      artifactName = artName,
      pass = true,
      renderMs = 5L,
      check = check,
      layout = null,
      tree = tree,
      pngPath = pngPath,
      endPngPath = null,
      pngBytes = bytes,
      endPngBytes = null,
      pngSha256 = ContentHasher.sha256Hex(bytes),
      entry = RenderEntry.of(artName, view.name, "$artName-${view.name}", check),
    )
  }

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

    val backend =
      object : PreviewBackend {
        override val name = "fake"
        override val supportsBatchCheck = true
        override val supportsHeadlessSpeculativePrecompile = true

        override fun compile(snapshot: ContentHasher.Snapshot): BackendCompileOutput {
          val loaded = fakeLoaded(snapshot.sourcePath)
          return BackendCompileOutput(
            loaded,
            ContentHasher.sha256Hex(snapshot.sourcePath.toString().toByteArray()),
            BoardSurface.Phone,
            "Board",
            fakeBoardMeta(),
            null,
          )
        }

        override fun renderBatchCheck(
          entry: CompileEntry,
          targets: List<CheckTargetSpec>,
          scoped: Boolean,
          rendersRoot: Path,
        ): List<RenderArtifact> {
          val cur = concurrentRenders.incrementAndGet()
          maxConcurrent.updateAndGet { maxOf(it, cur) }
          try {
            Thread.sleep(25)
            return targets.map { fakeArtifact(it.direction, it.screenId, it.view, rendersRoot) }
          } finally {
            concurrentRenders.decrementAndGet()
          }
        }

        override fun renderSingle(
          entry: CompileEntry,
          direction: String,
          screenId: String,
          prefix: String,
          overlayId: String?,
          view: ViewSpec,
          densityScale: Int,
          designSystem: bot.charlies.androiddesign.preview.model.DesignSystem,
          baseCheck: ViewCheck?,
          writeFiles: Boolean,
          rendersRoot: Path,
          frameFormat: FrameFormat,
          fontScale: Float,
          seedOverride: String?,
          variantOverride: String?,
          darkOverride: Boolean?,
        ): RenderArtifact = fakeArtifact(direction, screenId, view, rendersRoot)

        override fun updateLive(
          entry: CompileEntry,
          direction: String?,
          screenId: String?,
          overlayId: String?,
          darkOverride: Boolean?,
          seedOverride: String?,
          variantOverride: String?,
          fontScale: Float,
        ) {
          liveOwners += entry.sourcePath.toAbsolutePath().normalize().toString()
        }
      }

    val server =
      PreviewHttpServer(
        repoRoot = repoA,
        cacheRoot = cache,
        preferredPort = 0,
        backend = backend,
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
    val backend =
      object : PreviewBackend {
        override val name = "fake-watch"
        override val supportsBatchCheck = true
        override val supportsHeadlessSpeculativePrecompile = true

        override fun compile(snapshot: ContentHasher.Snapshot): BackendCompileOutput {
          compileCount.incrementAndGet()
          val loaded = fakeLoaded(snapshot.sourcePath)
          compiledLatches.get().countDown()
          return BackendCompileOutput(
            loaded,
            "dex-${snapshot.contentHash}",
            BoardSurface.Phone,
            "Board",
            fakeBoardMeta(),
            null,
          )
        }

        override fun renderBatchCheck(
          entry: CompileEntry,
          targets: List<CheckTargetSpec>,
          scoped: Boolean,
          rendersRoot: Path,
        ): List<RenderArtifact> = targets.map {
          fakeArtifact(it.direction, it.screenId, it.view, rendersRoot)
        }

        override fun renderSingle(
          entry: CompileEntry,
          direction: String,
          screenId: String,
          prefix: String,
          overlayId: String?,
          view: ViewSpec,
          densityScale: Int,
          designSystem: bot.charlies.androiddesign.preview.model.DesignSystem,
          baseCheck: ViewCheck?,
          writeFiles: Boolean,
          rendersRoot: Path,
          frameFormat: FrameFormat,
          fontScale: Float,
          seedOverride: String?,
          variantOverride: String?,
          darkOverride: Boolean?,
        ): RenderArtifact = fakeArtifact(direction, screenId, view, rendersRoot)
      }

    val server =
      PreviewHttpServer(
        repoRoot = repo,
        cacheRoot = cache,
        preferredPort = 0,
        backend = backend,
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
    val backend =
      object : PreviewBackend {
        override val name = "slow"
        override val supportsBatchCheck = true
        override val supportsHeadlessSpeculativePrecompile = false

        override fun compile(snapshot: ContentHasher.Snapshot): BackendCompileOutput {
          val loaded = fakeLoaded(snapshot.sourcePath)
          return BackendCompileOutput(
            loaded,
            "dex",
            BoardSurface.Phone,
            "Board",
            fakeBoardMeta(),
            null,
          )
        }

        override fun renderBatchCheck(
          entry: CompileEntry,
          targets: List<CheckTargetSpec>,
          scoped: Boolean,
          rendersRoot: Path,
        ): List<RenderArtifact> {
          renderStarted.countDown()
          Thread.sleep(10_000)
          return targets.map { fakeArtifact(it.direction, it.screenId, it.view, rendersRoot) }
        }

        override fun renderSingle(
          entry: CompileEntry,
          direction: String,
          screenId: String,
          prefix: String,
          overlayId: String?,
          view: ViewSpec,
          densityScale: Int,
          designSystem: bot.charlies.androiddesign.preview.model.DesignSystem,
          baseCheck: ViewCheck?,
          writeFiles: Boolean,
          rendersRoot: Path,
          frameFormat: FrameFormat,
          fontScale: Float,
          seedOverride: String?,
          variantOverride: String?,
          darkOverride: Boolean?,
        ): RenderArtifact = fakeArtifact(direction, screenId, view, rendersRoot)
      }

    val server =
      PreviewHttpServer(
        repoRoot = repo,
        cacheRoot = cache,
        preferredPort = 0,
        backend = backend,
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
  fun `apkSha256 change triggers replacement with --force-reinstall-apk`(@TempDir tmp: Path) {
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
      apkSha256=new-sha-2222
      composeCompilerPluginJar=/tmp/compose.jar
      """
        .trimIndent(),
    )

    // Write active.json pointing to an in-process stub HTTP server advertising old apkSha256 =
    // old-sha-1111. Use a nonexistent PID so stopServer waits for the stub HTTP server to stop.
    val stopRequested = CountDownLatch(1)
    val stubHttp = HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0)
    val secret = "sec-123"
    stubHttp.createContext("/api/status") { ex ->
      val body =
        PreviewJson.compact.encodeToString(
          StatusResponse.serializer(),
          StatusResponse(
            pid = 999_999_999L,
            port = stubHttp.address.port,
            jarVersion = AppInfo.VERSION,
            startedAt = System.currentTimeMillis(),
            compileCount = 0,
            repoRoot = repo.toString(),
            boardUrl = "http://127.0.0.1:${stubHttp.address.port}",
            backend = RuntimeIdentity.BACKEND_ANDROID,
            runtimeHash = "old-hash",
          ),
        )
      val bytes = body.toByteArray(StandardCharsets.UTF_8)
      ex.sendResponseHeaders(200, bytes.size.toLong())
      ex.responseBody.use { it.write(bytes) }
    }
    stubHttp.createContext("/api/stop") { ex ->
      val bytes = """{"ok":true}""".toByteArray(StandardCharsets.UTF_8)
      ex.sendResponseHeaders(200, bytes.size.toLong())
      ex.responseBody.use { it.write(bytes) }
      stopRequested.countDown()
      Thread { stubHttp.stop(0) }.start()
    }
    stubHttp.start()

    val recordPath = ServerPaths.sharedRecordPath(cache)
    ServerPaths.writeRecord(
      recordPath,
      ServerRecord(
        pid = 999_999_999L,
        port = stubHttp.address.port,
        viewToken = "view-tok",
        clientSecret = secret,
        jarVersion = AppInfo.VERSION,
        startedAt = System.currentTimeMillis(),
        repoRoot = repo.toString(),
        backend = RuntimeIdentity.BACKEND_ANDROID,
        runtimeHash = "old-hash",
        apkSha256 = "old-sha-1111",
      ),
    )

    val argsCapture = tmp.resolve("spawn_args.txt")
    val fakeJdk = tmp.resolve("fake-jdk")
    val fakeJava = fakeJdk.resolve("bin/java")
    Files.createDirectories(fakeJava.parent)
    Files.writeString(
      fakeJava,
      """#!/bin/sh
printf '%s\n' "${'$'}@" > "$argsCapture"
exit 1
""",
    )
    fakeJava.toFile().setExecutable(true)

    val prevJavaHome = System.getProperty("java.home")
    try {
      System.setProperty("java.home", fakeJdk.toString())
      val client =
        PreviewClient(
          cacheRoot = cache,
          env =
            mapOf(
              RuntimeIdentity.BACKEND_ENV to RuntimeIdentity.BACKEND_ANDROID,
              BootstrapMeta.BOOTSTRAP_DIR_ENV to bdir.toString(),
              RuntimeIdentity.SERVER_CP_ENV to bdir.resolve("server.cp").toString(),
            ),
        )
      client.ensureServer(repo)
      assertTrue(stopRequested.await(2, TimeUnit.SECONDS), "Old server should receive /api/stop")
      val spawnedArgs = Files.readAllLines(argsCapture)
      assertTrue(
        "--force-reinstall-apk" in spawnedArgs,
        "Expected --force-reinstall-apk in $spawnedArgs",
      )
      val flagIdx = spawnedArgs.indexOf("--force-reinstall-apk")
      assertEquals("true", spawnedArgs[flagIdx + 1])
    } finally {
      System.setProperty("java.home", prevJavaHome)
      stubHttp.stop(0)
    }
  }

  @Test
  fun `requestedSerial mismatch replaces shared server and passes --device-serial`(
    @TempDir tmp: Path,
  ) {
    val cache = tmp.resolve("cache")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val bdir = tmp.resolve("bootstrap").also { Files.createDirectories(it) }
    val apk = bdir.resolve("host.apk").also { Files.writeString(it, "apk") }
    val androidJar = bdir.resolve("android.jar").also { Files.writeString(it, "aj") }
    val d8Jar = bdir.resolve("d8.jar").also { Files.writeString(it, "d8") }
    val bundleJar = bdir.resolve("bundle.jar").also { Files.writeString(it, "bj") }
    val composeJar = bdir.resolve("compose.jar").also { Files.writeString(it, "cj") }
    Files.writeString(bdir.resolve("server.cp"), "-cp\nfoo.jar\n")
    val sha = BootstrapMeta.computeSha256(apk)
    Files.writeString(
      bdir.resolve("bootstrap-meta.properties"),
      """
      androidJar=$androidJar
      d8Jar=$d8Jar
      bundleJar=$bundleJar
      apkPath=$apk
      apkSha256=$sha
      composeCompilerPluginJar=$composeJar
      """
        .trimIndent(),
    )

    val env =
      mapOf(
        RuntimeIdentity.BACKEND_ENV to RuntimeIdentity.BACKEND_ANDROID,
        BootstrapMeta.BOOTSTRAP_DIR_ENV to bdir.toString(),
        RuntimeIdentity.SERVER_CP_ENV to bdir.resolve("server.cp").toString(),
      )
    val id =
      RuntimeIdentity.compute(
        env,
        meta = BootstrapMeta.fromFile(bdir.resolve("bootstrap-meta.properties")),
      )

    val stopRequested = CountDownLatch(1)
    val stubHttp = HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0)
    stubHttp.createContext("/api/status") { ex ->
      val body =
        PreviewJson.compact.encodeToString(
          StatusResponse.serializer(),
          StatusResponse(
            pid = 999_999_999L,
            port = stubHttp.address.port,
            jarVersion = AppInfo.VERSION,
            startedAt = System.currentTimeMillis(),
            compileCount = 0,
            repoRoot = repo.toString(),
            boardUrl = "http://127.0.0.1:${stubHttp.address.port}",
            backend = id.backend,
            runtimeHash = id.runtimeHash,
            deviceSerial = "emulator-5554",
          ),
        )
      val bytes = body.toByteArray(StandardCharsets.UTF_8)
      ex.sendResponseHeaders(200, bytes.size.toLong())
      ex.responseBody.use { it.write(bytes) }
    }
    stubHttp.createContext("/api/stop") { ex ->
      val bytes = """{"ok":true}""".toByteArray(StandardCharsets.UTF_8)
      ex.sendResponseHeaders(200, bytes.size.toLong())
      ex.responseBody.use { it.write(bytes) }
      stopRequested.countDown()
      Thread { stubHttp.stop(0) }.start()
    }
    stubHttp.start()

    ServerPaths.writeRecord(
      ServerPaths.sharedRecordPath(cache),
      ServerRecord(
        pid = 999_999_999L,
        port = stubHttp.address.port,
        viewToken = "vt",
        clientSecret = "sec",
        jarVersion = AppInfo.VERSION,
        startedAt = System.currentTimeMillis(),
        repoRoot = repo.toString(),
        backend = id.backend,
        runtimeHash = id.runtimeHash,
        deviceSerial = "emulator-5554",
        apkSha256 = sha,
      ),
    )

    val argsCapture = tmp.resolve("spawn_args.txt")
    val fakeJdk = tmp.resolve("fake-jdk")
    val fakeJava = fakeJdk.resolve("bin/java")
    Files.createDirectories(fakeJava.parent)
    Files.writeString(
      fakeJava,
      """#!/bin/sh
printf '%s\n' "${'$'}@" > "$argsCapture"
exit 1
""",
    )
    fakeJava.toFile().setExecutable(true)

    val prevJavaHome = System.getProperty("java.home")
    try {
      System.setProperty("java.home", fakeJdk.toString())
      val client = PreviewClient(cacheRoot = cache, env = env)
      client.ensureServer(repo, requestedSerial = "emulator-5556")
      assertTrue(stopRequested.await(2, TimeUnit.SECONDS), "Mismatched serial must stop old server")
      val spawnedArgs = Files.readAllLines(argsCapture)
      val serialIdx = spawnedArgs.indexOf("--device-serial")
      assertTrue(serialIdx >= 0, "Expected --device-serial in $spawnedArgs")
      assertEquals("emulator-5556", spawnedArgs[serialIdx + 1])
    } finally {
      System.setProperty("java.home", prevJavaHome)
      stubHttp.stop(0)
    }
  }
}
