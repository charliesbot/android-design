# Pointer input in Compose
Source: https://developer.android.com/develop/ui/compose/touch-input/pointer-input

Compose provides a variety of APIs to help you detect gestures that are generated from user interactions. The APIs cover a wide range of use cases:
- Some of them are **high-level** and designed to cover the most commonly used gestures. For example, the `clickable` modifier allows easy detection of a click, and it also provides accessibility features and displays visual indicators when tapped (such as ripples).
- There are also less commonly used gesture detectors that offer more flexibility on a **lower level**, like `PointerInputScope.detectTapGestures` or `PointerInputScope.detectDragGestures` but don't include the extra features.

Learn more about pointer input on the following pages:
- Understand gestures gives an explanation of the core concepts playing a role when handling pointer input.
- Tap and press expands on single pointer, single position events.
- Scroll explains how to implement scrolling containers, and handles interoperability concerns.
- Drag composables explains how to move UI elements along a single axis, across two dimensions, or between predefined anchor points using modifiers like `draggable`, `draggable2D`, and `anchoredDraggable`.
- Multi-touch dives into situations where more than one pointer is used.

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Enable user interactions
- Semantics in Compose
- Compose modifiers

Next  Understand gestures   arrow_forward
