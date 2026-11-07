package bot.charlies.androiddesign.preview.domain

import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.Proposal
import kotlinx.serialization.json.Json

private val testJson = Json { ignoreUnknownKeys = false }

/**
 * Builds proposals for tests from component JSON snippets keyed by id. Replace one component to
 * break exactly one rule.
 */
class TestProposal(
  private val plates: LinkedHashMap<String, String> = linkedMapOf(),
  private val detail: LinkedHashMap<String, String> = linkedMapOf(),
  private val navigation: String =
    """{"start":"plates","panes":{"plates":"list","plate_detail":"detail"}}""",
  private val theme: String? = null,
  private val format: Int = 1,
  private val platesCatalogId: String = M3ExpressiveCatalog.ID,
  private val extraScreens: LinkedHashMap<String, String> = linkedMapOf(),
) {
  fun with(id: String, json: String) = copy(plates = LinkedHashMap(plates).also { it[id] = json })

  fun without(id: String) = copy(plates = LinkedHashMap(plates).also { it.remove(id) })

  fun withDetail(id: String, json: String) =
    copy(detail = LinkedHashMap(detail).also { it[id] = json })

  fun withNavigation(json: String) = copy(navigation = json)

  /** Adds a screen after the two built-in ones; [componentsJson] is its component list. */
  fun withScreen(id: String, componentsJson: String) =
    copy(extraScreens = LinkedHashMap(extraScreens).also { it[id] = componentsJson })

  /** Adds `pick`, a screen that says what to choose, for use as a detail placeholder. */
  fun withPickScreen() =
    withScreen(
      "pick",
      """{"id":"root","component":"Scaffold","content":"hint"},""" +
        """{"id":"hint","component":"Text","text":"Pick a plate","type":"bodyLarge"}""",
    )

  fun withTheme(json: String) = copy(theme = json)

  fun withFormat(value: Int) = copy(format = value)

  fun withCatalogId(value: String) = copy(platesCatalogId = value)

  /** Index of [id] in the plates component list, for building expected paths. */
  fun indexOf(id: String): Int = plates.keys.indexOf(id)

  private fun copy(
    plates: LinkedHashMap<String, String> = this.plates,
    detail: LinkedHashMap<String, String> = this.detail,
    navigation: String = this.navigation,
    theme: String? = this.theme,
    format: Int = this.format,
    platesCatalogId: String = this.platesCatalogId,
    extraScreens: LinkedHashMap<String, String> = this.extraScreens,
  ) = TestProposal(plates, detail, navigation, theme, format, platesCatalogId, extraScreens)

  fun json(): String = buildString {
    append("""{"format":$format,"name":"Test",""")
    if (theme != null) append(""""theme":$theme,""")
    append(""""navigation":$navigation,"screens":{""")
    append(
      """"plates":{"catalogId":"$platesCatalogId","components":[${plates.values.joinToString(",")}]},""",
    )
    append(
      """"plate_detail":{"catalogId":"${M3ExpressiveCatalog.ID}","components":[${detail.values.joinToString(",")}]}""",
    )
    extraScreens.forEach { (id, components) ->
      append(""","$id":{"catalogId":"${M3ExpressiveCatalog.ID}","components":[$components]}""")
    }
    append("}}")
  }

  fun build(): Proposal = testJson.decodeFromString(Proposal.serializer(), json())

  companion object {
    private const val NAVIGATE_DETAIL =
      """{"functionCall":{"@call":"navigate","args":{"screen":"plate_detail"}}}"""
    private const val OPEN_SHEET =
      """{"functionCall":{"@call":"open","args":{"overlay":"sheet"}}}"""

    /** Two screens, a navigate edge, a sheet and a button: valid with no errors. */
    fun valid() =
      TestProposal(
        plates =
          linkedMapOf(
            "root" to """{"id":"root","component":"Scaffold","topBar":"bar","content":"body"}""",
            "bar" to """{"id":"bar","component":"TopAppBar","title":"Plates"}""",
            "body" to
              """{"id":"body","component":"Column","children":["open_plate","more"],"spacing":"md"}""",
            "open_plate" to
              """{"id":"open_plate","component":"Button","label":"Cook","primary":true,"action":$NAVIGATE_DETAIL}""",
            "more" to
              """{"id":"more","component":"Button","variant":"text","label":"More","action":$OPEN_SHEET}""",
            "sheet" to """{"id":"sheet","component":"BottomSheet","content":["sheet_text"]}""",
            "sheet_text" to
              """{"id":"sheet_text","component":"Text","text":"Sheet","type":"bodyLarge"}""",
          ),
        detail =
          linkedMapOf(
            "root" to """{"id":"root","component":"Scaffold","content":"title"}""",
            "title" to
              """{"id":"title","component":"Text","text":"Detail","type":"headlineLarge","color":"onSurface"}""",
          ),
      )
  }
}
