package bot.charlies.androiddesign.preview.app.server

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey
import java.nio.file.WatchService
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Watches the active `.explore.kt` file and its paired `DESIGN.md` using NIO [WatchService],
 * debouncing rapid saves into one callback per change.
 */
class FileWatcher(
  private val debounceMs: Long = 80L,
  private val onChanged: (Path, Path?) -> Unit,
) {
  private val running = AtomicBoolean(false)
  private val scheduler = Executors.newSingleThreadScheduledExecutor { runnable ->
    Thread(runnable, "android-design-watcher-debounce").apply { isDaemon = true }
  }
  private val watchedFiles = ConcurrentHashMap<Path, Set<String>>()
  private val keysByDir = ConcurrentHashMap<Path, WatchKey>()
  @Volatile private var watchService: WatchService? = null
  @Volatile private var loopThread: Thread? = null
  @Volatile private var activeSource: Path? = null
  @Volatile private var activeDesign: Path? = null
  @Volatile private var pendingTask: ScheduledFuture<*>? = null

  fun watch(sourcePath: Path, designPath: Path?) {
    val normSource = sourcePath.toAbsolutePath().normalize()
    val normDesign = designPath?.toAbsolutePath()?.normalize()
    if (normSource == activeSource && normDesign == activeDesign && running.get()) return
    activeSource = normSource
    activeDesign = normDesign
    ensureStarted()
    rebindDirectories(normSource, normDesign)
  }

  private fun ensureStarted() {
    if (!running.compareAndSet(false, true)) return
    val service = FileSystems.getDefault().newWatchService()
    watchService = service
    val thread =
      Thread(
        {
          while (running.get()) {
            val key =
              try {
                service.take()
              } catch (_: Exception) {
                break
              }
            val dir = key.watchable() as? Path
            val targets = if (dir != null) watchedFiles[dir].orEmpty() else emptySet()
            var relevant = false
            for (event in key.pollEvents()) {
              val kind = event.kind()
              if (kind == StandardWatchEventKinds.OVERFLOW) {
                relevant = true
                continue
              }
              val changed = (event.context() as? Path)?.fileName?.toString()
              if (changed == null || changed in targets) {
                relevant = true
              }
            }
            if (!key.reset() && dir != null) {
              keysByDir.remove(dir)
            }
            if (relevant) {
              scheduleFire()
            }
          }
        },
        "android-design-file-watcher",
      )
    thread.isDaemon = true
    loopThread = thread
    thread.start()
  }

  private fun rebindDirectories(source: Path, design: Path?) {
    val service = watchService ?: return
    val byDir = LinkedHashMap<Path, MutableSet<String>>()
    fun addTarget(file: Path) {
      val parent = file.parent ?: return
      if (!Files.isDirectory(parent)) return
      byDir.getOrPut(parent) { linkedSetOf() } += file.fileName.toString()
    }
    addTarget(source)
    if (design != null) addTarget(design)

    watchedFiles.clear()
    watchedFiles.putAll(byDir)

    val obsolete = keysByDir.keys.filter { it !in byDir }
    for (dir in obsolete) {
      keysByDir.remove(dir)?.cancel()
    }
    for (dir in byDir.keys) {
      if (!keysByDir.containsKey(dir)) {
        try {
          val key =
            dir.register(
              service,
              StandardWatchEventKinds.ENTRY_CREATE,
              StandardWatchEventKinds.ENTRY_MODIFY,
              StandardWatchEventKinds.ENTRY_DELETE,
            )
          keysByDir[dir] = key
        } catch (_: Exception) {
          // Ignore environments where directory watching is restricted.
        }
      }
    }
  }

  private fun scheduleFire() {
    pendingTask?.cancel(false)
    val src = activeSource ?: return
    val design = activeDesign
    pendingTask =
      scheduler.schedule(
        {
          if (running.get() && Files.isRegularFile(src)) {
            onChanged(src, design)
          }
        },
        debounceMs,
        TimeUnit.MILLISECONDS,
      )
  }

  fun stop() {
    running.set(false)
    pendingTask?.cancel(false)
    pendingTask = null
    keysByDir.values.forEach { it.cancel() }
    keysByDir.clear()
    try {
      watchService?.close()
    } catch (_: Exception) {
      // Ignore close errors.
    }
    watchService = null
    loopThread?.interrupt()
    loopThread = null
    scheduler.shutdownNow()
  }
}
