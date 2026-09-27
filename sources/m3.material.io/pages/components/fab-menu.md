# FAB menu
Source: https://m3.material.io/components/fab-menu

## Overview

- Opens from a FAB to show 2–6 related actions floating on screen
- One FAB menu size for all sizes of FABs
- Not used with extended FABs
- Available in primary, secondary, and tertiary color sets

[IMAGE] 3 FAB menus in different color schemes.

_The FAB menu comes in three color sets: primary, secondary, tertiary_

#### Availability & resources

#### M3 Expressive update

**May 2025**

The FAB menu adds more options to the FAB. It should replace the speed dial and any usage of stacked small FABs. More on M3 Expressive

New component added to catalog:
- One menu size that pairs with any FAB
- Replaces any usage of stacked small FABs

Color:
- Contrasting close button and item colors
- Supports dynamic color
- Compatible with any FAB color style

[IMAGE] 4 screens. The FAB menu is on the first, and 3 FABs of different sizes are on the others.

_The FAB menu uses contrasting color and large items to focus attention. It can open from any size FAB._

#### Differences from M2

[IMAGE] M2 speed dial.

_M2: The speed dial used small round FABs_

[IMAGE] GM3 FAB menu.

_M3: The FAB menu uses dynamic color and a larger item size_

## Specs

#### Variants

[IMAGE] The FAB menu in its single variant.

_There’s one variant of FAB menu_

|

Variant |

M3 |

M3 Expressive
|

FAB menu |

-- |

Available

#### Configurations

[IMAGE] 3 color configurations of FAB menus.

_Three color sets:
- Primary
- Secondary
- Tertiary_

|

Category |

Configuration |

M3 |

M3 Expressive
|

Color |

Primary set, secondary set, tertiary set |

-- |

Available

#### Tokens & specs

Use the table's menu to switch token sets. The FAB menu has a common token set and six color sets, three for each element (close button and menu item). Learn about design tokens

#### Anatomy

[IMAGE] 2 elements of a FAB menu.

_- Close button
- Menu item_

[IMAGE] 5 FAB menus showing the range of 2–6 items.

_The FAB menu can have up to six items_

#### Color

Color values are implemented through design tokens. For designers, this means working with color values that correspond with tokens. In implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] 12 colors of the FAB menu.

_- On primary container
- Primary container
- On primary
- Primary
- On secondary container
- Secondary container
- On secondary
- Secondary
- On tertiary container
- Tertiary container
- On tertiary
- Tertiary_

#### States

States are visual representations used to communicate the status of a component or interactive element. Learn more about interaction states

##### Close button

[IMAGE] 4 states of the FAB menu close button.

_Close button states in light and dark themes:
- Enabled
- Hovered
- Focused
- Pressed_

##### Menu item

[IMAGE] 4 states of the FAB menu items.

_Menu item states in light and dark themes:
- Enabled
- Hovered
- Focused
- Pressed_

#### Measurements

FAB menu items share the same measurements as the medium button specs.

The close button should always be 56dp.

[IMAGE] FAB menu size measurements.

_FAB menu size measurements_

The FAB menu animates from the top trailing edge of the FAB to ensure a smooth animation.

[IMAGE] FAB on a mobile screen with 16dp margins annotated.

_The FAB should always have 16dp margins_

[IMAGE] FAB menu opened from a FAB has matching margins of 16dp.

_The close button and FAB share the top trailing corner as an anchor and appear in the same place_

Larger FABs will place the FAB menu slightly higher, with larger margins underneath.

[IMAGE] Medium FAB on a mobile screen with 16dp margins annotated.

_The medium FAB placement has 16dp margins_

[IMAGE] FAB menu opened from the medium FAB has a 40dp margin from bottom of screen.

_The close button is placed higher to align with the top of the medium FAB_

[IMAGE] Large FAB on a mobile screen with 16dp margins annotated.

_The large FAB placement has 16dp margins_

[IMAGE] FAB menu opened from the large FAB has a 56dp margin from bottom of screen.

_The close button is placed higher to align with the top of the large FAB_

On web, the FAB menu opens from the FAB, and inherits its states and specs from the baseline menu component.

The gap between the FAB and menu can vary, but 4dp is recommended.

[IMAGE] FAB menu on web states and specifications.

_Spacing and interaction on FAB menu for web:
- Enabled
- Hovered
- Selected_

## Guidelines

[IMAGE] On a page of music albums, a FAB menu shows options to make a new playlist, collection, or station.

_Use the FAB menu to show multiple related actions in a prominent, expressive style_

#### Usage

A FAB menu opens from a FAB to show multiple related actions. It should always appear in the same place as the FAB that opened it.

This makes actions immediately accessible, and keeps the UI clean by concealing actions when they’re not needed.

Don’t open a FAB menu from an extended FAB or any other component.

[IMAGE] 1 mobile screen with a FAB, 1 with a FAB menu. Both are right aligned.

_The FAB menu should always open from a FAB_

The FAB menu should be aligned to the trailing edge of the window.

In right-to-left (RTL) languages, this means the FAB and FAB menu should be aligned to the left edge, and the layout of elements should be mirrored.

[IMAGE] 1 mobile screen with a FAB, 1 with a FAB menu. Both are left aligned and mirrored for a right-to-left language.

_In RTL languages, the FAB menu should be left-aligned with the icon and text placement mirrored_

FAB menus can contain 2–6 items. These should be closely related under a single action, like **Share**.

Avoid grouping unrelated actions in the same FAB menu.

[IMAGE DO] A FAB menu with 5 options on a photo gallery UI.

**DO:** _FAB menus can have 2-6 items_

[IMAGE DON'T] A FAB menu with 1 option on a photo gallery UI.

**DON'T:** _Don’t use a FAB menu with one item_

When a FAB is paired with other components, like the floating toolbar or navigation rail, don’t use the FAB menu. This prevents cognitive overload and interface clutter.

[IMAGE DO] A toolbar with a FAB directly next to it.

**DO:** _FABs can be placed next to toolbars and other components_

[IMAGE DON'T] A toolbar with a FAB menu next to it.

**DON'T:** _Don't use a FAB menu with a toolbar or navigation rail_

##### Color sets

FAB menus have three color sets: primary, secondary, and tertiary. Use the color set that best matches the FAB color style.

Use the primary FAB menu color set with the **primary** or **primary container** FAB color styles.

[IMAGE] A FAB menu using the primary color set.

_A primary FAB is paired with a primary FAB menu_

Use the secondary FAB menu color set with the **secondary** or **secondary container** FAB color styles.

[IMAGE] A FAB menu using the secondary color set.

_A secondary FAB is paired with a secondary FAB menu_

Use the tertiary FAB menu color set with the **tertiary** or **tertiary container** FAB color styles.

[IMAGE] A FAB menu using the tertiary color set.

_A tertiary FAB is paired with a tertiary FAB menu_

#### Anatomy

[IMAGE] 2 elements of a FAB menu.

_- Close button
- List item_

FAB menu items should always have label text. The icons shouldn’t be removed since they make each item easy to identify.

[IMAGE CAUTION] A FAB menu with 3 options for selecting Food, People, or Nature. There are no icons next to the text.

**CAUTION:** _Only remove the icon if necessary. The icon provides a differentiation between items._

[IMAGE DON'T] A FAB menu with 3 options for selecting Food, People, or Nature. The options are only icons, no text.

**DON'T:** _Don’t remove the label_

The list item should always hug its contents and look consistent. Avoid truncating text or setting fixed widths. All FAB menu elements should be rounded.

[IMAGE DO] A FAB menu used out of the box with no configurations.

**DO:** _Keep the padding between the container and icon, icon and text, and text and container consistent_

[IMAGE DON'T] FAB menu items are equal width despite having different lengths of text.

**DON'T:** _Don’t expand container sizes_

[IMAGE DON'T] FAB menu items are square instead of round.

**DON'T:** _Don’t change FAB menu shapes_

#### Adaptive layout

The FAB menu can open from any sized FAB. Use with a FAB size suitable for the window size class. For example, larger FABs are recommended for larger windows.

[VIDEO] The same FAB menu used in medium and compact window sizes.

_The FAB menu works in any window size. Pair it with the FAB suitable for that window size._

The FAB menu should remain anchored to the same corner or edge regardless of window size.

In large and extra large windows, the FAB and FAB menu margins should increase from 16dp to 24dp.

[IMAGE] A FAB menu with 24dp margins from the edge of the window.

_On desktop, use larger FABs and margins_

On web, the FAB menu uses a menu component for an experience that's consistent with other desktop apps.

[IMAGE] A FAB menu using menu component on web and traditional FAB menu on compact screen.

_The same FAB menu options on both large window (left) and an Android compact window (right)_

#### Behavior

##### Appearing

The FAB should transform into the close button of the FAB menu. The menu items should appear using the enter and exit transition.

Originate the transition from one of the FAB's trailing corners, preferably the top-aligned corner.

[VIDEO] A FAB transforms into a FAB menu while anchoring the animation to the top right corner of the FAB.

_Animate FAB menus from the top-aligned corner of FABs_

To ensure accessibility for keyboard users on the web, avoid positioning the FAB menu to completely obscure the focus indicator of an actionable element.

Partially covering the desired element is fine, as long as the focus indicator is visible.

[IMAGE DO] FAB menu doesn’t obscure actionable element and its focus indicator.

**DO:** _Ensure the actionable element and its focus indicator are visible behind the FAB menu_

[IMAGE DON'T] FAB menu obscures both an actionable element and its focus indicator.

**DON'T:** _Don’t block an actionable element and its focus indicator completely with the FAB menu_

##### Scrolling

When window height is limited, like when viewing phones in horizontal orientation, FAB menu items can scroll.

The items should scroll behind the close button.

[VIDEO] A FAB menu with 6 items scrolls off screen on a horizontal-oriented device. Scrolled items move behind the close button.

_FAB menus can scroll if the window height is too short to contain all the options_

##### Expanding

Any FAB menu item can expand and adapt to any shape using a container transform transition pattern. This includes a surface that is part of the app structure, or a surface that spans the entire screen.

[VIDEO] A FAB menu item expands and transforms into a full screen dialog.

_FAB menu items can transition into any kind of shape when selected_

## Accessibility

#### Use cases

People should be able to do the following using assistive technology:
- Navigate and interact with the FAB menu
- Ensure focus is correct when navigating through the menu

#### Interaction & style

FAB menu elements meet the minimum target size of 48dp.

[IMAGE] FAB menu measurement annotations. All elements are larger than the minimum target size.

_FAB menus have 48x48dp minimum width and sufficient spacing by default_

When the FAB menu can scroll, make sure the items scroll behind the close button.

The close button should always be easy to access and unobstructed.

[IMAGE DO] FAB menu items are scrolling behind the close button.

**DO:** _Allow the menu items to scroll behind the close button_

[IMAGE DON'T] FAB menu items are scrolling in front of the close button.

**DON'T:** _Don’t obstruct the close button in short screens like horizontal orientation_

#### Initial focus

When the FAB is selected, the FAB menu opens, and initial focus remains on the close button, which takes the place of the original FAB.

Then the focus moves from the top menu item to the bottom.

[IMAGE] 4 FAB menus with the focus order labelled. Focus moves from the close button at the bottom to the topmost menu item next.

_Focus lands on the close button. People can then navigate through all the items.
- Close button
- First menu item
- Second menu item
- Third menu item_

#### Keyboard navigation

|

**Keys** |

**Actions**
|

**Tab** |

Navigate to the next interactive element
|

**Space** or **Enter** |

Activate the focused button or item

#### Labeling elements

##### Android

On Android, a FAB menu’s close button should include a state to tell screen readers what action will occur when it's toggled. The close button should be labeled:
- Label: Toggle menu
- Role: Button
- State: Expanded or collapsed

[IMAGE] Accessibility labels for the close button on an Android device.

_On Android, the **close button** accessibility labels should include a toggle menu label, button role, and an expanded or collapsed state_

FAB menu items should be labeled:
- Label: Match the item’s UI text, such as **Reply all **
- Role: Button

[IMAGE] Accessibility labels for a FAB menu item on an Android device.

_Label FAB menu items to match their UI text, like **Reply all**, and use the button role_

##### Web

On web, a FAB menu is a combination of a FAB and a menu component. The FAB opens the menu. Follow the accessibility guidelines for FABs and menus.

The FAB's accessibility label should describe the menu that the FAB will open.
