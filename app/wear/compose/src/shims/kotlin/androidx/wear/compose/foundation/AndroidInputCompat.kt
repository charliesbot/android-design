package androidx.wear.compose.foundation

/** `KeyEvent.nativeKeyEvent.device` on Android. Desktop key events come from no named device. */
class InputDeviceStub(val name: String?)

val Any.device: InputDeviceStub?
  get() = null
