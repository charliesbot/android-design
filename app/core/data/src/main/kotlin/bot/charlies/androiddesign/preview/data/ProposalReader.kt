package bot.charlies.androiddesign.preview.data

import bot.charlies.androiddesign.preview.domain.ProposalSnapshot
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.domain.validation.ProposalValidator
import bot.charlies.androiddesign.preview.model.Catalogs
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.ValidationError
import java.io.IOException
import java.nio.charset.CharacterCodingException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path

/**
 * Reads a proposal file and the DESIGN.md that applies to it into one [ProposalSnapshot]. The
 * window, the MCP tools and the command line all load proposals through this, so a file means the
 * same thing everywhere. A bad file is an error in the snapshot, never an exception.
 */
class ProposalReader(private val validator: ProposalValidator = ProposalValidator()) {
  /**
   * @param designMd The DESIGN.md to use instead of the lookup ([DesignMdLocator]: the proposal's
   *   folder, then each parent).
   */
  fun read(file: Path, designMd: Path? = null): ProposalSnapshot {
    val proposalPath = file.toAbsolutePath().normalize()
    val designMdPath = designMd?.toAbsolutePath()?.normalize() ?: DesignMdLocator.find(proposalPath)
    val errors = mutableListOf<ValidationError>()
    val designTheme = designMdPath?.let { readDesignMd(it, errors) }

    val proposal: Proposal? =
      when (val text = readText(proposalPath)) {
        is Text.Failed -> {
          errors += ValidationError("/", "proposal file ${text.reason}: $proposalPath")
          null
        }
        is Text.Read ->
          when (val parsed = ProposalCodec.parse(text.value)) {
            is ProposalParse.Parsed -> parsed.proposal
            is ProposalParse.Failed -> {
              errors += parsed.errors
              null
            }
          }
      }
    if (proposal != null) errors += validator.validate(proposal, designTheme)

    return ProposalSnapshot(
      proposalPath,
      designMdPath,
      proposal,
      DesignSystemResolver.resolve(
        designTheme,
        proposal?.theme,
        proposal?.let(Catalogs::of) ?: M3ExpressiveCatalog,
      ),
      errors,
    )
  }

  private fun readDesignMd(path: Path, errors: MutableList<ValidationError>): ThemeOverride? =
    when (val text = readText(path)) {
      is Text.Failed -> {
        errors += ValidationError("/designMd", "$path: DESIGN.md ${text.reason}")
        null
      }
      is Text.Read ->
        when (val parsed = DesignMdParser.parse(text.value)) {
          is DesignMdParse.Parsed -> parsed.theme
          is DesignMdParse.Failed -> {
            errors += ValidationError("/designMd", "$path: ${parsed.message}")
            null
          }
        }
    }

  private sealed interface Text {
    class Read(val value: String) : Text

    class Failed(val reason: String) : Text
  }

  private fun readText(path: Path): Text =
    try {
      Text.Read(Files.readString(path))
    } catch (_: NoSuchFileException) {
      Text.Failed("not found")
    } catch (_: CharacterCodingException) {
      Text.Failed("is not valid UTF-8 text")
    } catch (e: IOException) {
      Text.Failed("could not be read (${e.message ?: e::class.simpleName})")
    }
}
