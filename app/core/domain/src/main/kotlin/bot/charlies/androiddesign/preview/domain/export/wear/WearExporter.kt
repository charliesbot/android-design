package bot.charlies.androiddesign.preview.domain.export.wear

import bot.charlies.androiddesign.preview.domain.PreviewException
import bot.charlies.androiddesign.preview.domain.export.BuiltSource
import bot.charlies.androiddesign.preview.domain.export.CodePrinter
import bot.charlies.androiddesign.preview.domain.export.Decl
import bot.charlies.androiddesign.preview.domain.export.EXPORT_PACKAGE
import bot.charlies.androiddesign.preview.domain.export.FileContext
import bot.charlies.androiddesign.preview.domain.export.Naming
import bot.charlies.androiddesign.preview.domain.export.SampleModel
import bot.charlies.androiddesign.preview.domain.export.appendGeneratedBy
import bot.charlies.androiddesign.preview.domain.export.appendPackageAndDrawableNotes
import bot.charlies.androiddesign.preview.domain.export.screensToExport
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.Proposal

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
 * A screen's data model, bindings and templates are written as the phone export writes them: data
 * classes, a sample object, screen parameters, and loops (`items` in the scrolling list).
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
    return SampleModel.writeAvoidingClashes { reserved ->
      build(proposal, design, screen, scheme, reserved)
    }
  }

  private fun build(
    proposal: Proposal,
    design: DesignSystem,
    screen: String?,
    scheme: WearColors,
    reserved: Set<String>,
  ): BuiltSource {
    val screens = screensToExport(proposal, screen)
    val file = FileContext()
    val title = proposal.name ?: design.name ?: "Design"
    val samples = SampleModel(reserved)
    val emitters = screens.associateWith { id ->
      val definition = proposal.screens.getValue(id)
      WearScreenEmitter(file, design, id, definition, samples.forScreen(id, definition.dataModel))
    }

    val decls = mutableListOf<Decl>()
    decls += WearThemeEmitter(file, scheme).declarations()
    if (screen == null) {
      decls +=
        WearNavigationEmitter(file, proposal, emitters, "${Naming.pascal(title)}App").declarations()
    }
    emitters.values.forEach { decls += it.declarations() }
    decls += helperDecls(file, emitters.values)
    decls += file.lookupDecls()
    decls += samples.classDecls()
    emitters.values.forEach { emitter -> emitter.sampleDeclaration()?.let { decls += it } }

    val code = CodePrinter.print(decls)
    val source = buildString {
      append(header(title, file))
      append("package $EXPORT_PACKAGE\n\n")
      append(file.importLines(code).joinToString("\n")).append("\n\n")
      append(code)
    }
    return BuiltSource(source, file.importedNames(), samples.classNames())
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
}
