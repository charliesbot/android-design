# Time text
Source: https://developer.android.com/design/ui/wear/guides/m2-5/components/time-text

`TimeText` is a layout that shows the current time at the top of the screen.

**Time Text**

Use time text to display the time as well as an optional label on the top of the screen. When the device has a round screen time text is curved. When the device has a rectangular screen, time text is straight.

You can add an additional leading content label to the time text. When adding leading content, the full length of the arc should not be larger than a quarter of the watch face.

## Anatomy

When creating `TimeText` with scrollable elements such as lists, design the `TimeText` to fade away when the element scrolls. Use `Modifier.scrollAway` to scroll `TimeText` vertically in and out of view, based on the scroll state.

** A. Leading content
B. Separator
C. Time
**

## Usage

To show the estimated time of arrival in a maps app, use time text with leading content as shown in the following example.
