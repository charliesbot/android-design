package bot.charlies.androiddesign.preview.host.sandbox

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.os.Process
import android.util.Log
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalFonts
import bot.charlies.androiddesign.preview.model.DeviceBatchRequest
import bot.charlies.androiddesign.preview.model.DeviceBatchResponse
import bot.charlies.androiddesign.preview.model.DeviceHasAssetsRequest
import bot.charlies.androiddesign.preview.model.DeviceHasAssetsResponse
import bot.charlies.androiddesign.preview.model.DeviceLiveStateRequest
import bot.charlies.androiddesign.preview.model.DeviceOp
import bot.charlies.androiddesign.preview.model.DevicePingResponse
import bot.charlies.androiddesign.preview.model.DevicePreloadResponse
import bot.charlies.androiddesign.preview.model.PreviewJson
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class RenderForegroundService : Service() {
  private val workerExecutor = Executors.newSingleThreadExecutor()
  private val clientExecutor = Executors.newCachedThreadPool { runnable ->
    Thread(runnable, "adp-tcp-client").apply { isDaemon = true }
  }
  private val watchdogExecutor = Executors.newSingleThreadScheduledExecutor()
  private val activeSockets = ConcurrentHashMap.newKeySet<Socket>()
  private var serverSocket: ServerSocket? = null
  @Volatile private var running = true
  private lateinit var renderer: VirtualClockComposeRenderer

  override fun onCreate() {
    super.onCreate()
    ProposalFonts.init(assets)
    renderer = VirtualClockComposeRenderer(this)
    startAsForeground()
    startTcpServer()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onDestroy() {
    running = false
    runCatching { serverSocket?.close() }
    activeSockets.forEach { sock -> runCatching { sock.close() } }
    activeSockets.clear()
    watchdogExecutor.shutdownNow()
    clientExecutor.shutdownNow()
    workerExecutor.shutdownNow()
    super.onDestroy()
  }

  private fun startAsForeground() {
    val mgr = getSystemService(NotificationManager::class.java)
    val channelId = "adp_preview_sandbox"
    if (mgr.getNotificationChannel(channelId) == null) {
      mgr.createNotificationChannel(
        NotificationChannel(
          channelId,
          "Preview Render Sandbox",
          NotificationManager.IMPORTANCE_LOW,
        ),
      )
    }
    val notification =
      Notification.Builder(this, channelId)
        .setContentTitle("Android Design Preview Sandbox")
        .setContentText("Offscreen Compose renderer active")
        .setSmallIcon(android.R.drawable.stat_notify_sync)
        .setOngoing(true)
        .build()
    startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
  }

  private fun startTcpServer() {
    Thread(
        {
          try {
            val server =
              ServerSocket(PORT, 8, InetAddress.getByName("127.0.0.1")).apply {
                reuseAddress = true
              }
            serverSocket = server
            Log.i(TAG, "Listening on 127.0.0.1:$PORT pid=${Process.myPid()}")
            while (running) {
              val client = server.accept()
              client.tcpNoDelay = true
              activeSockets.add(client)
              clientExecutor.submit { handleClient(client) }
            }
          } catch (t: Throwable) {
            if (running) {
              Log.e(TAG, "TCP server error", t)
            }
          }
        },
        "adp-tcp-accept",
      )
      .start()
  }

  private fun handleClient(socket: Socket) {
    val writeLock = Any()
    try {
      socket.use { s ->
        val input = DataInputStream(BufferedInputStream(s.getInputStream(), 65536))
        val output = DataOutputStream(BufferedOutputStream(s.getOutputStream(), 65536))
        while (running) {
          val op =
            try {
              input.readByte().toInt()
            } catch (_: IOException) {
              break
            }
          when (op) {
            DeviceOp.PING -> handlePing(output, writeLock)
            DeviceOp.PRELOAD_DEX -> {
              val dexHash = readUtfBytes(input)
              val entryClass = readUtfBytes(input)
              val dexBytes = readSizedBytes(input)
              runOnWorker { executePreloadDex(dexHash, entryClass, dexBytes, output, writeLock) }
            }
            DeviceOp.PUT_ASSETS -> {
              val count = input.readInt()
              val items =
                ArrayList<Pair<String, ByteArray>>(count).apply {
                  for (i in 0 until count) {
                    add(readUtfBytes(input) to readSizedBytes(input))
                  }
                }
              runOnWorker { executePutAssets(items, output, writeLock) }
            }
            DeviceOp.HAS_ASSETS -> {
              val reqJson = readUtfBytes(input)
              runOnWorker { executeHasAssets(reqJson, output, writeLock) }
            }
            DeviceOp.RENDER_BATCH -> {
              val dexBytes = readOptionalSizedBytes(input)
              val entryClass = readUtfBytes(input)
              val reqJson = readUtfBytes(input)
              runOnWorker {
                executeRenderBatch(s, dexBytes, entryClass, reqJson, output, writeLock)
              }
            }
            DeviceOp.SET_LIVE -> {
              val dexBytes = readOptionalSizedBytes(input)
              val reqJson = readUtfBytes(input)
              runOnWorker { executeSetLive(dexBytes, reqJson, output, writeLock) }
            }
            DeviceOp.RECYCLE -> {
              handleRecycle(output, writeLock)
              return
            }
            else -> {
              writeJsonPacket(output, writeLock, """{"ok":false,"error":"unknown op $op"}""")
            }
          }
        }
      }
    } finally {
      activeSockets.remove(socket)
    }
  }

  private fun runOnWorker(block: () -> Unit) {
    try {
      workerExecutor.submit(block).get()
    } catch (e: java.util.concurrent.ExecutionException) {
      throw e.cause ?: e
    }
  }

  private fun handlePing(output: DataOutputStream, writeLock: Any) {
    val resp =
      DevicePingResponse(
        ok = true,
        status = "pong",
        pid = Process.myPid(),
        reloadCount = renderer.currentReloadCount(),
        loadedDexHashes = renderer.cachedDexHashes(),
        memory = renderer.collectMemoryStats(),
      )
    val json = PreviewJson.compact.encodeToString(DevicePingResponse.serializer(), resp)
    writeJsonPacket(output, writeLock, json)
  }

  private fun executePreloadDex(
    dexHash: String,
    entryClass: String,
    dexBytes: ByteArray,
    output: DataOutputStream,
    writeLock: Any,
  ) {
    val resp =
      try {
        val board = renderer.preloadDex(dexHash, entryClass, dexBytes)
        val defaultSystem = board.resolveDesignSystem(null)
        DevicePreloadResponse(
          ok = true,
          dexHash = dexHash,
          boardMeta = renderer.buildBoardMeta(board),
          boardTheme = board.toThemeOverride(),
          defaultSeed = defaultSystem.seed,
          defaultVariant = defaultSystem.variant.wire,
          defaultContrast = defaultSystem.contrast,
        )
      } catch (t: Throwable) {
        Log.e(TAG, "PRELOAD_DEX failed", t)
        DevicePreloadResponse(ok = false, error = "${t::class.java.name}: ${t.message}")
      }
    writeJsonPacket(
      output,
      writeLock,
      PreviewJson.compact.encodeToString(DevicePreloadResponse.serializer(), resp),
    )
  }

  private fun executePutAssets(
    items: List<Pair<String, ByteArray>>,
    output: DataOutputStream,
    writeLock: Any,
  ) {
    for ((hash, bytes) in items) {
      renderer.assetCache.put(hash, bytes)
    }
    writeJsonPacket(output, writeLock, """{"ok":true,"stored":${items.size}}""")
  }

  private fun executeHasAssets(
    reqJson: String,
    output: DataOutputStream,
    writeLock: Any,
  ) {
    val req = PreviewJson.compact.decodeFromString(DeviceHasAssetsRequest.serializer(), reqJson)
    val missing = renderer.assetCache.missing(req.hashes)
    val resp = DeviceHasAssetsResponse(missing = missing)
    val json = PreviewJson.compact.encodeToString(DeviceHasAssetsResponse.serializer(), resp)
    writeJsonPacket(output, writeLock, json)
  }

  private fun executeRenderBatch(
    socket: Socket,
    dexBytes: ByteArray?,
    entryClass: String,
    reqJson: String,
    output: DataOutputStream,
    writeLock: Any,
  ) {
    val request = PreviewJson.compact.decodeFromString(DeviceBatchRequest.serializer(), reqJson)

    val responded = AtomicBoolean(false)
    val timeoutMs = request.timeoutMs.coerceAtLeast(250L)
    val timeoutFuture =
      watchdogExecutor.schedule(
        {
          if (responded.compareAndSet(false, true)) {
            Log.e(TAG, "Watchdog timeout after ${timeoutMs}ms; killing :sandbox process")
            synchronized(writeLock) {
              runCatching {
                val timeoutResp =
                  DeviceBatchResponse(
                    ok = false,
                    error = "Batch render timed out after $timeoutMs ms",
                    reloadCount = renderer.currentReloadCount(),
                    recycleRecommended = true,
                    recycleReason = "watchdog_timeout",
                  )
                writeBatchResult(output, timeoutResp, emptyList())
                socket.close()
              }
            }
            runCatching { Thread.sleep(50) }
            Process.killProcess(Process.myPid())
          }
        },
        timeoutMs,
        TimeUnit.MILLISECONDS,
      )

    val (response, frames) =
      try {
        renderer.executeBatch(dexBytes, entryClass, request)
      } catch (t: Throwable) {
        Log.e(TAG, "Batch render failed", t)
        DeviceBatchResponse(
          ok = false,
          error = "${t::class.java.name}: ${t.message}\n${t.stackTraceToString()}",
          reloadCount = renderer.currentReloadCount(),
          memoryAfter = renderer.collectMemoryStats(),
        ) to emptyList()
      } finally {
        timeoutFuture.cancel(false)
      }

    if (responded.compareAndSet(false, true)) {
      synchronized(writeLock) {
        writeBatchResult(output, response, frames)
      }
    }
  }

  private fun executeSetLive(
    dexBytes: ByteArray?,
    reqJson: String,
    output: DataOutputStream,
    writeLock: Any,
  ) {
    val req = PreviewJson.compact.decodeFromString(DeviceLiveStateRequest.serializer(), reqJson)
    val json =
      try {
        renderer.setLive(dexBytes, req)
        """{"ok":true}"""
      } catch (t: Throwable) {
        Log.e(TAG, "SET_LIVE failed", t)
        """{"ok":false,"error":"${escapeJson("${t::class.java.name}: ${t.message}")}"}"""
      }
    writeJsonPacket(output, writeLock, json)
  }

  private fun handleRecycle(output: DataOutputStream, writeLock: Any) {
    writeJsonPacket(output, writeLock, """{"ok":true}""")
    Thread(
        {
          Thread.sleep(50)
          Process.killProcess(Process.myPid())
        },
        "adp-recycle",
      )
      .start()
  }

  private fun writeBatchResult(
    output: DataOutputStream,
    response: DeviceBatchResponse,
    frames: List<EncodedFramePayload>,
  ) {
    val respBytes =
      PreviewJson.compact
        .encodeToString(DeviceBatchResponse.serializer(), response)
        .toByteArray(StandardCharsets.UTF_8)
    output.writeInt(respBytes.size)
    output.write(respBytes)
    output.writeInt(frames.size)
    for (frame in frames) {
      writeUtfBytes(output, frame.direction)
      writeUtfBytes(output, frame.screen)
      writeUtfBytes(output, frame.viewName)
      writeUtfBytes(output, frame.overlayId.orEmpty())
      output.writeBoolean(frame.isEndFrame)
      writeUtfBytes(output, frame.format.name)
      output.writeInt(frame.widthPx)
      output.writeInt(frame.heightPx)
      output.writeInt(frame.rawLength)
      output.writeInt(frame.payload.size)
      output.write(frame.payload)
    }
    output.flush()
  }

  private fun writeJsonPacket(output: DataOutputStream, writeLock: Any, json: String) {
    val bytes = json.toByteArray(StandardCharsets.UTF_8)
    synchronized(writeLock) {
      output.writeInt(bytes.size)
      output.write(bytes)
      output.flush()
    }
  }

  private fun readOptionalSizedBytes(input: DataInputStream): ByteArray? {
    val len = input.readInt()
    if (len <= 0) return null
    val bytes = ByteArray(len)
    input.readFully(bytes)
    return bytes
  }

  private fun readSizedBytes(input: DataInputStream): ByteArray {
    val len = input.readInt()
    require(len >= 0) { "Negative payload length: $len" }
    val bytes = ByteArray(len)
    input.readFully(bytes)
    return bytes
  }

  private fun readUtfBytes(input: DataInputStream): String =
    String(readSizedBytes(input), StandardCharsets.UTF_8)

  private fun writeUtfBytes(output: DataOutputStream, text: String) {
    val bytes = text.toByteArray(StandardCharsets.UTF_8)
    output.writeInt(bytes.size)
    output.write(bytes)
  }

  private fun escapeJson(value: String): String =
    value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")

  companion object {
    private const val TAG = "AdpRenderService"
    const val PORT = 18765
  }
}
