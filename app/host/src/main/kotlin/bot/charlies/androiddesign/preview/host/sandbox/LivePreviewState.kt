package bot.charlies.androiddesign.preview.host.sandbox

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import bot.charlies.androiddesign.preview.kit.DirectionSpec
import bot.charlies.androiddesign.preview.kit.ExplorationBoard
import bot.charlies.androiddesign.preview.model.DesignSystem

internal class LiveSession(
  val dexHash: String,
  val board: ExplorationBoard,
  val direction: DirectionSpec,
  val screenId: String,
  val overlayId: String?,
  val designSystem: DesignSystem,
  val dark: Boolean,
  val assetResolver: ((String) -> ImageBitmap?)?,
)

/** Hoisted live interactive session state shared between [RenderForegroundService] and UI. */
internal object LivePreviewState {
  val session: MutableState<LiveSession?> = mutableStateOf(null)
}
