package bot.charlies.androiddesign.preview.feature.preview.sandbox

import android.content.res.Configuration
import androidx.compose.material3.Text
import bot.charlies.androiddesign.preview.host.SandboxExcludingRobolectricTestRunner
import bot.charlies.androiddesign.preview.kit.designBoard
import bot.charlies.androiddesign.preview.kit.wearBoard
import bot.charlies.androiddesign.preview.model.DirectionMeta
import bot.charlies.androiddesign.preview.model.OverlayMeta
import bot.charlies.androiddesign.preview.model.PhoneSurface
import bot.charlies.androiddesign.preview.model.Views
import bot.charlies.androiddesign.preview.model.WearSurface
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(SandboxExcludingRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class ComposeBoardRendererTest {

  @Test
  fun preparedRenderTargetScalesDimensionsPinsFontScaleAndScopesDarkOverrideToPhone() {
    val phoneBoard =
      designBoard("PhoneTest") {
        direction("Editorial") { screen("home") { Text("Hi") } }
      }
    val phonePrepared =
      PreparedRenderTarget.from(
        RenderTargetRequest(
          board = phoneBoard,
          direction = phoneBoard.directions.single(),
          screenId = "home",
          view = Views.Light,
          sourceFile = "Phone.explore.kt",
          sourceText = "",
          density = 2f,
          darkOverride = true,
        ),
      )
    assertTrue(phonePrepared.effectiveView.dark)
    assertEquals(Views.Light.width * 2, phonePrepared.widthPx)
    assertEquals(Views.Light.height * 2, phonePrepared.heightPx)
    assertEquals(320, phonePrepared.dpi)
    assertEquals(2, phonePrepared.perimeterPx)

    val (_, phoneCfg) = phonePrepared.createOverrideContext(RuntimeEnvironment.getApplication())
    assertEquals(Views.Light.width, phoneCfg.screenWidthDp)
    assertEquals(Views.Light.height, phoneCfg.screenHeightDp)
    assertEquals(1f, phoneCfg.fontScale, 0.001f)
    assertEquals(
      Configuration.UI_MODE_NIGHT_YES,
      phoneCfg.uiMode and Configuration.UI_MODE_NIGHT_MASK,
    )
    assertEquals(
      Configuration.SCREENLAYOUT_ROUND_NO,
      phoneCfg.screenLayout and Configuration.SCREENLAYOUT_ROUND_MASK,
    )

    val wearBoard =
      wearBoard("WearTest") {
        screen("main") { androidx.wear.compose.material3.Text("Watch") }
      }
    val wearPrepared =
      PreparedRenderTarget.from(
        RenderTargetRequest(
          board = wearBoard,
          direction = wearBoard.directions.single(),
          screenId = "main",
          view = Views.Watch,
          sourceFile = "Wear.explore.kt",
          sourceText = "",
          density = 1.5f,
          darkOverride = false,
        ),
      )
    assertTrue("Wear view must ignore light darkOverride", wearPrepared.effectiveView.dark)
    assertFalse(phonePrepared.effectiveView.round)
    assertTrue(wearPrepared.effectiveView.round)
    assertEquals((Views.Watch.width * 1.5f).toInt(), wearPrepared.widthPx)
    assertEquals((Views.Watch.width * 1.5f * 0.6f), wearPrepared.minScrollerWidthPx, 0.01f)
    assertEquals(Views.Watch.height * 1.5f / 2f, wearPrepared.scrollStepPx, 0.01f)

    val (_, wearCfg) = wearPrepared.createOverrideContext(RuntimeEnvironment.getApplication())
    assertEquals(
      Configuration.UI_MODE_NIGHT_YES,
      wearCfg.uiMode and Configuration.UI_MODE_NIGHT_MASK,
    )
    assertEquals(
      Configuration.SCREENLAYOUT_ROUND_YES,
      wearCfg.screenLayout and Configuration.SCREENLAYOUT_ROUND_MASK,
    )
  }

  @Test
  fun buildBoardMetaMapsDirectionsScreensOverlaysAndViews() {
    val board =
      designBoard("Plates") {
        direction("Main") {
          screen("home") { Text("Home") }
          screen("detail") { Text("Detail") }
          overlay("confirm", screen = "home") { Text("Confirm") }
        }
      }
    val meta = ComposeBoardRenderer.buildBoardMeta(board)
    assertEquals("phone", meta.surface)
    assertEquals("Plates", meta.boardName)
    assertEquals(PhoneSurface.views, meta.views)
    assertEquals(
      listOf(
        DirectionMeta(
          name = "Main",
          surface = "phone",
          startScreen = "home",
          screens = listOf("home", "detail"),
          overlays = listOf(OverlayMeta(id = "confirm", screen = "home")),
        ),
      ),
      meta.directions,
    )

    val wear = wearBoard("Watch") { screen("main") { androidx.wear.compose.material3.Text("W") } }
    val wearMeta = ComposeBoardRenderer.buildBoardMeta(wear)
    assertEquals("wear", wearMeta.surface)
    assertEquals(WearSurface.views, wearMeta.views)
  }
}
