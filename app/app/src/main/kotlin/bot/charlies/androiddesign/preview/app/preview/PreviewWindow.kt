package bot.charlies.androiddesign.preview.app.preview

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Whether the preview window is on screen. The window follows [visible]; closing it only hides it,
 * because the process has to keep serving the agent. It starts hidden, until `open_proposal` or
 * `show_preview` brings it up.
 */
class PreviewWindow(initiallyVisible: Boolean = false) {
  private val state = MutableStateFlow(initiallyVisible)
  val visible: StateFlow<Boolean> = state.asStateFlow()

  fun show() {
    state.value = true
  }

  fun hide() {
    state.value = false
  }
}
