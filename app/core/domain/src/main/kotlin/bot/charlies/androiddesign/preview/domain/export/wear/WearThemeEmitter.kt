package bot.charlies.androiddesign.preview.domain.export.wear

import bot.charlies.androiddesign.preview.domain.export.Arg
import bot.charlies.androiddesign.preview.domain.export.Call
import bot.charlies.androiddesign.preview.domain.export.Decl
import bot.charlies.androiddesign.preview.domain.export.FileContext
import bot.charlies.androiddesign.preview.domain.export.FunDecl
import bot.charlies.androiddesign.preview.domain.export.Sym
import bot.charlies.androiddesign.preview.domain.export.arg
import bot.charlies.androiddesign.preview.domain.export.stmt
import bot.charlies.androiddesign.preview.model.WearCatalog

/** The name of the exported theme function. */
internal const val WEAR_THEME = "WearAppTheme"

/**
 * `WearAppTheme`: Wear's `MaterialTheme` over a `ColorScheme` that lists every role as a literal.
 * The literals are the values the previewer rendered; the typography is Wear's default.
 */
internal class WearThemeEmitter(private val file: FileContext, private val colors: WearColors) {
  fun declarations(): List<Decl> {
    val color = file.use(Sym.Color)
    val scheme =
      Call(
        file.use(WearSym.ColorScheme),
        WearCatalog.colorRoles.map { role -> arg(role, "$color(${colors.roles.getValue(role)})") },
      )
    val theme =
      Call(
        file.use(WearSym.MaterialTheme),
        listOf<Arg>(arg("colorScheme", scheme), arg("content", "content")),
      )
    file.use(Sym.Composable)
    return listOf(
      FunDecl(
        annotations =
          listOf(
            "/** The color roles are the values the previewer drew; the type scale is Wear's own. */",
            "@Composable",
          ),
        modifiers = "",
        name = WEAR_THEME,
        params = listOf("content: @Composable () -> Unit"),
        body = listOf(stmt(theme)),
      ),
    )
  }
}
