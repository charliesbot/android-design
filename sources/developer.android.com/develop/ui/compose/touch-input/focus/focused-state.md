# Indicate focus state
Source: https://developer.android.com/develop/ui/compose/touch-input/focus/focused-state

A focused state provides a clear visual cue to help users understand which UI element currently holds input focus. All interactive Material 3 components provide built-in focus indications by default. **Note:** This guide covers how to draw visual focus indications. To programmatically listen to focus transitions, query focus flags (`isFocused`, `hasFocus`, `isCaptured`), or trigger state-driven UI changes, see React to focus.

## Material theme focus indication

Material components use the `ripple` function and `LocalIndication` to draw visual state changes. To attach standard Material focus feedback to a custom component:
- Create a `MutableInteractionSource` using `remember { MutableInteractionSource() }`.
- Pass the interaction source to the `indication` modifier, alongside `ripple()`.

```
val interactionSource = remember { MutableInteractionSource() }

Box(
modifier = Modifier
.clickable(
interactionSource = interactionSource,
indication = null
) { /* onClick */ }
.indication(
interactionSource = interactionSource,
indication = ripple()
)
) {
Text("Custom component with Material ripple")
}
```

## Implement custom focus indications

For custom visual cues (such as scaling, borders, or glowing effects):
- Create an `IndicationNodeFactory`.
- Observe focus interactions emitted by the element's `InteractionSource`.
- Apply the custom indication using the `indication` modifier.

```
var interactionSource = remember { MutableInteractionSource() }

Card(
modifier = Modifier
.clickable(
interactionSource = interactionSource,
indication = MyHighlightIndication,
enabled = true,
onClick = { }
)
) {
Text("hello")
}
```

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Focus targets
- Focus traversal order
- React to focus

Previous  arrow_back   Restore focus

Next  React to focus   arrow_forward
