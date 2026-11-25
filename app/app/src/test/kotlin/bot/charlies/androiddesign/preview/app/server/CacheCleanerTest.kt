package bot.charlies.androiddesign.preview.app.server

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class CacheCleanerTest {
  @Test
  fun `server startup cleans dead legacy records and stale renders while preserving active and timeout records`(
    @TempDir tmp: Path,
  ) {
    val cacheRoot = tmp.resolve("cache")
    val startingRepo = tmp.resolve("starting-repo").also { Files.createDirectories(it) }
    val deadRepo = tmp.resolve("dead-repo").also { Files.createDirectories(it) }
    val activeTimeoutRepo = tmp.resolve("timeout-active-repo")
    val staleTimeoutRepo = tmp.resolve("timeout-stale-repo")
    Files.createDirectories(activeTimeoutRepo.resolve("design"))
    Files.createDirectories(staleTimeoutRepo.resolve("design"))

    val activeExplore = activeTimeoutRepo.resolve("design/proposal.explore.kt")
    Files.writeString(activeExplore, "val active = 1\n")
    val activeHash = ContentHasher.snapshot(activeExplore).contentHash

    val staleExplore = staleTimeoutRepo.resolve("design/proposal.explore.kt")
    Files.writeString(staleExplore, "val updated = 2\n")

    val noopWorker =
      InMemoryRenderWorker(
        name = RuntimeIdentity.BACKEND_ANDROID,
        compileOverride = { error("unused in CacheCleanerTest") },
      )

    val startingServer =
      PreviewHttpServer(
        repoRoot = startingRepo,
        cacheRoot = cacheRoot,
        preferredPort = 0,
        idleTimeoutMs = 0L,
        worker = noopWorker,
        enableFileWatcher = false,
      )
    try {
      // Dead legacy per-repo record with no timeouts (+ paired log) -> MUST BE DELETED.
      val deadRecPath = legacyRecordPath(deadRepo, cacheRoot)
      val deadLogPath =
        ServerPaths.serversDir(cacheRoot).resolve("${ServerPaths.repoHash(deadRepo)}.log")
      Files.createDirectories(deadLogPath.parent)
      Files.writeString(deadLogPath, "old log\n")
      ServerPaths.writeRecord(
        deadRecPath,
        ServerRecord(
          pid = 999999L,
          port = 1,
          jarVersion = "0.5.0",
          startedAt = 1000L,
          repoRoot = deadRepo.toString(),
          clientSecret = "dead-secret",
          viewToken = "dead-view",
        ),
      )

      // Dead legacy record holding a timeout for its repo's CURRENT content hash -> MUST BE KEPT.
      val activeTimeoutPath = legacyRecordPath(activeTimeoutRepo, cacheRoot)
      ServerPaths.writeRecord(
        activeTimeoutPath,
        ServerRecord(
          pid = 999998L,
          port = 1,
          jarVersion = "0.5.0",
          startedAt = 1000L,
          repoRoot = activeTimeoutRepo.toString(),
          clientSecret = "t-secret",
          viewToken = "t-view",
          timeouts =
            listOf(
              TimeoutEntry(
                contentHash = activeHash,
                scope = "compile",
                recordedAt = System.currentTimeMillis(),
                repoRoot = activeTimeoutRepo.toString(),
              ),
            ),
          haltedAt = System.currentTimeMillis(),
        ),
      )

      // Dead legacy record holding a timeout for an OLD content hash -> MUST BE DELETED.
      val staleTimeoutPath = legacyRecordPath(staleTimeoutRepo, cacheRoot)
      ServerPaths.writeRecord(
        staleTimeoutPath,
        ServerRecord(
          pid = 999997L,
          port = 1,
          jarVersion = "0.5.0",
          startedAt = 1000L,
          repoRoot = staleTimeoutRepo.toString(),
          clientSecret = "s-secret",
          viewToken = "s-view",
          timeouts =
            listOf(TimeoutEntry("000000000000oldhash", "compile", System.currentTimeMillis())),
          haltedAt = System.currentTimeMillis(),
        ),
      )

      // 25 recent render hash directories for one repo -> keep newest 20, delete oldest 5.
      val now = Instant.now()
      val manyRepoRenders = cacheRoot.resolve("renders/repo-many")
      Files.createDirectories(manyRepoRenders)
      val hashDirs =
        (1..25).map { idx ->
          val dir = manyRepoRenders.resolve("hash%02d".format(idx))
          Files.createDirectories(dir)
          val png = dir.resolve("view.png")
          Files.write(png, byteArrayOf(1, 2, 3))
          val ts = FileTime.from(now.minusSeconds((26 - idx) * 60L))
          Files.setLastModifiedTime(png, ts)
          Files.setLastModifiedTime(dir, ts)
          dir
        }

      // Render hash directories older than 7 days -> deleted even when <= 20 exist.
      val agedRepoRenders = cacheRoot.resolve("renders/repo-aged")
      val freshDir = agedRepoRenders.resolve("freshhash001").also { Files.createDirectories(it) }
      val freshPng = freshDir.resolve("view.png").also { Files.write(it, byteArrayOf(7, 8, 9)) }
      val twoDaysAgo = FileTime.from(now.minus(Duration.ofDays(2)))
      Files.setLastModifiedTime(freshPng, twoDaysAgo)
      Files.setLastModifiedTime(freshDir, twoDaysAgo)

      val oldDir = agedRepoRenders.resolve("oldhash00001").also { Files.createDirectories(it) }
      val oldPng = oldDir.resolve("view.png").also { Files.write(it, byteArrayOf(4, 5, 6)) }
      val eightDaysAgo = FileTime.from(now.minus(Duration.ofDays(8)))
      Files.setLastModifiedTime(oldPng, eightDaysAgo)
      Files.setLastModifiedTime(oldDir, eightDaysAgo)

      // Starting the shared server writes active.json and triggers cleanup.
      startingServer.start()

      val deadline = System.currentTimeMillis() + 5_000L
      while (
        System.currentTimeMillis() < deadline &&
          (Files.exists(deadRecPath) ||
            Files.exists(deadLogPath) ||
            Files.exists(staleTimeoutPath) ||
            Files.exists(oldDir) ||
            hashDirs.take(5).any { Files.exists(it) })
      ) {
        Thread.sleep(50L)
      }

      assertFalse(Files.exists(deadRecPath), "Dead server record should be deleted")
      assertFalse(Files.exists(deadLogPath), "Dead server log should be deleted")
      assertFalse(Files.exists(staleTimeoutPath), "Stale timeout record should be deleted")
      assertTrue(
        Files.exists(ServerPaths.sharedRecordPath(cacheRoot)),
        "Shared active.json server record must be kept",
      )
      assertTrue(
        Files.exists(activeTimeoutPath),
        "Dead record with timeout for repo's current content hash must NEVER be deleted",
      )

      val remainingMany =
        Files.list(manyRepoRenders).use { s -> s.map { it.fileName.toString() }.toList().sorted() }
      assertEquals(20, remainingMany.size, "Expected latest 20 render hash folders kept")
      assertEquals(hashDirs.drop(5).map { it.fileName.toString() }.sorted(), remainingMany)

      assertFalse(Files.exists(oldDir), "Render folder untouched for 8 days should be deleted")
      assertTrue(Files.exists(freshDir), "Render folder touched 2 days ago should be kept")
    } finally {
      startingServer.stop()
    }
  }

  @Test
  fun `handles missing timeout repo, multi-repo shared record timeout, concurrent replacement, child mtime, empty repo dirs, and orphan logs`(
    @TempDir tmp: Path,
  ) {
    val cacheRoot = tmp.resolve("cache")
    val missingRepo = tmp.resolve("gone-repo")
    val repoB = tmp.resolve("repo-b").also { Files.createDirectories(it.resolve("design")) }
    val sourceB = repoB.resolve("design/b.explore.kt").also { Files.writeString(it, "val b = 1\n") }
    val hashB = ContentHasher.snapshot(sourceB).contentHash
    val replacedRepo = tmp.resolve("replaced-repo").also { Files.createDirectories(it) }
    val now = Instant.now()

    // Record with timeouts pointing at a deleted repo -> safe to delete.
    val missingRecPath = legacyRecordPath(missingRepo, cacheRoot)
    ServerPaths.writeRecord(
      missingRecPath,
      ServerRecord(
        pid = 111L,
        port = 1,
        jarVersion = "0.5.0",
        startedAt = 1000L,
        repoRoot = missingRepo.toString(),
        clientSecret = "m-secret",
        viewToken = "m-view",
        timeouts = listOf(TimeoutEntry("deadhash", "compile", now.toEpochMilli())),
      ),
    )

    // Dead shared active.json whose primary repoRoot is missingRepo, but holding an active timeout
    // for repoB -> MUST BE KEPT via TimeoutEntry.repoRoot!
    val sharedRecPath = ServerPaths.sharedRecordPath(cacheRoot)
    ServerPaths.writeRecord(
      sharedRecPath,
      ServerRecord(
        pid = 112L,
        port = 1,
        jarVersion = "0.5.0",
        startedAt = 1000L,
        repoRoot = missingRepo.toString(),
        clientSecret = "shared-secret",
        viewToken = "shared-view",
        timeouts =
          listOf(TimeoutEntry(hashB, "compile", now.toEpochMilli(), repoRoot = repoB.toString())),
      ),
    )

    // Record replaced concurrently while statusProbe is running -> must NOT be deleted.
    val replacedRecPath = legacyRecordPath(replacedRepo, cacheRoot)
    val oldRec =
      ServerRecord(
        pid = 222L,
        port = 50001,
        jarVersion = "0.5.0",
        startedAt = 1000L,
        repoRoot = replacedRepo.toString(),
        clientSecret = "old-secret",
        viewToken = "old-view",
      )
    val freshRec = oldRec.copy(pid = 333L, startedAt = 2000L, clientSecret = "fresh-secret")
    ServerPaths.writeRecord(replacedRecPath, oldRec)

    // Orphaned log older than 60s -> deleted.
    val orphanLog = ServerPaths.serversDir(cacheRoot).resolve("orphan123456.log")
    Files.writeString(orphanLog, "stale log\n")
    Files.setLastModifiedTime(orphanLog, FileTime.from(now.minusSeconds(120L)))

    // Repo render dir with only expired hashes -> whole repo dir removed.
    val emptyAfterCleanRepo = cacheRoot.resolve("renders/repo-all-old")
    val expiredHash =
      emptyAfterCleanRepo.resolve("expired00001").also { Files.createDirectories(it) }
    val expiredPng = expiredHash.resolve("a.png").also { Files.write(it, byteArrayOf(1)) }
    val tenDaysAgo = FileTime.from(now.minus(Duration.ofDays(10)))
    Files.setLastModifiedTime(expiredPng, tenDaysAgo)
    Files.setLastModifiedTime(expiredHash, tenDaysAgo)

    // Hash dir whose directory mtime is 10 days old, but a PNG inside was touched 1 day ago ->
    // kept.
    val childFreshRepo = cacheRoot.resolve("renders/repo-child-fresh")
    val mixedHash = childFreshRepo.resolve("mixed0000001").also { Files.createDirectories(it) }
    val freshPng = mixedHash.resolve("b.png").also { Files.write(it, byteArrayOf(2)) }
    Files.setLastModifiedTime(mixedHash, tenDaysAgo)
    Files.setLastModifiedTime(freshPng, FileTime.from(now.minus(Duration.ofDays(1))))

    val summary =
      CacheCleaner.clean(
        cacheRoot = cacheRoot,
        nowMs = now.toEpochMilli(),
        statusProbe = { rec ->
          if (rec == oldRec) {
            ServerPaths.writeRecord(replacedRecPath, freshRec)
          }
          false
        },
      )

    assertEquals(1, summary.deletedRecords)
    assertEquals(2, summary.keptRecords)
    assertEquals(1, summary.deletedRenderDirs)
    assertFalse(Files.exists(missingRecPath))
    assertTrue(Files.exists(sharedRecPath))
    assertTrue(Files.exists(replacedRecPath))
    assertEquals(freshRec, ServerPaths.readRecord(replacedRecPath))
    assertFalse(Files.exists(orphanLog))
    assertFalse(Files.exists(emptyAfterCleanRepo), "Empty repo render dir should be removed")
    assertTrue(Files.exists(mixedHash), "Hash dir with recently touched child PNG must be kept")
  }

  @Test
  fun `preserves active timeout record when repoRoot is a symbolic link`(@TempDir tmp: Path) {
    val cacheRoot = tmp.resolve("cache")
    val realRepo = tmp.resolve("real-repo")
    Files.createDirectories(realRepo.resolve("design"))
    Files.writeString(realRepo.resolve("DESIGN.md"), "---\ncolors:\n  primary: \"#336699\"\n---\n")
    Files.writeString(realRepo.resolve("design/proposal.explore.kt"), "val loop = 1\n")

    val symlinkRepo = Files.createSymbolicLink(tmp.resolve("linked-repo"), realRepo)
    val logicalSource = symlinkRepo.resolve("design/proposal.explore.kt")
    val activeHash = ContentHasher.snapshot(logicalSource).contentHash

    val recPath = ServerPaths.sharedRecordPath(cacheRoot)
    ServerPaths.writeRecord(
      recPath,
      ServerRecord(
        pid = 444L,
        port = 1,
        jarVersion = "0.5.0",
        startedAt = 1000L,
        repoRoot = symlinkRepo.toAbsolutePath().normalize().toString(),
        clientSecret = "sym-secret",
        viewToken = "sym-view",
        timeouts = listOf(TimeoutEntry(activeHash, "compile", System.currentTimeMillis())),
        haltedAt = System.currentTimeMillis(),
      ),
    )

    val summary = CacheCleaner.clean(cacheRoot = cacheRoot, statusProbe = { false })
    assertEquals(0, summary.deletedRecords)
    assertEquals(1, summary.keptRecords)
    assertTrue(
      Files.exists(recPath),
      "Dead record with active timeout under a symlinked repoRoot must be preserved",
    )
  }

  /** Pre-v6 per-repo record path, used to prove startup cleanup still removes stale v5 records. */
  private fun legacyRecordPath(repoRoot: Path, cacheRoot: Path): Path =
    ServerPaths.serversDir(cacheRoot).resolve("${ServerPaths.repoHash(repoRoot)}.json")
}
