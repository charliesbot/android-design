package bot.charlies.androiddesign.preview.feature.preview.sandbox

import android.content.Context
import android.content.res.Configuration
import android.view.ContextThemeWrapper
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.runtime.tooling.LocalInspectionTables
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import bot.charlies.androiddesign.preview.kit.BoardSurface
import bot.charlies.androiddesign.preview.kit.DirectionSpec
import bot.charlies.androiddesign.preview.kit.ExplorationBoard
import bot.charlies.androiddesign.preview.kit.ExploreBoardFrame
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.ViewSpec
import java.nio.file.Path
import kotlin.math.roundToInt

/** Inputs for rendering a single screen/overlay target of an [ExplorationBoard]. */
data class RenderTargetRequest(
  val board: ExplorationBoard,
  val direction: DirectionSpec,
  val screenId: String,
  val view: ViewSpec,
  val sourceFile: String,
  val sourceText: String,
  val designMd: ThemeOverride? = null,
  val overlayId: String? = null,
  val density: Float = 2f,
  val fontScale: Float = 1f,
  val darkOverride: Boolean? = null,
  val seedOverride: String? = null,
  val variantOverride: String? = null,
  val assetResolver: ((String) -> ImageBitmap?)? = null,
  val explorationDir: Path? = null,
)

/** Resolved dimensions, theme, and view configuration derived from a [RenderTargetRequest]. */
data class PreparedRenderTarget(
  val request: RenderTargetRequest,
  val effectiveView: ViewSpec,
  val effectiveFontScale: Float,
  val designSystem: DesignSystem,
  val widthPx: Int,
  val heightPx: Int,
  val dpi: Int,
  val perimeterPx: Int,
) {
  val minScrollerWidthPx: Float
    get() = widthPx * 0.6f

  val scrollStepPx: Float
    get() = effectiveView.height * request.density / 2f

  fun createOverrideContext(base: Context): Pair<Context, Configuration> {
    val cfg = Configuration(base.resources.configuration)
    cfg.densityDpi = dpi
    cfg.fontScale = effectiveFontScale
    cfg.screenWidthDp = effectiveView.width
    cfg.screenHeightDp = effectiveView.height
    cfg.smallestScreenWidthDp = minOf(effectiveView.width, effectiveView.height)
    cfg.uiMode =
      (cfg.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
        if (effectiveView.dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
    cfg.screenLayout =
      (cfg.screenLayout and Configuration.SCREENLAYOUT_ROUND_MASK.inv()) or
        if (effectiveView.round) Configuration.SCREENLAYOUT_ROUND_YES
        else Configuration.SCREENLAYOUT_ROUND_NO
    val wrapped =
      object : ContextThemeWrapper(base, base.theme) {
        init {
          applyOverrideConfiguration(cfg)
        }
      }
    return wrapped to cfg
  }

  companion object {
    private const val BASE_DPI = 160f

    fun from(request: RenderTargetRequest): PreparedRenderTarget {
      val effectiveView =
        if (request.darkOverride != null && request.board.surface == BoardSurface.Phone) {
          request.view.copy(dark = request.darkOverride)
        } else {
          request.view
        }
      val effectiveFontScale = request.fontScale.coerceAtLeast(0.5f)
      val variant = request.variantOverride?.let { PaletteVariant.fromWire(it) }
      val designSystem =
        request.board.resolveDesignSystem(
          designMd = request.designMd,
          seedOverride = request.seedOverride,
          variantOverride = variant,
        )
      return PreparedRenderTarget(
        request = request,
        effectiveView = effectiveView,
        effectiveFontScale = effectiveFontScale,
        designSystem = designSystem,
        widthPx = (effectiveView.width * request.density).roundToInt().coerceAtLeast(1),
        heightPx = (effectiveView.height * request.density).roundToInt().coerceAtLeast(1),
        dpi = (BASE_DPI * request.density).roundToInt().coerceAtLeast(120),
        perimeterPx = (1f * request.density).roundToInt().coerceAtLeast(1),
      )
    }
  }
}

/** Captured RGBA frame alongside overlay window offsets measured during composition draw. */
data class CapturedFrame(
  val frame: RawRgbaFrame,
  val explicitWindowOffsets: Map<View, Offset>,
)

/** Main decor view and active offscreen windows present in the host's window hierarchy. */
data class RenderWindowSnapshot(val mainDecorView: View, val activeWindows: List<View>)

/** Output of rendering, settling, and optionally scrolling a single board target. */
data class BoardRenderResult(
  val target: PreparedRenderTarget,
  val tree: LayoutTree,
  val endTree: LayoutTree?,
  val frame: RawRgbaFrame,
  val endFrame: RawRgbaFrame?,
  val renderMs: Double,
)

/** Surface mount used by [ComposeBoardRenderer] (Display 0 `HardwareRenderer` on Android). */
interface BoardRenderHost {
  suspend fun <T> withMountedTarget(
    target: PreparedRenderTarget,
    inspectionTables: MutableSet<CompositionData>,
    onRootCoordinates: (LayoutCoordinates) -> Unit,
    onEmptyDetail: (String?) -> Unit,
    block: suspend (MountedRenderSession) -> T,
  ): T
}

/** Active mounted target handle providing frame capture, UI-thread dispatch, and window access. */
interface MountedRenderSession {
  val driver: WearScrollDriver

  suspend fun captureFrame(): CapturedFrame

  suspend fun <T> onUiThread(block: () -> T): T

  fun currentWindows(): RenderWindowSnapshot
}

@OptIn(ComposeToolingApi::class)
@Composable
fun RenderBoardContent(
  target: PreparedRenderTarget,
  overrideContext: Context,
  overrideConfig: Configuration,
  inspectionTables: MutableSet<CompositionData>,
  navigationEventDispatcherOwner: NavigationEventDispatcherOwner? = null,
  onRootCoordinates: (LayoutCoordinates) -> Unit,
  onEmptyDetail: (String?) -> Unit,
) {
  val provided = buildList {
    add(LocalContext provides overrideContext)
    add(LocalConfiguration provides overrideConfig)
    add(
      LocalDensity provides Density(target.request.density, fontScale = target.effectiveFontScale),
    )
    if (navigationEventDispatcherOwner != null) {
      add(LocalNavigationEventDispatcherOwner provides navigationEventDispatcherOwner)
    }
  }
    .toTypedArray()
  CompositionLocalProvider(*provided) {
    Box(Modifier.onGloballyPositioned(onRootCoordinates)) {
      InspectedSubcomposition(inspectionTables) {
        ExploreBoardFrame(
          board = target.request.board,
          direction = target.request.direction,
          screenId = target.request.screenId,
          view = target.effectiveView,
          designSystem = target.designSystem,
          explorationDir = target.request.explorationDir,
          overlayId = target.request.overlayId,
          assetResolver = target.request.assetResolver,
          onEmptyDetail = onEmptyDetail,
        )
      }
    }
  }
}

@OptIn(ComposeToolingApi::class)
@Composable
fun InspectedSubcomposition(
  tables: MutableSet<CompositionData>,
  content: @Composable () -> Unit,
) {
  // Enable source/parameter markers only on the outer composer if root didn't enable them.
  // Calling collectParameterInformation() inside subcompose(Unit) resets an already-populated
  // SlotTable.sourceInformationMap mid-composition and drops child source info.
  if (LocalInspectionTables.current == null) {
    currentComposer.collectParameterInformation()
  }
  CompositionLocalProvider(LocalInspectionTables provides tables) {
    SubcomposeLayout { constraints ->
      val placeables = subcompose(Unit) { content() }.map { it.measure(constraints) }
      val width = placeables.maxOfOrNull { it.width } ?: constraints.minWidth
      val height = placeables.maxOfOrNull { it.height } ?: constraints.minHeight
      layout(width, height) { placeables.forEach { it.placeRelative(0, 0) } }
    }
  }
}
