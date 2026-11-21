# Create a parallax scrolling effect
Source: https://developer.android.com/develop/ui/compose/quick-guides/content/parallax-scrolling

Parallax scrolling is a technique in which the background content and foreground content scroll at different speeds. You can implement this technique to enhance your app's UI, creating a more dynamic experience as your users scroll.

## Results

**Figure 1.** A scrolling list with a parallax effect.

## Version compatibility

This implementation requires that your project minSDK be set to API level 21 or higher.

### Dependencies

## Create a parallax effect

To achieve the parallax effect, you apply a fraction of the scrolling value from the scrolling composable to the composable that needs the parallax effect. The following snippet takes two nested visual elements—an image and a block of text—and scrolls them in the same direction at different speeds:

```
@Composable
fun ParallaxEffect() {
fun Modifier.parallaxLayoutModifier(scrollState: ScrollState, rate: Int) =
layout { measurable, constraints ->
val placeable = measurable.measure(constraints)
val height = if (rate > 0) scrollState.value / rate else scrollState.value
layout(placeable.width, placeable.height) {
placeable.place(0, height)
}
}

val scrollState = rememberScrollState()
Column(
modifier = Modifier
.fillMaxWidth()
.verticalScroll(scrollState),
) {

Image(
painterResource(id = R.drawable.cupcake),
contentDescription = "Android logo",
contentScale = ContentScale.Fit,
// Reduce scrolling rate by half.
modifier = Modifier.parallaxLayoutModifier(scrollState, 2)
)

Text(
text = stringResource(R.string.detail_placeholder),
modifier = Modifier
.background(Color.White)
.padding(horizontal = 8.dp),

)
}
}
```

### Key points about the code
- Creates a custom `layout` modifier to adjust the rate by which the composable scrolls.
- The `Image` scrolls at a slower rate than the `Text`, producing a parallax effect as the two composables translate vertically at different rates.

## Collections that contain this guide

This guide is part of these curated Quick Guide collections that cover broader Android development goals:

###### Display a list or grid
Lists and grids allow your app to display collections in a visually pleasing form that's easy for users to consume.
Quick guide collection

###### Display images
Discover techniques for using bright, engaging visuals to give your Android app a beautiful look and feel.
Quick guide collection

###### Display text
Text is a central piece of any UI. Find out different ways you can present text in your app to provide a delightful user experience.
Quick guide collection
