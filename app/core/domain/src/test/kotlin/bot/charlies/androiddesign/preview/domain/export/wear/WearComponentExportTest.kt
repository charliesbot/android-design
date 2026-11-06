package bot.charlies.androiddesign.preview.domain.export.wear

import bot.charlies.androiddesign.preview.domain.export.wear.WearExportFixtures.flat
import bot.charlies.androiddesign.preview.model.WearCatalog
import kotlin.test.Test
import kotlin.test.assertEquals

/** The Wear Compose call written for every `WearCatalog` component. */
class WearComponentExportTest {
  @Test
  fun `every catalog component has an export case`() {
    val covered = wearCases.flatMap { it.covers }.toSet()
    // Screen is the frame: WearScreenExportTest covers it.
    val expected = WearCatalog.components.map { it.name }.toSet() - "Screen"
    assertEquals(expected - covered, emptySet())
    assertEquals(emptySet(), covered - expected)
  }

  @Test
  fun `each case writes the calls it expects`() {
    val failures = mutableListOf<String>()
    wearCases.forEachIndexed { i, case ->
      val source = WearExportFixtures.export(*case.components.toTypedArray())
      WearExportFixtures.dump("case-$i-${case.covers.first()}.kt", source)
      val flat = flat(source)
      case.expect
        .filter { !flat.contains(flat(it)) }
        .forEach { failures += "${case.covers} #$i should contain:\n  $it" }
      case.reject
        .filter { flat.contains(flat(it)) }
        .forEach { failures += "${case.covers} #$i should not contain:\n  $it" }
    }
    assertEquals(emptyList(), failures)
  }
}
