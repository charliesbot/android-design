# Text fields
Source: https://m3.material.io/components/text-fields

## Overview

- Make sure text fields look interactive
- Two variants: filled and outlined
- The text field’s state (blank, with input, error, etc) should be visible at a glance
- Keep labels and error messages brief and easy to act on
- Text fields commonly appear in forms and dialogs

[IMAGE] 2 variants of text fields, filled and outlined.

_- Filled text field
- Outlined text field_

#### Availability & resources

#### Differences from M2

- Color: New color mappings and compatibility with dynamic color

[IMAGE] A filled and outlined text field with M3 color mappings.

_Text fields have new color mappings_

## Specs

#### Tokens & specs

Browse the component elements, attributes, tokens, and their values. Learn about design tokens

#### Filled text field

[IMAGE] Diagram of a filled text field indicating the 10 parts of its anatomy.

_- Container
- Leading icon (optional)
- Label text in empty field
- Label text in populated field
- Trailing icon (optional)
- Focused active Indicator
- Caret
- Input text
- Supporting text (optional)
- Enabled active indicator_

##### Filled text field color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] Diagram of a filled text field indicating its color mappings.

_Filled text field color roles used for light and dark schemes:
- Surface container highest
- On surface variant
- On surface variant
- Primary
- On surface variant
- Primary
- Primary
- On surface
- On surface variant
- On surface_

##### Filled text field states

States are visual representations used to communicate the status of a component or interactive element. Learn more about interaction states

[IMAGE] Side by side view of empty and populated filled text fields across different states, showing the differences between enabled, focused, hovered, and disabled.

_- Enabled (empty)
- Focused (empty)
- Hovered (empty)
- Disabled (empty)
- Enabled (populated)
- Focused (populated)
- Hovered (populated)
- Disabled (populated)_

##### Filled text field error states

Error states are visual representations used to communicate the status of a component or interactive element. An error message can display instructions on how to fix it. Error messages are displayed below the text field as supporting text until fixed.

[IMAGE] Side by side view of empty and populated filled text fields across different error states, showing the differences between enabled, focused, hovered.

_- Enabled (empty)
- Focused (empty)
- Hovered (empty)
- Enabled (populated)
- Focused (populated)
- Hovered (populated)_

##### Filled text field measurements

[IMAGE] Diagram showing layout values and paddings for filled text fields without icons.

_Padding and size measurements without icons_

[IMAGE] Diagram showing layout values and paddings for outlined text fields with leading and trailing icons.

_Padding and size measurements with icons_

[IMAGE] A diagram showing layout values and paddings for supporting text, and supporting text in combination with a character count.

_Padding and size measurements with supporting text and character count_

| Attribute | Value
| Default container height
| 56dp
| Label alignment (unpopulated)
| Vertically centered
| Top/bottom padding
| 8dp
| Left/right padding without icons
| 16dp
| Left/right padding with icons
| 12dp
| Icon alignment
| Vertically centered
| Padding between icons and text
| 16dp
| Supporting text and character counter top padding
| 4dp
| Padding between supporting text and character counter
| 16dp
| Target size | 56dp

##### Filled text field configurations

[IMAGE] Side by side view of filled text fields in different configurations.

_Empty and populated filled text fields with:
- Supporting text
- Trailing icon
- Leading icon
- Leading and trailing icons
- Prefix
- Suffix
- Multi-line text field_

#### Outlined text field

[IMAGE] Diagram of an outlined text field indicating the 9 parts of its anatomy

_- Enabled container outline
- Leading icon (optional)
- Label text in empty field
- Label text in populated field
- Trailing icon (optional)
- Focused container outline
- Caret
- Input text
- Supporting text (optional)_

##### Outlined text field color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] Diagram of an outlined text field indicating its color mappings

_Outlined text field color roles used for light and dark schemes:
- Outline
- On surface variant
- On surface variant
- Primary
- On surface variant
- Primary
- Primary
- On surface
- On surface variant_

##### Outlined text field states

States are visual representations used to communicate the status of a component or interactive element. Learn more about interaction states

[IMAGE] Side by side view of empty and populated outlined text fields across different states, showing the differences between enabled, focused, hovered, and disabled.

_- Enabled (empty)
- Focused (empty)
- Hovered (empty)
- Disabled (empty)
- Enabled (populated)
- Focused (populated)
- Hovered (populated)
- Disabled (populated)_

##### Outlined text field error states

Error states are visual representations used to communicate the status of a component or interactive element. An error message can display instructions on how to fix it. Error messages are displayed below the text field as supporting text until fixed.

[IMAGE] Side by side view of empty and populated filled text fields across different error states, showing the differences between enabled, focused, hovered.

_- Enabled (empty)
- Focused (empty)
- Hovered (empty)
- Enabled (populated)
- Focused (populated)
- Hovered (populated)_

##### Outlined text field measurements

[IMAGE] A diagram showing layout values and paddings for outlined text fields without icons.

_Padding and size measurements without icons_

[IMAGE] A diagram showing layout values and paddings for outlined text fields with leading and trailing icons.

_Padding and size measurements with icons_

[IMAGE] A diagram showing layout values and paddings for supporting text, and supporting text in combination with a character count.

_Padding and size measurements with supporting text and character count_

| Attribute | Value
| Container height
| 56dp
| Left/right padding without icons
| 16dp
| Left/right padding with icons
| 12dp
| Padding between icons and text
| 16dp
| Icon alignment
| Vertically centered
| Supporting text and character counter top padding
| 4dp
| Padding between supporting text and character counter
| 16dp
| Label alignment
| Vertically centered
| Left/right padding populated label text
| 4dp
| Target size | 56dp

##### Outlined text field configurations

[IMAGE] A side by side view of outlined text fields in different configurations.

_Empty and populated outlined text fields with:
- Supporting text
- Trailing icon
- Leading icon
- Leading and trailing icons
- Prefix
- Suffix
- Multi-line text field_

## Guidelines

[IMAGE] A side by side view of a filled and a outlined text field.

_Filled and outlined text fields_

#### Usage

Use a text field when someone needs to enter text into a UI, such as filling in contact or payment information.

[IMAGE] Mobile UI of contact form with several text fields.

_Contact form using outlined text fields_

There are two variants of text fields:
- Filled text fields
- Outlined text fields

Both variants of text fields use a container to provide a visual cue for interaction and provide the same functionality.

[IMAGE] Side by side view of a populated and unpopulated filled text field.

_Filled text field_

[IMAGE] Side by side view of a populated and unpopulated outlined text field.

_Outlined text field_

##### Outlined text fields

Outlined text fields have less visual emphasis than filled text fields. When they appear in places like forms (where many text fields are placed together), their reduced emphasis helps simplify the layout.

[IMAGE] App screen with 1 focused and 1 unfocused outlined text field.

_Login screen with outlined text fields_

#### Choosing text fields

##### Choosing text fields

Both variants of text field provide the same functionality. The variant of text field used can depend on style alone.

Choose the variant that:
- Works best with an app’s visual style
- Best accommodates the UI's goals
- Is most distinct from other components (like buttons) and surrounding content

[IMAGE] Mobile UI of a contact form with several filled text fields.

_Mobile form using filled text fields_

[IMAGE] Mobile UI of a contact form with several outlined text fields.

_The same mobile form using outlined text fields_

##### Using both text field variants on the same screen

If both variants of text field are used in a UI, they should be used consistently within different sections, and not intermixed within the same region.

For example, use outlined text fields in one section and filled text fields in another.

[IMAGE DO] Mobile UI of a contact form with several filled text fields, and an open dialog on top using an outlined text field.

**DO:** _When using both variants of text fields in a UI, separate them by region_

[IMAGE DON'T] Mobile UI of a contact form with a mix of outlined and filled text fields.

**DON'T:** _When using both variants of text fields, don't use both next to each other or within the same form_

#### Anatomy

##### Filled text field

[IMAGE] Diagram of filled text field indicating the 10 parts of its anatomy.

_- Container
- Leading icon (optional)
- Label text in empty field
- Label text in populated field
- Trailing icon (optional)
- Focused active Indicator
- Caret
- Input text
- Supporting text (optional)
- Enabled active Indicator_

##### Outlined text field

[IMAGE] Diagram of an outlined text field indicating the 9 parts of its anatomy.

_- Enabled container outline
- Label text in empty field
- Leading icon (optional)
- Label text in populated field
- Trailing icon (optional)
- Focused container outline
- Caret
- Input text
- Supporting text (optional)_

##### Containers

Containers improve the discoverability of text fields by creating contrast between the text field and surrounding content.

**Fill and stroke**
A text field container has a fill and a stroke either around the entire container, or just the bottom edge. The color and thickness of a stroke can change to indicate when the text field is active.

**Rounded corners**
The container of an outlined text field has rounded corners, while the container of a filled text field has rounded top corners and square bottom corners.

[IMAGE] Side by side view of the containers of a filled and outlined text field.

_Text field containers_

##### Label text

Label text tells people what information is requested. Every text field should have a label.

Label text should be aligned with the input text, and always visible. It can be placed in the middle of a text field, or rest near the top of the container.

Label text shouldn't be truncated or take up multiple lines. Keep it short, clear, and fully visible.

[VIDEO] Animation of 2 text field’s label text changing position when empty and populated.

_Label text should always be visible. When the field is selected, the label text moves from the middle of the text field to the top._

[IMAGE DON'T] Text field with very long label text, too long to display fully display inside the text field container.

**DON'T:** _Don’t truncate label text. Keep it short, clear, and fully visible._

[IMAGE DON'T] Text field with very long label text split into 2 lines.

**DON'T:** _Label text shouldn’t take up multiple lines_

##### Adjacent label

A text field doesn't require a label if the field's purpose is indicated by a separate, adjacent label.

Adjacent labels should be aligned to the leading edge of the text field container.

[IMAGE] Mobile UI of a contact form with label texts placed outside and on top of the text fields.

_Text fields with adjacent labels_

##### Required text indicator

To show a field is required, display an asterisk (*) next to the label text, and explain that asterisks indicate required fields in one of two ways:
- Supporting text

- A single note at the beginning of the form

Additional best practices include:

- Indicate all required fields

- If required text has a particular color, use the same color for the asterisk

[IMAGE] Mobile UI of a contact form showing supporting text below the text field, indicating an input is required.

_Asterisk with required supporting text_

##### Input text

Input text is text a person has entered into a text field.

Text fields can display input text in the following ways:
- **Single line** text fields display only one line of text
- **Multi-line** text fields grow to accommodate multiple lines of text
- **Text areas** are fixed-height fields

[IMAGE] Text field with populated input text.

_Input text in a filled text field_

[VIDEO] Text field with very long input text that scrolls sideways.

_In **single-line** fields, as the cursor reaches the right field edge, text longer than the input line automatically scrolls left. Single-line fields are not suitable for collecting long responses; use a multi-line text field or text area instead._

[VIDEO] Text field with very long input text that wraps into a second line, expanding the text field container.

_In **multi-line** fields, overflow text causes the text field to expand, shifting screen elements downward and text wraps onto a new line. These fields initially appear as single-line fields, which is useful for compact layouts that need to accommodate large amounts of text._

[VIDEO] Text area with enough space to allow a very long input text to wrap into a second line without the need to expand the container.

_**Text areas** are taller than text fields and wrap overflow text onto a new line. They are a fixed height and scroll vertically when the cursor reaches the bottom of the field. The large initial size indicates that longer responses are possible and encouraged. These should be used instead of multi-line fields on the web. Ensure the height of a text area fits within mobile screen sizes._

##### Prefix text

Text fields can contain prefix text such as currency symbol.

[IMAGE] Text field with a currency prefix before the input text.

_A text field with a currency symbol text prefix_

##### Suffix text

Text fields can contain suffix text such as unit of measurement or email domain.

[IMAGE] Text field with a suffix after the input text indicating a maximum input of 100.

_A text field with a grading scale as suffix_

[IMAGE] Text field with a suffix after the input text indicating an email address.

_A text field with an email domain suffix_

##### Supporting text & character counter

Supporting text conveys additional information about the input field, such as how it will be used. It should ideally be one line, though may wrap to multiple lines if required. It can be either persistently visible or visible only on focus.

If there is a character or word limit, include a character or word counter. They display the ratio of characters used and the total character limit.

[IMAGE] A side by side view of a text field with supporting text aligned with the trailing side, and a character counter aligned with the trailing side.

_- Supporting text
- Character counter_

##### Error text

For text fields that validate their content such as passwords, replace supporting text with error text. Swapping supporting text with error text prevents new lines of text from bumping content and changing the layout.
- If only one error is possible, error text should describe how to avoid the error
- If multiple errors are possible, error text should describe how to avoid the most likely error

[VIDEO DO] Mobile UI of a sign up form indicating a invalid entry, and a hint on how to resolve as supporting text below the container.

**DO:** _Swap supporting text with error text_

[VIDEO DON'T] Mobile UI of a sign up form with an invalid text field entry. The regular supporting text remains above the error message.

**DON'T:** _Don't add error text in addition to supporting text, as their appearance will shift content_

[IMAGE CAUTION] Mobile UI of a sign up form with an invalid text field entry. The error message wraps to 2 lines.

**CAUTION:** _Long errors can wrap to multiple lines if there isn't enough space to clearly describe the error. In this case, ensure padding between text fields is sufficient to prevent multi-lined errors from bumping layout content._

##### Error icon

It’s strongly recommended to show an error icon when the text field is in the error state.

This highlights the error for people with visual impairments, and provides an additional sensory indicator.

[IMAGE] 2 text fields with error messages. The active text field has a thicker border. Both text fields have a trailing error icon.

_The error icon is an important second visual indicator that a text field has an error_

##### Icons & images

Icons in text fields are optional. Text field icons can:
- Describe valid input methods such as a microphone icon
- Provide affordances to access additional functionality such as clearing the content of a field
- Express an error

Leading and trailing icons change their position based on LTR or RTL contexts.

Images that are 24dp in height can be placed inside of text fields. This image height allows for optimal top and bottom padding within the field and is consistent with icon size recommendations.

- **Icon signifier
**Icon signifiers can describe the type of input a text field requires, and be touch targets for nested components. For example, a calendar icon may be tapped to reveal a date picker.
- **Valid or error icon
**Iconography can indicate both valid and invalid inputs, making error states clear for colorblind users.
- **Clear icon
**Clear icons let a person clear an entire input field. They appear only when input text is present.
- **Voice input icon
**A microphone icon signifies that people can input characters using voice.
- **Dropdown icon
**A dropdown arrow indicates that a text field has a nested selection component.
- **Image
**An image can help contextualize the required input text such as a credit card number.

[IMAGE] Side by side view of text fields with different icons and images as trailing elements within the container.

_- Icon signifier
- Valid or error icon
- Clear icon
- Voice input icon
- Dropdown icon
- Image_

##### Read-only fields

Read-only text fields display pre-filled text that people cannot edit.

A read-only text field is styled the same as a regular text field and is clearly labeled as read-only.

[IMAGE] Read only filled text field.

_A filled read-only text field_

[IMAGE] Read only outlined text field.

_An outlined read-only text field_

#### Adaptive design

As layouts adapt to larger screens and different breakpoints, apply flexible container dimensions to text fields. Set minimum and maximum values for margins, padding, and container dimensions as layouts scale so that typography adjusts for better reading experiences.

[IMAGE] UI for creating a new album in a side by side view on mobile and tablet.

_For compact breakpoints, text fields can span the full width of the display. For medium and expanded breakpoints, text fields should be bound by flexible margins or other containers._

As text fields expand in fluid layouts, avoid maintaining fixed margins and typography properties. This can lead to extra long text fields.

For example, text fields should not span the full width of a large screen.

[IMAGE DON'T] Tablet UI with text fields spanning the complete width of the screen.

**DON'T:** _Don’t use fixed text field margins on large devices. Text fields shouldn’t span the full width of a large screen._

##### Density

Dense text fields enable people to scan and take action on large amounts of information.

[IMAGE] Tablet UI with desne text field as part of event creation form.

_A form with dense text fields_

###### **Avoid applying density by default**

Don't apply density to text fields by default. This lowers their targets below the recommended 48x48 CSS pixels. Instead, give people a way to choose a higher density, like selecting a denser layout or changing the theme.

To ensure this density setting can be easily reverted when it's active, keep all the targets to change it at a minimum of 48x48 CSS pixels each.

## Accessibility

#### Use cases

User should be able to:
- Navigate to and activate a text field with assistive technology
- Input information into the text field
- Receive and understand supporting text and error messages
- Navigate to and select interactive icons

#### Interaction & style

The containers for both filled and outlined text fields provide the same functionality. Changes to color and thickness of stroke help provide clear visual cues for interaction.

[IMAGE] 2 filled text fields in enabled empty state and in focused populated state have visual cues to identify their state.

_Filled text fields_

[IMAGE] 2 outlined text fields in enabled empty state and in focused populated state have visual cues to identify their state.

_Outlined text fields_

Containers improve the discoverability of text fields by creating contrast between the text field and surrounding content.

In some contexts, outlined text fields can improve the perception of the fields with a 3:1 or greater contrast ratio between the container outline and the background.

[IMAGE DO] An outlined text field with label text that passes the minimum contrast of 3:1.

**DO:** _Make sure the container outline has a minimum contrast of 3:1 to the background_

[IMAGE DON'T] An outlined text field with label text fails the minimum 3:1 contrast.

**DON'T:** _Don't choose colors that won't pass Material's minimum contrast of 3:1_

#### Keyboard navigation

|
Keys |
Actions
| Tab | Focus lands on (non-disabled) text field

#### Labeling elements

If the UI text is correctly linked, assistive tech (such as a screenreader) will read the UI text followed by the component’s role.

The accessibility label for a text field is the same as the text field label.

[IMAGE] The text field  and accessibility label both read “Email.” The role is “textbox.”

_A text field’s label should include its UI text_

For text fields with interactive trailing icons, the accessibility label clarifies its function.

For example, when a password is hidden, the label for the view icon is "Show password," and when the password is visible, the label is "Hide password."

When an icon has no actionable role, like an error icon, the label is "Error."

[IMAGE] The trailing icon’s accessibility label “Show Password.” The role is “Button.”

_When a trailing icon in the field acts as a button, the label should clarify function, while the role explains the component type_

The prefix and suffix of a text field provides symbols and abbreviations to help users enter the correct values.

The accessibility label for prefix and suffix needs to have a unique id attribute, for example, the currency name for a currency symbol prefix.

[IMAGE] Text field accessibility labels “UI text” are “Euro” for a currency prefix and “At gmail dot com” for the email address suffix.

_A form containing fields with both a prefix for currency, and a suffix for email address_

When there is an error, "alert" is applied to the role and the error message to the text label.

If a text field displays both supporting text and error text, the label should include the supporting text first, followed by the error text.

[IMAGE] The text field accessibility labels is: UI text “Not a valid ZIP code.” The role is “Alert.”

_Text field error messages should be given an “alert” role in accessibility labels_

The accessibility label for the character counter clarifies the number of characters that can be entered into the text field.

[IMAGE] A character counter's accessibility label  reads: UI text (“Character count, 5/20”)

_The remaining character counter should be called “character count” within the label_

The text displayed in the supporting text is also used for its accessibility label.

[IMAGE] The accessibility label uses the supporting text. It reads: UI text (“Please use the company email address”). Role [No role].

_Text field supporting text should have its own accessibility label_

If a text field requires input, indicate so with an asterisk at the end of the text field label. The accessibility label must include the asterisk.

[IMAGE] Accessibility label reads: UI text (“Username*”).  The role is “Textbox.”

_A required text field’s accessibility label should include any supporting text_
