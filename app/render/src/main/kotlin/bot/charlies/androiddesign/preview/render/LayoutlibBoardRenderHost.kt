package bot.charlies.androiddesign.preview.render

import android.view.View
import android.view.ViewGroup
import android.view.ViewRootImpl_Accessor
import android.view.WindowManagerGlobal
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.runtime.tooling.LocalInspectionTables
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.ComposeView
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.PaparazziSdk
import bot.charlies.androiddesign.preview.feature.preview.sandbox.BoardRenderHost
import bot.charlies.androiddesign.preview.feature.preview.sandbox.CapturedFrame
import bot.charlies.androiddesign.preview.feature.preview.sandbox.FrameStatus
import bot.charlies.androiddesign.preview.feature.preview.sandbox.MountedRenderSession
import bot.charlies.androiddesign.preview.feature.preview.sandbox.OverlayWindowHelper
import bot.charlies.androiddesign.preview.feature.preview.sandbox.PreparedRenderTarget
import bot.charlies.androiddesign.preview.feature.preview.sandbox.RawRgbaFrame
import bot.charlies.androiddesign.preview.feature.preview.sandbox.RenderBoardContent
import bot.charlies.androiddesign.preview.feature.preview.sandbox.RenderWindowSnapshot
import bot.charlies.androiddesign.preview.feature.preview.sandbox.SettlePolicy
import bot.charlies.androiddesign.preview.feature.preview.sandbox.WearScrollDriver
import bot.charlies.androiddesign.preview.feature.preview.sandbox.WearScrollHelper
import bot.charlies.androiddesign.preview.model.Device
import com.android.resources.Density
import com.android.resources.NightMode
import com.android.resources.ScreenOrientation
import com.android.resources.ScreenRound
import com.android.resources.ScreenSize
import com.android.resources.UiMode
import java.awt.image.BufferedImage
import java.util.IdentityHashMap

/**
 * [BoardRenderHost] backed by layoutlib via [PaparazziSdk]. Every target gets a fresh
 * `RenderSession`, zero insets on all windows, and real-time coroutine delays over a virtual frame
 * clock driven by [SettlePolicy].
 */
@OptIn(ComposeToolingApi::class, InternalComposeApi::class)
internal class LayoutlibBoardRenderHost(private val sdk: PaparazziSdk) : BoardRenderHost {
  private val navOwner =
    object : NavigationEventDispatcherOwner {
      override val navigationEventDispatcher = NavigationEventDispatcher()
    }

  override suspend fun <T> withMountedTarget(
    target: PreparedRenderTarget,
    inspectionTables: MutableSet<CompositionData>,
    onRootCoordinates: (LayoutCoordinates) -> Unit,
    onEmptyDetail: (String?) -> Unit,
    block: suspend (MountedRenderSession) -> T,
  ): T {
    sdk.resetSession(deviceConfigFor(target))

    val (overrideCtx, overrideCfg) = target.createOverrideContext(sdk.context)
    val composeView =
      ComposeView(sdk.context).apply {
        layoutParams =
          ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
          )
        setContent {
          currentComposer.collectParameterInformation()
          inspectionTables.add(currentComposer.compositionData)
          CompositionLocalProvider(LocalInspectionTables provides inspectionTables) {
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
        }
      }

    return sdk.openSession(composeView).use { driverSession ->
      var frameIndex = 0
      var lastFrameTimeNanos = 0L

      val driver =
        object : WearScrollDriver {
          override suspend fun advanceFrame(): FrameStatus {
            val nowNanos = frameIndex * SettlePolicy.FRAME_NS
            lastFrameTimeNanos = nowNanos
            frameIndex++
            val status = driverSession.advanceFrame(nowNanos)
            val secondaryWindows = windowViewsOrFallback(composeView).drop(1)
            val awaitingAttach = secondaryWindows.any { !it.isAttachedToWindow || it.width == 0 }
            return FrameStatus(
              workPending = status.hasPendingWork,
              overlayPresent = secondaryWindows.isNotEmpty(),
              awaitingOverlayAttach = awaitingAttach,
            )
          }

          override suspend fun finishSettle() {
            // captureFrame redraws at lastFrameTimeNanos.
          }

          override suspend fun scrollOnce(): Boolean {
            val roots =
              OverlayWindowHelper.collectComposeRoots(windowViewsOrFallback(composeView)).map {
                it.semanticsOwner.unmergedRootSemanticsNode
              }
            return WearScrollHelper.scrollDown(
              roots = roots,
              minWidthPx = target.minScrollerWidthPx,
              stepPx = target.scrollStepPx,
            )
          }
        }

      val mounted =
        object : MountedRenderSession {
          override val driver: WearScrollDriver = driver

          override suspend fun captureFrame(): CapturedFrame {
            val image = driverSession.captureFrame(lastFrameTimeNanos)
            val raw = image.toRawRgba()
            val explicitOffsets = IdentityHashMap<View, Offset>()
            for (win in windowViewsOrFallback(composeView).drop(1)) {
              val root = win.viewRootImpl ?: continue
              val winFrame = ViewRootImpl_Accessor.getWindowFrame(root) ?: continue
              explicitOffsets[win] = Offset(winFrame.left.toFloat(), winFrame.top.toFloat())
            }
            return CapturedFrame(frame = raw, explicitWindowOffsets = explicitOffsets)
          }

          override suspend fun <R> onUiThread(block: () -> R): R = block()

          override fun currentWindows(): RenderWindowSnapshot {
            val windows = windowViewsOrFallback(composeView)
            return RenderWindowSnapshot(mainDecorView = windows.first(), activeWindows = windows)
          }
        }

      block(mounted)
    }
  }

  private fun windowViewsOrFallback(fallback: View): List<View> {
    val windows = WindowManagerGlobal.getInstance().windowViews
    return if (windows.isNotEmpty()) windows else listOf(fallback)
  }

  companion object {
    fun deviceConfigFor(target: PreparedRenderTarget): DeviceConfig {
      val view = target.effectiveView
      val w = target.widthPx
      val h = target.heightPx
      val isWatch = view.device == Device.Watch
      return DeviceConfig(
        screenHeight = h,
        screenWidth = w,
        xdpi = target.dpi,
        ydpi = target.dpi,
        orientation = if (w > h) ScreenOrientation.LANDSCAPE else ScreenOrientation.PORTRAIT,
        uiMode = if (isWatch) UiMode.WATCH else UiMode.NORMAL,
        nightMode = if (view.dark) NightMode.NIGHT else NightMode.NOTNIGHT,
        density = Density.create(target.dpi),
        fontScale = target.effectiveFontScale,
        size =
          when {
            isWatch -> ScreenSize.SMALL
            view.width >= 840 -> ScreenSize.XLARGE
            view.width >= 600 -> ScreenSize.LARGE
            else -> ScreenSize.NORMAL
          },
        softButtons = false,
        screenRound = if (view.round) ScreenRound.ROUND else null,
      )
    }
  }
}

internal fun BufferedImage.toRawRgba(): RawRgbaFrame {
  val out = ByteArray(width * height * 4)
  var i = 0
  for (y in 0 until height) {
    for (x in 0 until width) {
      val argb = getRGB(x, y)
      out[i++] = (argb ushr 16).toByte()
      out[i++] = (argb ushr 8).toByte()
      out[i++] = argb.toByte()
      out[i++] = (argb ushr 24).toByte()
    }
  }
  return RawRgbaFrame(width = width, height = height, bytes = out)
}
