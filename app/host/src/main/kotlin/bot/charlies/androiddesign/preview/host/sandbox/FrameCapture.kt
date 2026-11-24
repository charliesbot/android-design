package bot.charlies.androiddesign.preview.host.sandbox

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.HardwareRenderer
import android.graphics.PixelFormat
import android.graphics.RenderNode
import android.hardware.HardwareBuffer
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inspector.WindowInspector
import androidx.compose.ui.geometry.Offset
import bot.charlies.androiddesign.preview.feature.preview.sandbox.CapturedFrame
import bot.charlies.androiddesign.preview.feature.preview.sandbox.OverlayWindowHelper
import bot.charlies.androiddesign.preview.feature.preview.sandbox.RawRgbaFrame
import bot.charlies.androiddesign.preview.host.LivePreviewActivity
import bot.charlies.androiddesign.preview.model.DeviceRenderTarget
import bot.charlies.androiddesign.preview.model.DeviceWireFrame
import bot.charlies.androiddesign.preview.model.FrameFormat
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import net.jpountz.lz4.LZ4Factory

/** Frame readback and encoding for the offscreen host: HardwareRenderer into an ImageReader. */
internal class FrameCapture {
  private val lz4Compressor = LZ4Factory.fastestInstance().fastCompressor()
  private val mainHandler = Handler(Looper.getMainLooper())
  private var hardwareRenderer: HardwareRenderer? = null
  private var renderNode: RenderNode? = null
  private var hwImageReader: ImageReader? = null
  private var hwWidth: Int = 0
  private var hwHeight: Int = 0

  suspend fun capture(
    activity: HostRenderActivity,
    widthPx: Int,
    heightPx: Int,
  ): CapturedFrame {
    if (hardwareRenderer == null) {
      hardwareRenderer = HardwareRenderer()
      renderNode = RenderNode("adp-frame")
    }
    if (hwImageReader == null || hwWidth != widthPx || hwHeight != heightPx) {
      hwImageReader?.close()
      hwImageReader =
        ImageReader.newInstance(
          widthPx,
          heightPx,
          PixelFormat.RGBA_8888,
          2,
          HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE or
            HardwareBuffer.USAGE_GPU_COLOR_OUTPUT or
            HardwareBuffer.USAGE_CPU_READ_OFTEN,
        )
      hwWidth = widthPx
      hwHeight = heightPx
      hardwareRenderer!!.setSurface(hwImageReader!!.surface)
    }
    val rn = renderNode!!
    val hr = hardwareRenderer!!
    val reader = hwImageReader!!

    while (true) {
      val stale = reader.acquireLatestImage() ?: break
      stale.close()
    }

    lateinit var overlayOffsets: Map<View, Offset>
    withContext(Dispatchers.Main) {
      measureMainActivityTarget(activity, widthPx, heightPx)
      val view = checkNotNull(activity.composeView)
      val overlays = collectOverlayWindows(activity)

      rn.setPosition(0, 0, widthPx, heightPx)
      val canvas = rn.beginRecording(widthPx, heightPx)
      try {
        overlayOffsets =
          OverlayWindowHelper.drawWindowComposite(
            canvas = canvas,
            mainView = view,
            mainDecorView = activity.window.decorView,
            overlays = overlays,
            widthPx = widthPx,
            heightPx = heightPx,
          )
      } finally {
        rn.endRecording()
      }
      hr.setContentRoot(rn)
    }

    val syncStatus = hr.createRenderRequest().setWaitForPresent(true).syncAndDraw()
    check(
      syncStatus == HardwareRenderer.SYNC_OK || syncStatus == HardwareRenderer.SYNC_FRAME_DROPPED,
    ) {
      "HardwareRenderer syncAndDraw status=$syncStatus"
    }
    val image = acquireLatestOrWaitImage(reader)
    return CapturedFrame(copyImageToRgba(image, widthPx, heightPx), overlayOffsets)
  }

  suspend fun acquireLatestOrWaitImage(
    reader: ImageReader,
    timeoutMs: Long = 1000L,
  ): Image {
    reader.acquireLatestImage()?.let {
      return it
    }
    return suspendCancellableCoroutine { cont ->
      val done = AtomicBoolean(false)
      val timeoutRunnable = Runnable {
        if (done.compareAndSet(false, true)) {
          reader.setOnImageAvailableListener(null, null)
          val fallback = reader.acquireLatestImage()
          if (fallback != null) {
            cont.resume(fallback)
          } else {
            cont.cancel(
              IllegalStateException("Timed out waiting ${timeoutMs}ms for ImageReader frame"),
            )
          }
        }
      }
      mainHandler.postDelayed(timeoutRunnable, timeoutMs)
      reader.setOnImageAvailableListener(
        { r ->
          val img = r.acquireLatestImage()
          if (img != null && done.compareAndSet(false, true)) {
            mainHandler.removeCallbacks(timeoutRunnable)
            r.setOnImageAvailableListener(null, null)
            cont.resume(img)
          } else {
            img?.close()
          }
        },
        mainHandler,
      )
      cont.invokeOnCancellation {
        mainHandler.removeCallbacks(timeoutRunnable)
        reader.setOnImageAvailableListener(null, null)
      }
    }
  }

  private fun copyImageToRgba(image: Image, expectedW: Int, expectedH: Int): RawRgbaFrame {
    try {
      val plane = image.planes[0]
      val buf = plane.buffer
      val rowStride = plane.rowStride
      val pixelStride = plane.pixelStride
      val w = minOf(image.width, expectedW)
      val h = minOf(image.height, expectedH)
      val out = ByteArray(w * h * 4)
      val expectedRowBytes = w * 4
      if (pixelStride == 4 && rowStride == expectedRowBytes) {
        buf.position(0)
        buf.get(out, 0, out.size)
      } else {
        val rowTmp = ByteArray(rowStride)
        for (y in 0 until h) {
          buf.position(y * rowStride)
          val toRead = minOf(rowStride, buf.remaining())
          buf.get(rowTmp, 0, toRead)
          if (pixelStride == 4) {
            System.arraycopy(rowTmp, 0, out, y * expectedRowBytes, expectedRowBytes)
          } else {
            var dst = y * expectedRowBytes
            var src = 0
            for (x in 0 until w) {
              out[dst++] = rowTmp[src]
              out[dst++] = rowTmp[src + 1]
              out[dst++] = rowTmp[src + 2]
              out[dst++] = rowTmp[src + 3]
              src += pixelStride
            }
          }
        }
      }
      return RawRgbaFrame(w, h, out)
    } finally {
      image.close()
    }
  }

  fun encodeFrame(
    target: DeviceRenderTarget,
    viewName: String,
    isEndFrame: Boolean,
    frame: RawRgbaFrame,
  ): DeviceWireFrame {
    val encodedBytes =
      when (target.frameFormat) {
        FrameFormat.LZ4_RGBA -> compressLz4(frame.bytes)
        FrameFormat.WEBP_LOSSLESS -> compressWebpLossless(frame)
        FrameFormat.NONE -> ByteArray(0)
      }
    return DeviceWireFrame(
      direction = target.direction,
      screen = target.screen,
      viewName = viewName,
      overlayId = target.overlayId,
      isEndFrame = isEndFrame,
      format = target.frameFormat,
      widthPx = frame.width,
      heightPx = frame.height,
      rawLength = frame.bytes.size,
      payload = encodedBytes,
    )
  }

  private fun compressLz4(raw: ByteArray): ByteArray {
    val maxLen = lz4Compressor.maxCompressedLength(raw.size)
    val dst = ByteArray(maxLen)
    val len = lz4Compressor.compress(raw, 0, raw.size, dst, 0, maxLen)
    return dst.copyOf(len)
  }

  private fun compressWebpLossless(frame: RawRgbaFrame): ByteArray {
    val bmp = Bitmap.createBitmap(frame.width, frame.height, Bitmap.Config.ARGB_8888)
    try {
      bmp.copyPixelsFromBuffer(ByteBuffer.wrap(frame.bytes))
      val out = ByteArrayOutputStream(frame.width * frame.height / 2)
      check(bmp.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 0, out)) {
        "Bitmap.compress(WEBP_LOSSLESS) returned false"
      }
      return out.toByteArray()
    } finally {
      bmp.recycle()
    }
  }
}

internal fun measureMainActivityTarget(activity: HostRenderActivity, widthPx: Int, heightPx: Int) {
  val wSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
  val hSpec = View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY)
  val decor = activity.window.decorView
  if (decor.isLayoutRequested || decor.width != widthPx || decor.height != heightPx) {
    decor.measure(wSpec, hSpec)
    decor.layout(0, 0, widthPx, heightPx)
  }
  activity.container?.let { c ->
    if (c.isLayoutRequested || c.width != widthPx || c.height != heightPx) {
      c.measure(wSpec, hSpec)
      c.layout(0, 0, widthPx, heightPx)
    }
  }
  activity.composeView?.let { cv ->
    if (cv.isLayoutRequested || cv.width != widthPx || cv.height != heightPx) {
      cv.measure(wSpec, hSpec)
      cv.layout(0, 0, widthPx, heightPx)
    }
  }
}

internal fun collectOverlayWindows(activity: HostRenderActivity): List<View> {
  val mainDecor = activity.window.decorView
  return WindowInspector.getGlobalWindowViews().filter { win ->
    win !== mainDecor && win.visibility == View.VISIBLE && belongsToOffscreenTree(activity, win)
  }
}

internal fun belongsToOffscreenTree(activity: HostRenderActivity, win: View): Boolean {
  if (win === activity.window.decorView) return true
  if (win === LivePreviewActivity.currentDecorView()) return false
  val lp = win.layoutParams as? WindowManager.LayoutParams
  if (lp?.type == WindowManager.LayoutParams.TYPE_BASE_APPLICATION) return false
  val liveToken = LivePreviewActivity.currentWindowToken()
  if (lp?.token != null && lp.token == liveToken) return false
  val hostToken = activity.window.decorView.windowToken
  if (lp?.token != null && lp.token == hostToken) return true
  return checkSubtreeContext(win) ?: false
}

internal fun checkSubtreeContext(view: View): Boolean? {
  var cur: Context? = view.context
  while (cur is ContextWrapper) {
    if (cur is HostRenderActivity) return true
    if (cur is LivePreviewActivity) return false
    cur = cur.baseContext
  }
  if (view is ViewGroup) {
    for (i in 0 until view.childCount) {
      checkSubtreeContext(view.getChildAt(i))?.let {
        return it
      }
    }
  }
  return null
}
