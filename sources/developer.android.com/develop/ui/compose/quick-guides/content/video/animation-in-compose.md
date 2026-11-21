# Animation in Compose
Source: https://developer.android.com/develop/ui/compose/quick-guides/content/video/animation-in-compose

See how to animate state values, using transitions, animating visibility or size changes and crossfades by using the Compose animation APIs.

## Key points
- The `animate*AsState` API is useful for animating a single value based on a state change.
- To animate multiple values at the same time, create a transition with the `updateTransition` function.
  - You can declare each animation value with an extension function on the transition object.

- To customize the animation behavior, specify the `transitionSpec` parameter.
- `AnimatedVisibility` is useful for animating appearance and disappearance.
- Customize animation behavior by specifying parameters for enter and exit.
- To animate size changes of elements, use the `animateContentSize` modifier.
- To animate changes when you swap out portions of your UI, use the `Crossfade` composable.

## Collections that contain this guide

This guide is part of these curated Quick Guide collections that cover broader Android development goals:

###### Compose basics
This series of videos introduces various Compose APIs, quickly showing you what’s available and how to use them.
Quick guide collection

###### Display images
Discover techniques for using bright, engaging visuals to give your Android app a beautiful look and feel.
Quick guide collection
