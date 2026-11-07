package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.has
import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.lacks
import kotlin.test.Test

/** `itemSize` becomes an `if` on `carouselItemDrawInfo` inside the carousel item. */
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
  fun `a large node is written under a check for the item at full size`() {
    val source = export(""","itemSize":"large"""")
    has(source, "if (carouselItemDrawInfo.size >= carouselItemDrawInfo.maxSize * 0.85f) {")
    has(source, "Text(\"Dinner\"")
    lacks(source, "itemSize")
    lacks(source, "LocalDensity")
  }

  @Test
  fun `a medium node is written under a check that the item is not the smallest`() {
    val source = export(""","itemSize":"medium"""")
    // The condition is long, so it breaks after the `||`.
    has(
      source,
      "carouselItemDrawInfo.size >= carouselItemDrawInfo.maxSize * 0.85f || " +
        "carouselItemDrawInfo.size > with(LocalDensity.current) { 56.dp.toPx() }",
    )
    has(source, "import androidx.compose.ui.platform.LocalDensity")
  }

  @Test
  fun `only the gated node sits under the check`() {
    val source = export(""","itemSize":"large"""")
    val gated = source.substringAfter("maxSize * 0.85f) {").substringBefore("}")
    has(gated, "Text(\"Dinner\"")
    lacks(gated, "Ready in 20 min")
  }

  @Test
  fun `a node without itemSize has no check`() {
    lacks(export(""), "carouselItemDrawInfo")
  }
}
