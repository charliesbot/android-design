package bot.charlies.androiddesign.preview.domain

import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.PropKind
import kotlin.test.Test
import kotlin.test.assertEquals

class VariantSpecTest {
  private fun catalogVariants(component: String): List<String> =
    (M3ExpressiveCatalog.find(component)!!.prop("variant")!!.kind as PropKind.Enum).values

  @Test
  fun `the carousel variants are the catalog's`() {
    assertEquals(catalogVariants("Carousel"), CarouselSpec.Variant.entries.map { it.wire })
  }

  @Test
  fun `the tooltip variants are the catalog's`() {
    assertEquals(catalogVariants("Tooltip"), TooltipSpec.Variant.entries.map { it.wire })
  }
}
