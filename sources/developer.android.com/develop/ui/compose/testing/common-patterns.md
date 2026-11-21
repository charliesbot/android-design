# Common patterns
Source: https://developer.android.com/develop/ui/compose/testing/common-patterns

You can test your Compose app with well-established approaches and patterns.

## Test in isolation

`ComposeTestRule` lets you start an activity displaying any composable: your full application, a single screen, or a small element. It's also a good practice to check that your composables are correctly encapsulated and they work independently, allowing for easier and more focused UI testing.

This doesn't mean you should _only_ create unit UI tests. UI tests scoping larger parts of your UI are also very important.

## Access the activity and resources after setting your own content

Oftentimes you need to set the content under test using `composeTestRule.setContent` and you also need to access activity resources, for example to assert that a displayed text matches a string resource. However, you can't call `setContent` on a rule created with `createAndroidComposeRule()` if the activity already calls it.

A common pattern to achieve this is to create an `AndroidComposeTestRule` using an empty activity such as `ComponentActivity`.

```
class MyComposeTest {

@get:Rule
val composeTestRule = createAndroidComposeRule<ComponentActivity>()

@Test
fun myTest() {
// Start the app.
composeTestRule.setContent {
MyAppTheme {
MainScreen(uiState = exampleUiState, /*...*/)
}
}
val continueLabel = composeTestRule.activity.getString(R.string.next)
composeTestRule.onNodeWithText(continueLabel).performClick()
}
}
```

Note that `ComponentActivity` needs to be added to your app's `AndroidManifest.xml` file. Enable that by adding this dependency to your module:

```
debugImplementation("androidx.compose.ui:ui-test-manifest:$compose_version")

```

## Custom semantics properties

You can create custom semantics properties to expose information to tests. To do this, define a new `SemanticsPropertyKey` and make it available using the `SemanticsPropertyReceiver`.

```
// Creates a semantics property of type Long.
val PickedDateKey = SemanticsPropertyKey<Long>("PickedDate")
var SemanticsPropertyReceiver.pickedDate by PickedDateKey
```

Now use that property in the `semantics` modifier:

```
val datePickerValue by remember { mutableStateOf(0L) }
MyCustomDatePicker(
modifier = Modifier.semantics { pickedDate = datePickerValue }
)
```

From tests, use `SemanticsMatcher.expectValue` to assert the value of the property:

```
composeTestRule
.onNode(SemanticsMatcher.expectValue(PickedDateKey, 1445378400)) // 2015-10-21
.assertExists()
```
**Warning:** You should only use custom Semantics properties when it's hard to match a specific item using the given finders and matchers. Using custom Semantics properties to expose visual properties such as colors, font size or rounded corner radius is not recommended, as it can pollute production code and wrong implementations can lead to bugs that are hard to find.

## Verify state restoration

Verify that the state of your Compose elements is correctly restored when the activity or process is recreated. Perform such checks without relying on activity recreation with the `StateRestorationTester` class.

This class lets you simulate the recreation of a composable. It's especially useful to verify the implementation of `rememberSaveable`.

```
class MyStateRestorationTests {

@get:Rule
val composeTestRule = createComposeRule()

@Test
fun onRecreation_stateIsRestored() {
val restorationTester = StateRestorationTester(composeTestRule)

restorationTester.setContent { MainScreen() }

// TODO: Run actions that modify the state

// Trigger a recreation.
restorationTester.emulateSavedInstanceStateRestore()

// TODO: Verify that state has been correctly restored.
}
}
```

## Test different device configurations

Android apps need to adapt to many changing conditions: window sizes, locales, font sizes, dark and light themes, and more. Most of these conditions are derived from device-level values controlled by the user and exposed with the current `Configuration` instance. Testing different configurations directly in a test is difficult since the test must configure device-level properties.

`DeviceConfigurationOverride` is a test-only API that lets you simulate different device configurations in a localized way for the `@Composable` content under test.

The companion object of `DeviceConfigurationOverride` has the following extension functions, which override device-level configuration properties:
- `DeviceConfigurationOverride.DarkMode()`: Overrides the system to dark theme or light theme.
- `DeviceConfigurationOverride.FontScale()`: Overrides the system font scale.
- `DeviceConfigurationOverride.FontWeightAdjustment()`: Overrides the system font weight adjustment.
- `DeviceConfigurationOverride.ForcedSize()`: Forces a specific amount of space regardless of device size.
- `DeviceConfigurationOverride.LayoutDirection()`: Overrides the layout direction (left-to-right or right-to-left).
- `DeviceConfigurationOverride.Locales()`: Overrides the locale.
- `DeviceConfigurationOverride.RoundScreen()`: Overrides if the screen is round.

To apply a specific override, wrap the content under test in a call to the `DeviceConfigurationOverride()` top-level function, passing the override to apply as a parameter.

For example, the following code applies the `DeviceConfigurationOverride.ForcedSize()` override to change the density locally, forcing the `MyScreen` composable to be rendered in a large landscape window, even if the device the test is running on doesn't support that window size directly:

```
composeTestRule.setContent {
DeviceConfigurationOverride(
DeviceConfigurationOverride.ForcedSize(DpSize(1280.dp, 800.dp))
) {
MyScreen() // Will be rendered in the space for 1280dp by 800dp without clipping.
}
}
```

To apply multiple overrides together, use `DeviceConfigurationOverride.then()`:

```
composeTestRule.setContent {
DeviceConfigurationOverride(
DeviceConfigurationOverride.FontScale(1.5f) then
DeviceConfigurationOverride.FontWeightAdjustment(200)
) {
Text(text = "text with increased scale and weight")
}
}
```

## Custom failure handling in tests

Debugging UI tests often requires inspecting the screen state and composition hierarchy when an assertion fails.

Compose provides a native failure handling pipeline to streamline diagnostics. You can automatically capture screenshots and UI hierarchies upon failure, or register custom handlers to forward crash telemetry to external tools. **Note:** The APIs associated with the failure handler pipeline (`TestFailurePolicy`, `TestFailureHandler`, and `FailureContext`) are included in `androidx.compose.ui:ui-test:1.13.0-alpha01+` and `androidx.compose.ui:ui-test-junit4:1.13.0-alpha01+`.

### Configure failure handling

To configure failure handling, pass a `TestFailurePolicy` object to `ComposeUiTestConfig`.

#### Capture built-in failure artifacts

To capture screenshots and UI hierarchies upon test failure, set the `screenshotCaptureMode` and `uiHierarchyCaptureMode` properties in `TestFailurePolicy`.

```
class MyComposeTest {
private val customConfig = ComposeUiTestConfig(
failurePolicy = TestFailurePolicy(
screenshotCaptureMode = TestFailurePolicy.CaptureMode.Enabled,
uiHierarchyCaptureMode = TestFailurePolicy.CaptureMode.Enabled
)
)

@Test
fun myFirstTest() = runComposeUiTest(config = customConfig) {
// ...
}
}
```

### Use a custom failure handler

To define custom behavior for test failures, add a `TestFailureHandler` to the `failureHandlers` list. Each handler receives a `FailureContext` object that contains the original error and a list of artifacts generated by the preceding pipeline handlers.

```
class MyComposeTestWithHandler {
private val customConfig = ComposeUiTestConfig(
failurePolicy = TestFailurePolicy(
screenshotCaptureMode = TestFailurePolicy.CaptureMode.Enabled,
failureHandlers = listOf(
TestFailureHandler { context ->
val screenshot = context.artifacts.firstOrNull {
it.type == FailureArtifact.Type.Screenshot
}
// ...
}
)
)
)

@get:Rule
val rule = createComposeRule(config = customConfig)

// ...
}
```

### Configure captures for an entire test suite

You can enable failure artifact captures for an entire test suite by passing instrumentation runner arguments in your module's `build.gradle` file. This eliminates the need to update individual test files.

```
// build.gradle.kts
android {
defaultConfig {
// ...
testInstrumentationRunnerArguments["androidx.compose.ui.test.failure.isUiHierarchyCaptureEnabled"] = "true"
testInstrumentationRunnerArguments["androidx.compose.ui.test.failure.isScreenshotCaptureEnabled"] = "true"
}
}

```

#### Configuration precedence

The framework evaluates both the local `TestFailurePolicy` and the suite-level runner arguments:
- If you explicitly set a mode to `CaptureMode.Enabled`/`Disabled` in your test `failurePolicy`, this overrides the runner argument.
- If you set a mode to `CaptureMode.Unspecified` (or leave `failurePolicy` as `null`), the framework falls back to the runner argument.

## Additional Resources
- **Test apps on Android**: The main Android testing landing page provides a broader view of testing fundamentals and techniques.
- **Fundamentals of testing:** Learn more about the core concepts behind testing an Android app.
- **Local tests:** You can run some tests locally, on your own workstation.
- **Instrumented tests:** It is good practice to also run instrumented tests. That is, tests that run directly on-device.
- **Continuous integration:** Continuous integration lets you integrate your tests into your deployment pipeline.
- **Test different screen sizes:** With some many devices available to users, you should test for different screen sizes.
- **Espresso**: While intended for View-based UIs, Espresso knowledge can still be helpful for some aspects of Compose testing.
