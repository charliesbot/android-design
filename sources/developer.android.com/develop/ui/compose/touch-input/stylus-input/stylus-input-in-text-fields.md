# Stylus input in text fields
Source: https://developer.android.com/develop/ui/compose/touch-input/stylus-input/stylus-input-in-text-fields

The Jetpack `androidx.compose.material3` library enables users to write into any `TextField` component in any app using a stylus.    Your browser doesn't support the video tag.
**Figure 1.** Handwritten input with a stylus.

To enable stylus input by default, add the library dependency to your app's `build.gradle` file:

###### Kotlin

```
dependencies {
implementation("androidx.compose.foundation:foundation:LATEST_COMPOSE_VERSION")
}

android {
buildFeatures {
compose = true
}

composeOptions {
kotlinCompilerExtensionVersion = "LATEST_EXTENSION_VERSION"
}

kotlinOptions {
jvmTarget = "LATEST_JVM_VERSION"
}
}
```

###### Groovy

```
dependencies {
implementation 'androidx.compose.foundation:foundation:LATEST_COMPOSE_VERSION'
}

android {
buildFeatures {
compose true
}

composeOptions {
kotlinCompilerExtensionVersion = 'LATEST_EXTENSION_VERSION'
}

kotlinOptions {
jvmTarget = 'LATEST_JVM_VERSION'
}
}
```

## `TextField`

Stylus handwriting is enabled for all `TextField` components by default on Android 14 and higher and the `androidx.compose.foundation:foundation:1.7.0` dependency. Handwriting mode is started for a `TextField` when a stylus motion event is detected within the handwriting bounds of the component.

The handwriting bounds include 40 dp of vertical padding and 10 dp of horizontal padding around the input field.
[IMAGE] Input field with surrounding rectangle indicating the bounds for detection of stylus motion events.

**Figure 2.** Handwriting bounds of `TextField` components.

Stylus handwriting is not supported for `TextField` fields when the input method editor is requested with `KeyboardType.Password`.

## Input delegation

Apps can display placeholder UI elements that appear to be text input fields but are actually just static UI elements with no text input capability. Search fields are a common example. Tapping the static UI element triggers a transition to a new UI that contains a functional text input field focused for input.    Your browser doesn't support the video tag.
**Figure 3.** Input delegation from static UI element to text input field.

### Stylus input delegation

Use the handwriting delegation APIs to support stylus handwriting input for placeholder input fields (see `handwritingDetector` and `handwritingHandler`). The placeholder UI element is configured to delegate handwriting to a functional input field. For an example implementation, see `HandwritingDetectorSample.kt`.

Stylus handwriting mode starts when the functional input field gains focus and creates an `InputConnection`.    Your browser doesn't support the video tag.
**Figure 4.** Stylus input delegation from static UI element to text input field.

## Testing

Stylus handwriting is supported on Android 14 and higher devices with a compatible stylus input device and an input method editor (IME) that supports the Android 14 stylus handwriting APIs.

If you don't have a stylus input device, simulate stylus input on any device with root access (including emulators) using the following Android Debug Bridge (adb) commands:

```

// Android 14
adb shell setprop persist.debug.input.simulate_stylus_with_touch true && adb shell stop && adb shell start

// Android 15 and higher
// Property takes effect after screen reconfiguration such as orientation change.
adb shell setprop debug.input.simulate_stylus_with_touch true

```

Use the Gboard beta for testing if you are using a device that doesn't support stylus.

## Additional resources
- Material Design — Text fields
- Handle user input

Previous  arrow_back   Overview

Next  Advanced stylus features   arrow_forward
