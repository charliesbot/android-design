# Chip
Source: https://developer.android.com/develop/ui/compose/components/chip

The `Chip` component is a compact, interactive UI element. It represents complex entities like a contact or tag, often with an icon and label. It can be checkable, dismissible, or clickable.

The four types of chips and where you might use them are as follows:
- **Assist**: Guides the user during a task. Often appears as a temporary UI element in response to user input.
- **Filter**: Allows users to refine content from a set of options. They can be selected or deselected, and may include a checkmark icon when selected.
- **Input**: Represents user-provided information, such as selections in a menu. They can contain an icon and text, and provide an **X** for removal.
- **Suggestion**: Provides recommendations to the user based on their recent activity or input. Typically appear beneath an input field to prompt user actions.

[IMAGE] An example of each of the four chip components, with their unique characteristics highlighted.

**Figure 1.** The four chip components.

## API surface

There are four composables that correspond to the four types of chips. The following sections outline these composables and their differences in detail. However, they share the following parameters:
- **`label`**: The string that appears on the chip.
- **`icon`**: The icon displayed at the start of the chip. Some of the specific composables have a separate `leadingIcon` and `trailingIcon` parameter.
- **`onClick`**: The lambda that the chip calls when the user presses it.

## Assist chip

The `AssistChip` composable provides a straightforward way to create an assist chip that nudges the user in a particular direction. One distinguishing feature is its `leadingIcon` parameter that lets you display an icon on the left side of the chip. The following example demonstrates how you can implement it:

```
@Composable
fun AssistChipExample() {
AssistChip(
onClick = { Log.d("Assist chip", "hello world") },
label = { Text("Assist chip") },
leadingIcon = {
Icon(
painter = rememberVectorPainter(AppIcons.Settings),
contentDescription = "Localized description",
Modifier.size(AssistChipDefaults.IconSize)
)
}
)
}
```

This implementation appears as follows.
[IMAGE] An assist chip with a settings icon and the label 'Assist chip'.

**Figure 2.** Assist chip.

## Filter chip

The `FilterChip` composable requires you to track whether or not the chip is selected. The following example demonstrates how you can show a leading checked icon only when the user has selected the chip:

```
@Composable
fun FilterChipExample() {
var selected by remember { mutableStateOf(false) }

FilterChip(
onClick = { selected = !selected },
label = {
Text("Filter chip")
},
selected = selected,
leadingIcon = if (selected) {
{
Icon(
painter = rememberVectorPainter(AppIcons.Done),
contentDescription = "Done icon",
modifier = Modifier.size(FilterChipDefaults.IconSize)
)
}
} else {
null
},
)
}
```

This implementation appears as follows when selected:
[IMAGE] A selected filter chip labeled 'Filter chip' with a leading check mark.

**Figure 3.** Selected filter chip.

## Input chip

You can use the `InputChip` composable to create chips that result from user interaction. For example, in an email client, when the user is writing an email, an input chip might represent a contact the user has added to the "To:" field.

The following implementation demonstrates an input chip that is already in a selected state. The user dismisses the chip when they press it. **Note:** Consider how you might use a chip like this in the preceding email use case, with a name passed in for the `label` parameter, and a function that performs the necessary network calls for `onDismiss`.

```
@Composable
fun InputChipExample(
text: String,
onDismiss: () -> Unit,
) {
var enabled by remember { mutableStateOf(true) }
if (!enabled) return

InputChip(
onClick = {
onDismiss()
enabled = !enabled
},
label = { Text(text) },
selected = enabled,
avatar = {
Icon(
painter = rememberVectorPainter(AppIcons.Person),
contentDescription = "Localized description",
Modifier.size(InputChipDefaults.AvatarSize)
)
},
trailingIcon = {
Icon(
painter = rememberVectorPainter(AppIcons.Close),
contentDescription = "Localized description",
Modifier.size(InputChipDefaults.AvatarSize)
)
},
)
}
```

This implementation appears as follows.
[IMAGE] An input chip with a person icon, the label 'Input Chip' and a close icon.

**Figure 4.** Input chip.

## Suggestion chip

The `SuggestionChip` composable is the most basic of the composables listed on this page, both in its API definition and its common use cases. Suggestion chips present dynamically generated hints. For example, in an AI chat app, you might use suggestion chips to present possible responses to the most recent message.

Consider this implementation of `SuggestionChip`:

```
@Composable
fun SuggestionChipExample() {
SuggestionChip(
onClick = { Log.d("Suggestion chip", "hello world") },
label = { Text("Suggestion chip") }
)
}
```

This implementation appears as follows:
[IMAGE] A suggestion chip labeled 'Suggestion chip'.

**Figure 5.** Suggestion chip.  **Note:** Although the suggestion chip component is intended for informational purposes, it does still take an `onClick` lambda that you can use to create interactivity.

## Elevated chip

All the examples in this document use the base composables that take a flat appearance. If you want a chip that has an elevated appearance, use one of the three following composables:
- `ElevatedAssistChip`
- `ElevatedFilterChip`
- `ElevatedSuggestionChip`

## M3 Expressive update

Filter and input chips feature stylistic updates, including increased corner radii and spring motion. Assist and suggestion chips have been merged into expressive buttons.

For more information about the M3 Expressive updates for chips, see the Material Design 3 Chips guidelines.

### Corner morphing

Filter and input chips support corner morphing, animating the container's corner radius from 12dp when unselected to fully rounded when selected.

###### Filter

The following snippet demonstrates how to implement a filter chip with corner morphing:

```
@Composable
fun FilterChipWithCornerMorphingSample() {
var selected by remember { mutableStateOf(false) }
FilterChip(
selected = selected,
onClick = { selected = !selected },
label = { Text("Filter chip") },
shapes = FilterChipDefaults.shapes(),
leadingIcon = if (selected) {
{
Icon(
painter = rememberVectorPainter(AppIcons.Done),
contentDescription = null,
modifier = Modifier.size(FilterChipDefaults.IconSize),
)
}
} else {
null
},
)
}
```

[IMAGE] An unselected outlined filter chip with rounded corners labeled 'Filter chip'.

Filter chip using corner morphing to transition shape when selected.

###### Elevated filter

Elevated filter chips provide separation from a visually prominent background and support corner morphing. The following snippet demonstrates how to implement an elevated filter chip with corner morphing:

```
@Composable
fun ElevatedFilterChipWithCornerMorphingSample() {
var selected by remember { mutableStateOf(false) }
ElevatedFilterChip(
selected = selected,
onClick = { selected = !selected },
label = { Text("Filter chip") },
shapes = FilterChipDefaults.shapes(),
leadingIcon = if (selected) {
{
Icon(
painter = rememberVectorPainter(AppIcons.Done),
contentDescription = null,
modifier = Modifier.size(FilterChipDefaults.IconSize),
)
}
} else {
{
Icon(
painter = rememberVectorPainter(AppIcons.Home),
contentDescription = null,
modifier = Modifier.size(FilterChipDefaults.IconSize),
)
}
},
trailingIcon = {
Icon(
painter = rememberVectorPainter(AppIcons.ArrowDropDown),
contentDescription = null,
modifier = Modifier.size(FilterChipDefaults.IconSize),
)
},
)
}
```

[IMAGE] An unselected elevated filter chip with a home icon, the label 'Filter chip', and a trailing drop-down arrow.

Elevated filter chip supporting corner morphing.

###### Input

Input chips represent discrete pieces of information entered by a user and also support corner morphing. The following snippet demonstrates how to implement an input chip with corner morphing:

```
@Composable
fun InputChipWithCornerMorphingSample() {
var selected by remember { mutableStateOf(false) }
InputChip(
selected = selected,
onClick = { selected = !selected },
label = { Text("Input Chip") },
shapes = InputChipDefaults.shapes(),
avatar = {
Icon(
painter = rememberVectorPainter(AppIcons.Person),
contentDescription = null,
modifier = Modifier.size(InputChipDefaults.AvatarSize),
)
},
)
}
```

[IMAGE] An unselected outlined input chip with a person icon and the label 'Input Chip'.

Input chip supporting corner morphing.

### Assist and suggestion chips

Assist and suggestion chips are merged into expressive buttons in Material 3 Expressive because they functionally mirror single-action button behaviors. Use expressive buttons when you need expressive assist or suggestion chip behavior; see the Buttons documentation for implementation details.

## Additional resources
- Material UI docs
