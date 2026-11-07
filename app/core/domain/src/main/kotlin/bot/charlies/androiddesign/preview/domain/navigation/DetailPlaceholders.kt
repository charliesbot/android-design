package bot.charlies.androiddesign.preview.domain.navigation

import bot.charlies.androiddesign.preview.model.Proposal

/**
 * The detail placeholders of a proposal's list-detail scene (`navigation.placeholders`). The
 * renderer, the exporter and the findings all read them through this, so they cannot disagree about
 * which screen fills the empty detail pane.
 */
object DetailPlaceholders {
  /** The screen drawn in the detail pane beside the list [listScreen], or null when none is set. */
  fun of(proposal: Proposal, listScreen: String): String? =
    proposal.navigation.placeholders[listScreen]

  /** Every screen that serves as a placeholder: drawn inside a scene, never a destination. */
  fun screens(proposal: Proposal): Set<String> = proposal.navigation.placeholders.values.toSet()
}
