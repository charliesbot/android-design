package bot.charlies.androiddesign.preview.app.client

import bot.charlies.androiddesign.preview.app.server.ServerPaths
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** A repository never ends up with an outdated server, or two servers, after a replacement. */
class ServerReplacementTest {
  private val builtJar: Path =
    Path.of(System.getProperty("preview.jar") ?: error("preview.jar system property missing"))

  private class Fixture(val repo: Path, val cache: Path, val jar: Path, val client: PreviewClient)

  /** A repository, a cache, and a private copy of the JAR whose timestamp the test may change. */
  private fun withFixture(block: (Fixture) -> Unit) {
    val tmp = createTempDirectory("server-replacement-")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val cache = tmp.resolve("cache")
    val jar = tmp.resolve("jar/android-design-preview.jar")
    Files.createDirectories(jar.parent)
    Files.copy(builtJar, jar)
    val client =
      PreviewClient(cacheRoot = cache, env = mapOf(PreviewClient.JAR_ENV to jar.toString()))
    try {
      block(Fixture(repo, cache, jar, client))
    } finally {
      client.stop(repo)
      tmp.toFile().deleteRecursively()
    }
  }

  private fun PreviewClient.start(repo: Path): Long {
    val ready = assertIs<PreviewClient.ServerLookup.Ready>(ensureServer(repo))
    return ready.record.pid
  }

  private fun isAlive(pid: Long): Boolean = ProcessHandle.of(pid).map { it.isAlive }.orElse(false)

  /** Rewrites the record's secret so the live server answers 403: alive but not answering. */
  private fun makeUnresponsive(f: Fixture) {
    val path = ServerPaths.sharedRecordPath(f.cache)
    val record = assertNotNull(ServerPaths.readRecord(path))
    ServerPaths.writeRecord(path, record.copy(clientSecret = "not-the-secret"))
  }

  @Test
  fun `record names the JAR the server runs`() = withFixture { f ->
    f.client.start(f.repo)
    val record = assertNotNull(ServerPaths.readRecord(ServerPaths.sharedRecordPath(f.cache)))
    assertEquals(f.jar.toRealPath().toString(), record.jarPath)
    assertEquals(Files.getLastModifiedTime(f.jar).toMillis(), record.jarModified)
  }

  @Test
  fun `unchanged JAR reuses the running server`() = withFixture { f ->
    val first = f.client.start(f.repo)

    assertEquals(first, f.client.start(f.repo), "an up-to-date server must be reused")
  }

  @Test
  fun `rebuilt JAR at the same version replaces the running server`() = withFixture { f ->
    val first = f.client.start(f.repo)
    val rebuilt = Files.getLastModifiedTime(f.jar).toMillis() + 10_000
    Files.setLastModifiedTime(f.jar, FileTime.fromMillis(rebuilt))

    val second = f.client.start(f.repo)

    assertNotEquals(first, second, "a rebuilt JAR must start a fresh server")
    assertFalse(isAlive(first), "the outdated server must exit before its replacement runs")
  }

  @Test
  fun `server that stops answering is stopped before a replacement starts`() = withFixture { f ->
    val first = f.client.start(f.repo)
    makeUnresponsive(f)

    val second = f.client.start(f.repo)

    assertNotEquals(first, second)
    assertFalse(isAlive(first), "two servers must never run for one repository")
  }

  @Test
  fun `stop ends a server that no longer answers`() = withFixture { f ->
    val first = f.client.start(f.repo)
    makeUnresponsive(f)

    val stopped = f.client.stop(f.repo)

    assertEquals(first, stopped?.pid)
    assertFalse(isAlive(first))
  }

  /** Moves the record's start time outside the pid guard's window, as a clock step would. */
  private fun skewStartTime(f: Fixture) {
    val path = ServerPaths.sharedRecordPath(f.cache)
    val record = assertNotNull(ServerPaths.readRecord(path))
    ServerPaths.writeRecord(path, record.copy(startedAt = record.startedAt + 3_600_000))
  }

  @Test
  fun `stop ends an answering server even when its start time looks wrong`() = withFixture { f ->
    val first = f.client.start(f.repo)
    skewStartTime(f)

    val stopped = f.client.stop(f.repo)

    assertEquals(first, stopped?.pid)
    assertFalse(isAlive(first))
  }

  @Test
  fun `outdated answering server is replaced even when its start time looks wrong`() =
    withFixture { f ->
      val first = f.client.start(f.repo)
      skewStartTime(f)
      val rebuilt = Files.getLastModifiedTime(f.jar).toMillis() + 10_000
      Files.setLastModifiedTime(f.jar, FileTime.fromMillis(rebuilt))

      val second = f.client.start(f.repo)

      assertNotEquals(first, second)
      assertFalse(isAlive(first), "two servers must never run for one repository")
    }

  @Test
  fun `a stale record never kills an unrelated process that reused its pid`() = withFixture { f ->
    val bystander = ProcessBuilder("sleep", "60").start()
    try {
      val path = ServerPaths.sharedRecordPath(f.cache)
      f.client.start(f.repo)
      val record = assertNotNull(ServerPaths.readRecord(path))
      f.client.stop(f.repo)
      // The record now describes a server that exited an hour ago, whose pid the bystander holds.
      ServerPaths.writeRecord(
        path,
        record.copy(pid = bystander.pid(), startedAt = System.currentTimeMillis() - 3_600_000),
      )

      f.client.start(f.repo)

      assertTrue(bystander.isAlive, "an unrelated process must survive a stale record")
    } finally {
      bystander.destroyForcibly()
    }
  }
}
