package bot.charlies.androiddesign.preview.host.robolectric

import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.model.MaterialOverride
import bot.charlies.androiddesign.preview.model.PhoneSurface
import bot.charlies.androiddesign.preview.model.SchemeOverride
import bot.charlies.androiddesign.preview.model.ThemeOverride
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DesignSystemResolverIcuTest {
  @Test
  fun resolvesColorReferenceUnderAndroidIcuRegexEngine() {
    assertTrue(DesignSystemResolver.isColorReference("{colors.primary}"))
    assertTrue(DesignSystemResolver.isColorReference("{colors.on-surface}"))
    assertFalse(DesignSystemResolver.isColorReference("#6750A4"))

    val theme =
      ThemeOverride(
        name = "Test",
        colors = mapOf("primary" to "#123456"),
        material = MaterialOverride(scheme = SchemeOverride(seed = "{colors.primary}")),
      )
    val resolved = DesignSystemResolver.resolve(theme, null, PhoneSurface)
    assertEquals("#123456", resolved.seed)
  }
}
