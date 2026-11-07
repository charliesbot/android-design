package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.data.ProposalReader
import bot.charlies.androiddesign.preview.domain.OverlayPlacement
import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessRenderer
import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessResult
import bot.charlies.androiddesign.preview.feature.preview.headless.RenderSource
import bot.charlies.androiddesign.preview.model.Catalog
import bot.charlies.androiddesign.preview.model.Catalogs
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.ValidationError
import bot.charlies.androiddesign.preview.model.ViewSpec
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path

/**
 * Draws one screen at one view, with [overlay] open when given; [HeadlessRenderer.image] in the
 * app, a stub in tests.
 */
fun interface ScreenRenderer {
  fun render(source: RenderSource, screen: String, view: ViewSpec, overlay: String?): HeadlessResult
}

/**
 * `render <proposal.json> --out <dir> [--design <DESIGN.md>] [--views a,b] [--screens a,b]`
 *
 * Draws each screen at each view without a window and writes `<screen>-<view>.png`,
 * `<screen>-<view>.tree.json` and `report.json` into `<dir>`. Each overlay of a screen is one more
 * render with it open, named `<screen>+<overlay>`, whose failures leave out what the plain screen
 * at that view already reports. A scrolling Wear screen is drawn at the top of its list with a tree
 * of the whole list, and also writes `<screen>-<view>-end.png` and `-end.tree.json`, the list
 * scrolled to its end. Exit 0 when the proposal is valid and every render passes, 1 when a render
 * has error findings or threw, 2 when the proposal is invalid.
 *
 * A render that throws does not stop the run: the report lists it as failed with a `render-error`
 * failure and no files, and the rest are still drawn.
 */
class RenderCommand(
  private val renderer: ScreenRenderer = ScreenRenderer { source, screen, view, overlay ->
    HeadlessRenderer.image(source, screen, view, overlay = overlay)
  },
) : Command {
  override val name = "render"
  override val usage =
    "render <proposal.json> --out <dir> [--design <DESIGN.md>] [--views light,dark,...] [--screens a,b]"

  private class Options(
    val proposal: Path,
    val out: Path,
    val design: Path?,
    /** The view names asked for, or null for every view of the proposal's catalog. */
    val views: List<String>?,
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
    val catalog = Catalogs.of(proposal)
    val views = views(options, catalog, err) ?: return ExitCode.USAGE
    val screens = options.screens ?: proposal.screens.keys.toList()
    val entries =
      renderAll(RenderSource(proposal, design, proposalPath.parent), screens, views, options)
    val report = writeReport(options, proposalPath, designPath, design, emptyList(), entries)
    entries
      .filter { !it.pass }
      .forEach { entry ->
        err.println("${entry.screen}/${entry.view}: ${entry.failures.joinToString("; ")}")
      }
    return if (report.pass) ExitCode.OK else ExitCode.FINDINGS_FAILED
  }

  /** The views to draw: those named, which must belong to [catalog], or all of its views. */
  private fun views(options: Options, catalog: Catalog, err: PrintStream): List<ViewSpec>? =
    options.views?.map { name ->
      catalog.views.firstOrNull { it.name == name }
        ?: run {
          err.println(
            "unknown view '$name' (the ${catalog.key} views: ${catalog.views.joinToString { it.name }})",
          )
          return null
        }
    } ?: catalog.views

  private fun renderAll(
    source: RenderSource,
    screens: List<String>,
    views: List<ViewSpec>,
    options: Options,
  ): List<RenderEntry> = screens.flatMap { screen ->
    val base = views.map { view -> renderOne(source, screen, null, view, options, null) }
    val overlays =
      OverlayPlacement(source.proposal.screens.getValue(screen)).overlays.flatMap { overlay ->
        views.mapIndexed { i, view ->
          renderOne(source, screen, overlay.id, view, options, base[i].check)
        }
      }
    (base + overlays).map { it.entry }
  }

  private class Rendered(val entry: RenderEntry, val check: ViewCheck)

  /**
   * One render written to the output folder; [overlay] is open in it when given, and [base] is the
   * verdict on the same screen and view without it, whose findings the overlay render does not
   * repeat.
   */
  private fun renderOne(
    source: RenderSource,
    screen: String,
    overlay: String?,
    view: ViewSpec,
    options: Options,
    base: ViewCheck?,
  ): Rendered {
    val name = overlay?.let { OverlayPlacement.renderName(screen, it) } ?: screen
    val result =
      try {
        renderer.render(source, screen, view, overlay)
      } catch (e: Exception) {
        val failed = ViewCheck.renderError(e)
        return Rendered(RenderEntry.of(name, view.name, null, failed), failed)
      }
    val files = "$name-${view.name}"
    write(options, files, result.png, result.tree)
    val endFiles =
      result.end?.let { "$files-end".also { end -> write(options, end, it.png, it.tree) } }
    val full = ViewCheck.of(source.proposal, result.tree)
    val check = if (base == null) full else full.overlaying(base)
    return Rendered(RenderEntry.of(name, view.name, files, check, endFiles), full)
  }

  private fun write(options: Options, base: String, png: ByteArray, tree: LayoutTree) {
    Files.write(options.out.resolve("$base.png"), png)
    Files.writeString(
      options.out.resolve("$base.tree.json"),
      PreviewJson.pretty.encodeToString(LayoutTree.serializer(), tree) + "\n",
    )
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
    val parsed =
      parseArgs(args, setOf("--out", "--design", "--views", "--screens"), err) ?: return null
    val proposal = parsed.positional
    val out = parsed.flags["--out"]
    val design = parsed.flags["--design"]
    val views = parsed.flags["--views"]
    val screens = parsed.flags["--screens"]
    if (proposal == null || out == null) {
      err.println("usage: $usage")
      return null
    }
    return Options(
      proposal = Path.of(proposal),
      out = Path.of(out),
      design = design?.let(Path::of),
      views = views?.split(',')?.map { it.trim() },
      screens = screens?.split(',')?.map { it.trim() },
    )
  }
}
