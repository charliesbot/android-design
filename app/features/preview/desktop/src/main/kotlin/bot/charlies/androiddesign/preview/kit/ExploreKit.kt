package bot.charlies.androiddesign.preview.kit

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bot.charlies.androiddesign.preview.feature.preview.render.SymbolIcon as InternalSymbolIcon
import bot.charlies.androiddesign.preview.feature.preview.theme.FontFace
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalFonts

/**
 * Marks a screen's primary CTA button for layout hierarchy checks without colliding with app tags.
 */
const val AD_PRIMARY_TAG = "ad:primary"

typealias PaletteVariant = bot.charlies.androiddesign.preview.model.PaletteVariant

typealias BundledFont = bot.charlies.androiddesign.preview.model.BundledFont

/** Draws a bundled Material Symbols Rounded glyph by ligature [name]. */
@Composable
fun SymbolIcon(
  name: String,
  contentDescription: String? = null,
  modifier: Modifier = Modifier,
  filled: Boolean = false,
  size: Dp = 24.dp,
  tint: Color = LocalContentColor.current,
) {
  InternalSymbolIcon(
    name = name,
    modifier = modifier,
    size = size,
    tint = tint,
    filled = filled,
    contentDescription = contentDescription,
  )
}

/** Returns a Compose [FontFamily] from the previewer's bundled variable fonts. */
fun bundledFontFamily(
  font: BundledFont,
  weight: Int = 400,
  variation: String? = null,
): FontFamily {
  val face = FontFace.entries.first { it.font == font }
  return ProposalFonts.family(face, weight, ProposalFonts.parseVariation(variation))
}
