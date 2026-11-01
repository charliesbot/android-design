package bot.charlies.androiddesign.preview.app.preview

import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessRenderer
import bot.charlies.androiddesign.preview.feature.preview.headless.RenderSource
import bot.charlies.androiddesign.preview.feature.preview.render.ProposalHostState
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.ViewSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Produces the layout tree of one screen at one view. */
interface TreeRenderer {
  /**
   * @param live A running host whose back stack and local state the render takes over, so the tree
   *   shows what that host shows; null renders the screen fresh.
   */
  suspend fun render(
    source: RenderSource,
    screen: String,
    view: ViewSpec,
    live: ProposalHostState? = null,
  ): LayoutTree
}

/**
 * Renders without a window on Compose's UI test engine, off the caller's thread. Renders are
 * serialized by [HeadlessRenderer] itself.
 */
class HeadlessTreeRenderer : TreeRenderer {
  override suspend fun render(
    source: RenderSource,
    screen: String,
    view: ViewSpec,
    live: ProposalHostState?,
  ): LayoutTree =
    withContext(Dispatchers.Default) {
      HeadlessRenderer.tree(source, screen, view, live)
    }
}
