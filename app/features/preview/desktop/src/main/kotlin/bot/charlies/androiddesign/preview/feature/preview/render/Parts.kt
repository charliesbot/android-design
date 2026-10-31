package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A text inside a component's slot (a label, a title, a supporting line). It reports its layout to
 * the recorder under [part], so truncation shows up on the owning node.
 */
@Composable
internal fun NodeContext.PartText(
  text: String,
  part: String = "label",
  style: TextStyle = LocalTextStyle.current,
  color: Color = Color.Unspecified,
  maxLines: Int = Int.MAX_VALUE,
  overflow: TextOverflow = TextOverflow.Ellipsis,
  modifier: Modifier = Modifier,
) {
  Text(
    text = text,
    modifier = modifier,
    color = color,
    style = style,
    maxLines = maxLines,
    overflow = overflow,
    onTextLayout = textReporter(part),
  )
}

/** An icon by its Material Symbols name, in the current content color. */
@Composable
internal fun IconSlot(name: String?, size: Dp = 24.dp, filled: Boolean = false) {
  if (name.isNullOrEmpty()) return
  SymbolIcon(name = name, size = size, filled = filled)
}
