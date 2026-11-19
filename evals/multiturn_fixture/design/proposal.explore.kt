@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bot.charlies.androiddesign.preview.kit.*

private data class Dish(
    val id: String,
    val title: String,
    val subtitle: String,
    val minutes: String,
    val servings: String,
    val tag: String,
    val image: String,
    val ingredients: List<String>,
    val steps: List<String>,
)

private val dishes = listOf(
    Dish(
        id = "shakshuka",
        title = "Smoky Baked Shakshuka",
        subtitle = "Charred tomatoes, cumin, poached eggs",
        minutes = "25 min",
        servings = "4 servings",
        tag = "Skillet",
        image = "assets/recipe_shakshuka.jpg",
        ingredients = listOf(
            "6 ripe plum tomatoes, crushed",
            "1 roasted red pepper, sliced",
            "4 large eggs",
            "2 tsp smoked paprika",
            "Warm sourdough for dipping",
        ),
        steps = listOf(
            "Simmer tomatoes, pepper, and spices for 12 minutes.",
            "Make four wells and crack eggs into the sauce.",
            "Cover until whites set and finish with herbs.",
        ),
    ),
    Dish(
        id = "pasta",
        title = "Lemon Ricotta Tagliatelle",
        subtitle = "Fresh zest, cracked pepper, basil",
        minutes = "20 min",
        servings = "2 servings",
        tag = "Pasta",
        image = "assets/recipe_pasta.jpg",
        ingredients = listOf(
            "250 g egg tagliatelle",
            "150 g whole-milk ricotta",
            "1 Meyer lemon, zested",
            "Fresh basil leaves",
        ),
        steps = listOf(
            "Cook pasta in salted water until al dente.",
            "Whisk ricotta with zest and pasta water.",
            "Toss glossy ribbons off heat with basil.",
        ),
    ),
    Dish(
        id = "ramen",
        title = "Ginger Miso Ramen",
        subtitle = "Soft egg, scallions, toasted sesame",
        minutes = "35 min",
        servings = "2 servings",
        tag = "Broth",
        image = "assets/recipe_ramen.jpg",
        ingredients = listOf(
            "2 packs fresh ramen noodles",
            "3 tbsp white miso paste",
            "1 knob fresh ginger, grated",
            "2 soft-boiled eggs",
        ),
        steps = listOf(
            "Infuse dashi broth with ginger and garlic.",
            "Whisk in miso off the boil and pour over noodles.",
            "Top with halved eggs and scallions.",
        ),
    ),
    Dish(
        id = "salad",
        title = "Charred Citrus Greens",
        subtitle = "Fennel, pistachio, olive oil",
        minutes = "15 min",
        servings = "4 servings",
        tag = "Greens",
        image = "assets/recipe_salad.jpg",
        ingredients = listOf(
            "2 heads bitter greens",
            "1 blood orange, segmented",
            "1 shaved fennel bulb",
            "Toasted pistachios",
        ),
        steps = listOf(
            "Whisk citrus juice with olive oil and flaky salt.",
            "Toss greens and shaved fennel lightly.",
            "Scatter orange segments and pistachios.",
        ),
    ),
)

val preview = designBoard(
    name = "Tonight's Plate",
    seed = 0xFFB5523BL,
    variant = PaletteVariant.TonalSpot,
    brandFont = BundledFont.Fraunces,
) {
    direction("Editorial split") {
        listDetail(
            list = { onOpenDetail -> EditorialHomePane(onOpenDetail = onOpenDetail) },
            detail = { EditorialDetailPane(dish = dishes.first()) },
            placeholder = { EditorialDetailPane(dish = dishes.first()) },
        )
    }
    direction("Quick pantry") {
        screen("pantry") {
            QuickPantryScreen()
        }
    }
}

@Composable
private fun EditorialHomePane(onOpenDetail: () -> Unit) {
    val featured = dishes.first()
    val scheme = MaterialTheme.colorScheme
    Scaffold(
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text("Tonight's Plate") },
                subtitle = { Text("Weeknight table") },
            )
        },
        containerColor = scheme.surface,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Image(
                            painter = designPainter(featured.image),
                            contentDescription = featured.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(MaterialTheme.shapes.medium),
                        )
                        Text(
                            text = featured.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = scheme.onSurface,
                        )
                        Text(
                            text = featured.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                        )
                        Button(
                            onClick = onOpenDetail,
                            modifier = Modifier.testTag(AD_PRIMARY_TAG),
                            shapes = ButtonDefaults.shapesFor(56.dp),
                            contentPadding = ButtonDefaults.contentPaddingFor(56.dp),
                        ) {
                            Text("Start cooking")
                        }
                    }
                }
            }
            item {
                Text(
                    text = "More for tonight",
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurface,
                )
            }
            itemsIndexed(dishes.drop(1)) { index, dish ->
                SegmentedListItem(
                    onClick = onOpenDetail,
                    modifier = Modifier.widthIn(max = 420.dp),
                    shapes = ListItemDefaults.segmentedShapes(index = index, count = dishes.size - 1),
                    leadingContent = {
                        Image(
                            painter = designPainter(dish.image),
                            contentDescription = dish.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.small),
                        )
                    },
                    supportingContent = {
                        Text(
                            text = dish.subtitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    trailingContent = {
                        Text(
                            text = dish.minutes,
                            style = MaterialTheme.typography.labelLarge,
                            color = scheme.onSurfaceVariant,
                        )
                    },
                ) {
                    Text(dish.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun EditorialDetailPane(dish: Dish) {
    val scheme = MaterialTheme.colorScheme
    Surface(color = scheme.surfaceContainerLowest, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = dish.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = scheme.onSurface,
                )
            }
            item {
                Surface(
                    color = scheme.surfaceContainerLow,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Ingredients",
                            style = MaterialTheme.typography.titleMedium,
                            color = scheme.onSurface,
                        )
                        dish.ingredients.forEach { item ->
                            Text(
                                text = "- $item",
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            item {
                Surface(
                    color = scheme.surfaceContainerLow,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Steps",
                            style = MaterialTheme.typography.titleMedium,
                            color = scheme.onSurface,
                        )
                        dish.steps.forEachIndexed { idx, step ->
                            Text(
                                text = "${idx + 1}. $step",
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickPantryScreen() {
    val scheme = MaterialTheme.colorScheme
    var selectedTag by remember { mutableStateOf("All") }
    val tags = listOf("All", "Skillet", "Pasta", "Broth")
    Scaffold(
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text("Quick Pantry") },
                subtitle = { Text("Under 35 min") },
            )
        },
        containerColor = scheme.surface,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    tags.forEach { tag ->
                        FilterChip(
                            selected = selectedTag == tag,
                            onClick = { selectedTag = tag },
                            label = { Text(tag) },
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = {},
                    modifier = Modifier.testTag(AD_PRIMARY_TAG),
                    shapes = ButtonDefaults.shapesFor(56.dp),
                    contentPadding = ButtonDefaults.contentPaddingFor(56.dp),
                ) {
                    Text("Cook now")
                }
            }
            itemsIndexed(dishes) { index, dish ->
                SegmentedListItem(
                    onClick = {},
                    modifier = Modifier.widthIn(max = 420.dp),
                    shapes = ListItemDefaults.segmentedShapes(index = index, count = dishes.size),
                    leadingContent = {
                        Image(
                            painter = designPainter(dish.image),
                            contentDescription = dish.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.small),
                        )
                    },
                    supportingContent = {
                        Text(dish.subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    trailingContent = {
                        Text(
                            text = dish.minutes,
                            style = MaterialTheme.typography.labelLarge,
                            color = scheme.onSurfaceVariant,
                        )
                    },
                ) {
                    Text(dish.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
