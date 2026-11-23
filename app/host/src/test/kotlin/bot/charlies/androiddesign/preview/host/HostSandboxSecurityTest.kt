package bot.charlies.androiddesign.preview.host

import bot.charlies.androiddesign.preview.domain.sandbox.BytecodeGuard
import bot.charlies.androiddesign.preview.domain.sandbox.SandboxClassLoader
import bot.charlies.androiddesign.preview.host.compiler.HostClassCompiler
import bot.charlies.androiddesign.preview.host.compiler.HostCompileOutcome
import org.junit.Assert.assertTrue
import org.junit.Test

class HostSandboxSecurityTest {
  @Test
  fun sourceTokenLinterRejectsRawColorAndAlphaCopy() {
    val rawColorSrc =
      """
      import androidx.compose.ui.graphics.Color
      import bot.charlies.androiddesign.preview.kit.designBoard
      val preview = designBoard("Bad") { screen("main") { val c = Color(0xFF123456) } }
      """
        .trimIndent()
    val res1 = HostClassCompiler.compileToClasses("BadColor.explore.kt", rawColorSrc)
    assertTrue("Expected lint failure for raw Color", res1 is HostCompileOutcome.Failed)

    val alphaSrc =
      """
      import androidx.compose.material3.MaterialTheme
      import androidx.compose.material3.Text
      import bot.charlies.androiddesign.preview.kit.designBoard
      val preview = designBoard("Bad") {
        screen("main") { Text("Hi", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) }
      }
      """
        .trimIndent()
    val res2 = HostClassCompiler.compileToClasses("BadAlpha.explore.kt", alphaSrc)
    assertTrue("Expected lint failure for .copy(alpha=...)", res2 is HostCompileOutcome.Failed)
  }

  @Test
  fun bytecodeGuardAndSandboxClassLoaderBlockDisallowedApis() {
    val hostileSource =
      """
      import java.io.File
      object HostileExplore {
        fun touchFile(): Boolean = File("/etc/passwd").exists()
      }
      """
        .trimIndent()

    // Compile with skipGuards=true to test BytecodeGuard & SandboxClassLoader independently.
    val unguarded =
      HostClassCompiler.compileToClasses("Hostile.explore.kt", hostileSource, skipGuards = true)
    check(unguarded is HostCompileOutcome.Classes) { "Expected compilation with skipGuards=true" }

    val violations =
      unguarded.classes.entries.flatMap { (name, bytes) ->
        BytecodeGuard.verify(name, bytes, unguarded.classes.keys)
      }
    assertTrue(
      "Expected BytecodeGuard violation for java.io.File, got $violations",
      violations.any { it.contains("java.io.File") },
    )

    val normalGuard = HostClassCompiler.compileToClasses("Hostile.explore.kt", hostileSource)
    assertTrue(
      "Expected HostClassCompiler guard rejection",
      normalGuard is HostCompileOutcome.Failed,
    )

    val loader =
      SandboxClassLoader(unguarded.classes, HostSandboxSecurityTest::class.java.classLoader!!)
    val loadFailure = runCatching {
      val cls = loader.loadClass("HostileExplore")
      cls.getDeclaredMethod("touchFile").invoke(cls.getDeclaredField("INSTANCE").get(null))
    }
    assertTrue("Expected SecurityException from SandboxClassLoader", loadFailure.isFailure)
  }

  @Test
  fun validPhoneAndWearBoardsCompileCleanlyWithSourceInfo() {
    val phoneRes =
      HostClassCompiler.compileToClasses(
        HostTestFixtures.PHONE_EXPLORE_FILE,
        HostTestFixtures.PHONE_EXPLORE_SOURCE,
      )
    assertTrue(
      "Phone fixture should compile cleanly: $phoneRes",
      phoneRes is HostCompileOutcome.Classes,
    )

    val wearRes =
      HostClassCompiler.compileToClasses(
        HostTestFixtures.WEAR_EXPLORE_FILE,
        HostTestFixtures.WEAR_EXPLORE_SOURCE,
      )
    assertTrue(
      "Wear fixture should compile cleanly: $wearRes",
      wearRes is HostCompileOutcome.Classes,
    )
  }
}
