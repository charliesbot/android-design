package bot.charlies.androiddesign.preview.app.android

import bot.charlies.androiddesign.preview.domain.image.FastPngEncoder
import bot.charlies.androiddesign.preview.model.DeviceAckResponse
import bot.charlies.androiddesign.preview.model.DeviceBatchRequest
import bot.charlies.androiddesign.preview.model.DeviceBatchResponse
import bot.charlies.androiddesign.preview.model.DeviceBatchWireResult
import bot.charlies.androiddesign.preview.model.DeviceHasAssetsRequest
import bot.charlies.androiddesign.preview.model.DeviceLiveStateRequest
import bot.charlies.androiddesign.preview.model.DevicePingResponse
import bot.charlies.androiddesign.preview.model.DevicePreloadResponse
import bot.charlies.androiddesign.preview.model.DeviceWireCodec
import bot.charlies.androiddesign.preview.model.DeviceWireFrame
import bot.charlies.androiddesign.preview.model.FrameFormat
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
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
    assetBlobs: Map<String, ByteArray> = emptyMap(),
  ): DeviceBatchExecution

  fun setLive(
    dexBytes: ByteArray?,
    request: DeviceLiveStateRequest,
    assetBlobs: Map<String, ByteArray> = emptyMap(),
  ): Boolean

  fun recycle(): Boolean
}

object FrameCodec {
  private const val MAX_DECODE_THREADS = 8
  private const val DECODE_THREAD_KEEP_ALIVE_SECONDS = 30L

  internal val DEFAULT_DECODE_PARALLELISM: Int =
    Runtime.getRuntime().availableProcessors().coerceIn(1, MAX_DECODE_THREADS)

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

  internal fun decodeWireFrame(frame: DeviceWireFrame): DecodedDeviceFrame {
    val key =
      FrameKey(
        direction = frame.direction,
        screen = frame.screen,
        overlayId = frame.overlayId,
        viewName = frame.viewName,
        isEndFrame = frame.isEndFrame,
      )
    return when (frame.format) {
      FrameFormat.LZ4_RGBA -> {
        val png =
          decodeLz4ToPng(
            widthPx = frame.widthPx,
            heightPx = frame.heightPx,
            rawLength = frame.rawLength,
            lz4Payload = frame.payload,
          )
        DecodedDeviceFrame(
          key = key,
          format = frame.format,
          widthPx = frame.widthPx,
          heightPx = frame.heightPx,
          pngBytes = png,
          imageBytes = png,
          imageMime = "image/png",
        )
      }
      FrameFormat.WEBP_LOSSLESS ->
        DecodedDeviceFrame(
          key = key,
          format = frame.format,
          widthPx = frame.widthPx,
          heightPx = frame.heightPx,
          pngBytes = null,
          imageBytes = frame.payload,
          imageMime = "image/webp",
        )
      FrameFormat.NONE ->
        DecodedDeviceFrame(
          key = key,
          format = frame.format,
          widthPx = frame.widthPx,
          heightPx = frame.heightPx,
          pngBytes = null,
          imageBytes = ByteArray(0),
          imageMime = "application/octet-stream",
        )
    }
  }

  /**
   * Decodes a batch of wire frames into a map preserving wire order. Batches with fewer than two
   * [FrameFormat.LZ4_RGBA] frames decode directly on the calling thread with no executor overhead;
   * larger batches decode LZ4 frames in parallel on the supplied executor.
   */
  internal fun decodeWireFrames(
    wireFrames: List<DeviceWireFrame>,
    executorProvider: () -> ExecutorService,
    maxParallelism: Int = DEFAULT_DECODE_PARALLELISM,
    decoder: (DeviceWireFrame) -> DecodedDeviceFrame = ::decodeWireFrame,
  ): Map<FrameKey, DecodedDeviceFrame> {
    if (wireFrames.isEmpty()) return emptyMap()
    val lz4Count = wireFrames.count { it.format == FrameFormat.LZ4_RGBA }
    if (lz4Count < 2 || maxParallelism <= 1) {
      val frames = LinkedHashMap<FrameKey, DecodedDeviceFrame>(wireFrames.size)
      for (frame in wireFrames) {
        val decoded = decoder(frame)
        frames[decoded.key] = decoded
      }
      return frames
    }

    val executor = executorProvider()
    val futures = arrayOfNulls<Future<DecodedDeviceFrame>>(wireFrames.size)
    try {
      for (i in wireFrames.indices) {
        val frame = wireFrames[i]
        if (frame.format == FrameFormat.LZ4_RGBA) {
          futures[i] = executor.submit(Callable { decoder(frame) })
        }
      }
      val frames = LinkedHashMap<FrameKey, DecodedDeviceFrame>(wireFrames.size)
      for (i in wireFrames.indices) {
        val future = futures[i]
        val decoded = future?.get() ?: decoder(wireFrames[i])
        frames[decoded.key] = decoded
      }
      return frames
    } catch (e: ExecutionException) {
      cancelAll(futures)
      throw e.cause ?: e
    } catch (e: InterruptedException) {
      cancelAll(futures)
      Thread.currentThread().interrupt()
      throw e
    } catch (t: Throwable) {
      cancelAll(futures)
      throw t
    }
  }

  internal fun createDecodeExecutor(maxThreads: Int): ExecutorService {
    val bounded = maxThreads.coerceAtLeast(1)
    val threadCounter = AtomicInteger(1)
    return ThreadPoolExecutor(
        bounded,
        bounded,
        DECODE_THREAD_KEEP_ALIVE_SECONDS,
        TimeUnit.SECONDS,
        LinkedBlockingQueue(),
      ) { runnable ->
        Thread(runnable, "device-frame-decode-${threadCounter.getAndIncrement()}").apply {
          isDaemon = true
        }
      }
      .apply { allowCoreThreadTimeOut(true) }
  }

  private fun cancelAll(futures: Array<Future<DecodedDeviceFrame>?>) {
    for (future in futures) {
      future?.cancel(true)
    }
  }
}

/**
 * Persistent keep-alive TCP bridge speaking `DeviceOp` via [DeviceWireCodec] to
 * `RenderForegroundService` over `adb forward` on `127.0.0.1:<port>`.
 */
class AdbTcpDeviceTransport
internal constructor(
  private val host: String,
  private val connectAndEnsurePort: () -> Int,
  private val restartSandboxActivity: () -> Unit,
  private val decodeParallelism: Int,
  private val decodeExecutorFactory: (Int) -> ExecutorService,
  private val wireFrameDecoder: (DeviceWireFrame) -> DecodedDeviceFrame,
) : DeviceTransport {
  constructor(
    host: String = "127.0.0.1",
    connectAndEnsurePort: () -> Int,
    restartSandboxActivity: () -> Unit,
  ) : this(
    host = host,
    connectAndEnsurePort = connectAndEnsurePort,
    restartSandboxActivity = restartSandboxActivity,
    decodeParallelism = FrameCodec.DEFAULT_DECODE_PARALLELISM,
    decodeExecutorFactory = FrameCodec::createDecodeExecutor,
    wireFrameDecoder = FrameCodec::decodeWireFrame,
  )

  private val lock = Any()
  private val decodeExecutorLock = Any()
  private var socket: Socket? = null
  private var output: DataOutputStream? = null
  private var input: DataInputStream? = null
  private var activePort: Int = -1
  private var lastSandboxPid: Int = -1
  private val preloadedDexHashes = ConcurrentHashMap.newKeySet<String>()
  private val syncedAssetHashes = ConcurrentHashMap.newKeySet<String>()
  private var decodeExecutor: ExecutorService? = null
  private var closed: Boolean = false

  override fun ping(): DevicePingResponse =
    synchronized(lock) {
      withReconnect(allowRetry = true) { out, inp ->
        DeviceWireCodec.writePingRequest(out)
        val resp = DeviceWireCodec.readPingResponse(inp)
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
        DeviceWireCodec.writePreloadDexRequest(out, dexHash, entryClass, dexBytes)
        val resp =
          try {
            DeviceWireCodec.readPreloadDexResponse(inp)
          } catch (e: SerializationException) {
            DevicePreloadResponse(
              ok = false,
              dexHash = dexHash,
              error = "Invalid preload response: ${e.message}",
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
      if (assetBlobs.keys.all { it in syncedAssetHashes }) return@synchronized 0
      try {
        withReconnect(allowRetry = true) { out, inp -> syncAssetsOnStream(out, inp, assetBlobs) }
      } catch (t: Throwable) {
        syncedAssetHashes.clear()
        throw t
      }
    }
  }

  override fun renderBatch(
    dexHash: String,
    entryClass: String,
    dexBytes: ByteArray?,
    request: DeviceBatchRequest,
    assetBlobs: Map<String, ByteArray>,
  ): DeviceBatchExecution {
    val wireResult =
      synchronized(lock) {
        val readTimeout =
          maxOf(request.timeoutMs + 10_000L, DEFAULT_READ_TIMEOUT_MS.toLong()).toInt()
        var sentDex = false
        var result =
          withReconnect(allowRetry = dexBytes != null, readTimeoutMs = readTimeout) { out, inp ->
            syncAssetsOnStream(out, inp, assetBlobs)
            val attemptDex =
              if (
                dexBytes != null && (request.forceNewClassLoader || dexHash !in preloadedDexHashes)
              ) {
                dexBytes
              } else {
                null
              }
            sentDex = attemptDex != null
            sendRenderBatchOnce(out, inp, attemptDex, entryClass, request)
          }
        val errText = result.response.error.orEmpty()
        if (!result.response.ok && "not preloaded" in errText && dexBytes != null && !sentDex) {
          clearSandboxCaches()
          result =
            withReconnect(allowRetry = true, readTimeoutMs = readTimeout) { out, inp ->
              syncAssetsOnStream(out, inp, assetBlobs)
              sendRenderBatchOnce(out, inp, dexBytes, entryClass, request)
            }
        }
        if (result.response.ok) {
          preloadedDexHashes.add(dexHash)
        }
        if (result.response.recycleReason == "watchdog_timeout") {
          closeSocketQuietly()
          clearSandboxCaches()
        }
        result
      }
    val frames =
      FrameCodec.decodeWireFrames(
        wireFrames = wireResult.frames,
        executorProvider = ::getOrCreateDecodeExecutor,
        maxParallelism = decodeParallelism,
        decoder = wireFrameDecoder,
      )
    return DeviceBatchExecution(wireResult.response, frames)
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

  override fun recycle(): Boolean =
    synchronized(lock) {
      val ack =
        try {
          withReconnect(allowRetry = false) { out, inp ->
            DeviceWireCodec.writeRecycleRequest(out)
            DeviceWireCodec.readAck(inp).ok
          }
        } catch (_: IOException) {
          false
        } finally {
          closeSocketQuietly()
          clearSandboxCaches()
        }
      restartSandboxActivity()
      ack
    }

  override fun close() {
    synchronized(lock) {
      closeSocketQuietly()
      clearSandboxCaches()
    }
    val executor =
      synchronized(decodeExecutorLock) {
        closed = true
        decodeExecutor.also { decodeExecutor = null }
      }
    executor?.shutdown()
  }

  private fun getOrCreateDecodeExecutor(): ExecutorService =
    synchronized(decodeExecutorLock) {
      check(!closed) { "AdbTcpDeviceTransport is closed" }
      decodeExecutor ?: decodeExecutorFactory(decodeParallelism).also { decodeExecutor = it }
    }

  private fun syncAssetsOnStream(
    out: DataOutputStream,
    inp: DataInputStream,
    assetBlobs: Map<String, ByteArray>,
  ): Int {
    if (assetBlobs.isEmpty()) return 0
    val unsynced = assetBlobs.filterKeys { it !in syncedAssetHashes }
    if (unsynced.isEmpty()) return 0
    DeviceWireCodec.writeHasAssetsRequest(out, DeviceHasAssetsRequest(unsynced.keys.toList()))
    val missingHashes = DeviceWireCodec.readHasAssetsResponse(inp).missing
    val toSend = missingHashes.mapNotNull { h -> unsynced[h]?.let { b -> h to b } }
    val uploaded =
      if (toSend.isEmpty()) {
        0
      } else {
        DeviceWireCodec.writePutAssetsRequest(out, toSend)
        val ack = DeviceWireCodec.readAck(inp)
        if (!ack.ok) {
          throw IOException(ack.error ?: "PUT_ASSETS failed")
        }
        toSend.size
      }
    syncedAssetHashes.addAll(unsynced.keys)
    return uploaded
  }

  private fun sendRenderBatchOnce(
    out: DataOutputStream,
    inp: DataInputStream,
    dexBytes: ByteArray?,
    entryClass: String,
    request: DeviceBatchRequest,
  ): DeviceBatchWireResult {
    DeviceWireCodec.writeRenderBatchRequest(out, dexBytes, entryClass, request)
    return DeviceWireCodec.readRenderBatchResponse(inp)
  }

  private fun sendSetLiveOnce(
    out: DataOutputStream,
    inp: DataInputStream,
    dexBytes: ByteArray?,
    request: DeviceLiveStateRequest,
  ): DeviceAckResponse {
    DeviceWireCodec.writeSetLiveRequest(out, dexBytes, request)
    return DeviceWireCodec.readAck(inp)
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
