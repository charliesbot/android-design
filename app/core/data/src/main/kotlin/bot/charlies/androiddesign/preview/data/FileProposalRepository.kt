package bot.charlies.androiddesign.preview.data

import bot.charlies.androiddesign.preview.domain.ProposalRepository
import bot.charlies.androiddesign.preview.domain.ProposalSnapshot
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Reads proposals from disk and watches them.
 *
 * Watching polls file stamps instead of using `WatchService`: on macOS the JDK implements that by
 * polling every few seconds, which is far too slow for a live preview. A stat of the proposal and
 * of each candidate DESIGN.md every [poll] is cheap. After a change it waits until the files stay
 * unchanged for [debounce], so a burst of writes yields one snapshot.
 */
class FileProposalRepository(
  private val reader: ProposalReader = ProposalReader(),
  private val poll: Duration = 20.milliseconds,
  private val debounce: Duration = 50.milliseconds,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ProposalRepository {

  override suspend fun load(path: Path): ProposalSnapshot =
    withContext(dispatcher) { reader.read(path) }

  override fun watch(path: Path): Flow<ProposalSnapshot> = flow {
    val file = path.toAbsolutePath().normalize()
    var seen = stamps(file)
    emit(reader.read(file))
    while (true) {
      delay(poll)
      val now = stamps(file)
      if (now == seen) continue
      var settled = now
      while (true) {
        delay(debounce)
        val again = stamps(file)
        if (again == settled) break
        settled = again
      }
      seen = settled
      emit(reader.read(file))
    }
  }
    .flowOn(dispatcher)

  /** What a change to the proposal or to the DESIGN.md that applies would alter. */
  private fun stamps(file: Path): List<Stamp?> {
    val watched = buildList {
      add(file)
      for (candidate in DesignMdLocator.candidates(file)) {
        add(candidate)
        if (Files.isRegularFile(candidate)) break
      }
    }
    return watched.map(::stamp)
  }

  private fun stamp(path: Path): Stamp? =
    try {
      val attributes = Files.readAttributes(path, BasicFileAttributes::class.java)
      Stamp(path, attributes.lastModifiedTime(), attributes.size())
    } catch (_: IOException) {
      null
    }

  private data class Stamp(val path: Path, val modified: FileTime, val size: Long)
}
