package bot.charlies.androiddesign.preview.app.client

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.app.server.RuntimeIdentity
import bot.charlies.androiddesign.preview.app.server.ServerPaths
import bot.charlies.androiddesign.preview.app.server.ServerRecord
import bot.charlies.androiddesign.preview.app.server.StatusResponse
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.model.PreviewJson
import com.sun.net.httpserver.HttpServer
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/** Timeout recovery and startup waiting never orphan or needlessly kill the shared server. */
class DeadlineRecoveryTest {
  private class Env(
    val tmp: Path,
    val cache: Path,
    val repo: Path,
    val spawnMarker: Path,
    val client: PreviewClient,
    val identity: RuntimeIdentity,
  )

  private fun withEnv(tmp: Path, block: (Env) -> Unit) {
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
      apkSha256=sha-1
      composeCompilerPluginJar=/tmp/compose.jar
      """
        .trimIndent(),
    )
    val spawnMarker = tmp.resolve("spawned.txt")
    val fakeJdk = tmp.resolve("fake-jdk")
    val fakeJava = fakeJdk.resolve("bin/java")
    Files.createDirectories(fakeJava.parent)
    Files.writeString(fakeJava, "#!/bin/sh\necho spawned >> \"$spawnMarker\"\nexit 1\n")
    fakeJava.toFile().setExecutable(true)
    val env =
      mapOf(
        RuntimeIdentity.BACKEND_ENV to RuntimeIdentity.BACKEND_ANDROID,
        BootstrapMeta.BOOTSTRAP_DIR_ENV to bdir.toString(),
        RuntimeIdentity.SERVER_CP_ENV to bdir.resolve("server.cp").toString(),
      )
    val prevJavaHome = System.getProperty("java.home")
    try {
      System.setProperty("java.home", fakeJdk.toString())
      block(
        Env(
          tmp,
          cache,
          repo,
          spawnMarker,
          PreviewClient(cacheRoot = cache, env = env, progress = {}),
          RuntimeIdentity.compute(env),
        ),
      )
    } finally {
      System.setProperty("java.home", prevJavaHome)
    }
  }

  /** A live stub server answering `/api/status`; records `/api/stop` calls. */
  private class Stub(val http: HttpServer, val stops: CopyOnWriteArrayList<String>) {
    val port: Int
      get() = http.address.port
  }

  private fun stub(identity: RuntimeIdentity, repo: Path, pid: Long): Stub {
    val stops = CopyOnWriteArrayList<String>()
    val http = HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0)
    http.createContext("/api/status") { ex ->
      val body =
        PreviewJson.compact.encodeToString(
          StatusResponse.serializer(),
          StatusResponse(
            pid = pid,
            port = http.address.port,
            jarVersion = AppInfo.VERSION,
            startedAt = 1_000L,
            compileCount = 1,
            repoRoot = repo.toString(),
            boardUrl = "http://127.0.0.1:${http.address.port}",
            backend = identity.backend,
            runtimeHash = identity.runtimeHash,
          ),
        )
      val bytes = body.toByteArray(StandardCharsets.UTF_8)
      ex.sendResponseHeaders(200, bytes.size.toLong())
      ex.responseBody.use { it.write(bytes) }
    }
    http.createContext("/api/stop") { ex ->
      stops.add("stop")
      val bytes = """{"ok":true}""".toByteArray(StandardCharsets.UTF_8)
      ex.sendResponseHeaders(200, bytes.size.toLong())
      ex.responseBody.use { it.write(bytes) }
    }
    http.start()
    return Stub(http, stops)
  }

  private fun record(identity: RuntimeIdentity, repo: Path, pid: Long, port: Int, startedAt: Long) =
    ServerRecord(
      pid = pid,
      port = port,
      jarVersion = AppInfo.VERSION,
      startedAt = startedAt,
      repoRoot = repo.toString(),
      clientSecret = "sec",
      viewToken = "view",
      backend = identity.backend,
      runtimeHash = identity.runtimeHash,
      apkSha256 = identity.apkSha256,
    )

  private fun freePort(): Int =
    ServerSocket(0, 0, InetAddress.getLoopbackAddress()).use { it.localPort }

  @Test
  fun `stale killAndRespawn adopts the server another CLI already started`(@TempDir tmp: Path) =
    withEnv(tmp) { e ->
      val live = stub(e.identity, e.repo, pid = 999_999_002L)
      try {
        val dead = record(e.identity, e.repo, 999_999_001L, live.port, startedAt = 1_000L)
        val current = record(e.identity, e.repo, 999_999_002L, live.port, startedAt = 2_000L)
        ServerPaths.writeRecord(ServerPaths.sharedRecordPath(e.cache), current)

        val result = e.client.killAndRespawn(e.repo, dead)

        val ready = result as PreviewClient.ServerLookup.Ready
        assertEquals(999_999_002L, ready.record.pid)
        assertFalse(Files.exists(e.spawnMarker), "must not spawn a second server")
        assertTrue(live.stops.isEmpty(), "must not stop the healthy replacement")
      } finally {
        live.http.stop(0)
      }
    }

  @Test
  fun `deadline on a responsive server leaves it running`(@TempDir tmp: Path) =
    withEnv(tmp) { e ->
      val live = stub(e.identity, e.repo, pid = 999_999_003L)
      try {
        val rec = record(e.identity, e.repo, 999_999_003L, live.port, startedAt = 1_000L)
        ServerPaths.writeRecord(ServerPaths.sharedRecordPath(e.cache), rec)

        val outcome = e.client.recoverFromDeadline(e.repo, rec)

        assertEquals(PreviewClient.DeadlineRecovery.SERVER_RESPONSIVE, outcome)
        assertFalse(Files.exists(e.spawnMarker))
        assertTrue(live.stops.isEmpty())
      } finally {
        live.http.stop(0)
      }
    }

  @Test
  fun `deadline on an unresponsive server replaces it`(@TempDir tmp: Path) =
    withEnv(tmp) { e ->
      val rec = record(e.identity, e.repo, 999_999_004L, freePort(), startedAt = 1_000L)
      ServerPaths.writeRecord(ServerPaths.sharedRecordPath(e.cache), rec)

      val outcome = e.client.recoverFromDeadline(e.repo, rec)

      assertEquals(PreviewClient.DeadlineRecovery.RESTARTED, outcome)
      assertTrue(Files.exists(e.spawnMarker), "unresponsive server must be respawned")
    }

  private fun fakeChild(script: String): Process =
    ProcessBuilder("sh", "-c", script).redirectErrorStream(true).start()

  @Test
  fun `startup wait fails fast when the child exits`(@TempDir tmp: Path) =
    withEnv(tmp) { e ->
      val log = tmp.resolve("log.txt").also { Files.writeString(it, "boom: no device\n") }
      val proc = fakeChild("exit 3")
      val start = System.currentTimeMillis()

      val result = e.client.awaitServerStart(proc, log.toFile(), 0L, waitMs = 30_000L)

      val notReady = result as PreviewClient.ServerLookup.NotReady
      assertTrue(notReady.message.contains("exited"), notReady.message)
      assertTrue(notReady.message.contains("boom"), notReady.message)
      assertTrue(System.currentTimeMillis() - start < 5_000L)
    }

  @Test
  fun `startup wait keeps waiting while the child lives, reports progress, then caps`(
    @TempDir tmp: Path,
  ) =
    withEnv(tmp) { e ->
      val messages = CopyOnWriteArrayList<String>()
      val client =
        PreviewClient(cacheRoot = e.cache, env = emptyMap(), progress = { messages.add(it) })
      val log = tmp.resolve("log.txt").also { Files.writeString(it, "startup: preparing device\n") }
      val proc = fakeChild("sleep 30")

      val result =
        client.awaitServerStart(proc, log.toFile(), 0L, waitMs = 700L, progressIntervalMs = 100L)

      assertTrue(result is PreviewClient.ServerLookup.NotReady)
      assertTrue((result as PreviewClient.ServerLookup.NotReady).message.contains("Timed out"))
      assertTrue(messages.isNotEmpty(), "expected progress output while waiting")
      assertTrue(messages.first().contains("preparing device"), messages.first())
      proc.waitFor()
      assertFalse(proc.isAlive)
    }
}
