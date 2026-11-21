# Configure the test environment with v2 testing APIs
Source: https://developer.android.com/develop/ui/compose/testing/migrate-v2

v2 versions of the Compose testing APIs (`createComposeRule`, `createAndroidComposeRule`, `runComposeUiTest`, `runAndroidComposeUiTest`, etc) are now available to improve control over coroutine execution. This update does not duplicate the entire API surface; only the APIs that establish the test environment have been updated.

The v1 APIs are deprecated, and it's strongly recommended to migrate to the new APIs. Migrating verifies your tests align with standard coroutine behavior and avoids future compatibility issues. For a list of the deprecated v1 APIs, see API mappings.

While the v1 APIs relied on the `UnconfinedTestDispatcher`, the v2 APIs use the `StandardTestDispatcher` by default for the running composition. This change aligns Compose test behavior with the standard `runTest` APIs and provides explicit control over coroutine execution order.

## Configure the test environment **Note:** The `ComposeUiTestConfig` class is included, starting in version 1.13, in the `junit4` and `ui-test` parts of the `androidx.compose.ui` AndroidX artifact (androidx.compose.ui:ui-test-junit4:1.13.0-alpha01+ and androidx.compose.ui:ui-test:1.13.0-alpha01+).

Compose test v2 APIs use `ComposeUiTestConfig` to customize the test environment. APIs that create setup functions for tests, such as `createComposeRule`, `runComposeUiTest`, and other related APIs, accept `ComposeUiTestConfig`. This configuration object consolidates environmental related APIs like `effectContext`, `runTestContext`, and `testTimeout` into a single object.

The configuration model also manages `inputMode`. Compose test v2 APIs enforce `InputMode.Touch` by default at the start of each test to ensure determinism and prevent input mode state from leaking between tests.

`ComposeUiTestConfig` is part of the Compose testing v2 APIs, which use `StandardTestDispatcher` by default. If your tests use v1 APIs, see Migrate to v2 testing APIs before adopting `ComposeUiTestConfig`.

## Migrate to `ComposeUiTestConfig`

Within the overloads for creating setup functions in tests, several overloads that accept individual configuration parameters -- such as `effectContext`, `runTestContext`, or `testTimeout` -- are deprecated. Update your tests to use `ComposeUiTestConfig` instead, as shown in the following example:

```
val testConfig = ComposeUiTestConfig(
effectContext = EmptyCoroutineContext,
runTestContext = EmptyCoroutineContext,
testTimeout = 30.seconds
)

@get:Rule
val rule = createComposeRule(config = testConfig)

// OR

runComposeUiTest(config = testConfig) {}
```

### Default input mode

Tests can fail during migration if they rely on non-touch input modes configured through instrumentation APIs before the test starts. Within the setup functions for tests, the system enforces `InputMode.Touch` by default at the start of each test for more determinism and to prevent state leakage, overriding ambient device state and pre-test setup.

To resolve this, specify the required input mode in `ComposeUiTestConfig`:

```
class FocusTest {
@get:Rule
val rule = createComposeRule(
config = ComposeUiTestConfig(inputMode = InputMode.Keyboard)
)

@Test
fun testFocus() {}
}
```

To configure the input mode for individual test cases instead of the entire test class, pass `ComposeUiTestConfig` to `runComposeUiTest`:

```
class FocusTest {
@Test
fun testTouchMode() = runComposeUiTest {
// Runs with the default InputMode.Touch
}

@Test
fun testKeyboardMode() = runComposeUiTest(
ComposeUiTestConfig(inputMode = InputMode.Keyboard)
) {
// Runs with InputMode.Keyboard
}
}
```

For other migration issues and resolutions, see Common failures and how to fix them.

## Migrate to v2 testing APIs **Note:** The v2 testing API changes are included in `androidx.compose.ui:ui-test-junit4:1.11.0-alpha03+` and `androidx.compose.ui:ui-test:1.11.0-alpha03+`.

When upgrading to v2 APIs, you can generally use **Find + Replace** to update the package imports and adopt the new dispatcher changes.

Alternatively, ask Gemini to perform a migration to v2 of the Compose testing APIs with the following prompt:

This prompt will use this guide to migrate to v2 testing APIs.

```
Migrate to Compose testing v2 APIs using the official
migration guide.
```

###### Using AI prompts

AI prompts are intended to be used within Gemini in Android Studio.

Learn more about Gemini in Studio here: https://developer.android.com/studio/gemini/overview

Use the following table to map deprecated v1 APIs to their v2 replacements:
|

**Deprecated (v1)**  |

**Replacement (v2)**
|

`androidx.compose.ui.test.junit4.createComposeRule`  |

`androidx.compose.ui.test.junit4.v2.createComposeRule`
|

`androidx.compose.ui.test.junit4.createAndroidComposeRule`  |

`androidx.compose.ui.test.junit4.v2.createAndroidComposeRule`
|

`androidx.compose.ui.test.junit4.createEmptyComposeRule`  |

`androidx.compose.ui.test.junit4.v2.createEmptyComposeRule`
|

`androidx.compose.ui.test.junit4.AndroidComposeTestRule`  |

`androidx.compose.ui.test.junit4.v2.AndroidComposeTestRule`
|

`androidx.compose.ui.test.runComposeUiTest`  |

`androidx.compose.ui.test.v2.runComposeUiTest`
|

`androidx.compose.ui.test.runAndroidComposeUiTest`  |

`androidx.compose.ui.test.v2.runAndroidComposeUiTest`
|

`androidx.compose.ui.test.runEmptyComposeUiTest`  |

`androidx.compose.ui.test.v2.runEmptyComposeUiTest`
|

`androidx.compose.ui.test.AndroidComposeUiTestEnvironment`  |

`androidx.compose.ui.test.v2.AndroidComposeUiTestEnvironment`

### Backward compatibility and exceptions

The existing v1 APIs are now deprecated, but continue to use `UnconfinedTestDispatcher` to maintain existing behavior and prevent breaking changes.

The following is the only exception where the default behavior has changed:

The default test dispatcher used for running composition in the `AndroidComposeUiTestEnvironment` class has switched from `UnconfinedTestDispatcher` to `StandardTestDispatcher`. This affects cases where you create an instance using the constructor, or subclass `AndroidComposeUiTestEnvironment`, and call that constructor.

## Key change: Impact on coroutine execution

The primary difference between v1 and v2 of the APIs is how coroutines are dispatched:
- **v1 APIs** (`UnconfinedTestDispatcher`): When a coroutine was launched, it executed immediately on the current thread, often finishing before the next line of test code ran. Unlike production behavior, this immediate execution could **inadvertently mask real timing issues or race conditions** that would occur in a live application.
- **v2 APIs** (`StandardTestDispatcher`): When a coroutine is launched, it is queued and does not execute until the test explicitly advances the virtual clock. Standard Compose test APIs (such as `waitForIdle()`) already handle this synchronization, so most tests relying on these standard APIs should continue to work with no changes.

## Common failures and how to fix them

If your tests fail after upgrading to v2, they likely exhibit the following pattern:
- **Failure**: You launch a task (for example, a ViewModel loads data), but your assertion fails immediately because the data is still in a "Loading" state.
- **Cause**: With the v2 APIs, coroutines are queued rather than executed immediately. The task was queued but never actually ran before the result was checked.
- **Fix**: Explicitly advance time. You must explicitly tell the v2 dispatcher when to execute work.

### Previous approach

In v1, the task launched and finished immediately. In v2, the following code fails because `loadData()` hasn't actually run yet.

```
// In v1, this launched and finished immediately.
viewModel.loadData()

// In v2, this fails because loadData() hasn't actually run yet!
assertEquals(Success, viewModel.state.value)

```

### Recommended approach

Use `waitForIdle` or `runOnIdle` to execute queued tasks before asserting.

**Option 1**: Using `waitForIdle` advances the clock until the UI is idle, verifying the coroutine has run.

```
viewModel.loadData()

// Explicitly run all queued tasks.
composeTestRule.waitForIdle()

assertEquals(Success, viewModel.state.value)
```

**Option 2**: Using `runOnIdle` executes the code block on the UI thread after the UI has become idle.

```
viewModel.loadData()

// Run the assertion after the UI is idle.
composeTestRule.runOnIdle {
assertEquals(Success, viewModel.state.value)
}
```

### Manual synchronization

In scenarios involving manual synchronization, such as when auto-advancing is disabled, launching a coroutine does not result in immediate execution because the test clock is paused. To execute coroutines in the queue without advancing the virtual clock, use the `runCurrent()` API. This runs tasks scheduled for the current virtual time.

```
composeTestRule.mainClock.scheduler.runCurrent()
```

In contrast to `waitForIdle()`, which advances the test clock until the UI stabilizes, `runCurrent()` executes pending tasks while maintaining the current virtual time. This behavior enables the verification of intermediate states that would otherwise be skipped if the clock were advanced to an idle state.

The underlying test scheduler used in the test environment is exposed. This scheduler can be used in conjunction with the Kotlin `runTest` API to synchronize the test clock.

### Migrate to `runComposeUiTest`

If you're using Compose test APIs alongside the Kotlin `runTest` API, it's strongly recommended to switch to `runComposeUiTest`.

#### Previous approach

Using `createComposeRule` in conjunction with `runTest` creates two separate clocks: one for Compose, and one for the test coroutine scope. This configuration can force you to manually synchronize the test scheduler.

```
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun testWithCoroutines() {
composeTestRule.setContent {
var status by remember { mutableStateOf("Loading...") }
LaunchedEffect(Unit) {
delay(1000)
status = "Done!"
}
Text(text = status)
}

// NOT RECOMMENDED
// Fails: runTest creates a new, separate scheduler.
// Advancing time here does NOT advance the compose clock.
// To fix this without migrating, you would need to share the scheduler
// by passing 'composeTestRule.mainClock.scheduler' to runTest.
runTest {
composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
advanceTimeBy(1000)
composeTestRule.onNodeWithText("Done!").assertIsDisplayed()
}
}
```

#### Recommended approach

The `runComposeUiTest` API automatically executes your test block within its own `runTest` scope. The test clock is synchronized with the Compose environment, so you no longer need to manage the scheduler manually.

```
@Test
fun testWithCoroutines() = runComposeUiTest {
setContent {
var status by remember { mutableStateOf("Loading...") }
LaunchedEffect(Unit) {
delay(1000)
status = "Done!"
}
Text(text = status)
}

onNodeWithText("Loading...").assertIsDisplayed()
mainClock.advanceTimeBy(1000 + 16 /* Frame buffer */)
onNodeWithText("Done!").assertIsDisplayed()
}
}
```
