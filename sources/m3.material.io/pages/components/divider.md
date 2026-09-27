# Divider
Source: https://m3.material.io/components/divider

## Overview

- Make dividers visible but not bold
- Only use dividers if items can’t be grouped with open space
- Use dividers to group things, not separate individual items

[IMAGE] Screen shot of five stacked dividers

_Dividers separating items in a list_

#### Availability & resources

#### Differences from M2

- Color: New color mappings and compatibility with dynamic color
- Configurations: Ability to have vertical dividers

[IMAGE] Screen shot of three dividers

_Dividers have new color mappings_

## Specs

[IMAGE] Diagram of divider set on horizontal line

_- Divider_

#### Tokens and specs

Browse the component elements, attributes, tokens, and their values.

#### Color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] Divider on light background and dark background.

_Divider color roles used for light and dark schemes:
- Outline variant_

#### Measurements

[IMAGE] Divider's measurement.

_Measurements_

| Attribute | Value
| Divider full-width
| 100%
| Divider inset left margin
| 16dp
| Divider inset right margin
| 0dp
| Divider middle-inset left margin
| 16dp
| Divider middle-inset right margin
| 16dp
| Space between divider & supporting-text
| 4dp
| Divider right margin
| 8dp
| Divider bottom margin | 8dp

## Guidelines

[IMAGE] Inbox app using full-width dividers to separate messages.

_Full-width dividers_

#### Usage

Dividers are one way to visually group components and create hierarchy. They can also be used to imply nested parent/child relationships.

The divider can be used in two ways:

- Full width
- Inset

[IMAGE] Inbox app using full-width dividers to separate messages.

_Full-width divider_

[IMAGE] Music app using inset dividers to separate songs in a playlist.

_Inset divider_

#### Anatomy

A divider is a simple line.

[IMAGE] Illustration of a divider.

_- Divider_

#### Full-width dividers

Use full-width dividers to separate larger sections of unrelated content. They can be used directly on a surface or inside other components like cards or lists.

Full-width dividers can also separate interactive areas from non-interactive areas.

They are used to group visual elements together, and indicate when elements are related to each other from an interaction perspective.

[IMAGE] Inbox messages visually separated by full-width dividers for improved readability.

_Full-width dividers to indicate separation of content_

[IMAGE DO] Non-interactive information on a card and a clickable read more link separated by a full-width divider.

**DO:** _Use full-width divider lines to separate interactive and non-interactive areas of a container such as a card_

[IMAGE CAUTION] Full-width dividers used after every piece of content on a page.

**CAUTION:** _Use full-width dividers sparingly. Too many divider lines will make an interface look cluttered._

#### Inset dividers

Use inset dividers to separate related content within a section.

Inset dividers are equally indented from both sides of the screen by default.

[IMAGE] 2 different content sections separated by an inset divider.

_Inset dividers separate related content, such as emails in a list. They should be used with anchoring elements such as icons or avatars, and align with the leading edge of the screen._

[IMAGE] 3 types of related content separated by inset dividers on a mobile screen.

_Inset dividers in a list of related items_

[IMAGE] Inset divider separating body text and selection chips.

_Inset dividers can be placed in the middle of a layout to separate elements such as body text from selection chips_

##### Using dividers both ways on the same screen

If dividers are used both ways in a UI, they must reinforce the hierarchy of information within different sections.
- To separate a different kind of content, use a full-width divider
- To separate nested content items, use inset dividers

[IMAGE DO] Dividers used to show content hierarchy: full-width for sections, inset for related items.

**DO:** _Use a combination of inset and full-width dividers to reflect the hierarchy of information_

List items with repetitive formats may not require an inset divider, in which using only the margin between items is acceptable.

[IMAGE DO] List with repetitive item formats. Visual separation achieved through margins only.

**DO:** _Content may not require a divider line_

#### Vertical divider

A vertical divider can be used to arrange content on a larger screen, such as separating paragraph text from video or imagery media.

[IMAGE] Image and text side-by-side, separated by a vertical divider on a large screen.

_Vertical divider in a large screen context_

## Accessibility

Dividers are decorative elements, which have no contrast minimums.

[IMAGE] Divider separating messages in an inbox that are already separated with type styles and spacing is annotated as decorative.

_Decorative elements have no contrast minimums_
