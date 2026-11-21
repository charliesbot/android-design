# Support desktop windowing
Source: https://developer.android.com/develop/ui/compose/layouts/adaptive/support-desktop-windowing

Desktop windowing enables users to run multiple apps simultaneously in resizable app windows for a versatile, desktop-like experience.

In figure 1, you can see the organization of the screen with desktop windowing enabled. Things to note:
- Users can run multiple apps side by side simultaneously.
- Taskbar is in a fixed position at the bottom of the display showing the running apps. Users can pin apps for quick access.
- New customizable header bar decorates the top of each window with controls such as minimize and maximize.

**Figure 1.** Desktop windowing on a tablet.

By default, apps open full screen on Android tablets. To launch an app in desktop windowing, press and hold the window handle at the top of the screen and drag the handle within the UI, as seen in figure 2.

When an app is open in desktop windowing, other apps open in desktop windows as well.    Your browser doesn't support the video tag.
**Figure 2.** Press, hold, and drag the app window handle to enter desktop windowing.

Users can also invoke desktop windowing from the menu that shows up below the window handle when you tap or click the handle or use the keyboard shortcut Meta key (Windows, Command, or Search) + Ctrl + Down.

Users exit desktop windowing by closing all active windows or by grabbing the window handle at the top of a desktop window and dragging the app to the top of the screen. The Meta + H keyboard shortcut also exits desktop windowing and runs apps full screen again.

To return to desktop windowing, tap or click the desktop space tile in the Recents screen.

## Optimize your app layout for a desktop-like environment

Designing for a desktop experience can differ significantly from mobile design because of the increased screen space, the precision of mouse and keyboard input, and the expectation of high productivity.

Jetpack WindowManager provides an opinionated API to help developers decide when to show a desktop UI, which typically has higher information density, different navigation patterns, and optimized mouse interactions.

```
lifecycleScope.launch(Dispatchers.Main) {
lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
windowInfoTracker.windowEngagementInfo(this@DesktopWindowingActivity)
.collect { windowEngagementInfo ->
if(windowEngagementInfo.hasEngagementMode(WindowEngagementInfo.EngagementMode.PRECISE_POINTER)){
showDesktopOptimizedUI()
}else {
showTouchOptimizedUI()
}
}
}
}
```

To learn more, see Design for desktop.

## Resizability and compatibility mode

In desktop windowing, apps with locked orientation are freely resizable. That means even if an activity is locked to portrait orientation, users can still resize the app to a landscape orientation window.    Your browser doesn't support the video tag.
**Figure 3.** Resizing the window of a portrait-restricted app to landscape.

Apps declared as nonresizable (that is, `resizeableActivity = false`) have their UI scaled while keeping the same aspect ratio.    Your browser doesn't support the video tag.
**Figure 4.** The UI of a nonresizable app scales as the window resizes.

Camera apps that lock the orientation or are declared as nonresizable have a special treatment for their camera viewfinders: the window is fully resizable, but the viewfinder keeps the same aspect ratio. By assuming apps always run in portrait or landscape, the apps hardcode or otherwise make assumptions that lead to miscalculations of the preview or captured image orientation or aspect ratio resulting in stretched, sideways, or upside-down images.

Until apps are ready to implement fully responsive camera viewfinders, the special treatment provides a more basic user experience that mitigates the effects wrong assumptions may cause.

To learn more about compatibility mode for camera apps, see Device compatibility mode.    Your browser doesn't support the video tag.
**Figure 5.** Camera viewfinder retains its aspect ratio as the window resizes.

## Customizable header insets

All apps running in desktop windowing have a header bar, even in immersive mode. You can customize this bar to prevent your app's content from being obscured and to draw custom UI elements directly into the header space.
[IMAGE] Chrome before and after implementing custom headers.

**Figure 6.** Chrome before and after implementing custom headers.

### Implementation

To draw custom content in the header bar, the first step is to make the header bar background transparent. You can achieve this by using the `APPEARANCE_TRANSPARENT_CAPTION_BAR_BACKGROUND` flag with the `WindowInsetsController`.

```
window.insetsController?.setSystemBarsAppearance(
WindowInsetsController.APPEARANCE_TRANSPARENT_CAPTION_BAR_BACKGROUND,
WindowInsetsController.APPEARANCE_TRANSPARENT_CAPTION_BAR_BACKGROUND
)
```

Once the header bar is transparent, you can style the header area to match your app's design. Use `WindowInsets.isCaptionBarVisible` to detect if the bar is present and apply the appropriate height or padding to your layout.

```
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CaptionBar() {
if (WindowInsets.isCaptionBarVisible) {
Row(
modifier = Modifier
.windowInsetsTopHeight(WindowInsets.captionBar)
.fillMaxWidth()
.background(if (isSystemInDarkTheme()) Color.White else Color.Black),
horizontalArrangement = Arrangement.Center,
verticalAlignment = Alignment.CenterVertically
) {
Text(
text = "Caption Bar Title",
style = MaterialTheme.typography.titleMedium,
modifier = Modifier.padding(4.dp)
)
}
}
}
```

- `setSystemBarsAppearance(appearance,mask)`: Configures the visual style of system bars. The first parameter defines the target appearance flags, while the second acts as a mask to control which specific flags are modified.
- `windowInsetsTopHeight()`: Automatically sets the height of your Composable to match the system's header bar, helping your custom background fill the caption area without hardcoding pixel values.
- `WindowInsets.captionBar`: Provides the dimensions for the desktop windowing controls (**Close**, **Maximize**, etc.), allowing your UI to scale or hide automatically when entering or leaving desktop windowing.

For more information, see About window insets. In addition to a title, you can display other UI elements in the caption bar, such as tabs—like in Google Chrome—search bars, or profile avatars. **Note:** The system continues to draw its own interactive caption elements, such as the close and maximize buttons, on top of your app content.

### User interface

To avoid overlapping your UI with system buttons, Android 15 provides the `WindowInsets#getBoundingRects()` method. The method returns a list of `Rect` objects representing areas occupied by system elements. Any remaining space in the caption bar is a _safe zone_ where you can safely place custom content. **Note:** By default, the system handles gestures in the caption region to facilitate window dragging. But if you want to place interactive UI (like tabs or search bars) in this area with your own custom handling, you must request priority input handling using the `setSystemGestureExclusionRects()` API.

Toggle the appearance of system caption elements for light and dark themes using `APPEARANCE_LIGHT_CAPTION_BARS`. Access insets using `WindowInsets.Companion.captionBar()` in Compose, or `WindowInsets.Type.captionBar()` in Views.

For more information, see About window insets.

## Multitasking and multi-instance support

Multitasking is at the core of desktop windowing, and allowing multiple instances of your app can highly increase users' productivity.

Starting from Android 15, you can use `PROPERTY_SUPPORTS_MULTI_INSTANCE_SYSTEM_UI`. By setting this property in your `AndroidManifest.xml`, you specify that the system UI should provide options (like a "New Window" button) for the app to be launched in multiple instances.

```
<application>
<property
android:name="android.window.PROPERTY_SUPPORTS_MULTI_INSTANCE_SYSTEM_UI"
android:value="true" />
</application>

```

**Note:** In desktop windowing and other multi-window environments, new tasks open in a new window, so double-check the user journey any time your app starts multiple tasks.

### Manage app instances with dragging gestures

In multi-window mode, users can start a new app instance by dragging a UI element (like a tab or a document) out of the app's window. Users can also move elements between different instances of the same app.    Your browser doesn't support the video tag.
**Figure 7.** Start a new instance of Chrome by dragging a tab out of the desktop window.

#### Transfer data with drag and drop

To configure a composable as a drag source for multi-instance drag-and-drop allowing users to drag content to another instance of your app, or create a _new_ instance by dropping content onto an empty area of the screen—use the `dragAndDropSource` modifier. In its lambda, return `DragAndDropTransferData`, passing the `ClipData` that contains the data to transfer, and flags to configure multi-instance behavior.

_Android 15 introduces two key flags for desktop-style windowing and multi-instance interactions:_
- **`DRAG_FLAG_GLOBAL_SAME_APPLICATION`**: Indicates that a drag operation can cross window boundaries (for multiple instances of the same application). When `startDragAndDrop()` is called with this flag set, only visible windows belonging to the same application are able to participate in the drag operation and receive the dragged content.

```
Modifier.dragAndDropSource { _ ->
DragAndDropTransferData(
clipData = ClipData.newPlainText("label", "Your data"),
flags = View.DRAG_FLAG_GLOBAL_SAME_APPLICATION
)
}
```

- **`DRAG_FLAG_START_INTENT_SENDER_ON_UNHANDLED_DRAG`**: Allows users to start a new instance of your app by dropping the dragged content onto an empty area of the screen, if no other window handles the drop.
  - When using this flag, you must provide an `IntentSender` using `ClipData.Item.Builder#setIntentSender()`, which the system uses to launch the new activity if an unhandled drop occurs.

```
Modifier.dragAndDropSource { _ ->
val intent = Intent.makeMainActivity(activity.componentName).apply {
putExtra("EXTRA_ITEM_ID", itemId)
flags = Intent.FLAG_ACTIVITY_NEW_TASK or
Intent.FLAG_ACTIVITY_MULTIPLE_TASK or
Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
}

val pendingIntent = PendingIntent.getActivity(
activity, 0, intent, PendingIntent.FLAG_IMMUTABLE
)

val data = ClipData(
"Item $itemId",
arrayOf(ClipDescription.MIMETYPE_TEXT_INTENT),
ClipData.Item.Builder().setIntentSender(pendingIntent.intentSender).build()
)

DragAndDropTransferData(
clipData = data,
flags = View.DRAG_FLAG_GLOBAL_SAME_APPLICATION or
View.DRAG_FLAG_START_INTENT_SENDER_ON_UNHANDLED_DRAG,
)
}
```
**Note:** To make sure drag operations are recognized by external system processes and other instances of your app, use `DRAG_FLAG_GLOBAL_SAME_APPLICATION` and `DRAG_FLAG_START_INTENT_SENDER_ON_UNHANDLED_DRAG` within your `dragAndDropSource`. Because this configuration utilizes a `PendingIntent` to resolve the action, you don't need to implement manual data retrieval logic; the system automatically triggers the intent when the drop occurs outside of an active window.

#### Receive transferred data

To accept data from another instance, use the `dragAndDropTarget` modifier. You must explicitly request permissions if the data is coming from a different instance or app.

```
Modifier.dragAndDropTarget(
shouldStartDragAndDrop = { event ->
event.toAndroidDragEvent().clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
},
target = object : DragAndDropTarget {
override fun onDrop(event: DragAndDropEvent): Boolean {
requestDragAndDropPermissions(activity, event.toAndroidDragEvent())
val clipData = event.toAndroidDragEvent().clipData
val item = clipData?.getItemAt(0)?.text
if (item != null) {
// Process the dropped text item here
}
return item != null
}
}
)
```

**Key steps:**
- Filter: Use `shouldStartDragAndDrop` to check if the incoming data (MIME type) is supported.
- Permissions: Call `requestDragAndDropPermissions(event)` to access the data.
- Handle: Extract the data in the `onDrop` callback.

## Additional optimizations

Customize app launches and transition apps from desktop windowing to full screen.

### Specify default size and position

Not all apps, even if resizable, need a large window to offer user value. You can use the `ActivityOptions#setLaunchBounds()` method to specify a default size and position when an activity is launched.

### Enter full-screen from the desktop space

Apps can go full-screen by calling `Activity#requestFullScreenMode()`. The method displays the app full screen directly from desktop windowing.

Previous  arrow_back   Support connected displays

Next  Support camera on multiple form factors   arrow_forward
