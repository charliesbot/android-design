package bot.charlies.androiddesign.preview.domain.node

import bot.charlies.androiddesign.preview.domain.export.ExportFixtures
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import kotlin.test.Test
import kotlin.test.assertEquals

class ChildGraphTest {
  @Test
  fun `an anchor is a reference, not a child`() {
    val proposal =
      ExportFixtures.proposal(
        """{"id":"root","component":"Column","children":["button","field"]}""",
        """{"id":"button","component":"Button","label":"Open"}""",
        """{"id":"field","component":"TextField","label":"Date"}""",
        """{"id":"menu","component":"Menu","anchor":"button","items":["group"]}""",
        """{"id":"group","component":"MenuGroup","items":[]}""",
        """{"id":"tip","component":"Tooltip","anchor":"button","text":"Hint"}""",
        """{"id":"picker","component":"DatePicker","anchor":"field","variant":"docked"}""",
      )
    val byId = proposal.screens.getValue("main").components.associateBy { it.id }
    assertEquals(listOf("group"), ChildGraph.childIds(byId.getValue("menu"), M3ExpressiveCatalog))
    assertEquals(emptyList(), ChildGraph.childIds(byId.getValue("tip"), M3ExpressiveCatalog))
    assertEquals(emptyList(), ChildGraph.childIds(byId.getValue("picker"), M3ExpressiveCatalog))
  }

  @Test
  fun `size overrides count only when asked`() {
    val proposal =
      ExportFixtures.proposal(
        """{"id":"root","component":"Column","children":["a"],"sizeClass":{"expanded":{"children":["a","b"]}}}""",
        """{"id":"a","component":"Text","text":"A"}""",
        """{"id":"b","component":"Text","text":"B"}""",
      )
    val root = proposal.screens.getValue("main").components.first()
    assertEquals(listOf("a"), ChildGraph.childIds(root, M3ExpressiveCatalog))
    assertEquals(
      listOf("a", "a", "b"),
      ChildGraph.childIds(root, M3ExpressiveCatalog, withSizeOverrides = true),
    )
  }

  @Test
  fun `a template counts once by its component id`() {
    val proposal =
      ExportFixtures.proposal(
        """{"id":"root","component":"Column","children":{"componentId":"row","path":"/items"}}""",
        """{"id":"row","component":"Text","text":"x"}""",
      )
    val root = proposal.screens.getValue("main").components.first()
    assertEquals(listOf("row"), ChildGraph.childIds(root, M3ExpressiveCatalog))
  }
}
