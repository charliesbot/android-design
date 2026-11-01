package bot.charlies.androiddesign.preview.mcp

import bot.charlies.androiddesign.preview.domain.PreviewController
import bot.charlies.androiddesign.preview.domain.PreviewException
import bot.charlies.androiddesign.preview.domain.ProposalCheck
import bot.charlies.androiddesign.preview.domain.ViewChange
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
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolAnnotations
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
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
 * The MCP face of the previewer: eight tools that call a [PreviewController]. It does not own any
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
        bot.charlies.androiddesign.preview.domain.OpenedProposal.serializer(),
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
          "labels, one FAB, primary action). A view passes when no finding has severity error. " +
          "With no arguments it describes what the window shows: its screen, device, brightness, " +
          "color overrides, open overlays and selections. With screen or view it renders that " +
          "screen at that view without a window. Read this instead of looking at screenshots.",
      schema =
        schema(
          string("screen", "Screen id to render. Defaults to the screen the window is on."),
          enum(
            "view",
            "A named view. Defaults to the view the window shows.",
            Views.all.map { it.name },
          ),
        ),
    ) { args ->
      val tree = controller.getTree(args.string("screen"), args.string("view"))
      PreviewJson.compact.encodeToString(LayoutTree.serializer(), tree)
    }
    tool(
      name = "set_view",
      title = "Change the window's view",
      description =
        "Changes what the window shows: device, brightness, seed color, palette variant. Omitted " +
          "values stay as they are. These choices only affect the window and get_tree without " +
          "arguments; they are never written to the proposal.",
      schema =
        schema(
          enum("device", "Device frame.", Device.entries.map { it.wire }),
          bool("dark", "True for dark brightness, false for light."),
          string("seed", "Seed color as #RRGGBB."),
          enum("variant", "Palette variant.", PaletteVariant.entries.map { it.wire }),
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
          variant =
            args.string("variant")?.let {
              PaletteVariant.fromWire(it)
                ?: throw PreviewException(
                  "unknown variant '$it' (variants: ${PaletteVariant.entries.joinToString { v -> v.wire }})",
                )
            },
        )
      PreviewJson.compact.encodeToString(
        bot.charlies.androiddesign.preview.domain.ViewState.serializer(),
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
          "proposal passes: per screen and view, pass plus every error finding (rule, component " +
          "id, detail), a count of info findings, and a layout summary: the navigation component " +
          "shown (NavigationBar, NavigationRail, FloatingToolbar or none) and the screens shown " +
          "left to right, each with the direct children of its content root as \"Type id width\" " +
          "(dp). Compare the layout of two views to see that a wider one reveals more, with no " +
          "need for get_tree. No other geometry; call get_tree for a failing view's bounds. With " +
          "no arguments it checks every screen at light, dark, foldable, tablet and desktop. Call " +
          "this after each edit instead of get_tree per view.",
      schema =
        schema(
          stringList("screens", "Screen ids to check. Defaults to every screen."),
          enumList(
            "views",
            "Views to check. Defaults to all five.",
            Views.all.map { it.name },
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
          "the color, type, shape and spacing tokens and the action functions. With component, " +
          "returns the full JSON Schema entries of those components. A proposal may only use " +
          "what the catalog declares.",
      schema =
        schema(
          stringOrList(
            "component",
            "A component name or a list of names, for their full JSON Schema entries. Omit for " +
              "the compact catalog.",
          ),
        ),
    ) { args ->
      controller.getCatalog(args.strings("component"))
    }
    tool(
      name = "export_kotlin",
      title = "Export to Jetpack Compose",
      description =
        "Returns Kotlin source for one screen, or for every screen with the Navigation 3 host when " +
          "screen is omitted: Material 3 components, the theme, sample data and previews. The " +
          "proposal must be valid.",
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
        bot.charlies.androiddesign.preview.domain.PreviewStatus.serializer(),
        controller.status(),
      )
    }
  }

  private fun tool(
    name: String,
    title: String,
    description: String,
    schema: ToolSchema,
    readOnly: Boolean = true,
    run: suspend (JsonObject) -> String,
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
    body: suspend () -> String,
  ): CallToolResult =
    try {
      CallToolResult(content = listOf(TextContent(body())))
    } catch (e: CancellationException) {
      throw e
    } catch (e: PreviewException) {
      error(e.message ?: "failed")
    } catch (e: Exception) {
      error("${request.name} failed: ${e::class.simpleName}: ${e.message}")
    }

  private fun error(message: String) =
    CallToolResult(content = listOf(TextContent(message)), isError = true)

  companion object {
    const val SERVER_NAME = "android-design-preview"

    private const val INSTRUCTIONS =
      "Live previewer for Material 3 Expressive design proposals. open_proposal once, then edit " +
        "the proposal file and call check_proposal after each edit; use get_tree for the geometry " +
        "of a failing view. The file on disk is the source of truth; there is no tool that edits " +
        "it."
  }
}

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
