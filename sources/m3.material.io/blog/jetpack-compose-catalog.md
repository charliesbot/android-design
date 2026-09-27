# Introducing the Compose Material Catalog
Source: https://m3.material.io/blog/jetpack-compose-catalog

> An overview of the new Material Design catalog for Jetpack Compose

Published: 2021-05-19

We’re excited to announce a new canonical reference of Material Design components, theming, dark theme, and how to implement these features in Jetpack Compose: the Compose Material Catalog.

If you’d like to learn more about the Material Design features of Compose, check out our blog post.

## Catalog features

The catalog consists of three main screens: the home screen, the component screen, and the example screen. At any point you can launch the theme picker or “more” menu from the top app bar. The app also supports dark theme.

#### Home screen

The home screen consists of a responsive grid of Material Components available in Compose—from floating action buttons to navigation drawers. Tapping on a component launches the component screen.

#### Component screen

The component screen consists of a description of the Material Component and a list of relevant examples implemented in Compose. Tapping on an example launches the example screen.

#### Example screen

The example screen contains an interactive example of the Material Component implemented in Compose.

#### Theme picker

The theme picker and “more” menu are accessible via the top app bar from any screen.

The theme picker allows you to pick Material Theming values—including color, typography, and shape. These values are applied (by animating between the current theme and the new theme) to examples and the app itself, so you can see how components respond to the changes.

#### “More” menu

The “more” menu contains links to design guidelines, developer docs, source code, and the issue tracker. These links are specific to the screen you’re on—eg. The source code link shows the source of a composable on the component screen, and the source of a sample on the example screen.

#### Dark theme

The app responds to the system dark theme. You can see Material Dark Theme features in action, like elevation overlays and limited color accents.

## In sync and up-to-date

The app is available alongside the source code of Compose libraries, ensuring you’ll have the latest Compose API changes. Built directly from the code snippets in the API reference docs, you’ll receive the latest samples directly in the catalog.

Let’s take a look at an example:
- The compilable `ButtonSample` stored in AOSP
- The `@sample` tag reference to `ButtonSample` in the `Button` KDoc
- The `Button` API reference doc showing the `ButtonSample` code snippet
- The app using the `ButtonSample` as an example for buttons

## Get the catalog

You can get access to the app in a variety of formats:
- Get the app on Google Play
- Download APKs from the Jetpack Compose samples GitHub repository releases page
- View the source code on AOSP using Android Code Search or the synchronized Android Jetpack repository on GitHub. You can also run the app if you’ve checked out the code and opened it in Android Studio.

Found the catalog helpful? Let us know on Twitter!

[IMAGE] Screens in the Compose Material Catalog: Screens in the Compose Material Catalog

[IMAGE] Theme picker in the Compose Material Catalog: Theme picker in the Compose Material Catalog

[IMAGE] Dark theme in the Compose Material Catalog: Dark theme in the Compose Material Catalog
