# Toolbars
Source: https://m3.material.io/components/toolbars

## Overview

- Two expressive variants: **docked toolbar** and **floating toolbar**
- Use the vibrant color style for greater emphasis
- Can display a wide variety of control types, like buttons, icon buttons, and text fields
- Can be paired with FABs to emphasize certain actions
- Don’t show at the same time as a navigation bar

[IMAGE] 2 variants of toolbars.

_Configurations of floating toolbars_

#### Availability & resources

#### M3 Expressive update

The **bottom app bar** is no longer recommended and should be replaced with the **docked toolbar**, which functions similarly, but is shorter and has more flexibility. The **floating toolbar** was created for more versatility, greater amounts of actions, and more variety in where it's placed. More on GM3 Expressive

Variants and naming:
- Added **docked toolbar **to replace **bottom app bar**
  - Size: Shorter height
  - Color: Standard or vibrant
  - Flexibility: More layout and element options

- Added **floating toolbar** with the following configurations:
  - Layout: Horizontal or vertical
  - Color: Standard or vibrant
  - Flexibility: Can hold many elements and components. Can be paired with FAB.

- **Bottom app bar **is still available, but not recommended

[IMAGE] 2 examples of toolbar variants.

_- Floating, vibrant color scheme and paired with FAB
- Docked with embedded primary action instead of FAB_

#### Differences from M2

- Color: New color mappings and compatibility with dynamic color
- Elevation: No shadow
- Layout: Container height is taller and the FAB is now contained within the app bar container

[IMAGE] M2 bottom app bar.

_M2: Bottom app bar had higher elevation of 8dp and didn't contain the FAB_

[IMAGE] M3 bottom app bar.

_M3: Bottom app bar has new colors, a taller container, no elevation or shadow, and contains the FAB_

## Specs

#### Variants

[IMAGE] 2 variants of toolbars.

_- Docked toolbar
- Floating toolbar_

##### Baseline variant

The baseline bottom app bar is no longer recommended. It should be replaced with the docked toolbar, which is very similar and more flexible.

[IMAGE] Baseline bottom app bar, which looks like the docked toolbar, but is not recommended.

_- Bottom app bar (not recommended)_

|

Variant |

M3 |

M3 Expressive
|

Docked toolbar |

-- |

Available
|

Floating toolbar |

-- |

Available
|

Bottom app bar |

Available |

Not recommended.

Use **docked toolbar**.

star

Note:
Implementation differs per platform. On Jetpack Compose, the floating toolbar is a separate component from the docked toolbar and bottom app bar.

#### Configurations

[IMAGE] Color configuration of toolbars.

_- Standard and vibrant toolbars
- Vertical floating toolbar
- Floating toolbar with FAB_

|

Category
|

Configuration
|

M3
|

M3 Expressive

|

Color
|

Standard (default)
|

Available as bottom app bar
|

Available

|

Vibrant
|

-- |

Available

|

Floating toolbar layout
|

Horizontal (default)
|

-- |

Available

|

Vertical
|

-- |

Available

|

Other elements |

With FAB |

Available as bottom app bar |

Available*

star

Note:
*Implementation differs per platform. On Jetpack Compose, floating toolbar with FAB is fully supported. On other platforms, each component needs to be added separately.

#### Tokens & specs

Browse the component elements, attributes, tokens, and their values. Jump to baseline bottom app bar specs

#### Anatomy

[IMAGE] 2 elements of a toolbar.

_- Container
- Placed components_

##### Flexibility & slots

When configuring a toolbar, think of it as a container with several slots.

Each slot can be a different element. The most common elements are icon buttons, buttons, and text fields.

[IMAGE] A toolbar with 5 slots, conceptual spaces for UI elements, next to each other.

_A toolbar is essentially a container with configurable slots_

#### Color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

##### Standard

[IMAGE] 4 color roles in the standard color scheme  of the floating toolbar in light and dark scheme.

_Standard color schemes and icon button types:
- Surface container
- Filled button (Primary, On primary)

- Toggle tonal button (Secondary container, On secondary container)
- Standard button (Primary)_

##### Vibrant

[IMAGE] 4 color roles in the vibrant color scheme of the floating toolbar in light and dark scheme.

_Vibrant color scheme and icon button types:
- Primary container
- Filled button (Primary, On primary)
- Toggle tonal button: (Surface container, On surface)
- Standard button (On primary container)_

#### Measurements

By default all toolbars are 64dp high, center-aligned, have equal padding between items, and have a minimum outside padding of 16dp.

##### Docked toolbar

[IMAGE] Default internal padding of a docked toolbar.

_- Default margins and padding
- Margins and padding with leading, middle, and trailing content_

[IMAGE] 2 docked toolbars with different margins and alignment.

_Alignment and padding can be configured to create unique layouts:
- Left and right alignment
- Center-aligned, 8dp padding between items_

##### Floating toolbar

[IMAGE] Diagram noting margin around edge of floating toolbar.

_Default padding of floating toolbar_

[IMAGE] Diagram noting layout measurements.

_Floating toolbar size and padding measurements_

[IMAGE] Diagram noting layout margins.

_Floating toolbar margins_

###### Bottom app bar (baseline)

[IMAGE] Diagram of bottom app bar indicating the container.

_- Container_

##### Tokens & specs

Bottom app bar tokens are in one token set.

##### Color

Color values are implemented through design tokens. For designers, this means working with color values that correspond with tokens. In implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] Diagram of bottom app bar indicating its color mappings.

_Bottom app bar color role used for light and dark themes:
- Surface container_

##### Measurements

[IMAGE] Diagram showing layout values and paddings for bottom app bar.

_Bottom app bar padding and size measurements_

##### Common layouts

[IMAGE] Side by side view of bottom app bars in different configurations.

_- Icon buttons and FAB
- Icon buttons and no FAB_

## Guidelines

[IMAGE] 5 toolbars of various colors, elements, and actions.

_Toolbars can be used for a wide variety of use cases_

#### Usage

Use a toolbar to provide actions related to the current page.

Toolbars can contain many actions and can scale to show more actions in larger windows.

[IMAGE] Vibrant toolbar at bottom of mobile screen.

_A toolbar provides actions related to the current page_

There are two variants of toolbars:
- **Docked toolbar**
Spans the full width of the window. It’s best used for global actions that remain the same across multiple pages.

- **Floating toolbar**
Floats above the body content. It’s best used for contextual actions relevant to the body content or the specific page.

The baseline **bottom app bar **is no longer recommended, but is still supported.

[IMAGE] Docked toolbar example.

_Docked toolbar shows global controls_

[IMAGE] Floating toolbar example.

_Floating toolbar show controls relevant to the current page_

When actions don’t fit in a toolbar, add a menu.

[IMAGE] Toolbar showing local navigation.

_Toolbar actions can open a menu_

There are two color configurations:

- **Standard**
A low-emphasis color scheme best used for focusing attention on the body content.

- **Vibrant**
A high-emphasis color scheme that draws attention to the controls. It can also indicate a temporary change in the page behavior, such as entering edit mode.

Consider using alternative color roles to create greater or lesser emphasis depending on the needs of the app. Experiment with different color roles to achieve different effects.

[IMAGE] Toolbar with low-emphasis controls.

_Use the standard color scheme to draw focus to content outside the toolbar_

[IMAGE] Toolbar with high-emphasis controls.

_Use the vibrant color scheme to emphasize controls or actions_

##### Toolbars & navigation bars

The toolbar and navigation bar are both placed at the bottom of the window, so should **not** be shown at the same time. Show the navigation bar on primary pages, and toolbars on subsequent pages with actions.

[IMAGE] A navigation bar shown on the main email Inbox page, and a toolbar shown when reading the email.

_- Navigation bar on a primary page

- Toolbar on a secondary page with contextual actions_

Floating toolbars can be used as tabs between related subsequent pages in the product hierarchy.

This helps group similar pages together, and shows that the selection affects the body content underneath.

[IMAGE DO] Floating toolbar with secondary navigation labels.

**DO:** _Keep navigation distinct, and use a toolbar to display local navigation on a specific page_

Consider the existing app hierarchy when using a toolbar for local navigation.

Avoid redundant or confusing navigation combinations in the same view.

[IMAGE DON'T] Floating toolbar with secondary navigation labels displaying above a bottom navigation bar.

**DON'T:** _Don’t show a navigation bar and a toolbar with navigation controls at the same time_

#### Anatomy

[IMAGE] Diagram of toolbar layouts.

_- Container

- Elements_

##### Container

The docked toolbar’s container spans the full width of the window.

Avoid applying rounded corners to the container. This can imply the container expands or changes upon interaction.

[IMAGE DO] Docked toolbar with square corners.

**DO:** _Use straight corners for docked toolbars_

[IMAGE DON'T] Docked toolbar with rounded corners.

**DON'T:** _Avoid modifying the container shape_

As long as there's a minimum of 16dp padding on the leading and trailing edge, arrange controls inside however you see fit. The 32dp padding between items is just the default.

All elements need a minimum 48x48dp target area to be accessible.

Be cautious of including too many controls as it can be overwhelming.

[IMAGE DON'T] Docked toolbar with too many controls.

**DON'T:** _Don’t overwhelm people with too many controls_

The floating toolbar’s container should be fully visible on screen. If more actions are needed, use an overflow menu.

[IMAGE DO] Floating toolbar with overflow menu icon.

**DO:** _Choose the most essential actions to show on screen by default_

[IMAGE DON'T] Floating toolbar that expands off edge of screen.

**DON'T:** _Floating toolbars shouldn’t exceed the edge of the window or pane_

###### Elevation

Floating toolbars have elevation by default.

If the content beneath the toolbar is visually distinct, elevation can be removed.

[IMAGE] Vibrant floating toolbar that's easy to see in front of a neutral text background.

_The elevation on floating toolbars can be removed if on a visually distinct background_

##### Flexibility & slots

When configuring a toolbar, think of it as a container with several slots.

These slots can be populated by buttons, icon buttons, images, text fields, or any kind of custom component.

Icon buttons provide an even hierarchy of controls. Mixing in a filled icon button can help add emphasis to a single action.

[IMAGE] 5 toolbars with slots, and various combinations of buttons, icon buttons, filled icon buttons, and text fields.

_Toolbars are made of slots that can contain many kinds of actions_

Visually emphasizing a single action more than others is an effective way to create hierarchy and guide people to controls they use most often. Avoid emphasizing more than one action at a time.

Some common ways to add emphasis to toolbar actions include:

- Use different icon button color styles, such as filled, tonal, and standard
- Customize the color roles of a single action, such as a primary or secondary palette
- Use wide and narrow icon buttons

- Pair the toolbar with a FAB

[IMAGE] 2 floating toolbars, 1 with a filled action button and 1 paired with a FAB.

_Two different ways to create a high emphasis action in toolbars_

[IMAGE DON'T] Floating toolbar with primary action and FAB.

**DON'T:** _Don’t emphasize multiple buttons with bold, primary colors, such as a button and FAB together. Emphasize one action at a time._

[IMAGE DON'T] Floating toolbar with different control designs.

**DON'T:** _Avoid mixing too many different controls in the same toolbar. A consistent control design keeps things clear._

Avoid using square icon buttons in floating toolbars. Their square shape conflicts with the fully-rounded shape of the floating toolbar container.

Square buttons can be used in the docked toolbar.

[IMAGE DON'T] A floating toolbar, which is rounded, with squared icon buttons inside.

**DON'T:** _Don’t use square filled icon buttons in floating toolbars_

##### Floating toolbar with FAB

A FAB can be placed next to a floating toolbar to present one high-priority action alongside a unified set of toolbar actions.

Use a FAB for the highest-priority action in the view, or to complement the controls.

[IMAGE] 3 toolbars paired with FABs.

_Floating toolbars can be paired with FABs_

#### Position & orientation

Only place docked toolbars at the bottom of the window.

If using other bottom-aligned elements, such as a navigation bar, don't use a docked toolbar.

[IMAGE] Docked toolbar on mobile.

_Docked toolbars are always at the bottom of the window_

Floating toolbars can be horizontal or vertical.

Horizontal toolbars should have a minimum 16dp margin from the edge of the window.

[IMAGE] Floating toolbar on mobile.

_Horizontal floating toolbars should be at least 16dp from the edge of the window_

In larger breakpoints, floating toolbars can be vertical and placed on either side of the screen.

Vertical toolbars should have a minimum 24dp margin.

[IMAGE] Vertical floating toolbar with 24dp margin.

_Maintain at least a 24dp margin for vertical toolbars_

To keep vertical toolbars compact, don’t use wide icon buttons.

Use narrow or default icon buttons instead.

[IMAGE DON'T] Toolbar showing local navigation.

**DON'T:** _Using wide buttons with vertical toolbars can unnecessarily widen toolbar containers and hide other UI elements_

Vertical toolbars should be positioned opposite the navigation rail to balance out the screen and keep actions easy to access.

When showing a navigation rail and vertical floating toolbar at once, use the centered configuration of the navigation rail.

[IMAGE] Large screen UI showing both a navigation rail and vertical floating toolbar.

_When a nav rail is visible, the floating toolbar should be vertical on the opposite edge of the window_

#### Adaptive design

Adaptive design allows an interface to respond or change based on context, such as the user, device, and usage. More on adaptive design

##### Resizing

###### Docked

The docked toolbar should always span 100% of the screen width.

In compact breakpoints, elements in the toolbar should be evenly spaced.

In medium breakpoints and larger, adjust the padding between controls to create a comfortable layout. This can be achieved by:
- Centering all elements
- Customizing to center a key action, and aligning other elements to the edges

[IMAGE] Docked toolbar with evenly spaced elements.

_Docked toolbar items should be evenly spaced in compact windows_

[IMAGE] Docked toolbar with centered elements.

_In medium breakpoints and larger, create a spacious layout by centering all elements_

[IMAGE] Docked toolbar with central action and some elements pushed to the edge.

_Align controls to the edge of the screen to make them easier to reach on tablets, and to better highlight a primary action in the middle_

On web and large screens, the docked toolbar can be rounded. Dividers can be used to organize large amounts of items. Only shrink the height and use extra small buttons if vertical space is limited.

[IMAGE] Docked toolbar with 15 actions for text editing on large screens, organized with dividers.

_On web and other large screens, docked toolbars can be rounded and placed in different parts of the page_

###### Floating

The container should only be as big as needed to hold the items inside before reaching the 16dp margin.

If there’s not enough space for all items, put them in an overflow menu in the trailing slot. As the breakpoint expands, more actions can be revealed.

The floating toolbar width can also be capped to keep it smaller and hide more elements.

[IMAGE DON'T] Floating toolbar in compact window with excess padding.

**DON'T:** _Don’t add extra space to a toolbar beyond its necessary items_

[IMAGE] Floating toolbar in expanded window class.

_At larger screen sizes, the container can display more controls before hitting the 16dp margin_

Vertical toolbars aren’t recommended for compact windows.

They take up a significant area of the screen and may feel visually overwhelming, especially on screens with complex layouts.

Only use them when the screen is simple or when the toolbar has a few controls.

[IMAGE CAUTION] Vertical toolbar in a compact window.

**CAUTION:** _Vertical toolbars can cover important content in compact windows_

##### Presentation

In larger breakpoints, floating toolbars can be aligned to opposite edges of the screen so they're easy to reach and group similar actions. For example, consider placing the undo and redo actions in one toolbar, and editing controls like highlight, erase, and select in another. Stylistic differences can help emphasize each toolbar’s purpose and clarify hierarchy.

[IMAGE] 2 toolbars, each with distinct stylistic treatment and actions.

_Multiple toolbars with different stylistic treatments can create hierarchy and distinguish different kinds of actions_

Don’t use multiple toolbars in compact windows. There typically isn’t enough room on screen.

Instead, use one toolbar for all actions.

[IMAGE DON'T] Multiple toolbars in a compact window.

**DON'T:** _Avoid using multiple toolbars in smaller windows_

Actions at the trailing edge of the toolbar can collapse into an overflow menu at smaller breakpoints, and become visible again at larger sizes.

[VIDEO] App bar resizing to wider view.

_Actions at the trailing edge collapse into an overflow menu_

##### Right-to-left languages

In right-to-left (RTL) languages, mirror individual items that need it, like icons and text direction. If the order of actions is important, flip the order of the actions as well.

[IMAGE] Next button is on trailing edge for a LTR language.

_In LTR languages, the **Next** button is intentionally placed on the trailing (right) edge_

[IMAGE] Next button is now on the trailing edge, at left, for RTL language.

_In RTL languages, reverse the order so **Next** remains on the trailing edge when flipped, now on the left. Text is not translated to illustrate mirroring._

#### Behavior

##### Scrolling

Docked toolbars can either remain on the screen during scroll, or animate offscreen.

[VIDEO] Docked toolbar animating off screen.

_Docked toolbars can animate offscreen_

Floating toolbars can remain on the screen, animate offscreen, or collapse into a single, high-emphasis action on scroll.

[VIDEO] Floating toolbar animating off screen.

_Floating toolbars can animate off screen_

On Jetpack Compose, the floating toolbar can collapse to a FAB or key action on scroll.

[VIDEO] Floating toolbar with several actions animating on scroll to hide all actions except the center one.

_Floating toolbars can be customized to do other actions on scroll, like collapse into a single action_

Don't collapse actions and scroll at the same time.

[VIDEO DON'T] A toolbar collapsing to one action, and moving off screen when the page is scrolled.

**DON'T:** _Toolbars shouldn't both collapse and transition off page_

## Accessibility

#### Use cases

People should be able to the following with assistive technology:

- Navigate and activate any actions in the toolbar

- Select a destination from a menu

- Activate a back button

- Maintain access to toolbar controls when the content is scrolled or collapsed

#### Interaction & style

The toolbar has no interactions by default. All interactions are with the elements placed inside.

**Touch**
- When tapping on an icon button in the toolbar, a touch ripple appears, indicating interaction feedback.

[VIDEO] An animation of the user tapping on an action item and the ripple effect being shown.

_Touch: Tap_

**Cursor**
- When hovered, the hover state provides a visual cue to the user that the element is interactive.

- When clicked (in both active and inactive states), a ripple appears, showing the user feedback.

[VIDEO] A mouse hovering over a button in the top app bar, then clicking.

_Cursor: Hover, Click_

##### Initial focus

Focus lands on the first interactive element.

Use **Tab** to navigate through all other actions.

[IMAGE] Navigating the top app bar using arrow or tab on a keyboard.

_Use **Tab** to navigate through interactive elements_

[IMAGE] Activating actions in the top app bar using space or enter on a keyboard.

_Use **Space** or **Enter** to activate actions_

#### Keyboard navigation

| Keys | Actions
| Tab or Arrows
| Navigate between interactive elements
| Space or Enter
| Activate the focused element

##### Labeling elements

On web, the toolbar container should have the **toolbar** role.

On mobile, it can be a generic container.

All actions inside the toolbar should follow their respective accessibility guidelines.

[IMAGE] A toolbar on web, with a “toolbar” role label.

_On web, use the **toolbar** role_
