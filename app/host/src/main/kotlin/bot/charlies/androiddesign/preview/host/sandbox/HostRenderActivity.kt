package bot.charlies.androiddesign.preview.host.sandbox

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.runtime.tooling.LocalInspectionTables
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.compositionContext
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import androidx.compose.ui.unit.Density
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import bot.charlies.androiddesign.preview.feature.preview.sandbox.InsetConsumingFrameLayout
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalFonts
import bot.charlies.androiddesign.preview.kit.DirectionSpec
import bot.charlies.androiddesign.preview.kit.ExplorationBoard
import bot.charlies.androiddesign.preview.kit.ExploreBoardFrame
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.ViewSpec
import java.lang.ref.WeakReference
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

internal class ActiveRenderSession(
  val sessionKey: String,
  val sourceFile: String,
  val sourceText: String,
  val board: ExplorationBoard,
  val direction: DirectionSpec,
  val screenId: String,
  val view: ViewSpec,
  val designSystem: DesignSystem,
  val overlayId: String?,
  val renderDensity: Float,
  val fontScale: Float,
  val overrideContext: Context,
  val overrideConfig: Configuration,
  val inspectionTables: MutableSet<CompositionData>,
  val assetResolver: ((String) -> ImageBitmap?)?,
  val onRootCoordinates: (LayoutCoordinates) -> Unit,
  val onEmptyDetail: (String?) -> Unit,
)

class HostRenderActivity : ComponentActivity() {
  internal var composeView: ComposeView? = null
  internal var container: FrameLayout? = null
  internal val activeSession: MutableState<ActiveRenderSession?> = mutableStateOf(null)
  internal var virtualClock: BroadcastFrameClock? = null
  internal var recomposer: Recomposer? = null
  internal var virtualTimeNanos: Long = 0L
  internal val recomposerFailure = AtomicReference<Throwable?>(null)
  private var guardedRecomposer: GuardedRecomposer? = null
  private val alwaysResumedOwners = ServiceViewTreeOwners()
  private var movedToBack = false
  private var activeViewKey: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ProposalFonts.init(assets)
    startForegroundService(Intent(this, RenderForegroundService::class.java))
    WindowCompat.setDecorFitsSystemWindows(window, false)
    val root = InsetConsumingFrameLayout(this)
    setContentView(root)
    alwaysResumedOwners.moveToResumed()
    window.decorView.setPadding(0, 0, 0, 0)
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, _ ->
      WindowInsetsCompat.CONSUMED
    }
    ViewCompat.setOnApplyWindowInsetsListener(root) { _, _ -> WindowInsetsCompat.CONSUMED }
    listOf(window.decorView, root).forEach { v ->
      v.setViewTreeLifecycleOwner(alwaysResumedOwners)
      v.setViewTreeSavedStateRegistryOwner(alwaysResumedOwners)
      v.setViewTreeViewModelStoreOwner(alwaysResumedOwners)
      v.setViewTreeOnBackPressedDispatcherOwner(alwaysResumedOwners)
    }
    container = root
    instanceRef = WeakReference(this)
    val decor = window.decorView
    if (decor.isAttachedToWindow) {
      readyLatch.countDown()
    } else {
      decor.addOnAttachStateChangeListener(
        object : View.OnAttachStateChangeListener {
          override fun onViewAttachedToWindow(v: View) {
            v.removeOnAttachStateChangeListener(this)
            readyLatch.countDown()
          }

          override fun onViewDetachedFromWindow(v: View) = Unit
        },
      )
    }
  }

  override fun onResume() {
    super.onResume()
    window.decorView.post {
      if (!movedToBack && intent?.getBooleanExtra("move_to_back", true) != false) {
        movedToBack = true
        moveTaskToBack(true)
      }
    }
  }

  private fun ensureRecomposerPipeline() {
    if (recomposer != null) return
    recomposerFailure.set(null)
    val clock = BroadcastFrameClock()
    val guard = GuardedRecomposer(clock) { t -> recomposerFailure.compareAndSet(null, t) }
    window.decorView.compositionContext = guard.recomposer
    guardedRecomposer = guard
    virtualClock = clock
    recomposer = guard.recomposer
  }

  internal fun ensureComposeViewForTarget(
    overrideCtx: Context,
    viewKey: String,
    widthPx: Int,
    heightPx: Int,
  ) {
    ensureRecomposerPipeline()
    val root = container ?: return
    val recomp = recomposer ?: return
    if (composeView == null || activeViewKey != viewKey) {
      activeSession.value = null
      composeView?.disposeComposition()
      root.removeAllViews()
      isDebugInspectorInfoEnabled = true
      val cv = ComposeView(overrideCtx)
      ViewCompat.setOnApplyWindowInsetsListener(cv) { _, _ -> WindowInsetsCompat.CONSUMED }
      cv.setViewTreeLifecycleOwner(alwaysResumedOwners)
      cv.setViewTreeSavedStateRegistryOwner(alwaysResumedOwners)
      cv.setViewTreeViewModelStoreOwner(alwaysResumedOwners)
      cv.setViewTreeOnBackPressedDispatcherOwner(alwaysResumedOwners)
      cv.setParentCompositionContext(recomp)
      root.layoutParams = FrameLayout.LayoutParams(widthPx, heightPx)
      root.addView(cv, FrameLayout.LayoutParams(widthPx, heightPx))
      cv.setContent {
        val session = activeSession.value
        if (session != null) {
          key(session.sessionKey) {
            CompositionLocalProvider(
              LocalContext provides session.overrideContext,
              LocalConfiguration provides session.overrideConfig,
              LocalDensity provides
                Density(session.renderDensity, fontScale = session.fontScale.coerceAtLeast(0.5f)),
              LocalNavigationEventDispatcherOwner provides alwaysResumedOwners,
            ) {
              Box(Modifier.onGloballyPositioned { session.onRootCoordinates(it) }) {
                InspectedSubcomposition(session.inspectionTables) {
                  ExploreBoardFrame(
                    board = session.board,
                    direction = session.direction,
                    screenId = session.screenId,
                    view = session.view,
                    designSystem = session.designSystem,
                    explorationDir = null,
                    overlayId = session.overlayId,
                    assetResolver = session.assetResolver,
                    onEmptyDetail = session.onEmptyDetail,
                  )
                }
              }
            }
          }
        }
      }
      composeView = cv
      activeViewKey = viewKey
    } else {
      root.layoutParams = FrameLayout.LayoutParams(widthPx, heightPx)
      composeView?.layoutParams = FrameLayout.LayoutParams(widthPx, heightPx)
    }
  }

  internal fun resetComposeHost() {
    activeSession.value = null
    recomposerFailure.set(null)
    runCatching { composeView?.disposeComposition() }
    composeView = null
    activeViewKey = null
    container?.removeAllViews()
    guardedRecomposer?.cancel()
    guardedRecomposer = null
    recomposer = null
    virtualClock = null
  }

  override fun onDestroy() {
    resetComposeHost()
    alwaysResumedOwners.destroy()
    if (instanceRef?.get() === this) {
      instanceRef = null
    }
    super.onDestroy()
  }

  companion object {
    @Volatile private var instanceRef: WeakReference<HostRenderActivity>? = null
    @Volatile var readyLatch = CountDownLatch(1)

    fun current(): HostRenderActivity? = instanceRef?.get()

    fun resetLatch() {
      readyLatch = CountDownLatch(1)
    }
  }
}

@OptIn(ComposeToolingApi::class)
@Composable
internal fun InspectedSubcomposition(
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

internal class ServiceViewTreeOwners :
  LifecycleOwner,
  SavedStateRegistryOwner,
  ViewModelStoreOwner,
  OnBackPressedDispatcherOwner,
  NavigationEventDispatcherOwner {
  private val lifecycleRegistry = LifecycleRegistry(this)
  private val savedStateController = SavedStateRegistryController.create(this)
  override val viewModelStore: ViewModelStore = ViewModelStore()
  override val onBackPressedDispatcher: OnBackPressedDispatcher = OnBackPressedDispatcher()
  override val navigationEventDispatcher: NavigationEventDispatcher = NavigationEventDispatcher()

  override val lifecycle: Lifecycle
    get() = lifecycleRegistry

  override val savedStateRegistry: SavedStateRegistry
    get() = savedStateController.savedStateRegistry

  fun moveToResumed() {
    savedStateController.performRestore(null)
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
  }

  fun destroy() {
    if (lifecycleRegistry.currentState != Lifecycle.State.DESTROYED) {
      lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
  }
}
