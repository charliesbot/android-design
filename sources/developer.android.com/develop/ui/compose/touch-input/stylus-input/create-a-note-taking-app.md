# Create a note-taking app
Source: https://developer.android.com/develop/ui/compose/touch-input/stylus-input/create-a-note-taking-app

Note-taking is a core capability of Android that enhances user productivity on large screen devices. Note‑taking apps enable users to write and sketch in a floating window or on the full screen, capture and annotate screen content, and save notes for later review and revision.

Users can access note-taking apps from the lock screen or while running other apps.

Stylus support for note-taking provides an exceptional user experience.

## Notes role

The `RoleManager.ROLE_NOTES` role identifies note‑taking apps and grants them the `LAUNCH_CAPTURE_CONTENT_ACTIVITY_FOR_NOTE` permission.

To acquire the notes role for your app, do the following:
- Call `isRoleAvailable()` to check the status of the role.
- If the notes role is available, call `createRequestRoleIntent()` to obtain a notes‑specific intent.
- Call `startActivityForResult()` with the notes intent to prompt the user to grant the notes role to your app.

Only one app can possess the notes role.

The app opens in response to an implicit `ACTION_CREATE_NOTE` intent action. If invoked from the device lock screen, the app opens full screen; if invoked while the screen is unlocked, in a floating window. **Note:** In system settings, users can opt out of having a default note‑taking app, in which case no app responds to the `ACTION_CREATE_NOTE` intent action.

## App manifest

To qualify for the notes role, your app must include the following declaration in the app manifest:

```
<activity
android:name="YourActivityName"
android:exported="true"
android:showWhenLocked="true"
android:turnScreenOn="true">
<intent-filter>
<action android:name="android.intent.action.CREATE_NOTE" />
<category android:name="android.intent.category.DEFAULT" />
</intent-filter>
</activity>

```

The declaration enables users to assign the notes role to your app, making it the default note‑taking application:
- `ACTION_CREATE_NOTE` sets the intent action to which your app responds
- `showWhenLocked` makes your app accessible from the device lock screen
- `turnScreenOn` enables your app to turn the device screen on when the app runs

## App features

A large screen differentiated note‑taking app provides a full complement of note‑taking capabilities.

### Stylus support

When your app is invoked with the `EXTRA_USE_STYLUS_MODE` intent extra set to `true`, the app should open a note that accepts stylus (or finger-touch) input.

If the intent extra is set to `false`, your app should open a note that accepts keyboard input.

### Lockscreen access

Your app must provide a full‑screen activity that runs when the app is opened from the device lock screen.

Your app should show only historical notes if the user has consented (in the unlocked device state) to showing past notes. Otherwise, when opened from the lock screen, your app should always create a new note.

You can check whether your app has been launched from the lock screen with `KeyguardManager#isKeyguardLocked()`. To ask the user to authenticate and unlock the device, call `KeyguardManager#requestDismissKeyguard()`:

```
val keyguardManager =
getSystemService(KEYGUARD_SERVICE) as KeyguardManager
keyguardManager.requestDismissKeyguard(  this, object :
KeyguardDismissCallback() {  override fun onDismissError() {  // Unlock failed.
Dismissing keyguard is not feasible.  }  override fun onDismissSucceeded() {  //
Unlock succeeded. Device is now unlocked.  }  override fun onDismissCancelled()
{  // Unlock failed. User cancelled operation or request otherwise cancelled.  }
} )

```
**Warning:** When launched from the device lock screen, your app must ensure user privacy.

### Floating windows

For contextual note-taking, your app must provide an activity that opens in a floating window when another application is running.

Your app should support `multi-instance` mode so that users can create multiple notes in multiple floating windows even when your note‑taking app is launched full screen or in split‑screen mode.

### Content capture

Content capture is a key capability of note‑taking apps. With content capture, users can take screenshots of the display behind the note‑taking app's floating window. Users can capture all or part of the display, paste the content into their note, and annotate or highlight the captured content.

Your note-taking app should provide a UI affordance that launches an `ActivityResultLauncher` created by `registerForActivityResult()`. The `ACTION_LAUNCH_CAPTURE_CONTENT_ACTIVITY_FOR_NOTE` intent action is provided to the launcher either directly or through an `ActivityResultContract`.

A system activity captures the content, saves it on the device, and returns the content URI to your app in the callback argument of `registerForActivityResult()`.

The following example uses a generic `StartActivityForResult` contract:

```
private val startForResult =
registerForActivityResult(  ActivityResultContracts.StartActivityForResult()) {
result: ActivityResult ->  if (result.resultCode ==
Intent.CAPTURE_CONTENT_FOR_NOTE_SUCCESS) {  val uri = result.data?.data  // Use
the URI to paste the captured content into the note.  }  } override fun
onCreate(savedInstanceState: Bundle?) {  super.onCreate(savedInstanceState)
setContent {  NotesTheme {  Surface(color =
MaterialTheme.colorScheme.background) {  CaptureButton(  onClick = {
Log.i("ContentCapture", "Launching intent...")
startForResult.launch(Intent(ACTION_LAUNCH_CAPTURE_CONTENT_ACTIVITY_FOR_NOTE))
})  }  }  } } @Composable fun CaptureButton(onClick: () -> Unit) {
Button(onClick = onClick)
{Text("Capture Content")} }

```

Your app should handle all result codes:
- `CAPTURE_CONTENT_FOR_NOTE_SUCCESS`
- `CAPTURE_CONTENT_FOR_NOTE_FAILED`
- `CAPTURE_CONTENT_FOR_NOTE_USER_CANCELED`
- `CAPTURE_CONTENT_FOR_NOTE_WINDOW_MODE_UNSUPPORTED`
- `CAPTURE_CONTENT_FOR_NOTE_BLOCKED_BY_ADMIN`

When content capture succeeds, paste the captured image into the note, for example:

```
registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
result: ActivityResult ->  if (result.resultCode ==
Intent.CAPTURE_CONTENT_FOR_NOTE_SUCCESS) {  val uri = result.data?data  // Use
the URI to paste the captured content into the note.  } }

```

The content capture feature should be exposed through a UI affordance only when your note‑taking app is running in a floating window—not when running full screen, launched from the device lock screen. (Users can take screenshots of the note‑taking app itself with device screenshot capabilities.)

To determine whether your app is in a floating window (or bubble), call the following methods:
- `isLaunchedFromBubble()` to check that your note‑taking app was not launched full screen from the device lock screen
- `isRoleHeld(RoleManager.ROLE_NOTES)` to verify that your app is the default note‑taking app (your app can run in a conversation or other type of bubble if the app does not hold the notes role)
**Note:** Your app should check whether content capture is permitted by device administrative policies. Call `getScreenCaptureDisabled()` with `null` as the argument.

## Additional resources
- Get a result from an activity

Previous  arrow_back   Advanced stylus features

Next  About the Ink API   arrow_forward
