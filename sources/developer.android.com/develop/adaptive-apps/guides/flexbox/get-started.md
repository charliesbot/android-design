# Get started with FlexBox
Source: https://developer.android.com/develop/adaptive-apps/guides/flexbox/get-started

This page describes how to implement basic `FlexBox` layouts.

## Set up project
- Add the `androidx.compose.foundation.layout` library to your project's `lib.versions.toml`.

```
[versions]
compose = "1.14.0-alpha01"

[libraries]
androidx-compose-foundation-layout = { group = "androidx.compose.foundation", name = "foundation-layout", version.ref = "compose" }

```

- Add the library dependency to your app's `build.gradle.kts`.

```
dependencies {
implementation(libs.androidx.compose.foundation.layout)
}

```

**Note:** `FlexBox` is stable starting in Compose 1.13 (`foundation-layout` 1.13.0). If your project uses the Compose BOM version `2026.11.01` or later, you can omit the explicit `foundation-layout` version.

## Create basic FlexBox layouts

**Example 1**: `FlexBox` lays out two `Text` elements that are centrally aligned.

```
FlexBox(
config = {
direction(FlexDirection.Column)
alignItems(FlexAlignItems.Center)
}
) {
Text(text = "Hello", fontSize = 48.sp)
Text(text = "World!", fontSize = 48.sp)
}
```

[IMAGE] Hello World text composables stacked on top of each other in a basic FlexBox implementation.

**Example 2**: `FlexBox` wraps five items onto two rows and grows them unequally to fill the available space on each row. There is an `8.dp` gap, both vertically and horizontally, between the items.

```
FlexBox(
config = {
wrap(FlexWrap.Wrap)
gap(8.dp)
}
) {
// All boxes have an intrinsic width of 100.dp
// Some grow to fill any remaining space on the row.
RedRoundedBox()
BlueRoundedBox()
GreenRoundedBox(modifier = Modifier.flex { grow(1.0f) })
OrangeRoundedBox(modifier = Modifier.flex { grow(1.0f) })
PinkRoundedBox(modifier = Modifier.flex { grow(1.0f) })
}
```

[IMAGE] Two rows of colored items, with three unequally sized items distributed across the top row and two unequally sized items across the bottom row.

To learn more about `FlexBox` behavior, see Set container behavior and Set item behavior.
