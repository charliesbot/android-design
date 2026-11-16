package bot.charlies.androiddesign.preview.kit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bot.charlies.androiddesign.preview.feature.preview.theme.FontFace
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalFonts

/**
 * Marks a screen's primary CTA button for layout hierarchy checks without colliding with app tags.
 */
const val AD_PRIMARY_TAG = "ad:primary"

typealias PaletteVariant = bot.charlies.androiddesign.preview.model.PaletteVariant

typealias BundledFont = bot.charlies.androiddesign.preview.model.BundledFont

/**
 * Draws a bundled Material Symbols Rounded glyph by ligature [name]: weight 400, grade 0, fill 0 or
 * 1, optical size matching [size].
 */
@Composable
fun SymbolIcon(
  name: String,
  contentDescription: String? = null,
  modifier: Modifier = Modifier,
  filled: Boolean = false,
  size: Dp = 24.dp,
  tint: Color = LocalContentColor.current,
) {
  val fontSize = size.value.sp
  val semanticsModifier =
    if (contentDescription != null) {
      Modifier.semantics {
        this.contentDescription = contentDescription
        role = Role.Image
      }
    } else {
      Modifier.clearAndSetSemantics {}
    }
  Box(modifier.size(size).then(semanticsModifier), contentAlignment = Alignment.Center) {
    Text(
      text = name,
      modifier = Modifier.wrapContentSize(Alignment.Center, unbounded = true),
      color = tint,
      maxLines = 1,
      softWrap = false,
      style =
        TextStyle(
          fontFamily = ProposalFonts.symbols(filled, size.value.toInt()),
          fontSize = fontSize,
          fontWeight = FontWeight.Normal,
          letterSpacing = 0.sp,
          lineHeight = fontSize,
          textAlign = TextAlign.Center,
          lineHeightStyle =
            LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both),
        ),
    )
  }
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
