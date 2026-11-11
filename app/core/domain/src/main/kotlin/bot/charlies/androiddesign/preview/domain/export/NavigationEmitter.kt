package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.BadgeContent
import bot.charlies.androiddesign.preview.domain.navigation.AppNavigation
import bot.charlies.androiddesign.preview.domain.navigation.DetailPlaceholders
import bot.charlies.androiddesign.preview.model.Destination
import bot.charlies.androiddesign.preview.model.PaneRole
import bot.charlies.androiddesign.preview.model.Proposal
import kotlinx.serialization.json.JsonPrimitive

/**
 * The Navigation 3 host for a full export: one `@Serializable` route key per screen, and a
 * `NavDisplay` whose entries carry the pane roles as `ListDetailSceneStrategy` and
 * `SupportingPaneSceneStrategy` metadata.
 *
 * When the proposal declares `navigation.destinations`, the `NavDisplay` sits inside one
 * `NavigationSuiteScaffold`, so the bar or rail is drawn once around the panes and never by a
 * screen. The code it writes makes the decisions of [AppNavigation]: which destination is selected,
 * whether the navigation shows, and which component draws.
 */
internal class NavigationEmitter(
  private val file: FileContext,
  private val proposal: Proposal,
  private val emitters: Map<String, ScreenEmitter>,
  private val themeName: String,
  private val appName: String,
) {
  private val roles: Map<String, PaneRole> = proposal.navigation.panes
  /** A proposal with a supporting pane uses that strategy for every pane, else list and detail. */
  private val supporting = roles.values.any { it == PaneRole.Supporting }
  private val listDetail = !supporting && roles.isNotEmpty()

  private val destinations: List<Destination> = AppNavigation.destinations(proposal)

  fun declarations(): List<Decl> =
    NavRoutes.declarations(file, proposal) + listOfNotNull(suiteTypeFunction()) + listOf(app())

  private fun strategies(): List<Pair<String, String>> {
    val key = file.use(Sym.NavKey)
    return when {
      supporting ->
        listOf(
          "supportingPaneStrategy" to
            "${file.use(Sym.rememberSupportingPaneSceneStrategy)}<$key>()",
        )
      listDetail ->
        listOf("listDetailStrategy" to "${file.use(Sym.rememberListDetailSceneStrategy)}<$key>()")
      else -> emptyList()
    }
  }

  private fun metadata(screen: String): Arg? {
    val role = roles[screen] ?: return null
    val call =
      if (supporting) {
        val pane = file.use(Sym.SupportingPaneSceneStrategy)
        when (role) {
          PaneRole.List,
          PaneRole.Detail -> Call("$pane.mainPane")
          PaneRole.Supporting -> Call("$pane.supportingPane")
          PaneRole.Extra -> Call("$pane.extraPane")
        }
      } else {
        val pane = file.use(Sym.ListDetailSceneStrategy)
        when (role) {
          PaneRole.List -> listPane(pane, DetailPlaceholders.of(proposal, screen))
          PaneRole.Detail -> Call("$pane.detailPane")
          PaneRole.Extra -> Call("$pane.extraPane")
          PaneRole.Supporting -> error("handled by the supporting branch")
        }
      }
    return arg("metadata", call)
  }

  /** `listPane()`, or `listPane(detailPlaceholder = { PickScreen() })` with a placeholder. */
  private fun listPane(pane: String, placeholder: String?): Call =
    Call(
      "$pane.listPane",
      listOfNotNull(
        placeholder?.let {
          arg("detailPlaceholder", lambdaOf(NavRoutes.screenCall(emitters.getValue(it))))
        },
      ),
    )

  /**
   * The navigation suite around [display]: the routes of the destinations, the selected one,
   * whether the navigation shows, and `NavigationSuiteScaffold` with one item per destination.
   */
  private fun suiteScaffold(display: Call): List<Stmt> {
    val key = file.use(Sym.NavKey)
    val adaptiveInfo = "${file.use(Sym.currentWindowAdaptiveInfoV2)}()"
    val routes = destinations.map { NavRoutes.name(it.screen) }
    fun routeList(names: List<String>) = Call("listOf<$key>", names.map { pos(it) })
    val statements = mutableListOf<Stmt>()
    statements += Assign("val destinations", "=", routeList(routes))
    statements += Assign("val topRoute", "=", Raw("backStack.lastOrNull()"))
    statements +=
      Assign("val selectedRoute", "=", Raw("backStack.lastOrNull { it in destinations }"))
    if (roles.isEmpty()) {
      statements += Assign("val showNavigation", "=", Raw("topRoute in destinations"))
    } else {
      // A pane beside a destination: the scaffold has room for two panes, and the selected
      // destination is a pane too.
      val directive = file.use(Sym.calculatePaneScaffoldDirective)
      statements += Assign("val paneRoutes", "=", routeList(roles.keys.map(NavRoutes::name)))
      statements +=
        Assign(
          "val twoPanes",
          "=",
          Raw("$directive($adaptiveInfo).maxHorizontalPartitions >= 2"),
        )
      statements +=
        Assign(
          "val showNavigation",
          "=",
          Or(
            listOf(
              "topRoute in destinations",
              "(topRoute in paneRoutes && twoPanes && selectedRoute in paneRoutes)",
            ),
          ),
        )
    }
    statements +=
      Assign(
        "val navigationSuiteType",
        "=",
        Raw("$SUITE_TYPE_FUNCTION(showNavigation, $adaptiveInfo.windowSizeClass)"),
      )
    val items = destinations.map { stmt(suiteItem(it)) }
    val scaffold =
      Call(
        file.use(Sym.NavigationSuiteScaffold),
        listOf(
          arg("navigationItems", lambda(items)),
          arg("navigationSuiteType", "navigationSuiteType"),
        ),
        lambdaOf(display),
      )
    return statements + stmt(scaffold)
  }

  /** `NavigationSuiteItem(...)` for [destination]: a tap makes it the whole back stack. */
  private fun suiteItem(destination: Destination): Call {
    val route = NavRoutes.name(destination.screen)
    val icon = painter(destination.icon, destination.selectedIcon, "selectedRoute == $route")
    val badge =
      BadgeContent.of(destination.badge as? JsonPrimitive)?.let { content ->
        val badgeName = file.use(Sym.m3("Badge"))
        when (content) {
          BadgeContent.Small -> lambdaOf(Call(badgeName))
          is BadgeContent.Large ->
            lambdaOf(
              Call(
                badgeName,
                emptyList(),
                lambdaOf(Call(file.use(Sym.Text), listOf(pos(kotlinString(content.label))))),
              ),
            )
        }
      }
    return Call(
      file.use(Sym.NavigationSuiteItem),
      listOfNotNull(
        arg("selected", "selectedRoute == $route"),
        arg(
          "onClick",
          lambda(stmt(Raw("backStack.clear()")), stmt(Raw("backStack.add($route)"))),
        ),
        arg(
          "icon",
          lambdaOf(
            Call(
              file.use(Sym.Icon),
              listOf(pos(icon), arg("contentDescription", "null")),
            ),
          ),
        ),
        arg(
          "label",
          lambdaOf(Call(file.use(Sym.Text), listOf(pos(kotlinString(destination.label))))),
        ),
        arg("navigationSuiteType", "navigationSuiteType"),
        badge?.let { arg("badge", it) },
      ),
    )
  }

  /** The icon's painter: its own drawable, or the selected one while the item is selected. */
  private fun painter(icon: String, selectedIcon: String?, selected: String): String {
    val painterResource = file.use(Sym.painterResource)
    val normal = file.drawable(Naming.snake(icon))
    if (selectedIcon == null || selectedIcon == icon) return "$painterResource($normal)"
    val chosen = file.drawable(Naming.snake(selectedIcon))
    return "$painterResource(if ($selected) $chosen else $normal)"
  }

  /**
   * `navigationSuiteTypeFor`, the function that picks what the suite draws, as the previewer does:
   * nothing while a screen covers the window, a short bar below 600 dp, else a wide rail, expanded
   * from 840 dp when the proposal asks for `railExpanded`. Null without destinations.
   */
  private fun suiteTypeFunction(): Decl? {
    if (destinations.isEmpty()) return null
    val type = file.use(Sym.NavigationSuiteType)
    val windowSizeClass = file.use(Sym.WindowSizeClass)
    val expanded = proposal.navigation.railExpanded == true
    return RawDecl(
      buildList {
        add("private fun $SUITE_TYPE_FUNCTION(")
        add("  showNavigation: Boolean,")
        add("  windowSizeClass: $windowSizeClass,")
        add("): $type =")
        add("  when {")
        add("    !showNavigation -> $type.None")
        add(
          "    !windowSizeClass.isWidthAtLeastBreakpoint($windowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) ->",
        )
        add("      $type.ShortNavigationBarCompact")
        if (expanded) {
          add(
            "    windowSizeClass.isWidthAtLeastBreakpoint($windowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ->",
          )
          add("      $type.WideNavigationRailExpanded")
        }
        add("    else -> $type.WideNavigationRailCollapsed")
        add("  }")
      },
    )
  }

  private fun app(): Decl {
    file.optIn(Sym.ExperimentalMaterial3AdaptiveApi)
    file.use(Sym.Composable)
    val strategies = strategies()
    val backStack = file.use(Sym.rememberNavBackStack)
    val start = NavRoutes.name(proposal.navigation.start)
    val entries =
      NavRoutes.routed(proposal).map { screen ->
        NavRoutes.entry(screen, emitters.getValue(screen), metadata(screen))
      }
    val display =
      Call(
        file.use(Sym.NavDisplay),
        listOfNotNull(
          arg("backStack", "backStack"),
          arg("onBack", lambdaOf(Raw("backStack.removeLastOrNull()"))),
          if (strategies.isEmpty()) null
          else arg("sceneStrategies", "listOf(${strategies.joinToString(", ") { it.first }})"),
          arg("entryProvider", Call(file.use(Sym.entryProvider), lambda = lambda(entries))),
        ),
      )
    val body = buildList {
      add(Assign("val backStack", "=", Call(backStack, listOf(pos(start)))))
      strategies.forEach { (name, expr) -> add(Assign("val $name", "=", Raw(expr))) }
      if (destinations.isEmpty()) add(stmt(display)) else addAll(suiteScaffold(display))
    }
    return FunDecl(
      annotations = listOf("@Composable"),
      modifiers = "",
      name = appName,
      params = emptyList(),
      body = listOf(stmt(Call(themeName, lambda = lambda(body)))),
    )
  }
}

private const val SUITE_TYPE_FUNCTION = "navigationSuiteTypeFor"

/** What the phone and Wear navigation hosts share: the route keys and the `entry` calls. */
internal object NavRoutes {
  fun name(screen: String) = Naming.pascal(screen) + "Route"

  /** The screens that are destinations: all but the detail placeholders, which have no route. */
  fun routed(proposal: Proposal): List<String> =
    (proposal.screens.keys - DetailPlaceholders.screens(proposal)).toList()

  /** One `@Serializable` route key per destination screen. */
  fun declarations(file: FileContext, proposal: Proposal): List<Decl> {
    file.use(Sym.Serializable)
    file.use(Sym.NavKey)
    return routed(proposal).map {
      RawDecl(listOf("@Serializable private data object ${name(it)} : NavKey"))
    }
  }

  /** `Screen(onBack = ..., onNavigateToX = ...)`, wired to the back stack. */
  fun screenCall(emitter: ScreenEmitterBase): Call {
    val args = mutableListOf<Arg>()
    if (emitter.usesBack) args += arg("onBack", lambdaOf(Raw("backStack.removeLastOrNull()")))
    emitter.navigateTargets.forEach { target ->
      args += arg(emitter.callbackName(target), lambdaOf(Raw("backStack.add(${name(target)})")))
    }
    return Call(emitter.functionName, args)
  }

  /** `entry<Route>(metadata) { Screen(onBack = ..., onNavigateToX = ...) }`. */
  fun entry(screen: String, emitter: ScreenEmitterBase, metadata: Arg?): Stmt =
    stmt(Call("entry<${name(screen)}>", listOfNotNull(metadata), lambda(stmt(screenCall(emitter)))))
}
