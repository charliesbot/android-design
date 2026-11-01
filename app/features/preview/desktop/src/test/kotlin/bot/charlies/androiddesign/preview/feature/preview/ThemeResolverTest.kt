package bot.charlies.androiddesign.preview.feature.preview

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.feature.preview.theme.FontAxis
import bot.charlies.androiddesign.preview.feature.preview.theme.FontFace
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalFonts
import bot.charlies.androiddesign.preview.feature.preview.theme.ThemeResolver
import bot.charlies.androiddesign.preview.feature.preview.theme.toHex
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.MaterialOverride
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.SchemeOverride
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.TypeRole
import bot.charlies.androiddesign.preview.model.TypographyOverride
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ThemeResolverTest {
  private fun design(theme: ThemeOverride) = DesignSystemResolver.resolve(null, theme)

  private val terracotta =
    ThemeOverride(
      colors = mapOf("primary" to "#B5523B"),
      material = MaterialOverride(scheme = SchemeOverride(variant = "expressive")),
    )

  @Test
  fun `the seed and variant generate the scheme and dark differs from light`() {
    val light = ThemeResolver.colorScheme(design(terracotta), dark = false)
    val dark = ThemeResolver.colorScheme(design(terracotta), dark = true)
    val tonal =
      ThemeResolver.colorScheme(
        design(
          terracotta.copy(
            material = MaterialOverride(scheme = SchemeOverride(variant = "tonal-spot")),
          ),
        ),
        dark = false,
      )
    assertNotEquals(light.surface, dark.surface)
    assertNotEquals(light.primaryContainer, tonal.primaryContainer, "variant changes the palette")
    assertEquals(DesignSystemResolver.parseHex("#B5523B"), "#B5523B")
  }

  @Test
  fun `contrast changes the scheme`() {
    val normal = ThemeResolver.colorScheme(design(terracotta), dark = false)
    val high =
      ThemeResolver.colorScheme(
        design(
          terracotta.copy(
            material =
              MaterialOverride(scheme = SchemeOverride(variant = "expressive", contrast = 1.0)),
          ),
        ),
        dark = false,
      )
    assertNotEquals(normal.onPrimaryContainer, high.onPrimaryContainer)
  }

  @Test
  fun `explicit role colors replace generated ones`() {
    val scheme =
      ThemeResolver.colorScheme(
        design(
          terracotta.copy(
            colors = mapOf("primary" to "#B5523B", "surface-container-high" to "#112233"),
          ),
        ),
        dark = false,
      )
    assertEquals("#112233", scheme.surfaceContainerHigh.toHex())
    assertEquals(
      ColorRole.SurfaceContainerHigh.wire,
      ColorRole.fromKebab("surface-container-high")?.wire,
    )
  }

  @Test
  fun `plain text is Google Sans Flex and a brand face takes only the roles that name it`() {
    val theme =
      ThemeResolver.resolve(
        design(
          ThemeOverride(
            typography =
              mapOf(
                "display-large" to
                  TypographyOverride(
                    fontFamily = "Fraunces",
                    fontVariation = "'opsz' 72, 'SOFT' 50",
                  ),
              ),
          ),
        ),
        dark = false,
      )
    assertEquals("Fraunces", theme.fontFamilies.getValue(TypeRole.DisplayLarge))
    assertEquals("Google Sans Flex", theme.fontFamilies.getValue(TypeRole.BodyLarge))
    assertEquals("Google Sans Flex", theme.fontFamilies.getValue(TypeRole.DisplayMedium))
    assertNotEquals(
      theme.style(TypeRole.DisplayLarge).fontFamily,
      theme.style(TypeRole.BodyLarge).fontFamily,
    )
    assertEquals(theme.style(TypeRole.DisplayLarge), theme.typography.displayLarge)
  }

  @Test
  fun `font variation strings parse into axes`() {
    assertEquals(
      listOf(FontAxis("opsz", 72f), FontAxis("SOFT", 50f)),
      ProposalFonts.parseVariation("'opsz' 72, 'SOFT' 50"),
    )
    assertEquals(listOf(FontAxis("wght", 500f)), ProposalFonts.parseVariation("\"wght\" 500"))
    assertTrue(ProposalFonts.parseVariation(null).isEmpty())
  }

  @Test
  fun `a family for a weight outside 1 to 1000 is built at the nearest one`() {
    for (weight in listOf(-5, 0, 1001, 5000)) {
      val family = ProposalFonts.family(FontFace.GoogleSansFlex, weight)
      assertEquals(
        ProposalFonts.family(FontFace.GoogleSansFlex, weight.coerceIn(1, 1000)),
        family,
      )
    }
  }

  @Test
  fun `variants of one font resource share a single base typeface`() {
    // Warm both resources, then count: further variants must not load the font data again.
    ProposalFonts.symbols(filled = false, sizeDp = 24)
    ProposalFonts.family(FontFace.Fraunces, 400)
    val before = ProposalFonts.baseTypefaceCount

    val symbols =
      (20..48).flatMap { size -> listOf(false, true).map { ProposalFonts.symbols(it, size) } }
    val frauncesAxes =
      (0..20).map {
        ProposalFonts.family(FontFace.Fraunces, 300 + it * 10, listOf(FontAxis("SOFT", it * 5f)))
      }

    assertEquals(58, symbols.toSet().size)
    assertEquals(21, frauncesAxes.toSet().size)
    assertEquals(before, ProposalFonts.baseTypefaceCount)
  }

  @Test
  fun `a family ignores a variation axis value that is not finite`() {
    val axes = listOf(FontAxis("opsz", Float.POSITIVE_INFINITY), FontAxis("SOFT", Float.NaN))
    ProposalFonts.family(FontFace.Fraunces, 400, axes)
  }

  @Test
  fun `a wght axis or fontWeight sets the style weight`() {
    val theme =
      ThemeResolver.resolve(
        design(
          ThemeOverride(
            typography =
              mapOf(
                "headline-large" to TypographyOverride(fontVariation = "'wght' 650"),
                "body-large" to TypographyOverride(fontWeight = 600, letterSpacing = "1px"),
              ),
          ),
        ),
        dark = false,
      )
    assertEquals(FontWeight(650), theme.style(TypeRole.HeadlineLarge).fontWeight)
    assertEquals(FontWeight(600), theme.style(TypeRole.BodyLarge).fontWeight)
    assertEquals(1f, theme.style(TypeRole.BodyLarge).letterSpacing.value)
  }

  @Test
  fun `emphasized roles are heavier than their baseline`() {
    val theme = ThemeResolver.resolve(design(ThemeOverride()), dark = false)
    assertEquals(FontWeight(700), theme.style(TypeRole.TitleMediumEmphasized).fontWeight)
    assertTrue(
      theme.style(TypeRole.DisplayLargeEmphasized).fontWeight!!.weight >
        theme.style(TypeRole.DisplayLarge).fontWeight!!.weight,
    )
  }

  @Test
  fun `the corner scale comes from rounded`() {
    val shapes =
      ThemeResolver.shapes(design(ThemeOverride(rounded = mapOf("extra-large" to "20px"))))
    val px = (shapes.extraLarge as RoundedCornerShape).topStart.toPx(Size(200f, 200f), Density(1f))
    assertEquals(20f, px)
    val defaultSmall =
      (shapes.small as RoundedCornerShape).topStart.toPx(Size(200f, 200f), Density(1f))
    assertEquals(8f, defaultSmall)
  }

  @Test
  fun `every variant resolves`() {
    PaletteVariant.entries.forEach { variant ->
      ThemeResolver.colorScheme(
        design(
          terracotta.copy(
            material = MaterialOverride(scheme = SchemeOverride(variant = variant.wire)),
          ),
        ),
        dark = true,
      )
    }
  }
}
