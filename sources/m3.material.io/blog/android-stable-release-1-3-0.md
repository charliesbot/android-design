# Material Design Components for Android 1.3.0
Source: https://m3.material.io/blog/android-stable-release-1-3-0

> ProgressIndicator, MaterialTimePicker, i18n / l10n, and more

Published: 2021-02-18

We’re excited to announce the release of Material Design Components (MDC) 1.3.0! A host of exciting new features have been added along with many bug fixes and accessibility improvements. Get the rundown below.

Be sure to check out the release notes. If you’re using MDC for the first time, also take a look at our getting started guide.

## What’s new in 1.3.0?

A fair bit has changed since we launched 1.2.1 in September last year – we added the progress indicator and time picker components, made extensive internationalization and localization string updates, and more. The things you loved from the alpha, beta, and RC releases of 1.3.0 are now officially stable. If you haven’t yet started using 1.3.0 of MDC, there’s never been a better time to update.

## ProgressIndicator

Progress indicators inform users about the status of ongoing processes, such as loading an app, submitting a form, or saving updates. They communicate an app’s state and indicate available actions, such as whether users can navigate away from the current screen.

MDC 1.3.0 allows you to use progress indicators in your Android app with the `LinearProgressIndicator`and `CircularProgressIndicator` widgets. They’re similar to `ProgressBar` but have additional features and support Material Theming.

Learn more about how to implement progress indicators in our documentation.

## MaterialTimePicker

Time pickers allow users to enter a specific time value. They can be used for a wide range of scenarios such as setting an alarm or scheduling a meeting. Mobile time pickers are displayed in dialogs and can be used to select hours, minutes, and a period of time.

MDC 1.3.0 allows you to use time pickers in your Android app with the `MaterialTimePicker` class, which extends `DialogFragment`. It’s similar to `TimePicker` but has additional features and input methods, and supports Material Theming.

_**Note:** MaterialTimePicker can be used with the Navigation component using the __`&lt;dialog&gt;` tag. Learn more about navigating to dialog destinations here._

Learn more about how to implement time pickers in our documentation.

## Internationalization and localization

Certain MDC components include strings as part of their anatomy—think of the “Choose a date” title and “OK” button in date pickers. Additionally there might be built-in strings for accessibility so that screen readers can announce actions and content descriptions.

As part of MDC 1.3.0 these strings have been translated for all languages supported by Android! The affected components include date picker, time picker, bottom sheet, text field, chips, badges, and dialogs.

## BadgeUtils

The `BadgeUtils` utility class was made public in MDC 1.3.0. It gives you a variety of useful static methods to use when dealing with badges in custom `View`s. New methods were also added for adding badges to `Toolbar` menu items.

_**Note:** `BadgeUtils` is considered experimental and the APIs are subject to change. You’ll need to use the `@ExperimentalBadgeUtils` annotation when accessing this class._

Learn more about how to implement badges in our documentation.

## Material Motion updates

MDC transitions received a number of updates to improve stability as well as compatibility with all Android API levels. In particular there are a couple of notable changes for `MaterialContainerTransform`.

We now recommend specifying a target for `View`-to-`View` container transform transitions to ensure they only run on a single `View`. We’ve added this to our documentation.

An exciting addition is that `MaterialContainerTransform` now supports additional interpolators—`OvershootInterpolator`, `AnticipateOvershootInterpolator`, `BounceInterpolator`, and any subclasses.

Learn more about how to implement motion in our documentation.

## … and more

A variety of smaller features and bug fixes made it into this release. Check out the releases page as well as the diff between 1.2.1 and 1.3.0 for a full list.

Here are some of the highlights:
- Show/hide `Slider` tick marks in discrete mode
- Shape theming in `NavigationView`
- A getter method to check if an `AppBarLayout` is lifted
- Elevation overlay support in `Chip`s
- Initial show animation in `ExtendedFloatingActionButton`
- Flipped password icon behavior in `TextInputLayout`
- Updated `BottomNavigationView` menu item selection animation to match spec
- Updated `Snackbar` dimensions to match spec
- Support for content padding in `ShapeableImageView`
- Better support for system gesture nav insets in `BottomSheetBehavior`

## What’s next for MDC?

The next feature release of MDC—1.4.0—is expected to land in alpha soon! Exciting new updates include `NavigationRailView`, motion theming, and various improvements to existing components. As always, we encourage you to file bug reports and feature requests on GitHub. Also be sure to check out our Android example apps and Build a Material Theme.

We highly encourage trying out MDC 1.3.0. If you’re using its features in your Android app, reach out to us on Twitter @materialdesign. We’d love to see it.

[IMAGE] GIF of linear and circular progress indicators: 

[IMAGE] Illustration of a Material time picker:
