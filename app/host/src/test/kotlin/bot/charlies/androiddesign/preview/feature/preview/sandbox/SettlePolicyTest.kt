package bot.charlies.androiddesign.preview.feature.preview.sandbox

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettlePolicyTest {
  /** A composition whose animation or delay finishes [endsAtNs] of virtual time after the start. */
  private class TimedDriver(private val endsAtNs: Long, private val overlay: Boolean = false) :
    WearScrollDriver {
    var nowNs = 0L
    var frames = 0
    var finished = 0
    var scrollsLeft = 0
    var scrolls = 0

    val settled: Boolean
      get() = nowNs >= endsAtNs

    override suspend fun advanceFrame(): FrameStatus {
      nowNs += SettlePolicy.FRAME_NS
      frames++
      return FrameStatus(
        workPending = nowNs < endsAtNs,
        overlayPresent = overlay,
        awaitingOverlayAttach = false,
      )
    }

    override suspend fun finishSettle() {
      finished++
    }

    override suspend fun scrollOnce(): Boolean {
      if (scrollsLeft == 0) return false
      scrollsLeft--
      scrolls++
      return true
    }
  }

  @Test
  fun animationOfOnePointFiveSecondsSettlesToItsFinalState() {
    val driver = TimedDriver(endsAtNs = 1_500_000_000L)
    runBlocking { SettlePolicy.settle(driver) }
    assertTrue("1.5 s animation must finish within the settle cap", driver.settled)
    assertEquals(1, driver.finished)
  }

  @Test
  fun settleCapIsTwoSecondsOfVirtualTime() {
    val driver = TimedDriver(endsAtNs = Long.MAX_VALUE)
    runBlocking { SettlePolicy.settle(driver) }
    assertEquals(SettlePolicy.MAX_SETTLE_FRAMES, driver.frames)
    assertTrue(driver.nowNs >= 1_990_000_000L)
  }

  @Test
  fun idleCompositionStopsAfterMinStepsAndIdleStreak() {
    val driver = TimedDriver(endsAtNs = 0L)
    runBlocking { SettlePolicy.settle(driver) }
    assertEquals(SettlePolicy.MIN_STEPS + SettlePolicy.IDLE_STREAK, driver.frames)
  }

  @Test
  fun overlaysWaitForTheModalTransitionBeforeStopping() {
    val driver = TimedDriver(endsAtNs = 0L, overlay = true)
    runBlocking { SettlePolicy.settle(driver) }
    assertEquals(SettlePolicy.MIN_OVERLAY_STEPS + SettlePolicy.IDLE_STREAK, driver.frames)
  }

  @Test
  fun scrollThroughCapturesEveryStepUntilTheListEnds() {
    val driver = TimedDriver(endsAtNs = 0L).apply { scrollsLeft = 3 }
    val captures = runBlocking { SettlePolicy.scrollThrough(driver) { driver.scrolls } }
    assertEquals(listOf(1, 2, 3), captures)
  }

  @Test
  fun scrollThroughStopsAtTheStepCap() {
    val driver = TimedDriver(endsAtNs = 0L).apply { scrollsLeft = Int.MAX_VALUE }
    val captures = runBlocking { SettlePolicy.scrollThrough(driver) { driver.scrolls } }
    assertEquals(SettlePolicy.MAX_SCROLL_STEPS, captures.size)
  }
}
