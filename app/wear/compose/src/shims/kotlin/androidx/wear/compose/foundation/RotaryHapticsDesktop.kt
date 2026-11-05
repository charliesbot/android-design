package androidx.wear.compose.foundation.rotary

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow

// DESKTOP REPLACEMENT of upstream rotary/Haptics.kt: a desktop has no rotary haptics, so only the
// no-op handler and the helpers other upstream files use are kept.

/** Handles haptics for rotary usage */
internal interface RotaryHapticHandler {
  fun handleScrollHaptic(timestamp: Long, deltaInPixels: Float, inputDeviceId: Int, axis: Int)

  fun handleSnapHaptic(timestamp: Long, deltaInPixels: Float, inputDeviceId: Int, axis: Int)

  fun handleLimitHaptic(isStart: Boolean, inputDeviceId: Int, axis: Int)
}

@Composable
@Suppress("UNUSED_PARAMETER")
internal fun rememberRotaryHapticHandler(
  scrollableState: ScrollableState,
  hapticsEnabled: Boolean,
): RotaryHapticHandler = remember { NoRotaryHaptics }

private object NoRotaryHaptics : RotaryHapticHandler {
  override fun handleScrollHaptic(
    timestamp: Long,
    deltaInPixels: Float,
    inputDeviceId: Int,
    axis: Int,
  ) {}

  override fun handleSnapHaptic(
    timestamp: Long,
    deltaInPixels: Float,
    inputDeviceId: Int,
    axis: Int,
  ) {}

  override fun handleLimitHaptic(isStart: Boolean, inputDeviceId: Int, axis: Int) {}
}

internal fun <T> Flow<T>.throttleLatest(timeframe: Long): Flow<T> = flow {
  conflate().collect {
    emit(it)
    delay(timeframe)
  }
}
