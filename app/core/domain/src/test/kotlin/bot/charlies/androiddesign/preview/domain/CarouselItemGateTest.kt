package bot.charlies.androiddesign.preview.domain

import bot.charlies.androiddesign.preview.model.CarouselItemSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The one rule that decides whether an `itemSize` node shows, for the renderer and the export. */
class CarouselItemGateTest {
  // Measured sizes of a multi-browse carousel at density 2, in pixels: large items 344, a small
  // item 69, an anchor 9, and a maxSize of 355 that sits above the large item on screen.
  private fun shown(minimum: CarouselItemSize, size: Float, max: Float = 355f) =
    CarouselItemGate.isShown(minimum, size, max, density = 2f)

  @Test
  fun `a large node shows on an item at full size and not on a smaller one`() {
    assertTrue(shown(CarouselItemSize.Large, 344.3f))
    assertTrue(shown(CarouselItemSize.Large, 355f))
    assertFalse(shown(CarouselItemSize.Large, 226f))
    assertFalse(shown(CarouselItemSize.Large, 69.3f))
    assertFalse(shown(CarouselItemSize.Large, 9.3f))
  }

  @Test
  fun `a medium node shows on large and medium items and hides on the small one`() {
    assertTrue(shown(CarouselItemSize.Medium, 344.3f))
    assertTrue(shown(CarouselItemSize.Medium, 226f))
    assertTrue(shown(CarouselItemSize.Medium, 113f))
    // The small item is at most 56 dp, 112 px here.
    assertFalse(shown(CarouselItemSize.Medium, 112f))
    assertFalse(shown(CarouselItemSize.Medium, 69.3f))
    assertFalse(shown(CarouselItemSize.Medium, 9.3f))
  }

  @Test
  fun `a carousel of only small items shows every node`() {
    for (minimum in CarouselItemSize.entries) {
      assertTrue(shown(minimum, size = 100f, max = 100f))
    }
  }

  @Test
  fun `before the first layout, when every size is zero, nodes show`() {
    for (minimum in CarouselItemSize.entries) {
      assertTrue(shown(minimum, size = 0f, max = 0f))
    }
  }

  @Test
  fun `the export writes the same thresholds the renderer reads`() {
    val info = "carouselItemDrawInfo"
    assertEquals(
      "$info.size >= $info.maxSize * 0.85f",
      CarouselItemGate.expression(CarouselItemSize.Large, info, "SMALL"),
    )
    assertEquals(
      "$info.size >= $info.maxSize * 0.85f || $info.size > SMALL",
      CarouselItemGate.expression(CarouselItemSize.Medium, info, "SMALL"),
    )
  }
}
