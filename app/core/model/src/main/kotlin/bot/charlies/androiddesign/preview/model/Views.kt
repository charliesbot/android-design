package bot.charlies.androiddesign.preview.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Device {
  @SerialName("phone") Phone,
  @SerialName("foldable") Foldable,
  @SerialName("tablet") Tablet,
  @SerialName("desktop") Desktop;

  val wire: String
    get() = lowerCamelName()

  companion object {
    fun fromWire(value: String): Device? = entries.firstOrNull { it.wire == value }
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

/** A named frame a screen is rendered in. Sizes are dp. */
@Serializable
data class ViewSpec(
  val name: String,
  val device: Device,
  val width: Int,
  val height: Int,
  val dark: Boolean,
  val sizeClass: SizeClass,
)

object Views {
  val Light = ViewSpec("light", Device.Phone, 411, 923, dark = false, SizeClass.Compact)
  val Dark = ViewSpec("dark", Device.Phone, 411, 923, dark = true, SizeClass.Compact)
  val Foldable = ViewSpec("foldable", Device.Foldable, 933, 704, dark = false, SizeClass.Expanded)
  val Tablet = ViewSpec("tablet", Device.Tablet, 1280, 800, dark = false, SizeClass.Expanded)
  val Desktop = ViewSpec("desktop", Device.Desktop, 1920, 1080, dark = false, SizeClass.Expanded)

  val all: List<ViewSpec> = listOf(Light, Dark, Foldable, Tablet, Desktop)

  fun byName(name: String): ViewSpec? = all.firstOrNull { it.name == name }

  /** The view for a device and brightness. Only the phone has a named dark view. */
  fun of(device: Device, dark: Boolean): ViewSpec {
    val light = all.first { it.device == device && !it.dark }
    if (!dark) return light
    return all.firstOrNull { it.device == device && it.dark }
      ?: light.copy(name = "${light.name}-dark", dark = true)
  }
}
