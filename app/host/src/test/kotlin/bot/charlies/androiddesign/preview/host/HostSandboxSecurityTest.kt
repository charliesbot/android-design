package bot.charlies.androiddesign.preview.host

import bot.charlies.androiddesign.preview.app.android.AndroidExplorationCompiler
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.domain.sandbox.BytecodeGuard
import bot.charlies.androiddesign.preview.domain.sandbox.SandboxClassLoader
import org.junit.Assert.assertTrue
import org.junit.Test

class HostSandboxSecurityTest {
  private val meta by lazy { BootstrapMeta.locate() }

  @Test
  fun sourceTokenLinterRejectsRawColorAndAlphaCopy() {
    val rawColorSrc =
      """
      import androidx.compose.ui.graphics.Color
      import bot.charlies.androiddesign.preview.kit.designBoard
      val preview = designBoard("Bad") { screen("main") { val c = Color(0xFF123456) } }
      """
        .trimIndent()
    val res1 =
      AndroidExplorationCompiler.compileSourceToClasses(
        "BadColor.explore.kt",
        rawColorSrc,
        meta = meta,
      )
    assertTrue(
      "Expected lint failure for raw Color",
      res1 is AndroidExplorationCompiler.ClassesOutcome.Failed,
    )

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
    val res2 =
      AndroidExplorationCompiler.compileSourceToClasses(
        "BadAlpha.explore.kt",
        alphaSrc,
        meta = meta,
      )
    assertTrue(
      "Expected lint failure for .copy(alpha=...)",
      res2 is AndroidExplorationCompiler.ClassesOutcome.Failed,
    )
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
      AndroidExplorationCompiler.compileSourceToClasses(
        "Hostile.explore.kt",
        hostileSource,
        skipGuards = true,
        meta = meta,
      )
    check(unguarded is AndroidExplorationCompiler.ClassesOutcome.Ready) {
      "Expected compilation with skipGuards=true"
    }

    val violations =
      unguarded.classBytes.entries.flatMap { (name, bytes) ->
        BytecodeGuard.verify(name, bytes, unguarded.classBytes.keys)
      }
    assertTrue(
      "Expected BytecodeGuard violation for java.io.File, got $violations",
      violations.any { it.contains("java.io.File") },
    )

    val normalGuard =
      AndroidExplorationCompiler.compileSourceToClasses(
        "Hostile.explore.kt",
        hostileSource,
        meta = meta,
      )
    assertTrue(
      "Expected guard rejection",
      normalGuard is AndroidExplorationCompiler.ClassesOutcome.Failed,
    )

    val loader =
      SandboxClassLoader(unguarded.classBytes, HostSandboxSecurityTest::class.java.classLoader!!)
    val loadFailure = runCatching {
      val cls = loader.loadClass("HostileExplore")
      cls.getDeclaredMethod("touchFile").invoke(cls.getDeclaredField("INSTANCE").get(null))
    }
    assertTrue("Expected SecurityException from SandboxClassLoader", loadFailure.isFailure)
  }

  @Test
  fun validPhoneAndWearBoardsCompileCleanlyWithSourceInfo() {
    val phoneRes =
      AndroidExplorationCompiler.compileSourceToClasses(
        HostTestFixtures.PHONE_EXPLORE_FILE,
        HostTestFixtures.PHONE_EXPLORE_SOURCE,
        meta = meta,
      )
    assertTrue(
      "Phone fixture should compile cleanly: $phoneRes",
      phoneRes is AndroidExplorationCompiler.ClassesOutcome.Ready,
    )

    val wearRes =
      AndroidExplorationCompiler.compileSourceToClasses(
        HostTestFixtures.WEAR_EXPLORE_FILE,
        HostTestFixtures.WEAR_EXPLORE_SOURCE,
        meta = meta,
      )
    assertTrue(
      "Wear fixture should compile cleanly: $wearRes",
      wearRes is AndroidExplorationCompiler.ClassesOutcome.Ready,
    )
  }
}
