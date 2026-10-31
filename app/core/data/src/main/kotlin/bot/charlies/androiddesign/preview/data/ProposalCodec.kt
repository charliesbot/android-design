package bot.charlies.androiddesign.preview.data

import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.ValidationError
import kotlinx.serialization.SerializationException

sealed interface ProposalParse {
  data class Parsed(val proposal: Proposal) : ProposalParse

  data class Failed(val errors: List<ValidationError>) : ProposalParse
}

/**
 * Reads and writes `*.proposal.json`. Reading accepts keys in any order. Writing is stable: keys
 * follow the model's declaration order (components lead with `id` and `component`, then keep their
 * props in order), two-space indent, trailing newline.
 */
object ProposalCodec {
  private val json = PreviewJson.pretty

  fun parse(text: String): ProposalParse =
    try {
      ProposalParse.Parsed(json.decodeFromString(Proposal.serializer(), text))
    } catch (e: SerializationException) {
      ProposalParse.Failed(listOf(ValidationError(pointerOf(e.message), summarize(e.message))))
    } catch (e: IllegalArgumentException) {
      ProposalParse.Failed(listOf(ValidationError(pointerOf(e.message), summarize(e.message))))
    }

  fun write(proposal: Proposal): String =
    json.encodeToString(Proposal.serializer(), proposal) + "\n"

  private val PATH = Regex("at path:? (\\$\\S*)")
  private val SEGMENT = Regex("\\.([^.\\[]+)|\\['((?:[^'\\\\]|\\\\.)*)']|\\[(\\d+)]")

  /**
   * Turns kotlinx's `at path: $.screens.a.components[3].id` (map keys may appear as `['a']`) into
   * the pointer `/screens/a/components/3/id`.
   */
  private fun pointerOf(message: String?): String {
    val path = message?.let { PATH.find(it)?.groupValues?.get(1) } ?: return "/"
    val segments =
      SEGMENT.findAll(path).map {
        it.groupValues[1].ifEmpty { it.groupValues[2].ifEmpty { it.groupValues[3] } }
      }
    val pointer = segments.joinToString("") { "/" + it.replace("~", "~0").replace("/", "~1") }
    return pointer.ifEmpty { "/" }
  }

  private fun summarize(message: String?): String =
    (message ?: "invalid proposal file").lineSequence().first().take(300)
}
