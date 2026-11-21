# Badges
Source: https://developer.android.com/develop/ui/compose/components/badges

Use a badge to display a small visual element to denote status or a numeric value on another composable. Here are a few common scenarios where you might use a badge:
- **Notifications**: Display the number of unread notifications on an app icon or notification bell.
- **Messages**: Indicate new or unread messages within a chat application.
- **Status updates**: Show the status of a task, such as "complete," "in progress," or "failed."
- **Cart quantity**: Display the number of items in a user's shopping cart.
- **New content**: Highlight new content or features available to the user.

[IMAGE] Different examples of the badge component.

**Figure 1.** Badge examples

## API surface

Use the `BadgedBox` composable to implement badges in your application. It is ultimately a container. You control its appearance with these two main parameters:
- `content`: The composable content that the `BadgedBox` contains. Typically `Icon`.
- `badge`: The composable that appears as the badge over the content. Typically the dedicated `Badge` composable.

## Basic example

This code snippet shows a basic implementation of `BadgedBox`:

```
@Composable
fun BadgeExample() {
BadgedBox(
badge = {
Badge()
}
) {
Icon(
painter = rememberVectorPainter(AppIcons.Mail),
contentDescription = "Email"
)
}
}
```

This example displays a badge that overlaps the provided `Icon` composable. Note the following in the code:
- `BadgedBox` serves as the overall container.
- The argument for the `badge` parameter of `BadgedBox` is `Badge`. Because `Badge` has no arguments of its own, the app displays the default badge, which is a small red circle.
- `Icon` serves as the argument for the `content` parameter of `BadgedBox`. It is the icon over which the badge appears. In this case, it is a mail icon.

This is how it appears:
[IMAGE] A mail icon with a small red badge at its top-right corner.

**Figure 2.** A minimal badge implementation.  **Important:** You can pass any composable to the `BadgedBox` composable's content slot. You could therefore create a badge that displays on a `Button`, an `Image`, or any other composable.

## Detailed example

The following snippet demonstrates how you can display values in the badge that respond to user actions.

```
@Composable
fun BadgeInteractiveExample() {
var itemCount by remember { mutableIntStateOf(3) }

Column(
verticalArrangement = Arrangement.spacedBy(16.dp),
horizontalAlignment = Alignment.CenterHorizontally,
) {
BadgedBox(
badge = {
if (itemCount > 0) {
Badge(
containerColor = Color.Red,
contentColor = Color.White
) {
Text("$itemCount")
}
}
}
) {
Icon(
painter = rememberVectorPainter(AppIcons.ShoppingCart),
contentDescription = "Shopping cart",
)
}
Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
FilledTonalIconButton(
onClick = { itemCount-- },
enabled = itemCount > 0,
) {
Icon(
painter = rememberVectorPainter(AppIcons.Remove),
contentDescription = "Remove item",
)
}
FilledIconButton(onClick = { itemCount++ }) {
Icon(
painter = rememberVectorPainter(AppIcons.Add),
contentDescription = "Add item",
)
}
}
}
}
```

This example implements a shopping cart icon with a badge that displays the number of items in the user's cart.
- The `BadgedBox` displays only when the item count is over 0.
- The arguments for `containerColor` and `contentColor` control the appearance of the badge.
- The `Text` composable for the content slot of `Badge` appears within the badge. In this case, it displays the number of items in the cart.
- The remove and add icon buttons decrease and increase the item count. The remove button is disabled when the cart is empty.

This implementation appears as follows:
[IMAGE] A shopping cart icon with a red badge that shows 3, above remove and add icon buttons.

**Figure 3.** A badge that displays the number of items in a shopping cart.

## Additional resources
- Material 3 - Badges
- `BadgedBox`
- `Badge`
