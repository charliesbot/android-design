# Create a scrollable grid
Source: https://developer.android.com/develop/ui/compose/quick-guides/content/create-scrollable-grid

You can manage large datasets and dynamic content with lazy grids, improving app performance. With lazy grid composables, you can display items in a scrollable container, spanned across multiple columns or rows.

## Results

**Figure 1.** A horizontal scrollable grid using `LazyHorizontalGrid`.

## Version compatibility

This implementation requires that your project minSDK be set to API level 21 or higher.

### Dependencies

## Decide grid orientation

The `LazyHorizontalGrid` and `LazyVerticalGrid` composables provide support for displaying items in a grid. A lazy vertical grid displays its items in a vertically scrollable container, spanned across multiple columns, while lazy horizontal grids have the same behavior on the horizontal axis.

### Create a scrollable grid

The following code creates a horizontal scrolling grid with three columns:

```
@Composable
fun ScrollingGrid() {
val itemsList = (0..15).toList()

val itemModifier = Modifier
.border(1.dp, Color.Blue)
.width(80.dp)
.wrapContentSize()

LazyHorizontalGrid(
rows = GridCells.Fixed(3),
horizontalArrangement = Arrangement.spacedBy(16.dp),
verticalArrangement = Arrangement.spacedBy(16.dp)
) {
items(itemsList) {
Text("Item is $it", itemModifier)
}

item {
Text("Single item", itemModifier)
}
}
}
```

### Key points about the code
- The `LazyHorizontalGrid` composable determines the horizontal orientation of the grid.
  - To create a vertical grid, use the `LazyVerticalGrid` instead.

- The `rows` property specifies how to arrange the grid content.
  - For a vertical grid, use the `columns` property to specify the arrangement.

- `items(itemsList)` adds `itemsList` to `LazyHorizontalGrid`. The lambda renders a `Text` composable for each item and set the text to the item description.
- `item()` adds a single item to `LazyHorizontalGrid` while the lambda renders a single `Text` composable in a similar manner to `items()`.
- `GridCells.Fixed` defines the number of rows or columns.
- To create a grid with as many rows as possible, set the number of rows using `GridCells.Adaptive`.
In the following code, the `20.dp` value specifies that every column is at least 20.dp, and all columns have equal widths. If the screen is 88.dp wide, there are 4 columns at 22.dp each.

## Collections that contain this guide

This guide is part of these curated Quick Guide collections that cover broader Android development goals:

###### Display a list or grid
Lists and grids allow your app to display collections in a visually pleasing form that's easy for users to consume.
Quick guide collection

###### Display interactive components
Learn how composable functions can enable you to easily create beautiful UI components based on the Material Design design system.
Quick guide collection

###### Compose basics (video collection)
This series of videos introduces various Compose APIs, quickly showing you what’s available and how to use them.
Quick guide collection
