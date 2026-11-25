package bot.charlies.androiddesign.preview.app.client

import bot.charlies.androiddesign.preview.app.android.CommandOutput
import bot.charlies.androiddesign.preview.app.android.DeviceManager
import bot.charlies.androiddesign.preview.app.android.ExternalCommandRunner
import bot.charlies.androiddesign.preview.app.server.ServerPaths
import bot.charlies.androiddesign.preview.app.server.ServerRecord
import bot.charlies.androiddesign.preview.app.server.StatusResponse
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import org.junit.jupiter.api.io.TempDir

/**
 * `start` leaves device setup to the server. The server is then the process that boots an emulator,
 * so it knows the emulator is its own and shuts it down when it stops.
 */
class StartCommandTest {
  @Test
  fun `start hands the device request to the server and reports the server's device`(
    @TempDir tmp: Path,
  ) {
    val meta = fakeBootstrap(tmp)
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    val requests = mutableListOf<StartCommand.ServerRequest>()
    val record =
      ServerRecord(
        pid = 42,
        port = 4100,
        jarVersion = "test",
        startedAt = 1L,
        repoRoot = repo.toString(),
        clientSecret = "s",
        viewToken = "v",
        deviceSerial = "emulator-5554",
        adbForwardPort = 18765,
      )
    val deviceCmds = mutableListOf<String>()
    val env = fakeToolsEnv(tmp)
    val deviceManager =
      DeviceManager(
        cacheRoot = tmp.resolve("cache"),
        env = env,
        runner =
          ExternalCommandRunner { cmd, _, _ ->
            val j = cmd.joinToString(" ")
            deviceCmds += j
            when {
              cmd.getOrNull(1) == "devices" -> CommandOutput(0, "List of devices attached\n")
              "emulator list" in j -> CommandOutput(0, "Pixel_10_Pro_XL\n")
              else -> CommandOutput(0, "")
            }
          },
        sleepFn = {},
      )
    val command =
      StartCommand(
        cacheRoot = tmp.resolve("cache"),
        env = env,
        deviceManager = deviceManager,
        bootstrapLocator = { meta },
        startServer = { request ->
          requests += request
          PreviewClient.ServerLookup.Ready(
            record,
            StatusResponse(
              pid = 42,
              port = 4100,
              jarVersion = "test",
              startedAt = 1L,
              compileCount = 0,
              repoRoot = repo.toString(),
              boardUrl = "http://127.0.0.1:4100/",
              deviceSerial = "emulator-5554",
              adbForwardPort = 18765,
            ),
          )
        },
      )
    val bytes = ByteArrayOutputStream()

    val code =
      command.run(
        listOf("--avd", "Pixel_10_Pro_XL", "--headless", repo.toString()),
        PrintStream(bytes),
        PrintStream(ByteArrayOutputStream()),
      )

    assertEquals(ClientExitCode.OK, code)
    assertEquals(
      listOf(
        StartCommand.ServerRequest(
          meta = meta,
          repoRoot = ServerPaths.resolveRepoRoot(repo),
          serial = null,
          avd = "Pixel_10_Pro_XL",
          headless = true,
        ),
      ),
      requests,
    )
    assertFalse(
      deviceCmds.any { "emulator start" in it || "emulator/emulator" in it || "install" in it },
      "start must leave booting and installing to the server: $deviceCmds",
    )
    val json = bytes.toString()
    assertContains(json, "\"serial\":\"emulator-5554\"")
    assertContains(json, "\"forwardPort\":18765")
    assertContains(json, "\"serverPid\":42")
  }

  private fun fakeToolsEnv(root: Path): Map<String, String> {
    val sdk = root.resolve("sdk")
    val adb = sdk.resolve("platform-tools/adb")
    val android = root.resolve("bin/android")
    Files.createDirectories(adb.parent)
    Files.createDirectories(android.parent)
    for (tool in listOf(adb, android)) {
      Files.writeString(tool, "#!/bin/sh\nexit 0\n")
      tool.toFile().setExecutable(true)
    }
    return mapOf(
      "ANDROID_HOME" to sdk.toString(),
      DeviceManager.ADB_ENV to adb.toString(),
      DeviceManager.ANDROID_CLI_ENV to android.toString(),
    )
  }

  private fun fakeBootstrap(root: Path): BootstrapMeta {
    val bdir = root.resolve("bootstrap")
    Files.createDirectories(bdir)
    val apk = bdir.resolve("host-preview.apk").also { Files.writeString(it, "apk") }
    val androidJar = bdir.resolve("android.jar").also { Files.writeString(it, "aj") }
    val d8Jar = bdir.resolve("d8.jar").also { Files.writeString(it, "d8") }
    val bundleJar = bdir.resolve("bundle.jar").also { Files.writeString(it, "bj") }
    val composeJar = bdir.resolve("compose.jar").also { Files.writeString(it, "cj") }
    val workerCp = bdir.resolve("worker.cp").also { Files.writeString(it, "-cp\nfoo.jar\n") }
    val llRuntime =
      bdir.resolve("layoutlib/runtime").also { Files.createDirectories(it.resolve("data")) }
    val llResources =
      bdir.resolve("layoutlib/resources").also { Files.createDirectories(it.resolve("res")) }
    val resDirsFile = bdir.resolve("worker-res-dirs.txt").also { Files.writeString(it, "") }
    val assetsDir = bdir.resolve("assets").also { Files.createDirectories(it) }
    val rJar = bdir.resolve("worker-r.jar").also { Files.writeString(it, "rj") }
    Files.writeString(
      bdir.resolve("bootstrap-meta.properties"),
      """
      androidJar=$androidJar
      d8Jar=$d8Jar
      bundleJar=$bundleJar
      apkPath=$apk
      apkSha256=${BootstrapMeta.computeSha256(apk)}
      composeCompilerPluginJar=$composeJar
      workerCp=$workerCp
      layoutlibRuntimeDir=$llRuntime
      layoutlibResourcesDir=$llResources
      workerResDirsFile=$resDirsFile
      workerAssetsDir=$assetsDir
      workerRJar=$rJar
      """
        .trimIndent(),
    )
    return BootstrapMeta.fromFile(bdir.resolve("bootstrap-meta.properties"))
  }
}
