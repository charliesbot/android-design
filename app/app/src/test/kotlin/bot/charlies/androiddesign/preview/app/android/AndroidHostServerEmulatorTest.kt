package bot.charlies.androiddesign.preview.app.android

import bot.charlies.androiddesign.preview.app.server.AndroidPreviewBackend
import bot.charlies.androiddesign.preview.app.server.BoardRenderRequest
import bot.charlies.androiddesign.preview.app.server.BoardRenderResponse
import bot.charlies.androiddesign.preview.app.server.BoardSelectionRequest
import bot.charlies.androiddesign.preview.app.server.CheckRequest
import bot.charlies.androiddesign.preview.app.server.CheckResponse
import bot.charlies.androiddesign.preview.app.server.PreviewHttpServer
import bot.charlies.androiddesign.preview.app.server.RuntimeIdentity
import bot.charlies.androiddesign.preview.app.server.SelectionResponse
import bot.charlies.androiddesign.preview.app.server.SelectionStore
import bot.charlies.androiddesign.preview.app.server.ServerPaths
import bot.charlies.androiddesign.preview.app.server.StatusResponse
import bot.charlies.androiddesign.preview.app.server.TreeRequest
import bot.charlies.androiddesign.preview.app.server.TreeResponse
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.domain.image.FastPngEncoder
import bot.charlies.androiddesign.preview.model.PreviewJson
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Base64
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * End-to-end server, D8 compiler, asset cache, WEBP board card, click selection line-shift, and
 * headless FileWatcher verification against a live Android emulator.
 *
 * Gated by `-Dadp.runEmulatorTests=true`.
 */
class AndroidHostServerEmulatorTest {

  private fun httpCall(
    url: String,
    method: String = "GET",
    headers: Map<String, String> = emptyMap(),
    body: String? = null,
  ): Pair<Int, String> {
    val conn = (URI(url).toURL().openConnection() as HttpURLConnection)
    conn.requestMethod = method
    conn.connectTimeout = 5_000
    conn.readTimeout = 30_000
    for ((k, v) in headers) conn.setRequestProperty(k, v)
    if (body != null) {
      conn.doOutput = true
      conn.setRequestProperty("Content-Type", "application/json")
      conn.outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) }
    }
    val code = conn.responseCode
    val stream = if (code in 200..399) conn.inputStream else (conn.errorStream ?: conn.inputStream)
    val text = stream.bufferedReader(StandardCharsets.UTF_8).readText()
    conn.disconnect()
    return code to text
  }

  private fun sampleExplore(
    bannerComment: String = "// initial banner",
    titleText: String = "Continue",
  ): String =
    """
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.size
    import androidx.compose.foundation.layout.widthIn
    import androidx.compose.material3.Button
    import androidx.compose.material3.ExperimentalMaterial3Api
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.Scaffold
    import androidx.compose.material3.Text
    import androidx.compose.material3.TopAppBar
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.platform.testTag
    import androidx.compose.ui.unit.dp
    import bot.charlies.androiddesign.preview.kit.AD_PRIMARY_TAG
    import bot.charlies.androiddesign.preview.kit.BundledFont
    import bot.charlies.androiddesign.preview.kit.PaletteVariant
    import bot.charlies.androiddesign.preview.kit.designBoard
    import bot.charlies.androiddesign.preview.kit.designPainter

    @OptIn(ExperimentalMaterial3Api::class)
    val preview = designBoard(
      name = "Emulator E2E",
      seed = 0x5B4CEEL,
      variant = PaletteVariant.Expressive,
      brandFont = BundledFont.Fraunces,
    ) {
      direction("Editorial") {
        screen("home") {
          Scaffold(
            topBar = { TopAppBar(title = { Text("Emulator E2E") }) },
          ) { innerPadding ->
            Column(
              modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp).widthIn(max = 420.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Image(
                painter = designPainter("assets/cover.png"),
                contentDescription = "Cover image",
                modifier = Modifier.size(64.dp),
              )
              $bannerComment
              Button(
                onClick = {},
                modifier = Modifier.testTag(AD_PRIMARY_TAG).height(56.dp),
              ) {
                Text("$titleText")
              }
            }
          }
        }
      }
    }
    """
      .trimIndent()

  @Test
  fun `end to end android server check, tree, webp board card, selection line shift, and headless speculative precompile`(
    @TempDir tmp: Path,
  ) {
    Assumptions.assumeTrue(
      System.getProperty("adp.runEmulatorTests") == "true",
      "Skipped unless -Dadp.runEmulatorTests=true",
    )

    val meta = BootstrapMeta.locate()
    val cache = tmp.resolve("cache")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val assetsDir = repo.resolve("assets").also { Files.createDirectories(it) }
    val rgba = ByteArray(16 * 16 * 4) { idx -> if (idx % 4 == 3) 0xFF.toByte() else 0x55 }
    Files.write(assetsDir.resolve("cover.png"), FastPngEncoder.encodeFast(16, 16, rgba))

    val explore = repo.resolve("Demo.explore.kt")
    Files.writeString(explore, sampleExplore())

    val deviceManager = DeviceManager(cacheRoot = cache)
    val ready =
      assertIs<DeviceSetupResult.Ready>(deviceManager.ensureDeviceReady(meta, headless = true))
    val currentPort = java.util.concurrent.atomic.AtomicInteger(ready.forwardPort)
    val transport =
      AdbTcpDeviceTransport(
        connectAndEnsurePort = {
          val ping = DeviceManager.pingLocalForward(currentPort.get())
          if (ping != null && ping.ok) {
            currentPort.get()
          } else {
            val refreshed =
              deviceManager.ensureDeviceReady(meta = meta, requestedSerial = ready.serial)
            if (refreshed is DeviceSetupResult.Ready) {
              currentPort.set(refreshed.forwardPort)
              refreshed.forwardPort
            } else {
              currentPort.get()
            }
          }
        },
        restartSandboxActivity = { deviceManager.restartSandboxOnSerial(ready.serial) },
      )
    val backend = AndroidPreviewBackend(meta = meta, transport = transport)
    val identity = RuntimeIdentity.compute(meta = meta)
    val server =
      PreviewHttpServer(
        repoRoot = repo,
        cacheRoot = cache,
        preferredPort = 0,
        backend = backend,
        runtimeIdentity = identity,
        deviceSerial = ready.serial,
        adbForwardPort = ready.forwardPort,
        apkSha256 = ready.apkSha256,
        enableFileWatcher = true,
      )
    val record = server.start()
    val base = "http://127.0.0.1:${record.port}"
    val repoId = ServerPaths.repoHash(repo)
    val secretHeader = mapOf("X-Client-Secret" to record.clientSecret)

    try {
      // 1. Verify status reports android backend + device + forward port.
      val (statusCode, statusBody) =
        httpCall("$base/api/status?repo=$repoId", headers = secretHeader)
      assertEquals(200, statusCode, statusBody)
      val status = PreviewJson.compact.decodeFromString(StatusResponse.serializer(), statusBody)
      assertEquals(RuntimeIdentity.BACKEND_ANDROID, status.backend)
      assertEquals(ready.serial, status.deviceSerial)
      assertEquals(ready.forwardPort, status.adbForwardPort)

      // 2. Run /api/check -> valid + host PNGs written cleanly.
      val (checkCode, checkBody) =
        httpCall(
          "$base/api/check",
          method = "POST",
          headers = secretHeader,
          body =
            PreviewJson.compact.encodeToString(
              CheckRequest.serializer(),
              CheckRequest(path = explore.toString(), repoRoot = repo.toString()),
            ),
        )
      assertEquals(200, checkCode, checkBody)
      val checkResp = PreviewJson.compact.decodeFromString(CheckResponse.serializer(), checkBody)
      assertTrue(checkResp.valid, "Expected valid exploration, errors: ${checkResp.errors}")
      assertTrue(checkResp.renders.isNotEmpty())
      assertTrue(checkResp.images.isNotEmpty(), "Expected host PNGs on unscoped check")
      for (img in checkResp.images) {
        val imgBytes = Files.readAllBytes(Path.of(img.path))
        assertEquals(
          listOf(0x89.toByte(), 0x50, 0x4E, 0x47),
          imgBytes.take(4),
          "Expected PNG header at ${img.path}",
        )
      }

      // 3. Run /api/tree -> verify nodes carry real source line info.
      val (treeCode, treeBody) =
        httpCall(
          "$base/api/tree",
          method = "POST",
          headers = secretHeader,
          body =
            PreviewJson.compact.encodeToString(
              TreeRequest.serializer(),
              TreeRequest(
                path = explore.toString(),
                direction = "Editorial",
                screen = "home",
                view = "light",
                repoRoot = repo.toString(),
              ),
            ),
        )
      assertEquals(200, treeCode, treeBody)
      val treeResp = PreviewJson.compact.decodeFromString(TreeResponse.serializer(), treeBody)
      assertTrue(treeResp.ok, treeBody)
      val tree = assertNotNull(treeResp.tree)
      val clickableNode =
        tree.nodes.firstOrNull {
          it.type == "Button" && it.sourceFile == "Demo.explore.kt" && (it.sourceLine ?: 0) > 0
        }
          ?: tree.nodes.lastOrNull {
            it.sourceFile == "Demo.explore.kt" && (it.sourceLine ?: 0) > 0
          }
      val selNode =
        assertNotNull(
          clickableNode,
          "Expected a node with Demo.explore.kt sourceLine in ${tree.nodes}",
        )
      val originalLine = selNode.sourceLine!!

      // 4. Board WEBP_LOSSLESS render for a non-primary view (`dark`) whose frame was skipped
      // during /api/check -> returns image/webp + RIFF bytes.
      val boardHeaders = mapOf("Origin" to base)
      val (renderCode, renderBody) =
        httpCall(
          "$base/api/board/render?view=${record.viewToken}&repo=$repoId",
          method = "POST",
          headers = boardHeaders,
          body =
            PreviewJson.compact.encodeToString(
              BoardRenderRequest.serializer(),
              BoardRenderRequest(
                repo = repoId,
                direction = "Editorial",
                screen = "home",
                view = "dark",
              ),
            ),
        )
      assertEquals(200, renderCode, renderBody)
      val boardRender =
        PreviewJson.compact.decodeFromString(BoardRenderResponse.serializer(), renderBody)
      assertEquals("image/webp", boardRender.imageMime)
      val webpBytes = Base64.getDecoder().decode(assertNotNull(boardRender.pngBase64))
      assertEquals("RIFF", String(webpBytes.copyOfRange(0, 4), Charsets.US_ASCII))

      // 5. Board click selection + 2-line shift re-resolution after editing file.
      val byId = tree.nodes.associateBy { it.id }
      val callChain = SelectionStore.ancestorChain(selNode, byId)
      val (selPostCode, selPostBody) =
        httpCall(
          "$base/api/board/selection?view=${record.viewToken}&repo=$repoId",
          method = "POST",
          headers = boardHeaders,
          body =
            PreviewJson.compact.encodeToString(
              BoardSelectionRequest.serializer(),
              BoardSelectionRequest(
                repo = repoId,
                path = explore.toString(),
                file = selNode.sourceFile!!,
                line = originalLine,
                callName = selNode.type,
                callChain = callChain,
                direction = "Editorial",
                screen = "home",
                view = "light",
                bounds = selNode.bounds,
                contentHash = checkResp.contentHash!!,
              ),
            ),
        )
      assertEquals(200, selPostCode, selPostBody)
      val selResp =
        PreviewJson.compact.decodeFromString(SelectionResponse.serializer(), selPostBody)
      assertEquals(originalLine, selResp.selection?.line)

      // Insert 2 comment lines before the Button while verifying headless speculative FileWatcher
      // pre-compile hits cache (zero SSE clients open).
      val compilesBeforeEdit = server.renderQueue.compileCount
      val shifted =
        Files.readString(explore)
          .replace(
            "// initial banner",
            "// shifted line 1\n              // shifted line 2\n              // shifted line 3",
          )
      Files.writeString(explore, shifted)

      val deadline = System.currentTimeMillis() + 8_000L
      while (
        server.renderQueue.compileCount <= compilesBeforeEdit &&
          System.currentTimeMillis() < deadline
      ) {
        Thread.sleep(100)
      }
      val compilesAfterSpeculative = server.renderQueue.compileCount
      assertTrue(
        compilesAfterSpeculative > compilesBeforeEdit,
        "Expected headless FileWatcher speculative compile while SSE clients are empty",
      )

      val (shiftedCheckCode, shiftedCheckBody) =
        httpCall(
          "$base/api/check",
          method = "POST",
          headers = secretHeader,
          body =
            PreviewJson.compact.encodeToString(
              CheckRequest.serializer(),
              CheckRequest(path = explore.toString(), repoRoot = repo.toString()),
            ),
        )
      assertEquals(200, shiftedCheckCode, shiftedCheckBody)
      val shiftedCheck =
        PreviewJson.compact.decodeFromString(CheckResponse.serializer(), shiftedCheckBody)
      assertTrue(shiftedCheck.cached, "Check right after speculative compile should hit cache")
      assertEquals(compilesAfterSpeculative, server.renderQueue.compileCount)
      assertEquals(
        originalLine + 2,
        shiftedCheck.selection?.line,
        "Selection line should shift by +2",
      )
      assertEquals(false, shiftedCheck.selection?.stale)

      // 6. Verify secondary PING socket responds without deadlocking while persistent TCP bridge
      // stays open, then run a second repo check through the shared server.
      val secondaryPing = DeviceManager.pingLocalForward(currentPort.get())
      assertNotNull(secondaryPing, "Secondary PING must succeed alongside keep-alive bridge")
      assertTrue(secondaryPing.ok)

      val repo2 = tmp.resolve("repo2").also { Files.createDirectories(it.resolve(".git")) }
      val explore2 = repo2.resolve("Second.explore.kt")
      Files.writeString(explore2, sampleExplore(titleText = "Launch"))
      val (check2Code, check2Body) =
        httpCall(
          "$base/api/check",
          method = "POST",
          headers = secretHeader,
          body =
            PreviewJson.compact.encodeToString(
              CheckRequest.serializer(),
              CheckRequest(path = explore2.toString(), repoRoot = repo2.toString()),
            ),
        )
      assertEquals(200, check2Code, check2Body)
      val check2 = PreviewJson.compact.decodeFromString(CheckResponse.serializer(), check2Body)
      assertTrue(check2.valid, "Expected second repo check to pass: ${check2.errors}")
    } finally {
      server.stop()
      transport.close()
    }
  }
}
