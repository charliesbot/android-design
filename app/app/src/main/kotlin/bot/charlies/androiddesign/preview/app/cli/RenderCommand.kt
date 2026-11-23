package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.app.client.V5ExitCode
import bot.charlies.androiddesign.preview.app.server.BoardMeta
import bot.charlies.androiddesign.preview.app.server.CompileEntry
import bot.charlies.androiddesign.preview.app.server.ContentHasher
import bot.charlies.androiddesign.preview.app.server.DirectionMeta
import bot.charlies.androiddesign.preview.app.server.PreviewBackend
import bot.charlies.androiddesign.preview.app.server.RobolectricPreviewBackend
import bot.charlies.androiddesign.preview.app.server.RuntimeIdentity
import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.model.BoardSurface
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PhoneSurface
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.SurfaceSpec
import bot.charlies.androiddesign.preview.model.ValidationError
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.WearSurface
import java.io.PrintStream
import java.nio.charset.CharacterCodingException
import java.nio.file.Files
import java.nio.file.Path

/**
 * `render <exploration.explore.kt> --out <dir> [--design <DESIGN.md>] [--views a,b]
 * [--screens a,b]`
 *
 * Compiles and draws each exploration screen and overlay at each view headlessly via
 * [RobolectricPreviewBackend], writing `<screen>-<view>.png`, `<screen>-<view>.tree.json`, and
 * `report.json` into `<dir>`. Exit 0 when valid and every render passes, 1 when a render has error
 * findings or threw, 2 when the exploration fails validation/compilation, and 3 when the bootstrap
 * classpath is missing.
 */
class RenderCommand(
  private val backendFactory: (() -> PreviewBackend)? = null,
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

  private data class ScreenTarget(
    val direction: DirectionMeta,
    val screenId: String,
    val prefix: String,
  )

  override fun run(args: List<String>, out: PrintStream, err: PrintStream): Int {
    if (args.any { it == "--help" || it == "-h" }) {
      out.println(
        """
        Usage: android-design-preview $usage

        Compile and render every screen, overlay, and view headlessly into <dir>, writing
        <screen>-<view>.png, <screen>-<view>.tree.json, and report.json.

        Exit codes:
          0  Valid and every render passed
          1  One or more renders failed checks or threw
          2  Invalid exploration or compilation error
          3  Missing Robolectric bootstrap classpath
        """
          .trimIndent(),
      )
      return ExitCode.OK
    }
    val options = parse(args, err) ?: return ExitCode.USAGE
    val sourcePath = options.source.toAbsolutePath().normalize()
    if (!Files.isRegularFile(sourcePath)) {
      err.println("no such file: $sourcePath")
      return ExitCode.USAGE
    }
    Files.createDirectories(options.out)
    val explicitDesign = options.design?.toAbsolutePath()?.normalize()
    if (!sourcePath.fileName.toString().endsWith(".explore.kt")) {
      val error = ValidationError(sourcePath.toString(), "Expected an .explore.kt exploration file")
      writeReport(
        outDir = options.out,
        sourcePath = sourcePath,
        designPath = explicitDesign,
        system = DesignSystemResolver.resolve(null, null),
        errors = listOf(error),
        renders = emptyList(),
      )
      err.println("invalid: ${error.path}: ${error.message}")
      return ExitCode.INVALID
    }

    try {
      Files.readString(sourcePath, Charsets.UTF_8)
    } catch (e: CharacterCodingException) {
      val error =
        ValidationError(
          sourcePath.toString(),
          "File is not valid UTF-8: ${e.message ?: e::class.simpleName}",
        )
      writeReport(
        outDir = options.out,
        sourcePath = sourcePath,
        designPath = explicitDesign,
        system = DesignSystemResolver.resolve(null, null),
        errors = listOf(error),
        renders = emptyList(),
      )
      err.println("invalid: ${error.path}: ${error.message}")
      return ExitCode.INVALID
    }

    val designLoaded = DesignMdLoader.load(sourcePath, options.design)
    if (designLoaded.errors.isNotEmpty()) {
      writeReport(
        outDir = options.out,
        sourcePath = sourcePath,
        designPath = designLoaded.path,
        system = DesignSystemResolver.resolve(designLoaded.theme, null),
        errors = designLoaded.errors,
        renders = emptyList(),
      )
      designLoaded.errors.forEach { err.println("invalid: ${it.path}: ${it.message}") }
      return ExitCode.INVALID
    }

    val backend =
      backendFactory?.invoke() ?: createRobolectricBackend(err) ?: return V5ExitCode.NOT_READY
    return try {
      renderWithBackend(
        backend = backend,
        sourcePath = sourcePath,
        options = options,
        err = err,
      )
    } finally {
      backend.close()
    }
  }

  private fun renderWithBackend(
    backend: PreviewBackend,
    sourcePath: Path,
    options: Options,
    err: PrintStream,
  ): Int {
    val snapshot = ContentHasher.snapshot(sourcePath, options.design)
    val compiled = backend.compile(snapshot)
    val meta: BoardMeta? = compiled.boardMeta
    if (compiled.loaded.errors.isNotEmpty() || meta == null) {
      val errors =
        compiled.loaded.errors.ifEmpty {
          listOf(
            ValidationError(
              sourcePath.toString(),
              "Exploration compilation returned no board metadata",
            ),
          )
        }
      writeReport(
        outDir = options.out,
        sourcePath = sourcePath,
        designPath = compiled.loaded.designPath,
        system = compiled.loaded.designSystem,
        errors = errors,
        renders = emptyList(),
      )
      errors.forEach { err.println("invalid: ${it.path}: ${it.message}") }
      return ExitCode.INVALID
    }

    val surface: SurfaceSpec =
      if (compiled.surface == BoardSurface.Wear) WearSurface else PhoneSurface
    val views = resolveViews(options.views, surface, err) ?: return ExitCode.USAGE
    val requestedScreens = options.screens
    if (requestedScreens != null) {
      val known =
        meta.directions
          .flatMap { dir ->
            dir.screens.flatMap { sid ->
              listOf(sid, "${dir.name}/$sid", "${sanitizeSegment(dir.name)}__$sid")
            }
          }
          .toSet()
      val unknown = requestedScreens.filter { it !in known }
      if (unknown.isNotEmpty()) {
        val allNames = meta.directions.flatMap { d -> d.screens.map { "${d.name}/$it" } }
        err.println(
          "no such screen: ${unknown.joinToString()} (screens: ${allNames.joinToString()})",
        )
        return ExitCode.USAGE
      }
    }

    val targets = resolveTargets(meta.directions, requestedScreens)
    val entry =
      CompileEntry(
        sourcePath = sourcePath,
        designPath = compiled.loaded.designPath,
        contentHash = snapshot.contentHash,
        loaded = compiled.loaded,
        fromCache = false,
        compileCountAtLoad = 1,
        dexHash = compiled.dexHash,
        surface = compiled.surface,
        boardName = compiled.boardName,
        boardMeta = meta,
        androidData = compiled.androidData,
        boardTheme = compiled.boardTheme,
      )

    val entries = mutableListOf<RenderEntry>()
    for (target in targets) {
      val baseChecks = ArrayList<ViewCheck>(views.size)
      for (view in views) {
        val rendered =
          renderSingleTarget(
            backend = backend,
            entry = entry,
            direction = target.direction.name,
            screenId = target.screenId,
            prefix = target.prefix,
            overlayId = null,
            view = view,
            baseCheck = null,
            outDir = options.out,
          )
        baseChecks += rendered.first
        entries += rendered.second
      }
      val overlays = target.direction.overlaysFor(target.screenId)
      for (overlay in overlays) {
        views.forEachIndexed { index, view ->
          val (_, renderEntry) =
            renderSingleTarget(
              backend = backend,
              entry = entry,
              direction = target.direction.name,
              screenId = target.screenId,
              prefix = target.prefix,
              overlayId = overlay.id,
              view = view,
              baseCheck = baseChecks[index],
              outDir = options.out,
            )
          entries += renderEntry
        }
      }
    }

    val report =
      writeReport(
        outDir = options.out,
        sourcePath = sourcePath,
        designPath = compiled.loaded.designPath,
        system = compiled.loaded.designSystem,
        errors = emptyList(),
        renders = entries,
      )
    entries
      .filter { !it.pass }
      .forEach { e ->
        err.println("${e.screen}/${e.view}: ${e.failures.joinToString("; ")}")
      }
    return if (report.pass) ExitCode.OK else ExitCode.FINDINGS_FAILED
  }

  private fun renderSingleTarget(
    backend: PreviewBackend,
    entry: CompileEntry,
    direction: String,
    screenId: String,
    prefix: String,
    overlayId: String?,
    view: ViewSpec,
    baseCheck: ViewCheck?,
    outDir: Path,
  ): Pair<ViewCheck, RenderEntry> {
    val artifactName = overlayId?.let { ViewCheck.renderOverlayName(prefix, it) } ?: prefix
    val artifact =
      try {
        backend.renderSingle(
          entry = entry,
          direction = direction,
          screenId = screenId,
          prefix = prefix,
          overlayId = overlayId,
          view = view,
          densityScale = 2,
          designSystem = entry.loaded.designSystem,
          baseCheck = baseCheck,
          writeFiles = false,
          rendersRoot = outDir,
          frameFormat = FrameFormat.LZ4_RGBA,
        )
      } catch (e: Exception) {
        val failed = ViewCheck.renderError(e)
        return failed to RenderEntry.of(artifactName, view.name, null, failed)
      }

    val tree = artifact.tree
    val png = artifact.pngBytes
    if (tree == null || png == null || png.isEmpty()) {
      return artifact.check to RenderEntry.of(artifactName, view.name, null, artifact.check)
    }

    val filesBase = "$artifactName-${view.name}"
    writeArtifacts(outDir, filesBase, png, tree)
    val endPng = artifact.endPngBytes
    val endTree = artifact.endTree
    val endFiles =
      if (endPng != null && endPng.isNotEmpty() && endTree != null) {
        val endBase = "$filesBase-end"
        writeArtifacts(outDir, endBase, endPng, endTree)
        endBase
      } else {
        null
      }
    return artifact.check to
      RenderEntry.of(artifactName, view.name, filesBase, artifact.check, endFiles)
  }

  private fun createRobolectricBackend(err: PrintStream): PreviewBackend? {
    val meta = BootstrapMeta.locateOrNull()
    val cpOverride =
      System.getenv(RuntimeIdentity.ROBOLECTRIC_CP_ENV)?.takeIf { it.isNotBlank() }?.let(Path::of)
    val robolectricCp = cpOverride ?: meta?.bootstrapDir?.resolve("robolectric.cp")
    if (robolectricCp == null || !Files.isRegularFile(robolectricCp)) {
      err.println(
        "Missing robolectric.cp bootstrap artifact; run `./gradlew :host:exportBootstrap` from app/.",
      )
      return null
    }
    return RobolectricPreviewBackend(
      meta = meta,
      robolectricCp = robolectricCp,
      bootstrapDir = meta?.bootstrapDir ?: robolectricCp.toAbsolutePath().normalize().parent,
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

  private fun resolveTargets(
    directions: List<DirectionMeta>,
    screenFilter: List<String>?,
  ): List<ScreenTarget> {
    val singleDirection = directions.size == 1
    val filterSet = screenFilter?.toSet()
    return buildList {
      for (dir in directions) {
        for (screenId in dir.screens) {
          val prefix = if (singleDirection) screenId else "${sanitizeSegment(dir.name)}__$screenId"
          val slashName = "${dir.name}/$screenId"
          if (
            filterSet == null ||
              screenId in filterSet ||
              prefix in filterSet ||
              slashName in filterSet
          ) {
            add(ScreenTarget(dir, screenId, prefix))
          }
        }
      }
    }
  }

  private fun resolveViews(
    requested: List<String>?,
    surface: SurfaceSpec,
    err: PrintStream,
  ): List<ViewSpec>? =
    requested?.map { name ->
      surface.views.firstOrNull { it.name == name }
        ?: run {
          err.println(
            "unknown view '$name' (the ${surface.key} views: ${surface.views.joinToString { it.name }})",
          )
          return null
        }
    } ?: surface.views

  private fun writeArtifacts(dir: Path, base: String, png: ByteArray, tree: LayoutTree) {
    Files.write(dir.resolve("$base.png"), png)
    Files.writeString(
      dir.resolve("$base.tree.json"),
      PreviewJson.pretty.encodeToString(LayoutTree.serializer(), tree) + "\n",
    )
  }

  private fun writeReport(
    outDir: Path,
    sourcePath: Path,
    designPath: Path?,
    system: DesignSystem,
    errors: List<ValidationError>,
    renders: List<RenderEntry>,
  ): RenderReport {
    val valid = errors.isEmpty()
    val report =
      RenderReport(
        proposal = sourcePath.toString(),
        design = designPath?.toString(),
        theme = ReportTheme(system.seed, system.variant.wire, system.contrast),
        valid = valid,
        errors = errors,
        renders = renders,
        pass = valid && renders.all { it.pass },
      )
    Files.createDirectories(outDir)
    Files.writeString(
      outDir.resolve("report.json"),
      PreviewJson.pretty.encodeToString(RenderReport.serializer(), report) + "\n",
    )
    return report
  }

  private fun sanitizeSegment(name: String): String =
    name.trim().replace(Regex("""[^A-Za-z0-9._-]+"""), "_").ifBlank { "direction" }
}
