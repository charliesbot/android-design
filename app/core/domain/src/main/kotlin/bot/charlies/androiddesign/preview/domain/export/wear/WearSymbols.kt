package bot.charlies.androiddesign.preview.domain.export.wear

/** Fully qualified names of the Wear Compose APIs the Wear exporter writes. */
internal object WearSym {
  private const val M3 = "androidx.wear.compose.material3"
  private const val FOUNDATION = "androidx.wear.compose.foundation"

  /** A `androidx.wear.compose.material3` name. */
  fun m3(name: String) = "$M3.$name"

  const val ColorScheme = "$M3.ColorScheme"
  const val MaterialTheme = "$M3.MaterialTheme"
  const val AppScaffold = "$M3.AppScaffold"
  const val ScreenScaffold = "$M3.ScreenScaffold"
  const val TimeText = "$M3.TimeText"
  const val Text = "$M3.Text"
  const val Icon = "$M3.Icon"
  const val LocalContentColor = "$M3.LocalContentColor"
  const val SurfaceTransformation = "$M3.SurfaceTransformation"
  const val rememberTransformationSpec = "$M3.lazy.rememberTransformationSpec"
  const val transformedHeight = "$M3.lazy.transformedHeight"

  // foundation
  const val TransformingLazyColumn = "$FOUNDATION.lazy.TransformingLazyColumn"
  const val rememberTransformingLazyColumnState =
    "$FOUNDATION.lazy.rememberTransformingLazyColumnState"
  const val ScrollInfoProvider = "$FOUNDATION.ScrollInfoProvider"

  // navigation3
  const val rememberSwipeDismissableSceneStrategy =
    "androidx.wear.compose.navigation3.rememberSwipeDismissableSceneStrategy"

  // phone Compose pieces a Wear file also uses
  const val LocalConfiguration = "androidx.compose.ui.platform.LocalConfiguration"
  const val CompositionLocalProvider = "androidx.compose.runtime.CompositionLocalProvider"
  const val mutableIntStateOf = "androidx.compose.runtime.mutableIntStateOf"
}
