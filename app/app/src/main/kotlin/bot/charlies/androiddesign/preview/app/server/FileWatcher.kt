package bot.charlies.androiddesign.preview.app.server

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Watches the active `.explore.kt` file, its paired `DESIGN.md`, and `assets/` per repository by
 * polling file modification time and size every [pollIntervalMs] ms (default 100 ms), avoiding
 * macOS [java.nio.file.WatchService] ~2 s polling latency, and debouncing rapid saves by
 * [debounceMs] ms (default 80 ms).
 */
class FileWatcher(
  private val debounceMs: Long = 80L,
  private val pollIntervalMs: Long = 100L,
  private val onChanged: (Path, Path?) -> Unit,
) {
  private data class FileStamp(
    val exists: Boolean,
    val lastModified: Instant?,
    val size: Long,
    val fileKey: Any?,
  )

  private data class WatchEntry(
    val repoKey: String,
    var generation: Long,
    var activeSource: Path,
    var activeDesign: Path?,
    var lastSourceStamp: FileStamp,
    var lastDesignStamp: FileStamp?,
    var lastAssetsStamp: List<Pair<String, FileStamp>>,
    var pendingTask: ScheduledFuture<*>? = null,
  )

  private val running = AtomicBoolean(false)
  private val stateLock = Any()
  private val pollScheduler = Executors.newSingleThreadScheduledExecutor { runnable ->
    Thread(runnable, "android-design-file-watcher").apply { isDaemon = true }
  }
  private val debounceScheduler = Executors.newSingleThreadScheduledExecutor { runnable ->
    Thread(runnable, "android-design-watcher-debounce").apply { isDaemon = true }
  }

  private var stopped = false
  private val entries = LinkedHashMap<String, WatchEntry>()
  private var pollTask: ScheduledFuture<*>? = null

  fun watch(sourcePath: Path, designPath: Path?) {
    watchForRepo(DEFAULT_REPO_KEY, sourcePath, designPath)
  }

  fun watchForRepo(repoKey: String, sourcePath: Path, designPath: Path?) {
    val normSource = sourcePath.toAbsolutePath().normalize()
    val normDesign = designPath?.toAbsolutePath()?.normalize()
    synchronized(stateLock) {
      if (stopped) return
      val existing = entries[repoKey]
      if (
        existing != null &&
          existing.activeSource == normSource &&
          existing.activeDesign == normDesign &&
          running.get()
      ) {
        return
      }
      existing?.pendingTask?.cancel(false)
      val nextGen = (existing?.generation ?: 0L) + 1L
      entries[repoKey] =
        WatchEntry(
          repoKey = repoKey,
          generation = nextGen,
          activeSource = normSource,
          activeDesign = normDesign,
          lastSourceStamp = readStamp(normSource),
          lastDesignStamp = normDesign?.let(::readStamp),
          lastAssetsStamp = readAssetsStamp(normSource.parent),
          pendingTask = null,
        )
      ensureStartedLocked()
    }
  }

  private fun ensureStartedLocked() {
    if (stopped || !running.compareAndSet(false, true)) return
    pollTask =
      pollScheduler.scheduleWithFixedDelay(
        { pollOnce() },
        pollIntervalMs,
        pollIntervalMs,
        TimeUnit.MILLISECONDS,
      )
  }

  private fun pollOnce() {
    if (!running.get()) return
    synchronized(stateLock) {
      if (stopped || !running.get()) return
      for (entry in entries.values) {
        val src = entry.activeSource
        val design = entry.activeDesign
        val nextSource = readStamp(src)
        val nextDesign = design?.let(::readStamp)
        val nextAssets = readAssetsStamp(src.parent)
        if (
          nextSource != entry.lastSourceStamp ||
            nextDesign != entry.lastDesignStamp ||
            nextAssets != entry.lastAssetsStamp
        ) {
          entry.lastSourceStamp = nextSource
          entry.lastDesignStamp = nextDesign
          entry.lastAssetsStamp = nextAssets
          scheduleFireLocked(entry)
        }
      }
    }
  }

  private fun scheduleFireLocked(entry: WatchEntry) {
    if (stopped || !running.get()) return
    val expectedGen = ++entry.generation
    val src = entry.activeSource
    val design = entry.activeDesign
    entry.pendingTask?.cancel(false)
    entry.pendingTask =
      debounceScheduler.schedule(
        { fireIfQuiet(entry.repoKey, src, design, expectedGen) },
        debounceMs,
        TimeUnit.MILLISECONDS,
      )
  }

  private fun fireIfQuiet(repoKey: String, src: Path, design: Path?, expectedGen: Long) {
    if (!running.get()) return
    val shouldFire =
      synchronized(stateLock) {
        val entry = entries[repoKey] ?: return
        if (
          stopped ||
            !running.get() ||
            entry.generation != expectedGen ||
            entry.activeSource != src ||
            entry.activeDesign != design
        ) {
          return
        }
        val nextSource = readStamp(src)
        val nextDesign = design?.let(::readStamp)
        val nextAssets = readAssetsStamp(src.parent)
        if (
          nextSource != entry.lastSourceStamp ||
            nextDesign != entry.lastDesignStamp ||
            nextAssets != entry.lastAssetsStamp
        ) {
          entry.lastSourceStamp = nextSource
          entry.lastDesignStamp = nextDesign
          entry.lastAssetsStamp = nextAssets
          scheduleFireLocked(entry)
          false
        } else {
          entry.pendingTask = null
          true
        }
      }
    if (shouldFire && running.get() && Files.isRegularFile(src)) {
      onChanged(src, design)
    }
  }

  fun stop() {
    running.set(false)
    val shouldShutdown =
      synchronized(stateLock) {
        if (stopped) {
          false
        } else {
          stopped = true
          pollTask?.cancel(false)
          pollTask = null
          for (entry in entries.values) {
            entry.generation++
            entry.pendingTask?.cancel(false)
            entry.pendingTask = null
          }
          entries.clear()
          true
        }
      }
    if (shouldShutdown) {
      pollScheduler.shutdownNow()
      debounceScheduler.shutdownNow()
    }
  }

  private fun readStamp(path: Path): FileStamp =
    try {
      val attrs = Files.readAttributes(path, BasicFileAttributes::class.java)
      FileStamp(
        exists = true,
        lastModified = attrs.lastModifiedTime().toInstant(),
        size = attrs.size(),
        fileKey = attrs.fileKey(),
      )
    } catch (_: Exception) {
      FileStamp(exists = false, lastModified = null, size = -1L, fileKey = null)
    }

  private fun readAssetsStamp(baseDir: Path?): List<Pair<String, FileStamp>> {
    val assetsDir = baseDir?.resolve("assets") ?: return emptyList()
    if (!Files.isDirectory(assetsDir) || Files.isSymbolicLink(assetsDir)) return emptyList()
    return try {
      Files.walk(assetsDir, 4).use { stream ->
        stream
          .filter { Files.isRegularFile(it) && !Files.isSymbolicLink(it) }
          .sorted()
          .map { path -> assetsDir.relativize(path).toString() to readStamp(path) }
          .toList()
      }
    } catch (_: Exception) {
      emptyList()
    }
  }

  private companion object {
    const val DEFAULT_REPO_KEY = "__default__"
  }
}
