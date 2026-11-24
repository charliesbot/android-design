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
import kotlin.test.assertNull

class DeviceWireCodecTest {
  private fun pipe(block: (DataOutputStream) -> Unit): DataInputStream {
    val bytes = ByteArrayOutputStream()
    DataOutputStream(bytes).use { out -> block(out) }
    return DataInputStream(ByteArrayInputStream(bytes.toByteArray()))
  }

  @Test
  fun `readOpOrNull reads opcode and returns null on EOF`() {
    val inp = pipe { out -> out.writeByte(DeviceOp.PING) }
    assertEquals(DeviceOp.PING, DeviceWireCodec.readOpOrNull(inp))
    assertNull(DeviceWireCodec.readOpOrNull(inp))
  }

  @Test
  fun `PING request and response round-trip cleanly`() {
    val resp =
      DevicePingResponse(
        ok = true,
        status = "pong",
        pid = 1234,
        reloadCount = 3,
        loadedDexHashes = listOf("abc", "def"),
        memory = DeviceMemoryStats(javaHeapUsedBytes = 100L, totalPssKb = 512),
        inFlightOp = "RENDER_BATCH",
        inFlightAgeMs = 15L,
        inFlightBudgetMs = 15_000L,
      )
    val inp = pipe { out ->
      DeviceWireCodec.writePingRequest(out)
      DeviceWireCodec.writePingResponse(out, resp)
    }
    assertEquals(DeviceOp.PING, DeviceWireCodec.readOpOrNull(inp))
    assertEquals(resp, DeviceWireCodec.readPingResponse(inp))
  }

  @Test
  fun `readPingResponse rejects zero or oversized packets`() {
    val zeroInp = pipe { out -> out.writeInt(0) }
    assertFailsWith<EOFException> { DeviceWireCodec.readPingResponse(zeroInp) }

    val tooBigInp = pipe { out -> out.writeInt(DeviceWireCodec.PING_MAX_BYTES + 1) }
    assertFailsWith<EOFException> { DeviceWireCodec.readPingResponse(tooBigInp) }
  }

  @Test
  fun `PRELOAD_DEX request and response round-trip cleanly`() {
    val dex = byteArrayOf(1, 2, 3, 4, 5)
    val resp =
      DevicePreloadResponse(
        ok = true,
        dexHash = "hash-1",
        boardMeta =
          DeviceBoardMeta(
            surface = "phone",
            boardName = "Sample",
            directions =
              listOf(
                DeviceDirectionMeta(
                  name = "Dir",
                  surface = "phone",
                  startScreen = "home",
                  screens = listOf("home"),
                  overlays = listOf(DeviceOverlayMeta(id = "dialog", screen = "home")),
                ),
              ),
            views = listOf(Views.Light),
          ),
        defaultSeed = "#FFB5523B",
        defaultVariant = "tonalSpot",
      )
    val inp = pipe { out ->
      DeviceWireCodec.writePreloadDexRequest(out, "hash-1", "SampleKt", dex)
      DeviceWireCodec.writePreloadDexResponse(out, resp)
    }
    assertEquals(DeviceOp.PRELOAD_DEX, DeviceWireCodec.readOpOrNull(inp))
    val decodedReq = DeviceWireCodec.readPreloadDexRequest(inp)
    assertEquals("hash-1", decodedReq.dexHash)
    assertEquals("SampleKt", decodedReq.entryClass)
    assertContentEquals(dex, decodedReq.dexBytes)
    assertEquals(resp, DeviceWireCodec.readPreloadDexResponse(inp))
  }

  @Test
  fun `HAS_ASSETS and PUT_ASSETS round-trip cleanly`() {
    val hasReq = DeviceHasAssetsRequest(listOf("sha-a", "sha-b"))
    val hasResp = DeviceHasAssetsResponse(listOf("sha-b"))
    val items = listOf("sha-b" to byteArrayOf(10, 20, 30), "sha-empty" to byteArrayOf())
    val ack = DeviceAckResponse(ok = true, stored = 2)

    val inp = pipe { out ->
      DeviceWireCodec.writeHasAssetsRequest(out, hasReq)
      DeviceWireCodec.writeHasAssetsResponse(out, hasResp)
      DeviceWireCodec.writePutAssetsRequest(out, items)
      DeviceWireCodec.writeAck(out, ack)
    }

    assertEquals(DeviceOp.HAS_ASSETS, DeviceWireCodec.readOpOrNull(inp))
    assertEquals(hasReq, DeviceWireCodec.readHasAssetsRequest(inp))
    assertEquals(hasResp, DeviceWireCodec.readHasAssetsResponse(inp))

    assertEquals(DeviceOp.PUT_ASSETS, DeviceWireCodec.readOpOrNull(inp))
    val decodedItems = DeviceWireCodec.readPutAssetsRequest(inp)
    assertEquals(2, decodedItems.size)
    assertEquals("sha-b", decodedItems[0].first)
    assertContentEquals(byteArrayOf(10, 20, 30), decodedItems[0].second)
    assertEquals("sha-empty", decodedItems[1].first)
    assertContentEquals(byteArrayOf(), decodedItems[1].second)
    assertEquals(ack, DeviceWireCodec.readAck(inp))
  }

  @Test
  fun `RENDER_BATCH request and response preserve null and non-null frames and overlays`() {
    val req =
      DeviceBatchRequest(
        dexHash = "dex-9",
        entryClass = "BoardKt",
        sourceFile = "Board.explore.kt",
        sourceText = "val preview = ...",
        targets =
          listOf(
            DeviceRenderTarget("Dir", "home", "light", frameFormat = FrameFormat.LZ4_RGBA),
            DeviceRenderTarget(
              "Dir",
              "home",
              "dark",
              overlayId = "sheet",
              frameFormat = FrameFormat.WEBP_LOSSLESS,
            ),
          ),
      )
    val tree = LayoutTree("home", Views.Light, emptyList(), emptyList())
    val resp =
      DeviceBatchResponse(
        ok = true,
        dexLoadMs = 12.5,
        renderTotalMs = 45.0,
        reloadCount = 1,
        results =
          listOf(
            DeviceTargetResult(
              direction = "Dir",
              screen = "home",
              viewName = "light",
              tree = tree,
              hasFrame = true,
              frameFormat = FrameFormat.LZ4_RGBA,
            ),
          ),
      )
    val frames =
      listOf(
        DeviceWireFrame(
          direction = "Dir",
          screen = "home",
          viewName = "light",
          overlayId = null,
          isEndFrame = false,
          format = FrameFormat.LZ4_RGBA,
          widthPx = 411,
          heightPx = 923,
          rawLength = 411 * 923 * 4,
          payload = byteArrayOf(1, 2, 3),
        ),
        DeviceWireFrame(
          direction = "Dir",
          screen = "home",
          viewName = "dark",
          overlayId = "sheet",
          isEndFrame = true,
          format = FrameFormat.WEBP_LOSSLESS,
          widthPx = 411,
          heightPx = 923,
          rawLength = 411 * 923 * 4,
          payload = byteArrayOf(4, 5),
        ),
        DeviceWireFrame(
          direction = "Dir",
          screen = "home",
          viewName = "tablet",
          overlayId = null,
          isEndFrame = false,
          format = FrameFormat.NONE,
          widthPx = 1280,
          heightPx = 800,
          rawLength = 0,
          payload = byteArrayOf(),
        ),
      )

    val inp = pipe { out ->
      DeviceWireCodec.writeRenderBatchRequest(out, null, "BoardKt", req)
      DeviceWireCodec.writeRenderBatchRequest(out, byteArrayOf(9, 8), "BoardKt", req)
      DeviceWireCodec.writeRenderBatchResponse(out, resp, frames)
    }

    assertEquals(DeviceOp.RENDER_BATCH, DeviceWireCodec.readOpOrNull(inp))
    val req1 = DeviceWireCodec.readRenderBatchRequest(inp)
    assertNull(req1.dexBytes)
    assertEquals("BoardKt", req1.entryClass)
    assertEquals(req, req1.request)

    assertEquals(DeviceOp.RENDER_BATCH, DeviceWireCodec.readOpOrNull(inp))
    val req2 = DeviceWireCodec.readRenderBatchRequest(inp)
    assertContentEquals(byteArrayOf(9, 8), req2.dexBytes)

    val decoded = DeviceWireCodec.readRenderBatchResponse(inp)
    assertEquals(resp, decoded.response)
    assertEquals(frames, decoded.frames)
  }

  @Test
  fun `SET_LIVE, RECYCLE, and special-character error ack round-trip cleanly`() {
    val liveReq =
      DeviceLiveStateRequest(dexHash = "h1", entryClass = "C", direction = "Dir", screen = "home")
    val errAck = DeviceAckResponse(ok = false, error = "Line 1\n\"quoted\" \\ backslash")

    val inp = pipe { out ->
      DeviceWireCodec.writeSetLiveRequest(out, null, liveReq)
      DeviceWireCodec.writeAck(out, DeviceAckResponse(ok = true))
      DeviceWireCodec.writeSetLiveRequest(out, byteArrayOf(7), liveReq)
      DeviceWireCodec.writeAck(out, errAck)
      DeviceWireCodec.writeRecycleRequest(out)
      DeviceWireCodec.writeAck(out, DeviceAckResponse(ok = true))
    }

    assertEquals(DeviceOp.SET_LIVE, DeviceWireCodec.readOpOrNull(inp))
    val live1 = DeviceWireCodec.readSetLiveRequest(inp)
    assertNull(live1.dexBytes)
    assertEquals(liveReq, live1.request)
    assertEquals(DeviceAckResponse(ok = true), DeviceWireCodec.readAck(inp))

    assertEquals(DeviceOp.SET_LIVE, DeviceWireCodec.readOpOrNull(inp))
    val live2 = DeviceWireCodec.readSetLiveRequest(inp)
    assertContentEquals(byteArrayOf(7), live2.dexBytes)
    assertEquals(errAck, DeviceWireCodec.readAck(inp))

    assertEquals(DeviceOp.RECYCLE, DeviceWireCodec.readOpOrNull(inp))
    assertEquals(DeviceAckResponse(ok = true), DeviceWireCodec.readAck(inp))
  }

  @Test
  fun `negative lengths and counts fail fast with EOFException`() {
    val negPut = pipe { out -> out.writeInt(-1) }
    assertFailsWith<EOFException> { DeviceWireCodec.readPutAssetsRequest(negPut) }

    val negFrameCount = pipe { out ->
      val respJson =
        PreviewJson.compact
          .encodeToString(
            DeviceBatchResponse.serializer(),
            DeviceBatchResponse(ok = true),
          )
          .toByteArray()
      out.writeInt(respJson.size)
      out.write(respJson)
      out.writeInt(-2)
    }
    assertFailsWith<EOFException> { DeviceWireCodec.readRenderBatchResponse(negFrameCount) }

    val negBatchDex = pipe { out -> out.writeInt(-3) }
    assertFailsWith<EOFException> { DeviceWireCodec.readRenderBatchRequest(negBatchDex) }
  }
}
