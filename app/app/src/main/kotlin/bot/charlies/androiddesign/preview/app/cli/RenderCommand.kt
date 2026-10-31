package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.data.ProposalReader
import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessRenderer
import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessResult
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.Severity
import bot.charlies.androiddesign.preview.model.ValidationError
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path

/** Draws one screen at one view; [HeadlessRenderer.render] in the app, a stub in tests. */
fun interface ScreenRenderer {
  fun render(
    proposal: Proposal,
    design: DesignSystem,
    proposalDir: Path?,
    screen: String,
    view: ViewSpec,
  ): HeadlessResult
}

/**
 * `render <proposal.json> --out <dir> [--design <DESIGN.md>] [--views a,b] [--screens a,b]`
 *
 * Draws each screen at each view without a window and writes `<screen>-<view>.png`,
 * `<screen>-<view>.tree.json` and `report.json` into `<dir>`. Exit 0 when the proposal is valid and
 * every render passes, 1 when a render has error findings or threw, 2 when the proposal is invalid.
 *
 * A render that throws does not stop the run: the report lists it as failed with a `render-error`
 * failure and no files, and the rest are still drawn.
 */
class RenderCommand(
  private val renderer: ScreenRenderer = ScreenRenderer(HeadlessRenderer::render),
) : Command {
  override val name = "render"
  override val usage =
    "render <proposal.json> --out <dir> [--design <DESIGN.md>] [--views light,dark,...] [--screens a,b]"

  private class Options(
    val proposal: Path,
    val out: Path,
    val design: Path?,
    val views: List<ViewSpec>,
    val screens: List<String>?,
  )

  override fun run(args: List<String>, out: PrintStream, err: PrintStream): Int {
    val options = parse(args, err) ?: return ExitCode.USAGE
    val proposalPath = options.proposal.toAbsolutePath().normalize()
    if (!Files.isRegularFile(proposalPath)) {
      err.println("no such file: $proposalPath")
      return ExitCode.USAGE
    }
    Files.createDirectories(options.out)

    val loaded = ProposalReader().read(proposalPath, options.design)
    val proposal = loaded.proposal
    val design = loaded.designSystem
    val designPath = loaded.designMdPath
    if (proposal == null || !loaded.isValid) {
      writeReport(options, proposalPath, designPath, design, loaded.errors, emptyList())
      loaded.errors.forEach { err.println("invalid: ${it.path}: ${it.message}") }
      return ExitCode.INVALID
    }

    val unknown = options.screens.orEmpty().filter { it !in proposal.screens }
    if (unknown.isNotEmpty()) {
      err.println(
        "no such screen: ${unknown.joinToString()} (screens: ${proposal.screens.keys.joinToString()})",
      )
      return ExitCode.USAGE
    }
    val screens = options.screens ?: proposal.screens.keys.toList()
    val entries = renderAll(proposal, design, proposalPath.parent, screens, options)
    val report = writeReport(options, proposalPath, designPath, design, emptyList(), entries)
    entries
      .filter { !it.pass }
      .forEach { entry ->
        err.println("${entry.screen}/${entry.view}: ${entry.failures.joinToString("; ")}")
      }
    return if (report.pass) ExitCode.OK else ExitCode.FINDINGS_FAILED
  }

  private fun renderAll(
    proposal: Proposal,
    design: DesignSystem,
    proposalDir: Path?,
    screens: List<String>,
    options: Options,
  ): List<RenderEntry> = screens.flatMap { screen ->
    options.views.map { view ->
      val base = "$screen-${view.name}"
      val result =
        try {
          renderer.render(proposal, design, proposalDir, screen, view)
        } catch (e: Exception) {
          return@map RenderEntry(
            screen = screen,
            view = view.name,
            png = null,
            tree = null,
            pass = false,
            failures = listOf("render-error: ${firstLine(e)}"),
          )
        }
      Files.write(options.out.resolve("$base.png"), result.png)
      Files.writeString(
        options.out.resolve("$base.tree.json"),
        PreviewJson.pretty.encodeToString(LayoutTree.serializer(), result.tree) + "\n",
      )
      RenderEntry(
        screen = screen,
        view = view.name,
        png = "$base.png",
        tree = "$base.tree.json",
        pass = result.tree.pass,
        failures = failures(result.tree),
      )
    }
  }

  /** The first line of the message, which is what fits in `report.json` and on a terminal. */
  private fun firstLine(error: Exception): String =
    error.message?.lineSequence()?.firstOrNull()?.takeIf { it.isNotBlank() }
      ?: error::class.simpleName.orEmpty()

  private fun failures(tree: LayoutTree): List<String> =
    tree.findings
      .filter { it.severity == Severity.Error }
      .map { finding ->
        buildString {
            append(finding.rule.wire)
            append(": ")
            append(listOfNotNull(finding.id, finding.detail).joinToString(" "))
          }
          .trimEnd(' ', ':')
      }

  private fun writeReport(
    options: Options,
    proposal: Path,
    design: Path?,
    system: DesignSystem,
    errors: List<ValidationError>,
    renders: List<RenderEntry>,
  ): RenderReport {
    val valid = errors.isEmpty()
    val report =
      RenderReport(
        proposal = proposal.toString(),
        design = design?.toString(),
        theme = ReportTheme(system.seed, system.variant.wire, system.contrast),
        valid = valid,
        errors = errors,
        renders = renders,
        pass = valid && renders.all { it.pass },
      )
    Files.writeString(
      options.out.resolve("report.json"),
      PreviewJson.pretty.encodeToString(RenderReport.serializer(), report) + "\n",
    )
    return report
  }

  private fun parse(args: List<String>, err: PrintStream): Options? {
    var proposal: String? = null
    var out: String? = null
    var design: String? = null
    var views: String? = null
    var screens: String? = null
    var index = 0
    fun value(flag: String): String? =
      args.getOrNull(++index).also { if (it == null) err.println("$flag needs a value") }
    while (index < args.size) {
      when (val arg = args[index]) {
        "--out" -> out = value(arg) ?: return null
        "--design" -> design = value(arg) ?: return null
        "--views" -> views = value(arg) ?: return null
        "--screens" -> screens = value(arg) ?: return null
        else ->
          if (arg.startsWith("--")) {
            err.println("unknown option $arg")
            return null
          } else if (proposal == null) {
            proposal = arg
          } else {
            err.println("unexpected argument $arg")
            return null
          }
      }
      index++
    }
    if (proposal == null || out == null) {
      err.println("usage: $usage")
      return null
    }
    val viewSpecs =
      views?.split(',')?.map { name ->
        Views.byName(name.trim())
          ?: run {
            err.println("unknown view '$name' (views: ${Views.all.joinToString { it.name }})")
            return null
          }
      } ?: Views.all
    return Options(
      proposal = Path.of(proposal),
      out = Path.of(out),
      design = design?.let(Path::of),
      views = viewSpecs,
      screens = screens?.split(',')?.map { it.trim() },
    )
  }
}
