# Use Material 3 insets
Source: https://developer.android.com/develop/ui/compose/system/material-insets

For ease of use, many of the built-in Material 3 composables (`androidx.compose.material3`) handle insets themselves, based on how the composables are placed in your app according to the Material specifications. **Note:** Material 2 Components (`androidx.compose.material`) don't automatically handle insets themselves. However, you can get access to the insets and apply them manually. In `androidx.compose.material 1.6.0` and later use the `windowInsets` parameter to apply the insets manually for `BottomAppBar`, `TopAppBar`, `BottomNavigation`, and `NavigationRail`. Likewise, use the `contentWindowInsets` parameter for `Scaffold`. Otherwise, apply the insets manually as padding.

## Inset handling composables

The following is a list of the Material Components that automatically handle insets.

## App bars
- `TopAppBar` / `CenterAlignedTopAppBar` / `MediumTopAppBar` / `LargeTopAppBar`: Applies the _top_ and _horizontal_ sides of the system bars as padding since it is used at the top of the window.
- `BottomAppBar`: Applies the _bottom_ and _horizontal_ sides of the system bars as padding.

### Content containers
- `ModalDrawerSheet` / `DismissibleDrawerSheet` / `PermanentDrawerSheet` (content inside a modal navigation drawer): Applies _vertical_ and _start_ insets to content.
- `ModalBottomSheet`: Applies the _bottom_ insets.
- `NavigationBar`: Applies the _bottom_ and _horizontal_ insets.
- `NavigationRail`: Applies the _vertical_ and _start_ insets.

### Scaffold

By default, `Scaffold` provides insets as parameter `PaddingValues` for you to consume and use. `Scaffold` does not apply the insets to content; this responsibility is yours. For example, to consume these insets with a `LazyColumn` inside a `Scaffold`:

```
Scaffold { innerPadding ->
// innerPadding contains inset information for you to use and apply
LazyColumn(
// consume insets as scaffold doesn't do it by default
modifier = Modifier.consumeWindowInsets(innerPadding),
contentPadding = innerPadding
) {
// ..
}
}
```

The following video shows a `LazyColumn` within a `Scaffold` with edge-to-edge display disabled and enabled:   Alas, your browser doesn't support HTML5 video. That's OK! You can still  download the video and watch it with a video player.

Using the `PaddingValues` parameter in `Scaffold` is generally enough to inset your UI away from the system UI and display cutouts. Avoid using additional inset handling approaches like rulers, padding modifiers, or inset size modifiers if using `Scaffold` to avoid applying too much padding to your UI.

## Override default insets

You can change the `windowInsets` parameter passed to the composable to configure the composable's behavior. This parameter can be a different type of window inset to apply instead, or disabled by passing an empty instance: `WindowInsets(0, 0, 0, 0)`.

For example, to disable the inset handling on `LargeTopAppBar`, set the `windowInsets` parameter to an empty instance:

```
LargeTopAppBar(
windowInsets = WindowInsets(0, 0, 0, 0),
title = {
Text("Hi")
}
)
```

Previous  arrow_back   Use keyboard IME animations

Next  Use insets in Views and Compose   arrow_forward
