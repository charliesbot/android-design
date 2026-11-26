package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.client.CheckCommand
import bot.charlies.androiddesign.preview.app.client.ClientExitCode
import bot.charlies.androiddesign.preview.app.client.PreviewClient
import bot.charlies.androiddesign.preview.app.client.StartCommand
import bot.charlies.androiddesign.preview.app.client.StatusCommand
import bot.charlies.androiddesign.preview.app.client.TreeCommand
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.model.ClassBundleCodec
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.Views
import bot.charlies.androiddesign.preview.model.WorkerRenderTarget
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.io.TempDir

class LayoutlibRenderWorkerTest {
  private val repoRoot: Path =
    Path.of(
      System.getProperty("adp.repo.root") ?: error("adp.repo.root system property missing"),
    )

  private val meta: BootstrapMeta by lazy { BootstrapMeta.locate() }

  private val multiturnFixture: Path
    get() = repoRoot.resolve("evals/multiturn_fixture/design/proposal.explore.kt")

  private fun fakeCommand(mode: String, markerDir: Path? = null): List<String> {
    val javaBin = Path.of(System.getProperty("java.home"), "bin", "java").toString()
    val cp = System.getProperty("java.class.path")
    return buildList {
      add(javaBin)
      add("-cp")
      add(cp)
      add(FakeLayoutlibWorkerMain::class.java.name)
      add(mode)
      if (markerDir != null) add(markerDir.toString())
    }
  }

  private fun fakeCompiledEntry(sourceFile: Path): CompileEntry {
    val emptyBundle = ClassBundleCodec.encode(emptyMap())
    val hash = ContentHasher.sha256Hex(emptyBundle)
    return CompileEntry(
      sourcePath = sourceFile,
      designPath = null,
      contentHash = hash,
      loaded =
        LoadedExploration(
          sourcePath = sourceFile,
          designPath = null,
          designTheme = null,
          designSystem =
            bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver.resolve(
              null,
              null,
            ),
          compileMs = 1L,
          errors = emptyList(),
        ),
      fromCache = false,
      compileCountAtLoad = 1,
      compiledData =
        CompiledBoardData(
          sourceFile = sourceFile.fileName.toString(),
          sourceText = "",
          entryClass = "FakeKt",
          classBytes = emptyMap(),
          classBundleBytes = emptyBundle,
          classHash = hash,
          explorationDir = sourceFile.parent,
        ),
    )
  }

  @Test
  fun `prepare spawns worker and responds to ping`() {
    LayoutlibRenderWorker(meta).use { worker ->
      worker.prepare()
      val pid = worker.currentWorkerPid()
      assertNotNull(pid, "Expected active worker pid after prepare()")
      assertTrue(ProcessHandle.of(pid).map { it.isAlive }.orElse(false))
    }
  }

  @Test
  fun `compile succeeds without D8 or device and render produces trees and PNGs`(
    @TempDir tmp: Path,
  ) {
    val boardCopy = tmp.resolve("design/proposal.explore.kt")
    Files.createDirectories(boardCopy.parent)
    Files.copy(multiturnFixture, boardCopy)

    LayoutlibRenderWorker(meta).use { worker ->
      worker.prepare()
      val compiled = worker.compile(ContentHasher.snapshot(boardCopy, null))
      assertTrue(
        compiled is WorkerCompileResult.Ready,
        "Expected Ready, got $compiled",
      )
      val boardData = assertNotNull(compiled.compiledData)
      assertTrue(boardData.classHash.isNotBlank())
      assertEquals(5, compiled.boardMeta.views.size)
      assertEquals(2, compiled.boardMeta.directions.size)

      val entry =
        CompileEntry(
          sourcePath = compiled.sourcePath,
          designPath = compiled.designPath,
          contentHash = boardData.classHash,
          loaded =
            LoadedExploration(
              sourcePath = compiled.sourcePath,
              designPath = compiled.designPath,
              designTheme = compiled.designTheme,
              designSystem =
                bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver.resolve(
                  compiled.designTheme,
                  compiled.boardTheme,
                ),
              compileMs = compiled.compileMs,
              errors = emptyList(),
            ),
          fromCache = false,
          compileCountAtLoad = 1,
          boardName = compiled.boardMeta.boardName,
          boardMeta = compiled.boardMeta,
          compiledData = boardData,
          boardTheme = compiled.boardTheme,
        )

      val dirName = compiled.boardMeta.directions.first().name
      val screenName = compiled.boardMeta.directions.first().startScreen
      val targets =
        listOf(
          WorkerRenderTarget(dirName, screenName, Views.Light.name, includeFrame = true),
          WorkerRenderTarget(dirName, screenName, Views.Dark.name, includeFrame = false),
        )
      val batch = worker.renderTargets(entry, targets, tmp)
      assertTrue(batch.ok, batch.error)
      assertEquals(2, batch.targets.size)

      val light = batch.targets[0]
      val lightTree = assertNotNull(light.tree)
      assertTrue(lightTree.nodes.isNotEmpty())
      val frame = assertNotNull(light.frame)
      assertTrue(frame.bytes.size > 8)
      assertEquals(0x89.toByte(), frame.bytes[0])
      assertEquals(0x50.toByte(), frame.bytes[1])
      assertEquals(0x4E.toByte(), frame.bytes[2])
      assertEquals(0x47.toByte(), frame.bytes[3])

      val dark = batch.targets[1]
      assertNotNull(dark.tree)
      assertNull(dark.frame)
    }
  }

  @Test
  fun `prepare timeout and ping failure kill unhealthy worker so next prepare recovers`(
    @org.junit.jupiter.api.io.TempDir tmp: Path,
  ) {
    val timeoutDir = tmp.resolve("hang").also { Files.createDirectories(it) }
    LayoutlibRenderWorker(
        meta = meta,
        controlTimeoutMs = 300L,
        workerCommandFactory = { fakeCommand("hang-first-ping", timeoutDir) },
      )
      .use { worker ->
        val err = kotlin.runCatching { worker.prepare() }.exceptionOrNull()
        assertNotNull(err, "Expected prepare() to throw on PING timeout")
        assertNull(worker.currentWorkerPid(), "Hung worker must be cleared after prepare failure")

        worker.prepare()
        val recoveredPid = assertNotNull(worker.currentWorkerPid())
        assertTrue(ProcessHandle.of(recoveredPid).map { it.isAlive }.orElse(false))
      }

    val failDir = tmp.resolve("fail").also { Files.createDirectories(it) }
    LayoutlibRenderWorker(
        meta = meta,
        workerCommandFactory = { fakeCommand("fail-first-ping", failDir) },
      )
      .use { worker ->
        val err = kotlin.runCatching { worker.prepare() }.exceptionOrNull()
        assertNotNull(err, "Expected prepare() to throw on PING ok=false")
        assertNull(
          worker.currentWorkerPid(),
          "Unhealthy worker must be cleared after PING ok=false",
        )

        worker.prepare()
        val recoveredPid = assertNotNull(worker.currentWorkerPid())
        assertTrue(ProcessHandle.of(recoveredPid).map { it.isAlive }.orElse(false))
      }
  }

  @Test
  fun `renderTargets retries with classBundleBytes when worker reports class hash not preloaded`(
    @org.junit.jupiter.api.io.TempDir tmp: Path,
  ) {
    val src = tmp.resolve("Board.explore.kt").also { Files.writeString(it, "") }
    val entry = fakeCompiledEntry(src)
    val target =
      listOf(
        WorkerRenderTarget("Main", "home", Views.Light.name, includeFrame = false),
      )

    LayoutlibRenderWorker(
        meta = meta,
        workerCommandFactory = { fakeCommand("evict-after-preload", tmp) },
      )
      .use { worker ->
        worker.prepare()
        val first = worker.renderTargets(entry, target, tmp)
        assertTrue(first.ok, first.error)

        // Second render assumes cached preload, worker reports eviction, worker helper retries
        // once with full classBundleBytes and re-populates preloadedHashes.
        val second = worker.renderTargets(entry, target, tmp)
        assertTrue(second.ok, second.error)

        val third = worker.renderTargets(entry, target, tmp)
        assertTrue(third.ok, third.error)

        val logLines = Files.readAllLines(tmp.resolve("wire-bytes-log.txt"))
        assertEquals(
          listOf("with-bytes", "null-bytes", "with-bytes", "null-bytes"),
          logLines,
        )
      }
  }

  @Test
  fun `batch timeout kills and respawns hung worker and keeps server healthy`(
    @org.junit.jupiter.api.io.TempDir tmp: Path,
  ) {
    val src = tmp.resolve("Board.explore.kt").also { Files.writeString(it, "") }
    val entry = fakeCompiledEntry(src)
    LayoutlibRenderWorker(
        meta = meta,
        batchTimeoutMs = 400L,
        workerCommandFactory = { fakeCommand("hang-until-killed", tmp) },
      )
      .use { worker ->
        worker.prepare()
        val firstPid = assertNotNull(worker.currentWorkerPid())
        val target =
          listOf(
            WorkerRenderTarget("Main", "home", Views.Light.name, includeFrame = true),
          )
        val first = worker.renderTargets(entry, target, tmp)
        assertFalse(first.ok, "Timed-out batch must return ok=false")
        assertTrue(first.error?.contains("timed out", ignoreCase = true) == true, first.error)

        val secondPid = assertNotNull(worker.currentWorkerPid())
        assertNotEquals(firstPid, secondPid, "Expected respawned worker PID after timeout")

        val recovery = worker.renderTargets(entry, target, tmp)
        assertTrue(recovery.ok, recovery.error)
        assertEquals(1, recovery.targets.size)
        val recFrame = assertNotNull(recovery.targets.single().frame)
        assertTrue(recFrame.bytes.size > 8)
      }
  }

  @Test
  fun `recycleRecommended respawns worker between batches`(
    @org.junit.jupiter.api.io.TempDir tmp: Path,
  ) {
    val src = tmp.resolve("Board.explore.kt").also { Files.writeString(it, "") }
    val entry = fakeCompiledEntry(src)
    LayoutlibRenderWorker(
        meta = meta,
        workerCommandFactory = { fakeCommand("recycle-always") },
      )
      .use { worker ->
        worker.prepare()
        val pid1 = assertNotNull(worker.currentWorkerPid())
        val target =
          listOf(
            WorkerRenderTarget("Main", "home", Views.Light.name, includeFrame = false),
          )
        val b1 = worker.renderTargets(entry, target, tmp)
        assertTrue(b1.ok, b1.error)

        val pid2 = assertNotNull(worker.currentWorkerPid())
        assertNotEquals(pid1, pid2, "Expected fresh worker PID after recycleRecommended=true")
        assertFalse(ProcessHandle.of(pid1).map { it.isAlive }.orElse(false))
      }
  }

  @Test
  fun `closing worker sends stdin EOF so child exits within two seconds`() {
    val worker =
      LayoutlibRenderWorker(
        meta = meta,
        workerCommandFactory = { fakeCommand("normal") },
      )
    worker.prepare()
    val pid = assertNotNull(worker.currentWorkerPid())
    val handle = ProcessHandle.of(pid).orElseThrow()
    worker.close()
    val exited = handle.onExit().runCatching { get(2, TimeUnit.SECONDS) }.isSuccess
    assertTrue(exited && !handle.isAlive, "Child worker did not exit within 2s of close()")
  }

  @Test
  fun `layoutlib backend runs start status check tree render and board without adb and kills worker on SIGKILL`(
    @org.junit.jupiter.api.io.TempDir tmp: Path,
  ) {
    val cacheDir = tmp.resolve("cache")
    val repoDir = tmp.resolve("repo")
    val designDir = repoDir.resolve("design")
    Files.createDirectories(repoDir.resolve(".git"))
    Files.createDirectories(designDir)
    repoRoot
      .resolve("evals/multiturn_fixture/design")
      .toFile()
      .copyRecursively(designDir.toFile(), overwrite = true)
    val boardFile = designDir.resolve("proposal.explore.kt")

    val adbMarker = tmp.resolve("adb-was-invoked.marker")
    val fakeAdb = tmp.resolve("fake-adb.sh")
    Files.writeString(
      fakeAdb,
      """#!/bin/sh
touch "$adbMarker"
exit 99
""",
    )
    fakeAdb.toFile().setExecutable(true)

    val java21 = LayoutlibRenderWorker.resolveJava21Binary()
    val bdir = meta.bootstrapDir
    val env =
      mapOf(
        RuntimeIdentity.SRC_ENV to repoRoot.resolve("app").toString(),
        BootstrapMeta.BOOTSTRAP_DIR_ENV to bdir.toString(),
        RuntimeIdentity.SERVER_CP_ENV to bdir.resolve("server.cp").toString(),
        RuntimeIdentity.WORKER_CP_ENV to meta.workerCp.toString(),
        ServerPaths.CACHE_DIR_ENV to cacheDir.toString(),
        PreviewClient.NO_OPEN_ENV to "1",
        "ANDROID_DESIGN_ADB_BIN" to fakeAdb.toString(),
        "ANDROID_DESIGN_PREVIEW_JAVA" to java21,
      )

    var serverPid: Long? = null
    try {
      val startOut = ByteArrayOutputStream()
      val startCode =
        StartCommand(cacheRoot = cacheDir, env = env)
          .run(
            listOf(repoDir.toString()),
            PrintStream(startOut),
            PrintStream(ByteArrayOutputStream()),
          )
      val startJson = startOut.toString(StandardCharsets.UTF_8).trim()
      assertEquals(ClientExitCode.OK, startCode, startJson)
      val startResp =
        PreviewJson.compact.decodeFromString(StartCommand.StartResponse.serializer(), startJson)
      assertEquals(RuntimeIdentity.BACKEND_LAYOUTLIB, startResp.backend)
      assertNull(startResp.serial)
      assertNull(startResp.forwardPort)
      serverPid = startResp.serverPid

      val client = PreviewClient(cacheRoot = cacheDir, env = env)
      val statusOut = ByteArrayOutputStream()
      val statusCode =
        StatusCommand(client)
          .run(
            listOf(repoDir.toString()),
            PrintStream(statusOut),
            PrintStream(ByteArrayOutputStream()),
          )
      assertEquals(ClientExitCode.OK, statusCode)
      val status =
        PreviewJson.compact.decodeFromString(
          StatusResponse.serializer(),
          statusOut.toString(StandardCharsets.UTF_8).trim(),
        )
      assertEquals(RuntimeIdentity.BACKEND_LAYOUTLIB, status.backend)
      assertNull(status.deviceSerial)
      assertNull(status.adbForwardPort)

      val checkOut = ByteArrayOutputStream()
      val checkCode =
        CheckCommand(client)
          .run(
            listOf(boardFile.toString()),
            PrintStream(checkOut),
            PrintStream(ByteArrayOutputStream()),
          )
      val checkJson = checkOut.toString(StandardCharsets.UTF_8).trim()
      assertTrue(
        checkCode == ClientExitCode.OK || checkCode == 1,
        "checkCode=$checkCode: $checkJson",
      )
      val checkResp = PreviewJson.compact.decodeFromString(CheckResponse.serializer(), checkJson)
      assertTrue(checkResp.valid, checkJson)
      assertTrue(
        checkResp.renders.size >= 15,
        "Expected >=15 renders, got ${checkResp.renders.size}",
      )

      val treeOut = ByteArrayOutputStream()
      val dirName = "Editorial split"
      val treeCode =
        TreeCommand(client)
          .run(
            listOf(
              boardFile.toString(),
              "--direction",
              dirName,
              "--view",
              "light",
            ),
            PrintStream(treeOut),
            PrintStream(ByteArrayOutputStream()),
          )
      val treeJson = treeOut.toString(StandardCharsets.UTF_8).trim()
      assertEquals(ClientExitCode.OK, treeCode, treeJson)
      val treeResp = PreviewJson.compact.decodeFromString(TreeResponse.serializer(), treeJson)
      val tree = assertNotNull(treeResp.tree)
      assertTrue(treeResp.ok && tree.nodes.isNotEmpty())

      val renderOutDir = tmp.resolve("out-renders")
      val renderOut = ByteArrayOutputStream()
      val renderCode =
        bot.charlies.androiddesign.preview.app.cli
          .RenderCommand(client = client)
          .run(
            listOf(boardFile.toString(), "--out", renderOutDir.toString(), "--views", "light"),
            PrintStream(renderOut),
            PrintStream(ByteArrayOutputStream()),
          )
      assertTrue(renderCode == 0 || renderCode == 1, "renderCode=$renderCode")
      assertTrue(Files.isRegularFile(renderOutDir.resolve("report.json")))

      // Verify browser board /api/board and /api/board/render return PNG cards.
      val ready = client.probe(repoDir) as PreviewClient.ServerLookup.Ready
      val boardGetConn =
        (URI("http://127.0.0.1:${ready.record.port}/api/board?view=${ready.record.viewToken}")
          .toURL()
          .openConnection() as HttpURLConnection)
      boardGetConn.setRequestProperty("Host", "127.0.0.1:${ready.record.port}")
      assertEquals(200, boardGetConn.responseCode)
      val boardJson = boardGetConn.inputStream.bufferedReader().readText()
      boardGetConn.disconnect()
      assertTrue(boardJson.contains(dirName), boardJson)

      val boardUrl =
        URI("http://127.0.0.1:${ready.record.port}/api/board/render?view=${ready.record.viewToken}")
          .toURL()
      val conn = (boardUrl.openConnection() as HttpURLConnection)
      conn.requestMethod = "POST"
      conn.doOutput = true
      conn.setRequestProperty("Host", "127.0.0.1:${ready.record.port}")
      conn.setRequestProperty("Origin", "http://127.0.0.1:${ready.record.port}")
      conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
      val screenId = assertNotNull(treeResp.screen)
      val body = """{"direction":"$dirName","screen":"$screenId","view":"light"}""".toByteArray()
      conn.outputStream.use { it.write(body) }
      assertEquals(200, conn.responseCode)
      val cardResp =
        PreviewJson.compact.decodeFromString(
          BoardRenderResponse.serializer(),
          conn.inputStream.bufferedReader().readText(),
        )
      conn.disconnect()
      assertEquals("image/png", cardResp.imageMime)
      assertFalse(cardResp.pngBase64.isNullOrBlank(), "Expected non-empty PNG card payload")

      assertFalse(Files.exists(adbMarker), "adb must NEVER be invoked on layoutlib backend")

      // Find the child layoutlib worker JVM and SIGKILL the server process: no orphan left behind.
      val serverHandle = ProcessHandle.of(serverPid).orElseThrow()
      val childWorker =
        serverHandle
          .descendants()
          .filter { h ->
            h.info().commandLine().orElse("").contains("LayoutlibWorkerMainKt")
          }
          .findFirst()
          .orElse(null)
      assertNotNull(childWorker, "Expected child LayoutlibWorkerMainKt process under server")
      assertTrue(serverHandle.destroyForcibly(), "SIGKILL server failed")
      val serverDead = serverHandle.onExit().runCatching { get(5, TimeUnit.SECONDS) }.isSuccess
      val workerDead = childWorker.onExit().runCatching { get(5, TimeUnit.SECONDS) }.isSuccess
      assertTrue(serverDead && !serverHandle.isAlive, "Server process did not die")
      assertTrue(
        workerDead && !childWorker.isAlive,
        "Orphaned worker process stayed alive after SIGKILL",
      )
    } finally {
      serverPid?.let { pid ->
        ProcessHandle.of(pid).ifPresent { h -> h.destroyForcibly() }
      }
    }
  }
}
