package bot.charlies.androiddesign.preview.feature.preview

import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessRenderer
import bot.charlies.androiddesign.preview.model.ComponentDef
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.PropDef
import bot.charlies.androiddesign.preview.model.PropKind
import bot.charlies.androiddesign.preview.model.Views
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * A proposal that states a catalog default must draw exactly what one that leaves the prop out
 * draws. Each component is drawn alone with only its required props, once as it is and once with
 * every default stated. The renderer takes most defaults from the catalog, so for those this holds
 * by construction (the committed schema pins their values); the test catches the defaults the
 * renderer applies in a `when` branch, such as an alignment, that the catalog must match.
 */
class CatalogDefaultsTest {
  private val defaulted =
    M3ExpressiveCatalog.components.filter { def -> def.props.any { it.default != null } }

  /** Components that only make sense inside another one. */
  private val hostOf =
    mapOf(
      "NavigationBarItem" to "NavigationBar",
      "NavigationRailItem" to "NavigationRail",
      "FabMenuItem" to "FabMenu",
    )

  /** The Scaffold slot a component fills; every other component goes in the content column. */
  private val slotOf =
    mapOf(
      "TopAppBar" to "topBar",
      "NavigationBar" to "bottomBar",
      "FloatingToolbar" to "bottomBar",
      "FloatingActionButton" to "floatingActionButton",
      "FabMenu" to "floatingActionButton",
      "NavigationRail" to "navigationRail",
    )

  private val text = node("t1", "Text") { put("text", "Hello") }

  private fun node(id: String, component: String, props: JsonObjectBuilder.() -> Unit = {}) =
    buildJsonObject {
      put("id", id)
      put("component", component)
      props()
    }

  /** A valid value for a required prop; a child it needs is added to [extra]. */
  private fun requiredValue(
    prop: PropDef,
    id: String,
    extra: MutableList<JsonObject>,
  ): JsonElement =
    when (val kind = prop.kind) {
      PropKind.Text -> JsonPrimitive("Sample")
      PropKind.Icon -> JsonPrimitive("home")
      PropKind.Image -> JsonPrimitive("missing.png")
      is PropKind.Enum -> JsonPrimitive(kind.values.first())
      PropKind.Child,
      PropKind.ChildList -> {
        val allowed = prop.allowedChildren?.minOrNull()
        val childId = if (allowed == null) "t1" else "${id}_${prop.name}"
        if (allowed != null) {
          extra += requiredNode(checkNotNull(M3ExpressiveCatalog.find(allowed)), childId, extra)
        }
        if (prop.kind == PropKind.Child) JsonPrimitive(childId)
        else JsonArray(listOf(JsonPrimitive(childId)))
      }
      else -> error("${prop.name}: no required value for $kind")
    }

  private fun requiredNode(
    def: ComponentDef,
    id: String,
    extra: MutableList<JsonObject>,
  ): JsonObject = buildJsonObject {
    put("id", id)
    put("component", def.name)
    def.props.filter { it.required }.forEach { put(it.name, requiredValue(it, id, extra)) }
  }

  /** The components of a screen that shows [def] as `subject`, with [stated] added to its props. */
  private fun screenOf(def: ComponentDef, stated: List<PropDef>): String {
    val extra = mutableListOf<JsonObject>()
    val required = requiredNode(def, "subject", extra)
    val subject = JsonObject(required + stated.associate { it.name to checkNotNull(it.default) })
    var top = "subject"
    val parts = mutableListOf<JsonObject>()
    hostOf[def.name]?.let { host ->
      parts += node("host", host) { put("children", JsonArray(listOf(JsonPrimitive("subject")))) }
      top = "host"
    }
    val scaffold =
      slotOf[hostOf[def.name] ?: def.name]?.let { slot ->
        node("root", "Scaffold") {
          put(slot, top)
          put("content", "t1")
        }
      }
        ?: node("root", "Scaffold") { put("content", "stage") }
          .also {
            parts +=
              node("stage", "Column") { put("children", JsonArray(listOf(JsonPrimitive(top)))) }
          }
    return (listOf(scaffold) + parts + subject + extra + text).joinToString(",")
  }

  private fun tree(def: ComponentDef, stated: List<PropDef>): String {
    val proposal = singleScreen(screenOf(def, stated))
    val tree: LayoutTree =
      HeadlessRenderer.renderTree(proposal, designOf(proposal.theme), null, "home", Views.Light)
    return PreviewJson.pretty.encodeToString(LayoutTree.serializer(), tree)
  }

  private fun ComponentDef.defaults() = props.filter { it.default != null }

  @Test
  fun `stating every default draws the same tree as leaving them out`() {
    assertTrue(defaulted.size > 25, "only ${defaulted.size} components have defaults")
    val different = defaulted.mapNotNull { def ->
      if (tree(def, emptyList()) == tree(def, def.defaults())) {
        null
      } else {
        val props = def.defaults().filter { tree(def, emptyList()) != tree(def, listOf(it)) }
        "${def.name}: ${props.joinToString { "${it.name}=${it.default?.content}" }}"
      }
    }
    assertEquals(emptyList(), different)
  }

  @Test
  fun `a value other than the default does change the tree`() {
    val button = checkNotNull(M3ExpressiveCatalog.find("Button"))
    val variant = checkNotNull(button.prop("variant"))
    val outlined = variant.copy(default = JsonPrimitive("outlined"))
    assertNotEquals(tree(button, emptyList()), tree(button, listOf(outlined)))
  }
}
