package bot.charlies.androiddesign.preview.feature.preview

import bot.charlies.androiddesign.preview.data.ProposalCodec
import bot.charlies.androiddesign.preview.data.ProposalParse
import bot.charlies.androiddesign.preview.model.Views
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * The eval verify proposal uses every catalog component, so drawing all of it is the widest check
 * that no component crashes in some layout. Every screen at every view must render.
 */
class VerifyProposalTest {
  private val verify = Path.of(System.getProperty("repo.dir"), "evals/verify/verify.proposal.json")

  @Test
  fun `every screen of the verify proposal renders at every view`() {
    val proposal =
      assertIs<ProposalParse.Parsed>(ProposalCodec.parse(Files.readString(verify))).proposal
    val design = designOf(proposal.theme)
    val failures = buildList {
      for (screen in proposal.screens.keys) {
        for (view in Views.all) {
          try {
            render(proposal, screen, view, design)
          } catch (e: Exception) {
            add("$screen/${view.name}: ${e::class.simpleName}: ${e.message}")
          }
        }
      }
    }
    assertEquals(emptyList(), failures)
  }
}
