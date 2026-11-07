package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.has
import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.lacks
import kotlin.test.Test

/**
 * `itemSize` becomes a check on `carouselItemDrawInfo` inside the carousel item. The size changes
 * every scroll frame, so the check is a `derivedStateOf`: the item recomposes only when the answer
 * flips, not on every frame.
 */
class CarouselItemSizeExportTest {
  private fun export(titleProps: String, caption: String = ""): String =
    ExportFixtures.export(
      """{"id":"root","component":"Carousel","children":["card"]}""",
      """{"id":"card","component":"Card","content":["col"]}""",
      """{"id":"col","component":"Column","children":["title","detail"]}""",
      """{"id":"title","component":"Text","text":"Dinner"$titleProps}""",
      """{"id":"detail","component":"Text","text":"Ready in 20 min"$caption}""",
    )

  @Test
  fun `a large node is written under a derived check for the item at full size`() {
    val source = export(""","itemSize":"large"""")
    has(
      source,
      "val titleShown by remember { derivedStateOf { " +
        "carouselItemDrawInfo.size >= carouselItemDrawInfo.maxSize * 0.85f } }",
    )
    has(source, "if (titleShown) {")
    has(source, "Text(\"Dinner\"")
    has(source, "import androidx.compose.runtime.derivedStateOf")
    has(source, "import androidx.compose.runtime.getValue")
    lacks(source, "itemSize")
    lacks(source, "LocalDensity")
  }

  @Test
  fun `a medium node reads the small item width once, outside the derived state`() {
    val source = export(""","itemSize":"medium"""")
    has(source, "val titleSmallPx = with(LocalDensity.current) { 56.dp.toPx() }")
    has(
      source,
      "val titleShown by remember(titleSmallPx) { derivedStateOf { " +
        "carouselItemDrawInfo.size >= carouselItemDrawInfo.maxSize * 0.85f || " +
        "carouselItemDrawInfo.size > titleSmallPx } }",
    )
    has(source, "if (titleShown) {")
    has(source, "import androidx.compose.ui.platform.LocalDensity")
  }

  @Test
  fun `only the gated node sits under the check`() {
    val source = export(""","itemSize":"large"""")
    val gated = source.substringAfter("if (titleShown) {").substringBefore("}")
    has(gated, "Text(\"Dinner\"")
    lacks(gated, "Ready in 20 min")
  }

  @Test
  fun `a node without itemSize has no check`() {
    lacks(export(""), "carouselItemDrawInfo")
  }
}
