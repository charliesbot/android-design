# Cards
Source: https://m3.material.io/components/cards

## Overview

- Use cards to contain related elements
- Three variants: elevated, filled, outlined
- Contents can include anything from images to headlines, supporting text, buttons, and lists
- Can also contain other components
- Cards have flexible layouts and dimensions based on their contents

[IMAGE] 3 variants of cards

_- Elevated card
- Filled card
- Outlined card_

#### Availability & resources

#### Differences from M2

- Color: New color mappings and compatibility with dynamic color
- Elevation: Lower elevation and no shadow by default
- Variants: Three official card variants – elevated, filled, and outlined

[IMAGE] M3-style card

_Cards have updated colors, elevation, and variants_

## Specs

#### Tokens & specs

Select a component variant below to see its elements, attributes, tokens, and their values.

#### Elevated card

[IMAGE] Diagram indicating elevated card container.

_- Container_

##### Elevated card color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] Color diagram indicating elevated card surface color.

_Elevated card color roles used for light and dark themes:
- Surface container low_

##### Elevated card states

States are visual representations used to communicate the status of a component or interactive element. Learn more about interaction states

[IMAGE] Diagram of 5 elevated card states.

_Elevated card states:
- Hovered
- Focused
- Pressed
- Dragged
- Disabled_

#### Filled card

[IMAGE] Diagram indicating filled card container.

_- Container_

##### Filled card color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] Color diagram indicating filled card surface color.

_Filled card color roles used for light and dark themes:
- Surface container highest_

##### Filled card states

States are visual representations used to communicate the status of a component or interactive element. Learn more about interaction states

[IMAGE] Diagram of 5 filled card states.

_Filled card states:
- Hovered
- Focused
- Pressed
- Dragged
- Disabled_

#### Outlined card

[IMAGE] Diagram indicating outlined card container and outline.

_- Container
- Outline_

##### Outlined card color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] Outlined card color roles in light and dark themes.

_Outlined card color roles used for light and dark themes:
- Surface
- Outline variant_

##### Outlined card states

States are visual representations used to communicate the status of a component or interactive element. Learn more about interaction states

[IMAGE] Diagram of 5 outlined card states.

_Outlined card states:
- Hovered
- Focused
- Pressed
- Dragged
- Disabled_

#### Measurements

[IMAGE] Diagram with card layout measurements.

_Card padding and size measurements_

| Attribute
| Value

| Shape
| 12dp corner radius
| Left/right padding
| 16dp
| Padding between cards
| 8dp max
| Label text alignment
| Start-aligned

## Guidelines

[IMAGE] 3 variants of cards: elevated, filled, and outlined.

#### Usage

Use a card to display content and actions on a single topic.

Cards should be easy to scan for relevant and actionable information.

Elements like text and images should be placed on cards in a way that clearly indicates hierarchy.

[IMAGE] Example card containing an image, title, text, and button.

_Cards can display content and actions on a single topic_

Cards can serve as entry points into deeper levels of detail or navigation, such as a music album or details on an upcoming vacation.

[IMAGE] Example world tour card.

_Card text and image show a clear hierarchy_

[IMAGE] Card displaying connected details about a world tour.

_Use cards to display related information on a single subject_

Cards can be displayed together in a grid, vertical list, or carousel.

[IMAGE DO] 4 cards together in a grid layout.

**DO:** _Cards can be shown together_

[IMAGE DON'T] 5 albums in a vertical list of cards.

**DON'T:** _Don't force content into cards when spacing, headlines, or dividers would create a simpler visual hierarchy_

There are three card variants:
- Elevated
- Filled
- Outlined

Each provides the same legibility and functionality, so the variant you use depends on style alone.

[IMAGE] 3 variants of cards.

_- Elevated card
- Filled card
- Outlined card_

[IMAGE] Example elevated card.

_Elevated cards have a drop shadow, providing more separation from the background than filled cards, but less than outlined cards_

[IMAGE] Example filled card.

_Filled cards provide subtle separation from the background. This has less emphasis than elevated or outlined cards._

[IMAGE] Example outlined card.

_Outlined cards have a visual boundary around the container. This can provide greater emphasis than the other variants._

#### Anatomy

The card container is the only required element in a card. Card layouts can vary to support the kinds of content they contain. Below is a common configuration of elements.

[IMAGE] Diagram labeling the 6 parts of card anatomy.

_- Container
- Image
- Button
- Supporting text
- Subhead
- Headline_

##### Container

Card containers hold all card elements. Their size is determined by the space those elements occupy. Card elevation is expressed by the container.

The card container is the only required element of a card. All other elements are optional.

[IMAGE] 3 card containers with various elements: 1 with all elements except a button. 1 with a container, headline, supporting text, button. 1 with a container, headline, supporting text, 2 buttons.

_Card size is determined by the elements it contains_

##### Content blocks

Card contents are grouped into blocks. Content can have different levels of visual emphasis depending on importance.

Card layouts vary to support the kinds of content they contain.

[IMAGE] Diagram of card content blocks.

_Cards can contain a headline, subhead, supporting text, media, and actions_

##### Dividers

Dividers can separate regions in cards or indicate areas of a card that can expand.

[IMAGE] A divider running the entire width of the card.

_1. Use full-width dividers for content that can be expanded_

[IMAGE] An inset divider indented from the edge of card.

_1. Use inset dividers, which don’t run the full width of a card, to separate related content_

##### Media

**Thumbnail**
Cards can include thumbnails for an avatar or logo.

**Image**
Cards can include photos, illustrations, and other graphics, such as weather icons.

**Video**
Cards can include video.

[IMAGE] A mobile chat app with: 5 cards with images, 1 card with a thumbnail avatar, and 1 card with a video.

_Cards can contain thumbnails, images, and video_

##### Text

**Headline**
Headline text often communicates the subject of the card, such as the name of a photo album or article.

**Subhead**
Subheads are smaller text elements, such as an article byline or a tagged location.

**Supporting text**
Supporting text includes body content, such as an article summary or a restaurant description.

[IMAGE] card container with several elementsA tablet email app with an email summary card with multiple text elements.

_Headline, subhead, and supporting text in a card_

###### Layering text, icons, and images

It isn’t recommended to place text or icons on images. If it’s necessary, ensure the background image provides sufficient contrast for the text to meet accessibility standards.

Add a translucent scrim or bounding shape beneath the text or icon to help ensure proper contrast.

[IMAGE CAUTION] Layered text contrasts with the background image.

**CAUTION:** _Ensure that text on images meets accessible contrast standards_

[IMAGE CAUTION] Icon within a bounding shape, placed on an image.

**CAUTION:** _When placing text or icons on images, consider using a bounding shape to ensure proper contrast_

##### Actions

###### **Primary action area**

Cards can be one large touch target triggering an expanded detail screen.

[IMAGE] The action area of a card contains rich media and supporting text.

_Cards can include a primary action area that expands into a full-screen view_

**Buttons**
Cards can include buttons for actions such as **Learn more** or **Add to cart**.

**Icon buttons**
Cards can include icon buttons for actions such as **Save**, **Heart**, or **Leave a 4-star review**.

**Selection controls**
Cards can also include chips, sliders, checkboxes, and other selection controls.

**Linked text**
There can be a link in the supporting text on a card.

[IMAGE] Supplemental text and actions at the top and bottom of the card.

_Cards can include multiple action areas containing buttons, links, and other controls_

[IMAGE] Album card with an option to give a star rating.

_Cards can contain icon buttons like stars to rate content_

[IMAGE] Card to purchase tickets with choice chips for 3 event times.

_Cards can contain choice chips in the action area_

[IMAGE] Card with slider to control a song’s volume.

_Cards can contain a slider control in the action area_

**Overflow menu**
Overflow menus contain related actions. They are typically placed in the upper-right or lower-right corner of a card.

[IMAGE] 2 cards: 1 with an overflow menu in the upper-right corner, the other with it in the lower right.

_Overflow menus are usually located in the upper-right or lower-right corner of a card_

#### Cards in a collection

Multiple cards can be grouped together into collections displayed in a grid, list, or carousel.

By default, cards in a collection are coplanar. They share the same resting elevation unless they're picked up or dragged.

[IMAGE] 9 cards in a grid layout.

_Multiple cards can be grouped into collections with a shared resting elevation_

###### Filtering and sorting

Card collections can be filtered in a variety of ways, including by date or alphabetical order. If a collection can be filtered, the filter must apply to each card in the collection.

Filter or sorting options should be placed outside of the card collection.

[IMAGE] A sort-by-date option placed above a card collection.

_Card collections can be filtered in a variety of ways, including by date:
1. A sort-by-date option is placed outside of the card collection_

Organize card collections so that they'e easy to use. Their layout affects how they are perceived.

[IMAGE] A template for an 8-card collection layout.

_Place cards in a collection in a straightforward, easy-to-use manner_

##### Grid

Cards can be displayed together in a grid.

[VIDEO] A set of 4 cards in a grid.

_Cards displayed in a grid_

The default grid can be customized in code to show cards in staggered or mosaic grids.

[IMAGE] 5 menu item cards in a mosaic grid.

_Custom mosaic grid_

[IMAGE] 4 menu item cards in a staggered grid.

_Custom staggered grid_

##### Vertical list

Cards can be displayed together in a vertical list.

[VIDEO] 3 cards in vertical list.

_Cards can be shown in a vertical list_

##### Carousel

Cards can be displayed together in a horizontal row or carousel.

[VIDEO] 3 event reservation cards in a horizontal row.

_Cards displayed together in a horizontal row or carousel_

#### Adaptive design

As cards scale to adapt to different breakpoints, their position and alignment can also change.

Cards and their elements can align left, right, or center as the layout scales.

[IMAGE] 2 cards on a mobile screen row expand to 4 cards on a tablet screen row.

_Card position and alignment changes as the screen size changes_

##### Ergonomics

Adjust the layout of cards to meet the ergonomic needs of large screens.

For example, a horizontally-oriented card in a compact breakpoint may become a larger, vertically-oriented card in an expanded breakpoint, with more space for images and text on the larger screen.

[IMAGE] Card sizes change from mobile to tablet, with larger images in the tablet layout.

_Adjust the card layout so content remains the main focus on large screens_

##### Visual presentation

To adjust the presentation of content-focused components, begin with spacing.

Allow components like lists, cards, and images to optimize space while filling the region of a screen that suits a device breakpoint’s ergonomic needs.

[IMAGE] 2 cards with optimized space: 1 narrow rectangle, 1 wide square.

_Spacing adjusts for components such as cards, lists, and images_

[IMAGE] 2 examples of the same card: 1 vertical with an image at the top, 1 horizontal with an image on the left.

_Example of the same card with two different orientations and element positioning_

##### Column-based layouts

In mobile layouts, components such as lists or cards are stretched to fit the full width of the screen without compromising visual quality or user experience. When designing for large screens with an expanded breakpoint, use multiple columns to display content.

Avoid extending UI elements across the screen when possible. On larger screens, rearrange groups of related cards into horizontal rows or carousels, to allow for better content organization.

[IMAGE] 3 related cards in a carousel.

_When designing for large screens, use multiple columns to display content_

##### Small screens

On smaller screens with the compact breakpoint, consider swapping cards for lists, which can display images and text in a more compact form.

Make sure that controls, actions, and other component-specific elements are maintained.

[VIDEO] Cards on a large screen adapt to a list on a small mobile screen.

_Certain devices or user contexts require different components to meet platform expectations_

#### Behavior

##### Expanding

Cards can use a container transform transition pattern to reveal additional content. Reserve this pattern for hero moments that are meant to be expressive.

[VIDEO] An album card expands to fill the screen and shows additional related content.

_A card expands to fill the full screen using a parent-child transition_

[VIDEO DO] A card expands, showing all content at once.

**DO:** _Expand a card to reveal information_

[VIDEO DON'T] A card expands, internally scrolling to only show some information.

**DON'T:** _Don’t scroll within a card to reveal information_

##### Navigation

Cards can use a forward and backward transition pattern to navigate between screens at consecutive levels of hierarchy. This pattern has a simpler motion style compared to container transform, which makes it suitable for common navigation transitions.

[VIDEO] Card interaction transitioning to full screen.

_Cards can use a forward and backward transition pattern to navigate between screens_

##### Gestures

###### Swipe

A swipe gesture can be performed on a single card at a time, anywhere on that card.

It can be used to:
- Dismiss a card
- Change the state of a card, such as flagging or archiving it

[VIDEO DO] A card with 1 assigned swipe: to heart the entire card.

**DO:** _A card should only have one swipe action assigned to it_

[VIDEO DON'T] A card with an image carousel and a swipe motion to heart only the bottom text on the card.

**DON'T:** _Cards shouldn’t contain content that can be swiped, such as an image carousel or pagination. Also, swipe gestures shouldn’t cause portions of cards to detach upon swiping._

###### Pick up & move

The pick-up-and-move gesture allows users to move and reorder cards in a collection.

[VIDEO DO] A card is elevated above all other cards when moved.

**DO:** _When moving a card, increase its elevation_

[VIDEO DON'T] A moved card bumps other cards around, and isn’t elevated above them.

**DON'T:** _Don’t let cards bump other elements out of the way. When a card is picked up, it appears in front of all elements, except app bars and navigation._

###### Scrolling

Card content that’s taller than the maximum card height is truncated and doesn’t scroll, but can be displayed by expanding the height of a card.

A card can expand beyond the maximum height of the screen, in which case the card scrolls within the screen.

[VIDEO DO] Scrolling a page with an expanded card.

**DO:** _On a mobile device, cards can expand to reveal more content, scrolling within the screen. Content within cards doesn’t scroll._

[VIDEO DON'T] Expanded card on mobile with internal scrolling.

**DON'T:** _On a mobile device, cards can't internally scroll, as it could cause two scroll bars to be displayed_

###### Scrolling on desktop

On a desktop device, card content can expand and scroll within a card.

[VIDEO] Expanded card on desktop with internal scrolling.

_On a desktop, content can expand and scroll within a card_

## Accessibility

#### Use cases

People should be able to do the following using assistive technology:
- Navigate to a card and the elements within a card
- Get appropriate feedback based on input type documented under Interaction & style

#### Interaction & style

A card can be a non-actionable container that holds actions like buttons and links, or it can be directly actionable without any buttons or links. This is to avoid stacking actionable elements. An action shouldn’t be placed on an actionable surface.

[IMAGE] 2 possible card interaction behaviors.

_- Non-actionable card with buttons
- Directly actionable card with no buttons_

##### Touch

When a user taps on a directly actionable card, a touch ripple appears across the card, indicating feedback.

Non-actionable cards don’t ripple.

[VIDEO] Ripple effect created when tapping on a card.

_Touch: Tap_

##### Dragging and dismissing

To meet Material's accessibility standards, any dragging and swiping interactions need a single-pointer alternative, like selecting the same actions from a menu.

For example, tapping a card, or pressing and holding, should open a menu to change its position in a list. That menu could also contain an action to delete the card.

[VIDEO] A list of cards has its order reshuffled using a single-pointer option.

_Use containers like bottom sheets or menus to show single-pointer options_

It isn’t recommended to place menus on top of the card on the draggable state. If doing so is necessary, ensure that the interaction can be completed.

[IMAGE CAUTION] A menu over a card that doesn't totally obscure the card.

**CAUTION:** _Ensure that the menu doesn't cover the card_

##### Cursor

When a directly actionable card is hovered, the hover state provides a visual cue to the person that the element is interactive. Non-actionable cards don’t have a hover state.

When a directly actionable card is clicked, a ripple appears, providing feedback.

[VIDEO] Hover state and touch ripple effect of a card.

_Cursor: Hover, Click_

##### Keyboard

A focus indicator appears around actionable elements when tabbing through cards. This provides a visual cue to a person that the destination is now focused and an action can be taken.

A person can **Tab** to navigate between actionable elements of the cards. If the cards are non-actionable, **Tab** navigates directly to the actionable buttons or links within the cards.

When engaging with a focused actionable card or element using the **Space** or **Enter** key, an action is performed or a secondary action is available, such as a menu.

Within the menu, a person is able to **Arrow** through the menu items, **Space** or **Enter** to select an item, or **Tab** to exit.

[VIDEO] Navigating cards with a keyboard.

_Keyboard: Tab, Arrows_

#### Focus

All interactive elements of cards need a tab stop so they can be focused. Directly actionable cards are tab stops.

For non-actionable cards, the card itself is not a tab stop. However, every actionable element in the card is a tab stop so they’re all visited before focus navigates to the next card.

[IMAGE] The focus areas of a card with interactive elements.

_Use **T****ab** to navigate through all buttons in a card_

[IMAGE] Cards forming a list on mobile and cards forming a gallery on tablet.

_Card layouts can change on different devices_

#### Keyboard navigation

| Keys | Actions
| **Tab** |

Move to the next actionable element

**Directly actionable cards:** Move to next card container

**Non-actionable cards with actionable elements:** Move to next actionable element
| **Space** or **Enter** | Confirm action

#### Labeling elements

The informative contents of a card are verbalized when navigating to them using a screen reader. If an image in a card is purely decorative, hide it from screen readers. All actionable elements must receive both screen reader and keyboard focus.

Directly actionable cards can have the **button** or **link** role, depending on how they’re used.

Non-actionable cards are purely containers, so they don’t need a role.

[IMAGE] Card elements annotated in the order that a screen reader tabs through them.

_Non-actionable card elements are navigable, focused in order, and verbalized when in focus. In this example, the order is:
- Heading
- Image
- Body text
- Primary button
- Secondary button_
