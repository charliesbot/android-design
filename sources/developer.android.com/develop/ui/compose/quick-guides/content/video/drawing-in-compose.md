# Intro to drawing in Compose
Source: https://developer.android.com/develop/ui/compose/quick-guides/content/video/drawing-in-compose

Learn how to draw something custom in Compose. With custom drawing, you can improve the look and feel of your app when the built-in components don't cover exactly what your app needs.

## Key points
- `DrawScope` is a declarative, stateless drawing API to draw shapes, paths, and more without needing to maintain the state of the component manually.
- Several drawing modifiers give you access to `DrawScope`, letting you draw with other composables:
  - `drawBehind`: draws behind the composables content.
  - `drawWithContent`: useful for rearranging content. You can choose when to call the content of the composable, either before or after.
  - `drawWithCache`: caches the objects until the size changes or the state variables read inside change.

- The coordinate system in Compose is the same as the view system.
- All draw and layout calls are performed in pixel values, not `dp`. To draw consistently across screens, use `dp` and convert to pixels before drawing.
- Draw calls are always relative to the parent composable.

## Collections that contain this guide

This guide is part of these curated Quick Guide collections that cover broader Android development goals:

###### Display images
Discover techniques for using bright, engaging visuals to give your Android app a beautiful look and feel.
Quick guide collection
