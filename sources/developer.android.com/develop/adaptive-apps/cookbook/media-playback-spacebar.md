# Media playback pause and resume with keyboard Spacebar
Source: https://developer.android.com/develop/adaptive-apps/cookbook/media-playback-spacebar

[IMAGE] Four star rating icon

Large screen optimization includes the ability to handle external keyboard inputs, like reacting to the Spacebar being pressed to pause or resume playback of videos and other media. This is particularly useful for tablets, which often connect to external keyboards, and Chromebooks, which usually come with external keyboards but can be used in tablet mode.

When media is the only element of the window (like full-screen video playback), respond to keypress events at the screen level in Jetpack Compose.

## Best practices

Whenever your app plays a media file, users should be able to pause and resume playback by pressing the Spacebar on a physical keyboard.

## Ingredients
- `KEYCODE_SPACE`: Key code constant for the Spacebar.
- `onPreviewKeyEvent`: `Modifier` that enables a component to intercept hardware key events when it (or one of its children) is focused.
- `onKeyEvent`: Similar to `onPreviewKeyEvent`, this `Modifier` enables a component to intercept hardware key events when it (or one of its children) is focused.

## Steps

To respond to keyboard key presses in Jetpack Compose, your app must listen for keypress events, filter the events, and respond to selected keypresses, such as a Spacebar keypress.

### 1. Listen for keyboard events

With Jetpack Compose, you can leverage either the `onPreviewKeyEvent` or the `onKeyEvent` modifier within the component that manages the keystroke.

The main difference between the two modifiers is where the event is dispatched if the modifier does not consume it:
- `onPreviewKeyEvent` — Dispatches the event to its first child.
- `onKeyEvent` — Dispatches the event to the composable's parent.

### 2. Filter and handle Spacebar presses

Inside the `onPreviewKeyEvent` or `onKeyEvent` modifier lambda, filter for key events where the key release (`KeyEventType.KeyUp`) matches the Spacebar (`Key.Spacebar`).

Return `true` if your code handles the event and you don't want the event to propagate further. Return `false` to allow other components to handle the event.
- **Option A: Using `onPreviewKeyEvent`**

```
Column(
modifier = Modifier.onPreviewKeyEvent { event ->
if (event.type == KeyEventType.KeyUp && event.key == Key.Spacebar) {
// Handle spacebar keypress to pause/resume playback.
true
} else {
false
}
}
) {
// Content
}
```

- **Option B: Using `onKeyEvent`**

```
Column(
modifier = Modifier.onKeyEvent { event ->
if (event.type == KeyEventType.KeyUp && event.key == Key.Spacebar) {
// Handle spacebar keypress to pause/resume playback.
true
} else {
false
}
}
) {
// Content
}
```

## Results

Your app can now respond to Spacebar key presses to pause and resume a video or other media.

## Additional resources

To learn more about keyboard events and how to manage them, see Handle keyboard input.
