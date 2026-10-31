package bot.charlies.androiddesign.preview.app.preview

import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessRenderer
import bot.charlies.androiddesign.preview.feature.preview.render.ProposalHostState
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.ViewSpec
import java.nio.file.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Produces the layout tree of one screen at one view. */
interface TreeRenderer {
  /**
   * @param live A running host whose back stack and local state the render takes over, so the tree
   *   shows what that host shows; null renders the screen fresh.
   */
  suspend fun render(
    proposal: Proposal,
    design: DesignSystem,
    proposalDir: Path?,
    screen: String,
    view: ViewSpec,
    live: ProposalHostState? = null,
  ): LayoutTree
}

/**
 * Renders without a window on Compose's UI test engine. The engine owns global state, so renders
 * run one at a time, off the caller's thread.
 */
class HeadlessTreeRenderer : TreeRenderer {
  private val one = Mutex()

  override suspend fun render(
    proposal: Proposal,
    design: DesignSystem,
    proposalDir: Path?,
    screen: String,
    view: ViewSpec,
    live: ProposalHostState?,
  ): LayoutTree = one.withLock {
    withContext(Dispatchers.Default) {
      HeadlessRenderer.renderTree(proposal, design, proposalDir, screen, view, live)
    }
  }
}
