# Unlocking component flexibility with slots in Figma
Source: https://m3.material.io/blog/material-3-slot-components-figma

> One less reason to “detach instance”

Published: 2024-11-04

Material Design is excited to introduce **Slot Components**, a powerful technique that allows you to create flexible and dynamic layouts with ease in the Material 3 Figma Design Kit.

Slot components unlock local custom component nesting in Figma. With this added flexibility to make changes and edits to components, slot components remove the need to disconnect from the main Material Design Kit. Components can now remain linked to the main library to receive updates and improvements as you change them to suit your needs.

Slot components are a step toward supporting component flexibility that makers need by:
- Allowing teams to extend component possibilities while still inheriting future updates and fixes.
- Designers no longer need to Detach components for unique variants.
- Material components are no longer fixed to only preconfigured offerings. Designers can now add custom variations to them!
- Material Design components are simpler and more powerful than ever.

Adding slots to components enables them to better express the maker’s needs. Slots gives designers the flexibility to create new component variations while still using the guidelines of Material’s design system.

This aligns with how these container-like components are designed with how they are built–– crafting the design toolkit to mimic the component capabilities in code.

## How to use slots

Explore the Material Slot Component Playground file in the Figma Community.

Some container oriented components (i.e. cards) now support Slots.

Each slot enabled component will have a unique instance for activating the slot.

Select the Slot property to change to the Slot placeholder content and reveal options related to the slot

The slot can then be swapped for a local instance of another component.

## Prior to slots

Prior to slots, the design kit offered a robust range of variations and possible component permutations for designs–– allowing different aspects to be manipulated or toggled on/off to ensure safe guide-rails and preserve component boundaries and design intent.

Behind the scenes this requires very complex library building to capture all possible iterations, exacerbated by newly introduced component features.

These complex components served as a great “jumping off point” for most users of the design kit. However for other use cases, subtle deviations (that would otherwise still adhere to guidelines) would require designers to detach from the master library and break these components to accomplish their goals. The result is designers lose their connection to the Material Design Kit library, component variations, state controls, and future library updates.

## Flexibility with slots

Slots serve as an empty component placeholder that designers can swap for any kind of custom content like Material or local custom components.

Components like Cards, Sheets, Lists, Menus and Toolbars are containers that house other elements within such as buttons and strings of text. Introducing Slots to container components adds the flexibility for components to contain any kind of content rather than having the content fixed or baked into the component itself. This also brings closer alignment with how these components are built in code.

(Guide to components in Figma)

The **slot component** technique is a powerful method that can help you create flexible and dynamic layouts with ease. As the Material library grows, more components will offer slot functionality. It's a great addition to Material's Figma Design Kit and we encourage you to try it out today!

[IMAGE] Material default card component beside slot enabled card: 

[IMAGE] Card component showing figma layout choices to enable slot: Slot enabled components will provide a layout override

[IMAGE] Slot component showing Figma instance swap functionality: Slot swap allows for quick replacement for local components

[IMAGE] Local component of grouped avatars replacing a leading slot position of a list item:
