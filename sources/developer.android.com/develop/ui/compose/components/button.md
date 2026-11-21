# Button
Source: https://developer.android.com/develop/ui/compose/components/button

Buttons are fundamental components that allow the user to trigger a defined action. There are five types of buttons. This table describes the appearance of each of the five button types, as well as where you should use them:

| Type  | Appearance  | Purpose
| Filled  | Solid background with contrasting text.  | High-emphasis buttons. These are for primary actions in an application, such as "submit" and "save." The shadow effect highlights the button's importance.
| Filled tonal  | Background color varies to match the surface.  | Also for primary or significant actions. Filled tonal buttons provide more visual weight and suit functions such as "add to cart" and "Sign in."
| Elevated  | Stands out by having a shadow.  | Serves a similar purpose to tonal buttons. Increase elevation to make the button appear even more prominently.
| Outlined  | Features a border with no fill.  | Medium-emphasis buttons, containing actions that are important but not primary. They pair well with other buttons to indicate alternative, secondary actions like "Cancel" or "Back."
| Text  | Displays text with no background or border.  | Low-emphasis buttons, ideal for less critical actions such as navigational links, or secondary functions like "Learn More" or "View details."

This image demonstrates the five types of buttons in Material Design:
[IMAGE] An example of each of the five button components, with their unique characteristics highlighted.

**Figure 1.** The five button components.

## API surface
- `onClick`: The function that the system calls when the user presses the button.
- `enabled`: When `false`, this parameter makes the button appear unavailable and inactive.
- `colors`: An instance of `ButtonColors` that determines the colors used in the button.
- `contentPadding`: The padding within the button.

## Filled button

The filled button component uses the basic `Button` composable. It is filled with a solid color by default. The snippet shows how to implement the component and preview it interactively:

```
@Composable
fun FilledButtonExample(onClick: () -> Unit) {
Button(onClick = { onClick() }) {
Text("Filled")
}
}
```

[IMAGE] A filled button with a black background that reads, 'Filled'.

Figure 2. A filled button.   **Note:** If you want to build a custom button, use the `Button` composable.

## Filled tonal button

The filled tonal button component uses the `FilledTonalButton` composable. It is filled with a tonal color by default.

The snippet shows how to implement the component and preview it interactively:

```
@Composable
fun FilledTonalButtonExample(onClick: () -> Unit) {
FilledTonalButton(onClick = { onClick() }) {
Text("Tonal")
}
}
```

[IMAGE] A tonal button with a light gray background that reads, 'Tonal'.

Figure 3. A tonal button.

## Outlined button

The outlined button component uses the `OutlinedButton` composable. It appears with an outline by default.

The snippet shows how to implement the component and preview it interactively:

```
@Composable
fun OutlinedButtonExample(onClick: () -> Unit) {
OutlinedButton(onClick = { onClick() }) {
Text("Outlined")
}
}
```

[IMAGE] A transparent outlined button with a dark border that reads, 'Outlined'.

Figure 4. An outlined button.

## Elevated button

The elevated button component uses the `ElevatedButton` composable. It has a shadow that represents the elevation effect by default. It is a filled button that includes a shadow.

The snippet shows how to implement the component and preview it interactively:

```
@Composable
fun ElevatedButtonExample(onClick: () -> Unit) {
ElevatedButton(onClick = { onClick() }) {
Text("Elevated")
}
}
```

[IMAGE] An elevated button with a gray background that reads, 'Elevated'.

Figure 5. An elevated button.

## Text button

The text button component uses the `TextButton` composable. It appears as only text until pressed. It does not have a solid fill or outline by default.

The snippet shows how to implement the component and preview it interactively:

```
@Composable
fun TextButtonExample(onClick: () -> Unit) {
TextButton(
onClick = { onClick() }
) {
Text("Text Button")
}
}
```

[IMAGE] A text button that reads 'Text Button'.

Figure 6. A text button.

## M3 Expressive update

Buttons now have a wider variety of shapes and sizes and toggle functionality, and can change shape when selected.
[IMAGE] Sizes and shapes of button

**Figure 7.** Material 3 Expressive button size and shape variations.

For more information about the M3 Expressive updates for buttons, see the Material Design 3 Buttons guidelines.

### Toggle buttons

Four toggle button styles are available: filled, elevated, tonal, and outlined.

###### Filled

The filled button style has the most visual impact after the floating action button (FAB), and should be used for important, final actions that complete a flow, like **Save**, **Join now**, or **Confirm**.

The following snippet demonstrates how to implement a filled toggle button:

```
@Composable
fun ToggleButtonSample() {
var checked by remember { mutableStateOf(false) }
ToggleButton(checked = checked, onCheckedChange = { checked = it }) {
Text("Button")
}
}
```

[IMAGE] A filled toggle button in its unselected state with a light gray background that reads, 'Button'.

A filled toggle button.   **Note:** The filled style should be used sparingly, ideally for only one action on a page.

###### Elevated

Elevated buttons provide separation from a visually prominent background.

The following snippet demonstrates how to implement an elevated toggle button:

```
@Composable
fun ElevatedToggleButtonSample() {
var checked by remember { mutableStateOf(false) }
ElevatedToggleButton(checked = checked, onCheckedChange = { checked = it }) {
Text("Elevated Button")
}
}
```

[IMAGE] An elevated toggle button in its unselected state with a subtle shadow that reads, 'Elevated Button'.

An elevated toggle button.   **Note:** Buttons at higher elevations typically have more emphasis in a design, and should be used sparingly.

###### Tonal

The tonal button style is filled with a tonal color by default. It is useful in contexts where a lower-priority button requires slightly more emphasis than an outline would give.

The following snippet demonstrates how to implement a tonal toggle button using `FilledTonalToggleButton`:

```
@Composable
fun TonalToggleButtonSample() {
var checked by remember { mutableStateOf(false) }
FilledTonalToggleButton(checked = checked, onCheckedChange = { checked = it }) {
Text("Tonal Button")
}
}
```

[IMAGE] A tonal toggle button in its unselected state with a light gray background that reads, 'Tonal Button'.

A tonal toggle button.

###### Outlined

The outlined style is ideal for medium-emphasis buttons that contain actions that are important, but are not the primary action in a product.

The following snippet demonstrates how to implement an outlined toggle button:

```
@Composable
fun OutlinedToggleButtonSample() {
var checked by remember { mutableStateOf(false) }
OutlinedToggleButton(checked = checked, onCheckedChange = { checked = it }) {
Text("Outlined Button")
}
}
```

[IMAGE] An outlined toggle button in its unselected state with a dark border that reads, 'Outlined Button'.

An outlined toggle button.   **Note:** Outlined buttons should be placed on plain backgrounds, not visually prominent backgrounds such as images or videos.

### Size options

Material 3 Expressive adds four sizing options besides the default small size: extra small, medium, large, and extra large. Expressive button overloads take a required `shapes` parameter, or you can apply expressive sizing to a standard `Button` by setting `ButtonDefaults.contentPaddingFor(...)`, as the following examples show:

###### Small (existing default)

Small button measurements:
- Height: 40dp

**Default button**

The following snippet demonstrates how to implement a small button with an icon:

```
@Composable
fun ButtonWithIconSample() {
Button(
onClick = { /* Do something! */ },
contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MinHeight, hasStartIcon = true),
) {
Icon(
painter = rememberVectorPainter(AppIcons.FavoriteFilled),
contentDescription = null,
modifier = Modifier.size(ButtonDefaults.iconSizeFor(ButtonDefaults.MinHeight)),
)
Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(ButtonDefaults.MinHeight)))
Text("Like")
}
}
```

[IMAGE] A small filled button with a black background, a leading heart icon, and the label 'Like'.

**Toggle button**

The following snippet demonstrates how to implement a small toggle button with an icon:

```
@Composable
fun ToggleButtonWithIconSample() {
var checked by remember { mutableStateOf(false) }
ToggleButton(checked = checked, onCheckedChange = { checked = it }) {
Icon(
painter = rememberVectorPainter(if (checked) AppIcons.FavoriteFilled else AppIcons.Favorite),
contentDescription = null,
modifier = Modifier.size(ButtonDefaults.IconSize),
)
Spacer(Modifier.size(ButtonDefaults.IconSpacing))
Text("Like")
}
}
```

[IMAGE] A small toggle button in its unselected state with a leading outlined heart icon and the label 'Like'.

###### Extra small

Extra small button measurements:
- Height: 32dp

**Default button**

The following snippet demonstrates how to implement an extra small button with an icon:

```
@Composable
fun XSmallButtonWithIconSample() {
val size = ButtonDefaults.ExtraSmallContainerHeight
Button(
onClick = { /* Do something! */ },
modifier = Modifier.heightIn(size),
contentPadding = ButtonDefaults.contentPaddingFor(size, hasStartIcon = true),
) {
Icon(
painter = rememberVectorPainter(AppIcons.EditFilled),
contentDescription = null,
modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
)
Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))
Text("Label")
}
}
```

[IMAGE] An extra small filled button with a black background, a leading edit icon, and the label 'Label'.

**Toggle button**

The following snippet demonstrates how to implement an extra small toggle button with an icon:

```
@Composable
fun XSmallToggleButtonWithIconSample() {
var checked by remember { mutableStateOf(false) }
val size = ButtonDefaults.ExtraSmallContainerHeight
ToggleButton(
checked = checked,
onCheckedChange = { checked = it },
modifier = Modifier.heightIn(size),
shapes = ToggleButtonDefaults.shapesFor(size),
contentPadding = ButtonDefaults.contentPaddingFor(size),
) {
Icon(
painter = rememberVectorPainter(if (checked) AppIcons.EditFilled else AppIcons.Edit),
contentDescription = null,
modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
)
Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))
Text("Label")
}
}
```

[IMAGE] An extra small toggle button in its unselected state with a leading edit icon and the label 'Label'.

###### Medium

Medium button measurements:
- Height: 56dp

**Default button**

The following snippet demonstrates how to implement a medium button with an icon:

```
@Composable
fun MediumButtonWithIconSample() {
val size = ButtonDefaults.MediumContainerHeight
Button(
onClick = { /* Do something! */ },
modifier = Modifier.heightIn(size),
contentPadding = ButtonDefaults.contentPaddingFor(size, hasStartIcon = true),
) {
Icon(
painter = rememberVectorPainter(AppIcons.EditFilled),
contentDescription = null,
modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
)
Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))
Text("Label", style = ButtonDefaults.textStyleFor(size))
}
}
```

[IMAGE] A medium filled button with a black background, a leading edit icon, and the label 'Label'.

**Toggle button**

The following snippet demonstrates how to implement a medium toggle button with an icon:

```
@Composable
fun MediumToggleButtonWithIconSample() {
var checked by remember { mutableStateOf(false) }
val size = ButtonDefaults.MediumContainerHeight
ToggleButton(
checked = checked,
onCheckedChange = { checked = it },
modifier = Modifier.heightIn(size),
shapes = ToggleButtonDefaults.shapesFor(size),
contentPadding = ButtonDefaults.contentPaddingFor(size),
) {
Icon(
painter = rememberVectorPainter(if (checked) AppIcons.EditFilled else AppIcons.Edit),
contentDescription = null,
modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
)
Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))
Text("Label", style = ButtonDefaults.textStyleFor(size))
}
}
```

[IMAGE] A medium toggle button in its unselected state with a leading edit icon and the label 'Label'.

###### Large

Large button measurements:
- Height: 96dp

**Default button**

The following snippet demonstrates how to implement a large button with an icon:

```
@Composable
fun LargeButtonWithIconSample() {
val size = ButtonDefaults.LargeContainerHeight
Button(
onClick = { /* Do something! */ },
modifier = Modifier.heightIn(size),
contentPadding = ButtonDefaults.contentPaddingFor(size, hasStartIcon = true),
) {
Icon(
painter = rememberVectorPainter(AppIcons.EditFilled),
contentDescription = null,
modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
)
Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))
Text("Label", style = ButtonDefaults.textStyleFor(size))
}
}
```

[IMAGE] A large filled button with a black background, a leading edit icon, and the label 'Label'.

**Toggle button**

The following snippet demonstrates how to implement a large toggle button with an icon:

```
@Composable
fun LargeToggleButtonWithIconSample() {
var checked by remember { mutableStateOf(false) }
val size = ButtonDefaults.LargeContainerHeight
ToggleButton(
checked = checked,
onCheckedChange = { checked = it },
modifier = Modifier.heightIn(size),
shapes = ToggleButtonDefaults.shapesFor(size),
contentPadding = ButtonDefaults.contentPaddingFor(size),
) {
Icon(
painter = rememberVectorPainter(if (checked) AppIcons.EditFilled else AppIcons.Edit),
contentDescription = null,
modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
)
Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))
Text("Label", style = ButtonDefaults.textStyleFor(size))
}
}
```

[IMAGE] A large toggle button in its unselected state with a leading edit icon and the label 'Label'.

###### Extra large

Extra large button measurements:
- Height: 136dp

**Default button**

The following snippet demonstrates how to implement an extra large button with an icon:

```
@Composable
fun XLargeButtonWithIconSample() {
val size = ButtonDefaults.ExtraLargeContainerHeight
Button(
onClick = { /* Do something! */ },
modifier = Modifier.heightIn(size),
contentPadding = ButtonDefaults.contentPaddingFor(size, hasStartIcon = true),
) {
Icon(
painter = rememberVectorPainter(AppIcons.EditFilled),
contentDescription = null,
modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
)
Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))
Text("Label", style = ButtonDefaults.textStyleFor(size))
}
}
```

[IMAGE] An extra large filled button with a black background, a leading edit icon, and the label 'Label'.

**Toggle button**

The following snippet demonstrates how to implement an extra large toggle button with an icon:

```
@Composable
fun XLargeToggleButtonWithIconSample() {
var checked by remember { mutableStateOf(false) }
val size = ButtonDefaults.ExtraLargeContainerHeight
ToggleButton(
checked = checked,
onCheckedChange = { checked = it },
modifier = Modifier.heightIn(size),
shapes = ToggleButtonDefaults.shapesFor(size),
contentPadding = ButtonDefaults.contentPaddingFor(size),
) {
Icon(
painter = rememberVectorPainter(if (checked) AppIcons.EditFilled else AppIcons.Edit),
contentDescription = null,
modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
)
Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))
Text("Label", style = ButtonDefaults.textStyleFor(size))
}
}
```

[IMAGE] An extra large toggle button in its unselected state with a leading edit icon and the label 'Label'.

### Shape options

Two shape options are available: square and round.
[IMAGE] Square buttons changing size.

**Figure 8.** Square button corner radius scaling across sizes.

Square buttons have containers with subtle corner rounding that scales based on button size.
[IMAGE] Example of button with rounded corners.

**Figure 9.** Button with rounded corners.

Round button types have containers with fully rounded corners. When checked, a square toggle button's shape changes to round.

The following snippet demonstrates how to implement a square toggle button that changes its shape to round when checked:

```
@Composable
fun SquareToggleButtonSample() {
var checked by remember { mutableStateOf(false) }
val shapes =
ToggleButtonShapes(
shape = ToggleButtonDefaults.checkedShape,
pressedShape = ToggleButtonDefaults.pressedShape,
checkedShape = ToggleButtonDefaults.shape,
)
ToggleButton(checked = checked, onCheckedChange = { checked = it }, shapes = shapes) {
Text("Button")
}
}
```

[IMAGE] A square toggle button in its unselected state with softly rounded corners that reads, 'Button'.

## Additional resources
- Floating action button
- Material Design 3 Buttons overview
