package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalFonts

/**
 * A Material Symbols icon, drawn as ligature text in the bundled Material Symbols Rounded font:
 * weight 400, grade 0, fill 0 or 1, optical size matching [size].
 */
@Composable
fun SymbolIcon(
  name: String,
  modifier: Modifier = Modifier,
  size: Dp = 24.dp,
  tint: Color = Color.Unspecified,
  filled: Boolean = false,
  contentDescription: String? = null,
) {
  val fontSize = size.value.sp
  val semantics =
    if (contentDescription != null) {
      Modifier.semantics {
        this.contentDescription = contentDescription
        role = Role.Image
      }
    } else {
      Modifier.clearAndSetSemantics {}
    }
  Box(modifier.size(size).then(semantics), contentAlignment = Alignment.Center) {
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
