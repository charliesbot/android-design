package bot.charlies.androiddesign.preview.domain.export.wear

import bot.charlies.androiddesign.preview.domain.PreviewException
import bot.charlies.androiddesign.preview.domain.export.KotlinExporter
import bot.charlies.androiddesign.preview.domain.export.wear.WearExportFixtures.has
import bot.charlies.androiddesign.preview.domain.export.wear.WearExportFixtures.lacks
import bot.charlies.androiddesign.preview.domain.export.wear.WearExportFixtures.root
import bot.charlies.androiddesign.preview.model.WearCatalog
import kotlin.test.Test
import kotlin.test.assertFailsWith

/** `WearAppTheme`: the resolved color scheme as literals, Wear's own typography. */
class WearThemeExportTest {
  private val source =
    WearExportFixtures.export(root("t"), """{"id":"t","component":"Text","text":"Hi"}""")

  @Test
  fun `the theme lists every Wear color role as a literal`() {
    has(
      source,
      "@Composable fun WearAppTheme(content: @Composable () -> Unit) { MaterialTheme(colorScheme = ColorScheme(",
    )
    WearCatalog.colorRoles.forEach { role ->
      val hex = WearExportFixtures.colors.getValue(role).removePrefix("#")
      has(source, "$role = Color(0xFF$hex)")
    }
    has(source, "import androidx.wear.compose.material3.ColorScheme")
    has(source, "import androidx.wear.compose.material3.MaterialTheme")
  }

  @Test
  fun `the typography is Wear's default and needs no font files`() {
    lacks(source, "typography =")
    lacks(source, "R.font")
    lacks(source, "FontFamily")
  }

  @Test
  fun `the file names the Wear Compose version it targets`() {
    has(source, "// Targets androidx.wear.compose 1.8.0-alpha03")
  }

  @Test
  fun `a Wear export without the resolved colors is refused`() {
    val proposal =
      WearExportFixtures.proposal(root("t"), """{"id":"t","component":"Text","text":"Hi"}""")
    assertFailsWith<PreviewException> {
      KotlinExporter.export(proposal, WearExportFixtures.design(), "main")
    }
  }

  @Test
  fun `a scheme that lacks a role is refused`() {
    assertFailsWith<PreviewException> { WearColors.of(WearExportFixtures.colors - "primary") }
  }
}
