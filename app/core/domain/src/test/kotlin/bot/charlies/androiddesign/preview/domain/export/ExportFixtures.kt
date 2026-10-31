package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.ThemeOverride
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import java.io.File
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

/** Proposals and design systems for the export tests. */
internal object ExportFixtures {
  private val json = Json { ignoreUnknownKeys = false }
  private val yaml = Yaml(configuration = YamlConfiguration(strictMode = false))

  /** The shared plates fixture that `:core:data` keeps, with its adjacent DESIGN.md. */
  fun plates(): Pair<Proposal, DesignSystem> {
    val dir = File(requireNotNull(System.getProperty("fixtures.dir")) { "fixtures.dir is not set" })
    val proposal =
      json.decodeFromString(Proposal.serializer(), File(dir, "plates.proposal.json").readText())
    val designMd = parseFrontMatter(File(dir, "DESIGN.md").readText())
    return proposal to DesignSystemResolver.resolve(designMd, proposal.theme)
  }

  fun parseFrontMatter(text: String): ThemeOverride {
    val lines = text.lines()
    val end = lines.drop(1).indexOfFirst { it.trimEnd() == "---" }
    val frontMatter = lines.subList(1, end + 1).joinToString("\n")
    return yaml.decodeFromString(ThemeOverride.serializer(), frontMatter)
  }

  /** The default design system: no DESIGN.md and no theme. */
  fun defaultDesign(): DesignSystem = DesignSystemResolver.resolve(null, null)

  /**
   * A one-screen proposal whose components are the given JSON objects, root first. [dataModel] is
   * the screen's data model JSON when it has one.
   */
  fun proposal(
    vararg components: String,
    dataModel: String? = null,
    name: String = "Test",
    navigation: String = """{"start":"main"}""",
  ): Proposal {
    val data = if (dataModel != null) """"dataModel":$dataModel,""" else ""
    val text =
      """{"format":1,"name":"$name","navigation":$navigation,"screens":{"main":{"catalogId":"${M3ExpressiveCatalog.ID}",$data"components":[${components.joinToString(",")}]}}}"""
    return json.decodeFromString(Proposal.serializer(), text)
  }

  fun export(
    vararg components: String,
    dataModel: String? = null,
    design: DesignSystem = defaultDesign(),
  ): String = KotlinExporter.export(proposal(*components, dataModel = dataModel), design, "main")

  /** The screen function's body: the text after the `fun MainScreen(` signature. */
  fun body(source: String): String =
    source.substringAfter("fun MainScreen(").substringAfter(") {\n").substringBefore("\n}\n")

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
}
