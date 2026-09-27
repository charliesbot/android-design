# Spacing
Source: https://m3.material.io/styles/spacing

> Spacing is the distance around and between component and layout elements

## Overview

- Apply spacing tokens to the margins, padding, and gaps of a component, UI element, or layout
- Adapt spacing to different values based on context, like mobile vs desktop, or density settings
- The spacing system is measured on an 8dp scale, where **space100 = 8dp**
- Spacing is applied to the flow of elements (horizontal, vertical), or in relation to the elements (leading, trailing, top, bottom, gap)
- More details on grid & spacing design principles

[IMAGE] Bottom padding of small and large buttons is space200 and space400. Leading padding is space300 and space600.

_Spacing should adapt to component size, layout, form factor, and other contexts_

#### Availability & resources

|

Type |

Resource |

Status
|

Implementation |

Android Views (MDC-Android) |

Unavailable
|

Jetpack Compose |

Available
|

Web |

Unavailable

#### Spacing units

Spacing units follow an 8dp scale. Rather than defining every value, Material only defines the most recommended spacing unit values on the scale. The system can be extended to add more spacing units and patterns as needed. More on spacing tokens

[IMAGE] A spacing scale shows 2, 4, 6, and 8 at the bottom range and 48, 56, 64, and 72 at the top of the range.

_Spacing units are are created as a multiplier from the baseline unit of 8dp, which is space100_

##### Component layouts

In **components**, spacing units define the padding and gaps between individual elements of a component, such as text, icons, and controls.

[IMAGE] Padding and gaps applied to a mobile layout and 2 components.

_Spacing concepts for components:
- Vertical padding (top & bottom)
- Vertical gap
- Horizontal padding (leading & trailing)
- Horizontal gap_

##### Page layouts

In **layouts**, spacing units standardize the overall composition of the page, like where text, UI elements, and components go.

Layouts use:
- Panes, spacers, and margins to structure the page
- Padding and gaps to organize content within the panes.

[IMAGE] Padding and gaps applied to a mobile layout and 2 components.

_Spacing concepts for layouts:
- Margin
- Top padding
- Horizontal padding (leading & trailing)
- Spacer (gap)
- Vertical gap_

#### Parts of spacing

Spacing has three categories: **padding**, **gaps**, and **margins**.
- Padding: Space inside an element
- Gap: Space between elements in a grid or container
- Margins: Space outside an element

The position of the spacing can be **vertical**, **top**, **bottom**, **horizontal**, **leading**, or **trailing**.
- **Leading** and **trailing** edges swap sides in right-to-left (RTL) languages. More on RTL layout design

For example, the search container has:
- 8dp vertical padding
- 8dp horizontal gaps
- 24dp horizontal margins (12dp when focused)

[IMAGE] Default search spacing.

_Search horizontal margins are 24dp by default to ensure accurate placement from the screen edge_

[IMAGE] Active search spacing.

_Search margins change to 12dp when focused, while the container padding and horizontal gaps remain the same_

##### Padding

Padding is the spacing inside an element. It adds a buffer from the container edge to the content, like text and icons.

[IMAGE] Examples of each possible padding value.

_Padding can be horizontal or vertical, or on a specific edge: leading, trailing, top, or bottom_

##### Gaps

Gaps are the spaces between elements in a grid or container. Horizontal gaps are between side-by-side elements, and vertical gaps are between stacked elements.

Use a **horizontal gap** and **vertical gap** for simple components where gaps are always the same size.

Complex components with many different gaps should define them by the elements on each side, like an **icon-label gap.**

[IMAGE] Examples of each possible gap value.

_Gaps can be horizontal, vertical, or defined by the elements around it_

##### Margins

Margins are the spacing outside an element. They add a buffer between the element and the parent container or screen edge.

**Use padding & gaps before using margins:**
- Material rarely uses margins in components; padding and gaps tend to apply spacing in a more uniform way
- Only use margins to apply further spacing beyond the parent container’s padding, or in layouts

[IMAGE] Examples of each possible margin value.

_Margins can be horizontal or vertical, or on a specific edge: leading, trailing, top, or bottom_

[IMAGE DO] Button with uniform container horizontal padding.

**DO:** _Define padding and gaps on the parent container to organize all elements inside_

[IMAGE DON'T] Button icon with different leading and trailing margin values.

**DON'T:** _Avoid defining margins on child elements as they usually aren’t uniform, and require more tokens_

## Applying spacing

Material’s spacing system is intentionally designed to be a simple linear scale. Unlike the color system, which adjusts light and dark theme logic across all components at once, tailored spacing logic is built within each component.

[IMAGE] Component and system token mapping for the leading padding of three different icon buttons.

_Components have padding tokens that map to system tokens, which resolve to final values_

#### What to use

##### Pre-tokenized components

Some Material components map to spacing system tokens out of the box. This mapping can be customized by products to adapt to form factor or density.

Note: Work is ongoing to hook up all Material components to spacing tokens.

###### System tokens

Spacing system tokens define the recommended values. Apply these to your product’s custom components and layouts, replacing any hardcoded values.

If the right system token doesn’t exist, customize the system and add your own.

[IMAGE] Button with spacing tokens for all padding and gaps.

_Many Material components map to spacing system tokens_

[IMAGE] A list of spacing system tokens from 100 to 400.

_System tokens define all spacing values recommended by Material_

##### Spacing complements text scaling

When text is scaled up to 200%, the same spacing should be preserved by default.

[IMAGE] A button with text scaled to 200% uses the same spacing tokens as an unscaled button.

_Keep the same spacing when text scales_

#### Customizing the system

The spacing system is meant to capture Material’s design intent, but customization is expected and often necessary. How you customize the system depends on your needs:

##### Customize Material’s existing component spacing

Use this approach to customize how the base component appears across the entire product.
- For example, change the “button top padding” mapping from **space125** to **space200** for a taller default button.

[IMAGE] A button’s vertical padding is changed from space125 to space200.

_Customize component spacing to change it across the entire product_

##### Add custom system spacing & patterns

Use this approach when you need spacing units beyond what Material provides, or have common adaptive spacing patterns in your product.
- Follow the multiplier pattern for new space tokens, so **space225 = 18dp** (8dp x 2.25).
- Spacing patterns unique to your product can be tokenized.
  - For example, if cards and sheets adapt horizontal content padding the same way, you could create a **surface content horizontal padding** token for that pattern.

[IMAGE] An outlined card and a bottom sheet both use the surface-content.padding.horizontal spacing pattern token.

_For products that adapt spacing the exact same way, consider creating a token for that pattern_

##### Add adaptive layout & density

Use this approach when you want the same core component, but want it to appear differently in specific situations, like screen sizes and density settings.
- Adaptive layout: Map the spacing to different system tokens for each device type, such as mobile or desktop
- Density: Adapt vertical padding to different spacing values for each setting

[VIDEO] A button’s spacing changes based on if it’s on mobile, desktop, cars, XR, or TVs.

_Components can be customized to adapt spacing to each form factor_

[VIDEO] A list item’s spacing changes based on density settings of 0 to -4.

_Components can be customized to adapt spacing to density settings_

## Tokens

star

Note:
The spacing system tokens are only used on Jetpack Compose.

#### System spacing tokens

System spacing tokens are a linear range of spacing values recommended by Material. They’re intended to cover the majority of spacing needs within the design system. The base unit of measurement **md.sys.measurement.space100** is **8dp**. Learn more about design tokens

[IMAGE] The spacing system tokens, built from 8dp (1x). The range covers 0x to 9x.

_The main spacing units are multiples of 8dp_

##### Nested units

Values other than multiples of 8 are also used in layouts and Material components, like 2dp, 4dp, 6dp, and 10dp. Material only defines the most recommended nested units.

[IMAGE] The spacing system defines tokens for 0.25x, 0.5x, 0.75x. 1.25x nested units.

_Material only defines nested units that are actively used in common layouts or Material components_

#### Component spacing

Most Material component spacing attributes will map to system spacing tokens. Spacing logic, like adaptive design or density, should be applied to the component attribute.

Component attributes follow a new naming strategy:
- Going forward, all component spacing attributes will use **padding**, **margin**, and **gap**, and positional language: **horizontal**, **vertical**, **leading**, **trailing**, **top**, and **bottom**
  - Example: “Medium button: leading padding”

- Past component spacing tokens use “**space**” to describe all padding, gaps, and margins, like **leading-space**, **trailing-space**, **top-space**, **bottom-space**, and **between-space**.
  - Example: “Medium button: leading space”
