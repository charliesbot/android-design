# Input compatibility on large screens
Source: https://developer.android.com/develop/ui/compose/touch-input/input-compatibility-on-large-screens

On large screen devices, users often interact with apps using a keyboard, mouse, trackpad, stylus, or gamepad. To enable your app to accept input from external devices, do the following:
- **Test basic keyboard support**, such as Ctrl+Z for undo, Ctrl+C for copy, and Ctrl+S for save. See Handle keyboard actions for a list of default keyboard shortcuts.
- **Test advanced keyboard support**, for example, Tab key and arrow key keyboard navigation, Enter key text entry confirmation, and Spacebar play and pause in media apps.
- **Test basic mouse interactions**, including right-click for context menu, icon changes on hover, and mouse wheel or trackpad scroll events on custom components.
- **Test app-specific input devices** such as a stylus, game controllers, and music app MIDI controllers.
- **Consider advanced input support** that could make the app stand out in desktop environments, for example, touchpad as a cross‑fader for DJ apps, mouse capture for games, and keyboard shortcuts for keyboard‑centric users.

## Keyboard

The way your app responds to keyboard input contributes to the large screen user experience. There are three kinds of keyboard input: navigation, keystrokes, and shortcuts.

### Navigation

Keyboard navigation is rarely implemented in touch‑centric apps, but users expect it when they're using an app and have their hands on a keyboard. Keyboard navigation can be essential on phones, tablets, foldables, and desktop devices for users with accessibility needs.

For many apps, arrow key and Tab key navigation are handled automatically by the Android framework. For example, some composables are focusable by default, such as a `Button` or a composable with the `clickable` modifier; keyboard navigation should generally work without any additional code. To enable keyboard navigation for custom composables that are not focusable by default, add the `focusable` modifier:

```
var color by remember { mutableStateOf(Green) }
Box(
Modifier
.background(color)
.onFocusChanged { color = if (it.isFocused) Blue else Green }
.focusable()
) {
Text("Focusable 1")
}
```

For more information, see Making a composable focusable.

When focus is enabled, the Android framework creates a navigational mapping for all focusable components based on their position. This usually works as expected and no further development is needed.

However, Compose doesn't always determine the correct next item for tabbed navigation for complex composables like tabs and lists, for example, when one of the composables is a horizontal scrollable that is not fully visible.

To control focus behavior, add the `focusGroup` modifier to the parent composable of a collection of composables. Focus moves to the group, then through the group before moving to the next focusable component, for example:

```
Row {
Column(Modifier.focusGroup()) {
Button({}) { Text("Row1 Col1") }
Button({}) { Text("Row2 Col1") }
Button({}) { Text("Row3 Col1") }
}
Column(Modifier.focusGroup()) {
Button({}) { Text("Row1 Col2") }
Button({}) { Text("Row2 Col2") }
Button({}) { Text("Row3 Col2") }
}
}

```

For more information, see Provide coherent navigation with focus groups.

Test access to every UI element of your app using the keyboard only. Frequently used elements should be accessible without mouse or touch input.

Remember, keyboard support might be essential for users with accessibility needs.

### Keystrokes

For text input that would be handled by an on screen virtual keyboard (IME), such as for a `TextField` , apps should behave as expected on large screen devices with no additional development work. For keystrokes that cannot be anticipated by the framework, apps need to handle the behavior themselves. This is especially true for apps with custom views.

Some examples are chat apps that use the Enter key to send a message, media apps that start and stop playback with the Spacebar, and games that control movement with the w, a, s, and d keys.

You can handle individual keystrokes with the `onKeyEvent` modifier, which accepts a lambda that's called when the modified component receives a key event. The `KeyEvent#type` property enables you to determine whether the event is a key press (`KeyDown`) or key release (`KeyUp`):

```
Box(
modifier = Modifier.focusable().onKeyEvent {
if(
it.type == KeyEventType.KeyUp &&
it.key == Key.S
) {
doSomething()
true
} else {
false
}
}
)  {
Text("Press S key")
}

```

Alternatively, you can override the `onKeyUp()` callback and add the expected behavior for each received keycode: `kotlin override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean { return when (keyCode) { KeyEvent.KEYCODE_ENTER -> { sendChatMessage() true } KeyEvent.KEYCODE_SPACE -> { playOrPauseMedia() true } else -> super.onKeyUp(keyCode, event) } }`

An `onKeyUp` event occurs when a key is released. Using the callback prevents apps from needing to process multiple `onKeyDown` events if a key is held down or released slowly. Games and apps that need to detect the moment a key is pressed or whether the user is holding a key down can listen for the `onKeyDown` event and handle repeated `onKeyDown` events themselves. **Note:** Depending on your app's needs, you can override `onKeyUp()` for the entire activity, or you can handle individual keystrokes with the `onKeyEvent` modifier. With `onKeyEvent`, an app can, for example, listen for the Enter key in a specific `TextField` to implement _send_ functionality only when the user is typing in a chat box.

For more information, see Handle keyboard actions.

### Shortcuts

Common keyboard shortcuts that include the Ctrl, Alt, Shift, and Meta keys are expected when using a hardware keyboard. If an app doesn't implement shortcuts, the experience can feel frustrating to users. Advanced users also appreciate shortcuts for frequently used app‑specific tasks. Shortcuts make an app easier to use and differentiate it from apps that don't have shortcuts. **Note:** On macOS keyboards, the Meta key is the Command key; on Windows keyboards, the Windows key; on ChromeOS keyboards, the Search key.

Some common shortcuts include Ctrl+S (save), Ctrl+Z (undo), and Ctrl+Shift+Z (redo). For a list of default shortcuts, see Handle keyboard actions.

A `KeyEvent` object has the following attributes which indicate whether modifier keys are pressed:
- `isAltPressed`
- `isCtrlPressed`
- `isMetaPressed`
- `isShiftPressed`

For example:

```
Box(
Modifier.onKeyEvent {
if (it.isAltPressed && it.key == Key.A) {
println("Alt + A is pressed")
true
} else {
false
}
}
.focusable()
)

```

For more information, see the following:
- Modifier keys
- Keyboard Shortcuts Helper

## Stylus

Many large screen devices come with a stylus. Android apps handle styluses as touchscreen input. Some devices might also have a USB or Bluetooth drawing table, like the Wacom Intuos. Android apps can receive bluetooth input but not USB input.

To access stylus `MotionEvent` objects, add the `pointerInteropFilter` modifier to a drawing surface. Implement a `ViewModel` class with a method that processes motion events; pass the method as the `onTouchEvent` lambda of the `pointerInteropFilter` modifier:

```
@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun DrawArea(modifier: Modifier = Modifier) {
Canvas(modifier = modifier
.clipToBounds()
.pointerInteropFilter {
viewModel.processMotionEvent(it)
}

) {
// Drawing code here.
}
}

```

The `MotionEvent` object contains information about the event:
- `MotionEvent#getToolType()` returns `TOOL_TYPE_FINGER`, `TOOL_TYPE_STYLUS`, or `TOOL_TYPE_ERASER` depending on the tool that made contact with the display
- `MotionEvent#getPressure()` reports the physical pressure applied to the stylus pen (if supported)
- `MotionEvent#getAxisValue()` with `MotionEvent.AXIS_TILT` and `MotionEvent.AXIS_ORIENTATION` provide the physical tilt and orientation of the stylus (if supported)

### Historical points

Android batches input events and delivers them once per frame. A stylus can report events at much higher frequencies than the display. When creating drawing apps, check for events that may be in the recent past by using the `getHistorical` APIs:
- `MotionEvent#getHistoricalX()`
- `MotionEvent#getHistoricalY()`
- `MotionEvent#getHistoricalPressure()`
- `MotionEvent#getHistoricalAxisValue()`

### Palm rejection

When users draw, write, or interact with your app using a stylus, they sometimes touch the screen with the palm of their hands. The touch event (set to `ACTION_DOWN` or `ACTION_POINTER_DOWN`) can be reported to your app before the system recognizes and disregards the inadvertent palm touch.

Android cancels palm touch events by dispatching a `MotionEvent`. If your app receives `ACTION_CANCEL`, cancel the gesture. If your app receives `ACTION_POINTER_UP`, check whether `FLAG_CANCELED` is set. If so, cancel the gesture.

Do not check for just `FLAG_CANCELED`. On Android 13 (API level 33) and higher, the system sets `FLAG_CANCELED` for `ACTION_CANCEL` events, but the system does not set the flag on lower Android versions.

#### Android 12

On Android 12 (API level 32) and lower, detection of palm rejection is possible only for single‑pointer touch events. If a palm touch is the only pointer, the system cancels the event by setting `ACTION_CANCEL` on the motion event object. If other pointers are down, the system sets `ACTION_POINTER_UP`, which is insufficient for detecting palm rejection.

#### Android 13

On Android 13 (API level 33) and higher, if a palm touch is the only pointer, the system cancels the event by setting `ACTION_CANCEL` and `FLAG_CANCELED` on the motion event object. If other pointers are down, the system sets `ACTION_POINTER_UP` and `FLAG_CANCELED`.

Whenever your app receives a motion event with `ACTION_POINTER_UP`, check for `FLAG_CANCELED` to determine whether the event indicates palm rejection (or other event cancellation). **Note:** One way to prevent extraneous palm and finger touch events in stylus‑enabled apps is to provide a UI control that activates a drawing, writing, or interactive mode that disables touch events and supports stylus events only.

### Note-taking apps

ChromeOS has a special intent that surfaces registered note‑taking apps to users. To register an app as a note‑taking app, add the following to your app manifest:

```
<intent-filter>
<action android:name="org.chromium.arc.intent.action.CREATE_NOTE" />
<category android:name="android.intent.category.DEFAULT" />
</intent-filter>

```

When an app is registered with the system, the user can select it as the default note‑taking app. When a new note is requested, the app should create an empty note ready for stylus input. When the user wishes to annotate an image (such as a screenshot or downloaded image), the app launches with `ClipData` containing one or more items with `content://` URIs. The app should create a note that uses the first attached image as a background image and enter a mode where the user can draw on the screen with a stylus.

#### Test note-taking intents without a stylus

To test whether an app responds correctly to note‑taking intents without an active stylus, use the following method to display the note‑taking options on ChromeOS:
- Switch to dev mode and make the device writable
- Press Ctrl+Alt+F2 to open a terminal
- Run the command `sudo vi /etc/chrome_dev.conf`
- Press `i` to edit and add `--ash-enable-palette` to a new line at the end of the file
- Save by pressing Esc and then typing :, w, q and pressing Enter
- Press Ctrl+Alt+F1 to return to the regular ChromeOS UI
- Log out, then log back in

A stylus menu should now be on the shelf:
- Tap the stylus button on the shelf and choose **New note**. This should open a blank drawing note.
- Take a screenshot. From the shelf, select **stylus button > Capture screen** or download an image. There should be the option to **Annotate image** in the notification. This should launch the app with the image ready to be annotated.

## Mouse and touchpad support

Most apps generally need to handle only three large screen–centric events: right-click, hover, and drag and drop.

### Right-click

Any actions that cause an app to show a context menu, like touch & hold on a list item, should also react to right‑click events.

To handle right‑click events, apps should register a `View.OnContextClickListener`:

```
Box(modifier = Modifier.fillMaxSize()) {
AndroidView(
modifier = Modifier.fillMaxSize(),
factory = { context ->
val rootView = FrameLayout(context)
val onContextClickListener =
View.OnContextClickListener { view ->
showContextMenu()
true
}
rootView.setOnContextClickListener(onContextClickListener)
rootView
},
)
}

```

For details on constructing context menus, see Create a contextual menu.

### Hover

You can make your app layouts feel polished and easier to use by handling hover events. This is especially true for custom components:

The two most common examples of this are:
- Indicating to users whether an element has interactive behavior, such as being clickable or editable, by changing the mouse pointer icon
- Adding visual feedback to items in a large list or grid when the pointer is hovering over them

### Drag and drop

In a multi-window environment, users expect to be able to drag and drop items between apps. This is true for desktop devices as well as tablets, phones, and foldables in split‑screen mode.

Consider whether users are likely to drag items into your app. For example, photo editors should expect to receive photos, audio players should expect to receive audio files, and drawing programs should expect to receive photos.

To add drag and drop support, see Drag and drop , and take a look at the Android on ChromeOS — Implementing Drag & Drop blog post.

**Special considerations for ChromeOS**
- Remember to request permission with `requestDragAndDropPermissions()` to access items dragged in from outside the app
- An item must have the `View.DRAG_FLAG_GLOBAL` flag in order to be dragged out to other applications
See Start a drag event

### Advanced pointer support

Apps that do advanced handling of mouse and touchpad input should implement a `pointerInput` modifier to obtain a `PointerEvent`:

```
@Composable
private fun LogPointerEvents(filter: PointerEventType? = null) {
var log by remember { mutableStateOf("") }
Column {
Text(log)
Box(
Modifier
.size(100.dp)
.background(Color.Red)
.pointerInput(filter) {
awaitPointerEventScope {
while (true) {
val event = awaitPointerEvent()
// handle pointer event
if (filter == null || event.type == filter) {
log = "${event.type}, ${event.changes.first().position}"
}
}
}
}
)
}
}
```

Examine the `PointerEvent` object to determine the following:
- `PointerType`: Mouse, stylus, touch, and so forth from `PointerEvent#changes`
- `PointerEventType`: Pointer actions, such as press, move, scroll, and release

## Game controllers

Some large screen Android devices support up to four game controllers. Use the standard Android game controller APIs to handle game controllers (see Support game controllers).

Game controller buttons are mapped to common values following a common mapping. But not all game controller manufacturers follow the same mapping conventions. You can provide a much better experience if you allow users to select different popular controller mappings. See Process gamepad button presses for more information.

## Input translation mode

ChromeOS enables an input translation mode by default. For most Android apps, this mode helps apps work as expected in a desktop environment. Some examples include automatically enabling two‑finger scrolling on the touchpad, mouse wheel scrolling, and mapping raw display coordinates to window coordinates. Generally, app developers don't need to implement any of these behaviors themselves.

If an app implements custom input behavior, for example defining a custom two‑finger touchpad pinch action, or these input translations don't provide the input events expected by the app, you can disable the input translation mode by adding the following tag to the Android manifest:

```
<uses-feature
android:name="android.hardware.type.pc"
android:required="false" />

```

## Additional resources
- Input compatibility on ChromeOS
