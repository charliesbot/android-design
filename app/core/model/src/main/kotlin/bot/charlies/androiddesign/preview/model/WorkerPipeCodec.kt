package bot.charlies.androiddesign.preview.model

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException

/**
 * Binary wire codec for [WorkerPipeOp] messages exchanged between the host server and the layoutlib
 * child JVM worker over stdin/stdout.
 */
object WorkerPipeCodec {
  const val PING_MAX_BYTES = 1024 * 1024
  private const val MAX_JSON_BYTES = WireFraming.DEFAULT_MAX_JSON_BYTES
  private const val MAX_BUNDLE_BYTES = 32 * 1024 * 1024
  private const val MAX_FRAME_BYTES = WireFraming.DEFAULT_MAX_BLOB_BYTES
  private const val MAX_FRAME_COUNT = 512

  fun readOpOrNull(inp: DataInputStream): Int? = WireFraming.readOpOrNull(inp)

  fun writePingRequest(out: DataOutputStream) {
    out.writeByte(WorkerPipeOp.PING)
    out.flush()
  }

  fun writePingResponse(out: DataOutputStream, response: WorkerPingResponse) {
    WireFraming.writeJson(out, WorkerPingResponse.serializer(), response)
    out.flush()
  }

  fun readPingResponse(inp: DataInputStream): WorkerPingResponse =
    WireFraming.readJson(inp, PING_MAX_BYTES, "PING response", WorkerPingResponse.serializer())

  fun writePreloadClassesRequest(
    out: DataOutputStream,
    classHash: String,
    entryClass: String,
    classBundle: ByteArray,
  ) {
    out.writeByte(WorkerPipeOp.PRELOAD_CLASSES)
    WireFraming.writeUtf8(out, classHash)
    WireFraming.writeUtf8(out, entryClass)
    WireFraming.writeBytes(out, classBundle)
    out.flush()
  }

  fun readPreloadClassesRequest(inp: DataInputStream): WorkerPreloadWireRequest {
    val classHash = WireFraming.readUtf8(inp, MAX_JSON_BYTES)
    val entryClass = WireFraming.readUtf8(inp, MAX_JSON_BYTES)
    val classBundle = WireFraming.readBytes(inp, MAX_BUNDLE_BYTES, "PRELOAD_CLASSES bundle")
    return WorkerPreloadWireRequest(classHash, entryClass, classBundle)
  }

  fun writePreloadClassesResponse(out: DataOutputStream, response: WorkerPreloadResponse) {
    WireFraming.writeJson(out, WorkerPreloadResponse.serializer(), response)
    out.flush()
  }

  fun readPreloadClassesResponse(inp: DataInputStream): WorkerPreloadResponse =
    WireFraming.readJson(
      inp,
      MAX_JSON_BYTES,
      "PRELOAD_CLASSES response",
      WorkerPreloadResponse.serializer(),
    )

  fun writeRenderBatchRequest(
    out: DataOutputStream,
    classBundle: ByteArray?,
    entryClass: String,
    request: WorkerBatchRequest,
  ) {
    out.writeByte(WorkerPipeOp.RENDER_BATCH)
    WireFraming.writeOptionalBytes(out, classBundle)
    WireFraming.writeUtf8(out, entryClass)
    WireFraming.writeJson(out, WorkerBatchRequest.serializer(), request)
    out.flush()
  }

  fun readRenderBatchRequest(inp: DataInputStream): WorkerBatchWireRequest {
    val classBundle = WireFraming.readOptionalBytes(inp, MAX_BUNDLE_BYTES, "RENDER_BATCH bundle")
    val entryClass = WireFraming.readUtf8(inp, MAX_JSON_BYTES)
    val request =
      WireFraming.readJson(
        inp,
        MAX_JSON_BYTES,
        "RENDER_BATCH request",
        WorkerBatchRequest.serializer(),
      )
    return WorkerBatchWireRequest(classBundle, entryClass, request)
  }

  fun writeRenderBatchResponse(
    out: DataOutputStream,
    response: WorkerBatchResponse,
    frames: List<WorkerWireFrame>,
  ) {
    WireFraming.writeJson(out, WorkerBatchResponse.serializer(), response)
    out.writeInt(frames.size)
    for (frame in frames) {
      WireFraming.writeUtf8(out, frame.direction)
      WireFraming.writeUtf8(out, frame.screen)
      WireFraming.writeUtf8(out, frame.viewName)
      WireFraming.writeUtf8(out, frame.overlayId ?: "")
      out.writeBoolean(frame.isEndFrame)
      out.writeInt(frame.widthPx)
      out.writeInt(frame.heightPx)
      WireFraming.writeBytes(out, frame.payload)
    }
    out.flush()
  }

  fun readRenderBatchResponse(inp: DataInputStream): WorkerBatchWireResult {
    val response =
      WireFraming.readJson(
        inp,
        MAX_JSON_BYTES,
        "RENDER_BATCH response",
        WorkerBatchResponse.serializer(),
      )
    val frameCount = inp.readInt()
    if (frameCount < 0 || frameCount > MAX_FRAME_COUNT) {
      throw EOFException("Invalid frame count $frameCount")
    }
    val frames =
      ArrayList<WorkerWireFrame>(frameCount).apply {
        for (i in 0 until frameCount) {
          val direction = WireFraming.readUtf8(inp, MAX_JSON_BYTES)
          val screen = WireFraming.readUtf8(inp, MAX_JSON_BYTES)
          val viewName = WireFraming.readUtf8(inp, MAX_JSON_BYTES)
          val overlayRaw = WireFraming.readUtf8(inp, MAX_JSON_BYTES)
          val isEndFrame = inp.readBoolean()
          val widthPx = inp.readInt()
          val heightPx = inp.readInt()
          val payload = WireFraming.readBytes(inp, MAX_FRAME_BYTES, "RENDER_BATCH frame payload")
          add(
            WorkerWireFrame(
              direction = direction,
              screen = screen,
              viewName = viewName,
              overlayId = overlayRaw.ifEmpty { null },
              isEndFrame = isEndFrame,
              widthPx = widthPx,
              heightPx = heightPx,
              payload = payload,
            ),
          )
        }
      }
    return WorkerBatchWireResult(response, frames)
  }

  fun writeAck(out: DataOutputStream, ack: WorkerAckResponse) {
    WireFraming.writeJson(out, WorkerAckResponse.serializer(), ack)
    out.flush()
  }

  fun readAck(inp: DataInputStream): WorkerAckResponse =
    WireFraming.readJson(inp, PING_MAX_BYTES, "ACK response", WorkerAckResponse.serializer())
}
