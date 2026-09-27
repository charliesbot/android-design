# Sliders
Source: https://m3.material.io/components/sliders

## Overview

- Three variants: Standard, centered, range
- Has five sizes, vertical and horizontal orientation, and an optional inset icon
- Sliders should present the full range of available values
- The slider value should take effect immediately

[VIDEO] A vertical slider changes the brightness of bedroom lights.

_Sliders change values along a range_

#### Availability & resources

#### M3 Expressive update

**May 2025**

The slider includes expressive configurations for orientation, shape sizes, and an inset icon. More on M3 Expressive

Updated on Android Views (MDC-Android) and Jetpack Compose.

Variants and naming:
- Changed **continuous** slider to **standard** slider
- The **discrete** slider is now the **stops** configuration

New configurations:
- Orientation: Horizontal, vertical
- Optional inset icon (standard slider only)
- Sizes: XS (existing default), S, M, L, XL

[IMAGE] 3 M3 Expressive sliders.

_- Standard slider
- Centered slider
- Range slider_

#### Previous updates

##### Visual refresh to improve non-text contrast

**Dec 2023:** Updated on Android Views (MDC-Android) and Jetpack Compose.
- **Configuration:** Added centered configuration and range selection
- **Shape: **New shape for slider tracks and handles. Slider elements change shape when selected.
- **Motion:** Slider handle adjusts width upon selection. Slider tracks adjust in shape when sliding to the edge.
- **Color: **Refreshed color mappings

[IMAGE] M3 visually-refreshed slider.

_M3 visual refresh: Sliders have a stop indicator, larger label text, and a vertical handle that narrows when pressed. Centered sliders start from the middle instead of the leading edge._

#### Differences from M2

- **Color**: New color mappings and compatibility with dynamic color

[IMAGE] M2 slider.

_M2: Sliders have a circular handle and a small label when pressed_

[IMAGE] Original M3 slider.

_M3: Sliders have new color mappings and support dynamic color_

## Specs

#### Variants

[IMAGE] 3 variants of sliders.

_- Standard
- Centered
- Range_

|

Variant |

M3 |

M3 Expressive
|

Standard |

Available as “continuous” slider |

Available
|

Centered |

Available (web only) |

Available
|

Range |

Available |

Available
|

Discrete |

Available |

Available as “stops” configuration

#### Configurations

[IMAGE] Orientation and size configurations of sliders.

_- Orientation: Horizontal, vertical
- Size: XS, S, M, L, XL_

[IMAGE] Optional anatomy configurations of sliders.

_- Inset icon
- Stops
- Value indicator_

|

Category |

Configuration |

M3 |

M3 Expressive
|

Inset icon |

No (default) |

Available |

Available
|

Yes |

-- |

Available
|

Orientation |

Horizontal (default) |

Available |

Available
|

Vertical |

-- |

Available
|

Size |

XS (default) |

Available |

Available
|

S, M, L, XL |

-- |

Available on Android Views (MDC-Android).
Available as tokens on other platforms.*

|

Stop indicators |

No (default), Yes |

Available as “discrete” slider |

Available
|

Value Indicator |

No (default), Yes |

Available |

Available

*Configurations only available using tokens don’t have implemented presets in code. To change the size, swap the default size tokens md.comp.slider.**xsmall**.[...] with those of the desired size.

#### Tokens & specs

Slider tokens are organized into a common token set, and token sets for each size. Switch token sets from the table’s menu. Learn more about design tokens

#### Anatomy

[IMAGE] 6 elements of a slider.

_- Value indicator (optional)
- Stop indicators (optional)
- Active track
- Handle
- Inactive track
- Inset icon (optional)_

#### Color

[IMAGE] 9 color roles of a slider.

_Slider color roles used for light and dark schemes:
- Inverse surface
- Inverse on surface
- Primary
- On primary
- Primary
- Secondary container
- On secondary container
- On secondary container
- On primary_

#### States

[IMAGE] 5 states of sliders in light and dark schemes.

_- Enabled
- Disabled
- Hovered
- Focused
- Pressed_

#### Measurements

[IMAGE] Common slider padding and size measurements.

_Padding and size measurements for common sliders_

[IMAGE] Slider padding and size measurements at each size configuration, XS to XL.

_Padding and size measurements for XS, S, M, L, and XL sliders_

| Attribute | XS | S | M
| L | XL
| Track height | 16dp | 24dp | 40dp | 56dp | 96dp
| Label container height
| 44dp
| Label container width | 48dp
| Handle height
| 44dp | 44dp | 52dp | 68dp | 108dp
| Handle width
| 4dp
| Track shape | 8dp | 8dp | 12dp | 16dp | 28dp
| Inset icon size | -- | -- | 24dp | 24dp | 32dp

## Guidelines

[VIDEO] Photo editor with sliders labeled pop, sharpen, and denoise being adjusted.

_Sliders can adjust values in real time, such as image attributes_

#### Usage

Sliders are used to select values along a track. They’re ideal for adjusting settings such as volume and brightness, or changing the intensity of image filters.

Sliders can use icons or labels to represent a numeric or relative scale.

[IMAGE] Sound settings screen with continuous sliders labeled call volume and alarm volume.

_Use sliders to pick a value from a range, like volume loudness_

Changes made with sliders must take effect immediately, so people can understand the effects of their selection as they're moving the slider.

[VIDEO] The brightness slider changes the screen’s brightness in real time.

_Selection changes are immediate_

There are three different variants of sliders: **standard**, **centered**, and **range:**

Standard sliders select one value from a range of values. Use this when the slider should start from zero or the beginning of a sequence.

[IMAGE] Horizontal standard slider with an end stop indicator.

_Horizontal standard slider_

[IMAGE] Vertical standard slider  with an end stop indicator.

_Vertical standard slider_

Centered sliders select a value from a positive and negative value range. Use this when zero, or the default value, is in the middle of the range.

[IMAGE] Horizontal centered slider with a negative value and visible stop indicators at each end.

_Horizontal centered slider_

[IMAGE] Vertical centered slider with a negative value and visible stop indicators at each end.

_Vertical centered slider_

Range sliders select two values on one slider to create a range. Use this when defining a minimum and maximum value.

Avoid using range sliders vertically, as this can add too much cognitive load. People are used to most sliders being horizontal.

[IMAGE DO] Horizontal range selection slider with 2 values selected, and a stop indicator at each end.

**DO:** _Horizontal range slider_

[IMAGE DON'T] Vertical range slider with 2 values selected, and a stop indicator at each end.

**DON'T:** _Because of the additional cognitive load of a range slider, avoid using it in vertical orientation._

#### Anatomy

[IMAGE] 6 elements of a slider.

_- Value indicator (optional)
- Stop indicators (optional)
- Active track
- Handle
- Inactive track
- Inset icon (optional)_

##### Track

The track shows the full range of values that can be selected on the slider. It has two sections: active and inactive.
- The **active **section of the track is from the minimum value to the handle. For range sliders, the active track is between the two handles.
- The **inactive** section of the track is from the handle to the maximum value, or outside the two handles of a range slider.

For left-to-right (LTR) languages, the values increase from left to right. For right-to-left (RTL) languages, this is reversed.

[IMAGE] Sliders for font size and display size with stop indicators along the track.

_The track on a slider shows the available range_

##### Handle

The handle can be moved along the track to choose a value.

When sliders have two handles, the handles choose the minimum and maximum values in a range.

The handle changes shape to indicate when it’s pressed.

[IMAGE] The handle is a vertical line on the current value of the slider. It shrinks in width when selected.

_A handle changes shape when it's being pressed or dragged_

[IMAGE] 2 unselected handles on a slider with range selection.

_Two handles are used for sliders with range selection_

#### Configurations

##### Value

The value displays the specific value that corresponds with the handle’s placement.

A value appears when interacting with the corresponding handle. For range sliders, only one value should be shown at a time.

If the value is shown elsewhere, the indicator is not required.

[IMAGE] A value of 50 is above a slider handle in the middle of the track.

_A value can appear while the handle is being pressed or dragged_

[IMAGE] A value of 75 is above the pressed range slider handle. The unselected slider doesn’t show the value.

_For range sliders, the value only appears on one handle at a time_

Instead of showing the built-in value label, a separate text input field can be added outside of the slider. If this is added, the slider and value in this text field should automatically update to match each other.

Make sure people can tab to the text field directly after the slider.

[VIDEO] Navigating to an adjacent text label, changing the value to 100 using arrow keys. The slider auto-updates based on input value.

_Use **Tab** to navigate to values that are shown outside the slider, like a text input field_

##### Stop indicators

Stop indicators show which predetermined values can be chosen on the slider. The slider handle snaps to the closest stop.

Avoid having too many stop indicators on a slider, because it can become visually crowded and difficult to adjust the value.

All sliders have stops at the end of the inactive track to ensure at least a 3:1 contrast with the background. If the inactive track has this level of contrast already, the end stops can be removed.

[IMAGE] Stop indicators are equally spaced out on a slider.

_Stop indicators show each available value on a slider_

Icons or text can be added outside the slider to indicate the range of values and make the slider more accessible. This can be used instead of a stop indicator.

[IMAGE] Plus and minus icons on each end of the slider.

_Plus and minus icons, or text, can be added to the left and right of the slider_

##### Orientation

Sliders can be oriented either horizontally or vertically, depending on what is best for your use case.

[IMAGE] Horizontal slider.

_Standard slider in horizontal orientation_

[IMAGE] Vertical slider. Zero is at the bottom.

_Standard slider in vertical orientation_

##### Inset icon

Standard sliders that are M, L, or XL can include an icon within the track. This icon should illustrate what the slider controls. Avoid adding inset icons to XS or S sliders.

When there’s not enough space for the icon on the active track, like at a low value, the icon moves to the inactive track.

Consider swapping which icon is displayed at zero, like a volume icon becoming a mute icon.

[IMAGE DO] Inset icon on the active track when the handle is at 50%, and on the inactive track when the handle is at 0.

**DO:** _Inset icons change placement based on the handle_

[IMAGE DON'T] An inset icon on an XS slider. The icon bounds are cut off by the slider container.

**DON'T:** _Don’t use an inset icon with sliders that have track thicknesses under 40dp_

Don’t use inset icons on centered or range sliders. It makes it unclear where the start of the slider is.

[IMAGE DON'T] Centered slider with an inset icon on one end, and a stop indicator on the other.

**DON'T:** _Don’t use an inset icon on a centered slider_

[IMAGE DON'T] Range slider with an inset icon on one end, and a stop indicator on the other.

**DON'T:** _Don’t use an inset icon on a range slider_

##### Size

Sliders come in different sizes: XS, S, M, L, and XL. Use larger sizes to increase the targets and provide a larger visual emphasis.

The active and inactive tracks should always be the same size.

[IMAGE] 5 sizes of sliders.

_- XS: 16dp
- S: 24dp
- M: 40dp
- L: 56dp
- XL: 96dp_

XL sliders should be reserved for hero moments, where the slider itself is the most important element on the page.

[IMAGE] An XL slider used to adjust living room temperature on mobile. No other controls are on screen.

_XL sliders should be the focus of the page_

#### Behaviors

##### Select & drag

Select a value by dragging the handle.

[VIDEO] Slider handle moves smoothly. The value increases and decreases by single digits.

_**Standard slider**: The handle drags smoothly_

[VIDEO] Slider handle snaps to the nearest multiple of 10 when moved.

_**Slider with stop indicators:** The handle snaps to the closest stop indicator while dragged_

##### Select jump

Select a value by selecting part of the track.

[VIDEO] Slider handle moves smoothly to selected value of the slider.

_**Standard**** slider**: The handle moves to the selected location_

[VIDEO] Slider handle moves smoothly to the closest stop indicator value.

_**Slider with stop indicators:** The handle moves to the closest stop indicator_

##### Select & arrow

Select a value using the keyboard.

**Tab:** Focus lands on handle

**Arrows: **Selected value increases or decreases by one value or stop indicator

**Space & arrows:** Selected value increases or decreases by a larger interval or stop indicator

[VIDEO] Tab selects the slider, and arrow keys move the value by 1.

_**Standard**** slider**: The handle moves one value_

[VIDEO] Tab selects the slider, and arrow keys move the value by 10.

_**Slider with stop indicators: **The handle moves to the next stop indicator_

## Accessibility

#### Use cases

People should be able to do the following using assistive technology:
- Navigate to a slider
- Select a range by controlling a handle along a track
- Get appropriate feedback based on input type

#### Interaction & style

The slider handle shrinks in width and the value appears to provide a visual cue to the user that the handle is being pressed.

**Touch**

When tapped or dragged, the handle width shrinks to provide interaction feedback, and the value appears.

**Cursor**

When hovered, the cursor changes. When clicked and dragged, the handle width shrinks, and the value appears.

[VIDEO] Dragging a slider using a mouse cursor and by touch. Handle width shrinks when dragged.

_The slider handle changes width during interaction_

##### Focus and navigation

Initial focus lands directly on the handle, since it’s the primary interactive element of the slider.

The slider value can then be adjusted using the arrow keys or other keyboard navigation options.

[VIDEO] Using keyboard navigation on a slider. Tab to the handle, and use arrow keys to change the value.

_Use arrow keys to change the slider value_

#### Color contrast

Use visual anchors so the end of the slider’s inactive track has at least 3:1 contrast with the background. The stop indicator makes the end easily visible on most backgrounds.

[IMAGE] The inactive track has contrast below 3:1 with the background. The stop indicator is above 3:1. Slider is accessible.

_A stop indicator on the inactive track makes it easier to identify the end of the slider on a low-contrast background_

Alternatively, icons or other elements that have a 3:1 contrast with the background can be used to indicate the ends of the slider’s inactive track.

[IMAGE] The inactive track has contrast ratio below 3:1 with the background. The icon is above 3:1. The slider is accessible.

_Icons make it easier to identify the ends of the slider on a low-contrast background_

#### Keyboard navigation

| Keys
| Actions

| Tab
| Moves focus to the slider handle
| Arrows
| Increase and decrease the value by one value or one stop indicator
| Space & Arrows
| Increase and decrease the value by one interval or one stop indicator
| Home or End
| Set the slider to the first and last values on the slider

#### Labeling elements

The accessibility label for a slider is typically the same as the slider's adjacent text label. It should have the **slider** role.

[IMAGE] Annotated aria tags of a slider.

_A slider’s accessibility label should match the adjacent UI text_

If the UI text is correctly linked to the slider, assistive tech (such as a screenreader) will read the UI text followed by the component’s role.

[IMAGE] Annotated aria tags of a slider with stepper icons.

_Icon buttons placed outside the slider should have the button role_
