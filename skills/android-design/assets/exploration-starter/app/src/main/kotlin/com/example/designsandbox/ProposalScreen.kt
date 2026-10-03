package com.example.designsandbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.designsandbox.strings.R
import com.example.designsandbox.theme.AppTheme

// Replace this sample-state screen in the disposable copy, not in the installed starter.
@Composable
fun ProposalScreen() {
  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .safeDrawingPadding()
        .padding(24.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(text = stringResource(R.string.app_name), color = MaterialTheme.colorScheme.onBackground)
  }
}

@Preview
@Composable
private fun ProposalPreview() {
  AppTheme { ProposalScreen() }
}
