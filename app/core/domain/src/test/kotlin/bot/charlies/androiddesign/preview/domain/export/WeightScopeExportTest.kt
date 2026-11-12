package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.export.ExportFixtures.body
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `Modifier.weight(...)` is a member of `RowScope` and `ColumnScope`, so it is written only where
 * the Compose lambda that holds the child has one of them. The catalog says which slots do
 * (`PropDef.childScope`), and the previewer weights a child in the same slots.
 */
class WeightScopeExportTest {
  private val child =
    """{"id":"w","component":"IconButton","icon":"edit","contentDescription":"Edit","weight":2}"""
  private val sibling =
    """{"id":"s","component":"IconButton","icon":"add","contentDescription":"Add"}"""

  private fun weights(vararg components: String): Int =
    Regex("""\.weight\(2f\)""")
      .findAll(body(ExportFixtures.flat(ExportFixtures.export(*components))))
      .count()

  private fun inColumn(host: String) =
    weights("""{"id":"root","component":"Column","children":["h"]}""", host, sibling, child)

  @Test
  fun `a horizontal floating toolbar gives its children a row scope`() {
    assertEquals(
      1,
      inColumn("""{"id":"h","component":"FloatingToolbar","children":["s","w"]}"""),
    )
  }

  @Test
  fun `a vertical floating toolbar gives its children a column scope`() {
    assertEquals(
      1,
      inColumn(
        """{"id":"h","component":"FloatingToolbar","orientation":"vertical","children":["s","w"]}""",
      ),
    )
  }

  @Test
  fun `a docked toolbar gives its children a row scope`() {
    assertEquals(
      1,
      inColumn("""{"id":"h","component":"DockedToolbar","children":["s","w"]}"""),
    )
  }

  @Test
  fun `a top app bar gives its actions a row scope`() {
    assertEquals(
      1,
      inColumn("""{"id":"h","component":"TopAppBar","title":"T","actions":["s","w"]}"""),
    )
  }

  @Test
  fun `a row and a column give their children a scope`() {
    assertEquals(1, inColumn("""{"id":"h","component":"Row","children":["s","w"]}"""))
    assertEquals(1, inColumn("""{"id":"h","component":"Column","children":["s","w"]}"""))
  }

  @Test
  fun `a box has no scope with a weight`() {
    assertEquals(0, inColumn("""{"id":"h","component":"Box","children":["s","w"]}"""))
  }

  @Test
  fun `a chip group is drawn as a wrapping flow, so its chips take no weight`() {
    val chip = """{"id":"w","component":"AssistChip","label":"Chip","weight":2}"""
    val other = """{"id":"s","component":"AssistChip","label":"Other"}"""
    assertEquals(
      0,
      weights(
        """{"id":"root","component":"Column","children":["h"]}""",
        """{"id":"h","component":"ChipGroup","children":["s","w"]}""",
        other,
        chip,
      ),
    )
  }

  @Test
  fun `a side sheet and a full-screen dialog scroll their content, so it takes no weight`() {
    assertEquals(
      0,
      inColumn("""{"id":"h","component":"SideSheet","title":"T","content":["s","w"]}"""),
    )
    assertEquals(
      0,
      inColumn(
        """{"id":"h","component":"Dialog","variant":"full-screen","title":"T","content":["s","w"]}""",
      ),
    )
  }
}
