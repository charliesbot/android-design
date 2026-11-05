package androidx.compose.ui.window

import android.view.Window

/** Upstream casts a dialog's `view.parent` to this to reach the Android window. */
interface DialogWindowProvider {
  val window: Window
}

object DesktopDialogWindowProvider : DialogWindowProvider {
  override val window = Window()
}

enum class SecureFlagPolicy {
  Inherit,
  SecureOn,
  SecureOff,
}

// DialogProperties on Android also carries window flags that have no desktop meaning. This factory
// accepts them and drops them (their getters are in material3/DialogPropertiesCompat.kt).
fun DialogProperties(
  dismissOnBackPress: Boolean = true,
  dismissOnClickOutside: Boolean = true,
  securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit,
  usePlatformDefaultWidth: Boolean = true,
  decorFitsSystemWindows: Boolean = true,
  windowTitle: String = "",
): DialogProperties =
  DialogProperties(
    dismissOnBackPress = dismissOnBackPress,
    dismissOnClickOutside = dismissOnClickOutside,
    usePlatformDefaultWidth = usePlatformDefaultWidth,
  )
