package androidx.wear.compose.material3

import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy

// Same package as upstream's Dialog.kt, so it reads these Android-only properties without an
// import.
internal val DialogProperties.securePolicy: SecureFlagPolicy
  get() = SecureFlagPolicy.Inherit

internal val DialogProperties.windowTitle: String
  get() = ""
