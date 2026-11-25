package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.data.DesignMdLocator
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

/**
 * Computes the shared content hash (`source + DESIGN.md + design/assets + JAR version`) that keys
 * compiled artifacts, cached renders, watchdog timeouts, and click selections.
 */
object ContentHasher {
  private const val MAX_ASSET_FILE_BYTES = 16L * 1024L * 1024L

  /** One pre-read asset file under `assets/`, captured during [snapshot] to avoid a second walk. */
  data class AssetEntry(
    val relativePath: String,
    val sha256: String,
    val bytes: ByteArray,
  ) {
    override fun equals(other: Any?): Boolean =
      this === other ||
        (other is AssetEntry &&
          relativePath == other.relativePath &&
          sha256 == other.sha256 &&
          bytes.contentEquals(other.bytes))

    override fun hashCode(): Int {
      var result = relativePath.hashCode()
      result = 31 * result + sha256.hashCode()
      result = 31 * result + bytes.contentHashCode()
      return result
    }
  }

  data class Snapshot(
    val sourcePath: Path,
    val designPath: Path?,
    val contentHash: String,
    val sourceBytes: ByteArray? = null,
    val assets: List<AssetEntry>? = null,
  ) {
    override fun equals(other: Any?): Boolean =
      this === other ||
        (other is Snapshot &&
          sourcePath == other.sourcePath &&
          designPath == other.designPath &&
          contentHash == other.contentHash)

    override fun hashCode(): Int {
      var result = sourcePath.hashCode()
      result = 31 * result + (designPath?.hashCode() ?: 0)
      result = 31 * result + contentHash.hashCode()
      return result
    }
  }

  fun snapshot(
    sourcePath: Path,
    explicitDesign: Path? = null,
    jarVersion: String = AppInfo.VERSION,
  ): Snapshot {
    val normSource = sourcePath.toAbsolutePath().normalize()
    val normDesign =
      explicitDesign?.toAbsolutePath()?.normalize() ?: DesignMdLocator.find(normSource)
    val digest = MessageDigest.getInstance("SHA-256")
    digest.update("jar:".toByteArray(Charsets.UTF_8))
    digest.update(jarVersion.toByteArray(Charsets.UTF_8))
    digest.update(0)
    digest.update("source:".toByteArray(Charsets.UTF_8))
    val sourceExists = Files.isRegularFile(normSource)
    val sourceBytes =
      if (sourceExists) {
        Files.readAllBytes(normSource).also { digest.update(it) }
      } else {
        null
      }
    digest.update(0)
    digest.update("design:".toByteArray(Charsets.UTF_8))
    if (normDesign != null && Files.isRegularFile(normDesign)) {
      digest.update(normDesign.toString().toByteArray(Charsets.UTF_8))
      digest.update(0)
      digest.update(Files.readAllBytes(normDesign))
    }
    val collectedAssets = if (sourceExists) mutableListOf<AssetEntry>() else null
    val assetsDir = normSource.parent?.resolve("assets")
    if (assetsDir != null && Files.isDirectory(assetsDir) && !Files.isSymbolicLink(assetsDir)) {
      val parentReal = runCatching { normSource.parent?.toRealPath() }.getOrNull()
      val baseReal = runCatching { assetsDir.toRealPath() }.getOrNull()
      val confinedUnderParent =
        parentReal != null && baseReal != null && baseReal.startsWith(parentReal)
      val assetFiles =
        if (baseReal != null) {
          Files.walk(assetsDir, 4).use { stream ->
            stream
              .filter { p ->
                Files.isRegularFile(p) &&
                  !Files.isSymbolicLink(p) &&
                  runCatching { p.toRealPath().startsWith(baseReal) }.getOrDefault(false)
              }
              .sorted()
              .toList()
          }
        } else {
          emptyList()
        }
      if (assetFiles.isNotEmpty()) {
        digest.update(0)
        digest.update("assets:".toByteArray(Charsets.UTF_8))
        for (asset in assetFiles) {
          val rel = assetsDir.relativize(asset).toString().replace('\\', '/')
          val assetBytes = Files.readAllBytes(asset)
          digest.update(rel.toByteArray(Charsets.UTF_8))
          digest.update(0)
          digest.update(assetBytes)
          digest.update(0)
          if (
            collectedAssets != null &&
              confinedUnderParent &&
              assetBytes.size.toLong() <= MAX_ASSET_FILE_BYTES
          ) {
            collectedAssets +=
              AssetEntry(
                relativePath = "assets/$rel",
                sha256 = sha256Hex(assetBytes),
                bytes = assetBytes,
              )
          }
        }
      }
    }
    val hex = digest.digest().joinToString("") { "%02x".format(it) }
    return Snapshot(normSource, normDesign, hex, sourceBytes, collectedAssets)
  }

  fun sha256Hex(bytes: ByteArray): String =
    MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}
