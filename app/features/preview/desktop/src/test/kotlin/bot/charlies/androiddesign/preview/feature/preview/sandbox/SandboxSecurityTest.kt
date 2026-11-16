package bot.charlies.androiddesign.preview.feature.preview.sandbox

import bot.charlies.androiddesign.preview.kit.BoardSurface
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Verifies the exploration compile-time and load-time security boundary (`SourceTokenLinter`,
 * `BytecodeGuard`, `SandboxClassLoader`) blocks I/O, reflection, thread creation,
 * prefs/ServiceLoader, raw color/alpha hacks, and unbundled font declarations while allowing
 * Compose/coroutines idioms.
 */
class SandboxSecurityTest {
  private fun compileSnippet(body: String, fileName: String = "sec.explore.kt"): CompileResult {
    val dir: Path = createTempDirectory("sec-test-")
    return try {
      ExplorationCompiler.compileSource(fileName, body, dir)
    } finally {
      dir.toFile().deleteRecursively()
    }
  }

  private fun assertRejected(snippet: String, description: String) {
    val result = compileSnippet(snippet)
    assertIs<CompileResult.Failed>(
      result,
      "Expected sandbox to reject ($description), but it compiled",
    )
    assertTrue(result.errors.isNotEmpty(), "Expected validation errors for $description")
  }

  @Test
  fun `rejects java io File imported or fully qualified`() {
    assertRejected(
      """
      import java.io.File
      import androidx.compose.material3.Text
      import bot.charlies.androiddesign.preview.kit.*
      val preview = designBoard {
        screen {
          val f = File("/etc/passwd")
          Text(f.name)
        }
      }
      """
        .trimIndent(),
      "imported java.io.File",
    )
    assertRejected(
      """
      import androidx.compose.material3.Text
      import bot.charlies.androiddesign.preview.kit.*
      val preview = designBoard {
        screen {
          val f = java.io.File("/etc/passwd")
          Text(f.name)
        }
      }
      """
        .trimIndent(),
      "fully qualified java.io.File",
    )
  }

  @Test
  fun `rejects kotlin io path, Class forName, Runtime, System exit, and MethodHandles`() {
    assertRejected(
      """
      import androidx.compose.material3.Text
      import bot.charlies.androiddesign.preview.kit.*
      val preview = designBoard {
        screen {
          val p = kotlin.io.path.Path("/tmp")
          Text(p.toString())
        }
      }
      """
        .trimIndent(),
      "kotlin.io.path.Path",
    )
    assertRejected(
      """
      import androidx.compose.material3.Text
      import bot.charlies.androiddesign.preview.kit.*
      val preview = designBoard {
        screen {
          val c = Class.forName("java.lang.Runtime")
          Text(c.name)
        }
      }
      """
        .trimIndent(),
      "Class.forName",
    )
    assertRejected(
      """
      import androidx.compose.material3.Text
      import bot.charlies.androiddesign.preview.kit.*
      val preview = designBoard {
        screen {
          val r = java.lang.Runtime.getRuntime()
          Text(r.availableProcessors().toString())
        }
      }
      """
        .trimIndent(),
      "Runtime.getRuntime",
    )
    assertRejected(
      """
      import androidx.compose.material3.Text
      import bot.charlies.androiddesign.preview.kit.*
      val preview = designBoard {
        screen {
          java.lang.System.exit(0)
          Text("done")
        }
      }
      """
        .trimIndent(),
      "System.exit",
    )
    assertRejected(
      """
      import androidx.compose.material3.Text
      import bot.charlies.androiddesign.preview.kit.*
      val preview = designBoard {
        screen {
          val l = java.lang.invoke.MethodHandles.lookup()
          Text(l.toString())
        }
      }
      """
        .trimIndent(),
      "MethodHandles.lookup",
    )
  }

  @Test
  fun `rejects thread spawning, ThreadPoolDispatchers, Preferences, ServiceLoader, and Timer`() {
    assertRejected(
      """
      import androidx.compose.material3.Text
      import bot.charlies.androiddesign.preview.kit.*
      val preview = designBoard {
        screen {
          kotlin.concurrent.thread(start = false) { }
          Text("hi")
        }
      }
      """
        .trimIndent(),
      "kotlin.concurrent.thread",
    )
    assertRejected(
      """
      import androidx.compose.material3.Text
      import bot.charlies.androiddesign.preview.kit.*
      import kotlinx.coroutines.DelicateCoroutinesApi
      import kotlinx.coroutines.newSingleThreadContext
      val preview = designBoard {
        screen {
          val d = newSingleThreadContext("x")
          Text(d.toString())
        }
      }
      """
        .trimIndent(),
      "newSingleThreadContext",
    )
    assertRejected(
      """
      import androidx.compose.material3.Text
      import bot.charlies.androiddesign.preview.kit.*
      import kotlinx.coroutines.newFixedThreadPoolContext
      val preview = designBoard {
        screen {
          val d = newFixedThreadPoolContext(2, "x")
          Text(d.toString())
        }
      }
      """
        .trimIndent(),
      "newFixedThreadPoolContext",
    )
    assertRejected(
      """
      import androidx.compose.material3.Text
      import bot.charlies.androiddesign.preview.kit.*
      val preview = designBoard {
        screen {
          val p = java.util.prefs.Preferences.userRoot()
          Text(p.name())
        }
      }
      """
        .trimIndent(),
      "java.util.prefs.Preferences",
    )
    assertRejected(
      """
      import androidx.compose.material3.Text
      import bot.charlies.androiddesign.preview.kit.*
      val preview = designBoard {
        screen {
          val s = java.util.ServiceLoader.load(CharSequence::class.java)
          Text(s.toString())
        }
      }
      """
        .trimIndent(),
      "java.util.ServiceLoader",
    )
    assertRejected(
      """
      import androidx.compose.material3.Text
      import bot.charlies.androiddesign.preview.kit.*
      val preview = designBoard {
        screen {
          val t = java.util.Timer()
          Text(t.toString())
        }
      }
      """
        .trimIndent(),
      "java.util.Timer",
    )
  }

  @Test
  fun `rejects raw Color literals, alpha copy, and unbundled FontFamily in source`() {
    assertRejected(
      """
      import androidx.compose.material3.Text
      import androidx.compose.ui.graphics.Color
      import bot.charlies.androiddesign.preview.kit.*
      val preview = designBoard {
        screen {
          Text("Bad", color = Color(0xFF123456))
        }
      }
      """
        .trimIndent(),
      "raw Color(0xFF123456)",
    )
    assertRejected(
      """
      import androidx.compose.material3.MaterialTheme
      import androidx.compose.material3.Text
      import bot.charlies.androiddesign.preview.kit.*
      val preview = designBoard {
        screen {
          Text("Bad", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
      }
      """
        .trimIndent(),
      ".copy(alpha = ...)",
    )
    assertRejected(
      """
      import androidx.compose.material3.Text
      import androidx.compose.ui.text.font.FontFamily
      import bot.charlies.androiddesign.preview.kit.*
      val preview = designBoard {
        screen {
          Text("Bad", fontFamily = FontFamily.Serif)
        }
      }
      """
        .trimIndent(),
      "FontFamily.Serif",
    )
    assertRejected(
      """
      import androidx.compose.material3.Text
      import androidx.compose.ui.text.font.Font
      import androidx.compose.ui.text.font.FontFamily
      import bot.charlies.androiddesign.preview.kit.*
      val preview = designBoard {
        screen {
          Text("Bad", fontFamily = FontFamily(emptyList<Font>()))
        }
      }
      """
        .trimIndent(),
      "FontFamily(...) constructor",
    )
  }

  @Test
  fun `allows lambdas, string templates, remember, LaunchedEffect delay, and M3 plus Wear Compose`() {
    val phoneSrc =
      """
      import androidx.compose.foundation.layout.*
      import androidx.compose.material3.*
      import androidx.compose.runtime.*
      import androidx.compose.ui.Modifier
      import androidx.compose.ui.platform.testTag
      import bot.charlies.androiddesign.preview.kit.*
      import kotlinx.coroutines.delay

      val preview = designBoard("Allowed Phone", seed = 0xFF3E63DD) {
        screen("main") {
          var tick by remember { mutableStateOf(1) }
          LaunchedEffect(Unit) {
            delay(10)
            tick = 2
          }
          Column {
            Text("Track ${'$'}tick")
            Button(
              onClick = { tick++ },
              modifier = Modifier.testTag(AD_PRIMARY_TAG),
            ) {
              Text("Play")
            }
          }
        }
      }
      """
        .trimIndent()
    val phoneCompiled =
      assertIs<CompileResult.Compiled>(compileSnippet(phoneSrc, "allowed-phone.explore.kt"))
    assertEquals(BoardSurface.Phone, phoneCompiled.board.surface)

    val wearSrc =
      """
      import androidx.compose.runtime.*
      import androidx.wear.compose.material3.*
      import bot.charlies.androiddesign.preview.kit.*

      val preview = wearBoard("Allowed Wear", seed = 0xFF3E63DD) {
        screen("main") {
          var count by remember { mutableStateOf(0) }
          ScreenScaffold {
            Button(onClick = { count++ }) { Text("Lap ${'$'}count") }
          }
        }
      }
      """
        .trimIndent()
    val wearCompiled =
      assertIs<CompileResult.Compiled>(compileSnippet(wearSrc, "allowed-wear.explore.kt"))
    assertEquals(BoardSurface.Wear, wearCompiled.board.surface)
  }

  @Test
  fun `allows exhaustive enum when expressions producing synthetic WhenMappings`() {
    val src =
      """
      import androidx.compose.material3.Text
      import androidx.compose.runtime.*
      import bot.charlies.androiddesign.preview.kit.*

      private enum class Mode { Overview, Detail }

      val preview = designBoard("Enum When") {
        screen("main") {
          var mode by remember { mutableStateOf(Mode.Overview) }
          val label = when (mode) {
            Mode.Overview -> "All"
            Mode.Detail -> "One"
          }
          Text(label)
        }
      }
      """
        .trimIndent()
    assertIs<CompileResult.Compiled>(compileSnippet(src, "enum-when.explore.kt"))
  }
}
