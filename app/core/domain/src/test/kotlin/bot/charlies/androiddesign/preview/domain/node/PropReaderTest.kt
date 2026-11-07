package bot.charlies.androiddesign.preview.domain.node

import bot.charlies.androiddesign.preview.domain.CarouselSpec
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

class PropReaderTest {
  private fun reader(component: String, vararg values: Pair<String, JsonElement>) =
    PropReader(M3ExpressiveCatalog.find(component)!!, values.toMap()::get)

  @Test
  fun `an absent prop reads as its catalog default`() {
    val carousel = reader("Carousel")
    assertEquals("multi-browse", carousel.enum("variant"))
    assertEquals(CarouselSpec.Variant.MultiBrowse, carousel.enum("variant", CarouselSpec::variant))
  }

  @Test
  fun `a stated value wins over the default`() {
    val carousel = reader("Carousel", "variant" to JsonPrimitive("hero"))
    assertEquals(CarouselSpec.Variant.Hero, carousel.enum("variant", CarouselSpec::variant))
  }

  @Test
  fun `an unknown enum value falls back to the default`() {
    val carousel = reader("Carousel", "variant" to JsonPrimitive("sideways"))
    assertEquals(CarouselSpec.Variant.MultiBrowse, carousel.enum("variant", CarouselSpec::variant))
  }

  @Test
  fun `a number outside its bounds is moved into them, and NaN is no number`() {
    val grid = reader("Grid", "columns" to JsonPrimitive(1000))
    assertEquals(24.0, grid.number("columns"))
    assertEquals(null, reader("Grid", "columns" to JsonPrimitive("NaN")).number("columns"))
  }

  @Test
  fun `a boolean reads a bound string and defaults to false without a catalog default`() {
    assertEquals(true, reader("Button", "primary" to JsonPrimitive("true")).bool("primary"))
    assertEquals(false, reader("Button").bool("primary"))
  }
}
