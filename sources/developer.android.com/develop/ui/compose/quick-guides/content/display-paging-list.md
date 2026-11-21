# Display a paging list
Source: https://developer.android.com/develop/ui/compose/quick-guides/content/display-paging-list

Create a paging list so that users can scroll to access content too large to fit on a single screen. Horizontal paging lists can help users navigate through content such as images, slideshows, or product carousels. Vertical paging lists are useful for content-heavy apps where users may need to scroll through a large number of items, such as articles.

## Results
**Figure 1.** Demo of `HorizontalPager`.

## Version compatibility

This implementation requires that your project minSDK be set to API level 21 or higher.

### Dependencies

## Create a paging list

You can configure a horizontal or vertical paging list, depending on the orientation required for your app. The following code creates a horizontal paging list displaying 10 items:

### Key points about the code
- The `HorizontalPager` composable provides a horizontally scrollable list of items.
  - To create a vertical paging list, use the `VerticalPager` composable instead.

- Each page in the list contains a `Text` object that displays the string "Page" and the page index number.
- An instance of `rememberPagerState()` persists a page's state when the user navigates away, and displays the same page when the user returns to it.

## Collections that contain this guide

This guide is part of these curated Quick Guide collections that cover broader Android development goals:

###### Display a list or grid
Lists and grids allow your app to display collections in a visually pleasing form that's easy for users to consume.
Quick guide collection

###### Display interactive components
Learn how composable functions can enable you to easily create beautiful UI components based on the Material Design design system.
Quick guide collection

###### Compose basics (video collection)
This series of videos introduces various Compose APIs, quickly showing you what’s available and how to use them.
Quick guide collection
