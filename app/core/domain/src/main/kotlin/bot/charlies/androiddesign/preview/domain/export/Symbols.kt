package bot.charlies.androiddesign.preview.domain.export

/** Fully qualified names of the Compose APIs the exporter writes. */
internal object Sym {
  private const val M3 = "androidx.compose.material3"
  private const val LAYOUT = "androidx.compose.foundation.layout"
  private const val RUNTIME = "androidx.compose.runtime"
  private const val UI = "androidx.compose.ui"
  private const val FONT = "androidx.compose.ui.text.font"

  // runtime
  const val Composable = "$RUNTIME.Composable"
  const val remember = "$RUNTIME.remember"
  const val mutableStateOf = "$RUNTIME.mutableStateOf"
  const val mutableFloatStateOf = "$RUNTIME.mutableFloatStateOf"
  const val getValue = "$RUNTIME.getValue"
  const val setValue = "$RUNTIME.setValue"
  const val LaunchedEffect = "$RUNTIME.LaunchedEffect"
  const val rememberCoroutineScope = "$RUNTIME.rememberCoroutineScope"
  const val launch = "kotlinx.coroutines.launch"
  const val rememberTextFieldState = "androidx.compose.foundation.text.input.rememberTextFieldState"

  // foundation
  const val Image = "androidx.compose.foundation.Image"
  const val BorderStroke = "androidx.compose.foundation.BorderStroke"
  const val background = "androidx.compose.foundation.background"
  const val clickable = "androidx.compose.foundation.clickable"
  const val verticalScroll = "androidx.compose.foundation.verticalScroll"
  const val rememberScrollState = "androidx.compose.foundation.rememberScrollState"
  const val isSystemInDarkTheme = "androidx.compose.foundation.isSystemInDarkTheme"
  const val CircleShape = "androidx.compose.foundation.shape.CircleShape"
  const val RoundedCornerShape = "androidx.compose.foundation.shape.RoundedCornerShape"

  // foundation layout
  const val Arrangement = "$LAYOUT.Arrangement"
  const val Box = "$LAYOUT.Box"
  const val BoxWithConstraints = "$LAYOUT.BoxWithConstraints"
  const val PaddingValues = "$LAYOUT.PaddingValues"
  const val Column = "$LAYOUT.Column"
  const val Row = "$LAYOUT.Row"
  const val Spacer = "$LAYOUT.Spacer"
  const val Grid = "$LAYOUT.Grid"
  const val FlexBox = "$LAYOUT.FlexBox"
  const val FlexWrap = "$LAYOUT.FlexWrap"
  const val ExperimentalGridApi = "$LAYOUT.ExperimentalGridApi"
  const val padding = "$LAYOUT.padding"
  const val fillMaxSize = "$LAYOUT.fillMaxSize"
  const val fillMaxWidth = "$LAYOUT.fillMaxWidth"
  const val fillMaxHeight = "$LAYOUT.fillMaxHeight"
  const val width = "$LAYOUT.width"
  const val height = "$LAYOUT.height"
  const val size = "$LAYOUT.size"
  const val heightIn = "$LAYOUT.heightIn"
  const val requiredHeightIn = "$LAYOUT.requiredHeightIn"
  const val widthIn = "$LAYOUT.widthIn"
  const val offset = "$LAYOUT.offset"
  const val aspectRatio = "$LAYOUT.aspectRatio"

  // ui
  const val Alignment = "$UI.Alignment"
  const val Modifier = "$UI.Modifier"
  const val clip = "$UI.draw.clip"
  const val nestedScroll = "$UI.input.nestedscroll.nestedScroll"
  const val semantics = "$UI.semantics.semantics"
  const val Role = "$UI.semantics.Role"
  const val ToggleableState = "$UI.state.ToggleableState"
  const val selectable = "androidx.compose.foundation.selection.selectable"
  const val selectableGroup = "androidx.compose.foundation.selection.selectableGroup"
  const val contentDescription = "$UI.semantics.contentDescription"
  const val ContentScale = "$UI.layout.ContentScale"
  const val painterResource = "$UI.res.painterResource"
  const val Dialog = "$UI.window.Dialog"
  const val DialogProperties = "$UI.window.DialogProperties"
  const val Popup = "$UI.window.Popup"
  const val Color = "$UI.graphics.Color"
  const val RectangleShape = "$UI.graphics.RectangleShape"
  const val Preview = "$UI.tooling.preview.Preview"
  const val dp = "$UI.unit.dp"
  const val Dp = "$UI.unit.Dp"
  const val sp = "$UI.unit.sp"
  const val TextAlign = "$UI.text.style.TextAlign"
  const val TextOverflow = "$UI.text.style.TextOverflow"
  const val Font = "$FONT.Font"
  const val FontFamily = "$FONT.FontFamily"
  const val FontStyle = "$FONT.FontStyle"
  const val FontVariation = "$FONT.FontVariation"
  const val FontWeight = "$FONT.FontWeight"
  const val FontRes = "androidx.annotation.FontRes"

  // material3
  const val ExperimentalMaterial3Api = "$M3.ExperimentalMaterial3Api"
  const val ExperimentalMaterial3ExpressiveApi = "$M3.ExperimentalMaterial3ExpressiveApi"
  const val ExperimentalMaterial3AdaptiveApi =
    "androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi"
  const val MaterialTheme = "$M3.MaterialTheme"
  const val MaterialExpressiveTheme = "$M3.MaterialExpressiveTheme"
  const val MotionScheme = "$M3.MotionScheme"
  const val Typography = "$M3.Typography"
  const val Shapes = "$M3.Shapes"
  const val MaterialShapes = "$M3.MaterialShapes"
  const val toShape = "$M3.toShape"
  const val Scaffold = "$M3.Scaffold"
  const val Text = "$M3.Text"
  const val Icon = "$M3.Icon"

  // MaterialKolor
  const val rememberDynamicColorScheme = "com.materialkolor.rememberDynamicColorScheme"
  const val PaletteStyle = "com.materialkolor.PaletteStyle"
  const val ColorSpec = "com.materialkolor.dynamiccolor.ColorSpec"

  // adaptive and navigation
  const val currentWindowAdaptiveInfoV2 =
    "androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2"
  const val WindowSizeClass = "androidx.window.core.layout.WindowSizeClass"
  const val NavKey = "androidx.navigation3.runtime.NavKey"
  const val entryProvider = "androidx.navigation3.runtime.entryProvider"
  const val rememberNavBackStack = "androidx.navigation3.runtime.rememberNavBackStack"
  const val NavDisplay = "androidx.navigation3.ui.NavDisplay"
  const val ListDetailSceneStrategy =
    "androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy"
  const val rememberListDetailSceneStrategy =
    "androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy"
  const val SupportingPaneSceneStrategy =
    "androidx.compose.material3.adaptive.navigation3.SupportingPaneSceneStrategy"
  const val rememberSupportingPaneSceneStrategy =
    "androidx.compose.material3.adaptive.navigation3.rememberSupportingPaneSceneStrategy"
  const val Serializable = "kotlinx.serialization.Serializable"

  /** A `androidx.compose.material3` name. */
  fun m3(name: String) = "$M3.$name"

  /** A `androidx.compose.material3.carousel` name. */
  fun carousel(name: String) = "$M3.carousel.$name"
}
