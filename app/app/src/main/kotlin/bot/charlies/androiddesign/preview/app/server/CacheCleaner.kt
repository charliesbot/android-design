package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.model.PreviewJson
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.time.Duration

/**
 * Cleans stale discovery records and old render directories under
 * `~/.cache/android-design-preview/`.
 *
 * On server startup:
 * - Deletes server records under `servers/` whose server does not answer `/api/status`, EXCEPT
 *   records holding `timeouts` that match any `.explore.kt` file's current content hash in that
 *   record's repository (so watchdog fast-fail state is never lost).
 * - Deletes content-hash directories under `renders/<repo-hash>/` older than the newest
 *   [DEFAULT_MAX_HASHES_PER_REPO] (20) per repo, or untouched for [DEFAULT_MAX_RENDER_AGE_DAYS] (7)
 *   days.
 */
object CacheCleaner {
  const val DEFAULT_MAX_HASHES_PER_REPO = 20
  const val DEFAULT_MAX_RENDER_AGE_DAYS = 7L
  private const val MAX_EXPLORE_SCAN_DEPTH = 6
  private const val PROBE_CONNECT_TIMEOUT_MS = 300
  private const val PROBE_READ_TIMEOUT_MS = 500
  private const val ORPHAN_LOG_GRACE_MS = 60_000L

  private val SKIP_DIRS =
    setOf(".git", "build", ".gradle", "node_modules", ".idea", ".cache", "target", "out")

  data class CleanupSummary(
    val deletedRecords: Int,
    val keptRecords: Int,
    val deletedRenderDirs: Int,
  )

  fun clean(
    cacheRoot: Path,
    currentRecordPath: Path? = null,
    nowMs: Long = System.currentTimeMillis(),
    maxHashesPerRepo: Int = DEFAULT_MAX_HASHES_PER_REPO,
    maxRenderAgeMs: Long = Duration.ofDays(DEFAULT_MAX_RENDER_AGE_DAYS).toMillis(),
    statusProbe: (ServerRecord) -> Boolean = ::probeServerStatus,
  ): CleanupSummary {
    val (deletedRecords, keptRecords) =
      cleanServerRecords(cacheRoot, currentRecordPath, nowMs, statusProbe)
    val deletedRenderDirs = cleanRenders(cacheRoot, nowMs, maxHashesPerRepo, maxRenderAgeMs)
    return CleanupSummary(
      deletedRecords = deletedRecords,
      keptRecords = keptRecords,
      deletedRenderDirs = deletedRenderDirs,
    )
  }

  private fun cleanServerRecords(
    cacheRoot: Path,
    currentRecordPath: Path?,
    nowMs: Long,
    statusProbe: (ServerRecord) -> Boolean,
  ): Pair<Int, Int> {
    val serversDir = ServerPaths.serversDir(cacheRoot)
    if (!Files.isDirectory(serversDir)) return 0 to 0

    val normCurrent = currentRecordPath?.toAbsolutePath()?.normalize()
    val currentStem = normCurrent?.fileName?.toString()?.removeSuffix(".json")
    var deleted = 0
    var kept = 0

    val jsonFiles =
      try {
        Files.list(serversDir).use { stream ->
          stream
            .filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".json") }
            .toList()
        }
      } catch (_: Exception) {
        return 0 to 0
      }

    for (path in jsonFiles) {
      val norm = path.toAbsolutePath().normalize()
      if (normCurrent != null && norm == normCurrent) {
        kept++
        continue
      }
      val record = ServerPaths.readRecord(norm)
      if (record == null) {
        // Corrupt record file: remove it and its paired log.
        if (deleteFileQuietly(norm)) {
          deleted++
          deletePairedLog(norm)
        }
        continue
      }
      if (statusProbe(record)) {
        kept++
        continue
      }
      if (hasActiveTimeout(record)) {
        kept++
        continue
      }
      if (ServerPaths.deleteRecordIfMatches(norm, record)) {
        deleted++
        deletePairedLog(norm)
      } else {
        kept++
      }
    }

    // Remove orphaned `.log` files whose matching `.json` record does not exist.
    try {
      Files.list(serversDir).use { stream ->
        stream
          .filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".log") }
          .forEach { logPath ->
            val stem = logPath.fileName.toString().removeSuffix(".log")
            if (stem == currentStem) return@forEach
            val matchingJson = serversDir.resolve("$stem.json")
            if (!Files.exists(matchingJson)) {
              val mtimeMs =
                try {
                  Files.getLastModifiedTime(logPath).toMillis()
                } catch (_: Exception) {
                  0L
                }
              if (nowMs - mtimeMs >= ORPHAN_LOG_GRACE_MS) {
                deleteFileQuietly(logPath)
              }
            }
          }
      }
    } catch (_: Exception) {
      // Best-effort log cleanup.
    }

    return deleted to kept
  }

  private fun cleanRenders(
    cacheRoot: Path,
    nowMs: Long,
    maxHashesPerRepo: Int,
    maxRenderAgeMs: Long,
  ): Int {
    val rendersRoot = cacheRoot.resolve("renders")
    if (!Files.isDirectory(rendersRoot)) return 0
    val cutoffMs = nowMs - maxRenderAgeMs
    var deletedDirs = 0

    val repoDirs =
      try {
        Files.list(rendersRoot).use { stream ->
          stream.filter { Files.isDirectory(it) && !Files.isSymbolicLink(it) }.toList()
        }
      } catch (_: Exception) {
        return 0
      }

    for (repoDir in repoDirs) {
      val hashDirs =
        try {
          Files.list(repoDir).use { stream ->
            stream.filter { Files.isDirectory(it) && !Files.isSymbolicLink(it) }.toList()
          }
        } catch (_: Exception) {
          continue
        }
      val ranked =
        hashDirs
          .map { dir -> dir to lastTouchedMs(dir) }
          .sortedWith(
            compareByDescending<Pair<Path, Long>> { it.second }
              .thenByDescending { it.first.fileName.toString() },
          )
      for ((index, entry) in ranked.withIndex()) {
        val (hashDir, touchedMs) = entry
        if (index >= maxHashesPerRepo || touchedMs < cutoffMs) {
          if (deleteRecursively(hashDir)) {
            deletedDirs++
          }
        }
      }
      // Remove empty repo render directories after pruning.
      try {
        val isEmpty = Files.list(repoDir).use { stream -> !stream.findFirst().isPresent }
        if (isEmpty) {
          Files.deleteIfExists(repoDir)
        }
      } catch (_: Exception) {
        // Ignore.
      }
    }
    return deletedDirs
  }

  internal fun hasActiveTimeout(record: ServerRecord): Boolean {
    if (record.timeouts.isEmpty()) return false
    val repoRoot =
      try {
        Path.of(record.repoRoot)
      } catch (_: Exception) {
        return false
      }
    if (!Files.isDirectory(repoRoot)) return false

    val recordedHashes = record.timeouts.mapTo(HashSet()) { it.contentHash }
    val versionsToCheck =
      if (record.jarVersion == AppInfo.VERSION) listOf(record.jarVersion)
      else listOf(record.jarVersion, AppInfo.VERSION)
    var matched = false

    try {
      Files.walkFileTree(
        repoRoot,
        emptySet(),
        MAX_EXPLORE_SCAN_DEPTH,
        object : SimpleFileVisitor<Path>() {
          override fun preVisitDirectory(
            dir: Path,
            attrs: BasicFileAttributes,
          ): FileVisitResult {
            if (dir != repoRoot) {
              if (attrs.isSymbolicLink) return FileVisitResult.SKIP_SUBTREE
              val name = dir.fileName?.toString().orEmpty()
              if (name in SKIP_DIRS) return FileVisitResult.SKIP_SUBTREE
            }
            return FileVisitResult.CONTINUE
          }

          override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            if (!attrs.isRegularFile || !file.fileName.toString().endsWith(".explore.kt")) {
              return FileVisitResult.CONTINUE
            }
            for (ver in versionsToCheck) {
              val hash =
                try {
                  ContentHasher.snapshot(file, jarVersion = ver).contentHash
                } catch (_: Exception) {
                  continue
                }
              if (hash in recordedHashes) {
                matched = true
                return FileVisitResult.TERMINATE
              }
            }
            return FileVisitResult.CONTINUE
          }

          override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult =
            FileVisitResult.CONTINUE
        },
      )
    } catch (_: Exception) {
      // Conservative fallback: if scanning fails unexpectedly while timeouts exist, preserve
      // record.
      return true
    }
    return matched
  }

  internal fun probeServerStatus(record: ServerRecord): Boolean {
    if (record.port !in 1..65535) return false
    return try {
      val conn =
        URI("http://127.0.0.1:${record.port}/api/status").toURL().openConnection()
          as HttpURLConnection
      conn.requestMethod = "GET"
      conn.connectTimeout = PROBE_CONNECT_TIMEOUT_MS
      conn.readTimeout = PROBE_READ_TIMEOUT_MS
      conn.useCaches = false
      conn.setRequestProperty("Host", "127.0.0.1:${record.port}")
      conn.setRequestProperty("X-Client-Secret", record.clientSecret)
      try {
        if (conn.responseCode != 200) return false
        val body = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val status = PreviewJson.compact.decodeFromString(StatusResponse.serializer(), body)
        status.ok && status.port == record.port
      } finally {
        conn.disconnect()
      }
    } catch (_: Exception) {
      false
    }
  }

  private fun lastTouchedMs(dir: Path): Long {
    var latest =
      try {
        Files.getLastModifiedTime(dir).toMillis()
      } catch (_: Exception) {
        0L
      }
    try {
      Files.list(dir).use { stream ->
        stream.forEach { child ->
          val childTime =
            try {
              Files.getLastModifiedTime(child).toMillis()
            } catch (_: Exception) {
              0L
            }
          if (childTime > latest) latest = childTime
        }
      }
    } catch (_: Exception) {
      // Fall back to directory mtime.
    }
    return latest
  }

  private fun deletePairedLog(recordPath: Path) {
    val name = recordPath.fileName?.toString() ?: return
    if (!name.endsWith(".json")) return
    val logPath = recordPath.resolveSibling(name.removeSuffix(".json") + ".log")
    deleteFileQuietly(logPath)
  }

  private fun deleteFileQuietly(path: Path): Boolean =
    try {
      Files.deleteIfExists(path)
    } catch (_: Exception) {
      false
    }

  private fun deleteRecursively(root: Path): Boolean {
    if (!Files.exists(root)) return false
    return try {
      Files.walkFileTree(
        root,
        object : SimpleFileVisitor<Path>() {
          override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            Files.deleteIfExists(file)
            return FileVisitResult.CONTINUE
          }

          override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
            Files.deleteIfExists(dir)
            return FileVisitResult.CONTINUE
          }
        },
      )
      true
    } catch (_: Exception) {
      false
    }
  }
}
