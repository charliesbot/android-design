package bot.charlies.androiddesign.preview.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Device {
  @SerialName("phone") Phone,
  @SerialName("foldable") Foldable,
  @SerialName("tablet") Tablet,
  @SerialName("desktop") Desktop,
  @SerialName("watch") Watch;

  val wire: String
    get() = lowerCamelName()

  companion object {
    fun fromWire(value: String): Device? = entries.firstOrNull { it.wire == value }
  }
}

/**
 * The size of a Carousel item, as a node inside it names it with `itemSize`: the smallest item size
 * the node shows at. A multi-browse or hero carousel has large, medium and small items; Material
 * hides the text of the small one.
 */
enum class CarouselItemSize(val wire: String) {
  Large("large"),
  Medium("medium");

  companion object {
    fun fromWire(wire: String?): CarouselItemSize? = entries.firstOrNull { it.wire == wire }
  }
}

/** Window width classes: compact under 600 dp, medium 600 to 839, expanded from 840. */
@Serializable
enum class SizeClass {
  @SerialName("compact") Compact,
  @SerialName("medium") Medium,
  @SerialName("expanded") Expanded;

  val wire: String
    get() = lowerCamelName()

  /** The narrowest window width in this class, in dp. */
  val minWidthDp: Int
    get() =
      when (this) {
        Compact -> 0
        Medium -> MEDIUM_MIN_DP
        Expanded -> EXPANDED_MIN_DP
      }

  companion object {
    const val MEDIUM_MIN_DP = 600
    const val EXPANDED_MIN_DP = 840

    fun forWidth(widthDp: Int): SizeClass =
      when {
        widthDp >= EXPANDED_MIN_DP -> Expanded
        widthDp >= MEDIUM_MIN_DP -> Medium
        else -> Compact
      }

    fun fromWire(value: String): SizeClass? = entries.firstOrNull { it.wire == value }
  }
}

/** A named frame a screen is rendered in. Sizes are dp. A [round] frame is a circular display. */
@Serializable
data class ViewSpec(
  val name: String,
  val device: Device,
  val width: Int,
  val height: Int,
  val dark: Boolean,
  val sizeClass: SizeClass,
  val round: Boolean = false,
)

object Views {
  val Light = ViewSpec("light", Device.Phone, 411, 923, dark = false, SizeClass.Compact)
  val Dark = ViewSpec("dark", Device.Phone, 411, 923, dark = true, SizeClass.Compact)
  /** An open foldable held upright: the one view in the medium size class. */
  val Foldable = ViewSpec("foldable", Device.Foldable, 704, 933, dark = false, SizeClass.Medium)
  val Tablet = ViewSpec("tablet", Device.Tablet, 1280, 800, dark = false, SizeClass.Expanded)
  val Desktop = ViewSpec("desktop", Device.Desktop, 1920, 1080, dark = false, SizeClass.Expanded)

  /** The small Wear OS display: a dark, round 204 dp frame. */
  val Watch =
    ViewSpec("watch", Device.Watch, 204, 204, dark = true, SizeClass.Compact, round = true)

  /** The large Wear OS display, from the 225 dp breakpoint up: a dark, round 227 dp frame. */
  val WatchLarge =
    ViewSpec("watch-large", Device.Watch, 227, 227, dark = true, SizeClass.Compact, round = true)

  /** The views of the phone catalog ([M3ExpressiveCatalog.views]). */
  val all: List<ViewSpec> = listOf(Light, Dark, Foldable, Tablet, Desktop)

  /** The views of the Wear catalog ([WearCatalog.views]). */
  val wear: List<ViewSpec> = listOf(Watch, WatchLarge)

  /** Every view of every catalog. A proposal is checked in its own catalog's views, not these. */
  val every: List<ViewSpec> = all + wear

  fun byName(name: String): ViewSpec? = every.firstOrNull { it.name == name }

  /**
   * The view for a device and brightness. Only the phone has a named dark view; a watch only has
   * dark views, so brightness does not choose between its two.
   */
  fun of(device: Device, dark: Boolean): ViewSpec {
    val onDevice = every.filter { it.device == device }
    onDevice
      .firstOrNull { it.dark == dark }
      ?.let {
        return it
      }
    val first = onDevice.first()
    return if (dark) first.copy(name = "${first.name}-dark", dark = true) else first
  }
}
