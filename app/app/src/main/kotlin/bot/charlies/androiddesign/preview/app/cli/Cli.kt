package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.app.AppInfo
import java.io.PrintStream

/** Exit codes of the command line. 0 is success; 1 means findings failed; 2 an invalid proposal. */
object ExitCode {
  const val OK = 0
  const val FINDINGS_FAILED = 1
  const val INVALID = 2

  /** The arguments were wrong (BSD `EX_USAGE`). */
  const val USAGE = 64
}

/** A subcommand such as `render`. */
interface Command {
  val name: String
  val usage: String

  fun run(args: List<String>, out: PrintStream, err: PrintStream): Int
}

/**
 * Dispatches the first argument to its [Command]. Running with no arguments starts the app (see
 * `Main.kt`), so it never reaches here; if it does, it prints the usage.
 */
object Cli {
  private val commands: List<Command> = listOf(RenderCommand(), ExportCommand, CatalogCommand)

  fun run(args: List<String>, out: PrintStream, err: PrintStream): Int {
    val first = args.firstOrNull()
    if (first == null) {
      printUsage(err)
      return ExitCode.USAGE
    }
    if (first == "--version") {
      out.println(AppInfo.VERSION)
      return ExitCode.OK
    }
    val command = commands.firstOrNull { it.name == first }
    if (command == null) {
      err.println("unknown command '$first'")
      printUsage(err)
      return ExitCode.USAGE
    }
    return command.run(args.drop(1), out, err)
  }

  fun printUsage(err: PrintStream) {
    err.println("usage:")
    commands.forEach { err.println("  ${it.usage}") }
    err.println("  --version")
  }
}
