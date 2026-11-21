# Icons
Source: https://developer.android.com/develop/ui/compose/graphics/images/material

The `Icon` composable is a convenient way to draw a single color icon on screen that follows Material Design guidelines. To use `Icon`, include the Compose Material library (or the Compose Material 3 library).

For example, if you had a vector drawable that you wanted to load up with Material defaults, you can use the `Icon` composable as follows:

```
Icon(
painter = painterResource(R.drawable.baseline_directions_bus_24),
contentDescription = stringResource(id = R.string.bus_content_description)
)
```

By default, the `Icon` composable is tinted with `LocalContentColor.current` and is `24.dp` in size. It also exposes a `tint` color parameter (which leverages the same mechanism for tinting as described in the Image tint section). The `Icon` composable is intended for use for small icon elements. You should use the `Image` composable for more customization options. **Note:** With Material there are two different styles of icons, Material Symbols (New) and Material Icons (`material-icons`). The Material Icon library includes a set of predefined `Icons` that can be used in Compose without needing to import an SVG manually. However, this artifact is no longer maintained or recommended for use in your apps, as it contains an older look and feel and can also increase the build time of your apps _significantly_. Instead, we recommend using Google Font Icons and download the XML file from the Android Tab to create an up-to-date Material Symbols style Icon.

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Resources in Compose
- Accessibility in Compose
- Loading images {:#loading-images}

Previous  arrow_back   ImageBitmap vs ImageVector

Next  Customize an image   arrow_forward
