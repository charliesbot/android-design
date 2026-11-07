package bot.charlies.androiddesign.preview.app

import java.io.ByteArrayOutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InstanceSocketTest {
  private val socket = Files.createTempDirectory("instance").resolve("s.sock")

  @Test
  fun `the socket path comes from the environment when it is set`() {
    val override =
      InstanceSocket.defaultPath(mapOf(InstanceSocket.SOCKET_ENV to "/tmp/adp-x/s.sock"))
    assertEquals(Path.of("/tmp/adp-x/s.sock"), override)
    val unset = InstanceSocket.defaultPath(emptyMap())
    assertTrue(unset.endsWith("AndroidDesignPreview/mcp.sock"), unset.toString())
    assertEquals(unset, InstanceSocket.defaultPath(mapOf(InstanceSocket.SOCKET_ENV to " ")))
  }

  @Test
  fun `a path longer than sun_path fails at once with the cause and the way out`() {
    val tooLong = Path.of("/tmp", "a".repeat(InstanceSocket.MAX_PATH_BYTES), "s.sock")
    val started = System.nanoTime()
    val bindError = assertFailsWith<IllegalStateException> { InstanceSocket.bind(tooLong) }
    val awaitError = assertFailsWith<IllegalStateException> { InstanceSocket.awaitConnect(tooLong) }
    assertTrue(System.nanoTime() - started < TimeUnit.SECONDS.toNanos(2), "no connect wait")
    for (error in listOf(bindError, awaitError)) {
      val message = error.message.orEmpty()
      assertTrue("${InstanceSocket.MAX_PATH_BYTES}" in message, message)
      assertTrue(tooLong.toString() in message, message)
      assertTrue(InstanceSocket.SOCKET_ENV in message, message)
    }
  }

  @Test
  fun `bytes flow both ways and the relay returns when its input ends`() {
    val server = assertNotNull(InstanceSocket.bind(socket))
    val client = assertNotNull(InstanceSocket.connect(socket))
    val accepted = server.accept()

    val stdinWriter = PipedOutputStream()
    val stdin = PipedInputStream(stdinWriter)
    val stdout = ByteArrayOutputStream()
    val relay = thread { InstanceSocket.relay(stdin, stdout, client) }

    stdinWriter.write("to instance".toByteArray())
    stdinWriter.flush()
    val received = ByteBuffer.allocate("to instance".length)
    while (received.hasRemaining()) accepted.read(received)
    assertEquals("to instance", String(received.array()))

    accepted.write(ByteBuffer.wrap("to agent".toByteArray()))
    val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10)
    while (stdout.size() < "to agent".length && System.nanoTime() < deadline) Thread.sleep(10)
    assertContentEquals("to agent".toByteArray(), stdout.toByteArray())

    stdinWriter.close()
    relay.join(10_000)
    assertTrue(!relay.isAlive, "the relay returns once its input ends")
    server.close()
  }

  @Test
  fun `connect finds nothing on a stale socket file and bind replaces it`() {
    Files.createFile(socket)

    assertNull(InstanceSocket.connect(socket))
    assertNotNull(InstanceSocket.bind(socket)).close()
  }

  @Test
  fun `a second bind loses to a live instance and leaves its socket alone`() {
    val first = assertNotNull(InstanceSocket.bind(socket))

    assertNull(InstanceSocket.bind(socket))
    assertNotNull(InstanceSocket.connect(socket)).close()
    first.close()
  }
}
