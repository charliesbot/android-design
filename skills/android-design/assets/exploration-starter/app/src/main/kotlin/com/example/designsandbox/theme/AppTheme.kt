package com.example.designsandbox.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme

// Mirrors the HTML runtime's <m3e-theme color variant scheme>: seed = color, style = variant,
// darkTheme = scheme. Wallpaper-based dynamic color is opt-in; otherwise the scheme is generated
// from the seed (Material 3 Expressive, needs material3 1.5.0-alpha or later).
@Composable
fun AppTheme(
  seed: Color = Color(0xFF6750A4),
  style: PaletteStyle = PaletteStyle.TonalSpot,
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamic: Boolean = false,
  content: @Composable () -> Unit,
) {
  val context = LocalContext.current
  val colorScheme =
    if (dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
      rememberDynamicColorScheme(
        seedColor = seed,
        isDark = darkTheme,
        style = style,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
      )
    }
  MaterialExpressiveTheme(
    colorScheme = colorScheme,
    motionScheme = MotionScheme.expressive(),
    typography = AppTypography,
    content = content,
  )
}
