package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.cli.RenderEntry
import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.domain.layout.ViewLayout
import bot.charlies.androiddesign.preview.model.BoardMeta
import bot.charlies.androiddesign.preview.model.BoardSurface
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.ViewSpec
import java.nio.file.Path
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/** Job priority for the shared queue: agent > board focus > speculative > board remainder. */
enum class QueuePriority(val rank: Int) {
  AGENT(0),
  BOARD_FOCUS(1),
  SPECULATIVE(2),
  BOARD_REMAINDER(3),
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

/** One compiled board cached for a `(repoRoot, sourcePath, contentHash)`. */
data class CompileEntry(
  val sourcePath: Path,
  val designPath: Path?,
  val contentHash: String,
  val loaded: LoadedExploration,
  val fromCache: Boolean,
  val compileCountAtLoad: Int,
  val repoRoot: Path = ServerPaths.resolveRepoRoot(sourcePath),
  val surface: BoardSurface = BoardSurface.Phone,
  val boardName: String? = null,
  val boardMeta: BoardMeta? = null,
  val compiledData: CompiledBoardData? = null,
  val boardTheme: ThemeOverride? = null,
)

/** One completed 1-view render, cached by `(repoRoot, sourcePath, contentHash, RenderKey)`. */
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
  val imageMime: String = "image/png",
  val endTree: LayoutTree? = null,
)

/** Signals that an in-flight queue job was displaced by a newer server version. */
class ServerReplacedException(val replacedByVersion: String) :
  CancellationException("replaced by v$replacedByVersion, rerun")

/**
 * Shared content-hash result store and multi-repo priority queue.
 *
 * Every compile, 1-view render, override render, and batch check runs sequentially on one worker
 * thread guarded by [watchdog], so higher-priority agent jobs preempt background speculative or
 * board remainder work, and multiple repositories cleanly isolate caches and render directories.
 */
class RenderQueue(
  private val rendersRoot: Path,
  private val executor: CheckExecutor,
  private val cacheRoot: Path? = null,
  private val watchdog: Watchdog? = null,
  private val onViewRendered: (Path, Path, String, RenderArtifact) -> Unit = { _, _, _, _ -> },
) {

  private data class FileCacheKey(val repoKey: String, val sourceKey: String)

  private abstract class QueueJob(
    val repoKey: String,
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

    abstract fun cancel(cause: CancellationException)

    abstract fun execute()
  }

  private class InFlightRender(
    val includeFrame: Boolean,
    val future: CompletableFuture<RenderArtifact>,
  )

  private class FileHashCache(val contentHash: String) {
    @Volatile var compileFuture: CompletableFuture<CompileEntry>? = null
    @Volatile var compiled: CompileEntry? = null
    val renderFutures = ConcurrentHashMap<RenderKey, InFlightRender>()
    val rendered = ConcurrentHashMap<RenderKey, RenderArtifact>()

    /**
     * Caches [art] under its [RenderKey]. A render error is never cached: the next request must
     * render again instead of repeating a transient worker failure until the server restarts.
     */
    fun store(
      art: RenderArtifact,
      key: RenderKey =
        RenderKey(art.direction, art.screen, art.overlay, art.view.name, art.densityScale),
    ) {
      if (!art.check.isRenderError) rendered[key] = art
    }
  }

  private val queue = PriorityBlockingQueue<QueueJob>()
  private val seqGen = AtomicLong(0L)
  private val compileCounter = AtomicInteger(0)
  private val running = AtomicBoolean(false)
  @Volatile private var replacedVersion: String? = null
  private val cachesByFile = ConcurrentHashMap<FileCacheKey, FileHashCache>()
  private val cacheLock = Any()
  @Volatile private var workerThread: Thread? = null

  val compileCount: Int
    get() = compileCounter.get()

  fun rendersRootForRepo(repoRoot: Path): Path =
    if (cacheRoot != null) ServerPaths.rendersDir(repoRoot, cacheRoot) else rendersRoot

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
            val repVer = replacedVersion
            if (repVer != null) {
              job.cancel(ServerReplacedException(repVer))
              continue
            }
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

  fun cancelForReplacement(newVersion: String) {
    replacedVersion = newVersion
    val drained = ArrayList<QueueJob>()
    queue.drainTo(drained)
    drained.forEach { job ->
      if (job.cancelled.compareAndSet(false, true)) {
        job.cancel(ServerReplacedException(newVersion))
      }
    }
  }

  fun stop() {
    running.set(false)
    workerThread?.interrupt()
    workerThread = null
    val drained = ArrayList<QueueJob>()
    queue.drainTo(drained)
    val rep = replacedVersion
    drained.forEach {
      val ex =
        if (rep != null) ServerReplacedException(rep) else CancellationException("server stopped")
      it.cancel(ex)
    }
  }

  /**
   * Cancels queued background board/speculative jobs belonging to `(repoRoot, sourcePath)` for any
   * hash older than [newContentHash]. Running jobs finish cleanly.
   */
  fun cancelStaleJobs(repoRoot: Path, sourcePath: Path, newContentHash: String) {
    val rKey = canonicalKey(repoRoot)
    val sKey = canonicalKey(sourcePath)
    val toRemove = ArrayList<QueueJob>()
    for (job in queue) {
      if (
        job.repoKey == rKey &&
          job.sourceKey == sKey &&
          job.contentHash != newContentHash &&
          job.cancellableOnNewSave
      ) {
        if (job.cancelled.compareAndSet(false, true)) {
          toRemove += job
        }
      }
    }
    toRemove.forEach { job ->
      queue.remove(job)
      job.cancel(CancellationException("superseded by newer save"))
    }
  }

  fun cancelStaleJobs(sourcePath: Path, newContentHash: String) {
    cancelStaleJobs(ServerPaths.resolveRepoRoot(sourcePath), sourcePath, newContentHash)
  }

  fun latestCompiled(repoRoot: Path, sourcePath: Path): CompileEntry? =
    cachesByFile[cacheKey(repoRoot, sourcePath)]?.compiled

  fun latestCompiled(sourcePath: Path): CompileEntry? =
    latestCompiled(ServerPaths.resolveRepoRoot(sourcePath), sourcePath)

  fun isCurrentHash(repoRoot: Path, sourcePath: Path, contentHash: String): Boolean =
    synchronized(cacheLock) {
      cachesByFile[cacheKey(repoRoot, sourcePath)]?.contentHash == contentHash
    }

  fun isCurrentHash(sourcePath: Path, contentHash: String): Boolean =
    isCurrentHash(ServerPaths.resolveRepoRoot(sourcePath), sourcePath, contentHash)

  fun completedBoardArtifacts(repoRoot: Path, sourcePath: Path): Map<RenderKey, RenderArtifact> {
    val cache = cachesByFile[cacheKey(repoRoot, sourcePath)] ?: return emptyMap()
    return cache.rendered.filterKeys { it.densityScale == CheckExecutor.CHECK_DENSITY_SCALE }
  }

  fun completedBoardArtifacts(sourcePath: Path): Map<RenderKey, RenderArtifact> =
    completedBoardArtifacts(ServerPaths.resolveRepoRoot(sourcePath), sourcePath)

  fun submitCompile(
    repoRoot: Path,
    snapshot: ContentHasher.Snapshot,
    priority: QueuePriority,
  ): CompletableFuture<CompileEntry> {
    replacedVersion?.let {
      return CompletableFuture.failedFuture(ServerReplacedException(it))
    }
    val normRepo = repoRoot.toAbsolutePath().normalize()
    val normSource = snapshot.sourcePath.toAbsolutePath().normalize()
    val rKey = canonicalKey(normRepo)
    val sKey = canonicalKey(normSource)
    val key = FileCacheKey(rKey, sKey)
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
            repoKey = rKey,
            sourceKey = sKey,
            contentHash = snapshot.contentHash,
            priority = priority,
            seq = seqGen.incrementAndGet(),
            cancellableOnNewSave = priority != QueuePriority.AGENT,
          ) {
          override fun cancel(cause: CancellationException) {
            future.completeExceptionally(cause)
          }

          override fun execute() {
            if (cancelled.get()) {
              cancel(CancellationException("cancelled"))
              return
            }
            try {
              val isCold = compileCounter.get() == 0
              val output =
                runWithWatchdog(snapshot.contentHash, "compile", isCold, normRepo) {
                  executor.compile(snapshot)
                }
              val count = compileCounter.incrementAndGet()
              val entry =
                CompileEntry(
                  sourcePath = normSource,
                  designPath = snapshot.designPath,
                  contentHash = snapshot.contentHash,
                  loaded = output.loaded,
                  fromCache = false,
                  compileCountAtLoad = count,
                  repoRoot = normRepo,
                  surface = output.surface,
                  boardName = output.boardName,
                  boardMeta = output.boardMeta,
                  compiledData = output.compiledData,
                  boardTheme = output.boardTheme,
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

  fun submitCompile(
    snapshot: ContentHasher.Snapshot,
    priority: QueuePriority,
  ): CompletableFuture<CompileEntry> =
    submitCompile(ServerPaths.resolveRepoRoot(snapshot.sourcePath), snapshot, priority)

  /**
   * Executes one batch check across [targets] via [CheckExecutor] serialized through the shared
   * queue. Uncached/unselected PNGs remain unstored on disk while cached [RenderArtifact]s populate
   * [FileHashCache] for reuse.
   */
  fun submitBatchCheck(
    entry: CompileEntry,
    targets: List<CheckTargetSpec>,
    scoped: Boolean,
    priority: QueuePriority = QueuePriority.AGENT,
  ): CompletableFuture<List<RenderArtifact>> {
    replacedVersion?.let {
      return CompletableFuture.failedFuture(ServerReplacedException(it))
    }
    val key = FileCacheKey(canonicalKey(entry.repoRoot), canonicalKey(entry.sourcePath))
    val batchDensity = CheckExecutor.CHECK_DENSITY_SCALE
    synchronized(cacheLock) {
      val current = cachesByFile[key]
      if (current != null && current.contentHash == entry.contentHash) {
        val cached = targets.mapNotNull { t ->
          current.rendered[
              RenderKey(t.direction, t.screenId, t.overlayId, t.view.name, batchDensity)]
        }
        if (
          cached.size == targets.size &&
            CheckImagePolicy.hasRequiredPngs(cached, scoped, entry.surface)
        ) {
          return CompletableFuture.completedFuture(cached)
        }
      }
    }
    val future = CompletableFuture<List<RenderArtifact>>()
    val repoRenders = rendersRootForRepo(entry.repoRoot)
    val job =
      object :
        QueueJob(
          repoKey = key.repoKey,
          sourceKey = key.sourceKey,
          contentHash = entry.contentHash,
          priority = priority,
          seq = seqGen.incrementAndGet(),
          cancellableOnNewSave = false,
        ) {
        override fun cancel(cause: CancellationException) {
          future.completeExceptionally(cause)
        }

        override fun execute() {
          if (cancelled.get()) {
            cancel(CancellationException("cancelled"))
            return
          }
          try {
            val scope = if (scoped) "batch-scoped" else "batch-full"
            val artifacts =
              runWithWatchdog(
                contentHash = entry.contentHash,
                scope = scope,
                isCold = false,
                repoRoot = entry.repoRoot,
                minBudgetMs = if (scoped) null else Watchdog.BATCH_BUDGET_MS,
              ) {
                executor.renderBatchCheck(entry, targets, scoped, repoRenders)
              }
            synchronized(cacheLock) {
              val current = cachesByFile[key]
              if (current != null && current.contentHash == entry.contentHash) {
                artifacts.forEach(current::store)
              }
            }
            for (art in artifacts) {
              onViewRendered(entry.repoRoot, entry.sourcePath, entry.contentHash, art)
            }
            future.complete(artifacts)
          } catch (t: Throwable) {
            future.completeExceptionally(t)
          }
        }
      }
    queue.put(job)
    return future
  }

  /**
   * Executes one view-grouped PNG batch render for [ReportRenderer] serialized through the shared
   * queue. Reuses cached 2x PNG artifacts only when every requested target already has a cached PNG
   * and layout tree.
   */
  fun submitBatchReport(
    entry: CompileEntry,
    targets: List<CheckTargetSpec>,
    outDir: Path,
    priority: QueuePriority = QueuePriority.AGENT,
  ): CompletableFuture<List<RenderArtifact>> {
    replacedVersion?.let {
      return CompletableFuture.failedFuture(ServerReplacedException(it))
    }
    if (targets.isEmpty()) {
      return CompletableFuture.completedFuture(emptyList())
    }
    val key = FileCacheKey(canonicalKey(entry.repoRoot), canonicalKey(entry.sourcePath))
    synchronized(cacheLock) {
      val current = cachesByFile[key]
      if (current != null && current.contentHash == entry.contentHash) {
        val cached = targets.mapNotNull { t ->
          current.rendered[
              RenderKey(
                t.direction,
                t.screenId,
                t.overlayId,
                t.view.name,
                CheckExecutor.CHECK_DENSITY_SCALE,
              ),
            ]
            ?.takeIf { art ->
              art.tree != null && art.pngBytes != null && art.pngBytes.isNotEmpty()
            }
        }
        if (cached.size == targets.size) {
          return CompletableFuture.completedFuture(cached)
        }
      }
    }
    val future = CompletableFuture<List<RenderArtifact>>()
    val job =
      object :
        QueueJob(
          repoKey = key.repoKey,
          sourceKey = key.sourceKey,
          contentHash = entry.contentHash,
          priority = priority,
          seq = seqGen.incrementAndGet(),
          cancellableOnNewSave = false,
        ) {
        override fun cancel(cause: CancellationException) {
          future.completeExceptionally(cause)
        }

        override fun execute() {
          if (cancelled.get()) {
            cancel(CancellationException("cancelled"))
            return
          }
          try {
            val artifacts =
              runWithWatchdog(
                contentHash = entry.contentHash,
                scope = "batch-report",
                isCold = false,
                repoRoot = entry.repoRoot,
                minBudgetMs = Watchdog.BATCH_BUDGET_MS,
              ) {
                executor.renderBatchReport(
                  entry,
                  targets,
                  outDir,
                  densityScale = CheckExecutor.CHECK_DENSITY_SCALE,
                )
              }
            synchronized(cacheLock) {
              val current = cachesByFile[key]
              if (current != null && current.contentHash == entry.contentHash) {
                artifacts.forEach(current::store)
              }
            }
            for (art in artifacts) {
              onViewRendered(entry.repoRoot, entry.sourcePath, entry.contentHash, art)
            }
            future.complete(artifacts)
          } catch (t: Throwable) {
            future.completeExceptionally(t)
          }
        }
      }
    queue.put(job)
    return future
  }

  /**
   * Enqueues or shares a single 1-view render job for [entry] at [priority]. Identical `(repo,
   * file, hash, key)` requests share the completed or in-flight result.
   */
  fun submitSingleRender(
    entry: CompileEntry,
    direction: String,
    screenId: String,
    prefix: String,
    overlayId: String?,
    view: ViewSpec,
    densityScale: Int,
    baseCheck: ViewCheck?,
    priority: QueuePriority,
    writeFiles: Boolean = true,
    includeFrame: Boolean = true,
  ): CompletableFuture<RenderArtifact> {
    replacedVersion?.let {
      return CompletableFuture.failedFuture(ServerReplacedException(it))
    }
    val key = FileCacheKey(canonicalKey(entry.repoRoot), canonicalKey(entry.sourcePath))
    val renderKey = RenderKey(direction, screenId, overlayId, view.name, densityScale)
    if (entry.compiledData == null && entry.boardMeta == null) {
      return CompletableFuture.failedFuture(IllegalStateException("Exploration is not compiled"))
    }

    val future: CompletableFuture<RenderArtifact>
    var needEnqueue = false
    synchronized(cacheLock) {
      val currentCache = cachesByFile[key]
      if (currentCache != null && currentCache.contentHash != entry.contentHash) {
        return CompletableFuture.failedFuture(CancellationException("superseded by newer save"))
      }
      val hashCache =
        currentCache
          ?: FileHashCache(entry.contentHash).also {
            it.compiled = entry
            cachesByFile[key] = it
          }
      hashCache.rendered[renderKey]?.let { done ->
        val hasImageIfNeeded =
          !includeFrame || (done.pngBytes != null && done.pngBytes.isNotEmpty())
        if (hasImageIfNeeded && (!writeFiles || done.pngPath != null)) {
          return CompletableFuture.completedFuture(done)
        }
      }
      val existing = hashCache.renderFutures[renderKey]
      val compatibleInFlight =
        existing != null &&
          !existing.future.isCancelled &&
          !existing.future.isCompletedExceptionally &&
          (!includeFrame || existing.includeFrame)
      if (compatibleInFlight) {
        future = existing.future
        promoteQueuedJob(key, entry.contentHash, renderKey, priority)
      } else {
        future = CompletableFuture()
        hashCache.renderFutures[renderKey] = InFlightRender(includeFrame, future)
        needEnqueue = true
      }
    }

    if (needEnqueue) {
      val job =
        RenderViewJob(
          key = key,
          contentHash = entry.contentHash,
          renderKey = renderKey,
          priority = priority,
          seq = seqGen.incrementAndGet(),
          cancellableOnNewSave = priority != QueuePriority.AGENT,
          entry = entry,
          direction = direction,
          screenId = screenId,
          prefix = prefix,
          overlayId = overlayId,
          view = view,
          densityScale = densityScale,
          baseCheck = baseCheck,
          writeFiles = writeFiles,
          includeFrame = includeFrame,
          future = future,
        )
      queue.put(job)
    }
    return future
  }

  /**
   * Serializes a single ad-hoc override render (theme/seed/variant/dark/fontScale) through the
   * priority queue and [watchdog] without populating the shared render cache or disk artifacts.
   */
  fun renderOverride(
    entry: CompileEntry,
    direction: String,
    screenId: String,
    prefix: String,
    overlayId: String?,
    view: ViewSpec,
    densityScale: Int = CheckExecutor.CHECK_DENSITY_SCALE,
    includeFrame: Boolean = true,
    fontScale: Float = 1f,
    seedOverride: String? = null,
    variantOverride: String? = null,
    darkOverride: Boolean? = null,
  ): CompletableFuture<RenderArtifact> {
    replacedVersion?.let {
      return CompletableFuture.failedFuture(ServerReplacedException(it))
    }
    val key = FileCacheKey(canonicalKey(entry.repoRoot), canonicalKey(entry.sourcePath))
    val future = CompletableFuture<RenderArtifact>()
    val scope = RenderKey(direction, screenId, overlayId, view.name, densityScale).scopeLabel()
    val job =
      object :
        QueueJob(
          repoKey = key.repoKey,
          sourceKey = key.sourceKey,
          contentHash = entry.contentHash,
          priority = QueuePriority.BOARD_FOCUS,
          seq = seqGen.incrementAndGet(),
          cancellableOnNewSave = true,
        ) {
        override fun cancel(cause: CancellationException) {
          future.completeExceptionally(cause)
        }

        override fun execute() {
          if (cancelled.get()) {
            cancel(CancellationException("cancelled"))
            return
          }
          try {
            val artifact =
              runWithWatchdog(
                entry.contentHash,
                "$scope:override",
                isCold = false,
                entry.repoRoot,
              ) {
                executor.renderSingle(
                  entry = entry,
                  request =
                    SingleRenderRequest(
                      direction = direction,
                      screenId = screenId,
                      prefix = prefix,
                      overlayId = overlayId,
                      view = view,
                      densityScale = densityScale,
                      baseCheck = null,
                      writeFiles = false,
                      rendersRoot = rendersRootForRepo(entry.repoRoot),
                      includeFrame = includeFrame,
                      fontScale = fontScale,
                      seedOverride = seedOverride,
                      variantOverride = variantOverride,
                      darkOverride = darkOverride,
                    ),
                )
              }
            future.complete(artifact)
          } catch (t: Throwable) {
            future.completeExceptionally(t)
          }
        }
      }
    queue.put(job)
    return future
  }

  private fun promoteQueuedJob(
    key: FileCacheKey,
    contentHash: String,
    renderKey: RenderKey,
    desiredPriority: QueuePriority,
  ) {
    val queued =
      queue.firstOrNull {
        it is RenderViewJob &&
          it.repoKey == key.repoKey &&
          it.sourceKey == key.sourceKey &&
          it.contentHash == contentHash &&
          it.renderKey == renderKey &&
          it.priority.rank > desiredPriority.rank
      } as? RenderViewJob ?: return
    if (queue.remove(queued)) {
      val promoted =
        RenderViewJob(
          key = key,
          contentHash = queued.contentHash,
          renderKey = queued.renderKey,
          priority = desiredPriority,
          seq = seqGen.incrementAndGet(),
          cancellableOnNewSave = desiredPriority != QueuePriority.AGENT,
          entry = queued.entry,
          direction = queued.direction,
          screenId = queued.screenId,
          prefix = queued.prefix,
          overlayId = queued.overlayId,
          view = queued.view,
          densityScale = queued.densityScale,
          baseCheck = queued.baseCheck,
          writeFiles = queued.writeFiles,
          includeFrame = queued.includeFrame,
          future = queued.future,
        )
      queue.put(promoted)
    }
  }

  private inner class RenderViewJob(
    val key: FileCacheKey,
    contentHash: String,
    val renderKey: RenderKey,
    priority: QueuePriority,
    seq: Long,
    cancellableOnNewSave: Boolean,
    val entry: CompileEntry,
    val direction: String,
    val screenId: String,
    val prefix: String,
    val overlayId: String?,
    val view: ViewSpec,
    val densityScale: Int,
    val baseCheck: ViewCheck?,
    val writeFiles: Boolean,
    val includeFrame: Boolean,
    val future: CompletableFuture<RenderArtifact>,
  ) :
    QueueJob(
      key.repoKey,
      key.sourceKey,
      contentHash,
      priority,
      seq,
      cancellableOnNewSave,
    ) {
    override fun cancel(cause: CancellationException) {
      synchronized(cacheLock) {
        val current = cachesByFile[key]
        if (current != null && current.contentHash == contentHash) {
          val entry = current.renderFutures[renderKey]
          if (entry?.future === future) current.renderFutures.remove(renderKey, entry)
        }
      }
      future.completeExceptionally(cause)
    }

    override fun execute() {
      if (cancelled.get()) {
        cancel(CancellationException("cancelled"))
        return
      }
      try {
        val scope = renderKey.scopeLabel()
        val artifact =
          runWithWatchdog(contentHash, scope, isCold = false, repoRoot = entry.repoRoot) {
            executor.renderSingle(
              entry = entry,
              request =
                SingleRenderRequest(
                  direction = direction,
                  screenId = screenId,
                  prefix = prefix,
                  overlayId = overlayId,
                  view = view,
                  densityScale = densityScale,
                  baseCheck = baseCheck,
                  writeFiles = writeFiles,
                  rendersRoot = rendersRootForRepo(entry.repoRoot),
                  includeFrame = includeFrame,
                ),
            )
          }
        synchronized(cacheLock) {
          val current = cachesByFile[key]
          if (current != null && current.contentHash == contentHash) {
            val prev = current.rendered[renderKey]
            val keepExistingPng =
              !includeFrame && prev?.pngBytes != null && prev.pngBytes.isNotEmpty()
            if (!keepExistingPng && (includeFrame || prev == null)) {
              current.store(artifact, renderKey)
            }
            if (artifact.check.isRenderError) {
              val inFlight = current.renderFutures[renderKey]
              if (inFlight?.future === future) current.renderFutures.remove(renderKey, inFlight)
            }
          }
        }
        try {
          onViewRendered(entry.repoRoot, entry.sourcePath, contentHash, artifact)
        } finally {
          future.complete(artifact)
        }
      } catch (t: Throwable) {
        synchronized(cacheLock) {
          val current = cachesByFile[key]
          if (current != null && current.contentHash == contentHash) {
            val entry = current.renderFutures[renderKey]
            if (entry?.future === future) current.renderFutures.remove(renderKey, entry)
          }
        }
        future.completeExceptionally(t)
      }
    }
  }

  private inline fun <T> runWithWatchdog(
    contentHash: String,
    scope: String,
    isCold: Boolean,
    repoRoot: Path,
    minBudgetMs: Long? = null,
    block: () -> T,
  ): T {
    // Warm a fresh worker first, outside the budget, so a cold start is not charged to this job.
    executor.prepare()
    val w = watchdog ?: return block()
    return w.guard(contentHash, scope, isCold, repoRoot.toString(), minBudgetMs, block)
  }

  private fun cacheKey(repoRoot: Path, sourcePath: Path): FileCacheKey =
    FileCacheKey(canonicalKey(repoRoot), canonicalKey(sourcePath))

  private fun canonicalKey(path: Path): String = path.toAbsolutePath().normalize().toString()
}
