# Migrating to Material Components for Android
Source: https://m3.material.io/blog/migrate-android-material-components

> From Design Support Library 👉 MDC 1.0.0 👉 MDC 1.1.0 and beyond

Published: 2020-04-16

We recently announced Material Design Components (MDC) `1.0.0` — a library update that brings Material Theming, new widgets, dark theme support and other exciting features to your Android app.

MDC replaces the Design Support Library. This guide will show you how to migrate your codebase so you can make use of the new attributes, styles, and widgets. If you’re on MDC `1.0.0` this also provides the necessary migration steps to `1.1.0`. Be sure to check out our corresponding video guide as well!

## A simplified theming example

This guide uses a simplified app to demonstrate the migration process. It uses an AppCompat theme, widgets from the Design Support Library (including a button with a custom background), and various other elements that require migration. We’ll start with an app theme which uses the traditional AppCompat template:

## Migrating from the Support Library to Jetpack

Before you can use MDC, you need to migrate from the Support Library to Android Jetpack. Jetpack uses the new `androidx.*` namespace and splits the previous Support Library packages into separately maintained, semantically versioned libraries, providing feature parity as well as new libraries. MDC is built with AndroidX libraries so migration is mandatory.

To migrate to AndroidX, we recommend following the official developer documentation or watching the “Migrating to AndroidX: The time is right” talk from Android Dev Summit ’19. The ‘**Refactor** **> Migrate to AndroidX**’ tool in Android Studio will refactor your Design Support Library dependency to MDC.

_Note: Jetpack and MDC artifacts with version _`_1.0.0_`_ are binary compatible with the Support Library _`_28.0.0_`_ artifacts. If you’re not on version 28 then we recommend upgrading to this first and then migrating._

## Updating to MDC 1.0.0

If you used the Android Studio ‘Refactor > Migrate to AndroidX’ tool during Jetpack migration, your Design Support Library dependency should have mapped to MDC `1.0.0` and you can skip this section.

If not, you will need to manually update your dependency:

You will also need to change the package namespace of any usages of the Design Support Library classes (in XML layouts and in code) from `android.support.design.*` to `com.google.android.material.*`. To do so, take a look at the class mapping table.

## Changing your theme(s)

You need to ensure that your app theme inherits from a Material Components theme. The same applies to any additional themes and theme overlays you may have in your project.

If you were previously using an `AppCompat` theme variant, the MDC-Android theme variants map one-to-one with these. In most cases, simply swap out the AppCompat portion of the parent with `MaterialComponents`.

See the full theme and theme overlay mapping tables below:

## Example updates

## Button changes

Having changed our dependency to MDC `1.0.0` and our app theme to inherit from `Theme.MaterialComponents.*`, we can observe some unexpected changes to buttons in our example app. We have lost our custom background! They now mostly make use of the green accent color and have wider letter spacing in their text labels.

To understand why this has happened, we need to start by taking a look at how we’ve added these buttons in our layout (as framework `&lt;Button&gt;`s):

So, what’s going on? 🤔

## MDC widgets and auto-inflation

Like AppCompat, MDC will replace some framework widgets with MDC equivalents at inflation time. This makes it possible to ship new features and bugfixes without having to swap all your declarations for a new type. This is done via `MaterialComponentsViewInflater,` an extension of AppCompatViewInflater.

See the full widget auto-inflation mapping table below:

_Note: In MDC _`_1.0.0_`_ only __Button__s were replaced. The other widgets above were added in subsequent versions of the library._

Our example app was previously replacing the framework `&lt;Button&gt;`s with `&lt;AppCompatButton&gt;`s because we had a `Theme.AppCompat.*` theme. Having migrated to a `Theme.MaterialComponents.*` theme, this has changed to `&lt;MaterialButton&gt;`s which has an updated default style.

Unlike `AppCompatButton`, `MaterialButton` did not support custom backgrounds until release 1.2.0-alpha06 of MDC-Android. This is covered in more detail, along with a workaround, in the “**Shape**” section below.

We will keep this as is for now.

## Updating to MDC 1.1.0

A _lot_ has changed in MDC between `1.0.0` and `1.1.0`! The new features include:
- Full Material Theming support for color, typography, and shape
- Dark theme support
- Android 10 gesture navigation insets in widgets
- New widgets like the extended FAB, date picker, badges, and toggle buttons
- Accessibility improvements, bug fixes, and more

We’re now ready to bump our MDC dependency version to `1.1.0`:

_Note:__ Some AndroidX dependencies, such as AppCompat, may also need updating at this time. While not strictly required, we recommend updating to the latest __stable versions__ if possible._

## Some expected changes and common issues

MDC `1.1.0` changes some default widget styling to better comply with the Material Design guidelines. After upgrading you may, however, notice some unexpected changes to certain widget colors and other attributes.

In our example above, buttons have changed once again, the colors of text and icons have changed, FABs are now a shade of teal, and the text field looks entirely different. Oh dear! Don’t worry, your theme is likely missing some of the important MDC attributes while also having some AppCompat or framework attributes you no longer need. Let’s understand these issues by going through some common migration scenarios.

## Text field changes

The default style for text fields has changed in MDC to a new, improved version backed by user research.

We recommend sticking with this version for improved usability and configurability. However, we realize that this may not immediately fit with your brand and design system.

To revert back to the legacy text field, adjust the style in your layout to use the Design Support Library version:

Alternatively, you can make this the default style for all text fields in your theme(s):

## Prefer MDC styles and widgets

As we’ve seen above, widgets previously in the Design Support Library have since become part of MDC. In most cases there are new `Widget.MaterialComponents.*` styles that replace `Widget.Design.*` styles, along with new attributes that enable additional features. While opting out is possible, we recommend adopting the new MDC styles.

For components that were not part of the Design Support Library, in some cases there is now a Material version of the class. We saw this above with `AppCompatButton` and `MaterialButton`. We recommend using MDC classes over AppCompat or framework equivalents, if available. These widgets use updated Material Design design guidelines by default and support the full set of MDC attributes, which enable Material Theming and other features.

There are a few scenarios you should consider:
- Widgets used directly in layouts should change to MDC versions (see the “**MDC widgets and auto-inflation**” section above to see which widgets can be kept as framework tags)
- Any styles, default styles and default style attributes should change to MDC versions
- Any widgets used programmatically or as parents for custom classes should change to MDC versions

See the full widget and style mapping tables below:

Be sure to also check out the full list of Android components for widgets new to MDC as well as usage documentation.

## Example updates

**Replaces widgets with MDC versions**

In our example, we need to change some of the widgets in our layout to use MDC versions:

## Color

**New attributes**

The MDC color palette draws directly from the Material Design color system.

As a result of the shared history between MDC-Android, AppCompat and the framework, the resulting set of color attributes comprises the following:
- Existing attributes from the framework that are appropriately named (eg. `android:colorBackground`)
- Existing attributes from AppCompat that are appropriately named (eg. `colorPrimary` and `colorError`)
- New attributes introduced by MDC (eg. `colorSurface`, `colorOnPrimary`, etc.)

These attributes are used by MDC widgets to tint their backgrounds, text, icons and more. Knowing which widgets use which colors requires inspecting the default widget styles in the source code.

There are also colors from AppCompat and the framework that still exist but no longer apply to this new system. The `Theme.MaterialComponents.*` themes do their best to backport these old attributes for widgets that still rely on them, eg.

However, you should consider these attributes deprecated; either use a more appropriate MDC attribute or phase them out.

See the full color attribute mapping table below:

## Example updates

**Update to new color attributes**

In our example, we need to update our app theme to override the preferred color attributes:

_Note: We have not overridden all of the color attributes and are relying on the defaults for __`colorSurface`, `colorError`__, etc, which is perfectly acceptable. We have also not specified a dark theme palette._

Use “on” color attributes where appropriate

We should also switch from using an `@color` to one of the new “on” color attributes for our contained button text color:

## Typography

**New **`**TextAppearance **`**styles/attributes**

The MDC type scales draw directly from the Material Design type system.

A new set of `TextAppearance.MaterialComponents.*` styles and corresponding `textAppearance*` theme attributes have been introduced, which replace existing AppCompat / framework styles.

These attributes are used by MDC widgets to style text. Knowing which widgets use which type scales requires inspecting the default widget styles in the source code.

See the full type style and attribute mapping table below:

## Example updates

**Update to new type attributes**

In our example, we need to update the `TextView`s within the card in the layout to use the preferred type attributes:

**Customize type scales with font family**

We can also optionally override type scales in our app theme to use a custom font family, with XML or downloadable fonts via Android Studio:

_Note: For this example, we have only overridden some of the type scales. If you’re using a custom font, we recommend overriding all of the type scales for brand consistency._

## Shape

`**ShapeAppearance **`**styles/attributes**

The Material Design shape system is a way to apply treatments to the corners of MDC widgets, split into small, medium and large component categories.

This takes the form of Android `ShapeAppearance.*` styles with corresponding theme attributes. They include a `cornerFamily` — `rounded` or `cut` — and `cornerSize*` as a dimension.

These attributes are used by MDC widgets to style their backgrounds. Knowing which widgets apply to which shape categories requires inspecting the default widget styles in the source code.

## Widget backgrounds

The class that implements this functionality is `MaterialShapeDrawable`. All MDC widgets use this drawable as their background by default and you can also consider using it for custom views. It handles shape theming, backported shadow rendering, dark theme elevation overlays and more.

As a result, we advise against using `android:background` with custom XML drawables on MDC widgets as this will override the `MaterialShapeDrawable`. You may notice that the default styles for most MDC widgets specify

to specifically avoid this. Rather, prefer using `shapeAppearance` / `shapeAppearanceOverlay` and `backgroundTint` attributes to adjust background shape and color.

However, there are exceptions:
- As mentioned above, `MaterialButton` ignored `android:background` until release 1.2.0-alpha06 of MDC-Android. If you require this functionality while using earlier versions of the library, we advise explicitly using `AppCompatButton` in your layout(s).
- `MaterialShapeDrawable` doesn’t support gradients. If your brand requires this, using `android:background` with a `GradientDrawable` is your best bet.

## Example updates

**Remove background attrs that do not work with shape theming**

In our example, we can remove some widget attributes that are now handled by shape theming:

**Customize shape with corner family and size**

We can also optionally override shape styles in our app theme to express our brand:

**Restore button custom gradient background**

Finally, here’s how to restore our button’s custom gradient background by explicitly using AppCompatButton (along with the new MDC button type theming attribute):

If you’re using MDC-Android `1.2.0-alpha-06` (or later) then you can rely on `MaterialButton` respecting `android:background`. Keep in mind that you may need to clear the `backgroundTint` (which is set to `colorPrimary` in the `Widget.MaterialComponents.Button` default style):

## What’s next?

We’ve successfully gone through the process of migrating from the Design Support Library, to MDC `1.0.0` and finally to MDC `1.1.0`. We’ve migrated our usages of AppCompat and have made use of Material Theming.

We encourage you to try out new widgets and features in MDC that were not part of the Design Support Library.

The next feature release of MDC —`1.2.0`— is well underway with multiple alpha releases out at the time of writing. Exciting new updates include `Slider` and `ShapeableImageView` components along with the first Android release of the Material motion system!

As always, we encourage you to file bug reports and feature requests on GitHub. Also be sure to check out our Android companion example apps and Build a Material Theme.

We highly encourage migrating to MDC `1.1.0` (or later). If you’ve successfully migrated or if you’re having trouble doing so, reach out to us on Twitter @MaterialDesign and @AndroidDev.

[IMAGE] : Example app using AppCompat and the Design Support Library

[IMAGE] Jetpack logo: 

[IMAGE] MDC logo: 

[IMAGE] Contained button with green background and text button with green label made in MDC 1.0.0: Buttons in MDC 1.0.0

[IMAGE] Filled text field with a light grey background and dark grey label and underline: Text fields in MDC 1.1.0+

[IMAGE] Text field with no background and light green label and underline: Legacy Design Support Library text field

[IMAGE] : MDC color attributes with baseline light theme values

[IMAGE] : MDC color attributes with baseline dark theme values

[IMAGE] : MDC type attributes

[IMAGE] ShapeAppearanceSmallComponent with rounded, 4 dp corners. ShapeAppearanceMediumComponent with rounded, 4 dp corners. ShapeAppearanceLargeComponent with rounded, 0 dp corners.: MDC shape attributes

[IMAGE] : Button with restored custom gradient background

[IMAGE] : Example app using MDC 1.0.0 and Theme.MaterialComponents.* theme

[IMAGE] : Example app using MDC 1.1.0

[IMAGE] : Example app with updated MDC color attributes (fixed FAB color)

[IMAGE] : Example app with updated MDC type attributes (updated font family)

[IMAGE] : Example app with updated MDC shape attributes (updated corner radii)
