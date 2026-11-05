package bot.charlies.androiddesign.preview.domain.catalog

import bot.charlies.androiddesign.preview.model.Catalogs
import java.io.File

/** Writes the committed schema files. Run through `./gradlew :core:domain:writeSchemas`. */
fun main(args: Array<String>) {
  val dir = File(args.singleOrNull() ?: error("usage: WriteSchemas <output dir>"))
  dir.mkdirs()
  for (catalog in Catalogs.all) {
    File(dir, CatalogJson.catalogFile(catalog))
      .writeText(CatalogJson.render(CatalogJson.catalog(catalog)))
  }
  File(dir, CatalogJson.PROPOSAL_SCHEMA_FILE)
    .writeText(CatalogJson.render(CatalogJson.proposalSchema()))
}
