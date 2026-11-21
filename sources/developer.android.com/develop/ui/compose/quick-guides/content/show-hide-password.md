# Show or hide password based on a user toggle
Source: https://developer.android.com/develop/ui/compose/quick-guides/content/show-hide-password

You can create an icon to hide or show a password based on a user toggle to improve security and enhance the user experience.

## Results

**Figure 1.** Toggling the show-and-hide password icon.

## Version compatibility

This implementation requires that your project minSDK be set to API level 21 or higher.

### Dependencies

## Show or hide a password based on user toggle

To show or hide a password based on a user toggle, create an input field for entering information and use a clickable icon for the toggle:

```
@Composable
fun PasswordTextField() {
val state = remember { TextFieldState() }
var showPassword by remember { mutableStateOf(false) }
BasicSecureTextField(
state = state,
textObfuscationMode =
if (showPassword) {
TextObfuscationMode.Visible
} else {
TextObfuscationMode.RevealLastTyped
},
modifier = Modifier
.fillMaxWidth()
.padding(6.dp)
.border(1.dp, Color.LightGray, RoundedCornerShape(6.dp))
.padding(6.dp),
decorator = { innerTextField ->
Box(modifier = Modifier.fillMaxWidth()) {
Box(
modifier = Modifier
.align(Alignment.CenterStart)
.padding(start = 16.dp, end = 48.dp)
) {
innerTextField()
}
Icon(
if (showPassword) {
Icons.Filled.Visibility
} else {
Icons.Filled.VisibilityOff
},
contentDescription = "Toggle password visibility",
modifier = Modifier
.align(Alignment.CenterEnd)
.requiredSize(48.dp).padding(16.dp)
.clickable { showPassword = !showPassword }
)
}
}
)
}
```

### Key points about the code
- Maintains the password visibility state in`showPassword`.
- Uses a `BasicSecureTextField` composable for password entry.
- Has a clickable trailing icon, which toggles the value of `showPassword`.
- Defines the `textObfuscationMode` attribute and the visible/not-visible state of the trailing icon by the state of `showPassword`.

## Collections that contain this guide

This guide is part of these curated Quick Guide collections that cover broader Android development goals:

###### Display text
Text is a central piece of any UI. Find out different ways you can present text in your app to provide a delightful user experience.
Quick guide collection

###### Request user input
Learn how to implement ways for users to interact with your app by entering text and using other means of input.
Quick guide collection
