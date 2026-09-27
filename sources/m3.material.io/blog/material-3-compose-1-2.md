# Material Design 3 for Compose 1.2
Source: https://m3.material.io/blog/material-3-compose-1-2

> Exploring the 1.2 release of Material Design 3 for Compose

Published: 2024-02-07

The 1.2 release of Compose Material 3 is here, and with it comes new components, some component changes and an expansion of the Material3 color system.

## Component Changes, Demotions and Promotions

`Segmented Button` is a new experimental component. There are single select and multiple selection variants.

`BottomAppBar` has a `BottomAppBarScrollBehavior` to auto-hide itself when content is scrolled.

`SwipeToDismiss` has been refactored into `SwipeDismissBox` and remains in experimental status.

`Badge` and `BadgedBox` have been promoted to stable.

The `Chip` APIs have been promoted to stable.

## Color Changes

Material3 Compose 1.2 has an expanded color set giving you more ways to personalize your apps. Notable additions are more surface options to represent emphasis of information in your UIs. Components have been updated to make fuller use of the new surface values.

A small implementation note is that the ColorScheme object is now immutable allowing Jetpack Compose to skip it and possibly optimize successive compositions when the object hasn’t changed.

If your code currently modifies the colors in ColorScheme directly, you will need to make use of the copy method now to change colors.

#### Surfaces

Previously in Material, we provided a single surface value and calculated interpolated variants of it at runtime by blending with the primary color. This was frustrating for developers and designers when it came to implementing UIs because they were unable to assign those colors directly.

Color roles simulating different elevation levels have been added to theming. There are now three surface color options and five surface container options.

`Surface Bright` is guaranteed to be the lightest surface color. It is not currently in use by any of our provided components.

`Surface` was the former backing color for many components. It has been replaced largely by the five surface container in 1.2. It is still in the spec so your components using it will not break.

`Surface Dim` is guaranteed to be the darkest surface color. Like Surface Bright, they are not in use in any existing components.

`Surface Container Lowest`, `Low`, `High` and `Highest` are additional color roles that can provide more or less emphasis in contrast to Surface Container.

The new color roles are already available in Material Theme Builder.

## Where can I find more information about Material Compose ?

The components or features we highlight in these posts are only a fraction of the work that lands in each release. Check out the release notes for a full listing.

You can file bug reports and follow open issues on Buganizer. You can also follow the progress of new versions on cs.android.com. Check out the catalog app to see the components in action.

[IMAGE] segmented button: 

[IMAGE] segmented button with two items selected: 

[IMAGE] Badge on icon: 

[IMAGE] Chips: 

[IMAGE] Light scheme: 

[IMAGE] Dark scheme: 

[IMAGE] Surface roles from a light color scheme:
