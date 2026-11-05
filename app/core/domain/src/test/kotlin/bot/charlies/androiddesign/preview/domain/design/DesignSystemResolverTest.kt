package bot.charlies.androiddesign.preview.domain.design

import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.ShapeToken
import bot.charlies.androiddesign.preview.model.ThemeOverride
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.json.Json

class DesignSystemResolverTest {
  private fun theme(json: String) = Json.decodeFromString(ThemeOverride.serializer(), json)

  @Test
  fun `no DESIGN dot md and no theme gives the defaults`() {
    val ds = DesignSystemResolver.resolve(null, null)
    assertEquals("#6750A4", ds.seed)
    assertEquals(PaletteVariant.TonalSpot, ds.variant)
    assertEquals(0.0, ds.contrast)
    assertEquals(emptyMap(), ds.colorOverrides)
    assertEquals(8f, ds.spacing["sm"])
    assertEquals(48f, ds.spacing["xxxl"])
    assertEquals(28f, ds.rounded[ShapeToken.ExtraLarge])
    assertEquals(emptyMap(), ds.styles)
    assertNull(ds.name)
  }

  @Test
  fun `colors primary is the seed`() {
    val ds = DesignSystemResolver.resolve(theme("""{"colors":{"primary":"#b5523b"}}"""), null)
    assertEquals("#B5523B", ds.seed)
    assertEquals(emptyMap(), ds.colorOverrides)
  }

  @Test
  fun `the scheme seed can reference colors primary`() {
    val ds =
      DesignSystemResolver.resolve(
        theme(
          """{"colors":{"primary":"#B5523B"},"material":{"scheme":{"seed":"{colors.primary}","variant":"expressive","contrast":0.5}}}""",
        ),
        null,
      )
    assertEquals("#B5523B", ds.seed)
    assertEquals(PaletteVariant.Expressive, ds.variant)
    assertEquals(0.5, ds.contrast)
  }

  @Test
  fun `a proposal theme deep merges over DESIGN dot md`() {
    val designMd =
      theme(
        """{"name":"Plates","colors":{"primary":"#B5523B","surface":"#FFF8F5"},"typography":{"display-large":{"fontFamily":"Fraunces","fontVariation":"'opsz' 72"}},"rounded":{"extra-large":"28px"},"spacing":{"md":"16px"},"material":{"scheme":{"seed":"{colors.primary}","variant":"tonal-spot"},"styles":{"hero":{"type":"displayLarge","color":"primary"}}}}""",
      )
    val proposal =
      theme(
        """{"colors":{"primary":"#006A6A"},"typography":{"display-large":{"fontWeight":600}},"rounded":{"extra-large":"32px"},"material":{"scheme":{"variant":"vibrant"},"styles":{"hero":{"color":"onPrimaryContainer"}}}}""",
      )
    val ds = DesignSystemResolver.resolve(designMd, proposal)
    assertEquals("Plates", ds.name)
    assertEquals("#006A6A", ds.seed)
    assertEquals(PaletteVariant.Vibrant, ds.variant)
    assertEquals("#FFF8F5", ds.colorOverrides["surface"])
    val display = ds.typography.getValue("displayLarge")
    assertEquals("Fraunces", display.fontFamily)
    assertEquals(600, display.fontWeight)
    assertEquals(32f, ds.rounded[ShapeToken.ExtraLarge])
    assertEquals(mapOf("md" to 16f), ds.spacing)
    val hero = ds.styles.getValue("hero")
    assertEquals("displayLarge", hero.type)
    assertEquals("onPrimaryContainer", hero.color)
  }

  @Test
  fun `dimensions accept px dp and bare numbers`() {
    val ds =
      DesignSystemResolver.resolve(
        null,
        theme("""{"spacing":{"a":"4px","b":"6dp","c":"8","d":"2.5px"}}"""),
      )
    assertEquals(mapOf("a" to 4f, "b" to 6f, "c" to 8f, "d" to 2.5f), ds.spacing)
  }

  @Test
  fun `unknown names are dropped from the resolved system`() {
    val ds =
      DesignSystemResolver.resolve(
        theme(
          """{"colors":{"brand-accent":"#ff00ff"},"typography":{"headline-xl":{"fontFamily":"Fraunces"}},"rounded":{"wobbly":"4px"}}""",
        ),
        null,
      )
    assertEquals(emptyMap(), ds.colorOverrides)
    assertEquals(emptyMap(), ds.typography)
  }
}
