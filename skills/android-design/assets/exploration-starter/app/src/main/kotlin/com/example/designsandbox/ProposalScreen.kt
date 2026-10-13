package com.example.designsandbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.designsandbox.theme.AppTheme

// Replace this sample in the exploration copy, not in the installed starter or requesting app.
@Composable
fun ProposalScreen() {
  BoxWithConstraints(
    modifier =
      Modifier.fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .safeDrawingPadding()
        .padding(24.dp),
    contentAlignment = Alignment.TopCenter,
  ) {
    val expanded = maxWidth >= 840.dp
    Column(
      modifier = Modifier.widthIn(max = 1200.dp).verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("YOUR DAILY SPACE", style = MaterialTheme.typography.labelLarge)
        Text("A little room to focus.", style = MaterialTheme.typography.displaySmallEmphasized)
        Text(
          "One adaptive screen. Four ways to see it.",
          style = MaterialTheme.typography.bodyLarge,
        )
      }
      if (expanded) {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
          FocusCard(Modifier.weight(2f))
          DetailsCard(Modifier.weight(1f))
        }
      } else {
        FocusCard(Modifier.fillMaxWidth())
        DetailsCard(Modifier.fillMaxWidth())
      }
    }
  }
}

@Composable
private fun FocusCard(modifier: Modifier = Modifier) {
  Card(modifier) {
    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Text("Make space for what matters", style = MaterialTheme.typography.headlineMedium)
      Text("Replace this sample with the proposal's primary task and representative content.")
      Button(onClick = {}) { Text("Start a moment") }
    }
  }
}

@Composable
private fun DetailsCard(modifier: Modifier = Modifier) {
  Card(modifier) {
    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Text("At a glance", style = MaterialTheme.typography.titleLarge)
      Text("20 minutes for yourself")
      Text("3 things worth keeping")
    }
  }
}

// Seed-tinted previews: surfaces should follow the seed, not the default palette.
private val PreviewSeed = Color(0xFFB8321F)

// Full layout canvases, not exact system chrome. See the starter README for density provenance.
@Preview(name = "Pixel 11 (modeled density)", device = "spec:width=411dp,height=923dp,dpi=420")
@Composable
private fun ProposalPhonePreview() {
  AppTheme(seed = PreviewSeed, darkTheme = false) { ProposalScreen() }
}

@Preview(name = "Pixel Tablet", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun ProposalTabletPreview() {
  AppTheme(seed = PreviewSeed, darkTheme = false) { ProposalScreen() }
}

@Preview(
  name = "Desktop 1080p (representative display)",
  device = "spec:width=1920dp,height=1080dp,dpi=160",
)
@Composable
private fun ProposalDesktopPreview() {
  AppTheme(seed = PreviewSeed, darkTheme = false) { ProposalScreen() }
}

@Preview(name = "Z Fold8 (modeled density)", device = "spec:width=933dp,height=704dp,dpi=420")
@Composable
private fun ProposalFoldablePreview() {
  AppTheme(seed = PreviewSeed, darkTheme = false) { ProposalScreen() }
}
