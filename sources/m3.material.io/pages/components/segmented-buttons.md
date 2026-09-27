# Segmented buttons
Source: https://m3.material.io/components/segmented-buttons

## Overview

star

Note:
Segmented buttons are no longer recommended in the Material 3 expressive update. For those who have updated, use the connected button group instead, which has mostly the same functionality but with an updated visual design.

- Segmented buttons can contain icons, label text, or both
- Two variants: single-select and multi-select
- Use for simple choices between two to five items (for more items or complex choices, use chips)

[IMAGE] Two variants of segmented buttons.

_- Single-select segmented button
- Multi-select segmented button_

#### Availability & resources

#### M3 Expressive update

**May 2025**

The segmented button is no longer recommended. Use the connected button group instead. More on M3 Expressive

#### Differences from M2

- **Color:** New color mappings and compatibility with dynamic color
- **Icons:** Optional check icon to indicate selected state
- **Layout:** Taller container height of 40dp
- **Name and variants:** Segmented buttons were previously known as toggle buttons. They now have two official variants: single-select and multi-select.
- **Shape:** Fully rounded corners
- **Typography: **Labels use sentence case instead of all caps

[IMAGE] Diagram indicating the fully rounded corner radius of a segmented button.

_Segmented buttons now have a container height of 40dp_

[IMAGE] Segmented buttons with M2 color mappings, all caps text labels, boxy shape, and shorter height.

_M2: Segmented buttons had a small corner radius and label text in all caps_

[IMAGE] Segmented buttons with M3 color mappings, sentence case text labels, fully round shape, and taller height.

_M3: Segmented buttons have fully rounded corners, sentence-case text, different height, and new color mappings_

## Specs

star

Note:
Segmented buttons are no longer recommended in the Material 3 expressive update. For those who have updated, use the connected button group instead, which has mostly the same functionality but with an updated visual design.

[IMAGE] Diagram of segmented button indicating 3 parts of its anatomy.

_- Container
- Icon (optional for unselected state)
- Label text_

#### Tokens and specs

Browse the component elements, attributes, tokens, and their values. Learn more about design tokens

#### Color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] Diagram of segmented button indicating its color mappings

_Segmented button color roles used for light and dark schemes:
- On surface
- Outline
- Secondary container
- On secondary container_

#### States

States are visual representations used to communicate the status of a component or interactive element. Learn more about interaction states

##### Unselected

[IMAGE] Side by side view of segmented buttons with 5 unselected states.

_Unselected button states:
- Enabled
- Disabled
- Hovered
- Focused
- Pressed_

##### Selected

[IMAGE] Side by side view of segmented buttons with 4 selected states.

_Selected button states:
- Selected
- Hovered on selected
- Focused on selected
- Pressed on selected_

#### Measurements

[IMAGE] Diagram indicating layout values, paddings, and target size for segmented buttons

_- Padding and container size
- Target size_

| Attribute | Value
| Container width
| Dynamic based on labels
| Segment width | Container width / total segments (Example: 1/3)
| Height
| 40dp
| Outline width | 1dp
| Label alignment
| Center
| Left/right padding
| Min 12dp
| Padding between elements
| 8dp
| Target size | 48dp

##### Density

Density can be used in denser UIs where space is limited. Density is only applied to the height.

[IMAGE] Side by side view of segmented buttons with 4 different density heights

_Each step down in density removes 4dp from the height_

## Guidelines

star

Note:
Segmented buttons are no longer recommended in the Material 3 expressive update. For those who have updated, use the connected button group instead, which has mostly the same functionality but with an updated visual design.

[IMAGE] Two types of segmented buttons.

_- Single-select
- Multi-select_

#### Usage

Segmented buttons help people select options, switch views, or sort elements.

[IMAGE] A segmented button for switching between restaurants and bar options.

_A segmented button can help switch between viewing restaurant and bar options_

There are 2 variants of segmented buttons:
- Single-select
- Multi-select

[IMAGE] Side by side view of single and multi-select segmented buttons

_- Single-select segmented button can only have 1 segment selected
- Multi-select segmented button can have multiple segments selected_

#### Anatomy

[IMAGE] Diagram of segmented button indicating 5 parts of its anatomy

_- Segment
- Container
- Icon (optional)
- Label text (optional)
- Selected icon_

##### Segments

Segmented buttons can have 2-5 segments. Each segment is clearly divided and contains label text, an icon, or both.

[IMAGE] Side by side view of segmented buttons each with additional segment starting from 2 to 5

_There can be anywhere from 2 to 5 segments in single-select and multi-select segmented buttons_

[IMAGE DO] Mobile UI of data usage screen with segmented button

**DO:** _Segmented buttons are best used for selecting between 2 and 5 choices_

[IMAGE DON'T] Incorrect use of segmented button with 6 segments

**DON'T:** _Don’t use more than five segments in a single segmented button. Choices should be scoped. If you have more than five choices, consider using another component, such as chips._

##### Container

Like common buttons, segmented buttons have fully rounded corners by default.

[IMAGE] Close up detail of segmented button with fully rounded corners

_Segmented buttons have fully rounded corners_

##### Icons

Icons may be used as labels by themselves or alongside text.

If an icon is used without label text, it must clearly communicate the option it represents.

[IMAGE] Side by side view of segmented buttons with different configurations of icons and label text

_Segmented buttons can include icons_

##### Label text

Labels should be short and succinct. If a label is too long to fit within its segment, consider using an icon alone.

[IMAGE] Mobile UI of music app showing a segmented button with options for music, albums, podcasts

_Use labels that are as clear and short as possible_

[IMAGE DO] Segmented button with options for day, week, month

**DO:** _Keep labels short and consistent in length_

[IMAGE DON'T] Segmented button with 4 segments. 3 are next to each other. The 4th is wrapped on a new line.

**DON'T:** _Don’t allow segments to wrap onto a new line_

[IMAGE DO] Segmented button with text labels reading day, week, month

**DO:** _Use consistent label types_

[IMAGE CAUTION] Segmented button with icons only labels for walking, transit, driving

**CAUTION:** _Icons can be used in place of labels, but they must clearly communicate their meaning_

[IMAGE DON'T] Segmented button with 2 icon only options indicating favorite and bookmark and 3rd option with text label reading recent

**DON'T:** _Avoid mixing icon-only labels with text labels. Choose one label type and use that type for all segments._

#### Single-select

Use a single-select segmented button to select one option from a set, switch between views, or sort elements from up to five options.

For example, use a single-select segmented button to choose one of a set of sizes, such as this beverage size selector.

[IMAGE] Mobile UI for ecommerce app with segmented button with 3 beverage size options

_A single select segmented button for choosing beverage size_

#### Multi-select

Use a multi-select segmented button to select or sort from two to five options. Unlike single-select, selection is not required and a user may concurrently select anywhere from all to none of the options.

For example, multi-select segmented buttons can be used to filter by price range when searching for a restaurant.

[IMAGE] Mobile UI for ecommerce app with multi-select segmented button with 4 price range options

_A multi-select segmented button for filtering restaurant search options_

#### Placement

Segmented buttons should have adequate margins from the edge of the viewport or frame.

On larger screens, set a maximum padding for all button segments so the set doesn't fill the screen.

[IMAGE DO] Mobile UI with 2-segment segmented button and 4-segment segmented button each with same margins to the viewport edge.

**DO:** _Allow adequate space for margins. The button container shouldn’t reach the edge of the viewport._

[IMAGE DO] Game store UI with a segmented button the proper width

**DO:** _Set a maximum padding within the segments to ensure usability on larger screens_

[IMAGE DON'T] Game store UI with a segmented button improperly spanning the entire width of the screen

**DON'T:** _Don’t allow segmented buttons to span the full width of larger screens or panes. This can leave too much padding on either side of the segment label, making the button less usable._

Segmented buttons can be placed on other components, such as bottom sheets or full-screen dialogs.

[IMAGE] Mobile UI with segmented button in bottom sheet

_A segmented button can be placed on a bottom sheet_

#### Behavior

When using both icons and label text in segmented buttons, the icon label is replaced by the checkmark icon when the segment is selected.

[VIDEO] Segmented button with icon and text labels with checkmark icon in selected segment

_Icons become checkmarks when selected in buttons that also use label text_

## Accessibility

star

Note:
Segmented buttons are no longer recommended in the Material 3 expressive update. For those who have updated, use the connected button group instead, which has mostly the same functionality but with an updated visual design.

#### Use cases

Users should be able to:
- Navigate to and activate segmented buttons with assistive tech
- Understand what each segment selection will do

##### Interaction & style

For keyboard navigation, **Tab** navigates to the button, and arrow keys navigate between the individual segments.

For single-select segments, **Space** or **Enter** will select or unselect the focused segment.

For multi-select segments, **Space** or **Enter** will:
- Select an unselected segment
- Select all of the segments
- Unselect a selected segment

[IMAGE] Tab moves the focus through segments of a segmented button.  Space or enter selects and unselects segments.

_Use **Tab** to navigate through segments, and **Space **or **Enter** to select and unselect_

##### Color contrast

Segmented buttons are clusters of similar components, so the outline should have at least a 3:1 contrast ratio with the background or surface. This helps distinguish each button.

Both a checkmark icon and a color change are used to distinguish selection. Make sure color isn’t the only way to show selection.

[IMAGE DO] The outline of segmented buttons on the surface passes the minimum contrast of 3:1.

**DO:** _Use an outline with a surface contrast of at least 3:1_

[IMAGE DON'T] The outline of segmented buttons on the surface fails the minimum contrast of 3:1.

**DON'T:** _The segmented button shouldn't have a contrast outline less than 3:1_

##### Initial focus

Focus will start in the first segment. Depending on the direction of the language, it is either the most left or the most right segment.

For single select and multi-select, the first segment will be focused regardless of selection state.

[IMAGE] Segmented button focus starts on the left for left-to-right languages and on the right for right-to-left languages.

_Focus begins on the left for left-to-right languages and on the right for right-to-left languages_

##### Keyboard navigation

|

Keys |

Actions
|

**Tab** |

Navigate between buttons
|

Arrow keys |

Navigate inside the component
|

**Space** or **Enter** |

Activate a focused button

##### Labeling elements

The accessibility label for a segmented button comes from the visible label text on such as **Relevance** and **Distance**.

If the segmented button displays icons without label text, the accessibility label describes the action that the button is expressing, such as **Inexpensive** for one currency symbol.

[IMAGE] The the text and accessibility role for the left-most section of a segmented button following “sort by” is  is “relevance.”

_The label for segmented button matches the text label_

Single-select segmented buttons behave like radio buttons: only one option can be selected at a time. The label is **Radiogroup**.

Multi-select buttons behave like checkboxes: more than one option can be selected. The label is **Checkbox**.

[IMAGE] Selected price range segmented buttons range from $ to $$$$. The accessibility label of the $ button is “inexpensive.”

_The role for the multi-select segmented button is **Checkbox**_
