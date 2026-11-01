package bot.charlies.androiddesign.preview.feature.preview.viewer

import bot.charlies.androiddesign.preview.data.FileProposalRepository
import bot.charlies.androiddesign.preview.domain.ProposalSnapshot
import bot.charlies.androiddesign.preview.model.Device
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.Views
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class ViewerViewModelTest {
  private val fixtures = Path.of(System.getProperty("fixtures.dir"))
  private val plates = fixtures.resolve("plates.proposal.json")

  private fun load(path: Path): ProposalSnapshot = runBlocking {
    FileProposalRepository().load(path)
  }

  private val viewModel = ViewerViewModel()

  /** The fixture written to a temp folder, so a test can break it. */
  private fun tempCopy(text: String = Files.readString(plates)): Path {
    val dir = Files.createTempDirectory("viewer")
    return dir.resolve("plates.proposal.json").also { Files.writeString(it, text) }
  }

  @Test
  fun `starts empty on a light phone`() {
    val state = viewModel.state.value
    assertFalse(state.hasProposal)
    assertEquals(Views.Light, state.view)
    assertNull(viewModel.currentScreen)
  }

  @Test
  fun `a valid proposal fills the state`() {
    viewModel.show(load(plates))
    val state = viewModel.state.value
    assertTrue(state.valid && state.hasProposal)
    assertEquals("Tonight's plate", state.name)
    assertEquals(listOf("plates", "plate_detail"), state.screens)
    assertEquals("#B5523B", state.design?.seed)
    assertEquals("plates", viewModel.currentScreen)
  }

  @Test
  fun `an invalid proposal reports errors and keeps the last good render`() {
    val path = tempCopy()
    viewModel.show(load(path))
    val good = viewModel.state.value
    Files.writeString(path, Files.readString(path).replace("\"Scaffold\"", "\"Scaffoldd\""))

    viewModel.show(load(path))
    val broken = viewModel.state.value
    assertFalse(broken.valid)
    assertTrue(broken.errors.any { "Scaffoldd" in it.message }, broken.errors.toString())
    assertSame(good.host, broken.host, "the running proposal is kept")
    assertEquals(good.design, broken.design)
    assertEquals(good.screens, broken.screens)

    Files.writeString(path, Files.readString(plates))
    viewModel.show(load(path))
    assertTrue(viewModel.state.value.valid)
    assertTrue(viewModel.state.value.errors.isEmpty())
  }

  @Test
  fun `an invalid first load has errors and nothing to draw`() {
    val path = tempCopy("{ not json")
    viewModel.show(load(path))
    val state = viewModel.state.value
    assertFalse(state.valid)
    assertFalse(state.hasProposal)
  }

  @Test
  fun `reloading the same file keeps navigation and the host`() {
    val path = tempCopy()
    viewModel.show(load(path))
    val host = assertNotNull(viewModel.state.value.host)
    viewModel.selectScreen("plate_detail")
    Files.writeString(
      path,
      Files.readString(path).replace("Tonight's plate\"\n", "Tonight's plates\"\n"),
    )

    viewModel.show(load(path))
    assertSame(host, viewModel.state.value.host)
    assertEquals("plate_detail", viewModel.currentScreen)
  }

  @Test
  fun `opening another file starts over`() {
    viewModel.show(load(tempCopy()))
    val first = viewModel.state.value.host
    viewModel.show(load(tempCopy()))
    assertTrue(viewModel.state.value.host !== first)
  }

  @Test
  fun `selecting a screen navigates the way the graph reaches it`() {
    viewModel.show(load(plates))
    viewModel.selectScreen("plate_detail")
    assertEquals(listOf("plates", "plate_detail"), viewModel.state.value.host?.backStack?.toList())
    viewModel.selectScreen("plates")
    assertEquals(listOf("plates"), viewModel.state.value.host?.backStack?.toList())
  }

  @Test
  fun `device and brightness pick the view`() {
    viewModel.setDevice(Device.Tablet)
    assertEquals(Views.Tablet, viewModel.state.value.view)
    viewModel.setDark(true)
    assertTrue(viewModel.state.value.view.dark)
    assertEquals(Device.Tablet, viewModel.state.value.view.device)
    viewModel.setDevice(Device.Phone)
    assertEquals(Views.Dark, viewModel.state.value.view)
  }

  @Test
  fun `seed and variant override the proposal design without touching it`() {
    viewModel.show(load(plates))
    val own = viewModel.state.value.design
    viewModel.setSeed("#0061A4")
    viewModel.setVariant(PaletteVariant.Vibrant)
    val state = viewModel.state.value
    assertEquals("#0061A4", state.effectiveDesign?.seed)
    assertEquals(PaletteVariant.Vibrant, state.effectiveDesign?.variant)
    assertEquals(own, state.design, "the proposal's own design is unchanged")

    viewModel.setSeed(null)
    viewModel.setVariant(null)
    assertEquals(own, viewModel.state.value.effectiveDesign)
  }

  @Test
  fun `viewer choices survive a reload and a new file`() {
    viewModel.setDevice(Device.Desktop)
    viewModel.setDark(true)
    viewModel.show(load(plates))
    assertEquals(Device.Desktop, viewModel.state.value.device)
    assertTrue(viewModel.state.value.dark)
    viewModel.show(load(tempCopy()))
    assertEquals(Device.Desktop, viewModel.state.value.device)
  }

  @Test
  fun `a frame failure is reported like a validation error and clears with the next good proposal`() {
    val path = tempCopy()
    viewModel.show(load(path))
    val host = viewModel.state.value.host

    viewModel.frameFailed(IllegalStateException("boom\nsecond line"))

    val failed = viewModel.state.value
    assertFalse(failed.valid)
    assertSame(host, failed.host, "the last good proposal stays")
    val error = failed.errors.single()
    assertEquals("render", error.path)
    assertTrue("boom" in error.message && "second line" !in error.message, error.message)

    viewModel.show(load(path))
    assertTrue(viewModel.state.value.valid)
    assertSame(host, viewModel.state.value.host)
  }
}
