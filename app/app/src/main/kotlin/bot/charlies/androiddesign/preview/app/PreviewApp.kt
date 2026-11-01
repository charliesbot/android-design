package bot.charlies.androiddesign.preview.app

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import bot.charlies.androiddesign.preview.app.di.appModule
import bot.charlies.androiddesign.preview.app.preview.PreviewWindow
import bot.charlies.androiddesign.preview.feature.preview.viewer.ViewerScreen
import bot.charlies.androiddesign.preview.feature.preview.viewer.ViewerViewModel
import bot.charlies.androiddesign.preview.mcp.PreviewMcpServer
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import java.awt.Desktop
import java.awt.desktop.AppReopenedListener
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream
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
 * Runs with no arguments: the MCP server on stdio, plus the preview window, hidden until
 * `open_proposal` or `show_preview` shows it. Closing the window hides it and the process keeps
 * serving; it exits when the client closes stdin.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun runPreviewApp() {
  // stdout carries only JSON-RPC. Keep the real one for the transport and send everything else
  // that prints to System.out (libraries, stray println) to stderr.
  val protocolOut = PrintStream(FileOutputStream(FileDescriptor.out), false)
  System.setOut(System.err)

  AppNap.optOutInBackground()

  val koin = startKoin { modules(appModule) }.koin
  val viewer: ViewerViewModel = koin.get()
  val window: PreviewWindow = koin.get()
  val server: PreviewMcpServer = koin.get()

  val transport =
    StdioServerTransport(System.`in`.asSource().buffered(), protocolOut.asSink().buffered())
  // Server.onClose only fires on an explicit close; the transport's fires when stdin ends.
  transport.onClose { exitProcess(0) }
  CoroutineScope(SupervisorJob() + Dispatchers.Default).launch { server.connect(transport) }

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
      onCloseRequest = window::hide,
      visible = visible,
      title = "Android Design Preview",
      state = rememberWindowState(size = DpSize(1280.dp, 900.dp)),
    ) {
      ViewerScreen(viewer)
    }
  }
}
