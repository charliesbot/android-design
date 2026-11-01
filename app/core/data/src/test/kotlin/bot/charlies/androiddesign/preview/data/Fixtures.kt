package bot.charlies.androiddesign.preview.data

import java.nio.file.Files
import java.nio.file.Path

/** Test resources under `fixtures/`, copied into temp folders so tests can edit them. */
object Fixtures {
  fun text(name: String): String =
    requireNotNull(Fixtures::class.java.getResourceAsStream("/fixtures/$name")) {
        "missing fixture $name"
      }
      .bufferedReader()
      .use { it.readText() }

  const val PLATES = "plates.proposal.json"
  const val DESIGN_MD = "DESIGN.md"

  /**
   * A fresh temp folder holding the plates proposal, and the fixture DESIGN.md when [withDesignMd].
   */
  fun workspace(withDesignMd: Boolean = true): Workspace {
    val root = Files.createTempDirectory("preview-test")
    val design = root.resolve("design").also(Files::createDirectories)
    val proposal = design.resolve(PLATES).also { Files.writeString(it, text(PLATES)) }
    val designMd = root.resolve("DESIGN.md")
    if (withDesignMd) Files.writeString(designMd, text(DESIGN_MD))
    return Workspace(root, proposal, designMd)
  }

  class Workspace(val root: Path, val proposal: Path, val designMd: Path)
}
