package bot.charlies.androiddesign.preview.host.sandbox

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.LocalServerSocket
import android.net.LocalSocket
import android.os.IBinder
import android.os.Process
import android.util.Log
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalFonts
import bot.charlies.androiddesign.preview.model.DeviceAckResponse
import bot.charlies.androiddesign.preview.model.DeviceHasAssetsRequest
import bot.charlies.androiddesign.preview.model.DeviceHasAssetsResponse
import bot.charlies.androiddesign.preview.model.DeviceLiveStateRequest
import bot.charlies.androiddesign.preview.model.DevicePingResponse
import bot.charlies.androiddesign.preview.model.DeviceTransportSpec
import bot.charlies.androiddesign.preview.model.LiveDeviceCodec
import bot.charlies.androiddesign.preview.model.LiveDeviceOp
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class RenderForegroundService : Service() {
  private val workerExecutor = Executors.newSingleThreadExecutor()
  private val clientExecutor = Executors.newCachedThreadPool { runnable ->
    Thread(runnable, "adp-client").apply { isDaemon = true }
  }
  private val watchdogExecutor = Executors.newSingleThreadScheduledExecutor()
  private val guardedWorker = GuardedWorker(workerExecutor, watchdogExecutor)
  private val activeSockets = ConcurrentHashMap.newKeySet<LocalSocket>()
  private var serverSocket: LocalServerSocket? = null
  @Volatile private var running = true
  private lateinit var renderer: VirtualClockComposeRenderer

  override fun onCreate() {
    super.onCreate()
    ProposalFonts.init(assets)
    renderer = VirtualClockComposeRenderer()
    startAsForeground()
    startSocketServer()
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
          "Preview Live Sandbox",
          NotificationManager.IMPORTANCE_LOW,
        ),
      )
    }
    val notification =
      Notification.Builder(this, channelId)
        .setContentTitle("Android Design Preview Host")
        .setContentText("Live preview session active")
        .setSmallIcon(android.R.drawable.stat_notify_sync)
        .setOngoing(true)
        .build()
    startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
  }

  private fun startSocketServer() {
    Thread(
        {
          try {
            val server = LocalServerSocket(DeviceTransportSpec.SOCKET_NAME)
            serverSocket = server
            Log.i(TAG, "Listening on @${DeviceTransportSpec.SOCKET_NAME} pid=${Process.myPid()}")
            while (running) {
              val client = server.accept()
              if (!isTrustedPeer(client)) {
                runCatching { client.close() }
                continue
              }
              activeSockets.add(client)
              clientExecutor.submit { handleClient(client) }
            }
          } catch (t: Throwable) {
            if (running) {
              Log.e(TAG, "Socket server error", t)
            }
          }
        },
        "adp-accept",
      )
      .start()
  }

  private fun isTrustedPeer(client: LocalSocket): Boolean {
    val uid = runCatching { client.peerCredentials.uid }.getOrDefault(-1)
    if (PeerCredentialPolicy.isTrusted(uid)) return true
    Log.w(TAG, "Rejected connection from uid=$uid")
    return false
  }

  private fun handleClient(socket: LocalSocket) {
    val writeLock = Any()
    try {
      socket.use { s ->
        val input = DataInputStream(BufferedInputStream(s.inputStream, 65536))
        val output = DataOutputStream(BufferedOutputStream(s.outputStream, 65536))
        val sink = ClientSink(s, output, writeLock)
        while (running) {
          val op =
            try {
              LiveDeviceCodec.readOpOrNull(input)
            } catch (_: IOException) {
              null
            } ?: break
          when (op) {
            LiveDeviceOp.PING -> handlePing(sink)
            LiveDeviceOp.PUT_ASSETS -> {
              val items = LiveDeviceCodec.readPutAssetsRequest(input)
              runGuarded("PUT_ASSETS", OP_BUDGET_MS, sink) { executePutAssets(items) }
            }
            LiveDeviceOp.HAS_ASSETS -> {
              val req = LiveDeviceCodec.readHasAssetsRequest(input)
              runGuarded("HAS_ASSETS", OP_BUDGET_MS, sink) { executeHasAssets(req) }
            }
            LiveDeviceOp.SET_LIVE -> {
              val wireReq = LiveDeviceCodec.readSetLiveRequest(input)
              runGuarded("SET_LIVE", OP_BUDGET_MS, sink) {
                executeSetLive(wireReq.dexBytes, wireReq.request)
              }
            }
            else -> {
              sink.send { out ->
                LiveDeviceCodec.writeAck(
                  out,
                  DeviceAckResponse(ok = false, error = "unknown op $op"),
                )
              }
            }
          }
        }
      }
    } finally {
      activeSockets.remove(socket)
    }
  }

  /**
   * Writes a client's replies; on watchdog expiry answers, closes the client, and kills `:sandbox`.
   */
  private inner class ClientSink(
    private val socket: LocalSocket,
    private val output: DataOutputStream,
    private val writeLock: Any,
  ) : ReplySink {
    override fun send(reply: OpReply) {
      synchronized(writeLock) {
        reply.write(output)
        output.flush()
      }
    }

    override fun expired(reply: OpReply) {
      Log.e(TAG, "Watchdog timeout; killing :sandbox process")
      synchronized(writeLock) {
        runCatching {
          reply.write(output)
          output.flush()
          socket.close()
        }
      }
      runCatching { Thread.sleep(50) }
      Process.killProcess(Process.myPid())
    }
  }

  /**
   * Every op that touches user DEX or renderer state goes through here, so none can wedge
   * `:sandbox`.
   */
  private fun runGuarded(
    name: String,
    budgetMs: Long,
    sink: ReplySink,
    timeoutReply: OpReply = opTimeoutReply(name, budgetMs),
    block: () -> OpReply,
  ) {
    guardedWorker.run(name, budgetMs, timeoutReply, sink, block)
  }

  private fun handlePing(sink: ReplySink) {
    val inFlight = guardedWorker.inFlight()
    val resp =
      DevicePingResponse(
        ok = true,
        status = "pong",
        pid = Process.myPid(),
        reloadCount = renderer.currentReloadCount(),
        loadedDexHashes = renderer.cachedDexHashes(),
        memory = renderer.collectMemoryStats(),
        inFlightOp = inFlight?.name,
        inFlightAgeMs = inFlight?.let { guardedWorker.ageMs(it) } ?: 0L,
        inFlightBudgetMs = inFlight?.budgetMs ?: 0L,
      )
    sink.send { out -> LiveDeviceCodec.writePingResponse(out, resp) }
  }

  private fun executePutAssets(items: List<Pair<String, ByteArray>>): OpReply {
    for ((hash, bytes) in items) {
      renderer.assetCache.put(hash, bytes)
    }
    return ackReply(DeviceAckResponse(ok = true, stored = items.size))
  }

  private fun executeHasAssets(req: DeviceHasAssetsRequest): OpReply {
    val missing = renderer.assetCache.missing(req.hashes)
    return OpReply { out ->
      LiveDeviceCodec.writeHasAssetsResponse(out, DeviceHasAssetsResponse(missing = missing))
    }
  }

  private fun executeSetLive(dexBytes: ByteArray?, req: DeviceLiveStateRequest): OpReply {
    val ack =
      try {
        renderer.setLive(dexBytes, req)
        DeviceAckResponse(ok = true)
      } catch (t: Throwable) {
        Log.e(TAG, "SET_LIVE failed", t)
        DeviceAckResponse(ok = false, error = "${t::class.java.name}: ${t.message}")
      }
    return ackReply(ack)
  }

  private fun opTimeoutReply(name: String, budgetMs: Long): OpReply =
    ackReply(
      DeviceAckResponse(
        ok = false,
        error = "$name timed out after $budgetMs ms; sandbox restarted",
      ),
    )

  private fun ackReply(ack: DeviceAckResponse): OpReply = OpReply { out ->
    LiveDeviceCodec.writeAck(out, ack)
  }

  companion object {
    private const val TAG = "AdpRenderService"
    private const val OP_BUDGET_MS = 30_000L
  }
}
