package bot.charlies.androiddesign.preview.app.android

import bot.charlies.androiddesign.preview.app.server.ContentHasher
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertIs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class AndroidExplorationCompilerTest {

  private fun validExploreSource(extraBody: String = ""): String =
    """
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.padding
    import androidx.compose.material3.Text
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.unit.dp
    import bot.charlies.androiddesign.preview.kit.designBoard
    import bot.charlies.androiddesign.preview.kit.designPainter

    val preview = designBoard(name = "Compiler Test") {
      direction("Main") {
        screen("home") {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Hello Android")
            $extraBody
          }
        }
      }
    }
    """
      .trimIndent()

  @Test
  fun `compiles valid exploration to DEX and collects confined assets`(@TempDir tmp: Path) {
    val meta = BootstrapMeta.locate()
    val assetsDir = tmp.resolve("assets")
    Files.createDirectories(assetsDir)
    val pngBytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3)
    Files.write(assetsDir.resolve("cover.png"), pngBytes)
    Files.writeString(
      tmp.resolve("DESIGN.md"),
      """
      ---
      version: 1
      name: Test Design
      ---
      """
        .trimIndent(),
    )
    val sourceFile = tmp.resolve("Sample.explore.kt")
    Files.writeString(
      sourceFile,
      validExploreSource(
        """Image(painter = designPainter("assets/cover.png"), contentDescription = "Cover")""",
      ),
    )

    val first =
      assertIs<AndroidCompileOutcome.Success>(
        AndroidExplorationCompiler.compile(sourceFile, explicitDesign = null, meta = meta),
      )
    val compiled = first.compiled
    assertTrue(compiled.entryClass.endsWith("Kt"))
    assertTrue(compiled.dexBytes.size > 16)
    val magic = String(compiled.dexBytes.copyOfRange(0, 4), Charsets.US_ASCII)
    assertEquals("dex\n", magic)
    assertTrue(compiled.dexHash.length == 64)
    val expectedAssetSha = ContentHasher.sha256Hex(pngBytes)
    assertEquals(expectedAssetSha, compiled.assetMap["assets/cover.png"])
    assertEquals(pngBytes.toList(), compiled.assetBlobs[expectedAssetSha]?.toList())

    // Second compile with modified body reuses cached D8 session and yields a distinct dexHash.
    Files.writeString(sourceFile, validExploreSource("""Text("Updated copy")"""))
    val second =
      assertIs<AndroidCompileOutcome.Success>(
        AndroidExplorationCompiler.compile(sourceFile, explicitDesign = null, meta = meta),
      )
    assertNotEquals(compiled.dexHash, second.compiled.dexHash)
  }

  @Test
  fun `rejects raw Color hex literals and alpha copy via SourceTokenLinter`(@TempDir tmp: Path) {
    val meta = BootstrapMeta.locate()
    val rawColorFile = tmp.resolve("BadColor.explore.kt")
    Files.writeString(
      rawColorFile,
      validExploreSource(
        """
        val c = androidx.compose.ui.graphics.Color(0xFFFF0000)
        Text("Bad", color = c)
        """
          .trimIndent(),
      ),
    )
    val rawFailed =
      assertIs<AndroidCompileOutcome.Failed>(
        AndroidExplorationCompiler.compile(rawColorFile, explicitDesign = null, meta = meta),
      )
    assertTrue(
      rawFailed.errors.any { "Color" in it.message || "semantic" in it.message.lowercase() },
    )

    val alphaCopyFile = tmp.resolve("BadAlpha.explore.kt")
    Files.writeString(
      alphaCopyFile,
      validExploreSource(
        """
        val c = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        Text("Alpha", color = c)
        """
          .trimIndent(),
      ),
    )
    val alphaFailed =
      assertIs<AndroidCompileOutcome.Failed>(
        AndroidExplorationCompiler.compile(alphaCopyFile, explicitDesign = null, meta = meta),
      )
    assertTrue(alphaFailed.errors.any { "alpha" in it.message.lowercase() })
  }

  @Test
  fun `rejects forbidden JVM IO and Runtime calls via BytecodeGuard`(@TempDir tmp: Path) {
    val meta = BootstrapMeta.locate()
    val fileAccess = tmp.resolve("BadBytecode.explore.kt")
    Files.writeString(
      fileAccess,
      validExploreSource(
        """
        val exists = java.io.File("/etc/passwd").exists()
        Text(exists.toString())
        """
          .trimIndent(),
      ),
    )
    val failed =
      assertIs<AndroidCompileOutcome.Failed>(
        AndroidExplorationCompiler.compile(fileAccess, explicitDesign = null, meta = meta),
      )
    assertFalse(failed.errors.isEmpty())
    assertTrue(failed.errors.any { "java/io/File" in it.message || "File" in it.message })
  }

  @Test
  fun `rejects escaping designPainter paths`(@TempDir tmp: Path) {
    val meta = BootstrapMeta.locate()
    val escapeFile = tmp.resolve("EscapeAsset.explore.kt")
    Files.writeString(
      escapeFile,
      validExploreSource("""val p = "../secret.png" ; Text(p)""").let {
        validExploreSource(
          """Image(painter = designPainter("../secret.png"), contentDescription = "Bad")""",
        )
      },
    )
    val failed =
      assertIs<AndroidCompileOutcome.Failed>(
        AndroidExplorationCompiler.compile(escapeFile, explicitDesign = null, meta = meta),
      )
    assertTrue(failed.errors.any { "escape" in it.message.lowercase() })
  }
}
