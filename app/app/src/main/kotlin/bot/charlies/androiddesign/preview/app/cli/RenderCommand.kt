package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.model.ValidationError
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path

/**
 * `render <exploration.explore.kt> --out <dir> [--design <DESIGN.md>] [--views a,b]
 * [--screens a,b]`
 *
 * Compiles and draws each exploration screen and overlay at each view without a window, writing
 * `<screen>-<view>.png`, `<screen>-<view>.tree.json`, and `report.json` into `<dir>`. Exit 0 when
 * valid and every render passes, 1 when a render has error findings or threw, 2 when the
 * exploration fails validation/compilation.
 */
class RenderCommand(
  private val targetRenderer: ExploreTargetRenderer = ExploreRenderRunner.defaultRenderer,
) : Command {
  override val name = "render"
  override val usage =
    "render <exploration.explore.kt> --out <dir> [--design <DESIGN.md>] [--views light,dark,...] [--screens a,b]"

  private class Options(
    val source: Path,
    val out: Path,
    val design: Path?,
    val views: List<String>?,
    val screens: List<String>?,
  )

  override fun run(args: List<String>, out: PrintStream, err: PrintStream): Int {
    val options = parse(args, err) ?: return ExitCode.USAGE
    val sourcePath = options.source.toAbsolutePath().normalize()
    if (!Files.isRegularFile(sourcePath)) {
      err.println("no such file: $sourcePath")
      return ExitCode.USAGE
    }
    Files.createDirectories(options.out)
    if (!sourcePath.fileName.toString().endsWith(".explore.kt")) {
      val error = ValidationError(sourcePath.toString(), "Expected an .explore.kt exploration file")
      ExploreRenderRunner.writeReport(
        outDir = options.out,
        sourcePath = sourcePath,
        designPath = options.design?.toAbsolutePath()?.normalize(),
        system = DesignSystemResolver.resolve(null, null),
        errors = listOf(error),
        renders = emptyList(),
      )
      err.println("invalid: ${error.path}: ${error.message}")
      return ExitCode.INVALID
    }

    return ExploreRenderRunner.runCliRender(
      sourcePath = sourcePath,
      outDir = options.out,
      explicitDesign = options.design,
      requestedViews = options.views,
      requestedScreens = options.screens,
      err = err,
      targetRenderer = targetRenderer,
    )
  }

  private fun parse(args: List<String>, err: PrintStream): Options? {
    val parsed =
      parseArgs(args, setOf("--out", "--design", "--views", "--screens"), err) ?: return null
    val source = parsed.positional
    val out = parsed.flags["--out"]
    val design = parsed.flags["--design"]
    val views = parsed.flags["--views"]
    val screens = parsed.flags["--screens"]
    if (source == null || out == null) {
      err.println("usage: $usage")
      return null
    }
    return Options(
      source = Path.of(source),
      out = Path.of(out),
      design = design?.let(Path::of),
      views = views?.split(',')?.map { it.trim() },
      screens = screens?.split(',')?.map { it.trim() },
    )
  }
}
