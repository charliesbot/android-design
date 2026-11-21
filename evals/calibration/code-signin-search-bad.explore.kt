@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import bot.charlies.androiddesign.preview.kit.*

val preview = designBoard(name = "Simmer", seed = 0xFFB5523BL, variant = PaletteVariant.TonalSpot) {
    direction("Saved recipes") {
        screen("saved") { SavedScreen() }
    }
}

@Composable
private fun SavedScreen() {
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            SearchBar(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                inputField = {
                    SearchBarDefaults.InputField(
                        query = query,
                        onQueryChange = { query = it },
                        onSearch = {},
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        placeholder = { Text("Search 2,400 recipes") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            ) {}
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(Modifier.fillMaxWidth().height(200.dp).clip(MaterialTheme.shapes.extraLarge)) {
                Image(
                    painter = designPainter("assets/recipe_cake.jpg"),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().blur(16.dp),
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                ) {
                    Text(
                        "Your saved recipes live here",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    )
                }
            }
            Text(
                "Sign in to sync",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() },
            )
            OutlinedTextField(
                state = rememberTextFieldState(),
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedSecureTextField(
                state = rememberTextFieldState(),
                label = { Text("Password") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = {}, modifier = Modifier.align(Alignment.End)) { Text("Sign in") }
        }
    }
}
