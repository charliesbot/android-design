# App bars
Source: https://developer.android.com/develop/ui/compose/components/app-bars

App bars are containers that provide the user access to key features and navigation items. There are two types of app bars, top app bars and bottom app bars. Their respective appearance and purpose are as follows:
|

Type  |

Appearance  |

Purpose
|

Top app bar  |

Across the top of the screen.  |

Provides access to key tasks and information. Generally hosts a title, core action items, and certain navigation items.
|

Bottom app bar  |

Across the bottom of the screen.  |

Typically includes core navigation items. May also provide access to other key actions, such as through a contained floating action button.
[IMAGE] An example of a top and bottom app bar.

**Figure 1.** A top app bar (left) and a bottom app bar (right).

To implement a top app bar and bottom app bar, use the `TopAppBar` and `BottomAppBar` composables, respectively. They let you create consistent interfaces that encapsulate navigation and action controls, and which are aligned with Material Design principles. **Important:** App bars are generally passed to the `Scaffold` composable, which has specific parameters to receive them. For more information about how `Scaffold` provides a convenient way of structuring your app, visit the `Scaffold` page.

## Top app bars

The following table outlines the four types of top app bars:
|

Type  |

Example
|

**Small**: For screens that don't require a lot of navigation or actions.  |
[IMAGE] Small top app bar

|

**Center aligned**: For screens that have a single, primary action.  |
[IMAGE] Center aligned top app bar

|

**Medium**: For screens that require a moderate amount of navigation and actions.  |
[IMAGE] Medium top app bar

|

**Large**: For screens that require a lot of navigation and actions.  |
[IMAGE] Large top app bar

### API surface

The various composables that allow you to implement the four different top app bars are quite similar. They share several key parameters:
- `title`: The text that appears across the app bar.
- `navigationIcon`: The primary icon for navigation. Appears on the left of the app bar.
- `actions`: Icons that provide the user access to key actions. They appear on the right of the app bar.
- `scrollBehavior`: Determines how the top app bar responds to scrolling of the scaffold's inner content.
- `colors`: Determines how the app bar appears.

### Scroll behavior

You can control how the app bar responds when the user scrolls the given scaffold's inner content. To do so, create an instance of `TopAppBarScrollBehavior` and pass it to your top app bar for the `scrollBehavior` parameter.

There are three types of `TopAppBarScrollBehavior`. They are as follows:
- `enterAlwaysScrollBehavior`: When the user pulls up the scaffold's inner content, the top app bar collapses. The app bar expands when the user starts pulling down the inner content.
- `exitUntilCollapsedScrollBehavior`: Similar to `enterAlwaysScrollBehavior`, though the app bar only expands when the user pulls the content all the way down.
- `pinnedScrollBehavior`: The app bar remains in place and does not react to scrolling.

The following examples implement several of these options.

### Examples

The following sections provide implementations for the four different types of top app bars, including varying examples of how you can control scroll behavior.

#### Small

To create a small top app bar, use the `TopAppBar` composable. The following example implements a basic top app bar that contains only a title.

The following example does not pass `TopAppBar` a value for `scrollBehavior` and it therefore does not react to scrolling of the inner content.

```
@Composable
fun SmallTopAppBarExample() {
Scaffold(
topBar = {
TopAppBar(
colors = TopAppBarDefaults.topAppBarColors(
containerColor = MaterialTheme.colorScheme.primaryContainer,
titleContentColor = MaterialTheme.colorScheme.primary,
),
title = {
Text("Small Top App Bar")
}
)
},
) { innerPadding ->
ScrollContent(innerPadding)
}
}
```

This implementation appears as follows:
[IMAGE] A small top app bar with the title 'Small Top App Bar' above a list of items.

**Figure 2.** A small top app bar.

#### Center aligned

The center aligned top app bar is essentially the same as the small app bar, though the title is centered within the component. To implement it, use the dedicated `CenterAlignedTopAppBar` composable.

This example uses `enterAlwaysScrollBehavior()` to get the value that it passes for `scrollBehavior`. As such, the bar collapses when the user scrolls the scaffold's inner content.

```
@Composable
fun CenterAlignedTopAppBarExample() {
val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

Scaffold(
modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

topBar = {
CenterAlignedTopAppBar(
colors = TopAppBarDefaults.topAppBarColors(
containerColor = MaterialTheme.colorScheme.primaryContainer,
titleContentColor = MaterialTheme.colorScheme.primary,
),
title = {
Text(
"Centered Top App Bar",
maxLines = 1,
overflow = TextOverflow.Ellipsis
)
},
navigationIcon = {
IconButton(onClick = { /* do something */ }) {
Icon(
painter = rememberVectorPainter(AppIcons.ArrowBack),
contentDescription = "Localized description"
)
}
},
actions = {
IconButton(onClick = { /* do something */ }) {
Icon(
painter = rememberVectorPainter(AppIcons.Menu),
contentDescription = "Localized description"
)
}
},
scrollBehavior = scrollBehavior,
)
},
) { innerPadding ->
ScrollContent(innerPadding)
}
}
```

This implementation appears as follows:
[IMAGE] A top app bar with a back arrow, the centered title 'Centered Top App Bar' and a menu icon, above a list of items.

**Figure 3.** A center-aligned top app bar.

#### Medium

The medium top app bar places the title beneath any additional icons. To create one, use the `MediumTopAppBar` composable.

Like the previous snippet, this example uses `enterAlwaysScrollBehavior()` to get the value that it passes for `scrollBehavior`.

```
@Composable
fun MediumTopAppBarExample() {
val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

Scaffold(
modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
topBar = {
MediumTopAppBar(
colors = TopAppBarDefaults.topAppBarColors(
containerColor = MaterialTheme.colorScheme.primaryContainer,
titleContentColor = MaterialTheme.colorScheme.primary,
),
title = {
Text(
"Medium Top App Bar",
maxLines = 1,
overflow = TextOverflow.Ellipsis
)
},
navigationIcon = {
IconButton(onClick = { /* do something */ }) {
Icon(
painter = rememberVectorPainter(AppIcons.ArrowBack),
contentDescription = "Localized description"
)
}
},
actions = {
IconButton(onClick = { /* do something */ }) {
Icon(
painter = rememberVectorPainter(AppIcons.Menu),
contentDescription = "Localized description"
)
}
},
scrollBehavior = scrollBehavior
)
},
) { innerPadding ->
ScrollContent(innerPadding)
}
}
```

[IMAGE] A medium top app bar with a back arrow and a menu icon, and the title 'Medium Top App Bar' below them, above a list of items.

This implementation appears as follows, with a demonstration of how the scroll behavior from `enterAlwaysScrollBehavior()` appears:
**Figure 4.** A medium top app bar that collapses as the user scrolls up and expands as the user scrolls down.

#### Large

A large top app bar is similar to the medium, though the padding between the title and the icons is greater and it occupies more space on screen overall. To create one, use the `LargeTopAppBar` composable.

Unlike the preceding snippets, this example uses `exitUntilCollapsedScrollBehavior()` to get the value that it passes for `scrollBehavior`. As such, the bar collapses when the user scrolls the scaffold's inner content, but then expands when the user scrolls to the end of the inner content.

```
@Composable
fun LargeTopAppBarExample() {
val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

Scaffold(
modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
topBar = {
LargeTopAppBar(
colors = TopAppBarDefaults.topAppBarColors(
containerColor = MaterialTheme.colorScheme.primaryContainer,
titleContentColor = MaterialTheme.colorScheme.primary,
),
title = {
Text(
"Large Top App Bar",
maxLines = 1,
overflow = TextOverflow.Ellipsis
)
},
navigationIcon = {
IconButton(onClick = { /* do something */ }) {
Icon(
painter = rememberVectorPainter(AppIcons.ArrowBack),
contentDescription = "Localized description"
)
}
},
actions = {
IconButton(onClick = { /* do something */ }) {
Icon(
painter = rememberVectorPainter(AppIcons.Menu),
contentDescription = "Localized description"
)
}
},
scrollBehavior = scrollBehavior
)
},
) { innerPadding ->
ScrollContent(innerPadding)
}
}
```

This implementation appears as follows:
[IMAGE] A large top app bar with a back arrow and a menu icon, and the title 'Large Top App Bar' below them, above a list of items.

**Figure 5.** An example implementation of a large top app bar.

### M3 Expressive update

The new **medium flexible** and **large flexible** app bars come with significant improvements, like flexible heights, multi-line support, and centered text. They can also contain a wider variety of elements, such as images. The medium flexible and large flexible app bars are recommended over the medium and large app bars. The small app bar has been updated with the same flexible improvements.
[IMAGE] Top app bars with expressive styles.

**Figure 6.** M3 Expressive top app bar variations.

For more information about the M3 Expressive updates for app bars, see the Material Design 3 App bars guidelines.

#### Center and left alignment
| Small  |
[IMAGE] Light theme center-aligned top app bar with subtitle

|
[IMAGE] Dark theme top app bar

| Medium  |
[IMAGE] Light theme center-aligned medium top app bar with subtitle

|
[IMAGE] Dark theme medium top app bar

| Large  |
[IMAGE] Light theme center-aligned large top app bar with subtitle

|
[IMAGE] Dark theme large top app bar

Small, medium flexible, and large flexible headlines can be aligned to the leading edge or centered.

The following snippet demonstrates how to implement a center-aligned top app bar with a subtitle:

```
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleCenterAlignedTopAppBarWithSubtitle() {
val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
Scaffold(
modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
topBar = {
TopAppBar(
title = { Text("Simple TopAppBar", maxLines = 1, overflow = TextOverflow.Ellipsis) },
subtitle = { Text("Subtitle", maxLines = 1, overflow = TextOverflow.Ellipsis) },
titleHorizontalAlignment = Alignment.CenterHorizontally,
navigationIcon = {
IconButton(onClick = { /* onBackClick() */ }) {
Icon(
painter = rememberVectorPainter(AppIcons.ArrowBack),
contentDescription = "Localized description",
)
}
},
actions = {
IconButton(onClick = { /* doSomething() */ }) {
Icon(painter = rememberVectorPainter(AppIcons.Favorite), contentDescription = "Localized description")
}
},
scrollBehavior = scrollBehavior,
)
},
content = { innerPadding ->
// ...
},
)
}
```

[IMAGE] A center-aligned top app bar with a back arrow, the title 'Simple TopAppBar', the subtitle 'Subtitle', and a favorite icon, above a list of items.

An example implementation of a center-aligned top app bar with a subtitle.

#### Scroll behavior

###### Enter always

When the user scrolls down through the content (pulling the content up), the app bar collapses immediately. When the user scrolls back up (pulling the content down), the app bar expands immediately, regardless of where the user is in the scrollable content list.

The following snippet demonstrates how to implement a top app bar with enter-always scroll behavior:

```
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterAlwaysTopAppBar() {
val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
Scaffold(
modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
topBar = {
TopAppBar(
title = { Text("TopAppBar", maxLines = 1, overflow = TextOverflow.Ellipsis) },
subtitle = { Text("Subtitle", maxLines = 1, overflow = TextOverflow.Ellipsis) },
navigationIcon = {
IconButton(onClick = { /* onBackClick() */ }) {
Icon(
painter = rememberVectorPainter(AppIcons.ArrowBack),
contentDescription = "Localized description",
)
}
},
actions = {
IconButton(onClick = { /* doSomething() */ }) {
Icon(painter = rememberVectorPainter(AppIcons.Favorite), contentDescription = "Localized description")
}
},
scrollBehavior = scrollBehavior,
)
},
content = { innerPadding ->
// ...
},
)
}
```

[IMAGE] A top app bar with a back arrow, the title 'TopAppBar', the subtitle 'Subtitle', and a favorite icon, above a list of items.

The following video shows the result of the preceding snippet:
**Figure 7.** A top app bar with enter-always scroll behavior that collapses as the user scrolls down and expands as the user scrolls up.

###### Exit until collapsed

When the user scrolls down through the content (pulling the content up), medium and large flexible app bars collapse until only the collapsed app bar remains visible. When the user scrolls back up (pulling the content down), the app bar remains collapsed until the user reaches the top of the content, at which point the app bar expands back to its full height, centered with its subtitle.

**Medium flexible**

The following snippet demonstrates how to implement a medium flexible top app bar with exit-until-collapsed scroll behavior:

```
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExitUntilCollapsedCenterAlignedMediumFlexibleTopAppBar() {
val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
Scaffold(
modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
topBar = {
MediumFlexibleTopAppBar(
title = { Text("Medium TopAppBar", maxLines = 1, overflow = TextOverflow.Ellipsis) },
subtitle = { Text("Subtitle", maxLines = 1, overflow = TextOverflow.Ellipsis) },
titleHorizontalAlignment = Alignment.CenterHorizontally,
navigationIcon = {
IconButton(onClick = { /* onBackClick() */ }) {
Icon(
painter = rememberVectorPainter(AppIcons.ArrowBack),
contentDescription = "Localized description",
)
}
},
actions = {
IconButton(onClick = { /* doSomething() */ }) {
Icon(painter = rememberVectorPainter(AppIcons.Favorite), contentDescription = "Localized description")
}
},
scrollBehavior = scrollBehavior,
)
},
content = { innerPadding ->
// ...
},
)
}
```

[IMAGE] A medium flexible top app bar with a back arrow, a favorite icon, and the centered title 'Medium TopAppBar' and subtitle 'Subtitle' below them, above a list of items.

The following video shows the result of the preceding snippet:
**Figure 8.** A medium-sized, center-aligned top app bar that collapses as the user scrolls down.

**Large flexible**

The following snippet demonstrates how to implement a large flexible top app bar with exit-until-collapsed scroll behavior:

```
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExitUntilCollapsedCenterAlignedLargeFlexibleTopAppBar() {
val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
Scaffold(
modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
topBar = {
LargeFlexibleTopAppBar(
title = { Text("Large TopAppBar", maxLines = 1, overflow = TextOverflow.Ellipsis) },
subtitle = { Text("Subtitle", maxLines = 1, overflow = TextOverflow.Ellipsis) },
titleHorizontalAlignment = Alignment.CenterHorizontally,
navigationIcon = {
IconButton(onClick = { /* onBackClick() */ }) {
Icon(
painter = rememberVectorPainter(AppIcons.ArrowBack),
contentDescription = "Localized description",
)
}
},
actions = {
IconButton(onClick = { /* doSomething() */ }) {
Icon(painter = rememberVectorPainter(AppIcons.Favorite), contentDescription = "Localized description")
}
},
scrollBehavior = scrollBehavior,
)
},
content = { innerPadding ->
// ...
},
)
}
```

[IMAGE] A large flexible top app bar with a back arrow, a favorite icon, and the centered title 'Large TopAppBar' and subtitle 'Subtitle' below them, above a list of items.

The following video shows the result of the preceding snippet:
**Figure 9.** A large, center-aligned top app bar that collapses as the user scrolls down.

## Bottom app bar

To create a bottom app bar, use the `BottomAppBar` composable. Using this composable is similar to the top app bar composables described in the preceding sections of this page. You pass composables for the following key parameters:
- `actions`: A series of icons that appear on the left side of the bar. These are commonly either key actions for the given screen, or navigation items.
- `floatingActionButton`: The floating action button that appears on the right side of the bar.

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

This implementation appears as follows:
[IMAGE] A scaffold with the text 'Example of a scaffold with a bottom app bar.' and a bottom app bar with check, edit, microphone and image icon buttons and a floating action button with an 'add' icon.

**Figure 10.** An example implementation of a bottom app bar.  **Note:** You can also use `BottomAppBar` without passing a value for `actions` and `floatingActionButton`. In that case, you can create a custom bottom app bar by filling `BottomAppBar` with content as you would other containers.

### M3 Expressive update

For Material 3 Expressive, use the docked toolbar (`FlexibleBottomAppBar`) instead of the standard bottom app bar for a shorter height and more flexible horizontal item arrangement.

The following snippet implements a `FlexibleBottomAppBar` with an `AppBarRow` that moves extra action items into an overflow menu when space is limited:

```
@Composable
fun FlexibleBottomAppBarSample() {
val icons =
listOf(
AppIcons.ArrowBack,
AppIcons.ArrowForward,
AppIcons.Add,
AppIcons.Check,
AppIcons.Edit,
AppIcons.Favorite,
)
val items = listOf("Back", "Forward", "Add", "Check", "Edit", "Favorite")

FlexibleBottomAppBar(
contentPadding = PaddingValues(horizontal = 96.dp),
horizontalArrangement = BottomAppBarDefaults.FlexibleFixedHorizontalArrangement,
) {
AppBarRow(
overflowIndicator = { menuState ->
IconButton(
onClick = {
if (menuState.isShowing) {
menuState.dismiss()
} else {
menuState.show()
}
}
) {
Icon(
painter = rememberVectorPainter(AppIcons.MoreVert),
contentDescription = "Overflow",
)
}
}
) {
items.forEachIndexed { index, item ->
clickableItem(
onClick = { /* doSomething() */ },
icon = {
Icon(
painter = rememberVectorPainter(icons[index]),
contentDescription = item,
)
},
label = item,
)
}
}
}
}
```

[IMAGE] A flexible bottom app bar with back, forward, add, check, edit, and favorite icon buttons.

A flexible bottom app bar with action items.

## Additional resources
- Material3 docs - Top app bar
- Material3 docs - Bottom app bar
- Scaffold
