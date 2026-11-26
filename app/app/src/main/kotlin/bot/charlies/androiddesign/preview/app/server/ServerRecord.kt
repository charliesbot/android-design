package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.model.PreviewJson
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermissions
import java.security.MessageDigest
import java.security.SecureRandom
import kotlinx.serialization.Serializable

/** One recorded watchdog timeout for a specific content hash, scope, and optional repository. */
@Serializable
data class TimeoutEntry(
  val contentHash: String,
  val scope: String,
  val recordedAt: Long,
  val reason: String? = null,
  val repoRoot: String? = null,
)

/** Runtime identity of the client or running host server so upgrades/rebuilds replace cleanly. */
data class RuntimeIdentity(
  val backend: String,
  val runtimeHash: String,
  val sourceDir: String?,
  val apkSha256: String?,
  val serverCpFile: Path?,
  val bootstrapDir: Path?,
) {
  companion object {
    const val BACKEND_ANDROID = "android"
    const val BACKEND_LAYOUTLIB = "layoutlib"
    const val BACKEND_ENV = "ANDROID_DESIGN_PREVIEW_BACKEND"
    const val SRC_ENV = "ANDROID_DESIGN_PREVIEW_SRC"
    const val SERVER_CP_ENV = "ANDROID_DESIGN_PREVIEW_SERVER_CP"
    const val WORKER_CP_ENV = "ANDROID_DESIGN_PREVIEW_WORKER_CP"
    const val RUNTIME_HASH_ENV = "ANDROID_DESIGN_PREVIEW_RUNTIME_HASH"

    fun resolveBackend(env: Map<String, String> = System.getenv()): String =
      env[BACKEND_ENV]?.trim()?.lowercase()?.takeIf { it.isNotBlank() } ?: BACKEND_ANDROID

    fun compute(
      env: Map<String, String> = System.getenv(),
      meta: BootstrapMeta? =
        env[BootstrapMeta.BOOTSTRAP_DIR_ENV]
          ?.takeIf { it.isNotBlank() }
          ?.let { dir ->
            runCatching {
              BootstrapMeta.fromFile(Path.of(dir).resolve("bootstrap-meta.properties"))
            }
              .getOrNull()
          } ?: BootstrapMeta.locateOrNull(env),
      version: String = AppInfo.VERSION,
    ): RuntimeIdentity {
      val backend = resolveBackend(env)
      val sourceDir = env[SRC_ENV]?.trim()?.takeIf { it.isNotBlank() }
      val bootstrapDir =
        env[BootstrapMeta.BOOTSTRAP_DIR_ENV]?.trim()?.takeIf { it.isNotBlank() }?.let(Path::of)
          ?: meta?.bootstrapDir
      val serverCpFile =
        env[SERVER_CP_ENV]?.trim()?.takeIf { it.isNotBlank() }?.let(Path::of)
          ?: bootstrapDir?.resolve("server.cp")?.takeIf { Files.isRegularFile(it) }
      val workerCpFile =
        env[WORKER_CP_ENV]?.trim()?.takeIf { it.isNotBlank() }?.let(Path::of)
          ?: bootstrapDir?.resolve("worker.cp")?.takeIf { Files.isRegularFile(it) }
      val apkSha =
        if (backend == BACKEND_LAYOUTLIB) {
          null
        } else {
          runCatching { meta?.apkSha256OrCompute() }.getOrNull()
        }

      val overrideHash = env[RUNTIME_HASH_ENV]?.trim()?.takeIf { it.isNotBlank() }
      val runtimeHash =
        overrideHash
          ?: buildString {
            val digest = MessageDigest.getInstance("SHA-256")
            fun feed(s: String) {
              digest.update(s.toByteArray(Charsets.UTF_8))
              digest.update(0)
            }
            fun feedClasspath(tag: String, cpFile: Path?) {
              if (cpFile == null || !Files.isRegularFile(cpFile)) return
              val cpText = runCatching { Files.readString(cpFile) }.getOrDefault("")
              feed("$tag:$cpText")
              for (entry in cpText.lines().drop(1).joinToString("").split(File.pathSeparator)) {
                val p = runCatching { Path.of(entry.trim()) }.getOrNull() ?: continue
                if (p.fileName?.toString()?.endsWith(".jar") == true && Files.exists(p)) {
                  val mtime = runCatching {
                    Files.getLastModifiedTime(p).toMillis()
                  }
                    .getOrDefault(0L)
                  feed("$tag-entry:$p:$mtime")
                }
              }
            }
            feed("ver:$version")
            feed("backend:$backend")
            feed("src:${sourceDir.orEmpty()}")
            if (apkSha != null) {
              feed("apk:$apkSha")
            }
            feedClasspath("cp", serverCpFile)
            if (backend == BACKEND_LAYOUTLIB) {
              feedClasspath("worker-cp", workerCpFile)
            }
            append(digest.digest().joinToString("") { "%02x".format(it) }.take(24))
          }

      return RuntimeIdentity(
        backend = backend,
        runtimeHash = runtimeHash,
        sourceDir = sourceDir,
        apkSha256 = apkSha,
        serverCpFile = serverCpFile,
        bootstrapDir = bootstrapDir,
      )
    }
  }
}

/**
 * On-disk discovery record for the single shared preview host server, stored with `0600`
 * permissions under `~/.cache/android-design-preview/servers/active.json`.
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
  /** Shared runtime version/checkout hash, backend mode, and active Android device metadata. */
  val runtimeHash: String? = null,
  val sourceDir: String? = null,
  val backend: String = RuntimeIdentity.BACKEND_ANDROID,
  val deviceSerial: String? = null,
  val adbForwardPort: Int? = null,
  val apkSha256: String? = null,
  val replacedByVersion: String? = null,
  val ownedEmulatorSerial: String? = null,
) {
  fun boardUrl(repoId: String? = null): String =
    if (repoId.isNullOrBlank()) {
      "http://127.0.0.1:$port/?view=$viewToken"
    } else {
      "http://127.0.0.1:$port/?repo=$repoId&view=$viewToken"
    }

  fun hasTimeout(contentHash: String, scope: CheckScope): Boolean = timeouts.any { entry ->
    if (entry.contentHash != contentHash) return@any false
    val scopeKey = scope.key
    if (
      entry.scope == scopeKey ||
        entry.scope == "compile" ||
        entry.scope == "full" ||
        scopeKey == "full"
    ) {
      return@any true
    }
    // Match RenderKey.scopeLabel() format: "<dir>/<screen>[+<overlay>]/<view>@<density>x"
    val base = entry.scope.substringBefore('@')
    val parts = base.split('/')
    if (parts.size == 3) {
      val (eDir, eScrRaw, eView) = parts
      val eScr = eScrRaw.substringBefore('+')
      val dirOk = scope.direction.isNullOrBlank() || eDir == scope.direction
      val scrOk = scope.screen.isNullOrBlank() || eScr == scope.screen
      val viewOk = scope.view.isNullOrBlank() || eView == scope.view
      dirOk && scrOk && viewOk
    } else {
      false
    }
  }
}

/** Deterministic cache paths, repo hashing, port selection, and atomic 0600 record persistence. */
object ServerPaths {
  const val CACHE_DIR_ENV = "ANDROID_DESIGN_CACHE_DIR"
  const val SHARED_PREFERRED_PORT = 49210
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

  fun serversDir(cacheRoot: Path = cacheRoot()): Path = cacheRoot.resolve("servers")

  /** Single shared active server discovery record across all repositories. */
  fun sharedRecordPath(cacheRoot: Path = cacheRoot()): Path =
    serversDir(cacheRoot).resolve("active.json")

  fun sharedLockPath(cacheRoot: Path = cacheRoot()): Path =
    serversDir(cacheRoot).resolve("active.lock")

  fun sharedLogPath(cacheRoot: Path = cacheRoot()): Path =
    serversDir(cacheRoot).resolve("active.log")

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
      path.toFile().setReadable(false, false)
      path.toFile().setReadable(true, true)
      path.toFile().setWritable(false, false)
      path.toFile().setWritable(true, true)
    } catch (_: Exception) {
      // Best-effort permissions.
    }
  }
}
