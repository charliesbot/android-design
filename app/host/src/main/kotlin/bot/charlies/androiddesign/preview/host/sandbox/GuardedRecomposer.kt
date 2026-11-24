package bot.charlies.androiddesign.preview.host.sandbox

import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.AndroidUiDispatcher
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * A Recomposer whose failures are reported to [onFailure] instead of crashing `:sandbox`. User
 * composables run inside it, so an exception thrown during composition or an effect would otherwise
 * reach the default handler and kill the process. The recomposer is cancelled after the first
 * failure (reported once); callers decide how to recover.
 *
 * [frameClock] replaces the Choreographer clock when a caller drives frames itself.
 */
internal class GuardedRecomposer(
  frameClock: CoroutineContext = EmptyCoroutineContext,
  onFailure: (Throwable) -> Unit,
) {
  private val failed = AtomicBoolean(false)
  private val reportOnce: (Throwable) -> Unit = { t ->
    if (failed.compareAndSet(false, true)) onFailure(t)
  }
  val recomposer: Recomposer
  private val job: Job

  init {
    lateinit var recomp: Recomposer
    val handler = CoroutineExceptionHandler { _, t ->
      if (t !is CancellationException) {
        reportOnce(t)
        recomp.cancel()
      }
    }
    val context = AndroidUiDispatcher.Main + frameClock + handler
    recomp = Recomposer(context)
    recomposer = recomp
    job =
      CoroutineScope(context).launch {
        try {
          recomp.runRecomposeAndApplyChanges()
        } catch (t: Throwable) {
          if (t is CancellationException) throw t
          reportOnce(t)
        }
      }
  }

  fun cancel() {
    recomposer.cancel()
    job.cancel()
  }
}
