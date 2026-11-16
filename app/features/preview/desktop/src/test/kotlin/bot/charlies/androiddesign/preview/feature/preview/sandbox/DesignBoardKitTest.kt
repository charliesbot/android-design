package bot.charlies.androiddesign.preview.feature.preview.sandbox

import bot.charlies.androiddesign.preview.data.DesignMdParse
import bot.charlies.androiddesign.preview.data.DesignMdParser
import bot.charlies.androiddesign.preview.domain.findings.FindingRules
import bot.charlies.androiddesign.preview.domain.layout.LayoutSummary
import bot.charlies.androiddesign.preview.kit.BoardSurface
import bot.charlies.androiddesign.preview.kit.BundledFont
import bot.charlies.androiddesign.preview.kit.PaletteVariant
import bot.charlies.androiddesign.preview.kit.designBoard
import bot.charlies.androiddesign.preview.kit.resolveConfinedAsset
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.M3Component
import bot.charlies.androiddesign.preview.model.Severity
import bot.charlies.androiddesign.preview.model.Views
import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

/** Tests the `bot.charlies.androiddesign.preview.kit` API, assets confinement, and inert locals. */
class DesignBoardKitTest {
  private val desktopView = Views.Desktop
  private val lightView = Views.Light
  private val watchView = Views.Watch

  @Test
  fun `designBoard merges DESIGN md defaults and allows board overrides`() {
    val parsedMd =
      assertIs<DesignMdParse.Parsed>(
        DesignMdParser.parse(
          """
          ---
          colors:
            primary: "#1B6B52"
          material:
            scheme:
              variant: expressive
              contrast: 0.25
          ---
          # Design System
          """
            .trimIndent(),
        ),
      )
    val board =
      designBoard(
        name = "Merged",
        seed = 0xFFB5523BL,
        variant = PaletteVariant.TonalSpot,
        brandFont = BundledFont.Fraunces,
      ) {
        screen { androidx.compose.material3.Text("Hello") }
      }
    val resolved = board.resolveDesignSystem(designMd = parsedMd.theme)
    assertEquals(PaletteVariant.TonalSpot, resolved.variant)
    assertEquals("#B5523B", resolved.seed)
    assertEquals("Fraunces", resolved.typography["displayLarge"]?.fontFamily)
    assertEquals(null, resolved.typography["bodyLarge"]?.fontFamily)
  }

  @Test
  fun `listDetail emits EmptyDetailPane on two-pane desktop when placeholder is omitted`() {
    val srcWithoutPlaceholder =
      """
      import androidx.compose.foundation.layout.*
      import androidx.compose.material3.*
      import androidx.compose.runtime.*
      import androidx.compose.ui.Modifier
      import androidx.compose.ui.platform.testTag
      import bot.charlies.androiddesign.preview.kit.*

      val preview = designBoard("Split") {
        direction("TwoPane") {
          listDetail(
            list = { onOpen ->
              Column {
                Button(onClick = onOpen, modifier = Modifier.testTag(AD_PRIMARY_TAG)) {
                  Text("Open item")
                }
              }
            },
            detail = { Text("Item detail") },
          )
        }
      }
      """
        .trimIndent()
    val srcWithPlaceholder =
      """
      import androidx.compose.foundation.layout.*
      import androidx.compose.material3.*
      import androidx.compose.runtime.*
      import androidx.compose.ui.Modifier
      import androidx.compose.ui.platform.testTag
      import bot.charlies.androiddesign.preview.kit.*

      val preview = designBoard("Split") {
        direction("TwoPane") {
          listDetail(
            list = { onOpen ->
              Column {
                Button(onClick = onOpen, modifier = Modifier.testTag(AD_PRIMARY_TAG)) {
                  Text("Open item")
                }
              }
            },
            detail = { Text("Item detail") },
            placeholder = { Text("Pick an item") },
          )
        }
      }
      """
        .trimIndent()
    val dir = createTempDirectory("list-detail-test-")
    try {
      val compiledNoPh =
        assertIs<CompileResult.Compiled>(
          ExplorationCompiler.compileSource("split.explore.kt", srcWithoutPlaceholder, dir),
        )
      val treeNoPh =
        SandboxHeadlessRenderer.render(
            compiled = compiledNoPh,
            direction = compiledNoPh.board.directions.single(),
            screenId = "list",
            view = desktopView,
            designSystem = compiledNoPh.board.resolveDesignSystem(),
          )
          .tree
      assertTrue(
        treeNoPh.findings.any { it.rule == FindingRule.EmptyDetailPane },
        "Expected EmptyDetailPane finding on desktop without placeholder: ${treeNoPh.findings}",
      )

      val compiledWithPh =
        assertIs<CompileResult.Compiled>(
          ExplorationCompiler.compileSource("split2.explore.kt", srcWithPlaceholder, dir),
        )
      val treeWithPh =
        SandboxHeadlessRenderer.render(
            compiled = compiledWithPh,
            direction = compiledWithPh.board.directions.single(),
            screenId = "list",
            view = desktopView,
            designSystem = compiledWithPh.board.resolveDesignSystem(),
          )
          .tree
      assertTrue(
        treeWithPh.findings.none { it.rule == FindingRule.EmptyDetailPane },
        "Unexpected EmptyDetailPane when placeholder provided: ${treeWithPh.findings}",
      )
    } finally {
      dir.toFile().deleteRecursively()
    }
  }

  @Test
  fun `supportingPane and multi-direction overlays render cleanly`() {
    val src =
      """
      import androidx.compose.foundation.layout.*
      import androidx.compose.material3.*
      import androidx.compose.runtime.*
      import androidx.compose.ui.Modifier
      import androidx.compose.ui.platform.testTag
      import bot.charlies.androiddesign.preview.kit.*

      val preview = designBoard("Multi") {
        direction("A") {
          supportingPane(
            main = { onOpen ->
              Button(onClick = onOpen, modifier = Modifier.testTag(AD_PRIMARY_TAG)) {
                Text("Inspect")
              }
            },
            supporting = { Text("Notes") },
          )
          overlay("filter") {
            AlertDialog(
              onDismissRequest = {},
              confirmButton = { TextButton(onClick = {}) { Text("Apply") } },
              title = { Text("Filter") },
              text = { Text("Active filters") },
            )
          }
        }
        direction("B") {
          screen("main") { Text("Alternate") }
        }
      }
      """
        .trimIndent()
    val dir = createTempDirectory("multi-dir-test-")
    try {
      val compiled =
        assertIs<CompileResult.Compiled>(
          ExplorationCompiler.compileSource("multi.explore.kt", src, dir),
        )
      assertEquals(2, compiled.board.directions.size)
      val dirA = compiled.board.directions[0]
      assertEquals(listOf("main", "supporting"), dirA.screenIds)
      assertEquals(1, dirA.overlaysFor("main").size)
      val overlayRes =
        SandboxHeadlessRenderer.render(
          compiled = compiled,
          direction = dirA,
          screenId = "main",
          view = lightView,
          designSystem = compiled.board.resolveDesignSystem(),
          overlayId = "filter",
        )
      assertTrue(overlayRes.png.isNotEmpty())
      val dialogText = overlayRes.tree.nodes.firstOrNull { it.text?.value == "Active filters" }
      assertNotNull(
        dialogText,
        "Expected dialog text node in overlay tree: ${overlayRes.tree.nodes}",
      )
      val ink = assertNotNull(dialogText.text?.ink)
      assertTrue(ink.x > 10f && ink.y > 50f, "Expected frame-relative dialog ink, got $ink")
      assertNotNull(dialogText.colors, "Expected sampled dialog text contrast colors")
    } finally {
      dir.toFile().deleteRecursively()
    }
  }

  @Test
  fun `BundledFont Doto applies only to display roles and keeps headline in Google Sans Flex`() {
    val src =
      """
      import androidx.compose.foundation.layout.*
      import androidx.compose.material3.*
      import androidx.compose.ui.Modifier
      import androidx.compose.ui.unit.dp
      import bot.charlies.androiddesign.preview.kit.*

      val preview = designBoard("Doto", brandFont = BundledFont.Doto) {
        screen("main") {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Big Hero", style = MaterialTheme.typography.displaySmall)
            Text("Section Headline", style = MaterialTheme.typography.headlineMedium)
          }
        }
      }
      """
        .trimIndent()
    val dir = createTempDirectory("doto-role-test-")
    try {
      val compiled =
        assertIs<CompileResult.Compiled>(
          ExplorationCompiler.compileSource("doto.explore.kt", src, dir),
        )
      val res =
        SandboxHeadlessRenderer.render(
          compiled = compiled,
          direction = compiled.board.directions.single(),
          screenId = "main",
          view = lightView,
          designSystem = compiled.board.resolveDesignSystem(),
        )
      assertTrue(
        res.tree.findings.none { it.rule == FindingRule.Font },
        "Unexpected Font findings for Doto board: ${res.tree.findings}",
      )
      val hero = res.tree.nodes.first { it.text?.value == "Big Hero" }
      val head = res.tree.nodes.first { it.text?.value == "Section Headline" }
      assertEquals("Doto", hero.text?.font)
      assertEquals("Google Sans Flex", head.text?.font)
    } finally {
      dir.toFile().deleteRecursively()
    }
  }

  @Test
  fun `two-pane listDetail isolates per-pane oneFab, primaryLargest, and LayoutSummary panes`() {
    val src =
      """
      import androidx.compose.foundation.layout.*
      import androidx.compose.material3.*
      import androidx.compose.ui.Modifier
      import androidx.compose.ui.platform.testTag
      import androidx.compose.ui.unit.dp
      import bot.charlies.androiddesign.preview.kit.*

      val preview = designBoard("TwoPane Isolation") {
        direction("Split") {
          listDetail(
            list = { onOpen ->
              Scaffold(
                floatingActionButton = { FloatingActionButton(onClick = {}) { Text("+") } },
              ) { pad ->
                Column(modifier = Modifier.padding(pad).padding(16.dp)) {
                  Button(
                    onClick = onOpen,
                    modifier = Modifier.testTag(AD_PRIMARY_TAG).height(64.dp),
                  ) {
                    Text("Open")
                  }
                }
              }
            },
            detail = {
              Scaffold(
                floatingActionButton = { FloatingActionButton(onClick = {}) { Text("Go") } },
              ) { pad ->
                Column(modifier = Modifier.padding(pad).padding(16.dp)) {
                  Button(
                    onClick = {},
                    modifier = Modifier.testTag(AD_PRIMARY_TAG).height(72.dp),
                  ) {
                    Text("Primary Detail")
                  }
                }
              }
            },
            placeholder = { Text("Pick") },
          )
        }
      }
      """
        .trimIndent()
    val dir = createTempDirectory("twopane-isolation-")
    try {
      val compiled =
        assertIs<CompileResult.Compiled>(
          ExplorationCompiler.compileSource("twopane.explore.kt", src, dir),
        )
      val res =
        SandboxHeadlessRenderer.render(
          compiled = compiled,
          direction = compiled.board.directions.single(),
          screenId = "detail",
          view = desktopView,
          designSystem = compiled.board.resolveDesignSystem(),
        )
      val isolationFailures =
        res.tree.findings.filter {
          it.rule == FindingRule.OneFab || it.rule == FindingRule.PrimaryLargest
        }
      assertTrue(
        isolationFailures.isEmpty(),
        "Expected per-pane isolation for OneFab & PrimaryLargest, got $isolationFailures; nodes=${res.tree.nodes.map { it.id }}",
      )
      val summary = LayoutSummary.of(res.tree)
      assertEquals(setOf("list", "detail"), summary.panes.keys, "panes=${summary.panes}")
      assertTrue(
        res.tree.nodes.count { it.type == M3Component.FloatingActionButton.wire } == 2,
      )
    } finally {
      dir.toFile().deleteRecursively()
    }
  }

  @Test
  fun `wearBoard compiles and renders Wear Material 3 screen`() {
    val src =
      """
      import androidx.compose.runtime.*
      import androidx.wear.compose.foundation.lazy.*
      import androidx.wear.compose.material3.*
      import bot.charlies.androiddesign.preview.kit.*

      val preview = wearBoard("Watch timer", seed = 0xFF4F86F7) {
        screen("timer") {
          val state = rememberTransformingLazyColumnState()
          ScreenScaffold(scrollState = state) { contentPadding ->
            TransformingLazyColumn(state = state, contentPadding = contentPadding) {
              item { ListHeader { Text("Intervals") } }
              item { Button(onClick = {}) { Text("Start run") } }
            }
          }
        }
      }
      """
        .trimIndent()
    val dir = createTempDirectory("wear-board-test-")
    try {
      val compiled =
        assertIs<CompileResult.Compiled>(
          ExplorationCompiler.compileSource("timer.explore.kt", src, dir),
        )
      assertEquals(BoardSurface.Wear, compiled.board.surface)
      val res =
        SandboxHeadlessRenderer.render(
          compiled = compiled,
          direction = compiled.board.directions.single(),
          screenId = "timer",
          view = watchView,
          designSystem = compiled.board.resolveDesignSystem(),
        )
      assertTrue(res.png.isNotEmpty())
      val errors = res.tree.findings.filter { it.severity == Severity.Error }
      assertTrue(
        errors.isEmpty(),
        "Expected zero error findings on clean Wear screen, got: ${FindingRules.toString()} $errors",
      )
    } finally {
      dir.toFile().deleteRecursively()
    }
  }

  @Test
  fun `designPainter resolves local assets, blocks path traversal, and inert locals are safe`() {
    val dir = createTempDirectory("assets-test-")
    try {
      val assetsDir = dir.resolve("assets").also { Files.createDirectories(it) }
      val imgPath = assetsDir.resolve("cover.png")
      val bmp = Bitmap().apply { allocN32Pixels(8, 8) }
      bmp.erase(0xFF336699.toInt())
      val png = Image.makeFromBitmap(bmp).use { it.encodeToData(EncodedImageFormat.PNG)!!.bytes }
      Files.write(imgPath, png)

      val resolved = resolveConfinedAsset(dir, "assets/cover.png")
      assertEquals(imgPath.toRealPath(), resolved.toRealPath())
      assertFailsWith<IllegalArgumentException> { resolveConfinedAsset(dir, "../secret.png") }
      assertFailsWith<IllegalArgumentException> { resolveConfinedAsset(dir, "/etc/passwd") }
      assertFailsWith<IllegalArgumentException> { resolveConfinedAsset(dir, "file:///etc/passwd") }

      val src =
        """
        import androidx.compose.foundation.Image
        import androidx.compose.foundation.layout.size
        import androidx.compose.material3.Button
        import androidx.compose.material3.Text
        import androidx.compose.runtime.Composable
        import androidx.compose.runtime.SideEffect
        import androidx.compose.ui.Modifier
        import androidx.compose.ui.platform.LocalClipboardManager
        import androidx.compose.ui.platform.LocalUriHandler
        import androidx.compose.ui.text.AnnotatedString
        import androidx.compose.ui.unit.dp
        import bot.charlies.androiddesign.preview.kit.*

        val preview = designBoard("Inert") {
          screen("main") {
            val uri = LocalUriHandler.current
            val clipboard = LocalClipboardManager.current
            SideEffect {
              uri.openUri("https://example.com")
              clipboard.setText(AnnotatedString("secret"))
            }
            Image(
              painter = designPainter("assets/cover.png"),
              contentDescription = "Cover",
              modifier = Modifier.size(64.dp),
            )
            Button(onClick = {}) { Text("Ok") }
          }
        }
        """
          .trimIndent()
      val compiled =
        assertIs<CompileResult.Compiled>(
          ExplorationCompiler.compileSource("inert.explore.kt", src, dir),
        )
      val res =
        SandboxHeadlessRenderer.render(
          compiled = compiled,
          direction = compiled.board.directions.single(),
          screenId = "main",
          view = lightView,
          designSystem = compiled.board.resolveDesignSystem(),
        )
      assertNotNull(res.tree.nodes.firstOrNull { it.type == "Image" })
    } finally {
      dir.toFile().deleteRecursively()
    }
  }
}
