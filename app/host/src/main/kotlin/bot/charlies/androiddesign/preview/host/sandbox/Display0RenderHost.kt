package bot.charlies.androiddesign.preview.host.sandbox

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inspector.WindowInspector
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.semantics.SemanticsNode
import bot.charlies.androiddesign.preview.feature.preview.sandbox.BoardRenderHost
import bot.charlies.androiddesign.preview.feature.preview.sandbox.CapturedFrame
import bot.charlies.androiddesign.preview.feature.preview.sandbox.FrameStatus
import bot.charlies.androiddesign.preview.feature.preview.sandbox.MountedRenderSession
import bot.charlies.androiddesign.preview.feature.preview.sandbox.OverlayWindowHelper
import bot.charlies.androiddesign.preview.feature.preview.sandbox.PreparedRenderTarget
import bot.charlies.androiddesign.preview.feature.preview.sandbox.RenderWindowSnapshot
import bot.charlies.androiddesign.preview.feature.preview.sandbox.SettlePolicy
import bot.charlies.androiddesign.preview.feature.preview.sandbox.WearScrollDriver
import bot.charlies.androiddesign.preview.feature.preview.sandbox.WearScrollHelper
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android Display 0 [BoardRenderHost] backed by [HostRenderActivity] and [FrameCapture]
 * (`HardwareRenderer` + `ImageReader`).
 */
@OptIn(ComposeToolingApi::class)
internal class Display0RenderHost(
  private val serviceContext: Context,
  private val frameCapture: FrameCapture,
) : BoardRenderHost {
  private var sessionSequence: Long = 0L

  override suspend fun <T> withMountedTarget(
    target: PreparedRenderTarget,
    inspectionTables: MutableSet<CompositionData>,
    onRootCoordinates: (LayoutCoordinates) -> Unit,
    onEmptyDetail: (String?) -> Unit,
    block: suspend (MountedRenderSession) -> T,
  ): T {
    ensureDisplay0ActivityHost()
    val activity = checkNotNull(HostRenderActivity.current()) { "HostRenderActivity did not start" }
    val req = target.request
    val (overrideCtx, overrideCfg) = target.createOverrideContext(activity)
    val sessionKey =
      "${req.direction.name}:${req.screenId}:${target.effectiveView.name}:${req.overlayId}:${req.density}:${target.effectiveFontScale}:${target.effectiveView.dark}:${req.seedOverride}:${req.variantOverride}:${sessionSequence++}"
    val activeSession =
      ActiveRenderSession(
        sessionKey = sessionKey,
        target = target,
        overrideContext = overrideCtx,
        overrideConfig = overrideCfg,
        inspectionTables = inspectionTables,
        onRootCoordinates = onRootCoordinates,
        onEmptyDetail = onEmptyDetail,
      )

    withContext(Dispatchers.Main) {
      activity.ensureComposeViewForTarget(
        overrideCtx,
        "${target.effectiveView.name}@${req.density}@${target.effectiveFontScale}",
        target.widthPx,
        target.heightPx,
      )
      activity.activeSession.value = activeSession
    }

    val mounted =
      object : MountedRenderSession {
        override val driver: WearScrollDriver = DeviceFrameDriver(activity, target)

        override suspend fun captureFrame(): CapturedFrame =
          frameCapture.capture(activity, target.widthPx, target.heightPx)

        override suspend fun <R> onUiThread(block: () -> R): R =
          withContext(Dispatchers.Main) { block() }

        override fun currentWindows(): RenderWindowSnapshot =
          RenderWindowSnapshot(
            mainDecorView = activity.window.decorView,
            activeWindows = offscreenWindows(activity),
          )
      }

    return try {
      block(mounted)
    } catch (t: Throwable) {
      withContext(Dispatchers.Main) { HostRenderActivity.current()?.resetComposeHost() }
      throw t
    }
  }

  private fun ensureDisplay0ActivityHost() {
    if (HostRenderActivity.current() != null) return
    HostRenderActivity.resetLatch()
    val intent =
      Intent(serviceContext, HostRenderActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
        putExtra("move_to_back", true)
      }
    serviceContext.startActivity(intent)
    check(HostRenderActivity.readyLatch.await(3, TimeUnit.SECONDS)) {
      "Timed out waiting for HostRenderActivity on Display 0"
    }
  }

  private fun offscreenWindows(activity: HostRenderActivity): List<View> =
    WindowInspector.getGlobalWindowViews().filter { belongsToOffscreenTree(activity, it) }

  private fun collectUnmergedRoots(activity: HostRenderActivity): List<SemanticsNode> =
    OverlayWindowHelper.collectComposeRoots(offscreenWindows(activity)).map {
      it.semanticsOwner.unmergedRootSemanticsNode
    }

  private inner class DeviceFrameDriver(
    private val activity: HostRenderActivity,
    private val target: PreparedRenderTarget,
  ) : WearScrollDriver {
    private val mainLoc = IntArray(2)

    override suspend fun advanceFrame(): FrameStatus {
      val clock = checkNotNull(activity.virtualClock)
      val recomp = checkNotNull(activity.recomposer)
      withContext(Dispatchers.Main) {
        Snapshot.sendApplyNotifications()
        activity.virtualTimeNanos += SettlePolicy.FRAME_NS
        clock.sendFrame(activity.virtualTimeNanos)
        Snapshot.sendApplyNotifications()
        activity.recomposerFailure.get()?.let { throw it }
      }
      return withContext(Dispatchers.Main) {
        activity.recomposerFailure.get()?.let { throw it }
        measureMainActivityTarget(activity, target.widthPx, target.heightPx)
        activity.window.decorView.getLocationOnScreen(mainLoc)
        val overlays = collectOverlayWindows(activity)
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
        activity.recomposerFailure.get()?.let { throw it }
        FrameStatus(
          workPending = clock.hasAwaiters || recomp.hasPendingWork,
          overlayPresent = overlays.isNotEmpty(),
          awaitingOverlayAttach = awaitingAttach,
        )
      }
    }

    override suspend fun finishSettle() {
      withContext(Dispatchers.Main) {
        activity.recomposerFailure.get()?.let { throw it }
        measureMainActivityTarget(activity, target.widthPx, target.heightPx)
        activity.window.decorView.getLocationOnScreen(mainLoc)
        for (win in collectOverlayWindows(activity)) {
          OverlayWindowHelper.measureAndLayoutOverlayWindow(
            win,
            mainLoc,
            target.widthPx,
            target.heightPx,
          )
        }
        activity.recomposerFailure.get()?.let { throw it }
      }
    }

    override suspend fun scrollOnce(): Boolean =
      withContext(Dispatchers.Main) {
        WearScrollHelper.scrollDown(
          collectUnmergedRoots(activity),
          target.minScrollerWidthPx,
          target.scrollStepPx,
        )
      }
  }
}
