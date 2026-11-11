package bot.charlies.androiddesign.preview.app.mcp

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.app.preview.AppPreviewController
import bot.charlies.androiddesign.preview.app.preview.HeadlessTreeRenderer
import bot.charlies.androiddesign.preview.app.preview.PreviewWindow
import bot.charlies.androiddesign.preview.data.FileProposalRepository
import bot.charlies.androiddesign.preview.feature.preview.viewer.ViewerViewModel
import bot.charlies.androiddesign.preview.mcp.PreviewMcpServer
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.StdioClientTransport
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Starts the real server with the real controller in this process, joined to a client by pipes, and
 * calls every tool with the plates fixture.
 */
class McpIntegrationTest {
  private val fixtures = Path.of(System.getProperty("fixtures.dir"))
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private val window = PreviewWindow()
  private val closed = CountDownLatch(1)

  private val clientOut = PipedOutputStream()
  private val serverIn = PipedInputStream(clientOut, 1 shl 16)
  private val serverOut = PipedOutputStream()
  private val clientIn = PipedInputStream(serverOut, 1 shl 16)

  private val controller =
    AppPreviewController(
      repository = FileProposalRepository(),
      viewer = ViewerViewModel(),
      window = window,
      renderer = HeadlessTreeRenderer(),
      scope = scope,
      version = AppInfo.VERSION,
    )

  private val client = Client(Implementation(name = "test-client", version = "0"))

  private fun connect() = runBlocking {
    val serverTransport =
      StdioServerTransport(serverIn.asSource().buffered(), serverOut.asSink().buffered())
    serverTransport.onClose { closed.countDown() }
    PreviewMcpServer(controller, AppInfo.VERSION).connect(serverTransport)
    withTimeout(10_000) {
      client.connect(
        StdioClientTransport(clientIn.asSource().buffered(), clientOut.asSink().buffered()),
      )
    }
  }

  @AfterTest
  fun stop() {
    runBlocking { runCatching { client.close() } }
    scope.cancel()
  }

  private fun workspace(): Path {
    val dir = Files.createTempDirectory("mcp")
    Files.copy(fixtures.resolve("DESIGN.md"), dir.resolve("DESIGN.md"))
    return dir.resolve("plates.proposal.json").also {
      Files.copy(fixtures.resolve("plates.proposal.json"), it)
    }
  }

  private fun call(name: String, args: Map<String, Any?> = emptyMap()): CallToolResult =
    runBlocking {
      withTimeout(30_000) { assertNotNull(client.callTool(name, args)) }
    }

  private fun CallToolResult.text(): String = (content.single() as TextContent).text

  private fun CallToolResult.json(): JsonObject = Json.parseToJsonElement(text()).jsonObject

  @Test
  fun `the server lists exactly the eight tools with schemas and descriptions`() {
    connect()
    val tools = runBlocking { client.listTools().tools }
    assertEquals(
      setOf(
        "open_proposal",
        "get_tree",
        "check_proposal",
        "set_view",
        "show_preview",
        "get_catalog",
        "export_kotlin",
        "status",
      ),
      tools.map { it.name }.toSet(),
    )
    for (tool in tools) {
      assertTrue(tool.description.orEmpty().length > 40, "${tool.name} has a real description")
      assertEquals("object", tool.inputSchema.type)
    }
    val byName = tools.associateBy { it.name }
    assertEquals(listOf("path"), byName.getValue("open_proposal").inputSchema.required)
    val view = byName.getValue("set_view").inputSchema.properties
    assertNotNull(view)
    assertEquals(setOf("device", "view", "dark", "seed", "variant", "screen"), view.keys)
    assertEquals(
      listOf("phone", "foldable", "tablet", "desktop", "watch"),
      view.getValue("device").jsonObject.getValue("enum").jsonArray.map {
        it.jsonPrimitive.content
      },
    )
    assertEquals(setOf("screen", "view"), byName.getValue("get_tree").inputSchema.properties?.keys)
    val check = byName.getValue("check_proposal").inputSchema.properties
    assertNotNull(check)
    assertEquals(setOf("screens", "views"), check.keys)
    assertEquals(
      listOf("light", "dark", "foldable", "tablet", "desktop", "watch", "watch-large"),
      check
        .getValue("views")
        .jsonObject
        .getValue("items")
        .jsonObject
        .getValue("enum")
        .jsonArray
        .map { it.jsonPrimitive.content },
    )
    assertEquals(
      setOf("component", "catalog"),
      byName.getValue("get_catalog").inputSchema.properties?.keys,
    )
  }

  @Test
  fun `a wear proposal opens, checks at the watch views and switches between them`() {
    connect()
    // Alone in its folder: the fixtures folder's DESIGN.md is the phone one.
    val path =
      Files.createTempDirectory("mcp-wear").resolve("wear.proposal.json").also {
        Files.copy(fixtures.resolve("wear.proposal.json"), it)
      }
    val opened = call("open_proposal", mapOf("path" to path.toString())).json()
    assertTrue(opened.getValue("valid").jsonPrimitive.boolean, opened.toString())

    val check = call("check_proposal").json()
    assertTrue(check.getValue("pass").jsonPrimitive.boolean, check.toString())
    val screens = check.getValue("screens").jsonObject
    assertEquals(
      listOf("home", "settings", "settings+sure", "adjust", "progress"),
      screens.keys.toList(),
    )
    for ((screen, views) in screens) {
      assertEquals(
        listOf("watch", "watch-large"),
        views.jsonObject.keys.toList(),
        "views of $screen",
      )
    }
    assertEquals(true, call("check_proposal", mapOf("views" to listOf("tablet"))).isError)

    val tree = call("get_tree", mapOf("screen" to "settings", "view" to "watch-large")).json()
    assertEquals(
      "watch-large",
      tree.getValue("view").jsonObject.getValue("name").jsonPrimitive.content,
    )
    assertTrue(
      tree.getValue("nodes").jsonArray.any {
        it.jsonObject.getValue("id").jsonPrimitive.content == "mode"
      },
    )

    val large = call("set_view", mapOf("view" to "watch-large")).json()
    assertEquals("watch", large.getValue("device").jsonPrimitive.content)
    assertEquals("watch-large", large.getValue("view").jsonPrimitive.content)
    assertEquals(true, call("set_view", mapOf("device" to "tablet")).isError)
    assertEquals(true, call("set_view", mapOf("view" to "tablet")).isError)

    assertTrue("EdgeButton" in call("get_catalog", mapOf("catalog" to "wear")).text())
  }

  @Test
  fun `every tool works against the plates fixture`() {
    connect()
    val file = workspace()

    val opened = call("open_proposal", mapOf("path" to file.toString())).json()
    assertTrue(opened.getValue("valid").jsonPrimitive.boolean, opened.toString())
    assertEquals(
      listOf("plates", "plate_detail"),
      opened.getValue("screens").jsonArray.map { it.jsonPrimitive.content },
    )
    assertTrue(window.visible.value)

    val tree = call("get_tree").json()
    assertEquals("plates", tree.getValue("screen").jsonPrimitive.content)
    assertEquals("light", tree.getValue("view").jsonObject.getValue("name").jsonPrimitive.content)
    val ids =
      tree.getValue("nodes").jsonArray.map { it.jsonObject.getValue("id").jsonPrimitive.content }
    assertTrue(ids.containsAll(listOf("root", "bar", "new_plate")), ids.toString())

    val other = call("get_tree", mapOf("screen" to "plate_detail", "view" to "tablet")).json()
    assertEquals("plate_detail", other.getValue("screen").jsonPrimitive.content)
    assertEquals("tablet", other.getValue("view").jsonObject.getValue("name").jsonPrimitive.content)

    val view =
      call(
          "set_view",
          mapOf("device" to "tablet", "dark" to true, "seed" to "#0061A4", "variant" to "vibrant"),
        )
        .json()
    assertEquals("tablet", view.getValue("device").jsonPrimitive.content)
    assertTrue(view.getValue("dark").jsonPrimitive.boolean)
    assertEquals("#0061A4", view.getValue("seed").jsonPrimitive.content)
    assertEquals("vibrant", view.getValue("variant").jsonPrimitive.content)
    val afterView = call("get_tree").json()
    assertTrue(afterView.getValue("view").jsonObject.getValue("dark").jsonPrimitive.boolean)

    window.hide()
    assertFalse(window.visible.value)
    assertTrue(call("show_preview").text().isNotBlank())
    assertTrue(window.visible.value)

    val check = call("check_proposal").json()
    assertTrue(check.getValue("pass").jsonPrimitive.boolean, check.toString())
    assertEquals(
      listOf("plates", "plates+new_sheet", "plate_detail"),
      check.getValue("screens").jsonObject.keys.toList(),
    )
    assertEquals(
      listOf("light", "dark", "foldable", "tablet", "desktop"),
      check.getValue("screens").jsonObject.getValue("plates").jsonObject.keys.toList(),
    )
    val some =
      call(
          "check_proposal",
          mapOf("screens" to listOf("plate_detail"), "views" to listOf("tablet")),
        )
        .json()
    assertEquals(
      listOf("tablet"),
      some.getValue("screens").jsonObject.getValue("plate_detail").jsonObject.keys.toList(),
    )

    val summary = call("get_catalog").text()
    assertTrue(summary.length < 20_000, "the compact catalog is ${summary.length} characters")
    assertTrue("Slider: " in summary && "colorRole: " in summary && "navigate(" in summary)
    val one = call("get_catalog", mapOf("component" to "Slider")).json()
    assertEquals(listOf("Slider"), one.keys.toList())
    val several = call("get_catalog", mapOf("component" to listOf("Button", "Slider"))).json()
    assertEquals(listOf("Button", "Slider"), several.keys.toList())
    assertEquals(
      "Slider",
      several
        .getValue("Slider")
        .jsonObject
        .getValue("properties")
        .jsonObject
        .getValue("component")
        .jsonObject
        .getValue("const")
        .jsonPrimitive
        .content,
    )

    val kotlin = call("export_kotlin", mapOf("screen" to "plates")).text()
    assertTrue("@Composable" in kotlin && "fun PlatesScreen" in kotlin)

    val status = call("status").json()
    assertEquals(AppInfo.VERSION, status.getValue("version").jsonPrimitive.content)
    assertEquals(file.toString(), status.getValue("proposal").jsonPrimitive.content)
    assertTrue(status.getValue("windowVisible").jsonPrimitive.boolean)
    assertEquals("plates", status.getValue("screen").jsonPrimitive.content)
  }

  @Test
  fun `check_proposal returns the layout of each view so adaptation can be compared`() {
    connect()
    call("open_proposal", mapOf("path" to workspace().toString()))

    val check =
      call(
          "check_proposal",
          mapOf("screens" to listOf("plate_detail"), "views" to listOf("light", "tablet")),
        )
        .json()

    fun layout(view: String) =
      check
        .getValue("screens")
        .jsonObject
        .getValue("plate_detail")
        .jsonObject
        .getValue(view)
        .jsonObject
        .getValue("layout")
        .jsonObject
    fun panes(view: String) = layout(view).getValue("panes").jsonObject
    assertEquals(listOf("plate_detail"), panes("light").keys.toList(), "one pane on a phone")
    assertEquals(listOf("plates", "plate_detail"), panes("tablet").keys.toList(), "two on a tablet")
    assertEquals("none", layout("light").getValue("nav").jsonPrimitive.content)
    val regions =
      panes("tablet").getValue("plate_detail").jsonArray.map { it.jsonPrimitive.content }
    assertTrue(regions.any { it.startsWith("Button cook ") }, regions.toString())
  }

  @Test
  fun `check_proposal returns exactly the documented shape`() {
    connect()
    call("open_proposal", mapOf("path" to workspace().toString()))

    val text =
      call(
          "check_proposal",
          mapOf("screens" to listOf("plate_detail"), "views" to listOf("light", "dark")),
        )
        .text()

    // The one field that varies between runs is how long the renders took.
    val withoutMs = text.replace(Regex(""","ms":\d+"""), "")
    assertEquals(
      """{"pass":true,"colors":{"light":{"background":"#FFF8F6","onBackground":"#522612","onPrimary":"#FFF7F6","onSurface":"#522612","onSurfaceVariant":"#87513A","primary":"#9C442F","surface":"#FFF8F6"},"dark":{"background":"#1B0A04","onBackground":"#FFDFD3","onPrimary":"#6D3022","onSurface":"#FFDFD3","onSurfaceVariant":"#D49F8A","primary":"#FFB8A8","surface":"#1B0A04"}},"screens":{"plate_detail":{"light":{"pass":true,"layout":{"nav":"none","panes":{"plate_detail":["Text hero_title 379","Text desc 379","Button cook 203"]}}},"dark":{"same":"light"}}}}""",
      withoutMs,
    )
  }

  @Test
  fun `a problem the agent can fix is a tool error that says what to do`() {
    connect()
    val noProposal = call("get_tree")
    assertEquals(true, noProposal.isError)
    assertTrue("open_proposal" in noProposal.text())

    val file = workspace()
    call("open_proposal", mapOf("path" to file.toString()))
    val unknownScreen = call("get_tree", mapOf("screen" to "nope"))
    assertEquals(true, unknownScreen.isError)
    assertTrue("plates" in unknownScreen.text())

    assertEquals(true, call("check_proposal", mapOf("screens" to listOf("nope"))).isError)
    assertEquals(true, call("get_catalog", mapOf("component" to "Nope")).isError)
    assertEquals(true, call("set_view", mapOf("device" to "toaster")).isError)
    assertEquals(true, call("set_view", mapOf("seed" to "blue")).isError)
    assertEquals(true, call("open_proposal").isError, "path is required")
    assertEquals(true, call("open_proposal", mapOf("path" to "/no/such/file.json")).isError)
    assertTrue(call("status").isError != true, "status never fails")
  }

  @Test
  fun `the server notices when the client goes away`() {
    connect()
    runBlocking { client.close() }
    assertTrue(closed.await(10, TimeUnit.SECONDS), "transport.onClose fired")
  }
}
