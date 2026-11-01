package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.has
import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.lacks
import bot.charlies.androiddesign.preview.model.MaterialOverride
import bot.charlies.androiddesign.preview.model.SchemeOverride
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.TypographyOverride
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The theme is MaterialKolor inside MaterialExpressiveTheme, plus typography and shapes. */
class ThemeExportTest {
  private fun flat(text: String) =
    text.replace(Regex("\\s+"), " ").replace(", )", ")").replace(" .", ".")

  private val text = """{"id":"root","component":"Text","text":"x"}"""

  private fun export(theme: ThemeOverride?, vararg components: String = arrayOf(text)): String =
    flat(
      ExportFixtures.export(*components, design = DesignSystemResolver.resolve(theme, null)),
    )

  @Test
  fun `the scheme comes from the seed variant and contrast`() {
    val source =
      export(
        ThemeOverride(
          colors = mapOf("primary" to "#B5523B"),
          material = MaterialOverride(SchemeOverride(variant = "vibrant", contrast = 0.5)),
        ),
      )
    has(source, "private val SeedColor = Color(0xFFB5523B)")
    has(source, "rememberDynamicColorScheme( seedColor = SeedColor, isDark = darkTheme,")
    has(source, "style = PaletteStyle.Vibrant")
    has(source, "contrastLevel = 0.5")
    has(source, "specVersion = ColorSpec.SpecVersion.SPEC_2025")
    has(source, "MaterialExpressiveTheme(")
    has(source, "motionScheme = MotionScheme.expressive()")
    has(source, "darkTheme: Boolean = isSystemInDarkTheme()")
  }

  @Test
  fun `defaults are the Material purple seed with tonal spot`() {
    val source = export(null)
    has(source, "Color(0xFF6750A4)")
    has(source, "style = PaletteStyle.TonalSpot")
    has(source, "contrastLevel = 0.0")
  }

  @Test
  fun `explicit role colors replace generated ones`() {
    val source =
      export(
        ThemeOverride(
          colors = mapOf("primary" to "#B5523B", "surface-container-high" to "#112233"),
        ),
      )
    has(source, "val dynamicScheme = rememberDynamicColorScheme(")
    has(source, "val colorScheme = dynamicScheme.copy(surfaceContainerHigh = Color(0xFF112233))")
    has(source, "colorScheme = colorScheme")
  }

  @Test
  fun `every type role gets Google Sans Flex unless the design says otherwise`() {
    val source = export(null)
    has(
      source,
      "private val GoogleSansFlex = variableFontFamily(R.font.google_sans_flex, listOf(400, 500, 700))",
    )
    has(source, "bodyLarge = BaseTypography.bodyLarge.copy(fontFamily = GoogleSansFlex)")
    has(source, "// R.font.google_sans_flex")
    lacks(source, "R.font.fraunces")
  }

  @Test
  fun `a brand face with variation axes is its own family`() {
    val source =
      export(
        ThemeOverride(
          typography =
            mapOf(
              "display-large" to
                TypographyOverride(
                  fontFamily = "Fraunces",
                  fontVariation = "'wght' 600, 'opsz' 72, 'SOFT' 50, 'wdth' 90",
                ),
              "headline-large" to
                TypographyOverride(
                  fontFamily = "Big Shoulders Display",
                  fontWeight = 800,
                  letterSpacing = "-0.5px",
                ),
              "label-small" to TypographyOverride(fontFamily = "Google Sans Code"),
            ),
        ),
      )
    has(
      source,
      "private val FrauncesDisplayLarge = variableFontFamily( R.font.fraunces, listOf(400, 500, 600, 700),",
    )
    has(source, "FontVariation.opticalSizing(72.sp)")
    has(source, "FontVariation.width(90f)")
    has(source, "FontVariation.Setting(\"SOFT\", 50f)")
    has(
      source,
      "displayLarge = BaseTypography.displayLarge.copy( fontFamily = FrauncesDisplayLarge, fontWeight = FontWeight(600)",
    )
    has(source, "listOf(400, 500, 700, 800)")
    has(
      source,
      "headlineLarge = BaseTypography.headlineLarge.copy( fontFamily = BigShouldersDisplay, fontWeight = FontWeight(800), letterSpacing = (-0.5).sp)",
    )
    has(source, "R.font.google_sans_code")
    has(source, "// R.font.big_shoulders_display")
  }

  @Test
  fun `emphasized roles carry Google's token weight and tracking`() {
    val source = export(null)
    has(
      source,
      "titleMediumEmphasized = BaseTypography.titleMedium.copy( fontFamily = GoogleSansFlex, fontWeight = FontWeight(700), letterSpacing = 0.15.sp)",
    )
    has(
      source,
      "displayLargeEmphasized = BaseTypography.displayLarge.copy( fontFamily = GoogleSansFlex, fontWeight = FontWeight(500), letterSpacing = 0.sp)",
    )
  }

  @Test
  fun `shapes come from the rounded scale`() {
    val source = export(ThemeOverride(rounded = mapOf("extra-large" to "40px", "small" to "6dp")))
    has(source, "small = RoundedCornerShape(6.dp)")
    has(source, "extraLarge = RoundedCornerShape(40.dp)")
    has(source, "shapes = AppShapes")
  }

  @Test
  fun `the header lists each drawable once in order`() {
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"Column","children":["b","a","c"]}""",
        """{"id":"b","component":"Icon","name":"star"}""",
        """{"id":"a","component":"Icon","name":"add"}""",
        """{"id":"c","component":"Icon","name":"star"}""",
      )
    val header = source.substringBefore("\npackage ")
    assertEquals(
      listOf("add", "star"),
      Regex("R\\.drawable\\.(\\w+)").findAll(header).map { it.groupValues[1] }.toList(),
    )
  }

  @Test
  fun `the theme function is named after the proposal`() {
    assertTrue(ExportFixtures.export(text).contains("fun TestTheme("))
    val named =
      KotlinExporter.export(
        ExportFixtures.proposal(text, name = "Tonight's plate"),
        ExportFixtures.defaultDesign(),
        "main",
      )
    assertTrue(named.contains("fun TonightsPlateTheme("))
  }
}
