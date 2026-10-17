package com.example.designsandbox

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.example.designsandbox.theme.AppTheme

@PreviewTest
@Preview(name = "proposal", device = "spec:width=412dp,height=915dp,dpi=420", showSystemUi = true)
@Preview(
  name = "dark",
  device = "spec:width=412dp,height=915dp,dpi=420",
  uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun ProposalRender() {
  AppTheme { ProposalScreen() }
}
