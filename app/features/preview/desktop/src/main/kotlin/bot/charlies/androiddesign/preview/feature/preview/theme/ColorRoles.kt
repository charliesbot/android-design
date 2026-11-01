package bot.charlies.androiddesign.preview.feature.preview.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import bot.charlies.androiddesign.preview.model.ColorRole

/** The color of [role] in this scheme. */
fun ColorScheme.color(role: ColorRole): Color =
  when (role) {
    ColorRole.Primary -> primary
    ColorRole.OnPrimary -> onPrimary
    ColorRole.PrimaryContainer -> primaryContainer
    ColorRole.OnPrimaryContainer -> onPrimaryContainer
    ColorRole.InversePrimary -> inversePrimary
    ColorRole.Secondary -> secondary
    ColorRole.OnSecondary -> onSecondary
    ColorRole.SecondaryContainer -> secondaryContainer
    ColorRole.OnSecondaryContainer -> onSecondaryContainer
    ColorRole.Tertiary -> tertiary
    ColorRole.OnTertiary -> onTertiary
    ColorRole.TertiaryContainer -> tertiaryContainer
    ColorRole.OnTertiaryContainer -> onTertiaryContainer
    ColorRole.Background -> background
    ColorRole.OnBackground -> onBackground
    ColorRole.Surface -> surface
    ColorRole.OnSurface -> onSurface
    ColorRole.SurfaceVariant -> surfaceVariant
    ColorRole.OnSurfaceVariant -> onSurfaceVariant
    ColorRole.SurfaceTint -> surfaceTint
    ColorRole.InverseSurface -> inverseSurface
    ColorRole.InverseOnSurface -> inverseOnSurface
    ColorRole.Error -> error
    ColorRole.OnError -> onError
    ColorRole.ErrorContainer -> errorContainer
    ColorRole.OnErrorContainer -> onErrorContainer
    ColorRole.Outline -> outline
    ColorRole.OutlineVariant -> outlineVariant
    ColorRole.Scrim -> scrim
    ColorRole.SurfaceBright -> surfaceBright
    ColorRole.SurfaceDim -> surfaceDim
    ColorRole.SurfaceContainer -> surfaceContainer
    ColorRole.SurfaceContainerHigh -> surfaceContainerHigh
    ColorRole.SurfaceContainerHighest -> surfaceContainerHighest
    ColorRole.SurfaceContainerLow -> surfaceContainerLow
    ColorRole.SurfaceContainerLowest -> surfaceContainerLowest
    ColorRole.PrimaryFixed -> primaryFixed
    ColorRole.PrimaryFixedDim -> primaryFixedDim
    ColorRole.OnPrimaryFixed -> onPrimaryFixed
    ColorRole.OnPrimaryFixedVariant -> onPrimaryFixedVariant
    ColorRole.SecondaryFixed -> secondaryFixed
    ColorRole.SecondaryFixedDim -> secondaryFixedDim
    ColorRole.OnSecondaryFixed -> onSecondaryFixed
    ColorRole.OnSecondaryFixedVariant -> onSecondaryFixedVariant
    ColorRole.TertiaryFixed -> tertiaryFixed
    ColorRole.TertiaryFixedDim -> tertiaryFixedDim
    ColorRole.OnTertiaryFixed -> onTertiaryFixed
    ColorRole.OnTertiaryFixedVariant -> onTertiaryFixedVariant
  }

/** A copy of this scheme with [role] replaced by [color]. */
fun ColorScheme.withColor(role: ColorRole, color: Color): ColorScheme =
  when (role) {
    ColorRole.Primary -> copy(primary = color)
    ColorRole.OnPrimary -> copy(onPrimary = color)
    ColorRole.PrimaryContainer -> copy(primaryContainer = color)
    ColorRole.OnPrimaryContainer -> copy(onPrimaryContainer = color)
    ColorRole.InversePrimary -> copy(inversePrimary = color)
    ColorRole.Secondary -> copy(secondary = color)
    ColorRole.OnSecondary -> copy(onSecondary = color)
    ColorRole.SecondaryContainer -> copy(secondaryContainer = color)
    ColorRole.OnSecondaryContainer -> copy(onSecondaryContainer = color)
    ColorRole.Tertiary -> copy(tertiary = color)
    ColorRole.OnTertiary -> copy(onTertiary = color)
    ColorRole.TertiaryContainer -> copy(tertiaryContainer = color)
    ColorRole.OnTertiaryContainer -> copy(onTertiaryContainer = color)
    ColorRole.Background -> copy(background = color)
    ColorRole.OnBackground -> copy(onBackground = color)
    ColorRole.Surface -> copy(surface = color)
    ColorRole.OnSurface -> copy(onSurface = color)
    ColorRole.SurfaceVariant -> copy(surfaceVariant = color)
    ColorRole.OnSurfaceVariant -> copy(onSurfaceVariant = color)
    ColorRole.SurfaceTint -> copy(surfaceTint = color)
    ColorRole.InverseSurface -> copy(inverseSurface = color)
    ColorRole.InverseOnSurface -> copy(inverseOnSurface = color)
    ColorRole.Error -> copy(error = color)
    ColorRole.OnError -> copy(onError = color)
    ColorRole.ErrorContainer -> copy(errorContainer = color)
    ColorRole.OnErrorContainer -> copy(onErrorContainer = color)
    ColorRole.Outline -> copy(outline = color)
    ColorRole.OutlineVariant -> copy(outlineVariant = color)
    ColorRole.Scrim -> copy(scrim = color)
    ColorRole.SurfaceBright -> copy(surfaceBright = color)
    ColorRole.SurfaceDim -> copy(surfaceDim = color)
    ColorRole.SurfaceContainer -> copy(surfaceContainer = color)
    ColorRole.SurfaceContainerHigh -> copy(surfaceContainerHigh = color)
    ColorRole.SurfaceContainerHighest -> copy(surfaceContainerHighest = color)
    ColorRole.SurfaceContainerLow -> copy(surfaceContainerLow = color)
    ColorRole.SurfaceContainerLowest -> copy(surfaceContainerLowest = color)
    ColorRole.PrimaryFixed -> copy(primaryFixed = color)
    ColorRole.PrimaryFixedDim -> copy(primaryFixedDim = color)
    ColorRole.OnPrimaryFixed -> copy(onPrimaryFixed = color)
    ColorRole.OnPrimaryFixedVariant -> copy(onPrimaryFixedVariant = color)
    ColorRole.SecondaryFixed -> copy(secondaryFixed = color)
    ColorRole.SecondaryFixedDim -> copy(secondaryFixedDim = color)
    ColorRole.OnSecondaryFixed -> copy(onSecondaryFixed = color)
    ColorRole.OnSecondaryFixedVariant -> copy(onSecondaryFixedVariant = color)
    ColorRole.TertiaryFixed -> copy(tertiaryFixed = color)
    ColorRole.TertiaryFixedDim -> copy(tertiaryFixedDim = color)
    ColorRole.OnTertiaryFixed -> copy(onTertiaryFixed = color)
    ColorRole.OnTertiaryFixedVariant -> copy(onTertiaryFixedVariant = color)
  }

/** `#RRGGBB` of an opaque color (alpha is dropped). */
fun Color.toHex(): String {
  val argb = toArgbCompat()
  return "#%02X%02X%02X".format((argb shr 16) and 0xFF, (argb shr 8) and 0xFF, argb and 0xFF)
}

private fun Color.toArgbCompat(): Int =
  ((alpha * 255f + 0.5f).toInt() shl 24) or
    ((red * 255f + 0.5f).toInt() shl 16) or
    ((green * 255f + 0.5f).toInt() shl 8) or
    (blue * 255f + 0.5f).toInt()

/** Reads `#RRGGBB`; throws for any other text. */
fun parseHexColor(hex: String): Color {
  require(hex.length == 7 && hex[0] == '#') { "not a #RRGGBB color: $hex" }
  return Color(0xFF000000L or hex.substring(1).toLong(16))
}
