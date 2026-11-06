package bot.charlies.androiddesign.preview.app.export

import bot.charlies.androiddesign.preview.domain.export.KotlinExporter
import bot.charlies.androiddesign.preview.domain.export.wear.WearColors
import bot.charlies.androiddesign.preview.feature.preview.theme.wear.WearThemeResolver
import bot.charlies.androiddesign.preview.model.Catalogs
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.WearCatalog

/**
 * The one way the app turns a proposal into Compose source, for the MCP `export_kotlin` tool and
 * the `export` command. A Wear proposal's color scheme is computed by the Wear theme in the
 * previewer (the exporter's module has no Compose), so it is resolved here and handed over.
 */
object KotlinExport {
  fun of(proposal: Proposal, design: DesignSystem, screen: String? = null): String {
    val wearColors =
      if (Catalogs.of(proposal) === WearCatalog) {
        WearColors.of(WearThemeResolver.roleColors(design))
      } else {
        null
      }
    return KotlinExporter.export(proposal, design, screen, wearColors)
  }
}
