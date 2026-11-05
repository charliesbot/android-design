package androidx.compose.animation.graphics.vector

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Stand-in for Android's animated vector. Upstream loads AAPT animated-vector XML, which desktop
 * cannot, so each animated drawable Wear M3 ships is rebuilt by hand in `WearDrawables` as a
 * function from time to [ImageVector] (the XML only animates path trimming).
 */
class AnimatedImageVector
internal constructor(
  val durationMillis: Int,
  internal val render: (timeMillis: Float) -> ImageVector,
) {
  companion object
}
