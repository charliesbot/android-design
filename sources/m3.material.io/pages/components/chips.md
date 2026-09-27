# Chips
Source: https://m3.material.io/components/chips

## Overview

- Use chips to show options for a specific context
- Four variants: assist, filter, input, and suggestion
- Chip elevation defaults to 0 but can be elevated if they need more visual separation

[IMAGE] 4 chip variants.

_- Assist chip
- Filter chip
- Input chip
- Suggestion chip_

#### Availability & resources

#### Updates

**Aug 2024**

Updated stroke color from **outline** to **outline variant**.

[IMAGE] A chip with a clear outline is now a chip with a subtle outline.

_The stroke color was softened to improve visual hierarchy between chips and buttons_

#### Differences from M2

- Color: New color mappings and compatibility with dynamic color
- Shape: Rounded rectangle
- Variants: Action chips have been separated into assist chips and suggestion chips. Choice chips are now a subset of filter chips

[IMAGE] M2 chip variants.

_M2: Variants of chips are input, choice, filter, and action chips_

[IMAGE] M3 chip variants.

_M3: Variants of chips updated to assist, filter, input, and suggestion chips_

## Specs

#### Tokens & specs

Select a component variant below to see its elements, attributes, tokens, and values.

#### Assist chip

[IMAGE] Assist chip diagram numbering 3 elements.

_- Container
- Label text
- Leading icon_

##### Assist chip color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] Assist chip diagram numbering 4 color elements.

_Assist chip color roles used for light and dark themes:
- Surface container low (optional)
- On surface
- Outline
- Primary_

##### Assist chip states

States are visual representations used to communicate the status of a component or interactive element. Learn more about interaction states

[IMAGE] 36 assist chips illustrating combinations of styles, selection and non-selection, and 6 interaction states.

_Selected and unselected assist chip states:
- Enabled
- Disabled
- Hovered
- Focused
- Pressed
- Dragged_

##### Assist chip measurements

[IMAGE] 3 assist chips with measurements shown for variants with and without a leading icon.

_Assist chip padding and size measurements_

| Attribute
| Value

| Height
| 32dp
| Shape
| 8dp corner radius
| Icon size
| 18dp
| Vertical label text alignment
| Center-aligned
| Horizontal label text alignment
| Start-aligned
| Left/right padding
| 16dp
| Left/right padding with icon
| 8dp
| Padding between elements
| 8dp

#### Filter chip

[IMAGE] Filter chip diagram numbering 4 elements.

_- Container
- Label text
- Leading icon
- Trailing icon_

##### Filter chip color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] Filter chip diagram numbering 4 color elements.

_Filter chip color roles used for light and dark themes:
- On surface variant
- On secondary container
- Secondary container
- Outline variant
- Surface container low (optional)_

##### Filter chip states

States are visual representations used to communicate the status of a component or interactive element. Learn more about interaction states

[IMAGE] 24 filter chips showing combinations of elevated, non-elevated, selected, and non-selected styles, and 6 interaction states.

_Selected and unselected filter chip states:
- Enabled
- Disabled
- Hovered
- Focused
- Pressed
- Dragged_

##### Filter chip measurements

[IMAGE] 3 filter chips with measurements shown for types with and without a leading icon and trailing icon.

_Filter chip padding and size measurements_

| Attribute
| Value

| Container height
| 32dp
| Container shape
| 8dp corner radius
| Icon size
| 18dp
| Vertical label text alignment
| Center-aligned
| Horizontal label text alignment
| Start-aligned
| Left/right padding
| 16dp
| Left/right padding with icon
| 8dp
| Padding between elements
| 8dp

#### Input chip

[IMAGE] Input chip diagram numbering 4 elements.

_- Container
- Label text
- Trailing icon
- Leading icon_

##### Input chip color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] Input chip diagram numbering 5 color elements.

_Input chip color roles used for light and dark themes:
- On surface variant
- Surface container low (optional)
- On surface variant
- On surface variant
- Outline variant
- Primary
- Secondary container
- On secondary container
- On secondary container_

##### Input chip states

States are visual representations used to communicate the status of a component or interactive element. Learn more about interaction states

[IMAGE] 33 input chips illustrating combinations of styles, selection and non-selection, and 6 interaction states.

_Selected and unselected input chip states:
- Enabled
- Disabled
- Hovered
- Focused
- Pressed
- Dragged_

##### Input chip measurements

[IMAGE] 2 input chips with measurements: 1 with a trailing icon only; 1 with an avatar as a leading icon and a trailing icon.

_Input chip padding and size measurements_

| Attribute
| Value

| Container height
| 32dp
| Container shape
| 8dp corner radius
| Icon size
| 18dp
| Avatar shape
| 12dp corner radius
| Avatar size
| 24dp
| Vertical label text alignment
| Center-aligned
| Horizontal label text alignment
| Start-aligned
| Left padding for avatar
| 4dp
| Right padding for avatar
| 8dp
| Left/right padding for icon
| 8dp
| Padding between elements
| 8dp
| Target size for close icon
| Min 48dp

#### Suggestion chip

[IMAGE] Suggestion chip diagram numbering 2 elements.

_- Container
- Label text_

##### Suggestion chip color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] Suggestion chip diagram numbering 3 color elements.

_Suggestion chip color roles used for light and dark themes:
- Outline
- Surface container low (optional)
- On surface variant_

##### Suggestion chip states

States are visual representations used to communicate the status of a component or interactive element. Learn more about interaction states

[IMAGE] 24 suggestion chips illustrating combinations of styles across 6 interaction states.

_Selected and unselected suggestion chip states:
- Enabled
- Disabled
- Hovered
- Focused
- Pressed
- Dragged_

##### Suggestion chip measurements

[IMAGE] 2 suggestion chips with measurements shown for variants with and without a leading icon.

_Suggestion chip padding and size measurements_

| Attribute
| Value

| Container height
| 32dp
| Container shape
| 8dp corner radius
| Icon size
| 18dp
| Vertical label text alignment
| Center-aligned
| Horizontal label text alignment
| Start-aligned
| Left/right padding without icon
| 16dp
| Left/right padding with icon
| 8dp
| Padding between elements
| 8dp

## Guidelines

[IMAGE] 4 chip variants: Assist, filter, input, and suggestion.

_There are four variants of chips: Assist, filter, input, and suggestion_

#### Usage

Chips help people enter information, make selections, filter content, or trigger actions. They're best used to help people accomplish their current task faster and easier.

[IMAGE] 3 assist chips in restaurant review app.

_Chips appear as a group of interactive elements_

##### Chips aren’t buttons

Chips and buttons are similar. They both provide visual cues to prompt people to take actions and make selections.

While buttons appear consistently and with familiar calls to action, chips are dynamic to the situation, and appear as a group of interactive elements.

Use chips to enhance a person's current journey and encourage action. Use buttons to progress them through the product and for significant actions.

[IMAGE DO] Chips used for “Reserve a table” and “Order delivery” in a restaurant app.

**DO:** _Use chips to present contextual, supplemental options_

[IMAGE DON'T] Chips used for Save and Cancel actions in a dialog.

**DON'T:** _Avoid replacing major actions with chips. Actions that progress people to the next or previous step should always be displayed as buttons._

Chips should dynamically offer various actions depending on the current task, whereas a button should be a persistent fixture of a layout.

Chips represent forking paths for a current task, while buttons represent linear steps.

[IMAGE DO] Button labeled “Save task” in a task management app.

**DO:** _Use buttons for the final step in a task_

[IMAGE DON'T] Chip labeled “Save task” in a task management app.

**DON'T:** _Avoid using chips to finish or progress a task_

Multiple chips should appear together in a set, whereas there should be no more than 3 buttons in a single arrangement.

Chip sets can be scrolled horizontally.

[IMAGE DO] Multiple chips in event reminder.

**DO:** _Chips can be scrolled horizontally_

[IMAGE DON'T] Single chip in event reminder.

**DON'T:** _Don’t display a single chip by itself. Chips should appear in a set._

##### Variants

Choose the chip variant based on its purpose and author.

Does the chip represent an action (assist chip) or filter results (filter chip)?

Is its content generated by the product (suggestion chip), or by the person using the product (input chip)?

[IMAGE] 4 chip variants.

_There are four chip variants:
- Assist
- Filter
- Input
- Suggestion_

|

Purpose |

Chip variant |

Rationale |

Example
|

Action |

Assist chip |

Assist chips represent smart or automated actions that can span multiple apps |

**Add to calendar** action
|

Filter |

Filter chip |

Filter chips represent filters for a collection |

Platform selector on material.io/components
|

Information, user-authored |

Input chip |

Input chips represent discrete pieces of information entered by a person |

Gmail contact in the **To** field
|

Information, product-authored |

Suggestion chip |

Suggestion chips help narrow a person’s intent by presenting dynamically-generated suggestions |

Suggested chat response

#### Anatomy

[IMAGE] Diagram of 4 elements of a chip.

_- Container
- Label text
- Leading icon or image (optional)
- Trailing icon (required for input chips, optional for filter chips)_

##### Container

All chips are slightly rounded with an 8dp corner.

[IMAGE] Diagram of rounded corner of chip container.

_Chips have rounded corners_

**Shadows & elevation**

Chip containers can be elevated if placed on top of an image or dynamic background.

[IMAGE] 3 elevated assist chips above image of restaurant photograph.

_When on complicated backgrounds, chip containers can be elevated_

[IMAGE DO] 3 variants of chips in an outlined style.

**DO:** _Use an outline to define the edge of the chip's container on regular backgrounds_

[IMAGE DO] 3 variants of chips in an elevated style above restaurant photograph.

**DO:** _Chips may use elevation when placed on an image_

[IMAGE DON'T] 4 elevated chips above blank background.

**DON'T:** _Chips shouldn't be elevated when placed directly on the page_

[IMAGE DON'T] Elevated chip in pressed state.

**DON'T:** _Avoid using elevation to indicate a chip's pressed state. Instead, use the visual ripple effect._

##### Label text

Chip label text should be 20 characters or fewer, and have the same typography style as buttons.

Chip labels should remain brief for the limited space available. Skip conventional grammar rules, such as articles (take "a" walk), to save space.

[IMAGE DO] Filter chip in maps app labeled “Walking.”

**DO:** _Keep chip labels short_

[IMAGE DON'T] Filter chip in maps app labeled “Walking navigation session."

**DON'T:** _Avoid chip labels longer than 20 characters_

##### Leading icon or image (optional)

Chips can contain a leading icon, logo, or circular image. Use a system icon to help identify a chip's category.

[IMAGE] 2 input chips, 1 with a leading circular avatar and the other with a leading icon.

_Chips can contain a logo, icon, or circular image_

The leading icon color for unselected chips can be customized through theming. While the default color role is **primary**, the **on surface variant** color role is a good alternative when the icon style requires less emphasis.

[IMAGE] 2 screens of a restaurant page with chips labeled "Add photos" and "Add dishes." The leading icons are purple in 1, grey in the other.

_**Primary** color (left) and **on surface variant** color (right)_

Leading circular images are sized larger than leading icons to provide more space for detail. Icons are designed to be legible at small sizes.

See the Specs tab for precise measurement values.

[IMAGE] 2 input chips, 1 with a 24dp leading avatar and the other with an 18dp leading icon.

_Leading images, such as avatars, are sized larger than leading icons or logos_

##### Trailing icon (optional, input and filter chips only)

The trailing icon is present for input and filter chips.

On input chips, it's required and must be used to remove the chip. On filter chips, it's optional, and can be used to open a menu or remove the chip.

Secondary actions (such as a trailing icon button for **Remove**) must have a 48x48dp interaction target that doesn’t interfere with the chip's primary action (such as **Edit** or **Drag**). To achieve this, apply a minimum width of 88dp to the chip, or 42dp to the label text.

[IMAGE] A chip with 88dp min width has 2 touch targets of 48x48dp.

_Interaction targets for actions like **Edit** or **Close** have a size of 48x48. This can be achieved by setting the minimum container width to 88dp._

#### Assist chips

Assist chips represent smart or automated actions that can span multiple apps, such as opening a calendar event from the home screen. Assist chips function as though the person asked an assistant to complete the action. They should appear dynamically and contextually in a UI.

An alternative to assist chips are buttons, which should appear persistently and consistently.

[IMAGE] 2 assist chips labeled “Add to my itinerary” and “12 mins from hotel” on map screen about Los Cantantes.

_The text in most assist chips begins with a short verb, like **Get **or **Add**_

[IMAGE] 2 assist chips labeled “Event location” and “Chat” on phone lock screen.

_An assist chip can surface supplemental information like a calendar event, as well as provide contextual actions_

[IMAGE] Assist chip transformed into time picker with radio buttons.

_During an interaction, assist chips can transform into modals, transition into full-screen views of new content, or readjust to display more results inline_

Assist chips can trigger an action or show progress and confirmation.

Write assist chips like buttons: start with a verb. Adjust text dynamically if the state changes, like **Save** to **Saved**.

[VIDEO] 3 assist chips labeled “Mail,” ”Messenger”, and “Video Chat” on a travel article.

_Tapping an assist chip triggers a contextual action_

[VIDEO] Assist chip showing confirmation upon selection.

_Assist chips can show progress and confirmation feedback_

Assist chips are displayed after primary content, such as below a card or persistently at the bottom of a screen.

[IMAGE] 3 assist chips labeled “Turn on lights”, “Set alarm”, and "Close Blinds" on Welcome Home screen.

_Assist chips should be shown underneath primary content_

#### Filter chips

Filter chips use tags or descriptive words to filter content. They can be a good alternative to segmented buttons or checkboxes when viewing a list or search results.

Tapping on a filter chip activates it and appends a leading checkmark icon to the starting edge of the chip label.

Write filter chips with nouns that describe the category to **include** in the results. Avoid negative phrases like **Exclude images**.

[IMAGE] 4 filter chips labeled “Docs” “Slides” “Sheets” and “Images” with “Slides” and “Sheets” selected

_Filter chips rely on tags or descriptive words to filter content_

[IMAGE] 5 filter chips representing popular electronics categories in a shopping app. The chip “Game consoles” is selected.

_Filter chips in a shopping app_

[IMAGE] 6 filter chips representing apartment amenities in a real estate app. 2 of the chips are selected.

_Filter chips in a real estate app_

Tap a chip to select it. Multiple chips can be selected or unselected.

[IMAGE] 6 filter chips representing apartment amenities in a real estate app. 3 selected chips include checkmarks while 3 don’t.

_An icon can be added to indicate when a filter chip is selected_

[IMAGE] 3 amenities chips are selected, and 2 neighborhoods.

_Filter chip suggestions can dynamically change as a person starts to select filters_

Alternatively, a single chip can be selected. This offers an alternative to segmented buttons, radio buttons, or single select menus.

However, avoid mixing chip set behaviors. All chip sets on a page should be either single-select or multi-select.

[VIDEO] filter chips labeled “Extra soft”, “Soft”, “Medium”, and “Hard” First, the “Extra soft” chip is selected, then it unselects as the “Medium” chip is selected.

_Filter chips can be set so that selecting a single chip automatically deselects all other chips in the set_

In medium and expanded breakpoints, filter chips may contain a trailing icon to directly remove the chip or open a menu of options.

In compact windows, the trailing icon's target area is too small to be accessible on its own. However, if the whole chip can be selected to accomplish the action, the chip is likely still accessible.

[IMAGE] Filter chip with trailing remove icon.

_The remove icon helps users remove the filter_

[IMAGE] Filter chip with trailing menu icon.

_Filter chips can open a menu for more filtering options_

When combined with a menu, the filter chip opens a list of selectable options.

[VIDEO CAUTION] In a trail selector mobile app, a person selects the whole chip to change the menu selection from Walking to Cycling.

**CAUTION:** _In compact windows, make sure the whole chip opens the menu. Otherwise, the target area is likely too small to be accessible._

Filter chips can be used with other components, such as search fields and sheets.

[IMAGE] 4 filter chips below search field

_Filter chips can be shown underneath a search field_

[IMAGE] Filter chips used as tags in a side sheet on a file application.

_Use a side sheet to organize many filter chips_

[IMAGE] 6 filter chips wrapping to 2 rows

_Filter chips can wrap to a new row. If there are more than two rows, consider using horizontal scrolling to access them all._

[IMAGE] 4 filter chips with right-most chip partly offscreen, indicating the chips are horizontally scrollable

_Filter chips can scroll horizontally to show many options_

[IMAGE DON'T] Single filter chip option beneath the header “Category”

**DON'T:** _Filter chips should not present only a single option_

#### Input chips

Input chips represent discrete pieces of information entered by a person, such as Gmail contacts or filter options within a search field.

They enable user input and verify that input by converting text into chips.

[IMAGE] Contacts transformed into 2 input chips in a draft event invite

_Input chips transform text based on a person's input_

Input chips can support editing to change their contents, such as correcting an email address. In edit mode, the chip reverts back to a text string. Editing can be triggered by interacting with the chip, either by selecting it or by a second interaction after selection.

[VIDEO] Contact transformed into input chip in the “To” filed of an email draft.

_Input chips converted from email addresses are editable_

[VIDEO] Animation showing selection of chip with backspace key.

_Using the backspace key with the cursor before a chip selects the entire chip. The chip can then be deleted when the user taps the backspace key again._

A single field can contain multiple input chips. These chips can be reordered or moved into other fields.

[IMAGE] 2 input chips in the “To” field of an email draft

_Multiple input chips in one field_

[IMAGE] 1 input chip moved from the “To” field to the “CC” field of an email draft

_Input chips being moved from one field to another_

Input chips can expand to show more information or options. A container transform transition pattern is used to reveal additional content.

[VIDEO] Contact input chip expands to show multiple email addresses for that contact

_Input chips can expand_

##### Placement

Input chips can be integrated with other components.

They can appear:
- Inline with the cursor in a text field
- In a stacked list
- In a list that can be horizontally scrolled

[IMAGE] 4 input chips wrapping to 2 rows.

_Input chips can wrap to a new row if all chips need to be visible_

[VIDEO] 3 input chips horizontally scrolling.

_Input chips can scroll horizontally_

##### Icons & images

The leading icon of input chips can be an icon, logo, or circular image.

[IMAGE] 2 input chips, 1 with leading thumbnail, 1 with leading icon.

_Input chips can contain an icon, logo, or circular image_

The trailing icon is always aligned to the end side of the container. It’s placed:
- On the right for left-to-right (LTR) languages, such as English
- On the left for right-to-left (RTL) languages, such as Farsi

[IMAGE] 2 input chips with trailing icons following the label text.

_Input chips can be a more flexible way to filter search results, compared to filter chips_

[IMAGE] 3 input chips with trailing icons following the label text.

_Input chips make it easier to add and remove contacts_

[VIDEO] Animation of overflowed chips in a text field.

_Overflowed chips in a text field should follow the same behavior as regular text. An unfocused text field with overflowed content should display the beginning of the input. Tapping the field snaps the user to the end of the input with the cursor and keyboard active._

#### Suggestion chips

Suggestion chips help narrow a user’s intent by presenting dynamically generated suggestions, such as possible responses or search filters.

Write suggestion chips as nouns or short phrases, depending on context. Avoid exceeding 20 characters when possible.

[IMAGE] 3 suggestion chips offering ID options in a photo-recognition app.

_The text labels within suggestion chips are most often nouns or short phrases_

[IMAGE] 3 suggestion chips with automated reply options in a chat app.

_Suggestion chips can offer quick-reply options in a chat or email app_

[IMAGE] 2 suggestion chips with search options “What song is this?” and “What are the lyrics?”

_A suggestion chip can help the user start a search_

##### Placement

When displaying multiple chips together, place them inline as a row of options, not listed vertically. Overflowing chips should break to the next line.

If the field is only one row high, chip sets can scroll horizontally instead.

Keep an 8dp minimum space between chips. Chips must also have a minimum 48dp target size, regardless of placement or density. If required, the target can extend beyond the visible container of the chip.

Text labels for chips should be concise. Chip labels will truncate when in a wrapped layout, and when they are wider than the full width of the window.

[IMAGE] Diagram of margins between a set of chips.

_- Margins between chips
- Margin between each line_

## Accessibility

#### Use cases

People should be able to do the following with assistive technology:
- Use a chip to perform an action
- Navigate to a chip
- Activate a chip

#### Interaction & style

The chip label needs at least 3:1 contrast with the background.

A chip that performs an action should present the same semantics as a button to a platform's accessibility API.

[IMAGE] The chip label needs to pass 3:1 contrast.

_High contrast helps differentiate chips clustered together_

##### Horizontal overflow

When there are too many chips to fit on one row, provide a way to display them all at once and avoid scrolling.

**Reflow method: **Use a filter chip as a leading element to reflow the horizontal list. This should shift down the content below and make room for all chips to show.

[VIDEO] Selecting a leading button pushes chips previously reachable only by scroll below, making them all visible.

_The **Show all **filter chip is used to reflow the list, displaying all chips at once and pushing down the content below_

**Menu method: **Create a leading button to display all chip options in a menu. Use this option to avoid shifting the position of the content below.

Don’t use the menu method on chips with a second action, like a remove icon.

[VIDEO] Selecting a filter button produces a menu with all previously hidden chips visible.

_The **Show all** leading button shows a menu of chip options, keeping the place of content below_

##### Avoid applying density by default

Don't apply density to chips by default — this lowers their targets below our best practice of 48x48 CSS pixels. Instead, give people a way to choose a higher density, like selecting a denser layout or changing the theme.

To ensure that this density setting can be easily reverted when it's active, keep all the targets to change it at minimum 48x48 CSS pixels each.

#### Keyboard navigation

| Keys | Actions
| **Tab** | Moves focus to enabled chip or chip group
| **Space** or **Enter** | Activates, selects, or deselects the focused chip
| **Backspace** or **Delete** | Removes currently focused input chip
| **Arrows** | Moves focus between chips

#### Labeling elements

|

Element |

A11y label |

Role (Web) |

Role (Android Views (MDC-Android)) |

Role (Jetpack Compose)
|

Image / Icon within chip |

Hide image |

- |

- |

-
|

Basic chip (one action) |

“{chip content}” |

gridcell |

button |

button
|

Selectable chip |

“{chip content}” |

gridcell |

radio button |

checkbox
|

Remove icon (no other action) |

“Remove {chip content}” |

- |

- |

-
|

Two actions (e.g., select + remove) |

“{chip content}.” Then

“Remove {chip content}”. |

button or checkbox |

button or checkbox |

button or checkbox

The accessibility label for a chip is the chip's label text. Additional actions, like remove, are labeled separately.

[IMAGE] Accessibility tags for a chip.

_Accessibility tags should include both the label and role_

##### Multi-select

For multi-select chip sets, **Space** or **Enter** will select the focused chip and allow you to select all of the chips. **Space** or **Enter** will also deselect a focused selected chip.

[IMAGE] Accessibility tags for a multi-select chip.

_While multiple chips can be selected, only one can be in focus_

##### Drop-down list

The accessibility label should align with each list item’s text label.

For list items with text and an icon, the accessibility label should be marked as decorative to avoid redundant verbalizations.

[IMAGE] Accessibility tags for a drop-down list chip.

_The accessibility label should be the text label_

##### Input chip remove action

Display the remove icon whenever a chip can be removed. On mobile, if remove is the only chip action, the remove icon isn't necessary. Instead the chip can be removed by selecting it and pressing the **Delete** key on the keyboard.

Each chip is a focusable element.
- If a chip only has a remove icon, the entire chip and icon are one focusable element.
- If a chip has a second action, like select, then the chip content and remove icon are two separate focusable elements.

[IMAGE] A focused remove action within a chip.

_The remove action is focused when the chip can also be selected_

##### Showing chip interactivity

Material requires that chips use a secondary indicator to show that they are interactive in context, allowing users with low vision and cognitive disabilities to see them.

Use one of the following methods:

- Add a label before the chip group suggesting interaction, such as **Select type**

[IMAGE] Label saying “Select type” above a series of chips, with “Extra soft” selected.

_Labels introducing a chip group can indicate that they are selectable_

- Provide interactive page context, such as **Filter results**, indicating chips can be selected to narrow results

[IMAGE] Page context saying “Filter results” above chips listing different categories.

_Page context can indicate how search results will be narrowed by selecting chips_

- Use the **outline** color role, instead of **outline variant**, to ensure a minimum 3:1 contrast
- Include an interactive chip label, such as **Turn on lights**, or leading icon

[IMAGE] A selectable chip with more pronounced outline stroke.

_Chips can show they are interactive with a darker outline color stroke_

[IMAGE] A chip labeled “Turn on lights” with a leading icon related to lights.

_Chips can also use a leading icon or label to show interactivity_
