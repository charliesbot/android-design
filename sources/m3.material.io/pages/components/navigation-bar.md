# Navigation bar
Source: https://m3.material.io/components/navigation-bar

## Overview

- Use navigation bars in compact or medium window sizes
- Can contain 3-5 destinations of equal importance
- Destinations don't change. They should be consistent across app screens.

[IMAGE] Two navigation bars of different widths with 4 destinations.

_Navigation bar for compact and medium window sizes_

#### Availability & resources

#### M3 Expressive update

**May 2025**

A new flexible navigation bar was introduced to replace the baseline navigation bar. It’s shorter and supports horizontal navigation items in medium windows. More on M3 Expressive

Variants and naming:
- Baseline navigation bar is no longer recommended
- Added **flexible **navigation bar
  - Shorter height
  - Can be used in medium window sizes with horizontal navigation items

Color:
- Active label changed from **on-surface-variant** to **secondary**

[IMAGE] Navigation bar in M3 Expressive. It’s shorter than the baseline nav bar.

_The flexible navigation bar is shorter and can be used in medium windows with horizontal nav items_

#### Differences from M2

- Color: New color mappings and compatibility with dynamic color
- Elevation: No shadow
- Layout: Container height is taller
- States: The active destination can be indicated with a pill shape in a contrasting color
- Name: Bottom navigation has been renamed **navigation bar**

[IMAGE] M2 nav bar with a drop shadow and no active indicator.

_M2: A drop shadow indicates placement on top of content. Filled and regular weight icons indicate active states._

[IMAGE] M3 nav bar with a surface color and active indicator.

_M3: Taller and no drop shadow. Filled icons and an active indicator indicate active state._

## Specs

#### Variants

[IMAGE] The recommended flexible navigation bar.

_- Flexible navigation bar_

##### Baseline variants

The baseline nav bar is no longer recommended, and should be replaced by the flexible nav bar, which is shorter and supports horizontal navigation items in medium windows. View baseline nav bar specs

[IMAGE] 1 baseline navigation bar.

_- Navigation bar (not recommended)_

|

Variant |

M3 |

M3 Expressive
|

Flexible navigation bar |

-- |

Available
|

Navigation bar |

Available |

Not recommended.

Use **flexible navigation bar**.

#### Configurations

In compact windows, navigation bars use vertical items. In medium windows, navigation bars should use horizontal items.

[IMAGE] Two size configurations for navigation bar and items.

_- Vertical navigation items

- Horizontal navigation items_

|

Category
|

Configuration
|

M3
|

M3 Expressive

|

Navigation item layout
|

Vertical (default)
|

Available
|

Available

|

Horizontal
|

--
|

Available

#### Tokens & specs

Use the table's menu to switch between token sets for the navigation bar and the nav items. Learn about design tokens

#### Anatomy

[IMAGE] Seven elements of the navigation bar.

_- Container

- Icon

- Label text

- Active indicator

- Small badge (optional)

- Large badge (optional)

- Large badge label_

#### Color

Color values are implemented through design tokens. For designers, this means working with color values that correspond with tokens; in implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] Six color roles of the navigation bar.

_Navigation bar color roles used for light and dark schemes:

- Surface container

- On-secondary container

- Secondary

- Secondary container

- On-surface variant

- On-surface variant_

For badge color roles, go to badge specs.

#### States

States are visual representations used to communicate the status of a component or an interactive element.

[IMAGE] Four states of the navigation bar items.

_- Enabled

- Hovered (8% state layer)

- Focused (10% state layer)

- Pressed (10% state layer)_

#### Measurements

The navigation bar stretches the full window width.

[IMAGE] Navigation bar padding and size measurements.

_Navigation bar padding and size measurements_

Vertical navigation items dynamically change width to equally fit the container. Horizontal navigation items have a fixed width, so extra space is added to the ends of the navigation bar instead.

[IMAGE] Navigation bar and item widths.

_Navigation bar width and margins for compact and medium windows.

- Vertical navigation item

- Margin from window edge
- Horizontal navigation item_

#### Baseline navigation bar

[IMAGE] 7 elements of baseline navigation bar.

_- Container

- Icon

- Label text

- Active indicator

- Small badge

- Large badge

- Large badge label_

##### Tokens & specs

These tokens are for the baseline navigation bar.

##### Color

Color values are implemented through design tokens. For designers, this means working with color values that correspond with tokens; in implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] 6 color roles of baseline navigation bar.

_Navigation bar color roles used for light and dark schemes:

- Surface

- On secondary container

- On surface

- Secondary container

- On surface variant

- On surface variant_

For badge color roles, go to badge specs.

##### States

States are visual representations used to communicate the status of a component or an interactive element.

[IMAGE] 4 states of baseline navigation bar.

_Navigation bar states:

- Enabled

- Hovered

- Focused

- Pressed_

#### Measurements

[IMAGE] Baseline navigation bar padding and size measurements.

_Navigation bar padding and size measurements_

[IMAGE] Baseline navigation bar target size and margins.

_Navigation bar target size and margins_

#### Configurations

[IMAGE] 3 configurations of the baseline navigation bar.

_- 3 destinations

- 4 destinations

- 5 destinations_

## Guidelines

[IMAGE] A nav bar with vertical items in a compact window, and horizontal items in a medium window.

_Navigation bars adapt to different breakpoints_

#### Usage

Navigation bars provide access to three to five destinations. The nav bar is positioned at the bottom of windows for convenient access.

Each destination is represented by an icon and label text. One navigation destination is always active.

When a navigation bar icon is tapped or focused, people are taken to the navigation destination associated with that icon.

[IMAGE] A nav bar for a music app with 4 destinations: Home, Browse, Radio, Library, It’s in a compact window.

_Navigation bars can have three to five destinations_

Navigation bars should be used for:
- Three to five main pages in the product
- Mobile or tablet only

Navigation bars shouldn’t be used for accessing single tasks, such as viewing one email.

[IMAGE] A nav bar for a music app with 4 destinations: Home, Browse, Radio, Library. It’s in a medium window.

_On mobile or tablet, navigation bars should be used for top-level destinations_

The navigation items can be **vertical** or **horizontal**.
- Use vertical items in compact windows, like mobile
- Use horizontal items in medium windows, like tablets

[IMAGE] A nav bar with vertical items in a compact window, and horizontal items in a medium window.

_Vertical navigation items work best in compact windows. Horizontal items work best in medium windows._

For products with more than five navigation items, don’t use a navigation bar; the elements may collide and there likely won’t be enough space for translated text.

Instead, consider using tabs to organize similar content within a page, or hide the navigation behind a menu icon using a modal expanded navigation rail.

[IMAGE DON'T] A nav bar with 7 items in a compact window.

**DON'T:** _Avoid putting more than five navigation items in a navigation bar_

[IMAGE DON'T] A nav bar with no labels for each page item.

**DON'T:** _Don’t remove the labels from navigation items_

[IMAGE DON'T] A nav bar with 2 page items.

**DON'T:** _Don’t use a navigation bar for fewer than three destinations. Instead, use tabs._

[IMAGE] A nav bar is on the Library page of a music app. Tabs at the top of the page have secondary navigation for playlists, artists, albums, and songs.

_Use navigation for distinct pages and tabs for related content within a page_

[IMAGE DON'T] Nav bar using horizontal items in a compact window. The items are too wide and flow off screen.

**DON'T:** _Navigation bar destinations have fixed positions. Don’t scroll them or modify their positions._

#### Anatomy

[IMAGE] 6 elements of the nav bar.

_- Container

- Icon

- Label text

- Active indicator

- Large badge (optional)

- Small badge (optional)_

##### Container

The container should always be placed at the bottom of the product and span the full length of the window. Navigation items are centered within the container.

The container has a color fill to provide separation from other content.

[IMAGE] The nav bar at the bottom of a medium window has a color fill to differentiate from the background.

_The navigation bar container holds all elements_

##### Navigation items

Navigation items hold all elements for each destination: the icon, label text, and active indicator. They can be **vertical**, with the text below the icon and indicator, or **horizontal**, with the icon and text beside each other inside the indicator.

Vertical items are best in compact windows, and horizontal items are best in medium windows.

Horizontal items are centered in the nav bar with outer margins.

[IMAGE] The nav bar in a medium window with padding on each side.

_The navigation bar is divided into equal-width segments with padding from the window edge_

##### Icons

Navigation rail items must use icons that symbolize the content of their page. Browse popular icon.

Use a filled icon for the active destination and outlined icons for inactive destinations. If an icon doesn’t have a filled version, apply **semibold** weight to the icon instead.

[IMAGE DO] An active nav item with a filled icon compared to inactive items with outlined icons.

**DO:** _Use filled icons when the navigation item is active_

[IMAGE CAUTION] An active nav item with a semibold icon compared to inactive items with outlined icons.

**CAUTION:** _If a filled version of an icon is unavailable, the icon’s weight must increase_

Active and inactive icons must have a minimum 3:1 contrast ratio with the container.

[IMAGE DON'T] 4 nav items that are each different colors with low contrast with the background.

**DON'T:** _Don’t use multiple or low-contrast colors in a navigation bar, as they make it harder for people to distinguish the active item and navigate to other destinations_

##### Active indicator

The active indicator shows which page from the nav bar is currently being displayed.

[IMAGE DO] The current page in a nav bar has an active indicator.

**DO:** _Use the active indicator only for the active destination_

[IMAGE DON'T] All items in a nav bar have active indicators.

**DON'T:** _Don’t use the active indicator for more than one destination at a time_

##### Label text

The label text should be a short, meaningful description of each navigation destination and another way for people to understand an icon’s meaning.

All navigation items require a label text. It should be 1-2 words.

[IMAGE] A nav bar on a music app with clearly labelled destinations: home, browse, radio, library.

_Label text must be brief and clear_

[IMAGE DO] A nav bar with 1-word labels for each page.

**DO:** _Use brief text labels to identify the purpose of a destination_

[IMAGE DON'T] A nav bar with “Music catalog” for a label. The label is truncated.

**DON'T:** _Don’t wrap or truncate text as it can make the label hard to understand_

[IMAGE DON'T] A nav bar with “Music catalog” for a label. The label is a smaller size to make the text fit.

**DON'T:** _Don’t shrink longer text to fit on a single line_

##### Badges (optional)

Navigation bars can display badges in the upper right corners of the destination icon.

Badges can contain dynamic information, such as the number of new messages.

[IMAGE] A nav bar with a destination called “Go” with a small badge and one called “Saved” with a large badge saying “3.”

_Use a small badge to indicate an update, and a large badge to show the amount of updates_

[IMAGE] Horizontal nav items with the badges in the same place of the icon as vertical nav items.

_Badges overlap the icon in both vertical and horizontal navigation items_

#### Placement

The floating action button (FAB) is placed above the navigation bar. Nav bars are always placed at the bottom of the window.

[IMAGE DO] The FAB should be right-aligned above the navigation bar

**DO:** _The FAB should be right-aligned above the navigation bar_

[IMAGE DON'T] A mobile page with a FAB overlapping a nav bar.

**DON'T:** _Don’t cover the navigation bar with a FAB_

Navigation bars can be temporarily covered by dialogs, bottom sheets, navigation drawers, the on-screen keyboard, or other elements needed to complete a flow. They should not be permanently obstructed on any screen.

[VIDEO] The navigation bar being covered by a bottom sheet when users take an action on screen.

_The search feature of the screen triggers the on-screen keyboard, temporarily covering the bottom navigation bar until the search flow is completed_

#### Adaptive design

Adaptive design allows an interface to respond or change based on context, such as the user, device, and usage. More on adaptive design

##### Resizing

Only use navigation bars for compact and medium breakpoints.

**Compact**: For narrow windows, use a navigation bar or modal navigation rail.

**Medium**: Use a navigation bar or navigation rail. Decide based on whether horizontal or vertical space is more important.

**Expanded and extra-large**: Use a navigation rail instead. Decide based on available window space and the number of navigation destinations.

[VIDEO] A music app with a nav bar adapting to larger screens, where it changes to horizontal items, and then into a nav rail.

_Navigation bars are best suited for compact and medium breakpoints_

The navigation bar container spans 100% of the window width.

[IMAGE] Navigation bar spanning the full width of a compact window.

_Navigation bars use 100% of the screen width_

The navigation bar is used on smaller devices. It’s not intended for desktop.

[IMAGE DON'T] Navigation bar spanning the full width of an expanded window size.

**DON'T:** _Don’t use navigation bars for desktop layouts. Instead, use a navigation rail or tabs._

##### Presentation

In medium breakpoints, use horizontal nav items to better use available space.

Horizontal nav items should remain centered with the same padding at each breakpoint.

[IMAGE] Horizontal nav items have the same width in medium and expanded windows. Only the padding changes.

_A navigation bar in horizontal orientation keeps the same spacing between destinations_

#### Behavior

##### Navigation

When selecting a navigation bar item not currently selected, the product navigates to that destination’s screen using a top level transition pattern. It can either remember where you left off, or reset to the default view.
- **Preserve state**: If someone has interacted with this destination, it returns to their scroll position, current tab, and in-line search status.
- **Reset state**: Any prior user interactions are reset, including scroll position, tab selection, and in-line search.

Choose the behavior that best suits the product and user needs. For example, an app that requires frequent switching between sections should preserve each section’s state.

[VIDEO] After selecting an item on the bottom navigation bar, the app navigates to that destination’s screen

_After selecting an item on the bottom navigation bar, the app navigates to that destination’s screen_

Re-selecting the currently active destination should reset the scroll position to the top of the page.

**Don't swipe between destinations**
Swiping across the screen does not navigate between destinations, and is not supported by the navigation bar. Swipe behavior should be reserved for related items, such as cards in a carousel, or actions such as archiving a list item.

[VIDEO] Selecting the currently active nav item scrolls the page content back to the top.

_Selecting the already selected navigation item scrolls to the top of the screen_

##### Scrolling

Upon scroll, the navigation bar can appear or disappear.

Don’t hide the navigation bar on scroll when a screen reader is active.

[VIDEO] When scrolling on the page, the nav bar scrolls the opposite direction off screen.

_Scrolling downward can hide the navigation bar; scrolling upward reveals it_

##### Selection

The icon becomes filled and the active indicator expands from the center of the icon when switching between destinations.

The active indicator animation should only apply on one axis to better represent a flat, shared plane.

[VIDEO] The active indicator expands from the target when a destination is selected

_An active indicator appears when the item is selected._

When a destination is tapped, the destination screens use a top level transition pattern. In addition, the icon becomes filled and the active indicator expands from the center of the icon.

[VIDEO] Selecting the home item changes the entire page contents.

_Tapping a destination uses a top level transition pattern_

## Accessibility

#### Use cases

People should be able to do the following using the assistive technology:
- Move between navigation destinations
- Select a particular navigation destination from a set
- Get appropriate feedback based on input type

#### Interaction & style

**Touch**
- When a navigation item is tapped, the active indicator appears in place, providing feedback that it’s selected

- A touch ripple passes through the indicator

- The icon switches from outlined to filled

- The icon changes color

[VIDEO] On a navigation bar, when the Home and Explore icons are tapped, an active indicator is displayed as interaction feedback.

_Touch: Tap_

**Cursor**
- When hovered, the active indicator appears in a reduced state providing a visual cue that the destination is interactive

- When clicked (in both active and inactive states), a ripple passes through the indicator

- The icon switches from outlined to filled

- The icon changes color, becoming darker

[VIDEO] On a navigation bar, the hover and click interactions on the Home and Explore icons have different interaction feedback.

_Cursor: Hover, Click_

##### Text scaling and truncation

When someone sets their device to show a larger text size, the navigation bar should grow vertically to accommodate larger labels while retaining the default padding. It’s okay for scaled text to wrap in navigation items.

To remain accessible, ensure the full label is always visible on-screen at up to 2x text sizing. Beyond this size, text can truncate.

[IMAGE] Nav bar with text scaled to 1.5x size. Some labels are on two lines, others are on one line.

_Text scaled to 1.5 size_

[IMAGE] Nav bar with text scaled to 2x size. Some labels wrap to two lines.

_Text scaled to 2x size_

#### Initial focus

Initial focus lands directly on the first navigation item, since that is the first interactive element of the component.

[IMAGE] Focus order and keyboard navigation of a nav bar.

_Focus lands on first navigation item_

[IMAGE] Activating a nav item with space on a keyboard.

_The navigation item is selected with Space/Enter_

#### Visual indicators

Use a filled icon with a bold label for selected destinations. For unselected destinations use an outlined icon with a medium label.

If an icon doesn’t have a filled style, use a thicker or heavier version of the icon instead.

[IMAGE DO] A nav bar with a filled icon for the selected nav item.

**DO:** _Use a filled icon for the selected navigation destination to differentiate from the other destinations_

[IMAGE DON'T] A nav bar with an outlined icon for the selected nav item.

**DON'T:** _Don’t use outlined icons on selected nav items_

[IMAGE] 2 nav items, one selected, one unselected.

_When selected, the icon fills, darkens, and is backed by an active indicator shape_

#### Keyboard navigation

| Keys | Actions
| Tab | Move between navigation items
| Space / Enter | Selects the focused navigation item

#### Labeling elements

The accessibility label for a navigation item is typically the same as the destination name.

[IMAGE] Accessibility label and role defined for a Home icon on a navigation bar.

_A navigation bar’s accessibility label can incorporate its adjacent UI text_

When the visible UI text is ambiguous, accessibility labels need to be more descriptive. For example, a navigation destination visibly labeled **Library **would benefit from additional information in its accessibility label to clarify the destination’s intent.

Note: On Android Views (MDC-Android), a more descriptive accessibility label is not available and the role is not announced.

[IMAGE] Accessibility labels of a navigation bar.

_While the visible label text reads **Library**, the accessibility label for this destination clarifies its function: **Music library**_
