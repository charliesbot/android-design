package bot.charlies.androiddesign.preview.app.android

import bot.charlies.androiddesign.preview.app.server.AndroidPreviewBackend
import bot.charlies.androiddesign.preview.app.server.CheckTargetSpec
import bot.charlies.androiddesign.preview.app.server.CompileEntry
import bot.charlies.androiddesign.preview.app.server.ContentHasher
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.DeviceBatchRequest
import bot.charlies.androiddesign.preview.model.DeviceBatchResponse
import bot.charlies.androiddesign.preview.model.DeviceBoardMeta
import bot.charlies.androiddesign.preview.model.DeviceDirectionMeta
import bot.charlies.androiddesign.preview.model.DeviceHasAssetsRequest
import bot.charlies.androiddesign.preview.model.DeviceHasAssetsResponse
import bot.charlies.androiddesign.preview.model.DeviceLiveStateRequest
import bot.charlies.androiddesign.preview.model.DeviceOp
import bot.charlies.androiddesign.preview.model.DevicePingResponse
import bot.charlies.androiddesign.preview.model.DevicePreloadResponse
import bot.charlies.androiddesign.preview.model.DeviceTargetResult
import bot.charlies.androiddesign.preview.model.Finding
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PhoneSurface
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.Severity
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
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

    val recycleReasons = mutableListOf<String?>()
    val transport = FakeTransport(failDarkView = true, recommendRecycle = true)
    val backend =
      AndroidPreviewBackend(
        meta = meta,
        transport = transport,
        onRecycleRecommended = { recycleReasons += it },
      )

    val compiledOut = backend.compile(snapshot)
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
      backend.renderBatchCheck(entry, targets, scoped = false, rendersRoot = rendersRoot)
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

    val darkArtifact = artifacts.first { it.view.name == "dark" }
    assertFalse(darkArtifact.pass)
    assertNotNull(darkArtifact.pngBytes, "Follow-up LZ4 fetch must populate failing dark PNG bytes")

    val expectedAssetSha = ContentHasher.sha256Hex(assetBytes)
    assertTrue(expectedAssetSha in transport.syncedAssetHashes)
    assertTrue("memory_pressure" in recycleReasons)

    val cardArtifact =
      backend.renderSingle(
        entry = entry,
        direction = "Main",
        screenId = "home",
        prefix = "Main-home",
        overlayId = null,
        view = darkView,
        densityScale = 2,
        designSystem = DesignSystemResolver.resolve(null, null, PhoneSurface),
        baseCheck = null,
        writeFiles = false,
        rendersRoot = rendersRoot,
        frameFormat = FrameFormat.WEBP_LOSSLESS,
      )
    assertEquals("image/webp", cardArtifact.imageMime)
    assertEquals(FrameFormat.WEBP_LOSSLESS, transport.singleFormats.last())
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
            val op = inp.read()
            if (op < 0) break
            when (op) {
              DeviceOp.PING -> {
                val json =
                  PreviewJson.compact.encodeToString(
                    DevicePingResponse.serializer(),
                    DevicePingResponse(ok = true, pid = 4242),
                  )
                val bytes = json.toByteArray(StandardCharsets.UTF_8)
                out.writeInt(bytes.size)
                out.write(bytes)
                out.flush()
              }
              DeviceOp.HAS_ASSETS -> {
                val len = inp.readInt()
                val buf = ByteArray(len)
                inp.readFully(buf)
                val req =
                  PreviewJson.compact.decodeFromString(
                    DeviceHasAssetsRequest.serializer(),
                    String(buf, StandardCharsets.UTF_8),
                  )
                val respJson =
                  PreviewJson.compact.encodeToString(
                    DeviceHasAssetsResponse.serializer(),
                    DeviceHasAssetsResponse(missing = req.hashes),
                  )
                val respBytes = respJson.toByteArray(StandardCharsets.UTF_8)
                out.writeInt(respBytes.size)
                out.write(respBytes)
                out.flush()
              }
              DeviceOp.PUT_ASSETS -> {
                val count = inp.readInt()
                repeat(count) {
                  val hLen = inp.readInt()
                  val hBytes = ByteArray(hLen)
                  inp.readFully(hBytes)
                  val hash = String(hBytes, StandardCharsets.UTF_8)
                  val bLen = inp.readInt()
                  val blob = ByteArray(bLen)
                  inp.readFully(blob)
                  receivedAssets[hash] = blob
                }
                val okBytes = """{"ok":true}""".toByteArray(StandardCharsets.UTF_8)
                out.writeInt(okBytes.size)
                out.write(okBytes)
                out.flush()
              }
            }
          }
        }
      }
      val connectAttempts = java.util.concurrent.atomic.AtomicInteger(0)
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
}
