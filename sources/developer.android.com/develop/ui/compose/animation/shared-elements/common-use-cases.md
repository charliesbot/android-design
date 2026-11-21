# Common shared element use cases
Source: https://developer.android.com/develop/ui/compose/animation/shared-elements/common-use-cases

When animating shared elements, there are some particular use cases that have specific recommendations.

## Asynchronous images

It's common to use a library to load up an image asynchronously, such as when using Coil's `AsyncImage` composable. For it to work seamlessly between two composables, it is recommended to set the `placeholderMemoryCacheKey()` and `memoryCacheKey()` to the same key as a string derived from the shared element key, so that the cache key is the same for the matched shared elements. The new shared element will be using its match's cache as the placeholder until it loads the new image.

The typical usage for `AsyncImage` is as follows:

```
AsyncImage(
model = ImageRequest.Builder(LocalContext.current)
.data("your-image-url")
.crossfade(true)
.placeholderMemoryCacheKey("image-key") //  same key as shared element key
.memoryCacheKey("image-key") // same key as shared element key
.build(),
placeholder = null,
contentDescription = null,
modifier = Modifier
.size(120.dp)
.sharedBounds(
rememberSharedContentState(
key = "image-key"
),
animatedVisibilityScope = this
)
)
```

## Text

To animate `fontSize` changes, use `Modifier.sharedBounds()` with `resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds()`. This transition makes the size change relatively fluid. The `contentScale` parameter can be tweaked to animate a specific font weight or style.

```
Text(
text = "This is an example of how to share text",
modifier = Modifier
.wrapContentWidth()
.sharedBounds(
rememberSharedContentState(
key = "shared Text"
),
animatedVisibilityScope = this,
enter = fadeIn(),
exit = fadeOut(),
resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds()
)
)
```

`TextAlign` changes are **not** animated by default. Instead, use `Modifier.wrapContentSize()` or `Modifier.wrapContentWidth()` over using different `TextAlign` for shared transitions.

Previous  arrow_back   Customize

Next  With navigation   arrow_forward
