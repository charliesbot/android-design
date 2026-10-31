package bot.charlies.androiddesign.preview.domain.validation

import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.ValidationError

/**
 * Checks a proposal against the catalog and the design system it resolves to.
 *
 * Paths are JSON-pointer-like: a proposal problem is `/screens/plates/components/3/variant`, a
 * problem in the proposal's `theme` is `/theme/...`, and one in DESIGN.md is `/designMd/...`.
 */
class ProposalValidator {
  fun validate(proposal: Proposal, designMd: ThemeOverride? = null): List<ValidationError> {
    val sink = ErrorSink()
    val design = DesignSystemResolver.resolve(designMd, proposal.theme)

    if (proposal.format != Proposal.FORMAT) {
      sink.add("/format", "format must be ${Proposal.FORMAT}, got ${proposal.format}")
    }
    val themes = ThemeValidator(sink, design)
    themes.validate(designMd, "/designMd", strict = false)
    themes.validate(proposal.theme, "/theme", strict = true)
    validateNavigation(sink, proposal)
    for ((screenId, screen) in proposal.screens) {
      ScreenValidator(sink, proposal, design, screenId, screen).validate()
    }
    return sink.errors
  }

  private fun validateNavigation(sink: ErrorSink, proposal: Proposal) {
    val navigation = proposal.navigation
    if (navigation.start !in proposal.screens) {
      sink.add("/navigation/start", "start screen '${navigation.start}' does not exist")
    }
    for (screenId in navigation.panes.keys) {
      if (screenId !in proposal.screens) {
        sink.add(
          "/navigation/panes".child(screenId),
          "'$screenId' is not a screen of this proposal",
        )
      }
    }
  }
}
