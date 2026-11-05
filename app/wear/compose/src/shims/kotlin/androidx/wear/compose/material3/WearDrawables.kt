package androidx.wear.compose.material3

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

// Hand-translations of the vector drawables in compose-material3's res/drawable. Path data is
// copied
// from the XML; the animated ones only trim stroke paths, which is replayed from a clock.
internal object WearDrawables {
  private val trimEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

  private fun trim(timeMillis: Float, startMillis: Float, durationMillis: Float) =
    trimEasing.transform(((timeMillis - startMillis) / durationMillis).coerceIn(0f, 1f))

  private fun nodes(pathData: String) = PathParser().parsePathString(pathData).toNodes()

  val error: ImageVector by lazy {
    ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      )
      .addPath(
        pathData =
          nodes(
            "M480,680Q497,680 508.5,668.5Q520,657 520,640Q520,623 508.5,611.5Q497,600 480,600" +
              "Q463,600 451.5,611.5Q440,623 440,640Q440,657 451.5,668.5Q463,680 480,680ZM440,520" +
              "L520,520L520,280L440,280L440,520ZM480,880Q397,880 324,848.5Q251,817 197,763Q143,709" +
              " 111.5,636Q80,563 80,480Q80,397 111.5,324Q143,251 197,197Q251,143 324,111.5Q397,80" +
              " 480,80Q563,80 636,111.5Q709,143 763,197Q817,251 848.5,324Q880,397 880,480Q880,563" +
              " 848.5,636Q817,709 763,763Q709,817 636,848.5Q563,880 480,880ZM480,800Q614,800 707," +
              "707Q800,614 800,480Q800,346 707,253Q614,160 480,160Q346,160 253,253Q160,346 160,480" +
              "Q160,614 253,707Q346,800 480,800Z",
          ),
        fill = SolidColor(Color.White),
      )
      .build()
  }

  val checkAnimation: AnimatedImageVector by lazy {
    AnimatedImageVector(durationMillis = 267) { time ->
      ImageVector.Builder(
          defaultWidth = 520.dp,
          defaultHeight = 520.dp,
          viewportWidth = 520f,
          viewportHeight = 520f,
        )
        .addGroup(translationX = 252f, translationY = 294f, scaleX = 1.05507f, scaleY = 1.05556f)
        .addPath(
          pathData =
            nodes("M-147.5 -20 C-147.5,-20 -51.5,76 -51.5,76 C-51.5,76 151.5,-127 151.5,-127"),
          stroke = SolidColor(Color(0xFFD3E3FD)),
          strokeLineWidth = 34f,
          strokeLineCap = StrokeCap.Round,
          strokeLineJoin = StrokeJoin.Round,
          trimPathEnd = trim(time, 0f, 267f),
        )
        .clearGroup()
        .build()
    }
  }

  val failureAnimation: AnimatedImageVector by lazy {
    AnimatedImageVector(durationMillis = 367) { time ->
      val red = SolidColor(Color(0xFFFF8986))
      ImageVector.Builder(
          defaultWidth = 520.dp,
          defaultHeight = 520.dp,
          viewportWidth = 520f,
          viewportHeight = 520f,
        )
        .addGroup(translationX = 264f, translationY = 260f, scaleX = 10.57f, scaleY = 10.57f)
        .addPath(pathData = nodes(PHONE_OUTLINE), fill = red)
        .clearGroup()
        .addGroup(translationX = 250f, translationY = 262f)
        .addPath(
          pathData = nodes("M55.5 59.5 C55.5,59.5 180,-64.5 180,-64.5"),
          stroke = red,
          strokeLineWidth = 34f,
          strokeLineCap = StrokeCap.Round,
          strokeLineJoin = StrokeJoin.Round,
          trimPathEnd = trim(time, 100f, 267f),
        )
        .addPath(
          pathData = nodes("M56 -64.5 C56,-64.5 179.5,59 179.5,59"),
          stroke = red,
          strokeLineWidth = 35f,
          strokeLineCap = StrokeCap.Round,
          strokeLineJoin = StrokeJoin.Round,
          trimPathEnd = trim(time, 0f, 267f),
        )
        .clearGroup()
        .build()
    }
  }

  private const val PHONE_OUTLINE =
    "M-14.49 17.77 C-14.49,17.77 -14.49,19.61 -14.49,19.61 C-14.49,19.72 -14.45,19.81 -14.38,19.88 " +
      "C-14.31,19.95 -14.22,19.99 -14.11,19.99 C-14.11,19.99 7.78,19.99 7.78,19.99 C7.88,19.99 7.97," +
      "19.95 8.05,19.88 C8.12,19.81 8.15,19.72 8.15,19.61 C8.15,19.61 8.15,17.77 8.15,17.77 " +
      "C8.15,17.77 -14.49,17.77 -14.49,17.77 Z M-14.49 -17.77 C-14.49,-17.77 8.15,-17.77 8.15,-17.77 " +
      "C8.15,-17.77 8.15,-19.61 8.15,-19.61 C8.15,-19.72 8.12,-19.81 8.05,-19.88 C7.97,-19.95 7.88," +
      "-19.99 7.78,-19.99 C7.78,-19.99 -14.11,-19.99 -14.11,-19.99 C-14.22,-19.99 -14.31,-19.95 " +
      "-14.38,-19.88 C-14.45,-19.81 -14.49,-19.72 -14.49,-19.61 C-14.49,-19.61 -14.49,-17.77 " +
      "-14.49,-17.77 Z M-14.11 23.29 C-15.12,23.29 -15.99,22.93 -16.71,22.21 C-17.43,21.49 -17.79," +
      "20.62 -17.79,19.61 C-17.79,19.61 -17.79,-19.61 -17.79,-19.61 C-17.79,-20.62 -17.43,-21.49 " +
      "-16.71,-22.21 C-15.99,-22.93 -15.12,-23.29 -14.11,-23.29 C-14.11,-23.29 7.78,-23.29 7.78," +
      "-23.29 C8.79,-23.29 9.65,-22.93 10.38,-22.21 C11.1,-21.49 11.46,-20.62 11.46,-19.61 " +
      "C11.46,-19.61 11.46,-13.32 11.46,-13.32 C11.46,-12.86 11.3,-12.46 10.97,-12.13 C10.65," +
      "-11.81 10.25,-11.65 9.78,-11.65 C9.31,-11.65 8.91,-11.81 8.59,-12.13 C8.3,-12.46 8.15," +
      "-12.86 8.15,-13.32 C8.15,-13.32 8.15,-14.46 8.15,-14.46 C8.15,-14.46 -14.49,-14.46 -14.49," +
      "-14.46 C-14.49,-14.46 -14.49,14.46 -14.49,14.46 C-14.49,14.46 8.15,14.46 8.15,14.46 " +
      "C8.15,14.46 8.15,13.33 8.15,13.33 C8.15,12.86 8.3,12.46 8.59,12.13 C8.91,11.81 9.31,11.65 " +
      "9.78,11.65 C10.25,11.65 10.65,11.81 10.97,12.13 C11.3,12.46 11.46,12.86 11.46,13.33 " +
      "C11.46,13.33 11.46,19.61 11.46,19.61 C11.46,20.62 11.1,21.49 10.38,22.21 C9.65,22.93 8.79," +
      "23.29 7.78,23.29 C7.78,23.29 -14.11,23.29 -14.11,23.29 Z"
}
