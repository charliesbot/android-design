package bot.charlies.androiddesign.preview.feature.preview.sandbox

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.material3.Text
import androidx.compose.ui.geometry.Offset
import bot.charlies.androiddesign.preview.host.robolectric.SandboxExcludingRobolectricTestRunner
import bot.charlies.androiddesign.preview.kit.designBoard
import bot.charlies.androiddesign.preview.kit.wearBoard
import bot.charlies.androiddesign.preview.model.DeviceDirectionMeta
import bot.charlies.androiddesign.preview.model.DeviceOverlayMeta
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
  fun preparedRenderTargetScalesDimensionsClampsFontScaleAndScopesDarkOverrideToPhone() {
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
          fontScale = 0.25f,
          darkOverride = true,
        ),
      )
    assertTrue(phonePrepared.effectiveView.dark)
    assertEquals(0.5f, phonePrepared.effectiveFontScale, 0.001f)
    assertEquals(Views.Light.width * 2, phonePrepared.widthPx)
    assertEquals(Views.Light.height * 2, phonePrepared.heightPx)
    assertEquals(320, phonePrepared.dpi)
    assertEquals(2, phonePrepared.perimeterPx)

    val (_, phoneCfg) = phonePrepared.createOverrideContext(RuntimeEnvironment.getApplication())
    assertEquals(Views.Light.width, phoneCfg.screenWidthDp)
    assertEquals(Views.Light.height, phoneCfg.screenHeightDp)
    assertEquals(0.5f, phoneCfg.fontScale, 0.001f)
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
          fontScale = 1.2f,
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
        DeviceDirectionMeta(
          name = "Main",
          surface = "phone",
          startScreen = "home",
          screens = listOf("home", "detail"),
          overlays = listOf(DeviceOverlayMeta(id = "confirm", screen = "home")),
        ),
      ),
      meta.directions,
    )

    val wear = wearBoard("Watch") { screen("main") { androidx.wear.compose.material3.Text("W") } }
    val wearMeta = ComposeBoardRenderer.buildBoardMeta(wear)
    assertEquals("wear", wearMeta.surface)
    assertEquals(WearSurface.views, wearMeta.views)
  }

  @Test
  fun drawWindowCompositeAppliesDimBehindScrimAndRecordsOverlayOffsets() {
    val ctx = RuntimeEnvironment.getApplication()
    val widthPx = 100
    val heightPx = 100
    val mainView =
      View(ctx).apply {
        setBackgroundColor(Color.GREEN)
        measure(
          View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
          View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY),
        )
        layout(0, 0, widthPx, heightPx)
      }
    val overlayView =
      FrameLayout(ctx).apply {
        setBackgroundColor(Color.WHITE)
        layoutParams =
          WindowManager.LayoutParams().apply {
            width = 40
            height = 20
            gravity = Gravity.CENTER
            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 0.5f
          }
      }

    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    try {
      val offsets =
        OverlayWindowHelper.drawWindowComposite(
          canvas = Canvas(bitmap),
          mainView = mainView,
          mainDecorView = mainView,
          overlays = listOf(overlayView),
          widthPx = widthPx,
          heightPx = heightPx,
        )
      assertEquals(Offset(30f, 40f), offsets[overlayView])
      val backgroundPixel = bitmap.getPixel(5, 5)
      assertTrue(
        "Dim scrim should darken green background below 255 green channel",
        Color.green(backgroundPixel) in 100..150 && Color.red(backgroundPixel) == 0,
      )
      assertEquals(Color.WHITE, bitmap.getPixel(50, 50))
    } finally {
      bitmap.recycle()
    }
  }
}
