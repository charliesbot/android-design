package app.cash.paparazzi

import android.graphics.Color as AndroidColor
import android.view.View
import android.view.ViewGroup
import android.view.ViewRootImpl_Accessor
import android.view.WindowManagerGlobal
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.blur.BlurRadiusSpec
import androidx.compose.ui.graphics.blur.BlurStop
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density as ComposeDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button as WearButton
import androidx.wear.compose.material3.MaterialTheme as WearMaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text as WearText
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.feature.preview.sandbox.SettlePolicy
import bot.charlies.androiddesign.preview.feature.preview.theme.FontFace
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalFonts
import bot.charlies.androiddesign.preview.render.LayoutlibEnvironment
import com.android.resources.Density
import com.android.resources.NightMode
import com.android.resources.ScreenOrientation
import com.android.resources.ScreenRound
import com.android.resources.ScreenSize
import com.android.resources.UiMode
import java.awt.image.BufferedImage
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LayoutlibDriverTest {
  companion object {
    private val sdk: PaparazziSdk by lazy { LayoutlibEnvironment.open(BootstrapMeta.locate()) }

    private val navOwner =
      object : NavigationEventDispatcherOwner {
        override val navigationEventDispatcher = NavigationEventDispatcher()
      }
  }

  private fun desktopConfig(): DeviceConfig =
    DeviceConfig(
      screenWidth = 3840,
      screenHeight = 2160,
      xdpi = 320,
      ydpi = 320,
      density = Density.XHIGH,
      nightMode = NightMode.NOTNIGHT,
      size = ScreenSize.XLARGE,
      orientation = ScreenOrientation.LANDSCAPE,
      softButtons = false,
    )

  private fun phoneConfig(widthPx: Int, heightPx: Int): DeviceConfig =
    DeviceConfig(
      screenWidth = widthPx,
      screenHeight = heightPx,
      xdpi = 320,
      ydpi = 320,
      density = Density.XHIGH,
      nightMode = NightMode.NOTNIGHT,
      size = ScreenSize.NORMAL,
      softButtons = false,
    )

  private fun renderSettled(
    config: DeviceConfig,
    density: Float = 2f,
    isWear: Boolean = false,
    onBeforeClose: ((List<View>) -> Unit)? = null,
    content: @Composable () -> Unit,
  ): BufferedImage {
    sdk.resetSession(config)
    val view =
      ComposeView(sdk.context).apply {
        layoutParams =
          ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
          )
        setContent {
          CompositionLocalProvider(
            LocalDensity provides ComposeDensity(density, 1f),
            LocalNavigationEventDispatcherOwner provides navOwner,
          ) {
            content()
          }
        }
      }

    return sdk.openSession(view).use { session ->
      var hadOverlay = false
      var idle = 0
      var lastTime = 0L
      for (step in 0 until SettlePolicy.MAX_SETTLE_FRAMES) {
        val nowNanos = step * SettlePolicy.FRAME_NS
        lastTime = nowNanos
        val status = session.advanceFrame(nowNanos)
        val secondary =
          if (isWear) emptyList() else WindowManagerGlobal.getInstance().windowViews.drop(1)
        if (secondary.isNotEmpty()) hadOverlay = true
        val awaitingAttach = secondary.any { !it.isAttachedToWindow || it.width == 0 }
        val minSteps = if (hadOverlay) SettlePolicy.MIN_OVERLAY_STEPS else SettlePolicy.MIN_STEPS
        if (!status.hasPendingWork && !awaitingAttach && step >= minSteps) {
          idle++
          if (idle >= SettlePolicy.IDLE_STREAK) break
        } else {
          idle = 0
        }
      }
      val image = session.captureFrame(lastTime)
      onBeforeClose?.invoke(WindowManagerGlobal.getInstance().windowViews)
      image
    }
  }

  @Test
  fun `Build VERSION SDK_INT is 37 inside composition`() {
    var observedSdkInt = -1
    renderSettled(phoneConfig(200, 200)) {
      observedSdkInt = Class.forName("android.os.Build\$VERSION").getField("SDK_INT").getInt(null)
      Box(Modifier.fillMaxSize().background(Color.White))
    }
    assertEquals(37, observedSdkInt)
  }

  @Test
  fun `FAB renders elevation shadow below its bounds`() {
    var fabCoords: LayoutCoordinates? = null
    var capturedBounds: Rect? = null
    val image =
      renderSettled(
        phoneConfig(320, 320),
        onBeforeClose = { capturedBounds = fabCoords?.boundsInRoot() },
      ) {
        MaterialTheme {
          Box(Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
            FloatingActionButton(
              onClick = {},
              elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 12.dp),
              modifier = Modifier.onGloballyPositioned { fabCoords = it },
            ) {
              Text("+")
            }
          }
        }
      }

    val bounds = checkNotNull(capturedBounds)
    val left = bounds.left.roundToInt().coerceIn(0, image.width - 1)
    val right = bounds.right.roundToInt().coerceIn(left + 1, image.width)
    val yStart = (bounds.bottom.roundToInt() + 2).coerceIn(0, image.height - 1)
    val yEnd = (bounds.bottom.roundToInt() + 12).coerceIn(yStart + 1, image.height)

    var nonWhitePixels = 0
    for (y in yStart until yEnd) {
      for (x in left until right) {
        if (image.getRGB(x, y) != AndroidColor.WHITE) {
          nonWhitePixels++
        }
      }
    }
    assertTrue(
      nonWhitePixels > 0,
      "Expected elevation shadow pixels below FAB bounds ($bounds), found none",
    )
  }

  @Test
  fun `Modifier blur differs from unblurred control`() {
    @Composable
    fun SampleBox(blurDp: Boolean) {
      Box(Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
        val mod =
          if (blurDp) {
            Modifier.size(40.dp)
              .blur(12.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
              .background(Color.Red)
          } else {
            Modifier.size(40.dp).background(Color.Red)
          }
        Box(mod)
      }
    }

    val unblurred = renderSettled(phoneConfig(240, 240)) { SampleBox(blurDp = false) }
    val blurred = renderSettled(phoneConfig(240, 240)) { SampleBox(blurDp = true) }

    var diffPixels = 0
    for (y in 0 until unblurred.height) {
      for (x in 0 until unblurred.width) {
        if (unblurred.getRGB(x, y) != blurred.getRGB(x, y)) {
          diffPixels++
        }
      }
    }
    assertTrue(
      diffPixels > 100,
      "Expected blurred control to differ from unblurred ($diffPixels px)",
    )
  }

  @Test
  fun `Google Sans Flex wght 300 and 700 produce different text widths from asset path`() {
    var width300 = -1f
    var width700 = -1f
    renderSettled(phoneConfig(900, 300)) {
      MaterialTheme {
        Column(Modifier.fillMaxSize().background(Color.White)) {
          Text(
            text = "Hamburgefonstiv 0123",
            fontFamily = ProposalFonts.family(FontFace.GoogleSansFlex, 300),
            fontWeight = FontWeight(300),
            fontSize = 32.sp,
            maxLines = 1,
            softWrap = false,
            onTextLayout = { width300 = it.getLineRight(0) },
          )
          Text(
            text = "Hamburgefonstiv 0123",
            fontFamily = ProposalFonts.family(FontFace.GoogleSansFlex, 700),
            fontWeight = FontWeight(700),
            fontSize = 32.sp,
            maxLines = 1,
            softWrap = false,
            onTextLayout = { width700 = it.getLineRight(0) },
          )
        }
      }
    }

    assertTrue(
      width300 > 0f && width700 > 0f,
      "Expected measured widths, got $width300 / $width700",
    )
    assertTrue(
      width700 != width300,
      "Expected wght 700 ($width700) to differ in width from wght 300 ($width300)",
    )
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun `ModalBottomSheet and DropdownMenu appear after settling without focus highlight`() {
    var sheetWindows: List<View> = emptyList()
    val baseImage =
      renderSettled(phoneConfig(600, 800)) {
        MaterialTheme { Box(Modifier.fillMaxSize().background(Color.White)) }
      }
    val sheetImage =
      renderSettled(
        phoneConfig(600, 800),
        onBeforeClose = { sheetWindows = it },
      ) {
        MaterialTheme {
          Box(Modifier.fillMaxSize().background(Color.White)) {
            ModalBottomSheet(
              onDismissRequest = {},
              sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            ) {
              Column(Modifier.fillMaxWidth().padding(24.dp)) {
                Text("Bottom Sheet Title")
              }
            }
          }
        }
      }
    assertTrue(sheetWindows.size >= 2, "Expected secondary window for ModalBottomSheet")
    assertTrue(sheetWindows.all { it.isInTouchMode }, "All windows must be in touch mode")
    var sheetDiffs = 0
    for (y in 450 until sheetImage.height) {
      for (x in 0 until sheetImage.width) {
        if (sheetImage.getRGB(x, y) != baseImage.getRGB(x, y)) sheetDiffs++
      }
    }
    assertTrue(
      sheetDiffs > 1_000,
      "Expected ModalBottomSheet visible in bottom half ($sheetDiffs px)",
    )

    var menuWindows: List<View> = emptyList()
    var row0Coords: LayoutCoordinates? = null
    var row1Coords: LayoutCoordinates? = null
    var row0Bounds: Rect? = null
    var row1Bounds: Rect? = null
    var popupOffsetX = 0
    var popupOffsetY = 0
    val menuImage =
      renderSettled(
        phoneConfig(600, 800),
        onBeforeClose = { wins ->
          menuWindows = wins
          row0Bounds = row0Coords?.boundsInRoot()
          row1Bounds = row1Coords?.boundsInRoot()
          val popupRoot = wins.drop(1).firstOrNull()?.viewRootImpl
          val winFrame = popupRoot?.let { ViewRootImpl_Accessor.getWindowFrame(it) }
          if (winFrame != null) {
            popupOffsetX = winFrame.left
            popupOffsetY = winFrame.top
          }
        },
      ) {
        MaterialTheme {
          Box(Modifier.fillMaxSize().background(Color.White).padding(40.dp)) {
            DropdownMenu(expanded = true, onDismissRequest = {}) {
              DropdownMenuItem(
                text = { Text("Rename") },
                onClick = {},
                modifier = Modifier.onGloballyPositioned { row0Coords = it },
              )
              DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {},
                modifier = Modifier.onGloballyPositioned { row1Coords = it },
              )
            }
          }
        }
      }
    assertTrue(menuWindows.size >= 2, "Expected secondary window for DropdownMenu")
    assertTrue(menuWindows.all { it.isInTouchMode }, "Menu windows must be in touch mode")

    val r0 = checkNotNull(row0Bounds)
    val r1 = checkNotNull(row1Bounds)
    val row0Bg =
      menuImage.getRGB(
        popupOffsetX + (r0.left + 8f).roundToInt(),
        popupOffsetY + (r0.top + r0.height / 2f).roundToInt(),
      )
    val row1Bg =
      menuImage.getRGB(
        popupOffsetX + (r1.left + 8f).roundToInt(),
        popupOffsetY + (r1.top + r1.height / 2f).roundToInt(),
      )
    assertEquals(
      row1Bg,
      row0Bg,
      "Expected first menu item background to match second item (no initial focus highlight)",
    )
    assertTrue(
      row0Bg != AndroidColor.WHITE,
      "Expected DropdownMenu surface color to differ from plain white base",
    )
  }

  @Test
  fun `Wear list with scroll indicator settles and shows the indicator`() {
    val wearConfig =
      DeviceConfig(
        screenWidth = 408,
        screenHeight = 408,
        xdpi = 320,
        ydpi = 320,
        density = Density.XHIGH,
        uiMode = UiMode.WATCH,
        nightMode = NightMode.NIGHT,
        size = ScreenSize.SMALL,
        softButtons = false,
        screenRound = ScreenRound.ROUND,
      )
    val image =
      renderSettled(wearConfig, density = 2f, isWear = true) {
        WearMaterialTheme {
          val state = rememberTransformingLazyColumnState()
          AppScaffold {
            ScreenScaffold(scrollState = state) { contentPadding ->
              TransformingLazyColumn(state = state, contentPadding = contentPadding) {
                items(12) { idx ->
                  WearButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    label = { WearText("Workout $idx") },
                  )
                }
              }
            }
          }
        }
      }

    var indicatorPixels = 0
    for (y in 140..268) {
      for (x in 384..404) {
        val rgb = image.getRGB(x, y) and 0x00FFFFFF
        if (rgb != 0) indicatorPixels++
      }
    }
    assertTrue(
      indicatorPixels > 0,
      "Expected visible Wear scroll indicator pixels along right edge",
    )
  }

  @Test
  fun `draw-phase state updates settle before captureFrame`() {
    val image =
      renderSettled(phoneConfig(120, 120)) {
        var drawReady by remember { mutableStateOf(false) }
        Box(
          Modifier.fillMaxSize().drawBehind {
            drawRect(if (drawReady) Color.Green else Color.Red)
            if (!drawReady) {
              drawReady = true
            }
          },
        )
      }
    assertEquals(AndroidColor.GREEN, image.getRGB(60, 60))
  }

  @Test
  fun `desktop bounded progressive blur settles and renders within warm budget`() {
    val fourStop16 =
      BlurRadiusSpec.verticalGradient(
        stops =
          listOf(
            BlurStop(fraction = 0f, radius = 16.dp),
            BlurStop(fraction = 0.3f, radius = 0.dp),
            BlurStop(fraction = 0.7f, radius = 0.dp),
            BlurStop(fraction = 1f, radius = 16.dp),
          ),
      )

    @Composable
    fun LyricsPane(blurSpec: BlurRadiusSpec?) {
      Box(
        Modifier.fillMaxSize().background(Color(0xFF0E0D11)),
        contentAlignment = Alignment.Center,
      ) {
        val blurMod =
          if (blurSpec != null) {
            Modifier.blur(blurRadiusSpec = blurSpec, edgeTreatment = BlurredEdgeTreatment.Rectangle)
          } else {
            Modifier
          }
        Column(
          modifier =
            Modifier.size(width = 600.dp, height = 800.dp)
              .then(blurMod)
              .background(Color(0xFF141218))
              .padding(24.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          val colors =
            listOf(Color(0xFF381E72), Color(0xFF4F378B), Color(0xFF2B2930), Color(0xFF633B48))
          repeat(10) { idx ->
            Surface(
              color = colors[idx % colors.size],
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text("Verse line ${idx + 1}", color = Color(0xFFE6E0E9))
                Text("03:${10 + idx}", color = Color(0xFFCAC4D0))
              }
            }
          }
        }
      }
    }

    // Warm up layoutlib and Compose on desktop before timing the blurred pass.
    val unblurred = renderSettled(desktopConfig()) { LyricsPane(blurSpec = null) }
    lateinit var blurred: BufferedImage
    val elapsedMs = measureTimeMillis {
      blurred = renderSettled(desktopConfig()) { LyricsPane(blurSpec = fourStop16) }
    }

    assertTrue(
      elapsedMs < 3_500L,
      "Expected bounded desktop progressive blur to settle within 3500ms, took ${elapsedMs}ms",
    )

    var diffPixels = 0
    for (y in 280 until 760) {
      for (x in 1320 until 2520) {
        if (unblurred.getRGB(x, y) != blurred.getRGB(x, y)) {
          diffPixels++
        }
      }
    }
    assertTrue(
      diffPixels > 1_000,
      "Expected progressive blur pixels in top band to differ from unblurred ($diffPixels px)",
    )
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun `ModalBottomSheet over progressive blur settles and renders both sheet and blur`() {
    val baseImage =
      renderSettled(phoneConfig(600, 800)) {
        MaterialTheme { Box(Modifier.fillMaxSize().background(Color.White)) }
      }
    val fourStop16 =
      BlurRadiusSpec.verticalGradient(
        stops =
          listOf(
            BlurStop(fraction = 0f, radius = 16.dp),
            BlurStop(fraction = 0.3f, radius = 0.dp),
            BlurStop(fraction = 0.7f, radius = 0.dp),
            BlurStop(fraction = 1f, radius = 16.dp),
          ),
      )

    var sheetWindows: List<View> = emptyList()
    lateinit var sheetImage: BufferedImage
    val elapsedMs = measureTimeMillis {
      sheetImage =
        renderSettled(
          phoneConfig(600, 800),
          onBeforeClose = { sheetWindows = it },
        ) {
          MaterialTheme {
            Box(Modifier.fillMaxSize().background(Color.White)) {
              Column(
                Modifier.fillMaxSize()
                  .blur(blurRadiusSpec = fourStop16, edgeTreatment = BlurredEdgeTreatment.Rectangle)
                  .background(Color(0xFF141218))
                  .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                repeat(6) { idx -> Text("Background row $idx", color = Color(0xFFE6E0E9)) }
              }
              ModalBottomSheet(
                onDismissRequest = {},
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
              ) {
                Column(Modifier.fillMaxWidth().padding(24.dp)) {
                  Text("Bottom Sheet Title")
                }
              }
            }
          }
        }
    }

    assertTrue(sheetWindows.size >= 2, "Expected secondary window for ModalBottomSheet")
    assertTrue(sheetWindows.all { it.isInTouchMode }, "All windows must be in touch mode")
    var sheetDiffs = 0
    for (y in 450 until sheetImage.height) {
      for (x in 0 until sheetImage.width) {
        if (sheetImage.getRGB(x, y) != baseImage.getRGB(x, y)) sheetDiffs++
      }
    }
    assertTrue(
      sheetDiffs > 1_000,
      "Expected ModalBottomSheet visible in bottom half ($sheetDiffs px)",
    )
    assertTrue(
      elapsedMs < 3_500L,
      "Expected ModalBottomSheet over progressive blur to settle within 3500ms, took ${elapsedMs}ms",
    )
  }
}
