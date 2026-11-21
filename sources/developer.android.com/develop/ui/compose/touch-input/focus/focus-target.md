# Focus targets
Source: https://developer.android.com/develop/ui/compose/touch-input/focus/focus-target

A focus target is a UI element that keyboard focus can move to. Users can move keyboard focus with the Tab key or directional (arrow) keys:
- Tab or Shift+Tab — Move focus forward or backward in one-dimensional appearance order.
- Directional keys — Move focus two-dimensionally (Up, Down, Left, and Right).

## Interactive UI elements are focus targets by default

An interactive component is a focus target by default. In other words, a UI element is a focus target if users can tap or click it.

For example, consider three Card components. If the first and third cards are interactive (they define an `onClick` callback), but the second card is purely informational (has no click handling), pressing the Tab key on the first card skips the second card and advances directly to the third card.

```
Card(onClick = { /* First card action */ }) {
Text("Card 1 (Interactive)")
}
Card {
Text("Card 2 (Informational - not focusable)")
}
Card(onClick = { /* Third card action */ }) {
Text("Card 3 (Interactive)")
}
```

You can make the second card a focus target by providing an `onClick` parameter:

```
Card(onClick = { /* Second card action */ }) {
Text("Card 2 (Now interactive and focusable)")
}
```

The `clickable` modifier also turns custom or standard UI elements into focus targets. Refer to Tap and press for details. **Warning:** Don't use the `focusable` modifier and the `clickable` modifier at the same time. It leads to incorrect behavior because it creates two independent focus targets.

```
Box(
modifier = Modifier
.clickable { /* Handle click */ }
) {
Text("Custom interactive element")
}
```

## Make a composable a non-clickable focus target

The `focusable` modifier makes a composable a focus target without requiring click semantics. Users can move keyboard focus to the modified composable (for example, to inspect content with accessibility tools or scroll the viewport), but pressing Enter or Space keys don't trigger a click action:

```
Box(
modifier = Modifier
.focusable()
) {
Text("Non-clickable focus target")
}
```

## Make a composable unfocusable

In some scenarios, you may want to exclude an interactive element from keyboard navigation without disabling pointer or click events. You can prevent a component from receiving focus by setting `canFocus = false` inside the `focusProperties` modifier:

```
var checked by remember { mutableStateOf(false) }

Switch(
checked = checked,
onCheckedChange = { checked = it },
// Prevent component from being focused
modifier = Modifier
.focusProperties { canFocus = false }
)
```

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Focus traversal order
- Focus in text fields
- Group focus targets
- Indicate focus state
- Test focus navigation

Previous  arrow_back   Overview

Next  Focus traversal order   arrow_forward
