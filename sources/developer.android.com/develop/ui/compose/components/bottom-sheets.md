# Bottom sheets
Source: https://developer.android.com/develop/ui/compose/components/bottom-sheets

[IMAGE] An example of a Material Design 3 modal bottom sheet.

If you want to implement a bottom sheet, you can use the `ModalBottomSheet` composable.

You can use the `content` slot, which uses a `ColumnScope` to layout sheet content composables in a column:

```
ModalBottomSheet(onDismissRequest = { /* Executed when the sheet is dismissed */ }) {
// Sheet content
}
```

## Control sheet state programmatically

To programmatically expand and collapse the sheet, use `SheetState`. You can use `rememberModalBottomSheetState` to create an instance of `SheetState` that must be passed to `ModalBottomSheet` with the `sheetState` parameter. `SheetState` provides access to the `show` and `hide` functions, as well as properties related to the current sheet state. These suspending functions require a `CoroutineScope` — for example, using `rememberCoroutineScope` — and you can call them in response to UI events. Make sure to remove the `ModalBottomSheet` from composition upon hiding the bottom sheet.

```
val sheetState = rememberModalBottomSheetState()
val scope = rememberCoroutineScope()
var showBottomSheet by remember { mutableStateOf(false) }
Scaffold(
floatingActionButton = {
ExtendedFloatingActionButton(
text = { Text("Show bottom sheet") },
icon = { Icon(Icons.Filled.Add, contentDescription = "") },
onClick = {
showBottomSheet = true
}
)
}
) { contentPadding ->
// Screen content

if (showBottomSheet) {
ModalBottomSheet(
onDismissRequest = {
showBottomSheet = false
},
sheetState = sheetState
) {
// Sheet content
Button(onClick = {
scope.launch { sheetState.hide() }.invokeOnCompletion {
if (!sheetState.isVisible) {
showBottomSheet = false
}
}
}) {
Text("Hide bottom sheet")
}
}
}
}
```

[IMAGE] A modal bottom sheet in Jetpack Compose showing content.
