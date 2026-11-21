# Restore focus
Source: https://developer.android.com/develop/ui/compose/touch-input/focus/focus-restoration

When users navigate complex user interfaces using a hardware keyboard or D-pad, they frequently move between different sections of the screen—such as jumping from a side navigation rail to a content list, interacting with an item, and then jumping back. _Focus restoration_ lets containers remember the previously focused child element and restore focus to it when focus re-enters the container. **Note:** This guide focuses on runtime navigation focus restoration across UI components and containers. Preserving focus across configuration changes or process recreation is handled through Compose's state restoration mechanisms.

## Restore focus in scrollable containers

In Jetpack Compose, scrollable containers such as `LazyColumn`, `LazyRow`, and `LazyVerticalGrid` automatically act as focus groups. To enable focus restoration on a lazy list or grid, attach the `focusRestorer` modifier:

```
LazyColumn(
modifier = Modifier.focusRestorer()
) {
items(items) { item ->
Button(onClick = { /* Handle click */ }) {
Text(item)
}
}
}
```

When focus exits the `LazyColumn`, the container records which item was active. When the user navigates back into the list, focus is restored to that item instead of restarting at the top.

## Restore focus in Row and Column

Unlike lazy layout containers, standard non-scrollable layout containers such as `Row` and `Column` don't establish a focus boundary by default. Because they don't intercept focus entry events on their own, applying the `focusRestorer` modifier alone is not sufficient.

To enable focus restoration on `Row` or `Column`, combine `focusGroup` with `focusRestorer`:

```
Row(
modifier = Modifier
.focusGroup()
.focusRestorer()
) {
Button(onClick = { /* Action 1 */ }) {
Text("First item")
}
Button(onClick = { /* Action 2 */ }) {
Text("Second item")
}
Button(onClick = { /* Action 3 */ }) {
Text("Third item")
}
}
```

## Specify a fallback focus target

When focus enters a container for the first time, no previously focused child exists. Compose follows the default focus order to determine which child to focuses.

You can customize which child receives focus on the first entry by passing a fallback `FocusRequester` to `focusRestorer` (or a lambda returning a `FocusRequester` on earlier Compose versions):

```
val firstItemRequester = remember { FocusRequester() }

LazyColumn(
modifier = Modifier.focusRestorer(firstItemRequester)
) {
itemsIndexed(items) { index, item ->
val itemModifier = if (index == 0) {
Modifier.focusRequester(firstItemRequester)
} else {
Modifier
}
Button(
onClick = { /* Handle click */ },
modifier = itemModifier
) {
Text(item)
}
}
}
```

If no child has been previously focused (or if focus restoration fails), Compose moves focus to the associated fallback target.

## Direct focus to the selected item in navigation components

In navigation components such as `NavigationRail`, `NavigationBar`, `NavigationDrawer`, or `TabRow`, users expect focus to land on the currently selected destination when entering the component from the content area.

You can direct focus to the active destination by combining `Modifier.focusGroup()` with `focusProperties` and setting the `onEnter` property (or `enter` on earlier Compose versions):

```
val (homeRequester, searchRequester, settingsRequester) = remember { FocusRequester.createRefs() }

val selectedRequester = when (currentDestination) {
"home" -> homeRequester
"search" -> searchRequester
"settings" -> settingsRequester
else -> homeRequester
}

NavigationRail(
modifier = modifier
.focusProperties {
// Redirect focus to the selected item when focus enters the navigation rail
onEnter = { selectedRequester.requestFocus() }
}
.focusGroup()
) {
NavigationRailItem(
selected = currentDestination == "home",
onClick = { onNavigate("home") },
icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
label = { Text("Home") },
modifier = Modifier.focusRequester(homeRequester)
)
NavigationRailItem(
selected = currentDestination == "search",
onClick = { onNavigate("search") },
icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
label = { Text("Search") },
modifier = Modifier.focusRequester(searchRequester)
)
NavigationRailItem(
selected = currentDestination == "settings",
onClick = { onNavigate("settings") },
icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
label = { Text("Settings") },
modifier = Modifier.focusRequester(settingsRequester)
)
}
```

## Restore focus across screen transitions

When a user clicks an item to navigate to a detail screen and subsequently returns using the Back button or gesture, you can restore focus to the item they clicked.

To preserve and restore focus across screen transitions:
- Associate the container with a `FocusRequester`.
- Before triggering the navigation event, call `saveFocusedChild()` on the container's `FocusRequester`.
- When the user navigates back to the screen, request initial focus on the container or call `restoreFocusedChild()`.

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Focus targets
- Focus traversal order
- Group focus targets
- Programmatically request focus
- Move and clear focus

Previous  arrow_back   Group focus targets

Next  Indicate focus state   arrow_forward
