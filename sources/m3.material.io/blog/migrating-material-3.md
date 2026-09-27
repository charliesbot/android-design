# Migrating to Material Design 3
Source: https://m3.material.io/blog/migrating-material-3

> Learn how to migrate to our newest updates

Published: 2021-10-27

At Google I/O, we announced Material You, highlighting the most expressive personalization features in our latest iteration of the Material Design system, Material Design 3. Along with support for dynamic color, we’ve also updated several Material Components.

Ready to migrate to Material Design 3? Let’s get started!

## Migrating from M2 to M3 (MDC 1.4.0 to MDC 1.5.0)

#### Updating your theme to Material 3

Material Design 3 has an expanded color system that takes advantage of features such as dynamic color.

If you use Material Theme Builder to reconfigure your palettes, you can simply drop in the Android XML or Compose theme code. Otherwise, replace `Theme.MaterialComponents.*` with `Theme.Material3.*` for themes and `Widget.MaterialComponents.*` with `Widget.Material3.*` for widget styles.

## Colors and Themes

Themes in Material You expand the color space of its predecessors, adding a Tertiary color and a number of interpolated colors derived from the Primary, Secondary, Tertiary, and Error colors.

Full version:

|

**MDC 1.4.0**   |

**Material 3**
|

android:backgroundColor   |

android:backgroundColor
|

colorPrimary   |

colorPrimary
|

colorOnPrimary   |

colorOnPrimary
|    |

colorPrimaryContainer
|    |

colorOnPrimaryContainer
|    |

colorPrimaryInverse
|

colorPrimaryVariant   |

DEPRECATED
|

colorPrimarySurface   |

colorSurface
|

colorOnPrimarySurface   |

colorOnSurface
|

colorSecondary   |

colorSecondary
|

colorOnSecondary   |

colorOnSecondary
|    |

colorSecondaryContainer
|    |

colorOnSecondaryContainer
|

colorSecondaryVariant   |

DEPRECATED
|    |

colorTertiary
|    |

colorOnTertiary
|    |

colorTertiaryContainer
|    |

colorOnTertiaryContainer
|

colorError   |

colorError
|

colorOnError   |

colorOnError
|    |

colorErrorContainer
|    |

colorOnErrorContainer
|

colorSurface   |

colorSurface
|

colorOnSurface   |

colorOnSurface
|    |

colorSurfaceVariant
|    |

colorOnSurfaceVariant
|    |

colorSurfaceInverse
|    |

colorOnSurfaceInverse
|    |

colorOutline

These new color spaces enable dynamic color expression. We’ve made it easy to visualize dynamic color and migrate to our new color system with the Material Theme Builder, available on the web and Figma. When generated via our tooling, these intermediate color values can be created by providing as little as one color.

## How it works

When a user changes their wallpaper on an Android 12 device, the image is analyzed to select a color and algorithmically choose Primary, Secondary, Tertiary, and Error colors using an initial seed color. At the same time it’s applying color theory and accessibility rules. From these colors, the algorithm creates tonal palettes ranging from 0% luminance (BLACK) to 100% (WHITE). Dynamic theming or custom theming created through our tooling will use values in these tonal palettes to set the theme attributes for that color range.

For example, if the On Primary color, for text/icons shown on the Primary color is a 100 tone, then On Secondary, On Tertiary, and On Error will all be 100 tones in their respective colors for the light theme and perhaps 20 tone for the dark theme.

You are free to customize any of these values but be mindful of contrast and accessibility.

On Android 12 devices, Material You will generate dynamic palettes based on the user's wallpaper. In addition to the generated tonal palettes, there will be several pre-created schemes. To opt into dynamic colors, you need to make sure there aren't any hardcoded colors in your app. Instead, all colors in your app should be referenced as theme attributes, that way they can be easily swapped when enabling dynamic wallpaper color support.

## Integrating dynamic color

The `DynamicColors` class from the Material library taps into Activity Lifecycle Callbacks to determine when and how to apply color overlays. Using the supplied API calls, you can apply dynamic color to a subset or the entire app. You can also determine when and where dynamic color should be applied.

To avoid unintended effects, you should make sure your application code references theme attributes and not any hard coded colors (HEX codes or @color/).

Given an app that uses a Material3 theme with MDC components, below is the minimum code you’d need to enable dynamic color - a line in the `onCreate` method of your Application class.

You can read more about our color theming. Follow along in our Applying Dynamic Color codelab to learn how to apply dynamic color in your app.

## Typography

Typography styles have undergone a simplification. Before Material 3, there were 6 Headline variations, 2 Subtitle variations, 2 Body variations, Button, Caption, and Overline styles.

In Material 3, there is a more regular and smaller number of variants for each classification, namely Small, Medium, and Large.

Most of the new `TextAppearance` styles map directly to pre-Material 3 styles. The property names in MDC-Android are formed using a similar pattern: `textAppearance` + {Display, Headline, Title, Body, Label} + {Large, Medium, Small}

|

**New Property**   |

**Old Property**
|

textAppearanceDisplayLarge   |

textAppearanceDisplay2
|

textAppearanceDisplayMedium   |

textAppearanceDisplay3
|

textAppearanceDisplaySmall   |

textAppearanceHeadline1
|

textAppearanceHeadlineLarge   |

textAppearanceHeadline2
|

textAppearanceHeadlineMedium   |

textAppearanceHeadline3
|

textAppearanceHeadlineSmall   |

textAppearanceHeadline4
|

textAppearanceTitleLarge   |

textAppearanceHeadline5
|

textAppearanceTitleMedium   |

textAppearanceSubhead1/Subtitle1
|

textAppearanceTitleSmall   |

textAppearanceSubhead2/Subtitle2
|

textAppearanceBodyLarge   |

textAppearanceBody1
|

textAppearanceBodyMedium   |

textAppearanceBody2
|

textAppearanceBodySmall   |

textAppearanceCaption
|

textAppearanceLabelLarge   |

textAppearanceButton
|

textAppearanceLabelMedium   |

textAppearanceOverline
|

textAppearanceLabelSmall   |

N/A

Read more about Typography.

## Component Updates

#### Top App Bar

Top app bars have undergone a Material You refresh to better align with color. Before Material You, top app bars used shadow to differentiate between the app bar and content. Color is now the primary indicator of elevation using newly created surface tones.

Read about implementing Top App Bar with more examples and code snippets.

#### Navigation Bar / Bottom Navigation View

Bottom Navigation View has undergone a visual refresh and gained some new functionality. Currently each menu item in a Bottom Navigation View is composed of an icon and text label. When a destination is selected, both the icon and label will be tinted a different color from the inactive state.

In Material Design 3, inactive destinations are indicated by an outlined version of the icon if available. Active destinations are indicated by a filled icon enclosed in a pill shaped container.

If distinct filled and outlined variants of an icon are not available, you may want to consider other cues to help indicate active state such as showing destination labels only on active destinations. Read about implementing Navigation Bar with more examples and code snippets.

#### Floating Action Button

`FloatingActionButton` has undergone a bit of a redesign. FABs with no explicit theme or a legacy `Widget.Design.FloatingActionButton` theme will inherit this new design in a `Theme.Material3.*` theme.

The new FAB has less corner radius and now resembles a rounded rectangle versus the circular shape of existing FABs. There are preset styles to theme your FAB using tones from the Primary color, Secondary color, or surface color. To retain the former design for your Floating Action Buttons, make sure to set your FAB’s style as `@style/Widget.MaterialComponents.FloatingActionButton`. There is also a new large FAB variant. Read about implementing FABs with more examples and code snippets.

#### Buttons

Filled and Outlined Buttons in Material Design 3 have fully rounded corners. Button widgets will auto-inflate to Material Button and apply a Material3 style if enclosed in a theme deriving from `Theme.Material3.*`. Read about implementing Buttons with more examples and code snippets.

#### Chips

In Material 2, Chips had fully rounded corners, in Material 3 they have undergone a visual change and now have 8 dp corners. They all have 8 dp corners in Material 3. Read about implementing Chips with more examples and code snippets.

#### Badges

Bottom Navigation View is now able to display an associated badge either as a filled shape or with a label. A badge can be anchored to one of the four corners of the icon by setting its gravity to one of the constants declared in `Badge Drawable.BadgeGravity `:

`TOP_START`

`TOP_END`

`BOTTOM_START`

`BOTTOM_END`

Here is some code showing how to add badges to BottomNavigationView menu items:

Read about implementing BadgeDrawable with more examples and code snippets.

## What’s next?

We’ve gone through the major differences between MDC 1.4.0 and the 1.5.0 (Material Design 3) release.

We encourage you to upgrade your application’s dependencies to understand and explore the new options that Material Design 3 provides to enhance your user experience using color.

As always, we encourage you to file bug reports and feature requests on GitHub.

[IMAGE] Light theme color values: Light theme color values

[IMAGE] Autogenerated color palette from wallpaper selection with purple background: Autogenerated color palette extracted from wallpaper selection

[IMAGE] Autogenerated basic colors extracted from wallpaper selection with light green background: Autogenerated basic colors extracted from wallpaper

[IMAGE] M2 Typescale showing values for Headline 1 through Headline 6: M2 typescale

[IMAGE] M2 Typescale showing values for Headline 4 through Overline: 

[IMAGE] Material Design 3 typescale from Display to Title: M3 typescale

[IMAGE] Material Design 3 typescale from Headline to Label: 

[IMAGE] M2 example of top app bar: M2 example of top app bar

[IMAGE] M3 example of top app bar: M3 example of top app bar

[IMAGE] M2 example of bottom navigation: M2 example of bottom navigation

[IMAGE] M23example of bottom navigation: M3 example of bottom navigation

[IMAGE] M2 examples of Floating action buttons (FABs): M2 examples of Floating action buttons (FABs)

[IMAGE] M3 examples of Floating action buttons (FABs): M3 examples of Floating action buttons (FABs)

[IMAGE] M2 examples of buttons: M2 examples of buttons

[IMAGE] M3 examples of buttons in purple light theme: M3 examples of buttons

[IMAGE] M2 examples of chips: M2 examples of chips

[IMAGE] M3 examples of chips: M3 examples of chips
