# Time pickers
Source: https://m3.material.io/components/time-pickers

## Overview

- Time pickers are modal and cover the main content
- Two variants: dial and input
- People can select hours, minutes, or periods of time
- Make sure time can easily be selected by hand on a mobile device

[IMAGE] Dial time picker dial and input time picker.

_- Time picker dial
- Time picker input_

#### Availability & resources

#### Differences from M2

- Color: New color mappings and compatibility with dynamic color

[IMAGE] Time picker’s old color mappings. The selected hour of 7 and AM text is purple, on a purple background.

_M2: Time pickers had different color mappings_

[IMAGE] Time picker's new color mappings. The selected hour of 7 and AM text is black, with different background colors.

_M3: Time pickers have new color mappings compatible with dynamic color_

## Specs

#### Tokens & specs

Select a component variant below to see its elements, attributes, tokens, and their values. Learn more about design tokens

#### Anatomy

##### Time picker dial

[IMAGE] Diagram indicating the 14 elements of a time picker dial.

_- Headline
- Time selector separator
- Container
- Period selector container
- Period selector label text
- Clock dial selector center
- Clock dial selector track
- Text button
- Icon button
- Clock dial selector container
- Clock dial label text
- Clock dial container
- Time selector label text
- Time selector container_

##### Time picker input

[IMAGE] Diagram indicating the 10 elements of a time picker input.

_- Headline
- Time input field seperator
- Container
- Period selector container
- Period selector label text
- Text button
- Icon button
- Time input field supporting text
- Time input field label text
- Time input field container_

#### Color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

##### Time picker dial color

[IMAGE] Side-by-side diagram indicating the 17 different color elements of a time picker dial.

_Time picker dial color roles used for light and dark themes:
- On surface variant
- On surface
- Surface container highest
- On surface
- Tertiary container
- On tertiary container
- Surface container high
- Outline
- On surface
- Primary
- On primary
- Primary
- On surface variant
- On surface
- Surface container highest
- On primary container
- Primary container_

##### Time picker input color

[IMAGE] Side-by-side diagram indicating the 13 different color elements of a time picker input.

_Time picker input color roles used for light and dark themes:
- On surface variant
- On surface
- Surface container highest
- On surface
- Tertiary container
- On tertiary container
- Surface container high
- Outline
- On surface
- Primary
- On surface variant
- On primary container
- Primary container_

#### States

[IMAGE] Diagram showing the 4 interactive states of a time picker, in both light theme and dark theme.

_- Enabled
- Hover
- Focus
- Pressed_

States specs can be found in the token module above

#### Measurements

##### Time picker dial - vertical

[IMAGE] Diagram of vertical time picker dial measurements.

_Vertical time picker dial padding and size measurements_

| Element | Attribute | Value
|

Container |

Width |

Dynamic
|

Height |

Dynamic
|

Headline alignment |

Left
|

Top/bottom padding |

24dp
|

Left/right padding |

24dp
|

Time selector container |

Width |

96dp
|

Width (24h vertical) |

114dp
|

Height |

80dp
|

Period selector container |

Width (vertical layout) |

52dp
|

Height (vertical layout) |

80dp
|

Width (horizontal layout) |

216dp
|

Height (horizontal layout) |

38dp
|

Clock dial container |

Size |

256dp
|

Clock dial selector handle |

Size |

48dp
|

Clock dial selector center |

Size |

8dp
|

Clock dial selector track |

Width |

2dp

##### Time picker dial - horizontal

[IMAGE] Diagram of horizontal time picker dial measurements.

_Horizontal time picker dial padding and size measurements_

| Element | Attribute | Value
|

Container |

Width |

Dynamic
|

Height |

Dynamic
|

Headline alignment |

Left
|

Top/bottom padding |

24dp
|

Left/right padding |

24dp
|

Time selector container |

Width |

96dp
|

Width (24h vertical) |

114dp
|

Height |

80dp
|

Period selector container |

Width (vertical layout) |

52dp
|

Height (vertical layout) |

80dp
|

Width (horizontal layout) |

216dp
|

Height (horizontal layout) |

38dp
|

Clock dial container |

Size |

256dp
|

Clock dial selector handle |

Size |

48dp
|

Clock dial selector center |

Size |

8dp
|

Clock dial selector track |

Width |

2dp

##### Time picker input

[IMAGE] Diagram of time picker input measurements.

_Time picker input padding and size measurements_

| Element
| Attribute | Value
|

Container |

Width |

Dynamic
|

Height |

Dynamic
|

Headline alignment |

Left
|

Top/bottom padding |

24dp
|

Left/right padding |

24dp
|

Time input field container |

Width |

96dp
|

Height |

72dp
|

Period selector container |

Width |

52dp
|

Height |

72dp

#### Configurations

##### Vertical orientation and horizontal orientation

[IMAGE] Comparing vertical and horizontal time picker dials.

_- Vertical layout (default on mobile)
- Horizontal layout_

##### 24-hour time picker dial

[IMAGE] 2 24-hour time picker dials with vertical and horizontal layouts.

_- 24h dial in vertical layout (default on mobile)
- 24h dial in horizontal layout_

##### 12-hour and 24-hour time picker inputs

[IMAGE] Compare 12-hour and 24-hour time picker inputs.

_- 12h input
- 24h input_

## Guidelines

[IMAGE] Time picker. It has a dial and keyboard input for hours and minutes, and a setting for AM or PM.

_Dial selector time picker for a 12-hour clock_

#### Usage

Time pickers allow people to enter a specific time value. They’re displayed in dialogs and can be used to select hours, minutes, or periods of time.

They can be used for a wide range of scenarios. Common use cases include:
- Setting an alarm
- Scheduling a meeting

Time pickers are not ideal for nuanced or granular time selection, such as milliseconds for a stopwatch application.

[IMAGE DO] Time picker with dial input selecting hour 7.

**DO:** _Hour selection in a mobile calendar picker_

##### Time input picker

Time input pickers allow people to specify a time using keyboard numbers. This input option should be accessible from any other mobile time picker interface by tapping the keyboard icon.

[IMAGE] Input time picker with keyboard active for the hour.

_Hour input with keyboard entry_

##### 24-hour time selection

The dial view can be changed to reflect time selection across 24 hours. This option is set outside of the time picker component, typically through system settings.

[IMAGE] Time picker with dial input selecting hour 20. Hours 0–11 use an outer dial, hours 12–23 use an inner dial.

_24-hour dial view_

#### Anatomy

[IMAGE] 17 elements of a dial time picker.

_- Label (headline)
- Time selector separator
- Input field
- Input text
- Period selector (selected)
- Period selector text (selected)
- Container
- Period selector outline
- Period selector text
- Dial selector track
- Dial label (selected)
- Text buttons
- Icon button
- Dial label (unselected)
- Clock dial
- Input text (selected)
- Input field (selected)_

[IMAGE] 13 elements of an input time picker.

_- Label (headline)
- Time selector separator
- Input field
- Input text
- Period selector (selected)
- Period selector text (selected)
- Container
- Period selector outline
- Period selector text (unselected)
- Text buttons
- Icon button
- Input text (selected)
- Input field (selected)_

##### Container

Like dialogs, the container should appear above other screen elements. To focus attention, surfaces behind the container have a temporary scrim overlay to make them less prominent.

[IMAGE] Time picker container, all elements inside.

_The container includes all time picker elements_

##### Input selector

The input selector is a unique kind of text field input. It differs from typical text field inputs in that it has:
- An added highlight to call attention to the selected field
- A larger shape, size, and font
- A label below the field

Hours and minutes should have separate inputs. For people using a 12-hour clock, an AM/PM selector appears to the right of minutes. For people using a 24-hour clock, the AM/PM selector shouldn’t appear.

[IMAGE] Input time picker with the hour field active, and so highlighted.

_Input selector for a 12-hour clock_

##### Dial selector

Dial selectors always mimic a round watch face. Hours and minutes can be selected by tapping a number or dragging the dial selector track.

When representing a 12-hour dial, all numbers appear in the outer ring. When representing a 24-hour dial, even numbers appear in an inner ring, and odd numbers appear in an outer ring.

[IMAGE] Dial time picker with hour 7 selected.

_Dial selector for a 12-hour clock_

##### Text & icon buttons

Icon buttons are used to switch between the input selector, represented by a keyboard, and the dial selector, represented by a clock.

Text buttons are used to exit the dialog (**Cancel**) and save the selector input (**OK**).

[IMAGE] Time picker buttons.

_The keyboard icon allows people to switch between the dial selector (pictured) and the input selector_

##### Landscape orientation

The clock dial interface adapts to a device’s orientation. In landscape mode, the stacked input and selection options are positioned side-by-side.

[IMAGE] Time picker in landscape orientation on mobile.

_On mobile, the time picker can adapt to landscape orientation_

#### Placement

Time pickers shouldn’t be obscured by other elements.

Time pickers should change orientation or variant to ensure they aren't cropped by the edge of the screen.

Time pickers are modal windows above a scrim. This puts the time pickers at the forefront of a person's view, calling attention to make a selection of time.

[IMAGE] Input time picker in landscape orientation on mobile. It's fully visible despite the limited device height.

_The time picker should change to fit the size of the screen so the time picker is always fully visible_

#### Adaptive design

Time pickers can swap between orientation or variant depending on device orientation and viewport constraints.

For example, the time picker can change to landscape orientation on larger breakpoints or when viewport height is limited, to avoid scrolling the dial presentation.

Time pickers can fallback to the input time picker when there isn’t enough vertical real estate to present the landscape orientation without scrolling.

[IMAGE] Dial time picker in portrait orientation on mobile.

_High-density time picker displayed on mobile_

##### Density

Don’t apply density to the time picker dial when the viewport is constrained. Instead, use an input picker.

[IMAGE DON'T] Dial time picker in portrait orientation, too tall to fully fit on a mobile device in landscape mode.

**DON'T:** _Don’t apply density to the time picker dial when the viewport is constrained. Instead, use an input picker._

#### Behavior

There are two primary methods for selecting time with the mobile time picker. People can:
- Type in a specific value in the hour and minute fields
- Select the hour or minute field from the text input and adjust the clock dial to simultaneously change the corresponding time field above

[VIDEO] Dial time picker possible interactions. Dial selector adjusts to match manual input.

_The dial time picker supports both manual and dial input_

##### Appearing & disappearing

Like other kinds of dialogs, time pickers use an enter and exit transition pattern to appear on the screen.

To exit a time picker, the input can either be confirmed (**OK**) or dismissed (**Cancel**). Interacting outside of the dialog will also dismiss the time picker. Unless one of these actions is taken, a time picker will continue to retain focus.

[VIDEO] Time picker enter and exit transitions.

_**OK** confirms the entry and closes the dialog_

##### Toggle between dial & input

Tapping the keyboard icon on a mobile time picker switches the view to the input picker.

[VIDEO] Time picker transitioning between dial and input selectors.

_The keyboard icon in the lower left toggles between the input picker and the dial picker_

##### Scrolling

Time pickers should avoid scrolling, and swap component orientation or variant based on device orientation or viewport size.

Time pickers don’t scroll with elements outside of the modal window, such as the background.

[IMAGE] Input time picker in landscape orientation to fit a mobile device in landscape mode.

_Time pickers shouldn’t scroll_

## Accessibility

#### Use cases

People should be able to use assistive technology to:
- Select or enter hours/minutes, and in some cases, seconds/milliseconds
- Choose from multiple time formats, including 24-hour clock view and AM/PM
- Enter time selection manually using input fields

#### Interaction & style

Time pickers should allow manual time entry through text input, rather than exclusively through the dial selector. This makes it easier for those using keyboard inputs rather than touchscreens.

If a screen is not large enough to display the dial selector, consider displaying the input selector alone. Currently for Android Views, the dial selector is always visible.

The input selector should be accessible from the dial selector via the keyboard icon. This interaction allows multiple input methods and makes the time picker accessible for assistive technology users.

[IMAGE] Time picker with active manual text input for hours.

_For time selection that doesn’t require a dial view, make a time input picker the default option_

##### Targets

Targets for dial selectors should be 48x48dp.

[IMAGE] Time picker dial selector specs, selecting hour 7.

_Dial selector targets should be 48x48dp_

#### Keyboard navigation

| Keys | Actions
| **Tab** | Focus lands on (non-disabled) time slot
| **Space** or **Enter**
| Activates the (non-disabled) time slot

#### Labeling elements

If the input text is correctly linked, assistive tech like a screenreader will read the component’s role first, then the UI text.

[IMAGE] Accessibility tags on the time picker's hour input field.

_The hour and minute fields have the text input role_

The dial selector will read a selection of total hours, such as **Hour 7 of 12**.

[IMAGE] Accessibility tag on the time picker's dial selector.

_A screen reader reads the text label of a dial selector_

##### Dial selector

| Element
| Accessibility label | Role (Wiz and Jetpack Compose)
| Role (Android Views)

| Hour input (input picker)
| Hour | Text input | -
| Minutes input
| Minute | Text input | -
| AM/PM selection
| AM or PM | Radio button (in list) | Checkbox (in list)
| Keyboard button
| Toggle input picker | Button | Button
| Cancel button
| Cancel | Button | Button
| OK button
| OK | Button | Button
| Clock dial time selection (dial selector)
| {Value} Hours or minutes of {Total} | Button | -

##### Input selector

| Element
| Accessibility label | Role (Wiz and Jetpack Compose)
| Role (Android Views)

| Hour input (input picker)
| Hour | Text input | -
| Minutes input
| Minute | Text input | -
| Clock button
| Toggle dial picker | Button | Button
| Cancel button
| Cancel | Button | Button
| OK button
| OK | Button | Button
