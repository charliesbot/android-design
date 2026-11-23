package bot.charlies.androiddesign.preview.host.robolectric

import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalFonts
import bot.charlies.androiddesign.preview.host.HostTestFixtures
import bot.charlies.androiddesign.preview.kit.BoardContent
import bot.charlies.androiddesign.preview.kit.designBoard
import bot.charlies.androiddesign.preview.model.RobolectricRenderRequest
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
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
}
