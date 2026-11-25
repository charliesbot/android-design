package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.app.cli.ReportTheme
import bot.charlies.androiddesign.preview.model.BoardSurface
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.PreviewJson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.io.OutputStream
import java.net.BindException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Long-running shared HTTP + SSE server owning compilation, queued rendering across repositories,
 * content-hash result caching, speculative file watching, click selection, and the browser board.
 */
class PreviewHttpServer(
  val repoRoot: Path,
  private val cacheRoot: Path = ServerPaths.cacheRoot(),
  private val preferredPort: Int = ServerPaths.SHARED_PREFERRED_PORT,
  private val existingViewToken: String? = null,
  private val existingClientSecret: String? = null,
  private val maxCompilesBeforeRecycle: Int = MAX_COMPILES_BEFORE_RECYCLE,
  private val idleTimeoutMs: Long = DEFAULT_IDLE_TIMEOUT_MS,
  private val coldBudgetMs: Long = Watchdog.COLD_BUDGET_MS,
  private val warmBudgetMs: Long = Watchdog.WARM_BUDGET_MS,
  private val worker: RenderWorker,
  private val runtimeIdentity: RuntimeIdentity? = null,
  private val deviceSerial: String? = null,
  private val adbForwardPort: Int? = null,
  private val apkSha256: String? = null,
  private val onWatchdogHalt: () -> Unit = { Runtime.getRuntime().halt(1) },
  private val onProcessExit: (() -> Unit)? = null,
  private val enableFileWatcher: Boolean = true,
  private val onLaunchLiveActivity: () -> Unit = {},
) {
  data class FocusTarget(
    val direction: String,
    val screen: String?,
    val overlay: String?,
    val view: String,
  )

  private class SseClient(
    val repoId: String?,
    val exchange: HttpExchange,
    val out: OutputStream,
  )

  private val defaultRepoRoot: Path = repoRoot.toAbsolutePath().normalize()
  private val defaultRepoId: String = ServerPaths.repoHash(defaultRepoRoot)

  val recordPath: Path = ServerPaths.sharedRecordPath(cacheRoot)
  val rendersRoot: Path = ServerPaths.rendersDir(defaultRepoRoot, cacheRoot)
  val selectionStore = SelectionStore()

  private val clientSecret: String = existingClientSecret ?: ServerPaths.randomToken()
  private val viewToken: String = existingViewToken ?: ServerPaths.randomToken()
  private val startedAt: Long = System.currentTimeMillis()
  private val lastActivityMs = AtomicLong(startedAt)
  private val closed = AtomicBoolean(false)
  private val recycleScheduled = AtomicBoolean(false)
  private val replacementSignal = CompletableFuture<String>()

  /** True once a newer version asked this server to stop; its successor reuses the device. */
  val wasReplaced: Boolean
    get() = replacementSignal.isDone

  private val inFlightChecks = AtomicInteger(0)
  private val sseClients = CopyOnWriteArrayList<SseClient>()
  private val repos = ConcurrentHashMap<String, RepoState>()
  private val nonceRng = SecureRandom()

  @Volatile private var httpServer: HttpServer? = null
  @Volatile private var boundPort: Int = 0
  @Volatile private var lastActiveRepoId: String = defaultRepoId
  @Volatile private var liveOwnerRepoId: String = defaultRepoId

  init {
    repos[defaultRepoId] = RepoState(defaultRepoId, defaultRepoRoot)
  }

  private val executor = CheckExecutor(worker)

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
    val hasBoardListeners = sseClients.any { it.repoId == null || it.repoId == repoState.repoId }
    if (hasBoardListeners) {
      handleFileChanged(repoState, src, design)
    } else if (executor.supportsHeadlessSpeculativePrecompile) {
      handleSpeculativePrecompile(repoState, src, design)
    }
  }

  private val httpExecutor = Executors.newCachedThreadPool { r ->
    Thread(r, "android-design-http").apply { isDaemon = true }
  }
  private val housekeepExecutor: ScheduledExecutorService =
    Executors.newSingleThreadScheduledExecutor { r ->
      Thread(r, "android-design-housekeep").apply { isDaemon = true }
    }

  private val checkService =
    CheckService(
      host = PreviewCheckHost(),
      renderQueue = renderQueue,
      executor = executor,
      selectionStore = selectionStore,
      warmBudgetMs = warmBudgetMs,
    )

  /** Exposes the server state [CheckService] orchestrates against. */
  private inner class PreviewCheckHost : CheckHost {
    private val server = this@PreviewHttpServer

    override val recordPath: Path
      get() = server.recordPath

    override fun repoState(repoRoot: Path): RepoState = server.getOrCreateRepo(repoRoot)

    override fun boardUrl(repoRoot: Path): String = server.boardUrl(repoRoot)

    override fun markActiveRepo(repoId: String) {
      server.lastActiveRepoId = repoId
    }

    override fun markLiveOwner(repoId: String) {
      server.liveOwnerRepoId = repoId
    }

    override fun clearTimeoutsIfHashChanged(repoRoot: Path, newHash: String) =
      server.clearTimeoutsIfHashChanged(repoRoot, newHash)

    override fun watchFile(repoId: String, sourcePath: Path, designPath: Path?) {
      if (server.enableFileWatcher) server.fileWatcher.watchForRepo(repoId, sourcePath, designPath)
    }

    override fun hasBoardListeners(repoId: String): Boolean =
      server.sseClients.any { it.repoId == null || it.repoId == repoId }

    override fun <T> awaitOrReplaced(future: CompletableFuture<T>): T =
      server.awaitOrReplaced(future)

    override fun broadcastSse(targetRepoId: String?, event: String, dataJson: String) =
      server.broadcastSse(targetRepoId, event, dataJson)

    override fun buildBoardPayload(
      repoId: String,
      entry: CompileEntry,
      artifacts: List<RenderArtifact>,
      staleViews: List<BoardViewRef>,
      overallPass: Boolean?,
    ): BoardPayload = server.buildBoardPayload(repoId, entry, artifacts, staleViews, overallPass)

    override fun buildStaleViewRefs(entry: CompileEntry?): List<BoardViewRef> =
      server.buildStaleViewRefs(entry)

    override fun enqueueBoardRenders(
      state: RepoState,
      compiledEntry: CompileEntry,
      overallPass: Boolean?,
    ) = server.enqueueBoardRenders(state, compiledEntry, overallPass)

    override fun handleWatchdogTimeout(job: Watchdog.ActiveJob) = server.handleWatchdogTimeout(job)

    override fun maybeScheduleRecycle() = server.maybeScheduleRecycle()
  }

  val port: Int
    get() = boundPort

  fun record(): ServerRecord = ServerPaths.readRecord(recordPath) ?: buildRecord(boundPort)

  fun boardUrlForRepoId(repoId: String?): String =
    if (!repoId.isNullOrBlank()) {
      "http://127.0.0.1:$boundPort/?repo=$repoId&view=$viewToken"
    } else {
      "http://127.0.0.1:$boundPort/?view=$viewToken"
    }

  fun boardUrl(repoRootForUrl: Path? = null): String {
    val rid = repoRootForUrl?.let { ServerPaths.repoHash(it.toAbsolutePath().normalize()) }
    return boardUrlForRepoId(rid)
  }

  fun origin(): String = "http://127.0.0.1:$boundPort"

  fun start(): ServerRecord {
    val loopback = InetAddress.getByName("127.0.0.1")
    val server =
      try {
        HttpServer.create(InetSocketAddress(loopback, preferredPort), 0)
      } catch (_: BindException) {
        HttpServer.create(InetSocketAddress(loopback, 0), 0)
      }
    boundPort = server.address.port
    server.executor = httpExecutor
    server.createContext("/") { exchange ->
      try {
        handleExchange(exchange)
      } catch (e: Exception) {
        if (exchange.responseCode == -1) {
          sendJson(
            exchange,
            500,
            ErrorEnvelope(
              error = "internal_error",
              message = e.message ?: e::class.simpleName.orEmpty(),
            ),
          )
        }
      }
    }
    httpServer = server
    watchdog.start()
    renderQueue.start()
    server.start()

    val priorTimeouts = ServerPaths.readRecord(recordPath)?.timeouts.orEmpty()
    val rec = buildRecord(boundPort, timeouts = priorTimeouts)
    ServerPaths.writeRecord(recordPath, rec)

    housekeepExecutor.execute {
      if (!closed.get()) {
        try {
          CacheCleaner.clean(cacheRoot = cacheRoot, currentRecordPath = recordPath)
        } catch (_: Exception) {
          // Best-effort startup cleanup must never crash the server.
        }
      }
    }

    if (idleTimeoutMs > 0L) {
      val checkEvery = (idleTimeoutMs / 4L).coerceIn(100L, 15_000L)
      housekeepExecutor.scheduleAtFixedRate(
        {
          if (
            !closed.get() &&
              sseClients.isEmpty() &&
              System.currentTimeMillis() - lastActivityMs.get() >= idleTimeoutMs
          ) {
            stop(triggerExit = true)
          }
        },
        checkEvery,
        checkEvery,
        TimeUnit.MILLISECONDS,
      )
    }
    return rec
  }

  private fun buildRecord(
    portValue: Int,
    timeouts: List<TimeoutEntry> = emptyList(),
  ): ServerRecord =
    ServerRecord(
      pid = ProcessHandle.current().pid(),
      port = portValue,
      jarVersion = AppInfo.VERSION,
      startedAt = startedAt,
      repoRoot = defaultRepoRoot.toString(),
      clientSecret = clientSecret,
      viewToken = viewToken,
      timeouts = timeouts,
      haltedAt = null,
      runtimeHash = runtimeIdentity?.runtimeHash,
      sourceDir = runtimeIdentity?.sourceDir,
      backend = executor.name,
      deviceSerial = deviceSerial,
      adbForwardPort = adbForwardPort,
      apkSha256 = apkSha256 ?: runtimeIdentity?.apkSha256,
    )

  fun stop(triggerExit: Boolean = false) {
    if (!closed.compareAndSet(false, true)) return
    fileWatcher.stop()
    watchdog.stop()
    renderQueue.stop()
    for (client in sseClients) {
      try {
        client.exchange.close()
      } catch (_: Exception) {
        // Ignore close errors.
      }
    }
    sseClients.clear()
    try {
      httpServer?.stop(0)
    } catch (_: Exception) {
      // Ignore stop errors.
    }
    httpServer = null
    runCatching { executor.close() }
    httpExecutor.shutdownNow()
    housekeepExecutor.shutdownNow()
    if (triggerExit) {
      onProcessExit?.invoke()
    }
  }

  /** Trigger a file-change cycle directly (used by [FileWatcher] and tests). */
  fun handleFileChanged(sourcePath: Path, designPath: Path?) {
    val state = resolveRepoForSource(sourcePath)
    handleFileChanged(state, sourcePath, designPath)
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
    val allTargets = enumerateBoardTargets(meta)
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
          densityScale = 2,
          baseCheck = null,
          priority = prio,
          writeFiles = false,
          frameFormat = FrameFormat.WEBP_LOSSLESS,
        )
      if (idx == 0) {
        fut.whenComplete { _, _ -> maybeScheduleRecycle() }
      }
    }
    if (ordered.isEmpty()) maybeScheduleRecycle()
  }

  private fun handleExchange(exchange: HttpExchange) {
    lastActivityMs.set(System.currentTimeMillis())
    val host = exchange.requestHeaders.getFirst("Host")
    if (host != "127.0.0.1:$boundPort") {
      sendJson(
        exchange,
        403,
        ErrorEnvelope(error = "forbidden_host", message = "Invalid Host header"),
      )
      return
    }

    val method = exchange.requestMethod.uppercase()
    val path = exchange.requestURI.path ?: "/"
    val query = parseQuery(exchange.requestURI.rawQuery)

    when {
      path == "/api/status" && method == "GET" -> {
        if (!requireClientSecret(exchange)) return
        handleStatus(exchange, query)
      }
      path == "/api/check" && method == "POST" -> {
        if (!requireClientSecret(exchange)) return
        handleCheck(exchange)
      }
      path == "/api/render" && method == "POST" -> {
        if (!requireClientSecret(exchange)) return
        handleRender(exchange)
      }
      path == "/api/tree" && method == "POST" -> {
        if (!requireClientSecret(exchange)) return
        handleTree(exchange)
      }
      path == "/api/selection" && method == "GET" -> {
        if (!requireClientSecret(exchange)) return
        handleGetSelection(exchange, query)
      }
      path == "/api/live" && method == "POST" -> {
        if (!requireClientSecret(exchange)) return
        handleLiveUpdate(exchange)
      }
      path == "/api/stop" && method == "POST" -> {
        if (!requireClientSecret(exchange)) return
        handleStop(exchange)
      }
      (path == "/" || path == "/index.html") && method == "GET" -> {
        if (!requireViewToken(exchange, query)) return
        handleBoardHtml(exchange)
      }
      path == "/api/board" && method == "GET" -> {
        if (!requireViewToken(exchange, query)) return
        handleGetBoard(exchange, query)
      }
      path == "/api/events" && method == "GET" -> {
        if (!requireViewToken(exchange, query)) return
        handleSse(exchange, query)
      }
      path == "/api/board/render" && method == "POST" -> {
        if (!requireViewToken(exchange, query)) return
        if (!requireBoardOrigin(exchange)) return
        handleBoardRender(exchange, query)
      }
      path == "/api/board/focus" && method == "POST" -> {
        if (!requireViewToken(exchange, query)) return
        if (!requireBoardOrigin(exchange)) return
        handleBoardFocus(exchange, query)
      }
      path == "/api/board/selection" && method == "POST" -> {
        if (!requireViewToken(exchange, query)) return
        if (!requireBoardOrigin(exchange)) return
        handleBoardSelection(exchange, query)
      }
      else -> {
        sendJson(
          exchange,
          404,
          ErrorEnvelope(error = "not_found", message = "Unknown route $method $path"),
        )
      }
    }
  }

  private fun handleStatus(exchange: HttpExchange, query: Map<String, String>) {
    val requestedRepoId = query["repo"]?.takeIf { it.isNotBlank() }
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
    val resp =
      StatusResponse(
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
        deviceSerial = deviceSerial,
        adbForwardPort = adbForwardPort,
        activeRepos = repos.values.map { it.repoRoot.toString() }.sorted(),
      )
    sendRawJson(
      exchange,
      200,
      PreviewJson.compact.encodeToString(StatusResponse.serializer(), resp),
    )
  }

  private fun handleStop(exchange: HttpExchange) {
    val raw =
      try {
        exchange.requestBody.use { it.readNBytes(MAX_BODY_BYTES) }.toString(Charsets.UTF_8).trim()
      } catch (_: Exception) {
        ""
      }
    val req =
      if (raw.isNotEmpty()) {
        runCatching { PreviewJson.compact.decodeFromString(StopRequest.serializer(), raw) }
          .getOrNull()
      } else {
        null
      }
    val repVersion = req?.replacedByVersion?.takeIf { it.isNotBlank() }
    if (repVersion != null) {
      ServerPaths.updateRecord(recordPath) { it.copy(replacedByVersion = repVersion) }
      replacementSignal.complete(repVersion)
      renderQueue.cancelForReplacement(repVersion)
      val waitDeadline = System.currentTimeMillis() + REPLACEMENT_DRAIN_WAIT_MS
      while (inFlightChecks.get() > 0 && System.currentTimeMillis() < waitDeadline) {
        try {
          Thread.sleep(5)
        } catch (_: InterruptedException) {
          break
        }
      }
      sendRawJson(
        exchange,
        200,
        """{"ok":true,"status":"stopping","replacedByVersion":"$repVersion"}""",
      )
      housekeepExecutor.schedule({ stop(triggerExit = true) }, 20, TimeUnit.MILLISECONDS)
    } else {
      sendRawJson(exchange, 200, """{"ok":true,"status":"stopping"}""")
      housekeepExecutor.schedule({ stop(triggerExit = true) }, 30, TimeUnit.MILLISECONDS)
    }
  }

  private fun handleLiveUpdate(exchange: HttpExchange) {
    val body = readBodyString(exchange, MAX_BODY_BYTES) ?: return
    val req =
      try {
        PreviewJson.compact.decodeFromString(LiveFocusRequest.serializer(), body)
      } catch (e: Exception) {
        sendJson(
          exchange,
          400,
          ErrorEnvelope(error = "bad_request", message = e.message ?: "Invalid JSON body"),
        )
        return
      }
    val targetPath =
      req.path?.takeIf { it.isNotBlank() }?.let { Path.of(it).toAbsolutePath().normalize() }
    val state =
      when {
        !req.repoRoot.isNullOrBlank() -> getOrCreateRepo(Path.of(req.repoRoot))
        targetPath != null -> resolveRepoForSource(targetPath)
        else -> repos[lastActiveRepoId] ?: getOrCreateRepo(defaultRepoRoot)
      }
    lastActiveRepoId = state.repoId
    liveOwnerRepoId = state.repoId

    val src = targetPath ?: state.activeSourcePath
    val entry = src?.let { renderQueue.latestCompiled(state.repoRoot, it) }
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
      executor.updateLive(
        entry = entry,
        LivePreviewUpdate(
          direction = dir,
          screenId = screen,
          overlayId = ov,
        ),
      )
    }
    if (req.launchActivity) {
      runCatching { onLaunchLiveActivity() }
    }
    sendRawJson(
      exchange,
      200,
      """{"ok":true,"repoId":"${state.repoId}","boardUrl":"${boardUrl(state.repoRoot)}"}""",
    )
  }

  private fun handleCheck(exchange: HttpExchange) {
    val body = readBodyString(exchange, MAX_BODY_BYTES) ?: return
    val req =
      try {
        PreviewJson.compact.decodeFromString(CheckRequest.serializer(), body)
      } catch (e: Exception) {
        sendJson(
          exchange,
          400,
          ErrorEnvelope(error = "bad_request", message = e.message ?: "Invalid JSON body"),
        )
        return
      }

    inFlightChecks.incrementAndGet()
    try {
      handleCheckBody(exchange, req)
    } finally {
      inFlightChecks.decrementAndGet()
    }
  }

  private fun handleCheckBody(exchange: HttpExchange, req: CheckRequest) {
    checkService.run(req) { status, json -> sendRawJson(exchange, status, json) }
  }

  private fun handleRender(exchange: HttpExchange) {
    val body = readBodyString(exchange, MAX_BODY_BYTES) ?: return
    val req =
      try {
        PreviewJson.compact.decodeFromString(RenderRequest.serializer(), body)
      } catch (e: Exception) {
        sendJson(
          exchange,
          400,
          ErrorEnvelope(error = "bad_request", message = e.message ?: "Invalid JSON body"),
        )
        return
      }
    inFlightChecks.incrementAndGet()
    try {
      handleRenderBody(exchange, req)
    } finally {
      inFlightChecks.decrementAndGet()
    }
  }

  private fun handleRenderBody(exchange: HttpExchange, req: RenderRequest) {
    val sourcePath = Path.of(req.path).toAbsolutePath().normalize()
    val outDir = Path.of(req.outDir).toAbsolutePath().normalize()
    val explicitDesign = req.design?.takeIf { it.isNotBlank() }?.let(Path::of)
    val reqRepoRoot =
      req.repoRoot?.takeIf { it.isNotBlank() }?.let(Path::of)
        ?: ServerPaths.resolveRepoRoot(sourcePath)
    val state = getOrCreateRepo(reqRepoRoot)
    lastActiveRepoId = state.repoId

    if (!Files.isRegularFile(sourcePath)) {
      sendJson(
        exchange,
        404,
        ErrorEnvelope(error = "no_such_file", message = "No such file: $sourcePath"),
      )
      return
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
          renderSingle = { entry, singleReq ->
            awaitOrReplaced(
              renderQueue.submitSingleRender(
                entry = entry,
                direction = singleReq.direction,
                screenId = singleReq.screenId,
                prefix = singleReq.prefix,
                overlayId = singleReq.overlayId,
                view = singleReq.view,
                densityScale = singleReq.densityScale,
                baseCheck = singleReq.baseCheck,
                priority = QueuePriority.AGENT,
                writeFiles = false,
                frameFormat = FrameFormat.LZ4_RGBA,
              ),
            )
          },
        )
      } catch (e: ServerReplacedException) {
        sendJson(
          exchange,
          409,
          ErrorEnvelope(
            error = "server_replaced",
            message = "replaced by v${e.replacedByVersion}, rerun",
          ),
        )
        return
      }
    val resp =
      RenderResponse(
        ok = result.ok,
        valid = result.valid,
        pass = result.pass,
        exitCode = result.exitCode,
        reportPath = result.reportPath?.toString(),
        error = result.error,
        messages = result.messages,
      )
    sendRawJson(
      exchange,
      200,
      PreviewJson.compact.encodeToString(RenderResponse.serializer(), resp),
    )
    maybeScheduleRecycle()
  }

  private fun handleTree(exchange: HttpExchange) {
    val body = readBodyString(exchange, MAX_BODY_BYTES) ?: return
    val req =
      try {
        PreviewJson.compact.decodeFromString(TreeRequest.serializer(), body)
      } catch (e: Exception) {
        sendJson(
          exchange,
          400,
          ErrorEnvelope(error = "bad_request", message = e.message ?: "Invalid JSON body"),
        )
        return
      }
    val sourcePath = Path.of(req.path).toAbsolutePath().normalize()
    val reqRepoRoot =
      req.repoRoot?.takeIf { it.isNotBlank() }?.let(Path::of)
        ?: ServerPaths.resolveRepoRoot(sourcePath)
    val state = getOrCreateRepo(reqRepoRoot)
    if (!Files.isRegularFile(sourcePath)) {
      sendJson(
        exchange,
        404,
        ErrorEnvelope(error = "no_such_file", message = "No such file: $sourcePath"),
      )
      return
    }
    val snapshot = ContentHasher.snapshot(sourcePath)
    val compileEntry =
      try {
        awaitOrReplaced(renderQueue.submitCompile(state.repoRoot, snapshot, QueuePriority.AGENT))
      } catch (e: ServerReplacedException) {
        sendJson(
          exchange,
          409,
          ErrorEnvelope(
            error = "server_replaced",
            message = "replaced by v${e.replacedByVersion}, rerun",
          ),
        )
        return
      }
    val meta = compileEntry.boardMeta
    if (meta == null) {
      sendJson(
        exchange,
        422,
        ErrorEnvelope(
          error = "compile_error",
          message = compileEntry.loaded.errors.joinToString("; ") { it.message },
        ),
      )
      return
    }
    val dir =
      req.direction
        ?.takeIf { it.isNotBlank() }
        ?.let { name ->
          meta.directions.firstOrNull { it.name == name }
            ?: run {
              sendJson(
                exchange,
                400,
                ErrorEnvelope(error = "unknown_direction", message = "Unknown direction '$name'"),
              )
              return
            }
        } ?: meta.directions.first()
    val screenId = req.screen?.takeIf { it.isNotBlank() } ?: dir.startScreen
    if (screenId !in dir.screens) {
      sendJson(
        exchange,
        400,
        ErrorEnvelope(error = "unknown_screen", message = "No screen '$screenId' in '${dir.name}'"),
      )
      return
    }
    val overlayId = req.overlay?.takeIf { it.isNotBlank() }
    if (overlayId != null && dir.overlays.none { it.id == overlayId }) {
      sendJson(
        exchange,
        400,
        ErrorEnvelope(
          error = "unknown_overlay",
          message = "No overlay '$overlayId' in '${dir.name}'",
        ),
      )
      return
    }
    val view =
      req.view
        ?.takeIf { it.isNotBlank() }
        ?.let { name ->
          meta.views.firstOrNull { it.name == name }
            ?: run {
              sendJson(
                exchange,
                400,
                ErrorEnvelope(error = "unknown_view", message = "Unknown view '$name'"),
              )
              return
            }
        } ?: meta.views.first()

    val prefix = if (meta.directions.size == 1) screenId else "${dir.name}__$screenId"
    val artifact =
      try {
        awaitOrReplaced(
          renderQueue.submitSingleRender(
            entry = compileEntry,
            direction = dir.name,
            screenId = screenId,
            prefix = prefix,
            overlayId = overlayId,
            view = view,
            densityScale = 2,
            baseCheck = null,
            priority = QueuePriority.AGENT,
            writeFiles = false,
            frameFormat = FrameFormat.NONE,
          ),
        )
      } catch (e: ServerReplacedException) {
        sendJson(
          exchange,
          409,
          ErrorEnvelope(
            error = "server_replaced",
            message = "replaced by v${e.replacedByVersion}, rerun",
          ),
        )
        return
      }
    val tree = artifact.tree
    if (tree == null) {
      sendJson(
        exchange,
        500,
        ErrorEnvelope(error = "render_failed", message = "Render produced no layout tree"),
      )
      return
    }
    val pruned = req.maxDepth?.let { pruneDepth(tree, it) } ?: tree
    val resp =
      TreeResponse(
        ok = true,
        direction = dir.name,
        screen = screenId,
        overlay = overlayId,
        view = view.name,
        tree = pruned,
      )
    sendRawJson(exchange, 200, PreviewJson.compact.encodeToString(TreeResponse.serializer(), resp))
    maybeScheduleRecycle()
  }

  private fun handleGetSelection(exchange: HttpExchange, query: Map<String, String>) {
    val activeRepo = repos[lastActiveRepoId] ?: repos[defaultRepoId]
    val rawPath = query["path"] ?: activeRepo?.activeSourcePath?.toString()
    if (rawPath.isNullOrBlank()) {
      val resp =
        SelectionResponse(
          ok = false,
          error = "no_selection",
          ask = "Click the element you mean on the preview board, then tell me to continue.",
        )
      sendRawJson(
        exchange,
        404,
        PreviewJson.compact.encodeToString(SelectionResponse.serializer(), resp),
      )
      return
    }
    val sel = selectionStore.current(Path.of(rawPath))
    if (sel == null) {
      val resp =
        SelectionResponse(
          ok = false,
          error = "no_selection",
          ask = "Click the element you mean on the preview board, then tell me to continue.",
        )
      sendRawJson(
        exchange,
        404,
        PreviewJson.compact.encodeToString(SelectionResponse.serializer(), resp),
      )
      return
    }
    val resp = SelectionResponse(ok = true, selection = sel)
    sendRawJson(
      exchange,
      200,
      PreviewJson.compact.encodeToString(SelectionResponse.serializer(), resp),
    )
  }

  private fun handleBoardHtml(exchange: HttpExchange) {
    val nonceBytes = ByteArray(16)
    nonceRng.nextBytes(nonceBytes)
    val nonce = Base64.getEncoder().encodeToString(nonceBytes)
    val html = BoardAssets.renderHtml(nonce).toByteArray(Charsets.UTF_8)
    exchange.responseHeaders.set("Content-Type", "text/html; charset=utf-8")
    exchange.responseHeaders.set("Cache-Control", "no-store")
    exchange.responseHeaders.set("X-Content-Type-Options", "nosniff")
    exchange.responseHeaders.set("Referrer-Policy", "no-referrer")
    exchange.responseHeaders.set(
      "Content-Security-Policy",
      "default-src 'none'; script-src 'nonce-$nonce'; style-src 'nonce-$nonce'; " +
        "img-src 'self' data: blob:; connect-src 'self'; font-src 'none'; object-src 'none'; " +
        "frame-ancestors 'none'; base-uri 'none'",
    )
    exchange.sendResponseHeaders(200, html.size.toLong())
    exchange.responseBody.use { it.write(html) }
  }

  private fun handleGetBoard(exchange: HttpExchange, query: Map<String, String>) {
    val repoId = query["repo"]?.takeIf { it.isNotBlank() }
    val state =
      if (repoId != null) repos[repoId] else (repos[lastActiveRepoId] ?: repos[defaultRepoId])
    val payload = state?.latestBoardPayload
    if (payload == null) {
      sendRawJson(exchange, 200, "{}")
      return
    }
    sendRawJson(
      exchange,
      200,
      PreviewJson.compact.encodeToString(BoardPayload.serializer(), payload),
    )
  }

  private fun handleSse(exchange: HttpExchange, query: Map<String, String>) {
    val requestedRepoId = query["repo"]?.takeIf { it.isNotBlank() }
    exchange.responseHeaders.set("Content-Type", "text/event-stream; charset=utf-8")
    exchange.responseHeaders.set("Cache-Control", "no-store")
    exchange.responseHeaders.set("Connection", "keep-alive")
    exchange.responseHeaders.set("X-Content-Type-Options", "nosniff")
    exchange.sendResponseHeaders(200, 0L)
    val out = exchange.responseBody
    val client = SseClient(requestedRepoId, exchange, out)
    try {
      out.write(": connected\n\n".toByteArray(Charsets.UTF_8))
      val state =
        if (requestedRepoId != null) repos[requestedRepoId]
        else (repos[lastActiveRepoId] ?: repos[defaultRepoId])
      state?.latestBoardPayload?.let { payload ->
        val json = PreviewJson.compact.encodeToString(BoardPayload.serializer(), payload)
        out.write("event: board\ndata: $json\n\n".toByteArray(Charsets.UTF_8))
      }
      out.flush()
      sseClients.add(client)
      val activeSrc = state?.activeSourcePath
      if (state != null && activeSrc != null) {
        handleFileChanged(state, activeSrc, state.activeDesignPath)
      }
      while (!closed.get()) {
        try {
          Thread.sleep(20_000L)
          synchronized(out) {
            out.write(": heartbeat\n\n".toByteArray(Charsets.UTF_8))
            out.flush()
          }
        } catch (_: InterruptedException) {
          break
        } catch (_: Exception) {
          break
        }
      }
    } finally {
      sseClients.remove(client)
      try {
        exchange.close()
      } catch (_: Exception) {
        // Ignore.
      }
    }
  }

  private fun handleBoardRender(exchange: HttpExchange, query: Map<String, String>) {
    val body = readBodyString(exchange, MAX_BODY_BYTES) ?: return
    val req =
      try {
        PreviewJson.compact.decodeFromString(BoardRenderRequest.serializer(), body)
      } catch (e: Exception) {
        sendJson(
          exchange,
          400,
          ErrorEnvelope(error = "bad_request", message = e.message ?: "Invalid JSON body"),
        )
        return
      }
    val requestedRepo =
      req.repo?.takeIf { it.isNotBlank() } ?: query["repo"]?.takeIf { it.isNotBlank() }
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
      sendJson(
        exchange,
        409,
        ErrorEnvelope(
          error = "no_compiled_board",
          message = "Run android-design-preview check before rendering on the board",
        ),
      )
      return
    }
    val dir =
      req.direction
        ?.takeIf { it.isNotBlank() }
        ?.let { name ->
          meta.directions.firstOrNull { it.name == name }
            ?: run {
              sendJson(
                exchange,
                400,
                ErrorEnvelope(error = "unknown_direction", message = "Unknown direction '$name'"),
              )
              return
            }
        } ?: meta.directions.first()
    val screenId = req.screen?.takeIf { it.isNotBlank() } ?: dir.startScreen
    if (screenId !in dir.screens) {
      sendJson(
        exchange,
        400,
        ErrorEnvelope(error = "unknown_screen", message = "No screen '$screenId' in '${dir.name}'"),
      )
      return
    }
    val overlayId = req.overlay?.takeIf { it.isNotBlank() }
    if (overlayId != null && dir.overlays.none { it.id == overlayId }) {
      sendJson(
        exchange,
        400,
        ErrorEnvelope(
          error = "unknown_overlay",
          message = "No overlay '$overlayId' in '${dir.name}'",
        ),
      )
      return
    }
    val baseView =
      req.view
        ?.takeIf { it.isNotBlank() }
        ?.let { name ->
          meta.views.firstOrNull { it.name == name }
            ?: run {
              sendJson(
                exchange,
                400,
                ErrorEnvelope(error = "unknown_view", message = "Unknown view '$name'"),
              )
              return
            }
        } ?: meta.views.first()

    val variantOverride =
      req.variant?.let { wire ->
        PaletteVariant.fromWire(wire)
          ?: run {
            sendJson(
              exchange,
              400,
              ErrorEnvelope(error = "invalid_variant", message = "Unknown variant '$wire'"),
            )
            return
          }
      }
    val fontScale = (req.fontScale ?: 1f).coerceIn(0.5f, 2.5f)
    val hasThemeOverride =
      req.seed != null || req.variant != null || req.dark != null || fontScale != 1f
    val prefix = if (meta.directions.size == 1) screenId else "${dir.name}__$screenId"
    val artifact =
      if (!hasThemeOverride) {
        renderQueue
          .submitSingleRender(
            entry = entry,
            direction = dir.name,
            screenId = screenId,
            prefix = prefix,
            overlayId = overlayId,
            view = baseView,
            densityScale = 2,
            baseCheck = null,
            priority = QueuePriority.BOARD_FOCUS,
            writeFiles = false,
            frameFormat = FrameFormat.WEBP_LOSSLESS,
          )
          .get()
      } else {
        val effectiveView =
          if (req.dark != null && entry.surface == BoardSurface.Phone) {
            baseView.copy(dark = req.dark)
          } else {
            baseView
          }
        renderQueue.renderOverride(
          entry = entry,
          direction = dir.name,
          screenId = screenId,
          prefix = prefix,
          overlayId = overlayId,
          view = effectiveView,
          densityScale = 2,
          frameFormat = FrameFormat.WEBP_LOSSLESS,
          fontScale = fontScale,
          seedOverride = req.seed,
          variantOverride = variantOverride?.wire,
          darkOverride = if (entry.surface == BoardSurface.Phone) req.dark else null,
        )
      }

    liveOwnerRepoId = state.repoId
    executor.updateLive(
      entry = entry,
      LivePreviewUpdate(
        direction = dir.name,
        screenId = screenId,
        overlayId = overlayId,
        darkOverride = if (entry.surface == BoardSurface.Phone) req.dark else null,
        seedOverride = req.seed,
        variantOverride = variantOverride?.wire,
        fontScale = fontScale,
      ),
    )

    val response = toBoardRenderResponse(artifact, entry.contentHash)
    sendRawJson(
      exchange,
      200,
      PreviewJson.compact.encodeToString(BoardRenderResponse.serializer(), response),
    )
  }

  private fun handleBoardFocus(exchange: HttpExchange, query: Map<String, String>) {
    val body = readBodyString(exchange, MAX_BODY_BYTES) ?: return
    val req =
      try {
        PreviewJson.compact.decodeFromString(BoardFocusRequest.serializer(), body)
      } catch (e: Exception) {
        sendJson(
          exchange,
          400,
          ErrorEnvelope(error = "bad_request", message = e.message ?: "Invalid JSON body"),
        )
        return
      }
    val requestedRepo =
      req.repo?.takeIf { it.isNotBlank() } ?: query["repo"]?.takeIf { it.isNotBlank() }
    val state =
      when {
        requestedRepo != null -> repos[requestedRepo]
        !req.path.isNullOrBlank() -> resolveRepoForSource(Path.of(req.path))
        else -> repos[lastActiveRepoId] ?: repos[defaultRepoId]
      } ?: getOrCreateRepo(defaultRepoRoot)

    state.currentFocus =
      FocusTarget(
        direction = req.direction,
        screen = req.screen?.takeIf { it.isNotBlank() },
        overlay = req.overlay?.takeIf { it.isNotBlank() },
        view = req.view,
      )
    liveOwnerRepoId = state.repoId
    state.activeSourcePath?.let { src ->
      renderQueue.latestCompiled(state.repoRoot, src)?.let { entry ->
        executor.updateLive(
          entry = entry,
          LivePreviewUpdate(
            direction = req.direction,
            screenId = req.screen?.takeIf { it.isNotBlank() },
            overlayId = req.overlay?.takeIf { it.isNotBlank() },
          ),
        )
      }
    }
    sendRawJson(exchange, 200, """{"ok":true}""")
  }

  private fun handleBoardSelection(exchange: HttpExchange, query: Map<String, String>) {
    val body = readBodyString(exchange, MAX_BODY_BYTES) ?: return
    val req =
      try {
        PreviewJson.compact.decodeFromString(BoardSelectionRequest.serializer(), body)
      } catch (e: Exception) {
        sendJson(
          exchange,
          400,
          ErrorEnvelope(error = "bad_request", message = e.message ?: "Invalid JSON body"),
        )
        return
      }
    val requestedRepo =
      req.repo?.takeIf { it.isNotBlank() } ?: query["repo"]?.takeIf { it.isNotBlank() }
    val state =
      if (requestedRepo != null) repos[requestedRepo]
      else (repos[lastActiveRepoId] ?: repos[defaultRepoId])
    val targetPath =
      req.path?.takeIf { it.isNotBlank() }?.let(Path::of)
        ?: state?.activeSourcePath
        ?: run {
          sendJson(
            exchange,
            409,
            ErrorEnvelope(error = "no_active_file", message = "No active exploration loaded"),
          )
          return
        }
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
    sendRawJson(
      exchange,
      200,
      PreviewJson.compact.encodeToString(
        SelectionResponse.serializer(),
        SelectionResponse(ok = true, selection = saved),
      ),
    )
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
      artifact.densityScale == 2 &&
        sseClients.any { it.repoId == null || it.repoId == repoId } &&
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

  private fun maybeScheduleRecycle() {
    if (
      renderQueue.compileCount >= maxCompilesBeforeRecycle &&
        recycleScheduled.compareAndSet(false, true)
    ) {
      broadcastSse(null, "recycling", """{"reason":"compile_limit"}""")
      housekeepExecutor.schedule({ stop(triggerExit = true) }, 50, TimeUnit.MILLISECONDS)
    }
  }

  private fun broadcastSse(targetRepoId: String?, event: String, dataJson: String) {
    if (sseClients.isEmpty()) return
    val frame = "event: $event\ndata: $dataJson\n\n".toByteArray(Charsets.UTF_8)
    for (client in sseClients) {
      if (targetRepoId != null && client.repoId != null && client.repoId != targetRepoId) {
        continue
      }
      try {
        synchronized(client.out) {
          client.out.write(frame)
          client.out.flush()
        }
      } catch (_: Exception) {
        sseClients.remove(client)
      }
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

  private fun requireClientSecret(exchange: HttpExchange): Boolean {
    val provided = exchange.requestHeaders.getFirst("X-Client-Secret") ?: ""
    if (!constantTimeEquals(provided, clientSecret)) {
      sendJson(
        exchange,
        403,
        ErrorEnvelope(error = "forbidden_secret", message = "Missing or invalid client secret"),
      )
      return false
    }
    return true
  }

  private fun requireViewToken(exchange: HttpExchange, query: Map<String, String>): Boolean {
    val header = exchange.requestHeaders.getFirst("X-View-Token")
    val provided = header?.takeIf { it.isNotBlank() } ?: query["view"] ?: ""
    if (!constantTimeEquals(provided, viewToken)) {
      sendJson(
        exchange,
        403,
        ErrorEnvelope(error = "forbidden_token", message = "Missing or invalid view token"),
      )
      return false
    }
    return true
  }

  private fun requireBoardOrigin(exchange: HttpExchange): Boolean {
    val origin = exchange.requestHeaders.getFirst("Origin")
    if (origin != origin()) {
      sendJson(
        exchange,
        403,
        ErrorEnvelope(error = "forbidden_origin", message = "Invalid Origin header"),
      )
      return false
    }
    return true
  }

  private fun readBodyString(exchange: HttpExchange, maxBytes: Int): String? {
    val bytes = exchange.requestBody.use { it.readNBytes(maxBytes + 1) }
    if (bytes.size > maxBytes) {
      sendJson(
        exchange,
        413,
        ErrorEnvelope(error = "payload_too_large", message = "Request body exceeds limit"),
      )
      return null
    }
    return bytes.toString(Charsets.UTF_8)
  }

  private fun sendJson(exchange: HttpExchange, status: Int, envelope: ErrorEnvelope) {
    sendRawJson(
      exchange,
      status,
      PreviewJson.compact.encodeToString(ErrorEnvelope.serializer(), envelope),
    )
  }

  private fun sendRawJson(exchange: HttpExchange, status: Int, json: String) {
    val bytes = json.toByteArray(Charsets.UTF_8)
    exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
    exchange.responseHeaders.set("Cache-Control", "no-store")
    exchange.responseHeaders.set("X-Content-Type-Options", "nosniff")
    exchange.responseHeaders.set("Referrer-Policy", "no-referrer")
    exchange.sendResponseHeaders(status, bytes.size.toLong())
    exchange.responseBody.use { it.write(bytes) }
  }

  private fun enumerateBoardTargets(meta: BoardMeta): List<CheckTargetSpec> {
    val targets = resolveMetaTargets(meta.directions, null, directionFilter = null)
    return buildList {
      for (t in targets) {
        for (view in meta.views) {
          add(CheckTargetSpec(t.direction.name, t.screenId, t.prefix, null, view))
        }
      }
    }
  }

  private fun buildStaleViewRefs(entry: CompileEntry?): List<BoardViewRef> {
    val meta = entry?.boardMeta ?: return emptyList()
    return enumerateBoardTargets(meta).map {
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

  companion object {
    const val MAX_COMPILES_BEFORE_RECYCLE = Int.MAX_VALUE
    const val DEFAULT_IDLE_TIMEOUT_MS = 15 * 60 * 1000L
    private const val MAX_BODY_BYTES = 64 * 1024
    private const val REPLACEMENT_DRAIN_WAIT_MS = 450L

    fun constantTimeEquals(a: String, b: String): Boolean {
      if (a.isEmpty() || b.isEmpty()) return false
      return MessageDigest.isEqual(a.toByteArray(Charsets.UTF_8), b.toByteArray(Charsets.UTF_8))
    }

    private fun parseQuery(raw: String?): Map<String, String> {
      if (raw.isNullOrBlank()) return emptyMap()
      val map = LinkedHashMap<String, String>()
      for (part in raw.split('&')) {
        if (part.isBlank()) continue
        val eq = part.indexOf('=')
        val k =
          if (eq >= 0) URLDecoder.decode(part.substring(0, eq), Charsets.UTF_8)
          else URLDecoder.decode(part, Charsets.UTF_8)
        val v = if (eq >= 0) URLDecoder.decode(part.substring(eq + 1), Charsets.UTF_8) else ""
        map[k] = v
      }
      return map
    }
  }
}
