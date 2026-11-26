package bot.charlies.androiddesign.preview.app.android

import bot.charlies.androiddesign.preview.model.DeviceAckResponse
import bot.charlies.androiddesign.preview.model.DeviceHasAssetsRequest
import bot.charlies.androiddesign.preview.model.DeviceLiveStateRequest
import bot.charlies.androiddesign.preview.model.DevicePingResponse
import bot.charlies.androiddesign.preview.model.LiveDeviceCodec
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap

/**
 * Persistent IPC contract between the host server and Android `:sandbox` `RenderForegroundService`
 * for opt-in live mode.
 */
interface DeviceTransport : AutoCloseable {
  fun ping(): DevicePingResponse

  fun syncAssets(assetBlobs: Map<String, ByteArray>): Int

  fun setLive(
    dexBytes: ByteArray?,
    request: DeviceLiveStateRequest,
    assetBlobs: Map<String, ByteArray> = emptyMap(),
  ): Boolean
}

/**
 * Persistent keep-alive TCP bridge speaking `LiveDeviceOp` via [LiveDeviceCodec] to
 * `RenderForegroundService` over `adb forward` on `127.0.0.1:<port>`.
 */
class AdbTcpDeviceTransport(
  private val host: String = "127.0.0.1",
  private val connectAndEnsurePort: () -> Int,
  private val restartSandboxActivity: () -> Unit,
) : DeviceTransport {
  private val lock = Any()
  private var socket: Socket? = null
  private var output: DataOutputStream? = null
  private var input: DataInputStream? = null
  private var activePort: Int = -1
  private var lastSandboxPid: Int = -1
  private val preloadedDexHashes = ConcurrentHashMap.newKeySet<String>()
  private val syncedAssetHashes = ConcurrentHashMap.newKeySet<String>()

  override fun ping(): DevicePingResponse =
    synchronized(lock) {
      withReconnect(allowRetry = true) { out, inp ->
        LiveDeviceCodec.writePingRequest(out)
        val resp = LiveDeviceCodec.readPingResponse(inp)
        noteSandboxPid(resp.pid)
        preloadedDexHashes.clear()
        preloadedDexHashes.addAll(resp.loadedDexHashes)
        resp
      }
    }

  override fun syncAssets(assetBlobs: Map<String, ByteArray>): Int {
    if (assetBlobs.isEmpty()) return 0
    return synchronized(lock) {
      if (assetBlobs.keys.all { it in syncedAssetHashes }) return@synchronized 0
      try {
        withReconnect(allowRetry = true) { out, inp -> syncAssetsOnStream(out, inp, assetBlobs) }
      } catch (t: Throwable) {
        syncedAssetHashes.clear()
        throw t
      }
    }
  }

  override fun setLive(
    dexBytes: ByteArray?,
    request: DeviceLiveStateRequest,
    assetBlobs: Map<String, ByteArray>,
  ): Boolean =
    synchronized(lock) {
      var sentDex = false
      var ack =
        withReconnect(allowRetry = true) { out, inp ->
          syncAssetsOnStream(out, inp, assetBlobs)
          val attemptDex = if (request.dexHash in preloadedDexHashes) null else dexBytes
          sentDex = attemptDex != null
          sendSetLiveOnce(out, inp, attemptDex, request)
        }
      if (!ack.ok && "not preloaded" in ack.error.orEmpty() && dexBytes != null && !sentDex) {
        clearSandboxCaches()
        ack =
          withReconnect(allowRetry = true) { out, inp ->
            syncAssetsOnStream(out, inp, assetBlobs)
            sendSetLiveOnce(out, inp, dexBytes, request)
          }
      }
      if (ack.ok) {
        preloadedDexHashes.add(request.dexHash)
      }
      ack.ok
    }

  override fun close() {
    synchronized(lock) {
      closeSocketQuietly()
      clearSandboxCaches()
    }
  }

  private fun syncAssetsOnStream(
    out: DataOutputStream,
    inp: DataInputStream,
    assetBlobs: Map<String, ByteArray>,
  ): Int {
    if (assetBlobs.isEmpty()) return 0
    val unsynced = assetBlobs.filterKeys { it !in syncedAssetHashes }
    if (unsynced.isEmpty()) return 0
    LiveDeviceCodec.writeHasAssetsRequest(out, DeviceHasAssetsRequest(unsynced.keys.toList()))
    val missingHashes = LiveDeviceCodec.readHasAssetsResponse(inp).missing
    val toSend = missingHashes.mapNotNull { h -> unsynced[h]?.let { b -> h to b } }
    val uploaded =
      if (toSend.isEmpty()) {
        0
      } else {
        LiveDeviceCodec.writePutAssetsRequest(out, toSend)
        val ack = LiveDeviceCodec.readAck(inp)
        if (!ack.ok) {
          throw IOException(ack.error ?: "PUT_ASSETS failed")
        }
        toSend.size
      }
    syncedAssetHashes.addAll(unsynced.keys)
    return uploaded
  }

  private fun sendSetLiveOnce(
    out: DataOutputStream,
    inp: DataInputStream,
    dexBytes: ByteArray?,
    request: DeviceLiveStateRequest,
  ): DeviceAckResponse {
    LiveDeviceCodec.writeSetLiveRequest(out, dexBytes, request)
    return LiveDeviceCodec.readAck(inp)
  }

  private inline fun <T> withReconnect(
    allowRetry: Boolean,
    readTimeoutMs: Int = DEFAULT_READ_TIMEOUT_MS,
    block: (DataOutputStream, DataInputStream) -> T,
  ): T {
    var attempt = 0
    var lastErr: Exception? = null
    while (attempt < 2) {
      attempt++
      try {
        ensureSocketOpen(readTimeoutMs)
        val out = checkNotNull(output)
        val inp = checkNotNull(input)
        return block(out, inp)
      } catch (e: IOException) {
        lastErr = e
        closeSocketQuietly()
        if (!allowRetry || attempt >= 2) break
        clearSandboxCaches()
        restartSandboxActivity()
      }
    }
    throw lastErr ?: IOException("Failed talking to Android preview sandbox")
  }

  private fun ensureSocketOpen(readTimeoutMs: Int) {
    val current = socket
    if (current != null && !current.isClosed && current.isConnected && activePort > 0) {
      current.soTimeout = readTimeoutMs
      return
    }
    val port = connectAndEnsurePort()
    closeSocketQuietly()
    val sock = Socket()
    sock.connect(InetSocketAddress(host, port), CONNECT_TIMEOUT_MS)
    sock.tcpNoDelay = true
    sock.keepAlive = true
    sock.soTimeout = readTimeoutMs
    socket = sock
    activePort = port
    output = DataOutputStream(BufferedOutputStream(sock.getOutputStream(), 65536))
    input = DataInputStream(BufferedInputStream(sock.getInputStream(), 65536))
  }

  private fun noteSandboxPid(pid: Int) {
    if (pid > 0 && pid != lastSandboxPid) {
      if (lastSandboxPid > 0) clearSandboxCaches()
      lastSandboxPid = pid
    }
  }

  private fun clearSandboxCaches() {
    preloadedDexHashes.clear()
    syncedAssetHashes.clear()
  }

  private fun closeSocketQuietly() {
    runCatching { output?.close() }
    runCatching { input?.close() }
    runCatching { socket?.close() }
    output = null
    input = null
    socket = null
    activePort = -1
  }

  companion object {
    private const val CONNECT_TIMEOUT_MS = 3_000
    private const val DEFAULT_READ_TIMEOUT_MS = 30_000
  }
}
