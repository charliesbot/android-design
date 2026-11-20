package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.model.PreviewJson
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermissions
import java.security.MessageDigest
import java.security.SecureRandom
import kotlinx.serialization.Serializable

/** One recorded watchdog timeout for a specific content hash and scope. */
@Serializable
data class TimeoutEntry(
  val contentHash: String,
  val scope: String,
  val recordedAt: Long,
  val reason: String? = null,
)

/**
 * On-disk discovery record for one repository's preview server, stored with `0600` permissions
 * under `~/.cache/android-design-preview/servers/<repo-hash>.json`.
 */
@Serializable
data class ServerRecord(
  val pid: Long,
  val port: Int,
  val jarVersion: String,
  val startedAt: Long,
  val repoRoot: String,
  val clientSecret: String,
  val viewToken: String,
  val timeouts: List<TimeoutEntry> = emptyList(),
  val haltedAt: Long? = null,
) {
  fun boardUrl(): String = "http://127.0.0.1:$port/?view=$viewToken"

  fun hasTimeout(
    contentHash: String,
    scope: String,
    direction: String? = null,
    screen: String? = null,
    view: String? = null,
  ): Boolean = timeouts.any { entry ->
    if (entry.contentHash != contentHash) return@any false
    if (
      entry.scope == scope || entry.scope == "compile" || entry.scope == "full" || scope == "full"
    ) {
      return@any true
    }
    // Match RenderKey.scopeLabel() format: "<dir>/<screen>[+<overlay>]/<view>@<density>x"
    val base = entry.scope.substringBefore('@')
    val parts = base.split('/')
    if (parts.size == 3) {
      val (eDir, eScrRaw, eView) = parts
      val eScr = eScrRaw.substringBefore('+')
      val dirOk = direction.isNullOrBlank() || eDir == direction
      val scrOk = screen.isNullOrBlank() || eScr == screen
      val viewOk = view.isNullOrBlank() || eView == view
      dirOk && scrOk && viewOk
    } else {
      false
    }
  }
}

/** Deterministic cache paths, repo hashing, port selection, and atomic 0600 record persistence. */
object ServerPaths {
  const val CACHE_DIR_ENV = "ANDROID_DESIGN_CACHE_DIR"
  private const val PORT_RANGE_START = 49200
  private const val PORT_RANGE_SIZE = 10000
  private val rng = SecureRandom()
  private val recordLock = Any()

  fun cacheRoot(env: Map<String, String> = System.getenv()): Path {
    val override = env[CACHE_DIR_ENV]?.takeIf { it.isNotBlank() }
    if (override != null) return Path.of(override).toAbsolutePath().normalize()
    return Path.of(System.getProperty("user.home"), ".cache", "android-design-preview")
      .toAbsolutePath()
      .normalize()
  }

  /**
   * Derives the repository root from [path] by walking upward for `.git`. If no `.git` directory or
   * file is found, uses [path] when it is a directory or its parent directory when it is a file.
   */
  fun resolveRepoRoot(path: Path): Path {
    val normalized = path.toAbsolutePath().normalize()
    val startDir =
      when {
        Files.isDirectory(normalized) -> normalized
        normalized.parent != null -> normalized.parent
        else -> normalized
      }
    var cursor: Path? = startDir
    while (cursor != null) {
      if (Files.exists(cursor.resolve(".git"))) return cursor
      cursor = cursor.parent
    }
    return startDir
  }

  fun repoHash(repoRoot: Path): String {
    val canonical = repoRoot.toAbsolutePath().normalize().toString()
    val digest = MessageDigest.getInstance("SHA-256").digest(canonical.toByteArray(Charsets.UTF_8))
    return digest.take(8).joinToString("") { "%02x".format(it) }
  }

  fun preferredPort(repoRoot: Path): Int {
    val hash = repoHash(repoRoot)
    val unsigned = hash.substring(0, 8).toLong(16)
    return PORT_RANGE_START + (unsigned % PORT_RANGE_SIZE).toInt()
  }

  fun serversDir(cacheRoot: Path = cacheRoot()): Path = cacheRoot.resolve("servers")

  fun recordPath(repoRoot: Path, cacheRoot: Path = cacheRoot()): Path =
    serversDir(cacheRoot).resolve("${repoHash(repoRoot)}.json")

  fun rendersDir(repoRoot: Path, cacheRoot: Path = cacheRoot()): Path =
    cacheRoot.resolve("renders").resolve(repoHash(repoRoot))

  fun randomToken(): String {
    val bytes = ByteArray(32)
    rng.nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
  }

  fun readRecord(path: Path): ServerRecord? {
    if (!Files.isRegularFile(path)) return null
    return try {
      PreviewJson.compact.decodeFromString(ServerRecord.serializer(), Files.readString(path))
    } catch (_: Exception) {
      null
    }
  }

  fun writeRecord(path: Path, record: ServerRecord) {
    synchronized(recordLock) {
      val parent = path.parent
      if (parent != null) Files.createDirectories(parent)
      val tmp = Files.createTempFile(parent ?: Path.of("."), "server-record-", ".tmp")
      try {
        setOwnerOnlyPermissions(tmp)
        Files.writeString(
          tmp,
          PreviewJson.pretty.encodeToString(ServerRecord.serializer(), record) + "\n",
        )
        setOwnerOnlyPermissions(tmp)
        try {
          Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: Exception) {
          Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING)
        }
        setOwnerOnlyPermissions(path)
      } finally {
        Files.deleteIfExists(tmp)
      }
    }
  }

  fun updateRecord(path: Path, transform: (ServerRecord) -> ServerRecord): ServerRecord? {
    synchronized(recordLock) {
      val current = readRecord(path) ?: return null
      val updated = transform(current)
      writeRecord(path, updated)
      return updated
    }
  }

  /**
   * Deletes [path] only if its current on-disk contents still equal [expected], preventing a
   * cleanup pass from removing a record that another process just replaced.
   */
  fun deleteRecordIfMatches(path: Path, expected: ServerRecord): Boolean {
    synchronized(recordLock) {
      val current = readRecord(path) ?: return false
      if (current != expected) return false
      return try {
        Files.deleteIfExists(path)
      } catch (_: Exception) {
        false
      }
    }
  }

  private fun setOwnerOnlyPermissions(path: Path) {
    try {
      Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rw-------"))
    } catch (_: UnsupportedOperationException) {
      // Non-POSIX filesystem, such as a FAT or network volume.
      path.toFile().setReadable(false, false)
      path.toFile().setReadable(true, true)
      path.toFile().setWritable(false, false)
      path.toFile().setWritable(true, true)
    } catch (_: Exception) {
      // Best-effort permissions.
    }
  }
}
