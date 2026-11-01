package bot.charlies.androiddesign.preview.app.di

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.app.preview.AppPreviewController
import bot.charlies.androiddesign.preview.app.preview.HeadlessTreeRenderer
import bot.charlies.androiddesign.preview.app.preview.PreviewWindow
import bot.charlies.androiddesign.preview.app.preview.TreeRenderer
import bot.charlies.androiddesign.preview.app.preview.WindowVisibility
import bot.charlies.androiddesign.preview.data.FileProposalRepository
import bot.charlies.androiddesign.preview.domain.PreviewController
import bot.charlies.androiddesign.preview.domain.ProposalRepository
import bot.charlies.androiddesign.preview.feature.preview.viewer.ViewerViewModel
import bot.charlies.androiddesign.preview.mcp.PreviewMcpServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.bind
import org.koin.dsl.module

/** Wires the app: data, the viewer, the controller the MCP tools call, and the MCP server. */
val appModule = module {
  single<ProposalRepository> { FileProposalRepository() }
  single { ViewerViewModel() }
  single { WindowVisibility() } bind PreviewWindow::class
  single<TreeRenderer> { HeadlessTreeRenderer() }
  single<PreviewController> {
    AppPreviewController(
      repository = get(),
      viewer = get(),
      window = get(),
      renderer = get(),
      scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
      version = AppInfo.VERSION,
    )
  }
  single { PreviewMcpServer(get(), AppInfo.VERSION) }
}
