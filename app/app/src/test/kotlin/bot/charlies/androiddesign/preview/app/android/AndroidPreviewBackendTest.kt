package bot.charlies.androiddesign.preview.app.android

import bot.charlies.androiddesign.preview.app.server.AndroidRenderWorker
import bot.charlies.androiddesign.preview.app.server.CheckExecutor
import bot.charlies.androiddesign.preview.app.server.CheckTargetSpec
import bot.charlies.androiddesign.preview.app.server.CompileEntry
import bot.charlies.androiddesign.preview.app.server.ContentHasher
import bot.charlies.androiddesign.preview.app.server.SingleRenderRequest
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.DeviceAckResponse
import bot.charlies.androiddesign.preview.model.DeviceBatchRequest
import bot.charlies.androiddesign.preview.model.DeviceBatchResponse
import bot.charlies.androiddesign.preview.model.DeviceBoardMeta
import bot.charlies.androiddesign.preview.model.DeviceDirectionMeta
import bot.charlies.androiddesign.preview.model.DeviceHasAssetsResponse
import bot.charlies.androiddesign.preview.model.DeviceLiveStateRequest
import bot.charlies.androiddesign.preview.model.DeviceOp
import bot.charlies.androiddesign.preview.model.DevicePingResponse
import bot.charlies.androiddesign.preview.model.DevicePreloadResponse
import bot.charlies.androiddesign.preview.model.DeviceRenderTarget
import bot.charlies.androiddesign.preview.model.DeviceTargetResult
import bot.charlies.androiddesign.preview.model.DeviceWireCodec
import bot.charlies.androiddesign.preview.model.DeviceWireFrame
import bot.charlies.androiddesign.preview.model.Finding
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PhoneSurface
import bot.charlies.androiddesign.preview.model.Severity
import bot.charlies.androiddesign.preview.model.Views
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import net.jpountz.lz4.LZ4Factory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class AndroidPreviewBackendTest {

  private fun compressedLz4Frame(width: Int, height: Int): ByteArray {
    val rgba = ByteArray(width * height * 4)
    for (i in rgba.indices step 4) {
      rgba[i] = 0x42
      rgba[i + 1] = 0x87.toByte()
      rgba[i + 2] = 0xF5.toByte()
      rgba[i + 3] = 0xFF.toByte()
    }
    val compressor = LZ4Factory.fastestInstance().fastCompressor()
    val maxCompressed = compressor.maxCompressedLength(rgba.size)
    val out = ByteArray(maxCompressed)
    val len = compressor.compress(rgba, 0, rgba.size, out, 0, maxCompressed)
    return out.copyOf(len)
  }

  private fun pngBytes(width: Int = 4, height: Int = 4): ByteArray {
    val lz4 = compressedLz4Frame(width, height)
    return FrameCodec.decodeLz4ToPng(width, height, width * height * 4, lz4)
  }

  private fun sampleExploreSource(): String =
    """
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.layout.Column
    import androidx.compose.material3.Text
    import bot.charlies.androiddesign.preview.kit.designBoard
    import bot.charlies.androiddesign.preview.kit.designPainter

    val preview = designBoard(name = "Backend Sample") {
      direction("Main") {
        screen("home") {
          Column {
            Text("Hello")
            Image(painter = designPainter("assets/cover.png"), contentDescription = "Cover")
          }
        }
      }
    }
    """
      .trimIndent()

  private class FakeTransport(
    private val failDarkView: Boolean = false,
    private val recommendRecycle: Boolean = false,
  ) : DeviceTransport {
    val preloaded = CopyOnWriteArrayList<String>()
    val syncedAssetHashes = CopyOnWriteArrayList<String>()
    val syncAssetCalls = AtomicInteger(0)
    val batchRequests = CopyOnWriteArrayList<DeviceBatchRequest>()
    val singleFormats = CopyOnWriteArrayList<FrameFormat>()

    override fun ping(): DevicePingResponse = DevicePingResponse(ok = true, pid = 99)

    override fun preloadDex(
      dexHash: String,
      entryClass: String,
      dexBytes: ByteArray,
    ): DevicePreloadResponse {
      preloaded += dexHash
      return DevicePreloadResponse(
        ok = true,
        dexHash = dexHash,
        boardMeta =
          DeviceBoardMeta(
            surface = "phone",
            boardName = "Backend Sample",
            directions =
              listOf(
                DeviceDirectionMeta(
                  name = "Main",
                  surface = "phone",
                  startScreen = "home",
                  screens = listOf("home"),
                ),
              ),
            views = PhoneSurface.views,
          ),
      )
    }

    override fun syncAssets(assetBlobs: Map<String, ByteArray>): Int {
      syncAssetCalls.incrementAndGet()
      syncedAssetHashes.addAll(assetBlobs.keys)
      return assetBlobs.size
    }

    override fun renderBatch(
      dexHash: String,
      entryClass: String,
      dexBytes: ByteArray?,
      request: DeviceBatchRequest,
    ): DeviceBatchExecution {
      batchRequests += request
      request.targets.forEach { singleFormats += it.frameFormat }
      val frames = linkedMapOf<FrameKey, DecodedDeviceFrame>()
      val results =
        request.targets.map { target ->
          val viewSpec = PhoneSurface.views.first { it.name == target.viewName }
          val isDarkFail = failDarkView && target.viewName == "dark"
          val findings =
            if (isDarkFail) {
              listOf(
                Finding(
                  id = "btn-1",
                  rule = FindingRule.TouchTarget,
                  severity = Severity.Error,
                  detail = "Tap target 32x32 dp below 48x48 dp",
                ),
              )
            } else {
              emptyList()
            }
          val tree =
            LayoutTree(
              screen = target.screen,
              view = viewSpec,
              nodes =
                listOf(
                  LayoutNode(
                    id = "btn-1",
                    type = "Button",
                    bounds = Bounds(0f, 0f, 100f, 48f),
                  ),
                ),
              findings = findings,
            )
          val hasFrame = target.frameFormat != FrameFormat.NONE
          if (hasFrame) {
            val fk =
              FrameKey(target.direction, target.screen, target.overlayId, target.viewName, false)
            val bytes =
              if (target.frameFormat == FrameFormat.WEBP_LOSSLESS) {
                byteArrayOf(
                  'R'.code.toByte(),
                  'I'.code.toByte(),
                  'F'.code.toByte(),
                  'F'.code.toByte(),
                  1,
                  2,
                  3,
                  4,
                )
              } else {
                val rgba = ByteArray(4 * 4 * 4)
                val compressor = LZ4Factory.fastestInstance().fastCompressor()
                val dst = ByteArray(compressor.maxCompressedLength(rgba.size))
                val cLen = compressor.compress(rgba, 0, rgba.size, dst, 0, dst.size)
                FrameCodec.decodeLz4ToPng(4, 4, rgba.size, dst.copyOf(cLen))
              }
            val mime =
              if (target.frameFormat == FrameFormat.WEBP_LOSSLESS) "image/webp" else "image/png"
            frames[fk] =
              DecodedDeviceFrame(
                key = fk,
                format = target.frameFormat,
                widthPx = 4,
                heightPx = 4,
                pngBytes = if (mime == "image/png") bytes else null,
                imageBytes = bytes,
                imageMime = mime,
              )
          }
          DeviceTargetResult(
            direction = target.direction,
            screen = target.screen,
            viewName = target.viewName,
            overlayId = target.overlayId,
            densityScale = target.densityScale,
            widthPx = 4,
            heightPx = 4,
            renderMs = 12.0,
            pass = findings.isEmpty(),
            tree = tree,
            endTree = if (target.includeEndFrame) tree else null,
            hasFrame = hasFrame,
            frameFormat = target.frameFormat,
          )
        }
      return DeviceBatchExecution(
        response =
          DeviceBatchResponse(
            ok = true,
            recycleRecommended = recommendRecycle,
            recycleReason = if (recommendRecycle) "memory_pressure" else null,
            results = results,
          ),
        frames = frames,
      )
    }

    override fun setLive(dexBytes: ByteArray?, request: DeviceLiveStateRequest): Boolean = true

    override fun recycle(): Boolean = true

    override fun close() {}
  }

  @Test
  fun `frameCodec decodes LZ4 RGBA to valid PNG header`() {
    val decoded = pngBytes(4, 4)
    assertTrue(decoded.size > 12)
    assertEquals(
      listOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A),
      decoded.take(8),
    )
  }

  @Test
  fun `batch check selects primary frames, syncs assets, fetches failing follow-ups, and preserves webp card mime`(
    @TempDir tmp: Path,
  ) {
    val meta = BootstrapMeta.locate()
    val assetsDir = tmp.resolve("assets")
    Files.createDirectories(assetsDir)
    val assetBytes = byteArrayOf(1, 2, 3, 4, 5)
    Files.write(assetsDir.resolve("cover.png"), assetBytes)
    val sourceFile = tmp.resolve("Demo.explore.kt")
    Files.writeString(sourceFile, sampleExploreSource())
    val snapshot = ContentHasher.snapshot(sourceFile, null)
    val rendersRoot = tmp.resolve("renders")

    val recycleAtBatchCount = mutableListOf<Int>()
    val recycleReasons = mutableListOf<String?>()
    val transport = FakeTransport(failDarkView = true, recommendRecycle = true)
    val worker =
      AndroidRenderWorker(
        meta = meta,
        transport = transport,
        onRecycleRecommended = { reason ->
          recycleReasons += reason
          recycleAtBatchCount += transport.batchRequests.size
        },
      )
    val executor = CheckExecutor(worker)

    val compiledOut = executor.compile(snapshot)
    assertTrue(compiledOut.loaded.errors.isEmpty(), "Compile errors: ${compiledOut.loaded.errors}")
    assertEquals(1, transport.preloaded.size)

    val entry =
      CompileEntry(
        sourcePath = sourceFile,
        designPath = null,
        contentHash = snapshot.contentHash,
        loaded = compiledOut.loaded,
        fromCache = false,
        compileCountAtLoad = 1,
        dexHash = compiledOut.dexHash,
        surface = compiledOut.surface,
        boardName = compiledOut.boardName,
        boardMeta = compiledOut.boardMeta,
        androidData = compiledOut.androidData,
      )

    val lightView = PhoneSurface.views.first { it.name == "light" }
    val darkView = PhoneSurface.views.first { it.name == "dark" }
    val desktopView = PhoneSurface.views.first { it.name == "desktop" }
    val targets =
      listOf(
        CheckTargetSpec("Main", "home", "Main-home", null, lightView),
        CheckTargetSpec("Main", "home", "Main-home", null, darkView),
        CheckTargetSpec("Main", "home", "Main-home", null, desktopView),
      )

    val artifacts =
      executor.renderBatchCheck(entry, targets, scoped = false, rendersRoot = rendersRoot)
    assertEquals(3, artifacts.size)
    // First batch + follow-up batch for failing non-primary `dark` view.
    assertEquals(2, transport.batchRequests.size)
    val firstBatchTargets =
      transport.batchRequests[0].targets.associate { it.viewName to it.frameFormat }
    assertEquals(FrameFormat.LZ4_RGBA, firstBatchTargets["light"])
    assertEquals(FrameFormat.LZ4_RGBA, firstBatchTargets["desktop"])
    assertEquals(FrameFormat.NONE, firstBatchTargets["dark"])

    val followUpTargets = transport.batchRequests[1].targets
    assertEquals(listOf("dark"), followUpTargets.map { it.viewName })
    assertEquals(FrameFormat.LZ4_RGBA, followUpTargets.single().frameFormat)

    val lightArtifact = artifacts.first { it.view.name == "light" }
    assertNotNull(lightArtifact.endTree, "CheckExecutor must preserve endTree for Android")

    val darkArtifact = artifacts.first { it.view.name == "dark" }
    assertFalse(darkArtifact.pass)
    assertNotNull(darkArtifact.pngBytes, "Follow-up LZ4 fetch must populate failing dark PNG bytes")

    val expectedAssetSha = ContentHasher.sha256Hex(assetBytes)
    assertTrue(expectedAssetSha in transport.syncedAssetHashes)
    assertTrue(transport.syncAssetCalls.get() >= 1)
    assertEquals(listOf("memory_pressure"), recycleReasons)
    assertEquals(listOf(2), recycleAtBatchCount)

    val cardArtifact =
      executor.renderSingle(
        entry = entry,
        SingleRenderRequest(
          direction = "Main",
          screenId = "home",
          prefix = "Main-home",
          overlayId = null,
          view = darkView,
          densityScale = 2,
          baseCheck = null,
          writeFiles = false,
          rendersRoot = rendersRoot,
          frameFormat = FrameFormat.WEBP_LOSSLESS,
        ),
      )
    assertEquals("image/webp", cardArtifact.imageMime)
    assertEquals(FrameFormat.WEBP_LOSSLESS, transport.singleFormats.last())
    assertNotNull(cardArtifact.endTree)
    assertEquals(listOf(2, 3), recycleAtBatchCount)
  }

  @Test
  fun `tcp bridge performs HAS_ASSETS and PUT_ASSETS wire roundtrip`() {
    val assetBytes = byteArrayOf(9, 8, 7, 6, 5, 4)
    val assetHash = ContentHasher.sha256Hex(assetBytes)
    val receivedAssets = linkedMapOf<String, ByteArray>()

    ServerSocket(0, 1, InetAddress.getByName("127.0.0.1")).use { server ->
      val executor = Executors.newSingleThreadExecutor()
      val worker = executor.submit {
        server.accept().use { sock ->
          val inp = DataInputStream(BufferedInputStream(sock.getInputStream()))
          val out = DataOutputStream(BufferedOutputStream(sock.getOutputStream()))
          while (true) {
            when (DeviceWireCodec.readOpOrNull(inp) ?: break) {
              DeviceOp.PING ->
                DeviceWireCodec.writePingResponse(out, DevicePingResponse(ok = true, pid = 4242))
              DeviceOp.HAS_ASSETS -> {
                val req = DeviceWireCodec.readHasAssetsRequest(inp)
                DeviceWireCodec.writeHasAssetsResponse(
                  out,
                  DeviceHasAssetsResponse(missing = req.hashes),
                )
              }
              DeviceOp.PUT_ASSETS -> {
                val items = DeviceWireCodec.readPutAssetsRequest(inp)
                for ((hash, blob) in items) {
                  receivedAssets[hash] = blob
                }
                DeviceWireCodec.writeAck(out, DeviceAckResponse(ok = true, stored = items.size))
              }
            }
          }
        }
      }
      val connectAttempts = AtomicInteger(0)
      try {
        AdbTcpDeviceTransport(
            connectAndEnsurePort = {
              connectAttempts.incrementAndGet()
              server.localPort
            },
            restartSandboxActivity = {},
          )
          .use { transport ->
            val ping = transport.ping()
            assertTrue(ping.ok)
            assertEquals(4242, ping.pid)
            val uploaded = transport.syncAssets(mapOf(assetHash to assetBytes))
            assertEquals(1, uploaded)
            val pingAgain = transport.ping()
            assertTrue(pingAgain.ok)
            assertEquals(
              1,
              connectAttempts.get(),
              "Keep-alive socket reuse must not invoke connectAndEnsurePort repeatedly",
            )
          }
        worker.get()
        assertEquals(assetBytes.toList(), receivedAssets[assetHash]?.toList())
      } finally {
        executor.shutdownNow()
      }
    }
  }

  @Test
  fun `tcp bridge performs preloadDex, renderBatch, setLive retry, and recycle wire roundtrip`() {
    val dexBytes = byteArrayOf(0x64, 0x65, 0x78)
    val lz4Payload = compressedLz4Frame(4, 4)
    val webpPayload =
      byteArrayOf('R'.code.toByte(), 'I'.code.toByte(), 'F'.code.toByte(), 'F'.code.toByte())
    val preloadedDex = mutableListOf<String>()
    val batchDexSizes = mutableListOf<Int>()
    val liveDexSizes = mutableListOf<Int>()
    val restarted = AtomicInteger(0)

    ServerSocket(0, 1, InetAddress.getByName("127.0.0.1")).use { server ->
      val executor = Executors.newSingleThreadExecutor()
      val worker = executor.submit {
        server.accept().use { sock ->
          val inp = DataInputStream(BufferedInputStream(sock.getInputStream()))
          val out = DataOutputStream(BufferedOutputStream(sock.getOutputStream()))
          while (true) {
            when (DeviceWireCodec.readOpOrNull(inp) ?: break) {
              DeviceOp.PRELOAD_DEX -> {
                val req = DeviceWireCodec.readPreloadDexRequest(inp)
                preloadedDex += req.dexHash
                DeviceWireCodec.writePreloadDexResponse(
                  out,
                  DevicePreloadResponse(ok = true, dexHash = req.dexHash),
                )
              }
              DeviceOp.RENDER_BATCH -> {
                val req = DeviceWireCodec.readRenderBatchRequest(inp)
                batchDexSizes += req.dexBytes?.size ?: 0
                val tree = LayoutTree("home", Views.Light, emptyList(), emptyList())
                DeviceWireCodec.writeRenderBatchResponse(
                  out,
                  DeviceBatchResponse(
                    ok = true,
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
                        DeviceTargetResult(
                          direction = "Dir",
                          screen = "home",
                          viewName = "dark",
                          tree = tree,
                          hasFrame = true,
                          frameFormat = FrameFormat.WEBP_LOSSLESS,
                        ),
                      ),
                  ),
                  listOf(
                    DeviceWireFrame(
                      direction = "Dir",
                      screen = "home",
                      viewName = "light",
                      overlayId = null,
                      isEndFrame = false,
                      format = FrameFormat.LZ4_RGBA,
                      widthPx = 4,
                      heightPx = 4,
                      rawLength = 4 * 4 * 4,
                      payload = lz4Payload,
                    ),
                    DeviceWireFrame(
                      direction = "Dir",
                      screen = "home",
                      viewName = "dark",
                      overlayId = null,
                      isEndFrame = false,
                      format = FrameFormat.WEBP_LOSSLESS,
                      widthPx = 4,
                      heightPx = 4,
                      rawLength = 4 * 4 * 4,
                      payload = webpPayload,
                    ),
                  ),
                )
              }
              DeviceOp.SET_LIVE -> {
                val req = DeviceWireCodec.readSetLiveRequest(inp)
                liveDexSizes += req.dexBytes?.size ?: 0
                if (req.dexBytes == null) {
                  DeviceWireCodec.writeAck(
                    out,
                    DeviceAckResponse(ok = false, error = "DEX 'h1' is not preloaded"),
                  )
                } else {
                  DeviceWireCodec.writeAck(out, DeviceAckResponse(ok = true))
                }
              }
              DeviceOp.RECYCLE -> {
                DeviceWireCodec.writeAck(out, DeviceAckResponse(ok = true))
                break
              }
            }
          }
        }
      }

      try {
        AdbTcpDeviceTransport(
            connectAndEnsurePort = { server.localPort },
            restartSandboxActivity = { restarted.incrementAndGet() },
          )
          .use { transport ->
            val preload = transport.preloadDex("h1", "SampleKt", dexBytes)
            assertTrue(preload.ok)
            assertEquals(listOf("h1"), preloadedDex)

            val exec =
              transport.renderBatch(
                dexHash = "h1",
                entryClass = "SampleKt",
                dexBytes = dexBytes,
                request =
                  DeviceBatchRequest(
                    dexHash = "h1",
                    entryClass = "SampleKt",
                    sourceFile = "Sample.explore.kt",
                    sourceText = "val preview = ...",
                    targets =
                      listOf(
                        DeviceRenderTarget(
                          "Dir",
                          "home",
                          "light",
                          frameFormat = FrameFormat.LZ4_RGBA,
                        ),
                        DeviceRenderTarget(
                          "Dir",
                          "home",
                          "dark",
                          frameFormat = FrameFormat.WEBP_LOSSLESS,
                        ),
                      ),
                  ),
              )
            assertTrue(exec.response.ok)
            assertEquals(listOf(0), batchDexSizes, "Already preloaded DEX should not resend bytes")
            val lightPng = exec.frames[FrameKey("Dir", "home", null, "light", false)]
            assertNotNull(lightPng?.pngBytes)
            val darkWebp = exec.frames[FrameKey("Dir", "home", null, "dark", false)]
            assertEquals("image/webp", darkWebp?.imageMime)
            assertEquals(webpPayload.toList(), darkWebp?.imageBytes?.toList())

            // setLive omits dexBytes first (since h1 is in preloadedDexHashes), gets `not
            // preloaded`
            // ack, and retries with dexBytes cleanly.
            val liveOk =
              transport.setLive(
                dexBytes = dexBytes,
                request = DeviceLiveStateRequest(dexHash = "h1", entryClass = "SampleKt"),
              )
            assertTrue(liveOk)
            assertEquals(listOf(0, dexBytes.size), liveDexSizes)

            assertTrue(transport.recycle())
            assertEquals(1, restarted.get())
          }
        worker.get()
      } finally {
        executor.shutdownNow()
      }
    }
  }
}
