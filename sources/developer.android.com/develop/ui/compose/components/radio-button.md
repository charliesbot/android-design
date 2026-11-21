# Radio button
Source: https://developer.android.com/develop/ui/compose/components/radio-button

A radio button lets a user select an option from a set of options. You use a radio button when only one item can be selected from a list. If users need to select more than one item, use a switch instead.
[IMAGE] Two radio buttons with no labels. The left button is selected, and the circle is filled in to indicate its selected state. The right button is not filled in

**Figure 1.** A pair of radio buttons with one option selected.

## API surface

Use the `RadioButton` composable to list the available options. Wrap each `RadioButton` option and its label inside a `Row` component to group them together.

`RadioButton` includes the following key parameters:
- `selected`: Indicates whether the radio button is selected.
- `onClick`: A lambda function that your app executes when the user clicks the radio button. If this is `null`, the user can't interact directly with the radio button.
- `enabled`: Controls whether the radio button is enabled or disabled. Users can't interact with disabled radio buttons.
- `interactionSource`: Lets you observe the interaction state of the button, for example, whether it's pressed, hovered, or focused.

## Create a basic radio button

The following code snippet renders a list of radio buttons within a `Column`:

```
@Composable
fun RadioButtonSingleSelection(modifier: Modifier = Modifier) {
val radioOptions = listOf("Calls", "Missed", "Friends")
val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }
// Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
Column(modifier.selectableGroup()) {
radioOptions.forEach { text ->
Row(
Modifier
.fillMaxWidth()
.height(56.dp)
.selectable(
selected = (text == selectedOption),
onClick = { onOptionSelected(text) },
role = Role.RadioButton
)
.padding(horizontal = 16.dp),
verticalAlignment = Alignment.CenterVertically
) {
RadioButton(
selected = (text == selectedOption),
onClick = null // null recommended for accessibility with screen readers
)
Text(
text = text,
style = MaterialTheme.typography.bodyLarge,
modifier = Modifier.padding(start = 16.dp)
)
}
}
}
}
```

### Key points about the code
- `radioOptions` represents the labels for the radio buttons.
- The `remember` composable function creates a state variable `selectedOption` and a function to update that state called `onOptionSelected`. This state holds the selected radio button option.
  - `mutableStateOf(radioOptions[0])` initializes the state to the first item in the list. "Calls" is the first item, so it's the radio button selected by default.

- `Modifier.selectableGroup()` ensures proper accessibility behavior for screen readers. It informs the system that the elements within this `Column` are part of a selectable group, which enables proper screen reader support.
- `Modifier.selectable()` makes the entire `Row` act as a single selectable item.
  - `selected` indicates whether the current `Row` is selected based on the `selectedOption` state.
  - The `onClick` lambda function updates the `selectedOption` state to the clicked option when the `Row` is clicked.
  - `role = Role.RadioButton` informs accessibility services that the `Row` functions as a radio button.

- `RadioButton(...)` creates the `RadioButton` composable.
  - `onClick = null` on the `RadioButton` improves accessibility. This prevents the radio button from handling the click event directly, and allows the `Row`'s `selectable` modifier to manage the selection state and accessibility behavior.

### Result
[IMAGE] Three radio buttons labeled 'Calls', 'Missed' and 'Friends', with 'Calls' selected.

**Figure 2.** Three radio buttons with the "Calls" option selected.

## Additional resources
- Material Design: Buttons
