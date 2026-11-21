# Display nested scrolling items in a list
Source: https://developer.android.com/develop/ui/compose/quick-guides/content/display-nested-list

You can display nested scrolling items within a list to present complex layouts, such as product catalogs, media galleries, news feeds, and more.

## Results

The following video shows the resulting behaviors of nested horizontal lists within a vertical scrolling list.

## Version compatibility

This implementation requires that your project minSDK be set to API level 21 or higher.

### Dependencies

## Implement nested horizontal scrolling in vertical list

The following code produces a list that scrolls two ways. The rows of the list scroll horizontally; the list as a whole—a single column—scrolls vertically.

```
@Composable
fun NestedScrollingRowsList(urls: List<String>) {
LazyColumn {
items(10) {
LazyRow {
item { Text("Row: $it") }
items(urls.size) { index ->
// AsyncImage provided by Coil.
AsyncImage(
model = urls[index],
modifier = Modifier.size(150.dp),
contentDescription = null
)
}
}
}
}
}
```

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
