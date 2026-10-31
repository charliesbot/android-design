package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.domain.catalog.CatalogJson
import java.io.PrintStream

/** `catalog`: the catalog JSON on stdout. */
object CatalogCommand : Command {
  override val name = "catalog"
  override val usage = "catalog"

  override fun run(args: List<String>, out: PrintStream, err: PrintStream): Int {
    if (args.isNotEmpty()) {
      err.println("catalog takes no arguments")
      return ExitCode.USAGE
    }
    out.print(CatalogJson.render(CatalogJson.catalog()))
    out.flush()
    return ExitCode.OK
  }
}
