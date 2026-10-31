package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.nio.file.Files
import java.nio.file.Path
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import org.jetbrains.skia.Image

/**
 * Loads the bitmaps an `Image` component names: a `data:` URI, or a path relative to the proposal
 * file. A source that cannot be read gives null, and the renderer draws a placeholder.
 */
class AssetLoader(private val proposalDir: Path?) {
  private val cache = ConcurrentHashMap<String, java.util.Optional<ImageBitmap>>()

  fun image(src: String): ImageBitmap? =
    cache.getOrPut(src) { java.util.Optional.ofNullable(decode(src)) }.orElse(null)

  private fun decode(src: String): ImageBitmap? =
    try {
      val bytes = bytes(src)
      bytes?.let { Image.makeFromEncoded(it).toComposeImageBitmap() }
    } catch (_: IllegalArgumentException) {
      null
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
    val dir = proposalDir ?: return null
    if (src.startsWith("/")) return null
    val file = dir.resolve(src).normalize()
    return if (Files.isRegularFile(file)) Files.readAllBytes(file) else null
  }
}
