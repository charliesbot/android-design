package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.data.ProposalReader
import bot.charlies.androiddesign.preview.domain.export.KotlinExporter
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path

/**
 * `export <proposal.json> [--design <DESIGN.md>] [--screen id]`
 *
 * Prints Jetpack Compose source for one screen, or for every screen with the navigation host, to
 * stdout. Exit 0 on success, 2 when the proposal is invalid (the errors go to stderr, stdout stays
 * empty), 64 for usage mistakes.
 */
object ExportCommand : Command {
  override val name = "export"
  override val usage = "export <proposal.json> [--design <DESIGN.md>] [--screen id]"

  override fun run(args: List<String>, out: PrintStream, err: PrintStream): Int {
    var proposal: String? = null
    var design: String? = null
    var screen: String? = null
    var index = 0
    fun value(flag: String): String? =
      args.getOrNull(++index).also { if (it == null) err.println("$flag needs a value") }
    while (index < args.size) {
      when (val arg = args[index]) {
        "--design" -> design = value(arg) ?: return ExitCode.USAGE
        "--screen" -> screen = value(arg) ?: return ExitCode.USAGE
        else ->
          if (arg.startsWith("--")) {
            err.println("unknown option $arg")
            return ExitCode.USAGE
          } else if (proposal == null) {
            proposal = arg
          } else {
            err.println("unexpected argument $arg")
            return ExitCode.USAGE
          }
      }
      index++
    }
    if (proposal == null) {
      err.println("usage: $usage")
      return ExitCode.USAGE
    }
    val file = Path.of(proposal).toAbsolutePath().normalize()
    if (!Files.isRegularFile(file)) {
      err.println("no such file: $file")
      return ExitCode.USAGE
    }

    val loaded = ProposalReader().read(file, design?.let(Path::of))
    val parsed = loaded.proposal
    if (parsed == null || !loaded.isValid) {
      loaded.errors.forEach { err.println("invalid: ${it.path}: ${it.message}") }
      return ExitCode.INVALID
    }
    if (screen != null && screen !in parsed.screens) {
      err.println("no such screen: $screen (screens: ${parsed.screens.keys.joinToString()})")
      return ExitCode.USAGE
    }
    out.print(KotlinExporter.export(parsed, loaded.designSystem, screen))
    out.flush()
    return ExitCode.OK
  }
}
