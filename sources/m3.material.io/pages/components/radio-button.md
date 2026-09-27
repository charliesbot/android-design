# Radio button
Source: https://m3.material.io/components/radio-button

## Overview

- Use radio buttons (not switches) when only one item can be selected from a list
- Label should be scannable
- Selected items are more prominent than unselected items

[IMAGE] 1 radio button is selected from a list of 4 radio buttons of different ringtones.

_Radio buttons can be selected_

#### Resources & availability

#### What’s new

- Color: New color mappings and compatibility with dynamic color

[IMAGE] App screen with 1 active button selected from list of 3 buttons.

_Radio buttons feature new color mappings_

## Specs

[IMAGE] Diagram of enabled radio button.

_- Radio button icon_

#### Tokens & specs

Learn more about design tokens

#### Color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] Diagram of selected and unselected radio button colors.

_Radio button color roles used for light and dark themes:
- Primary
- On surface variant_

##### Adjacent text label color

Use the color role **on surface** for adjacent text labels. This remains the same even if interacting with the label or component.

[IMAGE] Radio buttons with labels. The labels are the same color for both selected and unselected radio buttons.

_The text color remains the same regardless if the button is selected or not_

#### States

States are visual representations used to communicate the status of a component or interactive element. Learn more about interaction states

[IMAGE] Diagram of radio button states including enabled, hover, focus, pressed, and disabled.

_- Enabled
- Hover
- Focus
- Pressed
- Disabled_

State specs are in the token module above

#### Measurements

[IMAGE] Diagram of radio button layout values.

_Radio button size measurements_

| Attribute
| Value

| Icon size
| 20dp
| State layer size
| 40dp
| Target size
| 48dp

## Guidelines

[IMAGE] 1 radio button is selected from a list of 4 radio buttons of different ringtones.

_Radio buttons_

#### Usage

Radio buttons are the recommended way to allow users to make a single selection from a list of options.

Only one radio button can be selected at a time.

[VIDEO] 1 of 3 languages is chosen using radio buttons. Selecting a language deselects the previous 1.

_Radio buttons should always be accompanied by clear inline labels_

Use radio buttons to:
- Select a single option from a set
- Expose all available options

[IMAGE] 2 radio buttons are used for allowing or turning off notifications. 2 checkboxes are used for microphone and location access.

_Radio buttons are single-select, unlike checkboxes which are multi-select_

[IMAGE DO] Filter page with 4 sort by options as radio buttons. Relevance is selected.

**DO:** _Use radio buttons when only one option can be selected from a list_

[IMAGE DO] Meal options page with the Additions item selected, along with 4 nested checkboxes for selecting various toppings. All checkboxes are selected.

**DO:** _Use checkboxes when multiple options can be selected from a list_

Avoid nesting radio buttons or using radio buttons to select multiple options.

[IMAGE DON'T] Selected radio button with 2 nested radio buttons.

**DON'T:** _Don’t nest radio buttons_

[IMAGE DON'T] 2 radio buttons selected at once from a list of 3 buttons.

**DON'T:** _Don’t allow radio buttons to select multiple options_

##### Alternate selection controls

Radio buttons are one of several selection controls, which allow people to make choices such as selecting options or switching settings on or off.

Switches and checkboxes are alternative selection controls that can be used to change settings or preferences.

[IMAGE] A selected and unselected switch.

_Switches_

[IMAGE] An unselected and selected checkbox.

_Checkboxes_

Use radio buttons when there are five or fewer options.

Consider using a drop-down menu instead of radio buttons when it’s important to save space on a screen. However, drop-down menus require additional steps for a person, both in the number of clicks and cognitive effort.

[IMAGE DO] A filter UI with 1 radio button selected from a list of 3 buttons.

**DO:** _Use radio buttons when there are five or fewer options_

[IMAGE DO] A dropdown menu with a list of 4 options.

**DO:** _Consider using a drop-down menu instead of radio buttons when space is constrained_

#### Anatomy

[IMAGE] 3 elements of a radio button.

_- Selected icon
- Adjacent label text
- Unselected icon_

##### Adjacent label text

Always pair radio buttons with an adjacent label describing what the radio button selects.

Because only one radio button can be selected at a time, each choice must have its own label.

[VIDEO] Checkout page with 2 radio buttons for home and office addresses. The labels are "Home" and "Office."

_Radio button always need label text_

#### Placement

Radio buttons are often arranged in stacked layouts.

[IMAGE] Settings page with 3 stacked radio buttons for selecting a language.

Radio buttons should be vertically listed and have one option always selected.

[IMAGE DO] 3 radio buttons with 1 option selected.

**DO:** _Radio buttons should always have one option pre-selected_

[IMAGE CAUTION] 2 radio buttons side by side with 1 option selected.

**CAUTION:** _Avoid using horizontal radio button lists_

#### Behavior

A radio button is successfully selected when a person clicks or taps either the radio button icon or the label.

[VIDEO] Selecting the radio button for dark theme instantly changes the screen to dark theme.

_Radio buttons should take effect immediately, unless they're in a dialog or page that needs to be saved_

## Accessibility

#### Use cases

People should be able to do the following with assistive technology:
- Navigate to a radio button
- Select a radio button
- Get appropriate feedback based on input type

#### Interaction & style

A radio button can be either selected or unselected. Selecting one radio button deselects any others. A radio group can start with one radio button selected, or none selected.

Once a radio button is selected, the group can’t be deselected. To let people opt out of their selection, either provide a **Not applicable** or **No option** radio button, or provide a separate way to deselect all radio buttons, like **Clear selection**.

People should be able to select either the text label or the radio button to select an option.

[VIDEO] Selecting between radio buttons for a home and office address.

_Only one radio button is selected at a time_

##### Avoid applying density by default

Don't apply density to radio buttons by default. This lowers their targets below Material's recommendation of 48x48 CSS pixels. Instead, give people a way to choose a higher density, like selecting a denser layout or changing the theme.

To ensure this density setting can be easily reverted when it's active, keep all targets to change it at a minimum of 48x48 CSS pixels each.

#### Initial focus

When outside the radio group, **Tab** moves focus directly to the selected radio button, or the first one if none are selected.

**Shift+Tab** instead focuses on the last radio if none are selected.

Use the **arrows** to navigate between options.

[IMAGE] The Tab key focuses on the first of 2 stacked radio buttons.

_Tab brings the focus to the initially selected item or the initial radio option_

[IMAGE] The up and down arrow keys move through a list of stacked radio buttons.

_Arrows move to next element in a list_

#### Keyboard navigation

| Keys | Actions
| **Tab** | Moves focus into the group to the selected radio button, or the first if none are selected
| **Shift** + **Tab** | Moves focus into the group to the selected radio button, or the last if none are selected
| **Arrows** | Moves focus and selects the previous or next radio button. Wraps focus and selection between the first and last radio buttons.
| **Space** | Selects a focused radio button. If already selected, does nothing.

#### Labeling elements

If the UI text is correctly linked to the radio button, assistive tech such as a screenreader will read the UI text, followed by the component’s role.

The accessibility label for a group of radio buttons is typically the same as its title. The role is **Radio group**.

[IMAGE] Accessibility tags for a radio button group.

_Label the radio group based on the category title_

The accessibility label for an individual radio button is typically the same as its adjacent text label.

[IMAGE] Accessibility tags for a radio button.

_Label the radio button based on its label text_
