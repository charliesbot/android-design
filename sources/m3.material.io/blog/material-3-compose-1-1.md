# Material Design 3 for Compose gets new components and features
Source: https://m3.material.io/blog/material-3-compose-1-1

> Exploring the 1.1 release of Material Design 3 for Compose

Published: 2023-05-10

The 1.1 release of Compose Material 3 is here, bringing new components, improved APIs, and many other updates and enhancements you’ve been asking us for. Material Design 3 is the next evolution of Material Design, enabling you to build expressive, spirited, and personal apps. Start using Material Design 3 in your production apps today!

_Note: The terms "Material Design 3", "Material 3", and "M3" are used interchangeably._

## New components

With the 1.1 release, Material 3 Compose provides all types of components needed to make your production-ready apps, with additions like bottom sheet, date and time pickers, and many others. Let’s dive into some of these additions.

**Bottom sheets**

There are two variations of bottom sheet available: standard and modal bottom sheet.

The standard bottom sheet is great for use cases where you need to interact with both the bottom sheet and main UI region. You can use the standard bottom sheet with BottomSheetScaffold by providing sheet UI in the sheetContent parameter. You can also provide an optional drag handle to easily interact with the sheet.

The modal bottom sheet overlays the main UI and can be dismissed when users interact outside of the sheet region, similar to a dialog. It provides clear separation from the main UI, making it easy for users to focus on the sheet content.

You can use the modal bottom sheet as a standalone component and invoke it using `ModalBottomSheetState`. The component also provides `onDismissRequest` function to be invoked on dismiss.

**Date pickers**

The 1.1 release also brings brand new date selection components: `DatePicker()` and `DateRangePicker()`.

Both the date picker and date range picker support date input mode by updating the `initalDisplayMode` parameter to input type, which sets the initial display mode to input type. The user can switch back to default selection mode by toggle.

Compose Material 3 also provides a `DatePickerDialog()` composable to show a date picker in a dialog, with the option to confirm or dismiss it.

**Time pickers**

Compose Material 3 also includes new time picker components for choosing the desired time.

There are two versions available: one with a horizontal layout in which users enter the time using the keyboard, and another with a vertical layout in which users can also use a slide dial to select the time.

Compose provides a single `TimePicker` composable function, where you can define the layoutType parameter to choose between a horizontal or vertical layout.

**Search bars**

The 1.1 release brings new search bar components, which provide users the ability to search queries and display dynamic search results.

Material 3 provides two options, search bar and docked search bar, to give you the flexibility to choose the right fit for your product.

When active, the search bar expands into a search view that displays dynamic results. The active search bar takes up all available space to display the results, and can also expand to full screen.

The docked search bar is great for medium to large devices where the search view doesn’t need to take up all the screen space and results can be shown in a docked view.

The implementation of the docked search bar is similar to the search bar to keep both the components consistent.

**Tooltips**

Tooltips are another new addition to the latest 1.1 release. Tooltips are informative text labels that provide additional context to a button or other UI element.

Material 3 provides two types of tooltips: plain tooltip and rich tooltip.

Plain tooltips briefly describe a UI element. Plain tooltips are great for labeling UI elements with no text, like icon-only and field elements.

You can use the `PlainTooltipBox()` composable to add a plain tooltip. To apply the tooltip to any component, wrap the component with the tooltip composable and add `Modifier.tooltipAnchor()` to the component’s modifier.

Rich tooltips are great for longer texts like definitions or explanations. Rich tooltips provide additional context to a UI element and can include a button or hyperlink.

You can have either persistent or non-persistent rich tooltips by providing the isPersistent parameter to `RichTooltipState()`. To dismiss persistent tooltips, you need to tap outside the tooltip area or invoke the dismiss action on tooltip state.

Non-persistent tooltips will be dismissed automatically after a short duration.

To add rich tooltips, you can use the `RichTooltipBox()` composable and modify tooltip state to control the visibility of the tooltip.

## Stable components

With the 1.1 release, many key components have graduated from the experimental stage and are ready for production apps.

Components like Scaffold, Surface, Navigation drawers, and many others are building blocks of your apps, and you can now be assured that they will not have any major breaking changes.

When updating to the latest release, you can remove the `@OptIn(ExperimentalMaterial3Api::class)` annotation from the stable components.

## Motion and animations

Material 3 components in Compose come with built-in motion and animations to provide interactive and expressive experiences to users.

Components also provide behavior control to give you flexibility in choosing the animation you need for your product.

**Top app bar motion and animation**

For top app bars, you can choose between pinned scroll behavior or enter always scroll behavior.

In pinned scroll behavior, the top app bar is always pinned at the top, but changes its container color when content is scrolled.

To add the behavior to the app bar, define a scroll behavior `TopAppBarDefaults.pinnedScrollBehavior()` that links to the scaffold’s `Modifier.nestedScroll()` to listen to the scrolling changes.

In the enter always scroll behavior, the top app bar disappears when the user scrolls through the content, and shows up again when the user scrolls down.

To achieve the enter always behavior, define a scroll behavior `TopAppBarDefaults.enterAlwaysScrollBehavior()` that links to the scaffold’s `Modifier.nestedScroll()` to listen to the scrolling changes.

Material 3 provides all these animations as part of the components, with easy ways to customize them for your project.

## Resources

With the latest 1.1 release, Compose Material 3 is ready for production apps. Check out the resources below for more information:
- Fully Material 3 and Compose sample: Reply 💌
- Theming in Compose codelab
- The Material 3 guidance to start adding Material 3 to your apps
- The Material 2 to Material 3 migration guide
- The Jetpack Compose Samples GitHub repository, where you’ll find a variety of up-to-date samples using Material 3
- The Compose community on StackOverflow and the Kotlin Slack group
- The bug tracker, where you can report an issue and track feature request

[IMAGE] Multiple apps using Material Design 3 theming: Multiple apps using Material Design 3 theming

[IMAGE] Standard bottom sheet: Standard bottom sheet

[IMAGE] Modal bottom sheet: Modal bottom sheet

[IMAGE] Selection of date pickers: Left: Date picker in input mode, Middle: Date picker in display mode, Right: Date range picker

[IMAGE] Date picker dialog switching between picker and input display mode: Date picker dialog switching between picker and input display mode

[IMAGE] : Left: Time picker in vertical layout, Right: Time picker in horizontal layout

[IMAGE] Search bar and search result view: Search bar and search result view

[IMAGE] Docked search bar and search view on a tablet: Docked search bar and search view on a tablet

[IMAGE] Plain tooltip on an icon button: Plain tooltip on an icon button

[IMAGE] Rich tooltip with description and text button as action: Rich tooltip with description and text button as action

[IMAGE] Navigation rail and Navigation drawer: Navigation rail and Navigation drawer

[IMAGE] Top app bar with pinned scroll behavior: Top app bar with pinned scroll behavior

[IMAGE] Top app bar with enter-always scroll behavior: Top app bar with enter-always scroll behavior.
