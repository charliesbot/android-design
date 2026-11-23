package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.DeviceBoardMeta
import bot.charlies.androiddesign.preview.model.DeviceDirectionMeta
import bot.charlies.androiddesign.preview.model.DeviceOverlayMeta
import bot.charlies.androiddesign.preview.model.Finding
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.PhoneSurface
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.RobolectricRenderRequest
import bot.charlies.androiddesign.preview.model.RobolectricRenderResponse
import bot.charlies.androiddesign.preview.model.RobolectricTargetResult
import bot.charlies.androiddesign.preview.model.SchemeOverride
import bot.charlies.androiddesign.preview.model.Severity
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.Views
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class RobolectricPreviewBackendTest {

  private fun sampleExploreSource(title: String = "Explore"): String =
    """
    import androidx.compose.material3.Text
    import bot.charlies.androiddesign.preview.kit.designBoard

    val preview = designBoard(name = "$title") {
      direction("Editorial") {
        screen("home") { Text("Hello") }
        overlay("sheet", screen = "home") { Text("Sheet") }
      }
    }
    """
      .trimIndent()

  private fun writeSampleFile(
    dir: Path,
    name: String = "Demo.explore.kt",
    title: String = "Explore",
  ): Path {
    Files.createDirectories(dir)
    val file = dir.resolve(name)
    Files.writeString(file, sampleExploreSource(title))
    return file
  }

  private fun sampleBoardMeta(): DeviceBoardMeta =
    DeviceBoardMeta(
      surface = "phone",
      boardName = "Explore",
      directions =
        listOf(
          DeviceDirectionMeta(
            name = "Editorial",
            surface = "phone",
            startScreen = "home",
            screens = listOf("home"),
            overlays = listOf(DeviceOverlayMeta("sheet", "home")),
          ),
        ),
      views = PhoneSurface.views,
    )

  private fun stubPngBytes(tag: String): ByteArray {
    // Minimal distinct byte payload with PNG header so SHA-256 differs when tag differs.
    val header = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    return header + tag.toByteArray(StandardCharsets.UTF_8)
  }

  private fun postJson(
    server: PreviewHttpServer,
    record: ServerRecord,
    path: String,
    body: String,
  ): Pair<Int, String> {
    val conn =
      (URI("${server.origin()}$path").toURL().openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        setRequestProperty("Host", "127.0.0.1:${record.port}")
        setRequestProperty("X-Client-Secret", record.clientSecret)
        setRequestProperty("Content-Type", "application/json; charset=utf-8")
        doOutput = true
        outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) }
      }
    val status = conn.responseCode
    val stream =
      if (status in 200..399) conn.inputStream else (conn.errorStream ?: conn.inputStream)
    val text = stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
    conn.disconnect()
    return status to text
  }

  @Test
  fun runtimeIdentitySupportsRobolectricAndHashesClasspathWithoutApk(@TempDir tmp: Path) {
    val bdir = tmp.resolve("bootstrap").also { Files.createDirectories(it) }
    val dep = tmp.resolve("robo-dep.jar").also { Files.writeString(it, "dep") }
    Files.writeString(bdir.resolve("server.cp"), "-cp\n$dep\n")
    Files.writeString(bdir.resolve("robolectric.cp"), "-cp\n$dep\n")

    val env =
      mapOf(
        RuntimeIdentity.BACKEND_ENV to RuntimeIdentity.BACKEND_ROBOLECTRIC,
        BootstrapMeta.BOOTSTRAP_DIR_ENV to bdir.toString(),
        RuntimeIdentity.SERVER_CP_ENV to bdir.resolve("server.cp").toString(),
        RuntimeIdentity.ROBOLECTRIC_CP_ENV to bdir.resolve("robolectric.cp").toString(),
      )
    assertEquals(RuntimeIdentity.BACKEND_ROBOLECTRIC, RuntimeIdentity.resolveBackend(env))
    val id1 = RuntimeIdentity.compute(env = env, jar = null, meta = null)
    assertEquals(RuntimeIdentity.BACKEND_ROBOLECTRIC, id1.backend)
    assertNull(id1.apkSha256)
    assertEquals(bdir.resolve("robolectric.cp"), id1.robolectricCpFile)

    Files.writeString(bdir.resolve("robolectric.cp"), "-cp\n$dep:$dep\n")
    val id2 = RuntimeIdentity.compute(env = env, jar = null, meta = null)
    assertFalse(
      id1.runtimeHash == id2.runtimeHash,
      "Changing robolectric.cp must change runtimeHash",
    )
  }

  @Test
  fun robolectricBackendHandlesScoped1xUnscoped2xFailDedupOverlayDiffTreeAndMultiRepo(
    @TempDir tmp: Path,
  ) {
    val cacheRoot = tmp.resolve("cache")
    val repoA = tmp.resolve("repo-a").also { Files.createDirectories(it.resolve(".git")) }
    val repoB = tmp.resolve("repo-b").also { Files.createDirectories(it.resolve(".git")) }
    val fileA = writeSampleFile(repoA.resolve("design"), title = "Alpha")
    val fileB = writeSampleFile(repoB.resolve("design"), title = "Beta")

    val requests = mutableListOf<RobolectricRenderRequest>()
    var failFoldable = false

    val backend =
      RobolectricPreviewBackend(
        requestExecutor = { req ->
          requests += req
          when (req.mode) {
            "ping" -> RobolectricRenderResponse(id = req.id, ok = true, valid = true, pass = true)
            "compile" ->
              RobolectricRenderResponse(
                id = req.id,
                ok = true,
                valid = true,
                pass = true,
                compileMs = 12L,
                surface = "phone",
                boardName = "Explore",
                boardMeta = sampleBoardMeta(),
                boardTheme =
                  ThemeOverride(
                    name = "Explore",
                    colors = mapOf("primary" to "#B5523B"),
                    material =
                      bot.charlies.androiddesign.preview.model.MaterialOverride(
                        scheme =
                          SchemeOverride(seed = "#B5523B", variant = PaletteVariant.TonalSpot.wire),
                      ),
                  ),
              )
            else -> {
              val outDir = req.outDir?.let { Path.of(it) }
              val targetResults =
                req.targets.orEmpty().mapIndexed { idx, t ->
                  val viewSpec = Views.byName(t.viewName)!!
                  val isBaseFail = t.overlayId == null && t.viewName == "light" && failFoldable
                  val isFoldFail = failFoldable && t.viewName == "foldable"
                  // Repeat the base light touch-target finding inside overlay so overlay diffing
                  // strips inherited findings and passes the overlay cleanly.
                  val findings = buildList {
                    if (
                      isFoldFail ||
                        (failFoldable && t.viewName == "light" && t.overlayId == "sheet")
                    ) {
                      add(
                        Finding(
                          rule = FindingRule.TouchTarget,
                          severity = Severity.Error,
                          id = "btn@1",
                          detail = "36x36 dp < 48x48 dp",
                        ),
                      )
                    } else if (isBaseFail) {
                      add(
                        Finding(
                          rule = FindingRule.TouchTarget,
                          severity = Severity.Error,
                          id = "btn@1",
                          detail = "36x36 dp < 48x48 dp",
                        ),
                      )
                    }
                  }
                  val screenLabel =
                    if (t.overlayId != null) ViewCheck.renderOverlayName(t.screen, t.overlayId!!)
                    else t.screen
                  val tree =
                    LayoutTree(
                      screen = screenLabel,
                      view = viewSpec,
                      nodes = listOf(LayoutNode("n1", "Text", bounds = Bounds(0f, 0f, 40f, 20f))),
                      findings = findings,
                    )
                  var pngPath: String? = null
                  if (t.frameFormat != FrameFormat.NONE && outDir != null) {
                    val file = outDir.resolve("s_${idx}_${t.viewName}.png")
                    Files.write(
                      file,
                      stubPngBytes("${t.viewName}-${t.overlayId ?: "base"}-${t.densityScale}"),
                    )
                    pngPath = file.toString()
                  }
                  RobolectricTargetResult(
                    direction = t.direction,
                    screen = t.screen,
                    viewName = t.viewName,
                    overlayId = t.overlayId,
                    densityScale = t.densityScale,
                    renderMs = 8L,
                    tree = tree,
                    pngPath = pngPath,
                  )
                }
              RobolectricRenderResponse(
                id = req.id,
                ok = true,
                valid = true,
                pass = true,
                boardMeta = sampleBoardMeta(),
                results = targetResults,
              )
            }
          }
        },
      )

    backend.warmUp()
    assertEquals("ping", requests.first().mode)

    val identity =
      RuntimeIdentity(
        backend = RuntimeIdentity.BACKEND_ROBOLECTRIC,
        runtimeHash = "test-robo-hash",
        sourceDir = null,
        apkSha256 = null,
        jarPath = null,
        jarModified = null,
        serverCpFile = null,
        bootstrapDir = null,
      )
    val server =
      PreviewHttpServer(
        repoRoot = repoA,
        cacheRoot = cacheRoot,
        preferredPort = 0,
        idleTimeoutMs = 0L,
        backend = backend,
        runtimeIdentity = identity,
        enableFileWatcher = false,
      )
    val record = server.start()
    try {
      // 1. Scoped check runs at 1x and writes 1x PNG.
      val (scStatus, scBody) =
        postJson(
          server,
          record,
          "/api/check",
          PreviewJson.compact.encodeToString(
            CheckRequest.serializer(),
            CheckRequest(
              path = fileA.toString(),
              direction = "Editorial",
              view = "light",
              repoRoot = repoA.toString(),
            ),
          ),
        )
      assertEquals(200, scStatus)
      val scResp = PreviewJson.compact.decodeFromString(CheckResponse.serializer(), scBody)
      assertTrue(scResp.ok && scResp.valid && scResp.pass && scResp.scoped)
      assertEquals(1, scResp.images.size)
      assertEquals(1, scResp.images.single().density)
      assertTrue(scResp.images.single().path.endsWith("-1x.png"))
      assertTrue(Files.isRegularFile(Path.of(scResp.images.single().path)))

      // 2. Unscoped passing check runs all 10 targets (5 base + 5 overlay) at 2x and writes light +
      // desktop 2x PNGs.
      val (fullStatus, fullBody) =
        postJson(
          server,
          record,
          "/api/check",
          PreviewJson.compact.encodeToString(
            CheckRequest.serializer(),
            CheckRequest(path = fileA.toString(), repoRoot = repoA.toString()),
          ),
        )
      assertEquals(200, fullStatus)
      val fullResp = PreviewJson.compact.decodeFromString(CheckResponse.serializer(), fullBody)
      assertTrue(fullResp.ok && fullResp.valid && fullResp.pass && !fullResp.scoped)
      assertEquals(10, fullResp.renders.size)
      assertEquals(listOf("light", "desktop"), fullResp.images.map { it.view })
      assertTrue(fullResp.images.all { it.density == 2 && Files.isRegularFile(Path.of(it.path)) })

      // 3. Tree request uses FrameFormat.NONE (no temp PNG writes needed).
      val prevReqCount = requests.size
      val (treeStatus, treeBody) =
        postJson(
          server,
          record,
          "/api/tree",
          PreviewJson.compact.encodeToString(
            TreeRequest.serializer(),
            TreeRequest(
              path = fileA.toString(),
              direction = "Editorial",
              view = "tablet",
              repoRoot = repoA.toString(),
            ),
          ),
        )
      assertEquals(200, treeStatus)
      val treeResp = PreviewJson.compact.decodeFromString(TreeResponse.serializer(), treeBody)
      assertTrue(treeResp.ok && treeResp.tree?.nodes?.isNotEmpty() == true)
      // Unscoped check already populated 2x cache for tablet, so /api/tree hit cache with 0 extra
      // worker calls!
      assertEquals(prevReqCount, requests.size)

      // 4. Failing foldable check in repoB fetches follow-up frame for non-primary failing view and
      // isolates repos.
      failFoldable = true
      val (bStatus, bBody) =
        postJson(
          server,
          record,
          "/api/check",
          PreviewJson.compact.encodeToString(
            CheckRequest.serializer(),
            CheckRequest(path = fileB.toString(), repoRoot = repoB.toString()),
          ),
        )
      assertEquals(200, bStatus)
      val bResp = PreviewJson.compact.decodeFromString(CheckResponse.serializer(), bBody)
      assertFalse(bResp.pass)
      assertTrue(
        bResp.renders.any { it.overlay == null && it.view == "foldable" && !it.pass },
      )
      assertTrue(
        bResp.renders.first { it.overlay == "sheet" && it.view == "light" }.pass,
        "Overlay diffing should drop inherited base TouchTarget finding on sheet/light",
      )
      assertTrue(bResp.images.isNotEmpty() && bResp.images.size <= 2)
      assertTrue(bResp.images.all { it.path.contains(ServerPaths.repoHash(repoB)) })
      val followUpReq = requests.last()
      assertTrue(
        followUpReq.targets.orEmpty().any {
          it.viewName == "foldable" && it.frameFormat == FrameFormat.LZ4_RGBA
        },
      )
    } finally {
      server.stop(triggerExit = false)
    }
  }

  @Test
  fun liveRobolectricBackendRunsRealWorkerJvmAcrossCompileAndScopedCheck(@TempDir tmp: Path) {
    val meta = BootstrapMeta.locateOrNull()
    val rcp = meta?.bootstrapDir?.resolve("robolectric.cp")
    assumeTrue(meta != null && rcp != null && Files.isRegularFile(rcp))

    val repo = tmp.resolve("live-repo").also { Files.createDirectories(it.resolve(".git")) }
    val file = writeSampleFile(repo.resolve("design"), title = "LiveRobo")
    val rendersRoot = tmp.resolve("renders")
    val backend = RobolectricPreviewBackend(meta = meta, robolectricCp = rcp)
    backend.use { rb ->
      rb.warmUp()
      val snap = ContentHasher.snapshot(file, null)
      val out = rb.compile(snap)
      assertTrue(out.loaded.errors.isEmpty(), "Expected compile errors empty: ${out.loaded.errors}")
      assertEquals("phone", out.boardMeta?.directions?.single()?.surface)

      val entry =
        CompileEntry(
          sourcePath = file,
          designPath = null,
          contentHash = snap.contentHash,
          loaded = out.loaded,
          fromCache = false,
          compileCountAtLoad = 1,
          repoRoot = repo,
          surface = out.surface,
          boardName = out.boardName,
          boardMeta = out.boardMeta,
          boardTheme = out.boardTheme,
        )
      val target =
        CheckTargetSpec(
          direction = "Editorial",
          screenId = "home",
          prefix = "home",
          overlayId = null,
          view = PhoneSurface.views.first { it.name == "light" },
        )
      val arts =
        rb.renderBatchCheck(entry, listOf(target), scoped = true, rendersRoot = rendersRoot)
      assertEquals(1, arts.size)
      val art = arts.single()
      assertTrue(art.pass, "Expected render pass: ${art.check}")
      assertEquals(1, art.densityScale)
      val artPng = art.pngPath
      val artTree = art.tree
      assertTrue(artPng != null && Files.isRegularFile(artPng))
      assertTrue(artTree != null && artTree.nodes.isNotEmpty())
    }
  }

  @Test
  fun closeDuringActiveRequestUnblocksImmediatelyAndKillsHungWorkerProcess(@TempDir tmp: Path) {
    val pidFile = tmp.resolve("worker.pid")
    val startedFile = tmp.resolve("worker.started")
    val script = tmp.resolve("hang-worker.sh")
    Files.writeString(
      script,
      """
      #!/bin/sh
      echo $$ > "${pidFile.toAbsolutePath()}"
      IFS= read -r _line
      touch "${startedFile.toAbsolutePath()}"
      sleep 60
      """
        .trimIndent() + "\n",
    )
    script.toFile().setExecutable(true)

    val backend = RobolectricPreviewBackend(workerCommand = listOf("/bin/sh", script.toString()))
    val requestDone = java.util.concurrent.CompletableFuture<Throwable?>()
    val caller = Thread {
      val err = runCatching { backend.warmUp() }.exceptionOrNull()
      requestDone.complete(err)
    }
    caller.isDaemon = true
    caller.start()

    val deadline = System.currentTimeMillis() + 5_000L
    while (!Files.exists(startedFile) && System.currentTimeMillis() < deadline) {
      Thread.sleep(20)
    }
    assertTrue(Files.exists(startedFile), "Worker did not read warmup request before timeout")
    val pid = Files.readString(pidFile).trim().toLong()

    val closeDone = java.util.concurrent.CompletableFuture<Unit>()
    val closer = Thread {
      backend.close()
      closeDone.complete(Unit)
    }
    closer.isDaemon = true
    closer.start()

    try {
      closeDone.get(3, java.util.concurrent.TimeUnit.SECONDS)
      val reqErr = requestDone.get(3, java.util.concurrent.TimeUnit.SECONDS)
      assertTrue(reqErr != null, "Expected blocked warmup request to fail on close")
      val deadDeadline = System.currentTimeMillis() + 3_000L
      while (
        ProcessHandle.of(pid).map { it.isAlive }.orElse(false) &&
          System.currentTimeMillis() < deadDeadline
      ) {
        Thread.sleep(20)
      }
      assertFalse(
        ProcessHandle.of(pid).map { it.isAlive }.orElse(false),
        "Worker process $pid stayed alive after backend.close()",
      )
    } finally {
      ProcessHandle.of(pid).ifPresent { it.destroyForcibly() }
    }
  }

  @Test
  fun watchdogHaltClosesRobolectricBackendBeforeHaltingServer(@TempDir tmp: Path) {
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val file = writeSampleFile(repo.resolve("design"), title = "WatchdogHung")
    val cacheRoot = tmp.resolve("cache")
    val pidFile = tmp.resolve("wd-worker.pid")
    val startedFile = tmp.resolve("wd-worker.started")
    val script = tmp.resolve("wd-hang.sh")
    Files.writeString(
      script,
      """
      #!/bin/sh
      echo $$ > "${pidFile.toAbsolutePath()}"
      IFS= read -r _line
      touch "${startedFile.toAbsolutePath()}"
      sleep 60
      """
        .trimIndent() + "\n",
    )
    script.toFile().setExecutable(true)

    val backend = RobolectricPreviewBackend(workerCommand = listOf("/bin/sh", script.toString()))
    val halted = java.util.concurrent.CountDownLatch(1)
    var aliveAtHalt: Boolean? = null
    val identity =
      RuntimeIdentity(
        backend = RuntimeIdentity.BACKEND_ROBOLECTRIC,
        runtimeHash = "wd-hash",
        sourceDir = null,
        apkSha256 = null,
        jarPath = null,
        jarModified = null,
        serverCpFile = null,
        bootstrapDir = null,
      )
    val server =
      PreviewHttpServer(
        repoRoot = repo,
        cacheRoot = cacheRoot,
        preferredPort = 0,
        idleTimeoutMs = 0L,
        coldBudgetMs = 250L,
        warmBudgetMs = 250L,
        backend = backend,
        runtimeIdentity = identity,
        onWatchdogHalt = {
          val pid =
            if (Files.exists(pidFile)) Files.readString(pidFile).trim().toLongOrNull() else null
          aliveAtHalt =
            pid?.let { id -> ProcessHandle.of(id).map { it.isAlive }.orElse(false) } ?: false
          halted.countDown()
        },
        enableFileWatcher = false,
      )
    val record = server.start()
    val httpDone = java.util.concurrent.CompletableFuture<Unit>()
    val reqThread = Thread {
      runCatching {
        postJson(
          server,
          record,
          "/api/check",
          PreviewJson.compact.encodeToString(
            CheckRequest.serializer(),
            CheckRequest(path = file.toString(), repoRoot = repo.toString()),
          ),
        )
      }
      httpDone.complete(Unit)
    }
    reqThread.isDaemon = true
    reqThread.start()

    try {
      assertTrue(
        halted.await(5, java.util.concurrent.TimeUnit.SECONDS),
        "Watchdog halt never fired",
      )
      assertEquals(false, aliveAtHalt, "Hung worker process must be dead before onWatchdogHalt")
    } finally {
      server.stop(triggerExit = false)
      runCatching {
        if (Files.exists(pidFile)) {
          Files.readString(pidFile).trim().toLongOrNull()?.let { pid ->
            ProcessHandle.of(pid).ifPresent { it.destroyForcibly() }
          }
        }
      }
      httpDone.get(3, java.util.concurrent.TimeUnit.SECONDS)
    }
  }
}
