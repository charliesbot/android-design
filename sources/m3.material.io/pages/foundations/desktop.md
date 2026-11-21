# Design for desktop
Source: https://m3.material.io/foundations/desktop

> Create desktop experiences with adaptive layouts and multiple inputs

## Overview

- People can use any Android app in a desktop environment by opening it on a Chromebook or Googlebook, docking a tablet to a folio, or connecting a phone to an external monitor
- Design apps so they’re easy to use on desktop, rather than feeling like a stretched mobile app
- Take advantage of the expanded space and longer viewing distances, and consider denser layouts that show more information at once
- Use adaptive components and layouts so apps scale smoothly across breakpoints
- Support external devices for efficient navigation across every input type, including mice, trackpads, and physical keyboards

#### Availability & resources

|

Type |

Resource |

Status
|

Design |

M3 Design Kit (Figma) |

Available
|

Android design guidelines |

Available
|

Material layout guidelines |

Available
|

Implementation |

Android large screen dev docs |

Available
|

Jetpack Compose |

Available
|

Android media query API |

Available

#### Design principles

##### Adaptive from the start

Adapting an app to the desktop experience shouldn’t be an afterthought. Design an app's layout and information hierarchy in a flexible and modular way, so it can seamlessly adapt from mobile to a larger screen.

More on adaptive design

[IMAGE] 4 devices: tablet, mobile, laptop, and desktop.

_Design layouts to adapt seamlessly from mobile to expanded windows_

##### More screen, more done

Use the expanded window to show more information in a denser layout. Don’t simply scale elements to fill empty space.

More on desktop layouts

[IMAGE] Desktop document editor showing full-page text beside an open comments side panel in a single expanded window.

_On desktop, use dense layouts to take advantage of the large screen_

##### Refine the motion

Desktop transitions can be faster with small elements and short distances because the screen is unobstructed. When moving large elements across long distances, slow down or simplify transitions to prevent visual overwhelm.

[VIDEO] In mobile and desktop email views, a navigation rail collapses and expands at different speeds.

_Motion duration should adjust based on distance and element size_

##### Multitasking at the core

People expect to work on more than one thing at a time on desktop. While the multi-window OS supports working across apps, allow people to multitask within an app too.

[IMAGE] Overlapping desktop windows showing a multi-pane email app and a video playing in the left corner.

_In-app panels and multi-window views support concurrent workflows_

##### Balance efficiency with simplicity

While mobile design focuses on simplicity, people expect to accomplish more in fewer steps on desktop.

Balance shorter task flows with clear layouts to avoid overwhelming people with information.

[IMAGE] Desktop photo app with a collapsed navigation rail,  organized multi-column image grid in the primary pane, and item details in a supporting pane.

_Desktop workflows should balance density with visual clarity_

##### All input types are equally important

People frequently switch between touch, pointer, and physical keyboard inputs while on desktop. Support pointer and keyboard interactions to ensure efficient navigation across every input type.

More on desktop interactions

[IMAGE] 3 inputs: hands using touch on a screen, typing on a physical keyboard, and controlling a mouse.

_Support all input types, including touch, keyboard, and pointer_

## Layout

#### Adaptive desktop layouts

Desktop apps typically open at medium or large breakpoints and can expand to extra-large. The quality bar is higher because of the larger window sizes and people’s expectation for greater information density. Make sure the app makes good use of the expanded space.

Use these guidelines to scale a mobile app to a desktop layout.

##### Use adaptive pane layouts

Consider using multi-column or multi-pane layouts for medium, large, and extra-large breakpoints to show more content at once.

If keeping a single pane on desktop, cap and center the content width to avoid looking like a stretched phone UI.

More on pane layouts

[IMAGE] A mobile app shown on desktop is capped and centered on the screen.

_Cap and center content in single-pane layouts to avoid stretched views_

##### Consider navigation & usability

Start with Material canonical layouts to automatically adapt mobile layouts to fill the available space on desktop. These make app navigation simple and predictable:
- Feed: For long scrolling pages of content
- List-detail: For selecting and opening an item, like chat or email
- Supporting pane: For showing additional tools or supporting information

More canonical layout examples

[IMAGE] A desktop app has an expanded navigation rail and a supporting pane with a list of “Lessons in this course.”

_Add columns or panes to support faster navigation and richer workflows_

##### Adapt components

Material components can adapt their layout across breakpoints to optimize space and support mouse and keyboard inputs.

For example, at medium and larger breakpoints, changing the navigation bar to a navigation rail gives more vertical space to the content.

More on adapting components for desktop

[IMAGE] A clock timer mobile UI has a navigation bar which switches to a navigation rail on a tablet.

_A navigation bar on mobile should adapt to a navigation rail in larger windows_

## Components

#### Adapt components for desktop experiences

Components like navigation rails, menus, side sheets, dialogs, and carousels are optimized for larger screens and desktop inputs.

##### Don’t overstretch components

While full-width buttons and text fields work well on mobile, they can feel imprecise on large screens. On desktop, maintain default component widths and padding, and don’t stretch these components to fill the pane.

##### Use navigation rails

If a mobile app uses a navigation bar, switch to a navigation rail at medium breakpoints and above.

At extra-large breakpoints, consider expanding the navigation rail by default to improve label readability.

[VIDEO] A collapsed navigation rail in a clock app expands and shows more menu items.

_Use navigation rails at medium breakpoints and above, and consider expanding them by default at extra-large breakpoints_

##### Replace bottom sheets

On mobile, bottom sheets are easy to reach, and their attachment to the device edge and scrim creates an immersive view.

However, on desktop, bottom sheets often require excessive cursor travel and can obstruct primary content. For an improved desktop experience, consider replacing bottom sheets with:
- Context menus
- Side sheets
- Dialogs

If using a bottom sheet on desktop, it must be detached from the bottom edge of the window.

[IMAGE] Mobile map UI with a bottom sheet anchored to the bottom edge with 6 actions.

_Bottom sheets provide an easy-to-reach view on mobile devices_

###### Context menus

Use right-click context menus when replacing bottom sheets triggered by press-and-hold or overflow actions on mobile.

[IMAGE] Satellite map on desktop with a context menu with 5 actions next to a pointer cursor.

_Replace press-and-hold bottom sheets with right-click context menus on desktop_

###### Side sheets

Use a side sheet with no scrim to show complex tools or configuration panels on desktop without obstructing the main content.

[IMAGE] Satellite map on desktop with a side sheet with 6 location options.

_On desktop, use side sheets to display tools or configuration panels alongside the main content_

###### Dialogs

Use a dialog when replacing modal bottom sheets that require a decision, since attention on desktop is focused higher on the screen.

[IMAGE] Satellite map on desktop with a centered dialog displaying pin coordinates and 2 action buttons.

_On desktop, use a dialog to keep important decisions in the person's line of sight_

##### Ensure carousels work with desktop inputs

For better cursor and keyboard interaction control, carousels can display direct targets for navigation when hovered.

Only hero carousels have built-in next and previous actions. If adding these buttons to other carousels, they should appear when the carousel is hovered or focused.

Consider adding a **Show all** button near the carousel to give people a persistent way to view all items.

[VIDEO] Photo carousel on desktop that scrolls horizontally. When a pointer hovers on it, previous and next buttons appear at the leading and trailing edges.

_When hovered, a multi-browse carousel can show next and previous buttons_

Hero and full-screen carousels can also include pagination dots for quicker navigation.

[IMAGE] Photo carousel on desktop with a hero-centered item and dot pagination showing 5 total items.

_A hero-centered carousel can show dot pagination_

###### Scrolling carousels on desktop

People can use a mouse wheel to scroll vertical hero carousels, or hold **Shift** to scroll horizontal carousels. On touchpad, people can scroll using a two-finger swipe. A single-pointer alternative is also required.

## Interaction

#### Support desktop interactions & inputs

People navigate desktop apps using touch, pointer devices, and physical keyboards. To make sure interactions like drag and drop, zoom, and refresh work across inputs:
- Provide single-click controls
- Support keyboard shortcuts
- Show clear hover and focus states

#### Drag & drop

Drag and drop allows people to quickly move or copy files, text, cards, list items, or input chips.

##### Provide clear visual feedback

Use Material states to give clear feedback throughout a drag-and-drop interaction:
- When dragging starts, apply the dragged state and elevation to create depth and focus
- Make the dragged item translucent so drop targets are visible beneath it
- Highlight valid drop targets with a border or insertion point when an item hovers above them

All drag-and-drop actions should include a single-click alternative, like a + icon button that opens a menu to upload an image without dragging.

[IMAGE] Laptop with photo and chat apps open. A hand cursor drags a semi-transparent photo tile into the chat attachment field.

_Represent dragged items with translucent images to keep drop targets visible_

##### Show clear error states

Display a prohibitive cursor symbol when hovering over invalid drop targets.

If an item is dropped where the action can't be completed, it should return to its original location or visually dissolve.

[IMAGE] A cursor with a red blocked badge drags a photo tile over a read-only pane in a desktop photo app.

_Display a prohibitive cursor symbol over invalid drop targets_

##### Make pointer dragging instant

Pointer dragging should be instantaneous, unlike touch interactions that often require a long press.

[VIDEO] On a laptop, a clock app time is dragged and dropped instantly into a new location, compared to a long press on mobile.

_Pointer drag interactions should be performed instantly_

#### Zooming & panning

Support zooming and panning across pointer, keyboard, and touch inputs so people can efficiently navigate large-scale content.

##### Provide multiple ways to zoom

On desktop, people should be able to zoom with keyboard shortcuts, scroll actions, and touch gestures:
- Zoom-first content: Scroll to zoom in and out on a map
- Pan-first content: Hold **Control** or **Cmd** while scrolling a canvas
- Single-pointer control: Include dedicated + and - icon buttons
- Touchpads: Support a two-finger pinch-to-zoom gesture

[IMAGE] Satellite map window with a location card, and trailing zoom + and - icon buttons.

_Include dedicated **+** and **-** buttons as an accessible single-pointer zoom alternative_

##### Adapt panning interactions

For zoom-first content, people should be able to pan by clicking and dragging with a mouse or touchpad once zoomed in.

For pan-first content, people should be able to pan by:
- Scrolling vertically, or holding **Shift** and scrolling horizontally
- Swiping with two fingers on a touchpad

[VIDEO] Satellite map window showing a pointer over alpine terrain. The map is zoomed in then pans.

_People should be able to swipe, scroll, or click and drag to pan zoomed-in content_

#### Secondary actions

To save screen space and reduce visual complexity on desktop, consider hiding secondary actions and revealing them only when necessary.

##### Use context menus

A context menu helps people maintain focus in the main window during element-specific tasks, like copy and paste.

Context menus should open with a secondary click, a long press, or an overflow icon button.

[IMAGE] Satellite map window showing a floating menu of 6 actions beside the pointer and a trailing location card.

_Context menus open with a secondary click, a long press, or an overflow icon_

##### Reveal actions on hover

Secondary actions can also appear when a cursor or stylus hovers over an area.

Don’t use hover as the only way to access these actions.

[IMAGE] On a satellite map, a hand cursor hovers over Tremorgio Lake to reveal a location card with a "Save to atlas" button.

_Secondary actions can appear on hover_

#### Making multiple selections

Multi-select helps people complete bulk actions.

On desktop, people can use the following pointer and keyboard interactions to select multiple items:
- Select one-by-one: **Control** + click. or **Cmd** + click (Mac)
- Select a range: **Shift** + click
- Select a group: Click + drag
- Select all: **Control** + A, or **Cmd** + A (Mac)

[IMAGE] Desktop journal app with 3 items selected.

_People can select multiple items using **Shift** + click_

#### Refreshing

When designing a refresh action, make sure to consider all input types.

On desktop, people can refresh a page by pressing **Control** + R, or **Cmd** + R on Mac.

Include a dedicated button so people using non-touch pointer devices or assistive technologies can refresh content with a single click.

[IMAGE] Browser window showing a course page with a selected reload icon in the search bar.

_Include a dedicated button for people to refresh content_
