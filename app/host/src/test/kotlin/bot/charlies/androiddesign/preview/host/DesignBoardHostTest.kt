package bot.charlies.androiddesign.preview.host

import androidx.compose.material3.Text
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import bot.charlies.androiddesign.preview.domain.findings.Contrast
import bot.charlies.androiddesign.preview.feature.preview.sandbox.CallNameMapper
import bot.charlies.androiddesign.preview.feature.preview.sandbox.LutPixelContrastSampler
import bot.charlies.androiddesign.preview.feature.preview.sandbox.RawRgbaFrame
import bot.charlies.androiddesign.preview.kit.BoardSurface
import bot.charlies.androiddesign.preview.kit.designBoard
import bot.charlies.androiddesign.preview.kit.resolveConfinedAsset
import bot.charlies.androiddesign.preview.model.BundledFont
import bot.charlies.androiddesign.preview.model.M3Component
import bot.charlies.androiddesign.preview.model.WearComponent
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DesignBoardHostTest {
  @get:Rule val tempFolder = TemporaryFolder()

  @Test
  fun directionSpecStoresPlaceholderDirectlyPerInstance() {
    val b1 =
      designBoard("A") {
        listDetail(
          list = { Text("List1") },
          detail = { Text("Det1") },
          placeholder = { Text("P1") },
        )
      }
    val b2 =
      designBoard("B") {
        listDetail(
          list = { Text("List2") },
          detail = { Text("Det2") },
          placeholder = { Text("P2") },
        )
      }
    val b3 =
      designBoard("C") {
        listDetail(list = { Text("List3") }, detail = { Text("Det3") })
      }
    assertNotNull(b1.directions.single().placeholder)
    assertNotNull(b2.directions.single().placeholder)
    assertNotSame(b1.directions.single().placeholder, b2.directions.single().placeholder)
    assertEquals(null, b3.directions.single().placeholder)
  }

  @Test
  fun designAssetsConfinementRejectsTraversalAndSchemes() {
    val base = tempFolder.newFolder("explore").toPath()
    val expected = base.resolve("assets/hero.png").normalize()
    assertEquals(expected, resolveConfinedAsset(base, "assets/hero.png"))

    val illegalInputs =
      listOf("", "   ", "../secret.png", "/etc/passwd", "file:hero.png", "a\\b.png")
    for (bad in illegalInputs) {
      val failed = runCatching { resolveConfinedAsset(base, bad) }.isFailure
      assertTrue("Expected rejection for '$bad'", failed)
    }
    assertTrue(Files.isDirectory(base))
  }

  @Test
  fun lutPixelContrastSamplerMatchesWcagContrastFormula() {
    val w = 32
    val h = 32
    val rgba = ByteArray(w * h * 4)
    // Paint background #1C1B1F across entire frame.
    val bgR = 0x1C
    val bgG = 0x1B
    val bgB = 0x1F
    for (i in 0 until w * h) {
      rgba[i * 4] = bgR.toByte()
      rgba[i * 4 + 1] = bgG.toByte()
      rgba[i * 4 + 2] = bgB.toByte()
      rgba[i * 4 + 3] = 0xFF.toByte()
    }
    val frame = RawRgbaFrame(w, h, rgba)
    val lineRect = Rect(8f, 8f, 24f, 18f)
    val sample =
      LutPixelContrastSampler.sample(
        frame = frame,
        fgColor = Color.White,
        lineRectsPx = listOf(lineRect),
        perimeterPx = 2,
      )
    assertNotNull(sample)
    assertEquals("#FFFFFF", sample!!.fgHex)
    assertEquals("#1C1B1F", sample.bgHex)
    val expectedRatio = Contrast.ratio("#FFFFFF", "#1C1B1F")!!
    assertEquals(expectedRatio, sample.ratio, 0.01)
  }

  @Test
  fun callNameMapperCoversPhoneAndWearComponents() {
    assertEquals(
      M3Component.Button.wire,
      CallNameMapper.map("FilledTonalButton", BoardSurface.Phone),
    )
    assertEquals(
      M3Component.BottomSheet.wire,
      CallNameMapper.map("ModalBottomSheet", BoardSurface.Phone),
    )
    assertEquals(WearComponent.Screen.wire, CallNameMapper.map("ScreenScaffold", BoardSurface.Wear))
    assertEquals(WearComponent.EdgeButton.wire, CallNameMapper.map("EdgeButton", BoardSurface.Wear))
    assertEquals("DatePicker", CallNameMapper.map("DatePicker", BoardSurface.Wear))
  }

  @Test
  fun brandFontSetsItsStartingVoiceNotRegularWeight() {
    val board =
      designBoard("Voice", seed = 0xFFC2452DL, brandFont = BundledFont.Fraunces) {
        screen { Text("Hi") }
      }
    val typography = board.toThemeOverride()!!.typography
    val display = typography.getValue("display-large")
    assertEquals("Fraunces", display.fontFamily)
    assertEquals(600, display.fontWeight)
    assertEquals("'SOFT' 100, 'WONK' 1", display.fontVariation)
    assertEquals(null, typography["body-large"])
  }
}
