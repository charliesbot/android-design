# Advanced layouts in Compose
Source: https://developer.android.com/develop/ui/compose/quick-guides/content/video/advanced-layouts-compose

See how to build complex designs for your Compose layouts, focusing on layout phase and constraints, subcompose layouts, and intrinsic measurements.

## Key points
- The _layout phase_ is the phase of Compose where element sizing and positioning is defined.
- During the layout phase, each element in the UI tree measures its children, enabling the parent to decide its own size and placing the children in the available 2D space.
- To build a custom layout, call the `Layout` composable, which accepts the composable content as its children.
- Subcomposition enables lazy components to add content on demand while scrolling.
- Subcomposed layouts can have an impact on performance. Use this approach when at least one child's composition depends on the result of another child's measurement.
- Intrinsic measurements let you query children before they're measured.

## Collections that contain this guide

This guide is part of these curated Quick Guide collections that cover broader Android development goals:

###### Display a list or grid
Lists and grids allow your app to display collections in a visually pleasing form that's easy for users to consume.
Quick guide collection
