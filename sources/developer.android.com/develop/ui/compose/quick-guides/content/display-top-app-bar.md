# Display a top app bar
Source: https://developer.android.com/develop/ui/compose/quick-guides/content/display-top-app-bar

Create a top app bar to help users navigate and access functions in your app, using the `TopAppBar` composable.

## Results

**Figure 1.** A medium top app bar.

## Version compatibility

This implementation requires that your project minSDK be set to API level 21 or higher.

### Dependencies

## Create a composable for top app bar

Create a top app bar using the `MediumTopAppBar` composable that collapses when the user scrolls down the content area, and expands when the user scrolls back to the top of the content:

### Key points about the code
- An outer `Scaffold` with a `TopBar` set.
- A title consisting of a single `Text` element.
- A top bar with a single action defined.
- An `IconButton` action with an `onClick` lambda to perform the action.
- An `IconButton` containing an `Icon` that has an icon image and a content description text.
- The scroll behavior for the Scaffold's inner content is defined as `enterAlwaysScrollBehavior()`. This collapses the app bar when the user pulls up the inner content, and expands the app bar when the user pulls down the inner content.
- In addition to `MediumTopBar`, which contains the title, you can also use:
  - `TopAppBar`: use for screens that don't require a lot of navigation or actions.
  - `CenterAlignedTopAppBar`: use for screens that have a single primary action.Title is centered within the component.
  - `MediumTopAppBar`: use for screens that require a moderate amount of navigation and actions.
  - `LargeTopAppBar`: use for screens that require a lot of navigation and actions. Uses more padding than `MediumTopAppBar` and places the title beneath any additional icons.

## Collections that contain this guide

This guide is part of these curated Quick Guide collections that cover broader Android development goals:

###### Create a home screen scaffold
Find out how to use a standardized platform to build complex user interfaces. The scaffold holds together different parts of the UI, giving apps a coherent look and feel.
Quick guide collection

###### Display interactive components
Learn how composable functions can enable you to easily create beautiful UI components based on the Material Design design system.
Quick guide collection
