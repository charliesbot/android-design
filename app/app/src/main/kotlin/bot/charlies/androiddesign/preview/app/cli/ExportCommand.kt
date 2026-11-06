package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.app.export.KotlinExport
import bot.charlies.androiddesign.preview.data.ProposalReader
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path

/**
 * `export <proposal.json> [--design <DESIGN.md>] [--screen id]`
 *
 * Prints Jetpack Compose source (Wear Compose for a Wear proposal) for one screen, or for every
 * screen with the navigation host, to stdout. Exit 0 on success, 2 when the proposal is invalid
 * (the errors go to stderr, stdout stays empty), 64 for usage mistakes.
 */
object ExportCommand : Command {
  override val name = "export"
  override val usage = "export <proposal.json> [--design <DESIGN.md>] [--screen id]"

  override fun run(args: List<String>, out: PrintStream, err: PrintStream): Int {
    val parsedArgs = parseArgs(args, setOf("--design", "--screen"), err) ?: return ExitCode.USAGE
    val proposal = parsedArgs.positional
    val design = parsedArgs.flags["--design"]
    val screen = parsedArgs.flags["--screen"]
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
    out.print(KotlinExport.of(parsed, loaded.designSystem, screen))
    out.flush()
    return ExitCode.OK
  }
}
