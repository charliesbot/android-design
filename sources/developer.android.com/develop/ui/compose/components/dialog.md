# Dialog
Source: https://developer.android.com/develop/ui/compose/components/dialog

The `Dialog` component displays dialog messages or requests user input on a layer above the main app content. It creates an interruptive UI experience to capture user attention.

Among the use cases for a dialog are the following:
- Confirming user action, such as when deleting a file.
- Requesting user input, such as in a to-do list app.
- Presenting a list of options for user selection, like choosing a country in a profile setup.

[IMAGE] A dialog populated with text and icons.

**Figure 1.** An example of a dialog populated with text and icons.

## Alert dialog

The `AlertDialog` composable provides a convenient API for creating a Material Design themed dialog. `AlertDialog` has specific parameters for handling particular elements of the dialog. Among them are the following:
- `title`: The text that appears along the top of the dialog.
- `text`: The text that appears centered within the dialog.
- `icon`: The graphic that appears at the top of the dialog.
- `onDismissRequest`: The function called when the user dismisses the dialog, such as by tapping outside of it.
- `dismissButton`: A composable that serves as the dismiss button.
- `confirmButton`: A composable that serves as the confirm button.

The following example implements two buttons in an alert dialog, one that dismisses the dialog, and another that confirms its request.

```
@Composable
fun AlertDialogExample(
onDismissRequest: () -> Unit,
onConfirmation: () -> Unit,
dialogTitle: String,
dialogText: String,
icon: ImageVector,
) {
AlertDialog(
icon = {
Icon(painter = rememberVectorPainter(icon), contentDescription = "Example Icon")
},
title = {
Text(text = dialogTitle)
},
text = {
Text(text = dialogText)
},
onDismissRequest = {
onDismissRequest()
},
confirmButton = {
TextButton(
onClick = {
onConfirmation()
}
) {
Text("Confirm")
}
},
dismissButton = {
TextButton(
onClick = {
onDismissRequest()
}
) {
Text("Dismiss")
}
}
)
}
```

This implementation implies a parent composable that passes arguments to the child composable in this way:

```
@Composable
fun DialogExamples() {
// ...
val openAlertDialog = remember { mutableStateOf(false) }

// ...
when {
// ...
openAlertDialog.value -> {
AlertDialogExample(
onDismissRequest = { openAlertDialog.value = false },
onConfirmation = {
openAlertDialog.value = false
println("Confirmation registered") // Add logic here to handle confirmation.
},
dialogTitle = "Alert dialog example",
dialogText = "This is an example of an alert dialog with buttons.",
icon = AppIcons.Info
)
}
}
}
}
```

This implementation appears as follows:
[IMAGE] An alert dialog with an info icon, the title 'Alert dialog example', the text 'This is an example of an alert dialog with buttons.', and 'Dismiss' and 'Confirm' buttons.

**Figure 2.** An alert dialog with buttons.  **Note:** When the user clicks either of the buttons, the dialog closes. When the user clicks confirm, it calls a function that also handles the confirmation. In this example, those functions are `onDismissRequest()` and `onConfirmRequest()`.**Note:** In cases where your dialog requires a more complex set of buttons, you may benefit from using the `Dialog` composable and populating it in a more freeform manner.

## Dialog composable

`Dialog` is a basic composable that doesn't provide any styling or predefined slots for content. It is a relatively straightforward container that you should populate with a container such as `Card`. The following are some of the key parameters of a dialog:
- **`onDismissRequest`**: The lambda called when the user closes the dialog.
- **`properties`**: An instance of `DialogProperties` that provides some additional scope for customization.
**Caution:** Unlike the example of `AlertDialog` in the preceding section, you need to manually specify the size and shape of `Dialog`. You also need to provide an inner container.

### Basic example

The following example is a basic implementation of the `Dialog` composable. Note that it uses a `Card` as the secondary container. Without the `Card`, the `Text` component would appear alone above the main app content.

```
@Composable
fun MinimalDialog(onDismissRequest: () -> Unit) {
Dialog(onDismissRequest = { onDismissRequest() }) {
Card(
modifier = Modifier
.fillMaxWidth()
.height(200.dp)
.padding(16.dp),
shape = RoundedCornerShape(16.dp),
) {
Text(
text = "This is a minimal dialog",
modifier = Modifier
.fillMaxSize()
.wrapContentSize(Alignment.Center),
textAlign = TextAlign.Center,
)
}
}
}
```

This implementation appears as follows. Note that when the dialog is open, the main app content beneath it appears darkened and grayed out:
[IMAGE] A dialog card that contains only the text 'This is a minimal dialog'.

**Figure 3.** Minimal dialog.

### Advanced example

The following is a more advanced implementation of the `Dialog` composable. In this case, the component manually implements a similar interface to the preceding `AlertDialog` example. **Caution:** If you only need to display a two-button dialog as in this example, you should use `AlertDialog` and its more convenient API. However, if you want to create a more complex dialog, perhaps with forms and multiple buttons, you should use `Dialog` with custom content, as in the following example.

```
@Composable
fun DialogWithImage(
onDismissRequest: () -> Unit,
onConfirmation: () -> Unit,
painter: Painter,
imageDescription: String,
) {
Dialog(onDismissRequest = { onDismissRequest() }) {
// Use the Material dialog shape and color. The Card clips the image to its rounded corners.
Card(
modifier = Modifier
.fillMaxWidth()
.padding(16.dp),
shape = MaterialTheme.shapes.extraLarge,
colors = CardDefaults.cardColors(
containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
),
) {
Column {
// The image fills the full width of the dialog.
Image(
painter = painter,
contentDescription = imageDescription,
contentScale = ContentScale.Crop,
modifier = Modifier
.fillMaxWidth()
.height(160.dp)
)
Text(
text = "This is a dialog with buttons and an image.",
style = MaterialTheme.typography.bodyMedium,
color = MaterialTheme.colorScheme.onSurfaceVariant,
modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp),
)
// Material dialogs align their actions to the end, 8.dp apart.
Row(
modifier = Modifier
.fillMaxWidth()
.padding(24.dp),
horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
) {
TextButton(onClick = { onDismissRequest() }) {
Text("Dismiss")
}
TextButton(onClick = { onConfirmation() }) {
Text("Confirm")
}
}
}
}
}
}
```

This implementation appears as follows:
[IMAGE] A dialog with a photo of a mountain across its full width, the text 'This is a dialog with buttons and an image.', and 'Dismiss' and 'Confirm' buttons aligned to the end.

**Figure 4.** A dialog that includes an image.

## Additional resources
- Material UI docs
