package bot.charlies.androiddesign.preview.app.preview

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.data.FileProposalRepository
import bot.charlies.androiddesign.preview.feature.preview.viewer.ViewerViewModel
import bot.charlies.androiddesign.preview.model.Views
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.measureNanoTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Tag

/**
 * Reports how long the live window takes to adopt a saved change to the verify proposal, the
 * largest proposal in the repository, and how long `check_proposal` takes right after a change. It
 * prints numbers and asserts none, because timing assertions are flaky. Run it with `./gradlew
 * :app:saveToAdopt`; the plain test task skips it.
 *
 * The first save is cold (the JVM has rendered nothing yet). The rest are warm. A hidden packaged
 * app is slowed about fivefold by macOS App Nap after ~30 s idle; this run is not.
 */
@Tag("benchmark")
class SaveToAdoptBenchmark {
  private val repo = Path.of(System.getProperty("repo.dir"))

  @Test
  fun `report the time from a saved change to the window adopting it`() = runBlocking {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val viewer = ViewerViewModel()
    val controller =
      AppPreviewController(
        repository = FileProposalRepository(),
        viewer = viewer,
        window = PreviewWindow(),
        renderer = HeadlessProposalRenderer(),
        scope = scope,
        version = AppInfo.VERSION,
      )
    val dir = Files.createTempDirectory("save-to-adopt")
    val file = dir.resolve("verify.proposal.json")
    val original = Files.readString(repo.resolve("evals/verify/verify.proposal.json"))
    Files.writeString(file, original)

    val openMs = millis { controller.openProposal(file.toString()) }
    val saves =
      (1..SAVES).map { n ->
        val name = "Verify $n"
        val written = original.replaceFirst("\"name\": \"Verify\"", "\"name\": \"$name\"")
        millis {
          Files.writeString(file, written)
          withTimeout(30_000) {
            while (viewer.state.value.name != name) delay(1)
          }
        }
      }
    val trees = (1..TREES).map { millis { controller.getTree() } }
    val checks =
      (1..CHECKS).map { n ->
        Files.writeString(
          file,
          original.replaceFirst("\"name\": \"Verify\"", "\"name\": \"Check $n\""),
        )
        millis { runBlocking { controller.checkProposal() } }
      }
    scope.cancel()

    println("SAVE_TO_ADOPT open_proposal (cold): ${fmt(openMs)} ms")
    println("SAVE_TO_ADOPT first save (cold):    ${fmt(saves.first())} ms")
    val warm = saves.drop(1).sorted()
    println(
      "SAVE_TO_ADOPT warm save, ${warm.size} runs: median ${fmt(median(warm))} ms, " +
        "min ${fmt(warm.first())} ms, max ${fmt(warm.last())} ms",
    )
    val warmTrees = trees.sorted()
    println(
      "SAVE_TO_ADOPT warm get_tree, ${warmTrees.size} runs: median ${fmt(median(warmTrees))} ms, " +
        "min ${fmt(warmTrees.first())} ms, max ${fmt(warmTrees.last())} ms",
    )
    val warmChecks = checks.drop(1).sorted()
    println(
      "SAVE_TO_ADOPT check_proposal after an edit, ${warmChecks.size} warm runs of " +
        "${Views.all.size} views each: median ${fmt(median(warmChecks))} ms, " +
        "min ${fmt(warmChecks.first())} ms, max ${fmt(warmChecks.last())} ms",
    )
    assertEquals(SAVES, saves.size)
  }

  private inline fun millis(block: () -> Unit): Double = measureNanoTime(block) / 1_000_000.0

  private fun median(sorted: List<Double>) = sorted[sorted.size / 2]

  private fun fmt(ms: Double) = "%.0f".format(ms)

  private companion object {
    const val SAVES = 11
    const val TREES = 5
    const val CHECKS = 6
  }
}
