package bot.charlies.androiddesign.preview.app

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import bot.charlies.androiddesign.preview.app.di.appModule
import bot.charlies.androiddesign.preview.app.preview.PreviewWindow
import bot.charlies.androiddesign.preview.app.preview.Sessions
import bot.charlies.androiddesign.preview.app.preview.TreeRenderer
import bot.charlies.androiddesign.preview.domain.PreviewException
import bot.charlies.androiddesign.preview.domain.ProposalRepository
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.desktop.AppReopenedListener
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.ServerSocketChannel
import java.nio.file.Files
import java.nio.file.Path
import kotlin.concurrent.thread
import kotlin.system.exitProcess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import org.koin.core.context.startKoin

/**
 * Runs with no arguments. No process an agent starts ever becomes the instance, because a harness
 * stops its MCP child when it exits and that would end every other agent's session. An agent's
 * process only relays its stdio to the instance socket, spawning the instance detached when none
 * answers. The owner launching the app from Finder runs the instance directly.
 */
fun runPreviewApp() {
  // Finder and `open` start the process from launchd, whose pid is 1; an agent is its own parent.
  val launchedByUser = ProcessHandle.current().parent().map { it.pid() == 1L }.orElse(true)
  if (launchedByUser) runInstance(launchedByUser = true) else relayThroughInstance()
}

/** Connects to the instance, starting it first when none runs, and relays stdio to it. */
private fun relayThroughInstance(): Nothing {
  val socketPath = InstanceSocket.defaultPath()
  val channel =
    InstanceSocket.connect(socketPath)
      ?: run {
        InstanceLauncher.spawn()
        InstanceSocket.awaitConnect(socketPath)
      }
  System.setProperty("java.awt.headless", "true")
  InstanceSocket.relay(System.`in`, FileOutputStream(FileDescriptor.out), channel)
  exitProcess(0)
}

/**
 * Owns the window and serves one MCP session per connection on the instance socket.
 * [launchedByUser] keeps it up with no session; a spawned instance exits when its last agent
 * leaves.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun runInstance(launchedByUser: Boolean) {
  val socketPath = InstanceSocket.defaultPath()
  val listener = InstanceSocket.bind(socketPath)
  if (listener == null) {
    System.err.println("an instance already runs at $socketPath")
    exitProcess(0)
  }

  AppNap.optOutInBackground()
  Runtime.getRuntime().addShutdownHook(Thread { Files.deleteIfExists(socketPath) })

  val koin = startKoin { modules(appModule) }.koin
  val window: PreviewWindow = koin.get()
  val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  val sessions =
    Sessions(
      repository = koin.get<ProposalRepository>(),
      renderer = koin.get<TreeRenderer>(),
      window = window,
      scope = scope,
      version = AppInfo.VERSION,
      launchedByUser = launchedByUser,
      onIdle = { exitProcess(0) },
    )

  if (launchedByUser) window.show()
  thread(name = "accept", isDaemon = true) { acceptAgents(listener, sessions, scope) }

  // A click on the Dock icon brings the window back.
  if (
    Desktop.isDesktopSupported() &&
      Desktop.getDesktop().isSupported(Desktop.Action.APP_EVENT_REOPENED)
  ) {
    Desktop.getDesktop().addAppEventListener(AppReopenedListener { window.show() })
  }

  ComposeUiFlags.isMediaQueryIntegrationEnabled = true
  application {
    val visible by window.visible.collectAsState()
    Window(
      // With an agent connected the process keeps serving it, so closing only hides the window.
      onCloseRequest = { if (sessions.hasAgent()) window.hide() else exitProcess(0) },
      visible = visible,
      title = "Android Design Preview",
      state = rememberWindowState(size = DpSize(1280.dp, 900.dp)),
    ) {
      MenuBar {
        Menu("File") {
          Item("Open Proposal…", shortcut = KeyShortcut(Key.O, meta = true)) {
            chooseProposal(this@Window.window)?.let { path ->
              scope.launch { openLocal(sessions, path) }
            }
          }
        }
      }
      PreviewWindowContent(sessions)
    }
  }
}

/** Opens an agent session for every connection to the instance socket. */
private fun acceptAgents(listener: ServerSocketChannel, sessions: Sessions, scope: CoroutineScope) {
  while (true) {
    val channel =
      try {
        listener.accept()
      } catch (_: IOException) {
        return
      }
    val transport =
      StdioServerTransport(
        InstanceSocket.inputOf(channel).asSource().buffered(),
        InstanceSocket.outputOf(channel).asSink().buffered(),
      )
    transport.onClose { channel.close() }
    scope.launch { sessions.openAgent(transport) }
  }
}

/** Asks for a proposal file with the native open dialog; null when the owner cancels. */
private fun chooseProposal(window: java.awt.Frame): String? {
  val dialog = FileDialog(window, "Open Proposal", FileDialog.LOAD)
  dialog.setFilenameFilter { _, name -> name.endsWith(".proposal.json") }
  dialog.isVisible = true
  val file = dialog.file ?: return null
  return Path.of(dialog.directory, file).toString()
}

private suspend fun openLocal(sessions: Sessions, path: String) {
  try {
    sessions.openLocal(path)
  } catch (e: PreviewException) {
    System.err.println("cannot open $path: ${e.message}")
  }
}
