# Navigation drawer
Source: https://m3.material.io/components/navigation-drawer

## Overview

star

Note:
The navigation drawer is no longer recommended in the Material 3 Expressive update. For those who have updated, use an expanded navigation rail, which has mostly the same functionality of the navigation drawer and adapts better across breakpoints.

- Use standard navigation drawers in expanded, large, and extra-large breakpoints
- Use modal navigation drawers in compact and medium breakpoints
- Can be open or closed by default
- Two variants: standard and modal
- Put the most frequent destinations at the top and group related destinations together

[IMAGE] 2 variants of navigation drawers: standard and modal.

_- Standard navigation drawer
- Modal navigation drawer_

#### Availability & resources

#### M3 Expressive update

**May 2025**

The navigation drawer is no longer recommended. Use the expanded navigation rail instead. More on M3 Expressive

#### Differences from M2

- Color: New color mappings and compatibility with dynamic color
- Variants: Distinguishes two separate variants of navigation drawer: Standard and modal
- Shape: Rounded corners at the ending edge of the drawer
- States: Updated color and shape for indicating selected state

[IMAGE] M2 navigation drawer with 4 destinations in a mail app. The active destination “Inbox” is rectangular.

_M2: Navigation drawer had square corners and a rectangular shape indicating the active destination_

[IMAGE] M3 navigation drawer with 4 destinations in a mail app. The active destination “Inbox” has rounded corners.

_M3: Navigation drawer has rounded corners, new color mappings, and an updated style for indicating the active destination_

## Specs

star

Note:
The navigation drawer is no longer recommended in the Material 3 Expressive update. For those who have updated, use an expanded navigation rail, which has mostly the same functionality of the navigation drawer and adapts better across breakpoints.

[IMAGE] Navigation drawer diagram numbering 7 elements

_- Container
- Headline
- Label text
- Active indicator
- Badge label text
- Scrim
- Icon_

#### Tokens & specs

The navigation drawer has one token set. Learn about design tokens

#### Color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] Navigation drawer diagram numbering 9 color roles.

_Navigation drawer color roles used for light and dark schemes:
- Surface container low
- On surface variant
- On secondary container
- On secondary container
- Secondary container
- On secondary container
- On surface variant
- On surface variant
- Scrim_

For divider color roles, go to divider specs.

#### States

States are visual representations used to communicate the status of a component or interactive element. Learn more about interaction states

[IMAGE] 4 navigation drawers illustrating enabled, hovered, focused, and pressed states.

_Navigation drawer states:
- Enabled
- Hovered
- Focused
- Pressed_

State specs are in the tokens module above

#### Measurements

##### Standard navigation drawer

[IMAGE] Standard navigation drawer with measurements shown for various elements.

_Element size measurements_

[IMAGE] Standard navigation drawer with measurements shown for padding and margins.

_Padding and margins_

| Attribute | Value
| Container height
| 100%
| Container width
| 360dp
| Container shape
| 0,16,16,0dp corner radii
| Icon size
| 24dp
| Active indicator height
| 56dp
| Active indicator shape
| 28dp
| Active indicator width
| 336dp
| Horizontal label alignment
| Start-aligned
| Left padding
| 28dp
| Right padding
| 28dp
| Active indicator padding
| 12dp
| Padding between elements
| 0dp

##### Modal navigation drawer

[IMAGE] Modal navigation drawer with measurements shown for various elements.

_Element size measurements_

[IMAGE] Modal navigation drawer with measurements shown for padding and margins.

_Padding and margins_

| Attribute | Value
| Container height
| 100%
| Container width
| 360dp
| Icon size
| 24dp
| Active indicator height
| 56dp
| Active indicator shape
| 28dp
| Active indicator width
| 336dp
| Horizontal label alignment
| Start-aligned
| Left padding
| 28dp
| Right padding
| 28dp
| Active indicator padding
| 12dp
| Padding between elements
| 0dp

## Guidelines

star

Note:
The navigation drawer is no longer recommended in the Material 3 Expressive update. For those who have updated, use an expanded navigation rail, which has mostly the same functionality of the navigation drawer and adapts better across breakpoints.

[IMAGE] Navigation drawer with 4 primary destinations

#### Usage

Navigation drawers provide access to destinations and app functionality, such as switching accounts. They can either be permanently on-screen or opened and closed by a navigation menu icon. One navigation destination is always active.

Navigation drawers are recommended for:
- Apps with 5 or more top-level destinations
- Apps with 2 or more levels of navigation hierarchy
- Quick navigation between unrelated destinations
- Replacing the navigation rail or navigation bar on large screens

[IMAGE DO] Navigation drawer with multiple destinations in a mail app.

**DO:** _Use a navigation drawer for 5 or more primary destinations, or more than 1 level of navigation hierarchy_

Avoid using a navigation drawer with other primary navigation components, such as a navigation bar.

Instead, choose a single navigation component based on product requirements and breakpoints:
- Navigation bars for compact breakpoints
- Navigation rails for medium and expanded breakpoints
- Standard navigation drawers for expanded, large and extra-large breakpoints

[IMAGE CAUTION] Standard navigation drawer and navigation bar used together.

**CAUTION:** _Avoid using two navigation components on the same screen_

There are two variants of navigation drawers:
- Standard navigation drawer
- Modal navigation drawer

[IMAGE] Standard navigation drawer with destinations in mail app.

_Standard navigation drawer_

[IMAGE] Modal navigation drawer with destinations and scrim.

_Modal navigation drawer_

##### Standard navigation drawer

Standard navigation drawers provide access to drawer destinations and app content for layouts in expanded, large, and extra-large breakpoints.

Standard drawers can be permanently visible (best for frequently switching destinations) or opened and closed by tapping a menu icon (best for focusing more on screen content).

In medium and compact breakpoints, use modal drawers instead.

[IMAGE] Standard navigation drawer in a mail app with active destination “Inbox” next to app content.

_Standard navigation drawer providing access to drawer destinations next to app content_

##### Modal navigation drawer

Modal navigation drawers use a scrim to block interaction with the rest of an app’s content, and don’t affect the screen’s layout grid.

Modal navigation drawers can be used in any breakpoint, but are primarily used in compact and medium sizes where space is limited or prioritized for app content.

They can be swapped with standard drawers on expanded, large, and extra-large breakpoints.

[IMAGE] Modal navigation drawer with 1 active destination and scrim.

_Modal navigation drawer using a scrim to block interaction with the rest of an app’s content_

Modal navigation drawers are always opened by an action outside of the drawer, such as clicking a navigation menu icon in a navigation rail.

Modal drawers can be dismissed by:
- Selecting a drawer item
- Tapping the scrim
- Swiping toward the drawer’s anchoring edge (for example, swiping right-to-left for a left-aligned navigation drawer)

[IMAGE] Diagram noting a navigation menu icon in a navigation rail.

_A modal drawer opened by an action such as clicking a navigation menu icon (1)_

Modal drawers can be dismissed by tapping the scrim or swiping the drawer toward its anchoring screen edge.

[IMAGE] 2 modal navigations illustrating tapping the scrim or swiping to dismiss a modal drawer

_1. Dismiss by tapping the scrim
2. Dismiss by swiping the drawer_

#### Anatomy

Navigation drawers are essentially a list contained within a side sheet. They can also include headers, subheads, and dividers to organize longer lists.

[IMAGE] Navigation drawer diagram numbering 8 elements.

_Navigation drawers can include headers, subheads, and dividers to organize longer lists
- Active Indicator
- Icon
- Label
- Badge label
- Sheet
- Divider
- Section label (optional)
- Scrim_

##### Sheet

A sheet holds all navigation drawer elements. Side sheets are used as the container for standard and modal navigation drawers.

Navigation drawers that open from the side are always placed on the start edge of the screen, on the left for left-to-right (LTR) languages, and on the right for right-to-left (RTL) languages.

[IMAGE DO] Modal navigation drawer opening from left side of screen.

**DO:** _A navigation drawer opens from the left side of the screen for left-to-right languages_

##### Divider (optional)

Dividers can be used to separate groups of destinations within the navigation drawer.

[IMAGE DO] Navigation drawer using horizontal dividers to separate a group of destinations

**DO:** _Use full-width dividers (1) to separate groups of destinations_

[IMAGE DON'T] Navigation drawer using horizontal dividers to separate individual destinations

**DON'T:** _Don’t use dividers to separate individual destinations_

##### Active indicator

The active indicator is a background shape communicating which destination of the navigation drawer is currently being displayed.

[IMAGE] Navigation drawer diagram numbering 1 element.

_The active indicator (1) is a background shape communicating which destination of the navigation drawer is currently being displayed_

##### Label text and icons

Destinations in a navigation drawer take the form of actionable list items. Each item describes its destination using label text and an optional icon.

[IMAGE] Navigation drawer diagram numbering 2 elements.

_Actionable list items in a navigation drawer describe each destination using (1) an optional icon and (2) required label text_

Label text should be clear and short enough that it isn’t cut off by the sheet.

[IMAGE] Navigation drawer using only label text for 4 destinations. Label text “Inbox” in active destination.

_Navigation drawers can use text labels without icons_

[IMAGE DO] Navigation drawer with 1 truncated text label.

**DO:** _Keep text labels concise, but truncate them if they extend beyond the container width_

[IMAGE DON'T] Navigation drawer with 1 text label with wrapped label text.

**DON'T:** _Don’t wrap label text_

[IMAGE DON'T] Navigation drawer with 1 text label featuring smaller text.

**DON'T:** _Don’t shrink text size in order to fit a text label on a single line_

Icons can supplement labels as indicators of a destination. When used, they should always be placed before text. Other app components and content should reference these icons.

[IMAGE DO] Navigation drawer with active destination “Inbox” featuring recognizable icon.

**DO:** _Use recognizable icons when conventions exist_

[IMAGE DON'T] Navigation drawer with 4 destinations, 2 with text label and icon, 2 with only text label.

**DON'T:** _Don’t apply icons to some destinations and not others. Icons should be used for all destinations, or none._

##### Section label (optional)

Short subhead section labels can help group related destinations in the navigation drawer.

[IMAGE] Navigation drawer showing subhead section labels.

_Related destinations can be grouped using short subhead section labels in the navigation drawer_

##### Scrim (modal only)

Modal navigation drawers use a scrim to block interaction with the rest of the app. The scrim is placed directly behind the drawer’s sheet and can be tapped or clicked to dismiss the drawer.

[IMAGE] Modal navigation drawer with scrim placed behind.

_Scrim applied behind a modal navigation drawer_

#### Responsive layout

A product’s navigation component should change to suit the breakpoint and form factor of the screen.

Modal navigation drawers can be used at any breakpoint but are most common in compact and medium breakpoints.

Standard navigation drawers are best for expanded, large, and extra-large breakpoints.

Use a transition when swapping components. For example, when switching from a portrait to landscape layout, the navigation rail should transform into a navigation drawer.

[IMAGE] Navigation rail changing to navigation. drawer on a larger screen

_Standard navigation drawers change size to suit the device’s screen_

##### Compact breakpoint

Use modal navigation drawers in compact breakpoints. Or swap the drawer for a navigation bar.

On web, when the screen size is smaller than 320 CSS pixels, swap the navigation drawer for a navigation bar to ensure accessibility.

[IMAGE] Modal navigation drawer with 1 active destination.

_Use a modal navigation drawer on mobile screens_

##### Medium & expanded breakpoints

Use a modal navigation drawer alone or with a navigation rail on medium and expanded breakpoints.

When a navigation rail and modal navigation drawer are used together, the drawer can repeat destinations in the navigation rail as long as the drawer offers enough visual separation between levels of the navigation hierarchy.

A standard navigation drawer can be used in single pane layouts in expanded breakpoints.

[VIDEO] Navigation drawer on tablet with 1 active destination.

_Use a navigation rail on tablet screens, or also allow a drawer to open and close via a menu icon_

##### Large and extra-large breakpoints

For web experiences on laptop and desktop devices, use either a standard navigation drawer, or a navigation rail that transitions into a modal navigation drawer.

[IMAGE] Navigation drawer showing 1 active destination.

_Use a standard navigation drawer on large and desktop screens_

#### Behavior

##### Scrolling

Navigation drawers can be vertically scrolled, independent of the rest of the screen’s content and UI. If the list of navigation destinations is longer than the height of the drawer, the drawer’s contents can be scrolled within the drawer.

[VIDEO] A navigation drawer on the left region of an email app is scrolled vertically without disturbing the rest of the content.

_When a navigation drawer is scrolled, the body content should remain stationary_

##### Visibility

**Dismissible standard drawers** can be used for layouts that prioritize content (such as a photo gallery) or for apps where users are unlikely to switch destinations often. They should use a visible navigation menu icon to open and close the drawer.

[IMAGE] Side-by-side standard navigation drawer opened and then closed after tapping menu bar.

_A standard dismissible navigation drawer is opened and closed by tapping the navigation menu icon in the app bar (1), and remains open until the menu icon is tapped again (2)_

**Permanently visible standard drawers** allow quick navigation between unrelated destinations. They can’t be closed or dismissed by the user.

[IMAGE] Standard navigation drawer moving between destinations.

_A permanently-visible standard navigation drawer on desktop_

##### Appearing

When a navigation drawer animates on screen, it uses an enter and exit transition pattern.

[VIDEO] In an email app, navigation drawer opens on menu icon click, and closes on outside click.

_A navigation drawer animating on screen_

## Accessibility

star

Note:
The navigation drawer is no longer recommended in the Material 3 Expressive update. For those who have updated, use an expanded navigation rail, which has mostly the same functionality of the navigation drawer and adapts better across window size classes.

#### Use cases

Users should be able to:
- Move between navigation destinations with assistive technology
- Select a particular navigation destination from a set
- Get appropriate feedback based on input type

#### Interaction & style

**Touch**
- When a navigation item is tapped, the active indicator appears in place, providing feedback to the user that it is selected
- A touch ripple passes through the indicator
- The icon switches from outlined to filled
- The icon changes color, becoming darker

[VIDEO] Active indicator and other visual cues appear when nav drawers are tapped.

_Touch: Tap_

**Cursor**
- When hovered, the hover indicator appears providing a visual cue that the destination is interactive
- When clicked, a ripple passes through the indicator
- The icon switches from outlined to filled
- The icon changes color, becoming darker in light theme and lighter in dark theme, to increase the contrast

[VIDEO] When a cursor goes over a navigation drawer, a hover indicator appears. When tapped, the active indicator appears.

_Cursor: Hover, Click_

#### Initial focus

Initial focus lands directly on the first navigation item, since that is the first interactive element of the component.

[IMAGE] 1. Tab lands on the first navigation item, Inbox. 2. Down arrow to get to the second navigation item, Outbox.

_Focus lands on first navigation item_

#### Closing

The modal navigation drawer can be dismissed by selecting the scrim that covers the rest of the screen.

[IMAGE] A navigation drawer with a scrim covering the body content. A touch target is selecting the scrim.

_Select the scrim to close the navigation drawer_

#### Visual indicators

Icons are the primary focus of the navigation and such give the dominant cue of its state. Use a filled icon for the selected destination to differentiate from the outlined icons of non-selected destinations.

[IMAGE] Space + enter is used to select the navigation item inbox.

_The navigation item is selected via **Space**/**Enter**_

[IMAGE DO] A navigation drawer with the home destination using a filled icon.

**DO:** _Use a filled icon for the selected navigation destination to differentiate from the other destinations_

[IMAGE DON'T] A navigation drawer with the home destination using an outlined icon.

**DON'T:** _Avoid keeping the icon style for the selected navigation destination the same as unselected destination's icons. This removes an important visual indicator of which destination is active._

[IMAGE] A selected home icon using a filled icon and active indicator and a unselected home icon using an outlined icon.

_When selected, the icon fills, darkens in light theme (or lightens in dark theme), and is backed by an active indicator shape_

#### Keyboard navigation

| **Keys** | **Actions**
| Tab | Focus lands on the first navigation destination
| Space or Enter | Selects the focused navigation destination, and focus moves to the newly opened section (if applicable)
| Arrow | Navigate between destinations within the navigation drawer

#### Labeling elements

The accessibility label for a navigation item is typically the same as the destination name.

If the UI text is correctly linked, assistive tech (such as a screenreader) will read the UI text followed by the component’s role.

For Android Views (MDC-Android), a more descriptive accessibility label is not available to be set and the role is not announced.

[IMAGE] A navigation drawer item’s label text and accessibility label both read “photos.” The role is “tab.”

_A navigation drawer’s accessibility label can incorporate its adjacent UI text_

When the visible UI text is ambiguous, accessibility labels need to be more descriptive. For example, a navigation destination visibly labeled **Recents** would benefit from additional information in its accessibility label to clarify the destination’s intent.

[IMAGE] A navigation drawer item’s label text is “recents”, the accessibility label is “recent images.” The role is “tab.”

_While the visible label text reads **Recents,** the accessibility label for this destination clarifies its function: **Recent images**_
