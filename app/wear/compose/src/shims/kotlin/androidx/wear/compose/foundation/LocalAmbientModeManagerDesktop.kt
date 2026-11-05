/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.wear.compose.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.isActive

/**
 * A [androidx.compose.runtime.CompositionLocal] that provides the current [AmbientModeManager]
 * interface.
 *
 * This local is the primary way for any composable in the hierarchy to access the manager
 * responsible for tracking the ambient (low-power) mode state.
 *
 * Composables read the manager using [LocalAmbientModeManager].current and subscribe to changes via
 * the [AmbientModeManager.currentAmbientMode] property.
 *
 * If no [AmbientModeManager] is explicitly provided higher up the tree (e.g., via
 * [rememberAmbientModeManager]), this local returns `null`.
 *
 * @sample androidx.wear.compose.foundation.samples.AmbientModeBasicSample
 */
public val LocalAmbientModeManager: ProvidableCompositionLocal<AmbientModeManager?> =
  staticCompositionLocalOf {
    null
  }

/**
 * Creates, remembers, and manages the lifecycle of the default [AmbientModeManager] implementation.
 *
 * **This function requires a [LocalActivity] be present and automatically enables Always-On mode
 * for that activity, ensuring it remains visible in the low-power ambient state.**
 *
 * The resulting [AmbientModeManager] is retained across recompositions via [remember] and its
 * system listeners are automatically registered and unregistered using [DisposableEffect], tying
 * the ambient tracking to the composition lifecycle.
 *
 * See the Android documentation for more details on enabling Always-On mode:
 * [https://developer.android.com/training/wearables/views/always-on]
 *
 * Example of a simple screen switching between Interactive and Ambient modes:
 *
 * @sample androidx.wear.compose.foundation.samples.AmbientModeBasicSample
 * @return A new or remembered [AmbientModeManager] instance.
 */
@Composable
public fun rememberAmbientModeManager(): AmbientModeManager {
  // DESKTOP: there is no ambient mode, so the display is always interactive.
  return remember { InteractiveOnlyAmbientModeManager }
}

public interface AmbientModeManager {

  /**
   * The current state of the display, which is either [AmbientMode.Interactive] or
   * [AmbientMode.Ambient].
   *
   * This property should be backed by Compose's Snapshot State system. Reading this value inside a
   * composable function should automatically subscribe the composable to state changes and trigger
   * a recomposition when the ambient mode changes.
   */
  public val currentAmbientMode: AmbientMode

  /**
   * Suspends the calling coroutine and waits for the next delivered "ambient tick".
   *
   * When the device is in [AmbientMode.Ambient], the system only provides updates at infrequent
   * intervals (typically once per minute) to preserve battery life. This function allows a
   * coroutine to pause and resume execution specifically when that tick is received.
   *
   * The provided [block] will be executed synchronously when the tick occurs, and any state updates
   * performed within it will be reflected on the ambient screen.
   *
   * This function should typically be used within a looping [LaunchedEffect] that runs only when
   * [currentAmbientMode] is [AmbientMode.Ambient].
   *
   * @param block The state update logic to execute immediately upon receiving the ambient tick.
   * @sample androidx.wear.compose.foundation.samples.AmbientModeWithAmbientTickSample
   */
  public suspend fun withAmbientTick(block: () -> Unit)
}

/**
 * A convenience extension that performs recurrent, battery-efficient UI updates when the device is
 * in [AmbientMode.Ambient].
 *
 * This extension handles the boilerplate for ambient tick synchronization: it automatically
 * launches and manages a [LaunchedEffect] that repeatedly suspends using
 * [AmbientModeManager.withAmbientTick] to align state updates with the system's infrequent ambient
 * tick schedule.
 *
 * The internal loop automatically terminates when the device returns to [AmbientMode.Interactive].
 *
 * **Efficiency Note:** The [block] lambda should only update the minimal
 * [androidx.compose.runtime.State] required to prevent excessive recomposition and maximize battery
 * life.
 *
 * Example of using AmbientTickEffect:
 *
 * @sample androidx.wear.compose.foundation.samples.AmbientModeWithAmbientTickSample
 * @param block The state update logic to execute once per ambient tick.
 */
@Composable
public fun AmbientModeManager.AmbientTickEffect(block: () -> Unit) {
  if (currentAmbientMode is AmbientMode.Ambient) {
    LaunchedEffect(this, block) {
      while (isActive) {
        withAmbientTick(block)
      }
    }
  }
}

internal object InteractiveOnlyAmbientModeManager : AmbientModeManager {
  override val currentAmbientMode: AmbientMode = AmbientMode.Interactive

  override suspend fun withAmbientTick(block: () -> Unit) {
    kotlinx.coroutines.awaitCancellation()
  }
}
