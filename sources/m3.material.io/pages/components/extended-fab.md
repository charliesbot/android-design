# Extended FABs
Source: https://m3.material.io/components/extended-fab

## Overview

- Use for the most common or important action on a screen
- Three variants: small, medium, and large
- Use instead of FAB when label text is needed to understand action

[IMAGE] 3 extended fab sizes.

_- Small extended FAB
- Medium extended FAB
- Large extended FAB_

#### Availability & resources

#### M3 Expressive update

**May 2025**

The extended FAB now has three sizes: small, medium, and large, each with updated type styles. These align with the FAB sizes for an easier transition between FABs. The baseline extended FAB is no longer recommended and should be replaced with the small extended FAB. Surface and FABs are also no longer recommended. More on M3 Expressive

Variants and naming:
- Added new sizes
  - Small: 56dp
  - Medium: 80dp
  - Large: 96dp

- No longer recommended
  - Baseline extended FAB (56dp)
  - Surface extended FAB

Updates:
- Adjusted typography to be larger

[IMAGE] The baseline extended FAB and the small, medium, and large extended FABs from the expressive update.

_The baseline extended FAB is replaced with a set of small, medium, and large extended FABs with new typography_

#### Differences from M2

- Color: New color mappings and compatibility with dynamic color
- Layout: Extended FAB is the same height as the FAB
- Shape: Boxier style with smaller corner radius

[IMAGE] Diagram comparing the M2 FAB and extended FAB.

_M2: Extended FABs are pill-shaped and have a different height and elevation_

[IMAGE] Diagram comparing the M3 FAB and extended FAB.

_M3: Extended FABs share the same height, boxier shape, and simpler elevation model as FABs_

## Specs

#### Variants

[IMAGE] 3 variants of extended FABs.

_- Small extended FAB
- Medium extended FAB
- Large extended FAB_

##### Baseline variants

The baseline extended FAB is no longer recommended in the M3 expressive update. Use a small extended FAB; the type style was updated from **label large** to **title medium**, and the inner padding was reduced. View baseline extended FAB specs

[IMAGE] 1 baseline extended FAB.

_- Extended FAB_

|

Variant |

M3 |

M3 Expressive
|

Small extended FAB |

-- |

Available
|

Medium extended FAB |

-- |

Available
|

Large extended FAB |

-- |

Available
|

Extended FAB (baseline)  |

Available |

Not recommended.
Use **small extended FAB.**

#### Tokens & specs

Use the table's menu to select a token set. Extended FAB tokens are organized by size and color.

#### Anatomy

[IMAGE] 3 elements of extended FABs.

_- Container
- Label text
- Icon_

#### Color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

##### Color styles

Extended FABs can use several combinations of **color** and **on color** styles, such as **primary** and **on primary**. The following color mappings provide the same level of contrast and functionality, so choose a color mapping based on visual preference.

[IMAGE] 6 extended FAB color styles.

_Extended FAB color roles used for light and dark schemes:
- Primary container & on primary container (default)
- Secondary container & on secondary container
- Tertiary container & on tertiary container
- Primary & on primary
- Secondary & on secondary
- Tertiary & on tertiary_

##### Baseline color styles

Extended FABs should no longer use surface color styles. They’re still available, but not recommended.

[IMAGE] 1 baseline extended FAB color style.

_- Surface container FAB_

#### States

States are visual representations used to communicate the status of a component or interactive element. Learn more about interaction states

When using a non-default color mapping for extended FABs, make sure the state layer color is the same as the icon color. For example, the state layer color for primary mapping should be md.sys.color.primary.

[IMAGE] 4 states of extended FABs.

_- Enabled
- Hovered - elevation 4
- Focused
- Pressed_

#### Measurements

[IMAGE] Extended FAB padding and size measurements.

_Size and padding measurements of the small, medium, and large extended FABs_

[IMAGE] Extended FAB margin measurements.

_Extended FABs should have margins of 16dp_

#### Baseline extended FAB

[IMAGE] 3 elements of baseline extended FAB.

_- Container
- Label text
- Icon_

##### Baseline configurations

[IMAGE] Baseline extended FAB with icon.

_With icon_

[IMAGE] Baseline extended FAB without icon.

_Without icon_

##### Baseline tokens

Use the table's menu to select a token set. The baseline extended FAB token sets are organized by common tokens, then by surface and branded color styles. Other color styles like primary, secondary, and tertiary are still used by the latest extended FABs.

##### Baseline colors

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] 3 baseline extended FAB color roles.

_Extended FAB color roles used for light and dark schemes:
- Primary container + shadow
- On primary container
- On primary container_

###### Additional color mappings

Extended FABs can use other combinations of container and icon colors. The color mappings below provide the same legibility and functionality as the default, so the color mapping you use depends on style alone.

[IMAGE] 3 deprecated extended FABs with different container and icon colors.

_Extended FABs can use different combinations of container and icon colors_

##### Baseline states

States are visual representations used to communicate the status of a component or interactive element. Learn more about interaction states

[IMAGE] 4 states of baseline extended FAB.

_- Enabled
- Hovered
- Focused
- Pressed_

##### Baseline measurements

[IMAGE] Margins of baseline extended FAB.

_Extended FABs have a padding of 16dp_

[IMAGE] Size of baseline extended FAB while on screen.

_Extended FAB height, width, and icon size_

| Attribute | Value
|

Container height |

56dp
|

Container width |

Dynamic, 80dp min
|

Container shape |

16dp corner radius
|

Icon size |

24dp
|

Padding |

16dp

## Guidelines

[IMAGE] Vibrant extended FAB on an email screen.

_Extended FABs are more prominent than regular FABs_

#### Usage

Use an extended FAB on screens with long, scrolling views that require persistent access to an action, such as a checkout screen.

Use it when label text helps understand the main action, or to add further emphasis to the button.

[IMAGE] A centered extended FAB is used to check out in a shopping app.

_Extended FABs ensure the main action is visible at all times_

[IMAGE] Extended FAB on an article with lots of body content to publish that article.

_Use an extended FAB to provide constant access to a primary action above long-scrolling surface content_

[IMAGE] Extended FAB on a task list to create a new task.

_Use an extended FAB to emphasize a page’s primary action_

##### Additional emphasis

The extended FAB can provide more emphasis and clarity to a product’s primary action.

Since it has room for both a text label and icon, the extended FAB can be effective where an icon alone is ambiguous. However, the relationship between an extended FAB's icon and label should be clear.

[IMAGE] Extended FAB labeled “find flights” with an airplane icon, which would be unclear on its own.

_An extended FAB can be effective where an icon alone is too vague_

Like the regular FAB, only one extended FAB should be used per screen.

Multiple FABs compete for attention.

If additional high-level actions are required, consider adding more buttons elsewhere on the page.

[IMAGE DO] An extended FAB used on a screen.

**DO:** _Only show one prominent action at a time with the extended FAB_

[IMAGE DON'T] 2 extended FABs used on 1 screen.

**DON'T:** _Don’t use multiple extended FABs in one screen as it disrupts visual hierarchy_

The extended FAB shouldn't be used as an option in a set of actions.

Instead, use filled buttons for a similar level of emphasis.

[IMAGE DO] Filled button labeled “finish setup” next to a “back” button.

**DO:** _Use a button with appropriate styling to emphasize it in a group of buttons_

[IMAGE DON'T] Extended FAB labeled “finish setup” next to a “back” button.

**DON'T:** _Don’t use the extended FAB to convey an option in a set of actions_

##### Choosing a size

There are three variants of extended FABs: small, medium, and large.

Choose an appropriately-sized extended FAB to add the right amount of emphasis for an action.

In compact windows with one prominent action, the large extended FAB can be appropriate.

In larger breakpoints, use a medium or large extended FAB.

[IMAGE] 1 large, 1 medium, and 1 small extended FAB on 3 different screen sizes.

_There are three sizes of extended FABs_

#### Anatomy

[IMAGE] 3 extended FAB elements.

_- Container
- Label text
- Icon (optional)_

##### Container

The extended FAB container is a rounded rectangle that hugs its contents.

The extended FAB grows and shrinks with text length.

[IMAGE] Fixed-width extended FAB, centered, ignoring layout grid.

_The extended FAB container hugs the icon and text_

##### Icon (optional)

An extended FAB's icon should intuitively represent its action.

[IMAGE DO] Extended FAB without an icon, labeled “Save draft”.

**DO:** _Unlike standard FABs, extended FABs don't require an icon_

[IMAGE DON'T] Extended FAB with icon only, with no label text.

**DON'T:** _An extended FAB can't have an icon without a text label_

##### Label text

The extended FAB’s label should clearly describe its action.

Use 1–2 words at most. Keep in mind that localization may increase the amount of characters and width of the extended FAB.

[IMAGE DO] Extended FAB with short text “Save”.

**DO:** _Shorten the text as much as needed. Include an icon for additional context._

[IMAGE DON'T] Extended FAB with wrapping text “Save draft in folder”.

**DON'T:** _Avoid wrapping or truncating text_

#### Placement

[IMAGE DO] Extended FAB placed above navigation bar.

**DO:** _Place the extended FAB above the rest of the UI, off of elements like app bars_

[IMAGE DON'T] Extended FAB overlaid on a docked toolbar.

**DON'T:** _Don’t place the extended FAB on top of toolbars. It disrupts the consistency of the elevation and surface layers._

[IMAGE DON'T] Extended FAB below an app bar at the top of a mobile screen.

**DON'T:** _Don’t place the extended FAB in the upper half of a mobile screen, as it disrupts the reading of the UI_

[IMAGE DON'T] An extended FAB labeled "Confirm" on a dialog to "Confirm your location".

**DON'T:** _Don’t place extended FABs on cards or inside other containers_

Avoid putting other floating components, like the floating toolbar, on screen with the extended FAB.

[IMAGE DON'T] The extended FAB is next to a floating toolbar.

**DON'T:** _Floating toolbars can be paired with FABs, but not extended FABs_

#### Responsive layout

The FAB and extended FAB can transform into each other depending on available space and layout.

In a collapsed navigation rail, a FAB would be used. When the rail is expanded, the FAB can transform into an extended FAB.

[IMAGE] Example of extended FAB transforming into standard FAB.

_When space is limited, an extended FAB can transform into a FAB_

##### Right-to-left languages

Extended FABs should mirror their elements in right-to-left (RTL) languages.

[IMAGE] Extended FAB in a left-to-right language placed at the bottom right of a screen. The icon is to the left of the text.

_Icons should be placed to the left of labels for left-to-right (LTR) languages_

[IMAGE] Extended FAB in a right-to-left language placed at the bottom left of a screen. The icon is to the right of the text.

_Icons should be placed to the right of labels for RTL languages_

##### Breakpoints

In compact and medium breakpoints, the extended FAB should be placed at the bottom of the screen, either center-aligned or aligned to the trailing edge of the window.

[IMAGE] Extended FAB center-aligned on a mobile screen.

_The extended FAB can be center-aligned_

[IMAGE] Extended FAB right-aligned on a mobile screen.

_The extended FAB can be aligned to the trailing edge of the window_

In expanded and larger breakpoints, the extended FAB should appear either:
- At the bottom right edge of the window, in both LTR and RTL languages
- Within the navigation rail

[IMAGE] Extended FAB at bottom right of screen.

_The extended FAB can be right-aligned in both LTR and RTL languages_

[IMAGE] Extended FAB in navigation drawer.

_The extended FAB can be at the top of the expanded navigation rail_

#### Behavior

##### Appearing

The extended FAB surface expands when appearing on screen using an enter and exit transition pattern.

[VIDEO] Extended FAB appearing on screen.

_An extended FAB expands when appearing on screen_

##### Expanding

The extended FAB can expand and adapt to any shape using a container transform transition pattern.

This includes a surface that is part of the app structure, or a surface that spans the entire screen.

[VIDEO] Extended FAB adapting to various shapes.

_An extended FAB can expand and adapt to any shape_

##### Transforming

The extended FAB can transform into a FAB on scroll to temporarily take up less space on screen.

[VIDEO] Extended FAB transforming into a FAB when the page is scrolled.

_An extended FAB can transform into a FAB_

##### Scrolling

The extended FAB can transform into a FAB when scrolling down, and back to an extended FAB when scrolling up.

[VIDEO] Extended FAB transforming while scrolling.

_An extended FAB collapses and expands when scrolling_

When the FAB switches to an extended FAB, the following transitions occur:
- The FAB shape changes
- FAB icon moves to the left
- FAB text label fades in

[VIDEO] FAB transforming into extended FAB.

_FAB switches to an extended FAB_

## Accessibility

#### Use cases

People should be able to do the following using assistive technology:
- Navigate to and activate the extended FAB

#### Interaction & style

To make it easier for users of screen readers to reach a primary action such as an extended FAB, consider placing the action in the upper left region of large web screens, like in an expanded navigation rail.

In smaller windows, the best place for the extended FAB is the lower right corner of a screen.

[IMAGE] On a large screen, the Compose FAB is placed on the upper left region in an email app with the expanded window on the right.

_Extended FABs can be placed in the expanded navigation rail_

[IMAGE DO] In a compact window, the Compose FAB is placed on the lower right region in an email app.

**DO:** _Place extended FABs in an easy-to-reach place that doesn’t obstruct other actions_

[IMAGE DON'T] In a compact window, the Compose FAB is overlapping other buttons in an email app.

**DON'T:** _Don’t place extended FABs over another actionable element_

#### Initial focus

Ensure the extended FAB is prioritized in the overall focus order to create an efficient experience for people who navigate UIs with assistive tech.

On mobile, the focus order may start with the app bar, move to the navigation bar, and then skip past any other content on the page to land on the extended FAB.

When using an extended FAB, both the visible label and icon should be treated as one focusable element. The extended FAB doesn’t need a tooltip because it already has a visible label.

[IMAGE DO] A focused extended FAB in the lower right region of a mobile screen.

**DO:** _Ensure extended FABs get focus when navigating with assistive technology_

[IMAGE DON'T] A focused extended FAB with a tooltip matching the text label.

**DON'T:** _Tooltips aren’t required since the extended FAB has label text_

#### Keyboard navigation

|
Keys |
Actions
| **Tab** | Moves focus to the extended FAB
| **Space **or **Enter**
| Activates the extended FAB

#### Labeling elements

To ensure the action is clear, use consistent icons and text labels, such as a **Compos****e **icon with a **Compose** text label.

The icon and text label combination should have one distinct purpose.

The accessibility label must include the same first word as the visible label. For example, if the visible button is **Create**, then the accessibility label might say **Create a new invite**.

[IMAGE] Accessibility labels of an extended FAB.

_The accessibility label reads **Compose **to match the extended FAB's displayed label_
