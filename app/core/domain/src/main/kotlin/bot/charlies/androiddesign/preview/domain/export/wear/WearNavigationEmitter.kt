package bot.charlies.androiddesign.preview.domain.export.wear

import bot.charlies.androiddesign.preview.domain.export.Assign
import bot.charlies.androiddesign.preview.domain.export.Call
import bot.charlies.androiddesign.preview.domain.export.Decl
import bot.charlies.androiddesign.preview.domain.export.FileContext
import bot.charlies.androiddesign.preview.domain.export.FunDecl
import bot.charlies.androiddesign.preview.domain.export.Naming
import bot.charlies.androiddesign.preview.domain.export.Raw
import bot.charlies.androiddesign.preview.domain.export.RawDecl
import bot.charlies.androiddesign.preview.domain.export.Sym
import bot.charlies.androiddesign.preview.domain.export.arg
import bot.charlies.androiddesign.preview.domain.export.lambda
import bot.charlies.androiddesign.preview.domain.export.lambdaOf
import bot.charlies.androiddesign.preview.domain.export.pos
import bot.charlies.androiddesign.preview.domain.export.stmt
import bot.charlies.androiddesign.preview.model.Proposal

/**
 * The Navigation 3 host of a full Wear export: one `@Serializable` route key per screen and a
 * `NavDisplay` with Wear's swipe-to-dismiss scene, so going back is a swipe from the left edge.
 * `AppScaffold` sits once at the root, inside the theme, with the clock; each screen brings its own
 * `ScreenScaffold`. A Wear proposal has no panes.
 */
internal class WearNavigationEmitter(
  private val file: FileContext,
  private val proposal: Proposal,
  private val emitters: Map<String, WearScreenEmitter>,
  private val appName: String,
) {
  fun declarations(): List<Decl> {
    file.use(Sym.Serializable)
    file.use(Sym.NavKey)
    return proposal.screens.keys.map {
      RawDecl(listOf("@Serializable private data object ${route(it)} : NavKey"))
    } + listOf(app())
  }

  private fun route(screen: String) = Naming.pascal(screen) + "Route"

  private fun app(): Decl {
    file.use(Sym.Composable)
    val key = file.use(Sym.NavKey)
    val entries =
      proposal.screens.keys.map { screen ->
        val emitter = emitters.getValue(screen)
        val args = mutableListOf<bot.charlies.androiddesign.preview.domain.export.Arg>()
        if (emitter.usesBack) args += arg("onBack", lambdaOf(Raw("backStack.removeLastOrNull()")))
        emitter.navigateTargets.forEach { target ->
          args +=
            arg(emitter.callbackName(target), lambdaOf(Raw("backStack.add(${route(target)})")))
        }
        stmt(
          Call(
            "entry<${route(screen)}>",
            emptyList(),
            lambda(stmt(Call(emitter.functionName, args))),
          ),
        )
      }
    val display =
      Call(
        file.use(Sym.NavDisplay),
        listOf(
          arg("backStack", "backStack"),
          arg("onBack", lambdaOf(Raw("backStack.removeLastOrNull()"))),
          arg(
            "sceneStrategies",
            "listOf(${file.use(WearSym.rememberSwipeDismissableSceneStrategy)}<$key>())",
          ),
          arg("entryProvider", Call(file.use(Sym.entryProvider), lambda = lambda(entries))),
        ),
      )
    val start = route(proposal.navigation.start)
    val scaffold =
      Call(
        file.use(WearSym.AppScaffold),
        listOf(arg("timeText", lambdaOf(Call(file.use(WearSym.TimeText))))),
        lambda(
          Assign(
            "val backStack",
            "=",
            Call(file.use(Sym.rememberNavBackStack), listOf(pos(start))),
          ),
          stmt(display),
        ),
      )
    return FunDecl(
      annotations = listOf("@Composable"),
      modifiers = "",
      name = appName,
      params = emptyList(),
      body = listOf(stmt(Call(WEAR_THEME, lambda = lambda(stmt(scaffold))))),
    )
  }
}
