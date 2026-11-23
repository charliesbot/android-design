package bot.charlies.androiddesign.preview.feature.preview.sandbox

import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.runtime.tooling.CompositionGroup
import androidx.compose.runtime.tooling.parseSourceInformation
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import bot.charlies.androiddesign.preview.model.LayoutTree

/** Shared Wear scrolling and circular-picker detection for both `:sandbox` and Robolectric. */
@OptIn(ComposeToolingApi::class)
internal object WearScrollHelper {
  private const val MAX_FINITE_SCROLL_EXTENT_PX = 50_000f
  private val PICKER_FUNCTIONS = setOf("DatePicker", "TimePicker", "Picker", "PickerGroup")
  private val PICKER_SOURCE_FILES = setOf("DatePicker.kt", "TimePicker.kt", "Picker.kt")

  fun hasWearPicker(tables: Set<CompositionData>, tree: LayoutTree): Boolean {
    if (tree.nodes.any { it.type == "DatePicker" || it.type == "TimePicker" }) return true
    fun visit(group: CompositionGroup): Boolean {
      val parsed = group.sourceInfo?.takeIf { it.isNotEmpty() }?.let(::parseSourceInformation)
      val fnName = parsed?.functionName
      val srcFile = parsed?.sourceFile?.substringAfterLast('/')
      if (fnName in PICKER_FUNCTIONS || srcFile in PICKER_SOURCE_FILES) {
        return true
      }
      for (child in group.compositionGroups) {
        if (visit(child)) return true
      }
      return false
    }
    return tables.any { table -> table.compositionGroups.any { visit(it) } }
  }

  fun findVerticalScroller(roots: List<SemanticsNode>, minWidthPx: Float): SemanticsNode? {
    for (root in roots) {
      val range = root.config.getOrNull(SemanticsProperties.VerticalScrollAxisRange)
      if (
        range != null &&
          root.config.contains(SemanticsActions.ScrollBy) &&
          range.maxValue().isFinite() &&
          range.maxValue() <= MAX_FINITE_SCROLL_EXTENT_PX &&
          root.size.width >= minWidthPx
      ) {
        return root
      }
      findVerticalScroller(root.children, minWidthPx)?.let {
        return it
      }
    }
    return null
  }
}
