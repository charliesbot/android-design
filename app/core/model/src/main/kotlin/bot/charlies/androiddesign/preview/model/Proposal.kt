package bot.charlies.androiddesign.preview.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/** The contents of a `design/<slug>.proposal.json` file. */
@Serializable
data class Proposal(
  @SerialName("\$schema") val schema: String? = null,
  val format: Int,
  val name: String? = null,
  val theme: ThemeOverride? = null,
  val navigation: Navigation,
  val screens: Map<String, Screen>,
) {
  companion object {
    const val FORMAT = 1
  }
}

@Serializable
data class Navigation(
  val start: String,
  /** Screen id to its pane role. A screen that is not listed is a single pane. */
  val panes: Map<String, PaneRole> = emptyMap(),
  /**
   * List screen id to the id of the screen a list-detail scene draws in its detail pane while
   * nothing is selected. The placeholder is an ordinary screen that is in no pane and that nothing
   * navigates to.
   */
  val placeholders: Map<String, String> = emptyMap(),
)

@Serializable
enum class PaneRole {
  @SerialName("list") List,
  @SerialName("detail") Detail,
  @SerialName("supporting") Supporting,
  @SerialName("extra") Extra,
}

/** One screen: the body of an A2UI v1.0 `createSurface` message. */
@Serializable
data class Screen(
  val catalogId: String,
  val dataModel: JsonElement? = null,
  val components: List<ComponentNode>,
)

/**
 * A component in a screen's flat list. In JSON it is one flat object: `id`, `component`, then the
 * component's props. [props] keeps those props in file order.
 */
@Serializable(with = ComponentNodeSerializer::class)
data class ComponentNode(val id: String, val component: String, val props: JsonObject) {
  /**
   * The `sizeClass` overrides that hold at [size] in the phone catalog. They apply from medium up:
   * medium's first, then expanded's on top, so an override carries on to wider classes until a
   * wider one replaces it. The renderer and the exporter both read overrides through this, so they
   * cannot disagree.
   */
  fun sizeOverridesAt(size: SizeClass): Map<String, JsonElement> =
    sizeOverridesAt(M3ExpressiveCatalog.sizeBreakpoints, size.minWidthDp)

  /**
   * The `sizeClass` overrides that hold in a view [widthDp] wide, under a catalog whose override
   * keys are [breakpoints]. A key applies from its breakpoint's width up, in ascending order, so an
   * override carries on to wider views until a wider key replaces it.
   */
  fun sizeOverridesAt(breakpoints: List<SizeBreakpoint>, widthDp: Int): Map<String, JsonElement> {
    val overrides = props[M3ExpressiveCatalog.SIZE_CLASS] as? JsonObject ?: return emptyMap()
    val merged = LinkedHashMap<String, JsonElement>()
    for (breakpoint in breakpoints.sortedBy { it.minWidthDp }) {
      if (widthDp < breakpoint.minWidthDp) continue
      (overrides[breakpoint.key] as? JsonObject)?.let { merged.putAll(it) }
    }
    return merged
  }

  /**
   * Whether the component shows at [size]: always without a `showIn` prop, else only in the size
   * classes it lists. The renderer and the exporter both read visibility through this.
   */
  fun isShownAt(size: SizeClass): Boolean {
    val listed = props[M3ExpressiveCatalog.SHOW_IN] as? JsonArray ?: return true
    return listed.any { (it as? JsonPrimitive)?.content == size.wire }
  }

  companion object {
    const val ID_KEY = "id"
    const val COMPONENT_KEY = "component"
    const val ROOT_ID = "root"
  }
}

object ComponentNodeSerializer : KSerializer<ComponentNode> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ComponentNode")

  override fun deserialize(decoder: Decoder): ComponentNode {
    val json = decoder as? JsonDecoder ?: throw SerializationException("JSON only")
    val obj =
      json.decodeJsonElement() as? JsonObject
        ?: throw SerializationException("A component must be a JSON object")
    // A missing id or component is kept as "" so the validator reports it with a path.
    val id = obj.text(ComponentNode.ID_KEY)
    val component = obj.text(ComponentNode.COMPONENT_KEY)
    val props =
      JsonObject(obj.filterKeys { it != ComponentNode.ID_KEY && it != ComponentNode.COMPONENT_KEY })
    return ComponentNode(id, component, props)
  }

  override fun serialize(encoder: Encoder, value: ComponentNode) {
    val json = encoder as? JsonEncoder ?: throw SerializationException("JSON only")
    json.encodeJsonElement(
      buildJsonObject {
        put(ComponentNode.ID_KEY, JsonPrimitive(value.id))
        put(ComponentNode.COMPONENT_KEY, JsonPrimitive(value.component))
        value.props.forEach { (key, element) -> put(key, element) }
      },
    )
  }

  private fun JsonObject.text(key: String): String {
    val element = this[key] ?: return ""
    val primitive = element as? JsonPrimitive
    if (primitive == null || !primitive.isString) {
      throw SerializationException("'$key' of a component must be a string")
    }
    return primitive.content
  }
}

/**
 * A problem found in a proposal or in the DESIGN.md it uses; [path] is a JSON-pointer-like
 * location.
 */
@Serializable data class ValidationError(val path: String, val message: String)

/** One `navigate` call found in a screen: [componentId] on screen [from] opens screen [to]. */
data class NavigationEdge(val from: String, val to: String, val componentId: String)
