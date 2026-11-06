package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.PreviewException
import bot.charlies.androiddesign.preview.domain.export.wear.WearColors
import bot.charlies.androiddesign.preview.domain.export.wear.WearExporter
import bot.charlies.androiddesign.preview.model.Catalogs
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.WearCatalog

/**
 * Turns a proposal into Compose source for an Android app on AndroidX material3 1.5.0-alpha. Null
 * [screen] exports every screen plus the Navigation 3 host. The output is deterministic and laid
 * out the way ktfmt (Google style) writes Kotlin.
 *
 * Structure of the file: the theme (`MaterialExpressiveTheme` over a MaterialKolor scheme, with the
 * design system's typography and shapes), the navigation host for a full export, one `@Composable`
 * per screen with a `@Preview`, the `private` drawable lookups that icon names and image sources
 * bound to data need, and the sample data as public data classes and `private` objects. Named
 * styles and tokens become plain parameters and modifiers, never the Styles API.
 *
 * A Wear proposal goes to [WearExporter] (`export/wear/`), which writes Wear Compose Material 3.
 */
object KotlinExporter {
  /**
   * [wearColors] is the Wear color scheme the previewer drew a Wear proposal with. It is computed
   * where Compose is available, so the caller that has it passes it in; a phone proposal ignores
   * it.
   */
  fun export(
    proposal: Proposal,
    design: DesignSystem,
    screen: String? = null,
    wearColors: WearColors? = null,
  ): String {
    val catalog = Catalogs.of(proposal)
    if (catalog === WearCatalog) return WearExporter.export(proposal, design, screen, wearColors)
    if (catalog !== M3ExpressiveCatalog) {
      throw PreviewException("exporting a ${catalog.key} proposal to Compose is not supported yet")
    }
    return SampleModel.writeAvoidingClashes { reserved ->
      build(proposal, design, screen, reserved)
    }
  }

  private fun build(
    proposal: Proposal,
    design: DesignSystem,
    screen: String?,
    reserved: Set<String>,
  ): BuiltSource {
    val screens = screensToExport(proposal, screen)

    val file = FileContext()
    val title = proposal.name ?: design.name ?: "Design"
    val base = Naming.pascal(title)
    val themeName = "${base}Theme"
    val samples = SampleModel(reserved)

    val emitters = screens.associateWith { id ->
      val definition = proposal.screens.getValue(id)
      ScreenEmitter(
        file,
        design,
        id,
        definition,
        samples.forScreen(id, definition.dataModel),
        themeName,
      )
    }

    val decls = mutableListOf<Decl>()
    decls += ThemeEmitter(file, design, themeName).declarations()
    if (screen == null) {
      decls += NavigationEmitter(file, proposal, emitters, themeName, "${base}App").declarations()
    }
    emitters.values.forEach { decls += it.declarations() }
    decls += file.lookupDecls()
    decls += samples.classDecls()
    emitters.values.forEach { emitter -> emitter.sampleDeclaration()?.let { decls += it } }

    val code = CodePrinter.print(decls)
    val optIn = file.optInAnnotation()
    val source = buildString {
      append(header(title, file))
      optIn?.let { append(it).append("\n\n") }
      append("package $EXPORT_PACKAGE\n\n")
      append(file.importLines(code + optIn.orEmpty()).joinToString("\n")).append("\n\n")
      append(code)
    }
    return BuiltSource(source, file.importedNames(), samples.classNames())
  }

  private fun header(title: String, file: FileContext): String = buildString {
    appendGeneratedBy(title)
    appendPackageAndDrawableNotes(file)
    if (file.fonts.isNotEmpty()) {
      appendLine("//")
      appendLine("// Add these fonts (TTF) to res/font:")
      file.fonts.forEach { appendLine("//   R.font.$it") }
    }
    appendLine()
  }
}
