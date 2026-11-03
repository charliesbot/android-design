package bot.charlies.androiddesign.preview.feature.preview.viewer

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.WindowExceptionHandler
import bot.charlies.androiddesign.preview.feature.preview.nav.ProposalPreview
import bot.charlies.androiddesign.preview.feature.preview.render.ProposalHostState
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.ViewSpec

/** What the frame draws; read by the frame's own composition, so changes redraw it in place. */
private data class FrameParams(
  val host: ProposalHostState,
  val design: DesignSystem,
  val view: ViewSpec,
  val frame: DpSize,
)

/**
 * The preview frame in a Compose scene of its own.
 *
 * Sheets, dialogs and menus of a proposal are popups, and a popup covers the whole scene it is
 * opened in. In the viewer's scene that would be the whole window, scrim over the side panel
 * included. Hosting the frame in a [ComposePanel] makes the panel's bounds the scene, so every
 * overlay stays inside the frame.
 *
 * The panel is [scale] times the frame's size in window units, and its density is scaled by the
 * same factor, so the proposal still lays out at the dp size of [frame] and everything, overlays
 * included, shrinks with it. The panel resizes with the frame while a device switch animates, so
 * the screen reflows live instead of being replaced.
 */
@Composable
fun ConfinedFrame(
  host: ProposalHostState,
  design: DesignSystem,
  view: ViewSpec,
  frame: DpSize,
  scale: Float,
  modifier: Modifier = Modifier,
  onFailure: (Throwable) -> Unit = {},
  content: @Composable (ProposalHostState, DesignSystem, ViewSpec, DpSize) -> Unit = { h, d, v, f ->
    ProposalPreview(h, d, v, f)
  },
) {
  var params by remember { mutableStateOf(FrameParams(host, design, view, frame)) }
  var frameScale by remember { mutableStateOf(scale) }
  params = FrameParams(host, design, view, frame)
  frameScale = scale
  val failed by rememberUpdatedState(onFailure)
  SwingPanel(
    factory = {
      newFramePanel(
        paramsOf = { params },
        scaleOf = { frameScale },
        onFailure = { failed(it) },
        content = content,
      )
    },
    modifier = modifier.size(frame.width * scale, frame.height * scale),
  )
}

/**
 * A panel that draws the frame. [paramsOf] and [scaleOf] read snapshot state, so the panel's
 * composition follows them.
 */
@OptIn(ExperimentalComposeUiApi::class)
private fun newFramePanel(
  paramsOf: () -> FrameParams,
  scaleOf: () -> Float,
  onFailure: (Throwable) -> Unit,
  content: @Composable (ProposalHostState, DesignSystem, ViewSpec, DpSize) -> Unit,
): ComposePanel =
  ComposePanel().apply {
    // Without a handler, a throw while composing ends the panel's composition on the EDT, and the
    // window stays frozen on its last picture for the rest of the session.
    exceptionHandler = WindowExceptionHandler { onFailure(it) }
    setContent {
      val p = paramsOf()
      val base = LocalDensity.current
      CompositionLocalProvider(
        LocalDensity provides Density(base.density * scaleOf(), base.fontScale),
      ) {
        content(p.host, p.design, p.view, p.frame)
      }
    }
  }
