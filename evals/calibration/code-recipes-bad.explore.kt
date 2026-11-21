@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import bot.charlies.androiddesign.preview.kit.*

val preview = designBoard(name = "Simmer", seed = 0xFFB5523BL, variant = PaletteVariant.TonalSpot) {
    direction("Featured first") {
        screen("discover") { DiscoverScreen() }
    }
}

private val recipes = listOf(
    Triple("Miso mushroom ramen", "45 min · 4.7", "assets/recipe_ramen.jpg"),
    Triple("Peach and burrata salad", "15 min · 4.9", "assets/recipe_salad.jpg"),
    Triple("Crispy fish tacos", "35 min · 4.6", "assets/recipe_tacos.jpg"),
)

@Composable
private fun DiscoverScreen() {
    var query by remember { mutableStateOf("") }
    Scaffold { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search 2,400 recipes") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                shape = TextFieldDefaults.roundedShape,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Text("Featured this week", style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
            Box(Modifier.fillMaxWidth().height(240.dp).clip(MaterialTheme.shapes.extraLarge)) {
                Image(
                    painter = designPainter("assets/recipe_shakshuka.jpg"),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().blur(24.dp),
                )
                Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                    Text("Shakshuka", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text("30 min · 4.8 stars", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }
            }
            Text("For you", style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
            recipes.forEach { (title, meta, image) ->
                ListItem(
                    headlineContent = { Text(title) },
                    supportingContent = { Text(meta) },
                    leadingContent = {
                        Image(
                            painter = designPainter(image),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.medium),
                        )
                    },
                )
            }
        }
    }
}
