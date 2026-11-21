# Create a button to enable snap scrolling
Source: https://developer.android.com/develop/ui/compose/quick-guides/content/enable-snap-scrolling

You can display a button to let a user snap scroll to a specific point in a list, saving time and increasing user engagement.

## Results
[IMAGE] A vertically scrolling list with an active button

**Figure 1.** A vertical scrolling list with a snap scroll button.

## Version compatibility

This implementation requires that your project minSDK be set to API level 21 or higher.

### Dependencies

## Create a button to enable snap scrolling

Use the following code to create a button for smooth snap scrolling in a vertical lazy list with 10 items:

```
@Composable
fun MessageList(modifier: Modifier = Modifier) {
val listState = rememberLazyListState()
val coroutineScope = rememberCoroutineScope()

LazyColumn(state = listState, modifier = Modifier.height(120.dp)) {
items(10) { index ->
Text(
modifier = Modifier.height(40.dp),
text = "Item $index"
)
}
}

Button(onClick = {
coroutineScope.launch {
listState.animateScrollToItem(index = 0)
}
}) {
Text(text = "Go top")
}
}
```

### Key points about the code
- Uses the `listState` object to remember the scroll state of `LazyColumn` to the selected position.
- Launches a coroutine to call `listState.animateScrollToItem`, which scrolls to the indexed item while animating the scrolling action.

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
