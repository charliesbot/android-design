package bot.charlies.androiddesign.preview.data

import bot.charlies.androiddesign.preview.domain.validation.ProposalValidator
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.ThemeOverride
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * The proposals committed outside the app (the eval verify page and the skill's format example)
 * must stay valid against the catalog, so a catalog change cannot silently break them.
 */
class RepoProposalsTest {
  private val repo: Path = findRepoRoot()

  private fun proposal(text: String): Proposal =
    assertIs<ProposalParse.Parsed>(ProposalCodec.parse(text)).proposal

  private fun designMd(text: String): ThemeOverride =
    assertIs<DesignMdParse.Parsed>(DesignMdParser.parse(text)).theme

  @Test
  fun `the verify proposal is valid`() {
    val verify = proposal(Files.readString(repo.resolve(VERIFY)))
    assertEquals(emptyList(), ProposalValidator().validate(verify))
  }

  @Test
  fun `the verify proposal uses every catalog component`() {
    val verify = proposal(Files.readString(repo.resolve(VERIFY)))
    val used = verify.screens.values.flatMap { screen -> screen.components.map { it.component } }
    val missing = M3ExpressiveCatalog.components.map { it.name } - used.toSet()
    assertEquals(emptyList(), missing)
  }

  @Test
  fun `the proposal format example is valid with its DESIGN md`() {
    val doc = Files.readString(repo.resolve(FORMAT_REFERENCE))
    val design = designMd(fencedBlocks(doc, "yaml").single())
    val example = proposal(fencedBlocks(doc.substringAfter("\n## Example\n"), "json").single())
    assertEquals(emptyList(), ProposalValidator().validate(example, design))
  }

  private fun fencedBlocks(markdown: String, language: String): List<String> =
    Regex("```$language\\n(.*?)\\n```", RegexOption.DOT_MATCHES_ALL)
      .findAll(markdown)
      .map { it.groupValues[1] }
      .toList()

  private companion object {
    const val VERIFY = "evals/verify/verify.proposal.json"
    const val FORMAT_REFERENCE = "skills/android-design-explore/references/proposal-format.md"

    /** Gradle runs tests from the module folder; the repository root holds `evals/`. */
    fun findRepoRoot(): Path {
      var dir: Path? = Paths.get("").toAbsolutePath()
      while (dir != null) {
        if (Files.exists(dir.resolve(VERIFY))) return dir
        dir = dir.parent
      }
      throw AssertionError("no $VERIFY above ${Paths.get("").toAbsolutePath()}")
    }
  }
}
