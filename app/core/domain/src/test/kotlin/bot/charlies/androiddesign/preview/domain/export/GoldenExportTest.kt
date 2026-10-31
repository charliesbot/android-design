package bot.charlies.androiddesign.preview.domain.export

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Exports the shared plates fixture and compares it with `plates.golden.kt`. The golden file is in
 * ktfmt form so spotless accepts it. Rewrite it with `UPDATE_GOLDEN=1 ./gradlew :core:domain:test
 * --tests '*GoldenExportTest*'` after a deliberate change, then read the diff.
 */
class GoldenExportTest {
  @Test
  fun `the plates export matches the golden file`() {
    val (proposal, design) = ExportFixtures.plates()
    val actual = KotlinExporter.export(proposal, design)
    if (System.getenv("UPDATE_GOLDEN") == "1") {
      File("src/test/resources/export/plates.golden.kt").writeText(actual)
      return
    }
    val golden =
      requireNotNull(javaClass.getResourceAsStream("/export/plates.golden.kt")) {
          "missing golden file"
        }
        .bufferedReader()
        .use { it.readText() }
    assertEquals(golden, actual)
  }
}
