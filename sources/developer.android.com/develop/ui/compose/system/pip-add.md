# Add PiP through a button
Source: https://developer.android.com/develop/ui/compose/system/pip-add

To enter PiP mode through a button click, call `enterPictureInPictureMode()` on `findActivity()`.

The parameters are already set by previous calls to the `PictureInPictureParams.Builder`, so you don't need to set new parameters on the builder. However, if you do want to change any parameters on button click, you can set them here.

```
val context = LocalContext.current
Button(onClick = {
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
context.findActivity().enterPictureInPictureMode(
PictureInPictureParams.Builder().build()
)
} else {
Log.i(PIP_TAG, "API does not support PiP")
}
}) {
Text(text = "Enter PiP mode!")
}
```

Previous  arrow_back   Enter PiP at correct times

Next  Add remote actions to PiP   arrow_forward
