package wear

import androidx.compose.foundation.background
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import kotlin.test.Test
import kotlin.test.assertTrue

/** The "roboto-flex" device font keeps the weight and width axes the Wear tokens ask for. */
@OptIn(ExperimentalTestApi::class)
class DeviceFontTest {
  private fun widths(variants: List<Pair<FontWeight, FontVariation.Settings>>): List<Int> {
    val result = mutableListOf<Int>()
    runDesktopComposeUiTest {
      setContent { Measure(variants, result) }
      waitForIdle()
    }
    return result
  }

  @Composable
  private fun Measure(
    variants: List<Pair<FontWeight, FontVariation.Settings>>,
    out: MutableList<Int>,
  ) {
    val measurer = rememberTextMeasurer()
    variants.forEach { (weight, settings) ->
      val family =
        Font(DeviceFontFamilyName("roboto-flex"), weight, variationSettings = settings)
          .toFontFamily()
      val style = TextStyle(fontFamily = family, fontSize = 20.sp, fontWeight = weight)
      out += measurer.measure("Workouts and more", style).size.width
    }
  }

  private fun settings(weight: Float, width: Float) =
    FontVariation.Settings(
      FontVariation.Setting("wght", weight),
      FontVariation.Setting("wdth", width),
    )

  @Test
  fun weightAndWidthAxesChangeTheGlyphs() {
    val (regular, bold, wide) =
      widths(
        listOf(
          FontWeight(450) to settings(450f, 100f),
          FontWeight(700) to settings(700f, 100f),
          FontWeight(450) to settings(450f, 110f),
        ),
      )
    assertTrue(bold > regular, "wght 700 ($bold) not wider than 450 ($regular)")
    assertTrue(wide > regular, "wdth 110 ($wide) not wider than 100 ($regular)")
  }

  private fun ink(styleWeight: FontWeight): IntArray {
    var pixels = IntArray(0)
    runDesktopComposeUiTest(width = 400, height = 60) {
      setContent {
        val family =
          Font(
              DeviceFontFamilyName("roboto-flex"),
              FontWeight(700),
              variationSettings = settings(700f, 100f),
            )
            .toFontFamily()
        BasicText(
          "Workouts",
          style =
            TextStyle(
              fontFamily = family,
              fontSize = 30.sp,
              fontWeight = styleWeight,
              color = Color.Black,
            ),
          modifier = Modifier.background(Color.White),
        )
      }
      waitForIdle()
      val image = onRoot().captureToImage()
      pixels = IntArray(image.width * image.height).also { image.readPixels(it) }
    }
    return pixels
  }

  @Test
  fun boldIsNotEmboldenedTwice() {
    // The wght axis already makes the glyphs bold, so a bold FontWeight on the text style must not
    // make Compose synthesize bold on top of it.
    assertTrue(ink(FontWeight(700)).contentEquals(ink(FontWeight(400))))
  }
}
