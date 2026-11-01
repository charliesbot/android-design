package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/**
 * Material 3 elevation level 3 (the FAB's resting shadow) as the two explicit shadows of the
 * Material spec: a tight key shadow and a wide ambient one.
 *
 * Compose Desktop draws `Modifier.shadow` from a light fixed near the top of the window, so the
 * spot shadow slides further down the lower a component sits. A FAB near the bottom of a tall frame
 * gets its shadow pushed clear of its edge, which leaves a hard-edged sliver of background between
 * the container and the shadow. Explicit shadows follow the component instead, wherever it sits and
 * however the frame is scaled.
 */
internal fun Modifier.level3Shadow(shape: Shape): Modifier =
  dropShadow(shape, AmbientShadow).dropShadow(shape, KeyShadow)

private val KeyShadow =
  Shadow(radius = 3.dp, color = Color.Black, offset = DpOffset(0.dp, 1.dp), alpha = 0.3f)

private val AmbientShadow =
  Shadow(
    radius = 8.dp,
    color = Color.Black,
    spread = 3.dp,
    offset = DpOffset(0.dp, 4.dp),
    alpha = 0.15f,
  )
