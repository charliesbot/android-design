package bot.charlies.androiddesign.preview.domain.export.wear

import bot.charlies.androiddesign.preview.domain.export.Assign
import bot.charlies.androiddesign.preview.domain.export.Call
import bot.charlies.androiddesign.preview.domain.export.Decl
import bot.charlies.androiddesign.preview.domain.export.FileContext
import bot.charlies.androiddesign.preview.domain.export.FunDecl
import bot.charlies.androiddesign.preview.domain.export.NavRoutes
import bot.charlies.androiddesign.preview.domain.export.Raw
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
  fun declarations(): List<Decl> = NavRoutes.declarations(file, proposal) + listOf(app())

  private fun app(): Decl {
    file.use(Sym.Composable)
    val key = file.use(Sym.NavKey)
    val entries =
      proposal.screens.keys.map { screen ->
        NavRoutes.entry(screen, emitters.getValue(screen), metadata = null)
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
    val start = NavRoutes.name(proposal.navigation.start)
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
