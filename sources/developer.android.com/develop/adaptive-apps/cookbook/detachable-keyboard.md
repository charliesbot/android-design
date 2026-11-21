# Detachable keyboard management
Source: https://developer.android.com/develop/adaptive-apps/cookbook/detachable-keyboard

[IMAGE] Three star rating icon

Support for detachable keyboards helps maximize user productivity on large screen devices. Android triggers a configuration change every time a keyboard is attached to or detached from a device, which can cause a loss of UI state. Your app can either save and restore its state, letting the system handle activity recreation, or restrict activity recreation for keyboard configuration changes.

In all cases all data related to the keyboard is stored in a `Configuration` object. The `keyboard` and `keyboardHidden` fields of the configuration object contain information about the type of keyboard and its availability.

## Best practices

Apps optimized for large screens support every type of input device, from software and hardware keyboards to stylus, mouse, trackpad, and other peripheral devices.

Support for external keyboards involves configuration changes, which you can manage in either of two ways:
- Let the system recreate the running activity, and you take care of managing the state of your app.
- Manage the configuration change yourself (the activity won't be recreated):
  - Declare all keyboard-related configuration values
  - Create a configuration change handler

Productivity apps, which often require fine control of the UI for text entry and other input, can benefit from the do-it-yourself approach to handling configuration changes.

In special cases, you might want to change your app layout when a hardware keyboard is attached or detached, for example, to make more space for tools or editing windows.

In Jetpack Compose, you can react to keyboard configuration changes in two ways:
- **Reactive UI:** Read the current configuration state directly in your Composables using `LocalConfiguration.current`. The UI automatically recomposes to adapt to the new state when a keyboard is attached or detached.
- **Side-Effect Callback:** Use a lifecycle-aware `DisposableEffect` to register a configuration change listener on the host Activity, which lets you trigger non-UI events (like logging or analytics) without recreating the activity or instantiating any dummy view elements.

## Ingredients
- `android:configChanges`: Attribute of the app manifest's `<activity>` element. Informs the system about configuration changes the app manages.
- `LocalConfiguration`: Composable local provider that exposes the current device `Configuration`.
- `DisposableEffect`: Compose side-effect API that runs a block of code when a Composable enters the composition, and cleans up when it leaves.

## Steps

Declare the `configChanges` attribute and add keyboard-related values to prevent activity recreation. Then, use either `LocalConfiguration.current` to dynamically adapt your Composable layout, or register a configuration listener using `DisposableEffect`.

### 1. Declare `configChanges` attribute

Update the `<activity>` element in the app manifest by adding the `keyboard|keyboardHidden` values to the list of already managed configuration changes:

```
<activity
…
android:configChanges="...|keyboard|keyboardHidden">

```

### 2. Respond to keyboard configuration changes

To handle configuration changes in Jetpack Compose, use one of the following options depending on whether you are updating the UI or executing a side-effect callback:
- **Option A: Reactive UI (Recommended)**
Read the keyboard type directly from `LocalConfiguration.current`. When a hardware keyboard is attached or detached, this value changes, and Compose automatically recomposes your UI.

```
val configuration = LocalConfiguration.current
val isPhysicalKeyboardAttached = configuration.keyboard == Configuration.KEYBOARD_QWERTY

if (isPhysicalKeyboardAttached) {
// Render layout optimized for physical keyboard
} else {
// Render default layout
}
```

- **Option B: Side-Effect Listener**
If you need to run a side-effect (such as logging, updating a local database, or triggering analytics) when the configuration changes, use a `DisposableEffect` to register a configuration change listener on the host `ComponentActivity`.

```
val context = LocalActivity.current
DisposableEffect(context) {
val activity = context as? ComponentActivity
val listener = Consumer<Configuration> { newConfig ->
val hasKeyboard = newConfig.keyboard == Configuration.KEYBOARD_QWERTY
// Trigger non-UI actions, analytics, etc.
}
activity?.addOnConfigurationChangedListener(listener)
onDispose {
activity?.removeOnConfigurationChangedListener(listener)
}
}
```

## Results

Your app will now respond to an external keyboard being attached or detached without recreating the running activity.

## Additional resources

To learn how to save your app's UI state during configuration changes like keyboard attachment or detachment, see Save UI states.
