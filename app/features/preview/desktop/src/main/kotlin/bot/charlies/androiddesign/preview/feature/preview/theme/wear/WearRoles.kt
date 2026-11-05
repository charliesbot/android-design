package bot.charlies.androiddesign.preview.feature.preview.theme.wear

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.Typography

/**
 * The color of the role named [role] (a [bot.charlies.androiddesign.preview.model.WearCatalog] wire
 * name).
 */
fun ColorScheme.color(role: String): Color? =
  when (role) {
    "primary" -> primary
    "primaryDim" -> primaryDim
    "primaryContainer" -> primaryContainer
    "onPrimary" -> onPrimary
    "onPrimaryContainer" -> onPrimaryContainer
    "secondary" -> secondary
    "secondaryDim" -> secondaryDim
    "secondaryContainer" -> secondaryContainer
    "onSecondary" -> onSecondary
    "onSecondaryContainer" -> onSecondaryContainer
    "tertiary" -> tertiary
    "tertiaryDim" -> tertiaryDim
    "tertiaryContainer" -> tertiaryContainer
    "onTertiary" -> onTertiary
    "onTertiaryContainer" -> onTertiaryContainer
    "surfaceContainerLow" -> surfaceContainerLow
    "surfaceContainer" -> surfaceContainer
    "surfaceContainerHigh" -> surfaceContainerHigh
    "onSurface" -> onSurface
    "onSurfaceVariant" -> onSurfaceVariant
    "outline" -> outline
    "outlineVariant" -> outlineVariant
    "background" -> background
    "onBackground" -> onBackground
    "error" -> error
    "errorDim" -> errorDim
    "errorContainer" -> errorContainer
    "onError" -> onError
    "onErrorContainer" -> onErrorContainer
    else -> null
  }

/** A copy of this scheme with the role named [role] replaced by [color]. */
fun ColorScheme.withColor(role: String, color: Color): ColorScheme =
  when (role) {
    "primary" -> copy(primary = color)
    "primaryDim" -> copy(primaryDim = color)
    "primaryContainer" -> copy(primaryContainer = color)
    "onPrimary" -> copy(onPrimary = color)
    "onPrimaryContainer" -> copy(onPrimaryContainer = color)
    "secondary" -> copy(secondary = color)
    "secondaryDim" -> copy(secondaryDim = color)
    "secondaryContainer" -> copy(secondaryContainer = color)
    "onSecondary" -> copy(onSecondary = color)
    "onSecondaryContainer" -> copy(onSecondaryContainer = color)
    "tertiary" -> copy(tertiary = color)
    "tertiaryDim" -> copy(tertiaryDim = color)
    "tertiaryContainer" -> copy(tertiaryContainer = color)
    "onTertiary" -> copy(onTertiary = color)
    "onTertiaryContainer" -> copy(onTertiaryContainer = color)
    "surfaceContainerLow" -> copy(surfaceContainerLow = color)
    "surfaceContainer" -> copy(surfaceContainer = color)
    "surfaceContainerHigh" -> copy(surfaceContainerHigh = color)
    "onSurface" -> copy(onSurface = color)
    "onSurfaceVariant" -> copy(onSurfaceVariant = color)
    "outline" -> copy(outline = color)
    "outlineVariant" -> copy(outlineVariant = color)
    "background" -> copy(background = color)
    "onBackground" -> copy(onBackground = color)
    "error" -> copy(error = color)
    "errorDim" -> copy(errorDim = color)
    "errorContainer" -> copy(errorContainer = color)
    "onError" -> copy(onError = color)
    "onErrorContainer" -> copy(onErrorContainer = color)
    else -> this
  }

/** The text style of the role named [role], or null for a name that is not a Wear type role. */
fun Typography.style(role: String): TextStyle? =
  when (role) {
    "displayLarge" -> displayLarge
    "displayMedium" -> displayMedium
    "displaySmall" -> displaySmall
    "titleLarge" -> titleLarge
    "titleMedium" -> titleMedium
    "titleSmall" -> titleSmall
    "labelLarge" -> labelLarge
    "labelMedium" -> labelMedium
    "labelSmall" -> labelSmall
    "bodyLarge" -> bodyLarge
    "bodyMedium" -> bodyMedium
    "bodySmall" -> bodySmall
    "bodyExtraSmall" -> bodyExtraSmall
    "numeralExtraLarge" -> numeralExtraLarge
    "numeralLarge" -> numeralLarge
    "numeralMedium" -> numeralMedium
    "numeralSmall" -> numeralSmall
    "numeralExtraSmall" -> numeralExtraSmall
    else -> null
  }

/** A copy of this typography with every role in [styles] replaced; roles it lacks stay. */
fun Typography.withStyles(styles: Map<String, TextStyle>): Typography =
  copy(
    displayLarge = styles["displayLarge"] ?: displayLarge,
    displayMedium = styles["displayMedium"] ?: displayMedium,
    displaySmall = styles["displaySmall"] ?: displaySmall,
    titleLarge = styles["titleLarge"] ?: titleLarge,
    titleMedium = styles["titleMedium"] ?: titleMedium,
    titleSmall = styles["titleSmall"] ?: titleSmall,
    labelLarge = styles["labelLarge"] ?: labelLarge,
    labelMedium = styles["labelMedium"] ?: labelMedium,
    labelSmall = styles["labelSmall"] ?: labelSmall,
    bodyLarge = styles["bodyLarge"] ?: bodyLarge,
    bodyMedium = styles["bodyMedium"] ?: bodyMedium,
    bodySmall = styles["bodySmall"] ?: bodySmall,
    bodyExtraSmall = styles["bodyExtraSmall"] ?: bodyExtraSmall,
    numeralExtraLarge = styles["numeralExtraLarge"] ?: numeralExtraLarge,
    numeralLarge = styles["numeralLarge"] ?: numeralLarge,
    numeralMedium = styles["numeralMedium"] ?: numeralMedium,
    numeralSmall = styles["numeralSmall"] ?: numeralSmall,
    numeralExtraSmall = styles["numeralExtraSmall"] ?: numeralExtraSmall,
  )
