package bot.charlies.androiddesign.preview.domain.layout

import bot.charlies.androiddesign.preview.domain.export.ExportFixtures
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.JsonObject

class SemanticsBoundsTest {
  private fun grows(root: String, vararg components: String): Boolean {
    val proposal = ExportFixtures.proposal(root, *components)
    val byId = proposal.screens.getValue("main").components.associateBy { it.id }
    return growsSemanticsBounds(byId.getValue("item"), byId, M3ExpressiveCatalog) {
      listOf(JsonObject(it.props))
    }
  }

  @Test
  fun `linear and wavy linear indicators grow, circular ones do not`() {
    fun indicator(vararg props: String) =
      grows("""{"id":"item","component":"ProgressIndicator",${props.joinToString(",")}}""")
    assertEquals(true, indicator("\"shape\":\"linear\""))
    assertEquals(true, indicator("\"shape\":\"linear\"", "\"wavy\":true"))
    assertEquals(true, indicator("\"wavy\":true"))
    assertEquals(false, indicator("\"shape\":\"circular\""))
    assertEquals(false, indicator("\"shape\":\"circular\"", "\"wavy\":true"))
  }

  @Test
  fun `only a vertical slider grows`() {
    fun slider(orientation: String) =
      grows(
        """{"id":"item","component":"Slider","orientation":"$orientation","value":1,"range":[0,2],"contentDescription":"x"}""",
      )
    assertEquals(true, slider("vertical"))
    assertEquals(false, slider("horizontal"))
  }

  @Test
  fun `a container grows when something under it does`() {
    assertEquals(
      true,
      grows(
        """{"id":"item","component":"Card","content":["p"]}""",
        """{"id":"p","component":"ProgressIndicator","shape":"linear"}""",
      ),
    )
    assertEquals(
      false,
      grows(
        """{"id":"item","component":"Card","content":["p"]}""",
        """{"id":"p","component":"ProgressIndicator","shape":"circular"}""",
      ),
    )
  }
}
