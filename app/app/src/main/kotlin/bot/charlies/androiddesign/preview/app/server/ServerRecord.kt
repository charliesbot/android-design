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
  val jarPath: Path?,
  val jarModified: Long?,
  val serverCpFile: Path?,
  val bootstrapDir: Path?,
  val robolectricCpFile: Path? = null,
) {
  companion object {
    const val BACKEND_SKIKO = "skiko"
    const val BACKEND_ANDROID = "android"
    const val BACKEND_ROBOLECTRIC = "robolectric"
    const val BACKEND_ENV = "ANDROID_DESIGN_PREVIEW_BACKEND"
    const val SRC_ENV = "ANDROID_DESIGN_PREVIEW_SRC"
    const val SERVER_CP_ENV = "ANDROID_DESIGN_PREVIEW_SERVER_CP"
    const val ROBOLECTRIC_CP_ENV = "ANDROID_DESIGN_PREVIEW_ROBOLECTRIC_CP"
    const val RUNTIME_HASH_ENV = "ANDROID_DESIGN_PREVIEW_RUNTIME_HASH"

    fun resolveBackend(env: Map<String, String> = System.getenv()): String {
      val explicit = env[BACKEND_ENV]?.trim()?.lowercase()
      if (
        explicit == BACKEND_ANDROID || explicit == BACKEND_ROBOLECTRIC || explicit == BACKEND_SKIKO
      ) {
        return explicit
      }
      val hasBootstrap =
        !env[BootstrapMeta.BOOTSTRAP_DIR_ENV].isNullOrBlank() || !env[SERVER_CP_ENV].isNullOrBlank()
      val hasLegacyJar = !env["ANDROID_DESIGN_PREVIEW_JAR"].isNullOrBlank()
      return when {
        hasLegacyJar && !hasBootstrap -> BACKEND_SKIKO
        hasBootstrap -> BACKEND_ANDROID
        ServerPaths.runningJar() != null -> BACKEND_SKIKO
        else -> BACKEND_SKIKO
      }
    }

    fun compute(
      env: Map<String, String> = System.getenv(),
      jar: Path? = ServerPaths.runningJar(),
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
      val robolectricCpFile =
        env[ROBOLECTRIC_CP_ENV]?.trim()?.takeIf { it.isNotBlank() }?.let(Path::of)
          ?: bootstrapDir?.resolve("robolectric.cp")?.takeIf { Files.isRegularFile(it) }
      val jarMod = jar?.let(ServerPaths::jarModified)
      val apkSha =
        if (backend == BACKEND_ANDROID) runCatching { meta?.apkSha256OrCompute() }.getOrNull()
        else null

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
            if (jar != null) {
              feed("jar:$jar:${jarMod ?: -1L}")
            }
            if (apkSha != null) {
              feed("apk:$apkSha")
            }
            feedClasspath("cp", serverCpFile)
            if (backend == BACKEND_ROBOLECTRIC) {
              feedClasspath("rcp", robolectricCpFile)
            }
            append(digest.digest().joinToString("") { "%02x".format(it) }.take(24))
          }

      return RuntimeIdentity(
        backend = backend,
        runtimeHash = runtimeHash,
        sourceDir = sourceDir,
        apkSha256 = apkSha,
        jarPath = jar,
        jarModified = jarMod,
        serverCpFile = serverCpFile,
        bootstrapDir = bootstrapDir,
        robolectricCpFile = robolectricCpFile,
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
  /** The JAR this server runs and its modification time (v5 JAR mode). */
  val jarPath: String? = null,
  val jarModified: Long? = null,
  /** Shared runtime version/checkout hash, backend mode, and active Android device metadata. */
  val runtimeHash: String? = null,
  val sourceDir: String? = null,
  val backend: String = RuntimeIdentity.BACKEND_SKIKO,
  val deviceSerial: String? = null,
  val adbForwardPort: Int? = null,
  val apkSha256: String? = null,
  val replacedByVersion: String? = null,
) {
  fun boardUrl(repoId: String? = null): String =
    if (repoId.isNullOrBlank()) {
      "http://127.0.0.1:$port/?view=$viewToken"
    } else {
      "http://127.0.0.1:$port/?repo=$repoId&view=$viewToken"
    }

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
  const val SHARED_PREFERRED_PORT = 49210
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

  /** Single shared active server discovery record across all repositories. */
  fun sharedRecordPath(cacheRoot: Path = cacheRoot()): Path =
    serversDir(cacheRoot).resolve("active.json")

  fun sharedLockPath(cacheRoot: Path = cacheRoot()): Path =
    serversDir(cacheRoot).resolve("active.lock")

  fun sharedLogPath(cacheRoot: Path = cacheRoot()): Path =
    serversDir(cacheRoot).resolve("active.log")

  /** Legacy per-repo record path (retained for startup cleanup of stale v5 records). */
  fun legacyRecordPath(repoRoot: Path, cacheRoot: Path = cacheRoot()): Path =
    serversDir(cacheRoot).resolve("${repoHash(repoRoot)}.json")

  fun recordPath(repoRoot: Path, cacheRoot: Path = cacheRoot()): Path = sharedRecordPath(cacheRoot)

  fun rendersDir(repoRoot: Path, cacheRoot: Path = cacheRoot()): Path =
    cacheRoot.resolve("renders").resolve(repoHash(repoRoot))

  /**
   * The real path of the JAR this process runs from, or null when it runs from class directories
   * (tests, IDE). Real paths keep a symlinked directory (macOS `/var`) from looking like another
   * JAR.
   */
  fun runningJar(): Path? =
    try {
      val location = ServerPaths::class.java.protectionDomain?.codeSource?.location?.toURI()
      location
        ?.let(Path::of)
        ?.takeIf { Files.isRegularFile(it) && it.toString().endsWith(".jar") }
        ?.toRealPath()
    } catch (_: Exception) {
      null
    }

  /** [jar]'s modification time in milliseconds, or null when it cannot be read. */
  fun jarModified(jar: Path): Long? =
    try {
      Files.getLastModifiedTime(jar).toMillis()
    } catch (_: Exception) {
      null
    }

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
