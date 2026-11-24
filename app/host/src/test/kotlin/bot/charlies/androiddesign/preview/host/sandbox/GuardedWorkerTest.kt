package bot.charlies.androiddesign.preview.host.sandbox

import java.io.DataOutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GuardedWorkerTest {
  private val worker = Executors.newSingleThreadExecutor()
  private val watchdog = Executors.newSingleThreadScheduledExecutor()

  @After
  fun tearDown() {
    worker.shutdownNow()
    watchdog.shutdownNow()
  }

  private class RecordingSink : ReplySink {
    val sent = mutableListOf<String>()
    val expired = mutableListOf<String>()
    val expiredLatch = CountDownLatch(1)

    override fun send(reply: OpReply) {
      sent += (reply as NamedReply).label
    }

    override fun expired(reply: OpReply) {
      expired += (reply as NamedReply).label
      expiredLatch.countDown()
    }
  }

  private class NamedReply(val label: String) : OpReply {
    override fun write(output: DataOutputStream) = Unit
  }

  @Test
  fun `a finished op replies once and leaves the worker idle`() {
    val guarded = GuardedWorker(worker, watchdog)
    val sink = RecordingSink()
    guarded.run("PING_LIKE", 5_000, NamedReply("timeout"), sink) { NamedReply("done") }
    assertEquals(listOf("done"), sink.sent)
    assertTrue(sink.expired.isEmpty())
    assertNull(guarded.inFlight())
  }

  @Test
  fun `a hung op such as a DEX static initializer is reported in flight and expires`() {
    val guarded = GuardedWorker(worker, watchdog)
    val sink = RecordingSink()
    val release = CountDownLatch(1)
    val started = CountDownLatch(1)
    val runner = Thread {
      guarded.run("PRELOAD_DEX", 150, NamedReply("timeout"), sink) {
        started.countDown()
        release.await(10, TimeUnit.SECONDS)
        NamedReply("late")
      }
    }
    runner.start()
    assertTrue(started.await(2, TimeUnit.SECONDS))
    val snapshot = guarded.inFlight()
    assertNotNull(snapshot)
    assertEquals("PRELOAD_DEX", snapshot!!.name)
    assertEquals(150L, snapshot.budgetMs)

    assertTrue("watchdog should fire", sink.expiredLatch.await(2, TimeUnit.SECONDS))
    assertEquals(listOf("timeout"), sink.expired)

    release.countDown()
    runner.join(2_000)
    assertTrue("a late reply must be dropped after expiry", sink.sent.isEmpty())
    assertNull(guarded.inFlight())
  }

  @Test
  fun `an op waiting behind a hung one does not burn its own budget in the queue`() {
    val guarded = GuardedWorker(worker, watchdog)
    val sink = RecordingSink()
    val gate = CountDownLatch(1)
    val first = Thread {
      guarded.run("A", 60_000, NamedReply("a-timeout"), sink) {
        gate.await()
        NamedReply("a")
      }
    }
    first.start()
    Thread.sleep(50)
    val second = Thread { guarded.run("B", 100, NamedReply("b-timeout"), sink) { NamedReply("b") } }
    second.start()
    Thread.sleep(300)
    assertFalse(sink.expired.contains("b-timeout"))
    gate.countDown()
    first.join(2_000)
    second.join(2_000)
    assertEquals(listOf("a", "b"), sink.sent)
  }

  @Test
  fun `a throwing op rethrows to the caller and clears the in-flight record`() {
    val guarded = GuardedWorker(worker, watchdog)
    val sink = RecordingSink()
    val failure = AtomicReference<Throwable?>()
    try {
      guarded.run("BOOM", 5_000, NamedReply("timeout"), sink) { error("boom") }
    } catch (t: Throwable) {
      failure.set(t)
    }
    assertEquals("boom", failure.get()?.message)
    assertNull(guarded.inFlight())
    assertTrue(sink.sent.isEmpty())
  }
}
