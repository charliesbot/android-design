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

class WorkerPipeCodecTest {
  private fun pipe(block: (DataOutputStream) -> Unit): DataInputStream {
    val bytes = ByteArrayOutputStream()
    DataOutputStream(bytes).use { out -> block(out) }
    return DataInputStream(ByteArrayInputStream(bytes.toByteArray()))
  }

  @Test
  fun `readOpOrNull reads opcode and returns null on EOF`() {
    val inp = pipe { out -> out.writeByte(WorkerPipeOp.PING) }
    assertEquals(WorkerPipeOp.PING, WorkerPipeCodec.readOpOrNull(inp))
    assertNull(WorkerPipeCodec.readOpOrNull(inp))
  }

  @Test
  fun `PING request and response round-trip cleanly`() {
    val resp =
      WorkerPingResponse(
        ok = true,
        pid = 1234,
        reloadCount = 3,
        loadedClassHashes = listOf("abc", "def"),
        memory = WorkerMemoryStats(javaHeapUsedBytes = 100L, javaHeapMaxBytes = 512L),
      )
    val inp = pipe { out ->
      WorkerPipeCodec.writePingRequest(out)
      WorkerPipeCodec.writePingResponse(out, resp)
    }
    assertEquals(WorkerPipeOp.PING, WorkerPipeCodec.readOpOrNull(inp))
    assertEquals(resp, WorkerPipeCodec.readPingResponse(inp))
  }

  @Test
  fun `readPingResponse rejects zero or oversized packets`() {
    val zeroInp = pipe { out -> out.writeInt(0) }
    assertFailsWith<EOFException> { WorkerPipeCodec.readPingResponse(zeroInp) }

    val tooBigInp = pipe { out -> out.writeInt(WorkerPipeCodec.PING_MAX_BYTES + 1) }
    assertFailsWith<EOFException> { WorkerPipeCodec.readPingResponse(tooBigInp) }
  }

  @Test
  fun `PRELOAD_CLASSES request and response round-trip cleanly`() {
    val bundle = byteArrayOf(1, 2, 3, 4, 5)
    val resp =
      WorkerPreloadResponse(
        ok = true,
        classHash = "hash-1",
        boardMeta =
          BoardMeta(
            surface = "phone",
            boardName = "Sample",
            directions =
              listOf(
                DirectionMeta(
                  name = "Dir",
                  surface = "phone",
                  startScreen = "home",
                  screens = listOf("home"),
                  overlays = listOf(OverlayMeta(id = "dialog", screen = "home")),
                ),
              ),
            views = listOf(Views.Light),
          ),
      )
    val inp = pipe { out ->
      WorkerPipeCodec.writePreloadClassesRequest(out, "hash-1", "SampleKt", bundle)
      WorkerPipeCodec.writePreloadClassesResponse(out, resp)
    }
    assertEquals(WorkerPipeOp.PRELOAD_CLASSES, WorkerPipeCodec.readOpOrNull(inp))
    val decodedReq = WorkerPipeCodec.readPreloadClassesRequest(inp)
    assertEquals("hash-1", decodedReq.classHash)
    assertEquals("SampleKt", decodedReq.entryClass)
    assertContentEquals(bundle, decodedReq.classBundle)
    assertEquals(resp, WorkerPipeCodec.readPreloadClassesResponse(inp))
  }

  @Test
  fun `RENDER_BATCH request and response preserve frames and overlays`() {
    val req =
      WorkerBatchRequest(
        classHash = "cls-9",
        entryClass = "BoardKt",
        sourceFile = "Board.explore.kt",
        sourceText = "val preview = ...",
        targets =
          listOf(
            WorkerRenderTarget("Dir", "home", "light", includeFrame = true),
            WorkerRenderTarget(
              "Dir",
              "home",
              "dark",
              overlayId = "sheet",
              includeFrame = false,
            ),
          ),
      )
    val tree = LayoutTree("home", Views.Light, emptyList(), emptyList())
    val resp =
      WorkerBatchResponse(
        ok = true,
        renderTotalMs = 45.0,
        reloadCount = 1,
        results =
          listOf(
            WorkerTargetResult(
              direction = "Dir",
              screen = "home",
              viewName = "light",
              tree = tree,
            ),
          ),
      )
    val frames =
      listOf(
        WorkerWireFrame(
          direction = "Dir",
          screen = "home",
          viewName = "light",
          overlayId = null,
          isEndFrame = false,
          widthPx = 411,
          heightPx = 923,
          payload = byteArrayOf(1, 2, 3),
        ),
        WorkerWireFrame(
          direction = "Dir",
          screen = "home",
          viewName = "light",
          overlayId = null,
          isEndFrame = true,
          widthPx = 411,
          heightPx = 923,
          payload = byteArrayOf(4, 5, 6),
        ),
      )

    val inp = pipe { out ->
      WorkerPipeCodec.writeRenderBatchRequest(out, null, "BoardKt", req)
      WorkerPipeCodec.writeRenderBatchRequest(out, byteArrayOf(9, 8), "BoardKt", req)
      WorkerPipeCodec.writeRenderBatchResponse(out, resp, frames)
    }

    assertEquals(WorkerPipeOp.RENDER_BATCH, WorkerPipeCodec.readOpOrNull(inp))
    val req1 = WorkerPipeCodec.readRenderBatchRequest(inp)
    assertNull(req1.classBundle)
    assertEquals("BoardKt", req1.entryClass)
    assertEquals(req, req1.request)

    assertEquals(WorkerPipeOp.RENDER_BATCH, WorkerPipeCodec.readOpOrNull(inp))
    val req2 = WorkerPipeCodec.readRenderBatchRequest(inp)
    assertContentEquals(byteArrayOf(9, 8), req2.classBundle)

    val decoded = WorkerPipeCodec.readRenderBatchResponse(inp)
    assertEquals(resp, decoded.response)
    assertEquals(frames, decoded.frames)
  }

  @Test
  fun `negative lengths and counts fail fast with EOFException`() {
    val negFrameCount = pipe { out ->
      val respJson =
        PreviewJson.compact
          .encodeToString(
            WorkerBatchResponse.serializer(),
            WorkerBatchResponse(ok = true),
          )
          .toByteArray()
      out.writeInt(respJson.size)
      out.write(respJson)
      out.writeInt(-2)
    }
    assertFailsWith<EOFException> { WorkerPipeCodec.readRenderBatchResponse(negFrameCount) }

    val negBatchBundle = pipe { out -> out.writeInt(-3) }
    assertFailsWith<EOFException> { WorkerPipeCodec.readRenderBatchRequest(negBatchBundle) }
  }
}
