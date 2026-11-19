package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.data.DesignMdLocator
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

/**
 * Computes the shared content hash (`source + DESIGN.md + JAR version`) that keys compiled classes,
 * cached renders, watchdog timeouts, and click selections.
 */
object ContentHasher {
  data class Snapshot(
    val sourcePath: Path,
    val designPath: Path?,
    val contentHash: String,
  )

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
    if (Files.isRegularFile(normSource)) {
      digest.update(Files.readAllBytes(normSource))
    }
    digest.update(0)
    digest.update("design:".toByteArray(Charsets.UTF_8))
    if (normDesign != null && Files.isRegularFile(normDesign)) {
      digest.update(normDesign.toString().toByteArray(Charsets.UTF_8))
      digest.update(0)
      digest.update(Files.readAllBytes(normDesign))
    }
    val hex = digest.digest().joinToString("") { "%02x".format(it) }
    return Snapshot(normSource, normDesign, hex)
  }

  fun sha256Hex(bytes: ByteArray): String =
    MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}
