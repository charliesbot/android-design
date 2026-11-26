package bot.charlies.androiddesign.preview.model

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException

/**
 * Binary wire codec for [LiveDeviceOp] messages sent over ADB forward TCP between
 * `AdbTcpDeviceTransport` and Android `:sandbox` `RenderForegroundService`.
 */
object LiveDeviceCodec {
  const val PING_MAX_BYTES = 1024 * 1024
  private const val MAX_JSON_BYTES = WireFraming.DEFAULT_MAX_JSON_BYTES
  private const val MAX_DEX_BYTES = 32 * 1024 * 1024
  private const val MAX_ASSET_BYTES = WireFraming.DEFAULT_MAX_BLOB_BYTES
  private const val MAX_ASSET_ITEMS = 4096

  fun readOpOrNull(inp: DataInputStream): Int? = WireFraming.readOpOrNull(inp)

  fun writePingRequest(out: DataOutputStream) {
    out.writeByte(LiveDeviceOp.PING)
    out.flush()
  }

  fun writePingResponse(out: DataOutputStream, response: DevicePingResponse) {
    WireFraming.writeJson(out, DevicePingResponse.serializer(), response)
    out.flush()
  }

  fun readPingResponse(inp: DataInputStream): DevicePingResponse =
    WireFraming.readJson(inp, PING_MAX_BYTES, "PING response", DevicePingResponse.serializer())

  fun writeHasAssetsRequest(out: DataOutputStream, req: DeviceHasAssetsRequest) {
    out.writeByte(LiveDeviceOp.HAS_ASSETS)
    WireFraming.writeJson(out, DeviceHasAssetsRequest.serializer(), req)
    out.flush()
  }

  fun readHasAssetsRequest(inp: DataInputStream): DeviceHasAssetsRequest =
    WireFraming.readJson(
      inp,
      MAX_JSON_BYTES,
      "HAS_ASSETS request",
      DeviceHasAssetsRequest.serializer(),
    )

  fun writeHasAssetsResponse(out: DataOutputStream, resp: DeviceHasAssetsResponse) {
    WireFraming.writeJson(out, DeviceHasAssetsResponse.serializer(), resp)
    out.flush()
  }

  fun readHasAssetsResponse(inp: DataInputStream): DeviceHasAssetsResponse =
    WireFraming.readJson(
      inp,
      MAX_JSON_BYTES,
      "HAS_ASSETS response",
      DeviceHasAssetsResponse.serializer(),
    )

  fun writePutAssetsRequest(out: DataOutputStream, items: List<Pair<String, ByteArray>>) {
    out.writeByte(LiveDeviceOp.PUT_ASSETS)
    out.writeInt(items.size)
    for ((hash, bytes) in items) {
      WireFraming.writeUtf8(out, hash)
      WireFraming.writeBytes(out, bytes)
    }
    out.flush()
  }

  fun readPutAssetsRequest(inp: DataInputStream): List<Pair<String, ByteArray>> {
    val count = inp.readInt()
    if (count < 0 || count > MAX_ASSET_ITEMS) {
      throw EOFException("Invalid PUT_ASSETS item count $count")
    }
    return ArrayList<Pair<String, ByteArray>>(count).apply {
      for (i in 0 until count) {
        val hash = WireFraming.readUtf8(inp, MAX_JSON_BYTES)
        val bytes = WireFraming.readBytes(inp, MAX_ASSET_BYTES, "PUT_ASSETS blob")
        add(hash to bytes)
      }
    }
  }

  fun writeSetLiveRequest(
    out: DataOutputStream,
    dexBytes: ByteArray?,
    request: DeviceLiveStateRequest,
  ) {
    out.writeByte(LiveDeviceOp.SET_LIVE)
    WireFraming.writeOptionalBytes(out, dexBytes)
    WireFraming.writeJson(out, DeviceLiveStateRequest.serializer(), request)
    out.flush()
  }

  fun readSetLiveRequest(inp: DataInputStream): DeviceSetLiveWireRequest {
    val dexBytes = WireFraming.readOptionalBytes(inp, MAX_DEX_BYTES, "SET_LIVE dex")
    val request =
      WireFraming.readJson(
        inp,
        MAX_JSON_BYTES,
        "SET_LIVE request",
        DeviceLiveStateRequest.serializer(),
      )
    return DeviceSetLiveWireRequest(dexBytes, request)
  }

  fun writeAck(out: DataOutputStream, ack: DeviceAckResponse) {
    WireFraming.writeJson(out, DeviceAckResponse.serializer(), ack)
    out.flush()
  }

  fun readAck(inp: DataInputStream): DeviceAckResponse =
    WireFraming.readJson(inp, PING_MAX_BYTES, "ACK response", DeviceAckResponse.serializer())
}
