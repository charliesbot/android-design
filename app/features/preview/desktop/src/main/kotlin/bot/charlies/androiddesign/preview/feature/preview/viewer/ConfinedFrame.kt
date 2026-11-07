package bot.charlies.androiddesign.preview.feature.preview.viewer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowExceptionHandler
import bot.charlies.androiddesign.preview.feature.preview.nav.ProposalPreview
import bot.charlies.androiddesign.preview.feature.preview.render.ProposalHostState
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.ViewSpec

/**
 * The colours around a round screen. A heavyweight panel cannot be clipped to a circle, so a round
 * panel paints its own corners [stage] and draws the bezel disc itself, up to the same radius the
 * stage's bezel uses, so the two halves of the bezel line up.
 */
private data class RoundBackdrop(val stage: Color, val bezel: Color, val outline: Color)

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
  // The stage's theme is the proposal's, so its background is the screen's. The panel paints it
  // wherever the screen has not drawn yet, which shows while a device switch resizes the panel.
  // A round view shows the stage's own colour there instead, since the panel's corners lie outside
  // the bezel.
  val colors = MaterialTheme.colorScheme
  val backdrop =
    if (view.round)
      RoundBackdrop(colors.surface, colors.surfaceContainerHighest, colors.outlineVariant)
    else null
  val background = java.awt.Color((backdrop?.stage ?: colors.background).toArgb(), true)
  var params by remember { mutableStateOf(FrameParams(host, design, view, frame)) }
  var frameScale by remember { mutableStateOf(scale) }
  var frameBackdrop by remember { mutableStateOf(backdrop) }
  params = FrameParams(host, design, view, frame)
  frameScale = scale
  frameBackdrop = backdrop
  val failed by rememberUpdatedState(onFailure)
  SwingPanel(
    factory = {
      newFramePanel(
        paramsOf = { params },
        scaleOf = { frameScale },
        backdropOf = { frameBackdrop },
        onFailure = { failed(it) },
        content = content,
      )
    },
    modifier = modifier.size(frame.width * scale, frame.height * scale),
    update = { it.background = background },
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
  backdropOf: () -> RoundBackdrop?,
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
        val backdrop = backdropOf()
        if (backdrop == null) {
          content(p.host, p.design, p.view, p.frame)
        } else {
          RoundFrame(backdrop, scaleOf(), p.frame) { content(p.host, p.design, p.view, p.frame) }
        }
      }
    }
  }

/**
 * A round screen on its bezel inside the panel. Density here is scaled by [scale], so one window dp
 * is `1.dp / scale` and the bezel's ring is `FrameRing` dp wide, as the stage's bezel is.
 */
@Composable
private fun RoundFrame(
  backdrop: RoundBackdrop,
  scale: Float,
  frame: DpSize,
  content: @Composable () -> Unit,
) {
  Box(Modifier.fillMaxSize()) {
    Canvas(Modifier.fillMaxSize()) {
      val outline = 1.dp.toPx() / scale
      val radius = frame.width.toPx() / 2 + FrameRing.toPx()
      drawRect(backdrop.stage)
      drawCircle(backdrop.bezel, radius)
      drawCircle(backdrop.outline, radius - outline / 2, style = Stroke(outline))
    }
    Box(Modifier.fillMaxSize().clip(CircleShape)) { content() }
  }
}
