package bot.charlies.androiddesign.preview.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bot.charlies.androiddesign.preview.app.preview.PreviewSession
import bot.charlies.androiddesign.preview.app.preview.Sessions
import bot.charlies.androiddesign.preview.feature.preview.render.SymbolIcon
import bot.charlies.androiddesign.preview.feature.preview.viewer.ViewerScreen
import bot.charlies.androiddesign.preview.feature.preview.viewer.ViewerState
import bot.charlies.androiddesign.preview.feature.preview.viewer.ViewerTheme
import bot.charlies.androiddesign.preview.feature.preview.viewer.ViewerViewModel

/**
 * The window: the selected session's viewer, under a tab row once a second session opens, or an
 * empty state while none is open. [viewer] draws a session's viewer; tests swap in one without the
 * native preview panel.
 */
@Composable
fun PreviewWindowContent(
  sessions: Sessions,
  viewer: @Composable (ViewerViewModel, Modifier) -> Unit = { vm, modifier ->
    ViewerScreen(vm, modifier)
  },
) {
  val all by sessions.sessions.collectAsState()
  val selected by sessions.selected.collectAsState()
  val current = selected
  if (current == null) {
    EmptyState()
    return
  }
  val state by current.viewer.state.collectAsState()
  ViewerTheme(state) {
    Column(Modifier.fillMaxSize()) {
      if (all.size >= 2) {
        SessionTabs(all, current, onSelect = sessions::select, onClose = sessions::close)
      }
      viewer(current.viewer, Modifier.weight(1f))
    }
  }
}

@Composable
private fun SessionTabs(
  all: List<PreviewSession>,
  current: PreviewSession,
  onSelect: (PreviewSession) -> Unit,
  onClose: (PreviewSession) -> Unit,
) {
  PrimaryTabRow(selectedTabIndex = all.indexOf(current).coerceAtLeast(0)) {
    for (session in all) {
      val name by session.name.collectAsState()
      Tab(
        selected = session === current,
        onClick = { onSelect(session) },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(name)
            // An agent's tab closes when the agent disconnects.
            if (!session.isAgent) {
              SymbolIcon(
                "close",
                size = 16.dp,
                contentDescription = "Close $name",
                modifier = Modifier.padding(start = 8.dp).clickable { onClose(session) },
              )
            }
          }
        },
      )
    }
  }
}

@Composable
private fun EmptyState() {
  ViewerTheme(ViewerState()) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
      Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text("No proposal open", style = MaterialTheme.typography.headlineMedium)
        Text(
          "Open a proposal file with File > Open Proposal (Cmd+O), or let an agent open one.",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
        )
      }
    }
  }
}
