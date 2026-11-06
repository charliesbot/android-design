package bot.charlies.androiddesign.preview.domain.export.wear

import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.domain.export.KotlinExporter
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.WearCatalog
import java.io.File
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

/** Wear proposals and the color scheme the export tests hand the exporter. */
internal object WearExportFixtures {
  private val json = Json { ignoreUnknownKeys = false }

  /** Every Wear color role with its own value, so a swapped role shows in the output. */
  val colors: Map<String, String> =
    WearCatalog.colorRoles
      .mapIndexed { i, role -> role to "#10%02X%02X".format(0x20 + i, 0x80 + i) }
      .toMap()

  val scheme: WearColors = WearColors.of(colors)

  fun design(): DesignSystem = DesignSystemResolver.resolve(null, null)

  /** A one-screen Wear proposal named "Wear test"; its screen is "main". */
  fun proposal(
    vararg components: String,
    name: String = "Wear test",
    dataModel: String? = null,
  ): Proposal =
    json.decodeFromString(
      Proposal.serializer(),
      """{"format":1,"name":"$name","navigation":{"start":"main"},"screens":{"main":{"catalogId":"${WearCatalog.ID}",${dataModel?.let { """"dataModel":$it,""" }.orEmpty()}"components":[${components.joinToString(",")}]}}}""",
    )

  /** A Wear proposal from raw screens: `"home":[components]` pairs. */
  fun screens(start: String, vararg screens: Pair<String, List<String>>): Proposal =
    json.decodeFromString(
      Proposal.serializer(),
      """{"format":1,"name":"Wear test","navigation":{"start":"$start"},"screens":{${screens.joinToString(",") { (id, components) -> """"$id":{"catalogId":"${WearCatalog.ID}","components":[${components.joinToString(",")}]}""" }}}}""",
    )

  /** The `Screen` at the root with the given extra props and children. */
  fun root(vararg children: String, props: String = ""): String =
    """{"id":"root","component":"Screen",${if (props.isEmpty()) "" else "$props,"}"children":[${children.joinToString(",") { "\"$it\"" }}]}"""

  fun export(
    vararg components: String,
    screen: String? = "main",
    dataModel: String? = null,
  ): String =
    KotlinExporter.export(proposal(*components, dataModel = dataModel), design(), screen, scheme)

  fun export(proposal: Proposal, screen: String? = null): String =
    KotlinExporter.export(proposal, design(), screen, scheme)

  /** Whitespace and trailing commas folded away, so a line break does not matter. */
  fun flat(text: String) =
    text.replace(Regex("\\s+"), " ").replace(", )", ")").replace("( ", "(").replace(" .", ".")

  fun has(source: String, expected: String) {
    assertTrue(flat(source).contains(flat(expected)), "missing:\n$expected\n\nin:\n$source")
  }

  fun lacks(source: String, unexpected: String) {
    assertTrue(
      !flat(source).contains(flat(unexpected)),
      "should not contain:\n$unexpected\n\nin:\n$source",
    )
  }

  /** The text of [source] from `fun [name](` to the end of that function. */
  fun function(source: String, name: String): String =
    source.substringAfter("fun $name(").substringBefore("\n}\n")

  private fun dumpDir(): File? = System.getenv("DUMP_WEAR_EXPORT")?.let(::File)

  fun dump(name: String, source: String) {
    dumpDir()?.let { File(it, name).writeText(source) }
  }
}
