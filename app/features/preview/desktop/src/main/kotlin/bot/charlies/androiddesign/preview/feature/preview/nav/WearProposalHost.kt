package bot.charlies.androiddesign.preview.feature.preview.nav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import androidx.wear.compose.WearDevice
import androidx.wear.compose.navigation3.rememberSwipeDismissableSceneStrategy
import bot.charlies.androiddesign.preview.feature.preview.record.PreviewFrame
import bot.charlies.androiddesign.preview.feature.preview.record.TreeRecorder
import bot.charlies.androiddesign.preview.feature.preview.render.ProposalHostState
import bot.charlies.androiddesign.preview.feature.preview.render.ProposalRenderer
import bot.charlies.androiddesign.preview.feature.preview.theme.wear.ProvideWearTheme
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.SizeClass
import bot.charlies.androiddesign.preview.model.ViewSpec
import kotlin.math.roundToInt

/**
 * The whole Wear preview: the Wear theme, the preview frame, and a [WearDevice] sized to [frame]
 * (the moving size while a device switch animates) that runs the proposal. The Wear scene is the
 * device: a Wear dialog is a popup over the whole Compose scene, which the window hosts in a panel
 * of the frame's size and the headless renderer sizes to the view.
 */
@Composable
internal fun WearProposalPreview(
  host: ProposalHostState,
  design: DesignSystem,
  view: ViewSpec,
  frame: DpSize,
  modifier: Modifier = Modifier,
  recorder: TreeRecorder? = null,
) {
  val width = frame.width.value.roundToInt()
  ProvideWearTheme(design) {
    PreviewFrame(recorder, modifier.size(frame.width, frame.height)) {
      WearDevice(width, frame.height.value.roundToInt(), view.round) {
        WearProposalHost(host, widthDp = width, recorder = recorder)
      }
    }
  }
}

/**
 * Runs a Wear proposal as a Navigation 3 back stack of screen ids with Wear's swipe-to-dismiss
 * scene, so going back is a swipe from the left edge. A Wear proposal has no panes.
 */
@Composable
private fun WearProposalHost(
  host: ProposalHostState,
  widthDp: Int,
  recorder: TreeRecorder?,
  primaryScreen: String = host.current,
) {
  // NavDisplay keeps each entry's content across recompositions, so the entries read these
  // through state: a captured value would stay at what the screen was first drawn with.
  val currentRecorder by rememberUpdatedState(recorder)
  val currentPrimary by rememberUpdatedState(primaryScreen)
  val currentWidth by rememberUpdatedState(widthDp)
  NavDisplay(
    backStack = host.backStack,
    modifier = Modifier.fillMaxSize(),
    sceneStrategies = listOf(rememberSwipeDismissableSceneStrategy<String>()),
    onBack = { host.back() },
    entryProvider = { screenId ->
      NavEntry(screenId) {
        ProposalRenderer(
          host = host,
          screenId = screenId,
          sizeClass = SizeClass.Compact,
          recorder = currentRecorder,
          idPrefix = if (screenId == currentPrimary) "" else "$screenId/",
          widthDp = currentWidth,
        )
      }
    },
  )
}
