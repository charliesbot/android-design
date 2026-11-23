package bot.charlies.androiddesign.preview.app.client

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.app.server.BoardSelectionRequest
import bot.charlies.androiddesign.preview.app.server.CheckResponse
import bot.charlies.androiddesign.preview.app.server.ContentHasher
import bot.charlies.androiddesign.preview.app.server.SelectionResponse
import bot.charlies.androiddesign.preview.app.server.ServerPaths
import bot.charlies.androiddesign.preview.app.server.ServerRecord
import bot.charlies.androiddesign.preview.app.server.StatusResponse
import bot.charlies.androiddesign.preview.app.server.TimeoutEntry
import bot.charlies.androiddesign.preview.app.server.TreeResponse
import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.PreviewJson
import com.sun.net.httpserver.HttpServer
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CliEndToEndTest {
  private val jarPath: Path =
    Path.of(
      System.getProperty("preview.jar") ?: error("preview.jar system property missing"),
    )
  private val binPath: Path =
    Path.of(
      System.getProperty("preview.bin") ?: error("preview.bin system property missing"),
    )

  private data class CliResult(val exitCode: Int, val stdout: String, val stderr: String)

  private fun execCli(
    cwd: Path,
    cacheDir: Path,
    vararg args: String,
    extraEnv: Map<String, String> = emptyMap(),
  ): CliResult {
    val pb = ProcessBuilder(listOf(binPath.toString()) + args).directory(cwd.toFile())
    pb.environment()[PreviewClient.JAR_ENV] = jarPath.toString()
    pb.environment()[PreviewClient.NO_OPEN_ENV] = "1"
    pb.environment()[ServerPaths.CACHE_DIR_ENV] = cacheDir.toString()
    extraEnv.forEach { (k, v) -> pb.environment()[k] = v }
    val proc = pb.start()
    val finished = proc.waitFor(60, TimeUnit.SECONDS)
    if (!finished) {
      proc.destroyForcibly()
      error("CLI timed out: ${args.joinToString(" ")}")
    }
    return CliResult(
      exitCode = proc.exitValue(),
      stdout = proc.inputStream.bufferedReader(Charsets.UTF_8).readText(),
      stderr = proc.errorStream.bufferedReader(Charsets.UTF_8).readText(),
    )
  }

  private fun sampleBoard(): String =
    """
    import androidx.compose.foundation.layout.*
    import androidx.compose.material3.*
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.platform.testTag
    import androidx.compose.ui.unit.dp
    import bot.charlies.androiddesign.preview.kit.*

    val preview = designBoard("Plates", seed = 0xFFB5523BL, variant = PaletteVariant.TonalSpot) {
      direction("Editorial") {
        screen("home") {
          Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Dinner")
            Button(onClick = {}, modifier = Modifier.testTag(AD_PRIMARY_TAG).height(56.dp)) {
              Text("Open")
            }
          }
        }
      }
    }
    """
      .trimIndent()

  @Test
  fun `end-to-end bin script covers all subcommands, exit codes 0 to 3, and check selection delivery`() {
    assertTrue(Files.isExecutable(binPath), "bootstrap script must be executable: $binPath")
    assertTrue(Files.isRegularFile(jarPath), "workerJar must exist: $jarPath")

    val tmp = createTempDirectory("v5-e2e-")
    val cacheDir = tmp.resolve("cache")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val source = repo.resolve("plates.explore.kt").also { Files.writeString(it, sampleBoard()) }
    try {
      // --help exits 0 for top-level and subcommands.
      for (cmd in
        listOf(
          "start",
          "check",
          "tree",
          "selection",
          "open",
          "status",
          "stop",
          "render",
          "install",
        )) {
        val h = execCli(repo, cacheDir, cmd, "--help")
        assertEquals(0, h.exitCode, "$cmd --help failed: ${h.stderr}")
        assertTrue(h.stdout.isNotBlank(), "$cmd --help should print help")
      }

      // Usage error exits 2.
      val badUsage = execCli(repo, cacheDir, "check")
      assertEquals(2, badUsage.exitCode, badUsage.stdout)

      // status before server starts exits 3 (server not ready).
      val coldStatus = execCli(repo, cacheDir, "status", repo.toString())
      assertEquals(3, coldStatus.exitCode, coldStatus.stdout)

      // selection before any click exits 1 with ask prompt.
      val emptySel = execCli(repo, cacheDir, "selection", source.toString())
      assertEquals(1, emptySel.exitCode, emptySel.stdout)
      val emptySelJson =
        PreviewJson.compact.decodeFromString(SelectionResponse.serializer(), emptySel.stdout.trim())
      assertFalse(emptySelJson.ok)
      assertNotNull(emptySelJson.ask)

      // Scoped check starts server, compiles, renders 1x PNG, exits 0, single JSON line, no
      // selection yet.
      val check1 =
        execCli(
          repo,
          cacheDir,
          "check",
          source.toString(),
          "--direction",
          "Editorial",
          "--view",
          "light",
        )
      assertEquals(0, check1.exitCode, "check1 failed: ${check1.stdout} ${check1.stderr}")
      assertEquals(1, check1.stdout.trim().lines().size, "stdout must be one JSON line")
      val r1 =
        PreviewJson.compact.decodeFromString(CheckResponse.serializer(), check1.stdout.trim())
      assertTrue(r1.ok && r1.valid && r1.pass && r1.scoped)
      assertNull(r1.selection)
      assertEquals(1, r1.images.size)
      assertEquals(1, r1.images.single().density)
      assertTrue(Files.isRegularFile(Path.of(r1.images.single().path)))

      // Warm status exits 0 and reports running server.
      val warmStatus = execCli(repo, cacheDir, "status", repo.toString())
      assertEquals(0, warmStatus.exitCode, warmStatus.stdout)
      val st =
        PreviewJson.compact.decodeFromString(StatusResponse.serializer(), warmStatus.stdout.trim())
      assertTrue(st.running)
      assertEquals(AppInfo.VERSION, st.jarVersion)

      // tree returns layout bounds and file:line.
      val treeRes =
        execCli(
          repo,
          cacheDir,
          "tree",
          source.toString(),
          "--direction",
          "Editorial",
          "--view",
          "light",
        )
      assertEquals(0, treeRes.exitCode, treeRes.stdout)
      val treeJson =
        PreviewJson.compact.decodeFromString(TreeResponse.serializer(), treeRes.stdout.trim())
      assertTrue(treeJson.ok)
      assertTrue(treeJson.tree?.nodes?.isNotEmpty() == true)

      // Simulate a board click selection via HTTP (viewToken + Origin), then verify check includes
      // it ONCE.
      val record = assertNotNull(ServerPaths.readRecord(ServerPaths.sharedRecordPath(cacheDir)))
      val selReq =
        PreviewJson.compact.encodeToString(
          BoardSelectionRequest.serializer(),
          BoardSelectionRequest(
            path = source.toString(),
            file = "plates.explore.kt",
            line = 12,
            callName = "Button",
            callChain = listOf("Column"),
            direction = "Editorial",
            screen = "home",
            view = "light",
            bounds = Bounds(16f, 40f, 100f, 56f),
            contentHash = assertNotNull(r1.contentHash),
          ),
        )
      val conn =
        URI("http://127.0.0.1:${record.port}/api/board/selection").toURL().openConnection()
          as HttpURLConnection
      try {
        conn.requestMethod = "POST"
        conn.setRequestProperty("Host", "127.0.0.1:${record.port}")
        conn.setRequestProperty("Origin", "http://127.0.0.1:${record.port}")
        conn.setRequestProperty("X-View-Token", record.viewToken)
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.outputStream.use { it.write(selReq.toByteArray(Charsets.UTF_8)) }
        assertEquals(200, conn.responseCode)
      } finally {
        conn.disconnect()
      }

      // `check` right after click carries `selection`.
      val checkWithSel =
        execCli(
          repo,
          cacheDir,
          "check",
          source.toString(),
          "--direction",
          "Editorial",
          "--view",
          "light",
        )
      assertEquals(0, checkWithSel.exitCode, checkWithSel.stdout)
      val rSel =
        PreviewJson.compact.decodeFromString(CheckResponse.serializer(), checkWithSel.stdout.trim())
      assertEquals("Button", rSel.selection?.callName)
      assertEquals(12, rSel.selection?.line)

      // Subsequent `check` with no new click omits `selection`.
      val checkAfter =
        execCli(
          repo,
          cacheDir,
          "check",
          source.toString(),
          "--direction",
          "Editorial",
          "--view",
          "light",
        )
      assertEquals(0, checkAfter.exitCode, checkAfter.stdout)
      val rAfter =
        PreviewJson.compact.decodeFromString(CheckResponse.serializer(), checkAfter.stdout.trim())
      assertNull(rAfter.selection)

      // `selection <file>` still returns the recorded selection with exit 0.
      val selCmd = execCli(repo, cacheDir, "selection", source.toString())
      assertEquals(0, selCmd.exitCode, selCmd.stdout)
      val selJson =
        PreviewJson.compact.decodeFromString(SelectionResponse.serializer(), selCmd.stdout.trim())
      assertEquals("Button", selJson.selection?.callName)

      // `open <file>` returns ?repo=<id>&view=<token> boardUrl with exit 0.
      val openCmd = execCli(repo, cacheDir, "open", source.toString())
      assertEquals(0, openCmd.exitCode, openCmd.stdout)
      assertTrue(record.boardUrl(ServerPaths.repoHash(repo)) in openCmd.stdout, openCmd.stdout)

      // `install --bin-dir` links the script cleanly.
      val fakeBin = tmp.resolve("local-bin")
      val installCmd = execCli(repo, cacheDir, "install", "--bin-dir", fakeBin.toString())
      assertEquals(0, installCmd.exitCode, installCmd.stdout)
      assertTrue(Files.exists(fakeBin.resolve("android-design-preview")))

      // `render` writes PNGs, trees, and report.json.
      val renderOut = tmp.resolve("renders")
      val renderCmd =
        execCli(
          repo,
          cacheDir,
          "render",
          source.toString(),
          "--out",
          renderOut.toString(),
          "--views",
          "light",
        )
      assertEquals(0, renderCmd.exitCode, renderCmd.stderr)
      assertTrue(Files.isRegularFile(renderOut.resolve("report.json")))

      // `stop` cleanly shuts down the server, and status then exits 3.
      val stopCmd = execCli(repo, cacheDir, "stop", repo.toString())
      assertEquals(0, stopCmd.exitCode, stopCmd.stdout)
      Thread.sleep(150)
      val afterStopStatus = execCli(repo, cacheDir, "status", repo.toString())
      assertEquals(3, afterStopStatus.exitCode, afterStopStatus.stdout)
    } finally {
      execCli(repo, cacheDir, "stop", repo.toString())
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `dead server record with recorded timeout fast-fails before respawn and clears on edit`() {
    val tmp = createTempDirectory("v5-e2e-timeout-record-")
    val cacheDir = tmp.resolve("cache")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val source = repo.resolve("hang.explore.kt").also { Files.writeString(it, sampleBoard()) }
    try {
      val snap = ContentHasher.snapshot(source)
      val recordPath = ServerPaths.sharedRecordPath(cacheDir)
      // Simulate a dead server record left behind after watchdog Runtime.halt(1).
      ServerPaths.writeRecord(
        recordPath,
        ServerRecord(
          pid = 999_999L,
          port = 59199,
          jarVersion = AppInfo.VERSION,
          startedAt = System.currentTimeMillis() - 10_000L,
          repoRoot = repo.toAbsolutePath().normalize().toString(),
          clientSecret = ServerPaths.randomToken(),
          viewToken = ServerPaths.randomToken(),
          timeouts = listOf(TimeoutEntry(snap.contentHash, "compile", System.currentTimeMillis())),
          haltedAt = System.currentTimeMillis(),
        ),
      )

      // Client must detect recorded timeout BEFORE overwriting dead record or respawning!
      val fastFail =
        execCli(
          repo,
          cacheDir,
          "check",
          source.toString(),
          "--direction",
          "Editorial",
          "--view",
          "light",
        )
      assertEquals(1, fastFail.exitCode, fastFail.stdout)
      val resp =
        PreviewJson.compact.decodeFromString(CheckResponse.serializer(), fastFail.stdout.trim())
      assertEquals("timeout", resp.error)
      // Dead record PID was NOT overwritten.
      assertEquals(999_999L, ServerPaths.readRecord(recordPath)?.pid)

      // Editing the file changes contentHash -> client respawns and succeeds!
      Files.writeString(source, sampleBoard() + "\n// edited\n")
      val recovered =
        execCli(
          repo,
          cacheDir,
          "check",
          source.toString(),
          "--direction",
          "Editorial",
          "--view",
          "light",
        )
      assertEquals(0, recovered.exitCode, "${recovered.stdout} ${recovered.stderr}")
    } finally {
      execCli(repo, cacheDir, "stop", repo.toString())
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `external client timeout kills wedged server, respawns fresh server, and returns timeout`() {
    val tmp = createTempDirectory("v5-e2e-ext-timeout-")
    val cacheDir = tmp.resolve("cache")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val source = repo.resolve("wedged.explore.kt").also { Files.writeString(it, sampleBoard()) }
    // Launch a real dummy sleeper subprocess whose PID the client can kill, paired with a stub HTTP
    // server that hangs on /api/check.
    val dummyProc = ProcessBuilder("sleep", "60").start()
    val stubSecret = ServerPaths.randomToken()
    val stubView = ServerPaths.randomToken()
    val stubServer = HttpServer.create(InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0), 0)
    val stubPort = stubServer.address.port
    stubServer.createContext("/api/status") { ex ->
      val body =
        """{"ok":true,"running":true,"pid":${dummyProc.pid()},"port":$stubPort,"jarVersion":"${AppInfo.VERSION}","startedAt":1,"compileCount":1,"repoRoot":"$repo","boardUrl":"http://127.0.0.1:$stubPort/?view=$stubView"}"""
          .toByteArray()
      ex.sendResponseHeaders(200, body.size.toLong())
      ex.responseBody.use { it.write(body) }
    }
    stubServer.createContext("/api/check") { _ ->
      // Intentionally hang past the client's external timeout.
      try {
        Thread.sleep(5_000)
      } catch (_: InterruptedException) {}
    }
    stubServer.start()
    try {
      ServerPaths.writeRecord(
        ServerPaths.sharedRecordPath(cacheDir),
        ServerRecord(
          pid = dummyProc.pid(),
          port = stubPort,
          jarVersion = AppInfo.VERSION,
          startedAt = dummyProc.toHandle().info().startInstant().get().toEpochMilli(),
          repoRoot = repo.toAbsolutePath().normalize().toString(),
          clientSecret = stubSecret,
          viewToken = stubView,
          jarPath = jarPath.toRealPath().toString(),
          jarModified = Files.getLastModifiedTime(jarPath).toMillis(),
        ),
      )

      val res =
        execCli(
          repo,
          cacheDir,
          "check",
          source.toString(),
          "--direction",
          "Editorial",
          "--view",
          "light",
          extraEnv = mapOf(PreviewClient.CLIENT_TIMEOUT_ENV to "300"),
        )
      assertEquals(1, res.exitCode, res.stdout)
      assertTrue("\"timeout\"" in res.stdout, res.stdout)
      // Dummy wedged PID was killed and a fresh real server was spawned!
      assertTrue(
        dummyProc.waitFor(3, TimeUnit.SECONDS),
        "wedged server PID should have been killed",
      )
      val freshStatus = execCli(repo, cacheDir, "status", repo.toString())
      assertEquals(
        0,
        freshStatus.exitCode,
        "respawned server should be live: ${freshStatus.stdout}",
      )
    } finally {
      stubServer.stop(0)
      dummyProc.destroyForcibly()
      execCli(repo, cacheDir, "stop", repo.toString())
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `bootstrap script emits JSON error line on stdout when JAR prerequisite is missing`() {
    val tmp = createTempDirectory("v5-e2e-missing-jar-")
    val cacheDir = tmp.resolve("cache")
    try {
      val pb = ProcessBuilder(binPath.toString(), "status").directory(tmp.toFile())
      pb.environment()[PreviewClient.JAR_ENV] = tmp.resolve("does-not-exist.jar").toString()
      pb.environment()[ServerPaths.CACHE_DIR_ENV] = cacheDir.toString()
      val proc = pb.start()
      assertTrue(proc.waitFor(10, TimeUnit.SECONDS))
      val stdout = proc.inputStream.bufferedReader(Charsets.UTF_8).readText().trim()
      assertEquals(3, proc.exitValue(), "missing JAR should exit 3")
      assertTrue("\"jar_not_found\"" in stdout, "stdout must carry JSON error line, got: '$stdout'")
    } finally {
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `watchdog TCP drop after recording timeout causes check to return exit 1 timeout`() {
    val tmp = createTempDirectory("v5-e2e-watchdog-drop-")
    val cacheDir = tmp.resolve("cache")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val source = repo.resolve("hang2.explore.kt").also { Files.writeString(it, sampleBoard()) }
    val snap = ContentHasher.snapshot(source)
    val stubSecret = ServerPaths.randomToken()
    val stubView = ServerPaths.randomToken()
    val recordPath = ServerPaths.sharedRecordPath(cacheDir)
    val stubServer = HttpServer.create(InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0), 0)
    val stubPort = stubServer.address.port
    stubServer.createContext("/api/status") { ex ->
      val body =
        """{"ok":true,"running":true,"pid":${ProcessHandle.current().pid()},"port":$stubPort,"jarVersion":"${AppInfo.VERSION}","startedAt":1,"compileCount":1,"repoRoot":"$repo","boardUrl":"http://127.0.0.1:$stubPort/?view=$stubView"}"""
          .toByteArray()
      ex.sendResponseHeaders(200, body.size.toLong())
      ex.responseBody.use { it.write(body) }
    }
    stubServer.createContext("/api/check") { ex ->
      ServerPaths.updateRecord(recordPath) { rec ->
        rec.copy(
          timeouts =
            rec.timeouts + TimeoutEntry(snap.contentHash, "compile", System.currentTimeMillis()),
          haltedAt = System.currentTimeMillis(),
        )
      }
      ex.close()
    }
    stubServer.start()
    try {
      ServerPaths.writeRecord(
        recordPath,
        ServerRecord(
          pid = ProcessHandle.current().pid(),
          port = stubPort,
          jarVersion = AppInfo.VERSION,
          startedAt = 1L,
          repoRoot = repo.toAbsolutePath().normalize().toString(),
          clientSecret = stubSecret,
          viewToken = stubView,
          jarPath = jarPath.toRealPath().toString(),
          jarModified = Files.getLastModifiedTime(jarPath).toMillis(),
        ),
      )
      val res =
        execCli(
          repo,
          cacheDir,
          "check",
          source.toString(),
          "--direction",
          "Editorial",
          "--view",
          "light",
        )
      assertEquals(1, res.exitCode, "watchdog drop must exit 1 (timeout), got: ${res.stdout}")
      val parsed =
        PreviewJson.compact.decodeFromString(CheckResponse.serializer(), res.stdout.trim())
      assertEquals("timeout", parsed.error)
    } finally {
      stubServer.stop(0)
      tmp.toFile().deleteRecursively()
    }
  }
}
