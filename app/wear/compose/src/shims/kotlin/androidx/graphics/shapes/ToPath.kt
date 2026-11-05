package androidx.graphics.shapes

import org.jetbrains.skia.Path
import org.jetbrains.skia.PathBuilder

/** The Android artifact draws a morph into an android.graphics.Path; this returns a Skia Path. */
fun Morph.toPath(progress: Float): Path {
  val builder = PathBuilder()
  var first = true
  asCubics(progress).forEach { c ->
    if (first) {
      builder.moveTo(c.anchor0X, c.anchor0Y)
      first = false
    }
    builder.cubicTo(c.control0X, c.control0Y, c.control1X, c.control1Y, c.anchor1X, c.anchor1Y)
  }
  builder.closePath()
  return builder.detach()
}
