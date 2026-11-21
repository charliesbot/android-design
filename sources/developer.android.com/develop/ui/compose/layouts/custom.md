# Custom layouts
Source: https://developer.android.com/develop/ui/compose/layouts/custom

In Compose, UI elements are represented by the composable functions that emit a piece of UI when invoked, that is then added to a UI tree that gets rendered on the screen. Each UI element has one parent and potentially many children. Each element is also located within its parent, specified as an (x, y) position, and a size, specified as a `width` and a `height`.

Parents define the constraints for their child elements. An element is asked to define its size within those constraints. Constraints restrict the minimum and maximum `width` and `height` of an element. If an element has child elements, it may measure each child to help determine its size. Once an element determines and reports its own size, it has an opportunity to define how to place its child elements relative to itself, as described in detail in Creating custom layouts.

Laying out each node in the UI tree is a three step process. Each node must:
- Measure any children
- Decide its own size
- Place its children

[IMAGE] Three steps of node layout: measure children, decide size, place children

**Figure 1.** The three steps of node layout are to measure children, decide size, and place children.   **Note:** Compose UI does not permit multi-pass measurement. This means that a layout element may not measure any of its children more than once in order to try different measurement configurations.

The use of scopes defines _when_ you can measure and place your children. Measuring a layout can only be done during the measurement and layout passes, and a child can only be placed during the layout passes (and only after it has been measured). Due to Compose scopes such as `MeasureScope`, and `PlacementScope`, this is enforced at compile time.

## Use the layout modifier

You can use the `layout` modifier to modify how an element is measured and laid out. The modifier takes a lambda; its parameters include the element you can measure, passed as `measurable`, and that composable's incoming constraints, passed as `constraints`. A custom layout modifier can look like this:

```
fun Modifier.customLayoutModifier() =
layout { measurable, constraints ->
// ...
}
```

Display a `Text` on the screen and control the distance from the top to the baseline of the first line of text. This is exactly what the `paddingFromBaseline` modifier does; you're implementing it here as an example. To do that, use the `layout` modifier to manually place the composable on the screen. Here's the resulting behavior where the `Text` top padding is set to `24.dp`:
[IMAGE] Shows the difference between normal UI padding, which sets the space between elements, and text padding that sets the space from one baseline to the next

**Figure 2.** Text with `paddingFromBaseline` applied.

Here's the code to produce that spacing:

```
fun Modifier.firstBaselineToTop(
firstBaselineToTop: Dp
) = layout { measurable, constraints ->
// Measure the composable
val placeable = measurable.measure(constraints)

// Check the composable has a first baseline
check(placeable[FirstBaseline] != AlignmentLine.Unspecified)
val firstBaseline = placeable[FirstBaseline]

// Height of the composable with padding - first baseline
val placeableY = firstBaselineToTop.roundToPx() - firstBaseline
val height = placeable.height + placeableY
layout(placeable.width, height) {
// Where the composable gets placed
placeable.placeRelative(0, placeableY)
}
}
```

Here's what's going on in that code:
- In the `measurable` lambda parameter, you measure the `Text` represented by the measurable parameter by calling `measurable.measure(constraints)`.
- You specify the size of the composable by calling the `layout(width, height)` method, which also gives a lambda used for placing the wrapped elements. In this case, the height is the height of the `Text` plus `placeableY`, which is `firstBaselineToTop` minus the first baseline of the `Text`.
- You position the wrapped elements on the screen by calling `placeable.placeRelative(x, y)`. If the wrapped elements aren't placed, they won't be visible. In a right-to-left layout, `placeRelative` mirrors the `x` position; see Layout direction. The `y` position is `firstBaselineToTop` minus the first baseline of the text, so that the first baseline is `firstBaselineToTop` from the top.

To verify this works as expected, use this modifier on a `Text`:

```
@Preview
@Composable
fun TextWithPaddingToBaselinePreview() {
MyApplicationTheme {
Text("Hi there!", Modifier.firstBaselineToTop(32.dp))
}
}

@Preview
@Composable
fun TextWithNormalPaddingPreview() {
MyApplicationTheme {
Text("Hi there!", Modifier.padding(top = 32.dp))
}
}
```

[IMAGE] Multiple previews of text elements; one shows ordinary padding between elements, the other shows padding from one baseline to the next.

**Figure 3.** Modifier applied to a `Text` composable and previewed.

## Create custom layouts

The `layout` modifier only changes the calling composable. To measure and layout multiple composables, use the `Layout` composable instead. This composable lets you measure and lay out children manually. All higher-level layouts like `Column` and `Row` are built with the `Layout` composable. **Note:** In the View system, creating a custom layout required extending `ViewGroup` and implementing measure and layout functions. In Compose, you write a function using the `Layout` composable.

This example builds a very basic version of `Column`. Most custom layouts follow this pattern:

```
@Composable
fun MyBasicColumn(
modifier: Modifier = Modifier,
content: @Composable () -> Unit
) {
Layout(
modifier = modifier,
content = content
) { measurables, constraints ->
// measure and position children given constraints logic here
// ...
}
}
```

Similarly to the `layout` modifier, `measurables` is the list of children that need to be measured and `constraints` are the constraints from the parent. Following the same logic as before, `MyBasicColumn` can be implemented like this:

```
@Composable
fun MyBasicColumn(
modifier: Modifier = Modifier,
content: @Composable () -> Unit
) {
Layout(
modifier = modifier,
content = content
) { measurables, constraints ->
// Don't constrain child views further, measure them with given constraints
// List of measured children
val placeables = measurables.map { measurable ->
// Measure each children
measurable.measure(constraints)
}

// Set the size of the layout as big as it can
layout(constraints.maxWidth, constraints.maxHeight) {
// Track the y co-ord we have placed children up to
var yPosition = 0

// Place children in the parent layout
placeables.forEach { placeable ->
// Position item on the screen
placeable.placeRelative(x = 0, y = yPosition)

// Record the y co-ord placed up to
yPosition += placeable.height
}
}
}
}
```

The child composables are constrained by the incoming `Layout` constraints, and they're placed based on the `yPosition` of the previous composable.

Here's how that custom composable would be used:

```
@Composable
fun CallingComposable(modifier: Modifier = Modifier) {
MyBasicColumn(modifier.padding(8.dp)) {
Text("MyBasicColumn")
Text("places items")
Text("vertically.")
Text("We've done it by hand!")
}
}
```

[IMAGE] Several text elements stacked one above the next in a column.

**Figure 4.** Custom `Column` implementation.

## Layout direction

Change the layout direction of a composable by changing the `LocalLayoutDirection` composition local.

If you're placing composables manually on the screen, the `LayoutDirection` is available from the `layoutDirection` property of the `MeasureScope` in the `layout` modifier or `Layout` composable.

When using `layoutDirection`, place composables using `place`. Unlike the `placeRelative` method, `place` doesn't change based on the layout direction (left-to-right versus right-to-left).

## Custom layouts in action

Learn more about layouts and modifiers in the Basic layouts in Compose, and see custom layouts in action in the Compose samples that create custom layouts.

## Learn more

To learn more about custom layouts in Compose, consult the following additional resources.

### Videos
- A deep dive into Jetpack Compose Layouts

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Intrinsic measurements in Compose layouts
- Graphics in Compose
- Compose modifiers

Previous  arrow_back   Support trifolds and landscape foldables

Next  Alignment lines   arrow_forward
