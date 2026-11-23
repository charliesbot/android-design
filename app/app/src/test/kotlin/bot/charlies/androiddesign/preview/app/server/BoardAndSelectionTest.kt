package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.cli.RenderEntry
import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.model.BoardSurface
import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PhoneSurface
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BoardAndSelectionTest {
  private fun fakeBoardBackend(): PreviewBackend =
    object : PreviewBackend {
      override val name = RuntimeIdentity.BACKEND_ROBOLECTRIC
      override val supportsBatchCheck = false
      override val supportsHeadlessSpeculativePrecompile = false

      override fun compile(snapshot: ContentHasher.Snapshot): BackendCompileOutput {
        val meta =
          BoardMeta(
            directions =
              listOf(
                DirectionMeta("Editorial", "phone", "home", listOf("home"), emptyList()),
                DirectionMeta("Cards", "phone", "home", listOf("home"), emptyList()),
              ),
            views = PhoneSurface.views,
          )
        return BackendCompileOutput(
          loaded =
            LoadedExploration(
              sourcePath = snapshot.sourcePath,
              designPath = snapshot.designPath,
              designTheme = null,
              designSystem = DesignSystemResolver.resolve(null, null, PhoneSurface),
              compileMs = 2L,
              errors = emptyList(),
            ),
          dexHash = null,
          surface = BoardSurface.Phone,
          boardName = "Plates",
          boardMeta = meta,
          androidData = null,
        )
      }

      override fun renderBatchCheck(
        entry: CompileEntry,
        targets: List<CheckTargetSpec>,
        scoped: Boolean,
        rendersRoot: Path,
      ): List<RenderArtifact> = targets.map { t ->
        renderSingle(
          entry = entry,
          direction = t.direction,
          screenId = t.screenId,
          prefix = t.prefix,
          overlayId = t.overlayId,
          view = t.view,
          densityScale = 2,
          designSystem = entry.loaded.designSystem,
          baseCheck = null,
          writeFiles = true,
          rendersRoot = rendersRoot,
          frameFormat = FrameFormat.LZ4_RGBA,
        )
      }

      override fun renderSingle(
        entry: CompileEntry,
        direction: String,
        screenId: String,
        prefix: String,
        overlayId: String?,
        view: ViewSpec,
        densityScale: Int,
        designSystem: DesignSystem,
        baseCheck: ViewCheck?,
        writeFiles: Boolean,
        rendersRoot: Path,
        frameFormat: FrameFormat,
        fontScale: Float,
        seedOverride: String?,
        variantOverride: String?,
        darkOverride: Boolean?,
      ): RenderArtifact {
        val check = ViewCheck(pass = true)
        val bytes = byteArrayOf(7, 8, 9)
        return RenderArtifact(
          direction = direction,
          screen = screenId,
          overlay = overlayId,
          view = view,
          densityScale = densityScale,
          artifactName = prefix,
          pass = true,
          renderMs = 2L,
          check = check,
          layout = null,
          tree =
            LayoutTree(
              screenId,
              view,
              listOf(LayoutNode("root", "Box", bounds = Bounds(0f, 0f, 100f, 100f))),
              emptyList(),
            ),
          pngPath = null,
          endPngPath = null,
          pngBytes = bytes,
          endPngBytes = null,
          pngSha256 = ContentHasher.sha256Hex(bytes),
          entry = RenderEntry.of(prefix, view.name, null, check),
        )
      }
    }

  private fun boardSource(extraTopLines: Int = 0, duplicateButton: Boolean = false): String {
    val paddingComments = (1..extraTopLines).joinToString("") { "// shift line $it\n" }
    val secondBtn =
      if (duplicateButton) {
        """
            Button(onClick = {}, modifier = Modifier.height(56.dp)) { Text("Start") }
        """
      } else {
        ""
      }
    return paddingComments +
      """
    import androidx.compose.foundation.layout.*
    import androidx.compose.material3.*
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.platform.testTag
    import androidx.compose.ui.unit.dp
    import bot.charlies.androiddesign.preview.kit.*

    val preview = designBoard("Plates", seed = 0xFFB5523BL, variant = PaletteVariant.TonalSpot) {
      direction("Editorial") {
        screen("home") {
          Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Dinner")
            Button(onClick = {}, modifier = Modifier.testTag(AD_PRIMARY_TAG).height(56.dp)) { Text("Start") }
            $secondBtn
          }
        }
      }
      direction("Cards") {
        screen("home") {
          Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Cards")
            Button(onClick = {}, modifier = Modifier.testTag(AD_PRIMARY_TAG).height(56.dp)) { Text("Go") }
          }
        }
      }
    }
    """
        .trimIndent()
  }

  private fun post(
    port: Int,
    path: String,
    headers: Map<String, String>,
    body: String,
  ): Pair<Int, String> {
    val conn = URI("http://127.0.0.1:$port$path").toURL().openConnection() as HttpURLConnection
    return try {
      conn.requestMethod = "POST"
      conn.connectTimeout = 2_000
      conn.readTimeout = 30_000
      conn.setRequestProperty("Host", "127.0.0.1:$port")
      headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
      conn.doOutput = true
      conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
      conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
      val status = conn.responseCode
      val stream =
        if (status in 200..399) conn.inputStream else (conn.errorStream ?: conn.inputStream)
      status to stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
    } finally {
      conn.disconnect()
    }
  }

  private fun get(port: Int, path: String, headers: Map<String, String>): Pair<Int, String> {
    val conn = URI("http://127.0.0.1:$port$path").toURL().openConnection() as HttpURLConnection
    return try {
      conn.requestMethod = "GET"
      conn.connectTimeout = 2_000
      conn.readTimeout = 10_000
      conn.setRequestProperty("Host", "127.0.0.1:$port")
      headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
      val status = conn.responseCode
      val stream =
        if (status in 200..399) conn.inputStream else (conn.errorStream ?: conn.inputStream)
      status to stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
    } finally {
      conn.disconnect()
    }
  }

  @Test
  fun `SSE streams stale then focused view first before remaining views on file save`() {
    val tmp = createTempDirectory("v6-sse-")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it) }
    val source = repo.resolve("board.explore.kt").also { Files.writeString(it, boardSource()) }

    val server =
      PreviewHttpServer(
        repoRoot = repo,
        cacheRoot = tmp.resolve("cache"),
        preferredPort = 0,
        backend = fakeBoardBackend(),
        enableFileWatcher = false,
      )
    try {
      val rec = server.start()
      // Verify board HTML is served with CSP nonce.
      val (htmlStatus, htmlBody) = get(rec.port, "/?view=${rec.viewToken}", emptyMap())
      assertEquals(200, htmlStatus)
      assertTrue("Android Design Preview Board" in htmlBody)
      assertFalse("{{NONCE}}" in htmlBody)

      // Connect SSE client before setting focus and saving.
      val events = CopyOnWriteArrayList<Pair<String, String>>()
      val connected = CountDownLatch(1)
      val gotThreeUpdates = CountDownLatch(3)
      val sseConn =
        URI("http://127.0.0.1:${rec.port}/api/events?view=${rec.viewToken}")
          .toURL()
          .openConnection() as HttpURLConnection
      sseConn.setRequestProperty("Host", "127.0.0.1:${rec.port}")
      sseConn.connectTimeout = 2_000
      sseConn.readTimeout = 10_000

      val readerThread = Thread {
        try {
          BufferedReader(InputStreamReader(sseConn.inputStream, Charsets.UTF_8)).use { reader ->
            var eventName = ""
            while (true) {
              val line = reader.readLine() ?: break
              if (line.startsWith(": connected")) {
                connected.countDown()
              } else if (line.startsWith("event:")) {
                eventName = line.removePrefix("event:").trim()
              } else if (line.startsWith("data:")) {
                val data = line.removePrefix("data:").trim()
                events += eventName to data
                if (eventName == "view_updated") {
                  gotThreeUpdates.countDown()
                }
              }
            }
          }
        } catch (_: Exception) {
          // Closed on teardown.
        }
      }
      readerThread.isDaemon = true
      readerThread.start()
      assertTrue(connected.await(5, TimeUnit.SECONDS), "SSE client did not connect")

      // Report focus on Cards / desktop (NOT the first direction or first view).
      val (focusStatus, _) =
        post(
          rec.port,
          "/api/board/focus",
          headers =
            mapOf("X-View-Token" to rec.viewToken, "Origin" to "http://127.0.0.1:${rec.port}"),
          body = """{"direction":"Cards","screen":"home","view":"desktop"}""",
        )
      assertEquals(200, focusStatus)

      // Trigger file save handling.
      Files.writeString(source, boardSource(extraTopLines = 2))
      server.handleFileChanged(source, null)

      assertTrue(
        gotThreeUpdates.await(15, TimeUnit.SECONDS),
        "expected SSE view_updated events, got $events",
      )
      sseConn.disconnect()

      val names = events.map { it.first }
      val staleIdx = names.indexOf("stale")
      val firstViewIdx = names.indexOf("view_updated")
      assertTrue(staleIdx >= 0, "expected stale event before redraw: $names")
      assertTrue(firstViewIdx > staleIdx, "stale must precede view_updated: $names")

      val firstUpdated =
        PreviewJson.compact.decodeFromString(
          BoardRenderResponse.serializer(),
          events[firstViewIdx].second,
        )
      assertEquals("Cards", firstUpdated.direction)
      assertEquals("desktop", firstUpdated.view)
    } finally {
      server.stop()
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `selection storage enforces Origin and viewToken and re-resolves or marks stale on edits`() {
    val tmp = createTempDirectory("v6-sel-")
    val repo = tmp.resolve("repo").also { Files.createDirectories(it) }
    val source = repo.resolve("sel.explore.kt").also { Files.writeString(it, boardSource()) }

    val server =
      PreviewHttpServer(
        repoRoot = repo,
        cacheRoot = tmp.resolve("cache"),
        preferredPort = 0,
        backend = fakeBoardBackend(),
        enableFileWatcher = false,
      )
    try {
      val rec = server.start()
      val snap1 = ContentHasher.snapshot(source)
      val lines1 = Files.readAllLines(source)
      val buttonLine1 = lines1.indexOfFirst { "Text(\"Start\")" in it } + 1
      assertTrue(buttonLine1 > 0)

      val selBody =
        PreviewJson.compact.encodeToString(
          BoardSelectionRequest.serializer(),
          BoardSelectionRequest(
            path = source.toString(),
            file = "sel.explore.kt",
            line = buttonLine1,
            callName = "Button",
            callChain = listOf("Column"),
            direction = "Editorial",
            screen = "home",
            view = "light",
            bounds = Bounds(16f, 40f, 120f, 56f),
            contentHash = snap1.contentHash,
          ),
        )

      // Reject missing Origin or bad viewToken.
      val (noOrigin, _) =
        post(rec.port, "/api/board/selection", mapOf("X-View-Token" to rec.viewToken), selBody)
      assertEquals(403, noOrigin)
      val (badTok, _) =
        post(
          rec.port,
          "/api/board/selection",
          mapOf("X-View-Token" to "wrong", "Origin" to "http://127.0.0.1:${rec.port}"),
          selBody,
        )
      assertEquals(403, badTok)

      // Record valid selection.
      val (okStatus, _) =
        post(
          rec.port,
          "/api/board/selection",
          mapOf("X-View-Token" to rec.viewToken, "Origin" to "http://127.0.0.1:${rec.port}"),
          selBody,
        )
      assertEquals(200, okStatus)

      // Shift lines down by 4 comments -> unique match updates line number cleanly, stale = false.
      Files.writeString(source, boardSource(extraTopLines = 4))
      val lines2 = Files.readAllLines(source)
      val buttonLine2 = lines2.indexOfFirst { "Text(\"Start\")" in it } + 1
      assertEquals(buttonLine1 + 4, buttonLine2)
      val tree2 =
        LayoutTree(
          screen = "home",
          view = Views.Light,
          nodes =
            listOf(
              LayoutNode("col", "Column", bounds = Bounds(0f, 0f, 400f, 800f)),
              LayoutNode(
                id = "btn",
                type = "Button",
                parent = "col",
                bounds = Bounds(16f, 44f, 120f, 56f),
                sourceFile = "sel.explore.kt",
                sourceLine = buttonLine2,
              ),
            ),
          findings = emptyList(),
        )
      server.selectionStore.reResolve(
        sourcePath = source,
        newContentHash = "hash-shifted",
        direction = "Editorial",
        screen = "home",
        overlay = null,
        view = "light",
        tree = tree2,
        sourceLines = lines2,
      )
      val resolved = assertNotNull(server.selectionStore.current(source))
      assertEquals(buttonLine2, resolved.line)
      assertFalse(resolved.stale)
      assertEquals("hash-shifted", resolved.contentHash)

      // Ambiguous edit: two identical Buttons under the same callChain -> stale = true,
      // callName/chain preserved.
      Files.writeString(source, boardSource(extraTopLines = 4, duplicateButton = true))
      val lines3 = Files.readAllLines(source)
      val btnLines = lines3.mapIndexedNotNull { i, s ->
        if ("Text(\"Start\")" in s) i + 1 else null
      }
      assertEquals(2, btnLines.size)
      val tree3 =
        LayoutTree(
          screen = "home",
          view = Views.Light,
          nodes =
            listOf(
              LayoutNode("col", "Column", bounds = Bounds(0f, 0f, 400f, 800f)),
              LayoutNode(
                "b1",
                "Button",
                parent = "col",
                bounds = Bounds(16f, 44f, 120f, 56f),
                sourceFile = "sel.explore.kt",
                sourceLine = btnLines[0],
              ),
              LayoutNode(
                "b2",
                "Button",
                parent = "col",
                bounds = Bounds(16f, 108f, 120f, 56f),
                sourceFile = "sel.explore.kt",
                sourceLine = btnLines[1],
              ),
            ),
          findings = emptyList(),
        )
      // Make both source lines match the stored text to force exact ambiguity.
      val identicalLines =
        lines3.toMutableList().apply {
          this[btnLines[1] - 1] = this[btnLines[0] - 1]
        }
      server.selectionStore.reResolve(
        sourcePath = source,
        newContentHash = "hash-ambiguous",
        direction = "Editorial",
        screen = "home",
        overlay = null,
        view = "light",
        tree = tree3,
        sourceLines = identicalLines,
      )
      val ambiguous = assertNotNull(server.selectionStore.current(source))
      assertTrue(ambiguous.stale, "ambiguous edit must mark selection stale")
      assertEquals("Button", ambiguous.callName)
      assertEquals(listOf("Column"), ambiguous.callChain)
    } finally {
      server.stop()
      tmp.toFile().deleteRecursively()
    }
  }
}
