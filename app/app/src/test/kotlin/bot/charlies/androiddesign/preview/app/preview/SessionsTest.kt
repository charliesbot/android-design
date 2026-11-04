package bot.charlies.androiddesign.preview.app.preview

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.data.FileProposalRepository
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.StdioClientTransport
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered

/** Opens agent sessions over piped transports, with no window. */
class SessionsTest {
  private val fixtures = Path.of(System.getProperty("fixtures.dir"))
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private var idled = false
  private val clients = mutableListOf<Client>()

  private fun sessions(launchedByUser: Boolean = false) =
    Sessions(
      repository = FileProposalRepository(),
      renderer = HeadlessTreeRenderer(),
      window = PreviewWindow(),
      scope = scope,
      version = AppInfo.VERSION,
      launchedByUser = launchedByUser,
      onIdle = { idled = true },
    )

  /** Opens an agent session and returns it with the client that talks to it. */
  private fun Sessions.connect(): Pair<PreviewSession, Client> = runBlocking {
    val clientOut = PipedOutputStream()
    val serverIn = PipedInputStream(clientOut, 1 shl 16)
    val serverOut = PipedOutputStream()
    val clientIn = PipedInputStream(serverOut, 1 shl 16)
    val session =
      openAgent(StdioServerTransport(serverIn.asSource().buffered(), serverOut.asSink().buffered()))
    val client = Client(Implementation(name = "test-client", version = "0"))
    withTimeout(10_000) {
      client.connect(
        StdioClientTransport(clientIn.asSource().buffered(), clientOut.asSink().buffered()),
      )
    }
    clients += client
    session to client
  }

  private fun workspace(): Path {
    val dir = Files.createTempDirectory("sessions")
    Files.copy(fixtures.resolve("DESIGN.md"), dir.resolve("DESIGN.md"))
    return dir.resolve("plates.proposal.json").also {
      Files.copy(fixtures.resolve("plates.proposal.json"), it)
    }
  }

  private suspend fun await(condition: () -> Boolean) =
    withTimeout(10_000) {
      while (!condition()) delay(20)
    }

  @AfterTest
  fun stop() {
    runBlocking { clients.forEach { runCatching { it.close() } } }
    scope.cancel()
  }

  @Test
  fun `two agents get two numbered sessions and the first stays selected`() {
    val sessions = sessions()
    val (first) = sessions.connect()
    val (second) = sessions.connect()

    assertEquals(listOf(first, second), sessions.sessions.value)
    assertEquals(listOf("Session 1", "Session 2"), sessions.sessions.value.map { it.name.value })
    assertSame(first, sessions.selected.value)
  }

  @Test
  fun `closing a transport removes its session and selects the one left`() {
    val sessions = sessions()
    val (first) = sessions.connect()
    val (second, secondClient) = sessions.connect()

    runBlocking {
      secondClient.close()
      await { sessions.sessions.value.size == 1 }
    }

    assertEquals(listOf(first), sessions.sessions.value)
    assertSame(first, sessions.selected.value)
    assertFalse(idled, "one session is still open")
    assertFalse(second in sessions.sessions.value)
  }

  @Test
  fun `a session is named after its proposal once one is open`() {
    val sessions = sessions()
    val (session, client) = sessions.connect()

    runBlocking {
      withTimeout(30_000) {
        client.callTool("open_proposal", mapOf("path" to workspace().toString()))
      }
      await { session.name.value != "Session 1" }
    }

    assertEquals("Tonight's plate", session.name.value)
  }

  @Test
  fun `open_proposal on the second session selects it while the first stays selected until then`() {
    val sessions = sessions()
    val (first) = sessions.connect()
    val (second, secondClient) = sessions.connect()
    assertSame(first, sessions.selected.value)

    runBlocking {
      withTimeout(30_000) {
        secondClient.callTool("open_proposal", mapOf("path" to workspace().toString()))
      }
    }

    assertSame(second, sessions.selected.value)
  }

  @Test
  fun `a local session has no server and closes on request`() {
    val sessions = sessions(launchedByUser = true)
    val local = runBlocking { sessions.openLocal(workspace().toString()) }

    assertFalse(local.isAgent)
    assertFalse(sessions.hasAgent())
    assertEquals("Tonight's plate", local.name.value)

    sessions.close(local)

    assertTrue(sessions.sessions.value.isEmpty())
    assertEquals(null, sessions.selected.value)
  }

  @Test
  fun `a process an agent started exits with its last session and one the owner started stays`() {
    val agentStarted = sessions(launchedByUser = false)
    assertFalse(agentStarted.staysAlive())
    val (_, client) = agentStarted.connect()
    assertTrue(agentStarted.staysAlive())
    runBlocking {
      client.close()
      await { idled }
    }

    val userStarted = sessions(launchedByUser = true)
    assertTrue(userStarted.staysAlive())
  }
}
