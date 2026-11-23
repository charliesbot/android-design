package bot.charlies.androiddesign.preview.host

import bot.charlies.androiddesign.preview.host.compiler.BootstrapMeta
import bot.charlies.androiddesign.preview.host.compiler.HostClassCompiler
import bot.charlies.androiddesign.preview.host.compiler.HostCompileOutcome
import bot.charlies.androiddesign.preview.host.robolectric.RobolectricBoardRenderer
import bot.charlies.androiddesign.preview.host.robolectric.SandboxExcludingRobolectricTestRunner
import bot.charlies.androiddesign.preview.host.sandbox.RenderForegroundService
import bot.charlies.androiddesign.preview.model.DeviceBatchRequest
import bot.charlies.androiddesign.preview.model.DeviceBatchResponse
import bot.charlies.androiddesign.preview.model.DeviceHasAssetsRequest
import bot.charlies.androiddesign.preview.model.DeviceHasAssetsResponse
import bot.charlies.androiddesign.preview.model.DeviceLiveStateRequest
import bot.charlies.androiddesign.preview.model.DeviceOp
import bot.charlies.androiddesign.preview.model.DevicePingResponse
import bot.charlies.androiddesign.preview.model.DeviceRenderTarget
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.RobolectricRenderRequest
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(SandboxExcludingRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class HostEmulatorIntegrationTest {
  @get:Rule val tempFolder = TemporaryFolder()

  private val pkg = "bot.charlies.androiddesign.preview.host"

  @Test
  fun fullEmulatorEndToEndSecurityWatchdogAndRobolectricParity() {
    assumeTrue(
      "Skipping emulator test unless -Dadp.runEmulatorTests=true",
      System.getProperty("adp.runEmulatorTests") == "true",
    )

    val meta = BootstrapMeta.locate()
    runAdb("install", "-r", meta.apkPath.toString())
    runAdb("shell", "cmd", "package", "compile", "-m", "speed", "-f", pkg)
    runAdb("shell", "dumpsys", "deviceidle", "whitelist", "+$pkg")
    runAdb("forward", "tcp:${RenderForegroundService.PORT}", "tcp:${RenderForegroundService.PORT}")
    startServiceAndAwaitPing()

    val workDir = tempFolder.newFolder("emu-fixtures").toPath()
    val (coverBytes, coverSha) = HostTestFixtures.writeSampleCoverPng(workDir)
    val phoneFile = workDir.resolve(HostTestFixtures.PHONE_EXPLORE_FILE)
    val wearFile = workDir.resolve(HostTestFixtures.WEAR_EXPLORE_FILE)
    Files.writeString(phoneFile, HostTestFixtures.PHONE_EXPLORE_SOURCE)
    Files.writeString(wearFile, HostTestFixtures.WEAR_EXPLORE_SOURCE)

    val phoneClasses =
      HostClassCompiler.compileToClasses(
        HostTestFixtures.PHONE_EXPLORE_FILE,
        HostTestFixtures.PHONE_EXPLORE_SOURCE,
      ) as HostCompileOutcome.Classes
    val phoneDex = HostClassCompiler.compileToDex(phoneClasses, meta)
    val phoneHash = sha256Hex(phoneDex).take(16)

    val wearClasses =
      HostClassCompiler.compileToClasses(
        HostTestFixtures.WEAR_EXPLORE_FILE,
        HostTestFixtures.WEAR_EXPLORE_SOURCE,
      ) as HostCompileOutcome.Classes
    val wearDex = HostClassCompiler.compileToDex(wearClasses, meta)
    val wearHash = sha256Hex(wearDex).take(16)

    // 1. Asset protocol (HAS_ASSETS + PUT_ASSETS) & PRELOAD_DEX + PHONE batch (1x, 2x, overlays,
    // LZ4, WEBP, NONE)
    Socket("127.0.0.1", RenderForegroundService.PORT).use { sock ->
      sock.tcpNoDelay = true
      sock.soTimeout = 30_000
      val output = DataOutputStream(BufferedOutputStream(sock.getOutputStream(), 65536))
      val input = DataInputStream(BufferedInputStream(sock.getInputStream(), 65536))

      val ping = sendPing(output, input)
      assertTrue(ping.ok && ping.pid > 0)

      val preload = sendPreloadDex(output, input, phoneHash, phoneClasses.entryClass, phoneDex)
      assertTrue("Expected PRELOAD_DEX ok: $preload", preload.contains("\"ok\":true"))

      val hasResp1 = sendHasAssets(output, input, listOf(coverSha))
      assertEquals(listOf(coverSha), hasResp1.missing)

      sendPutAssets(output, input, mapOf(coverSha to coverBytes))
      val hasResp2 = sendHasAssets(output, input, listOf(coverSha))
      assertTrue("Asset should now be cached", hasResp2.missing.isEmpty())

      val (phoneResp, phoneFrames) =
        sendBatch(
          output = output,
          input = input,
          dexBytes = null, // verify preloaded DEX reuse
          entryClass = phoneClasses.entryClass,
          request =
            DeviceBatchRequest(
              dexHash = phoneHash,
              entryClass = phoneClasses.entryClass,
              sourceFile = HostTestFixtures.PHONE_EXPLORE_FILE,
              sourceText = HostTestFixtures.PHONE_EXPLORE_SOURCE,
              assetMap = mapOf("assets/cover.png" to coverSha),
              targets =
                listOf(
                  DeviceRenderTarget(
                    direction = "Editorial",
                    screen = "home",
                    viewName = "light",
                    densityScale = 1f,
                    frameFormat = FrameFormat.LZ4_RGBA,
                  ),
                  DeviceRenderTarget(
                    direction = "Editorial",
                    screen = "home",
                    viewName = "light",
                    densityScale = 2f,
                    frameFormat = FrameFormat.WEBP_LOSSLESS,
                  ),
                  DeviceRenderTarget(
                    direction = "Editorial",
                    screen = "home",
                    viewName = "light",
                    overlayId = "dialog",
                    densityScale = 1f,
                    frameFormat = FrameFormat.NONE,
                  ),
                  DeviceRenderTarget(
                    direction = "Editorial",
                    screen = "home",
                    viewName = "light",
                    overlayId = "sheet",
                    densityScale = 1f,
                    frameFormat = FrameFormat.NONE,
                  ),
                  DeviceRenderTarget(
                    direction = "Editorial",
                    screen = "home",
                    viewName = "light",
                    overlayId = "popup",
                    densityScale = 1f,
                    frameFormat = FrameFormat.NONE,
                  ),
                ),
            ),
        )
      assertTrue("Phone batch failed: ${phoneResp.error}", phoneResp.ok)
      assertEquals(5, phoneResp.results.size)
      assertEquals(2, phoneFrames.size)
      assertEquals(FrameFormat.LZ4_RGBA, phoneFrames[0].format)
      assertEquals(FrameFormat.WEBP_LOSSLESS, phoneFrames[1].format)
      assertEquals(
        "RIFF",
        String(phoneFrames[1].payload.copyOfRange(0, 4), StandardCharsets.US_ASCII),
      )

      val emuPhone1x = phoneResp.results[0].tree
      val emuPhone2x = phoneResp.results[1].tree
      assertTrue(
        "Expected sourceFile + line numbers in emulator nodes",
        emuPhone2x.nodes.any {
          it.sourceFile == HostTestFixtures.PHONE_EXPLORE_FILE && (it.sourceLine ?: 0) > 0
        },
      )
      for (ovResult in phoneResp.results.subList(2, 5)) {
        assertTrue(
          "Expected overlay nodes for ${ovResult.overlayId}",
          ovResult.tree.nodes.isNotEmpty(),
        )
      }

      // 2. Wear fixture (scroll, DatePicker skip < 2000ms, AlertDialog, curveToEdge)
      val (wearResp, wearFrames) =
        sendBatch(
          output = output,
          input = input,
          dexBytes = wearDex,
          entryClass = wearClasses.entryClass,
          request =
            DeviceBatchRequest(
              dexHash = wearHash,
              entryClass = wearClasses.entryClass,
              sourceFile = HostTestFixtures.WEAR_EXPLORE_FILE,
              sourceText = HostTestFixtures.WEAR_EXPLORE_SOURCE,
              targets =
                listOf(
                  DeviceRenderTarget(
                    direction = "Wear Fitness",
                    screen = "main",
                    viewName = "watch",
                    densityScale = 2f,
                    frameFormat = FrameFormat.LZ4_RGBA,
                    includeEndFrame = true,
                  ),
                  DeviceRenderTarget(
                    direction = "Wear Fitness",
                    screen = "date",
                    viewName = "watch",
                    densityScale = 2f,
                    frameFormat = FrameFormat.NONE,
                  ),
                  DeviceRenderTarget(
                    direction = "Wear Fitness",
                    screen = "curved",
                    viewName = "watch",
                    densityScale = 2f,
                    frameFormat = FrameFormat.NONE,
                  ),
                  DeviceRenderTarget(
                    direction = "Wear Fitness",
                    screen = "main",
                    viewName = "watch",
                    overlayId = "confirm",
                    densityScale = 2f,
                    frameFormat = FrameFormat.NONE,
                  ),
                ),
            ),
        )
      assertTrue("Wear batch failed: ${wearResp.error}", wearResp.ok)
      val emuWearMain = wearResp.results[0]
      assertTrue("Expected Wear auto-scroll endTree", emuWearMain.endTree != null)
      assertTrue(
        "Expected Wear start + end LZ4 frames",
        wearFrames.size == 2 && wearFrames[1].isEndFrame,
      )
      val wearDateResult = wearResp.results[1]
      assertTrue(
        "Wear DatePicker on emulator must complete < 2000ms, took ${wearDateResult.renderMs}ms",
        wearDateResult.renderMs < 2000.0,
      )
      assertTrue("Wear DatePicker must not scroll endTree", wearDateResult.endTree == null)
      assertTrue("Wear curved node present", wearResp.results[2].tree.nodes.isNotEmpty())
      assertTrue("Wear confirm dialog present", wearResp.results[3].tree.nodes.isNotEmpty())

      // 3. Parity check: Emulator (Display 0 insets consumed) vs Robolectric (same fixtures)
      val roboPhone1x =
        RobolectricBoardRenderer.handleRequest(
            RobolectricRenderRequest(
              id = "parity-p1",
              sourcePath = phoneFile.toString(),
              views = listOf("light"),
              screens = listOf("home"),
              density = 1f,
              encodePng = false,
            ),
          )
          .trees
          .single()
      val roboPhone2x =
        RobolectricBoardRenderer.handleRequest(
            RobolectricRenderRequest(
              id = "parity-p2",
              sourcePath = phoneFile.toString(),
              views = listOf("light"),
              screens = listOf("home"),
              density = 2f,
              encodePng = false,
            ),
          )
          .trees
          .single()
      val roboWearMain =
        RobolectricBoardRenderer.handleRequest(
            RobolectricRenderRequest(
              id = "parity-w1",
              sourcePath = wearFile.toString(),
              views = listOf("watch"),
              screens = listOf("main"),
              density = 2f,
              encodePng = false,
            ),
          )
          .trees
          .single()

      assertTreeParity("phone-1x", roboPhone1x, emuPhone1x)
      assertTreeParity("phone-2x", roboPhone2x, emuPhone2x)
      assertTreeParity("wear-main", roboWearMain, emuWearMain.tree)

      // 4. OP_SET_LIVE updates LivePreviewActivity cleanly
      val liveJson =
        sendSetLive(
          output,
          input,
          null,
          DeviceLiveStateRequest(
            dexHash = phoneHash,
            entryClass = phoneClasses.entryClass,
            sourceFile = HostTestFixtures.PHONE_EXPLORE_FILE,
            assetMap = mapOf("assets/cover.png" to coverSha),
            direction = "Editorial",
            screen = "home",
          ),
        )
      assertTrue("Expected SET_LIVE ok: $liveJson", liveJson.contains("\"ok\":true"))
      runAdb("shell", "am", "start", "-W", "-n", "$pkg/.LivePreviewActivity")
      val topActivity = runAdb("shell", "dumpsys", "activity", "activities")
      assertTrue("LivePreviewActivity should launch", topActivity.contains("LivePreviewActivity"))
      val (liveOpenResp, _) =
        sendBatch(
          output = output,
          input = input,
          dexBytes = null,
          entryClass = phoneClasses.entryClass,
          request =
            DeviceBatchRequest(
              dexHash = phoneHash,
              entryClass = phoneClasses.entryClass,
              sourceFile = HostTestFixtures.PHONE_EXPLORE_FILE,
              sourceText = HostTestFixtures.PHONE_EXPLORE_SOURCE,
              assetMap = mapOf("assets/cover.png" to coverSha),
              targets =
                listOf(
                  DeviceRenderTarget(
                    direction = "Editorial",
                    screen = "home",
                    viewName = "light",
                    densityScale = 1f,
                    frameFormat = FrameFormat.NONE,
                  ),
                ),
            ),
        )
      assertTrue(
        "Offscreen batch while LivePreviewActivity foreground failed: ${liveOpenResp.error}",
        liveOpenResp.ok,
      )
      assertTreeParity("phone-1x-while-live-open", roboPhone1x, liveOpenResp.results[0].tree)

      // 5. Malicious DEX bypassing BytecodeGuard is rejected on-device by PolicyDexClassLoader
      val maliciousSource =
        """
        import bot.charlies.androiddesign.preview.kit.designBoard
        val preview = designBoard("Evil") {
          screen("main") {
            val f = java.io.File("/etc/hosts")
            androidx.compose.material3.Text(f.path)
          }
        }
        """
          .trimIndent()
      val maliciousClasses =
        HostClassCompiler.compileToClasses(
          "Malicious.explore.kt",
          maliciousSource,
          skipGuards = true,
        ) as HostCompileOutcome.Classes
      val maliciousDex = HostClassCompiler.compileToDex(maliciousClasses, meta)
      val (malResp, _) =
        sendBatch(
          output = output,
          input = input,
          dexBytes = maliciousDex,
          entryClass = maliciousClasses.entryClass,
          request =
            DeviceBatchRequest(
              dexHash = sha256Hex(maliciousDex).take(16),
              entryClass = maliciousClasses.entryClass,
              sourceFile = "Malicious.explore.kt",
              sourceText = maliciousSource,
              targets = listOf(DeviceRenderTarget("Evil", "main", "light", densityScale = 1f)),
            ),
        )
      assertFalse("Malicious DEX must be rejected on-device", malResp.ok)
      assertTrue(
        "Expected SecurityException in error: ${malResp.error}",
        malResp.error.orEmpty().contains("SecurityException"),
      )

      // 6. Simulated hang triggers watchdog timeout + :sandbox process kill + clean restart
      val oldPid = ping.pid
      val hangSource =
        """
        import androidx.compose.material3.Text
        import bot.charlies.androiddesign.preview.kit.designBoard
        val preview = designBoard("Hang") {
          screen("main") {
            val tick = java.util.concurrent.atomic.AtomicLong(0L)
            while (tick.incrementAndGet() > 0L) {
              // Intentional spin to exercise watchdog_timeout on :sandbox
            }
            Text("count ${'$'}{tick.get()}")
          }
        }
        """
          .trimIndent()
      val hangClasses =
        HostClassCompiler.compileToClasses("Hang.explore.kt", hangSource, skipGuards = true)
          as HostCompileOutcome.Classes
      val hangDex = HostClassCompiler.compileToDex(hangClasses, meta)
      val (hangResp, _) =
        sendBatch(
          output = output,
          input = input,
          dexBytes = hangDex,
          entryClass = hangClasses.entryClass,
          request =
            DeviceBatchRequest(
              dexHash = sha256Hex(hangDex).take(16),
              entryClass = hangClasses.entryClass,
              sourceFile = "Hang.explore.kt",
              sourceText = hangSource,
              targets = listOf(DeviceRenderTarget("Hang", "main", "light", densityScale = 1f)),
              timeoutMs = 1500L,
            ),
        )
      assertFalse("Expected hang response ok=false", hangResp.ok)
      assertTrue("Expected watchdog_timeout reason", hangResp.recycleReason == "watchdog_timeout")

      Thread.sleep(250)
      val newPing = startServiceAndAwaitPing(excludePid = oldPid)
      assertNotEquals("Expected new :sandbox PID after watchdog kill", oldPid, newPing.pid)
    }
  }

  private fun assertTreeParity(label: String, robo: LayoutTree, emu: LayoutTree) {
    val rByKey =
      robo.nodes.associateBy { "${it.type}:${it.text?.value ?: it.id.substringBefore('@')}" }
    val eByKey =
      emu.nodes.associateBy { "${it.type}:${it.text?.value ?: it.id.substringBefore('@')}" }
    val common = rByKey.keys.intersect(eByKey.keys)
    assertTrue("$label: expected matched nodes between Robolectric and Emulator", common.size >= 4)
    val deltas = common.map { k ->
      val r = rByKey[k]!!.bounds
      val e = eByKey[k]!!.bounds
      Triple(k, abs(r.width - e.width), abs(r.height - e.height))
    }
    val maxWDelta = deltas.maxOf { it.second }
    val maxHDelta = deltas.maxOf { it.third }
    val worstH = deltas.maxBy { it.third }
    val colorMismatches = common.mapNotNull { k ->
      val rNode = rByKey[k]
      val eNode = eByKey[k]
      val rc = rNode?.colors
      val ec = eNode?.colors
      if (rc != null && ec != null && (rc.content != ec.content || rc.container != ec.container)) {
        "$k(robo=$rc@${rNode.bounds}/line=${rNode.sourceLine} vs emu=$ec@${eNode.bounds}/line=${eNode.sourceLine})"
      } else {
        null
      }
    }
    assertTrue("$label: maxWDelta=$maxWDelta dp (>1dp)", maxWDelta <= 1.0f)
    assertTrue("$label: maxHDelta=$maxHDelta dp (>1dp, worst=$worstH)", maxHDelta <= 2.5f)
    assertTrue("$label: color mismatches: $colorMismatches", colorMismatches.isEmpty())
  }

  private fun startServiceAndAwaitPing(excludePid: Int? = null): DevicePingResponse {
    runAdb("shell", "am", "start-foreground-service", "-n", "$pkg/.sandbox.RenderForegroundService")
    runAdb(
      "shell",
      "am",
      "start",
      "-W",
      "-n",
      "$pkg/.sandbox.HostRenderActivity",
      "--ez",
      "move_to_back",
      "true",
    )
    val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15)
    while (System.nanoTime() < deadline) {
      val ping = runCatching {
        Socket("127.0.0.1", RenderForegroundService.PORT).use { s ->
          s.soTimeout = 3000
          val out = DataOutputStream(BufferedOutputStream(s.getOutputStream()))
          val inp = DataInputStream(BufferedInputStream(s.getInputStream()))
          sendPing(out, inp)
        }
      }
        .getOrNull()
      if (ping != null && ping.ok && (excludePid == null || ping.pid != excludePid)) return ping
      Thread.sleep(150)
    }
    error("Timed out waiting for RenderForegroundService PING")
  }

  private fun sendPing(output: DataOutputStream, input: DataInputStream): DevicePingResponse {
    output.writeByte(DeviceOp.PING)
    output.flush()
    val json = readUtf(input)
    return PreviewJson.compact.decodeFromString(DevicePingResponse.serializer(), json)
  }

  private fun sendPreloadDex(
    output: DataOutputStream,
    input: DataInputStream,
    dexHash: String,
    entryClass: String,
    dexBytes: ByteArray,
  ): String {
    output.writeByte(DeviceOp.PRELOAD_DEX)
    writeUtf(output, dexHash)
    writeUtf(output, entryClass)
    output.writeInt(dexBytes.size)
    output.write(dexBytes)
    output.flush()
    return readUtf(input)
  }

  private fun sendHasAssets(
    output: DataOutputStream,
    input: DataInputStream,
    hashes: List<String>,
  ): DeviceHasAssetsResponse {
    output.writeByte(DeviceOp.HAS_ASSETS)
    writeUtf(
      output,
      PreviewJson.compact.encodeToString(
        DeviceHasAssetsRequest.serializer(),
        DeviceHasAssetsRequest(hashes),
      ),
    )
    output.flush()
    return PreviewJson.compact.decodeFromString(
      DeviceHasAssetsResponse.serializer(),
      readUtf(input),
    )
  }

  private fun sendPutAssets(
    output: DataOutputStream,
    input: DataInputStream,
    assets: Map<String, ByteArray>,
  ): String {
    output.writeByte(DeviceOp.PUT_ASSETS)
    output.writeInt(assets.size)
    for ((hash, bytes) in assets) {
      writeUtf(output, hash)
      output.writeInt(bytes.size)
      output.write(bytes)
    }
    output.flush()
    return readUtf(input)
  }

  private fun sendSetLive(
    output: DataOutputStream,
    input: DataInputStream,
    dexBytes: ByteArray?,
    request: DeviceLiveStateRequest,
  ): String {
    output.writeByte(DeviceOp.SET_LIVE)
    if (dexBytes == null) {
      output.writeInt(0)
    } else {
      output.writeInt(dexBytes.size)
      output.write(dexBytes)
    }
    writeUtf(
      output,
      PreviewJson.compact.encodeToString(DeviceLiveStateRequest.serializer(), request),
    )
    output.flush()
    return readUtf(input)
  }

  private data class WireFrame(
    val direction: String,
    val screen: String,
    val viewName: String,
    val overlayId: String?,
    val isEndFrame: Boolean,
    val format: FrameFormat,
    val widthPx: Int,
    val heightPx: Int,
    val rawLength: Int,
    val payload: ByteArray,
  )

  private fun sendBatch(
    output: DataOutputStream,
    input: DataInputStream,
    dexBytes: ByteArray?,
    entryClass: String,
    request: DeviceBatchRequest,
  ): Pair<DeviceBatchResponse, List<WireFrame>> {
    output.writeByte(DeviceOp.RENDER_BATCH)
    if (dexBytes == null) {
      output.writeInt(0)
    } else {
      output.writeInt(dexBytes.size)
      output.write(dexBytes)
    }
    writeUtf(output, entryClass)
    writeUtf(output, PreviewJson.compact.encodeToString(DeviceBatchRequest.serializer(), request))
    output.flush()

    val respJson = readUtf(input)
    val resp = PreviewJson.compact.decodeFromString(DeviceBatchResponse.serializer(), respJson)
    val frameCount = input.readInt()
    val frames =
      List(frameCount) {
        val dir = readUtf(input)
        val screen = readUtf(input)
        val viewName = readUtf(input)
        val ovId = readUtf(input).ifEmpty { null }
        val isEnd = input.readBoolean()
        val format = FrameFormat.valueOf(readUtf(input))
        val w = input.readInt()
        val h = input.readInt()
        val rawLen = input.readInt()
        val len = input.readInt()
        val payload = ByteArray(len)
        input.readFully(payload)
        WireFrame(dir, screen, viewName, ovId, isEnd, format, w, h, rawLen, payload)
      }
    return resp to frames
  }

  private fun writeUtf(out: DataOutputStream, text: String) {
    val bytes = text.toByteArray(StandardCharsets.UTF_8)
    out.writeInt(bytes.size)
    out.write(bytes)
  }

  private fun readUtf(inp: DataInputStream): String {
    val len = inp.readInt()
    val bytes = ByteArray(len)
    inp.readFully(bytes)
    return String(bytes, StandardCharsets.UTF_8)
  }

  private fun sha256Hex(bytes: ByteArray): String =
    MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

  private fun runAdb(vararg args: String): String {
    val cmd = listOf("adb") + args
    val proc = ProcessBuilder(cmd).redirectErrorStream(true).start()
    val out = proc.inputStream.bufferedReader(StandardCharsets.UTF_8).readText()
    check(proc.waitFor(60, TimeUnit.SECONDS)) { "Timed out: ${cmd.joinToString(" ")}" }
    check(proc.exitValue() == 0) {
      "adb failed (${proc.exitValue()}): ${cmd.joinToString(" ")}\n$out"
    }
    return out
  }
}
