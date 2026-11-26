package bot.charlies.androiddesign.preview.model

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LiveDeviceCodecTest {
  private fun pipe(block: (DataOutputStream) -> Unit): DataInputStream {
    val bytes = ByteArrayOutputStream()
    DataOutputStream(bytes).use { out -> block(out) }
    return DataInputStream(ByteArrayInputStream(bytes.toByteArray()))
  }

  @Test
  fun `an idle worker is never stuck and op inside budget is not stuck`() {
    assertFalse(DevicePingResponse().isWorkerStuck())
    assertFalse(
      DevicePingResponse(inFlightOp = "PUT_ASSETS", inFlightAgeMs = 900, inFlightBudgetMs = 1_000)
        .isWorkerStuck(),
    )
    assertTrue(
      DevicePingResponse(
          inFlightOp = "SET_LIVE",
          inFlightAgeMs = 30_001,
          inFlightBudgetMs = 30_000,
        )
        .isWorkerStuck(),
    )
  }

  @Test
  fun `ping liveness fields survive a JSON round trip and default when absent`() {
    val ping =
      DevicePingResponse(inFlightOp = "SET_LIVE", inFlightAgeMs = 12, inFlightBudgetMs = 34)
    val json = PreviewJson.compact.encodeToString(DevicePingResponse.serializer(), ping)
    assertEquals(ping, PreviewJson.compact.decodeFromString(DevicePingResponse.serializer(), json))
    val legacy =
      PreviewJson.compact.decodeFromString(DevicePingResponse.serializer(), """{"ok":true}""")
    assertFalse(legacy.isWorkerStuck())
    assertEquals("localabstract:${DeviceTransportSpec.SOCKET_NAME}", DeviceTransportSpec.ADB_REMOTE)
  }

  @Test
  fun `HAS_ASSETS and PUT_ASSETS round-trip cleanly`() {
    val hasReq = DeviceHasAssetsRequest(listOf("sha-a", "sha-b"))
    val hasResp = DeviceHasAssetsResponse(listOf("sha-b"))
    val items = listOf("sha-b" to byteArrayOf(10, 20, 30), "sha-empty" to byteArrayOf())
    val ack = DeviceAckResponse(ok = true, stored = 2)

    val inp = pipe { out ->
      LiveDeviceCodec.writeHasAssetsRequest(out, hasReq)
      LiveDeviceCodec.writeHasAssetsResponse(out, hasResp)
      LiveDeviceCodec.writePutAssetsRequest(out, items)
      LiveDeviceCodec.writeAck(out, ack)
    }

    assertEquals(LiveDeviceOp.HAS_ASSETS, LiveDeviceCodec.readOpOrNull(inp))
    assertEquals(hasReq, LiveDeviceCodec.readHasAssetsRequest(inp))
    assertEquals(hasResp, LiveDeviceCodec.readHasAssetsResponse(inp))

    assertEquals(LiveDeviceOp.PUT_ASSETS, LiveDeviceCodec.readOpOrNull(inp))
    val decodedItems = LiveDeviceCodec.readPutAssetsRequest(inp)
    assertEquals(2, decodedItems.size)
    assertEquals("sha-b", decodedItems[0].first)
    assertContentEquals(byteArrayOf(10, 20, 30), decodedItems[0].second)
    assertEquals("sha-empty", decodedItems[1].first)
    assertContentEquals(byteArrayOf(), decodedItems[1].second)
    assertEquals(ack, LiveDeviceCodec.readAck(inp))
  }

  @Test
  fun `SET_LIVE and special-character error ack round-trip cleanly`() {
    val liveReq =
      DeviceLiveStateRequest(dexHash = "h1", entryClass = "C", direction = "Dir", screen = "home")
    val errAck = DeviceAckResponse(ok = false, error = "Line 1\n\"quoted\" \\ backslash")

    val inp = pipe { out ->
      LiveDeviceCodec.writeSetLiveRequest(out, null, liveReq)
      LiveDeviceCodec.writeAck(out, DeviceAckResponse(ok = true))
      LiveDeviceCodec.writeSetLiveRequest(out, byteArrayOf(7), liveReq)
      LiveDeviceCodec.writeAck(out, errAck)
    }

    assertEquals(LiveDeviceOp.SET_LIVE, LiveDeviceCodec.readOpOrNull(inp))
    val live1 = LiveDeviceCodec.readSetLiveRequest(inp)
    assertNull(live1.dexBytes)
    assertEquals(liveReq, live1.request)
    assertEquals(DeviceAckResponse(ok = true), LiveDeviceCodec.readAck(inp))

    assertEquals(LiveDeviceOp.SET_LIVE, LiveDeviceCodec.readOpOrNull(inp))
    val live2 = LiveDeviceCodec.readSetLiveRequest(inp)
    assertContentEquals(byteArrayOf(7), live2.dexBytes)
    assertEquals(errAck, LiveDeviceCodec.readAck(inp))
  }

  @Test
  fun `negative lengths and counts fail fast with EOFException`() {
    val negPut = pipe { out -> out.writeInt(-1) }
    assertFailsWith<EOFException> { LiveDeviceCodec.readPutAssetsRequest(negPut) }

    val zeroPing = pipe { out -> out.writeInt(0) }
    assertFailsWith<EOFException> { LiveDeviceCodec.readPingResponse(zeroPing) }

    val tooBigPing = pipe { out -> out.writeInt(LiveDeviceCodec.PING_MAX_BYTES + 1) }
    assertFailsWith<EOFException> { LiveDeviceCodec.readPingResponse(tooBigPing) }
  }
}
