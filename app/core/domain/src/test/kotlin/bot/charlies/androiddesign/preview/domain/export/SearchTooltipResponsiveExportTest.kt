package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.has
import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.lacks
import kotlin.test.Test

/** Search views and tooltips follow their `sizeClass` overrides in the exported code. */
class SearchTooltipResponsiveExportTest {
  private fun flat(text: String) =
    text.replace(Regex("\\s+"), " ").replace(", )", ")").replace("( ", "(").replace(" .", ".")

  private fun export(vararg components: String) = flat(ExportFixtures.export(*components))

  private fun search(viewOverride: String) =
    export(
      """{"id":"root","component":"Column","children":["bar"]}""",
      """{"id":"bar","component":"SearchBar","placeholder":"Search","expanded":"results"}""",
      """{"id":"results","component":"SearchView","layout":"full-screen"$viewOverride,"content":[]}""",
    )

  @Test
  fun `a view that is docked at medium and up exports both layouts by window width`() {
    val source = search(""","sizeClass":{"medium":{"layout":"docked"}}""")
    has(
      source,
      "val barSearchState = if (atLeastMedium) rememberSearchBarWithGapState() else rememberContainedSearchBarState()",
    )
    has(source, "if (atLeastMedium) {")
    has(source, "ExpandedDockedSearchBarWithGap(")
    has(source, "ExpandedFullScreenContainedSearchBar(")
    has(source, "SearchBarDefaults.colors()")
    has(source, "SearchBarDefaults.containedColors(state = barSearchState)")
  }

  @Test
  fun `a view with one layout at every size exports that layout alone`() {
    val source = search("")
    has(source, "val barSearchState = rememberContainedSearchBarState()")
    lacks(source, "ExpandedDockedSearchBarWithGap(")
    lacks(source, "atLeastMedium")
  }

  private fun tooltip(extra: String) =
    export(
      """{"id":"root","component":"Column","children":["info"]}""",
      """{"id":"info","component":"IconButton","icon":"info","contentDescription":"Info"}""",
      """{"id":"tip","component":"Tooltip","anchor":"info","text":"Hi"$extra}""",
    )

  @Test
  fun `a tooltip that is rich at medium and up exports both variants by window width`() {
    val source = tooltip(""","sizeClass":{"medium":{"variant":"rich","title":"T"}}""")
    has(source, "PlainTooltip {")
    has(source, "RichTooltip(")
    has(source, "if (atLeastMedium) {")
  }

  @Test
  fun `only a rich tooltip with buttons is persistent`() {
    val buttons = """,{"id":"ok","component":"Button","variant":"text","label":"Got it"}"""
    val plain = tooltip("")
    lacks(plain, "isPersistent")
    val noActions = tooltip(""","variant":"rich","title":"T"""")
    has(noActions, "val tipTooltipState = rememberTooltipState()")
    lacks(noActions, "isPersistent")
    val withActions =
      flat(
        ExportFixtures.export(
          """{"id":"root","component":"Column","children":["info"]}""",
          """{"id":"info","component":"IconButton","icon":"info","contentDescription":"Info"}""",
          """{"id":"tip","component":"Tooltip","anchor":"info","variant":"rich","text":"Hi","actions":["ok"]}""",
          """{"id":"ok","component":"Button","variant":"text","label":"Got it"}""",
        ),
      )
    has(withActions, "val tipTooltipState = rememberTooltipState(isPersistent = true)")
    has(withActions, "hasAction = true")
    check(buttons.isNotEmpty())
  }

  @Test
  fun `buttons that exist only in an override make the tooltip persistent and have an action`() {
    val source =
      flat(
        ExportFixtures.export(
          """{"id":"root","component":"Column","children":["info"]}""",
          """{"id":"info","component":"IconButton","icon":"info","contentDescription":"Info"}""",
          """{"id":"tip","component":"Tooltip","anchor":"info","variant":"rich","text":"Hi","sizeClass":{"medium":{"actions":["ok"]}}}""",
          """{"id":"ok","component":"Button","variant":"text","label":"Got it"}""",
        ),
      )
    has(source, "val tipTooltipState = rememberTooltipState(isPersistent = true)")
    has(source, "if (atLeastMedium) {")
    has(source, "hasAction = true")
  }

  @Test
  fun `a tooltip button in a toolbar keeps the toolbar content color`() {
    for (toolbar in
      listOf(
        """{"id":"bar","component":"FloatingToolbar","colors":"vibrant","children":["info"]}""",
        """{"id":"bar","component":"DockedToolbar","children":["info"]}""",
      )) {
      val source =
        export(
          """{"id":"root","component":"Column","children":["bar"]}""",
          toolbar,
          """{"id":"info","component":"IconButton","icon":"info","contentDescription":"Info"}""",
          """{"id":"tip","component":"Tooltip","anchor":"info","text":"Hi"}""",
        )
      has(source, "TooltipBox(")
      lacks(source, "iconButtonColors")
    }
  }
}
