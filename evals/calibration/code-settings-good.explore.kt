@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import bot.charlies.androiddesign.preview.kit.*

val preview = designBoard(name = "Jot settings", seed = 0xFF4A6FA5L, variant = PaletteVariant.TonalSpot) {
    direction("Grouped") {
        screen("settings") { SettingsScreen() }
    }
}

@Composable
private fun SettingsScreen() {
    var theme by remember { mutableStateOf("System") }
    var dynamic by remember { mutableStateOf(true) }
    var reminders by remember { mutableStateOf(true) }
    var summary by remember { mutableStateOf(true) }
    var autoSync by remember { mutableStateOf(false) }
    Scaffold(topBar = { LargeTopAppBar(title = { Text("Settings") }) }) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())
                .widthIn(max = 600.dp).padding(horizontal = 16.dp),
        ) {
            SectionTitle("Appearance")
            Column(Modifier.selectableGroup()) {
                listOf("System", "Light", "Dark").forEach { option ->
                    ListItem(
                        headlineContent = { Text(option) },
                        leadingContent = { RadioButton(selected = theme == option, onClick = null) },
                        modifier = Modifier.selectable(
                            selected = theme == option,
                            role = Role.RadioButton,
                            onClick = { theme = option },
                        ),
                    )
                }
            }
            SwitchRow("Use dynamic color", dynamic) { dynamic = it }
            SectionTitle("Notifications")
            SwitchRow("Reminders", reminders) { reminders = it }
            SwitchRow("Daily summary", summary) { summary = it }
            AnimatedVisibility(visible = summary) {
                ListItem(
                    headlineContent = { Text("Summary time") },
                    supportingContent = { Text("8:00 AM") },
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
            SectionTitle("Backup & sync")
            SwitchRow("Auto sync", autoSync) { autoSync = it }
            ListItem(headlineContent = { Text("Last synced") }, supportingContent = { Text("2 min ago") })
            FilledTonalButton(onClick = {}, modifier = Modifier.padding(16.dp)) { Text("Back up now") }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp).semantics { heading() },
    )
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(label) },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        modifier = Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
    )
}
