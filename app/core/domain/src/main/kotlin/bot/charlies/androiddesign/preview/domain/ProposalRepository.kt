package bot.charlies.androiddesign.preview.domain

import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.ValidationError
import java.nio.file.Path
import kotlinx.coroutines.flow.Flow

/**
 * One reading of a proposal file together with the DESIGN.md it resolved.
 *
 * @property proposal Null when the file could not be read or parsed; [errors] then says why.
 * @property designMdPath The DESIGN.md that applies, or null when none was found (defaults apply).
 * @property designSystem DESIGN.md merged with the proposal's `theme`, never null.
 * @property errors Parse and validation errors. Empty means the proposal is ready to render.
 */
data class ProposalSnapshot(
  val path: Path,
  val designMdPath: Path?,
  val proposal: Proposal?,
  val designSystem: DesignSystem,
  val errors: List<ValidationError>,
) {
  val isValid: Boolean
    get() = proposal != null && errors.isEmpty()
}

interface ProposalRepository {
  /**
   * Reads, parses and validates [path] once. Never throws for a bad file; see [ProposalSnapshot].
   */
  suspend fun load(path: Path): ProposalSnapshot

  /**
   * Emits a snapshot now and again after every change to the proposal or to the DESIGN.md it
   * resolved, debounced. Collecting it is what watches the files.
   */
  fun watch(path: Path): Flow<ProposalSnapshot>
}
