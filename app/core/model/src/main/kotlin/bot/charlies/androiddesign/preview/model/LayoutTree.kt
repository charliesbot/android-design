package bot.charlies.androiddesign.preview.model

import kotlin.math.round
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
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

/**
 * What the agent reads instead of a screenshot. Bounds are dp relative to the preview frame; nodes
 * are the proposal's components in document (pre-order) order.
 */
@Serializable
data class LayoutTree(
  val screen: String,
  val view: ViewSpec,
  val nodes: List<LayoutNode>,
  val findings: List<Finding>,
) {
  /** A view passes when it has no error findings. */
  val pass: Boolean
    get() = findings.none { it.severity == Severity.Error }
}

@Serializable
data class LayoutNode(
  val id: String,
  val type: String,
  val parent: String? = null,
  val primary: Boolean = false,
  val bounds: Bounds,
  /** The touch area, which is at least 48 x 48 dp for interactive components. */
  val touch: Bounds? = null,
  val text: TextInfo? = null,
  val roles: NodeRoles? = null,
  val colors: NodeColors? = null,
  val a11y: A11yInfo? = null,
)

/** `[x, y, width, height]` in dp. */
@Serializable(with = BoundsSerializer::class)
data class Bounds(val x: Float, val y: Float, val width: Float, val height: Float)

@Serializable
data class TextInfo(
  val value: String,
  val lines: Int,
  val overflow: Boolean,
  val truncated: Boolean,
  val font: String? = null,
)

@Serializable
data class NodeRoles(
  val container: String? = null,
  val content: String? = null,
  val type: String? = null,
  val shape: String? = null,
)

/** Resolved `#RRGGBB` colors, used for contrast. */
@Serializable data class NodeColors(val container: String? = null, val content: String? = null)

@Serializable
data class A11yInfo(
  val role: String? = null,
  val label: String? = null,
  val actions: List<String>,
)

@Serializable
enum class Severity {
  @SerialName("error") Error,
  @SerialName("info") Info,
}

@Serializable
enum class FindingRule(val wire: String) {
  @SerialName("touch-target") TouchTarget("touch-target"),
  @SerialName("font") Font("font"),
  @SerialName("text-clipped") TextClipped("text-clipped"),
  @SerialName("text-truncated") TextTruncated("text-truncated"),
  @SerialName("label-truncated") LabelTruncated("label-truncated"),
  @SerialName("contrast") Contrast("contrast"),
  @SerialName("primary-largest") PrimaryLargest("primary-largest"),
  @SerialName("one-fab") OneFab("one-fab"),
  @SerialName("offscreen") Offscreen("offscreen"),
}

/** [id] is the component the finding is about, or null when it concerns the whole view. */
@Serializable
data class Finding(
  val id: String? = null,
  val rule: FindingRule,
  val severity: Severity,
  val detail: String? = null,
)

/** Writes whole numbers without a decimal point and others rounded to two places. */
object BoundsSerializer : KSerializer<Bounds> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Bounds")

  override fun serialize(encoder: Encoder, value: Bounds) {
    val json = encoder as? JsonEncoder ?: throw SerializationException("JSON only")
    json.encodeJsonElement(
      JsonArray(listOf(value.x, value.y, value.width, value.height).map { JsonPrimitive(it.dp()) }),
    )
  }

  override fun deserialize(decoder: Decoder): Bounds {
    val json = decoder as? JsonDecoder ?: throw SerializationException("JSON only")
    val array = json.decodeJsonElement() as? JsonArray
    val numbers = array?.map { (it as? JsonPrimitive)?.doubleOrNull?.toFloat() }
    if (numbers == null || numbers.size != 4 || numbers.any { it == null }) {
      throw SerializationException("Bounds must be [x, y, width, height]")
    }
    return Bounds(numbers[0]!!, numbers[1]!!, numbers[2]!!, numbers[3]!!)
  }

  private fun Float.dp(): Number {
    val rounded = round(this * 100f) / 100f
    return if (rounded == round(rounded)) rounded.toLong() else rounded
  }
}
