package bot.charlies.androiddesign.preview.domain.export.wear

import bot.charlies.androiddesign.preview.domain.PreviewException
import bot.charlies.androiddesign.preview.domain.export.CodePrinter
import bot.charlies.androiddesign.preview.domain.export.Decl
import bot.charlies.androiddesign.preview.domain.export.EXPORT_PACKAGE
import bot.charlies.androiddesign.preview.domain.export.FileContext
import bot.charlies.androiddesign.preview.domain.export.Naming
import bot.charlies.androiddesign.preview.domain.export.appendGeneratedBy
import bot.charlies.androiddesign.preview.domain.export.appendPackageAndDrawableNotes
import bot.charlies.androiddesign.preview.domain.export.screensToExport
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.Proposal
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Turns a Wear proposal into Wear Compose Material 3 source, with the same layout as the phone
 * export: the theme, the Navigation 3 host for a full export, one `@Composable` per screen with a
 * `@Preview`, then the helpers the screens call. The output is deterministic and laid out the way
 * ktfmt writes Kotlin.
 *
 * The theme lists the color scheme the previewer drew the proposal with as literals. That scheme is
 * computed where Compose is available, so the caller passes it in as [export]'s `colors`.
 * Typography is Wear's default, so no font files are needed.
 *
 * Not exported yet: data models, bindings and templates. A Wear screen written with them is
 * refused.
 */
internal object WearExporter {
  /** The `androidx.wear.compose` release the code targets: `app/wear/compose/upstream/VERSION`. */
  const val WEAR_COMPOSE_VERSION = "1.8.0-alpha03"

  fun export(
    proposal: Proposal,
    design: DesignSystem,
    screen: String?,
    colors: WearColors?,
  ): String {
    val scheme =
      colors
        ?: throw PreviewException(
          "a Wear export needs the resolved Wear color scheme, which the previewer computes",
        )
    val screens = screensToExport(proposal, screen)
    screens.forEach { rejectBindings(it, proposal) }

    val file = FileContext()
    val title = proposal.name ?: design.name ?: "Design"
    val emitters = screens.associateWith { id ->
      WearScreenEmitter(file, design, id, proposal.screens.getValue(id))
    }

    val decls = mutableListOf<Decl>()
    decls += WearThemeEmitter(file, scheme).declarations()
    if (screen == null) {
      decls +=
        WearNavigationEmitter(file, proposal, emitters, "${Naming.pascal(title)}App").declarations()
    }
    emitters.values.forEach { decls += it.declarations() }
    decls += helperDecls(file, emitters.values)

    val code = CodePrinter.print(decls)
    return buildString {
      append(header(title, file))
      append("package $EXPORT_PACKAGE\n\n")
      append(file.importLines(code).joinToString("\n")).append("\n\n")
      append(code)
    }
  }

  /** The functions and objects the screens call, written once for the file. */
  private fun helperDecls(file: FileContext, emitters: Collection<WearScreenEmitter>): List<Decl> =
    buildList {
      if (emitters.any { it.usesLarge }) add(WearHelpers.largeDisplay(file))
      if (emitters.any { it.usesFixedScroll }) add(WearHelpers.fixedScrollInfoProvider(file))
    }

  private fun header(title: String, file: FileContext): String = buildString {
    appendGeneratedBy(title)
    appendLine(
      "// Targets androidx.wear.compose $WEAR_COMPOSE_VERSION (material3, foundation, navigation3).",
    )
    appendPackageAndDrawableNotes(file)
    if (file.images.isNotEmpty()) {
      appendLine("//")
      appendLine("// Add these images as drawables:")
      file.images.forEach { appendLine("//   R.drawable.$it") }
    }
    appendLine()
  }

  // region unsupported input

  private fun rejectBindings(id: String, proposal: Proposal) {
    val screen = proposal.screens.getValue(id)
    if (screen.dataModel != null || screen.components.any { it.props.values.any(::binds) }) {
      throw PreviewException(
        "screen '$id' uses data bindings or templates, which the Wear export does not write yet; " +
          "write the values into the components",
      )
    }
  }

  /** True for a `{"@path": ...}` binding, an `@index` call, or a template over a data array. */
  private fun binds(element: JsonElement): Boolean =
    when (element) {
      is JsonArray -> element.any(::binds)
      is JsonObject ->
        "@path" in element ||
          "componentId" in element ||
          (element["@call"] as? JsonPrimitive)?.content == "@index" ||
          element.values.any(::binds)
      else -> false
    }

  // endregion
}
