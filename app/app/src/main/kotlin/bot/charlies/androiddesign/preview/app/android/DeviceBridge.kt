package bot.charlies.androiddesign.preview.app.android

import bot.charlies.androiddesign.preview.domain.image.FastPngEncoder
import bot.charlies.androiddesign.preview.model.DeviceBatchRequest
import bot.charlies.androiddesign.preview.model.DeviceBatchResponse
import bot.charlies.androiddesign.preview.model.DeviceHasAssetsRequest
import bot.charlies.androiddesign.preview.model.DeviceHasAssetsResponse
import bot.charlies.androiddesign.preview.model.DeviceLiveStateRequest
import bot.charlies.androiddesign.preview.model.DeviceOp
import bot.charlies.androiddesign.preview.model.DevicePingResponse
import bot.charlies.androiddesign.preview.model.DevicePreloadResponse
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.PreviewJson
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.SerializationException
import net.jpountz.lz4.LZ4Factory

data class FrameKey(
  val direction: String,
  val screen: String,
  val overlayId: String?,
  val viewName: String,
  val isEndFrame: Boolean,
)

data class DecodedDeviceFrame(
  val key: FrameKey,
  val format: FrameFormat,
  val widthPx: Int,
  val heightPx: Int,
  val pngBytes: ByteArray?,
  val imageBytes: ByteArray,
  val imageMime: String,
)

data class DeviceBatchExecution(
  val response: DeviceBatchResponse,
  val frames: Map<FrameKey, DecodedDeviceFrame>,
)

/**
 * Persistent IPC contract between the host server and Android `:sandbox` `RenderForegroundService`.
 */
interface DeviceTransport : AutoCloseable {
  fun ping(): DevicePingResponse

  fun preloadDex(dexHash: String, entryClass: String, dexBytes: ByteArray): DevicePreloadResponse

  fun syncAssets(assetBlobs: Map<String, ByteArray>): Int

  fun renderBatch(
    dexHash: String,
    entryClass: String,
    dexBytes: ByteArray?,
    request: DeviceBatchRequest,
  ): DeviceBatchExecution

  fun setLive(dexBytes: ByteArray?, request: DeviceLiveStateRequest): Boolean

  fun recycle(): Boolean
}

object FrameCodec {
  private val lz4Decompressor by lazy { LZ4Factory.fastestInstance().fastDecompressor() }

  /**
   * Decodes an LZ4-compressed raw RGBA_8888 frame from `:sandbox` and encodes a PNG on the host CPU
   * using [FastPngEncoder.encodeFast] (`Deflater.BEST_SPEED`).
   */
  fun decodeLz4ToPng(
    widthPx: Int,
    heightPx: Int,
    rawLength: Int,
    lz4Payload: ByteArray,
  ): ByteArray {
    require(widthPx > 0 && heightPx > 0) {
      "Frame dimensions must be positive: ${widthPx}x$heightPx"
    }
    val minRgba = widthPx * heightPx * 4
    require(rawLength >= minRgba) { "Expected rawLength >= $minRgba, got $rawLength" }
    val rgba = ByteArray(rawLength)
    lz4Decompressor.decompress(lz4Payload, 0, rgba, 0, rawLength)
    return FastPngEncoder.encodeFast(widthPx, heightPx, rgba)
  }
}

/**
 * Persistent keep-alive TCP bridge speaking [DeviceOp] to `RenderForegroundService` over `adb
 * forward` on `127.0.0.1:<port>`.
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

  override fun ping(): DevicePingResponse =
    synchronized(lock) {
      withReconnect(allowRetry = true) { out, inp ->
        out.writeByte(DeviceOp.PING)
        out.flush()
        val resp =
          PreviewJson.compact.decodeFromString(DevicePingResponse.serializer(), readUtf(inp))
        noteSandboxPid(resp.pid)
        preloadedDexHashes.clear()
        preloadedDexHashes.addAll(resp.loadedDexHashes)
        resp
      }
    }

  override fun preloadDex(
    dexHash: String,
    entryClass: String,
    dexBytes: ByteArray,
  ): DevicePreloadResponse =
    synchronized(lock) {
      withReconnect(allowRetry = true) { out, inp ->
        out.writeByte(DeviceOp.PRELOAD_DEX)
        writeUtf(out, dexHash)
        writeUtf(out, entryClass)
        out.writeInt(dexBytes.size)
        out.write(dexBytes)
        out.flush()
        val raw = readUtf(inp)
        val resp =
          try {
            PreviewJson.compact.decodeFromString(DevicePreloadResponse.serializer(), raw)
          } catch (_: SerializationException) {
            DevicePreloadResponse(
              ok = false,
              dexHash = dexHash,
              error = "Invalid preload response: $raw",
            )
          }
        if (resp.ok) {
          preloadedDexHashes.add(dexHash)
        }
        resp
      }
    }

  override fun syncAssets(assetBlobs: Map<String, ByteArray>): Int {
    if (assetBlobs.isEmpty()) return 0
    return synchronized(lock) {
      val missingHashes =
        withReconnect(allowRetry = true) { out, inp ->
          out.writeByte(DeviceOp.HAS_ASSETS)
          writeUtf(
            out,
            PreviewJson.compact.encodeToString(
              DeviceHasAssetsRequest.serializer(),
              DeviceHasAssetsRequest(assetBlobs.keys.toList()),
            ),
          )
          out.flush()
          PreviewJson.compact
            .decodeFromString(DeviceHasAssetsResponse.serializer(), readUtf(inp))
            .missing
        }
      if (missingHashes.isEmpty()) return@synchronized 0
      val toSend = missingHashes.mapNotNull { h -> assetBlobs[h]?.let { b -> h to b } }
      if (toSend.isEmpty()) return@synchronized 0
      withReconnect(allowRetry = true) { out, inp ->
        out.writeByte(DeviceOp.PUT_ASSETS)
        out.writeInt(toSend.size)
        for ((hash, bytes) in toSend) {
          writeUtf(out, hash)
          out.writeInt(bytes.size)
          out.write(bytes)
        }
        out.flush()
        readUtf(inp)
        toSend.size
      }
    }
  }

  override fun renderBatch(
    dexHash: String,
    entryClass: String,
    dexBytes: ByteArray?,
    request: DeviceBatchRequest,
  ): DeviceBatchExecution =
    synchronized(lock) {
      val readTimeout = maxOf(request.timeoutMs + 10_000L, DEFAULT_READ_TIMEOUT_MS.toLong()).toInt()
      var sendDex =
        if (dexBytes != null && (request.forceNewClassLoader || dexHash !in preloadedDexHashes)) {
          dexBytes
        } else {
          null
        }
      var result =
        withReconnect(allowRetry = dexBytes != null, readTimeoutMs = readTimeout) { out, inp ->
          sendRenderBatchOnce(out, inp, sendDex, entryClass, request)
        }
      val errText = result.response.error.orEmpty()
      if (
        !result.response.ok && "not preloaded" in errText && dexBytes != null && sendDex == null
      ) {
        sendDex = dexBytes
        result =
          withReconnect(allowRetry = true, readTimeoutMs = readTimeout) { out, inp ->
            sendRenderBatchOnce(out, inp, sendDex, entryClass, request)
          }
      }
      if (result.response.ok || sendDex != null && "not preloaded" !in errText) {
        if (result.response.ok) {
          preloadedDexHashes.add(dexHash)
        }
      }
      if (result.response.recycleReason == "watchdog_timeout") {
        closeSocketQuietly()
        preloadedDexHashes.clear()
      }
      result
    }

  override fun setLive(dexBytes: ByteArray?, request: DeviceLiveStateRequest): Boolean =
    synchronized(lock) {
      val sendDex = if (request.dexHash in preloadedDexHashes) null else dexBytes
      var raw =
        withReconnect(allowRetry = true) { out, inp ->
          sendSetLiveOnce(out, inp, sendDex, request)
        }
      if (
        !raw.contains("\"ok\":true") &&
          "not preloaded" in raw &&
          dexBytes != null &&
          sendDex == null
      ) {
        raw =
          withReconnect(allowRetry = true) { out, inp ->
            sendSetLiveOnce(out, inp, dexBytes, request)
          }
      }
      raw.contains("\"ok\":true")
    }

  override fun recycle(): Boolean =
    synchronized(lock) {
      val ack =
        try {
          withReconnect(allowRetry = false) { out, inp ->
            out.writeByte(DeviceOp.RECYCLE)
            out.flush()
            readUtf(inp).contains("\"ok\":true")
          }
        } catch (_: IOException) {
          false
        } finally {
          closeSocketQuietly()
          preloadedDexHashes.clear()
        }
      restartSandboxActivity()
      ack
    }

  override fun close() {
    synchronized(lock) { closeSocketQuietly() }
  }

  private fun sendRenderBatchOnce(
    out: DataOutputStream,
    inp: DataInputStream,
    dexBytes: ByteArray?,
    entryClass: String,
    request: DeviceBatchRequest,
  ): DeviceBatchExecution {
    out.writeByte(DeviceOp.RENDER_BATCH)
    if (dexBytes == null || dexBytes.isEmpty()) {
      out.writeInt(0)
    } else {
      out.writeInt(dexBytes.size)
      out.write(dexBytes)
    }
    writeUtf(out, entryClass)
    writeUtf(out, PreviewJson.compact.encodeToString(DeviceBatchRequest.serializer(), request))
    out.flush()

    val respJson = readUtf(inp)
    val resp = PreviewJson.compact.decodeFromString(DeviceBatchResponse.serializer(), respJson)
    val frameCount = inp.readInt()
    val frames = LinkedHashMap<FrameKey, DecodedDeviceFrame>(frameCount.coerceAtLeast(0))
    repeat(frameCount) {
      val dir = readUtf(inp)
      val screen = readUtf(inp)
      val viewName = readUtf(inp)
      val ovId = readUtf(inp).ifEmpty { null }
      val isEnd = inp.readBoolean()
      val format = FrameFormat.valueOf(readUtf(inp))
      val widthPx = inp.readInt()
      val heightPx = inp.readInt()
      val rawLen = inp.readInt()
      val payloadLen = inp.readInt()
      val payload = ByteArray(payloadLen)
      inp.readFully(payload)
      val key = FrameKey(dir, screen, ovId, viewName, isEnd)
      val decoded =
        when (format) {
          FrameFormat.LZ4_RGBA -> {
            val png = FrameCodec.decodeLz4ToPng(widthPx, heightPx, rawLen, payload)
            DecodedDeviceFrame(
              key = key,
              format = format,
              widthPx = widthPx,
              heightPx = heightPx,
              pngBytes = png,
              imageBytes = png,
              imageMime = "image/png",
            )
          }
          FrameFormat.WEBP_LOSSLESS ->
            DecodedDeviceFrame(
              key = key,
              format = format,
              widthPx = widthPx,
              heightPx = heightPx,
              pngBytes = null,
              imageBytes = payload,
              imageMime = "image/webp",
            )
          FrameFormat.NONE ->
            DecodedDeviceFrame(
              key = key,
              format = format,
              widthPx = widthPx,
              heightPx = heightPx,
              pngBytes = null,
              imageBytes = ByteArray(0),
              imageMime = "application/octet-stream",
            )
        }
      frames[key] = decoded
    }
    return DeviceBatchExecution(resp, frames)
  }

  private fun sendSetLiveOnce(
    out: DataOutputStream,
    inp: DataInputStream,
    dexBytes: ByteArray?,
    request: DeviceLiveStateRequest,
  ): String {
    out.writeByte(DeviceOp.SET_LIVE)
    if (dexBytes == null || dexBytes.isEmpty()) {
      out.writeInt(0)
    } else {
      out.writeInt(dexBytes.size)
      out.write(dexBytes)
    }
    writeUtf(out, PreviewJson.compact.encodeToString(DeviceLiveStateRequest.serializer(), request))
    out.flush()
    return readUtf(inp)
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
        preloadedDexHashes.clear()
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
      if (lastSandboxPid > 0) preloadedDexHashes.clear()
      lastSandboxPid = pid
    }
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

  private fun writeUtf(out: DataOutputStream, text: String) {
    val bytes = text.toByteArray(StandardCharsets.UTF_8)
    out.writeInt(bytes.size)
    out.write(bytes)
  }

  private fun readUtf(inp: DataInputStream): String {
    val len = inp.readInt()
    if (len < 0) throw EOFException("Invalid packet length: $len")
    val bytes = ByteArray(len)
    inp.readFully(bytes)
    return String(bytes, StandardCharsets.UTF_8)
  }

  companion object {
    private const val CONNECT_TIMEOUT_MS = 3_000
    private const val DEFAULT_READ_TIMEOUT_MS = 30_000
  }
}
