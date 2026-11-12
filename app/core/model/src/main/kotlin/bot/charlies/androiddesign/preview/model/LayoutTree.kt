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
  /**
   * True for a container that scrolls its children, such as a Wear Screen with `scroll` on or an
   * AlertDialog whose content does not fit (upstream then lays it out in a list): its descendants
   * move and are transformed at the display's edge by design. A dialog whose content fits is fixed.
   */
  val scrolls: Boolean = false,
  val bounds: Bounds,
  /**
   * The layout box, when it reaches past [bounds] horizontally. [bounds] come from semantics, which
   * clip to the window, so a node laid out wider than the window reports a clipped box there; the
   * layout box is not clipped. Omitted when the two agree.
   */
  val layout: Bounds? = null,
  /** The touch area, which is at least 48 x 48 dp for interactive components. */
  val touch: Bounds? = null,
  val text: TextInfo? = null,
  /**
   * What a ring that fills the display takes from the edge, when this node is one. Omitted for
   * every other node.
   */
  val ring: RingInset? = null,
  /**
   * The rounded shape this node clips its content to, when it is a Card or its style gives it a
   * `container` with a corner `shape`, and its bounds are its whole laid-out box. Omitted for every
   * other node, and for one the window, a scroll viewport or a carousel mask cuts off, whose bounds
   * end where no corner is drawn.
   */
  val clip: CornerClip? = null,
  /**
   * The `variant` of a component whose variant changes how the layout checks treat it, which is a
   * date picker (a docked one is a dropdown, the rest are modal). Omitted for every other node.
   */
  val variant: String? = null,
  /**
   * The `weight` of a node a Row or Column gives its share of the space, and the axis it shares.
   * Omitted for every node without one, and where a weight means nothing (a Box, a Grid).
   */
  val weight: NodeWeight? = null,
  val roles: NodeRoles? = null,
  val colors: NodeColors? = null,
  val a11y: A11yInfo? = null,
) {
  /** A node with no area was not laid out, such as an overlay that is closed. */
  val isLaidOut: Boolean
    get() = bounds.width > 0f && bounds.height > 0f
}

/**
 * The [value] of a node's `weight`, which shares the parent's [axis]: width in a Row, height in a
 * Column. [size] is the node's measured size along that axis in dp, not cut to the window or a
 * scroll viewport as `bounds` are, so a node scrolled out of view still has its real size.
 */
@Serializable data class NodeWeight(val value: Float, val axis: WeightAxis, val size: Float? = null)

@Serializable
enum class WeightAxis {
  @SerialName("width") Width,
  @SerialName("height") Height,
}

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
  /**
   * The box the drawn lines cover, when the node shows one text and the renderer measured it. The
   * layout box of a centered text can span the width while its glyphs do not.
   */
  val ink: Bounds? = null,
  /**
   * True when a text of the node has characters but drew no glyphs, or drew at zero width: the
   * space it was given is gone. Unlike [ink] it covers a node that shows several texts (a list
   * item's headline and supporting line).
   */
  val undrawn: Boolean = false,
)

/**
 * The band a progress ring that fills a round display takes at its edge, from Wear's public
 * defaults at render time: the [padding] between the ring and the edge, and its [stroke] width, in
 * dp. Content inside the circle but within [inset] of the edge reaches into the ring.
 */
@Serializable
data class RingInset(val padding: Float, val stroke: Float) {
  /** How far in from the display's edge the ring reaches. */
  val inset: Float
    get() = padding + stroke
}

/**
 * The rounded shape a container clips its content to: its corner [radius] in dp, at most half the
 * shape's shorter side as Compose draws it, and how far the shape reaches past the node's bounds on
 * every side ([outset]). A styled container draws its shape around its own padding while the tree
 * reports the box inside that padding, so its outset is the padding; a Card's is 0.
 */
@Serializable data class CornerClip(val radius: Float, val outset: Float = 0f)

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
  @SerialName("overflow") Overflow("overflow"),
  @SerialName("round-edge-clipped") RoundEdgeClipped("round-edge-clipped"),
  @SerialName("corner-clipped") CornerClipped("corner-clipped"),
  @SerialName("carousel-items") CarouselItems("carousel-items"),
  @SerialName("docked-toolbar-nav-bar") DockedToolbarNavBar("docked-toolbar-nav-bar"),
  @SerialName("full-screen-date-picker") FullScreenDatePicker("full-screen-date-picker"),
  @SerialName("empty-detail-pane") EmptyDetailPane("empty-detail-pane"),
  @SerialName("line-length") LineLength("line-length"),
  @SerialName("weight-collapsed") WeightCollapsed("weight-collapsed"),
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
    val numbers = array?.mapNotNull { (it as? JsonPrimitive)?.doubleOrNull?.toFloat() }
    // A non-number element drops out of `numbers`, so four of them out of four elements is exact.
    if (array == null || array.size != 4 || numbers?.size != 4) {
      throw SerializationException("Bounds must be [x, y, width, height]")
    }
    val (x, y, width, height) = numbers
    return Bounds(x, y, width, height)
  }

  private fun Float.dp(): Number {
    val rounded = round(this * 100f) / 100f
    return if (rounded == round(rounded)) rounded.toLong() else rounded
  }
}
