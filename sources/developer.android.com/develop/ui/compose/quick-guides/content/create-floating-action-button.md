# Create a floating action button (FAB)
Source: https://developer.android.com/develop/ui/compose/quick-guides/content/create-floating-action-button

A floating action button (FAB) is a high-emphasis button that lets the user perform a primary action in an application. It promotes a single, focused action that is the most common pathway a user might take and is typically found anchored to the bottom right of the screen.

Consider these three use cases where you might use a FAB:
- **Create new item**: In a note-taking app, a FAB might be used to quickly create a new note.
- **Add new contact**: In a chat app, a FAB could open an interface that lets the user add someone to a conversation.
- **Center location**: In a map interface, a FAB could center the map on the user's current location.

In Material Design, there are four types of FAB:
- FAB: A floating action button of ordinary size.
- Small FAB: A smaller floating action button.
- Large FAB: A larger floating action button.
- Extended FAB: A floating action button that contains more than just an icon.

## Version compatibility

This implementation requires that your project minSDK be set to API level 21 or higher.

### Dependencies

###### Kotlin

```
implementation(platform("androidx.compose:compose-bom:2026.09.00"))
```

###### Groovy

```
implementation platform('androidx.compose:compose-bom:2026.09.00')
```

## Create a basic floating action button

To create a general floating action button, use the basic `FloatingActionButton` composable:

```
@Composable
fun Example(onClick: () -> Unit) {
FloatingActionButton(
onClick = { onClick() },
) {
Icon(painter = rememberVectorPainter(AppIcons.Add), "Floating action button.")
}
}
```

### Result
[IMAGE] A standard floating action button with rounded corner, a shadow, and an 'add' icon.

**Figure 1.** A floating action button.

## Create a small floating action button

To create a small floating action button, use the `SmallFloatingActionButton` composable. The following example demonstrates how to do so, with the addition of custom colors.

```
@Composable
fun SmallExample(onClick: () -> Unit) {
SmallFloatingActionButton(
onClick = { onClick() },
containerColor = MaterialTheme.colorScheme.secondaryContainer,
contentColor = MaterialTheme.colorScheme.secondary
) {
Icon(painter = rememberVectorPainter(AppIcons.Add), "Small floating action button.")
}
}
```
**Note:** Because the various FAB composables share many parameters, you can use the approach in this example to customize colors with other composables.

### Result
[IMAGE] An implementation of SmallFloatingActionButton that contains an 'add' icon.

**Figure 2.** A small floating action button.

## Create a large floating action button

To create a large floating action button, use the `LargeFloatingActionButton` composable. This composable is not significantly different from the other examples aside from the fact that it results in a bigger button.

The following is a straightforward implementation of a large FAB. **Note:** This example passes `CircleShape` as the value for the `shape` parameter, resulting in a round button, rather than a square with rounded borders. You can pass any instance of `Shape`, or set the value of `MaterialTheme.shape.large` to adjust it across your app.

```
@Composable
fun LargeExample(onClick: () -> Unit) {
LargeFloatingActionButton(
onClick = { onClick() },
shape = CircleShape,
) {
Icon(painter = rememberVectorPainter(AppIcons.Add), "Large floating action button")
}
}
```

### Result
[IMAGE] An implementation of LargeFloatingActionButton that contains an 'add' icon.

**Figure 3.** A large floating action button.

## Create an extended floating action button

You can create more complex floating action buttons with the `ExtendedFloatingActionButton` composable. The key difference between it and `FloatingActionButton` is that it has dedicated `icon` and `text` parameters. They let you create a button with more complex content that scales to fit its content appropriately.

The following snippet demonstrates how to implement `ExtendedFloatingActionButton`, with example values passed for `icon` and `text`.

```
@Composable
fun ExtendedExample(onClick: () -> Unit) {
ExtendedFloatingActionButton(
onClick = { onClick() },
icon = { Icon(painter = rememberVectorPainter(AppIcons.Edit), "Extended floating action button.") },
text = { Text(text = "Extended FAB") },
)
}
```

### Result
[IMAGE] An implementation of ExtendedFloatingActionButton that displays text that says 'extended button' and an edit icon.

**Figure 4.** A floating action button with both text and an icon.

## Key points

Although there are several composables you can use to create floating action buttons consistent with Material Design, their parameters don't differ greatly. Among the key parameters you should keep in mind are the following:
- `onClick`: The function called when the user presses the button.
- `containerColor`: The color of the button.
- `contentColor`: The color of the icon.

z## Collections that contain this guide

This guide is part of these curated Quick Guide collections that cover broader Android development goals:

###### Display interactive components
Learn how composable functions can enable you to easily create beautiful UI components based on the Material Design design system.
Quick guide collection
