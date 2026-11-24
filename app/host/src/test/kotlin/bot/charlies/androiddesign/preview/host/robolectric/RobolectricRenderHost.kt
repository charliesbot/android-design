package bot.charlies.androiddesign.preview.host.robolectric

import android.graphics.Color as AndroidColor
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.inspector.WindowInspector
import androidx.activity.ComponentActivity
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.compositionContext
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import androidx.compose.ui.semantics.SemanticsNode
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import bot.charlies.androiddesign.preview.feature.preview.sandbox.BoardRenderHost
import bot.charlies.androiddesign.preview.feature.preview.sandbox.CapturedFrame
import bot.charlies.androiddesign.preview.feature.preview.sandbox.FrameStatus
import bot.charlies.androiddesign.preview.feature.preview.sandbox.InsetConsumingFrameLayout
import bot.charlies.androiddesign.preview.feature.preview.sandbox.MountedRenderSession
import bot.charlies.androiddesign.preview.feature.preview.sandbox.OverlayWindowHelper
import bot.charlies.androiddesign.preview.feature.preview.sandbox.PreparedRenderTarget
import bot.charlies.androiddesign.preview.feature.preview.sandbox.RenderBoardContent
import bot.charlies.androiddesign.preview.feature.preview.sandbox.RenderWindowSnapshot
import bot.charlies.androiddesign.preview.feature.preview.sandbox.SettlePolicy
import bot.charlies.androiddesign.preview.feature.preview.sandbox.WearScrollDriver
import bot.charlies.androiddesign.preview.feature.preview.sandbox.WearScrollHelper
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalFonts
import bot.charlies.androiddesign.preview.host.sandbox.GuardedRecomposer
import bot.charlies.androiddesign.preview.host.sandbox.HardwareFrameRenderer
import bot.charlies.androiddesign.preview.host.sandbox.toRawRgbaFrame
import java.util.concurrent.atomic.AtomicReference
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf

/**
 * Headless Robolectric SDK 35 [BoardRenderHost] backed by an isolated `ComponentActivity` and
 * `HardwareRenderer` frame capture shared with the device host.
 */
@OptIn(ComposeToolingApi::class)
internal class RobolectricRenderHost : BoardRenderHost {
  private val frameRenderer by lazy { HardwareFrameRenderer() }

  override suspend fun <T> withMountedTarget(
    target: PreparedRenderTarget,
    inspectionTables: MutableSet<CompositionData>,
    onRootCoordinates: (LayoutCoordinates) -> Unit,
    onEmptyDetail: (String?) -> Unit,
    block: suspend (MountedRenderSession) -> T,
  ): T {
    isDebugInspectorInfoEnabled = true
    val controller = Robolectric.buildActivity(ComponentActivity::class.java)
    var guardedRecomposer: GuardedRecomposer? = null
    var composeView: ComposeView? = null
    try {
      val activity = controller.get()
      activity.setTheme(android.R.style.Theme_Material_NoActionBar)
      WindowCompat.setDecorFitsSystemWindows(activity.window, false)
      controller.create().start().resume().visible()
      ProposalFonts.init(activity.assets)

      val (overrideCtx, overrideCfg) = target.createOverrideContext(activity)
      val clock = BroadcastFrameClock()
      val recomposerFailure = AtomicReference<Throwable?>(null)
      val guarded = GuardedRecomposer(clock) { t -> recomposerFailure.compareAndSet(null, t) }
      guardedRecomposer = guarded
      val activeRecomposer = guarded.recomposer

      val insetContainer = InsetConsumingFrameLayout(overrideCtx)
      val cv = ComposeView(overrideCtx)
      composeView = cv
      ViewCompat.setOnApplyWindowInsetsListener(activity.window.decorView) { _, _ ->
        WindowInsetsCompat.CONSUMED
      }
      ViewCompat.setOnApplyWindowInsetsListener(insetContainer) { _, _ ->
        WindowInsetsCompat.CONSUMED
      }
      ViewCompat.setOnApplyWindowInsetsListener(cv) { _, _ -> WindowInsetsCompat.CONSUMED }

      insetContainer.addView(cv, ViewGroup.LayoutParams(target.widthPx, target.heightPx))
      activity.window.decorView.compositionContext = activeRecomposer
      cv.setParentCompositionContext(activeRecomposer)
      activity.setContentView(
        insetContainer,
        ViewGroup.LayoutParams(target.widthPx, target.heightPx),
      )

      val navOwner =
        (activity as? NavigationEventDispatcherOwner) ?: FallbackNavigationEventDispatcherOwner()
      cv.setContent {
        RenderBoardContent(
          target = target,
          overrideContext = overrideCtx,
          overrideConfig = overrideCfg,
          inspectionTables = inspectionTables,
          navigationEventDispatcherOwner = navOwner,
          onRootCoordinates = onRootCoordinates,
          onEmptyDetail = onEmptyDetail,
        )
      }

      shadowOf(Looper.getMainLooper()).idle()
      recomposerFailure.get()?.let { throw it }

      val driver =
        RobolectricFrameDriver(
          activity = activity,
          insetContainer = insetContainer,
          composeView = cv,
          clock = clock,
          recomposer = activeRecomposer,
          recomposerFailure = recomposerFailure,
          target = target,
        )

      val mounted =
        object : MountedRenderSession {
          override val driver: WearScrollDriver = driver

          override suspend fun captureFrame(): CapturedFrame =
            captureHardwareFrame(activity, cv, target.widthPx, target.heightPx)

          override suspend fun <R> onUiThread(block: () -> R): R = block()

          override fun currentWindows(): RenderWindowSnapshot =
            RenderWindowSnapshot(
              mainDecorView = activity.window.decorView,
              activeWindows =
                WindowInspector.getGlobalWindowViews().filter { it.isAttachedToWindow },
            )
        }

      return block(mounted)
    } finally {
      runCatching { composeView?.disposeComposition() }
      guardedRecomposer?.cancel()
      runCatching { controller.pause().stop().destroy() }
      shadowOf(Looper.getMainLooper()).idle()
    }
  }

  private fun captureHardwareFrame(
    activity: ComponentActivity,
    composeView: ComposeView,
    widthPx: Int,
    heightPx: Int,
  ): CapturedFrame {
    val mainDecor = activity.window.decorView
    val attachedWindows = WindowInspector.getGlobalWindowViews().filter { it.isAttachedToWindow }
    val overlays = attachedWindows.filter { it !== mainDecor }
    val mainView = if (mainDecor in attachedWindows) mainDecor else composeView
    val reader = frameRenderer.prepare(widthPx, heightPx)
    val offsets = frameRenderer.record { canvas ->
      OverlayWindowHelper.drawWindowComposite(
        canvas = canvas,
        mainView = mainView,
        mainDecorView = mainDecor,
        overlays = overlays,
        widthPx = widthPx,
        heightPx = heightPx,
        backgroundColor = AndroidColor.BLACK,
      )
    }
    frameRenderer.syncAndDraw()
    // Robolectric's acquireLatestImage never reports an empty queue, so take the one frame drawn.
    val image = reader.acquireNextImage()
    val frame = image.toRawRgbaFrame(widthPx, heightPx)
    swapRedBlue(frame.bytes)
    return CapturedFrame(frame, offsets)
  }

  /** Host Skia hands Robolectric's ImageReader BGRA bytes; the device's are RGBA. */
  private fun swapRedBlue(bytes: ByteArray) {
    var i = 0
    while (i < bytes.size) {
      val r = bytes[i]
      bytes[i] = bytes[i + 2]
      bytes[i + 2] = r
      i += 4
    }
  }

  private class FallbackNavigationEventDispatcherOwner : NavigationEventDispatcherOwner {
    override val navigationEventDispatcher: NavigationEventDispatcher = NavigationEventDispatcher()
  }

  private class RobolectricFrameDriver(
    private val activity: ComponentActivity,
    private val insetContainer: View,
    private val composeView: ComposeView,
    private val clock: BroadcastFrameClock,
    private val recomposer: androidx.compose.runtime.Recomposer,
    private val recomposerFailure: AtomicReference<Throwable?>,
    private val target: PreparedRenderTarget,
  ) : WearScrollDriver {
    private val wSpec = View.MeasureSpec.makeMeasureSpec(target.widthPx, View.MeasureSpec.EXACTLY)
    private val hSpec = View.MeasureSpec.makeMeasureSpec(target.heightPx, View.MeasureSpec.EXACTLY)
    private val mainLoc = IntArray(2)
    private var virtualTimeNs = 0L

    override suspend fun advanceFrame(): FrameStatus {
      androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications()
      virtualTimeNs += SettlePolicy.FRAME_NS
      clock.sendFrame(virtualTimeNs)
      androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications()
      shadowOf(Looper.getMainLooper()).idle()
      recomposerFailure.get()?.let { throw it }
      return layoutWindows()
    }

    override suspend fun finishSettle() {
      shadowOf(Looper.getMainLooper()).idle()
      recomposerFailure.get()?.let { throw it }
    }

    override suspend fun scrollOnce(): Boolean =
      WearScrollHelper.scrollDown(
        collectUnmergedRoots(),
        target.minScrollerWidthPx,
        target.scrollStepPx,
      )

    private fun layoutWindows(): FrameStatus {
      val mainDecor = activity.window.decorView
      mainDecor.measure(wSpec, hSpec)
      mainDecor.layout(0, 0, target.widthPx, target.heightPx)
      insetContainer.measure(wSpec, hSpec)
      insetContainer.layout(0, 0, target.widthPx, target.heightPx)
      composeView.measure(wSpec, hSpec)
      composeView.layout(0, 0, target.widthPx, target.heightPx)
      mainDecor.getLocationOnScreen(mainLoc)
      val overlays =
        WindowInspector.getGlobalWindowViews().filter {
          it !== mainDecor && it.visibility == View.VISIBLE
        }
      var awaitingAttach = false
      for (win in overlays) {
        val m =
          OverlayWindowHelper.measureAndLayoutOverlayWindow(
            win,
            mainLoc,
            target.widthPx,
            target.heightPx,
          )
        if (!win.isAttachedToWindow || m.width == 0) awaitingAttach = true
      }
      recomposerFailure.get()?.let { throw it }
      return FrameStatus(
        workPending = clock.hasAwaiters || recomposer.hasPendingWork,
        overlayPresent = overlays.isNotEmpty(),
        awaitingOverlayAttach = awaitingAttach,
      )
    }

    private fun collectUnmergedRoots(): List<SemanticsNode> {
      val windows = WindowInspector.getGlobalWindowViews().filter { it.isAttachedToWindow }
      return OverlayWindowHelper.collectComposeRoots(windows).map {
        it.semanticsOwner.unmergedRootSemanticsNode
      }
    }
  }
}
