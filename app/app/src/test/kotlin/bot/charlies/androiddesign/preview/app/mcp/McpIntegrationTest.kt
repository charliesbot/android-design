package bot.charlies.androiddesign.preview.app.mcp

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.app.preview.AppPreviewController
import bot.charlies.androiddesign.preview.app.preview.HeadlessTreeRenderer
import bot.charlies.androiddesign.preview.app.preview.WindowVisibility
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
  private val window = WindowVisibility()
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
  fun `the server lists exactly the seven tools with schemas and descriptions`() {
    connect()
    val tools = runBlocking { client.listTools().tools }
    assertEquals(PreviewMcpServer.TOOL_NAMES.sorted(), tools.map { it.name }.sorted())
    assertEquals(
      setOf(
        "open_proposal",
        "get_tree",
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
    assertEquals(setOf("device", "dark", "seed", "variant"), view.keys)
    assertEquals(
      listOf("phone", "foldable", "tablet", "desktop"),
      view.getValue("device").jsonObject.getValue("enum").jsonArray.map {
        it.jsonPrimitive.content
      },
    )
    assertEquals(setOf("screen", "view"), byName.getValue("get_tree").inputSchema.properties?.keys)
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

    val catalog = call("get_catalog").json()
    assertEquals("1.0", catalog.getValue("protocolVersion").jsonPrimitive.content)

    val kotlin = call("export_kotlin", mapOf("screen" to "plates")).text()
    assertTrue("@Composable" in kotlin && "fun PlatesScreen" in kotlin)

    val status = call("status").json()
    assertEquals(AppInfo.VERSION, status.getValue("version").jsonPrimitive.content)
    assertEquals(file.toString(), status.getValue("proposal").jsonPrimitive.content)
    assertTrue(status.getValue("windowVisible").jsonPrimitive.boolean)
    assertEquals("plates", status.getValue("screen").jsonPrimitive.content)
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

    assertEquals(true, call("set_view", mapOf("device" to "watch")).isError)
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
