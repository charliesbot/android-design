package com.example.designsandbox.wear.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.MotionScheme
import androidx.wear.compose.material3.Typography
import androidx.wear.compose.material3.dynamicColorScheme
import com.example.designsandbox.designsystem.common.R

// Wear's MaterialTheme defaults to the standard motion scheme; opt into expressive.
// Design guidance: the android-design skill (references/wear-os.md).
@Composable
fun WearAppTheme(content: @Composable () -> Unit) {
  val context = LocalContext.current
  MaterialTheme(
    // Dynamic color comes from the watch face; null when unavailable.
    colorScheme = dynamicColorScheme(context) ?: ColorScheme(),
    motionScheme = MotionScheme.expressive(),
    typography = Typography(defaultFontFamily = FontFamily(Font(R.font.roboto_flex))),
    content = content,
  )
}
