package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.has
import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.lacks
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A wrapping `FlexBox` measures an item with the largest height `Constraints` can hold, and a
 * Material component that grows its semantics bounds (progress indicators, vertical sliders) adds
 * to that and throws "Can't represent a width of 630 and height of 262194 in Constraints".
 */
class FlexBoxExportTest {
  private val unbounded = "requiredHeightIn(max = Dp.Infinity)"

  private fun export(vararg components: String) =
    ExportFixtures.export(
      """{"id":"root","component":"Column","scroll":true,"children":["flex"]}""",
      *components,
    )

  private fun count(source: String, needle: String) =
    Regex(Regex.escape(needle)).findAll(source).count()

  @Test
  fun `a progress indicator in a flex box gets an unbounded height`() {
    val source =
      export(
        """{"id":"flex","component":"FlexBox","wrap":true,"children":["a","b"]}""",
        """{"id":"a","component":"ProgressIndicator","shape":"linear","progress":0.4}""",
        """{"id":"b","component":"ProgressIndicator","shape":"linear","wavy":true}""",
      )
    has(source, "LinearProgressIndicator(progress = { 0.4f }, modifier = Modifier.$unbounded)")
    has(source, "LinearWavyProgressIndicator(modifier = Modifier.$unbounded)")
    has(source, "import androidx.compose.ui.unit.Dp")
    has(source, "import androidx.compose.foundation.layout.requiredHeightIn")
  }

  @Test
  fun `an item that holds a progress indicator gets it on the item`() {
    val source =
      export(
        """{"id":"flex","component":"FlexBox","wrap":true,"children":["card"]}""",
        """{"id":"card","component":"Card","content":["p"]}""",
        """{"id":"p","component":"ProgressIndicator","shape":"linear"}""",
      )
    assertEquals(1, count(source, unbounded), source)
    has(source, "Card(modifier = Modifier.$unbounded)")
  }

  @Test
  fun `a vertical slider in a flex box gets an unbounded height`() {
    val source =
      export(
        """{"id":"flex","component":"FlexBox","children":["s"]}""",
        """{"id":"s","component":"Slider","orientation":"vertical","value":60,"range":[0,100],"contentDescription":"Brightness"}""",
      )
    assertEquals(1, count(source, unbounded), source)
  }

  @Test
  fun `items that do not grow their semantics bounds are left as they are`() {
    val source =
      export(
        """{"id":"flex","component":"FlexBox","wrap":true,"children":["chip","h"]}""",
        """{"id":"chip","component":"AssistChip","label":"Assist"}""",
        """{"id":"h","component":"Slider","value":40,"range":[0,100],"contentDescription":"Volume"}""",
      )
    lacks(source, unbounded)
  }

  @Test
  fun `a progress indicator outside a flex box is left as it is`() {
    val source =
      ExportFixtures.export(
        """{"id":"root","component":"Column","children":["p"]}""",
        """{"id":"p","component":"ProgressIndicator","shape":"linear"}""",
      )
    lacks(source, unbounded)
  }

  @Test
  fun `a circular progress indicator in a flex box is left as it is`() {
    val source =
      export(
        """{"id":"flex","component":"FlexBox","wrap":true,"children":["a","b"]}""",
        """{"id":"a","component":"ProgressIndicator","shape":"circular"}""",
        """{"id":"b","component":"ProgressIndicator","shape":"circular","wavy":true}""",
      )
    lacks(source, unbounded)
  }
}
