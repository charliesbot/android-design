# Understand gestures
Source: https://developer.android.com/develop/ui/compose/touch-input/pointer-input/understand-gestures

There are several terms and concepts that are important to understand when working on gesture handling in an application. This page explains the terms pointers, pointer events, and gestures, and introduces the different abstraction levels for gestures. It also dives deeper into event consumption and propagation.

## Definitions

To understand the various concepts on this page, you need to understand some of the terminology used:
- **Pointer**: A physical object you can use to interact with your application. For mobile devices, the most common pointer is your finger interacting with the touchscreen. Alternatively, you could use a stylus to replace your finger. For large screens, you can use a mouse or trackpad to indirectly interact with the display. An input device must be able to "point" at a coordinate to be considered a pointer, so a keyboard, for example, cannot be considered a pointer. In Compose, the pointer type is included in pointer changes using `PointerType`.
- **Pointer event**: Describes a low-level interaction of one or more pointers with the application at a given time. Any pointer interaction, such as putting a finger on the screen or dragging a mouse, would trigger an event. In Compose, all relevant information for such an event is contained in the `PointerEvent` class.
- **Gesture**: A sequence of pointer events that can be interpreted as a single action. For example, a tap gesture can be considered a sequence of a down event followed by an up event. There are common gestures that are used by many apps, such as tap, drag, or transform, but you can also create your own custom gesture when needed.

## Different levels of abstraction

Jetpack Compose provides different levels of abstraction for handling gestures. On the top level is _component support_. Composables like `Button` automatically include gesture support. To add gesture support to custom components, you can add _gesture modifiers_ like `clickable` to arbitrary composables. Finally, if you need a custom gesture, you can use the `pointerInput` modifier.

As a rule, build on the highest level of abstraction that offers the functionality you need. This way, you benefit from the best practices included in the layer. For example, `Button` contains more semantic information, used for accessibility, than `clickable`, which contains more information than a raw `pointerInput` implementation. **Note:** Choosing the right level of abstraction is a common theme in Compose. Read more in the documentation on architectural layering.

### Component support

Many out-of-the-box components in Compose include some sort of internal gesture handling. For example, a `LazyColumn` responds to drag gestures by scrolling its content, a `Button` shows a ripple when you press down on it, and the `SwipeToDismiss` component contains swiping logic to dismiss an element. This type of gesture handling works automatically.

Next to internal gesture handling, many components also require the caller to handle the gesture. For example, a `Button` automatically detects taps and triggers a click event. You pass an `onClick` lambda to the `Button` to react to the gesture. Similarly, you add an `onValueChange` lambda to a `Slider` to react to the user dragging the slider handle.

When it fits your use case, prefer gestures included in components, as they include out-of-the-box support for focus and accessibility, and they are well-tested. For example, a `Button` is marked in a special way so that accessibility services correctly describe it as a button, instead of just any clickable element:

```
// Talkback: "Click me!, Button, double tap to activate"
Button(onClick = { /* TODO */ }) { Text("Click me!") }
// Talkback: "Click me!, double tap to activate"
Box(Modifier.clickable { /* TODO */ }) { Text("Click me!") }
```

To learn more about accessibility in Compose, see Accessibility in Compose.

### Add specific gestures to arbitrary composables with modifiers

You can apply gesture modifiers to any arbitrary composable to make the composable listen to gestures. For example, you can let a generic `Box` handle tap gestures by making it `clickable`, or let a `Column` handle vertical scroll by applying `verticalScroll`.

There are many modifiers to handle different types of gestures:
- Handle taps and presses with the `clickable`, `combinedClickable`, `selectable` , `toggleable`, and `triStateToggleable` modifiers.
- Handle scrolling with the `horizontalScroll`, `verticalScroll`, and more generic `scrollable` modifiers.
- Handle dragging with the `draggable` and `swipeable` modifier.
- Handle multi-touch gestures such as panning, rotating, and zooming, with the `transformable` modifier.

As a rule, prefer out-of-the-box gesture modifiers over custom gesture handling. The modifiers add more functionality on top of the pure pointer event handling. For example, the `clickable` modifier not only adds detection of presses and taps, but also adds semantic information, visual indications on interactions, hovering, focus, and keyboard support. You can check the source code of `clickable` to see how the functionality is being added.

### Add custom gesture to arbitrary composables with `pointerInput` modifier

Not every gesture is implemented with an out-of-the-box gesture modifier. For example, you cannot use a modifier to react to a drag after long-press, a control-click, or a three-finger tap. Instead, you can write your own gesture handler to identify these custom gestures. You can create a gesture handler with the `pointerInput` modifier, which gives you access to the raw pointer events.

The following code listens to raw pointer events:

```
@Composable
private fun LogPointerEvents(filter: PointerEventType? = null) {
var log by remember { mutableStateOf("") }
Column {
Text(log)
Box(
Modifier
.size(100.dp)
.background(Color.Red)
.pointerInput(filter) {
awaitPointerEventScope {
while (true) {
val event = awaitPointerEvent()
// handle pointer event
if (filter == null || event.type == filter) {
log = "${event.type}, ${event.changes.first().position}"
}
}
}
}
)
}
}
```

If you break this snippet up, the core components are:
- The `pointerInput` modifier. You pass it one or more _keys_. When the value of one of those keys changes, the modifier content lambda is re-executed. The sample passes an optional filter to the composable. If the value of that filter changes, the pointer event handler should be re-executed to make sure the right events are logged.
- `awaitPointerEventScope` creates a coroutine scope that can be used to wait for pointer events.
- `awaitPointerEvent` suspends the coroutine until a next pointer event occurs.

Although listening to raw input events is powerful, it is also complex to write a custom gesture based on this raw data. To simplify the creation of custom gestures, many utility methods are available.

#### Detect full gestures

Instead of handling the raw pointer events, you can listen for specific gestures to occur and respond appropriately. The `AwaitPointerEventScope` provides methods for listening for:
- Press, tap, double tap, and long-press: `detectTapGestures`
- Drags: `detectHorizontalDragGestures`, `detectVerticalDragGestures`, `detectDragGestures` and `detectDragGesturesAfterLongPress`
- Transforms: `detectTransformGestures`

These are top-level detectors, so you can't add multiple detectors within one `pointerInput` modifier. The following snippet only detects the taps, not the drags:

```
var log by remember { mutableStateOf("") }
Column {
Text(log)
Box(
Modifier
.size(100.dp)
.background(Color.Red)
.pointerInput(Unit) {
detectTapGestures { log = "Tap!" }
// Never reached
detectDragGestures { _, _ -> log = "Dragging" }
}
)
}
```

Internally, the `detectTapGestures` method blocks the coroutine, and the second detector is never reached. If you need to add more than one gesture listener to a composable, use separate `pointerInput` modifier instances instead:

```
var log by remember { mutableStateOf("") }
Column {
Text(log)
Box(
Modifier
.size(100.dp)
.background(Color.Red)
.pointerInput(Unit) {
detectTapGestures { log = "Tap!" }
}
.pointerInput(Unit) {
// These drag events will correctly be triggered
detectDragGestures { _, _ -> log = "Dragging" }
}
)
}
```

#### Handle events per gesture

By definition, gestures start with a pointer down event. You can use the `awaitEachGesture` helper method instead of the `while(true)` loop that passes through each raw event. The `awaitEachGesture` method restarts the containing block when all pointers have been lifted, indicating the gesture is completed:

```
@Composable
private fun SimpleClickable(onClick: () -> Unit) {
Box(
Modifier
.size(100.dp)
.pointerInput(onClick) {
awaitEachGesture {
awaitFirstDown().also { it.consume() }
val up = waitForUpOrCancellation()
if (up != null) {
up.consume()
onClick()
}
}
}
)
}
```

In practice, you almost always want to use `awaitEachGesture` unless you're responding to pointer events without identifying gestures. An example of this is `hoverable`, which does not respond to pointer down or up events— it just needs to know when a pointer enters or exits its bounds.

#### Wait for a specific event or sub-gesture

There's a set of methods that helps identify common parts of gestures:
- Suspend until a pointer goes down with `awaitFirstDown`, or wait for all pointers to go up with `waitForUpOrCancellation`.
- Create a low-level drag listener using `awaitTouchSlopOrCancellation` and `awaitDragOrCancellation`. The gesture handler first suspends until the pointer reaches the touch slop and then suspends until a first drag event comes through. If you're only interested in drags along a single axis, use `awaitHorizontalTouchSlopOrCancellation` plus `awaitHorizontalDragOrCancellation`, or `awaitVerticalTouchSlopOrCancellation` plus `awaitVerticalDragOrCancellation` instead.
- Suspend until a long press happens with `awaitLongPressOrCancellation`.
- Use the `drag` method to continuously listen to drag events, or `horizontalDrag` or `verticalDrag` to listen to drag events on one axis.

#### Apply calculations for multi-touch events

When a user is performing a multi-touch gesture using more than one pointer, it's complex to understand the required transformation based on the raw values. If the `transformable` modifier or the `detectTransformGestures` methods aren't giving enough fine-grained control for your use case, you can listen to the raw events and apply calculations on those. These helper methods are `calculateCentroid`, `calculateCentroidSize`, `calculatePan`, `calculateRotation`, and `calculateZoom`.

## Event dispatching and hit-testing

Not every pointer event is sent to every `pointerInput` modifier. Event dispatching works as follows:
- Pointer events are dispatched to a _composable hierarchy_. The moment that a new pointer triggers its first pointer event, the system starts hit-testing the "eligible" composables. A composable is considered eligible when it has pointer input handling capabilities. Hit-testing flows from the top of the UI tree to the bottom. A composable is "hit" when the pointer event occurred within the bounds of that composable. This process results in a _chain of composables_ that hit-test positively.
- By default, when there are multiple eligible composables on the same level of the tree, only the composable with the highest z-index is "hit". For example, when you add two overlapping `Button` composables to a `Box`, only the one drawn on top receives any pointer events. You can theoretically override this behavior by creating your own `PointerInputModifierNode` implementation and setting `sharePointerInputWithSiblings` to true.
- Further events for the same pointer are dispatched _to that same chain of composables_, and flow according to event propagation logic. The system does not perform any more hit-testing for this pointer. This means that each composable in the chain receives all events for that pointer, even when those occur outside of the bounds of that composable. Composables that are not in the chain never receive pointer events, even when the pointer is inside of their bounds.

Hover events, triggered by a mouse or stylus hovering, are an exception to the rules defined here. Hover events are sent to any composable that they hit. So when a user hovers a pointer from the bounds of one composable to the next, instead of sending the events to that first composable, events are sent to the new composable.

## Event consumption

When more than one composable has a gesture handler assigned to it, those handlers shouldn't conflict. For example, take a look at this UI:

[IMAGE] List item with an Image, a Column with two texts, and a Button.

When a user taps the bookmark button, the button's `onClick` lambda handles that gesture. When a user taps on any other part of the list item, the `ListItem` handles that gesture and navigates to the article. In terms of pointer input, the Button must _consume_ this event, so that its parent knows not to react to it anymore. Gestures included in out-of-the-box components and the common gesture modifiers include this consumption behavior, but if you are writing your own custom gesture, you must consume events manually. You do this with the `PointerInputChange.consume` method:

```
Modifier.pointerInput(Unit) {

awaitEachGesture {
while (true) {
val event = awaitPointerEvent()
// consume all changes
event.changes.forEach { it.consume() }
}
}
}
```

Consuming an event does not stop the event propagation to other composables. A composable needs to explicitly ignore consumed events instead. When writing custom gestures, you should check if an event was already consumed by another element:

```
Modifier.pointerInput(Unit) {
awaitEachGesture {
while (true) {
val event = awaitPointerEvent()
if (event.changes.any { it.isConsumed }) {
// A pointer is consumed by another gesture handler
} else {
// Handle unconsumed event
}
}
}
}
```

## Event propagation

As mentioned before, pointer changes are passed to each composable that it hits. But if more than one such composable exists, in what order do the events propagate? If you take the example from the last section, this UI translates to the following UI tree, where only the `ListItem` and the `Button` respond to pointer events:

[IMAGE] Tree structure. Top layer is ListItem, second layer has Image, Column, and Button, and the Column splits out into two Texts. ListItem and Button are highlighted.

Pointer events flow through each of these composables three times, during three "passes":
- In the **Initial pass**, the event flows from the top of the UI tree to the bottom. This flow allows a parent to intercept an event before the child can consume it. For example, tooltips need to intercept a long-press instead of passing it on to their children. In our example, `ListItem` receives the event before the `Button`.
- In the **Main pass**, the event flows from the UI tree's leaf nodes up to the root of the UI tree. This phase is where you normally consume gestures, and is the default pass when listening to events. Handling gestures in this pass means that leaf nodes takes precedence over their parents, which is the most logical behavior for most gestures. In our example, the `Button` receives the event before the `ListItem`.
- In the **Final pass**, the event flows one more time from the top of the UI tree to the leaf nodes. This flow allows elements higher in the stack to respond to event consumption by their parent. For example, a button removes its ripple indication when a press turns into a drag of its scrollable parent.

Visually, the event flow can be represented as follows:

Once an input change is consumed, this information is passed from that point in the flow onwards:

In code, you can specify the pass that you're interested in:

```
Modifier.pointerInput(Unit) {
awaitPointerEventScope {
val eventOnInitialPass = awaitPointerEvent(PointerEventPass.Initial)
val eventOnMainPass = awaitPointerEvent(PointerEventPass.Main) // default
val eventOnFinalPass = awaitPointerEvent(PointerEventPass.Final)
}
}
```

In this code snippet, the same identical event is returned by each of these await method calls, although the data about the consumption might have changed.

## Test gestures

In your test methods, you can manually send pointer events using the `performTouchInput` method. This lets you perform either higher-level full gestures (such as pinch or long click) or low level gestures (such as moving the cursor by a certain amount of pixels):

```
composeTestRule.onNodeWithTag("MyList").performTouchInput {
swipeUp()
swipeDown()
click()
}
```

See the `performTouchInput` documentation for more examples.

## Learn more

You can learn more about gestures in Jetpack Compose from the following resources:
- Types of gestures
- `PointerInputChange`

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Accessibility in Compose
- Scroll
- Tap and press

Previous  arrow_back   Overview

Next  Tap and press   arrow_forward
