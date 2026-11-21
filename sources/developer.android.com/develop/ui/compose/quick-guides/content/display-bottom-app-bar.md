# Display a bottom app bar
Source: https://developer.android.com/develop/ui/compose/quick-guides/content/display-bottom-app-bar

Create a bottom app bar to help users navigate and access functions in your app. Follow this guidance to add a bottom app bar to your app by using the `BottomAppBar` composable.

## Results
[IMAGE] An example of a bottom app bar

**Figure 1.** An example of a bottom app bar.

## Version compatibility

This implementation requires that your project minSDK be set to API level 21 or higher.

### Dependencies

## Create a bottom app bar

Use the following code to create a bottom app bar containing four icon buttons, and a floating action button:

```
@Composable
fun BottomAppBarExample() {
Scaffold(
bottomBar = {
BottomAppBar(
actions = {
IconButton(onClick = { /* do something */ }) {
Icon(painter = rememberVectorPainter(AppIcons.Check), contentDescription = "Localized description")
}
IconButton(onClick = { /* do something */ }) {
Icon(
painter = rememberVectorPainter(AppIcons.Edit),
contentDescription = "Localized description",
)
}
IconButton(onClick = { /* do something */ }) {
Icon(
painter = rememberVectorPainter(AppIcons.Mic),
contentDescription = "Localized description",
)
}
IconButton(onClick = { /* do something */ }) {
Icon(
painter = rememberVectorPainter(AppIcons.Image),
contentDescription = "Localized description",
)
}
},
floatingActionButton = {
FloatingActionButton(
onClick = { /* do something */ },
containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
) {
Icon(painter = rememberVectorPainter(AppIcons.Add), "Localized description")
}
}
)
},
) { innerPadding ->
Text(
modifier = Modifier.padding(innerPadding),
text = "Example of a scaffold with a bottom app bar."
)
}
}
```

### Key points about the code
- An outer `Scaffold` that has a `bottomBar` set.
- A `bottomBar` implementation that contains a list of actions.
- Actions that are implementations of `IconButton` that contain `Icon` for image and content description text, each with an `onClick` lambda to perform these actions.

You can pass composables for the following key parameters:
- `actions`: a series of icons that appear on the left side of the bar. These are commonly either key actions for the given screen, or navigation items.
- `floatingActionButton`: the floating action button that appears on the right side of the bar.
**Note:** After you add the bottom app bar, you can customize it with content as you do with other containers by filling the `BottomAppBar` composable with other content. You can also use `BottomAppBar` without passing a value for `actions` or `floatingActionButton`.

## Collections that contain this guide

This guide is part of these curated Quick Guide collections that cover broader Android development goals:

###### Create a home screen scaffold
Find out how to use a standardized platform to build complex user interfaces. The scaffold holds together different parts of the UI, giving apps a coherent look and feel.
Quick guide collection

###### Display interactive components
Learn how composable functions can enable you to easily create beautiful UI components based on the Material Design design system.
Quick guide collection
