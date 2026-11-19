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
 * Watches the active `.explore.kt` file and its paired `DESIGN.md` by polling file modification
 * time and size every [pollIntervalMs] ms (default 100 ms), avoiding macOS
 * [java.nio.file.WatchService] ~2 s polling latency, and debounces rapid saves by [debounceMs] ms
 * (default 80 ms).
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

  private val running = AtomicBoolean(false)
  private val stateLock = Any()
  private val pollScheduler = Executors.newSingleThreadScheduledExecutor { runnable ->
    Thread(runnable, "android-design-file-watcher").apply { isDaemon = true }
  }
  private val debounceScheduler = Executors.newSingleThreadScheduledExecutor { runnable ->
    Thread(runnable, "android-design-watcher-debounce").apply { isDaemon = true }
  }

  private var activeSource: Path? = null
  private var activeDesign: Path? = null
  private var lastSourceStamp: FileStamp? = null
  private var lastDesignStamp: FileStamp? = null
  private var pollTask: ScheduledFuture<*>? = null
  private var pendingTask: ScheduledFuture<*>? = null

  fun watch(sourcePath: Path, designPath: Path?) {
    val normSource = sourcePath.toAbsolutePath().normalize()
    val normDesign = designPath?.toAbsolutePath()?.normalize()
    synchronized(stateLock) {
      if (normSource == activeSource && normDesign == activeDesign && running.get()) return
      activeSource = normSource
      activeDesign = normDesign
      lastSourceStamp = readStamp(normSource)
      lastDesignStamp = normDesign?.let(::readStamp)
      ensureStartedLocked()
    }
  }

  private fun ensureStartedLocked() {
    if (!running.compareAndSet(false, true)) return
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
    val changedTarget: Pair<Path, Path?>? =
      synchronized(stateLock) {
        if (!running.get()) return
        val src = activeSource ?: return
        val design = activeDesign
        val nextSource = readStamp(src)
        val nextDesign = design?.let(::readStamp)
        val changed = nextSource != lastSourceStamp || nextDesign != lastDesignStamp
        if (changed) {
          lastSourceStamp = nextSource
          lastDesignStamp = nextDesign
          src to design
        } else {
          null
        }
      }
    if (changedTarget != null) {
      scheduleFire(changedTarget.first, changedTarget.second)
    }
  }

  private fun scheduleFire(src: Path, design: Path?) {
    synchronized(stateLock) {
      if (!running.get()) return
      pendingTask?.cancel(false)
      pendingTask =
        debounceScheduler.schedule(
          { fireIfQuiet(src, design) },
          debounceMs,
          TimeUnit.MILLISECONDS,
        )
    }
  }

  private fun fireIfQuiet(src: Path, design: Path?) {
    if (!running.get()) return
    val stillChanging =
      synchronized(stateLock) {
        if (!running.get() || activeSource != src || activeDesign != design) return
        val nextSource = readStamp(src)
        val nextDesign = design?.let(::readStamp)
        if (nextSource != lastSourceStamp || nextDesign != lastDesignStamp) {
          lastSourceStamp = nextSource
          lastDesignStamp = nextDesign
          true
        } else {
          false
        }
      }
    if (stillChanging) {
      scheduleFire(src, design)
      return
    }
    if (running.get() && Files.isRegularFile(src)) {
      onChanged(src, design)
    }
  }

  fun stop() {
    running.set(false)
    synchronized(stateLock) {
      pollTask?.cancel(false)
      pollTask = null
      pendingTask?.cancel(false)
      pendingTask = null
    }
    pollScheduler.shutdownNow()
    debounceScheduler.shutdownNow()
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
}
