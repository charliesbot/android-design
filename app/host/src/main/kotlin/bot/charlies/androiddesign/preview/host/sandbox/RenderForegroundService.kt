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
import bot.charlies.androiddesign.preview.model.PreviewJson
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class RenderForegroundService : Service() {
  private val workerExecutor = Executors.newSingleThreadExecutor()
  private val watchdogExecutor = Executors.newSingleThreadScheduledExecutor()
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
    watchdogExecutor.shutdownNow()
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
              workerExecutor.submit { handleClient(client) }
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
    socket.use { s ->
      val input = DataInputStream(BufferedInputStream(s.getInputStream(), 65536))
      val output = DataOutputStream(BufferedOutputStream(s.getOutputStream(), 65536))
      while (running) {
        val op =
          try {
            input.readByte().toInt()
          } catch (_: EOFException) {
            break
          }
        when (op) {
          DeviceOp.PING -> handlePing(output, writeLock)
          DeviceOp.PRELOAD_DEX -> handlePreloadDex(input, output, writeLock)
          DeviceOp.PUT_ASSETS -> handlePutAssets(input, output, writeLock)
          DeviceOp.HAS_ASSETS -> handleHasAssets(input, output, writeLock)
          DeviceOp.RENDER_BATCH -> handleRenderBatch(s, input, output, writeLock)
          DeviceOp.SET_LIVE -> handleSetLive(input, output, writeLock)
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

  private fun handlePreloadDex(
    input: DataInputStream,
    output: DataOutputStream,
    writeLock: Any,
  ) {
    val dexHash = readUtfBytes(input)
    val entryClass = readUtfBytes(input)
    val dexBytes = readSizedBytes(input)
    val json =
      try {
        renderer.preloadDex(dexHash, entryClass, dexBytes)
        """{"ok":true,"dexHash":"${escapeJson(dexHash)}"}"""
      } catch (t: Throwable) {
        Log.e(TAG, "PRELOAD_DEX failed", t)
        """{"ok":false,"error":"${escapeJson("${t::class.java.name}: ${t.message}")}"}"""
      }
    writeJsonPacket(output, writeLock, json)
  }

  private fun handlePutAssets(
    input: DataInputStream,
    output: DataOutputStream,
    writeLock: Any,
  ) {
    val count = input.readInt()
    for (i in 0 until count) {
      val hash = readUtfBytes(input)
      val bytes = readSizedBytes(input)
      renderer.assetCache.put(hash, bytes)
    }
    writeJsonPacket(output, writeLock, """{"ok":true,"stored":$count}""")
  }

  private fun handleHasAssets(
    input: DataInputStream,
    output: DataOutputStream,
    writeLock: Any,
  ) {
    val reqJson = readUtfBytes(input)
    val req = PreviewJson.compact.decodeFromString(DeviceHasAssetsRequest.serializer(), reqJson)
    val missing = renderer.assetCache.missing(req.hashes)
    val resp = DeviceHasAssetsResponse(missing = missing)
    val json = PreviewJson.compact.encodeToString(DeviceHasAssetsResponse.serializer(), resp)
    writeJsonPacket(output, writeLock, json)
  }

  private fun handleRenderBatch(
    socket: Socket,
    input: DataInputStream,
    output: DataOutputStream,
    writeLock: Any,
  ) {
    val dexBytes = readOptionalSizedBytes(input)
    val entryClass = readUtfBytes(input)
    val reqJson = readUtfBytes(input)
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

  private fun handleSetLive(
    input: DataInputStream,
    output: DataOutputStream,
    writeLock: Any,
  ) {
    val dexBytes = readOptionalSizedBytes(input)
    val reqJson = readUtfBytes(input)
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
