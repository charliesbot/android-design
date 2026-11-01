package bot.charlies.androiddesign.preview.feature.preview.viewer

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import bot.charlies.androiddesign.preview.data.FileProposalRepository
import bot.charlies.androiddesign.preview.model.Device
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.ValidationError
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalTestApi::class)
class ViewerScreenTest {
  private val plates = Path.of(System.getProperty("fixtures.dir")).resolve("plates.proposal.json")

  private fun loaded(): ViewerViewModel =
    ViewerViewModel().also { vm -> vm.show(runBlocking { FileProposalRepository().load(plates) }) }

  @Test
  fun `the panel offers brightness, color, style and screens`() =
    runDesktopComposeUiTest(1280, 1400) {
      val vm = loaded()
      setContent { ViewerScreen(vm, frame = InlineFrame) }
      waitForIdle()
      for (title in listOf("Brightness", "Color", "Style", "Screens", "Light", "Dark")) {
        onNodeWithText(title).assertIsDisplayed()
      }
      for (preset in ViewerPresets.seeds) onNodeWithText(preset.name).assertExists()
      for (variant in ViewerPresets.variants) onNodeWithText(ViewerPresets.label(variant))
        .assertExists()
      onNodeWithText("plate_detail").assertExists()
      onNodeWithText("Tonight's plate · ", substring = true).assertExists()
    }

  @Test
  fun `the controls drive the view model`() =
    runDesktopComposeUiTest(1280, 1400) {
      val vm = loaded()
      setContent { ViewerScreen(vm, frame = InlineFrame) }
      waitForIdle()

      onNodeWithText("Dark").performClick()
      waitForIdle()
      assertTrue(vm.state.value.dark)

      onNodeWithText("Coral").performClick()
      onNodeWithText("Vibrant").performClick()
      waitForIdle()
      assertEquals("#984061", vm.state.value.seed)
      assertEquals(PaletteVariant.Vibrant, vm.state.value.variant)

      onNodeWithContentDescription("Tablet 1280x800").performClick()
      waitForIdle()
      assertEquals(Device.Tablet, vm.state.value.device)

      onNodeWithText("plate_detail").performClick()
      waitForIdle()
      assertEquals("plate_detail", vm.currentScreen)

      onNodeWithText("Proposal").performClick()
      waitForIdle()
      assertEquals(null, vm.state.value.seed)
    }

  @Test
  fun `the device toggle shows the selected device`() =
    runDesktopComposeUiTest(1280, 1400) {
      val vm = loaded()
      setContent { ViewerScreen(vm, frame = InlineFrame) }
      waitForIdle()
      onNodeWithContentDescription("Phone 411x923").assertIsOn()
      onNodeWithText("411×923", substring = true).assertExists()
      vm.setDevice(Device.Foldable)
      waitForIdle()
      onNodeWithContentDescription("Foldable 933x704").assertIsOn()
      onNodeWithText("933×704", substring = true).assertExists()
    }

  @Test
  fun `a narrow window keeps every section and moves the panel above the stage`() {
    var wideDeviceTop = 0f
    var narrowDeviceTop = 0f
    runDesktopComposeUiTest(1280, 1400) {
      setContent { ViewerScreen(loaded(), frame = InlineFrame) }
      waitForIdle()
      wideDeviceTop =
        onNodeWithContentDescription("Phone 411x923").fetchSemanticsNode().boundsInRoot.top
    }
    runDesktopComposeUiTest(600, 1400) {
      setContent { ViewerScreen(loaded(), frame = InlineFrame) }
      waitForIdle()
      for (title in listOf("Brightness", "Color", "Style", "Screens")) {
        onNodeWithText(title).assertExists()
      }
      narrowDeviceTop =
        onNodeWithContentDescription("Phone 411x923").fetchSemanticsNode().boundsInRoot.top
    }
    assertTrue(
      narrowDeviceTop > wideDeviceTop + 100,
      "wide $wideDeviceTop, narrow $narrowDeviceTop",
    )
  }

  @Test
  fun `validation errors replace the frame and the last good render comes back`() =
    runDesktopComposeUiTest(1280, 1400) {
      val vm = loaded()
      setContent { ViewerScreen(vm, frame = InlineFrame) }
      waitForIdle()
      onNodeWithTag("root", useUnmergedTree = true).assertExists()

      val good = vm.state.value
      vm.show(
        runBlocking { FileProposalRepository().load(plates) }
          .copy(
            errors =
              listOf(ValidationError("/screens/plates/components/2/text", "must be a string")),
          ),
      )
      waitForIdle()
      onNodeWithText("1 validation error").assertIsDisplayed()
      onNodeWithText("/screens/plates/components/2/text").assertIsDisplayed()
      onNodeWithTag("root", useUnmergedTree = true).assertDoesNotExist()
      assertEquals(good.host, vm.state.value.host)

      vm.show(runBlocking { FileProposalRepository().load(plates) })
      waitForIdle()
      onNodeWithTag("root", useUnmergedTree = true).assertExists()
    }

  @Test
  fun `the frame scales down to fit the stage and never up`() {
    val fits =
      fitScale(
        availableWidth = androidx.compose.ui.unit.Dp(1000f),
        availableHeight = androidx.compose.ui.unit.Dp(600f),
        frameWidth = androidx.compose.ui.unit.Dp(431f),
        frameHeight = androidx.compose.ui.unit.Dp(943f),
      )
    assertEquals((600f - 36f) / 943f, fits, 0.0001f)
    val roomy =
      fitScale(
        androidx.compose.ui.unit.Dp(3000f),
        androidx.compose.ui.unit.Dp(3000f),
        androidx.compose.ui.unit.Dp(431f),
        androidx.compose.ui.unit.Dp(943f),
      )
    assertEquals(1f, roomy)
  }
}
