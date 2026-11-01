package bot.charlies.androiddesign.preview.domain.export

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A proposal that states a catalog default exports the same Compose code as one that leaves the
 * prop out. Numbers and Text overflow are left out: a stated size, step or overflow is written even
 * when it matches Compose.
 */
class CatalogDefaultsExportTest {
  /** [required] is a component's required props as JSON members; [defaults] its stated defaults. */
  private class Case(val component: String, val required: String, val defaults: String)

  private val cases =
    listOf(
      Case(
        "Column",
        """"children":[]""",
        """"horizontalAlignment":"start","arrangement":"start","scroll":false""",
      ),
      Case("Row", """"children":[]""", """"verticalAlignment":"top","arrangement":"start""""),
      Case("Box", """"children":[]""", """"contentAlignment":"topStart""""),
      Case("Divider", "", """"vertical":false"""),
      Case("Icon", """"name":"home"""", """"filled":false"""),
      Case("Card", """"content":[]""", """"variant":"filled""""),
      Case(
        "Button",
        """"label":"Go"""",
        """"variant":"filled","size":"small","shape":"round","enabled":true,"primary":false""",
      ),
      Case(
        "ToggleButton",
        """"label":"Go"""",
        """"variant":"filled","size":"small","checked":false""",
      ),
      Case(
        "IconButton",
        """"icon":"edit"""",
        """"variant":"standard","size":"small","width":"uniform","toggle":false""",
      ),
      Case("SplitButton", """"label":"Go"""", """"variant":"filled""""),
      Case("AssistChip", """"label":"Go"""", """"variant":"outlined""""),
      Case("FilterChip", """"label":"Go"""", """"variant":"outlined","selected":false"""),
      Case("TextField", "", """"variant":"filled","error":false"""),
      Case("ProgressIndicator", "", """"shape":"linear","wavy":false"""),
      Case("LoadingIndicator", "", """"contained":false"""),
      Case("Switch", "", """"checked":false"""),
      Case("FloatingActionButton", """"icon":"add"""", """"size":"small""""),
      Case("Image", """"src":"photo.png"""", """"fit":"fit""""),
      Case("FlexBox", """"children":[]""", """"wrap":true"""),
    )

  private fun export(case: Case, members: String): String {
    val props = listOf(case.required, members).filter { it.isNotEmpty() }.joinToString(",")
    val subject =
      """{"id":"x","component":"${case.component}"${if (props.isEmpty()) "" else ",$props"}}"""
    return ExportFixtures.export("""{"id":"root","component":"Column","children":["x"]}""", subject)
  }

  @Test
  fun `stating a default exports the same code as leaving it out`() {
    val different = cases.filter { export(it, "") != export(it, it.defaults) }
    assertEquals(emptyList(), different.map { it.component })
  }
}
