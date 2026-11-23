package bot.charlies.androiddesign.preview.host.robolectric

import android.os.Looper
import android.view.inspector.WindowInspector
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalWindowInfo
import bot.charlies.androiddesign.preview.domain.RenderReport
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalFonts
import bot.charlies.androiddesign.preview.host.HostTestFixtures
import bot.charlies.androiddesign.preview.kit.BoardContent
import bot.charlies.androiddesign.preview.kit.designBoard
import bot.charlies.androiddesign.preview.model.DeviceRenderTarget
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.RobolectricRenderRequest
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(SandboxExcludingRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class RobolectricBoardRendererTest {
  @get:Rule val tempFolder = TemporaryFolder()

  @Test
  fun rendersPhoneOverlaysAssetsVariableFontsAndWearScrollSafely() {
    val workDir = tempFolder.newFolder("fixtures").toPath()
    HostTestFixtures.writeSampleCoverPng(workDir)
    val phoneFile = workDir.resolve(HostTestFixtures.PHONE_EXPLORE_FILE)
    val wearFile = workDir.resolve(HostTestFixtures.WEAR_EXPLORE_FILE)
    Files.writeString(phoneFile, HostTestFixtures.PHONE_EXPLORE_SOURCE)
    Files.writeString(wearFile, HostTestFixtures.WEAR_EXPLORE_SOURCE)
    val outDir = tempFolder.newFolder("out").toPath()

    // 1. Phone main view at 2x with Fraunces brand font + designPainter asset
    val phoneMain =
      RobolectricBoardRenderer.handleRequest(
        RobolectricRenderRequest(
          id = "p-main",
          sourcePath = phoneFile.toString(),
          views = listOf("light"),
          screens = listOf("home"),
          outDir = outDir.toString(),
          density = 2f,
          encodePng = true,
        ),
      )
    assertTrue("Phone main ok & valid: $phoneMain", phoneMain.ok && phoneMain.valid)
    assertEquals("phone", phoneMain.surface)
    assertEquals(1, phoneMain.trees.size)
    val phoneTree = phoneMain.trees.single()
    assertTrue("Expected phone nodes", phoneTree.nodes.size >= 5)
    assertTrue(
      "Expected <file>:<line> sources",
      phoneTree.nodes.any {
        it.sourceFile == HostTestFixtures.PHONE_EXPLORE_FILE && (it.sourceLine ?: 0) > 0
      },
    )
    assertTrue(
      "Expected sampled WCAG colors on text nodes",
      phoneTree.nodes.any { it.colors?.content?.startsWith("#") == true },
    )
    assertEquals(1, phoneMain.writtenFiles.size)
    assertTrue(Files.size(Paths.get(phoneMain.writtenFiles.single())) > 100L)

    // 2. Phone overlays: dialog, sheet, popup
    for (overlayId in listOf("dialog", "sheet", "popup")) {
      val ov =
        RobolectricBoardRenderer.handleRequest(
          RobolectricRenderRequest(
            id = "p-$overlayId",
            sourcePath = phoneFile.toString(),
            views = listOf("light"),
            screens = listOf("home"),
            overlay = overlayId,
            density = 1f,
            encodePng = false,
          ),
        )
      assertTrue("Overlay $overlayId ok & valid: $ov", ov.ok && ov.valid)
      assertEquals(1, ov.trees.size)
      assertTrue("Expected non-empty tree for $overlayId", ov.trees.single().nodes.isNotEmpty())
    }

    // 3. Wear main scroll + endTree + EdgeButton + TimeText
    val wearMain =
      RobolectricBoardRenderer.handleRequest(
        RobolectricRenderRequest(
          id = "w-main",
          sourcePath = wearFile.toString(),
          views = listOf("watch"),
          screens = listOf("main"),
          outDir = outDir.toString(),
          density = 2f,
          encodePng = true,
        ),
      )
    assertTrue("Wear main ok & valid: $wearMain", wearMain.ok && wearMain.valid)
    assertEquals("wear", wearMain.surface)
    assertTrue("Expected auto-scroll endTree on Wear list", wearMain.endTrees.isNotEmpty())
    assertTrue(
      "Expected both main and end PNGs",
      wearMain.writtenFiles.size == 2 && wearMain.writtenFiles.any { it.endsWith("-end.png") },
    )

    // 4. Wear DatePicker must skip infinite circular picker scrolling (< 2s)
    val t0 = System.nanoTime()
    val wearDate =
      RobolectricBoardRenderer.handleRequest(
        RobolectricRenderRequest(
          id = "w-date",
          sourcePath = wearFile.toString(),
          views = listOf("watch"),
          screens = listOf("date"),
          density = 2f,
          encodePng = false,
        ),
      )
    val dateMs = (System.nanoTime() - t0) / 1_000_000L
    assertTrue("Wear date ok & valid: $wearDate", wearDate.ok && wearDate.valid)
    assertTrue(
      "Wear DatePicker must skip infinite scroll quickly (< 2000ms), was ${dateMs}ms",
      dateMs < 2000L,
    )
    assertTrue("DatePicker should not emit endTrees", wearDate.endTrees.isEmpty())

    // 5. Malformed request/file returns structured validation failure without throwing
    val missing =
      RobolectricBoardRenderer.handleRequest(
        RobolectricRenderRequest(
          id = "missing",
          sourcePath = workDir.resolve("Nope.explore.kt").toString(),
        ),
      )
    assertTrue(missing.ok)
    assertFalse(missing.valid)
    assertTrue(missing.errors.isNotEmpty())
  }

  @Test
  fun recomposerExceptionReturnsStructuredFailureInsteadOfCrashing() {
    val workDir = tempFolder.newFolder("recomp-fail").toPath()
    val boomFile = workDir.resolve("Boom.explore.kt")
    Files.writeString(
      boomFile,
      """
      import androidx.compose.material3.Text
      import androidx.compose.runtime.LaunchedEffect
      import androidx.compose.runtime.getValue
      import androidx.compose.runtime.mutableStateOf
      import androidx.compose.runtime.remember
      import androidx.compose.runtime.setValue
      import bot.charlies.androiddesign.preview.kit.designBoard
      val preview = designBoard("Boom") {
        screen("main") {
          var trigger by remember { mutableStateOf(false) }
          LaunchedEffect(Unit) { trigger = true }
          if (trigger) error("boom in recomposition")
          Text("initial")
        }
      }
      """
        .trimIndent(),
    )
    val resp =
      RobolectricBoardRenderer.handleRequest(
        RobolectricRenderRequest(
          id = "recomp-boom",
          sourcePath = boomFile.toString(),
          views = listOf("light"),
          screens = listOf("main"),
          encodePng = false,
        ),
      )
    assertFalse("Expected ok=false when recomposition throws: $resp", resp.ok)
    assertTrue(
      "Expected recomposition error message in errors: ${resp.errors}",
      resp.errors.any { it.message.contains("boom in recomposition") },
    )
  }

  @Test
  fun launchedEffectExceptionReturnsStructuredFailureInsteadOfCrashing() {
    val workDir = tempFolder.newFolder("effect-fail").toPath()
    val effectFile = workDir.resolve("EffectBoom.explore.kt")
    Files.writeString(
      effectFile,
      """
      import androidx.compose.material3.Text
      import androidx.compose.runtime.LaunchedEffect
      import bot.charlies.androiddesign.preview.kit.designBoard
      val preview = designBoard("EffectBoom") {
        screen("main") {
          LaunchedEffect(Unit) { error("boom in launched effect") }
          Text("initial")
        }
      }
      """
        .trimIndent(),
    )
    val resp =
      RobolectricBoardRenderer.handleRequest(
        RobolectricRenderRequest(
          id = "effect-boom",
          sourcePath = effectFile.toString(),
          views = listOf("light"),
          screens = listOf("main"),
          encodePng = false,
        ),
      )
    assertFalse("Expected ok=false when LaunchedEffect throws: $resp", resp.ok)
    assertTrue(
      "Expected effect error message in errors: ${resp.errors}",
      resp.errors.any { it.message.contains("boom in launched effect") },
    )
    assertTrue(
      "Expected failed Activity windows to be detached after error",
      WindowInspector.getGlobalWindowViews().none { it.isAttachedToWindow },
    )

    val validFile = workDir.resolve("ValidAfterFail.explore.kt")
    Files.writeString(
      validFile,
      """
      import androidx.compose.material3.Text
      import bot.charlies.androiddesign.preview.kit.designBoard
      val preview = designBoard("ValidAfterFail") {
        screen("main") { Text("Recovered Cleanly") }
      }
      """
        .trimIndent(),
    )
    val validAfter =
      RobolectricBoardRenderer.handleRequest(
        RobolectricRenderRequest(
          id = "valid-after-effect-boom",
          sourcePath = validFile.toString(),
          views = listOf("light"),
          screens = listOf("main"),
          encodePng = false,
        ),
      )
    assertTrue(
      "Expected subsequent valid request to succeed: $validAfter",
      validAfter.ok && validAfter.valid,
    )
    val nodes = validAfter.trees.single().nodes
    assertTrue(
      "Expected Recovered Cleanly text node",
      nodes.any { it.text?.value == "Recovered Cleanly" },
    )
    assertTrue(
      "Expected no leaked initial node from failed request",
      nodes.none { it.text?.value == "initial" },
    )
  }

  @Test
  fun switchingDirectionsResetsNavBackStackWithoutNoSuchElementException() {
    var detailComposed = false
    var switchedComposed = false
    val board =
      designBoard("DirSwitch") {
        direction("DirA") {
          listDetail(
            id = "list",
            detailId = "detail",
            list = { onOpenDetail ->
              LaunchedEffect(Unit) { onOpenDetail() }
              Text("A List")
            },
            detail = {
              detailComposed = true
              Text("A Detail")
            },
          )
        }
        direction("DirB") {
          screen("list") {
            switchedComposed = true
            Text("B List")
          }
        }
      }
    var activeDir by mutableStateOf(board.directions[0])
    val controller = Robolectric.buildActivity(ComponentActivity::class.java)
    val activity = controller.setup().get()
    ProposalFonts.init(activity.assets)
    val cv = ComposeView(activity)
    cv.setContent {
      BoardContent(
        board = board,
        direction = activeDir,
        screenId = "list",
        frameWidthDp = 360,
        frameHeightDp = 640,
        fillContainer = false,
        dark = false,
        roundClip = false,
        designSystem = board.resolveDesignSystem(),
        explorationDir = null,
      )
    }
    activity.setContentView(cv)
    try {
      repeat(5) { shadowOf(Looper.getMainLooper()).idleFor(16, TimeUnit.MILLISECONDS) }
      assertTrue("Expected DirA to navigate to detail before switching", detailComposed)
      activeDir = board.directions[1]
      repeat(5) { shadowOf(Looper.getMainLooper()).idleFor(16, TimeUnit.MILLISECONDS) }
      assertTrue("Expected DirB list screen to compose after direction switch", switchedComposed)
    } finally {
      controller.pause().stop().destroy()
      shadowOf(Looper.getMainLooper()).idle()
    }
  }

  @Test
  fun pingCompileBatchTargetsAndReportModesWorkEndToEnd() {
    val ping =
      RobolectricBoardRenderer.handleRequest(RobolectricRenderRequest(id = "ping-1", mode = "ping"))
    assertTrue(ping.ok && ping.valid && ping.pass)

    val workDir = tempFolder.newFolder("report-fixtures").toPath()
    HostTestFixtures.writeSampleCoverPng(workDir)
    val phoneFile = workDir.resolve(HostTestFixtures.PHONE_EXPLORE_FILE)
    val wearFile = workDir.resolve(HostTestFixtures.WEAR_EXPLORE_FILE)
    Files.writeString(phoneFile, HostTestFixtures.PHONE_EXPLORE_SOURCE)
    Files.writeString(wearFile, HostTestFixtures.WEAR_EXPLORE_SOURCE)

    // 1. compile mode returns boardMeta + boardTheme and caches unchanged source
    val compileFirst =
      RobolectricBoardRenderer.handleRequest(
        RobolectricRenderRequest(id = "cmp-1", sourcePath = phoneFile.toString(), mode = "compile"),
      )
    assertTrue("Expected compile ok & valid: $compileFirst", compileFirst.ok && compileFirst.valid)
    assertEquals("Editorial", compileFirst.boardMeta?.directions?.single()?.name)
    assertEquals("expressive", compileFirst.boardTheme?.material?.scheme?.variant)

    val compileSecond =
      RobolectricBoardRenderer.handleRequest(
        RobolectricRenderRequest(id = "cmp-2", sourcePath = phoneFile.toString(), mode = "compile"),
      )
    assertTrue(compileSecond.ok && compileSecond.valid)
    assertEquals("Expected cached compile to skip kotlinc", 0L, compileSecond.compileMs)

    // 2. targets batch writes PNG only when frameFormat != NONE
    val batchOut = tempFolder.newFolder("batch-out").toPath()
    val batchResp =
      RobolectricBoardRenderer.handleRequest(
        RobolectricRenderRequest(
          id = "batch-1",
          sourcePath = phoneFile.toString(),
          outDir = batchOut.toString(),
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
                viewName = "dark",
                densityScale = 2f,
                frameFormat = FrameFormat.NONE,
              ),
            ),
        ),
      )
    assertTrue(batchResp.ok && batchResp.valid)
    assertEquals(2, batchResp.results.size)
    assertTrue(
      batchResp.results[0].pngPath != null &&
        Files.isRegularFile(Paths.get(batchResp.results[0].pngPath!!)),
    )
    assertEquals(null, batchResp.results[1].pngPath)

    // 3. report mode writes report.json, overlays, trees, PNGs, and Wear end-of-list artifacts
    val phoneReportOut = tempFolder.newFolder("phone-report").toPath()
    val phoneReportResp =
      RobolectricBoardRenderer.handleRequest(
        RobolectricRenderRequest(
          id = "rep-phone",
          sourcePath = phoneFile.toString(),
          views = listOf("light"),
          screens = listOf("home"),
          outDir = phoneReportOut.toString(),
          mode = "report",
          includeOverlays = true,
        ),
      )
    assertTrue(phoneReportResp.ok && phoneReportResp.valid)
    val phoneReport =
      PreviewJson.compact.decodeFromString(
        RenderReport.serializer(),
        Files.readString(phoneReportOut.resolve("report.json")),
      )
    assertTrue(phoneReport.valid)
    assertEquals("expressive", phoneReport.theme.variant)
    assertEquals(
      listOf("home", "home+dialog", "home+sheet", "home+popup"),
      phoneReport.renders.map { it.screen },
    )
    assertTrue(Files.isRegularFile(phoneReportOut.resolve("home-light.png")))
    assertTrue(Files.isRegularFile(phoneReportOut.resolve("home-light.tree.json")))
    assertTrue(Files.isRegularFile(phoneReportOut.resolve("home+dialog-light.png")))

    val wearReportOut = tempFolder.newFolder("wear-report").toPath()
    val wearReportResp =
      RobolectricBoardRenderer.handleRequest(
        RobolectricRenderRequest(
          id = "rep-wear",
          sourcePath = wearFile.toString(),
          views = listOf("watch"),
          screens = listOf("main"),
          outDir = wearReportOut.toString(),
          mode = "report",
          includeOverlays = true,
        ),
      )
    assertTrue(wearReportResp.ok && wearReportResp.valid)
    val wearReport =
      PreviewJson.compact.decodeFromString(
        RenderReport.serializer(),
        Files.readString(wearReportOut.resolve("report.json")),
      )
    assertEquals(listOf("main", "main+confirm"), wearReport.renders.map { it.screen })
    assertEquals("main-watch-end.png", wearReport.renders[0].endPng)
    assertEquals("main-watch-end.tree.json", wearReport.renders[0].endTree)
    assertTrue(Files.isRegularFile(wearReportOut.resolve("main-watch-end.png")))
    assertTrue(Files.isRegularFile(wearReportOut.resolve("main-watch-end.tree.json")))

    // 4. invalid proposal in report mode writes valid=false report.json
    val brokenFile = workDir.resolve("Broken.explore.kt")
    Files.writeString(brokenFile, "val preview = unresolvedSymbol()\n")
    val brokenOut = tempFolder.newFolder("broken-report").toPath()
    val brokenResp =
      RobolectricBoardRenderer.handleRequest(
        RobolectricRenderRequest(
          id = "rep-broken",
          sourcePath = brokenFile.toString(),
          outDir = brokenOut.toString(),
          mode = "report",
        ),
      )
    assertTrue(brokenResp.ok)
    assertFalse(brokenResp.valid)
    val brokenReport =
      PreviewJson.compact.decodeFromString(
        RenderReport.serializer(),
        Files.readString(brokenOut.resolve("report.json")),
      )
    assertFalse(brokenReport.valid)
    assertTrue(brokenReport.errors.isNotEmpty())
  }

  @Test
  fun localWindowInfoReflectsTargetViewDimensionsInsideListDetail() {
    var observedDpWidth = -1
    var observedDpHeight = -1
    var observedPxWidth = -1
    var observedPxHeight = -1
    val board =
      designBoard("WindowInfoParity") {
        direction("Split") {
          listDetail(
            list = {
              val windowInfo = LocalWindowInfo.current
              observedDpWidth = windowInfo.containerDpSize.width.value.roundToInt()
              observedDpHeight = windowInfo.containerDpSize.height.value.roundToInt()
              observedPxWidth = windowInfo.containerSize.width
              observedPxHeight = windowInfo.containerSize.height
              Text("List ${observedDpWidth}x$observedDpHeight")
            },
            detail = { Text("Detail") },
            placeholder = { Text("Placeholder") },
          )
        }
      }
    val controller = Robolectric.buildActivity(ComponentActivity::class.java)
    val activity = controller.setup().get()
    ProposalFonts.init(activity.assets)
    val density = 2f
    val widthPx = (1280 * density).roundToInt()
    val heightPx = (800 * density).roundToInt()
    val cv = ComposeView(activity)
    cv.setContent {
      androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalDensity provides
          androidx.compose.ui.unit.Density(density, fontScale = 1f),
      ) {
        BoardContent(
          board = board,
          direction = board.directions.single(),
          screenId = "list",
          frameWidthDp = 1280,
          frameHeightDp = 800,
          fillContainer = false,
          dark = false,
          roundClip = false,
          designSystem = board.resolveDesignSystem(),
          explorationDir = null,
        )
      }
    }
    activity.setContentView(cv, android.view.ViewGroup.LayoutParams(widthPx, heightPx))
    try {
      repeat(5) { shadowOf(Looper.getMainLooper()).idleFor(16, TimeUnit.MILLISECONDS) }
      assertEquals(1280, observedDpWidth)
      assertEquals(800, observedDpHeight)
      assertEquals(2560, observedPxWidth)
      assertEquals(1600, observedPxHeight)
    } finally {
      controller.pause().stop().destroy()
      shadowOf(Looper.getMainLooper()).idle()
    }
  }
}
