package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.model.BoardMeta
import bot.charlies.androiddesign.preview.model.DirectionMeta
import bot.charlies.androiddesign.preview.model.OverlayMeta
import bot.charlies.androiddesign.preview.model.PhoneSurface
import bot.charlies.androiddesign.preview.model.Views
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TargetResolverTest {
  private val multiDirMeta =
    BoardMeta(
      surface = "phone",
      boardName = "Multi",
      directions =
        listOf(
          DirectionMeta(
            name = "Editorial",
            surface = "phone",
            startScreen = "home",
            screens = listOf("home", "detail"),
            overlays = listOf(OverlayMeta("sheet", "home")),
          ),
          DirectionMeta(
            name = "Compact",
            surface = "phone",
            startScreen = "feed",
            screens = listOf("feed"),
          ),
        ),
      views = PhoneSurface.views,
    )

  @Test
  fun `resolveSingleTarget picks defaults and reports specific unknown codes`() {
    val ok =
      assertIs<TargetResolution.Ok<SingleTargetSelection>>(
        TargetResolver.resolveSingleTarget(multiDirMeta, null, null, null, null),
      )
    assertEquals("Editorial", ok.value.direction.name)
    assertEquals("home", ok.value.screenId)
    assertEquals(Views.Light, ok.value.view)
    assertEquals("Editorial__home", ok.value.prefix)

    val badDir =
      assertIs<TargetResolution.Error>(
        TargetResolver.resolveSingleTarget(multiDirMeta, "Nope", null, null, null),
      )
    assertEquals("unknown_direction", badDir.code)

    val badScreen =
      assertIs<TargetResolution.Error>(
        TargetResolver.resolveSingleTarget(multiDirMeta, "Compact", "home", null, null),
      )
    assertEquals("unknown_screen", badScreen.code)

    val badOverlay =
      assertIs<TargetResolution.Error>(
        TargetResolver.resolveSingleTarget(multiDirMeta, "Editorial", "home", "nope", null),
      )
    assertEquals("unknown_overlay", badOverlay.code)

    val badView =
      assertIs<TargetResolution.Error>(
        TargetResolver.resolveSingleTarget(multiDirMeta, "Editorial", "home", "sheet", "watch"),
      )
    assertEquals("unknown_view", badView.code)
  }

  @Test
  fun `resolveReportTargets validates views and slash-prefixed screens`() {
    val badView =
      assertIs<TargetResolution.Error>(
        TargetResolver.resolveReportTargets(
          meta = multiDirMeta,
          surface = PhoneSurface,
          requestedViews = listOf("nope"),
          requestedScreens = null,
        ),
      )
    assertEquals("unknown_view", badView.code)

    val badScreen =
      assertIs<TargetResolution.Error>(
        TargetResolver.resolveReportTargets(
          meta = multiDirMeta,
          surface = PhoneSurface,
          requestedViews = listOf("light"),
          requestedScreens = listOf("missing"),
        ),
      )
    assertEquals("unknown_screen", badScreen.code)

    val ok =
      assertIs<TargetResolution.Ok<List<CheckTargetSpec>>>(
        TargetResolver.resolveReportTargets(
          meta = multiDirMeta,
          surface = PhoneSurface,
          requestedViews = listOf("light"),
          requestedScreens = listOf("Editorial/home"),
        ),
      )
    assertEquals(listOf(null, "sheet"), ok.value.map { it.overlayId })
  }
}
