package bot.charlies.androiddesign.preview.host.robolectric

import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.RobolectricRenderRequest
import bot.charlies.androiddesign.preview.model.RobolectricRenderResponse
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import kotlin.system.exitProcess
import org.junit.Test
import org.junit.runner.JUnitCore
import org.junit.runner.Request
import org.junit.runner.RunWith
import org.junit.runners.model.FrameworkMethod
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.internal.bytecode.InstrumentationConfiguration

/**
 * Custom [RobolectricTestRunner] keeping `kotlinc`, `:core:*`, and JSON stdio bridge uninstrumented
 * so compiled `.explore.kt` code runs inside Robolectric's SDK sandbox with shared domain models.
 */
class SandboxExcludingRobolectricTestRunner(testClass: Class<*>) :
  RobolectricTestRunner(testClass) {
  override fun createClassLoaderConfig(method: FrameworkMethod): InstrumentationConfiguration {
    val base = super.createClassLoaderConfig(method)
    return InstrumentationConfiguration.Builder(base)
      .doNotAcquirePackage("org.jetbrains.kotlin.")
      .doNotAcquirePackage("org.jetbrains.annotations.")
      .doNotAcquirePackage("com.intellij.")
      .doNotAcquirePackage("javax.xml.")
      .doNotAcquirePackage("org.w3c.")
      .doNotAcquirePackage("org.xml.")
      .doNotAcquirePackage("kotlinx.serialization.")
      .doNotAcquirePackage("com.charleskorn.kaml.")
      .doNotAcquirePackage("bot.charlies.androiddesign.preview.model.")
      .doNotAcquirePackage("bot.charlies.androiddesign.preview.domain.")
      .doNotAcquirePackage("bot.charlies.androiddesign.preview.data.")
      .doNotAcquirePackage("bot.charlies.androiddesign.preview.app.")
      .doNotAcquireClass("bot.charlies.androiddesign.preview.host.robolectric.RunnerStdioBridge")
      .build()
  }
}

/** Shared outside Robolectric bytecode acquisition so stdout carries only JSON protocol lines. */
object RunnerStdioBridge {
  @Volatile var protocolOut: PrintStream? = null
  @Volatile var singleRequestJson: String? = null
  @Volatile var lastFailure: Throwable? = null
  private val stdinReader = BufferedReader(InputStreamReader(System.`in`, StandardCharsets.UTF_8))

  fun nextRequestLine(): String? {
    singleRequestJson?.let { req ->
      singleRequestJson = null
      return req
    }
    return stdinReader.readLine()
  }

  fun writeResponseLine(json: String) {
    val out = protocolOut ?: System.out
    out.println(json)
    out.flush()
  }
}

@RunWith(SandboxExcludingRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class RobolectricRunnerEntry {
  @Test
  fun serveLoop() {
    if (RunnerStdioBridge.protocolOut == null) return
    while (true) {
      val line = RunnerStdioBridge.nextRequestLine() ?: break
      val trimmed = line.trim()
      if (trimmed.isEmpty()) continue
      val response =
        try {
          val req =
            PreviewJson.compact.decodeFromString(RobolectricRenderRequest.serializer(), trimmed)
          RobolectricStaging.handle(req)
        } catch (t: Throwable) {
          RunnerStdioBridge.lastFailure = t
          RobolectricRenderResponse(
            id = "0",
            ok = false,
            valid = false,
            pass = false,
            error = "${t::class.java.name}: ${t.message}",
          )
        }
      RunnerStdioBridge.writeResponseLine(
        PreviewJson.compact.encodeToString(RobolectricRenderResponse.serializer(), response),
      )
    }
  }
}

/**
 * Persistent headless Robolectric Native Graphics runner launched via `@robolectric.cp`. Redirects
 * Robolectric and Android runtime `System.out` logs to `System.err` while writing one compact JSON
 * line per stdin request to protocol stdout.
 */
object RobolectricRunnerMain {
  @JvmStatic
  fun main(args: Array<String>) {
    val realOut = System.out
    System.setOut(System.err)
    RunnerStdioBridge.protocolOut = realOut

    val onceIdx = args.indexOf("--once")
    if (onceIdx >= 0) {
      val jsonArg =
        args.getOrNull(onceIdx + 1)
          ?: error("--once requires a JSON RobolectricRenderRequest argument")
      RunnerStdioBridge.singleRequestJson = jsonArg
    }

    val result = JUnitCore().run(Request.method(RobolectricRunnerEntry::class.java, "serveLoop"))
    if (!result.wasSuccessful()) {
      result.failures.forEach { System.err.println(it.trace) }
      exitProcess(1)
    }
    exitProcess(0)
  }
}
