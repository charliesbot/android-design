package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.app.client.CheckCommand
import bot.charlies.androiddesign.preview.app.client.InstallCommand
import bot.charlies.androiddesign.preview.app.client.OpenCommand
import bot.charlies.androiddesign.preview.app.client.SelectionCommand
import bot.charlies.androiddesign.preview.app.client.StartCommand
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

/** A subcommand such as `check` or `render`. */
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

/**
 * Dispatches the first argument lazily so lightweight commands (`status`) avoid loading compilers.
 */
object Cli {
  private val commandUsages =
    listOf(
      "start [--serial <id>] [--avd <name>] [--headless] [--help]",
      "check <file> [--direction d] [--screen s] [--view v] [--design path] [--help]",
      "tree <file> --direction d --view v [--screen s] [--overlay o] [--max-depth n] [--help]",
      "selection <file> [--help]",
      "open <file> [--help]",
      "status [<file-or-dir>] [--help]",
      "stop [<file-or-dir>] [--help]",
      "render <exploration.explore.kt> --out <dir> [--design <DESIGN.md>] [--views light,dark,...] [--screens a,b]",
      "install [--bin-dir <dir>] [--help]",
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
    val command: Command =
      when (first) {
        "start" -> StartCommand()
        "check" -> CheckCommand()
        "tree" -> TreeCommand()
        "selection" -> SelectionCommand()
        "open" -> OpenCommand()
        "status" -> StatusCommand()
        "stop" -> StopCommand()
        "render" -> RenderCommand()
        "install" -> InstallCommand()
        "server" -> ServerCommand()
        else -> {
          err.println("unknown command '$first'")
          printUsage(err)
          return ExitCode.USAGE
        }
      }
    return command.run(args.drop(1), out, err)
  }

  fun printUsage(out: PrintStream) {
    out.println("usage: android-design-preview <command> [options]")
    commandUsages.forEach { out.println("  $it") }
    out.println("  --version")
    out.println("  --help")
    out.println("Run `android-design-preview <command> --help` for details and exit codes.")
  }
}
