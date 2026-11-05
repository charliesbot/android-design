package android.view

open class View(open val context: android.content.Context = android.content.Context()) {
  /** The dialog window shim, which is all upstream ever casts a parent to. */
  open val parent: Any? = androidx.compose.ui.window.DesktopDialogWindowProvider
  open val isAttachedToWindow: Boolean = true
  open val rootView: View? = null
  open val rootWindowInsets: WindowInsets? = null
  open val windowInsetsController: WindowInsetsController? = null
  open val resources: android.content.res.Resources
    get() = context.resources

  open var contentDescription: CharSequence? = null
  open var alpha: Float = 1f
  open var visibility: Int = VISIBLE
  open var importantForAccessibility: Int = 0
  open var accessibilityLiveRegion: Int = 0
  open var layoutParams: ViewGroup.LayoutParams? = null

  fun addOnAttachStateChangeListener(l: OnAttachStateChangeListener) {}

  fun removeOnAttachStateChangeListener(l: OnAttachStateChangeListener) {}

  fun addOnLayoutChangeListener(l: OnLayoutChangeListener) {}

  fun removeOnLayoutChangeListener(l: OnLayoutChangeListener) {}

  interface OnAttachStateChangeListener {
    fun onViewAttachedToWindow(v: View)

    fun onViewDetachedFromWindow(v: View)
  }

  fun interface OnLayoutChangeListener {
    fun onLayoutChange(
      v: View,
      left: Int,
      top: Int,
      right: Int,
      bottom: Int,
      oldLeft: Int,
      oldTop: Int,
      oldRight: Int,
      oldBottom: Int,
    )
  }

  companion object {
    const val VISIBLE = 0
    const val IMPORTANT_FOR_ACCESSIBILITY_YES = 1
    const val ACCESSIBILITY_LIVE_REGION_POLITE = 1
  }
}

open class ViewGroup(context: android.content.Context = android.content.Context()) : View(context) {
  open class LayoutParams(width: Int, height: Int)

  fun addView(child: View, params: LayoutParams? = null) {}

  fun removeView(child: View) {}
}

class WindowInsets {
  fun isVisible(type: Int): Boolean = false

  object Type {
    fun statusBars(): Int = 1
  }
}

class WindowInsetsController {
  fun show(types: Int) {}

  fun hide(types: Int) {}
}

class ViewConfiguration {
  val scaledMinimumFlingVelocity: Int = 50
  val scaledMaximumFlingVelocity: Int = 8000

  fun getScaledMinimumFlingVelocity(inputDeviceId: Int, axis: Int, source: Int): Int = 50

  fun getScaledMaximumFlingVelocity(inputDeviceId: Int, axis: Int, source: Int): Int = 8000

  companion object {
    fun get(context: android.content.Context): ViewConfiguration = ViewConfiguration()
  }
}

object MotionEvent {
  const val AXIS_SCROLL = 26
}

object InputDevice {
  const val SOURCE_ROTARY_ENCODER = 0x00400000
}

object Surface {
  const val ROTATION_0 = 0
  const val ROTATION_180 = 2
}
