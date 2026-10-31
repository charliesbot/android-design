package bot.charlies.androiddesign.preview.domain.findings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ContrastTest {
  @Test
  fun `luminance of black and white`() {
    assertEquals(0.0, Contrast.relativeLuminance("#000000"))
    assertEquals(1.0, Contrast.relativeLuminance("#FFFFFF")!!, 1e-9)
  }

  @Test
  fun `black on white is 21 to 1 in either order`() {
    assertEquals(21.0, Contrast.ratio("#000000", "#FFFFFF")!!, 1e-9)
    assertEquals(21.0, Contrast.ratio("#FFFFFF", "#000000")!!, 1e-9)
  }

  @Test
  fun `a color against itself is 1 to 1`() {
    assertEquals(1.0, Contrast.ratio("#6750A4", "#6750A4")!!, 1e-9)
  }

  @Test
  fun `known grays match the WCAG reference values`() {
    assertEquals(4.54, Contrast.ratio("#767676", "#FFFFFF")!!, 0.005)
    assertEquals(4.48, Contrast.ratio("#777777", "#FFFFFF")!!, 0.005)
  }

  @Test
  fun `malformed colors have no ratio`() {
    assertNull(Contrast.relativeLuminance("white"))
    assertNull(Contrast.relativeLuminance("#FFF"))
    assertNull(Contrast.ratio("#000000", "nope"))
  }

  @Test
  fun `large text starts at 24 dp or 18_66 dp bold`() {
    assertEquals(4.5, Contrast.requiredRatio(23.9f, bold = false))
    assertEquals(3.0, Contrast.requiredRatio(24f, bold = false))
    assertEquals(4.5, Contrast.requiredRatio(18.6f, bold = true))
    assertEquals(3.0, Contrast.requiredRatio(18.66f, bold = true))
    assertEquals(4.5, Contrast.requiredRatio(18.66f, bold = false))
  }
}
