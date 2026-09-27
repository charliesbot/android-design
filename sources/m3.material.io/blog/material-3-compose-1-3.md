# Material Design 3 for Compose version 1.3
Source: https://m3.material.io/blog/material-3-compose-1-3

> Exploring the 1.3 release of Material Design 3 for Compose

Published: 2024-09-10

Version 1.3 of the Compose Material 3 Components is here and with it come a few changes for Material Components and theming!

Version 1.3 introduces:
- Behavior changes for ripple APIs for performance improvements
- A brand-new component: Carousel
- Built-in predictive back support for some Material Components.
- Color token alignment to be more colorful while still having accessible color contrast
- Previously experimental APIs have been stabilized

## Breaking API changes

To improve performance, the Material components have been migrated to use the new Ripple APIs, and no longer query `RippleTheme`. If you have set a custom ripple in your applications, you will need to perform a migration to use the new APIs as Material will no longer query `LocalRippleTheme`. See the “Migrate to Indication and Ripple APIs” documentation for more information about how to migrate your ripple theme.

## Carousel component

Material 1.3 introduces two experimental versions of the Material 3 Carousel - `HorizontalMultiBrowseCarousel` and `HorizontalUncontainedCarousel`.

The `HorizontalMultiBrowseCarousel` shows at least one large, medium, and small carousel item.

The `HorizontalUncontainedCarousel` displays its items at a given size and one item at the end that peeks in to show that there are more items in the list.

Similar to using the `HorizontalPager`, the Carousel applies a few design specifications on top of the `Pager`, such as the parallax effect for item content and how the items on each end should size themselves. An example of using the `HorizontalUncontainedCarousel` can be found below:

As part of the Carousel component, we’ve added two new modifiers to `CarouselItemScope` - maskClip and maskBorder - to easily add a shape and border to any carousel item. This is needed over the regular `clip` and `border` modifiers to be able to apply it on a container level as the item moves inside the Carousel, if you had to apply the standard `clip` modifier, the items would be statically clipped and not apply the parallax effect that this component introduces.

For example, if you wanted to surround your Carousel items with a border, you can do so by placing the `maskBorder` modifier onto the individual item, as well as provide a different shape to the items with `maskClip`:

## Component updates

The following components have been updated to match the Material 3 specification, there are also contrast updates to these components to improve accessibility out of the box.

**Progress Indicator**

The progress indicator components have been updated to a new visual style to match the Material 3 specification:

- `LinearProgessIndicator` is a straight line progress indicator, the visual style has changed to use rounded `strokeCap` by default, this can be changed with the `strokeCap` parameter. New parameter for specifying the `gapSize` between the progress indicator and track is now available. To change or disable the visual indicator at the end - use the `drawStopIndicator` parameter.
- `CircularProgressIndicator` is a circle shaped progress indicator, the visual style has changed to use rounded `strokeCap`, this can be changed with the `strokeCap` parameter. New parameter for specifying the `gapSize` between the progress indicator and track is now available.

**Slider**

The slider component has been updated to a new visual style to match the Material 3 specification, including more prominent handles and an updated shape spec that can be customized:
- Continuous slider: `Slider`

- Discrete slider: `Slider` with `steps` parameter set

- Range slider: `RangeSlider`

**Bottom Sheet**

Bottom sheets have been updated to fix bugs and offer improvements to the API. `ModalBottomSheet` more accurately draws scrim over status bar when edge to edge is enabled.

Modal bottom sheet content can now consume window insets, allowing for visible content above the navigation bar. `ModalBottomSheet` parameter `windowInsets` is renamed to `contentWindowInsets` to specify where the insets will be applied, and the insets are no longer tied to window logic. In addition, the bottom sheet includes predictive back support.

**Pull to refresh API changes**

The pull to refresh experimental APIs have been overhauled and updated to fix a few bugs with the component. The updated APIs for `PullToRefreshBox` simplify the state to use fractional values instead of dp units, and separate out the nested scroll connection from `PullToRefreshState`. A simple example of using pull to refresh:

## Predictive back support

Support for predictive back is now built into some of the Material Components. Predictive back allows a user to swipe left or right on certain components as a way to navigate to a previous destination. Before completing the swipe, the user can decide to continue to the previous view or stay in the current view, where the component will indicate a preview of where going back would take the user.

The following components will now automatically handle predictive back on devices that support it:
- `ModalBottomSheet`
- `SearchBar`
- `ModalDrawerSheet`
- `DismissibleDrawerSheet`

## Color Token Alignment / Tonal Surface Colors

Tone-based surface color roles have replaced the previous “surfaces at +1 to +5 elevation” approach. The new color roles are not tied to elevation, and offer more flexibility and support for color features such as user-controlled contrast. All Material Components will **automatically update to use the new surface container color roles**.

For developers using the previous opacity-based surface model for custom color mappings, we recommend remapping these to the new roles:

Instead of using `ColorScheme.surfaceColorAtElevation()`, use the following colors instead:
- `surfaceContainerLowest` is a new role
- Surface at elevation +1 becomes `surfaceContainerLow`
- Surface at elevation +2 becomes `surfaceContainer`
- Surface at elevation +3 becomes `surfaceContainerHigh`
- Surface at elevation +4 and +5 are being deprecated, it is recommended to use `surfaceContainerHighest` by default as a replacement. As an alternative `surfaceContainerHigh`, or `surfaceDim` can be used depending on the specific use case.
- Surface Variant becomes `surfaceContainerHighest`

For more information on tone-based surface color roles and the exact changes, see the blog post.

In addition to the introduction of tone based surface color roles, the following color roles have been updated in the default `lightColorScheme` to be more colorful while still having accessible color contrast:
- `onPrimaryContainer`
- `onSecondaryContainer`
- `onTertiaryContainer`
- `onErrorContainer`

Affected components:
- Badges
- Bottom app bar
- Buttons
- Common buttons
- Extended FAB
- FAB
- Icon buttons
- Segmented buttons
- Chips
- Lists
- Menus
- Navigation bar
- Navigation drawer
- Navigation rail
- Switches

Promotion of experimental APIs to Stable

The experimental Material components continue to be evaluated and adjusted based on your feedback and bug reports. In this release, we’ve graduated more APIs and removed the experimental annotations from:
- `SegmentedButton` and associated APIs are now stable.
- `SwipeToDismissBox` and associated APIs are now stable.

## Learn more

The components or features that are highlighted in these posts are only a subset of the work that lands in each release. Check out the release notes for a full listing of all the changes.

You can file bug reports and follow open issues on Buganizer. You can also follow the progress of new versions on cs.android.com. Install the catalog app on Google play to see the latest components in action.

[IMAGE] carousel component: 

[IMAGE] carousel component with equal width items: 

[IMAGE] carousel with bordered items: 

[IMAGE] linear and circular progress indicators: 

[IMAGE] continuous slider: 

[IMAGE] discrete slider: 

[IMAGE] range slider: 

[IMAGE] updated color mapping diagram: 

[IMAGE] updated color scheme applied to components: 

[VIDEO] 

[VIDEO] 

[VIDEO] 

[VIDEO]
