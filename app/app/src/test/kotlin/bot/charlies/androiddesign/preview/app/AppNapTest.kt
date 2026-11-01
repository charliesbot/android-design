package bot.charlies.androiddesign.preview.app

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintStream
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppNapTest {
  private val id = "bot.example.app"
  private val read = listOf("defaults", "read", id, "NSAppSleepDisabled")
  private val write = listOf("defaults", "write", id, "NSAppSleepDisabled", "-bool", "YES")
  private val calls = mutableListOf<List<String>>()
  private val logged = mutableListOf<String>()
  private val stdout = System.out
  private val captured = ByteArrayOutputStream()

  private fun optOut(
    os: String = "Mac OS X",
    bundleId: String? = id,
    shell: (List<String>) -> CommandResult,
  ) =
    AppNap.optOut(
      os,
      bundleId,
      { command ->
        calls += command
        shell(command)
      },
      { logged += it },
    )

  @AfterTest
  fun restoreStdout() {
    System.setOut(stdout)
  }

  @Test
  fun `a preference that is already set is not written again`() {
    optOut { CommandResult(0, "1\n") }

    assertEquals(listOf(read), calls)
  }

  @Test
  fun `a missing or different preference is written`() {
    optOut { if (it == read) CommandResult(1, "") else CommandResult(0, "") }
    assertEquals(listOf(read, write), calls)

    calls.clear()
    optOut { if (it == read) CommandResult(0, "0\n") else CommandResult(0, "") }
    assertEquals(listOf(read, write), calls)
  }

  @Test
  fun `a failure never reaches the caller or stdout`() {
    System.setOut(PrintStream(captured, true))

    optOut { throw IOException("no defaults here") }
    optOut { if (it == read) CommandResult(1, "") else CommandResult(1, "denied") }

    assertEquals("", captured.toString(), "stdout carries only JSON-RPC")
    assertTrue(logged.isNotEmpty(), "failures go to the log")
  }

  @Test
  fun `nothing runs off macOS or outside the packaged app`() {
    optOut(os = "Linux") { CommandResult(0, "") }
    optOut(bundleId = null) { CommandResult(0, "") }

    assertEquals(emptyList(), calls)
  }
}
