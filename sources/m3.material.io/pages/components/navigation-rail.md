# Navigation rail
Source: https://m3.material.io/components/navigation-rail

## Overview

- Use navigation rails in medium, expanded, large, or extra-large window sizes
- Can contain 3-7 destinations plus an optional FAB
- Always put the rail in the same place, even on different screens of an app

[VIDEO] Navigation rail with 4 destinations, 1 active, and FAB.

_Collapsed and expanded navigation rails can transition between each other on any device, including:

1. Large or medium window size classes like tablets
2. Compact window size classes like phones in portrait orientation_

#### Availability & resources

#### M3 Expressive update

**May 2025**

A **collapsed** and **expanded** navigation rail have been introduced to replace the baseline nav rail. The expanded nav rail is meant to replace the navigation drawer. More on M3 Expressive

Variants and naming:
- The baseline **navigation rail** is no longer recommended
- Added two wider navigation rails:
  - **Collapsed:** replaces baseline nav rail
  - **Expanded**: replaces navigation drawer

Configurations:
- Expanded rail modality:
  - Non-modal
  - Modal

- Expanded behavior:
  - Transition to collapsed navigation rail
  - Hide when collapsed

- Color:
  - Active label on vertical items changed from **on surface variant** to **secondary **

[IMAGE] A collapsed and expanded navigation rail.

_The collapsed and expanded navigation rails match visually and can transition into each other_

#### Differences from M2

- Behavior: Predictive back interaction
- Color: New color mappings and compatibility with dynamic color
- States: The active destination can be indicated with a pill shape in a contrasting color

[IMAGE] M2 navigation rail with 1 colored and filled icon showing the active state and 3 inactive icons.

_M2: The navigation rail uses icon color, weight, and fill to communicate which destination is active_

[IMAGE] M3 navigation rail with 1 icon surrounded by a pill shape in contrasting color to show the active state.

_M3: The navigation rail uses a pill-shaped active indicator to communicate which destination is active_

## Specs

#### Variants

[IMAGE] 2 variants of navigation rails.

_- Collapsed navigation rail
- Expanded navigation rail_

##### Baseline variants

The baseline navigation rail is no longer recommended, and should be replaced by the collapsed navigation rail. View baseline tokens

[IMAGE] Baseline navigation rail.

_- The baseline navigation rail is no longer recommended_

|

Variant |

M3 |

M3 Expressive
|

Collapsed navigation rail
|

-- |

Available
|

Expanded navigation rail
|

-- |

Available
|

Navigation rail (baseline)
|

Available |

Not recommended.

Use **collapsed navigation rail**.

#### Configurations

[IMAGE] Standard and modal layouts of navigation rail.

_- Expanded layout: standard
- Expanded layout: modal_

|

Category |

Configuration |

M3 |

M3 Expressive
|

Expanded layout |

Standard (default) |

Available as navigation drawer
|

Available
|

Modal |

Available as navigation drawer
|

Available
|

Expanded behavior |

Hide when collapsed |

-- |

Available

#### Tokens & specs

Browse the component elements, attributes, tokens, and their values. Learn about design tokens

#### Anatomy

[IMAGE] 9 elements of collapsed and expanded navigation rails.

_Collapsed and expanded navigation rail elements:
- Container
- Menu (optional)
- FAB or Extended FAB (optional)
- Icon
- Active indicator
- Label text
- Large badge (optional)
- Large badge label (optional)
- Small badge (optional)_

#### Color

Color values are implemented through design tokens. For designers, this means working with color values that correspond with tokens; in implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] Color roles of 9 elements of collapsed and expanded navigation rails in light and dark color schemes.

_Navigation rail color roles used for light and dark schemes:
- Surface container (optional)
- On secondary container
- Secondary container
- Secondary (vertical), On secondary container (horizontal)
- On surface variant
- On surface variant
- Error
- On error
- Error_

#### States

States are visual representations used to communicate the status of a component or an interactive element.

The navigation item’s target area always spans the full width of the nav rail, even if the item container hugs its contents.

[IMAGE] 4 states of collapsed navigation rails.

__

[IMAGE] 4 states of expanded navigation rails.

_- Enabled
- Hovered
- Focused
- Pressed_

#### Measurements

[IMAGE] Padding and measurements for expanded and collapsed navigation rails.

_Navigation rail padding and size measurements_

#### Common layouts

[IMAGE] 4 common layouts of collapsed navigation rail.

[IMAGE] 4 common layouts of expanded navigation rail.

_- Three navigation items
- Three navigation items with a menu
- Three navigation items with a FAB
- Three navigation items with a menu and FAB_

#### Baseline navigation rail

[IMAGE] 8 elements of baseline navigation rail.

_- Container
- Menu icon (optional)
- Icon
- Active indicator
- Label text
- Large badge label (optional)
- Large badge (optional)
- Badge (optional)_

##### Tokens & specs

##### Color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] 8 color roles of baseline navigation rail.

_Navigation rail color roles used for light and dark themes:
- On secondary container
- Secondary container
- On surface
- On surface variant
- On surface variant
- Error
- On error
- Error_

##### States

States are visual representations used to communicate the status of a component or interactive element.

[IMAGE] 8 states of baseline navigation rail.

_Navigation rail states:
- Enabled (on active destination)
- Hovered (on active destination)
- Focused (on active destination)
- Pressed (on active destination)
- Enabled (on inactive destination)
- Hovered (on inactive destination)
- Focused (on inactive destination)
- Pressed (on inactive destination)_

##### Measurements

[IMAGE] Baseline nav rail size measurements.

_Navigation rail size measurements_

[IMAGE] Baseline nav rail padding and margin measurements.

_Navigation rail padding and margin measurements_

##### Configurations

Common arrangements of elements within a navigation rail.

[IMAGE] 5 configurations of the baseline navigation rail.

_- With a menu
- With a FAB
- With menu and FAB, without labels
- All destinations with text labels
- With menu, FAB, and label text for all destinations_

## Guidelines

[IMAGE] Colorful, purple navigation rail shown collapsed and expanded.

_Use the menu icon to transition between collapsed and expanded navigation rails_

#### Usage

The navigation rail can display navigation items, a menu, and a floating action button (FAB) in a vertical orientation.

There are two variants of navigation rails, **collapsed** and **expanded**, which can easily transform into each other when the menu button is selected.

###### Collapsed

The **collapsed** nav rail runs along the leading edge of the window, and should contain 3–7 navigation items. It should not be hidden.

It can be used in medium to extra large breakpoints, such as tablets and desktop. In medium windows with few destinations, consider using a navigation bar instead. Compact windows should always use a navigation bar.

[IMAGE] Collapsed navigation rail with “timer” icon on FAB.

_A navigation rail should be the only visible navigation element_

##### Expanded

The **expanded** navigation rail can be standard or modal, and should always open from a menu icon. An expanded rail can reveal secondary destinations not visible when collapsed.

The **standard** configuration is placed beside body content. It’s best for larger windows with lots of available space.

The **modal** configuration overlaps the body content, and should be opened from a menu icon. Use the modal configuration for:
- Information dense layouts where space is limited
- Products with many navigation items

[IMAGE] Expanded navigation rail shown expanded by default and expanded over screen content.

_A navigation rail can be expanded by default on larger screen sizes, or can be expanded over content on smaller screen sizes_

In immersive experiences, the expanded navigation rail can be hidden entirely, appearing only when the menu icon is selected.

The collapsed navigation rail should not be hidden.

[IMAGE] Navigation rail and hidden navigation rail with menu icon button for expansion.

_The expanded navigation rail can also be hidden, appearing only when the menu icon is selected_

#### Anatomy

[IMAGE] 10 elements of expanded and collapsed navigation rails.

_- Container
- Menu (optional)

- Floating action button (FAB) (optional)

- Icon - active

- Label text - active

- Active indicator

- Icon - inactive

- Large badge (optional)

- Large badge label

- Small badge

- Label text - inactive_

##### Container

The navigation rail should be placed on the leading edge of the window. This is the left side for left-to-right languages, and the right side for right-to-left languages.

The container fill can be turned off so the nav rail appears directly on the surface. When doing this, make sure all items have a minimum of 3:1 color contrast.

[IMAGE] Right-to-left navigation rail in Hebrew, and left-to-right navigation rail in English.

_The navigation rail should be placed on the leading edge of the window_

The navigation rail should always run vertically along the side of a layout. Don’t make it horizontal.

Use a navigation bar for horizontal navigation.

[IMAGE DON'T] Horizontal navigation rail on timer screen.

**DON'T:** _Don’t use the navigation rail horizontally. Use a navigation bar instead._

Navigation rail items can be aligned as a group to the top or center of a layout. On tablets, use center alignment to make it easier to reach items.

The menu icon and FAB should always be top-aligned.

[IMAGE] Navigation rails with different alignments.

_Top and center aligned rail destination placement_

##### Menu (optional)

The menu button can transition between the **collapsed **and **expanded** navigation rails.

Once expanded, the rail can reveal secondary destinations.

When the navigation rail is expanded, the menu icon should change to represent that it can be collapsed.

[IMAGE] Expanded and collapsed navigation rails controlled by a menu icon button.

_A navigation rail can expand to reveal more destinations_

##### Floating action button (FAB) (optional)

The container of the navigation rail is ideal for anchoring the FAB to the top of a screen, placing the app’s key action above navigation destinations.

When nested within another component, such as the navigation rail, the FAB's resting elevation should be level 0.

[IMAGE DO] Navigation rail with a FAB button at the top of the screen.

**DO:** _A top-aligned FAB in the navigation rail_

[IMAGE DON'T] Navigation rail with a FAB button at the bottom of the screen.

**DON'T:** _Avoid placing the FAB below navigation items_

The top of the rail can also be used for a logo, however avoid using logos that could be mistaken as buttons.

Don’t use a logo as a menu button to expand the navigation rail.

[IMAGE CAUTION] Navigation rail with Material design logo at the top of the screen.

**CAUTION:** _Use caution when placing logos in the rail where they might be confused with an action or destination_

##### Active indicator

The active indicator shows which page is being displayed.

[IMAGE DO] Navigation rail with active indicators present for the current screen.

**DO:** _Use the active indicator only for the current open page_

[IMAGE DON'T] Navigation rail with active indicators present for all navigation items.

**DON'T:** _Don’t use the active indicator for more than one navigation item at a time_

The active indicator hugs the label text in the expanded nav rail. To achieve a similar style to the baseline navigation drawer, consider modifying the active indicator to fill the container.

The target area should always span the full width.

[IMAGE] Navigation rail with active indicator that hugs the text and icon.

_The active indicator hugs contents in the expanded nav rail_

[IMAGE] Navigation rail with active indicator that is larger than the content within it.

_Override the indicator to fill the container to more closely resemble the baseline navigation drawer_

##### Icons

Navigation rail items must use icons that symbolize the content of their page. Browse popular icons on Google Fonts.

[IMAGE] Navigation rail with icons that fit the destinations, like a timer icon and label leading to a timer feature.

_Icons should symbolize the content of the page they open_

When a destination is selected, the icon fills and changes color. An active indicator appears behind the icon.

[IMAGE] Icons with and without an active indicator.

_Selected navigation items have an active indicator, a filled icon, and a more prominent color_

##### Label text

The label text should be a short, meaningful description of each navigation destination and another way for users to understand an icon’s meaning.

All navigation items require a one word label text.

[IMAGE DO] Navigation rail with clear text labels.

**DO:** _Write clear and concise labels that describe the destination page_

Avoid wrapping long labels when possible. If necessary, create a line break between words, or hyphenate longer words.

[IMAGE CAUTION] Navigation rail with lengthy text labels.

**CAUTION:** _Break up longer phrases into two text lines if necessary_

Labels should be short enough to not be truncated. Don’t shrink the type scale to fit longer text labels.

[IMAGE DON'T] Navigation rail with truncated text label with ellipses.

**DON'T:** _Don’t truncate or display an ellipsis in place of label text_

[IMAGE DON'T] Navigation rail with small text label.

**DON'T:** _Don’t reduce the type size to fit more characters into a destination label_

##### Badges

Navigation rail icons can include badges to communicate dynamic information about the destination, such as counts or status.

In compact nav rails, the badge is placed in the upper right corner of the icon. In expanded nav rails, the badge should be placed next to the label text.

[IMAGE] Navigation rail with badges on each icon.

_1. Small badge on a rail destination
2. Large badge with a number
3. Large badge with a maximum character count_

##### Divider (optional)

A vertical divider can help separate the rail from app content. The divider should be positioned on the edge of the rail container that’s adjacent to the app’s content area.

[IMAGE] Navigation rail with divider separating it from screen content.

_A divider can make the navigation rail container distinct from other on-screen content_

#### Placement

In adaptive layouts, the navigation rail should be placed outside any panes, always along the leading edge of the window. Don’t place it within body content.

When the navigation rail is hidden, the body content can fill in the remaining space as long as the menu icon is still accessible.

Tabs can be used alongside a navigation rail to create an extra layer of visible navigation.

[VIDEO] Navigation rail collapsed next to screen content, and expanded overlaying screen content with secondary destinations.

_Expanded navigation rails can open from menu buttons on mobile_

#### Adaptive design

For more, see adaptive design.

##### Resizing

When moving from a large screen to a small screen, a navigation rail can transform into a navigation bar, providing the same quick access in a configuration that’s easier to use on smaller displays. Never use the navigation rail and navigation bar simultaneously.

Only use navigation rails for medium breakpoints and larger. Don’t use a navigation bar. If there are more than five destinations, consider using a modal expanded nav rail instead.

**Compact:** Don’t use a standard navigation rail for compact layouts due to space constraints. Use a navigation bar instead.

**Medium:** Use a navigation rail, especially if prioritizing persistent vertical navigation over maximizing vertical content space.

**Expanded to extra-large:** Use a navigation rail, not a navigation bar. Consider available horizontal space and the number of destinations when choosing between standard and modal.

[IMAGE] Navigation bar on a phone screen and navigation rail on a tablet screen.

_On smaller devices, use a navigation bar. On larger displays, use a navigation rail._

##### Presentation

When the navigation rail transitions from collapsed to expanded, the contents of the page should automatically adjust to fit.

The contents of the navigation rail also expand to fill the space. For example, the FAB should transition into an extended FAB.

Extra destinations can be shown in an expanded nav rail.

[VIDEO] Extra destinations in an expanded navigation rail.

_Use a standard expanded rail when there are secondary destinations or actions that have lower priority than the main navigation items_

#### Behavior

##### Scrolling

Destinations in the navigation rail should remain visible and fixed when scrolling vertically.

[VIDEO] Screen content scrolls while rails stay fixed.

_Rail destinations remain fixed while on-screen content scrolls vertically_

If a layout scrolls horizontally, the rail can scroll off-screen or remain fixed. To distinguish that content is scrolling underneath the rail, use a divider or add elevation to the rail.

[VIDEO] A nav rail with a divider between it and the page’s content.

_A divider and color fill change create visual distinction between the rail and horizontally scrolling content_

[VIDEO] A nav rail with slight elevation.

_Elevating the rail to level 1 creates visual distinction between the rail and horizontally scrolling content_

##### Selection

When a destination is tapped, the destination screen uses a top level transition pattern. In addition, the icon becomes filled and the active indicator expands from the center of the icon.

[VIDEO] Selection transition between one navigation item and another.

_Tapping a destination uses a top level transition pattern_

##### Back

On Android, a gesture called predictive back allows people to swipe left or right on the screen to go back or dismiss modal components.

- Previous screen is revealed in a preview to signal the destination

- Predictive back only applies to the **modal expanded** navigation rail.

A list of compatible components is available on the gestures page.

[VIDEO] Modal expanded navigation rail with Android predictive back feature.

_The nav rail pops off the edge of the window during the predictive back gesture_

## Accessibility

#### Use cases

People should be able to do the following using the assistive technology:
- Navigate between navigation destinations
- Select a particular navigation destination from a set
- Get appropriate feedback based on input type

#### Interaction & style

When a navigation item is tapped, the active indicator appears, providing the following feedback to the user that it is selected:
- A ripple passes through the indicator
- The icon switches from outlined to filled
- The icon and text change color

When hovered, the hover state appears, providing a visual cue that the destination is interactive.

[IMAGE] Colorful, purple navigation rail shown collapsed and expanded.

_Touch: Tap_

[IMAGE] Tap indicator on a collapsed nav rail.

_Cursor: Hover, Click_

The target area for expanded navigation rails spans the full width of the container, even though the active indicator visually hugs the content.

[IMAGE] Touch indicator on a nav rail.

_Touch: Tap_

Use a filled icon for the active destination and outlined icons for inactive destinations.

Active and inactive icon colors need sufficient contrast against the container.

[IMAGE DO] Navigation rail with filled element.

**DO:** _Use the default color scheme to ensure proper contrast and emphasis on the active destination_

[IMAGE DON'T] Nav rail with multiple navigation destinations and multi-colored contrast.

**DON'T:** _Don’t use more than two colors for destinations or low-contrast colors in the navigation rail. This will make distinguishing active items difficult._

If an icon doesn’t have a filled style, use the semibold icon weight instead.

[IMAGE] Icon button with semibold weight, without filled options.

_An icon with no filled option should use the semibold weight when active_

##### Text scaling and truncation

When someone sets their device to show a larger text size, the navigation rail items should grow vertically to accommodate larger labels while retaining the default padding. It’s okay for scaled text to wrap in navigation items.

To remain accessible, ensure the full label is always visible on-screen at up to 2x text sizing. Beyond this size, text can truncate.

[IMAGE] Nav rail with text scaled to 1.5x size. All labels are on one line.

_Text scaled to 1.5 size_

[IMAGE] Nav rail with text scaled to 2x size. Some labels wrap to two lines.

_Text scaled to 2x size_

##### Initial focus

Initial focus lands directly on the first interactive item, whether it’s the menu, the FAB, or the first navigation item.

From the FAB or menu, **Tab** brings the person to the navigation items. **Tab** or **Arrows** then navigate between items.

[IMAGE] Arrows help people move between pages.

_Use arrows to move between navigation items_

[IMAGE] Space/enter help people choose a navigation destination.

_Use space/enter to activate the focused navigation item_

##### Visual indicators

Icons give the dominant cue of the navigation state. Use a filled icon for the selected destination to contrast with outlined icons for the non-selected destinations.

[IMAGE DO] Nav bar with an active, filled icon button.

**DO:** _Use a filled icon variant on the selected navigation item to differentiate from inactive navigation items_

[IMAGE DON'T] Selected navigation item without filled icon style.

**DON'T:** _Avoid using the same unfilled icon style for both selected and unselected items because it lacks important visual feedback cue_

#### Keyboard navigation

| Keys | Actions
| Tab / Arrows | Navigate between interactive elements
| Space / Enter
| Selects an interactive element

#### Labeling elements

The accessibility label for a navigation item is typically the same as the adjacent text label.

When the visible UI text is ambiguous, accessibility labels need to be more descriptive. For example, a navigation item visibly labeled **Recent** would benefit from additional information in its accessibility label to clarify the destination's intent.

Note: On Android Views (MDC-Android), a more descriptive accessibility label is not available and the role is not announced.

[IMAGE] “Maps” is both the icon label text and the accessibility label.

_While the visible label text reads **Recent**, the accessibility label for this switch clarifies its function: **Recent images**_
