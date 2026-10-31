package bot.charlies.androiddesign.preview.data

import java.nio.file.Files
import java.nio.file.Path

/** Finds the DESIGN.md for a proposal: its folder, then each parent; the first one wins. */
object DesignMdLocator {
  const val FILE_NAME = "DESIGN.md"

  /** Every place a DESIGN.md could be, nearest first. */
  fun candidates(proposal: Path): List<Path> =
    generateSequence(proposal.toAbsolutePath().normalize().parent) { it.parent }
      .map { it.resolve(FILE_NAME) }
      .toList()

  fun find(proposal: Path): Path? = candidates(proposal).firstOrNull { Files.isRegularFile(it) }
}
