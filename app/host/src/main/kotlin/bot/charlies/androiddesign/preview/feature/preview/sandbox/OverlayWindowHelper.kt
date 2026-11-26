package bot.charlies.androiddesign.preview.feature.preview.sandbox

import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.runtime.tooling.CompositionGroup
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutInfo
import androidx.compose.ui.node.RootForTest
import androidx.compose.ui.semantics.SemanticsNode
import java.util.IdentityHashMap

/** Android view-hierarchy helpers used by layoutlib tree extraction and overlay window offsets. */
@OptIn(ComposeToolingApi::class)
object OverlayWindowHelper {
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
