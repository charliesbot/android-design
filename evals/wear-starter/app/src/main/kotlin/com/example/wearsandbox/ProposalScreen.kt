package com.example.wearsandbox

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

// The export check replaces this whole file with the previewer's exported Kotlin, which declares
// its own
// WearAppTheme. Wear is always dark.
@Composable
fun WearAppTheme(content: @Composable () -> Unit) {
  MaterialTheme(content = content)
}

/** Placeholder screen. */
@Composable
fun ProposalScreen() {
  AppScaffold {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("Proposal", style = MaterialTheme.typography.titleMedium)
    }
  }
}
