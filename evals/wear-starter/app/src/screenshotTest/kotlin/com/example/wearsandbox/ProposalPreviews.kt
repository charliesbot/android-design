package com.example.wearsandbox

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.TimeText
import com.android.tools.screenshot.PreviewTest

// The previewer's two views: watch (204 dp) and watch-large (227 dp), round, on black.
private const val WATCH = "spec:width=204dp,height=204dp,dpi=320,isRound=true"
private const val WATCH_LARGE = "spec:width=227dp,height=227dp,dpi=320,isRound=true"

// The app clock the previewer draws. A screen's own `timeText = {}` registers only inside the
// navigation host,
// so a preview of a screen with the time text off passes clock = false.
@Composable
private fun Frame(clock: Boolean = true, content: @Composable () -> Unit) {
  WearAppTheme { AppScaffold(timeText = { if (clock) TimeText() }) { content() } }
}

// One @PreviewTest per exported screen, both views. Add one like this for each screen function.
@PreviewTest
@Preview(name = "watch", device = WATCH, showBackground = true, backgroundColor = 0xFF000000)
@Preview(
  name = "watch-large",
  device = WATCH_LARGE,
  showBackground = true,
  backgroundColor = 0xFF000000,
)
@Composable
fun ProposalPreview() = Frame { ProposalScreen() }
