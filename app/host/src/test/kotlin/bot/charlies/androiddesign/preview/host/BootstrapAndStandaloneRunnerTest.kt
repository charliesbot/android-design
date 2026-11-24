package bot.charlies.androiddesign.preview.host

import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.host.robolectric.RobolectricStaging
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.RobolectricRenderRequest
import bot.charlies.androiddesign.preview.model.RobolectricRenderResponse
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BootstrapAndStandaloneRunnerTest {
  @get:Rule val tempFolder = TemporaryFolder()

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

    for (argName in listOf("robolectric.cp", "server.cp", "client.cp")) {
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
    }
  }

  @Test
  fun standaloneRobolectricRunnerServesSequentialRequestsViaStdioFromNeutralCwd() {
    val meta = BootstrapMeta.locate()
    val robolectricCp = meta.bootstrapDir.resolve("robolectric.cp")
    val workDir = tempFolder.newFolder("fixtures").toPath()
    HostTestFixtures.writeSampleCoverPng(workDir)
    val phoneFile = workDir.resolve(HostTestFixtures.PHONE_EXPLORE_FILE)
    val wearFile = workDir.resolve(HostTestFixtures.WEAR_EXPLORE_FILE)
    Files.writeString(phoneFile, HostTestFixtures.PHONE_EXPLORE_SOURCE)
    Files.writeString(wearFile, HostTestFixtures.WEAR_EXPLORE_SOURCE)
    val outDir = tempFolder.newFolder("out").toPath()
    val neutralCwd = tempFolder.newFolder("neutral-cwd")

    val javaBin = Paths.get(System.getProperty("java.home"), "bin", "java").toString()
    val proc =
      ProcessBuilder(
          javaBin,
          "-Xmx3072m",
          "@$robolectricCp",
          "bot.charlies.androiddesign.preview.host.robolectric.RobolectricRunnerMain",
        )
        .directory(neutralCwd)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    val req1 =
      PreviewJson.compact.encodeToString(
        RobolectricRenderRequest.serializer(),
        RobolectricStaging.stage(
          RobolectricRenderRequest(
            id = "seq-1",
            sourcePath = phoneFile.toString(),
            views = listOf("light"),
            screens = listOf("home"),
            outDir = outDir.toString(),
            density = 2f,
            encodePng = true,
          ),
        ),
      )
    // Source-only, as the eval harness sends report renders: the runner must stage it itself.
    val req2 =
      PreviewJson.compact.encodeToString(
        RobolectricRenderRequest.serializer(),
        RobolectricRenderRequest(
          id = "seq-2",
          sourcePath = wearFile.toString(),
          views = listOf("watch"),
          screens = listOf("main"),
          outDir = outDir.toString(),
          density = 2f,
          encodePng = true,
        ),
      )

    proc.outputStream.bufferedWriter(StandardCharsets.UTF_8).use { writer ->
      writer.write(req1)
      writer.newLine()
      writer.write(req2)
      writer.newLine()
      writer.flush()
    }

    val finished = proc.waitFor(120, TimeUnit.SECONDS)
    if (!finished) {
      proc.destroyForcibly()
      error("Standalone RobolectricRunnerMain timed out")
    }
    val stdoutText = proc.inputStream.bufferedReader(StandardCharsets.UTF_8).readText()
    val stderrText = proc.errorStream.bufferedReader(StandardCharsets.UTF_8).readText()
    assertEquals(
      "Child exit code != 0.\nSTDERR:\n$stderrText\nSTDOUT:\n$stdoutText",
      0,
      proc.exitValue(),
    )

    val lines = stdoutText.lines().map { it.trim() }.filter { it.isNotEmpty() }
    assertEquals(
      "Expected exactly 2 clean JSON protocol lines on stdout.\nSTDERR:\n$stderrText\nSTDOUT:\n$stdoutText",
      2,
      lines.size,
    )
    val resp1 =
      PreviewJson.compact.decodeFromString(RobolectricRenderResponse.serializer(), lines[0])
    val resp2 =
      PreviewJson.compact.decodeFromString(RobolectricRenderResponse.serializer(), lines[1])

    assertEquals("seq-1", resp1.id)
    assertTrue(resp1.ok && resp1.valid)
    assertEquals("phone", resp1.surface)
    assertTrue(resp1.trees.single().nodes.isNotEmpty())
    assertTrue(resp1.writtenFiles.all { Files.isRegularFile(Paths.get(it)) })

    assertEquals("seq-2", resp2.id)
    assertTrue(resp2.ok && resp2.valid)
    assertEquals("wear", resp2.surface)
    assertTrue(resp2.trees.single().nodes.isNotEmpty())
    assertTrue(resp2.endTrees.isNotEmpty())
    assertTrue(resp2.writtenFiles.all { Files.isRegularFile(Paths.get(it)) })
  }
}
