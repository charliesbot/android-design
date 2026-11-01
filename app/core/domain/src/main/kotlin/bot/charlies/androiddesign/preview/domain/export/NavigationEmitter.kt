package bot.charlies.androiddesign.preview.domain.export

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

  fun declarations(): List<Decl> {
    file.use(Sym.Serializable)
    file.use(Sym.NavKey)
    return proposal.screens.keys.map {
      RawDecl(listOf("@Serializable private data object ${route(it)} : NavKey"))
    } + listOf(app())
  }

  private fun route(screen: String) = Naming.pascal(screen) + "Route"

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
          PaneRole.Detail -> "$pane.mainPane()"
          PaneRole.Supporting -> "$pane.supportingPane()"
          PaneRole.Extra -> "$pane.extraPane()"
        }
      } else {
        val pane = file.use(Sym.ListDetailSceneStrategy)
        when (role) {
          PaneRole.List -> "$pane.listPane()"
          PaneRole.Detail -> "$pane.detailPane()"
          PaneRole.Extra -> "$pane.extraPane()"
          PaneRole.Supporting -> error("handled by the supporting branch")
        }
      }
    return arg("metadata", call)
  }

  private fun app(): Decl {
    file.optIn(Sym.ExperimentalMaterial3AdaptiveApi)
    file.use(Sym.Composable)
    val strategies = strategies()
    val backStack = file.use(Sym.rememberNavBackStack)
    val start = route(proposal.navigation.start)
    val entries =
      proposal.screens.keys.map { screen ->
        val emitter = emitters.getValue(screen)
        val args = mutableListOf<Arg>()
        if (emitter.usesBack) args += arg("onBack", lambdaOf(Raw("backStack.removeLastOrNull()")))
        emitter.navigateTargets.forEach { target ->
          args +=
            arg(emitter.callbackName(target), lambdaOf(Raw("backStack.add(${route(target)})")))
        }
        stmt(
          Call(
            "entry<${route(screen)}>",
            listOfNotNull(metadata(screen)),
            lambda(stmt(Call(emitter.functionName, args))),
          ),
        )
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
