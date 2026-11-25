package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.cli.DesignMdLoader
import bot.charlies.androiddesign.preview.app.cli.ExitCode
import bot.charlies.androiddesign.preview.app.cli.RenderEntry
import bot.charlies.androiddesign.preview.app.cli.RenderReport
import bot.charlies.androiddesign.preview.app.cli.ReportTheme
import bot.charlies.androiddesign.preview.domain.ViewCheck
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
import java.nio.charset.CharacterCodingException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Compiles an exploration and writes `<screen>-<view>.png`, `<screen>-<view>.tree.json`, optional
 * `-end` files, and `report.json` into `outDir` via the provided [compile] and [renderSingle]
 * callbacks.
 */
object ReportRenderer {
  data class Result(
    val ok: Boolean,
    val valid: Boolean,
    val pass: Boolean,
    val exitCode: Int,
    val reportPath: Path? = null,
    val error: String? = null,
    val messages: List<String> = emptyList(),
  )

  private data class ScreenTarget(
    val direction: DirectionMeta,
    val screenId: String,
    val prefix: String,
  )

  fun render(
    sourcePath: Path,
    outDir: Path,
    explicitDesign: Path?,
    requestedViews: List<String>?,
    requestedScreens: List<String>?,
    compile: (ContentHasher.Snapshot) -> CompileEntry,
    renderSingle: (CompileEntry, SingleRenderRequest) -> RenderArtifact,
  ): Result {
    val normSource = sourcePath.toAbsolutePath().normalize()
    val normOut = outDir.toAbsolutePath().normalize()
    val normDesign = explicitDesign?.toAbsolutePath()?.normalize()
    Files.createDirectories(normOut)

    if (!normSource.fileName.toString().endsWith(".explore.kt")) {
      val error = ValidationError(normSource.toString(), "Expected an .explore.kt exploration file")
      writeReport(
        outDir = normOut,
        sourcePath = normSource,
        designPath = normDesign,
        system = DesignSystemResolver.resolve(null, null),
        errors = listOf(error),
        renders = emptyList(),
      )
      return Result(
        ok = false,
        valid = false,
        pass = false,
        exitCode = ExitCode.INVALID,
        reportPath = normOut.resolve("report.json"),
        error = "invalid_exploration",
        messages = listOf("invalid: ${error.path}: ${error.message}"),
      )
    }

    try {
      Files.readString(normSource, Charsets.UTF_8)
    } catch (e: CharacterCodingException) {
      val error =
        ValidationError(
          normSource.toString(),
          "File is not valid UTF-8: ${e.message ?: e::class.simpleName}",
        )
      writeReport(
        outDir = normOut,
        sourcePath = normSource,
        designPath = normDesign,
        system = DesignSystemResolver.resolve(null, null),
        errors = listOf(error),
        renders = emptyList(),
      )
      return Result(
        ok = false,
        valid = false,
        pass = false,
        exitCode = ExitCode.INVALID,
        reportPath = normOut.resolve("report.json"),
        error = "invalid_utf8",
        messages = listOf("invalid: ${error.path}: ${error.message}"),
      )
    }

    val designLoaded = DesignMdLoader.load(normSource, normDesign)
    if (designLoaded.errors.isNotEmpty()) {
      writeReport(
        outDir = normOut,
        sourcePath = normSource,
        designPath = designLoaded.path,
        system = DesignSystemResolver.resolve(designLoaded.theme, null),
        errors = designLoaded.errors,
        renders = emptyList(),
      )
      return Result(
        ok = false,
        valid = false,
        pass = false,
        exitCode = ExitCode.INVALID,
        reportPath = normOut.resolve("report.json"),
        error = "invalid_design_md",
        messages = designLoaded.errors.map { "invalid: ${it.path}: ${it.message}" },
      )
    }

    val snapshot = ContentHasher.snapshot(normSource, normDesign)
    val entry = compile(snapshot)
    val meta = entry.boardMeta
    if (entry.loaded.errors.isNotEmpty() || meta == null) {
      val errors =
        entry.loaded.errors.ifEmpty {
          listOf(
            ValidationError(
              normSource.toString(),
              "Exploration compilation returned no board metadata",
            ),
          )
        }
      writeReport(
        outDir = normOut,
        sourcePath = normSource,
        designPath = entry.loaded.designPath,
        system = entry.loaded.designSystem,
        errors = errors,
        renders = emptyList(),
      )
      return Result(
        ok = false,
        valid = false,
        pass = false,
        exitCode = ExitCode.INVALID,
        reportPath = normOut.resolve("report.json"),
        error = "compile_error",
        messages = errors.map { "invalid: ${it.path}: ${it.message}" },
      )
    }

    val surface: SurfaceSpec = if (entry.surface == BoardSurface.Wear) WearSurface else PhoneSurface
    val views =
      resolveViews(requestedViews, surface).getOrElse { err ->
        return Result(
          ok = false,
          valid = true,
          pass = false,
          exitCode = ExitCode.USAGE,
          error = "unknown_view",
          messages = listOf(err.message ?: "Unknown view"),
        )
      }
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
        return Result(
          ok = false,
          valid = true,
          pass = false,
          exitCode = ExitCode.USAGE,
          error = "unknown_screen",
          messages =
            listOf(
              "no such screen: ${unknown.joinToString()} (screens: ${allNames.joinToString()})",
            ),
        )
      }
    }

    val targets = resolveTargets(meta.directions, requestedScreens)
    val entries = mutableListOf<RenderEntry>()
    for (target in targets) {
      val baseChecks = ArrayList<ViewCheck>(views.size)
      for (view in views) {
        val (check, renderEntry) =
          renderSingleTarget(
            renderSingle = renderSingle,
            entry = entry,
            direction = target.direction.name,
            screenId = target.screenId,
            prefix = target.prefix,
            overlayId = null,
            view = view,
            baseCheck = null,
            outDir = normOut,
          )
        baseChecks += check
        entries += renderEntry
      }
      val overlays = target.direction.overlaysFor(target.screenId)
      for (overlay in overlays) {
        views.forEachIndexed { index, view ->
          val (_, renderEntry) =
            renderSingleTarget(
              renderSingle = renderSingle,
              entry = entry,
              direction = target.direction.name,
              screenId = target.screenId,
              prefix = target.prefix,
              overlayId = overlay.id,
              view = view,
              baseCheck = baseChecks[index],
              outDir = normOut,
            )
          entries += renderEntry
        }
      }
    }

    val report =
      writeReport(
        outDir = normOut,
        sourcePath = normSource,
        designPath = entry.loaded.designPath,
        system = entry.loaded.designSystem,
        errors = emptyList(),
        renders = entries,
      )
    val failures =
      entries
        .filter { !it.pass }
        .map { e -> "${e.screen}/${e.view}: ${e.failures.joinToString("; ")}" }
    return Result(
      ok = report.pass,
      valid = report.valid,
      pass = report.pass,
      exitCode = if (report.pass) ExitCode.OK else ExitCode.FINDINGS_FAILED,
      reportPath = normOut.resolve("report.json"),
      messages = failures,
    )
  }

  private fun renderSingleTarget(
    renderSingle: (CompileEntry, SingleRenderRequest) -> RenderArtifact,
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
        renderSingle(
          entry,
          SingleRenderRequest(
            direction = direction,
            screenId = screenId,
            prefix = prefix,
            overlayId = overlayId,
            view = view,
            densityScale = 2,
            baseCheck = baseCheck,
            writeFiles = false,
            rendersRoot = outDir,
            frameFormat = FrameFormat.LZ4_RGBA,
          ),
        )
      } catch (e: ServerReplacedException) {
        throw e
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
  ): kotlin.Result<List<ViewSpec>> {
    if (requested == null) return kotlin.Result.success(surface.views)
    val mapped = requested.map { name ->
      surface.views.firstOrNull { it.name == name }
        ?: return kotlin.Result.failure(
          IllegalArgumentException(
            "unknown view '$name' (the ${surface.key} views: ${surface.views.joinToString { it.name }})",
          ),
        )
    }
    return kotlin.Result.success(mapped)
  }

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
