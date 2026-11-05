package android.content.res

/** The device Wear content sees; [androidx.wear.compose.WearDevice] provides it. */
class Configuration(
  var screenWidthDp: Int,
  var screenHeightDp: Int,
  var isScreenRound: Boolean,
  var fontScale: Float = 1f,
)

/** The colors by `android.R.color` id that [androidx.wear.compose.dynamicWearColorScheme] fills. */
class Resources(private val colors: Map<Int, Int> = emptyMap()) {
  fun getColor(id: Int, theme: Theme?): Int = colors[id] ?: 0

  fun getString(id: Int, vararg args: Any): String = ""
}

class Theme
