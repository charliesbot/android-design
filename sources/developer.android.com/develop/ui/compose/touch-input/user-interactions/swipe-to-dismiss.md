# Swipe to dismiss or update
Source: https://developer.android.com/develop/ui/compose/touch-input/user-interactions/swipe-to-dismiss

The `SwipeToDismissBox` component allows a user to dismiss or update an item by swiping it to the left or right.

## API surface

Use the `SwipeToDismissBox` composable to implement actions that are triggered by swipe gestures. Key parameters include:
- `state`: The `SwipeToDismissBoxState` state created to store the value produced by calculations on the swipe item, which triggers events when produced.
- `backgroundContent`: A customizable composable displayed behind the item content that is revealed when the content is swiped.

## Basic example: Update or dismiss on swipe

The snippets in this example show a swipe implementation that either updates the item when swiped from start to end, or dismisses the item when swiped from end to start.

```
data class TodoItem(
val itemDescription: String,
var isItemDone: Boolean = false
)
```

```
@Composable
fun TodoListItem(
todoItem: TodoItem,
onToggleDone: (TodoItem) -> Unit,
onRemove: (TodoItem) -> Unit,
modifier: Modifier = Modifier,
) {
val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
confirmValueChange = {
if (it == StartToEnd) onToggleDone(todoItem)
else if (it == EndToStart) onRemove(todoItem)
// Reset item when toggling done status
it != StartToEnd
}
)

SwipeToDismissBox(
state = swipeToDismissBoxState,
modifier = modifier.fillMaxSize(),
backgroundContent = {
when (swipeToDismissBoxState.dismissDirection) {
StartToEnd -> {
Icon(
painter = rememberVectorPainter(if (todoItem.isItemDone) AppIcons.CheckBox else AppIcons.CheckBoxOutlineBlank),
contentDescription = if (todoItem.isItemDone) "Done" else "Not done",
modifier = Modifier
.fillMaxSize()
.background(Color.Blue)
.wrapContentSize(Alignment.CenterStart)
.padding(12.dp),
tint = Color.White
)
}
EndToStart -> {
Icon(
painter = rememberVectorPainter(AppIcons.Delete),
contentDescription = "Remove item",
modifier = Modifier
.fillMaxSize()
.background(Color.Red)
.wrapContentSize(Alignment.CenterEnd)
.padding(12.dp),
tint = Color.White
)
}
Settled -> {}
}
}
) {
ListItem(
headlineContent = { Text(todoItem.itemDescription) },
supportingContent = { Text("swipe me to update or remove.") }
)
}
}
```

### Key points about the code
- `swipeToDismissBoxState` manages the component state. It triggers the `confirmValueChange` callback once the interaction with the item is done. The callback body handles the different possible actions. The callback returns a boolean that tells the component whether it should display a dismiss animation. In this case:
  - If the item is swiped from start to end, it calls the `onToggleDone` lambda, passing the current `todoItem`. This corresponds with updating the to-do item.
  - If the item is swiped from end to start, it calls the `onRemove` lambda, passing the current `todoItem`. This corresponds with deleting the to-do item.
  - `it != StartToEnd`: This line returns `true` if the swipe direction is not `StartToEnd`, and `false` otherwise. Returning `false` prevents the `SwipeToDismissBox` from immediately disappearing after a "toggle done" swipe, allowing for a visual confirmation or animation.

- `SwipeToDismissBox` enables horizontal swiping interactions on each item. In rest, it shows the inner content of the component, but when a user starts swiping, the content is moved away and the `backgroundContent` appears. Both the normal content and the `backgroundContent` get the full constraints of the parent container to render themselves in. The `content` is drawn on top of the `backgroundContent`. In this case:
  - `backgroundContent` is implemented as a `Icon` with a background color based on `SwipeToDismissBoxValue`:
  - `Blue` when swiping `StartToEnd` — toggling a to-do item.
  - `Red` when swiping `EndToStart` — deleting a to-do item.
  - Nothing is displayed in the background for `Settled` — when the item is not being swiped, nothing is displayed in the background.
  - Similarly, the `Icon` that is displayed adapts to the swipe direction:
  - `StartToEnd` shows a `CheckBox` icon when the to-do item is done and a `CheckBoxOutlineBlank` icon when it is not done.
  - `EndToStart` displays a `Delete` icon.

```
@Composable
fun SwipeItemExample() {
val todoItems = remember {
mutableStateListOf(
TodoItem("Pay bills"), TodoItem("Buy groceries"),
TodoItem("Go to gym"), TodoItem("Get dinner")
)
}

LazyColumn {
items(
items = todoItems,
key = { it.itemDescription }
) { todoItem ->
TodoListItem(
todoItem = todoItem,
onToggleDone = { todoItem ->
todoItem.isItemDone = !todoItem.isItemDone
},
onRemove = { todoItem ->
todoItems -= todoItem
},
modifier = Modifier.animateItem()
)
}
}
}
```

[IMAGE] A list of to-do items with the first item partly swiped toward the start, revealing a red background with a delete icon, above 'Buy groceries' and 'Go to gym'.

### Key points about the code
- `mutableStateListOf(...)` creates an observable list that can hold `TodoItem` objects. When an item is added or removed from this list, Compose recomposes the parts of the UI that depend on it.
  - Inside `mutableStateListOf()`, four `TodoItem` objects are initialized with their respective descriptions: "Pay bills", "Buy groceries", "Go to gym", and "Get dinner".

- `LazyColumn` displays a vertically scrolling list of `todoItems`.
- `onToggleDone = { todoItem -> ... }` is a callback function invoked from within `TodoListItem` when the user marks an object as done. It updates the `isItemDone` property of the `todoItem`. Because `todoItems` is a `mutableStateListOf`, this change triggers a recomposition, updating the UI.
- `onRemove = { todoItem -> ... }` is a callback function triggered when the user removes the item. It removes the specific `todoItem` from the `todoItems` list. This also causes a recomposition, and the item will be removed from the displayed list.
- An `animateItem` modifier is applied to each `TodoListItem` so that the modifier's `placementSpec` is called when the item has been dismissed. This animates the removal of the item, as well as the reordering of other items in the list.

### Result

The following video demonstrates the basic swipe-to-dismiss functionality from the preceding snippets:
**Figure 1**. A basic implementation of swipe-to-dismiss that can both mark an item as complete and show a dismiss animation for an item in a list.

See the GitHub source file for the full sample code.

## Advanced example: Animate background color on swipe

The following snippets show how to incorporate a positional threshold to animate an item's background color on swipe.

```
data class TodoItem(
val itemDescription: String,
var isItemDone: Boolean = false
)
```

```
@Composable
fun TodoListItemWithAnimation(
todoItem: TodoItem,
onToggleDone: (TodoItem) -> Unit,
onRemove: (TodoItem) -> Unit,
modifier: Modifier = Modifier,
) {
val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
confirmValueChange = {
if (it == StartToEnd) onToggleDone(todoItem)
else if (it == EndToStart) onRemove(todoItem)
// Reset item when toggling done status
it != StartToEnd
}
)

SwipeToDismissBox(
state = swipeToDismissBoxState,
modifier = modifier.fillMaxSize(),
backgroundContent = {
when (swipeToDismissBoxState.dismissDirection) {
StartToEnd -> {
Icon(
painter = rememberVectorPainter(if (todoItem.isItemDone) AppIcons.CheckBox else AppIcons.CheckBoxOutlineBlank),
contentDescription = if (todoItem.isItemDone) "Done" else "Not done",
modifier = Modifier
.fillMaxSize()
.drawBehind {
drawRect(lerp(Color.LightGray, Color.Blue, swipeToDismissBoxState.progress))
}
.wrapContentSize(Alignment.CenterStart)
.padding(12.dp),
tint = Color.White
)
}
EndToStart -> {
Icon(
painter = rememberVectorPainter(AppIcons.Delete),
contentDescription = "Remove item",
modifier = Modifier
.fillMaxSize()
.background(lerp(Color.LightGray, Color.Red, swipeToDismissBoxState.progress))
.wrapContentSize(Alignment.CenterEnd)
.padding(12.dp),
tint = Color.White
)
}
Settled -> {}
}
}
) {
OutlinedCard(shape = RectangleShape) {
ListItem(
headlineContent = { Text(todoItem.itemDescription) },
supportingContent = { Text("swipe me to update or remove.") }
)
}
}
}
```

### Key points about the code
- `drawBehind` draws directly into the canvas behind the content of the `Icon` composable.
  - `drawRect()` draws a rectangle on the canvas and fills the entire bounds of the drawing scope with the specified `Color`.

- When swiping, the background color of the item smoothly transitions using `lerp`.
  - For a swipe from `StartToEnd`, the background color gradually changes from light gray to blue.
  - For a swipe from `EndToStart`, the background color gradually changes from light gray to red.
  - The amount of transition from one color to the next is determined by `swipeToDismissBoxState.progress`.

- `OutlinedCard` adds a subtle visual separation between the list items.

```
@Composable
fun SwipeItemWithAnimationExample() {
val todoItems = remember {
mutableStateListOf(
TodoItem("Pay bills"), TodoItem("Buy groceries"),
TodoItem("Go to gym"), TodoItem("Get dinner")
)
}

LazyColumn {
items(
items = todoItems,
key = { it.itemDescription }
) { todoItem ->
TodoListItemWithAnimation(
todoItem = todoItem,
onToggleDone = { todoItem ->
todoItem.isItemDone = !todoItem.isItemDone
},
onRemove = { todoItem ->
todoItems -= todoItem
},
modifier = Modifier.animateItem()
)
}
}
}
```

[IMAGE] A list of to-do items with the first item partly swiped toward the start, revealing a red background with a delete icon, above 'Buy groceries' and 'Go to gym'.

### Key points about the code
- For key points about this code, see Key points from a previous section, which describes an identical code snippet.

### Result

The following video shows the advanced functionality with animated background color:
**Figure 2**. An implementation of swiping to reveal or delete, with animated background colors and a longer threshold before the action registers.

See the GitHub source file for the full sample code.

## Additional resources
- `SwipeToDismissBoxState`
- `SwipeToDismissBox`
- Swipe - Material 3
