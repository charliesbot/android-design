# Component Snippets

Read when implementing production Jetpack Compose; explorations use `compose-signatures.md` in `android-design-explore`. Snippets use `Icons.Rounded.*` as shorthand; production apps load Material Symbols drawables ([components.md, Icons](components.md#icons)).

## Flexible app bar

```kotlin
val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
        LargeFlexibleTopAppBar(
            title = { Text("Habits") },
            subtitle = { Text("3 left today") },
            navigationIcon = { /* back icon button */ },
            actions = {
                FilledTonalIconButton(onClick = onAdd, shapes = IconButtonDefaults.shapes()) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add habit")
                }
            },
            scrollBehavior = scrollBehavior,
        )
    },
) { padding -> /* content */ }
```

## XL control-led hero button

```kotlin
val height = ButtonDefaults.ExtraLargeContainerHeight
Button(
    onClick = onStart,
    shapes = ButtonDefaults.shapesFor(height),
    modifier = Modifier.fillMaxWidth().heightIn(height),
    contentPadding = ButtonDefaults.contentPaddingFor(height),
) {
    Text("Start session", style = ButtonDefaults.textStyleFor(height))
}
```

## Floating toolbar with a FAB

```kotlin
Box(Modifier.fillMaxSize()) {
    content()
    HorizontalFloatingToolbar(
        expanded = expanded,
        floatingActionButton = {
            FloatingToolbarDefaults.VibrantFloatingActionButton(onClick = onCompose) {
                Icon(Icons.Rounded.Edit, contentDescription = "Compose")
            }
        },
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
        modifier = Modifier.align(Alignment.BottomEnd).offset(x = -FloatingToolbarDefaults.ScreenOffset, y = -FloatingToolbarDefaults.ScreenOffset),
    ) {
        IconButton(onClick = onArchive) { Icon(Icons.Rounded.Archive, contentDescription = "Archive") }
        IconButton(onClick = onLabel) { Icon(Icons.Rounded.Label, contentDescription = "Label") }
    }
}
```

Collapse on scroll by attaching `Modifier.floatingToolbarVerticalNestedScroll(expanded, onExpand = { expanded = true }, onCollapse = { expanded = false })` to the scrolling container.

## FAB menu

```kotlin
var open by rememberSaveable { mutableStateOf(false) }
FloatingActionButtonMenu(
    expanded = open,
    button = {
        ToggleFloatingActionButton(checked = open, onCheckedChange = { open = it }) {
            val icon = if (checkedProgress > 0.5f) Icons.Rounded.Close else Icons.Rounded.Add
            Icon(icon, contentDescription = null, modifier = Modifier.animateIcon({ checkedProgress }))
        }
    },
) {
    FloatingActionButtonMenuItem(onClick = onPhoto, text = { Text("Photo") }, icon = { Icon(Icons.Rounded.PhotoCamera, null) })
    FloatingActionButtonMenuItem(onClick = onNote, text = { Text("Note") }, icon = { Icon(Icons.Rounded.Notes, null) })
}
```

## Connected button group

```kotlin
Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
    options.forEachIndexed { index, label ->
        ToggleButton(
            checked = selected == index,
            onCheckedChange = { selected = index },
            shapes = when (index) {
                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
            },
            modifier = Modifier.weight(1f).semantics { role = Role.RadioButton },
        ) { Text(label) }
    }
}
```

## Standard button group

```kotlin
ButtonGroup(overflowIndicator = { ButtonGroupDefaults.OverflowIndicator(it) }) {
    clickableItem(onClick = onPrevious, label = "Previous", icon = { Icon(Icons.Rounded.SkipPrevious, null) })
    toggleableItem(checked = playing, label = "Play", onCheckedChange = onPlayToggle, icon = { Icon(Icons.Rounded.PlayArrow, null) }, weight = 2f)
    clickableItem(onClick = onNext, label = "Next", icon = { Icon(Icons.Rounded.SkipNext, null) })
}
```

## Split button

```kotlin
SplitButtonLayout(
    leadingButton = { SplitButtonDefaults.LeadingButton(onClick = onSend) { Text("Send") } },
    trailingButton = {
        SplitButtonDefaults.TrailingButton(checked = menuOpen, onCheckedChange = { menuOpen = it }) {
            Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "More send options")
        }
    },
)
```

## Segmented list

```kotlin
Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
    settings.forEachIndexed { index, setting ->
        SegmentedListItem(
            onClick = { onOpen(setting) },
            shapes = ListItemDefaults.segmentedShapes(index, settings.size),
            leadingContent = { Icon(setting.icon, null) },
            supportingContent = { Text(setting.summary) },
        ) { Text(setting.title) }
    }
}
```

Separate groups with a larger gap (16 to 24dp) between columns of segmented items.

## Grouped vertical menu

```kotlin
DropdownMenuPopup(expanded = open, onDismissRequest = { open = false }) {
    val groups = listOf(listOf("Rename", "Duplicate"), listOf("Delete"))
    groups.forEachIndexed { groupIndex, items ->
        DropdownMenuGroup(shapes = MenuDefaults.groupShape(groupIndex, groups.size)) {
            items.forEachIndexed { i, item ->
                DropdownMenuItem(
                    onClick = { onAction(item) },
                    text = { Text(item) },
                    shape = MenuDefaults.itemShape(i, items.size).shape,
                )
            }
        }
        if (groupIndex < groups.lastIndex) Spacer(Modifier.height(MenuDefaults.GroupSpacing))
    }
}
```

## Loading and progress

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Loading() = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { ContainedLoadingIndicator() }

LinearWavyProgressIndicator(progress = { playback.fraction }, modifier = Modifier.fillMaxWidth())
```

## Navigation by window size

```kotlin
NavigationSuiteScaffold(
    navigationItems = {
        destinations.forEach { dest ->
            NavigationSuiteItem(
                selected = dest == current,
                onClick = { onNavigate(dest) },
                icon = { Icon(dest.icon, contentDescription = null) },
                label = { Text(dest.label) },
            )
        }
    },
) { /* current destination */ }
```

Use the `navigationItems` + `NavigationSuiteItem` overload (`material3-adaptive-navigation-suite` `1.5.0` stable) instead of the older `navigationSuiteItems` scope overload. For explicit control, use `ShortNavigationBar` on compact and `WideNavigationRail` on medium and up.
