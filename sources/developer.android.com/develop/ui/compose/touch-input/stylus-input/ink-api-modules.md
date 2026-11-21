# Modules
Source: https://developer.android.com/develop/ui/compose/touch-input/stylus-input/ink-api-modules

Ink API is modularized, so you can use only what you need.

## Strokes

The strokes module serves as the foundation of the Ink API. Key data types within this module are:
- **`StrokeInputBatch`**: Represents a series of pointer inputs, including their position, timestamp, and optionally pressure, tilt, and orientation.
- **`InProgressStroke`**: Represents a stroke that is actively being drawn. `InProgressStroke` is used to render partial strokes with low latency and to build the final `Stroke` once input is complete, after which the object can be reused. `InProgressStroke` is used by the `InProgressStrokes` composable.
- **`Stroke`**: An immutable representation of a finalized stroke with fixed geometry. Each `Stroke` has an `ImmutableStrokeInputBatch` (input points), a `Brush` (style), and a `PartitionedMesh` (geometric shape). You can store, manipulate, and render strokes within your application.

## Geometry

The Geometry module supports geometric operations on primitive shapes (using dedicated classes like Box and Vec), as well as arbitrary shapes (using PartitionedMesh), including intersection detection and transformation. PartitionedMesh can also hold additional data to support rendering.

## Brush

The `brush` module defines the style of strokes. It consists of two main parts:
- **`Brush`**: Specifies the style of a stroke including base color, base size, and `BrushFamily`. `BrushFamily` is analogous to a font family, it defines a stroke's style. For example, a `BrushFamily` can represent a specific style of marker or highlighter, allowing strokes with different sizes and colors to share that style.
- **`StockBrushes`**: Provides factory functions for creating ready-to-use `BrushFamily` instances.

## Authoring

The Compose Authoring module lets you capture user touch input and render it as low-latency strokes on the screen in real time. This is achieved through the `InProgressStrokes` composable, which processes motion events and displays the strokes as they are drawn.

Once a stroke is completed, the composable notifies the client application using an `InProgressStrokesFinishedListener` callback. This allows the application to retrieve the finished strokes for rendering or storage.

In Compose, `InProgressStrokes` takes this callback in the `onStrokesFinished` parameter. Pass the finished strokes to another composable to commit them to the screen using the rendering module.

## Rendering

The Rendering module simplifies drawing ink strokes onto an Android `Canvas`. It provides `CanvasStrokeRenderer` for Compose and `ViewStrokeRenderer` for view-based layouts. These renderers optimize rendering performance and help deliver high-quality visuals, including antialiasing.

To render strokes, call the `create()` method to get a `CanvasStrokeRenderer` instance, and then call the `draw()` method to render either finished (`Stroke`) or in-progress (`InProgressStroke`) strokes onto a `Canvas`.

You can transform the canvas when you draw a stroke. Examples include panning, zooming, and rotating. To render the stroke correctly, you must also pass the `canvas` transform to `CanvasStrokeRenderer.draw`.

To avoid tracking the `canvas` transform separately, use `ViewStrokeRenderer` instead.

## Storage

The storage module provides utilities for efficiently serializing and deserializing stroke data, primarily focusing on `StrokeInputBatch`.

The module uses protocol buffers and optimized delta compression techniques, resulting in significant storage savings compared to naive methods.

The storage module simplifies saving, loading, and sharing strokes.

Previous  arrow_back   Add dependencies

Next  Define your brushes   arrow_forward
