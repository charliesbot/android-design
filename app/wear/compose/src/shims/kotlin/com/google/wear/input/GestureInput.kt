package com.google.wear.input

import android.view.View
import java.util.concurrent.Executor
import java.util.function.Consumer

class GestureEvent {
  companion object {
    const val ACTION_PRIMARY = 1
    const val ACTION_DISMISS = 2
  }
}

class ForegroundGestureSubscriptionParams private constructor() {
  class Builder(actions: IntArray, view: View) {
    fun setAmbientSupported(supported: Boolean): Builder = this

    fun build(): ForegroundGestureSubscriptionParams = ForegroundGestureSubscriptionParams()
  }
}

open class GestureInputManager {
  open fun notifyGestureConsumed(gestureId: String, action: Int) {}

  open fun isActionSupported(action: Int): Boolean = false

  open fun isActionEnabled(action: Int): Boolean = false

  open fun shouldShowHint(gestureId: String, action: Int, flags: Int = 0): Boolean = false

  open fun notifyHintShown(gestureId: String, action: Int) {}

  open fun addGestureEventListener(
    params: ForegroundGestureSubscriptionParams,
    executor: Executor,
    consumer: Consumer<GestureEvent>?,
  ) {}

  open fun addGestureEventListener(
    actions: IntArray,
    view: View,
    executor: Executor,
    consumer: Consumer<GestureEvent>?,
  ) {}

  open fun removeGestureEventListener(consumer: Consumer<GestureEvent>) {}

  companion object {
    const val FLAG_HINT_STYLE_OVERLAY = 1
  }
}
