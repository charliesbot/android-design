package bot.charlies.androiddesign.preview.domain.validation

import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.domain.navigation.DetailPlaceholders
import bot.charlies.androiddesign.preview.domain.navigation.NavigationGraph
import bot.charlies.androiddesign.preview.model.Catalog
import bot.charlies.androiddesign.preview.model.Catalogs
import bot.charlies.androiddesign.preview.model.Navigation
import bot.charlies.androiddesign.preview.model.PaneRole
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
    validatePlaceholders(sink, proposal, catalog)
    validateDestinations(sink, proposal, catalog)
  }

  /**
   * `navigation.destinations`: the screens the bar and the rail open. Material gives a navigation
   * bar "three to five" destinations, and a destination is a top-level screen, so one that is a
   * detail, supporting or extra pane, or a placeholder, is rejected.
   */
  private fun validateDestinations(sink: ErrorSink, proposal: Proposal, catalog: Catalog) {
    val navigation = proposal.navigation
    if (!catalog.supportsPanes) {
      for ((key, present) in
        listOf(
          "destinations" to (navigation.destinations != null),
          "railExpanded" to (navigation.railExpanded != null),
        )) {
        if (present) {
          sink.add(
            "/navigation".child(key),
            "${catalog.title} proposals have no navigation bar or rail: a watch shows one screen " +
              "at a time; remove $key",
          )
        }
      }
      return
    }
    val destinations = navigation.destinations ?: return
    val root = "/navigation/destinations"
    if (destinations.size !in Navigation.MIN_DESTINATIONS..Navigation.MAX_DESTINATIONS) {
      sink.add(
        root,
        "needs ${Navigation.MIN_DESTINATIONS} to ${Navigation.MAX_DESTINATIONS} destinations, " +
          "got ${destinations.size}; use tabs for fewer, and move the rest into the screens",
      )
    }
    val placeholders = DetailPlaceholders.screens(proposal)
    val seen = mutableMapOf<String, Int>()
    destinations.forEachIndexed { index, destination ->
      val path = root.child(index)
      val screen = destination.screen
      val screenPath = path.child("screen")
      val role = navigation.panes[screen]
      val first = seen.putIfAbsent(screen, index)
      when {
        screen !in proposal.screens ->
          sink.add(screenPath, "'$screen' is not a screen of this proposal")
        first != null -> sink.add(screenPath, "'$screen' is already destination $first")
        screen in placeholders ->
          sink.add(
            screenPath,
            "'$screen' is a detail placeholder, which a scene draws and nothing opens; " +
              "a destination is a screen of its own",
          )
        role != null && role != PaneRole.List ->
          sink.add(
            screenPath,
            "'$screen' has the pane role ${role.name.lowercase()}; a destination is a top-level " +
              "screen, with the pane role list or none",
          )
      }
      if (destination.label.isBlank()) sink.add(path.child("label"), "must not be empty")
      for ((key, icon) in
        listOf("icon" to destination.icon, "selectedIcon" to destination.selectedIcon)) {
        if (icon != null && !ScreenValidator.ICON_NAME.matches(icon)) {
          sink.add(
            path.child(key),
            "'$icon' is not a Material Symbols name (lowercase letters, digits and underscores)",
          )
        }
      }
      destination.badge?.let { badge ->
        BadgeRules.problem(badge, bindingNote = "; a destination takes no binding")?.let {
          sink.add(path.child("badge"), it)
        }
      }
    }
  }

  private fun validatePlaceholders(sink: ErrorSink, proposal: Proposal, catalog: Catalog) {
    val navigation = proposal.navigation
    if (navigation.placeholders.isEmpty()) return
    if (!catalog.supportsPanes) {
      sink.add(
        "/navigation/placeholders",
        "${catalog.title} proposals have no panes, so no detail placeholders; remove placeholders",
      )
      return
    }
    if (PaneRole.Supporting in navigation.panes.values) {
      sink.add(
        "/navigation/placeholders",
        "placeholders fill the detail pane of a list-detail layout; a supporting pane layout has none",
      )
      return
    }
    val destinations = NavigationGraph(proposal).edges.groupBy { it.to }
    for ((list, placeholder) in navigation.placeholders) {
      val path = "/navigation/placeholders".child(list)
      if (list !in proposal.screens) {
        sink.add(path, "'$list' is not a screen of this proposal")
      } else if (navigation.panes[list] != PaneRole.List) {
        sink.add(path, "'$list' needs the pane role list in navigation.panes to have a placeholder")
      }
      when {
        placeholder !in proposal.screens ->
          sink.add(path, "placeholder '$placeholder' is not a screen of this proposal")
        placeholder in navigation.panes ->
          sink.add(
            path,
            "placeholder '$placeholder' is in navigation.panes; remove it from the panes",
          )
        placeholder == navigation.start ->
          sink.add(path, "placeholder '$placeholder' is the start screen; start on a real screen")
        else ->
          destinations[placeholder]?.firstOrNull()?.let { edge ->
            sink.add(
              path,
              "screen '${edge.from}' navigates to placeholder '$placeholder' " +
                "(component '${edge.componentId}'); a placeholder is not a destination",
            )
          }
      }
    }
  }
}
