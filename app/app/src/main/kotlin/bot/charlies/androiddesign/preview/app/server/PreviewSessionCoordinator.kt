package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.app.cli.ReportTheme
import bot.charlies.androiddesign.preview.app.client.ClientExitCode
import bot.charlies.androiddesign.preview.model.BoardSurface
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.ValidationError
import java.nio.file.Files
import java.nio.file.Path
import java.util.Base64
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** Per-repository session state tracked across check, board, selection, and file-watch events. */
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

/**
 * Typed outcomes returned by [PreviewSessionCoordinator] without coupling domain orchestration to
 * HTTP transport details.
 */
internal sealed interface CoordinatorOutcome<out T> {
  data class Success<T>(
    val status: Int,
    val payload: T,
    val afterSend: () -> Unit = {},
  ) : CoordinatorOutcome<T>

  data class Failure(
    val status: Int,
    val error: ErrorEnvelope,
  ) : CoordinatorOutcome<Nothing>
}

internal sealed interface CheckOutcome {
  data class Reply(
    val status: Int,
    val response: CheckResponse,
    val afterSend: () -> Unit = {},
  ) : CheckOutcome

  data class HttpError(
    val status: Int,
    val error: ErrorEnvelope,
  ) : CheckOutcome
}

internal sealed interface SelectionOutcome {
  data class Found(val response: SelectionResponse) : SelectionOutcome

  data class Missing(val response: SelectionResponse) : SelectionOutcome
}

/**
 * Owns all preview session state and orchestration: multi-repo registry, check execution, report
 * rendering, tree inspection, interactive board rendering/focus/selection, file watching, live
 * device update coalescing, timeout recording, and recycle scheduling.
 */
internal class PreviewSessionCoordinator(
  repoRoot: Path,
  private val cacheRoot: Path = ServerPaths.cacheRoot(),
  val recordPath: Path = ServerPaths.sharedRecordPath(cacheRoot),
  private val maxCompilesBeforeRecycle: Int = PreviewHttpServer.MAX_COMPILES_BEFORE_RECYCLE,
  coldBudgetMs: Long = Watchdog.COLD_BUDGET_MS,
  private val warmBudgetMs: Long = Watchdog.WARM_BUDGET_MS,
  worker: RenderWorker,
  private val liveSessionHost: LiveSessionHost = NoOpLiveSessionHost,
  private val onWatchdogHalt: () -> Unit = { Runtime.getRuntime().halt(1) },
  private val enableFileWatcher: Boolean = true,
  private val boardUrlForRepoId: (String?) -> String = { "" },
  private val broadcastSse: (targetRepoId: String?, event: String, dataJson: String) -> Unit =
    { _, _, _ ->
    },
  private val hasBoardListeners: (repoId: String) -> Boolean = { false },
  private val onRecycleRequested: () -> Unit = {},
) {
  val defaultRepoRoot: Path = repoRoot.toAbsolutePath().normalize()
  val defaultRepoId: String = ServerPaths.repoHash(defaultRepoRoot)
  val rendersRoot: Path = ServerPaths.rendersDir(defaultRepoRoot, cacheRoot)
  val selectionStore = SelectionStore()

  private val closed = AtomicBoolean(false)
  private val recycleScheduled = AtomicBoolean(false)
  private val replacementSignal = CompletableFuture<String>()
  private val inFlightChecks = AtomicInteger(0)
  private val repos = ConcurrentHashMap<String, RepoState>()
  @Volatile private var lastActiveRepoId: String = defaultRepoId

  val wasReplaced: Boolean
    get() = replacementSignal.isDone

  val activeInFlightChecks: Int
    get() = inFlightChecks.get()

  val executor = CheckExecutor(worker)

  private val watchdog =
    Watchdog(
      coldBudgetMs = coldBudgetMs,
      warmBudgetMs = warmBudgetMs,
      onTimeout = { job -> handleWatchdogTimeout(job) },
      onHalt = {
        runCatching { executor.close() }
        onWatchdogHalt()
      },
    )

  val renderQueue: RenderQueue =
    RenderQueue(
      rendersRoot = rendersRoot,
      executor = executor,
      cacheRoot = cacheRoot,
      watchdog = watchdog,
      onViewRendered = { rRoot, sourcePath, contentHash, artifact ->
        handleViewRendered(rRoot, sourcePath, contentHash, artifact)
      },
    )

  private val fileWatcher = FileWatcher { src, design ->
    val repoState = resolveRepoForSource(src)
    if (hasBoardListeners(repoState.repoId)) {
      handleFileChanged(repoState, src, design)
    } else {
      handleSpeculativePrecompile(repoState, src, design)
    }
  }

  private val liveUpdateExecutor = Executors.newSingleThreadExecutor { r ->
    Thread(r, "android-design-live-update").apply { isDaemon = true }
  }
  private val pendingLiveUpdate = AtomicReference<Pair<CompileEntry, LivePreviewUpdate>?>(null)

  init {
    repos[defaultRepoId] = RepoState(defaultRepoId, defaultRepoRoot)
  }

  fun start() {
    watchdog.start()
    renderQueue.start()
  }

  fun stop() {
    if (!closed.compareAndSet(false, true)) return
    fileWatcher.stop()
    watchdog.stop()
    renderQueue.stop()
    runCatching { executor.close() }
    runCatching { liveSessionHost.close() }
    pendingLiveUpdate.set(null)
    liveUpdateExecutor.shutdownNow()
  }

  fun markReplaced(repVersion: String) {
    replacementSignal.complete(repVersion)
    renderQueue.cancelForReplacement(repVersion)
  }

  fun awaitReplacementDrain(timeoutMs: Long) {
    val waitDeadline = System.currentTimeMillis() + timeoutMs
    while (inFlightChecks.get() > 0 && System.currentTimeMillis() < waitDeadline) {
      try {
        Thread.sleep(5)
      } catch (_: InterruptedException) {
        break
      }
    }
  }

  fun boardUrl(repoRootForUrl: Path? = null): String {
    val rid = repoRootForUrl?.let { ServerPaths.repoHash(it.toAbsolutePath().normalize()) }
    return boardUrlForRepoId(rid)
  }

  fun status(
    requestedRepoId: String?,
    boundPort: Int,
    startedAt: Long,
    runtimeIdentity: RuntimeIdentity?,
  ): StatusResponse {
    val reqRepo =
      if (requestedRepoId != null) {
        repos[requestedRepoId]
      } else {
        repos[lastActiveRepoId] ?: repos[defaultRepoId]
      }
    val url =
      when {
        reqRepo != null -> boardUrlForRepoId(reqRepo.repoId)
        requestedRepoId != null -> boardUrlForRepoId(requestedRepoId)
        else -> boardUrlForRepoId(null)
      }
    val reportedRepoRoot =
      when {
        reqRepo != null -> reqRepo.repoRoot.toString()
        requestedRepoId != null -> ""
        else -> defaultRepoRoot.toString()
      }
    return StatusResponse(
      pid = ProcessHandle.current().pid(),
      port = boundPort,
      jarVersion = AppInfo.VERSION,
      startedAt = startedAt,
      compileCount = renderQueue.compileCount,
      repoRoot = reportedRepoRoot,
      boardUrl = url,
      activeFile = reqRepo?.activeSourcePath?.toString(),
      backend = executor.name,
      runtimeHash = runtimeIdentity?.runtimeHash,
      deviceSerial = liveSessionHost.deviceSerial,
      adbForwardPort = liveSessionHost.adbForwardPort,
      activeRepos = repos.values.map { it.repoRoot.toString() }.sorted(),
    )
  }

  fun check(req: CheckRequest): CheckOutcome {
    inFlightChecks.incrementAndGet()
    try {
      return runCheck(req)
    } finally {
      inFlightChecks.decrementAndGet()
    }
  }

  private fun runCheck(req: CheckRequest): CheckOutcome {
    val sourcePath = Path.of(req.path).toAbsolutePath().normalize()
    val reqRepoRoot =
      req.repoRoot?.takeIf { it.isNotBlank() }?.let(Path::of)
        ?: ServerPaths.resolveRepoRoot(sourcePath)
    val state = getOrCreateRepo(reqRepoRoot)
    val repoBoardUrl = boardUrl(state.repoRoot)
    lastActiveRepoId = state.repoId

    if (!Files.isRegularFile(sourcePath)) {
      return CheckOutcome.HttpError(
        404,
        ErrorEnvelope(error = "no_such_file", message = "No such file: $sourcePath"),
      )
    }
    val scope = req.scope()
    val scoped = scope.isScoped
    if (!sourcePath.fileName.toString().endsWith(".explore.kt")) {
      return CheckOutcome.Reply(
        200,
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
        ),
      )
    }

    val explicitDesign = req.design?.takeIf { it.isNotBlank() }?.let(Path::of)
    val snapshot = ContentHasher.snapshot(sourcePath, explicitDesign)
    val scopeKey = scope.key

    val rec = ServerPaths.readRecord(recordPath)
    if (rec != null && rec.hasTimeout(snapshot.contentHash, scope)) {
      return CheckOutcome.Reply(
        200,
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
        ),
      )
    }

    clearTimeoutsIfHashChanged(state.repoRoot, snapshot.contentHash)
    val previousHash = renderQueue.latestCompiled(state.repoRoot, sourcePath)?.contentHash
    if (previousHash != null && previousHash != snapshot.contentHash) {
      renderQueue.cancelStaleJobs(state.repoRoot, sourcePath, snapshot.contentHash)
    }

    state.activeSourcePath = sourcePath
    state.activeDesignPath = snapshot.designPath
    if (enableFileWatcher) fileWatcher.watchForRepo(state.repoId, sourcePath, snapshot.designPath)

    val compileEntry =
      try {
        awaitOrReplaced(renderQueue.submitCompile(state.repoRoot, snapshot, QueuePriority.AGENT))
      } catch (e: ServerReplacedException) {
        return replacedCheckOutcome(scoped, sourcePath, e.replacedByVersion)
      } catch (e: Exception) {
        val cause = e.cause ?: e
        return CheckOutcome.HttpError(
          500,
          ErrorEnvelope(error = "compile_error", message = cause.message ?: "Compilation failed"),
        )
      }

    val loaded = compileEntry.loaded
    val boardMeta = compileEntry.boardMeta
    if (boardMeta == null || loaded.errors.isNotEmpty()) {
      val pendingSel = selectionStore.consumePending(sourcePath)
      val boardPayload = buildBoardPayload(state.repoId, compileEntry, emptyList(), emptyList())
      state.latestBoardPayload = boardPayload
      broadcastSse(
        state.repoId,
        "board",
        PreviewJson.compact.encodeToString(BoardPayload.serializer(), boardPayload),
      )
      return CheckOutcome.Reply(
        status = 200,
        response =
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
          ),
        afterSend = { maybeScheduleRecycle() },
      )
    }

    val selection =
      when (
        val resolved =
          TargetResolver.resolveCheckTargets(
            meta = boardMeta,
            direction = req.direction,
            screen = req.screen,
            view = req.view,
            scoped = scoped,
          )
      ) {
        is TargetResolution.Ok -> resolved.value
        is TargetResolution.Error ->
          return CheckOutcome.HttpError(
            400,
            ErrorEnvelope(error = resolved.code, message = resolved.message),
          )
      }

    if (state.currentFocus == null) {
      state.currentFocus =
        PreviewHttpServer.FocusTarget(
          direction = selection.targets.first().direction.name,
          screen = selection.targets.first().screenId,
          overlay = null,
          view = selection.views.first().name,
        )
    }

    val renderStartNs = System.nanoTime()
    val artifacts =
      try {
        awaitOrReplaced(
          renderQueue.submitBatchCheck(
            compileEntry,
            selection.planned,
            scoped,
            QueuePriority.AGENT,
          ),
        )
      } catch (e: ServerReplacedException) {
        return replacedCheckOutcome(scoped, sourcePath, e.replacedByVersion)
      } catch (e: SandboxWatchdogException) {
        handleWatchdogTimeout(
          Watchdog.ActiveJob(
            contentHash = snapshot.contentHash,
            scope = scopeKey,
            budgetMs = warmBudgetMs,
            startedAtMs = System.currentTimeMillis(),
            repoRoot = state.repoRoot.toString(),
          ),
        )
        return CheckOutcome.Reply(
          200,
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
          ),
        )
      }
    val totalRenderMs = (System.nanoTime() - renderStartNs) / 1_000_000L

    val allPass = artifacts.all { it.pass }
    val selectedImages =
      CheckImagePolicy.selectResponseImages(artifacts, scoped, compileEntry.surface, allPass)
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
    val listenersPresent = hasBoardListeners(state.repoId)
    val needBackgroundBoard =
      scoped && listenersPresent && state.lastBoardQueuedHash[fileKey] != snapshot.contentHash
    if (!scoped) {
      state.lastBoardQueuedHash[fileKey] = snapshot.contentHash
    }
    val boardPayload =
      buildBoardPayload(state.repoId, compileEntry, artifacts, emptyList(), overallPass = allPass)
    state.latestBoardPayload = boardPayload
    if (!needBackgroundBoard && listenersPresent) {
      broadcastSse(
        state.repoId,
        "board",
        PreviewJson.compact.encodeToString(BoardPayload.serializer(), boardPayload),
      )
    }

    val pendingSelection = selectionStore.consumePending(sourcePath)
    val response =
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

    return CheckOutcome.Reply(
      status = 200,
      response = response,
      afterSend = {
        scheduleLiveUpdate(
          entry = compileEntry,
          update =
            LivePreviewUpdate(
              direction = selection.targets.first().direction.name,
              screenId = selection.targets.first().screenId,
              overlayId = null,
            ),
        )
        if (needBackgroundBoard) {
          broadcastSse(
            state.repoId,
            "stale",
            PreviewJson.compact.encodeToString(
              StaleEventPayload.serializer(),
              StaleEventPayload(
                repoId = state.repoId,
                contentHash = snapshot.contentHash,
                sourcePath = fileKey,
                views = buildStaleViewRefs(compileEntry),
              ),
            ),
          )
          enqueueBoardRenders(state, compileEntry, overallPass = allPass)
        } else {
          maybeScheduleRecycle()
        }
      },
    )
  }

  private fun replacedCheckOutcome(
    scoped: Boolean,
    sourcePath: Path,
    replacedByVersion: String,
  ): CheckOutcome.Reply =
    CheckOutcome.Reply(
      409,
      CheckResponse(
        ok = false,
        valid = false,
        pass = false,
        scoped = scoped,
        sourcePath = sourcePath.toString(),
        error = "server_replaced",
        tell = "replaced by v$replacedByVersion, rerun",
      ),
    )

  fun renderReport(req: RenderRequest): CoordinatorOutcome<RenderResponse> {
    inFlightChecks.incrementAndGet()
    try {
      val sourcePath = Path.of(req.path).toAbsolutePath().normalize()
      val outDir = Path.of(req.outDir).toAbsolutePath().normalize()
      val explicitDesign = req.design?.takeIf { it.isNotBlank() }?.let(Path::of)
      val reqRepoRoot =
        req.repoRoot?.takeIf { it.isNotBlank() }?.let(Path::of)
          ?: ServerPaths.resolveRepoRoot(sourcePath)
      val state = getOrCreateRepo(reqRepoRoot)
      lastActiveRepoId = state.repoId

      if (!Files.isRegularFile(sourcePath)) {
        return CoordinatorOutcome.Failure(
          404,
          ErrorEnvelope(error = "no_such_file", message = "No such file: $sourcePath"),
        )
      }

      val result =
        try {
          ReportRenderer.render(
            sourcePath = sourcePath,
            outDir = outDir,
            explicitDesign = explicitDesign,
            requestedViews = req.views,
            requestedScreens = req.screens,
            compile = { snap ->
              awaitOrReplaced(renderQueue.submitCompile(state.repoRoot, snap, QueuePriority.AGENT))
            },
            renderBatch = { entry, targets ->
              awaitOrReplaced(
                renderQueue.submitBatchReport(
                  entry = entry,
                  targets = targets,
                  outDir = outDir,
                  priority = QueuePriority.AGENT,
                ),
              )
            },
          )
        } catch (e: ServerReplacedException) {
          return serverReplacedFailure(e)
        }

      return CoordinatorOutcome.Success(
        status = 200,
        payload =
          RenderResponse(
            ok = result.ok,
            valid = result.valid,
            pass = result.pass,
            exitCode = result.exitCode,
            reportPath = result.reportPath?.toString(),
            error = result.error,
            messages = result.messages,
          ),
        afterSend = { maybeScheduleRecycle() },
      )
    } finally {
      inFlightChecks.decrementAndGet()
    }
  }

  fun tree(req: TreeRequest): CoordinatorOutcome<TreeResponse> {
    val sourcePath = Path.of(req.path).toAbsolutePath().normalize()
    val reqRepoRoot =
      req.repoRoot?.takeIf { it.isNotBlank() }?.let(Path::of)
        ?: ServerPaths.resolveRepoRoot(sourcePath)
    val state = getOrCreateRepo(reqRepoRoot)
    if (!Files.isRegularFile(sourcePath)) {
      return CoordinatorOutcome.Failure(
        404,
        ErrorEnvelope(error = "no_such_file", message = "No such file: $sourcePath"),
      )
    }
    val snapshot = ContentHasher.snapshot(sourcePath)
    val compileEntry =
      try {
        awaitOrReplaced(renderQueue.submitCompile(state.repoRoot, snapshot, QueuePriority.AGENT))
      } catch (e: ServerReplacedException) {
        return serverReplacedFailure(e)
      }
    val meta =
      compileEntry.boardMeta
        ?: return CoordinatorOutcome.Failure(
          422,
          ErrorEnvelope(
            error = "compile_error",
            message = compileEntry.loaded.errors.joinToString("; ") { it.message },
          ),
        )
    val target =
      when (
        val resolved =
          TargetResolver.resolveSingleTarget(meta, req.direction, req.screen, req.overlay, req.view)
      ) {
        is TargetResolution.Ok -> resolved.value
        is TargetResolution.Error ->
          return CoordinatorOutcome.Failure(
            400,
            ErrorEnvelope(error = resolved.code, message = resolved.message),
          )
      }

    val artifact =
      try {
        awaitOrReplaced(
          renderQueue.submitSingleRender(
            entry = compileEntry,
            direction = target.direction.name,
            screenId = target.screenId,
            prefix = target.prefix,
            overlayId = target.overlayId,
            view = target.view,
            densityScale = CheckExecutor.CHECK_DENSITY_SCALE,
            baseCheck = null,
            priority = QueuePriority.AGENT,
            writeFiles = false,
            includeFrame = false,
          ),
        )
      } catch (e: ServerReplacedException) {
        return serverReplacedFailure(e)
      }
    val tree =
      artifact.tree
        ?: return CoordinatorOutcome.Failure(
          500,
          ErrorEnvelope(error = "render_failed", message = "Render produced no layout tree"),
        )
    val pruned = req.maxDepth?.let { pruneDepth(tree, it) } ?: tree
    return CoordinatorOutcome.Success(
      status = 200,
      payload =
        TreeResponse(
          ok = true,
          direction = target.direction.name,
          screen = target.screenId,
          overlay = target.overlayId,
          view = target.view.name,
          tree = pruned,
        ),
      afterSend = { maybeScheduleRecycle() },
    )
  }

  fun getSelection(requestedPath: String?): SelectionOutcome {
    val activeRepo = repos[lastActiveRepoId] ?: repos[defaultRepoId]
    val rawPath = requestedPath ?: activeRepo?.activeSourcePath?.toString()
    if (rawPath.isNullOrBlank()) {
      return SelectionOutcome.Missing(
        SelectionResponse(
          ok = false,
          error = "no_selection",
          ask = "Click the element you mean on the preview board, then tell me to continue.",
        ),
      )
    }
    val sel = selectionStore.current(Path.of(rawPath))
    if (sel == null) {
      return SelectionOutcome.Missing(
        SelectionResponse(
          ok = false,
          error = "no_selection",
          ask = "Click the element you mean on the preview board, then tell me to continue.",
        ),
      )
    }
    return SelectionOutcome.Found(SelectionResponse(ok = true, selection = sel))
  }

  fun recordBoardSelection(
    repoQuery: String?,
    req: BoardSelectionRequest,
  ): CoordinatorOutcome<SelectionResponse> {
    val requestedRepo =
      req.repo?.takeIf { it.isNotBlank() } ?: repoQuery?.takeIf { it.isNotBlank() }
    val state =
      if (requestedRepo != null) repos[requestedRepo]
      else (repos[lastActiveRepoId] ?: repos[defaultRepoId])
    val targetPath =
      req.path?.takeIf { it.isNotBlank() }?.let(Path::of)
        ?: state?.activeSourcePath
        ?: return CoordinatorOutcome.Failure(
          409,
          ErrorEnvelope(error = "no_active_file", message = "No active exploration loaded"),
        )
    val saved =
      selectionStore.record(
        targetPath,
        NodeSelection(
          file = req.file,
          line = req.line,
          callName = req.callName,
          callChain = req.callChain,
          direction = req.direction,
          screen = req.screen,
          overlay = req.overlay,
          view = req.view,
          bounds = req.bounds,
          contentHash = req.contentHash,
          selectedAt = System.currentTimeMillis(),
        ),
      )
    return CoordinatorOutcome.Success(200, SelectionResponse(ok = true, selection = saved))
  }

  fun getBoardPayload(requestedRepoId: String?): BoardPayload? {
    val repoId = requestedRepoId?.takeIf { it.isNotBlank() }
    val state =
      if (repoId != null) repos[repoId] else (repos[lastActiveRepoId] ?: repos[defaultRepoId])
    return state?.latestBoardPayload
  }

  fun refreshOnSseSubscribe(requestedRepoId: String?) {
    val repoId = requestedRepoId?.takeIf { it.isNotBlank() }
    val state =
      if (repoId != null) repos[repoId] else (repos[lastActiveRepoId] ?: repos[defaultRepoId])
    val activeSrc = state?.activeSourcePath
    if (state != null && activeSrc != null) {
      handleFileChanged(state, activeSrc, state.activeDesignPath)
    }
  }

  fun boardRender(
    repoQuery: String?,
    req: BoardRenderRequest,
  ): CoordinatorOutcome<BoardRenderResponse> {
    val requestedRepo =
      req.repo?.takeIf { it.isNotBlank() } ?: repoQuery?.takeIf { it.isNotBlank() }
    val state =
      if (requestedRepo != null) repos[requestedRepo]
      else (repos[lastActiveRepoId] ?: repos[defaultRepoId])
    val sourcePath = state?.activeSourcePath
    val entry =
      if (state != null && sourcePath != null)
        renderQueue.latestCompiled(state.repoRoot, sourcePath)
      else null
    val meta = entry?.boardMeta
    if (state == null || entry == null || meta == null) {
      return CoordinatorOutcome.Failure(
        409,
        ErrorEnvelope(
          error = "no_compiled_board",
          message = "Run android-design-preview check before rendering on the board",
        ),
      )
    }
    val target =
      when (
        val resolved =
          TargetResolver.resolveSingleTarget(meta, req.direction, req.screen, req.overlay, req.view)
      ) {
        is TargetResolution.Ok -> resolved.value
        is TargetResolution.Error ->
          return CoordinatorOutcome.Failure(
            400,
            ErrorEnvelope(error = resolved.code, message = resolved.message),
          )
      }

    val variantOverride =
      req.variant?.let { wire ->
        PaletteVariant.fromWire(wire)
          ?: return CoordinatorOutcome.Failure(
            400,
            ErrorEnvelope(error = "invalid_variant", message = "Unknown variant '$wire'"),
          )
      }
    val fontScale = (req.fontScale ?: 1f).coerceIn(0.5f, 2.5f)
    val hasThemeOverride =
      req.seed != null || req.variant != null || req.dark != null || fontScale != 1f
    val artifact =
      try {
        if (!hasThemeOverride) {
          awaitOrReplaced(
            renderQueue.submitSingleRender(
              entry = entry,
              direction = target.direction.name,
              screenId = target.screenId,
              prefix = target.prefix,
              overlayId = target.overlayId,
              view = target.view,
              densityScale = CheckExecutor.CHECK_DENSITY_SCALE,
              baseCheck = null,
              priority = QueuePriority.BOARD_FOCUS,
              writeFiles = false,
              includeFrame = true,
            ),
          )
        } else {
          val effectiveView =
            if (req.dark != null && entry.surface == BoardSurface.Phone) {
              target.view.copy(dark = req.dark)
            } else {
              target.view
            }
          awaitOrReplaced(
            renderQueue.renderOverride(
              entry = entry,
              direction = target.direction.name,
              screenId = target.screenId,
              prefix = target.prefix,
              overlayId = target.overlayId,
              view = effectiveView,
              densityScale = CheckExecutor.CHECK_DENSITY_SCALE,
              includeFrame = true,
              fontScale = fontScale,
              seedOverride = req.seed,
              variantOverride = variantOverride?.wire,
              darkOverride = if (entry.surface == BoardSurface.Phone) req.dark else null,
            ),
          )
        }
      } catch (e: ServerReplacedException) {
        return serverReplacedFailure(e)
      }

    scheduleLiveUpdate(
      entry = entry,
      update =
        LivePreviewUpdate(
          direction = target.direction.name,
          screenId = target.screenId,
          overlayId = target.overlayId,
          darkOverride = if (entry.surface == BoardSurface.Phone) req.dark else null,
          seedOverride = req.seed,
          variantOverride = variantOverride?.wire,
          fontScale = fontScale,
        ),
    )

    return CoordinatorOutcome.Success(200, toBoardRenderResponse(artifact, entry.contentHash))
  }

  fun boardFocus(repoQuery: String?, req: BoardFocusRequest) {
    val requestedRepo =
      req.repo?.takeIf { it.isNotBlank() } ?: repoQuery?.takeIf { it.isNotBlank() }
    val state =
      when {
        requestedRepo != null -> repos[requestedRepo]
        !req.path.isNullOrBlank() -> resolveRepoForSource(Path.of(req.path))
        else -> repos[lastActiveRepoId] ?: repos[defaultRepoId]
      } ?: getOrCreateRepo(defaultRepoRoot)

    state.currentFocus =
      PreviewHttpServer.FocusTarget(
        direction = req.direction,
        screen = req.screen?.takeIf { it.isNotBlank() },
        overlay = req.overlay?.takeIf { it.isNotBlank() },
        view = req.view,
      )
    state.activeSourcePath?.let { src ->
      renderQueue.latestCompiled(state.repoRoot, src)?.let { entry ->
        scheduleLiveUpdate(
          entry = entry,
          update =
            LivePreviewUpdate(
              direction = req.direction,
              screenId = req.screen?.takeIf { it.isNotBlank() },
              overlayId = req.overlay?.takeIf { it.isNotBlank() },
            ),
        )
      }
    }
  }

  fun liveUpdate(req: LiveFocusRequest): CoordinatorOutcome<LiveFocusResponse> {
    val targetPath =
      req.path?.takeIf { it.isNotBlank() }?.let { Path.of(it).toAbsolutePath().normalize() }
    val state =
      when {
        !req.repoRoot.isNullOrBlank() -> getOrCreateRepo(Path.of(req.repoRoot))
        targetPath != null -> resolveRepoForSource(targetPath)
        else -> repos[lastActiveRepoId] ?: getOrCreateRepo(defaultRepoRoot)
      }
    lastActiveRepoId = state.repoId

    val src = targetPath ?: state.activeSourcePath
    val entry = src?.let { renderQueue.latestCompiled(state.repoRoot, it) }
    val liveUpdate =
      if (entry != null && entry.boardMeta != null) {
        val dir =
          req.direction
            ?: state.currentFocus?.direction
            ?: entry.boardMeta.directions.firstOrNull()?.name
        val screen =
          req.screen
            ?: state.currentFocus?.screen
            ?: entry.boardMeta.directions.firstOrNull { it.name == dir }?.startScreen
        val ov = req.overlay ?: state.currentFocus?.overlay
        LivePreviewUpdate(direction = dir, screenId = screen, overlayId = ov)
      } else {
        null
      }

    if (req.startLive) {
      return when (
        val started =
          liveSessionHost.startSession(
            serial = req.serial,
            avd = req.avd,
            entry = entry,
            update = liveUpdate,
          )
      ) {
        is LiveSessionStartResult.Failed -> {
          val httpStatus =
            when (started.exitCode) {
              ClientExitCode.USAGE -> 400
              ClientExitCode.NOT_READY -> 503
              else -> 500
            }
          CoordinatorOutcome.Failure(
            httpStatus,
            ErrorEnvelope(error = started.error, message = started.message),
          )
        }
        is LiveSessionStartResult.Ready ->
          CoordinatorOutcome.Success(
            200,
            LiveFocusResponse(
              ok = true,
              repoId = state.repoId,
              boardUrl = boardUrl(state.repoRoot),
              deviceSerial = started.serial,
              adbForwardPort = started.forwardPort,
            ),
          )
      }
    }

    if (liveSessionHost.isActive && entry != null && liveUpdate != null) {
      runLiveUpdateSync(entry = entry, update = liveUpdate)
    }
    return CoordinatorOutcome.Success(
      200,
      LiveFocusResponse(
        ok = true,
        repoId = state.repoId,
        boardUrl = boardUrl(state.repoRoot),
        deviceSerial = liveSessionHost.deviceSerial,
        adbForwardPort = liveSessionHost.adbForwardPort,
      ),
    )
  }

  /** Trigger a file-change cycle directly (used by [FileWatcher] and tests). */
  fun handleFileChanged(sourcePath: Path, designPath: Path?) {
    handleFileChanged(resolveRepoForSource(sourcePath), sourcePath, designPath)
  }

  private fun handleSpeculativePrecompile(
    state: RepoState,
    sourcePath: Path,
    designPath: Path?,
  ) {
    if (closed.get() || replacementSignal.isDone) return
    val normSource = sourcePath.toAbsolutePath().normalize()
    val snapshot = ContentHasher.snapshot(normSource, designPath)
    if (
      renderQueue.latestCompiled(state.repoRoot, normSource)?.contentHash == snapshot.contentHash
    ) {
      return
    }
    renderQueue.cancelStaleJobs(state.repoRoot, normSource, snapshot.contentHash)
    renderQueue.submitCompile(state.repoRoot, snapshot, QueuePriority.SPECULATIVE)
  }

  private fun handleFileChanged(
    state: RepoState,
    sourcePath: Path,
    designPath: Path?,
  ) {
    if (closed.get() || replacementSignal.isDone) return
    val normSource = sourcePath.toAbsolutePath().normalize()
    val snapshot = ContentHasher.snapshot(normSource, designPath)
    val fileKey = normSource.toString()
    if (state.lastBoardQueuedHash[fileKey] == snapshot.contentHash) return

    val latest = renderQueue.latestCompiled(state.repoRoot, normSource)
    clearTimeoutsIfHashChanged(state.repoRoot, snapshot.contentHash)
    renderQueue.cancelStaleJobs(state.repoRoot, normSource, snapshot.contentHash)
    broadcastSse(
      state.repoId,
      "stale",
      PreviewJson.compact.encodeToString(
        StaleEventPayload.serializer(),
        StaleEventPayload(
          repoId = state.repoId,
          contentHash = snapshot.contentHash,
          sourcePath = fileKey,
          views = buildStaleViewRefs(latest),
        ),
      ),
    )

    renderQueue.submitCompile(state.repoRoot, snapshot, QueuePriority.BOARD_FOCUS).whenComplete {
      compiledEntry,
      err ->
      if (err != null || compiledEntry == null) {
        if (!renderQueue.isCurrentHash(state.repoRoot, normSource, snapshot.contentHash)) {
          return@whenComplete
        }
        broadcastSse(
          state.repoId,
          "error_event",
          PreviewJson.compact.encodeToString(
            JsonObject.serializer(),
            JsonObject(
              mapOf("message" to JsonPrimitive(err?.message ?: "Recompile failed")),
            ),
          ),
        )
        return@whenComplete
      }
      if (!renderQueue.isCurrentHash(state.repoRoot, normSource, compiledEntry.contentHash)) {
        return@whenComplete
      }
      enqueueBoardRenders(state, compiledEntry, overallPass = null)
    }
  }

  private fun enqueueBoardRenders(
    state: RepoState,
    compiledEntry: CompileEntry,
    overallPass: Boolean?,
  ) {
    val normSource = compiledEntry.sourcePath.toAbsolutePath().normalize()
    val fileKey = normSource.toString()
    if (!renderQueue.isCurrentHash(state.repoRoot, normSource, compiledEntry.contentHash)) return
    state.lastBoardQueuedHash[fileKey] = compiledEntry.contentHash

    val meta = compiledEntry.boardMeta
    if (meta == null || compiledEntry.loaded.errors.isNotEmpty()) {
      val payload =
        buildBoardPayload(
          state.repoId,
          compiledEntry,
          emptyList(),
          emptyList(),
          overallPass = false,
        )
      state.latestBoardPayload = payload
      broadcastSse(
        state.repoId,
        "board",
        PreviewJson.compact.encodeToString(BoardPayload.serializer(), payload),
      )
      maybeScheduleRecycle()
      return
    }
    val allTargets = TargetResolver.enumerateBoardTargets(meta)
    val focus = state.currentFocus
    val ordered =
      if (focus != null) {
        val (focused, rest) =
          allTargets.partition { t ->
            t.direction == focus.direction &&
              (focus.screen == null || t.screenId == focus.screen) &&
              t.overlayId == focus.overlay &&
              t.view.name == focus.view
          }
        focused.map { it to QueuePriority.BOARD_FOCUS } +
          rest.map { it to QueuePriority.BOARD_REMAINDER }
      } else {
        val firstTarget = allTargets.firstOrNull()
        allTargets.map { target ->
          val prio =
            if (target == firstTarget) QueuePriority.BOARD_FOCUS else QueuePriority.BOARD_REMAINDER
          target to prio
        }
      }
    val staleRefs = ordered.map { (t, _) ->
      BoardViewRef(t.direction, t.screenId, t.overlayId, t.view.name)
    }
    val existing2x = renderQueue.completedBoardArtifacts(state.repoRoot, normSource).values.toList()
    val metaPayload =
      buildBoardPayload(state.repoId, compiledEntry, existing2x, staleRefs, overallPass)
    state.latestBoardPayload = metaPayload
    broadcastSse(
      state.repoId,
      "board",
      PreviewJson.compact.encodeToString(BoardPayload.serializer(), metaPayload),
    )

    ordered.forEachIndexed { idx, (target, prio) ->
      val fut =
        renderQueue.submitSingleRender(
          entry = compiledEntry,
          direction = target.direction,
          screenId = target.screenId,
          prefix = target.prefix,
          overlayId = target.overlayId,
          view = target.view,
          densityScale = CheckExecutor.CHECK_DENSITY_SCALE,
          baseCheck = null,
          priority = prio,
          writeFiles = false,
          includeFrame = true,
        )
      if (idx == 0) {
        fut.whenComplete { _, _ -> maybeScheduleRecycle() }
      }
    }
    if (ordered.isEmpty()) maybeScheduleRecycle()
  }

  private fun handleViewRendered(
    repoRoot: Path,
    sourcePath: Path,
    contentHash: String,
    artifact: RenderArtifact,
  ) {
    val repoId = ServerPaths.repoHash(repoRoot.toAbsolutePath().normalize())
    val tree = artifact.tree
    if (tree != null && Files.isRegularFile(sourcePath)) {
      val lines =
        try {
          Files.readAllLines(sourcePath)
        } catch (_: Exception) {
          emptyList()
        }
      selectionStore.reResolve(
        sourcePath = sourcePath,
        newContentHash = contentHash,
        direction = artifact.direction,
        screen = artifact.screen,
        overlay = artifact.overlay,
        view = artifact.view.name,
        tree = tree,
        sourceLines = lines,
      )
    }
    if (
      artifact.densityScale == CheckExecutor.CHECK_DENSITY_SCALE &&
        hasBoardListeners(repoId) &&
        artifact.pngBytes != null &&
        artifact.pngBytes.isNotEmpty()
    ) {
      val resp = toBoardRenderResponse(artifact, contentHash)
      broadcastSse(
        repoId,
        "view_updated",
        PreviewJson.compact.encodeToString(BoardRenderResponse.serializer(), resp),
      )
    }
  }

  private fun handleWatchdogTimeout(job: Watchdog.ActiveJob) {
    val now = System.currentTimeMillis()
    val jobRepoRoot = job.repoRoot ?: defaultRepoRoot.toString()
    val entry =
      TimeoutEntry(
        contentHash = job.contentHash,
        scope = job.scope,
        recordedAt = now,
        reason = "Exceeded ${job.budgetMs} ms budget",
        repoRoot = jobRepoRoot,
      )
    ServerPaths.updateRecord(recordPath) { rec ->
      val updatedTimeouts =
        if (rec.timeouts.any { it.contentHash == entry.contentHash && it.scope == entry.scope }) {
          rec.timeouts
        } else {
          rec.timeouts + entry
        }
      rec.copy(timeouts = updatedTimeouts, haltedAt = now)
    }
    val state = repos[ServerPaths.repoHash(Path.of(jobRepoRoot))] ?: repos[defaultRepoId]
    val restartCmd =
      state?.activeSourcePath?.let { "android-design-preview open $it" }
        ?: "android-design-preview open <file>"
    broadcastSse(
      null,
      "halted",
      PreviewJson.compact.encodeToString(
        HaltedEventPayload.serializer(),
        HaltedEventPayload(job.contentHash, job.scope, restartCmd),
      ),
    )
  }

  private fun clearTimeoutsIfHashChanged(repoRoot: Path, newHash: String) {
    val repoStr = repoRoot.toAbsolutePath().normalize().toString()
    ServerPaths.updateRecord(recordPath) { rec ->
      if (rec.timeouts.isEmpty()) rec
      else {
        val kept =
          rec.timeouts.filter {
            (it.repoRoot != null && it.repoRoot != repoStr) || it.contentHash == newHash
          }
        if (kept.size == rec.timeouts.size) rec else rec.copy(timeouts = kept, haltedAt = null)
      }
    }
  }

  private fun runLiveUpdateSync(entry: CompileEntry, update: LivePreviewUpdate) {
    if (closed.get() || replacementSignal.isDone || !liveSessionHost.isActive) return
    pendingLiveUpdate.set(null)
    val future =
      try {
        liveUpdateExecutor.submit {
          if (closed.get() || replacementSignal.isDone || !liveSessionHost.isActive) return@submit
          runCatching { liveSessionHost.updateIfActive(entry, update) }
        }
      } catch (_: RejectedExecutionException) {
        return
      }
    try {
      future.get()
    } catch (e: ExecutionException) {
      throw e.cause ?: e
    } catch (e: InterruptedException) {
      Thread.currentThread().interrupt()
      throw e
    }
  }

  private fun scheduleLiveUpdate(entry: CompileEntry, update: LivePreviewUpdate) {
    if (closed.get() || replacementSignal.isDone || !liveSessionHost.isActive) return
    pendingLiveUpdate.set(entry to update)
    runCatching {
      liveUpdateExecutor.execute {
        if (closed.get() || replacementSignal.isDone || !liveSessionHost.isActive) return@execute
        val next = pendingLiveUpdate.getAndSet(null) ?: return@execute
        runCatching { liveSessionHost.updateIfActive(next.first, next.second) }
      }
    }
  }

  private fun maybeScheduleRecycle() {
    if (
      renderQueue.compileCount >= maxCompilesBeforeRecycle &&
        recycleScheduled.compareAndSet(false, true)
    ) {
      broadcastSse(null, "recycling", """{"reason":"compile_limit"}""")
      onRecycleRequested()
    }
  }

  private fun <T> awaitOrReplaced(future: CompletableFuture<T>): T {
    CompletableFuture.anyOf(future, replacementSignal).get()
    if (replacementSignal.isDone) {
      throw ServerReplacedException(replacementSignal.getNow("new"))
    }
    return try {
      future.get()
    } catch (e: ExecutionException) {
      when (val cause = e.cause) {
        is ServerReplacedException -> throw cause
        is RuntimeException -> throw cause
        is Error -> throw cause
        else -> throw e
      }
    }
  }

  private fun getOrCreateRepo(repoRoot: Path): RepoState {
    val norm = repoRoot.toAbsolutePath().normalize()
    val rid = ServerPaths.repoHash(norm)
    return repos.computeIfAbsent(rid) { RepoState(rid, norm) }
  }

  private fun resolveRepoForSource(sourcePath: Path): RepoState {
    val norm = sourcePath.toAbsolutePath().normalize()
    val matched = repos.values.firstOrNull { it.activeSourcePath == norm }
    if (matched != null) return matched
    return getOrCreateRepo(ServerPaths.resolveRepoRoot(norm))
  }

  private fun buildStaleViewRefs(entry: CompileEntry?): List<BoardViewRef> {
    val meta = entry?.boardMeta ?: return emptyList()
    return TargetResolver.enumerateBoardTargets(meta).map {
      BoardViewRef(it.direction, it.screenId, it.overlayId, it.view.name)
    }
  }

  private fun buildBoardPayload(
    repoId: String,
    entry: CompileEntry,
    artifacts: List<RenderArtifact>,
    staleViews: List<BoardViewRef>,
    overallPass: Boolean? = null,
  ): BoardPayload {
    val loaded = entry.loaded
    val system = loaded.designSystem
    val valid = entry.boardMeta != null && loaded.errors.isEmpty()
    val pass = valid && (overallPass ?: (artifacts.isNotEmpty() && artifacts.all { it.pass }))
    return BoardPayload(
      repoId = repoId,
      valid = valid,
      pass = pass,
      contentHash = entry.contentHash,
      compileMs = loaded.compileMs,
      renderMs = artifacts.sumOf { it.renderMs },
      compileCount = renderQueue.compileCount,
      surface = if (entry.surface == BoardSurface.Wear) "wear" else "phone",
      boardName = entry.boardName,
      sourcePath = entry.sourcePath.toString(),
      sourceFile = entry.sourcePath.fileName.toString(),
      designPath = loaded.designPath?.toString(),
      theme = ReportTheme(system.seed, system.variant.wire, system.contrast),
      errors = loaded.errors,
      boardMeta = entry.boardMeta,
      renders = artifacts.map { toBoardRenderResponse(it, entry.contentHash) },
      staleViews = staleViews,
      updatedAt = System.currentTimeMillis(),
    )
  }

  private fun toBoardRenderResponse(
    artifact: RenderArtifact,
    contentHash: String,
  ): BoardRenderResponse {
    val b64 = Base64.getEncoder()
    return BoardRenderResponse(
      direction = artifact.direction,
      screen = artifact.screen,
      overlay = artifact.overlay,
      view = artifact.view.name,
      contentHash = contentHash,
      pass = artifact.pass,
      renderMs = artifact.renderMs,
      compileCount = renderQueue.compileCount,
      check = artifact.check,
      layout = artifact.layout,
      tree = artifact.tree,
      pngBase64 = artifact.pngBytes?.takeIf { it.isNotEmpty() }?.let(b64::encodeToString),
      endPngBase64 = artifact.endPngBytes?.takeIf { it.isNotEmpty() }?.let(b64::encodeToString),
      imageMime = artifact.imageMime,
      failures = artifact.entry.failures,
    )
  }

  private fun pruneDepth(tree: LayoutTree, maxDepth: Int): LayoutTree {
    if (maxDepth < 0) return tree.copy(nodes = emptyList())
    val depthById = HashMap<String, Int>(tree.nodes.size)
    val childrenByParent = tree.nodes.groupBy { it.parent }
    val q = ArrayDeque<Pair<LayoutNode, Int>>()
    childrenByParent[null].orEmpty().forEach { q += it to 0 }
    while (q.isNotEmpty()) {
      val (node, depth) = q.removeFirst()
      depthById[node.id] = depth
      childrenByParent[node.id].orEmpty().forEach { child -> q += child to (depth + 1) }
    }
    return tree.copy(nodes = tree.nodes.filter { (depthById[it.id] ?: 0) <= maxDepth })
  }

  private fun serverReplacedFailure(e: ServerReplacedException): CoordinatorOutcome.Failure =
    CoordinatorOutcome.Failure(
      409,
      ErrorEnvelope(
        error = "server_replaced",
        message = "replaced by v${e.replacedByVersion}, rerun",
      ),
    )
}
