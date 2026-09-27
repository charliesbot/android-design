# Material Design Components for Android 1.10.0
Source: https://m3.material.io/blog/android-stable-release-1-10-0

> Updates, refinements and a new way to transition between destinations

Published: 2023-10-25

We’re happy to announce the release of Material Design Components for Android (MDC-Android) 1.10.0. It offers a mix of functional changes and updates so make sure to check the release notes for a complete listing.

## (In-App) Predictive Back

Predictive back allows a user to see where the swipe gesture will take them before they execute the action, usually by scaling the view to reveal the destination. At any time during the gesture, the user can cancel and remain at the current destination.

Starting with Android 14/API level 34, Android supports predictive back inside your app between destinations in addition to the predictive back-to-home introduced in Android 13/API 33.

The general steps to opt-in to predictive back are:
- Implementing the “back callback” APIs (`OnBackAnimationCallback`, `OnBackPressedCallback`, `OnBackPressedDispatcher`, etc)
- Setting the `android:enableOnBackInvokedCallback` manifest flag to true
- Moving away from legacy back navigation (`Activity#onBackPressed`, `KeyEvent.KEYCODE_BACK`, etc)

Once your app is migrated, destinations involving supported components will get predictive back navigation gestures and animations for free.

At the time of writing, they are :
- Search Bar / SearchView
- Bottom sheet
- Side sheet
- Navigation drawer / NavigationView

To follow a detailed guide to implementing predictive back in your apps, please check out the documentation on developers.android.com.

Our MDC Predictive Back documentation page is the best place to learn about the growing number of components that support predictive back and future slated components.

## Updates and Changes

Starting with this release the minimum `compileSdkVersion` is 34.

**Side Sheet**

Side sheets, first mentioned in the blog post announcing version 1.8.0, contain supplementary information related to a screen. They were previously anchored only to the right side of the screen. Starting with version 1.10.0, they may be anchored to either side of the screen. To learn more about the different side sheet types, refer to that blog post or the documentation page for side sheet.

**Changes to Carousel**

Carousels, introduced in the 1.9.0 announcement post, have a new variant (`HeroCarouselStrategy`) that aligns content to the start and uses a “hero” strategy, allocating most of the space in the carousel to one main element.

**BadgeDrawable**

BadgeDrawables, often used to annotate items in navigation components with text such as a new/unread message count or unlabeled shape, have undergone a small tweak.

Previously, you could align the badge to any of the four corners of the anchor view. That has been reduced to two alignments: `TOP_START` and `TOP_END` (default).

`BOTTOM_START` and `BOTTOM_END` are deprecated and usage is discouraged. To read more about the changes, please see the BadgeDrawable developer documentation.

## What’s next for MDC?

We’re hard at work on the 1.11.0 release with some clarifying updates to how we use color in components. The components or features we highlight in these posts are only a fraction of the work that lands in each release. Check out the release notes for a full listing.

You can follow the progress of new versions, file bug reports and submit feature requests on GitHub. Check out the catalog app to see the components in action. Also feel free to reach out to us on Twitter @materialdesign.

[IMAGE] carousel in the material catalog app: 

[IMAGE] Bottom nav bar with badges: TOP_START Alignment

[IMAGE] Bottom nav bar with badges: TOP_END Alignment (Default)

[VIDEO]
