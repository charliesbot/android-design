package android.view

/**
 * Android window attributes upstream's Dialog sets; a Compose Desktop dialog has no such window.
 */
class Window {
  fun setWindowAnimations(resId: Int) {}

  fun setDimAmount(amount: Float) {}
}
