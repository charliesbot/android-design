# Canonical layout examples
Source: https://m3.material.io/foundations/layout/canonical-examples

> Canonical layout examples are designs for common screen layouts across all breakpoints

## Overview

Canonical layout examples demonstrate how to implement the layout scaffold. They’re also available in code to provide a strong starting point for your product.

Each layout example considers common use cases and components to address expectations and user needs for how products adapt across breakpoints (previously window size classes).

[IMAGE] A messaging app on desktop mapping bar, rail, and pane regions.

_A layout scaffold can include bar, rail, and pane regions_

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

Implementation |

Jetpack Compose: Canonical layouts |

Available
|

Android Views (MDC-Android): Canonical layouts |

Available

#### Examples

There are three canonical layout examples: feed, list-detail, and supporting pane. Each example has configurations for compact, medium, and expanded breakpoints.

Use these canonical examples as a starting point to create layouts for a product.

##### Feed

Use a feed layout to arrange elements like cards in a configurable grid for a quick, convenient view of a large amount of content.

More on feed layouts

[IMAGE] A course list in a feed layout at a medium breakpoint. 8 cards in a single pane, with bar above and leading rail.

##### List-detail

Use a list-detail layout to display explorable lists of items alongside each item’s details. This layout divides the window into two side-by-side panes.

More on list-detail layouts

[IMAGE] List-detail layout of a messaging app. Pane 1 lists all conversations. Pane 2 shows the selected message.

__

##### Supporting pane

Use a supporting pane layout to organize content into primary and secondary sections:
- Primary display area: Contains the main content and occupies the majority of the window (typically about two-thirds)
- Secondary display area: Presents supporting content in a panel that takes up the remainder of the space

More on supporting pane layouts

[IMAGE] Supporting pane layout. The primary pane shows course details. The  secondary pane lists “Lessons in this course”.

#### Advanced custom layouts

To create a custom layout, build on top of canonical layouts or layer scaffold elements.

##### Layering

Use the levitate adaptive strategy to create a layered layout. Layering panes above other content can create a focused, task-oriented experience such as:
- Reviewing a shopping basket
- Responding to comments
- Creating a calendar event

[IMAGE] Custom layout. A shopping basket floats above a clothing product page.

_Layering panes helps people focus on a specific task_

## Feed

A feed layout uses a grid composition to enable quick content browsing and discovery. Key use cases include news, photos, and social media.

[IMAGE] Education app using a feed layout.

_Feed layouts help people quickly browse and discover content_

#### Usage

Use a feed layout to show different pieces of content through cards and lists.

Feeds support displays of almost any size as grids can adapt from single to multi-column.

[IMAGE] An education app has 2 columns on mobile, and 4 columns on a tablet.

_Feed layouts can adapt the number of columns across breakpoints_

#### Dividing space

A feed composition is flexible enough to allow for content with varying proportions and sizing.

[IMAGE] Feed layout in a medium window with 2 panes. 1 pane has 2 columns of small cards and the other has 1 large card.

_Feeds can organize content of different sizes, like using small and large cards_

Use size and position to establish relationships among content elements.

Feed items should reflow when the amount of available space changes like:
- Rotating or unfolding a device
- Entering multi-window mode

The order of items is determined by their position.

More on adaptive design for cards

[IMAGE] Lead article image is prominent in a 2-pane news feed.

_Feed items can change size to group content_

#### Across breakpoints

##### Compact

A feed layout should stack vertically, like a list of cards with individual items filling the width of the pane.

[IMAGE] Cards in 2 feed layouts, compact and expanded.

_In compact windows, the cards in a feed stack vertically, filling the full width:
- Compact breakpoint
- Expanded breakpoint_

##### Medium

A feed layout can support components with different widths and be split across multiple columns.

[IMAGE] In a medium window, 4 equal-width columns of cards in a feed layout.

_Feed layouts can add columns in a medium window_

##### Expanded, large, & extra-large

A feed layout can support components with different widths and be split across multiple columns. The number of columns should usually increase at expanded breakpoints.

[IMAGE] Expanded window has wider columns than a compact window.

_Column width can increase at larger breakpoints_

## List-detail

Many layouts can be split into a list view and a detail view.

Key use cases for this layout include parent-child pairings of information like:
- Text message + conversation
- File browser + open folder
- Musical artist + album detail
- Settings + category detail
- Email inbox + selected email

[IMAGE] An email app in a list-detail layout at a medium breakpoint.

_- List
- Detail_

#### Usage

Use the list-detail layout for quickly accessing details of an item from a long list of content.

Examples include:
- Showing a series of conversations and a text message
- Browsing files and seeing their details
- Browsing multiple albums and seeing individual track information

[IMAGE] Several stacked cards make up the list area on the left pane, while the detail area is a single section on the right pane.

_Simplified diagram of:
- List area
- Detail area_

##### Dividing space

[IMAGE] Compact windows have 1 pane, while medium and expanded windows can have 2 panes for list-detailed views.

_The most basic list-detail views for compact, medium, and expanded layouts_

A list-detail layout uses two panes. Depending on the breakpoint, the two panes may appear together in the same layout or across separate layouts.

List-detail layouts use the same pane guidance as all single and two-pane layouts, including special behavior for foldables.

|

**Breakpoint (dp)** |

**Visible panes**
|

Compact (0-599) |

1 pane
|

Medium (600-839) |

1 (recommended) or 2 panes
|

Expanded (840+) |

2 panes
|

Large (1200-1599) |

2 panes
|

Extra-large (1600+) |

2 panes

#### Across breakpoints

##### Compact
- Use a single-pane layout
- Only one view is visible at a time, either list or detail

[IMAGE] Single-pane layout on 3 devices at compact breakpoints.

_- Phone in portrait orientation
- Closed foldable
- Tablet in split-screen mode_

##### Medium
- Use a single-pane layout for information-dense content or deep focus

[IMAGE] Single-pane layout on a foldable open flat and a tablet in portrait orientation.

_- Foldable open flat
- Tablet in portrait orientation_

- Use a two-pane layout to browse collections and switch between items quickly
- To maximize horizontal space for two-pane layouts, use a bottom navigation bar or modal navigation rail

[IMAGE] 2-pane layout on a foldable open flat and a tablet in portrait orientation.

_- Foldable open flat
- Tablet in portrait orientation_

##### Expanded, large, & extra-large
- Use a two-pane layout

[IMAGE] 2-pane layout on a phone and tablet, both in landscape orientation.

_- Phone in landscape orientation
- Tablet in landscape orientation_

#### Behavior

##### Single vs two-pane
- Back button: Appears in detail view only for single-pane layouts
- Selected state: Appears only in list view for two-pane layouts
- Visual focus: Use explicit and implicit grouping to direct focus in two-pane layouts

[IMAGE] A 2-pane layout shows a selected list item. A single-pane layout uses a Back button to return to the list.

_- Use a selection state in two-pane layouts only
- Use a back button in single-pane layouts only_

##### Transitioning between layouts

The amount of available space is dynamic and changes based on how someone uses a device, such as rotating or unfolding it, or entering multi-window mode.

[VIDEO] Device rotating from landscape to portrait mode, reducing layout panes from 2 to 1.

_A two-pane layout adapts to a single-pane layout when a device rotates from an expanded to medium breakpoint_

###### No selected list item

A single-pane layout shows a list view, while a two-pane layout shows placeholder content in the detail pane.

In some cases, such as multi-select, the most recently used pane should stay visible when switching to single-pane layout.

[VIDEO] No item selected on a folded device. When unfolded, the detail pane is empty.

_If no item is selected when a foldable opens, the detail pane displays an empty state_

###### Selected list item

When switching from a single- to two-pane layout, both panes should be shown and the selected item’s details are visible.

When going from a two- to single-pane layout, the view depends on the product:
- The detail pane should typically show in a single-pane layout, and an app bar appears
- If the product supports selection without deep navigation, like multi-select, the list view can show with the item selected
- Consistency is key: If a layout showed the list view previously, it should return to that view when returning to a single pane

[VIDEO] Item selected on a folded device. When unfolded, the detail view shows the item.

_If an item is selected when a foldable opens, the detail pane shows that item_

[VIDEO] Item selected on an unfolded device. When folded, only the detail view shows.

_When a foldable closes with an item selected, the single pane shows the detail view_

###### Persistent states

In most cases, a state should be saved when navigating between detail views. This includes read and unread content.

[VIDEO] Scroll position is kept after folding or unfolding the device.

_Detail views should retain their scroll position when navigating to other items_

## Supporting pane

The supporting pane layout organizes content into primary and secondary areas.

The primary area contains the main content and occupies the majority of the space. The secondary area contains supporting content.

Key use cases for supporting pane layouts include:
- Productivity
- Document editing and commenting
- Content and media browsing

[IMAGE] A video app has the main content in the primary area and “up next” content is listed in the secondary area.

_Supporting pane layouts organize content into primary and secondary areas_

#### Usage

Use the supporting pane layout when the secondary content is only meaningful in relation to the primary content.

For content with a parent-child relationship, use a list-detail layout instead.

[IMAGE] The supporting pane has vertically stacked cards.

_Supporting panes provide contextual info for the primary area_

#### Dividing space

The window is divided between a focus pane and a supporting pane.

Depending on the breakpoint, the supporting pane may appear below or beside the focus pane.

[IMAGE] The cards of a supporting pane scroll horizontally across the bottom of the screen.

_Supporting panes can appear beside or below the primary area_

|

Supporting pane placement |

Pane width |

Breakpoint
|

Below |

Flexible |

Compact or Medium
|

Leading or trailing |

Fixed (360 dp) |

Expanded

#### Across breakpoints

##### Compact

The supporting pane should appear below the focus pane.

A bottom sheet can be useful for keeping focus on the primary pane while providing access to supporting information.

[IMAGE] 2 layouts showing  bottom sheets at a compact breakpoint.

_Bottom sheets can provide supporting information in compact windows_

##### Medium

The supporting pane should appear below the focus pane.

[IMAGE] 3 cards in a supporting pane are horizontal across the bottom of a tablet.

_Supporting panes appear below the focus pane in medium windows_

##### Expanded

The supporting pane should appear on the leading or trailing side of the focus pane.

[IMAGE] The supporting pane is to the right of the primary focus pane on 2 screens.

_Supporting panes appear beside the focus pane in expanded windows_
