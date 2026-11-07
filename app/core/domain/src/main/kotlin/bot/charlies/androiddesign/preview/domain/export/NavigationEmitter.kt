package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.navigation.DetailPlaceholders
import bot.charlies.androiddesign.preview.model.PaneRole
import bot.charlies.androiddesign.preview.model.Proposal

/**
 * The Navigation 3 host for a full export: one `@Serializable` route key per screen, and a
 * `NavDisplay` whose entries carry the pane roles as `ListDetailSceneStrategy` and
 * `SupportingPaneSceneStrategy` metadata.
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

  fun declarations(): List<Decl> = NavRoutes.declarations(file, proposal) + listOf(app())

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
      add(stmt(display))
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
