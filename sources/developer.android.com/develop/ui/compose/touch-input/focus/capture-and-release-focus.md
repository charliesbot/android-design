# Capture and release focus
Source: https://developer.android.com/develop/ui/compose/touch-input/focus/capture-and-release-focus

In specialized interaction modes (such as an embedded drawing canvas, game controller surface, or modal input dialog), your app might need to temporarily lock focus to a single component and prevent Tab or directional keys from navigating away. **Warning:** Capturing focus can cause accessibility issues. Users might not be able to navigate to other focusable components.

## Capture focus

To capture focus, invoke `captureFocus()` on a `FocusRequester`:

```
val focusRequester = remember { FocusRequester() }

Box(
modifier = Modifier
.focusRequester(focusRequester)
.focusable()
.onFocusChanged { focusState ->
if (focusState.isFocused) {
focusRequester.captureFocus()
}
}
)
```
**Key Point:** The target component must already hold active focus (`isFocused == true`) when calling `captureFocus()`. Invoking `captureFocus()` on an unfocused element returns `false` and fails to capture focus.

While focus is captured, standard focus navigation keys (such as Tab) don't move focus to neighboring components.

## Release focus

To exit the captured state and restore normal focus navigation across the UI, call `freeFocus()`:

```
IconButton(
onClick = {
focusRequester.freeFocus()
}
) {
Icon(Icons.Default.Close, contentDescription = "Exit focus lock")
}
```

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Focus targets
- Group focus targets
- Programmatically request focus

Previous  arrow_back   Move and clear focus

Next  Test focus navigation   arrow_forward
