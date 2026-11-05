package bot.charlies.androiddesign.preview.app

import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/** Starts the instance as a process that outlives the agent process that asked for it. */
object InstanceLauncher {
  const val INSTANCE_ARG = "--instance"

  private const val PACKAGED_SUFFIX = "/Contents/MacOS/AndroidDesignPreview"
  private const val WINDOWS_EXE = "AndroidDesignPreview.exe"

  /**
   * The command that starts the instance. On macOS the packaged app goes through `open`, which
   * hands the process to launchd so it has no link to the caller; a JVM run from Gradle is
   * relaunched with the caller's classpath and main class. On Windows both go through `cmd /c
   * start`: `cmd` exits at once, so the instance is left with no live parent and a harness that
   * stops its MCP child's process tree does not reach it. A JVM from Gradle is relaunched as
   * `javaw` (no console window) and reads its classpath from [argFile], because `cmd` caps a
   * command line at 8191 characters.
   */
  fun command(
    executable: String,
    classPath: String,
    mainClass: String,
    windows: Boolean = false,
    argFile: () -> Path = { error("no argument file") },
  ): List<String> =
    when {
      windows && executable.endsWith("\\$WINDOWS_EXE", ignoreCase = true) ->
        detachOnWindows(executable, INSTANCE_ARG)
      windows -> {
        val javaw =
          executable.replace(Regex("java(\\.exe)?$", RegexOption.IGNORE_CASE), "javaw.exe")
        detachOnWindows(javaw, "@${argFile()}")
      }
      executable.endsWith(PACKAGED_SUFFIX) ->
        listOf(
          "/usr/bin/open",
          "-a",
          executable.removeSuffix(PACKAGED_SUFFIX),
          "--args",
          INSTANCE_ARG,
        )
      else -> listOf(executable, "-cp", classPath, mainClass, INSTANCE_ARG)
    }

  /** `start`'s first quoted argument is the window title, so it gets an empty one. */
  private fun detachOnWindows(vararg command: String): List<String> =
    listOf("cmd.exe", "/c", "start", "") + command

  /**
   * A `java @file` argument file that relaunches [mainClass] as the instance. Backslashes are
   * escaped because the launcher reads them as escapes inside quotes.
   */
  fun argFileContent(classPath: String, mainClass: String): String {
    val quoted = "\"" + classPath.replace("\\", "\\\\") + "\""
    return listOf("-cp", quoted, mainClass, INSTANCE_ARG).joinToString("\n")
  }

  /** Starts the instance with no stdio attached. */
  fun spawn() {
    val executable =
      ProcessHandle.current().info().command().orElseThrow {
        IllegalStateException("cannot find this process's executable")
      }
    val mainClass = System.getProperty("sun.java.command").substringBefore(' ')
    val classPath = System.getProperty("java.class.path")
    val command =
      command(executable, classPath, mainClass, Os.isWindows) {
        Files.createTempFile("android-design-preview", ".args").also {
          it.toFile().deleteOnExit()
          Files.writeString(it, argFileContent(classPath, mainClass))
        }
      }
    ProcessBuilder(command)
      .redirectInput(ProcessBuilder.Redirect.from(File(if (Os.isWindows) "NUL" else "/dev/null")))
      .redirectOutput(ProcessBuilder.Redirect.DISCARD)
      .redirectError(ProcessBuilder.Redirect.DISCARD)
      .start()
  }

  /**
   * Whether the owner started this process rather than an agent's harness. On macOS, Finder and
   * `open` start it from launchd, whose pid is 1. On Windows, Explorer starts it from the Start
   * menu, a shortcut or a double click. A parent that already exited counts as the owner.
   */
  fun launchedByUser(windows: Boolean, parentPid: Long?, parentCommand: String?): Boolean =
    when {
      parentPid == null -> true
      windows ->
        parentCommand?.substringAfterLast('\\')?.equals("explorer.exe", ignoreCase = true) == true
      else -> parentPid == 1L
    }
}
