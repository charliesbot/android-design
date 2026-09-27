# Side sheets
Source: https://m3.material.io/components/side-sheets

## Overview

- Use side sheets to provide optional content and actions without interrupting the main content
- Two variants: standard and modal
- People can navigate to another region within the sheet
- Side sheets can contain a back icon for navigation

[IMAGE] The 2 variants of side sheets.

_- Standard side sheet
- Modal side sheet_

#### Availability & resources

#### Differences from M2

- Right-to-left (RTL) language support with left side sheet
- Color: New color mappings and compatibility with dynamic color
- Shape: Modal side sheets have a 16dp corner radius

[IMAGE] A modal side sheet showing the 16dp corner radius.

_Side sheets have new color mappings to support dynamic color_

## Specs

#### Tokens & specs

Browse the component elements, attributes, tokens, and their values. Learn more about design tokens

#### Standard side sheet

[IMAGE] 4 elements of a standard side sheet.

_- Divider (optional)
- Headline
- Container
- Close icon button_

##### Standard side sheet color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] 4 color roles applied to a side sheet in light and dark themes.

_Side sheet color roles used for light and dark themes:
- Outline variant
- On surface variant
- Surface
- On surface variant_

##### Standard side sheet measurements

[IMAGE] Standard side sheet padding and size measurements.

_Side sheet padding and size measurements_

| Attribute
| Value

| Start/end padding
| 24dp
| Padding between top elements
| 12dp
| Bottom actions height
| 72dp
| Bottom actions top padding
| 16dp
| Bottom actions bottom padding
| 24dp
| Bottom actions alignment (horizontal)
| Left
| Max-width
| 400dp
| Margins (when detached)
| 16dp

#### Modal side sheet

[IMAGE] 7 elements of a modal side sheet.

_- Back icon button (optional)
- Headline
- Container
- Close icon button
- Divider (optional)
- Action buttons (optional)
- Scrim_

##### Modal side sheet color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens.

[IMAGE] 4 color roles applied to a modal side sheet in light and dark themes.

_Side sheet color roles used for light and dark themes:
- On surface variant
- On surface variant
- Surface container low
- On surface variant_

##### Modal side sheet measurements

[IMAGE] Modal side sheet padding and size measurements

_Modal side sheet padding and size measurements_

| Attribute
| Value

| Start/end padding
| 24dp
| Start padding with icon | 16dp
| Padding between top elements | 12dp
| Bottom actions height
| 72dp
| Bottom actions top padding
| 16dp
| Bottom actions bottom padding | 24dp
| Bottom actions alignment (horizontal)
| Left
| Max-width
| 400dp
| Margins (when detached)
| 16dp

## Guidelines

[IMAGE] Side by side comparison of a standard and a modal side sheet.

_- Standard side sheet
- Modal side sheet_

#### Usage

Standard side sheets are supplementary surfaces used mostly in medium to expanded breakpoints, like tablet and desktop. They provide a consistent and predictable surface for contextual actions and information.

Standard side sheets display content that complements the screen’s primary content. They remain visible while people interact with primary content.

Common uses include:
- Displaying a list of actions that affect the screen’s primary content, such as filters
- Displaying supplemental content and features

[IMAGE] Standard side sheet showing supplementary information about a photo.

_Information about a photo in a standard side sheet_

Modal side sheets are preferred in compact breakpoints, like mobile, due to limited screen size.

They can display the same kinds of content as standard side sheets, but must be dismissed in order to interact with the underlying content.

[IMAGE] Modal side sheet showing filter controls.

_Modal side sheet with filter controls_

Side sheets have a fixed width and typically span the height of the screen.

Their dimensions depend on how the app’s layout is subdivided into UI regions.

[IMAGE DO] A modal sheet at the right of a screen, with the correct inset.

**DO:** _Place side sheets along the edge of the screen, usually on the right side to avoid interference with any navigational components on the left edge. They can be slightly inset by 16dp._

[IMAGE DON'T] A modal side sheet at the right of the screen with the wrong inset.

**DON'T:** _Don’t inset a side sheet from the screen edges far beyond the recommended margin. This makes the sheet’s position and scroll behavior unclear, while obscuring primary content._

#### Anatomy

[IMAGE] 4 elements of a standard side sheet.

_- Divider (optional)
- Headline
- Container
- Close icon button_

[IMAGE] 7 elements of a modal side sheet.

_- Back icon button (optional)
- Headline
- Container
- Close icon button
- Divider (optional)
- Action buttons (optional)
- Scrim_

##### Container

Side sheet containers hold all side sheet elements. Their size is determined by the space those elements occupy.

The container is the only required element of a side sheet.

[IMAGE] A modal side sheet’s container.

_- Container_

##### Back icon button (optional)

Icon buttons can provide ways to exit a side sheet or move to a different experience.

Because the primary content behind or beside a side sheet is always visible, it’s important to provide affordances for leaving a side sheet and returning to the primary content.

[IMAGE] Back icon button on the upper left of a modal side sheet.

_- Back icon button_

##### Close icon button (optional)

A close affordance provides a consistent method for dismissing a side sheet.

A close icon button is highly recommended, increases accessibility, and makes focused side sheets easier to close.

[IMAGE] Close icon button on the upper right of a modal side sheet.

_- Close icon button_

##### Action buttons (optional)

Buttons represent actions available from a side sheet. Examples: **Save**, **Edit**, **Download**

Use elevation, fill, and tone to call attention to specific actions.

[IMAGE] Save and cancel buttons at the bottom of a modal side sheet.

_- Action buttons_

##### Divider (optional)

Dividers can separate different kinds of content and create distinct regions in a side sheet.

Use a divider to separate:
- Action buttons from content
- User-generated content from system-generated content

[IMAGE] Horizontal divider on a modal side sheet.

_- Divider_

##### Content (optional)

Side sheets can display a wide variety of content and layouts, ranging from a list of actions to supplemental content in a tabular layout.

[IMAGE] 2 side sheets with different content displayed side by side.

_Form controls shown in a side sheet for app settings_

[VIDEO] As a small screen changes to a larger size the modal side sheet transitions to a standard side sheet.

_Modal side sheets on smaller screens can transition to standard side sheets at larger screen sizes_

#### Adaptive design

Side sheets have a default width, but can be resized depending on the needs of the layout.

When a standard side sheet opens, the body area shrinks to accommodate the sheet’s width while maintaining a margin on the body’s trailing edge.

[VIDEO] Body area of a screen adjusts to accommodate entrance and exit of side sheet.

_Entrance of standard side sheets will cause the body area to adjust and accommodate the new content_

##### RTL language support

In right-to-left (RTL) languages, side sheets should appear on the left edge of the window with all elements reversed.

[IMAGE] Side sheet along the left edge of a screen. All buttons and icons are reversed.

_Side sheet elements are reversed in RTL languages_

#### Behavior

Side sheets can vertically scroll independent of the rest of the UI.

This allows their scroll position and content to persist while the page is scrolled, and vice versa.

Side sheets cannot scroll horizontally.

[VIDEO DO] Animation showing a side sheet being scrolled vertically to view all the options.

**DO:** _Side sheets can vertically scroll internally when their content exceeds the screen height_

[IMAGE DON'T] A side sheet appears to scroll horizontally.

**DON'T:** _Don’t allow horizontal scrolling or lay out the side sheet in a way that suggests horizontal scrolling. A side sheet’s narrow width leaves limited space to fully view items._

##### Predictive back

On Android, a gesture called predictive back allows a person to swipe left or right on the side sheet.

When predictive back is used:
- The side sheet detaches from the top and bottom edges of the screen to signal it will close
- The previous screen is revealed in a preview
- The side sheet and its content always scales in the direction of the gesture

Find a list of compatible components

[VIDEO] Swiping to go back shows a preview of the previous screen.

_Preview of the result of the gestures: release to commit, fling to commit, and cancel_

## Accessibility

#### Use cases

People should be able to dismiss the side sheet using assistive technology.

#### Interaction & style

Material requires that a close affordance, such as a close icon button, is always present within a side sheet.

[IMAGE DO] Side sheet correctly designed with close icon in upper right corner.

**DO:** _A close icon button makes the side sheet easy to dismiss_

[IMAGE DON'T] Side sheet incorrectly designed with no close icon button.

**DON'T:** _Without a close icon button, people can’t predict the opening and closing flow of side sheets, or know if the sheet is transient or permanent_

#### Initial focus

Actions within a side sheet can be focused by tab order using a keyboard or switch control.

[IMAGE] Side sheet diagram showing the focus order of headline, close, save, cancel.

_Visible focus shown on the available actions within a side sheet:
- Headline
- Close
- Cancel
- Save_

#### Keyboard navigation

|

Keys |

Actions
|

**Tab** |

Focus lands on (non-disabled) icon button

|

**Space** or **Enter** |

Activates the (non-disabled) icon button

#### Labeling

The accessibility role for a side sheet is **Dialog**.

[IMAGE] Side sheet showing the accessibility role as dialog.

_The role for side sheets is **Dialog**_
