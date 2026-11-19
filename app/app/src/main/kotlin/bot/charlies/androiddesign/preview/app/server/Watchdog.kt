package bot.charlies.androiddesign.preview.app.server

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Daemon watchdog that bounds every server compile and 1-view render against its deadline (30 s
 * cold first compile, 5 s warm compile and 1-view render). When a job passes its budget, the
 * watchdog invokes [onTimeout] (to record `(contentHash, scope)` in the server record and notify
 * browsers) and then invokes [onHalt] (`Runtime.getRuntime().halt(1)` in production).
 */
class Watchdog(
  private val coldBudgetMs: Long = COLD_BUDGET_MS,
  private val warmBudgetMs: Long = WARM_BUDGET_MS,
  private val pollIntervalMs: Long = 100L,
  private val onTimeout: (ActiveJob) -> Unit,
  private val onHalt: () -> Unit = { Runtime.getRuntime().halt(1) },
) {
  data class ActiveJob(
    val contentHash: String,
    val scope: String,
    val budgetMs: Long,
    val startedAtMs: Long,
  )

  private val active = AtomicReference<ActiveJob?>(null)
  private val running = AtomicBoolean(false)
  private val halted = AtomicBoolean(false)
  @Volatile private var thread: Thread? = null

  fun start() {
    if (!running.compareAndSet(false, true)) return
    val t =
      Thread(
        {
          while (running.get()) {
            val current = active.get()
            if (current != null && !halted.get()) {
              val elapsed = System.currentTimeMillis() - current.startedAtMs
              if (elapsed > current.budgetMs && halted.compareAndSet(false, true)) {
                try {
                  onTimeout(current)
                } finally {
                  onHalt()
                }
                break
              }
            }
            try {
              Thread.sleep(pollIntervalMs)
            } catch (_: InterruptedException) {
              break
            }
          }
        },
        "android-design-watchdog",
      )
    t.isDaemon = true
    thread = t
    t.start()
  }

  fun stop() {
    running.set(false)
    thread?.interrupt()
    thread = null
  }

  fun begin(contentHash: String, scope: String, isCold: Boolean): ActiveJob {
    val budget = if (isCold) coldBudgetMs else warmBudgetMs
    val job = ActiveJob(contentHash, scope, budget, System.currentTimeMillis())
    active.set(job)
    return job
  }

  fun end(job: ActiveJob) {
    active.compareAndSet(job, null)
  }

  inline fun <T> guard(contentHash: String, scope: String, isCold: Boolean, block: () -> T): T {
    val token = begin(contentHash, scope, isCold)
    return try {
      block()
    } finally {
      end(token)
    }
  }

  companion object {
    const val COLD_BUDGET_MS = 30_000L
    const val WARM_BUDGET_MS = 5_000L
  }
}
