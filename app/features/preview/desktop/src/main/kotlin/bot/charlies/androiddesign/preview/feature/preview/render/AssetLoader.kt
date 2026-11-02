package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import org.jetbrains.skia.Image

/**
 * Loads the bitmaps an `Image` component names: a `data:` URI, or a path relative to the proposal
 * file. A source that cannot be read gives null, and the renderer draws a placeholder.
 */
class AssetLoader(private val proposalDir: Path?) {
  /** A decoded source and what the file looked like then, so a rewrite is noticed. */
  private class Entry(val stamp: Stamp?, val image: ImageBitmap?)

  /** A file's modification time and size. */
  private data class Stamp(val modified: FileTime, val size: Long)

  private val cache = ConcurrentHashMap<String, Entry>()

  /** The image for [src]. Rewriting the file replaces its entry, so old versions do not pile up. */
  fun image(src: String): ImageBitmap? {
    val stamp = stampOf(src)
    val cached = cache[src]
    if (cached != null && cached.stamp == stamp) return cached.image
    return Entry(stamp, decode(src)).also { cache[src] = it }.image
  }

  /** What [src] is now: null for a `data:` URI or a file that is not there, which never changes. */
  private fun stampOf(src: String): Stamp? {
    val file = fileOf(src) ?: return null
    return try {
      val attributes = Files.readAttributes(file, BasicFileAttributes::class.java)
      if (attributes.isRegularFile) Stamp(attributes.lastModifiedTime(), attributes.size())
      else null
    } catch (_: java.io.IOException) {
      null
    }
  }

  /** The file a relative path names inside the proposal folder, or null for anything else. */
  private fun fileOf(src: String): Path? {
    if (src.startsWith("data:") || src.startsWith("/")) return null
    return proposalDir?.resolve(src)?.normalize()
  }

  private fun decode(src: String): ImageBitmap? =
    try {
      val bytes = bytes(src)
      bytes?.let { Image.makeFromEncoded(it).toComposeImageBitmap() }
    } catch (_: java.io.IOException) {
      null
    } catch (_: RuntimeException) {
      null
    }

  private fun bytes(src: String): ByteArray? {
    if (src.startsWith("data:")) {
      val comma = src.indexOf(',')
      if (comma < 0) return null
      val header = src.substring(5, comma)
      val payload = src.substring(comma + 1)
      return if (header.endsWith(";base64")) Base64.getDecoder().decode(payload.trim())
      else java.net.URLDecoder.decode(payload, Charsets.UTF_8).toByteArray()
    }
    val file = fileOf(src) ?: return null
    return if (Files.isRegularFile(file)) Files.readAllBytes(file) else null
  }
}
