# Why We Recommend Material Components for Android
Source: https://m3.material.io/blog/why-we-recommend-material-design-components-android

> Ensuring that patterns learned in one app can be used in another

Published: 2020-08-25

##### _Android users expect your app to look and behave in a way that’s consistent with the platform. You should follow material design guidelines for visual and navigation patterns…
— d.android.com/design_

Since your app exists alongside other apps installed on a user’s phone, it’s recommended that applications follow the Material Design guidelines to ensure that apps operate consistently, and that patterns learned in one app can be used in another.

To implement this, you can use the Material Components for Android (MDC) library. This post outlines the many benefits of using MDC, explaining why it’s our recommended solution.

## Widgets

MDC provides styled versions of standard widgets making it easy to implement Material styling. For example Material offers styled versions of `Button`s, `Toolbar`s, `CheckBox`es and more. If you use a `MaterialComponents` theme, then Material widgets will be instantiated instead of the stock components when you inflate a layout (by MDC’s View Inflater) so it’s easy to achieve Material styling without having to make large updates to your layouts:

All Material widgets extend their relevant `AppCompat` counterparts so they benefit from the same backported functionality or bug fixes.

Material widgets offer extra styling and functionality over their platform or `AppCompat` counterparts, for example `MaterialButton` offers a number of different display styles:

One of my favorite additions is `TextView`s being replaced with `MaterialTextView`s which add support for specifying line heights in `TextAppearance`s. Handy.

As well as adding to existing widgets, MDC offers a number of new widgets, not available in the platform or `AppCompat`. You’ve likely seen Bottom Navigation, Bottom Sheet and Floating Action Buttons but might not have come across Chips, Date Picker or Time Picker.

For a complete list of components offered by the library, see the components section.

## Material Theming

Material Theming is a systematic way to customize Material Design to reflect your product’s brand. A Material Theme comprises color, typography and shape attributes. Customizing these will be automatically reflected in the components you use to build your app.

You can think of Material Theming as a design system for creating design systems 🤯. You configure the color, type and shape inputs and get out a complete design system for your brand.

Nick Rout goes in depth into each of the three sub-systems in the following posts:

Building a Material Theme on Android: Color
Building a Material Theme on Android: Typography
Building a Material Theme on Android: Shape

## Dark Theme

MDC widgets implement Material’s dark theme guidance, with many widgets adapting their colors to dark themes and offering elevation overlays to communicate elevation when shadows don’t read.

Chris Banes goes in depth about MDC's dark theme support in another post.

## Material Motion

Material Design provides guidance for screen transitions. Better than guidance alone, MDC now implements these patterns as Transitions ready to be dropped into your app. Hunter Stich introduces you to the material motion library in this post.

## Get Composed

Jetpack Compose is Android’s next-generation UI toolkit, currently in alpha. While it is not yet stable, it will offer an implementation of material components and material theming. Adopting MDC now will prepare your codebase for later adopting Jetpack Compose — it uses the same concepts, design vocabulary and components. It even becomes possible to ease your migration with libraries like MDC-Android Compose Theme Adapter which converts an MDC XML theme into a Compose MaterialTheme.

## Material Recommendation

Hopefully it’s clear to see why we recommend building Android UI with Material Components for Android. We’ve recently updated the `File > New Project` template in Android Studio to use MDC and set up a material theme for you, making it even easier to get started. If you haven’t already migrated to MDC, then check out our migration guide.

[IMAGE] Grid of baseline purple Material Components including banners, bottom navigation, buttons, floating action buttons, card, and chips.: 

[IMAGE] Three baseline purple Material buttons with a filled style, text style and outlined style.: 

[IMAGE] Material Time Picker with a digital clock showing 07:00, an AM/PM toggle, and a circular time picker.: 

[IMAGE] Various apps demonstrating Material Theming, including a purple baseline app, a pink “Shrine” shopping app, a dark purple and red “Crane” travel app, and a grey and orange “Reply” inbox app.: 

[IMAGE] Various screens showing grids, lists and details, each animating to demonstrate container transform, shared axis, fade through, and fade transitions.: 

[IMAGE] Light and dark versions of a stats app using baseline Material purple.:
