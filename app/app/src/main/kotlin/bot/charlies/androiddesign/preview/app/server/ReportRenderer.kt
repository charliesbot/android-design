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
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PhoneSurface
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.SurfaceSpec
import bot.charlies.androiddesign.preview.model.ValidationError
import bot.charlies.androiddesign.preview.model.WearSurface
import java.nio.charset.CharacterCodingException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Compiles an exploration and writes `<screen>-<view>.png`, `<screen>-<view>.tree.json`, optional
 * `-end` files, and `report.json` into `outDir` via the provided [compile] and [renderBatch]
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

  fun render(
    sourcePath: Path,
    outDir: Path,
    explicitDesign: Path?,
    requestedViews: List<String>?,
    requestedScreens: List<String>?,
    compile: (ContentHasher.Snapshot) -> CompileEntry,
    renderBatch: (CompileEntry, List<CheckTargetSpec>) -> List<RenderArtifact>,
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
    val planned =
      when (
        val resolved =
          TargetResolver.resolveReportTargets(meta, surface, requestedViews, requestedScreens)
      ) {
        is TargetResolution.Ok -> resolved.value
        is TargetResolution.Error ->
          return Result(
            ok = false,
            valid = true,
            pass = false,
            exitCode = ExitCode.USAGE,
            error = resolved.code,
            messages = listOf(resolved.message),
          )
      }

    val entries =
      try {
        val artifacts = renderBatch(entry, planned)
        artifacts.map { artifact -> writeArtifactAndBuildEntry(artifact, normOut) }
      } catch (e: ServerReplacedException) {
        throw e
      } catch (e: Exception) {
        val failed = ViewCheck.renderError(e)
        planned.map { spec ->
          val artifactName =
            spec.overlayId?.let { ViewCheck.renderOverlayName(spec.prefix, it) } ?: spec.prefix
          RenderEntry.of(artifactName, spec.view.name, null, failed)
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

  private fun writeArtifactAndBuildEntry(
    artifact: RenderArtifact,
    outDir: Path,
  ): RenderEntry {
    val artifactName = artifact.artifactName
    val viewName = artifact.view.name
    val tree = artifact.tree
    val png = artifact.pngBytes
    if (tree == null || png == null || png.isEmpty()) {
      return RenderEntry.of(artifactName, viewName, null, artifact.check)
    }

    val filesBase = "$artifactName-$viewName"
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
    return RenderEntry.of(artifactName, viewName, filesBase, artifact.check, endFiles)
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
}
