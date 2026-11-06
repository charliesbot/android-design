package bot.charlies.androiddesign.preview.app

import java.io.File

/** Starts the instance as a process that outlives the agent process that asked for it. */
object InstanceLauncher {
  const val INSTANCE_ARG = "--instance"

  private const val PACKAGED_SUFFIX = "/Contents/MacOS/AndroidDesignPreview"

  /**
   * The command that starts the instance. The packaged app goes through `open`, which hands the
   * process to launchd so it has no link to the caller; a JVM run from Gradle is relaunched with
   * the caller's classpath and main class.
   */
  fun command(
    executable: String,
    classPath: String,
    mainClass: String,
    socketOverride: String? = null,
  ): List<String> =
    if (executable.endsWith(PACKAGED_SUFFIX)) {
      // `open` does not pass its caller's environment on, so the socket override goes by `--env`.
      // A bundle that already runs would only be activated, so an override asks for a new
      // process (`-n`): each override socket has its own instance.
      listOf("/usr/bin/open") +
        listOfNotNull("-n".takeIf { socketOverride != null }) +
        listOf("-a", executable.removeSuffix(PACKAGED_SUFFIX)) +
        (socketOverride?.let { listOf("--env", "${InstanceSocket.SOCKET_ENV}=$it") }
          ?: emptyList()) +
        listOf("--args", INSTANCE_ARG)
    } else {
      listOf(executable, "-cp", classPath, mainClass, INSTANCE_ARG)
    }

  /** Starts the instance with no stdio attached. */
  fun spawn() {
    val executable =
      ProcessHandle.current().info().command().orElseThrow {
        IllegalStateException("cannot find this process's executable")
      }
    val mainClass = System.getProperty("sun.java.command").substringBefore(' ')
    ProcessBuilder(
        command(
          executable,
          System.getProperty("java.class.path"),
          mainClass,
          socketOverride = System.getenv(InstanceSocket.SOCKET_ENV)?.takeIf { it.isNotBlank() },
        ),
      )
      .redirectInput(ProcessBuilder.Redirect.from(File("/dev/null")))
      .redirectOutput(ProcessBuilder.Redirect.DISCARD)
      .redirectError(ProcessBuilder.Redirect.DISCARD)
      .start()
  }
}
