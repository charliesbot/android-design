package bot.charlies.androiddesign.preview.kit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.util.concurrent.ConcurrentHashMap
import org.jetbrains.skia.Image

/** Directory containing the active exploration file; used to confine [designPainter] reads. */
internal val LocalExplorationDir = staticCompositionLocalOf<Path?> { null }

internal object DesignAssetStore {
  private data class Stamp(val modified: FileTime, val size: Long)

  private data class Entry(val stamp: Stamp, val bitmap: ImageBitmap)

  private val cache = ConcurrentHashMap<Path, Entry>()

  fun loadBitmap(path: Path): ImageBitmap? {
    val normalized = path.toAbsolutePath().normalize()
    val stamp =
      try {
        val attrs = Files.readAttributes(normalized, BasicFileAttributes::class.java)
        if (!attrs.isRegularFile) return null
        Stamp(attrs.lastModifiedTime(), attrs.size())
      } catch (_: java.io.IOException) {
        return null
      }
    val existing = cache[normalized]
    if (existing != null && existing.stamp == stamp) return existing.bitmap
    val decoded =
      try {
        Image.makeFromEncoded(Files.readAllBytes(normalized)).toComposeImageBitmap()
      } catch (_: java.io.IOException) {
        return null
      } catch (_: RuntimeException) {
        return null
      }
    cache[normalized] = Entry(stamp, decoded)
    return decoded
  }
}

/**
 * Resolves [relativePath] strictly beneath [baseDir], throwing [IllegalArgumentException] on empty
 * paths, schemes, absolute paths, or `..` traversal segments.
 */
internal fun resolveConfinedAsset(baseDir: Path?, relativePath: String): Path {
  val trimmed = relativePath.trim()
  require(trimmed.isNotEmpty()) { "Asset path cannot be blank" }
  require(!trimmed.startsWith("/") && !trimmed.startsWith("\\") && !trimmed.contains(':')) {
    "Asset path must be relative to the exploration folder: $relativePath"
  }
  val rel = Path.of(trimmed)
  require(!rel.isAbsolute && rel.none { it.toString() == ".." }) {
    "Asset path cannot escape the exploration folder: $relativePath"
  }
  val base =
    requireNotNull(baseDir) { "No exploration folder is bound for asset '$relativePath'" }
      .toAbsolutePath()
      .normalize()
  val resolved = base.resolve(rel).normalize()
  require(resolved.startsWith(base)) {
    "Asset path cannot escape the exploration folder: $relativePath"
  }
  return resolved
}

/**
 * Loads an image asset relative to the exploration file's folder (for example
 * `"assets/cover.jpg"`). Throws [IllegalArgumentException] on paths escaping that directory.
 */
@Composable
fun designPainter(relativePath: String): Painter {
  val baseDir = LocalExplorationDir.current
  val target = resolveConfinedAsset(baseDir, relativePath)
  val bitmap = remember(target) { DesignAssetStore.loadBitmap(target) }
  return if (bitmap != null) BitmapPainter(bitmap) else ColorPainter(Color.Gray)
}
