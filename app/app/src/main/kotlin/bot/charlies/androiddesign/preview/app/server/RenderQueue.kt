package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.cli.ExploreRenderRunner
import bot.charlies.androiddesign.preview.app.cli.RenderEntry
import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.domain.layout.LayoutSummary
import bot.charlies.androiddesign.preview.domain.layout.ViewLayout
import bot.charlies.androiddesign.preview.feature.preview.sandbox.CompileResult
import bot.charlies.androiddesign.preview.feature.preview.sandbox.HeadlessResult
import bot.charlies.androiddesign.preview.feature.preview.sandbox.SandboxHeadlessRenderer
import bot.charlies.androiddesign.preview.kit.DirectionSpec
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.SurfaceSpec
import bot.charlies.androiddesign.preview.model.ViewSpec
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/** Job priority for the single-view render queue: agent > board focus > board remainder. */
enum class QueuePriority(val rank: Int) {
  AGENT(0),
  BOARD_FOCUS(1),
  BOARD_REMAINDER(2),
}

/** Unique key for one rendered view inside a specific content hash. */
data class RenderKey(
  val direction: String,
  val screen: String,
  val overlay: String?,
  val view: String,
  val densityScale: Int,
) {
  fun scopeLabel(): String = buildString {
    append(direction)
    append('/')
    append(screen)
    if (overlay != null) {
      append('+')
      append(overlay)
    }
    append('/')
    append(view)
    append('@')
    append(densityScale)
    append('x')
  }
}

/** One compiled board cached for a content hash. */
data class CompileEntry(
  val sourcePath: Path,
  val designPath: Path?,
  val contentHash: String,
  val loaded: ExploreRenderRunner.LoadedExploration,
  val fromCache: Boolean,
  val compileCountAtLoad: Int,
)

/** One completed 1-view render, cached by `(sourcePath, contentHash, RenderKey)`. */
data class RenderArtifact(
  val direction: String,
  val screen: String,
  val overlay: String?,
  val view: ViewSpec,
  val densityScale: Int,
  val artifactName: String,
  val pass: Boolean,
  val renderMs: Long,
  val check: ViewCheck,
  val layout: ViewLayout?,
  val tree: LayoutTree?,
  val pngPath: Path?,
  val endPngPath: Path?,
  val pngBytes: ByteArray?,
  val endPngBytes: ByteArray?,
  val pngSha256: String?,
  val entry: RenderEntry,
)

/** Pluggable compiler so unit tests can inject deterministic or slow compiles. */
fun interface ExplorationLoader {
  fun loadAndCompile(sourcePath: Path, explicitDesign: Path?): ExploreRenderRunner.LoadedExploration
}

/** Pluggable single-view renderer so unit tests can inject deterministic or slow renders. */
fun interface DensityTargetRenderer {
  fun render(
    compiled: CompileResult.Compiled,
    direction: DirectionSpec,
    screenId: String,
    view: ViewSpec,
    designSystem: DesignSystem,
    overlayId: String?,
    density: Float,
  ): HeadlessResult
}

/**
 * Shared content-hash result store and 1-view-per-job priority queue.
 *
 * Every compile and every 1-view render runs sequentially on one worker thread guarded by
 * [watchdog], so a higher-priority request or a newer file save preempts remaining queued views
 * within a single view's render (~0.1 to 0.35 s). Identical `(file, hash, key)` requests share the
 * same completed or in-flight result.
 */
class RenderQueue(
  private val rendersRoot: Path,
  private val watchdog: Watchdog? = null,
  private val loader: ExplorationLoader = ExplorationLoader { src, design ->
    ExploreRenderRunner.loadAndCompile(src, design)
  },
  private val renderer: DensityTargetRenderer =
    DensityTargetRenderer { compiled, dir, screenId, view, system, overlayId, density ->
      SandboxHeadlessRenderer.render(
        compiled = compiled,
        direction = dir,
        screenId = screenId,
        view = view,
        designSystem = system,
        overlayId = overlayId,
        density = density,
        encodePng = true,
      )
    },
  private val onViewRendered: (Path, String, RenderArtifact) -> Unit = { _, _, _ -> },
) {
  private abstract class QueueJob(
    val sourceKey: String,
    val contentHash: String,
    val priority: QueuePriority,
    val seq: Long,
    val cancellableOnNewSave: Boolean,
  ) : Comparable<QueueJob> {
    val cancelled = AtomicBoolean(false)

    override fun compareTo(other: QueueJob): Int {
      val byRank = priority.rank.compareTo(other.priority.rank)
      if (byRank != 0) return byRank
      return seq.compareTo(other.seq)
    }

    abstract fun cancel(reason: String)

    abstract fun execute()
  }

  private class FileHashCache(val contentHash: String) {
    @Volatile var compileFuture: CompletableFuture<CompileEntry>? = null
    @Volatile var compiled: CompileEntry? = null
    val renderFutures = ConcurrentHashMap<RenderKey, CompletableFuture<RenderArtifact>>()
    val rendered = ConcurrentHashMap<RenderKey, RenderArtifact>()
  }

  private val queue = PriorityBlockingQueue<QueueJob>()
  private val seqGen = AtomicLong(0L)
  private val compileCounter = AtomicInteger(0)
  private val running = AtomicBoolean(false)
  private val cachesByFile = ConcurrentHashMap<String, FileHashCache>()
  private val cacheLock = Any()
  @Volatile private var workerThread: Thread? = null

  val compileCount: Int
    get() = compileCounter.get()

  fun start() {
    if (!running.compareAndSet(false, true)) return
    val t =
      Thread(
        {
          while (running.get()) {
            val job =
              try {
                queue.take()
              } catch (_: InterruptedException) {
                break
              }
            if (job.cancelled.get()) continue
            try {
              job.execute()
            } catch (_: Throwable) {
              // Each job completes its own CompletableFuture exceptionally.
            }
          }
        },
        "android-design-render-queue",
      )
    t.isDaemon = true
    workerThread = t
    t.start()
  }

  fun stop() {
    running.set(false)
    workerThread?.interrupt()
    workerThread = null
    val drained = ArrayList<QueueJob>()
    queue.drainTo(drained)
    drained.forEach { it.cancel("server stopped") }
  }

  /**
   * Cancels queued background board jobs for [sourcePath] belonging to any hash older than
   * [newContentHash]. Running 1-view renders finish cleanly; queued remainder jobs are dropped.
   */
  fun cancelStaleJobs(sourcePath: Path, newContentHash: String) {
    val key = canonicalKey(sourcePath)
    val toRemove = ArrayList<QueueJob>()
    for (job in queue) {
      if (job.sourceKey == key && job.contentHash != newContentHash && job.cancellableOnNewSave) {
        if (job.cancelled.compareAndSet(false, true)) {
          toRemove += job
        }
      }
    }
    toRemove.forEach { job ->
      queue.remove(job)
      job.cancel("superseded by newer save")
    }
  }

  /** Returns the currently cached [CompileEntry] for [sourcePath], if any. */
  fun latestCompiled(sourcePath: Path): CompileEntry? =
    cachesByFile[canonicalKey(sourcePath)]?.compiled

  /** Returns true when [contentHash] is still the latest hash registered for [sourcePath]. */
  fun isCurrentHash(sourcePath: Path, contentHash: String): Boolean =
    synchronized(cacheLock) { cachesByFile[canonicalKey(sourcePath)]?.contentHash == contentHash }

  /** Returns all completed 2x [RenderArtifact]s for [sourcePath]'s current hash. */
  fun completedBoardArtifacts(sourcePath: Path): Map<RenderKey, RenderArtifact> {
    val cache = cachesByFile[canonicalKey(sourcePath)] ?: return emptyMap()
    return cache.rendered.filterKeys { it.densityScale == 2 }
  }

  /**
   * Compiles [sourcePath] for [snapshot.contentHash] or shares an existing completed/in-flight
   * compilation for the exact same hash.
   */
  fun submitCompile(
    snapshot: ContentHasher.Snapshot,
    priority: QueuePriority,
  ): CompletableFuture<CompileEntry> {
    val key = canonicalKey(snapshot.sourcePath)
    val future: CompletableFuture<CompileEntry>
    var needEnqueue = false
    synchronized(cacheLock) {
      val existing = cachesByFile[key]
      val hashCache =
        if (existing != null && existing.contentHash == snapshot.contentHash) {
          existing
        } else {
          FileHashCache(snapshot.contentHash).also { cachesByFile[key] = it }
        }
      hashCache.compiled?.let { ready ->
        return CompletableFuture.completedFuture(ready.copy(fromCache = true))
      }
      val inFlight = hashCache.compileFuture
      if (inFlight != null && !inFlight.isCancelled && !inFlight.isCompletedExceptionally) {
        future = inFlight
      } else {
        future = CompletableFuture()
        hashCache.compileFuture = future
        needEnqueue = true
      }
    }

    if (needEnqueue) {
      val job =
        object :
          QueueJob(
            sourceKey = key,
            contentHash = snapshot.contentHash,
            priority = priority,
            seq = seqGen.incrementAndGet(),
            cancellableOnNewSave = priority != QueuePriority.AGENT,
          ) {
          override fun cancel(reason: String) {
            future.completeExceptionally(CancellationException(reason))
          }

          override fun execute() {
            if (cancelled.get()) {
              cancel("cancelled")
              return
            }
            try {
              val isCold = compileCounter.get() == 0
              val loaded =
                runWithWatchdog(snapshot.contentHash, "compile", isCold) {
                  loader.loadAndCompile(snapshot.sourcePath, snapshot.designPath)
                }
              val count = compileCounter.incrementAndGet()
              val entry =
                CompileEntry(
                  sourcePath = snapshot.sourcePath,
                  designPath = snapshot.designPath,
                  contentHash = snapshot.contentHash,
                  loaded = loaded,
                  fromCache = false,
                  compileCountAtLoad = count,
                )
              synchronized(cacheLock) {
                val current = cachesByFile[key]
                if (current != null && current.contentHash == snapshot.contentHash) {
                  current.compiled = entry
                }
              }
              future.complete(entry)
            } catch (t: Throwable) {
              synchronized(cacheLock) {
                val current = cachesByFile[key]
                if (current != null && current.contentHash == snapshot.contentHash) {
                  current.compileFuture = null
                }
              }
              future.completeExceptionally(t)
            }
          }
        }
      queue.put(job)
    }
    return future
  }

  /**
   * Enqueues or shares a single 1-view render job for [entry] at [priority]. If an identical
   * `(file, contentHash, key)` render is already complete or in flight, returns that shared future
   * (promoting queued work when a higher-priority caller waits on it).
   */
  fun submitRender(
    entry: CompileEntry,
    direction: DirectionSpec,
    screenId: String,
    prefix: String,
    overlayId: String?,
    view: ViewSpec,
    densityScale: Int,
    baseCheck: ViewCheck?,
    priority: QueuePriority,
  ): CompletableFuture<RenderArtifact> {
    val sourceKey = canonicalKey(entry.sourcePath)
    val renderKey = RenderKey(direction.name, screenId, overlayId, view.name, densityScale)
    val compiled =
      entry.loaded.compiled
        ?: return CompletableFuture.failedFuture(
          IllegalStateException("Exploration is not compiled"),
        )
    val surface = compiled.board.surfaceSpec

    val future: CompletableFuture<RenderArtifact>
    var needEnqueue = false
    synchronized(cacheLock) {
      val currentCache = cachesByFile[sourceKey]
      if (currentCache != null && currentCache.contentHash != entry.contentHash) {
        return CompletableFuture.failedFuture(CancellationException("superseded by newer save"))
      }
      val hashCache =
        currentCache
          ?: FileHashCache(entry.contentHash).also {
            it.compiled = entry
            cachesByFile[sourceKey] = it
          }
      hashCache.rendered[renderKey]?.let { done ->
        return CompletableFuture.completedFuture(done)
      }
      val existing = hashCache.renderFutures[renderKey]
      if (existing != null && !existing.isCancelled && !existing.isCompletedExceptionally) {
        future = existing
        // If an AGENT or BOARD_FOCUS request awaits a queued BOARD_REMAINDER job, promote it.
        promoteQueuedJob(sourceKey, entry.contentHash, renderKey, priority)
      } else {
        future = CompletableFuture()
        hashCache.renderFutures[renderKey] = future
        needEnqueue = true
      }
    }

    if (needEnqueue) {
      val job =
        RenderViewJob(
          sourceKey = sourceKey,
          contentHash = entry.contentHash,
          renderKey = renderKey,
          priority = priority,
          seq = seqGen.incrementAndGet(),
          cancellableOnNewSave = priority != QueuePriority.AGENT,
          entry = entry,
          compiled = compiled,
          direction = direction,
          screenId = screenId,
          prefix = prefix,
          overlayId = overlayId,
          view = view,
          surface = surface,
          densityScale = densityScale,
          baseCheck = baseCheck,
          future = future,
        )
      queue.put(job)
    }
    return future
  }

  /** Renders directly using cached compiled classes with ad-hoc theme/view overrides (no cache). */
  fun renderOverride(
    entry: CompileEntry,
    direction: DirectionSpec,
    screenId: String,
    prefix: String,
    overlayId: String?,
    view: ViewSpec,
    designSystem: DesignSystem,
    densityScale: Int = 2,
  ): RenderArtifact {
    val compiled =
      checkNotNull(entry.loaded.compiled) { "Cannot render override without compiled board" }
    val surface = compiled.board.surfaceSpec
    return executeSingleRender(
      contentHash = entry.contentHash,
      compiled = compiled,
      direction = direction,
      screenId = screenId,
      prefix = prefix,
      overlayId = overlayId,
      view = view,
      designSystem = designSystem,
      surface = surface,
      densityScale = densityScale,
      baseCheck = null,
      writeFiles = false,
    )
  }

  private fun promoteQueuedJob(
    sourceKey: String,
    contentHash: String,
    renderKey: RenderKey,
    desiredPriority: QueuePriority,
  ) {
    val queued =
      queue.firstOrNull {
        it is RenderViewJob &&
          it.sourceKey == sourceKey &&
          it.contentHash == contentHash &&
          it.renderKey == renderKey &&
          it.priority.rank > desiredPriority.rank
      } as? RenderViewJob ?: return
    if (queue.remove(queued)) {
      val promoted =
        RenderViewJob(
          sourceKey = queued.sourceKey,
          contentHash = queued.contentHash,
          renderKey = queued.renderKey,
          priority = desiredPriority,
          seq = seqGen.incrementAndGet(),
          cancellableOnNewSave = desiredPriority != QueuePriority.AGENT,
          entry = queued.entry,
          compiled = queued.compiled,
          direction = queued.direction,
          screenId = queued.screenId,
          prefix = queued.prefix,
          overlayId = queued.overlayId,
          view = queued.view,
          surface = queued.surface,
          densityScale = queued.densityScale,
          baseCheck = queued.baseCheck,
          future = queued.future,
        )
      queue.put(promoted)
    }
  }

  private inner class RenderViewJob(
    sourceKey: String,
    contentHash: String,
    val renderKey: RenderKey,
    priority: QueuePriority,
    seq: Long,
    cancellableOnNewSave: Boolean,
    val entry: CompileEntry,
    val compiled: CompileResult.Compiled,
    val direction: DirectionSpec,
    val screenId: String,
    val prefix: String,
    val overlayId: String?,
    val view: ViewSpec,
    val surface: SurfaceSpec,
    val densityScale: Int,
    val baseCheck: ViewCheck?,
    val future: CompletableFuture<RenderArtifact>,
  ) : QueueJob(sourceKey, contentHash, priority, seq, cancellableOnNewSave) {
    override fun cancel(reason: String) {
      synchronized(cacheLock) {
        val current = cachesByFile[sourceKey]
        if (current != null && current.contentHash == contentHash) {
          current.renderFutures.remove(renderKey, future)
        }
      }
      future.completeExceptionally(CancellationException(reason))
    }

    override fun execute() {
      if (cancelled.get()) {
        cancel("cancelled")
        return
      }
      try {
        val artifact =
          executeSingleRender(
            contentHash = contentHash,
            compiled = compiled,
            direction = direction,
            screenId = screenId,
            prefix = prefix,
            overlayId = overlayId,
            view = view,
            designSystem = entry.loaded.designSystem,
            surface = surface,
            densityScale = densityScale,
            baseCheck = baseCheck,
            writeFiles = true,
          )
        synchronized(cacheLock) {
          val current = cachesByFile[sourceKey]
          if (current != null && current.contentHash == contentHash) {
            current.rendered[renderKey] = artifact
          }
        }
        try {
          onViewRendered(entry.sourcePath, contentHash, artifact)
        } finally {
          future.complete(artifact)
        }
      } catch (t: Throwable) {
        synchronized(cacheLock) {
          val current = cachesByFile[sourceKey]
          if (current != null && current.contentHash == contentHash) {
            current.renderFutures.remove(renderKey, future)
          }
        }
        future.completeExceptionally(t)
      }
    }
  }

  private fun executeSingleRender(
    contentHash: String,
    compiled: CompileResult.Compiled,
    direction: DirectionSpec,
    screenId: String,
    prefix: String,
    overlayId: String?,
    view: ViewSpec,
    designSystem: DesignSystem,
    surface: SurfaceSpec,
    densityScale: Int,
    baseCheck: ViewCheck?,
    writeFiles: Boolean,
  ): RenderArtifact {
    val artifactName = overlayId?.let { ViewCheck.renderOverlayName(prefix, it) } ?: prefix
    val scope = RenderKey(direction.name, screenId, overlayId, view.name, densityScale).scopeLabel()
    val startNs = System.nanoTime()
    val headless =
      try {
        runWithWatchdog(contentHash, scope, isCold = false) {
          renderer.render(
            compiled = compiled,
            direction = direction,
            screenId = screenId,
            view = view,
            designSystem = designSystem,
            overlayId = overlayId,
            density = densityScale.toFloat(),
          )
        }
      } catch (e: Exception) {
        val elapsedMs = (System.nanoTime() - startNs) / 1_000_000L
        val failed = ViewCheck.renderError(e)
        return RenderArtifact(
          direction = direction.name,
          screen = screenId,
          overlay = overlayId,
          view = view,
          densityScale = densityScale,
          artifactName = artifactName,
          pass = false,
          renderMs = elapsedMs,
          check = failed,
          layout = null,
          tree = null,
          pngPath = null,
          endPngPath = null,
          pngBytes = null,
          endPngBytes = null,
          pngSha256 = null,
          entry = RenderEntry.of(artifactName, view.name, null, failed),
        )
      }
    val elapsedMs = (System.nanoTime() - startNs) / 1_000_000L
    val full = ViewCheck.of(headless.tree, surface)
    val check = if (baseCheck == null) full else full.overlaying(baseCheck)
    val layout = if (overlayId == null) LayoutSummary.of(headless.tree, surface) else null

    var pngPath: Path? = null
    var endPngPath: Path? = null
    if (writeFiles && headless.png.isNotEmpty()) {
      val hashDir = rendersRoot.resolve(contentHash.take(12))
      Files.createDirectories(hashDir)
      val baseFile = "${sanitizeFileName(artifactName)}-${view.name}-${densityScale}x"
      pngPath = hashDir.resolve("$baseFile.png").toAbsolutePath().normalize()
      Files.write(pngPath, headless.png)
      headless.end?.let { end ->
        if (end.png.isNotEmpty()) {
          endPngPath = hashDir.resolve("$baseFile-end.png").toAbsolutePath().normalize()
          Files.write(endPngPath, end.png)
        }
      }
    }
    val sha = if (headless.png.isNotEmpty()) ContentHasher.sha256Hex(headless.png) else null
    val baseName = if (pngPath != null) "${artifactName}-${view.name}" else null
    val endName = if (endPngPath != null) "${artifactName}-${view.name}-end" else null

    return RenderArtifact(
      direction = direction.name,
      screen = screenId,
      overlay = overlayId,
      view = view,
      densityScale = densityScale,
      artifactName = artifactName,
      pass = check.pass,
      renderMs = elapsedMs,
      check = check,
      layout = layout,
      tree = headless.tree,
      pngPath = pngPath,
      endPngPath = endPngPath,
      pngBytes = headless.png,
      endPngBytes = headless.end?.png,
      pngSha256 = sha,
      entry = RenderEntry.of(artifactName, view.name, baseName, check, endName),
    )
  }

  private inline fun <T> runWithWatchdog(
    contentHash: String,
    scope: String,
    isCold: Boolean,
    block: () -> T,
  ): T {
    val w = watchdog ?: return block()
    return w.guard(contentHash, scope, isCold, block)
  }

  private fun canonicalKey(path: Path): String = path.toAbsolutePath().normalize().toString()

  private fun sanitizeFileName(raw: String): String =
    raw.replace(Regex("""[^A-Za-z0-9._+-]+"""), "_").ifBlank { "render" }
}
