package androidx.compose.animation.graphics.res

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.wear.compose.material3.R
import androidx.wear.compose.material3.WearDrawables

fun AnimatedImageVector.Companion.animatedVectorResource(id: Int): AnimatedImageVector =
  when (id) {
    R.drawable.wear_m3c_check_animation -> WearDrawables.checkAnimation
    R.drawable.wear_m3c_failure_animation -> WearDrawables.failureAnimation
    else -> error("no animated vector drawable $id")
  }

/** Plays [image] forward when [atEnd] turns true, like Android's painter of the same name. */
@Composable
fun rememberAnimatedVectorPainter(image: AnimatedImageVector, atEnd: Boolean): Painter {
  val time by
    animateFloatAsState(
      targetValue = if (atEnd) image.durationMillis.toFloat() else 0f,
      animationSpec = tween(image.durationMillis, easing = LinearEasing),
      label = "animated-vector",
    )
  val vector = remember(image, time) { image.render(time) }
  return rememberVectorPainter(vector)
}
