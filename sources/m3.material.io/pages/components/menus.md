# Menus
Source: https://m3.material.io/components/menus

## Overview

- Use a **menu** to show a temporary set of actions. To show actions on screen at all times, use a **toolbar** instead
- Menus can open from many components, including icon buttons, split buttons, and text fields
- **Context menus** provide actions for a specific element, like an image or highlighted text, and usually open with a secondary click

[IMAGE] 1 vertical menu with vibrant colors opens from a split button, and 1 vertical menu with a submenu.

_Vertical menus can include vibrant colors, gaps, dividers, and submenus to organize a list of choices_

#### Availability & resources

#### M3 Expressive update

**November 2025**

**Vertical menus** were introduced with new shapes, color styles, selection states, and refined submenu motion. Gaps can be used for a more flexible layout on Android. More on M3 Expressive

Variants:
- Added **vertical menus**, recommended for new designs
- Baseline **menu** is still available

Color styles:
- Standard
- Vibrant

[IMAGE] A vertical menu using shape and vibrant color to show a selected state.

_Vibrant colors help selected menu items stand out_

#### Differences from M2

- **Color**: New color mappings and compatibility with dynamic color
- **Variants**: Dropdown menu and exposed dropdown menu are now both referred to as menu, since they differ only in the element which opens the menu surface

[IMAGE] Menu with gray color.

_M2: Former menu colors don’t contrast with the background_

[IMAGE] Menu with purple background and outline.

_M3: Menus feature new color mappings and dynamic color_

## Specs

#### Variants

##### Vertical menus

Use vertical menus for a more expressive look and feel, including rounded corners, standard and vibrant color styles, more selection states, and submenu motion.

[IMAGE] 2 vertical menus use shape and color to indicate selected state.

_- Vertical menu with gap
- Vertical menu with divider_

##### Baseline variant

In M3 Expressive, baseline menu is still available to use, but doesn’t have the latest shapes, color styles, selection states, and motion. See baseline menu specs

[IMAGE] A baseline menu variant with square corners and standard colors.

_A baseline **menu** has square corners, as compared to a **vertical menu’s** round corners and expressive styling_

|

**Variant** |

**M3** |

**M3 Expressive**
|

Vertical menus |

-- |

Available
|

Menu (baseline) |

Available |

Available

#### Configurations

##### Vertical menus layout

[IMAGE] 2 menus: 1 standard, and 1 with a gap, creating groups.

_- Standard
- Grouped_

| **Category
** | **Configuration
** | **M3** |
**M3 Expressiv****e**
| Color | Standard | Available | Available
| Vibrant | -- | Available
| Layout | Standard | Available | Available
| Grouped | -- | Available

#### Tokens & specs

Browse the component elements, attributes, tokens, and their values. Learn about design tokens

#### Anatomy

##### Vertical menus

[IMAGE] A diagram of a vertical menu.

_- Menu item
- Leading icon (optional)
- Menu item text
- Trailing icon (optional)
- Badge (optional)
- Trailing text (optional)
- Container
- Supporting text (optional)
- Label text (optional)
- Gap (optional)
- Divider (optional)_

#### Color

Color values are implemented through design tokens. For designers, this means working with color values that correspond with tokens. In implementation, a color value will be a token that references a value. Learn more about design tokens

Menus have two color mappings:
- Standard: Surface-based
- Vibrant: Tertiary-based

These mappings provide options for lower or higher visual emphasis. Vibrant menus are more prominent so should be used sparingly.

[IMAGE] 2 vertical menus: 1 with lower visual emphasis, and 1 vibrant menu with bold shades.

_- Standard color scheme
- Vibrant color scheme_

##### Standard colors

[IMAGE] 2 vertical menus with standard color roles mapped to 11 elements.

_Vertical menus color roles used for light and dark themes:
- On surface variant
- On surface
- On surface (state layer)
- Surface container low
- On surface variant
- On surface variant
- Tertiary container (selected)
- On tertiary container (selected)
- On surface variant
- On surface variant
- On tertiary container (selected)_

##### Vibrant colors

[IMAGE] 2 vertical menus with vibrant color roles mapped to 11 elements.

_Vertical menus color roles used for light and dark themes:
- On tertiary container
- On tertiary container
- On tertiary container (state layer)
- Tertiary container
- On tertiary container
- On tertiary container
- Tertiary (selected)
- On tertiary (selected)
- On tertiary container
- On tertiary container
- On tertiary (selected)_

#### States

States are visual representations used to communicate the status of a component or an interactive element. More on interaction states

Shape morphing in vertical menus creates an expressive active state. As focus moves between submenus, the corner shape changes to highlight the active menu. More on menu focus

[IMAGE] 6 vertical menu states in light and dark themes.

_- Enabled
- Disabled
- Hovered
- Focused
- Pressed
- Active (main menu reveals submenu)_

#### Measurements

[IMAGE] Vertical menu marked with spacing and padding measurements.

_Vertical menu padding and size measurements_

#### Menu (baseline)

The baseline menu variant is available and continues to work in existing products. However, M3 expressive vertical menus are recommended for new designs.

##### Baseline tokens & specs

Browse the component elements, attributes, tokens, and their values. Learn about design tokens

##### Anatomy

[IMAGE] Diagram of 6 elements of a baseline menu.

_- List item
- List item leading icon
- List item trailing icon
- Container
- List item trailing text
- Divider_

##### Color

[IMAGE] 9 color roles of a baseline menu in light and dark themes.

_Baseline menu color roles used for light and dark themes:
- On surface variant
- On surface
- On surface - opacity: 0.08
- Surface container
- On surface variant
- On surface variant
- On surface variant
- Surface container highest
- Outline variant_

##### States

###### Default menu items

[IMAGE] Diagram numbering the 5 default states of a baseline menu.

_- Enabled
- Disabled
- Hovered
- Focused
- Pressed_

###### Selected menu items

[IMAGE] 5 states of a selected baseline menu item.

_- Enabled
- Disabled
- Hovered
- Focused
- Pressed_

State specs are in the token module above

##### Measurements

[IMAGE] Diagram of a baseline menu’s padding, text alignment, height, and width.

_Baseline menu padding and size measurements_

| Attribute
| Value

| Container width
| 112dp min, 280dp max
| Corner radius
| 4dp
| Vertical label text alignment
| Center-aligned
| Horizontal label text alignment
| Start-aligned
| Left/right padding
| 12dp
| Left/right padding with-icon
| 12dp
| List item height
| 48dp
| Padding between elements within a list item
| 12dp
| Divider top/bottom padding
| 8dp
| Divider height
| 1dp
| Divider width
| Dynamic
| Leading/trailing icon size
| 24dp

##### Configurations

A baseline menu appears when a person interacts with a button, action, or other control.

A few examples:
- Button
- Text field
- Icon button
- Selected text

[IMAGE] Examples of 4 baseline menu inputs.

## Guidelines

[VIDEO] 2 vertical menus, 1 with vibrant colors, 1 with standard colors and grouped items.

_A menu in the **vibrant** color style is more expressive, and one with **standard** colors is more utilitarian_

#### Usage

Use a menu to show a temporary set of actions. To show actions on screen at all times, use a toolbar instead.

A menu takes up less space than a set of radio buttons or chips.

###### Color options

Menus have two color mappings:
- Standard: Surface-based, lower visual emphasis
- Vibrant: Tertiary-based, higher visual emphasis

Vibrant menus are more prominent, and should be used sparingly.

[IMAGE] Menu shows item “Line spacing” opening a submenu. In the second menu, “Custom 1.2” is selected with vibrant color.

_On web, menus can open submenus_

##### Opening menus

Menus temporarily appear in front of all other permanent UI elements.

A menu should open when a person:
- Selects an element, such as an icon, button, or text field
- Performs a specific action to trigger the menu, like right-click or press-and-hold

Use menus in situations that need extra actions, like:
- Overflow menus
- Text field dropdown menus
- Select menus
- Context menus

[IMAGE] A grouped menu with Undo, Redo, Cut, Copy, and Paste options appear over highlighted text in an ebook.

_Menus appear in front of all other UI elements_

##### Menu groups

Vertical menu items can be grouped by adding a divider or small gap. Use groups to bundle similar actions together.

Gaps and dividers guidelines

[IMAGE] 2 vertical menus: a standard menu with no gap and a grouped menu with 1 gap.

_Menu items can be grouped to be more scannable:
- Standard vertical menu
- Grouped vertical menu_

##### Context menus

Context menus provide a list of additional actions a person can take on an item. A secondary click, like a right-click on a mouse or a two-finger tap on a trackpad, opens a context menu.

[IMAGE] A context menu pops up from a newspaper link. The menu items are: Open in new window, Save link as, Copy address, and Inspect.

_A context menu appears when right clicking with a mouse or trackpad. It can reveal key actions related to the associated content._

#### Anatomy

[IMAGE] Diagram outlining 11 elements of a menu’s anatomy.

_- Menu item
- Leading icon (optional)
- Menu item text
- Trailing icon (optional)
- Badge (optional)
- Trailing text (optional)
- Container
- Supporting text (optional)
- Label text (optional)
- Gap (optional)
- Divider (optional)_

##### Menu items

Menu items can include label text, leading icons, trailing icons, and keyboard commands.

When a menu item can only be used under specific conditions, it should appear disabled rather than be removed.

[IMAGE] Menu shows 1 item that’s  disabled, “Redo”. The text color of the disabled item is lighter than the active items.

_The **Redo** action is disabled when that action isn’t available_

##### Gaps & dividers (optional)

Gaps and dividers can be used to separate and group menu items.

**Gaps **

Use a gap to visually divide menu items into distinct groups. Gaps are more expressive than dividers and make the relationship between items clear.
- Avoid changing the size of the gap
- Limit the number of gaps in a menu to one or two
- Don’t use gaps in scrollable menus

[IMAGE] 2 vertical menus with 5 items. A gap separates items into a group of 3 and group of 2.

_Gaps separate menu items using expressive shapes_

star

Note:
Gaps are not currently available on web

**Dividers**

Dividers create a more subtle separation between items. Use a divider for:
- Scrollable menus
- Text fields with a dropdown menu, where a grouped treatment isn’t appropriate

On web, use a divider to separate menu items.

[IMAGE] A menu on a web interface with items separated by a divider line.

_Dividers separate menu items in baseline menus and on web_

#### Flexibility & slots

Menus have custom slots that support more flexible item layouts.

When creating a complicated menu, think of the menu item as a container with a swappable slot.

Slots work best with simple content such as:
- Images
- Progress indicators
- Color swatches

[IMAGE] A menu showing an undefined slot that could be used for a different element, such as an image.

_Slots can appear anywhere in a menu_

**Slot accessibility**

Use caution when adding slots to menus:
- Make sure the menu remains accessible
- Elements must follow the rules and interaction patterns of the menu component
- Keep the same menu item padding
- Targets should be 48x48dp or larger

Don't add buttons, switches, or other direct actions into the menu item. Nested elements should only perform one action. Adding multiple actions can break keyboard navigation and screen reader functionality.

More on required accessibility guidelines

[IMAGE CAUTION] 1 diagram and 1 menu showing icons in each item’s leading slot.

**CAUTION:** _Reserve the use of slots for use cases that maintain the menu’s accessibility and functionality_

#### Placement

A menu is positioned relative to the window edge. It typically appears below, next to, or in front of the element that generates it.

If a menu is in a position to be cut off, it should automatically reposition to appear to the left, right, or above the element that generates it.

[IMAGE] 6 abstract shapes showing how a menu can extend from the edge of the screen.

_Menus can appear around or in front of the element that opened them_

##### Submenus

Submenus should open next to the parent menu item without overlapping it.

Submenus are best used on large screens where there's space. See adaptive guidance for alternatives on mobile.

[IMAGE] A submenu opens to the right of its parent menu item, and doesn’t cover it. A selected submenu item includes a checkmark and vibrant highlight.

_Position submenus to the side of the parent item_

star

Note:
Submenus are not currently available on Jetpack Compose

#### Adaptive design

##### Compact breakpoints

Consider adapting menus into bottom sheets on small screens. They have more space to display additional items and longer labels.

[IMAGE] A bottom sheet shows longer labels and improved readability on a compact window.

_A bottom sheet can replace a menu on smaller screens_

##### Other breakpoints

On medium and expanded windows, menus are most effective as they appear in context with the content. On larger screens, menus can also display more items, and can use submenus to organize complex sets of options.

[IMAGE] A menu with vibrant color on a mid-size screen, with the same elements as a bottom sheet.

_On large screens, a menu is often more appropriate than a bottom sheet_

#### Behavior

##### Appearing

A menu can appear when a person interacts with an element on the page, like a button, text field, filter chip, or highlighted text.

A menu’s position on screen affects where and how it appears. If opened at the top of the screen, it expands downwards to avoid being cropped.

[VIDEO] A menu activated at the top of the screen expands downwards, then a menu opened at the bottom of the screen expands upward.

_Menus at different positions on a screen open in different directions, adapting to the available space_

[VIDEO] A menu expands downward from the top of the screen, appearing below a split button.

_A menu can open from a split button_

[VIDEO] A menu expands both above and below a line of selected text, separated by a gap.

_A menu can appear in context, like next to highlighted text or a selected image_

[VIDEO] Selecting the “Phone type” text field reveals a menu with multiple options: Business, Mobile (selected), and Home.

_A menu can open from a text field_

[VIDEO] A filter chip for “Cycling” in a map UI reveals more menus items: Running, Walking, and Hiking.

_A menu can open from a filter chip_

**Motion**

Menus use an enter and exit transition. This animation creates a relationship between the menu and the element that generates it.

When a menu expands, the trigger element becomes pressed. When an item is selected, a ripple appears on touch.

[VIDEO] An animation for entering a new contact’s address. The state selection menu expands and the state California is selected.

_A menu expands when opened, and has a ripple when an item is selected_

In dense products, such as on desktop, menus can open instantly to reduce motion.

[VIDEO] A menu for changing a font type opens instantly on a desktop UI.

_Desktop menus can open instantly_

##### Filtering

A menu can include a text field to filter options. This pattern is also known as autocomplete.

As someone types, the list of menu options filters to show relevant results. This helps people quickly find the right option from a long list.

Menu items ease into their new position as the menu is filtered.

[VIDEO] An animation showing a text field being typed into. As text is added, the list of menu items below filters down to show only matching options.

_As a person types in the text field, the menu options filter to match the input_

##### Scrolling

Menus can scroll when all menu items can’t display at once. In this state, menus show a persistent scrollbar.

Don’t use gaps if a menu scrolls; this is currently unsupported.

[VIDEO] A font menu on a document shows a scrollbar to access font options not currently visible.

_When content is scrollable, menus display scrollbars_

##### Selecting

When a menu is opened, the corresponding button or icon button should remain the same visually, with the addition of a pressed state.

This should happen even when opening from a keyboard shortcut.

[IMAGE] The overflow icon remains the same, even after the menu is opened.

_Tapping the icon triggers a menu. Choosing a menu option doesn’t change the icon generating the menu._

##### Single- and multi-select menus

Menus can allow either single-select or multi-select actions:
- **Single-select** menus can have one item selected at a time. When a new item is selected, the previously selected item is automatically unselected.
- **Multi-select** menus can have many selected items. They stay open until the person dismisses the menu.

More on selection accessibility requirements

[IMAGE] 1 menu for dietary options shows a single selection, Vegan. Another menu shows Vegan and Nut-free selections at the same time.

_Menus can be single- or multi-select_

#### Focus

When a menu has multiple submenus, focus follows the current hovered or focused submenu.

**Shape morphing **

As a person moves from one submenu to the next, the corners of the focused submenu become more rounded, while the unfocused submenu becomes less rounded. This adds a dynamic quality to menu interactions.

[VIDEO] On a submenu next to a main menu, a selected item’s corner shape expands for added emphasis.

_On a custom menu, the corner shape changes to indicate focus as the cursor moves across submenus_

#### Density

On web only, density levels control the spacing between elements. Increasing density decreases the top and bottom padding. More on layout density

[IMAGE] 4 menus becoming increasingly dense and compressed.

_Density of menus from 0 to -3_

## Accessibility

#### Use cases

People should be able to do the following using assistive technology:
- Navigate to, open, and close a menu
- Navigate between and select menu items

#### Interaction & style

Menu items need certain cues to clearly show when they're selected:
- By default, menu items change shape and color when selected
- The default color contrast is 3:1 between selected and unselected menu items
- It's recommended to include another visual cue, like a checkmark

[IMAGE] A state dropdown menu with the selected item Alaska highlighted in a vibrant color, with a checkmark icon.

_Use multiple visual cues like color, shape, and icons to show that an item is selected_

#### Flexibility & slots

Use caution when adding slots to menus:
- Make sure the menu remains accessible
- Elements must follow the rules and interaction patterns of the menu component
- Keep the same menu item padding
- Targets should be 48x48dp or larger

Don't add buttons, switches, or other direct actions into the menu item. Nested elements should only perform one action. Adding multiple actions can break keyboard navigation and screen reader functionality.

More on slots in menus

[IMAGE CAUTION] 1 diagram and 1 menu showing icons in each item’s leading slot.

**CAUTION:** _Reserve the use of slots for use cases that maintain the menu’s accessibility and functionality_

#### Focus

**Initial focus**

When a menu opens, focus should be placed on the first menu item. This allows people using a keyboard or other assistive technologies to begin navigating the menu immediately.

**Exiting a menu**

People expect to exit a menu by:
- Selecting an option
- Tapping **Escape** or outside of the menu
- Using the system back button

Where focus is placed after closing the menu depends on the app.

[IMAGE] 4 common keyboard navigation methods for menus on Android and web.

_Keyboard navigation on Android and web:
- **Tab** to select a menu item
- **Space** or **Enter** to open a menu
- **Space** or **Enter** to select a menu item
- **Escape** to close a menu_

#### Keyboard navigation

| **Keys**
| **Actions**

| **Tab** | Focus lands on menu
|

**Space **or** Enter** |

For closed menus: Opens menu or submenu

For open menus: Selects a menu item
| **Up** and **Down** arrows |

For closed menus: Opens menu

For open menus: Moves focus to the next item
| **Left** and **Right** arrows | Opens or closes a submenu
| **Letters** | Focus moves to the next menu item starting with letter
| **Escape** | Closes menu

#### Interactability

Disabled menu items can receive focus but aren't selectable.

Dividers and gaps can't receive focus.

[IMAGE DO] A disabled menu item “Share” is in focus.

**DO:** _Disabled menu items can receive focus_

[IMAGE DON'T] A divider with focus.

**DON'T:** _A divider or gap can’t receive focus_

#### Labeling elements

Accessibility labels are used with assistive technology devices like screen readers.

The accessibility label should be the same as the menu item text.

The role is dependent on platform.

[IMAGE] A “Preview” menu item has an accessibility label of ”preview”.

_The menu item’s accessibility label aligns with the UI text_

|
**Element** |

**A11y label** |

**Role (Web)** |
**Role (Android Views)
** |

**Role (Jetpack Compose)**
| Menu item text | Preview | Menu item | Generic actionable element | Generic actionable element

For menu items with text and an icon, the icon’s accessibility label should be marked as **decorative** to avoid redundant verbalizations.

[IMAGE] A menu item icon of an eye next to the word “preview” has a note of “Decorative.”

_For menu items with text and an icon, the icon’s accessibility label is **decorative**_
