package bot.charlies.androiddesign.preview.render

import bot.charlies.androiddesign.preview.app.android.AndroidExplorationCompiler
import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.model.ClassBundleCodec
import bot.charlies.androiddesign.preview.model.DeviceBatchRequest
import bot.charlies.androiddesign.preview.model.DeviceRenderTarget
import bot.charlies.androiddesign.preview.model.DeviceWireCodec
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.Views
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import java.util.zip.CRC32
import java.util.zip.Deflater
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class LayoutlibWorkerProcessTest {
  private val repoRoot: Path =
    Path.of(
      System.getProperty("adp.repo.root") ?: error("adp.repo.root system property missing"),
    )
  private val meta: BootstrapMeta by lazy { BootstrapMeta.locate() }

  private fun startWorker(): Process {
    val javaBin = Path.of(System.getProperty("java.home"), "bin", "java").toString()
    return ProcessBuilder(
        javaBin,
        "-Xmx2g",
        "-Djava.awt.headless=true",
        "-Djdk.attach.allowAttachSelf=true",
        "-XX:+EnableDynamicAgentLoading",
        "@${meta.workerCp}",
        "bot.charlies.androiddesign.preview.render.LayoutlibWorkerMainKt",
        "--bootstrap-dir",
        meta.bootstrapDir.toString(),
      )
      .redirectError(ProcessBuilder.Redirect.INHERIT)
      .start()
  }

  private fun withWorker(block: (DataInputStream, DataOutputStream, Process) -> Unit) {
    val proc = startWorker()
    try {
      val out = DataOutputStream(BufferedOutputStream(proc.outputStream, 65536))
      val inp = DataInputStream(BufferedInputStream(proc.inputStream, 65536))
      block(inp, out, proc)
    } finally {
      runCatching { proc.outputStream.close() }
      if (!proc.waitFor(5, TimeUnit.SECONDS)) {
        proc.destroyForcibly()
      }
    }
  }

  private fun sha256(bytes: ByteArray): String =
    MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

  private fun compileFile(sourcePath: Path): AndroidExplorationCompiler.ClassStage {
    val outcome = AndroidExplorationCompiler.compileClassStage(sourcePath, null, meta)
    return when (outcome) {
      is AndroidExplorationCompiler.ClassStageOutcome.Ready -> outcome.stage
      is AndroidExplorationCompiler.ClassStageOutcome.Failed ->
        error("Compile failed for $sourcePath: ${outcome.errors}")
    }
  }

  @Test
  fun `worker responds to ping and exits within 2 seconds when stdin closes`() {
    val proc = startWorker()
    try {
      val out = DataOutputStream(BufferedOutputStream(proc.outputStream, 65536))
      val inp = DataInputStream(BufferedInputStream(proc.inputStream, 65536))

      DeviceWireCodec.writePingRequest(out)
      val ping = DeviceWireCodec.readPingResponse(inp)
      assertTrue(ping.ok)
      assertEquals("pong", ping.status)
      assertTrue(ping.pid > 0)

      out.close()
      assertTrue(
        proc.waitFor(2, TimeUnit.SECONDS),
        "Worker did not exit within 2s after stdin closed",
      )
      assertEquals(0, proc.exitValue())
    } finally {
      if (proc.isAlive) proc.destroyForcibly()
    }
  }

  @Test
  fun `worker renders all 15 views of multiturn fixture with zero finding errors`() {
    val fixturePath = repoRoot.resolve("evals/multiturn_fixture/design/proposal.explore.kt")
    val stage = compileFile(fixturePath)
    val bundleBytes = ClassBundleCodec.encode(stage.classBytes)
    val classHash = sha256(bundleBytes)

    withWorker { inp, out, _ ->
      DeviceWireCodec.writePreloadDexRequest(out, classHash, stage.entryClass, bundleBytes)
      val preload = DeviceWireCodec.readPreloadDexResponse(inp)
      assertTrue(preload.ok, "Preload failed: ${preload.error}")
      val boardMeta = checkNotNull(preload.boardMeta)

      val targets =
        boardMeta.directions.flatMap { dir ->
          val baseTargets =
            dir.screens.flatMap { screen ->
              boardMeta.views.map { view ->
                DeviceRenderTarget(
                  direction = dir.name,
                  screen = screen,
                  viewName = view.name,
                  frameFormat = FrameFormat.NONE,
                )
              }
            }
          val overlayTargets =
            dir.overlays.flatMap { ovl ->
              val scr = ovl.screen ?: dir.startScreen
              boardMeta.views.map { view ->
                DeviceRenderTarget(
                  direction = dir.name,
                  screen = scr,
                  viewName = view.name,
                  overlayId = ovl.id,
                  frameFormat = FrameFormat.NONE,
                )
              }
            }
          baseTargets + overlayTargets
        }

      assertEquals(15, targets.size, "Expected 15 phone-family targets in multiturn fixture")

      DeviceWireCodec.writeRenderBatchRequest(
        out,
        dexBytes = null,
        entryClass = stage.entryClass,
        request =
          DeviceBatchRequest(
            dexHash = classHash,
            entryClass = stage.entryClass,
            sourceFile = stage.sourceFile,
            sourceText = stage.sourceText,
            designTheme = stage.designTheme,
            targets = targets,
            explorationDir = fixturePath.parent.toString(),
          ),
      )
      val batch = DeviceWireCodec.readRenderBatchResponse(inp)
      assertTrue(batch.response.ok, "Batch failed: ${batch.response.error}")
      assertEquals(15, batch.response.results.size)

      for (res in batch.response.results) {
        val check = ViewCheck.of(res.tree)
        assertTrue(
          check.errors.isEmpty(),
          "Expected zero finding errors on ${res.direction}/${res.screen}/${res.viewName}, got: ${check.errors}",
        )
      }
    }
  }

  @Test
  fun `worker catches line-length and contrast errors on calibration bad boards`() {
    val settingsPath = repoRoot.resolve("evals/calibration/code-settings-bad.explore.kt")
    val recipesPath = repoRoot.resolve("evals/calibration/code-recipes-bad.explore.kt")

    val settingsStage = compileFile(settingsPath)
    val recipesStage = compileFile(recipesPath)

    withWorker { inp, out, _ ->
      fun renderAllErrors(path: Path, stage: AndroidExplorationCompiler.ClassStage): Set<String> {
        val bundle = ClassBundleCodec.encode(stage.classBytes)
        val hash = sha256(bundle)
        DeviceWireCodec.writePreloadDexRequest(out, hash, stage.entryClass, bundle)
        val preload = DeviceWireCodec.readPreloadDexResponse(inp)
        assertTrue(preload.ok, preload.error)
        val metaBoard = checkNotNull(preload.boardMeta)
        val targets =
          metaBoard.directions.flatMap { dir ->
            dir.screens.flatMap { scr ->
              metaBoard.views.map { v ->
                DeviceRenderTarget(dir.name, scr, v.name, frameFormat = FrameFormat.NONE)
              }
            }
          }
        DeviceWireCodec.writeRenderBatchRequest(
          out,
          dexBytes = null,
          entryClass = stage.entryClass,
          request =
            DeviceBatchRequest(
              dexHash = hash,
              entryClass = stage.entryClass,
              sourceFile = stage.sourceFile,
              sourceText = stage.sourceText,
              designTheme = stage.designTheme,
              targets = targets,
              explorationDir = path.parent.toString(),
            ),
        )
        val batch = DeviceWireCodec.readRenderBatchResponse(inp)
        assertTrue(batch.response.ok, batch.response.error)
        return batch.response.results
          .flatMap { ViewCheck.of(it.tree).errors }
          .map { it.rule }
          .toSet()
      }

      val settingsRules = renderAllErrors(settingsPath, settingsStage)
      assertTrue(
        "line-length" in settingsRules,
        "Expected line-length error on code-settings-bad, got: $settingsRules",
      )

      val recipesRules = renderAllErrors(recipesPath, recipesStage)
      assertTrue(
        "contrast" in recipesRules,
        "Expected contrast error on code-recipes-bad, got: $recipesRules",
      )
    }
  }

  @Test
  fun `worker renders Wear fixture with scrolled end tree and curved screen`(@TempDir tmp: Path) {
    val wearFile = tmp.resolve("WearFixture.explore.kt")
    Files.writeString(wearFile, WEAR_EXPLORE_SOURCE)
    val stage = compileFile(wearFile)
    val bundle = ClassBundleCodec.encode(stage.classBytes)
    val hash = sha256(bundle)

    withWorker { inp, out, _ ->
      DeviceWireCodec.writePreloadDexRequest(out, hash, stage.entryClass, bundle)
      val preload = DeviceWireCodec.readPreloadDexResponse(inp)
      assertTrue(preload.ok, preload.error)
      val dirName = preload.boardMeta!!.directions.single().name

      val wearViews = listOf(Views.Watch, Views.WatchLarge)
      val targets = wearViews.flatMap { view ->
        listOf(
          DeviceRenderTarget(dirName, "main", view.name, frameFormat = FrameFormat.RAW_RGBA),
          DeviceRenderTarget(dirName, "curved", view.name, frameFormat = FrameFormat.RAW_RGBA),
          DeviceRenderTarget(dirName, "date", view.name, frameFormat = FrameFormat.NONE),
          DeviceRenderTarget(
            dirName,
            "main",
            view.name,
            overlayId = "confirm",
            frameFormat = FrameFormat.RAW_RGBA,
          ),
        )
      }

      DeviceWireCodec.writeRenderBatchRequest(
        out,
        dexBytes = null,
        entryClass = stage.entryClass,
        request =
          DeviceBatchRequest(
            dexHash = hash,
            entryClass = stage.entryClass,
            sourceFile = stage.sourceFile,
            sourceText = stage.sourceText,
            targets = targets,
            explorationDir = tmp.toString(),
          ),
      )
      val batch = DeviceWireCodec.readRenderBatchResponse(inp)
      assertTrue(batch.response.ok, batch.response.error)
      assertEquals(8, batch.response.results.size)

      for (viewSpec in wearViews) {
        val mainRes =
          batch.response.results.first {
            it.screen == "main" && it.viewName == viewSpec.name && it.overlayId == null
          }
        assertNotNull(mainRes.endTree, "Expected scrolled endTree for Wear main/${viewSpec.name}")
        assertTrue(mainRes.hasEndFrame, "Expected scrolled endFrame for Wear main/${viewSpec.name}")

        val curvedRes =
          batch.response.results.first { it.screen == "curved" && it.viewName == viewSpec.name }
        assertTrue(
          curvedRes.tree.nodes.isNotEmpty(),
          "Expected nodes for Wear curved/${viewSpec.name}",
        )

        val confirmRes =
          batch.response.results.first {
            it.screen == "main" && it.viewName == viewSpec.name && it.overlayId == "confirm"
          }
        assertTrue(confirmRes.hasFrame, "Expected frame for Wear confirm/${viewSpec.name}")
        assertTrue(
          confirmRes.tree.nodes.isNotEmpty(),
          "Expected tree nodes for Wear confirm/${viewSpec.name}",
        )
        val confirmFrame =
          batch.frames.first {
            it.screen == "main" && it.viewName == viewSpec.name && it.overlayId == "confirm"
          }
        val mainFrame =
          batch.frames.first {
            it.screen == "main" &&
              it.viewName == viewSpec.name &&
              it.overlayId == null &&
              !it.isEndFrame
          }
        var diffPixels = 0
        var offset = 0
        while (offset + 3 < confirmFrame.payload.size && offset + 3 < mainFrame.payload.size) {
          if (
            confirmFrame.payload[offset] != mainFrame.payload[offset] ||
              confirmFrame.payload[offset + 1] != mainFrame.payload[offset + 1] ||
              confirmFrame.payload[offset + 2] != mainFrame.payload[offset + 2] ||
              confirmFrame.payload[offset + 3] != mainFrame.payload[offset + 3]
          ) {
            diffPixels++
          }
          offset += 4
        }
        assertTrue(
          diffPixels > 10_000,
          "Expected Wear confirm AlertDialog to composite visible pixels over main/${viewSpec.name}, diffPixels=$diffPixels",
        )
      }
    }
  }

  @Test
  fun `worker loads assets from explorationDir and extracts in-view overlay bounds`(
    @TempDir tmp: Path,
  ) {
    writeSampleCoverPng(tmp)
    val phoneFile = tmp.resolve("PhoneFixture.explore.kt")
    Files.writeString(phoneFile, PHONE_EXPLORE_SOURCE)
    val stage = compileFile(phoneFile)
    val bundle = ClassBundleCodec.encode(stage.classBytes)
    val hash = sha256(bundle)

    withWorker { inp, out, _ ->
      DeviceWireCodec.writePreloadDexRequest(out, hash, stage.entryClass, bundle)
      val preload = DeviceWireCodec.readPreloadDexResponse(inp)
      assertTrue(preload.ok, preload.error)
      val dirName = preload.boardMeta!!.directions.single().name

      val targets =
        listOf(
          DeviceRenderTarget(dirName, "home", Views.Light.name, frameFormat = FrameFormat.RAW_RGBA),
          DeviceRenderTarget(
            dirName,
            "home",
            Views.Light.name,
            overlayId = "dialog",
            frameFormat = FrameFormat.RAW_RGBA,
          ),
          DeviceRenderTarget(
            dirName,
            "home",
            Views.Light.name,
            overlayId = "sheet",
            frameFormat = FrameFormat.RAW_RGBA,
          ),
          DeviceRenderTarget(
            dirName,
            "home",
            Views.Light.name,
            overlayId = "popup",
            frameFormat = FrameFormat.RAW_RGBA,
          ),
        )

      DeviceWireCodec.writeRenderBatchRequest(
        out,
        dexBytes = null,
        entryClass = stage.entryClass,
        request =
          DeviceBatchRequest(
            dexHash = hash,
            entryClass = stage.entryClass,
            sourceFile = stage.sourceFile,
            sourceText = stage.sourceText,
            designTheme = stage.designTheme,
            targets = targets,
            explorationDir = tmp.toString(),
          ),
      )
      val batch = DeviceWireCodec.readRenderBatchResponse(inp)
      assertTrue(batch.response.ok, batch.response.error)
      assertEquals(4, batch.response.results.size)
      assertEquals(4, batch.frames.size)

      // Verify cover.png purple pixels (#5B4CEE) rendered onto home screen rather than gray
      // fallback.
      val homeFrame = batch.frames.first { it.overlayId == null }
      var sawPurple = false
      val bytes = homeFrame.payload
      var i = 0
      while (i + 3 < bytes.size) {
        val r = bytes[i].toInt() and 0xFF
        val g = bytes[i + 1].toInt() and 0xFF
        val b = bytes[i + 2].toInt() and 0xFF
        if (
          kotlin.math.abs(r - 0x5B) <= 4 &&
            kotlin.math.abs(g - 0x4C) <= 4 &&
            kotlin.math.abs(b - 0xEE) <= 4
        ) {
          sawPurple = true
          break
        }
        i += 4
      }
      assertTrue(sawPurple, "Expected sample cover.png purple pixels in home frame")

      // Verify dialog, sheet, and popup each have overlay nodes with in-view bounds.
      val expectedTextByOverlay =
        mapOf(
          "dialog" to "Delete playlist?",
          "sheet" to "Share Queue",
          "popup" to "Rename",
        )
      for ((ovlId, needle) in expectedTextByOverlay) {
        val res = batch.response.results.first { it.overlayId == ovlId }
        val matchingNodes = res.tree.nodes.filter { node -> needle in node.text?.value.orEmpty() }
        assertTrue(
          matchingNodes.isNotEmpty(),
          "Expected node containing '$needle' in overlay '$ovlId'",
        )
        for (node in matchingNodes) {
          assertTrue(node.bounds.x >= 0f && node.bounds.y >= 0f, "Out of bounds: $node")
          assertTrue(
            node.bounds.x + node.bounds.width <= Views.Light.width.toFloat() &&
              node.bounds.y + node.bounds.height <= Views.Light.height.toFloat(),
            "Exceeds view bounds: $node",
          )
        }
      }
    }
  }

  private fun writeSampleCoverPng(dir: Path) {
    val width = 16
    val height = 16
    val raw = ByteArray((1 + width * 4) * height)
    var pos = 0
    for (y in 0 until height) {
      raw[pos++] = 0
      for (x in 0 until width) {
        raw[pos++] = 0x5B.toByte()
        raw[pos++] = 0x4C.toByte()
        raw[pos++] = 0xEE.toByte()
        raw[pos++] = 0xFF.toByte()
      }
    }
    val deflater = Deflater(Deflater.BEST_SPEED)
    val deflated =
      ByteArrayOutputStream().use { baos ->
        deflater.setInput(raw)
        deflater.finish()
        val buf = ByteArray(1024)
        while (!deflater.finished()) {
          val n = deflater.deflate(buf)
          if (n > 0) baos.write(buf, 0, n)
        }
        deflater.end()
        baos.toByteArray()
      }
    val out = ByteArrayOutputStream()
    out.write(byteArrayOf(137.toByte(), 80, 78, 71, 13, 10, 26, 10))
    fun chunk(type: String, payload: ByteArray) {
      out.write(ByteBuffer.allocate(4).putInt(payload.size).array())
      val typeBytes = type.toByteArray(Charsets.US_ASCII)
      out.write(typeBytes)
      out.write(payload)
      val crc = CRC32()
      crc.update(typeBytes)
      crc.update(payload)
      out.write(ByteBuffer.allocate(4).putInt(crc.value.toInt()).array())
    }
    chunk(
      "IHDR",
      ByteBuffer.allocate(13)
        .putInt(width)
        .putInt(height)
        .put(8)
        .put(6)
        .put(0)
        .put(0)
        .put(0)
        .array(),
    )
    chunk("IDAT", deflated)
    chunk("IEND", ByteArray(0))
    val asset = dir.resolve("assets/cover.png")
    Files.createDirectories(asset.parent)
    Files.write(asset, out.toByteArray())
  }

  companion object {
    private val PHONE_EXPLORE_SOURCE: String =
      """
      import androidx.compose.foundation.Image
      import androidx.compose.foundation.layout.Arrangement
      import androidx.compose.foundation.layout.Box
      import androidx.compose.foundation.layout.Column
      import androidx.compose.foundation.layout.fillMaxSize
      import androidx.compose.foundation.layout.fillMaxWidth
      import androidx.compose.foundation.layout.height
      import androidx.compose.foundation.layout.padding
      import androidx.compose.foundation.layout.size
      import androidx.compose.foundation.layout.widthIn
      import androidx.compose.material3.AlertDialog
      import androidx.compose.material3.Button
      import androidx.compose.material3.Card
      import androidx.compose.material3.DropdownMenu
      import androidx.compose.material3.DropdownMenuItem
      import androidx.compose.material3.ExperimentalMaterial3Api
      import androidx.compose.material3.FilterChip
      import androidx.compose.material3.FloatingActionButton
      import androidx.compose.material3.ListItem
      import androidx.compose.material3.MaterialTheme
      import androidx.compose.material3.ModalBottomSheet
      import androidx.compose.material3.NavigationBar
      import androidx.compose.material3.NavigationBarItem
      import androidx.compose.material3.OutlinedTextField
      import androidx.compose.material3.Scaffold
      import androidx.compose.material3.Text
      import androidx.compose.material3.TextButton
      import androidx.compose.material3.TopAppBar
      import androidx.compose.material3.rememberModalBottomSheetState
      import androidx.compose.ui.Modifier
      import androidx.compose.ui.platform.testTag
      import androidx.compose.ui.unit.dp
      import bot.charlies.androiddesign.preview.kit.AD_PRIMARY_TAG
      import bot.charlies.androiddesign.preview.kit.BundledFont
      import bot.charlies.androiddesign.preview.kit.PaletteVariant
      import bot.charlies.androiddesign.preview.kit.SymbolIcon
      import bot.charlies.androiddesign.preview.kit.designBoard
      import bot.charlies.androiddesign.preview.kit.designPainter

      @OptIn(ExperimentalMaterial3Api::class)
      val preview = designBoard(
        name = "Music Discovery",
        seed = 0x5B4CEEL,
        variant = PaletteVariant.Expressive,
        brandFont = BundledFont.Fraunces,
      ) {
        direction("Editorial") {
          screen("home") {
            Scaffold(
              topBar = { TopAppBar(title = { Text("Editorial Mix") }) },
              bottomBar = {
                NavigationBar {
                  NavigationBarItem(selected = true, onClick = {}, icon = { SymbolIcon("home") }, label = { Text("Home") })
                  NavigationBarItem(selected = false, onClick = {}, icon = { SymbolIcon("library_music") }, label = { Text("Library") })
                }
              },
              floatingActionButton = { FloatingActionButton(onClick = {}) { SymbolIcon("add") } },
            ) { innerPadding ->
              Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp).widthIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                  ListItem(
                    headlineContent = { Text("Late Night Mix") },
                    supportingContent = { Text("32 tracks") },
                    leadingContent = {
                      Image(
                        painter = designPainter("assets/cover.png"),
                        contentDescription = "Cover",
                        modifier = Modifier.size(40.dp),
                      )
                    },
                    trailingContent = { FilterChip(selected = true, onClick = {}, label = { Text("Live") }) },
                  )
                }
                OutlinedTextField(value = "Search", onValueChange = {}, label = { Text("Filter") }, modifier = Modifier.fillMaxWidth())
                Button(
                  onClick = {},
                  modifier = Modifier.fillMaxWidth().height(56.dp).testTag(AD_PRIMARY_TAG),
                ) { Text("Play Queue") }
              }
            }
          }
          overlay("dialog", screen = "home") {
            AlertDialog(
              onDismissRequest = {},
              confirmButton = { TextButton(onClick = {}) { Text("Delete") } },
              title = { Text("Delete playlist?") },
            )
          }
          overlay("sheet", screen = "home") {
            ModalBottomSheet(
              onDismissRequest = {},
              sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            ) {
              Column(Modifier.fillMaxWidth().padding(24.dp)) {
                Text("Share Queue", style = MaterialTheme.typography.titleLarge)
              }
            }
          }
          overlay("popup", screen = "home") {
            Box(Modifier.fillMaxSize().padding(top = 64.dp, start = 24.dp)) {
              DropdownMenu(expanded = true, onDismissRequest = {}) {
                DropdownMenuItem(text = { Text("Rename") }, onClick = {})
              }
            }
          }
        }
      }
      """
        .trimIndent()

    private val WEAR_EXPLORE_SOURCE: String =
      """
      import androidx.compose.foundation.layout.Box
      import androidx.compose.foundation.layout.fillMaxSize
      import androidx.compose.foundation.layout.fillMaxWidth
      import androidx.compose.foundation.layout.padding
      import androidx.compose.runtime.Composable
      import androidx.compose.ui.Alignment
      import androidx.compose.ui.Modifier
      import androidx.compose.ui.platform.testTag
      import androidx.compose.ui.unit.dp
      import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
      import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
      import androidx.wear.compose.material3.AlertDialog
      import androidx.wear.compose.material3.AlertDialogDefaults
      import androidx.wear.compose.material3.AppScaffold
      import androidx.wear.compose.material3.Button
      import androidx.wear.compose.material3.DatePicker
      import androidx.wear.compose.material3.EdgeButton
      import androidx.wear.compose.material3.FilledTonalButton
      import androidx.wear.compose.material3.ListHeader
      import androidx.wear.compose.material3.ScreenScaffold
      import androidx.wear.compose.material3.SurfaceTransformation
      import androidx.wear.compose.material3.Text
      import androidx.wear.compose.material3.TimeSource
      import androidx.wear.compose.material3.TimeText
      import androidx.wear.compose.material3.TitleCard
      import androidx.wear.compose.material3.curveToEdge
      import androidx.wear.compose.material3.lazy.rememberTransformationSpec
      import androidx.wear.compose.material3.lazy.transformedHeight
      import bot.charlies.androiddesign.preview.kit.AD_PRIMARY_TAG
      import bot.charlies.androiddesign.preview.kit.wearBoard
      import java.time.LocalDate

      val preview = wearBoard("Wear Fitness", seed = 0x00897BL) {
        screen("main") {
          val state = rememberTransformingLazyColumnState()
          AppScaffold(
            timeText = {
              TimeText(
                timeSource = object : TimeSource {
                  @Composable override fun currentTime() = "10:09"
                },
              )
            },
          ) {
            ScreenScaffold(
              scrollState = state,
              edgeButton = { EdgeButton(onClick = {}, modifier = Modifier.testTag(AD_PRIMARY_TAG)) { Text("Start") } },
            ) { contentPadding ->
              val spec = rememberTransformationSpec()
              TransformingLazyColumn(state = state, contentPadding = contentPadding) {
                item { ListHeader(Modifier.fillMaxWidth().transformedHeight(this, spec)) { Text("Runs") } }
                item {
                  Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                    transformation = SurfaceTransformation(spec),
                    label = { Text("Outdoor Run") },
                    secondaryLabel = { Text("5.2 km") },
                  )
                }
                item {
                  TitleCard(
                    onClick = {},
                    title = { Text("Weekly Goal") },
                    time = { Text("Now") },
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                    transformation = SurfaceTransformation(spec),
                  ) { Text("3 of 5 done") }
                }
                item {
                  FilledTonalButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                    transformation = SurfaceTransformation(spec),
                    label = { Text("Intervals") },
                  )
                }
              }
            }
          }
        }
        screen("curved") {
          Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Text("GPS Locked", modifier = Modifier.padding(top = 12.dp).curveToEdge())
          }
        }
        screen("date") {
          DatePicker(initialDate = LocalDate.of(2026, 11, 22), onDatePicked = {})
        }
        overlay("confirm", screen = "main") {
          AlertDialog(
            visible = true,
            onDismissRequest = {},
            confirmButton = { AlertDialogDefaults.ConfirmButton(onClick = {}) },
            title = { Text("Save run?") },
          )
        }
      }
      """
        .trimIndent()
  }
}
