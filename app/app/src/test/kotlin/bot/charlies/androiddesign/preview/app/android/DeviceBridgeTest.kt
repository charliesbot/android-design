package bot.charlies.androiddesign.preview.app.android

import bot.charlies.androiddesign.preview.model.DeviceBatchRequest
import bot.charlies.androiddesign.preview.model.DeviceBatchResponse
import bot.charlies.androiddesign.preview.model.DeviceOp
import bot.charlies.androiddesign.preview.model.DevicePingResponse
import bot.charlies.androiddesign.preview.model.DeviceWireCodec
import bot.charlies.androiddesign.preview.model.DeviceWireFrame
import bot.charlies.androiddesign.preview.model.FrameFormat
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import javax.imageio.ImageIO
import net.jpountz.lz4.LZ4Factory
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DeviceBridgeTest {

  private fun solidRgbaLz4(
    width: Int,
    height: Int,
    r: Int,
    g: Int,
    b: Int,
    a: Int = 0xFF,
  ): ByteArray {
    val rgba = ByteArray(width * height * 4)
    for (i in rgba.indices step 4) {
      rgba[i] = r.toByte()
      rgba[i + 1] = g.toByte()
      rgba[i + 2] = b.toByte()
      rgba[i + 3] = a.toByte()
    }
    val compressor = LZ4Factory.fastestInstance().fastCompressor()
    val out = ByteArray(compressor.maxCompressedLength(rgba.size))
    val len = compressor.compress(rgba, 0, rgba.size, out, 0, out.size)
    return out.copyOf(len)
  }

  private fun lz4WireFrame(
    viewName: String,
    r: Int,
    g: Int,
    b: Int,
    width: Int = 4,
    height: Int = 4,
    direction: String = "Dir",
    screen: String = "home",
    overlayId: String? = null,
    isEndFrame: Boolean = false,
  ): DeviceWireFrame =
    DeviceWireFrame(
      direction = direction,
      screen = screen,
      viewName = viewName,
      overlayId = overlayId,
      isEndFrame = isEndFrame,
      format = FrameFormat.LZ4_RGBA,
      widthPx = width,
      heightPx = height,
      rawLength = width * height * 4,
      payload = solidRgbaLz4(width, height, r, g, b),
    )

  private fun webpWireFrame(viewName: String, payload: ByteArray): DeviceWireFrame =
    DeviceWireFrame(
      direction = "Dir",
      screen = "home",
      viewName = viewName,
      overlayId = null,
      isEndFrame = false,
      format = FrameFormat.WEBP_LOSSLESS,
      widthPx = 4,
      heightPx = 4,
      rawLength = 4 * 4 * 4,
      payload = payload,
    )

  private fun noneWireFrame(viewName: String): DeviceWireFrame =
    DeviceWireFrame(
      direction = "Dir",
      screen = "home",
      viewName = viewName,
      overlayId = null,
      isEndFrame = false,
      format = FrameFormat.NONE,
      widthPx = 0,
      heightPx = 0,
      rawLength = 0,
      payload = ByteArray(0),
    )

  @Test
  fun `decodeWireFrames handles empty batch without creating executor`() {
    val providerCalls = AtomicInteger(0)
    val decoded =
      FrameCodec.decodeWireFrames(
        wireFrames = emptyList(),
        executorProvider = {
          providerCalls.incrementAndGet()
          error("Executor must not be requested for 0 frames")
        },
        maxParallelism = 4,
      )
    assertTrue(decoded.isEmpty())
    assertEquals(0, providerCalls.get())
  }

  @Test
  fun `decodeWireFrames decodes single LZ4 frame directly on caller thread without executor`() {
    val providerCalls = AtomicInteger(0)
    val callerThread = Thread.currentThread()
    val decodeThread = AtomicReference<Thread>()
    val frame = lz4WireFrame("light", r = 0x11, g = 0x22, b = 0x33)

    val decoded =
      FrameCodec.decodeWireFrames(
        wireFrames = listOf(frame, webpWireFrame("dark", byteArrayOf(1, 2, 3)), noneWireFrame("x")),
        executorProvider = {
          providerCalls.incrementAndGet()
          error("Executor must not be requested when fewer than 2 LZ4 frames exist")
        },
        maxParallelism = 4,
        decoder = { wire ->
          if (wire.format == FrameFormat.LZ4_RGBA) decodeThread.set(Thread.currentThread())
          FrameCodec.decodeWireFrame(wire)
        },
      )

    assertEquals(0, providerCalls.get())
    assertEquals(callerThread, decodeThread.get())
    assertEquals(listOf("light", "dark", "x"), decoded.keys.map { it.viewName })
    val light = decoded[FrameKey("Dir", "home", null, "light", false)]
    assertNotNull(light?.pngBytes)
    assertArrayEquals(
      FrameCodec.decodeLz4ToPng(4, 4, 4 * 4 * 4, frame.payload),
      light!!.pngBytes,
    )
  }

  @Test
  fun `decodeWireFrames decodes multi-frame batch in parallel while preserving exact order and content`() {
    val frames =
      listOf(
        lz4WireFrame("view-0", r = 0x10, g = 0x20, b = 0x30),
        webpWireFrame("view-1", byteArrayOf(9, 8, 7, 6)),
        lz4WireFrame("view-2", r = 0x40, g = 0x50, b = 0x60),
        noneWireFrame("view-3"),
        lz4WireFrame("view-4", r = 0x70, g = 0x80, b = 0x90),
        lz4WireFrame("view-5", r = 0xA0, g = 0xB0, b = 0xC0),
      )
    val executor = FrameCodec.createDecodeExecutor(4)
    val barrier = CyclicBarrier(2)
    val lz4Calls = AtomicInteger(0)
    try {
      val decoded =
        FrameCodec.decodeWireFrames(
          wireFrames = frames,
          executorProvider = { executor },
          maxParallelism = 4,
          decoder = { wire ->
            if (wire.format == FrameFormat.LZ4_RGBA && lz4Calls.getAndIncrement() < 2) {
              // Fails with TimeoutException if LZ4 decoding runs serially.
              barrier.await(5, TimeUnit.SECONDS)
            }
            FrameCodec.decodeWireFrame(wire)
          },
        )

      assertEquals(frames.map { it.viewName }, decoded.keys.map { it.viewName })

      val expectedColors =
        mapOf(
          "view-0" to 0xFF102030.toInt(),
          "view-2" to 0xFF405060.toInt(),
          "view-4" to 0xFF708090.toInt(),
          "view-5" to 0xFFA0B0C0.toInt(),
        )
      for ((viewName, expectedArgb) in expectedColors) {
        val frame = decoded[FrameKey("Dir", "home", null, viewName, false)]
        assertNotNull(frame, "Missing decoded frame for $viewName")
        assertEquals("image/png", frame!!.imageMime)
        assertNotNull(frame.pngBytes)
        val img = ImageIO.read(ByteArrayInputStream(frame.pngBytes))
        assertEquals(4, img.width)
        assertEquals(4, img.height)
        assertEquals(expectedArgb, img.getRGB(0, 0), "Pixel mismatch for $viewName")
      }

      val webp = decoded[FrameKey("Dir", "home", null, "view-1", false)]!!
      assertNull(webp.pngBytes)
      assertEquals("image/webp", webp.imageMime)
      assertArrayEquals(byteArrayOf(9, 8, 7, 6), webp.imageBytes)

      val none = decoded[FrameKey("Dir", "home", null, "view-3", false)]!!
      assertNull(none.pngBytes)
      assertEquals("application/octet-stream", none.imageMime)
      assertEquals(0, none.imageBytes.size)
    } finally {
      executor.shutdown()
    }
  }

  @Test
  fun `decodeWireFrames unwraps and propagates exceptions from parallel worker`() {
    val validFrame = lz4WireFrame("ok", r = 0x10, g = 0x20, b = 0x30)
    val invalidFrame =
      DeviceWireFrame(
        direction = "Dir",
        screen = "home",
        viewName = "bad",
        overlayId = null,
        isEndFrame = false,
        format = FrameFormat.LZ4_RGBA,
        widthPx = 0,
        heightPx = 4,
        rawLength = 64,
        payload = validFrame.payload,
      )
    val executor = FrameCodec.createDecodeExecutor(2)
    try {
      val err =
        assertThrows(IllegalArgumentException::class.java) {
          FrameCodec.decodeWireFrames(
            wireFrames = listOf(validFrame, invalidFrame),
            executorProvider = { executor },
            maxParallelism = 2,
          )
        }
      assertTrue(err.message.orEmpty().contains("Frame dimensions must be positive"))
    } finally {
      executor.shutdown()
    }
  }

  @Test
  fun `renderBatch releases socket lock before decoding and shuts down decode executor on close`() {
    val batchFrames =
      listOf(
        lz4WireFrame("light", r = 0xAA, g = 0xBB, b = 0xCC),
        lz4WireFrame("dark", r = 0x11, g = 0x22, b = 0x33),
        lz4WireFrame("desktop", r = 0x44, g = 0x55, b = 0x66),
      )
    val decodeStarted = CountDownLatch(1)
    val allowDecodeFinish = CountDownLatch(1)
    val createdExecutors = mutableListOf<ExecutorService>()

    ServerSocket(0, 1, InetAddress.getByName("127.0.0.1")).use { server ->
      val serverPool = Executors.newSingleThreadExecutor()
      val serverTask = serverPool.submit {
        server.accept().use { sock ->
          val inp = DataInputStream(BufferedInputStream(sock.getInputStream()))
          val out = DataOutputStream(BufferedOutputStream(sock.getOutputStream()))
          while (true) {
            when (DeviceWireCodec.readOpOrNull(inp) ?: break) {
              DeviceOp.RENDER_BATCH -> {
                DeviceWireCodec.readRenderBatchRequest(inp)
                DeviceWireCodec.writeRenderBatchResponse(
                  out,
                  DeviceBatchResponse(ok = true),
                  batchFrames,
                )
              }
              DeviceOp.PING -> {
                DeviceWireCodec.writePingResponse(out, DevicePingResponse(ok = true, pid = 777))
              }
            }
          }
        }
      }

      val callerPool = Executors.newSingleThreadExecutor()
      try {
        val transport =
          AdbTcpDeviceTransport(
            host = "127.0.0.1",
            connectAndEnsurePort = { server.localPort },
            restartSandboxActivity = {},
            decodeParallelism = 3,
            decodeExecutorFactory = { threads ->
              FrameCodec.createDecodeExecutor(threads).also { createdExecutors += it }
            },
            wireFrameDecoder = { frame ->
              decodeStarted.countDown()
              assertTrue(
                allowDecodeFinish.await(5, TimeUnit.SECONDS),
                "Timed out waiting for concurrent ping while decoding",
              )
              FrameCodec.decodeWireFrame(frame)
            },
          )

        val execFuture =
          callerPool.submit<DeviceBatchExecution> {
            transport.renderBatch(
              dexHash = "h1",
              entryClass = "SampleKt",
              dexBytes = byteArrayOf(1, 2, 3),
              request =
                DeviceBatchRequest(
                  dexHash = "h1",
                  entryClass = "SampleKt",
                  sourceFile = "Sample.explore.kt",
                  sourceText = "",
                  targets = emptyList(),
                ),
            )
          }

        assertTrue(decodeStarted.await(5, TimeUnit.SECONDS), "Decode should start after socket I/O")

        // If renderBatch still held `lock` during frame decoding, this ping would deadlock until
        // `allowDecodeFinish` timed out.
        val ping = transport.ping()
        assertTrue(ping.ok)
        assertEquals(777, ping.pid)

        allowDecodeFinish.countDown()
        val execution = execFuture.get(5, TimeUnit.SECONDS)
        assertTrue(execution.response.ok)
        assertEquals(listOf("light", "dark", "desktop"), execution.frames.keys.map { it.viewName })
        assertEquals(1, createdExecutors.size)
        assertFalse(createdExecutors.single().isShutdown)

        transport.close()
        assertTrue(createdExecutors.single().isShutdown)
        serverTask.get(5, TimeUnit.SECONDS)
      } finally {
        allowDecodeFinish.countDown()
        callerPool.shutdownNow()
        serverPool.shutdownNow()
      }
    }
  }
}
