package wear

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.ScrollWheel
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.unit.Density
import androidx.wear.compose.WearDevice
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.AlertDialogDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import java.io.File
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

private const val DENSITY = 2f
private const val SIZE_DP = 227

/**
 * Renders Wear content in [WearDevice] at 227 dp, density 2, and writes PNGs under build/renders.
 */
@OptIn(ExperimentalTestApi::class)
class WearRenderTest {
  private fun deviceTest(block: SkikoComposeUiTest.() -> Unit) {
    val px = (SIZE_DP * DENSITY).toInt()
    DesktopComposeUiTest(
        width = px,
        height = px,
        density = Density(DENSITY),
        useStandardTestDispatcherForComposition = false,
      )
      .runTest(block)
  }

  private fun SkikoComposeUiTest.settle() {
    waitForIdle()
    mainClock.advanceTimeBy(1500)
    waitForIdle()
  }

  private fun SkikoComposeUiTest.capture(name: String): ImageBitmap {
    val image = captureToImage()
    val bytes =
      Image.makeFromBitmap(image.asSkiaBitmap()).use {
        it.encodeToData(EncodedImageFormat.PNG)!!.bytes
      }
    val dir = File(System.getProperty("renders.dir")).apply { mkdirs() }
    File(dir, "$name.png").writeBytes(bytes)
    return image
  }

  private fun ImageBitmap.pixels(): IntArray = IntArray(width * height).also { readPixels(it) }

  @Test
  fun spikeScreenRendersAndWheelScrollsIt() = deviceTest {
    lateinit var scrolled: () -> Int
    setContent {
      val state = rememberTransformingLazyColumnState()
      scrolled = { state.anchorItemIndex * 10_000 + state.anchorItemScrollOffset }
      WearDevice(SIZE_DP, SIZE_DP, round = true) { SpikeScreen(state) }
    }
    mainClock.autoAdvance = false
    settle()
    val top = capture("spike-top")
    assertTrue(top.pixels().distinct().size > 50, "screen is mostly empty")
    val before = scrolled()

    onRoot().performMouseInput {
      moveTo(center)
      scroll(4f, ScrollWheel.Vertical)
    }
    settle()
    val moved = capture("spike-scrolled")
    assertNotEquals(before, scrolled(), "wheel did not scroll the list")
    assertTrue(!top.pixels().contentEquals(moved.pixels()), "scrolling changed no pixels")
  }

  @Test
  fun alertDialogRenders() = deviceTest {
    setContent {
      MaterialTheme {
        var visible by remember { mutableStateOf(true) }
        WearDevice(SIZE_DP, SIZE_DP, round = true) {
          AlertDialog(
            visible = visible,
            onDismissRequest = { visible = false },
            title = { Text("Delete workout?") },
            text = { Text("This cannot be undone.") },
            confirmButton = { AlertDialogDefaults.ConfirmButton(onClick = {}) },
            dismissButton = { AlertDialogDefaults.DismissButton(onClick = { visible = false }) },
          )
        }
      }
    }
    mainClock.autoAdvance = false
    settle()
    val dialog = capture("alert-dialog")
    assertTrue(dialog.pixels().distinct().size > 20, "dialog is mostly empty")
  }
}
