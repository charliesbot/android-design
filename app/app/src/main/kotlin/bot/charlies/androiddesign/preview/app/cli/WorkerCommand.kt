package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.data.ProposalReader
import bot.charlies.androiddesign.preview.domain.OverlayPlacement
import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.domain.layout.LayoutSummary
import bot.charlies.androiddesign.preview.domain.layout.ViewLayout
import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessRenderer
import bot.charlies.androiddesign.preview.feature.preview.headless.RenderSource
import bot.charlies.androiddesign.preview.kit.BoardSurface
import bot.charlies.androiddesign.preview.model.Catalogs
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.ValidationError
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * Headless socket-free stdio JSON-RPC worker (`ping`, `compileAndCheck`, `renderView`, `getTree`,
 * `shutdown`) driven by the Node `npx` MCP supervisor.
 */
class WorkerCommand(
  private val input: InputStream? = null,
  private val redirectSystemOut: Boolean = true,
) : Command {
  override val name = "worker"
  override val usage = "worker"

  @Serializable
  data class Request(
    val id: JsonElement? = null,
    val method: String,
    val params: JsonObject = JsonObject(emptyMap()),
  )

  @Serializable data class RpcError(val code: String, val message: String)

  @Serializable
  data class Response(
    val id: JsonElement? = null,
    val result: JsonElement? = null,
    val error: RpcError? = null,
  )

  @Serializable data class PingResult(val status: String, val compileCount: Int)

  @Serializable
  data class CompileAndCheckParams(
    val path: String,
    val design: String? = null,
    val directions: List<String>? = null,
    val screens: List<String>? = null,
    val views: List<String>? = null,
    val outDir: String? = null,
    val includeImages: Boolean = false,
    val allImages: Boolean = false,
  )

  @Serializable data class OverlayMeta(val id: String, val screen: String? = null)

  @Serializable
  data class DirectionMeta(
    val name: String,
    val surface: String,
    val startScreen: String,
    val screens: List<String>,
    val overlays: List<OverlayMeta>,
  )

  @Serializable
  data class BoardMeta(
    val directions: List<DirectionMeta>,
    val views: List<ViewSpec>,
  )

  @Serializable
  data class WorkerRenderItem(
    val direction: String,
    val screen: String,
    val overlay: String? = null,
    val view: String,
    val artifactName: String,
    val pass: Boolean,
    val renderMs: Long,
    val check: ViewCheck,
    val layout: ViewLayout? = null,
    val failures: List<String>,
    val pngBase64: String? = null,
    val endPngBase64: String? = null,
  )

  @Serializable
  data class CompileAndCheckResult(
    val valid: Boolean,
    val pass: Boolean,
    val compileMs: Long,
    val renderMs: Long,
    val compileCount: Int,
    val surface: String,
    val boardName: String? = null,
    val sourcePath: String,
    val designPath: String? = null,
    val theme: ReportTheme,
    val errors: List<ValidationError>,
    val boardMeta: BoardMeta? = null,
    val renders: List<WorkerRenderItem>,
  )

  @Serializable
  data class RenderViewParams(
    val direction: String? = null,
    val screen: String? = null,
    val overlay: String? = null,
    val view: String? = null,
    val seed: String? = null,
    val variant: String? = null,
    val dark: Boolean? = null,
  )

  @Serializable
  data class RenderViewResult(
    val direction: String,
    val screen: String,
    val overlay: String? = null,
    val view: String,
    val pass: Boolean,
    val renderMs: Long,
    val compileCount: Int,
    val check: ViewCheck,
    val layout: ViewLayout? = null,
    val tree: LayoutTree,
    val pngBase64: String,
    val endPngBase64: String? = null,
  )

  @Serializable
  data class GetTreeParams(
    val direction: String? = null,
    val screen: String? = null,
    val overlay: String? = null,
    val view: String? = null,
    val maxDepth: Int? = null,
  )

  @Serializable
  data class GetTreeResult(
    val direction: String,
    val screen: String,
    val overlay: String? = null,
    val view: String,
    val tree: LayoutTree,
  )

  private data class TreeKey(
    val direction: String,
    val screen: String,
    val overlay: String?,
    val view: String,
  )

  private sealed interface CachedSource {
    data class Exploration(val loaded: ExploreRenderRunner.LoadedExploration) : CachedSource

    data class Proposal(val path: Path, val source: RenderSource) : CachedSource
  }

  private class SessionState {
    var compileCount: Int = 0
    var cached: CachedSource? = null
    val trees = LinkedHashMap<TreeKey, LayoutTree>()
  }

  override fun run(args: List<String>, out: PrintStream, err: PrintStream): Int {
    val previousOut = System.out
    val rpcOut = out
    if (redirectSystemOut && out === previousOut) {
      System.setOut(err)
    }
    return try {
      val state = SessionState()
      val effectiveInput = input ?: System.`in`
      val reader = BufferedReader(InputStreamReader(effectiveInput, Charsets.UTF_8))
      while (true) {
        val rawLine = reader.readLine() ?: break
        if (rawLine.isBlank()) continue
        val request =
          try {
            PreviewJson.compact.decodeFromString(Request.serializer(), rawLine)
          } catch (e: Exception) {
            writeResponse(
              rpcOut,
              Response(
                error = RpcError("bad_json", e.message ?: "Malformed JSON-RPC request"),
              ),
            )
            continue
          }
        val shouldExit = dispatch(request, state, rpcOut)
        if (shouldExit) break
      }
      ExitCode.OK
    } finally {
      if (redirectSystemOut && out === previousOut) {
        System.setOut(previousOut)
      }
    }
  }

  private fun dispatch(request: Request, state: SessionState, rpcOut: PrintStream): Boolean {
    val response =
      try {
        when (request.method) {
          "ping" ->
            Response(
              id = request.id,
              result =
                PreviewJson.compact.encodeToJsonElement(PingResult("ok", state.compileCount)),
            )
          "compileAndCheck" -> {
            val params =
              PreviewJson.compact.decodeFromJsonElement<CompileAndCheckParams>(request.params)
            val result = handleCompileAndCheck(params, state)
            Response(
              id = request.id,
              result = PreviewJson.compact.encodeToJsonElement(result),
            )
          }
          "renderView" -> {
            val params = PreviewJson.compact.decodeFromJsonElement<RenderViewParams>(request.params)
            val result = handleRenderView(params, state)
            Response(
              id = request.id,
              result = PreviewJson.compact.encodeToJsonElement(result),
            )
          }
          "getTree" -> {
            val params = PreviewJson.compact.decodeFromJsonElement<GetTreeParams>(request.params)
            val result = handleGetTree(params, state)
            Response(
              id = request.id,
              result = PreviewJson.compact.encodeToJsonElement(result),
            )
          }
          "shutdown" -> {
            writeResponse(
              rpcOut,
              Response(
                id = request.id,
                result = JsonObject(mapOf("status" to JsonPrimitive("bye"))),
              ),
            )
            return true
          }
          else ->
            Response(
              id = request.id,
              error = RpcError("unknown_method", "Unknown worker method '${request.method}'"),
            )
        }
      } catch (e: WorkerException) {
        Response(id = request.id, error = RpcError(e.code, e.message ?: e.code))
      } catch (e: Exception) {
        Response(
          id = request.id,
          error = RpcError("internal_error", e.message ?: e::class.simpleName.orEmpty()),
        )
      }
    writeResponse(rpcOut, response)
    return false
  }

  private fun handleCompileAndCheck(
    params: CompileAndCheckParams,
    state: SessionState,
  ): CompileAndCheckResult {
    val sourcePath = Path.of(params.path).toAbsolutePath().normalize()
    if (!Files.isRegularFile(sourcePath)) {
      throw WorkerException("no_such_file", "No such file: $sourcePath")
    }
    state.compileCount += 1
    state.trees.clear()

    if (!sourcePath.fileName.toString().endsWith(".explore.kt")) {
      return compileAndCheckProposal(sourcePath, params, state)
    }

    val loaded = ExploreRenderRunner.loadAndCompile(sourcePath, params.design?.let(Path::of))
    val compiled = loaded.compiled
    if (compiled == null || loaded.errors.isNotEmpty()) {
      state.cached = null
      return CompileAndCheckResult(
        valid = false,
        pass = false,
        compileMs = loaded.compileMs,
        renderMs = 0L,
        compileCount = state.compileCount,
        surface = "phone",
        boardName = null,
        sourcePath = sourcePath.toString(),
        designPath = loaded.designPath?.toString(),
        theme =
          ReportTheme(
            loaded.designSystem.seed,
            loaded.designSystem.variant.wire,
            loaded.designSystem.contrast,
          ),
        errors = loaded.errors,
        boardMeta = null,
        renders = emptyList(),
      )
    }

    state.cached = CachedSource.Exploration(loaded)
    val board = compiled.board
    val catalog = board.catalog
    val views = resolveWorkerViews(params.views, catalog)
    val directionFilter = params.directions?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet()
    val targets =
      ExploreRenderRunner.resolveTargets(board.directions, params.screens, directionFilter)
    if (targets.isEmpty()) {
      throw WorkerException("no_matching_targets", "No matching directions or screens selected")
    }

    val renderStartNs = System.nanoTime()
    val rendered =
      ExploreRenderRunner.renderTargets(
        compiled = compiled,
        designSystem = loaded.designSystem,
        targets = targets,
        views = views,
        outDir = params.outDir?.let(Path::of),
      )
    val renderMs = (System.nanoTime() - renderStartNs) / 1_000_000L

    for (item in rendered) {
      item.result?.tree?.let { tree ->
        state.trees[TreeKey(item.direction, item.screen, item.overlay, item.view.name)] = tree
      }
    }

    val allPass = rendered.all { it.entry.pass }
    val primaryImageViews =
      if (board.surface == BoardSurface.Wear) setOf("watch", "watch-large")
      else setOf("light", "desktop")
    val b64 = Base64.getEncoder()

    val workerItems = rendered.map { item ->
      val headless = item.result
      val attachPng =
        headless != null &&
          (params.allImages ||
            (params.includeImages && item.view.name in primaryImageViews) ||
            (allPass && item.view.name in primaryImageViews && item.overlay == null))
      WorkerRenderItem(
        direction = item.direction,
        screen = item.screen,
        overlay = item.overlay,
        view = item.view.name,
        artifactName = item.artifactName,
        pass = item.entry.pass,
        renderMs = item.renderMs,
        check = item.check,
        layout = item.layout,
        failures = item.entry.failures,
        pngBase64 = if (attachPng) b64.encodeToString(headless.png) else null,
        endPngBase64 = if (attachPng) headless.end?.png?.let(b64::encodeToString) else null,
      )
    }

    val meta =
      BoardMeta(
        directions =
          board.directions.map { dir ->
            DirectionMeta(
              name = dir.name,
              surface = if (dir.surface == BoardSurface.Wear) "wear" else "phone",
              startScreen = dir.startScreen,
              screens = dir.screenIds,
              overlays = dir.overlays.map { ov -> OverlayMeta(ov.id, ov.screen) },
            )
          },
        views = catalog.views,
      )

    return CompileAndCheckResult(
      valid = true,
      pass = allPass,
      compileMs = loaded.compileMs,
      renderMs = renderMs,
      compileCount = state.compileCount,
      surface = if (board.surface == BoardSurface.Wear) "wear" else "phone",
      boardName = board.name,
      sourcePath = sourcePath.toString(),
      designPath = loaded.designPath?.toString(),
      theme =
        ReportTheme(
          loaded.designSystem.seed,
          loaded.designSystem.variant.wire,
          loaded.designSystem.contrast,
        ),
      errors = emptyList(),
      boardMeta = meta,
      renders = workerItems,
    )
  }

  private fun compileAndCheckProposal(
    sourcePath: Path,
    params: CompileAndCheckParams,
    state: SessionState,
  ): CompileAndCheckResult {
    val startedNs = System.nanoTime()
    val loaded = ProposalReader().read(sourcePath, params.design?.let(Path::of))
    val loadMs = (System.nanoTime() - startedNs) / 1_000_000L
    val proposal = loaded.proposal
    if (proposal == null || !loaded.isValid) {
      state.cached = null
      return CompileAndCheckResult(
        valid = false,
        pass = false,
        compileMs = loadMs,
        renderMs = 0L,
        compileCount = state.compileCount,
        surface = "phone",
        boardName = null,
        sourcePath = sourcePath.toString(),
        designPath = loaded.designMdPath?.toString(),
        theme =
          ReportTheme(
            loaded.designSystem.seed,
            loaded.designSystem.variant.wire,
            loaded.designSystem.contrast,
          ),
        errors = loaded.errors,
        boardMeta = null,
        renders = emptyList(),
      )
    }

    val renderSource = RenderSource(proposal, loaded.designSystem, sourcePath.parent)
    state.cached = CachedSource.Proposal(sourcePath, renderSource)
    val catalog = Catalogs.of(proposal)
    val views = resolveWorkerViews(params.views, catalog)
    val screens = params.screens ?: proposal.screens.keys.toList()
    val renderStartNs = System.nanoTime()
    val outDir = params.outDir?.let(Path::of)
    val b64 = Base64.getEncoder()
    val primaryViews =
      if (catalog.key == "wear") setOf("watch", "watch-large") else setOf("light", "desktop")

    val items = mutableListOf<WorkerRenderItem>()
    for (screen in screens) {
      val baseChecks = ArrayList<ViewCheck>(views.size)
      for (view in views) {
        val t0 = System.nanoTime()
        val res = HeadlessRenderer.image(renderSource, screen, view)
        val dt = (System.nanoTime() - t0) / 1_000_000L
        val check = ViewCheck.of(proposal, res.tree)
        val layout = LayoutSummary.of(res.tree, catalog)
        state.trees[TreeKey("main", screen, null, view.name)] = res.tree
        val entry =
          RenderEntry.of(
            screen,
            view.name,
            if (outDir != null) "$screen-${view.name}" else null,
            check,
          )
        if (outDir != null) {
          Files.createDirectories(outDir)
          Files.write(outDir.resolve("$screen-${view.name}.png"), res.png)
        }
        baseChecks += check
        val attachPng = params.allImages || (params.includeImages && view.name in primaryViews)
        items +=
          WorkerRenderItem(
            direction = "main",
            screen = screen,
            overlay = null,
            view = view.name,
            artifactName = screen,
            pass = entry.pass,
            renderMs = dt,
            check = check,
            layout = layout,
            failures = entry.failures,
            pngBase64 = if (attachPng) b64.encodeToString(res.png) else null,
          )
      }
      OverlayPlacement(proposal.screens.getValue(screen)).overlays.forEach { ov ->
        views.forEachIndexed { idx, view ->
          val t0 = System.nanoTime()
          val res = HeadlessRenderer.image(renderSource, screen, view, overlay = ov.id)
          val dt = (System.nanoTime() - t0) / 1_000_000L
          val full = ViewCheck.of(proposal, res.tree)
          val check = full.overlaying(baseChecks[idx])
          val art = OverlayPlacement.renderName(screen, ov.id)
          state.trees[TreeKey("main", screen, ov.id, view.name)] = res.tree
          val entry = RenderEntry.of(art, view.name, null, check)
          items +=
            WorkerRenderItem(
              direction = "main",
              screen = screen,
              overlay = ov.id,
              view = view.name,
              artifactName = art,
              pass = entry.pass,
              renderMs = dt,
              check = check,
              layout = LayoutSummary.of(res.tree, catalog),
              failures = entry.failures,
              pngBase64 = if (params.allImages) b64.encodeToString(res.png) else null,
            )
        }
      }
    }

    val renderMs = (System.nanoTime() - renderStartNs) / 1_000_000L
    val allPass = items.all { it.pass }
    val finalItems =
      if (!allPass || params.allImages || params.includeImages) {
        items
      } else {
        items.mapIndexed { idx, item ->
          if (item.overlay == null && item.view in primaryViews) {
            val view = views.first { it.name == item.view }
            val png = HeadlessRenderer.image(renderSource, item.screen, view).png
            item.copy(pngBase64 = b64.encodeToString(png))
          } else {
            item
          }
        }
      }
    val meta =
      BoardMeta(
        directions =
          listOf(
            DirectionMeta(
              name = "main",
              surface = catalog.key,
              startScreen = proposal.screens.keys.first(),
              screens = proposal.screens.keys.toList(),
              overlays = emptyList(),
            ),
          ),
        views = catalog.views,
      )
    return CompileAndCheckResult(
      valid = true,
      pass = allPass,
      compileMs = loadMs,
      renderMs = renderMs,
      compileCount = state.compileCount,
      surface = catalog.key,
      boardName = loaded.designSystem.name,
      sourcePath = sourcePath.toString(),
      designPath = loaded.designMdPath?.toString(),
      theme =
        ReportTheme(
          loaded.designSystem.seed,
          loaded.designSystem.variant.wire,
          loaded.designSystem.contrast,
        ),
      errors = emptyList(),
      boardMeta = meta,
      renders = finalItems,
    )
  }

  private fun handleRenderView(params: RenderViewParams, state: SessionState): RenderViewResult {
    val cached =
      state.cached
        ?: throw WorkerException("no_compiled_board", "Call compileAndCheck before renderView")
    return when (cached) {
      is CachedSource.Exploration -> renderExplorationView(cached.loaded, params, state)
      is CachedSource.Proposal -> renderProposalView(cached.source, params, state)
    }
  }

  private fun renderExplorationView(
    loaded: ExploreRenderRunner.LoadedExploration,
    params: RenderViewParams,
    state: SessionState,
  ): RenderViewResult {
    val compiled =
      loaded.compiled
        ?: throw WorkerException("no_compiled_board", "Cached exploration has compile errors")
    val board = compiled.board
    val direction =
      params.direction?.let { name ->
        board.directions.firstOrNull { it.name == name }
          ?: throw WorkerException("unknown_direction", "Unknown direction '$name'")
      } ?: board.directions.first()
    val screenId = params.screen ?: direction.startScreen
    if (screenId !in direction.screenIds) {
      throw WorkerException(
        "unknown_screen",
        "No screen '$screenId' in direction '${direction.name}'",
      )
    }
    val overlayId =
      params.overlay
        ?.takeIf { it.isNotBlank() }
        ?.also { ovId ->
          if (direction.overlays.none { it.id == ovId }) {
            throw WorkerException("unknown_overlay", "No overlay '$ovId' in '${direction.name}'")
          }
        }
    val catalog = board.catalog
    val defaultView = catalog.views.first()
    val baseView =
      params.view?.let { name ->
        catalog.views.firstOrNull { it.name == name }
          ?: throw WorkerException("unknown_view", "Unknown view '$name' for ${catalog.key}")
      } ?: defaultView
    val effectiveView =
      if (params.dark != null && board.surface == BoardSurface.Phone) {
        baseView.copy(dark = params.dark)
      } else {
        baseView
      }
    val variantOverride =
      params.variant?.let { wire ->
        PaletteVariant.fromWire(wire)
          ?: throw WorkerException("invalid_variant", "Unknown palette variant '$wire'")
      }
    val system =
      board.resolveDesignSystem(
        designMd = loaded.designTheme,
        seedOverride = params.seed,
        variantOverride = variantOverride,
      )
    val prefix = if (board.directions.size == 1) screenId else "${direction.name}__$screenId"
    val rendered =
      ExploreRenderRunner.renderSingle(
        compiled = compiled,
        direction = direction,
        screenId = screenId,
        prefix = prefix,
        overlayId = overlayId,
        view = effectiveView,
        designSystem = system,
        catalog = catalog,
        baseCheck = null,
        outDir = null,
      )
    val headless =
      rendered.result
        ?: throw WorkerException(
          "render_failed",
          rendered.entry.failures.firstOrNull() ?: "Render failed",
        )
    state.trees[TreeKey(direction.name, screenId, overlayId, baseView.name)] = headless.tree
    val b64 = Base64.getEncoder()
    return RenderViewResult(
      direction = direction.name,
      screen = screenId,
      overlay = overlayId,
      view = baseView.name,
      pass = rendered.entry.pass,
      renderMs = rendered.renderMs,
      compileCount = state.compileCount,
      check = rendered.check,
      layout = rendered.layout,
      tree = headless.tree,
      pngBase64 = b64.encodeToString(headless.png),
      endPngBase64 = headless.end?.png?.let(b64::encodeToString),
    )
  }

  private fun renderProposalView(
    source: RenderSource,
    params: RenderViewParams,
    state: SessionState,
  ): RenderViewResult {
    val catalog = Catalogs.of(source.proposal)
    val screen = params.screen ?: source.proposal.screens.keys.first()
    val baseView =
      params.view?.let { name -> catalog.views.firstOrNull { it.name == name } }
        ?: catalog.views.first()
    val view =
      if (params.dark != null && catalog.key == "phone") baseView.copy(dark = params.dark)
      else baseView
    val t0 = System.nanoTime()
    val res = HeadlessRenderer.image(source, screen, view, overlay = params.overlay)
    val dt = (System.nanoTime() - t0) / 1_000_000L
    val check = ViewCheck.of(source.proposal, res.tree)
    state.trees[TreeKey("main", screen, params.overlay, baseView.name)] = res.tree
    val b64 = Base64.getEncoder()
    return RenderViewResult(
      direction = "main",
      screen = screen,
      overlay = params.overlay,
      view = baseView.name,
      pass = check.pass,
      renderMs = dt,
      compileCount = state.compileCount,
      check = check,
      layout = LayoutSummary.of(res.tree, catalog),
      tree = res.tree,
      pngBase64 = b64.encodeToString(res.png),
      endPngBase64 = res.end?.png?.let(b64::encodeToString),
    )
  }

  private fun handleGetTree(params: GetTreeParams, state: SessionState): GetTreeResult {
    val cached =
      state.cached
        ?: throw WorkerException("no_compiled_board", "Call compileAndCheck before getTree")
    val (directionName, screenId, viewName) =
      when (cached) {
        is CachedSource.Exploration -> {
          val compiled =
            cached.loaded.compiled
              ?: throw WorkerException("no_compiled_board", "Cached exploration is invalid")
          val dir =
            params.direction?.let { name ->
              compiled.board.directions.firstOrNull { it.name == name }
            } ?: compiled.board.directions.first()
          val scr = params.screen ?: dir.startScreen
          val vw =
            params.view
              ?: if (compiled.board.surface == BoardSurface.Wear) Views.Watch.name
              else Views.Light.name
          Triple(dir.name, scr, vw)
        }
        is CachedSource.Proposal -> {
          val cat = Catalogs.of(cached.source.proposal)
          val scr = params.screen ?: cached.source.proposal.screens.keys.first()
          val vw = params.view ?: cat.views.first().name
          Triple("main", scr, vw)
        }
      }

    val key = TreeKey(directionName, screenId, params.overlay, viewName)
    val tree =
      state.trees[key]
        ?: handleRenderView(
            RenderViewParams(
              direction = directionName,
              screen = screenId,
              overlay = params.overlay,
              view = viewName,
            ),
            state,
          )
          .tree
    val bounded = params.maxDepth?.let { pruneDepth(tree, it) } ?: tree
    return GetTreeResult(directionName, screenId, params.overlay, viewName, bounded)
  }

  private fun pruneDepth(tree: LayoutTree, maxDepth: Int): LayoutTree {
    if (maxDepth < 0) return tree.copy(nodes = emptyList())
    val depthById = HashMap<String, Int>(tree.nodes.size)
    val childrenByParent = tree.nodes.groupBy { it.parent }
    val queue = ArrayDeque<Pair<LayoutNode, Int>>()
    childrenByParent[null].orEmpty().forEach { queue += it to 0 }
    while (queue.isNotEmpty()) {
      val (node, depth) = queue.removeFirst()
      depthById[node.id] = depth
      childrenByParent[node.id].orEmpty().forEach { child -> queue += child to (depth + 1) }
    }
    val kept = tree.nodes.filter { (depthById[it.id] ?: 0) <= maxDepth }
    return tree.copy(nodes = kept)
  }

  private fun resolveWorkerViews(
    requested: List<String>?,
    catalog: bot.charlies.androiddesign.preview.model.Catalog,
  ): List<ViewSpec> {
    if (requested == null) return catalog.views
    return requested.map { name ->
      catalog.views.firstOrNull { it.name == name }
        ?: throw WorkerException(
          "unknown_view",
          "Unknown view '$name' (expected one of ${catalog.views.joinToString { it.name }})",
        )
    }
  }

  private fun writeResponse(out: PrintStream, response: Response) {
    out.println(PreviewJson.compact.encodeToString(Response.serializer(), response))
    out.flush()
  }

  private class WorkerException(val code: String, message: String) : RuntimeException(message)
}
