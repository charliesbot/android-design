package bot.charlies.androiddesign.preview.mcp

import bot.charlies.androiddesign.preview.domain.OpenedProposal
import bot.charlies.androiddesign.preview.domain.PreviewController
import bot.charlies.androiddesign.preview.domain.PreviewException
import bot.charlies.androiddesign.preview.domain.PreviewStatus
import bot.charlies.androiddesign.preview.domain.ProposalCheck
import bot.charlies.androiddesign.preview.domain.RenderedPng
import bot.charlies.androiddesign.preview.domain.ViewChange
import bot.charlies.androiddesign.preview.domain.ViewRender
import bot.charlies.androiddesign.preview.domain.ViewState
import bot.charlies.androiddesign.preview.model.Catalogs
import bot.charlies.androiddesign.preview.model.Device
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.Views
import io.modelcontextprotocol.kotlin.sdk.server.ClientConnection
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.shared.Transport
import io.modelcontextprotocol.kotlin.sdk.types.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.ContentBlock
import io.modelcontextprotocol.kotlin.sdk.types.ImageContent
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolAnnotations
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import java.util.Base64
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/**
 * The MCP face of the previewer: nine tools that call a [PreviewController]. It does not own any
 * transport; [connect] attaches it to one, so the app can use stdio and tests can use pipes.
 *
 * Problems the agent can fix (an unknown screen, an invalid proposal) come back as tool errors with
 * a message that says what to change; they are never protocol errors.
 */
class PreviewMcpServer(private val controller: PreviewController, private val version: String) {
  val server: Server =
    Server(
      Implementation(name = SERVER_NAME, version = version),
      ServerOptions(
        capabilities = ServerCapabilities(tools = ServerCapabilities.Tools(listChanged = false)),
      ),
      INSTRUCTIONS,
    )

  init {
    registerTools()
  }

  /** Starts serving on [transport] and returns once the session is created. */
  suspend fun connect(transport: Transport) {
    server.createSession(transport)
  }

  private fun registerTools() {
    tool(
      name = "open_proposal",
      title = "Open a proposal",
      description =
        "Loads a design proposal file (design/<slug>.proposal.json), validates it, shows it in the " +
          "preview window and starts watching it. From then on, edit the file with your normal " +
          "file tools and the window redraws on its own; no call is needed after an edit. " +
          "Returns the screens, the resolved DESIGN.md path and any validation errors.",
      schema =
        schema(
          string(
            "path",
            "Path to the proposal file, absolute or relative to the working directory.",
          ),
          required = listOf("path"),
        ),
      readOnly = false,
    ) { args ->
      PreviewJson.compact.encodeToString(
        OpenedProposal.serializer(),
        controller.openProposal(args.requiredString("path")),
      )
    }
    tool(
      name = "get_tree",
      title = "Read the layout tree",
      description =
        "Returns the layout tree: every proposal component with its bounds and touch bounds in dp " +
          "(relative to the preview frame), text lines and truncation, color and type roles, " +
          "accessibility role and label, plus findings (touch targets, fonts, contrast, truncated " +
          "labels, one FAB, primary action). The bottom bar or rail of navigation.destinations is " +
          "in the tree as @navigation (NavigationBar or NavigationRail) with one " +
          "@navigation[index] item per destination, outside every screen's components. A view " +
          "passes when no finding has severity error. " +
          "With no arguments it describes what the window shows: its screen, device, brightness, " +
          "color overrides, open overlays and selections. With screen or view it renders that " +
          "screen at that view without a window. A scrolling Wear list is scrolled from top to end " +
          "and every row and the edge button are in the tree, each with its bounds at the scroll " +
          "position where it showed whole, so do not compare positions of rows that are not on " +
          "screen together; an open overlay keeps the list at the top. Read this instead of " +
          "looking at pictures; render_view is the one visual confirmation, after this passes.",
      schema = treeSchema(),
    ) { args ->
      val tree = controller.getTree(args.string("screen"), args.string("view"))
      PreviewJson.compact.encodeToString(LayoutTree.serializer(), tree)
    }
    contentTool(
      name = "render_view",
      title = "Render a screen to a picture",
      description =
        "Renders one screen at one view to a PNG and returns it, for one visual confirmation " +
          "after check_proposal passes: things the layout tree cannot show, such as icon fit, " +
          "image assets, and overall balance. Same screen and view arguments as get_tree. Not " +
          "for iterating: fix from check_proposal and get_tree. If the picture disagrees with " +
          "the tree, report it as a possible previewer bug. A scrolling Wear list also returns " +
          "its end-of-list picture. Drawn at 1 px per dp. Also saved to the returned path.",
      schema = treeSchema(),
    ) { args ->
      controller.renderView(args.string("screen"), args.string("view")).toContent()
    }
    tool(
      name = "set_view",
      title = "Change the window's view",
      description =
        "Changes what the window shows: screen, device, watch view, brightness, seed color, " +
          "palette variant. Omitted values stay as they are. These choices only affect the window and " +
          "get_tree without arguments; they are never written to the proposal.",
      schema =
        schema(
          enum(
            "device",
            "Device frame: phone, foldable, tablet or desktop for a phone proposal, watch for a " +
              "Wear proposal.",
            Device.entries.map { it.wire },
          ),
          enum(
            "view",
            "The watch view of a Wear proposal: watch (204 dp) or watch-large (227 dp).",
            Views.wear.map { it.name },
          ),
          bool("dark", "True for dark brightness, false for light."),
          string("seed", "Seed color as #RRGGBB."),
          enum("variant", "Palette variant.", PaletteVariant.entries.map { it.wire }),
          string("screen", "Screen id to show, as the side panel's screen list does."),
        ),
      readOnly = false,
    ) { args ->
      val change =
        ViewChange(
          device =
            args.string("device")?.let {
              Device.fromWire(it)
                ?: throw PreviewException(
                  "unknown device '$it' (devices: ${Device.entries.joinToString { d -> d.wire }})",
                )
            },
          dark = args.bool("dark"),
          seed = args.string("seed"),
          screen = args.string("screen"),
          view = args.string("view"),
          variant =
            args.string("variant")?.let {
              PaletteVariant.fromWire(it)
                ?: throw PreviewException(
                  "unknown variant '$it' (variants: ${PaletteVariant.entries.joinToString { v -> v.wire }})",
                )
            },
        )
      PreviewJson.compact.encodeToString(
        ViewState.serializer(),
        controller.setView(change),
      )
    }
    tool(
      name = "show_preview",
      title = "Show the preview window",
      description =
        "Brings the preview window back after it was closed. Closing the window only hides it; " +
          "this server keeps working and get_tree still answers from a headless render.",
      schema = schema(),
      readOnly = false,
    ) {
      controller.showPreview()
      "The preview window is visible."
    }
    tool(
      name = "check_proposal",
      title = "Check every screen and view",
      description =
        "Renders each screen at each view without a window, tree only, and returns whether the " +
          "proposal passes: per screen and view, pass, every error finding (rule, component ids, " +
          "detail), notes, and a layout summary. Notes are the info findings, never a failure, " +
          "grouped by rule with their component ids and no detail. The layout is the navigation " +
          "component shown (NavigationBar, NavigationRail, FloatingToolbar or none, from " +
          "navigation.destinations) and the screens shown left to right, each with the direct " +
          "children of its content root as \"Type id width\" (dp). A view whose result equals an " +
          "earlier view's of the same screen is only {\"same\": \"<that view>\"}: read the first. " +
          "Compare the layout of two views to see that a wider one reveals more, with no need for " +
          "get_tree. Top-level colors give, for light and dark as checked (Wear is dark only), the " +
          "#RRGGBB of each color role the renders draw with, by role name, so contrast can be " +
          "judged without get_tree. No other geometry; call get_tree for a failing view's bounds. " +
          "With no arguments it checks every screen at every view of the proposal's catalog: " +
          "light, dark, foldable, tablet and desktop for a phone proposal, watch and watch-large " +
          "for a Wear proposal. A scrolling Wear list is scrolled from top to end, so its " +
          "findings cover every item and the edge button. Each overlay (sheet, dialog, menu, " +
          "search view, tooltip, snackbar, picker) is one more entry per view under " +
          "\"screen+overlayId\", drawn with it open and checked the same way, but listing only the " +
          "errors and notes the plain screen at that view does not already report (matched by " +
          "rule and component id), and no layout summary. Call this after each edit instead of " +
          "get_tree per view.",
      schema =
        schema(
          stringList("screens", "Screen ids to check. Defaults to every screen."),
          enumList(
            "views",
            "Views to check. Defaults to every view of the proposal's catalog.",
            Views.every.map { it.name },
          ),
        ),
    ) { args ->
      PreviewJson.compact.encodeToString(
        ProposalCheck.serializer(),
        controller.checkProposal(args.strings("screens"), args.strings("views")),
      )
    }
    tool(
      name = "get_catalog",
      title = "Read the component catalog",
      description =
        "With no arguments, returns a compact catalog: every component with its purpose and its " +
          "props as name: kind (enum values inline, child slots and required props marked), then " +
          "the navigation field (destinations and railExpanded: the bottom bar and the rail are " +
          "declared there once, never as components of a screen), the color, type, shape and " +
          "spacing tokens and the action functions. With component, " +
          "returns the full JSON Schema entries of those components. A proposal may only use " +
          "what its catalog declares.",
      schema =
        schema(
          stringOrList(
            "component",
            "A component name or a list of names, for their full JSON Schema entries. Omit for " +
              "the compact catalog.",
          ),
          enum(
            "catalog",
            "The catalog: m3-expressive for phone, tablet and desktop proposals (the default), " +
              "wear for Wear OS proposals.",
            Catalogs.all.map { it.key },
          ),
        ),
    ) { args ->
      controller.getCatalog(args.strings("component"), args.string("catalog"))
    }
    tool(
      name = "export_kotlin",
      title = "Export to Jetpack Compose",
      description =
        "Returns Kotlin source for one screen, or for every screen with the Navigation 3 host when " +
          "screen is omitted: Material 3 components, the theme, sample data and previews. The " +
          "host wraps the NavDisplay in a NavigationSuiteScaffold when navigation.destinations is " +
          "declared. A Wear " +
          "proposal exports as Wear Compose Material 3: the scaffolds, the scrolling list, the " +
          "theme with the exact colors the previewer draws, and previews. The proposal must be " +
          "valid.",
      schema = schema(string("screen", "Screen id. Omit to export every screen.")),
    ) { args ->
      controller.exportKotlin(args.string("screen"))
    }
    tool(
      name = "status",
      title = "Previewer status",
      description =
        "Returns the app version, catalog id, the open proposal, whether the window is visible, " +
          "the current screen and view, and the proposal's current validation errors.",
      schema = schema(),
    ) {
      PreviewJson.compact.encodeToString(
        PreviewStatus.serializer(),
        controller.status(),
      )
    }
  }

  /** The arguments `get_tree` and `render_view` share: which screen, at which view. */
  private fun treeSchema() =
    schema(
      string("screen", "Screen id to render. Defaults to the screen the window is on."),
      enum(
        "view",
        "A named view of the proposal's catalog. Defaults to the view the window shows.",
        Views.every.map { it.name },
      ),
    )

  private fun tool(
    name: String,
    title: String,
    description: String,
    schema: ToolSchema,
    readOnly: Boolean = true,
    run: suspend (JsonObject) -> String,
  ) =
    contentTool(name, title, description, schema, readOnly) { args ->
      listOf(TextContent(run(args)))
    }

  /** A tool whose result is a list of content items, such as a picture and its caption. */
  private fun contentTool(
    name: String,
    title: String,
    description: String,
    schema: ToolSchema,
    readOnly: Boolean = true,
    run: suspend (JsonObject) -> List<ContentBlock>,
  ) {
    server.addTool(
      name = name,
      description = description,
      inputSchema = schema,
      title = title,
      toolAnnotations =
        ToolAnnotations(title = title, readOnlyHint = readOnly, openWorldHint = false),
    ) { request ->
      guarded(request) { run(request.arguments ?: JsonObject(emptyMap())) }
    }
  }

  private suspend fun ClientConnection.guarded(
    request: CallToolRequest,
    body: suspend () -> List<ContentBlock>,
  ): CallToolResult =
    try {
      CallToolResult(content = body())
    } catch (e: CancellationException) {
      throw e
    } catch (e: PreviewException) {
      toolError(e.message ?: "failed")
    } catch (e: Exception) {
      toolError("${request.name} failed: ${e::class.simpleName}: ${e.message}")
    }

  private fun toolError(message: String) =
    CallToolResult(content = listOf(TextContent(message)), isError = true)

  companion object {
    const val SERVER_NAME = "android-design-preview"

    private const val INSTRUCTIONS =
      "Live previewer for Material 3 Expressive design proposals, for phone, tablet and desktop " +
        "(get_catalog m3-expressive) and for Wear OS, drawn in the watch and watch-large views " +
        "(get_catalog wear). open_proposal once, then edit the proposal file and call " +
        "check_proposal after each edit; use get_tree for the geometry of a failing view. Once " +
        "check_proposal passes, render_view gives one picture of a screen at a view as the single " +
        "visual confirmation; do not iterate on pictures. The file on disk is the source of " +
        "truth; there is no tool that edits it."
  }
}

/**
 * The picture or pictures, then one text item saying what each is, its size and where it was saved
 * (for harnesses that do not show MCP images). No structured content: some clients drop images when
 * it is set.
 */
private fun ViewRender.toContent(): List<ContentBlock> {
  val size = "${widthPx}x$heightPx px"
  val caption = buildString {
    append("Screen '$screen' at view '$view', $size, 1 px per dp. Saved to ${png.path}")
    end?.let {
      append(
        ". A scrolling list: the first image is the top of the list, the second is the list " +
          "scrolled to its end, saved to ${it.path}",
      )
    }
  }
  return listOfNotNull(png, end).map { it.toImage() } + TextContent(caption)
}

private fun RenderedPng.toImage() =
  ImageContent(data = Base64.getEncoder().encodeToString(bytes), mimeType = "image/png")

private class Prop(val name: String, val schema: JsonObject)

private fun string(name: String, description: String) =
  Prop(
    name,
    buildJsonObject {
      put("type", "string")
      put("description", description)
    },
  )

private fun bool(name: String, description: String) =
  Prop(
    name,
    buildJsonObject {
      put("type", "boolean")
      put("description", description)
    },
  )

private fun enum(name: String, description: String, values: List<String>) =
  Prop(
    name,
    buildJsonObject {
      put("type", "string")
      put("description", description)
      putJsonArray("enum") { values.forEach { add(JsonPrimitive(it)) } }
    },
  )

private fun stringList(name: String, description: String) =
  Prop(
    name,
    buildJsonObject {
      put("type", "array")
      put("description", description)
      putJsonObject("items") { put("type", "string") }
    },
  )

private fun enumList(name: String, description: String, values: List<String>) =
  Prop(
    name,
    buildJsonObject {
      put("type", "array")
      put("description", description)
      putJsonObject("items") {
        put("type", "string")
        putJsonArray("enum") { values.forEach { add(JsonPrimitive(it)) } }
      }
    },
  )

private fun stringOrList(name: String, description: String) =
  Prop(
    name,
    buildJsonObject {
      put("description", description)
      putJsonArray("anyOf") {
        add(buildJsonObject { put("type", "string") })
        add(
          buildJsonObject {
            put("type", "array")
            putJsonObject("items") { put("type", "string") }
          },
        )
      }
    },
  )

private fun schema(vararg props: Prop, required: List<String> = emptyList()): ToolSchema =
  ToolSchema(
    properties =
      buildJsonObject {
        props.forEach { putJsonObject(it.name) { it.schema.forEach { (k, v) -> put(k, v) } } }
      },
    required = required.ifEmpty { null },
  )

private fun JsonObject.string(name: String): String? =
  (this[name] as? JsonPrimitive)?.takeIf { it.isString }?.contentOrNull

private fun JsonObject.bool(name: String): Boolean? = (this[name] as? JsonPrimitive)?.booleanOrNull

private fun JsonObject.requiredString(name: String): String =
  string(name) ?: throw PreviewException("missing required argument '$name'")

/** A string argument given once or as a list; null when absent. */
private fun JsonObject.strings(name: String): List<String>? =
  when (val value = this[name]) {
    is JsonArray ->
      value.mapNotNull { (it as? JsonPrimitive)?.takeIf { p -> p.isString }?.contentOrNull }
    is JsonPrimitive -> listOfNotNull(value.takeIf { it.isString }?.contentOrNull)
    else -> null
  }
