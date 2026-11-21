# Drag and drop
Source: https://developer.android.com/develop/ui/compose/touch-input/user-interactions/drag-and-drop

Jetpack Compose supports drag and drop with two modifiers:
- `dragAndDropSource`: Specifies a composable as the starting point of the drag gesture
- `dragAndDropTarget`: Specifies a composable that accepts the dropped data

For example, to enable users to drag an image in your app, create an image composable and add the `dragAndDropSource` modifier. To set up a drop target, create another image composable and add the `dragAndDropTarget` modifier.

The modifiers can be applied to multiple drag sources and multiple drop targets.

The modifiers enable apps to share data between two or more composables using `ClipData`, which is interoperable with `View` implementations. **Note:** Drag and drop is different from pick up and move, a common list pattern.

## Specify a drag source

To enable drag events inside a component, add the `dragAndDropSource` modifier. This takes a function as a parameter. Inside this function, use `DragAndDropTransferData` to represent the transferable data. The data can be a remote URI, rich text data on the clipboard, a local file, or more, but they all need to be wrapped in a `ClipData` object. Provide plain text, for example, as follows:

```
Modifier.dragAndDropSource { _ ->
DragAndDropTransferData(
ClipData.newPlainText(
"image Url", url
)
)
}
```

To allow the drag action to cross the borders of the app, the `DragAndDropTransferData` constructor accepts a `flags` argument. In the following example, the `DRAG_FLAG_GLOBAL` constant specifies that data can be dragged from one app into another:

```
Modifier.dragAndDropSource { _ ->
DragAndDropTransferData(
ClipData.newPlainText(
"image Url", url
),
flags = View.DRAG_FLAG_GLOBAL
)
}
```

`DragAndDropTransferData` accepts flags supported by the Android View system. See the list of View constants for an exhaustive list of available flags.

## Receive drop data

Assign the `dragAndDropTarget` modifier to a composable to enable the composable to receive drag-and-drop events. The modifier has two parameters: the first acts as a filter and specifies the kind of data the modifier can accept, and the second delivers the data in a callback.

Note that the callback instance should be _remembered_. The following snippet shows how to remember the callback:

```
val callback = remember {
object : DragAndDropTarget {
override fun onDrop(event: DragAndDropEvent): Boolean {
// Parse received data
return true
}
}
}
```

To accept data from other apps, use the `requestDragAndDropPermission` to enable the reception, and release it once done:

```
val externalAppCallback = remember {
object : DragAndDropTarget {
override fun onDrop(event: DragAndDropEvent): Boolean {
val permission =
activity.requestDragAndDropPermissions(event.toAndroidDragEvent())
// Parse received data
permission?.release()
return true
}
}
}
```

It's important to remember that the `DragAndDropEvent` returned from the Compose callback is different from the one expected by the `requestDragAndDropPermission` method, so you need to call the `toAndroidDragEvent()` extension function on the parameter before passing it to the permission request.

The next snippet demonstrates how to handle dropped plain text:

```
Modifier.dragAndDropTarget(
shouldStartDragAndDrop = { event ->
event.mimeTypes().contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
}, target = callback // or externalAppCallback
)
```

The callback function should return `true` if the event is consumed, or `false` if the event is refused and does not propagate to the parent component.

## Handle drag-and-drop events

Override callbacks in the `DragAndDropTarget` interface to observe when a drag-and-drop event starts, ends, or enters or exits a component for precise control of the UI and the app's behavior:

```
object : DragAndDropTarget {
override fun onStarted(event: DragAndDropEvent) {
// When the drag event starts
}

override fun onEntered(event: DragAndDropEvent) {
// When the dragged object enters the target surface
}

override fun onEnded(event: DragAndDropEvent) {
// When the drag event stops
}

override fun onExited(event: DragAndDropEvent) {
// When the dragged object exits the target surface
}

override fun onDrop(event: DragAndDropEvent): Boolean = true
}
```
