# Loading images
Source: https://developer.android.com/develop/ui/compose/graphics/images/loading

## Load an image from the disk

Use the `Image` composable to display a graphic on screen. To load an image (for example: PNG, JPEG, WEBP) or vector resource from the disk, use the `painterResource` API with your image reference. You don't need to know the type of the asset, just use `painterResource` in `Image` or `paint` modifiers.

`DrawScope`:

```
Image(
painter = painterResource(id = R.drawable.dog),
contentDescription = stringResource(id = R.string.dog_content_description)
)
```

To ensure that your app is accessible, supply a `contentDescription` for visual elements on screen. TalkBack reads out the content description, so you must ensure that the text is meaningful if read out loud and translated. In the preceding example, a `stringResource()` is used to load up the translated content description from the `strings.xml` file. If your visual element on screen is purely for visual decoration, set your `contentDescription` to `null` for the screen reader to ignore it.

If you need lower-level `ImageBitmap` specific functionality, you can use `ImageBitmap.imageResource()` to load up a Bitmap. For more information on ImageBitmaps, read the ImageBitmap versus ImageVector section.

### Drawable support

`painterResource` supports the following drawable types:
- `AnimatedVectorDrawable`
- `BitmapDrawable` (PNG, JPG, WEBP)
- `ColorDrawable`
- `VectorDrawable`

## Load an image from the internet

To load an image from the internet, there are several third-party libraries available to help you handle the process. Image loading libraries do a lot of the heavy lifting for you; they handle both caching (so you don't download the image multiple times) and networking logic to download the image and display it on screen.

For example, to load an image with Coil from Instacart, add the library to your gradle file, and use an `AsyncImage` to load an image from a URL:

```
AsyncImage(
model = "https://example.com/image.jpg",
contentDescription = "Translated description of what the image contains"
)
```

###### Coil

An image loading library backed by Kotlin Coroutines (Instacart).

[IMAGE] Maven version for Coil

###### Glide

A fast and efficient image loading library for Android focused on smooth scrolling (Google).

[IMAGE] Maven version for Glide

## Additional resources
- Load and display images

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Resources in Compose
- Accessibility in Compose
- Graphics in Compose

Previous  arrow_back   Overview

Next  ImageBitmap vs ImageVector   arrow_forward
