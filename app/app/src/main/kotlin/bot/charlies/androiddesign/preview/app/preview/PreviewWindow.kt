package bot.charlies.androiddesign.preview.app.preview

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Whether the preview window is on screen. The window follows [visible]; closing it only hides it,
 * because the process has to keep serving the agent.
 */
interface PreviewWindow {
  val visible: StateFlow<Boolean>

  fun show()

  fun hide()
}

/** The window starts hidden, until `open_proposal` or `show_preview` brings it up. */
class WindowVisibility(initiallyVisible: Boolean = false) : PreviewWindow {
  private val state = MutableStateFlow(initiallyVisible)
  override val visible: StateFlow<Boolean> = state.asStateFlow()

  override fun show() {
    state.value = true
  }

  override fun hide() {
    state.value = false
  }
}
