package bot.charlies.androiddesign.preview.app.server

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class FileWatcherTest {
  @Test
  fun `detects source file save within 300 ms`(@TempDir tempDir: Path) {
    val source = tempDir.resolve("proposal.explore.kt")
    Files.writeString(source, "val v = 1\n")

    val detectedAtNs = AtomicLong(0L)
    val latch = CountDownLatch(1)
    val watcher =
      FileWatcher(debounceMs = 80L) { _, _ ->
        detectedAtNs.compareAndSet(0L, System.nanoTime())
        latch.countDown()
      }
    try {
      watcher.watch(source, null)
      // Allow initial baseline capture to settle before writing the edit.
      Thread.sleep(150L)

      val writeAtNs = System.nanoTime()
      Files.writeString(source, "val v = 2 // longer edit\n")

      val fired = latch.await(3_000L, TimeUnit.MILLISECONDS)
      assertTrue(fired, "Expected FileWatcher callback to fire after saving $source")
      val elapsedMs = (detectedAtNs.get() - writeAtNs) / 1_000_000L
      println("FileWatcher save-to-callback latency: ${elapsedMs}ms")
      assertTrue(
        elapsedMs <= 300L,
        "Expected save detection within 300 ms (including 80 ms debounce), got ${elapsedMs} ms",
      )
    } finally {
      watcher.stop()
    }
  }

  @Test
  fun `debounces rapid saves into one callback`(@TempDir tempDir: Path) {
    val source = tempDir.resolve("proposal.explore.kt")
    Files.writeString(source, "val v = 0\n")

    val calls = AtomicInteger(0)
    val firstDone = CountDownLatch(1)
    val watcher =
      FileWatcher(debounceMs = 80L) { _, _ ->
        calls.incrementAndGet()
        firstDone.countDown()
      }
    try {
      watcher.watch(source, null)
      Thread.sleep(150L)

      Files.writeString(source, "val v = 1\n")
      Thread.sleep(25L)
      Files.writeString(source, "val v = 22\n")
      Thread.sleep(25L)
      Files.writeString(source, "val v = 333\n")

      assertTrue(firstDone.await(1_000L, TimeUnit.MILLISECONDS))
      Thread.sleep(250L)
      assertEquals(
        1,
        calls.get(),
        "Rapid saves within debounce window should coalesce into one callback",
      )
    } finally {
      watcher.stop()
    }
  }

  @Test
  fun `detects DESIGN md change and atomic rename replace and stops cleanly`(
    @TempDir tempDir: Path,
  ) {
    val source = tempDir.resolve("proposal.explore.kt")
    val design = tempDir.resolve("DESIGN.md")
    Files.writeString(source, "val v = 1\n")
    Files.writeString(design, "---\ncolors:\n  primary: \"#336699\"\n---\n")

    val callbacks = AtomicInteger(0)
    val designLatch = CountDownLatch(1)
    val renameLatch = CountDownLatch(1)
    val watcher =
      FileWatcher(debounceMs = 80L) { _, _ ->
        val count = callbacks.incrementAndGet()
        if (count == 1) designLatch.countDown()
        if (count == 2) renameLatch.countDown()
      }
    try {
      watcher.watch(source, design)
      Thread.sleep(150L)

      val designWriteNs = System.nanoTime()
      Files.writeString(design, "---\ncolors:\n  primary: \"#993366\"\n---\n")
      assertTrue(designLatch.await(1_000L, TimeUnit.MILLISECONDS))
      val designElapsedMs = (System.nanoTime() - designWriteNs) / 1_000_000L
      assertTrue(
        designElapsedMs <= 300L,
        "Expected DESIGN.md detection <= 300 ms, got ${designElapsedMs} ms",
      )

      // Atomic replace (temp file moved over watched file)
      val tmp = tempDir.resolve("proposal.explore.kt.tmp")
      Files.writeString(tmp, "val v = 999\n")
      Files.move(tmp, source, StandardCopyOption.REPLACE_EXISTING)
      assertTrue(renameLatch.await(1_000L, TimeUnit.MILLISECONDS))

      watcher.stop()
      val countAfterStop = callbacks.get()
      Files.writeString(source, "val v = 1000\n")
      Thread.sleep(250L)
      assertEquals(countAfterStop, callbacks.get(), "Stopped watcher must not fire callbacks")
    } finally {
      watcher.stop()
    }
  }
}
