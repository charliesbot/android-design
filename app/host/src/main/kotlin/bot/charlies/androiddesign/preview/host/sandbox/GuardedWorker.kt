package bot.charlies.androiddesign.preview.host.sandbox

import java.io.DataOutputStream
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/** A response a worker op produces; written to the client under that client's write lock. */
internal fun interface OpReply {
  fun write(output: DataOutputStream)
}

/** Where a client's replies go. [expired] must flush the reply, close the client, and kill. */
internal interface ReplySink {
  fun send(reply: OpReply)

  fun expired(reply: OpReply)
}

internal class InFlightOp(val name: String, val startedAtMs: Long, val budgetMs: Long)

/**
 * Runs every op on the single worker thread behind one watchdog. User DEX code executes on that
 * thread (top-level initializers, composition), so any op can hang; a hung op must kill the whole
 * process rather than leave later ops queued behind it. [inFlight] feeds PING so the host can see a
 * stuck worker even if this watchdog itself fails.
 */
internal class GuardedWorker(
  private val worker: ExecutorService,
  private val watchdog: ScheduledExecutorService,
  private val nowMs: () -> Long = { System.nanoTime() / 1_000_000L },
) {
  @Volatile private var current: InFlightOp? = null

  fun inFlight(): InFlightOp? = current

  fun ageMs(op: InFlightOp): Long = (nowMs() - op.startedAtMs).coerceAtLeast(0L)

  /**
   * Blocks until [block] finishes on the worker, then sends its reply unless the watchdog already
   * answered with [timeoutReply]. The budget starts when the op reaches the front of the queue.
   */
  fun run(
    name: String,
    budgetMs: Long,
    timeoutReply: OpReply,
    sink: ReplySink,
    block: () -> OpReply,
  ) {
    val future = worker.submit {
      val responded = AtomicBoolean(false)
      current = InFlightOp(name, nowMs(), budgetMs)
      val timeout =
        watchdog.schedule(
          {
            if (responded.compareAndSet(false, true)) {
              sink.expired(timeoutReply)
            }
          },
          budgetMs,
          TimeUnit.MILLISECONDS,
        )
      try {
        val reply = block()
        if (responded.compareAndSet(false, true)) sink.send(reply)
      } finally {
        timeout.cancel(false)
        current = null
      }
    }
    try {
      future.get()
    } catch (e: ExecutionException) {
      throw e.cause ?: e
    }
  }
}
