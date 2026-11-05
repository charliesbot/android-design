package androidx.wear.compose

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

/**
 * Hosts Wear Compose content as a watch of the given size: provides the screen size and shape the
 * Wear components read from the configuration, clips to a circle when [round], and draws on black.
 * The mouse wheel and trackpad scroll lists as plain scrolling; crown (rotary) input is not
 * available, so rotary-only behavior is not shown.
 *
 * This is the one entry point the rest of the app needs; everything inside [content] is the
 * upstream Wear Compose API. Dialogs are scene-wide layers on desktop, so size the scene to the
 * device when a dialog may show.
 */
@Composable
fun WearDevice(widthDp: Int, heightDp: Int, round: Boolean, content: @Composable () -> Unit) {
  val configuration = remember(widthDp, heightDp, round) { Configuration(widthDp, heightDp, round) }
  CompositionLocalProvider(LocalConfiguration provides configuration) {
    Box(
      Modifier.size(widthDp.dp, heightDp.dp)
        .let { if (round) it.clip(CircleShape) else it }
        .background(Color.Black),
    ) {
      content()
    }
  }
}
