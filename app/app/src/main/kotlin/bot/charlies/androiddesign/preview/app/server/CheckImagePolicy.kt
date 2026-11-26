package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.model.BoardSurface
import java.nio.file.Files

/**
 * Single policy source for check PNG retention, SHA-256 deduplication, failing-frame follow-up
 * selection, and disk-cache sufficiency across [CheckExecutor], [RenderQueue], and
 * [PreviewSessionCoordinator].
 */
object CheckImagePolicy {
  private const val MAX_FAILING_IMAGES = 2
  private const val MAX_FOLLOWUP_TARGETS = 3

  fun primaryViews(surface: BoardSurface): Set<String> =
    if (surface == BoardSurface.Wear) setOf("watch", "watch-large") else setOf("light", "desktop")

  fun shouldRequestInitialFrame(
    scoped: Boolean,
    surface: BoardSurface,
    overlayId: String?,
    viewName: String,
  ): Boolean = scoped || (overlayId == null && viewName in primaryViews(surface))

  /**
   * Selects up to [MAX_FOLLOWUP_TARGETS] failing specs that still need a follow-up frame fetch,
   * returning empty when [MAX_FAILING_IMAGES] distinct failing frame hashes already exist.
   */
  fun selectFollowUpFailingSpecs(
    specs: List<CheckTargetSpec>,
    isPass: (CheckTargetSpec) -> Boolean,
    hasRenderError: (CheckTargetSpec) -> Boolean,
    frameBytesFor: (CheckTargetSpec) -> ByteArray?,
  ): List<CheckTargetSpec> {
    val existingFailingHashes = LinkedHashSet<String>()
    for (spec in specs) {
      if (isPass(spec)) continue
      val bytes = frameBytesFor(spec)
      if (bytes != null && bytes.isNotEmpty()) {
        existingFailingHashes += ContentHasher.sha256Hex(bytes)
      }
    }
    if (existingFailingHashes.size >= MAX_FAILING_IMAGES) return emptyList()
    return specs
      .filter { spec ->
        !isPass(spec) && !hasRenderError(spec) && frameBytesFor(spec) == null
      }
      .take(MAX_FOLLOWUP_TARGETS)
  }

  /** Selects which targets from a batch check should persist their PNG files to disk. */
  fun selectSpecsToWrite(
    specs: List<CheckTargetSpec>,
    scoped: Boolean,
    surface: BoardSurface,
    isPass: (CheckTargetSpec) -> Boolean,
    frameBytesFor: (CheckTargetSpec) -> ByteArray?,
  ): List<CheckTargetSpec> =
    selectRetained(
      items = specs,
      scoped = scoped,
      allPass = specs.all(isPass),
      isPass = isPass,
      isPrimary = { spec ->
        spec.overlayId == null && spec.view.name in primaryViews(surface)
      },
      dedupKey = { spec ->
        frameBytesFor(spec)?.takeIf { it.isNotEmpty() }?.let(ContentHasher::sha256Hex)
      },
    )

  /**
   * Selects which rendered PNGs to return in a check response: scoped checks return every written
   * view; failing checks deduplicate by PNG content hash and cap at [MAX_FAILING_IMAGES]; passing
   * full checks return primary views (`light`/`desktop` on phone, `watch`/`watch-large` on wear).
   */
  fun selectResponseImages(
    artifacts: List<RenderArtifact>,
    scoped: Boolean,
    surface: BoardSurface,
    allPass: Boolean = artifacts.all { it.pass },
  ): List<ImageOutput> =
    selectRetained(
        items = artifacts,
        scoped = scoped,
        allPass = allPass,
        isPass = { it.pass },
        isPrimary = { art -> art.overlay == null && art.view.name in primaryViews(surface) },
        dedupKey = { art ->
          if (art.pngPath == null) {
            null
          } else {
            art.pngSha256
              ?: art.pngBytes?.takeIf { it.isNotEmpty() }?.let(ContentHasher::sha256Hex)
              ?: art.pngPath.toString()
          }
        },
      )
      .mapNotNull(::toImageOutput)

  /**
   * True when cached [artifacts] already include all on-disk PNG files required for a check
   * response of this [scoped] and [surface] combination.
   */
  fun hasRequiredPngs(
    artifacts: List<RenderArtifact>,
    scoped: Boolean,
    surface: BoardSurface,
  ): Boolean {
    fun fileExists(art: RenderArtifact): Boolean =
      art.pngPath != null && Files.isRegularFile(art.pngPath)
    if (scoped) {
      return artifacts.all(::fileExists)
    }
    if (!artifacts.all { it.pass }) {
      return artifacts.any { !it.pass && fileExists(it) }
    }
    val primaries = artifacts.filter { it.overlay == null && it.view.name in primaryViews(surface) }
    return primaries.isEmpty() || primaries.all(::fileExists)
  }

  private fun toImageOutput(art: RenderArtifact): ImageOutput? {
    val path = art.pngPath ?: return null
    return ImageOutput(
      direction = art.direction,
      screen = art.screen,
      overlay = art.overlay,
      view = art.view.name,
      path = path.toString(),
      density = art.densityScale,
      pass = art.pass,
      endPath = art.endPngPath?.toString(),
    )
  }

  private inline fun <T> selectRetained(
    items: List<T>,
    scoped: Boolean,
    allPass: Boolean,
    crossinline isPass: (T) -> Boolean,
    crossinline isPrimary: (T) -> Boolean,
    crossinline dedupKey: (T) -> String?,
  ): List<T> {
    if (scoped) {
      return items
    }
    val seenKeys = LinkedHashSet<String>()
    val result = mutableListOf<T>()
    val candidates =
      if (!allPass) {
        items.asSequence().filter { !isPass(it) }
      } else {
        items.asSequence().filter { isPrimary(it) }
      }
    for (item in candidates) {
      val key = dedupKey(item) ?: continue
      if (!seenKeys.add(key)) continue
      result += item
      if (!allPass && result.size >= MAX_FAILING_IMAGES) break
    }
    return result
  }
}
