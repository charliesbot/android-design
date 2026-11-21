# Create a bottom sheet
Source: https://developer.android.com/develop/ui/compose/quick-guides/content/create-bottom-sheet

A bottom sheet shows secondary content, anchored to the bottom of the screen.

## Results

**Figure 1.** A standard bottom sheet (left) and a modal bottom sheet (right).

## Version compatibility

This implementation requires that your project minSDK be set to API level 21 or higher.

### Dependencies

## Implement a bottom sheet

To implement a bottom sheet, use the `ModalBottomSheet` composable:

## Expand and collapse the sheet

To expand and collapse the sheet, use `SheetState`:

### Key points
- Use the `content` slot, which uses a `ColumnScope` to lay out sheet content composables in a column.
- Use `rememberSheetState` to create an instance of `SheetState` that you pass to `ModalBottomSheet` with the `sheetState` parameter.
- `SheetState` provides access to the `show` and `hide` functions and to properties related to the current sheet state. These functions require a `CoroutineScope` — for example, `rememberCoroutineScope` — and can be called in response to UI events.
- Make sure to remove the `ModalBottomSheet` from composition when you hide the bottom sheet.

## Collections that contain this guide

This guide is part of these curated Quick Guide collections that cover broader Android development goals:

###### Display interactive components
Learn how composable functions can enable you to easily create beautiful UI components based on the Material Design design system.
Quick guide collection
