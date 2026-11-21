# Get started
Source: https://developer.android.com/develop/adaptive-apps/guides/grid/get-started

This page describes how to implement basic `Grid` layouts.

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

**Note:** `Grid` is stable starting in Compose 1.13 (`foundation-layout` 1.13.0). If your project uses the Compose BOM version `2026.11.01` or later, you can omit the explicit `foundation-layout` version.

## Create a basic grid

The following example creates a basic 2x3 grid, with the columns and rows having a fixed size of `100.dp`.

```
Grid(
config = {
repeat(2) {
column(100.dp)
}
repeat(3) {
row(100.dp)
}
}
) {
Card1(containerColor = PastelRed)
Card2(containerColor = PastelGreen)
Card3(containerColor = PastelBlue)
Card4(containerColor = PastelPink)
Card5(containerColor = PastelOrange)
Card6(containerColor = PastelYellow)
}
```

[IMAGE] A basic grid consists of rows and columns with fixed size.

**Figure 1**. A basic grid consists of rows and columns with fixed size.

To learn how to implement more advanced grids, see Set container properties and Set item properties.
