package bot.charlies.androiddesign.preview.host.sandbox

import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import bot.charlies.androiddesign.preview.kit.DesignAssetStore
import bot.charlies.androiddesign.preview.kit.normalizeRelativeAssetPath

/** Content-addressed in-memory cache of design assets pushed by the host server. */
internal class DeviceAssetCache(
  private val maxRawBytes: Int = DEFAULT_MAX_RAW_BYTES,
  private val maxBitmaps: Int = DEFAULT_MAX_BITMAPS,
) {
  private val rawByHash =
    object : LruCache<String, ByteArray>(maxRawBytes) {
      override fun sizeOf(key: String, value: ByteArray): Int = value.size.coerceAtLeast(1)
    }

  private val bitmapByHash =
    object : LruCache<String, ImageBitmap>(maxBitmaps) {
      override fun sizeOf(key: String, value: ImageBitmap): Int = 1
    }

  @Synchronized
  fun missing(hashes: List<String>): List<String> =
    hashes.distinct().filter { rawByHash.get(it) == null }

  @Synchronized
  fun put(sha256: String, bytes: ByteArray) {
    rawByHash.put(sha256, bytes)
    bitmapByHash.remove(sha256)
  }

  @Synchronized
  fun getBitmap(sha256: String): ImageBitmap? {
    bitmapByHash.get(sha256)?.let {
      return it
    }
    val raw = rawByHash.get(sha256) ?: return null
    val decoded = DesignAssetStore.decodeBytes(raw) ?: return null
    bitmapByHash.put(sha256, decoded)
    return decoded
  }

  fun resolverFor(assetMap: Map<String, String>): ((String) -> ImageBitmap?)? {
    if (assetMap.isEmpty()) return null
    val normalized = assetMap.entries.associate { (k, v) -> normalizeRelativeAssetPath(k) to v }
    return { relPath ->
      val key = normalizeRelativeAssetPath(relPath)
      normalized[key]?.let(::getBitmap)
    }
  }

  private companion object {
    const val DEFAULT_MAX_RAW_BYTES = 48 * 1024 * 1024
    const val DEFAULT_MAX_BITMAPS = 64
  }
}
