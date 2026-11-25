package bot.charlies.androiddesign.preview.host.sandbox

import android.graphics.Canvas
import android.graphics.HardwareRenderer
import android.graphics.PixelFormat
import android.graphics.RenderNode
import android.hardware.HardwareBuffer
import android.media.Image
import android.media.ImageReader
import bot.charlies.androiddesign.preview.feature.preview.sandbox.RawRgbaFrame

/**
 * Draws a frame through `HardwareRenderer` into an `ImageReader`, because a software
 * `Canvas(bitmap)` skips `RenderEffect`s such as `Modifier.blur`.
 */
internal class HardwareFrameRenderer {
  private val renderer = HardwareRenderer()
  private val node = RenderNode("adp-frame")
  private var reader: ImageReader? = null
  private var width = 0
  private var height = 0

  /** Returns the reader for this size, creating it when the size changes. */
  fun prepare(widthPx: Int, heightPx: Int): ImageReader {
    val current = reader
    return if (current != null && width == widthPx && height == heightPx) {
      current
    } else {
      current?.close()
      ImageReader.newInstance(
          widthPx,
          heightPx,
          PixelFormat.RGBA_8888,
          2,
          HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE or
            HardwareBuffer.USAGE_GPU_COLOR_OUTPUT or
            HardwareBuffer.USAGE_CPU_READ_OFTEN,
        )
        .also {
          reader = it
          width = widthPx
          height = heightPx
          renderer.setSurface(it.surface)
        }
    }
  }

  /** Records [draw] as the next frame's content. Call on the UI thread after [prepare]. */
  fun <T> record(draw: (Canvas) -> T): T {
    node.setPosition(0, 0, width, height)
    val canvas = node.beginRecording(width, height)
    val result =
      try {
        draw(canvas)
      } finally {
        node.endRecording()
      }
    renderer.setContentRoot(node)
    return result
  }

  /** Renders the recorded frame into the reader and waits until it is presented. */
  fun syncAndDraw() {
    val status = renderer.createRenderRequest().setWaitForPresent(true).syncAndDraw()
    check(status == HardwareRenderer.SYNC_OK || status == HardwareRenderer.SYNC_FRAME_DROPPED) {
      "HardwareRenderer syncAndDraw status=$status"
    }
  }
}

/** Copies the image into tightly packed RGBA bytes and closes it. */
internal fun Image.toRawRgbaFrame(expectedW: Int, expectedH: Int): RawRgbaFrame {
  try {
    val plane = planes[0]
    val buf = plane.buffer
    val rowStride = plane.rowStride
    val pixelStride = plane.pixelStride
    val w = minOf(width, expectedW)
    val h = minOf(height, expectedH)
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
    close()
  }
}
