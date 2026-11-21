# Programmatically request focus
Source: https://developer.android.com/develop/ui/compose/touch-input/focus/request-focus

In some user flows, your application needs to explicitly move focus to a specific component. For example, when a user clicks a "Search" button, you might want to automatically place focus into the search input field.

## Use FocusRequester

To explicitly request focus:
- Create and remember a `FocusRequester` instance: `val focusRequester = remember { FocusRequester() }`.
- Attach the `FocusRequester` to the target composable using the `focusRequester` modifier.
- Call `requestFocus()` inside an event callback (such as `onClick`) or a coroutine scope.

```
val focusRequester = remember { FocusRequester() }
var text by remember { mutableStateOf("") }

TextField(
value = text,
onValueChange = { text = it },
modifier = Modifier.focusRequester(focusRequester)
)
```

```
val focusRequester = remember { FocusRequester() }
var text by remember { mutableStateOf("") }

TextField(
value = text,
onValueChange = { text = it },
modifier = Modifier.focusRequester(focusRequester)
)

Button(onClick = { focusRequester.requestFocus() }) {
Text("Request focus on TextField")
}
```
**Note:** Always invoke `focusRequester.requestFocus()` inside event callbacks, side-effects (`LaunchedEffect`), or coroutines—never directly within the body of a composable function, which causes repeated execution on every recomposition.

## Set initial screen focus

To place initial focus on a specific component when a screen first appears, trigger `requestFocus()` inside a `LaunchedEffect(Unit)`:

```
LaunchedEffect(Unit) {
focusRequester.requestFocus()
}

```

When navigating between containers or returning from another screen, you can also attach `focusRestorer()` to parent containers to automatically return focus to the last focused child. For more information, see the Restore focus guide.

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Focus targets
- Group focus targets
- Restore focus
- Move and clear focus

Previous  arrow_back   React to focus

Next  Move and clear focus   arrow_forward
