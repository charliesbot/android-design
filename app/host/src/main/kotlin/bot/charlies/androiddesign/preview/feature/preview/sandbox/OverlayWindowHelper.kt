package bot.charlies.androiddesign.preview.feature.preview.sandbox

import android.content.Context
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.runtime.tooling.CompositionGroup
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutInfo
import androidx.compose.ui.node.RootForTest
import androidx.compose.ui.semantics.SemanticsNode
import java.util.IdentityHashMap

/** Container that absorbs physical display window insets so offscreen Compose trees never shift. */
internal class InsetConsumingFrameLayout(context: Context) : FrameLayout(context) {
  override fun dispatchApplyWindowInsets(insets: WindowInsets): WindowInsets = WindowInsets.CONSUMED

  override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets = WindowInsets.CONSUMED
}

internal data class OverlayMeasureResult(val offset: Offset, val width: Int, val height: Int)

/**
 * Shared Android view-hierarchy helpers used identically by `:sandbox` offscreen capture and the
 * headless Robolectric Native Graphics renderer.
 */
@OptIn(ComposeToolingApi::class)
internal object OverlayWindowHelper {
  fun forceLayoutTree(v: View) {
    v.forceLayout()
    if (v is ViewGroup) {
      for (i in 0 until v.childCount) {
        forceLayoutTree(v.getChildAt(i))
      }
    }
  }

  fun measureAndLayoutOverlayWindow(
    win: View,
    mainLoc: IntArray,
    widthPx: Int,
    heightPx: Int,
  ): OverlayMeasureResult {
    val lp = win.layoutParams as? WindowManager.LayoutParams
    fun specFor(lpDim: Int, maxSize: Int): Int =
      when {
        lpDim == ViewGroup.LayoutParams.MATCH_PARENT ->
          View.MeasureSpec.makeMeasureSpec(maxSize, View.MeasureSpec.EXACTLY)
        lpDim == ViewGroup.LayoutParams.WRAP_CONTENT ->
          View.MeasureSpec.makeMeasureSpec(maxSize, View.MeasureSpec.AT_MOST)
        lpDim > 0 ->
          View.MeasureSpec.makeMeasureSpec(lpDim.coerceAtMost(maxSize), View.MeasureSpec.EXACTLY)
        else -> View.MeasureSpec.makeMeasureSpec(maxSize, View.MeasureSpec.AT_MOST)
      }
    forceLayoutTree(win)
    val wSpec = specFor(lp?.width ?: ViewGroup.LayoutParams.WRAP_CONTENT, widthPx)
    val hSpec = specFor(lp?.height ?: ViewGroup.LayoutParams.WRAP_CONTENT, heightPx)
    win.measure(wSpec, hSpec)
    var w = win.measuredWidth.coerceIn(0, widthPx)
    var h = win.measuredHeight.coerceIn(0, heightPx)
    if ((w == 0 || h == 0) && win is ViewGroup && win.childCount > 0) {
      val child = win.getChildAt(0)
      forceLayoutTree(child)
      child.measure(
        View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.AT_MOST),
        View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.AT_MOST),
      )
      val cw = child.measuredWidth.coerceIn(0, widthPx)
      val ch = child.measuredHeight.coerceIn(0, heightPx)
      if (cw > 0 && ch > 0) {
        w = cw
        h = ch
        win.layout(0, 0, w, h)
        child.layout(0, 0, w, h)
      } else {
        win.layout(0, 0, w, h)
      }
    } else {
      win.layout(0, 0, w, h)
    }

    if (
      lp != null &&
        lp.type in
          WindowManager.LayoutParams.FIRST_SUB_WINDOW..WindowManager.LayoutParams.LAST_SUB_WINDOW
    ) {
      val left = (lp.x - mainLoc[0]).coerceIn(0, (widthPx - w).coerceAtLeast(0))
      val top = (lp.y - mainLoc[1]).coerceIn(0, (heightPx - h).coerceAtLeast(0))
      return OverlayMeasureResult(Offset(left.toFloat(), top.toFloat()), w, h)
    }

    val containerRect = Rect(0, 0, widthPx, heightPx)
    val outRect = Rect()
    Gravity.apply(lp?.gravity ?: Gravity.CENTER, w, h, containerRect, outRect)
    val left = (outRect.left + (lp?.x ?: 0)).coerceIn(0, (widthPx - w).coerceAtLeast(0))
    val top = (outRect.top + (lp?.y ?: 0)).coerceIn(0, (heightPx - h).coerceAtLeast(0))
    return OverlayMeasureResult(Offset(left.toFloat(), top.toFloat()), w, h)
  }

  fun collectComposeRoots(windows: Iterable<View>): List<RootForTest> {
    val roots = mutableListOf<RootForTest>()
    fun walk(v: View) {
      if (v is RootForTest) roots += v
      if (v is ViewGroup) {
        for (i in 0 until v.childCount) walk(v.getChildAt(i))
      }
    }
    for (win in windows) {
      walk(win)
    }
    return roots
  }

  fun buildLayoutWindowOffsets(
    mainDecorView: View,
    composeRoots: List<RootForTest>,
    tables: Iterable<CompositionData>,
    explicitWindowOffsets: Map<View, Offset>,
  ): IdentityHashMap<LayoutInfo, Offset> {
    val mainLoc = IntArray(2)
    mainDecorView.getLocationOnScreen(mainLoc)

    val rootLayoutOffsets = IdentityHashMap<LayoutInfo, Offset>()
    for (r in composeRoots) {
      val rv = r as? View ?: continue
      var winRoot: View = rv
      while (winRoot.parent is View) {
        winRoot = winRoot.parent as View
      }
      if (winRoot !== mainDecorView) {
        val offset =
          explicitWindowOffsets[winRoot]
            ?: run {
              val vLoc = IntArray(2)
              winRoot.getLocationOnScreen(vLoc)
              Offset((vLoc[0] - mainLoc[0]).toFloat(), (vLoc[1] - mainLoc[1]).toFloat())
            }
        rootLayoutOffsets[r.semanticsOwner.unmergedRootSemanticsNode.layoutInfo] = offset
        rootLayoutOffsets[r.semanticsOwner.rootSemanticsNode.layoutInfo] = offset
      }
    }

    val layoutWindowOffsets = IdentityHashMap<LayoutInfo, Offset>()
    fun resolveOffset(li: LayoutInfo): Offset? {
      var cur: LayoutInfo? = li
      while (cur != null) {
        rootLayoutOffsets[cur]?.let {
          return it
        }
        cur = cur.parentInfo
      }
      return null
    }
    fun indexSemanticsOffsets(node: SemanticsNode) {
      resolveOffset(node.layoutInfo)?.let { layoutWindowOffsets[node.layoutInfo] = it }
      node.children.forEach(::indexSemanticsOffsets)
    }
    for (r in composeRoots) {
      indexSemanticsOffsets(r.semanticsOwner.unmergedRootSemanticsNode)
      indexSemanticsOffsets(r.semanticsOwner.rootSemanticsNode)
    }
    fun indexGroupOffsets(g: CompositionGroup) {
      (g.node as? LayoutInfo)?.let { li ->
        resolveOffset(li)?.let { layoutWindowOffsets[li] = it }
      }
      g.compositionGroups.forEach(::indexGroupOffsets)
    }
    for (table in tables) {
      table.compositionGroups.forEach(::indexGroupOffsets)
    }
    return layoutWindowOffsets
  }
}
