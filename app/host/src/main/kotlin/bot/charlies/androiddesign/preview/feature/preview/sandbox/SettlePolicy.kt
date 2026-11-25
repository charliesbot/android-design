package bot.charlies.androiddesign.preview.feature.preview.sandbox

import kotlinx.coroutines.delay

/** What one virtual frame left behind, as reported by a [FrameDriver]. */
data class FrameStatus(
  /** The frame clock has awaiters or the recomposer still has work queued. */
  val workPending: Boolean,
  /** At least one popup, dialog, or sheet window exists besides the main activity window. */
  val overlayPresent: Boolean,
  /** An overlay window exists but is not attached or measured yet. */
  val awaitingOverlayAttach: Boolean,
)

/** The renderer-specific half of settling: how one virtual frame is produced and laid out. */
interface FrameDriver {
  /**
   * Sends one [SettlePolicy.FRAME_NS] virtual frame, lays windows out, and reports what remains.
   */
  suspend fun advanceFrame(): FrameStatus

  /** Runs once after the last frame so the capture sees a fully laid-out tree. */
  suspend fun finishSettle()
}

/** A [FrameDriver] whose composition hosts a vertical Wear scroller. */
interface WearScrollDriver : FrameDriver {
  /** Scrolls the Wear list by half a viewport; false when it cannot move any further. */
  suspend fun scrollOnce(): Boolean
}

/** Canonical frame-settling policy for `:sandbox` offscreen capture and render tests. */
object SettlePolicy {
  const val FRAME_NS = 16_666_667L
  const val MAX_SETTLE_FRAMES = 120
  const val MIN_STEPS = 4
  const val MIN_OVERLAY_STEPS = 45
  const val IDLE_STREAK = 3
  const val MAX_ATTACH_YIELDS = 6
  const val ATTACH_YIELD_MS = 16L
  const val SCROLL_SETTLE_FRAMES = 32
  const val MAX_SCROLL_STEPS = 60

  /** Advances frames until the composition is idle [IDLE_STREAK] frames in a row or [maxFrames]. */
  suspend fun settle(driver: FrameDriver, maxFrames: Int = MAX_SETTLE_FRAMES) {
    var attachYields = 0
    var hadOverlay = false
    var idleStreak = 0
    for (step in 0 until maxFrames) {
      val status = driver.advanceFrame()
      if (status.overlayPresent) hadOverlay = true
      if (step >= 1 && status.awaitingOverlayAttach && attachYields < MAX_ATTACH_YIELDS) {
        attachYields++
        delay(ATTACH_YIELD_MS)
      }
      val minSteps = if (hadOverlay) MIN_OVERLAY_STEPS else MIN_STEPS
      if (!status.workPending && !status.awaitingOverlayAttach && step >= minSteps) {
        idleStreak++
        if (idleStreak >= IDLE_STREAK) break
      } else {
        idleStreak = 0
      }
    }
    driver.finishSettle()
  }

  /**
   * Scrolls a Wear list to its end, settling and calling [capture] after every step. Returns the
   * captures in scroll order; empty when the list never moved.
   */
  suspend fun <T> scrollThrough(driver: WearScrollDriver, capture: suspend () -> T): List<T> {
    val captures = mutableListOf<T>()
    var steps = 0
    while (steps++ < MAX_SCROLL_STEPS) {
      if (!driver.scrollOnce()) break
      settle(driver, SCROLL_SETTLE_FRAMES)
      captures += capture()
    }
    return captures
  }
}
