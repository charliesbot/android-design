package bot.charlies.androiddesign.preview.data

import bot.charlies.androiddesign.preview.domain.ProposalSnapshot
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.ShapeToken
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

class FileProposalRepositoryTest {
  private val repository =
    FileProposalRepository(poll = 10.milliseconds, debounce = 40.milliseconds)

  @Test
  fun `the fixture loads and validates clean`() = runBlocking {
    val ws = Fixtures.workspace(withDesignMd = false)
    val snapshot = repository.load(ws.proposal)
    assertEquals(emptyList(), snapshot.errors)
    assertTrue(snapshot.isValid)
    assertNull(snapshot.designMdPath)
    assertEquals("#B5523B", snapshot.designSystem.seed)
    assertEquals(PaletteVariant.Expressive, snapshot.designSystem.variant)
  }

  @Test
  fun `the DESIGN dot md found by the lookup rule is merged in`() = runBlocking {
    val ws = Fixtures.workspace()
    val snapshot = repository.load(ws.proposal)
    assertEquals(emptyList(), snapshot.errors)
    assertEquals(ws.designMd, snapshot.designMdPath)
    assertEquals("Tonight's plate", snapshot.designSystem.name)
    assertEquals(28f, snapshot.designSystem.rounded.getValue(ShapeToken.ExtraLarge))
    // The proposal's theme wins over DESIGN.md for the variant and the hero style.
    assertEquals(PaletteVariant.Expressive, snapshot.designSystem.variant)
    assertEquals("displaySmallEmphasized", snapshot.designSystem.styles.getValue("hero_title").type)
  }

  @Test
  fun `a missing file is an error not an exception`() = runBlocking {
    val ws = Fixtures.workspace()
    val snapshot = repository.load(ws.root.resolve("nope.proposal.json"))
    assertNull(snapshot.proposal)
    assertFalse(snapshot.isValid)
    assertTrue(snapshot.errors.single().message.contains("not found"))
  }

  @Test
  fun `malformed json is an error with the defaults as design system`() = runBlocking {
    val ws = Fixtures.workspace()
    Files.writeString(ws.proposal, "{ nope")
    val snapshot = repository.load(ws.proposal)
    assertNull(snapshot.proposal)
    assertEquals(1, snapshot.errors.size)
    assertEquals("#B5523B", snapshot.designSystem.seed) // from DESIGN.md
  }

  @Test
  fun `validation errors come with the snapshot`() = runBlocking {
    val ws = Fixtures.workspace()
    val broken =
      Fixtures.text(Fixtures.PLATES)
        .replace("\"label\": \"Cook\"", "\"label\": \"Cook\",\n        \"colour\": \"red\"")
    Files.writeString(ws.proposal, broken)
    val snapshot = repository.load(ws.proposal)
    assertNotNull(snapshot.proposal)
    assertFalse(snapshot.isValid)
    assertTrue(snapshot.errors.any { it.path.endsWith("/colour") }, snapshot.errors.toString())
  }

  @Test
  fun `a broken DESIGN dot md is reported and the proposal still loads`() = runBlocking {
    val ws = Fixtures.workspace()
    Files.writeString(ws.designMd, "---\ncolors: [oops\n---\n")
    val snapshot = repository.load(ws.proposal)
    assertNotNull(snapshot.proposal)
    assertTrue(snapshot.errors.any { it.path == "/designMd" })
  }

  private suspend fun kotlinx.coroutines.CoroutineScope.collect(
    ws: Fixtures.Workspace,
    count: Int,
  ) = async(Dispatchers.Default) { repository.watch(ws.proposal).take(count).toList() }

  @Test
  fun `watch emits the current state first`() = runBlocking {
    val ws = Fixtures.workspace()
    val first = withTimeout(5.seconds) { collect(ws, 1).await() }.single()
    assertTrue(first.isValid)
  }

  @Test
  fun `watch emits again when the proposal changes`() = runBlocking {
    val ws = Fixtures.workspace()
    val collected = collect(ws, 2)
    delay(100.milliseconds)
    Files.writeString(
      ws.proposal,
      Fixtures.text(Fixtures.PLATES).replace("Tonight's plate", "Tomorrow's plate"),
    )
    val snapshots = withTimeout(5.seconds) { collected.await() }
    assertEquals("Tonight's plate", snapshots[0].proposal?.name)
    assertEquals("Tomorrow's plate", snapshots[1].proposal?.name)
  }

  @Test
  fun `watch emits again when the DESIGN dot md changes`() = runBlocking {
    val ws = Fixtures.workspace()
    val collected = collect(ws, 2)
    delay(100.milliseconds)
    Files.writeString(ws.designMd, Fixtures.text(Fixtures.DESIGN_MD).replace("28px", "32px"))
    val snapshots = withTimeout(5.seconds) { collected.await() }
    assertEquals(28f, snapshots[0].designSystem.rounded.getValue(ShapeToken.ExtraLarge))
    assertEquals(32f, snapshots[1].designSystem.rounded.getValue(ShapeToken.ExtraLarge))
  }

  @Test
  fun `watch notices a DESIGN dot md that appears nearer the proposal`() = runBlocking {
    val ws = Fixtures.workspace()
    val collected = collect(ws, 2)
    delay(100.milliseconds)
    Files.writeString(
      ws.proposal.parent.resolve("DESIGN.md"),
      Fixtures.text(Fixtures.DESIGN_MD).replace("name: Tonight's plate", "name: Nearer"),
    )
    val snapshots = withTimeout(5.seconds) { collected.await() }
    assertEquals("Nearer", snapshots[1].designSystem.name)
  }

  @Test
  fun `a burst of writes is debounced into one emission`() = runBlocking {
    val ws = Fixtures.workspace()
    val collected = collect(ws, 3)
    delay(100.milliseconds)
    repeat(5) { i ->
      Files.writeString(
        ws.proposal,
        Fixtures.text(Fixtures.PLATES).replace("Tonight's plate", "Draft $i"),
      )
      delay(5.milliseconds)
    }
    delay(400.milliseconds)
    Files.writeString(
      ws.proposal,
      Fixtures.text(Fixtures.PLATES).replace("Tonight's plate", "Final"),
    )
    val snapshots: List<ProposalSnapshot> = withTimeout(5.seconds) { collected.await() }
    assertEquals("Final", snapshots.last().proposal?.name)
    assertEquals(3, snapshots.size)
  }

  @Test
  fun `watch survives the file disappearing and returning`() = runBlocking {
    val ws = Fixtures.workspace()
    val collected = collect(ws, 3)
    delay(100.milliseconds)
    Files.delete(ws.proposal)
    delay(300.milliseconds)
    Files.writeString(ws.proposal, Fixtures.text(Fixtures.PLATES))
    val snapshots = withTimeout(5.seconds) { collected.await() }
    assertNull(snapshots[1].proposal)
    assertTrue(snapshots[2].isValid)
  }
}
