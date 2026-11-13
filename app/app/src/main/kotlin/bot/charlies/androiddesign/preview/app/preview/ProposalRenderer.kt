package bot.charlies.androiddesign.preview.app.preview

import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessRenderer
import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessResult
import bot.charlies.androiddesign.preview.feature.preview.headless.RenderSource
import bot.charlies.androiddesign.preview.feature.preview.render.ProposalHostState
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.ViewSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Draws one screen at one view, as a layout tree or as a picture. */
interface ProposalRenderer {
  /**
   * The layout tree.
   *
   * @param live A running host whose back stack and local state the render takes over, so the tree
   *   shows what that host shows; null renders the screen fresh.
   * @param overlay The id of an overlay of [screen] to draw open.
   */
  suspend fun render(
    source: RenderSource,
    screen: String,
    view: ViewSpec,
    live: ProposalHostState? = null,
    overlay: String? = null,
  ): LayoutTree

  /** The tree and a PNG at [density] pixels per dp. See [render] for the parameters. */
  suspend fun image(
    source: RenderSource,
    screen: String,
    view: ViewSpec,
    live: ProposalHostState? = null,
    overlay: String? = null,
    density: Float = HeadlessRenderer.DENSITY,
  ): HeadlessResult
}

/**
 * Renders without a window on Compose's UI test engine, off the caller's thread. Renders are
 * serialized by [HeadlessRenderer] itself.
 */
class HeadlessProposalRenderer : ProposalRenderer {
  override suspend fun render(
    source: RenderSource,
    screen: String,
    view: ViewSpec,
    live: ProposalHostState?,
    overlay: String?,
  ): LayoutTree =
    withContext(Dispatchers.Default) {
      HeadlessRenderer.tree(source, screen, view, live, overlay)
    }

  override suspend fun image(
    source: RenderSource,
    screen: String,
    view: ViewSpec,
    live: ProposalHostState?,
    overlay: String?,
    density: Float,
  ): HeadlessResult =
    withContext(Dispatchers.Default) {
      HeadlessRenderer.image(
        source,
        screen,
        view,
        replay = live,
        overlay = overlay,
        density = density,
      )
    }
}
