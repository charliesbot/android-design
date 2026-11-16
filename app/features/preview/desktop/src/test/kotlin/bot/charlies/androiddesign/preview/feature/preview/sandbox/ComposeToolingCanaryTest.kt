package bot.charlies.androiddesign.preview.feature.preview.sandbox

import bot.charlies.androiddesign.preview.model.M3Component
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Canary verifying `@ComposeToolingApi` slot-table extraction still resolves component call names,
 * exact 1-based line numbers, and non-empty dp bounds across Material 3 Expressive screens. If a
 * Compose compiler or runtime upgrade changes `parseSourceInformation`, this test fails loudly.
 */
class ComposeToolingCanaryTest {
  private val lightView: ViewSpec = Views.Light

  @Test
  fun `slot extractor resolves call names, exact 1-based lines, and bounds without stealing user wrappers`() {
    val lines =
      listOf(
        "import androidx.compose.foundation.layout.*", // 1
        "import androidx.compose.material3.*", // 2
        "import androidx.compose.runtime.*", // 3
        "import androidx.compose.ui.Modifier", // 4
        "import androidx.compose.ui.platform.testTag", // 5
        "import androidx.compose.ui.unit.dp", // 6
        "import bot.charlies.androiddesign.preview.kit.*", // 7
        "", // 8
        "@Composable", // 9
        "fun FixtureScreen() {", // 10
        "  Scaffold(", // 11
        "    topBar = { TopAppBar(title = { Text(\"Playlist\") }) },", // 12
        "  ) { innerPadding ->", // 13
        "    Column(", // 14
        "      modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),", // 15
        "      verticalArrangement = Arrangement.spacedBy(12.dp),", // 16
        "    ) {", // 17
        "      Card {", // 18
        "        ListItem(", // 19
        "          headlineContent = { Text(\"Late Night Mix\") },", // 20
        "          supportingContent = { Text(\"32 tracks\") },", // 21
        "        )", // 22
        "      }", // 23
        "      Button(", // 24
        "        onClick = {},", // 25
        "        modifier = Modifier.testTag(AD_PRIMARY_TAG),", // 26
        "      ) {", // 27
        "        Text(\"Play now\")", // 28
        "      }", // 29
        "    }", // 30
        "  }", // 31
        "}", // 32
        "", // 33
        "val preview = designBoard(\"Canary\") {", // 34
        "  screen(\"home\") { FixtureScreen() }", // 35
        "}", // 36
      )
    val sourceText = lines.joinToString("\n")
    val dir: Path = createTempDirectory("canary-explore-")
    try {
      val rawCompile = ExplorationCompiler.compileSource("canary.explore.kt", sourceText, dir)
      assertIs<CompileResult.Compiled>(rawCompile, "Compile failed: $rawCompile")
      val compiled = rawCompile
      val direction = compiled.board.directions.single()
      val rendered =
        SandboxHeadlessRenderer.render(
          compiled = compiled,
          direction = direction,
          screenId = "home",
          view = lightView,
          designSystem = compiled.board.resolveDesignSystem(),
        )

      val nodes = rendered.tree.nodes
      assertTrue(nodes.isNotEmpty(), "Expected extracted nodes, got empty tree")
      assertTrue(
        nodes.none { it.type == "FixtureScreen" },
        "User wrapper FixtureScreen must not steal a child LayoutInfo: $nodes",
      )

      val expectedLines =
        mapOf(
          M3Component.TopAppBar.wire to 12,
          M3Component.Card.wire to 18,
          M3Component.ListItem.wire to 19,
          M3Component.Button.wire to 24,
        )
      for ((expectedType, expectedLine) in expectedLines) {
        val node =
          nodes.firstOrNull { it.type == expectedType }
            ?: error(
              "Missing node of type $expectedType in ${nodes.map { it.type to it.sourceLine }}",
            )
        assertEquals("canary.explore.kt", node.sourceFile, "Wrong sourceFile for $node")
        assertEquals(expectedLine, node.sourceLine, "Wrong sourceLine for $expectedType: $node")
        assertTrue(node.bounds.width > 0f, "Expected positive width for $expectedType: $node")
        assertTrue(node.bounds.height > 0f, "Expected positive height for $expectedType: $node")
      }

      val playButton = nodes.first { it.type == M3Component.Button.wire }
      assertTrue(playButton.primary, "Button with AD_PRIMARY_TAG should be marked primary")

      val textNodes = nodes.filter { it.type == M3Component.Text.wire }
      val expectedTextLines = setOf(12, 20, 21, 28)
      val resolvedTextLines = textNodes.mapNotNull { it.sourceLine }.toSet()
      assertEquals(
        expectedTextLines,
        resolvedTextLines,
        "Expected distinct Text sourceLines $expectedTextLines, got $textNodes",
      )

      assertNotNull(textNodes.firstOrNull { it.text?.value == "Play now" })
      assertTrue(rendered.png.isNotEmpty(), "Expected PNG output bytes")
      assertEquals(
        emptyList(),
        rendered.tree.findings.filter {
          it.rule == bot.charlies.androiddesign.preview.model.FindingRule.TextClipped
        },
        "Unexpected TextClipped findings: ${rendered.tree.findings}",
      )
    } finally {
      dir.toFile().deleteRecursively()
    }
  }

  @Test
  fun `slot extractor resolves cross-layer AlertDialog node, sourceLine, bounds, and child parenting`() {
    val lines =
      listOf(
        "import androidx.compose.foundation.layout.*", // 1
        "import androidx.compose.material3.*", // 2
        "import androidx.compose.runtime.*", // 3
        "import androidx.compose.ui.Modifier", // 4
        "import androidx.compose.ui.unit.dp", // 5
        "import bot.charlies.androiddesign.preview.kit.*", // 6
        "", // 7
        "val preview = designBoard(\"Dialog Canary\") {", // 8
        "  screen(\"home\") {", // 9
        "    Box(modifier = Modifier.fillMaxSize()) {", // 10
        "      Text(\"Behind modal\")", // 11
        "      AlertDialog(", // 12
        "        onDismissRequest = {},", // 13
        "        confirmButton = { TextButton(onClick = {}) { Text(\"Delete\") } },", // 14
        "        title = { Text(\"Delete item?\") },", // 15
        "        text = { Text(\"This cannot be undone.\") },", // 16
        "      )", // 17
        "    }", // 18
        "  }", // 19
        "}", // 20
      )
    val sourceText = lines.joinToString("\n")
    val dir: Path = createTempDirectory("dialog-canary-")
    try {
      val rawCompile =
        ExplorationCompiler.compileSource("dialog-canary.explore.kt", sourceText, dir)
      assertIs<CompileResult.Compiled>(rawCompile, "Compile failed: $rawCompile")
      val direction = rawCompile.board.directions.single()
      val rendered =
        SandboxHeadlessRenderer.render(
          compiled = rawCompile,
          direction = direction,
          screenId = "home",
          view = lightView,
          designSystem = rawCompile.board.resolveDesignSystem(),
        )
      val nodes = rendered.tree.nodes
      val dialogNode = nodes.firstOrNull { it.type == M3Component.Dialog.wire }
      assertNotNull(dialogNode, "Expected cross-layer Dialog node in ${nodes.map { it.type }}")
      assertEquals("dialog-canary.explore.kt", dialogNode.sourceFile)
      assertEquals(12, dialogNode.sourceLine, "Wrong AlertDialog sourceLine: $dialogNode")
      assertTrue(dialogNode.bounds.width > 0f && dialogNode.bounds.height > 0f)
      assertTrue(
        dialogNode.bounds.height < lightView.height,
        "Expected AlertDialog card bounds smaller than screen height, got ${dialogNode.bounds}",
      )

      val titleText = nodes.firstOrNull { it.text?.value == "Delete item?" }
      assertNotNull(titleText, "Expected dialog title Text node")
      assertEquals(dialogNode.id, titleText.parent, "Title Text must parent to Dialog: $titleText")
      assertEquals(15, titleText.sourceLine)

      val confirmButton = nodes.firstOrNull { it.type == M3Component.Button.wire }
      assertNotNull(confirmButton, "Expected confirm TextButton node")
      assertEquals(
        dialogNode.id,
        confirmButton.parent,
        "Confirm button must parent to Dialog: $confirmButton",
      )
      assertEquals(14, confirmButton.sourceLine)
    } finally {
      dir.toFile().deleteRecursively()
    }
  }
}
