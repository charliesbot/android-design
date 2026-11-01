package bot.charlies.androiddesign.preview.app

import java.util.concurrent.TimeUnit

/** What a finished command printed and the code it exited with. */
class CommandResult(val exitCode: Int, val output: String)

/**
 * Keeps macOS from napping the app.
 *
 * About half a minute after its window is hidden or covered, macOS lowers the priority of the
 * process to the background class (App Nap), and everything it computes runs about five times
 * slower. The server is idle between tool calls and then asked for an answer at once, so the first
 * `check_proposal` after a pause paid for it: 0.2 s awake, 1 to 2 s napped. The
 * `NSAppSleepDisabled` preference in the app's own domain opts a process out, and takes effect in
 * the running process once it is written. No Info.plist key does it for a process that is started
 * directly.
 */
object AppNap {
  /** Names the app's preferences domain and its bundle; the packaging passes it in. */
  const val BUNDLE_ID_PROPERTY = "app.bundleId"

  private const val KEY = "NSAppSleepDisabled"
  private const val COMMAND_TIMEOUT_SECONDS = 5L

  /** Opts the packaged app out of App Nap without holding up startup. Failures are ignored. */
  fun optOutInBackground() {
    Thread {
      optOut(System.getProperty("os.name"), System.getProperty(BUNDLE_ID_PROPERTY), ::run) {
        System.err.println(it)
      }
    }
      .apply {
        isDaemon = true
        name = "app-nap-opt-out"
      }
      .start()
  }

  /**
   * Writes the preference unless it is already 1, so a repeat launch writes nothing. It does
   * nothing off macOS, and with no [bundleId], which only the packaged app has: a run from Gradle
   * or an IDE has no preferences domain of its own. Nothing here throws; what goes wrong is passed
   * to [log], which must not be stdout.
   */
  fun optOut(
    osName: String,
    bundleId: String?,
    shell: (List<String>) -> CommandResult,
    log: (String) -> Unit,
  ) {
    if (!osName.startsWith("Mac") || bundleId == null) return
    try {
      val current = shell(listOf("defaults", "read", bundleId, KEY))
      if (current.exitCode == 0 && current.output.trim() == "1") return
      val written = shell(listOf("defaults", "write", bundleId, KEY, "-bool", "YES"))
      if (written.exitCode != 0) log("could not opt out of App Nap: ${written.output.trim()}")
    } catch (e: Exception) {
      log("could not opt out of App Nap: ${e.message}")
    }
  }

  private fun run(command: List<String>): CommandResult {
    val process = ProcessBuilder(command).redirectErrorStream(true).start()
    if (!process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
      process.destroyForcibly()
      throw java.io.IOException("${command.first()} did not finish")
    }
    return CommandResult(process.exitValue(), process.inputStream.bufferedReader().readText())
  }
}
