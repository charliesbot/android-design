package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.model.BoardSurface
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.ValidationError
import bot.charlies.androiddesign.preview.model.ViewSpec
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/** Per-repository server state shared by the HTTP routes and [CheckService]. */
internal class RepoState(
  val repoId: String,
  val repoRoot: Path,
) {
  @Volatile var activeSourcePath: Path? = null
  @Volatile var activeDesignPath: Path? = null
  @Volatile var currentFocus: PreviewHttpServer.FocusTarget? = null
  @Volatile var latestBoardPayload: BoardPayload? = null
  val lastBoardQueuedHash = ConcurrentHashMap<String, String>()
}

/** Server-owned capabilities [CheckService] needs while orchestrating a check. */
internal interface CheckHost {
  val recordPath: Path

  fun repoState(repoRoot: Path): RepoState

  fun boardUrl(repoRoot: Path): String

  fun markActiveRepo(repoId: String)

  fun markLiveOwner(repoId: String)

  fun scheduleLiveUpdate(entry: CompileEntry, update: LivePreviewUpdate)

  fun clearTimeoutsIfHashChanged(repoRoot: Path, newHash: String)

  fun watchFile(repoId: String, sourcePath: Path, designPath: Path?)

  fun hasBoardListeners(repoId: String): Boolean

  fun <T> awaitOrReplaced(future: CompletableFuture<T>): T

  fun broadcastSse(targetRepoId: String?, event: String, dataJson: String)

  fun buildBoardPayload(
    repoId: String,
    entry: CompileEntry,
    artifacts: List<RenderArtifact>,
    staleViews: List<BoardViewRef>,
    overallPass: Boolean? = null,
  ): BoardPayload

  fun buildStaleViewRefs(entry: CompileEntry?): List<BoardViewRef>

  fun enqueueBoardRenders(state: RepoState, compiledEntry: CompileEntry, overallPass: Boolean?)

  fun handleWatchdogTimeout(job: Watchdog.ActiveJob)

  fun maybeScheduleRecycle()
}

/**
 * Orchestrates one `/api/check` request: validation, compile, batch or single renders, timeout
 * recording, board payload updates, and the [CheckResponse]. Responses go out through the `reply`
 * callback so the caller owns HTTP transport.
 */
internal class CheckService(
  private val host: CheckHost,
  private val renderQueue: RenderQueue,
  private val executor: CheckExecutor,
  private val selectionStore: SelectionStore,
  private val warmBudgetMs: Long,
) {
  fun run(req: CheckRequest, reply: (status: Int, json: String) -> Unit) {
    fun replyError(status: Int, envelope: ErrorEnvelope) =
      reply(status, PreviewJson.compact.encodeToString(ErrorEnvelope.serializer(), envelope))

    val sourcePath = Path.of(req.path).toAbsolutePath().normalize()
    val reqRepoRoot =
      req.repoRoot?.takeIf { it.isNotBlank() }?.let(Path::of)
        ?: ServerPaths.resolveRepoRoot(sourcePath)
    val state = host.repoState(reqRepoRoot)
    val repoBoardUrl = host.boardUrl(state.repoRoot)
    host.markActiveRepo(state.repoId)

    if (!Files.isRegularFile(sourcePath)) {
      replyError(
        404,
        ErrorEnvelope(error = "no_such_file", message = "No such file: $sourcePath"),
      )
      return
    }
    val scope = req.scope()
    val scoped = scope.isScoped
    if (!sourcePath.fileName.toString().endsWith(".explore.kt")) {
      val resp =
        CheckResponse(
          ok = false,
          valid = false,
          pass = false,
          scoped = scoped,
          sourcePath = sourcePath.toString(),
          boardUrl = repoBoardUrl,
          errors =
            listOf(
              ValidationError(sourcePath.toString(), "Expected an .explore.kt exploration file"),
            ),
        )
      reply(
        200,
        PreviewJson.compact.encodeToString(CheckResponse.serializer(), resp),
      )
      return
    }

    val explicitDesign = req.design?.takeIf { it.isNotBlank() }?.let(Path::of)
    val snapshot = ContentHasher.snapshot(sourcePath, explicitDesign)
    val scopeKey = scope.key

    val rec = ServerPaths.readRecord(host.recordPath)
    if (rec != null && rec.hasTimeout(snapshot.contentHash, scope)) {
      val resp =
        CheckResponse(
          ok = false,
          valid = false,
          pass = false,
          scoped = scoped,
          contentHash = snapshot.contentHash,
          sourcePath = sourcePath.toString(),
          designPath = snapshot.designPath?.toString(),
          boardUrl = repoBoardUrl,
          error = "timeout",
          tell =
            "Previous attempt for this exact file timed out ($scopeKey). Edit and save the file to clear the timeout.",
        )
      reply(
        200,
        PreviewJson.compact.encodeToString(CheckResponse.serializer(), resp),
      )
      return
    }

    host.clearTimeoutsIfHashChanged(state.repoRoot, snapshot.contentHash)
    val previousHash = renderQueue.latestCompiled(state.repoRoot, sourcePath)?.contentHash
    if (previousHash != null && previousHash != snapshot.contentHash) {
      renderQueue.cancelStaleJobs(state.repoRoot, sourcePath, snapshot.contentHash)
    }

    state.activeSourcePath = sourcePath
    state.activeDesignPath = snapshot.designPath
    host.watchFile(state.repoId, sourcePath, snapshot.designPath)

    val compileEntry =
      try {
        host.awaitOrReplaced(
          renderQueue.submitCompile(state.repoRoot, snapshot, QueuePriority.AGENT),
        )
      } catch (e: ServerReplacedException) {
        sendReplacedCheckResponse(reply, scoped, sourcePath, e.replacedByVersion)
        return
      } catch (e: Exception) {
        val cause = e.cause ?: e
        replyError(
          500,
          ErrorEnvelope(error = "compile_error", message = cause.message ?: "Compilation failed"),
        )
        return
      }

    val loaded = compileEntry.loaded
    val boardMeta = compileEntry.boardMeta
    if (boardMeta == null || loaded.errors.isNotEmpty()) {
      val pendingSel = selectionStore.consumePending(sourcePath)
      val boardPayload =
        host.buildBoardPayload(state.repoId, compileEntry, emptyList(), emptyList())
      state.latestBoardPayload = boardPayload
      host.broadcastSse(
        state.repoId,
        "board",
        PreviewJson.compact.encodeToString(BoardPayload.serializer(), boardPayload),
      )
      val resp =
        CheckResponse(
          ok = false,
          valid = false,
          pass = false,
          scoped = scoped,
          contentHash = snapshot.contentHash,
          compileMs = loaded.compileMs,
          compileCount = renderQueue.compileCount,
          cached = compileEntry.fromCache,
          surface = if (compileEntry.surface == BoardSurface.Wear) "wear" else "phone",
          boardName = compileEntry.boardName,
          sourcePath = sourcePath.toString(),
          designPath = loaded.designPath?.toString(),
          boardUrl = repoBoardUrl,
          errors = loaded.errors,
          selection = pendingSel,
        )
      reply(
        200,
        PreviewJson.compact.encodeToString(CheckResponse.serializer(), resp),
      )
      host.maybeScheduleRecycle()
      return
    }

    val directionFilter = req.direction?.takeIf { it.isNotBlank() }?.let { setOf(it.trim()) }
    if (directionFilter != null && boardMeta.directions.none { it.name in directionFilter }) {
      val known = boardMeta.directions.joinToString { it.name }
      replyError(
        400,
        ErrorEnvelope(
          error = "unknown_direction",
          message = "Unknown direction '${req.direction}' (available: $known)",
        ),
      )
      return
    }
    val screenFilter = req.screen?.takeIf { it.isNotBlank() }?.let { listOf(it.trim()) }
    val targets = resolveMetaTargets(boardMeta.directions, screenFilter, directionFilter)
    if (targets.isEmpty()) {
      replyError(
        400,
        ErrorEnvelope(
          error = "unknown_screen",
          message = "No matching directions or screens selected",
        ),
      )
      return
    }
    val views =
      if (!req.view.isNullOrBlank()) {
        val wanted = req.view.trim()
        val match = boardMeta.views.firstOrNull { it.name == wanted }
        if (match == null) {
          val known = boardMeta.views.joinToString { it.name }
          replyError(
            400,
            ErrorEnvelope(
              error = "unknown_view",
              message = "Unknown view '$wanted' (available: $known)",
            ),
          )
          return
        }
        listOf(match)
      } else {
        boardMeta.views
      }

    if (state.currentFocus == null) {
      state.currentFocus =
        PreviewHttpServer.FocusTarget(
          direction = targets.first().direction.name,
          screen = targets.first().screenId,
          overlay = null,
          view = views.first().name,
        )
    }

    val renderStartNs = System.nanoTime()
    val planned = planBatchTargets(targets, views, includeOverlays = !scoped)
    val artifacts =
      try {
        host.awaitOrReplaced(
          renderQueue.submitBatchCheck(compileEntry, planned, scoped, QueuePriority.AGENT),
        )
      } catch (e: ServerReplacedException) {
        sendReplacedCheckResponse(reply, scoped, sourcePath, e.replacedByVersion)
        return
      } catch (e: SandboxWatchdogException) {
        host.handleWatchdogTimeout(
          Watchdog.ActiveJob(
            contentHash = snapshot.contentHash,
            scope = scopeKey,
            budgetMs = warmBudgetMs,
            startedAtMs = System.currentTimeMillis(),
            repoRoot = state.repoRoot.toString(),
          ),
        )
        val resp =
          CheckResponse(
            ok = false,
            valid = false,
            pass = false,
            scoped = scoped,
            contentHash = snapshot.contentHash,
            sourcePath = sourcePath.toString(),
            designPath = snapshot.designPath?.toString(),
            boardUrl = repoBoardUrl,
            error = "timeout",
            tell = e.message ?: "Render timed out inside Android sandbox",
          )
        reply(
          200,
          PreviewJson.compact.encodeToString(CheckResponse.serializer(), resp),
        )
        return
      }
    val totalRenderMs = (System.nanoTime() - renderStartNs) / 1_000_000L

    val allPass = artifacts.all { it.pass }
    val selectedImages = selectImagesForResponse(artifacts, scoped, allPass, compileEntry.surface)
    val summaries = artifacts.map { art ->
      CheckRenderSummary(
        direction = art.direction,
        screen = art.screen,
        overlay = art.overlay,
        view = art.view.name,
        pass = art.pass,
        renderMs = art.renderMs,
        check = art.check,
        failures = art.entry.failures,
      )
    }

    val fileKey = sourcePath.toString()
    val hasBoardListeners = host.hasBoardListeners(state.repoId)
    val needBackgroundBoard =
      scoped && hasBoardListeners && state.lastBoardQueuedHash[fileKey] != snapshot.contentHash
    if (!scoped) {
      state.lastBoardQueuedHash[fileKey] = snapshot.contentHash
    }
    val boardPayload =
      host.buildBoardPayload(
        state.repoId,
        compileEntry,
        artifacts,
        emptyList(),
        overallPass = allPass,
      )
    state.latestBoardPayload = boardPayload
    if (!needBackgroundBoard && hasBoardListeners) {
      host.broadcastSse(
        state.repoId,
        "board",
        PreviewJson.compact.encodeToString(BoardPayload.serializer(), boardPayload),
      )
    }

    val pendingSelection = selectionStore.consumePending(sourcePath)
    val resp =
      CheckResponse(
        ok = allPass,
        valid = true,
        pass = allPass,
        scoped = scoped,
        contentHash = snapshot.contentHash,
        compileMs = loaded.compileMs,
        renderMs = totalRenderMs,
        compileCount = renderQueue.compileCount,
        cached = compileEntry.fromCache,
        surface = if (compileEntry.surface == BoardSurface.Wear) "wear" else "phone",
        boardName = compileEntry.boardName,
        sourcePath = sourcePath.toString(),
        designPath = loaded.designPath?.toString(),
        boardUrl = repoBoardUrl,
        errors = emptyList(),
        renders = summaries,
        images = selectedImages,
        selection = pendingSelection,
      )
    reply(200, PreviewJson.compact.encodeToString(CheckResponse.serializer(), resp))

    host.markLiveOwner(state.repoId)
    host.scheduleLiveUpdate(
      entry = compileEntry,
      update =
        LivePreviewUpdate(
          direction = targets.first().direction.name,
          screenId = targets.first().screenId,
          overlayId = null,
        ),
    )

    if (needBackgroundBoard) {
      host.broadcastSse(
        state.repoId,
        "stale",
        PreviewJson.compact.encodeToString(
          StaleEventPayload.serializer(),
          StaleEventPayload(
            repoId = state.repoId,
            contentHash = snapshot.contentHash,
            sourcePath = fileKey,
            views = host.buildStaleViewRefs(compileEntry),
          ),
        ),
      )
      host.enqueueBoardRenders(state, compileEntry, overallPass = allPass)
    } else {
      host.maybeScheduleRecycle()
    }
  }

  private fun sendReplacedCheckResponse(
    reply: (Int, String) -> Unit,
    scoped: Boolean,
    sourcePath: Path,
    replacedByVersion: String,
  ) {
    val msg = "replaced by v$replacedByVersion, rerun"
    val resp =
      CheckResponse(
        ok = false,
        valid = false,
        pass = false,
        scoped = scoped,
        sourcePath = sourcePath.toString(),
        error = "server_replaced",
        tell = msg,
      )
    reply(409, PreviewJson.compact.encodeToString(CheckResponse.serializer(), resp))
  }

  companion object {
    /**
     * Image output policy (`visual-design-exploration-v7.md`):
     * - Scoped checks always return their PNG path(s), whether findings pass or fail.
     * - Failing full checks return at most 2 failing view paths, deduplicating identical PNGs.
     * - Passing full checks return `light` and `desktop` (`watch` and `watch-large` on Wear).
     */
    fun selectImagesForResponse(
      artifacts: List<RenderArtifact>,
      scoped: Boolean,
      allPass: Boolean,
      surface: BoardSurface,
    ): List<ImageOutput> {
      fun toOutput(art: RenderArtifact): ImageOutput? {
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
      if (scoped) {
        return artifacts.mapNotNull(::toOutput)
      }
      if (!allPass) {
        val seenHashes = LinkedHashSet<String>()
        val selected = mutableListOf<ImageOutput>()
        for (art in artifacts) {
          if (art.pass) continue
          val out = toOutput(art) ?: continue
          val hash = art.pngSha256
          if (hash != null && !seenHashes.add(hash)) continue
          selected += out
          if (selected.size >= 2) break
        }
        return selected
      }
      val primaryViews = CheckExecutor.primaryCheckViews(surface)
      val seenHashes = LinkedHashSet<String>()
      return buildList {
        for (art in artifacts) {
          if (art.overlay != null || art.view.name !in primaryViews) continue
          val out = toOutput(art) ?: continue
          val hash = art.pngSha256
          if (hash != null && !seenHashes.add(hash)) continue
          add(out)
        }
      }
    }
  }
}

private val SANITIZE_SEGMENT_REGEX = Regex("""[^A-Za-z0-9._-]+""")

internal fun sanitizeSegment(name: String): String =
  name.trim().replace(SANITIZE_SEGMENT_REGEX, "_").ifBlank { "direction" }

internal data class MetaScreenTarget(
  val direction: DirectionMeta,
  val screenId: String,
  val prefix: String,
)

internal fun resolveMetaTargets(
  directions: List<DirectionMeta>,
  screenFilter: List<String>?,
  directionFilter: Set<String>? = null,
): List<MetaScreenTarget> {
  val activeDirections =
    if (directionFilter.isNullOrEmpty()) directions
    else directions.filter { it.name in directionFilter }
  val singleDirection = activeDirections.size == 1 && directions.size == 1
  val filterSet = screenFilter?.toSet()
  return buildList {
    for (dir in activeDirections) {
      val sanitizedDir = sanitizeSegment(dir.name)
      for (screenId in dir.screens) {
        val prefix = if (singleDirection) screenId else "${sanitizedDir}__$screenId"
        val slashName = "${dir.name}/$screenId"
        if (
          filterSet == null ||
            screenId in filterSet ||
            prefix in filterSet ||
            slashName in filterSet
        ) {
          add(MetaScreenTarget(dir, screenId, prefix))
        }
      }
    }
  }
}

/**
 * Expands [targets] x [views] (plus overlays when [includeOverlays] is true) in deterministic
 * canonical order (`direction/screen -> base views -> overlays -> views`).
 */
internal fun planBatchTargets(
  targets: List<MetaScreenTarget>,
  views: List<ViewSpec>,
  includeOverlays: Boolean,
): List<CheckTargetSpec> = buildList {
  for (target in targets) {
    for (view in views) {
      add(
        CheckTargetSpec(
          direction = target.direction.name,
          screenId = target.screenId,
          prefix = target.prefix,
          overlayId = null,
          view = view,
        ),
      )
    }
    if (includeOverlays) {
      for (overlay in target.direction.overlaysFor(target.screenId)) {
        for (view in views) {
          add(
            CheckTargetSpec(
              direction = target.direction.name,
              screenId = target.screenId,
              prefix = target.prefix,
              overlayId = overlay.id,
              view = view,
            ),
          )
        }
      }
    }
  }
}

/**
 * Orders batch render targets by `(view.width, view.height, view.name)` so consecutive renders
 * reuse `ComposeView` and `ImageReader` across directions, while keeping every base screen ahead of
 * its overlays and preserving deterministic relative order within each group.
 */
internal fun orderTargetsForBatchRender(targets: List<CheckTargetSpec>): List<CheckTargetSpec> =
  targets
    .withIndex()
    .sortedWith(
      compareBy<IndexedValue<CheckTargetSpec>>(
        { it.value.view.width },
        { it.value.view.height },
        { it.value.view.name },
        { it.value.overlayId != null },
        { it.index },
      ),
    )
    .map { it.value }
