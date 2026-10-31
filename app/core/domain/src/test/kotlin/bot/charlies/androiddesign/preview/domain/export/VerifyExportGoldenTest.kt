package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.model.Proposal
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json

/**
 * Exports `evals/verify/verify.proposal.json`, which uses every catalog component, with the default
 * design and compares it with `verify.golden.kt`. Rewrite the golden with `UPDATE_GOLDEN=1
 * ./gradlew :core:domain:test --tests '*VerifyExportGoldenTest*'` after a deliberate change, then
 * read the diff.
 */
class VerifyExportGoldenTest {
  @Test
  fun `the verify export matches the golden file`() {
    val dir = File(requireNotNull(System.getProperty("verify.dir")) { "verify.dir is not set" })
    val proposal =
      Json.decodeFromString(Proposal.serializer(), File(dir, "verify.proposal.json").readText())
    val actual = KotlinExporter.export(proposal, ExportFixtures.defaultDesign())
    if (System.getenv("UPDATE_GOLDEN") == "1") {
      File("src/test/resources/export/verify.golden.kt").writeText(actual)
      return
    }
    val golden =
      requireNotNull(javaClass.getResourceAsStream("/export/verify.golden.kt")) {
          "missing golden file"
        }
        .bufferedReader()
        .use { it.readText() }
    assertEquals(golden, actual)
  }
}
