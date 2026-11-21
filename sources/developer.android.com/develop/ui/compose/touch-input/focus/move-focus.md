# Move and clear focus
Source: https://developer.android.com/develop/ui/compose/touch-input/focus/move-focus

`FocusManager` lets you move focus directionally or clear it entirely. Use `FocusRequester` for direct component targeting (see Programmatically request focus).

## Directional focus movement with FocusManager

To move focus programmatically in a relative direction:
- Obtain the `FocusManager` instance using `LocalFocusManager.current`.
- Call `moveFocus()` with a `FocusDirection` value (`Next`, `Previous`, `Up`, `Down`, `Left`, `Right`, `Enter`, or `Exit`).

```
val focusManager = LocalFocusManager.current

Button(
onClick = {
focusManager.moveFocus(FocusDirection.Next)
}
) {
Text("Next Field")
}
```

## Clear focus

To remove focus from whichever element currently holds it (for example, when the user presses the Esc key), call `clearFocus()`:

```
val focusManager = LocalFocusManager.current

Box(
modifier = Modifier.onPreviewKeyEvent { keyEvent ->
if (keyEvent.key == Key.Escape && keyEvent.type == KeyEventType.KeyUp) {
focusManager.clearFocus()
true
} else {
false
}
}
)
```

The `clearFocus()` function accepts an optional `force: Boolean = false` parameter:
- **`force = false` (default)**: Respects focus locks and will not clear focus if the active component has captured focus.
- **`force = true`**: Unconditionally clears focus even if a component has captured focus.

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Focus targets
- Focus traversal order
- Programmatically request focus

Previous  arrow_back   Programmatically request focus

Next  Capture and release focus   arrow_forward
