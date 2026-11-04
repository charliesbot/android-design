package bot.charlies.androiddesign.preview.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import bot.charlies.androiddesign.preview.app.preview.HeadlessTreeRenderer
import bot.charlies.androiddesign.preview.app.preview.PreviewWindow
import bot.charlies.androiddesign.preview.app.preview.Sessions
import bot.charlies.androiddesign.preview.data.FileProposalRepository
import bot.charlies.androiddesign.preview.feature.preview.viewer.InlineFrame
import bot.charlies.androiddesign.preview.feature.preview.viewer.ViewerScreen
import bot.charlies.androiddesign.preview.feature.preview.viewer.ViewerViewModel
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalTestApi::class)
class PreviewWindowContentTest {
  private val fixtures = Path.of(System.getProperty("fixtures.dir"))
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  private val sessions =
    Sessions(
      repository = FileProposalRepository(),
      renderer = HeadlessTreeRenderer(),
      window = PreviewWindow(),
      scope = scope,
      version = AppInfo.VERSION,
      launchedByUser = true,
      onIdle = {},
    )

  /** The plates fixture copied next to its design file, its proposal renamed to [name]. */
  private fun proposal(name: String): String {
    val dir = Files.createTempDirectory("tabs")
    Files.copy(fixtures.resolve("DESIGN.md"), dir.resolve("DESIGN.md"))
    val text =
      Files.readString(fixtures.resolve("plates.proposal.json"))
        .replace("\"name\": \"Tonight's plate\"", "\"name\": \"$name\"")
    return dir.resolve("$name.proposal.json").also { Files.writeString(it, text) }.toString()
  }

  @Composable
  private fun inlineViewer(vm: ViewerViewModel, modifier: Modifier) =
    ViewerScreen(vm, modifier, frame = InlineFrame)

  @AfterTest
  fun stop() {
    scope.cancel()
  }

  @Test
  fun `no session shows the empty state`() =
    runDesktopComposeUiTest(1280, 900) {
      setContent { PreviewWindowContent(sessions, viewer = ::inlineViewer) }
      waitForIdle()
      onNodeWithText("No proposal open").assertIsDisplayed()
      onNodeWithText("File > Open Proposal", substring = true).assertIsDisplayed()
    }

  @Test
  fun `one session shows the viewer with no tab row`() =
    runDesktopComposeUiTest(1280, 900) {
      runBlocking { sessions.openLocal(proposal("Alpha")) }
      setContent { PreviewWindowContent(sessions, viewer = ::inlineViewer) }
      waitForIdle()
      onNodeWithText("Brightness").assertIsDisplayed()
      onNodeWithText("Alpha").assertDoesNotExist()
      onNodeWithContentDescription("Close Alpha").assertDoesNotExist()
    }

  @Test
  fun `two sessions show tabs that select and close`() =
    runDesktopComposeUiTest(1280, 900) {
      val first = runBlocking { sessions.openLocal(proposal("Alpha")) }
      val second = runBlocking { sessions.openLocal(proposal("Beta")) }
      setContent { PreviewWindowContent(sessions, viewer = ::inlineViewer) }
      waitForIdle()
      onNodeWithText("Alpha").assertIsDisplayed()
      onNodeWithText("Beta").assertIsDisplayed()
      assertSame(second, sessions.selected.value)

      onNodeWithText("Alpha").performClick()
      waitForIdle()
      assertSame(first, sessions.selected.value)

      onNodeWithContentDescription("Close Beta").performClick()
      waitForIdle()
      assertEquals(listOf(first), sessions.sessions.value)
      onNodeWithContentDescription("Close Alpha").assertDoesNotExist()
    }
}
