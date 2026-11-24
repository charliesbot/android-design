package bot.charlies.androiddesign.preview.app.android

import bot.charlies.androiddesign.preview.app.client.StartCommand
import bot.charlies.androiddesign.preview.app.client.V5ExitCode
import bot.charlies.androiddesign.preview.app.server.RuntimeIdentity
import bot.charlies.androiddesign.preview.app.server.ServerPaths
import bot.charlies.androiddesign.preview.app.server.ServerRecord
import bot.charlies.androiddesign.preview.app.server.StatusResponse
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.model.DevicePingResponse
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertIs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class DeviceManagerTest {

  private fun fakeBootstrap(root: Path, apkContent: String = "apk-v1"): BootstrapMeta {
    val bdir = root.resolve("bootstrap")
    Files.createDirectories(bdir)
    val apk = bdir.resolve("host-preview.apk").also { Files.writeString(it, apkContent) }
    val androidJar = bdir.resolve("android.jar").also { Files.writeString(it, "aj") }
    val d8Jar = bdir.resolve("d8.jar").also { Files.writeString(it, "d8") }
    val bundleJar = bdir.resolve("bundle.jar").also { Files.writeString(it, "bj") }
    val composeJar = bdir.resolve("compose.jar").also { Files.writeString(it, "cj") }
    Files.writeString(bdir.resolve("server.cp"), "-cp\nfoo.jar\n")
    val sha = BootstrapMeta.computeSha256(apk)
    Files.writeString(
      bdir.resolve("bootstrap-meta.properties"),
      """
      androidJar=$androidJar
      d8Jar=$d8Jar
      bundleJar=$bundleJar
      apkPath=$apk
      apkSha256=$sha
      composeCompilerPluginJar=$composeJar
      """
        .trimIndent(),
    )
    return BootstrapMeta.fromFile(bdir.resolve("bootstrap-meta.properties"))
  }

  private fun fakeToolsEnv(root: Path): Map<String, String> {
    val sdk = root.resolve("sdk")
    val adb = sdk.resolve("platform-tools/adb")
    val android = root.resolve("bin/android")
    Files.createDirectories(adb.parent)
    Files.createDirectories(android.parent)
    Files.writeString(adb, "#!/bin/sh\nexit 0\n")
    Files.writeString(android, "#!/bin/sh\nexit 0\n")
    adb.toFile().setExecutable(true)
    android.toFile().setExecutable(true)
    return mapOf(
      "ANDROID_HOME" to sdk.toString(),
      DeviceManager.ADB_ENV to adb.toString(),
      DeviceManager.ANDROID_CLI_ENV to android.toString(),
    )
  }

  @Test
  fun `runs full setup sequence, caches APK stamp, and reinstalls on change or force`(
    @TempDir tmp: Path,
  ) {
    val cache = tmp.resolve("cache")
    val metaV1 = fakeBootstrap(tmp.resolve("b1"), "apk-bytes-1")
    val env = fakeToolsEnv(tmp)

    val commands = mutableListOf<List<String>>()
    var packageInstalled = false
    var pingCount = 0

    val runner = ExternalCommandRunner { cmd, _, _ ->
      commands += cmd
      val joined = cmd.joinToString(" ")
      when {
        cmd.getOrNull(1) == "devices" ->
          CommandOutput(0, "List of devices attached\nemulator-5554\tdevice\n")
        "emu avd name" in joined -> CommandOutput(0, "Pixel_Phone\nOK\n")
        "getprop sys.boot_completed" in joined -> CommandOutput(0, "1\n")
        "pm path" in joined ->
          CommandOutput(0, if (packageInstalled) "package:/data/app/host.apk\n" else "")
        cmd.contains("install") -> {
          packageInstalled = true
          CommandOutput(0, "Success\n")
        }
        "forward --list" in joined -> CommandOutput(0, "")
        else -> CommandOutput(0, "OK\n")
      }
    }

    val dm =
      DeviceManager(
        cacheRoot = cache,
        env = env,
        runner = runner,
        portAllocator = { 18999 },
        pingFn = { _ ->
          pingCount++
          if (pingCount >= 2) DevicePingResponse(ok = true, pid = 505) else null
        },
        sleepFn = {},
      )

    val first = assertIs<DeviceSetupResult.Ready>(dm.ensureDeviceReady(metaV1))
    assertEquals("emulator-5554", first.serial)
    assertEquals(18999, first.forwardPort)
    assertTrue(first.apkInstalled)
    assertTrue(first.aotCompiled)
    assertEquals(505, first.sandboxPid)

    val joinedAll = commands.map { it.joinToString(" ") }
    assertTrue(joinedAll.any { "install --device=emulator-5554" in it })
    assertTrue(
      joinedAll.any { "cmd package compile -m speed -f ${DeviceManager.HOST_PACKAGE}" in it },
    )
    assertTrue(
      joinedAll.any { "dumpsys deviceidle whitelist +${DeviceManager.HOST_PACKAGE}" in it },
    )
    assertTrue(
      joinedAll.any {
        "cmd appops set ${DeviceManager.HOST_PACKAGE} RUN_ANY_IN_BACKGROUND allow" in it
      },
    )
    assertTrue(joinedAll.any { "forward tcp:18999 localabstract:adp-render-sandbox" in it })
    assertTrue(
      joinedAll.any {
        "am start -W -n ${DeviceManager.HOST_RENDER_ACTIVITY} --ez move_to_back true" in it
      },
    )

    // Second run with unchanged APK skips install and AOT.
    commands.clear()
    val second = assertIs<DeviceSetupResult.Ready>(dm.ensureDeviceReady(metaV1))
    assertFalse(second.apkInstalled)
    assertFalse(second.aotCompiled)
    assertFalse(commands.any { "install" in it.joinToString(" ") })
    assertFalse(commands.any { "cmd package compile" in it.joinToString(" ") })

    // Changing APK SHA forces reinstall + AOT.
    commands.clear()
    val metaV2 = fakeBootstrap(tmp.resolve("b2"), "apk-bytes-2")
    val third = assertIs<DeviceSetupResult.Ready>(dm.ensureDeviceReady(metaV2))
    assertTrue(third.apkInstalled)
    assertTrue(third.aotCompiled)

    // forceReinstall = true also forces reinstall even with matching SHA.
    commands.clear()
    val fourth =
      assertIs<DeviceSetupResult.Ready>(dm.ensureDeviceReady(metaV2, forceReinstall = true))
    assertTrue(fourth.apkInstalled)
    assertTrue(fourth.aotCompiled)
  }

  @Test
  fun `selects resizable AVD before phone AVD and creates medium_phone when none exist`(
    @TempDir tmp: Path,
  ) {
    val env = fakeToolsEnv(tmp)
    val meta = fakeBootstrap(tmp.resolve("b"), "apk")

    fun runWithAvds(initialAvds: MutableList<String>): List<List<String>> {
      val cmds = mutableListOf<List<String>>()
      var online = false
      val runner = ExternalCommandRunner { cmd, _, _ ->
        cmds += cmd
        val j = cmd.joinToString(" ")
        when {
          cmd.getOrNull(1) == "devices" ->
            CommandOutput(
              0,
              if (online) "List of devices attached\nemulator-5560\tdevice\n"
              else "List of devices attached\n",
            )
          "emulator list" in j -> CommandOutput(0, initialAvds.joinToString("\n") + "\n")
          "emulator create" in j -> {
            initialAvds += "medium_phone"
            CommandOutput(0, "Created medium_phone\n")
          }
          "emulator start" in j -> {
            online = true
            CommandOutput(0, "Started\n")
          }
          "getprop sys.boot_completed" in j -> CommandOutput(0, "1\n")
          "pm path" in j -> CommandOutput(0, "package:/data/app/host.apk\n")
          "forward --list" in j ->
            CommandOutput(0, "emulator-5560 tcp:18990 localabstract:adp-render-sandbox\n")
          else -> CommandOutput(0, "OK\n")
        }
      }
      val dm =
        DeviceManager(
          cacheRoot = tmp.resolve("cache-${initialAvds.size}"),
          env = env,
          runner = runner,
          pingFn = { DevicePingResponse(ok = true, pid = 11) },
          sleepFn = {},
        )
      assertIs<DeviceSetupResult.Ready>(dm.ensureDeviceReady(meta, headless = true))
      return cmds
    }

    val resizableCmds = runWithAvds(mutableListOf("Pixel_9_Phone", "Medium_Resizable_API_35"))
    val startRes = resizableCmds.first { "emulator start" in it.joinToString(" ") }
    assertEquals(
      listOf(
        env[DeviceManager.ANDROID_CLI_ENV]!!,
        "emulator",
        "start",
        "--headless",
        "Medium_Resizable_API_35",
      ),
      startRes,
    )

    val createCmds = runWithAvds(mutableListOf())
    assertTrue(
      createCmds.any {
        "emulator create ${DeviceManager.DEFAULT_AVD_PROFILE}" in it.joinToString(" ")
      },
    )
    val startCreated = createCmds.first { "emulator start" in it.joinToString(" ") }
    assertTrue("medium_phone" in startCreated)
  }

  @Test
  fun `StartCommand returns exit codes 0, 2, and 3`(@TempDir tmp: Path) {
    val meta = fakeBootstrap(tmp.resolve("b"), "apk")
    val envWithBootstrap =
      fakeToolsEnv(tmp) +
        mapOf(
          RuntimeIdentity.BACKEND_ENV to RuntimeIdentity.BACKEND_ANDROID,
          BootstrapMeta.BOOTSTRAP_DIR_ENV to meta.bootstrapDir.toString(),
          RuntimeIdentity.SERVER_CP_ENV to meta.bootstrapDir.resolve("server.cp").toString(),
        )

    // Exit 2 on unknown option.
    val usageOut = ByteArrayOutputStream()
    val usageCode =
      StartCommand(tmp.resolve("c0"), envWithBootstrap)
        .run(listOf("--bogus"), PrintStream(usageOut), PrintStream(ByteArrayOutputStream()))
    assertEquals(V5ExitCode.USAGE, usageCode)

    // Exit 3 when bootstrap missing.
    val notReadyOut = ByteArrayOutputStream()
    val notReadyCode =
      StartCommand(tmp.resolve("c1"), emptyMap(), bootstrapLocator = { null })
        .run(emptyList(), PrintStream(notReadyOut), PrintStream(ByteArrayOutputStream()))
    assertEquals(V5ExitCode.NOT_READY, notReadyCode)
    assertTrue("bootstrap_missing" in notReadyOut.toString(StandardCharsets.UTF_8))

    // Exit 2 when --serial is not online.
    val noAdbsRunner = ExternalCommandRunner { cmd, _, _ ->
      if (cmd.getOrNull(1) == "devices") CommandOutput(0, "List of devices attached\n")
      else CommandOutput(0, "")
    }
    val dmUnknownSerial =
      DeviceManager(tmp.resolve("c2"), envWithBootstrap, noAdbsRunner, sleepFn = {})
    val serialCode =
      StartCommand(tmp.resolve("c2"), envWithBootstrap, dmUnknownSerial)
        .run(
          listOf("--serial", "emulator-9999"),
          PrintStream(ByteArrayOutputStream()),
          PrintStream(ByteArrayOutputStream()),
        )
    assertEquals(V5ExitCode.USAGE, serialCode)

    // Exit 0 when device + server are ready.
    val readyRunner = ExternalCommandRunner { cmd, _, _ ->
      val j = cmd.joinToString(" ")
      when {
        cmd.getOrNull(1) == "devices" ->
          CommandOutput(0, "List of devices attached\nemulator-5554\tdevice\n")
        "emu avd name" in j -> CommandOutput(0, "Pixel_10\nOK\n")
        "getprop sys.boot_completed" in j -> CommandOutput(0, "1\n")
        "pm path" in j -> CommandOutput(0, "package:/data/app/host.apk\n")
        "forward --list" in j ->
          CommandOutput(0, "emulator-5554 tcp:18991 localabstract:adp-render-sandbox\n")
        else -> CommandOutput(0, "OK\n")
      }
    }
    val readyCache = tmp.resolve("c3")
    val readyDm =
      DeviceManager(
        cacheRoot = readyCache,
        env = envWithBootstrap,
        runner = readyRunner,
        pingFn = { DevicePingResponse(ok = true, pid = 77) },
        sleepFn = {},
      )
    val stubHttp =
      com.sun.net.httpserver.HttpServer.create(
        java.net.InetSocketAddress(java.net.InetAddress.getLoopbackAddress(), 0),
        0,
      )
    val id = RuntimeIdentity.compute(envWithBootstrap)
    val repo = tmp.resolve("repo").also { Files.createDirectories(it.resolve(".git")) }
    stubHttp.createContext("/api/status") { ex ->
      val body =
        bot.charlies.androiddesign.preview.model.PreviewJson.compact.encodeToString(
          StatusResponse.serializer(),
          StatusResponse(
            pid = 999_999_999L,
            port = stubHttp.address.port,
            jarVersion = bot.charlies.androiddesign.preview.app.AppInfo.VERSION,
            startedAt = System.currentTimeMillis(),
            compileCount = 0,
            repoRoot = repo.toString(),
            boardUrl = "http://127.0.0.1:${stubHttp.address.port}",
            backend = id.backend,
            runtimeHash = id.runtimeHash,
            deviceSerial = "emulator-5554",
          ),
        )
      val bytes = body.toByteArray(StandardCharsets.UTF_8)
      ex.sendResponseHeaders(200, bytes.size.toLong())
      ex.responseBody.use { it.write(bytes) }
    }
    stubHttp.start()
    try {
      ServerPaths.writeRecord(
        ServerPaths.sharedRecordPath(readyCache),
        ServerRecord(
          pid = 999_999_999L,
          port = stubHttp.address.port,
          viewToken = "vt",
          clientSecret = "sec",
          jarVersion = bot.charlies.androiddesign.preview.app.AppInfo.VERSION,
          startedAt = System.currentTimeMillis(),
          repoRoot = repo.toString(),
          backend = id.backend,
          runtimeHash = id.runtimeHash,
          deviceSerial = "emulator-5554",
          apkSha256 = id.apkSha256,
        ),
      )
      val okOut = ByteArrayOutputStream()
      val okCode =
        StartCommand(readyCache, envWithBootstrap, readyDm, bootstrapLocator = { meta })
          .run(listOf(repo.toString()), PrintStream(okOut), PrintStream(ByteArrayOutputStream()))
      assertEquals(V5ExitCode.OK, okCode, okOut.toString(StandardCharsets.UTF_8))
      val jsonStr = okOut.toString(StandardCharsets.UTF_8).trim()
      val parsedResp =
        bot.charlies.androiddesign.preview.model.PreviewJson.compact.decodeFromString(
          StartCommand.StartResponse.serializer(),
          jsonStr,
        )
      assertTrue(parsedResp.ok, jsonStr)
      assertEquals("emulator-5554", parsedResp.serial)
    } finally {
      stubHttp.stop(0)
    }
  }

  @Test
  fun `requestedAvd reuses matching online AVD or boots target AVD instead of unrelated online device`(
    @TempDir tmp: Path,
  ) {
    val env = fakeToolsEnv(tmp)
    val meta = fakeBootstrap(tmp.resolve("b"), "apk")

    // Case 1: Target_AVD is not online while Unrelated_AVD is online on emulator-5554 -> boot
    // Target_AVD on emulator-5562 without returning emulator-5554.
    var targetBooted = false
    val cmdsBoot = mutableListOf<List<String>>()
    val bootRunner = ExternalCommandRunner { cmd, _, _ ->
      cmdsBoot += cmd
      val j = cmd.joinToString(" ")
      when {
        cmd.getOrNull(1) == "devices" ->
          CommandOutput(
            0,
            buildString {
              appendLine("List of devices attached")
              appendLine("emulator-5554\tdevice")
              if (targetBooted) appendLine("emulator-5562\tdevice")
            },
          )
        "-s emulator-5554 emu avd name" in j -> CommandOutput(0, "Unrelated_AVD\nOK\n")
        "-s emulator-5562 emu avd name" in j -> CommandOutput(0, "Target_AVD\nOK\n")
        "emulator list" in j -> CommandOutput(0, "Unrelated_AVD\nTarget_AVD\n")
        "emulator start" in j -> {
          targetBooted = true
          CommandOutput(0, "Started\n")
        }
        "getprop sys.boot_completed" in j -> CommandOutput(0, "1\n")
        "pm path" in j -> CommandOutput(0, "package:/data/app/host.apk\n")
        "forward --list" in j ->
          CommandOutput(0, "emulator-5562 tcp:18995 localabstract:adp-render-sandbox\n")
        else -> CommandOutput(0, "OK\n")
      }
    }
    val dmBoot =
      DeviceManager(
        cacheRoot = tmp.resolve("c-boot"),
        env = env,
        runner = bootRunner,
        pingFn = { DevicePingResponse(ok = true, pid = 88) },
        sleepFn = {},
      )
    val bootRes =
      assertIs<DeviceSetupResult.Ready>(
        dmBoot.ensureDeviceReady(meta, requestedAvd = "Target_AVD", headless = true),
      )
    assertEquals("emulator-5562", bootRes.serial)
    assertEquals("Target_AVD", bootRes.avdName)
    assertTrue(
      cmdsBoot.any {
        "emulator start --headless Target_AVD" in it.joinToString(" ")
      },
    )

    // Case 2: Target_AVD IS already online on emulator-5562 alongside emulator-5554 -> reuse it.
    val cmdsReuse = mutableListOf<List<String>>()
    val reuseRunner = ExternalCommandRunner { cmd, _, _ ->
      cmdsReuse += cmd
      val j = cmd.joinToString(" ")
      when {
        cmd.getOrNull(1) == "devices" ->
          CommandOutput(
            0,
            "List of devices attached\nemulator-5554\tdevice\nemulator-5562\tdevice\n",
          )
        "-s emulator-5554 emu avd name" in j -> CommandOutput(0, "Unrelated_AVD\nOK\n")
        "-s emulator-5562 emu avd name" in j -> CommandOutput(0, "Target_AVD\nOK\n")
        "getprop sys.boot_completed" in j -> CommandOutput(0, "1\n")
        "pm path" in j -> CommandOutput(0, "package:/data/app/host.apk\n")
        "forward --list" in j ->
          CommandOutput(0, "emulator-5562 tcp:18996 localabstract:adp-render-sandbox\n")
        else -> CommandOutput(0, "OK\n")
      }
    }
    val dmReuse =
      DeviceManager(
        cacheRoot = tmp.resolve("c-reuse"),
        env = env,
        runner = reuseRunner,
        pingFn = { DevicePingResponse(ok = true, pid = 89) },
        sleepFn = {},
      )
    val reuseRes =
      assertIs<DeviceSetupResult.Ready>(
        dmReuse.ensureDeviceReady(meta, requestedAvd = "Target_AVD"),
      )
    assertEquals("emulator-5562", reuseRes.serial)
    assertFalse(cmdsReuse.any { "emulator start" in it.joinToString(" ") })
  }

  private fun stuckPing() =
    DevicePingResponse(
      pid = 1,
      inFlightOp = "PRELOAD_DEX",
      inFlightAgeMs = 60_000,
      inFlightBudgetMs = 30_000,
    )

  @Test
  fun `a sandbox whose worker outlived its watchdog is force-stopped before relaunch`(
    @TempDir tmp: Path,
  ) {
    val meta = fakeBootstrap(tmp.resolve("b"), "apk-stuck")
    val env = fakeToolsEnv(tmp)
    val commands = mutableListOf<String>()
    val runner = ExternalCommandRunner { cmd, _, _ ->
      val j = cmd.joinToString(" ")
      commands += j
      when {
        cmd.getOrNull(1) == "devices" ->
          CommandOutput(0, "List of devices attached\nemulator-5554\tdevice\n")
        "emu avd name" in j -> CommandOutput(0, "Pixel_Phone\nOK\n")
        "getprop sys.boot_completed" in j -> CommandOutput(0, "1\n")
        "pm path" in j -> CommandOutput(0, "package:/data/app/host.apk\n")
        "forward --list" in j ->
          CommandOutput(0, "emulator-5554 tcp:18997 localabstract:adp-render-sandbox\n")
        else -> CommandOutput(0, "OK\n")
      }
    }
    var pings = 0
    val dm =
      DeviceManager(
        cacheRoot = tmp.resolve("c"),
        env = env,
        runner = runner,
        pingFn = {
          pings++
          if (pings == 1) stuckPing() else DevicePingResponse(pid = 2)
        },
        sleepFn = {},
      )
    assertIs<DeviceSetupResult.Ready>(dm.ensureDeviceReady(meta))
    val stopIdx = commands.indexOfFirst { "am force-stop ${DeviceManager.HOST_PACKAGE}" in it }
    val startIdx = commands.indexOfFirst {
      "am start -W -n ${DeviceManager.HOST_RENDER_ACTIVITY}" in it
    }
    assertTrue(stopIdx >= 0, "expected force-stop in $commands")
    assertTrue(startIdx > stopIdx, "expected relaunch after force-stop in $commands")
  }

  @Test
  fun `restartSandboxOnSerial force-stops a wedged worker instead of trusting its ping`(
    @TempDir tmp: Path,
  ) {
    val env = fakeToolsEnv(tmp)
    val commands = mutableListOf<String>()
    val runner = ExternalCommandRunner { cmd, _, _ ->
      val j = cmd.joinToString(" ")
      commands += j
      if ("forward --list" in j) {
        CommandOutput(0, "emulator-5554 tcp:18998 localabstract:adp-render-sandbox\n")
      } else {
        CommandOutput(0, "OK\n")
      }
    }
    var pings = 0
    val dm =
      DeviceManager(
        cacheRoot = tmp.resolve("c"),
        env = env,
        runner = runner,
        pingFn = {
          pings++
          if (pings == 1) stuckPing() else DevicePingResponse(pid = 7)
        },
        sleepFn = {},
      )
    assertTrue(dm.restartSandboxOnSerial("emulator-5554"))
    assertTrue(commands.any { "am force-stop ${DeviceManager.HOST_PACKAGE}" in it }, "$commands")
  }

  @Test
  fun `a busy but within budget sandbox is not force-stopped on restart`(@TempDir tmp: Path) {
    val env = fakeToolsEnv(tmp)
    val commands = mutableListOf<String>()
    val runner = ExternalCommandRunner { cmd, _, _ ->
      val j = cmd.joinToString(" ")
      commands += j
      if ("forward --list" in j) {
        CommandOutput(0, "emulator-5554 tcp:18998 localabstract:adp-render-sandbox\n")
      } else {
        CommandOutput(0, "OK\n")
      }
    }
    val dm =
      DeviceManager(
        cacheRoot = tmp.resolve("c"),
        env = env,
        runner = runner,
        pingFn = {
          DevicePingResponse(
            pid = 7,
            inFlightOp = "RENDER_BATCH",
            inFlightAgeMs = 10,
            inFlightBudgetMs = 15_000,
          )
        },
        sleepFn = {},
      )
    assertTrue(dm.restartSandboxOnSerial("emulator-5554"))
    assertFalse(commands.any { "force-stop" in it })
  }
}
