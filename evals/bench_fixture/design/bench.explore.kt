import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bot.charlies.androiddesign.preview.kit.*

// Speed gate fixture: 3 directions x the 5 phone views. bench_speed.py rewrites EDIT_MARKER
// before every timed check so each run pays a real compile.
const val EDIT_MARKER = 0

val preview = designBoard(name = "Bench", seed = 0xFF4A6FA5L, variant = PaletteVariant.TonalSpot) {
    direction("List") { screen("home") { BenchScreen("List", rows = 8) } }
    direction("Cards") { screen("home") { BenchScreen("Cards", rows = 5) } }
    direction("Dense") { screen("home") { BenchScreen("Dense", rows = 12) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BenchScreen(title: String, rows: Int) {
    Scaffold(topBar = { TopAppBar(title = { Text("$title $EDIT_MARKER") }) }) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Today", style = MaterialTheme.typography.titleLarge)
            repeat(rows) { index ->
                ListItem(
                    headlineContent = { Text("Item ${index + 1}") },
                    supportingContent = { Text("Supporting text for row ${index + 1}") },
                )
            }
            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Continue") }
        }
    }
}
