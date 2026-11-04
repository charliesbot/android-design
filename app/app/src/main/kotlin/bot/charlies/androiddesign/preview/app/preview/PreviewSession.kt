package bot.charlies.androiddesign.preview.app.preview

import bot.charlies.androiddesign.preview.domain.ProposalRepository
import bot.charlies.androiddesign.preview.feature.preview.viewer.ViewerViewModel
import bot.charlies.androiddesign.preview.mcp.PreviewMcpServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * One connection to the previewer: an agent talking MCP, or a file the owner opened. It owns its
 * own viewer and controller, so sessions never see each other's proposal, device or view.
 *
 * @param number Counts up from 1 in the order sessions open; the name falls back to it.
 * @param agent Whether an MCP client is attached. A local session has no [server].
 * @param onShown Called when a tool of this session brings the window up.
 */
class PreviewSession
internal constructor(
  val number: Int,
  agent: Boolean,
  repository: ProposalRepository,
  renderer: TreeRenderer,
  window: PreviewWindow,
  parentScope: CoroutineScope,
  version: String,
  onShown: (PreviewSession) -> Unit,
) {
  private val scope =
    CoroutineScope(parentScope.coroutineContext + SupervisorJob(parentScope.coroutineContext[Job]))

  val viewer = ViewerViewModel()
  val controller =
    AppPreviewController(
      repository = repository,
      viewer = viewer,
      window = window,
      renderer = renderer,
      scope = scope,
      version = version,
      onShown = { onShown(this) },
    )
  internal val server: PreviewMcpServer? =
    if (agent) PreviewMcpServer(controller, version) else null

  val isAgent: Boolean
    get() = server != null

  /** The proposal's name, else the file's name, else "Session N". */
  val name: StateFlow<String> =
    viewer.state
      .map { it.name ?: it.path?.fileName?.toString() ?: "Session $number" }
      .stateIn(scope, SharingStarted.Eagerly, "Session $number")

  /** Stops the file watcher and the name flow. */
  internal fun close() {
    scope.cancel()
  }
}
