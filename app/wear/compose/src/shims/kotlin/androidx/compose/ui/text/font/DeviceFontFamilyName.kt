package androidx.compose.ui.text.font

import androidx.compose.ui.text.platform.Typeface
import androidx.compose.ui.unit.Density
import java.util.concurrent.ConcurrentHashMap
import org.jetbrains.skia.Data
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontVariation as SkiaFontVariation
import org.jetbrains.skia.Typeface as SkiaTypeface

/**
 * Android resolves a device font family by name from the system. Wear names "roboto-flex", which is
 * bundled on the classpath (see [DESKTOP_DEVICE_FONTS]); any other name is a missing font.
 */
class DeviceFontFamilyName(val name: String)

/** Device font family name to the variable font file bundled with this module. */
val DESKTOP_DEVICE_FONTS: Map<String, String> = mapOf("roboto-flex" to "fonts/roboto_flex.ttf")

/** Result of [Font] for a device font: a family-to-be, since Compose Desktop has no such Font. */
class DesktopDeviceFont internal constructor(internal val family: FontFamily)

private val bases = ConcurrentHashMap<String, SkiaTypeface>()

private fun baseTypeface(name: String): SkiaTypeface =
  bases.getOrPut(name) {
    val resource = checkNotNull(DESKTOP_DEVICE_FONTS[name]) { "no bundled device font \"$name\"" }
    val bytes =
      checkNotNull(DeviceFontFamilyName::class.java.classLoader.getResourceAsStream(resource)) {
          "missing $resource on the classpath"
        }
        .use { it.readAllBytes() }
    checkNotNull(FontMgr.default.makeFromData(Data.makeFromBytes(bytes)))
  }

private val clones = ConcurrentHashMap<String, FontFamily>()

/** The desktop twin of Android's `Font(DeviceFontFamilyName, ...)`: a clone of the bundled face. */
fun Font(
  familyName: DeviceFontFamilyName,
  weight: FontWeight = FontWeight.Normal,
  style: FontStyle = FontStyle.Normal,
  variationSettings: FontVariation.Settings = FontVariation.Settings(weight, style),
): DesktopDeviceFont {
  // Explicit variation settings win over the plain weight. Axis values that depend on density are
  // not used by the Wear tokens, so any density will do.
  val settings = variationSettings.settings.map { it.axisName to it.toVariationValue(Density(1f)) }
  val axes =
    if (settings.any { it.first == "wght" }) settings
    else listOf("wght" to weight.weight.toFloat()) + settings
  val alias =
    "wear-device-font:${familyName.name}?" + axes.joinToString("&") { (tag, v) -> "$tag=$v" }
  return DesktopDeviceFont(
    clones.getOrPut(alias) {
      FontFamily(
        Typeface(
          baseTypeface(familyName.name)
            .makeClone(axes.map { (t, v) -> SkiaFontVariation(t, v) }.toTypedArray(), 0),
          alias,
        ),
      )
    },
  )
}

fun DesktopDeviceFont.toFontFamily(): FontFamily = family
