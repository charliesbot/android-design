package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/** The Compose call written for every `M3ExpressiveCatalog` component. */
class ComponentExportTest {
  private fun export(case: ComponentCase): String {
    val proposal = ExportFixtures.proposal(*case.components.toTypedArray())
    return KotlinExporter.export(proposal, ExportFixtures.defaultDesign(), "main")
  }

  /** Whitespace and trailing commas folded away, so a line break does not matter. */
  private fun flat(text: String) =
    text.replace(Regex("\\s+"), " ").replace(", )", ")").replace("( ", "(").replace(" .", ".")

  @Test
  fun `every catalog component has an export case`() {
    val covered = componentCases.flatMap { it.covers }.toSet()
    assertEquals(M3ExpressiveCatalog.components.map { it.name }.toSet() - covered, emptySet())
    assertEquals(emptySet(), covered - M3ExpressiveCatalog.components.map { it.name }.toSet())
  }

  @Test
  fun `each case writes the calls it expects`() {
    val dump = System.getenv("DUMP_EXPORT")
    val failures = mutableListOf<String>()
    componentCases.forEachIndexed { i, case ->
      val source = export(case)
      val flat = flat(source)
      if (dump != null) File(dump, "case-$i-${case.covers.first()}.kt").writeText(source)
      val body = source.substringAfter("\n\n@Composable\nfun MainScreen(")
      case.expect
        .filter { !flat.contains(flat(it)) }
        .forEach { failures += "${case.covers} #$i should contain:\n  $it" }
      case.reject
        .filter { flat(body).contains(flat(it)) }
        .forEach { failures += "${case.covers} #$i should not contain:\n  $it" }
    }
    assertEquals(emptyList(), failures)
  }
}
