package bot.charlies.androiddesign.preview.app

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.BindException
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

/**
 * The Unix domain socket that makes the app one instance. The instance binds it and owns the
 * window; agent processes connect and only relay stdio to it.
 */
object InstanceSocket {
  /**
   * Names a socket path that replaces the default, so a build can run its own instance beside an
   * installed one (the evals do this to test the branch build, not the installed app).
   */
  const val SOCKET_ENV = "ANDROID_DESIGN_PREVIEW_SOCKET"

  /**
   * The path in [SOCKET_ENV] when [env] sets it, else `~/Library/Application
   * Support/AndroidDesignPreview/mcp.sock`.
   */
  fun defaultPath(env: Map<String, String> = System.getenv()): Path =
    env[SOCKET_ENV]?.takeIf { it.isNotBlank() }?.let(Path::of) ?: homePath()

  private fun homePath(): Path =
    Path.of(
      System.getProperty("user.home"),
      "Library",
      "Application Support",
      "AndroidDesignPreview",
      "mcp.sock",
    )

  /** Connects to the instance listening on [path], or returns null when there is none. */
  fun connect(path: Path): SocketChannel? =
    try {
      SocketChannel.open(UnixDomainSocketAddress.of(path))
    } catch (_: IOException) {
      null
    }

  /** Polls [connect] every 50 ms for up to 10 s, for an instance that is still starting. */
  fun awaitConnect(path: Path): SocketChannel {
    val deadline = System.nanoTime() + CONNECT_TIMEOUT_NANOS
    while (System.nanoTime() < deadline) {
      connect(path)?.let {
        return it
      }
      Thread.sleep(CONNECT_POLL_MILLIS)
    }
    error("the instance did not start at $path")
  }

  /**
   * Binds [path]. Returns null when a live instance already holds it, in which case [connect]
   * reaches it. A socket file nobody listens on is stale: it is removed and the bind retried once.
   * A live socket is never deleted, so racing processes cannot bind two instances.
   */
  fun bind(path: Path): ServerSocketChannel? {
    Files.createDirectories(path.parent)
    tryBind(path)?.let {
      return it
    }
    val live = connect(path)
    if (live != null) {
      live.close()
      return null
    }
    Files.deleteIfExists(path)
    return tryBind(path)
  }

  private fun tryBind(path: Path): ServerSocketChannel? {
    val server = ServerSocketChannel.open(StandardProtocolFamily.UNIX)
    return try {
      server.bind(UnixDomainSocketAddress.of(path))
      server
    } catch (_: BindException) {
      server.close()
      null
    }
  }

  /**
   * Copies [input] to [channel] and [channel] to [output] until either side ends, then closes
   * [channel]. Streams are not taken from the channel, because the JDK's channel streams hold one
   * lock for reads and writes, which would block a write behind a waiting read.
   */
  fun relay(input: InputStream, output: OutputStream, channel: SocketChannel) {
    val ended = CountDownLatch(1)
    fun pump(pumpName: String, copy: () -> Unit) =
      thread(name = pumpName, isDaemon = true) {
        try {
          copy()
        } catch (_: IOException) {
          // The other side went away; ending the relay is the handling.
        } finally {
          ended.countDown()
        }
      }
    pump("relay-in") { input.copyTo(outputOf(channel)) }
    pump("relay-out") { channel.copyTo(output) }
    ended.await()
    channel.close()
  }

  fun inputOf(channel: SocketChannel): InputStream =
    object : InputStream() {
      override fun read(): Int {
        val one = ByteArray(1)
        return if (read(one, 0, 1) < 0) -1 else one[0].toInt() and 0xff
      }

      override fun read(b: ByteArray, off: Int, len: Int): Int =
        if (len == 0) 0 else channel.read(ByteBuffer.wrap(b, off, len))
    }

  fun outputOf(channel: SocketChannel): OutputStream =
    object : OutputStream() {
      override fun write(b: Int) = write(byteArrayOf(b.toByte()), 0, 1)

      override fun write(b: ByteArray, off: Int, len: Int) {
        val buffer = ByteBuffer.wrap(b, off, len)
        while (buffer.hasRemaining()) channel.write(buffer)
      }
    }

  private fun SocketChannel.copyTo(output: OutputStream) {
    val buffer = ByteArray(BUFFER_BYTES)
    val source = inputOf(this)
    while (true) {
      val read = source.read(buffer)
      if (read < 0) return
      output.write(buffer, 0, read)
      output.flush()
    }
  }

  private const val BUFFER_BYTES = 8192
  private const val CONNECT_POLL_MILLIS = 50L
  private const val CONNECT_TIMEOUT_NANOS = 10_000_000_000L
}
