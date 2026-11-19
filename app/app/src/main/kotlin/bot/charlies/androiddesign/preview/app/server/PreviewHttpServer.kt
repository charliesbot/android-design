package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.app.cli.ExploreRenderRunner
import bot.charlies.androiddesign.preview.app.cli.ReportTheme
import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.kit.BoardSurface
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.ValidationError
import bot.charlies.androiddesign.preview.model.ViewSpec
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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Long-running per-repository HTTP + SSE server owning compilation, 1-view-per-job queued
 * rendering, content-hash result caching, file watching, click selection, and the browser board.
 */
class PreviewHttpServer(
  val repoRoot: Path,
  private val cacheRoot: Path = ServerPaths.cacheRoot(),
  private val preferredPort: Int = ServerPaths.preferredPort(repoRoot),
  private val existingViewToken: String? = null,
  private val existingClientSecret: String? = null,
  private val maxCompilesBeforeRecycle: Int = MAX_COMPILES_BEFORE_RECYCLE,
  private val idleTimeoutMs: Long = DEFAULT_IDLE_TIMEOUT_MS,
  private val coldBudgetMs: Long = Watchdog.COLD_BUDGET_MS,
  private val warmBudgetMs: Long = Watchdog.WARM_BUDGET_MS,
  private val loader: ExplorationLoader = ExplorationLoader { src, design ->
    ExploreRenderRunner.loadAndCompile(src, design)
  },
  private val renderer: DensityTargetRenderer? = null,
  private val onWatchdogHalt: () -> Unit = { Runtime.getRuntime().halt(1) },
  private val onProcessExit: (() -> Unit)? = null,
  private val enableFileWatcher: Boolean = true,
) {
  data class FocusTarget(
    val direction: String,
    val screen: String?,
    val overlay: String?,
    val view: String,
  )

  private class SseClient(val exchange: HttpExchange, val out: OutputStream)

  val recordPath: Path = ServerPaths.recordPath(repoRoot, cacheRoot)
  val rendersRoot: Path = ServerPaths.rendersDir(repoRoot, cacheRoot)
  val selectionStore = SelectionStore()

  private val clientSecret: String = existingClientSecret ?: ServerPaths.randomToken()
  private val viewToken: String = existingViewToken ?: ServerPaths.randomToken()
  private val startedAt: Long = System.currentTimeMillis()
  private val lastActivityMs = AtomicLong(startedAt)
  private val closed = AtomicBoolean(false)
  private val recycleScheduled = AtomicBoolean(false)
  private val sseClients = CopyOnWriteArrayList<SseClient>()
  private val lastBoardQueuedHash = ConcurrentHashMap<String, String>()
  private val nonceRng = SecureRandom()

  @Volatile private var httpServer: HttpServer? = null
  @Volatile private var boundPort: Int = 0
  @Volatile private var activeSourcePath: Path? = null
  @Volatile private var activeDesignPath: Path? = null
  @Volatile private var currentFocus: FocusTarget? = null
  @Volatile private var latestBoardPayload: BoardPayload? = null

  private val watchdog =
    Watchdog(
      coldBudgetMs = coldBudgetMs,
      warmBudgetMs = warmBudgetMs,
      onTimeout = { job -> handleWatchdogTimeout(job) },
      onHalt = onWatchdogHalt,
    )

  val renderQueue: RenderQueue =
    RenderQueue(
      rendersRoot = rendersRoot,
      watchdog = watchdog,
      loader = loader,
      renderer =
        renderer
          ?: DensityTargetRenderer { compiled, dir, screenId, view, system, overlayId, density ->
            bot.charlies.androiddesign.preview.feature.preview.sandbox.SandboxHeadlessRenderer
              .render(
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
      onViewRendered = { sourcePath, contentHash, artifact ->
        handleViewRendered(sourcePath, contentHash, artifact)
      },
    )

  private val fileWatcher = FileWatcher { src, design ->
    if (sseClients.isNotEmpty()) {
      handleFileChanged(src, design)
    }
  }

  private val httpExecutor = Executors.newCachedThreadPool { r ->
    Thread(r, "android-design-http").apply { isDaemon = true }
  }
  private val housekeepExecutor: ScheduledExecutorService =
    Executors.newSingleThreadScheduledExecutor { r ->
      Thread(r, "android-design-housekeep").apply { isDaemon = true }
    }

  val port: Int
    get() = boundPort

  fun record(): ServerRecord =
    ServerPaths.readRecord(recordPath)
      ?: ServerRecord(
        pid = ProcessHandle.current().pid(),
        port = boundPort,
        jarVersion = AppInfo.VERSION,
        startedAt = startedAt,
        repoRoot = repoRoot.toAbsolutePath().normalize().toString(),
        clientSecret = clientSecret,
        viewToken = viewToken,
      )

  fun boardUrl(): String = "http://127.0.0.1:$boundPort/?view=$viewToken"

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
    val rec =
      ServerRecord(
        pid = ProcessHandle.current().pid(),
        port = boundPort,
        jarVersion = AppInfo.VERSION,
        startedAt = startedAt,
        repoRoot = repoRoot.toAbsolutePath().normalize().toString(),
        clientSecret = clientSecret,
        viewToken = viewToken,
        timeouts = priorTimeouts,
        haltedAt = null,
      )
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
    httpExecutor.shutdownNow()
    housekeepExecutor.shutdownNow()
    if (triggerExit) {
      onProcessExit?.invoke()
    }
  }

  /** Trigger a file-change cycle directly (used by [FileWatcher] and tests). */
  fun handleFileChanged(sourcePath: Path, designPath: Path?) {
    if (closed.get()) return
    val normSource = sourcePath.toAbsolutePath().normalize()
    val snapshot = ContentHasher.snapshot(normSource, designPath)
    val fileKey = normSource.toString()
    if (lastBoardQueuedHash[fileKey] == snapshot.contentHash) return

    val latest = renderQueue.latestCompiled(normSource)
    clearTimeoutsIfHashChanged(snapshot.contentHash)
    renderQueue.cancelStaleJobs(normSource, snapshot.contentHash)
    broadcastSse(
      "stale",
      PreviewJson.compact.encodeToString(
        StaleEventPayload.serializer(),
        StaleEventPayload(
          contentHash = snapshot.contentHash,
          sourcePath = fileKey,
          views = buildStaleViewRefs(latest),
        ),
      ),
    )

    renderQueue.submitCompile(snapshot, QueuePriority.BOARD_FOCUS).whenComplete { compiledEntry, err
      ->
      if (err != null || compiledEntry == null) {
        if (!renderQueue.isCurrentHash(normSource, snapshot.contentHash)) return@whenComplete
        broadcastSse(
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
      if (!renderQueue.isCurrentHash(normSource, compiledEntry.contentHash)) return@whenComplete
      enqueueBoardRenders(compiledEntry, overallPass = null)
    }
  }

  private fun enqueueBoardRenders(compiledEntry: CompileEntry, overallPass: Boolean?) {
    val normSource = compiledEntry.sourcePath.toAbsolutePath().normalize()
    val fileKey = normSource.toString()
    if (!renderQueue.isCurrentHash(normSource, compiledEntry.contentHash)) return
    lastBoardQueuedHash[fileKey] = compiledEntry.contentHash

    val compiled = compiledEntry.loaded.compiled
    if (compiled == null || compiledEntry.loaded.errors.isNotEmpty()) {
      val payload = buildBoardPayload(compiledEntry, emptyList(), emptyList(), overallPass = false)
      latestBoardPayload = payload
      broadcastSse(
        "board",
        PreviewJson.compact.encodeToString(BoardPayload.serializer(), payload),
      )
      maybeScheduleRecycle()
      return
    }
    val allTargets = enumerateBoardTargets(compiled)
    val focus = currentFocus
    val ordered =
      if (focus != null) {
        val (focused, rest) =
          allTargets.partition { t ->
            t.direction.name == focus.direction &&
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
      BoardViewRef(t.direction.name, t.screenId, t.overlayId, t.view.name)
    }
    val existing2x = renderQueue.completedBoardArtifacts(normSource).values.toList()
    val metaPayload = buildBoardPayload(compiledEntry, existing2x, staleRefs, overallPass)
    latestBoardPayload = metaPayload
    broadcastSse(
      "board",
      PreviewJson.compact.encodeToString(BoardPayload.serializer(), metaPayload),
    )

    ordered.forEachIndexed { idx, (target, prio) ->
      val fut =
        renderQueue.submitRender(
          entry = compiledEntry,
          direction = target.direction,
          screenId = target.screenId,
          prefix = target.prefix,
          overlayId = target.overlayId,
          view = target.view,
          densityScale = 2,
          baseCheck = null,
          priority = prio,
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
        handleStatus(exchange)
      }
      path == "/api/check" && method == "POST" -> {
        if (!requireClientSecret(exchange)) return
        handleCheck(exchange)
      }
      path == "/api/tree" && method == "POST" -> {
        if (!requireClientSecret(exchange)) return
        handleTree(exchange)
      }
      path == "/api/selection" && method == "GET" -> {
        if (!requireClientSecret(exchange)) return
        handleGetSelection(exchange, query)
      }
      path == "/api/stop" && method == "POST" -> {
        if (!requireClientSecret(exchange)) return
        sendRawJson(exchange, 200, """{"ok":true,"status":"stopping"}""")
        housekeepExecutor.schedule({ stop(triggerExit = true) }, 30, TimeUnit.MILLISECONDS)
      }
      (path == "/" || path == "/index.html") && method == "GET" -> {
        if (!requireViewToken(exchange, query)) return
        handleBoardHtml(exchange)
      }
      path == "/api/board" && method == "GET" -> {
        if (!requireViewToken(exchange, query)) return
        handleGetBoard(exchange)
      }
      path == "/api/events" && method == "GET" -> {
        if (!requireViewToken(exchange, query)) return
        handleSse(exchange)
      }
      path == "/api/board/render" && method == "POST" -> {
        if (!requireViewToken(exchange, query)) return
        if (!requireBoardOrigin(exchange)) return
        handleBoardRender(exchange)
      }
      path == "/api/board/focus" && method == "POST" -> {
        if (!requireViewToken(exchange, query)) return
        if (!requireBoardOrigin(exchange)) return
        handleBoardFocus(exchange)
      }
      path == "/api/board/selection" && method == "POST" -> {
        if (!requireViewToken(exchange, query)) return
        if (!requireBoardOrigin(exchange)) return
        handleBoardSelection(exchange)
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

  private fun handleStatus(exchange: HttpExchange) {
    val resp =
      StatusResponse(
        pid = ProcessHandle.current().pid(),
        port = boundPort,
        jarVersion = AppInfo.VERSION,
        startedAt = startedAt,
        compileCount = renderQueue.compileCount,
        repoRoot = repoRoot.toAbsolutePath().normalize().toString(),
        boardUrl = boardUrl(),
        activeFile = activeSourcePath?.toString(),
      )
    sendRawJson(
      exchange,
      200,
      PreviewJson.compact.encodeToString(StatusResponse.serializer(), resp),
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

    val sourcePath = Path.of(req.path).toAbsolutePath().normalize()
    if (!Files.isRegularFile(sourcePath)) {
      sendJson(
        exchange,
        404,
        ErrorEnvelope(error = "no_such_file", message = "No such file: $sourcePath"),
      )
      return
    }
    if (!sourcePath.fileName.toString().endsWith(".explore.kt")) {
      val resp =
        CheckResponse(
          ok = false,
          valid = false,
          pass = false,
          scoped = isScoped(req),
          sourcePath = sourcePath.toString(),
          boardUrl = boardUrl(),
          errors =
            listOf(
              ValidationError(sourcePath.toString(), "Expected an .explore.kt exploration file"),
            ),
        )
      sendRawJson(
        exchange,
        200,
        PreviewJson.compact.encodeToString(CheckResponse.serializer(), resp),
      )
      return
    }

    val explicitDesign = req.design?.takeIf { it.isNotBlank() }?.let(Path::of)
    val snapshot = ContentHasher.snapshot(sourcePath, explicitDesign)
    val scopeKey = buildScopeKey(req)

    val rec = ServerPaths.readRecord(recordPath)
    if (
      rec != null &&
        rec.hasTimeout(
          contentHash = snapshot.contentHash,
          scope = scopeKey,
          direction = req.direction,
          screen = req.screen,
          view = req.view,
        )
    ) {
      val resp =
        CheckResponse(
          ok = false,
          valid = false,
          pass = false,
          scoped = isScoped(req),
          contentHash = snapshot.contentHash,
          sourcePath = sourcePath.toString(),
          designPath = snapshot.designPath?.toString(),
          boardUrl = boardUrl(),
          error = "timeout",
          tell =
            "Previous attempt for this exact file timed out ($scopeKey). Edit and save the file to clear the timeout.",
        )
      sendRawJson(
        exchange,
        200,
        PreviewJson.compact.encodeToString(CheckResponse.serializer(), resp),
      )
      return
    }

    clearTimeoutsIfHashChanged(snapshot.contentHash)
    val previousHash = renderQueue.latestCompiled(sourcePath)?.contentHash
    if (previousHash != null && previousHash != snapshot.contentHash) {
      renderQueue.cancelStaleJobs(sourcePath, snapshot.contentHash)
    }

    activeSourcePath = sourcePath
    activeDesignPath = snapshot.designPath
    if (enableFileWatcher) {
      fileWatcher.watch(sourcePath, snapshot.designPath)
    }

    val compileEntry =
      try {
        renderQueue.submitCompile(snapshot, QueuePriority.AGENT).get()
      } catch (e: Exception) {
        val cause = e.cause ?: e
        sendJson(
          exchange,
          500,
          ErrorEnvelope(error = "compile_error", message = cause.message ?: "Compilation failed"),
        )
        return
      }

    val loaded = compileEntry.loaded
    val compiled = loaded.compiled
    if (compiled == null || loaded.errors.isNotEmpty()) {
      val pendingSel = selectionStore.consumePending(sourcePath)
      val boardPayload = buildBoardPayload(compileEntry, emptyList(), emptyList())
      latestBoardPayload = boardPayload
      broadcastSse(
        "board",
        PreviewJson.compact.encodeToString(BoardPayload.serializer(), boardPayload),
      )
      val resp =
        CheckResponse(
          ok = false,
          valid = false,
          pass = false,
          scoped = isScoped(req),
          contentHash = snapshot.contentHash,
          compileMs = loaded.compileMs,
          compileCount = renderQueue.compileCount,
          cached = compileEntry.fromCache,
          sourcePath = sourcePath.toString(),
          designPath = loaded.designPath?.toString(),
          boardUrl = boardUrl(),
          errors = loaded.errors,
          selection = pendingSel,
        )
      sendRawJson(
        exchange,
        200,
        PreviewJson.compact.encodeToString(CheckResponse.serializer(), resp),
      )
      maybeScheduleRecycle()
      return
    }

    val board = compiled.board
    val surface = board.surfaceSpec
    val directionFilter = req.direction?.takeIf { it.isNotBlank() }?.let { setOf(it.trim()) }
    if (directionFilter != null && board.directions.none { it.name in directionFilter }) {
      val known = board.directions.joinToString { it.name }
      sendJson(
        exchange,
        400,
        ErrorEnvelope(
          error = "unknown_direction",
          message = "Unknown direction '${req.direction}' (available: $known)",
        ),
      )
      return
    }
    val screenFilter = req.screen?.takeIf { it.isNotBlank() }?.let { listOf(it.trim()) }
    val targets =
      ExploreRenderRunner.resolveTargets(board.directions, screenFilter, directionFilter)
    if (targets.isEmpty()) {
      sendJson(
        exchange,
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
        val match = surface.views.firstOrNull { it.name == wanted }
        if (match == null) {
          val known = surface.views.joinToString { it.name }
          sendJson(
            exchange,
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
        surface.views
      }

    val scoped = isScoped(req)
    // Remember most recent agent scope as fallback board focus when no browser is open.
    if (currentFocus == null) {
      currentFocus =
        FocusTarget(
          direction = targets.first().direction.name,
          screen = targets.first().screenId,
          overlay = null,
          view = views.first().name,
        )
    }

    val densityScale = if (scoped) 1 else 2
    val renderStartNs = System.nanoTime()
    val artifacts = mutableListOf<RenderArtifact>()
    for (target in targets) {
      val baseChecks = HashMap<String, ViewCheck>()
      for (view in views) {
        val art =
          renderQueue
            .submitRender(
              entry = compileEntry,
              direction = target.direction,
              screenId = target.screenId,
              prefix = target.prefix,
              overlayId = null,
              view = view,
              densityScale = densityScale,
              baseCheck = null,
              priority = QueuePriority.AGENT,
            )
            .get()
        baseChecks[view.name] = art.check
        artifacts += art
      }
      if (!scoped) {
        for (overlay in target.direction.overlaysFor(target.screenId)) {
          for (view in views) {
            val art =
              renderQueue
                .submitRender(
                  entry = compileEntry,
                  direction = target.direction,
                  screenId = target.screenId,
                  prefix = target.prefix,
                  overlayId = overlay.id,
                  view = view,
                  densityScale = densityScale,
                  baseCheck = baseChecks[view.name],
                  priority = QueuePriority.AGENT,
                )
                .get()
            artifacts += art
          }
        }
      }
    }
    val totalRenderMs = (System.nanoTime() - renderStartNs) / 1_000_000L

    val allPass = artifacts.all { it.pass }
    val selectedImages = selectImagesForResponse(artifacts, scoped, allPass, board.surface)
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

    // Refresh board payload; on 2x full checks the board has fresh renders immediately.
    // On scoped 1x checks, keep board status accurate and schedule focus-first 2x redraws for any
    // open browser clients without blocking the agent response.
    val fileKey = sourcePath.toString()
    val needBackgroundBoard =
      scoped && sseClients.isNotEmpty() && lastBoardQueuedHash[fileKey] != snapshot.contentHash
    if (!scoped) {
      lastBoardQueuedHash[fileKey] = snapshot.contentHash
    }
    val boardRenders =
      if (densityScale == 2) artifacts
      else renderQueue.completedBoardArtifacts(sourcePath).values.toList()
    val boardPayload =
      buildBoardPayload(compileEntry, boardRenders, emptyList(), overallPass = allPass)
    latestBoardPayload = boardPayload
    if (!needBackgroundBoard) {
      broadcastSse(
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
        surface = if (board.surface == BoardSurface.Wear) "wear" else "phone",
        boardName = board.name,
        sourcePath = sourcePath.toString(),
        designPath = loaded.designPath?.toString(),
        boardUrl = boardUrl(),
        errors = emptyList(),
        renders = summaries,
        images = selectedImages,
        selection = pendingSelection,
      )
    sendRawJson(exchange, 200, PreviewJson.compact.encodeToString(CheckResponse.serializer(), resp))
    if (needBackgroundBoard) {
      broadcastSse(
        "stale",
        PreviewJson.compact.encodeToString(
          StaleEventPayload.serializer(),
          StaleEventPayload(
            contentHash = snapshot.contentHash,
            sourcePath = fileKey,
            views = buildStaleViewRefs(compileEntry),
          ),
        ),
      )
      enqueueBoardRenders(compileEntry, overallPass = allPass)
    } else {
      maybeScheduleRecycle()
    }
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
    if (!Files.isRegularFile(sourcePath)) {
      sendJson(
        exchange,
        404,
        ErrorEnvelope(error = "no_such_file", message = "No such file: $sourcePath"),
      )
      return
    }
    val snapshot = ContentHasher.snapshot(sourcePath)
    val compileEntry = renderQueue.submitCompile(snapshot, QueuePriority.AGENT).get()
    val compiled = compileEntry.loaded.compiled
    if (compiled == null) {
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
    val board = compiled.board
    val dir =
      req.direction
        ?.takeIf { it.isNotBlank() }
        ?.let { name ->
          board.directions.firstOrNull { it.name == name }
            ?: run {
              sendJson(
                exchange,
                400,
                ErrorEnvelope(error = "unknown_direction", message = "Unknown direction '$name'"),
              )
              return
            }
        } ?: board.directions.first()
    val screenId = req.screen?.takeIf { it.isNotBlank() } ?: dir.startScreen
    if (screenId !in dir.screenIds) {
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
    val surface = board.surfaceSpec
    val view =
      req.view
        ?.takeIf { it.isNotBlank() }
        ?.let { name ->
          surface.views.firstOrNull { it.name == name }
            ?: run {
              sendJson(
                exchange,
                400,
                ErrorEnvelope(error = "unknown_view", message = "Unknown view '$name'"),
              )
              return
            }
        } ?: surface.views.first()

    val prefix = if (board.directions.size == 1) screenId else "${dir.name}__$screenId"
    val artifact =
      renderQueue
        .submitRender(
          entry = compileEntry,
          direction = dir,
          screenId = screenId,
          prefix = prefix,
          overlayId = overlayId,
          view = view,
          densityScale = 1,
          baseCheck = null,
          priority = QueuePriority.AGENT,
        )
        .get()
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
    val rawPath = query["path"] ?: activeSourcePath?.toString()
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

  private fun handleGetBoard(exchange: HttpExchange) {
    val payload = latestBoardPayload
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

  private fun handleSse(exchange: HttpExchange) {
    exchange.responseHeaders.set("Content-Type", "text/event-stream; charset=utf-8")
    exchange.responseHeaders.set("Cache-Control", "no-store")
    exchange.responseHeaders.set("Connection", "keep-alive")
    exchange.responseHeaders.set("X-Content-Type-Options", "nosniff")
    exchange.sendResponseHeaders(200, 0L)
    val out = exchange.responseBody
    val client = SseClient(exchange, out)
    try {
      out.write(": connected\n\n".toByteArray(Charsets.UTF_8))
      latestBoardPayload?.let { payload ->
        val json = PreviewJson.compact.encodeToString(BoardPayload.serializer(), payload)
        out.write("event: board\ndata: $json\n\n".toByteArray(Charsets.UTF_8))
      }
      out.flush()
      sseClients.add(client)
      activeSourcePath?.let { src -> handleFileChanged(src, activeDesignPath) }
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

  private fun handleBoardRender(exchange: HttpExchange) {
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
    val sourcePath = activeSourcePath
    val entry = sourcePath?.let { renderQueue.latestCompiled(it) }
    val compiled = entry?.loaded?.compiled
    if (entry == null || compiled == null) {
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
    val board = compiled.board
    val dir =
      req.direction
        ?.takeIf { it.isNotBlank() }
        ?.let { name ->
          board.directions.firstOrNull { it.name == name }
            ?: run {
              sendJson(
                exchange,
                400,
                ErrorEnvelope(error = "unknown_direction", message = "Unknown direction '$name'"),
              )
              return
            }
        } ?: board.directions.first()
    val screenId = req.screen?.takeIf { it.isNotBlank() } ?: dir.startScreen
    if (screenId !in dir.screenIds) {
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
    val surface = board.surfaceSpec
    val baseView =
      req.view
        ?.takeIf { it.isNotBlank() }
        ?.let { name ->
          surface.views.firstOrNull { it.name == name }
            ?: run {
              sendJson(
                exchange,
                400,
                ErrorEnvelope(error = "unknown_view", message = "Unknown view '$name'"),
              )
              return
            }
        } ?: surface.views.first()

    val hasThemeOverride = req.seed != null || req.variant != null || req.dark != null
    val prefix = if (board.directions.size == 1) screenId else "${dir.name}__$screenId"
    val artifact =
      if (!hasThemeOverride) {
        renderQueue
          .submitRender(
            entry = entry,
            direction = dir,
            screenId = screenId,
            prefix = prefix,
            overlayId = overlayId,
            view = baseView,
            densityScale = 2,
            baseCheck = null,
            priority = QueuePriority.BOARD_FOCUS,
          )
          .get()
      } else {
        val effectiveView =
          if (req.dark != null && board.surface == BoardSurface.Phone) {
            baseView.copy(dark = req.dark)
          } else {
            baseView
          }
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
        val system =
          board.resolveDesignSystem(
            designMd = entry.loaded.designTheme,
            seedOverride = req.seed,
            variantOverride = variantOverride,
          )
        renderQueue.renderOverride(
          entry = entry,
          direction = dir,
          screenId = screenId,
          prefix = prefix,
          overlayId = overlayId,
          view = effectiveView,
          designSystem = system,
          densityScale = 2,
        )
      }
    val response = toBoardRenderResponse(artifact, entry.contentHash)
    sendRawJson(
      exchange,
      200,
      PreviewJson.compact.encodeToString(BoardRenderResponse.serializer(), response),
    )
  }

  private fun handleBoardFocus(exchange: HttpExchange) {
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
    currentFocus =
      FocusTarget(
        direction = req.direction,
        screen = req.screen?.takeIf { it.isNotBlank() },
        overlay = req.overlay?.takeIf { it.isNotBlank() },
        view = req.view,
      )
    sendRawJson(exchange, 200, """{"ok":true}""")
  }

  private fun handleBoardSelection(exchange: HttpExchange) {
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
    val targetPath =
      req.path?.takeIf { it.isNotBlank() }?.let(Path::of)
        ?: activeSourcePath
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
    sourcePath: Path,
    contentHash: String,
    artifact: RenderArtifact,
  ) {
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
    if (artifact.densityScale == 2 && sseClients.isNotEmpty()) {
      val resp = toBoardRenderResponse(artifact, contentHash)
      broadcastSse(
        "view_updated",
        PreviewJson.compact.encodeToString(BoardRenderResponse.serializer(), resp),
      )
    }
  }

  private fun handleWatchdogTimeout(job: Watchdog.ActiveJob) {
    val now = System.currentTimeMillis()
    val entry =
      TimeoutEntry(
        contentHash = job.contentHash,
        scope = job.scope,
        recordedAt = now,
        reason = "Exceeded ${job.budgetMs} ms budget",
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
    val restartCmd =
      activeSourcePath?.let { "android-design-preview open $it" }
        ?: "android-design-preview open <file>"
    broadcastSse(
      "halted",
      PreviewJson.compact.encodeToString(
        HaltedEventPayload.serializer(),
        HaltedEventPayload(job.contentHash, job.scope, restartCmd),
      ),
    )
  }

  private fun clearTimeoutsIfHashChanged(newHash: String) {
    ServerPaths.updateRecord(recordPath) { rec ->
      if (rec.timeouts.isEmpty()) rec
      else {
        val kept = rec.timeouts.filter { it.contentHash == newHash }
        if (kept.size == rec.timeouts.size) rec else rec.copy(timeouts = kept, haltedAt = null)
      }
    }
  }

  private fun maybeScheduleRecycle() {
    if (
      renderQueue.compileCount >= maxCompilesBeforeRecycle &&
        recycleScheduled.compareAndSet(false, true)
    ) {
      broadcastSse("recycling", """{"reason":"compile_limit"}""")
      housekeepExecutor.schedule({ stop(triggerExit = true) }, 50, TimeUnit.MILLISECONDS)
    }
  }

  private fun broadcastSse(event: String, dataJson: String) {
    if (sseClients.isEmpty()) return
    val frame = "event: $event\ndata: $dataJson\n\n".toByteArray(Charsets.UTF_8)
    for (client in sseClients) {
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

  private data class BoardTarget(
    val direction: bot.charlies.androiddesign.preview.kit.DirectionSpec,
    val screenId: String,
    val prefix: String,
    val overlayId: String?,
    val view: ViewSpec,
  )

  private fun enumerateBoardTargets(
    compiled: bot.charlies.androiddesign.preview.feature.preview.sandbox.CompileResult.Compiled,
  ): List<BoardTarget> {
    val surface = compiled.board.surfaceSpec
    val targets =
      ExploreRenderRunner.resolveTargets(compiled.board.directions, null, directionFilter = null)
    return buildList {
      for (t in targets) {
        for (view in surface.views) {
          add(BoardTarget(t.direction, t.screenId, t.prefix, null, view))
        }
      }
    }
  }

  private fun buildStaleViewRefs(entry: CompileEntry?): List<BoardViewRef> {
    val compiled = entry?.loaded?.compiled ?: return emptyList()
    return enumerateBoardTargets(compiled).map {
      BoardViewRef(it.direction.name, it.screenId, it.overlayId, it.view.name)
    }
  }

  private fun buildBoardPayload(
    entry: CompileEntry,
    artifacts: List<RenderArtifact>,
    staleViews: List<BoardViewRef>,
    overallPass: Boolean? = null,
  ): BoardPayload {
    val loaded = entry.loaded
    val compiled = loaded.compiled
    val board = compiled?.board
    val meta = board?.let { b ->
      BoardMeta(
        directions =
          b.directions.map { dir ->
            DirectionMeta(
              name = dir.name,
              surface = if (dir.surface == BoardSurface.Wear) "wear" else "phone",
              startScreen = dir.startScreen,
              screens = dir.screenIds,
              overlays = dir.overlays.map { ov -> OverlayMeta(ov.id, ov.screen) },
            )
          },
        views = b.surfaceSpec.views,
      )
    }
    val system = loaded.designSystem
    val valid = compiled != null && loaded.errors.isEmpty()
    val pass = valid && (overallPass ?: (artifacts.isNotEmpty() && artifacts.all { it.pass }))
    return BoardPayload(
      valid = valid,
      pass = pass,
      contentHash = entry.contentHash,
      compileMs = loaded.compileMs,
      renderMs = artifacts.sumOf { it.renderMs },
      compileCount = renderQueue.compileCount,
      surface = if (board?.surface == BoardSurface.Wear) "wear" else "phone",
      boardName = board?.name,
      sourcePath = entry.sourcePath.toString(),
      sourceFile = entry.sourcePath.fileName.toString(),
      designPath = loaded.designPath?.toString(),
      theme = ReportTheme(system.seed, system.variant.wire, system.contrast),
      errors = loaded.errors,
      boardMeta = meta,
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
      failures = artifact.entry.failures,
    )
  }

  private fun isScoped(req: CheckRequest): Boolean =
    !req.direction.isNullOrBlank() || !req.screen.isNullOrBlank() || !req.view.isNullOrBlank()

  private fun buildScopeKey(req: CheckRequest): String {
    val parts =
      listOfNotNull(
        req.direction?.takeIf { it.isNotBlank() }?.let { "dir=$it" },
        req.screen?.takeIf { it.isNotBlank() }?.let { "screen=$it" },
        req.view?.takeIf { it.isNotBlank() }?.let { "view=$it" },
      )
    return if (parts.isEmpty()) "full" else parts.joinToString(",")
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
    const val MAX_COMPILES_BEFORE_RECYCLE = 25
    const val DEFAULT_IDLE_TIMEOUT_MS = 15 * 60 * 1000L
    private const val MAX_BODY_BYTES = 64 * 1024

    fun constantTimeEquals(a: String, b: String): Boolean {
      if (a.isEmpty() || b.isEmpty()) return false
      return MessageDigest.isEqual(a.toByteArray(Charsets.UTF_8), b.toByteArray(Charsets.UTF_8))
    }

    /**
     * Image output policy from `visual-design-exploration-v5.md`:
     * - Scoped checks always return their 1x PNG path(s), whether findings pass or fail.
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
      val primaryViews =
        if (surface == BoardSurface.Wear) setOf("watch", "watch-large")
        else setOf("light", "desktop")
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
