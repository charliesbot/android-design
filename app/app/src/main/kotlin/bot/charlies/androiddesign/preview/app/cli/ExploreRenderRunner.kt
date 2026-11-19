package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.data.DesignMdLocator
import bot.charlies.androiddesign.preview.data.DesignMdParse
import bot.charlies.androiddesign.preview.data.DesignMdParser
import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.domain.layout.LayoutSummary
import bot.charlies.androiddesign.preview.domain.layout.ViewLayout
import bot.charlies.androiddesign.preview.feature.preview.sandbox.CompileResult
import bot.charlies.androiddesign.preview.feature.preview.sandbox.ExplorationCompiler
import bot.charlies.androiddesign.preview.feature.preview.sandbox.HeadlessResult
import bot.charlies.androiddesign.preview.feature.preview.sandbox.SandboxHeadlessRenderer
import bot.charlies.androiddesign.preview.kit.DirectionSpec
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.SurfaceSpec
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.ValidationError
import bot.charlies.androiddesign.preview.model.ViewSpec
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path

/** Renders one compiled screen target at one [ViewSpec] with an optional open overlay. */
fun interface ExploreTargetRenderer {
  fun render(
    compiled: CompileResult.Compiled,
    direction: DirectionSpec,
    screenId: String,
    view: ViewSpec,
    designSystem: DesignSystem,
    overlayId: String?,
  ): HeadlessResult
}

/**
 * Shared loader and headless renderer for `.explore.kt` explorations used by [RenderCommand] and
 * the preview HTTP server.
 */
object ExploreRenderRunner {
  val defaultRenderer: ExploreTargetRenderer =
    ExploreTargetRenderer { compiled, direction, screenId, view, designSystem, overlayId ->
      SandboxHeadlessRenderer.render(
        compiled = compiled,
        direction = direction,
        screenId = screenId,
        view = view,
        designSystem = designSystem,
        overlayId = overlayId,
        encodePng = true,
      )
    }

  data class LoadedExploration(
    val sourcePath: Path,
    val designPath: Path?,
    val designTheme: ThemeOverride?,
    val designSystem: DesignSystem,
    val compiled: CompileResult.Compiled?,
    val compileMs: Long,
    val errors: List<ValidationError>,
  )

  data class RenderedTarget(
    val direction: String,
    val screen: String,
    val overlay: String?,
    val view: ViewSpec,
    val artifactName: String,
    val renderMs: Long,
    val result: HeadlessResult?,
    val check: ViewCheck,
    val layout: ViewLayout?,
    val entry: RenderEntry,
  )

  fun loadAndCompile(sourcePath: Path, explicitDesign: Path?): LoadedExploration {
    val normalized = sourcePath.toAbsolutePath().normalize()
    val (designPath, designTheme, designErrors) = loadDesignMd(normalized, explicitDesign)
    if (designErrors.isNotEmpty()) {
      return LoadedExploration(
        sourcePath = normalized,
        designPath = designPath,
        designTheme = designTheme,
        designSystem = DesignSystemResolver.resolve(designTheme, null),
        compiled = null,
        compileMs = 0L,
        errors = designErrors,
      )
    }

    return when (val compileResult = ExplorationCompiler.compile(normalized)) {
      is CompileResult.Failed ->
        LoadedExploration(
          sourcePath = normalized,
          designPath = designPath,
          designTheme = designTheme,
          designSystem = DesignSystemResolver.resolve(designTheme, null),
          compiled = null,
          compileMs = compileResult.compileMs,
          errors = compileResult.errors,
        )
      is CompileResult.Compiled -> {
        val designSystem = compileResult.board.resolveDesignSystem(designTheme)
        LoadedExploration(
          sourcePath = normalized,
          designPath = designPath,
          designTheme = designTheme,
          designSystem = designSystem,
          compiled = compileResult,
          compileMs = compileResult.compileMs,
          errors = emptyList(),
        )
      }
    }
  }

  private data class DesignLoadResult(
    val path: Path?,
    val theme: ThemeOverride?,
    val errors: List<ValidationError>,
  )

  private fun loadDesignMd(sourcePath: Path, explicitDesign: Path?): DesignLoadResult {
    val designPath =
      if (explicitDesign != null) {
        val norm = explicitDesign.toAbsolutePath().normalize()
        if (!Files.isRegularFile(norm)) {
          return DesignLoadResult(
            norm,
            null,
            listOf(ValidationError(norm.toString(), "no such file")),
          )
        }
        norm
      } else {
        DesignMdLocator.find(sourcePath)
      }
    if (designPath == null) return DesignLoadResult(null, null, emptyList())
    val text =
      try {
        Files.readString(designPath)
      } catch (e: Exception) {
        return DesignLoadResult(
          designPath,
          null,
          listOf(ValidationError(designPath.toString(), e.message ?: "cannot read DESIGN.md")),
        )
      }
    return when (val parsed = DesignMdParser.parse(text)) {
      is DesignMdParse.Parsed -> DesignLoadResult(designPath, parsed.theme, emptyList())
      is DesignMdParse.Failed ->
        DesignLoadResult(
          designPath,
          null,
          listOf(ValidationError(designPath.toString(), parsed.message)),
        )
    }
  }

  fun runCliRender(
    sourcePath: Path,
    outDir: Path,
    explicitDesign: Path?,
    requestedViews: List<String>?,
    requestedScreens: List<String>?,
    err: PrintStream,
    targetRenderer: ExploreTargetRenderer = defaultRenderer,
  ): Int {
    val loaded = loadAndCompile(sourcePath, explicitDesign)
    val compiled = loaded.compiled
    if (compiled == null || loaded.errors.isNotEmpty()) {
      writeReport(
        outDir,
        sourcePath,
        loaded.designPath,
        loaded.designSystem,
        loaded.errors,
        emptyList(),
      )
      loaded.errors.forEach { err.println("invalid: ${it.path}: ${it.message}") }
      return ExitCode.INVALID
    }

    val surface = compiled.board.surfaceSpec
    val views = resolveViews(requestedViews, surface, err) ?: return ExitCode.USAGE
    val targets =
      resolveTargets(compiled.board.directions, requestedScreens, directionFilter = null)
    if (requestedScreens != null) {
      val known =
        compiled.board.directions
          .flatMap { dir ->
            dir.screenIds.flatMap { sid -> listOf(sid, "${dir.name}/$sid", "${dir.name}__$sid") }
          }
          .toSet()
      val unknown = requestedScreens.filter { it !in known }
      if (unknown.isNotEmpty()) {
        val allNames =
          compiled.board.directions.flatMap { d -> d.screenIds.map { "${d.name}/$it" } }
        err.println(
          "no such screen: ${unknown.joinToString()} (screens: ${allNames.joinToString()})",
        )
        return ExitCode.USAGE
      }
    }

    val rendered =
      renderTargets(
        compiled = compiled,
        designSystem = loaded.designSystem,
        targets = targets,
        views = views,
        outDir = outDir,
        targetRenderer = targetRenderer,
      )
    val entries = rendered.map { it.entry }
    val report =
      writeReport(
        outDir = outDir,
        sourcePath = sourcePath,
        designPath = loaded.designPath,
        system = loaded.designSystem,
        errors = emptyList(),
        renders = entries,
      )
    entries
      .filter { !it.pass }
      .forEach { entry ->
        err.println("${entry.screen}/${entry.view}: ${entry.failures.joinToString("; ")}")
      }
    return if (report.pass) ExitCode.OK else ExitCode.FINDINGS_FAILED
  }

  data class ScreenTarget(val direction: DirectionSpec, val screenId: String, val prefix: String)

  fun resolveTargets(
    directions: List<DirectionSpec>,
    screenFilter: List<String>?,
    directionFilter: Set<String>?,
  ): List<ScreenTarget> {
    val activeDirections =
      if (directionFilter.isNullOrEmpty()) directions
      else directions.filter { it.name in directionFilter }
    val singleDirection = activeDirections.size == 1 && directions.size == 1
    val filterSet = screenFilter?.toSet()
    return buildList {
      for (dir in activeDirections) {
        for (screenId in dir.screenIds) {
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

  fun renderTargets(
    compiled: CompileResult.Compiled,
    designSystem: DesignSystem,
    targets: List<ScreenTarget>,
    views: List<ViewSpec>,
    outDir: Path? = null,
    targetRenderer: ExploreTargetRenderer = defaultRenderer,
  ): List<RenderedTarget> {
    val surface = compiled.board.surfaceSpec
    val out = mutableListOf<RenderedTarget>()
    for (target in targets) {
      val baseChecks = ArrayList<ViewCheck>(views.size)
      for (view in views) {
        val item =
          renderSingle(
            compiled = compiled,
            direction = target.direction,
            screenId = target.screenId,
            prefix = target.prefix,
            overlayId = null,
            view = view,
            designSystem = designSystem,
            surface = surface,
            baseCheck = null,
            outDir = outDir,
            targetRenderer = targetRenderer,
          )
        baseChecks += item.check
        out += item
      }
      val overlays = target.direction.overlaysFor(target.screenId)
      for (overlay in overlays) {
        views.forEachIndexed { index, view ->
          out +=
            renderSingle(
              compiled = compiled,
              direction = target.direction,
              screenId = target.screenId,
              prefix = target.prefix,
              overlayId = overlay.id,
              view = view,
              designSystem = designSystem,
              surface = surface,
              baseCheck = baseChecks[index],
              outDir = outDir,
              targetRenderer = targetRenderer,
            )
        }
      }
    }
    return out
  }

  fun renderSingle(
    compiled: CompileResult.Compiled,
    direction: DirectionSpec,
    screenId: String,
    prefix: String,
    overlayId: String?,
    view: ViewSpec,
    designSystem: DesignSystem,
    surface: SurfaceSpec,
    baseCheck: ViewCheck?,
    outDir: Path?,
    targetRenderer: ExploreTargetRenderer = defaultRenderer,
  ): RenderedTarget {
    val artifactName = overlayId?.let { ViewCheck.renderOverlayName(prefix, it) } ?: prefix
    val startNs = System.nanoTime()
    val headless =
      try {
        targetRenderer.render(
          compiled = compiled,
          direction = direction,
          screenId = screenId,
          view = view,
          designSystem = designSystem,
          overlayId = overlayId,
        )
      } catch (e: Exception) {
        val elapsedMs = (System.nanoTime() - startNs) / 1_000_000L
        val failed = ViewCheck.renderError(e)
        return RenderedTarget(
          direction = direction.name,
          screen = screenId,
          overlay = overlayId,
          view = view,
          artifactName = artifactName,
          renderMs = elapsedMs,
          result = null,
          check = failed,
          layout = null,
          entry = RenderEntry.of(artifactName, view.name, null, failed),
        )
      }
    val elapsedMs = (System.nanoTime() - startNs) / 1_000_000L
    val full = ViewCheck.of(headless.tree, surface)
    val check = if (baseCheck == null) full else full.overlaying(baseCheck)
    val layout = LayoutSummary.of(headless.tree, surface)
    val filesBase = "$artifactName-${view.name}"
    var endFiles: String? = null
    if (outDir != null) {
      Files.createDirectories(outDir)
      writeArtifacts(outDir, filesBase, headless.png, headless.tree)
      headless.end?.let { end ->
        val endBase = "$filesBase-end"
        writeArtifacts(outDir, endBase, end.png, end.tree)
        endFiles = endBase
      }
    }
    return RenderedTarget(
      direction = direction.name,
      screen = screenId,
      overlay = overlayId,
      view = view,
      artifactName = artifactName,
      renderMs = elapsedMs,
      result = headless,
      check = check,
      layout = if (overlayId == null) layout else null,
      entry =
        RenderEntry.of(
          artifactName,
          view.name,
          if (outDir != null) filesBase else null,
          check,
          endFiles,
        ),
    )
  }

  private fun writeArtifacts(dir: Path, base: String, png: ByteArray, tree: LayoutTree) {
    Files.write(dir.resolve("$base.png"), png)
    Files.writeString(
      dir.resolve("$base.tree.json"),
      PreviewJson.pretty.encodeToString(LayoutTree.serializer(), tree) + "\n",
    )
  }

  fun writeReport(
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

  fun resolveViews(
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

  private fun sanitizeSegment(name: String): String =
    name.trim().replace(Regex("""[^A-Za-z0-9._-]+"""), "_").ifBlank { "direction" }
}
