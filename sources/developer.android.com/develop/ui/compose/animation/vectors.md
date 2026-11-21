# Animated vector images in Compose
Source: https://developer.android.com/develop/ui/compose/animation/vectors

Animating vectors in Compose is possible in a few different ways. You can use any of the following:
- `AnimatedVectorDrawable` file format
- `ImageVector` with Compose animation APIs, like in this Medium article
- A third-party solution like Lottie

## Animated vector drawables
[IMAGE] Hourglass animating its contents and rotating

**Figure 1.** Animated vector drawable in Compose

To use an `AnimatedVectorDrawable` resource, load up the drawable file using `animatedVectorResource` and pass in a `boolean` to switch between the start and end state of your drawable, performing the animation.

```
@Composable
fun AnimatedVectorDrawable() {
val image = AnimatedImageVector.animatedVectorResource(R.drawable.ic_hourglass_animated)
var atEnd by remember { mutableStateOf(false) }
Image(
painter = rememberAnimatedVectorPainter(image, atEnd),
contentDescription = "Timer",
modifier = Modifier.clickable {
atEnd = !atEnd
},
contentScale = ContentScale.Crop
)
}
```

For more information about the format of your drawable file, see Animate drawable graphics.

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Loading images

Previous  arrow_back   Value-based animations

Next  Customize animations   arrow_forward
