# Focus traversal order
Source: https://developer.android.com/develop/ui/compose/touch-input/focus/focus-traversal

Users can move keyboard focus across UI elements using the Tab key or directional (arrow / D-pad) keys:
- Tab / Shift+Tab: Move focus forward or backward in one-dimensional appearance order.
- Directional keys: Move focus two-dimensionally (Up, Down, Left, and Right).

## One-dimensional focus traversal

In one-dimensional focus traversal, the Tab key advances focus through the UI based on the visual **appearance order** on the screen.

For example, when four buttons are arranged in rows:

```
Column {
Row {
Button(onClick = { /* ... */ }) { Text("1st") }
Button(onClick = { /* ... */ }) { Text("2nd") }
}
Row {
Button(onClick = { /* ... */ }) { Text("3rd") }
Button(onClick = { /* ... */ }) { Text("4th") }
}
}
```

Pressing the Tab key moves focus sequentially across the buttons in the following order:
- **1st button** (top-start)
- **2nd button** (top-end)
- **3rd button** (bottom-start)
- **4th button** (bottom-end)

Pressing Tab on the final element wraps back to the first focus target. Pressing Shift+Tab moves focus in reverse order.

## Two-dimensional focus traversal

Pressing keyboard arrow keys or using a D-pad triggers two-dimensional focus traversal.

In two-dimensional traversal, the system inspects the geometric coordinates and spatial boundaries of UI elements to determine the closest target in the requested direction.

Two-dimensional focus traversal **does not wrap around**. If the user presses the Down key while focused on the bottom-most element, focus remains on that element rather than jumping to the top of the screen.

## Reset focus traversal with pointer clicks

When switching between a physical keyboard and a mouse or touchpad on desktop or large-screen devices:
- **Clear focus on tap or click**: Clicking or tapping on non-interactive space with a mouse or touchpad releases focus from the active element.
- **Restart traversal**: After focus is cleared, the next Tab key press restarts one-dimensional traversal at the **first focus target** in visual appearance order, rather than resuming from the previously active element. It is same to the two-dimensional focus traversal. The directional keys moves keyboard focus to the closest focus target in the requested direction.

For details on programmatically clearing focus, see Move and clear focus.

## Customize focus traversal order

You can customize traversal behavior with the `focusProperties` modifier.

### Customize one-dimensional traversal order

To override one-dimensional traversal, specify the `next` or `previous` property with a `FocusRequester`:
- Create a `FocusRequester` object with `remember { FocusRequester() }`.
- Attach the `FocusRequester` to the target composable using the `focusRequester` modifier.
- Apply the `focusProperties` modifier to the source composable and assign the `FocusRequester` to `next` (for Tab) or `previous` (for Shift+Tab).

```
val (first, second, third) = remember { FocusRequester.createRefs() }

Column {
Button(
onClick = { /* ... */ },
modifier = Modifier
.focusRequester(first)
.focusProperties { next = third }
) {
Text("First (Tab jumps to Third)")
}
Button(
onClick = { /* ... */ },
modifier = Modifier.focusRequester(second)
) {
Text("Second")
}
Button(
onClick = { /* ... */ },
modifier = Modifier.focusRequester(third)
) {
Text("Third")
}
}
```

### Customize two-dimensional traversal order

Similarly, you can override two-dimensional focus traversal by assigning `FocusRequester` instances to `up`, `down`, `start`, `end`, `left`, or `right` within `focusProperties`:

```
val (topButton, bottomButton) = remember { FocusRequester.createRefs() }

Button(
onClick = { /* ... */ },
modifier = Modifier
.focusRequester(topButton)
.focusProperties {
down = bottomButton
right = bottomButton
}
) {
Text("Top button")
}
Button(
onClick = { /* ... */ },
modifier = Modifier.focusRequester(bottomButton)
) {
Text("Bottom button")
}
```

To block focus navigation in a specific direction (for example, at layout boundaries), assign `FocusRequester.Cancel` to that property (for example, `down = FocusRequester.Cancel`). To explicitly retain the system's default traversal algorithm for a given direction, assign `FocusRequester.Default`.

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Focus targets
- Focus in text fields
- Group focus targets
- Move and clear focus
- Programmatically request focus
- Test focus navigation

Previous  arrow_back   Focus targets

Next  Focus in text fields   arrow_forward
