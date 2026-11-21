# Create a finite scrollable list
Source: https://developer.android.com/develop/ui/compose/quick-guides/content/finite-scrolling-list

Scrollable lists can help manage datasets, create responsive designs, and facilitate navigation. You can display smaller sets of items in your app by using a finite scrolling list. To avoid performance issues with larger datasets or a list of unknown length, see Lazily load data with lists and Paging.

## Results

[IMAGE] A vertical list responding to scroll
gestures

**Figure 1.** A vertical scrolling list.

## Version compatibility

This implementation requires that your project minSDK be set to API level 21 or higher.

### Dependencies

## Create a vertical scrolling list

Use the following code to create a vertical scrolling list:

```
@Composable
private fun ScrollBoxes() {
Column(
modifier = Modifier
.background(Color.LightGray)
.size(100.dp)
.verticalScroll(rememberScrollState())
) {
repeat(10) {
Text("Item $it", modifier = Modifier.padding(2.dp))
}
}
}
```

### Key points about the code
- Sets the `Column` scrolling behavior with the `verticalScroll` modifier and the `rememberScrollState` function.
- To create a horizontal scrolling list, create a `Row` with a `horizontalScroll` modifier.

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
