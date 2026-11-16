package bot.charlies.androiddesign.preview.feature.preview.sandbox

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.Views
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.jetbrains.skia.Bitmap

/**
 * Verifies `PixelContrastSampler` and end-to-end slot/semantics/pixel `FindingRules` evaluation on
 * compiled `.explore.kt` screens (both Material 3 phone and Wear Compose).
 */
class SandboxFindingRulesTest {
  private fun renderSource(
    source: String,
    fileName: String = "rules.explore.kt",
    view: bot.charlies.androiddesign.preview.model.ViewSpec = Views.Light,
  ): bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessResult {
    val dir: Path = createTempDirectory("rules-test-")
    return try {
      val compiled =
        assertIs<CompileResult.Compiled>(ExplorationCompiler.compileSource(fileName, source, dir))
      val direction = compiled.board.directions.single()
      SandboxHeadlessRenderer.render(
        compiled = compiled,
        direction = direction,
        screenId = direction.startScreen,
        view = view,
        designSystem = compiled.board.resolveDesignSystem(),
      )
    } finally {
      dir.toFile().deleteRecursively()
    }
  }

  private fun solidBitmap(w: Int, h: Int, argb: Int): Bitmap =
    Bitmap().apply {
      allocN32Pixels(w, h)
      erase(argb)
    }

  private fun Bitmap.putPixel(x: Int, y: Int, argb: Int) {
    erase(argb, org.jetbrains.skia.IRect.makeXYWH(x, y, 1, 1))
  }

  @Test
  fun `pixel contrast sampler passes dark text on white background`() {
    val bmp = solidBitmap(60, 40, 0xFFFFFFFF.toInt())
    val sample =
      PixelContrastSampler.sample(
        bitmap = bmp,
        fgColor = Color(0xFF1C1B1FL),
        lineRectsPx = listOf(Rect(10f, 10f, 50f, 24f)),
      )
    assertNotNull(sample)
    assertTrue(sample.ratio > 15.0, "Expected >15:1 ratio, got ${sample.ratio}")
  }

  @Test
  fun `pixel contrast sampler flags white on white failure`() {
    val bmp = solidBitmap(60, 40, 0xFFFFFFFF.toInt())
    val sample =
      PixelContrastSampler.sample(
        bitmap = bmp,
        fgColor = Color.White,
        lineRectsPx = listOf(Rect(10f, 10f, 50f, 24f)),
      )
    assertNotNull(sample)
    assertEquals(1.0, sample.ratio, 0.05)
  }

  @Test
  fun `pixel contrast sampler catches white text over half-bright gradient via p90`() {
    val bmp = Bitmap().apply { allocN32Pixels(60, 40) }
    for (y in 0 until 40) {
      for (x in 0 until 60) {
        val color = if (x < 30) 0xFF181818.toInt() else 0xFFEEEEEE.toInt()
        bmp.putPixel(x, y, color)
      }
    }
    val sample =
      PixelContrastSampler.sample(
        bitmap = bmp,
        fgColor = Color.White,
        lineRectsPx = listOf(Rect(10f, 12f, 50f, 26f)),
      )
    assertNotNull(sample)
    assertTrue(sample.ratio < 2.0, "Expected bright right-half p90 contrast failure, got $sample")
  }

  @Test
  fun `pixel contrast sampler ignores single 1 px bright outlier in 2 px dark perimeter`() {
    val bmp = solidBitmap(60, 40, 0xFF141414.toInt())
    bmp.putPixel(8, 8, 0xFFFFFFFF.toInt())
    val sample =
      PixelContrastSampler.sample(
        bitmap = bmp,
        fgColor = Color.White,
        lineRectsPx = listOf(Rect(10f, 10f, 50f, 24f)),
      )
    assertNotNull(sample)
    assertTrue(
      sample.ratio > 15.0,
      "Single 1 px bright border outlier should be ignored below 90th percentile, got $sample",
    )
  }

  @Test
  fun `enforces primaryLargest with AD_PRIMARY_TAG and detects weightCollapsed`() {
    val src =
      """
      import androidx.compose.foundation.layout.*
      import androidx.compose.material3.*
      import androidx.compose.runtime.*
      import androidx.compose.ui.Modifier
      import androidx.compose.ui.platform.testTag
      import androidx.compose.ui.unit.dp
      import bot.charlies.androiddesign.preview.kit.*

      val preview = designBoard("Hierarchy") {
        screen {
          Column(modifier = Modifier.fillMaxSize()) {
            Button(
              onClick = {},
              modifier = Modifier.testTag(AD_PRIMARY_TAG).height(48.dp),
            ) {
              Text("Primary")
            }
            Button(
              onClick = {},
              modifier = Modifier.height(72.dp).fillMaxWidth(),
            ) {
              Text("Bigger Rival")
            }
            Box(modifier = Modifier.height(50.dp)) {
              Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(50.dp))
                Text("Collapsed", modifier = Modifier.weight(1f))
              }
            }
          }
        }
      }
      """
        .trimIndent()
    val res = renderSource(src)
    val rules = res.tree.findings.map { it.rule }.toSet()
    assertTrue(
      FindingRule.PrimaryLargest in rules,
      "Expected PrimaryLargest finding, got ${res.tree.findings}",
    )
    assertTrue(
      FindingRule.WeightCollapsed in rules,
      "Expected WeightCollapsed finding, got ${res.tree.findings}",
    )
  }

  @Test
  fun `detects cornerClipped, lineLength, and labelTruncated`() {
    val src =
      """
      import androidx.compose.foundation.layout.*
      import androidx.compose.foundation.shape.RoundedCornerShape
      import androidx.compose.material3.*
      import androidx.compose.runtime.*
      import androidx.compose.ui.Modifier
      import androidx.compose.ui.draw.clip
      import androidx.compose.ui.platform.testTag
      import androidx.compose.ui.text.style.TextOverflow
      import androidx.compose.ui.unit.dp
      import bot.charlies.androiddesign.preview.kit.*

      val preview = designBoard("Typography & Clip") {
        screen {
          Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Surface(
              shape = RoundedCornerShape(32.dp),
              modifier = Modifier.clip(RoundedCornerShape(32.dp)),
            ) {
              Text("FlushCornerClippedText")
            }
            Card {
              Text(
                "This line is intentionally much longer than sixty characters inside a wide card so the readability rule flags excessive measure cleanly across multiple lines.",
              )
            }
            Button(
              onClick = {},
              modifier = Modifier.width(64.dp).testTag(AD_PRIMARY_TAG),
            ) {
              Text(
                "Very long button action label",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }
      """
        .trimIndent()
    val res = renderSource(src)
    val rules = res.tree.findings.map { it.rule }.toSet()
    assertTrue(
      FindingRule.CornerClipped in rules,
      "Expected CornerClipped finding, got ${res.tree.findings}",
    )
    assertTrue(
      FindingRule.LabelTruncated in rules,
      "Expected LabelTruncated finding, got ${res.tree.findings}",
    )

    val wideLineSrc =
      """
      import androidx.compose.foundation.layout.*
      import androidx.compose.material3.*
      import androidx.compose.runtime.*
      import androidx.compose.ui.Modifier
      import androidx.compose.ui.platform.testTag
      import androidx.compose.ui.unit.dp
      import bot.charlies.androiddesign.preview.kit.*

      val preview = designBoard("Wide Measure") {
        screen {
          Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Short sample label for character width")
            Card(modifier = Modifier.fillMaxWidth()) {
              Text(
                "This paragraph sits inside an unconstrained full-width tablet card with plenty of horizontal space so each wrapped line spans far more than sixty characters before wrapping onto a second line on screen.",
              )
            }
            Button(onClick = {}, modifier = Modifier.testTag(AD_PRIMARY_TAG)) { Text("Continue") }
          }
        }
      }
      """
        .trimIndent()
    val wideRes =
      renderSource(wideLineSrc, fileName = "line-length.explore.kt", view = Views.Tablet)
    assertTrue(
      wideRes.tree.findings.any { it.rule == FindingRule.LineLength },
      "Expected LineLength finding on tablet wide card, got ${wideRes.tree.findings}",
    )
  }

  @Test
  fun `wear detects roundEdgeClipped and merges scrolled TransformingLazyColumn items`() {
    val scrollSrc =
      """
      import androidx.compose.runtime.*
      import androidx.wear.compose.foundation.lazy.*
      import androidx.wear.compose.material3.*
      import bot.charlies.androiddesign.preview.kit.*

      val preview = wearBoard("Wear Scroll") {
        screen("list") {
          val state = rememberTransformingLazyColumnState()
          ScreenScaffold(scrollState = state) { contentPadding ->
            TransformingLazyColumn(state = state, contentPadding = contentPadding) {
              items(12) { idx ->
                Button(onClick = {}) { Text("Workout item #${'$'}idx") }
              }
            }
          }
        }
      }
      """
        .trimIndent()
    val scrollRes = renderSource(scrollSrc, fileName = "wear-scroll.explore.kt", view = Views.Watch)
    assertNotNull(scrollRes.end, "Expected Wear scrolling list to produce end snapshot")
    val allText = scrollRes.tree.nodes.mapNotNull { it.text?.value }
    assertTrue(
      allText.any { it.contains("Workout item #11") },
      "Expected ScrolledTree.merge to capture bottom item #11, saw: $allText",
    )

    val clipSrc =
      """
      import androidx.compose.foundation.layout.*
      import androidx.compose.runtime.*
      import androidx.compose.ui.Modifier
      import androidx.wear.compose.material3.*
      import bot.charlies.androiddesign.preview.kit.*

      val preview = wearBoard("Wear Corner Clip") {
        screen("top") {
          ScreenScaffold {
            Box(modifier = Modifier.fillMaxSize()) {
              Text("Unpadded top corner text that touches round bezel")
            }
          }
        }
      }
      """
        .trimIndent()
    val clipRes = renderSource(clipSrc, fileName = "wear-clip.explore.kt", view = Views.Watch)
    val rules = clipRes.tree.findings.map { it.rule }.toSet()
    assertTrue(
      FindingRule.RoundEdgeClipped in rules,
      "Expected RoundEdgeClipped on unpadded non-scrolling Wear text, got ${clipRes.tree.findings}",
    )
  }
}
