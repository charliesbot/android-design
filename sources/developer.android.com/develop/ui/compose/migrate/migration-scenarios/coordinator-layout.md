# Migrate CoordinatorLayout to Compose
Source: https://developer.android.com/develop/ui/compose/migrate/migration-scenarios/coordinator-layout

`CoordinatorLayout` is a `ViewGroup` that enables complex, overlapping, and nested layouts. It's used as a container to enable specific Material Design interactions, such as expanding/collapsing toolbars and bottom sheets, for Views contained within it.

In Compose, the closest equivalent of a `CoordinatorLayout` is a `Scaffold`. A `Scaffold` provides content slots for combining Material Components into common screen patterns and interactions. This page describes how you can migrate your `CoordinatorLayout` implementation to use `Scaffold` in Compose.

## Migration steps

To migrate `CoordinatorLayout` to `Scaffold`, follow these steps:
- In the snippet below, the `CoordinatorLayout` contains an `AppBarLayout` for containing a `ToolBar`, a `ViewPager`, and a `FloatingActionButton`. Comment out the `CoordinatorLayout` and its children from your UI hierarchy and add a `ComposeView` to replace it.
```xml

```
<!-- AppBarLayout contents here -->

```

```  **Note:** Since `CoordinatorLayout` is a `ViewGroup`, it's best to migrate all its child views to Compose at the same time or prior to this step, depending on your migration strategy. However, if you are unable to do so, you can add an `AndroidView` to use Views within Compose. See Using Views in Compose to learn more.
- In your Fragment or Activity, obtain a reference to the `ComposeView` you just added and call the `setContent` method on it. In the body of the method, set a `Scaffold` as its content:

```
composeView.setContent {
Scaffold(Modifier.fillMaxSize()) { contentPadding ->
// Scaffold contents
// ...
}
}
```

- In the content of your `Scaffold`, add your screen's primary content within it. Because the primary content in the XML above is a `ViewPager2`, we'll use a `HorizontalPager`, which is the Compose equivalent of it. The `content` lambda of the `Scaffold` also receives an instance of `PaddingValues` that should be applied to the content root. You can use `Modifier.padding` to apply the same `PaddingValues` to the `HorizontalPager`.

```
composeView.setContent {
Scaffold(Modifier.fillMaxSize()) { contentPadding ->
val pagerState = rememberPagerState {
10
}
HorizontalPager(
state = pagerState,
modifier = Modifier.padding(contentPadding)
) { /* Page contents */ }
}
}
```

- Use other content slots that `Scaffold` provides to add more screen elements and migrate remaining child Views. You can use the `topBar` slot to add a `TopAppBar`, and the `floatingActionButton` slot to provide a `FloatingActionButton`.

```
composeView.setContent {
Scaffold(
Modifier.fillMaxSize(),
topBar = {
TopAppBar(
title = {
Text("My App")
}
)
},
floatingActionButton = {
FloatingActionButton(
onClick = { /* Handle click */ }
) {
Icon(
Icons.Filled.Add,
contentDescription = "Add Button"
)
}
}
) { contentPadding ->
val pagerState = rememberPagerState {
10
}
HorizontalPager(
state = pagerState,
modifier = Modifier.padding(contentPadding)
) { /* Page contents */ }
}
}
```

## Common use cases

### Collapse and expand toolbars

In the View system, to collapse and expand the toolbar with `CoordinatorLayout`, you use an `AppBarLayout` as a container for the toolbar. You can then specify a `Behavior` through `layout_behavior` in XML on the associated scrollable View (like `RecyclerView` or `NestedScrollView`) to declare how the toolbar collapses/expands as you scroll.

In Compose, you can achieve a similar effect through a `TopAppBarScrollBehavior`. For example, to implement a collapsing/expanding toolbar so that the toolbar appears when you scroll up, follow these steps:
- Call `TopAppBarDefaults.enterAlwaysScrollBehavior()` to create a `TopAppBarScrollBehavior`.
- Provide the created `TopAppBarScrollBehavior` to the `TopAppBar`.
- Connect the `NestedScrollConnection` via `Modifier.nestedScroll` on the `Scaffold` so that the Scaffold can receive nested scroll events as the scrollable content scrolls up/down. This way, the contained app bar can appropriately collapse/expand as the content scrolls.

```
// 1. Create the TopAppBarScrollBehavior
val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

Scaffold(
topBar = {
TopAppBar(
title = {
Text("My App")
},
// 2. Provide scrollBehavior to TopAppBar
scrollBehavior = scrollBehavior
)
},
// 3. Connect the scrollBehavior.nestedScrollConnection to the Scaffold
modifier = Modifier
.fillMaxSize()
.nestedScroll(scrollBehavior.nestedScrollConnection)
) { contentPadding ->
/* Contents */
// ...
}
```

**Note:** If you are using Material 2 components, you must manually implement the collapsing/expanding toolbar yourself. Alternatively, you can use a `CoordinatorLayout` as the outer layout to your `ComposeView`. This method is documented in the Migrating Sunflower to Jetpack Compose blog post.

#### Customize the collapsing/expanding scroll effect

You can provide several parameters for `enterAlwaysScrollBehavior` to customize the collapsing/expanding animation effect. `TopAppBarDefaults` also provides other `TopAppBarScrollBehavior` such as `exitUntilCollapsedScrollBehavior`, which only expands the app bar when the content is scrolled all the way down.

To create a completely custom effect (for example, a parallax effect), you can also create your own `NestedScrollConnection` and offset the toolbar manually as the content scrolls. See the Nested scroll sample on AOSP for a code example.

### Drawers

With Views, you implement a navigation drawer by using a `DrawerLayout` as the root view. In turn, your `CoordinatorLayout` is a child view of the `DrawerLayout`. The `DrawerLayout` also contains another child view, such as a `NavigationView`, to display the navigation options in the drawer.

In Compose, you can implement a navigation drawer using the `ModalNavigationDrawer` composable. `ModalNavigationDrawer` offers a `drawerContent` slot for the drawer and a `content` slot for the screen's content.

```
ModalNavigationDrawer(
drawerContent = {
ModalDrawerSheet {
Text("Drawer title", modifier = Modifier.padding(16.dp))
HorizontalDivider()
NavigationDrawerItem(
label = { Text(text = "Drawer Item") },
selected = false,
onClick = { /*TODO*/ }
)
// ...other drawer items
}
}
) {
Scaffold(Modifier.fillMaxSize()) { contentPadding ->
// Scaffold content
// ...
}
}
```

See Drawers to learn more.

### Snackbars

`Scaffold` provides a `snackbarHost` slot, which can accept a `SnackbarHost` composable to display a `Snackbar`.

```
val scope = rememberCoroutineScope()
val snackbarHostState = remember { SnackbarHostState() }
Scaffold(
snackbarHost = {
SnackbarHost(hostState = snackbarHostState)
},
floatingActionButton = {
ExtendedFloatingActionButton(
text = { Text("Show snackbar") },
icon = { Icon(Icons.Filled.Image, contentDescription = "") },
onClick = {
scope.launch {
snackbarHostState.showSnackbar("Snackbar")
}
}
)
}
) { contentPadding ->
// Screen content
// ...
}
```

See Snackbars to learn more.

## Learn more

For more information about migrating a `CoordinatorLayout` to Compose, see the following resources:
- Material Components and layouts: Documentation on Material Design components that are supported in Compose, like `Scaffold`.
- Migrating Sunflower to Jetpack Compose: A blog post that documents the migration journey from Views to Compose of the Sunflower sample app, which contains a `CoordinatorLayout`.

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Material Components and layouts
- Window insets in Compose
- Scroll
