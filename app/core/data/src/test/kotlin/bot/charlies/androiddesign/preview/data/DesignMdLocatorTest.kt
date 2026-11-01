package bot.charlies.androiddesign.preview.data

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DesignMdLocatorTest {
  @Test
  fun `a DESIGN dot md next to the proposal wins`() {
    val ws = Fixtures.workspace()
    val near = ws.proposal.parent.resolve("DESIGN.md").also { Files.writeString(it, "near") }
    assertEquals(near, DesignMdLocator.find(ws.proposal))
  }

  @Test
  fun `parent folders are searched nearest first`() {
    val ws = Fixtures.workspace()
    assertEquals(ws.designMd, DesignMdLocator.find(ws.proposal))
    val middle = ws.root.resolve("design/DESIGN.md").also { Files.writeString(it, "middle") }
    assertEquals(middle, DesignMdLocator.find(ws.proposal))
  }

  @Test
  fun `no DESIGN dot md means none`() {
    val ws = Fixtures.workspace(withDesignMd = false)
    // The system temp folder has no DESIGN.md above it.
    assertNull(DesignMdLocator.find(ws.proposal))
  }

  @Test
  fun `candidates run from the proposal folder upward`() {
    val ws = Fixtures.workspace()
    val candidates = DesignMdLocator.candidates(ws.proposal)
    assertEquals(ws.proposal.parent.resolve("DESIGN.md"), candidates.first())
    assertEquals(ws.root.resolve("DESIGN.md"), candidates[1])
  }
}
