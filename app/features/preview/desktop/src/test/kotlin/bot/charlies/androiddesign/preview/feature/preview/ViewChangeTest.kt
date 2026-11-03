package bot.charlies.androiddesign.preview.feature.preview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import bot.charlies.androiddesign.preview.feature.preview.nav.ProposalPreview
import bot.charlies.androiddesign.preview.feature.preview.render.ProposalHostState
import bot.charlies.androiddesign.preview.model.Views
import kotlin.test.Test

/** The window keeps one host while the view changes, so every screen must follow the new size. */
@OptIn(ExperimentalTestApi::class)
class ViewChangeTest {
  // A view change or a back step can land a frame after waitForIdle returns, so each check waits
  // for the text to appear or go, and fails only when it never does.
  private fun ComposeUiTest.shows(text: String) =
    waitUntil(timeoutMillis = TIMEOUT_MS) {
      onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }

  private fun ComposeUiTest.hides(text: String) =
    waitUntil(timeoutMillis = TIMEOUT_MS) {
      onAllNodesWithText(text).fetchSemanticsNodes().isEmpty()
    }

  private val sized =
    """{"id":"root","component":"Scaffold","content":"col"},{"id":"col","component":"Column","children":["wide","narrow"]},""" +
      """{"id":"wide","component":"Text","text":"wide","showIn":["expanded"]},{"id":"narrow","component":"Text","text":"narrow","showIn":["compact","medium"]}"""

  @Test
  fun `a screen follows the size class when the view changes`() =
    runDesktopComposeUiTest(2000, 1200) {
      val proposal = singleScreen(sized)
      val host = ProposalHostState(proposal)
      var view by mutableStateOf(Views.Light)
      setContent { ProposalPreview(host, designOf(), view) }
      shows("narrow")

      view = Views.Tablet
      shows("wide")
      hides("narrow")

      view = Views.Light
      shows("narrow")
      hides("wide")
    }

  @Test
  fun `a list-detail pair collapses to the list when the view narrows`() =
    runDesktopComposeUiTest(2000, 1200) {
      val proposal =
        proposalOf(
          "list" to
            screenJson(
              sized.replace(
                """"children":["wide","narrow"]""",
                """"children":["wide","narrow","rows"]""",
              ) +
                """,{"id":"rows","component":"List","variant":"segmented","children":["row"]},""" +
                """{"id":"row","component":"ListItem","headline":"Open","supporting":"the detail",""" +
                """"action":{"functionCall":{"@call":"navigate","args":{"screen":"detail"}}}}""",
            ),
          "detail" to
            screenJson(
              """{"id":"root","component":"Scaffold","topBar":"bar","content":"body"},""" +
                """{"id":"bar","component":"TopAppBar","title":"Detail","navigationIcon":"back"},""" +
                """{"id":"back","component":"IconButton","icon":"arrow_back","label":"Back","showIn":["compact","medium"],"action":{"functionCall":{"@call":"back"}}},""" +
                """{"id":"body","component":"Text","text":"detail body"}""",
            ),
          panes = """{"list":"list","detail":"detail"}""",
        )
      val host = ProposalHostState(proposal)
      var view by mutableStateOf(Views.Tablet)
      setContent { ProposalPreview(host, designOf(), view) }
      shows("wide")

      view = Views.Light
      shows("narrow")
      hides("wide")

      view = Views.Tablet
      host.navigate("detail")
      shows("detail body")
      shows("wide")
      view = Views.Light
      hides("wide")
      shows("detail body")

      // At foldable size the pair shows one pane, and opening the detail replaces the list.
      host.back()
      view = Views.Foldable
      hides("detail body")
      shows("Open")
      onNodeWithText("Open").performClick()
      shows("detail body")
      hides("Open")
    }
}

private const val TIMEOUT_MS = 5_000L
