# Breakpoints
Source: https://m3.material.io/foundations/layout/breakpoints

> Breakpoints ensure layouts work across a wide range of devices

## Overview

Material uses breakpoints to create adaptive designs that work across devices:
- There are five main breakpoints: compact, medium, expanded, large, and extra-large
- Layouts typically transition from a single pane to two or three panes as window size increases
- When moving across breakpoints, decide which elements to reveal, divide, resize, reposition, or swap

#### Breakpoints overview

A breakpoint (previously window size class) is the window size at which a layout needs to change to match available space, device conventions, and ergonomics. These apply to Android and web.

All devices fall into one of five Material breakpoints:
- Compact
- Medium
- Expanded
- Large
- Extra-large

Rather than designing for an ever-increasing number of display states, focusing on breakpoints ensures layouts work across a wide range of devices.

[IMAGE] 3 breakpoints from small to expanded.

_- Compact
- Medium
- Expanded_

Large and extra-large breakpoints are used on devices like laptops, desktops, and external monitors.

[IMAGE] 2 devices with large and extra-large breakpoints.

_- Large
- Extra-large_

**Design for breakpoints instead of specific devices because:**
- The amount of available window space is dynamic and changes based on user behavior, such as multi-window modes or unfolding a foldable device
- Devices fall into different breakpoints based on orientation

|

Breakpoint |

Width (dp) |

Common devices
|

Compact |

Under 600dp |

Phone in portrait
|

Medium |

600–839dp |

Tablet in portrait

Foldable in portrait (unfolded)
|

Expanded |

840–1199dp |

Phone in landscape

Tablet in landscape

Foldable in landscape (unfolded)

Desktop
|

Large |

1200–1599dp |

Desktop
|

Extra-large |

1600dp+ |

Desktop

Ultra-wide monitors

##### Height breakpoints

On Android, compact, medium, and expanded breakpoints are also available for height. These can be used to adjust the layout when available vertical space is unusually small or large. However, since most layouts contain vertically scrolling content, it's rare that layouts need to adjust to available height.

#### Designing across breakpoints

[IMAGE] An email app shown at 3 breakpoints: compact, medium, and expanded.

_Products should automatically adapt to any breakpoint_

[IMAGE] 2 screens at large and extra-large breakpoints.

_A product’s layout should adjust to fit each breakpoint. For example, a large window can have two panes, while an extra-large window can have three._

Each product view should have a layout for the breakpoints most appropriate for your platform and users.

Different components are recommended for performing the same function across the five layouts.

|

Breakpoint |

Panes |

Navigation |

Communication |

Action
|

Compact |

1 |

Navigation bar, modal expanded

navigation rail |

Simple dialog

Full-screen dialog |

Bottom sheet
|

Medium |

1 (recommended) or 2 |

Navigation bar, modal expanded

navigation rail |

Simple dialog |

Menu
|

Expanded |

1 or 2 (recommended) |

Modal or standard expanded

navigation rail |

Simple dialog |

Menu
|

Large |

1 or 2 (recommended) |

Modal or standard expanded

navigation rail |

Simple dialog |

Menu
|

Extra-large |

1 to 3 (recommended) |

Modal or standard expanded

navigation rail |

Simple dialog |

Menu

Start by designing for one breakpoint, then adjust the layout for the next size by asking these five questions:

##### 1. What should be revealed?

Parts of the UI that are hidden on smaller devices can be revealed in larger layouts.

For example:
- On mobile, the navigation rail is collapsed by default
- On an expanded device, the navigation rail can be open by default, revealing more actions and features

[IMAGE] Email app layouts in compact and expanded windows.

_A product’s navigation rail can be revealed in an expanded layout_

The same can be applied to panes. Larger layouts can simultaneously display an inbox pane and a pane containing a selected conversation. Additional space doesn’t just mean making the same thing bigger.

[IMAGE] Messaging app layouts in compact and expanded windows.

_An expanded layout for a messaging app reveals a second pane with the selected conversation_

##### 2. How should a screen be divided?

When dividing a screen into layout panes, consider the breakpoint:
- Compact and medium breakpoints: A single pane works best
- Expanded and large breakpoints: Two panes are recommended
- Extra-large breakpoints: Consider using three panes

[IMAGE] A compact and medium window have a single pane. A larger window has 2 panes.

_Compact and medium breakpoints should use a single pane, while larger breakpoints can use two_

At medium breakpoints, two panes are useful when they contain low-density content with clear actions.

Don’t use two panes in medium layouts with high information density, as it can reduce usability.

[IMAGE] Medium window with 2 panes with low-density content: an email setting menu and a list of setting switches.

_A settings view with low-density content and quick actions is a good use of two panes in a medium layout_

[IMAGE] Video app in a medium window has 1 pane in portrait layout and 2 in landscape.

_Rotating a device often changes the breakpoint. A layout can have two panes in landscape orientation, and one pane in portrait._

Single-pane layouts can focus attention on one action or view, creating a distraction-free environment for a specific goal such as:
- Playing a game
- Watching a movie
- Video calls
- Creative applications

[IMAGE] Video app in a single-pane landscape layout at an expanded breakpoint.

_Consider using an immersive single-pane layout for video calls_

##### 3. What should be resized?

UI elements that are small on compact screens can grow as breakpoints increase. Panes can also expand to rearrange elements and make better use of space.

Consider resizing:
- Cards
- Feeds
- Lists
- Panes

Resizing can highlight imagery and improve text readability. This type of adaptation affects the scale of content and the relationship between objects on screen. For example, a vertical card on mobile can adjust its margins, orientation, text size, and density to better fit a tablet.

Across all breakpoints, adjust margins and type styles to keep text between 40–60 characters per line.

[IMAGE] 2 cards in compact and medium layouts. The image and text are rearranged and resized to be more legible in the medium layout.

_A small card in a compact layout can be resized larger in a medium or expanded layout_

##### 4. What should be repositioned?

A UI and its components can reflow or reposition to make use of additional space on expanded screens and in resized panes. Repositioning is also a way to match the ergonomic and input needs that change across device sizes, such as shifting actions from the bottom of a compact window to the leading edge of medium and expanded windows. This method is similar to responsive design on the web.

Consider:
- Repositioning cards
- Adding a second column of content
- Creating a more complex layout of photos
- Introducing more negative space
- Ensuring reachability for navigation and interactive elements

Internal elements can be anchored to the left, right, or center as a parent container scales. Internal elements can also maintain fixed positions, such as a floating action button (FAB) in a navigation rail.

[IMAGE] Comparison of compact and medium breakpoints with tabs anchored to the middle of each layout.

_Tabs can remain anchored to the middle of a layout at both compact and medium breakpoints_

In the case of a button, the icon and text label within the button container can remain anchored to each other, staying centered as the button container scales horizontally.

[IMAGE] Label text remains the same size and centered as button size increases.

_Button icons and label text can remain anchored to each other no matter the width_

##### 5. What should be swapped?

As a layout changes across breakpoints, components with similar functions can also be exchanged. This makes it possible to adjust a layout for large-scale changes to the ergonomic and functional qualities of an interface.

For example, a bottom navigation bar in a compact layout can be swapped with a navigation rail in a medium layout.

[IMAGE DO] A compact layout’s navigation bar becomes a navigation rail in an expanded layout.

**DO:** _Swap a navigation bar in a compact layout for a navigation rail in a medium or expanded layout_

Likewise, a navigation rail can swap from collapsed to expanded at larger breakpoints.

Use caution when swapping components. Make sure:
- The interchangeable components are functionally equivalent
- The component swap serves a functional and ergonomic purpose

Don’t swap a button for a chip. Be careful when changing between list items and cards.

[IMAGE DO] A compact layout’s navigation rail expands in an extra-large layout.

**DO:** _A collapsed navigation rail in medium or expanded layouts can become an expanded navigation rail in large or extra-large layouts_

[IMAGE DON'T] A button is swapped out for a menu in a food delivery app.

**DON'T:** _Don’t arbitrarily swap components that aren’t functionally equivalent, such as swapping a button with a menu_

##### Common swappable components

|

Component type |

Compact |

Medium |

Expanded
|

Navigation |

Navigation bar |

Collapsed navigation rail |

Collapsed navigation rail
|

Navigation |

Modal expanded navigation rail |

Modal expanded navigation rail |

Standard expanded navigation rail
|

Communication |

Basic or full-screen dialog |

Basic dialog |

Basic dialog
|

Supplemental selection |

Bottom sheet |

Menu |

Menu

## Compact

Layouts for compact breakpoints are for **screen widths smaller than 600dp.**

[IMAGE] Messaging app at a compact breakpoint.

_A compact breakpoint focuses on a single view_

#### Navigation

Use a navigation bar or modal expanded navigation rail.

Place navigation components close to the edge of the screen where they’re easier to reach.

[IMAGE] Navigation bar and FAB are close to the bottom of a mobile app in a compact window.

_Place navigation elements near the bottom of a compact window so they’re easy to reach_

#### Panes

Use a single pane in compact layouts.

[IMAGE] The single pane consumes most of the area in a compact window.

_- Single-pane layouts work best for compact breakpoints_

#### Spacing

Margins are 16dp from the leading and trailing edge of the window.

[IMAGE] The left and right margins of a compact window pane are 16dp.

_In compact layouts, use 16dp margins_

#### Special considerations

A compact layout will need to transition dynamically to a medium or expanded layout when:
- A foldable device is unfolded
- A mobile device is rotated from portrait to landscape
- A tablet exits split-screen mode
- A product is resized to be larger in multi-window mode
- A free-form window is resized

[IMAGE] 2 mobile layouts showing a messaging app with a 1-pane list and an expanded navigation rail.

_Compact layouts should dynamically transition to larger layouts when a device is unfolded or rotated_

## Medium

Layouts for medium breakpoints are for **screen widths from 600dp to 839dp.**

[IMAGE] A medium breakpoint with a video call app in full-screen mode.

_Single-pane layouts can focus attention on one action or view, such as a video call_

#### Navigation

Place navigation components close to edges of the window where they’re easier to reach:
- Single-pane layouts: Navigation rail
- Two-pane layouts: Navigation bar

The navigation rail can be hidden in secondary destinations as long as the primary destination can still be accessed using a back button.

[IMAGE] The navigation area of a medium breakpoint is a vertical bar at the leading edge, beside a single pane.

_- Navigation area
- Single pane_

#### Panes

##### Single-pane layout

In a medium layout, a single pane is recommended because of limited screen width.

[IMAGE] A single pane uses most of the space in a medium window.

_- A single-pane layout is recommended for medium breakpoints_

##### Two-pane layout

Limit use of two panes for content with lower information density, such as a settings screen.

Each pane in a two-pane layout should take up 50% of the window width. Avoid setting custom widths. A drag handle can be used to expand or collapse panes to be 100% of the window width.

[IMAGE] 2-pane layout in a medium window. Both panes fit 50% of the window width by default.

_Two-pane layouts should use 50% widths for each pane by default_

When adding navigation to a two-pane layout, use a navigation bar. This allows the panes to fully use the available window width.

[IMAGE] A navigation bar extends over 2 panes at the bottom of a medium window.

_Two-pane layout with:
- Navigation bar_

#### Spacing

Medium layouts have margins of 24dp.

The spacer between panes is also 24dp.

[IMAGE] 2 pane layout with 24dp margins and 24dp space between panes.

_Use 24dp for margins and spacer in a medium layout_

#### Special considerations

A medium layout will need to transition dynamically to a compact or expanded layout when:
- A foldable device is folded
- A tablet is rotated from portrait to landscape
- A product goes from full-screen to split-screen
- Multi-window mode is initiated
- A free-form window is resized

[IMAGE] Email app with 2 panes at  a medium breakpoint.

_Think of how a medium layout should change to a compact or expanded layout_

##### Reachability

For horizontal tablets and unfolded foldables, the top 25% of the screen is likely out of reach, unless the grip is adjusted. To accommodate device and hand sizes, limit the amount of interactions that are placed in the upper 25% of the screen.

[IMAGE] The hard-to-reach top quarter of a medium breakpoint in landscape mode.

_- Limit interactions in the upper quarter of a screen, as they can be hard to reach_

Avoid placing essential interactive elements too close to the bottom edge of the screen. Some users, particularly those with larger hands, might struggle to reach this area.

Specify interactions in a layout with these ergonomic regions in mind:
- Users can reach this area by extending their fingers, which makes it inconvenient
- Users can reach this area comfortably
- Reaching this area is challenging when holding the device

[IMAGE] The 3 ergonomic regions of a medium breakpoint.

_Medium breakpoint ergonomic regions:
- Inconvenient
- Comfortable
- Challenging_

## Expanded

Layouts for expanded breakpoints are for **screen widths from 840dp to 1199dp.**

[IMAGE] Supporting pane layout of a video app. The large, primary pane has the video, title, and actions. The secondary pane has queued videos.

_Two-pane layouts are often best for expanded breakpoints_

#### Navigation

Place navigation components close to edges of the window where they’re easier to reach. Use a navigation rail, either collapsed or expanded.

The navigation rail can be hidden in secondary destinations as long as the primary destination can still be accessed using a back button.

For sorting, filtering, or secondary navigation, use tabs or other components directly in the pane.

[IMAGE] The navigation area is a vertical bar at the left of the screen. To its right, the body pane fills the rest of the window.

_- Navigation area
- Single pane_

#### Panes

Use a single-pane or two-pane layout.

A two-pane layout is often best for expanded breakpoints. However, a single-pane layout can work when displaying visually- or information-dense content, such as videos.

[IMAGE] A single pane covers most of the expanded screen except for the navigation area and margins.

_At an expanded breakpoint, reserve single-pane layouts for information-dense content_

When using a fixed-and-flexible layout, the fixed pane should have a width of 360dp by default.

[VIDEO] A 2-pane layout at an expanded breakpoint. The fixed pane starts at 360dp, then expands to a single-pane layout.

_In a fixed-and-flexible layout, the fixed pane is 360dp by default_

A split-pane layout uses two flexible panes and visually centers the spacer by default.

[VIDEO] A nav rail and a pane fill 50% of the window. A second pane fills the remaining 50%.

_The navigation and first pane are 50% of the window width to keep the spacer visually centered_

#### Spacing

Expanded layouts have a leading and trailing margin of 24dp.

The spacer between panes is 24dp.

[IMAGE] 2 pane layout with 24dp margins and 24dp space between panes.

_Use 24dp for margins and spacer in an expanded layout_

#### Special considerations

An expanded layout will need to transition dynamically to a compact or medium layout when:
- A foldable device is folded
- A tablet is rotated from landscape to portrait
- The app goes from full-screen to split-screen
- Multi-window mode is initiated
- A free-form window is resized

[VIDEO] Email app moves from 2-pane expanded layout to medium to 1-pane small layout.

_Consider how an expanded layout should change at medium and compact breakpoints_

## Large & extra-large

These breakpoints are most useful for creating web experiences tailored to laptop and desktop devices. Some products may not need large and extra-large breakpoints. Consider your platform’s conventions and users when making decisions on which breakpoints to design for.
- Layouts for large breakpoints are for screen widths **from 1200dp to 1599dp**
- Layouts for extra-large breakpoints are for screen widths of **1600dp and larger**

[IMAGE] Large window with a video app in 2 pane layout.

_A two-pane layout is recommended for large and extra-large breakpoints_

#### Navigation

Use a navigation rail, either collapsed or expanded, depending on the amount of content.

For sorting, filtering, or secondary navigation, use tabs or other components directly in the pane.

[IMAGE] Large web browser with a small navigation area on the leading edge with 1 pane filling the rest of the window.

_- Collapsed navigation area
- Single-pane layout_

An expanded navigation rail is best suited for extra-large windows, where there's still plenty of room for content. Consider collapsing the navigation rail when space is needed, or when on pages deeper in the page hierarchy.

[IMAGE] Extra-large web browser with an expanded navigation area and 1 pane filling the rest of the window.

_- Expanded navigation area
- Single-pane layout_

#### Panes

A two-pane layout is often best for large and extra-large breakpoints.

However, a single-pane layout can work when displaying visually- or information-dense content, such as videos.

[IMAGE] The single pane covers most of a large screen except for the navigation area and margins.

_Only use a single-pane layout for dense content or media_

When using a fixed-and-flexible layout, the fixed pane should have a width of 412dp by default.

[IMAGE] A 2-pane layout with 1 pane fixed at 412dp.

_Fixed panes should be 412dp in large and extra-large layouts_

When using a split-pane layout, the spacer should be visually centered by default, even when using an expanded navigation rail.

[IMAGE] An expanded nav rail and the first pane take up 50% of the window, and the second pane takes up 50%.

_In split-pane layouts, navigation components shrink the leading pane, so the spacer remains centered_

#### Additional panes

The extra-large breakpoint supports using a standard side sheet as a third pane. When the side sheet is present, the navigation rail can remain visible, collapse, or hide completely. Don't use more than three panes.

Note: Fixed panes in this window size are recommended to be 412dp, but side sheets have a default maximum width of 400dp.

[IMAGE] Extra-large window with 2 panes and a side sheet acting as a third pane.

_- Standard side sheet (third pane)_

#### Spacing

Large and extra-large layouts have a leading and trailing margin of 24dp.

The spacer between panes is 24dp.

[IMAGE] 2 pane layout with 24dp margins and 24dp space between panes.

_Use 24dp for margins and spacers in large and extra-large layouts_

#### Special considerations

Large and extra-large layouts will need to transition dynamically to a smaller layout when:
- The app goes from full-screen to split-screen
- Multi-window mode is initiated
- A free-form window is resized

Pay attention to typographic elements such as line length to ensure readability on large and extra-large layouts.

[IMAGE] An email app with 2 panes in a large layout.

_Consider how a large layout should change at smaller breakpoints_
