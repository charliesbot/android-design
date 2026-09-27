# FABs
Source: https://m3.material.io/components/floating-action-button

## Overview

- Use a FAB for the most common or important action on a screen
- Make sure the icon in a FAB is clear and understandable
- FABs persist on the screen when content is scrolling
- Three variants: FAB, medium FAB, large FAB

[IMAGE] The 3 sizes of floating action buttons.

_- FAB
- Medium FAB
- Large FAB_

#### Availability & resources

#### M3 Expressive update

**May 2025**

The FAB has new sizes to match the extended FAB and more color options. The small FAB is no longer recommended. More on M3 Expressive

Variants and naming:
- Added **medium** FAB size
- **Small** FAB size is no longer recommended
- FAB and large FAB sizes are unchanged
- FAB variants are based on size, not color

Color:
- Added tone color styles:
  - Primary
  - Secondary
  - Tertiary

- Renamed existing tonal color styles to match their token names:
  - **Primary** to **Primary container**
  - **Secondary** to **Secondary container**
  - **Tertiary** to **Tertiary container**
  - The values haven't changed

- Surface color FABs are no longer recommended

[IMAGE] 4 FABs showing the colors available after the expressive update.

_FABs have updated colors and sizes_

#### Differences from M2

[IMAGE] M2 circular FAB with a plus icon.

_M2: FABs are circles and always have a drop shadow_

[IMAGE] M3 rounded corner square FAB with an artist’s palette icon.

_M3: FABs have a boxier shape, can use dynamic color, and include a new large FAB variation_

## Specs

#### Variants

[IMAGE] An icon on the container of a FAB, medium FAB, and large FAB.

_- FAB
- Medium FAB
- Large FAB_

##### Baseline variants

The small FAB is still available, but no longer recommended. Jump to baseline specs

[IMAGE] An icon on the container of a small FAB.

_1. Small FAB_

|

Variant |

M3 |

M3 Expressive
|

FAB |

Available |

Available
|

Medium FAB |

-- |

Available
|

Large FAB |

Available |

Available
|

Small FAB |

Available |

Not recommended.

Use a larger size.

#### Configurations

In the expressive update, the **primary**, **secondary**, and **tertiary** set colors were renamed to **primary container**, **secondary container**, and **tertiary container **to match the actual color roles used. New primary, secondary, and tertiary color styles were created to match the corresponding color roles. View details in the color styles section

|

Category |

Configuration |

M3 |

M3 Expressive
|

Color |

Primary container, secondary container, tertiary container |

Available as primary, secondary, tertiary |

Available
|

Primary. secondary, tertiary |

-- |

Available

#### Tokens & specs

Use the table's menu to select a token set. FAB tokens are organized by size and color. Learn more about design tokens

#### Anatomy

[IMAGE] 2 elements of the FAB.

_1. Container

2. Icon_

#### Color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. In implementation, a color value will be a token that references a value. Learn more about design tokens

##### Color styles

FABs can use several combinations of **color** and **on-color** styles, such as **primary** and **on-primary**. The following color mappings provide the same legibility and functionality, so the color mapping you use depends on style alone.

[IMAGE] 6 FAB color styles in light and dark themes. Each style has 2 color roles, 1 for the container and icon.

_- Primary container & On primary container (default)
- Secondary container & On secondary container
- Tertiary container & On tertiary container
- Primary & On primary
- Secondary & On secondary
- Tertiary & On tertiary_

##### Baseline color styles

Surface FAB color styles are still available, but no longer recommended.

[IMAGE] Baseline FAB style in all 3 sizes.

_- Surface FABs_

#### States

States are visual representations used to communicate the status of a component or interactive element.

When using a non-default color mapping for FABs, make sure the state layer color is the same as the icon color. For example, the state layer color for the **primary** color style should be md.sys.color.primary.

[IMAGE] 4 states of a FAB shown in light and dark themes.

_- Enabled
- Hovered (8% state layer) - elevation 4
- Focused (10% state layer)
- Pressed (10% state layer)_

#### Measurements

##### FAB

[IMAGE] FAB size measurements.

_FAB size measurements_

[IMAGE] FAB padding measurements.

_FAB padding measurements_

##### Medium FAB

[IMAGE] Medium FAB size measurements.

_Medium FAB size measurements_

[IMAGE] Medium FAB padding measurements.

_Medium FAB padding measurements_

##### Large FAB

[IMAGE] Large FAB size measurements.

_Large FAB size measurements_

[IMAGE] Large FAB padding measurements.

_Large FAB padding measurements_

#### Baseline tokens & specs

Use the table's menu to select a token set. This only includes tokens for small and surface FABs, which are both no longer recommended. It doesn't include other colors, or large or regular FABs, since those are still currently used.

## Guidelines

[IMAGE] 3 screens with various FAB sizes.

_FABs have multiple sizes that scale with the breakpoint_

#### Usage

Use a FAB for the most important action on a screen; it appears in front of all other content.

The FAB can be aligned left, center, or right. It can be positioned above the navigation bar, or nested within it.

[IMAGE] A Compose FAB is positioned above a nav bar on a mobile email inbox.

_FABs can use dynamic color_

There are three FAB sizes:
- FAB
- Medium FAB (most recommended)
- Large FAB

Choose the FAB size based on the visual hierarchy of your layout.

Note: The small FAB is no longer recommended.

[IMAGE] 3 FAB sizes.

_- FAB
- Medium FAB
- Large FAB_

The FAB is the smallest size, and is best used in compact windows where other actions may be present on screen.

The medium FAB is recommended for most situations, and works best in compact and medium windows. Use it for important actions without taking up too much space.

A large FAB is useful in any window size when the layout calls for a clear and prominent primary action, but is best suited for expanded and larger breakpoints, where its size helps draw attention.

[IMAGE] A medium FAB over an email app UI.

_Use a medium FAB in most breakpoints_

[IMAGE] A large FAB over an email app UI.

_Use a large FAB when the primary action needs to be prominent_

[IMAGE DO] A photo feed with no FAB.

**DO:** _FABs are not needed on every screen, such as when images represent primary actions_

[IMAGE DON'T] A screen with 3 FABs makes it hard to tell what the primary action should be.

**DON'T:** _Don't display multiple FABs on a single screen_

A FAB can transform into an extended FAB on larger screens, or it can transition into a FAB menu when selected. Use a FAB menu when there are many kinds of actions relevant to the FAB.

More on FAB menus

[IMAGE] A extended FAB saying “Share” with a less popular share icon.

_Use the extended FAB when label text is necessary_

[IMAGE] A FAB menu showing 3 actions related to sharing.

_Use the FAB menu when there are many kinds of actions relevant to the FAB_

#### Actions

A FAB can trigger an action on the current screen, or it can perform an action that creates a new screen.

A FAB promotes an important, constructive action such as:
- Create
- Favorite
- Share
- Start a process

[IMAGE DO] FABS for 12 common actions including, create, edit, and navigate.

**DO:** _Use FABs for primary, positive actions_

Avoid using a FAB for minor or destructive actions, such as:
- Archive or trash
- Alerts or errors
- Limited tasks like cutting text
- Controls better suited to a toolbar, like to adjust volume or font color

[IMAGE DON'T] FABs for 18 minor or destructive actions, such as cut, trash, and volume.

**DON'T:** _Don’t use FABs for minor, overflow, unclear, or destructive actions_

#### Anatomy

[IMAGE] 2 elements of a FAB.

_- Container
- Icon_

##### Container

The FAB is typically displayed in a square container. The container shouldn’t be covered by other elements, such as badges.

The container must have sufficient color contrast with the surface it’s placed on.

[IMAGE] A bright colored FAB has high contrast with the background.

_A FAB container color needs to stand out from its background_

##### Icon

An icon in a FAB should be clear and understandable. When hovering over a FAB on web products, FABs should display a tooltip with an accompanying icon text label. Use a filled icon instead of an outlined icon.

A FAB shouldn't contain notifications or actions found elsewhere on a screen.

[IMAGE DO] 4 FABs each with a simple icon.

**DO:** _Use clear and simple icons such as add, message, or edit_

[IMAGE DON'T] 4 FABs each with an ambiguous icon.

**DON'T:** _Don’t use confusing or open-ended icons to symbolize less common actions_

#### Adaptive design

In compact and medium breakpoints, the best place for the FAB is typically the lower right corner of a screen, since it’s easy to reach and is less likely to cover important content.

In expanded breakpoints, consider placing the FAB in the upper left corner, like in the navigation rail. This positions it as one of the first interactive elements people see when they land on the page.

Adjust the size of the FAB based on the context. Use a medium FAB for mobile layouts, and large FAB for tablets and large screens.

[IMAGE] Large screen layout showing FAB in upper left region of the screen, below navigation rail icon.

_For large screens, place the FAB in the upper left corner_

[IMAGE DO] A screen layout with several interactive elements. A single FAB is in the navigation rail.

**DO:** _A FAB can be used within a navigation component, such as a navigation rail_

[IMAGE DON'T] A busy screen layout with 8 cards, each with their own FAB.

**DON'T:** _Individual components, such as cards, shouldn’t have their own FAB_

#### Behaviors

##### Appearing

When a FAB animates on screen, it expands outward from a central point. The icon within it can be animated as well.

While FABs should be relevant to screen content, they aren't attached to the surface on which content appears. FABs move separately from other UI elements because of their relative importance.

**Screen transitions
**FABs can morph to launch related actions. When a screen changes its layout, the FAB should disappear and reappear during the transition.

**Reappearance
**The FAB should only reappear if it's relevant to the new screen. It should reappear in the same position, if possible.

[VIDEO] FAB appearing from the bottom of the screen, moving to a position above the  navigation bar.

_FAB animating on screen_

##### Expanding

The FAB can expand and adapt to any shape using a container transform transition pattern. This includes a surface that's part of the app structure, or a surface that spans the entire screen.

The FAB can also transition into a FAB menu.

More on FAB menus

[VIDEO] Compose FAB expands to container size, opening a draft email screen.

_FABs can expand and adapt to any shape_

##### Scrolling

FABs remain in place on scroll.

Extended FABs can collapse into a FAB on scroll and expand on reaching the bottom of the view.

[VIDEO] Extended FAB collapsed into a FAB when scrolling on the page.

_FABs stay in place above a scrolling background_

##### Moving across tabs

When tabs are present, the FAB should briefly disappear, then reappear when the new content moves into place. This shows that the FAB is not connected to any particular tab.

[VIDEO DO] FAB blinks away and reappears with new screen content.

**DO:** _The FAB should disappear and reappear when switching pages_

Don't animate the FAB with body content.

[VIDEO DON'T] FAB moves horizontally when scrolling between tabs.

**DON'T:** _Don’t keep the FAB on screen when switching pages_

## Accessibility

#### Use cases

People should be able to do the following using assistive technology:
- Navigate to and activate the FAB
- Perform an action with the FAB
- Expand and minimize an extended FAB

#### Interaction & style

Don't disable the FAB. If the action represented in the FAB is unavailable, the FAB shouldn't appear.

Ensure the icon has a minimum 3:1 contrast ratio with the container.

[IMAGE DO] FAB with highly contrasting bright container and dark icon.

**DO:** _FAB icons are above the 3:1 contrast ratio_

[IMAGE DON'T] FAB with low-contrasting dark container and dark icon.

**DON'T:** _Avoid using colors with a contrast below 3:1_

#### Focus

Ensure the FAB is prioritized in the overall focus order to create an efficient experience for people who navigate UIs with assistive tech.

On mobile, the focus order may start with the app bar, move to the navigation bar, and then skip past any other content on the page to land on the FAB.

Consider displaying a tooltip when the FAB is focused. This is supported on web.

[IMAGE] A focused FAB with a tooltip saying “Compose” appearing below it.

_Tooltips surface the FAB’s label when focused_

#### Layout & position

To make it easier for users of screen readers to reach a primary action such as a FAB on expanded breakpoints, consider placing the FAB in the upper left region.

However, it’s critical to test placement options with users to see if the upper left region is the best position in all browser windows. For compact and medium breakpoints, the best place for the FAB is the lower right corner of a screen.

[IMAGE] FAB in the lower right region of a small screen.

_In compact windows, place the FAB in the bottom trailing edge_

[IMAGE] FAB in the upper left region of a large screen.

_In expanded windows, place the FAB in the navigation rail_

To ensure accessibility for keyboard users on the web, avoid positioning the FAB in a way that completely obscures the focus indicator of an actionable element.

It’s okay to partially cover the desired element, as long as the focus indicators are still visible.

[IMAGE DO] FAB in the lower right region doesn’t obscure the focus indicator of an actionable icon.

**DO:** _The FAB can partially cover an actionable element, as long as the focus indicator is still clearly visible_

[IMAGE DON'T] FAB in the lower right region obscures an actionable icon and its focus indicator.

**DON'T:** _Don’t completely obscure an actionable element and its focus indicator_

#### Keyboard navigation

|

**Keys** |

**Actions**
|

**Tab** |

Focus lands on the FAB
|

**Space** or **Enter** |

Perform the default action on an item

#### Labeling elements

The accessibility label should describe the action that the button is performing, such as **Compose a new message**.

[IMAGE] Accessibility label and accessibility role of a FAB.

_The accessibility label of the FAB with a pencil icon describes the action of composing a new message_
