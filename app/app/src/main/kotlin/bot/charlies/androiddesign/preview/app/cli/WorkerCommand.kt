package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.domain.layout.ViewLayout
import bot.charlies.androiddesign.preview.kit.BoardSurface
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.SurfaceSpec
import bot.charlies.androiddesign.preview.model.ValidationError
import bot.charlies.androiddesign.preview.model.ViewSpec
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
 * `shutdown`) driven by the Node MCP supervisor.
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

  private class SessionState {
    var compileCount: Int = 0
    var cached: ExploreRenderRunner.LoadedExploration? = null
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
      state.cached = null
      val defaults = DesignSystemResolver.resolve(null, null)
      return CompileAndCheckResult(
        valid = false,
        pass = false,
        compileMs = 0L,
        renderMs = 0L,
        compileCount = state.compileCount,
        surface = "phone",
        boardName = null,
        sourcePath = sourcePath.toString(),
        designPath = params.design,
        theme = ReportTheme(defaults.seed, defaults.variant.wire, defaults.contrast),
        errors =
          listOf(
            ValidationError(sourcePath.toString(), "Expected an .explore.kt exploration file"),
          ),
        boardMeta = null,
        renders = emptyList(),
      )
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

    state.cached = loaded
    val board = compiled.board
    val surface = board.surfaceSpec
    val views = resolveWorkerViews(params.views, surface)
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
        views = surface.views,
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

  private fun handleRenderView(params: RenderViewParams, state: SessionState): RenderViewResult {
    val loaded =
      state.cached
        ?: throw WorkerException("no_compiled_board", "Call compileAndCheck before renderView")
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
    val surface = board.surfaceSpec
    val defaultView = surface.views.first()
    val baseView =
      params.view?.let { name ->
        surface.views.firstOrNull { it.name == name }
          ?: throw WorkerException("unknown_view", "Unknown view '$name' for ${surface.key}")
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
        surface = surface,
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

  private fun handleGetTree(params: GetTreeParams, state: SessionState): GetTreeResult {
    val loaded =
      state.cached
        ?: throw WorkerException("no_compiled_board", "Call compileAndCheck before getTree")
    val compiled =
      loaded.compiled ?: throw WorkerException("no_compiled_board", "Cached exploration is invalid")
    val overlayId = params.overlay?.takeIf { it.isNotBlank() }
    val dir =
      params.direction?.let { name ->
        compiled.board.directions.firstOrNull { it.name == name }
          ?: throw WorkerException("unknown_direction", "Unknown direction '$name'")
      } ?: compiled.board.directions.first()
    val scr = params.screen ?: dir.startScreen
    if (scr !in dir.screenIds) {
      throw WorkerException("unknown_screen", "No screen '$scr' in direction '${dir.name}'")
    }
    if (overlayId != null && dir.overlays.none { it.id == overlayId }) {
      throw WorkerException("unknown_overlay", "No overlay '$overlayId' in '${dir.name}'")
    }
    val vw = params.view ?: compiled.board.surfaceSpec.views.first().name
    val key = TreeKey(dir.name, scr, overlayId, vw)
    val tree =
      state.trees[key]
        ?: handleRenderView(
            RenderViewParams(
              direction = dir.name,
              screen = scr,
              overlay = overlayId,
              view = vw,
            ),
            state,
          )
          .tree
    val bounded = params.maxDepth?.let { pruneDepth(tree, it) } ?: tree
    return GetTreeResult(dir.name, scr, overlayId, vw, bounded)
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
    surface: SurfaceSpec,
  ): List<ViewSpec> {
    if (requested == null) return surface.views
    return requested.map { name ->
      surface.views.firstOrNull { it.name == name }
        ?: throw WorkerException(
          "unknown_view",
          "Unknown view '$name' (expected one of ${surface.views.joinToString { it.name }})",
        )
    }
  }

  private fun writeResponse(out: PrintStream, response: Response) {
    out.println(PreviewJson.compact.encodeToString(Response.serializer(), response))
    out.flush()
  }

  private class WorkerException(val code: String, message: String) : RuntimeException(message)
}
