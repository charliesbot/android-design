package com.example.designsandbox.wear

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.example.designsandbox.wear.theme.WearAppTheme

@PreviewTest
@Preview(
  name = "proposal",
  device = "spec:width=192dp,height=192dp,dpi=320,isRound=true",
  showSystemUi = true,
)
@Composable
fun ProposalRender() {
  WearAppTheme { ProposalScreen() }
}
