package bot.charlies.androiddesign.preview.feature.preview.record

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * The box a proposal is drawn in. Recorded bounds are relative to it, so the preview can sit
 * anywhere in a window, scaled or not.
 */
@Composable
fun PreviewFrame(
  recorder: TreeRecorder?,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Box(
    modifier.onGloballyPositioned { recorder?.attachFrame(it) },
  ) {
    content()
  }
}

@Preview
@Composable
private fun PreviewFramePreview() {
  PreviewFrame(TreeRecorder(), Modifier.size(120.dp, 60.dp)) { Text("Frame") }
}
