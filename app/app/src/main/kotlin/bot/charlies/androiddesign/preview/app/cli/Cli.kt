package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.app.client.CheckCommand
import bot.charlies.androiddesign.preview.app.client.InstallCommand
import bot.charlies.androiddesign.preview.app.client.OpenCommand
import bot.charlies.androiddesign.preview.app.client.SelectionCommand
import bot.charlies.androiddesign.preview.app.client.StatusCommand
import bot.charlies.androiddesign.preview.app.client.StopCommand
import bot.charlies.androiddesign.preview.app.client.TreeCommand
import bot.charlies.androiddesign.preview.app.server.ServerCommand
import java.io.PrintStream

/** Exit codes of the command line. 0 is success; 1 means findings failed; 2 an invalid file. */
object ExitCode {
  const val OK = 0
  const val FINDINGS_FAILED = 1
  const val INVALID = 2

  /** The arguments were wrong (BSD `EX_USAGE`). */
  const val USAGE = 64
}

/** A subcommand such as `render` or `worker`. */
interface Command {
  val name: String
  val usage: String

  fun run(args: List<String>, out: PrintStream, err: PrintStream): Int
}

/** The arguments of a command: its one positional argument and the value of each flag given. */
class ParsedArgs(val positional: String?, val flags: Map<String, String>)

/**
 * Splits [args] into one positional argument and the values of [flags], which each take a value.
 * Prints the mistake to [err] and returns null for an unknown option, a flag without a value, or a
 * second positional argument.
 */
fun parseArgs(args: List<String>, flags: Set<String>, err: PrintStream): ParsedArgs? {
  var positional: String? = null
  val values = mutableMapOf<String, String>()
  var index = 0
  while (index < args.size) {
    val arg = args[index]
    when {
      arg in flags -> {
        val value = args.getOrNull(++index)
        if (value == null) {
          err.println("$arg needs a value")
          return null
        }
        values[arg] = value
      }
      arg.startsWith("--") -> {
        err.println("unknown option $arg")
        return null
      }
      positional == null -> positional = arg
      else -> {
        err.println("unexpected argument $arg")
        return null
      }
    }
    index++
  }
  return ParsedArgs(positional, values)
}

/** Dispatches the first argument to its headless [Command]. */
object Cli {
  private val commands: List<Command> =
    listOf(
      CheckCommand(),
      TreeCommand(),
      SelectionCommand(),
      OpenCommand(),
      StatusCommand(),
      StopCommand(),
      RenderCommand(),
      InstallCommand(),
      ServerCommand(),
      WorkerCommand(),
    )

  fun run(args: List<String>, out: PrintStream, err: PrintStream): Int {
    val first = args.firstOrNull()
    if (first == null) {
      printUsage(err)
      return ExitCode.USAGE
    }
    if (first == "--help" || first == "-h") {
      printUsage(out)
      return ExitCode.OK
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

  fun printUsage(out: PrintStream) {
    out.println("usage: android-design-preview <command> [options]")
    commands
      .filter { it.name != "server" && it.name != "worker" }
      .forEach { out.println("  ${it.usage}") }
    out.println("  --version")
    out.println("  --help")
    out.println("Run `android-design-preview <command> --help` for details and exit codes.")
  }
}
