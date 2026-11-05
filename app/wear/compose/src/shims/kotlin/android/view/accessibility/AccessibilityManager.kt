package android.view.accessibility

class AccessibilityManager {
  val isEnabled: Boolean = false
  val isTouchExplorationEnabled: Boolean = false

  fun interface AccessibilityStateChangeListener {
    fun onAccessibilityStateChanged(enabled: Boolean)
  }

  fun interface TouchExplorationStateChangeListener {
    fun onTouchExplorationStateChanged(enabled: Boolean)
  }

  fun addTouchExplorationStateChangeListener(l: TouchExplorationStateChangeListener) {}

  fun removeTouchExplorationStateChangeListener(l: TouchExplorationStateChangeListener) {}

  fun addAccessibilityStateChangeListener(l: AccessibilityStateChangeListener) {}

  fun removeAccessibilityStateChangeListener(l: AccessibilityStateChangeListener) {}
}
