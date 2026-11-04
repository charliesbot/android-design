package bot.charlies.androiddesign.preview.app.preview

import bot.charlies.androiddesign.preview.domain.ProposalRepository
import io.modelcontextprotocol.kotlin.sdk.shared.Transport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Every open [PreviewSession] and which one the window shows. The repository, renderer, window and
 * scope are shared; each session builds its own viewer and controller on top of them.
 *
 * @param launchedByUser True when the owner started the process (Finder, `open`) and not an agent,
 *   so the process stays up with no session at all.
 * @param onIdle Called when the last session closes and [staysAlive] is false.
 */
class Sessions(
  private val repository: ProposalRepository,
  private val renderer: TreeRenderer,
  private val window: PreviewWindow,
  private val scope: CoroutineScope,
  private val version: String,
  private val launchedByUser: Boolean,
  private val onIdle: () -> Unit,
) {
  private val list = MutableStateFlow<List<PreviewSession>>(emptyList())
  val sessions: StateFlow<List<PreviewSession>> = list.asStateFlow()

  private val current = MutableStateFlow<PreviewSession?>(null)
  val selected: StateFlow<PreviewSession?> = current.asStateFlow()

  private var opened = 0

  /**
   * Whether the process keeps running: always when the owner launched it, else while a session is
   * open.
   */
  fun staysAlive(): Boolean = launchedByUser || list.value.isNotEmpty()

  /** Whether an agent is connected, which is what makes closing the window hide it, not quit. */
  fun hasAgent(): Boolean = list.value.any { it.isAgent }

  fun select(session: PreviewSession) {
    if (session in list.value) current.value = session
  }

  /** Serves a new agent session on [transport]. It closes when the transport does. */
  suspend fun openAgent(transport: Transport): PreviewSession {
    val session = add(agent = true)
    transport.onClose { close(session) }
    checkNotNull(session.server).connect(transport)
    return session
  }

  /** Opens the proposal file at [path] in a new session with no agent. */
  suspend fun openLocal(path: String): PreviewSession {
    val session = add(agent = false)
    select(session)
    try {
      session.controller.openProposal(path)
    } catch (e: Exception) {
      close(session)
      throw e
    }
    return session
  }

  fun close(session: PreviewSession) {
    if (list.value.none { it === session }) return
    list.update { sessions -> sessions.filterNot { it === session } }
    if (current.value === session) current.value = list.value.lastOrNull()
    session.close()
    if (!staysAlive()) onIdle()
  }

  private fun add(agent: Boolean): PreviewSession {
    val session =
      synchronized(this) {
        PreviewSession(
          number = ++opened,
          agent = agent,
          repository = repository,
          renderer = renderer,
          window = window,
          parentScope = scope,
          version = version,
          onShown = ::select,
        )
      }
    list.update { it + session }
    current.compareAndSet(null, session)
    return session
  }
}
