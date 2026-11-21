# Multitouch: Panning, zooming, rotating
Source: https://developer.android.com/develop/ui/compose/touch-input/pointer-input/multi-touch

To detect multitouch gestures used for panning, zooming and rotating, you can use the `transformable` modifier. This modifier does not transform elements by itself, it only detects the gestures.

```
@Composable
private fun TransformableSample() {
// set up all transformation states
var scale by remember { mutableFloatStateOf(1f) }
var rotation by remember { mutableFloatStateOf(0f) }
var offset by remember { mutableStateOf(Offset.Zero) }
val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
scale *= zoomChange
rotation += rotationChange
offset += offsetChange
}
Box(
Modifier
// apply other transformations like rotation and zoom
// on the pizza slice emoji
.graphicsLayer(
scaleX = scale,
scaleY = scale,
rotationZ = rotation,
translationX = offset.x,
translationY = offset.y
)
// add transformable to listen to multitouch transformation events
// after offset
.transformable(state = state)
.background(Color.Blue)
.fillMaxSize()
)
}
```

[IMAGE] A UI element responding to multitouch gestures—panning, zooming, and rotating

If you need to combine zooming, panning and rotation with other gestures, you can use the `PointerInputScope.detectTransformGestures` detector.

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Understand gestures

Previous  arrow_back   Drag composables
