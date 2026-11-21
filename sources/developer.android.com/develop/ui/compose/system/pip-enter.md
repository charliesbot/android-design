# Enter PiP at correct times
Source: https://developer.android.com/develop/ui/compose/system/pip-enter

Your app shouldn't enter PiP mode in the following situations:
- If the video is stopped or paused.
- If you are on a different page of the app than the video player.

To control when your app enters PiP mode, add a variable that tracks the state of the video player using a `mutableStateOf`.

## Toggle state based on if video is playing

To toggle the state based on if the video player is playing, add a listener on the video player. Toggle the state of your state variable based on if the player is playing or not:

```
player.addListener(object : Player.Listener {
override fun onIsPlayingChanged(isPlaying: Boolean) {
shouldEnterPipMode = isPlaying
}
})
```

## Toggle state based on if player is released

When the player is released, set your state variable to `false`:

```
fun releasePlayer() {
shouldEnterPipMode = false
}
```

## Use state to define if PiP mode is entered (pre-Android 12)
- Since adding PiP pre-12 uses a `DisposableEffect`, you need to create a new variable by `rememberUpdatedState` with `newValue` set as your state variable. This will ensure that the updated version is used within the `DisposableEffect`.
- In the lambda that defines the behavior when the `OnUserLeaveHintListener` is triggered, add an `if` statement with the state variable around the call to `enterPictureInPictureMode()`:

```
val currentShouldEnterPipMode by rememberUpdatedState(newValue = shouldEnterPipMode)
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
Build.VERSION.SDK_INT < Build.VERSION_CODES.S
) {
val context = LocalContext.current
DisposableEffect(context) {
val onUserLeaveBehavior = Runnable {
if (currentShouldEnterPipMode) {
context.findActivity()
.enterPictureInPictureMode(PictureInPictureParams.Builder().build())
}
}
context.findActivity().addOnUserLeaveHintListener(
onUserLeaveBehavior
)
onDispose {
context.findActivity().removeOnUserLeaveHintListener(
onUserLeaveBehavior
)
}
}
} else {
Log.i("PiP info", "API does not support PiP")
}
```

## Use state to define if PiP mode is entered (post-Android 12)

Pass your state variable into `setAutoEnterEnabled` so that your app only enters PiP mode at the right time:

```
val pipModifier = modifier.onGloballyPositioned { layoutCoordinates ->
val builder = PictureInPictureParams.Builder()

// Add autoEnterEnabled for versions S and up
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
builder.setAutoEnterEnabled(shouldEnterPipMode)
}
context.findActivity().setPictureInPictureParams(builder.build())
}

VideoPlayer(pipModifier)
```

## Use `setSourceRectHint` for a smooth animation

The `setSourceRectHint` API creates a smoother animation for entering PiP mode. In Android 12+, it also creates a smoother animation for exiting PiP mode. Add this API to the PiP builder to indicate the area of the activity that is visible following the transition into PiP.
- Only add `setSourceRectHint()` to the `builder` if the state defines that the app should enter PiP mode. This avoids calculating `sourceRect` when the app does not need to enter PiP.
- To set the `sourceRect` value, use the `layoutCoordinates` that are given from the `onGloballyPositioned` function on the modifier.
- Call `setSourceRectHint()` on the `builder` and pass in the `sourceRect` variable.

```
val context = LocalContext.current

val pipModifier = modifier.onGloballyPositioned { layoutCoordinates ->
val builder = PictureInPictureParams.Builder()
if (shouldEnterPipMode) {
val sourceRect = layoutCoordinates.boundsInWindow().toAndroidRectF().toRect()
builder.setSourceRectHint(sourceRect)
}

if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
builder.setAutoEnterEnabled(shouldEnterPipMode)
}
context.findActivity().setPictureInPictureParams(builder.build())
}

VideoPlayer(pipModifier)
```
**Note:** Depending on the video player you are using, you may need to reference the documentation and choose the `sourceRectHint` that represents the actual video content instead of the full video player.

Previous  arrow_back   Set up your app for PiP

Next  Add PiP through a button   arrow_forward
