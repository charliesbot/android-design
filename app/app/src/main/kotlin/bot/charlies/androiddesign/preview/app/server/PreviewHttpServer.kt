package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.model.PreviewJson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.io.OutputStream
import java.net.BindException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.file.Path
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlinx.serialization.KSerializer

/** Thin HTTP, auth, CSP, and SSE transport adapter over [PreviewSessionCoordinator]. */
class PreviewHttpServer(
  val repoRoot: Path,
  private val cacheRoot: Path = ServerPaths.cacheRoot(),
  private val preferredPort: Int = ServerPaths.SHARED_PREFERRED_PORT,
  private val existingViewToken: String? = null,
  private val existingClientSecret: String? = null,
  maxCompilesBeforeRecycle: Int = MAX_COMPILES_BEFORE_RECYCLE,
  private val idleTimeoutMs: Long = DEFAULT_IDLE_TIMEOUT_MS,
  coldBudgetMs: Long = Watchdog.COLD_BUDGET_MS,
  warmBudgetMs: Long = Watchdog.WARM_BUDGET_MS,
  worker: RenderWorker,
  private val runtimeIdentity: RuntimeIdentity? = null,
  liveSessionHost: LiveSessionHost = NoOpLiveSessionHost,
  onWatchdogHalt: () -> Unit = { Runtime.getRuntime().halt(1) },
  private val onProcessExit: (() -> Unit)? = null,
  enableFileWatcher: Boolean = true,
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

  private val clientSecret: String = existingClientSecret ?: ServerPaths.randomToken()
  private val viewToken: String = existingViewToken ?: ServerPaths.randomToken()
  private val startedAt: Long = System.currentTimeMillis()
  private val lastActivityMs = AtomicLong(startedAt)
  private val closed = AtomicBoolean(false)

  private val sseClients = CopyOnWriteArrayList<SseClient>()
  private val nonceRng = SecureRandom()

  @Volatile private var httpServer: HttpServer? = null
  @Volatile private var boundPort: Int = 0

  private val httpExecutor = Executors.newCachedThreadPool { r ->
    Thread(r, "android-design-http").apply { isDaemon = true }
  }
  private val housekeepExecutor: ScheduledExecutorService =
    Executors.newSingleThreadScheduledExecutor { r ->
      Thread(r, "android-design-housekeep").apply { isDaemon = true }
    }

  private val coordinator =
    PreviewSessionCoordinator(
      repoRoot = repoRoot,
      cacheRoot = cacheRoot,
      maxCompilesBeforeRecycle = maxCompilesBeforeRecycle,
      coldBudgetMs = coldBudgetMs,
      warmBudgetMs = warmBudgetMs,
      worker = worker,
      liveSessionHost = liveSessionHost,
      onWatchdogHalt = onWatchdogHalt,
      enableFileWatcher = enableFileWatcher,
      boardUrlForRepoId = ::boardUrlForRepoId,
      broadcastSse = ::broadcastSse,
      hasBoardListeners = { repoId ->
        sseClients.any { it.repoId == null || it.repoId == repoId }
      },
      onRecycleRequested = {
        housekeepExecutor.schedule({ stop(triggerExit = true) }, 50, TimeUnit.MILLISECONDS)
      },
    )

  val recordPath: Path
    get() = coordinator.recordPath

  val rendersRoot: Path
    get() = coordinator.rendersRoot

  val selectionStore: SelectionStore
    get() = coordinator.selectionStore

  val renderQueue: RenderQueue
    get() = coordinator.renderQueue

  val wasReplaced: Boolean
    get() = coordinator.wasReplaced

  val port: Int
    get() = boundPort

  fun record(): ServerRecord = ServerPaths.readRecord(recordPath) ?: buildRecord(boundPort)

  fun boardUrlForRepoId(repoId: String?): String =
    if (!repoId.isNullOrBlank()) {
      "http://127.0.0.1:$boundPort/?repo=$repoId&view=$viewToken"
    } else {
      "http://127.0.0.1:$boundPort/?view=$viewToken"
    }

  fun boardUrl(repoRootForUrl: Path? = null): String = coordinator.boardUrl(repoRootForUrl)

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
    coordinator.start()
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
              coordinator.activeInFlightChecks == 0 &&
              System.currentTimeMillis() - lastActivityMs.get() >= idleTimeoutMs
          ) {
            broadcastSse(null, "stopped", """{"reason":"idle_timeout"}""")
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
      repoRoot = coordinator.defaultRepoRoot.toString(),
      clientSecret = clientSecret,
      viewToken = viewToken,
      timeouts = timeouts,
      haltedAt = null,
      runtimeHash = runtimeIdentity?.runtimeHash,
      sourceDir = runtimeIdentity?.sourceDir,
      backend = coordinator.executor.name,
    )

  fun stop(triggerExit: Boolean = false) {
    if (!closed.compareAndSet(false, true)) return
    coordinator.stop()
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
    coordinator.handleFileChanged(sourcePath, designPath)
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
        val requestedRepoId = query["repo"]?.takeIf { it.isNotBlank() }
        val resp = coordinator.status(requestedRepoId, boundPort, startedAt, runtimeIdentity)
        sendSerialized(exchange, 200, StatusResponse.serializer(), resp)
      }
      path == "/api/check" && method == "POST" -> {
        if (!requireClientSecret(exchange)) return
        val req = readJsonBody(exchange, CheckRequest.serializer()) ?: return
        try {
          when (val outcome = coordinator.check(req)) {
            is CheckOutcome.Reply -> {
              sendSerialized(exchange, outcome.status, CheckResponse.serializer(), outcome.response)
              outcome.afterSend()
            }
            is CheckOutcome.HttpError -> sendJson(exchange, outcome.status, outcome.error)
          }
        } finally {
          lastActivityMs.set(System.currentTimeMillis())
        }
      }
      path == "/api/render" && method == "POST" -> {
        if (!requireClientSecret(exchange)) return
        val req = readJsonBody(exchange, RenderRequest.serializer()) ?: return
        try {
          sendOutcome(exchange, RenderResponse.serializer(), coordinator.renderReport(req))
        } finally {
          lastActivityMs.set(System.currentTimeMillis())
        }
      }
      path == "/api/tree" && method == "POST" -> {
        if (!requireClientSecret(exchange)) return
        val req = readJsonBody(exchange, TreeRequest.serializer()) ?: return
        sendOutcome(exchange, TreeResponse.serializer(), coordinator.tree(req))
      }
      path == "/api/selection" && method == "GET" -> {
        if (!requireClientSecret(exchange)) return
        when (val outcome = coordinator.getSelection(query["path"])) {
          is SelectionOutcome.Missing ->
            sendSerialized(exchange, 404, SelectionResponse.serializer(), outcome.response)
          is SelectionOutcome.Found ->
            sendSerialized(exchange, 200, SelectionResponse.serializer(), outcome.response)
        }
      }
      path == "/api/live" && method == "POST" -> {
        if (!requireClientSecret(exchange)) return
        val req = readJsonBody(exchange, LiveFocusRequest.serializer()) ?: return
        sendOutcome(exchange, LiveFocusResponse.serializer(), coordinator.liveUpdate(req))
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
        val payload = coordinator.getBoardPayload(query["repo"])
        if (payload == null) {
          sendRawJson(exchange, 200, "{}")
        } else {
          sendSerialized(exchange, 200, BoardPayload.serializer(), payload)
        }
      }
      path == "/api/events" && method == "GET" -> {
        if (!requireViewToken(exchange, query)) return
        handleSse(exchange, query)
      }
      path == "/api/board/render" && method == "POST" -> {
        if (!requireViewToken(exchange, query)) return
        if (!requireBoardOrigin(exchange)) return
        val req = readJsonBody(exchange, BoardRenderRequest.serializer()) ?: return
        sendOutcome(
          exchange,
          BoardRenderResponse.serializer(),
          coordinator.boardRender(query["repo"], req),
        )
      }
      path == "/api/board/focus" && method == "POST" -> {
        if (!requireViewToken(exchange, query)) return
        if (!requireBoardOrigin(exchange)) return
        val req = readJsonBody(exchange, BoardFocusRequest.serializer()) ?: return
        coordinator.boardFocus(query["repo"], req)
        sendRawJson(exchange, 200, """{"ok":true}""")
      }
      path == "/api/board/selection" && method == "POST" -> {
        if (!requireViewToken(exchange, query)) return
        if (!requireBoardOrigin(exchange)) return
        val req = readJsonBody(exchange, BoardSelectionRequest.serializer()) ?: return
        sendOutcome(
          exchange,
          SelectionResponse.serializer(),
          coordinator.recordBoardSelection(query["repo"], req),
        )
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
      coordinator.markReplaced(repVersion)
      coordinator.awaitReplacementDrain(REPLACEMENT_DRAIN_WAIT_MS)
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
      coordinator.getBoardPayload(requestedRepoId)?.let { payload ->
        val json = PreviewJson.compact.encodeToString(BoardPayload.serializer(), payload)
        out.write("event: board\ndata: $json\n\n".toByteArray(Charsets.UTF_8))
      }
      out.flush()
      sseClients.add(client)
      coordinator.refreshOnSseSubscribe(requestedRepoId)
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

  private fun <T> readJsonBody(exchange: HttpExchange, serializer: KSerializer<T>): T? {
    val body = readBodyString(exchange, MAX_BODY_BYTES) ?: return null
    return try {
      PreviewJson.compact.decodeFromString(serializer, body)
    } catch (e: Exception) {
      sendJson(
        exchange,
        400,
        ErrorEnvelope(error = "bad_request", message = e.message ?: "Invalid JSON body"),
      )
      null
    }
  }

  private fun <T> sendOutcome(
    exchange: HttpExchange,
    serializer: KSerializer<T>,
    outcome: CoordinatorOutcome<T>,
  ) {
    when (outcome) {
      is CoordinatorOutcome.Failure -> sendJson(exchange, outcome.status, outcome.error)
      is CoordinatorOutcome.Success -> {
        sendSerialized(exchange, outcome.status, serializer, outcome.payload)
        outcome.afterSend()
      }
    }
  }

  private fun <T> sendSerialized(
    exchange: HttpExchange,
    status: Int,
    serializer: KSerializer<T>,
    payload: T,
  ) {
    sendRawJson(exchange, status, PreviewJson.compact.encodeToString(serializer, payload))
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
