package bot.charlies.androiddesign.preview.domain.validation

import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.model.Catalog
import bot.charlies.androiddesign.preview.model.Catalogs
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
    val catalog = Catalogs.of(proposal)
    val design = DesignSystemResolver.resolve(designMd, proposal.theme, catalog)

    if (proposal.format != Proposal.FORMAT) {
      sink.add("/format", "format must be ${Proposal.FORMAT}, got ${proposal.format}")
    }
    val themes = ThemeValidator(sink, design, catalog)
    themes.validate(designMd, "/designMd", strict = false)
    themes.validate(proposal.theme, "/theme", strict = true)
    validateNavigation(sink, proposal, catalog)
    for ((screenId, screen) in proposal.screens) {
      // A screen is checked against the catalog it names, so a stray screen of another catalog
      // reports its mismatch once and not a wrong-component error per component.
      val own = Catalogs.byId(screen.catalogId) ?: catalog
      ScreenValidator(sink, proposal, design, screenId, screen, own, catalog).validate()
    }
    return sink.errors
  }

  private fun validateNavigation(sink: ErrorSink, proposal: Proposal, catalog: Catalog) {
    val navigation = proposal.navigation
    if (!catalog.supportsPanes && navigation.panes.isNotEmpty()) {
      sink.add(
        "/navigation/panes",
        "${catalog.title} proposals have no panes: a watch shows one screen at a time; remove panes",
      )
    }
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
