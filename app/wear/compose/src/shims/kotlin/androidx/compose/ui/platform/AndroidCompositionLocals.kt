package androidx.compose.ui.platform

import android.content.Context
import android.content.res.Configuration
import android.view.View
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

val LocalContext = staticCompositionLocalOf<Context> { Context() }
val LocalConfiguration =
  compositionLocalOf<Configuration> {
    error("Wear content must be composed inside WearDevice")
  }
val LocalView = staticCompositionLocalOf<View> { View() }
