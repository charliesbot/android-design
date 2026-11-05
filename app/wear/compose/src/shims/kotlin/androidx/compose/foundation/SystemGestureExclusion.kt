package androidx.compose.foundation

import androidx.compose.ui.Modifier

/** Desktop has no system back gesture to exclude from. */
fun Modifier.systemGestureExclusion(): Modifier = this
