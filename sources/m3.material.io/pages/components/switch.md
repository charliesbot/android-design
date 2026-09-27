# Switch
Source: https://m3.material.io/components/switch

## Overview

- Use switches (not radio buttons) if the items in a list can be independently controlled
- Switches are the best way to let people adjust settings
- Make sure the switch’s selection (on or off) is visible at a glance

[IMAGE] A switch in two states, off and on.

_Switches can be toggled on and off_

#### Availability & resources

#### Differences from M2

- Accessibility: Visual presentation is more accessible
- Color: New color mappings meet Material's non-text-contrast requirements in addition to compatibility with dynamic color
- Icons: Ability to have an optional icon within the switch handle
- Layout: Track is taller and wider

[IMAGE] M2 switches in off and on states.

_M2: Switches have a circular handle that extends beyond the edge of the track_

[IMAGE] M3 switch shown toggled off and toggled on. When switched on, it has a checkmark icon.

_M3: Switches have a taller and wider track, new color mappings, and the ability to show an icon in the handle_

## Specs

[IMAGE] 3 elements of a switch.

_- Track
- Handle (formerly "thumb")
- Icon_

#### Tokens & specs

Browse the component elements, attributes, tokens, and their values. Learn more about design tokens

#### Color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] 6 color roles of a switch in light and dark themes.

_Switch color roles used for light and dark themes:
- Surface container highest
- Outline
- Outline
- Primary
- On primary
- On primary container_

##### Adjacent text label color

Use the color role **on surface** for adjacent text labels. This remains the same even if interacting with the label or component.

[IMAGE] The large body text adjacent to switches uses "on surface" color and the body text uses "on surface variant."

_The text label uses **on surface**. Supporting text may use **on surface variant**._

#### States

States are visual representations used to communicate the status of a component or interactive element. Learn more about interaction states

[IMAGE] 5 states of a switch shown in light and dark themes.

_- Enabled
- Hovered
- Focused
- Pressed
- Disabled_

State specs are in the token module above

#### Measurements

[IMAGE] Measurements of switches without icons.

_Switches without icons_

[IMAGE] Measurements of pressed switches without icons.

_Pressed switches without icons_

[IMAGE] Measurements of switches with icons.

_Switches with icons_

[IMAGE] Measurements of pressed switches with icons.

_Pressed switches with icons_

|

Element |

Attribute |

Value
|

Track |

Height |

32dp
|

Width |

52dp
|

Outline width |

2dp
|

Shape |

md.sys.shape.corner.full
|

Handle |

Height (unselected) |

16dp
|

Height - with icon |

24dp
|

Height (selected) |

24dp
|

Height (pressed) |

28dp
|

Width (unselected) |

16dp
|

Width - with icon |

24dp
|

Width (selected) |

24dp
|

Width (pressed) |

28dp
|

Shape |

md.sys.shape.corner.full
|

State layer |

Size |

40dp
|

Shape |

md.sys.shape.corner.full
|

Target |

Size |

48dp
|

Icon |

Size (selected) |

16dp
|

Icon |

Size (unselected) |

16dp

#### Configurations

- Without icons
- Icon on selected switch
- Icon on selected and unselected switch

[IMAGE] 3 example switches with and without icons in on and off states.

## Guidelines

[IMAGE] A switch in 2 states, off and on.

_Switches change settings and other options immediately_

#### Usage

Switches are best used to adjust settings and other standalone options.

They make a binary selection:
- On and off
- True and false

The effects of a switch should start immediately, without needing to save.

[VIDEO] Toggling a switch turns on dark mode in Settings on a mobile device.

_Use a switch to turn an option on and off_

Use switches to:
- Toggle a single item on or off
- Immediately activate or deactivate something

[IMAGE] Switch used in notification settings to turn on and off the "play over notifications" function.

_Switches are commonly used on mobile to turn settings on or off_

Switches control binary options, not opposing ones. A binary option represents a single selection that's either on or off.

Opposing options are when only one option in a set can be selected at a time, like a list or map view. Use a connected button group instead.

[IMAGE DO] A connected button group with options of List View and Map View.

**DO:** _Use a connected button group to choose between opposing options_

[IMAGE DON'T] A switch with non-binary options of List View and Map View.

**DON'T:** _Avoid using switches to toggle between opposing options_

##### Alternate selection controls

Checkboxes, radio buttons, and switches are the three main kinds of selection controls. They help people make choices, like selecting options or turning settings on and off.

Use checkboxes to select multiple related options in a list.

Use radio buttons to select a single option in a list.

Use switches to select standalone or more verbose options in a list, like settings.

[IMAGE] 2 checkboxes, 1 unchecked and 1 checked.

_Checkboxes_

[IMAGE] 2 radio buttons, 1 in an enabled state, 1 in a disabled state.

_Radio buttons_

[IMAGE DO] Mobile screen with checkboxes to select list items and call to action button to update the list.

**DO:** _Use checkboxes (not switches) to let people select one or more options from a list_

[IMAGE DON'T] Mobile screen with checkboxes to select list items and call to action switch to update the list.

**DON'T:** _A switch can't replace a button. People expect a call to action to be a button, not a switch._

[IMAGE DO] Radio buttons used to select a language for a mobile app.

**DO:** _Use radio buttons (not switches) when only one item can be selected from a list_

[IMAGE DON'T] Mobile screen with switches to select list items and call to action button to update the list.

**DON'T:** _Avoid using a switch to select multiple options that require people to save. Switches should be immediate. Use checkboxes instead._

#### Anatomy

[IMAGE] 3 elements of a switch.

_- Track
- Handle
- Icon (optional)_

##### Icon (optional)

The switch handle can contain an optional icon.

[VIDEO] A checkmark appears on the filled circle of a handle when the switch is toggled on.

_The icon within the handle should always communicate the switch's selection_

Icons can be used to visually emphasize the switch’s selection. The icon’s meaning should be clear and unambiguous to help the people understand whether switch is on or off.

[IMAGE DO] 2 switches, the unselected state icon is an X, and the selected state is a checkmark.

**DO:** _Use icons that clearly communicate whether the switch is on or off, such as an X and a checkmark_

[IMAGE DON'T] A switch’s unselected handle icon is a moon and the selected state icon is a pencil.

**DON'T:** _Avoid using more ambiguous or non-binary icons, such as a moon or edit icon_

##### Label text

Switches should always be paired with an inline label describing what the switch controls when selected.

[IMAGE DO] Switch label text: 1. Permission manager, App has access to your data. 2. Camera access, app has access to your camera. 3. Show password.

**DO:** _Keep labels short and direct. A label should describe what the control does when the switch is on._

[IMAGE DON'T] On a mobile screen’s privacy settings, 2 switches have “On” label text and checkmark icons. The last switch has “Off” label text and no icon.

**DON'T:** _Don't add label text into the switch; the font size would be too small to be accessible. Use an appropriate icon instead._

#### Placement

Switches are often arranged in stacked layouts.

[IMAGE] Screen showing labels and stacked switches in varying on/off modes.

_Settings screens are common places to use switches_

#### Behavior

A switch is successfully toggled when the handle slides to the other side of the track after an interaction.

[VIDEO] A switch is toggled to turn on airplane mode and Wi-Fi switch automatically turns off.

_When selected, the switch handle slides to the opposite end of the track_

When a person toggles a switch, its handle size changes and the corresponding action takes effect immediately.

[VIDEO] When selected the switch’s handle gets larger and a checkmark appears on the filled circle.

_The **on** state of the switch is indicated by a larger handle size_

## Accessibility

#### Use cases

People should be able to do the following with assistive technology:
- Navigate to a switch with a keyboard or switch input
- Toggle the switch on and off
- Get appropriate feedback based on input type documented under Interaction & style

#### Interaction & style

The switch handle increases in size to indicate interactivity for both touch and cursor control interactions.

**Touch**
When tapped or dragged, the handle size grows, providing interaction feedback.

**Cursor**
When hovered (in both on and off states), the hover area grows, providing a visual cue that the handle is interactive. When clicked, the handle size grows.

[IMAGE] The switch handle increases in size when tapped and dragged.

_Touch: Tap, Drag_

[IMAGE] The cursor changes from an arrow to a hand pointer when hovering over and clicking the switch.

_Cursor: Hover, Click_

##### Avoid applying density by default

Don't apply density to switches by default — this lowers their targets below our best practice of 48x48 CSS pixels. Instead, give people a way to choose a higher density, like selecting a denser layout or changing the theme.

To ensure that this density setting can easily be reverted when it's active, keep all targets to change it at a minimum 48x48 CSS pixels each.

#### Initial focus

Initial focus lands directly on the switch’s handle, since it’s the primary interactive element of the component.

[IMAGE] The focus is on the switch handle, which is toggled on.

_Focus lands on the switch handle_

[IMAGE] Space or Enter is used to toggle the switch off.

_The switch is toggled using **Space** or **Enter**_

#### Keyboard navigation

| Keys | Actions
| **Tab** | Focus lands on the switch handle
| **Space **or** Enter** | Toggles the handle on and off

#### Labeling elements

The accessibility label for a switch uses the adjacent label text if implemented correctly.

Assistive tech such as a screen reader will read the UI text followed by the component’s role.

[IMAGE] “Dark theme” is the switch’s adjacent label text and the accessibility label.

_A switch’s accessibility label can incorporate its adjacent UI text_

When the visible UI text is ambiguous, accessibility labels need to be more descriptive. For example, a switch visibly labelled **Photo album** would benefit from additional information to clarify the switch’s function.

Consider making the adjacent label text more descriptive when possible. This reduces the need for different accessibility text.

[IMAGE] The accessibility label for the switch is “Photo album access” though the label text is “photo album.”

_While the visible label text reads **Photo album**, the accessibility label for this switch clarifies its function: **Photo album access**_
