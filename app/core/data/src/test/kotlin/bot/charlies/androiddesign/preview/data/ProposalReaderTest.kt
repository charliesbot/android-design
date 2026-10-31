package bot.charlies.androiddesign.preview.data

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProposalReaderTest {
  private val reader = ProposalReader()
  private val notUtf8 = byteArrayOf('{'.code.toByte(), 0xC3.toByte(), 0x28, '}'.code.toByte())

  @Test
  fun `a proposal that is not utf-8 is an error that says so`() {
    val ws = Fixtures.workspace()
    Files.write(ws.proposal, notUtf8)

    val snapshot = reader.read(ws.proposal)

    assertNull(snapshot.proposal)
    assertFalse(snapshot.isValid)
    val error = snapshot.errors.single()
    assertTrue("UTF-8" in error.message, error.message)
    assertEquals("#B5523B", snapshot.designSystem.seed, "the DESIGN.md still applies")
  }

  @Test
  fun `a DESIGN dot md that is not utf-8 is an error naming the file`() {
    val ws = Fixtures.workspace()
    Files.write(ws.designMd, notUtf8)

    val snapshot = reader.read(ws.proposal)

    val error = snapshot.errors.single()
    assertEquals("/designMd", error.path)
    assertTrue(ws.designMd.toString() in error.message && "UTF-8" in error.message, error.message)
  }

  @Test
  fun `an explicit DESIGN dot md replaces the lookup`() {
    val ws = Fixtures.workspace()
    val other = ws.root.resolve("OTHER.md")
    Files.writeString(other, "---\nversion: alpha\nname: Other\n---\n")

    val snapshot = reader.read(ws.proposal, designMd = other)

    assertEquals(other, snapshot.designMdPath)
    assertEquals("Other", snapshot.designSystem.name)
  }

  @Test
  fun `an explicit DESIGN dot md that does not exist is an error`() {
    val ws = Fixtures.workspace()
    val missing = ws.root.resolve("MISSING.md")

    val snapshot = reader.read(ws.proposal, designMd = missing)

    assertNotNull(snapshot.proposal, "the proposal itself still reads")
    val error = snapshot.errors.single()
    assertEquals("/designMd", error.path)
    assertTrue(missing.toString() in error.message && "not found" in error.message, error.message)
  }
}
