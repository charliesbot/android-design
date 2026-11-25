package bot.charlies.androiddesign.preview.host

import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BootstrapAndStandaloneRunnerTest {
  @Test
  fun bootstrapArtifactsApkAndClasspathArgfilesAreValid() {
    val meta = BootstrapMeta.locate()
    assertTrue("host-preview.apk must exist", Files.isRegularFile(meta.apkPath))
    assertTrue("androidx-compile-bundle.jar must exist", Files.isRegularFile(meta.bundleJar))
    assertTrue("android.jar must exist", Files.isRegularFile(meta.androidJar))
    assertTrue("d8.jar must exist", Files.isRegularFile(meta.d8Jar))
    assertTrue("compose plugin must exist", Files.isRegularFile(meta.composeCompilerPluginJar))

    ZipFile(meta.apkPath.toFile()).use { apk ->
      assertTrue(apk.getEntry("assets/fonts/google_sans_flex.ttf") != null)
      assertTrue(apk.getEntry("assets/fonts/roboto_flex.ttf") != null)
      assertTrue(apk.getEntry("assets/fonts/fraunces.ttf") != null)
    }

    var storedClasses = 0
    var kotlinModules = 0
    var kotlinBuiltins = 0
    ZipFile(meta.bundleJar.toFile()).use { bundle ->
      val entries = bundle.entries()
      while (entries.hasMoreElements()) {
        val e = entries.nextElement()
        assertEquals(
          "Bundle entries must be STORED uncompressed: ${e.name}",
          ZipEntry.STORED,
          e.method,
        )
        if (e.name.endsWith(".class")) storedClasses++
        if (e.name.startsWith("META-INF/") && e.name.endsWith(".kotlin_module")) kotlinModules++
        if (e.name.startsWith("kotlin/") && e.name.endsWith(".kotlin_builtins")) kotlinBuiltins++
      }
    }
    assertTrue("Expected >10000 classes in bundle, got $storedClasses", storedClasses > 10_000)
    assertTrue("Expected >30 .kotlin_module entries, got $kotlinModules", kotlinModules > 30)
    assertTrue("Expected >=4 .kotlin_builtins entries, got $kotlinBuiltins", kotlinBuiltins >= 4)

    for (argName in listOf("server.cp", "client.cp", "worker.cp")) {
      val cpFile = meta.bootstrapDir.resolve(argName)
      assertTrue("Missing argfile $cpFile", Files.isRegularFile(cpFile))
      val lines = Files.readAllLines(cpFile).filter { it.isNotBlank() }
      assertEquals("-cp", lines.first())
      val entries = lines[1].split(File.pathSeparator).filter { it.isNotBlank() }
      assertTrue("Empty classpath in $argName", entries.isNotEmpty())
      for (entry in entries) {
        val p = Paths.get(entry)
        assertTrue("Missing classpath entry in $argName: $p", Files.exists(p))
      }
      if (argName == "worker.cp") {
        val layoutlibIdx = entries.indexOfFirst {
          Paths.get(it).fileName.toString().startsWith("layoutlib-")
        }
        val androidIdx = entries.indexOfFirst { Paths.get(it) == meta.androidJar }
        assertTrue("worker.cp must include layoutlib jar", layoutlibIdx >= 0)
        assertTrue("worker.cp must include android.jar", androidIdx >= 0)
        assertTrue("layoutlib must precede android.jar in worker.cp", layoutlibIdx < androidIdx)
        assertEquals("android.jar must be last in worker.cp", entries.lastIndex, androidIdx)
      }
    }
    assertTrue("workerRJar must exist", Files.isRegularFile(meta.workerRJar))
    assertTrue("workerResDirsFile must exist", Files.isRegularFile(meta.workerResDirsFile))
    assertTrue("workerAssetsDir must exist", Files.isDirectory(meta.workerAssetsDir))
    assertTrue(
      "layoutlibRuntimeDir data must exist",
      Files.isDirectory(meta.layoutlibRuntimeDir.resolve("data")),
    )
    assertTrue(
      "layoutlibResourcesDir res must exist",
      Files.isDirectory(meta.layoutlibResourcesDir.resolve("res")),
    )
    assertFalse(
      "robolectric.cp should not be exported",
      Files.exists(meta.bootstrapDir.resolve("robolectric.cp")),
    )
  }

  /**
   * A compressed font asset makes every Typeface built from it copy the whole file onto the Java
   * heap, and ProposalFonts keeps one Typeface per weight or icon size. Stored fonts are mapped.
   */
  @Test
  fun apkStoresFontAssetsUncompressed() {
    val meta = BootstrapMeta.locate()
    ZipFile(meta.apkPath.toFile()).use { apk ->
      val fonts = apk.entries().toList().filter { it.name.endsWith(".ttf") }
      assertTrue("Expected bundled fonts in the APK", fonts.isNotEmpty())
      for (font in fonts) {
        assertEquals("Font must be STORED: ${font.name}", ZipEntry.STORED, font.method)
      }
    }
  }
}
