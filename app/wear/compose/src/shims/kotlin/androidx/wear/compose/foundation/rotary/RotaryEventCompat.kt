package androidx.wear.compose.foundation.rotary

import androidx.compose.ui.input.rotary.RotaryScrollEvent

/** Android reports which device produced a rotary event; desktop events carry no device. */
val RotaryScrollEvent.inputDeviceId: Int
  get() = 0
