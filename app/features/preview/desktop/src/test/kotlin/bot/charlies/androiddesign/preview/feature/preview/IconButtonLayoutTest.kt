package bot.charlies.androiddesign.preview.feature.preview

import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Dp
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Android's material3 pads every interactive component to 48dp in layout
 * (`minimumInteractiveComponentSize()`), so two stacked default icon buttons are 48dp apart even
 * though each draws 40dp. The desktop build of material3 resolves the same 48dp, so the previewer
 * lays out like a phone as long as it does not pin the default button to its 40dp container.
 */
@OptIn(ExperimentalTestApi::class)
class IconButtonLayoutTest {
  private fun gapBetweenStacked(props: String): Float {
    val tree =
      render(
          singleScreen(
            """{"id":"root","component":"Scaffold","content":"col"},{"id":"col","component":"Column","children":["a","b"]},""" +
              """{"id":"a","component":"IconButton","icon":"undo","contentDescription":"Undo"$props},""" +
              """{"id":"b","component":"IconButton","icon":"redo","contentDescription":"Redo"$props}""",
          ),
        )
        .tree
    return tree.node("b").bounds.y - tree.node("a").bounds.y
  }

  @Test
  fun `the desktop build resolves the 48dp minimum interactive size`() = runComposeUiTest {
    var size: Dp? = null
    setContent { size = LocalMinimumInteractiveComponentSize.current }
    assertEquals(48f, size?.value)
  }

  @Test
  fun `stacked default icon buttons are laid out 48dp apart`() {
    assertEquals(48f, gapBetweenStacked(""), 0.5f)
  }

  @Test
  fun `a sized icon button keeps the container size it asks for`() {
    assertEquals(32f, gapBetweenStacked(""","size":"extra-small""""), 0.5f)
    assertEquals(56f, gapBetweenStacked(""","size":"medium""""), 0.5f)
  }
}
