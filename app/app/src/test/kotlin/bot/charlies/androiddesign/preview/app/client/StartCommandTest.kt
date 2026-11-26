package bot.charlies.androiddesign.preview.app.client

import bot.charlies.androiddesign.preview.app.server.RuntimeIdentity
import bot.charlies.androiddesign.preview.app.server.ServerPaths
import bot.charlies.androiddesign.preview.app.server.ServerRecord
import bot.charlies.androiddesign.preview.app.server.StatusResponse
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.io.TempDir

/** `start` starts only the shared host server and layoutlib worker JVM with no device setup. */
class StartCommandTest {
  @Test
  fun `start requests device-free server start and reports layoutlib backend`(@TempDir tmp: Path) {
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
      )
    val command =
      StartCommand(
        cacheRoot = tmp.resolve("cache"),
        env = emptyMap(),
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
              backend = RuntimeIdentity.BACKEND_LAYOUTLIB,
            ),
          )
        },
      )
    val bytes = ByteArrayOutputStream()

    val code =
      command.run(
        listOf(repo.toString()),
        PrintStream(bytes),
        PrintStream(ByteArrayOutputStream()),
      )

    assertEquals(ClientExitCode.OK, code)
    assertEquals(
      listOf(StartCommand.ServerRequest(meta = meta, repoRoot = ServerPaths.resolveRepoRoot(repo))),
      requests,
    )
    val json = bytes.toString()
    assertContains(json, "\"backend\":\"layoutlib\"")
    assertContains(json, "\"serverPid\":42")
  }

  @Test
  fun `start rejects retired device flags and reports usage, help, and missing bootstrap`(
    @TempDir tmp: Path,
  ) {
    val meta = fakeBootstrap(tmp)
    for (badFlag in listOf("--serial", "--avd", "--headless", "--bogus")) {
      val code =
        StartCommand(tmp.resolve("c"), emptyMap(), bootstrapLocator = { meta })
          .run(
            listOf(badFlag),
            PrintStream(ByteArrayOutputStream()),
            PrintStream(ByteArrayOutputStream()),
          )
      assertEquals(ClientExitCode.USAGE, code, "Expected USAGE for $badFlag")
    }

    val helpOut = ByteArrayOutputStream()
    val helpCode =
      StartCommand(tmp.resolve("c"), emptyMap(), bootstrapLocator = { meta })
        .run(listOf("--help"), PrintStream(helpOut), PrintStream(ByteArrayOutputStream()))
    assertEquals(ClientExitCode.OK, helpCode)
    val helpText = helpOut.toString(StandardCharsets.UTF_8)
    assertTrue("layoutlib" in helpText, helpText)
    assertTrue("--headless" !in helpText, helpText)

    val notReadyOut = ByteArrayOutputStream()
    val notReadyCode =
      StartCommand(tmp.resolve("c"), emptyMap(), bootstrapLocator = { null })
        .run(emptyList(), PrintStream(notReadyOut), PrintStream(ByteArrayOutputStream()))
    assertEquals(ClientExitCode.NOT_READY, notReadyCode)
    assertTrue("bootstrap_missing" in notReadyOut.toString(StandardCharsets.UTF_8))
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
