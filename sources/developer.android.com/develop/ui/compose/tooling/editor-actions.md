# Editor actions
Source: https://developer.android.com/develop/ui/compose/tooling/editor-actions

Android Studio has features inside the editor area to improve your productivity with Jetpack Compose.

## Live templates

Android Studio comes with these Compose-related live templates, which allow you to enter code snippets for fast insertion by typing the corresponding template abbreviation:
- `comp` to set up a `@Composable` function
- `prev` to create a `@Preview` composable function
- `paddp` to add a `padding` Modifier in dp
- `weight` to add a `weight` Modifier
- `W`, `WR`, `WC` to surround the current composable with a `Box`, `Row`, or `Column` container

## Gutter icons

Gutter icons are contextual actions visible on the sidebar, next to the line numbers. Android Studio introduces several gutter icons specific to Jetpack Compose to ease your developer experience.

### Deploy preview

You can deploy a `@Preview` to the emulator or physical device directly from the gutter icon:

[IMAGE] The user clicking a preview function's deploy gutter icon, and deploying the
preview to the
device

### Color picker

Whenever a color is defined inside or outside a composable, its preview is shown on the gutter. You can change the color using the color picker by clicking on it like this:

[IMAGE] The user clicking a color in the gutter, bringing up a color
picker

### Image resource picker

Whenever a drawable, vector, or image is defined inside or outside a composable, its preview is shown on the gutter. You can change it using the image resource picker by clicking on it like this:

[IMAGE] The user clicking an icon in the gutter, bringing up the resource
picker

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Compose layout basics
- Compose modifiers
- Lists and grids

Previous  arrow_back   Develop code iteratively

Next  Lint   arrow_forward
