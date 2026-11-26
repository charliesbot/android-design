package bot.charlies.androiddesign.preview.model

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.nio.charset.StandardCharsets
import kotlinx.serialization.KSerializer

/**
 * Symmetric binary framing codec for every [DeviceOp] message exchanged between the host
 * (`AdbTcpDeviceTransport`, `DeviceManager`) and the Android `:sandbox` `RenderForegroundService`.
 */
object DeviceWireCodec {
  const val PING_MAX_BYTES: Int = 1024 * 1024

  /** Reads the next 1-byte [DeviceOp] opcode, or null on clean EOF. */
  fun readOpOrNull(inp: DataInputStream): Int? = inp.read().takeIf { it >= 0 }

  /** Writes and flushes a [DeviceOp.PING] request frame. */
  fun writePingRequest(out: DataOutputStream) {
    out.writeByte(DeviceOp.PING)
    out.flush()
  }

  /** Writes and flushes a length-prefixed [DevicePingResponse] JSON packet. */
  fun writePingResponse(out: DataOutputStream, response: DevicePingResponse) {
    writeJson(out, DevicePingResponse.serializer(), response)
    out.flush()
  }

  /** Reads a [DevicePingResponse], rejecting empty or oversized packets above [maxBytes]. */
  fun readPingResponse(
    inp: DataInputStream,
    maxBytes: Int = PING_MAX_BYTES,
  ): DevicePingResponse {
    val json = readUtf(inp, maxBytes = maxBytes, allowZero = false)
    return PreviewJson.compact.decodeFromString(DevicePingResponse.serializer(), json)
  }

  /** Writes and flushes a [DeviceOp.PRELOAD_DEX] request carrying hash, class, and DEX bytes. */
  fun writePreloadDexRequest(
    out: DataOutputStream,
    dexHash: String,
    entryClass: String,
    dexBytes: ByteArray,
  ) {
    out.writeByte(DeviceOp.PRELOAD_DEX)
    writeUtf(out, dexHash)
    writeUtf(out, entryClass)
    writeSizedBytes(out, dexBytes)
    out.flush()
  }

  /** Reads a [PreloadDexWireRequest] payload following a [DeviceOp.PRELOAD_DEX] opcode. */
  fun readPreloadDexRequest(inp: DataInputStream): PreloadDexWireRequest =
    PreloadDexWireRequest(
      dexHash = readUtf(inp),
      entryClass = readUtf(inp),
      dexBytes = readSizedBytes(inp),
    )

  /** Writes and flushes a [DevicePreloadResponse] JSON packet. */
  fun writePreloadDexResponse(out: DataOutputStream, response: DevicePreloadResponse) {
    writeJson(out, DevicePreloadResponse.serializer(), response)
    out.flush()
  }

  /** Reads a [DevicePreloadResponse] JSON packet. */
  fun readPreloadDexResponse(inp: DataInputStream): DevicePreloadResponse =
    readJson(inp, DevicePreloadResponse.serializer())

  /** Writes and flushes a [DeviceOp.HAS_ASSETS] request frame. */
  fun writeHasAssetsRequest(out: DataOutputStream, request: DeviceHasAssetsRequest) {
    out.writeByte(DeviceOp.HAS_ASSETS)
    writeJson(out, DeviceHasAssetsRequest.serializer(), request)
    out.flush()
  }

  /** Reads a [DeviceHasAssetsRequest] payload following a [DeviceOp.HAS_ASSETS] opcode. */
  fun readHasAssetsRequest(inp: DataInputStream): DeviceHasAssetsRequest =
    readJson(inp, DeviceHasAssetsRequest.serializer())

  /** Writes and flushes a [DeviceHasAssetsResponse] JSON packet. */
  fun writeHasAssetsResponse(out: DataOutputStream, response: DeviceHasAssetsResponse) {
    writeJson(out, DeviceHasAssetsResponse.serializer(), response)
    out.flush()
  }

  /** Reads a [DeviceHasAssetsResponse] JSON packet. */
  fun readHasAssetsResponse(inp: DataInputStream): DeviceHasAssetsResponse =
    readJson(inp, DeviceHasAssetsResponse.serializer())

  /** Writes and flushes a [DeviceOp.PUT_ASSETS] batch of `(hash, bytes)` asset entries. */
  fun writePutAssetsRequest(out: DataOutputStream, items: List<Pair<String, ByteArray>>) {
    out.writeByte(DeviceOp.PUT_ASSETS)
    out.writeInt(items.size)
    for ((hash, bytes) in items) {
      writeUtf(out, hash)
      writeSizedBytes(out, bytes)
    }
    out.flush()
  }

  /** Reads a [DeviceOp.PUT_ASSETS] batch of `(hash, bytes)` pairs, rejecting negative counts. */
  fun readPutAssetsRequest(inp: DataInputStream): List<Pair<String, ByteArray>> {
    val count = inp.readInt()
    if (count < 0) throw EOFException("Invalid asset item count: $count")
    return List(count) { readUtf(inp) to readSizedBytes(inp) }
  }

  /** Writes and flushes a [DeviceOp.RENDER_BATCH] request with optional inline DEX bytes. */
  fun writeRenderBatchRequest(
    out: DataOutputStream,
    dexBytes: ByteArray?,
    entryClass: String,
    request: DeviceBatchRequest,
  ) {
    out.writeByte(DeviceOp.RENDER_BATCH)
    writeOptionalBytes(out, dexBytes)
    writeUtf(out, entryClass)
    writeJson(out, DeviceBatchRequest.serializer(), request)
    out.flush()
  }

  /** Reads a [RenderBatchWireRequest] following a [DeviceOp.RENDER_BATCH] opcode. */
  fun readRenderBatchRequest(inp: DataInputStream): RenderBatchWireRequest {
    val dexBytes = readOptionalBytes(inp)
    val entryClass = readUtf(inp)
    val request = readJson(inp, DeviceBatchRequest.serializer())
    return RenderBatchWireRequest(dexBytes = dexBytes, entryClass = entryClass, request = request)
  }

  /** Writes and flushes a [DeviceBatchResponse] followed by its encoded [frames]. */
  fun writeRenderBatchResponse(
    out: DataOutputStream,
    response: DeviceBatchResponse,
    frames: List<DeviceWireFrame>,
  ) {
    writeJson(out, DeviceBatchResponse.serializer(), response)
    out.writeInt(frames.size)
    for (frame in frames) {
      writeUtf(out, frame.direction)
      writeUtf(out, frame.screen)
      writeUtf(out, frame.viewName)
      // Empty string encodes null overlayId on the wire so field framing stays fixed-arity.
      writeUtf(out, frame.overlayId.orEmpty())
      out.writeBoolean(frame.isEndFrame)
      writeUtf(out, frame.format.name)
      out.writeInt(frame.widthPx)
      out.writeInt(frame.heightPx)
      out.writeInt(frame.rawLength)
      writeSizedBytes(out, frame.payload)
    }
    out.flush()
  }

  /** Reads a [DeviceBatchResponse] and its trailing [DeviceWireFrame] list. */
  fun readRenderBatchResponse(inp: DataInputStream): DeviceBatchWireResult {
    val response = readJson(inp, DeviceBatchResponse.serializer())
    val frameCount = inp.readInt()
    if (frameCount < 0) throw EOFException("Invalid frame count: $frameCount")
    val frames =
      List(frameCount) {
        val direction = readUtf(inp)
        val screen = readUtf(inp)
        val viewName = readUtf(inp)
        // Mirror writeRenderBatchResponse: empty wire string restores null overlayId.
        val overlayId = readUtf(inp).ifEmpty { null }
        val isEndFrame = inp.readBoolean()
        val format = FrameFormat.valueOf(readUtf(inp))
        val widthPx = inp.readInt()
        val heightPx = inp.readInt()
        val rawLength = inp.readInt()
        val payload = readSizedBytes(inp)
        DeviceWireFrame(
          direction = direction,
          screen = screen,
          viewName = viewName,
          overlayId = overlayId,
          isEndFrame = isEndFrame,
          format = format,
          widthPx = widthPx,
          heightPx = heightPx,
          rawLength = rawLength,
          payload = payload,
        )
      }
    return DeviceBatchWireResult(response = response, frames = frames)
  }

  /** Writes and flushes a [DeviceOp.SET_LIVE] request with optional inline DEX bytes. */
  fun writeSetLiveRequest(
    out: DataOutputStream,
    dexBytes: ByteArray?,
    request: DeviceLiveStateRequest,
  ) {
    out.writeByte(DeviceOp.SET_LIVE)
    writeOptionalBytes(out, dexBytes)
    writeJson(out, DeviceLiveStateRequest.serializer(), request)
    out.flush()
  }

  /** Reads a [SetLiveWireRequest] following a [DeviceOp.SET_LIVE] opcode. */
  fun readSetLiveRequest(inp: DataInputStream): SetLiveWireRequest {
    val dexBytes = readOptionalBytes(inp)
    val request = readJson(inp, DeviceLiveStateRequest.serializer())
    return SetLiveWireRequest(dexBytes = dexBytes, request = request)
  }

  /** Writes and flushes a [DeviceAckResponse] JSON packet. */
  fun writeAck(out: DataOutputStream, response: DeviceAckResponse) {
    writeJson(out, DeviceAckResponse.serializer(), response)
    out.flush()
  }

  /** Reads a [DeviceAckResponse] JSON packet. */
  fun readAck(inp: DataInputStream): DeviceAckResponse =
    readJson(inp, DeviceAckResponse.serializer())

  private fun <T> writeJson(out: DataOutputStream, serializer: KSerializer<T>, value: T) {
    writeUtf(out, PreviewJson.compact.encodeToString(serializer, value))
  }

  private fun <T> readJson(inp: DataInputStream, serializer: KSerializer<T>): T =
    PreviewJson.compact.decodeFromString(serializer, readUtf(inp))

  private fun writeUtf(out: DataOutputStream, text: String) {
    writeSizedBytes(out, text.toByteArray(StandardCharsets.UTF_8))
  }

  private fun readUtf(
    inp: DataInputStream,
    maxBytes: Int = Int.MAX_VALUE,
    allowZero: Boolean = true,
  ): String =
    String(
      readSizedBytes(inp, maxBytes = maxBytes, allowZero = allowZero),
      StandardCharsets.UTF_8,
    )

  private fun writeSizedBytes(out: DataOutputStream, bytes: ByteArray) {
    out.writeInt(bytes.size)
    out.write(bytes)
  }

  private fun readSizedBytes(
    inp: DataInputStream,
    maxBytes: Int = Int.MAX_VALUE,
    allowZero: Boolean = true,
  ): ByteArray {
    val len = inp.readInt()
    val minLen = if (allowZero) 0 else 1
    if (len < minLen || len > maxBytes) throw EOFException("Invalid packet length: $len")
    val bytes = ByteArray(len)
    inp.readFully(bytes)
    return bytes
  }

  private fun writeOptionalBytes(out: DataOutputStream, bytes: ByteArray?) {
    // Length 0 signals "use DEX already cached on :sandbox"; positive length supplies DEX inline.
    if (bytes == null || bytes.isEmpty()) {
      out.writeInt(0)
    } else {
      writeSizedBytes(out, bytes)
    }
  }

  private fun readOptionalBytes(inp: DataInputStream): ByteArray? =
    // Empty payload means the caller omitted inline DEX bytes because it expects cache hit.
    readSizedBytes(inp).takeIf { it.isNotEmpty() }
}
