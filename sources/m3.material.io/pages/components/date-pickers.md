# Date pickers
Source: https://m3.material.io/components/date-pickers

## Overview

- Date pickers can display past, present, or future dates
- Three variants: docked, modal, modal input
- Clearly indicate important dates, such as current and selected days
- Follow common patterns, like a calendar view

[IMAGE] 3 variants of date pickers side-by-side. The docked date picker has an outlined text field above a calendar view. The modal date picker allows people to select a date from a calendar view. The modal date input lets someone type in a date.

_- Docked date picker
- Modal date picker
- Modal date input_

#### Availability & resources

#### Differences from M2

- Typography and spacing: Titles and labels are larger and have increased spacing to accommodate 48dp target size
- Color: New color mappings and compatibility with dynamic color
- Variants: The three variants of date pickers have been renamed to not be device-dependent. The former desktop date picker is now known as the docked date picker. The former mobile date picker and date input are now known as modal date picker and modal date input to reinforce that the user must take an action.

[IMAGE] Old version of a date picker with a white background and shadows.

_M2: Date pickers had a drop shadow and different color mappings_

[IMAGE] New version of date picker with a colorful background, rounded corners, and no shadows.

_M3: Date pickers have larger typography, no shadow, and new color mappings compatible with dynamic color_

## Specs

#### Tokens & specs

Select a component variant below to see its elements, attributes, tokens, and their values.

#### Docked date picker

[IMAGE] Diagram indicating the 11 elements of a docked date picker.

_- Outlined text field
- Menu button: Month selection
- Menu button: Year selection
- Icon button
- Weekdays label text
- Unselected date
- Today’s date
- Outside month date
- Text buttons
- Selected date
- Container_

[IMAGE] Diagram indicating 8 elements of a docked date picker with an open dropdown menu showing the months May to November.

_- Outlined text field
- Menu button: Month selection (pressed)
- Menu button: Year selection (disabled)
- Header
- Menu
- Selected list item
- Unselected menu list item
- Container_

##### Docked date picker color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] 11 color roles of a docked date picker in light and dark themes.

_Docked date picker color roles used for light and dark themes:
- Primary
- On surface variant
- On surface variant
- On surface
- On surface
- Primary
- On surface variant
- Primary
- Surface container high
- Primary
- On primary_

[IMAGE] 7 color roles of a docked date picker menu in light and dark themes.

_Docked date picker menu color roles used for light and dark themes:
- Primary
- On surface variant
- On surface
- Outline variant
- Surface container high
- Surface variant
- On surface_

##### Docked date picker measurements

[IMAGE] Diagram of padding, size, and layout measurements.

_Docked date picker padding and size measurements_

[IMAGE] Diagram of padding, size, and layout measurements.

_Docked date picker month menu padding and size measurements_

##### Docked date picker configurations

[IMAGE] 3 configurations of docked date picker.

_- Day selection
- Month selection
- Year selection_

#### Modal date picker

[IMAGE] Diagram indicating the 13 elements of a modal date picker in the day selection view.

_- Headline
- Supporting text
- Header
- Container
- Icon button
- Icon buttons
- Weekdays
- Today’s date
- Unselected date
- Text buttons
- Selected date
- Menu button
- Divider_

[IMAGE] 10 elements of a modal date picker menu.

_- Headline
- Supporting text
- Header
- Container
- Icon button
- Unselected year
- Selected year
- Text buttons
- Divider
- Menu button_

[IMAGE] Diagram indicating the 15 elements of a modal date picker when selecting a range of dates.

_- Headline
- Supporting text
- Icon button
- Header
- Text button
- Icon button
- Weekdays label text
- Container
- Today’s date
- Unselected date
- In-range active indicator
- In-range date
- Month subhead
- Selected date
- Divider_

##### Modal date picker color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] 12 color roles of a modal date picker day selection view.

_Modal date picker color roles used for light and dark themes in a day selection menu:
- On surface
- On surface variant
- Surface container high
- On surface variant
- On surface variant
- On surface
- Primary
- On surface
- Primary
- Primary
- On surface variant
- Outline variant_

[IMAGE] Diagram of 9 color roles of a modal date picker year selection view.

_Modal date picker color roles used for light and dark themes in a year selection menu:
- On surface
- On surface variant
- Surface container high
- On surface variant
- On surface variant
- Primary
- Primary
- Outline variant
- On surface variant_

[IMAGE] Diagram of 14 color roles of a modal date picker when selecting a range of dates.

_Modal date picker range selector color roles used for light and dark themes:

- On surface

- On surface variant

- On surface variant

- Surface container high

- Primary

- On surface variant

- On surface

- Primary

- On surface

- Secondary container

- On secondary container

- Outline variant

- On surface variant

- Primary_

##### Modal date picker measurements

[IMAGE] Diagram of size and padding measurements in day selection view.

_Modal date picker padding and size measurements_

[IMAGE] Diagram of size and padding measurements in year selection view.

_Modal date picker year selector padding and size measurements_

[IMAGE] Diagram of size and padding measurements when selecting a range of dates.

_Modal date picker date range selector padding and size measurements_

##### Modal date picker configurations

[IMAGE] 3 configurations of a modal date picker shown in dark mode.

_- Single date selection
- Date range selection
- Year selection_

#### Modal date input

[IMAGE] Diagram indicating the 8 elements of a modal date input.

_- Headline
- Supporting text
- Header
- Container
- Icon button
- Outlined text field
- Text buttons
- Divider_

##### Modal date input color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] Diagram indicating the 7 color roles of a modal date input.

_Modal date input color roles used for light and dark themes:
- On surface
- On surface variant
- Surface container high
- On surface variant
- Primary
- Primary
- Outline variant_

##### Modal date input measurements

[IMAGE] Diagram of the padding and size measurements of a modal date input.

_Modal date input padding and size measurements_

##### Modal date input configurations

[IMAGE] 2 configurations of modal date input.

_- Single date input
- Date range input_

#### Element states

[IMAGE] Diagram of 5 various states for date and year elements within date pickers.

_States for date and year selection:
- Default (enabled)
- Disabled
- Hovered
- Focused
- Pressed (ripple)_

## Guidelines

[IMAGE] A date picker opens in a form UI.

_Docked date picker on desktop_

#### Usage

Date pickers let people select a date or range of dates. They should be suitable for the context in which they appear.

Date pickers can be embedded into:
- Dialogs on compact breakpoints like mobile
- Text field drop-downs on medium and expanded breakpoints like tablet and desktop

[IMAGE] 2 date picker variations: a dialog on mobile and a dropdown within a text field on desktop.

_- Date picker dialog on mobile
- Date picker text field dropdown on desktop_

There are three variants of date pickers:
- Docked date picker
- Modal date picker
- Modal date input

[IMAGE] A docked date picker component.

_1. Docked date picker_

[IMAGE] A modal date picker component.

_2. Modal date picker_

[IMAGE] A modal date input component.

_3. Modal date input_

#### Anatomy

##### Docked date picker

[IMAGE] 7 elements of a docked date picker.

_- Text field
- Menu button
- Icon button
- Label text
- Menu
- Text buttons
- Container_

[IMAGE] 3 elements of a docked date picker.

_1. Text field
2. Menu button
3. Menu_

##### Modal date picker

[IMAGE] 12 elements of a modal date picker.

_- Headline
- Supporting text
- Container
- Icon button
- Previous/next month buttons
- Day of week labels
- Today’s date
- Unselected date
- Text buttons
- Selected date
- Menu button
- Divider_

[IMAGE] 9 elements of a modal date picker.

_- Headline
- Supporting text
- Container
- Icon button
- Unselected year
- Selected year
- Text buttons
- Divider
- Menu button_

##### Modal date input

[IMAGE] 7 elements of a modal date input.

_1. Headline
2. Supporting text
3. Container
4. Icon button
5. Date input
6. Text buttons
7. Divider_

##### Full-screen date picker

[IMAGE] 14 elements of a full-screen date picker.

_1. Headline
2. Supporting text
3. Icon button
4. Container
5. Text button
6. Icon button
7. Divider
8. Day of week labels
9. Today’s date
10. Selected date range
11. Unselected date
12. Text buttons
13. Selected date range start date
14. Month label_

#### Docked date picker

##### Usage

Docked date pickers allow the selection of a specific date and year. The docked date picker displays a date input field by default, and a dropdown calendar appears when the user taps on the input field. Either form of date entry can be interacted with.

Docked date pickers are ideal for navigating dates in both the near future or past and the distant future or past, as they provide multiple ways to select dates.

[IMAGE] Docked date picker on a desktop screen.

_Docked date picker on desktop_

##### Behavior

Dates can be added by using a keyboard or by navigating the calendar UI; both options are immediately available when the docked date picker is accessed.

[IMAGE] Docked date picker with a text field and the UI picker showing the selected date.

_Docked date picker_

[VIDEO] Animation of a docked date picker adjusting its size to the selected month.

_Docked date pickers adjust size dynamically_

[VIDEO] Animation of a docked date picker changing from calendar view to year selection menu view.

_The year selection menu replaces the calendar view_

##### Month selection

Month selection can be navigated with the corresponding back and next arrows or by tapping the dropdown menu.

[IMAGE] Docked date picker with a list of months May through September. August is selected.

_Docked date picker month selection_

##### Year selection

Year selection can be navigated with the corresponding back and next arrows or by tapping the dropdown menu.

[IMAGE] Docked date picker with a list of years 2025 to 2029. 2025 is selected.

_Docked date picker year selection_

#### Modal date picker

##### Behavior

Modal date pickers navigate across dates in several ways:
- To navigate across months, swipe horizontally
- To navigate across years, scroll vertically
- To access the year picker, tap the year

Don’t use a modal date picker to prompt for dates in the distant past or future, such as a date of birth. In these cases, use a modal input picker or a docked date picker instead.

[VIDEO] Animation of a docked date picker being swiped horizontally to navigate across months.

_To navigate across months, swipe horizontally_

[VIDEO] Animation of a year picker in a docked date picker being tapped and scrolled vertically to navigate across years.

_To navigate across years, tap the year picker and scroll vertically_

##### Date range selection

Date range selection provides a start and end date. Common use cases include:
- Booking a flight
- Reserving a hotel

Modal date pickers navigate across date ranges in several ways:
- To select a range of dates, tap the start and end dates on the calendar
- To navigate across months, scroll vertically

[VIDEO] Animation of a date range being selected on a modal date range picker.

_Modal date range picker_

[VIDEO] Animation of a modal date range picker being scrolled vertically to navigate across months.

_Modal date range picker with vertical scroll_

#### Modal date input

##### Usage

Modal date inputs allow the manual entry of dates using the numbers on a keyboard. People can input a date or a range of dates in a dialog.

[IMAGE] A modal date input component.

_Modal date with manual input_

[IMAGE DO] A modal date input component showing a day in 1979, which would be difficult to choose using UI.

**DO:** _For dates that don’t require a calendar view, the modal date input can be the default view_

[IMAGE DO] A modal date input with hint text for entering the date.

**DO:** _Alternatively, a text field with appropriate hint text can prompt for dates, such as in a form_

##### Behavior

You can swap between the modal date picker and modal date input using the edit or calendar icon.

[VIDEO] Animation of a modal date picker switching to a modal date input when the range selection icon is tapped.

_Switching from a modal date picker to a mobile date input for selecting ranges_

[VIDEO] Animation of a modal date picker switching to a modal date input when the edit icon is tapped.

_Switching from a modal date picker to a modal date input for selecting a single date_

##### Compact breakpoint

On compact breakpoints, such as mobile, a full-screen modal date picker is recommended to increase readability and touch target size. It can cover the entire screen.

[IMAGE] A full-screen view of modal date picker on a mobile device.

_A full-screen modal date picker on mobile_

##### Medium and expanded breakpoints

The docked date picker works best for medium and expanded breakpoints. It displays a date input field by default, and a dropdown calendar appears when a person taps on the input field. A person can interact with either form of date entry.

Docked date pickers are ideal for navigating dates in both the near future or past, and in the distant future or past, as they provide multiple ways to select dates.

[IMAGE] A docked date picker displaying a full calendar view on a large screen device.

_A docked date picker with a full calendar view is best used on larger devices_

##### Selection

Selection is indicated through color, drawing visual attention. In date ranges, start and end dates are selected, while dates in-between appear connected with a subtle highlight.

[VIDEO] Animation of a modal date range picker showing visual differences between selecting a date range and today's date.

_Differences between selected the selected date range (August 17–23) and today's date (August 5) are shown through color and fill_

##### Appearing and disappearing

Like other kinds of dialogs, modal date pickers use an enter and exit transition pattern to appear on the screen.

To exit a date picker, the input can either be confirmed (**OK**) or dismissed (**Cancel**). Interacting outside of the dialog will also dismiss the time picker. Unless one of these actions is taken, a time picker will continue to retain focus. Mobile full-screen pickers also have an additional close affordance (x) icon button and **Save** confirmation.

Docked date pickers appear just below the input field.

[VIDEO] Animation of a modal date picker's enter and exit transition.

_Modal date pickers can be dismissed through interacting with content outside the dialog, or with the action buttons in the lower right_

[VIDEO] Animation of a docked date picker's calendar icon being clicked making the calendar view appear for date selection.

_Interacting with the input for a docked date picker makes the calendar view appear below_

##### Responsive layout

The sizing of the docked and modal date picker components don’t scale responsively to different breakpoints.

[IMAGE DON'T] Docked date picker enlarged on a large screen responsively.

**DON'T:** _Don’t scale the date picker responsively to a larger size_

## Accessibility

#### Use cases

People should be able to:
- Enter dates manually by inputting text, without using the picker
- Use multiple input methods, making it accessible to those using assistive technology

On the docked date picker, the text field can be used for input.
On the modal date picker, the date input option should be available using the edit icon.

#### Interaction & style

The edit icon indicates the ability to switch to the modal date input.

Interactive targets for all elements meet Material's 48x48dp minimum touch target requirement. Increasing density would negatively impact accessibility by limiting tappable/clickable targets.

[IMAGE] Date picker with the edit icon focused.

_The edit icon indicates the ability to switch to the modal date input_

[IMAGE] Touch target used to select September 17 to 23 on a date picker.

_Touch targets are 48x48dp_

#### Date entry methods

The date entry component offers two ways to enter a date:
- Direct text entry into a text field
- Through the date picker

The calendar icon is the exclusive entry point for the date picker.

This improves efficiency for a screen reader and other keyboard users, as it makes interaction with the date picker optional and reduces the amount of key presses required to input a date.

Each input is a separate tab stop, which improves discoverability of the control.

[IMAGE] Text input field next to a date picker icon provides a choice of how to enter the date.

_Entering a date either through direct text entry or the date picker_

#### Accessible date input

Automatically format the date after the user hits “Enter“ or navigates out of the text field. Don't automatically format the date by adding slashes or other special characters while the user is typing (also known as input masks). This can cause confusion for people using screen readers because it changes what they typed.

To reduce errors, accept a range of formats including dashes, spaces, slashes, dots, and 0 to the left of a single digit month/day. This is especially helpful for assistive technology users who might be more prone to errors when interacting with complex inputs.

[IMAGE] Numeric entry 08172323 automatically formatted to 08/17/2023.

_The text field's logic can adapt to the user's actual input format, applying the correct formatting after the user has completed their text entry_

#### Optional **Clear** button

If it's not needed for your use case, remove the **Clear** button from the screen to reduce the number of tab stops for keyboard users.

[IMAGE] Optional clear button on lower left corner of a date picker.

_Remove non-critical actions to reduce the number of tab stops for keyboard users_

#### Affordance for keyboard shortcuts

Ensure keyboard shortcuts are readily available for keyboard and screen reader users by providing the shortcut key in the tooltip. It should be included in the hint description to be read out by the screen reader.

As shown here, the previous year button is interactive and can therefore be focused via the keyboard. Upon focus, the tooltip explains the behavior of the button and shows the shortcut key.

[IMAGE] Shift + Page up is the keyboard shortcut to go to the previous year on a date picker.

_Keyboard tooltip example for date picker_

#### Truncated labels & tooltips

Truncating labels isn't ideal, but tooltips allow the full text to be shown on hover or keyboard focus.

Days of the week are not interactive and are therefore not focusable via keyboard, yet the tooltip is available on hover. The date picker relies on the conventionality of these abbreviations for some assistive technology users.

[IMAGE] A pointer hover over the “T” day on a date picker produces the tooltip “Tuesday.”

_Days of the week are not navigable via keyboard, so the tooltip is shown only on pointer hover_

#### Color contrast between dates

Dates should have contrast of at least 4.5:1 between the link text colors and the background.

[IMAGE] A date picker’s label text passes the color contrast minimum of 4.5:1.

_Dates pass the 4.5:1 contrast minimum_

#### Keyboard navigation

|
**Keys** |
**Actions**
| Enter/return | Enter/return
| Enter/return | Closes the calendar and saves the selected date
| Page up/down | Move to the same date on next/previous month
| Home/End  | Move to the first day of the month
| Shift + Page up/down | Moves to the same date in the next/previous year
| Shift + M | Moves to the month list dropdown
| Shift + Y | Moves to the year list dropdown

#### Labeling elements

The text field's accessibility label should clearly state the purpose of the input (for example, event date or reservation date) and should match the placeholder text when the field is empty.

The helper text (below the text field) should specify the date format (for example, MM/DD/YYYY or YYYY/MM/DD) and act as a description for the text field. The default helper text is "MM/DD/YYYY," but this can be customized.

[IMAGE] Text field accessibility labels.

_The accessibility label clearly states the kind of input as an event date_

|
**Element** |
**A11y label** |
**Role**
| Previous / next month and year | “{label}” | Button
| Month and year dropdowns | “{label}” | Button
| Days of the week | Column header |
| Month grid | Grid |

#### Screen reader verbalizations

To support screen reader users, labels are used to enumerate the complete date. This allows screen reader users to hear the full context of "Monday, August 17” instead of just part of the date.

[IMAGE] Current date label providing day, month, and year for screen reader accessibility.

_Screen readers will state the full day, month, date, and year instead of just the number 17_
