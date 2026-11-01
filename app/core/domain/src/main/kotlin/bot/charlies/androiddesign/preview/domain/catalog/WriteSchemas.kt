package bot.charlies.androiddesign.preview.domain.catalog

import java.io.File

/** Writes the committed schema files. Run through `./gradlew :core:domain:writeSchemas`. */
fun main(args: Array<String>) {
  val dir = File(args.singleOrNull() ?: error("usage: WriteSchemas <output dir>"))
  dir.mkdirs()
  File(dir, CatalogJson.CATALOG_FILE).writeText(CatalogJson.render(CatalogJson.catalog()))
  File(dir, CatalogJson.PROPOSAL_SCHEMA_FILE)
    .writeText(CatalogJson.render(CatalogJson.proposalSchema()))
}
