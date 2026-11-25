package bot.charlies.androiddesign.preview.kit

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.util.concurrent.ConcurrentHashMap

/** Directory containing the active exploration file; used by JVM test hosts. */
internal val LocalExplorationDir = staticCompositionLocalOf<Path?> { null }

/** Content-addressed asset resolver injected by `:sandbox` or host renderers. */
internal val LocalAssetResolver = staticCompositionLocalOf<((String) -> ImageBitmap?)?> { null }

internal object DesignAssetStore {
  private data class Stamp(val modified: FileTime, val size: Long)

  private data class Entry(val stamp: Stamp, val bitmap: ImageBitmap)

  private val cache = ConcurrentHashMap<Path, Entry>()

  fun decodeBytes(bytes: ByteArray): ImageBitmap? =
    try {
      BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (_: RuntimeException) {
      null
    }

  fun loadBitmap(path: Path): ImageBitmap? {
    val normalized = path.toAbsolutePath().normalize()
    val stamp =
      try {
        val attrs = Files.readAttributes(normalized, BasicFileAttributes::class.java)
        if (!attrs.isRegularFile) return null
        Stamp(attrs.lastModifiedTime(), attrs.size())
      } catch (_: IOException) {
        return null
      }
    val existing = cache[normalized]
    if (existing != null && existing.stamp == stamp) return existing.bitmap
    val decoded =
      try {
        decodeBytes(Files.readAllBytes(normalized))
      } catch (_: IOException) {
        null
      } ?: return null
    cache[normalized] = Entry(stamp, decoded)
    return decoded
  }
}

/** Validates [relativePath] as a safe exploration-relative asset path without `..` segments. */
internal fun normalizeRelativeAssetPath(relativePath: String): String {
  val trimmed = relativePath.trim()
  require(trimmed.isNotEmpty()) { "Asset path cannot be blank" }
  require(!trimmed.startsWith("/") && !trimmed.contains('\\') && !trimmed.contains(':')) {
    "Asset path must be relative to the exploration folder: $relativePath"
  }
  val rel = Paths.get(trimmed)
  require(!rel.isAbsolute && rel.none { it.toString() == ".." }) {
    "Asset path cannot escape the exploration folder: $relativePath"
  }
  return rel.normalize().toString().replace('\\', '/')
}

/**
 * Resolves [relativePath] strictly beneath [baseDir], throwing [IllegalArgumentException] on
 * escape.
 */
internal fun resolveConfinedAsset(baseDir: Path?, relativePath: String): Path {
  val normalizedRel = normalizeRelativeAssetPath(relativePath)
  val base =
    requireNotNull(baseDir) { "No exploration folder is bound for asset '$relativePath'" }
      .toAbsolutePath()
      .normalize()
  val resolved = base.resolve(normalizedRel).normalize()
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
  val normalizedKey = normalizeRelativeAssetPath(relativePath)
  val resolver = LocalAssetResolver.current
  val baseDir = LocalExplorationDir.current
  val bitmap =
    remember(normalizedKey, resolver, baseDir) {
      resolver?.invoke(normalizedKey)
        ?: baseDir?.let { DesignAssetStore.loadBitmap(resolveConfinedAsset(it, normalizedKey)) }
    }
  return if (bitmap != null) BitmapPainter(bitmap) else ColorPainter(Color.Gray)
}
