package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.has
import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.lacks
import kotlin.test.Test
import kotlin.test.assertEquals

/** A top app bar collapses in the export in the window sizes where the preview collapses it. */
class CollapsingAppBarExportTest {
  private fun export(bar: String) =
    ExportFixtures.flat(
      ExportFixtures.export(
        """{"id":"root","component":"Scaffold","topBar":"bar","content":"c"}""",
        bar,
        """{"id":"c","component":"Column","scroll":true,"children":[]}""",
      ),
    )

  private fun count(text: String, part: String) = text.windowed(part.length).count { it == part }

  @Test
  fun `a bar that is large at every size collapses at every size`() {
    val source = export("""{"id":"bar","component":"TopAppBar","size":"large","title":"T"}""")
    has(source, "val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()")
    has(source, "modifier.nestedScroll(scrollBehavior.nestedScrollConnection)")
    has(source, "scrollBehavior = scrollBehavior")
    lacks(source, "atLeast")
  }

  @Test
  fun `a bar that is small until it is large at expanded collapses only at expanded`() {
    val source =
      export(
        """{"id":"bar","component":"TopAppBar","size":"small","title":"T","sizeClass":{"expanded":{"size":"large"}}}""",
      )
    has(source, "val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()")
    has(
      source,
      "modifier.then(if (atLeastExpanded) Modifier.nestedScroll(scrollBehavior.nestedScrollConnection) else Modifier)",
    )
    assertEquals(1, count(source, "scrollBehavior = scrollBehavior"), source)
    has(source, "LargeFlexibleTopAppBar(")
  }

  @Test
  fun `a bar that is large only at compact never gives the plain bar the behavior`() {
    val source =
      export(
        """{"id":"bar","component":"TopAppBar","size":"large","title":"T","sizeClass":{"medium":{"size":"small"},"expanded":{"size":"small"}}}""",
      )
    has(
      source,
      "modifier.then(if (!atLeastMedium) Modifier.nestedScroll(scrollBehavior.nestedScrollConnection) else Modifier)",
    )
    assertEquals(1, count(source, "scrollBehavior = scrollBehavior"), source)
    has(source, "LargeFlexibleTopAppBar(")
  }
}
