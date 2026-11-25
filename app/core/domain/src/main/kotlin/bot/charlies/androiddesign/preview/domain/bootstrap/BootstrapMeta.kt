package bot.charlies.androiddesign.preview.domain.bootstrap

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.Properties

/**
 * Resolved paths from `:host:exportBootstrap` (`bootstrap-meta.properties`), shared between the
 * host preview server/CLI and `:host` tests.
 */
data class BootstrapMeta(
  val bootstrapDir: Path,
  val androidJar: Path,
  val bundleJar: Path,
  val d8Jar: Path,
  val composeCompilerPluginJar: Path,
  val apkPath: Path,
  val apkSha256: String? = null,
  val workerCp: Path,
  val layoutlibRuntimeDir: Path,
  val layoutlibResourcesDir: Path,
  val workerResDirsFile: Path,
  val workerAssetsDir: Path,
  val workerRJar: Path,
) {
  /** Returns [apkSha256] when recorded in metadata, or computes the APK's SHA-256 digest. */
  fun apkSha256OrCompute(): String = apkSha256?.takeIf { it.isNotBlank() } ?: computeSha256(apkPath)

  companion object {
    const val BOOTSTRAP_DIR_ENV = "ANDROID_DESIGN_PREVIEW_BOOTSTRAP_DIR"
    const val BOOTSTRAP_DIR_PROP = "adp.bootstrap.dir"

    fun locate(
      env: Map<String, String> = System.getenv(),
      sysProps: Properties = System.getProperties(),
    ): BootstrapMeta =
      locateOrNull(env, sysProps)
        ?: error(
          "Cannot find bootstrap-meta.properties; run `./gradlew :host:exportBootstrap` from app/ " +
            "or set $BOOTSTRAP_DIR_ENV (searched: ${candidatePaths(env, sysProps).joinToString()})",
        )

    fun locateOrNull(
      env: Map<String, String> = System.getenv(),
      sysProps: Properties = System.getProperties(),
    ): BootstrapMeta? {
      val metaFile =
        candidatePaths(env, sysProps).firstOrNull { Files.isRegularFile(it) } ?: return null
      return runCatching { fromFile(metaFile) }.getOrNull()
    }

    private fun candidatePaths(env: Map<String, String>, sysProps: Properties): List<Path> {
      val explicitProp =
        sysProps.getProperty(BOOTSTRAP_DIR_PROP)?.trim()?.takeIf { it.isNotEmpty() }
      val explicitEnv = env[BOOTSTRAP_DIR_ENV]?.trim()?.takeIf { it.isNotEmpty() }
      val cpBootstrapDir =
        sysProps
          .getProperty("java.class.path")
          .orEmpty()
          .split(File.pathSeparator)
          .firstNotNullOfOrNull { entry ->
            val p = runCatching { Paths.get(entry).toAbsolutePath().normalize() }.getOrNull()
            when {
              p == null -> null
              Files.isDirectory(p) ->
                p.resolve("bootstrap-meta.properties").takeIf { Files.isRegularFile(it) }
              else -> null
            }
          }
      return buildList {
        if (explicitProp != null) {
          add(Paths.get(explicitProp).resolve("bootstrap-meta.properties"))
        }
        if (explicitEnv != null) {
          add(Paths.get(explicitEnv).resolve("bootstrap-meta.properties"))
        }
        if (cpBootstrapDir != null) {
          add(cpBootstrapDir)
        }
        add(Paths.get("build/bootstrap/bootstrap-meta.properties"))
        add(Paths.get("host/build/bootstrap/bootstrap-meta.properties"))
        add(Paths.get("app/host/build/bootstrap/bootstrap-meta.properties"))
      }
    }

    fun fromFile(metaFile: Path): BootstrapMeta {
      val props = Properties()
      Files.newBufferedReader(metaFile).use { props.load(it) }
      fun reqPath(key: String): Path {
        val raw =
          requireNotNull(props.getProperty(key)?.trim()?.takeIf { it.isNotEmpty() }) {
            "Missing '$key' in $metaFile"
          }
        val path = Paths.get(raw).toAbsolutePath().normalize()
        require(Files.exists(path)) { "Bootstrap entry '$key' missing on disk: $path" }
        return path
      }
      return BootstrapMeta(
        bootstrapDir = metaFile.toAbsolutePath().normalize().parent,
        androidJar = reqPath("androidJar"),
        bundleJar = reqPath("bundleJar"),
        d8Jar = reqPath("d8Jar"),
        composeCompilerPluginJar = reqPath("composeCompilerPluginJar"),
        apkPath = reqPath("apkPath"),
        apkSha256 = props.getProperty("apkSha256")?.trim()?.takeIf { it.isNotEmpty() },
        workerCp = reqPath("workerCp"),
        layoutlibRuntimeDir = reqPath("layoutlibRuntimeDir"),
        layoutlibResourcesDir = reqPath("layoutlibResourcesDir"),
        workerResDirsFile = reqPath("workerResDirsFile"),
        workerAssetsDir = reqPath("workerAssetsDir"),
        workerRJar = reqPath("workerRJar"),
      )
    }

    fun computeSha256(path: Path): String {
      val digest = MessageDigest.getInstance("SHA-256")
      Files.newInputStream(path).use { input ->
        val buf = ByteArray(8192)
        while (true) {
          val n = input.read(buf)
          if (n <= 0) break
          digest.update(buf, 0, n)
        }
      }
      return digest.digest().joinToString("") { "%02x".format(it) }
    }
  }
}
