package androidx.compose.foundation.layout

import androidx.compose.runtime.Composable

/** Desktop has no status bar. */
val WindowInsets.Companion.statusBarsIgnoringVisibility: WindowInsets
  @Composable get() = WindowInsets(0, 0, 0, 0)
