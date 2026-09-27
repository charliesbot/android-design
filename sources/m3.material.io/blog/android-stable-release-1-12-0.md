# Material Design Components for Android 1.11.0 and 1.12.0
Source: https://m3.material.io/blog/android-stable-release-1-12-0

> New color options and carousel refinements

Published: 2024-05-08

Material Design Components versions 1.11 and 1.12 come on the eve of our yearly developer conference Google I/O and offer a number of new options for Carousel control, an expansion to color theming, and non-text contrast updates for progress indicators and sliders. The minimum SDK version for MDC Views raises to API 19 in version 1.12, similar to the rest of AndroidX. 1.12 also updates its dependency on androidx.transition (used in SharedElement transitions, Fade, and Predictive Back animations) to version 1.5.0 from 1.2.0.

## Carousel Updates

In the last post we announced a new Carousel strategy, HeroCarouselStrategy, that can be used to showcase large, more immersive content. In MDC 1.11, we’ve added two new strategies - UncontainedCarouselStrategy and FullscreenCarouselStrategy. Additionally, we’ve added the option to center-align any carousel so large, focal items can be placed in the center of the scroll container.

`UncontainedCarouselStrategy` doesn’t alter the aspect ratio of the carousel items. This is a useful strategy when altering an item’s size could detract from its meaning - for example, a carousel of album covers might benefit from retaining a recognizable 1:1 aspect ratio.

`FullScreenCarouselStrategy` allows a single item to take all the available space in a container. This is useful for fully immersive content like video or slideshows where items are interacted with, or viewed, one-at-a-time. We only recommend using this strategy in the vertical orientation.

**Item Snapping**

`CarouselSnapHelper` allows your carousels to give focus to the nearest item. They can be used on all strategies but are strongly encouraged on hero carousels and full-screen carousels.

## Color Changes

API 34 has an expanded color set giving you more ways to personalize your apps. Some of the additions like the new surface options have been incorporated in components. Fixed and Dim colors on the other hand are developer options but not used by the component set at time of writing.

**System UI access to named color roles**

Before API 34, when you used dynamic color, the color was stored as individual tones of the color palettes. MDC would map those colors to dynamic color properties and then assign those properties to color roles.

As of API 34, the traversal is more direct and compact. Material color roles directly map to color resources from `@android:color` namespace.

**Fixed and Fixed Dim**

Fixed and Fixed Dim color roles are new additions to the color spec. Fixed color roles do not change between light and dark themes. Dim color roles offer a less emphasized option to the associated color role. Neither of these categories of color roles are used in components but are available to further customize your UIs.

**Surfaces**

Previously in Material themes, we provided a single surface color value and calculated interpolated variants of it at runtime by blending with the primary color (based on elevation). This was frustrating for developers and designers when it came to implementing UIs because they were unable to assign those colors directly.

Color roles simulating different elevation levels have been added to theming. There are now three surface color options and five surface container options.

Surface Bright is guaranteed to be the lightest surface color.

Surface is largely unchanged from previous versions.

Surface Dim is equivalent or darker than its relative Surface color role.

Surface Container Lowest, Low, High and Highest are additional color roles that can provide more or less emphasis in contrast to Surface Container.

These components use some of the new surface container color roles:

Elevated Button

Bottom App Bar

Navigation Bar

Bottom Sheet

Modal Side Sheet

Elevated Card

Elevated Chips

Datepicker

Dialogs

Surface FAB/ Surface exFAB

Menu

Navigation Drawer

Search

Switch

Text Fields

Timepicker

Top App Bar

The new color roles are already available in Material Theme Builder.

**Non-Text Contrast Updates (NTC) for Progress Indicators and Sliders**

Non-text contrast standards involve first evaluating which parts of controls are essential to demark their state, interactivity options, and legibility and secondly making changes to improve the identified attributes and making sure those portions have a minimum contrast ratio (3:1 in standard contrast at time of writing). Progress Indicators and Sliders have been updated to implement these standards.

**Progress indicators **

The NTC changes to progress indicators include a clearly identified gap between the active and inactive tracks for linear progress indicators. In circular progress indicators, the pre-1.12 version does not by default include a track making it more difficult to estimate its value. An inactive track is now drawn to complete the full circle with gaps on both ends where the track meets the active indicator.

A number of these options such as the size of the gap size and track attributes are addressable in XML code. You can see the full listing here.

**Sliders**

Sliders have undergone similar changes to progress indicators. There are now gaps between the active and inactive tracks.The default thumb for sliders has also changed from a connected filled circle to a vertical bar with configurable gaps on each side. The shape for formatted labels have also undergone a few tweaks to assist with NTC.

As is the case with progress indicators, you can configure these attributes inXML code. You can see the full listing here.

## What’s next for MDC?

We’re hard at work on the 1.13.0 release. The components or features we highlight in these posts are only a fraction of the work that lands in each release. Check out the release notes for a full listing.

You can follow the progress of new versions, file bug reports and submit feature requests on GitHub. Check out the catalog app to see the components in action. Also feel free to reach out to us on Twitter @materialdesign.

[IMAGE] carousel component: Start-aligned HeroCarouselStrategy

[IMAGE] Carousel component: Center-aligned HeroCarouselStrategy

[IMAGE] uncontained carousel: 

[IMAGE] color diagram: 

[IMAGE] section of color scheme: 

[IMAGE] surface color scheme: 

[IMAGE] Progress indicator settings in the catalog app: MDC 1.11 version of Progress Indicators

[IMAGE] Progress indicator settings in the catalog app: Basic Progress Indicator Options

[IMAGE] Progress indicator settings in the catalog app: Advanced Progress Indicator Options

[IMAGE] customized progress indicators in catalog app: 

[IMAGE] sliders in catalog app: MDC 1.11 version of Sliders

[IMAGE] sliders in catalog app: Basic Slider Options

[IMAGE] sliders in catalog app: Advanced Slider Options
