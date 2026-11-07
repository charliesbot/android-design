package bot.charlies.androiddesign.preview.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ComponentIdsTest {
  @Test
  fun `the phone enum names exactly the phone catalog's components`() {
    assertEquals(
      M3ExpressiveCatalog.components.map { it.name }.toSet(),
      M3Component.entries.map { it.wire }.toSet(),
    )
  }

  @Test
  fun `the Wear enum names exactly the Wear catalog's components`() {
    assertEquals(
      WearCatalog.components.map { it.name }.toSet(),
      WearComponent.entries.map { it.wire }.toSet(),
    )
  }

  @Test
  fun `an unknown wire name is null from fromWire and an error from of`() {
    assertNull(M3Component.fromWire("TabRow"))
    assertFailsWith<IllegalStateException> { WearComponent.of("Carousel") }
  }
}
